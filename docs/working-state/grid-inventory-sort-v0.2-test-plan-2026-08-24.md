# GridInventorySort v0.2 — multi-page / packing / MP CAS runtime plan

Date: 2026-08-24
Branch: `agent/b42-20-compatibility-patch`
Addon: `LaccckaB4220GridInventorySort`
Target: GridInventory `3782313362`, Project Zomboid Build 42.20.x

## What changed

v0.2 addresses two runtime findings from the first dedicated tests:

1. GridInventory turns non-floor items that do not fit the single 2D grid into `unpositioned`, and `GridAutoDropSystem` later moves those items to another worn container or the floor even when vanilla `ItemContainer` capacity still allows them.
2. v0.1 auto-sort was deterministic and rotation-aware but still a greedy solver; screenshots showed avoidable holes.

v0.2 remains a source-clean addon. It does not publish copies of GridInventory's `GridContainer.lua`, `GridCore.lua`, `GridRender.lua`, `GridClientNetwork.lua`, or `GridServerNetwork.lua`.

## Multi-page overflow rescue

`LCC/GridMultiPage.lua` wraps the existing `GridContainer:refresh()` after upstream has done its normal work.

For non-floor containers only:

- items left in `unpositioned` are placed into additional real `GridCore` pages using their real footprints and rotation;
- the existing GridInventory UI already renders every entry in `gridContainer.grids`, so these pages use normal GridRender behavior instead of the forced-1x1 `OverflowGridRender`;
- successful rescue removes those items from `self.unpositioned`, which prevents normal valid contents from entering `GridAutoDropSystem` merely because page 1 ran out of geometric cells;
- vanilla physical capacity is not increased or bypassed by this patch; extra pages are a visualization/placement layer for items already present in the real ItemContainer;
- floor keeps the upstream GridInventory multi-grid implementation unchanged;
- hard page guard: 32 pages. Items that still cannot be represented remain `unpositioned` fail-closed.

Auto-assigned extra-page coordinates are initially deterministic client UI state and are not written as authoritative `gridPage` ModData until a manual page action or server-authoritative sort commits them. Manual/server-backed page positions are preserved across refresh.

## Sorter v2

`LCC/GridAutoSort.lua` now uses two packing stages:

### Single-page attempt

A bounded beam search replaces the old single-path first-fit greedy solver.

For every item it:

- enumerates valid coordinates across the grid;
- tries natural and rotated orientation;
- reuses GridCore collision and compatible-stack rules;
- keeps several best intermediate layouts instead of committing permanently to the first placement;
- scores layouts by used height, holes, used width and rotation count;
- runs several deterministic item orders.

The beam is intentionally bounded so large containers cannot create an exponential UI stall.

### Multi-page fallback

If the complete set cannot fit page 1 even with beam search, the sorter packs into the minimum page count found by several deterministic multi-page greedy orders. Existing pages are preferred before a new page is created.

Targets now include `page` in addition to x/y/rotation.

## Dedicated MP compare-and-swap sorting

The sort button no longer optimistically mutates item ModData before the dedicated server answers.

Shared `LCC/GridSortState.lua` calculates a deterministic hash of the current direct grid-item membership and authoritative placement state.

Client sort request contains:

- container ref;
- expected pre-sort layout hash;
- full proposed target layout including page/x/y/rotation.

Server `zzz_LCC_GridSortServer.lua` performs:

1. container resolution;
2. current-hash comparison;
3. exact item-set comparison;
4. fresh per-page GridCore validation of every proposed item;
5. second hash check immediately before commit;
6. atomic commit of the complete layout;
7. one authoritative layout broadcast.

This is first-writer-wins compare-and-swap semantics. If two clients solve the same container concurrently from hash H0:

- request A reaches the server first and commits H0 -> H1;
- request B still carries H0 and is rejected as stale;
- B receives the authoritative H1 snapshot and refreshes to A's accepted layout.

A stale/conflicting layout must never be partially applied.

