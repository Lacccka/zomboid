require "Bandit"
require "BanditBrain"
require "BanditPrograms"

ZombiePrograms = ZombiePrograms or {}

local Program = ZombiePrograms.LCCQFQuestGiver or {}
local zombieDontAttackCheat = nil
local cheatResolved = false

local function resolveZombieDontAttackCheat()
    if cheatResolved then return zombieDontAttackCheat end
    cheatResolved = true
    if CheatType and CheatType.fromString then
        zombieDontAttackCheat = CheatType.fromString("ZOMBIES_DONT_ATTACK")
    end
    return zombieDontAttackCheat
end

local function applyZombieIgnoreFlag(bandit)
    if not bandit or not bandit.getCheats then return end
    local cheats = bandit:getCheats()
    local flag = resolveZombieDontAttackCheat()
    if cheats and flag then
        cheats:set(flag, true)
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
    -- targets. Keep a layered policy because Bandits2 is client-driven in MP.
    if bandit.setUseless then bandit:setUseless(true) end
    if bandit.setInvulnerable then bandit:setInvulnerable(true) end
    if bandit.setShootable then bandit:setShootable(false) end
    if bandit.setIgnoreStaggerBack then bandit:setIgnoreStaggerBack(true) end
    applyZombieIgnoreFlag(bandit)
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
