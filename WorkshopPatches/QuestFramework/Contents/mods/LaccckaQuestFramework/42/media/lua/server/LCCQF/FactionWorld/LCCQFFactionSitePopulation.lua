-- Provider-neutral logical population owned by LCCQF. Physical runtime adapters bind
-- to these members, but never define their identity.
if isClient and isClient() and not (isServer and isServer()) then
    return {}
end

require "LCCQF/LCCQFConstants"
require "LCCQF/FactionWorld/LCCQFFactionSiteRegistry"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Sites = LCCQF.FactionSiteRegistry
local Population = LCCQF.FactionSitePopulation or {}

local VALID_MEMBER_STATES = {
    PLANNED = true,
    MATERIALIZED = true,
    VIRTUALIZED = true,
    MISSING = true,
    DEAD = true,
}

local function validId(value)
    return type(value) == "string" and value ~= "" and #value <= C.MAX_IDENTIFIER_LENGTH * 3
end

local function normalizeCount(value, minimum, maximum)
    local number = math.floor(tonumber(value) or minimum)
    return math.max(minimum, math.min(maximum or number, number))
end

local function worldHours()
    if not getGameTime then return 0 end
    local gameTime = getGameTime()
    return gameTime and gameTime.getWorldAgeHours and (tonumber(gameTime:getWorldAgeHours()) or 0) or 0
end

