local NPC_Behavior_Talking = {}

local NPC_BehaviorNPCRegistry = require("DialogueFramework/Behavior/NPC_BehaviorNPCRegistry")
local NPC_DialogueSessionManager = require("DialogueFramework/Dialogue/NPC_DialogueSessionManager")

local talkingStates = {}
local updateHandlerActive = false

local DISTANCE_THRESHOLD = 3.0
local APPROACH_DISTANCE = 1.5

local function calculateDistance(entity1, entity2)
    local x1, y1 = entity1:getX(), entity1:getY()
    local x2, y2 = entity2:getX(), entity2:getY()
    local deltaX = x1 - x2
    local deltaY = y1 - y2
    return math.sqrt(deltaX * deltaX + deltaY * deltaY)
end

local function makeMuggyFaceAwayFromPlayer(npc, player)
    local npcX, npcY = npc:getX(), npc:getY()
    local playerX, playerY = player:getX(), player:getY()

    local deltaX = playerX - npcX
    local deltaY = playerY - npcY

    local angleToPlayer = math.atan2(deltaY, deltaX)
    local angleAwayFromPlayer = angleToPlayer + math.pi

    while angleAwayFromPlayer > math.pi do
        angleAwayFromPlayer = angleAwayFromPlayer - 2 * math.pi
    end
    while angleAwayFromPlayer < -math.pi do
        angleAwayFromPlayer = angleAwayFromPlayer + 2 * math.pi
    end

    local directions = {
        {angle = 0, dir = IsoDirections.E},
        {angle = math.pi/4, dir = IsoDirections.SE},
        {angle = math.pi/2, dir = IsoDirections.S},
        {angle = 3*math.pi/4, dir = IsoDirections.SW},
        {angle = math.pi, dir = IsoDirections.W},
        {angle = -3*math.pi/4, dir = IsoDirections.NW},
        {angle = -math.pi/2, dir = IsoDirections.N},
        {angle = -math.pi/4, dir = IsoDirections.NE}
    }

    local closestDir = IsoDirections.S
    local minDiff = math.huge

    for _, d in ipairs(directions) do
        local diff = math.abs(angleAwayFromPlayer - d.angle)
        if diff < minDiff then
            minDiff = diff
            closestDir = d.dir
        end
    end

    npc:setDir(closestDir)
end

local function onTalkingUpdate()
    if not updateHandlerActive then
        return
    end

    for npcID, state in pairs(talkingStates) do
        local npc = state.npc
        local player = state.player

        if not NPC_BehaviorNPCRegistry.isValidNPC(npc) then
            talkingStates[npcID] = nil
        elseif not npc:isExistInTheWorld() then
            talkingStates[npcID] = nil
        elseif npc:isDead() then
            talkingStates[npcID] = nil
        else
            if not NPC_DialogueSessionManager.hasActiveSession(player) then
                NPC_Behavior_Talking.cleanup(npc)
            else
                if state.state == "WAITING_FOR_THRESHOLD" then
                    local distance = calculateDistance(npc, player)

                    if distance >= DISTANCE_THRESHOLD then
                        state.thresholdAchieved = true
                        state.state = "ACTIVE"
                        state.lockedPosition = {
                            x = npc:getX(),
                            y = npc:getY(),
                            z = npc:getZ()
                        }

                        state.allowMovement = true
                        makeMuggyFaceAwayFromPlayer(npc, player)
                        state.allowMovement = false
                    end

                elseif state.state == "ACTIVE" then
                    if not state.allowMovement then
                        local currentX = npc:getX()
                        local currentY = npc:getY()
                        local lockedX = state.lockedPosition.x
                        local lockedY = state.lockedPosition.y

                        if currentX ~= lockedX or currentY ~= lockedY then
                            npc:setX(lockedX)
                            npc:setY(lockedY)
                            npc:setZ(state.lockedPosition.z)
                        end
                    end

                elseif state.state == "APPROACHING_PLAYER" then
                    local distance = calculateDistance(npc, player)

                    if distance <= APPROACH_DISTANCE then
                        state.state = "ACTIVE"
                        state.lockedPosition = {
                            x = npc:getX(),
                            y = npc:getY(),
                            z = npc:getZ()
                        }
                        state.allowMovement = false

                        makeMuggyFaceAwayFromPlayer(npc, player)

                        npc:getPathFindBehavior2():cancel()
                        npc:setPath2(nil)
                    end
                end
            end
        end
    end

    local hasActiveStates = false
    for _ in pairs(talkingStates) do
        hasActiveStates = true
        break
    end

    if not hasActiveStates and updateHandlerActive then
        Events.OnTick.Remove(onTalkingUpdate)
        updateHandlerActive = false
    end
end

function NPC_Behavior_Talking.execute(npc, params, player)
    if not npc then
        return false, "NPC is nil"
    end

    if not params or not params.player then
        return false, "Player param is nil"
    end

    player = params.player

    if not player then
        return false, "Player is nil"
    end

    local npcID = NPC_BehaviorNPCRegistry.getNPCID(npc)
    if not npcID then
        return false, "Failed to get NPC ID"
    end

    local initialDistance = calculateDistance(npc, player)

    local initialState
    local thresholdAchieved = false

    if initialDistance >= DISTANCE_THRESHOLD then
        initialState = "ACTIVE"
        thresholdAchieved = true

        makeMuggyFaceAwayFromPlayer(npc, player)
    else
        initialState = "WAITING_FOR_THRESHOLD"
        thresholdAchieved = false
    end

    local state = {
        npc = npc,
        player = player,
        state = initialState,
        thresholdAchieved = thresholdAchieved,
        lockedPosition = initialState == "ACTIVE" and {
            x = npc:getX(),
            y = npc:getY(),
            z = npc:getZ()
        } or nil,
        allowMovement = false,
        startTime = getTimestampMs() / 1000.0
    }

    talkingStates[npcID] = state

    if not updateHandlerActive then
        Events.OnTick.Add(onTalkingUpdate)
        updateHandlerActive = true
    end

    return true, "Talking behavior started"
end

function NPC_Behavior_Talking.startApproach(npc, player)
    local npcID = NPC_BehaviorNPCRegistry.getNPCID(npc)
    if not npcID then return false end

    local state = talkingStates[npcID]
    if not state then return false end

    if state.thresholdAchieved then
        state.state = "APPROACHING_PLAYER"
        state.allowMovement = true

        local playerX, playerY, playerZ = player:getX(), player:getY(), player:getZ()
        npc:getPathFindBehavior2():pathToLocation(playerX, playerY, playerZ)

        return true
    end

    return false
end

function NPC_Behavior_Talking.cleanup(npc)
    if not npc then
        return
    end

    local npcID = NPC_BehaviorNPCRegistry.getNPCID(npc)
    if not npcID then
        return
    end

    local state = talkingStates[npcID]
    if state then
        if state.state == "APPROACHING_PLAYER" then
            npc:getPathFindBehavior2():cancel()
            npc:setPath2(nil)
        end
    end

    talkingStates[npcID] = nil

    local hasActiveStates = false
    for _ in pairs(talkingStates) do
        hasActiveStates = true
        break
    end

    if not hasActiveStates and updateHandlerActive then
        Events.OnTick.Remove(onTalkingUpdate)
        updateHandlerActive = false
    end
end

function NPC_Behavior_Talking.onComplete(npc, params, result)
    NPC_Behavior_Talking.cleanup(npc)
end

function NPC_Behavior_Talking.onFailed(npc, params, reason)
    NPC_Behavior_Talking.cleanup(npc)
end

return NPC_Behavior_Talking
