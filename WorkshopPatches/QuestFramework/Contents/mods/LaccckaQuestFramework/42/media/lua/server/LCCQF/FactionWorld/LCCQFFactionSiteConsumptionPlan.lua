-- Persistent logical consumption planning for faction settlements.
-- This module accrues demand from population and faction policy, but deliberately does
-- not mutate physical ItemContainer state. A transactional executor must prove exact
-- loaded-world removal before acknowledging pending units as consumed.
if isClient and isClient() and not (isServer and isServer()) then return {} end

require "LCCQF/LCCQFConstants"
require "LCCQF/FactionWorld/LCCQFFactionSiteRegistry"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Sites = LCCQF.FactionSiteRegistry
local Plan = LCCQF.FactionSiteConsumptionPlan or {}
local MIN_INTERVAL_HOURS = 1
local MAX_CATCHUP_HOURS = 168
local MAX_PENDING_UNITS = 100000

local function worldHours()
    if not getGameTime then return 0 end
    local gameTime = getGameTime()
    return gameTime and gameTime.getWorldAgeHours and (tonumber(gameTime:getWorldAgeHours()) or 0) or 0
end

local function normalizeUnits(value)
    return math.max(0, math.floor(tonumber(value) or 0))
end

local function policyRows(definition)
    local operations = definition and definition.operationsProfile or nil
    return type(operations) == "table" and type(operations.supplies) == "table"
        and operations.supplies or {}
end

local function ensureState(site)
    site.economy = type(site.economy) == "table" and site.economy or {
        schemaVersion = 1,
        revision = 0,
        categories = {},
    }
    local consumption = site.economy.consumption
    if type(consumption) ~= "table" then
        consumption = {
            schemaVersion = 1,
            revision = 0,
            rows = {},
        }
        site.economy.consumption = consumption
    end
    consumption.schemaVersion = 1
    consumption.revision = normalizeUnits(consumption.revision)
    consumption.rows = type(consumption.rows) == "table" and consumption.rows or {}
    return consumption
end

local function rowFor(consumption, supplyId, category)
    local row = consumption.rows[supplyId]
    if type(row) ~= "table" then
        row = {
            supplyId = supplyId,
            category = category,
            accruedFraction = 0,
            pendingUnits = 0,
            plannedTotal = 0,
            appliedTotal = 0,
        }
        consumption.rows[supplyId] = row
    end
    row.supplyId = supplyId
    row.category = category
    row.accruedFraction = math.max(0, math.min(0.999999, tonumber(row.accruedFraction) or 0))
    row.pendingUnits = normalizeUnits(row.pendingUnits)
    row.plannedTotal = normalizeUnits(row.plannedTotal)
    row.appliedTotal = normalizeUnits(row.appliedTotal)
    return row
end

