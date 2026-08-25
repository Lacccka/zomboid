require "ISUI/ISPanelJoypad"
require "ISUI/ISButton"
require "ISUI/ISDPadWheels"
require "ExtractionMode/Config"
require "ExtractionMode/Localization"
require "ExtractionMode/Util"

ExtractionMode = ExtractionMode or {}

local Config = ExtractionMode.Config
local Localization = ExtractionMode.Localization
local Util = ExtractionMode.Util
local ControllerPanel = ISPanelJoypad:derive("ExtractionModeControllerPanel")

local function state(playerNum)
    if ExtractionMode.Client and ExtractionMode.Client.stateFor then
        return ExtractionMode.Client.stateFor(playerNum or 0)
    end
    return ExtractionMode.ClientState or {}
end

local function playerFor(playerNum)
    return getSpecificPlayer and getSpecificPlayer(playerNum or 0) or (getPlayer and getPlayer())
end

local function closeOtherPanels()
    local panels = {
        ExtractionMode.TownPickerInstance,
        ExtractionMode.UpgradePanelInstance,
        ExtractionMode.QuestPanelInstance,
    }
    for _, panel in pairs(panels) do
        if panel and panel:isVisible() and panel.onClose then
            panel.returnToController = false
            panel:onClose()
        end
    end
end

function ControllerPanel:close()
    if self.joyfocus and setJoypadFocus then setJoypadFocus(self.playerNum, nil) end
    self:setVisible(false)
    self:removeFromUIManager()
    if ExtractionMode.ControllerPanelInstances then
        ExtractionMode.ControllerPanelInstances[self.playerNum] = nil
    end
end

function ControllerPanel:onClose()
    self:close()
end

function ControllerPanel:openTown()
    local playerNum = self.playerNum
    self:close()
    ExtractionMode.openTownPicker(playerNum, true)
end

function ControllerPanel:toggleReady()
    local player = playerFor(self.playerNum)
    if player and ExtractionMode.Client then
        if state(self.playerNum).canJoinFactionRaid == true then
            ExtractionMode.Client.sendCommand(player, "JoinFactionRaid", {})
        elseif state(self.playerNum).canReady ~= false then
            ExtractionMode.Client.sendCommand(player, "SetReady", { ready = state(self.playerNum).selfReady ~= true })
        end
    end
    self:close()
end

function ControllerPanel:toggleOptOut()
    local player = playerFor(self.playerNum)
    if player and ExtractionMode.Client then
        ExtractionMode.Client.sendCommand(player, "SetOptOut", {
            optedOut = state(self.playerNum).selfOptedOut ~= true,
        })
    end
    self:close()
end

function ControllerPanel:openUpgrades()
    local playerNum = self.playerNum
    self:close()
    ExtractionMode.openUpgradePanel(playerNum, true)
end

function ControllerPanel:openContacts()
    local playerNum = self.playerNum
    self:close()
    ExtractionMode.openQuestPanel(playerNum, true)
end

function ControllerPanel:raidAction()
    local player = playerFor(self.playerNum)
    local data = state(self.playerNum)
    local client = ExtractionMode.Client
    if player == nil or client == nil then return end

    if data.state == Config.STATE_RAID and client.campaignQuestActive(data)
        and client.atCampaignHandoff(player, data) and data.campaignHandoffActive ~= true then
        if client.findVaccineSample(player) then
            client.sendCommand(player, "StartCampaignHandoff", {})
        else
            client.showMessage(Localization.get("IGUI_ExtractionMode_Error_VaccineSampleRequired",
                "Carry the Vaccine Sample onto the helipad before signaling."), true)
        end
    elseif data.state == Config.STATE_RAID and client.atExtractionSite
        and client.atExtractionSite(player) and client.findFlare(player) then
        if client.flareEquipped(player) then
            client.showMessage(Localization.get("IGUI_ExtractionMode_FireFlareHint",
                "Hold Aim, then press Attack to fire the extraction flare."), false)
        else
            client.equipFlare(player)
        end
    elseif data.state == Config.STATE_BOARDING and data.extractionRope
        and Util.playerNear(player, data.extractionRope, tonumber(data.extractionRope.radius) or 3)
        and data.boardingPendingSelf ~= true then
        if player:getVehicle() ~= nil then
            client.showMessage(Localization.get("IGUI_ExtractionMode_Error_BoardVehicle",
                "Exit the vehicle before boarding the extraction helicopter."), true)
        else
            client.boardExtraction(player)
        end
    end
    self:close()
end

