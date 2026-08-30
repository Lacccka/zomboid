-- Server-owned relocation coordinator. A relocation keeps logical NPC identity and
-- waits for a replacement site to validate/materialize before abandoning the old site.
if isClient and isClient() and not (isServer and isServer()) then
    return {}
end

require "LCCQF/LCCQFConstants"
require "LCCQF/FactionWorld/LCCQFFactionSiteRegistry"
require "LCCQF/FactionWorld/LCCQFFactionSitePopulation"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Sites = LCCQF.FactionSiteRegistry
local Population = LCCQF.FactionSitePopulation
local Service = LCCQF.FactionSiteRelocationService or {}
local lastSignature = Service.lastSignature or {}

local function log(message)
    print(C.LOG_PREFIX .. "[FACTION:SITE:RELOCATION] " .. tostring(message))
end

function Service.Request(siteId, reason)
    local ok, result = Sites.BeginRelocation(siteId, reason)
    if ok then
        log("requested siteId=" .. tostring(siteId)
            .. " reason=" .. tostring(reason or "server relocation request"))
    end
    return ok, result
end

local function logOnce(site, message)
    local signature = tostring(site.state) .. "|" .. tostring(site.replacementSiteId or "none") .. "|" .. tostring(message)
    if lastSignature[site.siteId] == signature then return end
    lastSignature[site.siteId] = signature
    log("siteId=" .. tostring(site.siteId) .. " " .. tostring(message))
end

local function rememberPreviousRuntimes(site)
    for _, member in ipairs(Population.ListAllMembers(site)) do
        if member.state ~= "DEAD" and member.runtimeId ~= nil and member.previousRuntimeId == nil then
            member.previousRuntimeId = tostring(member.runtimeId)
        end
    end
end

local function processRelocatingSite(site)
    local replacementId = site.replacementSiteId
    if not replacementId then
        logOnce(site, "waiting for allocator replacement")
        return true
    end

    local replacement = Sites.GetSite(replacementId)
    if not replacement then
        site.replacementSiteId = nil
        Sites.MarkDirty(site.siteId, "missing relocation replacement cleared")
        logOnce(site, "replacement missing; allocator may retry")
        return true
    end

    if replacement.state == "ABANDONED" then
        site.replacementSiteId = nil
        Sites.MarkDirty(site.siteId, "failed relocation replacement cleared")
        logOnce(site, "replacement abandoned; allocator may retry")
        return true
    end

    if replacement.state == "VALIDATING" and type(site.population) == "table"
        and type(replacement.population) ~= "table"
    then
        rememberPreviousRuntimes(site)
        local ok, result = Population.TransferPlan(site, replacement, "faction site relocation")
        if ok then
            replacement.relocatesSiteId = site.siteId
            Sites.MarkDirty(replacement.siteId, "relocation population accepted")
            logOnce(site, "population transferred replacementSiteId=" .. tostring(replacement.siteId))
        else
            logOnce(site, "population transfer deferred reason=" .. tostring(result))
        end
        return true
    end

    if replacement.state == "ACTIVE" then
        local ok, result = Sites.Transition(site.siteId, "ABANDONED", "replacement faction site active")
        if ok then
            log("completed oldSiteId=" .. tostring(site.siteId)
                .. " replacementSiteId=" .. tostring(replacement.siteId))
        end
        return ok, result
    end

    logOnce(site, "waiting replacementSiteId=" .. tostring(replacement.siteId)
        .. " state=" .. tostring(replacement.state))
    return true
end

function Service.RunOnce()
    local processed = 0
    local failed = 0
    for _, site in ipairs(Sites.ListSites()) do
        if site.state == "RELOCATING" then
            local ok = processRelocatingSite(site)
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
LCCQF.FactionSiteRelocationService = Service
return Service
