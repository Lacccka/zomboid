# GridInventorySort v0.4 — continuous-grid redesign

Date: 2026-08-25  
Branch: `agent/b42-20-compatibility-patch`  
Addon: `LaccckaB4220GridInventorySort`  
Target: GridInventory `3782313362`, Project Zomboid Build 42.20.x

## Why the v0.3 pager was removed

The dedicated/client runtime captured on 2026-08-25 disproved the pager approach.

Observed client state:

- the same physical Nomad bag was still represented by several independent-looking panels;
- page hiding/re-selection did not produce a robust one-container UX;
- `console.txt` emitted **636** `SpriteRenderer$RingBuffer.next > Buffer overrun` errors;
- the first recorded overrun was at frame `498`, time `50,774,165`;
- the last was at frame `848`, time `50,864,735`;
- that is only 350 frames over ~90.57 seconds, roughly **3.9 FPS** during the affected interval;
- there were **no** `[LCC GridSort] slow solve` diagnostics in the run, so the visible stalls were not explained by the bounded sort solver;
- one sort was rejected with `membership`, showing that the client UI-model item set could still be one replication step away from the physical server container.

The supplied ZGC log also contains long allocation-driven collections during the bad run, including a ~4.26 s minor collection and a ~8.08 s major collection. This is consistent with the severe allocation/render pressure visible in the client log.

Conclusion: maintaining hidden `GridRender` instances/pages and rebuilding pane membership is too invasive for GridInventory's existing UI lifecycle. The pagination design is removed, not patched further.

## External design check

The replacement follows the simpler behavior used by mature grid inventories:

- Escape from Tarkov treats inventories/stashes as rectangular grids and rotates items inside those grids; a container is not presented as a set of UI pages.
- Inventory Tetris explicitly recommends a **single grid for regular containers**, and its long-standing compatibility escape hatch is to increase grid size (`Bonus Grid Size`) when geometric cells run out before the desired carrying capacity.
- Current GridInventory discussion also treats bag/container dimensions as configurable and mentions future sub-grids, but does not require page-style UI for normal containers.

For this compatibility addon, the least invasive solution is therefore one physical `ItemContainer` → one `GridCore` → one `GridRender`.

## v0.4 architecture

### 1. One adaptive continuous grid

New shared module:

`media/lua/shared/LCC/GridContinuousGrid.lua`

Rules:

- floor remains upstream GridInventory behavior;
- player root inventory keeps its upstream fixed dimensions so AutoDrop/worn-bag routing is not defeated;
- normal bag/world containers keep their original width;
- only height grows when the direct physical contents require more geometric area;
- growth uses stack-aware footprint area;
- manual placements contribute their occupied bottom row so a user placement is not clipped;
- height grows in **4-row buckets** to avoid recreating a `GridCore` for every single transfer;
- hard safety ceiling: 60 rows;
- there are never hidden page grids or additional page `GridRender` objects.

Vanilla `ItemContainer` weight/capacity remains authoritative for accepting a new transfer. The adaptive grid only makes sure already-accepted contents can be represented geometrically.

### 2. Legacy page migration

v0.2/v0.3 wrote page-local state into item ModData.

On a grid-size query, v0.4 removes `gridPage` from the container's direct items. If an item came from page > 1, its old page-local x/y/rotation/manual state is cleared and the normal deterministic GridInventory refresh places it in the continuous grid.

Migration also runs for player-root containers even though they are not expanded. This prevents abandoned page coordinates surviving only on the dedicated server and later colliding with ordinary `GridServerNetwork` validation.

### 3. Simpler auto-sort

`GridAutoSort.lua` no longer contains a page packer or profile matrix.

- descriptors come directly from `GridSortState.collectItems(container)`, the same physical-container filter used by the server;
- one deterministic large-item-first greedy pass is used;
- for <= 50 items, one second long-item-first ordering may be compared;
- no beam search;
- no page search;
- no cloned multi-page states;
- the entire solve remains synchronous but deliberately bounded and simple.

This also addresses the earlier `membership` rejection source: client sorting no longer derives its item set from transient UI grids/unpositioned lists.

### 4. Server-issued CAS retained, page protocol removed

The good part of v0.3 is kept: the client never invents its own authoritative revision.

New flow:

