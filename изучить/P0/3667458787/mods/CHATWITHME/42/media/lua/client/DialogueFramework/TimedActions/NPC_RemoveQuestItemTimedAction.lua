require "TimedActions/ISBaseTimedAction"

NPC_RemoveQuestItemTimedAction = ISBaseTimedAction:derive("NPC_RemoveQuestItemTimedAction")

function NPC_RemoveQuestItemTimedAction:new(player, npc, itemType, flagKey)
    local o = ISBaseTimedAction.new(self, player)
    o.npc = npc
    o.itemType = itemType
    o.flagKey = flagKey
    o.maxTime = 100
    o.stopOnWalk = true
    o.stopOnRun = true
    return o
end

function NPC_RemoveQuestItemTimedAction:isValid()
    if not self.npc or not self.npc:isExistInTheWorld() then
        return false
    end

    local inventory = self.character:getInventory()
    if not inventory then
        return false
    end

    local item = inventory:getFirstTypeRecurse(self.itemType)
    return item ~= nil
end

function NPC_RemoveQuestItemTimedAction:start()
    self:setActionAnim("Loot")
    if self.npc then
        self.character:faceThisObject(self.npc)
    end
end

function NPC_RemoveQuestItemTimedAction:update()
end

function NPC_RemoveQuestItemTimedAction:stop()
    ISBaseTimedAction.stop(self)
end

function NPC_RemoveQuestItemTimedAction:perform()
    local inventory = self.character:getInventory()
    if inventory then
        local item = inventory:getFirstTypeRecurse(self.itemType)
        if item then
            inventory:Remove(item)

            if self.flagKey then
                local NPC_DialogueActions = require("DialogueFramework/Dialogue/NPC_DialogueActions")
                NPC_DialogueActions.setModDataFlag(self.character, self.flagKey, true)
            end
        end
    end

    ISBaseTimedAction.perform(self)
end

return NPC_RemoveQuestItemTimedAction
