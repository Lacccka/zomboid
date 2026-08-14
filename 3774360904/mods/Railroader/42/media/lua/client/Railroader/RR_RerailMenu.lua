--***********************************************************************
-- Railroader / RR_RerailMenu  -- "Put it back on the rails", DEBUG-GATED
--
-- Derailment is TERMINAL by design (Task 1.N step 6): the loco detaches from the spline,
-- freezes at a wreck pose, the engine dies and the controls refuse -- for good. A real
-- re-railing mechanic (crane, crew, hours of work) is the deferred owner-B plan, and this
-- file is NOT it. It is the development back door: one context-menu entry that calls
-- RR_TrainEntity.rerail and lifts the loco back onto the rails at the point it left them.
--
-- THE GATE IS THE GAME'S OWN DEBUG MODE (`-debug`), NOT RR.Debug (owner, 2026-08-02).
-- A context-menu entry is even easier to hit by accident than a keypress -- a player who
-- wrecks his loco and then right-clicks it looking for anything at all would find a free
-- undo, and the whole weight of a derailment (the reason a driver watches his speed at a
-- broken end) would evaporate. So it must be invisible in an ordinary game; but making the
-- owner type a console command every session to see his own dev tool is friction for
-- nothing. `isDebugEnabled()` is exactly `Core.debug` (LuaManager:4222), i.e. the `-debug`
-- launch option -- the same test vanilla's own DebugContextMenu uses, and something no
-- player is in by accident. Launched normally, the entry does not exist.
--
-- This does NOT change the standing rule that RR.Debug is untied from `-debug`: that rule
-- is about the KEYS (O teleports the loco, C deletes it, G drives it), which must stay
-- inert for a tester running with the console open. It says nothing about one menu entry
-- that has to be found deliberately. RR.Debug.set(true) does not turn this on and does not
-- need to. The console twin, RR.TrainEntity.forceRerail(), is ungated like every other
-- console helper: it cannot be hit by accident, and it is how a tester reproduces a report.
--
-- FAILS CLOSED, on purpose (the opposite of the RR_MP guard's fail-open rule): if the debug
-- test can't be made at all, there is no entry. A gate whose whole job is to hide something
-- from players must not open itself when it is confused.
--
-- ANCHORING -- TWO PATHS, because neither alone covers a 17-tile body:
--   1. the WORLD-object menu (the square under the cursor) matched against the wreck's own
--      footprint box (RR.Body.hullDistance on the frozen pose, the same measure the E-key
--      boarding reach uses) rather than a radius from the loco's centre, which for a
--      15-tile body puts "arm's length at the cab door" and "two car-lengths off the nose"
--      at the same distance;
--   2. the ANIMAL path -- RR_AnimalMenuFilter calls addForAnimal() as it suppresses the
--      livestock menu on one of ours. The engine fills `clickedAnimals` from the 3x3 around
--      the clicked square (ISWorldObjectContextMenuLogic:584-596), so any of our animals
--      near the cursor answers, with no dependence on the cursor->square projection at all.
--      Which animals those are has changed twice: the hull used to be lined with
--      rr_collider segments every ~2.2 tiles (Collider.useSegments is false since v1.0.2,
--      so there are none), and since Task 3.C it carries an rr_truck at each end -- which
--      restores most of the reach the segments used to give, for free.
--
-- WHY PATH 1 NEEDS A FAT PAD: the click resolves to a GROUND square, but the hull is drawn
-- ~4.5 tiles tall. Click the roof or the long hood and the square under the cursor is several
-- tiles up-screen of the footprint the loco actually occupies -- the standard isometric height
-- offset. A 2-tile pad missed those clicks entirely; the pad is precision we have no use for
-- here (the only thing within tiles of a wreck is the wreck), so it is generous.
--***********************************************************************

print("[Railroader] RR_RerailMenu.lua: loading...")

local RerailMenu = {}

-- tiles of slack around the DRAWN hull for the world-menu path (see the isometric-offset
-- note above; also covers the ballast beside a wreck that sits at an angle).
RerailMenu.CLICK_PAD = 6.0
-- tiles from one of OUR animals (loco body or a collider segment) to the wreck it belongs
-- to, for the animal path. A segment is never more than half a body length off its own
-- loco, so this only has to beat ~9 tiles; the slack costs nothing because the search is
-- over derailed locos only.
RerailMenu.ANIMAL_RADIUS = 30.0

--------------------------------------------------------------------------
-- txt(key, fallback): getText echoes the KEY back when a translation is missing, so
-- every label falls back to readable English instead of printing "IGUI_RR_...".
-- (Same helper as RR_CabMenu -- kept local rather than shared for one string.)
--------------------------------------------------------------------------
local function txt(key, fallback)
    local s = key
    pcall(function() s = getText(key) end)
    if not s or s == "" or s == key then return fallback or key end
    return s
end

--------------------------------------------------------------------------
-- debugMode(): is the GAME in debug mode (`-debug`)? isDebugEnabled() is Core.debug
-- verbatim (LuaManager:4222); getDebug() is the same OR a debug dedicated server, which is
-- not a case we want to open a client-side world edit for. Wrapped because a missing global
-- must read FALSE (fail closed), not throw inside a context-menu build.
--------------------------------------------------------------------------
local function debugMode()
    local on = false
    pcall(function() on = isDebugEnabled() and true or false end)
    return on
end
RerailMenu.debugMode = debugMode   -- exposed for report()

--------------------------------------------------------------------------
-- derailedAt(sq): the DERAILED loco whose footprint (+ CLICK_PAD) covers this square,
-- nearest hull first, or nil. Only derailed ones are considered -- a healthy loco has
-- nothing to offer this menu.
--------------------------------------------------------------------------
local function derailedAt(sq)
    local TE = RR.TrainEntity
    if not (sq and TE and TE.active and RR.Body) then return nil end
    local x, y = sq:getX() + 0.5, sq:getY() + 0.5
    local best, bestD
    for _, e in ipairs(TE.active) do
        local pose = e.derailed and (e.derailPose or e.lastPose)
        if pose and e.animal then
            local size = 1.0
            pcall(function() size = e.animal:getAnimalSize() end)
            local d = RR.Body.hullDistance(pose, size, x, y)
            if d <= RerailMenu.CLICK_PAD and ((not bestD) or d < bestD) then
                best, bestD = e, d
            end
        end
    end
    return best
end

--------------------------------------------------------------------------
-- nearestDerailed(x, y, radius): the derailed loco whose WRECK CENTRE is nearest (x,y)
-- within `radius`, or nil. The centre is the right measure here -- the caller already
-- knows it is touching this loco (it clicked one of its own animals), it just has to say
-- WHICH loco that was.
--------------------------------------------------------------------------
local function nearestDerailed(x, y, radius)
    local TE = RR.TrainEntity
    if not (TE and TE.active) then return nil end
    local best, bestD
    for _, e in ipairs(TE.active) do
        local pose = e.derailed and (e.derailPose or e.lastPose)
        if pose then
            local dx, dy = (pose.x or 0) - x, (pose.y or 0) - y
            local d = math.sqrt(dx * dx + dy * dy)
            if d <= radius and ((not bestD) or d < bestD) then best, bestD = e, d end
        end
    end
    return best
end

--------------------------------------------------------------------------
-- perform(e): do it. No walk-to and no timed action on purpose -- a timed action would be
-- pretending this is a mechanic, and the point of the tool is to get straight back to
-- whatever was being tested. The result is spoken in the player's channel (green halo)
-- and in the log, since the loco often lifts back onto rails that are off screen.
--------------------------------------------------------------------------
function RerailMenu.perform(e)
    if not (RR.TrainEntity and RR.TrainEntity.rerail) then return end
    local ok = RR.TrainEntity.rerail(e)
    local p = getPlayer()
    if p then
        pcall(function()
            if ok then
                HaloTextHelper.addGoodText(p, txt("IGUI_RR_Rerailed", "Back on the rails"))
            else
                HaloTextHelper.addBadText(p, txt("IGUI_RR_RerailFailed", "It won't go back on"))
            end
        end)
    end
end

-- The two option shapes. ISContextMenu passes the option's `param1` first, so the world
-- menu (param1 = worldobjects) and the animal menu (param1 = the record) need different
-- entry points into the same call.
function RerailMenu.onRerail(worldobjects, e) RerailMenu.perform(e) end
function RerailMenu.onRerailTarget(e)         RerailMenu.perform(e) end

--------------------------------------------------------------------------
-- offered(context, label): is this entry already on the menu? BOTH paths can fire for one
-- right-click -- the animal path runs first (inside createMenuEntries, which triggers
-- OnClickedAnimalForContext) and the world path second (OnFillWorldObjectContextMenu, at
-- the end of createMenu) -- and the animal one is called once PER clicked animal, i.e. once
-- per collider segment in the 3x3. Without this the wreck would sprout four identical
-- entries, which reads as broken rather than as thorough.
--------------------------------------------------------------------------
local function offered(context, label)
    for i = 1, (context.numOptions or 1) - 1 do
        local o = context.options and context.options[i]
        if o and o.name == label then return true end
    end
    return false
end

--------------------------------------------------------------------------
-- addForAnimal(playerNum, context, animal, test): the ANIMAL path, called from
-- RR_AnimalMenuFilter at the exact point where it drops the vanilla livestock menu for one
-- of ours. `animal` is an rr_loco body or (far more often, since they line the whole hull)
-- an rr_collider segment; either way the wreck it belongs to is the nearest derailed loco.
-- Never adds anything twice: the world path runs on a different event, and a context menu
-- that offered "put it back on the rails" twice would just look broken -- so it checks.
--------------------------------------------------------------------------
function RerailMenu.addForAnimal(playerNum, context, animal, test)
    if RR.MP and RR.MP.blocked() then return end
    if not debugMode() then return end            -- no `-debug`: the entry does not exist
    if not (animal and context) then return end
    local x, y
    pcall(function() x, y = animal:getX(), animal:getY() end)
    if not x then return end
    local e = nearestDerailed(x, y, RerailMenu.ANIMAL_RADIUS)
    if not e then return end
    if test then return ISWorldObjectContextMenu.setTest() end   -- controller (see OnFill)
    local label = txt("IGUI_RR_Rerail", "Put it back on the rails [debug]")
    if offered(context, label) then return end
    context:addOption(label, e, RerailMenu.onRerailTarget)
end

--------------------------------------------------------------------------
-- OnFillWorldObjectContextMenu: (playerNum, context, worldobjects, test)
--------------------------------------------------------------------------
function RerailMenu.OnFill(playerNum, context, worldobjects, test)
    if RR.MP and RR.MP.blocked() then return end   -- stood down on a client (RR_MP)
    if not debugMode() then return end             -- no `-debug`: the entry does not exist

    local sq
    for _, o in ipairs(worldobjects) do
        local s = o:getSquare()
        if s then sq = s; break end
    end
    if not sq then return end

    local e = derailedAt(sq)
    if not e then return end

    -- CONTROLLER: the pad's interact button only opens a menu when a handler declares an
    -- entry in the TEST pass (ISWorldObjectContextMenu:122 + :216). A tester on a pad has
    -- the same right to the dev back door as one on a keyboard.
    if test then return ISWorldObjectContextMenu.setTest() end

    local label = txt("IGUI_RR_Rerail", "Put it back on the rails [debug]")
    if offered(context, label) then return end   -- the animal path already added it
    context:addOption(label, worldobjects, RerailMenu.onRerail, e)
end

Events.OnFillWorldObjectContextMenu.Add(RerailMenu.OnFill)

--------------------------------------------------------------------------
-- report(): why is the entry not there? Answers the whole gate chain in one console line
-- each -- the debug flag, the MP guard, how many locos are active, which of them are
-- derailed and how far the PLAYER is from each wreck's hull (the same measure the world
-- path uses on the clicked square, so a number well over CLICK_PAD tells you the click
-- simply landed too far away).
--   RR.RerailMenu.report()
--------------------------------------------------------------------------
function RerailMenu.report()
    local TE = RR.TrainEntity
    print(string.format("[Railroader] rerail menu: gameDebug(-debug)=%s  mpBlocked=%s  active=%d",
        tostring(debugMode()),
        tostring(RR.MP and RR.MP.blocked()),
        (TE and TE.active and #TE.active) or -1))
    if not (TE and TE.active) then return end
    local p = getPlayer()
    for i, e in ipairs(TE.active) do
        local pose = e.derailPose or e.lastPose
        local d = -1
        if p and pose and RR.Body and e.animal then
            local size = 1.0
            pcall(function() size = e.animal:getAnimalSize() end)
            d = RR.Body.hullDistance(pose, size, p:getX(), p:getY())
        end
        print(string.format("  [%d] derailed=%s at (%.1f,%.1f) -- player %.1f tiles from the hull "
              .. "(world-menu pad %.1f, animal radius %.1f)",
              i, tostring(e.derailed and true or false),
              (pose and pose.x) or 0, (pose and pose.y) or 0, d,
              RerailMenu.CLICK_PAD, RerailMenu.ANIMAL_RADIUS))
    end
end

RR = RR or {}
RR.RerailMenu = RerailMenu
print("[Railroader] RR_RerailMenu.lua: loaded OK -- re-rail entry is "
      .. (debugMode() and "LIVE (game is in -debug)" or "hidden (game not in -debug)")
      .. "; console: RR.TrainEntity.forceRerail() / RR.RerailMenu.report().")
return RerailMenu
