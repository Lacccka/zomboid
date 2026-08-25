# Lacccka B42 NPC Fixes

Stable, source-clean Build 42.20 compatibility fixes for NPC combat and the NPC death/corpse lifecycle. The current validated integration target is `Bandits2` on Build 42.20.3.

This Workshop item contains LCC-authored shims, guards and lifecycle repairs only. It does **not** redistribute complete third-party Lua files or assets. The original mod must be installed separately.

Current stable code version: `1.0.2`.

The source-clean architecture was accepted on 2026-08-21 against the normal Workshop `Bandits2` installation (Workshop ID `3268487204`), without `Bandits-LCC-Dev`. Both runtime transformers loaded in `mode=PATCHED`; `ClassCastException`, `AttackState.triggerPlayerReaction` and `NetworkZombieMind: goal character is not set` remained zero. See `docs/final-reports/npcfixes-source-clean-bandits-acceptance-2026-08-21.md`.

## Scope

### Zombie -> NPC combat

B42.20.3 vanilla `AttackState` is unsafe when an ordinary `IsoZombie` receives a Bandit NPC (also represented as `IsoZombie`) through vanilla character-target APIs. The validated architecture therefore keeps ordinary-zombie -> Bandit pursuit coordinate-only:

- no `pathToCharacter(Bandit)`;
- no `spotted/addAggro/setTarget/setAttackedBy(Bandit)` bridge for the Bandits custom bite path;
- `PathFindBehavior2 Goal.Location` is refreshed only when the Bandit moved far enough or a bounded idle retry is needed;
- the original Bandits `biteTab` / `Bite` / `BiteLow` implementation remains authoritative and is not copied into this patch.

`client/BanditUpdate.lua` is a source-clean runtime transformer. It reads the installed `Bandits2` file with `getModFileReader()`, validates four exact B42.20 fingerprints, applies the coordinate-pursuit and typed death-key guards in memory, and executes the transformed source with `loadstring()`. If a future Bandits version no longer matches the fingerprints, the transformer logs a warning and runs that upstream file unchanged rather than guessing against new source.

`shared/ZombieActions/ZAShoot.lua` uses the same mechanism for the one gunshot-alert block that previously created a vanilla zombie -> Bandit target. Idle zombies are sent to the shot coordinates instead.

### Relationship and fake-hit sanitation

The remaining melee/gun retaliation seams are sanitized after real Bandit damage. Only unsafe Bandit character relationships are removed; ordinary zombie -> player combat is left untouched.

Bandits gun damage also uses `getCell():getFakeZombieForHit()`. B42.20 can temporarily promote that helper into `IsoZombie.target` or `PathFindBehavior2 Goal.Character`, which `NetworkZombieMind` cannot serialize as a player target. The stable fix uses two exact layers:

- immediate cleanup after the Bandit gun-hit chain;
- a late `OnZombieUpdate` cleanup for any relation that survives engine hit-reaction processing.

Both layers recognize only the exact cell fake-hit zombie and do not cancel arbitrary NPC targets from other mods.

### Terminal drag-down death

Bandits can enqueue a locked `Die` task while the NPC is already `onground`. Upstream `ManageActionState()` returns before its local `ProcessTask()`, starving that retained terminal task and allowing a living downed NPC to become a long-lived target magnet for an entire horde.

The terminal-Die guard only progresses an **already existing** `Die` task while a live Bandit is in the exact `onground` state. It never invents a death task or changes ordinary-zombie targeting.

### Clothing and corpse lifecycle

`Bandit.ApplyVisuals()` can leave configured clothing as ItemVisual-only state. The client repair recreates typed real WornItems for live/reconnected Bandits.

On dedicated server, corpse durability requires the same `InventoryItem` object to exist in both the dying zombie inventory and WornItems before `IsoDeadBody` is constructed. The server death-boundary repair enforces that invariant. A separate `BanditRemove` snapshot fallback covers the race where cluster brain data is deleted before server `OnZombieDead` can resolve `brain.clothing`.

## Source-clean contract

The stable item must not contain complete copies of Bandits `BanditUpdate.lua`, `ZAShoot.lua`, `BanditZombie.lua`, server schedulers, assets, or other upstream implementation files. The two same-path files in this package are small runtime transformers; they read the user's installed `Bandits2` source at runtime.

The transformers are intentionally version-pinned by exact fingerprints. A fingerprint bypass is a compatibility warning and must be investigated before claiming support for a newer Bandits release.

## Relationship to the other LCC patches

- `PatchCore` provides the preferred shared guard implementation and is strongly recommended.
- `RuntimeFixes` remains focused on lower-level runtime/dedicated/API compatibility and does not absorb NPC combat/death behavior.
- `NPCCombatExperimental` is diagnostics and admin stress tooling only. It may be enabled for regression testing, but is not required for normal play.
- `Bandits-LCC-Dev` is an internal repository test stand and is not a Workshop package.

## Stable regression contract

Normal regression tests use the original `Bandits2` plus the split patches. Do **not** copy `Bandits-LCC-Dev` into the game.

Required transformer boot markers:

- `[LCC][NPCFixes][BanditUpdateShim][BOOT] ... mode=PATCHED`
- `[LCC][NPCFixes][ZAShootShim][BOOT] ... mode=PATCHED`

Any `BYPASS_FINGERPRINT`, `BYPASS_COMPILE` or `[FATAL]` means the installed upstream version is outside the currently accepted transformer contract and must be investigated before support is claimed.

Stable acceptance criteria remain:

- no `AttackState.triggerPlayerReaction` / `IsoZombie -> IsoPlayer ClassCastException`;
- no `NetworkZombieMind: goal character is not set`;
- no sustained zombie pathfind/walktoward freeze;
- zombies remain active without a nearby player waking them;
- normal zombie -> player attacks remain unchanged;
- no long-lived living `onground` Bandit target magnet after terminal `Die` is assigned;
- client live/reconnect clothing repair remains functional;
- server clothing primary/fallback keep `errors=0`;
- corpses retain the configured wearable set without duplicate-loot regression.

## Publication state

The code is stable `1.0.2`. The repository Workshop project may retain its existing publication metadata until a separate release decision is made. This metadata does not change the stability classification of the code itself.
