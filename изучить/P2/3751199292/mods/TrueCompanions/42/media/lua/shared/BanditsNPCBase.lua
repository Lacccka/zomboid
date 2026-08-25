--
-- True Companions - Home base: what is in it, and who uses what (v0.67)
--
-- WHAT PROBLEM THIS SOLVES. The need routines have worked for several versions and
-- almost nobody has seen them, because every one of them is gated on a spot the
-- player assigned BY HAND: BanditsNPC.Routine.MaybeStart only fires when
-- brain.spots[key] exists, so a companion with no assigned bed never sleeps, one with
-- no assigned food container never eats, and the whole of base life is invisible
-- unless you knew to open the Orders tab and place five cursors per companion.
--
-- Now that the player draws a base AREA, the furniture inside it can simply be found.
-- A defined base means beds, chairs, TVs, food containers and bathrooms are known
-- without anyone assigning anything, and the manual spots stay exactly as they were:
-- an OVERRIDE, checked first, for when you want a specific companion in a specific
-- bed.
--
-- WHERE THE SCAN LIVES. On the beacon's registry entry in global mod data, next to
-- the zones, for the same reason the zones are there: a companion deciding where to
-- sleep must get an answer while the base chunk is unloaded, and an object you cannot
-- reach is an object you cannot ask. Coordinates are stored, never object references.
--
-- HOW FURNITURE IS RECOGNISED. Sprite properties, using only forms vanilla itself
-- uses in Lua:
--   bed        getProperties():has(IsoFlagType.bed)      -- modded beds set it too
--   chair      getProperties():has("chairN"|"chairS"|...) -- 293 vanilla chair tiles
--   television instanceof(o, "IsoTelevision")
--   food       a container that currently holds something edible
--   bathroom   a water source (see IsWater -- B42 moved this off getWaterAmount)
-- Note that chairs ALSO carry the bed flag (they are "badBed" to the engine, which is
-- how sleeping in a chair works), so the chair test has to run FIRST and a chair must
-- be excluded from the bed list -- otherwise every companion in the house would try
-- to spend the night sitting up in an armchair.
--

BanditsNPC = BanditsNPC or {}
BanditsNPC.Base = BanditsNPC.Base or {}
local Base = BanditsNPC.Base

-- Spot keys this module can discover. Deliberately a SUBSET of the eight the Orders
-- tab offers: "weapon", "healing" and "reading" containers are choices about which
-- crate is which, and guessing them from a shelf of books would be wrong more often
-- than right. Those stay manual.
-- KEYS are the spots the ROUTINE system needs (bed, food, bathroom) plus the ones the
-- player can assign by hand. PASTIME_KEYS are found by the same scan but are only ever
-- used to give a companion at home something to do -- nothing depends on them existing.
Base.KEYS = { "bed", "chair", "tv", "food", "bathroom" }
Base.PASTIME_KEYS = { "stove", "washer", "window", "bookshelf" }

local MAX_PER_KIND = 8        -- more than a base ever needs; keeps the saved table small
-- Hard ceiling on one scan, whatever the player drew. Raised from 3000 on 4 Aug when
-- the scan started covering every floor of the base rather than only the one it was
-- drawn on: the budget is spent per TILE PER FLOOR, and at 3000 a decent-sized house
-- ran out partway up the stairs -- which would have looked exactly like the
-- single-floor bug it was meant to fix. Levels with no square cost a nil lookup.
local MAX_TILES    = 9000
local RESCAN_MS    = 90000    -- furniture moves; re-look every 90s of real time
-- How far a seat may be from a television and still count as "watching it" (tiles).
-- 6 covers a normal living room without pairing a set with a chair in the next room;
-- tvFacing() scans +/-4, so anything further would sit her facing nothing anyway.
local TV_SEAT_RANGE = 6

-- ---------------------------------------------------------------------------
-- classifiers -- the ONE definition of each, shared with the manual spot cursors
-- ---------------------------------------------------------------------------

