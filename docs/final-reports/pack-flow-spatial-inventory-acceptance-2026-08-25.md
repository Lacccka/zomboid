# Pack Flow spatial-inventory acceptance — 2026-08-25

Branch: `agent/b42-20-compatibility-patch`

Runtime archive: `ZomboidLogs_2026-08-25_03-51-14.zip`

Release identity:

- Workshop title: `Lacccka B42 Pack Flow`
- Workshop ID: `3789630746`
- Mod ID: `LaccckaPackFlow`
- patch version: `0.6.1`
- target: Project Zomboid Build `42.20.x`
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
   active grid temporarily lost its controls-footer height during refresh.

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

Sorting uses a deterministic rotation-aware greedy layout. It tries an
area-first ordering and, for smaller inventories, one additional geometric
ordering. It is intentionally bounded and heuristic rather than an expensive
globally optimal solver.

On a dedicated server, the client first requests an authoritative membership
token. The server accepts the layout only if the container still matches that
token, validates every placement in a fresh grid, applies the coordinates and
sends the authoritative result back. Concurrent changes are rejected and
resynchronized instead of silently overwriting newer state.

The dedicated-server occupancy guard ignores hidden, equipped and attached
items consistently with the client grid model.

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

The final client and dedicated-server archive contains all expected startup
markers:

- `[LCC GridSort] bounded capacity grid installed`
- `[LCC GridSort] simple token/CAS network installed`
- `[LCC GridSort] stable wheel scrolling and container keybinds registered`
- `[LCC GridSort] native footer handlers registered`
- `[LCC GridSort] server safe occupancy installed`
- `[LCC GridSort] server simple token/CAS authority installed`

No Lua error or exception associated with Pack Flow, `GridSort` or
`GridPaneUX` appears in the archive. Other engine/mod warnings in the logs are
unrelated to this patch.

Final in-game visual acceptance from the user confirmed that the lower rows no
longer jump or overlap during transfers and that the resulting inventory UI
works correctly.

Static validation also passes:

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
- keep server token/CAS validation for multiplayer sorting;
- keep wheel scrolling separate from container selection;
- preserve both pane scroll state and individual grid outer heights during
  refresh;
- do not publish copied GridInventory source files inside Pack Flow;
- auto-sort remains unavailable for floor and corpse inventories, during busy
  transfer/search states, and for nested bags in multiplayer.

## Acceptance decision

Pack Flow `0.6.1` is accepted for publication for Project Zomboid Build 42.20.x
with GridInventory. Capacity representation, bounded sorting, multiplayer
authority, scrolling/navigation and transfer-time grid stability are treated as
the confirmed release state.
