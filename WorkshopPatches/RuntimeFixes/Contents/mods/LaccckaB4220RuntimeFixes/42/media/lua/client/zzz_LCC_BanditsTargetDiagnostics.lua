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
local HEARTBEAT_CHECK_EVERY_UPDATES = 512

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
        local ok, value = pcall(function() return character:getPersistentOutfitID() end)
        if ok and value ~= nil then return tostring(value) end
    end

    return tostring(character)
end

local function characterClass(character)
    if not character then return "nil" end
    if instanceof(character, "IsoPlayer") then return "IsoPlayer" end
    if instanceof(character, "IsoZombie") then return "IsoZombie" end
    return "unknown"
end

local function isBandit(character)
    return character
        and instanceof(character, "IsoZombie")
        and character:getVariableBoolean("Bandit")
end

local function snapshot(zombie, target)
    local asn = zombie:getActionStateName() or "<none>"
    local bump = zombie:getBumpType()
    if not bump or tostring(bump) == "" then bump = "<none>" end

    local md = zombie:getModData()
    local modDataZid = md and md.zid or nil
    local bAttack = zombie:getVariableBoolean("bAttack")
    local noLungeAttack = zombie:getVariableBoolean("NoLungeAttack")
    local dx = zombie:getX() - target:getX()
    local dy = zombie:getY() - target:getY()

    return {
        target = target,
        targetId = characterId(target),
        targetClass = characterClass(target),
        asn = tostring(asn),
        bAttack = bAttack,
        noLungeAttack = noLungeAttack,
        bump = tostring(bump),
        modDataZid = modDataZid,
        customBite = modDataZid ~= nil and (bump == "Bite" or bump == "BiteLow"),
        attackState = asn == "attack" or asn == "attack-network",
        dist = math.sqrt(dx * dx + dy * dy),
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
            local ok, result = pcall(function()
                return zombie:isAlive() and isBandit(zombie:getTarget())
            end)
            keep = ok and result == true
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
    if stats.zombieUpdates % HEARTBEAT_CHECK_EVERY_UPDATES == 0 then
        maybeHeartbeat()
    end

    if not zombie or zombie:getVariableBoolean("Bandit") then return end

    local target = zombie:getTarget()
    local targetIsBandit = isBandit(target)
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