-- THE PREDICATES ARE PER-OBJECT, NOT PER-SQUARE, and that is a performance decision,
-- not a stylistic one. The area scan asks all five questions about every tile, and a
-- per-square form means five separate walks of the square's object list behind five
-- separate pcalls -- on a 3000-tile budget that is ~15000 pcall-wrapped loops in one
-- tick, which is a visible hitch. Written this way, Base.Classify walks each square
-- ONCE inside ONE pcall and tests every object against all five.
--
-- The square-level wrappers below stay, because the cursor validators want exactly one
-- question about exactly one tile and run every render frame.

local CHAIR_PROPS = { "chairN", "chairS", "chairE", "chairW" }

local P = {}

-- Something you can sit on. The string form of has() is the one vanilla uses for
-- non-flag properties (ISZoneDisplay.lua:356).
function P.chair(o)
    if not (o and o.getProperties) then return false end
    local props = o:getProperties()
    if not props then return false end
    for i = 1, #CHAIR_PROPS do
        if props:has(CHAIR_PROPS[i]) then return true end
    end
    return false
end

-- Carries the engine's own sleepable flag. Modded beds set it too -- it is what makes
-- them sleepable for players. Chairs carry it as well (they are "badBed" to the
-- engine, which is how sleeping in an armchair works), so this is the test for "you
-- could sleep here", not "this is a bed".
function P.sleepable(o)
    return o and o.getSprite and o.getProperties and o:getSprite()
           and o:getProperties():has(IsoFlagType.bed) and true or false
end

-- ACTUALLY A BED, not merely sleepable.
--
-- The chairN/S/E/W test in P.chair is not enough on its own: plenty of soft furniture
-- carries the sleepable flag WITHOUT carrying a sit property, so it fell through to the
-- bed list. The author's companion was sent to sleep and lay down on a "Fancy yellow
-- striped chair" in the kitchen as if it were a bed (4 Aug).
--
-- Vanilla's own discriminator is the BedType tile property, read at
-- ISWorldObjectContextMenu.lua:2966 and set across CustomTileProps.lua -- 87 entries,
-- every one of them either "badChair" or "averageChair". Anything whose BedType ends in
-- Chair is somewhere to sit, whatever else it claims.
--
-- Vanilla defaults a missing BedType to "averageBed", so the sprite-name net catches the
-- rest: a sofa with no properties at all is still not a bed.
local SEAT_WORDS = { "chair", "sofa", "couch", "armchair", "bench", "stool", "seat" }

-- CustomName is checked against a WIDER list than the sprite name: it is a human label, so
-- "Table" and "Toilet" show up there as plainly as "Chair" does, and none of the three is
-- somewhere to spend the night.
local NOT_A_BED_NAMES = { "chair", "sofa", "couch", "armchair", "bench", "stool", "seat",
                          "seating", "table", "toilet" }

local function nameSaysNotABed(text, list)
    if type(text) ~= "string" or text == "" then return false end
    text = text:lower()
    for i = 1, #list do
        if text:find(list[i], 1, true) then return true end
    end
    return false
end

