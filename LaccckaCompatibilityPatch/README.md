# Lacccka B42.20 Compatibility Patch

Compatibility patch for the Project Zomboid Build 42.20.x server/mod set maintained in this repository.

## Current scope

The patch contains targeted B42.20 compatibility fixes and guards for mods used by the server, including MFS, SVU3/TsarLib, PZK VLC, zRe, Bandits, Lifestyle, Aegis Panel and Federal Ranger's Chimera.

## Platform support

The gameplay/Lua side of the patch is intended to be platform-neutral and is supported on both Windows and Linux. Platform-specific workarounds live outside the gameplay files so a Linux fix cannot accidentally change Windows behavior.

### Linux: B42.20 XML/AnimSets case-sensitivity

Build 42.20 can lowercase filesystem components while resolving animation XML `x_extends` paths. Windows normally hides this mismatch because its common filesystems are case-insensitive; Linux does not.

The problem has been reproduced in the server logs for:

- Bandits;
- Lifestyle;
- Escape from Kentucky4215;
- tsarslib.

The resolver can lowercase the mod directory, `AnimSets`, and in some cases the XML filename itself. Lifestyle is an important example because mixed-case filenames such as `LSKnifeDefault.xml` can be requested as `lsknifedefault.xml`.

The repository therefore provides `server/linux/start-server.sh`. Before starting Project Zomboid it:

- creates lowercase aliases for affected mod directories when required;
- creates `animsets -> AnimSets` aliases for every existing affected media tree;
- creates lowercase aliases for mixed-case entries inside affected `AnimSets` trees;
- checks `common/media`, `42/media`, `42.20/media` and root `media` layouts;
- keeps the aliases alive during the first 180 seconds of startup because Project Zomboid performs its own Workshop pass after the launcher starts and Steam can refresh a mod directory;
- creates `${HOME}/Zomboid/mods` to avoid the dedicated-server file-watcher error when that optional directory is absent;
- writes launcher diagnostics to `logs/jvm/launcher.log`.

The upstream Workshop files are not replaced. Existing non-matching files or symlinks cause a hard compatibility error instead of being overwritten. On non-Linux systems the preflight is skipped.

For the dedicated server, place the repository launcher in the Project Zomboid server root as `start-server.sh` (next to `ProjectZomboid64`) and start it with the same arguments as the stock launcher, for example:

```bash
bash start-server.sh -servername servertest
```

After a test launch, keep both the normal server console and `logs/jvm/launcher.log`. The launcher log records exactly which compatibility aliases were created or rejected.

The patch also contains the Bandits dedicated-server Lua guards/overrides used by the server, including the missing `BanditZombie.GetInstanceById()` contract and the wanderer scheduler fix for empty dedicated multiplayer sessions.

### Lifestyle: Yoga progress

Lifestyle stores Yoga progress separately from the ordinary Project Zomboid perk list. The patch exposes that progress in the normal character skill panel without creating a second gameplay progression system.

The patch adds a UI-only, non-passive `Yoga` perk under the existing `Lifestyle` parent and renders its progress directly from Lifestyle's authoritative data:

- level 0–10;
- current XP / XP required for the next level;
- normal ten-segment skill progress display;
- Russian skill name and concise player-facing description;
- no duplicate gameplay XP and no save migration;
- manual level-up clicks are disabled because Yoga levels automatically through practice;
- the row is hidden together with the Meditation/Yoga feature if that Lifestyle sandbox section is disabled;
- upstream data reads are guarded, so an API change logs a warning instead of crashing the character panel.

The implementation is in:

- `42/media/perks.txt`
- `42/media/lua/client/zzz_LCC_LifestyleYogaProgress.lua`

The patch `perks.txt` is a merge of the upstream Lifestyle perk registrations plus Yoga. It must preserve Lifestyle, Art, Cleaning, Dancing, Meditation and Music rather than replacing the upstream file with Yoga alone.

