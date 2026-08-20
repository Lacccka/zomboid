# Lacccka B42 NPC Combat Experimental

This Workshop project isolates NPC-combat fixes, diagnostics and admin test tooling that are still under active investigation. Stable, already-validated runtime compatibility hooks remain in `RuntimeFixes`.

The public package name, Mod ID and Workshop description are intentionally generic. Internally, the current implementation integrates with `Bandits2` / Bandits APIs because that is the compatibility path under test. The project contains LCC-authored hooks only and must not contain complete third-party Lua files or assets.

## Workshop publication

- Workshop ID: `3786817782`
- Mod ID: `LaccckaB4220NPCCombatExperimental`
- Public title: `Lacccka B42 NPC Combat Experimental`
- Current visibility: `public`
- Current experimental version: `0.1.2`

## 1. Zombie -> NPC AttackState investigation

The observed client crash is a vanilla Java `AttackState` path receiving an NPC represented as an `IsoZombie` where the engine later assumes an `IsoPlayer`. Diagnostics repeatedly reproduced the dangerous precondition: a normal `IsoZombie` holds a Bandit `IsoZombie` as its vanilla target and enters `attack` / `attack-network`.

Two interventions are now disproven on B42.20.3:

1. clearing `bAttack` does not work because it is exposed through a read-only animation callback variable;
2. asserting `bandit:setZombiesDontAttack(true)` early does not prevent the vanilla target/AttackState path.

The second result is conclusive from the 2026-08-20 stress test. With the target-side guard active and `earlyHook=true`, one sample reached `protectedBandits=10`, `targetLeaks=650`, `bAttackObserved=290`, and `attackStateObserved=103` while upstream custom combat remained active with `customBiteStart=92`. The target-side flag is therefore insufficient as a standalone crash fix.

Source inspection explains why. Bandits `UpdateZombies()` explicitly rebuilds a full vanilla combat relationship for the selected NPC:

```lua
zombie:spotted(bandit, true)
zombie:addAggro(bandit, 1)
zombie:setTarget(bandit)
zombie:setAttackedBy(bandit)
```

Bandits also maintains an independent NPC scan / custom `Bite` / `BiteLow` simulation, so retaining that vanilla target after the upstream callback is not required for the custom damage path we are trying to preserve.

### Current v3 experiment

`zzz_LCC_BanditsAttackStateGuard.lua` now runs later in `OnZombieUpdate`. When and only when a normal zombie currently targets a Bandit NPC, it records the state and then executes:

```lua
zombie:setTarget(nil)
```

This happens after upstream Bandits has completed its own update logic for that tick. The v3 guard deliberately does **not** call `clearAggroList()`: Build 42 exposes no proven single-target aggro-removal API here, and clearing the entire list could erase legitimate aggro toward nearby players. It also does not call `changeState()`, change `Bite` / `BiteLow`, write `bAttack`, or alter damage/infection bookkeeping.

Expected v3 logs:

- `[LCC][BanditsAttackGuard][BOOT] ... target-disconnect ...`;
- `[PROTECT_TARGET]` — supplemental target-side flag was asserted;
- `[DISCONNECT]` — a vanilla zombie -> NPC target was removed; `disconnected=true` is required;
- `[READ_ONLY_BATTACK]` — diagnostic observation only;
- `[ESCAPED_ATTACK_STATE]` — the callback still found the zombie already inside vanilla AttackState before/while the target was disconnected;
- `[SUMMARY]` — includes `targetLeaks`, `targetDisconnects`, `disconnectFailures`, and `attackStateObserved`.

A successful v3 test requires `targetDisconnects > 0`, `disconnectFailures=0`, continued upstream `customBiteStart > 0`, and a decisive reduction to zero (or a clearly understood transient startup-only case) in `attackStateObserved`. Ordinary zombie -> `IsoPlayer` attacks must remain unchanged.

## 2. Target diagnostics

`zzz_LCC_BanditsTargetDiagnostics.lua` remains observe-only. It records target acquisition/switch/loss, `bAttack`, vanilla AttackState entry and upstream custom `Bite` / `BiteLow` activity. It is intentionally colocated with the experimental guard so stable RuntimeFixes does not carry stress-test logging.

