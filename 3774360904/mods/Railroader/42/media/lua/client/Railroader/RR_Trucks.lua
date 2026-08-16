--***********************************************************************
-- Railroader / RR_Trucks  -- the two trucks (bogies) as their own entities (Task 3.C)
--
-- WHAT IT DOES. The locomotive's trucks swivel to point along the rail UNDER THEM while
-- the carbody rides the chord between them, the way a real long-frame loco takes a curve.
-- Purely visual: nothing here touches the drive, the collider, the crush sweep or any
-- saved state.
--
-- WHY IT IS THREE ENTITIES AND NOT ONE MESH WITH THREE BONES. Because B42 gives Lua no
-- way to move a bone. Every bone-transform method lives on AnimationPlayer, which is not
-- in Kahlua's exposed whitelist and carries zero @LuaMethod, so `getAnimationPlayer()`
-- hands back a handle with nothing callable on it; model-script attachments are static
-- per SCRIPT, fixed at load, not per instance and not per frame. Verified 2026-07-21, and
-- re-verified when this was written -- see docs/ENGINE_NOTES.md, "Lua cannot drive
-- individual bones". A separate entity is not the cheap way round the problem, it is the
-- only way.
--
-- LIFECYCLE, and why it is the cheapest one available:
--   * spawn with the loco, despawn with it, and sweep any stray rr_truck on load.
--   * NOTHING IS PERSISTED. A truck holds no state that cannot be recomputed from the
--     loco's own route + distance in one line, so a save/reload just builds two fresh
--     ones. That is strictly less code than a modData round-trip AND strictly more
--     robust: there is no version of this that can load wrong.
--   * every filter that already knows about our animals works on these too, because they
--     are `rr_`-prefixed: RR_Crush skips them, the obstacle sweep skips them, the animal
--     context menu and the V radial refuse them, the joypad interact ignores them.
--
-- THE PIN. Position is the rail point at (loco distance +/- halfWheelbase) -- which is
-- exactly the point the carbody's chord is drawn between, so the truck cannot drift out
-- from under the pin it carries. It is NOT centre + hw*forward: on a corner the chord is
-- shorter than the arc, and that difference is the overhang a real loco has.
--
-- NO setZ TRICKERY. The truck mesh has its origin on the RAILHEAD, like the body's, so
-- both models take the same Z and there is nothing to raise. (Raising an IsoAnimal by Z
-- crashes the game -- docs/ENGINE_NOTES.md. It would have been the obvious thing to try.)
--***********************************************************************

print("[Railroader] RR_Trucks.lua: loading...")
require("Railroader/RR_Spline")
require("Railroader/RR_Body")
require("Railroader/RR_Truck")

local Trucks = {}
Trucks.TYPE    = "rr_truck"
Trucks.enabled = true       -- master on/off. false => the loco draws without trucks.
Trucks._breed  = nil        -- cached breed object

--------------------------------------------------------------------------
-- Resolve (and cache) a real breed. A nil breed NPEs inside IsoAnimal.init.
--------------------------------------------------------------------------
local function truckBreed()
    if Trucks._breed then return Trucks._breed end
    local adef = AnimalDefinitions and AnimalDefinitions.getDef(Trucks.TYPE)
    if not adef then return nil end
    local ok, breed = pcall(function() return adef:getRandomBreed() end)
    if ok then Trucks._breed = breed end
    return Trucks._breed
end

--------------------------------------------------------------------------
-- Spawn one truck entity at a world point.
--------------------------------------------------------------------------
local function spawnTruck(cell, x, y, z)
    local breed = truckBreed()
    if not breed then return nil end
    local animal
    local ok = pcall(function()
        animal = addAnimal(cell, math.floor(x), math.floor(y), math.floor(z),
                           Trucks.TYPE, breed)
    end)
    if not ok or not animal then return nil end
    pcall(function() animal:addToWorld() end)
    pcall(function() animal:setGodMod(true) end)
    -- IsoAnimal keeps its own huntable health that godMod does NOT protect, and a truck
    -- stands where cars get thrown and zombies get run over. setIsInvincible (the IsoAnimal
    -- setter) writes the field; setInvincible (the IsoGameCharacter one) is capability-gated
    -- and silently forces it back to false for a plain animal. Same trap as the loco.
    pcall(function() animal:setIsInvincible(true) end)
    -- Hold the wander AI. Unlike a collider segment we WANT blockMovement: a truck has no
    -- push to preserve (collidable=false), so the strongest available brake on the AI is
    -- the right one. It auto-clears after ~8 s in BaseAnimalBehavior.wanderIdle, so it is
    -- re-asserted every tick in update() below, exactly as the loco does it.
    local behavior
    pcall(function() behavior = animal:getBehavior() end)
    if behavior then pcall(function() behavior:setBlockMovement(true) end) end
    -- Not huntable, not petted, not talked to: the rr_ prefix already keeps our menus off
    -- it, and setNpc suppresses the bump-fall the player would otherwise take walking into
    -- one (IsoMovingObject.separate gates that on !isNpc). Inert otherwise -- nothing in
    -- B42 simulates an AIComponent.
    pcall(function() animal:setNpc(true) end)
    return animal, behavior
