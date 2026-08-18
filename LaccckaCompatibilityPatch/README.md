# Lacccka B42.20 Compatibility Patch

Compatibility patch for the Project Zomboid Build 42.20.x server/mod set maintained in this repository.

## Current scope

The patch contains targeted B42.20 compatibility fixes and guards for mods used by the server, including MFS, SVU3/TsarLib, PZK VLC, zRe, Bandits, Lifestyle, Aegis Panel and Federal Ranger's Chimera.

## Platform support

The gameplay/Lua side of the patch is intended to be platform-neutral and is supported on both Windows and Linux. Platform-specific workarounds live outside the gameplay files so a Linux fix cannot accidentally change Windows behavior.

### Bandits on Linux: AnimSets case-sensitivity

Bandits ships its animation tree under `mods/Bandits/common/media/AnimSets`. In Build 42.20 the XML `x_extends` resolver can request the inherited file through a lower-cased filesystem path such as `mods/bandits/common/media/animsets/...`. Windows normally hides this mismatch because its common filesystems are case-insensitive; Linux does not.

The repository therefore provides `server/linux/start-server.sh`. Before starting Project Zomboid it creates two idempotent Linux-only aliases:

- `mods/bandits -> Bandits`
- `mods/Bandits/common/media/animsets -> AnimSets`

The upstream Bandits files are not modified. Existing non-matching files/symlinks cause a hard error instead of being overwritten. On non-Linux systems the preflight is skipped.

For the dedicated server, place the repository launcher in the Project Zomboid server root as `start-server.sh` (next to `ProjectZomboid64`) and start it with the same arguments as the stock launcher, for example:

```bash
bash start-server.sh -servername servertest
```

A successful first Linux preflight prints `[LCC][Linux][OK]` for the aliases. If Bandits has not been downloaded yet, the launcher warns you; after Steam finishes the first Workshop download, stop and start the server once more so the aliases exist before AnimSets are parsed.

The patch also contains the Bandits dedicated-server Lua guards/overrides used by the server, including the missing `BanditZombie.GetInstanceById()` contract and the wanderer scheduler fix for empty dedicated multiplayer sessions.

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

`perks.txt` intentionally contains no Lua-style `--` comments: Build 42's `CustomPerks` parser treats them as an unknown block type. The compatibility workflow checks this invariant.

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

`.github/workflows/lifestyle-translation-audit.yml` validates Lifestyle translation/Yoga contracts.

`.github/workflows/compat-contract-audit.yml` validates high-risk compatibility overrides and cross-platform invariants, including:

- strict upstream contracts for compatibility files;
- rejection of Lua-style comments in `media/perks.txt`;
- `bash -n` syntax validation for the Linux launcher;
- presence of both Bandits Linux case aliases.

These checks are intended to fail loudly when a future mod or patch update invalidates assumptions made by the compatibility layer.

## Load order

`mod.info` declares `loadafter` dependencies for the mods whose Lua/UI behavior is patched. Keep `LaccckaB4220Compat` after Lifestyle and the other listed dependencies in the server `Mods=` order.

## Test checklist

1. On Linux, start through `server/linux/start-server.sh` copied to the PZ server root and verify the `[LCC][Linux][OK]` aliases are created.
2. On Linux, verify the server log no longer contains `FileNotFoundException` paths under `mods/bandits/common/media/animsets`.
3. On Windows, start normally and verify no Linux-specific filesystem changes are required.
4. Verify the server log no longer reports `CustomPerks.readFile ... unknown block type "--"`.
5. Start a client with Russian language and the server's normal mod order.
6. Open **Персонаж → Навыки → Образ жизни** and verify that **Йога** appears in the same Lifestyle group as **Медитация**.
7. Complete at least one Yoga pose, reopen the skills panel and verify that Yoga XP increased.
8. Reconnect to the server and verify that the same Yoga level/XP is restored from `LSHiddenSkills`.
9. Check the client/server logs for `[LCC][Guard][OK]` messages and make sure no compatibility feature is unexpectedly disabled.
