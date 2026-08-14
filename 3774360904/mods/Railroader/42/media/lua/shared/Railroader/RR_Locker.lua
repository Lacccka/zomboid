--***********************************************************************
-- Railroader / RR_Locker -- the pure rules of the cab locker (storage in the cab).
--
-- Engine-free: capacity, what the locker refuses, and the placement decision. The
-- engine half is client/Railroader/RR_CabLocker.lua.
--
-- ==== WHY THERE IS A REAL WORLD OBJECT BEHIND THIS ====
--
-- PZ anchors item persistence to exactly three things: a square (chunk data), a
-- character's inventory, or an item inside one of those. There is no free-floating
-- item store, and the loco is none of the three:
--
--   * IsoAnimal.save (IsoAnimal.java:1292-1400) is a full override and writes pose,
--     genome, stress and getModData() -- NO inventory. So the loco's own
--     getInventory() exists at runtime and is empty after every reload.
--   * modData is a Kahlua table, so putting items there means hand-serialising them:
--     rot timers, condition, nested bags and -- the one that matters here -- a
--     FluidContainer full of diesel. Lossy by construction.
--
-- So the locker is a real IsoObject carrying a real ItemContainer, standing on the
-- cab's tile and moved along with it. That buys, with no code of ours:
--   * persistence at full item fidelity (IsoObject.save:1453-1464 writes every
--     container; load:1210-1243 restores them regardless of the sprite's flags)
--   * the loot window (ISInventoryPage:refreshBackpacks:1728-1745 scans the 3x3
--     around the player and adds a button per getContainerByIndex) -- the driver is
--     pinned to the cab tile, so the locker is on his own square. Zero UI code.
--
-- ==== CAPACITY 50 (owner, 2026-08-09; was 30 from 2026-08-08) ====
--
-- The vanilla scale, for calibration: player base carry 8 (IsoGameCharacter:575),
-- glovebox 5, car seat 20, small-car trunk 40, station wagon 60, pickup bed 100,
-- default world container 50 (ItemContainer:86).
--
-- Two items set the floor, and BOTH are load-bearing for a mechanic of ours:
--
--   * a full 20 L diesel can weighs 1.6 + 20 = 21.6, because
--     InventoryItem.getContentsWeight (:3407) returns a FluidContainer's litres
--     DIRECTLY (railroader_fluids.txt). The spare can is what a loco cab carries.
--   * a GENERATOR weighs 40.0 flat (scripts/generated/items/normal.txt: item
--     Generator / _Yellow / _Blue / _Old, all 40.0). That is not decoration: a
--     running generator powering the loco's square is the ONLY way back from a
--     flat battery (RR_Ride.startEnv -> env.externalPower, Task 1.M). A cab that
--     cannot stow the genset makes the recovery kit un-carryable by the vehicle it
--     recovers -- so 30 was quietly a gameplay wall, not a flavour number.
--
-- 50 takes the generator (leaving 10, about one player's carry) OR two cans and
-- change. It does NOT take a generator AND a can (61.6) -- deliberate: the cab is
-- the recovery kit or the fuel kit, not a freight car. The boxcar (ROADMAP task
-- 3.B) is still the intended answer to hauling.
--
-- KNOWN EDGE, accepted: getEffectiveCapacity (:194-207) scales a non-character
-- container by the owner's trait -- ORGANIZED x1.3 (=65), DISORGANIZED x0.7 (=35).
-- A disorganized engineer therefore still cannot stow a generator. He is not locked
-- out of the mechanic (the jump-start needs the generator ON THE GROUND and running,
-- never in the locker), only out of carrying it in the cab -- which is exactly what
-- Disorganized does to every car trunk in the game. Covering him would need 58+.
--
-- HARD CEILING 100: getCapacity (ItemContainer:154-165) clamps a container on a
-- plain IsoObject to min(capacity,100). Only a BaseVehicle parent gets 1000. When
-- the boxcar lands it will need SEVERAL containers on one object
-- (addSecondaryContainer) -- no capacity number can get past this.
--
-- ==== WHY CAPACITY CANNOT KEEP FURNITURE OUT ====
--
-- Careful: it is NOT the weights that separate a generator from a fridge -- they are
-- the same number. A moveable weighs PickUpWeight/10 (ISMoveableSpriteProps.lua:141-142),
-- and the fridge declares PickUpWeight = 400 (newtiledefinitions.tiles.txt,
-- appliances_refrigeration_01_0), i.e. 40.0 -- to the gram what a generator weighs.
-- (An earlier version of this header used the /10 DEFAULT of 5.0 and concluded six
-- fridges fit in 30. Wrong: the fridge declares its own weight. Do not restore it.)
--
-- What separates them is the CLASS, not the scale: a generator is a plain
-- ItemType = base:normal item, a fridge picked up is a Moveable. So BLOCK_MOVEABLES
-- is the whole lever, fed to ItemContainer.setAcceptItemFunction, which the engine
-- calls from isItemAllowed (ItemContainer.java:291-300) so it closes drag-and-drop
-- and the context menu at once -- and it keeps working at ANY capacity. Raising the
-- number to 50 does not let one fridge in, because no number ever did.
--
-- In the real thing the constraint is the DOORWAY, not the floor: a GP7 cab door is
-- roughly two feet wide, a period fridge about thirty inches. It does not go in. The
-- railroad answer to "move a fridge" was always a car, never the cab.
--
-- ==== THE PLAYER NEVER SEES THE WORD "LOCKER" (owner, 2026-08-09) ====
--
-- The loot-window label is "Cab" / "Кабина" (IGUI_ContainerTitle_rrcab), NOT the
-- "Cab locker" / "Рундук в кабине" it read until then. That old name leaked the
-- implementation: this is ONE IsoObject only because PZ gives us nowhere else to
-- anchor items, but what the player is looking at is the WHOLE CAB -- the locker
-- behind the seat, the shelf under the window, the floor beside the control stand --
-- exactly as a car's container is "Seat" and not "the box under the seat". Naming one
-- box also invites "why can't I just put it on the cab floor then?", and answers it
-- wrongly: the cab floor is a world SQUARE and stays behind when she pulls out (which
-- is also why HIDE_FLOOR exists).
--
-- The internal names stay: RR_Locker / RR_CabLocker / RR_LockerAccess, and above all
-- TYPE = "rrcab", which ItemContainer.save:2424-2425 writes into the save as a raw
-- string and load:2438 reads back -- renaming it orphans the container in every
-- existing save. So: LOCKER is our word for the mechanism, CAB is the player's word
-- for the place. Don't let a later edit put "locker" back in front of a player.
--***********************************************************************

RR = RR or {}

local Locker = {}

--------------------------------------------------------------------------
-- Tunables. Live: every one of these can be changed from the console mid-session
-- (the adapter re-reads them on the next reconcile), so an owner pass can A/B a
-- number without a rebuild -- the same contract as RR.Obstacle.SOLID_IS_HARD.
--------------------------------------------------------------------------
Locker.C = {
    CAPACITY        = 50,          -- see the header. Engine clamps to 100 whatever we say.
    TYPE            = "rrcab",     -- ItemContainer type; drives IGUI_ContainerTitle_rrcab ("Cab").
                                   -- SAVE FORMAT: written as a raw string, ItemContainer:2424.
    BLOCK_MOVEABLES = true,        -- refuse furniture/appliances (Moveable items)
    SEATED_ONLY     = true,        -- the locker is reachable only from the driver's seat
    HIDE_FLOOR      = true,        -- no Floor container while sitting in the cab, as in a car
    HIDE_WORLD      = true,        -- ...and no corpses/crates/bags either: from the seat the
                                   -- loot panel offers the cab and nothing else, as in a car
    DROP_TRACKSIDE  = true,        -- things dropped from the cab land beside the track, not in it
    DROP_SIDE       = 3,           -- tiles from the centreline; see dropTiles for why 3 and not 2
}

--------------------------------------------------------------------------
-- dropTiles(cab, dirX, dirY, dist) -> ordered candidate tiles to drop onto.
--
-- Dropping from the cab used to put things BETWEEN THE RAILS, under the locomotive
-- (owner, 2026-08-08) -- because vanilla's chooser starts at the character's own
-- square (ISInventoryTransferAction:getNotFullFloorSquare:612) and the driver is
-- pinned to the cab tile. Vanilla solves the same problem for cars and solves it the
-- way we want: ISInventoryPaneContextMenu.dropItem:3859-3866 uses
-- ISDropVehicleItemAction with vehicle:getSquareForArea(door:getArea()) -- the square
-- OUTSIDE the door. We cannot use that (it needs a BaseVehicle), but the shape of the
-- answer is settled: drop beside, not underneath.
--
-- Candidates run out from the centreline perpendicular to travel, engineer's side
-- (right of travel) first, then the fireman's, then one tile further out on each.
--
-- WHY dist DEFAULTS TO 3 AND NOT 2. The drawn hull is Body.hullExtents wide --
-- 3.56 * 1.5 * 0.7 - 2*0.3 = 3.14 tiles, i.e. 1.57 either side of the centre, and the
-- raw mesh is 1.87. On a straight, 2 would just clear it; but our rails run at 45
-- degrees as often as not, and there a perpendicular offset of 2 rounds to one tile
-- diagonally -- 1.41 tiles from the centre, which is UNDER the frame. 3 rounds to two
-- tiles diagonally (2.83) and stays 3 on the axes. Both are clear of the hull, and
-- both land about where the ballast shoulder is.
--------------------------------------------------------------------------
function Locker.dropTiles(cab, dirX, dirY, dist)
    local out = {}
    if not (cab and dirX and dirY) then return out end
    if dirX == 0 and dirY == 0 then return out end
    dist = dist or Locker.C.DROP_SIDE
    local rx, ry = -dirY, dirX                     -- right of travel
    local function push(d, s)
        local x = cab.x + math.floor(rx * d * s + 0.5)
        local y = cab.y + math.floor(ry * d * s + 0.5)
        if x ~= cab.x or y ~= cab.y then
            out[#out + 1] = { x = x, y = y, z = cab.z }
        end
    end
    for _, d in ipairs({ dist, dist + 1 }) do
        push(d,  1)                                -- engineer's side
        push(d, -1)                                -- fireman's side
    end
    return out
end

--------------------------------------------------------------------------
-- floorVisible(inCab, alone) -> should the FLOOR container be offered right now?
--
-- Vanilla never offers the floor to someone in a vehicle: refreshBackpacks has a
-- separate `elseif playerObj:getVehicle()` branch (ISInventoryPage.lua:1581) that
-- lists only the vehicle's own containers, and the floor is appended solely in the
-- on-foot branch (:1797). Our driver is not in a vehicle as far as the engine is
-- concerned, so he was being offered the ground under the locomotive.
--
-- That was not merely odd, it cost the owner items (2026-08-08): when the locker
-- button flickered out at speed, vanilla fell back to backpacks[1] -- the floor --
-- and things moved in the panel went onto the track. The flicker is fixed in
-- RR_CabLocker; this closes the trap door it dropped him through.
--
-- `alone` is the safety catch: refreshBackpacks ends with
-- self.backpacks[#self.backpacks]:getBottom() (:1902), so an EMPTY button list is a
-- nil-index crash in vanilla UI code. If the floor is the only button left -- the
-- half-second after boarding, before the locker exists -- it stays.
--------------------------------------------------------------------------
function Locker.floorVisible(inCab, alone)
    if Locker.C.HIDE_FLOOR ~= true then return true end
    if inCab ~= true then return true end
    if alone == true then return true end
    return false
end

--------------------------------------------------------------------------
-- worldVisible(inCab) -> may the loot panel offer a container that is neither ours
-- nor the floor: a CORPSE, a crate, a bag lying on the ballast, a parked car?
--
-- Owner report, 2026-08-09: "from the locomotive's cab you can reach dead zombies'
-- inventories." You can, and it is the same hole HIDE_FLOOR closed one plank of.
-- The on-foot branch of refreshBackpacks walks the 3x3 around the player and adds a
-- button for every container it finds -- world items (ISInventoryPage.lua:1691),
-- getStaticMovingObjects, which is where IsoDeadBody lives (:1709-1727), objects
-- with containers (:1729), and vehicles (:1758). The vehicle branch (:1581) does
-- NONE of that: a driver sees his car's own containers and nothing else, because
-- reaching through a windscreen into a corpse is not a thing. Our driver gets the
-- on-foot branch, since the engine does not consider an IsoAnimal a vehicle.
--
-- So the locomotive was doing what a car cannot, and doing it worse: the loco parks
-- ON the bodies it just ran over, so the panel filled with the corpses of the crowd
-- the driver had flattened, lootable from the seat at 0 risk. This makes the cab
-- behave like a car -- the cab, and nothing else, until you step down.
--
-- Deliberately WIDER than the report. Crates and bags go too, and that is the point:
-- a car cannot load a crate through the door either, and enumerating "corpses but
-- not crates" would be the same losing game as enumerating obstacle props (see
-- RR_Obstacle's inverted fallback). One rule, matching vanilla's.
--
-- LOOT PANEL ONLY -- the caller must not apply this to the character page, where
-- these same buttons are the player's own bags.
--------------------------------------------------------------------------
function Locker.worldVisible(inCab)
    if Locker.C.HIDE_WORLD ~= true then return true end
    return inCab ~= true
end

--------------------------------------------------------------------------
-- visible(s) -> should this container be offered to the player right now?
--
--   s.ours   bool   is this one of OUR lockers (container type == C.TYPE)?
--   s.seated bool   is the player sitting in the cab THIS locker belongs to?
--
-- This decides OUR lockers only. Whether a container that is not ours may be shown
-- is worldVisible's job above -- on foot a kitchen counter beside the track shows up
-- as it always did; from the seat nothing outside the cab does.
--
-- WHY THIS IS A UI RULE AND NOT A GEOMETRIC ONE. The driver is pinned to the cab
-- tile, so the locker has to sit within one tile of him to be reachable at all --
-- and every square in that radius is open track anyone can walk onto. There is no
-- position that reaches the seat and not the ground beside it. So "inside the cab"
-- can only ever be a rule about who is asking.
--
-- WHY REMOVING A BUTTON IS A REAL LOCK RATHER THAN A COSMETIC ONE.
-- ISInventoryPaneContextMenu.getContainers (:2425-2443) -- vanilla's own answer to
-- "what is within reach" -- does NOT scan the world. It reads
-- getPlayerLoot(n).inventoryPane.inventoryPage.backpacks, i.e. the very button list.
-- The context menu, repairs (fixer materials), evolved recipes and the hand-craft
-- panel (ISHandCraftPanel.lua:195) all go through it. So one removal closes all of
-- them at once: from outside you cannot take from the locker, cook out of it or
-- repair out of it.
--
-- A DERAILED loco behaves exactly like an intact one (owner, 2026-08-08): you can
-- still climb into the cab of a wreck, and reaching the locker means doing so. That
-- needs no special case here -- "seated" is the whole test either way.
--------------------------------------------------------------------------
function Locker.visible(s)
    s = s or {}
    if s.ours ~= true then return true end          -- not ours: leave vanilla alone
    if Locker.C.SEATED_ONLY == false then return true end
    return s.seated == true
end

--------------------------------------------------------------------------
-- accepts(info) -> true if the locker takes this item.
--
-- `info` is what the adapter could learn about the item without the engine:
--     { moveable = bool }        -- instanceof(item, "Moveable")
-- A nil/empty info accepts, deliberately: an item we could not classify must not be
-- silently rejected (the same fail-open rule RR_MP uses -- a probe that throws must
-- not lock the player out of his own locker).
--------------------------------------------------------------------------
function Locker.accepts(info)
    if not info then return true end
    if Locker.C.BLOCK_MOVEABLES and info.moveable == true then return false end
    return true
end

--------------------------------------------------------------------------
-- sameTile(a, b) -> both non-nil and on the same integer tile.
--------------------------------------------------------------------------
function Locker.sameTile(a, b)
    if not (a and b) then return false end
    return a.x == b.x and a.y == b.y and a.z == b.z
end

--------------------------------------------------------------------------
-- plan(s) -> what the adapter should do with the locker object this reconcile.
--
--   s.cur       {x,y,z} | nil   where we last knew the object to be (from modData)
--   s.curLoaded bool            is that square streamed in right now?
--   s.found     bool            is OUR object actually on it?
--   s.cab       {x,y,z} | nil   the driver's seat tile this tick
--   s.cabLoaded bool            is THAT square streamed in?
--
-- Returns "idle" | "wait" | "create" | "keep" | "move".
--
-- THE WHOLE POINT OF THIS FUNCTION IS THE DUPE IT REFUSES TO MAKE. After a chunk
-- unloads and streams back in, the locker is a NEW Java object rebuilt from disk;
-- any Lua handle we cached is a corpse pointing at a discarded square. Re-adding
-- that corpse to a square would put TWO lockers in the world -- the real one from
-- disk and a stale copy with stale contents. So:
--
--   * we never create while we remember a position we cannot currently verify
--     ("wait", not "create") -- an unloaded chunk is not evidence of loss;
--   * "create" needs the remembered square LOADED and our object provably absent;
--   * the adapter never keeps a handle across a reconcile: it re-finds by tag.
--
-- This costs nothing in the normal case, because the loco only MOVES with a driver
-- aboard, and a driver aboard means the cab's chunk is loaded by definition.
--------------------------------------------------------------------------
function Locker.plan(s)
    s = s or {}
    if not s.cab then return "idle" end          -- no pose yet: nothing to place

    if s.cur then
        if not s.curLoaded then return "wait" end          -- unverifiable => never create
        if not s.found then
            -- The square IS loaded and our object is not on it: it is really gone
            -- (cleared, destroyed, or a map rework ate it). Only now may we rebuild.
            return s.cabLoaded and "create" or "wait"
        end
        if Locker.sameTile(s.cur, s.cab) then return "keep" end
        return s.cabLoaded and "move" or "wait"
    end

    return s.cabLoaded and "create" or "wait"
end

RR.Locker = Locker
return Locker
