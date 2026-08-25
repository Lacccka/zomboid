-- Copyright (c) 2026 ReapBone. All rights reserved.

require "ISUI/ISPanel"
require "ISUI/ISButton"
require "ISUI/ISLabel"
require "ISUI/ISTextEntryBox"
require "ISUI/ISScrollingListBox"
require "ISUI/ISColorPicker"
require "BClan/BClan_Config"
require "BClan/BClan_Language"

BClanUI = ISPanel:derive("BClanUI")
BClanUI.instance = nil

local PAD = 12
local SMALL = getTextManager():getFontHeight(UIFont.Small)
local MEDIUM = getTextManager():getFontHeight(UIFont.Medium)
local BUTTON_H = SMALL + 8
local LANGUAGE_ACTIVE = { r = 0.48, g = 0.12, b = 0.1, a = 1 }
local LANGUAGE_INACTIVE = { r = 0, g = 0, b = 0, a = 0.8 }

local function send(command, args)
    local player = getPlayer()
    if player then sendClientCommand(player, BClan.Config.NetworkModule, command, args or {}) end
end

local function addLabel(parent, text, x, y, color)
    color = color or { r = 1, g = 1, b = 1 }
    local label = ISLabel:new(x, y, BUTTON_H, text, color.r, color.g, color.b, 1, UIFont.Small, true)
    label:initialise()
    label:instantiate()
    parent:addChild(label)
    return label
end

local function addEntry(parent, text, x, y, width)
    local entry = ISTextEntryBox:new(text or "", x, y, width, BUTTON_H)
    entry.font = UIFont.Small
    entry:initialise()
    entry:instantiate()
    parent:addChild(entry)
    return entry
end

local function addButton(parent, title, internal, x, y, width)
    local button = ISButton:new(x, y, width, BUTTON_H, title, parent, BClanUI.onButton)
    button.internal = internal
    button:initialise()
    button:instantiate()
    button.borderColor = { r = 0.65, g = 0.65, b = 0.65, a = 0.55 }
    parent:addChild(button)
    return button
end

local function selectedName(list)
    if not list or list.selected <= 0 or not list.items[list.selected] then return nil end
    return list.items[list.selected].item.name
end

function BClanUI.drawListItem(list, y, item, alt)
    if alt then list:drawRect(0, y, list.width, list.itemheight - 1, 0.12, 0.25, 0.25, 0.25) end
    if list.selected == item.index then
        list:drawRect(0, y, list.width, list.itemheight - 1, 0.35, 0.55, 0.18, 0.16)
    end
    list:drawRectBorder(0, y, list.width, list.itemheight - 1, 0.5, 0.5, 0.5, 0.5)
    list:drawText(item.item.name, 8, y + 3, 0.92, 0.92, 0.92, 1, UIFont.Small)
    return y + list.itemheight
end

function BClanUI:addList(x, y, width, height)
    local list = ISScrollingListBox:new(x, y, width, height)
    list:initialise()
    list:instantiate()
    list.itemheight = BUTTON_H
    list.selected = 0
    list.font = UIFont.Small
    list.doDrawItem = BClanUI.drawListItem
    list.drawBorder = true
    self:addChild(list)
    return list
end

function BClanUI:getCurrent()
    local player = getPlayer()
    local faction = player and Faction.getPlayerFaction(player) or nil
    local entry = nil
    if faction and BClan.ClientData and BClan.ClientData.clans then
        entry = BClan.ClientData.clans[faction:getName()]
    end
    return player, faction, entry
end

function BClanUI:initialise()
    ISPanel.initialise(self)
    self.languageTR = addButton(self, "TR", "LANG_TR", self.width - 98, 6, 42)
    self.languageEN = addButton(self, "EN", "LANG_EN", self.width - 50, 6, 42)
    local language = BClan.getLanguage()
    self.languageTR.backgroundColor = language == "TR" and LANGUAGE_ACTIVE or LANGUAGE_INACTIVE
    self.languageEN.backgroundColor = language == "EN" and LANGUAGE_ACTIVE or LANGUAGE_INACTIVE
    local player, faction, entry = self:getCurrent()
    self.factionName = faction and faction:getName() or nil
    self.hasInvite = player and BClan.ClientData and BClan.ClientData.invites and BClan.ClientData.invites[player:getUsername()] ~= nil

    if not faction then
        self:buildNoClan(player)
    else
        self:buildClan(player, faction, entry)
    end
