--
-- True Companions - Base zones (v0.75.76)
--
-- The player drags a rectangle on the ground to say "this is my base". Companions then
-- respect that boundary instead of each program inventing its own geography.
--
--
-- WHY THIS EXISTS AT ALL. The brain accumulated NINE separate positional fields
-- that all mean roughly "where should she be" -- stayPos, relaxPos, guardA, guardB,
-- guardTarget, workstation, spots, anchor, wanderGoal -- because every program
-- invented its own. Zones replace that with one place the player can see and edit.
--
-- WHERE ZONES LIVE. On the beacon's registry entry in GlobalModData, NOT on the
-- beacon's IsoObject. Same reasoning as the companion roster: a program deciding
-- where to send someone must work when the beacon's chunk is not loaded, and an
-- object you cannot reach is an object you cannot ask. The world object is only
-- an anchor and an identity; its configuration lives in the notes.
--
-- RENDERING. addAreaHighlightForPlayer is a public static global (javap-confirmed)
-- and is what vanilla's own zone tools use (ISBuildingRoomsEditor_ToolAddRect.lua:53,
-- ISAddDesignationAnimalZoneUI.lua:271). Two things about it that are easy to get
-- wrong: x2/y2 are EXCLUSIVE (hence the +1s), and it is IMMEDIATE MODE -- the
-- highlight exists only for the frame you call it in, so it must be re-issued every
-- frame from a UI-draw phase.
--

BanditsNPC = BanditsNPC or {}
BanditsNPC.Zones = BanditsNPC.Zones or {}
local Z = BanditsNPC.Zones

-- Zone kinds. INTERNAL KEYS -- stored, compared, never displayed. Labels come from
-- Z.KindName, which is keyed separately so a translator can never change behaviour.
-- ONE KIND. The only area a companion has any use for is the base she lives in.
-- The table is kept rather than folded into a bare string so stored zones, the
-- beacon UI and the draw code all speak the same language, and so another kind
-- would be one line.
Z.KIND = {
    BASE = "base",
}

-- The base area's draw colour. Faint on purpose: it is a large fill the player is looking
-- THROUGH at the world, and highlights add where pieces of the same area overlap.
--
-- Drawn as a flat fill, edge included -- there is no separate, brighter perimeter pass.
-- v0.66 drew the outline at up to 0.55 alpha over an interior at 0.30 and the result read
-- as a bright frame around a washed-out middle instead of as one marked-out area.
Z.COLOR = {
    base = { r = 0.95, g = 0.95, b = 1.00, a = 0.14 },
}

-- Sanity limits. A drag across half the map would be both useless and a
-- performance problem for anything that iterates the area.
Z.MIN_SIDE = 2
Z.MAX_SIDE = 60


function Z.KindName(kind)
    return BanditsNPC.T("UI_BN_Zone_Base", "Base area")
end

-- ---------------------------------------------------------------------------
-- storage
-- ---------------------------------------------------------------------------

-- All zones belonging to a beacon site (the registry entry from BanditsNPCBeacon).
local function zonesOf(site)
    if not site then return nil end
    if type(site.zones) ~= "table" then site.zones = {} end
    return site.zones
end

