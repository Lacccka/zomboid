--
-- Bandits NPC - Companions Overhaul - Daily Schedule editor
--
-- Opened from the Orders tab. Lets the player set each of the 3 daily shifts to
-- Follow / Stay / Guard / Sleep, with tile-selected locations (Sleep uses her Bed
-- spot instead), and toggle the schedule on/off.
--

require "ISUI/ISCollapsableWindow"
require "ISUI/ISButton"

BanditsNPCScheduleWindow = ISCollapsableWindow:derive("BanditsNPCScheduleWindow")

local FONT_HGT = getTextManager():getFontHeight(UIFont.Small)
local PAD = 12
local BTN_H = FONT_HGT + 8
--
-- The activity list and the cycling both live on BanditsNPC.Schedule now (Schedule.TYPES /
-- Schedule.NextType): the Orders tab grew its own picker in v0.77.2 and a second copy of
-- the list here would drift the moment either gained an activity.

function BanditsNPCScheduleWindow:createChildren()
    ISCollapsableWindow.createChildren(self)
    local top = self:titleBarHeight() + PAD

    self.enableBtn = ISButton:new(PAD, top, 200, BTN_H, BanditsNPC.T("UI_BN_ScheduleOff", "Schedule: Off"), self, self.onToggle)
    self.enableBtn:initialise(); self.enableBtn:instantiate(); self:addChild(self.enableBtn)

    self.rowY = top + BTN_H + 16
    self.typeBtns, self.setBtns, self.testBtns = {}, {}, {}
    for i = 1, 3 do
        local y = self.rowY + (i - 1) * (BTN_H + 10)
        local tb = ISButton:new(190, y, 90, BTN_H, BanditsNPC.T("UI_BN_Type_follow", "Follow"), self, self.onCycleType)
        tb.internal = i
        tb:initialise(); tb:instantiate(); self:addChild(tb)
        self.typeBtns[i] = tb
        local sb = ISButton:new(288, y, 150, BTN_H, BanditsNPC.T("UI_BN_SetLocation", "Set location"), self, self.onSetLoc)
        sb.internal = i
        sb:initialise(); sb:instantiate(); self:addChild(sb)
        self.setBtns[i] = sb
        local xb = ISButton:new(444, y, 110, BTN_H, BanditsNPC.T("UI_BN_TestNow", "Test now"), self, self.onTestBlock)
        xb.internal = i
        xb:initialise(); xb:instantiate(); self:addChild(xb)
        self.testBtns[i] = xb
    end

    local closeBtn = ISButton:new(self.width - PAD - 80, self.height - PAD - BTN_H, 80, BTN_H, BanditsNPC.T("UI_BN_Close", "Close"), self, self.onClose)
    closeBtn:initialise(); closeBtn:instantiate(); self:addChild(closeBtn)

    self:refresh()
end

function BanditsNPCScheduleWindow:refresh()
    local brain = BanditBrain.Get(self.zombie)
    if not brain then self:onClose(); return end
    local sch = BanditsNPC.Schedule.Ensure(brain)
    self.enableBtn:setTitle(sch.enabled and BanditsNPC.T("UI_BN_ScheduleOn", "Schedule: On") or BanditsNPC.T("UI_BN_ScheduleOff", "Schedule: Off"))
    for i = 1, 3 do
        local b = sch.blocks[i] or {}
        local t = b.type or "follow"
        self.typeBtns[i]:setTitle(BanditsNPC.T("UI_BN_Type_" .. t, t:gsub("^%l", string.upper)))
        local set
        if t == "guard" then set = (b.a and b.b)
        elseif t == "stay" or t == "relax" then set = (b.pos ~= nil)
        else set = nil end
        if t == "follow" then
            self.setBtns[i]:setEnable(false); self.setBtns[i]:setTitle("--")
        elseif t == "sleep" then
            -- sleeps in her assigned Bed spot (Orders tab), no location to pick here
            self.setBtns[i]:setEnable(false)
            local hasBed = brain.spots and brain.spots.bed
            self.setBtns[i]:setTitle(hasBed and BanditsNPC.T("UI_BN_UsesBedSpot", "Uses their Bed spot") or BanditsNPC.T("UI_BN_NeedBedSpot", "! Set a Bed spot first"))
        else
            self.setBtns[i]:setEnable(true); self.setBtns[i]:setTitle(set and BanditsNPC.T("UI_BN_LocationSet", "Location set *") or BanditsNPC.T("UI_BN_SetLocation", "Set location"))
        end
    end
