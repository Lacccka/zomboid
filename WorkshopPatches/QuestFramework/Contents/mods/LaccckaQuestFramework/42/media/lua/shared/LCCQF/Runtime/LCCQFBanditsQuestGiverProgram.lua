require "Bandit"
require "BanditBrain"
require "BanditPrograms"

-- BanditZombie is client-only. The shared quest-giver role is parsed on the
-- dedicated server too, so never require the client cache there.
if not (isServer and isServer()) then
    require "BanditZombie"
    require "BanditUtils"
end

ZombiePrograms = ZombiePrograms or {}

local Program = ZombiePrograms.LCCQFQuestGiver or {}
local NON_COMBAT_MODDATA_KEY = "lccqIgnoreZombieAggro"
local NON_COMBAT_VARIABLE = "LCCQFNonCombat"

local function excludeFromBanditsZombieCombat(bandit)
    if not bandit then return end

    -- Do not use getCheats()/PlayerCheats here. PlayerCheats is a Java userdata
    -- that is not exposed as a Lua-indexable API in B42.20.3.
    local modData = bandit.getModData and bandit:getModData() or nil
    if modData then
        modData[NON_COMBAT_MODDATA_KEY] = true
    end
    if bandit.setVariable then
        bandit:setVariable(NON_COMBAT_VARIABLE, true)
    end

    -- Bandits2 UpdateZombies chooses potential NPC prey from CacheLightB.
    -- BanditZombie's own OnZombieUpdate populates that cache before BanditUpdate
    -- runs, so removing this physical quest giver here keeps it materialized in
    -- Cache while excluding only the zombie-vs-bandit combat discovery layer.
    if BanditZombie and BanditZombie.CacheLightB then
        local runtimeId = nil
        if BanditUtils and BanditUtils.GetZombieID then
            runtimeId = BanditUtils.GetZombieID(bandit)
        end
        if runtimeId == nil then
            local brain = BanditBrain.Get(bandit)
            runtimeId = brain and brain.id or nil
        end
        if runtimeId ~= nil then
            BanditZombie.CacheLightB[runtimeId] = nil
        end
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

    Bandit.ForceStationary(bandit, true)
    Bandit.ClearMoveTasks(bandit)

    -- Essential quest givers are presentation/interaction entities, not combat
    -- targets. Keep the policy client-side too because Bandits2 drives its
    -- physical NPC program from BanditUpdate in multiplayer.
    if bandit.setUseless then bandit:setUseless(true) end
    if bandit.setInvulnerable then bandit:setInvulnerable(true) end
    if bandit.setShootable then bandit:setShootable(false) end
    if bandit.setIgnoreStaggerBack then bandit:setIgnoreStaggerBack(true) end
    excludeFromBanditsZombieCombat(bandit)
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