end

--------------------------------------------------------------------------
-- Where the trucks belong THIS tick, in world coords, with the same calibration and
-- world offset the carbody got.
--
-- Returns two tables { x, y, z, dirX, dirY, heading }, front (the +distance end) first.
--
-- A DERAILED loco is off the spline entirely: its body sits at a frozen pose with no rail
-- under it, so the trucks are hung off the BODY frame instead and share its heading. A
-- wreck is rigid -- articulating it would be the one moment the effect looks like a bug.
--------------------------------------------------------------------------
local function truckTargets(rec)
    local pose = rec.lastPose
    if not pose then return nil end

    local size = 0.7
    if rec.animal then pcall(function() size = rec.animal:getAnimalSize() end) end
    local hw = RR.Body.halfWheelbase(size)

    if rec.derailed or not rec.route then
        local fx, fy = pose.dirX or 0, pose.dirY or -1
        if fx == 0 and fy == 0 then fx, fy = 0, -1 end
        local h = RR.Geom.heading2d(0, 0, fx, fy)
        local out = {}
        for i, sign in ipairs({ 1, -1 }) do
            out[i] = { x = pose.x + sign * hw * fx, y = pose.y + sign * hw * fy, z = pose.z,
                       dirX = fx, dirY = fy, heading = h }
        end
        return out[1], out[2]
    end

    local axle = RR.Body.truckAxleHalf(size)
    local f, r = RR.Spline.truckPoses(rec.route, rec.distance, hw, axle)

    -- The world offset the demo route carries, and then the per-direction render alignment
    -- offset. Both are taken from the BODY: the calibration exists to put the drawn model on
    -- the drawn rails, and a truck that calibrated itself off its own heading would slide
    -- against the frame it belongs to every time the two disagreed -- i.e. on every corner,
    -- which is the only place anybody will be looking.
    local ox, oy, oz = (rec.ox or 0), (rec.oy or 0), (rec.oz or 0)
    if RR.Calib and RR.Calib.offsetFor then
        local cx, cy, cz = RR.Calib.offsetFor(pose.dirX or 0, pose.dirY or 0)
        ox, oy, oz = ox + cx, oy + cy, oz + (cz or 0)
    end
    for _, p in ipairs({ f, r }) do
        p.x, p.y, p.z = p.x + ox, p.y + oy, p.z + oz
    end
    return f, r
end

