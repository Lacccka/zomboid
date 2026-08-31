-- Server-owned objective for an autonomous settlement supply episode.
-- Progress is credited only by confirmed settlement transfer events. Completion also
-- requires the original need episode to have closed in authoritative site operations.
if isClient and isClient() and not (isServer and isServer()) then return {} end

require "LCCQF/FactionWorld/LCCQFFactionSiteRegistry"

LCCQF = LCCQF or {}
LCCQF.QuestObjectives = LCCQF.QuestObjectives or {}

local Sites = LCCQF.FactionSiteRegistry
local SettlementSupply = {}

local function validString(value, maximum)
    return type(value) == "string" and value ~= "" and #value <= (maximum or 192)
end

local function normalizedEpoch(value)
    local number = tonumber(value)
    if number == nil or number < 1 then return nil end
    return math.floor(number)
end

local function minimumContribution(value)
    return math.max(1, math.min(1000, math.floor(tonumber(value) or 1)))
end

function SettlementSupply.Create(spec)
    if type(spec) ~= "table"
        or not validString(spec.siteId, 288)
        or not validString(spec.supplyId, 96)
        or not validString(spec.category, 96)
        or not validString(spec.signalId, 384)
        or not normalizedEpoch(spec.openEpoch)
    then
        return nil, "invalid SettlementSupply objective"
    end

    return {
        id = spec.id,
        type = "SettlementSupply",
        titleKey = spec.titleKey,
        state = "pending",
        siteId = spec.siteId,
        supplyId = spec.supplyId,
        category = spec.category,
        signalId = spec.signalId,
        openEpoch = normalizedEpoch(spec.openEpoch),
        minimumContribution = minimumContribution(spec.minimumContribution),
        contributed = 0,
        lastAvailable = math.max(0, math.floor(tonumber(spec.available) or 0)),
        target = math.max(0, math.floor(tonumber(spec.target) or 0)),
    }
end

function SettlementSupply.ValidatePersisted(objective)
    return type(objective) == "table"
        and validString(objective.siteId, 288)
        and validString(objective.supplyId, 96)
        and validString(objective.category, 96)
        and validString(objective.signalId, 384)
        and normalizedEpoch(objective.openEpoch) ~= nil
        and tonumber(objective.minimumContribution) ~= nil
        and tonumber(objective.contributed) ~= nil
end

local function signalForObjective(objective)
    local site = Sites.GetSite(objective.siteId)
    local signals = site and site.operations and site.operations.signals or nil
    local signal = type(signals) == "table" and signals[objective.signalId] or nil
    return site, type(signal) == "table" and signal or nil
end

local function originalEpisodeClosed(objective, signal)
    if type(signal) ~= "table" then return false end
    local currentEpoch = math.max(0, math.floor(tonumber(signal.openEpoch) or 0))
    local wantedEpoch = normalizedEpoch(objective.openEpoch) or 0
    if currentEpoch > wantedEpoch then return true end
    return currentEpoch == wantedEpoch and signal.status == "RESOLVED"
end

function SettlementSupply.EvaluateSettlementTransfer(player, objective, event)
    if not player or not objective or objective.state ~= "active" or type(event) ~= "table" then
        return false, false
    end
    if tostring(event.siteId or "") ~= tostring(objective.siteId) then return false, false end
    local categories = type(event.categories) == "table" and event.categories or {}
    if categories[objective.category] ~= true then return false, false end

    local contributed = math.max(0, math.floor(tonumber(objective.contributed) or 0)) + 1
    objective.contributed = contributed
    local site, signal = signalForObjective(objective)
    if site and signal then
        objective.lastAvailable = math.max(0, math.floor(tonumber(signal.available) or 0))
        objective.target = math.max(0, math.floor(tonumber(signal.target) or 0))
    end

    local complete = contributed >= minimumContribution(objective.minimumContribution)
        and originalEpisodeClosed(objective, signal)
    return complete, true, complete and "settlement_supply_delivered" or "settlement_supply_contribution"
end

function SettlementSupply.EvaluateTick(player, objective)
    if not player or not objective or objective.state ~= "active" then return false, false end
    local _, signal = signalForObjective(objective)
    local changed = false
    if signal then
        local available = math.max(0, math.floor(tonumber(signal.available) or 0))
        local target = math.max(0, math.floor(tonumber(signal.target) or 0))
        if tonumber(objective.lastAvailable) ~= available then objective.lastAvailable = available changed = true end
        if tonumber(objective.target) ~= target then objective.target = target changed = true end
    end
    local complete = math.max(0, math.floor(tonumber(objective.contributed) or 0))
            >= minimumContribution(objective.minimumContribution)
        and originalEpisodeClosed(objective, signal)
    return complete, changed, complete and "settlement_supply_resolved" or nil
end

function SettlementSupply.MakeProgressView(objective)
    return math.max(0, math.floor(tonumber(objective and objective.contributed) or 0)),
        minimumContribution(objective and objective.minimumContribution)
end

LCCQF.QuestObjectives.SettlementSupply = SettlementSupply
return SettlementSupply
