require "loot/NMFallbackRepresentativeResolver"
require "loot/NMLootDebugHelpers"
require "loot/NMLootDistributionUtils"
require "loot/NMLootSandboxSettings"
require "loot/NMManagedSpawnCatalog"

NMLootRoutePlanner = NMLootRoutePlanner or {}

local planner = NMLootRoutePlanner

local BASE_MEDIA_DEFAULT = NMLootSandboxSettings.BASE_MEDIA_DEFAULT
local BASE_DEVICE_DEFAULT = NMLootSandboxSettings.BASE_DEVICE_DEFAULT
local DEVICE_DIRECT_SPAWN_SCALE_BASE = NMLootSandboxSettings.DEVICE_DIRECT_SPAWN_SCALE_BASE
local CATEGORY_ORDER = NMLootSandboxSettings.CATEGORY_ORDER
local MEDIA_CATEGORY_ORDER = NMLootSandboxSettings.MEDIA_CATEGORY_ORDER
local DEVICE_CATEGORY_ORDER = NMLootSandboxSettings.DEVICE_CATEGORY_ORDER
local VEHICLE_ROLE_ORDER = NMLootSandboxSettings.VEHICLE_ROLE_ORDER

local VEHICLE_ROLE_PRIORITY = {
    glovebox = 1,
    seatrear = 2,
    cargo = 3
}

local VANILLA_MEDIA_BACKFILL_ENTRY_DIVISOR = NMLootSandboxSettings.VANILLA_MEDIA_BACKFILL_ENTRY_DIVISOR
local VANILLA_DEVICE_BACKFILL_ENTRY_DIVISOR = NMLootSandboxSettings.VANILLA_DEVICE_BACKFILL_ENTRY_DIVISOR
local VANILLA_MEDIA_BACKFILL_SCALE = NMLootSandboxSettings.VANILLA_MEDIA_BACKFILL_SCALE
local VANILLA_DEVICE_BACKFILL_SCALE = NMLootSandboxSettings.VANILLA_DEVICE_BACKFILL_SCALE
local VANILLA_BACKFILL_MAX_MULTIPLIER = NMLootSandboxSettings.VANILLA_BACKFILL_MAX_MULTIPLIER
local MUSIC_STORE_MEDIA_MIN_PRESENCE_UNITS = NMLootSandboxSettings.MUSIC_STORE_MEDIA_MIN_PRESENCE_UNITS
local MEDIA_REPRESENTATIVE_PROCEDURAL_LANE_MULTIPLIERS = NMLootSandboxSettings.MEDIA_REPRESENTATIVE_PROCEDURAL_LANE_MULTIPLIERS
local MEDIA_REPRESENTATIVE_MAIL_LANE_MULTIPLIERS = NMLootSandboxSettings.MEDIA_REPRESENTATIVE_MAIL_LANE_MULTIPLIERS
local MUSIC_STORE_TARGET_ORDER = NMLootSandboxSettings.MUSIC_STORE_TARGET_ORDER
local MUSIC_STORE_PROCEDURAL_WEIGHT_OVERRIDES = NMLootSandboxSettings.MUSIC_STORE_PROCEDURAL_WEIGHT_OVERRIDES
local MUSIC_STORE_DEVICE_TOPUP_BIAS = NMLootSandboxSettings.MUSIC_STORE_DEVICE_TOPUP_BIAS
local MUSIC_STORE_TOPUP_REFERENCE_RATE = NMLootSandboxSettings.MUSIC_STORE_TOPUP_REFERENCE_RATE
local MUSIC_STORE_TOPUP_DECAY_EXPONENT = NMLootSandboxSettings.MUSIC_STORE_TOPUP_DECAY_EXPONENT
local MUSIC_STORE_TOPUP_TARGET_FLOORS = NMLootSandboxSettings.MUSIC_STORE_TOPUP_TARGET_FLOORS

local LOOT_ROUTE_KEYS = NMLootDebugHelpers and NMLootDebugHelpers.getRouteKeys and NMLootDebugHelpers.getRouteKeys() or {
    standard = "standard",
    globalBackfill = "globalBackfill",
    storeTopUp = "storeTopUp"
}
local LOOT_RESULT_KEYS = NMLootDebugHelpers and NMLootDebugHelpers.getResultKeys and NMLootDebugHelpers.getResultKeys() or {
    inserted = "inserted",
    increased = "increased",
    alreadyPresent = "already_present",
    replaced = "replaced",
    skipped = "skipped",
    rejected = "rejected"
}

local clamp = NMLootSandboxSettings.clamp
local pow = NMLootSandboxSettings.pow
local resolveCategoryRate = NMLootSandboxSettings.resolveCategoryRate
local resolveDirectSpawnScalar = NMLootSandboxSettings.resolveDirectSpawnScalar
local resolveStandardDeviceScalar = NMLootSandboxSettings.resolveStandardDeviceScalar
local resolveMediaFallbackBudgetScalar = NMLootSandboxSettings.resolveMediaFallbackBudgetScalar
local isMediaCategory = NMLootSandboxSettings.isMediaCategory
local resolveStandardMediaProceduralAbundanceBoost = NMLootSandboxSettings.resolveStandardMediaProceduralAbundanceBoost
local resolveStandardMediaVehicleAbundanceBoost = NMLootSandboxSettings.resolveStandardMediaVehicleAbundanceBoost
local resolveMediaStoreTopUpBudgetBoost = NMLootSandboxSettings.resolveMediaStoreTopUpBudgetBoost
local normalizePlayableRate = NMLootSandboxSettings.normalizePlayableRate

local addItemIfMissingCached = NMLootDistributionUtils.addItemIfMissingCached
local addOrIncreaseItemWeight = NMLootDistributionUtils.addOrIncreaseItemWeight
local addOrIncreaseItemWeightWithResult = NMLootDistributionUtils.addOrIncreaseItemWeightWithResult
local injectIntoProcedural = NMLootDistributionUtils.injectIntoProcedural
local injectIntoProceduralAdditive = NMLootDistributionUtils.injectIntoProceduralAdditive
local injectIntoSuburbsTopLevel = NMLootDistributionUtils.injectIntoSuburbsTopLevel
local injectIntoSuburbsTopLevelAdditive = NMLootDistributionUtils.injectIntoSuburbsTopLevelAdditive

local ACTIVE_STORE_TOPUP_MEDIA_POOL = nil

local VEHICLE_ROLE_WEIGHT_CURVES = {
    cassettes = {
        glovebox = { low = 30.00, high = 40.00 },
        seatrear = { low = 0.20, high = 0.90 },
        cargo = { low = 0.24, high = 1.80 }
    },
    vinyl = {
        glovebox = { low = 0.0, high = 0.0 },
        seatrear = { low = 0.0, high = 0.0 },
        cargo = { low = 0.10, high = 1.30 }
    },
    cds = {
        glovebox = { low = 1.20, high = 2.40 },
        seatrear = { low = 0.22, high = 0.95 },
        cargo = { low = 0.22, high = 1.60 }
    },
    walkman = {
        glovebox = { low = 0.10, high = 1.00 },
        seatrear = { low = 0.05, high = 0.55 },
        cargo = { low = 0.08, high = 0.85 }
    },
    boombox = {
        glovebox = { low = 0.0, high = 0.0 },
        seatrear = { low = 0.0, high = 0.0 },
        cargo = { low = 0.08, high = 0.75 }
    },
    cdplayer = {
        glovebox = { low = 0.10, high = 0.95 },
        seatrear = { low = 0.05, high = 0.50 },
        cargo = { low = 0.08, high = 0.80 }
    },
    recordplayer = {
        glovebox = { low = 0.0, high = 0.0 },
        seatrear = { low = 0.0, high = 0.0 },
        cargo = { low = 0.045, high = 0.34 }
    }
}

