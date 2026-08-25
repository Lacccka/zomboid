--
-- True Companions - Nav (supervised traversal)
--
-- The engine's A* (PathFindBehavior2) plans the route; this module owns the parts
-- of WALKING that the engine gets wrong for a driven character. It is the fix for
-- "companions fall down the stairs" and "companions walk through windows".
--
-- Worth recording why this is not an entity-class problem: getPathFindBehavior2()
-- is declared on IsoGameCharacter -- javap finds it there and NOT on IsoZombie or
-- IsoPlayer -- so both classes drive the identical planner. Rebuilding companions
-- on IsoPlayer would have inherited exactly the same two bugs (and would have
-- needed a Java agent to ship, which rules it out anyway). The mover is the fix.
--
-- THE CENTRAL RULE, and the one that retires a whole family of bugs:
--
--     WHILE A COMPANION IS IN TRANSIT, NOTHING MAY ASSERT HER POSITION.
--
-- A companion crossing a stair or climbing a window occupies a fractional Z and a
-- square she is not "on" in any ordinary sense. This mod has several independent
-- systems that may each reposition her at any moment -- the Guardian stuck
-- watchdog, program arrival checks, BNSit/BNSleep pose pinning. Fire one of those
-- mid-stair and she is moved in X/Y while keeping a stair Z, i.e. left standing
-- over nothing, and she falls.
--
-- The concrete instance, for the record: BanditsNPCGuardian.lua:500 moved a stuck
-- companion with setX/setY and NO setZ and NO floor check. Its sibling at :536
-- does both and even carries a comment describing the resulting fall
-- ("teleporting to a floorless upper-level square dropped a companion through the
-- air"). Every reposition in the mod now goes through Nav.Teleport, which refuses
-- both cases.
--

BanditsNPC = BanditsNPC or {}
BanditsNPC.Nav = BanditsNPC.Nav or {}
local N = BanditsNPC.Nav

-- Edge classifications. INTERNAL KEYS -- never displayed, never translated.
N.EDGE = {
    OPEN    = "open",       -- walk straight through
    WINDOW  = "window",     -- IsoWindow, needs a climb
    FRAME   = "frame",      -- IsoWindowFrame, needs a climb
    THUMP   = "thump",      -- player-built window-like IsoThumpable
    FENCE   = "fence",      -- low fence, needs a vault
    STAIRS  = "stairs",     -- level change
    BLOCKED = "blocked",    -- impassable; route around it
}

local function nowMs()
    return (getTimestampMs and getTimestampMs()) or 0
end

-- ---------------------------------------------------------------------------
-- square safety
-- ---------------------------------------------------------------------------

-- A square is safe to STAND on only if it is unoccupied AND has a floor.
--
-- The floor test is not paranoia. An upper-level square with no floor object is
-- open air; putting a companion there drops her, and the fall damage surfaces as
-- an unexplained injury or a downed companion with no attacker.
function N.IsSafeSquare(sq)
    if sq == nil then return false end
    local ok = false
    pcall(function()
        ok = sq:isFree(false) and sq:getFloor() ~= nil
    end)
    return ok
end

-- ---------------------------------------------------------------------------
-- transit
-- ---------------------------------------------------------------------------

-- True while the companion is mid-traversal and MUST NOT be repositioned.
--
-- Covers explicit climbs (isClimbing, or a Climb* state) and stair transit, which
-- the engine expresses as a fractional Z rather than as a named state.
--
-- CALL THIS BEFORE ANY REPOSITION. That includes pose pinning: a positioned hold
-- that re-pins every tick will happily re-pin her onto a staircase.
-- FIX(BUG-043): THE CLIMB SIGNALS ARE NOW BOUNDED. THIS IS A SAFETY GUARD, NOT A KNOB.
--
-- BeginTraverse below now drives the engine's STATE MACHINE instead of calling a
-- player-only method that did nothing. That is the fix -- and it introduces a failure
-- mode strictly worse than the bug it replaces, so the guard ships with it, not after.
--
-- THE HAZARD, PRECISELY. Today a failed climb leaves her in ZombieIdleState, InTransit
-- reads false, and the Guardian watchdog flushes at 540 ticks and teleports at 1200 --
-- ugly, bounded, ~20 seconds. Once changeState actually lands, a climb that ENTERS the
-- state and never COMPLETES makes InTransit permanently true, and every caller stands
-- off forever:
--     Guardian.lua:913   resets st.x/st.y/st.acc/st.ticks EVERY dispatch, so the
--                        watchdog counter never reaches FLUSH, let alone PORT
--     Nav.Teleport:105   refuses outright, so "Bring here" cannot recover her either
--     Guardian.lua:263   ZFALL detection stands off
--     Actions.lua:191, :247   pose pinning stands off
-- That is not a 20-second freeze. It is a companion frozen for the rest of the save with
-- the entire watchdog disarmed, which is the worst outcome this project can ship.
--
-- SO THE CLIMB BRANCH GETS A DEADLINE AND THE STAIRS BRANCH DOES NOT.
-- 10 seconds is chosen against measurement, not taste. (The original justification here
-- said "the three climbs in logs/task016-test.txt held the state for at most FIVE
-- consecutive samples, so this is 2x the longest climb observed". THAT WAS WRONG and is
-- corrected below: those five-sample clusters were adjacent climbs plus sampling slop,
-- not one climb. Direct measurement puts a climb at ~1 second and the margin at ~10x, so
-- the conclusion held by a much wider margin than the arithmetic that produced it.)
-- It must not be tightened -- the cost of firing early is repositioning her MID-CLIMB,
-- which is the fall this whole module exists to prevent, and that is a much worse trade
-- than waiting a few extra seconds.
-- It must also stay under the watchdog's own horizon so recovery still happens at a
-- comparable time: PORT_TICKS 1200 is ~13.5s at the measured ~89 ticks/s, so a blown
-- deadline costs about 10 + 13.5 = ~24s to teleport against ~13.5s today. Bounded either
-- way, which is the only property that matters here.
--
-- STAIRS AND FRACTIONAL Z ARE DELIBERATELY LEFT UNBOUNDED. A companion standing on a
-- staircase square reports transit forever today and that is a real, separate hazard --
-- it is BUG-024's territory (every standability test we own returns true for a staircase)
-- and bundling it here would change behaviour nobody asked about. Note the consequence
-- honestly: the deadline only rescues a climb that keeps an INTEGRAL Z, which is exactly
-- the window case (measured z=0.0 throughout ClimbThroughWindowState). A stuck climb that
-- also holds a fractional Z still falls through to the Z check and stays in transit.
-- MEASURED 2026-08-22, AND THE MARGIN IS ~10x. DO NOT "OPTIMISE" THIS DOWNWARD.
--
-- The 2-5 second figure used to justify 10000 above was an OVER-ESTIMATE read off
-- 1-second trace sampling, and the real number is smaller by a factor of about five.
-- Measured directly by setting this constant to 1000 for one run and climbing repeatedly:
-- five successful climbs (78.5, 90.6, 99.6, 108.7, 120.8s), both directions, under
-- prog=NPCStay. **Each climb occupies exactly TWO consecutive samples, so a real climb
-- lasts about ONE SECOND.**
--
-- CONSEQUENCE: 10000 is roughly ten times the measured duration of the thing it is
-- guarding, not twice. The deadline never blew even at 1000, because the state clears
-- before it can be reached. Zero WATCH, zero NAV, zero ZFALL, no repositioning, no falls,
-- no stutter -- direct observation matched the trace exactly.
--
-- SO THE TEMPTATION WILL BE TO TIGHTEN THIS TOWARD 2000 OR 3000 AND IT MUST BE RESISTED.
-- The measured duration is the duration of a climb THAT WORKS. This constant does not
-- bound working climbs; it bounds a climb that has ALREADY FAILED to complete, and there
-- is no measurement of how long those take because none has ever been observed. Cutting
-- the margin buys nothing (a stuck companion waits 10s instead of 3s before the watchdog
-- takes over, and she is stuck either way) and risks everything (firing early repositions
-- her MID-CLIMB, which is the fall this module exists to prevent). An asymmetric bet with
-- a ~10x buffer on the safe side is the correct shape here.
--
-- WHAT THE RUN DID NOT PROVE: the EXPIRY BRANCH ITSELF IS STILL UNEXECUTED. Because no
-- climb ever got stuck, the fall-through after the deadline has never run. What is proven
-- is that the restructured InTransit does not break a working climb, and that the margin
-- is wide. The failure path remains theory.
local CLIMB_TRANSIT_MAX = 10000    -- ms; see above before changing
local climbSince = {}              -- uid -> nowMs() when a Climb* signal was first seen

function N.InTransit(zombie)
    if not zombie then return false end
    local transit = false
    pcall(function()
        -- BOTH climb signals, folded into one flag. isClimbing() used to return early on
        -- its own line, which would have let a stuck climb walk straight past the deadline.
        local climbing = false
        if zombie:isClimbing() then
            climbing = true
        else
            local sn = zombie.getCurrentStateName and zombie:getCurrentStateName()
            if type(sn) == "string" and string.find(sn, "Climb") then climbing = true end
        end

        local uid
        if BanditUtils and BanditUtils.GetCharacterID then
            uid = BanditUtils.GetCharacterID(zombie)   -- BanditUtils.lua:628
        end

        if climbing then
            -- NO ID MEANS TODAY'S BEHAVIOUR, UNBOUNDED, ON PURPOSE. Failing the other way
            -- would reposition a genuinely climbing companion, and a fall is worse than a
            -- freeze. The deadline is a backstop, not a guarantee.
            if not uid then transit = true; return end
            local t = nowMs()
            if not climbSince[uid] then climbSince[uid] = t; transit = true; return end
            if t - climbSince[uid] < CLIMB_TRANSIT_MAX then transit = true; return end
            -- Deadline blown: she is not climbing, she is STUCK in a climb state. Fall
            -- through and let the ordinary checks (and then the watchdog) have her.
        elseif uid then
            climbSince[uid] = nil
        end

        -- Fractional Z means the engine is interpolating her between levels.
        local z = zombie:getZ()
        if z and math.abs(z - math.floor(z + 0.5)) > 0.05 then transit = true; return end

        local sq = zombie:getSquare()
        if sq and sq:HasStairs() then transit = true end
    end)
    return transit
end

-- ---------------------------------------------------------------------------
-- the ONE teleport
-- ---------------------------------------------------------------------------

function N.Teleport(zombie, sq, why)
    if not zombie or not sq then return false end

    if N.InTransit(zombie) then
        -- Not an error, and deliberately silent. She is on a stair or mid-climb;
        -- whatever wanted to move her can want it again in a moment, when moving
        -- her is safe. Callers are expected to retry, not to escalate.
        return false
    end
    if not N.IsSafeSquare(sq) then
        -- THROTTLED (v0.75.20). Callers retry this every tick while a companion is stuck,
        -- so an unsafe destination produced one log line PER TICK PER COMPANION -- tens of
        -- thousands of identical lines in a session, which buries the errors the log is
        -- read for. Once every 10s per reason keeps the diagnostic and loses the flood.
        local now = (getTimestampMs and getTimestampMs()) or 0
        N._refusedAt = N._refusedAt or {}
        local k = tostring(why or "?")
        if now - (N._refusedAt[k] or 0) > 10000 then
            N._refusedAt[k] = now
            print("[BanditsNPC] Nav refused a teleport to an unsafe square (" .. k .. ")")
        end
        return false
    end

    local moved = false
    pcall(function()
        zombie:setX(sq:getX() + 0.5)
        zombie:setY(sq:getY() + 0.5)
        zombie:setZ(sq:getZ())
        -- RE-REGISTER HER ON THE DESTINATION SQUARE (v0.75.63).
        --
        -- setX/setY/setZ move the COORDINATES. They do not move the object between squares:
        -- getCurrentSquare() still returns the one she left, and the engine goes on treating
        -- that as the square she occupies. Send her upstairs that way and she is standing at
        -- z=1 while the engine believes she is on the ground-floor square -- so its own
        -- reconciliation finds nothing holding her up and drops her. That is the author's
        -- "she teleported to bed but kept falling through the floor to the floor below and
        -- died, then resurrected and went to sleep and kept falling through in a loop"
        -- (6 Aug): fall, die, immortality knocks her down instead, the routine sends her
        -- back to the bed, and round again.
        --
        -- setCurrent(square) is the one-argument, unambiguous form. IsoGameCharacter also
        -- exposes teleportTo, which does all of this properly -- but it ships as
        -- teleportTo(F,F,I) AND teleportTo(I,I,I), two THREE-ARGUMENT overloads, and Kahlua
        -- picks overloads by arity. That is the removeFluid trap again, so it is left alone.
        zombie:setCurrent(sq)
        moved = true
    end)
    -- LOG THE ONES THAT WORK, NOT ONLY THE ONES THAT DO NOT (v0.75.64). Until now this
    -- function printed on refusal and said nothing on success, so the mod's only record
    -- of repositioning a companion was a record of DECLINING to. Every teleport the
    -- author has reported went through here and left no trace. `why` is already threaded
    -- through every caller, so the attribution is free.
    if moved then
        local T = BanditsNPC.Trace
        if T and T.On() then
            T.Line(zombie, "NAV", "teleported to " .. T.Sq(sq) .. " -- reason: " .. tostring(why or "?"))
        end
    end
    return moved
end

-- Convenience: teleport to (x, y, z) if that square is usable. Same guarantees.
function N.TeleportTo(zombie, x, y, z, why)
    if not zombie or x == nil or y == nil or z == nil then return false end
    local sq
    pcall(function() sq = getCell():getGridSquare(math.floor(x), math.floor(y), math.floor(z)) end)
    return N.Teleport(zombie, sq, why)
end

-- ---------------------------------------------------------------------------
-- edge classification
-- ---------------------------------------------------------------------------

-- True when the barrier between two ADJACENT squares is a TALL hoppable.
--
-- Edge ownership in PZ: a square owns its NORTH and WEST edges. Moving north or
-- west therefore tests the FROM square's flag; moving south or east tests the TO
-- square's. Getting this backwards makes half of all fences read as walls.
-- `square:has(IsoFlagType.HoppableN)` is the vanilla idiom (ISClimbOverFence.lua:48).
function N.IsTallHoppableBetween(fromSq, toSq)
    local tall = false
    pcall(function()
        local dx = toSq:getX() - fromSq:getX()
        local dy = toSq:getY() - fromSq:getY()
        if dy < 0 then
            tall = fromSq:has(IsoFlagType.TallHoppableN)
        elseif dy > 0 then
            tall = toSq:has(IsoFlagType.TallHoppableN)
        elseif dx < 0 then
            tall = fromSq:has(IsoFlagType.TallHoppableW)
        elseif dx > 0 then
            tall = toSq:has(IsoFlagType.TallHoppableW)
        end
    end)
    return tall and true or false
end

-- CAN SHE WORK ON THAT TILE FROM WHERE SHE IS STANDING? (v0.75.57)
--
-- "She stood indoors and tilled the ground outside THROUGH a floor-to-ceiling window."
-- Every arrival test in the mod was a straight-line DistTo, and a straight line does not
-- care about glass: standing one tile from an outdoor square with a window between them
-- reads as "arrived" and the job proceeds through the wall. The same blindness is why she
-- then walked into that window instead of the door three tiles along -- the target was
-- chosen, and declared reached, without anything ever asking whether she could get there.
--
-- Cardinal neighbours only, which is all the reach checks need: an edge exists between
-- two squares that share a side. Diagonals and any longer gap answer `true` and fall back
-- to the caller's own distance test, so this can only ever REJECT a reach that is plainly
-- through a wall -- it never invents a new restriction on open ground.
--
-- A WINDOW COUNTS AS NOT REACHABLE even though she can climb one. Climbing is how she
-- TRAVELS; it is not how she reaches across a sill to plough a field. The Guardian's
-- stuck watchdog still offers a climb when she is genuinely trying to cross.
function N.CanWorkAcross(fromSq, toSq)
    if not (fromSq and toSq) then return false end
    local ok = true
    pcall(function()
        if fromSq == toSq then return end
        if fromSq:getZ() ~= toSq:getZ() then return end
        local dx = math.abs(fromSq:getX() - toSq:getX())
        local dy = math.abs(fromSq:getY() - toSq:getY())
        if dx + dy ~= 1 then return end            -- not cardinally adjacent: not our call
        local kind = N.ClassifyEdge(fromSq, toSq)
        -- SEE-THROUGH BARRIERS ONLY. `BLOCKED` was in this list for one build (v0.75.57)
        -- and it was badly wrong: a sink, a crate, a counter -- ANY solid furniture --
        -- makes the edge to its own square BLOCKED, and standing next to furniture is the
        -- NORMAL, CORRECT way to use it. So every container, water source and wash spot
        -- started reporting "can't reach", she never counted as arrived, she walked at it
        -- forever, and the Guardian watchdog teleported her every couple of minutes.
        -- That was the author's "frozen in time and teleporting" farming report and the
        -- washing freeze, and it is why sleep/TV/sitting were unaffected -- those end with
        -- her ON the target tile, where fromSq == toSq and this never runs.
        --
        -- A window is different in kind: it is passable-looking, it is what DistTo happily
        -- measures through, and there is never a legitimate reason to reach a farm plot
        -- across a windowsill. Those stay vetoed; nothing else does.
        if kind == N.EDGE.WINDOW or kind == N.EDGE.FRAME or kind == N.EDGE.THUMP then
            ok = false
        end
    end)
    return ok
end

-- ===========================================================================
-- ROUTING AROUND WINDOWS (v0.75.62)
-- ===========================================================================
--
-- "AGAIN BY WALKING AGAINST A FLOOR TO CEILING WINDOW!!! THERE IS A DOOR TO HER LEFT 3
-- TILES AWAY. HOOK INTO THEIR PATHING AI AND FIX IT!" (author, 6 Aug -- the third report
-- of the same thing.) Fair. Here is why every previous fix missed it.
--
-- Nothing in this mod moves a companion. Bandits issues {action="Move"} and ZAMove.onStart
-- hands the coordinates to the ENGINE:
--     zombie:getPathFindBehavior2():pathToLocation(task.x, task.y, task.z)
-- That is the zombie pathfinder, and to a zombie A WINDOW IS A DOOR. Climbing through one
-- is a normal traversal for them, so the route it returns goes through the glass and it is
-- not wrong to -- it is doing exactly what zombies do. On a floor-to-ceiling fixed window
-- (a wall sprite with no IsoWindow object behind it) there is nothing to climb, so she
-- arrives at the pane and shoves at it until something teleports her. Every fix so far --
-- CanWorkAcross, the arrival tests -- changed our mind about whether she had ARRIVED. None
-- of them changed the route, because we were never the one choosing it.
--
-- So choose it. This is a plain breadth-first search over squares that refuses exactly the
-- edges a companion should not be crossing on her way to work, and hands back a WAYPOINT
-- to steer the engine through. Give the engine a target on THIS side of the door and its
-- own pathfinder cannot invent a shortcut through the window, because from where she is
-- standing there is no window in the way of the waypoint.
--
-- WHY A WAYPOINT AND NOT A FULL PATH. Bandits' task queue takes one destination at a time
-- and re-generates constantly; feeding it a whole path would mean owning the follow-along
-- state and fighting every other thing that queues a Move. One waypoint at a time, refreshed
-- when she reaches it, needs no state we do not already have and degrades to the old
-- behaviour the moment the search fails.
--
-- IT IS BOUNDED AND IT IS CHEAP. 45x45 squares around her at most, one visit each, and it
-- only runs when a straight line at the target is actually obstructed -- which on open
-- ground is never. Failure means "no opinion": the caller walks at the target exactly as
-- it always did, so this can only ever improve a route, never remove one.

