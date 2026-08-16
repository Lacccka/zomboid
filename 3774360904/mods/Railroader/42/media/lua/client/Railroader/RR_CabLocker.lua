--***********************************************************************
-- Railroader / RR_CabLocker -- the engine-facing half of the cab locker.
--
-- The rules (capacity 30, what it refuses, and the placement decision that must not
-- duplicate itself) live in the pure, unit-tested shared/Railroader/RR_Locker.lua.
-- This file owns the one world object behind them.
--
-- ==== WHAT IT IS ====
--
-- One invisible IsoObject per loco, carrying one real ItemContainer, standing on the
-- driver's seat tile and moved along with it. See RR_Locker's header for why item
-- storage HAS to be a world object here (IsoAnimal.save writes no inventory).
--
-- Everything downstream is vanilla and costs us nothing:
--   * the chunk saves it (IsoObject.save:1453-1464 -> ItemContainer.save ->
--     CompressIdenticalItems: full fidelity, including a can of diesel);
--   * the chunk restores it (IsoObject.load:1210-1243 rebuilds the container
--     regardless of the sprite's flags, and re-parents it to the square);
--   * the loot window finds it by itself -- ISInventoryPage:refreshBackpacks:1728-1745
--     walks the 3x3 around the player and adds a button for every container on it,
--     and the driver is pinned to this exact tile. No UI code at all.
--
-- ==== THE DUPE THIS FILE EXISTS TO NOT MAKE ====
--
-- When a chunk unloads and streams back in, the locker is a NEW Java object rebuilt
-- from disk. Any handle we cached is a corpse whose `square` points at a discarded
-- IsoGridSquare. Re-adding that corpse would put TWO lockers in the world: the real
-- one on disk and a stale copy with stale contents.
--
-- So the handle is NEVER trusted across a reconcile. Every reconcile re-finds the
-- object by scanning the remembered square for our modData tag, and RR_Locker.plan
-- refuses to create anything while the remembered square is not loaded -- an
-- unloaded chunk is not evidence of loss. Costs nothing in practice: the loco only
-- moves with a driver aboard, and that means the cab's chunk is loaded.
--
-- ==== TWO VANILLA SYSTEMS THAT WOULD OTHERWISE HELP THEMSELVES ====
--
--   * setExplored(true) at creation. Otherwise ISInventoryPage:checkExplored:1511
--     runs ItemPicker.fillContainer the first time the panel shows it and rolls
--     random loot into the cab.
--   * LootRespawn (LootRespawn.java:130-147) tops up any explored+looted container
--     in a TownZone -- and the Muldraugh depot may well be in one. Our container type
--     has no distribution, so fillContainer has nothing to put in it, but this is the
--     reason the type is ours and not "crate".
--
-- ==== NOT VERIFIED IN-GAME YET ====
--
-- Written 2026-08-08, syntax-checked and unit-tested, NOT yet run in the game. The
-- two things to look at in the first pass are marked below: HIDE (does alpha 0 really
-- keep the object off the screen) and the reconcile cost while driving.
--***********************************************************************

print("[Railroader] RR_CabLocker.lua: loading...")
require("Railroader/RR_Locker")

local Locker = RR and RR.Locker

local CabLocker = {}
CabLocker.enabled      = true
CabLocker.MIN_INTERVAL = 0.5     -- s between reconciles; caps the move rate at 2/s
CabLocker.FOLLOW_SLACK = 1       -- tiles of drift tolerated before the locker is moved.
                                 -- 1 is free: the loot panel scans the 3x3 around the
                                 -- player, so a locker one tile off is still reachable.
CabLocker.HIDE         = true    -- pin alpha to 0 so the object is never drawn
CabLocker.NO_HIGHLIGHT = true    -- ...and keep it out of the container highlight pass,
                                 -- which draws it whatever its alpha is (see markNoHighlight)

-- The sprite has to EXIST (a chunk stores objects by sprite and resolves them on load)
-- and it has to be INERT. Verified against newtiledefinitions.tiles with
-- tools/dump_tiledefs.py -- this tile's ENTIRE property list is `attachedFloor`:
--   * no solid / solidtrans / collideN / collideW  => RR_ObstacleSweep's blocksMovement
--     cannot see it even before the isOurs() guard, and it never blocks a character;
--   * no `container` flag => IsoObject:5254 will not build a SECOND container over ours;
--   * nothing that maps it to an IsoDoor/IsoWindow/IsoThumpable subclass, so
--     getObjectName() falls through to "IsoObject" (:1029-1034) and the chunk rebuilds
--     it as a plain object. The create log below prints the name it actually got, so a
--     wrong pick shows up in console.txt instead of as a mystery after a reload.
-- Deliberately NOT the neighbouring _52 that RR_CabClimate uses: that one also carries
-- BlocksPlacement, and this object travels the whole county -- it must not drag a
-- "you cannot build here" square along the right-of-way behind it.
local BOX_SPRITE = "industry_railroad_05_54"
local TAG        = "rrCabBox"    -- IsoObject modData flag; IsoObject.save:1466 persists it

--------------------------------------------------------------------------
-- cabTile(rec): the driver's seat tile. Deliberately NOT re-implemented here --
-- RR_CabClimate owns it and the Z is FLOORED rather than rounded for a reason that
-- is easy to get wrong twice (see its comment). Looked up at call time, not at load
-- time, so file order cannot matter.
--------------------------------------------------------------------------
local function cabTile(rec)
    local f = RR.CabClimate and RR.CabClimate.cabTile
    if not f then return nil end
    local x, y, z = f(rec)
    if not x then return nil end
    return { x = x, y = y, z = z }
end

local function squareAt(t)
    if not t then return nil end
    local sq
    pcall(function() sq = getCell():getGridSquare(t.x, t.y, t.z) end)
    return sq
end

--------------------------------------------------------------------------
-- findOn(sq): our locker object on this square, or nil. Identified by the modData
-- tag, which is what survives a save/load round trip (a Lua handle is not).
--------------------------------------------------------------------------
local function findOn(sq)
    if not sq then return nil end
    local found
    pcall(function()
        local objs = sq:getObjects()
        if not objs then return end
        for i = 0, objs:size() - 1 do
            local o = objs:get(i)
            local md
            pcall(function() md = o and o:getModData() end)
            if md and md[TAG] == true then found = o; return end
        end
    end)
    return found
end

--------------------------------------------------------------------------
-- container(o): the locker's ItemContainer, or nil.
--------------------------------------------------------------------------
local function container(o)
    local c
    pcall(function() c = o and o:getContainer() end)
    return c
end

--------------------------------------------------------------------------
-- dress(o, sq): (re-)apply everything about the container that is NOT persisted, and
-- everything the owner may have retuned from the console since the last reconcile.
--
-- ItemContainer.save (:2424) writes type, explored, items, hasBeenLooted and capacity
-- -- and NOTHING else. In particular acceptItemFunction is dropped on every reload,
-- so a locker that refused furniture yesterday would happily take a fridge today if
-- this were done only at creation.
--------------------------------------------------------------------------
local function dress(o, sq)
    local c = container(o)
    if not c then return false end
    pcall(function()
        c:setCapacity(Locker.C.CAPACITY)
        c:setExplored(true)                                -- keep ItemPicker out of it
        c:setAcceptItemFunction("RR.CabLocker.acceptItem") -- dotted names resolve (LuaManager:1403)
        if sq then c:setSourceGrid(sq) end
    end)
    if CabLocker.HIDE then
        -- FIRST IN-GAME CHECK. Our tick runs after IsoWorld.update and before the
        -- render (the same ordering the collider clamp relies on), so pinning both
        -- alpha and target each reconcile should mean the object is never drawn. If a
        -- crate-ish sprite turns out to be visible on the rails anyway, this knob is
        -- where to look -- not the sprite.
        pcall(function() o:setAlphaAndTarget(0.0) end)
    end
    return true
end

--------------------------------------------------------------------------
-- markNoHighlight(rec, o): keep the locker out of the container HIGHLIGHT.
--
-- FOUND IN THE FIRST IN-GAME PASS (owner, 2026-08-08): the locker worked, but
-- selecting it lit up scenery on the track. HIDE (alpha 0) only covers the ordinary
-- render pass -- the highlight/outline pass draws the object regardless, so the one
-- thing that made the locker visible was looking at it.
--
-- Vanilla provides the escape hatch itself:
--     -- Hack to give priority to another piece of code highlighting an object.
--     function ISInventoryPage.OnObjectHighlighted(playerNum, object, isHighlighted)
-- (ISInventoryPage.lua:427-434). BOTH highlight paths consult that table and back
-- off: shouldSetOutlineHighlight:1999 (mouse-over) and updateContainerHighlight:455
-- and :466 (the selected container). So we claim ownership of the locker's highlight
-- and then simply never highlight it.
--
-- THE TRAP: that same check also stops vanilla turning a highlight OFF. Registering
-- an object that is already lit would freeze it lit forever. So the highlight is
-- cleared here first -- the same three calls as ISInventoryPage:stopHighlightContainer
-- -- and only then claimed.
--
-- Registration is per player index (split screen; harmless where a player does not
-- exist, OnObjectHighlighted nil-guards getPlayerData). The previous object is always
-- released first: after a chunk reload the locker is a NEW Java object, and the table
-- is keyed by object, so never releasing would pile up dead keys for the session.
--------------------------------------------------------------------------
local function markNoHighlight(rec, o)
    if not (CabLocker.NO_HIGHLIGHT and rec) then return end
    if rec.boxHl == o then return end

    local function tell(obj, on)
        if not obj then return end
        for pn = 0, 3 do
            pcall(function()
                if on then
                    -- Put it out ourselves before claiming it -- see THE TRAP above.
                    obj:setHighlighted(pn, false, false)
                    obj:setOutlineHighlight(pn, false)
                    obj:setOutlineHlAttached(pn, false)
                end
                ISInventoryPage.OnObjectHighlighted(pn, obj, on)
            end)
        end
    end

    tell(rec.boxHl, false)
    rec.boxHl = o
    tell(o, true)
end

--------------------------------------------------------------------------
-- build(sq): a brand-new empty locker on this square.
--------------------------------------------------------------------------
local function build(sq)
    local o, c
    local ok, err = pcall(function()
        o = IsoObject.new(sq, BOX_SPRITE)
        c = ItemContainer.new(Locker.C.TYPE, sq, o)
        o:setContainer(c)
        o:getModData()[TAG] = true
        sq:AddTileObject(o)
    end)
    if not (ok and o and c) then
        print("[Railroader] cab locker: FAILED to build -- " .. tostring(err)
              .. " -- there is no storage in this cab this session.")
        return nil
    end
    dress(o, sq)
    return o
end

--------------------------------------------------------------------------
-- move(o, from, to): carry the locker to the new cab tile.
--
-- The object is re-used rather than rebuilt, so the contents never pass through our
-- hands: nothing to lose, nothing to duplicate. setSquare + setSourceGrid keep the
-- object and its container agreeing about where they are -- ItemContainer.isExistYet
-- (:3387-3391) tests the parent against sourceGrid, and while
-- SystemDisabler.doWorldSyncEnable is false that test is skipped, but a container
-- that lies about its square is a trap laid for the day it isn't.
--------------------------------------------------------------------------
local function move(o, fromSq, toSq)
    local ok = pcall(function()
        if fromSq then fromSq:RemoveTileObject(o) end
        o:setSquare(toSq)
        local c = o:getContainer()
        if c then c:setSourceGrid(toSq) end
        toSq:AddTileObject(o)
    end)
    if not ok then return false end
    dress(o, toSq)
    return true
end

--------------------------------------------------------------------------
-- acceptItem(cont, item): the engine's per-item veto, called from
-- ItemContainer.isItemAllowed (:291-300) as a plain function with (container, item).
-- Must return exactly true to accept -- Java tests `accept != Boolean.TRUE`.
--
-- Classification is the only thing that needs the engine; the rule is RR_Locker's.
--------------------------------------------------------------------------
function CabLocker.acceptItem(cont, item)
    local moveable = false
    pcall(function() moveable = instanceof(item, "Moveable") == true end)
    return Locker.accepts({ moveable = moveable }) == true
end

--------------------------------------------------------------------------
-- onSpawn(rec, x, y, z): give the record its locker state. The coordinates come from
-- the loco's modData on a re-adoption (rrBoxX/Y/Z) and are nil for a fresh spawn --
-- in which case the first reconcile builds one on the cab tile.
--------------------------------------------------------------------------
function CabLocker.onSpawn(rec, x, y, z)
    if not (CabLocker.enabled and rec) then return end
    rec.box    = (x and y and z) and { x = x, y = y, z = z } or nil
    rec.boxObj = nil
    rec.boxHl  = nil          -- no highlight claim yet; the first reconcile makes it
    rec.boxAcc = 0
end

--------------------------------------------------------------------------
-- update(rec, dt): called every tick from RR_TrainEntity (driving AND derailed -- a
-- wreck still has a cab you can reach into).
--------------------------------------------------------------------------
function CabLocker.update(rec, dt)
    if not (CabLocker.enabled and rec) then return end

    local cab = cabTile(rec)

    -- WHY THIS IS NOT ON A TIMER ANY MORE (owner report, 2026-08-08: "while driving the
    -- locker vanishes now and then and the tile container shows up instead").
    --
    -- It was reconciled every MIN_INTERVAL. At 20 m/s the cab covers ~6 tiles a second,
    -- so between two reconciles the locker was left up to TEN tiles behind -- far
    -- outside the 3x3 the loot panel scans around the player (ISInventoryPage.lua:1667).
    -- Vanilla then simply did not find it, dropped the button, and -- because the
    -- selected container had gone -- fell back to backpacks[1] (:1839-1845). That
    -- fallback is the FLOOR container, whose capacity nobody ever sets, so it reads 50
    -- (GetFloorContainer:1531 -> ItemContainer default :86). Anything moved into the
    -- panel at that moment went onto the ground under the locomotive.
    --
    -- So the position test now runs EVERY tick and is deliberately cheap: three integer
    -- compares against the remembered tile, no square lookup and no object scan. The
    -- expensive path (find / move / build) only runs when the locker has actually
    -- fallen outside FOLLOW_SLACK, or on the slow timer for the upkeep dress() does.
    local slack = CabLocker.FOLLOW_SLACK or 0
    local near  = (cab ~= nil) and (rec.box ~= nil)
                  and rec.box.z == cab.z
                  and math.abs(rec.box.x - cab.x) <= slack
                  and math.abs(rec.box.y - cab.y) <= slack

    rec.boxAcc = (rec.boxAcc or 0) + (dt or 0)
    local due  = rec.boxAcc >= CabLocker.MIN_INTERVAL
    if near and not due then return end
    if due then rec.boxAcc = 0 end

    local cabSq  = squareAt(cab)
    local curSq  = squareAt(rec.box)
    -- Re-find every reconcile. The handle from last time may be a corpse from a chunk
    -- that has since been unloaded and streamed back in; see the header.
    local obj    = findOn(curSq)
    rec.boxObj   = obj
    -- Claim (or release) the highlight as soon as we know which object is ours this
    -- reconcile -- including the nil case, so a locker that has gone away releases
    -- its claim instead of leaving a dead key behind.
    markNoHighlight(rec, obj)
    -- A locomotive keeps her own operating manual in the cab (training system, Task 2.I).
    -- Done on the reconcile as well as on "create" because a freshly spawned locomotive
    -- whose cab square was not loaded yet gets her locker a few ticks later. Whether she
    -- is OWED one at all is not decided here: RR_Licence arms that at spawn, so a
    -- locomotive re-adopted from an old save is never handed one retroactively.
    -- Cheap after the first time: seedCab returns on one modData read.
    if obj and RR.Licence and RR.Licence.seedCab then
        pcall(function() RR.Licence.seedCab(rec, obj:getContainer()) end)
    end

    local action = Locker.plan({
        cur       = rec.box,
        curLoaded = curSq ~= nil,
        found     = obj ~= nil,
        cab       = cab,
        cabLoaded = cabSq ~= nil,
    })

    if action == "idle" or action == "wait" then return end

    if action == "keep" then
        dress(obj, curSq)                      -- live knobs + the un-persisted accept fn
        return
    end

    if action == "move" then
        -- `near` was computed at the top of the tick from the same slack: a locker
        -- within FOLLOW_SLACK of the seat is still inside the loot panel's 3x3, so
        -- there is nothing to gain by moving it -- and each move costs an
        -- AddTileObject -> RecalcAllWithNeighbours on both squares. Slack must stay
        -- at 1 or below: at 2 the locker can leave the 3x3 and the button flickers,
        -- which is the bug this whole comment block exists because of.
        if near then
            dress(obj, curSq)
            return
        end
        if move(obj, curSq, cabSq) then
            rec.box = { x = cab.x, y = cab.y, z = cab.z }
        end
        return
    end

    if action == "create" then
        local o = build(cabSq)
        if o then
            rec.boxObj = o
            rec.box    = { x = cab.x, y = cab.y, z = cab.z }
            markNoHighlight(rec, o)   -- claim it now, not on the next reconcile
            -- The object name is printed because it is what the chunk writes as the
            -- class id: anything but "IsoObject" means the sprite carries a parent
            -- object name and the locker would come back as some other class after a
            -- reload. Cheap to say once, very hard to diagnose later.
            local nm
            pcall(function() nm = o:getObjectName() end)
            print(string.format("[Railroader] cab locker placed at (%d,%d,%d), capacity %d, class '%s'.",
                  cab.x, cab.y, cab.z, Locker.C.CAPACITY, tostring(nm)))
            -- The other half of the seeding above: a brand-new locker has no `obj` yet
            -- at the point that runs. RR_Licence owns the rule and the flags, and they
            -- live on the LOCO rather than on the locker precisely because "create" also
            -- fires when a locker was provably lost -- a rebuilt locker must not print a
            -- fresh manual.
            if RR.Licence and RR.Licence.seedCab then
                pcall(function() RR.Licence.seedCab(rec, o:getContainer()) end)
            end
        end
    end
end

--------------------------------------------------------------------------
-- onRemove(rec): the loco is gone (debug C / TrainEntity.clear). The locker goes with
-- it -- an invisible container full of loot left standing on the rails forever is a
-- worse outcome than losing it, and C is a debug key that already deletes the loco.
-- Says what was inside, so it is never a silent loss.
--------------------------------------------------------------------------
function CabLocker.onRemove(rec)
    if not rec then return end
    markNoHighlight(rec, nil)          -- release the highlight claim before the object goes
    local sq = squareAt(rec.box)
    local o  = rec.boxObj or findOn(sq)
    if o and sq then
        local n = 0
        pcall(function()
            local c = o:getContainer()
            if c and c:getItems() then n = c:getItems():size() end
        end)
        pcall(function() sq:RemoveTileObject(o) end)
        if n > 0 then
            print("[Railroader] cab locker removed with " .. tostring(n) .. " item(s) in it.")
        end
    end
    rec.box    = nil
    rec.boxObj = nil
end

--------------------------------------------------------------------------
-- containerOf(rec): this loco's locker ItemContainer, or nil.
--
-- Used by RR_LockerAccess to answer "is the button in front of me the locker of the
-- cab I am sitting in". The cached handle is tried first and the square is re-scanned
-- if it is missing, because a panel refresh can land between reconciles -- and being
-- briefly unable to name the driver's own locker would lock him out of it.
--------------------------------------------------------------------------
function CabLocker.containerOf(rec)
    if not rec then return nil end
    local o = rec.boxObj
    local c = container(o)
    if c then return c end
    o = findOn(squareAt(rec.box))
    if o then rec.boxObj = o end
    return container(o)
end

--------------------------------------------------------------------------
-- isOurs(o): identity test for RR_ObstacleSweep, which must never treat the loco's
-- own locker as something to flatten or to hard-stop against.
--
-- Deliberately an identity comparison against the live records rather than a modData
-- read: blocksMovement runs over every object on every swept square every tick, and
-- IsoObject.getModData() ALLOCATES the table when the object hasn't got one -- doing
-- that per object per frame would quietly grow the save and the heap.
--------------------------------------------------------------------------
function CabLocker.isOurs(o)
    if not o then return false end
    local list = (RR.TrainEntity and RR.TrainEntity.active) or {}
    for i = 1, #list do
        if list[i].boxObj == o then return true end
    end
    return false
end

--------------------------------------------------------------------------
-- Console helper (not gated -- it cannot be hit by accident and it is how a tester
-- answers "where did my stuff go"). Prints, per loco, where the locker is believed
-- to be, whether that square is loaded, whether the object is really on it, and how
-- full it is.
--------------------------------------------------------------------------
function CabLocker.report()
    local list = (RR.TrainEntity and RR.TrainEntity.active) or {}
    print(string.format("[Railroader] cab locker: capacity %d  type '%s'  moveables %s  hide=%s  noHighlight=%s",
          Locker.C.CAPACITY, Locker.C.TYPE,
          Locker.C.BLOCK_MOVEABLES and "REFUSED" or "allowed",
          tostring(CabLocker.HIDE), tostring(CabLocker.NO_HIGHLIGHT)))
    if #list == 0 then print("  (no loco in the world)") end
    for i, e in ipairs(list) do
        local sq  = squareAt(e.box)
        local o   = findOn(sq)
        local w, n = -1, -1
        pcall(function()
            local c = o and o:getContainer()
            if c then w = c:getCapacityWeight(); n = c:getItems():size() end
        end)
        print(string.format("  loco %d: box=%s  square=%s  object=%s  %d item(s), %.1f/%d used",
              i,
              e.box and string.format("(%d,%d,%d)", e.box.x, e.box.y, e.box.z) or "none",
              sq and "loaded" or "not loaded",
              o and "present" or "MISSING",
              n, w, Locker.C.CAPACITY))
    end
end

RR = RR or {}
RR.CabLocker = CabLocker
print("[Railroader] RR_CabLocker.lua: loaded OK.")

return CabLocker
