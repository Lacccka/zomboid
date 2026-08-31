# QuestFramework external research synthesis and roadmap impact

Date: 2026-09-01

Status: **research integrated into architecture/roadmap; most newly proposed subsystems are not implemented or runtime-accepted yet**

Target runtime: **Project Zomboid Dedicated Server, Build 42.20.x**

Target mod: `WorkshopPatches/QuestFramework` / Mod ID `LaccckaQuestFramework`

Baseline branch state when this document was written: `b33862e0ffed860074a53a3510a712b232ba3990`

## 1. Purpose

This document records conclusions from a second external research pass across:

- current/near-current Build 42 Workshop RPG/NPC/economy/settlement mods;
- existing local source audits under `docs/mod-research/`;
- Project Zomboid/Indie Stone NPC and Build 42 design material;
- community-reported multiplayer/performance/UI failure modes.

It is intentionally written **relative to the current LaccckaQuestFramework architecture**, not as a generic list of interesting features.

The purpose is to answer four questions:

1. Which missing systems are important enough to change the roadmap?
2. Which abstractions must be introduced **before** we harden the current economy model?
3. Which existing QuestFramework invariants are externally validated and must not regress?
4. Which patterns from other mods should explicitly **not** be copied?

## 2. Current QuestFramework baseline

At the time of this research pass, QuestFramework already has or is actively building the following server-authoritative stack:

```text
persistent logical NPC identity
    -> Bandits2 provider adapter
    -> server-validated proximity / E interaction
    -> DialogueSession
    -> QuestService / objectives / branching / rewards
    -> NPC relationships and knowledge
    -> factions / reputation / membership / ranks
    -> autonomous faction site selection
    -> persistent logical faction population
    -> virtualization / rematerialization / relocation
    -> jobs / schedules / guard duty
    -> exact settlement container discovery
    -> observed physical stock
    -> settlement economy snapshot
    -> logical consumption demand
    -> transactional physical consumption
    -> dynamic supply signals / supply quests
```

The following identity rule remains fundamental:

```text
logical NPC/faction/site identity
    != Bandits brain.id
    != IsoZombie runtime instance
    != provider profile id
    != current building coordinates
```

Bandits2 remains a physical runtime provider behind an adapter boundary.

## 3. Research corpus and confidence levels

There are two source classes in this report.

### 3.1 Locally source-audited references

These already have local reports and, in many cases, Workshop source snapshots under `изучить/`:

- Bandits2 — `docs/mod-research/P0/3268487204-bandits2.md`
- Dynamic Trading Common / V1 / V2 — `docs/mod-research/P0/3635333613-dynamic-trading-common-dynamic-trading-v1-v2.md`
- Dynamic Objectives — `docs/mod-research/P0/3715741925-dynamic-objectives.md`
- QP Survivor Contracts — `docs/mod-research/P0/3761060249-qp-survivor-contracts.md`
- QP Supply Requests — `docs/mod-research/P0/3766282536-qp-supply-requests.md`
- BONE / Bclan — `docs/mod-research/P0/3768490404-bone-bclan.md`
- Extraction Mode — `docs/mod-research/P0/3785397275-extraction-mode.md`
- SSR Quest System — `docs/mod-research/P0/2793385743-ssr-quest-system.md`
- PZNS Framework — `docs/mod-research/P1/3001908830-pzns-framework.md`
- Bandits Week One — `docs/mod-research/P1/3403180543-bandits-week-one.md`
- Better Safehouse — `docs/mod-research/P1/3634569678-better-safehouse.md`
- Interactive NPCs — `docs/mod-research/P1/3727050776-interactive-npcs.md`
- Pager Network — `docs/mod-research/P1/3744455714-pager-network.md`
- True Companions — `docs/mod-research/P2/3751199292-true-companions.md`
- NPC&QUEST — `docs/mod-research/P2/3754417819-npc-quest.md`
- Shared Faction Map — `docs/mod-research/P2/2877685881-shared-faction-map.md`

