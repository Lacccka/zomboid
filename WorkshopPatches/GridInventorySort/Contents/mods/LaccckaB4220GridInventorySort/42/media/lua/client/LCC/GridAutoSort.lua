local GridCore = require("DataModel/GridCore")
local GridContainer = require("DataModel/GridContainer")
local ItemFootprint = require("Algorithm/ItemFootprint")
local GridClientNetwork = require("Network/GridClientNetwork")
local GridSortState = require("LCC/GridSortState")
local GridSortNetwork = require("LCC/GridSortNetwork")

local GridAutoSort = {}

local function itemId(item)
    return item and item.getID and item:getID() or nil
end

local function makeDescriptor(item)
    if not item then return nil end
    local id = itemId(item)
    if id == nil then return nil end
    local w, h = ItemFootprint.getSize(item)
    w, h = tonumber(w), tonumber(h)
    if not w or not h or w < 1 or h < 1 then return nil end
    local compatKey, stackInfo = GridContainer.getStackInfo(item)
    local md = item.getModData and item:getModData() or nil
    return {
        id = id,
        itemObj = item,
        originalW = w,
        originalH = h,
        compatKey = compatKey,
        stackInfo = stackInfo,
        area = w * h,
        longSide = math.max(w, h),
        shortSide = math.min(w, h),
        currentRotated = md and md.gridRot and true or false,
        currentPage = md and tonumber(md.gridPage) or 1,
    }
end

local function collectDescriptors(container, playerNum)
    local model = GridContainer.getOrCreate(container, playerNum)
    local out, seen = {}, {}
    for _, grid in ipairs(model.grids or {}) do
        for id, data in pairs(grid.items or {}) do
            if not seen[id] and data and data.itemObj then
                local d = makeDescriptor(data.itemObj)
                if d then seen[id] = true; table.insert(out, d) end
            end
        end
    end
    for _, item in ipairs(model.unpositioned or {}) do
        local id = itemId(item)
        if id ~= nil and not seen[id] then
            local d = makeDescriptor(item)
            if d then seen[id] = true; table.insert(out, d) end
        end
    end
    return out, model
end

local function compareId(a, b)
    local ai, bi = tonumber(a.id), tonumber(b.id)
    if ai and bi then return ai < bi end
    return tostring(a.id) < tostring(b.id)
end

local SORTERS = {
    function(a, b)
        if a.area ~= b.area then return a.area > b.area end
        if a.longSide ~= b.longSide then return a.longSide > b.longSide end
        if a.shortSide ~= b.shortSide then return a.shortSide > b.shortSide end
        return compareId(a, b)
    end,
    function(a, b)
        if a.longSide ~= b.longSide then return a.longSide > b.longSide end
        if a.area ~= b.area then return a.area > b.area end
        if a.shortSide ~= b.shortSide then return a.shortSide > b.shortSide end
        return compareId(a, b)
    end,
    function(a, b)
        if a.originalW ~= b.originalW then return a.originalW > b.originalW end
        if a.area ~= b.area then return a.area > b.area end
        if a.originalH ~= b.originalH then return a.originalH > b.originalH end
        return compareId(a, b)
    end,
    compareId,
}

local function cloneGrid(width, height, targets)
    local grid = GridCore.new(width, height)
    for _, t in ipairs(targets) do
        local d = t.d
        if not grid:insertItem(d.id, t.tx, t.ty, t.ew, t.eh, t.rotated,
            d.itemObj, d.compatKey, d.stackInfo) then
            return nil
        end
    end
    return grid
end

local function layoutMetrics(grid, targets)
    local bottom, right, rotations = 0, 0, 0
    for _, t in ipairs(targets) do
        bottom = math.max(bottom, t.ty + t.eh - 1)
        right = math.max(right, t.tx + t.ew - 1)
        if t.rotated then rotations = rotations + 1 end
    end
    local holes = 0
    if bottom > 0 and right > 0 then
        for y = 1, bottom do
            for x = 1, right do
                if grid.cells[x][y] == nil then holes = holes + 1 end
            end
        end
    end
    return bottom, holes, right, rotations
