# Active mod Russian localization — final report

Date: 2026-08-25

Branch: `agent/b42-20-compatibility-patch`

Patch mod: `LaccckaB4220RussianText` 1.1.1

## Result

The active-mod translation set has been added as an overlay. No source Workshop mod was edited. The patch supplies 1,902 Russian entries in separate `LCC_*.json` dictionaries and narrow client-side bridges for strings that the source mods still hardcode in Lua or pass to Build 42.20 Mod Options as already-resolved display text.

Coverage is derived from the current branch rather than the earlier estimate. In particular, the installed MFS community fix expands the actual MFS gap to 698 entries, while the current Explosives release already contains most of its Russian dictionaries and needs six patch dictionary entries plus one hardcoded action/compatibility override.

| Target | New patch entries | Coverage after overlay |
| --- | ---: | ---: |
| BackpackSystemB42 | 400 | 400/400 |
| Federal Rangers Chimera | 200 | 164 active script items + 36 menu/UI keys |
| SVU3 Core + Vanilla | 374 | 374/374 |
| Immersive Vehicle Paint | 94 | 94/94 |
| MFS + MFS community fix | 698 | 1,317/1,317 combined EN keys |
| Explosives | 6 dictionary + 1 runtime | 65/65 dictionary keys plus B42.20 percent-format override |
| PZK Vanilla Plus Car Pack | 116 | 769/769 combined EN keys |
| PZK Carzone Workshop | 2 dictionary + 2 runtime | 2/2 dictionary keys plus hardcoded menu/tooltip |
| Survival's Hauler | 3 dictionary + 6 runtime | Static names and known radial-menu strings |
| Runtime dictionary | 9 | 9/9 EN/RU parity |
| AP | 0 | Existing RU remains complete: 1,092/1,092 |

The 1,902 total counts each patch dictionary key once. Runtime keys are broken out by their owning mods in the descriptions above but counted only once in the total.

## Safety design

- Original Workshop content is unchanged, so Workshop updates can still be inspected and replaced normally.
- Each target has separate overlay files instead of adding another monolithic translation dictionary.
- `loadModAfter` lists every translated dependency, including `MFS_community_fix`, so the overlay wins deterministically without changing the server's `Mods=` order.
- The runtime text bridge changes display text only. It wraps `ISContextMenu.addOption`, `ISRadialMenu.addSlice`, and the PZK vehicle-menu tooltip after the source code has built its options. Actions, callbacks, arguments, vehicle state, networking, and recipes are not modified.
- Build 42.20's Mod Options compatibility guard wraps only `MainOptions.addModOptionsPanel`. Before vanilla calls `getText()` a second time, it escapes unsafe literal percent signs in panel/option names, tooltips, and multi-tick labels while preserving `%%` and `%1`/`%2` placeholders. Combo-box values and descriptions are not altered because MainOptions renders them directly.
- Runtime replacements use exact source literals or anchored Hauler patterns. The generic word `Vehicle` is translated only when it occurs inside Hauler's `Load … [slot]` or `Unload slot: …` strings.
- English fallback entries are included for every patch-owned runtime key, preserving non-Russian clients and placeholder order.

## B42.20.3 percent-format hotfix

A Russian-client smoke log on 2026-08-25 exposed two identical `UnknownFormatConversionException: Conversion = ')'` failures while `MainOptions.addModOptionsPanel()` was built: once for the main menu and once for the in-game options menu. The same run also reported 12 malformed Immersive Vehicle Paint percentage labels and the Explosives fire-damage tooltip.

The regression was introduced with the expanded localization overlay earlier on 2026-08-25: literal display percentages such as `-75%` were copied into translation values without the `%%` escape expected by the current Translator formatter. Version 1.1.1 corrects those known dictionary values and adds the narrow Mod Options guard described above. This is intentionally not a global `getText()` replacement.

## Regression audit

`tools/audit_russian_text_targets.py` validates:

- per-mod EN coverage using only that mod's own RU plus this overlay;
- the union of MFS base and community-fix English dictionaries;
- all 164 Chimera items currently declared by active clothing scripts, including items absent from the mod's EN dictionary;
- placeholder parity (`%1`, `%2`, `%%`);
- conflicting duplicate keys in the patch dictionaries;
- AP's existing complete RU set;
- runtime EN/RU parity and the exact upstream hardcoded literals on which the bridge depends.

The accompanying GitHub Actions workflow additionally rejects unsafe literal `%` values in the two Build 42.20 format-sensitive Sandbox overrides that caused this incident. An upstream rename or a recurrence of the observed percent-format regression therefore becomes a visible CI failure instead of silently reaching the client.

## Static validation performed

- All generated JSON files parse successfully.
- Translation coverage audit previously passed 4,325 target/key checks; the new Explosives override does not remove any key.
- Patch collision audit previously reported no conflicting values among the existing overlay keys.
- Runtime source contract and EN/RU placeholder checks passed before the hotfix; the new guard is isolated from gameplay callbacks and networking.
- The workflow now contains a dedicated B42.20 literal-percent regression check for the affected Sandbox overrides.

## Remaining manual verification

Run one fresh Build 42.20.3 Russian-client join after updating the patch. The expected result is: no 12 Immersive Vehicle Paint formatter warnings, no Explosives `FireDamageMultiplier_tooltip` formatter warning, and no pair of `UnknownFormatConversionException: Conversion = ')'` exceptions from `MainOptions.addModOptionsPanel`. If another third-party Mod Options field arrives already translated with an unsafe percent, the compatibility guard will sanitize it and log only the field/scope identifier rather than failing menu construction.
