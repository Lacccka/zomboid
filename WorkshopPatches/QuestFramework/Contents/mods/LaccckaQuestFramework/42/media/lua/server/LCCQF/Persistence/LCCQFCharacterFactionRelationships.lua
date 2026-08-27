require "LCCQF/LCCQFConstants"
require "LCCQF/Core/LCCQFFactionRegistry"
require "LCCQF/Persistence/LCCQFCharacterIdentity"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local FactionRegistry = LCCQF.FactionRegistry
local CharacterIdentity = LCCQF.CharacterIdentity
local CharacterFactionRelationships = LCCQF.CharacterFactionRelationships or {}
local eventSink = nil

local MIN_REPUTATION = -100
local MAX_REPUTATION = 100

local function log(message)
    print(C.LOG_PREFIX .. "[FACTION:RELATIONSHIP:SERVER] " .. tostring(message))
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
    local store = record.factionRelationships
    if type(store) ~= "table" then
        store = {}
        record.factionRelationships = store
    end
    if tonumber(store.schemaVersion) == nil then
        store.schemaVersion = C.FACTION_RELATIONSHIP_PERSISTENCE_SCHEMA_VERSION
    end
    if tonumber(store.revision) == nil then store.revision = 0 end
    if type(store.byFactionId) ~= "table" then store.byFactionId = {} end
    return store
end

local function getStore(player, create)
    local record, characterId = CharacterIdentity.GetRecord(player, create ~= false)
    if not record or not characterId then return nil, nil end
    return ensureStore(record), characterId
end

local function normalizeEntry(entry, factionId)
    entry.factionId = factionId
    entry.reputation = clamp(entry.reputation, MIN_REPUTATION, MAX_REPUTATION)
    entry.member = entry.member == true
    if not entry.member then entry.rankId = nil end
    if type(entry.appliedTokens) ~= "table" then entry.appliedTokens = {} end
    return entry
end

local function ensureEntry(store, factionId)
    local entry = store.byFactionId[factionId]
    local created = type(entry) ~= "table"
    if created then
        entry = {
            factionId = factionId,
            reputation = 0,
            member = false,
            rankId = nil,
            establishedWorldHours = worldHours(),
            appliedTokens = {},
        }
        store.byFactionId[factionId] = entry
    end
    return normalizeEntry(entry, factionId), created
end

local function getEntry(player, factionId)
    if not validId(factionId) then return nil end
    local store = getStore(player, false)
    if not store then return nil end
    local entry = store.byFactionId[factionId]
    return type(entry) == "table" and normalizeEntry(entry, factionId) or nil
end

local function tierFor(entry)
    local reputation = tonumber(entry and entry.reputation) or 0
    if reputation <= -60 then return "hostile" end
    if reputation <= -20 then return "unfriendly" end
    if reputation >= 60 then return "allied" end
    if reputation >= 20 then return "friendly" end
    return "neutral"
end

local function rankView(factionId, entry)
    if not entry or not entry.member or not validId(entry.rankId) then return nil end
    local rank = FactionRegistry.GetRank(factionId, entry.rankId)
    if not rank then return nil end
    return {
        rankId = rank.rankId,
        displayNameKey = rank.displayNameKey,
    }
end

local function makeView(factionId, entry)
    if type(entry) ~= "table" then return nil end
    return {
        reputation = clamp(entry.reputation, MIN_REPUTATION, MAX_REPUTATION),
        tier = tierFor(entry),
        member = entry.member == true,
        rank = rankView(factionId, entry),
    }
end

local function touch(store, characterId)
    store.revision = math.max(0, math.floor(tonumber(store.revision) or 0)) + 1
    CharacterIdentity.Touch(characterId)
end

local function emitUpsert(player, store, factionId, entry)
    if not eventSink then return end
    eventSink("upsert", player, {
        factionId = factionId,
        factionRelationship = makeView(factionId, entry),
        factionRelationshipRevision = math.max(0, math.floor(tonumber(store.revision) or 0)),
    })
end

function CharacterFactionRelationships.Initialize()
    local ok, err = CharacterIdentity.Initialize()
    if not ok then return false, err end
    log("initialized schemaVersion=" .. tostring(C.FACTION_RELATIONSHIP_PERSISTENCE_SCHEMA_VERSION))
    return true
end

function CharacterFactionRelationships.SetEventSink(sink)
    eventSink = type(sink) == "function" and sink or nil
end

