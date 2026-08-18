require "loot/NMManagedSpawnCatalog"
require "loot/NMLootSandboxSettings"

NMLootRealizationAuthority = NMLootRealizationAuthority or {}

local authority = NMLootRealizationAuthority

local MEDIA_CATEGORY_ORDER = NMLootSandboxSettings.MEDIA_CATEGORY_ORDER
local DEVICE_CATEGORY_ORDER = NMLootSandboxSettings.DEVICE_CATEGORY_ORDER
local VEHICLE_ROLE_ORDER = NMLootSandboxSettings.VEHICLE_ROLE_ORDER
local MUSIC_STORE_DEVICE_TOPUP_BIAS = NMLootSandboxSettings.MUSIC_STORE_DEVICE_TOPUP_BIAS
local MUSIC_STORE_TARGET_ORDER = NMLootSandboxSettings.MUSIC_STORE_TARGET_ORDER
local clamp = NMLootSandboxSettings.clamp
local resolveCategoryRate = NMLootSandboxSettings.resolveCategoryRate
local resolveMediaFallbackBudgetScalar = NMLootSandboxSettings.resolveMediaFallbackBudgetScalar
local resolveMediaStoreTopUpBudgetBoost = NMLootSandboxSettings.resolveMediaStoreTopUpBudgetBoost
local resolveStandardDeviceScalar = NMLootSandboxSettings.resolveStandardDeviceScalar
local resolveStandardMediaProceduralAbundanceBoost = NMLootSandboxSettings.resolveStandardMediaProceduralAbundanceBoost
local resolveStandardMediaVehicleAbundanceBoost = NMLootSandboxSettings.resolveStandardMediaVehicleAbundanceBoost
local normalizePlayableRate = NMLootSandboxSettings.normalizePlayableRate

local STANDARD_RESIDENTIAL_MEDIA_ROUTE_CLASS = "residential_misc"

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

authority.MEDIA_PROCEDURAL_TARGETS = {
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
        { name = "RecRoomShelf", weight = 1.5 }, { name = "SchoolLockers", weight = 1.0 },
        { name = "SchoolLockersBad", weight = 1.0 }, { name = "LivingRoomShelf", weight = 3.25 },
        { name = "LivingRoomShelfClassy", weight = 6.0 }, { name = "LivingRoomShelfRedneck", weight = 4.0 },
        { name = "LivingRoomShelfNoTapes", weight = 3.0 }, { name = "LivingRoomSideTable", weight = 2.5 },
        { name = "LivingRoomCabinet", weight = 3.0 }, { name = "BedroomDresser", weight = 3.0 },
        { name = "BedroomDresserClassy", weight = 3.0 }, { name = "StoreShelfCombo", weight = 1.5 }
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
        { name = "LivingRoomShelf", weight = 3.25 }, { name = "LivingRoomShelfClassy", weight = 3.25 },
        { name = "LivingRoomShelfRedneck", weight = 2.25 }, { name = "LivingRoomShelfNoTapes", weight = 1.75 },
        { name = "LivingRoomSideTable", weight = 1.75 }, { name = "LivingRoomCabinet", weight = 2.25 },
        { name = "BedroomDresser", weight = 2.25 }, { name = "BedroomDresserClassy", weight = 2.25 },
        { name = "StoreShelfCombo", weight = 1.0 }
    }
}

