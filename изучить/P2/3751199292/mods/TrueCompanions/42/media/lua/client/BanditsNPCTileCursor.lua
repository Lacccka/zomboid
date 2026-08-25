--
-- Bandits NPC - Companions Overhaul - Tile selection cursor
--
-- Picks 1 or 2 ground tiles (Stay / Guard orders, routine spots) using the
-- game's drag-cursor system. Draws a green ghost-tile highlight under the mouse
-- and calls onComplete({picks}) once enough tiles are left-clicked, then exits.
--
-- Optional opts (4th arg to Begin):
--   validate  = function(square) -> bool   -- invalid tiles show RED and refuse the click
--   direction = true                       -- facing picker: the rotate key ("Rotate
--                                          -- building", default R) cycles S/W/N/E and a
--                                          -- BLUE tile shows which way she will face;
--                                          -- each pick carries .dir ("N"/"S"/"E"/"W")
--
-- NOTE: the cursor derives from ISBuildingObject, which is a gameplay class not
-- yet loaded at the main menu. So we build the class LAZILY on first use (in
-- game), not at file load -- otherwise indexing nil ISBuildingObject errors.
--

BanditsNPCTileCursor = BanditsNPCTileCursor or {}

local CursorClass = nil

local NEXT_DIR = { S = "W", W = "N", N = "E", E = "S" }
local DIR_VEC  = { S = {0, 1}, N = {0, -1}, E = {1, 0}, W = {-1, 0} }

local function buildClass()
    if CursorClass then return CursorClass end
    if not ISBuildingObject then return nil end

    local C = ISBuildingObject:derive("BanditsNPCTileCursorObj")

    function C:new(playerNum, count, onComplete, opts)
        local o = ISBuildingObject:new()
        setmetatable(o, self)
        self.__index = self
        o.player = playerNum
        o.count = count or 1
        o.onComplete = onComplete
        o.picks = {}
        opts = opts or {}
        o.validate = opts.validate
        o.direction = opts.direction and true or false
        o.dir = "S"
        o.sprite = "carpentry_02_56"
        o.sprites = { "carpentry_02_56" }
        o.nSprite = 1
        o.north = false
        o.canBeAlwaysPlaced = true
        o.skipBuildAction = true
        o.noNeedHammer = true
        o.dragNilAfterPlace = false
        return o
    end

    -- engine sets canBeBuild from this; with a validator, bad tiles go red
    function C:isValid(square)
        if not square then return false end
        if self.validate then
            local ok, res = pcall(self.validate, square)
            return ok and res == true
        end
        return true
    end

    function C:render(x, y, z, square)
        if not square then return end
        if not self.hlSprite then
            self.hlSprite = IsoSprite.new()
            self.hlSprite:LoadSingleTexture("carpentry_02_56")
        end
        -- confirmed picks: steady green
        for _, p in ipairs(self.picks) do
            self.hlSprite:RenderGhostTileColor(p.x, p.y, p.z, 0, 0, 0.2, 0.85, 0.2, 0.55)
        end
        -- hovered tile: a pulsing placement-style highlight (green = ok, red = invalid)
        local phase = (getTimestampMs() % 900) / 900
        local pulse = 0.35 + 0.22 * math.sin(phase * math.pi * 2)
        local valid = self:isValid(square)
        if valid then
            self.hlSprite:RenderGhostTileColor(x, y, z, 0, 0, 0.4, 1.0, 0.5, pulse)
        else
            self.hlSprite:RenderGhostTileColor(x, y, z, 0, 0, 1.0, 0.35, 0.3, pulse)
        end
        -- facing indicator: the blue tile is the way she will face (rotate key cycles it)
        if self.direction and valid then
            local v = DIR_VEC[self.dir] or DIR_VEC.S
            self.hlSprite:RenderGhostTileColor(x + v[1], y + v[2], z, 0, 0, 0.35, 0.6, 1.0, pulse)
        end
    end

    -- called by the drag system on left-click; capture the tile instead of building
    function C:tryBuild(x, y, z)
        local sq = getCell():getGridSquare(x, y, z)
        if not self:isValid(sq) then return end   -- refuse invalid tiles (red highlight)
        local pick = { x = x, y = y, z = z }
        if self.direction then pick.dir = self.dir end
        table.insert(self.picks, pick)
        if #self.picks >= self.count then
            local picks = self.picks
            local cb = self.onComplete
            getCell():setDrag(nil, self.player)   -- exit cursor mode
            if BanditsNPCTileCursor.active == self then BanditsNPCTileCursor.active = nil end
            if cb then cb(picks) end
        end
    end

    CursorClass = C
    return C
end

-- rotate key while a direction-mode cursor is active. Vanilla does NOT route the
-- rotate key to ISBuildingObject drag objects (only the furniture-move cursor gets
-- it), so we listen ourselves: honor the user's "Rotate building" binding, fall
-- back to R. The drag identity check makes a stale .active (ESC-cancelled cursor)
-- self-clear instead of eating keys.
local function onRotateKey(key)
    if ISChat and ISChat.focused then return end   -- not while typing (audit Cluster D)
    local active = BanditsNPCTileCursor.active
    if not (active and active.direction) then return end
    local ok, drag = pcall(function() return getCell():getDrag(active.player) end)
    if not ok or drag ~= active then BanditsNPCTileCursor.active = nil; return end
    local rk
    pcall(function() rk = getCore():getKey("Rotate building") end)
    if not rk or rk == 0 then rk = Keyboard.KEY_R end
    if key == rk then
        active.dir = NEXT_DIR[active.dir] or "S"
    end
end
Events.OnKeyPressed.Add(onRotateKey)

function BanditsNPCTileCursor.Begin(playerNum, count, onComplete, opts)
    local C = buildClass()
    if not C then
        print("[BanditsNPC] Tile cursor unavailable (ISBuildingObject not loaded yet).")
        return nil
    end
    local cursor = C:new(playerNum, count, onComplete, opts)
    getCell():setDrag(cursor, playerNum)
    BanditsNPCTileCursor.active = cursor
    return cursor
end
