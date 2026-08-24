local GridCore = require("DataModel/GridCore")
local GridContainer = require("DataModel/GridContainer")
local ItemFootprint = require("Algorithm/ItemFootprint")
local GridClientNetwork = require("Network/GridClientNetwork")
local GridSortState = require("LCC/GridSortState")
local GridSortNetwork = require("LCC/GridSortNetwork")

local GridAutoSort = {}

-- Sorting is executed on the UI thread. The previous bounded beam search still
-- cloned/re-scanned many complete grids and caused visible freezes on large
-- inventories. v0.2.1 uses several cheap deterministic best-fit passes instead:
-- all cells + both rotations are still considered, but only one live GridCore is
-- maintained per pass. Extra passes stop once the small interactive-time budget
-- is consumed and a valid layout is already available.
local EXTRA_PASS_BUDGET_MS = 25
local SLOW_SOLVE_WARN_MS = 75

local function nowMs()
    return getTimestampMs and getTimestampMs() or (getTimeInMillis and getTimeInMillis() or 0)
end

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

local function compareIdReverse(a, b)
    return compareId(b, a)
end

local SORTERS = {
    -- Large rectangles first.
    function(a, b)
        if a.area ~= b.area then return a.area > b.area end
        if a.longSide ~= b.longSide then return a.longSide > b.longSide end
        if a.shortSide ~= b.shortSide then return a.shortSide > b.shortSide end
        return compareId(a, b)
    end,
    -- Awkward long items first.
    function(a, b)
        if a.longSide ~= b.longSide then return a.longSide > b.longSide end
        if a.area ~= b.area then return a.area > b.area end
        if a.shortSide ~= b.shortSide then return a.shortSide > b.shortSide end
        return compareId(a, b)
    end,
    -- Tall natural footprints first.
    function(a, b)
        if a.originalH ~= b.originalH then return a.originalH > b.originalH end
        if a.area ~= b.area then return a.area > b.area end
        if a.originalW ~= b.originalW then return a.originalW > b.originalW end
        return compareId(a, b)
    end,
    -- Wide natural footprints first.
    function(a, b)
        if a.originalW ~= b.originalW then return a.originalW > b.originalW end
        if a.area ~= b.area then return a.area > b.area end
        if a.originalH ~= b.originalH then return a.originalH > b.originalH end
        return compareId(a, b)
    end,
    -- High aspect-ratio items before square blocks.
    function(a, b)
        local aa = a.longSide / math.max(1, a.shortSide)
        local ba = b.longSide / math.max(1, b.shortSide)
        if aa ~= ba then return aa > ba end
        if a.area ~= b.area then return a.area > b.area end
        return compareId(a, b)
    end,
    -- Stable ID perturbations rescue layouts tied under geometric sort keys.
    compareId,
    compareIdReverse,
}

local function copyAndSort(descriptors, sorter)
    local ordered = {}
    for i = 1, #descriptors do ordered[i] = descriptors[i] end
    table.sort(ordered, sorter)
    return ordered
end

local function perimeterContact(grid, x, y, w, h)
    local contact = 0
    for cx = x, x + w - 1 do
        local up = y - 1
        local down = y + h
        if up < 1 or grid.cells[cx][up] ~= nil then contact = contact + 1 end
        if down > grid.height or grid.cells[cx][down] ~= nil then contact = contact + 1 end
    end
    for cy = y, y + h - 1 do
        local left = x - 1
        local right = x + w
        if left < 1 or grid.cells[left][cy] ~= nil then contact = contact + 1 end
        if right > grid.width or grid.cells[right][cy] ~= nil then contact = contact + 1 end
    end
    return contact
end

local function findBestPlacement(grid, d, bottom, occupiedCells, profile)
    local best = nil

    local function considerOrientation(rotated)
        local ew = rotated and d.originalH or d.originalW
        local eh = rotated and d.originalW or d.originalH
        if ew > grid.width or eh > grid.height then return end

        for y = 1, grid.height - eh + 1 do
            for x = 1, grid.width - ew + 1 do
                if grid:canPlaceItem(d.id, x, y, ew, eh, nil,
                    d.compatKey, rotated, d.stackInfo) then
                    local occupant = grid.cells[x][y]
                    local isStack = d.compatKey ~= nil and occupant ~= nil
                    local added = isStack and 0 or (ew * eh)
                    local newBottom = math.max(bottom, y + eh - 1)
                    local holes = (newBottom * grid.width) - (occupiedCells + added)
                    if holes < 0 then holes = 0 end
                    local contact = isStack and 99 or perimeterContact(grid, x, y, ew, eh)
                    local changedRotation = rotated == d.currentRotated and 0 or 1
                    local wide = ew >= eh and 1 or 0

                    -- Lexicographic intent encoded into integer ranges:
                    -- 1) keep used height minimal;
                    -- 2) avoid empty cells inside the used rows;
                    -- 3) pack against existing/boundary edges;
                    -- 4) deterministic top-left tie-break;
                    -- 5) avoid gratuitous rotation.
                    local score = newBottom * 100000000
                        + holes * 100000
                        - contact * (profile.contactWeight or 500)
                        + y * 1000 + x * 10
                        + changedRotation

                    if profile.preferWide then score = score - wide * 25 end
                    if profile.preferTall then score = score + wide * 25 end
                    if isStack then score = score - 5000000 end

                    if not best or score < best.score then
                        best = {
                            tx = x, ty = y, ew = ew, eh = eh,
                            rotated = rotated, score = score,
                            isStack = isStack, added = added,
                            newBottom = newBottom,
                        }
                    end
                end
            end
        end
    end

    considerOrientation(false)
    if d.originalW ~= d.originalH then considerOrientation(true) end
    return best
