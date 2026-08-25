local NPC_Behavior_FollowPlayer = {}

local activeFollows = {}
local eventHandlerRegistered = false

local FOLLOW_DISTANCE = 1.5
local MAX_FOLLOW_DISTANCE = 10.0

local function calculateFollowPosition(npc, player)
    if not npc or not player then
        return nil
    end

    local npcX = npc:getX()
    local npcY = npc:getY()
    local npcZ = npc:getZ()

    local playerX = player:getX()
    local playerY = player:getY()
    local playerZ = player:getZ()

    if npcZ ~= playerZ then
        return nil
    end

    local dx = playerX - npcX
    local dy = playerY - npcY
    local distance = math.sqrt(dx * dx + dy * dy)

    if distance <= FOLLOW_DISTANCE then
        return nil
    end

    if distance > MAX_FOLLOW_DISTANCE then
        return nil
    end

    local dirX = dx / distance
    local dirY = dy / distance

    local targetX = playerX - (dirX * FOLLOW_DISTANCE)
    local targetY = playerY - (dirY * FOLLOW_DISTANCE)

    return targetX, targetY, npcZ
end

local function moveNPCToPosition(npc, targetX, targetY, targetZ)
    if not npc then return false end

    local success, error = pcall(function()
        npc:setX(targetX)
        npc:setY(targetY)
        npc:setZ(targetZ)
    end)

    return success
end

local function checkSessionActive(player, npc)
    if not player or not npc then
        return false
    end

    local NPC_DialogueEngine = require("DialogueFramework/Dialogue/NPC_DialogueEngine")
    return NPC_DialogueEngine.hasActiveSession(player)
end

local function onTenMinuteUpdate()
    local NPC_BehaviorNPCRegistry = require("DialogueFramework/Behavior/NPC_BehaviorNPCRegistry")

    local toRemove = {}

    for npcID, followData in pairs(activeFollows) do
        local npc = followData.npc
        local player = followData.player

        if not NPC_BehaviorNPCRegistry.isValidNPC(npc) then
            table.insert(toRemove, npcID)
        elseif not player then
            table.insert(toRemove, npcID)
        elseif not checkSessionActive(player, npc) then
            table.insert(toRemove, npcID)
        else
            local targetX, targetY, targetZ = calculateFollowPosition(npc, player)
            if targetX and targetY and targetZ then
                moveNPCToPosition(npc, targetX, targetY, targetZ)
            end

            followData.lastUpdate = getTimestampMs() / 1000.0
        end
    end

    for _, npcID in ipairs(toRemove) do
        activeFollows[npcID] = nil
    end

    local hasActiveFollows = false
    for _ in pairs(activeFollows) do
        hasActiveFollows = true
        break
    end

    if not hasActiveFollows and eventHandlerRegistered then
        Events.EveryTenMinutes.Remove(onTenMinuteUpdate)
        eventHandlerRegistered = false
    end
end

function NPC_Behavior_FollowPlayer.execute(npc, params, player)
    if not npc then
        return false, "NPC is nil"
    end

    if not player then
        return false, "Player is nil"
    end

    local NPC_BehaviorNPCRegistry = require("DialogueFramework/Behavior/NPC_BehaviorNPCRegistry")
    local npcID = NPC_BehaviorNPCRegistry.getNPCID(npc)

    if not npcID then
        return false, "Failed to get NPC ID"
    end

    activeFollows[npcID] = {
        npc = npc,
        player = player,
        startTime = getTimestampMs() / 1000.0,
        lastUpdate = getTimestampMs() / 1000.0
    }

    if not eventHandlerRegistered then
        Events.EveryTenMinutes.Add(onTenMinuteUpdate)
        eventHandlerRegistered = true
    end

    local targetX, targetY, targetZ = calculateFollowPosition(npc, player)
    if targetX and targetY and targetZ then
        moveNPCToPosition(npc, targetX, targetY, targetZ)
    end

    return true, "Follow behavior started"
end

function NPC_Behavior_FollowPlayer.canExecute(npc, params)
    return npc ~= nil and params.player ~= nil
end

function NPC_Behavior_FollowPlayer.cleanup(npc)
    if not npc then return end

    local NPC_BehaviorNPCRegistry = require("DialogueFramework/Behavior/NPC_BehaviorNPCRegistry")
    local npcID = NPC_BehaviorNPCRegistry.getNPCID(npc)

    if npcID then
        activeFollows[npcID] = nil
    end

    local hasActiveFollows = false
    for _ in pairs(activeFollows) do
        hasActiveFollows = true
        break
    end

    if not hasActiveFollows and eventHandlerRegistered then
        Events.EveryTenMinutes.Remove(onTenMinuteUpdate)
        eventHandlerRegistered = false
    end
end

function NPC_Behavior_FollowPlayer.onComplete(npc, params, result)
    NPC_Behavior_FollowPlayer.cleanup(npc)
end

function NPC_Behavior_FollowPlayer.onFailed(npc, params, reason)
    NPC_Behavior_FollowPlayer.cleanup(npc)
end

return NPC_Behavior_FollowPlayer
