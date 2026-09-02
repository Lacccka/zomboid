-- Pack Flow: GridInventory tooltip hover-stability shim.
--
-- GridInventory creates ISToolTipInv as a separate top-level UI element and
-- brings it to the front. Near screen edges the vanilla tooltip can be clamped
-- back underneath the mouse cursor. If that tooltip consumes mouse events,
-- GridRender:isMouseOver() / parent:isMouseOver() can become false for one
-- frame; GridInventory then removes the tooltip, hover returns to the grid on
-- the next frame, and the tooltip is recreated. The result is visible blinking.
--
-- Keep the upstream tooltip implementation intact and only make GridInventory's
-- item tooltip mouse-transparent. This preserves click/drag/context behaviour
-- on the grid underneath and avoids copying/replacing GridRender:updateTooltip.

local okGridRender, GridRender = pcall(require, "UI/GridRender/GridRender")
if not okGridRender or not GridRender or not GridRender.updateTooltip then
    print("[LCC PackFlow] tooltip stability unavailable: GridRender API missing")
    return
end

LCC_GridTooltipStability = LCC_GridTooltipStability or {}
local TooltipStability = LCC_GridTooltipStability

local function makeTooltipMouseTransparent(grid)
    local tooltip = grid and grid.toolRender or nil
    if not tooltip then return end

    if tooltip.setConsumeMouseEvents then
        tooltip:setConsumeMouseEvents(false)
        tooltip._lccPackFlowMouseTransparent = true
    end
end

function TooltipStability.install()
    local current = GridRender.updateTooltip
    if not current or current == TooltipStability.wrapper then
        return
    end

    local downstream = current
    local wrapper = function(self, ...)
        local result = downstream(self, ...)
        makeTooltipMouseTransparent(self)
        return result
    end

    TooltipStability.downstream = downstream
    TooltipStability.wrapper = wrapper
    GridRender.updateTooltip = wrapper
end

-- Install immediately (GridInventory is a hard dependency and Pack Flow loads
-- after it), then repeat at boot/start in case another UI mod re-hooks the
-- method during Lua initialization.
TooltipStability.install()

if not TooltipStability.eventsRegistered then
    TooltipStability.eventsRegistered = true
    Events.OnGameBoot.Add(TooltipStability.install)
    Events.OnGameStart.Add(TooltipStability.install)
end

print("[LCC PackFlow] GridInventory tooltip mouse-pass-through installed")
return TooltipStability
