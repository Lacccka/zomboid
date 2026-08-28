-- Promotes reserved faction sites into the materialization-ready VALIDATING state only
-- after a bounded live-world resource scan. This service is server authority only.
if isClient and isClient() and not (isServer and isServer()) then
    return {}
end

require "LCCQF/LCCQFConstants"
require "LCCQF/Content/LCCQFFactionDefinitions"
require "LCCQF/FactionWorld/LCCQFFactionSiteRegistry"
require "LCCQF/FactionWorld/LCCQFFactionSiteCandidateIndex"
require "LCCQF/FactionWorld/LCCQFFactionSiteResourceScanner"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local FactionRegistry = LCCQF.FactionRegistry
local Sites = LCCQF.FactionSiteRegistry
local Candidates = LCCQF.FactionSiteCandidateIndex
local Scanner = LCCQF.FactionSiteResourceScanner
local Service = LCCQF.FactionSiteValidationService or {}
local lastOutcomeBySite = Service.lastOutcomeBySite or {}

local function log(message)
    print(C.LOG_PREFIX .. "[FACTION:SITE:VALIDATION] " .. tostring(message))
end

local function countSummary(result)
    local counts = result and result.counts or {}
    return "beds=" .. tostring(counts.beds or 0)
        .. " water=" .. tostring(counts.water or 0)
        .. " storage=" .. tostring(counts.storage or 0)
        .. " food=" .. tostring(counts.food or 0)
        .. " freeSpawn=" .. tostring(counts.freeSpawnPoints or 0)
end

local function setDerived(site, result)
    -- `site` is the table owned by the server GlobalModData store. Keep the derived
    -- payload plain-data-only so it survives restart and can be inspected while the
    -- physical building is unloaded.
    site.derived = {
        schemaVersion = result.schemaVersion,
        scannedWorldHours = result.scannedWorldHours,
        tilesVisited = result.tilesVisited,
        tileBudget = result.tileBudget,
        complete = result.complete,
        safeHouseOverlap = result.safeHouseOverlap,
        counts = result.counts,
        points = result.points,
    }
end

local function logOutcome(site, outcome, detail, result)
    local signature = table.concat({
        tostring(outcome),
        tostring(detail or "none"),
        result and countSummary(result) or "no-scan",
    }, "|")
    if lastOutcomeBySite[site.siteId] == signature then return end
    lastOutcomeBySite[site.siteId] = signature

    log("siteId=" .. tostring(site.siteId)
        .. " factionId=" .. tostring(site.factionId)
        .. " state=" .. tostring(site.state)
        .. " outcome=" .. tostring(outcome)
        .. " detail=" .. tostring(detail or "none")
        .. " " .. (result and countSummary(result) or "scan=unavailable"))
end

local function validateReservedSite(site)
    local definition = FactionRegistry.Get(site.factionId)
    if not definition then
        local reason = "site references unknown faction"
        logOutcome(site, "REJECT", reason)
        return Sites.Transition(site.siteId, "ABANDONED", reason)
    end

    local result, scanError = Scanner.Scan(site)
    if not result then
        logOutcome(site, "DEFER", scanError or "resource scan unavailable")
        return false, "deferred"
    end

    setDerived(site, result)
    local accepted, reason = Scanner.Evaluate(definition, result)
    if not accepted then
        reason = tostring(reason or "resource requirements failed")
        Candidates.NoteRejection(site.factionId, site.candidateKey, reason)
        logOutcome(site, "REJECT", reason, result)
        return Sites.Transition(site.siteId, "ABANDONED", reason)
    end

    logOutcome(site, "PASS", "resource scan passed", result)
    return Sites.Transition(site.siteId, "VALIDATING", "resource scan passed")
end

function Service.RunOnce()
    local changed = 0
    local deferred = 0
    for _, site in ipairs(Sites.ListSites()) do
        if site.state == "RESERVED" then
            local ok, result = validateReservedSite(site)
            if ok then
                changed = changed + 1
            elseif result == "deferred" then
                deferred = deferred + 1
            end
        end
    end
    return true, changed, deferred
end

local function onServerStarted()
    local ok, changed, deferred = Service.RunOnce()
    log("initial pass ok=" .. tostring(ok)
        .. " changed=" .. tostring(changed or 0)
        .. " deferred=" .. tostring(deferred or 0))
end

local function onEveryOneMinute()
    local ok, changed, deferred = Service.RunOnce()
    if not ok then
        log("validation pass failed")
    elseif (changed or 0) > 0 or (deferred or 0) > 0 then
        log("pass changed=" .. tostring(changed or 0)
            .. " deferred=" .. tostring(deferred or 0))
    end
end

if isServer and isServer() then
    if Events.OnServerStarted then Events.OnServerStarted.Add(onServerStarted) end
    if Events.EveryOneMinute then Events.EveryOneMinute.Add(onEveryOneMinute) end
end

Service.lastOutcomeBySite = lastOutcomeBySite
LCCQF.FactionSiteValidationService = Service
return Service
