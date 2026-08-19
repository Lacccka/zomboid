# Split Workshop patches

This directory contains the staged split of `LaccckaCompatibilityPatch` into independent Workshop-ready patch items. The original monolithic patch is intentionally left untouched while this layout is tested.

## Public naming

The Workshop-facing names are deliberately generic. Public descriptions only state that the item contains targeted Build 42 compatibility/fix files. They do not imply ownership of or bundle the upstream mods.

Every item must keep this warning in its Workshop description:

> Do not use this patch unless you are sure it is required by your setup. This item contains patch files only; the original mods are not included and must be installed separately.

## Items

| Folder | Mod ID | Public title | Internal scope |
| --- | --- | --- | --- |
| `PatchCore` | `LaccckaB4220PatchCore` | Lacccka B42 Patch Core | Shared guarded-patch helper used by the functional patches. |
| `RuntimeFixes` | `LaccckaB4220RuntimeFixes` | Lacccka B42 Runtime Fixes | Bandits runtime / dedicated-server / zombie-action compatibility fixes. |
| `ActivityFixes` | `LaccckaB4220ActivityFixes` | Lacccka B42 Activity Fixes | Lifestyle hygiene, Yoga/progression and perk compatibility fixes. |
| `CompatibilityBridges` | `LaccckaB4220CompatBridges` | Lacccka B42 Compatibility Bridges | Build 42 legacy module/API redirects used by weapon, vehicle and framework mods. |
| `SafetyFixes` | `LaccckaB4220SafetyFixes` | Lacccka B42 Safety Fixes | Defensive inventory/UI compatibility guards. |
| `RussianTextFixes` | `LaccckaB4220RussianText` | Lacccka B42 Russian Text Fixes | Russian localization and skill/UI text corrections. |

## Dependency model

`RuntimeFixes`, `ActivityFixes`, `CompatibilityBridges`, and `SafetyFixes` require `LaccckaB4220PatchCore`. `RussianTextFixes` is standalone.

The dependency is declared with the Build 42 `mod.info` `require=` field so a functional patch cannot be enabled without its helper. Upstream mods are deliberately not hard-required by these generic patch items because the fixes are guarded/late-loaded and the public rule is to install a patch only for a setup that needs it.

## Source ownership

The files below are copied from the current monolithic compatibility patch, not from the original Workshop mods directly. No complete upstream mod is included in any split item.

The split is organized by responsibility so one upstream update can disable/remove one patch without forcing unrelated fixes to be republished.

## Publishing

Each child directory is a separate Workshop staging directory with its own `workshop.txt` and `Contents/mods/.../42/mod.info`.

`workshop.txt` uses `id=0` as a staging placeholder. Replace it with the assigned Workshop item ID after the first upload. Add a per-item `preview.png` before publishing; binary preview assets are intentionally not duplicated as part of this source split.

## Migration rule

Do not enable the monolithic `LaccckaB4220Compat` together with these split items during normal testing. The split tree intentionally reuses the same compatibility Lua module paths and would otherwise duplicate overrides.
