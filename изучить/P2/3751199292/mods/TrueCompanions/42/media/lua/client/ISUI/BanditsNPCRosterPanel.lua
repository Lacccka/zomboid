--
-- Bandits NPC - Companions Overhaul - Companion Roster (overview panel)
--
-- A button on the vanilla left sidebar (getPlayerData(0).equipped -- the
-- ISEquippedItem panel, see ISEquippedItem.lua:1062) toggles an overview window
-- of every companion the local player has recruited.
--
-- 13 Jul redesign (author feedback): NEAREST COMPANION FIRST, ACCORDION cards
-- (click the header to expand/collapse), and each card carries only what a
-- control panel needs -- HEALTH plus QUICK ORDERS: Follow / Stay (holds her
-- current position -- no tile cursor, that's what makes it quick), stance
-- Defensive / Aggressive, Firearms ON/OFF, and a Details chip that opens the
-- full companion window for everything else. All order handlers are the same
-- BanditsNPC.Interact functions the dialog window uses.
--
-- Look reverse-engineered from HDX_Strangers' NPCs panel; TECHNIQUE only, per
-- the no-third-party-assets rule: everything here is our own drawRect/drawText
-- work and the button icon is the VANILLA media/ui/group.png. Rows are drawn
-- (not child widgets) with click hit-testing in onMouseUp, so rows keep
-- working while the list scrolls.
--

require "ISUI/ISCollapsableWindow"
require "ISUI/ISButton"

BanditsNPCRosterPanel = ISCollapsableWindow:derive("BanditsNPCRosterPanel")

-- ===== scaling (mirrors BanditsNPCDialogWindow; recomputed on open) =====
local SCALE = 1
local SMALL_FONT = UIFont.Small
local MED_FONT = UIFont.Medium
local FONT_HGT = getTextManager():getFontHeight(UIFont.Small)
local FONT_MED = getTextManager():getFontHeight(UIFont.Medium)
local PAD = 10
local BTN_H = FONT_HGT + 8
local function sc(v) return math.floor(v * (SCALE or 1)) end
-- Thresholds and maths live in BanditsNPC.UIScaleValues so this window and the roster
-- panel cannot drift apart; the locals stay here because every line below uses them.
local function bnApplyScale()
    SCALE, SMALL_FONT, MED_FONT, FONT_HGT, FONT_MED, PAD, BTN_H =
        BanditsNPC.UIScaleValues()
end

-- expand/collapse state per companion, survives re-renders (session-local)
local expandedState = {}

local function uidOf(brain)
    return tostring(brain.npcUid or brain.id or "?")
end

-- ===== data =====
-- Companions of the local player, from Bandits' own light bandit cache
-- (CacheLightB holds only brained bandits -- a handful of entries -- so this is
-- cheap enough to refresh a few times a second while the panel is open).
-- Companions of the local player. IMPORTANT (29 Jul): the cache this reads is rebuilt from
-- cell:getZombieList() once a minute (BanditZombie.flush), so it only ever contains
-- companions who are currently STREAMED IN. Listing just those meant a companion left at
-- base dropped off the panel entirely the moment the player walked away -- indistinguishable
-- from having lost her, and a direct contributor to "my companions aren't showing". She is
-- fine; she is simply out of range. So the roster is now seeded from the PERSISTENCE ROSTER
-- (every companion we know about) and the live cache only supplies position/health for the
-- ones actually loaded. Out-of-range companions render as a muted "Away" card: present,
-- accounted for, but not orderable, because there is no entity to give an order to.
local function collectCompanions()
    local out = {}
    local player = getSpecificPlayer(0)
    if not player then return out end
    local cacheB = (BanditZombie and BanditZombie.CacheLightB) or {}
    local cache = (BanditZombie and BanditZombie.Cache) or {}
    local px, py = player:getX(), player:getY()

    local seen = {}
    for id, light in pairs(cacheB) do
        local brain = light.brain
        if brain and brain.recruited and BanditsNPC.IsMine and BanditsNPC.IsMine(brain) then
            local z = cache[id]
            local dead = false
            if z then pcall(function() dead = z:isDead() end) end
            if z and not dead then
                local dx, dy = (light.x or px) - px, (light.y or py) - py
                if brain.npcUid then seen[brain.npcUid] = true end
                out[#out + 1] = { zombie = z, brain = brain, dist = math.sqrt(dx * dx + dy * dy) }
            end
        end
    end

    -- rostered but not loaded right now -> "Away", sorted to the bottom
    pcall(function()
        local roster = ModData.getOrCreate("TrueCompanionsRoster")
        for uid, entry in pairs(roster or {}) do
            local brain = entry and entry.brain
            if brain and not seen[uid] and BanditsNPC.IsMine and BanditsNPC.IsMine(brain) then
                out[#out + 1] = { zombie = nil, brain = brain, dist = math.huge,
                                  away = true, riding = entry.riding and true or false }
            end
        end
    end)
    -- nearest first (author ask 13 Jul); name breaks ties so equal-distance
    -- companions don't swap rows every refresh
    table.sort(out, function(a, b)
        if math.abs(a.dist - b.dist) > 0.01 then return a.dist < b.dist end
        return (a.brain.fullname or "") < (b.brain.fullname or "")
    end)
    return out
end

-- health % like the dialog window header (brain.health is her provisioned max)
-- nil for an out-of-range companion: there is no entity to read health off, and showing 0%
-- (what the old pcall-swallows-the-error path produced) would render her as dying
local function healthPct(e)
    if not e.zombie then return nil end
    local cur = 0
    pcall(function() cur = e.zombie:getHealth() end)
    local maxh = (e.brain.health and e.brain.health > 0) and e.brain.health or 1
    if cur > maxh and cur <= 100 then maxh = 100 end
    return math.max(0, math.min(100, (cur / maxh) * 100))
end

-- Is this card's companion currently on Follow? The follow-distance sub-row appears only
-- then, and cardHeight and drawCard have to agree about it or the cards overlap -- so both
-- ask the same question here rather than each testing the program name themselves.
local function followingCard(e)
    return e and not e.away and e.brain and e.brain.program
           and e.brain.program.name == "NPCCompanion"
end

-- ===== window =====
-- The size the player last dragged it to. Session-local on purpose: this window is opened
-- and closed constantly, and re-measuring it every time would be worse than either
-- remembering it or not.
local lastSize = nil

-- NARROWEST USABLE WIDTH, ONE THIRD OF THE SCREEN TALL (author ask, 8 Aug -- the third and
-- final word on this shape, after 1:3 on 6 Aug and 1:2 on 7 Aug).
--
-- Both numbers are now absolute rather than a ratio, which is what the author actually
-- wanted all along and is also more predictable: a ratio derived the width from the height,
-- so a taller screen silently made the window wider too. It is a list of companion cards
-- down the side of the screen -- the width only has to fit a card, and every pixel past
-- that is wasted. Still resizable; this is only the shape it OPENS at.
local MIN_W = 300      -- also the resize floor, set on minimumWidth below -- keep in step
local HEIGHT_FRACTION = 1 / 3

function BanditsNPCRosterPanel:new()
    local sw, sh = getCore():getScreenWidth(), getCore():getScreenHeight()
    local w = sc(MIN_W)
    local h = math.floor(sh * HEIGHT_FRACTION)
    -- On a very short screen a third of the height is not enough for one card; on a very
    -- narrow one the minimum width will not fit either. Clamp both, in that order.
    if h < sc(200) then h = math.min(sc(200), sh - sc(40)) end
    if w > sw - sc(40) then w = sw - sc(40) end
    if lastSize then
        w = math.min(math.max(lastSize.w, sc(MIN_W)), sw - sc(40))
        h = math.min(math.max(lastSize.h, sc(200)), sh - sc(40))
    end
    w, h = math.floor(w), math.floor(h)
    -- open near the left edge, next to the sidebar button that summoned it
    local o = ISCollapsableWindow.new(self, sc(72), math.floor((sh - h) / 2), w, h)
    o.title = BanditsNPC.T("UI_BN_Roster_Title", "Companions")
    -- RESIZABLE (v0.75.61, author ask). Nothing in the drawing needed changing for this --
    -- render() already lays every card out from self.width and clips the list to
    -- self.height, so it reflows on its own; the window was simply built with the flag off.
    -- Floors, not guesses: a card's three chips stop being readable much under 300 wide,
    -- and under 200 tall the list area is shorter than one expanded card.
    o.resizable = true
    o.minimumWidth = sc(MIN_W)      -- the width it OPENS at, so the two cannot drift apart
    o.minimumHeight = sc(200)
    o.scrollY = 0
    o._maxScroll = 0
    o._hits = {}
    o._list = {}
    o._nextCollectMs = 0
    return o
end

function BanditsNPCRosterPanel:close()
    -- Give the controller back BEFORE anything else can fail. See BanditsNPCJoypad: a
    -- window that closes while holding joypad focus takes the pad away from the game.
    if BanditsNPC.Joypad then BanditsNPC.Joypad.Release() end
    lastSize = { w = self.width, h = self.height }
    self:setVisible(false)
    self:removeFromUIManager()
    BanditsNPCRosterPanel.instance = nil
end

-- THE SHARED PALETTE, not a private one (v0.77.6). This panel had its own greens, its own
-- greys and its own borders, drifted a shade away from the dialog window's -- which is the
-- exact drift BanditsNPCUIStyle exists to end. Everything below reads U.C now, and the
-- ACTIVE state is the vanilla list-selection orange rather than the green it used to be.
function BanditsNPCRosterPanel:drawBar(x, y, w, h, v)
    local U = BanditsNPC.UI
    v = math.max(0, math.min(100, v or 0))
    U.Fill(self, x, y, w, h, { r = 0, g = 0, b = 0, a = 0.5 })
    local fw = math.floor(w * v / 100)
    if fw > 0 then
        -- Same three bands as the dialog window's health bar, so a companion who reads
        -- "hurt" on one panel cannot read "fine" on the other.
        local c = U.C.good
        if v < 30 then c = { r = 0.78, g = 0.34, b = 0.30, a = 1 }
        elseif v < 60 then c = U.C.warn end
        U.Fill(self, x, y, fw, h, c)
    end
    U.Border(self, x, y, w, h, U.C.hairline)
end

-- one quick-order chip; records a hit rect. lit=true renders as the active state.
function BanditsNPCRosterPanel:chip(label, x, y, w, lit, kind, e)
    local U = BanditsNPC.UI
    local mx, my = self:getMouseX(), self:getMouseY()
    local hover = mx >= x and mx <= x + w and my >= y and my <= y + BTN_H
    local bg = lit and U.C.activeBg or U.C.btnBg
    local bc = lit and U.C.activeBorder or U.C.btnBorder
    local tc = lit and U.C.activeText or U.C.btnText
    U.Fill(self, x, y, w, BTN_H, bg)
    if hover then U.Fill(self, x, y, w, BTN_H, { r = 1, g = 1, b = 1, a = 0.10 }) end
    U.Border(self, x, y, w, BTN_H, bc)
    local tw = getTextManager():MeasureStringX(SMALL_FONT, label)
    self:drawText(U.Fit(label, w - U.S(6)), x + math.floor((w - math.min(tw, w - U.S(6))) / 2),
                  y + math.floor((BTN_H - FONT_HGT) / 2), tc.r, tc.g, tc.b, 1, SMALL_FONT)
    table.insert(self._hits, { x = x, y = y, w = w, h = BTN_H, kind = kind, zombie = e.zombie, uid = uidOf(e.brain) })
end

-- Portrait box + name over a subtitle line, per the mock-up. The old header was one line
-- with just the name on it, so a collapsed roster told you nothing about what anyone was
-- doing -- which is the one thing an overview panel is for.
function BanditsNPCRosterPanel:hdrHeight()
    return sc(6) + FONT_MED + FONT_HGT + sc(6) + 3   -- name + subtitle, health strip beneath
end

function BanditsNPCRosterPanel:cardHeight(e)
    local h = self:hdrHeight()
    if expandedState[uidOf(e.brain)] then
        if e.away then
            -- explanatory line, then the Unstuck row: an out-of-range companion is
            -- precisely the one you most want to be able to call back
            h = h + sc(8) + FONT_HGT + sc(8) + BTN_H + sc(8)
        else
            -- Health row, then Movement (header + two strips), then Stance (header, strip,
            -- firearms), then the action row. Measured here rather than guessed: getting
            -- this wrong overlaps the next card instead of just looking odd.
            h = h + sc(6) + FONT_HGT + sc(2) + sc(12)             -- Health label + bar
                  + sc(8) + FONT_HGT + sc(4) + BTN_H              -- "Movement" + Follow/Stay
                  + sc(8) + FONT_HGT + sc(4) + BTN_H + sc(4) + BTN_H  -- "Stance" + strip + firearms
                  + sc(8) + BTN_H + sc(10)                        -- unstick / Profile row
            -- The follow-distance strip only exists while she is actually following, so the
            -- card grows and shrinks with it.
            if followingCard(e) then h = h + sc(3) + BTN_H end
        end
    end
    return h
end

-- A welded strip of chips: one row, buttons butting edge to edge, exactly one lit. The same
-- shape the dialog window's Movement and Stance use, which is what the mock-up draws here.
function BanditsNPCRosterPanel:strip(x, y, w, items, e)
    local n = #items
    if n == 0 then return y end
    local base = math.floor(w / n)
    local extra = w - base * n
    local cx = x
    for i, it in ipairs(items) do
        local sw = base + (i <= extra and 1 or 0)
        self:chip(it.label, cx, y, sw, it.lit, it.kind, e)
        cx = cx + sw
    end
    return y + BTN_H
end

function BanditsNPCRosterPanel:drawCard(e, x, y, w)
    local brain = e.brain
    local uid = uidOf(brain)
    local open = expandedState[uid] and true or false
    local ch = self:cardHeight(e)
    local hdrH = self:hdrHeight()
    local hp = healthPct(e)
    local U = BanditsNPC.UI
    local hc = U.C.good
    if hp and hp < 30 then hc = { r = 0.78, g = 0.34, b = 0.30, a = 1 }
    elseif hp and hp < 60 then hc = U.C.warn end

    U.Container(self, x, y, w, ch)
    -- An OPEN card is lifted a shade and gets the orange left edge, the same mark the dialog
    -- window's active tab uses: one idiom for "this is the one you are looking at".
    if open then
        U.Fill(self, x + 1, y + 1, w - 2, ch - 2, { r = 1, g = 1, b = 1, a = 0.03 })
        U.Fill(self, x, y, U.S(3), ch, U.C.activeBorder)
    end

    -- HEADER, to the mock-up: chevron, portrait box, name over a subtitle, then a health
    -- sliver and the distance on the right.
    local tm = getTextManager()
    local yy = y + sc(6)
    local nameA = e.away and 0.62 or 1
    -- ASCII, not U+25B8/U+25BE. The game fonts have no geometric-shape glyphs, so the
    -- triangles rendered as the question mark the author saw beside every card. "-" and "+"
    -- are what this used before and they read as open/closed perfectly well.
    self:drawText(open and "-" or "+", x + sc(7), yy + sc(1),
                  U.C.label.r, U.C.label.g, U.C.label.b, 1, MED_FONT)

    -- An EMPTY framed thumbnail, as the mock-up draws it. There is one 3D portrait renderer
    -- mod-wide and it belongs to the dialog window, so this cannot be a real render; it held
    -- her initial for a while, and a lone "N" in a box says less than the box alone.
    local pbox = FONT_MED + FONT_HGT - sc(2)
    local px = x + sc(20)
    U.Fill(self, px, yy, pbox, pbox, { r = 0, g = 0, b = 0, a = 0.45 })
    U.Border(self, px, yy, pbox, pbox, U.C.hairline)

    -- an out-of-range companion has no meaningful distance: say where she is, not "0m"
    local distTxt
    if e.away then
        distTxt = e.riding and BanditsNPC.T("UI_BN_Roster_Riding", "riding")
                           or BanditsNPC.T("UI_BN_Roster_Away", "away")
    else
        distTxt = string.format("%dm", math.floor((e.dist or 0) + 0.5))
    end
    local distW = tm:MeasureStringX(SMALL_FONT, distTxt)

    -- A health SLIVER beside the distance, so a collapsed card still answers "is anyone
    -- hurt" without being opened.
    local sliverW = sc(46)
    local sliverX = x + w - sc(8) - distW - sc(6) - sliverW
    if hp then
        U.Fill(self, sliverX, yy + math.floor(FONT_MED / 2) - 1, sliverW, sc(5), { r = 0, g = 0, b = 0, a = 0.5 })
        U.Fill(self, sliverX, yy + math.floor(FONT_MED / 2) - 1, math.floor(sliverW * hp / 100), sc(5), hc)
    end
    self:drawText(distTxt, x + w - sc(8) - distW, yy + math.floor((FONT_MED - FONT_HGT) / 2),
                  U.C.muted.r, U.C.muted.g, U.C.muted.b, 1, SMALL_FONT)

    local nx = px + pbox + sc(7)
    local nameW = sliverX - nx - sc(6)
    self:drawText(U.Fit(brain.fullname or "?", nameW), nx, yy, nameA, nameA, nameA, 1, MED_FONT)
    -- The subtitle is what she is DOING, which is the whole point of an overview.
    local sub
    if e.away then
        sub = e.riding and BanditsNPC.T("UI_BN_Roster_Riding", "riding")
                        or BanditsNPC.T("UI_BN_Roster_Away", "away")
    else
        local stanceName = ({
            passive    = BanditsNPC.T("UI_BN_Stance_Passive", "Passive"),
            defensive  = BanditsNPC.T("UI_BN_Stance_Defensive", "Defensive"),
            aggressive = BanditsNPC.T("UI_BN_Stance_Aggressive", "Aggressive"),
        })[brain.npcStance or "defensive"] or ""
        sub = BanditsNPC.ProgramLabel(brain.program and brain.program.name, "short")
              .. "  \194\183  " .. stanceName
    end
    self:drawText(U.Fit(sub, nameW), nx, yy + FONT_MED,
                  U.C.muted.r, U.C.muted.g, U.C.muted.b, 1, SMALL_FONT)
    -- whole header toggles the accordion
    table.insert(self._hits, { x = x, y = y, w = w, h = hdrH, kind = "toggle", zombie = e.zombie, uid = uid })

    if not open then return y + ch + sc(6) end

    -- Out of range: no entity exists, so there is nothing to read health from and nothing to
    -- give an order to. Say so plainly instead of drawing dead-looking bars and chips that
    -- would silently do nothing when clicked.
    if e.away then
        self:drawText(e.riding and BanditsNPC.T("UI_BN_Roster_AwayRiding", "Riding in your vehicle.")
                                or BanditsNPC.T("UI_BN_Roster_AwayHint", "Out of range - waiting where you left them."),
            x + sc(8), y + hdrH + sc(8), 0.66, 0.7, 0.74, 1, SMALL_FONT)
        -- Offered even here -- ESPECIALLY here. "Out of range" and "lost" look
        -- identical from this panel, and this is the button that resolves both.
        self:chip(BanditsNPC.T("UI_BN_Roster_Unstuck", "Bring here / unstick"),
                  x + sc(8), y + hdrH + sc(8) + FONT_HGT + sc(6), w - sc(16), false, "unstuck", e)
        return y + ch + sc(6)
    end

    -- Health bar
    local ix, iw = x + sc(8), w - sc(16)
    yy = y + hdrH + sc(6)
    self:drawText(BanditsNPC.T("UI_BN_Roster_Health", "Health"), ix, yy,
                  U.C.label.r, U.C.label.g, U.C.label.b, 1, SMALL_FONT)
    local pct = tostring(math.floor(hp))
    local pw = getTextManager():MeasureStringX(SMALL_FONT, pct)
    self:drawText(pct, x + w - sc(8) - pw, yy, U.C.btnText.r, U.C.btnText.g, U.C.btnText.b, 1, SMALL_FONT)
    yy = yy + FONT_HGT + sc(2)
    self:drawBar(ix, yy, iw, sc(12), hp)
    yy = yy + sc(12) + sc(8)

    -- MOVEMENT: a labelled section of welded strips, the shape the mock-up draws and the
    -- same one the dialog window's Orders tab uses. Three loose chips in a 3-column grid
    -- put Follow, Stay and a Details button on one row as if they were the same kind of
    -- thing, and left the stance row looking identical to the movement row above it.
    local progName = brain.program and brain.program.name
    yy = U.Header(self, ix, yy, iw, BanditsNPC.T("UI_BN_Lbl_Movement", "Movement"))
    yy = self:strip(ix, yy, iw, {
        { label = BanditsNPC.T("UI_BN_Order_Follow", "Follow"), lit = progName == "NPCCompanion", kind = "follow" },
        { label = BanditsNPC.T("UI_BN_Order_Stay", "Stay"),     lit = progName == "NPCStay",      kind = "stay" },
    }, e)

    -- FOLLOW DISTANCE, only while she is following (author ask, 9 Aug), directly under the
    -- strip it qualifies so it reads as a sub-setting rather than three more orders.
    if followingCard(e) then
        local fd = "near"
        pcall(function() fd = BanditsNPC.GetFollowDist(e.zombie) end)
        yy = yy + sc(3)
        yy = self:strip(ix, yy, iw, {
            { label = BanditsNPC.T("UI_BN_Follow_Near", "Near"),   lit = fd == "near", kind = "fd-near" },
            { label = BanditsNPC.T("UI_BN_Follow_Mid", "Middle"),  lit = fd == "mid",  kind = "fd-mid" },
            { label = BanditsNPC.T("UI_BN_Follow_Far", "Far"),     lit = fd == "far",  kind = "fd-far" },
        }, e)
    end
    yy = yy + sc(8)

    -- STANCE, then the firearms toggle on its own full-width row: it is not one of the
    -- stances and welding it to them said it was.
    local stance = brain.npcStance or "defensive"
    yy = U.Header(self, ix, yy, iw, BanditsNPC.T("UI_BN_Lbl_Stance", "Stance"))
    yy = self:strip(ix, yy, iw, {
        { label = BanditsNPC.T("UI_BN_Stance_Defensive", "Defensive"),   lit = stance == "defensive",  kind = "stance-def" },
        { label = BanditsNPC.T("UI_BN_Stance_Aggressive", "Aggressive"), lit = stance == "aggressive", kind = "stance-agg" },
    }, e)
    yy = yy + sc(4)
    local gunsOn = not brain.npcNoGuns
    self:chip(BanditsNPC.T(gunsOn and "UI_BN_FirearmsOn" or "UI_BN_FirearmsOff", gunsOn and "Firearms: ON" or "Firearms: OFF"),
              ix, yy, iw, gunsOn, "guns", e)
    yy = yy + BTN_H + sc(8)

    -- Recovery and the way into the full window, side by side as the mock draws them.
    local profW = math.floor(iw * 0.34)
    self:chip(BanditsNPC.T("UI_BN_Roster_Unstuck", "Bring here / unstick"),
              ix, yy, iw - profW - sc(6), false, "unstuck", e)
    self:chip(BanditsNPC.T("UI_BN_Roster_Profile", "Profile >"),
              ix + iw - profW, yy, profW, false, "details", e)

    return y + ch + sc(6)
end

function BanditsNPCRosterPanel:render()
    ISCollapsableWindow.render(self)
    if self.isCollapsed then return end
    self._hits = {}

    local now = getTimestampMs()
    if now >= (self._nextCollectMs or 0) then
        self._list = collectCompanions()
        self._nextCollectMs = now + 250
    end
    local list = self._list or {}

    local top = self:titleBarHeight() + PAD
    self:drawText(BanditsNPC.TF("UI_BN_Roster_Count", "Recruited: %1", #list),
        PAD, top, 0.85, 0.85, 0.85, 1, SMALL_FONT)
    local listTop = top + FONT_HGT + sc(6)
    -- Keep the list clear of the resize grip, or the bottom card is drawn underneath it and
    -- its chips swallow the drag that was meant to resize the window.
    local listBottom = self.height - PAD - self:resizeWidgetHeight()
    self._listTop, self._listBottom = listTop, listBottom

    if #list == 0 then
        local msg = BanditsNPC.T("UI_BN_Roster_Empty", "No companions recruited yet.")
        local hint = BanditsNPC.T("UI_BN_Roster_EmptyHint", "Talk to survivors you meet and ask them to join you.")
        local mw = getTextManager():MeasureStringX(SMALL_FONT, msg)
        local hw = getTextManager():MeasureStringX(SMALL_FONT, hint)
        local cy = math.floor((listTop + listBottom) / 2) - FONT_HGT
        self:drawText(msg, math.floor((self.width - mw) / 2), cy, 0.85, 0.85, 0.85, 1, SMALL_FONT)
        self:drawText(hint, math.floor((self.width - hw) / 2), cy + FONT_HGT + sc(4), 0.6, 0.6, 0.6, 1, SMALL_FONT)
        self._maxScroll = 0
        return
    end

    self:setStencilRect(0, listTop, self.width, listBottom - listTop)
    local y = listTop - (self.scrollY or 0)
    local contentH = 0
    for _, e in ipairs(list) do
        local ch = self:cardHeight(e) + sc(6)
        -- skip drawing cards fully outside the view; hits are only recorded for
        -- drawn cards, so off-screen chips can't be clicked
        if y + ch > listTop and y < listBottom then
            self:drawCard(e, PAD, y, self.width - PAD * 2)
        end
        y = y + ch
        contentH = contentH + ch
    end
    self:clearStencilRect()

    self._maxScroll = math.max(0, contentH - (listBottom - listTop))
    if (self.scrollY or 0) > self._maxScroll then self.scrollY = self._maxScroll end

    -- CONTROLLER SELECTION, drawn last so it sits over the card it marks. Guarded by the
    -- module's own kill switch: if controller support has failed or is simply not in use,
    -- this draws nothing and the panel is exactly as it was.
    if BanditsNPC.Joypad then BanditsNPC.Joypad.DrawSelection(self) end
end

-- Scroll the selected button into view. The controller has no scrollbar to grab, so a
-- selection that walks off the bottom of the list would look like the panel had frozen.
function BanditsNPCRosterPanel:EnsureVisible(h)
    local top, bot = self._listTop or 0, self._listBottom or self.height
    if not h then return end
    if h.y < top then
        self.scrollY = math.max(0, (self.scrollY or 0) - (top - h.y))
    elseif h.y + h.h > bot then
        self.scrollY = math.min(self._maxScroll or 0, (self.scrollY or 0) + (h.y + h.h - bot))
    end
end

function BanditsNPCRosterPanel:onMouseUp(x, y)
    ISCollapsableWindow.onMouseUp(self, x, y)
    if self.isCollapsed then return end
    if y < (self._listTop or 0) or y > (self._listBottom or self.height) then return end
    for _, h in ipairs(self._hits or {}) do
        if x >= h.x and x <= h.x + h.w and y >= h.y and y <= h.y + h.h then
            self:onHit(h)
            return
        end
    end
end

function BanditsNPCRosterPanel:onHit(h)
    local z = h.zombie
    if h.kind == "toggle" then
        expandedState[h.uid] = not expandedState[h.uid]
        return
    end

    -- Unstuck is handled BEFORE the `z` guard below, because the case it exists for is
    -- often that there is no live body to hold a reference to: an out-of-range card has
    -- e.zombie == nil, and that is exactly when the player needs this.
    if h.kind == "unstuck" then
        local ok, why = false, nil
        pcall(function()
            if BanditsNPC.Persistence and BanditsNPC.Persistence.Unstuck then
                ok, why = BanditsNPC.Persistence.Unstuck(h.uid)
            end
        end)
        local player = getSpecificPlayer(0)
        if player then
            if ok and why == "duplicates" then
                player:Say(BanditsNPC.T("UI_BN_Unstuck_Dupes", "Sorted them out - there was more than one."))
            elseif ok and why == "respawned" then
                player:Say(BanditsNPC.T("UI_BN_Unstuck_Back", "Brought them back."))
            elseif ok then
                player:Say(BanditsNPC.T("UI_BN_Unstuck_Ok", "Over here."))
            else
                player:Say(BanditsNPC.T("UI_BN_Unstuck_Fail", "No room to bring them here - try somewhere clearer."))
            end
        end
        return
    end

    if not (z and BanditsNPC.Interact) then return end
    pcall(function()
        if h.kind == "details" then
            if BanditsNPCDialogWindow and BanditsNPCDialogWindow.OpenFor then
                BanditsNPCDialogWindow.OpenFor(z)
            end
        elseif h.kind == "follow" then
            BanditsNPC.Interact.SetOrder(z, "NPCCompanion")
            pcall(function() z:addLineChatElement(BanditsNPC.T("UI_BN_Say_Follow", "Right behind you."), 0.3, 0.9, 0.3) end)
        elseif h.kind == "fd-near" or h.kind == "fd-mid" or h.kind == "fd-far" then
            local key = string.sub(h.kind, 4)
            BanditsNPC.SetFollowDist(z, key)
            local said = (key == "near" and BanditsNPC.T("UI_BN_Say_FollowNear", "I'll stick close."))
                      or (key == "mid" and BanditsNPC.T("UI_BN_Say_FollowMid", "I'll give you some room."))
                      or BanditsNPC.T("UI_BN_Say_FollowFar", "I'll hang back.")
            pcall(function() z:addLineChatElement(said, 0.3, 0.9, 0.3) end)
        elseif h.kind == "stay" then
            -- quick Stay = hold her CURRENT spot (the dialog window's Stay keeps
            -- its pick-a-tile cursor; a control panel wants one click)
            local brain = BanditBrain.Get(z)
            if brain then
                brain.stayPos = { x = math.floor(z:getX()), y = math.floor(z:getY()), z = math.floor(z:getZ()) }
                BanditsNPC.Interact.SetOrder(z, "NPCStay")   -- syncs program
                BanditBrain.Update(z, brain)
                if Bandit and Bandit.ForceSyncPart then
                    Bandit.ForceSyncPart(z, { id = brain.id, stayPos = brain.stayPos })
                end
                pcall(function() z:addLineChatElement(BanditsNPC.T("UI_BN_Say_Stay", "Staying here."), 0.3, 0.9, 0.3) end)
            end
        elseif h.kind == "stance-def" then
            BanditsNPC.Interact.SetStance(z, "defensive")
        elseif h.kind == "stance-agg" then
            BanditsNPC.Interact.SetStance(z, "aggressive")
        elseif h.kind == "guns" then
            local brain = BanditBrain.Get(z)
            if brain and BanditsNPC.Interact.SetFirearmsEnabled then
                -- npcNoGuns true means OFF -> clicking enables (and vice versa);
                -- SetFirearmsEnabled says its own overhead confirm
                BanditsNPC.Interact.SetFirearmsEnabled(z, brain.npcNoGuns and true or false)
            end
        end
    end)
end

function BanditsNPCRosterPanel:onMouseWheel(del)
    if self.isCollapsed then return false end
    self.scrollY = math.max(0, math.min(self._maxScroll or 0, (self.scrollY or 0) + del * sc(48)))
    return true
end

function BanditsNPCRosterPanel.Toggle()
    local inst = BanditsNPCRosterPanel.instance
    if inst then
        inst:close()
        return
    end
    bnApplyScale()
    local win = BanditsNPCRosterPanel:new()
    win:initialise()
    win:addToUIManager()
    BanditsNPCRosterPanel.instance = win

    -- CONTROLLER, IF THERE IS ONE (v0.76.1). Install the handlers on the class and take
    -- joypad focus so the D-pad can drive the panel -- but ONLY when a pad is actually
    -- connected for this player. On mouse and keyboard none of this runs, and if any part
    -- of it fails the module disables itself and the panel carries on as a mouse panel.
    -- Deliberately last: nothing above depends on it.
    pcall(function()
        local Jp = BanditsNPC.Joypad
        if not Jp then return end
        Jp.Install(BanditsNPCRosterPanel)
        if Jp.Present(0) then
            win.joySel = 1
            Jp.Grab(win, 0)
        end
    end)
end

-- ===== left-sidebar toggle button =====
-- Attached to the vanilla equipped-item panel the same way HDX mounts theirs:
-- add an ISButton below the last existing child and grow the panel. In MP the
-- sidebar often isn't built yet at OnCreatePlayer, so a self-removing OnTick
-- retry keeps trying until it attaches (the "button in SP but not MP" trap).
local sidebarAdded = false

local function addSidebarButton()
    if sidebarAdded then return end
    local pd = getPlayerData and getPlayerData(0)
    if not pd or not pd.equipped then return end
    local panel = pd.equipped

    local kids = panel:getChildren()
    if kids then
        for _, child in pairs(kids) do
            if child and child.internal == "BN_ROSTER" then sidebarAdded = true; return end
        end
    end

    -- vanilla sidebar size option -> button edge (ISEquippedItem.lua:8-25)
    local sizeIdx = (getCore().getOptionSidebarSize and getCore():getOptionSidebarSize()) or 1
    local szMap = { [1] = 48, [2] = 64, [3] = 80, [4] = 96, [5] = 128, [6] = 48 }
    local szBtn = szMap[sizeIdx] or 48

    local bottom = 0
    if kids then
        for _, child in pairs(kids) do
            if child then
                local b = child:getY() + child:getHeight()
                if b > bottom then bottom = b end
            end
        end
    end

    -- SQUARE (author ask, 6 Aug). v0.75.61 made it 4:3 to match vanilla's sidebar tiles
    -- (TEXTURE_HEIGHT = TEXTURE_WIDTH * 0.75, ISEquippedItem.lua:24); the author wants 1:1,
    -- which also suits the icon better -- group.png is square, so a 4:3 plate left dead
    -- margin either side of it.
    local btnH = szBtn

    local btn = ISButton:new(0, bottom + 6, szBtn, btnH, "", panel, function()
        BanditsNPCRosterPanel.Toggle()
    end)
    btn.internal = "BN_ROSTER"
    btn:initialise()
    btn:instantiate()

    -- "ALMOST INVISIBLE AND REALLY HARD TO SEE" (author, 6 Aug). It was, and the reason is
    -- that this copied the WRONG HALF of vanilla's sidebar buttons. They call
    -- setDisplayBackground(false) and get away with it because their art is purpose-made
    -- for the job: media/ui/Sidebar/48/Heart_Off_48.png is a 48x36 pale glyph with a dark
    -- outline, drawn to read against any world behind it. Ours borrowed media/ui/group.png,
    -- which is a small DARK line drawing meant for a panel interior -- no outline, no
    -- contrast, scaled up to 62% and left floating over the scenery with nothing behind it.
    --
    -- Two fixes rather than one, because either alone is thin: a proper dark plate with a
    -- pale border so the button has an edge wherever it happens to sit, and the glyph
    -- tinted near-white so it reads like the vanilla icons above it instead of vanishing
    -- into its own plate. Hover brightens the plate, which is feedback this button had none
    -- of either.
    --
    -- BOTH FLAGS, and this is the part that is easy to get wrong: setDisplayBackground only
    -- opens the gate in prerender. What decides whether the plate is actually painted while
    -- the mouse is elsewhere is ISButton:shouldDrawBackground -> `self.background and
    -- self.isBaseBackgroundVisible`, and `background` is never initialised by ISButton:new,
    -- so it is nil until something sets it (ISCharacterScreen.lua:322 sets it the other way
    -- for the same reason). Without this line the button draws its border on hover and
    -- nothing at all the rest of the time -- which is where we came in.
    btn:setDisplayBackground(true)
    btn.background = true
    btn.isBaseBackgroundVisible = true
    btn.isBorderVisible = true
    btn:setBackgroundRGBA(0.08, 0.09, 0.11, 0.82)
    btn:setBackgroundColorMouseOverRGBA(0.24, 0.30, 0.36, 0.95)
    btn:setBorderRGBA(0.62, 0.68, 0.76, 0.90)

    local icon = getTexture("media/ui/group.png")   -- vanilla icon, nothing shipped
    if icon then
        btn:setImage(icon)
        btn:setTextureRGBA(0.96, 0.97, 1.0, 1.0)
        -- Sized off the SHORT edge and kept square, so it cannot be stretched by the 4:3
        -- plate, with a little room left for the border.
        local ic = math.floor(btnH * 0.70)
        btn:forceImageSize(ic, ic)
    else
        btn:setTitle("NPC")
    end
    if btn.ignoreWidthChange then btn:ignoreWidthChange() end
    if btn.ignoreHeightChange then btn:ignoreHeightChange() end
    panel:addChild(btn)
    if panel.addMouseOverToolTipItem then
        panel:addMouseOverToolTipItem(btn, BanditsNPC.T("UI_BN_Roster_Title", "Companions"))
    end
    panel:setHeight(btn:getBottom())
    sidebarAdded = true
end

local retryTick
retryTick = function()
    pcall(addSidebarButton)
    if sidebarAdded then Events.OnTick.Remove(retryTick) end
end
local function armSidebarRetry()
    sidebarAdded = false
    Events.OnTick.Add(retryTick)
end

Events.OnCreatePlayer.Add(armSidebarRetry)
Events.OnGameStart.Add(armSidebarRetry)
Events.OnPlayerDeath.Add(armSidebarRetry)
Events.OnMainMenuEnter.Add(function() sidebarAdded = false end)