function CharacterFactionRelationships.EnsureFaction(player, factionId, source)
    if not player or not validId(factionId) or not FactionRegistry.Get(factionId) then
        return false, "invalid faction relationship"
    end

    local store, characterId = getStore(player, true)
    if not store or not characterId then return false, "character unavailable" end
    local entry, created = ensureEntry(store, factionId)

    if created then
        entry.lastSource = tostring(source or "faction-state")
        entry.updatedWorldHours = worldHours()
        touch(store, characterId)
        log("faction relationship established characterId=" .. tostring(characterId)
            .. " factionId=" .. tostring(factionId)
            .. " source=" .. tostring(source or "faction-state"))
    end
    return true, created, makeView(factionId, entry)
end

function CharacterFactionRelationships.AdjustReputation(player, factionId, delta, source, token)
    if not player or not validId(factionId) or not FactionRegistry.Get(factionId) then
        return false, "invalid faction relationship"
    end
    if tonumber(delta) == nil then return false, "invalid faction reputation delta" end
    if not validToken(token) then return false, "invalid faction relationship token" end

    local store, characterId = getStore(player, true)
    if not store or not characterId then return false, "character unavailable" end
    local entry = ensureEntry(store, factionId)

    if token and entry.appliedTokens[token] == true then
        return true, false, makeView(factionId, entry)
    end

    local oldReputation = entry.reputation
    entry.reputation = clamp(oldReputation + tonumber(delta), MIN_REPUTATION, MAX_REPUTATION)
    local changed = entry.reputation ~= oldReputation
    local tokenApplied = token ~= nil
    if tokenApplied then entry.appliedTokens[token] = true end

    if changed or tokenApplied then
        entry.lastSource = tostring(source or "faction-reputation")
        entry.updatedWorldHours = worldHours()
        touch(store, characterId)
        emitUpsert(player, store, factionId, entry)
        log("faction reputation adjusted characterId=" .. tostring(characterId)
            .. " factionId=" .. tostring(factionId)
            .. " reputation=" .. tostring(entry.reputation)
            .. " tier=" .. tostring(tierFor(entry))
            .. " source=" .. tostring(source or "faction-reputation")
            .. " token=" .. tostring(token or "none"))
    end

    return true, changed, makeView(factionId, entry)
end

function CharacterFactionRelationships.SetMembership(player, factionId, member, rankId, source)
    if not player or not validId(factionId) or not FactionRegistry.Get(factionId) then
        return false, "invalid faction relationship"
    end
    if member ~= true and member ~= false then return false, "membership must be boolean" end
    if member and rankId ~= nil and not FactionRegistry.GetRank(factionId, rankId) then
        return false, "unknown faction rank"
    end

    local store, characterId = getStore(player, true)
    if not store or not characterId then return false, "character unavailable" end
    local entry = ensureEntry(store, factionId)

    local nextRankId = member and rankId or nil
    local changed = entry.member ~= member or entry.rankId ~= nextRankId
    if not changed then return true, false, makeView(factionId, entry) end

    entry.member = member
    entry.rankId = nextRankId
    entry.lastSource = tostring(source or "faction-membership")
    entry.updatedWorldHours = worldHours()
    touch(store, characterId)
    emitUpsert(player, store, factionId, entry)

    log("faction membership changed characterId=" .. tostring(characterId)
        .. " factionId=" .. tostring(factionId)
        .. " member=" .. tostring(member)
        .. " rankId=" .. tostring(nextRankId or "none")
        .. " source=" .. tostring(source or "faction-membership"))
    return true, true, makeView(factionId, entry)
end

function CharacterFactionRelationships.GetReputation(player, factionId)
    local entry = getEntry(player, factionId)
    return entry and tonumber(entry.reputation) or nil
end

function CharacterFactionRelationships.GetTier(player, factionId)
    local entry = getEntry(player, factionId)
    return entry and tierFor(entry) or nil
end

function CharacterFactionRelationships.IsMember(player, factionId)
    local entry = getEntry(player, factionId)
    return entry and entry.member == true or false
end

function CharacterFactionRelationships.GetRankId(player, factionId)
    local entry = getEntry(player, factionId)
    return entry and entry.member and entry.rankId or nil
end

function CharacterFactionRelationships.ExportView(player, factionId)
    local entry = getEntry(player, factionId)
    return entry and makeView(factionId, entry) or nil
end

function CharacterFactionRelationships.GetRevision(player)
    local store = getStore(player, false)
    if not store then return 0 end
    return math.max(0, math.floor(tonumber(store.revision) or 0))
end

LCCQF.CharacterFactionRelationships = CharacterFactionRelationships
return CharacterFactionRelationships
