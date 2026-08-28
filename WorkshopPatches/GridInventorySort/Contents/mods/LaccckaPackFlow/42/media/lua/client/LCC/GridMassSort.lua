require "TimedActions/ISInventoryTransferAction"
require "TimedActions/ISTimedActionQueue"

local GridContainer = require("DataModel/GridContainer")
local GridSortState = require("LCC/GridSortState")
local GridSortNetwork = require("LCC/GridSortNetwork")
local GridAutoSort = require("LCC/GridAutoSort")

local GridMassSort = {}

GridMassSort.SCOPE_PLAYER = "player"
GridMassSort.SCOPE_EXTERNAL = "external"
GridMassSort.active = {}

local MOVE_TIMEOUT_MS = 45000
local SETTLE_MS = 150

local function nowMs()
    return getTimestampMs and getTimestampMs() or (getTimeInMillis and getTimeInMillis() or 0)
end

local function getPlayerByNum(playerNum)
    if getSpecificPlayer then return getSpecificPlayer(playerNum or 0) end
    return getPlayer and getPlayer() or nil
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

local function collectScopeGrids(scope, playerNum, requireIdle)
    local player = getPlayerByNum(playerNum)
    local page = pageForScope(scope, playerNum)
    local pane = page and page.inventoryPane or nil
    if not player or not pane or not pane.gridContainerUis then return {} end

    local seen, out = {}, {}
    for index, gridUi in ipairs(pane.gridContainerUis) do
        local container = gridUi and not gridUi.isOverflow and gridUi.inventoryContainer or nil
        if container and not seen[container]
            and not (container.getType and container:getType() == "floor")
            and not isCorpseContainer(container) then
            local owned = isPlayerContainer(container, player)
            local inScope = (scope == GridMassSort.SCOPE_PLAYER and owned)
                or (scope == GridMassSort.SCOPE_EXTERNAL and not owned)
            if inScope then
                local can, reason = GridAutoSort.canSort(gridUi)
                if can or (not requireIdle and reason == "busy") then
                    seen[container] = true
                    table.insert(out, {
                        index = index,
                        gridUi = gridUi,
                        container = container,
                    })
                end
            end
        end
    end
    return out
end

local function isRelocatable(item)
    if not item then return false end
    if item.isFavorite and item:isFavorite() then return false end
    -- Never physically move a bag/container during a bulk pass. Its ref and
    -- child inventory can otherwise change while later queued moves still refer
    -- to that tree. The bag itself is still positioned by the per-grid sorter.
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
    local groups = {}
    local groupList = {}

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

            -- Move smaller fragments toward the containers that already hold the
            -- largest amount of this exact item type. Alternatives are limited to
            -- earlier/larger buckets, so the pass converges instead of shuffling
            -- duplicates back and forth when the primary destination is full.
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
                        source = source.record.container,
                        destinations = destinations,
                        groupKey = group.key,
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
    if not source then return nil, nil end

    for _, destination in ipairs(move.destinations or {}) do
        if state.containerSet[destination] and canTransfer(state.player, item, source, destination) then
            return source, destination
        end
    end
    return source, nil
end

local function queueMove(state, move, source, destination)
    local action = ISInventoryTransferAction:new(state.player, move.item, source, destination, nil)
    if not action then return false end
    ISTimedActionQueue.add(action)
    state.waitingMove = {
        move = move,
        source = source,
        destination = destination,
        startedAt = nowMs(),
    }
    return true
end

local function finish(state)
    GridMassSort.active[state.playerNum] = nil
    print("[LCC GridSort] mass " .. tostring(state.scope)
        .. " sort complete: " .. tostring(state.transferred) .. " transfers, "
        .. tostring(state.sortedContainers) .. " containers sorted")
end

local function tickTransfers(state)
    local waiting = state.waitingMove
    if waiting then
        local item = waiting.move and waiting.move.item or nil
        local current = item and item.getContainer and item:getContainer() or nil
        if current == waiting.destination then
            state.transferred = state.transferred + 1
            state.waitingMove = nil
            state.settleUntil = nowMs() + SETTLE_MS
            return
        end
        if nowMs() - waiting.startedAt < MOVE_TIMEOUT_MS then return end

        print("[LCC GridSort] mass transfer timed out for item "
            .. tostring(waiting.move and waiting.move.itemId))
        state.waitingMove = nil
        state.settleUntil = nowMs() + SETTLE_MS
        return
    end

    if state.settleUntil and nowMs() < state.settleUntil then return end
    state.settleUntil = nil

    while state.moveIndex <= #state.moves do
        local move = state.moves[state.moveIndex]
        state.moveIndex = state.moveIndex + 1
        local source, destination = chooseDestination(state, move)
        if source and destination and queueMove(state, move, source, destination) then
            return
        end
    end

    state.phase = "sort"
    state.sortIndex = 1
    state.sortWaiting = nil
    state.sortRetrySince = nil
end

local function tickSort(state)
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

    local ok, reason = GridAutoSort.sort(record.gridUi)
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

local function onTick()
    for _, state in pairs(GridMassSort.active) do
        if state.phase == "transfer" then
            tickTransfers(state)
        elseif state.phase == "sort" then
            tickSort(state)
        else
            finish(state)
        end
    end
end

function GridMassSort.isBusy(playerNum)
    return GridMassSort.active[playerNum or 0] ~= nil
end

function GridMassSort.canStart(scope, playerNum)
    playerNum = playerNum or 0
    if GridMassSort.isBusy(playerNum) then return false, "busy" end
    local records = collectScopeGrids(scope, playerNum, true)
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
    local can, reason, records = GridMassSort.canStart(scope, playerNum)
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
        sortedContainers = 0,
        waitingMove = nil,
        settleUntil = nil,
    }

    print("[LCC GridSort] mass " .. tostring(scope) .. " sort started: "
        .. tostring(#records) .. " containers, " .. tostring(#moves)
        .. " duplicate consolidation candidates")
    return true, "started"
end

if not GridMassSort._eventRegistered then
    GridMassSort._eventRegistered = true
    Events.OnTick.Add(onTick)
end

print("[LCC GridSort] scoped multi-container consolidation registered")
return GridMassSort
