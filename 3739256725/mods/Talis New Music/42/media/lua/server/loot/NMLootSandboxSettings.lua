NMLootSandboxSettings = NMLootSandboxSettings or {}

local settings = NMLootSandboxSettings
local resolvePolicyCategoryRate = nil

settings.BASE_MEDIA_DEFAULT = 0.6
settings.BASE_DEVICE_DEFAULT = 0.6
settings.BASELINE_FEEL_NO_BOOST_RATE = 0.1
settings.MEDIA_BASELINE_FEEL_MULTIPLIER = 1.0
settings.MEDIA_ROUTE_BASELINE_TARGET_RATE = 2.4
settings.DEVICE_BASELINE_FEEL_MULTIPLIER = 1.8
settings.DEVICE_INTENSITY_MULTIPLIER = 0.25
settings.VEHICLE_RESPONSE_BASE = 0.18
settings.VEHICLE_RESPONSE_EXPONENT = 0.72
settings.MEDIA_DIRECT_SPAWN_SCALE_BASE = 0.5
settings.DEVICE_DIRECT_SPAWN_SCALE_BASE = 0.125
settings.DEVICE_HIGH_RATE_MAX_MULTIPLIER = 2.0
settings.MEDIA_FALLBACK_DEFAULT_BUDGET = 0.055
settings.MEDIA_FALLBACK_LOW_RATE_EXPONENT = 1.35
settings.MEDIA_FALLBACK_HIGH_RATE_EXPONENT = 1.1
settings.MEDIA_FALLBACK_HIGH_RATE_BUDGET_MAX = 3.75
settings.MEDIA_HIGH_RATE_RICHNESS_EXPONENT = 0.9
settings.MEDIA_STANDARD_PROCEDURAL_ABUNDANCE_MAX = 6.5
settings.MEDIA_STANDARD_VEHICLE_ABUNDANCE_MAX = 4.0
settings.MEDIA_STORE_TOPUP_BUDGET_MAX = 5.75
settings.MEDIA_UNIFIED_POOL_SIZE_EXPONENT = 0.95
settings.MEDIA_STORE_BIAS_TINY_POOL_EXPONENT = 1.15
settings.MUSIC_STORE_TOPUP_REFERENCE_RATE = 0.6
settings.MUSIC_STORE_TOPUP_DECAY_EXPONENT = 1.35

settings.CATEGORY_ORDER = {
    "cassettes",
    "vinyl",
    "cds",
    "walkman",
    "boombox",
    "cdplayer",
    "recordplayer"
}

settings.MEDIA_CATEGORY_ORDER = {
    "cassettes",
    "vinyl",
    "cds"
}

settings.DEVICE_CATEGORY_ORDER = {
    "walkman",
    "boombox",
    "cdplayer",
    "recordplayer"
}

settings.VEHICLE_ROLE_ORDER = {
    "glovebox",
    "seatrear",
    "cargo"
}

settings.DEVICE_SCALAR_PROFILES = {
    walkman = {
        baseScale = settings.DEVICE_DIRECT_SPAWN_SCALE_BASE,
        lowRateBonus = 1.00,
        highRateBonus = 1.00
    },
    boombox = {
        baseScale = settings.DEVICE_DIRECT_SPAWN_SCALE_BASE,
        lowRateBonus = 1.20,
        highRateBonus = 2.15
    },
    cdplayer = {
        baseScale = settings.DEVICE_DIRECT_SPAWN_SCALE_BASE,
        lowRateBonus = 1.08,
        highRateBonus = 2.20
    },
    recordplayer = {
        baseScale = 0.29,
        lowRateBonus = 1.15,
        highRateBonus = 1.90
    }
}

settings.MEDIA_UNIFIED_CATEGORY_BIAS = {
    cassettes = 1.15,
    vinyl = 0.82,
    cds = 1.00
}

settings.MEDIA_BASELINE_TARGET_SHARES = {
    cassettes = 1.0,
    vinyl = 1.0,
    cds = 1.0
}

settings.MEDIA_BASELINE_BALANCE_MAX_RATE = settings.BASE_MEDIA_DEFAULT

