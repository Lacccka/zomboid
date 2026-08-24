require "ISUI/ISInventoryPane"

local GridPaneUX = {}

if ISInventoryPane._lccGridSortPaneUXInstalled then
    return GridPaneUX
end
ISInventoryPane._lccGridSortPaneUXInstalled = true

-- GridInventory intentionally gives the mouse wheel two unrelated meanings:
-- over a grid it scrolls the pane, while a few pixels away in the same pane it
-- cycles the selected container. Once multi-page grids exist this becomes very
-- easy to trigger accidentally and feels like two overlapping scroll regions.
-- Keep container cycling only when the pane has nothing to scroll. Whenever a
-- vertical scrollbar is actually needed, the whole pane consistently scrolls.
local originalIsMouseOverAnyGrid = ISInventoryPane.isMouseOverAnyGrid
if originalIsMouseOverAnyGrid then
    function ISInventoryPane:isMouseOverAnyGrid()
        if self._lccForcePaneScroll then return true end
        return originalIsMouseOverAnyGrid(self)
    end
end

local originalOnMouseWheel = ISInventoryPane.onMouseWheel
if originalOnMouseWheel then
    function ISInventoryPane:onMouseWheel(del)
        local page = self.inventoryPage
        local scrollHeight = self.getScrollHeight and self:getScrollHeight() or 0
        local viewHeight = self.getHeight and self:getHeight() or self.height or 0
        local scrollable = scrollHeight > viewHeight + 2

        if page and not page.isCollapsed and scrollable then
            -- The upstream GridInventory wrapper calls its saved vanilla wheel
            -- implementation when isMouseOverAnyGrid() is true. Force that path
            -- for the duration of this event instead of duplicating vanilla
            -- scroll direction/smoothing math here.
            self._lccForcePaneScroll = true
            local ok, result = pcall(originalOnMouseWheel, self, del)
            self._lccForcePaneScroll = nil
            if not ok then error(result) end
            return result
        end

        return originalOnMouseWheel(self, del)
    end
end

print("[LCC GridSort] unified inventory-pane scrolling installed")
return GridPaneUX
