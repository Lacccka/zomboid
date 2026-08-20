# Bandits B42.20 Attack Bridge PoC

## Purpose

This is a controlled local experiment for the Bandits zombie -> NPC combat path on Build 42.20.3. It is intentionally **not** a redistributable replacement of `BanditUpdate.lua` and is not part of the public Workshop package.

The experiment tests one concrete hypothesis from upstream source inspection:

> Bandits' custom zombie -> NPC combat does not need a vanilla zombie combat target. The dangerous `AttackState` path is introduced by the intermediate `spotted/addAggro/setTarget/setAttackedBy` bridge, while target discovery, pursuit, `Bite/BiteLow`, damage and infection already have separate Bandits logic.

## Upstream block under test

Current Bandits 42.20 uses this close-range bridge in `UpdateZombies()`:

```lua
zombie:spotted(bandit, true)
zombie:addAggro(bandit, 1)
zombie:setTarget(bandit)
zombie:setAttackedBy(bandit)
```

The Bandit object is still an `IsoZombie`. Previous runtime traces showed that this relationship can reach vanilla `attack` / `attack-network`, where Build 42.20.3 can follow an `IsoPlayer`-specific path and crash with the known `ClassCastException`.

The same function separately:

- finds NPCs through `BanditZombie.CacheLightB`;
- uses `pathToCharacter(bandit)` while the NPC is farther away;
- starts custom `Bite` / `BiteLow` through `biteTab` at close range;
- applies damage with `bandit:Hit(...)`;
- updates infection itself;
- synchronizes health itself.

This PoC therefore removes only the vanilla bridge and keeps explicit pursuit:

```lua
zombie:pathToCharacter(bandit)
```

No Java state is forced and `bAttack` is not written.

## Apply on the Windows test checkout

From the repository root:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\Apply-BanditsAttackBridgePoC.ps1
```

The script is strict and only patches the audited B42.20 source block. If the upstream file no longer matches exactly, it aborts instead of modifying an unknown revision.

To revert:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\Apply-BanditsAttackBridgePoC.ps1 -Revert
```

The patch changes the tracked upstream snapshot at:

`3268487204/mods/Bandits/42.20/media/lua/client/BanditUpdate.lua`

For a live test, copy/use that patched Bandits client source in the same local mod workflow used for the current B42.20 test build. Do not upload the modified upstream file as part of `NPCCombatExperimental`.

## Interaction with NPCCombatExperimental

The patched upstream block sets:

```lua
LCC_BANDITS_ATTACK_BRIDGE_POC = "upstream-pursuit-v1"
```

and prints once when the close-range path is first exercised:

```text
[LCC][BanditsAttackPoC][INIT] upstream-pursuit-v1 active; vanilla spotted/addAggro/setTarget/setAttackedBy bridge disabled
```

`zzz_LCC_BanditsAttackStateGuard.lua` detects the same marker and switches the v3 target-disconnect intervention to **observe-only**. This is required to keep the experiment causal: during PoC mode, `zombie:setTarget(nil)` from the compatibility patch must not help the source-level change.

Expected guard marker:

```text
[LCC][BanditsAttackGuard][UPSTREAM_POC_ACTIVE] marker=upstream-pursuit-v1 mode=observe-only v3Disconnect=false
```

If a normal zombie still acquires a Bandit as a vanilla target while the PoC marker is active, the guard reports:

```text
[LCC][BanditsAttackGuard][POC_TARGET_LEAK] ... intervention=false
```

That is a PoC failure signal and the guard intentionally does not clear the target.

## Test matrix

Use a full client restart after applying or reverting the source patch.

1. Spawn several NPCs with the existing LCC admin stress action.
2. Let single zombies pursue NPCs from more than 3 tiles away.
3. Verify they close the final 3-tile gap instead of stalling or orbiting.
4. Verify `Bite` / `BiteLow` still starts and NPC health decreases.
5. Test 2-3 zombies on one NPC and confirm the existing multi-zombie death logic still occurs.
6. Keep a real player near the same zombies and confirm normal zombie -> player attacks still work.
7. Drive the test long enough to produce multiple close-range engagements.

## Success criteria

A strong positive result requires all of the following:

- `[BanditsAttackPoC][INIT]` appears;
- `[BanditsAttackGuard][UPSTREAM_POC_ACTIVE]` appears;
- `POC_TARGET_LEAK=0` in practice (no leak lines and summary counter remains zero);
- target diagnostics stop reporting normal zombie -> Bandit vanilla target acquisitions;
- `DANGER_ATTACK_STATE` / `ESCAPED_ATTACK_STATE` for Bandit targets disappear;
- no `ClassCastException` from `AttackState.triggerPlayerReaction` occurs;
- Bandits custom `Bite` / `BiteLow` counters continue to increase;
- NPCs still take zombie damage / infection;
- zombies do not stall in the 0.8-3 tile pursuit band;
- ordinary zombie -> real player behavior remains normal.

## Failure interpretation

- **No target leaks, but zombies stall before biting:** the removed vanilla bridge was also being used as the final pursuit controller. The next PoC should refine explicit pathing/close-range path cancellation without restoring a Bandit vanilla target.
- **Custom bite stops:** inspect whether persistent `pathToCharacter` conflicts with `Bite/BiteLow`; the next iteration should stop/cancel pursuit only at the custom bite transition.
- **Bandit target still appears:** another code path is assigning the target and must be located before designing the source-clean hook.
- **AttackState still appears without a Bandit target:** the state can be entered from a second vanilla mechanism, so target creation was not the only trigger.
- **PoC works completely:** treat this source edit as the behavioral reference implementation, then reproduce it with an LCC-authored source-clean interception rather than bundling the modified upstream file.

## Publication constraint

This experiment is deliberately implemented as a local exact-block transformation. The public `NPCCombatExperimental` package remains source-clean and must not ship the complete upstream `BanditUpdate.lua`.
