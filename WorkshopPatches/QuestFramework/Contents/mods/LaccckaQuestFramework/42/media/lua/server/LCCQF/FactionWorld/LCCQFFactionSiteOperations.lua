-- Server-owned faction site jobs, schedules, infrastructure capabilities and needs.
-- This layer stores plain logical data only. It never treats container counts as stock
-- quantities and never mutates world inventory; physical providers receive projections.
if isClient and isClient() and not (isServer and isServer()) then
    return {}
end

require "LCCQF/LCCQFConstants"
require "LCCQF/Core/LCCQFFactionJobRegistry"
require "LCCQF/FactionWorld/LCCQFFactionSiteRegistry"
require "LCCQF/FactionWorld/LCCQFFactionSitePopulation"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Jobs = LCCQF.FactionJobRegistry
local Sites = LCCQF.FactionSiteRegistry
local Population = LCCQF.FactionSitePopulation
local Operations = LCCQF.FactionSiteOperations or {}

local function worldHours()
    if not getGameTime then return 0 end
    local gameTime = getGameTime()
    return gameTime and gameTime.getWorldAgeHours and (tonumber(gameTime:getWorldAgeHours()) or 0) or 0
end

local function copyPoint(point)
    if type(point) ~= "table" then return nil end
    local x, y, z = tonumber(point.x), tonumber(point.y), tonumber(point.z)
    if x == nil or y == nil then return nil end
    return { x = x, y = y, z = z or 0 }
end

local function pointEqual(a, b)
    if a == nil or b == nil then return a == b end
    return tonumber(a.x) == tonumber(b.x)
        and tonumber(a.y) == tonumber(b.y)
        and tonumber(a.z or 0) == tonumber(b.z or 0)
end

local function currentMembers(site)
    local members = Population.ListMembers(site)
    table.sort(members, function(a, b) return tostring(a.npcId) < tostring(b.npcId) end)
    return members
end

