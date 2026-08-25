-- ============================================================
-- PagerMod_Shared.lua
-- Shared namespace, constants, config and helpers.
-- Loaded on both client and server.
-- ============================================================

PagerMod = PagerMod or {}

PagerMod.VERSION   = "1.0.0"
PagerMod.DEBUG     = false             -- set true for verbose console.txt diagnostics
PagerMod.MODULE    = "PagerMod"        -- network command module string
PagerMod.MODDATA   = "PagerMod"        -- global ModData key (server side routing/state)
-- We use the vanilla Base.Pager item (B42 ships it and spawns it in loot; the
-- B41 build defines it). All pager logic attaches via the item's ModData.
PagerMod.ITEM      = "Base.Pager"        -- full item type
PagerMod.TOWER     = "PagerMod.PagerTower" -- craftable relay tower item (Ultra tier)

-- ── Signal modes ───────────────────────────────────────────
PagerMod.SignalMode = {
    GLOBAL      = 1, -- works map-wide (default; pagers are the long-range tier)
    RANGE       = 2, -- only delivered if recipient is within SignalRange tiles of sender
    -- Powered-tower modes. The dropdown order is GLOBAL, RANGE, TOWER_AFTER, TOWER.
    TOWER_AFTER = 3, -- powered tower only, AFTER the power dies: map-wide while the
                     -- electricity grid is up; once power's out, the network only
                     -- works through a powered tower's coverage (the realistic tier).
    TOWER       = 4, -- powered tower only, REGARDLESS of power: the network always
                     -- needs a built, powered tower covering both ends.
}

-- True if `mode` (defaults to the live config) is one of the powered-tower modes.
function PagerMod.isTowerMode(mode)
    if mode == nil then mode = PagerMod.Config and PagerMod.Config.signalMode end
    return mode == PagerMod.SignalMode.TOWER or mode == PagerMod.SignalMode.TOWER_AFTER
end

-- ── Pager mode (send & receive vs receive-only) ────────────
PagerMod.PagerMode = {
    BOTH         = 1, -- pagers can send and receive (default)
    RECEIVE_ONLY = 2, -- pagers can only receive; the compose UI is hidden
}

-- ── Difficulty presets ─────────────────────────────────────
PagerMod.Preset = {
    DEFAULT    = 1, -- Custom: "leave it as is" — individual sandbox options apply untouched
    CONVENIENT = 2, -- Convenient: the relaxed defaults (map-wide, full two-way, everything on)
    ALMOST     = 3, -- Almost Realistic
    ULTRA      = 4, -- Ultra Realistic (receive-only + powered-tower-after-power-dies network)
}

-- Tower: tile distance from a fuelled generator that counts as "powered",
-- and how often the server re-checks tower power.
PagerMod.TOWER_GEN_RANGE = 20

-- The deployed tower renders a CUSTOM sprite rendered from the 3D pager-tower
-- model (tools/render_tower_sprite.py). Following the TrampleSteam pattern (the
-- ONLY reliable way to show a mod PNG on a world object): the object's MAIN
-- sprite is an invisible 1px base (given a solidtrans flag at boot, so the tower
-- is solid), and the visible art rides on top as an ATTACHED anim (a runtime
-- sprite does NOT render via the main-sprite path -> setSprite was invisible).
-- Two facings are rendered: index 0 (up/down) and 1 (left/right). All sprites
-- are registered by name at boot (shared/PagerMod_TowerSprite.lua). The attached
-- anim doesn't persist, so it's re-applied on OnObjectAdded / LoadGridsquare.
PagerMod.TOWER_OBJECT_NAME = "PagerModTower"            -- IsoObject name (identifier)
PagerMod.TOWER_BASE_SPRITE = "PagerMod_TowerBase"       -- invisible 1px solid main sprite
PagerMod.TOWER_BASE_TEX    = "media/textures/pagermod_blank.png"
-- Directional art: { spriteName, texturePath } per facing index (0=up/down, 1=left/right).
PagerMod.TOWER_SPRITES = {
    [0] = { name = "PagerMod_TowerSprite",   tex = "media/textures/PagerTower.png" },
    [1] = { name = "PagerMod_TowerSprite_H", tex = "media/textures/PagerTower_h.png" },
}
PagerMod.TOWER_SPRITE = PagerMod.TOWER_SPRITES[0].name  -- default/back-compat
-- Tile anchoring (printed by the renderer). Pixel offsets from the tile's north
-- corner; positive = left/up. DEPTH_SOFF biases draw depth so the tall sprite
-- sorts above its floor but behind things in front. VISUAL TUNING values.
PagerMod.TOWER_SPRITE_OFFSET_X = 38
PagerMod.TOWER_SPRITE_OFFSET_Y = 101
PagerMod.TOWER_DEPTH_SOFF      = -56

