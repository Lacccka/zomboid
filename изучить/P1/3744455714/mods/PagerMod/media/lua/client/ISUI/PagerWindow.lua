-- ============================================================
-- PagerWindow.lua
-- The pager device UI: Inbox / Compose / Groups / Contacts.
-- ============================================================

require "ISUI/ISCollapsableWindow"
require "ISUI/ISScrollingListBox"
require "ISUI/ISButton"
require "ISUI/ISTextEntryBox"
require "ISUI/ISTickBox"
require "PagerMod_Shared"
require "PagerMod_Client"

PagerWindow = ISCollapsableWindow:derive("PagerWindow")

local FONT_SMALL  = UIFont.Small
local FONT_MED    = UIFont.Medium
local SCREEN_BG   = { r = 0.06, g = 0.09, b = 0.06 }
local SCREEN_TXT  = { r = 0.45, g = 1.0, b = 0.55 }
local SCREEN_DIM  = { r = 0.25, g = 0.6,  b = 0.3 }
local TAB_ACTIVE  = { r = 0.18, g = 0.30, b = 0.20 }
local LOC_TXT     = { r = 0.55, g = 0.8,  b = 1.0 }
local SOS_TXT     = { r = 1.0,  g = 0.45, b = 0.3 }

local MODE_INBOX, MODE_COMPOSE, MODE_GROUPS, MODE_CONTACTS, MODE_GLOBAL = 1, 2, 3, 4, 5

-- ── Text helpers ───────────────────────────────────────────
-- Strings here are UTF-8 (Cyrillic, Korean, Polish diacritics, ...): a plain
-- byte-oriented sub() can chop a multi-byte character in half and leave a
-- dangling continuation byte, which renders as a garbled/broken glyph. Trim
-- whole codepoints instead.
local function utf8TrimLastChar(text)
    local i = #text
    if i == 0 then return text end
    while i > 1 and text:byte(i) >= 0x80 and text:byte(i) < 0xC0 do
        i = i - 1
    end
    return text:sub(1, i - 1)
end

local function truncate(text, maxWidth, font)
    local tm = getTextManager()
    if tm:MeasureStringX(font, text) <= maxWidth then return text end
    while #text > 0 and tm:MeasureStringX(font, text .. "...") > maxWidth do
        text = utf8TrimLastChar(text)
    end
    return text .. "..."
end

local function locationMode()
    local st = PagerMod.netStatus or {}
    return st.locationSharing or PagerMod.Config.locationSharing
end

-- ── Layout sizing ──────────────────────────────────────────
-- Translated button/tab labels vary wildly in length (Cyrillic, Korean, long
-- Polish compounds, ...). Size every button from its own measured text width
-- instead of a hardcoded pixel constant, and size the window to whatever the
-- longest row actually needs instead of assuming English fits in 460px.
local BTN_H = 26
local BTN_PAD = 20
local BTN_MIN_W = 60
local BTN_GAP = 4

local function measureBtn(label, font)
    return math.max(BTN_MIN_W, getTextManager():MeasureStringX(font or FONT_SMALL, label) + BTN_PAD)
end

-- Width needed for a row of left-anchored buttons plus one right-anchored one.
local function rowWidth(pad, leftLabels, rightLabel, font)
    local w = pad
    for _, l in ipairs(leftLabels) do
        w = w + measureBtn(l, font) + BTN_GAP
    end
    if rightLabel then
        w = w + measureBtn(rightLabel, font)
    else
        w = w - BTN_GAP
    end
    return w + pad
end

-- Width needed for the tab row: all tabs share one width, sized to the widest label.
local function tabRowWidth(pad, gap, labels, font)
    local maxW = 0
    for _, l in ipairs(labels) do
        local w = measureBtn(l, font)
        if w > maxW then maxW = w end
    end
    return pad * 2 + #labels * maxW + gap * (#labels - 1)
end

-- ── Construction ───────────────────────────────────────────

function PagerWindow:new(x, y)
    PagerMod.refreshConfig()
    local hasGlobal = PagerMod.Config.globalChat == true
    local pad, gap = 10, 4

    local tabLabels = {
        getText("IGUI_PagerMod_TabInbox"), getText("IGUI_PagerMod_TabCompose"),
        getText("IGUI_PagerMod_TabGroups"), getText("IGUI_PagerMod_TabContacts"),
    }
    if hasGlobal then table.insert(tabLabels, getText("IGUI_PagerMod_TabGlobal")) end

    -- The Block/Unblock button toggles its label at runtime (see prerender), so
    -- reserve room for whichever text is wider.
    local blockLabel = getText("IGUI_PagerMod_Block")
    if getTextManager():MeasureStringX(FONT_SMALL, getText("IGUI_PagerMod_Unblock")) > getTextManager():MeasureStringX(FONT_SMALL, blockLabel) then
        blockLabel = getText("IGUI_PagerMod_Unblock")
    end

    local needed = tabRowWidth(pad, gap, tabLabels, FONT_SMALL)
    needed = math.max(needed, rowWidth(pad, {
        getText("IGUI_PagerMod_OpenMsg"), getText("IGUI_PagerMod_Reply"),
        blockLabel, getText("IGUI_PagerMod_Delete"),
    }, getText("IGUI_PagerMod_ClearAll")))
    needed = math.max(needed, rowWidth(pad, {
        getText("IGUI_PagerMod_Send"), getText("IGUI_PagerMod_Broadcast"),
    }, getText("IGUI_PagerMod_SOS")))
    needed = math.max(needed, rowWidth(pad, {
        getText("IGUI_PagerMod_NewChannel"), getText("IGUI_PagerMod_AddMember"), getText("IGUI_PagerMod_Message"),
    }, getText("IGUI_PagerMod_Delete")))
    needed = math.max(needed, rowWidth(pad, {
        getText("IGUI_PagerMod_Add"), getText("IGUI_PagerMod_Message"), blockLabel,
    }, getText("IGUI_PagerMod_Remove")))

    local width, height = math.max(460, needed), 540
    if not x then x = (getCore():getScreenWidth() - width) / 2 end
    if not y then y = (getCore():getScreenHeight() - height) / 2 end
    local o = ISCollapsableWindow.new(self, x, y, width, height)
    o.resizable = false
    o.title = "Pager"
    o.mode = MODE_INBOX
    o.item = nil
    o.pagerNumber = nil
    o.md = nil
    o.targetChannel = nil
    return o
