--***********************************************************************
-- Railroader / RR_Spike3B0  --  DISPOSABLE measurement harness for Task 3.B-0
--
-- This file is NOT a feature. It answers three questions that could each sink the
-- rolling-stock design (docs/ROLLING_STOCK_DESIGN.md), and it is meant to be DELETED
-- once the numbers are in. Nothing here runs by itself: every entry point is a console
-- command, nothing is bound to a key, and the per-tick hook does nothing until armed.
--
--   SPIKE 1 -- THE ANCHOR (the one that can kill the cargo design).
--     Verified: IsoAnimal.save (IsoAnimal.java:1292-1402) persists zone/x/y/z/dir/stats/
--     ModData/type/breed/genome and *no ItemContainer*. So cargo cannot live on the car
--     entity -- it would vanish on reload. The plan (design doc 4.3) is an ordinary
--     IsoObject on a real square holding a real ItemContainer: vanilla saves it with the
--     chunk, with full item fidelity, and the vanilla loot panel lists it with no UI code.
--     Open questions this measures:
--       (a) does it survive a chunk unload/reload round trip WITH its contents?
--       (b) can we find it again from coordinates kept in ModData?
--       (c) how expensive is moving it as the car changes tile -- and is
--           RecalcAllWithNeighbours needed at all for a container object?
--       (d) MOVE the same object, or RECREATE one and transfer items? Both are built
--           here (Spike.anchorStrategy) because the reuse path is the unverified one:
--           IsoGridSquare.transmitRemoveItemFromSquare -> RemoveTileObject calls
--           obj.removeFromWorld()/removeFromSquare() and does NOT visibly pool the
--           object, but "does not visibly" is exactly what a spike is for.
--
--   SPIKE 2 -- COLLIDER COST OF A CONSIST.
--     One locomotive already lines its hull with a chain of invisible `rr_collider`
--     animals (RR_Collider) *and*, since 1.1, two `rr_truck` animals (RR_Trucks). A
--     six-car train multiplies the first. Spawns N dummy bodies trailing the lead loco
--     and reports the per-tick cost of pinning them.
--
--   SPIKE 3 -- OVERHANG OF A LONG CAR (optional; the business car is phase 3.B-8).
--     A dummy body of arbitrary LENGTH, so the 26-tile heavyweight can be marched
--     through the worst 45-degree kink and looked at before anyone models it. Reuses
--     spike 2's machinery: `I` highlights collider tiles, `U` draws the real габарит.
--
-- Console:
--   RR.Spike.help()
--   RR.Spike.anchorCreate([sprite])   .anchorReport()   .anchorRemove()
--   RR.Spike.anchorAttach()           .anchorDetach()
--   RR.Spike.anchorStrategy("move"|"recreate")          .anchorRecalc(true|false)
--   RR.Spike.bodies(n [, lengthTiles])                  .bodiesOff()
--   RR.Spike.ghost(lengthTiles)                         (= bodies(1, len))
--   RR.Spike.perf()                   .perfReset()
--***********************************************************************

print("[Railroader] RR_Spike3B0.lua: loading...")

RR = RR or {}
local Spike = {}

--------------------------------------------------------------------------
-- Knobs
--------------------------------------------------------------------------
-- Visible on purpose. The shipping anchor would be invisible, but the spike wants to
-- SEE it, and a real vanilla container sprite also exercises the thing we actually care
-- about: createContainersFromSpriteProperties + the vanilla loot panel picking it up
-- with no code of ours. crafted_05_16 = "Locker", ContainerCapacity=40 (read out of
-- newtiledefinitions.tiles with tools/dump_tiledefs.py).
Spike.ANCHOR_SPRITE = "crafted_05_16"
Spike.MD_TAG        = "RR_World"          -- same per-save table the depot flag uses
Spike.MD_KEY        = "spikeAnchor"
Spike.OBJ_TAG       = "rrSpikeAnchor"     -- marker in the object's own ModData
Spike.SEED_ITEMS    = { "Base.Hammer", "Base.Screwdriver", "Base.Nails", "Base.WaterBottleFull" }

