local GridContainer = require("DataModel/GridContainer")
local GridCore = require("DataModel/GridCore")
local ItemFootprint = require("Algorithm/ItemFootprint")
local GridSortState = require("LCC/GridSortState")

local GridMultiPage = {}

local originalRefresh = GridContainer.refresh
if not originalRefresh or GridContainer._lccMultiPageInstalled then
    return GridMultiPage
end
GridContainer._lccMultiPageInstalled = true

local function itemId(item)
    return item and item.getID and item:getID() or nil
end

local function descriptor(item)
    if not item then return nil end
    local w, h = ItemFootprint.getSize(item)
    w, h = tonumber(w), tonumber(h)
    if not w or not h or w < 1 or h < 1 then return nil end
    local compatKey, stackInfo = GridContainer.getStackInfo(item)
    local md = item.getModData and item:getModData() or nil
    local savedPage = md and tonumber(md.gridPage) or 1
    return {
        id = itemId(item), itemObj = item,
        w = w, h = h,
        compatKey = compatKey, stackInfo = stackInfo,
        savedX = md and tonumber(md.gridX) or nil,
        savedY = md and tonumber(md.gridY) or nil,
        savedRot = md and md.gridRot and true or false,
        savedPage = savedPage,
        persistedPage = savedPage > 1,
        manual = md and md.gridManual and true or false,
        area = w * h,
    }
end

local function compareDescriptors(a, b)
    if a.persistedPage ~= b.persistedPage then return a.persistedPage end
    if a.savedPage ~= b.savedPage then return a.savedPage < b.savedPage end
    if a.manual ~= b.manual then return a.manual end
    if a.area ~= b.area then return a.area > b.area end
    return tostring(a.id) < tostring(b.id)
end

local function findPlacement(grid, d, preferSaved)
    if preferSaved and d.savedX and d.savedY then
        local ew = d.savedRot and d.h or d.w
        local eh = d.savedRot and d.w or d.h
        if grid:canPlaceItem(d.id, d.savedX, d.savedY, ew, eh, nil,
            d.compatKey, d.savedRot, d.stackInfo) then
            return d.savedX, d.savedY, d.savedRot, ew, eh
        end
    end

    local best = nil
    local function consider(rotated)
        local ew = rotated and d.h or d.w
        local eh = rotated and d.w or d.h
        if ew > grid.width or eh > grid.height then return end
        for y = 1, grid.height - eh + 1 do
            for x = 1, grid.width - ew + 1 do
                if grid:canPlaceItem(d.id, x, y, ew, eh, nil,
                    d.compatKey, rotated, d.stackInfo) then
                    local bottom = y + eh - 1
                    local right = x + ew - 1
                    local score = bottom * 10000 + right * 100 + y * 10 + x
                    if not best or score < best.score then
                        best = { x = x, y = y, rotated = rotated, w = ew, h = eh, score = score }
                    end
                end
            end
        end
    end
    consider(false)
    if d.w ~= d.h then consider(true) end
    if best then return best.x, best.y, best.rotated, best.w, best.h end
    return nil
end

local function ensurePage(self, page, width, height)
    while #self.grids < page and #self.grids < GridSortState.MAX_PAGES do
        table.insert(self.grids, GridCore.new(width, height))
    end
    return self.grids[page]
end

local function isFloor(container)
    return container and container.getType and container:getType() == "floor"
end

local function insertDescriptor(grid, d, x, y, rotated, ew, eh)
    return grid:insertItem(d.id, x, y, ew, eh, rotated,
        d.itemObj, d.compatKey, d.stackInfo)
end

