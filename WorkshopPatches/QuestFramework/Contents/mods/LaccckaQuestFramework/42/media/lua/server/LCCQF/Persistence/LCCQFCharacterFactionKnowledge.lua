require "LCCQF/LCCQFConstants"
require "LCCQF/Core/LCCQFFactionRegistry"
require "LCCQF/Persistence/LCCQFCharacterIdentity"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local FactionRegistry = LCCQF.FactionRegistry
local CharacterIdentity = LCCQF.CharacterIdentity
local CharacterFactionKnowledge = LCCQF.CharacterFactionKnowledge or {}
local eventSink = nil

local function log(message)
    print(C.LOG_PREFIX .. "[FACTION:KNOWLEDGE:SERVER] " .. tostring(message))
end

local function worldHours()
    if not getGameTime then return 0 end
    local gameTime = getGameTime()
    if not gameTime or not gameTime.getWorldAgeHours then return 0 end
    return tonumber(gameTime:getWorldAgeHours()) or 0
end

local function validId(value)
    return type(value) == "string" and value ~= "" and #value <= C.MAX_IDENTIFIER_LENGTH
end

local function ensureStore(record)
    if not record then return nil end
    local store = record.factionKnowledge
    if type(store) ~= "table" then
        store = {}
        record.factionKnowledge = store
    end
    if tonumber(store.schemaVersion) == nil then
        store.schemaVersion = C.FACTION_KNOWLEDGE_PERSISTENCE_SCHEMA_VERSION
    end
    if tonumber(store.revision) == nil then store.revision = 0 end
    if type(store.knownFactions) ~= "table" then store.knownFactions = {} end
    return store
end

local function getStore(player, create)
    local record, characterId = CharacterIdentity.GetRecord(player, create ~= false)
    if not record or not characterId then return nil, nil end
    return ensureStore(record), characterId
end

local function factDefinition(definition, factId)
    if not definition or type(definition.knowledgeFacts) ~= "table" then return nil end
    for _, fact in ipairs(definition.knowledgeFacts) do
        if type(fact) == "table" and fact.id == factId then return fact end
    end
    return nil
end

local function makeFactView(definition, entry, factId)
    if type(entry.facts) ~= "table" or entry.facts[factId] ~= true then return nil end
    local fact = factDefinition(definition, factId)
    if not fact then return nil end
    return {
        id = fact.id,
        titleKey = fact.titleKey,
        textKey = fact.textKey,
    }
end

local function makeView(factionId, entry)
    local definition = FactionRegistry.Get(factionId)
    if not definition or type(entry) ~= "table" then return nil end

    local facts = {}
    for _, fact in ipairs(type(definition.knowledgeFacts) == "table" and definition.knowledgeFacts or {}) do
        local view = fact and makeFactView(definition, entry, fact.id) or nil
        if view then facts[#facts + 1] = view end
    end

    return {
        factionId = factionId,
        displayNameKey = definition.displayNameKey,
        summaryKey = definition.summaryKey,
        discoveredWorldHours = tonumber(entry.discoveredWorldHours) or 0,
        facts = facts,
    }
end

local function touch(store, characterId)
    store.revision = math.max(0, math.floor(tonumber(store.revision) or 0)) + 1
    CharacterIdentity.Touch(characterId)
end

local function emitUpsert(player, store, factionId, entry)
    if not eventSink then return end
    local view = makeView(factionId, entry)
    if not view then return end
    view.factionKnowledgeRevision = math.max(0, math.floor(tonumber(store.revision) or 0))
    eventSink("upsert", player, view)
end

function CharacterFactionKnowledge.Initialize()
    local ok, err = CharacterIdentity.Initialize()
    if not ok then return false, err end
    log("initialized schemaVersion=" .. tostring(C.FACTION_KNOWLEDGE_PERSISTENCE_SCHEMA_VERSION))
    return true
end

function CharacterFactionKnowledge.SetEventSink(sink)
    eventSink = type(sink) == "function" and sink or nil
end

function CharacterFactionKnowledge.DiscoverFaction(player, factionId, source)
    if not player or not validId(factionId) then return false, "invalid faction discovery" end
    local definition = FactionRegistry.Get(factionId)
    if not definition then return false, "unknown faction" end

    local store, characterId = getStore(player, true)
    if not store or not characterId then return false, "character unavailable" end

    local entry = store.knownFactions[factionId]
    local created = type(entry) ~= "table"
    if created then
        entry = {
            factionId = factionId,
            discoveredWorldHours = worldHours(),
            source = tostring(source or "discovery"),
            facts = {},
        }
        store.knownFactions[factionId] = entry
    elseif type(entry.facts) ~= "table" then
        entry.facts = {}
    end

    local changed = created
    for _, factId in ipairs(type(definition.initialKnowledgeFacts) == "table" and definition.initialKnowledgeFacts or {}) do
        if validId(factId) and factDefinition(definition, factId) and entry.facts[factId] ~= true then
            entry.facts[factId] = true
            changed = true
        end
    end

    if changed then
        touch(store, characterId)
        emitUpsert(player, store, factionId, entry)
        log("faction discovered characterId=" .. tostring(characterId)
            .. " factionId=" .. tostring(factionId)
            .. " source=" .. tostring(source or "discovery"))
    end
    return true, created
end

function CharacterFactionKnowledge.UnlockFact(player, factionId, factId, source)
    if not validId(factionId) or not validId(factId) then return false, "invalid faction fact" end
    local definition = FactionRegistry.Get(factionId)
    if not factDefinition(definition, factId) then return false, "unknown faction fact" end

    local store, characterId = getStore(player, false)
    if not store or not characterId then return false, "character unavailable" end
    local entry = store.knownFactions[factionId]
    if type(entry) ~= "table" then return false, "faction not discovered" end
    if type(entry.facts) ~= "table" then entry.facts = {} end
    if entry.facts[factId] == true then return true, false end

    entry.facts[factId] = true
    touch(store, characterId)
    emitUpsert(player, store, factionId, entry)
    log("faction fact unlocked characterId=" .. tostring(characterId)
        .. " factionId=" .. tostring(factionId)
        .. " factId=" .. tostring(factId)
        .. " source=" .. tostring(source or "unknown"))
    return true, true
end

function CharacterFactionKnowledge.IsKnown(player, factionId)
    if not validId(factionId) then return false end
    local store = getStore(player, false)
    return store ~= nil and type(store.knownFactions[factionId]) == "table"
end

function CharacterFactionKnowledge.GetView(player, factionId)
    if not validId(factionId) then return nil end
    local store = getStore(player, false)
    if not store then return nil end
    local entry = store.knownFactions[factionId]
    return type(entry) == "table" and makeView(factionId, entry) or nil
end

function CharacterFactionKnowledge.ExportViews(player)
    local store = getStore(player, true)
    local result = {}
    if not store then return result, 0 end

    for factionId, entry in pairs(store.knownFactions) do
        local view = makeView(factionId, entry)
        if view then result[#result + 1] = view end
    end
    table.sort(result, function(a, b)
        return tostring(a.factionId) < tostring(b.factionId)
    end)
    return result, math.max(0, math.floor(tonumber(store.revision) or 0))
end

LCCQF.CharacterFactionKnowledge = CharacterFactionKnowledge
return CharacterFactionKnowledge