end

function BClanUI:buildNoClan(player)
    local y = MEDIUM + PAD * 3
    if not BClan.ClientData then
        addLabel(self, BClan.text("BClan_Loading"), PAD, y, { r = 0.8, g = 0.8, b = 0.45 })
        send("RequestData")
        self:setHeight(150)
        self.closeButton = addButton(self, BClan.text("BClan_Close"), "CLOSE", self.width - 112, self.height - PAD - BUTTON_H, 100)
        return
    end

    local invite = player and BClan.ClientData.invites and BClan.ClientData.invites[player:getUsername()] or nil
    if invite then
        addLabel(self, BClan.text("BClan_InviteTitle"), PAD, y, { r = 0.9, g = 0.72, b = 0.25 })
        y = y + BUTTON_H + 4
        addLabel(self, invite, PAD, y, { r = 0.85, g = 0.85, b = 1 })
        y = y + BUTTON_H + PAD
        addButton(self, BClan.text("BClan_Accept"), "ACCEPT_INVITE", PAD, y, 140)
        addButton(self, BClan.text("BClan_Decline"), "DECLINE_INVITE", PAD + 152, y, 140)
        y = y + BUTTON_H + PAD * 2
    else
        addLabel(self, BClan.text("BClan_Name"), PAD, y)
        self.nameEntry = addEntry(self, "", 150, y, self.width - 162)
        y = y + BUTTON_H + PAD
        addLabel(self, BClan.text("BClan_Tag"), PAD, y)
        self.tagEntry = addEntry(self, "", 150, y, 130)
        y = y + BUTTON_H + PAD * 2
        addButton(self, BClan.text("BClan_Create"), "CREATE", PAD, y, 180)
        y = y + BUTTON_H + PAD * 2
        addLabel(self, BClan.text("BClan_CreateHint"), PAD, y, { r = 0.65, g = 0.68, b = 0.7 })
        y = y + BUTTON_H + PAD
    end
    self:setHeight(math.max(230, y + BUTTON_H + PAD * 2))
    addButton(self, BClan.text("BClan_Ranking"), "RANKING", PAD, self.height - PAD - BUTTON_H, 150)
    self.closeButton = addButton(self, BClan.text("BClan_Close"), "CLOSE", self.width - 112, self.height - PAD - BUTTON_H, 100)
end

