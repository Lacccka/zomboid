local NPC_DialogueEngine = {}

if not Events.OnNPCDialogueStarted then
    LuaEventManager.AddEvent("OnNPCDialogueStarted")
end

if not Events.OnNPCDialogueEnded then
    LuaEventManager.AddEvent("OnNPCDialogueEnded")
end

local NPC_DialogueConfig = require("DialogueFramework/Dialogue/NPC_DialogueConfig")
local NPC_DialogueSessionManager = require("DialogueFramework/Dialogue/NPC_DialogueSessionManager")
local NPC_SoundManager = require("DialogueFramework/Sound/NPC_SoundManager")
local NPC_DialogueConditions = require("DialogueFramework/Dialogue/NPC_DialogueConditions")
local MUGGY_EnvironmentDetector = require("MuggyMod/MUGGY_EnvironmentDetector")

local registeredDefinitionProviders = {}

local function findNodeAcrossSessions(npcID, nodeID, currentSessionDef)
    if not npcID or not nodeID then
        return nil
    end

    local definitionProvider = registeredDefinitionProviders[npcID]
    if not definitionProvider then
        return nil
    end

    if not definitionProvider.getAllSessionsForNPC then
        return nil
    end

    local allSessions = definitionProvider.getAllSessionsForNPC(npcID)
    if not allSessions then
        return nil
    end

    for _, sessionDef in ipairs(allSessions) do
        if sessionDef ~= currentSessionDef and sessionDef.nodes then
            local node = sessionDef.nodes[nodeID]
            if node then
                return node
            end
        end
    end

    return nil
end

local function getNodeByID(sessionDef, nodeID)
    if not sessionDef or not nodeID then
        return nil
    end

    local node = sessionDef.nodes[nodeID]

    if not node and sessionDef.npcID then
        node = findNodeAcrossSessions(sessionDef.npcID, nodeID, sessionDef)
    end

    return node
end

function NPC_DialogueEngine.startSession(player, npc, sessionID)
    if not player or not npc then
        return false
    end

    if NPC_DialogueSessionManager.hasActiveSession(player) then
        return false
    end

    if isServer() then
        local NPC_DialogueLockRegistry = require("DialogueFramework/Dialogue/NPC_DialogueLockRegistry")
        local success, reason = NPC_DialogueLockRegistry.tryAcquireLock(npc, player)

        if not success then
            return false, reason
        end
    end

    local sessionDef = NPC_DialogueEngine.getSessionDefinition(sessionID, player, npc)

    if not sessionDef then
        if isServer() then
            local NPC_DialogueLockRegistry = require("DialogueFramework/Dialogue/NPC_DialogueLockRegistry")
            NPC_DialogueLockRegistry.releaseLock(npc, player)
        end

        return false
    end

    local resumeNode = nil
    if sessionDef.resumable and NPC_DialogueConfig.RESUME_SYSTEM.ENABLED then
        resumeNode = NPC_DialogueSessionManager.getResumeNode(
            player, sessionDef.npcID, sessionDef.sessionID
        )

        if resumeNode then
            local resumeNodeData = getNodeByID(sessionDef, resumeNode)
            if resumeNodeData then
                local nodeType = NPC_DialogueConfig.getNodeType(resumeNodeData)
                if nodeType == NPC_DialogueConfig.NODE_TYPES.NPC_TRADING and
                   not NPC_DialogueConfig.RESUME_SYSTEM.ALLOW_TRADING_NODE_RESUME then
                    resumeNode = nil
                end
            else
                resumeNode = nil
            end
        end
    end

    local session = {
        sessionID = sessionDef.sessionID,
        sessionDef = sessionDef,
        npcEntity = npc,
        playerEntity = player,
        currentNode = resumeNode or sessionDef.rootNode,
        previousNodes = {},
        currentSoundHandle = nil,
        startTime = getTimestampMs() / 1000.0,
        isResuming = resumeNode ~= nil
    }

    NPC_DialogueSessionManager.registerSession(player, session)

    triggerEvent("OnNPCDialogueStarted", npc, player, session)

    NPC_DialogueSessionManager.saveSessionProgress(player, session)

    local NPC_BehaviorConfig = require("DialogueFramework/Behavior/NPC_BehaviorConfig")
    local NPC_BehaviorController = require("DialogueFramework/Behavior/NPC_BehaviorController")
    NPC_BehaviorController.queueBehavior(
        npc,
        "talking",
        {
            player = player
        },
        {
            priority = NPC_BehaviorConfig.PRIORITY.TALKING
        }
    )

    local startNode = getNodeByID(sessionDef, session.currentNode)
    if startNode and startNode.onEnter then
        local success, error = pcall(function()
            startNode.onEnter(player, npc, session)
        end)
        if not success then
        end
    end

    if startNode and startNode.soundFile then
        local soundHandle = NPC_SoundManager.playDialogueSound(npc, startNode.soundFile, session.sessionID)
        session.currentSoundHandle = soundHandle
    end

    if NPC_DialogueConfig.isTradingNode(startNode) then
        if MUGGY_EnvironmentDetector.isSinglePlayer() or isClient() then
            NPC_DialogueEngine.openTradingUI(player, npc, session, startNode)
        end
        return true, session
    elseif NPC_DialogueConfig.isInfoNode(startNode) then
        if MUGGY_EnvironmentDetector.isSinglePlayer() or isClient() then
            NPC_DialogueEngine.openInfoUI(player, npc, session, startNode)
        end
        return true, session
    end

    local NPC_BehaviorController = require("DialogueFramework/Behavior/NPC_BehaviorController")
    NPC_BehaviorController.queueBehavior(npc, "follow_player", { player = player }, {
        priority = 5
    })

    local NPC_Behavior_Guarding = require("DialogueFramework/Behavior/Behaviors/NPC_Behavior_Guarding")
    NPC_Behavior_Guarding.refresh(npc, player)

    local NPC_DialoguePositioning = require("DialogueFramework/Dialogue/NPC_DialoguePositioning")
    NPC_DialoguePositioning.updatePosition(player, npc)

    return true, session
