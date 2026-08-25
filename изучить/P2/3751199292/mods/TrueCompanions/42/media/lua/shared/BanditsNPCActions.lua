--
-- True Companions - custom ZombieActions (engine task handlers)
--
-- The Bandits engine dispatches brain tasks to ZombieActions[task.action] (BanditUpdate
-- :1794/:1807/:1826). We register our own handlers ADDITIVELY under names no other mod
-- uses ("BNSit", "BNSleep"), so this can never collide with Bandits' or Bandits Week
-- One's own ZombieActions regardless of load order -- BWO also ships a chair-sit action
-- (ZombieActions.SitInChair) and we must not overwrite or depend on it.
--
-- WHY EVERY HANDLER PINS IN onStart AND onComplete TOO (the "flicker" lesson, 11 Jul):
-- ProcessTask advances one state per tick (NEW -> WORKING -> COMPLETED) and the next
-- program dispatch takes another tick -- so between one hold-task expiring and the
-- re-issued task's first onWorking there are ~3 ticks where nothing pins her, and the
-- furniture's solid collision pushes her out ("she clips out of the bed and back every
-- second or two"). Pinning in all three stages closes the gap to the single dispatch
-- tick, which is not visible.
--
-- BNSit: a POSITIONED chair sit. Bandits' stock "Sit" bump anim is Bob_SitGround_Idle
-- (sitting on the FLOOR), which is why a companion sent to a chair "phased through the
-- seat" (tester report, 8 Jul). The pose is our own "BNSitChair" bump anim node
-- (common/media/AnimSets/zombie/bumped/BNSitChair.xml); as of 13 Jul it plays
-- Bob_IdleSeated_BN -- the second community-CONTRIBUTED animation (shipped with
-- permission, anims_X/Zombie/) -- replacing the vanilla stand-in Bob_IdleSeatedExt01
-- (its 0.80 SpeedScale crutch went back to 1.00; a purpose-made idle plays as
-- authored). Like the sleep anim it positions itself from the plain tile center, so
-- BWO's seat-edge offsets were REVERTED (see SIT_OFFSETS below).
--
-- BNSleep: a POSITIONED bed sleep. Replaces the engine's ZASleep for our bed routine.
-- The pose is "BNSleepBed" (common/media/AnimSets/zombie/bumped/BNSleepBed.xml) playing
-- Bob_Asleep_Bed -- a community-CONTRIBUTED animation for this mod (shipped with
-- permission; not ripped from another mod) that was AUTHORED to lie correctly on the
-- mattress from the plain tile center, so the pin is exactly the engine's own ZASleep
-- math (tile center + facing) minus its S-only "eoffset" special case.
--
-- HISTORY (13 Jul revert): before the contributed anim existed, BNSleep compensated
-- for the vanilla lying pose by CENTERING her between the bed's two tiles (task.x2/y2).
-- With the custom anim that centering (a) double-offset the pose ("does not look
-- correct") and (b) parked her exactly ON the boundary between the two bed squares, so
-- her occupied square flip-flopped per tick -- the engine re-sorted/collided her per
-- square (invisible frames, pop-out beside the bed) and the next pin snapped her back
-- ("visible -> invisible -> next to bed -> back" report). Reverted to the single
-- spot-tile pin; SLEEP_NUDGE stays as an all-zero tuning hook.
--

ZombieActions = ZombieActions or {}

-- ===== WHICH POSE NODES TO USE (v0.75.57) =====
--
-- OUR TWO CONTRIBUTED ANIMATIONS DO NOT LOAD, and it is not our bug. The author's test
-- log has, in one batch:
--     AnimationAssetManager.loadCallback > Failed to load asset: "zombie/bob_asleep_bed"
--     AnimationAssetManager.loadCallback > Failed to load asset: "zombie/bob_idleseated_bn"
-- alongside the SAME failure for every animation Bandits ships itself -- bob_frontkick,
-- bob_pushkick, all thirteen bob_walkbwd_*. Every one of those lives in a mod's
-- `common/media/anims_X/Zombie/`, and nothing in that folder resolves for ANY mod on that
-- install. The AnimSet XML nodes beside them load fine (the engine only asked for
-- "bob_asleep_bed" because it had read our BNSleepBed.xml), so it is anims_X
-- specifically, not the common/ root. Not fileGuidTable either -- Bandits ships one and
-- fails the same way. Cause not established; it needs a clean-profile test.
--
-- Meanwhile the BUMP NODES that reference VANILLA assets work perfectly, because those
-- assets ship with the game. So the fallbacks below are not a downgrade to nothing --
-- they are a real sleeping pose and a real seated pose that will actually play:
--     ZSSleepBed  -> Bob_Asleep              (vanilla, media/anims_X/Bob/)
--     ZSSitAction -> Bob_SitGround_ActionIdle (vanilla)
-- ZSSitAction is a GROUND sit, so a chair sit is imperfect until the custom pose loads.
-- It is the closest vanilla-backed seated idle; BWO's ZSSitChair1/2 look better but point
-- at Bob_SittingChair/Bob_SatChair, which are mod-supplied and so fail for the same reason.
--
-- LEFT ON, on the author's word that these poses demonstrably worked before (5 Aug).
-- The evidence agrees with him and not with my first reading:
--   * "bob_asleep_bed" appears in exactly ONE log ever -- the 21:36 session. No earlier
--     log mentions it at all.
--   * Bandits' equivalent failure is NOT permanent: present 17-30 Jun, ABSENT 13 Jul,
--     back on 5 Aug. A property of the install would not come and go.
--   * The files themselves are sound -- xof 0303txt header, internal `AnimationSet
--     Bob_Asleep_Bed` matching the filename exactly as vanilla's own files do, correct
--     anims_X/Zombie/ folder, byte-identical in source and deploy, untouched since 13 Jul.
--   * The failing batch sits in the middle of a pile of SAVE-LOAD errors from a REMOVED
--     mod (BurdSurvivalJournals items "removed = true", "SpriteConfig.load > Could not
--     load script for sprite config"). A load already going wrong is a plausible reason
--     for an asset preload to give up, and would explain a failure that comes and goes.
-- So this stays true and the commissioned poses stay in. Switching them out on one log
-- would have quietly downgraded paid-for art on a guess.
--
-- IF SLEEP STILL DOES NOT ANIMATE, flip this to false for the vanilla-backed stand-ins
-- (ZSSleepBed -> Bob_Asleep, ZSSitAction -> Bob_SitGround_ActionIdle, both of which ship
-- with the game and always load). That is a fallback, not a fix.
local USE_CONTRIBUTED_POSES = true

local POSE_SIT   = USE_CONTRIBUTED_POSES and "BNSitChair" or "SitAction"
local POSE_SLEEP = USE_CONTRIBUTED_POSES and "BNSleepBed" or "SleepBed"

-- The pose a task should actually play. A task still carries its own `anim`, but a task
-- naming one of OUR nodes is asking for "the sit pose" / "the sleep pose", not for that
-- literal node -- so it gets remapped when the contributed assets are unavailable.
local function poseFor(anim, fallback)
    if anim == "BNSitChair" then return POSE_SIT end
    if anim == "BNSleepBed" then return POSE_SLEEP end
    return anim or fallback
end

-- ===== BNSit =====

ZombieActions.BNSit = {}

-- per-facing position + look vectors. dx/dy were BWO's seat-edge offsets, needed to
-- line the VANILLA seated idle up with the seat -- REVERTED to plain tile center
-- 13 Jul: the contributed Bob_IdleSeated_BN (like Bob_Asleep_Bed) is authored to sit
-- correctly from the tile center, so the old offsets double-shifted the pose
-- (author report: couch looked right facing one way, clipped facing the other --
-- the asymmetric-offset signature). dx/dy stay as per-facing tunables if the pose
-- needs a nudge on some furniture. "facing" = the direction the SITTER faces (same
-- semantics as the seat sprite's own "Facing" tile property).
local SIT_OFFSETS = {
    S = { dx = 0.5, dy = 0.5, fx = 0,  fy = 20 },
    N = { dx = 0.5, dy = 0.5, fx = 0,  fy = -20 },
    E = { dx = 0.5, dy = 0.5, fx = 20, fy = 0 },
    W = { dx = 0.5, dy = 0.5, fx = -20, fy = 0 },
}

-- COUCHES seat differently than chairs (author report 13 Jul: chairs and
-- armchairs sit right from the tile center, but on couches she sits "a little
-- inside it and to its back" -- sofa sprites keep their seat surface toward the
-- tile front and the back half is all backrest). When the furniture POSITIVELY
-- identifies as a couch, nudge the pin forward along the sitter's facing;
-- anything unrecognized gets NO offset, so chairs stay exactly as they are.
-- Detection, all proven calls: CustomName sprite property read via
-- props:has("CustomName")/get("CustomName") (live vanilla usage,
-- ISHandcraftWindow.lua:22); fallback = multi-tile seat via obj:hasSpriteGrid()
-- (single-tile chairs have no sprite grid, sofas/loveseats do).
--
-- 29 Jul (42.20 audit): the fallback also tested props:Val("SitAllowed") -- BOTH
-- halves of that were wrong. PropertyContainer has NO Val() method at all on 42.20
-- (full jar dump; vanilla Lua never calls :Val anywhere), and no "SitAllowed"
-- property exists in the game data either. The existence guard `props.Val and`
-- short-circuited, so it never errored -- it just meant this entire branch was
-- unreachable and couch detection silently collapsed to the CustomName list, which
-- is exactly the "she sits inside the couch and to its back" case v0.46.1 set out
-- to fix (any sofa not named sofa/couch/settee/loveseat/futon got no nudge).
-- hasSpriteGrid() alone is the right signal AND the proven one: it is the same
-- notion vanilla's own sit-on-furniture pathing keys off (bAnySpriteGridObject,
-- ISPathFindAction:pathToSitOnFurniture). Worst case a multi-tile non-seat the
-- player deliberately designated as a seat spot gets a 0.25 nudge -- cosmetic.
local COUCH_FORWARD = 0.25   -- tiles toward facing (watch-list tunable)

-- CLAMP (v0.75.28). Every pose pin is built as tile-centre (0.5) plus a tunable, so any
-- tunable of 0.5 or more puts the pin ON or PAST the tile boundary -- which is exactly the
-- "boundary positions flip her occupied square per tick" flicker that pinSleep documents
-- below. The knobs stay adjustable; they just cannot be turned into that bug.
local MAX_NUDGE = 0.45
local function clampNudge(v)
    if type(v) ~= "number" then return 0 end
    if v > MAX_NUDGE then return MAX_NUDGE end
    if v < -MAX_NUDGE then return -MAX_NUDGE end
    return v
end
local COUCH_NAMES = { "sofa", "couch", "settee", "loveseat", "futon" }

local function seatIsCouch(x, y, z)
    local ok, res = pcall(function()
        local sq = getCell():getGridSquare(x, y, z)
        if not sq then return false end
        local objs = sq:getObjects()
        for i = 0, objs:size() - 1 do
            local o = objs:get(i)
            local props = o and o.getProperties and o:getProperties()
            if props and props.has and props:has("CustomName") then
                local nm = string.lower(tostring(props:get("CustomName") or ""))
                for _, key in ipairs(COUCH_NAMES) do
                    if nm:find(key, 1, true) then return true end
                end
            end
            if o and o.hasSpriteGrid and o:hasSpriteGrid() then
                return true   -- multi-tile sit furniture: couch-shaped
            end
        end
        return false
    end)
    return ok and res == true
end

-- NOTE (v0.60): the pose pins deliberately place her ON an OCCUPIED tile (the
-- chair/bed itself), so they must NOT go through Nav.Teleport -- its safety check
-- requires a free square and would reject every seat in the game. What they DO
-- need is the transit guard: these re-pin every single tick, so without it a
-- stray sit/sleep task left running while she is on a staircase re-pins her onto
-- the seat's Z from a fractional stair Z. Skip the tick instead; the hold is
-- re-issued continuously and will take effect the moment she is standing again.
local function pinSit(zombie, task)
    if BanditsNPC.Nav.InTransit(zombie) then return end
    if task.x and task.y and task.z ~= nil then
        local o = SIT_OFFSETS[task.facing or "S"] or SIT_OFFSETS.S
        -- detect once per task; re-issued holds re-check every ~5s, negligible
        if task._couch == nil then task._couch = seatIsCouch(task.x, task.y, task.z) end
        local dx, dy = o.dx, o.dy
        if task._couch then
            local f = task.facing or "S"
            local cf = clampNudge(COUCH_FORWARD)
            if f == "S" then dy = dy + cf
            elseif f == "N" then dy = dy - cf
            elseif f == "E" then dx = dx + cf
            elseif f == "W" then dx = dx - cf end
        end
        zombie:setX(task.x + dx)
        zombie:setY(task.y + dy)
        zombie:setZ(task.z)
        -- and tell the engine WHICH SQUARE that is -- see the note in pinSleep
        pcall(function() zombie:setCurrentSquareFromPosition() end)
        zombie:faceLocationF(task.x + o.fx, task.y + o.fy)
    end
end

ZombieActions.BNSit.onStart = function(zombie, task)
    pinSit(zombie, task)   -- ProcessTask has already set the bump anim in NEW state
    return true
end

-- re-pin position + bump anim every tick, like the engine's own positioned Sleep
-- action (ZASleep) -- a shove or path nudge can't slide her off the seat mid-sit
ZombieActions.BNSit.onWorking = function(zombie, task)
    zombie:setBumpType(poseFor(task.anim, POSE_SIT))
    pinSit(zombie, task)
    return false
end

ZombieActions.BNSit.onComplete = function(zombie, task)
    pinSit(zombie, task)   -- hold through the COMPLETED tick (see header note)
    return true
end

-- ===== BNSleep =====

ZombieActions.BNSleep = {}

-- fine-tune nudges for the lying pose, per facing (watch-list tunables; all zero --
-- the contributed anim positions itself from the tile center, see header)
local SLEEP_NUDGE = {
    S = { x = 0, y = 0 },
    N = { x = 0, y = 0 },
    E = { x = 0, y = 0 },
    W = { x = 0, y = 0 },
}
local FACE_VEC = { S = {0, 20}, N = {0, -20}, E = {20, 0}, W = {-20, 0} }

local function pinSleep(zombie, task)
    if BanditsNPC.Nav.InTransit(zombie) then return end   -- see the note on pinSit
    if task.x and task.y and task.z ~= nil then
        -- plain spot-tile center, exactly like the engine's ZASleep (never pin
        -- between two squares -- boundary positions flip her occupied square per
        -- tick and cause the invisible/pop-out flicker, see header)
        local n = SLEEP_NUDGE[task.facing or "S"] or SLEEP_NUDGE.S
        zombie:setX(task.x + 0.5 + clampNudge(n.x))
        zombie:setY(task.y + 0.5 + clampNudge(n.y))
        zombie:setZ(task.z)
        -- AND TELL THE ENGINE WHICH SQUARE THAT IS (v0.75.63).
        --
        -- setX/setY/setZ move the coordinates only; the object stays REGISTERED on the
        -- square it was on. Pin her onto an upstairs bed that way and she is at z=1 while
        -- the engine still has her on the ground-floor square, finds no floor holding her
        -- up, and drops her: "she teleported to bed but kept falling through the floor to
        -- the floor below and died, then resurrected and went to sleep and kept falling
        -- through the floor in a loop" (author, 6 Aug). Bandits' own ZASleep has the same
        -- omission, which is why this was never visible in their code either.
        --
        -- setCurrentSquareFromPosition() takes no arguments, so unlike teleportTo (which
        -- ships as both (F,F,I) and (I,I,I) and would be an arity-ambiguous dispatch) there
        -- is nothing for Kahlua to guess at.
        pcall(function() zombie:setCurrentSquareFromPosition() end)
        local f = FACE_VEC[task.facing or "S"] or FACE_VEC.S
        zombie:faceLocationF(task.x + f[1], task.y + f[2])
    end
end

ZombieActions.BNSleep.onStart = function(zombie, task)
    pinSleep(zombie, task)
    return true
end

ZombieActions.BNSleep.onWorking = function(zombie, task)
    zombie:setBumpType(poseFor(task.anim, POSE_SLEEP))
    pinSleep(zombie, task)
    return false
end

ZombieActions.BNSleep.onComplete = function(zombie, task)
    pinSleep(zombie, task)
    return true
end
