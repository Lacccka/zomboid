-- Bandits2 physical materializer for LCCQF faction sites. Logical identity and
-- population ownership remain in FactionSitePopulation; Bandits brains are runtime
-- instances tagged with framework ownership after successful provider spawn.
if isClient and isClient() and not (isServer and isServer()) then
    return {}
end

require "Bandit"
require "BanditBrain"
require "BanditCustom"
require "BanditUtils"
require "LCCQF/LCCQFConstants"
require "LCCQF/FactionWorld/LCCQFFactionSiteMaterializerRegistry"
require "LCCQF/FactionWorld/LCCQFFactionSitePopulation"
require "LCCQF/Runtime/LCCQFBanditsFactionProfiles"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Registry = LCCQF.FactionSiteMaterializerRegistry
local Population = LCCQF.FactionSitePopulation
local Profiles = LCCQF.BanditsFactionProfiles
local Adapter = {}

local NPC_ID_FIELD = "lccqNpcId"
local FACTION_ID_FIELD = "lccqFactionId"
local SITE_ID_FIELD = "lccqSiteId"
local SQUAD_ID_FIELD = "lccqSquadId"
local ROLE_ID_FIELD = "lccqRoleId"
local PROVIDER_FIELD = "lccqProvider"

local function log(message)
    print(C.LOG_PREFIX .. "[RUNTIME:BANDITS:FACTION:SITE] " .. tostring(message))
end

local function technicalPlayer()
    if not getOnlinePlayers then return nil end
    local players = getOnlinePlayers()
    if not players then return nil end
    local size = 0
    pcall(function() size = players:size() end)
    for index = 0, size - 1 do
        local player
        pcall(function() player = players:get(index) end)
        if player then
            local alive = true
            pcall(function() alive = not player:isDead() end)
            if alive then return player end
        end
    end
    return nil
end

local function snapshotBrainIds()
    local ids = {}
    if type(BanditClusters) ~= "table" then return ids end
    for _, cluster in pairs(BanditClusters) do
        if type(cluster) == "table" then
            for _, brain in pairs(cluster) do
                if type(brain) == "table" and brain.id ~= nil then
                    ids[tostring(brain.id)] = true
                end
            end
        end
    end
    return ids
end