Spike.CAR_GAP       = 18.0                -- tiles between dummy body centres (~a 50ft car)
Spike.DEFAULT_LEN   = 17.4                -- tiles; a PS-1 50' boxcar

Spike.anchorRecalcOn = false              -- (c): is RecalcAllWithNeighbours needed?
Spike.strategy       = "move"             -- (d): "move" | "recreate"

--------------------------------------------------------------------------
-- Small helpers
--------------------------------------------------------------------------
local function say(fmt, ...)
    if select("#", ...) > 0 then print("[RR.Spike] " .. string.format(fmt, ...))
    else print("[RR.Spike] " .. tostring(fmt)) end
end

local function blocked()
    -- Same fail-open guard every other entry point uses: a load failure of RR_MP must
    -- not kill singleplayer, so "no guard" means "run".
    return RR.MP and RR.MP.blocked and RR.MP.blocked()
end

local function player()
    return getSpecificPlayer(0)
end

local function worldData()
    local wd
    pcall(function() wd = ModData.getOrCreate(Spike.MD_TAG) end)
    return wd
end

local function squareAt(x, y, z)
    local cell = getCell()
    if not cell then return nil end
    return cell:getGridSquare(math.floor(x), math.floor(y), math.floor(z or 0))
end

-- The lead locomotive record, or nil. Prefers the one being ridden.
local function leadRec()
    if not (RR.TrainEntity and RR.TrainEntity.active) then return nil end
    if RR.Ride and RR.Ride.current then return RR.Ride.current end
    return RR.TrainEntity.active[1]
end

-- length (tiles) -> the animal `size` that RR_Body's geometry is written against.
-- Body.extents: len = MESH.long * RENDER_MULT * size, so size = len / (17.10 * 1.5).
local function sizeForLength(lenTiles)
    if not (RR.Body and RR.Body.MESH) then return 0.7 end
    local unit = RR.Body.MESH.long * (RR.Body.RENDER_MULT or 1.5)
    if unit <= 0 then return 0.7 end
    return lenTiles / unit
end

--------------------------------------------------------------------------
-- Timing. getTimestampMs() is millisecond-resolution, which is far too coarse for one
-- tick -- so we never time a single tick. We accumulate over a window and report the
-- average, plus the worst single sample (which milliseconds CAN see).
--------------------------------------------------------------------------
local perf = { bodies = {n=0,sum=0,max=0}, anchor = {n=0,sum=0,max=0} }

local function timeIt(bucket, fn)
    local t0 = getTimestampMs()
    fn()
    local dt = getTimestampMs() - t0
    local b = perf[bucket]
    b.n = b.n + 1; b.sum = b.sum + dt
    if dt > b.max then b.max = dt end
end

function Spike.perfReset()
    for _, b in pairs(perf) do b.n = 0; b.sum = 0; b.max = 0 end
    say("perf counters reset")
end

function Spike.perf()
    say("---- perf ----")
    for name, b in pairs(perf) do
        if b.n > 0 then
            say("%-7s samples=%-6d avg=%.3f ms   worst=%d ms", name, b.n, b.sum / b.n, b.max)
        else
            say("%-7s (no samples)", name)
        end
    end
    local rec = leadRec()
    local segs = rec and rec.segments and #rec.segments or 0
    say("loco collider segments=%d   dummy bodies=%d   dummy segments=%d",
        segs, #Spike.dummies, Spike.dummySegCount())
    say("NOTE: ms resolution -- read avg over a long run, not the worst sample.")
end

--------------------------------------------------------------------------
-- SPIKE 1 -- the anchor
--------------------------------------------------------------------------
Spike.anchor = nil        -- { obj = IsoObject, x, y, z }

-- Give the object a container. Prefer the sprite's own definition (that is the real
-- mechanism -- the same call CellLoader makes at :325 and the moveables system makes at
-- ISMoveableSpriteProps.lua:2330); fall back to a bare ItemContainer if the sprite
-- carries no container property.
local function ensureContainer(obj)
    pcall(function() obj:createContainersFromSpriteProperties() end)
    local c = obj:getContainer()
    if not c then
        pcall(function()
            local cont = ItemContainer.new("crate", obj:getSquare(), obj)
            obj:setContainer(cont)
        end)
        c = obj:getContainer()
    end
    if c then pcall(function() c:setExplored(true) end) end
    return c
