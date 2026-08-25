local NPC_GuardStateManager = {}

function NPC_GuardStateManager.initializeGuardState(player, npcID)
    if not player then
        return nil
    end

    if not npcID then
        return nil
    end

    local modData = player:getModData()
    if not modData then
        return nil
    end

    if not modData.npcGuardStates then
        modData.npcGuardStates = {}
    end

    if not modData.npcGuardStates[npcID] then
        modData.npcGuardStates[npcID] = {
            assignedZoneID = nil,
            isGuarding = false,
            lastBoundaryCheck = 0,
            detectedTimestamp = 0,
            lastKnownX = nil,
            lastKnownY = nil,
            lastKnownZ = nil
        }
    end

    return modData.npcGuardStates[npcID]
end

function NPC_GuardStateManager.getGuardState(player, npcID)
    if not player then
        return nil
    end

    if not npcID then
        return nil
    end

    local modData = player:getModData()
    if not modData then
        return nil
    end

    if not modData.npcGuardStates then
        return nil
    end

    return modData.npcGuardStates[npcID]
end

function NPC_GuardStateManager.setGuardState(player, npcID, guardState)
    if not player then
        return false
    end

    if not npcID then
        return false
    end

    if not guardState then
        return false
    end

    local modData = player:getModData()
    if not modData then
        return false
    end

    if not modData.npcGuardStates then
        modData.npcGuardStates = {}
    end

    modData.npcGuardStates[npcID] = guardState

    return true
end

function NPC_GuardStateManager.activateGuarding(player, npcID, zoneID)
    if not player then
        return false
    end

    if not npcID then
        return false
    end

    if not zoneID then
        return false
    end

    local guardState = NPC_GuardStateManager.initializeGuardState(player, npcID)
    if not guardState then
        return false
    end

    guardState.assignedZoneID = zoneID
    guardState.isGuarding = true
    guardState.detectedTimestamp = getTimestampMs() / 1000.0

    return true
end

function NPC_GuardStateManager.deactivateGuarding(player, npcID)
    if not player then
        return false
    end

    if not npcID then
        return false
    end

    local guardState = NPC_GuardStateManager.getGuardState(player, npcID)
    if not guardState then
        return false
    end

    guardState.isGuarding = false

    return true
end

function NPC_GuardStateManager.isGuarding(player, npcID)
    if not player then
        return false
    end

    if not npcID then
        return false
    end

    local guardState = NPC_GuardStateManager.getGuardState(player, npcID)
    if not guardState then
        return false
    end

    return guardState.isGuarding == true
end

function NPC_GuardStateManager.getAssignedZoneID(player, npcID)
    if not player then
        return nil
    end

    if not npcID then
        return nil
    end

    local guardState = NPC_GuardStateManager.getGuardState(player, npcID)
    if not guardState then
        return nil
    end

    return guardState.assignedZoneID
end

function NPC_GuardStateManager.updateLastPosition(player, npcID, x, y, z)
    if not player then
        return false
    end

    if not npcID then
        return false
    end

    local guardState = NPC_GuardStateManager.getGuardState(player, npcID)
    if not guardState then
        return false
    end

    guardState.lastKnownX = x
    guardState.lastKnownY = y
    guardState.lastKnownZ = z

    return true
end

function NPC_GuardStateManager.updateBoundaryCheckTime(player, npcID)
    if not player then
        return false
    end

    if not npcID then
        return false
    end

    local guardState = NPC_GuardStateManager.getGuardState(player, npcID)
    if not guardState then
        return false
    end

    guardState.lastBoundaryCheck = getTimestampMs() / 1000.0

    return true
end

function NPC_GuardStateManager.getLastBoundaryCheckTime(player, npcID)
    if not player then
        return 0
    end

    if not npcID then
        return 0
    end

    local guardState = NPC_GuardStateManager.getGuardState(player, npcID)
    if not guardState then
        return 0
    end

    return guardState.lastBoundaryCheck or 0
end

function NPC_GuardStateManager.cleanupGuardState(player, npcID)
    if not player then
        return false
    end

    if not npcID then
        return false
    end

    local modData = player:getModData()
    if not modData then
        return false
    end

    if not modData.npcGuardStates then
        return false
    end

    modData.npcGuardStates[npcID] = nil

    return true
end

function NPC_GuardStateManager.cleanupAllGuardStates(player)
    if not player then
        return false
    end

    local modData = player:getModData()
    if not modData then
        return false
    end

    modData.npcGuardStates = {}

    return true
end

function NPC_GuardStateManager.getAllGuardingNPCs(player)
    if not player then
        return {}
    end

    local modData = player:getModData()
    if not modData then
        return {}
    end

    if not modData.npcGuardStates then
        return {}
    end

    local guardingNPCs = {}

    for npcID, guardState in pairs(modData.npcGuardStates) do
        if guardState.isGuarding then
            table.insert(guardingNPCs, {
                npcID = npcID,
                zoneID = guardState.assignedZoneID
            })
        end
    end

    return guardingNPCs
end

function NPC_GuardStateManager.getGuardStateByZoneID(player, zoneID)
    if not player then
        return nil
    end

    if not zoneID then
        return nil
    end

    local modData = player:getModData()
    if not modData then
        return nil
    end

    if not modData.npcGuardStates then
        return nil
    end

    for npcID, guardState in pairs(modData.npcGuardStates) do
        if guardState.assignedZoneID == zoneID and guardState.isGuarding then
            return guardState, npcID
        end
    end

    return nil
end

function NPC_GuardStateManager.validateGuardState(guardState)
    if not guardState then
        return false
    end

    if type(guardState) ~= "table" then
        return false
    end

    if guardState.assignedZoneID == nil then
        return false
    end

    if guardState.isGuarding == nil then
        return false
    end

    return true
end

return NPC_GuardStateManager
