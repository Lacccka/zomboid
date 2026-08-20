# Lacccka B42 NPC Combat Experimental

This Workshop project isolates NPC-combat fixes, diagnostics and admin test tooling that are still under active investigation. Stable, already-validated runtime compatibility hooks remain in `RuntimeFixes`.

The public package name, Mod ID and Workshop description are intentionally generic and do not reference the upstream mod. Internally, the current implementation integrates with `Bandits2` / Bandits APIs because that is the compatibility path under test. The project contains LCC-authored hooks only and must not contain complete third-party Lua files or assets.

## Workshop publication

- Workshop ID: `3786817782`
- Mod ID: `LaccckaB4220NPCCombatExperimental`
- Public title: `Lacccka B42 NPC Combat Experimental`
- Current visibility: `public`

The repository must keep this Workshop ID pinned in `workshop.txt` and in the grouped Workshop audit. The item is published independently from the stable RuntimeFixes package.

## 1. Zombie -> NPC AttackState guard

`zzz_LCC_BanditsAttackStateGuard.lua` clears `bAttack` only when a normal `IsoZombie` currently targets an NPC represented by the upstream integration as an `IsoZombie`. It does not change target selection, aggro, pathfinding, `NoLungeAttack`, bump type, custom bite bookkeeping, damage, or infection. It never forces `bAttack=true` and does not call `changeState()`.

Interventions are logged as `[LCC][BanditsAttackGuard][BLOCK]`. If `attack` / `attack-network` is already visible to the callback, `[ESCAPED_ATTACK_STATE]` is emitted so a too-late Lua guard can be detected instead of silently treated as fixed.

## 2. Target diagnostics

`zzz_LCC_BanditsTargetDiagnostics.lua` remains observe-only. It records NPC target acquisition/loss, `bAttack`, vanilla attack-state entry and the upstream custom `Bite` / `BiteLow` activity. It is intentionally colocated with the experimental guard so the stable RuntimeFixes package does not carry diagnostic logging.

## 3. Admin context-menu stress spawner

`zzz_LCC_BanditsAdminSpawnMenu.lua` provides the admin/debug right-click action for queuing a small NPC stress-test group.

`zzz_LCC_BanditsTestSpawnBridge.lua` is the matching dedicated-server command bridge. Both files must move together: the client menu sends `LCCBanditsTest/SpawnOne`, and the server bridge validates staff access, calls the existing upstream `BanditServer.Spawner.Clan` path and logs whether an NPC was actually created.

The spawner is test tooling, not a production gameplay feature.

## Relationship to RuntimeFixes

`RuntimeFixes` keeps only the already-accepted compatibility hooks: empty-server wanderer protection, squareless/cache lifecycle protection, farming guards, dedicated NPC lookup and the legacy character-screen path shim.

`NPCCombatExperimental` may be enabled alongside `RuntimeFixes`. Its `mod.info` loads after `Bandits2` and after `LaccckaB4220RuntimeFixes` when that stable patch is present.

## Regression checklist

- verify `RuntimeFixes` loads without any `[LCC][BanditsDiag]`, `[LCC][BanditsAttackGuard]` or `[LCC][BanditsSpawn]` initialization lines;
- enable `NPCCombatExperimental` and verify all three experimental subsystems initialize;
- use the admin right-click action and confirm `BATCH_QUEUED`, client `SEND`, server `SERVER_BEGIN` and `SERVER_RESULT` messages;
- stress normal zombie -> NPC combat; `BLOCK` must become non-zero while the upstream custom bite activity continues;
- `ESCAPED_ATTACK_STATE` and diagnostic `DANGER_ATTACK_STATE` should remain zero after the guard is active;
- verify ordinary zombie -> `IsoPlayer` attacks remain unchanged;
- disable `NPCCombatExperimental` again and verify stable RuntimeFixes behavior is identical to the pre-experiment setup.

## Repository preview state

`preview.png` is versioned in the Workshop root through Git LFS. The repository pointer currently resolves to a 114483-byte PNG, and the grouped audit must require the preview for this published item just like it does for the other published split patches.