function P.bed(o)
    if not P.sleepable(o) then return false end
    if P.chair(o) then return false end

    -- THE `BedType` LAYER IS DEAD AND IS KEPT ONLY AS A FUTURE-PROOF (v0.75.13). Vanilla's
    -- chair BedType entries in CustomTileProps.lua are all COMMENTED OUT ("Not work now!
    -- Need add this ... to tileset config"), and the complete set of BedType values in the
    -- shipped tile data is badBed (433), averageBed (216) and goodBed (92) -- not one
    -- *Chair among them. So this test has never once fired. It costs a property read and
    -- starts working by itself the day vanilla uncomments those lines.
    local bt
    pcall(function()
        local props = o:getProperties()
        bt = props and props.get and props:get("BedType")
    end)
    if bt and tostring(bt):lower():find("chair", 1, true) then return false end

    -- CUSTOMNAME IS THE LAYER THAT ACTUALLY WORKS, and it is what fixes the reported bug
    -- ("she lay down on a Fancy yellow striped chair as if it were a bed"). Measured from
    -- the tile data: 504 tiles carry the sleepable flag WITHOUT a sit property, and 394 of
    -- those have sprite names the SEAT_WORDS net does not catch. Reading their CustomName
    -- shows why that net was never enough -- most of the 394 are genuine (Bed 112, sleeping
    -- bags 88, Tent 32, Shelter 24, hay, mattresses) but ~70 are named, in the game's own
    -- words, Chair (24), Seat (24), Seating (8), Stool (7) or Bench (4), plus Table (32)
    -- and Toilet (8). The sprite SHEET is named for where the furniture lives
    -- (location_restaurant_*, location_entertainment_theatre_*); the CustomName is named
    -- for what it IS, which is why it discriminates and the sheet name cannot.
    local cn
    pcall(function()
        local props = o:getProperties()
        cn = props and props.get and props:get("CustomName")
    end)
    if nameSaysNotABed(cn, NOT_A_BED_NAMES) then return false end

    local name
    pcall(function()
        local spr = o:getSprite()
        name = spr and spr:getName()
    end)
    if nameSaysNotABed(name, SEAT_WORDS) then return false end
    return true
end

function P.television(o)
    return o ~= nil and instanceof(o, "IsoTelevision")
end

-- ===== PASTIMES (4 Aug) =====
--
-- "when relaxing in base I followed one companion to see what she does and she only sat
-- in a chair all day in the kitchen and did nothing else." She had one chair and three
-- ambient idles, so that is all there was to see.
--
-- Each of these is a real piece of furniture the base scan can find, so what a companion
-- does at home depends on what the player actually built. A house with a cooker gets
-- someone cooking in it; a house without one does not.
--
-- All instanceof, or a container type, because those are what the engine itself uses to
-- decide these are ovens and washing machines -- no sprite-name guessing.
function P.stove(o)
    return o ~= nil and (instanceof(o, "IsoStove") or instanceof(o, "IsoFireplace"))
end

function P.washer(o)
    return o ~= nil and (instanceof(o, "IsoClothingWasher") or instanceof(o, "IsoClothingDryer"))
end

function P.window(o)
    return o ~= nil and instanceof(o, "IsoWindow")
end

-- A bookcase is a container whose type says so. Container types are set per tile by the
-- game's own definitions, so this catches every shelf the map places without listing
-- sprites.
-- CONTAINER TYPES, VERIFIED AGAINST THE TILE DATA (v0.75.13). This was
-- { shelves, bookcase, bookshelf, desk } and two of those four do not exist: enumerating
-- every `container = X` in the three tile-definition files gives 63 distinct types, and
-- neither "bookcase" nor "bookshelf" is among them. The feature still worked -- shelves
-- (192 tiles) and desk (62) carried it -- but the dead keys hid the real miss:
-- **shelvesmag** (24 tiles) is the magazine shelf, i.e. literally the reading material,
-- and it was not matched. metal_shelves (16) added for the same reason.
local READ_CONTAINERS = { shelves = true, shelvesmag = true, metal_shelves = true, desk = true }
function P.bookshelf(o)
    local t
    pcall(function()
        local c = o and o.getContainer and o:getContainer()
        t = c and c:getType()
    end)
    return t ~= nil and READ_CONTAINERS[tostring(t):lower()] == true
end

function P.container(o)
    return o and o.getContainer and o:getContainer() and true or false
end

-- A container with something edible in it right now.
function P.food(o)
    local cont = o and o.getContainer and o:getContainer()
    if not cont then return false end
    local list = ArrayList.new()
    cont:getAllEvalRecurse(function(it) return it.IsFood and it:IsFood() end, list)
    return list:size() > 0
end

-- A water source (sink/bath/toilet/barrel...).
--
-- B42's fluid update MOVED world-object water off getWaterAmount(): the live APIs are
-- getFluidAmount() (ISWashYourself:125/:157, piped sources report 10000) and hasWater()
-- (ISCleanBandage:7). The old getWaterAmount-only probe silently failed the existence
-- guard on every object -> every sink/tub/toilet read as "not a bathroom" (the wave of
-- reports, 6-9 Jul). Probe all three plus the waterPiped sprite flag so dry-but-plumbed
-- fixtures count too -- the wash routine is cosmetic anyway.
--
-- MUST be has(IsoFlagType.waterPiped): that is the only form vanilla ever uses
-- (ISMoveableSpriteProps:1394/:2335). The string overload Is("waterPiped") was an
-- UNPROVEN engine call, and Java dispatch errors BYPASS pcall -- it was the v0.43
-- "Set Bathroom error loop", an error screen alternating with doRDoubleClick every
-- frame.
function P.water(o)
    if not o then return false end
    if o.hasWater and o:hasWater() then return true end
    if o.getFluidAmount then
        local f = o:getFluidAmount()
        if f and f > 0 then return true end
    end
    if o.getWaterAmount then
        local w = o:getWaterAmount()
        if w and w > 0 then return true end
    end
    if o.getSprite and o.getProperties and o:getSprite() then
        return o:getProperties():has(IsoFlagType.waterPiped) and true or false
    end
    return false
end

Base.P = P

-- One walk of one square, answering every question at once. Returns a table of flags,
-- or nil if the square could not be read.
function Base.Classify(square)
    if not square then return nil end
    local out
    pcall(function()
        local objs = square:getObjects()
        local r = { chair = false, sleepable = false, bed = false, television = false,
                    container = false, food = false, water = false,
                    stove = false, washer = false, window = false, bookshelf = false }
        for i = 0, objs:size() - 1 do
            local o = objs:get(i)
            if not r.chair      and P.chair(o)      then r.chair = true end
            if not r.sleepable  and P.sleepable(o)  then r.sleepable = true end
            if not r.bed        and P.bed(o)        then r.bed = true end
            if not r.television and P.television(o) then r.television = true end
            if not r.container  and P.container(o)  then r.container = true end
            if not r.food       and P.food(o)       then r.food = true end
            if not r.water      and P.water(o)      then r.water = true end
            if not r.stove      and P.stove(o)      then r.stove = true end
            if not r.washer     and P.washer(o)     then r.washer = true end
            if not r.window     and P.window(o)     then r.window = true end
            if not r.bookshelf  and P.bookshelf(o)  then r.bookshelf = true end
        end
        out = r
    end)
    return out
end

-- ONE QUESTION, ONE PREDICATE (v0.75.31). This called the full Classify and threw ten of
-- the eleven answers away. That contradicted the design note at the top of this file,
-- which justifies keeping these wrappers on the grounds that "the cursor validators want
-- exactly one question about exactly one tile and run every render frame" -- they do run
-- every frame, but they were not asking one question. The expensive predicate is P.food,
-- which allocates an ArrayList and walks a container's FULL NESTED CONTENTS, so hovering
-- a placement cursor over a packed crate ran that recursion once per rendered frame to
-- answer "does this tile have a container at all".
local ONE = {
    chair = P.chair, sleepable = P.sleepable, bed = P.bed, television = P.television,
    container = P.container, food = P.food, water = P.water,
    stove = P.stove, washer = P.washer, window = P.window, bookshelf = P.bookshelf,
}

local function ask(square, key)
    local pred = ONE[key]
    if not pred or not square then return false end
    local hit = false
    pcall(function()
        local objs = square:getObjects()
        for i = 0, objs:size() - 1 do
            if pred(objs:get(i)) then hit = true; return end
        end
    end)
    return hit
end

function Base.IsChair(square)     return ask(square, "chair") end
function Base.IsSleepable(square) return ask(square, "sleepable") end
function Base.IsTelevision(square) return ask(square, "television") end
function Base.HasContainer(square) return ask(square, "container") end
function Base.HasFood(square)     return ask(square, "food") end
function Base.IsWater(square)     return ask(square, "water") end

-- True if the square holds an actual BED. Chairs are excluded -- see P.sleepable, and
-- the note at the top of the file: without this, every armchair in the house would be
-- filed as somewhere to spend the night.
--
-- The manual "Set bed" cursor deliberately uses IsSleepable instead, because a player
-- pointing at an armchair and saying "sleep there" has said what they meant.
-- 4 Aug: this used to be `c.sleepable and not c.chair`, which let anything soft with
-- the sleepable flag and no sit property through -- see P.bed for what that put a
-- companion to sleep on.
function Base.IsBed(square)
    local c = Base.Classify(square)
    return c ~= nil and c.bed == true
end

-- FIX(BUG-017): "can she actually stand here", for the MOVEMENT cursors.
-- MOVED HERE from BanditsNPCInteract.lua by TASK-010. It began as a local next to
-- OrderStay; three more callers in two other files now need the same answer, and the
-- note at Interact.lua:1106-1112 already says square tests are defined ONCE here and
-- delegated to. Two copies of a rule that must agree is how they stop agreeing.
--
-- WHY THIS EXISTS. Ordering a companion onto a cupboard tile froze her: she never took
-- a step and only the watchdog teleport freed her. It is a LIVELOCK and the mechanism
-- is in Bandits, not ours (BUG-018) -- ZAMove.onWorking returns true for
-- BehaviorResult.Failed exactly as it does for Succeeded (ZAMove.lua:34-39), so "no
-- route exists" is reported as "arrived". The task completes, the program sees it is
-- still too far away and re-issues, forever. We cannot edit Bandits, so the fix is to
-- never ISSUE an unreachable destination.
--
-- UNLIKE EVERY OTHER TEST IN THIS SECTION, this one does not go through ask()/Classify.
-- Those inspect the OBJECTS on a square to answer "what furniture is this". This asks
-- the engine a different question -- "is this square passable" -- so it calls isFree
-- directly and deliberately.
--
-- WHY isFree(false). It is the game's own test, not one invented here: vanilla uses it
-- for the same question (ISWorldObjectContextMenu.lua:2214, :2254) and our own
-- furniture-unstick uses it (Guardian.lua). Semantics are jar-verified, not assumed:
-- IsoGridSquare.isFree(boolean) returns false when the square has `solid` or
-- `solidtrans`, or IsoObjectType.tree, or LACKS `solidfloor`. A cupboard contributes
-- `solid`; a midair tile lacks `solidfloor`.
--
-- THE ARGUMENT IS `false` DELIBERATELY. It gates the moving-objects test (bytecode:
-- `if arg and !movingObjects.isEmpty() return false`). Passing true would redden any
-- tile with a character or item standing on it, so the cursor would refuse tiles that
-- are perfectly fine a second later. STATIC blockers only.
--
-- KNOWN QUIRK, ACCEPTED: stairs force isFree TRUE (the stair checks run last in the
-- bytecode and overwrite the earlier result), so a stair square passes. Standing on
-- stairs is odd but reachable, so it cannot livelock -- leaving it permissive beats
-- inventing a second test. See BUG-019 for why that contradicts an older comment.
function Base.IsStandable(square)
    return square ~= nil and square:isFree(false) == true
end

-- The opts table every DESTINATION cursor passes to BanditsNPCTileCursor.Begin.
-- Shared so the callers cannot drift apart; invalid tiles render red and the click is
-- refused (TileCursor.lua:83-84, :95).
--
-- NOT FOR EVERY CURSOR -- only the ones that pick somewhere she must WALK TO AND STAND
-- ON. Deliberately NOT used by:
--   Interact.lua:1193 BeginSetSpot   -- already validated by purpose (isBedSquare,
--                                       hasContainerSquare, isWaterSquare). Those
--                                       tiles ARE furniture; standability would
--                                       reject every valid bed.
--   Production.lua:466 BeginPlace    -- places an object, never a Move destination.
--   Production.lua:483 SelectWorkstation -- targets the occupied workstation tile ON
--                                       PURPOSE; this predicate would reject every
--                                       valid choice. See the comment there.
Base.STAND_OPTS = { validate = Base.IsStandable }

-- ---------------------------------------------------------------------------
-- which base does this companion belong to?
-- ---------------------------------------------------------------------------

-- The base zone of a site, or nil.
function Base.Zone(site)
    if not (site and BanditsNPC.Zones) then return nil end
    return BanditsNPC.Zones.Base(site)
end

-- The nearest site that actually HAS a base area drawn, to a world position.
-- A beacon with no base area is a radio, not a home, and must not claim anyone.
-- Is this base one the local player may use as a home? Sites are stamped with the
-- username of whoever registered the beacon (see Beacon.lua). A site with NO owner is
-- accepted: that is either single player or a base built before v0.75.5, and refusing
-- those would strand every companion attached to them.
local function siteUsable(site)
    local owner = site and site.owner
    if type(owner) ~= "string" or owner == "" then return true end
    local un
    pcall(function()
        local p = getSpecificPlayer(0)
        un = p and p:getUsername()
    end)
    if type(un) ~= "string" or un == "" then return true end
    return un == owner
end

function Base.SiteNear(x, y, z)
    -- The SHARED registry first: BanditsNPC.Beacon is a client/ module and does not exist
    -- on a dedicated server, which made every base-derived decision nil there (v0.75.11).
    local all
    if BanditsNPC.BeaconRegistry and BanditsNPC.BeaconRegistry.AllSites then
        all = BanditsNPC.BeaconRegistry.AllSites()
    elseif BanditsNPC.Beacon and BanditsNPC.Beacon.AllSites then
        all = BanditsNPC.Beacon.AllSites()
    end
    if not all then return nil end
    local best, bestD
    for _, site in ipairs(all) do
        if Base.Zone(site) and siteUsable(site) then
            local d = BanditUtils.DistTo(x or 0, y or 0, site.x or 0, site.y or 0)
            if bestD == nil or d < bestD then best, bestD = site, d end
        end
    end
    return best
end

-- The home site of a companion. Falls back to the player's position when the
-- companion has none yet, so a survivor who has just walked in still gets a home.
function Base.SiteFor(bandit)
    if bandit then
        local ok, site = pcall(function()
            return Base.SiteNear(bandit:getX(), bandit:getY(), bandit:getZ())
        end)
        if ok and site then return site end
    end
    local p = getSpecificPlayer(0)
    if p then return Base.SiteNear(p:getX(), p:getY(), p:getZ()) end
    return nil
end

-- ---------------------------------------------------------------------------
-- the scan
-- ---------------------------------------------------------------------------

local function pushSpot(list, x, y, z)
    if #list >= MAX_PER_KIND then return end
    list[#list + 1] = { x = x, y = y, z = z }
end

-- Walks every tile of the base area and files what it finds. Returns the table it
-- stored on the site, or nil if there is no base area to walk.
--
-- BOUNDED ON PURPOSE. A base can legitimately be sixty tiles across, and this touches
-- every object on every tile, so it is capped at MAX_TILES and only re-run every
-- RESCAN_MS. Anything the cap cuts off is at the far end of a very large base, which
-- is not where anyone's bed is.
function Base.Rescan(site)
    local zone = Base.Zone(site)
    if not zone then return nil end

    local found = {}
    for _, k in ipairs(Base.KEYS) do found[k] = {} end
    for _, k in ipairs(Base.PASTIME_KEYS) do found[k] = {} end

    local budget = MAX_TILES
    local cell = getCell()
    if not cell then return nil end

    -- EVERY FLOOR THE BASE COVERS, not just the one it was drawn on. A bed upstairs is
    -- still a bed in this house; see Z.ZLevels for why the span is bounded.
    local levels = (BanditsNPC.Zones.ZLevels and BanditsNPC.Zones.ZLevels(zone))
                   or { zone.z or 0 }

    for i = 1, #zone.parts do
        local p = zone.parts[i]
        for x = p.x1, p.x2 do
            for y = p.y1, p.y2 do
                if budget <= 0 then break end
                for li = 1, #levels do
                    local z = levels[li]
                    budget = budget - 1
                    local sq
                    pcall(function() sq = cell:getGridSquare(x, y, z) end)
                    -- A z with no square at all is a floor this building does not have,
                    -- so the extra levels cost a nil lookup and nothing more.
                    local c = sq and Base.Classify(sq)
                    if c then
                        -- Chair WINS over bed: a chair also carries the engine's bed
                        -- flag, so filing on the bed flag first would put every armchair
                        -- in the house on the list of places to spend the night.
                        if c.chair then
                            pushSpot(found.chair, x, y, z)
                        elseif c.bed then
                            pushSpot(found.bed, x, y, z)
                        end
                        if c.television then pushSpot(found.tv, x, y, z) end
                        if c.food then pushSpot(found.food, x, y, z) end
                        if c.water then pushSpot(found.bathroom, x, y, z) end
                        if c.stove then pushSpot(found.stove, x, y, z) end
                        if c.washer then pushSpot(found.washer, x, y, z) end
                        if c.window then pushSpot(found.window, x, y, z) end
                        if c.bookshelf then pushSpot(found.bookshelf, x, y, z) end
                    end
                end
            end
            if budget <= 0 then break end
        end
        if budget <= 0 then break end
    end

    -- A "TV" SPOT IS A SEAT FACING THE SET, NOT THE SET (v0.75.57, author test: "for tv
    -- watching she stood inside the tv"). The scan files the television's own tile above,
    -- and both consumers -- the Relax pastime and the boredom routine -- then treat that
    -- tile as somewhere to SIT, so she was pinned inside the cabinet. The television tile
    -- is still what the scan can find; turning it into a place to watch from is this pass.
    --
    -- Nearest chair on the same floor within TV_SEAT_RANGE, deduped, because two sets in
    -- one lounge should not produce two entries for the same armchair. No chair in range
    -- means no TV pastime at all -- correct, and better than sitting on the appliance.
    -- Facing is not decided here: tvFacing() re-derives it from the seat at sit time.
    local seats, seen = {}, {}
    for _, tv in ipairs(found.tv) do
        local best, bestD
        for _, ch in ipairs(found.chair) do
            if (ch.z or 0) == (tv.z or 0) then
                local dx, dy = ch.x - tv.x, ch.y - tv.y
                local d = dx * dx + dy * dy
                if d <= TV_SEAT_RANGE * TV_SEAT_RANGE and (not bestD or d < bestD) then
                    bestD, best = d, ch
                end
            end
        end
        if best then
            local key = best.x .. ":" .. best.y .. ":" .. (best.z or 0)
            if not seen[key] then
                seen[key] = true
                seats[#seats + 1] = { x = best.x, y = best.y, z = best.z }
            end
        end
    end
    found.tv = seats

    site.autoSpots = found
    site.autoSpotsAt = (getTimestampMs and getTimestampMs()) or 0
    return found
end

-- Cached accessor. Never rescans from a hot path more than once per RESCAN_MS.
function Base.Spots(site)
    if not site then return nil end
    if not Base.Zone(site) then
        site.autoSpots = nil
        return nil
    end
    local now = (getTimestampMs and getTimestampMs()) or 0
    local at = site.autoSpotsAt
    if type(site.autoSpots) == "table" and type(at) == "number"
       and now >= at and now - at < RESCAN_MS then
        return site.autoSpots
    end
    local ok, res = pcall(Base.Rescan, site)
    return ok and res or site.autoSpots
end

-- Throws away the cache. Called when the base area changes, because everything in it
-- was found by walking an area that no longer exists.
function Base.Forget(site)
    if not site then return end
    site.autoSpots = nil
    site.autoSpotsAt = nil
end

-- ---------------------------------------------------------------------------
-- resolving a spot for one companion
-- ---------------------------------------------------------------------------

-- Stable per-companion index into a list. Two companions sharing a base must not
-- both march to the same bed, and the answer must be the SAME on every tick or she
-- would change her mind halfway across the room. Derived from the id, so it survives
-- a save and needs nothing stored.
-- v0.75.8 (audit Cluster F): the comment above promises two things and the hash only
-- delivered one. `seed % n` is STABLE -- the same companion picks the same spot every
-- tick and across saves, which is the important half -- but it is not an ALLOCATION:
-- two companions collide whenever their seeds are congruent mod n, which with two
-- companions and two beds is a coin flip. And bed/chair/bathroom are exactly the scarce
-- things where sharing is visible.
--
-- Now: rank this companion among the OTHER companions that share the base, by a stable
-- key, and index by that rank. Same stability (the ordering is by uid, not by distance
-- or load order) but distinct spots while there are enough to go round -- and it
-- degrades to the old modulo once there are more companions than spots.
local function stableKey(brain)
    return tostring((brain and (brain.npcUid or brain.id)) or "0")
end

local function pick(list, brain)
    local n = #list
    if n == 0 then return nil end
    if n == 1 then return list[1] end

    local me = stableKey(brain)
    local rank = 0
    local counted = false
    pcall(function()
        local cache = BanditZombie and BanditZombie.CacheLightB
        if not cache then return end
        for _, z in pairs(cache) do
            local b = z.brain
            if b and b.recruited and BanditsNPC.IsMine and BanditsNPC.IsMine(b) then
                local k = stableKey(b)
                if k ~= me and k < me then rank = rank + 1 end
                counted = true
            end
        end
    end)
    if not counted then
        -- no roster visible (early load): fall back to the stable hash
        local seed = 0
        for c = 1, #me do seed = (seed * 31 + string.byte(me, c)) % 100003 end
        return list[(seed % n) + 1]
    end
    return list[(rank % n) + 1]
end

-- Where should THIS companion go for THIS need?
--   1. the spot the player assigned to her -- always wins, that is what assigning is
--   2. otherwise, a stable pick from what the scan found inside her base area
-- Returns {x, y, z} or nil.
function Base.Resolve(brain, key, bandit)
    if brain and brain.spots and brain.spots[key] then return brain.spots[key] end

    local site = Base.SiteFor(bandit)
    if not site then return nil end
    local spots = Base.Spots(site)
    if not (spots and spots[key]) then return nil end
    return pick(spots[key], brain)
end

-- Does this companion's base have anything at all filed under `key`?
function Base.Has(brain, key, bandit)
    return Base.Resolve(brain, key, bandit) ~= nil
end

-- A place to stand inside the base area -- the anchor for relaxing when the player
-- has not picked a tile by hand. Prefers indoors, because a base area drawn over a
-- house is a house.
function Base.Anchor(site)
    local zone = Base.Zone(site)
    if not zone then return nil end
    for _ = 1, 20 do
        local sq = BanditsNPC.Zones.RandomFreeSquare(zone)
        if sq then
            local indoors = false
            pcall(function() indoors = sq:getRoom() ~= nil end)
            if indoors then
                return { x = sq:getX(), y = sq:getY(), z = sq:getZ() }
            end
        end
    end
    local sq = BanditsNPC.Zones.RandomFreeSquare(zone)
    if sq then return { x = sq:getX(), y = sq:getY(), z = sq:getZ() } end
    return nil
end
