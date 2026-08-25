require "LCCQF/LCCQFConstants"
require "LCCQF/Quest/LCCQFQuestRegistry"
require "LCCQF/Quest/LCCQFQuestInstance"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local QuestRegistry = LCCQF.QuestRegistry
local QuestInstance = LCCQF.QuestInstance
local QuestService = LCCQF.QuestService or {}
local players = {}
local eventSink = nil

local function log(message)
    print(C.LOG_PREFIX .. "[QUEST:SERVER] " .. tostring(message))
end

local function getPlayerKey(player)
    if not player then return nil end
    if player.getOnlineID then return tostring(player:getOnlineID()) end
    if player.getUsername then return tostring(player:getUsername()) end
    return nil
end

local function getStore(player, create)
    local playerKey = getPlayerKey(player)
    if not playerKey then return nil, nil end

    local store = players[playerKey]
    if not store and create then
        store = {
            playerKey = playerKey,
            byInstanceId = {},
            byQuestId = {},
        }
        players[playerKey] = store
    end
    return store, playerKey
end

local function emit(kind, player, payload)
    if eventSink then eventSink(kind, player, payload) end
end

local function emitUpsert(player, instance)
    emit("upsert", player, QuestInstance.MakeView(instance))
end

local function emitEvent(player, instance, messageKey, objective)
    emit("event", player, {
        messageKey = messageKey,
        instanceId = instance and instance.id or nil,
        questId = instance and instance.questId or nil,
        objectiveId = objective and objective.id or nil,
        state = instance and instance.state or nil,
    })
end

local function completeCurrentObjective(player, instance, reason)
    local objective = QuestInstance.GetCurrentObjective(instance)
    if not objective then return false end

    local completed, questCompleted = QuestInstance.CompleteCurrentObjective(instance, reason)
    if not completed then return false end

    emitEvent(player, instance, "IGUI_LCCQF_QuestEvent_ObjectiveComplete", objective)
    emitUpsert(player, instance)

    log("objective completed player=" .. tostring(player:getUsername())
        .. " questId=" .. tostring(instance.questId)
        .. " instanceId=" .. tostring(instance.id)
        .. " objectiveId=" .. tostring(objective.id)
        .. " reason=" .. tostring(reason))

    if questCompleted then
        emitEvent(player, instance, "IGUI_LCCQF_QuestEvent_Completed", objective)
        log("quest completed player=" .. tostring(player:getUsername())
            .. " questId=" .. tostring(instance.questId)
            .. " instanceId=" .. tostring(instance.id))
    end
    return true
end

function QuestService.SetEventSink(sink)
    eventSink = type(sink) == "function" and sink or nil
end

function QuestService.GetPlayerKey(player)
    return getPlayerKey(player)
end

function QuestService.GetInstanceForQuest(player, questId)
    local store = getStore(player, false)
    if not store then return nil end
    local instanceId = store.byQuestId[questId]
    return instanceId and store.byInstanceId[instanceId] or nil
end

function QuestService.GetQuestState(player, questId)
    local instance = QuestService.GetInstanceForQuest(player, questId)
    if not instance then return "available" end
    return instance.state or "available"
end

function QuestService.EvaluateCondition(player, condition)
    if condition == nil then return true end
    if type(condition) ~= "table" then return false end

    if condition.kind == "questState" then
        local questId = condition.questId
        local expected = condition.state
        if type(questId) ~= "string" or type(expected) ~= "string" then return false end
        return QuestService.GetQuestState(player, questId) == expected
    end

    return false
end

function QuestService.Accept(player, questId, context)
    local definition = QuestRegistry.Get(questId)
    if not definition then return nil, "unknown quest" end
    if not context or tostring(context.giverNpcId or "") ~= tostring(definition.giverNpcId) then
        return nil, "invalid quest giver"
    end

    local store, playerKey = getStore(player, true)
    if not store or not playerKey then return nil, "player unavailable" end

    local previous = QuestService.GetInstanceForQuest(player, questId)
    if previous then
        if previous.state == "active" then return nil, "quest already active" end
        if definition.repeatable ~= true then return nil, "quest already completed" end
    end

    local instance, err = QuestInstance.Create(definition, playerKey, context)
    if not instance then return nil, err end

    store.byInstanceId[instance.id] = instance
    store.byQuestId[questId] = instance.id

    emitUpsert(player, instance)
    emitEvent(player, instance, "IGUI_LCCQF_QuestEvent_Accepted", QuestInstance.GetCurrentObjective(instance))

    log("quest accepted player=" .. tostring(player:getUsername())
        .. " questId=" .. tostring(questId)
        .. " instanceId=" .. tostring(instance.id)
        .. " giverNpcId=" .. tostring(definition.giverNpcId))
    return instance
end

function QuestService.ExecuteAction(player, action, context)
    if action == nil then return true end
    if type(action) ~= "table" then return false, "invalid dialogue action" end

    if action.kind == "questAccept" then
        local instance, err = QuestService.Accept(player, action.questId, context)
        return instance ~= nil, err
    end

    return false, "unsupported dialogue action"
end

function QuestService.NotifyTalkToNPC(player, npcId)
    local store = getStore(player, false)
    if not store or type(npcId) ~= "string" then return 0 end

    local completed = 0
    for _, instance in pairs(store.byInstanceId) do
        if instance.state == "active" then
            local objective = QuestInstance.GetCurrentObjective(instance)
            if objective and objective.type == "TalkToNPC" then
                local handler = QuestInstance.GetHandler(objective.type)
                if handler and handler.EvaluateTalk and handler.EvaluateTalk(objective, npcId) then
                    if completeCurrentObjective(player, instance, "talk_to_npc") then
                        completed = completed + 1
                    end
                end
            end
        end
    end
    return completed
end

function QuestService.UpdatePlayer(player)
    local store = getStore(player, false)
    if not store or not player then return 0 end

    local changed = 0
    for _, instance in pairs(store.byInstanceId) do
        if instance.state == "active" then
            local objective = QuestInstance.GetCurrentObjective(instance)
            if objective and objective.type == "ReachArea" then
                local handler = QuestInstance.GetHandler(objective.type)
                if handler and handler.Evaluate and handler.Evaluate(player, objective) then
                    if completeCurrentObjective(player, instance, "reach_area") then
                        changed = changed + 1
                    end
                end
            end
        end
    end
    return changed
end

function QuestService.Tick()
    if not getOnlinePlayers then return 0 end
    local onlinePlayers = getOnlinePlayers()
    if not onlinePlayers then return 0 end

    local changed = 0
    for i = 0, onlinePlayers:size() - 1 do
        changed = changed + QuestService.UpdatePlayer(onlinePlayers:get(i))
    end
    return changed
end

function QuestService.ExportViews(player)
    local store = getStore(player, false)
    local result = {}
    if not store then return result end

    for _, instance in pairs(store.byInstanceId) do
        local view = QuestInstance.MakeView(instance)
        if view then result[#result + 1] = view end
    end
    table.sort(result, function(a, b)
        return tostring(a.instanceId) < tostring(b.instanceId)
    end)
    return result
end

LCCQF.QuestService = QuestService

return QuestService
