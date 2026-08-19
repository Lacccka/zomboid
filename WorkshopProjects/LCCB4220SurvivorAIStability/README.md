# LCC B42.20 Survivor AI Stability

Status: **TECHNICALLY_CLEAN_PERMISSION_REVIEW**.

The publication split no longer carries full Bandits Lua source files. The monolithic `LaccckaCompatibilityPatch` remains unchanged as the known regression baseline, while this project uses LCC-authored guards/shims that operate on the installed original mod.

Publication-oriented implementation:

- `zzz_LCC_BanditsZombieCacheGuard.lua` cleans squareless/despawned zombie references after Bandits updates/rebuilds its caches; it does not replace `BanditZombie.lua`.
- `zzz_LCC_BanditsFarmingGuard.lua` wraps the already-installed `WaterFarm` / `StompPlant` callbacks with small B42 prechecks; the original Bandits callbacks remain authoritative.
- `zzz_LCC_BanditsEmptyServerWandererGuard.lua` makes `BanditCustom.ClanGetAll()` return an empty temporary view only when the dedicated multiplayer server has zero online players. This makes the original local wanderer orchestrator skip the tick while its player-derived `day` value is unavailable; it does not replace `BanditServerWanderers.lua`.
- `zzz_LCC_BanditsDedicatedServerGuard.lua` provides only the missing dedicated-server lookup contract.
- `ISCharacterScreen.lua` is an LCC legacy-path shim to the B42.20 character-screen module.

The project still intentionally has no active `workshop.txt`. Source-redistribution blockers have been removed, but the user's prior interaction/research around Bandits permissions is restrictive enough that publication should stay disabled until the current author terms are reviewed/documented and this split passes runtime regression testing.

Do not enable this split together with `LaccckaB4220Compat` during runtime testing.
