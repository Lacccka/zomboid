require "ISUI/ISButton"

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
    ["no-space"] = { "UI_LCC_GridSort_NoSpace", "These items cannot all fit in the primary grid, even after auto-sort." },
    unavailable = { "UI_LCC_GridSort_Unavailable", "Auto-sort is unavailable for this grid." },
}

local function tooltipForReason(reason)
    local entry = REASON_TOOLTIP[reason]
    if entry then return text(entry[1], entry[2]) end
    return text("UI_LCC_GridSort_Tooltip", "Auto-sort this container.")
end

local function getPage(controls)
    return controls and (controls.inventoryWindow or controls.lootWindow) or nil
end

local function getActiveGrid(controls)
    local page = getPage(controls)
    local pane = page and page.inventoryPane
    if not pane or not pane.gridContainerUis then return nil end

    local container = nil
    if controls.getDisplayedContainer then
        container = controls:getDisplayedContainer()
    end
    if not container then container = pane.inventory end
    if not container then return nil end

    for _, grid in ipairs(pane.gridContainerUis) do
        if not grid.isOverflow and grid.inventoryContainer == container then
            return grid
        end
    end
    return nil
end

local function containsControl(controls, control)
    if not controls or not control then return false end
    for _, c in ipairs(controls) do
        if c == control then return true end
    end
    return false
end

local function removeControl(controlsUI, control)
    if not controlsUI or not control then return end
    local controls = controlsUI.controls or {}
    for i = #controls, 1, -1 do
        if controls[i] == control then table.remove(controls, i) end
    end
    if control.parent == controlsUI and controlsUI.removeChild then
        controlsUI:removeChild(control)
    end
    control:setVisible(false)
end

-- The upstream footer compactor is local to ISInventoryPage_Hijack.lua, so an
-- addon cannot call it. Only compact the LEFT group here; right-anchored object
-- controls stay exactly where GridInventory/vanilla placed them.
local function compactLeftControls(controlsUI, sortButton)
    local controls = controlsUI and controlsUI.controls
    local uiW = controlsUI and (controlsUI.width or 0) or 0
    if not controls or #controls < 2 or uiW <= 0 then return end

    local rightStart = #controls + 1
    for i, c in ipairs(controls) do
        if c ~= sortButton and c.getRight and c:getRight() >= uiW - 6 then
            rightStart = i
            break
        end
    end

    local rows = {}
    for i = 1, rightStart - 1 do
        local c = controls[i]
        local y = c:getY()
        local row = rows[y]
        if not row then row = {} rows[y] = row end
        table.insert(row, c)
    end

    for _, row in pairs(rows) do
        table.sort(row, function(a, b)
            if a == sortButton then return true end
            if b == sortButton then return false end
            return a:getX() < b:getX()
        end)
        row[1]:setX(0)
        local prevRight = row[1]:getRight()
        for j = 2, #row do
            row[j]:setX(prevRight + 1)
            prevRight = row[j]:getRight()
        end
    end
end

local function onSortClicked(controlsUI)
    local grid = getActiveGrid(controlsUI)
    if not grid then return end

    local ok, reason = GridAutoSort.sort(grid)
    if ok then
        controlsUI._lccGridSortStatusReason = nil
        controlsUI._lccGridSortStatusUntil = nil
        if reason == "sorted" then
            print("[LCC GridSort] sorted " .. tostring(grid.inventoryContainer:getType()))
        end
        return
    end

    -- Keep a failure explanation visible for a short time. arrange() runs very
    -- frequently and would otherwise replace the tooltip with the generic one
    -- on the next frame, making no-space/nothing feedback impossible to read.
    controlsUI._lccGridSortStatusReason = reason
    controlsUI._lccGridSortStatusUntil = getTimeInMillis() + 2500

    local button = controlsUI._lccGridSortButton
    if button then button:setTooltip(tooltipForReason(reason)) end
    print("[LCC GridSort] sort skipped: " .. tostring(reason))
