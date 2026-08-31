-- Periodic server coordinator for logical settlement economy snapshots.
-- Stock remains the physical observation layer; this service only evaluates policy.
if isClient and isClient() and not (isServer and isServer()) then return {} end

require "LCCQF/LCCQFConstants"
require "LCCQF/Content/LCCQFFactionDefinitions"
require "LCCQF/FactionWorld/LCCQFFactionSiteRegistry"
require "LCCQF/FactionWorld/LCCQFFactionSiteEconomy"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Factions = LCCQF.FactionRegistry
local Sites = LCCQF.FactionSiteRegistry
local Economy = LCCQF.FactionSiteEconomy
local Service = LCCQF.FactionSiteEconomyService or {}
local lastSignature = Service.lastSignature or {}

local function log(message)
    print(C.LOG_PREFIX .. "[FACTION:SITE:ECONOMY] " .. tostring(message))
end

local function signature(site)
    local economy = site.economy or {}
    local parts = {
        tostring(economy.revision or 0),
        tostring(economy.sourceStockRevision or 0),
        tostring(economy.livingPopulation or 0),
    }
    for _, row in ipairs(Economy.ListSupplies(site)) do
        parts[#parts + 1] = table.concat({
            tostring(row.supplyId),
            tostring(row.available or 0),
            tostring(row.target or 0),
            tostring(row.deficit or 0),
            tostring(row.surplus or 0),
            tostring(row.status or ""),
        }, ":")
    end
    return table.concat(parts, "|")
end

local function logChanged(site)
    local nextSignature = signature(site)
    if lastSignature[site.siteId] == nextSignature then return end
    lastSignature[site.siteId] = nextSignature
    local economy = site.economy or {}
    local rows = {}
    for _, row in ipairs(Economy.ListSupplies(site)) do
        rows[#rows + 1] = tostring(row.supplyId)
            .. "=" .. tostring(row.available or 0) .. "/" .. tostring(row.target or 0)
            .. ":" .. tostring(row.status or "")
    end
    log("siteId=" .. tostring(site.siteId)
        .. " state=" .. tostring(site.state)
        .. " revision=" .. tostring(economy.revision or 0)
        .. " stockRevision=" .. tostring(economy.sourceStockRevision or 0)
        .. " population=" .. tostring(economy.livingPopulation or 0)
        .. " supplies=" .. (#rows > 0 and table.concat(rows, ",") or "none"))
end

local function processSite(site)
    local definition = Factions.Get(site.factionId)
    if not definition then return false, "unknown faction" end
    local operations = definition.operationsProfile
    if type(operations) ~= "table" or operations.enabled == false or type(operations.supplies) ~= "table" then
        return false, "economy policy unavailable"
    end
    local ok, changedOrError = Economy.Refresh(site, definition)
    if not ok then return false, changedOrError end
    logChanged(site)
    return true, changedOrError == true
end

function Service.RunOnce()
    local processed, changed, deferred = 0, 0, 0
    for _, site in ipairs(Sites.ListSites()) do
        if site.state == "VALIDATING" or site.state == "ACTIVE" or site.state == "DORMANT" then
            local ok, didChange = processSite(site)
            if ok then
                processed = processed + 1
                if didChange then changed = changed + 1 end
            else
                deferred = deferred + 1
            end
        end
    end
    return true, processed, changed, deferred
end

local function onServerStarted()
    local ok, processed, changed, deferred = Service.RunOnce()
    log("initial pass ok=" .. tostring(ok)
        .. " processed=" .. tostring(processed or 0)
        .. " changed=" .. tostring(changed or 0)
        .. " deferred=" .. tostring(deferred or 0))
end

if isServer and isServer() then
    if Events.OnServerStarted then Events.OnServerStarted.Add(onServerStarted) end
    if Events.EveryOneMinute then Events.EveryOneMinute.Add(Service.RunOnce) end
end

Service.lastSignature = lastSignature
LCCQF.FactionSiteEconomyService = Service
return Service
