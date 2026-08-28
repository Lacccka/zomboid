# Faction autonomous site selection and spawn safety

Status: design / research, not runtime accepted.

Target runtime: Project Zomboid Dedicated Server, Build 42.20.4.

## Goal

Faction bases must not be authored as fixed world coordinates and a faction must not be permanently attached to one map location. The server should be able to discover candidate places, reserve an appropriate site, validate it against the live world, materialize faction NPCs there, keep the logical faction/site identity while chunks unload, and relocate or abandon the site later without destructive world surgery.

The important distinction is:

```text
Faction identity != physical NPC instance != current site != one fixed coordinate
```

A faction can own zero, one or several sites. A site can change state over time. A physical Bandits2 zombie is only the currently materialized runtime representation of a faction member.

## Research references from `изучить`

### Bandits2 — useful spawn validation, but not a faction base allocator

Source snapshot: `изучить/P0/3268487204`.

`BanditServerSpawner.lua` already contains several useful protections:

- it probes the world/meta zone with `getWorld():getMetaGrid():getZoneAt(...)`;
- it distinguishes zone/ground/outside context;
- it rejects vanilla SafeHouse squares with `SafeHouse.isSafeHouse(...)`;
- it rejects occupied squares;
- it rejects points too close to real players;
- it contains explicit handling for basement/RV-interior cases;
- it can generate room-based spawn points for a building and run defenders with the `Defend` program.

This is a good **physical materialization backend**. It is not sufficient as a settlement allocator because its normal random spawn logic is centered around players/waves, not around durable faction territory.

### True Companions — best site/persistence model to adapt

Source snapshot: `изучить/P2/3751199292`.

`BanditsNPCBeaconRegistry.lua` has a useful split between authoritative site data and derived local/cache data:

- site records live in global mod data;
- server owns shared writes in MP;
- coordinates are persisted, not `IsoObject` references;
- `autoSpots` is explicitly treated as a derived cache and excluded from replication;
- downstream systems query the registry rather than holding direct world-object references.

`BanditsNPCBase.lua` shows another important pattern. Once an area exists it can discover useful base furniture automatically by scanning the loaded world and classifying objects using engine properties instead of hard-coded sprite lists where possible:

- beds;
- chairs;
- TV;
- food containers;
- water/bathroom sources;
- stove;
- washer;
- windows;
- shelves/reading furniture.

The scan is deliberately bounded (`MAX_TILES`) and cached/re-scanned instead of walking an arbitrary area every tick.

The part we should **replace** is the player-authored beacon/area. For our framework the site record should be created by a server-side allocator.

`BanditsNPCWildSpawn.lua` is also useful for materialization semantics: bounded spawn attempts, loaded-square verification, caps, one server-side hourly roll instead of multiplying work by online player count, and persistent refusal reasons. However, its arrival origin is still a pre-existing site/beacon.

### PZNS — useful building/room helpers, unsafe spawn cleanup

Source snapshot: `изучить/P1/3001908830`.

PZNS has useful conceptual helpers for:

- getting rooms from a building;
- choosing a random room/free square;
- moving NPCs between persistent data and loaded physical representation.

But `PZNS_ClearZombiesFromSquare()` removes live zombies from the world with `removeFromWorld()` in order to make spawn space. This is explicitly **rejected** for our design. A faction is not allowed to repair a bad spawn candidate by deleting vanilla population.

### Dynamic Trading V2 — useful persistent NPC/faction separation

Source snapshot: `изучить/P0/3635333613`.

Dynamic Trading V2 demonstrates persistent NPC UUID/faction data and server-side NPC synchronization. It is useful as a reference for keeping persistent logical records separate from the current zombie body, but its large alpha subsystem should not become our direct dependency or authority model.

### Better Safehouse — exclusion/reference for player-owned protected territory

Source snapshot: `изучить/P1/3634569678`.

Its main value here is confirming that native SafeHouse state is the correct ownership/protection surface to respect. Faction site discovery must treat existing player safehouses as hard exclusions, not mutate or silently take them over.

## Build 42.20.4 note

The published 42.20.4 hotfix is a security update. The mod-facing breaking change is removal of `loadstring` and `loadstream`. No published 42.20.4 note describes a change to world/meta-grid/building spawn semantics.

Therefore this design must:

- target 42.20.4 and never reintroduce dynamic-code execution through `loadstring`/`loadstream`;
- rely on runtime-observed 42.20.x APIs and the current active mod paths;
- not treat 42.20.3 decompiled Java as authoritative for signature-level decisions. If a Java method signature becomes critical, verify it against 42.20.4 or a small runtime capability probe before using it.

## Proposed architecture

```text
FactionDefinition
    |
    v
FactionSiteAllocator (server authority)
    |
    +--> CandidateIndex / candidate discovery
    |
    +--> coarse score
    |
    +--> SiteReservation
             |
             v
       LoadedSiteValidator
             |
       +-----+------+
       |            |
     reject       activate
                      |
                      v
               FactionSiteRegistry
                      |
                      v
               RuntimeMaterializer
                      |
                      v
                Bandits2 adapter
```

