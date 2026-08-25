require "ISUI/ISPanel"
require "ISUI/ISPanelJoypad"
require "ISUI/ISButton"
require "ExtractionMode/Config"
require "ExtractionMode/Localization"

ExtractionMode = ExtractionMode or {}
local Config = ExtractionMode.Config
local Picker = ISPanelJoypad:derive("ExtractionModeTownPicker")
local Localization = ExtractionMode.Localization

function Picker:onSelect(button)
    local player = getSpecificPlayer and getSpecificPlayer(self.playerNum or 0) or (getPlayer and getPlayer())
    if player and button and button.townKey and ExtractionMode.Client then
        ExtractionMode.Client.sendCommand(player, "SelectTown", {
            townKey = button.townKey,
            activeRaidKey = button.activeRaidKey,
        })
    end
    self:close(false)
end

function Picker:onClose()
    self:close(true)
end

function Picker:close(goBack)
    local returnToController = goBack == true and self.returnToController == true
    local playerNum = self.playerNum or 0
    self.returnToController = false
    if self.joyfocus and setJoypadFocus then setJoypadFocus(self.playerNum or 0, nil) end
    self:setVisible(false)
    self:removeFromUIManager()
    if ExtractionMode.TownPickerInstance == self then ExtractionMode.TownPickerInstance = nil end
    if returnToController and ExtractionMode.openControllerPanel then
        ExtractionMode.openControllerPanel(playerNum)
    end
end

function Picker:createChildren()
    ISPanelJoypad.createChildren(self)
    local state = ExtractionMode.Client and ExtractionMode.Client.stateFor
        and ExtractionMode.Client.stateFor(self.playerNum or 0) or ExtractionMode.ClientState or {}
    local towns = state.townChoices or {}
    local buttonWidth = 238
    local buttonHeight = 36
    local buttonStartY = 92
    local buttonRowSpacing = 44
    for index, town in ipairs(towns) do
        local column = (index - 1) % 2
        local row = math.floor((index - 1) / 2)
        local x = 14 + column * 248
        local y = buttonStartY + row * buttonRowSpacing
        local label = Config.townDisplayName(town.key, town.name)
        local definition = Config.town(town.key)
        if definition == nil or definition.unlockQuestId == nil then
            label = Localization.get("IGUI_ExtractionMode_TownChoice", "%1  |  %2",
                label, Config.townSize(town.key, town.size))
        end
        if town.activeFactionRaid == true then
            label = Localization.get("IGUI_ExtractionMode_TownChoiceActiveRaid",
                "%1  |  FACTION RAID #%2 ACTIVE", label, tostring(town.activeRaidId or "?"))
        end
        local button = ISButton:new(x, y, buttonWidth, buttonHeight, label, self, Picker.onSelect)
        button:initialise()
        button:instantiate()
        button.townKey = town.key
        button.activeRaidKey = town.activeRaidKey
        button.activeRaidId = town.activeRaidId
        if town.activeFactionRaid == true then
            button.borderColor = { r = 0.20, g = 0.55, b = 1.00, a = 1.00 }
        end
        if state.selectedTownKey == town.key
            and tostring(state.selectedJoinRaidKey or "")
                == tostring(town.activeRaidKey or "") then
            button.backgroundColor = { r = 0.28, g = 0.20, b = 0.06, a = 1 }
        end
        self:addChild(button)
    end

    self.closeButton = ISButton:new(14, self.height - 43, self.width - 28, 29,
        Localization.get("IGUI_ExtractionMode_Cancel", "CANCEL"), self, Picker.onClose)
    self.closeButton:initialise()
    self.closeButton:instantiate()
    self:addChild(self.closeButton)
    self:autoGenerateJoypadButtonsLists()
end

function Picker:prerender()
    self:stayOnSplitScreen(self.playerNum or 0)
    ISPanelJoypad.prerender(self)
end

function Picker:render()
    ISPanel.render(self)
    self:drawTextCentre(Localization.get("IGUI_ExtractionMode_SelectRaidDestination",
        "SELECT RAID DESTINATION"), self.width / 2, 13, 0.96, 0.72, 0.18, 1, UIFont.Medium)
    local state = ExtractionMode.Client and ExtractionMode.Client.stateFor
        and ExtractionMode.Client.stateFor(self.playerNum or 0) or ExtractionMode.ClientState or {}
    local count = #(state.townChoices or {})
    self:drawTextCentre(Localization.get("IGUI_ExtractionMode_AvailableDestinations",
        "Today's %1 available destinations.\nChanging town clears your team's ready status.", tostring(count)),
        self.width / 2, 44, 0.82, 0.82, 0.82, 1, UIFont.Small)
end

function Picker:new(playerNum)
    playerNum = tonumber(playerNum) or 0
    local width = 510
    local height = 356
    local screenLeft = getPlayerScreenLeft(playerNum)
    local screenTop = getPlayerScreenTop(playerNum)
    local screenWidth = getPlayerScreenWidth(playerNum)
    local screenHeight = getPlayerScreenHeight(playerNum)
    local x = math.floor(screenLeft + (screenWidth - width) / 2)
    local y = math.floor(screenTop + (screenHeight - height) / 2)
    local object = ISPanelJoypad:new(x, y, width, height)
    setmetatable(object, self)
    self.__index = self
    object.background = true
    object.backgroundColor = { r = 0.03, g = 0.035, b = 0.04, a = 0.96 }
    object.borderColor = { r = 0.82, g = 0.58, b = 0.16, a = 0.9 }
    object.moveWithMouse = true
    object.playerNum = playerNum
    return object
end

function ExtractionMode.openTownPicker(playerNum, returnToController)
    playerNum = tonumber(playerNum) or 0
    if ExtractionMode.TownPickerInstance then
        ExtractionMode.TownPickerInstance:close()
    end
    local picker = Picker:new(playerNum)
    picker:initialise()
    picker:addToUIManager()
    picker:setAlwaysOnTop(true)
    picker.returnToController = returnToController == true
    picker:clearISButtonB()
    if picker.returnToController then picker:setISButtonForB(picker.closeButton) end
    if getJoypadData and getJoypadData(playerNum) then setJoypadFocus(playerNum, picker) end
    ExtractionMode.TownPickerInstance = picker
    return picker
end

function ExtractionMode.toggleTownPicker(playerNum)
    local picker = ExtractionMode.TownPickerInstance
    if picker ~= nil and picker:isVisible() then
        picker:close()
        return nil
    end
    return ExtractionMode.openTownPicker(playerNum)
end

ExtractionMode.TownPicker = Picker
return Picker