--------------------------------------------------------------------------
-- onSpawn: build the pair. Called from RR_TrainEntity right after the loco is posed.
--------------------------------------------------------------------------
function Trucks.onSpawn(rec)
    if not (Trucks.enabled and rec and rec.animal and RR.Body and RR.Spline) then return end
    Trucks.onRemove(rec)                                  -- never leave a pair behind
    if not (AnimalDefinitions and AnimalDefinitions.getDef(Trucks.TYPE)) then
        print("[Railroader] RR_Trucks: rr_truck type not defined; the loco will draw without trucks")
        return
    end
    local cell = getCell()
    if not cell then return end
    local f, r = truckTargets(rec)
    if not f then return end

    rec.trucks = {}
    for _, t in ipairs({ f, r }) do
        local a, behavior = spawnTruck(cell, t.x, t.y, t.z)
        if a then
            -- heading = nil means "no previous frame": the first pin takes the geometric
            -- heading as it is instead of sweeping into it from due east.
            table.insert(rec.trucks, { animal = a, behavior = behavior, heading = nil })
        end
    end
    if #rec.trucks ~= 2 then
        print("[Railroader] RR_Trucks: spawned " .. #rec.trucks .. " of 2 trucks")
    end
end

--------------------------------------------------------------------------
-- update: re-pin both trucks. Called every tick from RR_TrainEntity.onTick AFTER the
-- loco's own pose has been applied (rec.lastPose must be this tick's).
--------------------------------------------------------------------------
function Trucks.update(rec, dt)
    if not rec then return end
    -- Switched off mid-session (console): take the pair away rather than leave two trucks
    -- standing where the loco last was. Switching back on re-spawns them on the next spawn
    -- or reload; RR.Trucks.setEnabled does both halves at once.
    if not Trucks.enabled then
        if rec.trucks then Trucks.onRemove(rec) end
        return
    end
    if not rec.trucks then return end
    local f, r = truckTargets(rec)
    if not f then return end
    local targets = { f, r }

    local pose = rec.lastPose
    local bodyH = pose and RR.Geom.heading2d(0, 0, pose.dirX or 0, pose.dirY or 0)
    local rate = RR.Truck.chaseRate(rec.speed)

    for i, t in ipairs(rec.trucks) do
        local want = targets[i]
        local a = t.animal
        if a and want then
            -- Aim at the rail, but no further from the body than a locomotive can fold
            -- (MAX_YAW_VS_BODY -- see RR_Truck: the honest 22.5 degrees our 45-degree rail
            -- kinks ask for is not a thing any truck does).
            local target = RR.Truck.targetHeading(bodyH, want.heading)
            -- Chase it at a budget measured in travel, not in seconds -- the geometry
            -- crosses a node in 2.8 tiles however fast the loco is going, so a per-second
            -- limit fell behind above ~7 m/s and did its turning after the corner.
            t.heading = RR.Truck.stepHeading(t.heading, target, dt, rate)
            -- ...and clamp what is actually DRAWN, because the body turns too: through a
            -- corner it sweeps 45 degrees over its wheelbase, and a truck still catching up
            -- would open the same folded angle from the other side.
            t.heading = RR.Truck.targetHeading(bodyH, t.heading)
            if bodyH then
                local yaw = math.abs(RR.Truck.yawVsBody(bodyH, t.heading))
                if yaw > (t.peakYaw or 0) then t.peakYaw = yaw end
            end
            local dx, dy = RR.Truck.dirFrom(t.heading)

            pcall(function() a:setX(want.x) end)
            pcall(function() a:setY(want.y) end)
            pcall(function() a:setZ(want.z) end)
            pcall(function() a:setTargetAndCurrentDirection(dx, dy) end)
            -- Defeat the inherited character culling, exactly as the loco does. A model's
            -- render alpha is judged by TestIfSeen against its ORIGIN SQUARE alone, and a
            -- truck's origin is ~5 tiles from the cab -- so without this the trucks would
            -- fade out on their own whenever that one square fell outside the driver's
            -- vision cone, which is most of the time when he is looking the other way.
            pcall(function() a:setAlphaAndTarget(1.0) end)
            -- setBlockMovement auto-clears after ~8 s; re-assert it like the loco's.
            if t.behavior then pcall(function() t.behavior:setBlockMovement(true) end) end
        end
    end
end

--------------------------------------------------------------------------
-- onRemove: despawn the pair (from TrainEntity.clear / derail teardown / respawn).
--------------------------------------------------------------------------
function Trucks.onRemove(rec)
    if not (rec and rec.trucks) then return end
    for _, t in ipairs(rec.trucks) do
        if t.animal then
            pcall(function() t.animal:removeFromWorld() end)
            pcall(function() t.animal:removeFromSquare() end)
        end
    end
    rec.trucks = nil
end