end

local function stateScore(state)
    return state.bottom * 1000000 + state.holes * 10000 + state.right * 100 + state.rotations
end

local function enumeratePlacements(grid, d)
    local candidates = {}
    local function scan(rotated)
        local ew = rotated and d.originalH or d.originalW
        local eh = rotated and d.originalW or d.originalH
        if ew > grid.width or eh > grid.height then return end
        for y = 1, grid.height - eh + 1 do
            for x = 1, grid.width - ew + 1 do
                if grid:canPlaceItem(d.id, x, y, ew, eh, nil,
                    d.compatKey, rotated, d.stackInfo) then
                    local bottom = y + eh - 1
                    local right = x + ew - 1
                    local keepRot = rotated == d.currentRotated and 0 or 1
                    local score = bottom * 100000 + y * 1000 + right * 10 + x + keepRot
                    table.insert(candidates, {
                        tx = x, ty = y, ew = ew, eh = eh,
                        rotated = rotated, localScore = score,
                    })
                end
            end
        end
    end
    scan(false)
    if d.originalW ~= d.originalH then scan(true) end
    table.sort(candidates, function(a, b)
        if a.localScore ~= b.localScore then return a.localScore < b.localScore end
        if a.ty ~= b.ty then return a.ty < b.ty end
        if a.tx ~= b.tx then return a.tx < b.tx end
        return (a.rotated and 1 or 0) < (b.rotated and 1 or 0)
    end)
    return candidates
end

