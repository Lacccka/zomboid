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
-- All five nutrition items carry ONE weight per table. That is the whole
-- authoring rule, and it exists because a player reads the family as one shelf:
-- finding protein where creatine is impossible, or a gas station that stocks
-- electrolytes but has never heard of protein, reads as a bug rather than as
-- flavour. Measured 2026-08-21 on a 216-mod 42.20.3 server, the old asymmetry
-- was severe -- protein and creatine were unreachable in every gasstore,
-- conveniencestore, cornerstore and zippeestore, while the electrolyte alone
-- sat at 11..18% per container.
--
-- The pre-workout briefly carried three quarters of the staple weight and that
-- was walked back the same day: a live run put it at 16 hits against 24.6
-- expected, and three quarters of a chance already this small reads as "rare"
-- rather than as "slightly rarer". It is a staple like the other four now.
--
-- Take A Bath And Shower authors the same way and was read as calibration
-- (`TABAS_Distributions.lua`, 2026-08-21): one number per table, identical for
-- every item it ships. Its bathroom numbers are 8 and 10 against our 1.2, so a
-- tub of theirs is 4.7% per counter against our 2.9% -- they are denser in the
-- one room they own, and absent everywhere else.
--
-- The weights differ between tables because the DILUTION differs, not because
-- the items do. A container picks one procList in proportion to weightChance
-- and only then rolls, so `grocery > shelves` hands GigamartDryGoods 100 out of
-- a list summing 700 while `grocerystorage > other` hands the three crates the
-- whole 180. Equal weights would mean unequal chances; these numbers were
-- solved backwards from the chance, not chosen by eye.
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
    -- 9.7x.
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
    BathroomCabinet = {
        { "AnabolicPreparation", 0.4 },
        { "CardioPreparation", 0.6 },
    },
    -- The pharmacy shop floor. Only `pharmacy > shelves` draws this table --
    -- nothing else in the game does -- so a course preparation placed here
    -- cannot leak onto a gas station shelf the way it would from
    -- StoreShelfMedical, which gasstore, fossoil, gas2go, conveniencestore and
    -- cornerstore all draw. The weights sit below the ones behind the counter:
    -- at a 15.8% share they read as 1.8% and 2.5% per container against 3.9%
    -- and 5.5% on `pharmacy > counter`, which is a display case being thinner
    -- than the dispensary behind it.
    --
    -- The thermogenic used to sit here and was moved out when the preparations
    -- moved in: a table that carries a course preparation carries nothing else,
    -- and the thermogenic is a training aid by family however medical its
    -- script reads. It loses nothing by the move, because `pharmacy > shelves`
    -- also draws StoreShelfMedical at a 26% share and `pharmacy > other` draws
    -- it whole.
    PharmacyCosmetics = {
        { "AnabolicPreparation", 5 },
        { "CardioPreparation", 7 },
    },

    -- The gym, in every form the map has: the home equipment table, the two
    -- weight-room tables and the locker.
    --
    -- FitnessTrainer is NOT the backbone of residential reach, whatever an
    -- earlier comment here claimed. It stands in livingroom, closet, bedroom
    -- and garagestorage at weightChance 10 against list sums of 765..2665, so
    -- a house container sees 0.02..0.44%. Proven live 2026-08-09: zero hits on
    -- 242 such containers. The residential backbone is the bathroom.
    FitnessTrainer = {
        { "ProteinPowder", 8 },
        { "CreatineComplex", 8 },
        { "CalorieGainer", 8 },
        { "ElectrolyteComplex", 8 },
        { "PreWorkoutStimulant", 8 },
    },
    CrateFitnessWeights = {
        { "ProteinPowder", 6 },
        { "CreatineComplex", 6 },
        { "CalorieGainer", 6 },
        { "ElectrolyteComplex", 6 },
        { "PreWorkoutStimulant", 6 },
    },
    GymWeights = {
        { "ProteinPowder", 8 },
        { "CreatineComplex", 8 },
        { "CalorieGainer", 8 },
        { "ElectrolyteComplex", 8 },
        { "PreWorkoutStimulant", 8 },
    },
    GymLockers = {
        { "ProteinPowder", 8 },
        { "CreatineComplex", 8 },
        { "CalorieGainer", 8 },
        { "ElectrolyteComplex", 8 },
        { "PreWorkoutStimulant", 8 },
        { "ThermogenicComplex", 1.5 },
    },
    -- The densest place in the game for this family, and the only shop floor
    -- that is thematically theirs.
    SportStoreAccessories = {
        { "ProteinPowder", 12 },
        { "CreatineComplex", 12 },
        { "CalorieGainer", 12 },
        { "ElectrolyteComplex", 12 },
        { "PreWorkoutStimulant", 12 },
        { "ThermogenicComplex", 4 },
    },

    -- The residential bathroom, through its own two containers. `counter` draws
    -- BathroomCounter and `shelves` draws BathroomShelf, the two motel and
    -- workplace variants being forceForRooms, so neither weight is diluted and
    -- the number below is the chance. They carry the same numbers because a tub
    -- under the sink and a tub on the shelf are the same tub.
    --
    -- Raised from 1.2 to 2 on 2026-08-21 against Take A Bath And Shower, which
    -- authors the same shelf at 8 and 10 and lands its shampoo at 4.71% per
    -- counter where ours sat at 2.85%. Two reads as 4.71% exactly. The ceiling
    -- is 2.4, the lightest weight any thematic table carries (StoreShelfSnacks
    -- and StoreShelfDrinks), because decision 33 says a house is never the best
    -- place to look and the test below enforces it strictly. The thermogenic
    -- stays at 0.8: its own ceiling is GymLockers at 1.5, and there is no room
    -- to move it without crowding that.
    BathroomCounter = {
        { "ProteinPowder", 2 },
        { "CreatineComplex", 2 },
        { "CalorieGainer", 2 },
        { "ElectrolyteComplex", 2 },
        { "PreWorkoutStimulant", 2 },
        { "ThermogenicComplex", 0.8 },
    },
    BathroomShelf = {
        { "ProteinPowder", 2 },
        { "CreatineComplex", 2 },
        { "CalorieGainer", 2 },
        { "ElectrolyteComplex", 2 },
        { "PreWorkoutStimulant", 2 },
        { "ThermogenicComplex", 0.8 },
    },

    -- The supermarket shop floor. GigamartDryGoods is the single source for
    -- `grocery > shelves` and `gigamart > shelves`, both of which dilute it to
    -- roughly one draw in seven, which is why 12 here reads as 3.7% per
    -- container. GigamartBottles used to carry the electrolyte and the
    -- pre-workout and is now absent entirely: the same shelf already draws this
    -- table, and two sources on one container add up and break the equality the
    -- rule above promises.
    GigamartDryGoods = {
        { "ProteinPowder", 12 },
        { "CreatineComplex", 12 },
        { "CalorieGainer", 12 },
        { "ElectrolyteComplex", 12 },
        { "PreWorkoutStimulant", 12 },
    },
    -- The back room. All three crates carry the same weights because
    -- `grocerystorage > other` draws from them with weightChance 100 / 40 / 40
    -- and nothing else shares the list, so the three together are the whole
    -- draw and 3.6 reads as 8.4% per container -- the densest ordinary place a
    -- player will find this family outside a gym.
    GroceryStorageCrate1 = {
        { "ProteinPowder", 3.6 },
        { "CreatineComplex", 3.6 },
        { "CalorieGainer", 3.6 },
        { "ElectrolyteComplex", 3.6 },
        { "PreWorkoutStimulant", 3.6 },
    },
    GroceryStorageCrate2 = {
        { "ProteinPowder", 3.6 },
        { "CreatineComplex", 3.6 },
        { "CalorieGainer", 3.6 },
        { "ElectrolyteComplex", 3.6 },
        { "PreWorkoutStimulant", 3.6 },
    },
    GroceryStorageCrate3 = {
        { "ProteinPowder", 3.6 },
        { "CreatineComplex", 3.6 },
        { "CalorieGainer", 3.6 },
        { "ElectrolyteComplex", 3.6 },
        { "PreWorkoutStimulant", 3.6 },
    },

    -- Small retail and the gas station. These two tables are the backbone of
    -- gasstore, fossoil, gas2go, conveniencestore, cornerstore and zippeestore,
    -- which draw them at 29..46% each; a shelf that rolls either one is the
    -- only chance those rooms have. Both carry the identical block, so it does
    -- not matter which of the two a given shelf rolled. The weight is small
    -- because the share is large: 2.3 across the pair reads as 3.6% at a gas
    -- station and 4.9% at a convenience store.
    StoreShelfSnacks = {
        { "ProteinPowder", 2.4 },
        { "CreatineComplex", 2.4 },
        { "CalorieGainer", 2.4 },
        { "ElectrolyteComplex", 2.4 },
        { "PreWorkoutStimulant", 2.4 },
    },
    StoreShelfDrinks = {
        { "ProteinPowder", 2.4 },
        { "CreatineComplex", 2.4 },
        { "CalorieGainer", 2.4 },
        { "ElectrolyteComplex", 2.4 },
        { "PreWorkoutStimulant", 2.4 },
    },
    -- The counter behind the till, drawn at weightChance 10 out of 130. A
    -- heavier weight buys very little here, which is why 6 reads as 1.1%.
    GasStoreSpecial = {
        { "ProteinPowder", 6 },
        { "CreatineComplex", 6 },
        { "CalorieGainer", 6 },
        { "ElectrolyteComplex", 6 },
        { "PreWorkoutStimulant", 6 },
    },
    -- The back room of the same shops. Undiluted in cornerstorestorage,
    -- conveniencestore metal shelves and cornerstore crates, and 44% of
    -- gasstorage, so 3.6 spans 3.7..8.4% depending on which of those a player
    -- opened.
    GasStorageCombo = {
        { "ProteinPowder", 3.6 },
        { "CreatineComplex", 3.6 },
        { "CalorieGainer", 3.6 },
        { "ElectrolyteComplex", 3.6 },
        { "PreWorkoutStimulant", 3.6 },
    },

    -- The thermogenic alone. It is a training aid by family but Medical by loot
    -- type, and these are the two retail tables where a fat burner belongs. The
    -- electrolyte used to sit in StoreShelfMedical and the gainer in
    -- GigamartCosmetics; both were removed because the shelves that draw them
    -- already draw a nutrition table, and the doubled source broke equality --
    -- it is also what made the electrolyte read as four times commoner than
    -- anything else in small retail.
    StoreShelfMedical = {
        { "ThermogenicComplex", 3 },
    },
    GigamartCosmetics = {
        { "ThermogenicComplex", 3 },
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