authority.DEVICE_PROCEDURAL_TARGETS = {
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

local function orderedMediaUnitsForCategory(pool, category)
    return NMManagedSpawnCatalog.orderedMediaUnitsForCategory({ media = pool or {} }, category)
end

local function orderedDeviceUnitsForCategory(pool, category)
    return NMManagedSpawnCatalog.orderedDeviceUnitsForCategory({ devices = pool or {} }, category)
end

local function resolveRepresentativeLaneMultiplier(category, lane)
    if lane == "procedural" then
        return tonumber(NMLootSandboxSettings.MEDIA_REPRESENTATIVE_PROCEDURAL_LANE_MULTIPLIERS[tostring(category or "")]) or 1.0
    end
    if lane == "mail" then
        return tonumber(NMLootSandboxSettings.MEDIA_REPRESENTATIVE_MAIL_LANE_MULTIPLIERS[tostring(category or "")]) or 1.0
    end
    return 1.0
end

function authority.isMusicStoreTarget(listName)
    local name = tostring(listName or "")
    return string.sub(name, 1, #"MusicStore") == "MusicStore"
end

function authority.isResidentialProceduralTarget(listName)
    local name = tostring(listName or "")
    return name == "BedroomDresser"
        or name == "BedroomDresserClassy"
        or name == "LivingRoomSideTable"
        or name == "LivingRoomCabinet"
        or string.find(name, "LivingRoomShelf", 1, true) ~= nil
        or name == "RecRoomShelf"
end

function authority.resolveProceduralTargetCap(listName, source, kind)
    local name = tostring(listName or "")
    local sourceKey = tostring(source or "standard")
    local kindKey = tostring(kind or "media")
    if authority.isMusicStoreTarget(name) then
        if kindKey == "media" then
            return sourceKey == "storeTopUp" and 3 or 2
        end
        return sourceKey == "storeTopUp" and 2 or 1
    end
    if authority.isResidentialProceduralTarget(name) then
        return 1
    end
    if name == "StoreShelfCombo" then
        return kindKey == "media" and 2 or 1
    end
    if kindKey == "media" then
        return sourceKey == "globalBackfill" and 1 or 2
    end
    return 1
end

function authority.resolveVehicleTargetCap(role, source, kind)
    local roleKey = tostring(role or "")
    local sourceKey = tostring(source or "standard")
    local kindKey = tostring(kind or "media")
    if roleKey == "cargo" then
        return kindKey == "media" and 2 or 1
    end
    if sourceKey == "globalBackfill" then
        return 1
    end
    return 1
end

function authority.resolveVehicleRoleTargetWeight(category, role, rate)
    local curve = VEHICLE_ROLE_WEIGHT_CURVES[tostring(category or "")]
        and VEHICLE_ROLE_WEIGHT_CURVES[tostring(category or "")][tostring(role or "")]
        or nil
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

function authority.resolveBudgetFromShare(totalBudget, shares, category)
    return (tonumber(totalBudget) or 0) * (tonumber(shares and shares[category]) or 0)
end

local function classifyProceduralTarget(listName)
    local name = tostring(listName or "")
    if authority.isMusicStoreTarget(name) then
        return "music_store"
    end
    if string.find(name, "ElectronicStore", 1, true) ~= nil
        or string.find(name, "Electronics", 1, true) ~= nil
        or string.find(name, "GigamartHouseElectronics", 1, true) ~= nil
        or string.find(name, "StoreShelfElectronics", 1, true) ~= nil
    then
        return "electronics"
    end
    if authority.isResidentialProceduralTarget(name) then
        return "residential_misc"
    end
    return "other"
end

local function isBaselineMediaBalanceActive(lootPolicy)
    local firstRate = nil
    for i = 1, #MEDIA_CATEGORY_ORDER do
        local category = MEDIA_CATEGORY_ORDER[i]
        local rate = clamp(resolveCategoryRate(category, lootPolicy), 0.0, 4.0)
        if rate <= 0 or rate > NMLootSandboxSettings.BASE_MEDIA_DEFAULT then
            return false
        end
        if firstRate == nil then
            firstRate = rate
        elseif math.abs(rate - firstRate) > 0.0001 then
            return false
        end
    end
    return true
end

local function isProceduralMediaCategoryAllowed(category, targetName)
    local categoryKey = tostring(category or "")
    local name = tostring(targetName or "")
    if categoryKey == "vinyl" then
        return name ~= "SchoolLockers"
            and name ~= "SchoolLockersBad"
            and name ~= "SchoolDesk"
            and name ~= "CrateCompactDiscs"
    end
    return true
end

local function isVehicleMediaCategoryAllowed(category, role)
    local categoryKey = tostring(category or "")
    local roleKey = tostring(role or "")
    if categoryKey == "vinyl" then
        return roleKey == "cargo"
    end
    return true
end

local function findProceduralMediaBaseWeight(category, targetName)
    local targets = authority.MEDIA_PROCEDURAL_TARGETS[tostring(category or "")] or {}
    local name = tostring(targetName or "")
    for i = 1, #targets do
        if tostring(targets[i] and targets[i].name or "") == name then
            return tonumber(targets[i].weight) or 0
        end
    end
    return 0
end

local function resolveBalancedProceduralMediaBaseWeight(category, targetName, defaultWeight, lootPolicy)
    if isBaselineMediaBalanceActive(lootPolicy) ~= true then
        return tonumber(defaultWeight) or 0
    end
    if isProceduralMediaCategoryAllowed(category, targetName) ~= true then
        return 0
    end

    local total = 0
    local count = 0
    for i = 1, #MEDIA_CATEGORY_ORDER do
        local allowedCategory = MEDIA_CATEGORY_ORDER[i]
        if isProceduralMediaCategoryAllowed(allowedCategory, targetName)
            and clamp(resolveCategoryRate(allowedCategory, lootPolicy), 0.0, 4.0) > 0
        then
            local weight = findProceduralMediaBaseWeight(allowedCategory, targetName)
            if weight > 0 then
                total = total + weight
                count = count + 1
            end
        end
    end
    if count < 1 then
        return 0
    end
    return total / count
end

local function resolveBalancedVehicleMediaRoleWeight(category, role, rate, lootPolicy)
    if isBaselineMediaBalanceActive(lootPolicy) ~= true then
        return authority.resolveVehicleRoleTargetWeight(category, role, rate)
    end
    if isVehicleMediaCategoryAllowed(category, role) ~= true then
        return 0
    end

    local total = 0
    local count = 0
    for i = 1, #MEDIA_CATEGORY_ORDER do
        local allowedCategory = MEDIA_CATEGORY_ORDER[i]
        if isVehicleMediaCategoryAllowed(allowedCategory, role)
            and clamp(resolveCategoryRate(allowedCategory, lootPolicy), 0.0, 4.0) > 0
        then
            local allowedRate = clamp(resolveCategoryRate(allowedCategory, lootPolicy), 0.0, 4.0)
            local weight = authority.resolveVehicleRoleTargetWeight(allowedCategory, role, allowedRate)
            if weight > 0 then
                total = total + weight
                count = count + 1
            end
        end
    end
    if count < 1 then
        return 0
    end
    return total / count
end

local function normalizeBias(order, biasMap, resolver)
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

local function ensureProfileAttempt(profiles, routeClass, route, kind)
    profiles[routeClass] = profiles[routeClass] or { id = routeClass, attempts = {} }
    local key = tostring(route or "") .. "|" .. tostring(kind or "")
    local attempt = profiles[routeClass].attempts[key]
    if attempt then
        return attempt
    end
    attempt = {
        id = key,
        route = tostring(route or "standard"),
        kind = tostring(kind or "media"),
        maxAdds = 0,
        rawWeights = {},
        categoryShares = {},
        totalWeight = 0
    }
    profiles[routeClass].attempts[key] = attempt
    return attempt
end

local function accumulateAttempt(profiles, routeClass, route, kind, category, weight, cap)
    if routeClass == "" or routeClass == "other" or weight <= 0 or cap <= 0 then
        return
    end
    local attempt = ensureProfileAttempt(profiles, routeClass, route, kind)
    attempt.maxAdds = math.max(tonumber(attempt.maxAdds) or 0, tonumber(cap) or 0)
    attempt.rawWeights[category] = (tonumber(attempt.rawWeights[category]) or 0) + weight
    attempt.totalWeight = (tonumber(attempt.totalWeight) or 0) + weight
end

local function finalizeProfiles(profiles)
    local routePriority = { standard = 1, globalBackfill = 2, storeTopUp = 3 }
    local kindPriority = { media = 1, device = 2 }
    local finalized = {}
    for routeClass, profile in pairs(profiles) do
        local attempts = {}
        for _, attempt in pairs(profile.attempts) do
            local order = attempt.kind == "device" and DEVICE_CATEGORY_ORDER or MEDIA_CATEGORY_ORDER
            local totalWeight = tonumber(attempt.totalWeight) or 0
            for i = 1, #order do
                local category = order[i]
                local rawWeight = tonumber(attempt.rawWeights[category]) or 0
                attempt.categoryShares[category] = totalWeight > 0 and (rawWeight / totalWeight) or 0
            end
            if totalWeight > 0 and (tonumber(attempt.maxAdds) or 0) > 0 then
                attempts[#attempts + 1] = attempt
            end
        end
        table.sort(attempts, function(a, b)
            local aRoute = tonumber(routePriority[a.route]) or 99
            local bRoute = tonumber(routePriority[b.route]) or 99
            if aRoute ~= bRoute then
                return aRoute < bRoute
            end
            local aKind = tonumber(kindPriority[a.kind]) or 99
            local bKind = tonumber(kindPriority[b.kind]) or 99
            if aKind ~= bKind then
                return aKind < bKind
            end
            return (tonumber(a.totalWeight) or 0) > (tonumber(b.totalWeight) or 0)
        end)
        finalized[routeClass] = {
            id = routeClass,
            attempts = attempts
        }
    end
    return finalized
end

local function buildProfiles(routes, lootPolicy, distroPatchStats)
    local profiles = {}

    local standardMediaShares = routes.standard and routes.standard.mediaShares or {}
    local standardTotalMediaBudget = tonumber(routes.standard and routes.standard.totalMediaBudget) or 0
    for i = 1, #MEDIA_CATEGORY_ORDER do
        local category = MEDIA_CATEGORY_ORDER[i]
        local rate = clamp(resolveCategoryRate(category, lootPolicy), 0.0, 4.0)
        local effectiveBudget = authority.resolveBudgetFromShare(standardTotalMediaBudget, standardMediaShares, category)
        for j = 1, #(authority.MEDIA_PROCEDURAL_TARGETS[category] or {}) do
            local cfg = authority.MEDIA_PROCEDURAL_TARGETS[category][j]
            local routeClass = classifyProceduralTarget(cfg.name)
            local baseWeight = resolveBalancedProceduralMediaBaseWeight(category, cfg.name, cfg.weight, lootPolicy)
            local weight = baseWeight
                * resolveRepresentativeLaneMultiplier(category, "procedural")
                * resolveStandardMediaProceduralAbundanceBoost(rate)
                * effectiveBudget
            if routeClass == STANDARD_RESIDENTIAL_MEDIA_ROUTE_CLASS then
                weight = tonumber(standardMediaShares[category]) or 0
            end
            accumulateAttempt(
                profiles,
                routeClass,
                "standard",
                "media",
                category,
                weight,
                authority.resolveProceduralTargetCap(cfg.name, "standard", "media")
            )
        end
        for roleIndex = 1, #VEHICLE_ROLE_ORDER do
            local role = VEHICLE_ROLE_ORDER[roleIndex]
            local weight = resolveBalancedVehicleMediaRoleWeight(category, role, rate, lootPolicy)
                * resolveStandardMediaVehicleAbundanceBoost(rate)
            accumulateAttempt(
                profiles,
                "vehicle_" .. tostring(role),
                "standard",
                "media",
                category,
                weight,
                authority.resolveVehicleTargetCap(role, "standard", "media")
            )
        end
    end

    for i = 1, #DEVICE_CATEGORY_ORDER do
        local category = DEVICE_CATEGORY_ORDER[i]
        local rate = clamp(resolveCategoryRate(category, lootPolicy), 0.0, 4.0)
        local effectiveBudget = resolveStandardDeviceScalar(category, rate)
        for j = 1, #(authority.DEVICE_PROCEDURAL_TARGETS[category] or {}) do
            local cfg = authority.DEVICE_PROCEDURAL_TARGETS[category][j]
            accumulateAttempt(
                profiles,
                classifyProceduralTarget(cfg.name),
                "standard",
                "device",
                category,
                (tonumber(cfg.weight) or 0) * effectiveBudget,
                authority.resolveProceduralTargetCap(cfg.name, "standard", "device")
            )
        end
        for roleIndex = 1, #VEHICLE_ROLE_ORDER do
            local role = VEHICLE_ROLE_ORDER[roleIndex]
            accumulateAttempt(
                profiles,
                "vehicle_" .. tostring(role),
                "standard",
                "device",
                category,
                authority.resolveVehicleRoleTargetWeight(category, role, rate) * effectiveBudget,
                authority.resolveVehicleTargetCap(role, "standard", "device")
            )
        end
    end

    local removedVanillaCDs = tonumber(distroPatchStats and distroPatchStats.removedVanillaCDs) or 0
    local removedVanillaCDPlayers = tonumber(distroPatchStats and distroPatchStats.removedVanillaCDPlayers) or 0
    local mediaBackfillMultiplier = math.min(
        clamp(
            removedVanillaCDs / math.max(1.0, tonumber(NMLootSandboxSettings.VANILLA_MEDIA_BACKFILL_ENTRY_DIVISOR) or 1.0),
            0.0,
            tonumber(NMLootSandboxSettings.VANILLA_BACKFILL_MAX_MULTIPLIER) or 0
        ),
        tonumber(NMLootSandboxSettings.VANILLA_BACKFILL_MAX_MULTIPLIER) or 0
    ) * (tonumber(NMLootSandboxSettings.VANILLA_MEDIA_BACKFILL_SCALE) or 0)
    local deviceBackfillMultiplier = math.min(
        clamp(
            removedVanillaCDPlayers / math.max(1.0, tonumber(NMLootSandboxSettings.VANILLA_DEVICE_BACKFILL_ENTRY_DIVISOR) or 1.0),
            0.0,
            tonumber(NMLootSandboxSettings.VANILLA_BACKFILL_MAX_MULTIPLIER) or 0
        ),
        tonumber(NMLootSandboxSettings.VANILLA_BACKFILL_MAX_MULTIPLIER) or 0
    ) * (tonumber(NMLootSandboxSettings.VANILLA_DEVICE_BACKFILL_SCALE) or 0)

    local backfillMediaShares = routes.globalBackfill and routes.globalBackfill.mediaShares or {}
    local backfillDeviceShares = routes.globalBackfill and routes.globalBackfill.deviceShares or {}
    for i = 1, #MEDIA_CATEGORY_ORDER do
        local category = MEDIA_CATEGORY_ORDER[i]
        local rate = clamp(resolveCategoryRate(category, lootPolicy), 0.0, 4.0)
        local effectiveBudget = authority.resolveBudgetFromShare(mediaBackfillMultiplier, backfillMediaShares, category)
        for j = 1, #(authority.MEDIA_PROCEDURAL_TARGETS[category] or {}) do
            local cfg = authority.MEDIA_PROCEDURAL_TARGETS[category][j]
            local baseWeight = resolveBalancedProceduralMediaBaseWeight(category, cfg.name, cfg.weight, lootPolicy)
            accumulateAttempt(
                profiles,
                classifyProceduralTarget(cfg.name),
                "globalBackfill",
                "media",
                category,
                baseWeight * effectiveBudget,
                authority.resolveProceduralTargetCap(cfg.name, "globalBackfill", "media")
            )
        end
        for roleIndex = 1, #VEHICLE_ROLE_ORDER do
            local role = VEHICLE_ROLE_ORDER[roleIndex]
            accumulateAttempt(
                profiles,
                "vehicle_" .. tostring(role),
                "globalBackfill",
                "media",
                category,
                resolveBalancedVehicleMediaRoleWeight(category, role, rate, lootPolicy),
                authority.resolveVehicleTargetCap(role, "globalBackfill", "media")
            )
        end
    end

    for i = 1, #DEVICE_CATEGORY_ORDER do
        local category = DEVICE_CATEGORY_ORDER[i]
        local rate = clamp(resolveCategoryRate(category, lootPolicy), 0.0, 4.0)
        local effectiveBudget = authority.resolveBudgetFromShare(deviceBackfillMultiplier, backfillDeviceShares, category)
        for j = 1, #(authority.DEVICE_PROCEDURAL_TARGETS[category] or {}) do
            local cfg = authority.DEVICE_PROCEDURAL_TARGETS[category][j]
            accumulateAttempt(
                profiles,
                classifyProceduralTarget(cfg.name),
                "globalBackfill",
                "device",
                category,
                (tonumber(cfg.weight) or 0) * effectiveBudget,
                authority.resolveProceduralTargetCap(cfg.name, "globalBackfill", "device")
            )
        end
        for roleIndex = 1, #VEHICLE_ROLE_ORDER do
            local role = VEHICLE_ROLE_ORDER[roleIndex]
            accumulateAttempt(
                profiles,
                "vehicle_" .. tostring(role),
                "globalBackfill",
                "device",
                category,
                authority.resolveVehicleRoleTargetWeight(category, role, rate),
                authority.resolveVehicleTargetCap(role, "globalBackfill", "device")
            )
        end
    end

    local storeMediaShares = routes.storeTopUp and routes.storeTopUp.mediaShares or {}
    local storeDeviceBias = routes.storeTopUp and routes.storeTopUp.deviceBias or {}
    local storeTotalMediaBudget = tonumber(routes.storeTopUp and routes.storeTopUp.totalMediaBudget) or 0
    for i = 1, #MEDIA_CATEGORY_ORDER do
        local category = MEDIA_CATEGORY_ORDER[i]
        local effectiveBudget = authority.resolveBudgetFromShare(storeTotalMediaBudget, storeMediaShares, category)
        for j = 1, #(authority.MEDIA_PROCEDURAL_TARGETS[category] or {}) do
            local cfg = authority.MEDIA_PROCEDURAL_TARGETS[category][j]
            if authority.isMusicStoreTarget(cfg.name) then
                local baseWeight = resolveBalancedProceduralMediaBaseWeight(category, cfg.name, cfg.weight, lootPolicy)
                accumulateAttempt(
                    profiles,
                    "music_store",
                    "storeTopUp",
                    "media",
                    category,
                    baseWeight * effectiveBudget,
                    authority.resolveProceduralTargetCap(cfg.name, "storeTopUp", "media")
                )
            end
        end
    end

    for i = 1, #DEVICE_CATEGORY_ORDER do
        local category = DEVICE_CATEGORY_ORDER[i]
        local rate = clamp(resolveCategoryRate(category, lootPolicy), 0.0, 4.0)
        local effectiveBudget = resolveStandardDeviceScalar(category, rate) * (tonumber(storeDeviceBias[category]) or 0)
        for j = 1, #(authority.DEVICE_PROCEDURAL_TARGETS[category] or {}) do
            local cfg = authority.DEVICE_PROCEDURAL_TARGETS[category][j]
            if authority.isMusicStoreTarget(cfg.name) then
                accumulateAttempt(
                    profiles,
                    "music_store",
                    "storeTopUp",
                    "device",
                    category,
                    (tonumber(cfg.weight) or 0) * effectiveBudget,
                    authority.resolveProceduralTargetCap(cfg.name, "storeTopUp", "device")
                )
            end
        end
    end

    return finalizeProfiles(profiles)
end

function authority.build(config)
    local mediaPool = config and config.mediaPool or {}
    local devicePool = config and config.devicePool or {}
    local lootPolicy = config and config.lootPolicy or nil
    local distroPatchStats = config and config.distroPatchStats or nil

    local standardMediaShares, standardMediaRawWeights = NMLootSandboxSettings.computeStandardBackfillMediaCategoryShares(
        mediaPool,
        orderedMediaUnitsForCategory,
        lootPolicy
    )
    local standardCompensatedMediaShares, standardCompensatedMediaRawWeights = standardMediaShares, standardMediaRawWeights
    local standardTotalMediaBudget = NMLootSandboxSettings.computeTotalMediaBudget(
        mediaPool,
        orderedMediaUnitsForCategory,
        function(category)
            local rate = clamp(resolveCategoryRate(category, lootPolicy), 0.0, 4.0)
            return resolveMediaFallbackBudgetScalar(rate)
        end
    )

    local backfillMediaShares, backfillMediaRawWeights = NMLootSandboxSettings.computeStandardBackfillMediaCategoryShares(
        mediaPool,
        orderedMediaUnitsForCategory,
        lootPolicy
    )
    local backfillCompensatedMediaShares, backfillCompensatedMediaRawWeights = backfillMediaShares, backfillMediaRawWeights
    local backfillDeviceShares, backfillDeviceRawWeights = NMLootSandboxSettings.computeUnifiedDeviceCategoryShares(
        devicePool,
        orderedDeviceUnitsForCategory,
        lootPolicy
    )

    local storeMediaShares = NMLootSandboxSettings.computeMusicStoreMediaShares(
        mediaPool,
        orderedMediaUnitsForCategory,
        lootPolicy
    )
    local storeDeviceBias = normalizeBias(
        DEVICE_CATEGORY_ORDER,
        MUSIC_STORE_DEVICE_TOPUP_BIAS,
        function(category)
            return resolveCategoryRate(category, lootPolicy) > 0 and #orderedDeviceUnitsForCategory(devicePool, category) > 0
        end
    )
    local storeTotalMediaBudget = NMLootSandboxSettings.computeTotalMediaBudget(
        mediaPool,
        orderedMediaUnitsForCategory,
        function(category)
            local rate = clamp(resolveCategoryRate(category, lootPolicy), 0.0, 4.0)
            return resolveMediaFallbackBudgetScalar(rate) * resolveMediaStoreTopUpBudgetBoost(rate)
        end
    )

    local routes = {
        standard = {
            mediaShares = standardMediaShares,
            mediaRawWeights = standardMediaRawWeights,
            compensatedMediaShares = standardCompensatedMediaShares,
            compensatedMediaRawWeights = standardCompensatedMediaRawWeights,
            totalMediaBudget = standardTotalMediaBudget
        },
        globalBackfill = {
            mediaShares = backfillMediaShares,
            mediaRawWeights = backfillMediaRawWeights,
            compensatedMediaShares = backfillCompensatedMediaShares,
            compensatedMediaRawWeights = backfillCompensatedMediaRawWeights,
            deviceShares = backfillDeviceShares,
            deviceRawWeights = backfillDeviceRawWeights
        },
        storeTopUp = {
            mediaShares = storeMediaShares,
            deviceBias = storeDeviceBias,
            totalMediaBudget = storeTotalMediaBudget
        }
    }

    return {
        routes = routes,
        profiles = buildProfiles(routes, lootPolicy, distroPatchStats),
        targetDiagnostics = {
            musicStoreTargets = MUSIC_STORE_TARGET_ORDER
        }
    }
end

return authority
