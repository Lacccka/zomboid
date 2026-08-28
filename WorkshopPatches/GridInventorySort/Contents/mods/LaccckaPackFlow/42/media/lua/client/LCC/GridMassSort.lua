require "TimedActions/ISInventoryTransferAction"
require "TimedActions/ISTimedActionQueue"

local GridContainer = require("DataModel/GridContainer")
local GridSortState = require("LCC/GridSortState")
local GridSortNetwork = require("LCC/GridSortNetwork")
local GridAutoSort = require("LCC/GridAutoSort")
local GridMassTransferVisualAction = require("LCC/GridMassTransferVisualAction")

local GridMassSort = {}

GridMassSort.SCOPE_PLAYER = "player"
GridMassSort.SCOPE_EXTERNAL = "external"
GridMassSort.active = {}

-- The real B42 MP ISInventoryTransferAction is server-authoritative and may be
-- force-completed immediately when the ItemTransaction ACK arrives. PackFlow
-- therefore performs the physical Loot/TransferItemOnSelf action first, then
-- queues the untouched vanilla MP transfer. This short settle is only for UI /
-- container refresh after the authoritative transfer has left the queue.
local SETTLE_MS = 150

local function nowMs()
    return getTimestampMs and getTimestampMs() or (getTimeInMillis and getTimeInMillis() or 0)
end

local function getPlayerByNum(playerNum)
    if getSpecificPlayer then return getSpecificPlayer(playerNum or 0) end
    return getPlayer and getPlayer() or nil
end

local function hasTimedActions(player)
    if not player or not ISTimedActionQueue then return false end
    local queue = ISTimedActionQueue.queues and ISTimedActionQueue.queues[player] or nil
    return queue and queue.queue and #queue.queue > 0 or false
end

local function itemFullType(item)
    if not item then return "" end
    if item.getFullType then
        local value = item:getFullType()
        if value ~= nil then return tostring(value) end
    end
    local moduleName = item.getModule and item:getModule() or ""
    local typeName = item.getType and item:getType() or ""
    return tostring(moduleName) .. "." .. tostring(typeName)
end

local function itemName(item)
    if not item then return "" end
    local value = item.getDisplayName and item:getDisplayName() or nil
    if (value == nil or value == "") and item.getName then value = item:getName() end
    return string.lower(tostring(value or itemFullType(item)))
end

local function containerLabel(container)
    if not container then return "nil" end
    local ctype = container.getType and container:getType() or "?"
    local parent = container.getParent and container:getParent() or nil
    if container.getVehiclePart and container:getVehiclePart() then
        local part = container:getVehiclePart()
        local vehicle = part and part:getVehicle() or nil
        return "vehicle:" .. tostring(vehicle and vehicle:getId() or "?")
            .. ":" .. tostring(part and part:getId() or ctype)
    end
    if parent and parent.getSquare then
        local square = parent:getSquare()
        if square then
            return tostring(ctype) .. "@"
                .. tostring(square:getX()) .. ","
                .. tostring(square:getY()) .. ","
                .. tostring(square:getZ())
        end
    end
    local containingItem = container.getContainingItem and container:getContainingItem() or nil
    if containingItem and containingItem.getID then
        return tostring(ctype) .. "#item:" .. tostring(containingItem:getID())
    end
    return tostring(ctype)
end

local function isCorpseContainer(container)
    local parent = container and container.getParent and container:getParent() or nil
    return parent and instanceof and instanceof(parent, "IsoDeadBody") or false
end

local function isPlayerContainer(container, player)
    if not container or not player then return false end
    if player.getInventory and container == player:getInventory() then return true end
    if container.isInCharacterInventory then
        local ok, result = pcall(function() return container:isInCharacterInventory(player) end)
        if ok and result then return true end
    end
    return false
end

local function pageForScope(scope, playerNum)
    if scope == GridMassSort.SCOPE_PLAYER then
        return getPlayerInventory and getPlayerInventory(playerNum) or nil
    end
    if scope == GridMassSort.SCOPE_EXTERNAL then
        return getPlayerLoot and getPlayerLoot(playerNum) or nil
    end
    return nil
