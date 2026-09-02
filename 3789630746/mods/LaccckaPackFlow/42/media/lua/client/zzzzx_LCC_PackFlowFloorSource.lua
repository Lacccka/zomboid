require "TimedActions/ISTimedActionQueue"

local okMass, GridMassSort = pcall(require, "LCC/GridMassSort")
if not okMass or not GridMassSort then return end
if GridMassSort._lccFloorSourceInstalled then return end
GridMassSort._lccFloorSourceInstalled = true

local GridContainer = require("DataModel/GridContainer")
local GridSortState = require("LCC/GridSortState")
local GridAutoSort = require("LCC/GridAutoSort")
local LCCMassInventoryTransferAction = require("LCC/GridMassInventoryTransferAction")

local floorActive = {}
local SETTLE_MS = 150

local originalStart = GridMassSort.start
local originalIsBusy = GridMassSort.isBusy
local originalCanStart = GridMassSort.canStart

local function nowMs()
    return getTimestampMs and getTimestampMs()
        or (getTimeInMillis and getTimeInMillis() or 0)
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

local function isFloor(container)
    return container and container.getType and container:getType() == "floor" or false
end

local function isCorpseContainer(container)
    local parent = container and container.getParent and container:getParent() or nil
    return parent and instanceof and instanceof(parent, "IsoDeadBody") or false
end

local function isPlayerContainer(container, player)
    if not container or not player then return false end
    if player.getInventory and container == player:getInventory() then return true end
    if container.isInCharacterInventory then
        local ok, result = pcall(function()
            return container:isInCharacterInventory(player)
        end)
        if ok and result then return true end
    end
    return false
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

local function stackUnits(item)
    local _, stackInfo = GridContainer.getStackInfo(item)
    local units = stackInfo and tonumber(stackInfo.units) or 1
    return units and math.max(1, units) or 1
end

local function isRelocatable(item)
    if not item then return false end
    if item.isFavorite and item:isFavorite() then return false end
    if item.getInventory then
        local ok, inventory = pcall(function() return item:getInventory() end)
        if ok and inventory then return false end
    end
    return true
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

local function containerLabel(container)
    if not container then return "nil" end
    local ctype = container.getType and container:getType() or "?"
    local parent = container.getParent and container:getParent() or nil
    local square = parent and parent.getSquare and parent:getSquare() or nil
    if square then
        return tostring(ctype) .. "@"
            .. tostring(square:getX()) .. ","
            .. tostring(square:getY()) .. ","
            .. tostring(square:getZ())
    end
    local containingItem = container.getContainingItem and container:getContainingItem() or nil
    if containingItem and containingItem.getID then
        return tostring(ctype) .. "#item:" .. tostring(containingItem:getID())
    end
    return tostring(ctype)
end