local MEDIA_PROCEDURAL_TARGETS = {
    cassettes = {
        { name = "MusicStoreCDs", weight = 18.0 }, { name = "MusicStoreShelves", weight = 12.0 },
        { name = "MusicStoreCounter", weight = 10.0 }, { name = "MusicStoreSpeaker", weight = 10.0 },
        { name = "MusicStoreOthers", weight = 8.0 }, { name = "ElectronicStoreMusic", weight = 6.0 },
        { name = "CrateElectronics", weight = 4.0 }, { name = "ElectronicStoreCases", weight = 4.0 },
        { name = "ElectronicStoreMisc", weight = 3.0 }, { name = "GigamartHouseElectronics", weight = 2.0 },
        { name = "CrateCompactDiscs", weight = 6.0 }, { name = "BookstoreMusic", weight = 2.0 },
        { name = "LibraryMusic", weight = 1.5 }, { name = "UniversityLibraryMusic", weight = 1.5 },
        { name = "RecRoomShelf", weight = 1.5 }, { name = "SchoolLockers", weight = 6.0 },
        { name = "SchoolLockersBad", weight = 6.0 }, { name = "SchoolDesk", weight = 3.0 },
        { name = "LivingRoomShelf", weight = 4.0 }, { name = "LivingRoomShelfClassy", weight = 4.0 },
        { name = "LivingRoomShelfRedneck", weight = 4.0 }, { name = "LivingRoomShelfNoTapes", weight = 3.0 },
        { name = "LivingRoomSideTable", weight = 2.5 }, { name = "LivingRoomCabinet", weight = 2.5 },
        { name = "BedroomDresser", weight = 3.0 }, { name = "BedroomDresserClassy", weight = 3.0 },
        { name = "StoreShelfCombo", weight = 1.5 }
    },
    vinyl = {
        { name = "MusicStoreCases", weight = 1080.0 }, { name = "MusicStoreCDs", weight = 720.0 },
        { name = "MusicStoreShelves", weight = 960.0 }, { name = "MusicStoreCounter", weight = 560.0 },
        { name = "MusicStoreSpeaker", weight = 600.0 }, { name = "ElectronicStoreMusic", weight = 5.0 },
        { name = "CrateElectronics", weight = 3.0 }, { name = "ElectronicStoreCases", weight = 3.0 },
        { name = "ElectronicStoreMisc", weight = 2.0 }, { name = "GigamartHouseElectronics", weight = 1.5 },
        { name = "CrateCompactDiscs", weight = 4.0 }, { name = "BookstoreMusic", weight = 1.5 },
        { name = "LibraryMusic", weight = 1.0 }, { name = "UniversityLibraryMusic", weight = 1.0 },
        { name = "RecRoomShelf", weight = 1.0 }, { name = "SchoolLockers", weight = 1.0 },
        { name = "SchoolLockersBad", weight = 1.0 }, { name = "LivingRoomShelf", weight = 3.0 },
        { name = "LivingRoomShelfClassy", weight = 3.0 }, { name = "LivingRoomShelfRedneck", weight = 2.0 },
        { name = "LivingRoomSideTable", weight = 1.5 }, { name = "LivingRoomCabinet", weight = 2.0 },
        { name = "BedroomDresser", weight = 2.0 }, { name = "StoreShelfCombo", weight = 1.0 }
    },
    cds = {
        { name = "MusicStoreCDs", weight = 18.0 }, { name = "MusicStoreShelves", weight = 12.0 },
        { name = "MusicStoreCounter", weight = 10.0 }, { name = "MusicStoreSpeaker", weight = 10.0 },
        { name = "MusicStoreOthers", weight = 8.0 }, { name = "ElectronicStoreMusic", weight = 6.0 },
        { name = "CrateElectronics", weight = 4.0 }, { name = "ElectronicStoreCases", weight = 4.0 },
        { name = "ElectronicStoreMisc", weight = 3.0 }, { name = "GigamartHouseElectronics", weight = 2.0 },
        { name = "CrateCompactDiscs", weight = 6.0 }, { name = "BookstoreMusic", weight = 2.0 },
        { name = "LibraryMusic", weight = 1.5 }, { name = "UniversityLibraryMusic", weight = 1.5 },
        { name = "RecRoomShelf", weight = 1.5 }, { name = "SchoolLockers", weight = 6.0 },
        { name = "SchoolLockersBad", weight = 6.0 }, { name = "SchoolDesk", weight = 3.0 },
        { name = "LivingRoomShelf", weight = 6.0 }, { name = "LivingRoomShelfClassy", weight = 6.0 },
        { name = "LivingRoomShelfRedneck", weight = 4.0 }, { name = "LivingRoomShelfNoTapes", weight = 3.0 },
        { name = "LivingRoomSideTable", weight = 2.5 }, { name = "LivingRoomCabinet", weight = 3.0 },
        { name = "BedroomDresser", weight = 3.0 }, { name = "BedroomDresserClassy", weight = 3.0 },
        { name = "StoreShelfCombo", weight = 1.5 }
    }
}

