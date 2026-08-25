LCCQF = LCCQF or {}

local QuestClientState = LCCQF.QuestClientState or {}
local byInstanceId = {}

local function validView(view)
    return type(view) == "table"
        and type(view.instanceId) == "string"
        and view.instanceId ~= ""
        and type(view.questId) == "string"
        and view.questId ~= ""
        and type(view.state) == "string"
end

function QuestClientState.Clear()
    byInstanceId = {}
end

function QuestClientState.Apply(view)
    if not validView(view) then return false end
    byInstanceId[view.instanceId] = view
    return true
end

function QuestClientState.Replace(views)
    byInstanceId = {}
    local count = 0
    for _, view in ipairs(type(views) == "table" and views or {}) do
        if QuestClientState.Apply(view) then count = count + 1 end
    end
    return count
end

function QuestClientState.Get(instanceId)
    if type(instanceId) ~= "string" then return nil end
    return byInstanceId[instanceId]
end

function QuestClientState.ListAll()
    local result = {}
    for _, view in pairs(byInstanceId) do
        result[#result + 1] = view
    end
    table.sort(result, function(a, b)
        return tostring(a.instanceId) < tostring(b.instanceId)
    end)
    return result
end

function QuestClientState.ListActive()
    local result = {}
    for _, view in ipairs(QuestClientState.ListAll()) do
        if view.state == "active" then result[#result + 1] = view end
    end
    return result
end

LCCQF.QuestClientState = QuestClientState

return QuestClientState
