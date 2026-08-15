PPO = PPO or {}
PPO.LootDefinitions = PPO.LootDefinitions or {}

local Definitions = PPO.LootDefinitions
local MODULE = "PhysicalProgressionOverhaul."

-- The vanilla rarity ladder, read from ItemPickerJava.doSandboxSettings and the
-- declared defaults of the *LootFactor options. Step 1 is a hard 0.0 there, so
-- it means "do not insert" here rather than "insert with weight zero": an entry
-- that can never roll only makes the container's item list longer.
local LADDER = { 0, 0.05, 0.2, 0.6, 1.0, 2.0, 3.0 }
local DEFAULT_STEP = 5

-- MedicalLootNew and CannedFoodLootNew are both newDoubleOption(0 .. 4, 0.6),
-- and getLootModifierFromType applies one of them to our items before our own
-- dial is ever consulted. Dividing by the live value against this reference is
-- what makes a step mean the same thing on every server. The ceiling exists
-- because the option reaches 0.01, where an uncapped quotient would inflate a
-- weight sixtyfold.
local REFERENCE_MODIFIER = 0.6
local MAX_COMPENSATION = 8

-- getActualSpawnChance is compared against Rand.Next(10000) after a factor of
-- 100, so weight 100 is certain and anything above it is dead arithmetic.
local MAX_WEIGHT = 100

-- The vanilla option the picker applies to each loot type, and the default it
-- declares. Both are newDoubleOption(min = 0, max = 4, default = 0.6), and the
-- Apocalypse preset sets 0.6 as well, which is why the reference above is that
-- number. A server that has never touched either dial therefore compensates by
-- exactly one.
Definitions.vanillaOptions = {
    Medical = { name = "MedicalLootNew", default = REFERENCE_MODIFIER },
    CannedFood = { name = "CannedFoodLootNew", default = REFERENCE_MODIFIER },
}

Definitions.families = {
    CoursePreparations = { option = "LootCoursePreparations" },
    SportsNutrition = { option = "LootSportsNutrition" },
    TrainingAids = { option = "LootTrainingAids" },
}

-- lootType is not a choice, it is a reading of the item script.
-- ItemPickerJava.getLootType tests isMedicalLoot() before the food branch, and
-- the three drainables declare both Medical = true and DisplayCategory =
-- FirstAid. The five base:food items declare no DaysFresh, so daysFresh keeps
-- the 1000000000 that Item.<init> writes and the food branch returns CannedFood
-- rather than Food.
Definitions.items = {
    AnabolicPreparation = { family = "CoursePreparations", lootType = "Medical" },
    CardioPreparation = { family = "CoursePreparations", lootType = "Medical" },
    ProteinPowder = { family = "SportsNutrition", lootType = "CannedFood" },
    CreatineComplex = { family = "SportsNutrition", lootType = "CannedFood" },
    CalorieGainer = { family = "TrainingAids", lootType = "CannedFood" },
    ElectrolyteComplex = { family = "TrainingAids", lootType = "CannedFood" },
    PreWorkoutStimulant = { family = "TrainingAids", lootType = "CannedFood" },
    ThermogenicComplex = { family = "TrainingAids", lootType = "Medical" },
}

