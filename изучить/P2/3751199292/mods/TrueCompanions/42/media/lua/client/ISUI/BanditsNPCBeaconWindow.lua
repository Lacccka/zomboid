--
-- True Companions - Survivors Beacon control panel (v0.67)
--
-- Three tabs: Overview / Areas / Settings. Opened by right-clicking a placed
-- beacon, or from the beacon's context submenu.
--
-- The window owns NO logic. Every button calls straight into BanditsNPC.Beacon or
-- BanditsNPC.Zones, which the context menu also calls, so the two entry points
-- cannot drift apart -- that is exactly how the v0.47 outfit-slot bug happened.
--
-- WHY THE CONTENT LIVES IN A CHILD PANEL. v0.66 put every control straight on the
-- window at fixed coordinates in a fixed 520x420 frame, and the Settings tab ran off
-- the bottom with no way to reach the rest -- the window was not resizable and did
-- not scroll. Everything below the tab strip is now inside `self.body`, an ISPanel
-- with setScrollChildren(true), and the window is resizable with a floor under it.
-- Two consequences worth knowing:
--   * body children scroll for free (the engine offsets them), but TEXT drawn by
--     hand does not -- every drawText in the body adds self:getYScroll() itself;
--   * setScrollChildren needs the javaObject, so it cannot be called before
--     instantiate(). It is called from the body's own createChildren.
--
-- LABELS ARE NEVER PLACED TO THE LEFT OF A BUTTON. In v0.66 the option rows drew the
-- label from the left edge and the On/Off toggle from the right, and at any larger UI
-- scale the text ran underneath the button. Toggles now come FIRST and the label runs
-- to the right, where the only thing that can clip it is the window edge -- and it is
-- truncated to fit that anyway.
--

require "ISUI/ISCollapsableWindow"
require "ISUI/ISPanel"
require "ISUI/ISButton"

BanditsNPCBeaconWindow = ISCollapsableWindow:derive("BanditsNPCBeaconWindow")
BanditsNPCBeaconBody   = ISPanel:derive("BanditsNPCBeaconBody")

local FONT_HGT   = getTextManager():getFontHeight(UIFont.Small)
local FONT_HGT_M = getTextManager():getFontHeight(UIFont.Medium)
local PAD   = 14
local BTN_H = FONT_HGT + 10
local LINE  = FONT_HGT + 5
local TOG_W = 58            -- On/Off toggle: fixed, so labels always start in line
local ROWS  = 16            -- pooled area rows; a base with more than this is not a base

-- One palette, so nothing is a one-off colour picked at the call site.
local C = {
    text    = { 0.88, 0.88, 0.90 },
    dim     = { 0.62, 0.62, 0.66 },
    head    = { 1.00, 1.00, 1.00 },
    good    = { 0.50, 0.85, 0.55 },
    warn    = { 0.95, 0.76, 0.36 },
    rule    = { 0.32, 0.32, 0.38 },
    tabOn   = { 0.20, 0.24, 0.30 },
    tabOff  = { 0.11, 0.11, 0.13 },
}

local TABS = { "overview", "areas", "settings" }

local function tabLabel(id)
    if id == "areas"    then return BanditsNPC.T("UI_BN_Tab_Areas", "Areas") end
    if id == "settings" then return BanditsNPC.T("UI_BN_Tab_Settings", "Settings") end
    return BanditsNPC.T("UI_BN_Tab_Overview", "Overview")
end

