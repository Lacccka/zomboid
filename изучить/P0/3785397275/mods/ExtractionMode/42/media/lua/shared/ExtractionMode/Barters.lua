require "ExtractionMode/Quests"
require "ExtractionMode/Localization"

ExtractionMode = ExtractionMode or {}

local Quests = ExtractionMode.Quests
local Localization = ExtractionMode.Localization
local Barters = {}

local CANNED_FOOD_REQUIREMENT = {
    typePrefixes = { "Base.Canned" },
    excludedTypeFragments = { "Open", "_Box" },
    types = { "Base.TunaTin", "Base.Dogfood", "Base.TinnedBeans", "Base.TinnedSoup" },
}

local CANNED_FOOD_REWARDS = {
    "Base.TinnedBeans", "Base.CannedBolognese", "Base.CannedCarrots2",
    "Base.CannedChili", "Base.CannedCornedBeef", "Base.CannedCorn",
    "Base.CannedFruitCocktail", "Base.CannedFruitBeverage", "Base.CannedMilk",
    "Base.CannedMushroomSoup", "Base.CannedPeaches", "Base.CannedPeas",
    "Base.CannedPineapple", "Base.CannedPotato2", "Base.CannedSardines",
    "Base.TinnedSoup", "Base.CannedTomato2", "Base.TunaTin", "Base.Dogfood",
}

local GENERATORS = {
    "Base.Generator", "Base.Generator_Blue", "Base.Generator_Old", "Base.Generator_Yellow",
}

local WALKIE_TALKIES = {
    "Base.WalkieTalkie1", "Base.WalkieTalkie2", "Base.WalkieTalkie3",
    "Base.WalkieTalkie4", "Base.WalkieTalkie5", "Base.WalkieTalkieMakeShift",
}

