require "LCCQF/Content/LCCQFFactionDefinitions"
require "LCCQF/FactionWorld/LCCQFFactionSiteRegistry"
require "LCCQF/FactionWorld/LCCQFFactionSiteCandidateIndex"
require "LCCQF/FactionWorld/LCCQFFactionSiteSafetyValidator"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local FactionRegistry = LCCQF.FactionRegistry
local Sites = LCCQF.FactionSiteRegistry
local Candidates = LCCQF.FactionSiteCandidateIndex
local Validator = LCCQF.FactionSiteSafetyValidator
local Allocator = LCCQF.FactionSiteAllocator or {}

local diagnostics = Allocator.diagnostics or {}
local lastLogSignature = Allocator.lastLogSignature or {}
local initialized = false

local function log(message)
    print(C.LOG_PREFIX .. "[FACTION:SITE:ALLOCATOR] " .. tostring(message))
end

local function allocatableDefinitions()
    local result = {}
    for _, definition in ipairs(FactionRegistry.List()) do
        local profile = definition.siteProfile
        if type(profile) == "table" and profile.enabled ~= false
            and Sites.HasCapacity(definition.factionId, profile.maxSites or 1)
        then
            result[#result + 1] = definition
        end
    end
    return result
end

local function addDiagnostic(list, candidate, outcome, detail)
    list[#list + 1] = {
        candidateKey = candidate and candidate.candidateKey or nil,
        zoneType = candidate and candidate.zoneType or nil,
        roomCount = candidate and candidate.roomCount or nil,
        score = candidate and candidate.score or nil,
        outcome = outcome,
        detail = type(detail) == "string" and detail or nil,
    }
end

local function diagnosticSignature(records)
    local parts = {}
    for i = 1, math.min(#records, C.FACTION_SITE_DIAGNOSTIC_TOP_N or 5) do
        local record = records[i]
        parts[#parts + 1] = table.concat({
            tostring(record.candidateKey or "none"),
            tostring(record.outcome or "none"),
            tostring(record.detail or "none"),
        }, ":")
    end
    return table.concat(parts, "|")
end

local function logDiagnostics(factionId, records)
    local signature = diagnosticSignature(records)
    if lastLogSignature[factionId] == signature then return end
    lastLogSignature[factionId] = signature

    if #records == 0 then
        log("no scored candidates factionId=" .. tostring(factionId))
        return
    end

    local maximum = math.min(#records, C.FACTION_SITE_DIAGNOSTIC_TOP_N or 5)
    for i = 1, maximum do
        local record = records[i]
        log("candidate factionId=" .. tostring(factionId)
            .. " rank=" .. tostring(i)
            .. " key=" .. tostring(record.candidateKey or "none")
            .. " zone=" .. tostring(record.zoneType or "Unknown")
            .. " rooms=" .. tostring(record.roomCount or 0)
            .. " score=" .. string.format("%.2f", tonumber(record.score) or 0)
            .. " outcome=" .. tostring(record.outcome or "unknown")
            .. " detail=" .. tostring(record.detail or "none"))
    end
end

local function allocateFaction(definition)
    local factionId = definition.factionId
    local records = {}
    local ranked = Candidates.RankForFaction(definition, 24)

    for _, candidate in ipairs(ranked) do
        local outcome, detail = Validator.Validate(definition, candidate)
        if outcome == "PASS" then
            local reserved, siteOrError = Sites.ReserveCandidate(factionId, candidate, "loaded-world-dry-run")
            if reserved then
                addDiagnostic(records, candidate, "RESERVED", nil)
                diagnostics[factionId] = records
                logDiagnostics(factionId, records)
                return true, siteOrError
            end
            Candidates.NoteRejection(factionId, candidate.candidateKey, siteOrError)
            addDiagnostic(records, candidate, "REJECT", tostring(siteOrError))
        elseif outcome == "DEFER" then
            addDiagnostic(records, candidate, "DEFER", tostring(detail))
        else
            Candidates.NoteRejection(factionId, candidate.candidateKey, detail)
            addDiagnostic(records, candidate, "REJECT", tostring(detail))
        end
    end

    diagnostics[factionId] = records
    logDiagnostics(factionId, records)
    return false, "no valid loaded candidate"
end

function Allocator.Initialize()
    if initialized then return true end
    local ok, err = Sites.Initialize()
    if not ok then return false, err end
    initialized = true
    log("initialized dryRun=true materialization=false")
    return true
end

function Allocator.RunOnce()
    local ok, err = Allocator.Initialize()
    if not ok then return false, err end

    local definitions = allocatableDefinitions()
    if #definitions == 0 then return true, 0 end

    local observed, loadedRoomCount = Candidates.DiscoverLoadedBuildings(C.FACTION_SITE_MAX_ROOMS_PER_PASS)
    log("discovery pass observedRooms=" .. tostring(observed)
        .. " loadedRoomCount=" .. tostring(loadedRoomCount)
        .. " indexedCandidates=" .. tostring(#Candidates.ListCandidates()))

    local reservedCount = 0
    for _, definition in ipairs(definitions) do
        local reserved = allocateFaction(definition)
        if reserved then reservedCount = reservedCount + 1 end
    end
    return true, reservedCount
end

function Allocator.GetDiagnostics(factionId)
    if factionId ~= nil then return diagnostics[factionId] or {} end
    return diagnostics
end

local function onInitGlobalModData()
    local ok, err = Allocator.Initialize()
    if not ok then log("initialization deferred error=" .. tostring(err)) end
end

local function onGameStart()
    local ok, err = Allocator.RunOnce()
    if not ok then log("initial allocation deferred error=" .. tostring(err)) end
end

local function onEveryOneMinute()
    local ok, err = Allocator.RunOnce()
    if not ok then log("allocation pass failed error=" .. tostring(err)) end
end

if Events and Events.OnInitGlobalModData then Events.OnInitGlobalModData.Add(onInitGlobalModData) end
if Events and Events.OnGameStart then Events.OnGameStart.Add(onGameStart) end
if Events and Events.EveryOneMinute then Events.EveryOneMinute.Add(onEveryOneMinute) end

Allocator.diagnostics = diagnostics
Allocator.lastLogSignature = lastLogSignature
LCCQF.FactionSiteAllocator = Allocator
return Allocator
