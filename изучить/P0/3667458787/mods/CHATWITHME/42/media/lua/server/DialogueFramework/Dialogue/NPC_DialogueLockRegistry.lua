if isServer() then
    local NPC_DialogueLockRegistry = {}

    local npcLocks = {}
    local LOCK_TIMEOUT = 300.0

    function NPC_DialogueLockRegistry.tryAcquireLock(npc, player)
        if not npc or not player then
            return false, "Invalid NPC or player"
        end

        local NPC_BehaviorNPCRegistry = require("DialogueFramework/Behavior/NPC_BehaviorNPCRegistry")
        local npcID = NPC_BehaviorNPCRegistry.getNPCID(npc)
        local playerID = player:getOnlineID()

        if not npcID or not playerID then
            return false, "Failed to get NPC or player ID"
        end

        local existingLock = npcLocks[npcID]

        if existingLock then
            if existingLock.playerID == playerID then
                existingLock.lastHeartbeat = getTimestampMs() / 1000.0
                return true, "Lock refreshed"
            else
                local lockOwner = getPlayerByOnlineID(existingLock.playerID)
                if lockOwner and lockOwner:isAlive() then
                    return false, "NPC is busy with another player"
                else
                    npcLocks[npcID] = nil
                end
            end
        end

        local currentTime = getTimestampMs() / 1000.0
        npcLocks[npcID] = {
            playerID = playerID,
            lockTime = currentTime,
            lastHeartbeat = currentTime,
            npcEntity = npc
        }

        return true, "Lock acquired"
    end

    function NPC_DialogueLockRegistry.releaseLock(npc, player)
        if not npc then
            return false
        end

        local NPC_BehaviorNPCRegistry = require("DialogueFramework/Behavior/NPC_BehaviorNPCRegistry")
        local npcID = NPC_BehaviorNPCRegistry.getNPCID(npc)

        if not npcID then
            return false
        end

        local existingLock = npcLocks[npcID]

        if not existingLock then
            return true
        end

        if player then
            local playerID = player:getOnlineID()
            if playerID and existingLock.playerID ~= playerID then
                return false
            end
        end

        npcLocks[npcID] = nil
        return true
    end

    function NPC_DialogueLockRegistry.releaseAllLocksForPlayer(player)
        if not player then
            return
        end

        local playerID = player:getOnlineID()
        if not playerID then
            return
        end

        for npcID, lockData in pairs(npcLocks) do
            if lockData.playerID == playerID then
                npcLocks[npcID] = nil
            end
        end
    end

    function NPC_DialogueLockRegistry.cleanupStaleLocks()
        local currentTime = getTimestampMs() / 1000.0

        for npcID, lockData in pairs(npcLocks) do
            local elapsed = currentTime - lockData.lastHeartbeat

            if elapsed > LOCK_TIMEOUT then
                npcLocks[npcID] = nil
            else
                local lockOwner = getPlayerByOnlineID(lockData.playerID)
                if not lockOwner or not lockOwner:isAlive() then
                    npcLocks[npcID] = nil
                end
            end
        end
    end

    function NPC_DialogueLockRegistry.isLocked(npc)
        if not npc then
            return false
        end

        local NPC_BehaviorNPCRegistry = require("DialogueFramework/Behavior/NPC_BehaviorNPCRegistry")
        local npcID = NPC_BehaviorNPCRegistry.getNPCID(npc)

        if not npcID then
            return false
        end

        return npcLocks[npcID] ~= nil
    end

    local function onPlayerDeath(player)
        NPC_DialogueLockRegistry.releaseAllLocksForPlayer(player)
    end

    local function onPlayerDisconnect(player)
        NPC_DialogueLockRegistry.releaseAllLocksForPlayer(player)
    end

    local cleanupTimer = 0
    local CLEANUP_INTERVAL = 60.0

    local function onTick()
        cleanupTimer = cleanupTimer + (1.0 / 60.0)

        if cleanupTimer >= CLEANUP_INTERVAL then
            NPC_DialogueLockRegistry.cleanupStaleLocks()
            cleanupTimer = 0
        end
    end

    Events.OnPlayerDeath.Add(onPlayerDeath)
    Events.OnPlayerDisconnect.Add(onPlayerDisconnect)
    Events.OnTick.Add(onTick)

    return NPC_DialogueLockRegistry
end