-- Tower power source (sandbox PagerMod.TowerPower).
PagerMod.TowerPower = {
    GRID_AND_GEN = 1, -- grid electricity OR a running generator nearby (default)
    GEN_ONLY     = 2, -- only a running generator nearby (ignores grid)
    NONE         = 3, -- towers need no power at all
}

-- Each deployed tower is its own network node with its own number, coverage
-- circle, inbox and console. Numbers are assigned low-first from this space and
-- rendered by formatNumber() as 000-0001, 000-0002, ... (reused after pickup).
PagerMod.TOWER_NUMBER_MAX = 999
function PagerMod.towerNumberFor(index)
    return string.format("%07d", index)  -- 1 -> "0000001"
end
-- True if a 7-digit number string is in the tower-number space (000-0001..0999).
function PagerMod.isTowerNumber(num)
    local n = tonumber(num)
    return n ~= nil and n >= 1 and n <= PagerMod.TOWER_NUMBER_MAX
end

-- ── Pager tower comms mode (what the tower nodes themselves can do) ──
PagerMod.TowerComms = {
    SEND_RECEIVE = 1, -- towers can send pages AND be paged (their consoles have an inbox)
    SEND_ONLY    = 2, -- towers can only send; paging a tower number is rejected
}

-- ── Location sharing modes ─────────────────────────────────
PagerMod.LocationMode = {
    OFF    = 1, -- coordinates are never attached to a page
    OPT_IN = 2, -- attached only when the sender ticks "attach location"
    ALWAYS = 3, -- every page carries the sender's coordinates
}

-- ── Faction scoping (PvPvE) ────────────────────────────────
PagerMod.FactionScope = {
    OFF       = 1, -- anyone can page anyone
    SAME_ONLY = 2, -- members of different factions cannot page each other
}

-- ── Pager spawn abundance (how much pager loot the world gets) ──
-- 2 (Vanilla) is the default and means "exactly what the build already does":
-- B42 leaves its vanilla pager loot untouched, B41 keeps having none of its own.
-- See server/PagerMod_Distribution.lua for how each step is applied.
PagerMod.SpawnAbundance = {
    RARE       = 1, -- half as common as vanilla
    VANILLA    = 2, -- unchanged (default)
    MORE       = 3, -- twice as common
    COMMON     = 4, -- four times as common
    EVERYWHERE = 5, -- eight times as common
}
local ABUNDANCE_MULT = { 0.5, 1, 2, 4, 8 }

-- Loot-weight multiplier for a SpawnAbundance value (1 for anything unknown,
-- so a bad value can never wipe out or explode the loot tables).
function PagerMod.spawnAbundanceMult(mode)
    return ABUNDANCE_MULT[mode or PagerMod.SpawnAbundance.VANILLA] or 1
end

-- ── Battery life presets (in-game hours; 0 = never dies) ────
PagerMod.BatteryLife = { MONTH1 = 1, MONTH3 = 2, MONTH6 = 3, YEAR1 = 4, NEVER = 5 }
local BATTERY_HOURS = { [1] = 720, [2] = 2160, [3] = 4320, [4] = 8640, [5] = 0 }

