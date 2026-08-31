-- Transactional physical consumption for loaded faction settlements.
-- Logical demand is authoritative in ConsumptionPlan; this executor only acknowledges
-- units after exact ItemContainer mutation and post-mutation item-id reconciliation.
if isClient and isClient() and not (isServer and isServer()) then return {} end

require "LCCQF/LCCQFConstants"
require "LCCQF/Content/LCCQFSupplyCategoryDefinitions"
require "LCCQF/World/LCCQFWorldContainerResolver"
require "LCCQF/FactionWorld/LCCQFFactionSiteRegistry"
require "LCCQF/FactionWorld/LCCQFFactionSiteStock"
require "LCCQF/FactionWorld/LCCQFFactionSiteConsumptionPlan"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Categories = LCCQF.SupplyCategoryRegistry
local Resolver = LCCQF.WorldContainerResolver
local Sites = LCCQF.FactionSiteRegistry
local Stock = LCCQF.FactionSiteStock
local Plan = LCCQF.FactionSiteConsumptionPlan
local Executor = LCCQF.FactionSiteConsumptionExecutor or {}

local function worldHours()
    if not getGameTime then return 0 end
    local gameTime = getGameTime()
    return gameTime and gameTime.getWorldAgeHours and (tonumber(gameTime:getWorldAgeHours()) or 0) or 0
end

local function units(value)
    return math.max(0, math.floor(tonumber(value) or 0))
end

local function itemId(item)
    if not item or not item.getID then return nil end
    local ok, value = pcall(function() return item:getID() end)
    value = ok and tonumber(value) or nil
    if value == nil then return nil end
    return math.floor(value)
end

local function itemFullType(item)
    if not item or not item.getFullType then return nil end
    local ok, value = pcall(function() return item:getFullType() end)
    return ok and type(value) == "string" and value ~= "" and value or nil
end

local function copyLocator(locator)
    if type(locator) ~= "table" then return nil end
    return {
        schemaVersion = 1,
        x = tonumber(locator.x), y = tonumber(locator.y), z = tonumber(locator.z),
        objectCollection = tostring(locator.objectCollection or "unknown"),
        collectionIndex = tonumber(locator.collectionIndex),
        objectIndex = tonumber(locator.objectIndex),
        containerIndex = tonumber(locator.containerIndex),
        containerType = tostring(locator.containerType or ""),
        spriteName = tostring(locator.spriteName or ""),
    }
end

local function rowFor(site, supplyId)
    return site and site.economy and site.economy.consumption
        and site.economy.consumption.rows and site.economy.consumption.rows[supplyId] or nil
end

local function mark(site, reason)
    if site and site.siteId then Sites.MarkDirty(site.siteId, reason) end
end

local function networkApiAvailable()
    return sendRemoveItemFromContainer ~= nil
end

local function listItems(container)
    local items
    if container and container.getItems then
        pcall(function() items = container:getItems() end)
    end
    return items
end

local function findItemById(container, wantedId)
    local items = listItems(container)
    local size = items and items.size and items:size() or 0
    for index = 0, size - 1 do
        local item = items:get(index)
        if itemId(item) == wantedId then return item end
    end
    return nil
end

local function removalAllowed(container, item)
    if not container or not item then return false end
    if container.isRemoveItemAllowed then
        local ok, allowed = pcall(function() return container:isRemoveItemAllowed(item) end)
        return ok and allowed == true
    end
    return false
end

local function categoryMatches(category, item)
    return Categories and Categories.Matches and Categories.Matches(category, item) == true
end

local function freshStock(site)
    local ok, changedOrError = Stock.Refresh(site)
    if not ok then return false, changedOrError end
    return true, changedOrError == true
end

local function collectLiveItemIds(site)
    local ids = {}
    local visited = 0
    local limit = math.max(1, units(C.FACTION_SITE_STOCK_MAX_ITEMS or 4096))
    local rows = site and site.stock and site.stock.containers or {}
    for _, snapshotRow in ipairs(rows) do
        local _, container = Resolver.Resolve(snapshotRow.locator)
        if container then
            local items = listItems(container)
            local size = items and items.size and items:size() or 0
            for index = 0, size - 1 do
                if visited >= limit then return nil, "item-id reconciliation budget exhausted" end
                visited = visited + 1
                local id = itemId(items:get(index))
                if id ~= nil then ids[id] = true end
            end
        end
    end
    return ids, nil
