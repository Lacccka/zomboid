--
-- Bandits NPC - Companions Overhaul - Dialogue overlay (Fallout-4 style)
--
-- A frameless, semi-transparent overlay (no window chrome) at the bottom of the
-- screen. Shows the NPC's line and a list of options for the CURRENT level of a
-- nested dialogue tree: picking a category descends into its questions; the last
-- option is "Back" (or "Leave" at the top level).
--
-- Each option is drawn full-width with brackets at the far edges and the text
-- centred. Colouring:
--   white       = available (clickable, full effect)
--   blue-grey   = already asked / on cooldown (still clickable: re-reads)
--   dark + amber [requirement] = locked by a stat/trait/affinity/time check
--                 (NOT clickable; the amber tag tells you what unlocks it)
--

require "ISUI/ISPanel"

BanditsNPCTalkWindow = ISPanel:derive("BanditsNPCTalkWindow")

local FONT_L = getTextManager():getFontHeight(UIFont.Large)
local FONT_M = getTextManager():getFontHeight(UIFont.Medium)
local PAD = 18
local ROW_H = FONT_M + 12

local function pid()
    return BanditUtils.GetCharacterID(getSpecificPlayer(0))
end

-- greedy word-wrap helpers -------------------------------------------------
-- Third copy of the same greedy fill, now delegating (v0.75.53). Argument order differs
-- from BanditsNPC.WrapLines and is kept, because every call site in this file uses it.
--
-- One real difference is preserved by the shared version: this guarded `if line ~= ""`
-- before inserting, so a single word LONGER than maxW does not emit a leading blank line.
-- WrapLines has the same guard.
local function wrapLines(text, maxW, font)
    return BanditsNPC.WrapLines(text, font, maxW)
end

function BanditsNPCTalkWindow:createChildren()
    if BanditsNPC.Talk and BanditsNPC.Talk.EnsureMet then BanditsNPC.Talk.EnsureMet(self.zombie) end
    self.brain = BanditBrain.Get(self.zombie)
    self.navStack = {}
    local recruited = self.brain and self.brain.recruited and self.brain.master == pid()
    self.lastLine = recruited and BanditsNPC.T("UI_BN_Talk_Greet", "\"What do you need?\"") or BanditsNPC.T("UI_BN_Talk_GreetStranger", "\"...what do you want?\"")
    self:rebuild()
end

