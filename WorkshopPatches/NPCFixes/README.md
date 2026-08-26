# Lacccka B42 NPC Fixes

Stable, source-clean Build 42.20 compatibility fixes for Bandits2 NPC combat, provider scheduling and the NPC death/corpse lifecycle.

This Workshop item contains LCC-authored bridges, guards and lifecycle repairs only. It does **not** redistribute complete third-party Lua files or assets. `Bandits2` must be installed separately.

Current code version: **`1.0.5`**.

Version `1.0.5` removes the two runtime source transformers used by the earlier implementation. The package no longer provides its own files at either of these upstream module paths:

- `media/lua/client/BanditUpdate.lua`
- `media/lua/shared/ZombieActions/ZAShoot.lua`

The installed Bandits2 versions of those files now load normally and remain the authoritative implementation. NPCFixes reads the installed files with `getModFileReader()` only to verify exact compatibility fingerprints. The text is never compiled or executed.

The previous source-clean runtime behavior accepted on 2026-08-21 remains the regression baseline for combat/death/corpse behavior. Version `1.0.5` is a new compatibility candidate because the runtime-source-compilation mechanism itself had to be removed. It therefore requires a fresh in-game smoke test before being called runtime accepted.

## Why 1.0.5 changed architecture

The old `BanditUpdate.lua` and `ZAShoot.lua` shims read the user's installed Bandits2 source, patched exact strings in memory and executed the result through `loadstring()`.

That mechanism is no longer a valid Build 42.20 runtime contract. NPCFixes 1.0.5 therefore adapts public Bandits runtime APIs instead of compiling Lua source text.

The main bridge is:

`42/media/lua/client/zz_LCC_BanditCallbackBridge.lua`

Compatibility marker:

`loadstring-free-predicate-bridge-v2`

Despite the historical filename, this file does **not** introspect or replace private event callback lists. An early prototype attempted to use `LuaEventManager` internals, but Build 42's production Kahlua exposure does not expose `Event.callbacks` as a usable Lua field. That design was rejected before publication.

## Upstream fingerprint contract

The bridge still verifies the installed Bandits2 revision before installing invasive wrappers. It reads the upstream files as text and requires exact, unique fingerprints for the specific seams on which 1.0.5 depends.

For `BanditUpdate.lua` this includes:

- the consecutive `IsReanimatedForGrappleOnly()` / `IsRagdoll()` early-return seam;
- the unsafe ordinary-zombie `UpdateZombies()` block;
- `pathToCharacter(bandit)`;
- the close-range `spotted/addAggro/setTarget/setAttackedBy` block;
- `ManageCombat()` and `ManageCollisions()` dispatch;
- the collision `Bandit.GetTask()` gate;
- the untyped `brain.key -> setKeyId()` call.

For `ZombieActions/ZAShoot.lua` this includes:

- `ZombieActions.Shoot.onComplete`;
- the idle-zombie `spottedNew/addAggro/setTarget(shooter)` alert block;
- the later `BanditUtils.ManageLineOfFire()` call.

If those exact seams no longer match, the 1.0.5 bridge logs an upstream-validation error and does not claim that the new Bandits revision is supported. It does not heuristically rewrite unknown source.

## Ordinary zombie -> Bandit combat

Build 42 networking is unsafe when an ordinary `IsoZombie` is made to pursue a Bandit NPC, also represented as `IsoZombie`, through vanilla character-target relationships. In particular, a `PathFindBehavior2 Goal.Character -> Bandit` cannot be serialized as if it were a player target.

NPCFixes therefore keeps this relation coordinate-only.

### Predicate handshake

Upstream `OnBanditUpdate()` currently enters through these two consecutive public compatibility predicates:

```lua
if BanditCompatibility.IsReanimatedForGrappleOnly(zombie) then return end
if BanditCompatibility.IsRagdoll(zombie) then return end
```

NPCFixes wraps both predicates.

