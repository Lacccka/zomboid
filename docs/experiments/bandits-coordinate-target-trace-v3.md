# Bandits coordinate target trace v3

## Why this iteration exists

`upstream-coordinate-pursuit-v2` removed the original `spotted/addAggro/setTarget/setAttackedBy` bridge and both active `pathToCharacter(bandit)` calls. Runtime testing still produced fresh normal-zombie -> Bandit vanilla targets, including targets for NPCs created in the same session.

The next question is therefore not whether a target exists, but **when it first exists relative to Bandits' `UpdateZombies()` callback and the coordinate-path decision**.

## Runtime marker

The working Bandits copy keeps the v2 pursuit implementation and adds:

```text
[LCC][BanditsAttackTraceV3][PRE_BOOT] marker=coordinate-target-trace-v3 ...
[LCC][BanditsAttackTraceV3][POST_BOOT] marker=coordinate-target-trace-v3 ...
```

`NPCCombatExperimental` must report:

```text
[LCC][BanditsAttackGuard][TRACE_SAFETY_ACTIVE] marker=coordinate-target-trace-v3 mode=late-disconnect targetProtection=false
```

This is intentionally **not** an observation-only crash test. The trace runs before the guard, then the guard performs a late `zombie:setTarget(nil)` to prevent the known Java `AttackState` path. The target-side `setZombiesDontAttack(true)` mutation is disabled in this mode.

## Checkpoints

`000_LCC_BanditAttackTracePre.lua` registers before `BanditUpdate.lua` and records the target at entry to the normal-zombie update cycle.

It also wraps `BanditUtils.IsController(zombie)`, which `PathZombieToBanditLocation()` calls immediately before `zombie:pathToLocationF(x,y,z)`. This records target state immediately before and after the controller decision.

`zzz_LCC_BanditAttackTracePost.lua` registers after `BanditUpdate.lua` and records target state after Bandits has completed its update but before the compatibility guard clears an unsafe Bandit target.

The effective sequence is:

```text
PRE observer
  -> BanditUpdate / UpdateZombies
       -> before IsController
       -> after IsController
       -> pathToLocationF / remaining UpdateZombies logic
  -> POST observer
  -> NPCCombatExperimental late safety disconnect
```

## Important log records

```text
[ENTRY_TARGET]
```

The Bandit target already existed before Bandits' update callback for that tick.

```text
[ISCONTROLLER_CREATED_TARGET]
```

`BanditUtils.IsController()` itself changed a non-Bandit target state into a Bandit target. This is not expected but is measured explicitly.

```text
[ACQUIRED_DURING_UPDATE]
```

No Bandit target existed at entry, but a Bandit target exists after BanditUpdate. The line includes `controllerCalls`, `beforeControllerTarget`, `afterControllerTarget`, state, bump type and distance.

```text
[TRACE_SAFETY_DISCONNECT]
```

The measurement completed and the late guard removed the unsafe target.

## Summary fields

`BanditsAttackTraceV3` prints every 15 seconds:

- `entryTargets`: Bandit targets already present before BanditUpdate;
- `postTargets`: Bandit targets present after BanditUpdate;
- `acquiredDuringUpdate`: target absent at entry but present after BanditUpdate;
- `controllerCalls`: coordinate-path controller decisions observed;
- `controllerBeforeTargets` / `controllerAfterTargets`: Bandit targets around `IsController()`;
- `controllerCreatedTargets`: target appeared inside the wrapped `IsController()` call itself;
- `acquiredAfterControllerWindow`: no target at entry or around `IsController()`, but one exists after BanditUpdate;
- `orderMisses`: post observer had no matching pre snapshot. A valid test should keep this at zero or explain the misses.

## Interpretation

- High `entryTargets`: another engine/event/network path is creating or retaining the target before Bandits runs.
- High `acquiredDuringUpdate` with `controllerCalls=0`: target creation occurs elsewhere in the Bandits update path, not coordinate pursuit.
- High `acquiredAfterControllerWindow` with `controllerCalls>0`: `pathToLocationF()` or code after the coordinate-path decision is the next suspect.
- Non-zero `controllerCreatedTargets`: `IsController()` itself is unexpectedly mutating target state.
- `orderMisses>0`: do not draw causal conclusions until callback ordering is verified.

## Safety criteria

During this trace iteration:

- target-side protection must remain disabled;
- `TRACE_SAFETY_DISCONNECT` should remove every observed Bandit target;
- `disconnectFailures` should stay zero;
- `AttackState.triggerPlayerReaction` / `ClassCastException` must not occur;
- ordinary zombie -> real-player combat must remain unaffected.
