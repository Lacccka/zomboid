# Bandits target-magnet / terminal Die checkpoint — 2026-08-21

Branch: `agent/b42-20-compatibility-patch`

Runtime archive: `ZomboidLogs_2026-08-21_17-38-53.zip`

Screenshot observation: a large group of ordinary zombies accumulated around one apparently broken/downed Bandit and did not switch to another Bandit firing nearby.

## Runtime evidence

This is not the old pathfinder cancel/restart defect.

Final pursuit trace (`pursuit-stall-trace-v4`):

- `pathfindStalls=0`
- `walktowardStalls=51`
- `idleStalls=3`
- `pairStalls=54`
- `pfbLocationStalls=54`
- `pfbCharacterStalls=0`
- `staleLocationStalls=0`
- `realControllerFalseStalls=0`

Almost all walktoward stalls selected Bandit id `11338181` (`Elias_Thomas`). For more than 30 seconds the target remained:

- alive;
- `Bandit=true`;
- `actionState=onground`;
- at a valid/current `Goal.Location` destination.

The ordinary zombies were therefore not frozen. They were correctly executing a bad higher-level target situation: the nearest live Bandit remained a permanent downed target magnet.

During the same interval Bandit id `7078188` repeatedly fired and damaged nearby zombies. The current gunshot coordinate alert only redirects zombies that are both idle and not moving, so existing `walktoward` pursuit is not overridden by the shot.

## Terminal Die starvation root cause

`UpdateZombies()` intentionally enqueues a locked terminal task when more than two zombies are already crowding the same Bandit:

`{ action="Die", lock=true, anim="Die", time=300 }`

`Bandit.ClearTasks()` does **not** delete this task because locked tasks are retained.

The actual defect is `ManageActionState()` in `BanditUpdate.lua`:

- `onground` is in `ClearTaskActionStates`;
- `ManageActionState()` calls `Bandit.ClearTasks()` and returns `false`;
- `OnBanditUpdate()` therefore returns before local `ProcessTask()`;
- the retained terminal `Die` task can remain queued but never progresses while the Bandit stays `onground`.

This matches the runtime observation of a living `onground` Bandit remaining the nearest target for tens of seconds.

## Experimental fix

Added external compatibility guard:

`WorkshopPatches/Bandits-LCC-Dev/42.20/media/lua/client/zzzzzzzz_LCC_BanditTerminalDiePump.lua`

Marker:

`terminal-die-onground-pump-v1`

The guard:

- never creates a Die task;
- acts only on a live Bandit whose current first task is already `Die`;
- acts only while `actionState == onground`, where normal BanditUpdate blocks local task processing;
- mirrors the existing ProcessTask lifecycle for that terminal task (`NEW -> WORKING -> COMPLETED`);
- does not modify ordinary-zombie target/PFB/aggro state.

The first validation should determine whether fixing terminal task starvation alone releases the crowd quickly enough. Do not add broad gunshot target-priority semantics unless runtime still shows a problem after this fix.

## NetworkZombieMind regression in this archive

Five client warnings returned:

`NetworkZombieMind: goal character is not set`

They were transient and correlated with Bandit gun-hit activity. The late PFB sweep still saw no non-player character goals, and the existing fake-hit OnZombieUpdate cleanup reported many exact fake target clears. This indicates a timing gap: network serialization can occasionally occur after `BanditUtils.Hit()` but before the later OnZombieUpdate cleanup.

Added exact immediate wrapper:

`WorkshopPatches/Bandits-LCC-Dev/42.20/media/lua/client/zzzzzzzz_LCC_BanditFakeHitImmediateCleanup.lua`

Marker:

`fake-hit-immediate-cleanup-v1`

It runs after the existing current `BanditUtils.Hit` wrapper chain and only clears relations to the exact object returned by `getCell():getFakeZombieForHit()`:

- `victim.target == fakeZombie`;
- `victim.attackedBy == fakeZombie`;
- `PFB Goal.Character -> fakeZombie`.

No arbitrary living NPC/non-player character goals are cancelled.

## Next validation

Reproduce a large zombie-vs-Bandit fight, preferably with one Bandit dragged down while another armed Bandit remains active nearby.

Required checks:

- no long-lived living `onground` Bandit holding a crowd for tens of seconds;
- `BanditsTerminalDie` should show `terminalSeen > 0`, then `starts/completes > 0`, `errors=0`;
- `NetworkZombieMind=0`;
- `ClassCastException=0`;
- `AttackState.triggerPlayerReaction=0`;
- `pfbCharacterStalls=0`;
- no return of pathfind starvation;
- custom Bite remains functional;
- clothing repair remains error-free.
