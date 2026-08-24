require "ISUI/InventoryWindow/ISInventoryWindowContainerControls"
require "ISUI/InventoryWindow/ISInventoryWindowControlHandler"
require "ISUI/LootWindow/ISLootWindowContainerControls"
require "ISUI/LootWindow/ISLootWindowObjectControlHandler"

-- Explicit load order for the addon layers. Guards inside each module make this
-- safe even when the PZ loader has already executed a nested client file.
pcall(require, "LCC/GridMultiPage")
pcall(require, "LCC/GridPaneUX")

local okSort, GridAutoSort = pcall(require, "LCC/GridAutoSort")
if not okSort or not GridAutoSort then
    print("[LCC GridSort] GridInventory API unavailable; addon disabled")
    return
end

local function text(key, fallback)
    if getTextOrNull then
        local value = getTextOrNull(key)
        if value then return value end
    end
    return fallback
end

local REASON_TOOLTIP = {
    floor = { "UI_LCC_GridSort_Floor", "Auto-sort is disabled on the floor." },
    corpse = { "UI_LCC_GridSort_Corpse", "Corpse auto-sort is disabled in this version." },
    nested = { "UI_LCC_GridSort_Nested", "Nested bags are temporarily not auto-sorted in multiplayer." },
    search = { "UI_LCC_GridSort_SearchFirst", "Search this container before sorting." },
    busy = { "UI_LCC_GridSort_Busy", "Wait for the current inventory action to finish." },
    locked = { "UI_LCC_GridSort_Locked", "This nested container is currently locked." },
    nothing = { "UI_LCC_GridSort_Nothing", "There is nothing to sort in this container." },
    ["no-space"] = { "UI_LCC_GridSort_NoSpace", "These items cannot be represented safely in the available grid pages." },
    unavailable = { "UI_LCC_GridSort_Unavailable", "Auto-sort is unavailable for this grid." },
}

local function tooltipForReason(reason)
    local entry = REASON_TOOLTIP[reason]
    if entry then return text(entry[1], entry[2]) end
    return text("UI_LCC_GridSort_Tooltip", "Auto-sort this container.")
end

local function getPage(handler)
    return handler and (handler.inventoryWindow or handler.lootWindow) or nil
end

local function getActiveGrid(handler)
    local page = getPage(handler)
    local pane = page and page.inventoryPane
    local container = handler and handler.container
    if not pane or not container or not pane.gridContainerUis then return nil end

    for _, grid in ipairs(pane.gridContainerUis) do
        if not grid.isOverflow and grid.inventoryContainer == container then
            return grid
        end
    end
    return nil
end

local function isFloor(container)
    return container and container.getType and container:getType() == "floor"
end

local function setButtonEnabled(button, enabled)
    if not button then return end
    if button.setEnable then
        button:setEnable(enabled)
    else
        button.enable = enabled
    end
end

-- Draw the icon on the normal vanilla handler button. This keeps all ownership,
-- add/remove, row wrapping and footer sizing in the game's control-handler
-- system while avoiding a separate PNG asset.
local function decorateSortButton(button)
    if not button or button._lccGridSortIconInstalled then return end
    button._lccGridSortIconInstalled = true
    button.internal = "LCC_GRID_AUTO_SORT"

    local originalPrerender = button.prerender
    button.prerender = function(self)
        if originalPrerender then originalPrerender(self) end

        local w = self.getWidth and self:getWidth() or self.width or 0
        local h = self.getHeight and self:getHeight() or self.height or 0
        if w <= 0 or h <= 0 then return end

        local size = math.min(w, h)
        local ox = math.floor((w - size) / 2)
        local oy = math.floor((h - size) / 2)
        local enabled = self.enable ~= false
        local alpha = enabled and 0.90 or 0.35
        if enabled and self.mouseOver then alpha = 1.0 end

        local r, g, b = 0.92, 0.92, 0.92
        local arrowX = ox + math.floor(size * 0.27)
        local topY = oy + math.floor(size * 0.18)
        local bottomY = oy + math.floor(size * 0.82)
        local shaftTop = topY + 4
        local shaftBottom = bottomY - 4

        self:drawRect(arrowX, shaftTop, 2, math.max(2, shaftBottom - shaftTop), alpha, r, g, b)
        self:drawRect(arrowX - 3, topY + 4, 8, 2, alpha, r, g, b)
        self:drawRect(arrowX - 2, topY + 2, 6, 2, alpha, r, g, b)
        self:drawRect(arrowX - 1, topY, 4, 2, alpha, r, g, b)
        self:drawRect(arrowX - 3, bottomY - 5, 8, 2, alpha, r, g, b)
        self:drawRect(arrowX - 2, bottomY - 3, 6, 2, alpha, r, g, b)
        self:drawRect(arrowX - 1, bottomY - 1, 4, 2, alpha, r, g, b)

        local barsX = ox + math.floor(size * 0.52)
        local barY = oy + math.floor(size * 0.27)
        local maxLen = math.max(7, math.floor(size * 0.30))
        self:drawRect(barsX, barY, maxLen, 2, alpha, r, g, b)
        self:drawRect(barsX, barY + math.floor(size * 0.20), math.max(6, maxLen - 2), 2, alpha, r, g, b)
        self:drawRect(barsX, barY + math.floor(size * 0.40), math.max(5, maxLen - 4), 2, alpha, r, g, b)
    end
