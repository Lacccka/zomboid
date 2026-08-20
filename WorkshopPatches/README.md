# Split Workshop patches

This directory contains the staged split of `LaccckaCompatibilityPatch` into independent Workshop-ready patch items. The original monolithic patch is intentionally left untouched while this layout is tested.

## Public naming

The Workshop-facing names are deliberately generic. Public descriptions only state that the item contains targeted Build 42 compatibility/fix files. They do not imply ownership of or bundle the upstream mods.

Every item must keep this warning in its Workshop description:

> Do not use this patch unless you are sure it is required by your setup. This item contains patch files only; the original mods are not included and must be installed separately.

## Items

| Folder | Mod ID | Public title | Internal scope |
| --- | --- | --- | --- |
| `PatchCore` | `LaccckaB4220PatchCore` | Lacccka B42 Patch Core | Recommended shared guarded-patch helper used by the functional patches. |
| `RuntimeFixes` | `LaccckaB4220RuntimeFixes` | Lacccka B42 Runtime Fixes | Source-clean Bandits runtime / dedicated-server / zombie-action compatibility hooks. |
| `NPCCombatExperimental` | `LaccckaB4220NPCCombatExperimental` | Lacccka B42 NPC Combat Experimental | In-development NPC combat AttackState guard, diagnostics and admin stress-test tooling. |
| `ActivityFixes` | `LaccckaB4220ActivityFixes` | Lacccka B42 Activity Fixes | Lifestyle hygiene, Yoga/progression and perk compatibility fixes. |
| `CompatibilityBridges` | `LaccckaB4220CompatBridges` | Lacccka B42 Compatibility Bridges | Build 42 legacy module/API redirects used by weapon, vehicle and framework mods. |
| `SafetyFixes` | `LaccckaB4220SafetyFixes` | Lacccka B42 Safety Fixes | Defensive inventory/UI compatibility guards. |
| `RussianTextFixes` | `LaccckaB4220RussianText` | Lacccka B42 Russian Text Fixes | Russian localization and skill/UI text corrections. |

## Dependency model

`RuntimeFixes`, `NPCCombatExperimental`, `ActivityFixes`, `CompatibilityBridges`, and `SafetyFixes` use `LaccckaB4220PatchCore` as a **recommended soft dependency**. `RussianTextFixes` remains completely standalone.

The functional patches no longer use the Build 42 `mod.info` `require=` field for Patch Core, because that field makes Core a hard blocker before any fallback Lua can run. Instead, each functional patch declares Patch Core in `loadafter=` and ships the same small `LCC/Guard.lua` bootstrap:

1. it first tries the Core-only `LCC/CoreGuard` entrypoint;
2. if Core is present and compatible, the shared guard is returned and the patch runs in `GUARDED` mode;
3. if Core is absent or incompatible, a local shared fallback is returned and the patch runs in `DEGRADED` mode.

`DEGRADED` mode is intentionally best-effort. The functional fixes remain loadable, but correct behavior and failure isolation are not guaranteed without Patch Core. The fallback exists so a missing/unavailable Core Workshop item does not automatically make every functional patch unloadable.

Upstream mods are deliberately not hard-required by these generic patch items because the fixes are guarded/late-loaded and the public rule is to install a patch only for a setup that needs it.

## RuntimeFixes source-clean contract

`RuntimeFixes` must not contain complete Bandits Lua files. Its compatibility layer is implemented only through LCC-authored hooks, wrappers and narrow path shims:

- the empty dedicated-server wanderer crash is neutralized through a guarded `BanditCustom.ClanGetAll()` runtime view;
- squareless/despawned zombie objects are rejected through Bandits' existing compatibility predicate and then removed from its caches;
- farming callbacks are prechecked and the original `ZombieActions` callbacks remain authoritative;
- the missing dedicated `BanditZombie.GetInstanceById()` contract is implemented with a server-side on-demand lookup rather than importing `BanditZombie.lua`;
- the B42 character-screen compatibility file is a small LCC path shim, not an upstream source copy.

The grouped audit must fail if `BanditZombie.lua`, `BanditServerWanderers.lua`, `ZAWaterFarm.lua` or `ZAStompPlant.lua` reappear under `RuntimeFixes`.

## NPCCombatExperimental isolation contract

`NPCCombatExperimental` contains only NPC-combat work that is still being actively tested: the zombie -> NPC `bAttack`/AttackState guard, its observe-only target diagnostics, and the admin right-click stress spawner plus its server bridge. These files must not live in `RuntimeFixes`.

The public Workshop title and Mod ID intentionally do not name the upstream mod. Internal Lua integration still uses the real `Bandits2`/Bandits API names where required for load ordering and debugging.

The stable RuntimeFixes item must remain usable without enabling this experimental item. Conversely, the experimental item is allowed to load after RuntimeFixes so both can be tested together without modifying the already-published stable package.

## Source ownership

The monolithic compatibility patch remains a private regression baseline and can contain historical full-file overrides. Those files are not automatically suitable for public Workshop publication.

Each split item must be reviewed on its own publication contract. In particular, `RuntimeFixes` is intentionally source-clean and does not redistribute Bandits Lua source or assets. The optional-Guard bootstrap and degraded fallback are LCC-authored support code.

## Publishing

Each child directory is a separate Workshop staging directory with its own `workshop.txt` and `Contents/mods/.../42/mod.info`.

Published items keep their assigned Workshop ID in `workshop.txt`. Experimental items may use `id=0` and omit `preview.png` until they are ready for upload; the audit treats that state as unpublished/private staging.

## Migration rule

Do not enable the monolithic `LaccckaB4220Compat` together with these split items during normal testing. Some compatibility module paths and behavioral hooks overlap, so simultaneous loading is not a supported configuration.
