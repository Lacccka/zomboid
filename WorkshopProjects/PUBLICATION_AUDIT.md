# Workshop publication audit

This is an engineering/publication-safety interpretation, not legal advice. Re-check the current Project Zomboid Modding Policy, Steam Subscriber Agreement, and target Workshop pages before changing an item from unlisted to public.

## Boundary used by this split

1. Third-party source code, models, textures, sounds, copied/translated text, or other third-party materials are not shipped in runtime compatibility items.
2. Independent LCC compatibility items require original mods separately and interact with installed APIs/types through LCC-authored hooks, wrappers, guards, and path shims.
3. Direct compatibility relationships are clearly credited; neutral functional titles must not conceal the target relationship.
4. Modpacks/reuploads are intentionally not used here.
5. Mod-specific translations are treated more conservatively and remain upload-disabled until provenance and publication rights/permission are clear.
6. Explicit abandoned/broken-mod expansion/reupload restrictions still require separate review.

## Current project status

| Project | Status | Reason |
|---|---|---|
| `LCCB4220FirearmsBridge` | READY_FOR_UNLISTED_TEST | LCC runtime wrapper; no MFS source/assets redistributed. |
| `LCCB4220SVUTsarBridge` | READY_FOR_UNLISTED_TEST | LCC legacy-path/API shims only. |
| `LCCB4220zReBridge` | READY_FOR_UNLISTED_TEST | LCC vanilla API path shim only. |
| `LCCB4220AegisGuard` | READY_FOR_UNLISTED_TEST | LCC runtime wrapper; no Aegis source redistributed. |
| `LCCB4220LegacyCallbacks` | READY_FOR_UNLISTED_TEST | Generic LCC Build 42 compatibility bridge. |
| `LCCB4220SkillDescriptionsRU` | READY_FOR_UNLISTED_TEST | LCC-authored vanilla/B42 Russian skill descriptions only. |
| `LCCB4220SurvivorAIStability` | READY_FOR_UNLISTED_TEST | All four former Bandits full-file overrides were removed and replaced by LCC guards/shims. |
| `LCCB4220WellnessCompat` | READY_FOR_UNLISTED_TEST | LCC bath/shower/Yoga runtime code; `perks.txt` declares only the LCC Yoga UI proxy. |
| `LCCB4220PZKBridge` | READY_FOR_UNLISTED_TEST | LCC path/API shims only; no PZK vehicle/source/assets repacked. |
| `LCCB4220OutfitMenuSafety` | READY_FOR_UNLISTED_TEST | LCC runtime wrapper only; no Chimera clothing/source/assets repacked. |
| `LCCB4220BanditsRU` | BLOCKED_PENDING_PERMISSION | Exact Bandits RU `IG_UI` translation isolated from runtime code. |
| `LCCB4220LifestyleRU` | BLOCKED_PENDING_PERMISSION | Lifestyle-oriented RU translation staging; Bandits translation excluded, final translation permission/provenance review still required. |

## Bandits runtime

Target: `[B42] Bandits NPC`, Workshop `3268487204`, author Slayer. The runtime split does not carry Bandits Lua or assets. CI explicitly forbids `BanditZombie.lua`, `BanditServerWanderers.lua`, `ZAStompPlant.lua`, and `ZAWaterFarm.lua`; LCC-authored post-load guards operate against the separately installed `Bandits2` dependency.

## Lifestyle runtime

Target: `Lifestyle: Hobbies`, Workshop `3403870858`, credited to Mopop and Angry. `LCCB4220WellnessCompat` is a separate dependency-driven item containing LCC-authored runtime compatibility code. Its `perks.txt` defines only `Yoga` with zero proxy XP thresholds; authoritative progress remains in installed Lifestyle `HiddenSkills`. No Lifestyle translations are bundled in this runtime project.

Because the target page uses broad extension language, re-check the target page before public visibility even though the runtime payload contains no Lifestyle source/assets/translations.

## PZK runtime

Target: PZK VLC, Workshop `3217685049`, PZK Forge. `LCCB4220PZKBridge` contains independent LCC path/API shims only and requires original PZK/SVU/Tsar dependencies separately. It does not repack vehicle/source/assets.

## Chimera runtime

Current B42 target: `Federal Ranger's [Chimera]`, Workshop `3766693411`, author EtherealShigure. The split contains only an LCC runtime wrapper and no Chimera clothing/source/assets.

## Translation provenance

### Bandits RU

`LCCB4220BanditsRU/.../IG_UI.json` is byte-for-byte identical to `3268487204/mods/Bandits/42.20/media/lua/shared/Translate/RU/IG_UI_ru.json`. CI compares the split directly against that target-side snapshot. It remains upload-disabled because translated target text is a different publication-rights question from interoperability code.

### Lifestyle RU

`LCCB4220LifestyleRU` contains the remaining RU subtrees formerly staged in the generic `LCCB4220ThirdPartyRU` project. The Bandits `42/.../IG_UI.json` blob is explicitly excluded and CI forbids it from appearing here.

The existing Lifestyle translation audit uses these categories to cover the current Lifestyle EN key set, but it allows `extra/custom` keys. Therefore this project is accurately described as Lifestyle-oriented localization staging, not yet as a fully proven one-to-one Lifestyle translation package. It stays upload-disabled until permission/publication rights and final key-level provenance are documented.

The deprecated `LCCB4220ThirdPartyRU` project must not return; CI checks for this.

## Publication rules

- Never bundle original Workshop directories or third-party source/assets into runtime compatibility items.
- Keep direct target names/authors in Workshop descriptions and mark every module unofficial.
- Keep runtime items `visibility=unlisted` until feature smoke tests pass.
- Do not enable split and equivalent monolithic runtime fixes together during A/B tests.
- Keep mod-specific translations separate from runtime patches.
- If a target author or moderation team disputes an independent-patch classification, stop public distribution of that module while the issue is resolved; do not conceal the relationship.

## Permission evidence

For translations or any future payload that actually includes third-party material, store evidence under `WorkshopProjects/permissions/` with target mod/author, date, requested scope, response/link, and covered LCC project(s).
