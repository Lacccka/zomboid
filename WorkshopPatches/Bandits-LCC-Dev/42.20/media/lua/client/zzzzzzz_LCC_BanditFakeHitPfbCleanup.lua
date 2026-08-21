-- LCC cleanup for B42.20.3 fake-hit zombie character goals.
--
-- BanditUtils.Hit uses getCell():getFakeZombieForHit() as the attacker passed to
-- IsoGameCharacter.Hit(). Runtime tracing showed that hit reaction processing can
-- temporarily promote this engine helper IsoZombie into both IsoZombie.target and
-- PathFindBehavior2 Goal.Character. NetworkZombieMind cannot serialize a
-- Goal.Character unless the target is IsoPlayer, producing
-- "NetworkZombieMind: goal character is not set".
--
-- This fix is deliberately exact: it only clears relations to the cell's engine
-- fake-hit zombie. Real players, Bandits and arbitrary non-player mod targets are
-- untouched here.
if isServer() then return end

local MARKER = "fake-hit-pfb-cleanup-v1"
LCC_BANDITS_FAKE_HIT_PFB_CLEANUP = MARKER

local stats = {
    updates = 0,
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

local function onZombieUpdate(zombie)
    if not zombie or not zombie:isAlive() or isBandit(zombie) then return end
    stats.updates = stats.updates + 1

    local cell = getCell()
    if not cell then return end

    local okFake, fakeZombie = pcall(function() return cell:getFakeZombieForHit() end)
    if not okFake or not fakeZombie then
        stats.errors = stats.errors + 1
        return
    end

    local pfb = zombie:getPathFindBehavior2()
    if pfb and pfb:isGoalCharacter() and pfb:getTargetChar() == fakeZombie then
        stats.fakeCharacterGoals = stats.fakeCharacterGoals + 1

        local targetCleared = false
        if zombie:getTarget() == fakeZombie then
            zombie:setTarget(nil)
            targetCleared = true
            stats.targetClears = stats.targetClears + 1
        end

        local attackedByCleared = false
        local okAttackedBy, attackedBy = pcall(function() return zombie:getAttackedBy() end)
        if okAttackedBy and attackedBy == fakeZombie then
            zombie:setAttackedBy(nil)
            attackedByCleared = true
            stats.attackedByClears = stats.attackedByClears + 1
        end

        local okCancel = pcall(function() pfb:cancel() end)
        if okCancel then
            stats.pfbCancels = stats.pfbCancels + 1
        else
            stats.errors = stats.errors + 1
        end

        if detailBudget > 0 then
            detailBudget = detailBudget - 1
            print(string.format(
                "[LCC][BanditsFakeHitPfb][CLEAN] marker=%s zombie=%s state=%s targetCleared=%s attackedByCleared=%s pfbCancelled=%s",
                MARKER,
                characterId(zombie),
                tostring(zombie:getActionStateName() or "<none>"),
                tostring(targetCleared),
                tostring(attackedByCleared),
                tostring(okCancel)
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
            "[LCC][BanditsFakeHitPfb][REBIND] marker=%s tick=%d removeOk=%s addOk=%s",
            MARKER, rebindTicks, tostring(okRemove), tostring(okAdd)
        ))
        pcall(function() Events.OnTick.Remove(lateRebind) end)
    end
end
Events.OnTick.Add(lateRebind)

Events.EveryOneMinute.Add(function()
    print(string.format(
        "[LCC][BanditsFakeHitPfb][SUMMARY] marker=%s updates=%d fakeCharacterGoals=%d pfbCancels=%d targetClears=%d attackedByClears=%d errors=%d lateRebinds=%d",
        MARKER,
        stats.updates,
        stats.fakeCharacterGoals,
        stats.pfbCancels,
        stats.targetClears,
        stats.attackedByClears,
        stats.errors,
        stats.lateRebinds
    ))
end)

print(string.format(
    "[LCC][BanditsFakeHitPfb][BOOT] marker=%s target=engine-fake-hit-zombie action=clear-target+attackedBy+cancel-pfb",
    MARKER
))
