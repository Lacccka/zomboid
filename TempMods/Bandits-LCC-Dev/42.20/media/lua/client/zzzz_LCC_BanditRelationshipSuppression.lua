-- LCC controlled suppression of unsafe Bandit -> ordinary-zombie character
-- relationships for B42.20.3.
--
-- ZAShoot.lua is source-edited to alert ordinary zombies with coordinates only.
-- This companion sanitizes the two remaining retaliation seams after actual
-- Bandit damage: ZombieActions.Smack and BanditUtils.Hit.
--
-- v6 additionally removes the hidden PathFindBehavior2 Goal.Character -> Bandit
-- relation. Build 42.20.3 NetworkZombieMind.set() rejects any character path goal
-- whose target is not IsoPlayer, even when zombie:getTarget() has already been
-- cleared. We cancel only that unsafe PFB character goal and do not issue an
-- exact-coordinate replacement path. Normal BanditUpdate coordinate pursuit and
-- gunshot sound-coordinate alerts remain responsible for movement.
if isServer() then return end

local MARKER = "character-relation-suppression-v6"
LCC_BANDITS_ATTACK_RELATION_POC = MARKER

local stats = rawget(_G, "LCC_BanditsRelationshipStats") or {}
stats.shotCoordinateAlerts = stats.shotCoordinateAlerts or 0
stats.meleeChecks = stats.meleeChecks or 0
stats.meleeDamageEvents = stats.meleeDamageEvents or 0
stats.gunChecks = stats.gunChecks or 0
stats.gunDamageEvents = stats.gunDamageEvents or 0
stats.targetClears = stats.targetClears or 0
stats.attackedByClears = stats.attackedByClears or 0
stats.pfbCharacterGoalChecks = stats.pfbCharacterGoalChecks or 0
stats.pfbCharacterGoalCancels = stats.pfbCharacterGoalCancels or 0
stats.coordinateResponses = stats.coordinateResponses or 0 -- legacy counter; v6 keeps it at zero
stats.retaliationPathsSuppressed = stats.retaliationPathsSuppressed or 0
stats.sanitizeErrors = stats.sanitizeErrors or 0
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
    return ok and value or nil
end

local function currentTarget(character)
    if not character then return nil end
    local ok, value = pcall(function() return character:getTarget() end)
    return ok and value or nil
end

local function banditCharacterGoal(character)
    if not character then return false, nil end
    local okPfb, pfb = pcall(function() return character:getPathFindBehavior2() end)
    if not okPfb or not pfb then return false, nil end

    stats.pfbCharacterGoalChecks = stats.pfbCharacterGoalChecks + 1
    local okGoal, isGoal = pcall(function() return pfb:isGoalCharacter() end)
    if not okGoal or not isGoal then return false, pfb end

    local okTarget, target = pcall(function() return pfb:getTargetChar() end)
    if not okTarget then return false, pfb end
    return isBandit(target), pfb
end

local function cancelBanditCharacterGoal(victim)
    local unsafe, pfb = banditCharacterGoal(victim)
    if not unsafe or not pfb then return false end

    local ok = pcall(function() pfb:cancel() end)
    if ok then
        stats.pfbCharacterGoalCancels = stats.pfbCharacterGoalCancels + 1
        return true
    end

    stats.sanitizeErrors = stats.sanitizeErrors + 1
    warnOnce("pfb-cancel", "[LCC][BanditsRelationPoC][ERROR] PathFindBehavior2.cancel() failed")
    return false
end

local function logPairOnce(source, victim, attacker, targetCleared, attackedByCleared, pfbCancelled)
    local key = table.concat({source, characterId(victim), characterId(attacker)}, ":")
    if seenPairs[key] then return end
    seenPairs[key] = true
    print(string.format(
        "[LCC][BanditsRelationPoC][SANITIZE] marker=%s source=%s victim=%s attacker=%s targetCleared=%s attackedByCleared=%s pfbCharacterGoalCancelled=%s coordinateResponse=false retaliationPathSuppressed=true",
        MARKER,
        source,
        characterId(victim),
        characterId(attacker),
        tostring(targetCleared),
        tostring(attackedByCleared),
        tostring(pfbCancelled)
    ))
end

local function sanitizeBanditRelationship(victim, attacker, source, clearAttackedBy)
    if not isOrdinaryZombie(victim) or not isBandit(attacker) then return false end
    if not victim:isAlive() then return false end

    local targetCleared = false
    local attackedByCleared = false

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

    -- Build 42.20.3 keeps PathFindBehavior2.goalCharacter independently from
    -- IsoZombie.target. Leaving Goal.Character -> Bandit produces
    -- NetworkZombieMind: goal character is not set. Cancel only that stale goal.
    local pfbCancelled = cancelBanditCharacterGoal(victim)

    -- No exact-coordinate path is issued here. The normal zombie update will
    -- rediscover a nearby Bandit and use the existing coordinate-only pursuit.
    stats.retaliationPathsSuppressed = stats.retaliationPathsSuppressed + 1
    logPairOnce(source, victim, attacker, targetCleared, attackedByCleared, pfbCancelled)
    return targetCleared or attackedByCleared or pfbCancelled
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
            local hasBanditPfbGoal = select(1, banditCharacterGoal(victim))
            if damaged then stats.meleeDamageEvents = stats.meleeDamageEvents + 1 end
            if damaged or hasBanditTarget or hasBanditPfbGoal then
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
            local hasBanditPfbGoal = select(1, banditCharacterGoal(victim))
            if damaged then stats.gunDamageEvents = stats.gunDamageEvents + 1 end
            if damaged or hasBanditTarget or hasBanditPfbGoal then
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
        "[LCC][BanditsRelationPoC][SUMMARY] marker=%s shotCoordinateAlerts=%d meleeChecks=%d meleeDamageEvents=%d gunChecks=%d gunDamageEvents=%d targetClears=%d attackedByClears=%d pfbCharacterGoalChecks=%d pfbCharacterGoalCancels=%d coordinateResponses=%d retaliationPathsSuppressed=%d sanitizeErrors=%d",
        MARKER,
        stats.shotCoordinateAlerts or 0,
        stats.meleeChecks or 0,
        stats.meleeDamageEvents or 0,
        stats.gunChecks or 0,
        stats.gunDamageEvents or 0,
        stats.targetClears or 0,
        stats.attackedByClears or 0,
        stats.pfbCharacterGoalChecks or 0,
        stats.pfbCharacterGoalCancels or 0,
        stats.coordinateResponses or 0,
        stats.retaliationPathsSuppressed or 0,
        stats.sanitizeErrors or 0
    ))
end

Events.EveryOneMinute.Add(summary)

print(string.format(
    "[LCC][BanditsRelationPoC][BOOT] marker=%s gunshotAlert=coordinate-only meleeWrapped=%s gunHitWrapped=%s retaliationPath=disabled pfbBanditCharacterGoal=cancel",
    MARKER,
    tostring(smackWrapped),
    tostring(gunWrapped)
))