end

local function ensureSortControl(controlsUI)
    if not controlsUI then return end
    controlsUI.controls = controlsUI.controls or {}

    local grid = getActiveGrid(controlsUI)
    local button = controlsUI._lccGridSortButton

    if not grid then
        if button then removeControl(controlsUI, button) end
        return
    end

    local label = text("UI_LCC_GridSort_Button", "SORT")
    local firstControl = controlsUI.controls[1]
    local buttonH = firstControl and firstControl.getHeight and firstControl:getHeight()
        or (getTextManager():getFontHeight(UIFont.Small) + 4)
    local buttonW = math.max(34, getTextManager():MeasureStringX(UIFont.Small, label) + 10)

    if not button then
        button = ISButton:new(0, 0, buttonW, buttonH, label, controlsUI, onSortClicked)
        button:initialise()
        button:instantiate()
        button.internal = "LCC_GRID_AUTO_SORT"
        controlsUI._lccGridSortButton = button
        print("[LCC GridSort] sort button created")
    else
        button:setWidth(buttonW)
        button:setHeight(buttonH)
        if button.setTitle then button:setTitle(label) end
    end

    if button.parent ~= controlsUI then controlsUI:addChild(button) end

    if not containsControl(controlsUI.controls, button) then
        -- Put SORT immediately before the first right-anchored control. This
        -- keeps Take/Transfer controls in their existing order.
        local insertAt = #controlsUI.controls + 1
        local uiW = controlsUI.width or 0
        for i, c in ipairs(controlsUI.controls) do
            if c.getRight and c:getRight() >= uiW - 6 then
                insertAt = i
                break
            end
        end
        table.insert(controlsUI.controls, insertAt, button)
    end

    local canSort, reason = GridAutoSort.canSort(grid)
    if button.setEnable then
        button:setEnable(canSort)
    else
        button.enable = canSort
    end

    local stickyReason = controlsUI._lccGridSortStatusReason
    local stickyUntil = controlsUI._lccGridSortStatusUntil
    if stickyReason and stickyUntil and getTimeInMillis() < stickyUntil then
        button:setTooltip(tooltipForReason(stickyReason))
    else
        controlsUI._lccGridSortStatusReason = nil
        controlsUI._lccGridSortStatusUntil = nil
        button:setTooltip(canSort
            and text("UI_LCC_GridSort_Tooltip", "Auto-sort this container.")
            or tooltipForReason(reason))
    end
    button:setVisible(true)

    compactLeftControls(controlsUI, button)
end

local function wrapArrange(classTable, slotName)
    if not classTable then return false end
    local original = classTable.arrange
    if not original then return false end

    -- Store the actual wrapper function, not a boolean. GridInventory replaces
    -- arrange() during its own client bootstrap. If that happens after our
    -- first pass, OnGameStart must be able to detect the replacement and wrap
    -- the new final implementation instead of incorrectly treating it as done.
    local installedWrapper = classTable[slotName]
    if installedWrapper and original == installedWrapper then return false end

    local wrapper
    wrapper = function(self)
        original(self)
        ensureSortControl(self)
    end

    classTable.arrange = wrapper
    classTable[slotName] = wrapper
    return true
end

local function installHooks()
    local installed = false
    if ISInventoryWindowContainerControls then
        installed = wrapArrange(
            ISInventoryWindowContainerControls,
            "_lccGridSortArrangeWrapper"
        ) or installed
    end
    if ISLootWindowContainerControls then
        installed = wrapArrange(
            ISLootWindowContainerControls,
            "_lccGridSortArrangeWrapper"
        ) or installed
    end
    if installed then
        print("[LCC GridSort] footer hooks installed")
    end
end

installHooks()

-- Defensive retry after every mod has finished bootstrapping. This is required
-- because GridInventory itself replaces the vanilla controls arrange() method.
if Events and Events.OnGameStart then
    Events.OnGameStart.Add(installHooks)
end
