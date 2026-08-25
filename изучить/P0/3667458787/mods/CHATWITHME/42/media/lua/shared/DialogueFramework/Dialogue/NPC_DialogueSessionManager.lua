local NPC_DialogueSessionManager = {}

local activeSessions = {}

function NPC_DialogueSessionManager.registerSession(player, session)
    if not player or not session then
        return false
    end

    local playerID = player:getOnlineID()

    activeSessions[playerID] = session

    return true
end

function NPC_DialogueSessionManager.unregisterSession(player)
    if not player then
        return false
    end

    local playerID = player:getOnlineID()
    local session = activeSessions[playerID]

    if session and session.tradingBackpack then
        if session.backpackSquare then
            session.backpackSquare:removeItem(session.tradingBackpack)
        end

        session.tradingBackpack = nil
        session.backpackSquare = nil
    end

    activeSessions[playerID] = nil

    return true
end

function NPC_DialogueSessionManager.getSession(player)
    if not player then
        return nil
    end

    local playerID = player:getOnlineID()

    return activeSessions[playerID]
end

function NPC_DialogueSessionManager.hasActiveSession(player)
    if not player then
        return false
    end

    local playerID = player:getOnlineID()

    return activeSessions[playerID] ~= nil
end

function NPC_DialogueSessionManager.updateSessionNode(player, newNodeID)
    local session = NPC_DialogueSessionManager.getSession(player)

    if not session then
        return false
    end

    session.currentNode = newNodeID

    return true
end

function NPC_DialogueSessionManager.pushPreviousNode(player, nodeID)
    local session = NPC_DialogueSessionManager.getSession(player)

    if not session then
        return false
    end

    if not session.previousNodes then
        session.previousNodes = {}
    end

    table.insert(session.previousNodes, nodeID)

    return true
end

