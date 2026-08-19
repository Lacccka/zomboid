# Split manifest

Source/reference package: `LaccckaCompatibilityPatch/Contents/mods/LaccckaCompatibilityPatch`.

The monolithic patch is not deleted or modified by this split. Most runtime files below are mirrored byte-for-byte and checked by `tools/audit_workshop_split.py`. Project-specific `mod.info`, README and Workshop descriptors are new packaging metadata. A small number of publication-oriented refactors intentionally replace redistributed third-party source with LCC-authored wrappers; those files are explicitly marked below.

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

## LCCB4220SurvivorAIStability — PERMISSION REVIEW

Mirrored LCC-authored files still shared with the monolithic package:

- `42/media/lua/client/ISUI/ISCharacterScreen.lua` — tiny legacy module-path shim.
- `42/media/lua/server/zzz_LCC_BanditsDedicatedServerGuard.lua` — dedicated-server lookup shim.
- `42/media/lua/shared/LCC/Guard.lua`.

Split-only LCC-authored publication refactors:

- `42/media/lua/client/zzz_LCC_BanditsZombieCacheGuard.lua` — post-update/post-flush cleanup; replaces the split copy of upstream `BanditZombie.lua`.
- `42/media/lua/shared/zzz_LCC_BanditsFarmingGuard.lua` — precheck wrappers around installed Bandits farming callbacks; replaces split copies of upstream `ZAStompPlant.lua` and `ZAWaterFarm.lua`.
- `42/media/lua/server/zzz_LCC_BanditsEmptyServerWandererGuard.lua` — returns an empty temporary clan view only on an empty MP server, allowing the original local wanderer orchestrator to skip its unsafe nil-day tick; replaces the split copy of upstream `BanditServerWanderers.lua`.

The split now contains no full Bandits Lua source override. The monolithic patch intentionally retains its existing full overrides as the known runtime baseline. Workshop upload remains disabled until author-specific permission/policy review and runtime regression testing are complete.

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
