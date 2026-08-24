# GridInventorySort v0.5 — bounded capacity grid

Date: 2026-08-25
Branch: `agent/b42-20-compatibility-patch`
Addon: `LaccckaB4220GridInventorySort`
Target: GridInventory `3782313362`, Project Zomboid Build 42.20.x

## Confirmed capacity mechanics

The implementation was based on the checked-in B42.20.3 source rather than
wiki assumptions.

| Modifier | Player root carry | Bag/world capacity | Per-player | Evidence |
| --- | --- | --- | --- | --- |
| Strength | Changes the comfortable/encumbrance `maxWeight` through `getWeightMod()` | No | Character state | `IsoGameCharacter.getWeightMod`, `BodyDamage.UpdateStrength` |
| Strength traits | Initial `maxWeightDelta`: Weak 0.75, Feeble 0.9, Stout 1.25, Strong 1.5 | No | Character state | `IsoPlayer` constructors |
| Organized | No root bonus | `max(capacity * 1.3, capacity + 1)` | Yes | `ItemContainer.getEffectiveCapacity` |
| Disorganized | No root penalty | `max(capacity * 0.7, 1)` | Yes | `ItemContainer.getEffectiveCapacity` |
| PPO tone | Adjusts `maxWeightBase`, divided by the Strength ladder | No | Character state | `PPO_PhysicalEffects`, `PPO_ToneMath` |
| Lifestyle Wanderer | Adjusts `maxWeightBase` | No | Character state | `LSWanderer`, `LSservercommands` |
| Aegis/AP carry override | Can pin `maxWeightBase` and `maxWeight` | No | Character/admin state | `Aegis_Server`, `AegisHud` |
| Aegis/AP capacity stamp | Replaces base bag/world/vehicle capacity and then reapplies traits | Yes | Shared container, trait evaluated per character | `Aegis_Capacity`, `AegisCapacityClient` |
| MFS backpack compatibility | No | Overrides seven BackpackSystem bags to base capacity 52–65 and wraps effective capacity | Yes | `MFSBackpackCapacity.lua` |

Important distinctions:

- player `maxWeight` is the comfortable encumbrance threshold shown by vanilla;
- the player root `ItemContainer` has its own hard transfer capacity;
- Strength does not enlarge a worn bag;
- Organized/Disorganized affect ordinary bags, world containers and vehicle
  containers, but vanilla skips character-root inventories, corpses and floor;
- therefore grid geometry for a normal container must account for effective
  capacity, but must not be derived from the viewing player's current traits.

## Why `61.08 / 47` was legal

The Chimera item `[ARES] Nomad Backpack` has script capacity `47`.

For an Organized character:

```text
47 * 1.3 = 61.1
vanilla int result = 61
```

GridInventory's header intentionally prints `getMaxWeight()` and therefore
keeps the base denominator `47`, while transfer/drop validation uses
`getEffectiveCapacity(character)`. A displayed state around `61 / 47` can
therefore be valid and is not evidence that vanilla capacity was bypassed.

The old GridInventory size calculation used the base capacity and capped normal
containers at `6x15`. It could not represent all contents that Organized legally
allowed into the Nomad bag, causing `unpositioned` followed by GridAutoDrop.

## v0.5 sizing rule

One physical container still maps to exactly one `GridCore` and one
`GridRender`. There are no pages or hidden renderers.

The shared client/server dimensions are now derived only from container
capacity:

1. read the wrapped `container:getCapacity()` so Aegis and MFS capacities are
   included;
2. for containers that can receive trait modifiers, reserve the conservative
   Organized ceiling `ceil(capacity * 1.30)`;
3. preserve GridInventory's approximate density of two cells per capacity;
4. add a 25% packing reserve;
5. choose a compact near-square rectangle;
6. preserve upstream/GridDevTool width and height as minimums;
7. cap addon growth at `12x30`.

The dimensions do not depend on current item membership. This prevents panel
resize flicker and keeps client/server bounds identical during replication lag.

Expected examples:

| Base capacity | Shared ceiling | Expected grid |
| ---: | ---: | ---: |
| 18 | 24 | 8x8 |
| 27 | 36 | 10x9 |
| 40 | 52 | 12x14 |
| 47 Nomad | 62 | 12x15 |
| 56 MFS | 73 | 12x16 |
| 65 MFS | 85 | 12x18 |
| 100 world container | 130 | 12x28 |

Floor, character-root inventories and corpses remain completely upstream-sized.
Existing firm GridInventory/GridDevTool overrides are never shrunk.

## Migration and cleanup

- v0.2/v0.3 `gridPage` data is still removed;
- v0.4 placements outside the new bounded rectangle have only their stale grid
  coordinates cleared and are repacked by normal refresh;
- the obsolete `zz_LCC_GridInventorySortV02.lua` bootstrap was removed because
  it still attempted to require deleted `GridMultiPage` code and printed a false
  `multi-page bootstrap failed` message every client start;
- mod version is now `0.5.0`;
- expected startup marker is:

```text
[LCC GridSort] bounded capacity grid installed
```

## Runtime test plan

1. Fully restart client and dedicated server; do not hot-reload the migration.
2. Confirm `bounded capacity grid installed` on client and server.
3. Confirm there is no `multi-page bootstrap failed` marker.
4. Open the Nomad bag with the Organized character:
   - one grid only;
   - expected dimensions `12x15`;
   - no overflow area for the previous `58–61 / 47` test set;
   - no item ejection by GridAutoDrop.
5. Transfer the same set floor → Nomad and Nomad → floor.
6. Press SORT repeatedly:
   - no freeze;
   - stable second-click no-op;
   - no membership/stale loop.
7. Test a normal character and a Disorganized character against the same bag.
   Grid dimensions must remain identical; only vanilla acceptance capacity may
   differ.
8. Test one MFS bag with configured base capacity above 50. Client and server
   must agree on dimensions and sort bounds.
9. Repeat the dedicated nested-bag transfer test for the safe occupancy guard.
10. Repeat the two-client simultaneous SORT test; first writer must win and both
    clients must converge.

This change is source-audited and statically validated, but the runtime items
above are still required before v0.5 can be called confirmed in dedicated MP.
