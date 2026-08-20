# Final report: Russian skill tooltips and Lifestyle Yoga

**Status:** Closed / confirmed fixed in-game  
**Date:** 2026-08-20  
**Target:** Project Zomboid Build 42.20.x  
**Branch:** `agent/b42-20-compatibility-patch`

## Final patch versions

- `LaccckaB4220ActivityFixes` — **1.1.7**
- `LaccckaB4220RussianText` — **1.0.3**

## Scope

The issue affected skill descriptions in the standard character Skills panel with Russian UI and the Lifestyle Yoga proxy shown by the compatibility patch.

Confirmed affected skills during debugging:

- Medicine (`Doctor`)
- Animal Care (`Husbandry`)
- Short Blade (`SmallBlade`)
- Short Blunt (`SmallBlunt`)
- Long Blade (`LongBlade`)
- Long Blunt (`Blunt`)
- Sprinting / Running (`Sprinting`)
- Lightfoot (`Lightfoot`)
- Lifestyle Yoga (`Yoga`)

Other skill descriptions were already working and were intentionally left on the normal game/Translator path.

## Symptoms

Initial failures displayed untranslated lookup keys such as:

```text
IGUI_perks_Медицина_Description
IGUI_perks_Длинное дробящее_Description
IGUI_perks_Короткое режущее_Description
```

Yoga also used an oversized technical description that exposed implementation details such as the hidden Lifestyle skill/proxy behavior.

A later intermediate build resolved the perk selection but rendered Russian fallback text as mojibake / corrupted characters on Windows.

## Confirmed root causes

### 1. Description lookup used localized perk names

Build 42.20 `ISSkillProgressBar.updateTooltip()` can construct the description key from `perk:getName()`. With Russian UI, `getName()` is already localized, producing keys such as:

```text
IGUI_perks_Медицина_Description
```

instead of stable keys based on the internal perk id.

The compatibility layer therefore must resolve the description from stable `perk:getId()` values first.

### 2. Early Russian-language detection was too narrow

An intermediate fix enabled direct Russian fallbacks only when `IGUI_perks_Fitness == "Фитнес"`. This assumption was not reliable for the active Build 42 Russian localization and prevented valid id-based fallbacks from being used.

The final implementation detects the active language through the Translator when possible and keeps safer fallbacks for Russian UI detection.

### 3. Cyrillic literals in client Lua were unsafe in this setup

The next intermediate version embedded Russian fallback text directly inside client Lua. On the tested Windows Build 42.20 client this text was rendered as mojibake.

Final rule: **client Lua used by these fixes stays ASCII-only; localized Russian text lives in normal Translator resources.**

## Final implementation

### Skill descriptions

`WorkshopPatches/ActivityFixes/Contents/mods/LaccckaB4220ActivityFixes/42/media/lua/client/zzz_LCC_SkillDescriptions.lua`

- wraps `ISSkillProgressBar.updateTooltip()` through `LCC/Guard`;
- obtains the stable internal perk id with `perk:getId()`;
- maps known Build 42 ids and compatibility aliases;
- for the affected Russian skills, resolves patch-owned fallback strings through ASCII Translator keys;
- preserves the normal Translator lookup for unaffected skills;
- logs installed version, language detection and per-skill override/miss diagnostics.

Russian fallback strings are stored in:

`WorkshopPatches/RussianTextFixes/Contents/mods/LaccckaB4220RussianText/42/media/lua/shared/Translate/RU/Farming.json`

The `Farming_LCC_Skill_*_Description` namespace is intentionally used as a patch-owned Translator namespace so the Lua layer never embeds Cyrillic literals.

### Yoga

`WorkshopPatches/ActivityFixes/Contents/mods/LaccckaB4220ActivityFixes/42/media/lua/client/zzz_LCC_LifestyleYogaProgress.lua`

Yoga remains a UI proxy for Lifestyle's authoritative hidden Yoga progression. The proxy:

- stays visible in the standard Skills panel;
- reads real progress from Lifestyle `HiddenSkills`;
- does not call `LevelPerk()` on the proxy;
- does not filter Yoga based on `DividerMeditationNew`;
- resolves the Russian tooltip through Translator resources instead of direct Cyrillic Lua literals.

The final concise Russian Yoga description is progression-oriented and omits implementation details:

> Прокачивается выполнением поз йоги.  
> 1 ур.: Шавасана даёт «Дзен» и повышает получение опыта Физподготовки, Силы, Медитации и Йоги.  
> С ростом уровня открываются новые позы и снижается шанс неудачи.  
> 10 ур.: позы больше не проваливаются.

## Important non-regression constraints

- Keep `ActivityFixes/media/perks.txt` Yoga-only. Do not re-register Lifestyle, Art, Cleaning, Dancing, Meditation or Music there.
- Do not remove the Yoga proxy perk unless the standard Skills-panel exposure is intentionally removed.
- Do not restore the old `ISCharacterInfo.loadPerk` / `DividerMeditationNew` Yoga filtering.
- Do not embed Russian fallback descriptions directly into these client Lua files.
- Prefer stable internal perk ids over localized display names for tooltip resolution.
- `RussianTextFixes` is required for the patch-owned Russian fallback strings used by ActivityFixes.

## Validation

The final combination was tested in-game after updating the local test copies of the split patches.

Result confirmed by the tester:

- all previously missing/corrupted descriptions render normally;
- Medicine, Animal Care, short/long blade and blunt skills are fixed;
- Sprinting and Lightfoot are fixed;
- Yoga is visible, synchronized with Lifestyle progression and uses the concise Russian description;
- unaffected skills continue to display correctly.

**Final result: issue closed.**
