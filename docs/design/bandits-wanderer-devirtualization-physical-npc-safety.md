# Bandits2 wanderer devirtualization vs physical NPC safety

Date: 2026-08-26
Target: Project Zomboid Build 42.20.x / Dedicated Server / Bandits2
Status: root cause confirmed; source-clean mitigation implemented; runtime retest pending

## Observed failure

A framework-owned essential NPC (Alexey, physical Bandits runtime `10944650`) was protected as stationary, non-combat and invulnerable, yet still died without a normal zombie-vs-Bandit combat trace.

The server log showed the death immediately after a Bandits wanderer group was devirtualized. The group center was approximately `(12570.295, 4259.559)` while Alexey was approximately `(12587.666, 4236.045)`, about `29.23` tiles away.

In the same frame the server-side death counter jumped from `18` to `536`, confirming a mass world cleanup rather than ordinary combat damage.

## Upstream root cause

`BanditServerWanderers.lua` devirtualizes a virtual group when a player comes within the configured contact range (`70`). Before spawning the group it iterates the complete server `cell:getZombieList()` and destroys every `IsoZombie` inside a radius of `30` around the group center:

```lua
zombie:setHealth(0)
zombie:clearAttachedItems()
zombie:changeState(ZombieOnGroundState.instance())
zombie:setAttackedBy(cell:getFakeZombieForHit())
zombie:die()
```

There is no exclusion for:

- physical Bandits;
- permanent Bandits;
- framework-owned NPCs;
- non-combat NPCs;
- invulnerable NPCs.

Bandits physical actors are also represented by `IsoZombie`, so the cleanup can directly kill an unrelated already-materialized NPC. `setInvulnerable(true)` cannot protect against this path because the upstream code explicitly sets health to zero and calls `die()`.

## Architectural conclusion

Physical Bandit identity must be respected at **world-maintenance boundaries**, not only at combat boundaries.

The following are separate policies:

1. zombie target eligibility;
2. Bandit combat/flee scheduling;
3. player hit target eligibility;
4. world cleanup / devirtualization eligibility.

An NPC can be correctly excluded from the first three and still be destroyed by a broad world-maintenance operation.

## Source-clean mitigation

NPCFixes adds:

`42/media/lua/server/zz_LCC_BanditWandererDevirtualizationGuard.lua`

Marker:

`wanderer-devirtualization-bandit-preservation-v1`

The guard does not copy or execute `BanditServerWanderers.lua` source. Instead it wraps the public server-side `BanditUtils.DistTo` seam used immediately before wanderer devirtualization.

The wrapper preserves the original distance unless all of the following are true:

1. the original distance is inside the Bandits wanderer contact range;
2. the first coordinate pair matches a current `GetBanditModData().Wanderers` group position;
3. the second coordinate pair matches an online player position;
4. an already-live physical Bandit is inside the upstream 30-tile purge circle.

When all conditions match, the wrapper returns exactly the contact-range boundary. Upstream uses `dist < contactRange`, so devirtualization is deferred for that update instead of running the untyped destructive purge.

This protects all already-materialized Bandits, not only Quest Framework NPCs, because the upstream failure is provider-wide.

## Expected runtime diagnostics

Boot:

```text
[LCC][NPCFixes][WandererDevirtGuard][BOOT] marker=wanderer-devirtualization-bandit-preservation-v1 mode=DEFER_ON_BANDIT_OVERLAP purgeRadius=30 contactRange=70 sourceClean=true
```

When a virtual group attempts to materialize over an existing Bandit:

```text
[LCC][NPCFixes][WandererDevirtGuard][DEFER] marker=wanderer-devirtualization-bandit-preservation-v1 ...
```

A `DEFER` event must not be followed by an Alexey `reason=death` event from the same wanderer materialization.

## Tradeoff

A virtual wanderer group may remain virtual for additional update cycles while its destructive 30-tile materialization circle overlaps a live Bandit. The group continues to exist and move according to Bandits world simulation. This is intentionally preferred over deleting an unrelated physical NPC.

## Acceptance test

1. Confirm the guard BOOT marker on dedicated server startup.
2. Keep Alexey materialized and alive.
3. Allow Bandits wanderer simulation to run normally.
4. If a `DEFER` marker occurs, verify Alexey remains alive and bound to the same logical NPC.
5. Verify normal wanderer groups still devirtualize when their purge circle contains no live Bandit.
6. Confirm no mass cleanup can produce an Alexey `reason=death` event.

Runtime acceptance remains pending until this behavior is observed in-game.