function BClanUI:buildClan(player, faction, entry)
    local leftX = PAD
    local rightX = 370
    local columnW = self.width - rightX - PAD
    local y = MEDIUM + PAD * 3

    self.owner = faction:isOwner(player:getUsername())
    local ok, admin = pcall(function() return player:getRole():hasCapability(Capability.FactionCheat) end)
    self.canManage = self.owner or (ok and admin)

    self.ownerLabel = addLabel(self, "", leftX, y)
    self.tagLabel = addLabel(self, "", rightX, y)
    if self.canManage then
        self.tagColorLabel = addLabel(self, BClan.text("BClan_TagColor"), rightX + 170, y)
        self.tagColorButton = addButton(self, "", "TAG_COLOR", self.width - PAD - BUTTON_H, y, BUTTON_H)
        local tagColor = faction:getTagColor()
        self.tagColorButton.backgroundColor = { r = tagColor:getR(), g = tagColor:getG(), b = tagColor:getB(), a = 1 }
        self.colorPicker = ISColorPicker:new(0, 0)
        self.colorPicker:initialise()
        self.colorPicker.pickedTarget = self
        self.colorPicker.resetFocusTo = self
        self.colorPicker:setInitialColor(tagColor)
    end
    y = y + BUTTON_H + 4
    self.levelLabel = addLabel(self, "", leftX, y, { r = 0.95, g = 0.72, b = 0.25 })
    self.limitLabel = addLabel(self, "", rightX, y)
    y = y + BUTTON_H + PAD
    self.progressY = y
    y = y + 30 + PAD

    addLabel(self, BClan.text("BClan_Members"), leftX, y)
    addLabel(self, BClan.text("BClan_Allies"), rightX, y)
    y = y + BUTTON_H
    self.memberList = self:addList(leftX, y, 330, BUTTON_H * 8)
    self.allyList = self:addList(rightX, y, columnW, BUTTON_H * 4)

    local memberBottom = self.memberList:getBottom()
    local allyY = self.allyList:getBottom() + PAD
    addLabel(self, BClan.text("BClan_AllyRequests"), rightX, allyY)
    self.requestList = self:addList(rightX, allyY + BUTTON_H, columnW, BUTTON_H * 3)

    local controlsY = memberBottom + PAD
    if self.canManage then
        self.memberEntry = addEntry(self, "", leftX, controlsY, 190)
        addButton(self, BClan.text("BClan_Invite"), "INVITE", leftX + 200, controlsY, 130)
        controlsY = controlsY + BUTTON_H + 6
        addButton(self, BClan.text("BClan_RemoveMember"), "REMOVE_MEMBER", leftX, controlsY, 160)
    end

    local rightControlsY = self.requestList:getBottom() + 6
    if self.canManage then
        addButton(self, BClan.text("BClan_Accept"), "ACCEPT_ALLY", rightX, rightControlsY, 100)
        addButton(self, BClan.text("BClan_Decline"), "DECLINE_ALLY", rightX + 110, rightControlsY, 100)
        rightControlsY = rightControlsY + BUTTON_H + 6
        self.allyEntry = addEntry(self, "", rightX, rightControlsY, columnW - 120)
        addButton(self, BClan.text("BClan_AddAlly"), "REQUEST_ALLY", rightX + columnW - 110, rightControlsY, 110)
        rightControlsY = rightControlsY + BUTTON_H + 6
        addButton(self, BClan.text("BClan_RemoveAlly"), "REMOVE_ALLY", rightX, rightControlsY, 150)
    end

    local bottomY = math.max(controlsY + BUTTON_H, rightControlsY + BUTTON_H) + PAD * 2
    if self.canManage then
        self.pvpButton = addButton(self, "", "TOGGLE_PVP", leftX, bottomY, 250)
    end
    if not self.owner then
        addButton(self, BClan.text("BClan_Leave"), "LEAVE", leftX + 265, bottomY, 130)
    end
    addButton(self, BClan.text("BClan_Ranking"), "RANKING", self.width - PAD - 220, bottomY, 108)
    addButton(self, BClan.text("BClan_Close"), "CLOSE", self.width - PAD - 100, bottomY, 100)
    self:setHeight(bottomY + BUTTON_H + PAD)
    self:refreshLists()
end

function BClanUI:refreshLists()
    local player, faction, entry = self:getCurrent()
    if not faction or not self.memberList then return end
    entry = entry or { level = 1, xp = 0, friendlyFire = false, allies = {}, pendingAllies = {} }

    self.memberList:clear()
    self.memberList:addItem(faction:getOwner() .. " " .. BClan.text("BClan_OwnerSuffix"), { name = faction:getOwner() })
    for i = 0, faction:getPlayers():size() - 1 do
        local name = faction:getPlayers():get(i)
        self.memberList:addItem(name, { name = name })
    end

    self.allyList:clear()
    for name, enabled in pairs(entry.allies or {}) do
        if enabled == true then self.allyList:addItem(name, { name = name }) end
    end

    self.requestList:clear()
    for name, enabled in pairs(entry.pendingAllies or {}) do
        if enabled == true then self.requestList:addItem(name, { name = name }) end
    end
end

