--
-- True Companions - Appearance Workstation window
--
-- Rename a companion and change her hair, beard and hair colour. Opened by
-- right-clicking the vanity, or standing at it and pressing the interact key.
--
-- ONLY COMPANIONS STANDING AT THE STATION CAN BE RESTYLED. That is not an arbitrary
-- restriction -- it is what makes the object a workstation rather than a menu. Bring
-- her here (the roster's Follow, or the Unstuck button) and she appears in the list.
--
-- EVERY CHANGE APPLIES IMMEDIATELY to the live companion, because the whole point is
-- to look at her while you choose. There is no OK button to forget to press and
-- nothing to roll back: BanditsNPC.Appearance.Set stores on the brain AND pushes to
-- the body in one call, so the two cannot drift apart.
--

require "ISUI/ISCollapsableWindow"
require "ISUI/ISButton"
require "ISUI/ISTextEntryBox"

BanditsNPCAppearanceWindow = ISCollapsableWindow:derive("BanditsNPCAppearanceWindow")

local FONT_HGT   = getTextManager():getFontHeight(UIFont.Small)
local FONT_HGT_M = getTextManager():getFontHeight(UIFont.Medium)
local PAD   = 14
local BTN_H = FONT_HGT + 10
local LINE  = FONT_HGT + 6
local RANGE = 4          -- tiles from the station a companion must be within

local C = {
    text = { 0.88, 0.88, 0.90 },
    dim  = { 0.62, 0.62, 0.66 },
    head = { 1.00, 1.00, 1.00 },
    rule = { 0.32, 0.32, 0.38 },
}

-- Companions close enough to the station to be worked on.
local function companionsAt(station)
    local out = {}
    pcall(function()
        local sq = station:getSquare()
        if not sq then return end
        local sx, sy, sz = sq:getX(), sq:getY(), sq:getZ()
        local zl = getCell():getZombieList()
        for i = 0, zl:size() - 1 do
            local z = zl:get(i)
            if z and z:getVariableBoolean("Bandit") then
                local b = BanditBrain.Get(z)
                -- OWN COMPANIONS ONLY. Filtering the LIST is the same shape RosterPanel
                -- uses: every control in this window then acts on something already
                -- checked, instead of each one needing its own guard. Appearance.Set
                -- refuses anyway, but a picker offering someone else's companion and
                -- then silently doing nothing is worse than not listing her.
                if b and b.recruited and BanditsNPC.IsMine and BanditsNPC.IsMine(b)
                   and math.abs(z:getZ() - sz) < 1
                   and BanditUtils.DistTo(z:getX(), z:getY(), sx + 0.5, sy + 0.5) <= RANGE then
                    out[#out + 1] = { zombie = z, brain = b }
                end
            end
        end
    end)
    table.sort(out, function(a, b)
        return tostring(a.brain.fullname or "") < tostring(b.brain.fullname or "")
    end)
    return out
end

-- index of `value` in `list`, or 0
local function indexOf(list, value)
    for i = 1, #list do if list[i] == value then return i end end
    return 0
end

function BanditsNPCAppearanceWindow:createChildren()
    ISCollapsableWindow.createChildren(self)
    local top = self:titleBarHeight() + PAD
    local right = self.width - PAD

    -- who
    self.prevWhoBtn = ISButton:new(PAD, top, 30, BTN_H, "<", self, self.onPrevWho)
    self.nextWhoBtn = ISButton:new(PAD + 34, top, 30, BTN_H, ">", self, self.onNextWho)
    for _, b in ipairs({ self.prevWhoBtn, self.nextWhoBtn }) do
        b:initialise(); b:instantiate(); self:addChild(b)
    end
    self.whoY = top

    -- name
    local ny = top + BTN_H + LINE + 4
    self.nameEntry = ISTextEntryBox:new("", PAD, ny + FONT_HGT + 2, self.width - PAD * 2 - 90, BTN_H)
    self.nameEntry:initialise(); self.nameEntry:instantiate()
    self.nameEntry.onCommandEntered = function() self:onRename() end
    self:addChild(self.nameEntry)
    self.renameBtn = ISButton:new(right - 84, ny + FONT_HGT + 2, 84, BTN_H,
        BanditsNPC.T("UI_BN_App_Rename", "Rename"), self, self.onRename)
    self.renameBtn:initialise(); self.renameBtn:instantiate(); self:addChild(self.renameBtn)
    self.nameY = ny

    -- hair style
    local hy = ny + FONT_HGT + 2 + BTN_H + LINE
    self.hairY = hy
    self.prevHairBtn = ISButton:new(PAD, hy + FONT_HGT + 2, 30, BTN_H, "<", self, self.onPrevHair)
    self.nextHairBtn = ISButton:new(PAD + 34, hy + FONT_HGT + 2, 30, BTN_H, ">", self, self.onNextHair)
    for _, b in ipairs({ self.prevHairBtn, self.nextHairBtn }) do
        b:initialise(); b:instantiate(); self:addChild(b)
    end

    -- beard
    local by = hy + FONT_HGT + 2 + BTN_H + LINE
    self.beardY = by
    self.prevBeardBtn = ISButton:new(PAD, by + FONT_HGT + 2, 30, BTN_H, "<", self, self.onPrevBeard)
    self.nextBeardBtn = ISButton:new(PAD + 34, by + FONT_HGT + 2, 30, BTN_H, ">", self, self.onNextBeard)
    for _, b in ipairs({ self.prevBeardBtn, self.nextBeardBtn }) do
        b:initialise(); b:instantiate(); self:addChild(b)
    end

    -- colour swatches
    self.colorY = by + FONT_HGT + 2 + BTN_H + LINE
    self.swatchBtns = {}
    local per = 8
    local sw = math.floor((self.width - PAD * 2 - (per - 1) * 4) / per)
    for i, col in ipairs(BanditsNPC.Appearance.COLORS) do
        local row = math.floor((i - 1) / per)
        local cx = PAD + ((i - 1) % per) * (sw + 4)
        local b = ISButton:new(cx, self.colorY + FONT_HGT + 2 + row * (BTN_H + 4), sw, BTN_H, "", self, self.onColor)
        b.internal = i
        b.backgroundColor = { r = col.r, g = col.g, b = col.b, a = 1 }
        b.backgroundColorMouseOver = { r = col.r, g = col.g, b = col.b, a = 1 }
        b:initialise(); b:instantiate(); self:addChild(b)
        self.swatchBtns[i] = b
    end

    self.closeBtn = ISButton:new(right - 100, self.height - PAD - BTN_H, 100, BTN_H,
        BanditsNPC.T("UI_BN_Close", "Close"), self, self.onClose)
    self.closeBtn:initialise(); self.closeBtn:instantiate(); self:addChild(self.closeBtn)

    self:refresh()
end

function BanditsNPCAppearanceWindow:current()
    return self.list and self.list[self.who or 1]
end

function BanditsNPCAppearanceWindow:refresh()
    self.list = companionsAt(self.station)
    if #self.list == 0 then self.who = nil
    else self.who = math.max(1, math.min(#self.list, self.who or 1)) end

    local e = self:current()
    local has = e ~= nil
    for _, b in ipairs({ self.prevWhoBtn, self.nextWhoBtn }) do b:setEnable(#self.list > 1) end
    for _, b in ipairs({ self.renameBtn, self.prevHairBtn, self.nextHairBtn }) do b:setEnable(has) end
    self.nameEntry:setEditable(has)
    for _, b in ipairs(self.swatchBtns) do b:setEnable(has) end

    -- No bearded ladies, exactly as the character creator has it.
    local male = has and not e.brain.female
    self.prevBeardBtn:setEnable(male and true or false)
    self.nextBeardBtn:setEnable(male and true or false)

    -- FIX(BUG-003): SYNC THE BOX TO THE BRAIN WHEN THE COMPANION CHANGES, NEVER
    -- BECAUSE THE TEXT DIFFERS. THIS IS THE RENAME BUTTON BUG.
    --
    -- `render` calls `refresh` EVERY FRAME (:285), and this used to re-set the box
    -- whenever its text differed from the brain, guarded only by `not isFocused()`.
    -- That guard asks "does this box have keyboard focus" in order to answer "is the
    -- player still editing it", and the two part company at exactly the wrong moment:
    --
    --   frame N   -- mouse DOWN on Rename; the engine moves focus off the text box
    --   frame N   -- render -> refresh -> isFocused() is now FALSE -> BOX IS RESET
    --                to her current name, discarding what was typed
    --   frame N+1 -- ISButton fires on the mouse UP; onRename reads the box and gets
    --                the name she already had, so the rename is a no-op
    --
    -- The Enter key never hit this because `onCommandEntered` fires while the box IS
    -- still focused, so the guard held. **One entry point worked and the other did
    -- not, and the difference was entirely this line** -- confirmed in game 2026-08-25:
    -- the box visibly snapped back to her current name at click time, and the chat
    -- feedback floated that same old name, which is what `getInternalText()` returned.
    --
    -- Keying on the BRAIN instead of on the box removes the race rather than retiming
    -- it. The box is re-filled when the thing it displays changes -- a different
    -- companion selected, or her name changed from somewhere else -- and never because
    -- of what is in the box. `brain.id` is Bandits-owned and stable for a live
    -- companion (refs/bandits/.../server/BanditServerSpawner.lua:301).
    --
    -- The name is tracked as well as the id so the one thing the old line did correctly
    -- is kept: a rename from another source (another player's client, the console) still
    -- repaints the field. That is why this is not simply an id check.
    --
    -- DO NOT REINTRODUCE A COMPARISON AGAINST `getInternalText()` HERE. "The box says
    -- something other than her name" is the NORMAL state while somebody is typing, and
    -- any rule that repaints on it is fighting the player for the field. The bug was
    -- never the timing of that comparison; it was making it at all.
    if has then
        local key, nm = e.brain.id, e.brain.fullname or ""
        if self._nameFor ~= key or self._nameWas ~= nm then
            self._nameFor, self._nameWas = key, nm
            self.nameEntry:setText(nm)
        end
    else
        -- She walked out of range. Forget the binding so returning re-fills the box
        -- rather than leaving whatever was in it when she left.
        self._nameFor, self._nameWas = nil, nil
    end
end

-- ---------------------------------------------------------------------------
-- actions
-- ---------------------------------------------------------------------------

function BanditsNPCAppearanceWindow:onPrevWho()
    if not self.list or #self.list == 0 then return end
    self.who = ((self.who or 1) - 2) % #self.list + 1
    self.nameEntry:setText(self:current().brain.fullname or "")
    self:refresh()
end

function BanditsNPCAppearanceWindow:onNextWho()
    if not self.list or #self.list == 0 then return end
    self.who = (self.who or 1) % #self.list + 1
    self.nameEntry:setText(self:current().brain.fullname or "")
    self:refresh()
end

function BanditsNPCAppearanceWindow:onRename()
    local e = self:current()
    if not e then return end
    local name = self.nameEntry:getInternalText()
    if not name then return end
    name = name:gsub("^%s*(.-)%s*$", "%1")
    -- An empty name would leave her unnameable everywhere else in the UI, which is a
    -- worse outcome than refusing the edit.
    if name == "" then
        self.nameEntry:setText(e.brain.fullname or "")
        return
    end
    if #name > 32 then name = name:sub(1, 32) end
    -- FIX(BUG-003): REPORT WHAT ACTUALLY HAPPENED. `Appearance.Set` returns false when
    -- `MayCommand` refuses (BanditsNPCAppearance.lua:379) and this floated the new name
    -- over her head regardless, so a refused rename LOOKED like a successful one. That
    -- is worse than a silent failure -- it is the reason "renaming does nothing" was
    -- reported as one bug when the write path had been sound all along, and it would
    -- have masked this button bug for anyone reading the chat line as confirmation.
    if BanditsNPC.Appearance.Set(e.zombie, { fullname = name }) then
        pcall(function() e.zombie:addLineChatElement(name, 0.85, 0.95, 0.8) end)
    else
        -- Same signal the empty-name reject gives four lines above: the box snaps back
        -- to what she is actually called, so a refusal is visible instead of silent.
        self.nameEntry:setText(e.brain.fullname or "")
    end
    self:refresh()
end

local function cycle(self, list, currentId, delta, field)
    local e = self:current()
    if not e then return end
    local i = indexOf(list, currentId)
    if i == 0 then i = 1 end
    i = ((i - 1 + delta) % #list) + 1
    BanditsNPC.Appearance.Set(e.zombie, { [field] = list[i] })
    self:refresh()
end

function BanditsNPCAppearanceWindow:hairList()
    local e = self:current()
    return BanditsNPC.Appearance.HairStyles(e and e.brain.female or false)
end

function BanditsNPCAppearanceWindow:onPrevHair()
    local e = self:current(); if not e then return end
    cycle(self, self:hairList(), e.brain.hairStyle or self:currentHairOnBody(), -1, "hairStyle")
end

function BanditsNPCAppearanceWindow:onNextHair()
    local e = self:current(); if not e then return end
    cycle(self, self:hairList(), e.brain.hairStyle or self:currentHairOnBody(), 1, "hairStyle")
end

-- A companion who has never been restyled has no brain.hairStyle -- her look came from
-- her Creator profile. Read it off the body so the first click steps from what you can
-- see rather than jumping to the top of the list.
function BanditsNPCAppearanceWindow:currentHairOnBody()
    local e = self:current()
    local id
    pcall(function() id = e.zombie:getHumanVisual():getHairModel() end)
    return id or ""
end

function BanditsNPCAppearanceWindow:currentBeardOnBody()
    local e = self:current()
    local id
    pcall(function() id = e.zombie:getHumanVisual():getBeardModel() end)
    return id or ""
end

function BanditsNPCAppearanceWindow:onPrevBeard()
    local e = self:current(); if not e then return end
    cycle(self, BanditsNPC.Appearance.BeardStyles(),
          e.brain.beardStyle or self:currentBeardOnBody(), -1, "beardStyle")
end

function BanditsNPCAppearanceWindow:onNextBeard()
    local e = self:current(); if not e then return end
    cycle(self, BanditsNPC.Appearance.BeardStyles(),
          e.brain.beardStyle or self:currentBeardOnBody(), 1, "beardStyle")
end

function BanditsNPCAppearanceWindow:onColor(button)
    local e = self:current()
    local col = BanditsNPC.Appearance.COLORS[button.internal]
    if not (e and col) then return end
    -- tcHairColor, NOT hairColor: Bandits' field of that name holds a palette INDEX for
    -- every clan-spawned NPC, and a table in it crashes their own visual code.
    local c = { r = col.r, g = col.g, b = col.b }
    BanditsNPC.Appearance.Set(e.zombie, { tcHairColor = c, tcBeardColor = c })
    self:refresh()
end

function BanditsNPCAppearanceWindow:onClose()
    self:removeFromUIManager()
    if BanditsNPCAppearanceWindow.instance == self then BanditsNPCAppearanceWindow.instance = nil end
end

-- ---------------------------------------------------------------------------
-- render
-- ---------------------------------------------------------------------------

function BanditsNPCAppearanceWindow:label(text, x, y, col, font)
    col = col or C.text
    self:drawText(text, x, y, col[1], col[2], col[3], 1, font or UIFont.Small)
end

function BanditsNPCAppearanceWindow:render()
    ISCollapsableWindow.render(self)

    -- The station can be destroyed while this is open.
    if not (self.station and BanditsNPC.Appearance.IsStation(self.station)) then self:onClose(); return end
    self:refresh()

    local e = self:current()

    self:label(BanditsNPC.T("UI_BN_App_Who", "Working on"), PAD + 70, self.whoY - FONT_HGT - 2, C.dim)
    if e then
        self:label(e.brain.fullname or BanditsNPC.T("UI_BN_Survivor", "Survivor"),
                   PAD + 70, self.whoY + 3, C.head, UIFont.Medium)
        self:label(BanditsNPC.TF("UI_BN_App_Count", "%1 of %2", self.who, #self.list),
                   self.width - PAD - 60, self.whoY + 5, C.dim)
    else
        self:label(BanditsNPC.T("UI_BN_App_Nobody",
            "Nobody here. Bring a companion to the mirror."), PAD + 70, self.whoY + 5, C.dim)
        return
    end

    self:label(BanditsNPC.T("UI_BN_App_Name", "Name"), PAD, self.nameY, C.dim)

    self:label(BanditsNPC.T("UI_BN_App_Hair", "Hair"), PAD, self.hairY, C.dim)
    self:label(BanditsNPC.Appearance.HairLabel(e.brain.hairStyle or self:currentHairOnBody()),
               PAD + 72, self.hairY + FONT_HGT + 2 + math.floor((BTN_H - FONT_HGT) / 2), C.text)

    self:label(BanditsNPC.T("UI_BN_App_Beard", "Beard"), PAD, self.beardY, C.dim)
    if e.brain.female then
        self:label(BanditsNPC.T("UI_BN_App_NoBeard", "-"),
                   PAD + 72, self.beardY + FONT_HGT + 2 + math.floor((BTN_H - FONT_HGT) / 2), C.dim)
    else
        self:label(BanditsNPC.Appearance.BeardLabel(e.brain.beardStyle or self:currentBeardOnBody()),
                   PAD + 72, self.beardY + FONT_HGT + 2 + math.floor((BTN_H - FONT_HGT) / 2), C.text)
    end

    self:label(BanditsNPC.T("UI_BN_App_Color", "Hair colour"), PAD, self.colorY, C.dim)
end

-- ---------------------------------------------------------------------------
-- construction
-- ---------------------------------------------------------------------------

function BanditsNPCAppearanceWindow:new(station)
    local w, h = 480, 400
    local sw, sh = getCore():getScreenWidth(), getCore():getScreenHeight()
    local o = ISCollapsableWindow.new(self, (sw - w) / 2, (sh - h) / 2, w, h)
    o.station = station
    o.who = 1
    o.title = BanditsNPC.T("UI_BN_App_Title", "Appearance Workstation")
    o.resizable = false
    return o
end

function BanditsNPCAppearanceWindow.OpenFor(station)
    if BanditsNPCAppearanceWindow.instance then BanditsNPCAppearanceWindow.instance:onClose() end
    local win = BanditsNPCAppearanceWindow:new(station)
    win:initialise()
    win:addToUIManager()
    BanditsNPCAppearanceWindow.instance = win
    return win
end
