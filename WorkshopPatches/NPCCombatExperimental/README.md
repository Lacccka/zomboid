# Lacccka B42 NPC Combat Experimental

Optional diagnostics and admin stress-test tooling for the NPC compatibility stack. Production NPC combat/death/corpse fixes have been promoted to the separate stable `NPCFixes` item.

This package contains LCC-authored diagnostics only and must not contain complete third-party Lua files or production target/pursuit workarounds.

## Workshop publication

- Workshop ID: `3786817782`
- Mod ID: `LaccckaB4220NPCCombatExperimental`
- Public title: `Lacccka B42 NPC Combat Experimental`
- Visibility: `public`
- Diagnostic package version: `0.2.1`

## Contents

### Target / AttackState diagnostics

`zzz_LCC_BanditsTargetDiagnostics.lua` is observe-only. It records target acquisition/switch/loss, vanilla AttackState entry and custom Bite/BiteLow activity so a stable `NPCFixes` regression can prove that unsafe zombie -> NPC vanilla targeting no longer appears.

The former target-disconnect guard has been removed from this package. Keeping a mutating fallback here would mask a broken `NPCFixes` source transformer during acceptance testing.

### Death/corpse diagnostics

`zzz_LCC_BanditsDeathLootDiagnostics.lua` observes the Bandits death manifest, `OnZombieDead`, and resulting corpse content. It remains useful for verifying the stable client/server clothing repairs without mutating the corpse itself.

`zzzz_LCC_BanditsStableDiagnostics.lua` owns the periodic `BanditsServerClothing` and `BanditsServerClothingFallback` `SUMMARY` heartbeat output. The stable `NPCFixes` package still maintains the counters and fallback stale-state pruning, but no longer prints these periodic summaries during normal server runs.

### Admin stress spawner

`zzz_LCC_BanditsAdminSpawnMenu.lua` and the matching server bridge provide the staff-only right-click spawn workflow used for controlled combat/death stress tests. Upstream `BanditServer.Spawner.Clan` remains the spawn authority after access validation.

## Relationship to stable patches

- `RuntimeFixes` provides low-level runtime/dedicated/API compatibility.
- `NPCFixes` provides production NPC combat, terminal-death and corpse/clothing fixes.
- `NPCCombatExperimental` may load after both to observe the final stack and restore periodic stable-fix heartbeat summaries. It is not required for normal play.
- `Bandits-LCC-Dev` is an internal research copy and must not be present during stable-package acceptance.

## Regression checklist

For an `NPCFixes` release-candidate test:

- use original `Bandits2`, `PatchCore`, `RuntimeFixes`, `NPCFixes`, and optionally this diagnostics package;
- verify the NPCFixes `BanditUpdateShim` and `ZAShootShim` boot with `mode=PATCHED`;
- require zero `DANGER_ATTACK_STATE` / zombie -> Bandit target leaks;
- verify custom Bite/BiteLow activity remains present;
- stress multiple Bandit deaths and compare expected configured clothing with corpse worn/container content;
- when periodic clothing counters are needed, enable `NPCCombatExperimental` and inspect the relocated `SUMMARY` lines;
- use the admin spawner only for controlled tests; it is not production gameplay logic.