function BClanUI:render()
    local player, faction, entry = self:getCurrent()
    local currentName = faction and faction:getName() or nil
    local invite = player and BClan.ClientData and BClan.ClientData.invites and BClan.ClientData.invites[player:getUsername()] ~= nil
    if currentName ~= self.factionName or (not faction and invite ~= self.hasInvite) then
        BClan.reopenWindow = true
        self:close()
        return
    end
    if not faction then return end
    entry = entry or { level = 1, xp = 0, friendlyFire = false }

    local owner = faction:getOwner()
    if self.renderedOwner ~= owner then
        self.renderedOwner = owner
        self.ownerLabel:setName(BClan.text("BClan_Owner") .. ": " .. owner)
    end

    local tag = tostring(faction:getTag() or "-")
    if self.renderedTag ~= tag then
        self.renderedTag = tag
        self.tagLabel:setName(BClan.text("BClan_Tag") .. ": [" .. tag .. "]")
    end

    local level = tonumber(entry.level) or 1
    if self.renderedLevel ~= level then
        self.renderedLevel = level
        self.levelLabel:setName(BClan.text("BClan_Level") .. ": " .. tostring(level))
        self.limitLabel:setName(BClan.text("BClan_MemberLimit") .. ": " .. tostring(BClan.memberLimit(level)))
    end

    local friendlyFire = entry.friendlyFire == true
    if self.pvpButton and self.renderedFriendlyFire ~= friendlyFire then
        self.renderedFriendlyFire = friendlyFire
        self.pvpButton:setTitle(friendlyFire and BClan.text("BClan_PvpOn") or BClan.text("BClan_PvpOff"))
    end
    if self.tagColorButton then
        local tagColor = faction:getTagColor()
        local r, g, b = tagColor:getR(), tagColor:getG(), tagColor:getB()
        if self.renderedTagR ~= r or self.renderedTagG ~= g or self.renderedTagB ~= b then
            self.renderedTagR, self.renderedTagG, self.renderedTagB = r, g, b
            self.tagColorButton.backgroundColor = { r = r, g = g, b = b, a = 1 }
        end
    end
end

function BClanUI:prerender()
    self:drawRect(0, 0, self.width, self.height, 0.92, 0.035, 0.035, 0.04)
    self:drawRectBorder(0, 0, self.width, self.height, 0.9, 0.42, 0.42, 0.42)
    local _, faction, entry = self:getCurrent()
    local title = faction and faction:getName() or BClan.text("BClan_Title")
    self:drawText(title, (self.width - getTextManager():MeasureStringX(UIFont.Medium, title)) / 2, PAD, 0.95, 0.95, 0.95, 1, UIFont.Medium)
    self:drawText(BClan.Config.ServerName, PAD, 8, 0.78, 0.18, 0.16, 1, UIFont.Small)

    if faction and self.progressY then
        entry = entry or { level = 1, xp = 0 }
        local level = tonumber(entry.level) or 1
        local needed = BClan.xpForLevel(level)
        local maxLevel = level >= BClan.Config.MaxLevel
        local ratio
        if maxLevel then
            ratio = ((tonumber(entry.xp) or 0) % BClan.Config.MaxLevelBarXP) / BClan.Config.MaxLevelBarXP
        else
            ratio = needed > 0 and math.min(1, (entry.xp or 0) / needed) or 0
        end
        local x = PAD
        local width = self.width - PAD * 2
        self:drawRect(x, self.progressY, width, 22, 0.8, 0.08, 0.08, 0.08)
        self:drawRect(x + 2, self.progressY + 2, (width - 4) * ratio, 18, 0.9, 0.58, 0.16, 0.12)
        self:drawRectBorder(x, self.progressY, width, 22, 0.8, 0.5, 0.5, 0.5)
        local xpText
        if maxLevel then
            xpText = BClan.text("BClan_MaxLevel") .. " | " .. BClan.text("BClan_RankingXP") .. ": " .. tostring(math.floor(entry.totalXP or 0))
        else
            xpText = tostring(math.floor(entry.xp or 0)) .. " / " .. tostring(needed) .. " XP"
        end
        self:drawText(xpText, x + (width - getTextManager():MeasureStringX(UIFont.Small, xpText)) / 2, self.progressY + 3, 1, 1, 1, 1, UIFont.Small)
    end
