--
-- True Companions - "press E" prompt over a nearby Survivors Beacon
--
-- Right-clicking a world object is not discoverable. A player who has just built the
-- beacon has no reason to know it does anything at all, and the whole companion system
-- is behind it. A floating prompt when you walk up to it is how every other
-- interactable thing in the game announces itself.
--
-- WORLD-TO-SCREEN is isoToScreenX/Y(playerIndex, x, y, z) -- the globals vanilla's own
-- Fishing tension bar and foraging icons use, so the projection matches the camera at
-- any zoom. The element is SMALL and follows the station; it must never be full-screen
-- (see the note on prerender).
--
-- THE KEY IS READ, NOT ASSUMED. getCore():getKey("Interact") is whatever the player has
-- bound, so the prompt and the handler always agree even if that is not E; the label
-- asks the engine for the key's name rather than hard-coding one.
--

require "ISUI/ISUIElement"

BanditsNPCBeaconPrompt = ISUIElement:derive("BanditsNPCBeaconPrompt")

local RANGE = 2.2      -- tiles; roughly "standing at it", not "in the same room"

-- Every station this prompt speaks for. Adding one is a row here, not a new file --
-- the scan, the projection, the key handling and the suppression rules are identical
-- for all of them and there is no reason to have two copies of any of it.
--
-- `find` takes a square and returns the object; `label` and `open` take that object.
local STATIONS = {
    {
        find  = function(sq) return BanditsNPC.Beacon and BanditsNPC.Beacon.OnSquare(sq) end,
        label = function() return BanditsNPC.T("UI_BN_Beacon_Menu", "Survivors Beacon") end,
        open  = function(o) BanditsNPCBeaconWindow.OpenFor(o) end,
    },
    {
        find  = function(sq) return BanditsNPC.Appearance and BanditsNPC.Appearance.OnSquare(sq) end,
        label = function() return BanditsNPC.T("UI_BN_App_Title", "Appearance Workstation") end,
        open  = function(o) BanditsNPCAppearanceWindow.OpenFor(o) end,
    },
}

-- The station the player is close enough to use, or nil. Scans the squares around the
-- player rather than any registry, because this is about what is in FRONT of you --
-- a beacon two hundred tiles away is in the registry too.
local function stationNearPlayer(player)
    if not player then return nil, nil end
    local foundObj, foundDef
    pcall(function()
        local px, py, pz = player:getX(), player:getY(), player:getZ()
        local cell = getCell()
        local bestD
        for dx = -2, 2 do
            for dy = -2, 2 do
                local sq = cell:getGridSquare(math.floor(px) + dx, math.floor(py) + dy, pz)
                if sq then
                    for _, def in ipairs(STATIONS) do
                        local o = def.find(sq)
                        if o then
                            local d = BanditUtils.DistTo(px, py, sq:getX() + 0.5, sq:getY() + 0.5)
                            -- Nearest wins, so two stations side by side prompt for the
                            -- one you are actually standing at.
                            if d <= RANGE and (bestD == nil or d < bestD) then
                                foundObj, foundDef, bestD = o, def, d
                            end
                        end
                    end
                end
            end
        end
    end)
    return foundObj, foundDef
end

-- THE ELEMENT MUST NOT COVER THE SCREEN.
--
-- v0.72 made this a full-screen ISUIElement and drew the prompt at projected
-- coordinates inside it. That broke RIGHT-CLICKING ANYWHERE IN THE GAME: the Java UI
-- manager picks the topmost element under the cursor before the world ever sees the
-- click, and a full-screen element is under the cursor by definition. Setting
-- wantMouseEvents = false was not enough -- there was no error in the log, the clicks
-- simply never reached ISObjectClickHandler.
--
-- So the element is kept SMALL and MOVED to the prompt's position each frame, which is
-- the pattern vanilla's own world-space icons use (ISBaseIcon:renderAltWorldTexture
-- sets x/y from isoToScreenX/Y). It is also hidden outright when there is nothing to
-- prompt for, so most of the time it is not in the picking list at all.
--
-- AND THAT HIDING IS WHY THE SEARCH CANNOT LIVE IN prerender().
--
-- v0.74.2-v0.74.4 ran the station scan in prerender() and hid the element from inside
-- it when nothing was near. The prompt then never appeared again for the rest of the
-- session -- and since a new game starts with the player nowhere near a beacon, the
-- very first frame killed it. It looked exactly like the feature had been removed.
--
-- zombie.ui.UIElement.render() is the ONLY caller of prerender, and it returns before
-- reaching it when the element is invisible (javap -c, offsets 8-18 test isVisible and
-- return; the prerender tableget is at offset 136). A self-hiding element is therefore
-- a ONE-WAY DOOR: prerender turns it off and nothing is left running to turn it back on.
--
-- So visibility is owned by a GAME event instead, which fires whether or not the element
-- is drawn. prerender only keeps the projection current while the prompt is already up,
-- which is what it is genuinely good for -- it runs in lockstep with the frame, so the
-- prompt does not lag the camera.
-- +0.8 on z lifts the prompt clear of the station without floating above it. It was
-- +1.6, which is two thirds of a storey: the label sat well over the object's head and
-- read as belonging to nothing (author, 4 Aug). One tile of z is roughly the height of
-- the object itself, so a bit under that puts the label at the top of it.
local Z_LIFT = 0.8

