--
-- True Companions - Survivors Beacon Station (v0.60)
--
-- A craftable radio post (see media/scripts/entity_tcbeacon.txt). From v0.60 it is
-- the ONLY way companions arrive: the old random wild-spawn events are gone.
--
-- WHY THE CHANGE. The wild spawner rolled a chance every in-game hour and dropped a
-- neutral survivor somewhere near the player. It was invisible, unpredictable, and
-- as of v0.52 still unproven -- a whole session's logging never caught it firing
-- once, because at ~8%/hour a short test never rolls. Worse, it gave the player
-- nothing to do: survivors either turned up or they didn't. The beacon replaces a
-- hidden dice roll with a thing you build, place, and control.
--
-- WHERE STATE LIVES. Two places, deliberately:
--   * On the beacon's own IsoObject ModData -- proven to persist, because
--     IsoObject.save writes the KahluaTable when non-empty. This is the truth for
--     "is this particular object a beacon, and what is it set to".
--   * A registry in GlobalModData keyed by coordinates, so the arrival logic can
--     find the beacon WITHOUT its chunk being loaded. An object you cannot reach
--     is not an object you can ask.
-- The registry is a cache of the objects; if the two ever disagree, the object wins.
--

BanditsNPC = BanditsNPC or {}
BanditsNPC.Beacon = BanditsNPC.Beacon or {}
local B = BanditsNPC.Beacon

-- ===========================================================================
-- STAGES
-- ===========================================================================
-- Three tiers of the same object. Stage 1 is what the Build menu places; 2 and 3
-- are reached by right-clicking the finished beacon and paying for an upgrade.
--
-- The stage lives on the object's ModData and upgrading repaints the sprites in
-- place. That is deliberately NOT "destroy and rebuild as a different entity":
-- keeping the same IsoObjects means the beacon's settings, its zones and its
-- registry entry survive the upgrade instead of having to be migrated across.
--
-- Every sprite below is verified FLOOR-STANDING in newtiledefinitions.tiles.txt --
-- no IsSurfaceOffset (which draws a sprite 34px up because it belongs on a table,
-- i.e. floating on a floor) and no IsoType (which risked vanilla radio behaviour).
--
--   1  Imekagi HK533p      a salvaged transceiver rig
--   2  Security terminal   a proper console
--   3  CeroSec terminal    a full relay console
--
-- THE BEACON IS ONE TILE. It was two -- a kitchen counter with a radio set beside it
-- -- and it looked like exactly that: two unrelated objects, and at stage 1 a small
-- walkie talkie sitting on the floor next to a cupboard. The counter was never the
-- point; it was there to carry a radio that could not be drawn on top of it, because
-- no vanilla entity face uses more than one layer.
--
-- WHAT WENT WRONG AT STAGE 1, EXACTLY, because it is the kind of thing that recurs:
-- appliances_com_01_40..43 (Tactical Walkie Talkie) carries `IsTableTop` AND
-- `IsoType = IsoRadio`. It is a small TABLE-TOP object that the engine also treats as
-- a working radio. Placed on a floor it reads as a dropped handset -- which is what
-- was reported. The v0.68 sprite check had been narrowed to IsSurfaceOffset and no
-- longer looked at IsoType, and that is how it got through.
--
-- Every sprite below is verified: single tile (no SpriteGridPos), floor-standing (no
-- IsSurfaceOffset, no IsTableTop), NOT a vanilla appliance (no IsoType), all four
-- facings present, and heavy enough to be furniture rather than a desk gadget
-- (PickUpWeight 200-350, Electrician tools -- these are consoles, not handsets).
--
-- ONE TILE ALSO DELETES A WHOLE CLASS OF BUG: no anchor tile to key the registry off,
-- no second half to repaint on upgrade, no second half to leave standing when the
-- first is removed.
B.STAGES = {
    -- Display names are NOT stored here; see B.StageName below for why.
    -- STAGE 1 HAS ONLY TWO FACINGS, and that is the sprite's own limit rather than an
    -- oversight. Security Wall Monitors (Base.Mov_SecurityWallMonitors) exists as S and
    -- E only, because it is a WALL object -- S hangs on a north wall, E on a west one.
    -- The entity declares needToBeAgainstWall so the engine will only let it be built
    -- where it renders correctly, and the build cursor's rotate key therefore cycles
    -- S and E rather than all four. Stages 2 and 3 have all four facings, so upgrading
    -- from either of these is fine.
    --
    -- Two earlier stage-1 sprites are kept recognised in LEGACY_SPRITES below: the
    -- Imekagi HK533p (v0.71-v0.74.2), which turned out to be a security camera, and the
    -- Standing Microphone (v0.74.3).
    [1] = { capacity = 2,
            station = { S = "security_01_4", E = "security_01_5" } },
    [2] = { capacity = 4,
            station = { N = "security_01_2",        E = "security_01_1",
                        S = "security_01_0",        W = "security_01_3" } },
    [3] = { capacity = 7,
            station = { N = "appliances_com_01_54", E = "appliances_com_01_53",
                        S = "appliances_com_01_52", W = "appliances_com_01_55" } },
}

-- sprite name -> { role, facing }. Built from the table above so the two can never
-- disagree; adding a stage or a facing needs no edit here.
local SPRITE_INFO = {}
for _, st in pairs(B.STAGES) do
    for facing, name in pairs(st.station) do
        SPRITE_INFO[name] = { role = "station", facing = facing }
    end
end

-- SPRITES A BEACON MAY STILL BE STANDING ON FROM AN EARLIER VERSION.
--
-- A beacon is recognised BY ITS SPRITE, so dropping a sprite from the table above
-- does not just change how new ones look -- it makes every beacon already built out
-- of it stop being a beacon. It would go unclickable, unremovable, and its whole site
-- (zones, stores, arrival state) would be stranded behind an object the mod no longer
-- recognises. Anyone who built one in v0.66 or v0.67 has exactly that object standing
-- in their base right now.
--
-- So the old Old-TV-antenna halves stay recognised as devices. They are NOT in
-- B.STAGES, so nothing new is ever built from them, and the first upgrade repaints
-- the beacon into the current set. The facings recorded here are the real ones from
-- newtiledefinitions.tiles.txt, so an upgrade turns the halves the right way round.
local LEGACY_SPRITES = {}
local function legacy(name, role, facing)
    LEGACY_SPRITES[name] = { role = role, facing = facing, legacy = true }
end
-- v0.66/v0.67 counters (S and E only), then v0.68's four-facing counters
legacy("fixtures_counters_01_13", "counter", "S"); legacy("fixtures_counters_01_11", "counter", "E")
legacy("fixtures_counters_01_9",  "counter", "N"); legacy("fixtures_counters_01_15", "counter", "W")
legacy("fixtures_counters_01_37", "counter", "S"); legacy("fixtures_counters_01_35", "counter", "E")
legacy("fixtures_counters_01_33", "counter", "N"); legacy("fixtures_counters_01_39", "counter", "W")
legacy("fixtures_counters_01_61", "counter", "S"); legacy("fixtures_counters_01_59", "counter", "E")
legacy("fixtures_counters_01_57", "counter", "N"); legacy("fixtures_counters_01_63", "counter", "W")
-- the Old TV antenna halves (v0.66/0.67) and the walkie talkie set (v0.68)
legacy("appliances_com_01_80", "device", "S"); legacy("appliances_com_01_82", "device", "E")
legacy("appliances_com_01_81", "device", "S"); legacy("appliances_com_01_83", "device", "E")
legacy("appliances_com_01_42", "device", "S"); legacy("appliances_com_01_43", "device", "E")
legacy("appliances_com_01_40", "device", "N"); legacy("appliances_com_01_41", "device", "W")
-- Earlier stage-1 sprites, kept recognised as STATIONS (not devices) so a beacon
-- already standing on one is still a whole beacon in its own right -- clickable,
-- upgradeable, removable -- rather than becoming scenery the mod no longer knows
-- about. Upgrading converts it to the current set.
--   v0.71-v0.74.2  Imekagi HK533p, which turned out to be a security camera
legacy("appliances_com_01_44", "station", "S"); legacy("appliances_com_01_45", "station", "E")
legacy("appliances_com_01_47", "station", "N"); legacy("appliances_com_01_46", "station", "W")
--   v0.74.3        Standing Microphone
legacy("appliances_com_01_69", "station", "S"); legacy("appliances_com_01_71", "station", "E")
legacy("appliances_com_01_68", "station", "N"); legacy("appliances_com_01_70", "station", "W")

