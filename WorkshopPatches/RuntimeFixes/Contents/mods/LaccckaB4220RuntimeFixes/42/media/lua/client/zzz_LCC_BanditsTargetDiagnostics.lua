-- Observe-only diagnostics for the Bandits zombie -> Bandit targeting path.
--
-- This file intentionally DOES NOT change zombie targets, action states, variables,
-- bump types, aggro, or Bandits tasks. Its only job is to leave a compact trace in
-- the client log so the dangerous path can be correlated with vanilla
-- NetworkZombieMind / AttackState errors.
if isServer() then return end

local Guard = require "LCC/Guard"
local FEATURE = "bandits.target-diagnostics"
local HEARTBEAT_MS = 15000

Guard.safeRequire(FEATURE, "BanditUtils")
if not Guard.isEnabled(FEATURE) then return end

local tracked = {}
local stats = {
    zombieUpdates = 0,
    targetAcquired = 0,
    targetSwitched = 0,
    targetLost = 0,
    customBiteStarted = 0,
    customBiteFinished = 0,
    bAttackTrue = 0,
    attackStateEntered = 0,
}
local lastHeartbeat = 0

local function nowMs()
    if type(getTimestampMs) == "function" then
        local ok, value = pcall(getTimestampMs)
        if ok and value then return value end
    end

    local gameTime = getGameTime and getGameTime()
    if gameTime and gameTime.getWorldAgeHours then
        local ok, hours = pcall(function() return gameTime:getWorldAgeHours() end)
        if ok and hours then return math.floor(hours * 3600000) end
    end

    return 0
end

local function boolString(value)
    return value and "true" or "false"
end

local function safeVariableBoolean(character, name)
    if not character or not character.getVariableBoolean then return false end
    local ok, value = pcall(function() return character:getVariableBoolean(name) end)
    return ok and value == true
end

local function safeActionState(character)
    if not character or not character.getActionStateName then return "<none>" end
    local ok, value = pcall(function() return character:getActionStateName() end)
    if ok and value then return tostring(value) end
    return "<none>"
end

local function safeBumpType(character)
    if not character or not character.getBumpType then return "<none>" end
    local ok, value = pcall(function() return character:getBumpType() end)
    if ok and value and tostring(value) ~= "" then return tostring(value) end
    return "<none>"
end

local function safeModDataZid(character)
    if not character or not character.getModData then return nil end
    local ok, value = pcall(function()
        local md = character:getModData()
        return md and md.zid or nil
    end)
    if ok then return value end
    return nil
end

local function characterId(character)
    if not character then return "nil" end

    if BanditUtils and type(BanditUtils.GetCharacterID) == "function" then
        local ok, value = pcall(BanditUtils.GetCharacterID, character)
        if ok and value ~= nil then return tostring(value) end
    end

    if character.getPersistentOutfitID then
        local ok, value = pcall(function() return character:getPersistentOutfitID() end)
        if ok and value ~= nil then return tostring(value) end
    end

    return tostring(character)
end

local function characterClass(character)
    if not character then return "nil" end
    if type(instanceof) == "function" then
        local okPlayer, isPlayer = pcall(instanceof, character, "IsoPlayer")
        if okPlayer and isPlayer then return "IsoPlayer" end
        local okZombie, isZombie = pcall(instanceof, character, "IsoZombie")
        if okZombie and isZombie then return "IsoZombie" end
    end
    return "unknown"
end

local function distance2d(a, b)
    if not a or not b then return -1 end
    local ok, value = pcall(function()
        local dx = a:getX() - b:getX()
        local dy = a:getY() - b:getY()
        return math.sqrt(dx * dx + dy * dy)
    end)
    if ok and value then return value end
    return -1
end

local function snapshot(zombie, target)
    local asn = safeActionState(zombie)
    local bump = safeBumpType(zombie)
    local modDataZid = safeModDataZid(zombie)

    return {
        target = target,
        targetId = characterId(target),
        targetClass = characterClass(target),
        asn = asn,
        bAttack = safeVariableBoolean(zombie, "bAttack"),
        noLungeAttack = safeVariableBoolean(zombie, "NoLungeAttack"),
        bump = bump,
        modDataZid = modDataZid,
        customBite = modDataZid ~= nil and (bump == "Bite" or bump == "BiteLow"),
        attackState = asn == "attack" or asn == "attack-network",
        dist = distance2d(zombie, target),
    }
