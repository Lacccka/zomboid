require "loot/NMLootContainerClassifier"

NMServerSPLootInvestigation = NMServerSPLootInvestigation or {}

local probe = NMServerSPLootInvestigation

local CATEGORY_ORDER = {
    "cassettes",
    "vinyl",
    "cds",
    "walkman",
    "boombox",
    "cdplayer",
    "recordplayer"
}
local MEDIA_CATEGORY_ORDER = { "cassettes", "vinyl", "cds" }
local CLASS_ORDER = {
    "residential_misc",
    "music_store",
    "electronics",
    "mail",
    "vehicle_glovebox",
    "vehicle_seatrear",
    "vehicle_cargo",
    "other"
}

local SUMMARY_STEP = 25
local FIRST_HOUSE_THRESHOLD = 12
local SUSPICIOUS_DETAIL_LIMIT = 8

local function toLower(value)
    return string.lower(tostring(value or ""))
end

local skippedContainerLogs = {}

local function isKnownNonMutableContainerShape(container)
    local text = tostring(container or "")
    if text == "" then
        return false
    end
    return string.find(text, "ItemPickerJava$ItemPickerContainer@", 1, true) ~= nil
end

local function logSkippedContainer(container, reason)
    if not (NMCore and NMCore.logChannel and NMCore.isDebugKnobOn and NMCore.isDebugKnobOn("lootDiagnostics") == true) then
        return
    end
    local shape = tostring(container or "nil")
    local key = tostring(reason or "unknown") .. "|" .. shape
    if skippedContainerLogs[key] then
        return
    end
    skippedContainerLogs[key] = true
    NMCore.logChannel(
        "lootDiagnostics",
        "sp_loot.skip_container",
        string.format("reason=%s shape=%s", tostring(reason or "unknown"), shape)
    )
end

local function safeCallMethod(target, methodName, ...)
    if target == nil then
        return nil
    end
    local args = { ... }
    local ok, result = pcall(function()
        local method = target[methodName]
        if type(method) ~= "function" then
            return nil
        end
        return method(target, unpack(args))
    end)
    if ok then
        return result
    end
    return nil
end

local function resolveContainerItems(container)
    if isKnownNonMutableContainerShape(container) then
        logSkippedContainer(container, "item_picker_container")
        return nil
    end

    local items = safeCallMethod(container, "getItems")
    if items and items.size and items.get then
        return items
    end

    local unwrapMethods = {
        "getActualContainer",
        "getContainer",
        "getInventory",
        "getItemContainer",
        "getActual"
    }
    for i = 1, #unwrapMethods do
        local resolved = safeCallMethod(container, unwrapMethods[i])
        if resolved ~= nil and resolved ~= container then
            if isKnownNonMutableContainerShape(resolved) then
                logSkippedContainer(resolved, "item_picker_container_unwrapped")
            else
                items = safeCallMethod(resolved, "getItems")
                if items and items.size and items.get then
                    return items
                end
            end
        end
    end

    return nil
end

local function newCategoryCounts()
    return {
        cassettes = 0,
        vinyl = 0,
        cds = 0,
        walkman = 0,
        boombox = 0,
        cdplayer = 0,
        recordplayer = 0
    }
end

local function newObservedSourceSummary()
    return {
        standard = { total = 0, byCategory = newCategoryCounts() },
        globalBackfill = { total = 0, byCategory = newCategoryCounts() },
        storeTopUp = { total = 0, byCategory = newCategoryCounts() }
    }
end

local function newClassSummary()
    return {
        observed = 0,
        anyMusic = 0,
        multiMusic = 0,
        one = 0,
        two = 0,
        threePlus = 0,
        totalManagedItems = 0,
        byCategory = newCategoryCounts(),
        visibleByCategory = newCategoryCounts(),
        observedSources = newObservedSourceSummary(),
        byFullType = {}
    }
end

local function newState()
    local classStats = {}
    for i = 1, #CLASS_ORDER do
        classStats[CLASS_ORDER[i]] = newClassSummary()
    end
    return {
        configured = false,
        headerLogged = false,
        firstHouseLogged = false,
        observedEligible = 0,
        suspiciousDetailCount = 0,
        rawRates = {},
        preset = "",
        managedMediaMap = {},
        managedMediaCounts = newCategoryCounts(),
        fallbackMediaCounts = newCategoryCounts(),
        coverageProvider = nil,
        conversionProvider = nil,
        classStats = classStats,
        nonStoreObservedSources = newObservedSourceSummary(),
        seenContainers = setmetatable({}, { __mode = "k" })
    }
