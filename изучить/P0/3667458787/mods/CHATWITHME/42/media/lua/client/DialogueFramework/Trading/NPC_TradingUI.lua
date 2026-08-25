require "ISUI/ISPanel"

NPC_TradingUI_Simple = ISPanel:derive("NPC_TradingUI_Simple")

local NPC_TradingEngine = require("DialogueFramework/Trading/NPC_TradingEngine")
local NPC_TradingConfig = require("DialogueFramework/Trading/NPC_TradingConfig")
local NPC_DialogueEngine = require("DialogueFramework/Dialogue/NPC_DialogueEngine")

function NPC_TradingUI_Simple:new(player, session, npc)
    local WINDOW_WIDTH = NPC_TradingConfig.getSimpleUIWidth()
    local WINDOW_HEIGHT = NPC_TradingConfig.getSimpleUIHeight()

    local screenWidth = getCore():getScreenWidth()
    local screenHeight = getCore():getScreenHeight()

    local x = (screenWidth - WINDOW_WIDTH) / 2
    local y = (screenHeight - WINDOW_HEIGHT) / 2

    local o = ISPanel:new(x, y, WINDOW_WIDTH, WINDOW_HEIGHT)
    setmetatable(o, self)
    self.__index = self

    o.player = player
    o.session = session
    o.npc = npc
    o.tradingTexture = getTexture("media/ui/maindialogueui.png")

    o.tradeInProgress = false
    o.cachedFoundItems = nil
    o.cachedReward = nil

    o:setVisible(true)
    o:setAlwaysOnTop(true)

    return o
end

function NPC_TradingUI_Simple:initialise()
    ISPanel.initialise(self)
    self:createButtons()
end

function NPC_TradingUI_Simple:createButtons()
    local buttonWidth = 400
    local buttonHeight = 50
    local buttonX = (self.width - buttonWidth) / 2
    local buttonY1 = 70
    local buttonY2 = 135

    self.tradeButton = ISButton:new(
        buttonX, buttonY1, buttonWidth, buttonHeight,
        "Trade with NPC",
        self, NPC_TradingUI_Simple.onTradeClick
    )
    self.tradeButton:initialise()
    self:addChild(self.tradeButton)

    self.cancelButton = ISButton:new(
        buttonX, buttonY2, buttonWidth, buttonHeight,
        "Do Nothing",
        self, NPC_TradingUI_Simple.onCancelClick
    )
    self.cancelButton:initialise()
    self:addChild(self.cancelButton)
end

function NPC_TradingUI_Simple:onTradeClick()
    if self.tradeInProgress then
        return
    end

    local hasItems, foundItems = NPC_TradingEngine.hasAcceptableItems(self.player, 1)

    if not hasItems then
        return
    end

    self.tradeInProgress = true
    self.cachedFoundItems = foundItems

    local NPC_TradingItemTables = require("DialogueFramework/Trading/NPC_TradingItemTables")

    local itemCounts = {}
    for itemType, items in pairs(foundItems) do
        itemCounts[itemType] = items:size()
    end

    local totalValue = NPC_TradingItemTables.calculateTotalValue(itemCounts, 1)

    local rewardType, rewardQuantity = NPC_TradingItemTables.selectRewardItem(totalValue)
    self.cachedReward = {
        rewardType = rewardType,
        rewardQuantity = rewardQuantity
    }

    local NPC_GatherItemsTimedAction = require("DialogueFramework/Trading/NPC_GatherItemsTimedAction")
    local gatherAction = NPC_GatherItemsTimedAction:new(
        self.player,
        self.npc,
        self.cachedFoundItems,
        self.cachedReward,
        self
    )
    ISTimedActionQueue.add(gatherAction)
end

function NPC_TradingUI_Simple:onCancelClick()
    self:close()
end

function NPC_TradingUI_Simple:resetTradeState()
    self.tradeInProgress = false
    self.cachedFoundItems = nil
    self.cachedReward = nil
end

function NPC_TradingUI_Simple:close()
    NPC_DialogueEngine.endSession(self.player)

    self:setVisible(false)
    self:removeFromUIManager()
end

function NPC_TradingUI_Simple:render()
    ISPanel.render(self)

    if self.tradingTexture then
        self:drawTextureScaled(self.tradingTexture, 0, 0, self.width, self.height, 1.0, 1.0, 1.0, 1.0)
    end

    local npcName = self.session.sessionDef.displayName or "NPC"
    self:drawText(npcName, self.width - 100, 10, 1.0, 0.59, 0.2, 1.0, UIFont.Large)

    local currentNode = NPC_DialogueEngine.getCurrentNode(self.player)
    if currentNode and currentNode.npcText then
        self:drawText(currentNode.npcText, 20, 40, 1.0, 0.78, 0.39, 1.0, UIFont.Medium)
    end
end

NPC_TradingUI_Value = ISPanel:derive("NPC_TradingUI_Value")

local NPC_TradingItemTables = require("DialogueFramework/Trading/NPC_TradingItemTables")
local NPC_BackpackScanner = require("DialogueFramework/Trading/NPC_BackpackScanner")

function NPC_TradingUI_Value:new(player, session, npc)
    local WINDOW_WIDTH = NPC_TradingConfig.getValueUIWidth()
    local WINDOW_HEIGHT = NPC_TradingConfig.getValueUIHeight()

    local screenWidth = getCore():getScreenWidth()
    local screenHeight = getCore():getScreenHeight()

    local x = (screenWidth - WINDOW_WIDTH) / 2
    local y = (screenHeight - WINDOW_HEIGHT) / 2

    local o = ISPanel:new(x, y, WINDOW_WIDTH, WINDOW_HEIGHT)
    setmetatable(o, self)
    self.__index = self

    o.player = player
    o.session = session
    o.npc = npc
    o.tradingTexture = getTexture("media/ui/maindialogueui.png")

    o.acceptedItems = {}
    o.totalValue = 0
    o.selectedReward = nil

    o:setVisible(true)
    o:setAlwaysOnTop(true)

    NPC_BackpackScanner.registerTradingUI(player, o)

    return o
