# Bandits B42.20.3 working-state handoff — 2026-08-21

Repository branch:
`agent/b42-20-compatibility-patch`

Primary working copy:
`WorkshopPatches/Bandits-LCC-Dev`

Captured/decompiled game runtime:
`game_source/common-42.20.3`

This file records the state required to continue the current Bandits compatibility investigation in a new chat without reconstructing the full history.

## High-level status

Three major Bandits issues have been under investigation:

1. ordinary zombie -> Bandit vanilla target relations causing `AttackState.triggerPlayerReaction` `IsoZombie -> IsoPlayer` `ClassCastException`;
2. persistent/reconnected Bandits losing real worn clothing and producing under-dressed corpses;
3. preserving Bandits' custom zombie -> Bandit `Bite/BiteLow` pipeline while removing unsafe vanilla character targeting.

Current result:

- the historical `AttackState` target crash path appears closed under combat load;
- custom `Bite/BiteLow` is confirmed to arm, enter `bumped`, and cause real Bandit health loss without a vanilla target;
- server-authoritative corpse clothing repair plus `BanditRemove` snapshot fallback produced complete corpse clothing for all 10 tested Bandit deaths;
- the only active combat tail is a small number of `NetworkZombieMind: goal character is not set` messages caused by a hidden `PathFindBehavior2 Goal.Character` state that is not caught by the current v6 sanitation callback timing.

## Confirmed Java-level facts from Build 42.20.3

### AttackState crash is structurally real

`AttackState.triggerPlayerReaction()` casts its target to `IsoPlayer` without a sufficient type guard in the relevant branch. A Bandit is Java `IsoZombie`, so a normal zombie entering vanilla `AttackState` with a Bandit target can crash with:

`IsoZombie cannot be cast to IsoPlayer`.

Therefore a Bandit must never be installed as the ordinary zombie's vanilla character target.

### NetworkZombieMind character-goal restriction

`game_source/common-42.20.3/java/zombie/characters/NetworkZombieMind.java` does:

- if PFB goal is `Character`, it only serializes it when `getTargetChar()` is `IsoPlayer`;
- otherwise it emits `NetworkZombieMind: goal character is not set`.

Thus `zombie:getTarget()==nil` is not sufficient. A stale `PathFindBehavior2 Goal.Character -> Bandit` also has to be eliminated.

### Coordinate pursuit is a true location goal

`PathFindBehavior2.pathToLocationF()` sets `Goal.Location` and the path completion logic approaches the endpoint to approximately 0.05 tiles. The custom-bite problem was therefore not simply 'location path stops before 0.8'.

### Corpse construction / serialization

The exact B42.20.3 lifecycle is:

`IsoZombie.DoZombieInventory()` -> `OnZombieDead` -> `DoDeath()` -> `new IsoDeadBody(died)`.

`IsoDeadBody` takes the dying zombie's inventory and copies its `WornItems`.

For durable save/load, every corpse worn item is serialized as an index into the corpse `ItemContainer`. The same `InventoryItem` therefore must be both:

- in the dying Bandit's inventory;
- in the dying Bandit's `WornItems`;

before `IsoDeadBody` is constructed.

## Current combat architecture

### Coordinate pursuit remains

`BanditUpdate.lua` marker:
`upstream-coordinate-pursuit-v2`

No active normal-zombie -> Bandit use of:

- `pathToCharacter(bandit)`;
- `setTarget(bandit)`;
- `spotted(bandit)`;
- `addAggro(bandit)`

remains in the main `UpdateZombies()` pursuit path.

Ordinary zombies pursue Bandit coordinates via `pathToLocationF()`.

### Gunshot alert source fixed

`ZombieActions/ZAShoot.lua` no longer does:

- `spottedNew(shooter, true)`;
- `addAggro(shooter, 1)`;
- `setTarget(shooter)`.

It alerts ordinary zombies using the shot coordinates instead.

### Relationship suppression v6

File:
`WorkshopPatches/Bandits-LCC-Dev/42.20/media/lua/client/zzzz_LCC_BanditRelationshipSuppression.lua`

Marker:
`character-relation-suppression-v6`

