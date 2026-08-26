require "LCCQF/LCCQFConstants"
require "LCCQF/Quest/LCCQFQuestRegistry"
require "LCCQF/Quest/LCCQFQuestInstance"
require "LCCQF/Persistence/LCCQFQuestPersistence"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local QuestRegistry = LCCQF.QuestRegistry
local QuestInstance = LCCQF.QuestInstance
local QuestPersistence = LCCQF.QuestPersistence
local QuestService = LCCQF.QuestService or {}
local eventSink = nil

local function log(message)
    print(C.LOG_PREFIX .. "[QUEST:SERVER] " .. tostring(message))
end

local function getStore(player, create)
    return QuestPersistence.GetQuestStore(player, create == true)
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

local function touchProgress(player, instance, objective, source)
    instance.updatedMs = getTimestampMs()
    QuestPersistence.Touch(instance.ownerCharacterId)
    emitUpsert(player, instance)
    log("objective progress player=" .. tostring(player:getUsername())
        .. " characterId=" .. tostring(instance.ownerCharacterId)
        .. " questId=" .. tostring(instance.questId)
        .. " instanceId=" .. tostring(instance.id)
        .. " objectiveId=" .. tostring(objective and objective.id)
        .. " source=" .. tostring(source))
end

local function completeCurrentObjective(player, instance, reason)
    local objective = QuestInstance.GetCurrentObjective(instance)
    if not objective then return false end

    local completed, questCompleted = QuestInstance.CompleteCurrentObjective(instance, reason)
    if not completed then return false end

    QuestPersistence.Touch(instance.ownerCharacterId)
    emitEvent(player, instance, "IGUI_LCCQF_QuestEvent_ObjectiveComplete", objective)
    emitUpsert(player, instance)

    log("objective completed player=" .. tostring(player:getUsername())
        .. " characterId=" .. tostring(instance.ownerCharacterId)
        .. " questId=" .. tostring(instance.questId)
        .. " instanceId=" .. tostring(instance.id)
        .. " objectiveId=" .. tostring(objective.id)
        .. " reason=" .. tostring(reason))

    if questCompleted then
        emitEvent(player, instance, "IGUI_LCCQF_QuestEvent_Completed", objective)
        log("quest completed player=" .. tostring(player:getUsername())
            .. " characterId=" .. tostring(instance.ownerCharacterId)
            .. " questId=" .. tostring(instance.questId)
            .. " instanceId=" .. tostring(instance.id))
    end
    return true
end

local function applyHandlerResult(player, instance, objective, complete, changed, reason, source)
    if complete == true then
        return completeCurrentObjective(player, instance, reason or source or "objective") and 1 or 0
    end
    if changed == true then
        touchProgress(player, instance, objective, source)
    end
    return 0
end

function QuestService.Initialize()
    return QuestPersistence.Initialize()
end

function QuestService.SetEventSink(sink)
    eventSink = type(sink) == "function" and sink or nil
end

function QuestService.GetCharacterId(player)
    return QuestPersistence.GetCharacterId(player)
end

function QuestService.GetPlayerKey(player)
    return QuestService.GetCharacterId(player)
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

    if condition.kind == "all" and type(condition.conditions) == "table" then
        for _, nested in ipairs(condition.conditions) do
            if not QuestService.EvaluateCondition(player, nested) then return false end
        end
        return true
    end

    return false
end

function QuestService.Accept(player, questId, context)
    local definition = QuestRegistry.Get(questId)
    if not definition then return nil, "unknown quest" end
    if not context or tostring(context.giverNpcId or "") ~= tostring(definition.giverNpcId) then
        return nil, "invalid quest giver"
    end

    local store, characterId = getStore(player, true)
    if not store or not characterId then return nil, "character unavailable" end

    local previous = QuestService.GetInstanceForQuest(player, questId)
    if previous then
        if previous.state == "active" then return nil, "quest already active" end
        if definition.repeatable ~= true then return nil, "quest already completed" end
    end

    local instance, err = QuestInstance.Create(definition, characterId, context)
    if not instance then return nil, err end

    store.byInstanceId[instance.id] = instance
    store.byQuestId[questId] = instance.id
    QuestPersistence.Touch(characterId)

    emitUpsert(player, instance)
    emitEvent(player, instance, "IGUI_LCCQF_QuestEvent_Accepted", QuestInstance.GetCurrentObjective(instance))

    log("quest accepted player=" .. tostring(player:getUsername())
        .. " characterId=" .. tostring(characterId)
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
            local handler = objective and QuestInstance.GetHandler(objective.type) or nil
            if handler and handler.EvaluateTalk then
                local complete, changed, reason = handler.EvaluateTalk(player, objective, npcId)
                completed = completed + applyHandlerResult(
                    player, instance, objective, complete, changed, reason, "talk"
                )
            end
        end
    end
    return completed
end

function QuestService.NotifyZombieDead(zombie)
    if not zombie or not zombie.getAttackedBy then return 0 end
    local player = zombie:getAttackedBy()
    if not player or not instanceof or not instanceof(player, "IsoPlayer") then return 0 end

    local store = getStore(player, false)
    if not store then return 0 end

    local completed = 0
    for _, instance in pairs(store.byInstanceId) do
        if instance.state == "active" then
            local objective = QuestInstance.GetCurrentObjective(instance)
            local handler = objective and QuestInstance.GetHandler(objective.type) or nil
            if handler and handler.EvaluateZombieDeath then
                local complete, changed, reason = handler.EvaluateZombieDeath(player, objective, zombie)
                completed = completed + applyHandlerResult(
                    player, instance, objective, complete, changed, reason, "zombie-death"
                )
            end
        end
    end
    return completed
end

function QuestService.UpdatePlayer(player)
    if not player then return 0 end
    if player.isDead and player:isDead() then
        QuestService.OnPlayerDeath(player)
        return 0
    end

    local store = getStore(player, false)
    if not store then return 0 end

    local completed = 0
    for _, instance in pairs(store.byInstanceId) do
        if instance.state == "active" then
            local objective = QuestInstance.GetCurrentObjective(instance)
            local handler = objective and QuestInstance.GetHandler(objective.type) or nil
            if handler and handler.EvaluateTick then
                local complete, changed, reason = handler.EvaluateTick(player, objective)
                completed = completed + applyHandlerResult(
                    player, instance, objective, complete, changed, reason, "tick"
                )
            end
        end
    end
    return completed
end

function QuestService.OnPlayerDeath(player)
    local changed, characterId = QuestPersistence.RetireCharacter(player, "death")
    if changed then
        log("character retired player=" .. tostring(player and player:getUsername())
            .. " characterId=" .. tostring(characterId))
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
    local store = getStore(player, true)
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
