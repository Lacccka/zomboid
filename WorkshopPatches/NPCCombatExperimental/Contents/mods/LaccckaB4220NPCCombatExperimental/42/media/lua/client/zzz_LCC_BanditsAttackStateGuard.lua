-- Client-side guard for the Bandits zombie -> NPC vanilla AttackState path.
--
-- B42.20.3 testing established two failed interventions:
--   1. bAttack cannot be cleared because it is a read-only animation callback;
--   2. setZombiesDontAttack(true) on the Bandit target does not stop Bandits'
--      UpdateZombies() from rebuilding a vanilla combat relationship.
--
-- Upstream UpdateZombies() explicitly calls spotted/addAggro/setTarget/setAttackedBy
-- for a normal zombie -> Bandit pair and independently performs its custom
-- Bite/BiteLow simulation. This v3 experiment runs later in OnZombieUpdate and
-- disconnects only the vanilla target with zombie:setTarget(nil) after upstream
-- has completed its custom logic for the tick. It deliberately does NOT clear
-- the complete aggro list, change Java action state, change bump type, write
-- bAttack, damage, infection, or Bandits' bite bookkeeping.
if isServer() then return end

local Guard = require "LCC/Guard"
local FEATURE = "bandits.attack-state-target-disconnect-v3"
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
local stats = {
    zombieUpdates = 0,
    protectedBandits = 0,
    targetLeaks = 0,
    targetDisconnects = 0,
    disconnectFailures = 0,
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

    local first = protectedBandits[bandit] ~= true
    if first or force then
        -- Keep the target-side engine flag as a supplemental protection. The v2
        -- test proved it is insufficient by itself, not that it is harmful.
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
        print("[LCC][BanditsAttackGuard][EARLY_HOOK] Bandit.ApplyVisuals target protection installed")
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

    print(string.format(
        "[LCC][BanditsAttackGuard][SUMMARY] updates=%d protectedBandits=%d targetLeaks=%d targetDisconnects=%d disconnectFailures=%d bAttackObserved=%d attackStateObserved=%d earlyHook=%s",
        stats.zombieUpdates,
        stats.protectedBandits,
        stats.targetLeaks,
        stats.targetDisconnects,
        stats.disconnectFailures,
        stats.bAttackObserved,
        stats.attackStateObserved,
        boolString(applyVisualsHookInstalled)
    ))
end

local function observeAndDisconnectZombie(zombie)
    stats.zombieUpdates = stats.zombieUpdates + 1
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

    stats.targetLeaks = stats.targetLeaks + 1

    -- This is the only active v3 intervention. Bandits has already run its own
    -- UpdateZombies callback for this tick, including custom Bite/BiteLow setup.
    -- Do not clear the whole aggro list: that could erase legitimate player aggro.
    zombie:setTarget(nil)

    local postTarget = zombie:getTarget()
    local disconnected = postTarget == nil
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

    if bAttack and (
            not previous
            or not previous.bAttack
            or previous.target ~= target
        ) then
        stats.bAttackObserved = stats.bAttackObserved + 1
        print(string.format(
            "[LCC][BanditsAttackGuard][READ_ONLY_BATTACK] attacker=%s target=%s state=%s bAttack=true targetDisconnected=%s",
            characterId(zombie),
            characterId(target),
            asn,
            boolString(disconnected)
        ))
    end

    if attackState and (
            not previous
            or not previous.attackState
            or previous.target ~= target
        ) then
        stats.attackStateObserved = stats.attackStateObserved + 1
        print(string.format(
            "[LCC][BanditsAttackGuard][ESCAPED_ATTACK_STATE] attacker=%s target=%s state=%s bAttack=%s noLunge=%s bump=%s customBite=%s targetDisconnected=%s diagnosticOnly=true",
            characterId(zombie),
            characterId(target),
            asn,
            boolString(bAttack),
            boolString(noLungeAttack),
            bump,
            boolString(customBite),
            boolString(disconnected)
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
            "[LCC][BanditsAttackGuard][INIT] vanilla target-disconnect guard active feature=%s earlyHook=%s heartbeat=%dms; clearAggroList=false",
            FEATURE,
            boolString(applyVisualsHookInstalled),
            HEARTBEAT_MS
        ))
    end,
}
