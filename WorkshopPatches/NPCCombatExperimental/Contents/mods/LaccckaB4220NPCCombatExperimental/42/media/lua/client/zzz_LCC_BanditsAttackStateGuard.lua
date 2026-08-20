-- Client-side guard for the Bandits zombie -> NPC vanilla AttackState path.
--
-- B42.20.3 testing established:
--   * bAttack is read-only;
--   * target-side setZombiesDontAttack(true) is insufficient;
--   * removing spotted/addAggro/setTarget/setAttackedBy is not enough;
--   * coordinate-only pathToLocationF still correlates with fresh Bandit targets.
--
-- Two controlled modes are supported:
--   1. upstream-coordinate-pursuit-v2: fully observation-only.
--   2. coordinate-target-trace-v3: no target-side protection, but a late
--      zombie:setTarget(nil) safety disconnect after Bandits' own post-trace
--      observer has measured the resulting target state.
if isServer() then return end

local Guard = require "LCC/Guard"
local FEATURE = "bandits.attack-state-target-disconnect-v3"
local POC_MARKER = "upstream-coordinate-pursuit-v2"
local TRACE_MARKER = "coordinate-target-trace-v3"
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
local traceAnnounced = false
local stats = {
    zombieUpdates = 0,
    protectedBandits = 0,
    targetLeaks = 0,
    targetDisconnects = 0,
    traceSafetyDisconnects = 0,
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

local function traceSafetyActive()
    return rawget(_G, "LCC_BANDITS_ATTACK_TRACE") == TRACE_MARKER
end

local function upstreamPocActive()
    return not traceSafetyActive()
        and rawget(_G, "LCC_BANDITS_ATTACK_BRIDGE_POC") == POC_MARKER
end

local function announceModeIfNeeded()
    if traceSafetyActive() then
        if traceAnnounced then return end
        traceAnnounced = true
        print(string.format(
            "[LCC][BanditsAttackGuard][TRACE_SAFETY_ACTIVE] marker=%s mode=late-disconnect targetProtection=false",
            TRACE_MARKER
        ))
        return
    end

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

    -- Neither controlled experiment may be helped by target-side mutation.
    if upstreamPocActive() or traceSafetyActive() then
        return true
    end

    local first = protectedBandits[bandit] ~= true
    if first or force then
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
        print("[LCC][BanditsAttackGuard][EARLY_HOOK] Bandit.ApplyVisuals target hook installed")
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

    announceModeIfNeeded()

    print(string.format(
        "[LCC][BanditsAttackGuard][SUMMARY] updates=%d protectedBandits=%d targetLeaks=%d targetDisconnects=%d traceSafetyDisconnects=%d disconnectFailures=%d pocTargetLeaks=%d bAttackObserved=%d attackStateObserved=%d earlyHook=%s upstreamPoc=%s traceSafety=%s",
        stats.zombieUpdates,
        stats.protectedBandits,
        stats.targetLeaks,
        stats.targetDisconnects,
        stats.traceSafetyDisconnects,
        stats.disconnectFailures,
        stats.pocTargetLeaks,
        stats.bAttackObserved,
        stats.attackStateObserved,
        boolString(applyVisualsHookInstalled),
        boolString(upstreamPocActive()),
        boolString(traceSafetyActive())
    ))
end

local function disconnectTarget(zombie)
    zombie:setTarget(nil)
    return zombie:getTarget() == nil
end

local function observeAndDisconnectZombie(zombie)
    stats.zombieUpdates = stats.zombieUpdates + 1
    announceModeIfNeeded()

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
    local traceSafety = traceSafetyActive()

    stats.targetLeaks = stats.targetLeaks + 1

    local postTarget = target
    local disconnected = false

    if pocActive then
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
        -- The normal fallback and trace-safety mode both disconnect late. In
        -- trace mode the Bandits pre/post observers have already measured this
        -- tick, while target-side protection remains disabled.
        disconnected = disconnectTarget(zombie)
        postTarget = zombie:getTarget()

        if disconnected then
            stats.targetDisconnects = stats.targetDisconnects + 1
            if traceSafety then
                stats.traceSafetyDisconnects = stats.traceSafetyDisconnects + 1
            end
        else
            stats.disconnectFailures = stats.disconnectFailures + 1
        end

        if newPair or not disconnected then
            local tag = traceSafety and "TRACE_SAFETY_DISCONNECT" or "DISCONNECT"
            print(string.format(
                "[LCC][BanditsAttackGuard][%s] attacker=%s target=%s stateBefore=%s bAttack=%s noLunge=%s bump=%s customBite=%s disconnected=%s postTarget=%s",
                tag,
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
            "[LCC][BanditsAttackGuard][READ_ONLY_BATTACK] attacker=%s target=%s state=%s bAttack=true targetDisconnected=%s upstreamPoc=%s traceSafety=%s",
            characterId(zombie),
            characterId(target),
            asn,
            boolString(disconnected),
            boolString(pocActive),
            boolString(traceSafety)
        ))
    end

    if attackState and (
            not previous
            or not previous.attackState
            or previous.target ~= target
        ) then
        stats.attackStateObserved = stats.attackStateObserved + 1
        print(string.format(
            "[LCC][BanditsAttackGuard][ESCAPED_ATTACK_STATE] attacker=%s target=%s state=%s bAttack=%s noLunge=%s bump=%s customBite=%s targetDisconnected=%s upstreamPoc=%s traceSafety=%s diagnosticOnly=true",
            characterId(zombie),
            characterId(target),
            asn,
            boolString(bAttack),
            boolString(noLungeAttack),
            bump,
            boolString(customBite),
            boolString(disconnected),
            boolString(pocActive),
            boolString(traceSafety)
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
            "[LCC][BanditsAttackGuard][INIT] feature=%s earlyHook=%s heartbeat=%dms clearAggroList=false upstreamPocMarker=%s traceMarker=%s",
            FEATURE,
            boolString(applyVisualsHookInstalled),
            HEARTBEAT_MS,
            POC_MARKER,
            TRACE_MARKER
        ))
    end,
}
