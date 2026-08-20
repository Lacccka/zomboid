-- Client-side guard for the Bandits zombie -> NPC vanilla AttackState path.
--
-- B42.20.3 exposes bAttack as a read-only animation callback variable, so this
-- experiment works at the target-side engine seam instead. Bandits already uses
-- setZombiesDontAttack(true) during its close-range custom Bite/BiteLow path; LCC
-- asserts the same engine flag earlier for Bandit IsoZombie targets.
--
-- This file deliberately uses a v2 Guard feature id. Older local test copies may
-- still contain the former experimental guard; sharing its feature id could make
-- Guard.install() treat this implementation as already installed and silently
-- skip it. The log namespace remains BanditsAttackGuard for easy comparison.
if isServer() then return end

local Guard = require "LCC/Guard"
local FEATURE = "bandits.attack-state-target-guard-v2"
local HEARTBEAT_MS = 15000
local HEARTBEAT_CHECK_EVERY_UPDATES = 512

print("[LCC][BanditsAttackGuard][BOOT] target-side guard file loaded feature=" .. FEATURE)

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
        -- Do not probe isZombiesDontAttack(). Java/Kahlua binding failures can be
        -- logged even when wrapped in Lua pcall. The setter is the upstream-used
        -- API and is sufficient for this experiment.
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
        "[LCC][BanditsAttackGuard][SUMMARY] updates=%d protectedBandits=%d targetLeaks=%d bAttackObserved=%d attackStateObserved=%d earlyHook=%s",
        stats.zombieUpdates,
        stats.protectedBandits,
        stats.targetLeaks,
        stats.bAttackObserved,
        stats.attackStateObserved,
        boolString(applyVisualsHookInstalled)
    ))
end

local function observeZombie(zombie)
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

    -- Reassert on every observed vanilla target leak. This is intentionally
    -- narrow: target/aggro/pathing/state/bump/custom-bite data are not rewritten.
    protectBandit(target, true)

    local asn = attackStateName(zombie)
    local bump = bumpType(zombie)
    local customBite = isCustomBite(zombie, bump)
    local bAttack = zombie:getVariableBoolean("bAttack")
    local noLungeAttack = zombie:getVariableBoolean("NoLungeAttack")
    local attackState = isAttackState(asn)
    local previous = trackedAttackers[zombie]

    if not previous or previous.target ~= target then
        stats.targetLeaks = stats.targetLeaks + 1
        print(string.format(
            "[LCC][BanditsAttackGuard][TARGET_LEAK] attacker=%s target=%s state=%s bAttack=%s noLunge=%s bump=%s customBite=%s targetProtected=true",
            characterId(zombie),
            characterId(target),
            asn,
            boolString(bAttack),
            boolString(noLungeAttack),
            bump,
            boolString(customBite)
        ))
    end

    if bAttack and (
            not previous
            or not previous.bAttack
            or previous.target ~= target
        ) then
        stats.bAttackObserved = stats.bAttackObserved + 1
        print(string.format(
            "[LCC][BanditsAttackGuard][READ_ONLY_BATTACK] attacker=%s target=%s state=%s bAttack=true noLunge=%s bump=%s customBite=%s targetProtected=true",
            characterId(zombie),
            characterId(target),
            asn,
            boolString(noLungeAttack),
            bump,
            boolString(customBite)
        ))
    end

    if attackState and (
            not previous
            or not previous.attackState
            or previous.target ~= target
        ) then
        stats.attackStateObserved = stats.attackStateObserved + 1
        print(string.format(
            "[LCC][BanditsAttackGuard][ESCAPED_ATTACK_STATE] attacker=%s target=%s state=%s bAttack=%s noLunge=%s bump=%s customBite=%s targetProtected=true diagnosticOnly=true",
            characterId(zombie),
            characterId(target),
            asn,
            boolString(bAttack),
            boolString(noLungeAttack),
            bump,
            boolString(customBite)
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
                Guard.protect(FEATURE, "protect/observe zombie attack state", observeZombie, zombie)
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
            "[LCC][BanditsAttackGuard][INIT] target-side guard active feature=%s earlyHook=%s heartbeat=%dms",
            FEATURE,
            boolString(applyVisualsHookInstalled),
            HEARTBEAT_MS
        ))
    end,
}
