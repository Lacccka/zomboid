-- Periodic read-only stock reconciliation for autonomous faction settlements.
if isClient and isClient() and not (isServer and isServer()) then return {} end

require "LCCQF/LCCQFConstants"
require "LCCQF/FactionWorld/LCCQFFactionSiteRegistry"
require "LCCQF/FactionWorld/LCCQFFactionSiteStock"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Sites = LCCQF.FactionSiteRegistry
local Stock = LCCQF.FactionSiteStock
local Service = LCCQF.FactionSiteStockService or {}
local lastAttemptHours = Service.lastAttemptHours or {}
local lastOutcome = Service.lastOutcome or {}

local function log(message)
    print(C.LOG_PREFIX .. "[FACTION:SITE:STOCK:SERVICE] " .. tostring(message))
end

local function worldHours()
    if not getGameTime then return 0 end
    local gameTime = getGameTime()
    return gameTime and gameTime.getWorldAgeHours and (tonumber(gameTime:getWorldAgeHours()) or 0) or 0
end

local function shouldProcess(site, nowHours, force)
    if force then return true end
    local previous = tonumber(lastAttemptHours[site.siteId])
    if previous == nil and type(site.stock) == "table" then
        previous = tonumber(site.stock.verifiedWorldHours)
    end
    if previous == nil then return true end
    local interval = math.max(1 / 60, tonumber(C.FACTION_SITE_STOCK_REFRESH_WORLD_HOURS) or (1 / 6))
    return nowHours - previous >= interval
end

local function logOutcome(site, outcome)
    if lastOutcome[site.siteId] == outcome then return end
    lastOutcome[site.siteId] = outcome
    log("siteId=" .. tostring(site.siteId) .. " " .. outcome)
end

local function processSite(site, nowHours, force)
    if not shouldProcess(site, nowHours, force) then return false, "throttled" end
    lastAttemptHours[site.siteId] = nowHours

    local ok, changedOrError = Stock.Refresh(site)
    if not ok then
        logOutcome(site, "outcome=DEFER detail=" .. tostring(changedOrError))
        return false, changedOrError
    end

    local stock = site.stock or {}
    logOutcome(site, "outcome=PASS revision=" .. tostring(stock.revision or 0)
        .. " containers=" .. tostring(stock.containerCount or 0)
        .. " items=" .. tostring(stock.itemCount or 0)
        .. " changed=" .. tostring(changedOrError == true))
    return true, changedOrError == true
end

function Service.RunOnce(force)
    local nowHours = worldHours()
    local processed, changed, deferred = 0, 0, 0
    for _, site in ipairs(Sites.ListSites()) do
        if site.state == "ACTIVE" or site.state == "DORMANT" then
            local ok, result = processSite(site, nowHours, force == true)
            if ok then
                processed = processed + 1
                if result == true then changed = changed + 1 end
            elseif result ~= "throttled" then
                deferred = deferred + 1
            end
        end
    end
    return true, processed, changed, deferred
end

local function onServerStarted()
    local ok, processed, changed, deferred = Service.RunOnce(true)
    log("initial pass ok=" .. tostring(ok)
        .. " processed=" .. tostring(processed)
        .. " changed=" .. tostring(changed)
        .. " deferred=" .. tostring(deferred))
end

local function onEveryOneMinute()
    Service.RunOnce(false)
end

if isServer and isServer() then
    if Events.OnServerStarted then Events.OnServerStarted.Add(onServerStarted) end
    if Events.EveryOneMinute then Events.EveryOneMinute.Add(onEveryOneMinute) end
end

Service.lastAttemptHours = lastAttemptHours
Service.lastOutcome = lastOutcome
LCCQF.FactionSiteStockService = Service
return Service
