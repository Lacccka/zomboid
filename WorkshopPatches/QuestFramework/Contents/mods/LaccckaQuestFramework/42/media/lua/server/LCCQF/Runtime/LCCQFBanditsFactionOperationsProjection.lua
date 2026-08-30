-- Bandits2 projection of server-owned faction job assignments. Logical schedules and
-- targets stay in FactionSiteOperations/member.assignment; brains receive runtime tags.
if isClient and isClient() and not (isServer and isServer()) then
    return {}
end

require "Bandit"
require "LCCQF/LCCQFConstants"
require "LCCQF/FactionWorld/LCCQFFactionSitePopulation"
require "LCCQF/Runtime/LCCQFBanditsFactionSiteMaterializer"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Population = LCCQF.FactionSitePopulation
local Adapter = LCCQF.BanditsFactionSiteMaterializer

local function log(message)
    print(C.LOG_PREFIX .. "[RUNTIME:BANDITS:FACTION:OPERATIONS] " .. tostring(message))
end

local function eachBrain(visitor)
    if type(BanditClusters) ~= "table" then return false end
    local seen = {}
    for _, cluster in pairs(BanditClusters) do
        if type(cluster) == "table" then
            for _, brain in pairs(cluster) do
                if type(brain) == "table" and brain.id ~= nil then
                    local runtimeId = tostring(brain.id)
                    if not seen[runtimeId] then
                        seen[runtimeId] = true
                        visitor(brain)
                    end
                end
            end
        end
    end
    return true
end

local function transmit(brain)
    if not brain or brain.id == nil then return end
    local gmd = GetBanditClusterData and GetBanditClusterData(brain.id) or nil
    if gmd then gmd[brain.id] = brain end
    if TransmitBanditCluster then TransmitBanditCluster(brain.id) end
end

local function brainForMember(site, member)
    local exact
    local fallback
    eachBrain(function(brain)
        if brain.lccqProvider == "Bandits"
            and brain.lccqRetired ~= true
            and brain.lccqSiteId == site.siteId
            and brain.lccqNpcId == member.npcId
        then
            if member.runtimeId ~= nil and tostring(brain.id) == tostring(member.runtimeId) then
                exact = brain
            elseif fallback == nil then
                fallback = brain
            end
        end
    end)
    return exact or fallback
end

local function setField(brain, key, value)
    if brain[key] == value then return false end
    brain[key] = value
    return true
end

local function applyAssignment(brain, assignment)
    if type(brain) ~= "table" or type(assignment) ~= "table" then return false end
    local changed = false
    changed = setField(brain, "lccqJobId", assignment.jobId) or changed
    changed = setField(brain, "lccqDutyMode", assignment.dutyMode) or changed
    changed = setField(brain, "lccqDutyTargetKind", assignment.targetKind) or changed
    changed = setField(brain, "lccqAssignmentRevision", tonumber(assignment.revision) or 0) or changed

    local target = assignment.target
    if type(target) == "table" then
        changed = setField(brain, "lccqDutyX", tonumber(target.x)) or changed
        changed = setField(brain, "lccqDutyY", tonumber(target.y)) or changed
        changed = setField(brain, "lccqDutyZ", tonumber(target.z) or 0) or changed
    else
        changed = setField(brain, "lccqDutyX", nil) or changed
        changed = setField(brain, "lccqDutyY", nil) or changed
        changed = setField(brain, "lccqDutyZ", nil) or changed
    end
    if changed then transmit(brain) end
    return changed
end

function Adapter.ApplyOperations(context)
    if type(context) ~= "table" or type(context.site) ~= "table" then
        return false, "invalid operations projection context"
    end
    if type(BanditClusters) ~= "table" then return false, "BanditClusters unavailable" end

    local site = context.site
    local applied = 0
    local changed = 0
    for _, member in ipairs(Population.ListMembers(site)) do
        if type(member.assignment) == "table" then
            local brain = brainForMember(site, member)
            if brain then
                applied = applied + 1
                if applyAssignment(brain, member.assignment) then changed = changed + 1 end
            end
        end
    end
    if changed > 0 then
        log("siteId=" .. tostring(site.siteId)
            .. " applied=" .. tostring(applied)
            .. " changed=" .. tostring(changed))
    end
    return true, { applied = applied, changed = changed }
end

log("extended materializer operationsProjection=jobs+dutyTargets")
return Adapter
