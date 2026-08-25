local NPC_GuardZoneBoundaryEnforcer = {}

local NPC_GuardConfig = require("DialogueFramework/Behavior/NPC_GuardConfig")
local NPC_GuardStateManager = require("DialogueFramework/Behavior/NPC_GuardStateManager")
local NPC_BehaviorNPCRegistry = require("DialogueFramework/Behavior/NPC_BehaviorNPCRegistry")
local MUGGY_ZoneDefinitions = require("MuggyMod/MUGGY_ZoneDefinitions")

local npcLastPositions = {}
local lastBoundaryChecks = {}

local function isNPCInZone(npc, zone)
    if not npc or not zone then
        return false
    end

    local npcX = npc:getX()
    local npcY = npc:getY()

    if not npcX or not npcY then
        return false
    end

    local minX = math.min(zone.minX, zone.maxX)
    local maxX = math.max(zone.minX, zone.maxX)
    local minY = math.min(zone.minY, zone.maxY)
    local maxY = math.max(zone.minY, zone.maxY)

    return npcX >= minX and npcX <= maxX and npcY >= minY and npcY <= maxY
end

local function getZoneCenter(zone)
    if not zone then
        return nil, nil, nil
    end

    local centerX = (zone.minX + zone.maxX) / 2
    local centerY = (zone.minY + zone.maxY) / 2

    return centerX, centerY, 0
end

local function returnNPCToZoneCenter(npc, zone)
    if not npc or not zone then
        return false
    end

    local centerX, centerY, centerZ = getZoneCenter(zone)
    if not centerX or not centerY then
        return false
    end

    local success, error = pcall(function()
        npc:setX(centerX)
        npc:setY(centerY)
        npc:setZ(centerZ)
    end)

    if not success then
        return false
    end

    return true
end

local function shouldCheckBoundary(npcID)
    if not npcID then
        return false
    end

    local currentTime = getTimestampMs() / 1000.0
    local lastCheck = lastBoundaryChecks[npcID] or 0

    if currentTime - lastCheck < NPC_GuardConfig.BOUNDARY_CHECK.THROTTLE_COOLDOWN then
        return false
    end

    lastBoundaryChecks[npcID] = currentTime

    return true
end

local function performBoundaryCheck(npc, npcID, player)
    if not npc or not npcID or not player then
        return false
    end

    if not npc:isExistInTheWorld() then
        return false
    end

    if npc:isDead() then
        return false
    end

    local guardState = NPC_GuardStateManager.getGuardState(player, npcID)
    if not guardState or not guardState.isGuarding then
        return false
    end

    if not guardState.assignedZoneID then
        return false
    end

    local zone = MUGGY_ZoneDefinitions.getZoneById(guardState.assignedZoneID)
    if not zone then
        return false
    end

    if not isNPCInZone(npc, zone) then
        local success = returnNPCToZoneCenter(npc, zone)

        if success then
            if isClient() and not isServer() then
                sendClientCommand(NPC_GuardConfig.COMMANDS.GUARD_SYSTEM.MODULE,
                                NPC_GuardConfig.COMMANDS.GUARD_SYSTEM.NPC_EXITED_ZONE, {
                    npcID = npcID,
                    zoneID = zone.id,
                    playerID = player:getOnlineID()
                })
            end
        end

        return success
    end

    return true
end

function NPC_GuardZoneBoundaryEnforcer.checkBoundary(npc, player)
    if not npc or not player then
        return false
    end

    local npcID = NPC_BehaviorNPCRegistry.getNPCID(npc)
    if not npcID then
        return false
    end

    if not shouldCheckBoundary(npcID) then
        return false
    end

    return performBoundaryCheck(npc, npcID, player)
end

function NPC_GuardZoneBoundaryEnforcer.onNPCPositionChange(npc, player)
    if not npc or not player then
        return false
    end

    if not NPC_GuardConfig.BOUNDARY_CHECK.CHECK_ON_MOVEMENT then
        return false
    end

    local npcID = NPC_BehaviorNPCRegistry.getNPCID(npc)
    if not npcID then
        return false
    end

    local lastPos = npcLastPositions[npcID]
    local currentPos = {
        x = npc:getX(),
        y = npc:getY()
    }

    if not currentPos.x or not currentPos.y then
        return false
    end

    if lastPos then
        if lastPos.x == currentPos.x and lastPos.y == currentPos.y then
            return false
        end
    end

    npcLastPositions[npcID] = currentPos

    if lastPos then
        return NPC_GuardZoneBoundaryEnforcer.checkBoundary(npc, player)
    end

    return false
end

function NPC_GuardZoneBoundaryEnforcer.forceReturnToZone(npc, player)
    if not npc or not player then
        return false
    end

    local npcID = NPC_BehaviorNPCRegistry.getNPCID(npc)
    if not npcID then
        return false
    end

    local guardState = NPC_GuardStateManager.getGuardState(player, npcID)
    if not guardState or not guardState.assignedZoneID then
        return false
    end

    local zone = MUGGY_ZoneDefinitions.getZoneById(guardState.assignedZoneID)
    if not zone then
        return false
    end

    return returnNPCToZoneCenter(npc, zone)
end

function NPC_GuardZoneBoundaryEnforcer.isInAssignedZone(npc, player)
    if not npc or not player then
        return false
    end

    local npcID = NPC_BehaviorNPCRegistry.getNPCID(npc)
    if not npcID then
        return false
    end

    local guardState = NPC_GuardStateManager.getGuardState(player, npcID)
    if not guardState or not guardState.assignedZoneID then
        return false
    end

    local zone = MUGGY_ZoneDefinitions.getZoneById(guardState.assignedZoneID)
    if not zone then
        return false
    end

    return isNPCInZone(npc, zone)
end

function NPC_GuardZoneBoundaryEnforcer.cleanup(npcID)
    if not npcID then
        return false
    end

    npcLastPositions[npcID] = nil
    lastBoundaryChecks[npcID] = nil

    return true
end

return NPC_GuardZoneBoundaryEnforcer
