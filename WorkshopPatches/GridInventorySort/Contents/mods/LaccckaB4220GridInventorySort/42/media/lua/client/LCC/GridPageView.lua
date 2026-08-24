require "ISUI/ISInventoryPane"

local GridContainer = require("DataModel/GridContainer")
local okRender, GridRender = pcall(require, "UI/GridRender/GridRender")

local GridPageView = {}

if ISInventoryPane._lccGridSortPageViewInstalled then
    return GridPageView
end
ISInventoryPane._lccGridSortPageViewInstalled = true

local function isFloorGrid(gridUi)
    if not gridUi or not gridUi.inventoryContainer then return false end
    if gridUi.isFloor then return true end
    local inv = gridUi.inventoryContainer
    return inv.getType and inv:getType() == "floor" or false
end

local function getAllGridUis(pane)
    if not pane then return {} end
    return pane._lccAllGridUis or pane.gridContainerUis or {}
end

local function restoreAllGridUis(pane)
    if pane and pane._lccAllGridUis then
        pane.gridContainerUis = pane._lccAllGridUis
    end
end

local function collectPageGroups(all)
    local groups = {}
    for _, gridUi in ipairs(all or {}) do
        if gridUi and not gridUi.isOverflow and gridUi.inventoryContainer and not isFloorGrid(gridUi) then
            local inv = gridUi.inventoryContainer
            local list = groups[inv]
            if not list then
                list = {}
                groups[inv] = list
            end
            table.insert(list, gridUi)
        end
    end
    for _, list in pairs(groups) do
        table.sort(list, function(a, b)
            return (tonumber(a.gridIndex) or 1) < (tonumber(b.gridIndex) or 1)
        end)
    end
    return groups
end

local function setGridVisible(gridUi, visible)
    if not gridUi then return end
    if gridUi.setVisible then
        gridUi:setVisible(visible)
    else
        gridUi.visible = visible and true or false
    end
end

local function applySelection(pane)
    if not pane then return end
    local all = getAllGridUis(pane)
    pane._lccAllGridUis = all
    pane._lccSelectedGridPages = pane._lccSelectedGridPages or {}

    local groups = collectPageGroups(all)
    for inv, list in pairs(groups) do
        local count = #list
        local selected = tonumber(pane._lccSelectedGridPages[inv]) or 1
        if selected < 1 then selected = 1 end
        if selected > count then selected = count end
        pane._lccSelectedGridPages[inv] = selected

        for ordinal, gridUi in ipairs(list) do
            local page = tonumber(gridUi.gridIndex) or ordinal
            local active = (page == selected)
            gridUi._lccPagePane = pane
            gridUi._lccPageCount = count
            gridUi._lccPageNumber = page
            gridUi._lccPageActive = active
            setGridVisible(gridUi, active)
        end
    end

    local visible = {}
    for _, gridUi in ipairs(all) do
        local keep = true
        if gridUi and not gridUi.isOverflow and gridUi.inventoryContainer and not isFloorGrid(gridUi) then
            local list = groups[gridUi.inventoryContainer]
            if list and #list > 1 then
                keep = gridUi._lccPageActive == true
            end
        end
        if keep then
            setGridVisible(gridUi, true)
            table.insert(visible, gridUi)
        else
            setGridVisible(gridUi, false)
        end
    end
    pane.gridContainerUis = visible
end

function GridPageView.selectRelative(gridUi, delta)
    if not gridUi or not gridUi.inventoryContainer then return false end
    local pane = gridUi._lccPagePane
    if not pane and gridUi.getParent then pane = gridUi:getParent() end
    if not pane then return false end

    local all = getAllGridUis(pane)
    local groups = collectPageGroups(all)
    local list = groups[gridUi.inventoryContainer]
    local count = list and #list or 0
    if count <= 1 then return false end

    pane._lccSelectedGridPages = pane._lccSelectedGridPages or {}
    local current = tonumber(pane._lccSelectedGridPages[gridUi.inventoryContainer])
        or tonumber(gridUi.gridIndex) or 1
    local nextPage = ((current - 1 + (delta or 1)) % count) + 1
    pane._lccSelectedGridPages[gridUi.inventoryContainer] = nextPage
    applySelection(pane)

    -- The upstream flex layout reads gridContainerUis every prerender, so the
    -- newly selected page takes the exact physical slot of the old one without
    -- recreating the container or changing scroll position.
    return true
