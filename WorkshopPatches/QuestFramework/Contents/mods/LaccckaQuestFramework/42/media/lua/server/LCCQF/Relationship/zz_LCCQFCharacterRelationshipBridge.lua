require "LCCQF/LCCQFConstants"
require "LCCQF/Core/LCCQFNPCRegistry"
require "LCCQF/Quest/LCCQFQuestRegistry"
require "LCCQF/Quest/LCCQFQuestService"
require "LCCQF/Persistence/LCCQFCharacterKnowledge"
require "LCCQF/Persistence/LCCQFCharacterRelationships"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local NPCRegistry = LCCQF.NPCRegistry
local QuestRegistry = LCCQF.QuestRegistry
local QuestService = LCCQF.QuestService
local CharacterKnowledge = LCCQF.CharacterKnowledge
local CharacterRelationships = LCCQF.CharacterRelationships
local observerInstalled = false
local reconciliationInstalled = false

local function log(message)
    print(C.LOG_PREFIX .. "[RELATIONSHIP:BRIDGE] " .. tostring(message))
end

local function rewardForQuest(questId)
    local definition = type(questId) == "string" and QuestRegistry.Get(questId) or nil
    if not definition or type(definition.relationshipReward) ~= "table" then return nil, nil end
    local npcId = definition.giverNpcId
    if type(npcId) ~= "string" or not NPCRegistry.Get(npcId) then return nil, nil end
    return npcId, definition.relationshipReward
end

local function applyQuestCompletionReward(player, questId, source)
    local npcId, reward = rewardForQuest(questId)
    if not npcId then return false, "no relationship reward" end

    return CharacterRelationships.Adjust(
        player,
        npcId,
        reward,
        source or "quest-completed",
        "quest-completed:" .. tostring(questId)
    )
end

local function reconcileQuestHistory(player)
    if not player or type(QuestService.ExportViews) ~= "function" then return 0 end
    local applied = 0
    for _, quest in ipairs(QuestService.ExportViews(player)) do
        if type(quest) == "table" and quest.state == "completed" and type(quest.questId) == "string" then
            local ok, changed = applyQuestCompletionReward(
                player,
                quest.questId,
                "quest-history-reconcile"
            )
            if ok and changed then applied = applied + 1 end
        end
    end
    return applied
end

local function onQuestEvent(kind, player, payload)
    if kind ~= "event" or not player or type(payload) ~= "table" then return end
    if payload.messageKey ~= "IGUI_LCCQF_QuestEvent_Completed" or payload.state ~= "completed" then return end

    local ok, changed = applyQuestCompletionReward(player, payload.questId, "quest-completed-live")
    if ok then
        log("quest relationship reward questId=" .. tostring(payload.questId)
            .. " changed=" .. tostring(changed))
    end
end

local function installQuestObserver()
    if observerInstalled then return true end
    if QuestService.__LCCQFRelationshipEventObserver then
        observerInstalled = true
        return true
    end
    if type(QuestService.SetEventSink) ~= "function" then return false end

    local originalSetEventSink = QuestService.SetEventSink
    QuestService.SetEventSink = function(sink)
        local downstream = type(sink) == "function" and sink or nil
        return originalSetEventSink(function(kind, player, payload)
            onQuestEvent(kind, player, payload)
            if downstream then downstream(kind, player, payload) end
        end)
    end

    QuestService.__LCCQFRelationshipEventObserver = true
    observerInstalled = true
    log("quest completion observer installed")
    return true
end

local function installKnowledgeReconciliation()
    if reconciliationInstalled then return true end
    if CharacterKnowledge.__LCCQFRelationshipReconcileWrapped then
        reconciliationInstalled = true
        return true
    end
    if type(CharacterKnowledge.ExportViews) ~= "function" then return false end

    local originalExportViews = CharacterKnowledge.ExportViews
    CharacterKnowledge.ExportViews = function(player)
        reconcileQuestHistory(player)
        return originalExportViews(player)
    end

    CharacterKnowledge.__LCCQFRelationshipReconcileWrapped = true
    reconciliationInstalled = true
    log("knowledge snapshot relationship reconciliation installed")
    return true
end

local function onRelationshipEvent(kind, player, payload)
    if kind ~= "upsert" or not player or type(payload) ~= "table" then return end
    local npcId = payload.npcId
    if type(npcId) ~= "string" then return end

    local view = CharacterKnowledge.GetView(player, npcId)
    if not view then return end
    view.knowledgeRevision = CharacterKnowledge.GetRevision(player)
    view.relationshipRevision = payload.relationshipRevision
    sendServerCommand(player, C.MODULE, C.COMMAND.KNOWN_PERSON_UPSERT, view)
end

local function onServerStarted()
    local ok, err = CharacterRelationships.Initialize()
    CharacterRelationships.SetEventSink(onRelationshipEvent)
    local observer = installQuestObserver()
    local reconcile = installKnowledgeReconciliation()
    log("loaded version=" .. tostring(C.VERSION)
        .. " persistence=" .. tostring(ok and "ready" or err or "unavailable")
        .. " questObserver=" .. tostring(observer)
        .. " historyReconcile=" .. tostring(reconcile))
end

installQuestObserver()
installKnowledgeReconciliation()

if isServer and isServer() then
    Events.OnServerStarted.Add(onServerStarted)
end

return true