end

function BClanUI:onButton(button)
    if button.internal == "LANG_TR" or button.internal == "LANG_EN" then
        BClan.setLanguage(button.internal == "LANG_TR" and "TR" or "EN")
        self:close()
        BClanUI.toggle()
        return
    end
    if button.internal == "CLOSE" then return self:close() end
    if button.internal == "RANKING" then
        self:close()
        BClanRankingUI.open()
    elseif button.internal == "TAG_COLOR" then
        local _, faction = self:getCurrent()
        if not self.canManage or not faction or not self.colorPicker then return end
        self.colorPicker:setX(self.width - self.colorPicker:getWidth() - PAD)
        self.colorPicker:setY(button:getBottom() + 4)
        self.colorPicker.pickedFunc = BClanUI.onPickedTagColor
        self.colorPicker:setInitialColor(faction:getTagColor())
        self:addChild(self.colorPicker)
    elseif button.internal == "CREATE" then
        send("Create", { name = self.nameEntry:getInternalText(), tag = self.tagEntry:getInternalText() })
    elseif button.internal == "ACCEPT_INVITE" then
        send("RespondInvite", { accept = true })
    elseif button.internal == "DECLINE_INVITE" then
        send("RespondInvite", { accept = false })
    elseif button.internal == "INVITE" then
        send("Invite", { username = self.memberEntry:getInternalText() })
    elseif button.internal == "REMOVE_MEMBER" then
        local name = selectedName(self.memberList)
        if name then send("RemoveMember", { username = name }) end
    elseif button.internal == "LEAVE" then
        send("Leave")
    elseif button.internal == "TOGGLE_PVP" then
        local _, _, entry = self:getCurrent()
        send("SetPvp", { enabled = not (entry and entry.friendlyFire == true) })
    elseif button.internal == "REQUEST_ALLY" then
        send("RequestAlly", { clan = self.allyEntry:getInternalText() })
    elseif button.internal == "ACCEPT_ALLY" then
        local name = selectedName(self.requestList)
        if name then send("RespondAlly", { clan = name, accept = true }) end
    elseif button.internal == "DECLINE_ALLY" then
        local name = selectedName(self.requestList)
        if name then send("RespondAlly", { clan = name, accept = false }) end
    elseif button.internal == "REMOVE_ALLY" then
        local name = selectedName(self.allyList)
        if name then send("RemoveAlly", { clan = name }) end
    end
end

function BClanUI:onPickedTagColor(color, mouseUp)
    if not color or not self.tagColorButton then return end
    self.tagColorButton.backgroundColor = { r = color.r, g = color.g, b = color.b, a = 1 }
    if mouseUp ~= false then
        send("SetTagColor", { r = color.r, g = color.g, b = color.b })
    end
end

function BClanUI:close()
    self:setVisible(false)
    self:removeFromUIManager()
    if BClanUI.instance == self then BClanUI.instance = nil end
end

function BClanUI:new(x, y, width, height)
    local o = ISPanel:new(x, y, width, height)
    setmetatable(o, self)
    self.__index = self
    o.moveWithMouse = true
    o.backgroundColor = { r = 0, g = 0, b = 0, a = 0.9 }
    o.borderColor = { r = 0.4, g = 0.4, b = 0.4, a = 1 }
    BClanUI.instance = o
    return o
end

function BClanUI.toggle()
    if BClanRankingUI and BClanRankingUI.instance then
        BClanRankingUI.instance:close()
        return
    end
    if BClanUI.instance then
        BClanUI.instance:close()
        return
    end
    local width, height = 720, 620
    local panel = BClanUI:new((getCore():getScreenWidth() - width) / 2, (getCore():getScreenHeight() - height) / 2, width, height)
    panel:initialise()
    panel:addToUIManager()
end

BClanRankingUI = ISPanel:derive("BClanRankingUI")
BClanRankingUI.instance = nil

