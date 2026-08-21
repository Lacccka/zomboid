# Bandits death clothing remove race v4

## Runtime confirmation

The `2026-08-21 15:01` dedicated-server / `15:04` client test finally exercised both orderings of the Bandit death race.

Ten Bandit deaths were observed by the client death-loot diagnostics.

Primary authoritative repair handled five:

```text
Ethan Evans
Hudson Phillips
Asher Bennett
Daniel Scott
Asher Morris
```

`server-death-worn-remove-snapshot-v1` fallback handled the other five:

```text
Jacob Cox
Gabriel Wilson
Alexander Gutierrez
Jacob Wood
Lincoln Brooks
```

All server repairs completed with `errors=0`.

The final client corpses were:

```text
Jacob Cox              expected=13 corpseWorn=14
Ethan Evans             expected=13 corpseWorn=14
Hudson Phillips         expected=13 corpseWorn=14
Gabriel Wilson          expected=13 corpseWorn=13
Asher Bennett           expected=13 corpseWorn=13
Alexander Gutierrez     expected=11 corpseWorn=11
Jacob Wood              expected=8  corpseWorn=9
Daniel Scott            expected=9  corpseWorn=10
Asher Morris            expected=8  corpseWorn=9
Lincoln Brooks          expected=9  corpseWorn=10
```

The `expected+1` cases are compatible with an additional bag/other wearable. Crucially, the historical persistent-corpse collapse to `0-2` worn items did not occur.

This validates the Build 42.20.3 invariant:

```text
same InventoryItem object
  -> dying IsoZombie inventory
  -> dying IsoZombie WornItems
  -> IsoDeadBody container + copied WornItems
```

## Confirmed race split

The test also proved that both event orders occur in practice:

```text
server OnZombieDead first -> primary repair
BanditRemove first        -> clothing snapshot fallback
```

Final counters showed:

```text
primary deathRepairs=5
fallback fallbackRepairs=5
snapshotsCaptured=10
```

The last value revealed a small lifecycle leak in fallback v1. When the primary repair won first, a later `BanditRemove` still captured a snapshot, but there was no future `OnZombieDead` to consume it. Five of the ten snapshots therefore remained stale.

## v4 cleanup: snapshot fallback v2

`zzz_LCC_BanditServerClothingSnapshotFallback.lua` is now:

```text
server-death-worn-remove-snapshot-v2
```

When fallback `OnZombieDead` sees the primary marker, it records that Bandit id briefly. If `BanditRemove` arrives afterward, snapshot creation is skipped and counted as:

```text
removeAfterPrimary
```

Race state is retained for only two `EveryOneMinute` epochs. Stale snapshots/handled ids are pruned and exposed as:

```text
activeSnapshots
activeHandled
snapshotsPruned
handledPruned
```

The actual clothing-repair logic is unchanged from the successful test.

## Next test criteria

For a mixed batch of Bandit deaths:

```text
primary deathRepairs + fallback fallbackRepairs == Bandit deaths under test
errors=0
removeAfterPrimary > 0 when primary wins before BanditRemove
activeSnapshots returns to 0 after race completion/pruning
activeHandled returns to 0 after late BanditRemove/pruning
```

Client corpse diagnostics should continue to show complete worn sets with no return to the historical `0-2` persistent-corpse state.
