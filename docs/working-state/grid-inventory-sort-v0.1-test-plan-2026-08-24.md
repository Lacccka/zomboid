# GridInventory Auto Sort v0.1 — implementation and smoke-test plan

Date: 2026-08-24
Branch: `agent/b42-20-compatibility-patch`
Addon: `LaccckaB4220GridInventorySort`
Upstream target: `GridInventory` (`3782313362`), Build 42.20

## Scope implemented

The addon adds a `SORT` control to GridInventory's existing active-container footer. It does not replace GridInventory source files.

The sorter:

- reads the current GridInventory model, including `unpositioned`/overflow items;
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
- corpse inventories — GridInventory includes worn corpse items that are not normal children of the ItemContainer, while the current authoritative reorder resolver is built around normal container trees.

These are fail-closed restrictions for the first smoke test, not architectural limitations.

## Static checks

Run from repository root:

```bash
python3 tools/audit_grid_inventory_sort.py
```

Expected result:

```text
GridInventorySort static contract audit: OK
```

The audit checks package metadata, EN/RU JSON translations, the expected GridInventory integration seams, and verifies that the addon does not contain copied upstream implementation files.

`WorkshopPatches/audit_split.sh` is not a valid acceptance gate for this work at the moment: it already contains stale hard-coded package/NPCFixes metadata from before the current published NPCFixes state and also hard-codes the previous package count.

## Runtime smoke matrix

### 1. UI / load order

1. Enable upstream `GridInventory`.
2. Enable local `LaccckaB4220GridInventorySort` after it.
3. Open player inventory and a world loot container.
4. Confirm one `SORT` button appears in the active GridInventory footer and no duplicate button appears after switching bags/containers repeatedly.
5. Confirm console contains one install line from `[LCC GridSort]` and no Lua exception.

### 2. Basic deterministic packing

Prepare a container with mixed footprints (1x1, 1x2, 2x2, 2x3, 3x1 or larger).

1. Manually scatter the items.
2. Press `SORT`.
3. Confirm all items remain in the same physical ItemContainer.
4. Confirm the layout compacts toward the top-left and may rotate non-square items.
5. Press `SORT` again; the second press must be a no-op visually.
6. Close/reopen the container; positions must persist.

### 3. Virtual stacks

Use stack-compatible items such as loose ammunition/nails/rags according to the active GridInventory rules.

1. Create two or more partial compatible virtual stacks.
2. Press `SORT`.
3. Confirm the sorter prefers filling compatible stacks up to their configured limit before allocating new cells.
4. Confirm no units/items disappear and stack badges remain correct after reopen.

### 4. Overflow recovery

1. Create a fragmented layout that pushes at least one item into GridInventory overflow even though a compact arrangement can fit all items in the primary grid.
2. Press `SORT` on the active container.
3. Confirm the overflow item returns to the main grid and the overflow UI disappears/rebuilds correctly.

If the complete set genuinely cannot fit into the primary grid, `SORT` must make no changes.

### 5. Active-action guards

For each case below, try to press `SORT` while the state is active:

- dragging an item;
- transfer timed action in progress;
- reorder ghost pending;
- unsearched world container;
- nested bag locked by GridInventory.

Expected: button disabled (or action skipped fail-closed), no item movement, no Lua exception.

### 6. Dedicated MP authority

Use one dedicated server and at least two clients observing the same world container.

1. Client A scatters items, then presses `SORT`.
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

### 7. Explicit v0.1 exclusions

Confirm the button is disabled for:

- floor;
- corpse inventory.

Do not treat those two disabled cases as test failures for v0.1.

## Acceptance criteria

The first version is acceptable for wider testing when:

- no Lua exceptions occur across the matrix;
- the same input set produces a stable layout on repeated sorts;
- no physical item transfer is caused by sorting;
- no item or stack member is lost/duplicated;
- dedicated MP clients converge to the server-authoritative positions;
- overflow can be eliminated when all items genuinely fit;
- all busy/search/locked gates fail closed.
