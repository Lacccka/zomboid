-- Server-owned objective for an autonomous settlement supply episode.
-- Confirmed vanilla transfers enter only through an ephemeral server queue. QuestService
-- consumes them through its normal EvaluateTick/applyHandlerResult persistence path.
if isClient and isClient() and not (isServer and isServer()) then return {} end

require "LCCQF/Content/LCCQFSupplyCategoryDefinitions"
require "LCCQF/FactionWorld/LCCQFFactionSiteRegistry"

LCCQF = LCCQF or {}
LCCQF.QuestObjectives = LCCQF.QuestObjectives or {}

local Categories = LCCQF.SupplyCategoryRegistry
local Sites = LCCQF.FactionSiteRegistry
local SettlementSupply = LCCQF.QuestObjectives.SettlementSupply or {}
local transferQueues = SettlementSupply.transferQueues or {}

local function validString(value, maximum)
    return type(value) == "string" and value ~= "" and #value <= (maximum or 192)
end

local function normalizedEpoch(value)
    local number = tonumber(value)
    if number == nil or number < 1 then return nil end
    return math.floor(number)
end

local function quantitySemantics(category)
    if not Categories or not Categories.GetQuantitySemantics then return nil end
    return Categories.GetQuantitySemantics(category)
end

local function normalizeQuantity(category, value)
    local number = tonumber(value)
    if number == nil or number ~= number or number == math.huge or number == -math.huge or number <= 0 then
        return 0
    end
    if Categories and Categories.NormalizeQuantity then
        return Categories.NormalizeQuantity(category, number)
    end
    return math.max(0, number)
end

local function quantityQuantum(category)
    local semantics = quantitySemantics(category)
    local precision = semantics and math.max(0, math.floor(tonumber(semantics.precision) or 0)) or 0
    if precision <= 0 then return 1 end
    return 1 / (10 ^ precision)
end

local function minimumContribution(value, category)
    local raw = tonumber(value)
    if raw == nil or raw ~= raw or raw == math.huge or raw == -math.huge then raw = 1 end
    raw = math.max(quantityQuantum(category), math.min(100000, raw))
    local normalized = normalizeQuantity(category, raw)
    return normalized > 0 and normalized or quantityQuantum(category)
end

local function playerKey(player)
    if not player then return nil end
    if player.getOnlineID then
        local ok, value = pcall(function() return player:getOnlineID() end)
        if ok and value ~= nil then return "id:" .. tostring(value) end
    end
    if player.getUsername then
        local ok, value = pcall(function() return player:getUsername() end)
        if ok and value ~= nil then return "user:" .. tostring(value) end
    end
    return nil
end

