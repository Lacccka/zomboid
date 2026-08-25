BanditSync = ISPanel:derive("BanditSync")

local FONT_HGT_SMALL = getTextManager():getFontHeight(UIFont.Small)
local FONT_HGT_MEDIUM = getTextManager():getFontHeight(UIFont.Medium)
local UI_BORDER_SPACING = 10
local BUTTON_HGT = FONT_HGT_SMALL + 6

function BanditSync:initialise()
    ISPanel.initialise(self)
    self.clientData = {}
    self.clientData.clanCount = 0
    self.clientData.banditCount = 0
    self.serverData = {}
    self.serverData.clanCount = 0
    self.serverData.banditCount = 0

    local lbl

    lbl = ISLabel:new(200, 60, BUTTON_HGT, getText("UI_BanditsCreator_Client"), 1, 1, 1, 1, UIFont.Medium, false)
    lbl:initialise()
    lbl:instantiate()
    self:addChild(lbl)

    lbl = ISLabel:new(320, 60, BUTTON_HGT, getText("UI_BanditsCreator_Server"), 1, 1, 1, 1, UIFont.Medium, false)
    lbl:initialise()
    lbl:instantiate()
    self:addChild(lbl)

    lbl = ISLabel:new(110, 100, BUTTON_HGT, getText("UI_BanditsCreator_Clans"), 1, 1, 1, 1, UIFont.Medium, false)
    lbl:initialise()
    lbl:instantiate()
    self:addChild(lbl)

    self.clientClanLbl = ISLabel:new(200, 100, BUTTON_HGT, "---", 1, 1, 1, 1, UIFont.Medium, false)
    self.clientClanLbl:initialise()
    self.clientClanLbl:instantiate()
    self:addChild(self.clientClanLbl)

    self.clientBanditLbl = ISLabel:new(200, 140, BUTTON_HGT, "---", 1, 1, 1, 1, UIFont.Medium, false)
    self.clientBanditLbl:initialise()
    self.clientBanditLbl:instantiate()
    self:addChild(self.clientBanditLbl)

    lbl = ISLabel:new(110, 140, BUTTON_HGT, getText("UI_BanditsCreator_Bandits"), 1, 1, 1, 1, UIFont.Medium, false)
    lbl:initialise()
    lbl:instantiate()
    self:addChild(lbl)

    self.serverClanLbl = ISLabel:new(320, 100, BUTTON_HGT, "---", 1, 1, 1, 1, UIFont.Medium, false)
    self.serverClanLbl:initialise()
    self.serverClanLbl:instantiate()
    self:addChild(self.serverClanLbl)

    self.serverBanditLbl = ISLabel:new(320, 140, BUTTON_HGT, "---", 1, 1, 1, 1, UIFont.Medium, false)
    self.serverBanditLbl:initialise()
    self.serverBanditLbl:instantiate()
    self:addChild(self.serverBanditLbl)

    local btnCancelWidth = 100 -- getTextManager():MeasureStringX(UIFont.Small, "Close") + 64
    local btnSaveWidth = 150 -- getTextManager():MeasureStringX(UIFont.Small, "Push To Server") + 64
    local btnCancelX = math.floor(self:getWidth() / 2) - ((btnCancelWidth + btnSaveWidth) / 2) - UI_BORDER_SPACING
    local btnSaveX = math.floor(self:getWidth() / 2) - ((btnCancelWidth + btnSaveWidth) / 2) + btnCancelWidth + UI_BORDER_SPACING

    self.cancel = ISButton:new(btnCancelX, self:getHeight() - UI_BORDER_SPACING - BUTTON_HGT - 1, btnCancelWidth, BUTTON_HGT, getText("UI_BanditsCreator_Close"), self, BanditSync.onClick)
    self.cancel.internal = "CLOSE"
    self.cancel.anchorTop = false
    self.cancel.anchorBottom = true
    self.cancel:initialise()
    self.cancel:instantiate()
    if BanditCompatibility.GetGameVersion() >= 42 then
        self.cancel:enableCancelColor()
    end
    self:addChild(self.cancel)

    self.save = ISButton:new(btnSaveX, self:getHeight() - UI_BORDER_SPACING - BUTTON_HGT - 1, btnSaveWidth, BUTTON_HGT, getText("UI_BanditsCreator_Push_To_Server"), self, BanditSync.onClick)
    self.save.internal = "SAVE"
    self.save.anchorTop = false
    self.save.anchorBottom = true
    self.save:initialise()
    self.save:instantiate()
    if BanditCompatibility.GetGameVersion() >= 42 then
        self.save:enableAcceptColor()
    end
    self:addChild(self.save)

    local stats = BanditCustom.GetStats()

    self.clientClanLbl:setName(tostring(stats.clanCount))
    self.clientBanditLbl:setName(tostring(stats.banditCount))

    sendClientCommand(getSpecificPlayer(0), "Custom", "SendStats", {a=1})
end

function BanditSync:onClick(button)

    if button.internal == "CLOSE" then
        self:removeFromUIManager()
        self:close()
    elseif button.internal == "SAVE" then
        BanditCustom.Load()
        local args = {}
        args.banditData = BanditCustom.banditData
        args.clanData = BanditCustom.clanData
        sendClientCommand(getSpecificPlayer(0), 'Custom', 'ReceiveFromClient', args)
        -- sendClientCommand(getSpecificPlayer(0), "Custom", "SendStats", {a=1})
    end
end

function BanditSync:update()
    ISPanel.update(self)
end

function BanditSync:prerender()
    ISPanel.prerender(self)
    self:drawTextCentre(getText("UI_BanditsCreator_Bandit_Sync"), self.width / 2, UI_BORDER_SPACING + 5, 1, 1, 1, 1, UIFont.Title)

    self:drawRectBorder(20, 95, self.width - 40, 1, 1, 0.5, 0.5, 0.5)
    self:drawRectBorder(20, 135, self.width - 40, 1, 1, 0.5, 0.5, 0.5)

    self:drawRectBorder(125, 60, 1, 115, 1, 0.5, 0.5, 0.5)
    self:drawRectBorder(240, 60, 1, 115, 1, 0.5, 0.5, 0.5)

end

function BanditSync:new(x, y, width, height, cid)
    local o = {}
    x = getCore():getScreenWidth() / 2 - (width / 2)
    y = getCore():getScreenHeight() / 2 - (height / 2)
    o = ISPanel:new(x, y, width, height)
    setmetatable(o, self)
    self.__index = self
    o.borderColor = {r=0.4, g=0.4, b=0.4, a=1}
    o.backgroundColor = {r=0, g=0, b=0, a=0.8}
    o.width = width
    o.height = height
    o.moveWithMouse = true
    o.cid = cid
    BanditSync.instance = o
    ISDebugMenu.RegisterClass(self)
    return o
end

local onServerCommand = function(module, command, args)
    if module == "Custom" then
        if command == "SendCustomStatsToClient" then
            if BanditSync.instance then
                BanditSync.instance.serverClanLbl:setName(tostring(args.clanCount))
                BanditSync.instance.serverBanditLbl:setName(tostring(args.banditCount))
            end
        end
    end
end

Events.OnServerCommand.Add(onServerCommand)