local function roleMembers(site, roleId)
    local out = {}
    for _, member in ipairs(currentMembers(site)) do
        if member.roleId == roleId then out[#out + 1] = member end
    end
    return out
end

local function memberOrdinal(site, npcId)
    for index, member in ipairs(currentMembers(site)) do
        if member.npcId == npcId then return index end
    end
    return 1
end

local POINT_KIND = {
    bed = "beds",
    water = "water",
    storage = "storage",
    food = "food",
    spawn = "spawn",
}

function Operations.ResolveTarget(site, targetKind, ordinal)
    if type(site) ~= "table" then return nil end
    targetKind = tostring(targetKind or "home")
    ordinal = math.max(1, math.floor(tonumber(ordinal) or 1))

    local points = site.derived and site.derived.points or nil
    local sourceKind = POINT_KIND[targetKind]
    local list = sourceKind and type(points) == "table" and points[sourceKind] or nil
    if type(list) == "table" and #list > 0 then
        return copyPoint(list[((ordinal - 1) % #list) + 1])
    end

    if targetKind == "home" and type(points) == "table" and type(points.spawn) == "table" and #points.spawn > 0 then
        return copyPoint(points.spawn[((ordinal - 1) % #points.spawn) + 1])
    end
    return copyPoint(site.anchor)
end

local function hourInside(hour, startHour, endHour)
    hour = tonumber(hour) or 0
    startHour = tonumber(startHour) or 0
    endHour = tonumber(endHour) or 0
    if startHour < endHour then return hour >= startHour and hour < endHour end
    return hour >= startHour or hour < endHour
end

local function scheduledJob(roleProfile, hour)
    for index, entry in ipairs(roleProfile.schedule or {}) do
        if hourInside(hour, entry.startHour, entry.endHour) then
            return entry.jobId, entry.targetKind, "schedule:" .. tostring(index)
        end
    end
    return nil, nil, nil
end

local function rotatedJob(site, member, roleProfile, nowHours)
    local rotation = roleProfile.rotation
    local sameRole = roleMembers(site, member.roleId)
    local count = #sameRole
    if count < 1 then return rotation.offJobId, rotation.offTargetKind, "rotation:empty" end

    local roleIndex = 1
    for index, candidate in ipairs(sameRole) do
        if candidate.npcId == member.npcId then roleIndex = index break end
    end
    local shiftHours = math.max(0.25, tonumber(rotation.shiftHours) or 12)
    local epoch = math.floor((tonumber(nowHours) or 0) / shiftHours)
    local rotatedSlot = ((roleIndex - 1 - epoch) % count) + 1
    local activeCount = math.min(count, math.max(1, math.floor(tonumber(rotation.activeCount) or 1)))
    local active = rotatedSlot <= activeCount
    return active and rotation.jobId or rotation.offJobId,
        active and rotation.targetKind or rotation.offTargetKind,
        "rotation:" .. tostring(epoch) .. ":" .. tostring(rotatedSlot)
end

local function buildAssignment(site, member, profile, nowHours)
    local roleProfile = profile.roles and profile.roles[member.roleId] or nil
    if type(roleProfile) ~= "table" then return nil end

    local jobId, targetKind, scheduleKey
    if roleProfile.schedule then
        jobId, targetKind, scheduleKey = scheduledJob(roleProfile, (nowHours or 0) % 24)
    else
        jobId, targetKind, scheduleKey = rotatedJob(site, member, roleProfile, nowHours)
    end
    local job = Jobs.Get(jobId)
    if not job then return nil end

    targetKind = targetKind or job.targetKind
    local target = Operations.ResolveTarget(site, targetKind, memberOrdinal(site, member.npcId))
    return {
        schemaVersion = 1,
        siteId = site.siteId,
        jobId = job.jobId,
        dutyMode = job.dutyMode,
        targetKind = targetKind,
        target = target,
        scheduleKey = scheduleKey,
        assignedWorldHours = nowHours,
    }
end

local function assignmentEqual(a, b)
    if type(a) ~= "table" or type(b) ~= "table" then return a == b end
    return a.siteId == b.siteId
        and a.jobId == b.jobId
        and a.dutyMode == b.dutyMode
        and a.targetKind == b.targetKind
        and a.scheduleKey == b.scheduleKey
        and pointEqual(a.target, b.target)
end

local function buildCapabilities(site)
    local counts = site.derived and site.derived.counts or {}
    local members = currentMembers(site)
    local roleCounts = {}
    for _, member in ipairs(members) do
        roleCounts[member.roleId] = (roleCounts[member.roleId] or 0) + 1
    end
    return {
        livingPopulation = #members,
        beds = math.max(0, math.floor(tonumber(counts.beds) or 0)),
        waterSources = math.max(0, math.floor(tonumber(counts.water) or 0)),
        storageContainers = math.max(0, math.floor(tonumber(counts.storage) or 0)),
        foodAccessContainers = math.max(0, math.floor(tonumber(counts.food) or 0)),
        freeSpawnPoints = math.max(0, math.floor(tonumber(counts.freeSpawnPoints) or 0)),
        roleCounts = roleCounts,
    }
end

local function buildNeeds(capabilities)
    return {
        housingDeficit = math.max(0, capabilities.livingPopulation - capabilities.beds),
        waterSourceMissing = capabilities.waterSources < 1,
        storageMissing = capabilities.storageContainers < 1,
        foodAccessMissing = capabilities.foodAccessContainers < 1,
    }
end

local function scalarTableEqual(a, b)
    if type(a) ~= "table" or type(b) ~= "table" then return a == b end
    for key, value in pairs(a) do
        if type(value) ~= "table" and b[key] ~= value then return false end
    end
    for key, value in pairs(b) do
        if type(value) ~= "table" and a[key] ~= value then return false end
    end
    return true
end

local function roleCountsEqual(a, b)
    a = type(a) == "table" and a or {}
    b = type(b) == "table" and b or {}
    for key, value in pairs(a) do if b[key] ~= value then return false end end
    for key, value in pairs(b) do if a[key] ~= value then return false end end
    return true
end

local function capabilityEqual(a, b)
    return scalarTableEqual(a, b)
        and roleCountsEqual(a and a.roleCounts, b and b.roleCounts)
end

local SIGNALS = {
    { key = "housing", field = "housingDeficit", numeric = true },
    { key = "water", field = "waterSourceMissing" },
    { key = "storage", field = "storageMissing" },
    { key = "food_access", field = "foodAccessMissing" },
}

local function signalValue(spec, needs)
    local value = needs[spec.field]
    if spec.numeric then return math.max(0, tonumber(value) or 0) end
    return value == true and 1 or 0
end

local function updateSignals(site, operations, needs, nowHours)
    operations.signals = type(operations.signals) == "table" and operations.signals or {}
    local changed = false
    for _, spec in ipairs(SIGNALS) do
        local signalId = tostring(site.siteId) .. ":need:" .. spec.key
        local value = signalValue(spec, needs)
        local shouldOpen = value > 0
        local signal = operations.signals[signalId]
        if type(signal) ~= "table" then
            signal = {
                signalId = signalId,
                kind = spec.key,
                status = shouldOpen and "OPEN" or "RESOLVED",
                value = value,
                revision = 1,
            }
            if shouldOpen then signal.raisedWorldHours = nowHours else signal.resolvedWorldHours = nowHours end
            operations.signals[signalId] = signal
            changed = true
        elseif signal.status ~= (shouldOpen and "OPEN" or "RESOLVED") or tonumber(signal.value) ~= value then
            signal.status = shouldOpen and "OPEN" or "RESOLVED"
            signal.value = value
            signal.revision = math.max(0, math.floor(tonumber(signal.revision) or 0)) + 1
            signal.changedWorldHours = nowHours
            if shouldOpen then signal.raisedWorldHours = nowHours else signal.resolvedWorldHours = nowHours end
            changed = true
        end
    end
    return changed
end

function Operations.Ensure(site)
    if type(site) ~= "table" then return nil end
    if type(site.operations) ~= "table" then
        site.operations = {
            schemaVersion = 1,
            revision = 0,
            capabilities = {},
            needs = {},
            signals = {},
        }
    end
    site.operations.schemaVersion = 1
    site.operations.revision = math.max(0, math.floor(tonumber(site.operations.revision) or 0))
    return site.operations
end

function Operations.UpdateSite(site, definition)
    if type(site) ~= "table" or type(definition) ~= "table" then return false, "invalid operations input" end
    local profile = definition.operationsProfile
    if type(profile) ~= "table" or profile.enabled == false then return false, "operations disabled" end
    local valid, errorText = Jobs.ValidateOperationsProfile(profile)
    if not valid then return false, errorText end

    local operations = Operations.Ensure(site)
    local nowHours = worldHours()
    local changed = false

    local capabilities = buildCapabilities(site)
    local needs = buildNeeds(capabilities)
    if not capabilityEqual(operations.capabilities, capabilities) then
        operations.capabilities = capabilities
        changed = true
    end
    if not scalarTableEqual(operations.needs, needs) then
        operations.needs = needs
        changed = true
    end
    if updateSignals(site, operations, needs, nowHours) then changed = true end

    for _, member in ipairs(currentMembers(site)) do
        local nextAssignment = buildAssignment(site, member, profile, nowHours)
        if nextAssignment and not assignmentEqual(member.assignment, nextAssignment) then
            local previousRevision = type(member.assignment) == "table" and tonumber(member.assignment.revision) or 0
            nextAssignment.revision = math.max(0, math.floor(previousRevision or 0)) + 1
            member.assignment = nextAssignment
            changed = true
        end
    end

    if changed then
        operations.revision = operations.revision + 1
        operations.changedWorldHours = nowHours
        Sites.MarkDirty(site.siteId, "faction site operations updated")
    end
    operations.lastEvaluatedWorldHours = nowHours
    return true, changed
end

function Operations.GetAssignment(siteOrId, npcId)
    local member = Population.GetMemberByNpcId(siteOrId, npcId)
    return member and member.assignment or nil
end

function Operations.GetOpenSignals(siteOrId)
    local site = type(siteOrId) == "table" and siteOrId or Sites.GetSite(siteOrId)
    local out = {}
    local signals = site and site.operations and site.operations.signals or {}
    for _, signal in pairs(type(signals) == "table" and signals or {}) do
        if type(signal) == "table" and signal.status == "OPEN" then out[#out + 1] = signal end
    end
    table.sort(out, function(a, b) return tostring(a.signalId) < tostring(b.signalId) end)
    return out
end

LCCQF.FactionSiteOperations = Operations
return Operations
