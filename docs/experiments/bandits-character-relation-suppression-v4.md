# Bandits character-relation suppression v4

## Result from coordinate target trace v3

The v3 trace established that `BanditUpdate.UpdateZombies()` and the coordinate-only pursuit path are not creating the remaining normal-zombie -> Bandit targets.

Across large update samples:

```text
acquiredDuringUpdate=0
controllerCreatedTargets=0
orderMisses=0
```

Bandit targets were observed already present at `ENTRY_TARGET`, before the normal zombie entered Bandits' update callback for that tick.

That moved the investigation to other Bandits action paths that can mutate ordinary zombies while a Bandit itself is being updated.

## Confirmed remaining source: gunshot alert

`ZombieActions/ZAShoot.lua` explicitly alerted nearby idle ordinary zombies using:

```lua
zombie:spottedNew(shooter, true)
zombie:addAggro(shooter, 1)
zombie:setTarget(shooter)
```

`shooter` is a Bandit represented by Java as `IsoZombie`. This reconstructs the exact vanilla character relationship removed earlier from `BanditUpdate.lua` and can feed B42 `AttackState`, whose player-oriented paths are unsafe for Bandit NPC targets.

v4 source-edits that block to preserve gunshot response without a character target:

```lua
zombie:pathToLocationF(sx, sy, sz)
```

The zombie still reacts to the shot location, but the Bandit is not installed as its vanilla target/aggro character.

## Remaining retaliation seams

Two additional upstream paths set the Bandit as the attacker of an ordinary zombie after damage:

- melee `ZombieActions.Smack` uses `victim:setAttackedBy(attacker)`;
- firearm `BanditUtils.Hit` uses `victim:setAttackedBy(shooter)`.

The actual hit simulation already uses fake engine actors where required, so retaining the Bandit as Java `attackedBy` is not necessary for the custom Bandits damage pipeline and may cause vanilla retaliation target acquisition between Lua update cycles.

For this experiment these internal seams are handled by:

```text
WorkshopPatches/Bandits-LCC-Dev/42.20/media/lua/client/zzzz_LCC_BanditRelationshipSuppression.lua
```

The file wraps the existing action functions rather than duplicating the large upstream sources.

After a **real damage event** against an ordinary zombie, the wrapper:

1. removes a current Bandit target if one was created;
2. clears `setAttackedBy(nil)` in the Bandit-caused hit context;
3. issues `pathToLocationF(attackerX, attackerY, attackerZ)` so the zombie still responds toward the attacker position.

Player targets are excluded. Bandit-vs-Bandit victims are excluded. The wrapper only watches an ordinary `IsoZombie` being damaged by a Bandit `IsoZombie`.

## Runtime marker

Correct load:

```text
[LCC][BanditsRelationPoC][BOOT] marker=character-relation-suppression-v4 gunshotAlert=coordinate-only meleeWrapped=true gunHitWrapped=true
```

First sanitation of a victim/attacker pair:

```text
[LCC][BanditsRelationPoC][SANITIZE] marker=character-relation-suppression-v4 source=melee|gun-hit victim=... attacker=... targetCleared=... attackedByCleared=... coordinateResponse=...
```

Periodic summary:

```text
[LCC][BanditsRelationPoC][SUMMARY] marker=character-relation-suppression-v4 shotCoordinateAlerts=... meleeChecks=... meleeDamageEvents=... gunChecks=... gunDamageEvents=... targetClears=... attackedByClears=... coordinateResponses=... sanitizeErrors=...
```

## Why the existing trace and guard remain enabled

`coordinate-target-trace-v3` stays enabled for this iteration so we can measure whether `ENTRY_TARGET` collapses after removing the newly identified sources.

`NPCCombatExperimental` also keeps its late trace-safety `setTarget(nil)` fallback. The intended result is that v4 prevents dangerous relationships early enough that the guard has little or nothing left to disconnect.

The guard remains a safety net, not the target architecture.

## Test matrix

1. Confirm all three boot markers load:
   - `BanditsAttackPoC` coordinate pursuit v2;
   - `BanditsAttackTraceV3`;
   - `BanditsRelationPoC` v4.
2. Fire weapons near groups of ordinary zombies with one or more Bandits present.
3. Let Bandits shoot ordinary zombies repeatedly.
4. Let Bandits fight ordinary zombies in melee repeatedly.
5. Keep ordinary zombie -> real player combat in the same session as a control.
6. Run long enough for multiple trace/guard/relation summaries.
7. Check for `AttackState.triggerPlayerReaction`, `ClassCastException`, `ESCAPED_ATTACK_STATE` and `NetworkZombieMind` noise.

## Success criteria

Strong evidence for v4 is:

- `shotCoordinateAlerts > 0` during gunfire tests;
- `meleeDamageEvents > 0` and/or `gunDamageEvents > 0` during Bandit combat;
- `sanitizeErrors=0`;
- `BanditsAttackTraceV3 acquiredDuringUpdate=0` remains true;
- `ENTRY_TARGET` and guard `traceSafetyDisconnects` fall sharply toward zero;
- no `ESCAPED_ATTACK_STATE`;
- no `AttackState.triggerPlayerReaction` / `ClassCastException`;
- ordinary zombies still move toward Bandit gunshots and retaliation locations;
- custom Bandits combat damage still works;
- ordinary zombie -> real player attacks remain normal;
- `NetworkZombieMind: goal character is not set` decreases if it was downstream of the removed target churn.

## Failure interpretation

- `ENTRY_TARGET` remains high while `shotCoordinateAlerts > 0`: at least one additional target source still exists outside the covered seams.
- `ENTRY_TARGET` clusters after melee/gun hits despite sanitation: `setAttackedBy` may schedule retaliation before the wrapper can clear it; the next step is direct source removal of those calls rather than same-callback cleanup.
- Zombies stop reacting to gunfire: coordinate sound response needs refinement, but the vanilla Bandit character target must not be restored merely for movement.
- Player combat changes: the wrapper scope is too broad and must be narrowed before any promotion.

This is still a controlled working-copy experiment. Do not promote it to the public compatibility patch until runtime confirms the early relationship suppression is both safe and behaviorally complete.
