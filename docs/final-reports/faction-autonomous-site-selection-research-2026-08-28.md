# Final research report: autonomous faction site selection and safe spawning

Date: 2026-08-28

Status: **research/design complete; implementation and runtime acceptance not started**

Target runtime: **Project Zomboid Dedicated Server, Build 42.20.4**

Related design document:

- `docs/design/faction-autonomous-site-selection-and-spawn-safety.md`

## 1. Why this research was needed

The next planned QuestFramework stage is physical/world-level faction simulation: factions must be able to appear in the world, establish places to live, maintain persistent members and later support squads, patrols, population and relocation.

The initial simplistic idea of manually authoring a base coordinate per faction was rejected. That model would make factions map-specific, fragile on modded maps and effectively permanent attachments to one building.

The required model is instead:

```text
Faction identity
    != current faction site
    != one building
    != one (x,y,z)
    != Bandits2 physical NPC instances
```

A faction should describe what kind of place it prefers. The server should discover candidates, score them, reserve a site, validate it non-destructively against the live world and only then materialize faction members.

The faction must remain the same faction if it later abandons, loses or relocates its site.

## 2. Core architectural decision

The selected architecture is:

```text
FactionDefinition
    |
    v
FactionSiteProfile
    |
    v
FactionSiteCandidateIndex
    |
    v
FactionSiteAllocator
    |
    +--> hard safety filters
    +--> weighted scoring
    +--> reservation
    |
    v
LoadedSiteValidator
    |
    +--> reject/release
    |
    v
FactionSiteRegistry
    |
    v
Faction world/population layer
    |
    v
Runtime materialization adapter
    |
    v
Bandits2
```

The site allocator is a **QuestFramework/server domain**. Bandits2 is not allowed to own faction site identity or decide strategic faction geography.

Bandits2 remains the physical NPC provider/materializer.

## 3. Research corpus

The investigation used the existing Workshop snapshots under `изучить/` and the previous reports under `docs/mod-research/`.

Most relevant references:

### P0 — Bandits2

Workshop snapshot:

- `изучить/P0/3268487204`

Research report:

- `docs/mod-research/P0/3268487204-bandits2.md`

Relevant source:

- `mods/Bandits/42.20/media/lua/server/BanditServerSpawner.lua`
- `mods/Bandits/42.20/media/lua/server/BanditServerCommands.lua`
- `mods/Bandits/42.20/media/lua/shared/BanditBrain.lua`
- `mods/Bandits/42.20/media/lua/shared/BanditCustom.lua`

### P2 — True Companions

Workshop snapshot:

- `изучить/P2/3751199292`

Research report:

- `docs/mod-research/P2/3751199292-true-companions.md`

Relevant source:

- `shared/BanditsNPCBeaconRegistry.lua`
- `shared/BanditsNPCBase.lua`
- `server/BanditsNPCWildSpawn.lua`
- `server/BanditsNPCSpawnServer.lua`

### P1 — PZNS Framework

Workshop snapshot:

- `изучить/P1/3001908830`

Research report:

- `docs/mod-research/P1/3001908830-pzns-framework.md`

Relevant source:

- `02_mod_utils/PZNS_WorldUtils.lua`
- `11_events_spawning/PZNS_Events.lua`

### P0 — Dynamic Trading Common / V2

Workshop snapshot:

- `изучить/P0/3635333613`

Research report:

- `docs/mod-research/P0/3635333613-dynamic-trading-common-dynamic-trading-v1-v2.md`

Useful areas:

- persistent NPC UUIDs;
- faction/NPC data separation;
- server-side synchronization;
- colony resident presence checks.

### P1 — Better Safehouse

Workshop snapshot:

- `изучить/P1/3634569678`

Research report:

- `docs/mod-research/P1/3634569678-better-safehouse.md`

Useful primarily as a reference for respecting native/player SafeHouse ownership and server-side protected-area semantics.

### Additional contextual references

- Bandits Week One — world/place event orchestration;
- Dynamic Objectives — server-side world encounters;
- existing QuestFramework persistent NPC identity work;
- existing NPCFixes Bandits wanderer-devirtualization investigation.