end

function NPC_DialogueEngine.endSession(player)
    if not player then
        return false
    end

    local session = NPC_DialogueSessionManager.getSession(player)
    if not session then
        return false
    end

    NPC_DialogueSessionManager.saveSessionProgress(player, session)

    NPC_DialogueSessionManager.cleanupOldProgress(player)

    if session.sessionDef and session.sessionDef.removeItemOnComplete then
        local itemType = session.sessionDef.requiredItem
        local flagKey = session.sessionDef.itemRemovedFlag

        if itemType then
            if isClient() then
                sendClientCommand(player, "NPCDialogue", "RemoveRequiredItem", {
                    itemType = itemType,
                    flagKey = flagKey
                })
            elseif isServer() then
                local inventory = player:getInventory()
                if inventory then
                    local item = inventory:getFirstTypeRecurse(itemType)
                    if item then
                        inventory:Remove(item)

                        if flagKey then
                            local modData = player:getModData()
                            if modData then
                                modData[flagKey] = true
                            end
                        end
                    end
                end
            end
        end
    end

    if session.npcEntity and session.sessionID then
        NPC_SoundManager.stopDialogueSound(session.npcEntity, session.sessionID)
        NPC_SoundManager.cleanupSession(session.sessionID)
    end

    if session.npcEntity then
        local NPC_Behavior_Talking = require("DialogueFramework/Behavior/Behaviors/NPC_Behavior_Talking")
        NPC_Behavior_Talking.cleanup(session.npcEntity)

        local NPC_BehaviorController = require("DialogueFramework/Behavior/NPC_BehaviorController")
        NPC_BehaviorController.onDialogueEnd(session.npcEntity)
    end

    if isServer() and session.npcEntity then
        local NPC_DialogueLockRegistry = require("DialogueFramework/Dialogue/NPC_DialogueLockRegistry")
        NPC_DialogueLockRegistry.releaseLock(session.npcEntity, player)
    end

    NPC_DialogueSessionManager.unregisterSession(player)

    triggerEvent("OnNPCDialogueEnded", session.npcEntity, player)

    return true
end

function NPC_DialogueEngine.getCurrentNode(player)
    local session = NPC_DialogueSessionManager.getSession(player)
    if not session then
        return nil
    end

    local sessionDef = session.sessionDef
    if not sessionDef then
        return nil
    end

    return getNodeByID(sessionDef, session.currentNode)
end

function NPC_DialogueEngine.getAvailableOptions(player)
    local node = NPC_DialogueEngine.getCurrentNode(player)
    if not node then
        return {}
    end

    local session = NPC_DialogueSessionManager.getSession(player)
    if not session then
        return {}
    end

    local availableOptions = {}

    if node.options then
        for _, option in ipairs(node.options) do
            local isAvailable = true

            if option.condition then
                local success, result = pcall(function()
                    return option.condition(session.playerEntity, session.npcEntity)
                end)

                if success then
                    isAvailable = result
                else
                    isAvailable = false
                end
            end

            if isAvailable then
                table.insert(availableOptions, option)
            end
        end
    end

    return availableOptions
end

