# Lacccka Quest Framework

Build 42.20 multiplayer foundation for server-authoritative NPC interaction, dialogue, quests and future faction/world simulation.

## Current version: v0.3.1

v0.3.1 keeps the v0.3.0 server-owned quest kernel and adds the first player-facing RPG shell:

- a compact floating `QF` button;
- one reusable tabbed RPG Hub window;
- the first `Quests` page;
- event-driven client refresh from sanitized quest views;
- a client `QuestMarkerService` backed by the native B42 world-map symbols API;
- an authored `QuestMarker` presentation for the active `ReachArea` objective.

The hub is deliberately broader than a one-off quest tracker. Future `Factions`, `Known People`, reputation/world-state and other pages should register into the same page host instead of creating unrelated floating panels.

## Architectural boundary

```text
SERVER
QuestDefinition
    |
    v
QuestInstance
    |
    +--> canonical objective state
    +--> canonical QuestTarget geometry
    +--> marker presentation policy
                 |
                 v
          sanitized quest view
                 |
                 v
CLIENT
QuestClientState
    |
    +--> RPG Hub / Quests page
    |
    +--> QuestMarkerService
             |
             v
       WorldMapSymbols API
```

`QuestTarget != QuestMarker` is now a hard rule.

The server decides what completes an objective. The client marker only visualizes information the server explicitly permits the player to know. Removing, moving or forging a local map symbol cannot complete a quest.

This follows the B42.20.3 spatial research in:

`docs/design/world-spatial-model-quest-targets-and-map-markers.md`

## v0.2.9 stabilization status

The supplied Build 42.20.3 dedicated-server/client logs passed the available single-client lifecycle acceptance:

- exact runtime interaction and dialogue work;
- dialogue distance enforcement works;
- death removes the runtime binding and invalidates dialogue;
- replacement NPC receives a new runtime id;
- unload removes the binding;
- rematerialization rebinds the current-process runtime;
- reconnect/full binding sync works;
- no Quest Framework exception was observed.

The dedicated two-client acceptance remains pending because a second player is not currently available.

## v0.3.0 runtime result

The 2026-08-26 test confirmed:

- v0.3.0 client/server startup;
- full empty quest sync on join;
- Alexey interaction/dialogue;
- server-authoritative acceptance of `lccq_test_checkpoint`;
- client `QuestUpsert` for objective `reach_checkpoint`;
- quest state remained active while the player moved around;
- NPC unload/rematerialization still worked without losing the active in-memory quest.

The first `ReachArea` was not completed in that run because the test instruction said "east", while the authored target was actually a world-coordinate `dx=+12, dy=0` offset. In Project Zomboid's isometric presentation that is not a reliable human-facing screen direction. The player passed well away from the target radius.

That test exposed a UI/navigation problem, not evidence of a server objective failure. v0.3.1 removes the misleading direction wording and provides an explicit map marker.

## Quest kernel

The quest layer remains split into server-owned components:

- `Quest/LCCQFQuestRegistry.lua` validates quest definitions;
- `Quest/LCCQFQuestInstance.lua` owns runtime state and sequential objective progression;
- `Quest/LCCQFQuestService.lua` owns per-player stores, acceptance, conditions, actions, evaluation and network views;
- `Quest/Objectives/LCCQFObjectiveReachArea.lua` validates server player position and now creates a separate marker projection;
- `Quest/Objectives/LCCQFObjectiveTalkToNPC.lua` completes only after a validated NPC interaction;
- `Content/LCCQFQuestDefinitions.lua` contains authored quest and marker policy data.

The client has no quest-completion authority.

## First quest: Old Checkpoint / Старый блокпост