-- What stands between two ADJACENT squares, and what it takes to cross.
-- Returns (kind, obj) where obj is the window/frame/thumpable when relevant.
--
-- Order matters: specific openings are tested before the generic isBlockedTo,
-- because a window IS blocking until she climbs it.
function N.ClassifyEdge(fromSq, toSq)
    if not fromSq or not toSq then return N.EDGE.BLOCKED, nil end

    local kind, obj = nil, nil
    pcall(function()
        -- Level change first: stairs are the only way a zombie body legitimately
        -- changes Z under its own power.
        if fromSq:getZ() ~= toSq:getZ() then
            if fromSq:isStairBlockedTo(toSq) then kind = N.EDGE.BLOCKED
            else kind = N.EDGE.STAIRS end
            return
        end

        local w = fromSq:getWindowTo(toSq)
        if w then kind, obj = N.EDGE.WINDOW, w; return end

        local wf = fromSq:getWindowFrameTo(toSq)
        if wf then kind, obj = N.EDGE.FRAME, wf; return end

        local wt = fromSq:getWindowThumpableTo(toSq)
        if wt then kind, obj = N.EDGE.THUMP, wt; return end

        if fromSq:isWindowTo(toSq) then kind = N.EDGE.WINDOW; return end

        -- Hoppables split into two very different cases:
        --   low fence     -> climbOverFence, and media/AnimSets/zombie/climbfence exists
        --   TALL hoppable -> needs climbOverWall, which javap finds on IsoPlayer and
        --                    NOT on IsoGameCharacter, and for which there is no
        --                    `climbwall` AnimSet under media/AnimSets/zombie/ either.
        -- A companion on a zombie rig therefore CANNOT cross a tall hoppable by any
        -- means. Calling it BLOCKED routes her around instead of leaving her
        -- shoving a wall until the stuck watchdog fires.
        if fromSq:isHoppableTo(toSq) then
            kind = N.IsTallHoppableBetween(fromSq, toSq) and N.EDGE.BLOCKED or N.EDGE.FENCE
            return
        end

        if fromSq:isBlockedTo(toSq) then kind = N.EDGE.BLOCKED; return end

        kind = N.EDGE.OPEN
    end)

    return kind or N.EDGE.BLOCKED, obj