for name, info in pairs(LEGACY_SPRITES) do
    if not SPRITE_INFO[name] then SPRITE_INFO[name] = info end
end

-- The sprite a given stage wears for a given role and facing, with a fall back to
-- the S facing. The fallback matters for a beacon saved by v0.67, which could be
-- standing on an N or W sprite this table did not then know about: without it, an
-- upgrade would blank the tile rather than repaint it.
function B.SpriteFor(stage, facing)
    local st = B.STAGES[stage] or B.STAGES[1]
    return st.station[facing or "S"] or st.station.S
end

B.MAX_STAGE = 3

-- What each upgrade costs. Tools are not consumed; `count` items are.
-- Every id here was verified present in the vanilla item scripts.
B.UPGRADE_COST = {
    [2] = {
        { type = "Base.ElectricWire",    count = 2 },
        { type = "Base.ElectronicsScrap", count = 3 },
        { type = "Base.SheetMetal",      count = 1 },
    },
    [3] = {
        { type = "Base.ElectricWire",    count = 4 },
        { type = "Base.ElectronicsScrap", count = 6 },
        { type = "Base.SheetMetal",      count = 3 },
    },
}

-- Every sprite any stage can wear -- this is how a beacon is recognised in the
-- world, so it must cover all of them or an upgraded beacon stops being findable.
local BEACON_SPRITES = SPRITE_INFO

local REGISTRY = "TrueCompanionsBeacons"

-- THE STORE LIVES IN shared/BanditsNPCBeaconRegistry.lua NOW (v0.75.11). Same ModData
-- key, so saves carry over untouched -- but a dedicated server can reach it, which it
-- could not when the only copy of this code was in client/. These two stay as thin
-- delegates so the ~30 call sites below did not have to change.
local function registry()
    return BanditsNPC.BeaconRegistry.Store()
end

local function keyFor(x, y, z)
    return BanditsNPC.BeaconRegistry.KeyFor(x, y, z)
end

-- Tell the server a site changed. No-op in SP beyond the transmit.
local function pushSite(x, y, z)
    pcall(function() BanditsNPC.BeaconRegistry.Push(keyFor(x, y, z)) end)
end

-- ---------------------------------------------------------------------------
-- identifying a beacon
-- ---------------------------------------------------------------------------

-- Is there a beacon site registered on this square or any of its four neighbours?
--
-- The registry is GLOBAL ModData, not chunk-local, so this answers for a beacon whose
-- chunk has never been loaded -- which is the reason the sprite test was primary in the
-- first place. Neighbours count because a legacy two-tile beacon is keyed on its counter
-- half, so its device half sits one square off the entry.
local function siteNear(obj)
    local ok = false
    pcall(function()
        local sq = obj:getSquare()
        if not sq then return end
        local sites = BanditsNPC.BeaconRegistry.Sites()
        if not sites then return end
        local x, y, z = sq:getX(), sq:getY(), sq:getZ()
        local probes = { {x, y}, {x + 1, y}, {x - 1, y}, {x, y + 1}, {x, y - 1} }
        for _, p in ipairs(probes) do
            if sites[BanditsNPC.BeaconRegistry.KeyFor(p[1], p[2], z)] then ok = true; return end
        end
    end)
    return ok
end

-- True if this world object is one of our beacons.
--
-- ===========================================================================
-- A SPRITE ALONE IS NOT PROOF, AND ASSUMING IT WAS TURNED EVERY KITCHEN IN KNOX
-- COUNTY INTO A BEACON (v0.77.9, player reports).
-- ===========================================================================
--
-- LEGACY_SPRITES exists so a beacon BUILT BY v0.66-v0.69 -- which was two tiles, a
-- counter plus a device -- is still recognised today. To do that it lists the twelve
-- vanilla counter sprites those beacons were made of... and those are ordinary kitchen
-- counters, standing in every house on the map. They were merged into BEACON_SPRITES,
-- this function matched on sprite alone, and so every counter in the world answered yes.
--
-- It did not stop at looking wrong either: B.State() writes tcBeacon = true and a full
-- settings table onto whatever it is handed, and State is reached through this test. So a
-- counter that was merely LOOKED at got permanently branded in the save. That is the
-- "turned into a beacon" in the reports -- it was a one-way conversion.
--
-- THE RULE NOW:
--   * our own flag is definitive, always;
--   * a CURRENT stage sprite is accepted on sight, because those are the sprites the mod
--     itself builds and upgrades to, and a beacon the player has just built has no flag
--     and no registry entry yet (it is created by a vanilla entity recipe, which gives us
--     no hook at the moment of construction -- see the OnObjectAdded handler below, which
--     closes that gap going forward);
--   * a LEGACY sprite must be CORROBORATED by a registry site on or beside the square.
--     Every genuine legacy beacon has one, because State wrote it the first time anyone
--     opened the thing. A kitchen counter has none, and never will.

-- ---------------------------------------------------------------------------
-- FIX(BUG-004): WAS THIS OBJECT BUILT BY A PLAYER, OR PLACED BY THE MAP?
--
-- DO NOT REMOVE THIS AS A REDUNDANT TYPE CHECK. It is the only thing separating our beacon
-- from the vanilla scenery that wears the same sprite, and without it every security console
-- in Knox County is a beacon.
--
-- Every sprite in B.STAGES is a VANILLA sprite (security_01_0..5, appliances_com_01_52..55),
-- so a sprite match alone cannot tell a built beacon from a map-placed console. This can:
--   * a player build goes through ISBuildIsoEntity, which constructs the object with
--     IsoThumpable.new(...) -- refs ISBuildIsoEntity.lua:603-605;
--   * map-placed scenery is a plain IsoObject and never an IsoThumpable.
-- Verified for BOTH build entry points, which matters because the right-click entry was
-- removed in v0.77.13 and beacons built by it are still out there in saves:
--   ISEntityBuildMenu.lua:120   ISBuildIsoEntity:new(...)   <- the removed right-click path
--   ISBuildPanel.lua:319        ISBuildIsoEntity:new(...)   <- the current buildings menu
-- Both reach the same constructor, so a beacon from either era is an IsoThumpable and stays
-- recognised. This is the same test A.IsStation has used since the Appearance Workstation
-- shipped (BanditsNPCAppearance.lua:138-142, "map furniture: never ours").
--
-- THREE-STATE ON PURPOSE: true (built), false (definitely scenery), nil (could not tell).
-- A failed probe must NOT read as "scenery", or an object we simply could not inspect would
-- be stranded -- which is the failure mode the first attempt at this fix actually shipped.
-- Callers therefore test `== false`, never `not isBuilt(...)`.
local function isBuilt(obj)
    local built
    pcall(function() built = instanceof(obj, "IsoThumpable") == true end)
    return built
end