-- ── Network commands ───────────────────────────────────────
-- Client -> Server
PagerMod.C2S = {
    REGISTER     = "Register",     -- announce which pager numbers I currently hold
    ASSIGN       = "AssignNumber", -- request a fresh unique number for a pager item
    SEND         = "SendMessage",  -- send a message to a number
    BROADCAST    = "Broadcast",    -- send to every known pager
    CHANNEL      = "Channel",      -- send to a list of numbers (a group/channel)
    SOS          = "SOS",          -- emergency broadcast (forces location when allowed)
    FETCH        = "FetchInbox",   -- pull queued messages for my numbers
    REQUEUE      = "Requeue",      -- bounce back a message we can't store (no longer holding)
    MARK_READ    = "MarkRead",     -- report read pages (drives read receipts)
    SHARE_NEARBY = "ShareNearby",  -- broadcast my number to nearby pagers
    GLOBAL       = "GlobalChat",   -- post to the town-wide global pager channel
    DEPLOY_TOWER = "DeployTower",  -- register a pager tower at a position (server assigns its number)
    PICKUP_TOWER = "PickupTower",  -- remove the tower at a position (owner/admin only)
    OPEN_TOWER   = "OpenTower",    -- operate the tower at a position (server resolves its number + inbox)
    RENAME_TOWER = "RenameTower",  -- rename the tower at a position (owner/admin); name syncs to all contacts
    RENAME       = "RenamePager",  -- rename my own pager; server persists it and syncs the name to everyone's saved contact
    REPLACE_BATT = "ReplaceBattery",-- fit a fresh battery; server resets batteryAge on the authoritative item copy
    PING         = "Ping",         -- request network/signal status
}
-- Server -> Client
PagerMod.S2C = {
    ASSIGNED     = "NumberAssigned",
    DELIVER      = "Deliver",      -- one or more messages delivered to a held number
    INBOX        = "Inbox",        -- response to FETCH
    STATUS       = "NetStatus",    -- response to PING
    INFO         = "Info",         -- generic toast/halo text
    SENT_OK      = "SentOk",       -- confirmation a message was routed
    SENT_FAIL    = "SentFail",     -- routing failed (no signal / unknown number)
    ADD_CONTACT  = "AddContact",   -- a nearby player (or a tower) shared a number
    TOWER_OPEN   = "TowerOpen",    -- open a tower's console: { number, name, powered, inbox }
    TOWER_REFUND = "TowerRefund",  -- a deploy was rejected: client removes the object it placed and gets the item back
    PICKUP_OK    = "PickupOk",      -- pickup authorised: client removes the world object at { x, y, z }
}

-- Distance (tiles) within which "Share my number nearby" reaches other pagers.
PagerMod.SHARE_RANGE = 40

-- Canned quick-page phrases offered in the radial/context menu.
-- Each entry is a translation key; English fallbacks live in the Translate files.
PagerMod.QuickPhrases = {
    "IGUI_PagerMod_Quick_OnMyWay",
    "IGUI_PagerMod_Quick_NeedBackup",
    "IGUI_PagerMod_Quick_MeetUp",
    "IGUI_PagerMod_Quick_AllClear",
    "IGUI_PagerMod_Quick_StayPut",
    "IGUI_PagerMod_Quick_Incoming",
}