function Plan.Refresh(site, definition)
    if type(site) ~= "table" or type(definition) ~= "table" then
        return false, "invalid consumption plan input"
    end
    local economy = type(site.economy) == "table" and site.economy or nil
    if not economy or type(economy.categories) ~= "table" then
        return false, "economy snapshot unavailable"
    end

    local consumption = ensureState(site)
    local now = worldHours()
    local last = tonumber(consumption.lastPlannedWorldHours)
    if last == nil then
        consumption.lastPlannedWorldHours = now
        consumption.lastObservedPopulation = normalizeUnits(economy.livingPopulation)
        consumption.revision = consumption.revision + 1
        Sites.MarkDirty(site.siteId, "settlement consumption planning initialized")
        return true, true
    end

    local elapsed = math.max(0, now - last)
    if elapsed < MIN_INTERVAL_HOURS then return true, false end
    local processedHours = math.min(elapsed, MAX_CATCHUP_HOURS)
    local population = normalizeUnits(economy.livingPopulation)
    local changed = false

    for supplyId, policy in pairs(policyRows(definition)) do
        local rate = tonumber(policy.consumptionPerResidentPerDay) or 0
        if rate < 0 then return false, "negative consumption policy: " .. tostring(supplyId) end
        if rate > 0 then
            local category = tostring(policy.category or supplyId)
            local row = rowFor(consumption, supplyId, category)
            local accrued = row.accruedFraction + (processedHours * population * rate / 24)
            local wholeUnits = math.max(0, math.floor(accrued))
            local nextFraction = math.max(0, accrued - wholeUnits)
            row.accruedFraction = nextFraction
            if wholeUnits > 0 then
                row.pendingUnits = math.min(MAX_PENDING_UNITS, row.pendingUnits + wholeUnits)
                row.plannedTotal = row.plannedTotal + wholeUnits
            end
            row.ratePerResidentPerDay = rate
            row.lastPlannedPopulation = population
            row.lastPlanWorldHours = now
            changed = true
        end
    end

    consumption.lastPlannedWorldHours = now
    consumption.lastObservedPopulation = population
    consumption.lastElapsedHours = elapsed
    consumption.lastProcessedHours = processedHours
    consumption.catchupCapped = elapsed > processedHours
    if changed then
        consumption.revision = consumption.revision + 1
        economy.revision = normalizeUnits(economy.revision) + 1
        economy.changedWorldHours = now
        Sites.MarkDirty(site.siteId, "settlement consumption demand accrued")
    end
    return true, changed
end

function Plan.GetPending(siteOrId, supplyId)
    local site = type(siteOrId) == "table" and siteOrId or Sites.GetSite(siteOrId)
    local row = site and site.economy and site.economy.consumption
        and site.economy.consumption.rows and site.economy.consumption.rows[supplyId] or nil
    return row and normalizeUnits(row.pendingUnits) or 0
end

function Plan.List(siteOrId)
    local site = type(siteOrId) == "table" and siteOrId or Sites.GetSite(siteOrId)
    local rows = site and site.economy and site.economy.consumption and site.economy.consumption.rows or {}
    local out = {}
    for _, row in pairs(type(rows) == "table" and rows or {}) do
        if type(row) == "table" then out[#out + 1] = row end
    end
    table.sort(out, function(a, b) return tostring(a.supplyId) < tostring(b.supplyId) end)
    return out
end

-- Called only after the physical executor proves exact world-container mutation and
-- post-mutation reconciliation. transactionId makes crash/retry acknowledgement
-- idempotent: a recovered MUTATED transaction can never decrement pending demand twice.
function Plan.AcknowledgeApplied(siteOrId, supplyId, quantity, transactionId)
    local site = type(siteOrId) == "table" and siteOrId or Sites.GetSite(siteOrId)
    local row = site and site.economy and site.economy.consumption
        and site.economy.consumption.rows and site.economy.consumption.rows[supplyId] or nil
    local amount = normalizeUnits(quantity)
    local txId = transactionId ~= nil and tostring(transactionId) or nil
    if txId == "" then txId = nil end
    if not row or amount < 1 then
        return false, "invalid consumption acknowledgement"
    end
    if txId and row.lastAcknowledgedTransactionId == txId then
        return true, "duplicate"
    end
    if amount > normalizeUnits(row.pendingUnits) then
        return false, "consumption acknowledgement exceeds pending demand"
    end

    row.pendingUnits = normalizeUnits(row.pendingUnits) - amount
    row.appliedTotal = normalizeUnits(row.appliedTotal) + amount
    row.lastAppliedWorldHours = worldHours()
    if txId then
        row.lastAcknowledgedTransactionId = txId
        row.lastAcknowledgedUnits = amount
    end
    site.economy.consumption.revision = normalizeUnits(site.economy.consumption.revision) + 1
    site.economy.revision = normalizeUnits(site.economy.revision) + 1
    Sites.MarkDirty(site.siteId, "settlement consumption acknowledged")
    return true, "applied"
end

LCCQF.FactionSiteConsumptionPlan = Plan
return Plan
