require "LCCQF/LCCQFConstants"
require "LCCQF/Core/LCCQFNPCRegistry"
require "LCCQF/Core/LCCQFNPCRuntime"
require "LCCQF/Content/LCCQFNPCDefinitions"
require "LCCQF/Runtime/LCCQFBanditsRuntime"
require "LCCQF/Content/LCCQFQuestDefinitions"
require "LCCQF/Quest/LCCQFQuestService"
require "LCCQF/Dialogue/LCCQFDialogueSession"

LCCQFInteractionServer = LCCQFInteractionServer or {}

local C = LCCQF.Constants
local DialogueSession = LCCQF.DialogueSession
local QuestService = LCCQF.QuestService
local lastCommandMs = {}
local nextRuntimeReconcileMs = 0
local nextQuestUpdateMs = 0

local function log(message)
    print(C.LOG_PREFIX .. "[SERVER] " .. tostring(message))
end

local function getPlayerKey(player)
    if not player then return nil end
    if player.getOnlineID then return tostring(player:getOnlineID()) end
    if player.getUsername then return tostring(player:getUsername()) end
    return nil
end

local function allowCommand(player, command)
    local playerKey = getPlayerKey(player)
    if not playerKey then return false end

    local byCommand = lastCommandMs[playerKey] or {}
    lastCommandMs[playerKey] = byCommand
    local now = getTimestampMs()
    local last = byCommand[command]
    if last and now - last < C.REQUEST_COOLDOWN_MS then return false end
    byCommand[command] = now
    byCommand.lastSeen = now
    return true
end

local function pruneCommandHistory()
    local now = getTimestampMs()
    for playerKey, byCommand in pairs(lastCommandMs) do
        if not byCommand.lastSeen or now - byCommand.lastSeen >= C.COMMAND_HISTORY_TIMEOUT_MS then
            lastCommandMs[playerKey] = nil
        end
    end
end

local function readIdentifier(args, field)
    if type(args) ~= "table" then return nil end
    local value = args[field]
    if type(value) ~= "string" or value == "" or #value > C.MAX_IDENTIFIER_LENGTH then return nil end
    return value
end

local function sendStatus(player, messageKey)
    if not player then return end
    sendServerCommand(player, C.MODULE, C.COMMAND.STATUS, {
        messageKey = tostring(messageKey or "IGUI_LCCQF_Status_GenericError"),
    })
end

local function sendDialogueState(player, view)
    sendServerCommand(player, C.MODULE, C.COMMAND.DIALOGUE_STATE, view)
end

local function sendDialogueClosed(player, sessionId)
    sendServerCommand(player, C.MODULE, C.COMMAND.DIALOGUE_CLOSED, {
        sessionId = tostring(sessionId or ""),
    })
end