end

local function findGridUi(pane, container)
    if not pane or not pane.gridContainerUis or not container then return nil end
    for _, gridUi in ipairs(pane.gridContainerUis) do
        if gridUi and not gridUi.isOverflow and gridUi.inventoryContainer == container then
            return gridUi
        end
    end
    return nil
end

local function hasScopeCandidate(scope, playerNum)
    local player = getPlayerByNum(playerNum)
    local page = pageForScope(scope, playerNum)
    if not player or not page then return false end

    if scope == GridMassSort.SCOPE_PLAYER then
        return player.getInventory and player:getInventory() ~= nil or false
    end

    for _, button in ipairs(page.backpacks or {}) do
        local container = button and button.inventory or nil
        if container
            and not (container.getType and container:getType() == "floor")
            and not isCorpseContainer(container)
            and not isPlayerContainer(container, player) then
            return true
        end
    end
    return false
end

local function collectScopeRecords(scope, playerNum)
    local player = getPlayerByNum(playerNum)
    local page = pageForScope(scope, playerNum)
    local pane = page and page.inventoryPane or nil
    if not player or not page or not pane then return {}, "unavailable" end

    local seen, candidates = {}, {}
    local function addCandidate(container)
        if not container or seen[container] then return end
        seen[container] = true
        table.insert(candidates, container)
    end

    if scope == GridMassSort.SCOPE_PLAYER and player.getInventory then
        local root = player:getInventory()
        addCandidate(root)

        local items = root and root.getItems and root:getItems() or nil
        if items then
            for i = 0, items:size() - 1 do
                local item = items:get(i)
                if item and item.getInventory then
                    local ok, child = pcall(function() return item:getInventory() end)
                    if ok and child then addCandidate(child) end
                end
            end
        end
    end

    for _, button in ipairs(page.backpacks or {}) do
        if button and button.inventory then addCandidate(button.inventory) end
    end
    if #candidates == 0 and pane.inventory then addCandidate(pane.inventory) end

    local records = {}
    for index, container in ipairs(candidates) do
        if not (container.getType and container:getType() == "floor")
            and not isCorpseContainer(container) then
            local owned = isPlayerContainer(container, player)
            local inScope = (scope == GridMassSort.SCOPE_PLAYER and owned)
                or (scope == GridMassSort.SCOPE_EXTERNAL and not owned)
            if inScope then
                local gridUi = findGridUi(pane, container)
                local can, reason = GridAutoSort.canSortContainer(container, playerNum, gridUi)
                if can then
                    table.insert(records, {
                        index = index,
                        gridUi = gridUi,
                        container = container,
                    })
                elseif reason == "busy" then
                    return {}, "busy"
                end
            end
        end
    end
    return records, nil
end

local function isRelocatable(item)
    if not item then return false end
    if item.isFavorite and item:isFavorite() then return false end

    -- Moving a containing bag would invalidate child-container references. The
    -- bag itself still participates in the final coordinate sort.
    if item.getInventory then
        local ok, inventory = pcall(function() return item:getInventory() end)
        if ok and inventory then return false end
    end
    return true
end

local function stackUnits(item)
    local _, stackInfo = GridContainer.getStackInfo(item)
    local units = stackInfo and tonumber(stackInfo.units) or 1
    return units and math.max(1, units) or 1
end

