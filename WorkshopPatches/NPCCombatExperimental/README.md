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

The old `[BLOCK]` counter therefore measured attempted writes rather than successful blocks and must not be treated as evidence that the crash path was fixed.

The current experiment moves to an earlier target-side engine seam. Bandits' own `UpdateZombies()` logic independently scans `BanditZombie.CacheLightB`, paths normal zombies toward the selected NPC and simulates `Bite` / `BiteLow`. The upstream code also calls `bandit:setZombiesDontAttack(true)`, but only once close-range bite handling is already underway. `zzz_LCC_BanditsAttackStateGuard.lua` now asserts that engine flag for every live Bandit as early as the guarded `Bandit.ApplyVisuals()` path and again from `OnZombieUpdate`.

The guard intentionally does **not** call `setTarget()`, `clearAggroList()`, `changeState()`, `setBumpType()` or write `bAttack`. This first active stage tests whether moving `setZombiesDontAttack(true)` earlier is sufficient to prevent the normal-zombie -> Bandit vanilla target relationship without disturbing Bandits' own pathing and custom bite simulation.

Expected logs:

- `[LCC][BanditsAttackGuard][PROTECT_TARGET]` — a Bandit target was observed with the engine no-attack flag asserted;
- `[TARGET_LEAK]` — a normal zombie still acquired that protected Bandit as its vanilla target;
- `[READ_ONLY_BATTACK]` — `bAttack=true` was observed on such a leak; the value remains diagnostic-only;
- `[ESCAPED_ATTACK_STATE]` — the zombie still entered `attack` / `attack-network` despite target-side protection.

Success criterion for this stage is `protectedBandits > 0` with `targetLeaks=0` and `attackStateObserved=0` while the upstream `Bite` / `BiteLow` counters remain active. If protected targets still leak, the next experimental stage may clear only the vanilla target/aggro relationship while leaving Bandits' independent NPC search/pathing intact.

## 2. Target diagnostics

`zzz_LCC_BanditsTargetDiagnostics.lua` remains observe-only. It records NPC target acquisition/loss, `bAttack`, vanilla attack-state entry and the upstream custom `Bite` / `BiteLow` activity. It is intentionally colocated with the experimental guard so the stable RuntimeFixes package does not carry diagnostic logging.

## 3. Death-loot / naked-corpse diagnostics

B42.20 source inspection found two upstream behaviors that plausibly combine into the long-standing naked/empty corpse bug:

1. `Bandit.ApplyVisuals()` clears `bandit:getWornItems()` and reconstructs configured clothing primarily as `ItemVisual` objects. The NPC can therefore look dressed while not owning equivalent real worn `InventoryItem` objects.
2. the upstream `OnZombieDead` handler removes inventory entries that do not have `item:getModData().preserve`, while `UpdateItemsToSpawnAtDeath()` is responsible for marking inventory and preparing the death-item manifest. Timing or synchronization gaps can therefore destroy items at death before the corpse is created.

The previous diagnostic registered its single `OnZombieDead` observer from shared Lua. Because `BanditUpdate.lua` is client Lua, that observer can run **before** the upstream cleanup even though the old log field was named `postCleanupInventory`. That ambiguity is now removed.

`zzz_LCC_BanditsDeathLootDiagnostics.lua` remains mutation-free and captures four explicit stages:

- `[BEFORE_UPDATE]` — the last real inventory/worn/item-visual state immediately before upstream `Bandit.UpdateItemsToSpawnAtDeath()`;
- `[AFTER_UPDATE]` — the same state immediately after the upstream function returns, plus a best-effort death-queue count when B42 exposes it;
- `[DEAD] phase=PRE_CLEANUP` — the Bandit at the start of `OnZombieDead`, before the client `BanditUpdate.lua` cleanup handler;
- `[DEAD] phase=POST_CLEANUP` — the same object after the upstream death handler has removed non-preserved inventory and deprovisioned the Bandit;
- `[CORPSE]` — the resulting `IsoDeadBody` container/worn/item-visual state for the same `brainId`.

The death logs also include configured `brain.clothing`, bag identity and concrete top-level inventory/worn item types. This should tell us whether a naked corpse starts with an empty death manifest, loses items specifically inside the upstream cleanup handler, or reaches cleanup intact and is lost only during zombie -> corpse materialization.

No preservation fix is enabled yet. The next mutation should target only the stage proven to lose the items, so successful corpses do not gain duplicated clothing, weapons or bags.

## 4. Admin context-menu stress spawner

`zzz_LCC_BanditsAdminSpawnMenu.lua` provides the admin/debug right-click action for queuing a small NPC stress-test group.

`zzz_LCC_BanditsTestSpawnBridge.lua` is the matching dedicated-server command bridge. Both files must move together: the client menu sends `LCCBanditsTest/SpawnOne`, and the server bridge validates staff access, calls the existing upstream `BanditServer.Spawner.Clan` path and logs whether an NPC was actually created.

The spawner is test tooling, not a production gameplay feature.

## Relationship to RuntimeFixes

`RuntimeFixes` keeps only the already-accepted compatibility hooks: empty-server wanderer protection, squareless/cache lifecycle protection, farming guards, dedicated NPC lookup and the legacy character-screen path shim.

`NPCCombatExperimental` may be enabled alongside `RuntimeFixes`. Its `mod.info` loads after `Bandits2` and after `LaccckaB4220RuntimeFixes` when that stable patch is present.

## Regression checklist

- verify `RuntimeFixes` loads without any `[LCC][BanditsDiag]`, `[LCC][BanditsAttackGuard]`, `[LCC][BanditsDeathLoot]` or `[LCC][BanditsSpawn]` initialization lines;
- enable `NPCCombatExperimental` and verify `BanditsAttackGuard`, target diagnostics, four-stage death-loot diagnostics and the admin test tooling initialize;
- confirm the client no longer emits LCC-triggered `Trying to set read-only variable "battack"` warnings;
- verify `[PROTECT_TARGET]` appears for spawned Bandits and summary `protectedBandits` grows;
- stress normal zombie -> NPC combat and record `TARGET_LEAK`, `READ_ONLY_BATTACK`, `ESCAPED_ATTACK_STATE` and `DANGER_ATTACK_STATE`; the desired result is zero target/AttackState leaks while custom `Bite` / `BiteLow` remains non-zero;
- verify ordinary zombie -> `IsoPlayer` attacks remain unchanged;
- spawn and kill multiple NPCs by player and zombie damage; for each affected death compare `BEFORE_UPDATE`, `AFTER_UPDATE`, `DEAD phase=PRE_CLEANUP`, `DEAD phase=POST_CLEANUP` and `CORPSE` using the same ID;
- specifically test NPCs with visible clothing, bags and carried/collected inventory so an empty-corpse transition can be distinguished from an NPC that legitimately had little loot;
- disable `NPCCombatExperimental` again and verify stable RuntimeFixes behavior is identical to the pre-experiment setup.

## Repository preview state

`preview.png` is versioned in the Workshop root through Git LFS. The repository pointer currently resolves to a 114483-byte PNG, and the grouped audit must require the preview for this published item just like it does for the other published split patches.
