--***********************************************************************
-- Railroader / RR_Collider  -- physical collider for the loco (Task: collision)
--
-- Zombies and the player must not pass through the locomotive. PZ has no box/poly
-- collider for characters -- the only character-vs-character blocking is separate(),
-- a per-entity CIRCULAR radius (width = collisionSize*size). One circle can't cover
-- a ~15.5 x 3.2 tile body, and our visible loco carries blockMovement=true which
-- disables its own push (isPushedByForSeparate). So the collider is a GRID of
-- invisible "rr_collider" segment animals over the body (a capsule of circles in
-- TWO lateral rows so both flanks are covered), positioned every tick from the SAME
-- spline the loco rides (so on curves they follow the rails). Layout is the pure
-- RR_Body geometry -- the primitive the future "crush zombies" sweep will reuse.
--
-- Making the segments unobtrusive (all verified, see ENGINE_NOTES "Collision"):
--   * INVISIBLE: the rr_collider type uses a STATIC model, which draws BLANK on the
--     animal path (skinningData null). Admin/debug-proof, unlike setInvisible.
--   * SILENT: mute the segment's sound emitter every tick (setVolumeAll(0)+stopAll)
--     -- kills mooing/footsteps. (We can't stop the AI itself: blockMovement would
--     disable the collision push, so the wander runs but is invisible + muted +
--     position/heading-pinned, i.e. unobservable.)
--   * CENTRED: apply the SAME per-direction RR_Calibrate offset the visible loco
--     uses, so the capsule sits on the model (one row alone let a zombie bite the
--     driver through the mis-centred far side).
--
-- Lifecycle mirrors RR.Sound: RR_TrainEntity calls onSpawn/update/onRemove per rec.
-- Debug: press I to highlight the ACTUAL segment tiles (markers) -- the bodies are
-- invisible, so this is how you verify placement/coverage. Collider.enabled=false
-- disables the whole system.
--***********************************************************************

print("[Railroader] RR_Collider.lua: loading...")
require("Railroader/RR_Spline")
require("Railroader/RR_Body")

local Collider = {}
Collider.SEG_TYPE  = "rr_collider"
Collider.enabled   = true      -- master on/off
-- Spawn the chain of invisible rr_collider animal-segments? DEFAULT OFF. The segment
-- separate() push was only ever the SOFT complement to the hard ejectIntruders sweep
-- (below); in practice it never smoothed anything -- zombies still jitter and lean
-- into the hull -- so it earned nothing while costing per-tick spawn/pin of dozens of
-- animals, a save/reload orphan mess, AND a ground shadow per segment pooled under the
-- loco (IsoAnimal casts its own shadow; no per-entity opt-out). ejectIntruders alone
-- is the wall. Flip back to true only if eject-only proves insufficient in-game.
-- (With this false, sweepOrphans below removes ALL rr_collider animals in the cell, so
-- it also migrates old saves that still carry the littered segments + their shadows.)
Collider.useSegments = false
Collider.eject     = true      -- HARD keep-out sweep (see ejectIntruders)
-- KEEP-OUT PAD -- how far OUTSIDE the raw hull rectangle the wall stands, in tiles.
-- It widens the rectangle for BOTH the containment test and the clamp target, so the
-- two agree (see EJECT_MARGIN below for why that matters). Its real job is the body
-- radius: we test a character's CENTRE against the hull box, so with pad 0 a survivor
-- stands half-sunk into the flank. ~0.3 puts them against the plating instead.
Collider.EJECT_PAD = 0.30
-- LEGACY OVERSHOOT -- extra distance the clamp target sits PAST the wall, on top of the
-- pad. MUST STAY 0. This was 0.15, and it was the character-jitter bug (v1.0.2):
-- the containment test used the raw half-width hw while the target was hw + 0.15, so
-- [hw, hw+0.15] was a DEAD BAND the intruder could walk through untouched. The cycle
-- was: 3-4 frames of smooth travel INTO the hull at ~0.05 tile/frame, then one 0.2-tile
-- shove back out -- a ~15 Hz sawtooth, i.e. exactly the "move, get flung, repeat" the
-- report described. With target == test boundary there is no band: the intruder is put
-- back on the same line every tick, so the drawn position never changes and the walk
-- animation plays on the spot, which is what walking into a vanilla wall looks like.
-- (Our sweep runs from OnTick, and OnTick is the LAST thing before the render --
-- IngameState.update: IsoWorld.update (-> MovingObjectUpdateScheduler.postupdate, where
-- the engine commits setX) -> AnimalController -> OnTick -> render. So the clamp always
-- gets the final word on where a character is drawn.)
-- Set to 0.15 from the console to reproduce the old jitter without a rebuild.
Collider.EJECT_MARGIN = 0.0
-- Sync lastX/lastY on every eject. Vanilla always pairs the two when it teleports a
-- character (IsoZombie.climbFenceWindowHit = setX then setLastX(getX())), and our own
-- RR_Ride step-off has done so since day one, so we keep it -- but it is NOT what fixes
-- the jitter, and the v1.0.2 note claiming it was is wrong. IsoMovingObject.update
-- (:1086-1090) re-stamps lastX = getX() at the top of every character's step, and our
-- sweep runs after postupdate, so by the time the engine next consults getLastX() it
-- already holds the clamped position -- there is nothing stale to roll back to.
-- WHO THE SWEEP REACHES: the PLAYER at any loco speed (the crush never targets them, so
-- the clamp is their only wall), and zombies only below Body.CRUSH.V_MIN -- above that
-- they are handed to RR_Crush, which never moves anyone, it only knockDowns/Kills. So a
-- zombie only ever meets the wall against a parked or creeping loco.
Collider.EJECT_SYNC_LAST = true
Collider.debugMark = false     -- I: highlight actual segment tiles (bodies are invisible)
Collider._breed    = nil       -- cached breed object
Collider._markers  = {}        -- live debug GridSquareMarker handles

