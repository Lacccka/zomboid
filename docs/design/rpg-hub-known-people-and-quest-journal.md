# RPG Hub: Known People and Quest Journal

Status: **v0.3.4 implementation candidate**

This note fixes the first user-facing information architecture for the Quest Framework RPG Hub.

## Top-level navigation

The initial Hub deliberately has only two top-level pages:

```text
RPG HUB
├── Known People / Известные люди
└── Quests / Задания
```

Search, sorting, factions, reputation and other future RPG systems must extend these pages without turning the Hub into a collection of unrelated windows.

## Known People

The page is a character-owned knowledge projection, not a browser over the global NPC registry.

```text
server NPC exists
        !=
current character knows NPC
```

An NPC becomes visible after a server-authoritative discovery transition. The current normal path is:

```text
E interaction
 -> server validates logical npcId + current runtimeId + range + physical entity
 -> DialogueSession.Open succeeds
 -> CharacterKnowledge.DiscoverNPC
 -> sanitized KnownPerson view
 -> Known People list
```

### List level

The left side is a scrollable list of all people known by this saved character.

Current display identity supports:

```text
displayNameKey
optional aliasKey
```

Search and sorting are future UI features; they must operate only on the already-authorized Known People projection.

### Person dossier

Selecting a person opens their dossier in the same Hub page.

The dossier is intended to contain:

- known name and nickname/alias;
- 3D portrait;
- short known description;
- history/facts actually discovered by this character;
- active assignments received from this person;
- completed assignments received from this person;
- navigation from those assignments to the canonical quest journal entry.

The dossier must never synthesize unknown biography from the NPC definition.

## Knowledge fragments

Biography is not one always-visible string.

NPC-authored information is split into stable fact IDs:

```text
met_alexey
checkpoint_completed
supply_run_completed
...
```

The server stores only which fact IDs the current durable `characterId` knows.

```text
CharacterKnowledge
└── knownNPCs[npcId]
    └── facts[factId] = true
```

`CharacterKnowledge.UnlockFact()` is the generic server transition for dialogue, quest, faction or world-event content.

### Quest-derived facts

NPC definitions may map authoritative quest states to facts:

```lua
questKnowledgeFacts = {
    [questId] = {
        completed = { "some_fact" },
    },
}
```

When a knowledge snapshot is requested, the server reconciles these mappings against canonical persisted QuestInstances. This serves two purposes:

1. completing a quest can update the person's history without the client inventing the fact;
2. existing pre-v0.3.4 characters can migrate their established NPC/quest history into Character Knowledge.

The client requests a fresh knowledge snapshot after an authoritative completed-quest event.

## 3D portrait

The portrait uses Project Zomboid's vanilla `ISUI3DModel` / `UI3DModel`, the same rendering path used by character creation.

The generic UI never imports Bandits2.

```text
KnownPerson dossier
      |
      v
NPCRuntime.ResolveClientEntity(npcId)
      |
      v
provider-specific presentation resolver
      |
      v
IsoGameCharacter -> ISUI3DModel:setCharacter()
```

For Bandits2, the presentation adapter resolves the already-synchronized active runtime ID through `BanditZombie.GetInstanceById(runtimeId)`.

This is presentation-only. It must not be reused for proximity interaction discovery.

### Current limitation

v0.3.4 can show the live model while the physical NPC is materialized on the client. If the physical runtime is unavailable, the UI shows a portrait placeholder.

The planned persistent visual-profile layer should later provide a `SurvivorDesc`-style preview constructed from framework-owned appearance data:

- body/sex/skin;
- hair/beard;
- clothing;
- equipped weapons;
- other authored presentation state.

That profile can feed both physical-provider materialization and offline Hub portrait rendering.

## Quest journal

The Quests page is a single canonical journal over `QuestClientState`.

It is intentionally Skyrim-like at the information-architecture level:

```text
ACTIVE QUESTS
<scrollable list of every active quest>

+ COMPLETED QUESTS (N)
<expandable scrollable completed list>
```

Selecting a quest shows:

- title;
- description;
- state;
- all objectives;
- numeric progress where relevant;
- map-marker status;
- known quest giver.

A known quest giver is a navigation link back to their person dossier.

## Cross navigation

The Hub exposes stable navigation operations rather than opening duplicate windows:

```text
Hub.OpenPerson(npcId)
Hub.OpenQuest(instanceId)
```

This produces the intended graph:

```text
Known People
    -> Person dossier
        -> related quest
            -> Quest journal
                -> quest giver
                    -> Person dossier
```

## Per-life lifecycle

Known People follows the same hard lifecycle boundary as quest projection.

On local character death or new-character creation:

```text
KnownPeopleClientState.BeginCharacterTransition()
 -> local projection cleared immediately
 -> new character requests authoritative snapshot
```

The dead character's persisted knowledge remains attached to its retired `characterId`; it is not inherited by the new life.

## Security / authority boundary

The client may display only a sanitized KnownPerson projection. It does not decide discovery, unlock biography facts, or derive them from local quest state.

The client is allowed to request a resynchronization when an event may have changed knowledge. The server still decides the resulting projection.

## Deferred extensions

After runtime acceptance of this slice:

```text
persistent visual profiles / offline portrait
 -> Known People search
 -> Known People sorting/filtering
 -> richer authored history fragments
 -> relationship state
 -> faction discovery
 -> faction pages and relationships
```

Search/sort and presentation features must not weaken the underlying `World existence != Character knowledge` invariant.
