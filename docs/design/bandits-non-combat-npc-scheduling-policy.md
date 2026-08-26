# Bandits2 non-combat NPC scheduling policy

Date: 2026-08-26
Target: Project Zomboid Build 42.20.3 / Dedicated Server / Bandits2 physical NPC provider
Status: implemented statically; runtime smoke test still required

## Problem

A framework NPC may be logically stationary, essential and non-combat while its physical representation is still a Bandits2 `IsoZombie`.

Using only a custom `ZombieProgram` is insufficient to enforce that policy. In current Bandits2, generic zombie-vs-bandit targeting and generic bandit combat scheduling happen before the custom program is invoked.

Observed runtime symptom:

- ordinary zombies pursue the quest giver;
- the quest giver remains alive due to invulnerability;
- when outnumbered, the quest giver receives a real `Move`/`Run` escape task and leaves its interaction anchor.

The 2026-08-26 acceptance log showed one runtime moving from approximately `(10712.41, 10622.55)` to `(10707.15, 10595.69)` in under twenty seconds. This was AI movement, not normal shove displacement.

## Bandits2 scheduling order

For a materialized Bandit, the relevant client path is effectively:

```text
OnZombieUpdate
  -> BanditZombie cache update
  -> BanditUpdate.OnBanditUpdate
       -> UpdateZombies              # ordinary zombie chooses Bandit prey
       -> ManageActionState
       -> ManageSocialDistance
       -> GenerateTask
            -> ManageEndurance
            -> ManageHealth
            -> ManageCombat          # can create Run/Move escape task
            -> ManageCollisions      # can create movement/collision tasks
            -> custom ZombieProgram  # too late to veto the above
       -> ProcessTask
```

Therefore:

> A custom Bandits `ZombieProgram` is a role/task provider, not a firewall around generic Bandits combat scheduling.

## Why `ForceStationary` was insufficient

Current Bandits2 `Bandit.ForceStationary(zombie, stationary)` only writes:

```lua
brain.stationary = stationary
```

It does not prevent `ManageCombat` from generating a move task.

When `ManageCombat` sees a sufficiently unfavorable local fight, it uses the escape branch and creates a `Run` movement task. That branch does not consult `brain.stationary`.

Thus the invariant `stationary == true` cannot by itself mean `must never receive a movement task`.

## Why deleting the NPC from `CacheLightB` was wrong

An earlier experiment removed the quest giver from `BanditZombie.CacheLightB` after its custom program ran.

This was structurally incorrect for two reasons:

1. `BanditZombie` repopulates every materialized Bandit into `CacheLightB` on zombie update, before `BanditUpdate` runs.
2. `BanditUpdate.OnBanditUpdate` itself uses presence in `CacheLightB` as an activity/materialization gate. If the physical Bandit is absent from that cache, Bandits marks it useless and returns before the custom program can execute.

`CacheLightB` therefore represents more than "combat targets" and must not be used as the framework's materialization switch.

## Correct separation

The physical quest giver remains a normal materialized Bandits runtime object:

```text
BanditZombie.Cache       yes
BanditZombie.CacheLightB yes
Bandit brain             yes
framework runtimeId      yes
framework npcId          yes
custom ZombieProgram     yes
```

But it is excluded from selected generic combat decisions before those decisions create tasks:

```text
ordinary zombie victim selection   veto
Bandits ManageCombat               veto
Bandits collision movement         veto
Bandits OnHitZombie reaction       veto
custom LCCQFQuestGiver program     allowed
```

This gives us the required distinction:

> Materialized Bandits runtime != participant in Bandits combat simulation.

## Canonical provider flag

The canonical provider-level policy is:

```lua
brain.lccqNonCombat = true
```

The current quest giver is also identifiable before physical/client-side policy convergence by:

```lua
brain.program.name == "LCCQFQuestGiver"
```

The program-name fallback is important during first materialization because the server-created Bandits cluster already contains the desired program before the shared quest-giver program gets its first update.

Physical presentation markers remain namespaced:

```text
modData.lccqIgnoreZombieAggro = true
variable LCCQFNonCombat = true
```

