-- Client-side guard for the Bandits zombie -> NPC vanilla AttackState path.
--
-- B42.20.3 testing established two failed interventions:
--   1. bAttack cannot be cleared because it is a read-only animation callback;
--   2. setZombiesDontAttack(true) on the Bandit target does not stop Bandits'
--      UpdateZombies() from rebuilding a vanilla combat relationship.
--
-- Upstream UpdateZombies() explicitly calls spotted/addAggro/setTarget/setAttackedBy
-- for a normal zombie -> Bandit pair and independently performs its custom
-- Bite/BiteLow simulation. The normal v3 fallback runs later in OnZombieUpdate
-- and disconnects only the vanilla target with zombie:setTarget(nil).
--
-- A controlled upstream proof-of-concept can set the global marker
-- LCC_BANDITS_ATTACK_BRIDGE_POC. While that marker is active this guard becomes
-- fully observation-only: it does not disconnect targets and does not assert
-- setZombiesDontAttack(true). The PoC must stand or fail on its own behavior.
if isServer() then return end

local Guard = require "LCC/Guard"
local FEATURE = "bandits.attack-state-target-disconnect-v3"
local POC_MARKER = "upstream-pursuit-v1"
local HEARTBEAT_MS = 15000
local HEARTBEAT_CHECK_EVERY_UPDATES = 512

print("[LCC][BanditsAttackGuard][BOOT] vanilla target-disconnect guard file loaded feature=" .. FEATURE)

Guard.safeRequire(FEATURE, "BanditUtils")
if not Guard.isEnabled(FEATURE) then
    local state = Guard.status(FEATURE)
    print("[LCC][BanditsAttackGuard][BOOT_DISABLED] reason=" .. tostring(state and state.reason or "unknown"))
    return
end

local trackedAttackers = setmetatable({}, { __mode = "k" })
local protectedBandits = setmetatable({}, { __mode = "k" })
local applyVisualsHookInstalled = false
local pocAnnounced = false
local stats = {
    zombieUpdates = 0,
    protectedBandits = 0,
    targetLeaks = 0,
    targetDisconnects = 0,
    disconnectFailures = 0,
    pocTargetLeaks = 0,
    bAttackObserved = 0,
    attackStateObserved = 0,
}
local lastHeartbeat = 0

local function nowMs()
    if type(getTimestampMs) == "function" then
        return getTimestampMs()
    end
    local gameTime = getGameTime and getGameTime()
    if gameTime then
        return math.floor(gameTime:getWorldAgeHours() * 3600000)
    end
    return 0
end

local function boolString(value)
    return value and "true" or "false"
end

local function upstreamPocActive()
    return rawget(_G, "LCC_BANDITS_ATTACK_BRIDGE_POC") == POC_MARKER
end

local function announcePocIfNeeded()
    if pocAnnounced or not upstreamPocActive() then return end
    pocAnnounced = true
    print(string.format(
        "[LCC][BanditsAttackGuard][UPSTREAM_POC_ACTIVE] marker=%s mode=observe-only v3Disconnect=false targetProtection=false",
        POC_MARKER
    ))
end

local function characterId(character)
    if not character then return "nil" end

    if BanditUtils and type(BanditUtils.GetCharacterID) == "function" then
        local ok, value = pcall(BanditUtils.GetCharacterID, character)
        if ok and value ~= nil then return tostring(value) end
    end

    local ok, value = pcall(function()
        return character:getPersistentOutfitID()
    end)
    if ok and value ~= nil then return tostring(value) end

    return tostring(character)
end

local function isBandit(character)
    if not character or not instanceof(character, "IsoZombie") then return false end
    local ok, value = pcall(function()
        return character:getVariableBoolean("Bandit")
    end)
    return ok and value == true
end

local function attackStateName(zombie)
    local asn = zombie:getActionStateName()
    if not asn or tostring(asn) == "" then return "<none>" end
    return tostring(asn)
end

local function isAttackState(asn)
    return asn == "attack" or asn == "attack-network"
end

