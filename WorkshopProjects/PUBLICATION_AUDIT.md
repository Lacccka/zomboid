# Workshop publication audit

This is an engineering/publication-safety interpretation, not legal advice. Re-check the current Project Zomboid Modding Policy, Steam Subscriber Agreement, and target Workshop pages before changing an item from unlisted to public.

## Boundary used by this split

The publication split follows a content-based rule:

1. Third-party source code, models, textures, sounds, copied text, or other third-party materials must not be shipped without the rights/permission required for that material.
2. An independent LCC compatibility item may require an original mod separately and interact with its installed API/types through LCC-authored hooks, wrappers, guards, and path shims without repacking the original item.
3. Any substantial/direct compatibility relationship must be clearly credited in the Workshop description; neutral functional titles must never conceal the target relationship.
4. Modpacks/reuploads remain a different case and are intentionally not used here.
5. Third-party translations remain permission-sensitive and are isolated from runtime compatibility code.
6. The abandoned/broken-mod rules still require respecting explicit expansion/reupload directions in that context.

## Current project status

| Project | Status | Reason |
|---|---|---|
| `LCCB4220FirearmsBridge` | READY_FOR_UNLISTED_TEST | LCC runtime wrapper; no MFS source/assets redistributed. |
| `LCCB4220SVUTsarBridge` | READY_FOR_UNLISTED_TEST | LCC legacy-path/API shims only. |
| `LCCB4220zReBridge` | READY_FOR_UNLISTED_TEST | LCC vanilla API path shim only. |
| `LCCB4220AegisGuard` | READY_FOR_UNLISTED_TEST | LCC wrapper around installed runtime API; no Aegis source redistributed. |
| `LCCB4220LegacyCallbacks` | READY_FOR_UNLISTED_TEST | Generic LCC Build 42 compatibility bridge. |
| `LCCB4220SkillDescriptionsRU` | READY_FOR_UNLISTED_TEST | LCC-authored vanilla/B42 Russian skill descriptions only. |
| `LCCB4220SurvivorAIStability` | READY_FOR_UNLISTED_TEST | All four former Bandits full-file overrides were removed from the split and replaced by LCC-authored guards/shims. |
| `LCCB4220WellnessCompat` | READY_FOR_UNLISTED_TEST | LCC bath/shower/Yoga wrappers only; `perks.txt` declares only an LCC Yoga UI proxy and no longer reproduces Lifestyle perk blocks. |
| `LCCB4220PZKBridge` | READY_FOR_UNLISTED_TEST | LCC path/API shims only; no PZK vehicle/source/assets are repacked. |
| `LCCB4220OutfitMenuSafety` | READY_FOR_UNLISTED_TEST | LCC runtime wrapper only; no Chimera clothing/source/assets are repacked. |
| `LCCB4220ThirdPartyRU` | BLOCKED_PENDING_PERMISSION | Contains mod-specific translated text. Keep disabled until the relevant translation rights/permissions are documented. |

## Target-specific notes

### Bandits

Current target: `[B42] Bandits NPC`, Workshop `3268487204`, author Slayer. The target page states that the author's work must not be reuploaded without written permission. This split does not reupload Bandits Lua or assets: `BanditZombie.lua`, `BanditServerWanderers.lua`, `ZAStompPlant.lua`, and `ZAWaterFarm.lua` are explicitly forbidden by CI.

The replacements are LCC-authored post-load guards that operate on the separately installed `Bandits2` dependency.

### Lifestyle

Current target: `Lifestyle: Hobbies`, Workshop `3403870858`, credited to Mopop and Angry. Its Workshop page asks for permission to add to/extend/alter the mod. This split does not alter or redistribute the Lifestyle item itself: it is a separate dependency-driven compatibility item containing LCC-authored runtime code.

To remove the last copied declaration structure, split `perks.txt` now defines only `Yoga`. Its XP thresholds are zero because the UI proxy reads the real Yoga level/XP directly from Lifestyle `HiddenSkills` storage. Lifestyle translations are not bundled here.

Because the target page uses broad extension language, re-check this classification before public visibility even though the Workshop payload itself contains no Lifestyle source/assets/translations.

### PZK VLC

Current target: PZK VLC, Workshop `3217685049`, PZK Forge. The target page prohibits repacking and states restrictions on modifying/extending the original mod. This split does not repack or edit PZK files; its payload is limited to independent LCC path/API shims requiring the original PZK/SVU/Tsar dependencies separately.

### Federal Ranger's Chimera

Current B42 target: `Federal Ranger's [Chimera]`, Workshop `3766693411`, author EtherealShigure. The current B42 page audited here does not carry the old B41 page's `On Lockdown` permission text. Regardless, this split contains only an LCC runtime wrapper and no Chimera clothing/source/assets.

## Publication rules

- Never bundle original Workshop directories or third-party source/assets into these compatibility items.
- Keep direct target names/authors in Workshop descriptions and mark every module unofficial.
- Keep `visibility=unlisted` until the exact runtime feature passes smoke testing.
- Do not enable a split project together with the equivalent monolithic `LaccckaB4220Compat` during A/B tests.
- Do not move third-party translations into runtime projects merely to make them publishable.
- If a target author or moderation team disputes the independent-patch classification, stop public distribution of that module while the issue is resolved; do not attempt to conceal the relationship.

## Permission evidence

For translations or any future case that actually includes third-party material, store evidence under `WorkshopProjects/permissions/` with the target mod/author, date, requested scope, response/link, and covered LCC project(s).
