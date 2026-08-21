# Bandits reconnect clothing / corpse PoC

## Reproduced defects

B42.20 multiplayer testing separated the clothing problem into two independent lifecycle boundaries.

### Live reconnect state

Persistent Bandits keep `brain.clothing` and their visual definitions across reconnect, but upstream `Bandit.ApplyVisuals()` clears real `WornItems` and rebuilds clothing primarily as standalone `ItemVisual` records. Old NPCs therefore become visually/physically undressed after a new client session.

### Corpse materialization

`real-worn-reconnect-v2` proved that restoring typed real `WornItems` fixes the live persistent NPC. A later runtime test proved that this is still insufficient for corpse creation: a Bandit can have the full real worn set immediately before death while the resulting `IsoDeadBody` contains only zero, one or two worn items.

## Rejected experiments

### real-worn-reconnect-v1

The first restore attempt passed string slot names such as `Hat` and `Jacket` to B42 live-character worn APIs. B42 requires `ItemBodyLocation`, producing repeated:

```text
expected argument of type ItemBodyLocation, got String
```

That was an LCC PoC bug and was removed.

### real-worn-death-queue-v1

The next experiment appended the **same real objects already worn by the Bandit** to `addItemToSpawnAtDeath()` after upstream rebuilt its normal death queue.

Runtime results rejected this mechanism. The queue wrapper ran without errors and reported complete sets such as `queuedRealWorn=13`, while persistent/reconnected Bandit corpses still materialized as `corpseWorn=0..2`.

Therefore the same-object death queue is removed. `zz_LCC_BanditClothingRestore.lua` no longer wraps `Bandit.UpdateItemsToSpawnAtDeath()`.

## Confirmed live fix: real-worn-reconnect-v2

`WorkshopPatches/Bandits-LCC-Dev/42.20/media/lua/client/zz_LCC_BanditClothingRestore.lua` still wraps `Bandit.ApplyVisuals()`.

For every clothing item it obtains the typed slot from:

```lua
item:getBodyLocation()
```

and only passes that object to the live-character APIs:

```lua
bandit:getWornItem(location)
bandit:setWornItem(location, item)
```

The same real item object is cached per live Bandit and re-worn after later upstream visual rebuilds. Runtime tests confirmed persistent NPCs repeatedly restoring from `beforeWorn=0` to their complete wearable set after reconnect.

The live wrapper now also records a compact snapshot keyed by Bandit brain id:

```text
LCC_BanditsClothingSnapshots[id] = {
  clothing = copy of brain.clothing,
  tint = copy of brain.tint,
  fullname,
  position,
  timestamp
}
```

The Bandit modData is tagged with `LCC_BanditsBrainId` to improve corpse correlation. No death queue manipulation remains.

Correct live boot marker:

```text
[LCC][BanditsClothingPoC][BOOT] marker=real-worn-reconnect-v2 mode=typed-ItemBodyLocation snapshot=true deathQueue=false
```

## Current corpse PoC: post-corpse-clothing-repair-v1

A new file performs repair only after B42 has already created the native corpse:

```text
WorkshopPatches/Bandits-LCC-Dev/42.20/media/lua/client/zzzzz_LCC_BanditCorpseClothingRepair.lua
```

It listens to:

```text
OnZombieDead
OnDeadBodySpawn
```

Corpse identity uses the same strategy already validated by `BanditsDeathLootDiagnostics`:

1. direct `LCC_BanditsBrainId` / `brainId` / `banditId` from modData when available;
2. otherwise a recent-death positional match limited to 10 seconds, the same Z level and 2.25 tiles.

Unrelated vanilla corpses are ignored.

### IsoDeadBody worn API

The live Bandit and the corpse intentionally use different APIs.

For a live `IsoZombie`, `item:getBodyLocation()` supplies the typed `ItemBodyLocation` required by `bandit:getWornItem()` / `bandit:setWornItem()`.

For an `IsoDeadBody`, the repair works through its `WornItems` collection:

```lua
local worn = body:getWornItems()
local slot = tostring(brainLocation)
local current = worn:getItem(slot)
worn:setItem(slot, item)
```

`item:getBodyLocation()` is still used on the corpse path, but only as a **wearability check**. Entries with no real body location, such as makeup-only visuals, are not manufactured as corpse items.

### Idempotent repair rules

For every item in the saved `brain.clothing` snapshot:

1. instantiate/probe the item and require a real `item:getBodyLocation()`; visual-only entries are skipped;
2. use the original `brain.clothing` key as the `WornItems` slot string;
3. if `worn:getItem(slot)` already contains the expected full type, do nothing;
4. if another real item occupies the slot, preserve it and log `SLOT_CONFLICT` rather than overwriting it;
5. if an unworn item of the expected full type already exists in the corpse container, reuse that object;
6. only if no matching container item exists, create one directly in the corpse container;
7. apply the saved Bandit tint and call `worn:setItem(slot, item)`;
8. if the worn mutation is rejected by B42, leave the item in the corpse container so the loot is still restored without generating another copy.

This makes the PoC deduplicating by both **slot** and **actual corpse container contents** instead of relying on pre-death queue semantics.

Correct corpse boot marker:

```text
[LCC][BanditsCorpseRepair][BOOT] marker=post-corpse-clothing-repair-v1 snapshotMarker=real-worn-reconnect-v2 mode=post-OnDeadBodySpawn+wornItems dedupe=slot+container
```

Each matched Bandit corpse prints one summary:

```text
[LCC][BanditsCorpseRepair][REPAIR]
marker=post-corpse-clothing-repair-v1
id=...
match=modData|position
expected=...
wearableExpected=...
beforeItems=...
beforeWorn=...
already=...
reused=...
created=...
repaired=...
conflicts=...
noLocation=...
instanceFailures=...
addFailures=...
wearFailures=...
afterItems=...
afterWorn=...
```

## Current test matrix

1. Start with persistent Bandits that survived the previous server/client session.
2. Reconnect and confirm they are restored by `real-worn-reconnect-v2`.
3. Confirm there are no old `DEATH_QUEUE` markers; that experiment is intentionally gone.
4. Kill at least three restored persistent Bandits with large clothing sets.
5. Inspect `BanditsCorpseRepair][REPAIR]` and the existing `BanditsDeathLoot][CORPSE]` records for the same ids.
6. Verify `afterWorn` approaches `wearableExpected` and corpse inventory contains the expected full types.
7. Verify `created` only covers actually missing items and does not duplicate items already supplied by vanilla corpse materialization.
8. Repeat with fresh current-session Bandits as a control; healthy vanilla materialization should mostly produce `already > 0` and little/no intervention.
9. Reconnect again and repeat with survivors.

## Success criteria

A strong result requires:

- live reconnect restoration remains stable;
- `DEATH_QUEUE` / `DEATH_QUEUE_ERROR` / `DEATH_QUEUE_REFRESH_ERROR` disappear because that wrapper no longer exists;
- every matched persistent corpse gets at most one `REPAIR` pass;
- `addFailures=0` and preferably `wearFailures=0`;
- `afterWorn` matches the number of wearable expected slots except explicit slot conflicts;
- no duplicate clothing full types are introduced by repair;
- fresh Bandit corpses that already materialize correctly require little or no creation;
- normal weapons, ammo, loot and bags from upstream death processing remain intact.

This remains a controlled working-copy experiment until the post-corpse mutation is validated in B42.20 multiplayer.