local function bumpType(zombie)
    local bump = zombie:getBumpType()
    if not bump or tostring(bump) == "" then return "<none>" end
    return tostring(bump)
end

local function isCustomBite(zombie, bump)
    local md = zombie:getModData()
    return md ~= nil
        and md.zid ~= nil
        and (bump == "Bite" or bump == "BiteLow")
end

local function protectBandit(bandit, force)
    if not isBandit(bandit) then return false end

    -- A source-level PoC must not be helped by compatibility-patch mutations.
    if upstreamPocActive() then
        return true
    end

    local first = protectedBandits[bandit] ~= true
    if first or force then
        -- Keep the target-side engine flag as a supplemental protection for the
        -- normal v3 fallback only. The v2 test proved it is insufficient alone.
        bandit:setZombiesDontAttack(true)
    end

    if first then
        protectedBandits[bandit] = true
        stats.protectedBandits = stats.protectedBandits + 1
        print(string.format(
            "[LCC][BanditsAttackGuard][PROTECT_TARGET] target=%s asserted=true",
            characterId(bandit)
        ))
    end

    return true
end

local function tryInstallApplyVisualsHook()
    if applyVisualsHookInstalled then return true end
    if type(Bandit) ~= "table" or type(Bandit.ApplyVisuals) ~= "function" then
        return false
    end

    local ok = Guard.wrapBefore(FEATURE, Bandit, "ApplyVisuals", function(bandit)
        protectBandit(bandit, true)
    end)
    if ok then
        applyVisualsHookInstalled = true
        print("[LCC][BanditsAttackGuard][EARLY_HOOK] Bandit.ApplyVisuals target protection hook installed")
    end
    return ok == true
end

local function maybeHeartbeat()
    local now = nowMs()
    if lastHeartbeat == 0 then
        lastHeartbeat = now
        return
    end
    if now - lastHeartbeat < HEARTBEAT_MS then return end
    lastHeartbeat = now

    announcePocIfNeeded()

    print(string.format(
        "[LCC][BanditsAttackGuard][SUMMARY] updates=%d protectedBandits=%d targetLeaks=%d targetDisconnects=%d disconnectFailures=%d pocTargetLeaks=%d bAttackObserved=%d attackStateObserved=%d earlyHook=%s upstreamPoc=%s",
        stats.zombieUpdates,
        stats.protectedBandits,
        stats.targetLeaks,
        stats.targetDisconnects,
        stats.disconnectFailures,
        stats.pocTargetLeaks,
        stats.bAttackObserved,
        stats.attackStateObserved,
        boolString(applyVisualsHookInstalled),
        boolString(upstreamPocActive())
    ))
end

