# Lacccka Quest Framework

Build 42.20 multiplayer foundation for server-authoritative NPC interaction, dialogue, quests and future faction/world simulation.

## Current version: v0.3.0

v0.3.0 introduces the first real server-owned quest vertical slice on top of the stabilized NPC/dialogue runtime.

The architecture remains deliberately provider-neutral above the physical NPC boundary:

```text
Bandits2 physical NPC
        |
        v
BanditsServerRuntime
        |
        v
framework npcId/runtime binding
        |
        +-------------------------+
        |                         |
        v                         v
DialogueSession              QuestService
server-owned                 server-owned
        |                         |
        |                         +--> QuestRegistry
        |                         +--> QuestInstance
        |                         +--> objective handlers
        |                                  |
        +---------- conditions/actions ----+
                          |
                          v
                    sanitized client views
```

Bandits2 owns only the current physical implementation of an NPC. It does not own Quest Framework quest identity, quest state or logical NPC identity.

## v0.2.9 stabilization status

The supplied Build 42.20.3 dedicated-server/client logs passed the available single-client lifecycle acceptance:

- exact runtime interaction and dialogue still work;
- dialogue distance enforcement works;
- NPC death removes the runtime binding and invalidates an open dialogue session;
- replacement NPC receives a new runtime id;
- unload removes the binding;
- rematerialization rebinds the same current-process runtime;
- reconnect/full runtime-binding sync works;
- no Quest Framework exception was observed in the supplied run.

The dedicated two-client acceptance remains pending because a second player is not currently available. This does not block the single-player-owned quest vertical slice, but concurrent quest/dialogue behavior still needs a later multiplayer run.

## v0.3.0 quest kernel

The new quest layer is split into small server-side components:

- `Quest/LCCQFQuestRegistry.lua` validates and registers quest definitions;
- `Quest/LCCQFQuestInstance.lua` owns one player's runtime quest state and sequential objective progression;
- `Quest/LCCQFQuestService.lua` owns per-player quest stores, acceptance, conditions, actions, objective evaluation and network views;
- `Quest/Objectives/LCCQFObjectiveReachArea.lua` validates a player's physical position on the server;
- `Quest/Objectives/LCCQFObjectiveTalkToNPC.lua` completes only after an already validated NPC interaction;
- `Content/LCCQFQuestDefinitions.lua` contains the first test quest as data;
- client `Quest/LCCQFQuestClientState.lua` stores sanitized server views only and is the future Quest Tracker data source.

The client has no `AcceptQuest`, `CompleteQuest`, reward or objective-transition command.

## First quest: Old Checkpoint / Старый блокпост

The first vertical slice is intentionally simple and tests the complete quest lifecycle without inventory ownership, kill attribution, escort AI or rewards.

```text
Talk to Alexey
    |
    v
"Any work?"
    |
    v
server validates dialogue choice
    |
    v
QuestService.Accept
    |
    v
QuestInstance ACTIVE
    |
    v
ReachArea: about 12 tiles east of Alexey
    |
    v
server detects player inside radius
    |
    v
TalkToNPC: Return to Alexey
    |
    v
exact NPC runtime/range validation
    |
    v
QuestInstance COMPLETED
```

The current target is derived from the giver's validated framework handle when the quest is accepted: approximately 12 tiles east, same Z, radius 2.25 tiles. Target coordinates are never sent as authoritative completion data by the client.

## Dialogue integration

Dialogue content can now declare server-evaluated quest conditions and server-executed actions.

For example, Alexey exposes different work choices depending on whether `lccq_test_checkpoint` is:

- `available`;
- `active`;
- `completed`.

The server filters the choices before creating the dialogue view. When a player submits a `choiceId`, `DialogueSession` checks the condition again before executing an action. A stale or forged hidden choice therefore cannot accept or advance a quest.

Quest acceptance is triggered by a dialogue action only after the same exact NPC/runtime/range validation already used by the interaction system.

## Objective authority

### ReachArea

`ReachArea` is evaluated on the dedicated server every 250 ms from the server-side player position.

The client never reports that it reached the destination.

### TalkToNPC

`TalkToNPC` is advanced only inside the validated dialogue-open path after:

1. logical `npcId` lookup;
2. active `runtimeId` equality;
3. physical provider resolve;
4. same-Z check;
5. server interaction-distance check.

A synchronized client anchor is still presentation/discovery data only.

## Quest synchronization

The protocol now includes:

- `RequestQuests` -> full player quest view sync;
- `Quests` -> sanitized full snapshot;
- `QuestUpsert` -> changed quest view;
- `QuestEvent` -> localized presentation event.