```text
Talk to Alexey
    |
    v
Accept Old Checkpoint
    |
    v
SERVER creates ReachArea target
    |
    +--> SERVER keeps exact radius validation
    |
    +--> CLIENT receives EXACT QuestMarker projection
                     |
                     v
               world-map ! marker
                     |
                     v
SERVER detects player inside ReachArea
    |
    v
marker disappears from new quest view
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

For the current test quest the target is still created from Alexey's validated framework handle with `dx=12, dy=0`, same Z and a server radius of 2.25 tiles. Those target details are implementation/debug data. The human-facing contract is the marker and localized objective description.

## Quest markers

The first marker mode is `EXACT`.

The server's `ReachArea` handler builds a marker projection containing only presentation fields such as:

- stable per-instance `markerId`;
- mode;
- marker world `x/y/z`;
- localization key;
- world-map visibility policy.

The client `LCCQFQuestMarkerService.lua`:

- observes `QuestClientState` changes;
- rebuilds only when quest state changes or initial map initialization needs retry;
- uses a hidden `ISWorldMap` to reach `getSymbolsAPIv2()`;
- creates a non-user-defined system-like `!` annotation;
- removes the marker automatically when the active objective no longer exposes it.

The marker object itself is never quest persistence state.

Future presentation modes remain compatible with the spatial design document:

- `EXACT`;
- `APPROXIMATE`;
- `AREA`;
- `HIDDEN`.

## RPG Hub

The floating `QF` button opens a single reusable window.

The window has a page registry rather than hard-coded quest-only architecture. v0.3.1 registers only:

- `Quests` / `Квесты`.

The quest page shows:

- synchronized quest title;
- description;
- quest state;
- all known objective states;
- the active objective;
- whether the current objective has a world-map marker.

The hub is presentation only. It reads `LCCQFQuestClientState` and does not execute quest transitions.

Intended later pages include, without committing to final naming yet:

```text
RPG Hub
├── Quests
├── Factions
├── Known People
├── Reputation / Relations
└── World information
```

## Dialogue integration

Dialogue choices remain server-filtered and server-revalidated. Alexey now describes the checkpoint as a place marked on the player's map instead of using the ambiguous word "east".

Quest acceptance still happens only through the validated dialogue action path.

## Objective authority

### ReachArea

`ReachArea` is evaluated on the dedicated server every 250 ms from the server-side player position.

The client does not submit "I reached the destination". A visible marker is not completion evidence.

### TalkToNPC

`TalkToNPC` advances only after:

1. logical `npcId` lookup;
2. active `runtimeId` equality;
3. physical provider resolve;
4. same-Z check;
5. server interaction-distance check.

## Quest synchronization

The protocol continues to use:

- `RequestQuests` -> full quest view sync;
- `Quests` -> sanitized full snapshot;
- `QuestUpsert` -> changed quest view;
- `QuestEvent` -> localized presentation event.

A quest view can now include a `marker` presentation object for the current objective. This does not expose or transfer objective mutation authority.

## Build 42 client-init hardening

Build 42.20 may execute modules stored under server paths during client initialization/checksum work. Server-directory placement is not treated as a process boundary by itself.

Server event registration remains guarded with `isServer()` in the interaction/dialogue/Bandits server paths.

## Current persistence limitation

v0.3.1 quest instances are still intentionally in-memory only.

The temporary store is not the final durable character identity model. Therefore:

- server restart loses quest state;
- reconnect does not yet promise quest restoration;
- character-death/reset semantics are not implemented;
- marker UI objects are rebuilt from quest views and are not persisted independently.

## v0.3.1 single-client test

1. Deploy `LaccckaQuestFramework` v0.3.1 to server and client.
2. Confirm server startup reports `loaded version=0.3.1`.
3. Confirm client logs contain `RPG hub bootstrap loaded`.
4. A small `QF` button should be visible near the upper-right side of the screen.
5. Click it. The RPG Hub should open with the `Квесты / Quests` tab.
6. Before accepting anything, the page should show that there are no known quests.
7. Spawn/approach Alexey, press `E`, ask for work and accept `Старый блокпост / Old Checkpoint`.
8. The quest should immediately appear in the Hub as active with `Добраться до старого блокпоста / Reach the old checkpoint area` active.
9. Open the normal world map. A system-like `!` marker should be visible at the checkpoint target.
10. Walk onto that marker. The server should log objective completion for `reach_checkpoint`.
11. The quest view should switch to `return_to_alexey`; the checkpoint map marker should disappear.
12. Return to Alexey and press `E`. The server should complete `return_to_alexey` and the quest.
13. The Hub should retain the quest and show it as completed with both objectives completed.
14. Reopen the dialogue: the original work offer must not become available again.

If the marker is missing but the quest is active, collect both client and server logs. Marker problems are presentation problems and must not be "fixed" by weakening the server `ReachArea` check.

## Acceptance status

- **v0.2.8:** accepted interaction/dialogue vertical slice.
- **v0.2.9:** available single-client lifecycle acceptance passed; two-client concurrency pending.
- **v0.3.0:** quest acceptance/runtime sync confirmed; ReachArea/turn-in end-to-end test was blocked by ambiguous navigation UX.
- **v0.3.1:** implemented and statically audited; RPG Hub + map-marker + full quest completion runtime acceptance pending.

## Regression constraints

Do not reintroduce:

- provider-specific physical lookup into generic client interaction discovery;
- client `getZombieList()` scans;
- multiple active runtime ids for one logical NPC;
- client-authoritative dialogue transitions;
- client-authoritative quest acceptance/objective completion/rewards;
- using a map marker as objective authority;
- equating `QuestTarget` with `QuestMarker`;
- framework NPC/quest identity stored in Bandits runtime identity;
- unguarded server event hooks that rely only on `/server/` placement.

## Next after v0.3.1 acceptance

Do not immediately add many quest types.

First decide whether the hub/marker UX is acceptable, then proceed with framework-owned persistence and durable character identity. After those foundations are safe, expand objectives (`Fetch`, `Deliver`, `Kill`, `ClearArea`) and later add the next RPG Hub domains such as factions and known people.
