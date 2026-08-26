require "Bandit"
require "BanditBrain"
require "BanditPrograms"

ZombiePrograms = ZombiePrograms or {}

local Program = ZombiePrograms.LCCQFQuestGiver or {}

local function enforceQuestGiverRole(bandit)
    if not bandit or bandit:isDead() then return end

    -- Bandits2 Defend is unsuitable for a static quest giver: it explicitly
    -- clears stationary mode and can switch outdoor NPCs to Looter. Keep this
    -- provider-specific role anchored instead of fighting that program every
    -- server reconciliation tick.
    Bandit.ForceStationary(bandit, true)
    Bandit.ClearMoveTasks(bandit)

    -- This program is deliberately reserved for essential quest givers.
    -- Other framework NPC roles may remain mortal by using another program.
    if bandit.setInvulnerable then
        bandit:setInvulnerable(true)
    end
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

    -- A small idle task keeps the physical NPC visually alive without creating
    -- Move/GoTo work or handing control to Bandits2 combat/looter programs.
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
