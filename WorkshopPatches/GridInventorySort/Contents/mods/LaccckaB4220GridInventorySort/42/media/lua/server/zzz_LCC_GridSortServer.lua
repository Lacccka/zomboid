if not isServer() then return end

local GridProtocol = require("Network/GridProtocol")
local GridContainer = require("DataModel/GridContainer")
local GridCore = require("DataModel/GridCore")
local ItemFootprint = require("Algorithm/ItemFootprint")
local GridSortState = require("LCC/GridSortState")

local GridSortServer = {}

local pending = {}
local PENDING_RETRIES = 30
local PENDING_DELAY_MS = 100

local function findItem(player, ref, itemId, sourceRef)
    local item = GridProtocol.findItemInTree(player:getInventory(), itemId)
    if item then return item end

    local target = GridProtocol.resolveContainerRef(ref, player)
    if target then
        item = GridProtocol.findItemInTree(target, itemId)
        if item then return item end
    end

    if sourceRef then
        local src = GridProtocol.resolveContainerRef(sourceRef, player)
        if src then
            item = GridProtocol.findItemInTree(src, itemId)
            if item then return item end
        end
    end

    local sq = player:getSquare()
    if sq and sq.getObjects then
        local objs = sq:getObjects()
        for i = 0, objs:size() - 1 do
            local obj = objs:get(i)
            if obj and obj.getItem then
                local oi = obj:getItem()
                if oi and oi.getID and oi:getID() == itemId then return oi end
            end
            if obj and obj.getContainer and obj:getContainer() then
                item = GridProtocol.findItemInTree(obj:getContainer(), itemId)
                if item then return item end
            end
        end
    end
    return nil
end

local function pageOf(item, container)
    if GridSortState.isPlayerRootContainer(container) then return 1 end
    local md = item and item.getModData and item:getModData() or nil
    local page = md and tonumber(md.gridPage) or 1
    if page < 1 then page = 1 end
    return page
end

local function buildPageOccupancy(container, page, ignoreSet)
    local w, h = GridContainer.getGridSize(container)
    local grid = GridCore.new(w, h)
    for _, item in ipairs(GridSortState.collectItems(container)) do
        local id = item:getID()
        if not (ignoreSet and ignoreSet[id]) and pageOf(item, container) == page then
            local md = item:getModData()
            local x, y = tonumber(md.gridX), tonumber(md.gridY)
            if x and y then
                local fw, fh = ItemFootprint.getSize(item)
                local rotated = md.gridRot and true or false
                local ew, eh = rotated and fh or fw, rotated and fw or fh
                local compatKey, stackInfo = GridContainer.getStackInfo(item)
                if not grid:insertItem(id, x, y, ew, eh, rotated,
                    item, compatKey, stackInfo) then
                    return nil
                end
            end
        end
    end
    return grid
end

local function validPage(page)
    page = tonumber(page) or 1
    return page >= 1 and page <= GridSortState.MAX_PAGES
end

local function sendSnapshot(player, container, command, reason)
    sendServerCommand(player, GridSortState.MODULE, command, {
        reason = reason,
        authoritativeHash = GridSortState.authorityHash(container),
        moves = GridSortState.snapshot(container),
    })
end

local function broadcastItem(item, container, clear)
    local md = item:getModData()
    sendServerCommand(GridSortState.MODULE, GridSortState.COMMANDS.SYNC_ITEM, {
        itemId = item:getID(),
        clear = clear and true or nil,
        x = clear and nil or tonumber(md.gridX),
        y = clear and nil or tonumber(md.gridY),
        rotated = clear and false or (md.gridRot and true or false),
        page = clear and 1 or pageOf(item, container),
        gridContainer = clear and nil or md.gridContainer,
        manual = clear and false or (md.gridManual and true or false),
    })
end

