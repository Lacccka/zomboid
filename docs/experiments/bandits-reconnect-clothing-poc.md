# Bandits reconnect clothing / corpse PoC

## Reproduced defects

B42.20 multiplayer testing separated the clothing problem into two distinct boundaries.

### 1. Live reconnect state

Persistent Bandits surviving a disconnect/reconnect retain `brain.clothing` and their visual clothing definitions, but their real `WornItems` collapse to zero or nearly zero. `Bandit.ApplyVisuals()` explains this: upstream clears `bandit:getWornItems()` and recreates clothing primarily as standalone `ItemVisual` records.

### 2. Corpse materialization

The `real-worn-reconnect-v2` test proved that restoring real typed `WornItems` fixes the live NPC state after reconnect. However, runtime diagnostics also proved that these real worn objects do **not** automatically survive Bandit corpse materialization. A restored Bandit can have the full real worn set immediately before death while the resulting corpse contains only zero, one or two unrelated worn items.

Upstream `Bandit.UpdateItemsToSpawnAtDeath()` clears/rebuilds the death queue for inventory, weapons, ammunition, generated loot and bags, while its clothing block is commented out.

## Rejected v1

The first `real-worn-reconnect-v1` experiment incorrectly passed string keys from `brain.clothing` such as `Hat` or `Jacket` directly to B42 `getWornItem()` / `setWornItem()`.

B42 requires an `ItemBodyLocation` object, producing repeated:

```text
expected argument of type ItemBodyLocation, got String
```

That was an LCC PoC bug, not an upstream Bandits failure.

## Confirmed live fix: real-worn-reconnect-v2

`WorkshopPatches/Bandits-LCC-Dev/42.20/media/lua/client/zz_LCC_BanditClothingRestore.lua` wraps `Bandit.ApplyVisuals()`.

For each instantiated clothing item it obtains the typed slot from:

```lua
item:getBodyLocation()
```

Only that `ItemBodyLocation` object is passed to:

```lua
bandit:getWornItem(location)
bandit:setWornItem(location, item)
```

The `brain.clothing` string key remains metadata/tint/cache information only. The same real item object is cached per live Bandit and re-worn after later upstream `ApplyVisuals()` calls clear the worn state again.

The second runtime test confirmed:

- the old `ItemBodyLocation, got String` error disappeared;
- persistent Bandits commonly restored from `beforeWorn=0` to the complete typed worn set;
- the same survivors recovered their clothing again after another reconnect;
- body-visual entries such as makeup can legitimately count in `expectedClothing` without having a real wearable `ItemBodyLocation`;
- a small number of custom bags still report `BAG_LOCATION_UNAVAILABLE` and remain a separate compatibility case.

## Current corpse PoC: real-worn-death-queue-v1

The same file now also wraps `Bandit.UpdateItemsToSpawnAtDeath()`.

It does **not** re-enable upstream's commented block that creates fresh clothing copies. Instead it appends only the real materialized objects already worn by the Bandit and marked by `real-worn-reconnect-v2`.

Each queued object receives:

```text
LCC_BanditsRealClothing=real-worn-reconnect-v2
LCC_BanditsDeathQueue=real-worn-death-queue-v1
preserve=true
```

The flow is now:

```text
brain.clothing
  -> typed real WornItem
  -> Bandit remains visibly/physically clothed after reconnect
  -> upstream rebuilds normal death queue
  -> same real worn objects are appended to death queue
  -> corpse materialization test
```

There is still no `inventory:AddItem()` call and the PoC does not manufacture a second clothing copy for the queue.

`Bandit.ApplyVisuals()` normally invokes the upstream death-queue builder before LCC restores the real worn state. Therefore, after restoration the PoC deliberately rebuilds the queue once more; upstream clears/recreates its normal loot first, then the wrapper appends the currently worn LCC clothing objects.

## Runtime markers

Correct load:

```text
[LCC][BanditsClothingPoC][BOOT] marker=real-worn-reconnect-v2 deathMarker=real-worn-death-queue-v1 mode=typed-ItemBodyLocation+same-object-death-queue inventoryAdd=false copyClothing=false
```

Live repair:

```text
[LCC][BanditsClothingPoC][RESTORE] marker=real-worn-reconnect-v2 id=... beforeWorn=... expectedClothing=... restored=... created=... afterWorn=... bag=...
```

Death queue:

```text
[LCC][BanditsClothingPoC][DEATH_QUEUE] marker=real-worn-death-queue-v1 id=... queuedRealWorn=... currentWorn=... expectedClothing=... bag=...
```

Bounded failure diagnostics include:

```text
[BODY_LOCATION_MISSING]
[BAG_LOCATION_UNAVAILABLE]
[BODY_LOCATION_API_ERROR]
[RESTORE_ERROR]
[SLOT_CONFLICT]
[DEATH_QUEUE_ERROR]
[DEATH_QUEUE_REFRESH_ERROR]
```

## Test matrix

1. Start with several persistent Bandits from an earlier server session.
2. Join and confirm there is no `ItemBodyLocation, got String` exception.
3. Confirm old Bandits recover visible clothing and `[RESTORE]` reaches the expected typed worn count.
4. Confirm `[DEATH_QUEUE]` appears for restored Bandits and `queuedRealWorn` closely tracks `currentWorn`.
5. Kill at least two restored persistent Bandits, including one with a large clothing set.
6. Compare `BanditsDeathLoot` `PRE_CLEANUP`/`POST_CLEANUP` worn counts with final corpse `corpseItems`/`corpseWorn`.
7. Verify the clothing exists on/in the corpse and that no duplicate full types are produced.
8. Spawn fresh Bandits, repeat the death test, then reconnect once more and repeat with survivors.
9. Separately note custom bags that still emit `BAG_LOCATION_UNAVAILABLE`.

## Success criteria

A strong result requires:

- live reconnect restoration remains stable;
- no new Lua/Java error spam;
- `DEATH_QUEUE_ERROR=0` and `DEATH_QUEUE_REFRESH_ERROR=0`;
- queued real worn count matches the materialized wearable set;
- restored clothing survives corpse creation;
- no duplicate clothing appears because of simultaneous worn/death-queue handling;
- normal Bandits weapon/loot/bag death drops remain intact.

The death queue mechanism is still experimental. In particular, B42 runtime behavior for the **same object being both worn and queued for death spawn** must be validated before promotion to a public compatibility fix.