local function buildMovePlan(records)
    local groups, groupList = {}, {}

    for _, record in ipairs(records) do
        for _, item in ipairs(GridSortState.collectItems(record.container)) do
            if isRelocatable(item) then
                local key = itemFullType(item)
                local group = groups[key]
                if not group then
                    group = { key = key, name = itemName(item), buckets = {}, bucketCount = 0 }
                    groups[key] = group
                    table.insert(groupList, group)
                end
                local bucket = group.buckets[record.container]
                if not bucket then
                    bucket = { record = record, items = {}, units = 0 }
                    group.buckets[record.container] = bucket
                    group.bucketCount = group.bucketCount + 1
                end
                table.insert(bucket.items, item)
                bucket.units = bucket.units + stackUnits(item)
            end
        end
    end

    table.sort(groupList, function(a, b)
        if a.name ~= b.name then return a.name < b.name end
        return a.key < b.key
    end)

    local plan = {}
    for _, group in ipairs(groupList) do
        if group.bucketCount > 1 then
            local buckets = {}
            for _, bucket in pairs(group.buckets) do table.insert(buckets, bucket) end
            table.sort(buckets, function(a, b)
                if a.units ~= b.units then return a.units > b.units end
                return a.record.index < b.record.index
            end)

            for sourceIndex = #buckets, 2, -1 do
                local source = buckets[sourceIndex]
                local destinations = {}
                for destinationIndex = 1, sourceIndex - 1 do
                    table.insert(destinations, buckets[destinationIndex].record.container)
                end
                table.sort(source.items, function(a, b)
                    local ai, bi = tonumber(a:getID()), tonumber(b:getID())
                    if ai and bi then return ai < bi end
                    return tostring(a:getID()) < tostring(b:getID())
                end)
                for _, item in ipairs(source.items) do
                    table.insert(plan, {
                        item = item,
                        itemId = item:getID(),
                        destinations = destinations,
                    })
                end
            end
        end
    end
    return plan
end

local function canTransfer(player, item, source, destination)
    if not player or not item or not source or not destination or source == destination then return false end
    if item.getContainer and item:getContainer() ~= source then return false end
    if source.isRemoveItemAllowed then
        local ok, allowed = pcall(function() return source:isRemoveItemAllowed(item) end)
        if not ok or not allowed then return false end
    end
    if destination.isItemAllowed then
        local ok, allowed = pcall(function() return destination:isItemAllowed(item) end)
        if not ok or not allowed then return false end
    end
    if destination.hasRoomFor then
        local ok, room = pcall(function() return destination:hasRoomFor(player, item) end)
        if not ok or not room then return false end
    end
    return true
end

local function chooseDestination(state, move)
    local item = move.item
    local source = item and item.getContainer and item:getContainer() or nil
    if not source or not state.containerSet[source] then return nil, nil end

    for _, destination in ipairs(move.destinations or {}) do
        if state.containerSet[destination] and canTransfer(state.player, item, source, destination) then
            return source, destination
        end
    end
    return source, nil
end

local function containerFindById(container, itemId)
    if not container or itemId == nil or not container.getItems then return nil end
    if container.getItemWithID then
        local ok, direct = pcall(function() return container:getItemWithID(itemId) end)
        if ok and direct then return direct end
    end
    local items = container:getItems()
    if not items then return nil end
    for i = 0, items:size() - 1 do
        local candidate = items:get(i)
        if candidate and candidate.getID and candidate:getID() == itemId then
            return candidate
        end
    end
    return nil
end

local function containerContainsMove(container, move)
    if not container or not move then return false end
    local item = move.item
    if item and container.contains then
        local ok, result = pcall(function() return container:contains(item) end)
        if ok and result then return true end
    end
    if item and item.getContainer and item:getContainer() == container then return true end
    return containerFindById(container, move.itemId) ~= nil
end