end

function PagerWindow:bindItem(item)
    self.item = item
    self.isTower = false
    self.md = item and item:getModData() or nil
    if self.md then
        self.md.messages = self.md.messages or {}
        self.md.unread = self.md.unread or 0
    end
    self.pagerNumber = self.md and self.md.pagerNumber or nil
    self.title = self.md and (self.md.pagerName or "Pager") or "Pager"
end

-- Bind the window to a specific pager-tower node (number e.g. "0000002"). There
-- is no backing inventory item: each tower's messages live in player modData
-- (PagerMod.getTowerMD[number]) and sends go out as that tower (viaTower).
function PagerWindow:bindTower(number, name, powered)
    self.item = nil
    self.isTower = true
    self.towerNumber = number
    self.towerName = name or getText("IGUI_PagerMod_TowerName")
    self.towerPowered = powered
    self.md = PagerMod.getTowerMD(number, name)
    self.md.messages = self.md.messages or {}
    self.md.unread = self.md.unread or 0
    self.pagerNumber = number
    self.title = self.towerName
end

-- Persist the currently-bound store (pager item or the tower's player modData).
function PagerWindow:persist()
    if self.isTower then
        PagerMod.saveTowerMD()
    else
        PagerMod.saveItem(self.item)
    end
end

-- ── Children ───────────────────────────────────────────────

function PagerWindow:createChildren()
    ISCollapsableWindow.createChildren(self)

    local th = self:titleBarHeight()
    local pad = 10
    local w = self.width
    local screenH = 58
    local tabsY = th + screenH + pad
    local tabH = 24
    local gap = 4
    -- A 5th GLOBAL tab appears only when the global channel is enabled.
    PagerMod.refreshConfig()
    self.hasGlobal = PagerMod.Config.globalChat == true
    local nTabs = self.hasGlobal and 5 or 4
    local tabW = (w - pad * 2 - gap * (nTabs - 1)) / nTabs
    local contentY = tabsY + tabH + pad

    -- Tabs
    local function mkTab(idx, key, label)
        local b = ISButton:new(pad + (tabW + gap) * idx, tabsY, tabW, tabH, label, self, PagerWindow.onTab)
        b.internal = key
        b:initialise(); b:instantiate()
        self:addChild(b)
        return b
    end
    self.tabInbox    = mkTab(0, "INBOX",    getText("IGUI_PagerMod_TabInbox"))
    self.tabCompose  = mkTab(1, "COMPOSE",  getText("IGUI_PagerMod_TabCompose"))
    self.tabGroups   = mkTab(2, "GROUPS",   getText("IGUI_PagerMod_TabGroups"))
    self.tabContacts = mkTab(3, "CONTACTS", getText("IGUI_PagerMod_TabContacts"))
    if self.hasGlobal then
        self.tabGlobal = mkTab(4, "GLOBAL", getText("IGUI_PagerMod_TabGlobal"))
    end

    local listH = self.height - contentY - 44
    local bottomY = self.height - 34

    -- ── Inbox ──────────────────────────────────────────────
    self.inboxList = ISScrollingListBox:new(pad, contentY, w - pad * 2, listH)
    self.inboxList:initialise(); self.inboxList:instantiate()
    self.inboxList.itemheight = 58
    self.inboxList.font = FONT_SMALL
    self.inboxList.doDrawItem = PagerWindow.drawInboxItem
    self.inboxList.drawBorder = true
    self.inboxList.parentWin = self
    self:addChild(self.inboxList)
    -- Double-click a message to open the full text (a list item only shows a
    -- truncated preview). The button below does the same for discoverability.
    local winRef = self
    self.inboxList.onMouseDoubleClick = function(_, mx, my) winRef:onOpenMessage() end

    local x = pad
    local openW = measureBtn(getText("IGUI_PagerMod_OpenMsg"))
    self.btnOpen = ISButton:new(x, bottomY, openW, BTN_H, getText("IGUI_PagerMod_OpenMsg"), self, PagerWindow.onOpenMessage)
    self.btnOpen:initialise(); self:addChild(self.btnOpen)
    x = x + openW + BTN_GAP
    local replyW = measureBtn(getText("IGUI_PagerMod_Reply"))
    self.btnReply = ISButton:new(x, bottomY, replyW, BTN_H, getText("IGUI_PagerMod_Reply"), self, PagerWindow.onReply)
    self.btnReply:initialise(); self:addChild(self.btnReply)
    x = x + replyW + BTN_GAP
    -- Sized for whichever of "Block"/"Unblock" is wider so toggling the label
    -- at runtime (see prerender) never clips or reflows the row.
    local blockMsgW = math.max(measureBtn(getText("IGUI_PagerMod_Block")), measureBtn(getText("IGUI_PagerMod_Unblock")))
    self.btnBlockMsg = ISButton:new(x, bottomY, blockMsgW, BTN_H, getText("IGUI_PagerMod_Block"), self, PagerWindow.onBlockSender)
    self.btnBlockMsg:initialise(); self:addChild(self.btnBlockMsg)
    x = x + blockMsgW + BTN_GAP
    local deleteW = measureBtn(getText("IGUI_PagerMod_Delete"))
    self.btnDelete = ISButton:new(x, bottomY, deleteW, BTN_H, getText("IGUI_PagerMod_Delete"), self, PagerWindow.onDelete)
    self.btnDelete:initialise(); self:addChild(self.btnDelete)
    local clearW = measureBtn(getText("IGUI_PagerMod_ClearAll"))
    self.btnClear = ISButton:new(w - pad - clearW, bottomY, clearW, BTN_H, getText("IGUI_PagerMod_ClearAll"), self, PagerWindow.onClear)
    self.btnClear:initialise(); self:addChild(self.btnClear)

    -- ── Compose ────────────────────────────────────────────
    local cy = contentY
    local pickW = measureBtn(getText("IGUI_PagerMod_Pick"))
    self.composeTo = ISTextEntryBox:new("", pad + 70, cy, w - pad * 2 - 70 - pickW - BTN_GAP, 24)
    self.composeTo:initialise(); self.composeTo:instantiate()
    self.composeTo:setOnlyNumbers(true)
    self.composeTo:setMaxTextLength(7)
    self:addChild(self.composeTo)

    self.btnPick = ISButton:new(w - pad - pickW, cy, pickW, 24, getText("IGUI_PagerMod_Pick"), self, PagerWindow.onPickContact)
    self.btnPick:initialise(); self:addChild(self.btnPick)

    self.composeBody = ISTextEntryBox:new("", pad, cy + 34, w - pad * 2, 80)
    self.composeBody:initialise(); self.composeBody:instantiate()
    self.composeBody:setMultipleLine(true)
    PagerMod.refreshConfig()
    self.composeBody:setMaxTextLength(PagerMod.Config.messageMaxLength or 140)
    self:addChild(self.composeBody)

    self.locTick = ISTickBox:new(pad, cy + 120, w - pad * 2, 20, "", self, PagerWindow.onLocTick)
    self.locTick:initialise()
    self.locTick:addOption(getText("IGUI_PagerMod_AttachLocation"))
    self:addChild(self.locTick)

    local sendW = measureBtn(getText("IGUI_PagerMod_Send"))
    self.btnSend = ISButton:new(pad, bottomY, sendW, BTN_H, getText("IGUI_PagerMod_Send"), self, PagerWindow.onSend)
    self.btnSend:initialise(); self:addChild(self.btnSend)
    local broadcastW = measureBtn(getText("IGUI_PagerMod_Broadcast"))
    self.btnBroadcast = ISButton:new(pad + sendW + BTN_GAP, bottomY, broadcastW, BTN_H, getText("IGUI_PagerMod_Broadcast"), self, PagerWindow.onBroadcast)
    self.btnBroadcast:initialise(); self:addChild(self.btnBroadcast)
    local sosW = measureBtn(getText("IGUI_PagerMod_SOS"))
    self.btnSOS = ISButton:new(w - pad - sosW, bottomY, sosW, BTN_H, getText("IGUI_PagerMod_SOS"), self, PagerWindow.onSOS)
    self.btnSOS:initialise()
    self.btnSOS.textColor = { r = SOS_TXT.r, g = SOS_TXT.g, b = SOS_TXT.b, a = 1 }
    self:addChild(self.btnSOS)

    -- ── Groups (channels) ──────────────────────────────────
    self.channelsList = ISScrollingListBox:new(pad, contentY, w - pad * 2, listH)
    self.channelsList:initialise(); self.channelsList:instantiate()
    self.channelsList.itemheight = 30
    self.channelsList.font = FONT_SMALL
    self.channelsList.doDrawItem = PagerWindow.drawChannelItem
    self.channelsList.drawBorder = true
    self.channelsList.parentWin = self
    self:addChild(self.channelsList)

    local chNewW = measureBtn(getText("IGUI_PagerMod_NewChannel"))
    self.btnChNew = ISButton:new(pad, bottomY, chNewW, BTN_H, getText("IGUI_PagerMod_NewChannel"), self, PagerWindow.onChannelNew)
    self.btnChNew:initialise(); self:addChild(self.btnChNew)
    local chAddW = measureBtn(getText("IGUI_PagerMod_AddMember"))
    self.btnChAdd = ISButton:new(pad + chNewW + BTN_GAP, bottomY, chAddW, BTN_H, getText("IGUI_PagerMod_AddMember"), self, PagerWindow.onChannelAddMember)
    self.btnChAdd:initialise(); self:addChild(self.btnChAdd)
    local chMsgW = measureBtn(getText("IGUI_PagerMod_Message"))
    self.btnChMsg = ISButton:new(pad + chNewW + BTN_GAP + chAddW + BTN_GAP, bottomY, chMsgW, BTN_H, getText("IGUI_PagerMod_Message"), self, PagerWindow.onMessageChannel)
    self.btnChMsg:initialise(); self:addChild(self.btnChMsg)
    local chDelW = measureBtn(getText("IGUI_PagerMod_Delete"))
    self.btnChDel = ISButton:new(w - pad - chDelW, bottomY, chDelW, BTN_H, getText("IGUI_PagerMod_Delete"), self, PagerWindow.onChannelDelete)
    self.btnChDel:initialise(); self:addChild(self.btnChDel)

    -- ── Contacts ───────────────────────────────────────────
    self.contactsList = ISScrollingListBox:new(pad, contentY, w - pad * 2, listH)
    self.contactsList:initialise(); self.contactsList:instantiate()
    self.contactsList.itemheight = 30
    self.contactsList.font = FONT_SMALL
    self.contactsList.doDrawItem = PagerWindow.drawContactItem
    self.contactsList.drawBorder = true
    self.contactsList.parentWin = self
    self:addChild(self.contactsList)

    local addContactW = measureBtn(getText("IGUI_PagerMod_Add"))
    self.btnAddContact = ISButton:new(pad, bottomY, addContactW, BTN_H, getText("IGUI_PagerMod_Add"), self, PagerWindow.onAddContact)
    self.btnAddContact:initialise(); self:addChild(self.btnAddContact)
    local msgContactW = measureBtn(getText("IGUI_PagerMod_Message"))
    self.btnMsgContact = ISButton:new(pad + addContactW + BTN_GAP, bottomY, msgContactW, BTN_H, getText("IGUI_PagerMod_Message"), self, PagerWindow.onMessageContact)
    self.btnMsgContact:initialise(); self:addChild(self.btnMsgContact)
    -- Sized for whichever of "Block"/"Unblock" is wider so toggling the label
    -- at runtime (see prerender) never clips or reflows the row.
    local blockContactW = math.max(measureBtn(getText("IGUI_PagerMod_Block")), measureBtn(getText("IGUI_PagerMod_Unblock")))
    self.btnBlockContact = ISButton:new(pad + addContactW + BTN_GAP + msgContactW + BTN_GAP, bottomY, blockContactW, BTN_H, getText("IGUI_PagerMod_Block"), self, PagerWindow.onBlockContact)
    self.btnBlockContact:initialise(); self:addChild(self.btnBlockContact)
    local removeContactW = measureBtn(getText("IGUI_PagerMod_Remove"))
    self.btnRemoveContact = ISButton:new(w - pad - removeContactW, bottomY, removeContactW, BTN_H, getText("IGUI_PagerMod_Remove"), self, PagerWindow.onRemoveContact)
    self.btnRemoveContact:initialise(); self:addChild(self.btnRemoveContact)

    -- ── Global channel: a post box + Post button under the shared feed list ──
    if self.hasGlobal then
        local globalPostW = measureBtn(getText("IGUI_PagerMod_GlobalPost"))
        self.globalEntry = ISTextEntryBox:new("", pad, bottomY, w - pad * 2 - globalPostW - BTN_GAP, 26)
        self.globalEntry.font = UIFont.Medium
        self.globalEntry:initialise(); self.globalEntry:instantiate()
        self:addChild(self.globalEntry)
        self.btnGlobalPost = ISButton:new(w - pad - globalPostW, bottomY, globalPostW, BTN_H, getText("IGUI_PagerMod_GlobalPost"), self, PagerWindow.onGlobalPost)
        self.btnGlobalPost:initialise(); self:addChild(self.btnGlobalPost)
    end

    self:setMode(MODE_INBOX)
    self:refresh()
end

-- ── Mode switching ─────────────────────────────────────────

function PagerWindow:onTab(button)
    local map = { INBOX = MODE_INBOX, COMPOSE = MODE_COMPOSE, GROUPS = MODE_GROUPS, CONTACTS = MODE_CONTACTS, GLOBAL = MODE_GLOBAL }
    self:setMode(map[button.internal] or MODE_INBOX)
end

function PagerWindow:setMode(mode)
    PagerMod.refreshConfig()
    local st = PagerMod.netStatus or {}
    -- The tower node can always send (it is not a pager); it can only receive
    -- when comms mode allows it.
    local canSend     = self.isTower and true or PagerMod.canSend()
    local canReceive  = (not self.isTower) or PagerMod.towerCanReceive()
    local canBroadcast = canSend and (st.allowBroadcast ~= false) and PagerMod.Config.allowBroadcast
    local canSOS      = canSend and (st.allowSOS ~= false) and PagerMod.Config.allowSOS
    local canBlock    = (not self.isTower) and (st.allowBlocking ~= false) and PagerMod.Config.allowBlocking
    -- The global channel is a player feature; a tower console has no GLOBAL tab.
    if mode == MODE_GLOBAL and self.isTower then mode = MODE_COMPOSE end
    -- Receive-only pagers have no Compose tab; redirect there to the inbox.
    if mode == MODE_COMPOSE and not canSend then mode = MODE_INBOX end
    -- A send-only tower has no inbox; redirect to compose.
    if mode == MODE_INBOX and not canReceive then mode = MODE_COMPOSE end
    self.mode = mode
    local inbox = (mode == MODE_INBOX)
    local compose = (mode == MODE_COMPOSE)
    local groups = (mode == MODE_GROUPS)
    local contacts = (mode == MODE_CONTACTS)
    local global = (mode == MODE_GLOBAL)
    self.globalMode = global  -- read by the notification path

    -- Switching away from a channel target unless we entered compose for one.
    if not compose then self.targetChannel = nil end

    -- The Compose and Groups tabs are sending features; hide them receive-only.
    self.tabCompose:setVisible(canSend)
    self.tabGroups:setVisible(canSend)
    -- The Inbox tab is hidden for a send-only tower (no incoming pages).
    self.tabInbox:setVisible(canReceive)
    -- The GLOBAL tab is a player feature; hide it on a tower console.
    if self.tabGlobal then self.tabGlobal:setVisible(not self.isTower) end

    -- The shared list shows the pager inbox OR the global feed.
    self.inboxList:setVisible(inbox or global)
    -- Open (read full message) applies to both the inbox and the global feed.
    self.btnOpen:setVisible(inbox or global)
    -- Reply is a sending action (inbox only, not the global feed).
    self.btnReply:setVisible(inbox and canSend)
    self.btnBlockMsg:setVisible(inbox and canBlock)
    self.btnDelete:setVisible(inbox)
    self.btnClear:setVisible(inbox)

    -- Global channel post row (read-only on receive-only servers).
    if self.globalEntry then self.globalEntry:setVisible(global and canSend) end
    if self.btnGlobalPost then self.btnGlobalPost:setVisible(global and canSend) end

    self.composeTo:setVisible(compose)
    self.btnPick:setVisible(compose)
    self.composeBody:setVisible(compose)
    self.btnSend:setVisible(compose)
    self.btnBroadcast:setVisible(compose and canBroadcast)
    self.btnSOS:setVisible(compose and canSOS)
    self.locTick:setVisible(compose and locationMode() == PagerMod.LocationMode.OPT_IN)

    -- Groups are only reachable when sending is possible.
    self.channelsList:setVisible(groups and canSend)
    self.btnChNew:setVisible(groups and canSend)
    self.btnChAdd:setVisible(groups and canSend)
    self.btnChMsg:setVisible(groups and canSend)
    self.btnChDel:setVisible(groups and canSend)

    self.contactsList:setVisible(contacts)
    self.btnAddContact:setVisible(contacts)
    -- Messaging a contact requires sending.
    self.btnMsgContact:setVisible(contacts and canSend)
    self.btnBlockContact:setVisible(contacts and canBlock)
    self.btnRemoveContact:setVisible(contacts)

    if inbox or global then self:markAllRead() end
    self:refresh()
end

function PagerWindow:onLocTick() end

-- ── Data refresh ───────────────────────────────────────────

function PagerWindow:markAllRead()
    -- In GLOBAL mode the feed is the shared global store, not the bound pager.
    if self.globalMode then
        local g = PagerMod.getGlobalMD()
        for _, m in ipairs(g.messages or {}) do m.read = true end
        g.unread = 0
        PagerMod.savePlayer()
        return
    end
    if not self.md then return end
    local justRead = {}
    for _, m in ipairs(self.md.messages or {}) do
        if not m.read then
            m.read = true
            table.insert(justRead, m)
        end
    end
    self.md.unread = 0
    if #justRead > 0 then
        self:persist()
        -- The tower node sends no read receipts.
        if not self.isTower then
            PagerMod.sendReadReceipts(justRead, self.item)
        end
    end
end

function PagerWindow:refresh()
    if not self.inboxList then return end
    if self.item then
        self.md = self.item:getModData()
        self.md.messages = self.md.messages or {}
        self.pagerNumber = self.md.pagerNumber
        self.title = self.md.pagerName or "Pager"
    end

    -- The shared list shows the global feed in GLOBAL mode, else the pager inbox.
    self.inboxList:clear()
    local feed = self.globalMode and PagerMod.getGlobalMD().messages or (self.md and self.md.messages)
    if feed then
        for i = #feed, 1, -1 do
            self.inboxList:addItem(feed[i].text or "", feed[i])
        end
    end

    self.contactsList:clear()
    for _, c in ipairs(PagerWindow.getContacts()) do
        self.contactsList:addItem(c.name or "?", c)
    end

    self.channelsList:clear()
    for _, ch in ipairs(PagerMod.getChannels()) do
        self.channelsList:addItem(ch.name or "?", ch)
    end
end

-- ── Contacts storage (player modData) ──────────────────────
-- Delegate to PagerMod so the persistence (transmitModData) happens in one place.

function PagerWindow.getContacts()
    return PagerMod.getContacts()
end

function PagerWindow.saveContacts(list)
    PagerMod.saveContacts(list)
end

-- ── Rendering ──────────────────────────────────────────────

function PagerWindow:prerender()
    ISCollapsableWindow.prerender(self)
    local th = self:titleBarHeight()
    local pad = 10
    local w = self.width

    self:drawRect(pad, th + 6, w - pad * 2, 50, 1, SCREEN_BG.r, SCREEN_BG.g, SCREEN_BG.b)
    self:drawRectBorder(pad, th + 6, w - pad * 2, 50, 1, SCREEN_DIM.r, SCREEN_DIM.g, SCREEN_DIM.b)

    local name = self.md and (self.md.pagerName or "Pager") or "Pager"
    local numStr = self.pagerNumber and PagerMod.formatNumber(self.pagerNumber) or "-------"
    self:drawText(name, pad + 8, th + 12, SCREEN_TXT.r, SCREEN_TXT.g, SCREEN_TXT.b, 1, FONT_MED)
    self:drawTextRight("#" .. numStr, w - pad - 8, th + 12, SCREEN_TXT.r, SCREEN_TXT.g, SCREEN_TXT.b, 1, FONT_MED)

    local st = PagerMod.netStatus or {}
    local sigLabel
    if st.signalMode == PagerMod.SignalMode.RANGE then
        sigLabel = getText("IGUI_PagerMod_SigRange") .. " " .. tostring(st.signalRange or "?")
    elseif PagerMod.isTowerMode(st.signalMode) then
        if st.signalMode == PagerMod.SignalMode.TOWER_AFTER and st.gridPower then
            -- Grid still up: in "after power dies" mode the network is map-wide.
            sigLabel = getText("IGUI_PagerMod_SigGlobal")
        elseif self.isTower then
            sigLabel = getText("IGUI_PagerMod_SigTowerUp")
        elseif not st.towerOnline then
            sigLabel = getText("IGUI_PagerMod_SigTowerDown")
        elseif st.inTowerRange == false then
            sigLabel = getText("IGUI_PagerMod_SigTowerOutOfRange")
        elseif st.coveringTowerNum then
            sigLabel = string.format(getText("IGUI_PagerMod_SigTowerOn"), st.coveringTowerNum)
        else
            sigLabel = getText("IGUI_PagerMod_SigTowerUp")
        end
    else
        sigLabel = getText("IGUI_PagerMod_SigGlobal")
    end
    if st.pagerMode == PagerMod.PagerMode.RECEIVE_ONLY then
        sigLabel = sigLabel .. " [" .. getText("IGUI_PagerMod_RxOnly") .. "]"
    end
    local unread = self.md and (self.md.unread or 0) or 0
    local statLine = string.format("%s  |  %s: %d  |  %s: %d",
        sigLabel, getText("IGUI_PagerMod_Net"), st.knownPagers or 0,
        getText("IGUI_PagerMod_Unread"), unread)
    self:drawText(statLine, pad + 8, th + 34, SCREEN_DIM.r, SCREEN_DIM.g, SCREEN_DIM.b, 1, FONT_SMALL)

    -- Battery readout (right side of the screen, reddens when low)
    if self.item then
        local pct = PagerMod.batteryPercent(self.item)
        local br, bg, bb = SCREEN_TXT.r, SCREEN_TXT.g, SCREEN_TXT.b
        if pct <= 15 then br, bg, bb = 1.0, 0.45, 0.3 end
        self:drawTextRight(string.format("BAT %d%%", pct), w - pad - 8, th + 34, br, bg, bb, 1, FONT_SMALL)
    end

    local function tabTint(btn, active)
        if not btn then return end
        if active then btn:setBackgroundRGBA(TAB_ACTIVE.r, TAB_ACTIVE.g, TAB_ACTIVE.b, 1)
        else btn:setBackgroundRGBA(0, 0, 0, 0.5) end
    end
    tabTint(self.tabInbox, self.mode == MODE_INBOX)
    tabTint(self.tabCompose, self.mode == MODE_COMPOSE)
    tabTint(self.tabGroups, self.mode == MODE_GROUPS)
    tabTint(self.tabContacts, self.mode == MODE_CONTACTS)
    if self.tabGlobal then
        tabTint(self.tabGlobal, self.mode == MODE_GLOBAL)
        -- Flag unread global posts on the tab when you're not looking at it.
        if self.mode ~= MODE_GLOBAL and (PagerMod.getGlobalMD().unread or 0) > 0 then
            self.tabGlobal:setBackgroundRGBA(0.15, 0.35, 0.5, 1)
        end
    end

    -- Block/Unblock is a single toggle (PagerMod.toggleBlock); relabel the
    -- button to reflect the current selection's state so it's clear a second
    -- click reverses it, instead of always reading "Block".
    if self.btnBlockContact then
        local c = self:getSelectedContact()
        local blocked = c and PagerMod.isBlockedNumber(c.number)
        self.btnBlockContact:setTitle(blocked and getText("IGUI_PagerMod_Unblock") or getText("IGUI_PagerMod_Block"))
    end
    if self.btnBlockMsg then
        local m = self:getSelectedMessage()
        local blocked = m and m.from and m.from ~= "SYSTEM" and PagerMod.isBlockedNumber(m.from)
        self.btnBlockMsg:setTitle(blocked and getText("IGUI_PagerMod_Unblock") or getText("IGUI_PagerMod_Block"))
    end

    if self.mode == MODE_COMPOSE then
        -- "To" label / channel indicator
        if self.targetChannel then
            self:drawText(getText("IGUI_PagerMod_ChannelTo") .. ": " .. tostring(self.targetChannel.name),
                pad, self.composeTo.y + 4, LOC_TXT.r, LOC_TXT.g, LOC_TXT.b, 1, FONT_SMALL)
        else
            self:drawText(getText("IGUI_PagerMod_To") .. ":", pad, self.composeTo.y + 4, 1, 1, 1, 1, FONT_SMALL)
        end
        local bodyLen = #(self.composeBody:getText() or "")
        local maxLen = PagerMod.Config.messageMaxLength or 140
        self:drawTextRight(bodyLen .. "/" .. maxLen, w - pad, self.composeBody.y - 16, 0.7, 0.7, 0.7, 1, FONT_SMALL)
        if locationMode() == PagerMod.LocationMode.ALWAYS then
            self:drawText(getText("IGUI_PagerMod_LocAlways"), pad, self.locTick.y + 2, LOC_TXT.r, LOC_TXT.g, LOC_TXT.b, 1, FONT_SMALL)
        end
    end
end

function PagerWindow.drawInboxItem(self, y, item, alt)
    local msg = item.item
    local h = self.itemheight
    if self.selected == item.index then
        self:drawRect(0, y, self:getWidth(), h, 0.25, 0.4, 0.7, 0.4)
    end
    self:drawRectBorder(0, y + h - 1, self:getWidth(), 1, 0.4, 0.3, 0.3, 0.3)
    if not msg then return y + h end

    local fromLabel = msg.fromName and msg.fromName ~= "" and msg.fromName or PagerMod.formatNumber(msg.from)
    local when = PagerMod.stampToClock(msg.stamp) .. " " .. PagerMod.stampToDate(msg.stamp)
    local r, g, b = 0.8, 1.0, 0.85
    if msg.sos then r, g, b = SOS_TXT.r, SOS_TXT.g, SOS_TXT.b
    elseif not msg.read then r, g, b = 0.4, 0.85, 1.0 end

    local prefix = msg.sos and "[SOS] " or ""
    self:drawText(prefix .. truncate(fromLabel, self:getWidth() - 110, self.font), 6, y + 4, r, g, b, 1, self.font)
    self:drawTextRight(when, self:getWidth() - 6, y + 4, 0.6, 0.6, 0.6, 1, self.font)
    self:drawText(truncate(msg.text or "", self:getWidth() - 12, self.font), 6, y + 22, 0.85, 0.85, 0.85, 1, self.font)

    -- Guarded: this runs every frame and reads live player position, so any
    -- unexpected data must not be allowed to abort the render (which would drop
    -- the buttons drawn after this list).
    local ok, loc = pcall(PagerMod.locationLine, msg)
    if ok and loc then
        self:drawText(loc, 6, y + 40, LOC_TXT.r, LOC_TXT.g, LOC_TXT.b, 1, self.font)
    end
    return y + h
end

function PagerWindow.drawContactItem(self, y, item, alt)
    local c = item.item
    local h = self.itemheight
    if self.selected == item.index then
        self:drawRect(0, y, self:getWidth(), h, 0.25, 0.4, 0.7, 0.4)
    end
    self:drawRectBorder(0, y + h - 1, self:getWidth(), 1, 0.4, 0.3, 0.3, 0.3)
    if c then
        local blocked = PagerMod.isBlockedNumber(c.number)
        local mark = blocked and (" [" .. getText("IGUI_PagerMod_Blocked") .. "]") or ""
        local r, g, b = 0.9, 1.0, 0.9
        if blocked then r, g, b = 0.9, 0.5, 0.5 end
        self:drawText((c.name or "?") .. mark, 6, y + 6, r, g, b, 1, self.font)
        self:drawTextRight("#" .. PagerMod.formatNumber(c.number), self:getWidth() - 6, y + 6, 0.6, 0.8, 0.6, 1, self.font)
    end
    return y + h
end

function PagerWindow.drawChannelItem(self, y, item, alt)
    local ch = item.item
    local h = self.itemheight
    if self.selected == item.index then
        self:drawRect(0, y, self:getWidth(), h, 0.25, 0.4, 0.7, 0.4)
    end
    self:drawRectBorder(0, y + h - 1, self:getWidth(), 1, 0.4, 0.3, 0.3, 0.3)
    if ch then
        self:drawText(ch.name or "?", 6, y + 6, 0.9, 1.0, 0.9, 1, self.font)
        local n = ch.members and #ch.members or 0
        self:drawTextRight(n .. " " .. getText("IGUI_PagerMod_Members"), self:getWidth() - 6, y + 6, 0.6, 0.8, 0.6, 1, self.font)
    end
    return y + h
end

-- ── Compose: location helper ───────────────────────────────

function PagerWindow:wantsLocation()
    local mode = locationMode()
    if mode == PagerMod.LocationMode.ALWAYS then return true end
    if mode == PagerMod.LocationMode.OPT_IN then return self.locTick:isSelected(1) end
    return false
end

-- ── Inbox actions ──────────────────────────────────────────

function PagerWindow:getSelectedMessage()
    local sel = self.inboxList.selected
    if not sel or sel < 1 then return nil end
    local entry = self.inboxList.items[sel]
    return entry and entry.item or nil
end

-- Open the selected message in a popup showing the FULL text (the inbox list
-- only shows a truncated preview). Works for both the pager inbox and the global
-- feed. Triggered by the Open button or a double-click on a message.
function PagerWindow:onOpenMessage()
    local msg = self:getSelectedMessage()
    if not msg then
        PagerMod.halo(getText("IGUI_PagerMod_NoReply"), { r = 0.9, g = 0.7, b = 0.2 })
        return
    end
    local from = (msg.fromName and msg.fromName ~= "" and msg.fromName) or PagerMod.formatNumber(msg.from)
    local when = PagerMod.stampToClock(msg.stamp) .. " " .. PagerMod.stampToDate(msg.stamp)
    local parts = {}
    table.insert(parts, (msg.sos and "[SOS] " or "") .. from .. "  (#" .. PagerMod.formatNumber(msg.from) .. ")")
    table.insert(parts, when)
    local ok, loc = pcall(PagerMod.locationLine, msg)
    if ok and loc then table.insert(parts, loc) end
    table.insert(parts, " ")
    table.insert(parts, tostring(msg.text or ""))
    local text = table.concat(parts, " <LINE> ")
    local mw, mh = 360, 280
    local mx = (getCore():getScreenWidth() - mw) / 2
    local my = (getCore():getScreenHeight() - mh) / 2
    local modal = ISModalRichText:new(mx, my, mw, mh, text, false)
    modal:initialise()
    modal:addToUIManager()
    -- Opening a message implicitly reads it.
    if self.markAllRead then self:markAllRead() end
end

function PagerWindow:onReply()
    local msg = self:getSelectedMessage()
    if not msg or not msg.from or msg.from == "SYSTEM" then
        PagerMod.halo(getText("IGUI_PagerMod_NoReply"), { r = 0.9, g = 0.7, b = 0.2 })
        return
    end
    self:setMode(MODE_COMPOSE)
    self.targetChannel = nil
    self.composeTo:setText(tostring(msg.from))
    self.composeBody:focus()
end

function PagerWindow:onBlockSender()
    local msg = self:getSelectedMessage()
    if not msg or not msg.from or msg.from == "SYSTEM" then return end
    local nowBlocked = PagerMod.toggleBlock(msg.from)
    PagerMod.halo(nowBlocked and getText("IGUI_PagerMod_BlockedNum") or getText("IGUI_PagerMod_UnblockedNum"))
    self:refresh()
end

function PagerWindow:onDelete()
    local sel = self.inboxList.selected
    if not sel or sel < 1 then return end
    local entry = self.inboxList.items[sel]
    if not entry or not entry.item or not self.md then return end
    local target = entry.item
    for i, m in ipairs(self.md.messages) do
        if m == target then
            if not m.read and self.md.unread > 0 then self.md.unread = self.md.unread - 1 end
            table.remove(self.md.messages, i)
            break
        end
    end
    self:persist()
    self:refresh()
end

function PagerWindow:onClear()
    if not self.md then return end
    self.md.messages = {}
    self.md.unread = 0
    self:persist()
    self:refresh()
end

-- ── Compose actions ────────────────────────────────────────

function PagerWindow:onSend()
    local body = self.composeBody:getText()
    if self.targetChannel then
        if self.isTower then
            PagerMod.towerSendChannel(self.towerNumber, self.towerName, self.targetChannel, body, self:wantsLocation())
        else
            PagerMod.sendChannel(self.item, self.targetChannel, body, self:wantsLocation())
        end
        return
    end
    local to = self.composeTo:getText()
    if self.isTower then
        PagerMod.towerSend(self.towerNumber, self.towerName, to, body, self:wantsLocation())
    else
        PagerMod.sendMessage(self.item, to, body, self:wantsLocation())
    end
end

function PagerWindow:onBroadcast()
    if self.isTower then
        PagerMod.towerBroadcast(self.towerNumber, self.towerName, self.composeBody:getText(), self:wantsLocation())
    else
        PagerMod.broadcast(self.item, self.composeBody:getText(), self:wantsLocation())
    end
end

function PagerWindow:onSOS()
    if self.isTower then
        PagerMod.towerSOS(self.towerNumber, self.towerName, self.composeBody:getText())
    else
        PagerMod.sendSOS(self.item, self.composeBody:getText())
    end
end

function PagerWindow:onGlobalPost()
    if not self.globalEntry then return end
    local body = self.globalEntry:getText()
    if not body or body == "" then return end
    PagerMod.globalSend(self.item, body, false)
end

function PagerWindow:onMessageSent()
    if self.composeBody then self.composeBody:setText("") end
    if self.globalEntry then self.globalEntry:setText("") end
end

-- ── Contact actions ────────────────────────────────────────

function PagerWindow:onPickContact() self:setMode(MODE_CONTACTS) end

function PagerWindow:getSelectedContact()
    local sel = self.contactsList.selected
    if not sel or sel < 1 then return nil end
    local entry = self.contactsList.items[sel]
    return entry and entry.item or nil
end

function PagerWindow:onMessageContact()
    local c = self:getSelectedContact()
    if not c then return end
    self:setMode(MODE_COMPOSE)
    self.targetChannel = nil
    self.composeTo:setText(tostring(c.number))
    self.composeBody:focus()
end

function PagerWindow:onBlockContact()
    local c = self:getSelectedContact()
    if not c then return end
    local nowBlocked = PagerMod.toggleBlock(c.number)
    PagerMod.halo(nowBlocked and getText("IGUI_PagerMod_BlockedNum") or getText("IGUI_PagerMod_UnblockedNum"))
    self:refresh()
end

function PagerWindow:onRemoveContact()
    local c = self:getSelectedContact()
    if not c then return end
    local list = PagerWindow.getContacts()
    for i, x in ipairs(list) do
        if x.number == c.number and x.name == c.name then
            table.remove(list, i)
            break
        end
    end
    PagerWindow.saveContacts(list)
    self:refresh()
end

function PagerWindow:onAddContact()
    local player = getPlayer()
    local win = self
    local function askName(number)
        local modal = ISTextBox:new(0, 0, 320, 160, getText("IGUI_PagerMod_EnterContactName"), "", number, function(num, button)
            if button.internal ~= "OK" then return end
            local name = button.parent.entry:getText()
            if not name or name == "" then name = PagerMod.formatNumber(num) end
            local list = PagerWindow.getContacts()
            table.insert(list, { name = name:sub(1, 24), number = num })
            PagerWindow.saveContacts(list)
            win:refresh()
        end, player:getPlayerNum())
        modal:initialise(); modal:addToUIManager()
    end
    local numModal = ISTextBox:new(0, 0, 320, 160, getText("IGUI_PagerMod_EnterContactNumber"), "", nil, function(_, button)
        if button.internal ~= "OK" then return end
        local num = PagerMod.sanitizeNumber(button.parent.entry:getText())
        if num == "" then
            PagerMod.halo(getText("IGUI_PagerMod_UnknownNumber"), { r = 0.9, g = 0.3, b = 0.2 })
            return
        end
        askName(num)
    end, player:getPlayerNum())
    numModal:initialise(); numModal:addToUIManager()
end

-- ── Channel actions ────────────────────────────────────────

function PagerWindow:getSelectedChannel()
    local sel = self.channelsList.selected
    if not sel or sel < 1 then return nil end
    local entry = self.channelsList.items[sel]
    return entry and entry.item or nil
end

function PagerWindow:onChannelNew()
    local win = self
    local modal = ISTextBox:new(0, 0, 320, 160, getText("IGUI_PagerMod_EnterChannelName"), "", nil, function(_, button)
        if button.internal ~= "OK" then return end
        local name = button.parent.entry:getText()
        if not name or name == "" then return end
        local list = PagerMod.getChannels()
        table.insert(list, { name = name:sub(1, 24), members = {} })
        PagerMod.saveChannels(list)
        win:refresh()
    end, getPlayer():getPlayerNum())
    modal:initialise(); modal:addToUIManager()
end

function PagerWindow:onChannelDelete()
    local ch = self:getSelectedChannel()
    if not ch then return end
    local list = PagerMod.getChannels()
    for i, x in ipairs(list) do
        if x == ch then table.remove(list, i) break end
    end
    PagerMod.saveChannels(list)
    self:refresh()
end

function PagerWindow:onChannelAddMember()
    local ch = self:getSelectedChannel()
    if not ch then
        PagerMod.halo(getText("IGUI_PagerMod_PickChannel"), { r = 0.9, g = 0.7, b = 0.2 })
        return
    end
    local win = self
    local modal = ISTextBox:new(0, 0, 320, 160, getText("IGUI_PagerMod_EnterContactNumber"), "", nil, function(_, button)
        if button.internal ~= "OK" then return end
        local num = PagerMod.sanitizeNumber(button.parent.entry:getText())
        if num == "" then return end
        ch.members = ch.members or {}
        for _, m in ipairs(ch.members) do if m == num then return end end
        table.insert(ch.members, num)
        PagerMod.saveChannels(PagerMod.getChannels())
        win:refresh()
    end, getPlayer():getPlayerNum())
    modal:initialise(); modal:addToUIManager()
end

function PagerWindow:onMessageChannel()
    local ch = self:getSelectedChannel()
    if not ch then return end
    self:setMode(MODE_COMPOSE)
    self.targetChannel = ch
    self.composeBody:focus()
end

-- ── Open / singleton ───────────────────────────────────────

function PagerMod.OpenPagerUI(item)
    -- Battery gate (covers every open path, including the hotkey).
    if item then
        PagerMod.ensureBattery(item)
        if PagerMod.batteryDead(item) then
            PagerMod.halo(getText("IGUI_PagerMod_BatteryDead"), { r = 0.9, g = 0.4, b = 0.2 })
            return
        end
    end
    PagerMod.pingNetwork()
    PagerMod.fetchInbox()
    if PagerMod.UI then
        PagerMod.UI:bindItem(item)
        if not PagerMod.UI:getIsVisible() then
            PagerMod.UI:setVisible(true)
            PagerMod.UI:addToUIManager()
        end
        PagerMod.UI:setMode(MODE_INBOX)
        PagerMod.UI:bringToTop()
        return
    end
    local win = PagerWindow:new()
    win:bindItem(item)
    win:initialise()
    win:addToUIManager()
    PagerMod.UI = win
end

-- Open the pager window as a specific tower's console. Called from the
-- TOWER_OPEN server reply, which already drained the tower's inbox into its
-- client store; no FETCH needed here. Sends go out as that tower.
function PagerMod.OpenTowerUI(number, name, powered)
    PagerMod.pingNetwork()
    local startMode = PagerMod.towerCanReceive() and MODE_INBOX or MODE_COMPOSE
    if PagerMod.UI then
        PagerMod.UI:bindTower(number, name, powered)
        if not PagerMod.UI:getIsVisible() then
            PagerMod.UI:setVisible(true)
            PagerMod.UI:addToUIManager()
        end
        PagerMod.UI:setMode(startMode)
        PagerMod.UI:bringToTop()
        return
    end
    local win = PagerWindow:new()
    win:bindTower(number, name, powered)
    win:initialise()
    win:addToUIManager()
    PagerMod.UI = win
    win:setMode(startMode)
end

function PagerWindow:close()
    self:setVisible(false)
    self:removeFromUIManager()
end

-- Open the UI straight into Compose, pre-targeted at a number.
function PagerMod.openComposeTo(item, toNumber)
    PagerMod.OpenPagerUI(item)
    local ui = PagerMod.UI
    if not ui then return end
    ui:setMode(MODE_COMPOSE)
    ui.targetChannel = nil
    ui.composeTo:setText(tostring(toNumber or ""))
    ui.composeBody:focus()
end

-- Open the UI straight into Compose, pre-targeted at a channel.
function PagerMod.openComposeChannel(item, channel)
    PagerMod.OpenPagerUI(item)
    local ui = PagerMod.UI
    if not ui then return end
    ui:setMode(MODE_COMPOSE)
    ui.targetChannel = channel
    ui.composeBody:focus()
end