end

local PROFILES = {
    { contactWeight = 500 },
    { contactWeight = 1200, preferWide = true },
    { contactWeight = 1200, preferTall = true },
}

local function packGreedySingle(descriptors, width, height, sorter, profile)
    local ordered = copyAndSort(descriptors, sorter)
    local grid = GridCore.new(width, height)
    local targets = {}
    local bottom, occupiedCells, rotations = 0, 0, 0

    for _, d in ipairs(ordered) do
        local p = findBestPlacement(grid, d, bottom, occupiedCells, profile)
        if not p then return nil end
        if not grid:insertItem(d.id, p.tx, p.ty, p.ew, p.eh, p.rotated,
            d.itemObj, d.compatKey, d.stackInfo) then
            return nil
        end
        bottom = p.newBottom
        occupiedCells = occupiedCells + p.added
        if p.rotated then rotations = rotations + 1 end
        table.insert(targets, {
            d = d, tx = p.tx, ty = p.ty, ew = p.ew, eh = p.eh,
            rotated = p.rotated, page = 1,
        })
    end

    local holes = math.max(0, bottom * width - occupiedCells)
    return {
        pages = 1,
        targets = targets,
        usedRows = bottom,
        holes = holes,
        rotations = rotations,
    }
end

local function lowerBoundArea(descriptors)
    local area = 0
    local groups = {}
    for _, d in ipairs(descriptors) do
        local limit = d.stackInfo and tonumber(d.stackInfo.limit) or nil
        local units = d.stackInfo and tonumber(d.stackInfo.units) or 1
        if d.compatKey and limit and limit > 1 then
            local key = tostring(d.compatKey) .. ":" .. tostring(d.area)
            local g = groups[key]
            if not g then
                g = { units = 0, limit = limit, area = d.area }
                groups[key] = g
            end
            g.units = g.units + (units or 1)
        else
            area = area + d.area
        end
    end
    for _, g in pairs(groups) do
        area = area + math.ceil(g.units / math.max(1, g.limit)) * g.area
    end
    return area
end

local function betterLayout(a, b)
    if not a then return b end
    if not b then return a end
    if a.pages ~= b.pages then return a.pages < b.pages and a or b end
    if a.usedRows ~= b.usedRows then return a.usedRows < b.usedRows and a or b end
    if a.holes ~= b.holes then return a.holes < b.holes and a or b end
    if a.rotations ~= b.rotations then return a.rotations < b.rotations and a or b end
    return a
end

local function maxSorterPasses(itemCount)
    if itemCount > 70 then return 2 end
    if itemCount > 45 then return 3 end
    if itemCount > 28 then return 4 end
    return #SORTERS
end