--------------------------------------------------------------------------
-- The loco's RIGID BODY FRAME in world coords: centre + forward/right unit vectors.
-- Segments hang off this frame rather than being sampled individually along the
-- rails, so the collider capsule is the same rigid box as the visible hull and as
-- the eject/crush rectangle. That matters on corners: the body chords the kink
-- (RR.Spline.bodyPose) instead of following it, and the collider must chord with it
-- -- a track-following capsule would bend out from under the model.
--
-- rec.lastPose is the pose RR_TrainEntity applied THIS tick (calibration + world
-- offset already baked in). It doesn't exist yet at spawn time, so derive it the
-- same way then.
--------------------------------------------------------------------------
local function bodyFrame(rec)
    local pose = rec.lastPose
    if not pose then
        local size = 0.7
        if rec.animal then pcall(function() size = rec.animal:getAnimalSize() end) end
        local p = RR.Spline.bodyPose(rec.route, rec.distance, RR.Body.halfWheelbase(size))
        local cox, coy, coz = 0, 0, 0
        if RR.Calib and RR.Calib.offsetFor then
            cox, coy, coz = RR.Calib.offsetFor(p.dirX or 0, p.dirY or 0)
        end
        pose = { x = p.x + cox + (rec.ox or 0), y = p.y + coy + (rec.oy or 0),
                 z = p.z + (coz or 0) + (rec.oz or 0), dirX = p.dirX, dirY = p.dirY }
    end
    local fx, fy = pose.dirX or 0, pose.dirY or -1
    if fx == 0 and fy == 0 then fx, fy = 0, -1 end
    return pose.x, pose.y, pose.z, fx, fy, -fy, fx      -- cx, cy, cz, forward, right
end

--------------------------------------------------------------------------
-- Resolve (and cache) a real breed for the segment type. A nil breed NPEs inside
-- IsoAnimal.init (see ENGINE_NOTES), so this must succeed before spawning.
--------------------------------------------------------------------------
local function segBreed()
    if Collider._breed then return Collider._breed end
    local adef = AnimalDefinitions and AnimalDefinitions.getDef(Collider.SEG_TYPE)
    if not adef then return nil end
    local ok, breed = pcall(function() return adef:getRandomBreed() end)
    if ok then Collider._breed = breed end
    return Collider._breed
end

