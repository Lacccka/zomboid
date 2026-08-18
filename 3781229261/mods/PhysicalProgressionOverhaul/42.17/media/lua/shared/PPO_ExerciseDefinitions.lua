PPO = PPO or {}
PPO.ExerciseDefinitions = PPO.ExerciseDefinitions or {}

local ExerciseDefinitions = PPO.ExerciseDefinitions

-- Load is the work a repetition costs, not the time it happened to take.
-- Vanilla charges endurance per repetition and never per minute:
-- Fitness.reduceEndurance spends BASE_ENDURANCE_RED = 0.015, scaled by 1.3 for
-- Metabolics.FitnessHeavy, and no perk appears in it. The animation rate does
-- scale with the Fitness perk (IsoPlayer.setFitnessSpeed, capped at 1.5x), so a
-- clock-charged load made a higher level cost more sets for the same tone.
--
-- ISFitnessAction:update force-stops the action once the endurance moodle
-- passes level 2, which is endurance 0.25, so a set to exhaustion always
-- spends the same 0.75 of the bar. Pricing a repetition off its own endurance
-- cost therefore makes one set worth the same training minutes for every
-- character, every exercise, and every DayLength.
local ENDURANCE_BAND = 0.75
local SET_TRAINING_MINUTES = 25
local BASE_ENDURANCE_PER_REPEAT = 0.015
local HEAVY_ENDURANCE_SCALE = 1.3
local MINUTES_PER_STIMULUS = 30

-- Mirrors Metabolics in vanilla FitnessExercises.exercisesType. Pinned here
-- rather than read at runtime so a mod that edits the vanilla table cannot
-- silently move PPO's load, and so the pin is testable.
local function minutesPerRepeatFor(heavy)
    local cost = BASE_ENDURANCE_PER_REPEAT
    if heavy then cost = cost * HEAVY_ENDURANCE_SCALE end
    return SET_TRAINING_MINUTES * cost / ENDURANCE_BAND
end

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
        heavy = true,
    },
    barbellcurl = {
        periodMs = 2200,
        spXp = { Strength = 7.2 },
        mpXp = { Strength = 7 },
        heavy = true,
    },
    dumbbellpress = {
        periodMs = 1500,
        spXp = { Strength = 7.2 },
        mpXp = { Strength = 7 },
        heavy = true,
    },
    bicepscurl = {
        periodMs = 1900,
        spXp = { Strength = 7.2 },
        mpXp = { Strength = 7 },
        heavy = true,
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

    local minutesPerRepeat = {}
    local loadPerRepeat = {}
    local minutes = minutesPerRepeatFor(source.heavy == true)
    for _, component in ipairs({ "Strength", "Fitness" }) do
        if source.spXp[component] ~= nil then
            minutesPerRepeat[component] = minutes
            loadPerRepeat[component] = minutes / MINUTES_PER_STIMULUS
        end
    end

    return {
        id = exerciseId,
        periodMs = source.periodMs,
        ttlMs = math.max(5000, 2 * source.periodMs),
        spXp = copyComponents(source.spXp),
        mpXp = copyComponents(source.mpXp),
        minutesPerRepeat = minutesPerRepeat,
        loadPerRepeat = loadPerRepeat,
    }
end

