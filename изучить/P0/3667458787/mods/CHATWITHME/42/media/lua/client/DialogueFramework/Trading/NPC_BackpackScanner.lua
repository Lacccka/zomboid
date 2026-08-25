local NPC_BackpackScanner = {}

local NPC_TradingItemTables = require("DialogueFramework/Trading/NPC_TradingItemTables")
local NPC_DialogueSessionManager = require("DialogueFramework/Dialogue/NPC_DialogueSessionManager")

local activeTradingUIs = {}

function NPC_BackpackScanner.scanBackpack(player, backpack)
    if not backpack or not player then
        return
    end

    local session = NPC_DialogueSessionManager.getSession(player)
    if not session then
        return
    end

    local backpackInventory = backpack:getInventory()
    if not backpackInventory then
        return
    end

    local items = backpackInventory:getItems()

    local acceptedItems = {}
    local totalValue = 0

    for i = 0, items:size() - 1 do
        local item = items:get(i)
        local itemType = item:getFullType()

        if NPC_TradingItemTables.isAcceptableItem(itemType, 2) then
            if not acceptedItems[itemType] then
                acceptedItems[itemType] = 0
            end
            acceptedItems[itemType] = acceptedItems[itemType] + 1

            local value = NPC_TradingItemTables.getItemValue(itemType, 2)
            totalValue = totalValue + value
        end
    end

    session.tradingAcceptedItems = acceptedItems
    session.tradingTotalValue = totalValue

    NPC_BackpackScanner.updateTradingUI(player, acceptedItems, totalValue)
end

function NPC_BackpackScanner.updateTradingUI(player, acceptedItems, totalValue)
    local playerID = player:getOnlineID()
    local tradingUI = activeTradingUIs[playerID]

    if tradingUI and tradingUI.updateItemList then
        tradingUI:updateItemList(acceptedItems, totalValue)
    end
end

function NPC_BackpackScanner.registerTradingUI(player, tradingUI)
    local playerID = player:getOnlineID()
    activeTradingUIs[playerID] = tradingUI
end

function NPC_BackpackScanner.unregisterTradingUI(player)
    local playerID = player:getOnlineID()
    activeTradingUIs[playerID] = nil
end

function NPC_BackpackScanner.startPeriodicScan(player, backpack)
    local session = NPC_DialogueSessionManager.getSession(player)
    if not session then
        return
    end

    session.scanBackpack = backpack
    session.lastScanTime = getGameTime():getMinutes()
    session.scanInterval = 10
end

function NPC_BackpackScanner.updatePeriodicScan(player)
    local session = NPC_DialogueSessionManager.getSession(player)
    if not session or not session.scanBackpack then
        return
    end

    local currentTime = getGameTime():getMinutes()
    local timeSinceLastScan = currentTime - session.lastScanTime

    if timeSinceLastScan >= session.scanInterval then
        NPC_BackpackScanner.scanBackpack(player, session.scanBackpack)
        session.lastScanTime = currentTime
    end
end

function NPC_BackpackScanner.stopPeriodicScan(player)
    local session = NPC_DialogueSessionManager.getSession(player)
    if session then
        session.scanBackpack = nil
        session.lastScanTime = nil
    end
end

return NPC_BackpackScanner
