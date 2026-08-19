# Workshop publication audit

This is an engineering/publication safety checklist, not legal advice.

The split follows two rules:

1. Project Zomboid's published modding policy requires modders to obtain the necessary permissions for third-party materials and says unlisted publication does not remove permission requirements.
2. A compatibility item should contain LCC-authored code/localization only, keep the original mod separate, identify the compatibility relationship, and avoid presenting itself as official.

## Current project status

| Project | Status | Reason |
|---|---|---|
| `LCCB4220FirearmsBridge` | READY_FOR_UNLISTED_TEST | LCC-authored runtime wrapper; no MFS assets/source redistributed. |
| `LCCB4220SVUTsarBridge` | READY_FOR_UNLISTED_TEST | LCC-authored legacy-path shim. Current audit found SVU3 permits third-party support work and no separate TsarLib prohibition relevant to this shim. |
| `LCCB4220zReBridge` | READY_FOR_UNLISTED_TEST | LCC-authored vanilla API path shim; no zRe source redistributed. |
| `LCCB4220AegisGuard` | READY_FOR_UNLISTED_TEST | LCC-authored wrapper around the inventory transfer validation path; no Aegis source redistributed. |
| `LCCB4220LegacyCallbacks` | READY_FOR_UNLISTED_TEST | Generic LCC-authored Build 42 compatibility bridge. |
| `LCCB4220SkillDescriptionsRU` | READY_FOR_UNLISTED_TEST | Contains only LCC-authored Russian descriptions for vanilla/Build 42 skills. Lifestyle-specific descriptions are intentionally excluded. |
| `LCCB4220SurvivorAIStability` | TECHNICALLY_CLEAN_PERMISSION_REVIEW | The split no longer redistributes Bandits Lua source: all four former full-file overrides are replaced by LCC-authored guards/shims. Upload remains disabled because prior author communication/permission context is restrictive and must be reviewed/documented before publication. |
| `LCCB4220WellnessCompat` | BLOCKED_PENDING_PERMISSION | Lifestyle permissions require express permission for extensions; Yoga/bath compatibility is an extension. |
| `LCCB4220PZKBridge` | BLOCKED_PENDING_PERMISSION | PZK extension policy requires permission even though the current implementation is mostly LCC-authored path shims. |
| `LCCB4220OutfitMenuSafety` | BLOCKED_PENDING_PERMISSION | Chimera's historical permission policy is restrictive. Keep blocked until current permission is documented. |
| `LCCB4220ThirdPartyRU` | BLOCKED_PENDING_PERMISSION | Translation material relates to third-party mods (notably Lifestyle/Bandits). Keep blocked until the relevant permissions are documented. |

## Bandits refactor progress

The publication split no longer carries these upstream Bandits files:

- `BanditZombie.lua`;
- `BanditServerWanderers.lua`;
- `ZombieActions/ZAStompPlant.lua`;
- `ZombieActions/ZAWaterFarm.lua`.

They are replaced only in the split by LCC-authored guards/wrappers. The monolithic `LaccckaCompatibilityPatch` deliberately retains its tested overrides as the known regression baseline.

The empty-server wanderer fix now wraps the public `BanditCustom.ClanGetAll()` API. In multiplayer with zero online players it returns an empty temporary table, making the original wanderer scheduler's clan loop a no-op while its player-derived `day` is unavailable. With a player online, and in single-player, it delegates to the original function unchanged.

This removes the source-redistribution blocker; it does **not** automatically grant publication permission. Keep the Workshop descriptor disabled until author-specific policy/communication is reviewed and the split-only behavior is runtime-tested.

## Publication rules

- Do not use a neutral title to conceal what a module patches. Neutral functional titles are fine, but the Workshop description must name/credit the target relationship.
- Do not bundle original Workshop directories, models, textures, sounds, Lua source, or other third-party assets unless explicit permission covers that redistribution.
- `Required Items`/dependencies are not permission by themselves.
- `visibility=unlisted` is for controlled testing of projects that already pass the audit; it is not a bypass for permission requirements.
- Permission evidence should be saved in this repository before a blocked/review project is enabled for Workshop upload.
- If upstream permissions change, re-audit before public release.

## Permission evidence

When permission is obtained, add a file under `WorkshopProjects/permissions/` containing:

- target mod and author;
- date;
- what was requested (compatibility fix / extension / translation / redistribution if any);
- the author's response or durable link/screenshot reference;
- which LCC Workshop project(s) the permission covers.

Only after that should a disabled Workshop descriptor be replaced by an active `workshop.txt`.
