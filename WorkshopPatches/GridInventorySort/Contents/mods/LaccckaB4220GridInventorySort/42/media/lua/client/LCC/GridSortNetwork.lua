local GridProtocol = require("Network/GridProtocol")
local GridContainer = require("DataModel/GridContainer")
local GridClientNetwork = require("Network/GridClientNetwork")
local GridSortState = require("LCC/GridSortState")

local GridSortNetwork = {}
GridSortNetwork.pending = {}
GridSortNetwork.pendingByRequest = {}

local requestSeq = 0

local function playerNum()
    local p = getPlayer()
    return p and p:getPlayerNum() or 0
end

local function keyFor(container)
    if not container then return nil end
    local containing = container.getContainingItem and container:getContainingItem() or nil
    if containing and containing.getID then
        return "item:" .. tostring(containing:getID())
    end
    return GridContainer.containerSignature(container) or tostring(container)
end

local function nextRequestId()
    requestSeq = requestSeq + 1
    if requestSeq > 1000000000 then requestSeq = 1 end
    return tostring(getTimestampMs()) .. ":" .. tostring(requestSeq)
end

local function clearPendingKey(key)
    if not key then return end
    local p = GridSortNetwork.pending[key]
    if p and p.requestId then
        GridSortNetwork.pendingByRequest[p.requestId] = nil
    end
    GridSortNetwork.pending[key] = nil
end

local function clearPendingRequest(requestId)
    if requestId == nil then return end
    local key = GridSortNetwork.pendingByRequest[tostring(requestId)]
    if key then clearPendingKey(key) end
end

local function send(command, args)
    if not isClient() then return false end
    local player = getPlayer()
    if not player then return false end
    sendClientCommand(player, GridSortState.MODULE, command, args)
    return true
end

local function buildSortMoves(targets)
    local moves, ids = {}, {}
    for _, t in ipairs(targets or {}) do
        local item = t.item and t.item.itemObj
        if item and item.getID then
            local id = item:getID()
            table.insert(ids, id)
            table.insert(moves, {
                itemId = id,
                x = tonumber(t.tx),
                y = tonumber(t.ty),
                rotated = t.item.rotated and true or false,
            })
        end
    end
    return moves, ids
end

function GridSortNetwork.isPending(container)
    local key = keyFor(container)
    if not key then return false end
    local p = GridSortNetwork.pending[key]
    if p and getTimestampMs() - (p.startedAt or 0) > 5000 then
        clearPendingKey(key)
        print("[LCC GridSort] pending sort timed out; control unlocked")
        return false
    end
    return p ~= nil
end

function GridSortNetwork.sendSort(container, targets, gridContainer)
    if not isClient() or not container or not targets or #targets == 0 then return false end
    local ref = GridProtocol.buildContainerRef(container)
    if not ref then return false end

    local moves, itemIds = buildSortMoves(targets)
    if #moves == 0 then return false end

    local key = keyFor(container)
    if not key or GridSortNetwork.pending[key] then return false end

    local requestId = nextRequestId()
    local p = {
        requestId = requestId,
        ref = ref,
        moves = moves,
        itemIds = itemIds,
        gridContainer = gridContainer,
        phase = "prepare",
        startedAt = getTimestampMs(),
    }
    GridSortNetwork.pending[key] = p
    GridSortNetwork.pendingByRequest[requestId] = key

    if not send(GridSortState.COMMANDS.SORT_PREPARE, {
        ref = ref,
        requestId = requestId,
        itemIds = itemIds,
    }) then
        clearPendingKey(key)
        return false
    end
    return true
end

local function applyOne(args)
    if not args or args.itemId == nil then return nil end
    local item = GridClientNetwork.findItem(args.itemId)
    if not item then return nil end
    local md = item:getModData()
    md.gridPage = nil
    if args.x == nil or args.y == nil then
        md.gridX = nil
        md.gridY = nil
        md.gridRot = false
        md.gridContainer = nil
        md.gridManual = nil
    else
        md.gridX = tonumber(args.x)
        md.gridY = tonumber(args.y)
        md.gridRot = args.rotated and true or false
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
        clearPendingKey(keyFor(container))
    end
    if args and args.requestId then clearPendingRequest(args.requestId) end
end

local function handleSortToken(args)
    if not args or args.requestId == nil or args.token == nil then return end
    local requestId = tostring(args.requestId)
    local key = GridSortNetwork.pendingByRequest[requestId]
    local p = key and GridSortNetwork.pending[key] or nil
    if not p or p.requestId ~= requestId or p.phase ~= "prepare" then return end

    p.phase = "commit"
    p.startedAt = getTimestampMs()
    if not send(GridSortState.COMMANDS.SORT_REQUEST, {
        ref = p.ref,
        requestId = requestId,
        expectedToken = tostring(args.token),
        gridContainer = p.gridContainer,
        moves = p.moves,
    }) then
        clearPendingKey(key)
    end
end

local function OnServerCommand(module, command, args)
    if module ~= GridSortState.MODULE then return end
    if command == GridSortState.COMMANDS.SORT_TOKEN then
        handleSortToken(args)
        return
    end
    if command == GridSortState.COMMANDS.SYNC_LAYOUT then
        applyLayout(args)
        return
    end
    if command == GridSortState.COMMANDS.REJECT_LAYOUT then
        applyLayout(args)
        clearPendingRequest(args and args.requestId)
        local reason = args and args.reason or "unknown"
        if reason == "membership-before-sort" then
            print("[LCC GridSort] sort resynced: container membership changed before commit")
        else
            print("[LCC GridSort] server rejected layout: " .. tostring(reason)
                .. "; authoritative snapshot restored")
        end
        return
    end
end
Events.OnServerCommand.Add(OnServerCommand)

print("[LCC GridSort] simple token/CAS network installed")
return GridSortNetwork