local function sendQuestState(player)
    if not player then return end
    local quests = QuestService.ExportViews(player)
    sendServerCommand(player, C.MODULE, C.COMMAND.QUESTS, {
        quests = quests,
    })
    log("quest state sent player=" .. tostring(player:getUsername()) .. " count=" .. tostring(#quests))
end

local function onQuestEvent(kind, player, payload)
    if not player or type(payload) ~= "table" then return end

    if kind == "upsert" then
        sendServerCommand(player, C.MODULE, C.COMMAND.QUEST_UPSERT, payload)
    elseif kind == "event" then
        sendServerCommand(player, C.MODULE, C.COMMAND.QUEST_EVENT, payload)
    end
end

local function getRuntimeAdapter()
    return LCCQF.NPCRuntime.GetAdapter("Bandits")
end

local function reconcileRuntimeBindings()
    local adapter = getRuntimeAdapter()
    if adapter and adapter.ReconcileRuntimeBindings then
        return adapter.ReconcileRuntimeBindings()
    end
    return 0, 0
end

local function refreshRuntimeBindings()
    reconcileRuntimeBindings()
    local adapter = getRuntimeAdapter()
    if adapter and adapter.RefreshRuntimeBindings then
        return adapter.RefreshRuntimeBindings()
    end
    return 0
end

local function sendRuntimeBindings(player)
    if not player then return end
    refreshRuntimeBindings()
    local bindings = LCCQF.NPCRuntime.ExportRuntimeBindings()
    sendServerCommand(player, C.MODULE, C.COMMAND.RUNTIME_BINDINGS, {
        bindings = bindings,
    })
    log("runtime bindings sent player=" .. tostring(player:getUsername()) .. " count=" .. tostring(#bindings))
end

local function broadcastRuntimeBinding(handle)
    if not handle or not handle.runtimeId or not handle.npcId then return end

    local payload = {
        runtimeId = tostring(handle.runtimeId),
        npcId = tostring(handle.npcId),
        x = handle.x,
        y = handle.y,
        z = handle.z,
    }
    sendServerCommand(C.MODULE, C.COMMAND.RUNTIME_BINDING_UPSERT, payload)

    log("runtime binding broadcast npcId=" .. tostring(handle.npcId)
        .. " runtimeId=" .. tostring(handle.runtimeId)
        .. " anchor=" .. tostring(handle.x) .. "," .. tostring(handle.y) .. "," .. tostring(handle.z))
end

local function broadcastRuntimeBindingRemoval(handle, reason)
    if not handle or not handle.runtimeId or not handle.npcId then return end

    local runtimeId = tostring(handle.runtimeId)
    local npcId = tostring(handle.npcId)
    local closedSessions = DialogueSession.InvalidateRuntime(runtimeId)

    sendServerCommand(C.MODULE, C.COMMAND.RUNTIME_BINDING_REMOVE, {
        runtimeId = runtimeId,
        npcId = npcId,
        reason = tostring(reason or "invalidated"),
    })

    log("runtime binding removed npcId=" .. npcId
        .. " runtimeId=" .. runtimeId
        .. " reason=" .. tostring(reason or "invalidated")
        .. " closedSessions=" .. tostring(closedSessions))
end

local function onRuntimeBindingEvent(kind, handle, reason)
    if kind == "remove" then
        broadcastRuntimeBindingRemoval(handle, reason)
    elseif kind == "upsert" then
        broadcastRuntimeBinding(handle)
    end
end

local function makeDialogueHooks(handle)
    return {
        IsChoiceAvailable = function(player, session, choice)
            return QuestService.EvaluateCondition(player, choice.condition)
        end,
        ExecuteAction = function(player, session, choice)
            return QuestService.ExecuteAction(player, choice.action, {
                giverNpcId = session.npcId,
                giverHandle = handle,
            })
        end,
    }
end

local function isPrivileged(player)
    if not player then return false end
    if isDebugEnabled and isDebugEnabled() then return true end
    if not player.getAccessLevel then return false end

    local access = tostring(player:getAccessLevel() or ""):lower()
    return access ~= "" and access ~= "none"
end

local function spawnTestNPC(player)
    if not isPrivileged(player) then
        sendStatus(player, "IGUI_LCCQF_Status_NoPermission")
        log("spawn rejected: insufficient privileges player=" .. tostring(player and player:getUsername()))
        return
    end

    local handle, result = LCCQF.NPCRuntime.Spawn(player, C.TEST_NPC_ID)
    if not handle then
        sendStatus(player, "IGUI_LCCQF_Status_SpawnFailed")
        log("spawn rejected npcId=" .. C.TEST_NPC_ID .. " reason=" .. tostring(result))
        return
    end

    broadcastRuntimeBinding(handle)

    if result == "already loaded" then
        sendStatus(player, "IGUI_LCCQF_Status_AlreadyNearby")
    elseif result == "already registered" then
        sendStatus(player, "IGUI_LCCQF_Status_AlreadyRegistered")
    else
        sendStatus(player, "IGUI_LCCQF_Status_Spawned")
    end
end

local function resolveDialogueHandle(player, npcId, runtimeId)
    local activeRuntimeId = LCCQF.NPCRuntime.GetActiveRuntimeId(npcId)
    if tostring(activeRuntimeId or "") ~= tostring(runtimeId or "") then
        return nil, "stale"
    end

    local handle = LCCQF.NPCRuntime.ResolveForPlayer(
        player,
        npcId,
        runtimeId,
        C.SERVER_INTERACTION_RANGE
    )
    if not handle then return nil, "unresolved" end
    return handle
end

local function requestDialogue(player, args)
    local npcId = readIdentifier(args, "npcId")
    local runtimeId = readIdentifier(args, "runtimeId")
    if not npcId or not runtimeId then
        sendStatus(player, "IGUI_LCCQF_Status_InvalidInteraction")
        return
    end

    local definition = LCCQF.NPCRegistry.Get(npcId)
    if not definition then
        sendStatus(player, "IGUI_LCCQF_Status_UnregisteredNPC")
        log("dialogue rejected: unknown npcId=" .. tostring(npcId))
        return
    end

    local handle, resolveReason = resolveDialogueHandle(player, npcId, runtimeId)
    if not handle then
        sendStatus(player, "IGUI_LCCQF_Status_NPCUnavailable")
        log("dialogue rejected: " .. tostring(resolveReason)
            .. " npcId=" .. npcId .. " runtimeId=" .. runtimeId)
        return
    end

    QuestService.NotifyTalkToNPC(player, npcId)

    local view, err = DialogueSession.Open(player, handle, definition, makeDialogueHooks(handle))
    if not view then
        sendStatus(player, "IGUI_LCCQF_Status_DialogueUnavailable")
        log("dialogue open failed npcId=" .. npcId .. " error=" .. tostring(err))
        return
    end

    sendDialogueState(player, view)
    log("dialogue opened session=" .. view.sessionId .. " player=" .. tostring(player:getUsername()) .. " npcId=" .. npcId)
end

local function chooseDialogue(player, args)
    local sessionId = readIdentifier(args, "sessionId")
    local choiceId = readIdentifier(args, "choiceId")
    if not sessionId or not choiceId then return end

    local session = DialogueSession.Get(player, sessionId)
    if not session then
        sendDialogueClosed(player, sessionId)
        return
    end

    local handle = resolveDialogueHandle(player, session.npcId, session.runtimeId)
    if not handle then
        DialogueSession.Close(player, sessionId)
        sendDialogueClosed(player, sessionId)
        sendStatus(player, "IGUI_LCCQF_Status_DialogueTooFar")
        return
    end

    local result, err = DialogueSession.Choose(
        player,
        sessionId,
        choiceId,
        makeDialogueHooks(handle)
    )
    if not result then
        log("choice rejected session=" .. sessionId .. " choice=" .. choiceId .. " error=" .. tostring(err))
        sendStatus(player, "IGUI_LCCQF_Status_ChoiceUnavailable")
        return
    end

    if result.closed then
        sendDialogueClosed(player, result.sessionId)
        log("dialogue finished session=" .. result.sessionId .. " player=" .. tostring(player:getUsername()))
    else
        sendDialogueState(player, result)
    end
end

local function closeDialogue(player, args)
    local sessionId = readIdentifier(args, "sessionId")
    if not sessionId then return end
    if DialogueSession.Close(player, sessionId) then
        log("dialogue closed session=" .. sessionId .. " player=" .. tostring(player:getUsername()))
    end
end

local function onClientCommand(module, command, player, args)
    if module ~= C.MODULE then return end

    local knownCommand = command == C.COMMAND.SPAWN_TEST_NPC
        or command == C.COMMAND.REQUEST_RUNTIME_BINDINGS
        or command == C.COMMAND.REQUEST_QUESTS
        or command == C.COMMAND.REQUEST_DIALOGUE
        or command == C.COMMAND.CHOOSE_DIALOGUE
        or command == C.COMMAND.CLOSE_DIALOGUE
    if not knownCommand or not allowCommand(player, command) then return end

    if command == C.COMMAND.SPAWN_TEST_NPC then
        spawnTestNPC(player)
    elseif command == C.COMMAND.REQUEST_RUNTIME_BINDINGS then
        sendRuntimeBindings(player)
    elseif command == C.COMMAND.REQUEST_QUESTS then
        sendQuestState(player)
    elseif command == C.COMMAND.REQUEST_DIALOGUE then
        requestDialogue(player, args)
    elseif command == C.COMMAND.CHOOSE_DIALOGUE then
        chooseDialogue(player, args)
    elseif command == C.COMMAND.CLOSE_DIALOGUE then
        closeDialogue(player, args)
    end
end

local function onTick()
    local now = getTimestampMs()

    if now >= nextRuntimeReconcileMs then
        nextRuntimeReconcileMs = now + C.RUNTIME_RECONCILE_INTERVAL_MS
        reconcileRuntimeBindings()
    end

    if now >= nextQuestUpdateMs then
        nextQuestUpdateMs = now + C.QUEST_UPDATE_INTERVAL_MS
        QuestService.Tick()
    end
end

local function onServerStarted()
    local adapter = getRuntimeAdapter()
    if adapter and adapter.SetBindingEventSink then
        adapter.SetBindingEventSink(onRuntimeBindingEvent)
    end
    QuestService.SetEventSink(onQuestEvent)

    local bindingCount = refreshRuntimeBindings()
    log("loaded version=" .. tostring(C.VERSION) .. " runtime=Bandits serverRange="
        .. tostring(C.SERVER_INTERACTION_RANGE) .. " restoredBindings=" .. tostring(bindingCount)
        .. " lifecycleReconcileMs=" .. tostring(C.RUNTIME_RECONCILE_INTERVAL_MS)
        .. " questUpdateMs=" .. tostring(C.QUEST_UPDATE_INTERVAL_MS))
end

if isServer and isServer() then
    Events.OnClientCommand.Add(onClientCommand)
    Events.OnTick.Add(onTick)
    Events.EveryOneMinute.Add(pruneCommandHistory)
    Events.OnServerStarted.Add(onServerStarted)
end

return LCCQFInteractionServer