function B.IsBeacon(obj)
    if not obj then return false end
    local name
    pcall(function()
        local spr = obj:getSprite()
        name = spr and spr:getName()
    end)
    local legacy = name and LEGACY_SPRITES[name] ~= nil

    local flagged = false
    pcall(function()
        local md = obj:getModData()
        flagged = md and md.tcBeacon == true
    end)

    -- A sprite the mod itself builds and upgrades to -- as opposed to a vanilla counter,
    -- microphone or security camera that an old two-tile beacon happened to be made of.
    local ours = name and SPRITE_INFO[name] ~= nil and not legacy

    if flagged then
        -- SELF-HEALING, and it has to live here rather than on a chunk-load sweep: this is
        -- the only place that already asks the question, so the repair costs nothing extra
        -- and happens exactly when it matters. Saves made before v0.77.9 are full of
        -- kitchen counters carrying this flag, and with the flag definitive they would
        -- otherwise stay beacons forever even though the sprite test no longer accepts
        -- them.
        --
        -- WIDENED (v0.77.13). It used to require a LEGACY sprite, which meant it healed
        -- exactly the twelve counter sprites and nothing else -- but the pre-v0.77.9 State
        -- branded whatever it was handed, and reporters listed "kitchen counter AND METAL
        -- CABINETS", plus a microphone. Anything not on that twelve-sprite list stayed a
        -- beacon forever. The test that matters is not which sprite it wears, it is
        -- whether anything CORROBORATES the flag: a genuine beacon either wears a sprite
        -- we build (new or upgraded) or has a registry site on/beside it (State wrote one
        -- the first time it was ever opened, which is also the only way it got flagged).
        -- An object with neither has a flag that nothing in the world backs up.
        -- FIX(BUG-004): `isBuilt(obj) == false` repairs the damage this bug already did.
        -- Older builds branded any current-stage sprite on sight, so saves contain security
        -- consoles carrying tcBeacon. `ours` is true for those, so the two clauses below
        -- both pass and the flag survived forever. Map scenery is definitively not an
        -- IsoThumpable, so that is the clause that finally clears them. A real beacon is
        -- built and is unaffected.
        if isBuilt(obj) == false or (not ours and not siteNear(obj)) then
            pcall(function()
                local md = obj:getModData()
                md.tcBeacon = nil
                md.tcBeaconState = nil
                if obj.transmitModData then obj:transmitModData() end
            end)
            return false
        end
        return true
    end

    if not (name and BEACON_SPRITES[name]) then return false end
    -- FIX(BUG-004): the sprite says "could be a beacon"; this says "is not map scenery".
    -- Placed BEFORE the `not legacy` shortcut below, because that shortcut is what accepted
    -- every vanilla security console on sight. See the note above isBuilt.
    if isBuilt(obj) == false then return false end
    if not legacy then return true end   -- a sprite we build ourselves
    return siteNear(obj)
end

local function spriteNameOf(obj)
    local name
    pcall(function()
        local spr = obj and obj:getSprite()
        name = spr and spr:getName()
    end)
    return name
end

