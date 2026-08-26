# Lacccka Quest Framework

Build 42.20 multiplayer foundation for server-authoritative NPC interaction, dialogue, quests, per-character knowledge and future faction/world simulation.

## Current version: v0.3.4

v0.3.4 keeps the accepted v0.3.3 objective runtime and adds the first **Character Knowledge + RPG Journal** vertical slice.

Current top-level RPG Hub pages:

- **Known People / Известные люди** — only NPCs discovered by the current saved character;
- **Quests / Задания** — all active quests plus a collapsible completed/archive section.

This revision is a runtime-test candidate. Static audit success does not by itself mean the new knowledge/UI slice is accepted in-game.

## Core invariants

```text
World existence != Character knowledge
Physical Bandits runtime != framework NPC identity
QuestTarget != QuestMarker
New character life != previous character knowledge/history
```

The server owns canonical NPC discovery, quest state and persistence. Clients receive sanitized projections only.

## Durable character identity

Quest Framework does not use username or multiplayer connection identity as RPG identity.

```text
SAVED PZ CHARACTER
player:getModData().lccqCharacterId
        |
        v
WORLD SAVE / GlobalModData
LCCQF_Persistence.characters[characterId]
        |
        +--> quests
        +--> knowledge
```

When a character dies its UUID is retired. A later live character receives a new UUID and starts with fresh personal knowledge and quest history.

## Character Knowledge

v0.3.4 introduces `LCCQFCharacterKnowledge.lua` under the server persistence boundary.

Per-character storage currently contains:

```text
knowledge
├── schemaVersion = 1
├── revision
└── knownNPCs
    └── <npcId>
        ├── discoveredWorldHours
        ├── source
        └── facts
```

### DiscoverNPC

`CharacterKnowledge.DiscoverNPC(player, npcId, source)` is server-authoritative and idempotent.

For normal NPC interaction the transition is deliberately placed **after successful `DialogueSession.Open`**:

```text
client presses E
    -> server validates npcId/runtimeId/range/physical handle
    -> DialogueSession.Open succeeds
    -> DiscoverNPC(npcId, "validated-dialogue")
    -> KnownPersonUpsert sent to that character
```

A fabricated client request therefore cannot reveal an NPC that failed the existing interaction validation chain.

### Knowledge fragments

NPC definitions may declare authored facts:

```lua
knowledgeFacts = {
    {
        id = "met_alexey",
        titleKey = "...",
        textKey = "...",
    },
}
```

The character record stores only unlocked fact IDs. `CharacterKnowledge.UnlockFact()` is the server API for future dialogue/quest rewards that reveal biography, affiliations, events or other information.

The client never receives the complete NPC registry as a hidden encyclopedia.

## Known People

The client keeps a sanitized `KnownPeopleClientState` with the same per-life lifecycle discipline already used by quests:

- full snapshot on game/player creation;
- incremental known-person upserts;
- clear immediately on local death;
- clear before a new character projection is synchronized.

The first dossier contains:

- known name and optional future nickname/alias;
- live 3D portrait when the physical NPC is currently materialized;
- short known summary;
- unlocked history fragments;
- quests given by that person;
- active and completed quest links.

Search and sorting are intentionally deferred until the data model is accepted.

## 3D NPC portrait

The dossier uses the vanilla Build 42 `ISUI3DModel`/`UI3DModel` path used by character creation rather than a static PNG.

Generic UI code calls:

```text
NPCRuntime.ResolveClientEntity(npcId)
```

The current Bandits presentation adapter resolves the already-synchronized active `runtimeId` through `BanditZombie.GetInstanceById(runtimeId)` and supplies that `IsoGameCharacter` to `ISUI3DModel:setCharacter()`.

Important boundary:

- framework anchor bindings remain the only interaction-discovery source;
- the Bandits cache is used only for presentation of an already-known NPC;
- no `getZombieList()` scan is used.

Current v0.3.4 limitation: if the NPC is not materialized on the client, the dossier shows a portrait placeholder. A later visual-profile layer can build a persistent `SurvivorDesc` preview from stored clothing/hair/weapons without requiring the physical NPC to be loaded.

## Quest journal

The new Quests page keeps the existing authoritative `QuestClientState` but changes presentation toward a Skyrim-style journal.

Left side:

```text
ACTIVE QUESTS
- Old Checkpoint
- Roadside Supplies
...

+ COMPLETED QUESTS (N)
```

Selecting a quest shows:

- title;
- description;
- state;
- all objectives and numeric progress;
- world-map marker notice when applicable;
- known quest giver with a link back to their dossier.

Known-person dossiers use the same quest instances and link into this journal rather than duplicating quest state.

## NPC runtime boundary