## 3. Death-loot / naked-corpse investigation

The current evidence shows that Bandits death cleanup is not the primary point where clothing disappears. In repeated bad deaths the NPC already reaches `DEAD phase=PRE_CLEANUP` with `inventory=0` and `worn=0` while still carrying 8-13 configured clothing entries as `ItemVisuals`. `POST_CLEANUP` remains `0/0`. Conversely, successful deaths can materialize a full corpse with the configured worn items, bag, weapons and generated loot.

This means at least two mechanisms must be distinguished:

1. configured clothing is often visual-only while the live NPC is active, so corpse materialization must turn that visual configuration into real worn inventory;
2. weapons, bag and generated loot use the upstream death-item spawn path and can independently fail to materialize in the resulting corpse.

`zzz_LCC_BanditsDeathLootDiagnostics.lua` remains mutation-free and records:

- `[BEFORE_UPDATE]` and `[AFTER_UPDATE]` around upstream `Bandit.UpdateItemsToSpawnAtDeath()`;
- `[DEAD] phase=PRE_CLEANUP`;
- `[DEAD] phase=POST_CLEANUP`;
- `[CORPSE]` with container and worn item types.

B42.20.3 does not provide a safe Lua getter for the native death queue, so the diagnostic no longer calls `getItemsToSpawnAtDeath()`. It also does not call `IsoDeadBody:getItemVisuals()` with the IsoZombie signature.

The positional corpse matcher now stores recent deaths as `id -> {x,y,z,time}` rather than an array. This fixes the Kahlua `attempted index: id of non-table: null` failure seen in the previous test and prevents one malformed correlation entry from disabling the complete death-diagnostics feature.

No preservation/materialization fix is enabled yet. The next corpse mutation will be added only after the diagnostic cleanly distinguishes a bad materialization from a good one, to avoid duplicating clothing, bags, weapons or ammunition on corpses that already work.

## 4. Admin context-menu stress spawner

`zzz_LCC_BanditsAdminSpawnMenu.lua` provides the admin/debug right-click action for queuing a small NPC stress-test group. `zzz_LCC_BanditsTestSpawnBridge.lua` is the matching dedicated-server command bridge and preserves upstream `BanditServer.Spawner.Clan` authority after staff validation.

The server-created logs include NPC id, clan/profile ids, clothing count, bag and weapon loadout so combat/death events can be correlated with a concrete preset.

## Relationship to RuntimeFixes

`RuntimeFixes` keeps only already-accepted compatibility hooks: empty-server wanderer protection, squareless/cache lifecycle protection, farming guards, dedicated NPC lookup and the legacy character-screen path shim.

`NPCCombatExperimental` may be enabled alongside `RuntimeFixes`. Its `mod.info` loads after `Bandits2` and after `LaccckaB4220RuntimeFixes` when that stable patch is present.

## Regression checklist

- fully restart client and dedicated server after updating the local patch;
- verify AttackGuard v3 prints `BOOT`, `INIT` and `EARLY_HOOK`;
- stress normal zombie -> NPC combat and verify `[DISCONNECT] ... disconnected=true` while custom `Bite` / `BiteLow` counters continue to grow;
- require `disconnectFailures=0` and inspect every remaining `[ESCAPED_ATTACK_STATE]` / `DANGER_ATTACK_STATE` before calling the Java crash path fixed;
- verify ordinary zombie -> player attacks remain normal;
- kill multiple visibly equipped NPCs and correlate `BEFORE_UPDATE`, `AFTER_UPDATE`, both `DEAD` phases and `CORPSE`;
- confirm death diagnostics no longer emits `getItemsToSpawnAtDeath`, `IsoDeadBody:getItemVisuals`, or `attempted index: id of non-table: null` errors;
- compare clearly bad corpses with a successful full corpse before enabling any inventory-preservation intervention;
- disable `NPCCombatExperimental` and verify stable RuntimeFixes behavior remains unchanged.

## Repository preview state

`preview.png` is versioned in the Workshop root through Git LFS and remains required for the published item.
