-- Periodic server coordinator for faction jobs/schedules and settlement capability needs.
if isClient and isClient() and not (isServer and isServer()) then
    return {}
end

require "LCCQF/LCCQFConstants"
require "LCCQF/Content/LCCQFFactionDefinitions"
require "LCCQF/FactionWorld/LCCQFFactionSiteRegistry"
require "LCCQF/FactionWorld/LCCQFFactionSiteOperations"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Factions = LCCQF.FactionRegistry
local Sites = LCCQF.FactionSiteRegistry
local Operations = LCCQF.FactionSiteOperations
local Service = LCCQF.FactionSiteOperationsService or {}
local lastSignature = Service.lastSignature or {}

local function log(message)
    print(C.LOG_PREFIX .. "[FACTION:SITE:OPERATIONS] " .. tostring(message))
end

local function signature(site)
    local operations = site.operations or {}
    local capabilities = operations.capabilities or {}
    local needs = operations.needs or {}
    return table.concat({
        tostring(operations.revision or 0),
        tostring(capabilities.livingPopulation or 0),
        tostring(needs.housingDeficit or 0),
        tostring(needs.waterSourceMissing == true),
        tostring(needs.storageMissing == true),
        tostring(needs.foodAccessMissing == true),
        tostring(#Operations.GetOpenSignals(site)),
    }, "|")
end

local function logChanged(site)
    local nextSignature = signature(site)
    if lastSignature[site.siteId] == nextSignature then return end
    lastSignature[site.siteId] = nextSignature
    local operations = site.operations or {}
    local capabilities = operations.capabilities or {}
    local needs = operations.needs or {}
    log("siteId=" .. tostring(site.siteId)
        .. " state=" .. tostring(site.state)
        .. " revision=" .. tostring(operations.revision or 0)
        .. " population=" .. tostring(capabilities.livingPopulation or 0)
        .. " beds=" .. tostring(capabilities.beds or 0)
        .. " water=" .. tostring(capabilities.waterSources or 0)
        .. " storage=" .. tostring(capabilities.storageContainers or 0)
        .. " foodAccess=" .. tostring(capabilities.foodAccessContainers or 0)
        .. " housingDeficit=" .. tostring(needs.housingDeficit or 0)
        .. " openSignals=" .. tostring(#Operations.GetOpenSignals(site)))
end

local function processSite(site)
    local definition = Factions.Get(site.factionId)
    if not definition then return false, "unknown faction" end
    if type(definition.operationsProfile) ~= "table" or definition.operationsProfile.enabled == false then
        return false, "operations disabled"
    end
    local ok, changedOrError = Operations.UpdateSite(site, definition)
    if not ok then
        log("siteId=" .. tostring(site.siteId) .. " outcome=DEFER detail=" .. tostring(changedOrError))
        return false, changedOrError
    end
    logChanged(site)
    return true, changedOrError == true
end

function Service.RunOnce()
    local processed = 0
    local changed = 0
    for _, site in ipairs(Sites.ListSites()) do
        if site.state == "VALIDATING" or site.state == "ACTIVE" or site.state == "DORMANT" then
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

local function onEveryOneMinute()
    Service.RunOnce()
end

if isServer and isServer() then
    if Events.OnServerStarted then Events.OnServerStarted.Add(onServerStarted) end
    if Events.EveryOneMinute then Events.EveryOneMinute.Add(onEveryOneMinute) end
end

Service.lastSignature = lastSignature
LCCQF.FactionSiteOperationsService = Service
return Service