The first call arms a very short per-object probe. The immediately following `IsRagdoll()` call identifies the `OnBanditUpdate` entry without reading private `LuaEventManager` state.

The bridge then checks Bandits cluster authority through `GetBanditClusterData(id)`:

- if the cluster contains a Bandit brain, the original Bandits `OnBanditUpdate` continues normally;
- if the cluster contains a non-combat Quest Framework brain, the original Bandit update continues but one-tick task-generation gates are armed;
- if the cluster does **not** contain a Bandit brain, the second predicate returns `true` for that one `OnBanditUpdate` entry, so upstream exits before its unsafe `UpdateZombies()` character-target block.

A later LCC `OnZombieUpdate` handler then performs only the source-clean ordinary-zombie behavior required for Bandits interaction.

This handshake is deliberately different from globally hiding `BanditZombie.CacheLightB`: that cache is also the upstream Bandit activity/materialization gate and is used by other Bandits utilities, so globally replacing it would break unrelated behavior.

### Coordinate pursuit

The LCC ordinary-zombie handler selects the nearest live combat Bandit from the real Bandits caches while excluding:

- the current zombie itself;
- stale/non-Bandit cache entries;
- Quest Framework non-combat Bandits.

Movement is issued only through:

```lua
zombie:pathToLocationF(x, y, z)
```

It never intentionally constructs `Goal.Character -> Bandit`.

The pursuit retains the established anti-repath controls:

- alignment threshold: `0.5625` squared tiles (`0.75` tile);
- bounded idle retry: `750 ms`;
- Bandits controller ownership check;
- original broad-phase/radius constraints.

Before coordinate processing, the bridge also sanitizes any stale Bandit target, attacked-by relation or Bandit `Goal.Character` left by an earlier runtime state.

### Bite / drag-down lifecycle

The close-range Bandits zombie-vs-Bandit behavior remains functionally represented:

- facing check;
- `Bite` / `BiteLow` bump animation;
- delayed bite/scratch impact;
- Bandit visual splash;
- Bandit health/infection update and MP health sync;
- multi-zombie drag-down / terminal `Die` behavior.

This is a narrow compatibility reproduction of the unsafe `UpdateZombies()` path, not a copy of the complete third-party `BanditUpdate.lua` implementation.

### Bandit -> ordinary transition

Because the ordinary-zombie branch now exits before upstream reaches its local `Zombify()` helper, 1.0.5 performs the equivalent small state transition when an object still has the Bandit variable but no longer has a cluster brain. It also updates the existing Bandit light caches and counters so the object immediately returns to the ordinary-zombie cache side.

## Ordinary crawler -> player behavior

Upstream BanditUpdate has a small pre-`UpdateZombies()` special case that lets a crawling ordinary zombie lunge a nearby player.

Because the 1.0.5 predicate bridge exits ordinary zombies earlier, this behavior is preserved separately in:

`42/media/lua/client/zzz_LCC_BanditCrawlerPlayerLunge.lua`

Marker:

`ordinary-crawler-player-lunge-v1`

It is intentionally limited to the original public transition: a live ordinary crawler, a nearby visible player target, no blocking wall, correct facing, then `LungeState` plus path cancellation. It does not target Bandits.

## Non-combat Bandits scheduling

Bandits generic task generation runs before a custom `ZombieProgram`:

```text
GenerateTask
  -> ManageEndurance
  -> ManageHealth
  -> ManageCombat
  -> ManageCollisions
  -> custom ZombieProgram
```

`Bandit.ForceStationary()` alone does not prevent `ManageCombat()` from generating escape movement.

Quest Framework therefore marks its presentation NPC with the canonical brain flag:

```lua
brain.lccqNonCombat = true
```

The first materialization can also be identified from:

```lua
brain.program.name == "LCCQFQuestGiver"
```

For an active non-combat Bandit the predicate handshake arms a one-update task gate:

- the first `Bandit.IsSleeping(bandit)` call used by `ManageCombat()` returns `true`, causing generic combat to produce no task;
- the collision-specific first `Bandit.GetTask(bandit)` returns `nil` when a collision is actually present, preventing climb/breach/movement generation;
- the later `Bandit.GetTask()` used by normal `ProcessTask()` is not suppressed.

For crawling non-combat Bandits, `ManageCombat()` already exits before checking sleeping state; the gate starts directly at collision suppression.

The physical Quest Framework giver additionally remains protected by its provider program (`ForceStationary`, `ClearMoveTasks`, `setInvulnerable(true)`, `setShootable(false)`, `setIgnoreStaggerBack(true)`).

### Non-combat hit limitation to test

The old source transformer could insert an early return directly inside Bandits' local `OnHitZombie` callback. A source-clean runtime wrapper cannot replace that local callback through supported public event APIs.

1.0.5 therefore relies on the Quest Framework provider's physical `invulnerable` / `shootable=false` contract to prevent normal hit entry for presentation NPCs. This must be explicitly tested in the 1.0.5 smoke pass. The README does not claim that the old local hit-dispatch seam is still patched internally.

## Gunshot zombie alerts

The installed Bandits2 `ZombieActions.Shoot.onComplete` remains authoritative for firing, projectiles, sound, line-of-fire damage, shell/rack behavior and weapon state.

NPCFixes wraps that public function. During the original call only, `BanditZombie.CacheLightZ` is temporarily presented as empty, which makes the upstream character-alert loop a no-op. The original `BanditUtils.ManageLineOfFire()` still executes and obtains characters from world squares rather than from `CacheLightZ`.

After a successful shot, NPCFixes iterates the real saved zombie-light cache and sends eligible idle ordinary zombies to the **shot coordinates** with `pathToLocationF()`.

No `spottedNew(Bandit)`, `addAggro(Bandit)` or `setTarget(Bandit)` is required for the sound response.

## Death-key compatibility

Bandits2 can pass `brain.key` directly to `InventoryItem.setKeyId(int)`. Legacy integrations may supply a nonnumeric value.

1.0.5 guards the public `BanditBrain.Get()` result for dead Bandits and removes a nonnumeric key before the upstream death callback reaches `setKeyId()`. Numeric keys remain untouched.

This is intentionally narrow and exists only to prevent the Java type-conversion crash.

## Relationship and fake-hit sanitation

The existing stable relationship guards remain in the package.

Bandit melee/gun damage can leave an ordinary zombie with unsafe Bandit character relationships. The stable sanitation removes only Bandit targets/attacked-by/PFB character goals; it does not clear normal zombie -> player aggression globally.

Bandits gun damage also uses `getCell():getFakeZombieForHit()`. Build 42 can transiently promote that helper into an `IsoZombie.target` or a PFB character goal. NPCFixes retains both immediate and late cleanup layers for the exact fake-hit helper.

## Terminal drag-down death

Bandits can enqueue a locked `Die` task while the NPC is already `onground`. Upstream action-state handling can return before normal task processing, leaving the live downed Bandit stuck.

The terminal-Die pump still progresses only an **already existing** `Die` task for a live Bandit in the exact terminal state. It never invents arbitrary deaths.

## Clothing and corpse lifecycle

The existing clothing/corpse fixes are unchanged by the 1.0.5 architecture migration:

- client live/reconnect repair recreates typed real WornItems where Bandits visuals alone are insufficient;
- dedicated-server death-boundary repair ensures the same real item objects exist in inventory/WornItems before corpse construction;
- the `BanditRemove` snapshot fallback covers the race where cluster brain data disappears before server `OnZombieDead` resolves configured clothing.

## Source-clean contract

NPCFixes 1.0.5 must **not** contain complete upstream Bandits implementation files.

In particular these paths must be absent from the package:

- `lua/client/BanditUpdate.lua`
- `lua/shared/ZombieActions/ZAShoot.lua`
- `lua/client/BanditZombie.lua`
- Bandits server schedulers or assets.

Runtime source compilation is not part of the contract. Source reads are fingerprint validation only.

The split audit enforces the absence of same-path upstream files and the presence of the new predicate/task bridge markers.

## Relationship to other LCC packages

- `PatchCore` provides the preferred shared guard implementation and is strongly recommended.
- `RuntimeFixes` owns lower-level runtime/dedicated/cache compatibility. NPCFixes intentionally loads after it; the predicate bridge wraps the already-guarded `IsReanimatedForGrappleOnly` implementation.
- `QuestFramework` currently requires NPCFixes while Bandits2 is its physical NPC provider. Its compatibility check now expects `loadstring-free-predicate-bridge-v2` and `runtimeTransform=false`.
- `NPCCombatExperimental` remains diagnostics/admin stress tooling only and is not required for normal play.
- `Bandits-LCC-Dev` is an internal repository research/test tree, not a Workshop dependency.

## Expected 1.0.5 boot markers

A compatible client should report the predicate bridge boot line containing:

```text
[LCC][NPCFixes][PredicateBridge][BOOT] marker=loadstring-free-predicate-bridge-v2 mode=PREDICATE_BRIDGE source=Bandits2 runtimeTransform=false bundledUpstream=false pursuit=coordinate-only nonCombatProgram=LCCQFQuestGiver
```

Crawler preservation should also report:

```text
[LCC][NPCFixes][CrawlerLunge][BOOT] marker=ordinary-crawler-player-lunge-v1
```

Quest Framework, when enabled, should report the matching scheduling seam as available.

Any line like:

```text
[LCC][NPCFixes][PredicateBridge][ERROR] ... upstream validation failed ...
```

means the installed Bandits2 revision no longer matches the accepted seam contract and must be reviewed before compatibility is claimed.

There is no `BYPASS_COMPILE` mode in 1.0.5 because no runtime source compilation is performed.

## 1.0.5 runtime smoke contract

The repository/static pass is not a substitute for a live Build 42.20 MP test. Before publishing 1.0.5 as runtime accepted, verify at minimum:

1. Client boot has the expected predicate/crawler markers and no runtime-source-compilation error.
2. A normal Bandit materializes and continues normal movement/combat/task processing.
3. Removing a Bandit from cluster authority returns the physical object to ordinary-zombie state without stale Bandit cache state.
4. An ordinary zombie can pursue a combat Bandit at long and short range without `Goal.Character -> Bandit`, `NetworkZombieMind: goal character is not set`, `AttackState.triggerPlayerReaction` or IsoZombie/IsoPlayer cast errors.
5. Close zombie bite/drag-down still damages/kills a combat Bandit and health synchronization remains correct in MP.
6. A crawling ordinary zombie can still lunge/attack a nearby player normally.
7. A Bandit gunshot attracts idle ordinary zombies to the shot coordinates while real projectile/line-of-fire damage still works.
8. The Quest Framework giver is never selected by the LCC zombie-vs-Bandit selector, receives no generic `Run` escape task, and collision handling does not take over its movement.
9. The Quest Framework giver remains unshootable/invulnerable in normal player combat and `[E]` interaction/dialogue still works.
10. A Bandit with a legacy nonnumeric `brain.key` can die without the `setKeyId(int)` conversion error.
11. Normal zombie -> player combat remains unchanged, including crawler close-range behavior.
12. Existing terminal-Die, clothing, corpse and reconnect regression checks remain clean.

## Publication state

`1.0.5` is the current **static compatibility candidate** for the imported 2026-08-26 Bandits2 source and the Build 42.20 runtime without the old source-compilation dependency.

Do not treat 1.0.5 as runtime accepted until the smoke contract above has been exercised against the actual client/server runtime and logs reviewed.