They are not the authoritative scheduling decision.

## NPCFixes scheduling seam

`Lacccka B42 NPC Fixes` owns the source-clean `BanditUpdate.lua` runtime transformation and now exposes the correct pre-program seam.

Marker:

```text
source-clean-coordinate-pursuit-v4
```

The transform adds four non-combat gates:

1. `non-combat-victim-selection`
   - ordinary zombies skip non-combat Bandits while selecting nearest Bandit prey;
2. `non-combat-combat-dispatch`
   - non-combat Bandits do not enter generic `ManageCombat`;
3. `non-combat-collision-dispatch`
   - generic collision handling cannot generate movement/climb/breach tasks for the role;
4. `non-combat-hit-dispatch`
   - Bandits-specific hit reaction/task mutation is skipped for non-combat Bandits.

Existing B42.20 NPCFixes seams remain intact, including coordinate-only pursuit and the typed Bandits death-key guard.

## Quest Framework responsibility

Quest Framework remains responsible for logical role policy and physical safety:

- `brain.lccqNonCombat = true`;
- `brain.stationary = true`;
- desired program reconciliation;
- clearing move tasks as defensive cleanup;
- invulnerability;
- non-shootable state when the exposed runtime supports it;
- stagger/knockdown recovery;
- framework-owned `npcId` and interaction anchor.

It does not directly modify Bandits generic combat code. That provider-specific scheduling seam remains in `Lacccka B42 NPC Fixes`.

Because the current Bandits2 adapter needs that seam, `LaccckaQuestFramework` explicitly requires both:

```text
Bandits2
LaccckaB4220NPCFixes
```

A future NPC provider does not inherit this requirement automatically; this is a property of the current Bandits adapter.

## Rejected approaches

### Engine cheat flag through `getCheats()`

Rejected. `PlayerCheats` is Java userdata and its internal `set(CheatType, ...)` is not a supported Kahlua method surface. Attempting it produced:

```text
attempted index: set of non-table: zombie.characters.PlayerCheats
```

### Public `setZombiesDontAttack(true)`

Not a general NPC solution in B42.20.3 because the public setter is capability-gated and clears the cheat when the character lacks the required capability.

### Post-target `zombie:setTarget(nil)` loop

Rejected as the main policy. It runs after acquisition, can leave attack/network state already in progress, and treats the symptom rather than preventing victim selection.

### Periodic teleport to home position

Rejected as the primary stationary mechanism. It hides incorrect AI scheduling, causes visible snapping and can interfere with multiplayer interpolation. Home-anchor correction may still be a last-resort drift guard later, but generic movement must first be prevented at source.

## Runtime acceptance still required

Static audit success does not prove the physical behavior in a live multiplayer session.

Required smoke test:

1. Client log must show `source-clean-coordinate-pursuit-v4 mode=PATCHED`.
2. There must be no `require("BanditZombie") failed` from `LCCQFBanditsQuestGiverProgram.lua`.
3. Spawn/materialize Alexey and record his initial anchor.
4. Bring several ordinary zombies within close range.
5. Zombies must not path toward or start the Bandits bite sequence against Alexey.
6. Alexey must not receive a multi-tile flee/run path.
7. Small physics displacement from a direct shove is acceptable for this smoke test; sustained AI movement is not.
8. Shooting/hitting must not cause Bandits-specific flee/task mutation.
9. `[E]` interaction and dialogue must remain functional.
10. If the shim logs `BYPASS_FINGERPRINT`, stop the test: the installed Bandits source differs from the audited upstream shape and the corresponding seam must be updated first.

## Long-term invariant

For every future Bandits-backed framework NPC role, combat participation must be an explicit provider policy. Do not infer it from `stationary`, `hostile`, quest-giver status, or logical framework identity.

Recommended conceptual provider flags:

```text
movementPolicy   = STATIC | AI | ESCORT | PATROL
combatPolicy     = NONE | DEFENSIVE | FULL
zombieTargetable = false | true
playerTargetable = false | true
essential        = false | true
```

The current `lccqNonCombat` flag is the first narrow implementation of that future policy model.
