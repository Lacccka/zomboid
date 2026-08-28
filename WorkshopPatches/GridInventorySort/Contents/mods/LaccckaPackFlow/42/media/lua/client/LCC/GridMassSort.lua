require "TimedActions/ISInventoryTransferUtil"
require "TimedActions/ISTimedActionQueue"

local GridContainer = require("DataModel/GridContainer")
local GridSortState = require("LCC/GridSortState")
local GridSortNetwork = require("LCC/GridSortNetwork")
local GridAutoSort = require("LCC/GridAutoSort")

local GridMassSort = {}

GridMassSort.SCOPE_PLAYER = "player"
GridMassSort.SCOPE_EXTERNAL = "external"
GridMassSort.active = {}

local nextOperationId = 0

local function nowMs()
    return getTimestampMs and getTimestampMs() or (getTimeInMillis and getTimeInMillis() or 0)
end

local function getPlayerByNum(playerNum)
    if getSpecificPlayer then return getSpecificPlayer(playerNum or 0) end
    return getPlayer and getPlayer() or nil
end

local function timedQueue(player)
    if not player or not ISTimedActionQueue then return nil end
    return ISTimedActionQueue.queues and ISTimedActionQueue.queues[player] or nil
end

local function hasTimedActions(player)
    local queue = timedQueue(player)
    return queue and queue.queue and #queue.queue > 0 or false
end

local function hasForeignTimedAction(player, ownAction)
    local queue = timedQueue(player)
    if not queue or not queue.queue then return false end
    for _, action in ipairs(queue.queue) do
        if action ~= ownAction then return true end
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

-- The official page.backpacks list is the source of truth for the accessible
-- container strip. GridInventory may render only the active grid, so relying on
-- gridContainerUis would silently omit reachable containers.
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

        -- Vanilla's player-side strip primarily lists equipped containers. Add
        -- direct carried container items too; deeper nested bags remain excluded
        -- by GridAutoSort's MP nested-container guard.
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
                -- Nested/locked containers are intentionally skipped. Search
                -- visibility is not a gate: hidden loot remains hidden by item ID.
            end
        end
    end
    return records, nil
end

local function isRelocatable(item)
    if not item then return false end
    if item.isFavorite and item:isFavorite() then return false end

    -- Corpse transfer has specialized vanilla actions/semantics and grouping
    -- corpse objects by fullType is not useful duplicate consolidation.
    if item.getType then
        local t = tostring(item:getType() or "")
        if t == "CorpseMale" or t == "CorpseFemale" or t == "CorpseAnimal" then
            return false
        end
    end
    if item.isHumanCorpse then
        local ok, result = pcall(function() return item:isHumanCorpse() end)
        if ok and result then return false end
    end

    -- Moving the containing bag itself would invalidate queued child-container
    -- references. The bag still participates in the final coordinate sort.
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

            -- Movement is monotonic: smaller/later fragments only flow toward
            -- earlier/larger buckets, never back toward a smaller source bucket.
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

local function resolveMoveItem(state, move)
    if not state or not move or move.itemId == nil then return nil, nil end

    -- Never trust a saved Lua InventoryItem reference after an MP transaction:
    -- B42 may detach/replace it. Resolve the current object by stable item ID.
    for _, record in ipairs(state.records or {}) do
        local item = containerFindById(record.container, move.itemId)
        if item then
            move.item = item
            return item, record.container
        end
    end
    return nil, nil
end

local function externalContainerStillAccessible(state, container)
    local page = getPlayerLoot and getPlayerLoot(state.playerNum) or nil
    if not page then return false end
    for _, button in ipairs(page.backpacks or {}) do
        if button and button.inventory == container then return true end
    end
    local pane = page.inventoryPane
    return pane and pane.inventory == container or false
end

local function containerStillInScope(state, container)
    if not state or not container or not state.containerSet[container] then return false end
    if state.scope == GridMassSort.SCOPE_PLAYER then
        return isPlayerContainer(container, state.player)
    end
    return externalContainerStillAccessible(state, container)
end

local function canTransfer(state, item, source, destination)
    if not state or not state.player or not item or not source or not destination or source == destination then
        return false
    end
    if not containerStillInScope(state, source) or not containerStillInScope(state, destination) then
        return false
    end
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
        local ok, room = pcall(function() return destination:hasRoomFor(state.player, item) end)
        if not ok or not room then return false end
    end
    return true
end

local function chooseDestination(state, move)
    local item, source = resolveMoveItem(state, move)
    if not item or not source then return nil, nil, "missing" end
    if not containerStillInScope(state, source) then return source, nil, "scope-changed" end

    for _, destination in ipairs(move.destinations or {}) do
        if containerStillInScope(state, destination)
            and canTransfer(state, item, source, destination) then
            return source, destination, nil
        end
    end
    return source, nil, "no-destination"
end

local function abortOperation(state, reason)
    if not state then return end
    if GridMassSort.active[state.playerNum] == state then
        GridMassSort.active[state.playerNum] = nil
    end
    print("[LCC GridSort] mass " .. tostring(state.scope)
        .. " sort aborted: " .. tostring(reason or "unknown"))
end

-- Vanilla invokes this only from ISInventoryTransferAction:perform(), after the
-- MP transaction completed normally. stop()/reject/cancel paths do not invoke it.
-- The callback only marks state; OnTick advances after vanilla removes the action
-- from its queue, so PackFlow never races the current action's teardown.
local function onTransferComplete(playerNum, operationId, itemId)
    local state = GridMassSort.active[playerNum]
    if not state or state.operationId ~= operationId then return end
    local waiting = state.waitingMove
    if not waiting or not waiting.move or waiting.move.itemId ~= itemId then return end
    waiting.completed = true
