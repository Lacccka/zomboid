require "LCCQF/Quest/Objectives/LCCQFObjectiveReachArea"
require "LCCQF/Quest/Objectives/LCCQFObjectiveTalkToNPC"
require "LCCQF/Quest/Objectives/LCCQFObjectiveFetch"
require "LCCQF/Quest/Objectives/LCCQFObjectiveDeliver"
require "LCCQF/Quest/Objectives/LCCQFObjectiveKill"
require "LCCQF/Quest/Objectives/LCCQFObjectiveClearArea"
require "LCCQF/Quest/Objectives/LCCQFObjectiveSettlementSupply"

LCCQF = LCCQF or {}

local QuestInstance = LCCQF.QuestInstance or {}

local handlers = {
    ReachArea = LCCQF.QuestObjectives.ReachArea,
    TalkToNPC = LCCQF.QuestObjectives.TalkToNPC,
    Fetch = LCCQF.QuestObjectives.Fetch,
    Deliver = LCCQF.QuestObjectives.Deliver,
    Kill = LCCQF.QuestObjectives.Kill,
    ClearArea = LCCQF.QuestObjectives.ClearArea,
    SettlementSupply = LCCQF.QuestObjectives.SettlementSupply,
}

local function createObjective(spec, context)
    local handler = handlers[spec.type]
    if not handler or not handler.Create then
        return nil, "unsupported objective type: " .. tostring(spec.type)
    end
    return handler.Create(spec, context)
end

local function isValidInstanceState(state)
    return state == "active" or state == "completed" or state == "failed"
end

local function validatePersistedObjective(objective, spec)
    if type(objective) ~= "table" or type(spec) ~= "table" then return false end
    if tostring(objective.id or "") ~= tostring(spec.id or "") then return false end
    if tostring(objective.type or "") ~= tostring(spec.type or "") then return false end

    local handler = handlers[spec.type]
    if not handler then return false end
    if handler.ValidatePersisted and not handler.ValidatePersisted(objective, spec) then
        return false
    end
    return true
end

function QuestInstance.Create(definition, ownerCharacterId, context)
    if not definition or not ownerCharacterId then return nil, "invalid quest instance context" end

    local now = getTimestampMs()
    local instance = {
        schemaVersion = 1,
        id = tostring(getRandomUUID()),
        questId = definition.questId,
        ownerCharacterId = tostring(ownerCharacterId),
        giverNpcId = definition.giverNpcId,
        titleKey = definition.titleKey,
        descriptionKey = definition.descriptionKey,
        state = "active",
        currentObjectiveIndex = 1,
        createdMs = now,
        updatedMs = now,
        completedMs = nil,
        failedMs = nil,
        failureReason = nil,
        objectives = {},
    }

    for index, spec in ipairs(definition.objectives or {}) do
        local objective, err = createObjective(spec, context)
        if not objective then return nil, err end
        objective.state = index == 1 and "active" or "pending"
        instance.objectives[index] = objective
    end

    if #instance.objectives == 0 then return nil, "quest has no objectives" end
    return instance
end

function QuestInstance.Restore(raw, definition, ownerCharacterId)
    if type(raw) ~= "table" or not definition or not ownerCharacterId then return nil end
    if type(raw.id) ~= "string" or raw.id == "" then return nil end
    if tostring(raw.questId or "") ~= tostring(definition.questId or "") then return nil end
    if not isValidInstanceState(raw.state) then return nil end
    if type(raw.objectives) ~= "table" then return nil end

    local objectiveCount = #(definition.objectives or {})
    if objectiveCount == 0 then return nil end

    local currentIndex = math.floor(tonumber(raw.currentObjectiveIndex) or 1)
    if currentIndex < 1 or currentIndex > objectiveCount then return nil end

    local objectives = {}
    for index, spec in ipairs(definition.objectives or {}) do
        local objective = raw.objectives[index]
        if not validatePersistedObjective(objective, spec) then return nil end

        objective.id = spec.id
        objective.type = spec.type
        objective.titleKey = spec.titleKey
        if raw.state == "completed" or index < currentIndex then
            objective.state = "completed"
        elseif index == currentIndex then
            objective.state = raw.state == "failed" and "failed" or "active"
        else
            objective.state = "pending"
        end
        objectives[index] = objective
    end

    raw.schemaVersion = 1
    raw.ownerKey = nil
    raw.ownerCharacterId = tostring(ownerCharacterId)
    raw.giverNpcId = definition.giverNpcId
    raw.titleKey = definition.titleKey
    raw.descriptionKey = definition.descriptionKey
    raw.currentObjectiveIndex = currentIndex
    raw.objectives = objectives
    if raw.state ~= "completed" then raw.completedMs = nil end
    if raw.state ~= "failed" then
        raw.failedMs = nil
        raw.failureReason = nil
    end
    return raw