end

-- ---------------------------------------------------------------------------
-- traversal
-- ---------------------------------------------------------------------------

-- Cardinal IsoDirections between adjacent squares, or nil if they are not
-- cardinally adjacent (climbOverFence accepts a cardinal only).
function N.DirectionBetween(fromSq, toSq)
    local dx, dy
    local ok = pcall(function()
        dx = toSq:getX() - fromSq:getX()
        dy = toSq:getY() - fromSq:getY()
    end)
    if not ok or dx == nil then return nil end
    if dx == 0 and dy == -1 then return IsoDirections.N end
    if dx == 0 and dy == 1 then return IsoDirections.S end
    if dx == 1 and dy == 0 then return IsoDirections.E end
    if dx == -1 and dy == 0 then return IsoDirections.W end
    return nil
end

-- Starts crossing an edge that needs an explicit action. Returns true if one was
-- issued; the caller must then poll InTransit() and NOT touch her position until
-- it clears.
--
-- Every call below is public on IsoGameCharacter (javap-confirmed) and each has a
-- matching zombie AnimSet under media/AnimSets/zombie/ (climbwindow, climbfence),
-- so the pose actually plays instead of her gliding through the glass -- which is
-- what "walks through windows" looks like.
function N.BeginTraverse(zombie, fromSq, toSq)
    if not zombie or not fromSq or not toSq then return false end
    local kind, obj = N.ClassifyEdge(fromSq, toSq)

    -- Ordinary walking already handles these; BLOCKED means the caller re-routes.
    if kind == N.EDGE.OPEN or kind == N.EDGE.STAIRS or kind == N.EDGE.BLOCKED then
        return false
    end

    -- FIX(BUG-010): A SHUT WINDOW IS NOT CLIMBABLE, AND ASKING ANYWAY WAS A SILENT LIE.
    --
    -- Measured 2026-08-19 (logs/climb-probe-trace.txt:308): the Guardian rescue reached
    -- here with a real IsoWindow handle and `can=0 facing=1 open=0` -- she was facing it
    -- and the window was CLOSED. `climbThroughWindow` was called, did nothing at all, and
    -- this function still returned `issued = true`. So the caller logged
    -- "ISSUED: climbing instead of shoving", cleared its stuck accumulator, and waited for
    -- a climb that was never going to happen. Two minutes of freezes have been read off
    -- lines that said a climb had been issued.
    --
    -- The entry was opened on an overload-dispatch theory and that is NOT what this was.
    -- The handle was the right type. We simply never asked whether the thing could be
    -- climbed. `object:canClimbThrough(bandit)` is the question, and both precedents ask
    -- it before acting -- Bandits at BanditUpdate.lua:683, the player's timed action in
    -- its isValid() at ISClimbThroughWindow.lua:6-14.
    --
    -- OPENING IT IS DELIBERATELY NOT DONE HERE. Bandits already opens windows correctly,
    -- with an animated task (`{action="OpenWindow", anim="WindowOpen", time=25}`,
    -- BanditUpdate.lua:677-678) that is gated on `not isPermaLocked()` and de-duplicated
    -- by HasTaskType. This function has no task queue to put one in, the only direct API
    -- in refs is commented out (ISClimbThroughWindow.lua:36), and -- decisively -- nothing
    -- in this mod ever closes anything behind her, so an opened window stays open. That is
    -- BUG-026's failure with a television, except the window is a hole in the player's
    -- base. See U-017.
    --
    -- FAIL-OPEN IF THE METHOD IS ABSENT, on purpose: a missing `canClimbThrough` must not
    -- disable climbing for 27,000 subscribers. The probe reads `can=0`, not `can=?`, so it
    -- is present -- but this is a live mod and the guard costs one comparison.
    if obj and obj.canClimbThrough then
        local can = false
        pcall(function() can = obj:canClimbThrough(zombie) end)
        if not can then return false end
    end

    -- FIX(BUG-043): DRIVE THE STATE MACHINE, AND DO NOT CLAIM SUCCESS WITHOUT CHECKING.
    --
    -- WHAT WAS WRONG: this called `zombie:climbThroughWindow(obj)`. That method has FOUR
    -- occurrences in all of refs/ and every one is a PLAYER receiver -- grep
    -- `climbThroughWindow|climbThroughWindowFrame|climbOverFence` over refs/pz-lua/ and
    -- refs/bandits/mods/Bandits/42.20/ gives ISClimbThroughWindow.lua:38 and :40
    -- (`self.character`), ISClimbOverFence.lua:31 (`self.character`), and
    -- ISObjectClickHandler.lua:187 (`playerObj`, COMMENTED OUT). There is no
    -- zombie-receiver precedent, so under the Java rule it should never have been written.
    --
    -- AND THE TRACES AGREE WITH THE CITATION. `grep -c ISSUED logs/*.txt` returns exactly
    -- three across nine runs -- climb-probe-trace.txt:308, task012-trace.txt:16,
    -- task015-trace.txt:306 -- and in ALL THREE the very next TICK reads
    -- `state=ZombieIdleState`, her position is unchanged to two decimals, and a WATCH FLUSH
    -- follows 3-18 seconds later. Zero state changes, zero crossings, three for three.
    -- Bandits' own climbs in logs/task016-test.txt enter ClimbThroughWindowState on the
    -- very sample they fire and she is across one second later.
    --
    -- WHAT IT IS NOW: Bandits' form, which is the only cited zombie-receiver precedent and
    -- is observed working -- BanditUpdate.lua:684-686
    --     ClimbThroughWindowState.instance():setParams(bandit, object)
    --     bandit:changeState(ClimbThroughWindowState.instance())
    --     bandit:setBumpType("ClimbWindow")
    --
    -- ONLY THE WINDOW BRANCH IS CONVERTED, AND THAT IS NOT TIMIDITY, IT IS THE CITATION
    -- RULE. Bandits' state-change chain is gated on `instanceof(object, "IsoWindow")`
    -- (BanditUpdate.lua:601), so setParams has a precedent for an IsoWindow and for
    -- NOTHING ELSE. IsoWindowFrame and IsoThumpable appear in Bandits only as things it
    -- asks `canClimbThrough` about (IsWindowClose, :64-65) -- never as things it hands to
    -- this state. Guessing that setParams accepts them is exactly the move that produced
    -- this bug. FRAME, THUMP and FENCE therefore keep their calls and are logged as
    -- BUG-044; the fence form is not even the same two lines (ClimbOverFenceState, bump
    -- "ClimbFenceEnd", no setParams, and Bandits gates it on isFacingObject first --
    -- BanditUpdate.lua:573-575).
    --
    -- `issued` IS NOW OBSERVED, NOT ASSUMED. It used to be set on the line after each call,
    -- inside the pcall, so a method that threw and a method that dispatched into nothing
    -- both came out as true -- the caller then logged "ISSUED: climbing instead of shoving"
    -- and cleared st.acc, buying the failure another 3 seconds. A call whose success cannot
    -- be read from its return value has to be checked against the WORLD.
    local issued, why = false, "no branch matched"
    pcall(function()
        local function climbStateNow(want)
            -- Cited primary: a zombie receiver for getCurrentState is
            -- refs/pz-lua/client/Tutorial/Steps.lua:925, :928
            -- (`FightStep.momzombie:getCurrentState()`), and comparing it against
            -- `ClimbThroughWindowState.instance()` is Steps.lua:891.
            if want and zombie.getCurrentState then
                local ok, cur = pcall(function() return zombie:getCurrentState() end)
                if ok and cur ~= nil then return cur == want end
            end
            -- Fallback for the branches with no cited state class. Same test InTransit has
            -- used since v0.60; the trace prints real names through it (BumpedState,
            -- ClimbThroughWindowState), so it works -- but it has no refs precedent of its
            -- own and that is recorded in UNKNOWNS as U-019.
            local sn = zombie.getCurrentStateName and zombie:getCurrentStateName()
            return type(sn) == "string" and string.find(sn, "Climb") ~= nil
        end
        local function stateNow()
            local sn = zombie.getCurrentStateName and zombie:getCurrentStateName()
            return tostring(sn)
        end

        if kind == N.EDGE.WINDOW and obj then
            if not (ClimbThroughWindowState and ClimbThroughWindowState.instance) then
                why = "ClimbThroughWindowState not loaded in this context"
                return
            end
            local target = ClimbThroughWindowState.instance()
            target:setParams(zombie, obj)
            zombie:changeState(target)
            zombie:setBumpType("ClimbWindow")
            if climbStateNow(target) then
                issued, why = true, "entered ClimbThroughWindowState"
            else
                why = "changeState did not take -- state is " .. stateNow()
            end

        elseif kind == N.EDGE.FRAME and obj then
            zombie:climbThroughWindowFrame(obj)       -- BUG-044: player-only receiver
            issued = climbStateNow(nil)
            why = issued and "frame climb took" or ("frame climb did nothing -- state is " .. stateNow())

        elseif kind == N.EDGE.THUMP and obj then
            zombie:climbThroughWindow(obj)            -- BUG-044: player-only receiver
            issued = climbStateNow(nil)
            why = issued and "thump climb took" or ("thump climb did nothing -- state is " .. stateNow())

        elseif kind == N.EDGE.FENCE then
            local dir = N.DirectionBetween(fromSq, toSq)
            if not dir then
                why = "fence with no direction between the squares"
            else
                zombie:climbOverFence(dir)            -- BUG-044: player-only receiver
                issued = climbStateNow(nil)
                why = issued and "fence vault took" or ("fence vault did nothing -- state is " .. stateNow())
            end
        end
    end)

    -- ONE LINE, FAILURES ONLY. The Guardian already writes exactly one CLIMB line per
    -- attempt and BeginTraverse's only caller is that once-per-struggle probe, so this
    -- cannot flood. Success needs no line here: it shows up as `state=ClimbThroughWindowState`
    -- on the next TICK, which is the criterion BUG-043 asks the next run to be judged by.
    if not issued then
        local T = BanditsNPC.Trace
        if T and T.Line and T.On and T.On() then
            T.Line(zombie, "TRAVERSE", "refused: " .. tostring(why) .. " -- kind=" .. tostring(kind))
        end
    end
    return issued
