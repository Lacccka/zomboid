-- Server coordinator for transactional physical settlement consumption.
-- Planning accrues logical demand first; this service attempts bounded loaded-world
-- execution and refreshes the economy before operations/signals run in the same tick.
if isClient and isClient() and not (isServer and isServer()) then return {} end

require "LCCQF/LCCQFConstants"
require "LCCQF/Content/LCCQFFactionDefinitions"
require "LCCQF/FactionWorld/LCCQFFactionSiteRegistry"
require "LCCQF/FactionWorld/LCCQFFactionSiteConsumptionPlan"
require "LCCQF/FactionWorld/LCCQFFactionSiteConsumptionExecutor"
require "LCCQF/FactionWorld/LCCQFFactionSiteEconomy"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Factions = LCCQF.FactionRegistry
local Sites = LCCQF.FactionSiteRegistry
local Plan = LCCQF.FactionSiteConsumptionPlan
local Executor = LCCQF.FactionSiteConsumptionExecutor
local Economy = LCCQF.FactionSiteEconomy
local Service = LCCQF.FactionSiteConsumptionExecutorService or {}
local lastOutcome = Service.lastOutcome or {}

local function log(message)
    print(C.LOG_PREFIX .. "[FACTION:SITE:CONSUMPTION:EXEC] " .. tostring(message))
end

local function logOutcome(site, supplyId, outcome)
    local key = tostring(site.siteId) .. ":" .. tostring(supplyId)
    if lastOutcome[key] == outcome then return end
    lastOutcome[key] = outcome
    log("siteId=" .. tostring(site.siteId)
        .. " supplyId=" .. tostring(supplyId)
        .. " " .. tostring(outcome))
end

local function processSite(site)
    local definition = Factions.Get(site.factionId)
    if not definition then return 0, 0, 0 end

    local attempted, applied, deferred = 0, 0, 0
    for _, row in ipairs(Plan.List(site)) do
        local pending = Plan.GetPending(site, row.supplyId)
        if pending > 0 or type(row.execution) == "table" then
            attempted = attempted + 1
            local ok, result = Executor.ExecuteSupply(site, row.supplyId)
            if ok then
                local consumed = math.max(0, math.floor(tonumber(result) or 0))
                applied = applied + consumed
                logOutcome(site, row.supplyId,
                    "outcome=PASS applied=" .. tostring(consumed)
                    .. " pending=" .. tostring(Plan.GetPending(site, row.supplyId)))
            else
                deferred = deferred + 1
                logOutcome(site, row.supplyId, "outcome=DEFER detail=" .. tostring(result))
            end
        end
    end

    if applied > 0 then
        local ok, changedOrError = Economy.Refresh(site, definition)
        if not ok then
            log("siteId=" .. tostring(site.siteId)
                .. " economyRefresh=DEFER detail=" .. tostring(changedOrError))
        end
    end
    return attempted, applied, deferred
end

function Service.RunOnce()
    local attempted, applied, deferred = 0, 0, 0
    for _, site in ipairs(Sites.ListSites()) do
        -- DORMANT sites retain logical demand, but the executor itself fails closed when
        -- the building/containers are unloaded. No virtual inventory is decremented.
        if site.state == "ACTIVE" or site.state == "DORMANT" then
            local a, p, d = processSite(site)
            attempted = attempted + a
            applied = applied + p
            deferred = deferred + d
        end
    end
    return true, attempted, applied, deferred
end

local function onServerStarted()
    local ok, attempted, applied, deferred = Service.RunOnce()
    log("initial pass ok=" .. tostring(ok)
        .. " attempted=" .. tostring(attempted)
        .. " applied=" .. tostring(applied)
        .. " deferred=" .. tostring(deferred))
end

if isServer and isServer() then
    if Events.OnServerStarted then Events.OnServerStarted.Add(onServerStarted) end
    if Events.EveryOneMinute then Events.EveryOneMinute.Add(Service.RunOnce) end
end

Service.lastOutcome = lastOutcome
LCCQF.FactionSiteConsumptionExecutorService = Service
return Service