end

local function newTransaction(site, row, supplyId, category, requested, selected)
    row.executionSequence = units(row.executionSequence) + 1
    local txId = table.concat({
        tostring(site.siteId), tostring(supplyId), tostring(row.executionSequence)
    }, ":")
    local tx = {
        schemaVersion = 1,
        txId = txId,
        state = "PREPARED",
        supplyId = supplyId,
        category = category,
        requestedUnits = requested,
        preparedStockRevision = units(site.stock and site.stock.revision),
        preparedWorldHours = worldHours(),
        items = selected,
    }
    row.execution = tx
    mark(site, "settlement consumption transaction prepared")
    return tx
end

local function selectItems(site, category, requested)
    local selected = {}
    for _, snapshotRow in ipairs(site.stock and site.stock.containers or {}) do
        if #selected >= requested then break end
        if units(snapshotRow.categories and snapshotRow.categories[category]) > 0 then
            local _, container = Resolver.Resolve(snapshotRow.locator)
            if container then
                local items = listItems(container)
                local size = items and items.size and items:size() or 0
                for index = 0, size - 1 do
                    if #selected >= requested then break end
                    local item = items:get(index)
                    local id = itemId(item)
                    local fullType = itemFullType(item)
                    if id ~= nil and fullType and categoryMatches(category, item)
                        and removalAllowed(container, item)
                    then
                        selected[#selected + 1] = {
                            itemId = id,
                            fullType = fullType,
                            locator = copyLocator(snapshotRow.locator),
                            locatorKey = tostring(snapshotRow.locatorKey or Resolver.LocatorKey(snapshotRow.locator)),
                            state = "SELECTED",
                        }
                    end
                end
            end
        end
    end
    return selected
end

local function descriptorStillValid(descriptor, category)
    local _, container = Resolver.Resolve(descriptor.locator)
    if not container then return nil, nil, "container unavailable" end
    local item = findItemById(container, tonumber(descriptor.itemId))
    if not item then return container, nil, "item not in exact container" end
    if itemFullType(item) ~= descriptor.fullType then return container, nil, "fullType changed" end
    if not categoryMatches(category, item) then return container, nil, "category no longer matches" end
    if not removalAllowed(container, item) then return container, nil, "removal not allowed" end
    return container, item, nil
end

local function mutateTransaction(site, tx)
    local removed = 0
    local syncFailed = false
    for _, descriptor in ipairs(tx.items or {}) do
        if descriptor.state == "SELECTED" then
            local container, item, err = descriptorStillValid(descriptor, tx.category)
            if not item then
                descriptor.state = "SKIPPED"
                descriptor.error = err
            else
                descriptor.state = "REMOVING"
                descriptor.removingWorldHours = worldHours()
                mark(site, "settlement consumption item removal started")

                local okRemove, removeError = pcall(function() container:Remove(item) end)
                local stillPresent = findItemById(container, descriptor.itemId) ~= nil
                if not okRemove or stillPresent then
                    descriptor.state = "SKIPPED"
                    descriptor.error = okRemove and "item remained after Remove" or tostring(removeError)
                else
                    descriptor.state = "REMOVED"
                    descriptor.removedWorldHours = worldHours()
                    removed = removed + 1
                    -- Build 42 LuaManager exposes sendRemoveItemFromContainer globally;
                    -- on a dedicated server it delegates to GameServer and sends the
                    -- RemoveInventoryItemFromContainer packet to relevant clients.
                    local okSync, syncError = pcall(function()
                        sendRemoveItemFromContainer(container, item)
                    end)
                    descriptor.networkSynced = okSync == true
                    if not okSync then
                        descriptor.networkError = tostring(syncError)
                        syncFailed = true
                        break
                    end
                end
            end
        end
    end
    tx.state = "MUTATED"
    tx.mutatedWorldHours = worldHours()
    tx.removedDuringPass = removed
    tx.networkSyncFailed = syncFailed
    mark(site, "settlement consumption transaction mutated")
    return removed, syncFailed
end

