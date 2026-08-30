-- Server-owned faction population maintenance. Death never resurrects an existing
-- logical npcId: replacement planning appends a new stable identity after a delay.
if isClient and isClient() and not (isServer and isServer()) then
    return {}
end

require "LCCQF/LCCQFConstants"
require "LCCQF/Content/LCCQFFactionDefinitions"
require "LCCQF/FactionWorld/LCCQFFactionSiteRegistry"
require "LCCQF/FactionWorld/LCCQFFactionSitePopulation"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Factions = LCCQF.FactionRegistry
local Sites = LCCQF.FactionSiteRegistry
local Population = LCCQF.FactionSitePopulation
local Service = LCCQF.FactionSitePopulationMaintenance or {}

local function log(message)
    print(C.LOG_PREFIX .. "[FACTION:SITE:POPULATION] " .. tostring(message))
end

local function worldHours()
    if not getGameTime then return 0 end
    local gameTime = getGameTime()
    return gameTime and gameTime.getWorldAgeHours and (tonumber(gameTime:getWorldAgeHours()) or 0) or 0
end

local function maintainSite(site)
    local definition = Factions.Get(site.factionId)
    local profile = definition and definition.populationProfile
    if type(profile) ~= "table" or profile.enabled == false or profile.replaceDead ~= true then
        return true, 0
    end

    local plan, planError = Population.EnsurePlan(site, profile)
    if not plan then return false, planError end
    local target = math.max(1, math.floor(tonumber(plan.targetPopulation) or 1))
    if #Population.ListMembers(site) >= target then return true, 0 end

    local delayHours = math.max(0, tonumber(profile.replacementDelayHours) or 0)
    local now = worldHours()
    local created = 0
    for _, member in ipairs(Population.ListAllMembers(site)) do
        if #Population.ListMembers(site) >= target then break end
        if member.state == "DEAD" and not member.replacementNpcId then
            local diedAt = tonumber(member.deadWorldHours) or now
            if now - diedAt >= delayHours then
                local ok, replacement = Population.AppendReplacement(site, member.npcId)
                if ok and replacement then
                    created = created + 1
                    log("planned replacement siteId=" .. tostring(site.siteId)
                        .. " deadNpcId=" .. tostring(member.npcId)
                        .. " npcId=" .. tostring(replacement.npcId)
                        .. " roleId=" .. tostring(replacement.roleId)
                        .. " delayHours=" .. tostring(delayHours))
                end
            end
        end
    end
    return true, created
end

function Service.RunOnce()
    local created = 0
    local failed = 0
    for _, site in ipairs(Sites.ListSites()) do
        if site.state == "ACTIVE" or site.state == "DORMANT" then
            local ok, result = maintainSite(site)
            if ok then created = created + (tonumber(result) or 0) else failed = failed + 1 end
        end
    end
    return true, created, failed
end

local function onServerStarted()
    local ok, created, failed = Service.RunOnce()
    log("initial pass ok=" .. tostring(ok)
        .. " replacements=" .. tostring(created or 0)
        .. " failed=" .. tostring(failed or 0))
end

local function onEveryOneMinute()
    Service.RunOnce()
end

if isServer and isServer() then
    if Events.OnServerStarted then Events.OnServerStarted.Add(onServerStarted) end
    if Events.EveryOneMinute then Events.EveryOneMinute.Add(onEveryOneMinute) end
end

LCCQF.FactionSitePopulationMaintenance = Service
return Service
