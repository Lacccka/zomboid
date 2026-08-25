require "TimedActions/ISBaseTimedAction"

NPC_AddToBackpackTimedAction = ISBaseTimedAction:derive("NPC_AddToBackpackTimedAction")

function NPC_AddToBackpackTimedAction:new(player, item, backpack)
    local o = ISBaseTimedAction.new(self, player)
    o.item = item
    o.backpack = backpack
    o.maxTime = 50
    o.stopOnWalk = true
    o.stopOnRun = true
    return o
end

function NPC_AddToBackpackTimedAction:isValid()
    return self.item and self.backpack
end

function NPC_AddToBackpackTimedAction:start()
    self:setActionAnim("Loot")
end

function NPC_AddToBackpackTimedAction:update()
end

function NPC_AddToBackpackTimedAction:stop()
    ISBaseTimedAction.stop(self)
end

function NPC_AddToBackpackTimedAction:perform()
    local playerInventory = self.character:getInventory()
    local backpackInventory = self.backpack:getInventory()

    if playerInventory and backpackInventory and playerInventory:contains(self.item) then
        playerInventory:Remove(self.item)
        backpackInventory:AddItem(self.item)

        local NPC_BackpackScanner = require("DialogueFramework/Trading/NPC_BackpackScanner")
        NPC_BackpackScanner.scanBackpack(self.character, self.backpack)
    end

    ISBaseTimedAction.perform(self)
end

return NPC_AddToBackpackTimedAction