function NPC_DialogueEngine.resolveNextNode(nodeData, player, npc)
    if not nodeData then
        return nil
    end

    if nodeData.conditionY and nodeData.nextNodeY then
        local success, result = pcall(function()
            return nodeData.conditionY(player, npc)
        end)

        if success and result then
            return nodeData.nextNodeY
        end
    end

    if nodeData.conditionX and nodeData.nextNodeX then
        local success, result = pcall(function()
            return nodeData.conditionX(player, npc)
        end)

        if success and result then
            return nodeData.nextNodeX
        end
    end

    return nodeData.nextNode
end

function NPC_DialogueEngine.selectOption(player, optionID)
    local session = NPC_DialogueSessionManager.getSession(player)
    if not session then
        return false
    end

    local sessionDef = session.sessionDef
    if not sessionDef then
        return false
    end

    local currentNode = getNodeByID(sessionDef, session.currentNode)
    if not currentNode then
        return false
    end

    local selectedOption = nil
    for _, option in ipairs(currentNode.options) do
        if option.id == optionID then
            selectedOption = option
            break
        end
    end

    if not selectedOption then
        return false
    end

    NPC_DialogueSessionManager.trackOptionSelection(
        player,
        sessionDef.npcID,
        sessionDef.sessionID,
        session.currentNode,
        optionID
    )

    if selectedOption.onSelect then
        local success, error = pcall(function()
            selectedOption.onSelect(session.playerEntity, session.npcEntity, session)
        end)
        if not success then
        end
    end

    local nextNodeID = NPC_DialogueEngine.resolveNextNode(
        selectedOption,
        session.playerEntity,
        session.npcEntity
    )

    if nextNodeID == NPC_DialogueConfig.SPECIAL_NODES.EXIT then
        return true, "EXIT"
    end

    if nextNodeID == NPC_DialogueConfig.SPECIAL_NODES.BACK then
        local previousNode = NPC_DialogueSessionManager.popPreviousNode(player)
        if previousNode then
            nextNodeID = previousNode
        else
            nextNodeID = sessionDef.rootNode
        end
    else
        local currentNode = getNodeByID(sessionDef, session.currentNode)
        if NPC_DialogueConfig.isStandardNode(currentNode) then
            NPC_DialogueSessionManager.pushPreviousNode(player, session.currentNode)
        end
    end

    local nextNode = getNodeByID(sessionDef, nextNodeID)
    if not nextNode then
        return false
    end

    session.currentNode = nextNodeID
    NPC_DialogueSessionManager.updateSessionNode(player, nextNodeID)

    if NPC_DialogueConfig.isNPCResponseNode(nextNode) then
        local NPC_DialoguePositioning = require("DialogueFramework/Dialogue/NPC_DialoguePositioning")
        NPC_DialoguePositioning.updatePosition(session.playerEntity, session.npcEntity)
    end

    if nextNode.completesSession then
        NPC_DialogueSessionManager.markSessionCompleted(
            player,
            sessionDef.npcID,
            sessionDef.sessionID
        )
    end

    NPC_DialogueSessionManager.saveSessionProgress(player, session)

    if nextNode.onEnter then
        local success, error = pcall(function()
            nextNode.onEnter(session.playerEntity, session.npcEntity, session)
        end)
        if not success then
        end
    end

    if nextNode.soundFile then
        local soundHandle = NPC_SoundManager.playDialogueSound(session.npcEntity, nextNode.soundFile, session.sessionID)
        session.currentSoundHandle = soundHandle
    end

    return true, nextNodeID
end

function NPC_DialogueEngine.advanceToNode(player, nextNodeID)
    local session = NPC_DialogueSessionManager.getSession(player)
    if not session then
        return false
    end

    local sessionDef = session.sessionDef
    if not sessionDef then
        return false
    end

    if nextNodeID == NPC_DialogueConfig.SPECIAL_NODES.EXIT then
        return false
    end

    if nextNodeID == NPC_DialogueConfig.SPECIAL_NODES.BACK then
        local previousNode = NPC_DialogueSessionManager.popPreviousNode(player)
        if previousNode then
            nextNodeID = previousNode
        else
            nextNodeID = sessionDef.rootNode
        end
    else
        local currentNode = getNodeByID(sessionDef, session.currentNode)
        if NPC_DialogueConfig.isStandardNode(currentNode) then
            NPC_DialogueSessionManager.pushPreviousNode(player, session.currentNode)
        end
    end

    local nextNode = getNodeByID(sessionDef, nextNodeID)
    if not nextNode then
        return false
    end

    session.currentNode = nextNodeID
    NPC_DialogueSessionManager.updateSessionNode(player, nextNodeID)

    if NPC_DialogueConfig.isNPCResponseNode(nextNode) then
        local NPC_DialoguePositioning = require("DialogueFramework/Dialogue/NPC_DialoguePositioning")
        NPC_DialoguePositioning.updatePosition(session.playerEntity, session.npcEntity)
    end

    if nextNode.completesSession then
        NPC_DialogueSessionManager.markSessionCompleted(
            player,
            sessionDef.npcID,
            sessionDef.sessionID
        )
    end

    NPC_DialogueSessionManager.saveSessionProgress(player, session)

    if nextNode.onEnter then
        local success, error = pcall(function()
            nextNode.onEnter(session.playerEntity, session.npcEntity, session)
        end)
        if not success then
        end
    end

    if nextNode.soundFile then
        local soundHandle = NPC_SoundManager.playDialogueSound(session.npcEntity, nextNode.soundFile, session.sessionID)
        session.currentSoundHandle = soundHandle
    end

    return true