These can be used as implementation references, while still preserving our own server-authoritative boundaries.

### 3.2 External feature/reliability references

These were useful as feature/design signals but are **not automatically trusted as implementation references** unless their source is separately imported/audited:

- Project Remnants — Workshop `3738362476`
- Project A-Life — Workshop `3775216390`
- Terminal Logistic — Workshop `3766943005`
- The Mission — Workshop `3640172314`
- Shared Global Map — Workshop `3700272975`
- Project Zomboid official NPC/Metaverse design material
- Project Zomboid Build 42.20 feature/design material
- multiplayer/community reports around quest UI, random group objectives and NPC performance

For these sources we adopt useful **concepts**, not unreviewed authority/security/runtime code.

## 4. Highest-priority roadmap change: QuantitySemantics before the economy ledger

### 4.1 Problem

The current stock/consumption implementation is intentionally conservative and currently treats physical matching items as integer units.

That is acceptable for the first `food` acceptance test, but it is not a safe universal economy model.

Examples:

- one ammunition box is not one round;
- one bottle is not necessarily one liter of water;
- one medication item may represent several doses/uses;
- fuel and other fluids are volumetric;
- food can have portions, nutrition, rot and other state;
- tools/materials can have durability/condition semantics.

If the ledger is permanently designed around `one InventoryItem == one economic unit`, future traders, production, consumption and shortages will become mathematically inconsistent.

### 4.2 Decision

**Before the general economy ledger is considered stable, add a `QuantitySemantics` layer to `SupplyCategoryRegistry`.**

Conceptual API:

```text
SupplyCategoryDefinition
    id
    Matches(item)
    Measure(item) -> quantity
    UnitKind -> ITEM | USE | ROUND | LITER | PORTION | CUSTOM
    CanSplit(item)
    Normalize(quantity)
```

Initial categories can still use `ITEM`, but the persistence/ledger schema must not assume that this is universal.

### 4.3 Build 42 tag support

Build 42 increasingly exposes registries/tags/resources for extensibility. The category layer should therefore evolve from only predicate/full-type matching toward:

```text
fullType
item tag
fluid tag / fluid identifier
resource/location identifier
vanilla semantic predicate
custom provider predicate
```

This is especially important for a large modpack: a third-party mod should be able to introduce a medicine, food or material that participates in our economy without writing a one-off QuestFramework patch for every full type.

### 4.4 Priority

**P0 — implement before declaring the economy ledger schema final.**

## 5. Economy must track custody, not only aggregate stock

External settlement/scavenging systems reinforce a missing distinction in our current design:

```text
where an item exists
!= who currently has custody of it
!= which economic operation owns/reserved it
```

A future autonomous worker/scavenger/trader will physically move items between several custody domains.

### 5.1 Required custody states

At minimum:

```text
WORLD_STORAGE
RESERVED_FOR_JOB
NPC_CARGO
SQUAD_CARGO
SETTLEMENT_STORAGE
TRADER_STOCK
DELIVERY_IN_TRANSIT
CORPSE_OR_DROPPED
LOST_OR_UNRESOLVED
```

This does not mean inventing a virtual duplicate inventory. Physical objects remain authoritative whenever loaded.

The custody ledger records **which logical subsystem currently claims responsibility for a physical resource**.

### 5.2 Why this matters

Without custody/reservation:

- two NPC jobs can select the same food/tool;
- a trader can sell an item already assigned to consumption;
- a scavenger can return resources that the economy has already counted elsewhere;
- death/unload transitions can duplicate or silently lose cargo;
- quest delivery and settlement jobs can race over the same container contents.

### 5.3 Decision

Add an explicit `ResourceReservation / InventoryCustody` layer before autonomous hauling/scavenging/production.

**P0/P1 boundary:** ledger schema should anticipate custody now; physical NPC hauling can come later.

## 6. Settlement areas and storage roles

Project Remnants and logistics mods provide a useful recurring model: settlements should not treat every container and square equally.