end

local function queueMove(state, move, source, destination)
    local item = move and move.item or nil
    if not item then return false, "missing" end

    local action = ISInventoryTransferUtil.newInventoryTransferAction(
        state.player, item, source, destination, nil)
    if not action then return false, "unavailable" end

    -- PackFlow deliberately requires a completion callback. Vanilla's transfer
    -- action refuses to merge actions carrying callbacks, which keeps each
    -- consolidation move isolated and gives us an authoritative completion edge.
    if not action.setOnComplete then return false, "unsupported-action" end
    action:setOnComplete(onTransferComplete,
        state.playerNum, state.operationId, move.itemId)

    local queue = ISTimedActionQueue.add(action)
    if not queue then return false, "queue-rejected" end

    state.waitingMove = {
        move = move,
        action = action,
        source = source,
        destination = destination,
        completed = false,
    }
    return true, nil
end

local function finish(state)
    if GridMassSort.active[state.playerNum] == state then
        GridMassSort.active[state.playerNum] = nil
    end
    print("[LCC GridSort] mass " .. tostring(state.scope)
        .. " sort complete: " .. tostring(state.transferred) .. " transfers, "
        .. tostring(state.skippedMoves or 0) .. " skipped, "
        .. tostring(state.sortedContainers) .. " containers sorted")
end

local function tickTransfers(state)
    local waiting = state.waitingMove
    if waiting then
        local ownQueued = waiting.action and ISTimedActionQueue.hasAction
            and ISTimedActionQueue.hasAction(waiting.action)

        -- If the user queued another timed action while PackFlow's transfer is
        -- active, stop orchestration immediately but leave vanilla's queue alone.
        -- The current transfer and the user's action retain normal vanilla rules.
        if hasForeignTimedAction(state.player, waiting.action) then
            abortOperation(state, "interrupted by another timed action")
            return
        end

        if waiting.completed then
            -- setOnComplete runs just before ISBaseTimedAction.perform removes the
            -- action from ISTimedActionQueue. Wait until teardown is finished.
            if ownQueued then return end
            state.transferred = state.transferred + 1
            state.waitingMove = nil
            return
        end

        if ownQueued then
            -- No PackFlow wall-clock timeout: B42's item transaction owns the
            -- transfer duration and its animation/progress completely.
            return
        end

        -- The action disappeared without onComplete => vanilla stop/reject/cancel.
        -- Fail closed and never clear/cancel the player's action queue ourselves.
        abortOperation(state, "vanilla transfer rejected or cancelled for item "
            .. tostring(waiting.move and waiting.move.itemId))
        return
    end

    if hasTimedActions(state.player) then
        abortOperation(state, "interrupted by another timed action")
        return
    end

    while state.moveIndex <= #state.moves do
        local move = state.moves[state.moveIndex]
        state.moveIndex = state.moveIndex + 1

        local source, destination, reason = chooseDestination(state, move)
        if reason == "scope-changed" then
            abortOperation(state, "container scope changed during consolidation")
            return
        end

        if source and destination then
            local queued, queueReason = queueMove(state, move, source, destination)
            if queued then return end
            if queueReason == "unsupported-action" then
                -- A specialized vanilla action without transfer completion
                -- semantics is outside PackFlow's consolidation contract.
                state.skippedMoves = state.skippedMoves + 1
            else
                abortOperation(state, "could not queue vanilla transfer: "
                    .. tostring(queueReason))
                return
            end
        else
            state.skippedMoves = state.skippedMoves + 1
        end
    end

    state.phase = "sort"
    state.sortIndex = 1
    state.sortWaiting = nil
    state.sortRetrySince = nil
end

local function tickSort(state)
    -- A user action invalidates the mass operation's original snapshot. Do not
    -- wait and later resume with stale assumptions.
    if hasTimedActions(state.player) then
        abortOperation(state, "interrupted before coordinate sort")
        return
    end

    local record = state.records[state.sortIndex]
    if not record then
        finish(state)
        return
    end

    if state.scope == GridMassSort.SCOPE_EXTERNAL
        and not containerStillInScope(state, record.container) then
        abortOperation(state, "container scope changed before coordinate sort")
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
    -- Never cancel or clear vanilla timed actions here. If PackFlow itself
    -- faults, drop only our orchestration state; an already-started vanilla
    -- transfer is allowed to complete using the game's normal transaction path.
    if state and GridMassSort.active[state.playerNum] == state then
        GridMassSort.active[state.playerNum] = nil
    end
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

-- Lightweight footer-state check. It intentionally does not build GridContainer
-- models or enumerate item contents; full validation happens only on click.
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

    nextOperationId = nextOperationId + 1
    local moves = buildMovePlan(records)
    GridMassSort.active[playerNum] = {
        operationId = nextOperationId,
        playerNum = playerNum,
        player = player,
        scope = scope,
        records = records,
        containerSet = containerSet,
        moves = moves,
        moveIndex = 1,
        phase = "transfer",
        transferred = 0,
        skippedMoves = 0,
        sortedContainers = 0,
        waitingMove = nil,
    }

    print("[LCC GridSort] mass " .. tostring(scope) .. " sort started: "
        .. tostring(#records) .. " containers, " .. tostring(#moves)
        .. " duplicate consolidation candidates; transfer=vanilla-callback")
    return true, "started"
end

if not GridMassSort._eventRegistered then
    GridMassSort._eventRegistered = true
    Events.OnTick.Add(onTick)
end

print("[LCC GridSort] scoped multi-container consolidation registered")
return GridMassSort
