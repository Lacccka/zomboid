# Lacccka Quest Framework

Build 42.20 multiplayer foundation for server-authoritative NPC interaction, dialogue, quests and future faction/world simulation.

## Current version: v0.3.2

v0.3.2 keeps the accepted v0.3.1 quest/RPG Hub/map-marker slice and adds the first durable persistence boundary:

- framework-owned `characterId` for one player-character life;
- the character id is stored in the saved Project Zomboid player `modData`;
- world-owned Quest Framework state is stored through B42 `ModData` / `GlobalModData`;
- quest instances are keyed by `characterId`, not `onlineID` or username;
- persisted quest instances are normalized and restored against current quest definitions;
- canonical `ReachArea` geometry survives restart instead of being recomputed from the current NPC position;
- dead character ids are retired so a new life cannot silently inherit the old RPG identity;
- persistence tables are schema-versioned from the first durable format.

The complete discovery / `Known People` / faction-relations layer is intentionally not implemented yet. Its architectural contract is documented in:

`docs/design/character-knowledge-discovery-and-life-cycle.md`

## Architectural boundary

```text
WORLD SAVE
GlobalModData / LCCQF_Persistence
    |
    +--> characterId -> character RPG record
                         |
                         +--> persisted QuestInstances

SAVED PLAYER CHARACTER
player:getModData()
    |
    +--> lccqCharacterId

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

`QuestTarget != QuestMarker` remains a hard rule.

The server decides what completes an objective. The client marker only visualizes information the server explicitly permits the character to know. Removing, moving or forging a local map symbol cannot complete a quest.

## Durable character identity

Quest Framework does not treat multiplayer connection identity as RPG character identity.

`onlineID` remains valid for transient connection concerns such as request throttling, but it must not own quests, reputation, discovery or future faction relations.

v0.3.2 assigns a UUID to one Project Zomboid character life:

```text
player:getModData().lccqCharacterId
```

Project Zomboid serializes object/player modData with the character save. The Quest Framework world persistence then stores RPG state under that UUID.

When the character dies the UUID is marked `retired` in the world store. If a later live player object presents a retired id because of engine/save-copy behavior, the framework issues a new UUID rather than reusing the dead character's identity.

This is the foundation for the long-term rule:

> The world survives character death; the dead character's personal knowledge, relations and quest history do not transfer to the new character.

## World existence vs character knowledge

An NPC or faction may exist in the persistent world while a character knows nothing about it.

A fresh character must not automatically receive a global list of NPCs, factions or locations. Future `Known People` and `Factions` pages will be projections of explicit character discovery state.

Example future flow:

```text
Alexey exists in world
    |
    |  fresh character has no knowledge entry
    v
validated interaction with Alexey
    |
    v
DiscoverNPC(alexey)
    |
    v
Alexey becomes visible in Known People
```

Discovery and relations are separate. Knowing that Alexey exists must not automatically grant trust, reputation or friendship.

## Quest persistence

The B42.20.3 engine exposes `ModData.getOrCreate(tag)` over `GlobalModData`. That data is saved in the world's `global_mod_data.bin`.

Quest Framework uses a dedicated tagged root with versioned records:

```text
LCCQF_Persistence
├── schemaVersion = 1
├── characters
│   └── <characterId>
│       ├── status
│       ├── created / updated world time
│       └── quests
│           ├── schemaVersion = 1
│           ├── byInstanceId
│           └── byQuestId
└── retiredCharacterIds
```

The marker UI object itself is never persisted. After reconnect/restart the server restores canonical quest state, sends a sanitized quest view, and the client rebuilds the marker from that view.

Persisted `ReachArea` objectives keep their already-authored target coordinates and radius. They are not regenerated from the giver NPC after a restart.

## Quest kernel

The server-owned quest layer is split into:

- `Quest/LCCQFQuestRegistry.lua` — validates quest definitions;
- `Quest/LCCQFQuestInstance.lua` — creates, restores and progresses sequential instances;
- `Quest/LCCQFQuestService.lua` — conditions, actions, objective evaluation and network views;
- `Persistence/LCCQFCharacterIdentity.lua` — one-life durable character identity and retirement;
- `Persistence/LCCQFQuestPersistence.lua` — world-backed character quest stores and restore normalization;
- `Quest/Objectives/LCCQFObjectiveReachArea.lua` — authoritative server position check and marker projection;
- `Quest/Objectives/LCCQFObjectiveTalkToNPC.lua` — validated NPC interaction completion;
- `Content/LCCQFQuestDefinitions.lua` — authored quest and presentation policy.

The client has no quest-completion or persistence authority.

## First quest: Old Checkpoint / Старый блокпост

```text
Talk to Alexey
    |
    v
