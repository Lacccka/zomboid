--
-- True Companions - Zone drag cursor (v0.64)
--
-- Drag out an area on the ground. Real bases are not rectangles, so you drag as
-- many PIECES as you need -- one per wing of the house, one for the yard -- and
-- click Finish when the shape is covered.
--
-- IT SUBCLASSES THE VANILLA BUILD CURSOR, which is why there are no mouse handlers.
-- ISBuildingObject owns mouse capture, ESC and joypad, and gives a subclass
-- self.isLeftDown plus a per-frame render(). While the button is held the engine
-- passes the square the drag STARTED on (ISBuildingObject.lua:128-133 swaps in
-- draggingItem.square), so render() doubles as mouse-move and create() is mouse-up.
--
-- THE PLAYER MUST NOT MOVE. ISBuildingObject:tryBuild ALWAYS calls self:walkTo()
-- (line 208) and the flag that suppresses it is `skipWalk2`, NOT `skipWalk` --
-- `skipWalk` is honoured nowhere in the engine. Setting the wrong one is why the
-- first build walked the player to the corner of the drag with a timed-action
-- progress bar over their head. Both the correct flag and an explicit walkTo
-- override are set below, because this must never regress: you are drawing on a
-- map, not building something.
--

BanditsNPC = BanditsNPC or {}

