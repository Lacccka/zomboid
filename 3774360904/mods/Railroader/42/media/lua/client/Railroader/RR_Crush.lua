--***********************************************************************
-- Railroader / RR_Crush  -- run zombies AND animals over with the locomotive (Task 1.H)
--
-- Wild animals (deer/rabbit/...) are handled like a car hitting them: mark roadkill
-- + spawn the ground carcass (IsoDeadBody). The vanilla roadKill flag makes the
-- corpse butcher for only a few hunger units of meat -- no custom meat math needed.
-- (getAnimals() also holds our rr_loco/rr_collider -- those are skipped by prefix.)
--
-- The collider (RR_Collider) keeps zombies from passing THROUGH the loco; this
-- layer kills the ones the moving loco runs INTO. We cannot use the vanilla
-- vehicle-hit path (IsoGameCharacter.onHitByVehicle needs a BaseVehicle for
-- CombatManager.checkPVP, and there is none -- locked decision), so we reproduce
-- the OUTCOME with the public zombie methods that DO work without a vehicle:
--   * IsoZombie.knockDown(hitFromBehind)     -- stagger/floor, chance of crawler
--   * IsoGameCharacter.Kill(killer)          -- death, credited to the driver
-- Verified in the B42 decompile (see docs/ENGINE_NOTES.md "crush zombies").
--
-- Each tick, for each active loco, we gate on |speed| (a PARKED loco must not be
-- lethal -- it is just a wall), gather the cell's zombies, cull to the footprint
-- bbox, and for each body inside the RR.Body footprint apply the speed tier from
-- the PURE RR.Body policy (knockDown below V_KILL, Kill at/above it). A per-record
-- "struck" set dedups knockdowns so we hit each body once per contact (kills need
-- no dedup -- a dead body drops out next tick). Ragdoll "fly-off" is deliberately
-- NOT here: it is a Bullet-physics effect tied to a vehicle collision body and is
-- single-player-only -- infeasible without Java (see docs/CRUSH_DESIGN.md).
--
-- Lifecycle mirrors RR.Collider: RR_TrainEntity.onTick calls Crush.update per rec
-- AFTER the pose/speed is updated. Crush.enabled=false disables the whole system.
--***********************************************************************

print("[Railroader] RR_Crush.lua: loading...")
require("Railroader/RR_Body")

local Crush = {}
Crush.enabled  = true
Crush.animals  = true          -- also run over wild animals (deer/rabbit/...) as roadkill
Crush.OWN_PREFIX = "rr_"       -- skip our OWN animals (rr_loco, rr_truck, rr_collider)
-- Seconds a loco keeps counting as "rolling" after it drops below V_ROLL, for the
-- purpose of finishing bodies already down in the gauge. This closes a real race:
-- a zombie does NOT become isOnFloor() at the moment it is hit. The floor flag is
-- an ANIM event -- staggerback-knockeddown/staggerAndFall.xml fires SetOnFloor=true
-- at TimePc 0.5, i.e. halfway through Zombie_StaggerBackFall, and the ragdoll branch
-- (staggerback/fromLeft_RAG|fromRight_RAG, gated on canRagdoll) only reaches the
-- floor when ZombieFallDownState.enter runs. That is ~0.4-0.8 s, while the air brake
-- (BRAKE_MAX 180 kN on ~112 t, ~1.6 m/s^2) takes the loco from 2 m/s through the whole
-- V_ROLL..V_MIN window in about half a second. So the loco CAN stop before the body it
-- just floored is flagged down -- and then, without this grace, the keep-out clamp
-- would slide the body out from under the frame instead. A wheel that came to rest ON
-- someone has still run them over. Long enough to cover the fall, short enough that a
-- zombie floored beside a LONG-parked loco is still only walled off, never killed.
Crush.ROLL_GRACE = 1.0

local function sgn(x)
    if x > 0 then return 1 elseif x < 0 then return -1 else return 0 end
end