Framework logical identity remains independent from Bandits2 physical identity.

```text
framework npcId
    |
    v
NPCRuntime adapter boundary
    |
    v
Bandits2 physical runtime / IsoZombie
```

Bandits2 is a provider, not the owner of NPC identity.

`NPCRuntime.IsFrameworkEntity(entity)` provides provider-neutral classification for objectives such as `Kill` and `ClearArea`.

The current Bandits2 adapter requires `Lacccka B42 NPC Fixes`. NPCFixes 1.0.5 supplies the loadstring-free scheduling/predicate compatibility bridge used by the non-combat quest-giver role.

## Objective runtime

Current handlers:

- `ReachArea` — server position/radius validation;
- `TalkToNPC` — completion through validated framework NPC interaction;
- `Kill` — server `OnZombieDead` credit;
- `Fetch` — server inventory count validation;
- `Deliver` — validated NPC interaction plus server-side inventory removal;
- `ClearArea` — bounded server world-square scan around canonical quest geometry.

Quest logic remains generic through handler capabilities such as `EvaluateTick`, `EvaluateTalk`, `EvaluateZombieDeath`, `MakeProgressView` and `MakeMarkerView`.

## Test quests

### Old Checkpoint / Старый блокпост

```text
ReachArea
    -> TalkToNPC Alexey
    -> completed
```

### Roadside Supplies / Припасы у дороги

```text
Kill 3 infected
    -> Fetch 2 x Base.Sheet
    -> ClearArea
    -> Deliver 2 x Base.Sheet to Alexey
    -> completed
```

The objective runtime was accepted on dedicated multiplayer before v0.3.4. This milestone should not require replaying the complete chain unless a regression appears.

Acceptance report:

`docs/final-reports/quest-framework-objective-runtime-acceptance-2026-08-26.md`

## Persistence shape

```text
LCCQF_Persistence
├── schemaVersion = 1
├── characters
│   └── <characterId>
│       ├── status
│       ├── quests
│       │   ├── schemaVersion = 1
│       │   ├── byInstanceId
│       │   └── byQuestId
│       └── knowledge
│           ├── schemaVersion = 1
│           ├── revision
│           └── knownNPCs
└── retiredCharacterIds
```

UI widgets, physical Bandits instances and world-map symbols are not canonical persisted RPG state.

## v0.3.4 focused runtime test

The first game test should stay narrow:

1. Start server/client and confirm `loaded version=0.3.4`.
2. Confirm server logs `validated-dialogue discovery installed` and knowledge persistence `ready`.
3. Before talking to Alexey, open RPG Hub: **Known People must be empty** for a fresh character.
4. Perform one validated `E` interaction with Alexey.
5. Confirm `npc discovered ... npcId=lccq_test_npc_01 source=validated-dialogue` on server and a known-person upsert on client.
6. Open RPG Hub -> Known People. Alexey must now appear.
7. Open his dossier. If his physical runtime is loaded, the vanilla `ISUI3DModel` portrait should render his live model; otherwise the placeholder must be shown without an error.
8. Verify the dossier lists any existing quests given by Alexey and that clicking one opens the Quests page on that exact quest.
9. From a quest, use the quest-giver link to return to Alexey's dossier.
10. Verify active quests are listed above the completed/archive toggle and completed quests can be expanded.
11. Restart/reconnect the same saved character: Alexey knowledge must persist.
12. Kill that character and create a new one: Known People must clear and Alexey must remain unknown until that new character interacts with him.

The full quest objective chain does not need to be replayed for this smoke test unless the new journal reveals a regression.

## Deferred work

After v0.3.4 acceptance:

```text
persistent offline NPC visual profiles / SurvivorDesc portrait fallback
    -> more authored knowledge fragments from quests/dialogue
    -> Known People search/sort
    -> NPC relationship state
    -> faction discovery
    -> faction relations/membership/rank
    -> larger persistent faction/world simulation
```

The separate Bandits2 wanderer-devirtualization population purge remains deferred as repository issue #3.

## Regression constraints

Do not reintroduce:

- Bandits physical lookup into generic interaction discovery;
- client/global `getZombieList()` scans;
- client-authoritative dialogue, discovery or quest transitions;
- full NPC registry disclosure to a fresh character;
- map markers as objective authority;
- framework `npcId` stored as Bandits physical identity;
- username/onlineID as durable RPG identity;
- inheritance of a retired character's quests or personal knowledge;
- late removal of quest NPCs from Bandits activity caches as a combat policy.

Detailed design contracts:

- `docs/design/quest-framework-target-vision.md`
- `docs/design/character-knowledge-discovery-and-life-cycle.md`
- `docs/design/bandits-non-combat-npc-scheduling-policy.md`
