# GridInventorySort v0.3 — server token CAS + single-panel pages

Date: 2026-08-25
Branch: `agent/b42-20-compatibility-patch`
Addon: `LaccckaB4220GridInventorySort`
Target: GridInventory `3782313362`, Project Zomboid Build 42.20.x

## Why v0.3 exists

The v0.2.1 dedicated test established three things:

1. Root-player multi-page rescue was correctly removed: the player no longer exploded into many tiny `... / 8` grids and worn bags again received overflow routing.
2. Bag multi-page rescue worked: a Nomad bag could keep valid vanilla-capacity contents instead of ejecting them merely because page 1 was geometrically full.
3. The remaining MP CAS still produced many false `stale` rejects because the client was manufacturing its expected authority hash from local ModData. The additional bag pages also looked like separate physical containers because upstream GridInventory FlexBox renders every `GridCore` independently.

v0.3 addresses points 3 and the page-layout UX without copying any upstream GridInventory source file.

## Server-issued sort token

MP auto-sort is now a two-phase transaction:

1. Client computes a proposed layout but does **not** claim a version.
2. Client sends `SortPrepare(requestId, containerRef)`.
3. Dedicated server resolves the real container and returns `SortToken(requestId, token)`, where `token` is computed exclusively from server state.
4. Client sends `SortRequest(requestId, expectedToken, fullLayout)`.
5. Server recomputes the token before validation and again before commit.
6. If the token changed, the entire layout is rejected and the authoritative snapshot is returned. Nothing is partially written.
7. If membership/geometry is valid and the token still matches, the full layout is committed and broadcast.

The token includes container membership and manual/server-committed positions. Automatic page routing (`gridManual=false`) is intentionally excluded so normal presentation rescue cannot manufacture a revision conflict.

This gives the intended first-writer-wins behavior for two users sorting the same container concurrently while removing false stale rejects caused by client/server auto-fit differences.

Expected startup server marker:

```text
[LCC GridSort] server token/CAS page authority installed
```

Expected client marker remains:

```text
[LCC GridSort] page-aware MP network installed
```

A normal one-client sort should no longer print `server rejected layout: stale` repeatedly. A deliberately concurrent two-client sort may still produce one `stale`; that is the expected conflict path.

## Single-panel multi-page view

Non-floor containers with multiple real grid pages are now collapsed to one visible `GridRender` at a time.

- one physical bag/container occupies one FlexBox slot;
- page selector is rendered in the normal container header as `< 1/N >`;
- clicking left/right cycles pages without recreating the ItemContainer or changing its vanilla capacity;
- hidden pages remain live GridRender/GridCore instances and are restored temporarily whenever upstream `ISInventoryPane:refreshContainer()` needs the complete UI set for reuse/stale detection;
- floor keeps the upstream multi-grid display unchanged;
- upstream `(Overflow)` title suffix is suppressed for normal page 2+ rendering.

Expected client marker:

```text
[LCC GridSort] single-panel multi-page view installed
```

## Performance contract

The synchronous beam search remains forbidden. Sorting continues to use the bounded multi-pass greedy/best-fit solver introduced in v0.2.1.

Diagnostic threshold:

```text
[LCC GridSort] slow solve: <ms>ms for <N> items
```

Any visible freeze without a corresponding slow-solve line should be treated as a UI/network refresh problem rather than the packer itself.

## Runtime test sequence

1. Fully restart dedicated server and client after updater sync.
2. Confirm all startup markers:
   - `multi-page overflow rescue installed`
   - `unified inventory-pane scrolling installed`
   - `single-panel multi-page view installed`
   - `page-aware MP network installed`
   - `native footer handlers registered`
   - server: `server token/CAS page authority installed`
3. Repeat the Nomad `47` capacity case with enough large items to require page 2+.
4. Verify the Nomad appears as **one panel**, not three separate panels, and the header shows `1/N` navigation.
5. Cycle every page and verify no item disappears/duplicates and controls stay attached to the same physical bag.
6. Press SORT several times on the same stable contents.
   - first sort should commit;
   - repeated sort should either be already-sorted or commit without false stale spam;
   - no visible UI freeze;
   - collect any `slow solve` line if present.
7. Transfer items in/out while page 2+ exists; verify page-aware movement does not collide with page-1 coordinates.
8. Two-client conflict test: both users open the same bag, press SORT nearly simultaneously.
   - one authoritative layout wins;
   - at most the stale participant is rejected/resynced;
   - both clients converge to the same pages/layout.
9. Separate later test: two clients grab the same physical item. This remains a transfer/ownership race and is intentionally not conflated with layout CAS.

## Current non-goals

- no replacement copies of upstream `GridContainer.lua`, `GridCore.lua`, `GridRender.lua`, `GridClientNetwork.lua` or `GridServerNetwork.lua`;
- no multi-page root player inventory;
- no change to vanilla `ItemContainer` capacity/`hasRoomFor()` authority;
- no floor auto-sort;
- no physical-item claim/lease protocol yet.
