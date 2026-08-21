-- LCC cleanup for B42.20.3 fake-hit zombie AI relations.
--
-- BanditUtils.Hit uses getCell():getFakeZombieForHit() as the attacker passed to
-- IsoGameCharacter.Hit(). Runtime tracing showed that hit reaction processing can
-- temporarily promote this engine helper IsoZombie into IsoZombie.target and/or
-- PathFindBehavior2 Goal.Character. NetworkZombieMind cannot serialize a
-- Goal.Character unless the target is IsoPlayer, producing
-- "NetworkZombieMind: goal character is not set". A vanilla target pointing at
-- the same helper is also not a legitimate combat relation and must not survive.
--
-- Runtime 2026-08-21 confirmed the narrow fix: 79 fake target relations were
-- cleared, no Goal.Character survived to the late sweep, and NetworkZombieMind
-- warnings dropped to zero. v3 keeps the same behavior but caches the cell's
-- reusable fake-hit zombie so getFakeZombieForHit() is not called for every
-- ordinary-zombie update.
--
-- This fix is deliberately exact: it only clears relations to the cell's engine
-- fake-hit zombie. Real players, Bandits and arbitrary non-player mod targets are
-- untouched here.
if isServer() then return end

local MARKER = "fake-hit-relation-cleanup-v3"
LCC_BANDITS_FAKE_HIT_PFB_CLEANUP = MARKER

local stats = {
    updates = 0,
    fakeRefRefreshes = 0,
    fakeRelations = 0,
    fakeCharacterGoals = 0,
    pfbCancels = 0,
    targetClears = 0,
    attackedByClears = 0,
    errors = 0,
    lateRebinds = 0,
}

local detailBudget = 24
local rebindTicks = 0
local rebound = false
local cachedCell = nil
local cachedFakeZombie = nil

local function isBandit(character)
    return character ~= nil
        and instanceof(character, "IsoZombie")
        and character:getVariableBoolean("Bandit") == true
end

local function characterId(character)
    if not character then return "nil" end
    if BanditUtils and type(BanditUtils.GetCharacterID) == "function" then
        local ok, value = pcall(BanditUtils.GetCharacterID, character)
        if ok and value ~= nil then return tostring(value) end
    end
    return tostring(character)
end

local function getFakeZombie()
    local cell = getCell()
    if not cell then return nil end

    if cell ~= cachedCell or not cachedFakeZombie then
        local ok, fakeZombie = pcall(function() return cell:getFakeZombieForHit() end)
        if not ok or not fakeZombie then
            stats.errors = stats.errors + 1
            return nil
        end
        cachedCell = cell
        cachedFakeZombie = fakeZombie
        stats.fakeRefRefreshes = stats.fakeRefRefreshes + 1
    end

    return cachedFakeZombie
end

local function onZombieUpdate(zombie)
    if not zombie or not zombie:isAlive() or isBandit(zombie) then return end
    stats.updates = stats.updates + 1

    local fakeZombie = getFakeZombie()
    if not fakeZombie then return end

    local changed = false
    local targetCleared = false
    local attackedByCleared = false
    local pfbCancelled = false

    -- Clear the ordinary vanilla target even if PFB has already changed to a
    -- location goal. The fake helper must never become a durable AI target.
    local okTarget, target = pcall(function() return zombie:getTarget() end)
    if okTarget and target == fakeZombie then
        local okClear = pcall(function() zombie:setTarget(nil) end)
        if okClear then
            targetCleared = true
            changed = true
            stats.targetClears = stats.targetClears + 1
        else
            stats.errors = stats.errors + 1
        end
    end

    local okAttackedBy, attackedBy = pcall(function() return zombie:getAttackedBy() end)
    if okAttackedBy and attackedBy == fakeZombie then
        local okClear = pcall(function() zombie:setAttackedBy(nil) end)
        if okClear then
            attackedByCleared = true
            changed = true
            stats.attackedByClears = stats.attackedByClears + 1
        else
            stats.errors = stats.errors + 1
        end
    end

    local okPfb, pfb = pcall(function() return zombie:getPathFindBehavior2() end)
    if okPfb and pfb then
        local okGoal, isCharacter = pcall(function() return pfb:isGoalCharacter() end)
        if okGoal and isCharacter then
            local okGoalTarget, goalTarget = pcall(function() return pfb:getTargetChar() end)
            if okGoalTarget and goalTarget == fakeZombie then
                stats.fakeCharacterGoals = stats.fakeCharacterGoals + 1
                local okCancel = pcall(function() pfb:cancel() end)
                if okCancel then
                    pfbCancelled = true
                    changed = true
                    stats.pfbCancels = stats.pfbCancels + 1
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
        stats.fakeRelations = stats.fakeRelations + 1
        if detailBudget > 0 then
            detailBudget = detailBudget - 1
            print(string.format(
                "[LCC][BanditsFakeHitRelation][CLEAN] marker=%s zombie=%s state=%s targetCleared=%s attackedByCleared=%s pfbCancelled=%s",
                MARKER,
                characterId(zombie),
                tostring(zombie:getActionStateName() or "<none>"),
                tostring(targetCleared),
                tostring(attackedByCleared),
                tostring(pfbCancelled)
            ))
        end
    end
end

Events.OnZombieUpdate.Add(onZombieUpdate)

local function lateRebind()
    rebindTicks = rebindTicks + 1
    if rebound or rebindTicks < 120 then return end

    local okRemove = pcall(function() Events.OnZombieUpdate.Remove(onZombieUpdate) end)
    local okAdd = pcall(function() Events.OnZombieUpdate.Add(onZombieUpdate) end)
    if okAdd then
        rebound = true
        stats.lateRebinds = stats.lateRebinds + 1
        print(string.format(
            "[LCC][BanditsFakeHitRelation][REBIND] marker=%s tick=%d removeOk=%s addOk=%s",
            MARKER, rebindTicks, tostring(okRemove), tostring(okAdd)
        ))
        pcall(function() Events.OnTick.Remove(lateRebind) end)
    end
end
Events.OnTick.Add(lateRebind)

Events.EveryOneMinute.Add(function()
    print(string.format(
        "[LCC][BanditsFakeHitRelation][SUMMARY] marker=%s updates=%d fakeRefRefreshes=%d fakeRelations=%d fakeCharacterGoals=%d pfbCancels=%d targetClears=%d attackedByClears=%d errors=%d lateRebinds=%d",
        MARKER,
        stats.updates,
        stats.fakeRefRefreshes,
        stats.fakeRelations,
        stats.fakeCharacterGoals,
        stats.pfbCancels,
        stats.targetClears,
        stats.attackedByClears,
        stats.errors,
        stats.lateRebinds
    ))
end)

print(string.format(
    "[LCC][BanditsFakeHitRelation][BOOT] marker=%s target=engine-fake-hit-zombie action=clear-target+attackedBy+cancel-character-pfb fakeRef=cached-per-cell",
    MARKER
))