`perks.txt` intentionally contains no Lua-style `--` comments: Build 42's `CustomPerks` parser treats them as an unknown block type. The compatibility workflow checks this invariant.

### Player-facing skill descriptions

The patch adds concise Russian descriptions to the skill panel. Descriptions are written for players rather than for patch developers: they explain what a skill improves and mention important progression effects without implementation jargon.

Lifestyle descriptions cover:

- Искусство;
- Уборка;
- Танцы;
- Медитация, including the important 3 / 6 / 10 level milestones;
- Музыка, including the important 2 / 4 / 6 level milestones;
- Йога.

`ZZ_LCC_VanillaPerks_RU.txt` also covers the vanilla and Build 42 skill set, including physical, agility, combat, firearm, crafting and survival skills plus the Build 42 production/animal skills such as Blacksmithing, Masonry, Pottery, Knapping, Carving, Glassmaking, Animal Care, Butchering and Tracking.

Compatibility aliases for renamed/internal keys are included where useful, for example Sprinting/Running, Farming/Agriculture, Doctor/First Aid and MetalWelding/Welding. These aliases only supply UI text; they do not modify the underlying perks.

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

### Known cross-platform checksum issue

Build 42.20 Linux dedicated servers can falsely reject Windows clients during `DoLuaChecksum` even when the reported Workshop file exists on the client. This has been reproduced in the current server/client setup.

The compatibility patch does not yet claim to repair the Java checksum implementation. `DoLuaChecksum=false` is therefore a temporary test/workaround for the affected server, not the final intended security configuration. Restoring checksum validation remains a separate compatibility task.

### Automatic audits

`.github/workflows/lifestyle-translation-audit.yml` validates Lifestyle translation/Yoga contracts.

`.github/workflows/compat-contract-audit.yml` validates high-risk compatibility overrides and cross-platform invariants, including:

- strict upstream contracts for compatibility files;
- rejection of Lua-style comments in `media/perks.txt`;
- preservation of every upstream Lifestyle perk plus Yoga;
- `bash -n` syntax validation for the Linux launcher;
- presence of the affected Linux mod list, casefold tree support, startup alias keeper and launcher log;
- presence of the required Lifestyle descriptions and 3 / 6 / 10 Meditation milestones;
- presence of descriptions for the canonical vanilla/Build 42 skill keys;
- rejection of implementation jargon in player-facing skill descriptions.

These checks are intended to fail loudly when a future mod or patch update invalidates assumptions made by the compatibility layer.

## Load order

`mod.info` declares `loadafter` dependencies for the mods whose Lua/UI behavior is patched. Keep `LaccckaB4220Compat` after Lifestyle and the other listed dependencies in the server `Mods=` order.

## Test checklist

1. On Linux, copy `server/linux/start-server.sh` to the PZ server root and start the server through it.
2. Save `logs/jvm/launcher.log` and verify that the Linux compatibility preflight completed without conflicts.
3. Verify the server log no longer contains lowercase-path `FileNotFoundException` failures for Bandits, Lifestyle, Escape from Kentucky4215 or tsarslib AnimSets XML inheritance.
4. On Windows, start normally and verify no Linux-specific filesystem changes are required.
5. Verify the server log no longer reports `CustomPerks.readFile ... unknown block type "--"`.
6. Start a Russian client with the server's normal mod order.
7. Open **Персонаж → Навыки** and verify descriptions for vanilla/Build 42 skills such as Физподготовка, Сила and Первая помощь.
8. Open **Образ жизни** and verify descriptions for Искусство, Уборка, Танцы, Медитация, Музыка and Йога.
9. Verify the Meditation tooltip clearly lists the 3 / 6 / 10 milestones and the Yoga tooltip contains only player-facing effects.
10. Complete at least one Yoga pose, reopen the skills panel and verify that Yoga XP increased.
11. Reconnect to the server and verify that the same Yoga level/XP is restored.
12. Check client/server logs for `[LCC][Guard][OK]` messages and make sure no compatibility feature is unexpectedly disabled.
