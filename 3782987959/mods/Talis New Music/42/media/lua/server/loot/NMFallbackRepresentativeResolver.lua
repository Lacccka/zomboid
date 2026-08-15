require "slot/NMWorldItemVisuals"
require "loot/NMManagedSpawnCatalog"
require "loot/NMLootDebugHelpers"

NMFallbackRepresentativeResolver = NMFallbackRepresentativeResolver or {}

local resolver = NMFallbackRepresentativeResolver

local MEDIA_CATEGORY_ORDER = NMManagedSpawnCatalog.getMediaCategoryOrder and NMManagedSpawnCatalog.getMediaCategoryOrder()
    or { "cassettes", "vinyl", "cds" }
local DEVICE_CATEGORY_ORDER = NMManagedSpawnCatalog.getDeviceCategoryOrder and NMManagedSpawnCatalog.getDeviceCategoryOrder()
    or { "walkman", "boombox", "cdplayer", "recordplayer" }

local REPRESENTATIVE_BY_CATEGORY_SOURCE = {
    standard = {
        cassettes = "NewMusic.LootRepMediaTape",
        vinyl = "NewMusic.LootRepMediaWax",
        cds = "NewMusic.LootRepMediaDisc"
    },
    globalBackfill = {
        cassettes = "NewMusic.LootRepMediaTapeBackfill",
        vinyl = "NewMusic.LootRepMediaWaxBackfill",
        cds = "NewMusic.LootRepMediaDiscBackfill"
    },
    storeTopUp = {
        cassettes = "NewMusic.LootRepMediaTapeStoreTopUp",
        vinyl = "NewMusic.LootRepMediaWaxStoreTopUp",
        cds = "NewMusic.LootRepMediaDiscStoreTopUp"
    }
}

local DEVICE_PLACEHOLDER_BY_CATEGORY_SOURCE = {
    globalBackfill = {
        walkman = "NewMusic.LootRepDeviceWalkmanBackfill",
        boombox = "NewMusic.LootRepDeviceBoomboxBackfill",
        cdplayer = "NewMusic.LootRepDeviceCDPlayerBackfill",
        recordplayer = "NewMusic.LootRepDeviceRecordPlayerBackfill"
    },
    storeTopUp = {
        walkman = "NewMusic.LootRepDeviceWalkmanStoreTopUp",
        boombox = "NewMusic.LootRepDeviceBoomboxStoreTopUp",
        cdplayer = "NewMusic.LootRepDeviceCDPlayerStoreTopUp",
        recordplayer = "NewMusic.LootRepDeviceRecordPlayerStoreTopUp"
    }
}

local PLACEHOLDER_METADATA_BY_FULLTYPE = {}
for source, mapping in pairs(REPRESENTATIVE_BY_CATEGORY_SOURCE) do
    for category, fullType in pairs(mapping) do
        PLACEHOLDER_METADATA_BY_FULLTYPE[fullType] = {
            kind = "media",
            category = category,
            source = source
        }
    end
end
for source, mapping in pairs(DEVICE_PLACEHOLDER_BY_CATEGORY_SOURCE) do
    for category, fullType in pairs(mapping) do
        PLACEHOLDER_METADATA_BY_FULLTYPE[fullType] = {
            kind = "device",
            category = category,
            source = source
        }
    end
end

local function toLower(value)
    return string.lower(tostring(value or ""))
end

local function logLoot(tag, detail)
    if NMLootDebugHelpers and NMLootDebugHelpers.logLoot then
        NMLootDebugHelpers.logLoot(tag or "fallback", detail or "")
    end
end

local function newState()
    return {
        configured = false,
        byCategory = {},
        replacementStats = {
            replaced = 0,
            failed = 0
        }
    }
end

local state = newState()

local function newCategoryState(units)
    local discovered = {}
    local remaining = {}
    local remainingPosition = {}
    for i = 1, #(units or {}) do
        remaining[i] = i
        remainingPosition[i] = i
    end
    return {
        units = units or {},
        total = #(units or {}),
        discovered = discovered,
        remaining = remaining,
        remainingPosition = remainingPosition,
        discoveredCount = 0,
        resolvedCount = 0,
        newDiscoveries = 0,
        repeatResolutions = 0
    }
