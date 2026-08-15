require "loot/NMLootDebugHelpers"

NMServerLootProbe = NMServerLootProbe or {}

local probe = NMServerLootProbe

local CATEGORY_ORDER = {
    "cassettes",
    "vinyl",
    "cds",
    "walkman",
    "boombox",
    "cdplayer",
    "recordplayer"
}

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

local function newState()
    return {
        configured = false,
        preset = "",
        rawRatesText = "",
        managedLootMap = {},
        totals = newCategoryCounts(),
        observedContainers = 0,
        observedManagedContainers = 0,
        observedManagedItems = 0,
        seenContainers = setmetatable({}, { __mode = "k" })
    }
end

local state = newState()

local function shouldLog()
    return NMCore and NMCore.isSubsystemDebugEnabled and NMCore.isSubsystemDebugEnabled("loot_probe") == true
end

local function logProbe(tag, detail)
    if shouldLog() and NMCore and NMCore.logChannel then
        NMCore.logChannel("loot_probe", tostring(tag or "loot_probe"), tostring(detail or ""))
    end
end

local function resolveContainerItems(container)
    if NMLootDebugHelpers and NMLootDebugHelpers.resolveContainerItems then
        return NMLootDebugHelpers.resolveContainerItems(container, "loot_probe.skip_container")
    end
    return nil
end

local function formatCategoryCounts(counts)
    local parts = {}
    for i = 1, #CATEGORY_ORDER do
        local key = CATEGORY_ORDER[i]
        parts[#parts + 1] = key .. "=" .. tostring(tonumber(counts and counts[key]) or 0)
    end
    return table.concat(parts, " ")
end

local function formatSessionHeader()
    return string.format(
        "preset=%s sandbox={%s}",
        tostring(state.preset or ""),
        tostring(state.rawRatesText or "")
    )
end

local function formatRunningTotals()
    return string.format(
        "containers=%s managedContainers=%s managedItems=%s totals={%s}",
        tostring(state.observedContainers),
        tostring(state.observedManagedContainers),
        tostring(state.observedManagedItems),
        formatCategoryCounts(state.totals)
    )
end

local function countManagedItemsInContainer(container)
    local counts = newCategoryCounts()
    local total = 0
    local items = resolveContainerItems(container)
    if not (items and items.size and items.get) then
        return counts, total
    end
    for i = 0, items:size() - 1 do
        local item = items:get(i)
        local fullType = item and item.getFullType and tostring(item:getFullType() or "") or ""
        local category = state.managedLootMap[fullType]
        if counts[category] ~= nil then
            counts[category] = (tonumber(counts[category]) or 0) + 1
            total = total + 1
        end
    end
    return counts, total
end

function probe.configure(config)
    state = newState()
    state.configured = true
    state.preset = tostring(config and config.preset or "")
    state.rawRatesText = tostring(config and config.rawRatesText or "")
    state.managedLootMap = config and config.managedLootMap or {}
    if shouldLog() then
        logProbe("loot_probe.session", formatSessionHeader())
    end
end

function probe.disable()
    state = newState()
end

function probe.isConfigured()
    return state.configured == true
end

function probe.isFillObservationActive()
    return state.configured == true and shouldLog()
end

function probe.onFillContainer(roomName, containerType, container)
    if not probe.isFillObservationActive() then
        return
    end
    if container ~= nil and state.seenContainers[container] then
        return
    end
    if container ~= nil then
        state.seenContainers[container] = true
    end

    state.observedContainers = (tonumber(state.observedContainers) or 0) + 1
    local counts, total = countManagedItemsInContainer(container)
    if total <= 0 then
        return
    end

    state.observedManagedContainers = (tonumber(state.observedManagedContainers) or 0) + 1
    state.observedManagedItems = (tonumber(state.observedManagedItems) or 0) + total
    for i = 1, #CATEGORY_ORDER do
        local key = CATEGORY_ORDER[i]
        state.totals[key] = (tonumber(state.totals[key]) or 0) + (tonumber(counts[key]) or 0)
    end

    logProbe("loot_probe.observed", formatRunningTotals())
end

return probe