--------------------------------------------------------------------------
-- Spawn one invisible (static-model) collidable segment at world (x,y,z).
--------------------------------------------------------------------------
local function spawnSegment(cell, x, y, z)
    local breed = segBreed()
    if not breed then return nil end
    local animal
    local ok = pcall(function()
        animal = addAnimal(cell, math.floor(x), math.floor(y), math.floor(z),
                           Collider.SEG_TYPE, breed)
    end)
    if not ok or not animal then return nil end
    pcall(function() animal:addToWorld() end)
    pcall(function() animal:setGodMod(true) end)             -- no damage reactions
    pcall(function() animal:setIsInvincible(true) end)        -- guard IsoAnimal health (setIsInvincible = ungated; setInvincible is capability-gated -> forced false)
    pcall(function() animal:setInvisible(true, true) end)    -- belt-and-braces (model is blank anyway)
    -- Stop the player TRIPPING/FALLING over the collider. Walking into the capsule
    -- overlaps several segments at once; the first sets the player's bumpedChr, then
    -- the next sees bumpedChr != null and fires setBumpFall -> a stumble/fall
    -- (IsoMovingObject.separate:1235-1242). That fall is gated by
    -- `!objPlyr.isNpc()`, so marking the segment an NPC suppresses it while the push
    -- (the actual blocking, :1314) is unaffected. setNpc only attaches an ECS
    -- AIComponent that nothing in B42 simulates (verified: no AIComponent systems),
    -- so it's inert here.
    pcall(function() animal:setNpc(true) end)
    -- NB: deliberately NO setBlockMovement -- it would stop the segment pushing.
    -- Silence is handled in the def (idleSoundVolume=0 + cleared breed sounds), not
    -- at runtime (the emitter exposes no Lua mute).
    return animal
end

--------------------------------------------------------------------------
-- Pin one segment onto its grid slot in the body frame: `along` down the travel
-- axis, `lateral` across it. Keeps it muted and heading-locked.
--------------------------------------------------------------------------
local function pinSegment(rec, seg)
    if not (seg.animal and rec.route) then return end
    local cx, cy, cz, fx, fy, rx, ry = bodyFrame(rec)

    local x = cx + seg.along * fx + seg.lateral * rx
    local y = cy + seg.along * fy + seg.lateral * ry

    pcall(function() seg.animal:setX(x) end)
    pcall(function() seg.animal:setY(y) end)
    pcall(function() seg.animal:setZ(cz) end)
    pcall(function() seg.animal:setTargetAndCurrentDirection(fx, fy) end)
end