end

function QuestInstance.GetCurrentObjective(instance)
    if not instance or instance.state ~= "active" then return nil end
    return instance.objectives[instance.currentObjectiveIndex]
end

function QuestInstance.CompleteCurrentObjective(instance, reason)
    local objective = QuestInstance.GetCurrentObjective(instance)
    if not objective then return false, false end

    local now = getTimestampMs()
    objective.state = "completed"
    objective.completedMs = now
    objective.completionReason = reason or "completed"
    instance.updatedMs = now

    local nextIndex = instance.currentObjectiveIndex + 1
    local nextObjective = instance.objectives[nextIndex]
    if nextObjective then
        instance.currentObjectiveIndex = nextIndex
        nextObjective.state = "active"
        nextObjective.activatedMs = now
        return true, false
    end

    instance.state = "completed"
    instance.completedMs = now
    return true, true
end

function QuestInstance.Fail(instance, reason)
    local objective = QuestInstance.GetCurrentObjective(instance)
    if not objective then return false end

    local now = getTimestampMs()
    local failureReason = tostring(reason or "failed")
    objective.state = "failed"
    objective.failedMs = now
    objective.failureReason = failureReason
    instance.state = "failed"
    instance.failedMs = now
    instance.failureReason = failureReason
    instance.updatedMs = now
    return true
end

local function makeObjectiveView(objective)
    local handler = objective and handlers[objective.type] or nil
    local progress, required
    if handler and handler.MakeProgressView then
        progress, required = handler.MakeProgressView(objective)
    end

    if tonumber(progress) == nil then
        progress = objective and objective.state == "completed" and 1 or 0
    end
    if tonumber(required) == nil then required = 1 end

    return {
        id = objective.id,
        type = objective.type,
        titleKey = objective.titleKey,
        state = objective.state,
        progress = tonumber(progress) or 0,
        required = math.max(1, tonumber(required) or 1),
    }
end

function QuestInstance.MakeView(instance)
    if not instance then return nil end

    local objectives = {}
    for index, objective in ipairs(instance.objectives or {}) do
        objectives[index] = makeObjectiveView(objective)
    end

    local current = instance.objectives and instance.objectives[instance.currentObjectiveIndex] or nil
    local marker = nil
    if instance.state == "active" and current then
        local handler = handlers[current.type]
        if handler and handler.MakeMarkerView then
            marker = handler.MakeMarkerView(current)
            if marker then
                marker.markerId = tostring(instance.id) .. ":" .. tostring(current.id)
            end
        end
    end

    return {
        instanceId = instance.id,
        questId = instance.questId,
        giverNpcId = instance.giverNpcId,
        titleKey = instance.titleKey,
        descriptionKey = instance.descriptionKey,
        state = instance.state,
        currentObjectiveId = current and current.id or nil,
        failureReason = instance.state == "failed" and instance.failureReason or nil,
        objectives = objectives,
        marker = marker,
    }
end

function QuestInstance.GetHandler(objectiveType)
    return handlers[objectiveType]
end

LCCQF.QuestInstance = QuestInstance

return QuestInstance