-- A zone is a LIST OF RECTANGLES, not one rectangle.
--
-- Real bases are not squares. A house with an L-shaped footprint, a yard round two
-- sides, a garage off one corner -- none of that is expressible as a single rect,
-- and forcing one means either cutting off part of the building or swallowing the
-- street. So the player drags as many pieces as they need and clicks Finish, and
-- every containment test is "inside ANY part".
--
--   zone = { kind = <KIND>, z = <level>, parts = { {x1,y1,x2,y2}, ... } }
--
-- Base is still one-per-beacon: "where my base is" cannot sensibly be two separate
-- places, and allowing it would make containment ambiguous. It can be any SHAPE,
-- it just cannot be two disjoint bases.
--
-- ===========================================================================
-- DRAWING AGAIN ADDS, IT DOES NOT REPLACE (v0.77.13)
-- ===========================================================================
--
-- "You can't add a new zone without deleting the old one. When you draw an additional
-- room, the previously drawn zones disappear." (Shabak, 10 Aug.) Correct, and the cause
-- was right here: a second BASE removed the first. The multi-rectangle design above was
-- already the answer to L-shaped bases -- but only WITHIN one drawing session, so the
-- moment you clicked Finish the shape was frozen and the only way to extend it was to
-- redraw the entire base from scratch.
--
-- Appending instead costs nothing structurally: a zone was always "inside ANY part", so
-- more parts is the case it was built for. The one-base-per-beacon rule is untouched --
-- there is still exactly one BASE zone, it simply grows. Starting over is "Clear all",
-- which already existed and is now the only thing that discards work, which is the right
-- way round: adding a room should not be able to destroy the rest of your base.
local MAX_PARTS = 64   -- Contains() is O(parts) and runs from program ticks
function Z.Set(site, kind, parts, z)
    local zones = zonesOf(site)
    if not zones or type(parts) ~= "table" or #parts == 0 then return nil end

    local norm = {}
    for i = 1, #parts do
        local p = parts[i]
        norm[#norm + 1] = {
            x1 = math.min(p.x1, p.x2), y1 = math.min(p.y1, p.y2),
            x2 = math.max(p.x1, p.x2), y2 = math.max(p.y1, p.y2),
        }
    end

    local zone = { kind = kind, z = math.floor(z or 0), parts = norm }

    if kind == Z.KIND.BASE then
        -- The furniture scan is a CACHE of what walking the old shape found, so it has to
        -- go either way -- on a replace because the old house is not hers any more, and on
        -- an add because the new room's beds and chairs are not in it yet. Forget() only
        -- drops site.autoSpots/autoSpotsAt; the next query rebuilds it over the new shape.
        if BanditsNPC.Base then BanditsNPC.Base.Forget(site) end

        local existing = Z.Base(site)
        if existing and type(existing.parts) == "table" then
            if #existing.parts + #norm > MAX_PARTS then return nil end
            for i = 1, #norm do existing.parts[#existing.parts + 1] = norm[i] end
            -- The BASE kind spans one floor down and three up from zone.z, so taking the
            -- LOWER of the two can only ever widen the span -- a piece added in a basement
            -- brings the whole base's reference down to it rather than leaving that piece
            -- outside its own zone. (Ceiling: parts three storeys apart still cannot both
            -- be covered; splitting z per part is the fix if anyone ever needs it.)
            existing.z = math.min(existing.z or 0, zone.z)
            pcall(function() BanditsNPC.BeaconRegistry.PushSite(site) end)
            return existing
        end
    end
    zones[#zones + 1] = zone
    -- zones live ON the site record, so a zone edit is a site change (v0.75.11)
    pcall(function() BanditsNPC.BeaconRegistry.PushSite(site) end)
    return zone
end

-- Total tile count across all parts (overlaps counted twice -- it is a rough size
-- readout for the UI, not a survey).
function Z.TileCount(zone)
    if not zone or not zone.parts then return 0 end
    local n = 0
    for i = 1, #zone.parts do
        local p = zone.parts[i]
        n = n + (p.x2 - p.x1 + 1) * (p.y2 - p.y1 + 1)
    end
    return n
end

-- Removes every zone of a kind. Passing nil clears the lot.
function Z.Clear(site, kind)
    local zones = zonesOf(site)
    if not zones then return 0 end
    local n = 0
    for i = #zones, 1, -1 do
        if kind == nil or zones[i].kind == kind then
            if zones[i].kind == Z.KIND.BASE and BanditsNPC.Base then
                BanditsNPC.Base.Forget(site)
            end
            table.remove(zones, i)
            n = n + 1
        end
    end
    pcall(function() BanditsNPC.BeaconRegistry.PushSite(site) end)
    return n
end

function Z.Base(site)
    local zones = zonesOf(site)
    if not zones then return nil end
    for i = 1, #zones do
        if zones[i].kind == Z.KIND.BASE then return zones[i] end
    end
    return nil
end

function Z.OfKind(site, kind)
    local out = {}
    local zones = zonesOf(site)
    if not zones then return out end
    for i = 1, #zones do
        if zones[i].kind == kind then out[#out + 1] = zones[i] end
    end
    return out
end

-- ---------------------------------------------------------------------------
-- queries
-- ---------------------------------------------------------------------------

-- Containment on FLOAT coordinates, because every caller has a character position
-- rather than a tile. Modelled on DesignationZoneAnimal.getZoneF, which vanilla
-- provides for exactly this reason. x2/y2 are INCLUSIVE tile indices here, so the
-- test extends to the far edge of the far tile.
-- A BASE IS A BUILDING, AND BUILDINGS HAVE FLOORS.
--
-- Every zone used to be one z level, tested with abs(z - zone.z) >= 1. Draw a base over
-- a house from the ground floor and the bedrooms upstairs were OUTSIDE it: no bed was
-- ever found there ("assign a bed spot first" with a bed one storey up), and a companion
-- who did go up was out of bounds (author, 4 Aug -- "our zoning ignores the floors above
-- and below which needs to be included").
--
-- The BASE kind spans floors, which is the whole reason the span logic exists: a house is
-- not one storey.
--
-- The span is deliberately not "all of z". It reaches one floor DOWN for a basement and
-- three UP, which covers every residential building in the game and stops a base drawn
-- at ground level from claiming the top of a tower block it happens to sit beside.
Z.FLOOR_DOWN = 1
Z.FLOOR_UP   = 3

-- The z levels a zone covers, low to high.
function Z.ZLevels(zone)
    local base = (zone and zone.z) or 0
    if not (zone and zone.kind == Z.KIND.BASE) then return { base } end
    local out = {}
    for z = math.max(0, base - Z.FLOOR_DOWN), math.min(7, base + Z.FLOOR_UP) do
        out[#out + 1] = z
    end
    return out
end

local function zInZone(zone, z)
    if z == nil then return true end
    z = math.floor(z)
    local base = zone.z or 0
    if zone.kind ~= Z.KIND.BASE then return math.abs(z - base) < 1 end
    return z >= (base - Z.FLOOR_DOWN) and z <= (base + Z.FLOOR_UP)
end

function Z.Contains(zone, x, y, z)
    if not (zone and zone.parts) then return false end
    if not zInZone(zone, z) then return false end
    for i = 1, #zone.parts do
        local p = zone.parts[i]
        if x >= p.x1 and x < (p.x2 + 1) and y >= p.y1 and y < (p.y2 + 1) then
            return true
        end
    end
    return false
end

-- A random standable tile inside a zone, or nil. Used to send a companion "into" a
-- base area without stacking everyone on one tile.
--
-- Picks a PART first, weighted by nothing in particular, then a tile inside it.
-- Bounded attempts rather than an exhaustive scan: an area can be large and this
-- runs from a program tick.
function Z.RandomFreeSquare(zone)
    if not (zone and zone.parts and #zone.parts > 0) then return nil end
    for _ = 1, 30 do
        local p = zone.parts[ZombRand(#zone.parts) + 1]
        local x = p.x1 + ZombRand(p.x2 - p.x1 + 1)
        local y = p.y1 + ZombRand(p.y2 - p.y1 + 1)
        -- EVERY FLOOR THE ZONE COVERS, not just the one it was drawn on (v0.75.50).
        -- Containment already spans floors -- zInZone and Z.ZLevels exist precisely for
        -- that, and the furniture scan uses them -- but this always resolved at zone.z,
        -- so a companion could be INSIDE a multi-floor base upstairs and never be sent
        -- anywhere upstairs. That half-wiring is what produced the NPCRelax destination
        -- bug (fixed separately in v0.75.8); this is the other half.
        --
        -- A level with no square costs a nil lookup and the attempt is simply retried,
        -- which is the same trade Base.Rescan already makes.
        local levels = (Z.ZLevels and Z.ZLevels(zone)) or { zone.z or 0 }
        local z = levels[ZombRand(#levels) + 1] or zone.z or 0
        local sq
        pcall(function() sq = getCell():getGridSquare(x, y, z) end)
        if sq and BanditsNPC.Nav.IsSafeSquare(sq) then return sq end
    end
    return nil
end

-- Rewrites a list of (possibly overlapping) rectangles as the FEWEST equivalent
-- non-overlapping rectangles, covering exactly the same tiles.
--
-- WHY THE COUNT MATTERS, AND NOT JUST THE OVERLAP.
--
-- The engine OUTLINES EVERY RECTANGLE IT IS GIVEN. FBORenderAreaHighlights carries a
-- `private final boolean outline` and a renderOutline(AreaHighlight) that runs per
-- highlight, and addAreaHighlightForPlayer takes no flag to turn it off. So the number
-- of rectangles submitted IS the number of outlines drawn: a zone chopped into six
-- horizontal strips is six outlined boxes, which is the reported "lines going through
-- the middle of my whole zone" -- and the outline colour is the engine's, not ours,
-- which is why the border looked like a different colour from the fill.
--
-- Overlap removal alone did not fix that, because strips still have edges. So this
-- does two passes:
--   1. SCANLINE -- cut into horizontal bands at every distinct y edge and merge the
--      x-intervals in each band. Removes all overlap (the highlight BLENDS, so two
--      pieces sharing tiles come out twice as bright).
--   2. COALESCE -- glue vertically adjacent bands that have identical x extents back
--      into one taller rectangle.
--
-- Pass 2 is what makes an ordinary rectangular base, however many drags it took, come
-- out as ONE rectangle with ONE outline round the outside. A genuinely L-shaped area
-- still needs two, and there is no way around that short of not using this API.
--
-- O(n^2) in the number of pieces, which is a handful.
function Z.Merge(parts)
    if type(parts) ~= "table" or #parts <= 1 then return parts or {} end

    local cuts, seen = {}, {}
    for i = 1, #parts do
        for _, y in ipairs({ parts[i].y1, parts[i].y2 + 1 }) do
            if not seen[y] then seen[y] = true; cuts[#cuts + 1] = y end
        end
    end
    table.sort(cuts)

    local out = {}
    for c = 1, #cuts - 1 do
        local ya, yb = cuts[c], cuts[c + 1] - 1
        local spans = {}
        for i = 1, #parts do
            local p = parts[i]
            if p.y1 <= ya and p.y2 >= yb then
                spans[#spans + 1] = { p.x1, p.x2 }
            end
        end
        table.sort(spans, function(a, b) return a[1] < b[1] end)
        local curA, curB
        for i = 1, #spans do
            local s = spans[i]
            if curA == nil then
                curA, curB = s[1], s[2]
            elseif s[1] <= curB + 1 then          -- overlapping OR touching
                if s[2] > curB then curB = s[2] end
            else
                out[#out + 1] = { x1 = curA, y1 = ya, x2 = curB, y2 = yb }
                curA, curB = s[1], s[2]
            end
        end
        if curA then out[#out + 1] = { x1 = curA, y1 = ya, x2 = curB, y2 = yb } end
    end

    -- ----- pass 2: coalesce vertically -----
    --
    -- Bands were produced in ascending y, so a rectangle can only ever be glued to one
    -- already in `merged` with the same x extents whose bottom edge touches this one's
    -- top. Repeated until nothing more joins, because gluing A to B can make B able to
    -- take C.
    local merged = {}
    for i = 1, #out do
        local r = out[i]
        local joined = false
        for j = 1, #merged do
            local m = merged[j]
            if m.x1 == r.x1 and m.x2 == r.x2 and m.y2 + 1 == r.y1 then
                m.y2 = r.y2
                joined = true
                break
            end
        end
        if not joined then
            merged[#merged + 1] = { x1 = r.x1, y1 = r.y1, x2 = r.x2, y2 = r.y2 }
        end
    end
    return merged
end

-- Removes one specific area. Identity, not index: the Areas list can be rebuilt
-- between drawing a row and clicking its Remove button.
function Z.Remove(site, zone)
    local zones = zonesOf(site)
    if not (zones and zone) then return false end
    for i = #zones, 1, -1 do
        if zones[i] == zone then
            if zone.kind == Z.KIND.BASE and BanditsNPC.Base then
                BanditsNPC.Base.Forget(site)
            end
            table.remove(zones, i)
            return true
        end
    end
    pcall(function() BanditsNPC.BeaconRegistry.PushSite(site) end)
    return false
end

-- "Base area 2" -- the ordinal is only added when there IS more than one of that kind, so
-- the usual single base stays plain "Base area".
function Z.Label(site, zone)
    if not zone then return "" end
    local name = Z.KindName(zone.kind)
    local same = Z.OfKind(site, zone.kind)
    if #same <= 1 then return name end
    for i = 1, #same do
        if same[i] == zone then
            return BanditsNPC.TF("UI_BN_Zone_Nth", "%1 %2", name, i)
        end
    end
    return name
end

-- ---------------------------------------------------------------------------
-- rendering
-- ---------------------------------------------------------------------------

-- Buffered draw calls, flushed once per frame in the UI phase.
--
-- addAreaHighlightForPlayer must be called from a UI-draw phase, but the cursor's
-- render() runs in the world-object phase. Buffering bridges the two. The sticky
-- window keeps the last frame's calls alive briefly so the highlight does not
-- strobe on a frame where nothing queued anything.
local queue = {}
local lastQueue = {}
local lastQueueMs = 0
local STICKY_MS = 180

local function queueRect(playerNum, x1, y1, x2, y2, z, color, alpha)
    local a = alpha or (color and color.a) or 0.16
    if a <= 0 then return end
    queue[#queue + 1] = {
        p = playerNum or 0, x1 = x1, y1 = y1, x2 = x2, y2 = y2, z = z,
        r = (color and color.r) or 1, g = (color and color.g) or 1, b = (color and color.b) or 1, a = a,
    }
end

-- Solid fill over a tile range. x2/y2 here are INCLUSIVE; the +1s convert to the
-- engine's exclusive convention.
function Z.DrawRect(playerNum, x1, y1, x2, y2, z, color, alpha)
    queueRect(playerNum, math.min(x1, x2), math.min(y1, y2),
              math.max(x1, x2) + 1, math.max(y1, y2) + 1, z, color, alpha)
end

-- One flat fill per piece, whatever the kind. Deliberately no border variant: see
-- the note on Z.COLOR. Overlapping PIECES of the same area double their alpha where
-- they meet, so pieces are normalised into non-overlapping strips first -- otherwise
-- an L drawn as two overlapping rectangles shows a bright bar down the join, which
-- looks exactly like a seam the player is meant to fix.
local function drawParts(playerNum, parts, z, col, alpha)
    for i = 1, #parts do
        local p = parts[i]
        Z.DrawRect(playerNum, p.x1, p.y1, p.x2, p.y2, z, col, alpha)
    end
end

function Z.DrawZone(playerNum, zone, highlight)
    if not (zone and zone.parts) then return end
    local col = Z.COLOR[zone.kind] or Z.COLOR.base
    local a = (col.a or 0.2)
    if highlight then a = math.min(0.55, a * 2) end
    drawParts(playerNum, Z.Merge(zone.parts), zone.z, col, a)
end

-- While this timestamp is in the future, every zone of every known beacon is drawn.
-- Set by the beacon's "Show zones" option and by the cursor, so the player can see
-- what already exists while placing something new.
Z.showUntilMs = 0

function Z.ShowFor(ms)
    local now = (getTimestampMs and getTimestampMs()) or 0
    Z.showUntilMs = now + (ms or 8000)
end

-- EVERY AREA OF ONE KIND IS MERGED INTO ONE SHAPE BEFORE IT IS DRAWN.
--
-- The highlight BLENDS, so two areas that touch or overlap show a brighter band along
-- the join, and the eye reads that band as an outline through the middle of the shape
-- -- which is exactly the "there is a highlighted border for each zone I draw" report.
-- Merging per kind means one flat wash per colour with an edge only on the true
-- outside, however many pieces or separate areas went into it.
--
-- Kinds are still drawn separately, which costs nothing now there is one of them and
-- keeps the renderer honest if another is ever added.
local function drawAllZones()
    local now = (getTimestampMs and getTimestampMs()) or 0
    if now > Z.showUntilMs then return end
    if not (BanditsNPC.Beacon and BanditsNPC.Beacon.AllSites) then return end
    local player = getSpecificPlayer(0)
    local pnum = player and player:getPlayerNum() or 0

    -- kind -> z -> list of rectangles. Split by z as well as by kind: merging across
    -- floors would fuse an upstairs room into a downstairs one.
    local byKind = {}
    for _, site in ipairs(BanditsNPC.Beacon.AllSites()) do
        local zones = site.zones
        if type(zones) == "table" then
            for i = 1, #zones do
                local zn = zones[i]
                if zn.parts then
                    -- DRAW ON EVERY FLOOR THE ZONE COVERS (v0.75.54). A BASE zone spans
                    -- zone.z-1 .. zone.z+3 for containment and for the furniture scan, but
                    -- the highlight only ever drew on zone.z -- so the upstairs half of a
                    -- multi-floor base was invisible when you pressed Show Areas, and the
                    -- player had no way to see the shape the mod was actually using.
                    -- Z.ZLevels is the same source containment uses, so the picture and
                    -- the behaviour now come from one place. Work zones return a single
                    -- level from ZLevels, so they are unchanged.
                    local k = zn.kind or Z.KIND.BASE
                    byKind[k] = byKind[k] or {}
                    local levels = (Z.ZLevels and Z.ZLevels(zn)) or { zn.z or 0 }
                    for li = 1, #levels do
                        local lvl = levels[li]
                        byKind[k][lvl] = byKind[k][lvl] or {}
                        local bucket = byKind[k][lvl]
                        for j = 1, #zn.parts do bucket[#bucket + 1] = zn.parts[j] end
                    end
                end
            end
        end
    end

    for kind, levels in pairs(byKind) do
        local col = Z.COLOR[kind] or Z.COLOR.base
        for lvl, parts in pairs(levels) do
            local merged = Z.Merge(parts)
            for i = 1, #merged do
                local p = merged[i]
                Z.DrawRect(pnum, p.x1, p.y1, p.x2, p.y2, lvl, col, col.a)
            end
        end
    end
end

local function flush()
    pcall(drawAllZones)

    local now = (getTimestampMs and getTimestampMs()) or 0
    local source = queue
    if #queue > 0 then
        lastQueue, lastQueueMs = queue, now
    elseif now - lastQueueMs < STICKY_MS then
        source = lastQueue
    end
    -- EMIT FOR EVERY LOCAL PLAYER (v0.75.23). The rectangles are built once against
    -- getSpecificPlayer(0), so in SPLIT-SCREEN player 2 saw no zone highlights at all --
    -- she could draw an area and not see it. The geometry is identical for both screens;
    -- only the player number differs, so this re-issues rather than recomputing.
    local pnums = {}
    for i = 0, 3 do
        if getSpecificPlayer(i) then pnums[#pnums + 1] = i end
    end
    if #pnums == 0 then pnums = { 0 } end
    for i = 1, #source do
        local a = source[i]
        for k = 1, #pnums do
            local pn = pnums[k]
            pcall(function()
                addAreaHighlightForPlayer(pn, a.x1, a.y1, a.x2, a.y2, a.z, a.r, a.g, a.b, a.a)
            end)
        end
    end
    queue = {}
end

Events.OnPreUIDraw.Add(flush)