-- Both halves of a beacon, given either half.
-- Returns { counter = obj|nil, device = obj|nil, facing = "S"|"E" }.
--
-- Scans the object's own square and its four neighbours, because the two tiles are
-- adjacent but which direction depends on the facing the player built it at.
-- Every IsoObject making up this beacon.
--
-- A NEW beacon is one tile, so `all` holds one object and `counter` is nil. A beacon
-- built by v0.66-v0.69 is still TWO tiles standing in somebody's base, so those are
-- recognised too and `counter`/`device` are filled -- see LEGACY_SPRITES. Everything
-- downstream works off `all` and `anchor` and does not care which shape it got.
function B.PartsOf(obj)
    local out = { all = {}, counter = nil, device = nil, station = nil, facing = "S" }
    if not obj then return out end

    local function consider(o)
        local info = SPRITE_INFO[spriteNameOf(o)]
        if not info then return end
        out.facing = info.facing
        out.all[#out.all + 1] = o
        if info.role == "counter" then out.counter = out.counter or o
        elseif info.role == "station" then out.station = out.station or o
        else out.device = out.device or o end
    end

    local sq
    pcall(function() sq = obj:getSquare() end)
    if not sq then return out end

    local function scan(s2)
        if not s2 then return end
        pcall(function()
            local objs = s2:getObjects()
            for i = 0, objs:size() - 1 do consider(objs:get(i)) end
        end)
    end
    scan(sq)
    pcall(function()
        local x, y, z = sq:getX(), sq:getY(), sq:getZ()
        local cell = getCell()
        scan(cell:getGridSquare(x + 1, y, z))
        scan(cell:getGridSquare(x - 1, y, z))
        scan(cell:getGridSquare(x, y + 1, z))
        scan(cell:getGridSquare(x, y - 1, z))
    end)
    return out
end

-- The tile a beacon is IDENTIFIED by -- the square its registry entry is keyed on.
--
-- For a one-tile beacon that is the object itself. For a legacy two-tile one it MUST
-- stay the counter, because that is the square v0.66-v0.69 wrote the entry under:
-- changing the rule would silently orphan the zones, stores and settings of every
-- beacon already standing in a save.
function B.AnchorOf(obj)
    local parts = B.PartsOf(obj)
    return parts.counter or parts.station or obj
end

-- Finds the beacon on a square, or nil.
function B.OnSquare(sq)
    if not sq then return nil end
    local found
    pcall(function()
        local objs = sq:getObjects()
        for i = 0, objs:size() - 1 do
            local o = objs:get(i)
            if B.IsBeacon(o) then found = o; return end
        end
    end)
    return found
end

-- ---------------------------------------------------------------------------
-- state
-- ---------------------------------------------------------------------------

-- Reads (and lazily initialises) a beacon's settings, stored on the object itself.
-- Also refreshes the coordinate registry, so simply looking at a beacon is enough
-- to make it findable later from anywhere.
function B.State(obj)
    if not obj then return nil end
    -- IT ASKS BEFORE IT BRANDS (v0.77.13). v0.77.9 fixed the sprite test that let counters
    -- in, but left the actual damage un-gated: this is the ONE place that writes tcBeacon,
    -- the write is a one-way door, and it happened to whatever it was handed. Every caller
    -- today reaches here through IsBeacon, so this changes nothing about correct use -- it
    -- means a future sloppy caller costs somebody a wrong menu entry for one frame instead
    -- of permanently converting their kitchen. Still-branded counters in an older save are
    -- healed by IsBeacon below rather than by anything here.
    --
    -- Every caller already handles nil (State could always return it -- the pcall bails on
    -- a nil modData), so refusing is safe at all fourteen call sites.
    if not B.IsBeacon(obj) then return nil end
    local st
    pcall(function()
        local md = obj:getModData()
        if not md then return end
        md.tcBeacon = true
        if type(md.tcBeaconState) ~= "table" then
            md.tcBeaconState = {
                stage    = 1,      -- 1..B.MAX_STAGE; upgraded in place
                welcome  = true,   -- broadcasting: survivors may arrive
                -- NO `zones` HERE (v0.75.33). This field was created for "Phase 2b" and
                -- never used: zones live on the SITE record in the registry, because a
                -- program deciding where to send someone must get an answer while the
                -- beacon's chunk is unloaded. An empty table on the OBJECT that looks
                -- like the zone store is how the Status diagnostic came to print
                -- zones=0 forever (fixed in v0.75.20) -- two plausible homes for one
                -- piece of state is the bug, not the wrong lookup.
                lastHour = nil,    -- world-age hour of the last arrival roll
            }
        end
        st = md.tcBeaconState
        -- A beacon built before stages existed has no stage field; treat it as 1.
        if type(st.stage) ~= "number" then st.stage = 1 end

        -- `capacity` is what the STATION can support -- derived from the stage,
        -- never stored, because two sources for one fact is how they drift apart.
        st.capacity = (B.STAGES[st.stage] or B.STAGES[1]).capacity

        -- `wanted` is what the PLAYER is ready for, and it is the one that governs
        -- arrivals. Being able to say "I can support three but I only want one
        -- right now" is the difference between a station you control and a tap you
        -- can only turn fully on. Clamped to the station's capacity so an upgrade
        -- never silently lowers it and a downgrade never leaves it impossible.
        if type(st.wanted) ~= "number" then st.wanted = st.capacity end
        if st.wanted > st.capacity then st.wanted = st.capacity end
        if st.wanted < 0 then st.wanted = 0 end

        -- maxHere is the EFFECTIVE limit everything else reads.
        st.maxHere = st.wanted

        -- Key off the ANCHOR (counter) tile: clicking either half must land on
        -- the same registry entry, or a two-tile beacon becomes two beacons.
        local anchor = B.AnchorOf(obj) or obj
        local sq
        pcall(function() sq = anchor:getSquare() end)
        if sq then
            local r = registry()
            local key = keyFor(sq:getX(), sq:getY(), sq:getZ())
            -- UPDATE the entry in place, NEVER replace it. The zones live on this
            -- table, and B.State is called on nearly every interaction -- assigning
            -- a fresh table here silently deleted every area the player had drawn,
            -- over and over.
            local site = r.sites[key]
            if not site then
                site = { zones = {} }
                r.sites[key] = site
            end
            if type(site.zones) ~= "table" then site.zones = {} end
            -- WHOSE BASE IS THIS? Sites had no owner field at all, so Base.SiteNear's
            -- only filter was "has a zone drawn" and a companion adopted whichever base
            -- was nearest -- in MP, quite possibly a stranger's (audit Cluster A).
            -- Stamped once, on first registration, and never overwritten: the beacon
            -- belongs to whoever put it there, not to whoever last walked past it.
            if site.owner == nil then
                local un
                pcall(function()
                    local p = getSpecificPlayer(0)
                    un = p and p:getUsername()
                end)
                if type(un) == "string" and un ~= "" then site.owner = un end
            end
            site.x, site.y, site.z = sq:getX(), sq:getY(), sq:getZ()
            site.welcome  = st.welcome
            site.stage    = st.stage
            site.capacity = st.capacity
            site.wanted   = st.wanted
            site.maxHere  = st.maxHere

            -- HOW MANY COMPANIONS THE OWNER ALREADY HAS, published for the server
            -- (v0.77.10). This is what makes arrivals work on a dedicated server at all.
            --
            -- The arrival check needs to know whether there is room, and it counts the
            -- ROSTER so that companions who are away or streamed out still occupy a slot.
            -- But the roster is a CLIENT store -- BanditsNPC.Persistence does not exist on a
            -- dedicated server -- so that check had no counter there and, quite correctly,
            -- refused to roll rather than ignore a cap the player had set. Correct, and it
            -- also meant NO ARRIVALS EVER on a dedicated server, which is the "the beacon
            -- says wait an hour and nobody ever comes" report. It looked random because it
            -- was universal on dedicated servers and absent everywhere else.
            --
            -- The site record already replicates (wireCopy sends every field), so the count
            -- rides along on a channel that exists. Same number the local check uses, so the
            -- two cannot disagree about capacity.
            pcall(function()
                local rc = BanditsNPC.Persistence and BanditsNPC.Persistence.RosterCount
                if rc then site.hereCount = rc() end
            end)

            -- REPLICATE (v0.75.11). Every field above is shared state; before this the
            -- write stayed on whichever client touched the beacon.
            pushSite(site.x, site.y, site.z)
        end
    end)
    return st
end

-- The registry entry for a specific beacon object, creating it if this is the
-- first time we have seen this beacon. Public because the window and the zone
-- tools both need it and neither should be reaching into the registry directly.
function B.SiteFor(obj)
    if not obj then return nil end
    B.State(obj)   -- ensures the entry exists and is current
    -- KEY OFF THE ANCHOR, exactly as B.State does when it WRITES the entry (:345).
    -- This read the clicked object's own square instead, so for any beacon whose clicked
    -- half is not its anchor the lookup missed the entry State had just created and the
    -- site came back nil -- no zones, no stores, no arrivals credited. Current beacons
    -- are one tile so the two agree, but a LEGACY two-tile beacon (still recognised on
    -- purpose, see LEGACY_SPRITES) is exactly the case where they do not (v0.75.16).
    local anchor = B.AnchorOf(obj) or obj
    local sq
    pcall(function() sq = anchor:getSquare() end)
    if not sq then return nil end
    return registry().sites[keyFor(sq:getX(), sq:getY(), sq:getZ())]
end

-- Every beacon we know about, as a list of {x, y, z, welcome, maxHere}.
-- Reads the REGISTRY, not the world, so it works for an unloaded base.
function B.AllSites()
    return BanditsNPC.BeaconRegistry.AllSites()
end

-- Drops a registry entry whose object is gone. Called when we look at a square
-- that should hold a beacon and does not -- a beacon can be picked up or destroyed,
-- and a stale entry would keep attracting arrivals to an empty field.
function B.ForgetSite(x, y, z)
    local r = registry()
    local k = keyFor(x, y, z)
    if r.sites[k] then
        r.sites[k] = nil
        pcall(function() BanditsNPC.BeaconRegistry.PushRemove(k) end)
        print("[BanditsNPC] Beacon at " .. k .. " is gone; removed from the registry.")
    end
end

-- Verifies every registered site whose chunk is currently loaded, and forgets the
-- ones whose beacon has been removed. Sites in unloaded chunks are left alone --
-- absence of a loaded object is not evidence the beacon is gone, exactly as with
-- companions.
function B.PruneSites()
    local r = registry()
    local dead = {}
    for k, site in pairs(r.sites) do
        local sq
        pcall(function() sq = getCell():getGridSquare(site.x, site.y, site.z) end)
        if sq and not B.OnSquare(sq) then dead[#dead + 1] = k end
    end
    for i = 1, #dead do
        r.sites[dead[i]] = nil
        print("[BanditsNPC] Beacon at " .. dead[i] .. " is gone; removed from the registry.")
    end
end

-- ---------------------------------------------------------------------------
-- queries used by the arrival logic
-- ---------------------------------------------------------------------------

-- The nearest broadcasting beacon to a point, with its distance, or nil.
function B.NearestWelcoming(x, y, z)
    return BanditsNPC.BeaconRegistry.NearestWelcoming(x, y, z)
end

-- Is there ANY beacon broadcasting anywhere? This is the gate on arrivals: with no
-- beacon, no survivors, which is the whole point of the mechanic.
function B.AnyWelcoming()
    return BanditsNPC.BeaconRegistry.AnyWelcoming()
end

-- ---------------------------------------------------------------------------
-- the interact key, for the proximity prompt
-- ---------------------------------------------------------------------------
--
-- READ, NOT ASSUMED. getCore():getKey("Interact") returns whatever the player has
-- bound (vanilla's own tutorial reads it the same way), so the prompt and the key
-- handler cannot disagree even on a remapped keyboard -- and the label shows the real
-- key rather than telling everybody "E" and being wrong for some of them.
function B.PromptKey()
    local k
    pcall(function() k = getCore():getKey("Interact") end)
    return k or Keyboard.KEY_E
end

function B.PromptKeyName()
    local n
    pcall(function() n = Keyboard.getKeyName(B.PromptKey()) end)
    if n and n ~= "" then return n end
    return "E"
end

-- ---------------------------------------------------------------------------
-- ARRIVAL PACING
-- ---------------------------------------------------------------------------
--
-- ONE TABLE, READ BY BOTH THE SPAWNER AND THE UI, so the number the window promises
-- and the number the spawner uses cannot drift.
--
-- `chance` is the per-in-game-hour percentage. `guarantee` is the hour count after
-- which the next check CANNOT fail. The guarantee is the point of this: a pure dice
-- roll can be unlucky for an arbitrarily long time, and an unlucky streak is
-- indistinguishable from a broken feature -- which is precisely the report that came
-- back from the first beacon test ("I set arrivals to Often and nobody came"). At
-- Often the roll alone averages under three hours; the guarantee only bites on a bad
-- run, so it costs nothing in normal play and removes the failure mode entirely.
--
-- Indexed by the WildSpawn enum: 1 = Off, 2 = Rare, 3 = Occasional, 4 = Often.
-- Shared with the server, which is what actually rolls arrivals. This is the one
-- LOAD-TIME reference to the registry (everything else resolves at call time), so it
-- carries its own fallback: shared/ loads before client/ in PZ, but a nil here would
-- turn every later B.ARRIVAL.chance read into an error, and this path cannot be tested
-- locally.
B.ARRIVAL = (BanditsNPC.BeaconRegistry and BanditsNPC.BeaconRegistry.ARRIVAL)
            or { chance = { 0, 8, 18, 35 }, guarantee = { 0, 36, 16, 6 } }

-- Hours a broadcasting station has been waiting with a free slot. Lives on the SITE
-- (global mod data), not on the object, because the hourly check has to work while
-- the beacon's chunk is unloaded -- same reason the registry exists at all.
function B.Waited(site)
    return BanditsNPC.BeaconRegistry.Waited(site)
end

function B.NoteArrival(site)
    return BanditsNPC.BeaconRegistry.NoteArrival(site)
end

-- Advances the counter and answers "does someone turn up this hour?". Kept HERE
-- rather than in the spawner so the window can explain the same state it produces.
function B.RollArrival(site)
    return BanditsNPC.BeaconRegistry.RollArrival(site)
end

-- One line of plain English for the Overview: what the station is doing and what
-- would have to change. Returns text plus a colour key ("good"/"warn"/"dim").
--
-- This exists because the first beacon test produced no arrivals and NOTHING said
-- why. The console had one message, printed once, at the very first hour before the
-- beacon was even built, and then latched silent forever. Every gate below was
-- already knowable at any moment; none of it was ever shown.
function B.ArrivalStatus(site, here)
    if not site then return BanditsNPC.T("UI_BN_Arr_Unknown", "No station data."), "dim" end

    local rate = BanditsNPC.Opt("WildSpawn", 2)
    if type(rate) ~= "number" then rate = 2 end
    if rate <= 1 then
        return BanditsNPC.T("UI_BN_Arr_Off",
            "Arrivals are switched off - turn them on in Settings."), "warn"
    end
    if not site.welcome then
        return BanditsNPC.T("UI_BN_Arr_Silent",
            "Nothing is being transmitted - nobody can hear this station."), "warn"
    end

    local want = site.wanted or 0
    if want <= 0 then
        return BanditsNPC.T("UI_BN_Arr_NoRoom",
            "You have asked for nobody - raise the number in Settings."), "warn"
    end
    if (here or 0) >= want then
        return BanditsNPC.T("UI_BN_Arr_Full",
            "Everyone you asked for is here. Raise the number to make room."), "dim"
    end

    -- WHAT THE SPAWNER ACTUALLY DECIDED, IF IT DECIDED ANYTHING (v0.77.13).
    --
    -- Everything above is what this side can work out for itself, and it was the whole
    -- status line -- so a station the hourly roll is refusing every hour still read
    -- "someone will answer within N hours", forever. That is the "it says wait an hour and
    -- nobody ever comes" report: not a broken spawner, a window describing a different
    -- system from the one running. BanditsNPCWildSpawn now records its refusal on the site
    -- record (which is shared ModData and reaches a dedicated-server client), and this
    -- reads it back.
    --
    -- Written as literal T() calls, one per reason, because the key scanner cannot see an
    -- indirect key -- a table of reason -> key would ship untranslatable.
    local why = site.arrWhy
    if why == "loose" then
        return BanditsNPC.T("UI_BN_Arr_Loose",
            "Survivors are already waiting nearby - recruit them and the station calls again."), "warn"
    elseif why == "noclan" then
        return BanditsNPC.T("UI_BN_Arr_NoClan",
            "No survivor group to draw from - check the Target Group sandbox option."), "warn"
    elseif why == "nocount" then
        return BanditsNPC.T("UI_BN_Arr_NoCount",
            "The server has no companion count for this station yet - it resumes shortly."), "warn"
    elseif why == "nositehere" then
        return BanditsNPC.T("UI_BN_Arr_Floor",
            "This station cannot be heard from where you are - it answers on its own floor."), "warn"
    elseif why == "noloaded" then
        -- The roll SUCCEEDED and only the geometry failed, so this one is genuinely good
        -- news with an instruction attached rather than a fault.
        return BanditsNPC.T("UI_BN_Arr_NoRoom2",
            "Someone is on their way - spend a little time at base so they can walk in."), "good"
    end

    local left = (B.ARRIVAL.guarantee[rate] or 0) - B.Waited(site)
    if left < 1 then left = 1 end
    return BanditsNPC.TF("UI_BN_Arr_Waiting",
        "Broadcasting - someone will answer within %1 hour(s).", left), "good"
end

-- ---------------------------------------------------------------------------
-- upgrading
-- ---------------------------------------------------------------------------

-- Written as three LITERAL T() calls on purpose. The key file is rebuilt by a
-- scanner that reads every translation call whose key and English text are both
-- written out at the call site -- an indirect call such as T(s.nameKey, s.nameEn)
-- is invisible to it, so those keys would never reach UI.json and could never be
-- translated, silently. Any string a player sees must be spelled out in full where
-- it is used. (Do not write an example call in a comment either: the scanner reads
-- comments too, and will happily add the example as a real key.)
function B.StageName(stage)
    if stage == 3 then return BanditsNPC.T("UI_BN_Beacon_Stage3", "Relay Station") end
    if stage == 2 then return BanditsNPC.T("UI_BN_Beacon_Stage2", "Field Beacon") end
    return BanditsNPC.T("UI_BN_Beacon_Stage1", "Makeshift Beacon")
end

-- Counts how many of `itemType` the player is carrying, recursively (so a stack
-- inside a bag counts). getNumberOfItem does not recurse, hence the manual walk
-- over getAllEvalRecurse -- the same idiom the give/inventory windows use.
local function countInInventory(player, itemType)
    local n = 0
    pcall(function()
        local items = player:getInventory():getAllEvalRecurse(function(it)
            return it ~= nil and it:getFullType() == itemType
        end)
        n = items and items:size() or 0
    end)
    return n
end

-- Returns true plus nil, or false plus a human-readable "what you still need".
function B.CanUpgrade(player, stage)
    local cost = B.UPGRADE_COST[stage]
    if not cost then return false, nil end
    local missing = {}
    for _, need in ipairs(cost) do
        local have = countInInventory(player, need.type)
        if have < need.count then
            -- getItemNameFromFullType is the vanilla helper for exactly this --
            -- ISEntityBuildMenu.lua:176 builds its "missing materials" tooltip the
            -- same way. (ScriptManager.instance:getItem(...):getDisplayName() also
            -- works but is two calls and one more thing to guard.)
            local label = need.type
            pcall(function()
                local n = getItemNameFromFullType(need.type)
                if n and n ~= "" then label = n end
            end)
            missing[#missing + 1] = string.format("%s %d/%d", label, have, need.count)
        end
    end
    if #missing > 0 then return false, table.concat(missing, ", ") end
    return true, nil
end

-- Consumes the cost, bumps the stage, and swaps the sprite in place.
--
-- setSpriteFromName is used rather than setSprite because setSprite has TWO
-- one-argument overloads (IsoSprite and String) and Kahlua dispatches overloads by
-- ARITY -- two same-arity overloads is exactly the ambiguity that produces a Java
-- dispatch error, and those bypass pcall.
-- The BUTTON. Checks it can be paid for, then queues the work -- it does not change
-- anything itself. Upgrading used to happen the instant you clicked, with no walk, no
-- animation and no time passing, which reads as a menu option rather than as building
-- something.
function B.DoUpgrade(obj, player)
    if not obj or not player then return end
    local st = B.State(obj)
    if not st then return end

    local nextStage = (st.stage or 1) + 1
    if nextStage > B.MAX_STAGE then return end

    local ok, missing = B.CanUpgrade(player, nextStage)
    if not ok then
        player:Say(BanditsNPC.TF("UI_BN_Beacon_NeedParts", "I still need: %1", missing or "?"))
        return
    end

    if not (BanditsNPCUpgradeBeaconAction and ISTimedActionQueue) then
        -- Timed actions unavailable for some reason: do it directly rather than
        -- leaving the button dead.
        B.ApplyUpgrade(obj, player)
        return
    end

    -- Walk there first, exactly as any build job does -- to a tile BESIDE it, not to
    -- the beacon's own square, which the beacon is standing on. Vanilla's own farming
    -- and hutch menus walk to an adjacent square for the same reason.
    -- DO NOT QUEUE A WALK TO WHERE HE ALREADY IS (v0.75.59). This queued one
    -- unconditionally, and clicking Upgrade while standing next to the beacon -- which is
    -- the normal way to click it -- threw a NullPointerException out of
    -- ISBaseTimedAction:begin -> StartAction, printing a full stack trace to the console
    -- on every upgrade (author's log, 6 Aug, 13:21:49; the upgrade itself then went
    -- through, so it looked like a crash that did nothing). Vanilla guards this the same
    -- way before queueing the walk: ISCampingMenu.lua:432 and luautils.walkAdj:106-109
    -- both return early when the player is already on or beside the target square.
    pcall(function()
        local sq = (B.AnchorOf(obj) or obj):getSquare()
        if not sq then return end
        local here = player:getCurrentSquare()
        if here and AdjacentFreeTileFinder.isTileOrAdjacent(here, sq) then return end
        local stand = AdjacentFreeTileFinder.Find(sq, player) or sq
        if stand and stand ~= here then
            ISTimedActionQueue.add(ISWalkToTimedAction:new(player, stand))
        end
    end)
    pcall(function()
        ISTimedActionQueue.add(BanditsNPCUpgradeBeaconAction:new(player, obj, 300))
    end)
end

-- The WORK. Called from the timed action when it completes, so nothing is consumed
-- and nothing changes if the player walks away mid-job.
function B.ApplyUpgrade(obj, player)
    if not obj or not player then return end
    local st = B.State(obj)
    if not st then return end

    local nextStage = (st.stage or 1) + 1
    if nextStage > B.MAX_STAGE then return end

    -- Checked AGAIN: several seconds have passed since the button, and the parts may
    -- have been dropped or used in the meantime.
    local ok, missing = B.CanUpgrade(player, nextStage)
    if not ok then
        player:Say(BanditsNPC.TF("UI_BN_Beacon_NeedParts", "I still need: %1", missing or "?"))
        return
    end

    -- CONSUME THROUGH THE SAME RECURSIVE VIEW WE COUNTED WITH (v0.75.6, audit Cluster E).
    -- This used to be a FindAndReturn loop, and FindAndReturn searches ONLY the top-level
    -- inventory -- ItemContainer marks recursion explicitly with a Recurse suffix and
    -- there is no FindAndReturnRecurse. CanUpgrade above counts with getAllEvalRecurse,
    -- so keeping the parts in a backpack -- which is how the game is normally played --
    -- passed the check, removed NOTHING, and bumped the stage anyway: stages 2 and 3 were
    -- free, silently, and only for players who bag their materials. Which is why it
    -- survived testing by anyone who keeps them loose.
    --
    -- Stations.TakeMaterials counts and takes through one recursive view and is atomic,
    -- so a shortfall here removes nothing and leaves the stage alone.
    local cost = {}
    for _, need in ipairs(B.UPGRADE_COST[nextStage]) do
        cost[need.type] = (cost[need.type] or 0) + need.count
    end
    local paid, short = false, nil
    if BanditsNPC.Stations and BanditsNPC.Stations.TakeMaterials then
        paid, short = BanditsNPC.Stations.TakeMaterials(player, cost)
    end
    if not paid then
        player:Say(BanditsNPC.TF("UI_BN_Beacon_NeedParts", "I still need: %1", short or missing or "?"))
        return
    end

    st.stage = nextStage
    st.capacity = B.STAGES[nextStage].capacity
    st.wanted = st.capacity   -- an upgrade opens the new slots up; the player can dial it back

    -- Repaint the anchor into the new stage's console, keeping the facing it was
    -- built at -- an upgrade must never rotate the beacon.
    --
    -- A LEGACY TWO-TILE BEACON IS CONVERTED HERE, and that is the migration path: the
    -- anchor (its counter) becomes the console and the loose radio half is taken away,
    -- so the first upgrade turns an old counter-and-handset into the new single-tile
    -- station instead of leaving a cupboard standing next to it forever.
    --
    -- setSpriteFromName, not setSprite: setSprite has TWO one-argument overloads
    -- (IsoSprite and String) and Kahlua dispatches by ARITY, so same-arity overloads
    -- are a dispatch error -- and those bypass pcall.
    local parts = B.PartsOf(obj)
    local anchor = parts.counter or parts.station or obj
    local facing = parts.facing or "S"
    pcall(function() anchor:setSpriteFromName(B.SpriteFor(nextStage, facing)) end)

    for i = 1, #parts.all do
        local part = parts.all[i]
        if part ~= anchor then
            pcall(function()
                local psq = part:getSquare()
                if psq then psq:transmitRemoveItemFromSquare(part) end
            end)
        end
    end
    pcall(function() anchor:getModData().tcBeacon = true end)
    pcall(function() anchor:transmitModData() end)

    B.State(obj)   -- refresh the registry with the new stage/maxHere
    player:Say(BanditsNPC.TF("UI_BN_Beacon_Upgraded", "Upgraded to %1.", B.StageName(nextStage)))
    print("[BanditsNPC] Beacon upgraded to stage " .. tostring(nextStage)
        .. " facing " .. tostring(parts.facing))
end

-- ---------------------------------------------------------------------------
-- removal
-- ---------------------------------------------------------------------------

-- Takes the beacon down: removes the object, drops its registry entry (and with it
-- every zone it owned), and -- if that was the last broadcasting beacon anywhere --
-- puts every companion back on Follow.
--
-- WHY THE FORCED FOLLOW. Stay, Guard, Relax at Base and every work assignment all
-- mean "be at a place". Remove the beacon and those places stop existing. A
-- companion left on Stay at a base you have abandoned waits at a post you will
-- never return to, which is indistinguishable from having lost her -- so moving the
-- base has to sweep everyone up rather than silently strand them. Set up a new
-- beacon and a new base zone and you can give the old orders again.
function B.Remove(obj, player)
    if not obj then return end

    -- OWNER ONLY (v0.75.23). Dismantling was open to anyone standing next to a beacon,
    -- and it does not just remove an object: ForgetSite drops the registry entry, taking
    -- the base area, every work zone and every assigned container with it, and every
    -- companion homed there loses her base. The server-side registry commands already
    -- refuse a foreign site (R.MayWrite), so without this the local half of the removal
    -- ran, the world object went, and the shared entry survived -- the worst of both.
    local site = B.SiteFor(obj)
    if site and BanditsNPC.BeaconRegistry then
        local un
        pcall(function()
            local p = player or getSpecificPlayer(0)
            un = p and p:getUsername()
        end)
        local key = BanditsNPC.BeaconRegistry.KeyFor(site.x, site.y, site.z or 0)
        if not BanditsNPC.BeaconRegistry.MayWrite(key, un) then
            pcall(function()
                local p = player or getSpecificPlayer(0)
                if p then
                    p:Say(BanditsNPC.T("UI_BN_Beacon_NotYours",
                        "This beacon belongs to someone else."))
                end
            end)
            return
        end
    end

    -- Forget the site by its ANCHOR square, which is the key State() wrote under.
    local parts = B.PartsOf(obj)
    local anchor = parts.counter or parts.station or obj
    local sq
    pcall(function() sq = anchor:getSquare() end)
    if not sq then return end
    B.ForgetSite(sq:getX(), sq:getY(), sq:getZ())

    -- EVERY tile of it comes down. A new beacon is one tile; a legacy one is two, and
    -- removing only the clicked half would leave the other standing, still carrying a
    -- beacon sprite -- so IsBeacon would keep matching it and the player would have a
    -- phantom half-station they could not remove.
    --
    -- transmitRemoveItemFromSquare is the whole removal (it takes the object off the
    -- square AND replicates that); vanilla removes placed objects exactly this way
    -- (ISWorldObjectContextMenu.lua:1952, ISRemoveItemTool.lua:105).
    local halves = parts.all
    if #halves == 0 then halves = { obj } end
    for i = 1, #halves do
        local part = halves[i]
        pcall(function()
            local psq = part:getSquare()
            if psq then psq:transmitRemoveItemFromSquare(part) end
        end)
    end

    -- Cancel a zone cursor still pointing at the site we just deleted.
    if BanditsNPCZoneCursor and BanditsNPCZoneCursor.Cancel then
        pcall(function() BanditsNPCZoneCursor.Cancel(player and player:getPlayerNum() or 0) end)
    end

    local stillWelcoming = B.AnyWelcoming()
    if not stillWelcoming and BanditsNPC.Persistence and BanditsNPC.Persistence.ForceAllFollow then
        BanditsNPC.Persistence.ForceAllFollow("beacon removed")
        if player then
            player:Say(BanditsNPC.T("UI_BN_Beacon_RemovedFollow",
                "Beacon's down. Everyone stays with me until I set up again."))
        end
    elseif player then
        player:Say(BanditsNPC.T("UI_BN_Beacon_Removed", "Beacon dismantled."))
    end

    print("[BanditsNPC] Beacon removed; another broadcasting beacon exists: "
        .. tostring(stillWelcoming))
end

-- ---------------------------------------------------------------------------
-- zone tools
-- ---------------------------------------------------------------------------

local function beginZone(obj, args)
    local player = args and args.player
    local kind = args and args.kind
    if not (player and kind) then return end
    local st = B.State(obj)
    if not st then return end

    local sq
    pcall(function() sq = obj:getSquare() end)
    if not sq then return end
    local site = registry().sites[keyFor(sq:getX(), sq:getY(), sq:getZ())]
    if not site then return end

    if not BanditsNPCZoneCursor then
        print("[BanditsNPC] Zone cursor class not built yet (OnGameStart has not run).")
        return
    end
    BanditsNPCZoneCursor.Begin(player, site, kind)
    player:Say(BanditsNPC.T("UI_BN_Zone_Prompt", "Drag out the area on the ground."))
end

local function clearZones(obj, args)
    local player = args and args.player
    local st = B.State(obj)
    if not st then return end
    local sq
    pcall(function() sq = obj:getSquare() end)
    if not sq then return end
    local site = registry().sites[keyFor(sq:getX(), sq:getY(), sq:getZ())]
    if not site then return end
    local n = BanditsNPC.Zones.Clear(site, nil)
    BanditsNPC.Zones.ShowFor(3000)
    if player then
        player:Say(BanditsNPC.TF("UI_BN_Zone_Cleared", "Cleared %1 area(s).", n))
    end
end

-- ---------------------------------------------------------------------------
-- context menu
-- ---------------------------------------------------------------------------

local function countCompanions()
    local n = 0
    if BanditsNPC.Persistence and BanditsNPC.Persistence.RosterCount then
        n = BanditsNPC.Persistence.RosterCount()
    end
    return n
end

-- ===========================================================================
-- PREPARE FOR UNINSTALL (v0.77.13)
-- ===========================================================================
--
-- "Removing the mod bricks the save" (Da Enemy) and "it seems to corrupt the server if you
-- try to remove it afterwards -- DO NOT USE IT" (Silhouette). Both are right, and the
-- cause is one line of Bandits with no nil guard on it:
--
--     BanditUpdate.lua:1893   local res = ZombiePrograms[program.name][program.stage](bandit)
--
-- Every companion persists her program name in her brain, and every one of ours is a name
-- only THIS mod defines -- NPCCompanion, NPCRelax, NPCRoutine, NPCDowned and the rest.
-- Uninstall True Companions while Bandits stays, and each saved companion indexes a nil
-- table on every single zombie update. It cannot be fixed after the fact, because the fix
-- would have to ship in the mod that is no longer there.
--
-- So it has to be offered BEFORE. This hands every companion back to Bandits' own
-- ZombiePrograms.Companion -- a real program in Bandits, with the Prepare stage ours were
-- modelled on -- and empties the roster, after which the mod can be removed cleanly.
--
-- NOT debug-gated. Someone uninstalling a mod has no reason to have turned debug mode on,
-- and a safety valve nobody can find is not a safety valve.
--
-- Reaches LOADED companions only, which is why the confirmation says to do it at base with
-- everyone gathered: an unloaded companion's brain lives in the Bandit cluster keyed by an
-- id we have no way to enumerate from here. Walking your base once first is a fair price
-- and is far better than the current answer, which is that there is no price that works.
function B.PrepareUninstall(player)
    local n = 0
    pcall(function()
        local zl = getCell() and getCell():getZombieList()
        if not zl then return end
        for i = 0, zl:size() - 1 do
            local z = zl:get(i)
            pcall(function()
                if not (z and z:getVariableBoolean("Bandit")) then return end
                local brain = BanditBrain.Get(z)
                local pn = brain and brain.program and brain.program.name
                if type(pn) ~= "string" or pn:sub(1, 3) ~= "NPC" then return end
                brain.program   = { name = "Companion", stage = "Prepare" }
                brain.recruited = false
                brain.master    = false
                brain.masterName = false
                BanditBrain.Update(z, brain)
                if Bandit and Bandit.ForceSyncPart then
                    Bandit.ForceSyncPart(z, {
                        id = brain.id, program = brain.program, recruited = false,
                        master = false, masterName = false,
                    })
                end
                n = n + 1
            end)
        end
    end)
    -- The roster is what would otherwise rebuild them; with the mod gone nothing reads it,
    -- but clearing it means a REINSTALL does not resurrect an army either.
    pcall(function()
        if BanditsNPC.Persistence and BanditsNPC.Persistence.ClearRoster then
            BanditsNPC.Persistence.ClearRoster()
        end
    end)
    if player then
        player:Say(BanditsNPC.TF("UI_BN_Uninstall_Done",
            "%1 handed back. It is safe to remove the mod now.", n))
    end
    print("[BanditsNPC] prepare-for-uninstall: " .. tostring(n)
        .. " companion(s) returned to Bandits' own Companion program, roster cleared.")
    return n
end

local function onToggleWelcome(obj)
    local st = B.State(obj)
    if not st then return end
    st.welcome = not st.welcome
    B.State(obj)   -- push the change back into the registry
    local player = getSpecificPlayer(0)
    if player then
        if st.welcome then
            player:Say(BanditsNPC.T("UI_BN_Beacon_On", "Broadcasting. Anyone out there is welcome here."))
        else
            player:Say(BanditsNPC.T("UI_BN_Beacon_Off", "Broadcast off. No one new will come."))
        end
    end
end

local function onStatus(obj)
    local st = B.State(obj)
    if not st then return end
    print("[BanditsNPC] Beacon status: broadcasting=" .. tostring(st.welcome)
        .. " maxHere=" .. tostring(st.maxHere)
        .. " companions=" .. tostring(countCompanions())
        -- zones live on the SITE record, not on the object state -- st.zones was
        -- always nil so this printed zones=0 no matter how many were drawn, which
        -- made the diagnostic actively misleading when chasing a zone problem
        -- (v0.75.20).
        .. " zones=" .. tostring(#((B.SiteFor(obj) or {}).zones or {})))
    local player = getSpecificPlayer(0)
    if player then
        -- gsub returns TWO values (string, count); wrapped in parens so only the
        -- string reaches Say.
        player:Say(BanditsNPC.TF("UI_BN_Beacon_Status", "The set is warm. %1 of us here.", countCompanions()))
    end
end

-- ---------------------------------------------------------------------------
-- THE RIGHT-CLICK "BUILD A SURVIVORS BEACON" ENTRY IS GONE (v0.77.13)
-- ---------------------------------------------------------------------------
--
-- Three Workshop reporters asked for it in two days, in the same words: it is not an
-- action anyone performs twice, and it sat in the ground context menu for the whole
-- game. Discoverability was the argument for it and discoverability is not worth a
-- permanent entry on every square. Both buildables live in the Build menu under
-- Furniture, which is where the recipes always were.
--
-- Deleting it also deletes a bug rather than moving one. The right-click path armed the
-- cursor through ISEntityBuildMenu.onBuildEntity, which -- unlike the Build panel --
-- never sets `blockBuild`, so ISBuildIsoEntity:isValid fell through to haveMaterial,
-- and that only reads legacy need:/use: modData keys an entity cursor never has. Every
-- ghost was green regardless of inventory. We patched around it with our own
-- haveMaterial on the drag; the Build panel needs none of that, because it sets
-- blockBuild from logic:canPerformCurrentRecipe() (ISBuildPanel.lua:330-336, honoured at
-- ISBuildIsoEntity.lua:436). The remaining door is the one that was always correct.

function B.OnFillWorldObjectContextMenu(player, context, worldobjects, test)
    if test and ISWorldObjectContextMenu.Test then return true end

    local beacon
    for _, o in ipairs(worldobjects) do
        if B.IsBeacon(o) then beacon = o; break end
        local sq = o:getSquare()
        if sq then
            local b = B.OnSquare(sq)
            if b then beacon = b; break end
        end
    end
    if not beacon then return false end

    if test then return ISWorldObjectContextMenu.setTest() end

    local st = B.State(beacon)
    local playerObj = getSpecificPlayer(player)

    -- The CONTROL PANEL is the front door -- a single top-level option that opens
    -- the window. The submenu below stays as the shortcut for people who would
    -- rather not open a window for a one-click toggle; both drive the same
    -- functions, so there is no second implementation to drift.
    context:addOption(BanditsNPC.T("UI_BN_Beacon_Open", "Survivors Beacon..."), beacon,
        function(b) BanditsNPCBeaconWindow.OpenFor(b) end)

    -- Menu title carries the stage, so the player can see what they have without
    -- opening anything: "Survivors Beacon (Makeshift Beacon)".
    local title = (BanditsNPC.T("UI_BN_Beacon_Menu", "Survivors Beacon")
                   .. " (" .. B.StageName(st and st.stage or 1) .. ")")
    local option = context:addOption(title, nil, nil)
    local sub = ISContextMenu:getNew(context)
    context:addSubMenu(option, sub)

    -- UPGRADE, offered on the object itself rather than as a separate build entry.
    local stage = (st and st.stage) or 1
    if stage < B.MAX_STAGE then
        local nextStage = stage + 1
        local label = BanditsNPC.TF("UI_BN_Beacon_UpgradeTo", "Upgrade to %1", B.StageName(nextStage))
        local up = sub:addOption(label, beacon, B.DoUpgrade, playerObj)
        local can, missing = B.CanUpgrade(playerObj, nextStage)
        if not can then
            -- Greyed out WITH the reason. A disabled option that does not say why
            -- reads as a bug; this lists exactly what is still short.
            up.notAvailable = true
            local tip = ISToolTip:new()
            tip:initialise()
            tip:setName(label)
            tip.description = BanditsNPC.TF("UI_BN_Beacon_NeedParts", "I still need: %1", missing or "?")
            up.toolTip = tip
        end
    end

    if st and st.welcome then
        sub:addOption(BanditsNPC.T("UI_BN_Beacon_StopBroadcast", "Stop broadcasting"),
                      beacon, onToggleWelcome)
    else
        sub:addOption(BanditsNPC.T("UI_BN_Beacon_StartBroadcast", "Broadcast for survivors"),
                      beacon, onToggleWelcome)
    end
    sub:addOption(BanditsNPC.T("UI_BN_Beacon_StatusOpt", "Status"), beacon, onStatus)

    -- ===== areas =====
    local Zs = BanditsNPC.Zones
    local areaOpt = sub:addOption(BanditsNPC.T("UI_BN_Beacon_Areas", "Areas"), nil, nil)
    local areaSub = ISContextMenu:getNew(sub)
    sub:addSubMenu(areaOpt, areaSub)

    -- The label says which of the two things it will do, because since v0.77.13 drawing
    -- again ADDS to the base rather than replacing it -- and "Mark out base area" on a
    -- beacon that already has one is exactly the wording that made Shabak expect a
    -- replacement. Two literal T() calls, not one computed key: the key scanner cannot
    -- see an indirect key and it would ship untranslatable.
    -- B.SiteFor, not a raw registry lookup off the clicked square: it keys off the ANCHOR,
    -- which is the only thing that finds the entry for a legacy two-tile beacon.
    local hasBase = false
    pcall(function() hasBase = BanditsNPC.Zones.Base(B.SiteFor(beacon)) ~= nil end)
    if hasBase then
        areaSub:addOption(BanditsNPC.T("UI_BN_Zone_AddBase", "Add to base area"),
                          beacon, beginZone, { player = playerObj, kind = Zs.KIND.BASE })
    else
        areaSub:addOption(BanditsNPC.T("UI_BN_Zone_SetBase", "Mark out base area"),
                          beacon, beginZone, { player = playerObj, kind = Zs.KIND.BASE })
    end

    areaSub:addOption(BanditsNPC.T("UI_BN_Zone_Show", "Show areas"), nil, function()
        BanditsNPC.Zones.ShowFor(12000)
    end)
    -- Now the ONLY thing that discards a drawn base, so it says so plainly.
    areaSub:addOption(BanditsNPC.T("UI_BN_Zone_ClearAll", "Clear the base area"),
                      beacon, clearZones, { player = playerObj })

    -- ===== take it down =====
    -- Confirmed, because it un-homes every companion. ISModalDialog is the vanilla
    -- yes/no prompt.
    -- Confirmed, and the wording has to carry the one limitation that matters: it can only
    -- reach companions who are LOADED, so it says to gather everyone first.
    sub:addOption(BanditsNPC.T("UI_BN_Uninstall_Opt", "Prepare to remove this mod..."), nil, function()
        local text = BanditsNPC.T("UI_BN_Uninstall_Confirm",
            "Hand every companion here back to the Bandits mod, so True Companions can be"
            .. " removed without breaking the save? Do this at your base with everyone"
            .. " gathered - only companions loaded around you can be handed back.")
        local modal = ISModalDialog:new(0, 0, 340, 170, text, true, nil, function(_, button)
            if button.internal == "YES" then B.PrepareUninstall(playerObj) end
        end)
        modal:initialise()
        modal:addToUIManager()
        modal:setAlwaysOnTop(true)
    end)

    sub:addOption(BanditsNPC.T("UI_BN_Beacon_RemoveOpt", "Dismantle the beacon"), nil, function()
        local text = BanditsNPC.T("UI_BN_Beacon_RemoveConfirm",
            "Take the beacon down? Everyone will follow you until you set up a new one.")
        local modal = ISModalDialog:new(0, 0, 300, 150, text, true, nil, function(_, button)
            if button.internal == "YES" then B.Remove(beacon, playerObj) end
        end)
        modal:initialise()
        modal:addToUIManager()
        modal:setAlwaysOnTop(true)
    end)

    return true
end

Events.OnFillWorldObjectContextMenu.Add(B.OnFillWorldObjectContextMenu)

-- Keep the registry honest. Cheap: it only inspects sites whose chunk is loaded.
Events.EveryTenMinutes.Add(function() pcall(B.PruneSites) end)

-- ---------------------------------------------------------------------------
-- flag a beacon AT BIRTH, and un-flag what should never have been branded
-- ---------------------------------------------------------------------------
--
-- The beacon is built by a vanilla entity recipe, so the mod gets no callback at the
-- moment of construction and has always had to recognise the result by its sprite. That
-- is the gap that made sprite-matching load-bearing in the first place. OnObjectAdded
-- closes it: it fires for objects added to the world AT RUNTIME, which is exactly a
-- player build and never a map-loaded counter. Vanilla uses it the same way (see
-- RainCollectorBarrel.lua:27).
Events.OnObjectAdded.Add(function(obj)
    pcall(function()
        if not obj then return end
        local spr = obj:getSprite()
        local name = spr and spr:getName()
        -- Only sprites the mod itself builds. A legacy sprite appearing at runtime is
        -- somebody placing a kitchen counter, which is not our business.
        if not (name and SPRITE_INFO[name] and not LEGACY_SPRITES[name]) then return end
        obj:getModData().tcBeacon = true
        if obj.transmitModData then obj:transmitModData() end
    end)
end)

-- (The repair for already-branded counters lives inside B.IsBeacon, not on a chunk-load
-- sweep: LoadGridsquare fires for every square streamed in and would pay a loop over every
-- object in the world to find a handful of stray flags, while IsBeacon is asked about
-- exactly the objects that matter and is already doing the lookup.)