-- ── Default config (overridden by sandbox at runtime) ───────
PagerMod.Config = {
    preset           = PagerMod.Preset.DEFAULT,
    signalMode       = PagerMod.SignalMode.GLOBAL,
    signalRange      = 7000,   -- tiles; shared by Limited (RANGE) range and Tower coverage radius
    pagerMode        = PagerMod.PagerMode.BOTH,
    allowBroadcast   = true,
    maxMessages      = 50,     -- max stored messages per pager
    messageMaxLength = 140,
    notifySound      = true,
    beepVolume       = 2,      -- 1..4 = 25/50/75/100% of the beep clip's full loudness
    -- Expanded options
    locationSharing  = PagerMod.LocationMode.OPT_IN,
    sendCooldown     = 0,      -- seconds between pages per player (0 = no limit)
    readReceipts     = true,
    factionScope     = PagerMod.FactionScope.OFF,
    allowSOS         = true,
    allowBlocking    = true,
    batteryLife      = PagerMod.BatteryLife.MONTH3,
    -- Tower (Ultra tier) options
    towerComms       = PagerMod.TowerComms.SEND_RECEIVE,
    towerLimit       = 0,      -- max towers one player may have set up at once (0 = unlimited)
    towerPower       = PagerMod.TowerPower.GRID_AND_GEN,  -- what powers a tower
    globalChat       = false,  -- a town-wide channel every pager is auto-joined to
    numericOnly      = false,  -- messages may contain digits only (no letters)
    spawnAbundance   = PagerMod.SpawnAbundance.VANILLA, -- how much pager loot the world gets
    logPages         = false,  -- append every routed page to a server-side log file
}

-- Preset bundles (the Preset sandbox option). Index 1 (Default) seeds nothing
-- and leaves the controls untouched. Choosing one of the bundles below seeds the
-- listed sandbox controls in the options UI (PagerMod_SandboxUI.lua); any key a
-- bundle omits is reset to its sandbox default first, so each preset is a clean
-- starting point the admin can then tweak.
PagerMod.PRESETS = {
    -- 2: Convenient — the relaxed defaults spelled out, so picking this always
    --    returns to the easy full-feature config (map-wide, two-way, all on).
    [PagerMod.Preset.CONVENIENT] = {
        maxMessages      = 50,
        messageMaxLength = 140,
        locationSharing  = PagerMod.LocationMode.OPT_IN,
        readReceipts     = true,
        allowSOS         = true,
        allowBlocking    = true,
        allowBroadcast   = true,
        pagerMode        = PagerMod.PagerMode.BOTH,
        signalMode       = PagerMod.SignalMode.GLOBAL,
    },
    -- 3: Almost Realistic ("semi") — a barely-functional pocket pager, but still
    --    map-wide (no tower needed).
    [PagerMod.Preset.ALMOST] = {
        maxMessages      = 1,
        messageMaxLength = 16,
        locationSharing  = PagerMod.LocationMode.OFF,
        readReceipts     = false,
        allowSOS         = false,
        allowBlocking    = false,
        allowBroadcast   = false,
        signalMode       = PagerMod.SignalMode.GLOBAL,
    },
    -- 4: Ultra Realistic — Almost Realistic, plus receive-only pagers and a
    --    network that goes map-wide only until the power dies, then needs a
    --    crafted, powered pager tower (Powered tower only - after power dies).
    [PagerMod.Preset.ULTRA] = {
        maxMessages      = 1,
        messageMaxLength = 16,
        locationSharing  = PagerMod.LocationMode.OFF,
        readReceipts     = false,
        allowSOS         = false,
        allowBlocking    = false,
        allowBroadcast   = false,
        pagerMode        = PagerMod.PagerMode.RECEIVE_ONLY,
        signalMode       = PagerMod.SignalMode.TOWER_AFTER,
    },
}

-- In-game hours a fresh battery lasts (0 = never dies).
function PagerMod.batteryLifeHours()
    return BATTERY_HOURS[PagerMod.Config.batteryLife] or 2160
end

-- Whether pagers may send (false in receive-only mode).
function PagerMod.canSend()
    return PagerMod.Config.pagerMode ~= PagerMod.PagerMode.RECEIVE_ONLY
end