-- Trust unlocks these repeatable exchanges but is never spent. Definitions use
-- the same requirement schema as quests and upgrades so nested bags and server-
-- authoritative consumption behave consistently everywhere.
local definitions = {
    {
        id = "layne_bleach_for_bandages", contactId = "dr_layne", requiredTrust = 10,
        offered = { { label = "Bleach", amount = 1, types = { "Base.Bleach" } } },
        received = { { label = "Bandages", amount = 3, fullType = "Base.Bandage" } },
    },
    {
        id = "layne_cans_for_disinfectant", contactId = "dr_layne", requiredTrust = 20,
        offered = { { label = "Random Cans of Food", amount = 2,
            typePrefixes = CANNED_FOOD_REQUIREMENT.typePrefixes,
            excludedTypeFragments = CANNED_FOOD_REQUIREMENT.excludedTypeFragments,
            types = CANNED_FOOD_REQUIREMENT.types } },
        received = { { label = "Bottle of Disinfectant", amount = 1, fullType = "Base.Disinfectant" } },
    },
    {
        id = "layne_garbage_bags_for_painkillers", contactId = "dr_layne", requiredTrust = 20,
        offered = { { label = "Box of Garbage Bags", amount = 1, types = { "Base.Garbagebag_box" } } },
        received = { { label = "Painkillers", amount = 1, fullType = "Base.Pills" } },
    },
    {
        id = "layne_soap_for_antibiotics", contactId = "dr_layne", requiredTrust = 30,
        offered = { { label = "Bars of Soap", amount = 3, types = { "Base.Soap2" } } },
        received = { { label = "Antibiotics", amount = 1, fullType = "Base.Antibiotics" } },
    },
    {
        id = "layne_towels_for_sutures", contactId = "dr_layne", requiredTrust = 30,
        offered = { { label = "Bath Towels", amount = 2, types = { "Base.BathTowel" } } },
        received = {
            { label = "Suture Needle", amount = 1, fullType = "Base.SutureNeedle" },
            { label = "Tweezers", amount = 1, fullType = "Base.Tweezers" },
        },
    },
    {
        id = "layne_sleeping_pills_for_beta_blockers", contactId = "dr_layne", requiredTrust = 40,
        offered = { { label = "Sleeping Pills", amount = 2, types = { "Base.PillsSleepingTablets" } } },
        received = { { label = "Beta Blockers", amount = 1, fullType = "Base.PillsBeta" } },
    },
    {
        id = "layne_generator_for_food", contactId = "dr_layne", requiredTrust = 50,
        offered = { { label = "Generator", amount = 1, types = GENERATORS } },
        received = { { label = "Random Cans of Food", amount = 5, randomTypes = CANNED_FOOD_REWARDS } },
    },

    {
        id = "porch_nails_for_shells", contactId = "franklin_porch", requiredTrust = 10,
        offered = { { label = "Box of Nails", amount = 1, types = { "Base.NailsBox" } } },
        received = { { label = "Box of 12ga Shells", amount = 1, fullType = "Base.ShotgunShellsBox" } },
    },
    {
        id = "porch_screws_for_shovel", contactId = "franklin_porch", requiredTrust = 10,
        offered = { { label = "Box of Screws", amount = 1, types = { "Base.ScrewsBox" } } },
        received = { { label = "Shovel", amount = 1, fullType = "Base.Shovel2" } },
    },
    {
        id = "porch_duct_tape_for_spear", contactId = "franklin_porch", requiredTrust = 20,
        offered = { { label = "Duct Tape", amount = 3, types = { "Base.DuctTape" } } },
        received = { { label = "Spear with Hunting Knife", amount = 1, fullType = "Base.SpearHuntingKnife" } },
    },
    {
        id = "porch_tools_for_sawed_off", contactId = "franklin_porch", requiredTrust = 20,
        offered = {
            { label = "Hammer", amount = 1, types = { "Base.Hammer" } },
            { label = "Saw", amount = 1, types = { "Base.Saw" } },
            { label = "Measuring Tape", amount = 1, types = { "Base.MeasuringTape" } },
        },
        received = { { label = "Sawed-off Shotgun", amount = 1, fullType = "Base.ShotgunSawnoff" } },
    },
    {
        id = "porch_sheets_for_machete", contactId = "franklin_porch", requiredTrust = 30,
        offered = { { label = "Metal Sheets", amount = 3, types = { "Base.SheetMetal" } } },
        received = { { label = "Machete", amount = 1, fullType = "Base.Machete" } },
    },
    {
        id = "porch_duct_tape_for_762", contactId = "franklin_porch", requiredTrust = 40,
        offered = { { label = "Duct Tape", amount = 2, types = { "Base.DuctTape" } } },
        received = { { label = "Box of 7.62x51mm Rounds", amount = 1, fullType = "Base.308Box" } },
    },
    {
        id = "porch_propane_for_msr788", contactId = "franklin_porch", requiredTrust = 40,
        offered = { { label = "Propane Tank (Not Empty)", amount = 1, types = { "Base.PropaneTank" },
            requiresNotEmpty = true } },
        received = { { label = "MSR788 Rifle", amount = 1, fullType = "Base.HuntingRifle" } },
    },
    {
        id = "porch_beer_for_sledgehammer", contactId = "franklin_porch", requiredTrust = 50,
        offered = { { label = "Bottles of Beer", amount = 12,
            types = { "Base.BeerBottle", "Base.BeerImported" } } },
        received = { { label = "Sledgehammer", amount = 1, fullType = "Base.Sledgehammer" } },
    },

    {
        id = "graves_scrap_for_9mm", contactId = "sgt_major_graves", requiredTrust = 10,
        offered = { { label = "Electronics Scrap", amount = 3, types = { "Base.ElectronicsScrap" } } },
        received = { { label = "Box of 9mm Rounds", amount = 1, fullType = "Base.Bullets9mmBox" } },
    },
    {
        id = "graves_wire_for_m9_magazine", contactId = "sgt_major_graves", requiredTrust = 10,
        offered = { { label = "Electrical Wire", amount = 2, types = { "Base.ElectricWire" } } },
        received = { { label = "M9 Magazine", amount = 1, fullType = "Base.9mmClip" } },
    },
    {
        id = "graves_walkie_for_556", contactId = "sgt_major_graves", requiredTrust = 20,
        offered = { { label = "Walkie Talkie", amount = 1, types = WALKIE_TALKIES } },
        received = { { label = "Box of 5.56 Rounds", amount = 1, fullType = "Base.556Box" } },
    },
    {
        id = "graves_gas_cans_for_js14", contactId = "sgt_major_graves", requiredTrust = 20,
        offered = { { label = "Gas Cans", amount = 2, types = { "Base.PetrolCan" } } },
        received = { { label = "JS-14 Rifle", amount = 1, fullType = "Base.JS14_Rifle" } },
    },
    {
        id = "graves_cans_for_js14_magazine", contactId = "sgt_major_graves", requiredTrust = 30,
        offered = { { label = "Cans of Food", amount = 2,
            typePrefixes = CANNED_FOOD_REQUIREMENT.typePrefixes,
            excludedTypeFragments = CANNED_FOOD_REQUIREMENT.excludedTypeFragments,
            types = CANNED_FOOD_REQUIREMENT.types } },
        received = { { label = "JS-14 Magazine", amount = 1, fullType = "Base.JS14_Clip" } },
    },
    {
        id = "graves_scrap_for_m1a", contactId = "sgt_major_graves", requiredTrust = 30,
        offered = { { label = "Electronics Scrap", amount = 10, types = { "Base.ElectronicsScrap" } } },
        received = { { label = "M1A Rifle", amount = 1, fullType = "Base.AssaultRifle2" } },
    },
    {
        id = "graves_transmitter_for_762", contactId = "sgt_major_graves", requiredTrust = 40,
        offered = { { label = "Radio Transmitter", amount = 1, types = { "Base.RadioTransmitter" } } },
        received = { { label = "Box of 7.62x51 Rounds", amount = 1, fullType = "Base.308Box" } },
    },
    {
        id = "graves_batteries_for_m1a_magazine", contactId = "sgt_major_graves", requiredTrust = 40,
        offered = { { label = "Batteries", amount = 3, types = { "Base.Battery" } } },
        received = { { label = "M1A Magazine", amount = 1, fullType = "Base.M14Clip" } },
    },
    {
        id = "graves_generator_for_m16", contactId = "sgt_major_graves", requiredTrust = 50,
        offered = { { label = "Generator", amount = 1, types = GENERATORS } },
        received = { { label = "M16 Assault Rifle", amount = 1, fullType = "Base.AssaultRifle" } },
    },
    {
        id = "graves_wire_for_m16_magazine", contactId = "sgt_major_graves", requiredTrust = 50,
        offered = { { label = "Electrical Wire", amount = 3, types = { "Base.ElectricWire" } } },
        received = { { label = "M16 Magazine", amount = 1, fullType = "Base.556Clip" } },
    },

    {
        id = "mercer_cigarettes_for_45", contactId = "silas_mercer", requiredTrust = 10,
        offered = { { label = "Pack of Cigarettes", amount = 1, types = { "Base.CigarettePack" } } },
        received = { { label = "Box of .45 Rounds", amount = 1, fullType = "Base.Bullets45Box" } },
    },
    {
        id = "mercer_fragrance_for_38", contactId = "silas_mercer", requiredTrust = 10,
        offered = { { label = "Perfume or Cologne", amount = 1,
            types = { "Base.Perfume", "Base.Cologne" } } },
        received = { { label = "Box of .38 Special Rounds", amount = 1, fullType = "Base.Bullets38Box" } },
    },
    {
        id = "mercer_vodka_for_1911", contactId = "silas_mercer", requiredTrust = 20,
        offered = { { label = "Bottles of Vodka", amount = 2, types = { "Base.Vodka" } } },
        received = { { label = "M1911 Pistol", amount = 1, fullType = "Base.Pistol2" } },
    },
    {
        id = "mercer_whiskey_for_sn38", contactId = "silas_mercer", requiredTrust = 20,
        offered = { { label = "Bottle of Whiskey", amount = 1, types = { "Base.Whiskey" } } },
        received = { { label = "SN38 Revolver", amount = 1, fullType = "Base.Revolver_Short" } },
    },
    {
        id = "mercer_bourbon_for_sledgehammer", contactId = "silas_mercer", requiredTrust = 30,
        offered = { { label = "Bottles of Bourbon", amount = 5, types = { "Base.Whiskey" } } },
        received = { { label = "Sledgehammer", amount = 1, fullType = "Base.Sledgehammer" } },
    },
    {
        id = "mercer_vodka_for_1911_magazine", contactId = "silas_mercer", requiredTrust = 30,
        offered = { { label = "Bottle of Vodka", amount = 1, types = { "Base.Vodka" } } },
        received = { { label = "M1911 Magazine", amount = 1, fullType = "Base.45Clip" } },
    },
    {
        id = "mercer_credit_cards_for_desert_eagle", contactId = "silas_mercer", requiredTrust = 40,
        offered = { { label = "Credit Cards", amount = 10,
            types = { "Base.CreditCard", "Base.CreditCard_Stolen" } } },
        received = { { label = "Desert Eagle (Magnum)", amount = 1, fullType = "Base.Pistol3" } },
    },
    {
        id = "mercer_credit_cards_for_44", contactId = "silas_mercer", requiredTrust = 40,
        offered = { { label = "Credit Cards", amount = 2,
            types = { "Base.CreditCard", "Base.CreditCard_Stolen" } } },
        received = { { label = "Box of .44 Rounds", amount = 1, fullType = "Base.Bullets44Box" } },
    },
    {
        id = "mercer_gold_watches_for_katana", contactId = "silas_mercer", requiredTrust = 50,
        offered = { { label = "Gold Wrist Watches", amount = 3,
            types = { "Base.WristWatch_Left_ClassicGold", "Base.WristWatch_Right_ClassicGold" } } },
        received = { { label = "Katana", amount = 1, fullType = "Base.Katana" } },
    },
}

