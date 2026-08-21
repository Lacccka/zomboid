# NPCFixes source-clean Bandits acceptance — 2026-08-21

Branch: `agent/b42-20-compatibility-patch`

Runtime archive: `ZomboidLogs_2026-08-21_19-06-07.zip`

## Test topology

This is the first acceptance run performed against the normal Workshop `Bandits2` installation rather than `Bandits-LCC-Dev`.

Observed Workshop item:

- Bandits Workshop ID: `3268487204`
- client reported `Subscribed|Installed` and loaded `Bandits2`

Observed LCC load order:

1. `Bandits2`
2. `LaccckaB4220PatchCore`
3. `LaccckaB4220RuntimeFixes`
4. other independent stable LCC packages
5. `LaccckaB4220NPCFixes`
6. `LaccckaB4220RussianText`
7. `LaccckaB4220NPCCombatExperimental`

`NPCCombatExperimental` contained diagnostics/admin tooling only; the old mutating AttackState target-disconnect guard was absent.

## Source-clean transformer acceptance

Both production transformers loaded the installed Workshop Bandits source successfully.

Client:

- `[LCC][NPCFixes][BanditUpdateShim][BOOT] marker=source-clean-coordinate-pursuit-v1 mode=PATCHED source=Bandits2 runtimeTransform=true bundledUpstream=false`
- `[LCC][NPCFixes][ZAShootShim][BOOT] marker=source-clean-gunshot-coordinate-alert-v1 mode=PATCHED source=Bandits2 runtimeTransform=true bundledUpstream=false`

Dedicated server also loaded the shared ZAShoot transformer in `mode=PATCHED`.

Across the archive:

- `BYPASS_FINGERPRINT=0`
- `BYPASS_COMPILE=0`
- NPCFixes `[FATAL]=0`
- no NPCFixes WARN/ERROR/FATAL lines were produced

This proves the split production package can patch the installed Workshop source at runtime without copying the full modified Bandits tree into the game.

## Combat/network acceptance

Critical engine failure signatures:

- `ClassCastException=0`
- `AttackState.triggerPlayerReaction=0`
- `NetworkZombieMind: goal character is not set=0`
- `[LCC][BanditsDiag][DANGER_ATTACK_STATE]=0`
- old `BanditsAttackGuard` / target-disconnect fallback was not loaded

Observe-only target diagnostics repeatedly ended with:

- `activeBanditTargets=0`
- `acquired=0`
- `bAttackTrue=0`
- `attackState=0`

The production relationship sanitation remained active during real combat. Final client summary from the combat-heavy session:

- `meleeChecks=2510`
- `meleeDamageEvents=63`
- `gunChecks=346`
- `gunDamageEvents=80`
- `targetClears=0`
- `attackedByClears=143`
- `pfbCharacterGoalChecks=2825`
- `pfbCharacterGoalCancels=0`
- `retaliationPathsSuppressed=143`
- `sanitizeErrors=0`

The zero target/PFB clears are desirable here: the source-clean transformers prevented the unsafe zombie -> Bandit character relationship before it reached those late guards. The remaining sanitation primarily removed transient `attackedBy` retaliation state after actual Bandit damage.

User visual observation for this run: combat behavior appeared normal and active with no obvious zombie freeze/regression.

## Fake-hit network acceptance

The exact engine fake-hit cleanup remained necessary and effective under Workshop Bandits gunfire.

Final immediate-cleanup summary:

- `calls=420`
- `watchedZombieHits=346`
- `changedHits=80`
- `targetClears=80`
- `characterGoalCancels=0`
- `errors=0`

Final late-cleanup summary:

- `updates=373952`
- `fakeRefRefreshes=1`
- `fakeRelations=59`
- `fakeCharacterGoals=0`
- `pfbCancels=0`
- `targetClears=59`
- `errors=0`

Despite substantial fake-hit activity, `NetworkZombieMind: goal character is not set` remained zero.

## Terminal Die

The guard loaded and rebound correctly with `errors=0`, but this particular run did not naturally create an `onground + existing Die` starvation case:

- `terminalSeen=0`

This does not invalidate the guard; the exact case was already reproduced and validated in the preceding Dev regression run. No long-lived downed target-magnet behavior was reported visually in this Workshop run.

## Clothing/corpse acceptance

Client reconnect/live restoration repeatedly reconstructed configured real WornItems from zero worn state. Representative restores reached the expected configured count, e.g. 9/9, 11/11, 13/13.

Dedicated primary death-boundary final summary:

- `banditDeathsMatched=8`
- `deathRepairs=8`
- `expected=80`
- `wearableExpected=80`
- `restored=75`
- `alreadyWorn=4`
- `conflicts=1`
- `errors=0`

Dedicated BanditRemove snapshot fallback final summary:

- `removeCalls=30`
- `removeAfterPrimary=8`
- `snapshotsCaptured=22`
- `snapshotMissesAtRemove=0`
- `fallbackMatches=22`
- `fallbackRepairs=22`
- `expected=196`
- `wearableExpected=196`
- `restored=155`
- `alreadyWorn=40`
- `conflicts=1`
- `errors=0`

The single slot conflict was safe/non-destructive:

- Bandit `12582989`
- brain slot `UnderwearBottom`
- expected `Base.Briefs_White`
- actual existing typed slot `Base.Briefs_SmallTrunks_Blue`
- `intervention=false`

The resulting correlated corpse still had `corpseWorn=13`, matching `expectedClothing=13`, so this is intentional conflict preservation rather than clothing loss.

Experimental corpse diagnostics observed 29 corpse correlations. One apparent deficit (`corpseWorn=8`, `expectedClothing=9` for id `10355094`) was a false positional correlation at distance `0.935`: the corpse contained an unrelated female formal outfit while the actual Charles_Hughes death had already been repaired server-side from 1 to 10 worn items with `errors=0`. This is a diagnostic matcher limitation, not a production corpse failure.

All other correlated corpses had `corpseWorn >= expectedClothing`.

## Acceptance decision

The source-clean `NPCFixes` architecture is accepted for Build 42.20.3 + current Workshop `Bandits2`.

Confirmed production invariants:

- no ordinary zombie -> Bandit vanilla Character pursuit/target bridge;
- coordinate-only zombie -> Bandit pursuit remains active;
- repeated identical path requests are throttled;
- custom upstream Bandits Bite/BiteLow implementation is left untouched;
- real zombie -> player combat is not intercepted by the NPC fix;
- Bandit melee/gun retaliation state is sanitized without global aggro clearing;
- exact fake-hit zombie relations are removed before they can produce NetworkZombieMind warnings;
- terminal Die starvation guard remains isolated to an already-existing `Die` task;
- real live/reconnected clothing is restored;
- dedicated corpse clothing preserves the inventory + same-WornItem-object invariant;
- BanditRemove/OnZombieDead ordering race is covered by the snapshot fallback.

`Bandits-LCC-Dev` is no longer required for normal installation or acceptance testing. It remains an internal research/regression tree only.

## Release state

Promote `LaccckaB4220NPCFixes` from RC `0.9.0` to stable `1.0.0` without changing validated runtime behavior. The Workshop project may remain `id=0` / private until a real Workshop item ID and final preview are assigned.
