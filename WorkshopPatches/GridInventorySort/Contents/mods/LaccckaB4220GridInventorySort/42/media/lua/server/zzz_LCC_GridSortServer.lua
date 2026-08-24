if not isServer() then return end

local GridProtocol = require("Network/GridProtocol")
local GridContainer = require("DataModel/GridContainer")
local GridCore = require("DataModel/GridCore")
local ItemFootprint = require("Algorithm/ItemFootprint")
local GridSortState = require("LCC/GridSortState")
local GridContinuousGrid = require("LCC/GridContinuousGrid")

local GridSortServer = {}

-- Current GridInventory has a reported dedicated-server failure in
-- GridContainer.buildOccupancy while moving a nested container with contents:
-- its captured ItemFootprint can be nil in that path, producing
-- "attempted index: getSize of non-table: null" and, in the worst case,
-- wedging the command-processing loop. Keep the upstream algorithm, but bind it
-- here to the known-good shared modules that have already loaded on this server.
if not GridContainer._lccSafeBuildOccupancyInstalled then
    GridContainer._lccSafeBuildOccupancyInstalled = true

    function GridContainer.buildOccupancy(container, grid)
        if not container or not grid or not container.getItems then return end
        local items = container:getItems()
        if not items then return end

        for i = 0, items:size() - 1 do
            local item = items:get(i)
            if GridSortState.isGridItem(item) then
                local md = item:getModData()
                local sx = md and tonumber(md.gridX) or nil
                local sy = md and tonumber(md.gridY) or nil
                if sx and sy then
                    local rotated = md.gridRot and true or false
                    local fw, fh = ItemFootprint.getSize(item)
                    local ew, eh = rotated and fh or fw, rotated and fw or fh
                    local compatKey, stackInfo = GridContainer.getStackInfo(item)
                    grid:insertItem(item:getID(), sx, sy, ew, eh, rotated,
                        item, compatKey, stackInfo)
                end
            end
        end
    end

    print("[LCC GridSort] server safe occupancy installed")
end

local function resolveSortableTarget(player, ref)
    local target = ref and GridProtocol.resolveContainerRef(ref, player) or nil
    if not target then return nil end
    if target.getType and target:getType() == "floor" then return nil end
    local parent = target.getParent and target:getParent()
    if parent and instanceof and instanceof(parent, "IsoDeadBody") then return nil end
    GridContinuousGrid.normalizeLegacyPages(target)
    return target
end

local function sendSnapshot(player, container, reason, requestId)
    sendServerCommand(player, GridSortState.MODULE, GridSortState.COMMANDS.REJECT_LAYOUT, {
        requestId = requestId,
        reason = reason,
        authoritativeHash = GridSortState.authorityHash(container),
        moves = GridSortState.snapshot(container),
    })
end

local function sameIdSet(container, ids)
    local expected = {}
    local items = GridSortState.collectItems(container)
    for _, item in ipairs(items) do expected[item:getID()] = true end

    local count = 0
    for _, id in ipairs(ids or {}) do
        if id == nil or expected[id] ~= true then return false end
        expected[id] = nil
        count = count + 1
    end
    if count ~= #items then return false end
    for _ in pairs(expected) do return false end
    return true
end

local function sameMoveSet(container, moves)
    local ids = {}
    for _, move in ipairs(moves or {}) do
        table.insert(ids, move.itemId)
    end
    return sameIdSet(container, ids)
end

local function directItemMap(container)
    local out = {}
    for _, item in ipairs(GridSortState.collectItems(container)) do
        out[item:getID()] = item
    end
    return out
end

local function processSortPrepare(player, args)
    if not args or not args.ref or args.requestId == nil then return "invalid" end
    local target = resolveSortableTarget(player, args.ref)
    if not target then return "invalid" end

    -- Fail early if the client's physical ItemContainer view is one replication
    -- step behind the dedicated server. Do not issue a CAS token for a layout
    -- that can never be a complete authoritative batch.
    if not sameIdSet(target, args.itemIds or {}) then
        sendSnapshot(player, target, "membership-before-sort", args.requestId)
        return "handled"
    end

    sendServerCommand(player, GridSortState.MODULE, GridSortState.COMMANDS.SORT_TOKEN, {
        requestId = tostring(args.requestId),
        token = GridSortState.authorityHash(target),
    })
    return "ok"
end

local function processSort(player, args)
    if not args or not args.ref or not args.moves or #args.moves == 0 then return "invalid" end
    local target = resolveSortableTarget(player, args.ref)
    if not target then return "invalid" end

    local currentToken = GridSortState.authorityHash(target)
    if tostring(args.expectedToken or "") ~= tostring(currentToken) then
        sendSnapshot(player, target, "stale", args.requestId)
        return "handled"
    end
    if not sameMoveSet(target, args.moves) then
        sendSnapshot(player, target, "membership", args.requestId)
        return "handled"
    end

    local w, h = GridContainer.getGridSize(target)
    local grid = GridCore.new(w, h)
    local itemMap = directItemMap(target)
    local resolved = {}

    for _, move in ipairs(args.moves) do
        if move.itemId == nil or move.x == nil or move.y == nil then
            sendSnapshot(player, target, "bounds", args.requestId)
            return "handled"
        end

        local item = itemMap[move.itemId]
        if not item then
            sendSnapshot(player, target, "missing-item", args.requestId)
            return "handled"
        end

        local fw, fh = ItemFootprint.getSize(item)
        local rotated = move.rotated and true or false
        local ew, eh = rotated and fh or fw, rotated and fw or fh
        local compatKey, stackInfo = GridContainer.getStackInfo(item)
        if not grid:insertItem(item:getID(), tonumber(move.x), tonumber(move.y), ew, eh,
            rotated, item, compatKey, stackInfo) then
            sendSnapshot(player, target, "collision", args.requestId)
            return "handled"
        end

        table.insert(resolved, {
            item = item,
            x = tonumber(move.x), y = tonumber(move.y),
            rotated = rotated,
        })
    end

    -- Server event processing is sequential, but recheck the revision immediately
    -- before commit so a mutation made by another command cannot be partially
    -- overwritten by this full-layout transaction.
    if GridSortState.authorityHash(target) ~= currentToken then
        sendSnapshot(player, target, "stale-after-validate", args.requestId)
        return "handled"
    end

    for _, entry in ipairs(resolved) do
        local md = entry.item:getModData()
        md.gridX = entry.x
        md.gridY = entry.y
        md.gridRot = entry.rotated
        md.gridPage = nil
        if args.gridContainer ~= nil then md.gridContainer = args.gridContainer end
        md.gridManual = true
    end

    sendServerCommand(GridSortState.MODULE, GridSortState.COMMANDS.SYNC_LAYOUT, {
        requestId = args.requestId,
        authoritativeHash = GridSortState.authorityHash(target),
        moves = GridSortState.snapshot(target),
    })
    return "ok"
end

local function OnClientCommand(module, command, player, args)
    if module ~= GridSortState.MODULE or not player then return end

    local status = "ignore"
    if command == GridSortState.COMMANDS.SORT_PREPARE then
        status = processSortPrepare(player, args)
    elseif command == GridSortState.COMMANDS.SORT_REQUEST then
        status = processSort(player, args)
    end

    if status == "invalid" then
        local target = args and args.ref and resolveSortableTarget(player, args.ref) or nil
        if target then sendSnapshot(player, target, "invalid", args and args.requestId) end
    end
end
Events.OnClientCommand.Add(OnClientCommand)

print("[LCC GridSort] server simple token/CAS authority installed")
return GridSortServer
