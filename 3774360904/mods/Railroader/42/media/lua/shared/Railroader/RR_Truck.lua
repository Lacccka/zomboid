--***********************************************************************
-- Railroader / RR_Truck  -- truck (bogie) yaw, PURE (Task 3.C)
--
-- The geometry of where a truck is and which way it points lives in RR_Spline.truckPoses
-- (it is the same chord trick as the carbody, one scale down). What lives HERE is
-- everything that stands between that geometry and what gets drawn: HOW FAR a truck may
-- be turned against the body, and HOW FAST it may get there.
--
-- Both exist because the geometry is not usable raw. Our routes follow the map's rails,
-- and the map's rails kink by a hard 45 degrees at a node -- there is no curve, just a
-- corner. Riding it honestly, a truck ends up folded 22.5 degrees against the frame it
-- carries, which no locomotive on earth does; and it gets there in a tenth of a second at
-- track speed, which reads as a glitch rather than as a bogie swinging. So the drawn
-- heading is CLAMPED to a believable angle (MAX_YAW_VS_BODY) and CHASES its target at a
-- bounded rate (YAW_PER_TILE). Each constant carries its own reasoning below.
--
-- None of this is a physical simulation: no torque, no inertia, no friction. None of that
-- would be visible at 800x300 pixels and all of it would need tuning nobody can measure
-- in game.
--
-- WHY NOT FIX THE SPLINE INSTEAD. Filleting the route polyline at every node would give
-- a genuinely continuous tangent and this file would not exist. It would also move the
-- centreline off the rail tiles it was authored from, and the centreline is what the
-- footprint, the collider rectangle, the crush sweep and the obstacle gauge are all built
-- on. That is a much larger and riskier change than a chase filter on a visual, and it is
-- not required by anything else. See docs/ARCHITECTURE.md.
--
-- No engine calls (Kahlua-safe, unit-tested by tests/run_tests.lua).
--***********************************************************************

local Geom = RR and RR.Geom or require("Railroader/RR_Geom")

local Truck = {}