settings.MEDIA_HIGH_RATE_SHARE_COMPENSATION = {
    cassettes = 1.00,
    vinyl = 1.55,
    cds = 1.00
}

settings.DEVICE_UNIFIED_CATEGORY_BIAS = {
    walkman = 1.00,
    boombox = 0.92,
    cdplayer = 0.90,
    recordplayer = 0.86
}

settings.MUSIC_STORE_MEDIA_MIN_PRESENCE_UNITS = {
    cassettes = 0.0,
    vinyl = 3.0,
    cds = 1.0
}

settings.MEDIA_REPRESENTATIVE_PROCEDURAL_LANE_MULTIPLIERS = {
    cassettes = 1.10,
    vinyl = 1.10,
    cds = 1.10
}

settings.MEDIA_REPRESENTATIVE_MAIL_LANE_MULTIPLIERS = {
    cassettes = 1.05,
    vinyl = 1.05,
    cds = 1.05
}

settings.MUSIC_STORE_TARGET_ORDER = {
    "MusicStoreCases",
    "MusicStoreCDs",
    "MusicStoreShelves",
    "MusicStoreCounter",
    "MusicStoreSpeaker",
    "MusicStoreOthers"
}

settings.MUSIC_STORE_PROCEDURAL_WEIGHT_OVERRIDES = {
    cassettes = {
        MusicStoreCDs = 14.0,
        MusicStoreShelves = 10.0,
        MusicStoreCounter = 8.0,
        MusicStoreSpeaker = 7.0,
        MusicStoreOthers = 6.0
    },
    vinyl = {
        MusicStoreCases = 32.0,
        MusicStoreCDs = 24.0,
        MusicStoreShelves = 30.0,
        MusicStoreCounter = 18.0,
        MusicStoreSpeaker = 16.0
    },
    cds = {
        MusicStoreCDs = 15.0,
        MusicStoreShelves = 11.0,
        MusicStoreCounter = 9.0,
        MusicStoreSpeaker = 7.0,
        MusicStoreOthers = 6.0
    },
    walkman = {
        MusicStoreCDs = 7.0,
        MusicStoreShelves = 8.0,
        MusicStoreCounter = 6.0,
        MusicStoreOthers = 6.0
    },
    boombox = {
        MusicStoreCDs = 4.0,
        MusicStoreShelves = 5.0,
        MusicStoreCounter = 4.0,
        MusicStoreOthers = 4.0
    },
    cdplayer = {
        MusicStoreCDs = 9.0,
        MusicStoreShelves = 10.0,
        MusicStoreCounter = 7.0
    },
    recordplayer = {
        MusicStoreCDs = 8.0,
        MusicStoreShelves = 12.0,
        MusicStoreCounter = 8.0,
        MusicStoreSpeaker = 7.0,
        MusicStoreOthers = 5.0
    }
}

settings.MUSIC_STORE_CATEGORY_FLOOR_BUDGETS = {
    cassettes = 0.75,
    vinyl = 1.20,
    cds = 0.75,
    walkman = 0.45,
    boombox = 0.30,
    cdplayer = 0.55,
    recordplayer = 0.75
}

settings.MUSIC_STORE_MEDIA_TOPUP_BIAS = {
    cassettes = 0.24,
    vinyl = 0.58,
    cds = 0.18
}

settings.MUSIC_STORE_DEVICE_TOPUP_BIAS = {
    walkman = 0.18,
    boombox = 0.12,
    cdplayer = 0.10,
    recordplayer = 0.60
}

settings.MUSIC_STORE_TOPUP_TARGET_FLOORS = {
    cassettes = 8.0,
    vinyl = 26.0,
    cds = 8.0,
    walkman = 5.0,
    boombox = 2.5,
    cdplayer = 2.0,
    recordplayer = 10.0
}

settings.VANILLA_MEDIA_BACKFILL_ENTRY_DIVISOR = 60.0
settings.VANILLA_DEVICE_BACKFILL_ENTRY_DIVISOR = 65.0
settings.VANILLA_MEDIA_BACKFILL_SCALE = 1.75
settings.VANILLA_DEVICE_BACKFILL_SCALE = 0.95
settings.VANILLA_BACKFILL_MAX_MULTIPLIER = 2.5

