local GridCore = require("DataModel/GridCore")
local GridContainer = require("DataModel/GridContainer")
local ItemFootprint = require("Algorithm/ItemFootprint")
local GridClientNetwork = require("Network/GridClientNetwork")

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
    }
end

local function collectDescriptors(container, playerNum)
    local model = GridContainer.getOrCreate(container, playerNum)
    model:refresh()

    local out = {}
    local seen = {}

    -- Use the GridContainer model instead of container:getItems() directly.
    -- This preserves GridInventory's own filtering (equipped/attached/hidden)
    -- and includes items that currently live in overflow/unpositioned.
    for _, grid in ipairs(model.grids or {}) do
        for id, data in pairs(grid.items or {}) do
            if not seen[id] and data and data.itemObj then
                local d = makeDescriptor(data.itemObj)
                if d then
                    seen[id] = true
                    table.insert(out, d)
                end
            end
        end
    end

    for _, item in ipairs(model.unpositioned or {}) do
        local id = itemId(item)
        if id ~= nil and not seen[id] then
            local d = makeDescriptor(item)
            if d then
                seen[id] = true
                table.insert(out, d)
            end
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
    -- Large rectangles first is a stable first-fit-decreasing baseline.
    function(a, b)
        if a.area ~= b.area then return a.area > b.area end
        if a.longSide ~= b.longSide then return a.longSide > b.longSide end
        if a.shortSide ~= b.shortSide then return a.shortSide > b.shortSide end
        return compareId(a, b)
    end,
    -- Long awkward items first can rescue layouts where pure area ordering
    -- fragments the narrow 6-column grids used by GridInventory.
    function(a, b)
        if a.longSide ~= b.longSide then return a.longSide > b.longSide end
        if a.area ~= b.area then return a.area > b.area end
        if a.shortSide ~= b.shortSide then return a.shortSide > b.shortSide end
        return compareId(a, b)
    end,
    -- Wider natural footprints first is useful for shelves/toolboxes.
    function(a, b)
        if a.originalW ~= b.originalW then return a.originalW > b.originalW end
        if a.area ~= b.area then return a.area > b.area end
        if a.originalH ~= b.originalH then return a.originalH > b.originalH end
        return compareId(a, b)
    end,
    -- Fully stable fallback.
    compareId,
}

local function betterCandidate(a, b, currentBottom, preferWide, currentRotated)
    if not a then return b end
    if not b then return a end

    local aBottom = math.max(currentBottom, a.y + a.h - 1)
    local bBottom = math.max(currentBottom, b.y + b.h - 1)
    if aBottom ~= bBottom then return aBottom < bBottom and a or b end
    if a.y ~= b.y then return a.y < b.y and a or b end
    if a.x ~= b.x then return a.x < b.x and a or b end

    local aWide = a.w >= a.h
    local bWide = b.w >= b.h
    if aWide ~= bWide then
        if preferWide then return aWide and a or b end
        return aWide and b or a
    end

    local aKeepsRotation = a.rotated == currentRotated
    local bKeepsRotation = b.rotated == currentRotated
    if aKeepsRotation ~= bKeepsRotation then
        return aKeepsRotation and a or b
    end

    return a.rotated and b or a
end

local function choosePlacement(grid, d, currentBottom, preferWide)
    -- Stack first: findFreeSpace() is intentionally top-left-first and would
    -- choose an earlier empty cell before a compatible pile later in the row.
    -- Auto-sort should consolidate compatible virtual stacks whenever the
    -- configured maxStack allows it, so probe real piles explicitly first.
    if d.compatKey and grid.findCompatibleStack then
        local stackBest = nil

        local sx, sy, srot = grid:findCompatibleStack(
            d.id, d.originalW, d.originalH, d.compatKey, d.stackInfo
        )
        if sx and sy then
            stackBest = {
                x = sx, y = sy,
                w = d.originalW, h = d.originalH,
                rotated = srot and true or false,
            }
        end

        if d.originalW ~= d.originalH then
            local rx, ry, rrot = grid:findCompatibleStack(
                d.id, d.originalH, d.originalW, d.compatKey, d.stackInfo
            )
            if rx and ry then
                local rotatedStack = {
                    x = rx, y = ry,
                    w = d.originalH, h = d.originalW,
                    rotated = rrot and true or false,
                }
                stackBest = betterCandidate(
                    stackBest, rotatedStack, currentBottom, preferWide, d.currentRotated
                )
            end
        end

        if stackBest then return stackBest end
    end

    local best = nil

    local x, y = grid:findFreeSpace(
        d.id, d.originalW, d.originalH,
        d.compatKey, d.stackInfo, false
    )
    if x and y then
        best = {
            x = x, y = y,
            w = d.originalW, h = d.originalH,
            rotated = false,
        }
    end

    if d.originalW ~= d.originalH then
        local rx, ry = grid:findFreeSpace(
            d.id, d.originalH, d.originalW,
            d.compatKey, d.stackInfo, true
        )
        if rx and ry then
            local rotated = {
                x = rx, y = ry,
                w = d.originalH, h = d.originalW,
                rotated = true,
            }
            best = betterCandidate(best, rotated, currentBottom, preferWide, d.currentRotated)
        end
    end

    return best
end