local function applyPosition(item, move, gridContainer, manual, container)
    local md = item:getModData()
    md.gridX = tonumber(move.x)
    md.gridY = tonumber(move.y)
    md.gridRot = move.rotated and true or false
    local page = GridSortState.isPlayerRootContainer(container) and 1 or (tonumber(move.page) or 1)
    md.gridPage = page > 1 and page or nil
    if gridContainer ~= nil then md.gridContainer = gridContainer end
    if manual ~= nil then md.gridManual = manual and true or nil end
end

local function processPageAssign(player, args)
    if not args or not args.ref or not args.moves or #args.moves == 0 then return "invalid" end
    local target = GridProtocol.resolveContainerRef(args.ref, player)
    if not target or GridSortState.isPlayerRootContainer(target) then return "invalid" end

    local movedSet = {}
    local resolved = {}
    for _, move in ipairs(args.moves) do
        local page = tonumber(move.page) or 1
        if move.itemId == nil or move.x == nil or move.y == nil
            or not validPage(page) or page <= 1 then
            return "invalid"
        end
        local item = findItem(player, args.ref, move.itemId, nil)
        if not item then return "notfound" end
        if item.isEquipped and item:isEquipped() then return "invalid" end

        -- A delayed automatic page-routing packet must never overwrite a sort
        -- or manual placement that the server has already committed. This can
        -- happen when two clients observe the same overflow and one sorts while
        -- another still has an older PAGE_ASSIGN in flight.
        local md = item.getModData and item:getModData() or nil
        if not (md and md.gridManual) then
            movedSet[item:getID()] = true
            table.insert(resolved, {
                item = item,
                move = {
                    itemId = move.itemId,
                    x = move.x, y = move.y,
                    page = page,
                    rotated = move.rotated,
                },
            })
        end
    end

    if #resolved == 0 then return "ok" end

    local pages = {}
    for _, entry in ipairs(resolved) do
        local page = entry.move.page
        if not pages[page] then
            pages[page] = buildPageOccupancy(target, page, movedSet)
            if not pages[page] then return "invalid" end
        end
        local item, move = entry.item, entry.move
        local fw, fh = ItemFootprint.getSize(item)
        local rotated = move.rotated and true or false
        local ew, eh = rotated and fh or fw, rotated and fw or fh
        local compatKey, stackInfo = GridContainer.getStackInfo(item)
        if not pages[page]:insertItem(item:getID(), tonumber(move.x), tonumber(move.y), ew, eh,
            rotated, item, compatKey, stackInfo, movedSet) then
            return "invalid"
        end
    end

    -- Auto page assignment is authoritative only for page routing. Keep
    -- gridManual=false so authorityHash deliberately remains unchanged and a
    -- presentation rescue cannot manufacture a false CAS conflict.
    for _, entry in ipairs(resolved) do
        applyPosition(entry.item, entry.move, args.gridContainer, false, target)
        broadcastItem(entry.item, target, false)
    end
    return "ok"
end

local function processPageMove(player, args)
    if not args or not args.ref or args.itemId == nil then return "invalid" end
    local target = GridProtocol.resolveContainerRef(args.ref, player)
    if not target then return "invalid" end
    local item = findItem(player, args.ref, args.itemId, args.sourceRef)
    if not item then return "notfound" end
    if item.isEquipped and item:isEquipped() then return "invalid" end

    if args.clear then
        local md = item:getModData()
        md.gridX, md.gridY = nil, nil
        md.gridRot = false
        md.gridPage = nil
        md.gridContainer = nil
        md.gridManual = nil
        broadcastItem(item, target, true)
        return "ok"
    end

    if args.x == nil or args.y == nil or not validPage(args.page) then return "invalid" end
    local page = GridSortState.isPlayerRootContainer(target) and 1 or (tonumber(args.page) or 1)
    local ignoreSet = { [item:getID()] = true }
    local grid = buildPageOccupancy(target, page, ignoreSet)
    if not grid then return "invalid" end

    local fw, fh = ItemFootprint.getSize(item)
    local rotated = args.rotated and true or false
    local ew, eh = rotated and fh or fw, rotated and fw or fh
    local compatKey, stackInfo = GridContainer.getStackInfo(item)
    if not grid:canPlaceItem(item:getID(), tonumber(args.x), tonumber(args.y), ew, eh,
        nil, compatKey, rotated, stackInfo, ignoreSet) then
        return "invalid"
    end

    local move = {
        x = args.x, y = args.y, rotated = args.rotated,
        page = page,
    }
    applyPosition(item, move, args.gridContainer, args.manual, target)
    broadcastItem(item, target, false)
    return "ok"
