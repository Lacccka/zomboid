require "Compat/PPO_Compat"

-- Shared by every support file for a mod that adds an exercise to the vanilla
-- `FitnessExercises.exercisesType`.
--
-- Such a mod does not write XP numbers: it writes a `stiffness` list, a
-- `metabolics` class and an `xpMod`, and vanilla `Fitness.incStats` turns those
-- into the award -- Strength from `arms 4 + chest 2`, Fitness from
-- `legs 4 + abs 2`, both scaled by `xpMod`. So a support file states what its
-- mod registers, and the award is derived here by the vanilla formula. Two
-- consequences worth keeping:
--
--   * no third-party number is copied into PPO; the arithmetic is vanilla's;
--   * the derivation is one place, so the next supported exercise mod states
--     three fields and inherits the pricing.
--
-- The values are pinned by the support file rather than read out of the live
-- `exercisesType` for the same reason the vanilla seven are pinned: a table PPO
-- reads at runtime is a table any load order can move under it. Drift is caught
-- by the contract test, which recomputes these from the installed copy.

PPO = PPO or {}
PPO.Compat = PPO.Compat or {}
PPO.Compat.Shared = PPO.Compat.Shared or {}
PPO.Compat.Shared.Exercises = PPO.Compat.Shared.Exercises or {}

local Exercises = PPO.Compat.Shared.Exercises

-- `Fitness.incStats`, offsets read from `zombie.characters.Fitness`.
local STIFFNESS_AWARD = {
    Strength = { arms = 4, chest = 2 },
    Fitness = { legs = 4, abs = 2 },
}

local function parts(stiffness)
    local found = {}
    if type(stiffness) ~= "string" then return found end
    for part in string.gmatch(stiffness, "[^,%s]+") do
        found[part] = true
    end
    return found
end

-- The dedicated server truncates each portion with JVM `f2i` before
-- `GameServer.addXp`, single player passes the float straight to `AddXP`.
local function truncate(value)
    return value - (value % 1)
end

-- `registration` is what the mod writes into the vanilla table, plus the period
-- its action schedules:
--   id, stiffness, xpMod, heavy, periodMs
function Exercises.register(modId, registration)
    if type(registration) ~= "table" then return false end
    local trained = parts(registration.stiffness)
    local xpMod = registration.xpMod
    if type(xpMod) ~= "number" or xpMod ~= xpMod then return false end

    local spXp = {}
    local mpXp = {}
    local trains = false
    for component, weights in pairs(STIFFNESS_AWARD) do
        local total = 0
        for part, weight in pairs(weights) do
            if trained[part] then total = total + weight end
        end
        if total > 0 then
            spXp[component] = total * xpMod
            mpXp[component] = truncate(spXp[component])
            trains = true
        end
    end
    -- An exercise that trains neither direction is not PPO's business: leaving
    -- it unclaimed keeps the vanilla behaviour instead of opening a session
    -- that can never be proved.
    if not trains then return false end

    return PPO.Compat.registerExercise(registration.id, {
        modId = modId,
        periodMs = registration.periodMs,
        spXp = spXp,
        mpXp = mpXp,
        heavy = registration.heavy == true,
    })
end