-- Does this square currently have power for a tower? Mirrors the server's
-- isTilePowered: NONE = always powered; GRID_AND_GEN = mains grid OR a running
-- fuelled generator within TOWER_GEN_RANGE; GEN_ONLY = generator only. Used
-- client-side to gate the "Use pager tower" menu (the server re-checks too).
function PagerMod.squareHasTowerPower(square)
    PagerMod.refreshConfig()
    local mode = PagerMod.Config.towerPower or PagerMod.TowerPower.GRID_AND_GEN
    if mode == PagerMod.TowerPower.NONE then return true end
    if not square then return false end
    if mode == PagerMod.TowerPower.GRID_AND_GEN then
        local ok, has = pcall(function()
            return ((square.hasGridPower and square:hasGridPower()) or square:haveElectricity()) == true
        end)
        if ok and has then return true end
    end
    -- A running, fuelled generator nearby counts in GRID_AND_GEN and GEN_ONLY.
    local cell = getCell and getCell()
    if not cell then return false end
    local x, y, z = square:getX(), square:getY(), square:getZ()
    local r = PagerMod.TOWER_GEN_RANGE or 8
    if r > 8 then r = 8 end
    for dx = -r, r do
        for dy = -r, r do
            local sq = cell:getGridSquare(x + dx, y + dy, z)
            local objs = sq and sq:getObjects()
            if objs then
                for i = 0, objs:size() - 1 do
                    local o = objs:get(i)
                    if o and instanceof(o, "IsoGenerator") then
                        local ok, on = pcall(function() return o:isActivated() and (o:getFuel() or 0) > 0 end)
                        if ok and on then return true end
                    end
                end
            end
        end
    end
    return false
end

-- Pull values from SandboxVars if present. Safe to call repeatedly.
-- Presets are a UI convenience only: choosing one seeds the individual sandbox
-- controls (see client/PagerMod_SandboxUI.lua), and those individual values are
-- authoritative here — we do not re-apply preset overrides at runtime.
function PagerMod.refreshConfig()
    local sv = SandboxVars and SandboxVars.PagerMod
    if not sv then return PagerMod.Config end
    local c = PagerMod.Config
    if sv.Preset           ~= nil then c.preset           = sv.Preset end
    if sv.SignalMode       ~= nil then c.signalMode       = sv.SignalMode end
    if sv.SignalRange      ~= nil then c.signalRange      = sv.SignalRange end
    if sv.PagerMode        ~= nil then c.pagerMode        = sv.PagerMode end
    if sv.AllowBroadcast   ~= nil then c.allowBroadcast   = sv.AllowBroadcast end
    if sv.MaxMessages      ~= nil then c.maxMessages      = sv.MaxMessages end
    if sv.MessageMaxLength ~= nil then c.messageMaxLength = sv.MessageMaxLength end
    if sv.NotifySound      ~= nil then c.notifySound      = sv.NotifySound end
    if sv.BeepVolume       ~= nil then c.beepVolume       = sv.BeepVolume end
    if sv.LocationSharing  ~= nil then c.locationSharing  = sv.LocationSharing end
    if sv.SendCooldown     ~= nil then c.sendCooldown     = sv.SendCooldown end
    if sv.ReadReceipts     ~= nil then c.readReceipts     = sv.ReadReceipts end
    if sv.FactionScope     ~= nil then c.factionScope     = sv.FactionScope end
    if sv.AllowSOS         ~= nil then c.allowSOS         = sv.AllowSOS end
    if sv.AllowBlocking    ~= nil then c.allowBlocking    = sv.AllowBlocking end
    if sv.BatteryLife      ~= nil then c.batteryLife      = sv.BatteryLife end
    if sv.TowerComms       ~= nil then c.towerComms       = sv.TowerComms end
    if sv.TowerLimit       ~= nil then c.towerLimit       = sv.TowerLimit end
    if sv.TowerPower       ~= nil then c.towerPower       = sv.TowerPower end
    if sv.GlobalChat       ~= nil then c.globalChat       = sv.GlobalChat end
    if sv.NumericOnly      ~= nil then c.numericOnly      = sv.NumericOnly end
    if sv.SpawnAbundance   ~= nil then c.spawnAbundance   = sv.SpawnAbundance end
    if sv.LogPages         ~= nil then c.logPages         = sv.LogPages end
    -- No preset override here on purpose: the individual options above already
    -- carry the preset's values once it has been applied in the sandbox UI.
    return c
