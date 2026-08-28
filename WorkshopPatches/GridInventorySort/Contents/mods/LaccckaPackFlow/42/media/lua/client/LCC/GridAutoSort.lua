local GridCore = require("DataModel/GridCore")
local GridContainer = require("DataModel/GridContainer")
local ItemFootprint = require("Algorithm/ItemFootprint")
local GridClientNetwork = require("Network/GridClientNetwork")
local GridSortState = require("LCC/GridSortState")
local GridSortNetwork = require("LCC/GridSortNetwork")
local okSearch, GridInventory_Search = pcall(require, "System/GridInventory_Search")

local GridAutoSort = {}

local SLOW_SOLVE_WARN_MS = 75

local function nowMs()
    return getTimestampMs and getTimestampMs() or (getTimeInMillis and getTimeInMillis() or 0)
end

local function itemId(item)
    return item and item.getID and item:getID() or nil
end

local function itemDisplayName(item)
    if not item then return "" end
    local value = nil
    if item.getDisplayName then value = item:getDisplayName() end
    if (value == nil or value == "") and item.getName then value = item:getName() end
    if value == nil then value = "" end
    return tostring(value)
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

local function normalizedName(item)
    return string.lower(itemDisplayName(item))
end

local function makeDescriptor(item)
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
        sortName = normalizedName(item),
        sortType = itemFullType(item),
    }
end

local function collectDescriptors(container, playerNum)
    local model = GridContainer.getOrCreate(container, playerNum)
    local out = {}
    for _, item in ipairs(GridSortState.collectItems(container)) do
        local d = makeDescriptor(item)
        if d then table.insert(out, d) end
    end
    return out, model
end

local function compareId(a, b)
    local ai, bi = tonumber(a.id), tonumber(b.id)
    if ai and bi then return ai < bi end
    return tostring(a.id) < tostring(b.id)
end

local function areaFirst(a, b)
    if a.area ~= b.area then return a.area > b.area end
    if a.longSide ~= b.longSide then return a.longSide > b.longSide end
    if a.shortSide ~= b.shortSide then return a.shortSide > b.shortSide end
    return compareId(a, b)
end

local function longFirst(a, b)
    if a.longSide ~= b.longSide then return a.longSide > b.longSide end
    if a.area ~= b.area then return a.area > b.area end
    if a.shortSide ~= b.shortSide then return a.shortSide > b.shortSide end
    return compareId(a, b)
end

local function nameFirst(a, b)
    if a.sortName ~= b.sortName then return a.sortName < b.sortName end
    if a.sortType ~= b.sortType then return a.sortType < b.sortType end
    local ak, bk = tostring(a.compatKey or ""), tostring(b.compatKey or "")
    if ak ~= bk then return ak < bk end
    if a.area ~= b.area then return a.area > b.area end
    if a.longSide ~= b.longSide then return a.longSide > b.longSide end
    if a.shortSide ~= b.shortSide then return a.shortSide > b.shortSide end
    return compareId(a, b)
end

local function copyAndSort(descriptors, sorter)
    local ordered = {}
    for i = 1, #descriptors do ordered[i] = descriptors[i] end
    table.sort(ordered, sorter)
    return ordered
end

local function perimeterContact(grid, x, y, w, h)
    local contact = 0
    for cx = x, x + w - 1 do
        local up, down = y - 1, y + h
        if up < 1 or grid.cells[cx][up] ~= nil then contact = contact + 1 end
        if down > grid.height or grid.cells[cx][down] ~= nil then contact = contact + 1 end
    end
    for cy = y, y + h - 1 do
        local left, right = x - 1, x + w
        if left < 1 or grid.cells[left][cy] ~= nil then contact = contact + 1 end
        if right > grid.width or grid.cells[right][cy] ~= nil then contact = contact + 1 end
    end
    return contact
end