end

function NPC_DialogueEngine.hasActiveSession(player)
    return NPC_DialogueSessionManager.hasActiveSession(player)
end

function NPC_DialogueEngine.registerDefinitionProvider(npcID, definitionProvider)
    if not npcID or not definitionProvider then
        return false
    end

    registeredDefinitionProviders[npcID] = definitionProvider
    return true
end

function NPC_DialogueEngine.getSessionDefinition(sessionID, player, npc)
    print("========================================")
    print("=== getSessionDefinition() CALLED ===")
    print("========================================")
    print("PARAMS: sessionID=[" .. tostring(sessionID) .. "] player=[" .. tostring(player) .. "] npc=[" .. tostring(npc) .. "]")

    if not npc then
        print("RETURN NIL: npc is nil")
        print("========================================")
        return nil
    end
    print("CHECK PASSED: npc exists")

    local npcID = nil

    if npc.getVariable then
        if npc:getVariable("currentAnimalMuggy") then
            npcID = "muggy"
            print("NPC ID DETECTED: [muggy]")
        end
    end

    if not npcID then
        print("RETURN NIL: npcID not detected")
        print("========================================")
        return nil
    end
    print("CHECK PASSED: npcID = [" .. npcID .. "]")

    local definitionProvider = registeredDefinitionProviders[npcID]
    if not definitionProvider then
        print("RETURN NIL: no definition provider for npcID [" .. npcID .. "]")
        print("========================================")
        return nil
    end
    print("CHECK PASSED: definition provider exists")

    if definitionProvider.getAllSessionsForNPC then
        local allSessions = definitionProvider.getAllSessionsForNPC(npcID)
        print("GOT ALL SESSIONS: " .. tostring(#allSessions) .. " sessions available")
        for i, sessionDef in ipairs(allSessions) do
            print("  [" .. i .. "] " .. tostring(sessionDef.sessionID))
        end

        local candidates = {}

        print("--- PHASE 1: ITEM OVERRIDE CHECK ---")
        for i, sessionDef in ipairs(allSessions) do
            if sessionDef.requiredItem and sessionDef.itemOverrideEnabled then
                print("  Session [" .. sessionDef.sessionID .. "] has item override (requiredItem: " .. tostring(sessionDef.requiredItem) .. ")")
                if NPC_DialogueConditions.hasItemRecursive(player, sessionDef.requiredItem) then
                    print("    Player HAS required item")
                    local conditionMet = true

                    if sessionDef.sessionCondition then
                        local success, result = pcall(function()
                            return sessionDef.sessionCondition(player, npc)
                        end)

                        if success then
                            conditionMet = result
                        else
                            conditionMet = false
                        end
                        print("    Session condition evaluated: " .. tostring(conditionMet))
                    end

                    if conditionMet then
                        table.insert(candidates, {
                            session = sessionDef,
                            priority = sessionDef.overridePriority or 0
                        })
                        print("    ADDED TO CANDIDATES with priority " .. tostring(sessionDef.overridePriority or 0))
                    end
                else
                    print("    Player DOES NOT have required item")
                end
            end
        end

        if #candidates > 0 then
            print("ITEM OVERRIDE CANDIDATES FOUND: " .. tostring(#candidates))
            table.sort(candidates, function(a, b)
                return a.priority > b.priority
            end)

            print("RETURNING HIGHEST PRIORITY ITEM OVERRIDE SESSION: " .. candidates[1].session.sessionID)
            print("========================================")
            return candidates[1].session
        else
            print("NO ITEM OVERRIDE CANDIDATES FOUND")
        end

        print("--- PHASE 2: RESUMABLE SESSION CHECK ---")
        for i, sessionDef in ipairs(allSessions) do
            print("  [" .. i .. "] Checking session: " .. tostring(sessionDef.sessionID))
            print("      resumable field: " .. tostring(sessionDef.resumable) .. " (type: " .. type(sessionDef.resumable) .. ")")

            if sessionDef.resumable then
                print("      Session IS resumable, calling hasIncompleteProgress...")
                local hasIncomplete = NPC_DialogueSessionManager.hasIncompleteProgress(
                    player, npcID, sessionDef.sessionID
                )
                print("      hasIncompleteProgress returned: " .. tostring(hasIncomplete))

                if hasIncomplete then
                    print("  RESUMABLE SESSION FOUND: " .. sessionDef.sessionID)
                    print("  RETURNING RESUMABLE SESSION")
                    print("========================================")
                    return sessionDef
                else
                    print("      Session not incomplete, continuing search...")
                end
            else
                print("      Session is NOT resumable (field missing or false), skipping")
            end
        end
        print("NO RESUMABLE SESSIONS FOUND")
    end

    print("--- PHASE 3: FALLBACK TO getSessionForNPC ---")
    if definitionProvider.getSessionForNPC then
        print("CALLING getSessionForNPC fallback for npcID [" .. npcID .. "]")
        local fallbackSession = definitionProvider.getSessionForNPC(npcID, player)
        if fallbackSession then
            print("FALLBACK RETURNED SESSION: " .. tostring(fallbackSession.sessionID))
        else
            print("FALLBACK RETURNED NIL")
        end
        print("========================================")
        return fallbackSession
    end

    print("NO DEFINITION PROVIDER getSessionForNPC METHOD")
    print("RETURNING NIL")
    print("========================================")
    return nil
end

function NPC_DialogueEngine.openTradingUI(player, npc, session, tradingNode)
    print("========================================")
    print("=== openTradingUI() CALLED ===")
    print("========================================")
    print("isClient() = " .. tostring(isClient()))
    print("isSinglePlayer() = " .. tostring(MUGGY_EnvironmentDetector.isSinglePlayer()))
    print("Combined check (isSinglePlayer or isClient) = " .. tostring(MUGGY_EnvironmentDetector.isSinglePlayer() or isClient()))

    if not (MUGGY_EnvironmentDetector.isSinglePlayer() or isClient()) then
        print("RETURN: Not client and not singleplayer - cannot create UI")
        print("========================================")
        return
    end

    print("Loading Trading modules...")
    local NPC_TradingConfig = require("DialogueFramework/Trading/NPC_TradingConfig")
    local NPC_TradingUI = require("DialogueFramework/Trading/NPC_TradingUI")
    print("Modules loaded successfully")

    local isSimple = NPC_TradingConfig.isSimpleSystemActive()
    print("isSimpleSystemActive() = " .. tostring(isSimple))

    if isSimple then
        print("Creating Simple Trading UI...")

        local success, errorMsg = pcall(function()
            local tradingUI = NPC_TradingUI.Simple:new(player, session, npc)
            print("Trading UI instance created")

            print("Calling initialise()...")
            tradingUI:initialise()
            print("initialise() completed")

            print("Adding to UI manager...")
            tradingUI:addToUIManager()
            print("Trading UI added to UI manager successfully")
        end)

        if not success then
            print("ERROR CREATING TRADING UI: " .. tostring(errorMsg))
        end
        print("========================================")
    else
        print("Creating Value Trading UI with backpack...")
        local NPC_SpawnBackpackTimedAction = require("DialogueFramework/Trading/NPC_SpawnBackpackTimedAction")
        local action = NPC_SpawnBackpackTimedAction:new(player, npc)
        action:setOnComplete(function()
            local tradingUI = NPC_TradingUI.Value:new(player, session, npc)
            tradingUI:initialise()
            tradingUI:addToUIManager()

            local NPC_BackpackScanner = require("DialogueFramework/Trading/NPC_BackpackScanner")
            NPC_BackpackScanner.startPeriodicScan(player, session.tradingBackpack)
        end)
        ISTimedActionQueue.add(action)
        print("Backpack spawn action queued")
        print("========================================")
    end
end

function NPC_DialogueEngine.openInfoUI(player, npc, session, infoNode)
    if not (MUGGY_EnvironmentDetector.isSinglePlayer() or isClient()) then
        return
    end

    local NPC_InfoUI = require("DialogueFramework/UI/NPC_InfoUI")
    local infoUI = NPC_InfoUI:new(player, session, npc, infoNode)
    infoUI:initialise()
    infoUI:addToUIManager()
end

return NPC_DialogueEngine