### 6.1 Add `SiteArea` / `WorkZone`

Examples:

```text
FOOD_STORAGE
WATER_STORAGE
ARMORY
MEDICAL_STORAGE
GENERAL_STORAGE
WORKSHOP
SLEEP
GUARD_POST
PATROL_ROUTE
FARM
LOOT_DUMP
CORPSE_DROP
VEHICLE_PARKING
TRADER_AREA
PUBLIC_AREA
RESTRICTED_AREA
```

A zone is logical/persistent metadata pointing at bounded world geometry or exact container sets.

### 6.2 Storage profiles

Each storage area should be able to express accepted categories/items:

```text
StorageProfile
    acceptedCategories
    acceptedTags
    acceptedFullTypes
    rejectedFullTypes
    priority
    capacity policy
```

The stock scanner can then expose both:

```text
site total stock
stock by storage role
```

### 6.3 Decision

Do not make future jobs search arbitrary containers across the whole site.

Jobs should request a role/capability from the settlement model:

```text
Cook -> FOOD_STORAGE
Medic -> MEDICAL_STORAGE
Guard -> ARMORY
Hauler -> LOOT_DUMP -> destination role
```

**P1 — implement after QuantitySemantics/ledger foundation and before rich worker AI.**

## 7. Job/resource reservation is mandatory for autonomous workers

Once more than one logical NPC can act on resources, jobs need reservations.

Proposed model:

```text
JobReservation
    reservationId
    siteId
    npcId / squadId
    jobId
    resource locator / itemId / areaId
    quantity
    createdWorldHours
    expiresWorldHours
    state
```

Core rules:

- server-owned;
- bounded TTL;
- released on death/job cancellation/relocation;
- revalidated against physical world before mutation;
- no reservation is itself proof that the item still exists.

This is a direct extension of the exact-item/transaction philosophy already used by settlement transfers and consumption.

**P1.**

## 8. Add a Narrative / Storylet layer above quests

This is the largest conceptual addition from the research pass.

The old official Project Zomboid NPC/Metaverse design separates immediate physical AI from high-level narrative/meta simulation. The useful lesson for QuestFramework is that **quests should not be the only persistent representation of world events**.

### 8.1 Proposed architecture

```text
World state / economy / factions / NPC deaths
        |
        v
WorldEvent
        |
        v
Storylet evaluator
        |
        +--> consequences
        +--> faction/NPC memory
        +--> rumours/intelligence
        +--> dialogue availability
        +--> generated quest offer
        +--> radio/event presentation
```

A quest can be one consequence of a storylet, not the storylet itself.

### 8.2 Example

```text
settlement has medicine shortage
    -> medic becomes ill / casualty event occurs
    -> storylet records cause and participants
    -> leader/medic remembers event
    -> faction emits supply request
    -> radio message may be generated
    -> player delivers medicine
    -> storylet resolves
    -> affected NPCs remember who helped
    -> later dialogue/reputation/event selection changes
```

### 8.3 Why this is better than only dynamic quests

A `QuestInstance` belongs to player progression.

A `Storylet/WorldEvent` belongs to the **world** and may exist even if no player accepts a quest.

This distinction is essential for a living server.

**P1 major architecture stage after settlement economy becomes reliable.**

## 9. NPC memory must evolve from relationship scores to event memory and rumours

QuestFramework already has known people, relationships, faction knowledge and persistent world state.

The next step should not merely add more numerical reputation modifiers.

### 9.1 Proposed `NpcMemoryEvent`

```text
memoryId
worldEventId
subjectNpcId / factionId / characterId
sourceNpcId
kind
importance
emotionalImpact
witnessed | heard | inferred
truthState
claimedVersion
createdWorldHours
decayPolicy
```

### 9.2 Rumours/intelligence

NPCs and factions should be able to know something without that knowledge being globally true or universally shared.

This enables:

- rumours;
- conflicting accounts;
- accusations;
- lies/deception;
- investigation quests;
- reputation changes based on what a faction *believes*;
- dialogue referring to past player actions without hardcoded quest flags.

