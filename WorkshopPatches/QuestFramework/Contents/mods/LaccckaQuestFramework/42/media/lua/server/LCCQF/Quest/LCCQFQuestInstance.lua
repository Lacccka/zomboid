require "LCCQF/Quest/Objectives/LCCQFObjectiveReachArea"
require "LCCQF/Quest/Objectives/LCCQFObjectiveTalkToNPC"

LCCQF = LCCQF or {}

local QuestInstance = LCCQF.QuestInstance or {}

local handlers = {
    ReachArea = LCCQF.QuestObjectives.ReachArea,
    TalkToNPC = LCCQF.QuestObjectives.TalkToNPC,
}

local function createObjective(spec, context)
    local handler = handlers[spec.type]
    if not handler or not handler.Create then
        return nil, "unsupported objective type: " .. tostring(spec.type)
    end
    return handler.Create(spec, context)
end

function QuestInstance.Create(definition, ownerKey, context)
    if not definition or not ownerKey then return nil, "invalid quest instance context" end

    local now = getTimestampMs()
    local instance = {
        id = tostring(getRandomUUID()),
        questId = definition.questId,
        ownerKey = tostring(ownerKey),
        giverNpcId = definition.giverNpcId,
        titleKey = definition.titleKey,
        descriptionKey = definition.descriptionKey,
        state = "active",
        currentObjectiveIndex = 1,
        createdMs = now,
        updatedMs = now,
        completedMs = nil,
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

function QuestInstance.MakeView(instance)
    if not instance then return nil end

    local objectives = {}
    for index, objective in ipairs(instance.objectives or {}) do
        objectives[index] = {
            id = objective.id,
            type = objective.type,
            titleKey = objective.titleKey,
            state = objective.state,
            progress = objective.state == "completed" and 1 or 0,
            required = 1,
        }
    end

    local current = QuestInstance.GetCurrentObjective(instance)
    local marker = nil
    if current then
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
        objectives = objectives,
        marker = marker,
    }
end

function QuestInstance.GetHandler(objectiveType)
    return handlers[objectiveType]
end

LCCQF.QuestInstance = QuestInstance

return QuestInstance
