-- Server-authoritative coordinator between logical faction sites/population and a
-- registered physical runtime adapter. This module is provider-neutral.
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
local Service = LCCQF.FactionSiteMaterializationService or {}
local lastOutcome = Service.lastOutcome or {}

local function log(message)
    print(C.LOG_PREFIX .. "[FACTION:SITE:MATERIALIZATION] " .. tostring(message))
end

local function logOnce(site, outcome, detail)
    local signature = tostring(outcome) .. "|" .. tostring(detail or "none")
        .. "|" .. tostring(Population.CountMaterialized(site))
    if lastOutcome[site.siteId] == signature then return end
    lastOutcome[site.siteId] = signature
    log("siteId=" .. tostring(site.siteId)
        .. " factionId=" .. tostring(site.factionId)
        .. " state=" .. tostring(site.state)
        .. " outcome=" .. tostring(outcome)
        .. " materialized=" .. tostring(Population.CountMaterialized(site))
        .. " detail=" .. tostring(detail or "none"))
end

local function formatSafetyDetail(detail, metadata)
    local result = tostring(detail or "none")
    if type(metadata) ~= "table" then return result end

    local playerDistance = tonumber(metadata.playerDistance)
    local minimumDistance = tonumber(metadata.minimumDistance)
    if playerDistance ~= nil then
        result = result .. " playerDistance=" .. string.format("%.2f", playerDistance)
    end
    if minimumDistance ~= nil then
        result = result .. " minimumDistance=" .. string.format("%.2f", minimumDistance)
    end
    return result
end

local function needsIdentityPreservingMaterialization(site)
    for _, member in ipairs(Population.GetUnmaterialized(site)) do
        if member.providerId ~= nil or member.runtimeId ~= nil or member.previousRuntimeId ~= nil then
            return true
        end
    end
    return false
end

local function invokeMaterializer(adapter, context)
    local preserveIdentity = needsIdentityPreservingMaterialization(context.site)
    if preserveIdentity then
        if type(adapter.Rematerialize) ~= "function" then
            return false, "identity-preserving rematerializer unavailable", "identity-preserving"
        end
        context.reason = context.reason or "identity-preserving-materialization"
        local ok, result = adapter.Rematerialize(context)
        return ok, result, "identity-preserving"
    end

    if type(adapter.Materialize) ~= "function" then
        return false, "initial materializer unavailable", "initial"
    end
    local ok, result = adapter.Materialize(context)
    return ok, result, "initial"
end

local function processSite(site)
    local definition = Factions.Get(site.factionId)
    if not definition then
        logOnce(site, "REJECT", "unknown faction")
        return Sites.Transition(site.siteId, "ABANDONED", "materialization references unknown faction")
    end

    local profile = definition.populationProfile
    if type(profile) ~= "table" or profile.enabled == false then
        logOnce(site, "DEFER", "population disabled")
        return false, "population disabled"
    end

    local plan, planError = Population.EnsurePlan(site, profile)
    if not plan then
        logOnce(site, "REJECT", planError)
        return Sites.Transition(site.siteId, "ABANDONED", planError)
    end
    if Population.IsInitialPopulationMaterialized(site) then
        logOnce(site, "PASS", "initial population already materialized")
        return Sites.Transition(site.siteId, "ACTIVE", "initial faction population materialized")
    end

    local safetyOutcome, safetyDetail, safetyMetadata = Safety.ValidateMaterializationSite(definition, site)
    if safetyOutcome == "DEFER" then
        logOnce(site, "DEFER", formatSafetyDetail(safetyDetail, safetyMetadata))
        return false, "deferred"
    elseif safetyOutcome ~= "PASS" then
        logOnce(site, "REJECT", formatSafetyDetail(safetyDetail, safetyMetadata))
        return Sites.Transition(site.siteId, "ABANDONED", safetyDetail)
    end
    local spawnPoints = safetyDetail

    local adapter = Materializers.Get(profile.materializer)
    if not adapter then
        logOnce(site, "DEFER", "materializer unavailable: " .. tostring(profile.materializer))
        return false, "deferred"
    end

    local ok, result, mode = invokeMaterializer(adapter, {
        site = site,
        definition = definition,
        population = plan,
        spawnPoints = spawnPoints,
    })
    if not ok then
        logOnce(site, "DEFER", tostring(mode) .. ": " .. tostring(result))
        return false, "deferred"
    end

    if Population.IsInitialPopulationMaterialized(site) then
        logOnce(site, "PASS", tostring(mode) .. " population materialized")
        return Sites.Transition(site.siteId, "ACTIVE", "initial faction population materialized")
    end

    logOnce(site, "PARTIAL", tostring(mode) .. " provider bound " .. tostring(result or 0) .. " member(s)")
    return false, "partial"
end

function Service.RunOnce()
    local activated = 0
    local deferred = 0
    for _, site in ipairs(Sites.ListSites()) do
        if site.state == "VALIDATING" then
            local ok, result = processSite(site)
            if ok then
                activated = activated + 1
            elseif result == "deferred" or result == "partial" then
                deferred = deferred + 1
            end
        end
    end
    return true, activated, deferred
end

local function onServerStarted()
    local ok, activated, deferred = Service.RunOnce()
    log("initial pass ok=" .. tostring(ok)
        .. " activated=" .. tostring(activated or 0)
        .. " deferred=" .. tostring(deferred or 0))
end

local function onEveryOneMinute()
    local ok, activated = Service.RunOnce()
    if not ok then
        log("materialization pass failed")
    elseif (activated or 0) > 0 then
        log("pass activated=" .. tostring(activated or 0))
    end
end

if isServer and isServer() then
    if Events.OnServerStarted then Events.OnServerStarted.Add(onServerStarted) end
    if Events.EveryOneMinute then Events.EveryOneMinute.Add(onEveryOneMinute) end
end

Service.lastOutcome = lastOutcome
LCCQF.FactionSiteMaterializationService = Service
return Service
