require "LCCQF/LCCQFConstants"
require "LCCQF/Core/LCCQFNPCRegistry"
require "LCCQF/Persistence/LCCQFCharacterIdentity"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local NPCRegistry = LCCQF.NPCRegistry
local CharacterIdentity = LCCQF.CharacterIdentity
local CharacterRelationships = LCCQF.CharacterRelationships or {}
local eventSink = nil

local MIN_SCORE = -100
local MAX_SCORE = 100
local MIN_HOSTILITY = 0
local MAX_HOSTILITY = 100
local PUBLIC_STATS = {
    trust = true,
    reputation = true,
    hostility = true,
}

local function log(message)
    print(C.LOG_PREFIX .. "[RELATIONSHIP:SERVER] " .. tostring(message))
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

local function validToken(value)
    return value == nil or (type(value) == "string" and value ~= "" and #value <= C.MAX_IDENTIFIER_LENGTH * 2)
end

local function clamp(value, minimum, maximum)
    local number = tonumber(value) or 0
    if number < minimum then return minimum end
    if number > maximum then return maximum end
    return number
end

local function ensureStore(record)
    if not record then return nil end
    local relationships = record.relationships
    if type(relationships) ~= "table" then
        relationships = {}
        record.relationships = relationships
    end
    if tonumber(relationships.schemaVersion) == nil then
        relationships.schemaVersion = C.RELATIONSHIP_PERSISTENCE_SCHEMA_VERSION
    end
    if tonumber(relationships.revision) == nil then relationships.revision = 0 end
    if type(relationships.byNPCId) ~= "table" then relationships.byNPCId = {} end
    return relationships
end

local function getStore(player, create)
    local record, characterId = CharacterIdentity.GetRecord(player, create ~= false)
    if not record or not characterId then return nil, nil end
    return ensureStore(record), characterId
end

local function normalizeEntry(entry, npcId)
    entry.npcId = npcId
    entry.trust = clamp(entry.trust, MIN_SCORE, MAX_SCORE)
    entry.reputation = clamp(entry.reputation, MIN_SCORE, MAX_SCORE)
    entry.hostility = clamp(entry.hostility, MIN_HOSTILITY, MAX_HOSTILITY)
    if type(entry.flags) ~= "table" then entry.flags = {} end
    if type(entry.appliedTokens) ~= "table" then entry.appliedTokens = {} end
    return entry
end

local function ensureEntry(store, npcId)
    local entry = store.byNPCId[npcId]
    local created = type(entry) ~= "table"
    if created then
        entry = {
            npcId = npcId,
            trust = 0,
            reputation = 0,
            hostility = 0,
            establishedWorldHours = worldHours(),
            flags = {},
            appliedTokens = {},
        }
        store.byNPCId[npcId] = entry
    end
    return normalizeEntry(entry, npcId), created
end

local function getEntry(player, npcId)
    if not validId(npcId) then return nil end
    local store = getStore(player, false)
    if not store then return nil end
    local entry = store.byNPCId[npcId]
    return type(entry) == "table" and normalizeEntry(entry, npcId) or nil
end

local function tierFor(entry)
    local trust = tonumber(entry and entry.trust) or 0
    local reputation = tonumber(entry and entry.reputation) or 0
    local hostility = tonumber(entry and entry.hostility) or 0

    if hostility >= 70 or reputation <= -70 then return "hostile" end
    if hostility >= 25 or reputation <= -25 or trust <= -25 then return "wary" end
    if trust >= 70 and reputation >= 50 and hostility <= 10 then return "trusted" end
    if trust >= 20 or reputation >= 25 then return "friendly" end
    return "neutral"
end

local function makeView(entry)
    if type(entry) ~= "table" then return nil end
    return {
        trust = clamp(entry.trust, MIN_SCORE, MAX_SCORE),
        reputation = clamp(entry.reputation, MIN_SCORE, MAX_SCORE),
        hostility = clamp(entry.hostility, MIN_HOSTILITY, MAX_HOSTILITY),
        tier = tierFor(entry),
    }
end

local function touch(store, characterId)
    store.revision = math.max(0, math.floor(tonumber(store.revision) or 0)) + 1
    CharacterIdentity.Touch(characterId)
end

local function emitUpsert(player, store, npcId, entry)
    if not eventSink then return end
    eventSink("upsert", player, {
        npcId = npcId,
        relationship = makeView(entry),
        relationshipRevision = math.max(0, math.floor(tonumber(store.revision) or 0)),
    })
end

function CharacterRelationships.Initialize()
    local ok, err = CharacterIdentity.Initialize()
    if not ok then return false, err end
    log("initialized schemaVersion=" .. tostring(C.RELATIONSHIP_PERSISTENCE_SCHEMA_VERSION))
    return true
end

function CharacterRelationships.SetEventSink(sink)
    eventSink = type(sink) == "function" and sink or nil
end

function CharacterRelationships.EnsureNPC(player, npcId, source)
    if not player or not validId(npcId) or not NPCRegistry.Get(npcId) then
        return false, "invalid relationship npc"
    end

    local store, characterId = getStore(player, true)
    if not store or not characterId then return false, "character unavailable" end

    local entry, created = ensureEntry(store, npcId)
    if created then
        entry.lastSource = tostring(source or "discovery")
        entry.updatedWorldHours = worldHours()
        touch(store, characterId)
        log("relationship established characterId=" .. tostring(characterId)
            .. " npcId=" .. tostring(npcId)
            .. " source=" .. tostring(source or "discovery"))
    end
    return true, created, makeView(entry)
end

function CharacterRelationships.Adjust(player, npcId, delta, source, token)
    if not player or not validId(npcId) or not NPCRegistry.Get(npcId) then
        return false, "invalid relationship npc"
    end
    if type(delta) ~= "table" then return false, "invalid relationship delta" end
    if not validToken(token) then return false, "invalid relationship token" end

    local store, characterId = getStore(player, true)
    if not store or not characterId then return false, "character unavailable" end

    local entry = ensureEntry(store, npcId)
    if token and entry.appliedTokens[token] == true then
        return true, false, makeView(entry)
    end

    local oldTrust = entry.trust
    local oldReputation = entry.reputation
    local oldHostility = entry.hostility

    entry.trust = clamp(oldTrust + (tonumber(delta.trust) or 0), MIN_SCORE, MAX_SCORE)
    entry.reputation = clamp(oldReputation + (tonumber(delta.reputation) or 0), MIN_SCORE, MAX_SCORE)
    entry.hostility = clamp(oldHostility + (tonumber(delta.hostility) or 0), MIN_HOSTILITY, MAX_HOSTILITY)

    local changed = entry.trust ~= oldTrust
        or entry.reputation ~= oldReputation
        or entry.hostility ~= oldHostility
    local tokenApplied = token ~= nil
    if tokenApplied then entry.appliedTokens[token] = true end

    if changed or tokenApplied then
        entry.lastSource = tostring(source or "unknown")
        entry.updatedWorldHours = worldHours()
        touch(store, characterId)
        emitUpsert(player, store, npcId, entry)
        log("relationship adjusted characterId=" .. tostring(characterId)
            .. " npcId=" .. tostring(npcId)
            .. " trust=" .. tostring(entry.trust)
            .. " reputation=" .. tostring(entry.reputation)
            .. " hostility=" .. tostring(entry.hostility)
            .. " tier=" .. tostring(tierFor(entry))
            .. " source=" .. tostring(source or "unknown")
            .. " token=" .. tostring(token or "none"))
    end

    return true, changed, makeView(entry)
end

function CharacterRelationships.SetFlag(player, npcId, flagId, value, source)
    if not player or not validId(npcId) or not NPCRegistry.Get(npcId) or not validId(flagId) then
        return false, "invalid relationship flag"
    end
    if value ~= true and value ~= false then return false, "relationship flag must be boolean" end

    local store, characterId = getStore(player, true)
    if not store or not characterId then return false, "character unavailable" end

    local entry = ensureEntry(store, npcId)
    local oldValue = entry.flags[flagId] == true
    if oldValue == value then return true, false, makeView(entry) end

    entry.flags[flagId] = value and true or nil
    entry.lastSource = tostring(source or "relationship-flag")
    entry.updatedWorldHours = worldHours()
    touch(store, characterId)
    emitUpsert(player, store, npcId, entry)
    log("relationship flag changed characterId=" .. tostring(characterId)
        .. " npcId=" .. tostring(npcId)
        .. " flagId=" .. tostring(flagId)
        .. " value=" .. tostring(value)
        .. " source=" .. tostring(source or "relationship-flag"))
    return true, true, makeView(entry)
end

function CharacterRelationships.GetStat(player, npcId, stat)
    if type(stat) ~= "string" or not PUBLIC_STATS[stat] then return nil end
    local entry = getEntry(player, npcId)
    return entry and tonumber(entry[stat]) or nil
end

function CharacterRelationships.GetTier(player, npcId)
    local entry = getEntry(player, npcId)
    return entry and tierFor(entry) or nil
end

function CharacterRelationships.GetFlag(player, npcId, flagId)
    if not validId(flagId) then return nil end
    local entry = getEntry(player, npcId)
    if not entry then return nil end
    return entry.flags[flagId] == true
end

function CharacterRelationships.ExportView(player, npcId)
    local entry = getEntry(player, npcId)
    return entry and makeView(entry) or nil
end

function CharacterRelationships.GetRevision(player)
    local store = getStore(player, false)
    if not store then return 0 end
    return math.max(0, math.floor(tonumber(store.revision) or 0))
end

LCCQF.CharacterRelationships = CharacterRelationships
return CharacterRelationships