### 9.3 Authority

Truth/world events are server state.

NPC beliefs are also server state.

The client only receives dialogue/intelligence projections that the current character is allowed to know.

**P1/P2, but data contracts should be designed together with Storylets.**

## 10. Squad is a real simulation entity, not just `squadId`

Current faction population already carries squad identity, but future combat/expedition AI should not be implemented as independent Bandits brains making unrelated strategic choices.

Required future `Squad` state:

```text
squadId
factionId
leaderNpcId
memberNpcIds
mission/job
rallyPoint
formation/spacing policy
shared threat/contact board
rules of engagement
retreat policy
ammo state
medical state
cargo custody
homeSiteId
```

Provider NPC brains execute local movement/combat, but QuestFramework decides shared mission/strategic state.

This protects the provider boundary and improves MP determinism.

**P1 before patrol/scavenge/convoy systems become complex.**

## 11. WorldEventDirector and encounter templates

Research into current NPC/A-Life mods produced a useful encounter vocabulary that fits our long-term design:

```text
PATROL
SCAVENGE_RUN
TRADER_CARAVAN
CONVOY
DISTRESS_CALL
RESCUE
AMBUSH
RAID
REINFORCEMENT
ROADBLOCK
PRISONER_TRANSFER
EVACUATION
OUTPOST_ESTABLISHMENT
```

### Decision

These should be authored as logical encounter/event templates and materialized only when relevant players are near enough.

```text
logical event
    -> interest/proximity activation
    -> runtime materialization
    -> provider execution
    -> world result
    -> virtualized continuation / resolution
```

Do not use permanently materialized NPCs/vehicles across the entire map.

**P1/P2 after Squad + Storylet foundations.**

## 12. Surrender / prisoner / disarm should be a separate domain

This feature is not immediate, but it has unusually high systemic value.

Potential states:

```text
HOSTILE
SURRENDERING
DISARMED
DETAINED
ESCORTED
IMPRISONED
RELEASED
EXECUTED
ESCAPED
```

It enables:

- bounty alive/dead;
- hostage/prisoner rescue;
- prisoner exchange;
- interrogation/intelligence;
- recruitment/defection;
- faction diplomatic consequences;
- execution/morality consequences.

Do not encode this as only an animation/program state inside Bandits2.

It must be a logical server-owned NPC state projected into the provider.

**P2.**

## 13. Faction communications / radio should be first-class

Current research supports adding a communication surface that does not require a physical NPC to remain materialized.

Proposed `FactionComms` use cases:

- distress calls;
- supply requests;
- contract announcements;
- faction warnings;
- patrol reports;
- trader/market notices;
- NPC messages;
- rumours/intelligence;
- remote quest updates where design permits it.

This should integrate with existing radio/pager-like references but preserve QuestFramework authority.

Important rule:

```text
radio availability != remote permission to mutate arbitrary quest/faction state
```

Each action still goes through the same server validation/service layer.

**P1.**

## 14. Dialogue/quest UI must be interruptible and rewards deferrable

Community feedback around existing quest systems demonstrates a practical problem: dialogue/reward UI that hard-locks the player becomes dangerous during combat.

### Required UX rules

- dialogue can be closed immediately;
- threat/combat can interrupt non-critical presentation;
- closing UI must not corrupt server session state;
- reward presentation can be deferred even if reward application is server-complete;
- important server state must never depend on a popup remaining open;
- quest tracker should be compact/non-modal.

### Decision

When dialogue/UI is expanded, add a presentation-level `threatInterrupt/deferredPresentation` concept rather than coupling gameplay completion to UI lifetime.

**P1 UI hardening.**

## 15. Party/group quest random state must be generated once server-side

External multiplayer reports reinforce an invariant already compatible with our server-authoritative design:

```text
one PartyQuestSession
    -> one canonical objective seed/location/state
    -> projections to all participants
```

Do not let each client independently roll:

- extraction locations;
- encounter targets;
- quest variants;
- random NPC giver/target selection.

The server chooses once and persists the result.

This should be mandatory when party/group quests are implemented.

**P0 invariant for the future party-quest stage.**

## 16. Content authoring needs revisions and migrations

Live mission/admin editors demonstrate useful authoring ergonomics, but dynamically changing content creates persistence hazards.

QuestFramework definitions should eventually include:

```text
contentId
contentRevision
schemaVersion
migration policy
active/retired state
```

Persistent instances must not silently become invalid merely because an authored definition changed after a restart/update.

Required future behavior:

- deterministic upgrade/migration;
- historical definitions or snapshots when necessary;
- reject unsafe mutation rather than corrupt state;
- admin/content reload is server authoritative;
- reconnecting clients receive current sanitized definitions/projections.

This generalizes work already done for historical dynamic supply quest definitions.

**P1 before exposing public content-authoring APIs.**

## 17. Network interest management must become an explicit architecture layer

NPC-heavy mods repeatedly encounter performance problems from excessive physical simulation and network traffic.

QuestFramework already uses virtualization and bounded scans, which is correct but not sufficient for very large logical populations.

Future network model should include:

```text
spatial buckets
client interest subscriptions
site/squad/NPC relevance radius
change signatures / revision deltas
no server-wide broadcast for local events
```

Examples of data that should be interest-filtered:

- nearby physical NPC runtime projections;
- squad combat contacts;
- temporary encounter state;
- settlement presentation details;
- local dialogue prompts.

Global/character-owned data such as the player's own journal can continue using its appropriate channel.

**P0 before large-scale population/encounter stress testing.**

## 18. AccessPolicy should generalize current SafeHouse exclusion

The existing hard SafeHouse exclusion remains correct and is externally validated by problems seen in NPC/trader mods entering player bases.

The next abstraction should be broader:

```text
AccessPolicy
    PLAYER_SAFEHOUSE
    FACTION_PRIVATE
    ALLIED
    PUBLIC
    RESTRICTED
    HOSTILE
```

Use it for:

- settlement allocation;
- patrol routing;
- scavenging;
- trader access;
- raids;
- spawn/materialization;
- work zones.

A provider must not independently decide that a protected area is valid because a path exists.

**P1, with SafeHouse hard exclusion remaining P0 invariant.**

## 19. KnowledgeMap should be scoped by knowledge, party and faction

Shared map mods demonstrate that persistent annotations are useful, but QuestFramework should not automatically expose a globally omniscient map.

Proposed model:

```text
WorldIntel
    intelId
    source
    subject/location
    accuracy/confidence
    visibility scope
    expiry/invalidated state
```

Scopes can include:

- character;
- party;
- faction;
- public/server.

Possible intel:

- known settlements;
- dangerous locations;
- patrol sightings;
- trader/caravan position;
- quest intelligence;
- radio reports;
- rumours.

This should extend the existing character/faction knowledge architecture instead of creating an unrelated map-note subsystem.

**P1/P2.**

## 20. Provider perception/animation/sound must remain behind adapters

Current NPC mods show that updates to sound/animation/provider internals can freeze or break AI behavior.

QuestFramework therefore must not make logical state transitions depend exclusively on one Bandits sound/animation callback.

Preferred boundary:

```text
logical intent/state
    -> provider adapter command
    -> provider observations/events
    -> timeout/reconciliation
    -> logical transition
```

There must be server-owned timeout/recovery paths when provider callbacks never arrive.

This extends the existing rule that `brain.id` and provider objects are not logical identity.

**P0 invariant.**

## 21. Dynamic prices should be derived from real economy state

Future trading should not start from random buy/sell multipliers disconnected from settlement simulation.

Preferred pipeline:

```text
physical stock
    -> QuantitySemantics
    -> economy ledger / custody
    -> target stock / consumption rate
    -> scarcity/surplus
    -> faction strategy / events
    -> price policy
    -> trader offer
```

Potential modifiers:

- scarcity/coverage;
- condition/durability;
- spoilage/age;
- faction reputation/rank;
- local danger/logistics cost;
- producer/consumer role;
- recent world events.

The server calculates canonical trade outcomes.

**P1 after ledger/custody is reliable.**

## 22. Revised development roadmap

The research changes the order of several upcoming stages.

### Phase A — close current settlement runtime acceptance

1. Dedicated Server acceptance of physical consumption:
   - pending logical demand;
   - exact item selection;
   - `Remove` + server sync;
   - post-scan proof;
   - idempotent ACK;
   - restart/retry behavior.
2. Dedicated Server acceptance of dynamic supply quest end-to-end.
3. Keep current known hard-crash persistence limitation documented; do not claim kill-9 atomicity without a proven forced-persistence API.

### Phase B — fix economy semantics before making ledger permanent

1. `QuantitySemantics`.
2. Build 42 item/tag/resource matching support.
3. Generalized supply categories beyond food.
4. Ledger schema using measured quantity, not hardcoded item count.

### Phase C — economy ledger and custody

1. Server-owned bounded/idempotent transaction ledger.
2. Confirmed `INGRESS` from player/world transfers.
3. Confirmed `EGRESS` from consumption.
4. Lifetime/accounting aggregates.
5. `InventoryCustody` / resource reservations.
6. Reconciliation against physical world state.

### Phase D — settlement logistics

1. `SiteArea` / `WorkZone`.
2. storage profiles;
3. job reservations;
4. hauling;
5. role-based storage;
6. worker/medic/cook/scavenger jobs;
7. NPC/squad cargo custody.

### Phase E — traders and production

1. trader role/inventory;
2. scarcity-driven prices;
3. restock through real settlement economy;
4. production/consumption recipes;
5. shortages and surplus;
6. quests generated from real economic state.

### Phase F — narrative world simulation

1. `WorldEvent` registry;
2. `Storylet` engine;
3. consequences;
4. NPC/faction event memory;
5. rumours/intelligence;
6. dialogue generation/conditions from memory;
7. world-event-generated quest offers.

### Phase G — squads and world encounters

1. real logical `Squad` state;
2. shared threat/contact board;
3. patrol/scavenge missions;
4. convoy/caravan/raid/rescue/distress encounters;
5. virtualization/materialization of event participants;
6. radio/faction communications.

### Phase H — advanced RPG systems

1. surrender/prisoners/disarm;
2. prisoner exchange/interrogation;
3. party/group quests;
4. faction/party knowledge map;
5. live content authoring + migrations;
6. advanced story presentation/voice/radio;
7. faction diplomacy/territorial strategy.

### Phase I — scale/performance hardening

This begins earlier in audits, but becomes a dedicated acceptance stage before large public release:

1. spatial interest management;
2. tiered simulation cadence;
3. budgeted scans/work queues;
4. network deltas/revisions;
5. large virtual populations;
6. multi-player stress tests;
7. provider replacement/adapter tests.

## 23. Explicitly rejected patterns

The following patterns should not be copied even when found in useful reference mods.

### 23.1 Client authority over RPG state

Reject:

```text
client says quest completed
client says reward amount
client owns faction roster
client owns NPC persistence
client declares delivery count
```

All canonical progression remains server-owned.

### 23.2 Provider runtime objects as identity

Reject `brain.id`, IsoZombie, runtime id or current coordinates as durable NPC/faction identity.

### 23.3 Java GameServer replacement unless unavoidable

Prefer Build 42 vanilla Lua/server APIs. Do not adopt a custom replacement launcher/GameServer merely because another quest framework uses one.

### 23.4 Blocking dialogue/reward UI

Server progression must survive UI closure/interruption.

### 23.5 Global client-side random quest generation

Party/group/world objectives are generated once server-side.

### 23.6 Destructive world modification to make NPC systems work

Do not delete world objects, zombies, vehicles, corpses or furniture merely to make a faction site/encounter valid.

### 23.7 Unlimited physical NPC simulation