local function bestSinglePage(descriptors, width, height, startedAt)
    if lowerBoundArea(descriptors) > width * height then return nil end

    local best = nil
    local passes = maxSorterPasses(#descriptors)
    for i = 1, passes do
        local sorter = SORTERS[i]
        for p = 1, #PROFILES do
            local candidate = packGreedySingle(descriptors, width, height, sorter, PROFILES[p])
            best = betterLayout(best, candidate)
            if best and nowMs() - startedAt >= EXTRA_PASS_BUDGET_MS then
                return best
            end
        end
    end
    return best
end

local function newPageState(width, height)
    return {
        grid = GridCore.new(width, height),
        bottom = 0,
        occupiedCells = 0,
    }
end

local function packGreedyMulti(descriptors, width, height, sorter, profile)
    local ordered = copyAndSort(descriptors, sorter)
    local pages = { newPageState(width, height) }
    local targets = {}
    local rotations = 0

    for _, d in ipairs(ordered) do
        local best = nil
        for pageIndex, state in ipairs(pages) do
            local p = findBestPlacement(state.grid, d, state.bottom, state.occupiedCells, profile)
            if p then
                -- Earlier pages dominate. Within a page use the same compact
                -- candidate score as the single-page solver.
                local score = pageIndex * 1000000000000 + p.score
                if not best or score < best.score then
                    best = { page = pageIndex, placement = p, score = score }
                end
            end
        end

        if not best and #pages < GridSortState.MAX_PAGES then
            local state = newPageState(width, height)
            local p = findBestPlacement(state.grid, d, 0, 0, profile)
            if p then
                table.insert(pages, state)
                best = { page = #pages, placement = p, score = #pages * 1000000000000 + p.score }
            end
        end
        if not best then return nil end

        local state = pages[best.page]
        local p = best.placement
        if not state.grid:insertItem(d.id, p.tx, p.ty, p.ew, p.eh, p.rotated,
            d.itemObj, d.compatKey, d.stackInfo) then
            return nil
        end
        state.bottom = p.newBottom
        state.occupiedCells = state.occupiedCells + p.added
        if p.rotated then rotations = rotations + 1 end
        table.insert(targets, {
            d = d, tx = p.tx, ty = p.ty, ew = p.ew, eh = p.eh,
            rotated = p.rotated, page = best.page,
        })
    end

    local usedRows, holes = 0, 0
    for _, state in ipairs(pages) do
        usedRows = usedRows + state.bottom
        holes = holes + math.max(0, state.bottom * width - state.occupiedCells)
    end
    return {
        pages = #pages,
        targets = targets,
        usedRows = usedRows,
        holes = holes,
        rotations = rotations,
    }
end

local function bestMultiPage(descriptors, width, height, startedAt)
    local best = nil
    local passes = maxSorterPasses(#descriptors)
    for i = 1, passes do
        local sorter = SORTERS[i]
        -- Multi-page is normally invoked for larger sets; one profile per
        -- ordering is enough and keeps the click comfortably interactive.
        local profile = PROFILES[((i - 1) % #PROFILES) + 1]
        best = betterLayout(best, packGreedyMulti(descriptors, width, height, sorter, profile))
        if best and nowMs() - startedAt >= EXTRA_PASS_BUDGET_MS then break end
    end
    return best
end

local function computePacked(descriptors, width, height, allowMultiPage, startedAt)
    local single = bestSinglePage(descriptors, width, height, startedAt)
    if single then return single end
    if not allowMultiPage then return nil end
    return bestMultiPage(descriptors, width, height, startedAt)
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

    local startedAt = nowMs()
    local container = gridUi.inventoryContainer
    local descriptors, model = collectDescriptors(container, gridUi.playerNum or 0)
    if #descriptors < 2 then return nil, "nothing" end
    if hasPendingWork(container, model) then return nil, "busy" end

    local width, height = GridContainer.getGridSize(container)
    -- Root player inventory deliberately stays one-page. Its unpositioned items
    -- are GridInventory's staging signal for redistribution into worn bags; a
    -- multi-page sort here would recreate the "character inventory eats all"
    -- regression seen in the dedicated test.
    local allowMultiPage = not GridSortState.isPlayerRootContainer(container)
    local packed = computePacked(descriptors, width, height, allowMultiPage, startedAt)
    local elapsed = nowMs() - startedAt
    GridAutoSort.lastSolveMs = elapsed
    if elapsed >= SLOW_SOLVE_WARN_MS then
        print("[LCC GridSort] slow solve: " .. tostring(elapsed) .. "ms for " .. tostring(#descriptors) .. " items")
    end
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

local function targetDiffers(t, signature, container)
    local item = t.item and t.item.itemObj
    local md = item and item.getModData and item:getModData() or nil
    if not md then return true end
    local page = GridSortState.isPlayerRootContainer(container)
        and 1 or (tonumber(md.gridPage) or 1)
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
        if targetDiffers(t, signature, container) then changed = true break end
    end
    if not changed then return true, "already-sorted" end

    if isClient and isClient() then
        -- CAS uses only membership + manual/server-authoritative positions.
        -- Client-only auto-fit coordinates are intentionally excluded, avoiding
        -- false stale rejects while retaining first-writer-wins for concurrent
        -- sorts of the same container.
        local expectedHash = GridSortState.authorityHash(container)
        if not GridSortNetwork.sendSort(container, targets, expectedHash, signature) then
            return false, "unavailable"
        end
        return true, "pending"
    end

    -- SP: no server round-trip; commit locally in one pass.
    local rootPlayer = GridSortState.isPlayerRootContainer(container)
    for _, t in ipairs(targets) do
        local item = t.item.itemObj
        local md = item and item.getModData and item:getModData() or nil
        if md then
            md.gridX = t.tx
            md.gridY = t.ty
            md.gridRot = t.item.rotated and true or false
            local page = rootPlayer and 1 or (t.page or 1)
            md.gridPage = page > 1 and page or nil
            md.gridContainer = signature
            md.gridManual = true
        end
    end
    GridClientNetwork.markGridChanged(container, gridUi.playerNum or 0)
    gridUi.selectedItems = {}
    return true, "sorted"
end

return GridAutoSort