local function pack(descriptors, width, height, sorter, preferWide)
    local ordered = {}
    for i = 1, #descriptors do ordered[i] = descriptors[i] end
    table.sort(ordered, sorter)

    local grid = GridCore.new(width, height)
    local targets = {}
    local bottom = 0

    for _, d in ipairs(ordered) do
        local p = choosePlacement(grid, d, bottom, preferWide)
        if not p then return nil end

        if not grid:insertItem(
            d.id, p.x, p.y, p.w, p.h, p.rotated,
            d.itemObj, d.compatKey, d.stackInfo
        ) then
            return nil
        end

        bottom = math.max(bottom, p.y + p.h - 1)
        table.insert(targets, {
            item = {
                id = d.id,
                itemObj = d.itemObj,
                originalW = d.originalW,
                originalH = d.originalH,
                rotated = p.rotated,
                compatKey = d.compatKey,
                stackInfo = d.stackInfo,
            },
            tx = p.x,
            ty = p.y,
            ew = p.w,
            eh = p.h,
        })
    end

    local holes = 0
    for y = 1, bottom do
        for x = 1, width do
            if grid.cells[x][y] == nil then holes = holes + 1 end
        end
    end

    local rotations = 0
    for _, t in ipairs(targets) do
        if t.item.rotated then rotations = rotations + 1 end
    end

    return {
        targets = targets,
        bottom = bottom,
        holes = holes,
        rotations = rotations,
    }
end

local function betterLayout(a, b)
    if not a then return b end
    if not b then return a end
    if a.bottom ~= b.bottom then return a.bottom < b.bottom and a or b end
    if a.holes ~= b.holes then return a.holes < b.holes and a or b end
    if a.rotations ~= b.rotations then return a.rotations < b.rotations and a or b end
    return a
end

local function hasPendingWork(container, model)
    if GridInventory_GlobalDrag and GridInventory_GlobalDrag.itemsData
        and #GridInventory_GlobalDrag.itemsData > 0 then
        return true
    end
    if ISMouseDrag and ISMouseDrag.dragging then return true end

    for _, grid in ipairs((model and model.grids) or {}) do
        for id in pairs(grid.items or {}) do
            if GridInventory_InTransit and GridInventory_InTransit[id] then
                return true
            end
        end
        for _ in pairs(grid.ghostItems or {}) do
            return true
        end
    end

    if container and container.getItems and GridInventory_InTransit then
        local items = container:getItems()
        for i = 0, items:size() - 1 do
            local item = items:get(i)
            if item and GridInventory_InTransit[item:getID()] then
                return true
            end
        end
    end

    return false
end

function GridAutoSort.canSort(gridUi)
    if not gridUi or gridUi.isOverflow or not gridUi.inventoryContainer then
        return false, "unavailable"
    end

    local container = gridUi.inventoryContainer
    if container.getType and container:getType() == "floor" then
        return false, "floor"
    end

    local parent = container.getParent and container:getParent()
    if parent and instanceof and instanceof(parent, "IsoDeadBody") then
        -- GridContainer adds corpse worn items that are not regular children of
        -- the ItemContainer. REQUEST_REORDER cannot authoritatively resolve all
        -- of them yet, so keep v1 fail-closed instead of risking partial state.
        return false, "corpse"
    end

    if gridUi.needsSearch and gridUi:needsSearch() then
        return false, "search"
    end

    local model = GridContainer.getOrCreate(container, gridUi.playerNum or 0)
    if hasPendingWork(container, model) then
        return false, "busy"
    end

    local okBag, BagDrop = pcall(require, "System/GridInventory_BagDrop")
    if okBag and BagDrop and BagDrop.isNestedLocked then
        local player = getSpecificPlayer(gridUi.playerNum or 0)
        if BagDrop.isNestedLocked(container, player) then
            return false, "locked"
        end
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
    local best = nil
    for _, sorter in ipairs(SORTERS) do
        best = betterLayout(best, pack(descriptors, width, height, sorter, true))
        best = betterLayout(best, pack(descriptors, width, height, sorter, false))
    end

    if not best then return nil, "no-space" end
    return best.targets, nil
end

function GridAutoSort.sort(gridUi)
    local targets, reason = GridAutoSort.computeTargets(gridUi)
    if not targets then return false, reason end

    local container = gridUi.inventoryContainer
    local signature = GridContainer.containerSignature(container)
    local changed = false

    for _, t in ipairs(targets) do
        local item = t.item.itemObj
        local md = item and item.getModData and item:getModData() or nil
        if md then
            if tonumber(md.gridX) ~= t.tx
                or tonumber(md.gridY) ~= t.ty
                or (md.gridRot and true or false) ~= (t.item.rotated and true or false)
                or md.gridContainer ~= signature then
                changed = true
            end
            md.gridX = t.tx
            md.gridY = t.ty
            md.gridRot = t.item.rotated and true or false
            md.gridContainer = signature
            md.gridManual = true
        end
    end

    if not changed then return true, "already-sorted" end

    -- Reuse GridInventory's authoritative batch protocol. One REQUEST_REORDER
    -- carries the complete target layout, so swaps and full-container compaction
    -- remain all-or-nothing on the dedicated server.
    GridClientNetwork.sendReorder(container, targets, signature)
    GridClientNetwork.markGridChanged(container, gridUi.playerNum or 0)
    gridUi.selectedItems = {}

    return true, "sorted"
end

return GridAutoSort
