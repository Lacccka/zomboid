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

local function memberId(siteId, index)
    return "lccqf_npc_" .. tostring(siteId) .. "_" .. tostring(index)
end

local function squadId(siteId)
    return "lccqf_squad_" .. tostring(siteId) .. "_1"
end

local function ensureMemberShape(member)
    if type(member) ~= "table" then return false end
    if not validId(member.npcId) or not validId(member.roleId) then return false end
    if not VALID_MEMBER_STATES[member.state] then member.state = "PLANNED" end
    if member.runtimeId ~= nil then member.runtimeId = tostring(member.runtimeId) end
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
            schemaVersion = 1,
            squadId = squadId(site.siteId),
            targetPopulation = initialPopulation,
            maxPopulation = maxPopulation,
            materializer = tostring(profile.materializer or ""),
            providerProfile = tostring(profile.providerProfile or ""),
            members = {},
        }
        site.population = plan
    end

    plan.schemaVersion = 1
    if not validId(plan.squadId) then plan.squadId = squadId(site.siteId) end
    plan.targetPopulation = normalizeCount(plan.targetPopulation or initialPopulation, 1, maxPopulation)
    plan.maxPopulation = maxPopulation
    plan.materializer = tostring(profile.materializer or plan.materializer or "")
    plan.providerProfile = tostring(profile.providerProfile or plan.providerProfile or "")
    if type(plan.members) ~= "table" then plan.members = {} end

    local roles = roleSequence(profile, plan.targetPopulation)
    local changed = false
    for index = 1, plan.targetPopulation do
        local member = plan.members[index]
        if not ensureMemberShape(member) then
            member = {
                npcId = memberId(site.siteId, index),
                roleId = roles[index],
                state = "PLANNED",
                runtimeId = nil,
                providerId = nil,
            }
            plan.members[index] = member
            changed = true
        elseif member.roleId == "member" and roles[index] ~= "member" then
            member.roleId = roles[index]
            changed = true
        end
    end

    -- Never silently delete logical identities if content later lowers its target.
    -- Existing members remain persisted; targetPopulation controls how many are needed.
    if changed then Sites.MarkDirty(site.siteId, "population plan created") end
    return plan
end

function Population.GetPlan(siteOrId)
    local site = type(siteOrId) == "table" and siteOrId or Sites.GetSite(siteOrId)
    if not site or type(site.population) ~= "table" then return nil end
    return site.population
end

function Population.ListMembers(siteOrId)
    local plan = Population.GetPlan(siteOrId)
    local out = {}
    if not plan or type(plan.members) ~= "table" then return out end
    local target = math.max(0, math.floor(tonumber(plan.targetPopulation) or #plan.members))
    for index = 1, math.min(target, #plan.members) do
        local member = plan.members[index]
        if type(member) == "table" then out[#out + 1] = member end
    end
    return out
end

function Population.GetMemberByNpcId(siteOrId, npcId)
    if not validId(npcId) then return nil end
    for _, member in ipairs(Population.ListMembers(siteOrId)) do
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
    if not member then return false, "logical member not found" end

    member.runtimeId = tostring(runtimeId)
    member.providerId = providerId and tostring(providerId) or member.providerId
    member.state = "MATERIALIZED"
    member.materializedWorldHours = getGameTime and getGameTime():getWorldAgeHours() or 0
    Sites.MarkDirty(site.siteId, "population runtime bound")
    return true, member
end

function Population.MarkMissing(siteOrId, npcId, reason)
    local site = type(siteOrId) == "table" and siteOrId or Sites.GetSite(siteOrId)
    if not site then return false, "site not found" end
    local member = Population.GetMemberByNpcId(site, npcId)
    if not member then return false, "logical member not found" end

    member.state = "MISSING"
    member.runtimeId = nil
    member.lastReason = reason and tostring(reason) or nil
    Sites.MarkDirty(site.siteId, "population member missing")
    return true, member
end

function Population.MarkDead(siteOrId, npcId, reason)
    local site = type(siteOrId) == "table" and siteOrId or Sites.GetSite(siteOrId)
    if not site then return false, "site not found" end
    local member = Population.GetMemberByNpcId(site, npcId)
    if not member then return false, "logical member not found" end

    member.state = "DEAD"
    member.runtimeId = nil
    member.lastReason = reason and tostring(reason) or nil
    member.deadWorldHours = getGameTime and getGameTime():getWorldAgeHours() or 0
    Sites.MarkDirty(site.siteId, "population member dead")
    return true, member
end

function Population.CountMaterialized(siteOrId)
    local count = 0
    for _, member in ipairs(Population.ListMembers(siteOrId)) do
        if member.state == "MATERIALIZED" and member.runtimeId ~= nil then count = count + 1 end
    end
    return count
end

function Population.IsInitialPopulationMaterialized(siteOrId)
    local plan = Population.GetPlan(siteOrId)
    if not plan then return false end
    return Population.CountMaterialized(siteOrId) >= math.max(1, math.floor(tonumber(plan.targetPopulation) or 1))
end

LCCQF.FactionSitePopulation = Population
return Population
