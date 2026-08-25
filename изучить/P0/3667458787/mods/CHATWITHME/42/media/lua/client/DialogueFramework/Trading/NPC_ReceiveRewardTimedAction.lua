require "TimedActions/ISBaseTimedAction"

NPC_ReceiveRewardTimedAction = ISBaseTimedAction:derive("NPC_ReceiveRewardTimedAction")

function NPC_ReceiveRewardTimedAction:new(player, npc, rewardType, rewardQuantity, tradingUI)
    local o = ISBaseTimedAction.new(self, player)
    o.npc = npc
    o.rewardType = rewardType
    o.rewardQuantity = rewardQuantity
    o.tradingUI = tradingUI
    o.maxTime = 100
    o.stopOnWalk = true
    o.stopOnRun = true
    return o
end

function NPC_ReceiveRewardTimedAction:isValid()
    if not self.npc or not self.npc:isExistInTheWorld() then
        return false
    end

    if not self.rewardType then
        return false
    end

    return true
end

function NPC_ReceiveRewardTimedAction:start()
    self:setActionAnim("Loot")
    if self.npc then
        self.character:faceThisObject(self.npc)
    end
end

function NPC_ReceiveRewardTimedAction:update()
end

function NPC_ReceiveRewardTimedAction:stop()
    ISBaseTimedAction.stop(self)
end

function NPC_ReceiveRewardTimedAction:perform()
    if isClient() then
        sendClientCommand(self.character, "NPCTrading", "AwardItems", {
            itemType = self.rewardType,
            quantity = self.rewardQuantity
        })
    else
        for i = 1, self.rewardQuantity do
            self.character:getInventory():AddItem(self.rewardType)
        end
    end

    ISBaseTimedAction.perform(self)

    if self.tradingUI then
        self.tradingUI:resetTradeState()
        self.tradingUI:close()
    end
end

return NPC_ReceiveRewardTimedAction
