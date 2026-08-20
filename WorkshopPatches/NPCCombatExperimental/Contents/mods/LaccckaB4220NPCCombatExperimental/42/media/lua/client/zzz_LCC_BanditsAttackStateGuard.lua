-- Narrow client-side guard for the Bandits zombie -> Bandit attack path.
--
-- Bandits intentionally represents NPCs as IsoZombie and assigns them as real
-- targets of normal zombies. Upstream also implements its own Bite/BiteLow
-- damage path, but the bAttack=false line intended to keep vanilla AttackState
-- out of that interaction is commented out in BanditUpdate.lua.
--
-- LCC does not replace BanditUpdate.lua and does not change targets, aggro,
-- pathfinding, bump types, custom bite bookkeeping, damage, or infection. It
-- only clears bAttack while a normal IsoZombie currently targets a Bandit.
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
    blocked = 0,
    escapedAttackState = 0,
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
        "[LCC][BanditsAttackGuard][SUMMARY] updates=%d banditTargetUpdates=%d blocked=%d escapedAttackState=%d",
        stats.zombieUpdates,
        stats.banditTargetUpdates,
        stats.blocked,
        stats.escapedAttackState
    ))
end

local function guardZombie(zombie)
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
    local bAttackBefore = zombie:getVariableBoolean("bAttack")
    local noLungeAttack = zombie:getVariableBoolean("NoLungeAttack")

    if bAttackBefore then
        zombie:setVariable("bAttack", false)
        stats.blocked = stats.blocked + 1

        print(string.format(
            "[LCC][BanditsAttackGuard][BLOCK] attacker=%s target=%s stateBefore=%s bAttackBefore=true bAttackAfter=false noLunge=%s bump=%s customBite=%s",
            characterId(zombie),
            characterId(target),
            asn,
            boolString(noLungeAttack),
            bump,
            boolString(customBite)
        ))
    end

    local attackState = isAttackState(asn)
    local previous = tracked[zombie]
    if attackState and (
            not previous
            or not previous.attackState
            or previous.target ~= target
        ) then
        stats.escapedAttackState = stats.escapedAttackState + 1

        print(string.format(
            "[LCC][BanditsAttackGuard][ESCAPED_ATTACK_STATE] attacker=%s target=%s state=%s bAttackBefore=%s bAttackAfter=%s noLunge=%s bump=%s customBite=%s",
            characterId(zombie),
            characterId(target),
            asn,
            boolString(bAttackBefore),
            boolString(zombie:getVariableBoolean("bAttack")),
            boolString(noLungeAttack),
            bump,
            boolString(customBite)
        ))
    end

    tracked[zombie] = {
        target = target,
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
                Guard.protect(FEATURE, "guard zombie attack state", guardZombie, zombie)
            end
        end)

        print(string.format(
            "[LCC][BanditsAttackGuard][INIT] bAttack guard active; heartbeat=%dms; targets/aggro/pathing/custom Bite are unchanged",
            HEARTBEAT_MS
        ))
    end,
}