local function extendOverflowIntoPages(self)
    local container = self.inventory
    if not container or isFloor(container) then return end

    -- IMPORTANT: the player's root inventory is deliberately NOT multi-page.
    -- GridInventory's own AutoDrop uses root-inventory unpositioned items as a
    -- staging signal and redistributes them into worn bags. Rescuing those items
    -- into extra 3x4 pages makes the character inventory absorb everything and
    -- prevents that routing, which produced dozens of tiny player grids and a
    -- massively overloaded root inventory. Bags/world containers still use the
    -- real multi-page rescue below; vanilla capacity remains authoritative.
    if GridSortState.isPlayerRootContainer(container) then return end

    local width, height = GridContainer.getGridSize(container)
    local sig = GridContainer.containerSignature(container)
    local persistedPages = {}
    local overflow = {}
    local seen = {}

    -- Upstream refresh understands only one page. Any item already assigned to
    -- page >1 (manual OR automatic/server-synced) may therefore be temporarily
    -- inserted into page 1 from its x/y. Remove it and rebuild its real page.
    if container.getItems then
        local items = container:getItems()
        for i = 0, items:size() - 1 do
            local item = items:get(i)
            local md = item and item.getModData and item:getModData() or nil
            if GridSortState.isGridItem(item) and md
                and tonumber(md.gridPage) and tonumber(md.gridPage) > 1 then
                local d = descriptor(item)
                if d then
                    seen[d.id] = true
                    table.insert(persistedPages, d)
                    for _, grid in ipairs(self.grids or {}) do
                        if grid.items and grid.items[d.id] then grid:removeItem(d.id) end
                    end
                end
            end
        end
    end

    for _, item in ipairs(self.unpositioned or {}) do
        local id = itemId(item)
        if id ~= nil and not seen[id] then
            local d = descriptor(item)
            if d then
                seen[id] = true
                table.insert(overflow, d)
            end
        end
    end

    if #persistedPages == 0 and #overflow == 0 then return end

    table.sort(persistedPages, compareDescriptors)
    table.sort(overflow, compareDescriptors)

    -- A page-2 item may have temporarily occupied page 1 during upstream
    -- refresh and pushed an otherwise fitting item to unpositioned. Retry those
    -- ordinary overflow items against the newly freed base page first.
    local base = self.grids and self.grids[1] or nil
    local pagePending = {}
    if base then
        for _, d in ipairs(overflow) do
            local x, y, rotated, ew, eh = findPlacement(base, d, false)
            if x and y then
                insertDescriptor(base, d, x, y, rotated, ew, eh)
            else
                table.insert(pagePending, d)
            end
        end
    else
        for _, d in ipairs(overflow) do table.insert(pagePending, d) end
    end

    -- Existing page assignments win their saved pages first. New overflow then
    -- fills the remaining cells and opens later pages as needed.
    local pending = {}
    for _, d in ipairs(persistedPages) do table.insert(pending, d) end
    for _, d in ipairs(pagePending) do table.insert(pending, d) end

    local stillUnpositioned = {}
    local autoAssignments = {}
    for _, d in ipairs(pending) do
        local placed = false
        local wasPersisted = d.persistedPage
        local startPage = wasPersisted and d.savedPage or 2
        local page = startPage
        while page <= GridSortState.MAX_PAGES do
            local grid = ensurePage(self, page, width, height)
            if not grid then break end
            local x, y, rotated, ew, eh = findPlacement(grid, d, wasPersisted and page == d.savedPage)
            if x and y then
                insertDescriptor(grid, d, x, y, rotated, ew, eh)

                local md = d.itemObj:getModData()
                md.gridX = x
                md.gridY = y
                md.gridRot = rotated and true or false
                md.gridPage = page
                md.gridContainer = sig

                if not wasPersisted then
                    -- This is automatic presentation/page routing, not a user
                    -- placement. Keep it out of authorityHash while still making
                    -- page routing persistent and synchronizable in MP.
                    md.gridManual = nil
                    table.insert(autoAssignments, {
                        itemId = d.id,
                        x = x, y = y,
                        page = page,
                        rotated = rotated and true or false,
                    })
                end

                placed = true
                break
            end
            page = page + 1
        end
        if not placed then table.insert(stillUnpositioned, d.itemObj) end
    end

    self.unpositioned = stillUnpositioned

    if #autoAssignments > 0 and isClient and isClient() then
        local okNet, GridSortNetwork = pcall(require, "LCC/GridSortNetwork")
        if okNet and GridSortNetwork and GridSortNetwork.sendPageAssignments then
            GridSortNetwork.sendPageAssignments(container, autoAssignments, sig)
        end
    end
end

function GridContainer:refresh(...)
    originalRefresh(self, ...)
    extendOverflowIntoPages(self)
end

print("[LCC GridSort] multi-page overflow rescue installed")
return GridMultiPage
