local NPC_IdleSoundSystem = {}

if not Events.OnNPCIdleTriggered then
    LuaEventManager.AddEvent("OnNPCIdleTriggered")
end

local NPC_DialogueEngine = require("DialogueFramework/Dialogue/NPC_DialogueEngine")
local MUGGY_EnvironmentDetector = require("MuggyMod/MUGGY_EnvironmentDetector")

local registeredNPCs = {}
local lastDialogueCloseTime = {}

local MIN_IDLE_INTERVAL = 15.0
local MAX_IDLE_INTERVAL = 45.0
local DIALOGUE_CLOSE_SUPPRESS_TIME = 3.0
local IDLE_PROXIMITY_RANGE = 8.0

function NPC_IdleSoundSystem.registerNPC(npc, npcIDString, idleDefinitionKeys)
    if not npc or not npcIDString or not idleDefinitionKeys or #idleDefinitionKeys == 0 then
        return false
    end

    local instanceID = tostring(npc)

    registeredNPCs[instanceID] = {
        npc = npc,
        npcIDString = npcIDString,
        idleDefinitionKeys = idleDefinitionKeys,
        nextIdleTime = 0,
        lastIdleTime = 0
    }

    NPC_IdleSoundSystem.scheduleNextIdle(instanceID)

    return true
end

function NPC_IdleSoundSystem.unregisterNPC(npc)
    if not npc then
        return
    end

    local npcID = tostring(npc)
    registeredNPCs[npcID] = nil
end

function NPC_IdleSoundSystem.scheduleNextIdle(npcID)
    local npcData = registeredNPCs[npcID]
    if not npcData then
        return
    end

    local randomInterval = MIN_IDLE_INTERVAL + (ZombRand(1000) / 1000.0) * (MAX_IDLE_INTERVAL - MIN_IDLE_INTERVAL)
    local currentTime = getTimestampMs() / 1000.0

    npcData.nextIdleTime = currentTime + randomInterval
end

function NPC_IdleSoundSystem.recordDialogueClose(player)
    if not player then
        return
    end

    local playerID = player:getOnlineID()
    lastDialogueCloseTime[playerID] = getTimestampMs() / 1000.0
end

function NPC_IdleSoundSystem.isDialogueSuppressed(player)
    if not player then
        return true
    end

    if NPC_DialogueEngine.hasActiveSession(player) then
        return true
    end

    local playerID = player:getOnlineID()
    local lastCloseTime = lastDialogueCloseTime[playerID]

    if lastCloseTime then
        local currentTime = getTimestampMs() / 1000.0
        local timeSinceClose = currentTime - lastCloseTime

        local suppressDuration = DIALOGUE_CLOSE_SUPPRESS_TIME + (ZombRand(3000) / 1000.0)

        if timeSinceClose < suppressDuration then
            return true
        end
    end

    return false
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

function NPC_IdleSoundSystem.update()
    if not getPlayer then
        return
    end

    local currentTime = getTimestampMs() / 1000.0
    local player = getPlayer()

    if not player then
        return
    end

    for npcID, npcData in pairs(registeredNPCs) do
        local npc = npcData.npc

        if not npc or not npc:isExistInTheWorld() or npc:isDead() then
            registeredNPCs[npcID] = nil
        else
            if currentTime >= npcData.nextIdleTime then
                if not NPC_IdleSoundSystem.isDialogueSuppressed(player) then
                    local distance = calculateDistance(player, npc)
                    if distance <= IDLE_PROXIMITY_RANGE then
                        NPC_IdleSoundSystem.triggerIdleEvent(npcID, npcData)
                    end
                end

                NPC_IdleSoundSystem.scheduleNextIdle(npcID)
            end
        end
    end
end

function NPC_IdleSoundSystem.triggerIdleEvent(instanceID, npcData)
    if not npcData or not npcData.idleDefinitionKeys or #npcData.idleDefinitionKeys == 0 then
        return
    end

    local randomIndex = ZombRand(#npcData.idleDefinitionKeys) + 1
    local idleKey = npcData.idleDefinitionKeys[randomIndex]

    npcData.lastIdleTime = getTimestampMs() / 1000.0

    triggerEvent("OnNPCIdleTriggered", npcData.npc, npcData.npcIDString, idleKey)
end

function NPC_IdleSoundSystem.clearAll()
    registeredNPCs = {}
    lastDialogueCloseTime = {}
end

local function onDialogueStarted(npc, player, session)
end

local function onDialogueEnded(npc, player)
    if npc and player then
        local success, NPC_DialogueLockRegistry = pcall(require, "DialogueFramework/Dialogue/NPC_DialogueLockRegistry")
        if success and NPC_DialogueLockRegistry and NPC_DialogueLockRegistry.releaseLock then
            NPC_DialogueLockRegistry.releaseLock(npc, player)
        end
    end
end

Events.OnNPCDialogueStarted.Add(onDialogueStarted)
Events.OnNPCDialogueEnded.Add(onDialogueEnded)

return NPC_IdleSoundSystem