end

local function makeAnchorAt(sq, sprite)
    local obj
    local ok = pcall(function() obj = IsoObject.new(sq, sprite) end)
    if not ok or not obj then return nil end
    pcall(function() obj:getModData()[Spike.OBJ_TAG] = true end)
    pcall(function() sq:AddTileObject(obj) end)
    ensureContainer(obj)
    return obj
end

-- Find the anchor again from the coordinates in ModData. Returns obj, square, reason.
local function findAnchor()
    local wd = worldData()
    local p = wd and wd[Spike.MD_KEY]
    if not p then return nil, nil, "no coordinates in ModData (never created?)" end
    local sq = squareAt(p.x, p.y, p.z)
    if not sq then return nil, nil, string.format("square (%d,%d,%d) NOT LOADED", p.x, p.y, p.z) end
    local objs = sq:getObjects()
    if objs then
        for i = 0, objs:size() - 1 do
            local o = objs:get(i)
            local tagged = false
            pcall(function() tagged = o:getModData()[Spike.OBJ_TAG] == true end)
            if tagged then return o, sq, nil end
        end
    end
    return nil, sq, "square loaded but no tagged object on it"
end

function Spike.anchorCreate(sprite)
    if blocked() then say("MP guard: standing down"); return end
    local p = player(); if not p then say("no player"); return end
    local sq = p:getSquare(); if not sq then say("no square under player"); return end

    -- Idempotent: never leave two anchors around.
    Spike.anchorRemove()

    local obj = makeAnchorAt(sq, sprite or Spike.ANCHOR_SPRITE)
    if not obj then say("FAILED to create the object (bad sprite '%s'?)", tostring(sprite or Spike.ANCHOR_SPRITE)); return end

    local c = obj:getContainer()
    if c then
        for _, ft in ipairs(Spike.SEED_ITEMS) do
            pcall(function() c:AddItem(ft) end)   -- InventoryItemFactory does NOT exist in B42
        end
    end

    local wd = worldData()
    if wd then wd[Spike.MD_KEY] = { x = sq:getX(), y = sq:getY(), z = sq:getZ() } end
    Spike.anchor = { obj = obj, x = sq:getX(), y = sq:getY(), z = sq:getZ() }

    say("anchor created at (%d,%d,%d) sprite=%s container=%s items=%d",
        sq:getX(), sq:getY(), sq:getZ(), tostring(sprite or Spike.ANCHOR_SPRITE),
        c and "YES" or "NO", c and c:getItems():size() or 0)
    say("NOW: walk away >150 tiles, come back, save+quit+reload, then RR.Spike.anchorReport()")
end