local function defineClass()
    if BanditsNPCZoneCursor then return end
    if not ISBuildingObject then return end

    BanditsNPCZoneCursor = ISBuildingObject:derive("BanditsNPCZoneCursor")

    function BanditsNPCZoneCursor:new(character, site, kind, onDone)
        local o = ISBuildingObject.new(self)
        o.character = character
        o.player = character:getPlayerNum()
        o.playerNum = o.player
        o.skipBuildAction = true   -- no timed action, straight to create()
        o.noNeedHammer = true
        o.skipWalk2 = true         -- THE flag the engine actually checks
        o.dragNilAfterPlace = false -- stay armed: more pieces may follow
        o.site = site
        o.kind = kind
        o.onDone = onDone
        o.parts = {}
        return o
    end

    -- Belt and braces with skipWalk2. If a future build renames the flag again,
    -- this still guarantees the player stays put.
    function BanditsNPCZoneCursor:walkTo(x, y, z)
        return true
    end

    function BanditsNPCZoneCursor:isValid(square)
        return square ~= nil
    end

    function BanditsNPCZoneCursor:pickSquare(screenX, screenY)
        local z = self.character:getCurrentSquare():getZ()
        local wx = math.floor(screenToIsoX(self.playerNum, screenX, screenY, z))
        local wy = math.floor(screenToIsoY(self.playerNum, screenX, screenY, z))
        return getCell():getGridSquare(wx, wy, z), wx, wy, z
    end

    function BanditsNPCZoneCursor:getRectangle(x, y)
        local x1, y1 = x, y
        local _, x2, y2 = self:pickSquare(getMouseX(), getMouseY())
        if x1 > x2 then x1, x2 = x2, x1 end
        if y1 > y2 then y1, y2 = y2, y1 end
        return x1, y1, x2, y2
    end

    -- Only the WHOLE area has a minimum size; individual pieces may be as small as
    -- one tile so a doorway or an alcove can be patched in.
    local function pieceTooBig(x1, y1, x2, y2)
        local Zc = BanditsNPC.Zones
        return (x2 - x1 + 1) > Zc.MAX_SIDE or (y2 - y1 + 1) > Zc.MAX_SIDE
    end

    -- ONE FLAT FILL, EDGE INCLUDED. No brighter perimeter pass: v0.66 drew the
    -- outline at 0.55 over an interior at 0.30 and it read as a frame around a
    -- washed-out middle rather than as a marked-out area. Pieces are merged into
    -- non-overlapping strips first, so overlapping drags do not double their alpha
    -- and show a glowing bar along the join.
    local FILL = 0.34
    local BAD  = { r = 0.90, g = 0.20, b = 0.20 }

    function BanditsNPCZoneCursor:render(x, y, z, square)
        local Zc = BanditsNPC.Zones
        local col = Zc.COLOR[self.kind] or Zc.COLOR.base

        Zc.ShowFor(500)   -- keep committed areas visible while placing

        -- Everything accepted so far PLUS the piece under the mouse, merged as one
        -- shape, so the live drag joins seamlessly onto what is already down instead
        -- of showing as a separate brighter patch over it.
        local live = {}
        for i = 1, #self.parts do live[i] = self.parts[i] end

        local bad = false
        if self.isLeftDown then
            local x1, y1, x2, y2 = self:getRectangle(x, y)
            bad = pieceTooBig(x1, y1, x2, y2)
            live[#live + 1] = { x1 = x1, y1 = y1, x2 = x2, y2 = y2 }
        elseif #live == 0 then
            live[1] = { x1 = x, y1 = y, x2 = x, y2 = y }   -- hover pip before the first drag
        end

        -- CACHED (v0.75.39). Z.Merge sorts and is O(n^2) in the number of pieces, and this
        -- ran it EVERY FRAME while the cursor is armed -- re-merging every already-placed
        -- rectangle to redraw a picture that only changes when the piece under the mouse
        -- does. The committed pieces cannot change mid-frame (they are only appended on
        -- mouse-up), so the result is keyed on the piece count plus the live rectangle:
        -- during a drag that still re-merges as the mouse moves, which is the case that
        -- genuinely needs it, and while hovering between drags it is free.
        local lastRect = live[#live]
        local key = tostring(#self.parts) .. "|" .. tostring(self.isLeftDown) .. "|"
            .. (lastRect and (tostring(lastRect.x1) .. "," .. tostring(lastRect.y1) .. ","
                              .. tostring(lastRect.x2) .. "," .. tostring(lastRect.y2)) or "-")
        local merged
        if self._mergeKey == key and self._merged then
            merged = self._merged
        else
            merged = Zc.Merge(live)
            self._mergeKey, self._merged = key, merged
        end
        for i = 1, #merged do
            local p = merged[i]
            Zc.DrawRect(self.playerNum, p.x1, p.y1, p.x2, p.y2, z, bad and BAD or col, FILL)
        end

        -- The hover pip still shows once pieces exist, so you can see where the next
        -- drag will start without it being lost inside the fill.
        if not self.isLeftDown and #self.parts > 0 then
            Zc.DrawRect(self.playerNum, x, y, x, y, z, col, 0.55)
        end
    end

    -- Mouse-up: accept the piece and STAY ARMED. Finishing is an explicit click on
    -- the panel, so letting go of the button never ends the job by surprise.
    function BanditsNPCZoneCursor:create(x, y, z, north, sprite)
        local x1, y1, x2, y2 = self:getRectangle(x, y)
        if pieceTooBig(x1, y1, x2, y2) then
            if self.character then
                self.character:Say(BanditsNPC.TF("UI_BN_Zone_TooBig",
                    "Too big - at most %1 by %1 tiles.", BanditsNPC.Zones.MAX_SIDE))
            end
            return
        end
        self.parts[#self.parts + 1] = { x1 = x1, y1 = y1, x2 = x2, y2 = y2, z = z }
        self.planeZ = z
        if BanditsNPCZonePanel and BanditsNPCZonePanel.instance then
            BanditsNPCZonePanel.instance:refresh()
        end
    end

    function BanditsNPCZoneCursor:undoPiece()
        if #self.parts > 0 then table.remove(self.parts) end
    end

    -- Commits every piece as ONE area.
    function BanditsNPCZoneCursor:finish()
        local Zc = BanditsNPC.Zones
        local player = self.character

        if #self.parts == 0 then
            if player then
                player:Say(BanditsNPC.T("UI_BN_Zone_Nothing", "Nothing marked out yet."))
            end
            return false
        end

        local total = 0
        for i = 1, #self.parts do
            local p = self.parts[i]
            total = total + (p.x2 - p.x1 + 1) * (p.y2 - p.y1 + 1)
        end
        if total < (Zc.MIN_SIDE * Zc.MIN_SIDE) then
            if player then
                player:Say(BanditsNPC.TF("UI_BN_Zone_TooSmall",
                    "Too small - mark out at least %1 tiles.", Zc.MIN_SIDE * Zc.MIN_SIDE))
            end
            return false
        end

        -- Z.Set REFUSES past the rectangle cap and returns nil (v0.77.14). Since drawing
        -- now ADDS to the base rather than replacing it, that refusal is reachable by
        -- ordinary use -- and saying "base area set." after discarding the whole drawing
        -- is the kind of silent no-op this mod has shipped before. Say what happened and
        -- keep the pieces on the cursor so nothing the player dragged is lost.
        if not Zc.Set(self.site, self.kind, self.parts, self.planeZ or 0) then
            if player then
                player:Say(BanditsNPC.T("UI_BN_Zone_TooMany",
                    "That is as many pieces as one base area can hold - clear it and draw a simpler shape."))
            end
            return false
        end
        Zc.ShowFor(6000)
        if player then
            player:Say(BanditsNPC.TF("UI_BN_Zone_Set", "%1 set.", Zc.KindName(self.kind)))
        end

        local done = self.onDone
        local site, kind = self.site, self.kind
        BanditsNPCZoneCursor.Cancel(self.player)
        if done then pcall(done, site, kind) end
        return true
    end

    function BanditsNPCZoneCursor:reset()
        ISBuildingObject.reset(self)
        ISWorldObjectContextMenu.disableWorldMenu = false
    end

    function BanditsNPCZoneCursor:deactivate()
        ISWorldObjectContextMenu.disableWorldMenu = false
    end

    function BanditsNPCZoneCursor.Begin(player, site, kind, onDone)
        if not (player and site and kind) then return end
        BanditsNPCZoneCursor.Cancel(player:getPlayerNum())
        local cursor = BanditsNPCZoneCursor:new(player, site, kind, onDone)
        getCell():setDrag(cursor, player:getPlayerNum())
        ISWorldObjectContextMenu.disableWorldMenu = true
        BanditsNPC.Zones.ShowFor(600000)   -- stays lit for the whole session of drawing
        BanditsNPCZonePanel.OpenFor(cursor)
    end

    -- Type-guarded so we never clear another mod's cursor out from under it.
    function BanditsNPCZoneCursor.Cancel(playerNum)
        playerNum = playerNum or 0
        local drag = getCell():getDrag(playerNum)
        if drag and drag.Type == "BanditsNPCZoneCursor" then
            getCell():setDrag(nil, playerNum)
        end
        ISWorldObjectContextMenu.disableWorldMenu = false
        BanditsNPC.Zones.ShowFor(4000)
        if BanditsNPCZonePanel and BanditsNPCZonePanel.instance then
            BanditsNPCZonePanel.instance:close()
        end
    end
end

Events.OnGameStart.Add(defineClass)
