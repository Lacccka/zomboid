# Bandits death clothing: BanditRemove race v3

## Runtime basis

Test archive:

```text
ZomboidLogs_2026-08-21_14-46-44.zip
```

Build: Project Zomboid 42.20.3.

The server-authoritative death-boundary repair was active as:

```text
server-authoritative-death-worn-v2
```

## What the run proved

The actual B42.20.3 corpse boundary is correct.

For four Bandit deaths where the server still had access to the cluster brain,
the primary repair ran with `errors=0`:

```text
James Bennett: beforeWorn=1  -> afterWorn=13
Aubrey Thomas: beforeWorn=2  -> afterWorn=14
Samuel Kim:    beforeWorn=1  -> afterWorn=13
Ella Lewis:    beforeWorn=0  -> afterWorn=13
```

The strongest corpse-side confirmations are:

```text
Aubrey Thomas
corpseItems=15
corpseWorn=14
matchDist=0.000

Samuel Kim
corpseItems=13
corpseWorn=13
matchDist=0.014

Ella Lewis
match=modData
corpseItems=13
corpseWorn=13
```

Ella Lewis is decisive because the corpse was keyed by Bandit modData rather
than positional fallback. The exact 13 restored inventory-backed WornItems
reached `IsoDeadBody` successfully.

James Bennett's observer result is not valid evidence against the repair. The
reported corpse was position-matched at `matchDist=0.841` and contained generic
vanilla clothing (`Tshirt_DefaultTEXTURE_TINT`, random shoes/trousers, etc.). It
was almost certainly an unrelated nearby corpse selected by the diagnostic
fallback.

## Why only four server Bandit deaths were repaired

Final primary summary:

```text
deathsSeen=26
banditDeathsMatched=4
deathRepairs=4
expected=52
wearableExpected=52
restored=49
alreadyWorn=1
conflicts=2
errors=0
```

The low `banditDeathsMatched` count exposed a separate race, not a WornItems
failure.

Client `BanditUpdate.lua` performs this during Bandit death cleanup:

```lua
local args = {}
args.id = brain.id
sendClientCommand(player, 'Commands', 'BanditRemove', args)
BanditBrain.Remove(bandit)
```

Server `BanditServer.Commands.BanditRemove` immediately does:

```lua
local gmd = GetBanditClusterData(id)
if gmd[id] then
    gmd[id] = nil
end
```

Therefore two valid event orders exist:

```text
A. server OnZombieDead first
   -> cluster brain still exists
   -> primary death repair succeeds

B. client BanditRemove reaches server first
   -> cluster brain is deleted
   -> later server OnZombieDead cannot resolve brain.clothing
```

The first test batch contains fresh server-created Bandits that died on the
client while the primary server summary still reported `banditDeathsMatched=0`,
which is consistent with order B.

## v3 race fallback

New server file:

```text
WorkshopPatches/Bandits-LCC-Dev/42.20/media/lua/server/
zzz_LCC_BanditServerClothingSnapshotFallback.lua
```

Marker:

```text
server-death-worn-remove-snapshot-v1
```

The fallback wraps `BanditServer.Commands.BanditRemove` and copies only:

```text
id
fullname
brain.clothing
brain.tint
```

immediately before the original command deletes the cluster entry.

It does not create InventoryItems and does not modify live WornItems.

Its `OnZombieDead` callback is registered after the primary v2 callback. If the
primary already marked the zombie with:

```text
server-authoritative-death-worn-v2
```

then the snapshot is discarded and no duplicate work occurs.

If the cluster was already deleted and the primary could not run, the fallback
restores the same death-boundary invariant from the snapshot:

```text
same InventoryItem
  -> zombie inventory
  -> zombie WornItems
```

before `IsoDeadBody` construction.

## Expected next-run diagnostics

Boot:

```text
[LCC][BanditsServerClothingFallback][BOOT]
marker=server-death-worn-remove-snapshot-v1
```

Summary:

```text
removeCalls
snapshotsCaptured
snapshotMissesAtRemove
primaryAlreadyHandled
fallbackMatches
fallbackRepairs
restored
errors
```

For a mixed death batch both paths may legitimately execute:

```text
primary death repair > 0
fallback death repair > 0
```

The important invariant is that every Bandit death follows one of them.

## Acceptance criteria

1. `errors=0` in both primary and fallback summaries.
2. `snapshotsCaptured` tracks BanditRemove calls while the brain still exists.
3. When BanditRemove wins the race, `fallbackRepairs > 0`.
4. Repaired keyed corpses retain approximately all expected wearable clothing.
5. A reconnect after corpse creation preserves the same worn/container state.
6. No post-corpse mutation is needed.
7. No live server inventory growth occurs before Bandit death.
