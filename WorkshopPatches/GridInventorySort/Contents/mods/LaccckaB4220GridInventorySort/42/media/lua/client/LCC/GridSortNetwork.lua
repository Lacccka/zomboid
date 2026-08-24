local GridProtocol = require("Network/GridProtocol")
local GridContainer = require("DataModel/GridContainer")
local GridClientNetwork = require("Network/GridClientNetwork")
local GridSortState = require("LCC/GridSortState")

local GridSortNetwork = {}
GridSortNetwork.pending = {}

local function playerNum()
    local p = getPlayer()
    return p and p:getPlayerNum() or 0
end

local function keyFor(container)
    if not container then return nil end
    -- GridContainer.containerSignature() intentionally identifies bags by type,
    -- which is fine for saved placement semantics but NOT for a pending network
    -- transaction: two identical Nomad bags must not lock/clear each other.
    local containing = container.getContainingItem and container:getContainingItem() or nil
    if containing and containing.getID then
        return "item:" .. tostring(containing:getID())
    end
    return GridContainer.containerSignature(container) or tostring(container)
end

local function findGridPage(container, itemId)
    local gc = GridContainer.getOrCreate(container, playerNum())
    for page, grid in ipairs(gc.grids or {}) do
        if (grid.items and grid.items[itemId]) or (grid.ghostItems and grid.ghostItems[itemId]) then
            return page
        end
    end
    return 1
end

local function containerHasExtraPages(container)
    if not container or GridSortState.isPlayerRootContainer(container) then return false end
    local gc = GridContainer.getOrCreate(container, playerNum())
    if gc and gc.grids and #gc.grids > 1 then return true end
    if container.getItems then
        local items = container:getItems()
        for i = 0, items:size() - 1 do
            local item = items:get(i)
            local md = item and item.getModData and item:getModData() or nil
            if md and tonumber(md.gridPage) and tonumber(md.gridPage) > 1 then
                return true
            end
        end
    end
    return false
end

local function sourceRefFor(itemId)
    local item = GridClientNetwork.findItem(itemId)
    local src = item and item.getContainer and item:getContainer() or nil
    return src and GridProtocol.buildContainerRef(src) or nil
end

local function send(command, args)
    if not isClient() then return false end
    local player = getPlayer()
    if not player then return false end
    sendClientCommand(player, GridSortState.MODULE, command, args)
    return true
end

function GridSortNetwork.isPending(container)
    local key = keyFor(container)
    if not key then return false end
    local p = GridSortNetwork.pending[key]
    if p and getTimestampMs() - (p.startedAt or 0) > 5000 then
        GridSortNetwork.pending[key] = nil
        print("[LCC GridSort] pending sort timed out; control unlocked")
        return false
    end
    return p ~= nil
end

function GridSortNetwork.sendSort(container, targets, expectedHash, gridContainer)
    if not isClient() then return false end
    if not container or not targets or #targets == 0 then return false end
    local ref = GridProtocol.buildContainerRef(container)
    if not ref then return false end

    local moves = {}
    for _, t in ipairs(targets) do
        local item = t.item and t.item.itemObj
        if item and item.getID then
            table.insert(moves, {
                itemId = item:getID(),
                x = tonumber(t.tx),
                y = tonumber(t.ty),
                page = tonumber(t.page) or 1,
                rotated = t.item.rotated and true or false,
            })
        end
    end
    if #moves == 0 then return false end

    local key = keyFor(container)
    local ok = send(GridSortState.COMMANDS.SORT_REQUEST, {
        ref = ref,
        expectedHash = expectedHash,
        gridContainer = gridContainer,
        moves = moves,
    })
    if not ok then return false end
    GridSortNetwork.pending[key] = {
        expectedHash = expectedHash,
        startedAt = getTimestampMs(),
    }
    return true
end

function GridSortNetwork.sendPageAssignments(container, assignments, gridContainer)
    if not isClient() then return false end
    if not container or not assignments or #assignments == 0 then return false end
    if GridSortState.isPlayerRootContainer(container) then return false end
    local ref = GridProtocol.buildContainerRef(container)
    if not ref then return false end

    local moves = {}
    for _, a in ipairs(assignments) do
        if a.itemId ~= nil and a.x ~= nil and a.y ~= nil then
            table.insert(moves, {
                itemId = a.itemId,
                x = tonumber(a.x), y = tonumber(a.y),
                page = tonumber(a.page) or 1,
                rotated = a.rotated and true or false,
            })
        end
    end
    if #moves == 0 then return false end

    return send(GridSortState.COMMANDS.PAGE_ASSIGN, {
        ref = ref,
        gridContainer = gridContainer,
        moves = moves,
    })
end

function GridSortNetwork.sendPageMove(container, itemId, x, y, rotated, gridContainer, manual, page)
    local ref = GridProtocol.buildContainerRef(container)
    if not ref then return false end
    return send(GridSortState.COMMANDS.PAGE_MOVE, {
        ref = ref,
        sourceRef = sourceRefFor(itemId),
        itemId = itemId,
        x = tonumber(x), y = tonumber(y),
        page = tonumber(page) or 1,
        rotated = rotated and true or false,
        gridContainer = gridContainer,
        manual = manual and true or nil,
    })
end

