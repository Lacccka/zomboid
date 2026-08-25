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

-- GridInventory's upstream capacity formula budgets about two cells per unit
-- of container capacity (6 columns * ceil(capacity / 3)). Keep that calibration
-- and add a small packing reserve for non-rectangular first-fit layouts.
--
-- Dimensions are derived from capacity only, never from the current membership.
-- Client and dedicated server therefore agree even while item replication is a
-- frame behind, and adding/removing an item cannot resize the panel.
local CELLS_PER_CAPACITY = 2
local PACKING_RESERVE = 1.25
local ORGANIZED_CEILING = 1.30
local MAX_WIDTH = 12
local MAX_HEIGHT = 30

local function isPlayerRoot(container)
    return GridSortState.isPlayerRootContainer(container)
end

local function isEligible(container)
    if not container then return false end
    if container.getType and container:getType() == "floor" then return false end
    if isPlayerRoot(container) then return false end

    -- Keep every character root and corpse at GridInventory's upstream size.
    -- The containing-item check distinguishes a real bag from a root inventory.
    local containing = container.getContainingItem and container:getContainingItem() or nil
    local parent = container.getParent and container:getParent() or nil
    if not containing and parent and instanceof then
        if instanceof(parent, "IsoGameCharacter") then return false end
        if instanceof(parent, "IsoDeadBody") then return false end
    end
    return true
end

-- ItemContainer.getEffectiveCapacity(character) applies Organized/Disorganized
-- to normal bags, world containers and vehicle containers, but not to a root
-- character inventory, corpse or floor. Grid geometry cannot be viewer-specific
-- in MP, so use the largest vanilla trait result as a stable shared ceiling.
-- This also covers wrappers such as MFSBackpackCapacity, whose Organized result
-- rounds upward instead of using Java's truncation.
local function supportsTraitCapacity(container)
    if not container then return false end
    if container.getType and container:getType() == "floor" then return false end

    local parent = container.getParent and container:getParent() or nil
    if parent and instanceof then
        if instanceof(parent, "IsoGameCharacter") then return false end
        if instanceof(parent, "IsoDeadBody") then return false end
    end
    return true
end

function GridContinuousGrid.getCapacityCeiling(container)
    if not container or not container.getCapacity then return 0 end
    local ok, value = pcall(function() return container:getCapacity() end)
    local capacity = ok and tonumber(value) or 0
    if not capacity or capacity <= 0 then return 0 end

    if supportsTraitCapacity(container) then
        capacity = math.max(capacity, math.ceil(capacity * ORGANIZED_CEILING))
    end
    return capacity
end

local function boundedDimensions(baseW, baseH, capacity)
    baseW = math.max(1, tonumber(baseW) or 1)
    baseH = math.max(1, tonumber(baseH) or 1)
    capacity = math.max(0, tonumber(capacity) or 0)

    local baseArea = baseW * baseH
    local capacityArea = math.ceil(
        capacity * CELLS_PER_CAPACITY * PACKING_RESERVE)
    local targetArea = math.max(baseArea, capacityArea)

    -- Prefer a compact, near-square rectangle. Preserve any wider/taller firm
    -- upstream/GridDevTool override as a minimum, and cap only our own growth.
    local desiredW = math.ceil(math.sqrt(targetArea))
    desiredW = math.max(baseW, math.min(MAX_WIDTH, desiredW))

    local desiredH = math.max(baseH, math.ceil(targetArea / desiredW))
    desiredH = math.min(math.max(baseH, MAX_HEIGHT), desiredH)
    return desiredW, desiredH
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
                -- page 1 in a single grid. Let refresh place them again.
                md.gridX = nil
                md.gridY = nil
                md.gridRot = false
                md.gridManual = nil
                md.gridContainer = nil
            end
            changed = true
        end
    end
    return changed
end

-- v0.4 could create a grid as tall as 60 rows. If an item was manually left
-- below the new bounded rectangle, clear only that stale placement and let the
-- ordinary deterministic refresh repack it. In-bounds manual layouts survive.
function GridContinuousGrid.normalizeBounds(container, gridW, gridH)
    if not container or not container.getItems then return false end
    local changed = false
    local items = container:getItems()
    for i = 0, items:size() - 1 do
        local item = items:get(i)
        local md = item and item.getModData and item:getModData() or nil
        local x = md and tonumber(md.gridX) or nil
        local y = md and tonumber(md.gridY) or nil
        if x and y then
            local w, h = ItemFootprint.getSize(item)
            w, h = tonumber(w) or 1, tonumber(h) or 1
            if md.gridRot then w, h = h, w end
            local inBounds = x >= 1 and y >= 1
                and (x + w - 1) <= gridW
                and (y + h - 1) <= gridH
            if not inBounds then
                md.gridX = nil
                md.gridY = nil
                md.gridRot = false
                md.gridManual = nil
                md.gridContainer = nil
                changed = true
            end
        end
    end
    return changed
end

function GridContinuousGrid.getGridSize(container)
    local baseW, baseH = originalGetGridSize(container)

    -- Migration must also run for the player root. Root inventory deliberately
    -- stays upstream-sized so GridAutoDrop can still route items to worn bags.
    GridContinuousGrid.normalizeLegacyPages(container)
    if not isEligible(container) then return baseW, baseH end

    local capacity = GridContinuousGrid.getCapacityCeiling(container)
    local w, h = boundedDimensions(baseW, baseH, capacity)
    GridContinuousGrid.normalizeBounds(container, w, h)
    return w, h
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

print("[LCC GridSort] bounded capacity grid installed")
return GridContinuousGrid
