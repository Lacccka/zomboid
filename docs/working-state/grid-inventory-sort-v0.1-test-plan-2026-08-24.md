# GridInventory Auto Sort v0.1 — implementation and smoke-test plan

Date: 2026-08-24
Branch: `agent/b42-20-compatibility-patch`
Addon: `LaccckaB4220GridInventorySort`
Upstream target: `GridInventory` (`3782313362`), Build 42.20

## Scope implemented

The addon adds a compact sort icon to GridInventory's existing active-container footer. Hover text is localized; the button itself has no text label. The addon does not replace GridInventory source files.

### Native footer integration

The current implementation uses the supported Build 42 container-control handler API instead of injecting a child button after `arrange()`:

- player inventory: `ISInventoryWindowControlHandler` + `ISInventoryWindowContainerControls.AddHandler(...)`;
- loot/world containers: `ISLootWindowObjectControlHandler` + `ISLootWindowContainerControls.AddHandler(...)`;
- floor: no sort handler is registered because floor sorting is intentionally unsupported in v0.1.

This is required because vanilla `arrange()` removes every current footer child, wipes `controls[]`, and reconstructs the footer exclusively from registered handlers. GridInventory's optimized `arrange()` uses the same handler lists. Therefore the sort icon now participates in normal footer rebuilds instead of being repaired after them.

The obsolete `zzzz_LCC_GridInventorySortVisibility.lua`, `arrange()` wrapper, `ISInventoryPage.update` wrapper, and manual `controls[]` repair path have been removed.

The sorter:

- reads the already-rendered GridInventory model, including `unpositioned`/overflow items, without forcing a pre-sort refresh;
- computes item footprints with upstream `ItemFootprint`;
- uses upstream stack metadata from `GridContainer.getStackInfo`;
- attempts compatible virtual stacks before consuming empty cells;
- tries several deterministic first-fit-decreasing orders;
- tries both natural and rotated orientations for non-square items;
- chooses the successful layout with the smallest used height, then the fewest holes, then fewer rotations;
- persists the chosen positions as manual grid positions;
- sends one existing GridInventory `REQUEST_REORDER` batch in MP;
- marks the container dirty locally so SP and the current client redraw immediately.

## Safety gates in v0.1

The button is disabled while:

- a global GridInventory drag is active;
- a vanilla mouse drag is active;
- any item in the target container is `GridInventory_InTransit`;
- the target grid has ghost/pending items;
- a Tarkov-style search is still required;
- a nested container is locked by GridInventory's bag-drop rules.

Auto-sort is intentionally disabled for:

- floor grids — their layout is virtual/deterministic and may span multiple floor grids;
- corpse inventories — GridInventory includes worn corpse items that are not normal children of the ItemContainer, while the current authoritative reorder resolver is built around normal container trees;
- a bag that is itself inside another bag while connected to multiplayer/dedicated server — current GridInventory has a reported `GridContainer.buildOccupancy` exception path around nested bags with contents, and auto-sort uses the same authoritative reorder path. The same nested layout remains allowed in SP.

These are fail-closed restrictions for the first smoke test, not architectural limitations.

## Static checks

Run from repository root:

```bash
python3 tools/audit_grid_inventory_sort.py
bash -n WorkshopPatches/audit_split.sh
bash WorkshopPatches/audit_split.sh
```

Expected addon-specific result:

```text
GridInventorySort static contract audit: OK
```

The addon audit checks package metadata, EN/RU JSON translations, native Build 42 handler registration, the MP nested-bag guard, source-clean packaging, and explicitly rejects the removed footer-repair architecture.

## Offline Lua checks performed

Both active addon Lua chunks parse successfully with `loadfile()` in an available Lua VM.

The sorter harness covers:

- deterministic mixed-footprint packing;
- a case where rotation is required to fit;
- compatible-stack consolidation with a hard stack limit;
- genuine `no-space` fail-closed behavior;
- exactly one batch reorder + one dirty mark for a successful sort;
- busy/active-drag gate.

A separate native-footer lifecycle harness covers the regression reported during runtime testing:

- inventory footer rebuild with ordinary handlers hidden -> visible -> hidden;
- loot footer rebuild with ordinary handlers hidden -> visible -> hidden;
- sort handler remains part of every rebuild;
- loot sort handler stays before the right-side object-action group;
- floor receives no sort handler;
- handler click reaches `GridAutoSort.sort()`.

Expected harness marker:

```text
NATIVE HANDLER REBUILD HARNESS PASSED
```