### 1. `FactionDefinition`

A faction defines **requirements**, not coordinates.

Example conceptual data:

```lua
siteProfile = {
    kind = "settlement",
    minRooms = 3,
    preferredZones = { TownZone = 1.0, Farm = 0.4, Ranch = 0.6 },
    avoidedZones = { DeepForest = 1.0 },
    minDistanceFromPlayers = 100,
    minDistanceFromPlayerSafehouses = 150,
    minDistanceFromOtherFactionSites = 250,
    wantsIndoor = true,
    wantsWater = true,
    wantsBeds = true,
    wantsRoadAccess = true,
}
```

Different faction archetypes can prefer different geography without any map-specific authored base coordinate:

- traders -> roads/towns, storage, parking/access;
- survivalists -> farms/ranches/edge-of-town;
- raiders -> industrial/remote buildings;
- military -> large compounds/open perimeter;
- nomads -> temporary outdoor/camp candidates rather than permanent buildings.

### 2. Candidate discovery must be lazy and bounded

Do **not** scan every square of the Knox map in one pass and do not require all chunks to be loaded.

Use two candidate sources:

1. **Coarse world metadata**, where an API is confirmed on 42.20.4. Zone/building metadata can cheaply produce candidate envelopes without touching every `IsoGridSquare`.
2. **Loaded-world discovery**, where chunks naturally become available. A loaded building can be fingerprinted and added/refreshed in the candidate index.

The allocator should work with a bounded queue:

```text
candidate discovered
-> lightweight metadata fingerprint
-> coarse score
-> top-N candidates retained
-> expensive validation only when needed/loaded
```

This avoids server startup stalls and prevents a global map walk from competing with normal chunk loading.

### 3. Site candidate identity

Do not persist a Java/Lua object reference.

A candidate/site identity should be reconstructible from stable world information, for example:

```text
world-space building/site key
+ bounding box / anchor
+ z span
+ optional building metadata fingerprint
```

The exact fingerprint fields should be limited to APIs verified on 42.20.4.

This allows the site to survive:

- chunk unload;
- server restart;
- physical NPC replacement;
- temporary absence of any online player near the site.

### 4. Two-stage validation

#### Stage A — coarse metadata validation

Cheap checks that do not mutate the world:

- map square/chunk is valid;
- candidate satisfies faction zone preference;
- approximate building/area size is sufficient;
- minimum distance from other reserved/active faction sites;
- not in a globally forbidden configured region;
- no known conflict with a player-owned protected area;
- avoid pathological location classes such as interior transforms/basements unless explicitly supported.

The result is a **reservation candidate**, not an active base.

#### Stage B — loaded live-world validation

When the relevant chunk/building is loaded, validate the actual live geometry:

- building/squares still exist;
- candidate contains enough free spawnable squares;
- no square chosen for arrival is inside a player SafeHouse;
- no active players inside or immediately next to the activation area;
- no conflict with another faction reservation/site;
- enough indoor/usable room geometry for the intended population;
- optional resource scan: beds, water, food/storage, seating, etc.;
- no attempt to clear zombies, destroy furniture, move vehicles, unlock doors, delete corpses or rewrite world objects just to make the candidate pass.

If validation fails, release the reservation and try another candidate.

### 5. Scoring instead of binary authored rules

Most characteristics should be weighted scores, not hard requirements.

Example:

```text
score =
    zoneSuitability
  + buildingCapacity
  + resourceFitness
  + roadAccess
  + isolationOrPopulationPreference
  + defensiveGeometry
  - playerInterferenceRisk
  - safehouseProximityPenalty
  - factionOverlapPenalty
  - recentFailurePenalty
```

Hard exclusions should remain small and safety-oriented.

This matters because a heavily modded map may introduce unknown/custom zone types. Unknown zones should usually become neutral/low-score candidates, not crash or be permanently forbidden.

### 6. Never make zombie deletion part of site acquisition

Rejected behavior:

```text
candidate has zombies
-> remove zombies
-> declare candidate safe
```

The world population is authoritative world state, not disposable spawn debris.

A faction may instead:

- postpone activation;
- choose another spawn square;
- choose another building;
- spawn outside the building and let its AI move in;
- later fight zombies through normal combat/AI if that gameplay is desired.

This also avoids repeating the class of Bandits wanderer devirtualization bug already observed in the project, where unrelated physical zombies can be destroyed by broad cleanup logic.

### 7. Reservation before materialization

A candidate must be atomically reserved on the server before physical NPC spawn begins.

Conceptual states:

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

Only one server subsystem may transition the site record.

Reservation prevents two factions from choosing the same building during the same update window.

### 8. Site is an area, not a single spawn tile

The durable record should store a site/territory envelope plus functional anchors, not one magic `(x,y,z)`.

Conceptually:

```text
FactionSite
├── siteId
├── factionId
├── state
├── anchor
├── bounds
├── buildingFingerprint
├── zoneContext
├── capacity
├── runtimePolicy
├── validationRevision
├── lastValidatedWorldHours
└── derived
    ├── beds[]
    ├── water[]
    ├── food[]
    ├── guardPoints[]
    └── spawnPoints[]
```