end

-- ---------------------------------------------------------------------------
-- stuck detection
-- ---------------------------------------------------------------------------

local stuck = {}   -- uid -> { x, y, acc, last }

-- Accumulated "not making progress" milliseconds.
--
-- Measured as displacement from an ANCHOR, not as a per-tick delta. Bumping a tree
-- oscillates a companion about half a tile each way, which resets any per-tick
-- check forever -- that is why the old 0.4-per-tick watchdog never fired and
-- companions bounced off the same tree indefinitely ("travelling through forests
-- is difficult", report 11 Jul). Only movement of more than a tile from where the
-- struggle began counts as progress.
--
-- IN TRANSIT ALWAYS REPORTS 0. A companion on a staircase is not stuck, and
-- treating her as stuck is what got her moved mid-step in the first place.
-- ===== DEAD: NOTHING CALLS THIS (verified 5 Aug, v0.75.32) =====
-- A mod-wide sweep finds exactly one reference to N.TrackStuck -- this definition. No
-- program, no watchdog, no tick handler ever calls it, so the stuck COUNTING it maintains
-- has never once run and every threshold in it is theoretical.
--
-- Read that plainly before trusting this file: **Nav does not detect stuck companions.**
-- What actually recovers one is the Guardian's watchdog plus the player pressing "Bring
-- here / unstick" (Persistence.Unstuck) -- and Persistence still calls N.ClearStuck, which
-- is why that function stays even though it now clears state nobody sets.
--
-- LEFT IN PLACE RATHER THAN DELETED, deliberately: removing it is a behaviour-neutral
-- tidy-up, but it is entangled with ClearStuck's contract and this was found late in an
-- audit pass. Documenting a dead safety net is what stops the next reader assuming it
-- works; deleting it is a separate, safe job for someone with the file in front of them.
function N.TrackStuck(zombie, uid)
    if not zombie or not uid then return 0 end
    local t = nowMs()

    if N.InTransit(zombie) then
        stuck[uid] = nil
        return 0
    end

    local x, y
    pcall(function() x, y = zombie:getX(), zombie:getY() end)
    if not x then return 0 end

    local s = stuck[uid]
    if not s then
        stuck[uid] = { x = x, y = y, acc = 0, last = t }
        return 0
    end

    if math.abs(x - s.x) > 1.2 or math.abs(y - s.y) > 1.2 then
        s.x, s.y, s.acc, s.last = x, y, 0, t
        return 0
    end

    s.acc = s.acc + math.max(0, t - s.last)
    s.last = t
    return s.acc
end

function N.ClearStuck(uid)
    if uid then stuck[uid] = nil end
end