end

local function logSnapshot(eventName, zombie, snap)
    print(string.format(
        "[LCC][BanditsDiag][%s] attacker=%s target=%s targetClass=%s dist=%.3f state=%s bAttack=%s noLunge=%s bump=%s customBite=%s mdZid=%s",
        eventName,
        characterId(zombie),
        tostring(snap.targetId),
        tostring(snap.targetClass),
        snap.dist or -1,
        tostring(snap.asn),
        boolString(snap.bAttack),
        boolString(snap.noLungeAttack),
        tostring(snap.bump),
        boolString(snap.customBite),
        tostring(snap.modDataZid)
    ))
end

local function activeTrackedCount()
    local count = 0
    for zombie, state in pairs(tracked) do
        local keep = false
        if zombie and state then
            local ok, alive = pcall(function() return zombie:isAlive() end)
            if ok and alive then
                local okTarget, target = pcall(function() return zombie:getTarget() end)
                keep = okTarget and target and safeVariableBoolean(target, "Bandit")
            end
        end

        if keep then
            count = count + 1
        else
            tracked[zombie] = nil
        end
    end
    return count
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
        "[LCC][BanditsDiag][SUMMARY] updates=%d activeBanditTargets=%d acquired=%d switched=%d lost=%d customBiteStart=%d customBiteEnd=%d bAttackTrue=%d attackState=%d",
        stats.zombieUpdates,
        activeTrackedCount(),
        stats.targetAcquired,
        stats.targetSwitched,
        stats.targetLost,
        stats.customBiteStarted,
        stats.customBiteFinished,
        stats.bAttackTrue,
        stats.attackStateEntered
    ))
end

local function observeZombie(zombie)
    stats.zombieUpdates = stats.zombieUpdates + 1
    maybeHeartbeat()

    if not zombie or safeVariableBoolean(zombie, "Bandit") then return end
    if not zombie.getTarget then return end

    local ok, target = pcall(function() return zombie:getTarget() end)
    if not ok then return end

    local targetIsBandit = target and safeVariableBoolean(target, "Bandit")
    local previous = tracked[zombie]

    if not targetIsBandit then
        if previous then
            stats.targetLost = stats.targetLost + 1
            print(string.format(
                "[LCC][BanditsDiag][TARGET_LOST] attacker=%s previousTarget=%s lastState=%s lastBump=%s lastCustomBite=%s",
                characterId(zombie),
                tostring(previous.targetId),
                tostring(previous.asn),
                tostring(previous.bump),
                boolString(previous.customBite)
            ))
            tracked[zombie] = nil
        end
        return
    end

    local current = snapshot(zombie, target)

    if not previous then
        stats.targetAcquired = stats.targetAcquired + 1
        logSnapshot("TARGET_ACQUIRED", zombie, current)
    elseif previous.target ~= target then
        stats.targetSwitched = stats.targetSwitched + 1
        print(string.format(
            "[LCC][BanditsDiag][TARGET_SWITCH] attacker=%s from=%s to=%s",
            characterId(zombie), tostring(previous.targetId), tostring(current.targetId)
        ))
        logSnapshot("TARGET_SWITCH_STATE", zombie, current)
    end

    if current.customBite and (not previous or not previous.customBite) then
        stats.customBiteStarted = stats.customBiteStarted + 1
        logSnapshot("CUSTOM_BITE_START", zombie, current)
    elseif previous and previous.customBite and not current.customBite then
        stats.customBiteFinished = stats.customBiteFinished + 1
        logSnapshot("CUSTOM_BITE_END", zombie, current)
    end

    if current.bAttack and (not previous or not previous.bAttack) then
        stats.bAttackTrue = stats.bAttackTrue + 1
        logSnapshot("BATTACK_TRUE", zombie, current)
    end

    if current.attackState and (not previous or not previous.attackState) then
        stats.attackStateEntered = stats.attackStateEntered + 1
        logSnapshot("DANGER_ATTACK_STATE", zombie, current)
    end

    tracked[zombie] = current
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
                Guard.protect(FEATURE, "observe zombie target", observeZombie, zombie)
            end
        end)

        print(string.format(
            "[LCC][BanditsDiag][INIT] observe-only diagnostics active; heartbeat=%dms; no gameplay state is modified",
            HEARTBEAT_MS
        ))
    end,
}