local function queueMove(state, move, source, destination)
    -- B42 MP completes ISInventoryTransferAction on ItemTransaction ACK and its
    -- forceComplete() explicitly stops the Loot animation. Preserve the server-
    -- authoritative transfer untouched, but run a local physical action first.
    -- The visual action never mutates ItemContainer membership.
    local visualAction = GridMassTransferVisualAction:new(
        state.player, move.item, source, destination)
    local transferAction = ISInventoryTransferAction:new(
        state.player, move.item, source, destination, nil)
    if not visualAction or not transferAction then return false end

    local queue = ISTimedActionQueue.add(visualAction)
    if not queue then return false end

    local transferQueue = ISTimedActionQueue.add(transferAction)
    if not transferQueue then
        -- queueMove only runs while the player queue is idle. Stop our visual
        -- action so its normal stop() resets/removes the unstarted tail without
        -- ever touching an unrelated player action.
        visualAction:forceStop()
        return false
    end

    print("[LCC GridSort] mass transfer queued item=" .. tostring(move.itemId)
        .. " type=" .. tostring(itemFullType(move.item))
        .. " physicalTicks=" .. tostring(visualAction.maxTime)
        .. " source=" .. containerLabel(source)
        .. " destination=" .. containerLabel(destination))

    state.waitingMove = {
        move = move,
        visualAction = visualAction,
        action = transferAction,
        source = source,
        destination = destination,
    }
    return true
end

local function finish(state)
    GridMassSort.active[state.playerNum] = nil
    print("[LCC GridSort] mass " .. tostring(state.scope)
        .. " sort complete: " .. tostring(state.transferred) .. " transfers, "
        .. tostring(state.unconfirmedTransfers or 0) .. " unconfirmed, "
        .. tostring(state.sortedContainers) .. " containers sorted")
end

local function confirmTransfer(state, waiting, replacementItem)
    if replacementItem then waiting.move.item = replacementItem end
    state.transferred = state.transferred + 1
    state.waitingMove = nil
    state.settleUntil = nowMs() + SETTLE_MS
end

local function tickTransfers(state)
    local waiting = state.waitingMove
    if waiting then
        local move = waiting.move
        local destinationItem = containerFindById(waiting.destination, move and move.itemId or nil)
        if destinationItem or containerContainsMove(waiting.destination, move) then
            confirmTransfer(state, waiting, destinationItem)
            return
        end

        -- transferAction is intentionally queued behind visualAction. As long as
        -- the real transfer remains in the vanilla queue, either the physical
        -- pre-phase is still playing or the authoritative MP transaction is in
        -- flight; both are valid states and require no timeout/polling hack.
        local stillQueued = waiting.action and ISTimedActionQueue.hasAction
            and ISTimedActionQueue.hasAction(waiting.action)
        if stillQueued then return end

        -- In MP the old Lua InventoryItem reference can be detached/replaced when
        -- the transaction commits. Absence of the stable item ID from the source
        -- after vanilla removed the action is a successful-move signal.
        local sourceItem = containerFindById(waiting.source, move and move.itemId or nil)
        if not sourceItem and not containerContainsMove(waiting.source, move) then
            confirmTransfer(state, waiting, destinationItem)
            return
        end

        state.unconfirmedTransfers = (state.unconfirmedTransfers or 0) + 1
        print("[LCC GridSort] mass transfer rejected/cancelled item="
            .. tostring(move and move.itemId)
            .. " source=" .. containerLabel(waiting.source)
            .. " destination=" .. containerLabel(waiting.destination))
        state.waitingMove = nil
        state.settleUntil = nowMs() + SETTLE_MS
        return
    end

    if state.settleUntil and nowMs() < state.settleUntil then return end
    state.settleUntil = nil

    -- Never enqueue PackFlow work behind an action already started by the player.
    if hasTimedActions(state.player) then return end

    while state.moveIndex <= #state.moves do
        local move = state.moves[state.moveIndex]
        state.moveIndex = state.moveIndex + 1
        local source, destination = chooseDestination(state, move)
        if source and destination and queueMove(state, move, source, destination) then return end
    end

    state.phase = "sort"
    state.sortIndex = 1
    state.sortWaiting = nil
    state.sortRetrySince = nil
end

