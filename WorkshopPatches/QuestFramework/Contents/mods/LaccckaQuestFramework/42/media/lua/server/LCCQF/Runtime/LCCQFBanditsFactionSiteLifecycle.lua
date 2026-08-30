-- Lifecycle extension for the existing Bandits faction-site materializer.
-- It owns physical reconciliation/rematerialization only; logical state stays in
-- FactionSitePopulation and provider-neutral services call these methods via adapter.
if isClient and isClient() and not (isServer and isServer()) then
    return {}
end

require "Bandit"
require "BanditBrain"
require "BanditCustom"
require "BanditUtils"
require "LCCQF/LCCQFConstants"
require "LCCQF/Runtime/LCCQFBanditsFactionGuardProgram"
require "LCCQF/FactionWorld/LCCQFFactionSitePopulation"
require "LCCQF/Runtime/LCCQFBanditsFactionProfiles"
require "LCCQF/Runtime/LCCQFBanditsFactionSiteMaterializer"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Population = LCCQF.FactionSitePopulation
local Profiles = LCCQF.BanditsFactionProfiles
local Adapter = LCCQF.BanditsFactionSiteMaterializer

local NPC_ID_FIELD = "lccqNpcId"
local FACTION_ID_FIELD = "lccqFactionId"
local SITE_ID_FIELD = "lccqSiteId"
local SQUAD_ID_FIELD = "lccqSquadId"
local ROLE_ID_FIELD = "lccqRoleId"
local PROVIDER_FIELD = "lccqProvider"
local RETIRED_FIELD = "lccqRetired"

local function log(message)
    print(C.LOG_PREFIX .. "[RUNTIME:BANDITS:FACTION:LIFECYCLE] " .. tostring(message))
end

local function eachBrain(visitor)
    if type(BanditClusters) ~= "table" then return false end
    local seen = {}
    for _, cluster in pairs(BanditClusters) do
        if type(cluster) == "table" then
            for _, brain in pairs(cluster) do
                if type(brain) == "table" and brain.id ~= nil then
                    local id = tostring(brain.id)
                    if not seen[id] then
                        seen[id] = true
                        visitor(brain)
                    end
                end
            end
        end
    end
    return true
end

local function ownedBrain(brain, siteId)
    return type(brain) == "table"
        and brain[PROVIDER_FIELD] == "Bandits"
        and (siteId == nil or brain[SITE_ID_FIELD] == siteId)
        and brain[RETIRED_FIELD] ~= true
end

local function brainByRuntimeId(runtimeId)
    if runtimeId == nil then return nil end
    local wanted = tostring(runtimeId)
    local found
    eachBrain(function(brain)
        if not found and tostring(brain.id) == wanted then found = brain end
    end)
    return found
end

