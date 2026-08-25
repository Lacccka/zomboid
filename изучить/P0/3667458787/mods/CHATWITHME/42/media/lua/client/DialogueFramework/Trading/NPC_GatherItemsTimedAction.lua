require "TimedActions/ISBaseTimedAction"

NPC_GatherItemsTimedAction = ISBaseTimedAction:derive("NPC_GatherItemsTimedAction")

function NPC_GatherItemsTimedAction:new(player, npc, foundItems, cachedReward, tradingUI)
    local o = ISBaseTimedAction.new(self, player)
    o.npc = npc
    o.foundItems = foundItems
    o.cachedReward = cachedReward
    o.tradingUI = tradingUI
    o.maxTime = 150
    o.stopOnWalk = true
    o.stopOnRun = true
    return o
end

function NPC_GatherItemsTimedAction:isValid()
    if not self.npc or not self.npc:isExistInTheWorld() then
        return false
    end

    if not self.foundItems then
        return false
    end

    return true
end

function NPC_GatherItemsTimedAction:start()
    self:setActionAnim("Loot")
    if self.npc then
        self.character:faceThisObject(self.npc)
    end
end

function NPC_GatherItemsTimedAction:update()
end

function NPC_GatherItemsTimedAction:stop()
    ISBaseTimedAction.stop(self)
end

function NPC_GatherItemsTimedAction:perform()
    local NPC_TradingItemTables = require("DialogueFramework/Trading/NPC_TradingItemTables")
    local acceptableItems = NPC_TradingItemTables.getAcceptableItems(1)

    for itemType, items in pairs(self.foundItems) do
        local itemData = acceptableItems[itemType]
        if itemData then
            local takeAll = itemData.takeAll
            if takeAll == nil then
                takeAll = true
            end

            if takeAll then
                for i = 0, items:size() - 1 do
                    local item = items:get(i)
                    if item then
                        self.character:getInventory():Remove(item)
                    end
                end
            else
                local quantity = itemData.acceptQuantity or 1
                local removed = 0

                for i = 0, items:size() - 1 do
                    if removed >= quantity then
                        break
                    end

                    local item = items:get(i)
                    if item then
                        self.character:getInventory():Remove(item)
                        removed = removed + 1
                    end
                end
            end
        end
    end

    ISBaseTimedAction.perform(self)

    if self.cachedReward and self.cachedReward.rewardType then
        local NPC_ReceiveRewardTimedAction = require("DialogueFramework/Trading/NPC_ReceiveRewardTimedAction")
        local rewardAction = NPC_ReceiveRewardTimedAction:new(
            self.character,
            self.npc,
            self.cachedReward.rewardType,
            self.cachedReward.rewardQuantity,
            self.tradingUI
        )
        ISTimedActionQueue.add(rewardAction)
    else
        if self.tradingUI then
            self.tradingUI:resetTradeState()
            self.tradingUI:close()
        end
    end
end

return NPC_GatherItemsTimedAction