end

local originalRefreshContainer = ISInventoryPane.refreshContainer
function ISInventoryPane:refreshContainer(...)
    -- Upstream reuses/destroys GridRender instances from gridContainerUis. Give
    -- it the complete set while it rebuilds, then collapse each non-floor
    -- multi-page container back to one visible page for layout/rendering.
    restoreAllGridUis(self)
    originalRefreshContainer(self, ...)
    self._lccAllGridUis = self.gridContainerUis or {}
    applySelection(self)
end

if okRender and GridRender then
    local originalRender = GridRender.render
    if originalRender and not GridRender._lccPageViewRenderWrapped then
        GridRender._lccPageViewRenderWrapped = true
        function GridRender:render(...)
            local pageCount = tonumber(self._lccPageCount) or 1
            local pageNumber = tonumber(self._lccPageNumber) or tonumber(self.gridIndex) or 1
            local paged = pageCount > 1 and self._lccPageActive and not isFloorGrid(self)

            -- GridInventory labels every non-floor gridIndex>1 as "(Overflow)".
            -- These are now normal pages, so render the normal container title.
            local realIndex = nil
            if paged and tonumber(self.gridIndex) and tonumber(self.gridIndex) > 1 then
                realIndex = self.gridIndex
                self.gridIndex = 1
            end

            originalRender(self, ...)

            if realIndex then self.gridIndex = realIndex end
            if not paged then
                self._lccPagerPrevRect = nil
                self._lccPagerNextRect = nil
                return
            end

            local font = UIFont.Small
            local label = tostring(pageNumber) .. "/" .. tostring(pageCount)
            local tm = getTextManager()
            local labelW = tm:MeasureStringX(font, label)
            local labelH = tm:MeasureStringY(font, label)
            local buttonW = 15
            local gap = 3
            local totalW = buttonW + gap + labelW + gap + buttonW
            local headerH = tonumber(self.headerH) or 28
            local pad = tonumber(self.gridPadding) or 10
            local boxH = math.max(16, math.min(20, headerH - 6))
            local x = math.floor((self.width - totalW) / 2)
            local y = pad + math.max(1, math.floor((headerH - boxH) / 2) - 1)

            -- Opaque backing prevents a long container title from visually
            -- colliding with the pager without needing to fork GridRender.lua.
            self:drawRect(x - 3, y - 1, totalW + 6, boxH + 2, 0.88, 0.03, 0.03, 0.03)

            self:drawRectBorder(x, y, buttonW, boxH, 0.75, 0.65, 0.65, 0.65)
            self:drawText("<", x + 4, y + math.floor((boxH - labelH) / 2), 0.92, 0.92, 0.92, 1, font)

            local labelX = x + buttonW + gap
            self:drawText(label, labelX, y + math.floor((boxH - labelH) / 2), 0.95, 0.95, 0.95, 1, font)

            local nextX = labelX + labelW + gap
            self:drawRectBorder(nextX, y, buttonW, boxH, 0.75, 0.65, 0.65, 0.65)
            self:drawText(">", nextX + 4, y + math.floor((boxH - labelH) / 2), 0.92, 0.92, 0.92, 1, font)

            self._lccPagerPrevRect = { x = x, y = y, w = buttonW, h = boxH }
            self._lccPagerNextRect = { x = nextX, y = y, w = buttonW, h = boxH }
        end
    end

    local function hit(rect, x, y)
        return rect and x >= rect.x and y >= rect.y
            and x < rect.x + rect.w and y < rect.y + rect.h
    end

    local originalOnMouseDown = GridRender.onMouseDown
    if originalOnMouseDown and not GridRender._lccPageViewMouseWrapped then
        GridRender._lccPageViewMouseWrapped = true
        function GridRender:onMouseDown(x, y)
            if self._lccPageActive and (tonumber(self._lccPageCount) or 1) > 1 then
                if hit(self._lccPagerPrevRect, x, y) then
                    GridPageView.selectRelative(self, -1)
                    return true
                end
                if hit(self._lccPagerNextRect, x, y) then
                    GridPageView.selectRelative(self, 1)
                    return true
                end
            end
            return originalOnMouseDown(self, x, y)
        end
    end
end

print("[LCC GridSort] single-panel multi-page view installed")
return GridPageView
