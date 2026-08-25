# Active mod Russian localization — final report

Date: 2026-08-25

Branch: `agent/b42-20-compatibility-patch`

Patch mod: `LaccckaB4220RussianText` 1.1.0

## Result

The active-mod translation set has been added as an overlay. No source Workshop mod was edited. The patch supplies 1,901 Russian entries in separate `LCC_*.json` dictionaries and a narrow client-side bridge for strings that the source mods still hardcode in Lua.

Coverage is derived from the current branch rather than the earlier estimate. In particular, the installed MFS community fix expands the actual MFS gap to 698 entries, while the current Explosives release already contains most of its Russian dictionaries and needs only five missing dictionary entries plus one hardcoded action.

| Target | New patch entries | Coverage after overlay |
| --- | ---: | ---: |
| BackpackSystemB42 | 400 | 400/400 |
| Federal Rangers Chimera | 200 | 164 active script items + 36 menu/UI keys |
| SVU3 Core + Vanilla | 374 | 374/374 |
| Immersive Vehicle Paint | 94 | 94/94 |
| MFS + MFS community fix | 698 | 1,317/1,317 combined EN keys |
| Explosives | 5 dictionary + 1 runtime | 65/65 dictionary keys |
| PZK Vanilla Plus Car Pack | 116 | 769/769 combined EN keys |
| PZK Carzone Workshop | 2 dictionary + 2 runtime | 2/2 dictionary keys plus hardcoded menu/tooltip |
| Survival's Hauler | 3 dictionary + 6 runtime | Static names and known radial-menu strings |
| Runtime dictionary | 9 | 9/9 EN/RU parity |
| AP | 0 | Existing RU remains complete: 1,092/1,092 |

The 1,901 total counts each patch dictionary key once. Runtime keys are broken out by their owning mods in the descriptions above but counted only once in the total.

## Safety design

- Original Workshop content is unchanged, so Workshop updates can still be inspected and replaced normally.
- Each target has separate overlay files instead of adding another monolithic translation dictionary.
- `loadModAfter` lists every translated dependency, including `MFS_community_fix`, so the overlay wins deterministically without changing the server's `Mods=` order.
- The runtime bridge changes display text only. It wraps `ISContextMenu.addOption`, `ISRadialMenu.addSlice`, and the PZK vehicle-menu tooltip after the source code has built its options. Actions, callbacks, arguments, vehicle state, networking, and recipes are not modified.
- Runtime replacements use exact source literals or anchored Hauler patterns. The generic word `Vehicle` is translated only when it occurs inside Hauler's `Load … [slot]` or `Unload slot: …` strings.
- English fallback entries are included for every patch-owned runtime key, preserving non-Russian clients and placeholder order.

## Regression audit

`tools/audit_russian_text_targets.py` validates:

- per-mod EN coverage using only that mod's own RU plus this overlay;
- the union of MFS base and community-fix English dictionaries;
- all 164 Chimera items currently declared by active clothing scripts, including items absent from the mod's EN dictionary;
- placeholder parity (`%1`, `%2`, `%%`);
- conflicting duplicate keys in the patch dictionaries;
- AP's existing complete RU set;
- runtime EN/RU parity and the exact upstream hardcoded literals on which the bridge depends.

The accompanying GitHub Actions workflow runs this audit whenever the patch or any target Workshop source changes. An upstream rename therefore becomes a visible CI failure instead of silently leaving an English string in game.

## Static validation performed

- All generated JSON files parse successfully.
- Translation coverage audit: 4,325 target/key checks passed.
- Patch collision audit: no conflicting values among 4,753 existing and new patch keys.
- Runtime source contract and EN/RU placeholder checks passed.
- Python audit syntax validation and repository whitespace validation passed.

## Remaining manual verification

A short Build 42.20 Russian-client smoke test is still required because the game runtime is not available in CI. Check MFS crafting and Sandbox pages, a Chimera/Backpack item tooltip, SVU/paint vehicle menus, Explosives' flare action, PZK rust removal, and Hauler load/unload radial entries. This validates rendering and load timing; the patch does not alter those mods' gameplay logic.
