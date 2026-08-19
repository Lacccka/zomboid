# Split manifest

Source of truth: `LaccckaCompatibilityPatch/Contents/mods/LaccckaCompatibilityPatch`.

The monolithic patch is not deleted or modified by this split. Runtime files below are mirrored byte-for-byte and checked by `tools/audit_workshop_split.py`. Project-specific `mod.info`, README and Workshop descriptors are new packaging metadata.

## LCCB4220FirearmsBridge

- `42/media/lua/server/BuildingObjects/ISPlace3DItemCursor_Fix.lua`
- `42/media/lua/shared/LCC/Guard.lua`

## LCCB4220SVUTsarBridge

- `42/media/lua/server/Tuning2/ATA2Tuning2.lua`
- `42/media/lua/shared/LCC/Guard.lua`

## LCCB4220zReBridge

- `42/media/lua/shared/BodyLocations.lua`
- `42/media/lua/shared/LCC/Guard.lua`

## LCCB4220AegisGuard

- `42/media/lua/client/zzz_LCC_AegisTransferGuard.lua`
- `42/media/lua/shared/LCC/Guard.lua`

## LCCB4220LegacyCallbacks

- `42/media/lua/shared/zzz_LCC_LegacyItemCallbacks.lua`

## LCCB4220SkillDescriptionsRU

- `42/media/lua/shared/Translate/RU/ZZ_LCC_VanillaPerks_RU.txt`

Lifestyle-specific `ZZ_LCC_Perks_RU.txt` is deliberately excluded from this publishable vanilla/B42 project.

## LCCB4220SurvivorAIStability — BLOCKED

- `42/media/lua/client/BanditZombie.lua`
- `42/media/lua/client/ISUI/ISCharacterScreen.lua`
- `42/media/lua/server/BanditServerWanderers.lua`
- `42/media/lua/server/zzz_LCC_BanditsDedicatedServerGuard.lua`
- `42/media/lua/shared/ZombieActions/ZAStompPlant.lua`
- `42/media/lua/shared/ZombieActions/ZAWaterFarm.lua`
- `42/media/lua/shared/LCC/Guard.lua`

The Bandits full-file overrides remain intact for local regression testing; their presence is exactly why the Workshop descriptor is disabled pending permission/refactor.

## LCCB4220WellnessCompat — BLOCKED

- `42/media/lua/client/zzz_LCC_LifestyleBathFix.lua`
- `42/media/lua/client/zzz_LCC_LifestyleYogaProgress.lua`
- `42/media/lua/shared/Hygiene/BathTubFunctions.lua`
- `42/media/lua/shared/Hygiene/ShowerFunctions.lua`
- `42/media/lua/shared/LCC/Guard.lua`
- `42/media/perks.txt`

## LCCB4220PZKBridge — BLOCKED

- `42/media/lua/client/Vehicle/ISUI/ISVehiclePartMenu.lua`
- `42/media/lua/client/Vehicle/ISVehiclePartMenu.lua`
- `42/media/lua/shared/ISBaseTimedAction.lua`
- `42/media/lua/server/utils/pzkZonesFunction.lua`
- `42/media/lua/shared/SVU3_PZKVLCCars_Stuffs.lua`
- `42/media/lua/shared/LCC/Guard.lua`

## LCCB4220OutfitMenuSafety — BLOCKED

- `42/media/lua/client/zzz_LCC_ChimeraGhillieFix.lua`
- `42/media/lua/shared/LCC/Guard.lua`

## LCCB4220ThirdPartyRU — BLOCKED

Mirrors all current third-party/mod-specific Russian localization from the monolithic patch while excluding `ZZ_LCC_VanillaPerks_RU.txt`:

- `42/media/lua/shared/Translate/RU/ContextMenu.json`
- `42/media/lua/shared/Translate/RU/Farming.json`
- `42/media/lua/shared/Translate/RU/IG_UI.json`
- `42/media/lua/shared/Translate/RU/IG_UI_RU.txt`
- `42/media/lua/shared/Translate/RU/ItemName.json`
- `42/media/lua/shared/Translate/RU/Mod.json`
- `42/media/lua/shared/Translate/RU/Moodles.json`
- `42/media/lua/shared/Translate/RU/Moveables.json`
- `42/media/lua/shared/Translate/RU/Moveables_RU.txt`
- `42/media/lua/shared/Translate/RU/Recipes.json`
- `42/media/lua/shared/Translate/RU/Recorded_Media.json`
- `42/media/lua/shared/Translate/RU/Sandbox.json`
- `42/media/lua/shared/Translate/RU/Tooltip.json`
- `42/media/lua/shared/Translate/RU/UI.json`
- `42/media/lua/shared/Translate/RU/ZZ_LCC_Perks_RU.txt`
- `common/media/lua/shared/Translate/RU/IG_UI.json`
- `common/media/lua/shared/Translate/RU/Moveables.json`
- `common/media/lua/shared/Translate/RU/Tooltip.json`

## Not moved into Workshop projects

- `server/linux/start-server.sh` and other server tooling: remains server-side repository tooling.
- Original Workshop source folders (`3217685049`, `3268487204`, etc.): remain reference/upstream snapshots and are never bundled by these projects.
- Original monolithic `42/mod.info`: replaced by project-specific IDs/load order metadata.
