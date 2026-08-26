# Quest Framework — character knowledge, discovery and life cycle

Status: **accepted architectural constraint**

Date: 2026-08-26

## Core invariant

**World existence is not the same thing as character knowledge.**

An NPC, faction, settlement, territory or world event may exist persistently in the world while a particular player character knows nothing about it.

The RPG UI must never expose world entities merely because the server knows that they exist.

```text
WORLD STATE
├── NPCs exist
├── factions exist
├── bases / territories exist
├── resources / diplomacy / history exist
└── world consequences persist

CHARACTER STATE
├── identity of this life
├── discovered / known entities
├── personal relations
└── personal quest history
```

## Initial knowledge

A newly created character starts with no Quest Framework world knowledge unless a future authored scenario explicitly grants some.

Conceptually:

```text
knownNPCs = {}
knownFactions = {}
knownLocations = {}
quests = {}
```

Therefore an existing NPC such as Alexey must not automatically appear under `Known People`, and an existing faction must not automatically appear under `Factions`.

The player discovers the world by travelling, investigating and interacting with it.

## Discovery is explicit server state

Discovery must be an explicit, server-authoritative transition such as:

```text
validated interaction with Alexey
    -> DiscoverNPC(alexey)
    -> Alexey becomes eligible for Known People UI
```

Future discovery sources may include validated NPC dialogue, entering an authored location, reading a document, receiving information from another NPC or an explicit quest/world event.

Physical existence, client proximity, runtime synchronization or a Bandits2 runtime id are not sufficient evidence of discovery.

`known = true` also must not imply positive relations. Discovery and relationship state are separate concerns.

## Intended character layers

The long-term model should keep these concerns separate:

```text
CharacterIdentity
    └── durable id for one player-character life

CharacterKnowledge
    ├── known NPCs
    ├── known factions
    ├── known locations
    └── discovered information

CharacterRelations
    ├── NPC relations
    └── faction reputation / trust / hostility / membership / rank

CharacterQuestStore
    ├── accepted quests
    ├── objective progress
    └── completed personal quest history
```

This separation allows a character to know that an NPC or faction exists without automatically trusting it, belonging to it or knowing all information about it.

## Death semantics

Project Zomboid world continuity and personal RPG continuity are deliberately different.

When Character A dies:

- persistent NPCs remain in the world;
- factions, bases, territories, resources, diplomacy and world history remain;
- Character A's personal identity is retired;
- Character A's known NPC/faction set is not inherited;
- Character A's personal relations are not inherited;
- Character A's personal quest state/history is not inherited by the new character.

Character B receives a new Quest Framework `characterId` and therefore starts with a new personal state.

Example:

```text
Character A
    -> discovers Alexey
    -> accepts Old Checkpoint
    -> dies

World
    -> Alexey still exists
    -> world state still exists

Character B
    -> Alexey absent from Known People
    -> Old Checkpoint absent from personal quest history
    -> travels to Alexey
    -> validated interaction discovers Alexey again
```

The same rule applies to factions: a new character does not inherit the previous character's knowledge, reputation, trust, membership or rank.

## Persistence ownership

World-owned state and character-owned state must not be keyed by transient multiplayer identifiers such as `onlineID`.

The current v0.3.2 foundation uses a framework-owned `characterId` for one life. That id is stored with the saved Project Zomboid player character, while character RPG data is stored in world-owned Quest Framework persistence keyed by that id.

A dead character id is retired. If a later live character presents a retired id because of engine/save-copy behavior, the framework must issue a new id rather than reuse the previous life.

## UI contract

Future RPG Hub pages must be projections of character knowledge, not global registries.

Expected empty state for a fresh character:

```text
Quests
    No known quests

Known People
    No known people

Factions
    No known factions
```

A server entity becomes visible in these pages only after the relevant character-owned discovery/quest transition occurs.

## v0.3.2 scope boundary

v0.3.2 implements the durable character identity and quest-persistence foundation required by this model.

It does **not** yet implement the complete `CharacterKnowledge`, `Known People`, faction discovery or relationship UI layers. Those systems must be built on top of this identity boundary rather than retrofitted onto usernames, online ids or Bandits2 runtime ids.