end

-- Whether the tower node itself accepts incoming pages (000-0001 has an inbox).
function PagerMod.towerCanReceive()
    return PagerMod.Config.towerComms ~= PagerMod.TowerComms.SEND_ONLY
end

-- ── Helpers ────────────────────────────────────────────────

-- Format a 7-digit pager number as XXX-XXXX for display.
function PagerMod.formatNumber(num)
    if not num then return "-------" end
    num = tostring(num)
    if #num == 7 then
        return num:sub(1,3) .. "-" .. num:sub(4,7)
    end
    return num
end

-- Strip everything except digits (for number entry fields).
function PagerMod.sanitizeNumber(str)
    if not str then return "" end
    return (tostring(str):gsub("[^%d]", ""))
end

-- Clamp/trim a message body to the configured maximum length.
function PagerMod.trimMessage(text)
    text = tostring(text or "")
    text = text:gsub("^%s+", ""):gsub("%s+$", "")
    -- Numbers-only mode: strip everything except digits, spaces and dashes.
    if PagerMod.Config.numericOnly then
        text = text:gsub("[^%d%s%-]", ""):gsub("^%s+", ""):gsub("%s+$", "")
    end
    local maxLen = PagerMod.Config.messageMaxLength or 140
    if #text > maxLen then
        text = text:sub(1, maxLen)
    end
    return text
end

-- Flat 2D distance between two world points.
function PagerMod.distance(x1, y1, x2, y2)
    local dx = x1 - x2
    local dy = y1 - y2
    return math.sqrt(dx * dx + dy * dy)
end

-- Build a compact timestamp table from the current in-game time.
function PagerMod.gameStamp()
    local gt = getGameTime()
    return {
        y = gt:getYear(),
        mo = gt:getMonth() + 1,
        d = gt:getDay() + 1,
        h = gt:getHour(),
        mi = gt:getMinutes(),
    }
end

-- Human readable clock from a stamp table.
function PagerMod.stampToClock(stamp)
    if not stamp then return "--:--" end
    return string.format("%02d:%02d", stamp.h or 0, stamp.mi or 0)
end

-- Human readable date from a stamp table.
function PagerMod.stampToDate(stamp)
    if not stamp then return "--/--" end
    return string.format("%02d/%02d", stamp.d or 0, stamp.mo or 0)
end

-- 8-point compass bearing from a delta (dx east, dy south in PZ coords).
-- Returns "" when there is no meaningful direction (you're on the spot).
function PagerMod.compass(dx, dy)
    dx = tonumber(dx) or 0
    dy = tonumber(dy) or 0
    if dx == 0 and dy == 0 then return "" end
    local dirs = { "E", "SE", "S", "SW", "W", "NW", "N", "NE" }
    local atan2 = math.atan2 or math.atan
    local ang = atan2(dy, dx) or 0 -- -pi..pi, 0 = east, +y = south
    local idx = math.floor((ang / (math.pi / 4)) + 0.5) % 8
    if idx < 0 then idx = idx + 8 end
    return dirs[idx + 1] or ""  -- never nil (callers concat the result)
end

-- Short distance label in tiles ("≈ 340t").
function PagerMod.distanceLabel(dist)
    dist = math.floor(dist + 0.5)
    if dist >= 1000 then
        return string.format("~%.1fkt", dist / 1000)
    end
    return "~" .. dist .. "t"
end

if not PagerMod._announced then
    PagerMod._announced = true
    print("[PagerMod] Shared loaded v" .. PagerMod.VERSION)
end
