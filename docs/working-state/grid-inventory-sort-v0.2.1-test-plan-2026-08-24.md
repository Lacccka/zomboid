# GridInventorySort v0.2.1 — dedicated runtime follow-up

Date: 2026-08-24
Branch: `agent/b42-20-compatibility-patch`
Addon: `LaccckaB4220GridInventorySort`
Target: GridInventory `3782313362`, Project Zomboid Build 42.20.x

## Runtime findings from 23:15 archive/screenshots

The v0.2 multi-page concept worked for bags, but the test exposed three regressions/UX issues that must be fixed before wider testing.

### 1. Root player inventory must not become multi-page

Screenshots showed many repeated small grids with the same `49.81 / 8` header. They were pages of the same root player inventory, not separate containers.

Cause: v0.2 rescued `unpositioned` for every non-floor container, including the root player inventory. Upstream GridInventory uses root-inventory `unpositioned` as a staging signal in `GridAutoDropSystem`: when idle it tries to move those items into worn bags. By converting them into real pages first, the patch suppressed that redistribution and allowed the root inventory to absorb almost everything.

v0.2.1 rule:

- root player inventory: single GridInventory page only; upstream `unpositioned`/redistribution behavior remains available;
- worn bags and ordinary world/vehicle containers: multi-page rescue remains enabled;
- floor: upstream GridInventory multi-grid behavior remains unchanged.

The sorter also refuses to create multiple pages for the root player inventory.

### 2. Beam search caused visible UI freezes

v0.2 used a bounded beam solver. Although bounded, it still enumerated many placements, cloned a full `GridCore` for each candidate and rescanned layout metrics repeatedly. On a large inventory this runs synchronously on the UI thread and produced a visible pause on every SORT press.

v0.2.1 replaces it with a deterministic multi-pass best-fit solver:

- scans all legal cells and both rotations;
- keeps one live GridCore per pass instead of cloning candidate states;
- uses height, internal holes, edge/contact packing, stacking and deterministic tie-breaks;
- tries several item orders/profiles only while a small interactive-time budget remains;
- reduces the number of passes for large item counts;
- uses a stack-aware lower-bound area test to skip impossible single-page attempts early;
- emits a `slow solve` marker only when a solve still exceeds 75 ms.

The objective remains: minimum pages, then minimum used rows, holes and unnecessary rotations.

### 3. CAS hash was too strict

The client log contains eight:

```text
[LCC GridSort] server rejected stale/conflicting layout; authoritative snapshot restored
```

with no GridSort Lua exception on either client or server.

Cause: v0.2 hashed every `gridX/gridY/gridPage`. GridInventory can auto-fit presentation coordinates locally without making them server-authoritative, so a single client could legitimately calculate a different exact layout hash from the dedicated server before pressing SORT.

v0.2.1 uses `authorityHash` instead:

- item membership is always hashed;
- coordinates/page/rotation are hashed only for `gridManual=true` items (server-backed/manual state);
- automatic local placement contributes only an `A` marker;
- first successful sort makes the whole committed layout manual, changing the authority hash;
- a concurrent second sort based on the old authority state is therefore still rejected first-writer-wins.

This preserves the intended MP compare-and-swap behavior without false conflicts from local auto-layout.

### 4. Multi-page network occupancy

Once a container has any page >1, *all* moves/reorders in that container now use the addon's page-aware server path. Previously a page-1 item could still use upstream `GridServerNetwork.buildOccupancy()`, which does not know `gridPage` and could treat page-2 coordinates as page 1.

Pending sort keys are now based on the containing item ID where available, so two bags of the same FullType cannot accidentally share one local pending lock.

### 5. Scroll UX

Upstream GridInventory intentionally uses the mouse wheel differently depending on whether the cursor is directly over a grid: over-grid scrolls the pane, while nearby empty pane space cycles containers. With many grids this creates two adjacent, easy-to-confuse scroll zones.

v0.2.1 keeps container cycling only when the pane has no vertical content to scroll. If a scrollbar is needed, the entire pane consistently scrolls regardless of whether the pointer is over a grid cell or the nearby empty pane area.

## Runtime acceptance pass

After updating both server and client, confirm startup markers:

```text
[LCC GridSort] server CAS/page authority installed
[LCC GridSort] page-aware MP network installed
[LCC GridSort] multi-page overflow rescue installed
[LCC GridSort] unified inventory-pane scrolling installed
[LCC GridSort] native footer handlers registered
```

Then test:

1. Transfer a large floor set into the character while wearing the Nomad bag.
   - The root inventory must NOT explode into many repeated 3x4 pages.
   - Excess root-grid items should again be eligible for GridInventory's worn-bag redistribution.
   - The Nomad bag may create real page 2+ while vanilla capacity still permits its contents.

2. Press SORT repeatedly on a large Nomad bag.
   - No perceptible UI freeze.
   - No `slow solve` marker ideally; if present, capture the reported milliseconds/item count.
   - Repeated SORT with unchanged contents should settle to a stable deterministic layout.

3. Watch CAS.
   - A single user sorting an otherwise idle container should not produce `server rejected layout: stale`.
   - Two clients deliberately sorting the same container at almost the same time should allow one result and reject/restore the other.

4. Move/reorder items on page 1 while page 2 exists.
   - No collision rejection caused by page-2 items sharing the same local x/y coordinates.

5. Scroll the inventory pane.
   - When the pane is taller than the viewport, wheel input anywhere in pane content should scroll vertically rather than unexpectedly switching the selected container.

## Deferred UI work

The pathological grid explosion in the screenshots was mainly the root-player multi-page regression and should disappear in v0.2.1. Extra pages of a real bag are still rendered using GridInventory's existing flex layout. If the remaining two-page bag presentation is still awkward after this pass, the next step is a dedicated page-group UI (group pages of one physical container as a single layout unit) rather than further patching the generic flexbox blindly.