end

function NPC_TradingUI_Value:initialise()
    ISPanel.initialise(self)
    self:createTradeButton()
    NPC_TradingItemTables.loadRewardTextures()
end

function NPC_TradingUI_Value:createTradeButton()
    local buttonWidth = 200
    local buttonHeight = 40
    local buttonX = (self.width - buttonWidth) / 2
    local buttonY = self.height - 60

    self.tradeButton = ISButton:new(
        buttonX, buttonY, buttonWidth, buttonHeight,
        "Trade",
        self, NPC_TradingUI_Value.onTradeClick
    )
    self.tradeButton:initialise()
    self.tradeButton.enable = false
    self:addChild(self.tradeButton)
end

function NPC_TradingUI_Value:updateItemList(acceptedItems, totalValue)
    self.acceptedItems = acceptedItems
    self.totalValue = totalValue

    if totalValue > 0 then
        self.tradeButton.enable = true
    else
        self.tradeButton.enable = false
    end
end

function NPC_TradingUI_Value:onTradeClick()
    if not self.selectedReward then
        return
    end

    local NPC_DialogueSessionManager = require("DialogueFramework/Dialogue/NPC_DialogueSessionManager")
    local session = NPC_DialogueSessionManager.getSession(self.player)

    if not session or not session.tradingBackpack then
        return
    end

    local totalValue = session.tradingTotalValue or 0
    if totalValue < self.selectedReward.value then
        return
    end

    local selectedRewards = {
        {
            itemType = self.selectedReward.itemType,
            quantity = self.selectedReward.quantity
        }
    }

    local NPC_CompleteTradeTimedAction = require("DialogueFramework/Trading/NPC_CompleteTradeTimedAction")
    local tradeAction = NPC_CompleteTradeTimedAction:new(
        self.player,
        self.npc,
        session.tradingBackpack,
        selectedRewards,
        self
    )
    ISTimedActionQueue.add(tradeAction)
end

function NPC_TradingUI_Value:close()
    NPC_BackpackScanner.unregisterTradingUI(self.player)
    NPC_BackpackScanner.stopPeriodicScan(self.player)
    NPC_DialogueEngine.endSession(self.player)

    self:setVisible(false)
    self:removeFromUIManager()
end

function NPC_TradingUI_Value:render()
    ISPanel.render(self)

    if self.tradingTexture then
        self:drawTextureScaled(self.tradingTexture, 0, 0, self.width, self.height, 1.0, 1.0, 1.0, 1.0)
    end

    local npcName = self.session.sessionDef.displayName or "NPC"
    self:drawText(npcName, 20, 10, 1.0, 0.59, 0.2, 1.0, UIFont.Large)

    local valueText = "Value: " .. self.totalValue
    local valueColor = self.totalValue < 0 and {r=1.0, g=0.2, b=0.2} or {r=0.2, g=1.0, b=0.2}
    self:drawText(valueText, self.width - 150, 10, valueColor.r, valueColor.g, valueColor.b, 1.0, UIFont.Large)

    self:drawLine(self.width / 2, 50, self.width / 2, self.height - 100, 1.0, 1.0, 1.0, 1.0)

    self:drawText("Your Items", 20, 50, 1.0, 1.0, 1.0, 1.0, UIFont.Medium)
    self:drawText("Available Rewards", self.width / 2 + 20, 50, 1.0, 1.0, 1.0, 1.0, UIFont.Medium)

    self:renderAcceptedItems()
    self:renderAvailableRewards()

    if self.totalValue < 0 and self.tradeButton.enable then
        self:drawRectBorder(
            self.tradeButton.x - 2,
            self.tradeButton.y - 2,
            self.tradeButton.width + 4,
            self.tradeButton.height + 4,
            1.0, 1.0, 0.0, 0.0
        )
    end
end

function NPC_TradingUI_Value:renderAcceptedItems()
    local y = 80
    local iconSize = 32
    local spacing = 40

    for itemType, quantity in pairs(self.acceptedItems) do
        local itemData = NPC_TradingItemTables.acceptableItems_System2[itemType]
        if itemData then
            local item = InventoryItemFactory.CreateItem(itemType)
            if item then
                local texture = item:getNormalTexture()
                if texture then
                    self:drawTextureScaled(texture, 20, y, iconSize, iconSize, 1.0, 1.0, 1.0, 1.0)
                end
            end

            local text = itemData.displayName .. " x" .. quantity
            self:drawText(text, 60, y + 8, 1.0, 1.0, 1.0, 1.0, UIFont.Small)

            y = y + spacing
        end
    end
end

function NPC_TradingUI_Value:renderAvailableRewards()
    local y = 80
    local iconSize = 32
    local spacing = 40
    local leftOffset = self.width / 2 + 20

    local affordableRewards = NPC_TradingItemTables.getAffordableRewards(self.totalValue)

    for _, reward in ipairs(affordableRewards) do
        if reward.iconTexture then
            self:drawTextureScaled(reward.iconTexture, leftOffset, y, iconSize, iconSize, 1.0, 1.0, 1.0, 1.0)
        end

        local text = reward.displayName .. " (" .. reward.value .. " value)"
        self:drawText(text, leftOffset + 40, y + 8, 1.0, 1.0, 1.0, 1.0, UIFont.Small)

        y = y + spacing
    end
end

return {
    Simple = NPC_TradingUI_Simple,
    Value = NPC_TradingUI_Value
}
