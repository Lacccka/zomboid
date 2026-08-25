# Grid Inventory Sort v0.6 — stable scrolling and container navigation

Date: 2026-08-25
Branch: `agent/b42-20-compatibility-patch`

## User-facing behavior

- The mouse wheel always scrolls the inventory or loot pane. It no longer
  changes the selected container over empty pane space or the container strip.
- Scroll speed is configurable from `Options -> Mods -> Grid Inventory Sort`.
  The slider range is 1-8 and the default is 3, matching the previous hard-coded
  three-times multiplier.
- Previous-container and next-container keys are configurable in the same
  options section. Both are unbound by default to avoid conflicts with existing
  controls and other mods.
- A navigation key affects the visible window under the mouse pointer. If the
  pointer is outside both visible windows, loot is preferred, then inventory.

## Transfer flicker diagnosis and fix

GridInventory calls vanilla `ISInventoryPane.refreshContainer()` whenever item
membership changes. Vanilla refresh updates the scrollbar from its hidden list
view before GridInventory restores the real grid height in `prerender()`. While
items are transferred, that temporary height can clamp `YScroll` and visibly
move the whole grid for one frame.

`GridPaneUX` now wraps the final GridInventory refresh hook, preserves the last
stable `myFinalHeight` and `YScroll`, and restores both immediately after the
refresh. Required item remapping and multiplayer refresh events remain intact.

The hook is installed on `OnGameBoot` and `OnGameStart`, after GridInventory's
deferred hijacks, so it is not overwritten by the upstream `OnGameBoot` layer.

## Runtime test checklist

1. Restart the client after deploying the patch.
2. Open `Options -> Mods -> Grid Inventory Sort` and bind previous/next keys.
3. Verify wheel scrolling over a grid, empty pane area, and the container icon
   strip. None of those locations should switch the selected container.
4. Set scroll speed to 1 and 8 and verify that the distance per wheel step
   changes immediately after applying options.
5. Hover inventory and loot in turn and test both navigation keys. Move the
   pointer outside both panels and verify that loot receives navigation.
6. Scroll a tall pane to the middle, then transfer individual items and use
   Take All / Transfer All. The pane should keep its vertical position without
   the previous one-frame jump.
7. Repeat step 6 on a dedicated server and watch for Lua exceptions or stale
   item renders.

Expected startup marker:

```text
[LCC GridSort] stable wheel scrolling and container keybinds registered
```