--------------------------------------------------------------------------
-- Is this zombie ALREADY on the ground? A body down in the gauge is under the
-- wheels, not against the frame, so RR.Body.crushEffect gives it the harsher
-- `downed` policy (kill at any rolling speed) -- see RR_Body.CRUSH.
--
-- Deliberately reads only REAL engine state, never our own "we knocked it down this
-- tick" bookkeeping: IsoZombie.knockDown sets knockedDown/staggerBack immediately
-- (:4871-4877) but the body reaches the floor a beat later, when the state machine
-- enters ZombieFallDownState and that state's enter() calls setOnFloor(true). Keying
-- on isKnockedDown would therefore kill on the very next tick and collapse the
-- knockdown tier into "everything dies"; keying on isOnFloor spends the fall, which
-- is the window in which a zombie clipped by a corner can end up clear of the
-- footprint and survive as a crawler -- the designed outcome.
--   isOnFloor()   -- floored, and it STAYS true through ZombieGetUpState.enter
--   isCrawling()  -- a permanent crawler, never standing to begin with
--   isGettingUp() -- ZombieGetUpState: literally the reported bug, standing up
--                    inside the hull. Redundant with isOnFloor, kept as a belt.
-- All three are public in B42 (IsoMovingObject:2301, IsoZombie:4775, IsoZombie:5223).
--------------------------------------------------------------------------
function Crush.isDown(z)
    if not z then return false end
    local down = false
    pcall(function()
        down = z:isOnFloor() or z:isCrawling() or z:isGettingUp()
    end)
    return down and true or false
end

--------------------------------------------------------------------------
-- Does this loco count as rolling over what is under it? True while |v| >= V_ROLL
-- and for ROLL_GRACE seconds after (see above). READ-ONLY -- the grace timer itself
-- is advanced once per tick by Crush.update.
--
-- RR_Collider calls this to decide which downed bodies to hand over instead of
-- clamping. It runs BEFORE Crush.update in the tick, so it sees last tick's timer --
-- one frame of staleness against a one-second window, which does not matter.
--------------------------------------------------------------------------
function Crush.rolling(rec, av)
    -- Crush off => it will kill nobody, so there is nothing to yield: the clamp is
    -- the only wall again, downed bodies included. (Also stops a stale _rollGrace
    -- left behind by Crush.enabled=false from muting the clamp forever.)
    if not Crush.enabled then return false end
    if not (rec and RR and RR.Body and RR.Body.CRUSH) then return false end
    av = av or math.abs(rec.speed or 0)
    if av >= RR.Body.CRUSH.V_ROLL then return true end
    return (rec._rollGrace or 0) > 0
end

--------------------------------------------------------------------------
-- Roadkill one animal exactly as a car does (verified in the B42 decompile against
-- IsoAnimal.Hit(vehicle,...)): mark it roadkill, splatter blood, then spawn the
-- ground carcass. IsoDeadBody.new(a, false) == (a, false, true): adds the corpse to
-- the square + fires OnDeadBodySpawn AND removes the live animal (the canonical
-- vanilla ground-corpse idiom, e.g. RDSRatInfested). The roadKill flag is copied
-- onto the corpse modData by setAnimalBodyData, and ButcheringUtil then halves the
-- meat + drops meatRatio to 0.2-0.4 -> only a few hunger units of meat, as asked.
--------------------------------------------------------------------------
local function roadkillAnimal(a, speed)
    pcall(function() a:setIsRoadKill(true) end)
    pcall(function() a:addBloodFromVehicleImpact(speed) end)   -- same splatter as a car
    pcall(function() IsoDeadBody.new(a, false) end)            -- ground carcass; removes the animal
end

