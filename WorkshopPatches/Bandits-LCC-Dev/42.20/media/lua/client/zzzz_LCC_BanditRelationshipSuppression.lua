-- LCC controlled PoC for B42.20 Bandits -> vanilla zombie combat relationships.
--
-- The direct gunshot alert in ZAShoot.lua is source-edited to use
-- pathToLocationF() instead of spottedNew/addAggro/setTarget. This companion
-- handles two remaining retaliation seams that are buried inside upstream
-- functions: melee Smack and BanditUtils.Hit.
if isServer() then return end

local MARKER = "character-relation-suppression-v4"
LCC_BANDITS_ATTACK_RELATION_POC = MARKER

local stats = rawget(_G, "LCC_BanditsRelationshipStats") or {
    shotCoordinateAlerts = 0,
    meleeChecks = 0,
    meleeDamageEvents = 0,
    gunChecks = 0,
    gunDamageEvents = 0,
    targetClears = 0,
    attackedByClears = 0,
    coordinateResponses = 0,
    sanitizeErrors = 0,
}
_G.LCC_BanditsRelationshipStats = stats

local seenPairs = {}
local warned = {}

local function warnOnce(key, message)
    if warned[key] then return end
    warned[key] = true
    print(message)
end

local function isBandit(character)
    if not character or not instanceof(character, "IsoZombie") then return false end
    local ok, value = pcall(function() return character:getVariableBoolean("Bandit") end)
    return ok and value == true
end

local function isOrdinaryZombie(character)
    return character ~= nil and instanceof(character, "IsoZombie") and not isBandit(character)
end

local function characterId(character)
    if not character then return "nil" end
    if BanditUtils and type(BanditUtils.GetCharacterID) == "function" then
        local ok, value = pcall(BanditUtils.GetCharacterID, character)
        if ok and value ~= nil then return tostring(value) end
    end
    local ok, value = pcall(function() return character:getPersistentOutfitID() end)
    if ok and value ~= nil then return tostring(value) end
    return tostring(character)
end

local function healthOf(character)
    if not character then return nil end
    local ok, value = pcall(function() return character:getHealth() end)
    if ok then return value end
    return nil
end

local function currentTarget(character)
    if not character then return nil end
    local ok, value = pcall(function() return character:getTarget() end)
    if ok then return value end
    return nil
end

local function logPairOnce(source, victim, attacker, targetCleared, attackedByCleared, pathIssued)
    local key = table.concat({source, characterId(victim), characterId(attacker)}, ":")
    if seenPairs[key] then return end
    seenPairs[key] = true
    print(string.format(
        "[LCC][BanditsRelationPoC][SANITIZE] marker=%s source=%s victim=%s attacker=%s targetCleared=%s attackedByCleared=%s coordinateResponse=%s",
        MARKER,
        source,
        characterId(victim),
        characterId(attacker),
        tostring(targetCleared),
        tostring(attackedByCleared),
        tostring(pathIssued)
    ))
end

local function sanitizeBanditRelationship(victim, attacker, source, clearAttackedBy)
    if not isOrdinaryZombie(victim) or not isBandit(attacker) then return false end
    if not victim:isAlive() then return false end

    local targetCleared = false
    local attackedByCleared = false
    local pathIssued = false

    local target = currentTarget(victim)
    if isBandit(target) then
        local ok = pcall(function() victim:setTarget(nil) end)
        if ok then
            targetCleared = true
            stats.targetClears = stats.targetClears + 1
        else
            stats.sanitizeErrors = stats.sanitizeErrors + 1
            warnOnce("target-clear", "[LCC][BanditsRelationPoC][ERROR] setTarget(nil) failed")
        end
    end

    if clearAttackedBy then
        local ok = pcall(function() victim:setAttackedBy(nil) end)
        if ok then
            attackedByCleared = true
            stats.attackedByClears = stats.attackedByClears + 1
        else
            stats.sanitizeErrors = stats.sanitizeErrors + 1
            warnOnce("attackedby-clear", "[LCC][BanditsRelationPoC][ERROR] setAttackedBy(nil) failed")
        end
    end

    -- Keep the intended retaliation/attention behavior without a character
    -- target. This mirrors the coordinate-only zombie pursuit used by the v2
    -- BanditUpdate PoC and the source-edited gunshot alert path.
    local okPath = pcall(function()
        victim:pathToLocationF(attacker:getX(), attacker:getY(), attacker:getZ())
    end)
    if okPath then
        pathIssued = true
        stats.coordinateResponses = stats.coordinateResponses + 1
    else
        stats.sanitizeErrors = stats.sanitizeErrors + 1
        warnOnce("path", "[LCC][BanditsRelationPoC][ERROR] pathToLocationF retaliation failed")
    end

    logPairOnce(source, victim, attacker, targetCleared, attackedByCleared, pathIssued)
    return targetCleared or attackedByCleared or pathIssued