local function findBestPlacement(grid, d, bottom, occupiedCells)
    local best = nil

    local function consider(rotated)
        local ew = rotated and d.originalH or d.originalW
        local eh = rotated and d.originalW or d.originalH
        if ew > grid.width or eh > grid.height then return end

        for y = 1, grid.height - eh + 1 do
            for x = 1, grid.width - ew + 1 do
                if grid:canPlaceItem(d.id, x, y, ew, eh, nil,
                    d.compatKey, rotated, d.stackInfo) then
                    local occupant = grid.cells[x][y]
                    local isStack = d.compatKey ~= nil and occupant ~= nil
                    local added = isStack and 0 or ew * eh
                    local newBottom = math.max(bottom, y + eh - 1)
                    local holes = math.max(0, newBottom * grid.width - (occupiedCells + added))
                    local contact = isStack and 99 or perimeterContact(grid, x, y, ew, eh)
                    local rotationPenalty = rotated == d.currentRotated and 0 or 1
                    local score = newBottom * 1000000
                        + holes * 1000
                        - contact * 20
                        + y * 10 + x
                        + rotationPenalty
                    if isStack then score = score - 100000 end

                    if not best or score < best.score then
                        best = {
                            tx = x, ty = y, ew = ew, eh = eh,
                            rotated = rotated, score = score,
                            added = added, newBottom = newBottom,
                        }
                    end
                end
            end
        end
    end

    consider(false)
    if d.originalW ~= d.originalH then consider(true) end
    return best
end

local function packGreedy(descriptors, width, height, sorter)
    local ordered = copyAndSort(descriptors, sorter)
    local grid = GridCore.new(width, height)
    local targets = {}
    local bottom, occupiedCells, rotations = 0, 0, 0

    for _, d in ipairs(ordered) do
        local p = findBestPlacement(grid, d, bottom, occupiedCells)
        if not p then return nil end
        if not grid:insertItem(d.id, p.tx, p.ty, p.ew, p.eh, p.rotated,
            d.itemObj, d.compatKey, d.stackInfo) then
            return nil
        end
        bottom = p.newBottom
        occupiedCells = occupiedCells + p.added
        if p.rotated then rotations = rotations + 1 end
        table.insert(targets, {
            d = d,
            tx = p.tx, ty = p.ty,
            ew = p.ew, eh = p.eh,
            rotated = p.rotated,
        })
    end

    return {
        targets = targets,
        usedRows = bottom,
        holes = math.max(0, bottom * width - occupiedCells),
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

local function better(a, b)
    if not a then return b end
    if not b then return a end
    if a.usedRows ~= b.usedRows then return a.usedRows < b.usedRows and a or b end
    if a.holes ~= b.holes then return a.holes < b.holes and a or b end
    if a.rotations ~= b.rotations then return a.rotations < b.rotations and a or b end
    return a
end

local function computePacked(descriptors, width, height)
    if lowerBoundArea(descriptors) > width * height then return nil end

    local named = packGreedy(descriptors, width, height, nameFirst)
    if named then return named end

    local best = packGreedy(descriptors, width, height, areaFirst)
    if #descriptors <= 50 then
        best = better(best, packGreedy(descriptors, width, height, longFirst))
    end
    if best then
        print("[LCC GridSort] alphabetical pass could not fit; geometric fallback used")
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

local function needsSearch(container, playerNum, gridUi)
    if gridUi and gridUi.needsSearch and gridUi:needsSearch() then return true end
    if okSearch and GridInventory_Search and GridInventory_Search.needsSearch then
        local ok, result = pcall(function()
            return GridInventory_Search.needsSearch(playerNum or 0, container)
        end)
        if ok and result then return true end
    end
    return false
end

function GridAutoSort.canSortContainer(container, playerNum, gridUi)
    if not container then return false, "unavailable" end
    if gridUi and gridUi.isOverflow then return false, "unavailable" end
    if container.getType and container:getType() == "floor" then return false, "floor" end
    local parent = container.getParent and container:getParent()
    if parent and instanceof and instanceof(parent, "IsoDeadBody") then return false, "corpse" end
    if isClient and isClient() and isNestedBagContainer(container) then return false, "nested" end
    if GridSortNetwork.isPending(container) then return false, "busy" end
    if needsSearch(container, playerNum, gridUi) then return false, "search" end

    local model = GridContainer.getOrCreate(container, playerNum or 0)
    if hasPendingWork(container, model) then return false, "busy" end

    local okBag, BagDrop = pcall(require, "System/GridInventory_BagDrop")
    if okBag and BagDrop and BagDrop.isNestedLocked then
        local player = getSpecificPlayer(playerNum or 0)
        if BagDrop.isNestedLocked(container, player) then return false, "locked" end
    end
    return true
end

function GridAutoSort.canSort(gridUi)
    if not gridUi or not gridUi.inventoryContainer then return false, "unavailable" end
    return GridAutoSort.canSortContainer(gridUi.inventoryContainer, gridUi.playerNum or 0, gridUi)
end

function GridAutoSort.computeTargetsForContainer(container, playerNum, gridUi)
    local can, reason = GridAutoSort.canSortContainer(container, playerNum, gridUi)
    if not can then return nil, reason end

    local startedAt = nowMs()
    local descriptors, model = collectDescriptors(container, playerNum or 0)
    if #descriptors < 2 then return nil, "nothing" end
    if hasPendingWork(container, model) then return nil, "busy" end

    local width, height = GridContainer.getGridSize(container)
    local packed = computePacked(descriptors, width, height)
    local elapsed = nowMs() - startedAt
    GridAutoSort.lastSolveMs = elapsed
    if elapsed >= SLOW_SOLVE_WARN_MS then
        print("[LCC GridSort] slow solve: " .. tostring(elapsed)
            .. "ms for " .. tostring(#descriptors) .. " items in "
            .. tostring(width) .. "x" .. tostring(height))
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
        })
    end
    return targets, nil
