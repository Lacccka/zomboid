-- Periodic health validation for active faction sites. Only naturally loaded geometry
-- is inspected; unloaded sites are deferred and never treated as failures.
if isClient and isClient() and not (isServer and isServer()) then
    return {}
end

require "LCCQF/LCCQFConstants"
require "LCCQF/Content/LCCQFFactionDefinitions"
require "LCCQF/FactionWorld/LCCQFFactionSiteRegistry"
require "LCCQF/FactionWorld/LCCQFFactionSiteCandidateIndex"
require "LCCQF/FactionWorld/LCCQFFactionSiteResourceScanner"
require "LCCQF/FactionWorld/zz_LCCQFFactionSiteRelocationService"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Factions = LCCQF.FactionRegistry
local Sites = LCCQF.FactionSiteRegistry
local Candidates = LCCQF.FactionSiteCandidateIndex
local Scanner = LCCQF.FactionSiteResourceScanner
local Relocation = LCCQF.FactionSiteRelocationService
local Service = LCCQF.FactionSiteHealthService or {}

local CHECK_INTERVAL_HOURS = 6
local UNLOADED_RETRY_HOURS = 1
local RESOURCE_FAILURE_THRESHOLD = 2
local lastLog = Service.lastLog or {}

local function log(message)
    print(C.LOG_PREFIX .. "[FACTION:SITE:HEALTH] " .. tostring(message))
end

local function worldHours()
    if not getGameTime then return 0 end
    local gameTime = getGameTime()
    return gameTime and gameTime.getWorldAgeHours and (tonumber(gameTime:getWorldAgeHours()) or 0) or 0
end

local function healthFor(site)
    if type(site.health) ~= "table" then
        site.health = {
            schemaVersion = 1,
            consecutiveResourceFailures = 0,
        }
    end
    return site.health
end

local function setDerived(site, result)
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

local function logOnce(site, outcome, detail)
    local signature = tostring(outcome) .. "|" .. tostring(detail or "none")
    if lastLog[site.siteId] == signature then return end
    lastLog[site.siteId] = signature
    log("siteId=" .. tostring(site.siteId)
        .. " factionId=" .. tostring(site.factionId)
        .. " state=" .. tostring(site.state)
        .. " outcome=" .. tostring(outcome)
        .. " detail=" .. tostring(detail or "none"))
end

local function requestRelocation(site, reason, health)
    Candidates.NoteRejection(site.factionId, site.candidateKey, reason)
    health.relocationRequestedWorldHours = worldHours()
    health.relocationReason = tostring(reason)
    Sites.MarkDirty(site.siteId, "site health requested relocation")
    local ok, result = Relocation.Request(site.siteId, reason)
    if ok then
        logOnce(site, "RELOCATING", reason)
        return true, "relocating"
    end
    logOnce(site, "DEFER", "relocation request failed: " .. tostring(result))
    return false, "deferred"
end

local function checkSite(site, now)
    local definition = Factions.Get(site.factionId)
    if not definition then return false, "unknown faction" end
    local health = healthFor(site)

    local lastAttempt = tonumber(health.lastAttemptWorldHours)
    local lastCompleted = tonumber(health.lastCompletedWorldHours)
    if lastCompleted and now - lastCompleted < CHECK_INTERVAL_HOURS then
        return false, "not-due"
    end
    if lastAttempt and now - lastAttempt < UNLOADED_RETRY_HOURS then
        return false, "not-due"
    end

    health.lastAttemptWorldHours = now
    local result, scanError = Scanner.Scan(site)
    if not result then
        health.lastOutcome = "DEFER"
        health.lastDetail = tostring(scanError or "site geometry unavailable")
        Sites.MarkDirty(site.siteId, "site health deferred")
        logOnce(site, "DEFER", health.lastDetail)
        return false, "deferred"
    end

    setDerived(site, result)
    health.lastCompletedWorldHours = now
    health.lastTilesVisited = math.max(0, tonumber(result.tilesVisited) or 0)
    health.lastSafeHouseOverlap = result.safeHouseOverlap == true

    local accepted, reason = Scanner.Evaluate(definition, result)
    if accepted then
        health.consecutiveResourceFailures = 0
        health.lastHealthyWorldHours = now
        health.lastOutcome = "PASS"
        health.lastDetail = "resource validation passed"
        Sites.MarkDirty(site.siteId, "site health passed")
        logOnce(site, "PASS", health.lastDetail)
        return true, "healthy"
    end

    reason = tostring(reason or "resource requirements failed")
    health.lastOutcome = "FAIL"
    health.lastDetail = reason

    if result.safeHouseOverlap == true then
        health.consecutiveResourceFailures = math.max(
            RESOURCE_FAILURE_THRESHOLD,
            math.floor(tonumber(health.consecutiveResourceFailures) or 0)
        )
        return requestRelocation(site, "player SafeHouse overlaps active faction site", health)
    end

    -- A bounded scan exhausting its budget does not prove the site became unsuitable.
    if result.complete == false then
        health.lastOutcome = "DEFER"
        Sites.MarkDirty(site.siteId, "site health scan incomplete")
        logOnce(site, "DEFER", reason)
        return false, "deferred"
    end

    health.consecutiveResourceFailures = math.max(
        0,
        math.floor(tonumber(health.consecutiveResourceFailures) or 0)
    ) + 1
    health.lastFailureWorldHours = now
    Sites.MarkDirty(site.siteId, "site health resource failure")

    if health.consecutiveResourceFailures >= RESOURCE_FAILURE_THRESHOLD then
        return requestRelocation(site, reason, health)
    end

    logOnce(site, "WARN", reason .. " failures=" .. tostring(health.consecutiveResourceFailures)
        .. "/" .. tostring(RESOURCE_FAILURE_THRESHOLD))
    return false, "warning"
end

function Service.RunOnce()
    local now = worldHours()
    local checked = 0
    local relocating = 0
    local deferred = 0
    for _, site in ipairs(Sites.ListSites()) do
        if site.state == "ACTIVE" or site.state == "DORMANT" then
            local ok, outcome = checkSite(site, now)
            if ok then checked = checked + 1 end
            if outcome == "relocating" then relocating = relocating + 1 end
            if outcome == "deferred" then deferred = deferred + 1 end
        end
    end
    return true, checked, relocating, deferred
end

local function onServerStarted()
    local ok, checked, relocating, deferred = Service.RunOnce()
    log("initial pass ok=" .. tostring(ok)
        .. " checked=" .. tostring(checked or 0)
        .. " relocating=" .. tostring(relocating or 0)
        .. " deferred=" .. tostring(deferred or 0))
end

local function onEveryOneMinute()
    Service.RunOnce()
end

if isServer and isServer() then
    if Events.OnServerStarted then Events.OnServerStarted.Add(onServerStarted) end
    if Events.EveryOneMinute then Events.EveryOneMinute.Add(onEveryOneMinute) end
end

Service.lastLog = lastLog
LCCQF.FactionSiteHealthService = Service
return Service
