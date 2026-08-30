-- Retire previous LCCQF-owned Bandits runtimes after identity-preserving relocation.
-- This wraps provider reconciliation; it never removes zombies or world objects.
if isClient and isClient() and not (isServer and isServer()) then
    return {}
end

require "LCCQF/LCCQFConstants"
require "LCCQF/FactionWorld/LCCQFFactionSiteRegistry"
require "LCCQF/FactionWorld/LCCQFFactionSitePopulation"
require "LCCQF/Runtime/LCCQFBanditsFactionSiteLifecycle"

local C = LCCQF.Constants
local Sites = LCCQF.FactionSiteRegistry
local Population = LCCQF.FactionSitePopulation
local Adapter = LCCQF.BanditsFactionSiteMaterializer
local originalReconcile = Adapter.Reconcile

local function log(message)
    print(C.LOG_PREFIX .. "[RUNTIME:BANDITS:FACTION:RELOCATION] " .. tostring(message))
end

local function findBrain(runtimeId)
    if runtimeId == nil or type(BanditClusters) ~= "table" then return nil end
    local wanted = tostring(runtimeId)
    local found
    for _, cluster in pairs(BanditClusters) do
        if type(cluster) == "table" then
            for _, brain in pairs(cluster) do
                if not found and type(brain) == "table" and brain.id ~= nil
                    and tostring(brain.id) == wanted
                then
                    found = brain
                end
            end
        end
    end
    return found
end

local function retire(brain, npcId)
    if type(brain) ~= "table" or brain.id == nil then return false end
    if brain.lccqProvider ~= "Bandits" or brain.lccqRetired == true then return false end
    if brain.lccqNpcId ~= npcId then return false end

    brain.lccqRetired = true
    brain.lccqRetiredNpcId = npcId
    brain.lccqRetiredReason = "faction site relocated"
    brain.lccqNpcId = nil
    brain.permanent = false
    brain.master = nil
    local gmd = GetBanditClusterData and GetBanditClusterData(brain.id) or nil
    if gmd then gmd[brain.id] = brain end
    if TransmitBanditCluster then TransmitBanditCluster(brain.id) end
    return true
end

Adapter.Reconcile = function(context)
    local ok, stats = originalReconcile(context)
    if not ok then return ok, stats end

    local site = context and context.site
    if type(site) ~= "table" then return ok, stats end
    local changed = false
    for _, member in ipairs(Population.ListAllMembers(site)) do
        local previousRuntimeId = member.previousRuntimeId
        if previousRuntimeId ~= nil and member.runtimeId ~= nil
            and tostring(previousRuntimeId) ~= tostring(member.runtimeId)
        then
            local oldBrain = findBrain(previousRuntimeId)
            local retired = oldBrain and retire(oldBrain, member.npcId) or false
            log("npcId=" .. tostring(member.npcId)
                .. " previousRuntimeId=" .. tostring(previousRuntimeId)
                .. " runtimeId=" .. tostring(member.runtimeId)
                .. " retired=" .. tostring(retired))
            member.previousRuntimeId = nil
            changed = true
        end
    end
    if changed then Sites.MarkDirty(site.siteId, "relocation previous runtimes reconciled") end
    return ok, stats
end

return Adapter
