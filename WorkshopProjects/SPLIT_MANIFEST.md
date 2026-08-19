# Split manifest

Source/reference package: `LaccckaCompatibilityPatch/Contents/mods/LaccckaCompatibilityPatch`.

The monolithic patch is not deleted or modified by this publication split. Files marked **mirrored** remain byte-for-byte copies of LCC-authored compatibility files in the monolith. Files marked **split-owned refactor** intentionally diverge to remove third-party full-file/declaration copies while preserving the compatibility contract.

## LCCB4220FirearmsBridge

- `42/media/lua/server/BuildingObjects/ISPlace3DItemCursor_Fix.lua` — mirrored.
- `42/media/lua/shared/LCC/Guard.lua` — mirrored.

## LCCB4220SVUTsarBridge

- `42/media/lua/server/Tuning2/ATA2Tuning2.lua` — mirrored path shim.
- `42/media/lua/shared/LCC/Guard.lua` — mirrored.

## LCCB4220zReBridge

- `42/media/lua/shared/BodyLocations.lua` — mirrored path shim.
- `42/media/lua/shared/LCC/Guard.lua` — mirrored.

## LCCB4220AegisGuard

- `42/media/lua/client/zzz_LCC_AegisTransferGuard.lua` — mirrored wrapper.
- `42/media/lua/shared/LCC/Guard.lua` — mirrored.

## LCCB4220LegacyCallbacks

- `42/media/lua/shared/zzz_LCC_LegacyItemCallbacks.lua` — mirrored generic callback bridge.

## LCCB4220SkillDescriptionsRU

- `42/media/lua/shared/Translate/RU/ZZ_LCC_VanillaPerks_RU.txt` — mirrored LCC-authored vanilla/B42 descriptions.

Lifestyle-specific `ZZ_LCC_Perks_RU.txt` is deliberately excluded.

## LCCB4220SurvivorAIStability — READY

Mirrored LCC-authored files:

- `42/media/lua/client/ISUI/ISCharacterScreen.lua` — legacy B42.20 path shim.
- `42/media/lua/server/zzz_LCC_BanditsDedicatedServerGuard.lua` — dedicated-server lookup shim.
- `42/media/lua/shared/LCC/Guard.lua`.

Split-owned refactors:

- `42/media/lua/client/zzz_LCC_BanditsZombieCacheGuard.lua` — replaces the publication copy of upstream `BanditZombie.lua`.
- `42/media/lua/shared/zzz_LCC_BanditsFarmingGuard.lua` — replaces publication copies of upstream `ZAStompPlant.lua` and `ZAWaterFarm.lua`.
- `42/media/lua/server/zzz_LCC_BanditsEmptyServerWandererGuard.lua` — replaces the publication copy of upstream `BanditServerWanderers.lua` by guarding the installed public `BanditCustom.ClanGetAll()` API only on an empty MP server.

The split contains none of those four upstream Bandits files. CI forbids them from returning.

## LCCB4220WellnessCompat — READY

Mirrored LCC-authored compatibility files:

- `42/media/lua/client/zzz_LCC_LifestyleBathFix.lua`.
- `42/media/lua/client/zzz_LCC_LifestyleYogaProgress.lua`.
- `42/media/lua/shared/Hygiene/BathTubFunctions.lua`.
- `42/media/lua/shared/Hygiene/ShowerFunctions.lua`.
- `42/media/lua/shared/LCC/Guard.lua`.

Split-owned refactor:

- `42/media/perks.txt` — declares only the `Yoga` UI proxy with zero XP thresholds. It no longer mirrors the monolith's combined Lifestyle/Art/Cleaning/Dancing/Meditation/Music declarations. Authoritative Yoga level/XP is read from installed Lifestyle `HiddenSkills` storage by the LCC UI wrapper.

No Lifestyle translations are bundled in this runtime item.

## LCCB4220PZKBridge — READY

All files are LCC-authored shims mirrored from the monolith:

- `42/media/lua/client/Vehicle/ISUI/ISVehiclePartMenu.lua`.
- `42/media/lua/client/Vehicle/ISVehiclePartMenu.lua`.
- `42/media/lua/shared/ISBaseTimedAction.lua`.
- `42/media/lua/server/utils/pzkZonesFunction.lua`.
- `42/media/lua/shared/SVU3_PZKVLCCars_Stuffs.lua`.
- `42/media/lua/shared/LCC/Guard.lua`.

No PZK vehicle/source/assets are bundled.

## LCCB4220OutfitMenuSafety — READY

- `42/media/lua/client/zzz_LCC_ChimeraGhillieFix.lua` — mirrored LCC runtime wrapper.
- `42/media/lua/shared/LCC/Guard.lua` — mirrored.

No Chimera source/clothing assets are bundled.

## LCCB4220ThirdPartyRU — BLOCKED

This project remains a byte-for-byte isolation of the current mod-specific translation material from the monolithic patch, excluding `ZZ_LCC_VanillaPerks_RU.txt`. It is deliberately not made publishable until the relevant translation permissions/rights are audited per target.

Current files include the 42.x RU JSON/TXT translation set plus the common `IG_UI.json`, `Moveables.json`, and `Tooltip.json` mirrors.

## Not moved into Workshop projects

- `server/linux/start-server.sh` and other server tooling.
- Original Workshop source folders (`3217685049`, `3268487204`, etc.).
- Third-party models, textures, sounds, vehicle data, clothing assets, and original Lua source.
- Original monolithic `42/mod.info`; every split has its own Mod ID/load metadata.