-- HOW FAR A TRUCK MAY BE TURNED AGAINST THE CARBODY, degrees. This is the knob that
-- decides what the whole thing looks like, and it is a deliberate lie.
--
-- A real GP7 truck swings about **6-8 degrees** at its limit, and only on a curve far
-- sharper than anything a railroad would lay for a road engine. Our track cannot offer
-- that: it follows the map's rails, which turn by a hard 45 degrees at a node, and the
-- honest geometry (the chord under the truck against the chord under the body) asks for
-- **22.5 degrees** at every corner. Drawn honestly the truck sits on the rails and looks
-- wrong -- no locomotive folds like that.
--
-- So the truck is NOT allowed to reach the rails. It is allowed a believable swing, and
-- the rest of the corner is absorbed by the body's own chord, exactly as before. The
-- trade is deliberate (owner, 2026-08-07): a truck that is a few degrees off its rail is
-- invisible at this camera -- it is 40 pixels tall, under a frame that overhangs it --
-- while the FACT of the swing is what the eye reads as the locomotive turning smoothly
-- rather than pivoting. We buy the illusion and pay in accuracy nobody can see.
--
-- 13 is the shipped compromise (owner, 2026-08-07, raised from the first pass's 10): well
-- over the prototype's limit, but the swing has to be seen from a camera that renders the
-- truck 40 pixels tall, and half of the corner is being absorbed by the body's chord in any
-- case. It is live-tunable from the console -- `RR.Truck.MAX_YAW_VS_BODY = 16` is read on
-- the next tick.
--     0    = rigid, the pre-3.C look (the trucks just point where the body points)
--     6-8  = the prototype's real limit
--     13   = shipped
--     22.5 = what our 45-degree rail kinks actually ask for
--     <0   = no clamp at all: full geometric truth, i.e. on the rails and folded 22.5 deg
Truck.MAX_YAW_VS_BODY = 13.0

-- HOW FAST it may get there -- in degrees per TILE OF TRAVEL, not per second.
--
-- This was per-second (110 deg/s) until 2026-08-07, and that was a real bug rather than a
-- badly chosen number: a truck's yaw is GEOMETRY, set by where its axles are, and the
-- geometry crosses a node in 2*axleHalf = 2.79 tiles of travel however fast the loco is
-- going. At 2 m/s that is 1.4 s and 110 deg/s is generous; at 20 m/s it is 0.14 s and the
-- corner needs 322 deg/s. So above about 7 m/s the limiter could not keep up and the truck
-- did its turning AFTER the corner, out on the straight -- which is exactly the "they don't
-- line up on some corners" report, and why the slow ones always looked right.
--
-- Per tile, the limit is speed-independent by construction: the same corner looks the same
-- at a crawl and at track speed, which is the point.
--
-- Read against the clamp above it is a rule of thumb: **the swing takes MAX_YAW_VS_BODY /
-- YAW_PER_TILE tiles of travel**, i.e. ~1.3 at the shipped pair -- ~5 frames at line speed
-- and ~40 at a crawl, so there is something to watch either way. There is plenty of
-- headroom: the corner itself lasts the body's whole wheelbase (10.95 tiles), so a swing
-- that needs one or two of them cannot lag out the far side, which is the failure this
-- constant is here to avoid. Raise it and the swing snaps in; lower it below ~3 and the
-- truck is still turning after the corner has gone.
Truck.YAW_PER_TILE = 10.0

-- ...with a floor in degrees per SECOND, for the case where there is no travel to measure
-- against: a parked loco whose route was re-anchored under it (a thrown switch), a spawn,
-- a reacquire. Without it a stopped truck would never close an error at all.
Truck.MIN_YAW_RATE = 60.0

-- Beyond this the heading SNAPS instead of sweeping. A truck is never legitimately 100
-- degrees away from where it belongs: that only happens when the loco was put somewhere
-- rather than driven there -- a spawn, a reacquire after a save, a debug teleport, a
-- reverse onto a branch. Sweeping through those would draw a truck spinning across the
-- yard for a second.
Truck.SNAP_DEG = 100.0

local PI, TWO_PI = math.pi, math.pi * 2

--------------------------------------------------------------------------
-- Signed smallest angle from a to b, radians, in [-pi, pi].
--------------------------------------------------------------------------
function Truck.angleDiff(a, b)
    local d = (b - a) % TWO_PI
    if d > PI then d = d - TWO_PI end
    return d
end

--------------------------------------------------------------------------
-- The heading a truck is ALLOWED to aim at: the rail's, pulled back to within
-- MAX_YAW_VS_BODY of the carbody's.
--
-- Applied twice per tick by RR_Trucks, and both are needed. Before the chase, so the
-- truck aims somewhere believable; and again to the CHASED result, because the body can
-- turn faster than the chase does -- during a corner the body sweeps 45 degrees over its
-- wheelbase, and a truck lagging behind that would open the same folded angle from the
-- other side. Clamping the drawn value makes the limit an actual guarantee.
--
--   maxDeg nil -> MAX_YAW_VS_BODY   |   0 -> rigid (take the body's heading)
--   maxDeg < 0 -> no clamp: the rail heading, whatever it is
--------------------------------------------------------------------------
function Truck.targetHeading(bodyHeading, railHeading, maxDeg)
    if maxDeg == nil then maxDeg = Truck.MAX_YAW_VS_BODY end
    if bodyHeading == nil or maxDeg < 0 then return railHeading end
    local lim = math.rad(maxDeg)
    local d = Truck.angleDiff(bodyHeading, railHeading)
    if d > lim then return bodyHeading + lim end
    if d < -lim then return bodyHeading - lim end
    return railHeading
end

--------------------------------------------------------------------------
-- The chase budget for this tick, in degrees per second, from the loco's speed.
-- Per TILE of travel (the geometry's own currency), floored so a standing loco still
-- converges. speed is m/s, and one tile is one metre, so it is also tiles/s.
--------------------------------------------------------------------------
function Truck.chaseRate(speed)
    local v = math.abs(speed or 0)
    local perTravel = Truck.YAW_PER_TILE * v
    if perTravel > Truck.MIN_YAW_RATE then return perTravel end
    return Truck.MIN_YAW_RATE
end

--------------------------------------------------------------------------
-- One frame of the chase. Returns the new heading in radians.
--   cur, target : radians (math convention, as RR_Geom.heading2d yields)
--   dt          : seconds
--   maxRate     : degrees per second (defaults to MIN_YAW_RATE; callers pass chaseRate)
--   snapDeg     : jump straight to target past this much error (defaults to SNAP_DEG)
--
-- cur == nil means "no previous frame" -- a truck that has just been spawned takes the
-- geometric heading as it is, so it never sweeps into place from due east.
--------------------------------------------------------------------------
function Truck.stepHeading(cur, target, dt, maxRate, snapDeg)
    if cur == nil then return target end
    if not dt or dt <= 0 then return cur end
    maxRate = maxRate or Truck.MIN_YAW_RATE
    snapDeg = snapDeg or Truck.SNAP_DEG

    local diff = Truck.angleDiff(cur, target)
    local snap = math.rad(snapDeg)
    if snap > 0 and math.abs(diff) >= snap then return target end

    local step = math.rad(maxRate) * dt
    if math.abs(diff) <= step then return target end
    return cur + (diff > 0 and step or -step)
end

--------------------------------------------------------------------------
-- Unit forward vector for a heading. The inverse of RR_Geom.heading2d, and the shape
-- IsoGameCharacter.setTargetAndCurrentDirection wants.
--------------------------------------------------------------------------
function Truck.dirFrom(heading)
    return math.cos(heading), math.sin(heading)
end

--------------------------------------------------------------------------
-- How far a truck is turned relative to the body it carries, in degrees, signed.
-- Only used by the debug readout and the tests -- but it is the number that says
-- whether any of this is working, so it is worth having a name.
--------------------------------------------------------------------------
function Truck.yawVsBody(bodyHeading, truckHeading)
    return math.deg(Truck.angleDiff(bodyHeading, truckHeading))
end

RR = RR or {}
RR.Truck = Truck

return Truck
