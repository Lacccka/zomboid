-- Read-only acceptance diagnostics for settlement supply quests.
-- This module never refreshes stock, mutates operations, registers quest definitions or
-- changes NPC state. It only projects the already server-authoritative state into a
-- compact signature-gated log row whenever that state changes.
if isClient and isClient() and not (isServer and isServer()) then return {} end

require "LCCQF/LCCQFConstants"
require "LCCQF/Quest/LCCQFQuestRegistry"
require "LCCQF/FactionWorld/LCCQFFactionSiteRegistry"
require "LCCQF/FactionWorld/LCCQFFactionSitePopulation"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local QuestRegistry = LCCQF.QuestRegistry
local Sites = LCCQF.FactionSiteRegistry
local Population = LCCQF.FactionSitePopulation
local Diagnostics = LCCQF.FactionSupplyQuestDiagnostics or {}
local lastSignature = Diagnostics.lastSignature or {}

local RELEVANT_SITE_STATES = {
    VALIDATING = true,
    ACTIVE = true,
    DORMANT = true,
}

local function log(message)
    print(C.LOG_PREFIX .. "[FACTION:SUPPLY:DIAG] " .. tostring(message))
end

local function integer(value)
    return math.max(0, math.floor(tonumber(value) or 0))
end

local function booleanText(value)
    return value == true and "true" or "false"
end