local function place(self, sq, w, h)
    local sx = isoToScreenX(0, sq:getX() + 0.5, sq:getY() + 0.5, sq:getZ() + Z_LIFT)
    local sy = isoToScreenY(0, sq:getX() + 0.5, sq:getY() + 0.5, sq:getZ() + Z_LIFT)
    self:setX(math.floor(sx - w / 2))
    self:setY(math.floor(sy - h))
    self:setWidth(w)
    self:setHeight(h)
end

function BanditsNPCBeaconPrompt:prerender()
    if not (self.station and self.label) then return end
    pcall(function()
        local sq = self.station:getSquare()
        if sq then place(self, sq, self.width, self.height) end
    end)
end

function BanditsNPCBeaconPrompt:render()
    if not self.station or not self.label then return end
    local w, h = self.width, self.height
    self:drawRect(0, 0, w, h, 0.62, 0.05, 0.06, 0.08)
    self:drawRectBorder(0, 0, w, h, 0.7, 0.45, 0.55, 0.45)
    self:drawText(self.label, 6, 3, 0.85, 1.0, 0.88, 1, UIFont.Small)
end

function BanditsNPCBeaconPrompt:new()
    -- Small, and NOT anchored to the screen edges. refresh() sizes it to the label and
    -- prerender() keeps it over the station each frame; see the note above prerender for
    -- why a full-screen element is not an option and why the two are split.
    local o = ISUIElement.new(self, 0, 0, 10, 10)
    o.wantMouseEvents = false
    return o
end

local instance

-- Owns the element's visibility. Runs off OnPlayerUpdate, NOT prerender -- see the long
-- note above prerender for why that distinction is the whole bug.
--
-- Throttled to 150ms: it is a 25-square scan and nobody walks up to a beacon faster than
-- that. Position is NOT throttled; prerender re-projects every frame while visible.
local lastScanMs = 0
local function refresh()
    if not instance then return end
    pcall(function()
        local now = getTimestampMs()
        if now - lastScanMs < 150 then return end
        lastScanMs = now

        -- Player 0 specifically, not whichever player OnPlayerUpdate handed us: the
        -- projection below is isoToScreenX(0, ...), so a second split-screen player
        -- would place the prompt on the wrong half of the screen.
        local player = getSpecificPlayer(0)
        local obj, def
        if player and not player:isDead() then obj, def = stationNearPlayer(player) end

        -- Suppressed while any of our own windows are up: the prompt would sit on top of
        -- the very UI it is advertising.
        if (BanditsNPCBeaconWindow and BanditsNPCBeaconWindow.instance)
           or (BanditsNPCAppearanceWindow and BanditsNPCAppearanceWindow.instance)
           or (BanditsNPCZonePanel and BanditsNPCZonePanel.instance) then
            obj, def = nil, nil
        end

        local sq
        if obj then pcall(function() sq = obj:getSquare() end) end
        if not sq then
            instance.station, instance.stationDef, instance.label = nil, nil, nil
            if instance:getIsVisible() then instance:setVisible(false) end
            return
        end

        instance.station, instance.stationDef = obj, def
        instance.label = BanditsNPC.TF("UI_BN_Beacon_Prompt", "[%1] %2",
                                       BanditsNPC.Beacon.PromptKeyName(), def.label())
        local w = getTextManager():MeasureStringX(UIFont.Small, instance.label) + 12
        local h = getTextManager():getFontHeight(UIFont.Small) + 6
        place(instance, sq, w, h)
        if not instance:getIsVisible() then instance:setVisible(true) end
    end)
end

local function openNearbyStation()
    if not (instance and instance.station and instance.stationDef) then return false end
    local ok = false
    pcall(function()
        instance.stationDef.open(instance.station)
        ok = true
    end)
    return ok
end

local function onKeyPressed(key)
    pcall(function()
        if key ~= BanditsNPC.Beacon.PromptKey() then return end
        -- Never while typing in chat, or the key would open the window mid-sentence.
        --
        -- `ISChat.focused` ALONE (v0.75.7, audit Cluster D). This used to bury the real
        -- test inside four preconditions, one of which was ISChat.instance.showTitle --
        -- not a state flag at all but the "show chat titles" DISPLAY PREFERENCE
        -- (ISChat.lua:42 sets it from getCore():isOptionShowChatTitle(), :666 toggles it
        -- from the settings menu). A player who turned that option off lost the guard
        -- completely. ISChat.focused is a plain module-level boolean (ISChat.lua:8, set
        -- at :615, cleared at :630) that vanilla reads unconditionally at :408, :556,
        -- :627, :1016 and :1030 -- no preconditions needed.
        if ISChat and ISChat.focused then return end
        openNearbyStation()
    end)
end

local function start()
    if instance then return end
    instance = BanditsNPCBeaconPrompt:new()
    instance:initialise()
    instance:addToUIManager()
    -- Starts hidden and STAYS hidden until refresh() finds a station. Safe now only
    -- because refresh() is the thing that turns it back on; when prerender owned
    -- visibility this line would have been the end of the feature.
    instance:setVisible(false)
end

Events.OnGameStart.Add(start)
if Events.OnPlayerUpdate then Events.OnPlayerUpdate.Add(refresh) end
if Events.OnKeyPressed then Events.OnKeyPressed.Add(onKeyPressed) end