local byId = {}
for _, definition in ipairs(definitions) do
    local prefix = "IGUI_ExtractionMode_Barter_" .. definition.id .. "_"
    definition.requirements = definition.offered
    for index, item in ipairs(definition.offered or {}) do
        item.labelKey = prefix .. "Offered_" .. tostring(index)
    end
    for index, item in ipairs(definition.received or {}) do
        item.labelKey = prefix .. "Received_" .. tostring(index)
    end
    byId[definition.id] = definition
end

function Barters.definitions()
    return definitions
end

function Barters.definition(id)
    return byId[tostring(id or "")]
end

function Barters.label(entry)
    return Localization.field(entry, "label")
end

function Barters.isUnlocked(trust, definition)
    if definition == nil then return false end
    local current = math.max(0, tonumber(trust and trust[definition.contactId]) or 0)
    return current >= math.max(0, tonumber(definition.requiredTrust) or 0)
end

function Barters.unlockedDefinitions(trust)
    local result = {}
    for _, definition in ipairs(definitions) do
        if Barters.isUnlocked(trust, definition) then result[#result + 1] = definition end
    end
    return result
end

function Barters.requirementCount(inventory, requirement)
    return Quests.requirementCount(inventory, requirement)
end

function Barters.requirementsMet(inventory, definition)
    return Quests.requirementsMet(inventory, definition)
end

function Barters.consumeRequirements(inventory, definition)
    return Quests.consumeRequirements(inventory, definition)
end

ExtractionMode.Barters = Barters
return Barters
