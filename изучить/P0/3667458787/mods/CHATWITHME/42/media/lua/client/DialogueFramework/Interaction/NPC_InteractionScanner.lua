local NPC_InteractionScanner = {}

local MAX_INTERACTION_RANGE = 3.0
local SCAN_THROTTLE_DELAY = 0.5

local lastScanTime = 0
local validationCallback = nil

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

function NPC_InteractionScanner.setValidationCallback(callback)
    validationCallback = callback
end

function NPC_InteractionScanner.scanForNPC(player, maxRange)
    if not player then
        return nil, nil
    end

    if not validationCallback then
        return nil, nil
    end

    maxRange = maxRange or MAX_INTERACTION_RANGE

    local playerX = math.floor(player:getX())
    local playerY = math.floor(player:getY())
    local playerZ = player:getZ()

    local cell = getCell()
    if not cell then
        return nil, nil
    end

    local scanRadius = math.ceil(maxRange)

    for xOffset = -scanRadius, scanRadius do
        for yOffset = -scanRadius, scanRadius do
            local checkX = playerX + xOffset
            local checkY = playerY + yOffset

            local square = cell:getGridSquare(checkX, checkY, playerZ)

            if square then
                local objects = square:getMovingObjects()

                if objects and objects:size() > 0 then
                    for i = 0, objects:size() - 1 do
                        local obj = objects:get(i)

                        if validationCallback(obj) then
                            local distance = calculateDistance(player, obj)

                            if distance <= maxRange then
                                return obj, distance
                            end
                        end
                    end
                end
            end
        end
    end

    return nil, nil
end

function NPC_InteractionScanner.canScanNow()
    local currentTime = getTimestampMs() / 1000.0
    local timeSinceLastScan = currentTime - lastScanTime

    return timeSinceLastScan >= SCAN_THROTTLE_DELAY
end

function NPC_InteractionScanner.updateScanTime()
    lastScanTime = getTimestampMs() / 1000.0
end

function NPC_InteractionScanner.resetThrottle()
    lastScanTime = 0
end

return NPC_InteractionScanner