end

local function processPageReorder(player, args)
    if not args or not args.ref or not args.moves or #args.moves == 0 then return "invalid" end
    local target = GridProtocol.resolveContainerRef(args.ref, player)
    if not target then return "invalid" end

    local moves, movedSet = {}, {}
    local page = nil
    for _, move in ipairs(args.moves) do
        if move.itemId == nil or move.x == nil or move.y == nil or not validPage(move.page) then
            return "invalid"
        end
        local item = findItem(player, args.ref, move.itemId, args.sourceRef)
        if not item then return "notfound" end
        local movePage = GridSortState.isPlayerRootContainer(target) and 1 or (tonumber(move.page) or 1)
        if page == nil then page = movePage end
        if page ~= movePage then return "invalid" end
        movedSet[item:getID()] = true
        table.insert(moves, { item = item, move = {
            itemId = move.itemId,
            x = move.x, y = move.y,
            page = movePage,
            rotated = move.rotated,
        } })
    end

    local grid = buildPageOccupancy(target, page or 1, movedSet)
    if not grid then return "invalid" end
    for _, entry in ipairs(moves) do
        local item, move = entry.item, entry.move
        local fw, fh = ItemFootprint.getSize(item)
        local rotated = move.rotated and true or false
        local ew, eh = rotated and fh or fw, rotated and fw or fh
        local compatKey, stackInfo = GridContainer.getStackInfo(item)
        if not grid:insertItem(item:getID(), tonumber(move.x), tonumber(move.y), ew, eh,
            rotated, item, compatKey, stackInfo, movedSet) then
            return "invalid"
        end
    end

    for _, entry in ipairs(moves) do
        applyPosition(entry.item, entry.move, args.gridContainer, args.manual, target)
    end
    sendServerCommand(GridSortState.MODULE, GridSortState.COMMANDS.SYNC_LAYOUT, {
        authoritativeHash = GridSortState.authorityHash(target),
        moves = GridSortState.snapshot(target),
    })
    return "ok"
end

local function sameItemSet(container, moves)
    local items = GridSortState.collectItems(container)
    local expected = {}
    for _, item in ipairs(items) do expected[item:getID()] = true end
    local count = 0
    for _, move in ipairs(moves or {}) do
        if move.itemId == nil or expected[move.itemId] ~= true then return false end
        expected[move.itemId] = nil
        count = count + 1
    end
    if count ~= #items then return false end
    for _ in pairs(expected) do return false end
    return true
end

