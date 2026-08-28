# Pack Flow spatial-inventory acceptance — 2026-08-25

Branch: `agent/b42-20-compatibility-patch`

Initial runtime archive: `ZomboidLogs_2026-08-25_03-51-14.zip`

Mass-consolidation follow-up archives:

- `ZomboidLogs_2026-08-29_00-13-33.zip`
- `ZomboidLogs_2026-08-29_01-15-49.zip`

Release identity:

- Workshop title: `Lacccka B42 Pack Flow`
- Workshop ID: `3789630746`
- Mod ID: `LaccckaPackFlow`
- accepted patch version: `0.7.12`
- target: Project Zomboid Build `42.20.x`
- latest acceptance server runtime: Build `42.20.4`
- required upstream mod: `GridInventory`, Workshop ID `3782313362`

## Scope and dependencies

Pack Flow is a targeted fix layer for spatial-inventory problems observed with
GridInventory on Build 42.20. It contains only integration and fix files; it
does not bundle replacement copies of GridInventory's implementation.

The patch hard-requires only `GridInventory` and loads after it. It does not
depend on `LaccckaB4220PatchCore` or any other Lacccka patch.

## Confirmed issue group

The completed patch covers the following reproduced problems:

1. normal containers could accept more items by vanilla capacity rules than
   GridInventory's original grid could represent, leaving items in a separate
   1x1 overflow area and making sorting unavailable;
2. item sorting needed a bounded rotation-aware layout and a safe multiplayer
   commit path instead of unrestricted client-side coordinate changes;
3. the mouse wheel changed containers over parts of the inventory UI instead
   of consistently scrolling the visible pane;
4. item transfers could briefly clamp the pane scroll position while the
   hidden vanilla list and the real grid reported different heights;
5. lower flexbox rows moved upward for one frame during transfers because the
   active grid temporarily lost its controls-footer height during refresh;
6. display-name sorting needed natural numeric ordering and Russian-aware
   comparison without Kahlua pattern failures on Cyrillic text;
7. scoped mass sort needed to consolidate duplicate item types across multiple
   accessible containers using real multiplayer inventory transfers rather
   than direct/teleport-style container mutation;
8. Build 42 multiplayer can complete an `ItemTransaction` quickly enough to
   make a healthy transfer look almost instantaneous, so Pack Flow must retain
   a visible transfer-action phase without replacing server authority;
9. nearby floor items were excluded from duplicate consolidation even when the
   same item type already existed in an accessible external container.

## Confirmed root causes

Grid dimensions were originally based mainly on base container capacity and
could not represent the higher effective capacity accepted for an Organized
character or capacity values modified by compatible container systems.
Membership-driven growth was rejected because it changed panel geometry while
items were being replicated or transferred.

The two visual jumps had separate causes:

- vanilla `ISInventoryPane:refreshContainer()` updated and clamped scrolling
  from its hidden list before GridInventory restored the real scroll height;
- GridInventory assigned a reused `GridRender` its pure `baseGridHeight` during
  refresh, then added the active controls footer later in
  `ISInventoryPage:update()`. The flexbox could consume the shorter intermediate
  height and move the next row upward.

Natural sorting originally passed UTF-8 Cyrillic bytes through Kahlua pattern
replacement, which could terminate with `malformed pattern (ends with '%')`.
The accepted sorter uses plain literal replacement for Russian case folding and
collation and keeps numeric chunk comparison separate.

For multiplayer inventory transfer, Build 42 sets the transfer action to wait
for the authoritative item transaction. When the transaction completes,
`ISInventoryTransferAction:forceComplete()` can end the action immediately.
Pack Flow therefore must not infer physical transfer duration from ordinary
`maxTime` alone and must not replace the authoritative transaction with direct
container mutation.

## Final implementation

### Capacity-aware single grid

Each physical container maps to exactly one `GridCore` and one `GridRender`.
There are no pages or hidden extra renderers.

Eligible bag, world and vehicle grids use a stable capacity-derived ceiling:

- wrapped base capacity is respected so compatible capacity overrides are
  included;
- ordinary containers reserve the conservative Organized ceiling;
- GridInventory's approximate two cells per capacity unit are retained;
- a 25% packing reserve is added;
- growth is bounded at `12x30` while firm upstream overrides remain minimums.

Floor, player-root inventories and corpses retain upstream dimensions.
Legacy page metadata and stale out-of-bounds placements are normalized without
deleting items.

### Sorting and multiplayer authority

Sorting uses a deterministic rotation-aware greedy layout. It tries the
name-aware pass first and falls back to bounded geometric orderings only when
needed for fit. It is intentionally heuristic rather than an expensive globally
optimal solver.

Display-name comparison uses natural numeric chunks, so names such as
`Книга 1`, `Книга 2`, `Книга 3`, `Книга 10` remain in human numeric order.
Russian case folding/collation is implemented with literal plain-string
replacement rather than Kahlua UTF-8 patterns.

On a dedicated server, the client first requests an authoritative membership
token. The server accepts the layout only if the container still matches that
token, validates every placement in a fresh grid, applies the coordinates and
sends the authoritative result back. Concurrent changes are rejected and
resynchronized instead of silently overwriting newer state.

The dedicated-server occupancy guard ignores hidden, equipped and attached
items consistently with the client grid model.

### Scoped duplicate consolidation

External and player mass-sort scopes build duplicate groups by stable item type
and move duplicate items one at a time through multiplayer inventory transfer
actions. Pack Flow does not call direct `transferItem()` as a replacement for
normal network transfer and does not teleport items between physical
containers.

The transfer orchestration retains stable `itemId` tracking because a successful
multiplayer transaction can detach or replace the original Lua `InventoryItem`
reference. Completion therefore does not depend solely on the old object's
`getContainer()` value.