--------------------------------------------------------------------------
-- Vanilla run-over foley for a zombie, applied JUST BEFORE Kill (the body is still
-- alive, so its own FMOD emitter is certainly still up).
--
-- WHY THIS EXISTS -- owner report 2026-08-02: "rolling over a body makes no death
-- sound". It technically does: Kill -> onKilled -> DoDeath -> doDeathSplatterAndSounds
-- (IsoGameCharacter:2046) -> playDeadSound() -> "HeadSmash" on the zombie's emitter,
-- and the doDeathSound flag is NOT consumed by an ordinary knockdown (the anim event
-- that clears it, PlayDeathSound, exists only in the knife-death nodes). What is
-- missing is everything AROUND it. A standing body killed at speed is knockDown-ed
-- first, so the ear gets the whole fall -- staggerAndFall.xml's BodyHitGround, the
-- slam, and our VehicleHitCharacter thud on top. A body already lying down has no
-- fall to play, so one short HeadSmash under a running diesel is the entire event.
-- Vanilla's own car fills that gap with two calls we were never making on zombies:
--   * addBloodFromVehicleImpact(speed)  -- IsoGameCharacter:12800, inside the vehicle
--     damage path. We already do the animal equivalent in roadkillAnimal and simply
--     never did it for zombies. It self-gates on speed (Rand.Next(10) > speed => no
--     blood), which is why a downed kill passes V_GORE rather than the real |v|: the
--     wheel does not care how slowly it arrived.
--   * playBloodSplatterSound() -- IsoGameCharacter:14515, the "BloodSplatter" squelch.
--     Vanilla locks it behind bare-hands-stomping-a-floored-zombie and so never gives
--     it to a car; it is exactly the sound of a body being pulped on the ground, which
--     is what a 112 t wheel does, so the DOWNED kill gets it. Capped at one per tick --
--     a crowd going under together would otherwise stack a wall of squelches.
--------------------------------------------------------------------------
local function runOverFoley(z, av, down, splatBudget)
    -- Blood on the body, as a car leaves. Downed => book it as a V_GORE hit.
    local bloodV = av
    if down then bloodV = math.max(av, RR.Body.CRUSH.V_GORE) end
    pcall(function() z:addBloodFromVehicleImpact(bloodV) end)
    if down and splatBudget > 0 then
        pcall(function() z:playBloodSplatterSound() end)
        return splatBudget - 1
    end
    return splatBudget
end

--------------------------------------------------------------------------
-- The IsoGameCharacter to credit a kill to: the local player IF they are driving
-- THIS loco (so zombie-kill count + XP land), else nil (autopilot/debug cruise --
-- Kill(nil) is a valid unattributed death).
--------------------------------------------------------------------------
local function killerFor(rec)
    if RR and RR.Ride and RR.Ride.current == rec then
        local p; pcall(function() p = getPlayer() end)
        return p
    end
    return nil
end