end

function GridAutoSort.computeTargets(gridUi)
    if not gridUi or not gridUi.inventoryContainer then return nil, "unavailable" end
    return GridAutoSort.computeTargetsForContainer(
        gridUi.inventoryContainer, gridUi.playerNum or 0, gridUi)
end

local function targetDiffers(t, signature)
    local item = t.item and t.item.itemObj
    local md = item and item.getModData and item:getModData() or nil
    if not md then return true end
    return tonumber(md.gridX) ~= t.tx
        or tonumber(md.gridY) ~= t.ty
        or (md.gridRot and true or false) ~= (t.item.rotated and true or false)
        or md.gridContainer ~= signature
        or md.gridManual ~= true
        or md.gridPage ~= nil
end

function GridAutoSort.sortContainer(container, playerNum, gridUi)
    local targets, reason = GridAutoSort.computeTargetsForContainer(container, playerNum, gridUi)
    if not targets then return false, reason end

    local signature = GridContainer.containerSignature(container)
    local changed = false
    for _, t in ipairs(targets) do
        if targetDiffers(t, signature) then changed = true break end
    end
    if not changed then return true, "already-sorted" end

    if isClient and isClient() then
        if not GridSortNetwork.sendSort(container, targets, signature) then
            return false, "unavailable"
        end
        return true, "pending"
    end

    for _, t in ipairs(targets) do
        local item = t.item.itemObj
        local md = item and item.getModData and item:getModData() or nil
        if md then
            md.gridX = t.tx
            md.gridY = t.ty
            md.gridRot = t.item.rotated and true or false
            md.gridPage = nil
            md.gridContainer = signature
            md.gridManual = true
        end
    end
    GridClientNetwork.markGridChanged(container, playerNum or 0)
    if gridUi then gridUi.selectedItems = {} end
    return true, "sorted"
end

function GridAutoSort.sort(gridUi)
    if not gridUi or not gridUi.inventoryContainer then return false, "unavailable" end
    return GridAutoSort.sortContainer(gridUi.inventoryContainer, gridUi.playerNum or 0, gridUi)
end

return GridAutoSort
