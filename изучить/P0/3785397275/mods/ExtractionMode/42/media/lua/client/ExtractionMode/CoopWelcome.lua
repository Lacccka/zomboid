require "ISUI/ISPanelJoypad"
require "ISUI/ISButton"
require "ISUI/ISRichTextPanel"
require "ExtractionMode/Config"
require "ExtractionMode/Localization"

ExtractionMode = ExtractionMode or {}
local Config = ExtractionMode.Config
local Localization = ExtractionMode.Localization
local Welcome = {}
local Panel = ISPanelJoypad:derive("ExtractionModeCoopWelcomePanel")

local function acknowledge(player)
    if player == nil then return end
    local playerNum = player:getPlayerNum()
    local state = ExtractionMode.Client and ExtractionMode.Client.stateFor
        and ExtractionMode.Client.stateFor(playerNum) or ExtractionMode.ClientState
    if state then state.showCoopWelcome = false end
    local playerData = player:getModData()
    playerData.ExtractionModeCoopWelcomeVersion = Config.COOP_WELCOME_VERSION
    pcall(function() player:transmitModData() end)
    if ExtractionMode.Client and ExtractionMode.Client.sendCommand then
        ExtractionMode.Client.sendCommand(player, "AcknowledgeCoopWelcome", {})
    end
end

function Panel:createChildren()
    ISPanelJoypad.createChildren(self)
    local margin = 22
    self.richText = ISRichTextPanel:new(margin, 58, self.width - margin * 2, self.height - 122)
    self.richText:initialise()
    self.richText:instantiate()
    self.richText.background = false
    self.richText.autosetheight = false
    self.richText.clip = true
    self.richText.marginLeft = 4
    self.richText.marginRight = 4
    self.richText:setText(Localization.get("IGUI_ExtractionMode_CoopWelcome_Body",
        "<SIZE:small> Extraction Mode uses Project Zomboid factions to organize multiplayer teams."
        .. " <LINE> <LINE> - Players in the same faction share raid destinations, ready status, raid instances, quests, and contact trust."
        .. " <LINE> <LINE> - Players in different factions can form separate teams and raid different towns simultaneously."
        .. " <LINE> <LINE> - Players without a faction use their own personal raid and quest progress."
        .. " <LINE> <LINE> - Hideout upgrades, the generator, deliveries, and other hideout infrastructure remain shared across the server."
        .. " <LINE> <LINE> If you want to play and progress together, create or join the same faction before readying up for a raid. Use Project Zomboid's Factions menu to create, join, or manage your faction."
        .. " <LINE> <LINE> If you encounter issues, check the FAQ thread in the Steam Workshop discussions section. For more support, feel free to start a new discussion there."))
    self.richText:paginate()
    self:addChild(self.richText)

    self.okButton = ISButton:new((self.width - 150) / 2, self.height - 48, 150, 30,
        Localization.get("IGUI_ExtractionMode_CoopWelcome_GotIt", "GOT IT"),
        self, Panel.onAcknowledge)
    self.okButton:initialise()
    self.okButton:instantiate()
    self.okButton:enableAcceptColor()
    self:addChild(self.okButton)
end

function Panel:onAcknowledge()
    if self.closed then return end
    self.closed = true
    self:setVisible(false)
    self:removeFromUIManager()
    Welcome.instances[self.playerNum] = nil
    if isJoypadFocusOnElementOrDescendant(self.playerNum, self) then
        setJoypadFocus(self.playerNum, self.previousJoypadFocus)
    end
    acknowledge(self.player)
end

function Panel:onGainJoypadFocus(joypadData)
    ISPanelJoypad.onGainJoypadFocus(self, joypadData)
    self:setISButtonForA(self.okButton)
    self:setISButtonForB(self.okButton)
end

function Panel:isKeyConsumed(key)
    return self.playerNum == 0
        and (key == Keyboard.KEY_ESCAPE or key == Keyboard.KEY_RETURN)
end

function Panel:onKeyRelease(key)
    if self:isKeyConsumed(key) then self:onAcknowledge(); return true end
    return false
end

function Panel:prerender()
    self:stayOnSplitScreen(self.playerNum)
    ISPanelJoypad.prerender(self)
end

function Panel:render()
    ISPanelJoypad.render(self)
    self:drawTextCentre(Localization.get("IGUI_ExtractionMode_CoopWelcome_Title",
        "Welcome to Extraction Mode Co-op!"), self.width / 2, 17,
        0.96, 0.72, 0.18, 1, UIFont.Medium)
    self:drawRect(20, 49, self.width - 40, 1, 0.55, 0.82, 0.58, 0.16)
end

function Panel:new(playerNum, player)
    local screenLeft = getPlayerScreenLeft(playerNum)
    local screenTop = getPlayerScreenTop(playerNum)
    local screenWidth = getPlayerScreenWidth(playerNum)
    local screenHeight = getPlayerScreenHeight(playerNum)
    local width = math.min(620, math.max(440, screenWidth - 40))
    local height = math.min(420, math.max(360, screenHeight - 40))
    local object = ISPanelJoypad:new(
        math.floor(screenLeft + (screenWidth - width) / 2),
        math.floor(screenTop + (screenHeight - height) / 2), width, height)
    setmetatable(object, self)
    self.__index = self
    object.playerNum = playerNum
    object.player = player
    object.background = true
    object.backgroundColor = { r = 0.025, g = 0.03, b = 0.035, a = 0.97 }
    object.borderColor = { r = 0.82, g = 0.58, b = 0.16, a = 0.95 }
    object.moveWithMouse = true
    return object
end

Welcome.instances = Welcome.instances or {}

function Welcome.maybeShow(player, state)
    if player == nil or state == nil or state.showCoopWelcome ~= true then return false end
    local playerNum = player:getPlayerNum()
    local playerData = player:getModData()
    if (tonumber(playerData.ExtractionModeCoopWelcomeVersion) or 0)
        >= Config.COOP_WELCOME_VERSION then
        acknowledge(player)
        return false
    end
    if Welcome.instances[playerNum] then return false end

    local panel = Panel:new(playerNum, player)
    panel:initialise()
    panel:addToUIManager()
    panel:setAlwaysOnTop(true)
    panel:stayOnSplitScreen(playerNum)
    Welcome.instances[playerNum] = panel
    local joypadData = getJoypadData and getJoypadData(playerNum)
    if joypadData then
        panel.previousJoypadFocus = joypadData.focus
        setJoypadFocus(playerNum, panel)
    end
    return true
end

ExtractionMode.CoopWelcome = Welcome
return Welcome