local function tickSort(state)
    if hasTimedActions(state.player) then return end

    local record = state.records[state.sortIndex]
    if not record then
        finish(state)
        return
    end

    if state.sortWaiting == record then
        if GridSortNetwork.isPending(record.container) then return end
        state.sortWaiting = nil
        state.sortIndex = state.sortIndex + 1
        state.sortRetrySince = nil
        return
    end

    local ok, reason = GridAutoSort.sortContainer(
        record.container, state.playerNum, record.gridUi)
    if ok then
        if reason == "pending" then
            state.sortWaiting = record
        else
            state.sortIndex = state.sortIndex + 1
        end
        state.sortedContainers = state.sortedContainers + 1
        state.sortRetrySince = nil
        return
    end

    if reason == "nothing" then
        state.sortIndex = state.sortIndex + 1
        state.sortRetrySince = nil
        return
    end

    if reason == "busy" then
        state.sortRetrySince = state.sortRetrySince or nowMs()
        if nowMs() - state.sortRetrySince < 5000 then return end
    end

    print("[LCC GridSort] mass sort skipped container "
        .. tostring(record.container and record.container:getType())
        .. ": " .. tostring(reason))
    state.sortIndex = state.sortIndex + 1
    state.sortRetrySince = nil
end

local function abortUnexpected(state, err)
    -- Fail closed at the PackFlow orchestration layer only. Never cancel the
    -- current transfer and never clear the player's vanilla timed-action queue.
    if state then GridMassSort.active[state.playerNum] = nil end
    print("[LCC GridSort] mass " .. tostring(state and state.scope or "unknown")
        .. " sort aborted after unexpected error: " .. tostring(err))
end

local function tickState(state)
    if state.phase == "transfer" then
        tickTransfers(state)
    elseif state.phase == "sort" then
        tickSort(state)
    else
        finish(state)
    end
end

local function onTick()
    for _, state in pairs(GridMassSort.active) do
        local ok, err = pcall(tickState, state)
        if not ok then abortUnexpected(state, err) end
    end
end

function GridMassSort.isBusy(playerNum)
    return GridMassSort.active[playerNum or 0] ~= nil
end

function GridMassSort.canStart(scope, playerNum)
    playerNum = playerNum or 0
    if GridMassSort.isBusy(playerNum) then return false, "busy" end

    local player = getPlayerByNum(playerNum)
    if not player then return false, "unavailable" end
    if hasTimedActions(player) then return false, "busy" end
    if not hasScopeCandidate(scope, playerNum) then return false, "unavailable" end
    return true, nil
end

local function validateStart(scope, playerNum)
    local can, reason = GridMassSort.canStart(scope, playerNum)
    if not can then return false, reason end

    local records, blocked = collectScopeRecords(scope, playerNum)
    if blocked then return false, blocked end
    if #records == 0 then return false, "unavailable" end

    local total = 0
    for _, record in ipairs(records) do
        total = total + #GridSortState.collectItems(record.container)
    end
    if total < 2 then return false, "nothing" end
    return true, nil, records
end

function GridMassSort.start(scope, playerNum)
    playerNum = playerNum or 0
    local can, reason, records = validateStart(scope, playerNum)
    if not can then return false, reason end

    local player = getPlayerByNum(playerNum)
    if not player then return false, "unavailable" end

    local containerSet = {}
    for _, record in ipairs(records) do containerSet[record.container] = true end

    local moves = buildMovePlan(records)
    GridMassSort.active[playerNum] = {
        playerNum = playerNum,
        player = player,
        scope = scope,
        records = records,
        containerSet = containerSet,
        moves = moves,
        moveIndex = 1,
        phase = "transfer",
        transferred = 0,
        unconfirmedTransfers = 0,
        sortedContainers = 0,
        waitingMove = nil,
        settleUntil = nil,
    }

    print("[LCC GridSort] mass " .. tostring(scope) .. " sort started: "
        .. tostring(#records) .. " containers, " .. tostring(#moves)
        .. " duplicate consolidation candidates; transfer=visual-prephase+vanilla-mp-v1")
    return true, "started"
end

if not GridMassSort._eventRegistered then
    GridMassSort._eventRegistered = true
    Events.OnTick.Add(onTick)
end

print("[LCC GridSort] scoped multi-container consolidation registered")
return GridMassSort