local function collectExternalParticipants(playerNum)
    local player = getPlayerByNum(playerNum)
    local page = getPlayerLoot and getPlayerLoot(playerNum) or nil
    local pane = page and page.inventoryPane or nil
    if not player or not page or not pane then return {}, {} end

    local seen = {}
    local floors = {}
    local destinations = {}

    local function add(container, index)
        if not container or seen[container] then return end
        seen[container] = true
        if isCorpseContainer(container) or isPlayerContainer(container, player) then return end

        if isFloor(container) then
            table.insert(floors, { index = index, container = container })
            return
        end

        local gridUi = findGridUi(pane, container)
        local can, reason = GridAutoSort.canSortContainer(container, playerNum, gridUi)
        if can then
            table.insert(destinations, {
                index = index,
                container = container,
                gridUi = gridUi,
            })
        elseif reason == "busy" then
            return
        end
    end

    for index, button in ipairs(page.backpacks or {}) do
        if button and button.inventory then add(button.inventory, index) end
    end
    if pane.inventory then add(pane.inventory, #(page.backpacks or {}) + 1) end

    return floors, destinations
end

local function buildFloorMoves(playerNum)
    local floors, destinations = collectExternalParticipants(playerNum)
    if #floors == 0 or #destinations == 0 then return {}, #floors, #destinations end

    local byType = {}
    for _, record in ipairs(destinations) do
        for _, item in ipairs(GridSortState.collectItems(record.container)) do
            local key = itemFullType(item)
            local bucket = byType[key]
            if not bucket then
                bucket = {}
                byType[key] = bucket
            end
            local entry = bucket[record.container]
            if not entry then
                entry = { record = record, units = 0 }
                bucket[record.container] = entry
            end
            entry.units = entry.units + stackUnits(item)
        end
    end

    local moves = {}
    for _, floorRecord in ipairs(floors) do
        for _, item in ipairs(GridSortState.collectItems(floorRecord.container)) do
            if isRelocatable(item) then
                local key = itemFullType(item)
                local matches = byType[key]
                if matches then
                    local ranked = {}
                    for _, entry in pairs(matches) do table.insert(ranked, entry) end
                    table.sort(ranked, function(a, b)
                        if a.units ~= b.units then return a.units > b.units end
                        return a.record.index < b.record.index
                    end)

                    local destinationList = {}
                    for _, entry in ipairs(ranked) do
                        table.insert(destinationList, entry.record.container)
                    end
                    if #destinationList > 0 then
                        table.insert(moves, {
                            item = item,
                            itemId = item:getID(),
                            source = floorRecord.container,
                            destinations = destinationList,
                        })
                    end
                end
            end
        end
    end

    table.sort(moves, function(a, b)
        local at, bt = itemFullType(a.item), itemFullType(b.item)
        if at ~= bt then return at < bt end
        local ai, bi = tonumber(a.itemId), tonumber(b.itemId)
        if ai and bi then return ai < bi end
        return tostring(a.itemId) < tostring(b.itemId)
    end)

    return moves, #floors, #destinations
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

local function containerContainsId(container, itemId)
    if not container or itemId == nil or not container.getItems then return false end
    if container.getItemWithID then
        local ok, direct = pcall(function() return container:getItemWithID(itemId) end)
        if ok and direct then return true end
    end
    local items = container:getItems()
    if not items then return false end
    for i = 0, items:size() - 1 do
        local item = items:get(i)
        if item and item.getID and item:getID() == itemId then return true end
    end
    return false
end

local function chooseDestination(state, move)
    local item = move.item
    local source = item and item.getContainer and item:getContainer() or nil
    if not source or not isFloor(source) then return nil, nil end

    for _, destination in ipairs(move.destinations or {}) do
        if canTransfer(state.player, item, source, destination) then
            return source, destination
        end
    end
    return source, nil
end

local function queueMove(state, move, source, destination)
    local action = LCCMassInventoryTransferAction:new(
        state.player, move.item, source, destination, nil)
    if not action then return false end
    local queue = ISTimedActionQueue.add(action)
    if not queue then return false end

    state.waiting = {
        action = action,
        move = move,
        source = source,
        destination = destination,
    }
    print("[LCC GridSort][floor-source] queued item=" .. tostring(move.itemId)
        .. " type=" .. tostring(itemFullType(move.item))
        .. " source=" .. containerLabel(source)
        .. " destination=" .. containerLabel(destination))
    return true
end

local function finishFloorPhase(state)
    floorActive[state.playerNum] = nil
    print("[LCC GridSort][floor-source] complete moved=" .. tostring(state.moved)
        .. " unconfirmed=" .. tostring(state.unconfirmed)
        .. "; starting normal external mass sort")

    local ok, reason = originalStart(GridMassSort.SCOPE_EXTERNAL, state.playerNum)
    if not ok then
        print("[LCC GridSort][floor-source] normal external sort not started: "
            .. tostring(reason))
    end
end

local function tickFloorState(state)
    local waiting = state.waiting
    if waiting then
        local stillQueued = waiting.action and ISTimedActionQueue.hasAction
            and ISTimedActionQueue.hasAction(waiting.action)
        if stillQueued then return end

        if containerContainsId(waiting.destination, waiting.move.itemId)
            or not containerContainsId(waiting.source, waiting.move.itemId) then
            state.moved = state.moved + 1
        else
            state.unconfirmed = state.unconfirmed + 1
            print("[LCC GridSort][floor-source] transfer rejected/cancelled item="
                .. tostring(waiting.move.itemId))
        end
        state.waiting = nil
        state.settleUntil = nowMs() + SETTLE_MS
        return
    end

    if state.settleUntil and nowMs() < state.settleUntil then return end
    state.settleUntil = nil
    if hasTimedActions(state.player) then return end

    while state.moveIndex <= #state.moves do
        local move = state.moves[state.moveIndex]
        state.moveIndex = state.moveIndex + 1
        local source, destination = chooseDestination(state, move)
        if source and destination and queueMove(state, move, source, destination) then return end
    end

    finishFloorPhase(state)
end

local function onTick()
    for playerNum, state in pairs(floorActive) do
        local ok, err = pcall(tickFloorState, state)
        if not ok then
            floorActive[playerNum] = nil
            print("[LCC GridSort][floor-source] aborted after unexpected error: "
                .. tostring(err))
        end
    end
end

function GridMassSort.isBusy(playerNum)
    playerNum = playerNum or 0
    return floorActive[playerNum] ~= nil or originalIsBusy(playerNum)
end

function GridMassSort.canStart(scope, playerNum)
    playerNum = playerNum or 0
    if floorActive[playerNum] then return false, "busy" end
    return originalCanStart(scope, playerNum)
end

function GridMassSort.start(scope, playerNum)
    playerNum = playerNum or 0
    if scope ~= GridMassSort.SCOPE_EXTERNAL then
        return originalStart(scope, playerNum)
    end
    if floorActive[playerNum] or originalIsBusy(playerNum) then return false, "busy" end

    local player = getPlayerByNum(playerNum)
    if not player then return false, "unavailable" end
    if hasTimedActions(player) then return false, "busy" end

    local moves, floorCount, destinationCount = buildFloorMoves(playerNum)
    if #moves == 0 then
        return originalStart(scope, playerNum)
    end

    floorActive[playerNum] = {
        playerNum = playerNum,
        player = player,
        moves = moves,
        moveIndex = 1,
        waiting = nil,
        settleUntil = nil,
        moved = 0,
        unconfirmed = 0,
    }

    print("[LCC GridSort][floor-source] started floors=" .. tostring(floorCount)
        .. " destinations=" .. tostring(destinationCount)
        .. " candidates=" .. tostring(#moves))
    return true, "started"
end

Events.OnTick.Add(onTick)
print("[LCC GridSort] floor items registered as external consolidation sources")
