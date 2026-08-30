-- Provider-neutral projection coordinator. Logical assignments are already persisted by
-- FactionSiteOperations; this service asks the selected physical adapter to mirror them.
if isClient and isClient() and not (isServer and isServer()) then
    return {}
end

require "LCCQF/LCCQFConstants"
require "LCCQF/Content/LCCQFFactionDefinitions"
require "LCCQF/FactionWorld/LCCQFFactionSiteRegistry"
require "LCCQF/FactionWorld/LCCQFFactionSiteMaterializerRegistry"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Factions = LCCQF.FactionRegistry
local Sites = LCCQF.FactionSiteRegistry
local Materializers = LCCQF.FactionSiteMaterializerRegistry
local Service = LCCQF.FactionSiteOperationsProjectionService or {}

local function log(message)
    print(C.LOG_PREFIX .. "[FACTION:SITE:OPERATIONS:PROJECTION] " .. tostring(message))
end

local function processSite(site)
    local definition = Factions.Get(site.factionId)
    local profile = definition and definition.populationProfile
    if type(profile) ~= "table" or profile.enabled == false then return false, "population disabled" end
    local adapter = Materializers.Get(profile.materializer)
    if not adapter or type(adapter.ApplyOperations) ~= "function" then
        return false, "operations projection unavailable"
    end
    return adapter.ApplyOperations({ site = site, definition = definition })
end

function Service.RunOnce()
    local processed = 0
    local changed = 0
    for _, site in ipairs(Sites.ListSites()) do
        if site.state == "VALIDATING" or site.state == "ACTIVE" or site.state == "DORMANT" then
            local ok, result = processSite(site)
            if ok then
                processed = processed + 1
                changed = changed + math.max(0, tonumber(result and result.changed) or 0)
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

LCCQF.FactionSiteOperationsProjectionService = Service
return Service
