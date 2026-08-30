-- Provider-neutral lifecycle reconciliation for persistent faction-site populations.
-- The runtime adapter reports provider state and exposes safe rematerialization; core
-- owns site state transitions. No gameplay client can invoke this service.
if isClient and isClient() and not (isServer and isServer()) then
    return {}
end

require "LCCQF/LCCQFConstants"
require "LCCQF/Content/LCCQFFactionDefinitions"
require "LCCQF/FactionWorld/LCCQFFactionSiteRegistry"
require "LCCQF/FactionWorld/LCCQFFactionSitePopulation"
require "LCCQF/FactionWorld/LCCQFFactionSiteSafetyValidator"
require "LCCQF/FactionWorld/LCCQFFactionSiteMaterializerRegistry"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Factions = LCCQF.FactionRegistry
local Sites = LCCQF.FactionSiteRegistry
local Population = LCCQF.FactionSitePopulation
local Safety = LCCQF.FactionSiteSafetyValidator
local Materializers = LCCQF.FactionSiteMaterializerRegistry
local Service = LCCQF.FactionSiteLifecycleService or {}
local lastSignature = Service.lastSignature or {}

local function log(message)
    print(C.LOG_PREFIX .. "[FACTION:SITE:LIFECYCLE] " .. tostring(message))
end

local function signatureFor(site, stats, action)
    return table.concat({
        tostring(site.state),
        tostring(stats and stats.materialized or 0),
        tostring(stats and stats.virtualized or 0),
        tostring(stats and stats.missing or 0),
        tostring(stats and stats.dead or 0),
        tostring(stats and stats.ambiguous or 0),
        tostring(stats and stats.retiredDuplicates or 0),
        tostring(action or "none"),
    }, "|")
end

local function logChanged(site, stats, action)
    local signature = signatureFor(site, stats, action)
    if lastSignature[site.siteId] == signature then return end
    lastSignature[site.siteId] = signature
    log("siteId=" .. tostring(site.siteId)
        .. " state=" .. tostring(site.state)
        .. " loaded=" .. tostring(stats and stats.loaded == true)
        .. " materialized=" .. tostring(stats and stats.materialized or Population.CountMaterialized(site))
        .. " virtualized=" .. tostring(stats and stats.virtualized or Population.CountVirtualized(site))
        .. " missing=" .. tostring(stats and stats.missing or 0)
        .. " dead=" .. tostring(stats and stats.dead or 0)
        .. " ambiguous=" .. tostring(stats and stats.ambiguous or 0)
        .. " retiredDuplicates=" .. tostring(stats and stats.retiredDuplicates or 0)
        .. " action=" .. tostring(action or "none"))
end

local function tryRematerialize(site, definition, adapter, stats)
    if not stats or stats.loaded ~= true then return false, "site not loaded" end
    if #Population.GetUnmaterialized(site) == 0 then return false, "nothing to rematerialize" end
    if type(adapter.Rematerialize) ~= "function" then return false, "rematerializer unavailable" end

    local safetyOutcome, safetyDetail = Safety.ValidateMaterializationSite(definition, site)
    if safetyOutcome ~= "PASS" then return false, tostring(safetyDetail or safetyOutcome) end

    local ok, result = adapter.Rematerialize({
        site = site,
        definition = definition,
        population = Population.GetPlan(site),
        spawnPoints = safetyDetail,
        reason = "lifecycle-recovery",
    })
    if not ok then return false, tostring(result or "rematerializer failed") end
    return true, "rematerialized=" .. tostring(result or 0)
end

local function processSite(site)
    local definition = Factions.Get(site.factionId)
    if not definition then return false, "unknown faction" end
    local profile = definition.populationProfile
    if type(profile) ~= "table" or profile.enabled == false then return false, "population disabled" end

    local adapter = Materializers.Get(profile.materializer)
    if not adapter or type(adapter.Reconcile) ~= "function" then
        return false, "lifecycle adapter unavailable"
    end

    local plan, planError = Population.EnsurePlan(site, profile)
    if not plan then return false, planError end

    local ok, stats = adapter.Reconcile({
        site = site,
        definition = definition,
        population = plan,
    })
    if not ok then
        logChanged(site, nil, "reconcile-failed:" .. tostring(stats))
        return false, stats
    end

    local action = "reconciled"
    if site.state == "VALIDATING" and Population.IsInitialPopulationMaterialized(site) then
        Sites.Transition(site.siteId, "ACTIVE", "recovered initial faction population")
        action = "activate-recovered"
    elseif site.state == "ACTIVE" and Population.CountMaterialized(site) == 0
        and #Population.ListMembers(site) > 0
    then
        Sites.Transition(site.siteId, "DORMANT", "physical faction population unloaded")
        action = "dormant"
    elseif site.state == "DORMANT" and Population.CountMaterialized(site) > 0 then
        Sites.Transition(site.siteId, "ACTIVE", "physical faction population returned")
        action = "reactivate"
    end

    if site.state == "ACTIVE" or site.state == "DORMANT" then
        local rematerialized, detail = tryRematerialize(site, definition, adapter, stats)
        if rematerialized then
            action = "recover-" .. tostring(detail)
            if site.state == "DORMANT" and Population.CountMaterialized(site) > 0 then
                Sites.Transition(site.siteId, "ACTIVE", "faction population rematerialized")
            end
        elseif detail ~= "site not loaded" and detail ~= "nothing to rematerialize" then
            action = "defer-" .. tostring(detail)
        end
    end

    logChanged(site, stats, action)
    return true, stats
end

function Service.RunOnce()
    local processed = 0
    local failed = 0
    for _, site in ipairs(Sites.ListSites()) do
        if site.state == "VALIDATING" or site.state == "ACTIVE" or site.state == "DORMANT" then
            local ok = processSite(site)
            if ok then processed = processed + 1 else failed = failed + 1 end
        end
    end
    return true, processed, failed
end

local function onServerStarted()
    local ok, processed, failed = Service.RunOnce()
    log("initial pass ok=" .. tostring(ok)
        .. " processed=" .. tostring(processed or 0)
        .. " failed=" .. tostring(failed or 0))
end

local function onEveryOneMinute()
    Service.RunOnce()
end

if isServer and isServer() then
    if Events.OnServerStarted then Events.OnServerStarted.Add(onServerStarted) end
    if Events.EveryOneMinute then Events.EveryOneMinute.Add(onEveryOneMinute) end
end

Service.lastSignature = lastSignature
LCCQF.FactionSiteLifecycleService = Service
return Service