function ControllerPanel:addAction(y, title, callback, enabled)
    local button = ISButton:new(18, y, self.width - 36, 34, title, self, callback)
    button:initialise()
    button:instantiate()
    if enabled == false then button.enable = false end
    self:addChild(button)
    return y + 42
end

function ControllerPanel:createChildren()
    ISPanelJoypad.createChildren(self)
    local data = state(self.playerNum)
    local player = playerFor(self.playerNum)
    local y = 54

    if data.state == Config.STATE_HIDEOUT then
        y = self:addAction(y, Localization.get("ContextMenu_ExtractionMode_ChooseDestination",
            "Choose Raid Destination..."), ControllerPanel.openTown)
        if data.selectedTownKey then
            y = self:addAction(y, data.canJoinFactionRaid == true
                and Localization.get("IGUI_ExtractionMode_JoinRaid", "Join Raid")
                or (data.vehicleInsertionActive == true and data.vehicleInsertionHasDriver ~= true
                    and Localization.get("IGUI_ExtractionMode_DriverRequired", "Driver Required")
                or (data.canReady == false
                    and Localization.get("IGUI_ExtractionMode_EnterInsertionVehicle", "Enter Insertion Vehicle")
                or (data.selfReady
                    and Localization.get("ContextMenu_ExtractionMode_CancelReady", "Cancel Raid Ready")
                    or Localization.get("ContextMenu_ExtractionMode_Ready", "Ready for Raid")))),
                ControllerPanel.toggleReady, data.canJoinFactionRaid == true or data.canReady ~= false)
            if data.canOptOut == true and data.canJoinFactionRaid ~= true then
                y = self:addAction(y, data.selfOptedOut == true
                    and Localization.get("IGUI_ExtractionMode_CancelOptOut", "Cancel Opt Out")
                    or Localization.get("IGUI_ExtractionMode_OptOut", "Opt Out"),
                    ControllerPanel.toggleOptOut)
            end
        end
    elseif data.state == Config.STATE_COUNTDOWN then
        y = self:addAction(y, data.vehicleInsertionActive == true
            and data.vehicleInsertionHasDriver ~= true
            and Localization.get("IGUI_ExtractionMode_DriverRequired", "Driver Required")
            or (data.canReady == false
            and Localization.get("IGUI_ExtractionMode_EnterInsertionVehicle", "Enter Insertion Vehicle")
            or (data.selfReady
            and Localization.get("ContextMenu_ExtractionMode_CancelReady", "Cancel Raid Ready")
            or Localization.get("ContextMenu_ExtractionMode_Ready", "Ready for Raid"))),
            ControllerPanel.toggleReady, data.canReady ~= false)
        if data.canOptOut == true then
            y = self:addAction(y, data.selfOptedOut == true
                and Localization.get("IGUI_ExtractionMode_CancelOptOut", "Cancel Opt Out")
                or Localization.get("IGUI_ExtractionMode_OptOut", "Opt Out"),
                ControllerPanel.toggleOptOut)
        end
    elseif data.state == Config.STATE_RAID then
        local canCampaign = player and ExtractionMode.Client
            and ExtractionMode.Client.campaignQuestActive(data)
            and ExtractionMode.Client.atCampaignHandoff(player, data)
            and data.campaignHandoffActive ~= true
        local canFlare = player and ExtractionMode.Client and ExtractionMode.Client.atExtractionSite
            and ExtractionMode.Client.atExtractionSite(player) and ExtractionMode.Client.findFlare(player)
        if canCampaign then
            y = self:addAction(y, Localization.get("ContextMenu_ExtractionMode_SignalVaccineHelicopter",
                "Signal Vaccine Helicopter"), ControllerPanel.raidAction)
        elseif canFlare then
            y = self:addAction(y, ExtractionMode.Client.flareEquipped(player)
                and Localization.get("IGUI_ExtractionMode_FireFlareHintShort", "Flare Ready - Aim and Fire")
                or Localization.get("IGUI_ExtractionMode_EquipFlare", "Equip Extraction Flare"),
                ControllerPanel.raidAction)
        end
    elseif data.state == Config.STATE_BOARDING and player and data.extractionRope
        and Util.playerNear(player, data.extractionRope, tonumber(data.extractionRope.radius) or 3)
        and data.boardingPendingSelf ~= true then
        y = self:addAction(y, Localization.get("IGUI_ExtractionMode_BoardExtraction",
            "Board Extraction Helicopter"), ControllerPanel.raidAction)
    end

    y = self:addAction(y, data.state == Config.STATE_RAID
        and Localization.get("IGUI_ExtractionMode_UpgradeReference", "Upgrade Reference")
        or Localization.get("ContextMenu_ExtractionMode_HideoutUpgrades", "Hideout Upgrades..."),
        ControllerPanel.openUpgrades)
    y = self:addAction(y, data.state == Config.STATE_RAID
        and Localization.get("IGUI_ExtractionMode_QuestReference", "Quest Reference")
        or Localization.get("ContextMenu_ExtractionMode_Contacts", "Contacts..."),
        ControllerPanel.openContacts)

    self.closeButton = ISButton:new(18, y + 4, self.width - 36, 30,
        Localization.get("IGUI_ExtractionMode_Close", "CLOSE"), self, ControllerPanel.onClose)
    self.closeButton:initialise()
    self.closeButton:instantiate()
    self:addChild(self.closeButton)
    self:setHeight(y + 52)
    self:setISButtonForB(self.closeButton)
    self:autoGenerateJoypadButtonsLists()
