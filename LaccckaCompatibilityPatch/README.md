# Lacccka B42.20 Compatibility Patch

Compatibility patch for the Project Zomboid Build 42.20.x server/mod set maintained in this repository.

## Current scope

The patch contains targeted B42.20 compatibility fixes and guards for mods used by the server, including MFS, SVU3/TsarLib, PZK VLC, zRe, Bandits, Lifestyle, Aegis Panel and Federal Ranger's Chimera.

### Lifestyle: Yoga progress

Lifestyle stores **Yoga** as a hidden skill in `LSHiddenSkills.Yoga`, so the base mod does not expose its level in the normal character skill panel.

The patch adds a UI-only, non-passive `Yoga` perk under the existing `Lifestyle` parent and renders its progress directly from Lifestyle's authoritative hidden-skill data:

- level 0–10;
- current XP / XP required for the next level;
- normal ten-segment skill progress display;
- Russian skill name and description;
- no duplicate gameplay XP and no save migration;
- manual level-up clicks are disabled because Yoga levels automatically through practice;
- the row is hidden together with the Meditation/Yoga feature if that Lifestyle sandbox section is disabled;
- HiddenSkills reads are guarded, so an upstream API change logs a warning instead of crashing the character panel.

The implementation is in:

- `42/media/perks.txt`
- `42/media/lua/client/zzz_LCC_LifestyleYogaProgress.lua`

The proxy XP thresholds intentionally mirror Lifestyle's `Helper/HSMng.lua` table. A static audit checks this contract so a future Lifestyle update cannot silently desynchronize the displayed Yoga progress.

### Russian localization

The patch supplies Russian localization for the current Lifestyle content, including:

- UI and context menus;
- Meditation, Yoga and the full Yoga tutorial;
- moodles and traits;
- sandbox settings;
- items and recipes;
- inventions and research UI;
- ambitions;
- art, painting and sculpture systems;
- movable objects;
- recorded training media;
- tooltips and gameplay descriptions.

For the very large generated painting-name catalogue, semantic Russian names are kept where available and the remaining catalogue is covered by patch localization so English placeholders do not leak into normal Russian UI. Individual titles can still be polished without changing their IDs.

Canonical song titles are intentionally kept in their original spelling. The translation coverage audit treats those ContextMenu song-title keys as valid original-title fallbacks rather than missing Russian UI strings.

The patch also continues to include the Russian Bandits localization used by the server.

### Automatic audits

`.github/workflows/lifestyle-translation-audit.yml` runs two repository checks whenever relevant Lifestyle or patch files change:

1. `tools/audit_lifestyle_translation.py`
   - validates EN/RU JSON syntax;
   - compares current Lifestyle translation keys with the patch's Russian coverage;
   - checks `%1`, `%2`, `%%` placeholder compatibility in JSON translations;
   - reports missing translatable keys while excluding canonical song-title keys.
2. `tools/audit_lifestyle_yoga_proxy.py`
   - verifies Yoga is still a Lifestyle HiddenSkill;
   - verifies the `LSHiddenSkills` storage/API contract used by the patch;
   - verifies proxy XP thresholds match Lifestyle levels 0–9;
   - verifies Lifestyle still does not define a native Yoga perk;
   - verifies the Lifestyle Skills-panel override remains compatible;
   - verifies required Russian Yoga UI/tutorial/tooltip keys are present.

These checks are intended to fail loudly when a future Lifestyle update invalidates assumptions made by the compatibility patch.

## Load order

`mod.info` declares `loadafter` dependencies for the mods whose Lua/UI behavior is patched. Keep `LaccckaB4220Compat` after Lifestyle and the other listed dependencies in the server `Mods=` order.

## Test checklist

1. Start a client with Russian language and the server's normal mod order.
2. Open **Персонаж → Навыки → Образ жизни** and verify that **Йога** appears in the same Lifestyle group as **Медитация**.
3. Complete at least one Yoga pose, reopen the skills panel and verify that Yoga XP increased.
4. Reconnect to the server and verify that the same Yoga level/XP is restored from `LSHiddenSkills`.
5. Hover Yoga progress and verify the Russian level/XP tooltip.
6. Open the Yoga tutorial, Lifestyle sandbox settings, inventions, ambitions and art UI and check for untranslated English keys/text.
7. Check the client log for `[LaccckaCompatibilityPatch] Lifestyle Yoga progress UI installed` and make sure no Yoga warning/error follows it.
