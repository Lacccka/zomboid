# Split manifest

Source/reference package: `LaccckaCompatibilityPatch/Contents/mods/LaccckaCompatibilityPatch`.

The monolithic patch is not deleted or modified by this publication split. **Mirrored** files remain byte-for-byte copies of LCC-authored compatibility files in the monolith. **Split-owned refactors** intentionally diverge to remove third-party full-file/declaration copies or hard-failure compatibility paths while preserving behavior.

All projects below are **READY_FOR_UNLISTED_TEST**. Public Workshop titles are neutral LCC functional names; direct target/dependency names remain in descriptions/credits.

## Runtime projects

### LCCB4220FirearmsBridge — LCC B42.20 Firearms Placement Bridge
- `42/media/lua/server/BuildingObjects/ISPlace3DItemCursor_Fix.lua` — mirrored LCC wrapper.
- `42/media/lua/shared/LCC/Guard.lua` — mirrored.

### LCCB4220SVUTsarBridge — LCC B42.20 Vehicle API Bridge
- `42/media/lua/server/Tuning2/ATA2Tuning2.lua` — mirrored path shim.
- `42/media/lua/shared/LCC/Guard.lua` — mirrored.

### LCCB4220zReBridge — LCC B42.20 Vaccine API Bridge
- `42/media/lua/shared/BodyLocations.lua` — mirrored path shim.
- `42/media/lua/shared/LCC/Guard.lua` — mirrored.

### LCCB4220AegisGuard — LCC B42.20 Inventory Safety Guard
- `42/media/lua/client/zzz_LCC_AegisTransferGuard.lua` — mirrored wrapper.
- `42/media/lua/shared/LCC/Guard.lua` — mirrored.

### LCCB4220LegacyCallbacks
- `42/media/lua/shared/zzz_LCC_LegacyItemCallbacks.lua` — mirrored generic callback bridge.

### LCCB4220SkillDescriptionsRU
- `42/media/lua/shared/Translate/RU/ZZ_LCC_VanillaPerks_RU.txt` — mirrored LCC-authored vanilla/B42 descriptions.

### LCCB4220SurvivorAIStability — LCC B42.20 Survivor AI Stability

Mirrored LCC-authored files:
- `42/media/lua/client/ISUI/ISCharacterScreen.lua`.
- `42/media/lua/server/zzz_LCC_BanditsDedicatedServerGuard.lua`.
- `42/media/lua/shared/LCC/Guard.lua`.

Split-owned refactors:
- `zzz_LCC_BanditsZombieCacheGuard.lua` replaces the publication copy of upstream `BanditZombie.lua`.
- `zzz_LCC_BanditsFarmingGuard.lua` replaces publication copies of upstream `ZAStompPlant.lua` and `ZAWaterFarm.lua`. LCC precheck failures fail open to the installed original callbacks.
- `zzz_LCC_BanditsEmptyServerWandererGuard.lua` replaces the publication copy of upstream `BanditServerWanderers.lua` by guarding installed `BanditCustom.ClanGetAll()` only on an empty multiplayer server.

The empty-server guard uses `getOnlinePlayers()` on server state and intentionally does not require `isClient()`. CI forbids all four former upstream override files from returning to the split.

### LCCB4220WellnessCompat — LCC B42.20 Wellness Compatibility

Mirrored LCC-authored files:
- `zzz_LCC_LifestyleBathFix.lua`.
- `zzz_LCC_LifestyleYogaProgress.lua`.
- `Hygiene/BathTubFunctions.lua`.
- `Hygiene/ShowerFunctions.lua`.
- `LCC/Guard.lua`.

Split-owned files:
- `42/media/perks.txt` declares only the `Yoga` UI proxy, `parent = Lifestyle`, with zero proxy XP thresholds.
- `42/media/lua/client/zzy_LCC_LifestyleYogaContract.lua` validates after startup that `Perks.Yoga:getParent()` resolves to `Lifestyle`; only the LCC Yoga UI feature is disabled if this contract changes.

Authoritative Yoga level/XP remains in installed `HiddenSkills` storage. No wellness/hobby translations are bundled in this runtime item.

### LCCB4220PZKBridge — LCC B42.20 Vehicle Integration Bridge

Mirrored LCC-authored path shims:
- `client/Vehicle/ISUI/ISVehiclePartMenu.lua`.
- `client/Vehicle/ISVehiclePartMenu.lua`.
- `shared/ISBaseTimedAction.lua`.
- `server/utils/pzkZonesFunction.lua`.
- `shared/LCC/Guard.lua`.

Split-owned refactor:
- `shared/SVU3_PZKVLCCars_Stuffs.lua` now resolves the installed support module through `Guard.safeRequire()` and returns a safe fallback if that optional support path is removed/renamed. The monolithic raw-require version remains unchanged as the regression baseline.

No vehicle/source/assets from target mods are bundled.

### LCCB4220OutfitMenuSafety — LCC B42.20 Outfit Menu Safety
- `zzz_LCC_ChimeraGhillieFix.lua` — mirrored LCC runtime wrapper.
- `LCC/Guard.lua` — mirrored.

No target clothing/source assets are bundled.

## Translation projects

### LCCB4220BanditsRU — LCC B42.20 Survivor Dialogue RU

- `42/media/lua/shared/Translate/RU/IG_UI.json` — isolated RU survivor/NPC dialogue localization.

Content remains byte-for-byte identical to `3268487204/mods/Bandits/42.20/media/lua/shared/Translate/RU/IG_UI_ru.json`. It has its own active `workshop.txt`, empty Workshop ID, and `visibility=unlisted`.

### LCCB4220LifestyleRU — LCC B42.20 Wellness & Hobbies RU

The remaining wellness/hobby-oriented RU translation tree is isolated from runtime compatibility and from Survivor Dialogue RU.

42.x RU categories:
- `ContextMenu.json`
- `Farming.json`
- `IG_UI_RU.txt`
- `ItemName.json`
- `Mod.json`
- `Moodles.json`
- `Moveables.json`
- `Moveables_RU.txt`
- `Recipes.json`
- `Recorded_Media.json`
- `Sandbox.json`
- `Tooltip.json`
- `UI.json`
- `ZZ_LCC_Perks_RU.txt`

Common RU categories:
- `IG_UI.json`
- `Moveables.json`
- `Tooltip.json`

The Survivor Dialogue `42/.../IG_UI.json` is not present. The project has its own active `workshop.txt`, empty Workshop ID, and `visibility=unlisted`. The deprecated mixed `LCCB4220ThirdPartyRU` project has been removed and CI forbids its return.

## Not moved into Workshop projects

- `server/linux/start-server.sh` and other server tooling.
- Original Workshop source folders (`3217685049`, `3268487204`, etc.).
- Third-party models, textures, sounds, vehicle data, clothing assets, and original Lua source.
- Original monolithic `42/mod.info`; every split has its own Mod ID/load metadata.