function GridSortNetwork.sendPageClear(container, itemId)
    local ref = GridProtocol.buildContainerRef(container)
    if not ref then return false end
    return send(GridSortState.COMMANDS.PAGE_CLEAR, {
        ref = ref,
        sourceRef = sourceRefFor(itemId),
        itemId = itemId,
    })
end

function GridSortNetwork.sendPageReorder(container, targets, gridContainer)
    local ref = GridProtocol.buildContainerRef(container)
    if not ref then return false end
    local moves = {}
    for _, t in ipairs(targets or {}) do
        local item = t.item and t.item.itemObj
        if item and item.getID then
            local id = item:getID()
            table.insert(moves, {
                itemId = id,
                x = tonumber(t.tx), y = tonumber(t.ty),
                page = tonumber(t.page) or findGridPage(container, id),
                rotated = t.item.rotated and true or false,
            })
        end
    end
    if #moves == 0 then return false end
    return send(GridSortState.COMMANDS.PAGE_REORDER, {
        ref = ref,
        gridContainer = gridContainer,
        manual = true,
        moves = moves,
    })
end

local function applyOne(args)
    if not args or args.itemId == nil then return nil end
    local item = GridClientNetwork.findItem(args.itemId)
    if not item then return nil end
    local md = item:getModData()
    if args.clear or args.x == nil or args.y == nil then
        md.gridX = nil
        md.gridY = nil
        md.gridRot = false
        md.gridPage = nil
        md.gridContainer = nil
        md.gridManual = nil
    else
        md.gridX = tonumber(args.x)
        md.gridY = tonumber(args.y)
        md.gridRot = args.rotated and true or false
        local page = tonumber(args.page) or 1
        md.gridPage = page > 1 and page or nil
        if args.gridContainer ~= nil then md.gridContainer = args.gridContainer end
        md.gridManual = args.manual and true or nil
    end
    return item.getContainer and item:getContainer() or nil
end

local function applyLayout(args)
    local touched = {}
    for _, move in ipairs((args and args.moves) or {}) do
        local container = applyOne(move)
        if container then touched[container] = true end
    end
    for container in pairs(touched) do
        GridClientNetwork.markGridChanged(container, playerNum())
        GridSortNetwork.pending[keyFor(container)] = nil
    end
end

local function OnServerCommand(module, command, args)
    if module ~= GridSortState.MODULE then return end
    if command == GridSortState.COMMANDS.SYNC_ITEM then
        local container = applyOne(args)
        if container then GridClientNetwork.markGridChanged(container, playerNum()) end
        return
    end
    if command == GridSortState.COMMANDS.SYNC_LAYOUT then
        applyLayout(args)
        return
    end
    if command == GridSortState.COMMANDS.REJECT_LAYOUT then
        applyLayout(args)
        print("[LCC GridSort] server rejected layout: " .. tostring(args and args.reason or "unknown") .. "; authoritative snapshot restored")
        return
    end
end
Events.OnServerCommand.Add(OnServerCommand)

-- Preserve upstream networking only while the container is genuinely a normal
-- one-page container. Once ANY extra page exists, all moves/reorders in that
-- container use the page-aware server path. Otherwise upstream buildOccupancy()
-- would count page-2 coordinates as page 1 and reject innocent page-1 moves.
local originalSendItemMove = GridClientNetwork.sendItemMove
GridClientNetwork.sendItemMove = function(container, itemId, x, y, rotated, gridContainer, manual)
    local item = GridClientNetwork.findItem(itemId)
    local md = item and item.getModData and item:getModData() or nil
    local oldPage = md and tonumber(md.gridPage) or 1
    local targetPage = findGridPage(container, itemId)
    if containerHasExtraPages(container) or oldPage > 1 or targetPage > 1 then
        if md and not GridSortState.isPlayerRootContainer(container) then
            md.gridPage = targetPage > 1 and targetPage or nil
        end
        return GridSortNetwork.sendPageMove(container, itemId, x, y, rotated,
            gridContainer, manual, targetPage)
    end
    return originalSendItemMove(container, itemId, x, y, rotated, gridContainer, manual)
end

local originalSendReorder = GridClientNetwork.sendReorder
GridClientNetwork.sendReorder = function(container, targets, gridContainer)
    local pageAware = containerHasExtraPages(container)
    if not pageAware then
        for _, t in ipairs(targets or {}) do
            local item = t.item and t.item.itemObj
            if item and item.getID then
                local md = item.getModData and item:getModData() or nil
                if (md and tonumber(md.gridPage) and tonumber(md.gridPage) > 1)
                    or findGridPage(container, item:getID()) > 1 then
                    pageAware = true
                    break
                end
            end
        end
    end
    if pageAware then
        return GridSortNetwork.sendPageReorder(container, targets, gridContainer)
    end
    return originalSendReorder(container, targets, gridContainer)
end

local originalClearServerPosition = GridClientNetwork.clearServerPosition
GridClientNetwork.clearServerPosition = function(container, itemId)
    local item = GridClientNetwork.findItem(itemId)
    local md = item and item.getModData and item:getModData() or nil
    if containerHasExtraPages(container)
        or (md and tonumber(md.gridPage) and tonumber(md.gridPage) > 1) then
        if md and not GridSortState.isPlayerRootContainer(container) then md.gridPage = nil end
        return GridSortNetwork.sendPageClear(container, itemId)
    end
    return originalClearServerPosition(container, itemId)
end

print("[LCC GridSort] page-aware MP network installed")
return GridSortNetwork
