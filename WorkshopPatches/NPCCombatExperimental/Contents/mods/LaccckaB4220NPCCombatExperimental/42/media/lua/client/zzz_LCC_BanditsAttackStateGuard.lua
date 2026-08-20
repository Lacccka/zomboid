-- Client-side observation of the Bandits zombie -> NPC AttackState path.
--
-- B42.20.3 testing proved that bAttack is backed by a read-only animation
-- callback variable. Calling zombie:setVariable("bAttack", false) produces
-- AnimationVariableSlotCallback.trySetValue warnings and does not change the
-- value. The former experimental intervention therefore did not block vanilla
-- AttackState and its BLOCK counter was misleading.
--
-- Keep this probe strictly observe-only until a mutable pre-AttackState seam is
-- identified. It does not change targets, aggro, pathfinding, bump types,
-- custom Bite bookkeeping, damage, infection, or Java action states.
if isServer() then return end

local Guard = require "LCC/Guard"
local FEATURE = "bandits.attack-state-guard"
local HEARTBEAT_MS = 15000
local HEARTBEAT_CHECK_EVERY_UPDATES = 512

Guard.safeRequire(FEATURE, "BanditUtils")
if not Guard.isEnabled(FEATURE) then return end

local tracked = setmetatable({}, { __mode = "k" })
local stats = {
    zombieUpdates = 0,
    banditTargetUpdates = 0,
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

    if character.getPersistentOutfitID then
        local ok, value = pcall(function()
            return character:getPersistentOutfitID()
        end)
        if ok and value ~= nil then return tostring(value) end
    end

    return tostring(character)
end

local function isBandit(character)
    return character
        and instanceof(character, "IsoZombie")
        and character:getVariableBoolean("Bandit")
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

local function maybeHeartbeat()
    local now = nowMs()
    if lastHeartbeat == 0 then
        lastHeartbeat = now
        return
    end
    if now - lastHeartbeat < HEARTBEAT_MS then return end
    lastHeartbeat = now

    print(string.format(
        "[LCC][BanditsAttackGuard][SUMMARY] updates=%d banditTargetUpdates=%d bAttackObserved=%d attackStateObserved=%d",
        stats.zombieUpdates,
        stats.banditTargetUpdates,
        stats.bAttackObserved,
        stats.attackStateObserved
    ))
end

local function observeZombie(zombie)
    stats.zombieUpdates = stats.zombieUpdates + 1
    if stats.zombieUpdates % HEARTBEAT_CHECK_EVERY_UPDATES == 0 then
        maybeHeartbeat()
    end

    if not zombie or zombie:getVariableBoolean("Bandit") then return end

    local target = zombie:getTarget()
    if not isBandit(target) then
        tracked[zombie] = nil
        return
    end

    stats.banditTargetUpdates = stats.banditTargetUpdates + 1

    local asn = attackStateName(zombie)
    local bump = bumpType(zombie)
    local customBite = isCustomBite(zombie, bump)
    local bAttack = zombie:getVariableBoolean("bAttack")
    local noLungeAttack = zombie:getVariableBoolean("NoLungeAttack")
    local attackState = isAttackState(asn)
    local previous = tracked[zombie]

    if bAttack and (
            not previous
            or not previous.bAttack
            or previous.target ~= target
        ) then
        stats.bAttackObserved = stats.bAttackObserved + 1
        print(string.format(
            "[LCC][BanditsAttackGuard][READ_ONLY_BATTACK] attacker=%s target=%s state=%s bAttack=true noLunge=%s bump=%s customBite=%s",
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
            "[LCC][BanditsAttackGuard][ESCAPED_ATTACK_STATE] attacker=%s target=%s state=%s bAttack=%s noLunge=%s bump=%s customBite=%s diagnosticOnly=true",
            characterId(zombie),
            characterId(target),
            asn,
            boolString(bAttack),
            boolString(noLungeAttack),
            bump,
            boolString(customBite)
        ))
    end

    tracked[zombie] = {
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
        Events.OnZombieUpdate.Add(function(zombie)
            if Guard.isEnabled(FEATURE) then
                Guard.protect(FEATURE, "observe zombie attack state", observeZombie, zombie)
            end
        end)

        print(string.format(
            "[LCC][BanditsAttackGuard][INIT] diagnostic-only on B42.20.3; bAttack is read-only; no mutation attempted; heartbeat=%dms",
            HEARTBEAT_MS
        ))
    end,
}