local function addRankingButton(parent, title, internal, x, y, width)
    local button = ISButton:new(x, y, width, BUTTON_H, title, parent, BClanRankingUI.onButton)
    button.internal = internal
    button:initialise()
    button:instantiate()
    button.borderColor = { r = 0.65, g = 0.65, b = 0.65, a = 0.55 }
    parent:addChild(button)
    return button
end

function BClanRankingUI:initialise()
    ISPanel.initialise(self)
    addLabel(self, BClan.text("BClan_RankingHint"), PAD, MEDIUM + PAD * 2, { r = 0.75, g = 0.75, b = 0.75 })
    self.rankingList = BClanUI.addList(self, PAD, MEDIUM + PAD * 2 + BUTTON_H, self.width - PAD * 2, self.height - 120)
    addRankingButton(self, BClan.text("BClan_Back"), "BACK", PAD, self.height - PAD - BUTTON_H, 120)
    addRankingButton(self, BClan.text("BClan_Close"), "CLOSE", self.width - PAD - 100, self.height - PAD - BUTTON_H, 100)
    self:refreshList()
end

function BClanRankingUI:refreshList()
    if not self.rankingList then return end
    self.rankingList:clear()
    local rankings = {}
    local clans = BClan.ClientData and BClan.ClientData.clans or {}
    for name, entry in pairs(clans) do
        table.insert(rankings, {
            name = name,
            level = tonumber(entry.level) or 1,
            totalXP = math.max(0, tonumber(entry.totalXP) or 0),
        })
    end
    table.sort(rankings, function(a, b)
        if a.totalXP == b.totalXP then return a.name:lower() < b.name:lower() end
        return a.totalXP > b.totalXP
    end)
    if #rankings == 0 then
        self.rankingList:addItem(BClan.text("BClan_RankingEmpty"), { name = BClan.text("BClan_RankingEmpty") })
        return
    end
    for index, clan in ipairs(rankings) do
        local text = "#" .. tostring(index) .. "  " .. clan.name .. "  |  " .. BClan.text("BClan_Level") .. ": " .. tostring(clan.level) .. "  |  " .. BClan.text("BClan_RankingXP") .. ": " .. tostring(math.floor(clan.totalXP))
        self.rankingList:addItem(text, { name = text })
    end
end

function BClanRankingUI:prerender()
    self:drawRect(0, 0, self.width, self.height, 0.94, 0.035, 0.035, 0.04)
    self:drawRectBorder(0, 0, self.width, self.height, 0.9, 0.42, 0.42, 0.42)
    local title = BClan.text("BClan_Ranking")
    self:drawText(title, (self.width - getTextManager():MeasureStringX(UIFont.Medium, title)) / 2, PAD, 0.95, 0.95, 0.95, 1, UIFont.Medium)
    self:drawText(BClan.Config.ServerName, PAD, 8, 0.78, 0.18, 0.16, 1, UIFont.Small)
end

function BClanRankingUI:onButton(button)
    if button.internal == "BACK" then
        self:close()
        BClanUI.toggle()
    elseif button.internal == "CLOSE" then
        self:close()
    end
end

function BClanRankingUI:close()
    self:setVisible(false)
    self:removeFromUIManager()
    if BClanRankingUI.instance == self then BClanRankingUI.instance = nil end
end

function BClanRankingUI:new(x, y, width, height)
    local o = ISPanel:new(x, y, width, height)
    setmetatable(o, self)
    self.__index = self
    o.moveWithMouse = true
    o.backgroundColor = { r = 0, g = 0, b = 0, a = 0.92 }
    o.borderColor = { r = 0.4, g = 0.4, b = 0.4, a = 1 }
    BClanRankingUI.instance = o
    return o
end

function BClanRankingUI.open()
    if BClanRankingUI.instance then return end
    local width, height = 680, 520
    local panel = BClanRankingUI:new((getCore():getScreenWidth() - width) / 2, (getCore():getScreenHeight() - height) / 2, width, height)
    panel:initialise()
    panel:addToUIManager()
    send("RequestData")
end