end

local function normalizeSortButton(button)
    if not button then return end
    if button.setTitle then button:setTitle("") end
    local h = button.getHeight and button:getHeight() or button.height or 0
    if h > 0 then button:setWidth(math.max(24, h)) end
    decorateSortButton(button)
end

local function refreshHandlerState(handler)
    local button = handler and handler.control
    if not button then return end

    local now = getTimeInMillis and getTimeInMillis() or 0
    local stickyReason = handler._lccStatusReason
    local stickyUntil = handler._lccStatusUntil

    local grid = getActiveGrid(handler)
    local canSort, reason = false, "unavailable"
    if grid then
        canSort, reason = GridAutoSort.canSort(grid)
    end

    setButtonEnabled(button, canSort)

    if stickyReason and stickyUntil and now < stickyUntil then
        button:setTooltip(tooltipForReason(stickyReason))
    else
        handler._lccStatusReason = nil
        handler._lccStatusUntil = nil
        button:setTooltip(canSort
            and text("UI_LCC_GridSort_Tooltip", "Auto-sort this container.")
            or tooltipForReason(reason))
    end
end

local function handlerShouldBeVisible(handler)
    local container = handler and handler.container
    -- Floor uses the separate vanilla FloorHandlerList. GridAutoSort intentionally
    -- does not support floor grids, so never register a fake floor control.
    if not container or isFloor(container) then return false end
    refreshHandlerState(handler)
    return true
end

local function handlerGetControl(handler)
    local button = handler:getButtonControl("")
    normalizeSortButton(button)
    refreshHandlerState(handler)
    return button
end

local function handlerPerform(handler)
    local grid = getActiveGrid(handler)
    if not grid then
        handler._lccStatusReason = "unavailable"
        handler._lccStatusUntil = (getTimeInMillis and getTimeInMillis() or 0) + 2500
        refreshHandlerState(handler)
        return
    end

    local ok, reason = GridAutoSort.sort(grid)
    if ok then
        handler._lccStatusReason = nil
        handler._lccStatusUntil = nil
        if reason == "sorted" then
            print("[LCC GridSort] sorted " .. tostring(grid.inventoryContainer:getType()))
        end
    else
        handler._lccStatusReason = reason
        handler._lccStatusUntil = (getTimeInMillis and getTimeInMillis() or 0) + 2500
        print("[LCC GridSort] sort skipped: " .. tostring(reason))
    end
    refreshHandlerState(handler)
end

LCC_InventorySortHandler = ISInventoryWindowControlHandler:derive("LCC_InventorySortHandler")

function LCC_InventorySortHandler:shouldBeVisible()
    return handlerShouldBeVisible(self)
end

function LCC_InventorySortHandler:getControl()
    return handlerGetControl(self)
end

function LCC_InventorySortHandler:perform()
    handlerPerform(self)
end

function LCC_InventorySortHandler:new()
    return ISInventoryWindowControlHandler.new(self)
end

LCC_LootSortHandler = ISLootWindowObjectControlHandler:derive("LCC_LootSortHandler")

function LCC_LootSortHandler:shouldBeVisible()
    return handlerShouldBeVisible(self)
end

function LCC_LootSortHandler:getControl()
    return handlerGetControl(self)
end

function LCC_LootSortHandler:perform()
    handlerPerform(self)
end

function LCC_LootSortHandler:new()
    return ISLootWindowObjectControlHandler.new(self)
end

local function moveLootHandlerBeforeRightGroup(handlerClass)
    local list = ISLootWindowContainerControls_HandlerList
    if not list then return end

    local currentIndex = nil
    local firstRight = nil
    for i, class in ipairs(list) do
        if class == handlerClass then currentIndex = i end
        if class ~= handlerClass and firstRight == nil and class.displayToRight then
            firstRight = i
        end
    end

    if currentIndex and firstRight and currentIndex > firstRight then
        table.remove(list, currentIndex)
        table.insert(list, firstRight, handlerClass)
    end
end

-- This is the supported Build 42 extension point. GridInventory's optimized
-- arrange() implementation also iterates these same handler lists, so the sort
-- control survives every footer rebuild naturally instead of being injected
-- back into controls[] after the fact.
ISInventoryWindowContainerControls.AddHandler(LCC_InventorySortHandler)
ISLootWindowContainerControls.AddHandler(LCC_LootSortHandler)
moveLootHandlerBeforeRightGroup(LCC_LootSortHandler)

print("[LCC GridSort] native footer handlers registered")
