local NPC_DialoguePositioning = {}

local POSITION_CHECK_DISTANCE = 3.0
local TARGET_DISTANCE = 1.0

function NPC_DialoguePositioning.updatePosition(player, npc)
    if isClient() and not isServer() then
        return false
    end

    if not player or not npc then
        return false
    end

    if not npc:isExistInTheWorld() or not player:isExistInTheWorld() then
        return false
    end

    local npcX = npc:getX()
    local npcY = npc:getY()
    local npcZ = npc:getZ()

    local playerX = player:getX()
    local playerY = player:getY()
    local playerZ = player:getZ()

    if npcZ ~= playerZ then
        player:faceThisObject(npc)
        return true
    end

    local dx = playerX - npcX
    local dy = playerY - npcY
    local distance = math.sqrt(dx * dx + dy * dy)

    if distance >= POSITION_CHECK_DISTANCE then
        if distance > 0 then
            local dirX = dx / distance
            local dirY = dy / distance

            local targetX = playerX - (dirX * TARGET_DISTANCE)
            local targetY = playerY - (dirY * TARGET_DISTANCE)

            npc:setX(targetX)
            npc:setY(targetY)
            npc:setZ(playerZ)
        end
    end

    player:faceThisObject(npc)

    return true
end

return NPC_DialoguePositioning