local function clamp(value, minValue, maxValue)
    local n = tonumber(value) or 0
    if n < minValue then
        return minValue
    end
    if n > maxValue then
        return maxValue
    end
    return n
end

local function pow(base, exponent)
    if math and math.pow then
        return math.pow(base, exponent)
    end
    return (tonumber(base) or 0) ^ (tonumber(exponent) or 0)
end

local function buildMatchedRateKey(rate)
    return string.format("%.4f", tonumber(rate) or 0)
end

local function applyMatchedRateTargetShareBalance(order, weights, rates, poolSizes, targetShares, maxBalancedRate)
    if type(targetShares) ~= "table" then
        return weights
    end

    local maxRate = tonumber(maxBalancedRate) or settings.BASE_MEDIA_DEFAULT
    local groups = {}
    for i = 1, #(order or {}) do
        local category = order[i]
        local weight = tonumber(weights and weights[category]) or 0
        local rate = tonumber(rates and rates[category]) or 0
        local poolSize = tonumber(poolSizes and poolSizes[category]) or 0
        if weight > 0 and rate > 0 and rate <= maxRate and poolSize > 0 then
            local key = buildMatchedRateKey(rate)
            groups[key] = groups[key] or {}
            groups[key][#groups[key] + 1] = category
        end
    end

    for _, group in pairs(groups) do
        if #group > 1 then
            local groupWeightTotal = 0
            local targetTotal = 0
            for i = 1, #group do
                local category = group[i]
                groupWeightTotal = groupWeightTotal + (tonumber(weights[category]) or 0)
                targetTotal = targetTotal + math.max(0, tonumber(targetShares[category]) or 0)
            end
            if groupWeightTotal > 0 and targetTotal > 0 then
                for i = 1, #group do
                    local category = group[i]
                    local targetShare = math.max(0, tonumber(targetShares[category]) or 0)
                    weights[category] = groupWeightTotal * (targetShare / targetTotal)
                end
            end
        end
    end

    return weights
end

local function resolveBaselineFeelMultiplier(rate, baseRate, multiplier)
    local r = tonumber(rate) or 0
    local base = tonumber(baseRate) or 0
    local boost = tonumber(multiplier) or 1.0
    local floorRate = clamp(tonumber(settings.BASELINE_FEEL_NO_BOOST_RATE) or 0, 0.0, base)
    if r <= 0 or base <= 0 or boost <= 1.0 or r > base then
        return 1.0
    end
    if r <= floorRate or floorRate >= base then
        return 1.0
    end
    local t = clamp((r - floorRate) / (base - floorRate), 0.0, 1.0)
    return 1.0 + ((boost - 1.0) * t)
end

local function resolveEffectiveMediaRouteRate(rate)
    local r = clamp(rate, 0.0, 4.0)
    if r <= 0 then
        return 0
    end

    local baseRate = math.max(0.0001, tonumber(settings.BASE_MEDIA_DEFAULT) or 0.6)
    local floorRate = clamp(tonumber(settings.BASELINE_FEEL_NO_BOOST_RATE) or 0.1, 0.0, baseRate)
    local targetRate = clamp(tonumber(settings.MEDIA_ROUTE_BASELINE_TARGET_RATE) or baseRate, baseRate, 4.0)

    if r <= floorRate or targetRate <= baseRate then
        return r
    end

    if r <= baseRate then
        local t = clamp((r - floorRate) / math.max(0.0001, baseRate - floorRate), 0.0, 1.0)
        return floorRate + ((targetRate - floorRate) * t)
    end

    local t = clamp((r - baseRate) / math.max(0.0001, 4.0 - baseRate), 0.0, 1.0)
    return targetRate + ((4.0 - targetRate) * t)
end

local function resolveSandboxPage()
    return type(SandboxVars) == "table" and type(SandboxVars.NewMusic) == "table" and SandboxVars.NewMusic or nil
end

function settings.clamp(value, minValue, maxValue)
    return clamp(value, minValue, maxValue)
end

function settings.pow(base, exponent)
    return pow(base, exponent)
end

function settings.getRawSandboxLootSettings()
    local page = resolveSandboxPage()
    return {
        cassettes = page and page.CassettesSpawnRate or nil,
        vinyl = page and page.VinylRecordsSpawnRate or nil,
        cds = page and page.CDsSpawnRate or nil,
        walkman = page and page.WalkmanSpawnRate or nil,
        boombox = page and page.BoomboxSpawnRate or nil,
        cdplayer = page and page.CDPlayerSpawnRate or nil,
        recordplayer = page and page.RecordPlayerSpawnRate or nil,
        _pagePresent = page ~= nil
    }
end

function settings.setPolicyRateResolver(resolver)
    if type(resolver) == "function" then
        resolvePolicyCategoryRate = resolver
        return
    end
    resolvePolicyCategoryRate = nil
end

function settings.resolveLootBuildZomboidOSTSetting()
    local page = resolveSandboxPage()
    local rawValue = page and page.ZomboidOST or nil
    local resolvedValue = false
    local state = "missing"

    if rawValue == true or rawValue == false then
        resolvedValue = rawValue == true
        state = resolvedValue and "enabled" or "disabled"
    else
        local asNumber = tonumber(rawValue)
        if asNumber ~= nil then
            resolvedValue = asNumber ~= 0
            state = resolvedValue and "enabled" or "disabled"
        else
            local text = tostring(rawValue or ""):lower()
            if text == "true" or text == "yes" or text == "on" then
                resolvedValue = true
                state = "enabled"
            elseif text == "false" or text == "no" or text == "off" then
                resolvedValue = false
                state = "disabled"
            end
        end
    end

    return {
        pagePresent = page ~= nil,
        keyPresent = rawValue ~= nil,
        rawValue = rawValue,
        enabled = resolvedValue,
        state = state
    }
end

function settings.resolveCategoryRate(category, lootPolicy)
    if type(resolvePolicyCategoryRate) == "function" then
        local overridden = resolvePolicyCategoryRate(lootPolicy, category)
        if overridden ~= nil then
            return overridden
        end
    end
    if type(lootPolicy) == "table" and type(lootPolicy.rates) == "table" then
        local key = tostring(category or "")
        if lootPolicy.rates[key] ~= nil then
            return tonumber(lootPolicy.rates[key]) or 0
        end
    end
    if category == "cassettes" then
        return NMRuntimeConfig.getCassettesSpawnRate() or settings.BASE_MEDIA_DEFAULT
    end
    if category == "vinyl" then
        return NMRuntimeConfig.getVinylRecordsSpawnRate() or settings.BASE_MEDIA_DEFAULT
    end
    if category == "cds" then
        return NMRuntimeConfig.getCDsSpawnRate() or settings.BASE_MEDIA_DEFAULT
    end
    if category == "walkman" then
        return NMRuntimeConfig.getWalkmanSpawnRate() or settings.BASE_DEVICE_DEFAULT
    end
    if category == "boombox" then
        return NMRuntimeConfig.getBoomboxSpawnRate() or settings.BASE_DEVICE_DEFAULT
    end
    if category == "cdplayer" then
        return NMRuntimeConfig.getCDPlayerSpawnRate() or settings.BASE_DEVICE_DEFAULT
    end
    if category == "recordplayer" then
        return NMRuntimeConfig.getRecordPlayerSpawnRate() or settings.BASE_DEVICE_DEFAULT
    end
    return settings.BASE_MEDIA_DEFAULT
end

function settings.resolveCategoryMultiplier(category)
    local rate = clamp(settings.resolveCategoryRate(category), 0.0, 4.0)
    local baseRate = settings.BASE_MEDIA_DEFAULT
    local baselineFeel = settings.MEDIA_BASELINE_FEEL_MULTIPLIER
    if category == "walkman" or category == "boombox" or category == "cdplayer" or category == "recordplayer" then
        baseRate = settings.BASE_DEVICE_DEFAULT
        baselineFeel = settings.DEVICE_BASELINE_FEEL_MULTIPLIER
    else
        rate = resolveEffectiveMediaRouteRate(rate)
    end
    if tonumber(baseRate) == nil or baseRate <= 0 then
        baseRate = settings.BASE_MEDIA_DEFAULT
    end
    return (rate / baseRate) * resolveBaselineFeelMultiplier(rate, baseRate, baselineFeel)
end

function settings.resolveDirectSpawnScalar(rate, defaultRate, baseScale)
    local r = clamp(rate, 0.0, 4.0)
    if r <= 0 then
        return 0
    end
    local base = tonumber(defaultRate) or settings.BASE_MEDIA_DEFAULT
    if base <= 0 then
        base = settings.BASE_MEDIA_DEFAULT
    end
    return (tonumber(baseScale) or 0.25) * (r / base)
end

function settings.resolveStandardDeviceScalar(category, rate)
    local categoryKey = tostring(category or "")
    local profile = settings.DEVICE_SCALAR_PROFILES[categoryKey] or nil
    local baseScale = tonumber(profile and profile.baseScale) or settings.DEVICE_DIRECT_SPAWN_SCALE_BASE
    local scalar = settings.resolveDirectSpawnScalar(rate, settings.BASE_DEVICE_DEFAULT, baseScale)
    if rate > 0 and rate <= settings.BASE_DEVICE_DEFAULT then
        local baselineBonus = tonumber(profile and profile.lowRateBonus) or 1.0
        scalar = scalar * baselineBonus
    end
    if rate > settings.BASE_DEVICE_DEFAULT then
        local highRateBonus = tonumber(profile and profile.highRateBonus) or 1.0
        if highRateBonus ~= 1.0 then
            local highRateT = clamp((rate - settings.BASE_DEVICE_DEFAULT) / (4.0 - settings.BASE_DEVICE_DEFAULT), 0.0, 1.0)
            scalar = scalar * (1.0 + ((highRateBonus - 1.0) * highRateT))
        end
        local highRateT = clamp((rate - settings.BASE_DEVICE_DEFAULT) / (4.0 - settings.BASE_DEVICE_DEFAULT), 0.0, 1.0)
        local maxMultiplier = math.max(1.0, tonumber(settings.DEVICE_HIGH_RATE_MAX_MULTIPLIER) or 1.0)
        scalar = scalar * (1.0 + ((maxMultiplier - 1.0) * highRateT))
    end
    scalar = scalar * resolveBaselineFeelMultiplier(rate, settings.BASE_DEVICE_DEFAULT, settings.DEVICE_BASELINE_FEEL_MULTIPLIER)
    return scalar
end

function settings.resolveMediaFallbackBudgetScalar(rate)
    local r = resolveEffectiveMediaRouteRate(rate)
    if r <= 0 then
        return 0
    end

    if r <= settings.BASE_MEDIA_DEFAULT then
        local normalized = r / settings.BASE_MEDIA_DEFAULT
        return settings.MEDIA_FALLBACK_DEFAULT_BUDGET
            * pow(normalized, settings.MEDIA_FALLBACK_LOW_RATE_EXPONENT)
            * resolveBaselineFeelMultiplier(r, settings.BASE_MEDIA_DEFAULT, settings.MEDIA_BASELINE_FEEL_MULTIPLIER)
    end

    local maxScalar = settings.resolveDirectSpawnScalar(4.0, settings.BASE_MEDIA_DEFAULT, settings.MEDIA_DIRECT_SPAWN_SCALE_BASE)
    local t = (r - settings.BASE_MEDIA_DEFAULT) / (4.0 - settings.BASE_MEDIA_DEFAULT)
    local scalar = settings.MEDIA_FALLBACK_DEFAULT_BUDGET
        + ((maxScalar - settings.MEDIA_FALLBACK_DEFAULT_BUDGET) * pow(t, settings.MEDIA_FALLBACK_HIGH_RATE_EXPONENT))
    return scalar * settings.scaleMediaHighRateMultiplier(rate, settings.MEDIA_FALLBACK_HIGH_RATE_BUDGET_MAX)
end

function settings.isMediaCategory(category)
    local key = tostring(category or "")
    return key == "cassettes" or key == "vinyl" or key == "cds"
end

function settings.resolveMediaHighRateRichness(rate)
    local r = clamp(rate, 0.0, 4.0)
    if r <= settings.BASE_MEDIA_DEFAULT then
        return 0
    end
    local t = (r - settings.BASE_MEDIA_DEFAULT) / (4.0 - settings.BASE_MEDIA_DEFAULT)
    return pow(clamp(t, 0.0, 1.0), settings.MEDIA_HIGH_RATE_RICHNESS_EXPONENT)
end

function settings.scaleMediaHighRateMultiplier(rate, maxMultiplier)
    local richness = settings.resolveMediaHighRateRichness(rate)
    return 1.0 + ((tonumber(maxMultiplier) or 1.0) - 1.0) * richness
end

function settings.resolveStandardMediaProceduralAbundanceBoost(rate)
    return settings.scaleMediaHighRateMultiplier(rate, settings.MEDIA_STANDARD_PROCEDURAL_ABUNDANCE_MAX)
end

function settings.resolveStandardMediaVehicleAbundanceBoost(rate)
    return settings.scaleMediaHighRateMultiplier(rate, settings.MEDIA_STANDARD_VEHICLE_ABUNDANCE_MAX)
end

function settings.resolveMediaStoreTopUpBudgetBoost(rate)
    return settings.scaleMediaHighRateMultiplier(rate, settings.MEDIA_STORE_TOPUP_BUDGET_MAX)
end

function settings.resolveMediaHighRateShareCompensation(category, rate)
    local key = tostring(category or "")
    if settings.isMediaCategory(key) ~= true then
        return 1.0
    end
    if clamp(rate, 0.0, 4.0) <= settings.BASE_MEDIA_DEFAULT then
        return 1.0
    end
    local maxCompensation = tonumber(settings.MEDIA_HIGH_RATE_SHARE_COMPENSATION[key]) or 1.0
    if maxCompensation <= 1.0 then
        return 1.0
    end
    local richness = settings.resolveMediaHighRateRichness(rate)
    if richness <= 0 then
        return 1.0
    end
    return 1.0 + ((maxCompensation - 1.0) * richness)
end

function settings.computeUnifiedCategoryShares(order, poolResolver, rateResolver, biasMap, weightMultiplierResolver, targetShareMap, maxBalancedRate)
    local weights = {}
    local rates = {}
    local poolSizes = {}
    local total = 0
    for i = 1, #(order or {}) do
        local category = order[i]
        local rate = clamp(tonumber(rateResolver and rateResolver(category)) or 0, 0.0, 4.0)
        local poolSize = tonumber(poolResolver and poolResolver(category)) or 0
        local weight = 0
        if rate > 0 and poolSize > 0 then
            local bias = tonumber(biasMap and biasMap[category]) or 0
            local extraMultiplier = tonumber(weightMultiplierResolver and weightMultiplierResolver(category, rate, poolSize) or 1.0) or 1.0
            weight = rate * bias * pow(poolSize, settings.MEDIA_UNIFIED_POOL_SIZE_EXPONENT) * extraMultiplier
        end
        weights[category] = weight
        rates[category] = rate
        poolSizes[category] = poolSize
    end

    weights = applyMatchedRateTargetShareBalance(order, weights, rates, poolSizes, targetShareMap, maxBalancedRate)

    for i = 1, #(order or {}) do
        local category = order[i]
        total = total + (tonumber(weights[category]) or 0)
    end

    local shares = {}
    for i = 1, #(order or {}) do
        local category = order[i]
        if total > 0 then
            shares[category] = (tonumber(weights[category]) or 0) / total
        else
            shares[category] = 0
        end
    end
    return shares, weights, total
end

function settings.computeUnifiedMediaCategoryShares(mediaPool, orderedUnitsForCategory, lootPolicy)
    return settings.computeUnifiedCategoryShares(
        settings.MEDIA_CATEGORY_ORDER,
        function(category)
            return #orderedUnitsForCategory(mediaPool, category)
        end,
        function(category)
            return resolveEffectiveMediaRouteRate(settings.resolveCategoryRate(category, lootPolicy))
        end,
        settings.MEDIA_UNIFIED_CATEGORY_BIAS,
        nil,
        settings.MEDIA_BASELINE_TARGET_SHARES,
        resolveEffectiveMediaRouteRate(settings.MEDIA_BASELINE_BALANCE_MAX_RATE)
    )
end

function settings.computeStandardBackfillMediaCategoryShares(mediaPool, orderedUnitsForCategory, lootPolicy)
    return settings.computeUnifiedCategoryShares(
        settings.MEDIA_CATEGORY_ORDER,
        function(category)
            return #orderedUnitsForCategory(mediaPool, category)
        end,
        function(category)
            return resolveEffectiveMediaRouteRate(settings.resolveCategoryRate(category, lootPolicy))
        end,
        settings.MEDIA_UNIFIED_CATEGORY_BIAS,
        function(category)
            local rawRate = settings.resolveCategoryRate(category, lootPolicy)
            return settings.resolveMediaHighRateShareCompensation(category, rawRate)
        end,
        settings.MEDIA_BASELINE_TARGET_SHARES,
        resolveEffectiveMediaRouteRate(settings.MEDIA_BASELINE_BALANCE_MAX_RATE)
    )
end

function settings.computeUnifiedDeviceCategoryShares(devicePool, orderedUnitsForCategory, lootPolicy)
    return settings.computeUnifiedCategoryShares(
        settings.DEVICE_CATEGORY_ORDER,
        function(category)
            return #orderedUnitsForCategory(devicePool, category)
        end,
        function(category)
            return settings.resolveCategoryRate(category, lootPolicy)
        end,
        settings.DEVICE_UNIFIED_CATEGORY_BIAS
    )
end

function settings.computeMusicStoreMediaShares(mediaPool, orderedUnitsForCategory, lootPolicy)
    local baseShares = settings.computeUnifiedMediaCategoryShares(mediaPool, orderedUnitsForCategory, lootPolicy)
    local weights = {}
    local total = 0
    for i = 1, #settings.MEDIA_CATEGORY_ORDER do
        local category = settings.MEDIA_CATEGORY_ORDER[i]
        local share = tonumber(baseShares[category]) or 0
        local poolSize = #orderedUnitsForCategory(mediaPool, category)
        local bias = tonumber(settings.MUSIC_STORE_MEDIA_TOPUP_BIAS[category]) or 0
        local tinyPoolScale = 0
        if poolSize > 0 then
            tinyPoolScale = pow(math.min(poolSize, 8) / 8, settings.MEDIA_STORE_BIAS_TINY_POOL_EXPONENT)
        end
        local weight = share * (1.0 + (bias * tinyPoolScale))
        weights[category] = weight
        total = total + weight
    end

    local shares = {}
    for i = 1, #settings.MEDIA_CATEGORY_ORDER do
        local category = settings.MEDIA_CATEGORY_ORDER[i]
        if total > 0 then
            shares[category] = (tonumber(weights[category]) or 0) / total
        else
            shares[category] = 0
        end
    end
    return shares
end

function settings.computeTotalMediaBudget(mediaPool, orderedUnitsForCategory, scalarResolver)
    local total = 0
    for i = 1, #settings.MEDIA_CATEGORY_ORDER do
        local category = settings.MEDIA_CATEGORY_ORDER[i]
        if #orderedUnitsForCategory(mediaPool, category) > 0 then
            total = total + math.max(0, tonumber(scalarResolver(category)) or 0)
        end
    end
    return total
end

function settings.normalizePlayableRate(rate)
    local r = clamp(rate, 0.0, 4.0)
    if r <= 0 then
        return 0
    end
    if r <= settings.BASE_MEDIA_DEFAULT then
        return clamp(
            (r / settings.BASE_MEDIA_DEFAULT)
                * settings.VEHICLE_RESPONSE_BASE
                * resolveBaselineFeelMultiplier(r, settings.BASE_MEDIA_DEFAULT, settings.MEDIA_BASELINE_FEEL_MULTIPLIER),
            0.0,
            1.0
        )
    end
    local t = (r - settings.BASE_MEDIA_DEFAULT) / (4.0 - settings.BASE_MEDIA_DEFAULT)
    return clamp(
        settings.VEHICLE_RESPONSE_BASE + ((1.0 - settings.VEHICLE_RESPONSE_BASE) * pow(t, settings.VEHICLE_RESPONSE_EXPONENT)),
        0.0,
        1.0
    )
end

return settings
