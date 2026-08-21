# Bandits reconnect clothing / corpse investigation

## Build reference

The repository now contains the captured/decompiled Build 42.20.3 runtime under:

```text
game_source/common-42.20.3
```

The Java sources changed this investigation from a post-corpse heuristic into a pre-death authoritative-state fix.

## Reproduced behavior

Persistent Bandits keep `brain.clothing` and ItemVisual definitions across reconnect, but their real `WornItems` collapse. Upstream `Bandit.ApplyVisuals()` explicitly does:

```lua
bandit:getItemVisuals():clear()
bandit:getWornItems():clear()
```

and then recreates `brain.clothing` primarily as `ItemVisual` records. The old real-item clothing path is commented out.

Runtime testing established two visible symptoms:

- old/persistent Bandits become undressed after reconnect unless LCC recreates typed real WornItems;
- fresh current-session Bandits can produce complete corpses, while persistent/reconnected Bandits often produce corpses with only 0-2 worn items.

## Java-level root cause from Build 42.20.3

### IsoDeadBody copies the dying character's WornItems

`game_source/common-42.20.3/java/zombie/iso/objects/IsoDeadBody.java` constructs a corpse from the dying character using the character's **current runtime state**:

```java
this.setContainer(died.getInventory());
...
this.setWornItems(died.getWornItems());
this.setAttachedItems(died.getAttachedItems());
died.clearWornItems();
```

`setWornItems()` creates a new `WornItems(other)` copy. There is no special Bandits `brain.clothing` recovery at corpse construction time.

Therefore the correct repair boundary is **before `IsoDeadBody` exists**.

### Corpse serialization requires worn objects to exist in the container

The same Build 42.20.3 `IsoDeadBody.save()` first serializes the corpse `ItemContainer`:

```java
ArrayList<InventoryItem> savedItems = this.container.save(output);
```

and then writes each worn entry as:

```java
GameWindow.WriteString(output, wornItem.getLocation().toString());
output.putShort((short)savedItems.indexOf(wornItem.getItem()));
```

On load, B42 restores the worn entry only when that index points to a valid saved container item.

This is the missing detail from the earlier PoCs: a real object that is only present in `WornItems`, but is not the same object stored in the character/corpse inventory, is not a durable serialized worn item.

### Why the previous client-only fix was incomplete

`real-worn-reconnect-v2` was stored under `media/lua/client` and began with:

```lua
if isServer() then return end
```

It successfully fixed the local live NPC, but the dedicated server could still own an empty/near-empty `WornItems` set. After controller/authority changes or reconnect, corpse creation/serialization could therefore use incomplete server state.

This explains the previous pattern better than the rejected post-corpse theories.

## Rejected experiments

### real-worn-reconnect-v1

Passed string slot names directly to B42 worn APIs and caused:

```text
expected argument of type ItemBodyLocation, got String
```

B42 requires `ItemBodyLocation`. Removed.

### real-worn-death-queue-v1

Added the same locally restored worn objects to `addItemToSpawnAtDeath()`. Runtime showed complete queue counters while persistent corpses still lost clothing. Removed.

### post-corpse-clothing-repair-v1

Attempted to mutate `IsoDeadBody` after `OnDeadBodySpawn`. Runtime showed the repair was too late/unreliable, and positional fallback could match an unrelated nearby corpse before the actual Bandit corpse arrived.

The file has been deleted. There is no longer any LCC post-corpse clothing mutation.

## Current architecture

### Client: real-worn-reconnect-v2

File:

```text
WorkshopPatches/Bandits-LCC-Dev/42.20/media/lua/client/zz_LCC_BanditClothingRestore.lua
```

Purpose: restore live local worn state after upstream `ApplyVisuals()` clears it.

It uses:

```lua
local location = item:getBodyLocation()
bandit:getWornItem(location)
bandit:setWornItem(location, item)
```

and does not perform death-queue or corpse repair work.

Expected marker:

```text
[LCC][BanditsClothingPoC][BOOT] marker=real-worn-reconnect-v2 role=client-live typedBodyLocation=true snapshot=false deathQueue=false postCorpse=false
```

### Dedicated server: server-authoritative-worn-v1

File:

```text
WorkshopPatches/Bandits-LCC-Dev/42.20/media/lua/server/zz_LCC_BanditServerClothingRestore.lua
```

It wraps the shared `Bandit.ApplyVisuals()` on the dedicated server. This is valid because the server spawner calls `Bandit.ApplyVisuals(zombie, brain)` while banditizing both new and restored Bandits.

For every wearable `brain.clothing` entry it now maintains one authoritative object with this invariant:

```text
same InventoryItem object
  -> exists in bandit:getInventory()
  -> exists in bandit:getWornItems()
```

This directly matches the Build 42.20.3 `IsoDeadBody` constructor/save contract.

Marked server clothing objects are reused after later `ApplyVisuals()` calls, because upstream clears WornItems but does not remove the marked objects from inventory. This avoids creating a new clothing object on every visual refresh.

Expected boot marker:

```text
[LCC][BanditsServerClothing][BOOT] marker=server-authoritative-worn-v1 authority=dedicated-server inventoryBacked=true corpseSource=died.WornItems
```

Per-Bandit restoration:

```text
[LCC][BanditsServerClothing][RESTORE]
marker=server-authoritative-worn-v1
id=...
beforeWorn=...
afterWorn=...
restored=...
created=...
reusedInventory=...
inventoryAdds=...
inventoryItems=...
```

Periodic summary:

```text
[LCC][BanditsServerClothing][SUMMARY]
applyCalls=...
restored=...
created=...
reusedInventory=...
inventoryAdds=...
alreadyWorn=...
noLocation=...
conflicts=...
errors=...
```

## Test matrix

1. Full dedicated-server and client restart with the updated Bandits-LCC-Dev copy.
2. Verify both client and server clothing boot markers.
3. Spawn several fresh Bandits and verify server `[RESTORE]` shows `afterWorn > 0`, `inventoryAdds > 0`, `errors=0`.
4. Keep several Bandits alive, reconnect the client, and verify the live client restore still works.
5. Kill at least three persistent/reconnected Bandits with large clothing sets.
6. Use the existing `BanditsDeathLoot` diagnostics to compare expected clothing with final `corpseWorn`/container contents.
7. Reconnect again and inspect the same corpse inventory to ensure the clothing survives serialization/load, not only the immediate death frame.
8. Kill fresh current-session Bandits as control.
9. Check for duplicate marked clothing in corpse inventory; repeated `ApplyVisuals()` should show `reusedInventory` instead of monotonically increasing `created`.

## Success criteria

Strong confirmation requires:

- client reconnect restoration remains stable;
- server `errors=0`;
- server `afterWorn` tracks the wearable brain.clothing set;
- the authoritative worn items are inventory-backed (`inventoryAdds` on first creation, `reusedInventory` on later visual refreshes);
- persistent/reconnected Bandit corpses retain their expected clothing immediately after death;
- the same corpse still retains clothing after another reconnect/load;
- no post-corpse LCC intervention is present;
- no duplicated clothing accumulates from repeated `ApplyVisuals()` calls;
- normal Bandits weapons/ammo/generated loot remain unchanged.

This remains a controlled working-copy experiment, but unlike the prior death-queue/post-corpse approaches it is now aligned with the actual Build 42.20.3 Java corpse lifecycle.
