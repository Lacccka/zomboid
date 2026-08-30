-- Bandits-only physical projection into the common interactive NPC runtime. This scanner
-- is bounded to one faction site and never discovers/owns logical population itself.
if isClient and isClient() and not (isServer and isServer()) then return {} end

require "BanditBrain"
require "LCCQF/LCCQFConstants"
require "LCCQF/Content/LCCQFFactionDefinitions"
require "LCCQF/FactionWorld/LCCQFFactionSiteRegistry"
require "LCCQF/FactionWorld/LCCQFFactionSitePopulation"
require "LCCQF/FactionWorld/LCCQFFactionNPCBridge"

LCCQF = LCCQF or {}
local C = LCCQF.Constants
local Factions = LCCQF.FactionRegistry
local Sites = LCCQF.FactionSiteRegistry
local Population = LCCQF.FactionSitePopulation
local Bridge = LCCQF.FactionNPCBridge
local Projection = LCCQF.BanditsFactionNPCProjection or {}
local lastLog = Projection.lastLog or {}

local function log(message)
    print(C.LOG_PREFIX .. "[RUNTIME:BANDITS:FACTION:NPC] " .. tostring(message))
end

local function currentByNpcId(site)
    local out = {}
    for _, member in ipairs(Population.ListMembers(site)) do out[member.npcId] = member end
    return out
end

local function scanSite(site)
    if type(site.bounds) ~= "table" then return 0, false end
    local cell = getCell and getCell() or nil
    if not cell then return 0, false end
    local faction = Factions.Get(site.factionId)
    local profile = faction and faction.populationProfile or {}
    local padding = math.max(0, math.floor(tonumber(profile.returnRadius) or 24))
    local x1 = math.floor(tonumber(site.bounds.x) or 0) - padding
    local y1 = math.floor(tonumber(site.bounds.y) or 0) - padding
    local x2 = math.floor(tonumber(site.bounds.x2)
        or ((tonumber(site.bounds.x) or 0) + (tonumber(site.bounds.w) or 1) - 1)) + padding
    local y2 = math.floor(tonumber(site.bounds.y2)
        or ((tonumber(site.bounds.y) or 0) + (tonumber(site.bounds.h) or 1) - 1)) + padding

    local zLevels = {}
    local points = site.derived and site.derived.points and site.derived.points.spawn
    for _, point in ipairs(type(points) == "table" and points or {}) do
        zLevels[math.floor(tonumber(point.z) or 0)] = true
    end
    if next(zLevels) == nil then zLevels[0] = true end

    local members = currentByNpcId(site)
    local scanned = 0
    local limit = math.max(1, math.floor(tonumber(C.FACTION_SITE_RUNTIME_SCAN_MAX_TILES) or 2048))
    for z in pairs(zLevels) do
        for y = y1, y2 do
            for x = x1, x2 do
                if scanned >= limit then return scanned, false end
                scanned = scanned + 1
                local square = cell:getGridSquare(x, y, z)
                if square then
                    local moving = square:getMovingObjects()
                    if moving then
                        for index = 0, moving:size() - 1 do
                            local zombie = moving:get(index)
                            if zombie and instanceof(zombie, "IsoZombie") and not zombie:isDead() then
                                local brain = BanditBrain.Get(zombie)
                                local npcId = brain and brain.lccqNpcId or nil
                                local member = npcId and members[npcId] or nil
                                if member and member.state == "MATERIALIZED"
                                    and brain.lccqProvider == "Bandits"
                                    and brain.lccqRetired ~= true
                                    and brain.lccqSiteId == site.siteId
                                    and tostring(brain.id) == tostring(member.runtimeId)
                                then
                                    Bridge.BindPhysical(site, member, {
                                        runtimeId = brain.id,
                                        x = zombie:getX(), y = zombie:getY(), z = zombie:getZ(),
                                    })
                                end
                            end
                        end
                    end
                end
            end
        end
    end

    for _, member in pairs(members) do
        if member.state ~= "MATERIALIZED" then
            Bridge.UnbindPhysical(member, "faction-member-" .. string.lower(tostring(member.state)))
        end
    end
    return scanned, true
end

function Projection.RunOnce()
    local sitesProcessed, tilesScanned = 0, 0
    for _, site in ipairs(Sites.ListSites()) do
        if site.population and (site.state == "VALIDATING" or site.state == "ACTIVE" or site.state == "DORMANT") then
            Bridge.EnsureSiteDefinitions(site)
            local scanned = scanSite(site)
            sitesProcessed = sitesProcessed + 1
            tilesScanned = tilesScanned + math.max(0, tonumber(scanned) or 0)
        end
    end
    local signature = tostring(sitesProcessed) .. "|" .. tostring(tilesScanned)
    if lastLog.summary ~= signature then
        lastLog.summary = signature
        log("pass sites=" .. tostring(sitesProcessed) .. " tiles=" .. tostring(tilesScanned))
    end
    return true, sitesProcessed, tilesScanned
end

if isServer and isServer() then
    if Events.OnServerStarted then Events.OnServerStarted.Add(Projection.RunOnce) end
    if Events.EveryOneMinute then Events.EveryOneMinute.Add(Projection.RunOnce) end
end

Projection.lastLog = lastLog
LCCQF.BanditsFactionNPCProjection = Projection
return Projection