Logical populations/events may be large; physical materialization remains interest/budget constrained.

## 24. Existing QuestFramework decisions externally reinforced by this research

The following current decisions should be considered stronger, not weaker, after this research pass:

- server-authoritative quest/dialogue/faction/world mutations;
- permanent logical `npcId` independent of Bandits runtime;
- Bandits2 behind a runtime adapter;
- bounded world scans;
- no forced full-map loading;
- virtualization/rematerialization;
- hard SafeHouse exclusion;
- exact item/container reconciliation for physical inventory changes;
- idempotent rewards/transactions;
- per-life `characterId` for character-owned progression;
- historical/restart-safe quest definitions;
- sanitized client projections;
- provider-neutral core services;
- fail-closed handling when world/container/provider state cannot be proven.

## 25. New architecture invariants to adopt

These should become CI/runtime contracts as the corresponding subsystems are implemented:

1. Economy ledger quantity is produced by `QuantitySemantics`, never implicitly by `#items` for all categories.
2. Stock scanner, consumption, delivery, traders and jobs use the same category registry.
3. Autonomous resource mutation requires reservation/custody ownership and fresh physical reconciliation.
4. A `WorldEvent/Storylet` exists independently of whether a player has a quest for it.
5. NPC belief/memory is not automatically equivalent to objective world truth.
6. Squad strategic state is QuestFramework-owned, not independently invented by each provider brain.
7. Encounter randomization is persisted server-side before client presentation.
8. SafeHouse/access restrictions are checked before spawn, job, patrol, scavenging and trader routing.
9. UI interruption cannot roll back or duplicate canonical server state.
10. Local NPC/encounter network updates are interest-filtered before large-scale rollout.

## 26. Immediate next implementation decision

The next planned economy work was a generic economy ledger.

**This research modifies that plan.**

Before finalizing that ledger, implement at least the schema/API foundation for:

```text
SupplyCategoryRegistry
    -> QuantitySemantics
    -> B42 tag/resource-aware matching
    -> measured quantity
    -> ledger entry
```

Then build:

```text
confirmed physical ingress/egress
    -> bounded idempotent ledger
    -> custody/reservation
    -> storage areas/jobs
    -> traders/production
```

This avoids locking QuestFramework into `one item = one unit` immediately before adding water, ammunition, medicine, fuel and production.

## 27. Reference links

Existing local reports remain the primary code-level reference where available.

External conceptual references from this research pass:

- Indie Stone, NPC/Metaverse design: `https://projectzomboid.com/blog/news/2013/01/tales-from-the-metaverse/`
- Build 42.20 overview: `https://projectzomboid.com/blog/features-overview-build-42-20/`
- Build 42.1.30 / registry/tag direction: `https://theindiestone.com/forums/topic/88501-build-42130-unstable-multiplayer-released/`
- Dynamic Trading: `https://steamcommunity.com/sharedfiles/filedetails/?id=3635333613`
- Dynamic Objectives: `https://steamcommunity.com/sharedfiles/filedetails/?id=3715741925`
- SSR Quest System: `https://steamcommunity.com/sharedfiles/filedetails/?id=2793385743`
- Project Remnants: `https://steamcommunity.com/sharedfiles/filedetails/?id=3738362476`
- Project A-Life: `https://steamcommunity.com/sharedfiles/filedetails/?id=3775216390`
- Terminal Logistic: `https://steamcommunity.com/sharedfiles/filedetails/?id=3766943005`
- The Mission: `https://steamcommunity.com/sharedfiles/filedetails/?id=3640172314`
- Shared Global Map: `https://steamcommunity.com/sharedfiles/filedetails/?id=3700272975`

## 28. Closure status

Research capture: **complete for this pass**.

Architecture impact: **accepted into roadmap**.

Runtime acceptance of new proposals: **not started unless the subsystem already existed before this report**.

The document should be updated when QuantitySemantics, custody, Storylets, Squad or interest-management contracts become implemented and accepted.