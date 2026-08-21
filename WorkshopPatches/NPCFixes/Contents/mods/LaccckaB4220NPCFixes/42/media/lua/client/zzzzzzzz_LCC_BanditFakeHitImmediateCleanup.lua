-- LCC immediate cleanup for B42.20.3 Bandit gun-hit fake-zombie relations.
--
-- BanditUtils.Hit() passes getCell():getFakeZombieForHit() into victim:Hit().
-- The existing OnZombieUpdate cleanup proved that this exact engine helper can
-- become IsoZombie.target / PFB Goal.Character. Under heavy gunfire a few
-- NetworkZombieMind serialization passes can occur before the later update sweep.
--
-- This wrapper runs immediately after the complete current BanditUtils.Hit chain
-- (including the relationship-suppression wrapper) and clears ONLY relations to
-- the exact cell fake-hit zombie. It never cancels arbitrary non-player goals.
if isServer() then return end

local MARKER = "fake-hit-immediate-cleanup-v1"
LCC_BANDITS_FAKE_HIT_IMMEDIATE_CLEANUP = MARKER

local stats = {
    calls = 0,
    watchedZombieHits = 0,
    targetClears = 0,
    attackedByClears = 0,
    characterGoalCancels = 0,
    changedHits = 0,
    errors = 0,
}

local detailBudget = 24

local function isBandit(character)
    return character ~= nil
        and instanceof(character, "IsoZombie")
        and character:getVariableBoolean("Bandit") == true
end

local function isOrdinaryZombie(character)
    return character ~= nil
        and instanceof(character, "IsoZombie")
        and not isBandit(character)
end

local function characterId(character)
    if not character then return "nil" end
    if BanditUtils and type(BanditUtils.GetCharacterID) == "function" then
        local ok, value = pcall(BanditUtils.GetCharacterID, character)
        if ok and value ~= nil then return tostring(value) end
    end
    return tostring(character)
end

local function cleanupFakeRelation(victim)
    local cell = getCell()
    if not cell then return false end

    local okFake, fakeZombie = pcall(function() return cell:getFakeZombieForHit() end)
    if not okFake or not fakeZombie then
        stats.errors = stats.errors + 1
        return false
    end

    local changed = false
    local targetCleared = false
    local attackedByCleared = false
    local pfbCancelled = false

    local okTarget, target = pcall(function() return victim:getTarget() end)
    if okTarget and target == fakeZombie then
        local okClear = pcall(function() victim:setTarget(nil) end)
        if okClear then
            changed = true
            targetCleared = true
            stats.targetClears = stats.targetClears + 1
        else
            stats.errors = stats.errors + 1
        end
    end

    local okAttacked, attackedBy = pcall(function() return victim:getAttackedBy() end)
    if okAttacked and attackedBy == fakeZombie then
        local okClear = pcall(function() victim:setAttackedBy(nil) end)
        if okClear then
            changed = true
            attackedByCleared = true
            stats.attackedByClears = stats.attackedByClears + 1
        else
            stats.errors = stats.errors + 1
        end
    end

    local okPfb, pfb = pcall(function() return victim:getPathFindBehavior2() end)
    if okPfb and pfb then
        local okGoal, goalCharacter = pcall(function() return pfb:isGoalCharacter() end)
        if okGoal and goalCharacter then
            local okGoalTarget, goalTarget = pcall(function() return pfb:getTargetChar() end)
            if okGoalTarget and goalTarget == fakeZombie then
                local okCancel = pcall(function() pfb:cancel() end)
                if okCancel then
                    changed = true
                    pfbCancelled = true
                    stats.characterGoalCancels = stats.characterGoalCancels + 1
                else
                    stats.errors = stats.errors + 1
                end
            end
        elseif not okGoal then
            stats.errors = stats.errors + 1
        end
    elseif not okPfb then
        stats.errors = stats.errors + 1
    end

    if changed then
        stats.changedHits = stats.changedHits + 1
        if detailBudget > 0 then
            detailBudget = detailBudget - 1
            print(string.format(
                "[LCC][BanditsFakeHitImmediate][CLEAN] marker=%s victim=%s state=%s targetCleared=%s attackedByCleared=%s pfbCancelled=%s",
                MARKER,
                characterId(victim),
                tostring(victim:getActionStateName() or "<none>"),
                tostring(targetCleared),
                tostring(attackedByCleared),
                tostring(pfbCancelled)
            ))
        end
    end

    return changed
end

local wrapped = false
if BanditUtils and type(BanditUtils.Hit) == "function" then
    local previousHit = BanditUtils.Hit
    BanditUtils.Hit = function(shooter, item, victim, ...)
        stats.calls = stats.calls + 1
        local watch = isBandit(shooter) and isOrdinaryZombie(victim)
        if watch then stats.watchedZombieHits = stats.watchedZombieHits + 1 end

        local result = previousHit(shooter, item, victim, ...)

        if watch and victim and victim:isAlive() then
            cleanupFakeRelation(victim)
        end
        return result
    end
    wrapped = true
end

Events.EveryOneMinute.Add(function()
    print(string.format(
        "[LCC][BanditsFakeHitImmediate][SUMMARY] marker=%s calls=%d watchedZombieHits=%d changedHits=%d targetClears=%d attackedByClears=%d characterGoalCancels=%d errors=%d wrapped=%s",
        MARKER,
        stats.calls,
        stats.watchedZombieHits,
        stats.changedHits,
        stats.targetClears,
        stats.attackedByClears,
        stats.characterGoalCancels,
        stats.errors,
        tostring(wrapped)
    ))
end)

print(string.format(
    "[LCC][BanditsFakeHitImmediate][BOOT] marker=%s wrapped=%s target=exact-engine-fake-hit-zombie timing=post-BanditUtils.Hit",
    MARKER, tostring(wrapped)
))
