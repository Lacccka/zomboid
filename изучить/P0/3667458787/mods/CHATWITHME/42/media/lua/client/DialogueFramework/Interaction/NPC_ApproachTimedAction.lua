require "TimedActions/ISBaseTimedAction"

NPC_ApproachTimedAction = ISBaseTimedAction:derive("NPC_ApproachTimedAction")

local APPROACH_DISTANCE = 1.5
local MAX_NPC_DISTANCE = 5.0

function NPC_ApproachTimedAction:new(player, npc)
    local o = ISBaseTimedAction.new(self, player)
    o.npc = npc
    o.startDistance = 0
    o.maxTime = -1
    o.stopOnWalk = false
    o.stopOnRun = false
    return o
end

function NPC_ApproachTimedAction:isValid()
    if not self.npc then
        return false
    end

    if not self.npc:isExistInTheWorld() then
        return false
    end

    if self.npc:isDead() then
        return false
    end

    local playerX = self.character:getX()
    local playerY = self.character:getY()
    local npcX = self.npc:getX()
    local npcY = self.npc:getY()

    local deltaX = playerX - npcX
    local deltaY = playerY - npcY
    local distance = math.sqrt(deltaX * deltaX + deltaY * deltaY)

    if distance > MAX_NPC_DISTANCE then
        return false
    end

    return true
end

function NPC_ApproachTimedAction:waitToStart()
    return false
end

function NPC_ApproachTimedAction:update()
    if not self.npc or not self.npc:isExistInTheWorld() then
        self:forceStop()
        return
    end

    local playerX = self.character:getX()
    local playerY = self.character:getY()
    local npcX = self.npc:getX()
    local npcY = self.npc:getY()

    local deltaX = playerX - npcX
    local deltaY = playerY - npcY
    local distance = math.sqrt(deltaX * deltaX + deltaY * deltaY)

    if distance <= APPROACH_DISTANCE then
        self:forceComplete()
        return
    end

    local result = self.character:getPathFindBehavior2():update()

    if result == BehaviorResult.Failed then
        self:forceStop()
        return
    end

    if result == BehaviorResult.Succeeded then
        if distance <= APPROACH_DISTANCE + 0.5 then
            self:forceComplete()
        else
            self:forceStop()
        end
    end
end

function NPC_ApproachTimedAction:start()
    if not self.npc or not self.npc:isExistInTheWorld() then
        self:forceStop()
        return
    end

    local playerX = self.character:getX()
    local playerY = self.character:getY()
    local npcX = self.npc:getX()
    local npcY = self.npc:getY()

    local deltaX = playerX - npcX
    local deltaY = playerY - npcY
    self.startDistance = math.sqrt(deltaX * deltaX + deltaY * deltaY)

    self.action:setPathfinding(true)
    self.character:getPathFindBehavior2():pathToLocation(npcX, npcY, self.character:getZ())
end

function NPC_ApproachTimedAction:stop()
    local NPC_InteractionCoordinator = require("DialogueFramework/Interaction/NPC_InteractionCoordinator")
    NPC_InteractionCoordinator.setApproaching(false)

    self.character:getPathFindBehavior2():cancel()
    self.character:setPath2(nil)
    ISBaseTimedAction.stop(self)
end

function NPC_ApproachTimedAction:perform()
    self.character:getPathFindBehavior2():cancel()
    self.character:setPath2(nil)

    if not self.npc or not self.npc:isExistInTheWorld() then
        local NPC_InteractionCoordinator = require("DialogueFramework/Interaction/NPC_InteractionCoordinator")
        NPC_InteractionCoordinator.setApproaching(false)
        ISBaseTimedAction.perform(self)
        return
    end

    local playerX = self.character:getX()
    local playerY = self.character:getY()
    local npcX = self.npc:getX()
    local npcY = self.npc:getY()

    local deltaX = playerX - npcX
    local deltaY = playerY - npcY
    local finalDistance = math.sqrt(deltaX * deltaX + deltaY * deltaY)

    if finalDistance > APPROACH_DISTANCE + 0.5 then
        local NPC_InteractionCoordinator = require("DialogueFramework/Interaction/NPC_InteractionCoordinator")
        NPC_InteractionCoordinator.setApproaching(false)
        ISBaseTimedAction.perform(self)
        return
    end

    local NPC_DialogueEngine = require("DialogueFramework/Dialogue/NPC_DialogueEngine")
    local NPC_InteractionCoordinator = require("DialogueFramework/Interaction/NPC_InteractionCoordinator")
    local NPC_DialogueUI = require("DialogueFramework/UI/NPC_DialogueUI")

    local success, session = NPC_DialogueEngine.startSession(self.character, self.npc, nil)

    if success then
        NPC_InteractionCoordinator.setApproaching(false)
        NPC_InteractionCoordinator.setInteractionActive(true)

        local dialogueUI = NPC_DialogueUI:new(self.character, session)
        dialogueUI:initialise()
        dialogueUI:addToUIManager()
    else
        NPC_InteractionCoordinator.setApproaching(false)
    end

    ISBaseTimedAction.perform(self)
end

return NPC_ApproachTimedAction
