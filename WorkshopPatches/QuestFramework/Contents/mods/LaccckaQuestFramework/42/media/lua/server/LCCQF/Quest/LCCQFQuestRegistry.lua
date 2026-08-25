require "LCCQF/LCCQFConstants"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local QuestRegistry = LCCQF.QuestRegistry or {}
local definitions = {}

local function validIdentifier(value)
    return type(value) == "string"
        and value ~= ""
        and #value <= C.MAX_IDENTIFIER_LENGTH
end

local function validateObjective(objective, index)
    if type(objective) ~= "table" then
        return false, "objective " .. tostring(index) .. " must be a table"
    end
    if not validIdentifier(objective.id) then
        return false, "objective " .. tostring(index) .. " has invalid id"
    end
    if not validIdentifier(objective.type) then
        return false, "objective " .. tostring(index) .. " has invalid type"
    end
    if not validIdentifier(objective.titleKey) then
        return false, "objective " .. tostring(index) .. " has invalid titleKey"
    end
    return true
end

local function validateDefinition(definition)
    if type(definition) ~= "table" then return false, "quest definition must be a table" end
    if not validIdentifier(definition.questId) then return false, "invalid questId" end
    if not validIdentifier(definition.titleKey) then return false, "invalid titleKey" end
    if not validIdentifier(definition.descriptionKey) then return false, "invalid descriptionKey" end
    if not validIdentifier(definition.giverNpcId) then return false, "invalid giverNpcId" end
    if type(definition.objectives) ~= "table" or #definition.objectives == 0 then
        return false, "quest requires at least one objective"
    end

    local seen = {}
    for index, objective in ipairs(definition.objectives) do
        local ok, err = validateObjective(objective, index)
        if not ok then return false, err end
        if seen[objective.id] then return false, "duplicate objective id: " .. objective.id end
        seen[objective.id] = true
    end

    return true
end

function QuestRegistry.Register(definition)
    local ok, err = validateDefinition(definition)
    if not ok then return false, err end
    if definitions[definition.questId] then return false, "duplicate questId" end
    definitions[definition.questId] = definition
    return true
end

function QuestRegistry.Get(questId)
    if not validIdentifier(questId) then return nil end
    return definitions[questId]
end

function QuestRegistry.IsRegistered(questId)
    return QuestRegistry.Get(questId) ~= nil
end

function QuestRegistry.List()
    local result = {}
    for _, definition in pairs(definitions) do
        result[#result + 1] = definition
    end
    table.sort(result, function(a, b)
        return tostring(a.questId) < tostring(b.questId)
    end)
    return result
end

LCCQF.QuestRegistry = QuestRegistry

return QuestRegistry
