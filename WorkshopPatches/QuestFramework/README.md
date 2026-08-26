# Lacccka Quest Framework

Build 42.20 multiplayer foundation for server-authoritative NPC interaction, dialogue, quests and future faction/world simulation.

## Current version: v0.3.3

v0.3.3 keeps the accepted v0.3.1 quest/RPG Hub/map-marker slice and the v0.3.2 durable character/persistence boundary, then adds the first extensible objective runtime.

Current objective handlers:

- `ReachArea` — server position/radius validation;
- `TalkToNPC` — completion through a validated framework NPC interaction;
- `Kill` — server `OnZombieDead` credit using the B42 attacker recorded on the dead zombie;
- `Fetch` — server inventory ownership/count validation;
- `Deliver` — validated NPC interaction plus server-side inventory removal and replication;
- `ClearArea` — bounded server world-square scan around canonical quest geometry.

The quest service dispatches generic handler capabilities (`EvaluateTick`, `EvaluateTalk`, `EvaluateZombieDeath`) instead of growing one branch per objective type.

## Durable identity and persistence

Quest Framework does not use multiplayer connection identity as RPG identity.

```text
SAVED PZ CHARACTER
player:getModData().lccqCharacterId
        |
        v
WORLD SAVE / GlobalModData
LCCQF_Persistence.characters[characterId]
        |
        +--> persisted QuestInstances
```

`onlineID` remains valid only for transient connection concerns such as request throttling. It must not own quests, reputation, discovery or future faction relations.

When a character dies, its framework UUID is marked `retired`. A later live character cannot reuse a retired identity.

This implements the long-term rule:

> The world survives character death; the dead character's personal knowledge, relations and quest history do not transfer to the new character.

## World existence vs character knowledge

World existence and character knowledge are separate states.

An NPC, faction, base or location may persist in the world while a fresh character knows nothing about it. Future `Known People` and `Factions` pages must be projections of explicit per-character discovery state rather than global registries.

```text
Alexey exists in world
        !=
current character knows Alexey
```

A future validated discovery transition will look conceptually like:

```text
validated interaction / information source
        -> DiscoverNPC(alexey)
        -> CharacterKnowledge records discovery
        -> Alexey appears in Known People
```

Discovery does not imply trust, reputation or friendship.

Detailed contract:

`docs/design/character-knowledge-discovery-and-life-cycle.md`

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

Bandits2 is a provider, not the owner of quest/NPC identity.

`NPCRuntime.IsFrameworkEntity(entity)` provides provider-neutral world classification. Individual adapters implement ownership checks, allowing quest systems such as `Kill` and `ClearArea` to exclude physical framework NPCs without importing Bandits APIs.

The current essential test quest-giver role additionally has server-side protection that attempts to keep Alexey stationary, invulnerable and removed from ordinary zombie targeting. This protection still requires runtime acceptance after the issues found in the v0.3.2 test.

## Quest instance model

The server owns canonical instances:

```text
QuestInstance
├── id
├── questId
├── ownerCharacterId
├── state
├── currentObjectiveIndex
├── objectives[]
└── canonical objective geometry/progress
```

The client receives sanitized views only.

`QuestTarget != QuestMarker` remains a hard rule. A marker visualizes authorized information; it never completes an objective.

## Objective runtime

Each objective module may expose only the capabilities it needs:

```text
Create(spec, context)
ValidatePersisted(objective)
EvaluateTick(player, objective)
EvaluateTalk(player, objective, npcId)
EvaluateZombieDeath(player, objective, zombie)
MakeProgressView(objective)
MakeMarkerView(objective)
```

`QuestService` invokes these capabilities generically.

### Kill

`Kill` advances only from server `OnZombieDead`. The B42 dead zombie exposes `getAttackedBy()`, so the current implementation credits the direct `IsoPlayer` attacker recorded by the engine. Framework-owned physical NPC entities are filtered before quest kill dispatch.

### Fetch

`Fetch` recursively counts matching full item types in the character inventory and nested carried containers. The client cannot declare that an item was obtained.

### Deliver

`Deliver` remains active until the player performs a validated interaction with the authored NPC while possessing the required items. The server removes the exact items and uses the B42 inventory-removal replication API.

### ClearArea

`ClearArea` stores canonical `x/y/z/radius` geometry in the QuestInstance and scans only the bounded area when the owning player is sufficiently near it. It does not use `getZombieList()` or another global zombie scan.

Framework-owned NPC entities and B42 `useless` physical zombies are not counted as hostile clearance targets.

## Test quests

### 1. Old Checkpoint / Старый блокпост

Accepted in v0.3.1:

```text
ReachArea
    -> TalkToNPC Alexey
    -> completed
```