end

local state = newState()

local function shouldLog()
    return NMCore and NMCore.isDebugKnobOn and NMCore.isDebugKnobOn("lootDiagnostics") == true
end

local function isSinglePlayerRuntime()
    return not (NMCore and NMCore.isMultiplayerMode and NMCore.isMultiplayerMode() == true)
end

local function formatCategoryCounts(counts)
    local parts = {}
    for i = 1, #CATEGORY_ORDER do
        local key = CATEGORY_ORDER[i]
        parts[#parts + 1] = key .. "=" .. tostring(tonumber(counts and counts[key]) or 0)
    end
    return table.concat(parts, " ")
end

local function formatFullTypeCounts(counts)
    local entries = {}
    for fullType, count in pairs(counts or {}) do
        local n = tonumber(count) or 0
        if n > 0 then
            entries[#entries + 1] = {
                fullType = tostring(fullType or ""),
                count = n
            }
        end
    end
    table.sort(entries, function(a, b)
        if a.count ~= b.count then
            return a.count > b.count
        end
        return a.fullType < b.fullType
    end)
    local parts = {}
    for i = 1, #entries do
        parts[#parts + 1] = string.format("%s=%s", entries[i].fullType, tostring(entries[i].count))
    end
    return table.concat(parts, " ")
end

local function logLoot(tag, detail)
    if NMCore and NMCore.logChannel then
        NMCore.logChannel("lootDiagnostics", tostring(tag or "sp_loot"), tostring(detail or ""))
    end
end

local function formatCoverageSummary()
    local provider = state.coverageProvider
    if type(provider) ~= "function" then
        return "none"
    end
    local snapshot = provider()
    if type(snapshot) ~= "table" then
        return "none"
    end

    local parts = {}
    for i = 1, #MEDIA_CATEGORY_ORDER do
        local category = MEDIA_CATEGORY_ORDER[i]
        local stats = snapshot[category] or {}
        parts[#parts + 1] = string.format(
            "%s=%s/%s/%s/%s",
            tostring(category),
            tostring(tonumber(stats.discovered) or 0),
            tostring(tonumber(stats.total) or 0),
            tostring(tonumber(stats.resolved) or 0),
            tostring(tonumber(stats.repeats) or 0)
        )
    end
    local replacement = snapshot.replacementStats or {}
    parts[#parts + 1] = string.format(
        "replace=%s/%s",
        tostring(tonumber(replacement.replaced) or 0),
        tostring(tonumber(replacement.failed) or 0)
    )
    return table.concat(parts, " ")
end

local function formatConversionSummary()
    local provider = state.conversionProvider
    if type(provider) ~= "function" then
        return "none"
    end
    local snapshot = provider()
    if type(snapshot) ~= "table" then
        return "none"
    end
    return string.format(
        "mode=%s cds=%s/%s cdplayers=%s/%s",
        tostring(snapshot.mode or "unknown"),
        tostring(tonumber(snapshot.convertedVanillaCDs) or 0),
        tostring(tonumber(snapshot.failedVanillaCDs) or 0),
        tostring(tonumber(snapshot.convertedVanillaCDPlayers) or 0),
        tostring(tonumber(snapshot.failedVanillaCDPlayers) or 0)
    )
end

local function formatObservedSourceSummary(snapshot)
    local standard = snapshot and snapshot.standard or {}
    local backfill = snapshot and snapshot.globalBackfill or {}
    local topup = snapshot and snapshot.storeTopUp or {}
    return string.format(
        "normal={total=%s byCategory={%s}} backfill={total=%s byCategory={%s}} topup={total=%s byCategory={%s}}",
        tostring(tonumber(standard.total) or 0),
        formatCategoryCounts(standard.byCategory or {}),
        tostring(tonumber(backfill.total) or 0),
        formatCategoryCounts(backfill.byCategory or {}),
        tostring(tonumber(topup.total) or 0),
        formatCategoryCounts(topup.byCategory or {})
    )
end

local function summarizeClassStats()
    local parts = {}
    for i = 1, #CLASS_ORDER do
        local key = CLASS_ORDER[i]
        local record = state.classStats[key]
        if record and record.observed > 0 then
            parts[#parts + 1] = string.format(
                "%s=%s/%s/%s",
                tostring(key),
                tostring(record.observed),
                tostring(record.anyMusic),
                tostring(record.multiMusic)
            )
        end
    end
    return table.concat(parts, " ")
end

local function categoryCountsDiffer(left, right)
    for i = 1, #CATEGORY_ORDER do
        local key = CATEGORY_ORDER[i]
        if (tonumber(left and left[key]) or 0) ~= (tonumber(right and right[key]) or 0) then
            return true
        end
    end
    return false
end

local function logSessionHeader()
    if state.headerLogged or not shouldLog() then
        return
    end
    state.headerLogged = true
    logLoot(
        "sp_loot.session",
        string.format(
            "preset=%s rates={%s} managedMedia={%s} fallbackPool={%s} coverage={%s} conversions={%s}",
            tostring(state.preset ~= "" and state.preset or "quiet"),
            tostring(state.rawRatesText or ""),
            formatCategoryCounts(state.managedMediaCounts),
            formatCategoryCounts(state.fallbackMediaCounts),
            formatCoverageSummary(),
            formatConversionSummary()
        )
    )
end

local function logAggregateSummary(tag)
    if not shouldLog() then
        return
    end
    local residential = state.classStats.residential_misc or newClassSummary()
    logLoot(
        tag,
        string.format(
            "observed=%s residential=%s/%s/%s classes={%s} coverage={%s} conversions={%s}",
            tostring(state.observedEligible),
            tostring(residential.observed),
            tostring(residential.anyMusic),
            tostring(residential.multiMusic),
            summarizeClassStats(),
            formatCoverageSummary(),
            formatConversionSummary()
        )
    )
    logLoot(
        "sp_loot.global_backfill_observed",
        string.format(
            "sources={%s}",
            formatObservedSourceSummary(state.nonStoreObservedSources)
        )
    )
    local musicStore = state.classStats.music_store or nil
    if musicStore and musicStore.observed > 0 then
        logLoot(
            "sp_loot.music_store",
            string.format(
                "observed=%s anyMusic=%s multiMusic=%s totalManaged=%s one=%s two=%s threePlus=%s byCategory={%s} sources={%s}",
                tostring(musicStore.observed),
                tostring(musicStore.anyMusic),
                tostring(musicStore.multiMusic),
                tostring(musicStore.totalManagedItems),
                tostring(musicStore.one),
                tostring(musicStore.two),
                tostring(musicStore.threePlus),
                formatCategoryCounts(musicStore.byCategory),
                formatObservedSourceSummary(musicStore.observedSources)
            )
        )
        if categoryCountsDiffer(musicStore.visibleByCategory, musicStore.byCategory) then
            logLoot(
                "sp_loot.music_store_visible",
                string.format(
                    "observed=%s visibleByCategory={%s}",
                    tostring(musicStore.observed),
                    formatCategoryCounts(musicStore.visibleByCategory)
                )
            )
        end
    end
end

local function logFirstHouseSummary()
    if state.firstHouseLogged or not shouldLog() then
        return
    end
    local residential = state.classStats.residential_misc or nil
    if not residential or residential.observed < FIRST_HOUSE_THRESHOLD then
        return
    end
    state.firstHouseLogged = true
    logLoot(
        "sp_loot.first_house",
        string.format(
            "containers=%s anyMusic=%s multiMusic=%s one=%s two=%s threePlus=%s byCategory={%s} coverage={%s} conversions={%s}",
            tostring(residential.observed),
            tostring(residential.anyMusic),
            tostring(residential.multiMusic),
            tostring(residential.one),
            tostring(residential.two),
            tostring(residential.threePlus),
            formatCategoryCounts(residential.byCategory),
            formatCoverageSummary(),
            formatConversionSummary()
        )
    )
    logLoot(
        "sp_loot.first_house_items",
        string.format(
            "containers=%s items={%s}",
            tostring(residential.observed),
            formatFullTypeCounts(residential.byFullType)
        )
    )
end

local function recordObservedSource(summary, source, category)
    local sourceKey = tostring(source or "standard")
    local bucket = summary and summary[sourceKey] or nil
    if not bucket then
        return
    end
    bucket.total = (tonumber(bucket.total) or 0) + 1
    bucket.byCategory[category] = (tonumber(bucket.byCategory[category]) or 0) + 1
end

local function resolveObservedItemSource(item)
    local modData = item and item.getModData and item:getModData() or nil
    local source = type(modData) == "table" and tostring(modData.nmLootSource or "") or ""
    if source == "globalBackfill" or source == "storeTopUp" or source == "standard" then
        return source
    end
    return "standard"
end

local function resolveProfileCategory(profile)
    local deviceType = toLower(profile and profile.deviceType or "")
    if deviceType == "walkman" then
        return "walkman"
    end
    if deviceType == "boombox" then
        return "boombox"
    end
    if deviceType == "cdplayer" then
        return "cdplayer"
    end
    if deviceType == "vinylplayer" or deviceType == "recordplayer" then
        return "recordplayer"
    end
    return nil
end

local function resolveVisibleCategory(item, fullType)
    local category = state.managedLootMap[fullType]
    if category ~= nil then
        return category
    end

    if NMDeviceProfiles and NMDeviceProfiles.getForItem then
        local profileCategory = resolveProfileCategory(NMDeviceProfiles.getForItem(item))
        if profileCategory ~= nil then
            return profileCategory
        end
    end

    local lower = toLower(fullType)
    if string.find(lower, "walkman", 1, true) then
        return "walkman"
    end
    if string.find(lower, "boombox", 1, true) then
        return "boombox"
    end
    if string.find(lower, "cdplayer", 1, true) then
        return "cdplayer"
    end
    if string.find(lower, "vinylplayer", 1, true) or string.find(lower, "recordplayer", 1, true) then
        return "recordplayer"
    end
    if string.find(lower, "cassette", 1, true) and not string.find(lower, "case", 1, true) and not string.find(lower, "cover", 1, true) then
        return "cassettes"
    end
    if string.find(lower, "vinyl", 1, true) and not string.find(lower, "cover", 1, true) and not string.find(lower, "jacket", 1, true) then
        return "vinyl"
    end
    if lower == "cd" or lower == "nm_cd" then
        return "cds"
    end
    if string.find(lower, "cd", 1, true) and not string.find(lower, "cover", 1, true) and not string.find(lower, "case", 1, true) and not string.find(lower, "cdplayer", 1, true) then
        return "cds"
    end
    return nil
end

local function countManagedItemsInContainer(container)
    local counts = newCategoryCounts()
    local visibleCounts = newCategoryCounts()
    local observedSources = newObservedSourceSummary()
    local fullTypeCounts = {}
    local total = 0
    local items = resolveContainerItems(container)
    if not (items and items.size and items.get) then
        return counts, visibleCounts, total, observedSources, fullTypeCounts
    end
    for i = 0, items:size() - 1 do
        local item = items:get(i)
        local fullType = item and item.getFullType and tostring(item:getFullType() or "") or ""
        local visibleCategory = resolveVisibleCategory(item, fullType)
        if visibleCounts[visibleCategory] ~= nil then
            visibleCounts[visibleCategory] = (tonumber(visibleCounts[visibleCategory]) or 0) + 1
        end
        local category = state.managedLootMap[fullType]
        if counts[category] ~= nil then
            counts[category] = (tonumber(counts[category]) or 0) + 1
            fullTypeCounts[fullType] = (tonumber(fullTypeCounts[fullType]) or 0) + 1
            total = total + 1
            recordObservedSource(observedSources, resolveObservedItemSource(item), category)
        end
    end
    return counts, visibleCounts, total, observedSources, fullTypeCounts
end

local function maybeLogSuspiciousContainer(classKey, roomName, containerType, counts, total)
    if state.suspiciousDetailEnabled ~= true or not shouldLog() then
        return
    end
    if state.suspiciousDetailCount >= SUSPICIOUS_DETAIL_LIMIT then
        return
    end
    local cassetteRate = tonumber(state.rawRates and state.rawRates.cassettes) or 0
    local suspicious = total >= 2 or ((tonumber(counts.cassettes) or 0) > 0 and cassetteRate <= 0.05)
    if not suspicious then
        return
    end
    state.suspiciousDetailCount = state.suspiciousDetailCount + 1
    logLoot(
        "sp_loot.suspicious_container",
        string.format(
            "class=%s room=%s container=%s total=%s byCategory={%s}",
            tostring(classKey),
            tostring(roomName or ""),
            tostring(containerType or ""),
            tostring(total),
            formatCategoryCounts(counts)
        )
    )
end

function probe.configure(config)
    state = newState()
    state.configured = true
    state.preset = tostring(config and config.preset or "")
    state.rawRates = config and config.rawRates or {}
    state.rawRatesText = tostring(config and config.rawRatesText or "")
    state.managedMediaMap = config and config.managedMediaMap or {}
    state.managedMediaCounts = config and config.managedMediaCounts or newCategoryCounts()
    state.managedLootMap = config and config.managedLootMap or {}
    state.managedLootCounts = config and config.managedLootCounts or newCategoryCounts()
    state.fallbackMediaCounts = config and config.fallbackMediaCounts or newCategoryCounts()
    state.coverageProvider = config and config.coverageProvider or nil
    state.conversionProvider = config and config.conversionProvider or nil
    state.suspiciousDetailEnabled = config and config.suspiciousDetailEnabled == true
    logSessionHeader()
end

function probe.onFillContainer(roomName, containerType, container)
    if not state.configured or not shouldLog() or not isSinglePlayerRuntime() then
        return
    end
    if container ~= nil and state.seenContainers[container] then
        return
    end
    if container ~= nil then
        state.seenContainers[container] = true
    end

    local classKey = NMLootContainerClassifier
        and NMLootContainerClassifier.classifyContainer
        and NMLootContainerClassifier.classifyContainer(roomName, containerType, container)
        or "other"
    if classKey == "other" and not container then
        return
    end

    local record = state.classStats[classKey] or nil
    if not record then
        return
    end

    local counts, visibleCounts, total, observedSources, fullTypeCounts = countManagedItemsInContainer(container)
    record.observed = record.observed + 1
    state.observedEligible = state.observedEligible + 1
    for i = 1, #CATEGORY_ORDER do
        local key = CATEGORY_ORDER[i]
        record.visibleByCategory[key] = (tonumber(record.visibleByCategory[key]) or 0) + (tonumber(visibleCounts[key]) or 0)
    end
    if total > 0 then
        record.anyMusic = record.anyMusic + 1
        record.totalManagedItems = record.totalManagedItems + total
        if total == 1 then
            record.one = record.one + 1
        elseif total == 2 then
            record.two = record.two + 1
            record.multiMusic = record.multiMusic + 1
        else
            record.threePlus = record.threePlus + 1
            record.multiMusic = record.multiMusic + 1
        end
        for i = 1, #CATEGORY_ORDER do
            local key = CATEGORY_ORDER[i]
            record.byCategory[key] = (tonumber(record.byCategory[key]) or 0) + (tonumber(counts[key]) or 0)
        end
        for fullType, count in pairs(fullTypeCounts or {}) do
            record.byFullType[fullType] = (tonumber(record.byFullType[fullType]) or 0) + (tonumber(count) or 0)
        end
        for sourceKey, sourceRecord in pairs(observedSources) do
            for i = 1, #CATEGORY_ORDER do
                local key = CATEGORY_ORDER[i]
                local count = tonumber(sourceRecord.byCategory[key]) or 0
                if count > 0 then
                    record.observedSources[sourceKey].total = (tonumber(record.observedSources[sourceKey].total) or 0) + count
                    record.observedSources[sourceKey].byCategory[key] =
                        (tonumber(record.observedSources[sourceKey].byCategory[key]) or 0) + count
                    if classKey ~= "music_store" then
                        state.nonStoreObservedSources[sourceKey].total =
                            (tonumber(state.nonStoreObservedSources[sourceKey].total) or 0) + count
                        state.nonStoreObservedSources[sourceKey].byCategory[key] =
                            (tonumber(state.nonStoreObservedSources[sourceKey].byCategory[key]) or 0) + count
                    end
                end
            end
        end
        maybeLogSuspiciousContainer(classKey, roomName, containerType, counts, total)
    end

    logFirstHouseSummary()
    if state.observedEligible % SUMMARY_STEP == 0 then
        logAggregateSummary("sp_loot.aggregate")
        local residential = state.classStats.residential_misc or nil
        if residential and residential.observed > 0 then
            logLoot(
                "sp_loot.aggregate_items.residential_misc",
                string.format(
                    "containers=%s items={%s}",
                    tostring(residential.observed),
                    formatFullTypeCounts(residential.byFullType)
                )
            )
        end
        local musicStore = state.classStats.music_store or nil
        if musicStore and musicStore.observed > 0 then
            logLoot(
                "sp_loot.aggregate_items.music_store",
                string.format(
                    "containers=%s items={%s}",
                    tostring(musicStore.observed),
                    formatFullTypeCounts(musicStore.byFullType)
                )
            )
        end
    end
end

return probe