end

local function getUnitWeight(unit)
    local weight = tonumber(unit and unit.variantWeight) or 1.0
    if weight <= 0 then
        return 0
    end
    return weight
end

local function getTotalWeightForIndexes(units, indexes)
    local total = 0
    for i = 1, #(indexes or {}) do
        total = total + getUnitWeight(units and units[indexes[i]] or nil)
    end
    return total
end

local function getTotalWeight(units)
    local total = 0
    for i = 1, #(units or {}) do
        total = total + getUnitWeight(units[i])
    end
    return total
end

local function randomWeightedRoll(totalWeight)
    if totalWeight <= 0 then
        return 0
    end
    if ZombRandFloat then
        return ZombRandFloat(0.0, totalWeight)
    end
    if ZombRand then
        return (ZombRand(1000000) / 1000000) * totalWeight
    end
    return math.random() * totalWeight
end

local function swap(array, a, b)
    local tmp = array[a]
    array[a] = array[b]
    array[b] = tmp
end

local function shuffleArray(array)
    for i = #array, 2, -1 do
        local swapIndex = (ZombRand and (ZombRand(i) + 1)) or i
        swap(array, i, swapIndex)
    end
end

local function rebuildRemaining(categoryState)
    local remaining = {}
    local remainingPosition = {}
    for i = 1, categoryState.total do
        if categoryState.discovered[i] ~= true then
            remaining[#remaining + 1] = i
        end
    end
    shuffleArray(remaining)
    for i = 1, #remaining do
        remainingPosition[remaining[i]] = i
    end
    categoryState.remaining = remaining
    categoryState.remainingPosition = remainingPosition
end

local function markDiscovered(categoryState, index)
    if categoryState.discovered[index] == true then
        return false
    end
    categoryState.discovered[index] = true
    categoryState.discoveredCount = categoryState.discoveredCount + 1

    local position = categoryState.remainingPosition[index]
    if position then
        local lastIndex = #categoryState.remaining
        local movedValue = categoryState.remaining[lastIndex]
        categoryState.remaining[position] = movedValue
        categoryState.remaining[lastIndex] = nil
        categoryState.remainingPosition[index] = nil
        if movedValue ~= nil then
            categoryState.remainingPosition[movedValue] = position
        end
    end
    return true
end

local function chooseUndiscovered(categoryState)
    if #categoryState.remaining < 1 then
        return nil
    end
    local totalWeight = getTotalWeightForIndexes(categoryState.units, categoryState.remaining)
    if totalWeight <= 0 then
        local pickIndex = #categoryState.remaining
        local chosen = categoryState.remaining[pickIndex]
        categoryState.remaining[pickIndex] = nil
        categoryState.remainingPosition[chosen] = nil
        return chosen
    end

    local roll = randomWeightedRoll(totalWeight)
    local running = 0
    for i = 1, #categoryState.remaining do
        local chosen = categoryState.remaining[i]
        running = running + getUnitWeight(categoryState.units and categoryState.units[chosen] or nil)
        if roll < running or i == #categoryState.remaining then
            local lastIndex = #categoryState.remaining
            local movedValue = categoryState.remaining[lastIndex]
            categoryState.remaining[i] = movedValue
            categoryState.remaining[lastIndex] = nil
            categoryState.remainingPosition[chosen] = nil
            if movedValue ~= nil then
                categoryState.remainingPosition[movedValue] = i
            end
            return chosen
        end
    end

    return nil
end

local function chooseWeightedIndexFromList(units, indexes)
    local count = #(indexes or {})
    if count < 1 then
        return nil
    end

    local totalWeight = getTotalWeightForIndexes(units, indexes)
    if totalWeight <= 0 then
        local pickIndex = (ZombRand and (ZombRand(count) + 1)) or count
        return indexes[pickIndex]
    end

    local roll = randomWeightedRoll(totalWeight)
    local running = 0
    for i = 1, count do
        local chosen = indexes[i]
        running = running + getUnitWeight(units and units[chosen] or nil)
        if roll < running or i == count then
            return chosen
        end
    end

    return indexes[count]
end

local function collectAvailableIndexes(categoryState, excludedIndexes)
    local available = {}
    local total = tonumber(categoryState and categoryState.total) or 0
    for i = 1, total do
        if not (excludedIndexes and excludedIndexes[i] == true) then
            available[#available + 1] = i
        end
    end
    return available
end

local function chooseContainerLocalUniqueIndex(categoryState, excludedIndexes)
    local available = collectAvailableIndexes(categoryState, excludedIndexes)
    if #available < 1 then
        return nil
    end
    return chooseWeightedIndexFromList(categoryState and categoryState.units, available)
end

local function chooseIndex(categoryState)
    local total = tonumber(categoryState and categoryState.total) or 0
    if total < 1 then
        return nil
    end

    local remainingCount = #(categoryState.remaining or {})
    if remainingCount > 0 then
        local chooseFresh = true
        if remainingCount < total then
            local threshold = math.floor((remainingCount / total) * 1000)
            chooseFresh = not ZombRand or ZombRand(1000) < threshold
        end
        if chooseFresh then
            return chooseUndiscovered(categoryState)
        end
    end

    local totalWeight = getTotalWeight(categoryState and categoryState.units)
    if totalWeight <= 0 then
        return (ZombRand and (ZombRand(total) + 1)) or 1
    end

    local roll = randomWeightedRoll(totalWeight)
    local running = 0
    for i = 1, total do
        running = running + getUnitWeight(categoryState.units[i])
        if roll < running or i == total then
            return i
        end
    end

    return total
end

local function addResolvedItem(container, fullType)
    if NMWorldItemVisuals and NMWorldItemVisuals.addItemWithVisual then
        local item = select(1, NMWorldItemVisuals.addItemWithVisual(container, fullType))
        if item then
            return item
        end
    end
    return container and container.AddItem and container:AddItem(fullType) or nil
end

local function removeContainerItem(container, item)
    if sendRemoveItemFromContainer then
        pcall(sendRemoveItemFromContainer, container, item)
    end
    if container and container.DoRemoveItem then
        container:DoRemoveItem(item)
        return true
    end
    if container and container.Remove then
        container:Remove(item)
        return true
    end
    return false
end

local function resolveSelectedUnit(category, selectionSession)
    local categoryState = state.byCategory[category]
    local localCategory = selectionSession
        and selectionSession.byCategory
        and selectionSession.byCategory[category]
        or nil
    local index = nil
    if localCategory then
        index = chooseContainerLocalUniqueIndex(categoryState, localCategory.chosenIndexes)
    end
    if index == nil then
        index = chooseIndex(categoryState)
    end
    local unit = index and categoryState and categoryState.units and categoryState.units[index] or nil
    return categoryState, index, unit
end

function resolver.addResolvedItemForCategory(container, category)
    return resolver.addResolvedItemForCategoryFromSource(container, category, "standard")
end

local function stampResolvedItemSource(item, category, source)
    if not (item and item.getModData) then
        return
    end
    local modData = item:getModData()
    if type(modData) ~= "table" then
        return
    end
    modData.nmLootResolved = true
    modData.nmLootCategory = tostring(category or "")
    modData.nmLootSource = tostring(source or "standard")
end

local function markSessionPick(selectionSession, category, index, wasRepeat)
    if not (selectionSession and category and index) then
        return
    end
    local byCategory = selectionSession.byCategory or {}
    local localCategory = byCategory[category]
    if not localCategory then
        localCategory = {
            chosenIndexes = {},
            placeholders = 0,
            uniquePicks = 0,
            repeatPicks = 0
        }
        byCategory[category] = localCategory
        selectionSession.byCategory = byCategory
    end

    if localCategory.chosenIndexes[index] ~= true then
        localCategory.chosenIndexes[index] = true
        localCategory.uniquePicks = localCategory.uniquePicks + 1
    elseif wasRepeat == true then
        localCategory.repeatPicks = localCategory.repeatPicks + 1
    end
end

local function logReplacementSession(container, selectionSession)
    if not selectionSession or not selectionSession.byCategory then
        return
    end

    local containerShape = tostring(container or "nil")
    for i = 1, #MEDIA_CATEGORY_ORDER do
        local category = MEDIA_CATEGORY_ORDER[i]
        local localCategory = selectionSession.byCategory[category]
        if category == "vinyl" and localCategory and (tonumber(localCategory.placeholders) or 0) > 0 then
            local detail = string.format(
                "category=%s placeholders=%s unique=%s repeats=%s shape=%s",
                tostring(category),
                tostring(tonumber(localCategory.placeholders) or 0),
                tostring(tonumber(localCategory.uniquePicks) or 0),
                tostring(tonumber(localCategory.repeatPicks) or 0),
                containerShape
            )
            if NMLootDebugHelpers and NMLootDebugHelpers.logLootThrottled then
                NMLootDebugHelpers.logLootThrottled(
                    "fallback.replace_container",
                    string.format(
                        "%s|%s|%s|%s",
                        tostring(category),
                        tostring(tonumber(localCategory.placeholders) or 0),
                        tostring(tonumber(localCategory.uniquePicks) or 0),
                        containerShape
                    ),
                    "fallback.replace_container",
                    detail
                )
            else
                logLoot("fallback.replace_container", detail)
            end
        end
    end
end

local function ensureSelectionSessionCategory(selectionSession, category)
    if not selectionSession then
        return nil
    end
    selectionSession.byCategory = selectionSession.byCategory or {}
    selectionSession.byCategory[category] = selectionSession.byCategory[category] or {
        chosenIndexes = {},
        placeholders = 0,
        uniquePicks = 0,
        repeatPicks = 0
    }
    return selectionSession.byCategory[category]
end

function resolver.addResolvedItemForCategoryFromSource(container, category, source, selectionSession)
    local categoryState, index, unit = resolveSelectedUnit(category, selectionSession)
    local resolvedType = tostring(unit and unit.spawnFullType or "")
    if resolvedType == "" then
        state.replacementStats.failed = state.replacementStats.failed + 1
        return nil
    end

    local added = addResolvedItem(container, resolvedType)
    if not added then
        state.replacementStats.failed = state.replacementStats.failed + 1
        return nil
    end
    stampResolvedItemSource(added, category, source)

    if sendAddItemToContainer then
        pcall(sendAddItemToContainer, container, added)
    end
    categoryState.resolvedCount = categoryState.resolvedCount + 1
    local wasFresh = markDiscovered(categoryState, index)
    if wasFresh then
        categoryState.newDiscoveries = categoryState.newDiscoveries + 1
    else
        categoryState.repeatResolutions = categoryState.repeatResolutions + 1
    end
    markSessionPick(selectionSession, category, index, not wasFresh)
    state.replacementStats.replaced = state.replacementStats.replaced + 1
    return added
end

function resolver.addResolvedDeviceItemForCategory(container, category, source)
    local categoryState, index, unit = resolveSelectedUnit(category)
    local resolvedType = tostring(unit and unit.spawnFullType or "")
    if resolvedType == "" then
        state.replacementStats.failed = state.replacementStats.failed + 1
        return nil
    end

    local added = addResolvedItem(container, resolvedType)
    if not added then
        state.replacementStats.failed = state.replacementStats.failed + 1
        return nil
    end
    stampResolvedItemSource(added, category, source)

    if sendAddItemToContainer then
        pcall(sendAddItemToContainer, container, added)
    end
    categoryState.resolvedCount = categoryState.resolvedCount + 1
    if markDiscovered(categoryState, index) then
        categoryState.newDiscoveries = categoryState.newDiscoveries + 1
    else
        categoryState.repeatResolutions = categoryState.repeatResolutions + 1
    end
    state.replacementStats.replaced = state.replacementStats.replaced + 1
    return added
end

function resolver.getRepresentativeFullType(category, source)
    local sourceKey = tostring(source or "standard")
    local mapping = REPRESENTATIVE_BY_CATEGORY_SOURCE[sourceKey] or REPRESENTATIVE_BY_CATEGORY_SOURCE.standard
    return mapping and mapping[tostring(category or "")] or nil
end

function resolver.getDevicePlaceholderFullType(category, source)
    local sourceKey = tostring(source or "")
    local mapping = DEVICE_PLACEHOLDER_BY_CATEGORY_SOURCE[sourceKey]
    return mapping and mapping[tostring(category or "")] or nil
end

function resolver.isRepresentativeFullType(fullType)
    local metadata = PLACEHOLDER_METADATA_BY_FULLTYPE[tostring(fullType or "")]
    return metadata and metadata.category or nil
end

function resolver.configure(mediaPool, devicePool)
    state = newState()
    for i = 1, #MEDIA_CATEGORY_ORDER do
        local category = MEDIA_CATEGORY_ORDER[i]
        local units = NMManagedSpawnCatalog.orderedMediaUnitsForCategory(
            { media = mediaPool or {} },
            category
        )
        state.byCategory[category] = newCategoryState(units)
        rebuildRemaining(state.byCategory[category])
    end
    for i = 1, #DEVICE_CATEGORY_ORDER do
        local category = DEVICE_CATEGORY_ORDER[i]
        local units = NMManagedSpawnCatalog.orderedDeviceUnitsForCategory(
            { devices = devicePool or {} },
            category
        )
        state.byCategory[category] = newCategoryState(units)
        rebuildRemaining(state.byCategory[category])
    end
    state.configured = true
end

function resolver.replaceRepresentativesInContainer(container)
    if not state.configured then
        return 0
    end

    local resolvedContainer, items = NMLootDebugHelpers.resolveMutableContainer(container, "fallback.skip_container")
    if not (resolvedContainer and items and items.size and items.get) then
        return 0
    end

    local representatives = {}
    local selectionSession = {
        byCategory = {}
    }
    for i = 0, items:size() - 1 do
        local item = items:get(i)
        local fullType = item and item.getFullType and tostring(item:getFullType() or "") or ""
        local metadata = PLACEHOLDER_METADATA_BY_FULLTYPE[fullType]
        if metadata then
            representatives[#representatives + 1] = {
                item = item,
                metadata = metadata
            }
            if metadata.kind == "media" then
                local localCategory = ensureSelectionSessionCategory(selectionSession, metadata.category)
                if localCategory then
                    localCategory.placeholders = (tonumber(localCategory.placeholders) or 0) + 1
                end
            end
        end
    end

    local replaced = 0
    for i = 1, #representatives do
        local rep = representatives[i]
        local added = nil
        if rep.metadata.kind == "device" then
            added = resolver.addResolvedDeviceItemForCategory(resolvedContainer, rep.metadata.category, rep.metadata.source)
            if added then
                removeContainerItem(resolvedContainer, rep.item)
            end
        else
            added = resolver.addResolvedItemForCategoryFromSource(
                resolvedContainer,
                rep.metadata.category,
                rep.metadata.source,
                selectionSession
            )
            if added then
                removeContainerItem(resolvedContainer, rep.item)
            end
        end
        if added then
            replaced = replaced + 1
        end
    end
    if replaced > 0 then
        logReplacementSession(resolvedContainer, selectionSession)
    end
    return replaced
end

function resolver.getCoverageSnapshot()
    local snapshot = {
        replacementStats = {
            replaced = state.replacementStats.replaced,
            failed = state.replacementStats.failed
        }
    }
    for i = 1, #MEDIA_CATEGORY_ORDER do
        local category = MEDIA_CATEGORY_ORDER[i]
        local categoryState = state.byCategory[category]
        snapshot[category] = {
            total = tonumber(categoryState and categoryState.total) or 0,
            discovered = tonumber(categoryState and categoryState.discoveredCount) or 0,
            resolved = tonumber(categoryState and categoryState.resolvedCount) or 0,
            repeats = tonumber(categoryState and categoryState.repeatResolutions) or 0,
            fresh = tonumber(categoryState and categoryState.newDiscoveries) or 0
        }
    end
    return snapshot
end

function resolver.getReplacementStats()
    return {
        replaced = state.replacementStats.replaced,
        failed = state.replacementStats.failed
    }
end

function resolver.getCategoryPoolSize(category)
    local categoryState = state.byCategory[tostring(category or "")]
    return tonumber(categoryState and categoryState.total) or 0
end

return resolver
