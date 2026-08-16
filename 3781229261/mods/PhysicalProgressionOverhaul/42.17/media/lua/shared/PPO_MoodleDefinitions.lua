PPO = PPO or {}
PPO.MoodleDefinitions = PPO.MoodleDefinitions or {}

local Definitions = PPO.MoodleDefinitions

Definitions.Order = {
    "StrengthLoad",
    "FitnessLoad",
    "StrengthTone",
    "FitnessTone",
}

Definitions.ByID = {
    StrengthLoad = {
        id = "StrengthLoad",
        direction = "Strength",
        kind = "Load",
        sourceField = "loadStage",
        alignment = "Bad",
        icon = "Strength.png",
    },
    FitnessLoad = {
        id = "FitnessLoad",
        direction = "Fitness",
        kind = "Load",
        sourceField = "loadStage",
        alignment = "Bad",
        icon = "Fitness.png",
    },
    StrengthTone = {
        id = "StrengthTone",
        direction = "Strength",
        kind = "Tone",
        sourceField = "toneStage",
        alignment = "Good",
        icon = "Strength_Tone.png",
    },
    FitnessTone = {
        id = "FitnessTone",
        direction = "Fitness",
        kind = "Tone",
        sourceField = "toneStage",
        alignment = "Good",
        icon = "Fitness_Tone.png",
    },
}

