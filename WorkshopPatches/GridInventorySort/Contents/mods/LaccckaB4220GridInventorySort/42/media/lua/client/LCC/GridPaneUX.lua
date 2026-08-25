require "ISUI/ISInventoryPane"
require "ISUI/ISInventoryPage"

local GridPaneUXOptions = require("LCC/GridPaneUXOptions")

LCC_GridPaneUX = LCC_GridPaneUX or {}
local GridPaneUX = LCC_GridPaneUX

local PIXELS_PER_SPEED_STEP = 9

local function clampScroll(pane, value, scrollHeight)
    local viewHeight = pane.getHeight and pane:getHeight() or pane.height or 0
    local maxScroll = math.max(0, (scrollHeight or 0) - viewHeight)
    if value > 0 then return 0 end
    if value < -maxScroll then return -maxScroll end
    return value
end

local function scrollPane(pane, del)
    local page = pane and pane.inventoryPage
    if not pane or (page and page.isCollapsed) then return false end

    local reportedHeight = pane.getScrollHeight and pane:getScrollHeight() or 0
    local stableHeight = pane.myFinalHeight or 0
    local scrollHeight = math.max(reportedHeight, stableHeight)
    local current = pane.getYScroll and pane:getYScroll() or 0
    local speed = GridPaneUXOptions.getScrollSpeed()
    local target = current - (del * PIXELS_PER_SPEED_STEP * speed)

    -- GridInventory keeps the vanilla, hidden item list alive. Its smooth-scroll
    -- rows do not line up with GridRender cells, so move the real pane directly.
    pane.smoothScrollTargetY = nil
    pane.smoothScrollY = nil
    pane:setYScroll(clampScroll(pane, target, scrollHeight))
    return true
end

local function isVisiblePage(page)
    if not page or page.isCollapsed then return false end
    if not page.getIsVisible or not page:getIsVisible() then return false end
    return page.backpacks and #page.backpacks > 1
end

local function isMouseOverPage(page)
    if not isVisiblePage(page) or not page.isMouseOver then return false end
    local ok, result = pcall(function() return page:isMouseOver() end)
    return ok and result == true
end

local function chooseContainerPage()
    local fallbackLoot = nil
    local fallbackInventory = nil
    local playerCount = getNumActivePlayers and getNumActivePlayers() or 1

    for playerNum = 0, playerCount - 1 do
        local inventory = getPlayerInventory and getPlayerInventory(playerNum) or nil
        local loot = getPlayerLoot and getPlayerLoot(playerNum) or nil

        if isMouseOverPage(inventory) then return inventory end
        if isMouseOverPage(loot) then return loot end
        if not fallbackLoot and isVisiblePage(loot) then fallbackLoot = loot end
        if not fallbackInventory and isVisiblePage(inventory) then fallbackInventory = inventory end
    end

    -- With both windows open and the cursor outside them, loot is the more
    -- useful fallback; hovering either window always takes precedence.
    return fallbackLoot or fallbackInventory
end

local function onKeyPressed(key)
    local previousKey = GridPaneUXOptions.getPreviousContainerKey()
    local nextKey = GridPaneUXOptions.getNextContainerKey()
    local direction = nil

    if previousKey > 0 and key == previousKey then
        direction = -1
    elseif nextKey > 0 and key == nextKey then
        direction = 1
    end
    if not direction then return end

    local page = chooseContainerPage()
    if not page then return end

    if direction < 0 and page.selectPrevContainer then
        page:selectPrevContainer()
    elseif direction > 0 and page.selectNextContainer then
        page:selectNextContainer()
    end
end

local function installPaneWheel()
    if ISInventoryPane.onMouseWheel == GridPaneUX.paneWheelWrapper then return end

    local wrapper = function(self, del)
        return scrollPane(self, del)
    end
    GridPaneUX.paneWheelWrapper = wrapper
    ISInventoryPane.onMouseWheel = wrapper
end

local function installPageWheel()
    if ISInventoryPage.onMouseWheel == GridPaneUX.pageWheelWrapper then return end

    local wrapper = function(self, del)
        if self.isCollapsed then return false end
        if self.inventoryPane then
            return scrollPane(self.inventoryPane, del)
        end
        return false
    end
    GridPaneUX.pageWheelWrapper = wrapper
    ISInventoryPage.onMouseWheel = wrapper
end

local function installStableRefresh()
    if ISInventoryPane.refreshContainer == GridPaneUX.refreshWrapper then return end

    local downstream = ISInventoryPane.refreshContainer
    local wrapper = function(self, ...)
        local stableHeight = self.myFinalHeight or 0
        local savedY = self.getYScroll and self:getYScroll() or 0
        local result = downstream(self, ...)

        -- Vanilla refreshContainer() briefly installs the height of its hidden
        -- list and clamps YScroll. GridInventory restores the grid height only
        -- in prerender(), which creates a visible one-frame jump while items move.
        if stableHeight > 0 and self.gridContainerUis then
            self:setScrollHeight(stableHeight)
            self:setYScroll(clampScroll(self, savedY, stableHeight))
        end
        return result
    end
    GridPaneUX.refreshWrapper = wrapper
    ISInventoryPane.refreshContainer = wrapper
end

function GridPaneUX.install()
    installPaneWheel()
    installPageWheel()
    installStableRefresh()
    GridPaneUX._lccGridSortPaneUXInstalled = true
end

if not GridPaneUX.eventsRegistered then
    GridPaneUX.eventsRegistered = true
    Events.OnGameBoot.Add(GridPaneUX.install)
    Events.OnGameStart.Add(GridPaneUX.install)
    Events.OnKeyPressed.Add(onKeyPressed)
end

-- Supports reloading the addon in an already-running world. During normal boot
-- the deferred hooks run after GridInventory has installed its own hijacks.
if getPlayer and getPlayer() then
    GridPaneUX.install()
end

print("[LCC GridSort] stable wheel scrolling and container keybinds registered")
return GridPaneUX
