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
    search = { "UI_LCC_GridSort_SearchFirst", "Search this container before sorting." },
    busy = { "UI_LCC_GridSort_Busy", "Wait for the current inventory action to finish." },
    locked = { "UI_LCC_GridSort_Locked", "This nested container is currently locked." },
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

-- Local copy of GridInventory's 1px footer flow, with one addition: our SORT
-- control is kept in the left group and ordered first. The upstream function is
-- local to ISInventoryPage_Hijack.lua, so an addon cannot call it directly.
local function compactControls(controlsUI, sortButton)
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

    if rightStart <= #controls then
        local firstRight = controls[rightStart]
        local rightEdge = uiW
        -- Keep the right group right-anchored first, then pack it leftward.
        for i = #controls, rightStart, -1 do
            local c = controls[i]
            c:setX(rightEdge - c:getWidth())
            rightEdge = c:getX() - 1
        end

        local lastLeft = nil
        for i = rightStart - 1, 1, -1 do
            local c = controls[i]
            if c:getY() == firstRight:getY() then
                lastLeft = c
                break
            end
        end
        if lastLeft and lastLeft:getRight() + 1 < firstRight:getX() then
            -- Preserve the upstream compact look without destroying right anchoring.
            -- Only close the gap when the groups actually have room to meet.
            firstRight:setX(lastLeft:getRight() + 1)
        end
    end
end

local function onSortClicked(controlsUI)
    local grid = getActiveGrid(controlsUI)
    if not grid then return end

    local ok, reason = GridAutoSort.sort(grid)
    if ok then
        if reason == "sorted" then
            print("[LCC GridSort] sorted " .. tostring(grid.inventoryContainer:getType()))
        end
        return
    end

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
    else
        button:setWidth(buttonW)
        button:setHeight(buttonH)
        button:setTitle(label)
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
    button:setTooltip(canSort
        and text("UI_LCC_GridSort_Tooltip", "Auto-sort this container.")
        or tooltipForReason(reason))
    button:setVisible(true)

    compactControls(controlsUI, button)
end

local function wrapArrange(classTable, flagName)
    if not classTable or classTable[flagName] then return false end
    classTable[flagName] = true
    local original = classTable.arrange
    if not original then return false end

    function classTable:arrange()
        original(self)
        ensureSortControl(self)
    end
    return true
end

local function installHooks()
    local installed = false
    if ISInventoryWindowContainerControls then
        installed = wrapArrange(
            ISInventoryWindowContainerControls,
            "_lccGridSortArrangeWrapped"
        ) or installed
    end
    if ISLootWindowContainerControls then
        installed = wrapArrange(
            ISLootWindowContainerControls,
            "_lccGridSortArrangeWrapped"
        ) or installed
    end
    if installed then
        print("[LCC GridSort] footer hooks installed")
    end
end

installHooks()

-- Defensive retry for load-order changes in future GridInventory builds.
if Events and Events.OnGameStart then
    Events.OnGameStart.Add(installHooks)
end