local function sortedSupplySignals(site)
    local out = {}
    local signals = site and site.operations and site.operations.signals or nil
    for _, signal in pairs(type(signals) == "table" and signals or {}) do
        if type(signal) == "table" and signal.kind == "supply" then
            out[#out + 1] = signal
        end
    end
    table.sort(out, function(a, b)
        if tostring(a.supplyId or "") ~= tostring(b.supplyId or "") then
            return tostring(a.supplyId or "") < tostring(b.supplyId or "")
        end
        return tostring(a.signalId or "") < tostring(b.signalId or "")
    end)
    return out
end

local function offerForSignal(site, signal)
    local offers = site and site.operations and site.operations.questOffers or nil
    local wantedSignalId = tostring(signal and signal.signalId or "")
    local wantedEpoch = integer(signal and signal.openEpoch)
    local best = nil
    for _, offer in pairs(type(offers) == "table" and offers or {}) do
        if type(offer) == "table" and tostring(offer.signalId or "") == wantedSignalId then
            local epoch = integer(offer.openEpoch)
            if epoch == wantedEpoch then return offer end
            if not best or epoch > integer(best.openEpoch) then best = offer end
        end
    end
    return best
end

local function giverProjection(site, offer)
    local npcId = type(offer) == "table" and tostring(offer.giverNpcId or "") or ""
    if npcId == "" then return "none", "none", "none", "none" end
    local member = Population.GetMemberByNpcId(site, npcId)
    if not member then return npcId, "missing", "none", "none" end
    return npcId,
        tostring(member.state or "unknown"),
        tostring(member.runtimeId or "none"),
        tostring(member.roleId or "unknown")
end

local function rowFor(site, signal)
    local stock = type(site.stock) == "table" and site.stock or {}
    local offer = signal and offerForSignal(site, signal) or nil
    local questId = type(offer) == "table" and tostring(offer.questId or "") or ""
    local definitionRegistered = questId ~= "" and QuestRegistry.Get(questId) ~= nil
    local giverNpcId, giverState, giverRuntimeId, giverRoleId = giverProjection(site, offer)
    local category = signal and tostring(signal.category or "") or ""
    local stockCategory = category ~= ""
        and integer(stock.categories and stock.categories[category])
        or 0

    return {
        siteId = tostring(site.siteId or "unknown"),
        siteState = tostring(site.state or "unknown"),
        stockComplete = stock.complete == true,
        stockRevision = integer(stock.revision),
        stockContainers = integer(stock.containerCount),
        stockItems = integer(stock.itemCount),
        supplyId = signal and tostring(signal.supplyId or "unknown") or "none",
        category = category ~= "" and category or "none",
        stockCategory = stockCategory,
        signalId = signal and tostring(signal.signalId or "unknown") or "none",
        signalStatus = signal and tostring(signal.status or "unknown") or "none",
        openEpoch = signal and integer(signal.openEpoch) or 0,
        available = signal and integer(signal.available) or 0,
        target = signal and integer(signal.target) or 0,
        deficit = signal and integer(signal.value) or 0,
        signalRevision = signal and integer(signal.revision) or 0,
        offerQuestId = questId ~= "" and questId or "none",
        offerStatus = type(offer) == "table" and tostring(offer.status or "unknown") or "none",
        definitionRegistered = definitionRegistered,
        giverNpcId = giverNpcId,
        giverState = giverState,
        giverRuntimeId = giverRuntimeId,
        giverRoleId = giverRoleId,
    }
end

local SIGNATURE_FIELDS = {
    "siteId", "siteState", "stockComplete", "stockRevision", "stockContainers", "stockItems",
    "supplyId", "category", "stockCategory", "signalId", "signalStatus", "openEpoch",
    "available", "target", "deficit", "signalRevision", "offerQuestId", "offerStatus",
    "definitionRegistered", "giverNpcId", "giverState", "giverRuntimeId", "giverRoleId",
}

local function signature(row)
    local parts = {}
    for _, field in ipairs(SIGNATURE_FIELDS) do
        parts[#parts + 1] = tostring(row[field])
    end
    return table.concat(parts, "|")
end

local function rowKey(row)
    return tostring(row.siteId) .. "|" .. tostring(row.supplyId) .. "|" .. tostring(row.signalId)
end

local function logRow(row, source)
    log("source=" .. tostring(source or "periodic")
        .. " siteId=" .. row.siteId
        .. " siteState=" .. row.siteState
        .. " stockComplete=" .. booleanText(row.stockComplete)
        .. " stockRev=" .. tostring(row.stockRevision)
        .. " containers=" .. tostring(row.stockContainers)
        .. " items=" .. tostring(row.stockItems)
        .. " supplyId=" .. row.supplyId
        .. " category=" .. row.category
        .. " stockCategory=" .. tostring(row.stockCategory)
        .. " signal=" .. row.signalId
        .. " status=" .. row.signalStatus
        .. " openEpoch=" .. tostring(row.openEpoch)
        .. " available=" .. tostring(row.available)
        .. " target=" .. tostring(row.target)
        .. " deficit=" .. tostring(row.deficit)
        .. " signalRev=" .. tostring(row.signalRevision)
        .. " questId=" .. row.offerQuestId
        .. " offer=" .. row.offerStatus
        .. " definition=" .. booleanText(row.definitionRegistered)
        .. " giverNpcId=" .. row.giverNpcId
        .. " giverRole=" .. row.giverRoleId
        .. " giverState=" .. row.giverState
        .. " giverRuntimeId=" .. row.giverRuntimeId)
end

function Diagnostics.Dump(force, source)
    local seen = {}
    local emitted = 0
    for _, site in ipairs(Sites.ListSites()) do
        if RELEVANT_SITE_STATES[site.state] == true then
            local signals = sortedSupplySignals(site)
            if #signals == 0 then signals = { false } end
            for _, signal in ipairs(signals) do
                local row = rowFor(site, signal or nil)
                local key = rowKey(row)
                seen[key] = true
                local nextSignature = signature(row)
                if force == true or lastSignature[key] ~= nextSignature then
                    lastSignature[key] = nextSignature
                    logRow(row, source)
                    emitted = emitted + 1
                end
            end
        end
    end

    for key in pairs(lastSignature) do
        if not seen[key] then lastSignature[key] = nil end
    end
    Diagnostics.lastSignature = lastSignature
    return emitted
end

local function onServerStarted()
    Diagnostics.Dump(true, "server-start")
end

local function onEveryOneMinute()
    Diagnostics.Dump(false, "minute")
end

if isServer and isServer() then
    if Events.OnServerStarted then Events.OnServerStarted.Add(onServerStarted) end
    if Events.EveryOneMinute then Events.EveryOneMinute.Add(onEveryOneMinute) end
end

Diagnostics.lastSignature = lastSignature
LCCQF.FactionSupplyQuestDiagnostics = Diagnostics
return Diagnostics
