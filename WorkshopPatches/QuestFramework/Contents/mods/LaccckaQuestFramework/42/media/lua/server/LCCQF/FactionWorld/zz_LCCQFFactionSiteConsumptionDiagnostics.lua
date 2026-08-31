-- Read-only acceptance diagnostics for physical settlement consumption.
-- Emits only when the authoritative signature changes; never refreshes/mutates world state.
if isClient and isClient() and not (isServer and isServer()) then return {} end

require "LCCQF/LCCQFConstants"
require "LCCQF/FactionWorld/LCCQFFactionSiteRegistry"
require "LCCQF/FactionWorld/LCCQFFactionSiteConsumptionPlan"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Sites = LCCQF.FactionSiteRegistry
local Plan = LCCQF.FactionSiteConsumptionPlan
local Diagnostics = LCCQF.FactionSiteConsumptionDiagnostics or {}
local lastSignature = Diagnostics.lastSignature or {}

local function units(value)
    return math.max(0, math.floor(tonumber(value) or 0))
end

local function itemStateCounts(tx)
    local counts = { SELECTED = 0, REMOVING = 0, REMOVED = 0, SKIPPED = 0, REAPPEARED = 0 }
    for _, descriptor in ipairs(type(tx) == "table" and tx.items or {}) do
        local state = tostring(descriptor.state or "UNKNOWN")
        counts[state] = units(counts[state]) + 1
    end
    return counts
end

local function rowSignature(site, row)
    local tx = type(row.execution) == "table" and row.execution or nil
    local last = type(row.lastExecution) == "table" and row.lastExecution or nil
    local counts = itemStateCounts(tx)
    return table.concat({
        tostring(site.state),
        tostring(site.stock and site.stock.revision or 0),
        tostring(site.economy and site.economy.revision or 0),
        tostring(row.supplyId),
        tostring(row.category),
        tostring(row.pendingUnits or 0),
        tostring(row.plannedTotal or 0),
        tostring(row.appliedTotal or 0),
        tostring(tx and tx.txId or "none"),
        tostring(tx and tx.state or "none"),
        tostring(counts.SELECTED), tostring(counts.REMOVING), tostring(counts.REMOVED),
        tostring(counts.SKIPPED), tostring(counts.REAPPEARED),
        tostring(tx and tx.networkSyncFailed == true),
        tostring(last and last.txId or "none"),
        tostring(last and last.outcome or "none"),
        tostring(last and last.appliedUnits or 0),
        tostring(last and last.networkSyncFailed == true),
    }, "|")
end

local function logRow(site, row)
    local key = tostring(site.siteId) .. ":" .. tostring(row.supplyId)
    local signature = rowSignature(site, row)
    if lastSignature[key] == signature then return end
    lastSignature[key] = signature

    local tx = type(row.execution) == "table" and row.execution or nil
    local last = type(row.lastExecution) == "table" and row.lastExecution or nil
    local counts = itemStateCounts(tx)
    print(C.LOG_PREFIX .. "[FACTION:SITE:CONSUMPTION:DIAG]"
        .. " siteId=" .. tostring(site.siteId)
        .. " siteState=" .. tostring(site.state)
        .. " supplyId=" .. tostring(row.supplyId)
        .. " category=" .. tostring(row.category)
        .. " pending=" .. tostring(units(row.pendingUnits))
        .. " planned=" .. tostring(units(row.plannedTotal))
        .. " applied=" .. tostring(units(row.appliedTotal))
        .. " stockRevision=" .. tostring(units(site.stock and site.stock.revision))
        .. " economyRevision=" .. tostring(units(site.economy and site.economy.revision))
        .. " txId=" .. tostring(tx and tx.txId or "none")
        .. " txState=" .. tostring(tx and tx.state or "none")
        .. " selected=" .. tostring(counts.SELECTED)
        .. " removing=" .. tostring(counts.REMOVING)
        .. " removed=" .. tostring(counts.REMOVED)
        .. " skipped=" .. tostring(counts.SKIPPED)
        .. " reappeared=" .. tostring(counts.REAPPEARED)
        .. " txNetworkSyncFailed=" .. tostring(tx and tx.networkSyncFailed == true)
        .. " lastTxId=" .. tostring(last and last.txId or "none")
        .. " lastOutcome=" .. tostring(last and last.outcome or "none")
        .. " lastApplied=" .. tostring(units(last and last.appliedUnits))
        .. " lastNetworkSyncFailed=" .. tostring(last and last.networkSyncFailed == true))
end

function Diagnostics.RunOnce()
    for _, site in ipairs(Sites.ListSites()) do
        for _, row in ipairs(Plan.List(site)) do
            if units(row.pendingUnits) > 0 or type(row.execution) == "table" or type(row.lastExecution) == "table" then
                logRow(site, row)
            end
        end
    end
end

local function onServerStarted()
    Diagnostics.RunOnce()
end

if isServer and isServer() then
    if Events.OnServerStarted then Events.OnServerStarted.Add(onServerStarted) end
    if Events.EveryOneMinute then Events.EveryOneMinute.Add(Diagnostics.RunOnce) end
end

Diagnostics.lastSignature = lastSignature
LCCQF.FactionSiteConsumptionDiagnostics = Diagnostics
return Diagnostics