## 4. Bandits2 findings

Bandits2 already contains several useful physical-spawn protections.

Its server spawner can inspect or derive:

- world/meta zones;
- ground type;
- whether a square is outside;
- free/occupied state;
- player proximity;
- native SafeHouse overlap;
- building/room context;
- loaded-square availability;
- basement/RV-interior compatibility gates;
- room-based spawn points for defender groups.

It also has a useful distinction between normal group spawn and a `Defend` program when a group is materialized into a building.

### What we should reuse conceptually

- supported Bandits group/clan spawn path;
- Bandits' own normal brain initialization/banditization path;
- free-square checks;
- SafeHouse exclusion;
- loaded-world checks;
- provider-specific spawn retries.

### What Bandits2 does not solve

Its normal spawn logic is still predominantly framed as:

```text
player/event exists
-> choose spawn point near that activity
-> create group
```

That is not the same problem as:

```text
faction exists as a durable world entity
-> autonomously choose a long-term settlement
```

Therefore Bandits2 must not become the `FactionSiteAllocator`.

## 5. True Companions findings

True Companions contains the most useful site/persistence concepts found in the research set.

### 5.1 Server-visible site registry

`BanditsNPCBeaconRegistry.lua` separates persistent site data from client UI.

Important patterns:

- sites are records in Global ModData;
- the server owns writes in multiplayer;
- sites use reconstructible coordinate data rather than persistent `IsoObject` references;
- derived caches are not treated as authoritative persistent world identity;
- queries go through a registry rather than keeping arbitrary live world references.

This is directly applicable to future `FactionSiteRegistry`.

### 5.2 Derived base-resource scan

`BanditsNPCBase.lua` can inspect a defined base area and discover useful features such as:

- beds;
- chairs;
- televisions;
- food containers;
- water sources;
- stoves;
- washing machines;
- windows;
- shelves/reading furniture.

The implementation prefers engine/object properties and object classes over giant hardcoded tile-sprite lists where possible.

This is useful for mod-map compatibility because correctly configured modded furniture can be classified without our framework knowing every sprite name.

The scan is also intentionally bounded and cached instead of being performed continuously.

### 5.3 Key change required for QuestFramework

True Companions expects a player-authored site/beacon.

QuestFramework needs to replace:

```text
player selects site
```

with:

```text
server FactionSiteAllocator discovers and reserves site
```

The downstream site/resource ideas remain useful.

## 6. PZNS findings and rejected pattern

PZNS contains convenient conceptual helpers for:

- getting building rooms;
- finding random/free room squares;
- retaining logical NPC state while physical NPC objects are not active.

However, its spawn-safety helper includes an unacceptable pattern:

```text
spawn area contains zombies
-> remove zombies from world
-> spawn NPC safely
```

`PZNS_ClearZombiesFromSquare()` directly removes live zombies with `removeFromWorld()`.

This behavior is explicitly rejected for QuestFramework.

### Architectural rule

**The faction system must never make an invalid candidate valid by destructively deleting unrelated world population or objects.**

If a location is unsafe or blocked, valid responses are:

- reject the location;
- defer activation;
- choose another square;
- materialize outside and let NPC AI enter;
- choose another building;
- later let the faction fight zombies through normal gameplay AI.

Invalid responses are:

- delete all zombies in a radius;
- clear all moving objects;
- remove vehicles;
- destroy furniture;
- silently unlock/rewrite the building merely to permit spawn.

This rule is especially important because the project has already identified a Bandits2 wanderer-devirtualization path capable of broadly destroying physical zombie-backed NPCs.

## 7. Site profiles instead of coordinates

A faction definition should describe **preferences and constraints**, not a base location.

Conceptual example:

```lua
siteProfile = {
    kind = "settlement",

    minRooms = 3,
    preferredZones = {
        TownZone = 1.0,
        Farm = 0.4,
        Ranch = 0.6,
    },

    minDistanceFromPlayers = 100,
    minDistanceFromPlayerSafehouses = 150,
    minDistanceFromOtherFactionSites = 250,

    wantsIndoor = true,
    wantsWater = true,
    wantsBeds = true,
    wantsRoadAccess = true,
}
```