end

function BanditsNPCScheduleWindow:onToggle()
    local brain = BanditBrain.Get(self.zombie)
    local sch = brain and BanditsNPC.Schedule.Ensure(brain)
    BanditsNPC.Schedule.SetEnabled(self.zombie, not (sch and sch.enabled))
    self:refresh()
end

function BanditsNPCScheduleWindow:onCycleType(button)
    local brain = BanditBrain.Get(self.zombie)
    local sch = brain and BanditsNPC.Schedule.Ensure(brain)
    local cur = sch and sch.blocks[button.internal] and sch.blocks[button.internal].type or "follow"
    BanditsNPC.Schedule.SetType(self.zombie, button.internal, BanditsNPC.Schedule.NextType(cur))
    self:refresh()
end

function BanditsNPCScheduleWindow:onSetLoc(button)
    local idx = button.internal
    local brain = BanditBrain.Get(self.zombie)
    local sch = brain and BanditsNPC.Schedule.Ensure(brain)
    local t = sch and sch.blocks[idx] and sch.blocks[idx].type or "follow"
    if t == "follow" then return end
    local count = (t == "guard") and 2 or 1
    local z = self.zombie
    self:onClose()
    BanditsNPCTileCursor.Begin(getSpecificPlayer(0):getPlayerNum(), count, function(picks)
        BanditsNPC.Schedule.SetLocation(z, idx, picks)
    -- FIX(TASK-010): Schedule.SetLocation:141-146 files these as b.pos (-> stayPos,
    -- Schedule.lua:170) or b.a/b.b (-> guard), and all three become Move targets.
    -- A blocked pick livelocks her every time that shift comes round, which is
    -- worse than a one-off order: it repeats on the clock.
    end, BanditsNPC.Base and BanditsNPC.Base.STAND_OPTS)
end

-- Debug: force this shift's activity right now (no clock change) so you can watch
-- her switch to it. Closes the window so she's visible.
function BanditsNPCScheduleWindow:onTestBlock(button)
    local z = self.zombie
    local idx = button.internal
    self:onClose()
    BanditsNPC.Schedule.ApplyBlock(z, idx)
end

function BanditsNPCScheduleWindow:onClose()
    self:removeFromUIManager()
    if BanditsNPCScheduleWindow.instance == self then BanditsNPCScheduleWindow.instance = nil end
end

function BanditsNPCScheduleWindow:render()
    ISCollapsableWindow.render(self)
    for i = 1, 3 do
        local y = self.rowY + (i - 1) * (BTN_H + 10)
        self:drawText(BanditsNPC.Schedule.BlockLabel(i) or BanditsNPC.TF("UI_BN_Shift", "Shift %1", i), PAD, y + 4, 0.9, 0.9, 0.9, 1, UIFont.Small)
    end
end

function BanditsNPCScheduleWindow:new(zombie)
    local w, h = 580, 240
    local x = (getCore():getScreenWidth() - w) / 2
    local y = (getCore():getScreenHeight() - h) / 2
    local o = ISCollapsableWindow.new(self, x, y, w, h)
    o.zombie = zombie
    o.title = BanditsNPC.T("UI_BN_Title_Schedule", "Daily Schedule")
    o.resizable = false
    return o
end

function BanditsNPCScheduleWindow.OpenFor(zombie)
    if BanditsNPCScheduleWindow.instance then BanditsNPCScheduleWindow.instance:onClose() end
    local win = BanditsNPCScheduleWindow:new(zombie)
    win:initialise()
    win:addToUIManager()
    BanditsNPCScheduleWindow.instance = win
    return win
end
