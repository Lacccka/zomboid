-- Adds selected EFZ items to vanilla loot tables with ultra-rare spawn weights (B41).
-- This lets players loot these items naturally from the world while keeping them extremely rare.

if isClient() then return end

local function safeRequire(path)
    local ok, res = pcall(require, path)
    if ok then
        return res
    end
    return nil
end

local function ensureProceduralLoaded()
    -- ProceduralDistributions.lua references ClutterTables.* which are defined in these files.
    safeRequire("Items/Distribution_BinJunk")
    safeRequire("Items/Distribution_ClosetJunk")
    safeRequire("Items/Distribution_CounterJunk")
    safeRequire("Items/Distribution_DeskJunk")
    safeRequire("Items/Distribution_ShelfJunk")
    safeRequire("Items/Distribution_SideTableJunk")
    safeRequire("Items/ProceduralDistributions")
end

local function getProcedural(listName)
    if not ProceduralDistributions or not ProceduralDistributions.list then
        return nil
    end
    return ProceduralDistributions.list[listName]
end

local function containsItem(items, fullType)
    if type(items) ~= "table" then return false end
    for i = 1, #items, 2 do
        if items[i] == fullType then
            return true
        end
    end
    return false
end

local function addToProcedural(listName, fullType, weight)
    local dist = getProcedural(listName)
    if not dist or type(dist.items) ~= "table" then
        return false
    end

    if containsItem(dist.items, fullType) then
        return false
    end

    table.insert(dist.items, fullType)
    table.insert(dist.items, weight)
    return true
end

local function injectEFZRareLoot()
    EFZ = EFZ or {}
    if EFZ.__rareLootInjected then
        return
    end
    ensureProceduralLoaded()
    if not ProceduralDistributions or not ProceduralDistributions.list then
        -- If vanilla tables aren't available yet, bail without setting the guard
        -- so we can retry later via startup hooks.
        return
    end

    EFZ.__rareLootInjected = true

    local W_ULTRA = 0.4
    local W_VERY = 0.6
    local W_RARE = 0.8

    -- Medical / Drugs
    addToProcedural("BathroomCabinet", "EFZ.InsulinSyringe", W_ULTRA)
    addToProcedural("BathroomCabinet", "EFZ.StrongNarcotics", W_ULTRA)

    addToProcedural("MedicalCabinet", "EFZ.WoundTimeReducer", W_ULTRA)
    addToProcedural("MedicalCabinet", "EFZ.InsulinSyringe", W_VERY)
    addToProcedural("MedicalCabinet", "EFZ.StrongNarcotics", W_ULTRA)
    addToProcedural("MedicalCabinet", "EFZ.WoundTimeReducer", W_RARE)
    addToProcedural("MedicalCabinet", "EFZ.InfectionSuppressorBox", W_ULTRA)

    addToProcedural("MedicalClinicDrugs", "EFZ.FitnessBooster", W_ULTRA)
    addToProcedural("MedicalClinicDrugs", "EFZ.StrengthBooster", W_ULTRA)
    addToProcedural("MedicalClinicDrugs", "EFZ.InsulinSyringe", W_VERY)
    addToProcedural("MedicalClinicDrugs", "EFZ.StrongNarcotics", W_VERY)
    addToProcedural("MedicalClinicDrugs", "EFZ.WoundTimeReducer", W_RARE)
    addToProcedural("MedicalClinicDrugs", "EFZ.InfectionSuppressorBox", W_ULTRA)

    addToProcedural("MedicalClinicTools", "EFZ.WoundTimeReducer", W_RARE)

    addToProcedural("MedicalStorageDrugs", "EFZ.InsulinSyringe", W_VERY)
    addToProcedural("MedicalStorageDrugs", "EFZ.StrongNarcotics", W_VERY)
    addToProcedural("MedicalStorageDrugs", "EFZ.WoundTimeReducer", W_RARE)
    addToProcedural("MedicalStorageDrugs", "EFZ.InfectionSuppressorBox", W_ULTRA)

    addToProcedural("MedicalStorageTools", "EFZ.WoundTimeReducer", W_RARE)

    -- Army
    addToProcedural("ArmyStorageMedical", "EFZ.WoundTimeReducer", W_RARE)
    addToProcedural("ArmyStorageMedical", "EFZ.InsulinSyringe", W_VERY)
    addToProcedural("ArmyStorageMedical", "EFZ.InfectionSuppressorBox", W_ULTRA)
    addToProcedural("ArmyStorageElectronics", "EFZ.CommunicationDevice", W_VERY)

    -- Electronics
    addToProcedural("CrateElectronics", "EFZ.CommunicationDevice", W_VERY)
    addToProcedural("ElectronicStoreHAMRadio", "EFZ.CommunicationDevice", W_VERY)
    addToProcedural("ElectronicStoreMisc", "EFZ.CommunicationDevice", W_VERY)

    -- Camping / Water
    addToProcedural("CampingStoreGear", "EFZ.WaterFilter", W_VERY)
    addToProcedural("CampingStoreTools", "EFZ.WaterFilter", W_VERY)
    addToProcedural("CrateCamping", "EFZ.WaterFilter", W_VERY)
    addToProcedural("ArmySurplusWater", "EFZ.WaterFilter", W_VERY)

    -- Gym / Drugs
    addToProcedural("GymLockers", "EFZ.FitnessBooster", W_ULTRA)
    addToProcedural("GymLockers", "EFZ.StrengthBooster", W_ULTRA)

    addToProcedural("DrugShackDrugs", "EFZ.FitnessBooster", W_ULTRA)
    addToProcedural("DrugShackDrugs", "EFZ.StrengthBooster", W_ULTRA)
    addToProcedural("DrugShackDrugs", "EFZ.StrongNarcotics", W_ULTRA)

    addToProcedural("DrugLabSupplies", "EFZ.StrongNarcotics", W_VERY)
    addToProcedural("DrugLabSupplies", "EFZ.WoundTimeReducer", W_RARE)
    addToProcedural("DerelictHouseDrugs", "EFZ.FitnessBooster", W_ULTRA)
    addToProcedural("DerelictHouseDrugs", "EFZ.StrengthBooster", W_ULTRA)

    -- Media
    addToProcedural("viplounge", "EFZ.DawnOfDeadVideo", W_ULTRA)

    addToProcedural("pawnshop", "EFZ.DawnOfDeadVideo", W_VERY)
    addToProcedural("pawnshop", "EFZ.CommunicationDevice", W_VERY)
    addToProcedural("pawnshop", "EFZ.WaterFilter", W_VERY)
    addToProcedural("pawnshopstorage", "EFZ.DawnOfDeadVideo", W_VERY)
    addToProcedural("pawnshopstorage", "EFZ.CommunicationDevice", W_VERY)
    addToProcedural("pawnshopstorage", "EFZ.WaterFilter", W_VERY)
end

-- Prefer the distribution merge hook so we patch at a safe time during startup.
if Events and Events.OnPreDistributionMerge and Events.OnPreDistributionMerge.Add then
    Events.OnPreDistributionMerge.Add(injectEFZRareLoot)
else
    injectEFZRareLoot()
end