local function packBeam(descriptors, width, height, sorter)
    local ordered = {}
    for i = 1, #descriptors do ordered[i] = descriptors[i] end
    table.sort(ordered, sorter)

    local beamWidth = #ordered <= 24 and 28 or (#ordered <= 40 and 18 or 10)
    local placementsPerState = #ordered <= 24 and 14 or 8
    local states = {{
        grid = GridCore.new(width, height), targets = {},
        bottom = 0, holes = 0, right = 0, rotations = 0,
    }}

    for _, d in ipairs(ordered) do
        local nextStates = {}
        for _, state in ipairs(states) do
            local candidates = enumeratePlacements(state.grid, d)
            local limit = math.min(#candidates, placementsPerState)
            for i = 1, limit do
                local p = candidates[i]
                local targets = {}
                for j = 1, #state.targets do targets[j] = state.targets[j] end
                table.insert(targets, {
                    d = d, tx = p.tx, ty = p.ty, ew = p.ew, eh = p.eh,
                    rotated = p.rotated,
                })
                local grid = cloneGrid(width, height, targets)
                if grid then
                    local bottom, holes, right, rotations = layoutMetrics(grid, targets)
                    table.insert(nextStates, {
                        grid = grid, targets = targets,
                        bottom = bottom, holes = holes, right = right, rotations = rotations,
                    })
                end
            end
        end
        if #nextStates == 0 then return nil end
        table.sort(nextStates, function(a, b)
            local sa, sb = stateScore(a), stateScore(b)
            if sa ~= sb then return sa < sb end
            return #a.targets < #b.targets
        end)
        states = {}
        local keep = math.min(#nextStates, beamWidth)
        for i = 1, keep do states[i] = nextStates[i] end
    end

    return states[1]
end

local function betterSingle(a, b)
    if not a then return b end
    if not b then return a end
    return stateScore(a) <= stateScore(b) and a or b
end

local function bestSinglePage(descriptors, width, height)
    local best = nil
    for _, sorter in ipairs(SORTERS) do
        best = betterSingle(best, packBeam(descriptors, width, height, sorter))
    end
    return best
end

local function bestPlacementAcrossPages(pages, d, width, height)
    local best = nil
    local function considerPage(pageIndex, grid)
        local candidates = enumeratePlacements(grid, d)
        local p = candidates[1]
        if not p then return end
        local score = pageIndex * 100000000 + p.localScore
        if not best or score < best.score then
            best = { page = pageIndex, p = p, score = score }
        end
    end
    for page, grid in ipairs(pages) do considerPage(page, grid) end
    if not best and #pages < GridSortState.MAX_PAGES then
        local newGrid = GridCore.new(width, height)
        local candidates = enumeratePlacements(newGrid, d)
        if candidates[1] then
            table.insert(pages, newGrid)
            best = {
                page = #pages, p = candidates[1],
                score = #pages * 100000000 + candidates[1].localScore,
            }
        end
    end
    return best
end

local function packMultiGreedy(descriptors, width, height, sorter)
    local ordered = {}
    for i = 1, #descriptors do ordered[i] = descriptors[i] end
    table.sort(ordered, sorter)
    local pages = { GridCore.new(width, height) }
    local targets = {}
    for _, d in ipairs(ordered) do
        local choice = bestPlacementAcrossPages(pages, d, width, height)
        if not choice then return nil end
        local p = choice.p
        if not pages[choice.page]:insertItem(d.id, p.tx, p.ty, p.ew, p.eh, p.rotated,
            d.itemObj, d.compatKey, d.stackInfo) then
            return nil
        end
        table.insert(targets, {
            d = d, tx = p.tx, ty = p.ty, ew = p.ew, eh = p.eh,
            rotated = p.rotated, page = choice.page,
        })
    end
    local holes, rotations = 0, 0
    for _, grid in ipairs(pages) do
        local bottom = 0
        for _, data in pairs(grid.items or {}) do
            if not data.stackMemberOf then bottom = math.max(bottom, data.y + data.h - 1) end
        end
        for y = 1, bottom do
            for x = 1, width do
                if grid.cells[x][y] == nil then holes = holes + 1 end
            end
        end
    end
    for _, t in ipairs(targets) do if t.rotated then rotations = rotations + 1 end end
    return { pages = #pages, targets = targets, holes = holes, rotations = rotations }
end

local function betterMulti(a, b)
    if not a then return b end
    if not b then return a end
    if a.pages ~= b.pages then return a.pages < b.pages and a or b end
    if a.holes ~= b.holes then return a.holes < b.holes and a or b end
    if a.rotations ~= b.rotations then return a.rotations < b.rotations and a or b end
    return a
end

local function computePacked(descriptors, width, height)
    local single = bestSinglePage(descriptors, width, height)
    if single then
        local targets = {}
        for _, t in ipairs(single.targets) do
            table.insert(targets, {
                d = t.d, tx = t.tx, ty = t.ty, ew = t.ew, eh = t.eh,
                rotated = t.rotated, page = 1,
            })
        end
        return {
            pages = 1, targets = targets,
            holes = single.holes, rotations = single.rotations,
        }
    end

    local best = nil
    for _, sorter in ipairs(SORTERS) do
        best = betterMulti(best, packMultiGreedy(descriptors, width, height, sorter))
    end
    return best
end

local function hasPendingWork(container, model)
    if GridInventory_GlobalDrag and GridInventory_GlobalDrag.itemsData
        and #GridInventory_GlobalDrag.itemsData > 0 then return true end
    if ISMouseDrag and ISMouseDrag.dragging then return true end
    for _, grid in ipairs((model and model.grids) or {}) do
        for id in pairs(grid.items or {}) do
            if GridInventory_InTransit and GridInventory_InTransit[id] then return true end
        end
        for _ in pairs(grid.ghostItems or {}) do return true end
    end
    if container and container.getItems and GridInventory_InTransit then
        local items = container:getItems()
        for i = 0, items:size() - 1 do
            local item = items:get(i)
            if item and GridInventory_InTransit[item:getID()] then return true end
        end
    end
    return false
end

local function isNestedBagContainer(container)
    if not container or not container.getContainingItem then return false end
    local containingItem = container:getContainingItem()
    if not containingItem or not containingItem.getContainer then return false end
    local parentContainer = containingItem:getContainer()
    if not parentContainer or not parentContainer.getContainingItem then return false end
    return parentContainer:getContainingItem() ~= nil
end

function GridAutoSort.canSort(gridUi)
    if not gridUi or gridUi.isOverflow or not gridUi.inventoryContainer then
        return false, "unavailable"
    end

    local container = gridUi.inventoryContainer
    if container.getType and container:getType() == "floor" then return false, "floor" end
    local parent = container.getParent and container:getParent()
    if parent and instanceof and instanceof(parent, "IsoDeadBody") then return false, "corpse" end
    if isClient and isClient() and isNestedBagContainer(container) then return false, "nested" end
    if GridSortNetwork.isPending(container) then return false, "busy" end
    if gridUi.needsSearch and gridUi:needsSearch() then return false, "search" end

    local model = GridContainer.getOrCreate(container, gridUi.playerNum or 0)
    if hasPendingWork(container, model) then return false, "busy" end

    local okBag, BagDrop = pcall(require, "System/GridInventory_BagDrop")
    if okBag and BagDrop and BagDrop.isNestedLocked then
        local player = getSpecificPlayer(gridUi.playerNum or 0)
        if BagDrop.isNestedLocked(container, player) then return false, "locked" end
    end
    return true
end

function GridAutoSort.computeTargets(gridUi)
    local can, reason = GridAutoSort.canSort(gridUi)
    if not can then return nil, reason end

    local container = gridUi.inventoryContainer
    local descriptors, model = collectDescriptors(container, gridUi.playerNum or 0)
    if #descriptors < 2 then return nil, "nothing" end
    if hasPendingWork(container, model) then return nil, "busy" end

    local width, height = GridContainer.getGridSize(container)
    local packed = computePacked(descriptors, width, height)
    if not packed then return nil, "no-space" end

    local targets = {}
    for _, t in ipairs(packed.targets) do
        table.insert(targets, {
            item = {
                id = t.d.id,
                itemObj = t.d.itemObj,
                originalW = t.d.originalW,
                originalH = t.d.originalH,
                rotated = t.rotated,
                compatKey = t.d.compatKey,
                stackInfo = t.d.stackInfo,
            },
            tx = t.tx, ty = t.ty, ew = t.ew, eh = t.eh,
            page = t.page or 1,
        })
    end
    return targets, nil
end

local function targetDiffers(t, signature)
    local item = t.item and t.item.itemObj
    local md = item and item.getModData and item:getModData() or nil
    if not md then return true end
    local page = tonumber(md.gridPage) or 1
    return tonumber(md.gridX) ~= t.tx
        or tonumber(md.gridY) ~= t.ty
        or (md.gridRot and true or false) ~= (t.item.rotated and true or false)
        or page ~= (t.page or 1)
        or md.gridContainer ~= signature
        or md.gridManual ~= true
end

function GridAutoSort.sort(gridUi)
    local targets, reason = GridAutoSort.computeTargets(gridUi)
    if not targets then return false, reason end

    local container = gridUi.inventoryContainer
    local signature = GridContainer.containerSignature(container)
    local changed = false
    for _, t in ipairs(targets) do
        if targetDiffers(t, signature) then changed = true break end
    end
    if not changed then return true, "already-sorted" end

    if isClient and isClient() then
        -- Optimistic concurrency without optimistic local mutation: solve from
        -- the current snapshot, send its hash, and let the server commit the
        -- whole layout atomically. The first concurrent writer wins; later
        -- stale requests receive the authoritative snapshot and are rolled back.
        local expectedHash = GridSortState.layoutHash(container)
        if not GridSortNetwork.sendSort(container, targets, expectedHash, signature) then
            return false, "unavailable"
        end
        return true, "pending"
    end

    -- SP: no server round-trip; commit locally in one pass.
    for _, t in ipairs(targets) do
        local item = t.item.itemObj
        local md = item and item.getModData and item:getModData() or nil
        if md then
            md.gridX = t.tx
            md.gridY = t.ty
            md.gridRot = t.item.rotated and true or false
            md.gridPage = (t.page or 1) > 1 and (t.page or 1) or nil
            md.gridContainer = signature
            md.gridManual = true
        end
    end
    GridClientNetwork.markGridChanged(container, gridUi.playerNum or 0)
    gridUi.selectedItems = {}
    return true, "sorted"
end

return GridAutoSort