local function roleSequence(profile, targetPopulation)
    local out = {}
    local roles = type(profile.roles) == "table" and profile.roles or {}
    for _, role in ipairs(roles) do
        if type(role) == "table" and validId(role.roleId) then
            local count = normalizeCount(role.count, 0, targetPopulation)
            for _ = 1, count do
                if #out >= targetPopulation then break end
                out[#out + 1] = role.roleId
            end
        end
    end
    while #out < targetPopulation do out[#out + 1] = "member" end
    return out
end

local function memberId(siteId, sequence)
    return "lccqf_npc_" .. tostring(siteId) .. "_" .. tostring(sequence)
end

local function squadId(siteId)
    return "lccqf_squad_" .. tostring(siteId) .. "_1"
end

local function ensureMemberShape(member)
    if type(member) ~= "table" then return false end
    if not validId(member.npcId) or not validId(member.roleId) then return false end
    if not VALID_MEMBER_STATES[member.state] then member.state = "PLANNED" end
    if member.runtimeId ~= nil then member.runtimeId = tostring(member.runtimeId) end
    if member.providerId ~= nil then member.providerId = tostring(member.providerId) end
    return true
end

function Population.EnsurePlan(site, profile)
    if type(site) ~= "table" or not validId(site.siteId) or not validId(site.factionId) then
        return nil, "invalid faction site"
    end
    if type(profile) ~= "table" or profile.enabled == false then
        return nil, "population disabled"
    end

    local initialPopulation = normalizeCount(profile.initialPopulation, 1, 64)
    local maxPopulation = normalizeCount(profile.maxPopulation or initialPopulation, initialPopulation, 64)
    if initialPopulation > maxPopulation then initialPopulation = maxPopulation end

    local plan = site.population
    if type(plan) ~= "table" then
        plan = {
            schemaVersion = 2,
            squadId = squadId(site.siteId),
            targetPopulation = initialPopulation,
            maxPopulation = maxPopulation,
            materializer = tostring(profile.materializer or ""),
            providerProfile = tostring(profile.providerProfile or ""),
            nextMemberSequence = 1,
            members = {},
        }
        site.population = plan
    end

    plan.schemaVersion = 2
    if not validId(plan.squadId) then plan.squadId = squadId(site.siteId) end
    plan.targetPopulation = normalizeCount(plan.targetPopulation or initialPopulation, 1, maxPopulation)
    plan.maxPopulation = maxPopulation
    plan.materializer = tostring(profile.materializer or plan.materializer or "")
    plan.providerProfile = tostring(profile.providerProfile or plan.providerProfile or "")
    if type(plan.members) ~= "table" then plan.members = {} end

    local changed = false
    local roles = roleSequence(profile, plan.targetPopulation)
    for index = 1, plan.targetPopulation do
        local member = plan.members[index]
        if not ensureMemberShape(member) then
            member = {
                npcId = memberId(site.siteId, index),
                roleId = roles[index],
                state = "PLANNED",
                runtimeId = nil,
                providerId = nil,
                createdWorldHours = worldHours(),
            }
            plan.members[index] = member
            changed = true
        elseif member.roleId == "member" and roles[index] ~= "member" then
            member.roleId = roles[index]
            changed = true
        end
    end

    local minimumNext = #plan.members + 1
    local nextSequence = math.max(1, math.floor(tonumber(plan.nextMemberSequence) or minimumNext))
    if nextSequence < minimumNext then nextSequence = minimumNext end
    if plan.nextMemberSequence ~= nextSequence then
        plan.nextMemberSequence = nextSequence
        changed = true
    end

    -- Never silently delete logical identities if authored content later changes.
    -- Dead/replaced members remain as history; current membership is selected from
    -- non-DEAD records up to targetPopulation.
    if changed then Sites.MarkDirty(site.siteId, "population plan normalized") end
    return plan
end

function Population.GetPlan(siteOrId)
    local site = type(siteOrId) == "table" and siteOrId or Sites.GetSite(siteOrId)
    if not site or type(site.population) ~= "table" then return nil end
    return site.population
end

function Population.ListAllMembers(siteOrId)
    local plan = Population.GetPlan(siteOrId)
    local out = {}
    if not plan or type(plan.members) ~= "table" then return out end
    for _, member in ipairs(plan.members) do
        if type(member) == "table" and ensureMemberShape(member) then out[#out + 1] = member end
    end
    return out
end

function Population.ListMembers(siteOrId)
    local plan = Population.GetPlan(siteOrId)
    local out = {}
    if not plan then return out end
    local target = math.max(0, math.floor(tonumber(plan.targetPopulation) or 0))
    for _, member in ipairs(Population.ListAllMembers(siteOrId)) do
        if member.state ~= "DEAD" then
            out[#out + 1] = member
            if #out >= target then break end
        end
    end
    return out
end

function Population.GetMemberByNpcId(siteOrId, npcId)
    if not validId(npcId) then return nil end
    for _, member in ipairs(Population.ListAllMembers(siteOrId)) do
        if member.npcId == npcId then return member end
    end
    return nil
end

function Population.GetUnmaterialized(siteOrId)
    local out = {}
    for _, member in ipairs(Population.ListMembers(siteOrId)) do
        if member.state ~= "MATERIALIZED" or member.runtimeId == nil then
            out[#out + 1] = member
        end
    end
    return out
end

function Population.BindRuntime(siteOrId, npcId, runtimeId, providerId)
    local site = type(siteOrId) == "table" and siteOrId or Sites.GetSite(siteOrId)
    if not site or not validId(npcId) or runtimeId == nil then return false, "invalid runtime binding" end
    local member = Population.GetMemberByNpcId(site, npcId)
    if not member or member.state == "DEAD" then return false, "logical member not bindable" end

    local newRuntimeId = tostring(runtimeId)
    local newProviderId = providerId and tostring(providerId) or member.providerId
    local changed = member.runtimeId ~= newRuntimeId
        or member.providerId ~= newProviderId
        or member.state ~= "MATERIALIZED"
    member.runtimeId = newRuntimeId
    member.providerId = newProviderId
    member.state = "MATERIALIZED"
    member.materializedWorldHours = worldHours()
    member.lastReason = nil
    if changed then Sites.MarkDirty(site.siteId, "population runtime bound") end
    return true, member
end

function Population.MarkVirtualized(siteOrId, npcId, runtimeId, reason)
    local site = type(siteOrId) == "table" and siteOrId or Sites.GetSite(siteOrId)
    if not site then return false, "site not found" end
    local member = Population.GetMemberByNpcId(site, npcId)
    if not member or member.state == "DEAD" then return false, "logical member not virtualizable" end

    local newRuntimeId = runtimeId ~= nil and tostring(runtimeId) or member.runtimeId
    local newReason = reason and tostring(reason) or nil
    local changed = member.state ~= "VIRTUALIZED"
        or member.runtimeId ~= newRuntimeId
        or member.lastReason ~= newReason
    member.runtimeId = newRuntimeId
    member.state = "VIRTUALIZED"
    member.virtualizedWorldHours = worldHours()
    member.lastReason = newReason
    if changed then Sites.MarkDirty(site.siteId, "population member virtualized") end
    return true, member
end

function Population.MarkMissing(siteOrId, npcId, reason)
    local site = type(siteOrId) == "table" and siteOrId or Sites.GetSite(siteOrId)
    if not site then return false, "site not found" end
    local member = Population.GetMemberByNpcId(site, npcId)
    if not member or member.state == "DEAD" then return false, "logical member not markable missing" end

    local newReason = reason and tostring(reason) or nil
    local changed = member.state ~= "MISSING" or member.runtimeId ~= nil or member.lastReason ~= newReason
    member.state = "MISSING"
    member.runtimeId = nil
    member.missingWorldHours = worldHours()
    member.lastReason = newReason
    if changed then Sites.MarkDirty(site.siteId, "population member missing") end
    return true, member
end

function Population.MarkDead(siteOrId, npcId, reason)
    local site = type(siteOrId) == "table" and siteOrId or Sites.GetSite(siteOrId)
    if not site then return false, "site not found" end
    local member = Population.GetMemberByNpcId(site, npcId)
    if not member then return false, "logical member not found" end
    if member.state == "DEAD" then return true, member end

    member.state = "DEAD"
    member.runtimeId = nil
    member.lastReason = reason and tostring(reason) or nil
    member.deadWorldHours = worldHours()
    Sites.MarkDirty(site.siteId, "population member dead")
    return true, member
end

function Population.AppendReplacement(siteOrId, deadNpcId)
    local site = type(siteOrId) == "table" and siteOrId or Sites.GetSite(siteOrId)
    if not site then return false, "site not found" end
    local plan = Population.GetPlan(site)
    if not plan then return false, "population plan missing" end
    local dead = Population.GetMemberByNpcId(site, deadNpcId)
    if not dead or dead.state ~= "DEAD" then return false, "replacement source is not dead" end
    if dead.replacementNpcId then return false, "replacement already planned" end
    if #Population.ListMembers(site) >= math.max(1, math.floor(tonumber(plan.targetPopulation) or 1)) then
        return false, "target population already satisfied"
    end

    local sequence = math.max(#plan.members + 1, math.floor(tonumber(plan.nextMemberSequence) or (#plan.members + 1)))
    local replacement = {
        npcId = memberId(site.siteId, sequence),
        roleId = dead.roleId or "member",
        state = "PLANNED",
        runtimeId = nil,
        providerId = nil,
        replacesNpcId = dead.npcId,
        createdWorldHours = worldHours(),
    }
    plan.members[#plan.members + 1] = replacement
    plan.nextMemberSequence = sequence + 1
    dead.replacementNpcId = replacement.npcId
    dead.replacedWorldHours = worldHours()
    Sites.MarkDirty(site.siteId, "population replacement planned")
    return true, replacement
end

function Population.TransferPlan(fromSiteOrId, toSiteOrId, reason)
    local fromSite = type(fromSiteOrId) == "table" and fromSiteOrId or Sites.GetSite(fromSiteOrId)
    local toSite = type(toSiteOrId) == "table" and toSiteOrId or Sites.GetSite(toSiteOrId)
    if not fromSite or not toSite or fromSite.siteId == toSite.siteId then
        return false, "invalid population transfer"
    end
    if type(fromSite.population) ~= "table" then return false, "source population missing" end
    if type(toSite.population) == "table" then return false, "destination population already exists" end

    local transferReason = tostring(reason or "faction site relocation")
    local plan = fromSite.population
    fromSite.population = nil
    toSite.population = plan
    plan.relocatedFromSiteId = fromSite.siteId
    plan.relocatedWorldHours = worldHours()

    for _, member in ipairs(Population.ListAllMembers(toSite)) do
        if member.state ~= "DEAD" then
            member.state = "VIRTUALIZED"
            member.lastReason = transferReason
            member.relocatedFromSiteId = fromSite.siteId
        end
    end

    Sites.MarkDirty(fromSite.siteId, "population transferred to " .. tostring(toSite.siteId))
    Sites.MarkDirty(toSite.siteId, "population transferred from " .. tostring(fromSite.siteId))
    return true, plan
end

function Population.CountMaterialized(siteOrId)
    local count = 0
    for _, member in ipairs(Population.ListMembers(siteOrId)) do
        if member.state == "MATERIALIZED" and member.runtimeId ~= nil then count = count + 1 end
    end
    return count
end

function Population.CountVirtualized(siteOrId)
    local count = 0
    for _, member in ipairs(Population.ListMembers(siteOrId)) do
        if member.state == "VIRTUALIZED" and member.runtimeId ~= nil then count = count + 1 end
    end
    return count
end

function Population.CountAvailable(siteOrId)
    return Population.CountMaterialized(siteOrId) + Population.CountVirtualized(siteOrId)
end

function Population.IsInitialPopulationMaterialized(siteOrId)
    local plan = Population.GetPlan(siteOrId)
    if not plan then return false end
    return Population.CountMaterialized(siteOrId) >= math.max(1, math.floor(tonumber(plan.targetPopulation) or 1))
end

LCCQF.FactionSitePopulation = Population
return Population
