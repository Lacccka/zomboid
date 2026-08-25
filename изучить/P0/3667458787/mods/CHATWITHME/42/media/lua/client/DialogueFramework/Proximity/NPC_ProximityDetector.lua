local NPC_ProximityDetector = {}

local NPC_GreetingSystem = require("DialogueFramework/Sound/NPC_GreetingSystem")

local registeredNPCs = {}
local detectedNPCs = {}

local PROXIMITY_RANGE = 3.0
local CHECK_INTERVAL = 1.0

local lastCheckTime = 0

function NPC_ProximityDetector.registerNPC(npc)
    if not npc then
        return false
    end

    local npcID = tostring(npc)
    registeredNPCs[npcID] = npc

    return true
end

function NPC_ProximityDetector.unregisterNPC(npc)
    if not npc then
        return
    end

    local npcID = tostring(npc)
    registeredNPCs[npcID] = nil
    detectedNPCs[npcID] = nil
end

local function calculateDistance(player, npc)
    if not player or not npc then
        return 999
    end

    local playerX = player:getX()
    local playerY = player:getY()
    local npcX = npc:getX()
    local npcY = npc:getY()

    local deltaX = playerX - npcX
    local deltaY = playerY - npcY

    return math.sqrt(deltaX * deltaX + deltaY * deltaY)
end

function NPC_ProximityDetector.checkProximity()
    local currentTime = getTimestampMs() / 1000.0

    if currentTime - lastCheckTime < CHECK_INTERVAL then
        return
    end

    lastCheckTime = currentTime

    local player = getPlayer()
    if not player then
        return
    end

    for npcID, npc in pairs(registeredNPCs) do
        if not npc or not npc:isExistInTheWorld() or npc:isDead() then
            registeredNPCs[npcID] = nil
            detectedNPCs[npcID] = nil
        else
            local distance = calculateDistance(player, npc)

            if distance <= PROXIMITY_RANGE then
                if not detectedNPCs[npcID] then
                    detectedNPCs[npcID] = true

                    NPC_GreetingSystem.playGreeting(npc)
                end
            else
                if detectedNPCs[npcID] then
                    detectedNPCs[npcID] = nil
                end
            end
        end
    end
end

function NPC_ProximityDetector.clearAll()
    registeredNPCs = {}
    detectedNPCs = {}
end

return NPC_ProximityDetector