Its persisted definition is intentionally unchanged in v0.3.3 so existing v0.3.2 saves are not invalidated by an objective-list migration.

### 2. Roadside Supplies / Припасы у дороги

Available from Alexey after `Old Checkpoint` is completed by the current character:

```text
Kill 3 infected
    -> Fetch 2 x Base.Sheet
    -> ClearArea at an authored roadside point
    -> Deliver 2 x Base.Sheet to Alexey
    -> completed
```

This second quest is developer acceptance content for the v0.3.3 objective runtime. It is not intended as final authored RPG content.

The RPG Hub now projects numeric progress for objectives whose required count is greater than one, for example `1/3` kills or `1/2` sheets.

## Persistence

World persistence uses B42 `ModData.getOrCreate(tag)` / `GlobalModData` and is schema-versioned.

```text
LCCQF_Persistence
├── schemaVersion = 1
├── characters
│   └── <characterId>
│       ├── status
│       └── quests
│           ├── schemaVersion = 1
│           ├── byInstanceId
│           └── byQuestId
└── retiredCharacterIds
```

Canonical objective geometry and progress are persisted. UI objects and map symbols are not.

## Runtime acceptance history

### v0.2.8

Accepted interaction/dialogue slice:

`NPC -> proximity -> E -> server validation -> DialogueSession -> dialogue UI`.

### v0.2.9

Single-client runtime lifecycle acceptance passed for exact runtime interaction, range enforcement, death invalidation, physical runtime replacement and unload/rematerialization behavior. Two-client concurrency remains pending.

### v0.3.1

Single-client dedicated multiplayer quest/RPG Hub/world-map-marker acceptance passed on 2026-08-26.

See:

`docs/final-reports/quest-framework-quest-hub-marker-acceptance-2026-08-26.md`

### v0.3.2

Runtime persistence test exposed two post-implementation defects outside the durable world-store design:

- a new character received a correct new server `characterId`, but the client retained the previous life's stale quest projection until a full restart;
- the Bandits quest-giver role could still move after shove/combat and could still be killed by zombies.

The first issue is now addressed by client per-life projection reset/resync. Quest-giver physical protection is now reinforced on the server and requires the next runtime test.

## Combined v0.3.3 acceptance plan

The next game run should test v0.3.2 fixes and v0.3.3 objective expansion together.

1. Deploy v0.3.3 to server and client and confirm `loaded version=0.3.3` plus `persistence=ready`.
2. Spawn/use a fresh Alexey. Shove him and expose him to zombies; he must not run away under AI control or die.
3. Verify an existing character receives only its own authoritative quest snapshot after reconnect/restart.
4. Die and create a new character. Old quest views must disappear immediately and must not reappear after the new snapshot.
5. Complete `Old Checkpoint` for the test character if required to unlock the second test quest.
6. Accept `Roadside Supplies`.
7. Kill three ordinary infected and verify Hub progress reaches `3/3` from server death events.
8. Obtain two `Base.Sheet` items and verify Fetch reaches `2/2` without a client command completing it.
9. Reach the marked roadside `ClearArea`. If infected are inside the area, clear them; the objective must complete only when the bounded server scan satisfies `maxRemaining=0`.
10. Return to Alexey. The `Deliver` objective must consume exactly two sheets on validated interaction and complete the quest.
11. Reconnect/restart during at least one objective with partial progress and verify the same objective/progress returns.
12. Restart after completion and verify the completed second quest persists for the same character.

Full `Known People` / faction discovery is still not part of this acceptance run.

## Regression constraints

Do not reintroduce:

- provider-specific physical lookup into generic client interaction discovery;
- client/global `getZombieList()` scans;
- Bandits API imports inside quest objective modules;
- counting framework-owned physical NPCs as infected quest targets;
- multiple active runtime ids for one logical NPC;
- client-authoritative dialogue or quest transitions;
- map markers as objective authority;
- framework identity stored as Bandits physical identity;
- `onlineID` or username as durable RPG identity;
- client access to server persistence internals;
- recomputing persisted canonical quest targets from a rematerialized NPC;
- inheriting a retired character's RPG identity into a new life;
- exposing every world NPC/faction to a fresh character without discovery.

## Next after v0.3.3 acceptance

The next milestone should be the **Character Knowledge / discovery foundation**, not the full faction simulator.

Recommended order:

```text
CharacterKnowledge store
    -> DiscoverNPC / DiscoverFaction server transitions
    -> sanitized Known People projection
    -> Known People page in RPG Hub
    -> NPC relationship state
    -> faction discovery / faction relations
    -> larger faction/world simulation
```

This layer must be keyed by durable `characterId`. A new character begins with no discovered Alexey/factions even though those entities continue to exist in the persistent world.
