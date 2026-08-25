require "TimedActions/ISBaseTimedAction"

NPC_SpawnBackpackTimedAction = ISBaseTimedAction:derive("NPC_SpawnBackpackTimedAction")

function NPC_SpawnBackpackTimedAction:new(player, npc)
    local o = ISBaseTimedAction.new(self, player)
    o.npc = npc
    o.maxTime = -1
    o.stopOnWalk = false
    o.stopOnRun = false
    return o
end

function NPC_SpawnBackpackTimedAction:isValid()
    return self.npc and self.npc:isExistInTheWorld()
end

function NPC_SpawnBackpackTimedAction:start()
    local npcX = self.npc:getX()
    local npcY = self.npc:getY()
    local npcZ = self.npc:getZ()

    local playerX = self.character:getX()
    local playerY = self.character:getY()

    local deltaX = playerX - npcX
    local deltaY = playerY - npcY

    local distance = math.sqrt(deltaX * deltaX + deltaY * deltaY)
    if distance > 0 then
        deltaX = deltaX / distance
        deltaY = deltaY / distance
    end

    local targetX = npcX + deltaX
    local targetY = npcY + deltaY

    if self.character:getPathFindBehavior2() then
        self.character:getPathFindBehavior2():pathToLocation(targetX, targetY, npcZ)
    end
end

function NPC_SpawnBackpackTimedAction:update()
    if self.character:getPathFindBehavior2() then
        local result = self.character:getPathFindBehavior2():update()

        if result == BehaviorResult.Failed then
            self:forceStop()
            return
        end

        if result == BehaviorResult.Succeeded then
            self:forceComplete()
        end
    end
end

function NPC_SpawnBackpackTimedAction:stop()
    if self.character:getPathFindBehavior2() then
        self.character:getPathFindBehavior2():cancel()
    end
    self.character:setPath2(nil)
    ISBaseTimedAction.stop(self)
end

function NPC_SpawnBackpackTimedAction:perform()
    if self.character:getPathFindBehavior2() then
        self.character:getPathFindBehavior2():cancel()
    end
    self.character:setPath2(nil)

    self:spawnBackpackAtNPC()

    ISBaseTimedAction.perform(self)

    if self.onCompleteFunc then
        self.onCompleteFunc()
    end
end

function NPC_SpawnBackpackTimedAction:spawnBackpackAtNPC()
    local NPC_TradingConfig = require("DialogueFramework/Trading/NPC_TradingConfig")
    local backpackType = NPC_TradingConfig.getBackpackItemType()

    local npcX = self.npc:getX()
    local npcY = self.npc:getY()
    local npcZ = self.npc:getZ()

    local backpack = InventoryItemFactory.CreateItem(backpackType)

    if backpack then
        local square = getCell():getGridSquare(npcX, npcY, npcZ)
        if square then
            square:AddWorldInventoryItem(
                backpack,
                npcX - math.floor(npcX) + 0.5,
                npcY - math.floor(npcY) + 0.5,
                npcZ - math.floor(npcZ)
            )

            local NPC_DialogueSessionManager = require("DialogueFramework/Dialogue/NPC_DialogueSessionManager")
            local session = NPC_DialogueSessionManager.getSession(self.character)
            if session then
                session.tradingBackpack = backpack
                session.backpackSquare = square
            end
        end
    end
end

function NPC_SpawnBackpackTimedAction:setOnComplete(func)
    self.onCompleteFunc = func
end

return NPC_SpawnBackpackTimedAction