--------------------------------------------------------------------------
-- update: run the crush sweep for one loco record. Called each tick from
-- RR_TrainEntity.onTick after the loco pose/speed is applied.
--------------------------------------------------------------------------
function Crush.update(rec, dt)
    if not (Crush.enabled and RR and RR.Body and rec and rec.animal) then return end

    local v  = rec.speed or 0
    local av = (v < 0) and -v or v
    -- Advance the "recently rolling" grace timer (Crush.ROLL_GRACE) BEFORE the gate,
    -- so it keeps ticking down on a loco that has come to rest.
    local rolling = av >= RR.Body.CRUSH.V_ROLL
    if rolling then
        rec._rollGrace = Crush.ROLL_GRACE
    else
        local g = (rec._rollGrace or 0) - (dt or 0)
        rec._rollGrace = (g > 0) and g or 0
    end
    -- `grace` = stopped, but only just: finish off whatever is already down under the
    -- frame. A standing body is unaffected -- crushEffect(av<V_ROLL, false) is "none".
    local grace = (not rolling) and rec._rollGrace > 0

    -- Standing still (and long enough for the grace to lapse): no crush at all. Drop
    -- any stale contact set so the next real hit knocks down again rather than being
    -- skipped as "already struck".
    -- The rolling gate is V_ROLL, not V_MIN: between the two the loco still runs over
    -- a body that is already DOWN (RR_Body.CRUSH), it just isn't fast enough to floor
    -- a standing one -- crushEffect returns "none" for those, so they fall through to
    -- the collider's keep-out clamp exactly as before.
    if not (rolling or grace) then
        rec._struck = nil
        return
    end

    local pose = rec.lastPose
    if not pose then return end

    local size = 0.7
    pcall(function() size = rec.animal:getAnimalSize() end)

    -- Cull radius around the loco centre: half the body length + a margin. A body
    -- outside this circle cannot be inside the oriented footprint box.
    local len   = RR.Body.extents(size)
    local reach = len * 0.5 + 2.0
    local reach2 = reach * reach

    local cell = getCell()
    if not cell then return end
    local zlist = cell:getZombieList()
    if not zlist then return end

    local killer = killerFor(rec)
    local struck = rec._struck or {}
    local seen   = {}
    local killedThisTick = false
    local splatBudget = 1          -- at most one BloodSplatter squelch per tick

    for i = 0, zlist:size() - 1 do
        local z = zlist:get(i)
        local dead = true
        pcall(function() dead = z:isDead() end)
        if z and not dead then
            local zx, zy = 0, 0
            pcall(function() zx = z:getX(); zy = z:getY() end)
            local dx, dy = zx - pose.x, zy - pose.y
            if dx * dx + dy * dy <= reach2 then
                local inside = RR.Body.contains(pose, size, zx, zy)
                if inside then
                    local id
                    pcall(function() id = z:getID() end)
                    if id then seen[id] = true end
                    local down = Crush.isDown(z)
                    -- Inside the grace the loco has already stopped, so |v| alone would
                    -- read "none". A body it stopped ON was still run over, so the
                    -- DOWNED policy is evaluated at the threshold speed. Standing bodies
                    -- keep the real |v| and are therefore untouched: a standstill is
                    -- never lethal to them.
                    local effAv = (down and grace) and RR.Body.CRUSH.V_ROLL or av
                    local effect = RR.Body.crushEffect(effAv, down)
                    if effect == "kill" or effect == "gib" then
                        splatBudget = runOverFoley(z, av, down, splatBudget)
                        -- Only pose a body that is still on its feet; one already on
                        -- the floor is in the right pose and re-knocking it would
                        -- restart the fall under the wheels.
                        if not down then pcall(function() z:knockDown(false) end) end
                        pcall(function() z:Kill(killer, effect == "gib") end)  -- bGory at speed
                        killedThisTick = true
                    elseif effect == "knockdown" then
                        if not (id and struck[id]) then
                            if id then struck[id] = true end
                            pcall(function() z:knockDown(false) end)
                        end
                    end
                end
            end
        end
    end

    -- Wild animals (deer/rabbit/...) -> roadkill, exactly like a car. getAnimals()
    -- ALSO holds OUR rr_loco visual + rr_collider segments, so skip the OWN_PREFIX
    -- types. TWO-PHASE: gather victims first, THEN kill -- roadkillAnimal removes the
    -- animal from the cell list (IsoDeadBody ctor), and mutating the list mid-iterate
    -- would skip/OOB.
    -- Still gated on V_MIN, NOT the sweep's V_ROLL: lowering the whole sweep to
    -- V_ROLL was for bodies already down in the gauge, and wild animals have no
    -- down/standing tiers -- anything in the box is roadkill. Without this an
    -- inching loco would silently start killing deer.
    if Crush.animals and IsoDeadBody and av >= RR.Body.CRUSH.V_MIN then
        local alist = cell:getAnimals()
        if alist then
            local victims
            for i = 0, alist:size() - 1 do
                local a = alist:get(i)
                if a then
                    local t; pcall(function() t = a:getAnimalType() end)
                    local mine = t and string.sub(t, 1, #Crush.OWN_PREFIX) == Crush.OWN_PREFIX
                    local dead = true
                    pcall(function() dead = a:isDead() end)
                    if a and not mine and not dead then
                        local ax, ay = 0, 0
                        pcall(function() ax = a:getX(); ay = a:getY() end)
                        local dx, dy = ax - pose.x, ay - pose.y
                        if dx * dx + dy * dy <= reach2 and RR.Body.contains(pose, size, ax, ay) then
                            victims = victims or {}
                            victims[#victims + 1] = a
                        end
                    end
                end
            end
            if victims then
                for _, a in ipairs(victims) do roadkillAnimal(a, av) end
                killedThisTick = true
            end
        end
    end

    -- Impact thud on a kill, throttled so a whole crowd going under in one tick is
    -- a single thud rather than a wall of overlapping one-shots.
    rec._crushSndCd = (rec._crushSndCd or 0) - (dt or 0)
    if killedThisTick and rec._crushSndCd <= 0 then
        rec._crushSndCd = (RR.SoundConfig and RR.SoundConfig.CRUSH_MIN_INTERVAL) or 0.12
        if RR.Sound and RR.Sound.crushImpact then pcall(function() RR.Sound.crushImpact(rec) end) end
    end

    -- Forget bodies no longer under the loco so a fresh contact re-triggers.
    for id in pairs(struck) do
        if not seen[id] then struck[id] = nil end
    end
    rec._struck = struck
end

RR = RR or {}
RR.Crush = Crush
print("[Railroader] RR_Crush.lua: loaded OK (V_MIN=" .. RR.Body.CRUSH.V_MIN
      .. " V_KILL=" .. RR.Body.CRUSH.V_KILL .. " m/s; animals="
      .. tostring(Crush.animals) .. ").")

return Crush