-- Base weights, authored against the stock modifier of 0.6 and step Normal, at
-- which the inserted weight equals the number written here. Every table below
-- rolls four times, so one container yields 1 - (1 - w * 0.006) ^ 4.
--
-- Two obvious names are deliberately absent. GymTowels reaches bathroom
-- smallbox, crate and cardboardbox, so anything placed there leaks into every
-- residential bathroom; StoreShelfCombo serves 24 room types including
-- toystore, toolstore and pawnshop. Both bans are about the table, not about
-- the room: the three Bathroom* tables below are deliberate.
Definitions.locations = {
    -- Course preparations: medicine and the black market, and no table here is
    -- shared with either other family. The course is the price of withdrawal,
    -- so it belongs to a pharmacy and not to a gym.
    --
    -- These weights were raised by about a third when the bathroom opened, so
    -- that a clinic still reads as several times denser than a house: the
    -- clinic is 4.3x the bathroom cabinet, the storage room 6.5x and the lab
    -- 9.7x. Nothing else needed raising -- a gym locker is already 4.8x the
    -- bathroom counter for the powder, and a sports shop 9.1x.
    MedicalClinicDrugs = {
        { "AnabolicPreparation", 1.8 },
        { "CardioPreparation", 2.6 },
    },
    MedicalStorageDrugs = {
        { "AnabolicPreparation", 2.8 },
        { "CardioPreparation", 4.0 },
    },
    MedicalCabinet = {
        { "AnabolicPreparation", 0.9 },
        { "CardioPreparation", 1.3 },
    },
    -- The asymmetry is intentional: the cardio preparation reads as
    -- prescription and leans clinical, the anabolic reads as illicit.
    DrugLabSupplies = {
        { "AnabolicPreparation", 4.0 },
        { "CardioPreparation", 1.4 },
    },
    DrugShackDrugs = {
        { "AnabolicPreparation", 3.4 },
        { "CardioPreparation", 1.1 },
    },
    -- The residential bathroom, and the only place course preparations reach a
    -- house. It is the mirror cabinet alone: BathroomCabinet is what the
    -- `medicine` container of room `bathroom` draws, and it is the sole entry
    -- in that procList, so unlike FitnessTrainer in a living room the draw is
    -- not diluted at all and the weight below is the whole story.
    --
    -- The other two bathroom tables carry the other two families, which is how
    -- a player finds all eight items in one bathroom without any single table
    -- mixing course preparations with anything else.
    BathroomCabinet = {
        { "AnabolicPreparation", 0.4 },
        { "CardioPreparation", 0.6 },
    },

    -- Sports nutrition alone. FitnessTrainer is the backbone of reachability:
    -- it is the only table in the family that reaches residential rooms, and
    -- gyms are rare on the map.
    FitnessTrainer = {
        { "ProteinPowder", 8 },
        { "CreatineComplex", 4 },
    },
    CrateFitnessWeights = {
        { "ProteinPowder", 5 },
        { "CreatineComplex", 2.5 },
    },
    GymWeights = {
        { "ProteinPowder", 6 },
        { "CreatineComplex", 3 },
    },

    -- Shared between sports nutrition and training aids. That overlap is
    -- allowed; the separation rule is about course preparations.
    --
    -- The two residential bathroom tables open with the rest of this group.
    -- Both are the sole entry in their container's procList -- `counter` draws
    -- BathroomCounter and `shelves` draws BathroomShelf, the two motel and
    -- workplace variants being forceForRooms -- so these weights are not
    -- diluted either. They carry the same numbers because a tub under the sink
    -- and a tub on the shelf are the same tub.
    BathroomCounter = {
        { "ProteinPowder", 1.2 },
        { "CreatineComplex", 0.6 },
        { "CalorieGainer", 0.8 },
        { "ElectrolyteComplex", 1.2 },
        { "PreWorkoutStimulant", 0.6 },
        { "ThermogenicComplex", 0.8 },
    },
    BathroomShelf = {
        { "ProteinPowder", 1.2 },
        { "CreatineComplex", 0.6 },
        { "CalorieGainer", 0.8 },
        { "ElectrolyteComplex", 1.2 },
        { "PreWorkoutStimulant", 0.6 },
        { "ThermogenicComplex", 0.8 },
    },
    GymLockers = {
        { "ProteinPowder", 6 },
        { "CreatineComplex", 3 },
        { "CalorieGainer", 3 },
        { "ElectrolyteComplex", 6 },
        { "PreWorkoutStimulant", 3 },
        { "ThermogenicComplex", 1.5 },
    },
    SportStoreAccessories = {
        { "ProteinPowder", 12 },
        { "CreatineComplex", 8 },
        { "CalorieGainer", 8 },
        { "ElectrolyteComplex", 10 },
        { "PreWorkoutStimulant", 6 },
        { "ThermogenicComplex", 4 },
    },
    GigamartDryGoods = {
        { "ProteinPowder", 6 },
        { "CreatineComplex", 3 },
        { "CalorieGainer", 4 },
        { "PreWorkoutStimulant", 2 },
    },
    -- All three crates carry the same weights because grocery > crate draws
    -- from them with weightChance 100 / 40 / 40. Filling only the first would
    -- leave half the back-room crates empty for no design reason.
    GroceryStorageCrate1 = {
        { "ProteinPowder", 4 },
        { "CreatineComplex", 2 },
        { "CalorieGainer", 3 },
        { "ElectrolyteComplex", 3 },
    },
    GroceryStorageCrate2 = {
        { "ProteinPowder", 4 },
        { "CreatineComplex", 2 },
        { "CalorieGainer", 3 },
        { "ElectrolyteComplex", 3 },
    },
    GroceryStorageCrate3 = {
        { "ProteinPowder", 4 },
        { "CreatineComplex", 2 },
        { "CalorieGainer", 3 },
        { "ElectrolyteComplex", 3 },
    },

    -- Training aids alone: retail.
    StoreShelfSnacks = {
        { "CalorieGainer", 4 },
        { "ElectrolyteComplex", 6 },
    },
    StoreShelfDrinks = {
        { "CalorieGainer", 2 },
        { "ElectrolyteComplex", 8 },
        { "PreWorkoutStimulant", 3 },
    },
    GasStoreSpecial = {
        { "ElectrolyteComplex", 5 },
        { "PreWorkoutStimulant", 4 },
    },
    StoreShelfMedical = {
        { "ElectrolyteComplex", 4 },
        { "ThermogenicComplex", 3 },
    },
    PharmacyCosmetics = {
        { "ThermogenicComplex", 2 },
    },
    GigamartBottles = {
        { "ElectrolyteComplex", 6 },
        { "PreWorkoutStimulant", 2 },
    },
    GigamartCosmetics = {
        { "CalorieGainer", 2 },
        { "ThermogenicComplex", 3 },
    },
    GasStorageCombo = {
        { "CalorieGainer", 2 },
        { "ElectrolyteComplex", 4 },
        { "PreWorkoutStimulant", 3 },
    },
}

