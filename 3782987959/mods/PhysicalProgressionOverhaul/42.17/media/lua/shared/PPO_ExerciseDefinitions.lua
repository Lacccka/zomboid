PPO = PPO or {}
PPO.ExerciseDefinitions = PPO.ExerciseDefinitions or {}

local ExerciseDefinitions = PPO.ExerciseDefinitions

-- Load is time under tension, not repetitions. Every exercise charges the same
-- rate per game training minute; vanilla already differentiates exercises by XP
-- per repetition, and duplicating that in load would only break the alignment
-- between the 10/20/30/40/50/60 fitness UI grid and the stage boundaries.
local LOAD_PER_TRAINING_MINUTE = 1 / 30

local DEFINITIONS = {
    squats = {
        periodMs = 3000,
        spXp = { Fitness = 4 },
        mpXp = { Fitness = 4 },
    },
    pushups = {
        periodMs = 1300,
        spXp = { Strength = 6 },
        mpXp = { Strength = 6 },
    },
    situp = {
        periodMs = 1300,
        spXp = { Fitness = 2 },
        mpXp = { Fitness = 2 },
    },
    burpees = {
        periodMs = 2400,
        spXp = { Strength = 4.8, Fitness = 3.2 },
        mpXp = { Strength = 4, Fitness = 3 },
    },
    barbellcurl = {
        periodMs = 2200,
        spXp = { Strength = 7.2 },
        mpXp = { Strength = 7 },
    },
    dumbbellpress = {
        periodMs = 1500,
        spXp = { Strength = 7.2 },
        mpXp = { Strength = 7 },
    },
    bicepscurl = {
        periodMs = 1900,
        spXp = { Strength = 7.2 },
        mpXp = { Strength = 7 },
    },
}

local function copyComponents(source)
    local result = {}
    if source.Strength ~= nil then result.Strength = source.Strength end
    if source.Fitness ~= nil then result.Fitness = source.Fitness end
    return result
end

function ExerciseDefinitions.get(exerciseId)
    local source = DEFINITIONS[exerciseId]
    if source == nil then return nil end

    local loadRate = {}
    if source.spXp.Strength ~= nil then
        loadRate.Strength = LOAD_PER_TRAINING_MINUTE
    end
    if source.spXp.Fitness ~= nil then
        loadRate.Fitness = LOAD_PER_TRAINING_MINUTE
    end

    return {
        id = exerciseId,
        periodMs = source.periodMs,
        ttlMs = math.max(5000, 2 * source.periodMs),
        spXp = copyComponents(source.spXp),
        mpXp = copyComponents(source.mpXp),
        loadRate = loadRate,
    }
end

