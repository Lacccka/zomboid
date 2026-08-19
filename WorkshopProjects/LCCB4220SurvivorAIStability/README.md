# LCC B42.20 Survivor AI Stability

Status: **READY_FOR_UNLISTED_TEST**.

Unofficial independent compatibility patch for `[B42] Bandits NPC` by Slayer. The original `Bandits2` Workshop mod remains a required separate dependency; this project contains no Bandits Lua source, assets, models, textures, sounds, or translations.

Publication-oriented implementation:

- `zzz_LCC_BanditsZombieCacheGuard.lua` cleans squareless/despawned zombie references after Bandits updates/rebuilds its caches; it does not replace `BanditZombie.lua`.
- `zzz_LCC_BanditsFarmingGuard.lua` wraps the already-installed `WaterFarm` / `StompPlant` callbacks with B42 prechecks; the original callbacks remain authoritative.
- `zzz_LCC_BanditsEmptyServerWandererGuard.lua` makes `BanditCustom.ClanGetAll()` return an empty temporary view only when a dedicated multiplayer server has zero online players; it does not replace `BanditServerWanderers.lua`.
- `zzz_LCC_BanditsDedicatedServerGuard.lua` provides only the missing dedicated-server lookup contract.
- `ISCharacterScreen.lua` is an LCC legacy-path shim to the B42.20 character-screen module.

The monolithic `LaccckaCompatibilityPatch` remains unchanged as the known regression baseline. Do not enable it together with this split during A/B testing.

Before public visibility, run the dedicated-server/client regression checklist and keep the Workshop description/Required Items relationship explicit.
