# Lacccka B42.20 Compatibility Patch

Compatibility patch for the Project Zomboid Build 42.20.x server/mod set maintained in this repository.

## Current scope

The patch contains targeted B42.20 compatibility fixes and guards for mods used by the server, including MFS, SVU3/TsarLib, PZK VLC, zRe, Bandits, Lifestyle, Aegis Panel and Federal Ranger's Chimera.

### Lifestyle: Yoga progress

Lifestyle stores **Yoga** as a hidden skill in `LSHiddenSkills.Yoga`, so the base mod does not expose its level in the normal character skill panel.

The patch adds a UI-only `Yoga` perk under the existing `Lifestyle` parent and renders its progress directly from Lifestyle's authoritative hidden-skill data:

- level 0–10;
- current XP / XP required for the next level;
- normal ten-segment skill progress display;
- Russian skill name and description;
- no duplicate gameplay XP and no save migration;
- manual level-up clicks are disabled because Yoga levels automatically through practice;
- the row is hidden together with the Meditation/Yoga feature if that Lifestyle sandbox section is disabled.

The implementation is in:

- `42/media/perks.txt`
- `42/media/lua/client/zzz_LCC_LifestyleYogaProgress.lua`

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

For the very large generated painting-name catalogue, the existing semantic Russian names are kept where available; remaining entries use a Russian numbered fallback (`Картина №...`) so no English painting placeholder leaks into the Russian UI. These can be replaced incrementally with individual literary titles without changing IDs.

The patch also continues to include the Russian Bandits localization used by the server.

## Load order

`mod.info` declares `loadafter` dependencies for the mods whose Lua/UI behavior is patched. Keep `LaccckaB4220Compat` after Lifestyle and the other listed dependencies in the server `Mods=` order.

## Test checklist

1. Start a client with Russian language and the server's normal mod order.
2. Open **Персонаж → Навыки → Образ жизни** and verify that **Йога** appears in the same Lifestyle group as **Медитация**.
3. Complete at least one Yoga pose, reopen the skills panel and verify that Yoga XP increased.
4. Reconnect to the server and verify that the same Yoga level/XP is restored from `LSHiddenSkills`.
5. Hover Yoga progress and verify the Russian level/XP tooltip.
6. Open the Yoga tutorial, Lifestyle sandbox settings, inventions, ambitions and art UI and check for untranslated English keys/text.