end

function ControllerPanel:prerender()
    self:stayOnSplitScreen(self.playerNum)
    ISPanelJoypad.prerender(self)
end

function ControllerPanel:render()
    ISPanelJoypad.render(self)
    self:drawTextCentre(Localization.get("IGUI_ExtractionMode_Title", "EXTRACTION MODE"),
        self.width / 2, 15, 0.96, 0.72, 0.18, 1, UIFont.Medium)
end

function ControllerPanel:new(playerNum)
    playerNum = tonumber(playerNum) or 0
    local width = 390
    local height = 250
    local screenLeft = getPlayerScreenLeft(playerNum)
    local screenTop = getPlayerScreenTop(playerNum)
    local screenWidth = getPlayerScreenWidth(playerNum)
    local screenHeight = getPlayerScreenHeight(playerNum)
    local x = math.floor(screenLeft + (screenWidth - width) / 2)
    local y = math.floor(screenTop + (screenHeight - height) / 2)
    local object = ISPanelJoypad:new(x, y, width, height)
    setmetatable(object, self)
    self.__index = self
    object.playerNum = playerNum
    object.background = true
    object.backgroundColor = { r = 0.025, g = 0.03, b = 0.035, a = 0.96 }
    object.borderColor = { r = 0.82, g = 0.58, b = 0.16, a = 0.9 }
    object.moveWithMouse = true
    return object
end

ExtractionMode.ControllerPanelInstances = ExtractionMode.ControllerPanelInstances or {}

function ExtractionMode.openControllerPanel(playerNum)
    playerNum = tonumber(playerNum) or 0
    closeOtherPanels()
    local old = ExtractionMode.ControllerPanelInstances[playerNum]
    if old then old:close() end
    local panel = ControllerPanel:new(playerNum)
    panel:initialise()
    panel:addToUIManager()
    panel:setAlwaysOnTop(true)
    panel:stayOnSplitScreen(playerNum)
    ExtractionMode.ControllerPanelInstances[playerNum] = panel
    if getJoypadData and getJoypadData(playerNum) then setJoypadFocus(playerNum, panel) end
    return panel
end

local function appendExtractionSlice(joypadData)
    local playerNum = joypadData and joypadData.player
    if playerNum == nil or playerFor(playerNum) == nil then return end
    local menu = getPlayerRadialMenu(playerNum)
    if menu == nil then return end
    menu:addSlice(Localization.get("IGUI_ExtractionMode_Title", "Extraction Mode"),
        getTexture("media/textures/worldMap/Map_On.png"), ExtractionMode.openControllerPanel, playerNum)
    if ExtractionMode.ControllerRadialLogged ~= true then
        ExtractionMode.ControllerRadialLogged = true
        Util.log("Controller radial extension active on D-pad Right")
    end
end

-- Build 42 has no public event for extending this wheel. Chain the vanilla
-- builder and append one slice after it has preserved all built-in entries.
if ISDPadWheels and ISDPadWheels.ExtractionModeRadialWrapped ~= true then
    local previousOnDisplayRight = ISDPadWheels.onDisplayRight
    ISDPadWheels.onDisplayRight = function(joypadData)
        local isPaused = UIManager.getSpeedControls()
            and UIManager.getSpeedControls():getCurrentGameSpeed() == 0
        previousOnDisplayRight(joypadData)
        if not isPaused then appendExtractionSlice(joypadData) end
    end
    ISDPadWheels.ExtractionModeRadialWrapped = true
end

ExtractionMode.ControllerPanel = ControllerPanel
return ControllerPanel