These checks validate the addon logic but do not replace a Project Zomboid/Kahlua runtime test.

## Runtime smoke matrix

### 1. UI / native handler lifecycle

1. Enable upstream `GridInventory`.
2. Enable local `LaccckaB4220GridInventorySort` after it.
3. Open player inventory and an ordinary world loot container such as a crate/cabinet/trunk.
4. Confirm one compact sort icon is present in each supported active-container footer.
5. Add/remove items until the vanilla/GridInventory footer changes between zero, one, two and three ordinary action buttons.
6. Confirm the sort icon survives every rebuild and remains in the normal left-side button group.
7. Switch bags/containers repeatedly and close/reopen both panels; no duplicate sort icon may appear.
8. Confirm the floor does **not** show a sort icon in v0.1.
9. Confirm console contains:

```text
[LCC GridSort] native footer handlers registered
```

The following obsolete markers must no longer appear:

```text
[LCC GridSort] footer hooks installed
[LCC GridSort] footer visibility hook installed
[LCC GridSort] sort button created
[LCC GridSort] footer membership repaired
```

If obsolete markers appear after running the updater, the local mod directory was not replaced with the current package.

### 2. Basic deterministic packing

Prepare a supported container with mixed footprints (1x1, 1x2, 2x2, 2x3, 3x1 or larger).

1. Manually scatter the items.
2. Press the sort icon.
3. Confirm all items remain in the same physical ItemContainer.
4. Confirm the layout compacts toward the top-left and may rotate non-square items.
5. Press sort again; the second press must be a no-op visually.
6. Close/reopen the container; positions must persist.

### 3. Virtual stacks

Use stack-compatible items such as loose ammunition/nails/rags according to the active GridInventory rules.

1. Create two or more partial compatible virtual stacks.
2. Press sort.
3. Confirm the sorter prefers filling compatible stacks up to their configured limit before allocating new cells.
4. Confirm no units/items disappear and stack badges remain correct after reopen.

### 4. Overflow recovery

1. Create a fragmented layout that pushes at least one item into GridInventory overflow even though a compact arrangement can fit all items in the primary grid.
2. Press sort on the active container.
3. Confirm the overflow item returns to the main grid and the overflow UI disappears/rebuilds correctly.

If the complete set genuinely cannot fit into the primary grid, sort must make no changes. The footer keeps a short explanatory tooltip visible instead of silently failing.

### 5. Active-action guards

For each case below, try to press sort while the state is active:

- dragging an item;
- transfer timed action in progress;
- reorder ghost pending;
- unsearched world container;
- nested bag locked by GridInventory.

Expected: button disabled (or action skipped fail-closed), no item movement, no Lua exception.

### 6. Dedicated MP authority

Use one dedicated server and at least two clients observing the same world container.

1. Client A scatters items, then presses sort.
2. Client B must converge to exactly the same positions after server broadcasts.
3. Close/reopen on both clients; layout must remain identical.
4. Repeat with stack-compatible items and with an arrangement requiring rotations.
5. While client B is moving an item in the same container, verify the safety gate prevents an unsafe concurrent local sort; if a true network race is forced, the server must reject the batch all-or-nothing rather than partially applying it.

Watch for:

- `REQUEST_REORDER` validation errors;
- `GridContainer.buildOccupancy` exceptions;
- items snapping back after the server echo;
- duplicate/lost virtual stack members;
- stale overflow panels.

### 7. Nested-bag regression guard

Dedicated MP:

1. Put bag B inside bag A and put items inside bag B.
2. Open bag B's grid.
3. Confirm auto-sort is disabled for bag B and its tooltip explains the MP nested-bag restriction.
4. Confirm bag A itself and a normal direct bag remain sortable.

SP control case:

1. Repeat the same nested bag setup in SP.
2. Confirm bag B remains sortable.

### 8. Explicit v0.1 exclusions

Confirm the sort action is unavailable for:

- floor;
- corpse inventory;
- nested-bag target in MP only.

Do not treat those disabled/absent cases as test failures for v0.1.

## Acceptance criteria

The first version is acceptable for wider testing when:

- the native sort handler survives all footer rebuild transitions;
- no Lua exceptions occur across the matrix;
- the same input set produces a stable layout on repeated sorts;
- no physical item transfer is caused by sorting;
- no item or stack member is lost/duplicated;
- dedicated MP clients converge to the server-authoritative positions;
- overflow can be eliminated when all items genuinely fit;
- all busy/search/locked/nested-MP gates fail closed.