This permits different faction archetypes without hardcoded map locations:

```text
traders
-> road/town/storage/access preference

survivalists
-> farms/ranches/houses/water

raiders
-> remote/industrial/defensible places

military
-> compounds/open perimeter/road access

nomads
-> temporary outdoor sites rather than buildings
```

The same allocator can therefore work on vanilla and modded maps.

## 8. Candidate discovery must be lazy and bounded

A full-map square scan at server boot is rejected.

A large modded Project Zomboid world may contain enormous numbers of squares and buildings. Forcing all of them through `getGridSquare()` or loading chunks for faction placement would create unnecessary server stalls and risk interacting badly with normal streaming.

The preferred design is a two-level candidate system.

### Level 1 — coarse candidate discovery

Use confirmed Build 42.20.4-safe metadata or naturally observed loaded regions to create inexpensive candidate fingerprints.

Conceptually:

```text
world metadata / loaded region
-> building or site fingerprint
-> cheap properties
-> CandidateIndex
```

### Level 2 — expensive live validation only for top candidates

```text
CandidateIndex
-> coarse score
-> keep top-N
-> reserve best candidate
-> validate when actual geometry is loaded
```

This means the allocator does not need to load the whole world to know that a possible settlement exists.

## 9. Candidate scoring

Most world characteristics should be weighted rather than binary.

Conceptual score:

```text
score =
    zoneSuitability
  + buildingCapacity
  + resourceFitness
  + roadAccess
  + defensiveGeometry
  + factionPreference
  - playerInterferenceRisk
  - safehouseProximityPenalty
  - factionOverlapPenalty
  - recentFailurePenalty
```

### Why scoring matters

Hardcoding a small list of known vanilla zone names would make the system brittle on modded maps.

Unknown/custom zone types should generally mean:

```text
unknown zone
-> neutral or low score
```

not:

```text
unknown zone
-> exception / permanent rejection
```

Only safety-sensitive conditions should be hard rejects.

## 10. Hard safety exclusions

The first implementation should at minimum reject or defer:

- player/native SafeHouse overlap;
- already reserved or active site conflict;
- activation too close to active players;
- final validation while geometry is unavailable;
- no valid free materialization squares;
- unsupported special interiors/transforms;
- explicit server-owner blacklist regions.

Additional hard exclusions should only be added when there is a clearly identified game-state integrity reason.

## 11. Reservation is required before materialization

Two factions must not be able to simultaneously decide that the same building is their base.

The server therefore needs an atomic reservation stage before any physical spawn.

Suggested state machine:

```text
DISCOVERED
-> CANDIDATE
-> RESERVED
-> VALIDATING
-> ACTIVE
-> DORMANT
-> ABANDONED
-> RELOCATING
```

A failed final validation should release the reservation and allow another candidate to be selected.

Physical Bandits must not appear before the site has passed the required logical state transition.

## 12. Durable site representation

A faction site is an area/resource envelope, not one spawn tile.

Conceptual durable record:

```text
FactionSite
├── siteId
├── factionId
├── kind
├── state
├── anchor
├── bounds
├── buildingFingerprint
├── zoneContext
├── capacity
├── validationRevision
├── createdWorldHours
├── lastValidatedWorldHours
└── derived
    ├── beds[]
    ├── water[]
    ├── food[]
    ├── guardPoints[]
    └── spawnPoints[]
```

No durable entry should contain live references to:

- `IsoBuilding`;
- `IsoRoom`;
- `IsoGridSquare`;
- `IsoObject`;
- Bandits zombie objects.

Only reconstructible IDs/fingerprints/coordinates should be persistent.

The `derived` data must be rebuildable from the loaded world.

## 13. Multiple sites must be supported by schema

Even if the first runtime implementation allows one active settlement per faction, persistence should be shaped as:

```text
factionId -> siteIds[]
```

rather than:

```text
factionId -> baseX/baseY/baseZ
```

This leaves room for future:

- main settlement;
- trader outpost;
- checkpoint;
- temporary scavenger camp;
- captured location;
- abandoned site;
- relocation/migration.

## 14. Relocation

A faction is not permanently attached to its first chosen site.

Possible invalidation reasons later:

- major building destruction/fire;
- player protected-area conflict;
- scripted world change;
- faction simulation decision;
- site becoming otherwise unusable.

The expected flow is:

```text
ACTIVE
-> invalidated
-> RELOCATING
-> candidate search
-> RESERVED
-> VALIDATING
-> ACTIVE at new site
```

The faction identity and surviving member identities remain unchanged.

## 15. Relationship to persistent NPC identity

The existing QuestFramework NPC-persistence rule extends naturally to faction members.

```text
logical npcId
    != Bandits brain.id
    != current zombie instance
```

For a generated faction resident:

```text
faction npc record
-> persistent framework npcId
-> factionId
-> siteId / squadId / role
-> runtime binding
-> current Bandits2 body
```

Server restart or chunk unload may replace the physical Bandits runtime while the logical identity remains the same.

## 16. Runtime materialization boundary

The site allocator should expose a provider-neutral request such as:

```text
MaterializeFactionSite(siteId, populationRequest)
```

A Bandits adapter then converts validated logical spawn requirements into supported Bandits server spawn calls.

The allocator itself must not directly depend on:

- `BanditBrain` internals;
- `brain.id`;
- Bandits clan implementation details;
- zombie object identity.

This preserves the provider boundary already established by QuestFramework.

## 17. Population and squads are later layers

Site allocation should be implemented before personality simulation and before complex faction economy/warfare.

Recommended progression:

```text
Faction identity/knowledge/relations
-> Faction site discovery/allocation
-> Persistent faction world state
-> Generated persistent NPC identity
-> Squad model
-> Population controller
-> Materialization / virtualization
-> Patrol / guard / movement
-> Multiple bases / relocation
-> faction economy/conflict
-> NPC personality layer
```

This order prevents us from building sophisticated individual NPC behavior before the framework can reliably decide where those NPCs exist in the world.

## 18. Performance rules

The allocator must follow these rules from the first implementation:

1. Server-authoritative only.
2. Never scan every world square at server boot.
3. Maintain a bounded candidate queue/top-N set.
4. Run expensive resource scans only for loaded candidates being seriously considered or already active.
5. Cache derived site scans.
6. Re-scan slowly or on meaningful invalidation, not every tick.
7. Never run global allocation once per player.
8. Never run candidate work from render/update loops intended for client presentation.
9. Keep explicit rejection/scoring diagnostics.
10. Provider spawn failure must not corrupt or silently delete logical site state.

## 19. Diagnostics requirement

Dry-run allocator logs should explain decisions.

Example desired diagnostics:

```text
[LCCQF][FACTION:SITE] candidate key=... faction=...
  zoneScore=12
  capacityScore=18
  resourceScore=8
  roadScore=5
  safehousePenalty=0
  factionDistancePenalty=0
  total=43
  result=accepted-for-reservation
```

Rejected candidate example:

```text
[LCCQF][FACTION:SITE] reject key=... reason=player-safehouse-overlap
```

This will be critical for tuning modded-map behavior without guessing why a faction selected a strange location.

## 20. Build 42.20.4 compatibility note

The project target is now **Build 42.20.4**, not 42.20.3.

The official 42.20.4 hotfix published on 2026-08-26 is primarily a security hotfix. Its explicitly documented mod-facing breaking change is the removal of:

- `loadstring`;
- `loadstream`.

Mods that previously executed code received from a server are expected to replace that pattern with explicit commands/method calls.

No published 42.20.4 changelog item was found describing a change to meta-grid/building/zone spawn semantics.

Nevertheless:

- 42.20.3 decompiled Java must not be considered authoritative for exact method signatures;
- if allocator implementation depends on a Java/API method not already verified by active 42.20.4 Lua/mod usage, verify it against 42.20.4 or use a small capability probe;
- do not reintroduce `loadstring`/`loadstream` anywhere in the faction implementation.

Official reference checked during this research:

- Steam announcement: `42.20.4 STABLE & 42.19.2 UNSTABLE & 41.78.21 LEGACY Hotfixes Released`, 2026-08-26.

## 21. Recommended implementation slice

The next code stage should **not immediately spawn a living faction**.

Build a dry-run allocator first:

1. `FactionSiteRegistry` durable persistence and states.
2. `FactionSiteCandidateIndex`.
3. provider-neutral `FactionSiteProfile` definitions.
4. simple building candidate discovery from verified world APIs / loaded-world observations.
5. SafeHouse, player-distance and site-overlap hard filters.
6. weighted scoring.
7. atomic reservation.
8. detailed logs showing top candidates and reject reasons.
9. restart persistence test: reserved `siteId` remains stable.
10. only after allocator behavior looks sensible, implement loaded site resource validation.
11. only after validation passes, connect Bandits2 materialization.

### First acceptance target

The ideal first milestone is:

```text
server starts
-> test faction has no authored coordinates
-> allocator discovers candidates
-> ranks them
-> rejects unsafe ones
-> reserves one appropriate site
-> saves reservation
-> server restarts
-> same logical site remains reserved
-> zero NPCs spawned
-> zero zombies/world objects removed
```

Once this works reliably, physical population can be layered on safely.

## 22. Runtime acceptance matrix for later

### Allocation

- modded/unknown zone types do not crash;
- no full-map startup hitch;
- same save retains site reservation across restart;
- two factions cannot reserve the same site;
- blacklist works;
- no client can authoritatively choose a faction site.

### Safety

- player SafeHouse candidate rejected;
- active player near initial materialization causes defer/retry;
- zombie presence never triggers broad deletion;
- vehicle/furniture presence is not destructively cleared;
- unsupported basement/interior candidate fails cleanly.

### Persistence

- site survives chunk unload;
- logical faction survives absence of physical NPCs;
- generated NPC IDs remain stable through runtime replacement;
- Bandits materialization failure does not lose site/world state.

### Multiplayer

- site decision runs once on server, not independently for each player;
- late join receives only needed projections;
- two simultaneous players near the site do not cause duplicate population;
- reconnect does not duplicate residents.

### Relocation

- invalidation produces explicit `RELOCATING` state;
- new site selection does not change `factionId`;
- surviving logical NPC IDs remain the same;
- old site is released through an explicit state transition.

## 23. Explicit non-goals of the first allocator

Do not combine initial site selection with:

- faction wars;
- dynamic territory conquest;
- resource economy;
- personality simulation;
- diplomacy AI;
- massive offline squad simulation;
- autonomous construction/remodeling of buildings;
- destructive zombie clearing.

Those systems should consume a stable faction/site/world-state foundation later.

## 24. Final conclusion

The research supports a clear direction:

**Faction spawning must begin with autonomous, server-owned, non-destructive site allocation—not with fixed authored bases and not with direct Bandits spawn calls.**

The most valuable pieces from existing mods are complementary rather than complete:

- Bandits2: physical spawn/runtime provider and useful immediate spawn checks;
- True Companions: durable site registry ideas and bounded derived base-resource scanning;
- PZNS: building/room helper concepts, but also a concrete destructive anti-pattern to avoid;
- Dynamic Trading V2: persistent logical NPC/faction separation ideas;
- Better Safehouse/native SafeHouse: protected player territory that must be respected.

QuestFramework should combine those lessons into its own provider-neutral domain:

```text
Faction
-> autonomous site requirements
-> bounded world candidate discovery
-> scoring
-> reservation
-> non-destructive live validation
-> persistent site
-> persistent faction members/squads
-> Bandits2 materialization
```

This architecture keeps factions portable across vanilla and modded maps, allows relocation and multiple sites later, protects normal game world state and preserves the server-authoritative model required for Dedicated Server multiplayer.
