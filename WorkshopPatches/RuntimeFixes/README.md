# Lacccka B42 Runtime Fixes — Bandits contract

This Workshop staging project contains LCC-authored compatibility hooks only. It must not contain complete Bandits Lua files or assets.

In-development NPC combat AttackState work, target diagnostics, and the admin stress-test spawner are intentionally isolated in `WorkshopPatches/NPCCombatExperimental` and are not part of this stable item.

Validated against the repository snapshot at `3268487204/mods/Bandits/42.20` (`Mod ID: Bandits2`) and the known B42.20 dedicated/client failures observed by this server project.

## 1. Empty dedicated server wanderer scheduler

Upstream `BanditServerWanderers.lua` derives `day` from a randomly selected online player in multiplayer. With zero online players, `day` remains nil and the later day-range comparison throws.

LCC does not replace the scheduler. `zzz_LCC_BanditsEmptyServerWandererGuard.lua` wraps `BanditCustom.ClanGetAll()` and returns a fresh empty table only while the runtime is a multiplayer server with zero online players. The existing scheduler therefore performs an empty iteration for that tick. Normal clan data is returned unchanged as soon as a player is online.

The other server-side `ClanGetAll()` consumer in the inspected Bandits spawner already returns before requesting clan data when no player exists, so the empty-server view does not change its active-player path.

## 2. Squareless/despawned zombie lifecycle

Upstream `BanditZombie.lua` caches `IsoZombie` references and `BanditUpdate.lua` consumes them. B42.20 MP can transiently deliver an object after its square is gone.

`zzz_LCC_BanditsZombieCacheGuard.lua` uses two layers:

1. wrap the global `BanditCompatibility.IsReanimatedForGrappleOnly()` predicate; `BanditUpdate` already calls this predicate before its main zombie/bandit AI work, so a squareless object can exit through an existing upstream early-return seam;
2. remove squareless objects from Bandits' raw/light caches after `OnZombieUpdate` and sweep again after the periodic cache rebuild.

The second layer is required because `BanditZombie.lua` localizes the original compatibility predicate when it loads, so changing the global function later does not change that already-captured local reference.

## 3. Farming actions

`zzz_LCC_BanditsFarmingGuard.lua` does not replace `ZAWaterFarm.lua` or `ZAStompPlant.lua`. It requires the installed callbacks and wraps only the affected entry points.

Watering is skipped cleanly when the inventory item is missing/not drainable/not one of the item types handled by Bandits, or when `CFarmingSystem.instance` is unavailable at completion. The completion wrapper also returns true for an invalid item so Bandits' `ProcessTask` can remove the completed task instead of repeatedly calling a callback that returns nil.

StompPlant only short-circuits completion when `CFarmingSystem.instance` is unavailable. In all normal states the original callback is called unchanged.

## 4. Dedicated `BanditZombie.GetInstanceById`

`BanditServerCommands.lua` calls the client-owned `BanditZombie.GetInstanceById()` API on dedicated server and then conditionally calls `Bandit.UpdateItemsToSpawnAtDeath()`.

Bandits also contains `BanditServerZombie.lua`, but its cache updater rebuilds a map by scanning the complete server `getZombieList()` every eight ticks and the upstream `Events.OnTick.Add(UpdateZombieCache)` registration is disabled. LCC deliberately does not re-enable or reproduce that global scan.

Instead, `zzz_LCC_BanditsDedicatedServerGuard.lua` maintains an O(1) registry containing only live Bandits. A server Bandit is registered when it already passes through the existing `Bandit.ApplyVisuals()` or `Bandit.UpdateItemsToSpawnAtDeath()` paths, where a live `IsoZombie` reference and brain are already available. Both the raw persistent-outfit ID and `BanditUtils.GetZombieID()` representation are indexed because Bandits uses both forms in different paths.

`GetInstanceById()` reads only that registry. It may also consume `BanditServerZombie.Cache` if a future Bandits release starts populating it, but LCC never starts the disabled cache updater. Missing entries return nil and skip the optional death-item refresh rather than falling back to a full zombie-list traversal. Stale/squareless references are removed lazily and by a once-per-minute sweep over the Bandit-only registry; `BanditRemove` and `BanditFlush` commands also purge entries.

## 5. Legacy character-screen path

`client/ISUI/ISCharacterScreen.lua` is a tiny LCC path shim. It redirects the old module path to the B42.20 `XpSystem/ISUI/ISCharacterScreen` module and contains no upstream character-screen source.

## Regression checklist

Before public Workshop visibility, test without the monolithic `LaccckaB4220Compat` enabled:

- start dedicated server and leave it empty for at least two `EveryTenMinutes` scheduler ticks; no wanderer `__le` / nil-day error;
- join, leave, and rejoin; wanderer scheduling resumes with real clan data after players return;
- drive/devirtualize through populated areas and exercise Bandits combat; no squareless `getSquare()` crash and no persistent stale BanditZombie cache growth;
- allow NPC farming watering/stomping actions; valid actions still execute and invalid/transient states complete without repeated task errors;
- exercise Bandit state updates that reach `BanditUpdatePart`; dedicated server no longer throws on missing `BanditZombie`, no complete server-zombie scan occurs, and registered live Bandits retain death-item synchronization;
- check for a one-time `bandits.dedicated-zombie-lookup:miss` warning; a miss is safe, but repeated gameplay cases that depend on an unregistered live Bandit should be investigated;
- verify the grouped audit rejects any reintroduced `BanditZombie.lua`, `BanditServerWanderers.lua`, `ZAWaterFarm.lua`, `ZAStompPlant.lua`, or global `getZombieList()` fallback under this project.