The `derived` section should be rebuildable and must not contain live `IsoObject` references.

### 9. Physical faction members spawn through an adapter

The allocator decides **where the faction exists logically**. It should not know Bandits internals.

A separate runtime adapter receives something like:

```text
MaterializeFactionSite(siteId, desiredPopulation)
```

and resolves validated spawn points to Bandits2 spawn calls.

For Bandits2 the preferred route is its existing server clan/group spawn path and normal banditization/persistence flow. Do not manually construct partially initialized Bandits brains when a supported server spawn path exists.

### 10. Stream out physical actors, keep logical site

An active faction site must continue to exist logically even when no relevant chunk is loaded.

```text
ACTIVE logical site
+ no nearby/loaded runtime relevance
-> physical NPCs may dematerialize according to provider rules
-> site remains ACTIVE/DORMANT in server persistence
```

When the area becomes relevant again, the framework reconciles logical residents to physical runtime instances using persistent framework NPC IDs.

This is the same identity rule already established for quest NPCs, extended from one NPC to faction membership.

## Safety exclusions

At minimum the allocator should refuse or heavily penalize:

- vanilla/player SafeHouse territory;
- an already reserved/active faction site from another faction;
- squares too close to active players during first materialization;
- invalid/unloaded geometry during the final validation phase;
- unsupported special interiors/transforms;
- candidate geometry with no valid free spawn points;
- explicitly blacklisted map areas configured by the server owner.

Additional exclusions should be introduced only when we can state exactly what game state they protect. Broad rules such as “delete all zombies in radius” or “clear all moving objects” are forbidden.

## Player interaction with future faction sites

A faction site should not be treated as immutable ownership of a building forever.

If later the world changes materially, for example:

- a player validly claims a SafeHouse overlapping the site;
- the building is burned/destroyed enough to fail validation;
- a scripted world event invalidates the location;
- faction simulation decides to migrate;

then the site can enter `RELOCATING` and the allocator searches again.

This is why `factionId -> hardcoded base coordinates` is the wrong persistence model.

## Multiple sites per faction

Design the registry as:

```text
factionId -> siteIds[]
```

not:

```text
factionId -> baseX/baseY/baseZ
```

Then later we can support:

- capital/main settlement;
- trader outpost;
- temporary scavenging camp;
- checkpoint;
- captured/abandoned site;
- migration.

The first implementation may cap a faction at one active settlement, but the persistence schema must not make that limitation permanent.

## Performance rules

1. Server authority only for discovery/reservation/activation state.
2. No full-map square scan during server boot.
3. Maintain a bounded candidate queue/top-N list.
4. Expensive furniture/resource scans only on loaded candidate areas.
5. Cache derived scans and invalidate/re-scan on a slow cadence or meaningful world change.
6. Never run candidate allocation per render tick or per player.
7. Global faction population decisions should run once per simulation interval, not once for every connected player.
8. Keep explicit diagnostic rejection reasons so a site allocator failure can be diagnosed from logs.

## Recommended first implementation slice

Do not start by spawning a complete faction settlement.

Implement in this order:

1. `FactionSiteRegistry` persistence and state machine.
2. `FactionSiteCandidateIndex` with bounded discovery.
3. one simple building-based `SiteProfile`.
4. hard safety filters: SafeHouse, player proximity, site overlap, valid/free squares.
5. weighted candidate scoring.
6. reserve one site for a test faction **without spawning NPCs**.
7. runtime command/log view that explains why each candidate passed/failed.
8. loaded validation and derived resource scan.
9. only then connect the accepted site to the Bandits2 materializer.
10. test restart, chunk unload/reload, two clients and relocation before enabling dynamic faction growth.

## Required runtime acceptance tests

### Allocator correctness

- same save/restart keeps the same `siteId` and reservation unless invalidated;
- two factions cannot reserve the same building;
- unknown/custom map zones do not crash scoring;
- no candidate causes a global/full-map scan spike.

### Safety

- candidate overlapping player SafeHouse is rejected;
- player entering an activation area prevents/defers first materialization;
- zombie presence never causes broad zombie deletion;
- vehicles/world objects are not removed to make spawn space;
- unsupported basement/interior candidate is rejected cleanly.

### Persistence/materialization

- site remains durable while chunk is unloaded;
- faction members re-bind after restart via framework NPC identity;
- late-joining clients receive site/faction projections, not ownership of simulation state;
- failed Bandits materialization does not lose the logical reservation/site.

### Relocation

- invalidated site transitions to `RELOCATING`;
- old site is released only according to an explicit state transition;
- new site gets a new `siteId` or a clearly versioned relocation record; NPC/faction identity remains unchanged.

## Architectural decision

The preferred model is therefore:

```text
Faction chooses a site profile
-> server discovers/scorers world candidates
-> server reserves one
-> loaded world validates it non-destructively
-> durable site becomes active
-> Bandits2 only materializes physical NPCs
-> site may later go dormant, be abandoned or relocate
```

This gives us dynamic faction geography without hardcoded bases and without treating the vanilla world as something the spawn system is allowed to clear or rewrite for convenience.