1. client computes the complete layout from the physical `ItemContainer`;
2. client sends `SortPrepare(requestId, containerRef, itemIds)`;
3. server compares `itemIds` with its current direct contents;
4. if membership already differs, the server immediately returns the authoritative snapshot with `membership-before-sort` and does **not** issue a token;
5. otherwise server sends a token derived from its current authoritative membership/manual layout;
6. client sends the complete `SortRequest` with that token;
7. server validates membership, bounds and collisions in one `GridCore`;
8. server rechecks the token before writing anything;
9. the full layout commits atomically or the full request is rejected/resynced.

Removed protocol/features:

- `PAGE_ASSIGN`
- `PAGE_MOVE`
- `PAGE_REORDER`
- `PAGE_CLEAR`
- page-aware wrappers around upstream `GridClientNetwork.sendItemMove`
- page-aware wrappers around upstream reorder/clear
- server pending retry queue used for page routing

Ordinary item move/reorder again uses the original GridInventory network implementation.

### 5. Dedicated nested-container occupancy guard

The current GridInventory Workshop comments contain a dedicated-server report for moving a nested bag/container with contents: `GridContainer.lua:405` / `buildOccupancy` can hit `attempted index: getSize of non-table: null`, and the reporter notes that it can wedge the network command loop. The upstream author acknowledged the report.

The attached 2026-08-25 pager-failure logs did **not** reproduce this exception, so v0.4 does not claim a runtime-confirmed fix yet. It does, however, install a narrow server-side replacement for `GridContainer.buildOccupancy` using the already-loaded `ItemFootprint`, `GridSortState.isGridItem()` and upstream `GridContainer.getStackInfo()` contracts. No upstream source file is copied or replaced wholesale.

Expected server marker:

```text
[LCC GridSort] server safe occupancy installed
```

This path needs a dedicated nested-bag transfer test before it can be moved from preventive hardening to confirmed fix.

## Deleted files

The following files must stay deleted:

- `client/LCC/GridMultiPage.lua`
- `client/LCC/GridPageView.lua`

The static contract audit now fails if either returns.

## Expected startup markers for v0.4

Client:

```text
[LCC GridSort] adaptive continuous grid installed
[LCC GridSort] unified inventory-pane scrolling installed
[LCC GridSort] simple token/CAS network installed
[LCC GridSort] native footer handlers registered
```

Dedicated server:

```text
[LCC GridSort] adaptive continuous grid installed
[LCC GridSort] server safe occupancy installed
[LCC GridSort] server simple token/CAS authority installed
```

There must be no startup marker containing `multi-page`, `single-panel multi-page`, or `page-aware MP network`.

## Runtime test plan

1. Fully restart dedicated server and client after updater sync. Do not hot-reload this migration.
2. Confirm the v0.4 startup markers above and confirm all v0.3 page markers are absent.
3. Open the same Nomad test case (`58.85 / 47` from the failed run).
   - exactly one Nomad panel/grid;
   - no page arrows/page number;
   - the grid may be taller than before and should use the pane's normal vertical scrolling.
4. Keep the inventory open and interact for 1–2 minutes.
   - zero new `SpriteRenderer$RingBuffer.next > Buffer overrun` spam is the target;
   - no severe FPS collapse.
5. Press SORT repeatedly on stable contents.
   - first click may commit;
   - subsequent clicks should be already-sorted/no-op;
   - no `membership` spam;
   - no visible freeze;
   - capture any `slow solve` diagnostic if one appears.
6. Remove and add several items in succession.
   - grid height should change only in coarse 4-row buckets, not continuously flicker every transfer;
   - existing items must not duplicate or disappear.
7. Near-full worn bag test.
   - if vanilla `hasRoomFor()` accepts the next item, adaptive spare rows should normally leave geometric room for it;
   - root player inventory must **not** become a huge grid or absorb everything itself.
8. Ordinary same-grid drag/reorder and transfer in/out of the bag.
   - these now use upstream GridInventory networking again;
   - no `gridPage` state should reappear.
9. Nested-bag dedicated test.
   - put several items inside bag B;
   - put bag B inside bag A or another supported parent container;
   - move bag B while it still contains items;
   - there must be no `GridContainer.lua:405`, `buildOccupancy`, or `getSize of non-table: null` exception;
   - server commands/restart/update control must remain responsive after the transfer.
10. Two-client same-container SORT.
   - first writer wins;
   - a genuinely concurrent second request may get one `stale` and resync;
   - both clients converge to one identical continuous layout.
11. If a freeze remains, collect `console.txt` + debug log + `gc.log` again. With page UI removed, remaining render or allocation pressure can then be isolated independently.
