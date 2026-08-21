# NPCFixes source-clean RC handoff — 2026-08-21

Branch: `agent/b42-20-compatibility-patch`

## Packaging decision

The supported compatibility project is split into independent Workshop items; there is no monolithic compatibility mod. `Bandits-LCC-Dev` remains an internal research/regression tree only and is excluded from the published-package audit.

Validated Bandits combat/death/corpse behavior has been promoted into a new stable responsibility boundary:

- folder: `WorkshopPatches/NPCFixes`
- Mod ID: `LaccckaB4220NPCFixes`
- public title: `Lacccka B42 NPC Fixes`
- RC version: `0.9.0`
- Workshop ID: staging `0`
- visibility: private until source-clean integration regression passes

`RuntimeFixes` remains low-level runtime/dedicated/API compatibility. `NPCCombatExperimental` is diagnostics/admin tooling only; its former mutating AttackState target-disconnect guard was removed so it cannot mask a failed stable fix.

## NPCFixes production contents

### Source-clean runtime transformers

`media/lua/client/BanditUpdate.lua`

Marker: `source-clean-coordinate-pursuit-v1`

This is not a copy of upstream BanditUpdate. It reads the installed `Bandits2` source with `getModFileReader()`, checks three exact B42.20 fingerprints and transforms in memory:

1. inject throttled coordinate-only pursuit helper;
2. replace active far `pathToCharacter(bandit)` with location pursuit;
3. replace close `spotted/addAggro/setTarget/setAttackedBy(bandit)` bridge with location pursuit.

The helper preserves the validated 0.75-tile destination alignment threshold and 750 ms idle retry. It leaves upstream `biteTab/Bite/BiteLow` code authoritative.

`media/lua/shared/ZombieActions/ZAShoot.lua`

Marker: `source-clean-gunshot-coordinate-alert-v1`

Reads installed Bandits2 ZAShoot and replaces only the idle-zombie gunshot `spottedNew/addAggro/setTarget(shooter)` bridge with `pathToLocationF(sx, sy, sz)`.

Both transformers must boot with `mode=PATCHED`. On fingerprint/compile drift they deliberately log a bypass and execute upstream unchanged rather than guessing against an unsupported version.

### Validated external hooks copied byte-for-byte from the Dev proof

Client:

- `real-worn-reconnect-v2`
- `character-relation-suppression-v6`
- `fake-hit-relation-cleanup-v3`
- `fake-hit-immediate-cleanup-v1`
- `terminal-die-onground-pump-v1`

Server:

- `server-authoritative-death-worn-v2`
- `server-death-worn-remove-snapshot-v2`

Diagnostics such as pursuit stall trace, PFB late sweep, Bite trace/outcome, attack pre/post trace and corpse correlation are not part of NPCFixes.

## Required RC test topology

Do not copy/install `Bandits-LCC-Dev` for this test.

Use normal original `Bandits2` and the split LCC items. At minimum for the NPC path:

- `LaccckaB4220PatchCore`
- `LaccckaB4220RuntimeFixes`
- `LaccckaB4220NPCFixes`
- original `Bandits2`

For one final evidence run, `LaccckaB4220NPCCombatExperimental` may also be enabled. It now loads after NPCFixes and contains diagnostics/admin tools only, so it should not repair a failed production target relationship.

## Mandatory boot checks

Required:

- `[LCC][NPCFixes][BanditUpdateShim][BOOT] marker=source-clean-coordinate-pursuit-v1 mode=PATCHED`
- `[LCC][NPCFixes][ZAShootShim][BOOT] marker=source-clean-gunshot-coordinate-alert-v1 mode=PATCHED`

Reject the RC if either transformer reports:

- `BYPASS_FINGERPRINT`
- `BYPASS_COMPILE`
- `[FATAL]`

The first test also proves that same-path override resolution loads the LCC transformer instead of loading both upstream and transformer copies of BanditUpdate.

## Acceptance criteria

Combat/network:

- `ClassCastException=0`
- `AttackState.triggerPlayerReaction=0`
- `NetworkZombieMind: goal character is not set=0`
- no zombie -> Bandit target leaks in observe-only diagnostics
- no sustained `pathfind`/`walktoward` freeze
- no player-proximity wake-up requirement
- original custom Bite/BiteLow still causes real Bandit health loss
- ordinary zombie -> player combat remains normal
- no long-lived living `onground` Bandit acting as a horde target magnet after terminal `Die` is assigned

Clothing/corpses:

- client reconnect real WornItems remain present
- server primary/fallback clothing summaries keep `errors=0`
- observed corpses contain at least expected configured wearables without duplicate-loot regression

## After a clean RC

1. mark NPCFixes combat/death architecture stable;
2. remove PoC wording from promoted hook log labels/comments without changing behavior;
3. assign a Workshop ID and final preview;
4. bump from `0.9.0` RC to the first public stable version;
5. update the grouped audit from staging `id=0/private` to the assigned published metadata;
6. keep `NPCCombatExperimental` optional for future diagnostics only.