local function processSort(player, args)
    if not args or not args.ref or not args.moves or #args.moves == 0 then return "invalid" end
    local target = GridProtocol.resolveContainerRef(args.ref, player)
    if not target then return "invalid" end

    if target.getType and target:getType() == "floor" then return "invalid" end
    local parent = target.getParent and target:getParent()
    if parent and instanceof and instanceof(parent, "IsoDeadBody") then return "invalid" end

    local currentHash = GridSortState.authorityHash(target)
    if tostring(args.expectedHash or "") ~= tostring(currentHash) then
        sendSnapshot(player, target, GridSortState.COMMANDS.REJECT_LAYOUT, "stale")
        return "stale"
    end
    if not sameItemSet(target, args.moves) then
        sendSnapshot(player, target, GridSortState.COMMANDS.REJECT_LAYOUT, "membership")
        return "invalid"
    end

    local w, h = GridContainer.getGridSize(target)
    local pages = {}
    local resolved = {}
    local rootPlayer = GridSortState.isPlayerRootContainer(target)
    for _, move in ipairs(args.moves) do
        if move.x == nil or move.y == nil or not validPage(move.page) then
            sendSnapshot(player, target, GridSortState.COMMANDS.REJECT_LAYOUT, "bounds")
            return "invalid"
        end
        local item = findItem(player, args.ref, move.itemId, nil)
        if not item then
            sendSnapshot(player, target, GridSortState.COMMANDS.REJECT_LAYOUT, "missing-item")
            return "invalid"
        end
        local page = rootPlayer and 1 or (tonumber(move.page) or 1)
        pages[page] = pages[page] or GridCore.new(w, h)
        local fw, fh = ItemFootprint.getSize(item)
        local rotated = move.rotated and true or false
        local ew, eh = rotated and fh or fw, rotated and fw or fh
        local compatKey, stackInfo = GridContainer.getStackInfo(item)
        if not pages[page]:insertItem(item:getID(), tonumber(move.x), tonumber(move.y), ew, eh,
            rotated, item, compatKey, stackInfo) then
            sendSnapshot(player, target, GridSortState.COMMANDS.REJECT_LAYOUT, "collision")
            return "invalid"
        end
        table.insert(resolved, { item = item, move = {
            itemId = move.itemId,
            x = move.x, y = move.y,
            page = page,
            rotated = move.rotated,
        } })
    end

    -- Compare-and-swap: automatic GridInventory coordinates are deliberately
    -- excluded from authorityHash, while membership and manual positions are
    -- included. The first concurrent writer makes every sorted item manual,
    -- changing the hash; a later request based on the old state is rejected.
    if GridSortState.authorityHash(target) ~= currentHash then
        sendSnapshot(player, target, GridSortState.COMMANDS.REJECT_LAYOUT, "stale-after-validate")
        return "stale"
    end

    for _, entry in ipairs(resolved) do
        applyPosition(entry.item, entry.move, args.gridContainer, true, target)
    end

    sendServerCommand(GridSortState.MODULE, GridSortState.COMMANDS.SYNC_LAYOUT, {
        authoritativeHash = GridSortState.authorityHash(target),
        moves = GridSortState.snapshot(target),
    })
    return "ok"
end

local function queuePending(player, command, args)
    local list = pending[player] or {}
    table.insert(list, {
        command = command, args = args,
        retries = 0, lastTry = getTimestampMs(),
    })
    pending[player] = list
end

local function dispatch(player, command, args)
    if command == GridSortState.COMMANDS.SORT_REQUEST then return processSort(player, args) end
    if command == GridSortState.COMMANDS.PAGE_ASSIGN then return processPageAssign(player, args) end
    if command == GridSortState.COMMANDS.PAGE_MOVE then return processPageMove(player, args) end
    if command == GridSortState.COMMANDS.PAGE_CLEAR then
        args = args or {}; args.clear = true
        return processPageMove(player, args)
    end
    if command == GridSortState.COMMANDS.PAGE_REORDER then return processPageReorder(player, args) end
    return "ignore"
end

local function OnClientCommand(module, command, player, args)
    if module ~= GridSortState.MODULE or not player then return end
    local status = dispatch(player, command, args)
    if status == "notfound" then
        queuePending(player, command, args)
    elseif status == "invalid" then
        local target = args and args.ref and GridProtocol.resolveContainerRef(args.ref, player) or nil
        if target then sendSnapshot(player, target, GridSortState.COMMANDS.REJECT_LAYOUT, "invalid") end
    end
end
Events.OnClientCommand.Add(OnClientCommand)

Events.OnTick.Add(function()
    local now = getTimestampMs()
    for player, list in pairs(pending) do
        for i = #list, 1, -1 do
            local p = list[i]
            if now - p.lastTry >= PENDING_DELAY_MS then
                p.lastTry = now
                p.retries = p.retries + 1
                local status = dispatch(player, p.command, p.args)
                if status ~= "notfound" or p.retries >= PENDING_RETRIES then
                    table.remove(list, i)
                end
            end
        end
        if #list == 0 then pending[player] = nil end
    end
end)

print("[LCC GridSort] server CAS/page authority installed")
return GridSortServer
