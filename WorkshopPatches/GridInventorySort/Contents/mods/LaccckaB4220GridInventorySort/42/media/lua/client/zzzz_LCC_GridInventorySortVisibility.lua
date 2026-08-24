require "ISUI/ISInventoryPage"

-- GridInventory rebuilds the vanilla controlsUI whenever its visible handler
-- signature changes. That rebuild can remove our injected SORT control from
-- controlsUI.controls while the Lua reference in _lccGridSortButton survives.
-- If we only re-add it as a child, it comes back at x=0 underneath the first
-- vanilla button. Repair both the child tree and controls[] membership after
-- GridInventory has completed its page update, then place SORT after the
-- contiguous left-side footer group.

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

local function containsControl(controls, wanted)
    if not controls or not wanted then return false end
    for _, control in ipairs(controls) do
        if control == wanted then return true end
    end
    return false
end

local function isControlVisible(control)
    if not control then return false end
    if control.getIsVisible then
        return control:getIsVisible()
    end
    if control.isVisible then
        return control:isVisible()
    end
    return control.visible ~= false
end

-- Keep SORT in the normal left-side footer flow without disturbing controls
-- that vanilla/GridInventory anchors on the right (object actions, settings,
-- etc.). We find the contiguous group that starts at x=0 and place SORT right
-- after it. A distant right-side group is intentionally ignored.
local function positionAfterLeftGroup(controlsUI, button)
    local controls = controlsUI and controlsUI.controls
    if not controls or not button then return end

    local buttonY = button.getY and button:getY() or button.y or 0
    local candidates = {}

    for _, control in ipairs(controls) do
        if control ~= button and isControlVisible(control)
            and control.getX and control.getRight and control.getY
            and math.abs(control:getY() - buttonY) <= 1 then
            table.insert(candidates, control)
        end
    end

    table.sort(candidates, function(a, b)
        return a:getX() < b:getX()
    end)

    local cursor = 0
    local foundLeft = false
    for _, control in ipairs(candidates) do
        local x = control:getX()
        -- Vanilla uses a small footer gap. Anything much farther away belongs
        -- to the right-side group and must not influence SORT placement.
        if (not foundLeft and x <= 6) or (foundLeft and x <= cursor + 6) then
            cursor = math.max(cursor, control:getRight())
            foundLeft = true
        else
            break
        end
    end

    local x = foundLeft and (cursor + 1) or 0
    local uiW = controlsUI.getWidth and controlsUI:getWidth() or controlsUI.width or 0
    local buttonW = button.getWidth and button:getWidth() or button.width or 0

    -- Defensive fallback for very narrow footers: keep the button inside the
    -- parent rather than letting it be clipped beyond the right edge.
    if uiW > 0 and buttonW > 0 and x + buttonW > uiW then
        x = math.max(0, uiW - buttonW)
    end

    if button.getX and button:getX() ~= x then
        button:setX(x)
    elseif not button.getX then
        button:setX(x)
    end
end

local function ensureGridSortFooter(page)
    local controlsUI = page and page.controlsUI
    local button = controlsUI and controlsUI._lccGridSortButton
    if not button then return end

    local repaired = false

    local buttonH = button.getHeight and button:getHeight() or button.height or 0
    local footerH = controlsUI.getHeight and controlsUI:getHeight() or controlsUI.height or 0
    if buttonH > 0 and footerH < buttonH then
        controlsUI:setHeight(buttonH)
        footerH = buttonH
        repaired = true
    end

    if button.parent ~= controlsUI then
        controlsUI:addChild(button)
        repaired = true
    end

    controlsUI.controls = controlsUI.controls or {}
    if not containsControl(controlsUI.controls, button) then
        table.insert(controlsUI.controls, button)
        repaired = true
    end

    -- GridInventory/vanilla may have moved the surviving button back to x=0
    -- during arrange(). Put it back after the normal left group every frame.
    positionAfterLeftGroup(controlsUI, button)

    local active = hasActiveGrid(page)
    local joypad = usingJoypad(page)
    if active and not joypad then
        button:setVisible(true)
        controlsUI:setVisible(true)
    end

    if repaired and not controlsUI._lccGridSortRepairLogged then
        controlsUI._lccGridSortRepairLogged = true
        print("[LCC GridSort] footer membership repaired")
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
