# LCC B42.20 Survivor AI Stability

Status: **BLOCKED_PENDING_PERMISSION**.

This split is now intentionally different from the monolithic regression package: it removes three redistributed Bandits source files and replaces them with LCC-owned post-load compatibility guards.

Publication-oriented implementation:

- `zzz_LCC_BanditsZombieCacheGuard.lua` cleans squareless/despawned zombie references after Bandits updates/rebuilds its caches; it does not replace `BanditZombie.lua`.
- `zzz_LCC_BanditsFarmingGuard.lua` wraps the already-installed `WaterFarm` / `StompPlant` callbacks with small B42 prechecks; the original Bandits callback is still called outside the LCC guard.
- `zzz_LCC_BanditsDedicatedServerGuard.lua` provides only the missing dedicated-server lookup contract.
- `ISCharacterScreen.lua` remains a tiny legacy-path shim.

One blocker remains: `BanditServerWanderers.lua`. Its failing `orchestrator` is local to the upstream file and is registered into `Events.EveryTenMinutes` by a local function reference. The current tested empty-server fix therefore still uses a full-file override. This project intentionally has no active `workshop.txt` until that last override is removed or permission is documented.

Do not enable this split together with `LaccckaB4220Compat` during runtime testing.