function NPC_DialogueSessionManager.popPreviousNode(player)
    local session = NPC_DialogueSessionManager.getSession(player)

    if not session then
        return nil
    end

    if not session.previousNodes or #session.previousNodes == 0 then
        return nil
    end

    local lastNode = session.previousNodes[#session.previousNodes]
    table.remove(session.previousNodes, #session.previousNodes)

    return lastNode
end

function NPC_DialogueSessionManager.clearAll()
    activeSessions = {}
end

function NPC_DialogueSessionManager.saveSessionProgress(player, session)
    print("========================================")
    print("=== saveSessionProgress() CALLED ===")
    print("========================================")

    if not player or not session then
        print("EARLY RETURN: player or session is nil")
        print("  player: " .. tostring(player))
        print("  session: " .. tostring(session))
        print("========================================")
        return false
    end
    print("CHECK PASSED: player and session exist")

    local modData = player:getModData()
    if not modData then
        print("EARLY RETURN: modData is nil")
        print("========================================")
        return false
    end
    print("CHECK PASSED: modData exists")

    local sessionDef = session.sessionDef
    if not sessionDef then
        print("EARLY RETURN: session.sessionDef is nil")
        print("========================================")
        return false
    end
    print("CHECK PASSED: sessionDef exists")

    local npcID = sessionDef.npcID
    local sessionID = sessionDef.sessionID

    print("STORING PROGRESS FOR:")
    print("  npcID: [" .. tostring(npcID) .. "]")
    print("  sessionID: [" .. tostring(sessionID) .. "]")
    print("  sessionID length: " .. tostring(string.len(sessionID)))
    print("  currentNode: [" .. tostring(session.currentNode) .. "]")

    if not modData.npcDialogueHistory then
        print("INITIALIZING: modData.npcDialogueHistory = {}")
        modData.npcDialogueHistory = {}
    else
        print("ALREADY EXISTS: modData.npcDialogueHistory")
    end

    if not modData.npcDialogueHistory[npcID] then
        print("INITIALIZING: modData.npcDialogueHistory[\"" .. npcID .. "\"] = {}")
        modData.npcDialogueHistory[npcID] = {}
    else
        print("ALREADY EXISTS: modData.npcDialogueHistory[\"" .. npcID .. "\"]")
    end

    if not modData.npcDialogueHistory[npcID].sessionProgress then
        print("INITIALIZING: modData.npcDialogueHistory[\"" .. npcID .. "\"].sessionProgress = {}")
        modData.npcDialogueHistory[npcID].sessionProgress = {}
    else
        print("ALREADY EXISTS: modData.npcDialogueHistory[\"" .. npcID .. "\"].sessionProgress")
    end

    local progress = modData.npcDialogueHistory[npcID].sessionProgress[sessionID]
    if not progress then
        print("CREATING NEW PROGRESS ENTRY for sessionID [" .. sessionID .. "]")
        progress = {
            started = true,
            completed = false,
            visitedNodes = {},
            selectedOptions = {}
        }
        modData.npcDialogueHistory[npcID].sessionProgress[sessionID] = progress
        print("PROGRESS ENTRY CREATED:")
        print("  started: " .. tostring(progress.started) .. " (type: " .. type(progress.started) .. ")")
        print("  completed: " .. tostring(progress.completed) .. " (type: " .. type(progress.completed) .. ")")
    else
        print("UPDATING EXISTING PROGRESS ENTRY for sessionID [" .. sessionID .. "]")
        print("  Current started: " .. tostring(progress.started))
        print("  Current completed: " .. tostring(progress.completed))
    end

    progress.lastNode = session.currentNode
    progress.lastTimestamp = getTimestampMs() / 1000.0

    print("UPDATED PROGRESS:")
    print("  lastNode: " .. tostring(progress.lastNode))
    print("  lastTimestamp: " .. tostring(progress.lastTimestamp))

    local NPC_DialogueConfig = require("DialogueFramework/Dialogue/NPC_DialogueConfig")
    local maxNodes = NPC_DialogueConfig.RESUME_SYSTEM.MAX_VISITED_NODES

    local alreadyVisited = false
    for _, node in ipairs(progress.visitedNodes) do
        if node == session.currentNode then
            alreadyVisited = true
            break
        end
    end

    if not alreadyVisited then
        table.insert(progress.visitedNodes, session.currentNode)
        print("ADDED NODE TO VISITED NODES: " .. tostring(session.currentNode))

        if #progress.visitedNodes > maxNodes then
            table.remove(progress.visitedNodes, 1)
            print("REMOVED OLDEST NODE (exceeded max of " .. tostring(maxNodes) .. ")")
        end
    else
        print("NODE ALREADY IN VISITED NODES: " .. tostring(session.currentNode))
    end

    print("FINAL PROGRESS STATE:")
    print("  started: " .. tostring(progress.started))
    print("  completed: " .. tostring(progress.completed))
    print("  visitedNodes count: " .. tostring(#progress.visitedNodes))
    print("SUCCESSFULLY STORED AT: modData.npcDialogueHistory[\"" .. npcID .. "\"].sessionProgress[\"" .. sessionID .. "\"]")
    print("RETURNING: true")
    print("========================================")

    return true
end

function NPC_DialogueSessionManager.hasIncompleteProgress(player, npcID, sessionID)
    print("========================================")
    print("=== hasIncompleteProgress() CALLED ===")
    print("========================================")
    print("PARAMS: npcID=[" .. tostring(npcID) .. "] sessionID=[" .. tostring(sessionID) .. "]")

    if not player then
        print("RETURN FALSE: player is nil")
        print("========================================")
        return false
    end
    print("CHECK PASSED: player exists")

    local modData = player:getModData()
    if not modData then
        print("RETURN FALSE: modData is nil")
        print("========================================")
        return false
    end
    print("CHECK PASSED: modData exists")

    if not modData.npcDialogueHistory then
        print("RETURN FALSE: modData.npcDialogueHistory is nil")
        print("========================================")
        return false
    end
    print("CHECK PASSED: modData.npcDialogueHistory exists")

    local npcHistory = modData.npcDialogueHistory[npcID]
    if not npcHistory then
        print("RETURN FALSE: npcHistory for [" .. tostring(npcID) .. "] is nil")
        print("========================================")
        return false
    end
    print("CHECK PASSED: npcHistory exists for [" .. tostring(npcID) .. "]")

    if not npcHistory.sessionProgress then
        print("RETURN FALSE: npcHistory.sessionProgress is nil")
        print("CRITICAL: npcHistory exists but sessionProgress subtable is missing")
        print("npcHistory table contents:")
        for k, v in pairs(npcHistory) do
            print("  [" .. tostring(k) .. "] = " .. tostring(v) .. " (type: " .. type(v) .. ")")
        end
        print("========================================")
        return false
    end
    print("CHECK PASSED: npcHistory.sessionProgress exists")

    local progress = npcHistory.sessionProgress[sessionID]
    if not progress then
        print("RETURN FALSE: progress entry for sessionID [" .. tostring(sessionID) .. "] is nil")
        print("sessionID length: " .. tostring(string.len(sessionID)))
        print("Available progress entries in sessionProgress:")
        for k, v in pairs(npcHistory.sessionProgress) do
            print("  [" .. tostring(k) .. "] (length: " .. tostring(string.len(k)) .. ") = " .. tostring(v))
        end
        print("========================================")
        return false
    end
    print("CHECK PASSED: progress entry exists for sessionID [" .. tostring(sessionID) .. "]")

    print("Progress data structure:")
    print("  started: " .. tostring(progress.started) .. " (type: " .. type(progress.started) .. ")")
    print("  completed: " .. tostring(progress.completed) .. " (type: " .. type(progress.completed) .. ")")
    print("  lastNode: " .. tostring(progress.lastNode))
    print("  lastTimestamp: " .. tostring(progress.lastTimestamp))
    print("  visitedNodes count: " .. tostring(#progress.visitedNodes))

    local result = progress.started and not progress.completed
    print("Logic evaluation: progress.started and not progress.completed")
    print("  progress.started = " .. tostring(progress.started))
    print("  progress.completed = " .. tostring(progress.completed))
    print("  not progress.completed = " .. tostring(not progress.completed))
    print("  FINAL RESULT = " .. tostring(result))
    print("RETURNING: " .. tostring(result))
    print("========================================")

    return result
end

function NPC_DialogueSessionManager.getResumeNode(player, npcID, sessionID)
    if not player then
        return nil
    end

    local modData = player:getModData()
    if not modData or not modData.npcDialogueHistory then
        return nil
    end

    local npcHistory = modData.npcDialogueHistory[npcID]
    if not npcHistory or not npcHistory.sessionProgress then
        return nil
    end

    local progress = npcHistory.sessionProgress[sessionID]
    if not progress or not progress.lastNode then
        return nil
    end

    return progress.lastNode
end

function NPC_DialogueSessionManager.markSessionCompleted(player, npcID, sessionID)
    if not player then
        return false
    end

    local modData = player:getModData()
    if not modData or not modData.npcDialogueHistory then
        return false
    end

    local npcHistory = modData.npcDialogueHistory[npcID]
    if not npcHistory or not npcHistory.sessionProgress then
        return false
    end

    local progress = npcHistory.sessionProgress[sessionID]
    if not progress then
        return false
    end

    progress.completed = true
    progress.completedTimestamp = getTimestampMs() / 1000.0

    return true
end

function NPC_DialogueSessionManager.isSessionCompleted(player, npcID, sessionID)
    if not player then
        return false
    end

    local modData = player:getModData()
    if not modData or not modData.npcDialogueHistory then
        return false
    end

    local npcHistory = modData.npcDialogueHistory[npcID]
    if not npcHistory or not npcHistory.sessionProgress then
        return false
    end

    local progress = npcHistory.sessionProgress[sessionID]
    if not progress then
        return false
    end

    return progress.completed == true
end

function NPC_DialogueSessionManager.trackOptionSelection(player, npcID, sessionID, nodeID, optionID)
    if not player then
        return false
    end

    local modData = player:getModData()
    if not modData or not modData.npcDialogueHistory then
        return false
    end

    local npcHistory = modData.npcDialogueHistory[npcID]
    if not npcHistory or not npcHistory.sessionProgress then
        return false
    end

    local progress = npcHistory.sessionProgress[sessionID]
    if not progress then
        return false
    end

    progress.selectedOptions[nodeID] = optionID

    return true
end

function NPC_DialogueSessionManager.cleanupOldProgress(player)
    if not player then
        return false
    end

    local modData = player:getModData()
    if not modData or not modData.npcDialogueHistory then
        return false
    end

    local NPC_DialogueConfig = require("DialogueFramework/Dialogue/NPC_DialogueConfig")
    local cleanupDays = NPC_DialogueConfig.RESUME_SYSTEM.AUTO_CLEANUP_DAYS
    local currentTime = getTimestampMs() / 1000.0
    local cleanupThreshold = cleanupDays * 24 * 60 * 60

    for npcID, npcHistory in pairs(modData.npcDialogueHistory) do
        if npcHistory.sessionProgress then
            for sessionID, progress in pairs(npcHistory.sessionProgress) do
                if progress.completed and progress.completedTimestamp then
                    local timeSinceCompletion = currentTime - progress.completedTimestamp

                    if timeSinceCompletion > cleanupThreshold then
                        npcHistory.sessionProgress[sessionID] = nil
                    end
                end
            end
        end
    end

    return true
end

function NPC_DialogueSessionManager.migrateOldSaveData(player)
    if not player then
        return false
    end

    local modData = player:getModData()
    if not modData or not modData.npcDialogueHistory then
        return false
    end

    for npcID, history in pairs(modData.npcDialogueHistory) do
        if not history.sessionProgress then
            history.sessionProgress = {}

            if history.firstMeeting then
                history.sessionProgress[npcID .. "_greeting_first"] = {
                    started = true,
                    completed = true,
                    lastNode = "EXIT",
                    completedTimestamp = history.firstMeeting,
                    visitedNodes = {},
                    selectedOptions = {}
                }
            end
        end
    end

    return true
end

return NPC_DialogueSessionManager
