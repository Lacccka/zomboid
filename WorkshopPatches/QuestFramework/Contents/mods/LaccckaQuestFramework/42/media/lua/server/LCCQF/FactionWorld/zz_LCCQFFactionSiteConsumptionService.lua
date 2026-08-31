-- Server coordinator for logical settlement consumption demand.
-- Runs from EveryOneMinute but the planner persists at most once per in-game hour.
if isClient and isClient() and not (isServer and isServer()) then return {} end

require "LCCQF/LCCQFConstants"
require "LCCQF/Content/LCCQFFactionDefinitions"
require "LCCQF/FactionWorld/LCCQFFactionSiteRegistry"
require "LCCQF/FactionWorld/LCCQFFactionSiteConsumptionPlan"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Factions = LCCQF.FactionRegistry
local Sites = LCCQF.FactionSiteRegistry
local Plan = LCCQF.FactionSiteConsumptionPlan
local Service = LCCQF.FactionSiteConsumptionService or {}
local lastSignature = Service.lastSignature or {}

local function log(message)
    print(C.LOG_PREFIX .. "[FACTION:SITE:CONSUMPTION] " .. tostring(message))
end

local function signature(site)
    local consumption = site.economy and site.economy.consumption or {}
    local parts = { tostring(consumption.revision or 0) }
    for _, row in ipairs(Plan.List(site)) do
        parts[#parts + 1] = tostring(row.supplyId)
            .. "=" .. tostring(row.pendingUnits or 0)
            .. "/" .. tostring(row.plannedTotal or 0)
            .. "/" .. tostring(row.appliedTotal or 0)
    end
    return table.concat(parts, "|")
end

local function logChanged(site)
    local nextSignature = signature(site)
    if lastSignature[site.siteId] == nextSignature then return end
    lastSignature[site.siteId] = nextSignature
    local rows = {}
    for _, row in ipairs(Plan.List(site)) do
        rows[#rows + 1] = tostring(row.supplyId)
            .. ":pending=" .. tostring(row.pendingUnits or 0)
            .. ":planned=" .. tostring(row.plannedTotal or 0)
    end
    log("siteId=" .. tostring(site.siteId)
        .. " state=" .. tostring(site.state)
        .. " demand=" .. (#rows > 0 and table.concat(rows, ",") or "none"))
end

local function processSite(site)
    local definition = Factions.Get(site.factionId)
    if not definition then return false end
    local ok, changed = Plan.Refresh(site, definition)
    if ok then logChanged(site) end
    return ok, changed == true
end

function Service.RunOnce()
    local processed, changed = 0, 0
    for _, site in ipairs(Sites.ListSites()) do
        -- DORMANT settlements keep accruing logical demand while their physical world is
        -- unloaded. No item mutation happens here; execution waits for loaded-world proof.
        if site.state == "ACTIVE" or site.state == "DORMANT" then
            local ok, didChange = processSite(site)
            if ok then
                processed = processed + 1
                if didChange then changed = changed + 1 end
            end
        end
    end
    return true, processed, changed
end

local function onServerStarted()
    local ok, processed, changed = Service.RunOnce()
    log("initial pass ok=" .. tostring(ok)
        .. " processed=" .. tostring(processed or 0)
        .. " changed=" .. tostring(changed or 0))
end

if isServer and isServer() then
    if Events.OnServerStarted then Events.OnServerStarted.Add(onServerStarted) end
    if Events.EveryOneMinute then Events.EveryOneMinute.Add(Service.RunOnce) end
end

Service.lastSignature = lastSignature
LCCQF.FactionSiteConsumptionService = Service
return Service
