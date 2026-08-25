local NPC_BehaviorNPCRegistry = {}

local registeredNPCs = {}
local npcBehaviorQueues = {}

function NPC_BehaviorNPCRegistry.getNPCID(npc)
    if not npc then return nil end

    local npcID = npc:getOnlineID()

    if not npcID or npcID < 0 then
        npcID = npc:getID()
    end

    return npcID
end

function NPC_BehaviorNPCRegistry.isValidNPC(npc)
    if not npc then return false end

    if not instanceof(npc, "IsoAnimal") then
        return false
    end

    local isNPC = false
    local success, result = pcall(function()
        return npc:getVariable("currentAnimalMuggy") ~= nil
    end)

    if success and result then
        isNPC = true
    end

    return isNPC
end

function NPC_BehaviorNPCRegistry.registerNPC(npc)
    if not NPC_BehaviorNPCRegistry.isValidNPC(npc) then
        return false
    end

    local npcID = NPC_BehaviorNPCRegistry.getNPCID(npc)
    if not npcID then return false end

    if registeredNPCs[npcID] then
        registeredNPCs[npcID].lastSeen = getTimestampMs() / 1000.0
        return true
    end

    registeredNPCs[npcID] = {
        npc = npc,
        npcID = npcID,
        registeredTime = getTimestampMs() / 1000.0,
        lastSeen = getTimestampMs() / 1000.0
    }

    npcBehaviorQueues[npcID] = {}

    return true
end

function NPC_BehaviorNPCRegistry.getNPCByID(npcID)
    if not npcID then return nil end

    local npcData = registeredNPCs[npcID]
    if not npcData then return nil end

    local npc = npcData.npc
    if not npc or not NPC_BehaviorNPCRegistry.isValidNPC(npc) then
        registeredNPCs[npcID] = nil
        npcBehaviorQueues[npcID] = nil
        return nil
    end

    return npc
end

function NPC_BehaviorNPCRegistry.unregisterNPC(npcID)
    if not npcID then return false end

    registeredNPCs[npcID] = nil
    npcBehaviorQueues[npcID] = nil

    return true
end

function NPC_BehaviorNPCRegistry.getAllRegisteredNPCs()
    local npcs = {}

    for npcID, npcData in pairs(registeredNPCs) do
        if NPC_BehaviorNPCRegistry.isValidNPC(npcData.npc) then
            table.insert(npcs, npcData.npc)
        else
            registeredNPCs[npcID] = nil
            npcBehaviorQueues[npcID] = nil
        end
    end

    return npcs
end

function NPC_BehaviorNPCRegistry.getQueue(npcID)
    return npcBehaviorQueues[npcID]
end

function NPC_BehaviorNPCRegistry.setQueue(npcID, queue)
    npcBehaviorQueues[npcID] = queue
end

function NPC_BehaviorNPCRegistry.clearAllQueues()
    npcBehaviorQueues = {}
end

function NPC_BehaviorNPCRegistry.clearAllRegistrations()
    registeredNPCs = {}
    npcBehaviorQueues = {}
end

return NPC_BehaviorNPCRegistry