Accept Old Checkpoint
    |
    v
SERVER creates and persists ReachArea target
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

For the current test quest the initial target is created from Alexey's validated framework handle with `dx=12, dy=0`, same Z and a server radius of 2.25 tiles. Once the instance exists, those canonical coordinates belong to that quest instance and are persisted.

## Quest markers

The first marker mode is `EXACT`.

The client `LCCQFQuestMarkerService.lua`:

- observes `QuestClientState` changes;
- uses a hidden `ISWorldMap` to reach `getSymbolsAPIv2()`;
- creates a service-owned `!` text symbol;
- uses the B42 `userDefined=true` rendering flag because non-user-defined text symbols are hidden when the renderer's `PlaceNames` layer is disabled;
- identifies and cleans only its own symbols;
- checks marker integrity and can rebuild presentation from authoritative quest state;
- removes the marker when the active objective no longer exposes it.

`userDefined=true` is only an engine rendering flag here. The user does not own quest state and deleting a local symbol cannot alter objective progress.

Future presentation modes remain:

- `EXACT`;
- `APPROXIMATE`;
- `AREA`;
- `HIDDEN`.

## RPG Hub

The floating `QF` button opens a reusable page host. v0.3.2 still registers only the `Quests / Квесты` page.

Future pages should use the same host, including:

```text
RPG Hub
├── Quests
├── Factions
├── Known People
├── Reputation / Relations
└── World information
```

Those future pages must obey the character-knowledge contract: they show only entities discovered by the current character, never every entity present in the world simulation.

## Accepted runtime history

### v0.2.8

Accepted interaction/dialogue vertical slice:

`NPC -> proximity -> E -> server validation -> DialogueSession -> dialogue UI`.

### v0.2.9

Available single-client lifecycle acceptance passed:

- exact runtime interaction and dialogue;
- range enforcement;
- death invalidation;
- replacement physical runtime gets a new runtime id;
- unload/rematerialization/reconnect binding behavior.

Two-client concurrency remains pending.

### v0.3.0

Quest accept/runtime synchronization worked. The first ReachArea test exposed ambiguous human navigation wording rather than an objective-authority failure.

### v0.3.1

**Single-client dedicated multiplayer acceptance passed on 2026-08-26.**

The complete first quest was finished end to end:

- dialogue and quest acceptance;
- RPG Hub state;
- visible world-map marker;
- server ReachArea completion;
- marker removal;
- return-to-Alexey objective;
- completed quest state.

During that test the first physical Alexey died and a later Bandits runtime representing the same framework `npcId` successfully accepted the quest turn-in. That confirmed logical NPC identity is independent of one Bandits runtime object.

See:

`docs/final-reports/quest-framework-quest-hub-marker-acceptance-2026-08-26.md`

## v0.3.2 test plan

v0.3.2 is implemented and must receive runtime persistence acceptance.

The next game run should combine persistence testing with the pending Alexey quest-giver-role smoke test.

Recommended sequence:

1. Deploy v0.3.2 to both server and client.
2. Confirm server log reports `loaded version=0.3.2` and `persistence=ready`.
3. Confirm a `characterId` is assigned for the current character.
4. Spawn a new Alexey and verify he remains stationary and cannot be killed by zombies during the test window.
5. Accept `Old Checkpoint` and confirm the active marker.
6. Disconnect/reconnect without dying. The same quest and marker must return.
7. Restart the dedicated server. The same active quest/objective/marker must return.
8. Complete `reach_checkpoint`; restart or reconnect again and verify `return_to_alexey` remains current.
9. Finish the quest; restart again and verify the quest remains completed and cannot be offered again to the same character.
10. Die and create a new character. The old character id must be retired and the new character must receive another id.
11. The new character must not inherit the old personal quest history.
12. Full `Known People` / faction-discovery reset is not yet an acceptance requirement because those systems are not implemented in v0.3.2.

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
- `onlineID` or username as durable RPG character identity;
- client access to server persistence internals;
- recomputing persisted canonical quest targets from a moved/rematerialized NPC;
- inheriting a retired character's RPG identity into a new life;
- exposing all world NPCs/factions in future RPG UI without character discovery;
- unguarded server event hooks that rely only on `/server/` placement.

## Next after v0.3.2 acceptance

Do not immediately build the full faction simulation.

After persistence/character-lifecycle acceptance, the next useful expansion is the reusable character knowledge boundary plus additional quest objectives such as `Fetch`, `Deliver`, `Kill` and `ClearArea`. `Known People`, faction discovery and relationship projections should then be built on the durable `characterId` rather than on account or connection identity.
