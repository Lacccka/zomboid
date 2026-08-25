require "TimedActions/ISBaseTimedAction"

NPC_ReceiveGiftTimedAction = ISBaseTimedAction:derive("NPC_ReceiveGiftTimedAction")

function NPC_ReceiveGiftTimedAction:new(player, npc, npcID, giftItems)
    local o = ISBaseTimedAction.new(self, player)
    o.npc = npc
    o.npcID = npcID
    o.giftItems = giftItems
    o.maxTime = 100
    o.stopOnWalk = true
    o.stopOnRun = true
    return o
end

function NPC_ReceiveGiftTimedAction:isValid()
    if not self.npc or not self.npc:isExistInTheWorld() then
        return false
    end

    if not self.giftItems or #self.giftItems == 0 then
        return false
    end

    return true
end

function NPC_ReceiveGiftTimedAction:start()
    self:setActionAnim("Loot")
    if self.npc then
        self.character:faceThisObject(self.npc)
    end
end

function NPC_ReceiveGiftTimedAction:update()
end

function NPC_ReceiveGiftTimedAction:stop()
    ISBaseTimedAction.stop(self)
end

function NPC_ReceiveGiftTimedAction:perform()
    local NPC_GiftingEngine = require("DialogueFramework/Gifting/NPC_GiftingEngine")

    if isClient() then
        for _, giftItem in ipairs(self.giftItems) do
            sendClientCommand(self.character, "NPCGifting", "AwardGift", {
                npcID = self.npcID,
                itemType = giftItem.itemType,
                quantity = giftItem.quantity
            })
        end
    else
        for _, giftItem in ipairs(self.giftItems) do
            for i = 1, giftItem.quantity do
                self.character:getInventory():AddItem(giftItem.itemType)
            end
        end
    end

    NPC_GiftingEngine.markGiftReceived(self.character, self.npcID)

    ISBaseTimedAction.perform(self)
end

return NPC_ReceiveGiftTimedAction