end

local smackWrapped = false
if type(ZombieActions) == "table"
        and type(ZombieActions.Smack) == "table"
        and type(ZombieActions.Smack.onWorking) == "function" then
    local originalSmackOnWorking = ZombieActions.Smack.onWorking
    ZombieActions.Smack.onWorking = function(bandit, task)
        local victim = task and BanditZombie and BanditZombie.Cache and BanditZombie.Cache[task.eid] or nil
        local watch = isBandit(bandit) and isOrdinaryZombie(victim)
        local beforeHealth = watch and healthOf(victim) or nil
        if watch then stats.meleeChecks = stats.meleeChecks + 1 end

        local result = originalSmackOnWorking(bandit, task)

        if watch and victim and victim:isAlive() then
            local afterHealth = healthOf(victim)
            local damaged = beforeHealth ~= nil and afterHealth ~= nil and afterHealth < beforeHealth
            local hasBanditTarget = isBandit(currentTarget(victim))
            if damaged then stats.meleeDamageEvents = stats.meleeDamageEvents + 1 end
            if damaged or hasBanditTarget then
                sanitizeBanditRelationship(victim, bandit, "melee", damaged)
            end
        end

        return result
    end
    smackWrapped = true
else
    warnOnce("smack-missing", "[LCC][BanditsRelationPoC][WARN] ZombieActions.Smack.onWorking unavailable")
end

local gunWrapped = false
if type(BanditUtils) == "table" and type(BanditUtils.Hit) == "function" then
    local originalBanditUtilsHit = BanditUtils.Hit
    BanditUtils.Hit = function(shooter, item, victim, ...)
        local watch = isBandit(shooter) and isOrdinaryZombie(victim)
        local beforeHealth = watch and healthOf(victim) or nil
        if watch then stats.gunChecks = stats.gunChecks + 1 end

        local result = originalBanditUtilsHit(shooter, item, victim, ...)

        if watch and victim and victim:isAlive() then
            local afterHealth = healthOf(victim)
            local damaged = beforeHealth ~= nil and afterHealth ~= nil and afterHealth < beforeHealth
            local hasBanditTarget = isBandit(currentTarget(victim))
            if damaged then stats.gunDamageEvents = stats.gunDamageEvents + 1 end
            if damaged or hasBanditTarget then
                sanitizeBanditRelationship(victim, shooter, "gun-hit", damaged)
            end
        end

        return result
    end
    gunWrapped = true
else
    warnOnce("gun-missing", "[LCC][BanditsRelationPoC][WARN] BanditUtils.Hit unavailable")
end

local function summary()
    print(string.format(
        "[LCC][BanditsRelationPoC][SUMMARY] marker=%s shotCoordinateAlerts=%d meleeChecks=%d meleeDamageEvents=%d gunChecks=%d gunDamageEvents=%d targetClears=%d attackedByClears=%d coordinateResponses=%d sanitizeErrors=%d",
        MARKER,
        stats.shotCoordinateAlerts or 0,
        stats.meleeChecks or 0,
        stats.meleeDamageEvents or 0,
        stats.gunChecks or 0,
        stats.gunDamageEvents or 0,
        stats.targetClears or 0,
        stats.attackedByClears or 0,
        stats.coordinateResponses or 0,
        stats.sanitizeErrors or 0
    ))
end

Events.EveryOneMinute.Add(summary)

print(string.format(
    "[LCC][BanditsRelationPoC][BOOT] marker=%s gunshotAlert=coordinate-only meleeWrapped=%s gunHitWrapped=%s",
    MARKER,
    tostring(smackWrapped),
    tostring(gunWrapped)
))