local function collectBrains(siteId)
    local byNpcId = {}
    eachBrain(function(brain)
        if ownedBrain(brain, siteId) and type(brain[NPC_ID_FIELD]) == "string" then
            local npcId = brain[NPC_ID_FIELD]
            byNpcId[npcId] = byNpcId[npcId] or {}
            byNpcId[npcId][#byNpcId[npcId] + 1] = brain
        end
    end)
    return byNpcId
end

local function transmit(brain)
    if type(brain) ~= "table" or brain.id == nil then return end
    local gmd = GetBanditClusterData and GetBanditClusterData(brain.id) or nil
    if gmd then gmd[brain.id] = brain end
    if TransmitBanditCluster then TransmitBanditCluster(brain.id) end
end

local function setField(brain, key, value)
    if value == nil or brain[key] == value then return false end
    brain[key] = value
    return true
end

local function homePointFor(site, member)
    local points = site and site.derived and site.derived.points and site.derived.points.spawn
    if type(points) == "table" and #points > 0 then
        for index, current in ipairs(Population.ListMembers(site)) do
            if current.npcId == member.npcId then
                return points[((index - 1) % #points) + 1]
            end
        end
    end
    return site and site.anchor or nil
end

local function enforceOwnership(brain, site, plan, member, profile)
    if type(brain) ~= "table" or brain.id == nil then return false end
    local changed = false
    changed = setField(brain, NPC_ID_FIELD, member.npcId) or changed
    changed = setField(brain, FACTION_ID_FIELD, site.factionId) or changed
    changed = setField(brain, SITE_ID_FIELD, site.siteId) or changed
    changed = setField(brain, SQUAD_ID_FIELD, plan.squadId) or changed
    changed = setField(brain, ROLE_ID_FIELD, member.roleId) or changed
    changed = setField(brain, PROVIDER_FIELD, "Bandits") or changed

    if brain[RETIRED_FIELD] == true then
        brain[RETIRED_FIELD] = false
        changed = true
    end
    if brain.master ~= nil then
        brain.master = nil
        changed = true
    end
    if brain.key ~= nil then
        brain.key = nil
        changed = true
    end
    if brain.permanent ~= true then
        brain.permanent = true
        changed = true
    end
    if brain.hostile == true then
        brain.hostile = false
        changed = true
    end
    if brain.hostileP == true then
        brain.hostileP = false
        changed = true
    end

    local home = homePointFor(site, member)
    if type(home) == "table" then
        changed = setField(brain, "lccqHomeX", tonumber(home.x)) or changed
        changed = setField(brain, "lccqHomeY", tonumber(home.y)) or changed
        changed = setField(brain, "lccqHomeZ", tonumber(home.z)) or changed
        changed = setField(brain, "lccqHomeRadius", tonumber(profile.homeRadius) or 12) or changed
        changed = setField(brain, "lccqReturnRadius", tonumber(profile.returnRadius) or 24) or changed
        changed = setField(brain, "lccqGuardRadius", tonumber(profile.guardRadius) or 18) or changed
    end

    local programName = tostring(profile.program or "LCCQFFactionGuard")
    if type(brain.program) ~= "table" then
        brain.program = { name = programName, stage = "Prepare" }
        changed = true
    elseif brain.program.name ~= programName then
        brain.program.name = programName
        brain.program.stage = "Prepare"
        changed = true
    elseif type(brain.program.stage) ~= "string" or brain.program.stage == "" then
        brain.program.stage = "Prepare"
        changed = true
    end
    if brain.programFallback ~= programName then
        brain.programFallback = programName
        changed = true
    end

    if changed then transmit(brain) end
    return true
end

local function retireBrain(brain, reason)
    if not ownedBrain(brain, nil) then return false end
    local previousNpcId = brain[NPC_ID_FIELD]
    brain[RETIRED_FIELD] = true
    brain.lccqRetiredNpcId = previousNpcId
    brain.lccqRetiredReason = tostring(reason or "runtime replaced")
    brain[NPC_ID_FIELD] = nil
    brain.permanent = false
    brain.master = nil
    transmit(brain)
    log("retired runtimeId=" .. tostring(brain.id)
        .. " previousNpcId=" .. tostring(previousNpcId)
        .. " reason=" .. tostring(reason or "runtime replaced"))
    return true
end

local function canonicalBrain(member, candidates)
    if type(candidates) ~= "table" or #candidates == 0 then return nil, "not-found" end
    if member.runtimeId ~= nil then
        for _, brain in ipairs(candidates) do
            if tostring(brain.id) == tostring(member.runtimeId) then return brain, "runtime" end
        end
    end
    if #candidates == 1 then return candidates[1], "logical" end
    return nil, "ambiguous:" .. tostring(#candidates)
end

local function siteLoaded(site, cell)
    local points = site and site.derived and site.derived.points and site.derived.points.spawn
    for _, point in ipairs(type(points) == "table" and points or {}) do
        local square
        pcall(function()
            square = cell:getGridSquare(
                math.floor(tonumber(point.x) or 0),
                math.floor(tonumber(point.y) or 0),
                math.floor(tonumber(point.z) or 0)
            )
        end)
        if square then return true end
    end
    return false
end

local function liveRuntimeIds(site)
    local live = {}
    local cell = getCell and getCell() or nil
    if not cell then return live, false end
    local loaded = siteLoaded(site, cell)

    local zombieList
    pcall(function()
        if cell.getZombieList then zombieList = cell:getZombieList() end
    end)
    if zombieList then
        local size = 0
        pcall(function() size = zombieList:size() end)
        for index = 0, size - 1 do
            local zombie
            pcall(function() zombie = zombieList:get(index) end)
            if zombie and not zombie:isDead() then
                local brain = BanditBrain.Get(zombie)
                if brain and ownedBrain(brain, site.siteId) then
                    live[tostring(brain.id)] = true
                end
            end
        end
        return live, loaded
    end

    if type(site.bounds) ~= "table" then return live, loaded end
    local x1 = math.floor(tonumber(site.bounds.x) or 0)
    local y1 = math.floor(tonumber(site.bounds.y) or 0)
    local x2 = math.floor(tonumber(site.bounds.x2) or (x1 + (tonumber(site.bounds.w) or 1) - 1))
    local y2 = math.floor(tonumber(site.bounds.y2) or (y1 + (tonumber(site.bounds.h) or 1) - 1))
    local zLevels = {}
    local points = site.derived and site.derived.points and site.derived.points.spawn
    for _, point in ipairs(type(points) == "table" and points or {}) do
        zLevels[math.floor(tonumber(point.z) or 0)] = true
    end
    if next(zLevels) == nil then zLevels[0] = true end

    local scanned = 0
    local limit = math.max(1, math.floor(tonumber(C.FACTION_SITE_RUNTIME_SCAN_MAX_TILES) or 2048))
    for z in pairs(zLevels) do
        for y = y1, y2 do
            for x = x1, x2 do
                if scanned >= limit then return live, loaded end
                scanned = scanned + 1
                local square = cell:getGridSquare(x, y, z)
                if square then
                    loaded = true
                    local moving = square:getMovingObjects()
                    if moving then
                        for index = 0, moving:size() - 1 do
                            local zombie = moving:get(index)
                            if zombie and instanceof(zombie, "IsoZombie") and not zombie:isDead() then
                                local brain = BanditBrain.Get(zombie)
                                if brain and ownedBrain(brain, site.siteId) then
                                    live[tostring(brain.id)] = true
                                end
                            end
                        end
                    end
                end
            end
        end
    end
    return live, loaded
end

function Adapter.Reconcile(context)
    if type(context) ~= "table" then return false, "invalid reconciliation context" end
    if type(BanditClusters) ~= "table" then return false, "BanditClusters unavailable" end
    local site = context.site
    local definition = context.definition
    local profile = definition and definition.populationProfile
    if type(site) ~= "table" or type(profile) ~= "table" then
        return false, "invalid reconciliation site"
    end

    local plan, planError = Population.EnsurePlan(site, profile)
    if not plan then return false, planError end
    local byNpcId = collectBrains(site.siteId)
    local live, loaded = liveRuntimeIds(site)
    local stats = {
        loaded = loaded,
        materialized = 0,
        virtualized = 0,
        missing = 0,
        dead = 0,
        ambiguous = 0,
        retiredDuplicates = 0,
    }

    for _, member in ipairs(Population.ListAllMembers(site)) do
        local candidates = byNpcId[member.npcId] or {}
        if member.state == "DEAD" then
            stats.dead = stats.dead + 1
            for _, candidate in ipairs(candidates) do
                if retireBrain(candidate, "logical member dead") then
                    stats.retiredDuplicates = stats.retiredDuplicates + 1
                end
            end
        else
            local brain, source = canonicalBrain(member, candidates)
            if not brain then
                if source and source:sub(1, 10) == "ambiguous:" then
                    stats.ambiguous = stats.ambiguous + 1
                else
                    if member.state ~= "PLANNED" and member.state ~= "MISSING" then
                        Population.MarkMissing(site, member.npcId, "provider brain not found")
                    end
                    stats.missing = stats.missing + 1
                end
            else
                enforceOwnership(brain, site, plan, member, profile)
                for _, duplicate in ipairs(candidates) do
                    if duplicate ~= brain and retireBrain(duplicate, "duplicate logical faction runtime") then
                        stats.retiredDuplicates = stats.retiredDuplicates + 1
                    end
                end

                if live[tostring(brain.id)] then
                    Population.BindRuntime(site, member.npcId, brain.id, brain.bid)
                    stats.materialized = stats.materialized + 1
                else
                    Population.MarkVirtualized(
                        site,
                        member.npcId,
                        brain.id,
                        loaded and "provider brain has no loaded physical entity"
                            or "site geometry is not currently loaded"
                    )
                    stats.virtualized = stats.virtualized + 1
                end
            end
        end
    end
    return true, stats
end

local function technicalPlayer()
    if not getOnlinePlayers then return nil end
    local players = getOnlinePlayers()
    if not players then return nil end
    for index = 0, players:size() - 1 do
        local player = players:get(index)
        if player and not player:isDead() then return player end
    end
    return nil
end

local function snapshotIds()
    local ids = {}
    eachBrain(function(brain) ids[tostring(brain.id)] = true end)
    return ids
end

local function findNewBrain(previousIds, providerId)
    local result
    eachBrain(function(brain)
        if not result and not previousIds[tostring(brain.id)]
            and tostring(brain.bid or "") == tostring(providerId)
        then
            result = brain
        end
    end)
    return result
end

function Adapter.Rematerialize(context)
    if type(context) ~= "table" then return false, "invalid rematerialization context" end
    local site = context.site
    local definition = context.definition
    local profile = definition and definition.populationProfile
    local spawnPoints = context.spawnPoints
    if type(site) ~= "table" or type(profile) ~= "table" then
        return false, "invalid rematerialization site"
    end
    if type(spawnPoints) ~= "table" or #spawnPoints == 0 then
        return false, "no validated rematerialization points"
    end
    if not BanditServer or not BanditServer.Spawner or not BanditServer.Spawner.Individual then
        return false, "Bandits2 Individual spawner unavailable"
    end

    local player = technicalPlayer()
    if not player then return false, "no online player available as Bandits API context" end
    local plan = Population.GetPlan(site)
    if not plan then return false, "population plan missing" end
    local provider, providerError = Profiles.Ensure(profile.providerProfile)
    if not provider then return false, providerError end

    local created = 0
    local pending = Population.GetUnmaterialized(site)
    for index, member in ipairs(pending) do
        local point = spawnPoints[index]
        local providerId = member.providerId
        if providerId == nil then
            local ids = Profiles.ListProviderIds(profile.providerProfile)
            providerId = ids[((index - 1) % #ids) + 1]
        end
        if not point or not providerId then break end

        local bandit = BanditCustom.GetById(providerId)
        if not bandit then return false, "Bandits provider profile missing" end
        bandit.cid = provider.cid
        bandit.general = bandit.general or {}
        bandit.general.cid = provider.cid

        local oldRuntimeId = member.runtimeId or member.previousRuntimeId
        local before = snapshotIds()
        local ok, errorText = pcall(BanditServer.Spawner.Individual, player, {
            bid = providerId,
            x = point.x,
            y = point.y,
            z = point.z,
            program = profile.program,
            permanent = true,
            hostile = false,
            hostileP = false,
        })
        if not ok then return false, "Bandits2 rematerialization failed: " .. tostring(errorText) end

        local brain = findNewBrain(before, providerId)
        if not brain then return false, "Bandits2 did not register rematerialized brain" end
        enforceOwnership(brain, site, plan, member, profile)
        Population.BindRuntime(site, member.npcId, brain.id, brain.bid)
        member.previousRuntimeId = nil
        created = created + 1

        if oldRuntimeId ~= nil and tostring(oldRuntimeId) ~= tostring(brain.id) then
            local oldBrain = brainByRuntimeId(oldRuntimeId)
            if oldBrain and ownedBrain(oldBrain, nil) then
                retireBrain(oldBrain, "logical member rematerialized")
            end
        end
        log("rematerialized npcId=" .. tostring(member.npcId)
            .. " runtimeId=" .. tostring(brain.id)
            .. " siteId=" .. tostring(site.siteId))
    end
    return true, created
end

local function brainFromZombie(zombie)
    if not zombie then return nil end
    local brain = BanditBrain.Get(zombie)
    if brain then return brain end
    if zombie.getModData then
        local modData = zombie:getModData()
        if modData and type(modData.brain) == "table" then return modData.brain end
    end
    local runtimeId = BanditUtils.GetZombieID and BanditUtils.GetZombieID(zombie) or nil
    if runtimeId ~= nil and GetBanditClusterData then
        local gmd = GetBanditClusterData(runtimeId)
        if gmd then return gmd[runtimeId] or gmd[tostring(runtimeId)] end
    end
    return nil
end

local function onZombieDead(zombie)
    local brain = brainFromZombie(zombie)
    if not ownedBrain(brain, nil) then return end
    local siteId = brain[SITE_ID_FIELD]
    local npcId = brain[NPC_ID_FIELD]
    if type(siteId) ~= "string" or type(npcId) ~= "string" then return end

    local ok = Population.MarkDead(siteId, npcId, "physical faction NPC died")
    if ok then
        brain[RETIRED_FIELD] = true
        brain.lccqRetiredNpcId = npcId
        brain.lccqRetiredReason = "dead"
        brain[NPC_ID_FIELD] = nil
        brain.permanent = false
        transmit(brain)
        log("dead npcId=" .. tostring(npcId)
            .. " runtimeId=" .. tostring(brain.id)
            .. " siteId=" .. tostring(siteId))
    end
end

if isServer and isServer() and Events.OnZombieDead then
    Events.OnZombieDead.Add(onZombieDead)
end

log("extended materializer lifecycle=reconcile+rematerialize+death guardProgram=LCCQFFactionGuard")
return Adapter