local function collectNewBrains(previousIds, cid)
    local out = {}
    if type(BanditClusters) ~= "table" then return out end
    for _, cluster in pairs(BanditClusters) do
        if type(cluster) == "table" then
            for _, brain in pairs(cluster) do
                if type(brain) == "table"
                    and brain.id ~= nil
                    and not previousIds[tostring(brain.id)]
                    and tostring(brain.cid or brain.clan or "") == tostring(cid)
                then
                    out[#out + 1] = brain
                end
            end
        end
    end
    table.sort(out, function(a, b) return tostring(a.id) < tostring(b.id) end)
    return out
end

local function tagBrain(brain, site, plan, member)
    if type(brain) ~= "table" or brain.id == nil then return false end
    brain[NPC_ID_FIELD] = member.npcId
    brain[FACTION_ID_FIELD] = site.factionId
    brain[SITE_ID_FIELD] = site.siteId
    brain[SQUAD_ID_FIELD] = plan.squadId
    brain[ROLE_ID_FIELD] = member.roleId
    brain[PROVIDER_FIELD] = "Bandits"

    -- Spawner.Clan/Individual require a player context and temporarily use that
    -- character as brain.master. Autonomous faction NPCs must not belong to that
    -- arbitrary player after creation.
    brain.master = nil
    brain.key = nil
    brain.permanent = true
    brain.hostile = false
    brain.hostileP = false

    local gmd = GetBanditClusterData and GetBanditClusterData(brain.id) or nil
    if gmd then gmd[brain.id] = brain end
    if TransmitBanditCluster then TransmitBanditCluster(brain.id) end
    return true
end

local function bindBrains(site, plan, members, brains)
    local bound = 0
    local maximum = math.min(#members, #brains)
    for index = 1, maximum do
        local member = members[index]
        local brain = brains[index]
        if tagBrain(brain, site, plan, member) then
            local ok = Population.BindRuntime(site, member.npcId, brain.id, brain.bid)
            if ok then
                bound = bound + 1
                log("bound npcId=" .. tostring(member.npcId)
                    .. " runtimeId=" .. tostring(brain.id)
                    .. " providerId=" .. tostring(brain.bid)
                    .. " factionId=" .. tostring(site.factionId)
                    .. " siteId=" .. tostring(site.siteId)
                    .. " squadId=" .. tostring(plan.squadId)
                    .. " roleId=" .. tostring(member.roleId))
            end
        end
    end
    return bound
end

local function usedProviderIds(plan)
    local used = {}
    for _, member in ipairs(type(plan.members) == "table" and plan.members or {}) do
        if member.providerId ~= nil then used[tostring(member.providerId)] = true end
    end
    return used
end

local function availableProviderIds(providerProfile, plan)
    local used = usedProviderIds(plan)
    local out = {}
    for _, providerId in ipairs(Profiles.ListProviderIds(providerProfile)) do
        if not used[tostring(providerId)] then out[#out + 1] = providerId end
    end
    return out
end

local function groupSpawn(player, site, profile, provider, plan, members, spawnPoints)
    if not BanditServer or not BanditServer.Spawner or not BanditServer.Spawner.Clan then
        return 0, "Bandits2 Spawner.Clan unavailable"
    end
    local points = {}
    for index = 1, #members do points[index] = spawnPoints[index] end

    local before = snapshotBrainIds()
    local ok, errorText = pcall(BanditServer.Spawner.Clan, player, {
        cid = provider.cid,
        size = #members,
        program = profile.program,
        permanent = true,
        hostile = false,
        hostileP = false,
        spawnPoints = points,
    })
    if not ok then return 0, "Bandits2 Clan spawn failed: " .. tostring(errorText) end

    local brains = collectNewBrains(before, provider.cid)
    local bound = bindBrains(site, plan, members, brains)
    if bound == 0 then return 0, "Bandits2 did not register faction brains" end
    return bound
end

local function individualSpawn(player, site, profile, provider, plan, member, spawnPoint, providerId)
    if not BanditServer or not BanditServer.Spawner or not BanditServer.Spawner.Individual then
        return 0, "Bandits2 Spawner.Individual unavailable"
    end

    local bandit = BanditCustom.GetById(providerId)
    if not bandit then return 0, "Bandits provider profile missing" end
    bandit.cid = provider.cid
    bandit.general = bandit.general or {}
    bandit.general.cid = provider.cid

    local before = snapshotBrainIds()
    local ok, errorText = pcall(BanditServer.Spawner.Individual, player, {
        bid = providerId,
        x = spawnPoint.x,
        y = spawnPoint.y,
        z = spawnPoint.z,
        program = profile.program,
        permanent = true,
        hostile = false,
        hostileP = false,
    })
    if not ok then return 0, "Bandits2 Individual spawn failed: " .. tostring(errorText) end

    local brains = collectNewBrains(before, provider.cid)
    local matching = {}
    for _, brain in ipairs(brains) do
        if tostring(brain.bid or "") == tostring(providerId) then matching[#matching + 1] = brain end
    end
    if #matching == 0 then return 0, "Bandits2 did not register retry brain" end
    return bindBrains(site, plan, { member }, { matching[1] })
end

function Adapter.Materialize(context)
    if type(context) ~= "table" then return false, "invalid materialization context" end
    local site = context.site
    local definition = context.definition
    local profile = definition and definition.populationProfile
    local spawnPoints = context.spawnPoints
    if type(site) ~= "table" or site.state ~= "VALIDATING" then
        return false, "site is not ready for materialization"
    end
    if type(profile) ~= "table" or profile.materializer ~= "Bandits" then
        return false, "invalid Bandits population profile"
    end
    if type(spawnPoints) ~= "table" or #spawnPoints == 0 then
        return false, "no validated materialization points"
    end

    local plan, planError = Population.EnsurePlan(site, profile)
    if not plan then return false, planError end
    local missing = Population.GetUnmaterialized(site)
    if #missing == 0 then return true, 0 end

    local provider, providerError = Profiles.Ensure(profile.providerProfile)
    if not provider then return false, providerError end
    if provider.profileCount < tonumber(profile.maxPopulation) then
        return false, "Bandits provider pool smaller than maxPopulation"
    end
    if #spawnPoints < #missing then
        return false, "not enough currently valid spawn points"
    end

    local player = technicalPlayer()
    if not player then return false, "no online player available as Bandits API context" end

    local bound = 0
    local firstMaterialization = Population.CountMaterialized(site) == 0
    if firstMaterialization then
        local groupBound, groupError = groupSpawn(player, site, profile, provider, plan, missing, spawnPoints)
        bound = bound + (groupBound or 0)
        if bound == 0 then return false, groupError end
    else
        -- Partial provider failures are retried deterministically with unused private
        -- provider profiles. This avoids Clan() randomly reusing a profile already
        -- bound to another logical member.
        local providerIds = availableProviderIds(profile.providerProfile, plan)
        for index, member in ipairs(missing) do
            local providerId = providerIds[index]
            local spawnPoint = spawnPoints[index]
            if not providerId or not spawnPoint then break end
            local oneBound, oneError = individualSpawn(
                player, site, profile, provider, plan, member, spawnPoint, providerId
            )
            if oneBound == 0 then
                log("retry deferred npcId=" .. tostring(member.npcId)
                    .. " reason=" .. tostring(oneError))
                break
            end
            bound = bound + oneBound
        end
    end

    return true, bound
end

Registry.Register("Bandits", Adapter)
LCCQF.BanditsFactionSiteMaterializer = Adapter
log("registered materializer=Bandits ownershipTags=true")
return Adapter