function SettlementSupply.Create(spec)
    if type(spec) ~= "table"
        or not validString(spec.siteId, 288)
        or not validString(spec.supplyId, 96)
        or not validString(spec.category, 96)
        or not validString(spec.signalId, 384)
        or not normalizedEpoch(spec.openEpoch)
        or not Categories.IsRegistered(spec.category)
    then
        return nil, "invalid SettlementSupply objective"
    end

    local semantics = quantitySemantics(spec.category) or {}
    return {
        id = spec.id,
        type = "SettlementSupply",
        titleKey = spec.titleKey,
        state = "pending",
        siteId = spec.siteId,
        supplyId = spec.supplyId,
        category = spec.category,
        unitKind = semantics.unitKind,
        precision = semantics.precision,
        signalId = spec.signalId,
        openEpoch = normalizedEpoch(spec.openEpoch),
        minimumContribution = minimumContribution(spec.minimumContribution, spec.category),
        contributed = 0,
        lastAvailable = normalizeQuantity(spec.category, spec.available),
        target = normalizeQuantity(spec.category, spec.target),
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

local function eventQuantity(objective, event)
    if type(objective) ~= "table" or type(event) ~= "table" then return 0 end
    local categories = type(event.categories) == "table" and event.categories or {}
    return normalizeQuantity(objective.category, categories[objective.category])
end

local function eventMatches(objective, event)
    if type(objective) ~= "table" or type(event) ~= "table" then return false end
    if tostring(event.siteId or "") ~= tostring(objective.siteId or "") then return false end
    if eventQuantity(objective, event) <= 0 then return false end

    -- Transfer credit belongs to one supply-need episode only. The observer refreshes
    -- authoritative stock/operations before queueing the event, so a transfer that closes
    -- this episode still sees the same openEpoch with RESOLVED status. A later episode has
    -- a higher openEpoch and must never complete an older accepted quest.
    local _, signal = signalForObjective(objective)
    return type(signal) == "table"
        and normalizedEpoch(signal.openEpoch) == normalizedEpoch(objective.openEpoch)
end

function SettlementSupply.QueueConfirmedTransfer(player, event)
    local key = playerKey(player)
    if not key or type(event) ~= "table" or event.stockRefreshOk ~= true then return false end
    local queue = transferQueues[key]
    if type(queue) ~= "table" then
        queue = {}
        transferQueues[key] = queue
    end
    if #queue >= 128 then table.remove(queue, 1) end
    queue[#queue + 1] = event
    SettlementSupply.transferQueues = transferQueues
    return true
end

function SettlementSupply.DiscardQueuedTransfers(player)
    local key = playerKey(player)
    if not key then return false end
    local existed = transferQueues[key] ~= nil
    transferQueues[key] = nil
    SettlementSupply.transferQueues = transferQueues
    return existed
end

local function consumeMatchingTransfer(player, objective)
    local key = playerKey(player)
    local queue = key and transferQueues[key] or nil
    if type(queue) ~= "table" then return nil end
    for index, event in ipairs(queue) do
        if eventMatches(objective, event) then
            table.remove(queue, index)
            if #queue == 0 then transferQueues[key] = nil end
            return event
        end
    end
    return nil
end

local function applyContribution(objective, event)
    local amount = eventQuantity(objective, event)
    if amount <= 0 then return false end
    objective.contributed = normalizeQuantity(
        objective.category,
        (tonumber(objective.contributed) or 0) + amount
    )
    return true
end

local function refreshSignalProgress(objective, signal)
    if type(signal) ~= "table" then return false end
    local changed = false
    local available = normalizeQuantity(objective.category, signal.available)
    local target = normalizeQuantity(objective.category, signal.target)
    if tonumber(objective.lastAvailable) ~= available then objective.lastAvailable = available changed = true end
    if tonumber(objective.target) ~= target then objective.target = target changed = true end
    return changed
end

function SettlementSupply.EvaluateSettlementTransfer(player, objective, event)
    if not player or not objective or objective.state ~= "active" or not eventMatches(objective, event) then
        return false, false
    end
    local changed = applyContribution(objective, event)
    local _, signal = signalForObjective(objective)
    if refreshSignalProgress(objective, signal) then changed = true end
    local contributed = normalizeQuantity(objective.category, objective.contributed)
    local required = minimumContribution(objective.minimumContribution, objective.category)
    local complete = contributed >= required and originalEpisodeClosed(objective, signal)
    local failed = not complete and originalEpisodeClosed(objective, signal)
    return complete, changed,
        complete and "settlement_supply_delivered" or "settlement_supply_contribution",
        failed
end

function SettlementSupply.EvaluateTick(player, objective)
    if not player or not objective or objective.state ~= "active" then return false, false end

    local changed = false
    local event = consumeMatchingTransfer(player, objective)
    if event and applyContribution(objective, event) then changed = true end

    local _, signal = signalForObjective(objective)
    if refreshSignalProgress(objective, signal) then changed = true end

    local contributed = normalizeQuantity(objective.category, objective.contributed)
    local required = minimumContribution(objective.minimumContribution, objective.category)
    local closed = originalEpisodeClosed(objective, signal)
    local complete = contributed >= required and closed
    local failed = closed and not complete
    local reason = nil
    if complete then
        reason = "settlement_supply_resolved"
    elseif failed then
        reason = "settlement_supply_episode_closed"
    elseif event then
        reason = "settlement_supply_contribution"
    end
    return complete, changed, reason, failed
end

function SettlementSupply.MakeProgressView(objective)
    if type(objective) ~= "table" then return 0, 1 end
    return normalizeQuantity(objective.category, objective.contributed),
        minimumContribution(objective.minimumContribution, objective.category)
end

SettlementSupply.transferQueues = transferQueues
LCCQF.QuestObjectives.SettlementSupply = SettlementSupply
return SettlementSupply