Important commit:
`6d4dfa3e1aa16fd1163327be281a75c246dae908`

The wrapper still sanitizes Bandit-caused melee/gun-hit retaliation by clearing unsafe:

- target(Bandit);
- attackedBy(Bandit).

v6 additionally checks `PathFindBehavior2` and cancels it if it is already `Goal.Character -> Bandit`.

It does NOT issue exact-attacker-coordinate `pathToLocationF()` after a hit.

### Latest combat runtime result — archive `ZomboidLogs_2026-08-21_15-14-31.zip`

Strongly confirmed:

- `targetLeaks = 0`;
- `traceSafetyDisconnects = 0`;
- `attackStateObserved = 0`;
- `ClassCastException = 0`;
- no `AttackState.triggerPlayerReaction` crash;
- no zero-vector `LungeState` error in this run.

However:

- `NetworkZombieMind: goal character is not set` occurred 7 times;
- v6 reported `pfbCharacterGoalCancels = 0`.

This means the hidden PFB `Goal.Character` is being created after the current same-callback sanitation check, or via a separate engine callback/path.

This is the exact point where investigation stopped.

### Next combat investigation

Do NOT restore vanilla Bandit targeting.

Find where `PathFindBehavior2 Goal.Character -> Bandit` is created after sanitation.

Relevant decompiled source already inspected:

- `NetworkZombieMind.java`;
- `IsoZombie.java` `pathToCharacter()` and spotting/pathing paths;
- `IsoGameCharacter.java`;
- `PathFindBehavior2.java`.

Likely next diagnostic: observe PFB goal state in a later `OnZombieUpdate` callback after all Bandits wrappers and correlate first transition from non-character to `Goal.Character` with state, attackedBy, target, and preceding Bandit gun/melee event. Cancel only confirmed Bandit PFB character goals after measurement.

## Custom bite status

### Close-range trace

File:
`WorkshopPatches/Bandits-LCC-Dev/42.20/media/lua/client/zz_LCC_BanditCloseRangeBiteTrace.lua`

Current marker:
`close-range-bite-trace-v2`

Important commit:
`faf3ec7b3fb9f61d80a5b3d7902e2aa31c16b6e1`

v1 over-counted `eligibleNotArmed` because it treated states such as `onground` as eligible even though real `BanditUpdate` returns before bite logic. v2 mirrors those early blockers.

### Bite outcome trace

Added observation-only outcome tracer.

Important commit:
`87a663709f0a4a2a0f788a5edc81200ab81fba10`

Latest runtime result:

- 24 observed bite windows in the final test session;
- 22 produced real Bandit health drops;
- successful windows reached the expected 16-tick lifetime.

Conclusion: the custom zombie -> Bandit bite pipeline is functional with `target=nil` and coordinate-only pursuit. It is no longer an unresolved architecture blocker.

## Clothing architecture

### Client live reconnect repair

File:
`WorkshopPatches/Bandits-LCC-Dev/42.20/media/lua/client/zz_LCC_BanditClothingRestore.lua`

Marker:
`real-worn-reconnect-v2`

Purpose only: restore live local worn state after reconnect so persistent Bandits are visibly/locally dressed.

The old string-body-location v1 was invalid and removed.

### Server primary death-boundary repair

File:
`WorkshopPatches/Bandits-LCC-Dev/42.20/media/lua/server/zz_LCC_BanditServerClothingRestore.lua`

Marker:
`server-authoritative-death-worn-v2`

Important commit:
`d96a4ccfa6157e1e6d54a2512f0faec3a43bab41`

It repairs clothing only in server `OnZombieDead`, after `DoZombieInventory()` and before `IsoDeadBody` construction.

It establishes the invariant:

same `InventoryItem` -> Bandit inventory + Bandit WornItems.

It does not mutate live inventory throughout the NPC lifetime.

### BanditRemove race fallback

Problem discovered: client Bandits sends `Commands/BanditRemove` when a Bandit dies. The server command can delete the cluster brain before server `OnZombieDead`, so the primary death repair can lose `brain.clothing`.

Fallback file:
`WorkshopPatches/Bandits-LCC-Dev/42.20/media/lua/server/zzz_LCC_BanditServerClothingSnapshotFallback.lua`