-- Cuts `text` down to `maxW` pixels, ending in an ellipsis. Used for anything drawn
-- next to a control: a label that overruns its neighbour reads as a broken window,
-- and there is no clipping on drawText to fall back on.
local function fit(text, font, maxW)
    local tm = getTextManager()
    if maxW <= 0 or tm:MeasureStringX(font, text) <= maxW then return text end
    local out = text
    while #out > 1 and tm:MeasureStringX(font, out .. "...") > maxW do
        out = out:sub(1, #out - 1)
    end
    return out .. "..."
end

-- ===========================================================================
-- THE CONTENT PANEL
-- ===========================================================================

-- The setup order here is copied from ISComponentsListPanel.lua:16-21, which is the
-- only vanilla example of a scrolling panel full of live controls. All four steps
-- matter:
--   * addScrollBars BEFORE setScrollChildren;
--   * both need the javaObject, which only exists once instantiate() has run, and
--     setScrollChildren silently returns when it is nil -- so this belongs in
--     createChildren (instantiate creates the javaObject, THEN calls createChildren)
--     and nowhere earlier;
--   * doStencilRender clips children to the panel. Without it the buttons scrolled
--     out of the top keep drawing, over the tab strip and the footer.
function BanditsNPCBeaconBody:createChildren()
    ISPanel.createChildren(self)
    self:addScrollBars()
    self:setScrollChildren(true)
    self.doStencilRender = true
end

-- Same behaviour as vanilla's shared onMouseWheelScrollHandler (env.lua:119): return
-- false when there is nothing to scroll, so the event falls through instead of being
-- swallowed.
function BanditsNPCBeaconBody:onMouseWheel(del)
    if self:getScrollHeight() <= 0 then return false end
    self:setYScroll(self:getYScroll() - (del * 40))
    return true
end

function BanditsNPCBeaconBody:label(text, x, y, col, font)
    col = col or C.text
    self:drawText(text, x, y + self:getYScroll(), col[1], col[2], col[3], 1, font or UIFont.Small)
end

function BanditsNPCBeaconBody:hline(y)
    self:drawRect(PAD, y + self:getYScroll(), self.width - PAD * 2 - 16, 1,
                  0.6, C.rule[1], C.rule[2], C.rule[3])
end

function BanditsNPCBeaconBody:render()
    ISPanel.render(self)
    local win = self.win
    if not win then return end
    local B = BanditsNPC.Beacon
    if not (win.beacon and B.IsBeacon(win.beacon)) then return end
    local st = B.State(win.beacon)
    local site = win:site()
    if not (st and site) then return end

    if win.tab == "overview" then
        win:renderOverview(self, st, site)
    elseif win.tab == "areas" then
        win:renderAreas(self, site)
    else
        win:renderSettings(self, st)
    end
end

-- ===========================================================================
-- BUILD
-- ===========================================================================

function BanditsNPCBeaconWindow:createChildren()
    ISCollapsableWindow.createChildren(self)
    local Zs = BanditsNPC.Zones

    self.tab = self.tab or "overview"

    -- tab strip
    self.tabBtns = {}
    for i, id in ipairs(TABS) do
        local b = ISButton:new(0, 0, 10, BTN_H, tabLabel(id), self, self.onTab)
        b.internal = id
        b:initialise(); b:instantiate(); self:addChild(b)
        self.tabBtns[i] = b
    end

    -- footer
    self.dismantleBtn = ISButton:new(0, 0, 230, BTN_H,
        BanditsNPC.T("UI_BN_Beacon_RemoveOpt", "Dismantle the beacon"), self, self.onDismantle)
    self.dismantleBtn:initialise(); self.dismantleBtn:instantiate(); self:addChild(self.dismantleBtn)
    self.dismantleBtn.backgroundColor = { r = 0.30, g = 0.12, b = 0.12, a = 0.9 }

    self.closeBtn = ISButton:new(0, 0, 100, BTN_H,
        BanditsNPC.T("UI_BN_Close", "Close"), self, self.onClose)
    self.closeBtn:initialise(); self.closeBtn:instantiate(); self:addChild(self.closeBtn)

    -- scrolling content
    self.body = BanditsNPCBeaconBody:new(0, 0, 10, 10)
    self.body.win = self
    self.body.backgroundColor = { r = 0, g = 0, b = 0, a = 0 }
    self.body.borderColor = { r = 0, g = 0, b = 0, a = 0 }
    self.body:initialise(); self.body:instantiate(); self:addChild(self.body)

    local function add(b)
        b:initialise(); b:instantiate(); self.body:addChild(b)
        return b
    end

    -- ===== overview =====
    self.broadcastBtn = add(ISButton:new(0, 0, 230, BTN_H, "", self, self.onBroadcast))
    self.upgradeBtn   = add(ISButton:new(0, 0, 230, BTN_H, "", self, self.onUpgrade))

    -- DEBUG ONLY. Waiting an in-game hour per dice roll is fine for a player and
    -- useless for testing, so with debug mode (or admin) on there is a button that runs
    -- the arrival pipeline immediately -- see BanditsNPC.WildSpawn.Force for what it
    -- does and does not skip. The button is not created at all in a normal game, rather
    -- than created and hidden: a hidden button is one refresh() bug away from shipping.
    if isDebugEnabled() or isAdmin() then
        self.debugSpawnBtn = add(ISButton:new(0, 0, 230, BTN_H,
            BanditsNPC.T("UI_BN_Dbg_SpawnNow", "[Debug] Summon a survivor now"),
            self, self.onDebugSpawn))
        self.debugSpawnBtn.backgroundColor = { r = 0.24, g = 0.18, b = 0.32, a = 0.9 }
    end

    -- ===== areas =====
    self.baseBtn  = add(ISButton:new(0, 0, 230, BTN_H, "", self, self.onSetBase))
    self.showBtn  = add(ISButton:new(0, 0, 110, BTN_H,
        BanditsNPC.T("UI_BN_Zone_Show", "Show areas"), self, self.onShow))
    -- "Clear the base area", not "Clear all": there is one kind of area, and this is now
    -- the ONLY control that throws a drawn base away (v0.77.13).
    self.clearBtn = add(ISButton:new(0, 0, 110, BTN_H,
        BanditsNPC.T("UI_BN_Zone_ClearAll", "Clear the base area"), self, self.onClear))

    -- One pooled Remove per list row. Pooled rather than rebuilt because a button
    -- created during render is a button whose click lands on the NEXT frame's list.
    self.rowBtns = {}
    for i = 1, ROWS do
        local b = add(ISButton:new(0, 0, 80, BTN_H,
            BanditsNPC.T("UI_BN_Remove", "Remove"), self, self.onRemoveArea))
        b.internal = i
        b:setVisible(false)
        self.rowBtns[i] = b
    end

    -- ===== settings =====
    self.lessBtn = add(ISButton:new(0, 0, 34, BTN_H, "-", self, self.onWantedLess))
    self.moreBtn = add(ISButton:new(0, 0, 34, BTN_H, "+", self, self.onWantedMore))
    self.rateBtn = add(ISButton:new(0, 0, 200, BTN_H, "", self, self.onCycleRate))

    -- World rules. Stored globally (see BanditsNPC.SetOpt) because they are not
    -- per-station: a player may have two beacons or none, so "protect companions"
    -- cannot have a different answer at each one.
    self.optRows = {
        { key = "ProtectCompanions",      def = true,
          label = BanditsNPC.T("UI_BN_Opt_Protect", "Zombies prefer the player over companions") },
        { key = "ImmortalCompanions",     def = true,
          label = BanditsNPC.T("UI_BN_Opt_Immortal", "Companions go down instead of dying") },
        { key = "RestoreLostCompanions",  def = true,
          label = BanditsNPC.T("UI_BN_Opt_Restore", "Bring back companions the game loses") },
        { key = "CompanionsEnterVehicles", def = true,
          label = BanditsNPC.T("UI_BN_Opt_Vehicles", "Companions ride in vehicles") },
        { key = "AllowZombification",     def = false,
          label = BanditsNPC.T("UI_BN_Opt_Zombify", "Bitten companions can turn") },
        { key = "InfiniteAmmo",           def = false,
          label = BanditsNPC.T("UI_BN_Opt_Ammo", "Companions never run out of ammo") },
        { key = "PauseNearPlayer",        def = true,
          label = BanditsNPC.T("UI_BN_Opt_Panic", "Companions calm the player's panic") },
        { key = "NPCPortrait3D",          def = false,
          label = BanditsNPC.T("UI_BN_Opt_Portrait", "3D portrait in the companion window") },
        { key = "CompanionsCloseDoors",   def = true,
          label = BanditsNPC.T("UI_BN_Opt_Doors", "Companions close doors behind them at base") },
    }
    self.optBtns = {}
    for i = 1, #self.optRows do
        local b = add(ISButton:new(0, 0, TOG_W, BTN_H, "", self, self.onToggleOpt))
        b.internal = i
        self.optBtns[i] = b
    end

    self:layout()
    self:refresh()
end

-- ===========================================================================
-- LAYOUT -- recomputed on every resize and every tab change
-- ===========================================================================

function BanditsNPCBeaconWindow:layout()
    local top = self:titleBarHeight()
    local tw = math.floor((self.width - PAD * 2) / #TABS)
    for i, b in ipairs(self.tabBtns) do
        b:setX(PAD + (i - 1) * tw); b:setY(top + 6); b:setWidth(tw)
    end

    local footY = self.height - PAD - BTN_H
    self.footY = footY
    self.dismantleBtn:setX(PAD); self.dismantleBtn:setY(footY)
    self.closeBtn:setX(self.width - PAD - 100); self.closeBtn:setY(footY)

    local bodyY = top + 6 + BTN_H + 10
    self.bodyTop = bodyY
    self.body:setX(0)
    self.body:setY(bodyY)
    self.body:setWidth(self.width)
    self.body:setHeight(math.max(40, footY - 10 - bodyY))

    local inner = self.body.width - PAD * 2 - 16   -- 16 leaves room for the scrollbar
    self.inner = inner
    local h = 0

    -- ----- overview -----
    -- THE INDICATOR NEEDS ITS OWN BAND. It is drawn from a centre point and its arcs
    -- reach ~33px ABOVE that centre, so the first version sat far enough up that the
    -- waves ran back through the "supports up to" line and into the buttons. The band
    -- starts a full line below the last text row and the centre sits low inside it,
    -- and both are in LINE units so it holds at any UI scale.
    self.ovIndicatorY = LINE * 4 + 10
    self.ovIndicatorCY = self.ovIndicatorY + 34
    local ovBtnY = self.ovIndicatorY + 62
    self.broadcastBtn:setX(PAD); self.broadcastBtn:setY(ovBtnY)
    self.broadcastBtn:setWidth(math.min(240, math.floor(inner / 2) - 6))
    self.upgradeBtn:setX(PAD + self.broadcastBtn.width + 12); self.upgradeBtn:setY(ovBtnY)
    self.upgradeBtn:setWidth(self.broadcastBtn.width)
    -- Below the arrival-status line, which sits one LINE under the button row, plus a
    -- line of its own for the result message the button writes back.
    local ovBottom = ovBtnY + BTN_H + LINE * 3 + PAD
    if self.debugSpawnBtn then
        local dy = ovBtnY + BTN_H + LINE * 2 + 4
        self.dbgBtnY = dy
        self.debugSpawnBtn:setX(PAD); self.debugSpawnBtn:setY(dy)
        self.debugSpawnBtn:setWidth(math.min(260, inner))
        ovBottom = dy + BTN_H + LINE + PAD
    end
    if self.tab == "overview" then h = ovBottom end

    -- ----- areas -----
    local ay = 0
    self.baseBtn:setX(PAD); self.baseBtn:setY(ay); self.baseBtn:setWidth(240)
    self.showBtn:setX(PAD + 252); self.showBtn:setY(ay)
    self.clearBtn:setX(PAD + 252 + 116); self.clearBtn:setY(ay)
    ay = ay + BTN_H + LINE + 4
    -- Two lines of headroom above the list: the "Marked out" heading and, above it,
    -- the summary of what the base scan found.
    ay = ay + BTN_H + PAD + LINE * 2
    self.areaListY = ay
    for i, b in ipairs(self.rowBtns) do
        b:setX(PAD + inner - 80); b:setY(ay + (i - 1) * (BTN_H + 3) - 3)
    end
    if self.tab == "areas" then
        local n = 0
        local site = self:site()
        if site and type(site.zones) == "table" then n = #site.zones end
        h = ay + math.max(1, n) * (BTN_H + 3) + PAD
    end

    -- ----- settings -----
    local sy = LINE + 4
    self.wantedY = sy
    self.lessBtn:setX(PAD); self.lessBtn:setY(sy)
    self.moreBtn:setX(PAD + 100); self.moreBtn:setY(sy)
    self.rateBtn:setX(PAD + 180); self.rateBtn:setY(sy); self.rateBtn:setWidth(math.min(220, inner - 190))
    local optsY = sy + BTN_H + LINE * 3
    self.optsY = optsY
    for i, b in ipairs(self.optBtns) do
        b:setX(PAD); b:setY(optsY + (i - 1) * (BTN_H + 4))
    end
    if self.tab == "settings" then
        h = optsY + #self.optRows * (BTN_H + 4) + PAD
    end

    -- A SCROLLBAR ONLY WHEN THERE IS SOMETHING TO SCROLL. ISScrollBar hides itself
    -- when the scroll height fits, but only against ITS OWN height rather than the
    -- panel's, so a short tab still showed a bar that did nothing. Reporting a scroll
    -- height of zero settles it from our side: no bar, and onMouseWheel declines the
    -- event instead of swallowing it.
    local needScroll = h > self.body.height
    self.body:setScrollHeight(needScroll and h or 0)
    if self.body.vscroll then self.body.vscroll:setVisible(needScroll) end
    if not needScroll then self.body:setYScroll(0) end

    self.laidOutW, self.laidOutH = self.width, self.height
end

function BanditsNPCBeaconWindow:onResize()
    ISCollapsableWindow.onResize(self)
    self:layout()
end

-- BELT AND BRACES ON RESIZE. onResize is an engine callback and it is not obvious from
-- the Lua side that dragging ISResizeWidget always reaches it -- the widget calls
-- setWidth/setHeight, which push to the java object, and the callback comes back from
-- there. A stale layout after a drag would leave the footer buttons floating in the
-- middle of the window, so the size is also checked once a frame here, which cannot
-- miss whatever route the resize took.
function BanditsNPCBeaconWindow:prerender()
    ISCollapsableWindow.prerender(self)
    if self.laidOutW ~= self.width or self.laidOutH ~= self.height then
        self:layout()
    end
end

-- ===========================================================================
-- STATE
-- ===========================================================================

function BanditsNPCBeaconWindow:site()
    return BanditsNPC.Beacon.SiteFor(self.beacon)
end

function BanditsNPCBeaconWindow:onTab(button)
    self.tab = button.internal
    self.body:setYScroll(0)
    self:layout()
    self:refresh()
end

function BanditsNPCBeaconWindow:refresh()
    local B = BanditsNPC.Beacon
    local Zs = BanditsNPC.Zones

    -- The beacon can be dismantled or destroyed while this is open.
    if not (self.beacon and B.IsBeacon(self.beacon)) then self:onClose(); return end
    local st = B.State(self.beacon)
    local site = self:site()
    if not (st and site) then self:onClose(); return end

    for _, b in ipairs(self.tabBtns) do
        local on = (b.internal == self.tab)
        b.backgroundColor = on and { r = C.tabOn[1], g = C.tabOn[2], b = C.tabOn[3], a = 1 }
                               or { r = C.tabOff[1], g = C.tabOff[2], b = C.tabOff[3], a = 1 }
    end

    local ov = (self.tab == "overview")
    local ar = (self.tab == "areas")
    local se = (self.tab == "settings")

    self.broadcastBtn:setVisible(ov)
    self.upgradeBtn:setVisible(ov)
    if self.debugSpawnBtn then self.debugSpawnBtn:setVisible(ov) end
    self.baseBtn:setVisible(ar)
    self.showBtn:setVisible(ar)
    self.clearBtn:setVisible(ar)
    self.lessBtn:setVisible(se)
    self.moreBtn:setVisible(se)
    self.rateBtn:setVisible(se)

    -- WORLD RULES ARE ADMIN-ONLY IN MULTIPLAYER (v0.75.2). The server enforces this
    -- (BanditsNPCSpawnServer's SetOpt handler); disabling here only stops the UI
    -- promising a change the server will refuse, and leaves the values READABLE so a
    -- player can still see what world they are in. The companion cap below is NOT
    -- gated -- st.wanted is per-beacon station state, not a world rule.
    local mayRule = self:mayEditWorldRules()

    for i, row in ipairs(self.optRows) do
        local b = self.optBtns[i]
        b:setVisible(se)
        b:setEnable(mayRule)
        local on = BanditsNPC.Opt(row.key, row.def) and true or false
        b:setTitle(on and BanditsNPC.T("UI_BN_Opt_On", "On")
                      or BanditsNPC.T("UI_BN_Opt_Off", "Off"))
        b.backgroundColor = on and { r = 0.16, g = 0.30, b = 0.18, a = 1 }
                               or { r = 0.26, g = 0.14, b = 0.14, a = 1 }
    end
    self.rateBtn:setEnable(mayRule)
    self.rateBtn:setTitle(BanditsNPC.TF("UI_BN_Opt_Rate", "Arrivals: %1", self:rateName()))

    self.broadcastBtn:setTitle(st.welcome
        and BanditsNPC.T("UI_BN_Beacon_StopBroadcast", "Stop broadcasting")
        or BanditsNPC.T("UI_BN_Beacon_StartBroadcast", "Broadcast for survivors"))

    local stage = st.stage or 1
    if stage >= B.MAX_STAGE then
        self.upgradeBtn:setTitle(BanditsNPC.T("UI_BN_Beacon_FullyUpgraded", "Fully upgraded"))
        self.upgradeBtn:setEnable(false)
    else
        self.upgradeBtn:setTitle(BanditsNPC.TF("UI_BN_Beacon_UpgradeTo",
            "Upgrade to %1", B.StageName(stage + 1)))
        -- Left enabled when unaffordable on purpose: clicking says what is missing.
        self.upgradeBtn:setEnable(true)
    end

    -- The base is the only kind of area there is -- but since v0.77.13 drawing again ADDS
    -- to it instead of replacing it, so the button no longer says "Redraw". "Redraw base
    -- area" promised destruction it no longer performs, and the destructive option now
    -- lives only on Clear.
    local base = Zs.Base(site)
    self.baseBtn:setTitle(base and BanditsNPC.T("UI_BN_Zone_AddBase", "Add to base area")
                               or BanditsNPC.T("UI_BN_Zone_SetBase", "Mark out base area"))
    -- Also disabled at the work-area cap: Z.Set refuses past it (the list can only show
    -- The furniture scan runs HERE -- on a tab switch or a button click, not per
    -- frame -- because a cold cache walks the whole base area.
    self.spotCounts = nil
    if ar and base and BanditsNPC.Base then
        local spots = BanditsNPC.Base.Spots(site)
        if spots then
            local n = {}
            for _, k in ipairs(BanditsNPC.Base.KEYS) do n[k] = #(spots[k] or {}) end
            self.spotCounts = n
        end
    end

    -- Each row button remembers WHICH area it removes, not which slot it sits in:
    -- the list shifts up the moment one goes, so an index would remove the wrong one.
    local zones = (ar and site.zones) or {}
    self.rowZones = {}
    for i = 1, ROWS do
        local zn = zones[i]
        self.rowZones[i] = zn
        self.rowBtns[i]:setVisible(ar and zn ~= nil)
    end

    self.lessBtn:setEnable((st.wanted or 0) > 0)
    self.moreBtn:setEnable((st.wanted or 0) < (st.capacity or 0))
end

-- ===========================================================================
-- ACTIONS
-- ===========================================================================

function BanditsNPCBeaconWindow:onBroadcast()
    local st = BanditsNPC.Beacon.State(self.beacon)
    if not st then return end
    st.welcome = not st.welcome
    BanditsNPC.Beacon.State(self.beacon)
    self:refresh()
end

-- Debug: run the arrival pipeline now instead of waiting for the hourly roll.
--
-- Single player and a listen host have the wild-spawn module in the same Lua state, so
-- they call it directly AND GET A RESULT BACK -- which matters, because the honest
-- answers here include "nowhere loaded to put anyone" and "no survivor group to draw
-- from", and a debug button that fails silently is worse than none. A multiplayer client
-- has no server Lua, so it goes through the command handler and the answer is in the
-- server log; nothing else can be done from that side.
function BanditsNPCBeaconWindow:onDebugSpawn()
    local player = getSpecificPlayer(0)
    if not player then return end
    if BanditsNPC.WildSpawn and BanditsNPC.WildSpawn.Force then
        local ok, msg = BanditsNPC.WildSpawn.Force(player)
        self.dbgMsg = msg or (ok and BanditsNPC.T("UI_BN_Dbg_Sent", "Sent.") or nil)
        self.dbgOk = ok and true or false
    else
        sendClientCommand(player, 'BanditsNPC', 'ForceArrival', {})
        self.dbgMsg = BanditsNPC.T("UI_BN_Dbg_Sent", "Sent.")
        self.dbgOk = true
    end
    self.dbgMsgUntil = getTimestampMs() + 8000
    self:refresh()
end

function BanditsNPCBeaconWindow:onUpgrade()
    BanditsNPC.Beacon.DoUpgrade(self.beacon, getSpecificPlayer(0))
    self:layout()
    self:refresh()
end

local function bumpWanted(self, delta)
    local st = BanditsNPC.Beacon.State(self.beacon)
    if not st then return end
    st.wanted = math.max(0, math.min(st.capacity or 0, (st.wanted or 0) + delta))
    BanditsNPC.Beacon.State(self.beacon)
    self:refresh()
end

-- 1=Off 2=Rare 3=Occasional 4=Often -- the sandbox enum, cycled in place.
function BanditsNPCBeaconWindow:rateName()
    local r = BanditsNPC.Opt("WildSpawn", 2)
    if r == 1 then return BanditsNPC.T("UI_BN_Rate_Off", "Off") end
    if r == 3 then return BanditsNPC.T("UI_BN_Rate_Occasional", "Occasional") end
    if r == 4 then return BanditsNPC.T("UI_BN_Rate_Often", "Often") end
    return BanditsNPC.T("UI_BN_Rate_Rare", "Rare")
end

function BanditsNPCBeaconWindow:onCycleRate()
    if not self:mayEditWorldRules() then return end
    local r = BanditsNPC.Opt("WildSpawn", 2)
    r = (type(r) == "number") and r or 2
    r = (r % 4) + 1
    BanditsNPC.SetOpt("WildSpawn", r)
    self:refresh()
end

-- May this player change WORLD rules? Single player: always (there is nobody else).
-- Multiplayer: admins only, mirroring the server's own check -- see the note in
-- refresh(). This is a UI convenience, never the permission itself.
function BanditsNPCBeaconWindow:mayEditWorldRules()
    if not (isClient and isClient()) then return true end
    local ok, admin = pcall(function()
        return (isAdmin and isAdmin()) or (isDebugEnabled and isDebugEnabled())
    end)
    return (ok and admin) and true or false
end

function BanditsNPCBeaconWindow:onToggleOpt(button)
    if not self:mayEditWorldRules() then return end
    local row = self.optRows[button.internal]
    if not row then return end
    local cur = BanditsNPC.Opt(row.key, row.def) and true or false
    BanditsNPC.SetOpt(row.key, not cur)
    self:refresh()
end

function BanditsNPCBeaconWindow:onWantedLess() bumpWanted(self, -1) end
function BanditsNPCBeaconWindow:onWantedMore() bumpWanted(self, 1) end

function BanditsNPCBeaconWindow:onRemoveArea(button)
    local site = self:site()
    local zn = self.rowZones and self.rowZones[button.internal]
    if not (site and zn) then return end
    BanditsNPC.Zones.Remove(site, zn)
    BanditsNPC.Zones.ShowFor(3000)
    self:layout()
    self:refresh()
end

-- Marking out an area needs the world visible, so the window closes and the
-- cursor + its Finish bar take over.
function BanditsNPCBeaconWindow:beginZone(kind)
    local site = self:site()
    local player = getSpecificPlayer(0)
    if not (site and player and BanditsNPCZoneCursor) then return end
    local beacon = self.beacon
    self:onClose()
    BanditsNPCZoneCursor.Begin(player, site, kind, function()
        -- reopen where they left off once the area is committed
        if beacon then BanditsNPCBeaconWindow.OpenFor(beacon, "areas") end
    end)
end

-- Pick the container for one job's output, or for tools. Uses the same single-tile
-- cursor the Orders tab uses for bed/food spots, with the container validator, so a
function BanditsNPCBeaconWindow:onSetBase() self:beginZone(BanditsNPC.Zones.KIND.BASE) end
function BanditsNPCBeaconWindow:onShow() BanditsNPC.Zones.ShowFor(12000) end

function BanditsNPCBeaconWindow:onClear()
    local site = self:site()
    if not site then return end
    BanditsNPC.Zones.Clear(site, nil)
    BanditsNPC.Zones.ShowFor(3000)
    self:layout()
    self:refresh()
end

function BanditsNPCBeaconWindow:onDismantle()
    local beacon = self.beacon
    local text = BanditsNPC.T("UI_BN_Beacon_RemoveConfirm",
        "Take the beacon down? Everyone will follow you until you set up a new one.")
    local modal = ISModalDialog:new(0, 0, 340, 170, text, true, nil, function(_, button)
        if button.internal == "YES" then
            -- Close FIRST. The window drives an object that is about to stop
            -- existing; leaving it up left a panel writing to a dead registry entry.
            if BanditsNPCBeaconWindow.instance then BanditsNPCBeaconWindow.instance:onClose() end
            BanditsNPC.Beacon.Remove(beacon, getSpecificPlayer(0))
        end
    end)
    modal:initialise()
    modal:addToUIManager()
    modal:setAlwaysOnTop(true)
end

function BanditsNPCBeaconWindow:onClose()
    self:removeFromUIManager()
    if BanditsNPCBeaconWindow.instance == self then BanditsNPCBeaconWindow.instance = nil end
end

-- ===========================================================================
-- RENDER
-- ===========================================================================

function BanditsNPCBeaconWindow:render()
    ISCollapsableWindow.render(self)
    self:drawRect(PAD, self.bodyTop - 6, self.width - PAD * 2, 1, 0.6, C.rule[1], C.rule[2], C.rule[3])
    self:drawRect(PAD, self.footY - 10, self.width - PAD * 2, 1, 0.6, C.rule[1], C.rule[2], C.rule[3])
end

-- THE BROADCAST INDICATOR.
--
-- A mast with radio waves spreading from it, pulsing outward while the station is on
-- the air and sitting dark and still when it is silent. There is no image file behind
-- this: it is drawn from filled rectangles, arcs stepped as short segments, so the mod
-- ships no artwork and nothing can fail to load. It is the one place in the window
-- that tells you at a glance whether the beacon is doing anything.
local function drawBeaconWaves(panel, cx, cy, on)
    local t = ((getTimestampMs and getTimestampMs()) or 0) / 1000

    -- mast + base
    local mr, mg, mb = 0.55, 0.58, 0.62
    if on then mr, mg, mb = C.good[1], C.good[2], C.good[3] end
    panel:drawRect(cx - 1, cy - 12, 2, 20, 0.95, mr, mg, mb)
    panel:drawRect(cx - 7, cy + 8, 14, 2, 0.95, mr, mg, mb)
    panel:drawRect(cx - 3, cy - 15, 6, 3, 0.95, mr, mg, mb)

    if not on then
        -- one flat, dim arc so the widget still reads as an aerial rather than a stick
        for k = -4, 4 do
            local a = (k / 4) * 0.9
            panel:drawRect(cx + math.floor(math.sin(a) * 11),
                           cy - 10 - math.floor(math.cos(a) * 11), 2, 2, 0.18, 0.7, 0.7, 0.75)
        end
        return
    end

    -- three arcs, each fading in as it travels outward, offset in phase so they read
    -- as one wave leaving the mast rather than three blinking rings
    for ring = 0, 2 do
        local phase = (t * 0.9 + ring * 0.33) % 1
        local radius = 8 + phase * 15
        local alpha = 0.85 * (1 - phase) * (math.min(1, phase * 5))
        if alpha > 0.02 then
            for k = -5, 5 do
                local a = (k / 5) * 1.0
                panel:drawRect(cx + math.floor(math.sin(a) * radius),
                               cy - 10 - math.floor(math.cos(a) * radius),
                               2, 2, alpha, C.good[1], C.good[2], C.good[3])
            end
        end
    end
end

-- Capacity at a glance: one pip per slot the station supports. Filled = someone is
-- here, hollow = a slot you have opened and nobody has taken, dark = supported by the
-- station but switched off by you. Three numbers in one row of dots.
local function drawCapacityPips(panel, x, y, here, wanted, capacity)
    for i = 1, (capacity or 0) do
        local px = x + (i - 1) * 13
        if i <= here then
            panel:drawRect(px, y, 9, 9, 1, C.good[1], C.good[2], C.good[3])
        elseif i <= wanted then
            panel:drawRect(px, y, 9, 9, 1, 0.30, 0.32, 0.36)
            panel:drawRect(px + 1, y + 1, 7, 7, 1, 0.10, 0.10, 0.12)
        else
            panel:drawRect(px, y, 9, 9, 1, 0.18, 0.18, 0.20)
            panel:drawRect(px + 1, y + 1, 7, 7, 1, 0.10, 0.10, 0.12)
        end
    end
end

-- `site` is passed in rather than looked up: SiteFor re-derives the beacon's anchor by
-- scanning five squares, and this runs every frame.
function BanditsNPCBeaconWindow:renderOverview(p, st, site)
    local B = BanditsNPC.Beacon
    local y = 0
    local stage = st.stage or 1

    p:label(B.StageName(stage), PAD, y, C.head, UIFont.Medium)

    -- stage pips, right-aligned against the station name
    for i = 1, B.MAX_STAGE do
        local on = i <= stage
        p:drawRect(PAD + self.inner - 14 + (i - B.MAX_STAGE) * 16, y + 4 + p:getYScroll(), 12, 6, 1,
                   on and C.warn[1] or 0.22, on and C.warn[2] or 0.22, on and C.warn[3] or 0.25)
    end
    y = y + FONT_HGT_M + 6

    if st.welcome then
        p:label(BanditsNPC.T("UI_BN_Beacon_OnLine", "On the air - survivors may find you"),
                PAD, y, C.good)
    else
        p:label(BanditsNPC.T("UI_BN_Beacon_OffLine", "Silent - no one new will come"),
                PAD, y, C.warn)
    end
    y = y + LINE

    local here = (BanditsNPC.Persistence and BanditsNPC.Persistence.RosterCount
                  and BanditsNPC.Persistence.RosterCount()) or 0
    local want = st.wanted or 0
    local cap  = st.capacity or 0
    local full = here >= want

    local peopleText = BanditsNPC.TF("UI_BN_Beacon_People", "Companions: %1 of %2", here, want)
    p:label(peopleText, PAD, y, full and C.warn or C.text)
    local w = getTextManager():MeasureStringX(UIFont.Small, peopleText)
    drawCapacityPips(p, PAD + w + 12, y + 3 + p:getYScroll(), here, want, cap)
    y = y + LINE

    p:label(BanditsNPC.TF("UI_BN_Beacon_Capacity", "This station supports up to %1.", cap),
            PAD, y, C.dim)

    -- the aerial, in its own band above the broadcast button
    drawBeaconWaves(p, PAD + math.floor(self.broadcastBtn.width / 2),
                    self.ovIndicatorCY + p:getYScroll(), st.welcome and true or false)

    -- What is actually going to happen next, in words. This is the readout that was
    -- missing when a whole test session produced no arrivals and nothing anywhere
    -- said why: the answer was always knowable, it just was not shown.
    local ry = self.ovIndicatorY + 62 + BTN_H + LINE
    local line, col = B.ArrivalStatus(site, here)
    p:label(fit(line, UIFont.Small, self.inner), PAD, ry, col == "good" and C.good
            or (col == "warn" and C.warn or C.dim))

    -- What the debug button did, for a few seconds after it was pressed.
    if self.dbgMsg and self.dbgBtnY then
        if getTimestampMs() > (self.dbgMsgUntil or 0) then
            self.dbgMsg = nil
        else
            p:label(fit(self.dbgMsg, UIFont.Small, self.inner), PAD,
                    self.dbgBtnY + BTN_H + 4, self.dbgOk and C.good or C.warn)
        end
    end
end

function BanditsNPCBeaconWindow:renderAreas(p, site)
    local Zs = BanditsNPC.Zones
    local y = self.areaListY
    p:label(BanditsNPC.T("UI_BN_Zone_ListHead", "Marked out"), PAD, y - LINE, C.head)

    local zones = site.zones or {}
    if #zones == 0 then
        p:label(BanditsNPC.T("UI_BN_Zone_None",
            "Nothing marked out yet. Start with the base area."), PAD, y, C.dim)
        return
    end

    -- WHAT THE BASE SCAN FOUND -- computed in refresh(), never here. Base.Spots can
    -- walk three thousand tiles when its cache is cold, and doing that from a render
    -- frame is a stutter the player would blame on the mod.
    --
    -- Without this line the auto-spot feature is invisible: a companion sleeps in a
    -- bed nobody assigned and there is no way to tell whether that was the scan
    -- working or luck. It is also the fastest way to notice that the base area was
    -- drawn one tile short of the bedroom.
    if self.spotCounts then
        local n = self.spotCounts
        p:label(fit(BanditsNPC.TF("UI_BN_Base_Found",
            "Found inside: %1 bed(s), %2 seat(s), %3 TV(s), %4 food store(s), %5 washroom(s)",
            n.bed, n.chair, n.tv, n.food, n.bathroom), UIFont.Small, self.inner),
            PAD, y - LINE * 2, C.dim)
    end

    for i = 1, math.min(#zones, ROWS) do
        local zn = zones[i]
        local col = Zs.COLOR[zn.kind] or Zs.COLOR.base
        p:drawRect(PAD, y + 3 + p:getYScroll(), 10, 10, 0.95, col.r, col.g, col.b)
        local parts = zn.parts and #zn.parts or 0
        local tiles = Zs.TileCount(zn)
        local text
        if parts > 1 then
            text = BanditsNPC.TF("UI_BN_Zone_RowMulti", "%1  -  %2 tiles in %3 pieces",
                                 Zs.Label(site, zn), tiles, parts)
        else
            text = BanditsNPC.TF("UI_BN_Zone_Row", "%1  -  %2 tiles",
                                 Zs.Label(site, zn), tiles)
        end
        p:label(fit(text, UIFont.Small, self.inner - 18 - 92), PAD + 18, y)
        y = y + BTN_H + 3
    end
end

function BanditsNPCBeaconWindow:renderSettings(p, st)
    p:label(BanditsNPC.T("UI_BN_Set_Head", "How many companions do you want here?"),
            PAD, 0, C.head)

    -- the number sits between the - and + buttons
    local n = tostring(st.wanted or 0)
    local w = getTextManager():MeasureStringX(UIFont.Medium, n)
    p:drawText(n, PAD + 34 + (66 - w) / 2, self.wantedY + 2 + p:getYScroll(), 1, 1, 1, 1, UIFont.Medium)

    local y2 = self.wantedY + BTN_H + 6
    p:label(fit(BanditsNPC.TF("UI_BN_Set_CapNote",
        "The %1 supports up to %2. Set this lower and no one new arrives until you raise it.",
        BanditsNPC.Beacon.StageName(st.stage or 1), st.capacity or 0),
        UIFont.Small, self.inner), PAD, y2, C.dim)

    p:hline(self.optsY - LINE + 2)
    -- The heading already says these apply everywhere; when they are read-only the
    -- player needs to know WHY, or a dead toggle just reads as a broken button.
    local ruleHead = BanditsNPC.T("UI_BN_Set_WorldHead", "Companion rules (apply everywhere)")
    if not self:mayEditWorldRules() then
        ruleHead = ruleHead .. "  " .. BanditsNPC.T("UI_BN_Set_AdminOnly", "- admin only")
    end
    p:label(ruleHead, PAD, self.optsY - LINE + 8, C.head)

    -- Label to the RIGHT of its toggle, and truncated to what is left of the row --
    -- see the note at the top of the file.
    for i, row in ipairs(self.optRows) do
        local ry = self.optsY + (i - 1) * (BTN_H + 4)
        p:label(fit(row.label, UIFont.Small, self.inner - TOG_W - 12),
                PAD + TOG_W + 12, ry + 4, C.text)
    end
end

-- ===========================================================================
-- CONSTRUCTION
-- ===========================================================================

function BanditsNPCBeaconWindow:new(beacon, tab)
    local w, h = 620, 520
    local sw, sh = getCore():getScreenWidth(), getCore():getScreenHeight()
    w = math.min(w, sw - 40)
    h = math.min(h, sh - 40)
    local o = ISCollapsableWindow.new(self, (sw - w) / 2, (sh - h) / 2, w, h)
    o.beacon = beacon
    o.tab = tab or "overview"
    o.title = BanditsNPC.T("UI_BN_Beacon_Menu", "Survivors Beacon")
    o.resizable = true
    -- Floors, not guesses: the tab strip and the footer are fixed, so anything under
    -- this leaves no content area at all.
    o.minimumWidth = 460
    o.minimumHeight = 300
    return o
end

function BanditsNPCBeaconWindow.OpenFor(beacon, tab)
    if BanditsNPCBeaconWindow.instance then BanditsNPCBeaconWindow.instance:onClose() end
    local win = BanditsNPCBeaconWindow:new(beacon, tab)
    win:initialise()
    win:addToUIManager()
    BanditsNPCBeaconWindow.instance = win
    return win
end