local DEVICE_PROCEDURAL_TARGETS = {
    walkman = {
        { name = "MusicStoreCDs", weight = 6.0 }, { name = "MusicStoreShelves", weight = 10.0 },
        { name = "MusicStoreCounter", weight = 6.0 }, { name = "MusicStoreOthers", weight = 5.0 },
        { name = "ElectronicStoreMusic", weight = 4.0 }, { name = "ElectronicStoreSpeaker", weight = 4.0 },
        { name = "CrateElectronics", weight = 4.0 }, { name = "ElectronicStoreCases", weight = 2.0 },
        { name = "ElectronicStoreMisc", weight = 2.0 }, { name = "GigamartHouseElectronics", weight = 1.0 },
        { name = "StoreShelfElectronics", weight = 1.5 }, { name = "BookstoreMusic", weight = 1.0 },
        { name = "LibraryMusic", weight = 0.5 }, { name = "UniversityLibraryMusic", weight = 0.5 },
        { name = "RecRoomShelf", weight = 0.5 }, { name = "SchoolLockers", weight = 3.0 },
        { name = "SchoolLockersBad", weight = 3.0 }, { name = "SchoolDesk", weight = 1.5 },
        { name = "LivingRoomShelf", weight = 3.0 }, { name = "LivingRoomShelfClassy", weight = 3.0 },
        { name = "LivingRoomShelfRedneck", weight = 3.0 }, { name = "LivingRoomShelfNoTapes", weight = 2.0 },
        { name = "LivingRoomSideTable", weight = 1.5 }, { name = "LivingRoomCabinet", weight = 1.5 },
        { name = "BedroomDresser", weight = 3.0 }, { name = "BedroomDresserClassy", weight = 3.0 },
        { name = "StoreShelfCombo", weight = 0.5 }
    },
    boombox = {
        { name = "MusicStoreCDs", weight = 4.0 }, { name = "MusicStoreShelves", weight = 7.0 },
        { name = "MusicStoreCounter", weight = 5.0 }, { name = "MusicStoreOthers", weight = 4.0 },
        { name = "ElectronicStoreMusic", weight = 3.0 }, { name = "ElectronicStoreSpeaker", weight = 3.0 },
        { name = "CrateElectronics", weight = 3.0 }, { name = "ElectronicStoreCases", weight = 1.5 },
        { name = "ElectronicStoreMisc", weight = 1.5 }, { name = "GigamartHouseElectronics", weight = 0.75 },
        { name = "StoreShelfElectronics", weight = 1.0 }, { name = "BookstoreMusic", weight = 0.5 },
        { name = "LibraryMusic", weight = 0.25 }, { name = "UniversityLibraryMusic", weight = 0.25 },
        { name = "RecRoomShelf", weight = 0.25 }, { name = "SchoolLockers", weight = 1.5 },
        { name = "SchoolLockersBad", weight = 1.5 }, { name = "LivingRoomShelf", weight = 3.0 },
        { name = "LivingRoomShelfClassy", weight = 3.0 }, { name = "LivingRoomShelfRedneck", weight = 3.0 },
        { name = "LivingRoomSideTable", weight = 0.75 }, { name = "LivingRoomCabinet", weight = 1.0 },
        { name = "StoreShelfCombo", weight = 0.25 }
    },
    cdplayer = {
        { name = "MusicStoreCDs", weight = 6.0 }, { name = "MusicStoreShelves", weight = 9.0 },
        { name = "MusicStoreCounter", weight = 6.0 }, { name = "ElectronicStoreMusic", weight = 5.0 },
        { name = "ElectronicStoreSpeaker", weight = 4.0 }, { name = "CrateElectronics", weight = 5.0 },
        { name = "ElectronicStoreCases", weight = 3.0 }, { name = "ElectronicStoreMisc", weight = 3.0 },
        { name = "GigamartHouseElectronics", weight = 1.5 }, { name = "StoreShelfElectronics", weight = 2.0 },
        { name = "BookstoreMusic", weight = 1.0 }, { name = "LibraryMusic", weight = 0.75 },
        { name = "UniversityLibraryMusic", weight = 0.75 }, { name = "RecRoomShelf", weight = 0.75 },
        { name = "SchoolLockers", weight = 4.0 }, { name = "SchoolLockersBad", weight = 4.0 },
        { name = "SchoolDesk", weight = 2.0 }, { name = "LivingRoomShelf", weight = 2.0 },
        { name = "LivingRoomShelfClassy", weight = 2.0 }, { name = "LivingRoomShelfRedneck", weight = 2.0 },
        { name = "LivingRoomShelfNoTapes", weight = 1.5 }, { name = "LivingRoomSideTable", weight = 1.0 },
        { name = "LivingRoomCabinet", weight = 1.0 }, { name = "BedroomDresser", weight = 1.0 },
        { name = "BedroomDresserClassy", weight = 1.0 }, { name = "StoreShelfCombo", weight = 0.5 }
    },
    recordplayer = {
        { name = "MusicStoreCDs", weight = 8.0 }, { name = "MusicStoreSpeaker", weight = 7.0 },
        { name = "MusicStoreShelves", weight = 12.0 }, { name = "MusicStoreCounter", weight = 8.0 },
        { name = "MusicStoreOthers", weight = 5.0 }, { name = "ElectronicStoreMusic", weight = 3.5 },
        { name = "CrateElectronics", weight = 3.0 }, { name = "ElectronicStoreCases", weight = 1.75 },
        { name = "ElectronicStoreMisc", weight = 1.5 }, { name = "GigamartHouseElectronics", weight = 1.25 },
        { name = "StoreShelfElectronics", weight = 1.75 }, { name = "BookstoreMusic", weight = 0.5 },
        { name = "LibraryMusic", weight = 0.35 }, { name = "UniversityLibraryMusic", weight = 0.35 },
        { name = "RecRoomShelf", weight = 0.35 }, { name = "LivingRoomShelfClassy", weight = 2.25 },
        { name = "LivingRoomShelf", weight = 1.5 }, { name = "LivingRoomShelfRedneck", weight = 0.9 },
        { name = "LivingRoomShelfNoTapes", weight = 0.75 }, { name = "LivingRoomSideTable", weight = 0.75 },
        { name = "LivingRoomCabinet", weight = 1.5 }, { name = "BedroomDresser", weight = 1.0 },
        { name = "BedroomDresserClassy", weight = 1.25 }, { name = "StoreShelfCombo", weight = 0.35 }
    }
}

local MEDIA_MAIL_TARGETS = {
    cassettes = {
        { kind = "suburbs", name = "Bag_Mail", weight = 0.003 },
        { kind = "suburbs", name = "Bag_Satchel_Mail", weight = 0.003 },
        { kind = "procedural", name = "PostOfficeParcels", weight = 0.01 }
    },
    vinyl = {
        { kind = "procedural", name = "PostOfficeParcels", weight = 0.004 }
    },
    cds = {
        { kind = "suburbs", name = "Bag_Mail", weight = 0.003 },
        { kind = "suburbs", name = "Bag_Satchel_Mail", weight = 0.003 },
        { kind = "procedural", name = "PostOfficeParcels", weight = 0.01 }
    }
}

local DEVICE_MAIL_TARGETS = {
    walkman = { { kind = "procedural", name = "PostOfficeParcels", weight = 0.008 } },
    boombox = { { kind = "procedural", name = "PostOfficeParcels", weight = 0.003 } },
    cdplayer = { { kind = "procedural", name = "PostOfficeParcels", weight = 0.008 } },
    recordplayer = { { kind = "procedural", name = "PostOfficeParcels", weight = 0.0025 } }
}

local function orderedMediaUnitsForCategory(pool, category)
    return NMManagedSpawnCatalog.orderedMediaUnitsForCategory({ media = pool or {} }, category)
end

local function orderedDeviceUnitsForCategory(pool, category)
    return NMManagedSpawnCatalog.orderedDeviceUnitsForCategory({ devices = pool or {} }, category)
end

