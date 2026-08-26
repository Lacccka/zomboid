require "Bandit"
require "BanditBrain"
require "BanditPrograms"

ZombiePrograms = ZombiePrograms or {}

local Program = ZombiePrograms.LCCQFQuestGiver or {}
local NON_COMBAT_MODDATA_KEY = "lccqIgnoreZombieAggro"
local NON_COMBAT_VARIABLE = "LCCQFNonCombat"

local function markNonCombatRole(bandit)
    if not bandit then return end

    local brain = BanditBrain.Get(bandit)
    if type(brain) == "table" then
        brain.lccqNonCombat = true
    end

    local modData = bandit.getModData and bandit:getModData() or nil
    if modData then
        modData[NON_COMBAT_MODDATA_KEY] = true
    end
    if bandit.setVariable then
        bandit:setVariable(NON_COMBAT_VARIABLE, true)
    end
end

local function recoverStandingState(bandit)
    if bandit.isKnockedDown and bandit:isKnockedDown() and bandit.setKnockedDown then
        bandit:setKnockedDown(false)
    end
    if bandit.isOnFloor and bandit:isOnFloor() and bandit.setOnFloor then
        bandit:setOnFloor(false)
    end
    if bandit.setFallOnFront then
        bandit:setFallOnFront(false)
    end
end

local function enforceQuestGiverRole(bandit)
    if not bandit or bandit:isDead() then return end

    markNonCombatRole(bandit)
    Bandit.ForceStationary(bandit, true)
    Bandit.ClearMoveTasks(bandit)

    -- Generic Bandits combat/victim selection is vetoed before this custom
    -- program by the B42 NPC Fixes BanditUpdate seam. These flags are the
    -- physical presentation safety layer after that scheduling decision.
    if bandit.setUseless then bandit:setUseless(true) end
    if bandit.setInvulnerable then bandit:setInvulnerable(true) end
    if bandit.setShootable then bandit:setShootable(false) end
    if bandit.setIgnoreStaggerBack then bandit:setIgnoreStaggerBack(true) end
    recoverStandingState(bandit)
end

Program.Init = function(bandit)
    enforceQuestGiverRole(bandit)
end

Program.Prepare = function(bandit)
    enforceQuestGiverRole(bandit)
    return {
        status = true,
        next = "Main",
        tasks = {},
    }
end

Program.Main = function(bandit)
    enforceQuestGiverRole(bandit)

    return {
        status = true,
        next = "Main",
        tasks = {
            { action = "Time", anim = "ShiftWeight", time = 200 },
        },
    }
end

ZombiePrograms.LCCQFQuestGiver = Program

return Program