Pack Flow never clears the player's entire timed-action queue as an internal
error-recovery mechanism. A Pack Flow failure must not cancel unrelated eating,
medical, reload or other player actions.

For the accepted Build 42 multiplayer path, the normal authoritative item
transaction remains responsible for the actual inventory mutation. The scoped
Pack Flow transfer action preserves a small visible transfer-action floor so a
low-latency server acknowledgement does not make all duplicate consolidation
look like teleportation.

### Floor as external consolidation source

Floor remains intentionally excluded from GridInventory coordinate auto-sort,
but `0.7.12` allows the nearby floor container to participate as an **external
source** for duplicate consolidation.

The rule is deliberately one-way:

- an item on the floor is eligible only when the same `fullType` already exists
  in an accessible non-floor external container;
- the destination with the largest existing amount of that type is preferred;
- unique floor items remain on the floor;
- floor is never selected as a destination for items already stored in a
  container;
- the floor container itself is never passed to the grid packing/CAS sorter.

After floor-source transfers finish, Pack Flow proceeds through the ordinary
external mass-sort path for the remaining containers.

### Scrolling and container navigation

The mouse wheel always scrolls the inventory or loot pane, including over empty
pane space and the container strip. Scroll speed is configurable from the
`PackFlow` options section with a range of 1–8 and default value 3.

Previous-container and next-container actions have separate configurable key
bindings and are unbound by default. The visible window under the pointer is
used first; outside both windows, loot takes priority over personal inventory.

### Stable refresh geometry

The final refresh wrapper preserves the stable pane scroll height and vertical
position across the hidden-list refresh.

Before each downstream container refresh it also snapshots every non-overflow
grid's base and outer height. Immediately after refresh it restores non-active
grids to base height and the active grid to base height plus its controls
footer. A previous footer reserve is retained when the controls themselves are
between refresh phases. Legitimately changing overflow grids are excluded.

## Runtime acceptance

The original client/dedicated-server acceptance contained the expected core
startup markers:

- `[LCC GridSort] bounded capacity grid installed`
- `[LCC GridSort] simple token/CAS network installed`
- `[LCC GridSort] stable wheel scrolling and container keybinds registered`
- `[LCC GridSort] native footer handlers registered`
- `[LCC GridSort] server safe occupancy installed`
- `[LCC GridSort] server simple token/CAS authority installed`

The later mass-consolidation validation additionally established a real
physical cross-container transfer: the Pack Flow transfer count and the
read-only Java `ItemContainer` membership trace both recorded one actual
container change (`1 transfers`, `physicalChanges=1`) rather than a
GridInventory-only coordinate relocation.

The final `ZomboidLogs_2026-08-29_01-15-49.zip` dedicated-server log loads the
accepted fingerprint:

```text
[LCC PackFlow] build=0.7.12 runtime=server natural-sort=plain-v2 search-gate=off mass-transfer=vanilla-mp+visual-floor-v1 floor-source=client physical-trace=client
```

No Pack Flow `sort aborted`, transfer `rejected/cancelled`, unconfirmed transfer,
Lua exception, or GridSort runtime failure appears in that archive. The only
PackFlow-path exceptions are the game's known scans for absent optional
`media/AnimSets` and `media/actiongroups` directories; these are not Pack Flow
runtime failures.

The client files inside that final archive are from a separate short game launch
that exits during loading and therefore do not contain the gameplay-side
`floor-source` telemetry. Final functional acceptance of floor-source behavior
is consequently based on the in-game test observation, together with the clean
0.7.12 dedicated-server runtime, rather than falsely attributing missing client
telemetry to the archive.

Final in-game acceptance confirms that the spatial UI remains stable, scoped
mass consolidation works, duplicate items are physically moved rather than
teleported by Pack Flow, and nearby floor duplicates can participate in
external consolidation without turning the floor into a destination.

Static validation from the original acceptance also passes:

```text
GridInventorySort static contract audit: OK
```

## Release constraints

The following behavior is intentional and must not be regressed:

- keep one physical container equal to one grid; do not restore the abandoned
  multi-page renderer layer;
- keep dimensions capacity-derived rather than membership-derived;
- keep sorting bounded; do not restore beam search or repeated profile
  matrices to the button click path;
- keep server token/CAS validation for multiplayer coordinate sorting;
- keep duplicate consolidation on real vanilla/server-authoritative inventory
  transactions; do not replace it with direct teleport-style mutation;
- do not rely on the lifetime of the original Lua item object as authoritative
  proof of a multiplayer transfer; stable item identity must be considered;
- do not clear unrelated player timed actions as Pack Flow recovery;
- floor may be a duplicate-consolidation **source**, but must not become a mass
  consolidation destination and must remain excluded from coordinate auto-sort;
- keep wheel scrolling separate from container selection;
- preserve both pane scroll state and individual grid outer heights during
  refresh;
- do not publish copied GridInventory source files inside Pack Flow;
- coordinate auto-sort remains unavailable for floor and corpse inventories and
  for nested bags in multiplayer; active/pending inventory work remains a busy
  gate where required for safe layout commits.

## Acceptance decision

Pack Flow `0.7.12` is accepted as the current release state for Project Zomboid
Build 42.20.x with GridInventory.

Capacity representation, bounded natural sorting, multiplayer coordinate
authority, stable scrolling/navigation, transfer-time grid stability, scoped
physical duplicate consolidation, visible multiplayer transfer behavior and
one-way floor-source consolidation are treated as the confirmed release state.

Further changes to the mass-transfer lifecycle or floor participation should be
considered regressions unless backed by a new reproducible runtime case and a
new acceptance pass.
