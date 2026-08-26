LCCQF = LCCQF or {}

local QuestClientState = LCCQF.QuestClientState or {}
local byInstanceId = {}
local listeners = QuestClientState._listeners or {}
local revision = tonumber(QuestClientState._revision) or 0

local function validView(view)
    return type(view) == "table"
        and type(view.instanceId) == "string"
        and view.instanceId ~= ""
        and type(view.questId) == "string"
        and view.questId ~= ""
        and type(view.state) == "string"
end

local function notifyChanged(reason, view)
    revision = revision + 1
    QuestClientState._revision = revision

    for _, listener in ipairs(listeners) do
        local ok, err = pcall(listener, reason, view, revision)
        if not ok then
            print("[LCCQF][QUEST:CLIENT] state listener failed: " .. tostring(err))
        end
    end
end

function QuestClientState.AddListener(listener)
    if type(listener) ~= "function" then return false end
    for _, current in ipairs(listeners) do
        if current == listener then return true end
    end
    listeners[#listeners + 1] = listener
    QuestClientState._listeners = listeners
    return true
end

function QuestClientState.GetRevision()
    return revision
end

function QuestClientState.Clear()
    byInstanceId = {}
    notifyChanged("clear", nil)
end

function QuestClientState.Apply(view)
    if not validView(view) then return false end
    byInstanceId[view.instanceId] = view
    notifyChanged("upsert", view)
    return true
end

function QuestClientState.Replace(views)
    byInstanceId = {}
    local count = 0
    for _, view in ipairs(type(views) == "table" and views or {}) do
        if validView(view) then
            byInstanceId[view.instanceId] = view
            count = count + 1
        end
    end
    notifyChanged("replace", nil)
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
        if a.state ~= b.state then
            if a.state == "active" then return true end
            if b.state == "active" then return false end
        end
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
