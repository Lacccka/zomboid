# Bandits B42.20 Attack Bridge PoC

## Purpose

This is a controlled local experiment for the Bandits zombie -> NPC combat path on Build 42.20.3. The full test working copy lives in:

`WorkshopPatches/Bandits-LCC-Dev`

It is intentionally separate from the public `NPCCombatExperimental` Workshop package. The repository working copy may temporarily contain direct upstream Bandits edits while we establish a behavioral reference implementation.

## Established failure model

Bandits NPCs are Java `IsoZombie` objects with a Lua human/NPC overlay. Its custom zombie -> Bandit combat already provides its own target discovery, bite animation, damage, infection and health synchronization. Vanilla `AttackState` is therefore not required for zombie damage to Bandits.

The original close-range bridge in `UpdateZombies()` was:

```lua
zombie:spotted(bandit, true)
zombie:addAggro(bandit, 1)
zombie:setTarget(bandit)
zombie:setAttackedBy(bandit)
```

That relationship can reach vanilla `attack` / `attack-network`, where Build 42.20.3 can take an `IsoPlayer`-specific path with an `IsoZombie` Bandit and produce the known `ClassCastException` in `AttackState.triggerPlayerReaction`.

## Iteration 1 result: `upstream-pursuit-v1`

The first upstream PoC removed the four-call vanilla bridge but retained `zombie:pathToCharacter(bandit)` for pursuit.

Multiplayer testing showed that v1 improved the dangerous behavior but did **not** remove vanilla zombie -> Bandit targets:

- `upstreamPoc=true` was confirmed;
- `pocTargetLeaks` remained non-zero;
- `bAttack` still appeared;
- the first session still observed several Bandit-targeted `AttackState` entries;
- `NetworkZombieMind: goal character is not set` remained very noisy;
- target diagnostics observed stale Bandit targets at more than 60 tiles even though Bandits' own selection radius is about 20 tiles (`dist2max < 400`).

This made `pathToCharacter(bandit)` the next causal suspect: it passes the real Bandit `IsoZombie` into the Java/network character-goal path even when the explicit `setTarget()` bridge is gone.

## Current experiment: `upstream-coordinate-pursuit-v2`

The current working copy removes **all active `pathToCharacter(bandit)` calls from `UpdateZombies()`** in addition to keeping the original four-call bridge disabled.

The marker is:

```lua
LCC_BANDITS_ATTACK_BRIDGE_POC = "upstream-coordinate-pursuit-v2"
```

Runtime proof:

```text
[LCC][BanditsAttackPoC][INIT] upstream-coordinate-pursuit-v2 active; character pursuit and vanilla target bridge disabled
```

Zombie pursuit now goes to coordinates only:

```lua
local function PathZombieToBanditLocation(zombie, banditCached)
    if not zombie or not banditCached then return end
    if BanditUtils.IsController(zombie) then
        zombie:pathToLocationF(banditCached.x, banditCached.y, banditCached.z)
    end
end
```

The same `pathToLocationF(x, y, z)` pattern is already used by Bandits' own B42.20 movement actions. The v2 change therefore avoids inventing a new movement primitive while ensuring the path request does not receive the Bandit object as a character destination.

Both pursuit bands use that helper:

- farther than 3 tiles (`dist2max > 9`);
- the final approach from about 3 tiles down to custom bite range.

The following remain unchanged:

- nearest-Bandit discovery through `BanditZombie.CacheLightB`;
- visibility/range checks;
- `Bite` / `BiteLow` transition;
- `biteTab` timing;
- manual `bandit:Hit(...)`;
- infection update;
- health synchronization;
- multi-zombie death logic.

## Interaction with NPCCombatExperimental

`zzz_LCC_BanditsAttackStateGuard.lua` recognizes exactly:

```text
upstream-coordinate-pursuit-v2
```

While that marker is active, the guard is fully observation-only. It does **not** run the old v3 `zombie:setTarget(nil)` fallback and does not assert target-side `setZombiesDontAttack(true)` protection.

Required guard marker:

```text
[LCC][BanditsAttackGuard][UPSTREAM_POC_ACTIVE] marker=upstream-coordinate-pursuit-v2 mode=observe-only v3Disconnect=false targetProtection=false
```

Any line like:

```text
[LCC][BanditsAttackGuard][POC_TARGET_LEAK] ... intervention=false
```

is therefore evidence that v2 still allowed or retained a vanilla Bandit target; the guard intentionally does not repair it.

## Working-copy workflow

For normal testing, **do not run the applicator**. `WorkshopPatches/Bandits-LCC-Dev` is already the materialized current experiment.

After `git pull`, replace the local test copy completely:

```text
C:\zomboid\WorkshopPatches\Bandits-LCC-Dev
    ->
C:\Users\user\Zomboid\mods\Bandits-LCC-Dev
```

Refresh `NPCCombatExperimental` as well, because its guard marker must match the Bandits experiment.

`tools/Apply-BanditsAttackBridgePoC.ps1` remains only as a strict reproducibility/revert tool. It knows the audited clean state and the complete coordinate-pursuit-v2 state and refuses mixed/unknown source revisions.

`tools/Setup-LocalBanditsDev.ps1` mirrors the already-prepared working copy by default. `-NoAttackPoC` instead mirrors the clean repository upstream snapshot from `3268487204/mods/Bandits`.

## Test matrix

Use a **full dedicated-server and client restart**, then use freshly spawned Bandits and freshly encountered/spawned zombies so stale v1 network targets cannot contaminate the result.

1. Confirm both v2 runtime markers.
2. Spawn several Bandits with the existing LCC admin action.
3. Let single zombies acquire them from more than 3 tiles away.
4. Verify zombies close the final 3-tile gap rather than stopping/orbiting.
5. Verify `Bite` / `BiteLow` starts and completes and Bandit health decreases.
6. Test 2-3+ zombies against one Bandit and preserve the existing death behavior.
7. Keep a real player near the same zombies and verify ordinary zombie -> player attacks remain normal.
8. Run long enough to produce multiple independent engagements and compare `NetworkZombieMind` frequency against v1.

## Success criteria

A strong positive result requires:

- `[BanditsAttackPoC][INIT] upstream-coordinate-pursuit-v2 ...` appears;
- `[BanditsAttackGuard][UPSTREAM_POC_ACTIVE] marker=upstream-coordinate-pursuit-v2 ...` appears;
- guard summary reports `upstreamPoc=true`;
- ideally `pocTargetLeaks=0` for fresh v2 engagements;
- no `[POC_TARGET_LEAK]` for fresh zombie/Bandit pairs;
- no `DANGER_ATTACK_STATE` / `ESCAPED_ATTACK_STATE` for Bandit targets;
- no `ClassCastException` from `AttackState.triggerPlayerReaction`;
- `customBiteStart` and `customBiteEnd` advance;
- NPCs still take zombie damage/infection;
- zombies do not stall in the 0.8-3 tile band;
- `NetworkZombieMind: goal character is not set` drops substantially;
- ordinary zombie -> real-player combat remains normal.

## Failure interpretation

- **Fresh v2 target leaks remain:** `pathToCharacter(bandit)` was not the only target source. Investigate stale network mind state, another Bandits/engine target assignment, and the remaining object-facing calls (`isFacingObject` / `faceThisObject`) separately.
- **No target leaks, but final approach stalls:** coordinate-only pursuit supports the target-root hypothesis, but close steering/path cancellation must be refined without restoring a character target.
- **No target leaks, but custom bite stops:** inspect the transition from coordinate pathing to `Bite/BiteLow`; stop/cancel coordinate pathing only when `biteTab` starts.
- **AttackState occurs without a Bandit target:** a second vanilla state-transition mechanism exists independently of target creation.
- **v2 works completely:** treat this source edit as the behavioral reference implementation, then look for a source-clean LCC interception or propose a minimal upstream Bandits change rather than shipping the complete upstream file inside the public patch.

## Publication constraint

`WorkshopPatches/Bandits-LCC-Dev` is a private development/test working copy inside this repository workflow. The public `NPCCombatExperimental` Workshop package remains source-clean and must not include the complete upstream `BanditUpdate.lua`.
