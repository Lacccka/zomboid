require "ISUI/ISInventoryPane"

local GridPaneUX = {}

if ISInventoryPane._lccGridSortPaneUXInstalled then
    return GridPaneUX
end
ISInventoryPane._lccGridSortPaneUXInstalled = true

-- GridInventory gives the mouse wheel two meanings: over a grid it scrolls the
-- pane, while a few pixels away it cycles selected containers. A tall adaptive
-- continuous grid makes that split easy to trigger accidentally. Keep container
-- cycling only when the pane genuinely has nothing vertical to scroll.
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
