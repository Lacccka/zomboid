# Bandits B42.20.3 combat freeze checkpoint — 2026-08-21

Branch: `agent/b42-20-compatibility-patch`

Runtime archive used for behavioral freeze: `ZomboidLogs_2026-08-21_17-08-24.zip`.

This checkpoint freezes the **behavioral architecture** of the current Bandits combat fix and records the first source-clean pursuit integration that must now pass a final regression run.

## Result

The combat architecture is behaviorally validated under stress load.

Confirmed across the latest regression run before source cleanup:

- `NetworkZombieMind: goal character is not set` = **0**;
- `ClassCastException` = **0**;
- `AttackState.triggerPlayerReaction` = **0**;
- attack guard `targetLeaks=0`, `attackStateObserved=0`, `bAttackObserved=0`;
- late PFB sweep `characterGoals=0`, `pfbCharacterStalls=0`;
- real MP controller loss during pursuit = **0**;
- no `walktoward` stalls;
- no stale Bandit location goals;
- custom `Bite/BiteLow` still causes real Bandit health loss;
- user visual observation in the preceding stress run: zombies remained active and selected targets normally, with no visible standing/frozen behavior.

## Why coordinate pursuit now works

`pathToCharacter(Bandit)` must stay forbidden because a Bandit is an `IsoZombie`, while vanilla zombie attack/network code assumes character targets are players in several paths.

The safe pursuit architecture is therefore:

- never install Bandit as ordinary zombie vanilla target;
- never create `PathFindBehavior2 Goal.Character -> Bandit`;
- pursue Bandit coordinates through `Goal.Location`;
- use the custom Bandits `Bite/BiteLow` pipeline at close range.

Build 42.20.3 `PathFindBehavior2.pathToLocationF()` calls `setData()`. `setData()` cancels the current path request and resets path state. Reissuing the same Bandit coordinates every `OnZombieUpdate` therefore caused pathfind starvation.

The coordinate pursuit throttle proved the fix: unchanged/aligned location goals are not reissued every frame. New location commands are allowed when the goal is missing/cancelled/non-location, when the Bandit has moved materially relative to the active destination, or during bounded idle recovery.

Historical stall comparison:

- before throttle: `pathfindStalls=64` in the targeted diagnostic run;
- first throttle validation: `pathfindStalls=0`;
- latest two-session regression: only 3 short (~2.5 s) pathfind observations across roughly one million ordinary-zombie updates, with `realController=true`, valid `Goal.Location`, no stale destination and no `walktoward` stalls.

This residual rate is not the former persistent cancel/restart defect and should not trigger another behavioral redesign without new visual/runtime evidence.

## Source-integrated pursuit — current HEAD

The temporary global `BanditUtils.IsController` wrapper has now been removed.

`BanditUpdate.lua` marker:

`upstream-coordinate-pursuit-v3`

`PathZombieToBanditLocation()` now owns the confirmed throttle logic directly:

1. require the real `BanditUtils.IsController(zombie)` result;
2. inspect the current `PathFindBehavior2`;
3. if an active `Goal.Location` destination is already within 0.75 tile of the Bandit's cached position and the Z level matches, do not reissue `pathToLocationF()`;
4. if the zombie is `idle`, allow bounded recovery no more often than once every 750 ms;
5. issue a new coordinate path immediately when the goal is cancelled, missing/non-location, or materially stale.

Important commits:

- `aed55ff31240cc3c654bb86ef241fb3b57de3a5f` — integrate throttle into `BanditUpdate.lua`;
- `fa244a8347b2b5f29d75ef5e9a50e830f3b6a8ac` — remove temporary `zzzzzz_LCC_BanditCoordinatePursuitThrottle.lua`;
- `22900c812a2cf27d5bbab24d466f2d5dfa4f582c` — update pursuit tracer for direct controller observation.

This is intentionally a behavior-preserving cleanup of the already validated algorithm, not a new AI experiment.

## Fake-hit zombie cleanup

Root cause of the remaining `NetworkZombieMind` warnings was identified as the engine helper returned by:

`getCell():getFakeZombieForHit()`

Bandits passes this helper to `victim:Hit(...)`. Hit reaction processing could temporarily install the helper as an ordinary zombie target and then produce an unserializable PFB `Goal.Character -> IsoZombie`.

Current exact cleanup marker:

`fake-hit-relation-cleanup-v3`

The cleanup only recognizes the exact cached engine fake-hit zombie reference. It does not clear arbitrary NPC/zombie character goals.

Latest regression:

- session 1: `fakeRelations=322`, `targetClears=322`, `fakeCharacterGoals=0`, `errors=0`;
- session 2: `fakeRelations=116`, `targetClears=116`, `fakeCharacterGoals=0`, `errors=0`;
- late sweep saw `characterGoals=0` in both sessions;
- `NetworkZombieMind` warnings = 0.

## Pursuit regression metrics

Latest session 1 final summary before source integration:

- updates: 838856
- pursuitCandidates: 23960
- actionableSamples: 19861
- realControllerFalseSamples: 0
- zombieStalls: 4
- pathfindStalls: 2
- walktowardStalls: 0
- idleStalls: 2
- turnAlertedStalls: 0
- pfbCharacterStalls: 0
- staleLocationStalls: 0

Latest session 2 final summary before source integration:

- updates: 208923
- pursuitCandidates: 2811
- actionableSamples: 1701
- realControllerFalseSamples: 0
- zombieStalls: 9
- pathfindStalls: 1
- walktowardStalls: 0
- idleStalls: 8
- turnAlertedStalls: 0
- pfbCharacterStalls: 0
- staleLocationStalls: 0

Do not compare these counts directly with the older v2 tracer because v3+ excludes normal reaction states such as `onground`, `getup`, `bumped` and `hitreaction`.

Current observation tracer marker after source integration:

`pursuit-stall-trace-v4`

It reads the controller directly from `BanditUtils.IsController`; there is no longer an exported/original-controller shim.

## Bite regression

Session 1:

- activeStarts=122
- activeEnds=122
- successfulWindows=112
- noHealthDropWindows=10
- shortWindows=0

Session 2:

- activeStarts=46
- activeEnds=46
- successfulWindows=44
- noHealthDropWindows=2
- shortWindows=0

The custom Bite pipeline remains functional without a vanilla Bandit target.

## Clothing note from this archive

Server clothing repair remained healthy (`errors=0`).

One client `CORPSE` diagnostic line reported `corpseWorn=7`, `expectedClothing=9` for Benjamin_Torres, but this is a diagnostic position-match false association, not a proven clothing loss:

- the real Bandit death had `preWorn=9` / `postWorn=9`;
- server fallback repaired it to `afterWorn=10`;
- the later client diagnostic used `match=position`, `matchDist=1.394` and inspected a corpse with completely unrelated clothing types.

Do not redesign the clothing repair because of that line. If corpse diagnostics are retained, positional fallback matching should eventually be tightened or treated as low-confidence.

## Frozen behavioral invariants

During cleanup, preserve all of these:

1. No `pathToCharacter(Bandit)` for ordinary zombies.
2. No vanilla `setTarget(Bandit)`, `spotted(Bandit)` or `addAggro(Bandit)` bridge.
3. Zombie -> Bandit pursuit remains `Goal.Location` based.
4. Do not reissue an already-aligned active location path every frame.
5. Keep bounded recovery for genuinely cancelled/missing/stale/idle paths.
6. Keep exact fake-hit-zombie relation cleanup.
7. Keep custom Bite/BiteLow close-range damage path.
8. Do not introduce broad cancellation of arbitrary living non-player `Goal.Character` targets; other NPC/animal mods may use them.

## Remaining PoC/diagnostic pieces

The global coordinate-pursuit wrapper is no longer present.

Remaining temporary diagnostics/safety layers include:

- `zzzzzz_LCC_BanditPursuitStallTrace.lua` (`pursuit-stall-trace-v4`);
- `zzzzz_LCC_BanditPfbLateSweep.lua` (diagnostic/safety tail);
- bite and attack diagnostic traces;
- NPCCombatExperimental attack-state/target guards used only for verification.

Do not remove these until the first runtime test of `upstream-coordinate-pursuit-v3` passes.

## Immediate next step

Run one final stress/regression test with the in-source pursuit implementation and **without** `zzzzzz_LCC_BanditCoordinatePursuitThrottle.lua`.

Required acceptance criteria:

- `NetworkZombieMind = 0`;
- `ClassCastException = 0`;
- `AttackState.triggerPlayerReaction = 0`;
- `targetLeaks = 0`;
- `pfbCharacterStalls = 0`;
- `realControllerFalseStalls = 0` for controlled pursuit candidates;
- no recurring/persistent `pathfind` or `walktoward` freeze;
- Bite remains functional;
- corpse clothing remains complete under server repair/fallback;
- visual behavior remains active without player-proximity wake-up.

If those hold, the pursuit cleanup is accepted. Then remove/reduce diagnostic-only layers and begin separating the minimal Bandits compatibility changes from the full experimental working copy into the final Lacccka B42.20 Compatibility Patch.
