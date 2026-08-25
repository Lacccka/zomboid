require "ISUI/ISPanel"
require "ISUI/ISButton"
require "ISUI/ISScrollingListBox"
require "ExtractionMode/Config"

ExtractionMode = ExtractionMode or {}
local Config = ExtractionMode.Config
local Panel = ISPanel:derive("ExtractionModeDebugPanel")

local function state()
    return ExtractionMode.ClientState or {}
end

local function localPlayer()
    return getSpecificPlayer and getSpecificPlayer(0) or (getPlayer and getPlayer())
end

local function send(command, args)
    local player = localPlayer()
    if player and ExtractionMode.Client and ExtractionMode.Client.sendCommand then
        ExtractionMode.Client.sendCommand(player, command, args or {})
    end
end

local function raidActive(data)
    return data.state == Config.STATE_RAID or data.state == Config.STATE_EXTRACTING
        or data.state == Config.STATE_BOARDING
end

local function clanLabel(clan)
    local firstDay = math.max(0, math.floor(tonumber(clan.dayStart) or 0))
    local lastDay = math.max(firstDay, math.floor(tonumber(clan.dayEnd) or firstDay))
    local window = lastDay >= 10000 and ("day " .. tostring(firstDay) .. "+")
        or ("days " .. tostring(firstDay) .. "-" .. tostring(lastDay))
    return tostring(clan.name or clan.cid or "Unknown Clan") .. "  |  " .. window
end

function Panel:close()
    self:setVisible(false)
    self:removeFromUIManager()
    if ExtractionMode.DebugPanelInstance == self then ExtractionMode.DebugPanelInstance = nil end
end

function Panel:onClose()
    self:close()
end

function Panel:onExtractionHorde()
    send("DebugSpawnExtractionHorde", {})
end

function Panel:onLateHorde()
    send("DebugSpawnLateRaidHorde", {})
end

function Panel:onExtractionHere()
    send("DebugBeginExtractionHere", {})
end

function Panel:onExtractAllPlayers()
    send("DebugExtractAllPlayers", {})
end

function Panel:onResetFinalQuest()
    send("DebugResetFinalQuest", {})
end

function Panel:onRefreshTownChoices()
    send("DebugRefreshTownChoices", {})
end

function Panel:onGarage()
    if ExtractionMode.openGaragePanel then ExtractionMode.openGaragePanel() end
end

function Panel:onBanditRaid()
    local selected = self.clanList.items[self.clanList.selected]
    local clan = selected and selected.item or {}
    send("DebugSpawnBanditRaid", { clanId = clan.cid or "" })
end

function Panel:createChildren()
    ISPanel.createChildren(self)

    self.extractAllButton = ISButton:new(16, 76, 282, 32,
        "EXTRACT ALL PLAYERS NOW", self, Panel.onExtractAllPlayers)
    self.extractAllButton:initialise()
    self.extractAllButton:instantiate()
    self.extractAllButton:setTooltip("Immediately return every active raid participant to the hideout, save occupied raid vehicles to their drivers' garages, and close the raid.")
    self:addChild(self.extractAllButton)

    self.finalQuestButton = ISButton:new(306, 76, 298, 32,
        "RESET TO FINAL QUEST", self, Panel.onResetFinalQuest)
    self.finalQuestButton:initialise()
    self.finalQuestButton:instantiate()
    self.finalQuestButton:setTooltip("Mark every earlier quest complete and reset the current quest group to One Last Flight.")
    self:addChild(self.finalQuestButton)

    self.refreshTownsButton = ISButton:new(16, 116, 588, 32,
        "REFRESH DAILY RAID DESTINATIONS", self, Panel.onRefreshTownChoices)
    self.refreshTownsButton:initialise()
    self.refreshTownsButton:instantiate()
    self.refreshTownsButton:setTooltip("Reroll today's available raid destinations without advancing time or creating daily deliveries.")
    self:addChild(self.refreshTownsButton)

    self.garageButton = ISButton:new(16, 156, 588, 32,
        "OPEN PERSONAL GARAGE", self, Panel.onGarage)
    self.garageButton:initialise()
    self.garageButton:instantiate()
    self.garageButton:setTooltip("Open the garage controls until the physical hideout garage interaction is authored.")
    self:addChild(self.garageButton)

    self.extractionHordeButton = ISButton:new(16, 236, 282, 32,
        "SPAWN EXTRACTION HORDE", self, Panel.onExtractionHorde)
    self.extractionHordeButton:initialise()
    self.extractionHordeButton:instantiate()
    self.extractionHordeButton:setTooltip("Spawn the configured extraction-response horde around your character.")
    self:addChild(self.extractionHordeButton)

    self.lateHordeButton = ISButton:new(306, 236, 298, 32,
        "SPAWN LATE-RAID HORDE", self, Panel.onLateHorde)
    self.lateHordeButton:initialise()
    self.lateHordeButton:instantiate()
    self.lateHordeButton:setTooltip("Force another configured major raid horde, even if one already arrived.")
    self:addChild(self.lateHordeButton)

    self.extractionHereButton = ISButton:new(16, 276, 588, 32,
        "BEGIN EXTRACTION AT CURRENT LOCATION", self, Panel.onExtractionHere)
    self.extractionHereButton:initialise()
    self.extractionHereButton:instantiate()
    self.extractionHereButton:setTooltip("Create a temporary extraction site here and start the complete helicopter sequence without a flare.")
    self:addChild(self.extractionHereButton)

    self.clanList = ISScrollingListBox:new(16, 370, 588, 176)
    self.clanList:initialise()
    self.clanList:instantiate()
    self.clanList.font = UIFont.Small
    self.clanList.itemheight = 26
    self.clanList.drawBorder = true
    self.clanList:addItem("Random era-appropriate hostile clan", { cid = "" })
    for _, clan in ipairs(state().debugBanditClans or {}) do
        self.clanList:addItem(clanLabel(clan), clan)
    end
    self.clanList.selected = 1
    self:addChild(self.clanList)

    self.banditButton = ISButton:new(16, 557, 588, 32,
        "SPAWN SELECTED BANDIT RAID", self, Panel.onBanditRaid)
    self.banditButton:initialise()
    self.banditButton:instantiate()
    self.banditButton:setTooltip("Spawn one configured hostile Bandits group using the selected clan.")
    self:addChild(self.banditButton)

    self.closeButton = ISButton:new(16, 597, 588, 28, "CLOSE", self, Panel.onClose)
    self.closeButton:initialise()
    self.closeButton:instantiate()
    self:addChild(self.closeButton)