A client quest view contains identifiers, title/description keys, state, current objective and objective progress/state. Physical objective target coordinates and server mutation functions are not exposed through the quest view.

The client currently uses quest events as transient status messages. A persistent HUD is intentionally the next UI milestone.

## Build 42 client-init hardening

Build 42.20 can execute modules stored under server paths during client initialization/checksum work. Server-directory placement is therefore not treated as a process boundary by itself.

Quest Framework now guards server event registration with `isServer()` in:

- `LCCQFInteractionServer.lua`;
- `LCCQFDialogueSession.lua`;
- `LCCQFBanditsServerRuntime.lua` for its death hook.

Provider modules may still be loaded while the client initializes, but they must not register server ticks/death/session timers or perform world mutations there.

## Current persistence limitation

v0.3.0 quest instances are deliberately in-memory only.

The temporary player store is keyed by current multiplayer player identity (`onlineID`, with username fallback). It is sufficient for the first vertical-slice test, but it is **not** the final character identity model and must not be mistaken for durable quest persistence.

Consequences in v0.3.0:

- server restart loses quest state;
- reconnect may produce a new online identity and therefore does not promise quest restoration;
- character death/reset semantics are not implemented yet;
- quest target anchors are not persisted.

Persistence and durable character identity are separate later milestones so they do not become coupled to Bandits runtime ids, Steam/account identity or a temporary online id by accident.

## v0.3.0 single-client test

1. Deploy `LaccckaQuestFramework` v0.3.0 to the dedicated server and client.
2. Confirm startup includes a line similar to:

```text
[LCCQF][SERVER] loaded version=0.3.0 ... lifecycleReconcileMs=250 questUpdateMs=250
```

3. Spawn Alexey and approach him.
4. Press `E` and choose `Есть работа?` / `Any work?`.
5. Accept `Старый блокпост` / `Old Checkpoint`.
6. The client should receive the localized quest-accepted event; the server should log `quest accepted` with a new `instanceId`.
7. Walk roughly 12 tiles east of the position where Alexey was standing when the quest was accepted.
8. Enter the target radius. The server should log completion of objective `reach_checkpoint`; the client should receive a quest upsert whose current objective becomes `return_to_alexey`.
9. Return to Alexey and press `E`.
10. The exact NPC/runtime/range validation must succeed first. The server should then complete `return_to_alexey`, mark the quest `completed`, and send the completion event.
11. Reopen the dialogue. The available-work offer must no longer be present; the completed checkpoint dialogue branch should be shown instead.
12. Kill/unload/respawn Alexey once more to ensure the v0.2.9 runtime lifecycle did not regress.

Do not treat quest loss after a server restart/reconnect as a v0.3.0 failure; persistence is intentionally not part of this milestone.

## Acceptance status

- **v0.2.8:** accepted dedicated-server/client NPC interaction and dialogue vertical slice.
- **v0.2.9:** available single-client lifecycle acceptance passed from the 2026-08-25 logs; two-client concurrency acceptance pending.
- **v0.3.0:** implemented and statically audited; runtime quest acceptance pending.

The original interaction/dialogue acceptance report is stored in:

`docs/final-reports/quest-framework-interaction-dialogue-acceptance-2026-08-25.md`

## Regression constraints

Do not reintroduce:

- provider-specific physical lookup into generic client prompt discovery;
- client `getZombieList()` scans;
- identical relative Lua module names for different client/server runtime implementations;
- multiple active runtime ids for one logical framework `npcId`;
- clearing shared provider cache because one player is merely out of range;
- client-authoritative dialogue transitions;
- client-authoritative quest acceptance/objective completion/rewards;
- dialogue or TalkToNPC completion from a synchronized anchor without exact server physical validation;
- framework NPC or quest identity stored in Bandits `brain.id`/`brain.key`;
- unguarded server event hooks that rely only on a `/server/` directory for process isolation.

## Next milestone after v0.3.0 acceptance

The next visible feature is the lightweight floating Quest Tracker inspired by the Aegis-style panel.

It should consume `LCCQFQuestClientState` and initially show only synchronized server views:

```text
Old Checkpoint
Alexey asked you to check the old checkpoint.

Reach the old checkpoint area
Status: In progress
```

or, after the first objective:

```text
Old Checkpoint

✓ Reach the old checkpoint area
● Return to Alexey
```

The tracker is presentation only. It must not evaluate objectives or mutate quest state.

After the tracker is accepted, the next architectural milestone is framework-owned persistence plus a durable character identity model; additional objective types (`Fetch`, `Deliver`, `Kill`, `ClearArea`) follow on top of the same quest kernel.