--------------------------------------------------------------------------
-- HARD keep-out sweep. The segment separate() push is SOFT ((len-twidth)/8 per
-- frame), so fast/strong zombies (e.g. Extinction sprinters) can out-run it and
-- reach the driver; slow ones (Rising) can't. This guarantees impenetrability at
-- any speed: every tick, anything inside the loco's oriented footprint rectangle
-- (except the driver) is CLAMPED back onto the nearest face of that rectangle.
-- Reuses the RR.Body footprint -- the same primitive the crush sweeps.
--
-- Clamp, not eject: the rectangle is grown by EJECT_PAD for the test AND for the
-- target, so an intruder is put back on the very line that defines "inside". No
-- dead band means no move-shove-move sawtooth; the character simply cannot advance,
-- which is how a vanilla wall reads (the engine does the same thing one step
-- earlier, by clamping nextX/nextY to lastX/lastY in IsoMovingObject.postupdate
-- rather than by moving anyone). Lateral clamps preserve the along-body coordinate
-- and vice versa, so sliding along the flank still works.
--
-- Center/heading come from rec.lastPose (calibration already applied by applyPose),
-- so the box matches the visible hull. We collect intruders first, THEN move them
-- (setX changes square membership, which would invalidate the iteration).
--------------------------------------------------------------------------
local function ejectIntruders(rec)
    if not (Collider.eject and rec.animal) then return end

    -- Yield zombies to the CRUSH when the loco is moving fast enough to run them
    -- over (RR_Crush handles av >= V_MIN with knockDown/Kill). Ejecting them first
    -- would teleport them out of the footprint before the crush sweep sees them
    -- (Collider.update runs before Crush.update). Below V_MIN the crush is inert, so
    -- the eject is the wall that keeps a parked/creeping loco impenetrable. The
    -- PLAYER is never crushed, so they're ejected at ANY speed (see consider()).
    local av = math.abs(rec.speed or 0)
    local crushing = RR.Body.CRUSH and av >= RR.Body.CRUSH.V_MIN
    -- ...and yield a body that is ALREADY DOWN from the moment the wheels turn at
    -- all (V_ROLL), not just from V_MIN. Between the two speeds the crush kills the
    -- downed (RR_Body.CRUSH) while the standing are still only walled off, so if we
    -- clamped the downed out of the box first the crush would never see them -- and
    -- that gap is the reported bug: floored at 2 m/s, dragged along untouched under
    -- the hull, back on its feet INSIDE the loco.
    -- RR.Crush.rolling also covers its ROLL_GRACE window (a loco that stopped ON a
    -- body still owes it a death, and the flag has to agree or we would slide the
    -- body out before the crush finished it). Fall back to the raw comparison if
    -- RR_Crush failed to load, so the clamp keeps working on its own.
    local rolling
    if RR.Crush and RR.Crush.rolling then
        rolling = RR.Crush.rolling(rec, av)
    else
        rolling = RR.Body.CRUSH and av >= (RR.Body.CRUSH.V_ROLL or 0)
    end

    local pose = rec.lastPose
    local cx, cy, cz, fx, fy
    if pose then
        cx, cy, cz, fx, fy = pose.x, pose.y, pose.z, pose.dirX, pose.dirY
    else
        cx, cy, cz = rec.animal:getX(), rec.animal:getY(), rec.animal:getZ()
        fx, fy = 0, -1
    end
    if not fx or (fx == 0 and fy == 0) then fx, fy = 0, -1 end
    local flen = math.sqrt(fx * fx + fy * fy)
    fx, fy = fx / flen, fy / flen
    local rx, ry = -fy, fx

    local size = 0.7
    pcall(function() size = rec.animal:getAnimalSize() end)
    local len, wid = RR.Body.extents(size)
    -- The wall stands EJECT_PAD outside the raw hull box (body radius of the intruder),
    -- and hl/hw below are that padded box -- used for the test and for the clamp alike.
    local pad = Collider.EJECT_PAD or 0
    local hl, hw = len * 0.5 + pad, wid * 0.5 + pad
    -- Lateral centre bias: the box straddles the DRAWN hull's centreline, not the raw
    -- anchor (RR_Body.LAT_BIAS -- the anchor sits a little off-centre across the width,
    -- which made the keep-out zone reach further on one flank than the other).
    local bias = RR.Body.LAT_BIAS or 0

    -- Is this loco the one being driven? Its driver is exempt (they sit at centre).
    local driver = nil
    if RR.Ride and RR.Ride.current == rec then driver = getPlayer() end

    local cell = getCell()
    if not cell then return end

    -- AABB half-extents of the ORIENTED rectangle (cheap reject before projecting).
    local ex = hl * math.abs(fx) + hw * math.abs(rx)
    local ey = hl * math.abs(fy) + hw * math.abs(ry)

    -- Test one candidate; append to hits if its centre is inside the rectangle.
    local hits = {}
    local function consider(obj)
        if not obj or obj == driver then return end
        local ox, oy, oz = obj:getX(), obj:getY(), obj:getZ()
        if math.abs((oz or 0) - (cz or 0)) >= 1 then return end
        local dx, dy = ox - cx, oy - cy
        if math.abs(dx) > ex + 1 or math.abs(dy) > ey + 1 then return end   -- fast reject
        local pf = dx * fx + dy * fy
        local pr = dx * rx + dy * ry - bias        -- lateral from the HULL centreline
        if math.abs(pf) < hl and math.abs(pr) < hw then
            hits[#hits + 1] = { obj = obj, pf = pf, pr = pr }
        end
    end

    -- Will the crush take this body this tick? Only then may we leave it alone.
    -- Three conditions, and the third is the fiddly one: the crush box is the RAW
    -- footprint (RR.Body.contains, no pad) while ours is that box grown by EJECT_PAD,
    -- so a downed body in the 0.3-tile ring between them is NOT under the wheels --
    -- yielding it there would leave it untouched by both of us, lying in the ring
    -- until it stood up and then took a 0.3-tile shove. Hand over exactly what the
    -- crush will actually kill, nothing more.
    local function yieldToCrush(z)
        if not (rolling and pose and z) then return false end
        if not (RR.Crush and RR.Crush.isDown and RR.Crush.isDown(z)) then return false end
        local zx, zy
        local ok = pcall(function() zx = z:getX(); zy = z:getY() end)
        if not (ok and zx and zy) then return false end
        return (RR.Body.contains(pose, size, zx, zy)) and true or false
    end

    -- Zombies: only when the loco ISN'T crushing (else RR_Crush runs them over), and
    -- never a body the crush is about to take (see yieldToCrush). A standing-still
    -- loco keeps clamping the downed, so a crawler still cannot crawl under a parked
    -- hull to the driver.
    if not crushing then
        local zlist
        pcall(function() zlist = cell:getZombieList() end)
        if zlist then
            local n = zlist:size()
            for i = 0, n - 1 do
                local z = zlist:get(i)
                if not yieldToCrush(z) then consider(z) end
            end
        end
    end
    -- The local player, if not the driver (stops "you can run inside if you try").
    -- Ejected at ANY loco speed -- the crush never targets the player.
    local p = getPlayer()
    if p and p ~= driver then consider(p) end

    -- Clamp each onto the nearest face (least penetration: sideways vs off-the-end).
    -- m is the legacy overshoot and is 0: the target lands ON the test boundary, so
    -- there is no band left for the intruder to creep through between ticks.
    local m = Collider.EJECT_MARGIN or 0
    for _, h in ipairs(hits) do
        local pf, pr = h.pf, h.pr
        local nx, ny
        -- Targets are built back in WORLD terms, so the hull-frame lateral gets the
        -- bias added again (pr/npr are measured from the hull centreline).
        if (hw - math.abs(pr)) <= (hl - math.abs(pf)) then
            local npr = ((pr >= 0) and 1 or -1) * (hw + m) + bias
            nx = cx + pf * fx + npr * rx
            ny = cy + pf * fy + npr * ry
        else
            local npf = ((pf >= 0) and 1 or -1) * (hl + m)
            nx = cx + npf * fx + (pr + bias) * rx
            ny = cy + npf * fy + (pr + bias) * ry
        end
        -- Three writes: where they are, where they were about to step, and where they
        -- came FROM -- the trio vanilla always writes together when it moves a
        -- character (see Collider.EJECT_SYNC_LAST for what each one is actually worth).
        pcall(function() h.obj:setX(nx); h.obj:setY(ny) end)
        pcall(function() h.obj:setNextX(nx); h.obj:setNextY(ny) end)  -- kill the pending step
        if Collider.EJECT_SYNC_LAST then
            pcall(function() h.obj:setLastX(nx); h.obj:setLastY(ny) end)  -- no rollback inside
        end
    end
end

--------------------------------------------------------------------------
-- onSpawn: build the segment grid for a freshly spawned loco record.
--------------------------------------------------------------------------
function Collider.onSpawn(rec)
    if not (Collider.enabled and rec and rec.animal and RR.Body and RR.Spline) then return end
    rec.segments = nil
    -- Eject-only collider: skip the animal-segment chain entirely (see useSegments).
    if not Collider.useSegments then return end
    if not (AnimalDefinitions and AnimalDefinitions.getDef(Collider.SEG_TYPE)) then
        print("[Railroader] RR_Collider: rr_collider type not defined; no collider spawned")
        return
    end

    local size = 0.7
    pcall(function() size = rec.animal:getAnimalSize() end)
    local grid = RR.Body.segmentGrid(size)

    local cell = getCell()
    local cx, cy, cz, fx, fy, rx, ry = bodyFrame(rec)
    rec.segments = {}
    for _, slot in ipairs(grid) do
        local a = spawnSegment(cell,
            cx + slot.along * fx + slot.lateral * rx,
            cy + slot.along * fy + slot.lateral * ry,
            cz)
        if a then table.insert(rec.segments, { animal = a, along = slot.along, lateral = slot.lateral }) end
    end
    print(string.format("[Railroader] collider: %d segments (size=%.3f, radius=%.2f, rows=%d)",
          #rec.segments, size, RR.Body.SEG_RADIUS, RR.Body.ROWS))
end

--------------------------------------------------------------------------
-- update: re-pin every segment. Called each tick from RR_TrainEntity.onTick AFTER
-- the loco pose/distance is updated.
--------------------------------------------------------------------------
function Collider.update(rec, dt)
    if not (Collider.enabled and rec) then return end
    if rec.segments then
        for _, seg in ipairs(rec.segments) do
            pinSegment(rec, seg)
        end
    end
    ejectIntruders(rec)
    if Collider.debugMark then Collider.redrawMarkers() end
end

--------------------------------------------------------------------------
-- onRemove: despawn all segments for a record (called from TrainEntity.clear).
--------------------------------------------------------------------------
function Collider.onRemove(rec)
    if not (rec and rec.segments) then return end
    for _, seg in ipairs(rec.segments) do
        if seg.animal then
            pcall(function() seg.animal:removeFromWorld() end)
            pcall(function() seg.animal:removeFromSquare() end)
        end
    end
    rec.segments = nil
end

--------------------------------------------------------------------------
-- sweepOrphans: remove stray rr_collider segment animals that no live loco owns.
-- WHY: segments are real IsoAnimals, so PZ saves & reloads them like any animal --
-- but they carry NO blockMovement (needed for the push), so across a save/reload
-- their wander AI has scattered them, and nothing re-pins them (re-adoption only
-- re-owns the LOCO, then spawns a FRESH chain via onSpawn). The stale copies just
-- litter the world (invisible, but they cast shadows). We can't stop PZ persisting
-- them, so we sweep them on load: any rr_collider whose animalId isn't in a current
-- rec.segments chain is garbage. Owned (freshly-spawned) segments are kept.
-- Called from RR_TrainEntity.reacquire (on load + the periodic scan).
--------------------------------------------------------------------------
function Collider.sweepOrphans()
    if not Collider.enabled then return 0 end
    local cell = getCell()
    if not cell then return 0 end
    local list = cell:getAnimals()
    if not list then return 0 end

    -- animalIds of segments we currently own (spawned this session, still pinned).
    local owned = {}
    local active = (RR and RR.TrainEntity and RR.TrainEntity.active) or {}
    for _, rec in ipairs(active) do
        if rec.segments then
            for _, seg in ipairs(rec.segments) do
                if seg.animal then
                    local id; pcall(function() id = seg.animal:getAnimalID() end)
                    if id then owned[id] = true end
                end
            end
        end
    end

    local n = 0
    for i = 0, list:size() - 1 do
        local a = list:get(i)
        local t; pcall(function() t = a:getAnimalType() end)
        if t == Collider.SEG_TYPE then
            local id; pcall(function() id = a:getAnimalID() end)
            if not (id and owned[id]) then
                pcall(function() a:removeFromWorld() end)
                pcall(function() a:removeFromSquare() end)
                n = n + 1
            end
        end
    end
    if n > 0 then print("[Railroader] collider: swept " .. n .. " orphaned segment(s) after reload") end
    return n
end

--------------------------------------------------------------------------
-- Debug markers (I): highlight the ACTUAL tile each segment occupies. The bodies
-- are invisible, so this is how placement/coverage is verified. Cleared + re-added
-- each frame while enabled (segment count is small).
--------------------------------------------------------------------------
local function clearMarkers()
    local wm
    pcall(function() wm = getWorldMarkers() end)
    if wm then
        for _, h in ipairs(Collider._markers) do
            pcall(function() wm:removeGridSquareMarker(h) end)
        end
    end
    Collider._markers = {}
end

function Collider.redrawMarkers()
    clearMarkers()
    if not Collider.debugMark then return end
    local wm, cell
    pcall(function() wm = getWorldMarkers() end)
    pcall(function() cell = getCell() end)
    if not (wm and cell) then return end
    local active = (RR and RR.TrainEntity and RR.TrainEntity.active) or {}
    for _, rec in ipairs(active) do
        if rec.segments then
            for _, seg in ipairs(rec.segments) do
                if seg.animal then
                    local tx = math.floor(seg.animal:getX())
                    local ty = math.floor(seg.animal:getY())
                    local tz = math.floor(seg.animal:getZ() + 0.5)
                    local sq
                    pcall(function() sq = cell:getGridSquare(tx, ty, tz) end)
                    if sq then
                        local h
                        pcall(function() h = wm:addGridSquareMarker(sq, 0.0, 0.8, 1.0, false, 0.6) end)
                        if h then table.insert(Collider._markers, h) end
                    end
                end
            end
        end
    end
end

function Collider.setDebugMark(v)
    Collider.debugMark = v and true or false
    if Collider.debugMark then Collider.redrawMarkers() else clearMarkers() end
    print("[Railroader] collider segment markers " .. (Collider.debugMark and "ON (cyan tiles)" or "off"))
end

local function onKey(key)
    if not (RR.Debug and RR.Debug.isOn()) then return end   -- field mode: no marker key
    if key == Keyboard.KEY_I then Collider.setDebugMark(not Collider.debugMark) end
end
Events.OnKeyPressed.Add(onKey)

RR = RR or {}
RR.Collider = Collider
print("[Railroader] RR_Collider.lua: loaded OK (I = segment tiles -- a gated dev key, RR.Debug.set(true)).")

return Collider