local function finite(value)
    return type(value) == "number" and value == value
        and value ~= math.huge and value ~= -math.huge
end

-- Garbage and an out-of-range index both fall to Normal, the same contract
-- `bounded` gives every other option. Step 1 resolves to 0, which callers read
-- as a refusal rather than as a multiplier.
function Definitions.ladderMultiplier(step)
    if not finite(step) then return LADDER[DEFAULT_STEP] end
    local multiplier = LADDER[step]
    if multiplier == nil then return LADDER[DEFAULT_STEP] end
    return multiplier
end

-- nil means "do not insert". A vanilla modifier of zero already makes
-- getActualSpawnChance return zero, so an entry would only grow the table.
function Definitions.compensation(vanillaModifier)
    if not finite(vanillaModifier) or vanillaModifier <= 0 then return nil end
    local factor = REFERENCE_MODIFIER / vanillaModifier
    if factor > MAX_COMPENSATION then return MAX_COMPENSATION end
    return factor
end

-- The single signal the server module acts on: a number to insert, or nil to
-- leave the item out of the table entirely. There is no second "weight zero"
-- path.
function Definitions.insertedWeight(baseWeight, step, vanillaModifier)
    if not finite(baseWeight) or baseWeight <= 0 then return nil end
    local ladder = Definitions.ladderMultiplier(step)
    if ladder <= 0 then return nil end
    local factor = Definitions.compensation(vanillaModifier)
    if factor == nil then return nil end
    local weight = baseWeight * ladder * factor
    if weight > MAX_WEIGHT then return MAX_WEIGHT end
    if weight <= 0 then return nil end
    return weight
end

-- Distribution tables name an item by module, and a bare name silently never
-- resolves.
function Definitions.typeName(itemName)
    if type(itemName) ~= "string" then return nil end
    return MODULE .. itemName
end

return Definitions
