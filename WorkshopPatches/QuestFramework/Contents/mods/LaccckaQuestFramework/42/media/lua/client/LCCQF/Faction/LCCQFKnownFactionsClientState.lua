LCCQF = LCCQF or {}

local KnownFactionsClientState = LCCQF.KnownFactionsClientState or {}
local byFactionId = {}
local listeners = KnownFactionsClientState._listeners or {}
local revision = tonumber(KnownFactionsClientState._revision) or 0
local serverRevision = tonumber(KnownFactionsClientState._serverRevision) or 0
local synchronized = KnownFactionsClientState._synchronized == true

local function validView(view)
    return type(view) == "table"
        and type(view.factionId) == "string"
        and view.factionId ~= ""
        and type(view.displayNameKey) == "string"
        and view.displayNameKey ~= ""
end

local function notifyChanged(reason, view)
    revision = revision + 1
    KnownFactionsClientState._revision = revision
    for _, listener in ipairs(listeners) do
        local ok, err = pcall(listener, reason, view, revision)
        if not ok then
            print("[LCCQF][FACTION:CLIENT] state listener failed: " .. tostring(err))
        end
    end
end

function KnownFactionsClientState.AddListener(listener)
    if type(listener) ~= "function" then return false end
    for _, current in ipairs(listeners) do
        if current == listener then return true end
    end
    listeners[#listeners + 1] = listener
    KnownFactionsClientState._listeners = listeners
    return true
end

function KnownFactionsClientState.GetRevision()
    return revision
end

function KnownFactionsClientState.GetServerRevision()
    return serverRevision
end

function KnownFactionsClientState.IsSynchronized()
    return synchronized
end

function KnownFactionsClientState.BeginCharacterTransition(source)
    byFactionId = {}
    serverRevision = 0
    synchronized = false
    KnownFactionsClientState._serverRevision = 0
    KnownFactionsClientState._synchronized = false
    notifyChanged("character-transition:" .. tostring(source or "unknown"), nil)
end

function KnownFactionsClientState.Clear()
    byFactionId = {}
    serverRevision = 0
    synchronized = false
    KnownFactionsClientState._serverRevision = 0
    KnownFactionsClientState._synchronized = false
    notifyChanged("clear", nil)
end

function KnownFactionsClientState.Apply(view, incomingRevision)
    if not validView(view) then return false end
    byFactionId[view.factionId] = view
    if tonumber(incomingRevision) then
        serverRevision = math.max(serverRevision, math.floor(tonumber(incomingRevision)))
        KnownFactionsClientState._serverRevision = serverRevision
    end
    notifyChanged("upsert", view)
    return true
end

function KnownFactionsClientState.Replace(views, incomingRevision)
    byFactionId = {}
    local count = 0
    for _, view in ipairs(type(views) == "table" and views or {}) do
        if validView(view) then
            byFactionId[view.factionId] = view
            count = count + 1
        end
    end
    serverRevision = math.max(0, math.floor(tonumber(incomingRevision) or 0))
    synchronized = true
    KnownFactionsClientState._serverRevision = serverRevision
    KnownFactionsClientState._synchronized = true
    notifyChanged("replace", nil)
    return count
end

function KnownFactionsClientState.Get(factionId)
    if type(factionId) ~= "string" then return nil end
    return byFactionId[factionId]
end

function KnownFactionsClientState.ListAll()
    local result = {}
    for _, view in pairs(byFactionId) do result[#result + 1] = view end
    table.sort(result, function(a, b)
        return tostring(a.displayNameKey) < tostring(b.displayNameKey)
    end)
    return result
end

LCCQF.KnownFactionsClientState = KnownFactionsClientState
return KnownFactionsClientState
