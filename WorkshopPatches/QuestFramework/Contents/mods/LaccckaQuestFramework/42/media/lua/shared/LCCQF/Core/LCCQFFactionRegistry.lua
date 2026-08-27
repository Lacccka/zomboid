require "LCCQF/LCCQFConstants"

LCCQF = LCCQF or {}

local Registry = LCCQF.FactionRegistry or {}
local definitions = Registry.definitions or {}

local function isIdentifier(value)
    return type(value) == "string"
        and value ~= ""
        and #value <= LCCQF.Constants.MAX_IDENTIFIER_LENGTH
end

local function validateFacts(definition)
    if definition.initialKnowledgeFacts ~= nil and type(definition.initialKnowledgeFacts) ~= "table" then
        return false, "initialKnowledgeFacts must be a table"
    end
    if definition.knowledgeFacts ~= nil and type(definition.knowledgeFacts) ~= "table" then
        return false, "knowledgeFacts must be a table"
    end

    local seen = {}
    for _, fact in ipairs(type(definition.knowledgeFacts) == "table" and definition.knowledgeFacts or {}) do
        if type(fact) ~= "table" or not isIdentifier(fact.id)
            or not isIdentifier(fact.titleKey) or not isIdentifier(fact.textKey)
        then
            return false, "invalid faction knowledge fact"
        end
        if seen[fact.id] then return false, "duplicate faction knowledge fact" end
        seen[fact.id] = true
    end

    for _, factId in ipairs(type(definition.initialKnowledgeFacts) == "table" and definition.initialKnowledgeFacts or {}) do
        if not isIdentifier(factId) or not seen[factId] then
            return false, "unknown initial faction fact"
        end
    end
    return true
end

local function validateRanks(definition)
    if definition.ranks == nil then return true end
    if type(definition.ranks) ~= "table" then return false, "ranks must be a table" end

    local seen = {}
    for _, rank in ipairs(definition.ranks) do
        if type(rank) ~= "table"
            or not isIdentifier(rank.rankId)
            or not isIdentifier(rank.displayNameKey)
        then
            return false, "invalid faction rank"
        end
        if seen[rank.rankId] then return false, "duplicate faction rank" end
        seen[rank.rankId] = true
    end
    return true
end

function Registry.Register(definition)
    if type(definition) ~= "table" then return false, "definition must be a table" end
    if not isIdentifier(definition.factionId) then return false, "invalid factionId" end
    if not isIdentifier(definition.displayNameKey) then return false, "invalid displayNameKey" end
    if not isIdentifier(definition.summaryKey) then return false, "invalid summaryKey" end
    if definitions[definition.factionId] then return false, "duplicate factionId" end

    local factsOk, factsErr = validateFacts(definition)
    if not factsOk then return false, factsErr end
    local ranksOk, ranksErr = validateRanks(definition)
    if not ranksOk then return false, ranksErr end

    definitions[definition.factionId] = definition
    return true
end

function Registry.Get(factionId)
    if not isIdentifier(factionId) then return nil end
    return definitions[factionId]
end

function Registry.IsRegistered(factionId)
    return Registry.Get(factionId) ~= nil
end

function Registry.GetRank(factionId, rankId)
    local definition = Registry.Get(factionId)
    if not definition or not isIdentifier(rankId) then return nil end
    for _, rank in ipairs(type(definition.ranks) == "table" and definition.ranks or {}) do
        if rank.rankId == rankId then return rank end
    end
    return nil
end

function Registry.GetDefinitions()
    return definitions
end

function Registry.List()
    local result = {}
    for _, definition in pairs(definitions) do result[#result + 1] = definition end
    table.sort(result, function(a, b)
        return tostring(a.factionId) < tostring(b.factionId)
    end)
    return result
end

Registry.definitions = definitions
LCCQF.FactionRegistry = Registry
return Registry
