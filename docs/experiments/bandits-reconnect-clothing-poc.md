# Bandits reconnect clothing / corpse investigation

## Build reference

Authoritative Build 42.20.3 source is stored under:

```text
game_source/common-42.20.3
```

The decompiled Java establishes the correct repair boundary and removes the need for post-corpse heuristics.

## Reproduced behavior

Persistent Bandits keep `brain.clothing` and ItemVisual definitions across reconnect, while their real `WornItems` collapse. Upstream `Bandit.ApplyVisuals()` clears both item visuals and real worn state, then reconstructs `brain.clothing` primarily as `ItemVisual` records.

Runtime testing established:

- client-side persistent Bandits become undressed after reconnect unless typed real WornItems are recreated;
- fresh current-session Bandits often create complete corpses;
- persistent/reconnected Bandits often create corpses with only 0-2 worn items.

## Exact Build 42.20.3 corpse contract

### Construction copies the dying character state

`game_source/common-42.20.3/java/zombie/iso/objects/IsoDeadBody.java` constructs a corpse with:

```java
this.setContainer(died.getInventory());
this.setWornItems(died.getWornItems());
this.setAttachedItems(died.getAttachedItems());
```

`setWornItems()` creates `new WornItems(other)`. There is no recovery from Bandits `brain.clothing` at corpse construction time.

### Serialization requires the same worn object in the corpse container

`IsoDeadBody.save()` first serializes the corpse container:

```java
ArrayList<InventoryItem> savedItems = this.container.save(output);
```

and serializes each worn entry as an index into that saved item list:

```java
GameWindow.WriteString(output, wornItem.getLocation().toString());
output.putShort((short)savedItems.indexOf(wornItem.getItem()));
```

On load, an invalid/negative index is skipped. Therefore a durable worn item must satisfy:

```text
same InventoryItem object
  -> present in corpse ItemContainer
  -> referenced by corpse WornItems
```

`WornItems.setItem()` itself does not add an item to inventory.

### Exact death ordering provides a better repair hook

`game_source/common-42.20.3/java/zombie/characters/IsoZombie.java` performs:

```text
DoZombieInventory()
OnZombieDead
DoDeath() / IsoDeadBody construction
```

`DoZombieInventory()` has already materialized normal `itemsToSpawnAtDeath` into the zombie inventory before `OnZombieDead` runs.

Therefore `OnZombieDead` on the dedicated server is an ideal just-in-time repair boundary: add only missing Bandit clothing to the zombie inventory, wear the exact same object, then let vanilla `DoDeath()` copy both structures into the corpse.

## Rejected experiments

### real-worn-reconnect-v1

Passed string body-location names to B42 APIs and produced `expected argument of type ItemBodyLocation, got String`. Removed.

### real-worn-death-queue-v1

Queued locally restored worn objects through `addItemToSpawnAtDeath()`. Runtime still produced incomplete persistent corpses. Removed.

### post-corpse-clothing-repair-v1

Tried to mutate `IsoDeadBody` after `OnDeadBodySpawn`; it was late, unreliable, and positional matching could select a nearby unrelated corpse. Removed.

### server-authoritative-worn-v1

The first server-side version wrapped `Bandit.ApplyVisuals()` and maintained inventory-backed clothing throughout the NPC lifetime. The Java death ordering shows this is broader than necessary and does not guarantee coverage for persistent entities that never pass through another server `ApplyVisuals()` call after restart.

It has been replaced by a death-only authoritative repair.

## Current architecture

### Client live state: real-worn-reconnect-v2

File:

```text
WorkshopPatches/Bandits-LCC-Dev/42.20/media/lua/client/zz_LCC_BanditClothingRestore.lua
```

Purpose: restore the visible/local live worn state after reconnect. It uses typed `item:getBodyLocation()` with `getWornItem/setWornItem` and performs no corpse/death-queue work.

Marker:

```text
[LCC][BanditsClothingPoC][BOOT] marker=real-worn-reconnect-v2 role=client-live typedBodyLocation=true snapshot=false deathQueue=false postCorpse=false
```

### Dedicated server corpse authority: server-authoritative-death-worn-v2

File:

```text
WorkshopPatches/Bandits-LCC-Dev/42.20/media/lua/server/zz_LCC_BanditServerClothingRestore.lua
```

The file no longer wraps `Bandit.ApplyVisuals()` and does not mutate live Bandit inventory during normal play.

On server `OnZombieDead` it:

1. reads the canonical persistent outfit id;
2. resolves the Bandit brain from `GetBanditClusterData(id)[id]`;
3. processes only actual `brain.clothing` entries with a real `ItemBodyLocation`;
4. preserves an already-correct worn item and ensures the exact same object is inventory-backed;
5. otherwise reuses an unworn matching inventory object when possible;
6. otherwise creates one item, adds that exact object to the zombie inventory, then wears the same object;
7. verifies `getWornItem(location) == item` and `item:getContainer() == zombie:getInventory()` before vanilla corpse construction.

It deliberately does not handle bags here; upstream Bandits already has separate bag/death-loot handling.

Boot marker:

```text
[LCC][BanditsServerClothing][BOOT] marker=server-authoritative-death-worn-v2 authority=dedicated-server boundary=OnZombieDead timing=after-DoZombieInventory-before-IsoDeadBody invariant=inventory+same-worn-object liveInventoryMutation=false
```

Per Bandit death:

```text
[LCC][BanditsServerClothing][DEATH_REPAIR]
marker=server-authoritative-death-worn-v2
id=...
expected=...
wearableExpected=...
beforeWorn=...
afterWorn=...
beforeInventory=...
afterInventory=...
restored=...
created=...
reusedInventory=...
inventoryAdds=...
alreadyWorn=...
noLocation=...
conflicts=...
errors=...
invariant=inventory+same-worn-object
```

Periodic summary exposes `deathsSeen`, `banditDeathsMatched`, `deathRepairs` and aggregate repair counters.

## Test matrix

1. Full dedicated-server/client restart with a fresh `Bandits-LCC-Dev` copy.
2. Verify the client live marker and server `server-authoritative-death-worn-v2` marker.
3. Keep several existing persistent Bandits alive through a reconnect; client live clothing should still restore normally.
4. Kill at least three persistent/reconnected Bandits with large clothing sets.
5. For every kill, require one server `[DEATH_REPAIR]` with `errors=0`.
6. `afterWorn` should approach `wearableExpected` except explicit slot conflicts/non-wearable visuals.
7. Compare the same ids against existing `BanditsDeathLoot` corpse diagnostics.
8. Reconnect again and inspect those corpses: clothing must survive corpse serialization/load, not only the immediate frame.
9. Kill fresh current-session Bandits as a control; these should often show more `alreadyWorn`/reuse and less creation.
10. Confirm normal weapon/ammo/generated loot is unchanged and no LCC post-corpse repair markers exist.

## Success criteria

Strong confirmation requires:

- live client reconnect restoration remains stable;
- `banditDeathsMatched == deathRepairs` for tested Bandit deaths;
- `errors=0` and invariant verification succeeds;
- persistent corpses immediately contain their expected wearable clothing;
- the same clothing remains after reconnect/reload;
- no post-corpse mutation is present;
- no duplicate clothing is introduced;
- no live server inventory pollution occurs before death.

This architecture is aligned directly with the Build 42.20.3 Java death lifecycle rather than inferred corpse behavior.
