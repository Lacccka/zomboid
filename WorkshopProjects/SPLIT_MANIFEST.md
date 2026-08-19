# Split manifest

Source/reference package: `LaccckaCompatibilityPatch/Contents/mods/LaccckaCompatibilityPatch`.

The monolithic patch is not deleted or modified by this publication split. Files marked **mirrored** remain byte-for-byte copies of LCC-authored compatibility files in the monolith. Files marked **split-owned refactor** intentionally diverge to remove third-party full-file/declaration copies while preserving the compatibility contract.

## Runtime projects

### LCCB4220FirearmsBridge
- `42/media/lua/server/BuildingObjects/ISPlace3DItemCursor_Fix.lua` — mirrored.
- `42/media/lua/shared/LCC/Guard.lua` — mirrored.

### LCCB4220SVUTsarBridge
- `42/media/lua/server/Tuning2/ATA2Tuning2.lua` — mirrored path shim.
- `42/media/lua/shared/LCC/Guard.lua` — mirrored.

### LCCB4220zReBridge
- `42/media/lua/shared/BodyLocations.lua` — mirrored path shim.
- `42/media/lua/shared/LCC/Guard.lua` — mirrored.

### LCCB4220AegisGuard
- `42/media/lua/client/zzz_LCC_AegisTransferGuard.lua` — mirrored wrapper.
- `42/media/lua/shared/LCC/Guard.lua` — mirrored.

### LCCB4220LegacyCallbacks
- `42/media/lua/shared/zzz_LCC_LegacyItemCallbacks.lua` — mirrored generic callback bridge.

### LCCB4220SkillDescriptionsRU
- `42/media/lua/shared/Translate/RU/ZZ_LCC_VanillaPerks_RU.txt` — mirrored LCC-authored vanilla/B42 descriptions.

Lifestyle-specific `ZZ_LCC_Perks_RU.txt` is deliberately excluded.

### LCCB4220SurvivorAIStability — READY

Mirrored LCC-authored files:
- `42/media/lua/client/ISUI/ISCharacterScreen.lua`.
- `42/media/lua/server/zzz_LCC_BanditsDedicatedServerGuard.lua`.
- `42/media/lua/shared/LCC/Guard.lua`.

Split-owned refactors:
- `zzz_LCC_BanditsZombieCacheGuard.lua` replaces the publication copy of upstream `BanditZombie.lua`.
- `zzz_LCC_BanditsFarmingGuard.lua` replaces publication copies of upstream `ZAStompPlant.lua` and `ZAWaterFarm.lua`.
- `zzz_LCC_BanditsEmptyServerWandererGuard.lua` replaces the publication copy of upstream `BanditServerWanderers.lua` by guarding installed `BanditCustom.ClanGetAll()` only on an empty MP server.

CI forbids all four upstream Bandits files from returning to the split.

### LCCB4220WellnessCompat — READY

Mirrored LCC-authored files:
- `zzz_LCC_LifestyleBathFix.lua`.
- `zzz_LCC_LifestyleYogaProgress.lua`.
- `Hygiene/BathTubFunctions.lua`.
- `Hygiene/ShowerFunctions.lua`.
- `LCC/Guard.lua`.

Split-owned refactor:
- `42/media/perks.txt` declares only the `Yoga` UI proxy with zero XP thresholds. Authoritative Yoga level/XP remains in installed Lifestyle `HiddenSkills` storage.

No Lifestyle translations are bundled in this runtime item.

### LCCB4220PZKBridge — READY

All files are LCC-authored shims mirrored from the monolith:
- `client/Vehicle/ISUI/ISVehiclePartMenu.lua`.
- `client/Vehicle/ISVehiclePartMenu.lua`.
- `shared/ISBaseTimedAction.lua`.
- `server/utils/pzkZonesFunction.lua`.
- `shared/SVU3_PZKVLCCars_Stuffs.lua`.
- `shared/LCC/Guard.lua`.

No PZK vehicle/source/assets are bundled.

### LCCB4220OutfitMenuSafety — READY
- `zzz_LCC_ChimeraGhillieFix.lua` — mirrored LCC runtime wrapper.
- `LCC/Guard.lua` — mirrored.

No Chimera source/clothing assets are bundled.

## Translation projects

### LCCB4220BanditsRU — BLOCKED

- `42/media/lua/shared/Translate/RU/IG_UI.json` — isolated Bandits translation blob.

Its content is byte-for-byte identical to `3268487204/mods/Bandits/42.20/media/lua/shared/Translate/RU/IG_UI_ru.json`. CI compares the split directly against that target-side snapshot. It is intentionally upload-disabled pending translation publication permission/rights.

### LCCB4220ThirdPartyRU — BLOCKED / UNCLASSIFIED

The previous mixed project no longer contains the Bandits `42/.../IG_UI.json`. Remaining 42.x categories are:

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

Common RU mirrors still staged here:
- `common/.../IG_UI.json`
- `common/.../Moveables.json`
- `common/.../Tooltip.json`

These are primarily used by the Lifestyle translation audit, but key-level provenance is not yet complete because extra/custom keys are allowed by that audit. Do not publish this project as a Lifestyle-only item yet.

## Not moved into Workshop projects

- `server/linux/start-server.sh` and other server tooling.
- Original Workshop source folders (`3217685049`, `3268487204`, etc.).
- Third-party models, textures, sounds, vehicle data, clothing assets, and original Lua source.
- Original monolithic `42/mod.info`; every split has its own Mod ID/load metadata.
