# Grid Inventory Sort v0.6.1 — stable grid rows

Date: 2026-08-25
Branch: `agent/b42-20-compatibility-patch`

## Symptom

While items are transferred, grids in a lower flexbox row briefly move upward,
overlap the row above, and then return to their normal position. The movement is
equal to the active grid's controls footer height.

The supplied client log confirms that v0.6.0 loaded and contains no Lua
exception from GridInventory or Grid Inventory Sort.

## Root cause

GridInventory uses two separate phases for grid height:

1. `ISInventoryPane:refreshContainer()` assigns every reused `GridRender` its
   pure `baseGridHeight`.
2. `ISInventoryPage:update()` later adds the controls footer height back to the
   active grid.

Item transfers trigger refresh repeatedly. The flexbox can therefore observe
the active grid between those phases, calculate the next row from the shorter
height, and move the lower grids upward for one frame.

The v0.6 scroll-height fix preserved pane `YScroll`, but it did not preserve the
individual `GridRender` outer heights, so it could not fix this row movement.

## Fix

The final `refreshContainer()` wrapper now snapshots each non-overflow grid's
outer and base heights before the upstream refresh. Immediately afterward it:

- finds the current active grid;
- calculates its footer reserve from `controlsUI`;
- retains the previous reserve if the controls are themselves between refresh
  phases;
- restores every non-active grid to its base height;
- restores the active grid to `baseGridHeight + footerHeight` before flexbox
  layout can consume the transient shorter value.

Overflow grids are intentionally excluded because their height can legitimately
change when their membership changes.

## Runtime test

1. Open enough inventory and floor grids to create at least two flexbox rows.
2. Transfer individual items repeatedly between the upper grids.
3. Use Take All and Transfer All in both directions.
4. Keep the lower row visible and verify that its headers never enter the upper
   row, even for one frame.
5. Switch the active container and verify that the footer moves to the newly
   active grid without leaving an extra gap on the previous grid.
6. Repeat on a dedicated server and check for new Lua exceptions.
