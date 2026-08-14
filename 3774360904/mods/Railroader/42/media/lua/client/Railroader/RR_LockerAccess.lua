--***********************************************************************
-- Railroader / RR_LockerAccess -- the cab locker is reachable ONLY from the seat.
--
-- The rule is the pure, unit-tested RR_Locker.visible. This file is the one hook
-- that applies it.
--
-- ==== WHY A UI HOOK IS THE ONLY WAY ====
--
-- The driver is pinned to the cab tile, so the locker must sit within a tile of him
-- to be reachable at all -- and vanilla's loot panel scans the 3x3 around the PLAYER
-- (ISInventoryPage.lua:1667), not around the seat. Every square in that radius is
-- open track a survivor can walk onto, so no placement reaches the seat without also
-- reaching the ground beside it. "Inside the cab" cannot be geometry. It has to be a
-- rule about who is asking.
--
-- ==== WHY REMOVING A BUTTON IS A REAL LOCK, NOT A COSMETIC ONE ====
--
-- This is the part that made the feature worth building. Vanilla's own answer to
-- "what containers are within reach" does not scan the world -- it reads the button
-- list this file edits:
--
--   ISInventoryPaneContextMenu.getContainers (:2425-2443)
--       for i,v in ipairs(getPlayerLoot(playerNum).inventoryPane.inventoryPage.backpacks)
--
-- and that function is what the inventory context menu, repairs (fixer materials),
-- evolved recipes and the hand-craft panel (ISHandCraftPanel.lua:195) all use. So
-- dropping one button closes every one of them together: from outside the cab you
-- cannot take from the locker, cook out of it, or repair out of it. (Checked on the
-- Lua side; no exhaustive sweep of Java-side scanners was done.)
--
-- ==== WHY "buttonsAdded" AND NOT A WRAP ====
--
-- OnRefreshInventoryWindowContainers is a real registered event
-- (LuaEventManager.java:621) and fires at four stages inside refreshBackpacks.
-- "buttonsAdded" (:1801) is after every button exists and BEFORE anything that
-- depends on them: the selected-container search (:1803), the fallbacks at
-- :1835-1845, the highlight pass and the scroll height (:1902). All of those
-- re-derive from self.backpacks, so a removal here is simply not seen -- and if the
-- removed container happened to be the selected one, `found` stays false and vanilla
-- falls back to self.backpacks[1] by itself. No vanilla method is overridden.
--
-- The list cannot be emptied by us: the loot branch always appends Floor (:1797) and
-- the character branch always adds the player's own inventory, so
-- self.backpacks[#self.backpacks]:getBottom() at :1902 stays safe.
--
-- No RR_MP guard: on a connected client the mod never spawns a loco, so there is no
-- locker and no container of our type for this to find. It is inert rather than
-- gated, like RR_JournalJoypadFix.
--
-- NOT VERIFIED IN-GAME (written 2026-08-08).
--***********************************************************************

print("[Railroader] RR_LockerAccess.lua: loading...")
require("Railroader/RR_Locker")

local Locker = RR and RR.Locker

local Access = {}
Access.enabled = true
Access.FREEZE_WHILE_SEATED = true   -- stop the loot panel rebuilding itself under a moving driver

-- Rebuild counter (see the note above tickFreeze). Declared here because onRefresh --
-- which does the counting -- is defined below and a later local would be invisible to it.
local rebuilds, ticks = 0, 0

--------------------------------------------------------------------------
-- seatedContainer(): the locker of the cab the player is sitting in, or nil when he
-- is not driving anything. Everything else our type owns is then hidden.
--
-- A DERAILED loco is not a special case (owner, 2026-08-08): you climb into the cab
-- of a wreck the same way, and RR.Ride.current says so the same way.
--------------------------------------------------------------------------
local function seatedContainer()
    local rec = RR.Ride and RR.Ride.current
    if not rec then return nil end
    if not (RR.CabLocker and RR.CabLocker.containerOf) then return nil end
    return RR.CabLocker.containerOf(rec)
end

--------------------------------------------------------------------------
-- isOurType(cont): our locker, identified by CONTAINER TYPE rather than by a cached
-- object handle. The handle can be a corpse from a chunk that has been unloaded and
-- streamed back (see RR_CabLocker's header); the type survives the round trip
-- because ItemContainer.save (:2424) writes it.
--------------------------------------------------------------------------
local function isOurType(cont)
    local t
    pcall(function() t = cont and cont:getType() end)
    return t == Locker.C.TYPE
end

--------------------------------------------------------------------------
-- onRefresh(page, stage): drop the buttons the rule says this player may not use.
--------------------------------------------------------------------------
local function isFloor(cont)
    local t
    pcall(function() t = cont and cont:getType() end)
    return t == "floor"
end

local function onRefresh(page, stage)
    if stage ~= "buttonsAdded" then return end
    -- Counted before any early-out: this fires once per refreshBackpacks, which is
    -- exactly the thing that must stop happening while the driver is rolling.
    if page and page.onCharacter == false then rebuilds = rebuilds + 1 end
    if not (Access.enabled and Locker and page and page.backpacks) then return end

    local mine  = seatedContainer()
    local inCab = (RR.Ride and RR.Ride.current) ~= nil

    -- The LOOT page only (onCharacter is an explicit boolean, ISInventoryPage:1959;
    -- true on the player's own panel, false on the loot panel). The world rule below
    -- drops everything that is not the cab, and on the character page those same
    -- buttons are the player's own inventory and worn bags -- removing them there
    -- would empty his backpack list. Our lockers never appear on that page anyway.
    local isLoot = (page.onCharacter == false)

    -- Two passes. Decide first, then remove -- because the floor rule needs to know
    -- whether anything would be LEFT, and vanilla nil-indexes self.backpacks[#...]
    -- at :1902 if the list ends up empty.
    local doomed, keep = {}, 0
    for i = 1, #page.backpacks do
        local b    = page.backpacks[i]
        local cont = b and b.inventory
        local drop = false
        if isOurType(cont) then
            -- "seated" is exact identity: sitting in one cab does not open another's
            -- locker, which is the only case two lockers could ever be in one 3x3.
            drop = not Locker.visible({ ours = true, seated = (cont ~= nil and cont == mine) })
        elseif isFloor(cont) then
            drop = not Locker.floorVisible(inCab, false)
        elseif isLoot then
            -- Corpses, crates, bags on the ballast, a car on the crossing: a driver
            -- reaches none of them, exactly as in a vehicle (Locker.worldVisible).
            drop = not Locker.worldVisible(inCab)
        end
        doomed[i] = drop
        if not drop then keep = keep + 1 end
    end

    -- Never hand vanilla an empty list: refreshBackpacks ends on
    -- self.backpacks[#self.backpacks]:getBottom() (:1902).
    -- Revive the FLOOR rather than abandoning the whole pass, which is what this used
    -- to do: the corpses are the thing the rule is for, and showing them again to save
    -- a button would give the bug back for as long as the locker is missing (a chunk
    -- reload, or the moment right after boarding). Vanilla always appends the floor
    -- (:1797), so there is nearly always something to revive; if there is not, bail.
    if keep == 0 then
        local revived = false
        for i = 1, #page.backpacks do
            local b = page.backpacks[i]
            if doomed[i] and isFloor(b and b.inventory) then
                doomed[i] = false
                revived = true
                break
            end
        end
        if not revived then return end
    end

    local removed = false
    -- Backwards: table.remove shifts everything after the index.
    for i = #page.backpacks, 1, -1 do
        if doomed[i] then
            local b = page.backpacks[i]
            pcall(function() page.containerButtonPanel:removeChild(b) end)
            if page.buttonPool then table.insert(page.buttonPool, b) end
            table.remove(page.backpacks, i)
            removed = true
        end
    end

    if not removed then return end
    -- Re-lay out: addContainerButton (:1465) derives y from the index at creation
    -- time, so a hole in the middle of the list would leave a gap on screen.
    for i, b in ipairs(page.backpacks) do
        pcall(function() b:setY(((i - 1) * page.buttonSize) - 1) end)
    end
end

Events.OnRefreshInventoryWindowContainers.Add(onRefresh)

--------------------------------------------------------------------------
-- Freezing the panel under a moving driver (owner report, 2026-08-08: "the locker's
-- item list flickers on the move, and picking several items to move is impossible
-- because the selection is gone after every flicker").
--
-- The cause is vanilla and it is not about our locker at all:
--
--   ISInventoryPage:update (:500-507)
--       if not self.onCharacter then
--           if self.lastDir ~= playerObj:getDir() then ... self:refreshBackpacks()
--           elseif self.lastSquare ~= playerObj:getCurrentSquare() then ... refreshBackpacks()
--
-- The loot panel rebuilds itself on every change of the player's SQUARE or FACING.
-- Standing in a kitchen that is free; being carried along at 20 m/s it fires about
-- six times a second, plus once per curve. Vanilla does try to carry the selection
-- across a rebuild (saveSelection :2033 -> restoreSelection :2176), but the list is
-- destroyed and rebuilt each time, which is the flicker itself.
--
-- We cannot remove that code, but we can make its test true: while the driver is
-- seated we keep lastSquare/lastDir up to date OURSELVES, every tick, before the UI
-- reads them. Our OnTick runs after IsoWorld.update and before the render (the same
-- ordering the collider clamp relies on), so vanilla never sees a difference and
-- never rebuilds. Nothing is overridden and nothing is patched.
--
-- The rebuild is then forced deliberately at the four moments it is actually needed:
-- climbing in, stepping off (otherwise the locker button would linger after the rule
-- says it should be gone), the locker object changing identity, and the panel being
-- opened (so it is not showing what was near the cab when the driver sat down).
--
-- Cost while seated: two field writes a tick. The trade is that a container coming
-- into range mid-journey is not noticed until one of those four moments -- which is
-- also what being in a car is like, and what the cab is meant to feel like.
--------------------------------------------------------------------------
local wasSeated, lastBox, wasVisible

local function lootPage()
    local p = getPlayer()
    if not p then return nil, nil end
    local pd
    pcall(function() pd = getPlayerData(p:getPlayerNum() or 0) end)
    return pd and pd.lootInventory or nil, p
end

local function freezePanel()
    if not (Access.enabled and Access.FREEZE_WHILE_SEATED) then return end
    local page, p = lootPage()
    if not page then return end

    local rec = RR.Ride and RR.Ride.current
    -- rec.boxObj, not containerOf(): identity is all we need here and it costs no
    -- square scan, which matters in a per-tick path.
    local box = rec and rec.boxObj or nil
    local vis
    pcall(function() vis = page:getIsVisible() end)

    if rec ~= wasSeated or box ~= lastBox or (vis == true and wasVisible ~= true) then
        wasSeated, lastBox, wasVisible = rec, box, vis
        pcall(function() page:refreshBackpacks() end)
        return
    end
    wasSeated, lastBox, wasVisible = rec, box, vis

    if not rec then return end
    Access.stamp()
end

--------------------------------------------------------------------------
-- stamp(): tell the loot panel it already knows where the driver is and which way he
-- is looking. Cheap: two field writes.
--
-- IT MUST RUN LATE IN THE FRAME, AND THAT IS THE WHOLE POINT (owner, 2026-08-09: the
-- flicker was still there at speed after the first freeze).
--
-- The first version stamped from our own OnTick handler, and Lua event handlers fire
-- in REGISTRATION order -- which is file load order, alphabetical, so RR_LockerAccess
-- is registered before RR_TrainEntity. But it is RR_TrainEntity's tick that calls
-- Ride.pinRider, and pinRider calls setTargetAndCurrentDirection every frame, while
-- getDir() is getForwardIsoDirection() -- live, not cached (IsoObject:1908). So we
-- were stamping the heading from BEFORE the locomotive turned the driver and the
-- panel then read the one from AFTER: on every curve the two disagreed, every frame,
-- and vanilla rebuilt the list. Exactly the ordering bug that once produced the rider
-- camera jitter, and it has the same fix -- do it from RR_TrainEntity's tick, after
-- the pose and after pinRider, not from a handler that races it.
--
-- Still called from our own OnTick as well, for the frames where no locomotive ticks
-- (nobody seated). Stamping twice is harmless: the later one wins and it is the
-- correct one.
--------------------------------------------------------------------------
function Access.stamp()
    if not (Access.enabled and Access.FREEZE_WHILE_SEATED) then return end
    if not (RR.Ride and RR.Ride.current) then return end
    local page, p = lootPage()
    if not (page and p) then return end
    pcall(function()
        page.lastSquare = p:getCurrentSquare()
        page.lastDir    = p:getDir()
    end)
end

--------------------------------------------------------------------------
-- How often is the panel actually rebuilding? Counted rather than guessed, because
-- this defect has now been diagnosed twice from symptoms and once wrongly. onRefresh
-- above runs on every refreshBackpacks, so it is an exact count; report() prints the
-- rate over the last second. Seated and rolling it should read 0.
--------------------------------------------------------------------------
Access.rebuildRate = 0

local function tickFreeze()
    ticks = ticks + 1
    if ticks >= 60 then
        Access.rebuildRate, rebuilds, ticks = rebuilds, 0, 0
    end
    freezePanel()
end

Events.OnTick.Add(tickFreeze)

--------------------------------------------------------------------------
-- Dropping from the cab lands BESIDE the track, not between the rails.
--
-- Every drop route ends in the same place: the "Move To Floor" button, right-click ->
-- Drop (ISInventoryPaneContextMenu.dropItem:3868) and a drag onto the world all queue
-- an ISInventoryTransferAction whose destination is the floor container, and that
-- action picks the square in ONE method -- getNotFullFloorSquare (:611). It is used
-- both to validate (:100) and to actually place the item (transferItem :651), so
-- taking it over changes where things land rather than only whether they may.
--
-- Its first choice is the character's own square, and our driver is pinned to the cab
-- tile -- hence tools and cans sitting between the rails under the locomotive.
--
-- Vanilla has the same problem with cars and answers it the same way: dropItem
-- (:3859-3866) sends a player in a vehicle to ISDropVehicleItemAction, which drops at
-- vehicle:getSquareForArea(door:getArea()) -- the square outside the door. We cannot
-- use that action (it needs a BaseVehicle), so we do the same thing by hand: offer
-- the trackside squares from the pure RR_Locker.dropTiles and fall through to vanilla
-- untouched if none of them will take the item.
--
-- The wrapper is deliberately narrow: it does nothing unless the character IS the
-- seated driver and the destination IS the floor. Everyone else, and every other
-- transfer, reaches the original method unchanged.
--------------------------------------------------------------------------
local function squareUsable(action, sq, item)
    if not sq then return false end
    -- Vanilla's canDropOnFloor is not reused wholesale: it tests
    -- current:isBlockedTo(square), which is meant for the ADJACENT square and is not
    -- meaningful three tiles out. These are its checks that do apply out here.
    local okFloor, solid = false, true
    pcall(function()
        okFloor = sq:TreatAsSolidFloor() == true
        solid   = sq:isSolid() or sq:isSolidTrans()
    end)
    if not okFloor or solid then return false end
    local room = true
    pcall(function() room = action:floorHasRoomFor(sq, item) ~= false end)
    return room
end

function Access.trackSideSquare(action, item)
    if not (Access.enabled and Locker and Locker.C.DROP_TRACKSIDE) then return nil end
    local rec = RR.Ride and RR.Ride.current
    if not rec then return nil end
    local p = getPlayer()
    if not (p and action and action.character == p) then return nil end

    local t
    pcall(function() t = action.destContainer and action.destContainer:getType() end)
    if t ~= "floor" then return nil end

    local pose = rec.lastPose
    local f    = RR.CabClimate and RR.CabClimate.cabTile
    if not (pose and f) then return nil end
    local x, y, z = f(rec)                      -- three values, not a table
    if not x then return nil end

    local tiles = Locker.dropTiles({ x = x, y = y, z = z }, pose.dirX, pose.dirY,
                                   Locker.C.DROP_SIDE)
    for i = 1, #tiles do
        local sq
        pcall(function() sq = getCell():getGridSquare(tiles[i].x, tiles[i].y, tiles[i].z) end)
        if squareUsable(action, sq, item) then return sq end
    end
    return nil
end

local origFloorSquare = ISInventoryTransferAction.getNotFullFloorSquare
function ISInventoryTransferAction:getNotFullFloorSquare(item)
    local sq = Access.trackSideSquare(self, item)
    if sq then return sq end
    return origFloorSquare(self, item)
end

--------------------------------------------------------------------------
-- Console helper: why can (or can't) I see the locker right now?
--------------------------------------------------------------------------
function Access.report()
    print(string.format("[Railroader] locker access: enabled=%s  SEATED_ONLY=%s  HIDE_FLOOR=%s  "
          .. "HIDE_WORLD=%s  FREEZE=%s",
          tostring(Access.enabled), tostring(Locker.C.SEATED_ONLY), tostring(Locker.C.HIDE_FLOOR),
          tostring(Locker.C.HIDE_WORLD), tostring(Access.FREEZE_WHILE_SEATED)))
    -- The number that settles an argument about flicker: loot-panel rebuilds in the
    -- last second. Seated and rolling it must be 0; anything above that IS the flicker.
    print("  loot-panel rebuilds in the last second: " .. tostring(Access.rebuildRate))
    local rec = RR.Ride and RR.Ride.current
    print("  seated in a cab: " .. (rec and "yes" or "no"))
    if rec then
        local c = seatedContainer()
        print("  that cab's locker: " .. (c and "found -- it is offered" or
              "NOT FOUND (not built yet, or its square is not loaded)"))
        print("  derailed: " .. tostring(rec.derailed == true) .. " (no difference by design)")
    end
end

RR = RR or {}
RR.LockerAccess = Access
print("[Railroader] RR_LockerAccess.lua: loaded OK.")

return Access
