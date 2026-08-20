# Lacccka B42 NPC Combat Experimental

This Workshop project isolates NPC-combat fixes, diagnostics and admin test tooling that are still under active investigation. Stable, already-validated runtime compatibility hooks remain in `RuntimeFixes`.

The public package name, Mod ID and Workshop description are intentionally generic and do not reference the upstream mod. Internally, the current implementation integrates with `Bandits2` / Bandits APIs because that is the compatibility path under test. The project contains LCC-authored hooks only and must not contain complete third-party Lua files or assets.

## Workshop publication

- Workshop ID: `3786817782`
- Mod ID: `LaccckaB4220NPCCombatExperimental`
- Public title: `Lacccka B42 NPC Combat Experimental`
- Current visibility: `public`
- Current experimental version: `0.1.1`

The repository must keep this Workshop ID pinned in `workshop.txt` and in the grouped Workshop audit. The item is published independently from the stable RuntimeFixes package.

## 1. Zombie -> NPC AttackState investigation

The first experimental implementation attempted to clear `bAttack` when a normal `IsoZombie` targeted an NPC represented by the upstream integration as another `IsoZombie`. B42.20.3 testing disproved that intervention: `bAttack` is exposed through a read-only animation callback variable, and `zombie:setVariable("bAttack", false)` produces `AnimationVariableSlotCallback.trySetValue: Trying to set read-only variable "battack"` while a subsequent read still returns `true`.

The old `[BLOCK]` counter therefore measured attempted writes rather than successful blocks and must not be treated as evidence that the crash path was fixed. The probe is now observe-only: it never calls `setVariable("bAttack", ...)`, `changeState()`, `setTarget()` or `setBumpType()`.

`[LCC][BanditsAttackGuard][READ_ONLY_BATTACK]` records transitions where `bAttack=true` is observed on a normal zombie targeting an NPC. `[ESCAPED_ATTACK_STATE]` records actual `attack` / `attack-network` entry and explicitly marks itself `diagnosticOnly=true`. The original dangerous precondition remains unresolved until a mutable pre-AttackState seam is identified.

## 2. Target diagnostics

`zzz_LCC_BanditsTargetDiagnostics.lua` remains observe-only. It records NPC target acquisition/loss, `bAttack`, vanilla attack-state entry and the upstream custom `Bite` / `BiteLow` activity. It is intentionally colocated with the experimental probe so the stable RuntimeFixes package does not carry diagnostic logging.

## 3. Death-loot / naked-corpse diagnostics

B42.20 source inspection found two upstream behaviors that plausibly combine into the long-standing naked/empty corpse bug:

1. `Bandit.ApplyVisuals()` clears `bandit:getWornItems()` and reconstructs configured clothing primarily as `ItemVisual` objects. The NPC can therefore look dressed while not owning equivalent real worn `InventoryItem` objects.
2. the upstream `OnZombieDead` handler removes inventory entries that do not have `item:getModData().preserve`, while `UpdateItemsToSpawnAtDeath()` is responsible for marking inventory and preparing the death-item manifest. Timing or synchronization gaps can therefore destroy items at death before the corpse is created.

The current package does **not** yet mutate this pipeline. `zzz_LCC_BanditsDeathLootDiagnostics.lua` wraps the existing `Bandit.UpdateItemsToSpawnAtDeath()` only to snapshot counts, then observes `OnZombieDead` and `OnDeadBodySpawn` without adding/removing/cloning/wearing items.

Expected diagnostic sequence for an affected NPC:

- `[LCC][BanditsDeathLoot][DEAD]` — inventory/worn counts after the upstream death cleanup plus the last manifest snapshot;
- `[LCC][BanditsDeathLoot][CORPSE]` — resulting corpse container/worn counts for the same `brainId`.

This is intended to prove exactly where the loss occurs before introducing a preservation fix and avoids creating duplicate loot by guessing at the wrong ownership layer.

## 4. Admin context-menu stress spawner

`zzz_LCC_BanditsAdminSpawnMenu.lua` provides the admin/debug right-click action for queuing a small NPC stress-test group.

`zzz_LCC_BanditsTestSpawnBridge.lua` is the matching dedicated-server command bridge. Both files must move together: the client menu sends `LCCBanditsTest/SpawnOne`, and the server bridge validates staff access, calls the existing upstream `BanditServer.Spawner.Clan` path and logs whether an NPC was actually created.

The spawner is test tooling, not a production gameplay feature.

## Relationship to RuntimeFixes

`RuntimeFixes` keeps only the already-accepted compatibility hooks: empty-server wanderer protection, squareless/cache lifecycle protection, farming guards, dedicated NPC lookup and the legacy character-screen path shim.

`NPCCombatExperimental` may be enabled alongside `RuntimeFixes`. Its `mod.info` loads after `Bandits2` and after `LaccckaB4220RuntimeFixes` when that stable patch is present.

## Regression checklist

- verify `RuntimeFixes` loads without any `[LCC][BanditsDiag]`, `[LCC][BanditsAttackGuard]`, `[LCC][BanditsDeathLoot]` or `[LCC][BanditsSpawn]` initialization lines;
- enable `NPCCombatExperimental` and verify attack-state observation, target diagnostics, death-loot diagnostics and the admin test tooling initialize;
- confirm the client no longer emits LCC-triggered `Trying to set read-only variable "battack"` warnings;
- stress normal zombie -> NPC combat and record `READ_ONLY_BATTACK`, `ESCAPED_ATTACK_STATE` and `DANGER_ATTACK_STATE`; non-zero attack-state observations mean the crash path remains unresolved;
- verify ordinary zombie -> `IsoPlayer` attacks remain unchanged;
- spawn and kill multiple NPCs by player and zombie damage; for each affected death compare `DEAD` and `CORPSE` counts using the same ID;
- specifically test NPCs with visible clothing, bags and carried/collected inventory so an empty-corpse transition can be distinguished from an NPC that legitimately had little loot;
- disable `NPCCombatExperimental` again and verify stable RuntimeFixes behavior is identical to the pre-experiment setup.

## Repository preview state

`preview.png` is versioned in the Workshop root through Git LFS. The repository pointer currently resolves to a 114483-byte PNG, and the grouped audit must require the preview for this published item just like it does for the other published split patches.