function BanditsNPCTalkWindow:currentParent()
    return self.navStack[#self.navStack]
end

-- Build the rows for the current level and resize the panel to fit (bottom-anchored).
function BanditsNPCTalkWindow:rebuild()
    self.brain = BanditBrain.Get(self.zombie)
    self.rows = {}
    if self.brain and BanditsNPC.Talk then
        for _, t in ipairs(BanditsNPC.Talk.GetChildren(self:currentParent())) do
            local st, reason = BanditsNPC.Talk.GetState(self.brain, t)
            if st ~= "hidden" then
                table.insert(self.rows, {
                    topic = t, state = st, reason = (st == "locked") and reason or nil,
                    label = t.text or t.id or "?", clickable = (st ~= "locked"),
                })
            end
        end
    end
    table.insert(self.rows, {
        nav = true, clickable = true, state = "nav",
        label = (#self.navStack > 0) and BanditsNPC.T("UI_BN_Talk_Back", "Back") or BanditsNPC.T("UI_BN_Talk_Leave", "Leave"),
    })

    -- vertical layout: portrait (left, if enabled) + name/response (right), then options
    local portraitOn = BanditsNPC.Portrait and BanditsNPC.Portrait.Enabled()
    self.portraitW = portraitOn and 96 or 0
    self.contentLeft = portraitOn and (PAD + self.portraitW + 14) or PAD
    self.contentW = self.width - self.contentLeft - PAD
    self.contentCx = self.contentLeft + self.contentW / 2

    local maxW = self.contentW
    local n = 0
    if self.playerLine then n = n + #wrapLines(BanditsNPC.TF("UI_BN_Talk_You", "You: %1", self.playerLine), maxW, UIFont.Medium) end
    if self.lastLine   then n = n + #wrapLines(self.lastLine, maxW, UIFont.Medium) end
    local storyN = self.storyLine and #wrapLines(self.storyLine, maxW, UIFont.Medium) or 0
    self.optY = PAD + FONT_L + 10 + n * (FONT_M + 2) + (storyN > 0 and (storyN * (FONT_M + 2) + 8) or 0) + 12
    if portraitOn then self.optY = math.max(self.optY, PAD + self.portraitW + 20) end

    local h = self.optY + #self.rows * ROW_H + PAD
    self:setHeight(h)
    -- Pin the TOP once (from the first layout) so the window -- and the portrait pinned to it
    -- -- doesn't jump every time the content resizes when you pick a dialogue option. Content
    -- grows/shrinks downward from this fixed top instead.
    if not self.anchorY then self.anchorY = getCore():getScreenHeight() - h - 36 end
    -- ...but never off the screen (user reports, 13 Jul): a category with more rows
    -- than the first level grows PAST the bottom edge and the extra options were
    -- unreachable on smaller monitors. Keep the pinned top while everything fits;
    -- otherwise slide up just enough that the bottom row stays on screen.
    local sh = getCore():getScreenHeight()
    local y = self.anchorY
    if y + h > sh - 8 then y = sh - 8 - h end
    if y < 8 then y = 8 end
    self:setY(y)
end

-- CHOOSING A LINE, once, for both input methods (v0.76.2). This was inline in
-- onMouseDown; the controller needs exactly the same behaviour, and two copies of a
-- conversation state machine would drift the first time one of them was edited.
function BanditsNPCTalkWindow:activateRow(i)
    local row = self.rows and self.rows[i]
    if not (row and row.clickable) then return end
    if row.nav then
        if #self.navStack > 0 then
            table.remove(self.navStack)
            self.playerLine, self.lastLine, self.storyLine = nil, nil, nil
            self:rebuild()
        else
            self:close()
        end
        return
    end
    local t = row.topic
    if t.isCategory then
        table.insert(self.navStack, t.id)
        self.playerLine, self.lastLine, self.storyLine = nil, nil, nil
        self:rebuild()
        return
    end
    -- a question: Do() rewards only when fresh, otherwise re-reads
    local resp, fresh, revealed = BanditsNPC.Talk.Do(self.zombie, t)
    self.playerLine = t.text
    self.lastLine = resp
    self.storyLine = (fresh and revealed) and revealed or nil
    self:rebuild()
end

function BanditsNPCTalkWindow:onMouseDown(x, y)
    if not self.rows then return true end
    for i, _ in ipairs(self.rows) do
        local ry = self.optY + (i - 1) * ROW_H
        if y >= ry and y < ry + ROW_H then
            self:activateRow(i)
            return true
        end
    end
    return true
end

-- The dialogue lines a controller may move between. Locked topics are shown but not
-- offered, exactly as the mouse treats them -- the pad skips straight past them rather
-- than letting the player select something that will not respond.
function BanditsNPCTalkWindow:getJoypadTargets()
    local out = {}
    for i, row in ipairs(self.rows or {}) do
        if row.clickable then
            out[#out + 1] = { x = 0, y = self.optY + (i - 1) * ROW_H,
                              w = self.width, h = ROW_H,
                              go = function() self:activateRow(i) end }
        end
    end
    return out
end

function BanditsNPCTalkWindow:onMouseDownOutside() self:close() end
function BanditsNPCTalkWindow:onRightMouseDown() self:close() end

function BanditsNPCTalkWindow:close()
    -- Controller first: a conversation that ends while holding joypad focus would take
    -- the pad away from the game.
    if BanditsNPC.Joypad then BanditsNPC.Joypad.Release() end
    if BanditsNPC.Portrait then BanditsNPC.Portrait.Hide() end
    self:removeFromUIManager()
    if BanditsNPCTalkWindow.instance == self then BanditsNPCTalkWindow.instance = nil end
end

-- draw word-wrapped text horizontally centred; returns the y below it
function BanditsNPCTalkWindow:drawWrapCentre(text, y, font, r, g, b)
    local cx = self.contentCx or (self.width / 2)
    local maxW = self.contentW or (self.width - PAD * 2)
    local fh = getTextManager():getFontHeight(font)
    for _, ln in ipairs(wrapLines(text, maxW, font)) do
        self:drawTextCentre(ln, cx, y, r, g, b, 1, font)
        y = y + fh + 2
    end
    return y
end

function BanditsNPCTalkWindow:render()
    local w, h = self.width, self.height
    local pw = (BanditsNPC.Portrait and (self.portraitW or 0) > 0) and self.portraitW or 0
    if pw > 0 then
        -- The 3D portrait renders BEHIND 2D rects, so a full-window dark panel would darken
        -- it (the "behind and darkened" report). Draw the dark background with a hole cut out
        -- around the portrait rect instead, leaving it clear.
        local ph = (self.optY or 120) - PAD - 8
        local px2, py2 = PAD + pw, PAD + ph
        self:drawRect(0, 0, w, PAD, 0.8, 0.07, 0.07, 0.07)              -- above portrait
        self:drawRect(0, py2, w, h - py2, 0.8, 0.07, 0.07, 0.07)        -- below portrait
        self:drawRect(0, PAD, PAD, ph, 0.8, 0.07, 0.07, 0.07)           -- left of portrait
        self:drawRect(px2, PAD, w - px2, ph, 0.8, 0.07, 0.07, 0.07)     -- right of portrait
    else
        self:drawRect(0, 0, w, h, 0.8, 0.07, 0.07, 0.07)
    end
    self:drawRectBorder(0, 0, w, h, 0.5, 0.3, 0.3, 0.3)

    local brain = self.brain
    if pw > 0 then
        BanditsNPC.Portrait.Show(self, self.zombie, PAD, PAD, pw, (self.optY or 120) - PAD - 8)
    end
    self:drawText((brain and brain.fullname) or BanditsNPC.T("UI_BN_Survivor", "Survivor"), self.contentLeft or PAD, PAD - 2, 0.95, 0.9, 0.7, 1, UIFont.Large)

    local y = PAD + FONT_L + 8
    if self.playerLine then
        y = self:drawWrapCentre(BanditsNPC.TF("UI_BN_Talk_You", "You: %1", self.playerLine), y, UIFont.Medium, 0.6, 0.78, 0.6)
    end
    if self.lastLine then
        y = self:drawWrapCentre(self.lastLine, y, UIFont.Medium, 0.95, 0.95, 0.95)
    end
    if self.storyLine then
        y = self:drawWrapCentre(self.storyLine, y + 6, UIFont.Medium, 0.78, 0.72, 0.55)
    end

    -- which row is the mouse over?
    local overRow = nil
    if self:isMouseOver() then
        local my = self:getMouseY()
        if my >= self.optY then
            local idx = math.floor((my - self.optY) / ROW_H) + 1
            if idx >= 1 and idx <= #(self.rows or {}) then overRow = idx end
        end
    end

    local tm = getTextManager()
    local cx = self.width / 2
    for i, row in ipairs(self.rows or {}) do
        local ry = self.optY + (i - 1) * ROW_H
        local ty = ry + 5
        local hovered = (i == overRow) and row.clickable

        if hovered then
            self:drawRect(PAD - 6, ry, self.width - (PAD - 6) * 2, ROW_H, 0.35, 0.35, 0.35, 0.5)
        end

        -- label colour by state
        local lr, lg, lb
        if row.nav then            lr, lg, lb = 0.95, 0.85, 0.45
        elseif row.state == "available" then lr, lg, lb = 0.93, 0.93, 0.93
        elseif row.state == "asked" then     lr, lg, lb = 0.50, 0.56, 0.62
        else                       lr, lg, lb = 0.40, 0.40, 0.42 end
        if hovered then lr, lg, lb = math.min(1, lr + 0.12), math.min(1, lg + 0.12), math.min(1, lb + 0.08) end

        -- centred content: optional [requirement] (amber) + label
        local reqSeg = row.reason and ("[" .. row.reason .. "]  ") or ""
        local wReq = reqSeg ~= "" and tm:MeasureStringX(UIFont.Medium, reqSeg) or 0
        local wLbl = tm:MeasureStringX(UIFont.Medium, row.label)
        local sx = cx - (wReq + wLbl) / 2
        if reqSeg ~= "" then
            self:drawText(reqSeg, sx, ty, 0.88, 0.62, 0.25, 1, UIFont.Medium)   -- amber requirement
        end
        self:drawText(row.label, sx + wReq, ty, lr, lg, lb, 1, UIFont.Medium)

        -- full-width brackets at the far edges
        local br, bg, bb = 0.5, 0.5, 0.55
        if hovered then br, bg, bb = 0.85, 0.8, 0.45 end
        self:drawText("[", PAD, ty, br, bg, bb, 1, UIFont.Medium)
        local rb = tm:MeasureStringX(UIFont.Medium, "]")
        self:drawText("]", self.width - PAD - rb, ty, br, bg, bb, 1, UIFont.Medium)
    end

    if BanditsNPC.Joypad then BanditsNPC.Joypad.DrawSelection(self) end
end

function BanditsNPCTalkWindow:new(zombie)
    -- fit narrow screens: the fixed 820 hung off both edges below ~860px wide
    local w = math.min(820, getCore():getScreenWidth() - 40)
    local x = math.floor((getCore():getScreenWidth() - w) / 2)
    local o = ISPanel:new(x, 0, w, 360)
    setmetatable(o, self)
    self.__index = self
    o.zombie = zombie
    o.moveWithMouse = false
    o.backgroundColor = {r=0, g=0, b=0, a=0}
    o.borderColor = {r=0, g=0, b=0, a=0}
    return o
end

function BanditsNPCTalkWindow.OpenFor(zombie)
    if BanditsNPCTalkWindow.instance then BanditsNPCTalkWindow.instance:close() end
    local win = BanditsNPCTalkWindow:new(zombie)
    win:initialise()
    win:addToUIManager()
    BanditsNPCTalkWindow.instance = win

    -- CONTROLLER (v0.76.2). A conversation is the one place a player CANNOT fall back on
    -- the mouse without leaving the pad down, so this is the window that most needed it.
    -- Its rows are published through getJoypadTargets above; locked lines are skipped.
    pcall(function()
        local Jp = BanditsNPC.Joypad
        if not Jp then return end
        Jp.Install(BanditsNPCTalkWindow)
        if Jp.Present(0) then
            win.joySel = 1
            Jp.Grab(win, 0)
        end
    end)
    return win
end
