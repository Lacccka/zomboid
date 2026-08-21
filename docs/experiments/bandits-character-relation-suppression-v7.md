# Bandits character-relation suppression v7

## Runtime result leading to v7

The `2026-08-21 15:04` client test kept the main safety result clean across three sessions:

```text
entryTargets=0
postTargets=0
targetLeaks=0
traceSafetyDisconnects=0
attackStateObserved=0
ClassCastException=0
LungeState zero-vector=0
```

At the same time `character-relation-suppression-v6` handled substantial real combat load. Final per-session examples included dozens of melee/gun damage events and `attackedByClears`, with `sanitizeErrors=0`.

However seven client errors remained:

```text
NetworkZombieMind: goal character is not set
```

The v6 counters showed:

```text
pfbCharacterGoalCancels=0
```

in every session. The errors appeared 0.5-4 seconds after Bandit gun/melee hit sanitation, not inside the hit callback itself.

## Build 42.20.3 explanation

`game_source/common-42.20.3/java/zombie/characters/NetworkZombieMind.java` serializes pathfinding separately from `IsoZombie.target`:

```java
if (pfb.getIsCancelled() || pfb.isGoalNone() || pfb.stopping || ...) {
    packet.pfb.goal = Goal.None;
} else if (pfb.isGoalCharacter()) {
    IsoGameCharacter character = pfb.getTargetChar();
    if (character instanceof IsoPlayer) {
        ...
    } else {
        packet.pfb.goal = Goal.None;
        DebugType.Multiplayer.error("NetworkZombieMind: goal character is not set");
    }
}
```

Therefore `zombie:getTarget()==nil` is not sufficient. A later Java/hit-reaction update may still leave `PathFindBehavior2.Goal.Character` pointing at a Bandit.

## v7 architecture

v6 hit sanitation remains unchanged.

A new file is loaded after the Bandits update/relation files:

```text
WorkshopPatches/Bandits-LCC-Dev/42.20/media/lua/client/zzzzz_LCC_BanditPfbLateSweep.lua
```

Marker:

```text
pfb-bandit-character-goal-late-sweep-v1
```

On each ordinary-zombie `OnZombieUpdate`, after the normal Bandits callbacks, it checks only:

```text
pfb:isGoalCharacter()
pfb:getTargetChar() is Bandit
```

and then performs:

```lua
pfb:cancel()
```

It does **not** change `IsoZombie.target`, `attackedBy`, bump state, or issue any replacement path. Normal coordinate-only Bandit pursuit remains responsible for movement on the next update.

Expected diagnostics:

```text
[LCC][BanditsPfbLateSweep][CANCEL]
[LCC][BanditsPfbLateSweep][SUMMARY]
characterGoals=...
banditCharacterGoals=...
cancels=...
cancelErrors=0
```

## Bite compatibility is now confirmed

The same runtime test proved that coordinate-only pursuit does not break the custom zombie -> Bandit bite pipeline.

The third session recorded:

```text
activeStarts=24
activeEnds=24
healthDropEvents=22
successfulWindows=22
noHealthDropWindows=2
shortWindows=0
maxActiveTicks=16
```

Detailed events showed `Bite` transitioning to `bumped` while `target=nil`, followed at about tick 15 by a real Bandit health decrease. This directly validates the manual `biteTab` / `bandit:Hit(...)` pipeline without restoring a vanilla Bandit target.

## Next test criteria

Strong v7 confirmation requires:

```text
BanditsPfbLateSweep.banditCharacterGoals > 0
BanditsPfbLateSweep.cancels > 0
BanditsPfbLateSweep.cancelErrors = 0
NetworkZombieMind = 0
entryTargets = 0
attackStateObserved = 0
ClassCastException = 0
LungeState zero-vector = 0
```

Custom bite should continue to show successful health-drop windows.