local function observeAndDisconnectZombie(zombie)
    stats.zombieUpdates = stats.zombieUpdates + 1
    announcePocIfNeeded()

    if stats.zombieUpdates % HEARTBEAT_CHECK_EVERY_UPDATES == 0 then
        maybeHeartbeat()
    end

    if not zombie then return end

    if isBandit(zombie) then
        protectBandit(zombie, false)
        return
    end

    local target = zombie:getTarget()
    if not isBandit(target) then
        trackedAttackers[zombie] = nil
        return
    end

    protectBandit(target, true)

    local asn = attackStateName(zombie)
    local bump = bumpType(zombie)
    local customBite = isCustomBite(zombie, bump)
    local bAttack = zombie:getVariableBoolean("bAttack")
    local noLungeAttack = zombie:getVariableBoolean("NoLungeAttack")
    local attackState = isAttackState(asn)
    local previous = trackedAttackers[zombie]
    local newPair = not previous or previous.target ~= target
    local pocActive = upstreamPocActive()

    stats.targetLeaks = stats.targetLeaks + 1

    local postTarget = target
    local disconnected = false

    if pocActive then
        -- Do not assist the upstream PoC. Any Bandit vanilla target reaching this
        -- callback is evidence that the source-level bridge replacement leaked.
        stats.pocTargetLeaks = stats.pocTargetLeaks + 1
        if newPair then
            print(string.format(
                "[LCC][BanditsAttackGuard][POC_TARGET_LEAK] attacker=%s target=%s state=%s bAttack=%s noLunge=%s bump=%s customBite=%s intervention=false",
                characterId(zombie),
                characterId(target),
                asn,
                boolString(bAttack),
                boolString(noLungeAttack),
                bump,
                boolString(customBite)
            ))
        end
    else
        -- Normal v3 fallback. Bandits has already run its own UpdateZombies
        -- callback for this tick. Do not clear the whole aggro list: that could
        -- erase legitimate player aggro.
        zombie:setTarget(nil)
        postTarget = zombie:getTarget()
        disconnected = postTarget == nil

        if disconnected then
            stats.targetDisconnects = stats.targetDisconnects + 1
        else
            stats.disconnectFailures = stats.disconnectFailures + 1
        end

        if newPair or not disconnected then
            print(string.format(
                "[LCC][BanditsAttackGuard][DISCONNECT] attacker=%s target=%s stateBefore=%s bAttack=%s noLunge=%s bump=%s customBite=%s disconnected=%s postTarget=%s",
                characterId(zombie),
                characterId(target),
                asn,
                boolString(bAttack),
                boolString(noLungeAttack),
                bump,
                boolString(customBite),
                boolString(disconnected),
                characterId(postTarget)
            ))
        end
    end

    if bAttack and (
            not previous
            or not previous.bAttack
            or previous.target ~= target
        ) then
        stats.bAttackObserved = stats.bAttackObserved + 1
        print(string.format(
            "[LCC][BanditsAttackGuard][READ_ONLY_BATTACK] attacker=%s target=%s state=%s bAttack=true targetDisconnected=%s upstreamPoc=%s",
            characterId(zombie),
            characterId(target),
            asn,
            boolString(disconnected),
            boolString(pocActive)
        ))
    end

    if attackState and (
            not previous
            or not previous.attackState
            or previous.target ~= target
        ) then
        stats.attackStateObserved = stats.attackStateObserved + 1
        print(string.format(
            "[LCC][BanditsAttackGuard][ESCAPED_ATTACK_STATE] attacker=%s target=%s state=%s bAttack=%s noLunge=%s bump=%s customBite=%s targetDisconnected=%s upstreamPoc=%s diagnosticOnly=true",
            characterId(zombie),
            characterId(target),
            asn,
            boolString(bAttack),
            boolString(noLungeAttack),
            bump,
            boolString(customBite),
            boolString(disconnected),
            boolString(pocActive)
        ))
    end

    trackedAttackers[zombie] = {
        target = target,
        bAttack = bAttack,
        attackState = attackState,
    }
end

Guard.install {
    id = FEATURE,
    validate = function()
        if type(BanditUtils) ~= "table" or type(BanditUtils.GetCharacterID) ~= "function" then
            return false, "BanditUtils.GetCharacterID is unavailable"
        end
        if not Events or not Events.OnZombieUpdate then
            return false, "OnZombieUpdate event is unavailable"
        end
        return true
    end,
    install = function()
        tryInstallApplyVisualsHook()

        Events.OnZombieUpdate.Add(function(zombie)
            if Guard.isEnabled(FEATURE) then
                Guard.protect(FEATURE, "disconnect vanilla Bandit target", observeAndDisconnectZombie, zombie)
            end
        end)

        if Events.OnGameStart then
            Events.OnGameStart.Add(function()
                if Guard.isEnabled(FEATURE) and not applyVisualsHookInstalled then
                    Guard.protect(FEATURE, "install delayed ApplyVisuals hook", tryInstallApplyVisualsHook)
                end
            end)
        end

        print(string.format(
            "[LCC][BanditsAttackGuard][INIT] vanilla target-disconnect guard active feature=%s earlyHook=%s heartbeat=%dms; clearAggroList=false upstreamPocMarker=%s",
            FEATURE,
            boolString(applyVisualsHookInstalled),
            HEARTBEAT_MS,
            POC_MARKER
        ))
    end,
}
