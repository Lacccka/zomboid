require "TimedActions/ISBaseTimedAction"

NPC_CompleteTradeTimedAction = ISBaseTimedAction:derive("NPC_CompleteTradeTimedAction")

function NPC_CompleteTradeTimedAction:new(player, npc, backpack, selectedRewards, tradingUI)
    local o = ISBaseTimedAction.new(self, player)
    o.npc = npc
    o.backpack = backpack
    o.selectedRewards = selectedRewards
    o.tradingUI = tradingUI
    o.maxTime = 150
    o.stopOnWalk = true
    o.stopOnRun = true
    return o
end

function NPC_CompleteTradeTimedAction:isValid()
    if not self.npc or not self.npc:isExistInTheWorld() then
        return false
    end

    if not self.backpack or not self.selectedRewards then
        return false
    end

    return true
end

function NPC_CompleteTradeTimedAction:start()
    self:setActionAnim("Loot")
    if self.npc then
        self.character:faceThisObject(self.npc)
    end
end

function NPC_CompleteTradeTimedAction:update()
end

function NPC_CompleteTradeTimedAction:stop()
    ISBaseTimedAction.stop(self)
end

function NPC_CompleteTradeTimedAction:perform()
    local NPC_DialogueSessionManager = require("DialogueFramework/Dialogue/NPC_DialogueSessionManager")
    local session = NPC_DialogueSessionManager.getSession(self.character)

    if session and session.backpackSquare then
        session.backpackSquare:removeItem(self.backpack)
        session.tradingBackpack = nil
        session.backpackSquare = nil
    end

    for _, reward in ipairs(self.selectedRewards) do
        if isClient() then
            sendClientCommand(self.character, "NPCTrading", "AwardItems", {
                itemType = reward.itemType,
                quantity = reward.quantity
            })
        else
            for i = 1, reward.quantity do
                self.character:getInventory():AddItem(reward.itemType)
            end
        end
    end

    ISBaseTimedAction.perform(self)

    if self.tradingUI then
        self.tradingUI:close()
    end
end

return NPC_CompleteTradeTimedAction