function Spike.anchorReport()
    say("---- anchor ----")
    local wd = worldData()
    local p  = wd and wd[Spike.MD_KEY]
    if p then say("ModData coords: (%d,%d,%d)", p.x, p.y, p.z) else say("ModData coords: NONE") end

    local obj, sq, why = findAnchor()
    if not obj then
        say("object: NOT FOUND -- %s", tostring(why))
        say("VERDICT: if the square is loaded and the object is gone, the anchor design FAILS.")
        return
    end
    say("object: found, sprite=%s", tostring(obj:getSprite() and obj:getSprite():getName()))

    local total, n = 0, 0
    pcall(function() n = obj:getContainerCount() end)
    for i = 0, (n or 0) - 1 do
        local c
        pcall(function() c = obj:getContainerByIndex(i) end)
        if c then
            local items = c:getItems()
            total = total + items:size()
            local names = {}
            for k = 0, math.min(items:size(), 12) - 1 do names[#names + 1] = items:get(k):getFullType() end
            say("  container[%d] type=%s items=%d  %s", i, tostring(c:getType()), items:size(), table.concat(names, ", "))
        end
    end
    say("VERDICT: %d item(s) survived. Expected %d after a fresh create.", total, #Spike.SEED_ITEMS)
    -- Keep the live handle in step with what is actually on the square.
    Spike.anchor = { obj = obj, x = sq:getX(), y = sq:getY(), z = sq:getZ() }
end

function Spike.anchorRemove()
    local obj, sq = findAnchor()
    if obj and sq then
        pcall(function() sq:transmitRemoveItemFromSquare(obj) end)
        say("anchor removed")
    end
    local wd = worldData()
    if wd then wd[Spike.MD_KEY] = nil end
    Spike.anchor  = nil
    Spike.following = false
end

--------------------------------------------------------------------------
-- (c)+(d): follow the loco, one relocation per TILE CHANGE (not per tick).
--------------------------------------------------------------------------
Spike.following = false
Spike.moves     = 0

function Spike.anchorStrategy(s)
    if s ~= "move" and s ~= "recreate" then say("strategy must be \"move\" or \"recreate\""); return end
    Spike.strategy = s
    say("relocation strategy = %s", s)
end

function Spike.anchorRecalc(v)
    Spike.anchorRecalcOn = (v == true)
    say("RecalcAllWithNeighbours on relocation = %s", tostring(Spike.anchorRecalcOn))
end

function Spike.anchorAttach()
    if blocked() then say("MP guard: standing down"); return end
    if not leadRec() then say("no locomotive in the world -- spawn/approach one first"); return end
    local obj = findAnchor()
    if not obj then say("no anchor -- RR.Spike.anchorCreate() first"); return end
    Spike.following = true
    Spike.moves     = 0
    say("anchor now follows the loco (strategy=%s, recalc=%s). Drive, then RR.Spike.perf().",
        Spike.strategy, tostring(Spike.anchorRecalcOn))
end

function Spike.anchorDetach()
    Spike.following = false
    say("anchor detached after %d relocation(s)", Spike.moves)
end

-- Move the anchor onto `sq`, by whichever strategy is armed.
local function relocate(obj, oldSq, newSq)
    if Spike.strategy == "move" then
        pcall(function() oldSq:transmitRemoveItemFromSquare(obj) end)
        pcall(function() newSq:AddTileObject(obj) end)
        if Spike.anchorRecalcOn then
            pcall(function() newSq:RecalcAllWithNeighbours(true) end)
            pcall(function() oldSq:RecalcAllWithNeighbours(true) end)
        end
        return obj
    end

    -- "recreate": a fresh object on the new square, items carried across by hand. Slower
    -- by construction, but immune to whatever the engine thinks it owns about the old one.
    local fresh = makeAnchorAt(newSq, Spike.ANCHOR_SPRITE)
    if fresh then
        local src, dst = obj:getContainer(), fresh:getContainer()
        if src and dst then
            local items = src:getItems()
            for i = items:size() - 1, 0, -1 do
                local it = items:get(i)
                pcall(function() dst:AddItem(it); src:Remove(it) end)
            end
        end
    end
    pcall(function() oldSq:transmitRemoveItemFromSquare(obj) end)
    if Spike.anchorRecalcOn then
        pcall(function() newSq:RecalcAllWithNeighbours(true) end)
        pcall(function() oldSq:RecalcAllWithNeighbours(true) end)
    end
    return fresh or obj
end

local function anchorTick()
    if not Spike.following then return end
    local rec = leadRec()
    if not (rec and rec.lastPose) then return end
    local a = Spike.anchor
    if not (a and a.obj) then Spike.following = false; return end

    local nx, ny, nz = math.floor(rec.lastPose.x), math.floor(rec.lastPose.y), math.floor(rec.lastPose.z or 0)
    if nx == a.x and ny == a.y and nz == a.z then return end       -- same tile: nothing to do

    local oldSq = squareAt(a.x, a.y, a.z)
    local newSq = squareAt(nx, ny, nz)
    if not (oldSq and newSq) then return end                        -- chunk not streamed; skip quietly

    timeIt("anchor", function()
        local obj = relocate(a.obj, oldSq, newSq)
        Spike.anchor = { obj = obj, x = nx, y = ny, z = nz }
        local wd = worldData()
        if wd then wd[Spike.MD_KEY] = { x = nx, y = ny, z = nz } end
    end)
    Spike.moves = Spike.moves + 1
end

--------------------------------------------------------------------------
-- SPIKE 2 / 3 -- dummy bodies
--------------------------------------------------------------------------
Spike.dummies = {}        -- list of { size, offset, segments = { {animal, along, lateral} } }

function Spike.dummySegCount()
    local n = 0
    for _, d in ipairs(Spike.dummies) do n = n + #d.segments end
    return n
end

-- Pose of a body trailing the lead by `offset` tiles. Same construction as
-- RR_Collider.bodyFrame's fallback branch: bodyPose on the spline, then the per-direction
-- calibration, then the record's own world offset (the demo route uses it).
local function carPose(rec, offset, size)
    if not (rec.route and RR.Spline and RR.Body) then return nil end
    local d = (rec.distance or 0) - offset
    local p = RR.Spline.bodyPose(rec.route, d, RR.Body.halfWheelbase(size))
    if not p then return nil end
    local cox, coy, coz = 0, 0, 0
    if RR.Calib and RR.Calib.offsetFor then
        cox, coy, coz = RR.Calib.offsetFor(p.dirX or 0, p.dirY or 0)
    end
    local fx, fy = p.dirX or 0, p.dirY or -1
    if fx == 0 and fy == 0 then fx, fy = 0, -1 end
    return p.x + cox + (rec.ox or 0), p.y + coy + (rec.oy or 0), p.z + (coz or 0) + (rec.oz or 0),
           fx, fy, -fy, fx
end

local function spawnDummySeg(cell, x, y, z)
    -- Deliberately the SAME type and the same flags as a real collider segment, or the
    -- measurement would not be measuring the real thing.
    local adef = AnimalDefinitions and AnimalDefinitions.getDef("rr_collider")
    if not adef then return nil end
    local breed
    local ok = pcall(function() breed = adef:getRandomBreed() end)
    if not ok or not breed then return nil end
    local a
    ok = pcall(function()
        a = addAnimal(cell, math.floor(x), math.floor(y), math.floor(z), "rr_collider", breed)
    end)
    if not ok or not a then return nil end
    pcall(function() a:addToWorld() end)
    pcall(function() a:setGodMod(true) end)
    pcall(function() a:setIsInvincible(true) end)
    pcall(function() a:setInvisible(true, true) end)
    pcall(function() a:setNpc(true) end)
    return a
end

function Spike.bodies(n, lengthTiles)
    if blocked() then say("MP guard: standing down"); return end
    n = tonumber(n) or 1
    local len  = tonumber(lengthTiles) or Spike.DEFAULT_LEN
    local size = sizeForLength(len)

    Spike.bodiesOff()

    local rec = leadRec()
    if not rec then say("no locomotive in the world -- spawn/approach one first"); return end
    if not (RR.Body and RR.Body.segmentGrid) then say("RR.Body missing"); return end

    local cell = getCell()
    local grid = RR.Body.segmentGrid(size)
    for i = 1, n do
        local offset = Spike.CAR_GAP * i
        local d = { size = size, offset = offset, segments = {} }
        local cx, cy, cz, fx, fy, rx, ry = carPose(rec, offset, size)
        if cx then
            for _, slot in ipairs(grid) do
                local a = spawnDummySeg(cell,
                    cx + slot.along * fx + slot.lateral * rx,
                    cy + slot.along * fy + slot.lateral * ry, cz)
                if a then d.segments[#d.segments + 1] = { animal = a, along = slot.along, lateral = slot.lateral } end
            end
        end
        Spike.dummies[#Spike.dummies + 1] = d
    end

    say("%d dummy bodies, length=%.1f tiles (size=%.3f), %d segments each, %d total",
        #Spike.dummies, len, size, #grid, Spike.dummySegCount())
    say("Drive. Then RR.Spike.perf().  `I` highlights collider tiles, `U` draws the габарит.")
end

-- Spike 3: one body of a given length, to look at the overhang on a 45-degree kink.
function Spike.ghost(lengthTiles)
    Spike.bodies(1, tonumber(lengthTiles) or 26.0)
    say("ghost body -- march it through the worst kink with `I` on and watch the corner cut.")
end

function Spike.bodiesOff()
    local n = 0
    for _, d in ipairs(Spike.dummies) do
        for _, seg in ipairs(d.segments) do
            if seg.animal then
                pcall(function() seg.animal:removeFromWorld() end)
                pcall(function() seg.animal:removeFromSquare() end)
                n = n + 1
            end
        end
    end
    Spike.dummies = {}
    if n > 0 then say("removed %d dummy segments", n) end
end

local function bodiesTick()
    if #Spike.dummies == 0 then return end
    local rec = leadRec()
    if not rec then return end
    timeIt("bodies", function()
        for _, d in ipairs(Spike.dummies) do
            local cx, cy, cz, fx, fy, rx, ry = carPose(rec, d.offset, d.size)
            if cx then
                for _, seg in ipairs(d.segments) do
                    if seg.animal then
                        local x = cx + seg.along * fx + seg.lateral * rx
                        local y = cy + seg.along * fy + seg.lateral * ry
                        pcall(function() seg.animal:setX(x) end)
                        pcall(function() seg.animal:setY(y) end)
                        pcall(function() seg.animal:setZ(cz) end)
                        pcall(function() seg.animal:setTargetAndCurrentDirection(fx, fy) end)
                    end
                end
            end
        end
    end)
end

--------------------------------------------------------------------------
-- Tick + help
--------------------------------------------------------------------------
local function onTick()
    if blocked() then return end
    anchorTick()
    bodiesTick()
end
Events.OnTick.Add(onTick)

function Spike.help()
    say("---- Task 3.B-0 measurement harness (disposable) ----")
    say("SPIKE 1  anchor / cargo persistence:")
    say("  RR.Spike.anchorCreate()            place a container object + seed items, remember coords")
    say("  RR.Spike.anchorReport()            is it still there, with what inside")
    say("  RR.Spike.anchorAttach() / Detach() make it follow the loco (one move per tile change)")
    say("  RR.Spike.anchorStrategy(\"move\"|\"recreate\")   RR.Spike.anchorRecalc(true|false)")
    say("  RR.Spike.anchorRemove()")
    say("SPIKE 2  consist collider cost:")
    say("  RR.Spike.bodies(6)                 six dummy bodies trailing the loco")
    say("  RR.Spike.bodiesOff()")
    say("SPIKE 3  long-car overhang:")
    say("  RR.Spike.ghost(26)                 one 26-tile body (the 85ft business car)")
    say("MEASURE:  RR.Spike.perf()   RR.Spike.perfReset()")
    say("Test order for spike 1: create -> walk 150+ tiles away -> return -> report ->")
    say("  save+quit+reload -> report again. Contents must match both times.")
end

RR.Spike = Spike
print("[Railroader] RR_Spike3B0.lua: ready (console only -- RR.Spike.help())")
return Spike
