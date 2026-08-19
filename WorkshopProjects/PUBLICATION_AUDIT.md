# Workshop publication audit

This is an engineering/publication-safety interpretation, not legal advice. Re-check the current Project Zomboid Modding Policy, Steam Subscriber Agreement, and target Workshop pages before changing an item from unlisted to public.

## Boundary used by this split

1. Third-party source code, models, textures, sounds, copied/translated text, or other third-party materials must not be shipped without the rights/permission required for that material.
2. An independent LCC compatibility item may require an original mod separately and interact with its installed API/types through LCC-authored hooks, wrappers, guards, and path shims without repacking the original item.
3. Any substantial/direct compatibility relationship must be clearly credited; neutral functional titles must never conceal the target relationship.
4. Modpacks/reuploads remain a different and stricter case and are intentionally not used here.
5. Mod-specific translations are handled more conservatively than runtime interoperability code and remain upload-disabled until provenance/permission is clear.
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
| `LCCB4220WellnessCompat` | READY_FOR_UNLISTED_TEST | LCC bath/shower/Yoga wrappers only; `perks.txt` declares only an LCC Yoga UI proxy. |
| `LCCB4220PZKBridge` | READY_FOR_UNLISTED_TEST | LCC path/API shims only; no PZK vehicle/source/assets are repacked. |
| `LCCB4220OutfitMenuSafety` | READY_FOR_UNLISTED_TEST | LCC runtime wrapper only; no Chimera clothing/source/assets are repacked. |
| `LCCB4220BanditsRU` | BLOCKED_PENDING_PERMISSION | Exact Bandits RU `IG_UI` translation blob isolated from runtime code; translation publication rights/permission not documented. |
| `LCCB4220ThirdPartyRU` | BLOCKED_PENDING_PERMISSION | Remaining mod-specific translations are still being classified at key/category level. |

## Bandits runtime

Target: `[B42] Bandits NPC`, Workshop `3268487204`, author Slayer. The target page says the author's work must not be reuploaded without written permission. The runtime split does not reupload Bandits Lua or assets: `BanditZombie.lua`, `BanditServerWanderers.lua`, `ZAStompPlant.lua`, and `ZAWaterFarm.lua` are explicitly forbidden by CI. Their compatibility behavior is supplied by LCC-authored post-load guards against the separately installed `Bandits2` dependency.

## Lifestyle runtime

Target: `Lifestyle: Hobbies`, Workshop `3403870858`, credited to Mopop and Angry. Its page asks for permission to add to/extend/alter the mod. The runtime split is a separate dependency-driven item containing LCC-authored compatibility code rather than a modified/repacked Lifestyle item.

`WellnessCompat/perks.txt` defines only `Yoga` with zero proxy XP thresholds; authoritative progress is read from installed Lifestyle `HiddenSkills`. Lifestyle translations are not bundled in the runtime item. Because the target page uses broad extension language, re-check this interpretation before public visibility even though the Workshop payload contains no Lifestyle source/assets/translations.

## PZK runtime

Target: PZK VLC, Workshop `3217685049`, PZK Forge. The target page prohibits repacking and uses broad modification/extension language. `LCCB4220PZKBridge` does not repack or edit PZK files; it contains only independent LCC path/API shims requiring the original PZK/SVU/Tsar dependencies separately.

## Chimera runtime

Current B42 target: `Federal Ranger's [Chimera]`, Workshop `3766693411`, author EtherealShigure. The current B42 page audited here does not carry the old B41 page's `On Lockdown` text. The split contains only an LCC runtime wrapper and no Chimera clothing/source/assets.

## Translation provenance

### Bandits RU

`LCCB4220BanditsRU/.../IG_UI.json` is byte-for-byte identical to `3268487204/mods/Bandits/42.20/media/lua/shared/Translate/RU/IG_UI_ru.json` (same Git blob SHA in the repository). It has been removed from the former mixed `LCCB4220ThirdPartyRU` project. Because it is translated target text rather than interoperability code, the project remains upload-disabled.

### Remaining RU staging

`LCCB4220ThirdPartyRU` no longer contains the Bandits `IG_UI.json`. The existing Lifestyle translation audit uses its remaining categories to cover the current Lifestyle EN translation set, but the audit also permits `extra/custom` keys. Therefore the remaining staging area is not yet called a clean Lifestyle-only project. Key-level/category-level provenance must be completed before another translation item is enabled.

## Publication rules

- Never bundle original Workshop directories or third-party source/assets into runtime compatibility items.
- Keep direct target names/authors in Workshop descriptions and mark every module unofficial.
- Keep runtime items `visibility=unlisted` until their feature smoke tests pass.
- Do not enable a split project together with equivalent `LaccckaB4220Compat` functionality during A/B tests.
- Do not move permission-sensitive translations into runtime projects merely to make them publishable.
- If a target author or moderation team disputes an independent-patch classification, stop public distribution of that module while the issue is resolved; do not conceal the relationship.

## Permission evidence

For translations or any future payload that actually includes third-party material, store evidence under `WorkshopProjects/permissions/` with the target mod/author, date, requested scope, response/link, and covered LCC project(s).