--------------------------------------------------------------------------
-- sweepOrphans: remove stray rr_truck animals no live loco owns.
--
-- A truck is a real IsoAnimal, so PZ saves and reloads it whether we want that or not --
-- and on reload nothing re-pins it, so it would sit in the world where the loco used to
-- be while a fresh pair spawns under the real one. We cannot stop the engine persisting
-- them; we can refuse to adopt them. Anything of our type that is not in a current
-- rec.trucks pair is garbage, including everything a pre-3.C save has never heard of.
-- Called from RR_TrainEntity.reacquire (on load and on the periodic scan), like the
-- collider's sweep.
--------------------------------------------------------------------------
function Trucks.sweepOrphans()
    local cell = getCell()
    if not cell then return 0 end
    local list = cell:getAnimals()
    if not list then return 0 end

    local owned = {}
    local active = (RR and RR.TrainEntity and RR.TrainEntity.active) or {}
    for _, rec in ipairs(active) do
        if rec.trucks then
            for _, t in ipairs(rec.trucks) do
                if t.animal then
                    local id; pcall(function() id = t.animal:getAnimalID() end)
                    if id then owned[id] = true end
                end
            end
        end
    end

    local n = 0
    for i = 0, list:size() - 1 do
        local a = list:get(i)
        local ty; pcall(function() ty = a:getAnimalType() end)
        if ty == Trucks.TYPE then
            local id; pcall(function() id = a:getAnimalID() end)
            if not (id and owned[id]) then
                pcall(function() a:removeFromWorld() end)
                pcall(function() a:removeFromSquare() end)
                n = n + 1
            end
        end
    end
    if n > 0 then print("[Railroader] trucks: swept " .. n .. " orphaned truck(s)") end
    return n
end

--------------------------------------------------------------------------
-- Console: turn the whole thing on or off on a live world, both halves. Off despawns
-- every pair (the next tick would anyway); on builds one under each loco immediately, so
-- the switch does not wait for a reload. The point of it is A/B: look at a corner with
-- the trucks articulated and again with them off, without restarting.
--------------------------------------------------------------------------
function Trucks.setEnabled(v)
    Trucks.enabled = v and true or false
    local active = (RR and RR.TrainEntity and RR.TrainEntity.active) or {}
    for _, rec in ipairs(active) do
        if Trucks.enabled then Trucks.onSpawn(rec) else Trucks.onRemove(rec) end
    end
    if not Trucks.enabled then Trucks.sweepOrphans() end
    print("[Railroader] truck articulation " .. (Trucks.enabled and "ON" or "off")
          .. " (" .. #active .. " loco(s))")
end

--------------------------------------------------------------------------
-- Console: is the articulation actually doing anything? Prints each truck's yaw
-- against the carbody -- 0 on a straight, and it should swing several degrees either
-- side while a corner passes under the loco.
--------------------------------------------------------------------------
-- The PEAK column is the tuning instrument: drive a corner, then call this. It is the
-- largest yaw either truck reached since the last report, so it says whether the clamp is
-- what you are looking at (peak == MAX_YAW_VS_BODY, the corner is being absorbed) or the
-- geometry is (peak below it, the rails never asked for that much). Reading it resets it.
function Trucks.report()
    local active = (RR and RR.TrainEntity and RR.TrainEntity.active) or {}
    print(string.format("[Railroader] trucks: enabled=%s, %d loco(s); clamp %.1f deg vs body, "
          .. "chase %.0f deg/tile (floor %.0f deg/s)",
          tostring(Trucks.enabled), #active, RR.Truck.MAX_YAW_VS_BODY,
          RR.Truck.YAW_PER_TILE, RR.Truck.MIN_YAW_RATE))
    for i, rec in ipairs(active) do
        local pose = rec.lastPose
        local bh = pose and RR.Geom.heading2d(0, 0, pose.dirX or 0, pose.dirY or 0)
        local n = rec.trucks and #rec.trucks or 0
        print(string.format("  loco #%d: %s, %d truck(s), speed %.2f m/s%s",
              i, tostring(rec.label or rec.routeId), n, rec.speed or 0,
              rec.derailed and " [DERAILED -- rigid]" or ""))
        if rec.trucks and bh then
            for k, t in ipairs(rec.trucks) do
                print(string.format("    truck %s: yaw vs body %+6.2f deg   (peak since last report %5.2f)",
                      k == 1 and "front" or "rear ",
                      t.heading and RR.Truck.yawVsBody(bh, t.heading) or 0,
                      t.peakYaw or 0))
                t.peakYaw = 0
            end
        end
    end
end

RR = RR or {}
RR.Trucks = Trucks
print("[Railroader] RR_Trucks.lua: loaded OK (RR.Trucks.report() = truck yaw vs body; "
      .. "RR.Trucks.setEnabled(false) = A/B against the old rigid look).")

return Trucks
