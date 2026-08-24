local GridContainer = require("DataModel/GridContainer")
local GridCore = require("DataModel/GridCore")
local ItemFootprint = require("Algorithm/ItemFootprint")
local GridSortState = require("LCC/GridSortState")

-- Shared files may be executed by the PZ loader before another file explicitly
-- requires them. Keep the actual module table on GridContainer so a second
-- execution returns the installed implementation instead of an empty table.
if GridContainer._lccContinuousGridModule then
    return GridContainer._lccContinuousGridModule
end

local GridContinuousGrid = {}
GridContainer._lccContinuousGridModule = GridContinuousGrid
GridContainer._lccContinuousGridInstalled = true

local originalGetGridSize = GridContainer.getGridSize
local originalRefresh = GridContainer.refresh

-- One physical container must always remain one mathematical/UI grid.
-- We only extend rows when the current direct contents need more geometric room
-- than GridInventory's base dimensions provide. Vanilla ItemContainer capacity
-- remains authoritative for whether NEW items may be inserted.
local MAX_HEIGHT = 60
local SAFETY_ROWS = 3

local function isEligible(container)
    if not container then return false end
    if container.getType and container:getType() == "floor" then return false end
    if GridSortState.isPlayerRootContainer(container) then return false end
    return true
end

local function bestHeightForWidth(itemW, itemH, gridW)
    local best = nil
    if itemW <= gridW then best = itemH end
    if itemH <= gridW then best = best and math.min(best, itemW) or itemW end
    return best
end

local function measureContents(container, gridW)
    local area = 0
    local maxItemH = 1
    local manualBottom = 0
    local groups = {}

    for _, item in ipairs(GridSortState.collectItems(container)) do
        local w, h = ItemFootprint.getSize(item)
        w, h = tonumber(w), tonumber(h)
        if w and h and w > 0 and h > 0 then
            local minH = bestHeightForWidth(w, h, gridW)
            if minH then maxItemH = math.max(maxItemH, minH) end

            local compatKey, stackInfo = GridContainer.getStackInfo(item)
            local limit = stackInfo and tonumber(stackInfo.limit) or nil
            local units = stackInfo and tonumber(stackInfo.units) or 1
            if compatKey and limit and limit > 1 then
                local key = tostring(compatKey) .. ":" .. tostring(w) .. "x" .. tostring(h)
                local g = groups[key]
                if not g then
                    g = {
                        units = 0,
                        limit = limit,
                        area = w * h,
                        minH = minH or math.max(w, h),
                    }
                    groups[key] = g
                end
                g.units = g.units + (units or 1)
            else
                area = area + (w * h)
            end

            local md = item.getModData and item:getModData() or nil
            if md and md.gridManual and md.gridY then
                local rotated = md.gridRot and true or false
                local eh = rotated and w or h
                manualBottom = math.max(manualBottom, tonumber(md.gridY) + eh - 1)
            end
        end
    end

    for _, g in pairs(groups) do
        local logicalPiles = math.ceil(g.units / math.max(1, g.limit))
        area = area + logicalPiles * g.area
        maxItemH = math.max(maxItemH, g.minH or 1)
    end

    return area, maxItemH, manualBottom
end

function GridContinuousGrid.normalizeLegacyPages(container)
    if not container or not container.getItems then return false end
    local changed = false
    local items = container:getItems()
    for i = 0, items:size() - 1 do
        local item = items:get(i)
        local md = item and item.getModData and item:getModData() or nil
        if md and md.gridPage ~= nil then
            local page = tonumber(md.gridPage) or 1
            md.gridPage = nil
            if page > 1 then
                -- v0.2/v0.3 coordinates were page-local and therefore overlap
                -- page 1 in a continuous grid. Let the normal deterministic
                -- refresh place them again instead of translating them.
                md.gridX = nil
                md.gridY = nil
                md.gridRot = false
                md.gridManual = nil
            end
            changed = true
        end
    end
    return changed
end

function GridContinuousGrid.getGridSize(container)
    local baseW, baseH = originalGetGridSize(container)
    if not isEligible(container) then return baseW, baseH end

    -- One-time migration is deliberately tied to this shared query. Both the
    -- client UI and the original dedicated GridServerNetwork ask getGridSize(),
    -- so stale page-local coordinates cannot survive on only one side.
    GridContinuousGrid.normalizeLegacyPages(container)

    local area, maxItemH, manualBottom = measureContents(container, baseW)
    if area <= 0 then return baseW, baseH end

    -- Area rows are the lower bound. Adding the tallest possible item plus a
    -- small safety band absorbs normal first-fit fragmentation and leaves room
    -- for the next vanilla-approved transfer without a page allocator.
    local areaRows = math.ceil(area / math.max(1, baseW))
    local desiredH = math.max(baseH, areaRows + maxItemH + SAFETY_ROWS, manualBottom + 1)
    desiredH = math.min(MAX_HEIGHT, desiredH)
    return baseW, desiredH
end

GridContainer.getGridSize = function(container)
    return GridContinuousGrid.getGridSize(container)
end

-- GridCore dimensions are fixed at construction time. Rebuild only the single
-- core when deterministic effective dimensions change; never create page grids.
if originalRefresh and not (isServer and isServer()) then
    function GridContainer:refresh(...)
        GridContinuousGrid.normalizeLegacyPages(self.inventory)
        local w, h = GridContainer.getGridSize(self.inventory)
        local first = self.grids and self.grids[1] or nil
        if not first or first.width ~= w or first.height ~= h or #(self.grids or {}) ~= 1 then
            self.grids = { GridCore.new(w, h) }
        end
        return originalRefresh(self, ...)
    end
end

print("[LCC GridSort] adaptive continuous grid installed")
return GridContinuousGrid
