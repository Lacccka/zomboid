# Bandits B42.20.3 combat freeze checkpoint — 2026-08-21

Branch: `agent/b42-20-compatibility-patch`

Runtime archive used for this checkpoint: `ZomboidLogs_2026-08-21_17-08-24.zip`.

This checkpoint freezes the **behavioral architecture** of the current Bandits combat fix before source cleanup / promotion into the final Compatibility Patch.

## Result

The current combat architecture is considered behaviorally validated under stress load.

Confirmed across the latest regression run:

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

The temporary coordinate pursuit throttle proved the fix: unchanged/aligned location goals are not reissued every frame. New location commands are allowed when the goal is missing/cancelled/non-location, when the Bandit has moved materially relative to the active destination, or during bounded idle recovery.

Historical stall comparison:

- before throttle: `pathfindStalls=64` in the targeted diagnostic run;
- first throttle validation: `pathfindStalls=0`;
- latest two-session regression: only 3 short (~2.5 s) pathfind observations across roughly one million ordinary-zombie updates, with `realController=true`, valid `Goal.Location`, no stale destination and no `walktoward` stalls.

This residual rate is not the former persistent cancel/restart defect and should not trigger another behavioral redesign without new visual/runtime evidence.

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

Latest session 1 final summary:

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

Latest session 2 final summary:

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

Do not compare these counts directly with the older v2 tracer because v3 excludes normal reaction states such as `onground`, `getup`, `bumped` and `hitreaction`.

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

## Temporary PoC pieces still to clean

Current temporary files include:

- `zzzzzz_LCC_BanditCoordinatePursuitThrottle.lua` (`coordinate-pursuit-throttle-v2`);
- `zzzzzz_LCC_BanditPursuitStallTrace.lua` (`pursuit-stall-trace-v3`);
- `zzzzz_LCC_BanditPfbLateSweep.lua` (diagnostic/safety tail);
- bite and attack diagnostic traces.

The throttle currently wraps `BanditUtils.IsController` only as an experimental interception point. This is **not** the desired final architecture.

## Next implementation step

Promote the confirmed throttle logic directly into the local `PathZombieToBanditLocation()` helper in `BanditUpdate.lua`, preserving the same thresholds/behavior, then remove the global `BanditUtils.IsController` throttle wrapper.

After that source cleanup, perform one final regression run using this checkpoint as the baseline. Required acceptance criteria:

- NetworkZombieMind = 0
- ClassCastException = 0
- AttackState.triggerPlayerReaction = 0
- targetLeaks = 0
- pfbCharacterStalls = 0
- no recurring/persistent pathfind or walktoward freeze
- Bite remains functional
- corpse clothing remains complete under server repair/fallback

Once those hold after cleanup, the combat fix can be separated from the experimental Bandits working copy and promoted into the final Lacccka B42.20 Compatibility Patch.