local function countUniqueVehicleTargetsByRoleMap(vehicleTargets)
    local grouped = { glovebox = {}, seatrear = {}, cargo = {} }
    local uniqueByItems = {}

    for i = 1, #(vehicleTargets or {}) do
        local target = vehicleTargets[i]
        local items = target and target.items or nil
        local role = tostring(target and target.role or "")
        if type(items) == "table" and grouped[role] then
            local uniqueTarget = uniqueByItems[items]
            if not uniqueTarget then
                uniqueTarget = { items = items, role = role, roles = {} }
                uniqueByItems[items] = uniqueTarget
            end
            uniqueTarget.roles[role] = true
            if (tonumber(VEHICLE_ROLE_PRIORITY[role]) or 999) < (tonumber(VEHICLE_ROLE_PRIORITY[uniqueTarget.role]) or 999) then
                uniqueTarget.role = role
            end
        end
    end

    for _, target in pairs(uniqueByItems) do
        grouped[target.role][#grouped[target.role] + 1] = target
    end

    return grouped
end

local function newInjectionSummary()
    local roles = {}
    for i = 1, #VEHICLE_ROLE_ORDER do
        roles[VEHICLE_ROLE_ORDER[i]] = 0
    end
    return {
        total = 0,
        byCategory = {},
        byRole = roles,
        bySource = {
            standard = { total = 0, byCategory = {} },
            globalBackfill = { total = 0, byCategory = {} },
            storeTopUp = { total = 0, byCategory = {} }
        }
    }
end

local function ensureCategoryDiagnostics(diag, category)
    diag[category] = diag[category] or {
        poolSize = 0,
        budget = 0,
        rate = 0,
        multiplier = 0,
        share = 0,
        rawWeight = 0,
        laneAmplifier = 1.0,
        estimatedWeight = 0,
        laneWeight = { procedural = 0, vehicle = 0, mail = 0 },
        injectedEntries = { procedural = 0, vehicle = 0, mail = 0 },
        eligibleTargets = { procedural = 0, vehicle = 0, mail = 0 },
        averageTargetWeight = { procedural = 0, vehicle = 0, mail = 0 },
        averageEntryWeight = { procedural = 0, vehicle = 0, mail = 0 },
        resultCounts = { procedural = {}, vehicle = {}, mail = {} },
        selectedUnits = {},
        strongProceduralAssigned = {},
        assignmentCounts = {}
    }
    return diag[category]
end

local function recordInjected(summary, category, role, source)
    if type(summary) ~= "table" then
        return
    end
    local sourceKey = tostring(source or LOOT_ROUTE_KEYS.standard)
    summary.total = (tonumber(summary.total) or 0) + 1
    if category then
        summary.byCategory[category] = (tonumber(summary.byCategory[category]) or 0) + 1
    end
    if role then
        summary.byRole[role] = (tonumber(summary.byRole[role]) or 0) + 1
    end
    summary.bySource[sourceKey] = summary.bySource[sourceKey] or { total = 0, byCategory = {} }
    summary.bySource[sourceKey].total = (tonumber(summary.bySource[sourceKey].total) or 0) + 1
    if category then
        summary.bySource[sourceKey].byCategory[category] = (tonumber(summary.bySource[sourceKey].byCategory[category]) or 0) + 1
    end
end

local function recordMutationResult(categoryDiag, lane, resultKey)
    categoryDiag.resultCounts[lane] = categoryDiag.resultCounts[lane] or {}
    categoryDiag.resultCounts[lane][resultKey] = (tonumber(categoryDiag.resultCounts[lane][resultKey]) or 0) + 1
end

local function recordInjectedEntry(categoryDiag, lane)
    categoryDiag.injectedEntries[lane] = (tonumber(categoryDiag.injectedEntries[lane]) or 0) + 1
end

local function recordSelectedUnits(categoryDiag, picks)
    for i = 1, #(picks or {}) do
        local unit = picks[i]
        local key = tostring(unit and (unit.key or unit.canonical or unit.spawnFullType) or "")
        if key ~= "" then
            categoryDiag.selectedUnits[key] = true
            categoryDiag.assignmentCounts[key] = (tonumber(categoryDiag.assignmentCounts[key]) or 0) + 1
        end
    end
end

local function isMusicStoreTarget(listName)
    local name = tostring(listName or "")
    return string.sub(name, 1, #"MusicStore") == "MusicStore"
end

local function resolveProceduralTargetBaseWeight(category, listName, defaultWeight)
    if not isMusicStoreTarget(listName) then
        return tonumber(defaultWeight) or 0
    end
    local overrides = MUSIC_STORE_PROCEDURAL_WEIGHT_OVERRIDES[tostring(category or "")]
    if type(overrides) == "table" and overrides[listName] ~= nil then
        return tonumber(overrides[listName]) or 0
    end
    return tonumber(defaultWeight) or 0
end

local function countCategoryMusicStoreTargets(category, targets)
    local count = 0
    local overrides = MUSIC_STORE_PROCEDURAL_WEIGHT_OVERRIDES[tostring(category or "")]
    for i = 1, #(targets or {}) do
        local name = tostring(targets[i] and targets[i].name or "")
        if isMusicStoreTarget(name) and ((type(overrides) == "table" and overrides[name] ~= nil) or name ~= "") then
            count = count + 1
        end
    end
    return count
end

local function resolveStoreTopUpFloorStrength(category, rate)
    local normalized = clamp((tonumber(rate) or 0) / MUSIC_STORE_TOPUP_REFERENCE_RATE, 0.0, 1.0)
    if normalized <= 0 then
        return 0
    end
    return pow(normalized, MUSIC_STORE_TOPUP_DECAY_EXPONENT)
end

local function resolveMusicStoreMinimumTargetBudget(category, rate, targets, source)
    if tostring(source or "") ~= LOOT_ROUTE_KEYS.storeTopUp then
        return 0
    end
    if countCategoryMusicStoreTargets(category, targets) < 1 then
        return 0
    end
    if isMediaCategory(category) then
        local poolSize = 0
        if type(ACTIVE_STORE_TOPUP_MEDIA_POOL) == "table" then
            poolSize = #orderedMediaUnitsForCategory(ACTIVE_STORE_TOPUP_MEDIA_POOL, category)
        end
        local unitFloor = tonumber(MUSIC_STORE_MEDIA_MIN_PRESENCE_UNITS[tostring(category or "")]) or 0
        if poolSize <= 0 or unitFloor <= 0 then
            return 0
        end
        return math.min(poolSize, unitFloor) * resolveStoreTopUpFloorStrength(category, rate)
    end
    local baseFloor = tonumber(MUSIC_STORE_TOPUP_TARGET_FLOORS[tostring(category or "")]) or 0
    if baseFloor <= 0 then
        return 0
    end
    return baseFloor * resolveStoreTopUpFloorStrength(category, rate)
end

local function resolveProceduralNormalizationCount(category, listName, unitCount)
    local count = math.max(1, tonumber(unitCount) or 1)
    if not isMusicStoreTarget(listName) then
        return count
    end
    if category == "vinyl" then
        return math.min(count, 8)
    end
    if category == "recordplayer" then
        return math.min(count, 3)
    end
    return count
end

local function resolveRepresentativeLaneMultiplier(category, lane)
    local key = tostring(category or "")
    if lane == "procedural" then
        return tonumber(MEDIA_REPRESENTATIVE_PROCEDURAL_LANE_MULTIPLIERS[key]) or 1.0
    end
    if lane == "mail" then
        return tonumber(MEDIA_REPRESENTATIVE_MAIL_LANE_MULTIPLIERS[key]) or 1.0
    end
    return 1.0
end

local function resolveVehicleRoleTargetWeight(category, role, rate)
    local curve = VEHICLE_ROLE_WEIGHT_CURVES[category] and VEHICLE_ROLE_WEIGHT_CURVES[category][role] or nil
    if not curve then
        return 0
    end
    local low = tonumber(curve.low) or 0
    local high = tonumber(curve.high) or low
    if high <= 0 then
        return 0
    end
    return low + ((high - low) * normalizePlayableRate(rate))
end

local function computeUnifiedMediaCategoryShares(mediaPool)
    return NMLootSandboxSettings.computeUnifiedMediaCategoryShares(mediaPool, orderedMediaUnitsForCategory)
end

local function computeUnifiedDeviceCategoryShares(devicePool)
    return NMLootSandboxSettings.computeUnifiedDeviceCategoryShares(devicePool, orderedDeviceUnitsForCategory)
end

local function computeMusicStoreMediaShares(mediaPool)
    return NMLootSandboxSettings.computeMusicStoreMediaShares(mediaPool, orderedMediaUnitsForCategory)
end

local function computeTotalMediaBudget(mediaPool, scalarResolver)
    return NMLootSandboxSettings.computeTotalMediaBudget(mediaPool, orderedMediaUnitsForCategory, scalarResolver)
end

local function injectUnitsIntoProceduralBudget(category, units, targets, effectiveBudget, summary, diagnostics, indexCache, source)
    if #units < 1 or effectiveBudget <= 0 then
        return
    end
    local categoryDiag = ensureCategoryDiagnostics(diagnostics, category)
    local activeTargets = targets or {}
    categoryDiag.eligibleTargets.procedural = #activeTargets
    if #activeTargets < 1 then
        return
    end

    local storeTargetFloor = resolveMusicStoreMinimumTargetBudget(category, resolveCategoryRate(category), activeTargets, source)
    local placeholderFullType = source ~= LOOT_ROUTE_KEYS.standard
        and NMFallbackRepresentativeResolver
        and NMFallbackRepresentativeResolver.getDevicePlaceholderFullType
        and NMFallbackRepresentativeResolver.getDevicePlaceholderFullType(category, source)
        or nil
    for i = 1, #activeTargets do
        local cfg = activeTargets[i]
        local baseWeight = resolveProceduralTargetBaseWeight(category, cfg.name, cfg.weight)
        local targetBudget = baseWeight * effectiveBudget
        if isMusicStoreTarget(cfg.name) and storeTargetFloor > 0 then
            targetBudget = math.max(targetBudget, storeTargetFloor)
        end
        recordSelectedUnits(categoryDiag, units)
        categoryDiag.estimatedWeight = categoryDiag.estimatedWeight + targetBudget
        categoryDiag.laneWeight.procedural = categoryDiag.laneWeight.procedural + targetBudget
        if tonumber(cfg.weight) and tonumber(cfg.weight) >= 0.8 then
            for j = 1, #units do
                local unit = units[j]
                local key = tostring(unit and (unit.key or unit.canonical or unit.spawnFullType) or "")
                if key ~= "" then
                    categoryDiag.strongProceduralAssigned[key] = true
                end
            end
        end
        if placeholderFullType and targetBudget > 0 then
            local list = ProceduralDistributions and ProceduralDistributions.list and ProceduralDistributions.list[cfg.name] or nil
            local resultKey = addOrIncreaseItemWeightWithResult(list and list.items or nil, placeholderFullType, targetBudget, indexCache)
            recordMutationResult(categoryDiag, "procedural", resultKey)
            if resultKey == LOOT_RESULT_KEYS.inserted or resultKey == LOOT_RESULT_KEYS.increased then
                recordInjected(summary, category, nil, source)
                recordInjectedEntry(categoryDiag, "procedural")
            end
        else
            local normalizationCount = resolveProceduralNormalizationCount(category, cfg.name, #units)
            local perUnitWeight = targetBudget / math.max(1, normalizationCount)
            for j = 1, #units do
                local spawnFullType = tostring(units[j].spawnFullType or "")
                local resultKey = LOOT_RESULT_KEYS.skipped
                if spawnFullType ~= "" and perUnitWeight > 0 then
                    local list = ProceduralDistributions and ProceduralDistributions.list and ProceduralDistributions.list[cfg.name] or nil
                    resultKey = addOrIncreaseItemWeightWithResult(list and list.items or nil, spawnFullType, perUnitWeight, indexCache)
                end
                recordMutationResult(categoryDiag, "procedural", resultKey)
                if resultKey == LOOT_RESULT_KEYS.inserted or resultKey == LOOT_RESULT_KEYS.increased then
                    recordInjected(summary, category, nil, source)
                    recordInjectedEntry(categoryDiag, "procedural")
                end
            end
        end
    end
    categoryDiag.averageTargetWeight.procedural = categoryDiag.laneWeight.procedural / math.max(1, categoryDiag.eligibleTargets.procedural)
    categoryDiag.averageEntryWeight.procedural = categoryDiag.laneWeight.procedural / math.max(1, tonumber(categoryDiag.injectedEntries.procedural) or 0)
end

local function injectUnitsIntoVehicleBudget(category, units, groupedVehicleTargets, effectiveBudget, summary, diagnostics, indexCache, source)
    if #units < 1 or effectiveBudget <= 0 then
        return
    end
    local rate = resolveCategoryRate(category)
    local categoryDiag = ensureCategoryDiagnostics(diagnostics, category)
    local totalTargets = 0
    for i = 1, #VEHICLE_ROLE_ORDER do
        totalTargets = totalTargets + #(groupedVehicleTargets[VEHICLE_ROLE_ORDER[i]] or {})
    end
    categoryDiag.eligibleTargets.vehicle = totalTargets
    if totalTargets < 1 then
        return
    end
    local placeholderFullType = source ~= LOOT_ROUTE_KEYS.standard
        and NMFallbackRepresentativeResolver
        and NMFallbackRepresentativeResolver.getDevicePlaceholderFullType
        and NMFallbackRepresentativeResolver.getDevicePlaceholderFullType(category, source)
        or nil

    for i = 1, #VEHICLE_ROLE_ORDER do
        local role = VEHICLE_ROLE_ORDER[i]
        local targets = groupedVehicleTargets[role] or {}
        local perTargetWeight = resolveVehicleRoleTargetWeight(category, role, rate) * effectiveBudget
        if perTargetWeight > 0 then
            for j = 1, #targets do
                recordSelectedUnits(categoryDiag, units)
                if placeholderFullType then
                    local resultKey = addOrIncreaseItemWeightWithResult(targets[j].items, placeholderFullType, perTargetWeight, indexCache)
                    recordMutationResult(categoryDiag, "vehicle", resultKey)
                    if resultKey == LOOT_RESULT_KEYS.inserted or resultKey == LOOT_RESULT_KEYS.increased then
                        recordInjected(summary, category, role, source)
                        recordInjectedEntry(categoryDiag, "vehicle")
                    end
                else
                    local perUnitWeight = perTargetWeight / math.max(1, #units)
                    for k = 1, #units do
                        local spawnFullType = tostring(units[k].spawnFullType or "")
                        local resultKey = LOOT_RESULT_KEYS.skipped
                        if spawnFullType ~= "" and perUnitWeight > 0 then
                            resultKey = addOrIncreaseItemWeightWithResult(targets[j].items, spawnFullType, perUnitWeight, indexCache)
                        end
                        recordMutationResult(categoryDiag, "vehicle", resultKey)
                        if resultKey == LOOT_RESULT_KEYS.inserted or resultKey == LOOT_RESULT_KEYS.increased then
                            recordInjected(summary, category, role, source)
                            recordInjectedEntry(categoryDiag, "vehicle")
                        end
                    end
                end
                categoryDiag.estimatedWeight = categoryDiag.estimatedWeight + perTargetWeight
                categoryDiag.laneWeight.vehicle = categoryDiag.laneWeight.vehicle + perTargetWeight
            end
        end
    end
    categoryDiag.averageTargetWeight.vehicle = categoryDiag.laneWeight.vehicle / math.max(1, categoryDiag.eligibleTargets.vehicle)
    categoryDiag.averageEntryWeight.vehicle = categoryDiag.laneWeight.vehicle / math.max(1, tonumber(categoryDiag.injectedEntries.vehicle) or 0)
end

local function injectUnitsIntoMailBudget(category, units, targets, effectiveBudget, summary, diagnostics, indexCache, source)
    if #units < 1 or effectiveBudget <= 0 then
        return
    end
    local categoryDiag = ensureCategoryDiagnostics(diagnostics, category)
    categoryDiag.eligibleTargets.mail = #(targets or {})
    if categoryDiag.eligibleTargets.mail < 1 then
        return
    end
    local placeholderFullType = source ~= LOOT_ROUTE_KEYS.standard
        and NMFallbackRepresentativeResolver
        and NMFallbackRepresentativeResolver.getDevicePlaceholderFullType
        and NMFallbackRepresentativeResolver.getDevicePlaceholderFullType(category, source)
        or nil

    for i = 1, #targets do
        local cfg = targets[i]
        local targetBudget = (tonumber(cfg.weight) or 0) * effectiveBudget
        recordSelectedUnits(categoryDiag, units)
        categoryDiag.estimatedWeight = categoryDiag.estimatedWeight + targetBudget
        categoryDiag.laneWeight.mail = categoryDiag.laneWeight.mail + targetBudget
        if placeholderFullType and targetBudget > 0 then
            local resultKey = LOOT_RESULT_KEYS.skipped
            if cfg.kind == "procedural" then
                local list = ProceduralDistributions and ProceduralDistributions.list and ProceduralDistributions.list[cfg.name] or nil
                resultKey = addOrIncreaseItemWeightWithResult(list and list.items or nil, placeholderFullType, targetBudget, indexCache)
            elseif cfg.kind == "suburbs" then
                local list = SuburbsDistributions and SuburbsDistributions[cfg.name] or nil
                resultKey = addOrIncreaseItemWeightWithResult(list and list.items or nil, placeholderFullType, targetBudget, indexCache)
            end
            recordMutationResult(categoryDiag, "mail", resultKey)
            if resultKey == LOOT_RESULT_KEYS.inserted or resultKey == LOOT_RESULT_KEYS.increased then
                recordInjected(summary, category, nil, source)
                recordInjectedEntry(categoryDiag, "mail")
            end
        else
            local perUnitWeight = targetBudget / math.max(1, #units)
            for j = 1, #units do
                local spawnFullType = tostring(units[j].spawnFullType or "")
                local resultKey = LOOT_RESULT_KEYS.skipped
                if spawnFullType ~= "" and perUnitWeight > 0 then
                    if cfg.kind == "procedural" then
                        local list = ProceduralDistributions and ProceduralDistributions.list and ProceduralDistributions.list[cfg.name] or nil
                        resultKey = addOrIncreaseItemWeightWithResult(list and list.items or nil, spawnFullType, perUnitWeight, indexCache)
                    elseif cfg.kind == "suburbs" then
                        local list = SuburbsDistributions and SuburbsDistributions[cfg.name] or nil
                        resultKey = addOrIncreaseItemWeightWithResult(list and list.items or nil, spawnFullType, perUnitWeight, indexCache)
                    end
                end
                recordMutationResult(categoryDiag, "mail", resultKey)
                if resultKey == LOOT_RESULT_KEYS.inserted or resultKey == LOOT_RESULT_KEYS.increased then
                    recordInjected(summary, category, nil, source)
                    recordInjectedEntry(categoryDiag, "mail")
                end
            end
        end
    end
    categoryDiag.averageTargetWeight.mail = categoryDiag.laneWeight.mail / math.max(1, categoryDiag.eligibleTargets.mail)
    categoryDiag.averageEntryWeight.mail = categoryDiag.laneWeight.mail / math.max(1, tonumber(categoryDiag.injectedEntries.mail) or 0)
end

local function injectRepresentativeIntoProceduralBudget(category, representativeFullType, targets, effectiveBudget, summary, diagnostics, indexCache, source)
    if representativeFullType == nil or representativeFullType == "" or effectiveBudget <= 0 then
        return
    end
    local categoryDiag = ensureCategoryDiagnostics(diagnostics, category)
    local laneMultiplier = resolveRepresentativeLaneMultiplier(category, "procedural")
    local activeTargets = targets or {}
    local storeTargetFloor = resolveMusicStoreMinimumTargetBudget(category, resolveCategoryRate(category), activeTargets, source)
    categoryDiag.eligibleTargets.procedural = #activeTargets
    if #activeTargets < 1 then
        return
    end

    for i = 1, #activeTargets do
        local cfg = activeTargets[i]
        local baseWeight = resolveProceduralTargetBaseWeight(category, cfg.name, cfg.weight)
        local targetBudget = baseWeight * effectiveBudget * laneMultiplier
        if source == LOOT_ROUTE_KEYS.standard and isMediaCategory(category) then
            targetBudget = targetBudget * resolveStandardMediaProceduralAbundanceBoost(resolveCategoryRate(category))
        end
        if isMusicStoreTarget(cfg.name) and storeTargetFloor > 0 then
            targetBudget = math.max(targetBudget, storeTargetFloor)
        end
        categoryDiag.estimatedWeight = categoryDiag.estimatedWeight + targetBudget
        categoryDiag.laneWeight.procedural = categoryDiag.laneWeight.procedural + targetBudget
        local injected = false
        if targetBudget > 0 then
            if source == LOOT_ROUTE_KEYS.standard then
                injected = injectIntoProcedural(cfg.name, representativeFullType, targetBudget, indexCache)
            else
                injected = injectIntoProceduralAdditive(cfg.name, representativeFullType, targetBudget, indexCache)
            end
        end
        if injected then
            recordInjected(summary, category, nil, source)
            recordInjectedEntry(categoryDiag, "procedural")
        end
    end
    categoryDiag.averageTargetWeight.procedural = categoryDiag.laneWeight.procedural / math.max(1, categoryDiag.eligibleTargets.procedural)
    categoryDiag.averageEntryWeight.procedural = categoryDiag.laneWeight.procedural / math.max(1, tonumber(categoryDiag.injectedEntries.procedural) or 0)
end

local function injectRepresentativeIntoVehicleBudget(category, representativeFullType, groupedVehicleTargets, summary, diagnostics, indexCache, source)
    if representativeFullType == nil or representativeFullType == "" then
        return
    end
    local rate = resolveCategoryRate(category)
    local categoryDiag = ensureCategoryDiagnostics(diagnostics, category)
    local totalTargets = 0
    for i = 1, #VEHICLE_ROLE_ORDER do
        totalTargets = totalTargets + #(groupedVehicleTargets[VEHICLE_ROLE_ORDER[i]] or {})
    end
    categoryDiag.eligibleTargets.vehicle = totalTargets
    if totalTargets < 1 then
        return
    end

    for i = 1, #VEHICLE_ROLE_ORDER do
        local role = VEHICLE_ROLE_ORDER[i]
        local targets = groupedVehicleTargets[role] or {}
        local perTargetWeight = resolveVehicleRoleTargetWeight(category, role, rate)
        if source == LOOT_ROUTE_KEYS.standard and isMediaCategory(category) then
            perTargetWeight = perTargetWeight * resolveStandardMediaVehicleAbundanceBoost(rate)
        end
        if perTargetWeight > 0 then
            for j = 1, #targets do
                local injected = false
                if source == LOOT_ROUTE_KEYS.standard then
                    injected = addItemIfMissingCached(targets[j].items, representativeFullType, perTargetWeight, indexCache)
                else
                    injected = addOrIncreaseItemWeight(targets[j].items, representativeFullType, perTargetWeight, indexCache)
                end
                if injected then
                    recordInjected(summary, category, role, source)
                    recordInjectedEntry(categoryDiag, "vehicle")
                end
                categoryDiag.estimatedWeight = categoryDiag.estimatedWeight + perTargetWeight
                categoryDiag.laneWeight.vehicle = categoryDiag.laneWeight.vehicle + perTargetWeight
            end
        end
    end
    categoryDiag.averageTargetWeight.vehicle = categoryDiag.laneWeight.vehicle / math.max(1, categoryDiag.eligibleTargets.vehicle)
    categoryDiag.averageEntryWeight.vehicle = categoryDiag.laneWeight.vehicle / math.max(1, tonumber(categoryDiag.injectedEntries.vehicle) or 0)
end

local function injectRepresentativeIntoMailBudget(category, representativeFullType, targets, effectiveBudget, summary, diagnostics, indexCache, source)
    if representativeFullType == nil or representativeFullType == "" or effectiveBudget <= 0 then
        return
    end
    local categoryDiag = ensureCategoryDiagnostics(diagnostics, category)
    local laneMultiplier = resolveRepresentativeLaneMultiplier(category, "mail")
    categoryDiag.eligibleTargets.mail = #(targets or {})
    if categoryDiag.eligibleTargets.mail < 1 then
        return
    end

    for i = 1, #targets do
        local cfg = targets[i]
        local targetBudget = (tonumber(cfg.weight) or 0) * effectiveBudget * laneMultiplier
        local injected = false
        categoryDiag.estimatedWeight = categoryDiag.estimatedWeight + targetBudget
        categoryDiag.laneWeight.mail = categoryDiag.laneWeight.mail + targetBudget
        if targetBudget > 0 then
            if cfg.kind == "procedural" then
                if source == LOOT_ROUTE_KEYS.standard then
                    injected = injectIntoProcedural(cfg.name, representativeFullType, targetBudget, indexCache)
                else
                    injected = injectIntoProceduralAdditive(cfg.name, representativeFullType, targetBudget, indexCache)
                end
            elseif cfg.kind == "suburbs" then
                if source == LOOT_ROUTE_KEYS.standard then
                    injected = injectIntoSuburbsTopLevel(cfg.name, representativeFullType, targetBudget, indexCache)
                else
                    injected = injectIntoSuburbsTopLevelAdditive(cfg.name, representativeFullType, targetBudget, indexCache)
                end
            end
        end
        if injected then
            recordInjected(summary, category, nil, source)
            recordInjectedEntry(categoryDiag, "mail")
        end
    end
    categoryDiag.averageTargetWeight.mail = categoryDiag.laneWeight.mail / math.max(1, categoryDiag.eligibleTargets.mail)
    categoryDiag.averageEntryWeight.mail = categoryDiag.laneWeight.mail / math.max(1, tonumber(categoryDiag.injectedEntries.mail) or 0)
end

local function resolveVanillaOwnershipBackfillMultiplier(removedCount, divisor, scale)
    local normalized = (tonumber(removedCount) or 0) / math.max(1.0, tonumber(divisor) or 1.0)
    normalized = clamp(normalized, 0.0, VANILLA_BACKFILL_MAX_MULTIPLIER)
    return normalized * (tonumber(scale) or 0)
end

local function resolveBudgetFromShare(totalBudget, shares, category)
    return (tonumber(totalBudget) or 0) * (tonumber(shares and shares[category]) or 0)
end

local function injectOwnedVanillaBackfill(mediaPool, devicePool, groupedVehicleTargets, summary, diagnostics, indexCache, distroPatchStats)
    local removedVanillaCDs = tonumber(distroPatchStats and distroPatchStats.removedVanillaCDs) or 0
    local removedVanillaCDPlayers = tonumber(distroPatchStats and distroPatchStats.removedVanillaCDPlayers) or 0
    local mediaBackfillMultiplier = resolveVanillaOwnershipBackfillMultiplier(removedVanillaCDs, VANILLA_MEDIA_BACKFILL_ENTRY_DIVISOR, VANILLA_MEDIA_BACKFILL_SCALE)
    local deviceBackfillMultiplier = resolveVanillaOwnershipBackfillMultiplier(removedVanillaCDPlayers, VANILLA_DEVICE_BACKFILL_ENTRY_DIVISOR, VANILLA_DEVICE_BACKFILL_SCALE)
    local mediaShares, mediaRawWeights = computeUnifiedMediaCategoryShares(mediaPool)
    local deviceShares, deviceRawWeights = computeUnifiedDeviceCategoryShares(devicePool)

    for i = 1, #MEDIA_CATEGORY_ORDER do
        local category = MEDIA_CATEGORY_ORDER[i]
        local representativeFullType = NMFallbackRepresentativeResolver
            and NMFallbackRepresentativeResolver.getRepresentativeFullType
            and NMFallbackRepresentativeResolver.getRepresentativeFullType(category, LOOT_ROUTE_KEYS.globalBackfill)
            or nil
        local effectiveBudget = resolveBudgetFromShare(mediaBackfillMultiplier, mediaShares, category)
        injectRepresentativeIntoProceduralBudget(category, representativeFullType, MEDIA_PROCEDURAL_TARGETS[category], effectiveBudget, summary, diagnostics, indexCache, LOOT_ROUTE_KEYS.globalBackfill)
        injectRepresentativeIntoVehicleBudget(category, representativeFullType, groupedVehicleTargets, summary, diagnostics, indexCache, LOOT_ROUTE_KEYS.globalBackfill)
        injectRepresentativeIntoMailBudget(category, representativeFullType, MEDIA_MAIL_TARGETS[category], effectiveBudget, summary, diagnostics, indexCache, LOOT_ROUTE_KEYS.globalBackfill)
    end

    for i = 1, #DEVICE_CATEGORY_ORDER do
        local category = DEVICE_CATEGORY_ORDER[i]
        local units = orderedDeviceUnitsForCategory(devicePool, category)
        local effectiveBudget = resolveBudgetFromShare(deviceBackfillMultiplier, deviceShares, category)
        injectUnitsIntoProceduralBudget(category, units, DEVICE_PROCEDURAL_TARGETS[category], effectiveBudget, summary, diagnostics, indexCache, LOOT_ROUTE_KEYS.globalBackfill)
        injectUnitsIntoVehicleBudget(category, units, groupedVehicleTargets, 1, summary, diagnostics, indexCache, LOOT_ROUTE_KEYS.globalBackfill)
        injectUnitsIntoMailBudget(category, units, DEVICE_MAIL_TARGETS[category], effectiveBudget, summary, diagnostics, indexCache, LOOT_ROUTE_KEYS.globalBackfill)
    end

    return {
        mediaMultiplier = mediaBackfillMultiplier,
        deviceMultiplier = deviceBackfillMultiplier,
        mediaShares = mediaShares,
        mediaRawWeights = mediaRawWeights,
        deviceShares = deviceShares,
        deviceRawWeights = deviceRawWeights,
        removedVanillaCDs = removedVanillaCDs,
        removedVanillaCDPlayers = removedVanillaCDPlayers
    }
end

local function normalizeStoreTopUpBias(order, biasMap, resolver)
    local weights = {}
    local total = 0
    for i = 1, #(order or {}) do
        local category = order[i]
        local allowed = resolver(category) == true
        local weight = allowed and (tonumber(biasMap and biasMap[category]) or 0) or 0
        weights[category] = weight
        total = total + weight
    end
    if total > 0 then
        for i = 1, #(order or {}) do
            local category = order[i]
            weights[category] = (tonumber(weights[category]) or 0) / total
        end
    end
    return weights
end

local function filterStoreTargets(targets)
    local filtered = {}
    for i = 1, #(targets or {}) do
        local cfg = targets[i]
        local name = tostring(cfg and cfg.name or "")
        if isMusicStoreTarget(name) then
            filtered[#filtered + 1] = cfg
        end
    end
    return filtered
end

local function injectMusicStoreTopUp(mediaPool, devicePool, summary, diagnostics, indexCache)
    local mediaShares = computeMusicStoreMediaShares(mediaPool)
    local deviceBias = normalizeStoreTopUpBias(
        DEVICE_CATEGORY_ORDER,
        MUSIC_STORE_DEVICE_TOPUP_BIAS,
        function(category)
            return resolveCategoryRate(category) > 0 and #orderedDeviceUnitsForCategory(devicePool, category) > 0
        end
    )
    local totalMediaBudget = computeTotalMediaBudget(mediaPool, function(category)
        local rate = clamp(resolveCategoryRate(category), 0.0, 4.0)
        return resolveMediaFallbackBudgetScalar(rate) * resolveMediaStoreTopUpBudgetBoost(rate)
    end)

    for i = 1, #MEDIA_CATEGORY_ORDER do
        local category = MEDIA_CATEGORY_ORDER[i]
        local share = tonumber(mediaShares[category]) or 0
        local effectiveBudget = totalMediaBudget * share
        ACTIVE_STORE_TOPUP_MEDIA_POOL = mediaPool
        injectRepresentativeIntoProceduralBudget(
            category,
            NMFallbackRepresentativeResolver
                and NMFallbackRepresentativeResolver.getRepresentativeFullType
                and NMFallbackRepresentativeResolver.getRepresentativeFullType(category, LOOT_ROUTE_KEYS.storeTopUp)
                or nil,
            filterStoreTargets(MEDIA_PROCEDURAL_TARGETS[category]),
            effectiveBudget,
            summary,
            diagnostics,
            indexCache,
            LOOT_ROUTE_KEYS.storeTopUp
        )
        ACTIVE_STORE_TOPUP_MEDIA_POOL = nil
    end

    for i = 1, #DEVICE_CATEGORY_ORDER do
        local category = DEVICE_CATEGORY_ORDER[i]
        local share = tonumber(deviceBias[category]) or 0
        local rate = clamp(resolveCategoryRate(category), 0.0, 4.0)
        local effectiveBudget = resolveDirectSpawnScalar(rate, BASE_DEVICE_DEFAULT, DEVICE_DIRECT_SPAWN_SCALE_BASE) * share
        injectUnitsIntoProceduralBudget(
            category,
            orderedDeviceUnitsForCategory(devicePool, category),
            filterStoreTargets(DEVICE_PROCEDURAL_TARGETS[category]),
            effectiveBudget,
            summary,
            diagnostics,
            indexCache,
            LOOT_ROUTE_KEYS.storeTopUp
        )
    end

    return {
        mediaShares = mediaShares,
        deviceBias = deviceBias
    }
end

local function countMediaPoolOwnership(unitMap)
    local owned = { base = {}, child = {} }
    for i = 1, #MEDIA_CATEGORY_ORDER do
        local category = MEDIA_CATEGORY_ORDER[i]
        owned.base[category] = 0
        owned.child[category] = 0
        for _, unit in pairs(unitMap and unitMap[category] or {}) do
            local owner = tostring(unit and unit.owner or "")
            if owner == "base" then
                owned.base[category] = owned.base[category] + 1
            else
                owned.child[category] = owned.child[category] + 1
            end
        end
    end
    return owned
end

local function buildMusicStoreTargetDiagnostics()
    local perCategory = {}
    local laneByCategory = {}
    for i = 1, #MUSIC_STORE_TARGET_ORDER do
        laneByCategory[MUSIC_STORE_TARGET_ORDER[i]] = {}
    end
    for i = 1, #CATEGORY_ORDER do
        local category = CATEGORY_ORDER[i]
        perCategory[category] = 0
        for j = 1, #MUSIC_STORE_TARGET_ORDER do
            laneByCategory[MUSIC_STORE_TARGET_ORDER[j]][category] = 0
        end
        local targets = MEDIA_PROCEDURAL_TARGETS[category] or DEVICE_PROCEDURAL_TARGETS[category] or {}
        for j = 1, #targets do
            local name = tostring(targets[j] and targets[j].name or "")
            if isMusicStoreTarget(name) then
                local weight = resolveProceduralTargetBaseWeight(category, name, targets[j].weight)
                laneByCategory[name][category] = weight
                perCategory[category] = perCategory[category] + weight
            end
        end
    end
    return perCategory, laneByCategory
end

function planner.applyBuildContext(context, options)
    local mediaPool = context and context.fallbackMediaPool or {}
    local devicePool = context and context.fallbackDevicePool or {}
    local vehicleTargets = context and context.vehicleTargets or {}
    local distributionAuditEnabled = options and options.distributionAuditEnabled == true

    local injected = newInjectionSummary()
    local diagnostics = {}
    local groupedVehicleTargets = countUniqueVehicleTargetsByRoleMap(vehicleTargets)
    local indexCache = {}
    local distroPatchStats = NMServerDistroPatch and NMServerDistroPatch.getStats and NMServerDistroPatch.getStats() or nil
    local mediaShares, mediaRawWeights = computeUnifiedMediaCategoryShares(mediaPool)
    local totalMediaBudget = computeTotalMediaBudget(mediaPool, function(category)
        local rate = clamp(resolveCategoryRate(category), 0.0, 4.0)
        return resolveMediaFallbackBudgetScalar(rate)
    end)

    for i = 1, #MEDIA_CATEGORY_ORDER do
        local category = MEDIA_CATEGORY_ORDER[i]
        local representativeFullType = NMFallbackRepresentativeResolver
            and NMFallbackRepresentativeResolver.getRepresentativeFullType
            and NMFallbackRepresentativeResolver.getRepresentativeFullType(category)
            or nil
        local categoryDiag = ensureCategoryDiagnostics(diagnostics, category)
        local rate = clamp(resolveCategoryRate(category), 0.0, 4.0)
        local multiplier = resolveMediaFallbackBudgetScalar(rate)
        local share = tonumber(mediaShares[category]) or 0
        local effectiveBudget = resolveBudgetFromShare(totalMediaBudget, mediaShares, category)
        categoryDiag.poolSize = #orderedMediaUnitsForCategory(mediaPool, category)
        categoryDiag.rate = rate
        categoryDiag.multiplier = multiplier
        categoryDiag.share = share
        categoryDiag.rawWeight = tonumber(mediaRawWeights[category]) or 0
        categoryDiag.budget = effectiveBudget
        injectRepresentativeIntoProceduralBudget(category, representativeFullType, MEDIA_PROCEDURAL_TARGETS[category], effectiveBudget, injected, diagnostics, indexCache, LOOT_ROUTE_KEYS.standard)
        injectRepresentativeIntoVehicleBudget(category, representativeFullType, groupedVehicleTargets, injected, diagnostics, indexCache, LOOT_ROUTE_KEYS.standard)
        injectRepresentativeIntoMailBudget(category, representativeFullType, MEDIA_MAIL_TARGETS[category], effectiveBudget, injected, diagnostics, indexCache, LOOT_ROUTE_KEYS.standard)
    end

    for i = 1, #DEVICE_CATEGORY_ORDER do
        local category = DEVICE_CATEGORY_ORDER[i]
        local units = orderedDeviceUnitsForCategory(devicePool, category)
        local categoryDiag = ensureCategoryDiagnostics(diagnostics, category)
        local rate = clamp(resolveCategoryRate(category), 0.0, 4.0)
        local multiplier = resolveStandardDeviceScalar(category, rate)
        categoryDiag.poolSize = #units
        categoryDiag.rate = rate
        categoryDiag.multiplier = multiplier
        categoryDiag.budget = multiplier
        injectUnitsIntoProceduralBudget(category, units, DEVICE_PROCEDURAL_TARGETS[category], multiplier, injected, diagnostics, indexCache, LOOT_ROUTE_KEYS.standard)
        injectUnitsIntoVehicleBudget(category, units, groupedVehicleTargets, multiplier, injected, diagnostics, indexCache, LOOT_ROUTE_KEYS.standard)
        injectUnitsIntoMailBudget(category, units, DEVICE_MAIL_TARGETS[category], multiplier, injected, diagnostics, indexCache, LOOT_ROUTE_KEYS.standard)
    end

    local backfill = injectOwnedVanillaBackfill(mediaPool, devicePool, groupedVehicleTargets, injected, diagnostics, indexCache, distroPatchStats)
    local storeTopUp = injectMusicStoreTopUp(mediaPool, devicePool, injected, diagnostics, indexCache)
    local musicStoreCategoryWeights = nil
    local musicStoreLaneWeights = nil
    local fallbackMediaOwnership = nil
    if distributionAuditEnabled then
        musicStoreCategoryWeights, musicStoreLaneWeights = buildMusicStoreTargetDiagnostics()
        fallbackMediaOwnership = countMediaPoolOwnership(mediaPool)
    end

    return {
        injected = injected,
        budgetDiagnostics = diagnostics,
        backfill = backfill,
        storeTopUp = storeTopUp,
        standardRoute = {
            mediaShares = mediaShares,
            mediaRawWeights = mediaRawWeights,
            totalMediaBudget = totalMediaBudget
        },
        fallbackMediaCounts = NMManagedSpawnCatalog.countUnitPool(mediaPool, MEDIA_CATEGORY_ORDER),
        fallbackDeviceCounts = NMManagedSpawnCatalog.countUnitPool(devicePool, DEVICE_CATEGORY_ORDER),
        fallbackMediaOwnership = fallbackMediaOwnership,
        musicStoreCategoryWeights = musicStoreCategoryWeights,
        musicStoreLaneWeights = musicStoreLaneWeights,
        fallbackRepresentatives = {
            cassettes = NMFallbackRepresentativeResolver.getRepresentativeFullType("cassettes") or "",
            vinyl = NMFallbackRepresentativeResolver.getRepresentativeFullType("vinyl") or "",
            cds = NMFallbackRepresentativeResolver.getRepresentativeFullType("cds") or ""
        }
    }
end

return planner