end

function Panel:prerender()
    local data = state()
    if data.debugEnabled ~= true then self:close(); return end
    local usable = raidActive(data) and data.isParticipant == true
    local hideoutUsable = data.state == Config.STATE_HIDEOUT
        and localPlayer() ~= nil and not localPlayer():isDead()
    self.extractAllButton.enable = usable
    self.finalQuestButton.enable = hideoutUsable
    self.refreshTownsButton.enable = hideoutUsable
    self.garageButton.enable = true
    self.extractionHordeButton.enable = usable
    self.lateHordeButton.enable = usable
    self.extractionHereButton.enable = usable and data.state == Config.STATE_RAID
    self.banditButton.enable = usable and data.debugBanditsAvailable == true
        and self.clanList.items[self.clanList.selected] ~= nil
    ISPanel.prerender(self)
end

function Panel:render()
    ISPanel.render(self)
    self:drawTextCentre("EXTRACTION MODE DEBUG TOOLS", self.width / 2, 14,
        0.96, 0.72, 0.18, 1, UIFont.Medium)
    self:drawTextCentre("Server-authoritative test actions. Spawned units remain part of the current raid cleanup.",
        self.width / 2, 43, 0.82, 0.82, 0.82, 1, UIFont.Small)
    self:drawText("RAID ENCOUNTERS", 16, 206, 0.96, 0.72, 0.18, 1, UIFont.Small)
    self:drawText("BANDIT RAID CLAN", 16, 326, 0.96, 0.72, 0.18, 1, UIFont.Small)
    local status = state().debugBanditsAvailable == true
        and "Choose a hostile assault clan, or use the current era's weighted random selection."
        or "Bandits2 server API is unavailable; zombie and extraction tools still work."
    self:drawText(status, 16, 347, state().debugBanditsAvailable == true and 0.78 or 0.95,
        state().debugBanditsAvailable == true and 0.78 or 0.45,
        state().debugBanditsAvailable == true and 0.78 or 0.30, 1, UIFont.Small)
end

function Panel:new()
    local width = 620
    local height = 640
    local x = math.floor((getCore():getScreenWidth() - width) / 2)
    local y = math.max(10, math.floor((getCore():getScreenHeight() - height) / 2))
    local object = ISPanel:new(x, y, width, height)
    setmetatable(object, self)
    self.__index = self
    object.background = true
    object.backgroundColor = { r = 0.025, g = 0.03, b = 0.035, a = 0.97 }
    object.borderColor = { r = 0.82, g = 0.32, b = 0.16, a = 0.9 }
    object.moveWithMouse = true
    return object
end

function ExtractionMode.openDebugPanel()
    if state().debugEnabled ~= true then return nil end
    if ExtractionMode.DebugPanelInstance then ExtractionMode.DebugPanelInstance:close() end
    local panel = Panel:new()
    panel:initialise()
    panel:addToUIManager()
    panel:setAlwaysOnTop(true)
    ExtractionMode.DebugPanelInstance = panel
    return panel
end

ExtractionMode.DebugPanel = Panel
return Panel
