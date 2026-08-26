LCCQF = LCCQF or {}

local KnownPeopleClientState = LCCQF.KnownPeopleClientState or {}
local byNpcId = {}
local listeners = KnownPeopleClientState._listeners or {}
local revision = tonumber(KnownPeopleClientState._revision) or 0
local serverRevision = tonumber(KnownPeopleClientState._serverRevision) or 0
local synchronized = KnownPeopleClientState._synchronized == true

local function validView(view)
    return type(view) == "table"
        and type(view.npcId) == "string"
        and view.npcId ~= ""
        and type(view.displayNameKey) == "string"
        and view.displayNameKey ~= ""
end

local function notifyChanged(reason, view)
    revision = revision + 1
    KnownPeopleClientState._revision = revision
    for _, listener in ipairs(listeners) do
        local ok, err = pcall(listener, reason, view, revision)
        if not ok then
            print("[LCCQF][KNOWLEDGE:CLIENT] state listener failed: " .. tostring(err))
        end
    end
end

function KnownPeopleClientState.AddListener(listener)
    if type(listener) ~= "function" then return false end
    for _, current in ipairs(listeners) do
        if current == listener then return true end
    end
    listeners[#listeners + 1] = listener
    KnownPeopleClientState._listeners = listeners
    return true
end

function KnownPeopleClientState.GetRevision()
    return revision
end

function KnownPeopleClientState.GetServerRevision()
    return serverRevision
end

function KnownPeopleClientState.IsSynchronized()
    return synchronized
end

function KnownPeopleClientState.BeginCharacterTransition(source)
    byNpcId = {}
    serverRevision = 0
    synchronized = false
    KnownPeopleClientState._serverRevision = 0
    KnownPeopleClientState._synchronized = false
    notifyChanged("character-transition:" .. tostring(source or "unknown"), nil)
end

function KnownPeopleClientState.Clear()
    byNpcId = {}
    serverRevision = 0
    synchronized = false
    KnownPeopleClientState._serverRevision = 0
    KnownPeopleClientState._synchronized = false
    notifyChanged("clear", nil)
end

function KnownPeopleClientState.Apply(view, incomingRevision)
    if not validView(view) then return false end
    byNpcId[view.npcId] = view
    if tonumber(incomingRevision) then
        serverRevision = math.max(serverRevision, math.floor(tonumber(incomingRevision)))
        KnownPeopleClientState._serverRevision = serverRevision
    end
    notifyChanged("upsert", view)
    return true
end

function KnownPeopleClientState.Replace(views, incomingRevision)
    byNpcId = {}
    local count = 0
    for _, view in ipairs(type(views) == "table" and views or {}) do
        if validView(view) then
            byNpcId[view.npcId] = view
            count = count + 1
        end
    end
    serverRevision = math.max(0, math.floor(tonumber(incomingRevision) or 0))
    synchronized = true
    KnownPeopleClientState._serverRevision = serverRevision
    KnownPeopleClientState._synchronized = true
    notifyChanged("replace", nil)
    return count
end

function KnownPeopleClientState.Get(npcId)
    if type(npcId) ~= "string" then return nil end
    return byNpcId[npcId]
end

function KnownPeopleClientState.ListAll()
    local result = {}
    for _, view in pairs(byNpcId) do
        result[#result + 1] = view
    end
    table.sort(result, function(a, b)
        return tostring(a.displayNameKey) < tostring(b.displayNameKey)
    end)
    return result
end

LCCQF.KnownPeopleClientState = KnownPeopleClientState
return KnownPeopleClientState
