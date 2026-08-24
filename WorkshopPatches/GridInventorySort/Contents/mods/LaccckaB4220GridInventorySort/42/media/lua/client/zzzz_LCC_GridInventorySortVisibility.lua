require "ISUI/ISInventoryPage"

-- GridInventory moves the vanilla controlsUI into the active GridRender footer.
-- When our SORT button is the only control, vanilla can leave controlsUI with
-- height 0. The button then exists (and even participates in the controls list)
-- but is clipped by its zero-height parent. Keep the parent at least as tall as
-- the injected button, after GridInventory has finished its page update.

local function hasActiveGrid(page)
    local pane = page and page.inventoryPane
    if not pane or not pane.inventory or not pane.gridContainerUis then return false end
    for _, grid in ipairs(pane.gridContainerUis) do
        if not grid.isOverflow and grid.inventoryContainer == pane.inventory then
            return true
        end
    end
    return false
end

local function usingJoypad(page)
    return page and JoypadState and JoypadState.players
        and JoypadState.players[(page.player or 0) + 1] ~= nil
end

local function ensureGridSortFooter(page)
    local controlsUI = page and page.controlsUI
    local button = controlsUI and controlsUI._lccGridSortButton
    if not button then return end

    local buttonH = button.getHeight and button:getHeight() or button.height or 0
    local footerH = controlsUI.getHeight and controlsUI:getHeight() or controlsUI.height or 0
    if buttonH > 0 and footerH < buttonH then
        controlsUI:setHeight(buttonH)
        footerH = buttonH
    end

    if button.parent ~= controlsUI then
        controlsUI:addChild(button)
    end

    local active = hasActiveGrid(page)
    local joypad = usingJoypad(page)
    if active and not joypad then
        button:setVisible(true)
        controlsUI:setVisible(true)
    end

    if not controlsUI._lccGridSortVisibilityLogged then
        controlsUI._lccGridSortVisibilityLogged = true
        local bw = button.getWidth and button:getWidth() or button.width or -1
        local bx = button.getX and button:getX() or button.x or -1
        local by = button.getY and button:getY() or button.y or -1
        local cw = controlsUI.getWidth and controlsUI:getWidth() or controlsUI.width or -1
        local ch = controlsUI.getHeight and controlsUI:getHeight() or controlsUI.height or -1
        print(string.format(
            "[LCC GridSort] footer visibility active=%s joypad=%s button=(%s,%s %sx%s) controls=(%sx%s)",
            tostring(active), tostring(joypad), tostring(bx), tostring(by), tostring(bw), tostring(buttonH), tostring(cw), tostring(ch)
        ))
    end
end

local function wrapPageUpdate()
    if not ISInventoryPage or not ISInventoryPage.update then return false end

    local current = ISInventoryPage.update
    local installed = ISInventoryPage._lccGridSortVisibilityUpdateWrapper
    if installed and current == installed then return false end

    local wrapper
    wrapper = function(self)
        current(self)
        ensureGridSortFooter(self)
    end

    ISInventoryPage.update = wrapper
    ISInventoryPage._lccGridSortVisibilityUpdateWrapper = wrapper
    return true
end

if wrapPageUpdate() then
    print("[LCC GridSort] footer visibility hook installed")
end

if Events and Events.OnGameStart then
    Events.OnGameStart.Add(function()
        if wrapPageUpdate() then
            print("[LCC GridSort] footer visibility hook reinstalled")
        end
    end)
end