Marker:
`server-death-worn-remove-snapshot-v1`

Important commit:
`f58ed226c163da9de8838faeb4309847d4c9a111`

It wraps `BanditServer.Commands.BanditRemove` and snapshots only minimal scalar clothing data before cluster deletion:

- id;
- fullname;
- clothing;
- tint.

It performs no live item mutation. A fallback `OnZombieDead` uses the snapshot only when the primary repair did not already handle that Bandit.

### Latest clothing runtime result — archive `ZomboidLogs_2026-08-21_15-14-31.zip`

This test is the strongest confirmation so far:

- 10 Bandit deaths tested;
- 5 handled by primary server `OnZombieDead` repair;
- 5 handled through the `BanditRemove` snapshot fallback race path;
- server repair errors = 0;
- all 10 client-observed Bandit corpses ended with complete worn sets, generally `corpseWorn == expected` or `expected + 1` when a bag/additional wearable existed.

Conclusion: the clothing/corpse architecture is now strongly validated. Next step is mainly cleanup/promotion from PoC to final source-clean fix, not another conceptual redesign.

## Rejected clothing experiments

Do not revive:

- `real-worn-reconnect-v1` — String instead of ItemBodyLocation, caused thousands of errors;
- `real-worn-death-queue-v1` — queue counts succeeded but persistent corpses still lost clothing;
- `post-corpse-clothing-repair-v1` — too late and positional matching could target unrelated corpses;
- long-lived server-authoritative inventory-backed wear — unnecessary once exact death lifecycle was known.

## Important recent commits

- `d96a4ccfa6157e1e6d54a2512f0faec3a43bab41` — authoritative death-boundary clothing repair;
- `26235067bca6363f4880a04a3d88425601ff9d42` — clothing lifecycle docs;
- `337cdb6c26b7c2597ab20553b261daa0fc6b8be8` — initial close-range bite trace;
- `6d4dfa3e1aa16fd1163327be281a75c246dae908` — relationship suppression v6 with PFB cancellation attempt;
- `f58ed226c163da9de8838faeb4309847d4c9a111` — BanditRemove clothing snapshot fallback;
- `faf3ec7b3fb9f61d80a5b3d7902e2aa31c16b6e1` — close-range bite trace v2;
- `87a663709f0a4a2a0f788a5edc81200ab81fba10` — bite outcome trace.

There are additional documentation commits after these; inspect current branch HEAD rather than assuming one of these is the final HEAD.

## Existing attack diagnostics still useful

`NPCCombatExperimental` contains:

- `zzz_LCC_BanditsAttackStateGuard.lua`;
- `zzz_LCC_BanditsTargetDiagnostics.lua`;
- `zzz_LCC_BanditsAdminSpawnMenu.lua`.

The guard is now mainly a safety/verification layer. In clean runs it should not need to disconnect any target.

Bandits working-copy attack trace files also include pre/post target-origin tracing.

## Working workflow for Windows testing

After repository changes:

```powershell
cd C:\zomboid
git pull
```

Then delete the active runtime copy:

`C:\Users\user\Zomboid\mods\Bandits-LCC-Dev`

and recopy:

`C:\zomboid\WorkshopPatches\Bandits-LCC-Dev`

into the runtime mods directory.

Full client and dedicated-server restart is required.

`NPCCombatExperimental` only needs to be recopied when that module itself changes.

## Immediate next task

Continue from the 7 remaining `NetworkZombieMind: goal character is not set` messages in the latest 15:14 log archive.

The critical observation is:

- vanilla target relation remains absent;
- `pfbCharacterGoalCancels=0` in v6;
- therefore the unsafe PFB character goal is likely created after the existing gun/melee sanitation callback or elsewhere in engine processing.

Instrument a later observer around ordinary zombies to detect the first `PathFindBehavior2.isGoalCharacter()` whose `getTargetChar()` is a Bandit, record state/target/attackedBy/path target and recent Bandit combat correlation, then eliminate only that relation.

Do not redesign clothing or bite again unless new runtime evidence contradicts the latest successful tests.
