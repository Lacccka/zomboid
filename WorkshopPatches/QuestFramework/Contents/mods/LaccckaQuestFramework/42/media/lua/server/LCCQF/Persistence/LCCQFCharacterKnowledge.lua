require "LCCQF/LCCQFConstants"
require "LCCQF/Core/LCCQFNPCRegistry"
require "LCCQF/Persistence/LCCQFCharacterIdentity"
require "LCCQF/Persistence/LCCQFCharacterRelationships"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local NPCRegistry = LCCQF.NPCRegistry
local CharacterIdentity = LCCQF.CharacterIdentity
local CharacterRelationships = LCCQF.CharacterRelationships
local CharacterKnowledge = LCCQF.CharacterKnowledge or {}
local eventSink = nil

local function log(message)
    print(C.LOG_PREFIX .. "[KNOWLEDGE:SERVER] " .. tostring(message))
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

local function ensureKnowledge(record)
    if not record then return nil end
    local knowledge = record.knowledge
    if type(knowledge) ~= "table" then
        knowledge = {}
        record.knowledge = knowledge
    end
    if tonumber(knowledge.schemaVersion) == nil then
        knowledge.schemaVersion = C.KNOWLEDGE_PERSISTENCE_SCHEMA_VERSION
    end
    if tonumber(knowledge.revision) == nil then knowledge.revision = 0 end
    if type(knowledge.knownNPCs) ~= "table" then knowledge.knownNPCs = {} end
    return knowledge
end

local function getStore(player, create)
    local record, characterId = CharacterIdentity.GetRecord(player, create ~= false)
    if not record or not characterId then return nil, nil end
    return ensureKnowledge(record), characterId
end

local function factDefinition(definition, factId)
    if not definition or type(definition.knowledgeFacts) ~= "table" then return nil end
    for _, fact in ipairs(definition.knowledgeFacts) do
        if type(fact) == "table" and fact.id == factId then return fact end
    end
    return nil
end

local function sanitizePortrait(definition)
    local source = definition and definition.portrait
    if type(source) ~= "table" then return nil end
    return {
        provider = type(source.provider) == "string" and source.provider or nil,
        zoom = tonumber(source.zoom),
        yOffset = tonumber(source.yOffset),
        xOffset = tonumber(source.xOffset),
        direction = type(source.direction) == "string" and source.direction or nil,
    }
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

local function makeKnownPersonView(player, npcId, entry)
    local definition = NPCRegistry.Get(npcId)
    if not definition or type(entry) ~= "table" then return nil end

    local facts = {}
    if type(definition.knowledgeFacts) == "table" then
        for _, fact in ipairs(definition.knowledgeFacts) do
            local view = fact and makeFactView(definition, entry, fact.id) or nil
            if view then facts[#facts + 1] = view end
        end
    end

    return {
        npcId = npcId,
        displayNameKey = definition.displayNameKey,
        aliasKey = definition.aliasKey,
        summaryKey = definition.summaryKey,
        discoveredWorldHours = tonumber(entry.discoveredWorldHours) or 0,
        facts = facts,
        portrait = sanitizePortrait(definition),
        relationship = CharacterRelationships.ExportView(player, npcId),
    }
end

local function emitUpsert(player, npcId, entry)
    if not eventSink then return end
    local view = makeKnownPersonView(player, npcId, entry)
    if view then eventSink("upsert", player, view) end
end

local function touch(knowledge, characterId)
    knowledge.revision = math.max(0, math.floor(tonumber(knowledge.revision) or 0)) + 1
    CharacterIdentity.Touch(characterId)
end

function CharacterKnowledge.Initialize()
    local ok, err = CharacterIdentity.Initialize()
    if not ok then return false, err end
    local relationshipOk, relationshipErr = CharacterRelationships.Initialize()
    if not relationshipOk then return false, relationshipErr end
    log("initialized schemaVersion=" .. tostring(C.KNOWLEDGE_PERSISTENCE_SCHEMA_VERSION)
        .. " relationships=" .. tostring(C.RELATIONSHIP_PERSISTENCE_SCHEMA_VERSION))
    return true
end

function CharacterKnowledge.SetEventSink(sink)
    eventSink = type(sink) == "function" and sink or nil
end

function CharacterKnowledge.DiscoverNPC(player, npcId, source)
    if not player or not validId(npcId) then return false, "invalid discovery" end
    local definition = NPCRegistry.Get(npcId)
    if not definition then return false, "unknown npc" end

    local knowledge, characterId = getStore(player, true)
    if not knowledge or not characterId then return false, "character unavailable" end

    local entry = knowledge.knownNPCs[npcId]
    local created = type(entry) ~= "table"
    if created then
        entry = {
            npcId = npcId,
            discoveredWorldHours = worldHours(),
            source = tostring(source or "interaction"),
            facts = {},
        }
        knowledge.knownNPCs[npcId] = entry
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

    local relationshipOk, relationshipCreated = CharacterRelationships.EnsureNPC(
        player,
        npcId,
        "knowledge-discovery"
    )
    if not relationshipOk then
        log("relationship establishment failed npcId=" .. tostring(npcId))
    end

    if changed then
        touch(knowledge, characterId)
        emitUpsert(player, npcId, entry)
        log("npc discovered characterId=" .. tostring(characterId)
            .. " npcId=" .. tostring(npcId)
            .. " source=" .. tostring(source or "interaction")
            .. " relationshipCreated=" .. tostring(relationshipCreated == true))
    end
    return true, created
end

function CharacterKnowledge.UnlockFact(player, npcId, factId, source)
    if not validId(npcId) or not validId(factId) then return false, "invalid fact" end
    local definition = NPCRegistry.Get(npcId)
    if not factDefinition(definition, factId) then return false, "unknown fact" end

    local knowledge, characterId = getStore(player, false)
    if not knowledge or not characterId then return false, "character unavailable" end
    local entry = knowledge.knownNPCs[npcId]
    if type(entry) ~= "table" then return false, "npc not discovered" end
    if type(entry.facts) ~= "table" then entry.facts = {} end
    if entry.facts[factId] == true then return true, false end

    entry.facts[factId] = true
    touch(knowledge, characterId)
    emitUpsert(player, npcId, entry)
    log("fact unlocked characterId=" .. tostring(characterId)
        .. " npcId=" .. tostring(npcId)
        .. " factId=" .. tostring(factId)
        .. " source=" .. tostring(source or "unknown"))
    return true, true
end

function CharacterKnowledge.GetView(player, npcId)
    if not player or not validId(npcId) then return nil end
    local knowledge = getStore(player, false)
    if not knowledge then return nil end
    local entry = knowledge.knownNPCs[npcId]
    return type(entry) == "table" and makeKnownPersonView(player, npcId, entry) or nil
end

function CharacterKnowledge.GetRevision(player)
    local knowledge = getStore(player, false)
    if not knowledge then return 0 end
    return math.max(0, math.floor(tonumber(knowledge.revision) or 0))
end

function CharacterKnowledge.ExportViews(player)
    local knowledge = getStore(player, true)
    local result = {}
    if not knowledge then return result, 0 end

    for npcId, entry in pairs(knowledge.knownNPCs) do
        local view = makeKnownPersonView(player, npcId, entry)
        if view then result[#result + 1] = view end
    end
    table.sort(result, function(a, b)
        return tostring(a.npcId) < tostring(b.npcId)
    end)
    return result, math.max(0, math.floor(tonumber(knowledge.revision) or 0))
end

LCCQF.CharacterKnowledge = CharacterKnowledge
return CharacterKnowledge