local function reconcileStates(tx, liveIds)
    local applied = 0
    for _, descriptor in ipairs(tx.items or {}) do
        local present = liveIds[tonumber(descriptor.itemId)] == true
        if descriptor.state == "REMOVING" then
            if present then
                descriptor.state = "SKIPPED"
                descriptor.error = "recovered item still present"
            else
                descriptor.state = "REMOVED"
                descriptor.recoveredRemoval = true
            end
        elseif descriptor.state == "REMOVED" and present then
            descriptor.state = "REAPPEARED"
            descriptor.error = "removed item id reappeared in settlement"
        end
        if descriptor.state == "REMOVED" and not present then applied = applied + 1 end
    end
    return applied
end

local function finalize(site, row, tx)
    local okRefresh, refreshError = freshStock(site)
    if not okRefresh then return false, "post-mutation stock refresh failed: " .. tostring(refreshError) end

    local liveIds, idError = collectLiveItemIds(site)
    if not liveIds then return false, idError end
    local applied = reconcileStates(tx, liveIds)

    if applied > 0 then
        local okAck, ackOutcome = Plan.AcknowledgeApplied(site, tx.supplyId, applied, tx.txId)
        if not okAck then return false, "consumption acknowledgement failed: " .. tostring(ackOutcome) end
        tx.ackOutcome = ackOutcome
    end

    row.lastExecution = {
        schemaVersion = 1,
        txId = tx.txId,
        requestedUnits = units(tx.requestedUnits),
        appliedUnits = applied,
        preparedStockRevision = units(tx.preparedStockRevision),
        completedStockRevision = units(site.stock and site.stock.revision),
        completedWorldHours = worldHours(),
        networkSyncFailed = tx.networkSyncFailed == true,
        outcome = applied > 0 and "APPLIED" or "NO_MUTATION",
    }
    row.execution = nil
    mark(site, "settlement consumption transaction finalized")
    return true, applied
end

local function reconcileExisting(site, row)
    local tx = row and row.execution or nil
    if type(tx) ~= "table" then return true, 0, false end
    if tx.state ~= "PREPARED" and tx.state ~= "MUTATED" then
        return false, "unknown consumption transaction state", true
    end

    -- Refresh + site-wide item-id set recovers a crash after Remove but before the
    -- descriptor could be advanced from REMOVING to REMOVED.
    local okRefresh, refreshError = freshStock(site)
    if not okRefresh then return false, refreshError, true end
    local liveIds, idError = collectLiveItemIds(site)
    if not liveIds then return false, idError, true end
    reconcileStates(tx, liveIds)

    if tx.state == "PREPARED" then
        -- Resume only descriptors that were never mutated. Recovered REMOVING rows have
        -- already become REMOVED/SKIPPED above.
        mutateTransaction(site, tx)
    end
    local ok, result = finalize(site, row, tx)
    return ok, result, true
end

function Executor.ExecuteSupply(site, supplyId)
    if type(site) ~= "table" or type(supplyId) ~= "string" or supplyId == "" then
        return false, "invalid consumption execution input"
    end
    if not networkApiAvailable() then
        return false, "Lua container removal sync API unavailable"
    end

    local row = rowFor(site, supplyId)
    if not row then return false, "consumption row unavailable" end

    local recoveredOk, recoveredResult, hadExisting = reconcileExisting(site, row)
    if hadExisting then return recoveredOk, recoveredResult end

    local pending = Plan.GetPending(site, supplyId)
    if pending < 1 then return true, 0 end
    local category = tostring(row.category or "")
    if category == "" or not Categories.IsRegistered(category) then
        return false, "unregistered consumption category"
    end

    local okRefresh, refreshError = freshStock(site)
    if not okRefresh then return false, refreshError end
    local available = Stock.GetCategoryQuantity(site, category)
    local maxPerPass = math.max(1, units(C.FACTION_SITE_CONSUMPTION_MAX_ITEMS_PER_PASS or 16))
    local requested = math.min(pending, available, maxPerPass)
    if requested < 1 then return true, 0 end

    local selected = selectItems(site, category, requested)
    if #selected < 1 then return false, "verified category stock could not be resolved to removable items" end
    requested = #selected

    local tx = newTransaction(site, row, supplyId, category, requested, selected)
    mutateTransaction(site, tx)
    return finalize(site, row, tx)
end

LCCQF.FactionSiteConsumptionExecutor = Executor
return Executor