## Page-aware manual MP positions

The addon wraps GridClientNetwork only when an operation touches page > 1. Page-1-only operations continue through upstream GridInventory networking.

Page-aware move/reorder commands validate occupancy on the server per page and broadcast authoritative `gridPage`, x/y and rotation.

## Offline checks performed before runtime test

The new Lua files were parsed with `texlua loadfile()` successfully.

Stub runtime harnesses passed:

- one-page beam packing;
- multi-page packing;
- MP sort sends proposal without optimistic local ModData mutation;
- SP multi-page commit;
- non-floor `unpositioned` rescue into real extra pages;
- server first-writer-wins CAS: first concurrent layout remains committed and a second request using the stale pre-sort hash is rejected.

Markers:

```text
ALL V02 HARNESS TESTS PASSED
MULTIPAGE_RESCUE_OK
SERVER_FIRST_WRITER_WINS_CAS_OK
```

These are logic harnesses, not a substitute for Kahlua/dedicated runtime testing.

## Required runtime smoke test

### A. Startup

Client should contain:

```text
[LCC GridSort] multi-page overflow rescue installed
[LCC GridSort] page-aware MP network installed
[LCC GridSort] native footer handlers registered
```

Dedicated server should contain:

```text
[LCC GridSort] server CAS/page authority installed
```

No addon Lua exception is acceptable.

### B. Backpack capacity regression

Use the same large backpack that previously started ejecting items well below its vanilla capacity.

1. Fill it with mixed large footprints using Transfer All until page 1 cannot geometrically hold the set but vanilla capacity is still below its limit.
2. Confirm a second normal GridInventory page appears.
3. Confirm items keep their real footprints; they must not become forced 1x1 overflow tiles.
4. Wait idle for several seconds.
5. Confirm GridAutoDrop does not move those valid items to another worn bag or onto the floor.
6. Continue until vanilla itself refuses further physical transfer at actual capacity.

Important: extra visual pages must not let a normal transfer exceed vanilla ItemContainer capacity.

### C. Packing quality

Recreate the screenshot case that left large holes in the ARES backpack.

1. Press sort.
2. Compare used height and obvious internal holes against v0.1.
3. Repeat sort: no visual change should occur once authoritative state has converged.
4. Test shapes where rotation is required.
5. Test compatible virtual stacks.

### D. Multi-page sort

1. Create a legal container content set that genuinely requires two pages.
2. Sort.
3. Page 1 should be filled preferentially; remainder goes to page 2.
4. Remove enough items that everything can fit page 1 and sort again.
5. Page 2 should disappear after refresh.

### E. Two-client CAS race

Both clients open the same world container.

1. Ensure both display the same initial layout.
2. Press sort on both clients as close together as possible.
3. One request may win.
4. Both clients must converge to the exact same server layout.
5. Look for client marker:

```text
[LCC GridSort] server rejected stale/conflicting layout; authoritative snapshot restored
```

Seeing this marker during an intentionally forced race is expected, not a failure.

No partial mix of layout A and layout B is acceptable.

### F. Page-2 manual movement

On dedicated MP:

1. Move an item inside page 2.
2. Close/reopen the container on both clients.
3. Position/page must persist and converge.
4. Move an item page 2 -> page 1 and page 1 -> page 2.
5. Repeat with a second client watching.

## Separate race: two users physically taking the same item

The observed case where two players start taking the same physical item and both temporarily see it locally is not the same transaction as grid sorting. The new layout CAS prevents conflicting reorder commits, but it does not claim to implement a physical loot-item lease/lock.

The fact that the second local copy disappears after authoritative redraw suggests the base game/server eventually resolves ownership, while GridInventory temporarily renders both clients' optimistic transfer state. That race should be investigated separately before adding a lock, because transfer ownership is a vanilla inventory transaction and an incorrect addon-level lock could create stuck items.

For the v0.2 test, record exact timestamps/client logs if this duplicate-pickup race is reproduced.
