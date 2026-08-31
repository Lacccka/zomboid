require "LCCQF/LCCQFConstants"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Registry = LCCQF.FactionJobRegistry or {}
local definitions = Registry.definitions or {}

local function validId(value)
    return type(value) == "string" and value ~= "" and #value <= C.MAX_IDENTIFIER_LENGTH
end

local function validHour(value)
    local hour = tonumber(value)
    return hour ~= nil and hour >= 0 and hour < 24
end

function Registry.Register(definition)
    if type(definition) ~= "table" then return false, "job definition must be a table" end
    if not validId(definition.jobId) then return false, "invalid jobId" end
    if not validId(definition.dutyMode) then return false, "invalid dutyMode" end
    if not validId(definition.targetKind) then return false, "invalid targetKind" end
    if definitions[definition.jobId] then return false, "duplicate jobId" end

    definitions[definition.jobId] = {
        jobId = definition.jobId,
        dutyMode = definition.dutyMode,
        targetKind = definition.targetKind,
        physical = definition.physical ~= false,
    }
    return true
end

function Registry.Get(jobId)
    if not validId(jobId) then return nil end
    return definitions[jobId]
end

function Registry.IsRegistered(jobId)
    return Registry.Get(jobId) ~= nil
end

function Registry.List()
    local out = {}
    for _, definition in pairs(definitions) do out[#out + 1] = definition end
    table.sort(out, function(a, b) return a.jobId < b.jobId end)
    return out
end

function Registry.ValidateOperationsProfile(profile)
    if profile == nil then return true end
    if type(profile) ~= "table" then return false, "operationsProfile must be a table" end
    if profile.enabled ~= nil and type(profile.enabled) ~= "boolean" then
        return false, "operationsProfile.enabled must be boolean"
    end
    if profile.enabled == false then return true end
    if type(profile.roles) ~= "table" then return false, "operationsProfile.roles must be a table" end

    for roleId, roleProfile in pairs(profile.roles) do
        if not validId(roleId) or type(roleProfile) ~= "table" then
            return false, "invalid operationsProfile role"
        end
        local hasSchedule = roleProfile.schedule ~= nil
        local hasRotation = roleProfile.rotation ~= nil
        if hasSchedule == hasRotation then
            return false, "operationsProfile role must define exactly one schedule or rotation"
        end

        if hasSchedule then
            if type(roleProfile.schedule) ~= "table" or #roleProfile.schedule < 1 then
                return false, "operationsProfile schedule must be non-empty"
            end
            for _, entry in ipairs(roleProfile.schedule) do
                if type(entry) ~= "table" or not Registry.IsRegistered(entry.jobId) then
                    return false, "operationsProfile schedule references unknown job"
                end
                if not validHour(entry.startHour) or not validHour(entry.endHour)
                    or tonumber(entry.startHour) == tonumber(entry.endHour)
                then
                    return false, "operationsProfile schedule has invalid hours"
                end
                if entry.targetKind ~= nil and not validId(entry.targetKind) then
                    return false, "operationsProfile schedule has invalid targetKind"
                end
            end
        else
            local rotation = roleProfile.rotation
            if type(rotation) ~= "table"
                or not Registry.IsRegistered(rotation.jobId)
                or not Registry.IsRegistered(rotation.offJobId)
            then
                return false, "operationsProfile rotation references unknown job"
            end
            local shiftHours = tonumber(rotation.shiftHours)
            local activeCount = tonumber(rotation.activeCount)
            if not shiftHours or shiftHours <= 0 or shiftHours > 24 then
                return false, "operationsProfile rotation shiftHours must be > 0 and <= 24"
            end
            if not activeCount or activeCount < 1 or math.floor(activeCount) ~= activeCount then
                return false, "operationsProfile rotation activeCount must be a positive integer"
            end
            if rotation.targetKind ~= nil and not validId(rotation.targetKind) then
                return false, "operationsProfile rotation has invalid targetKind"
            end
            if rotation.offTargetKind ~= nil and not validId(rotation.offTargetKind) then
                return false, "operationsProfile rotation has invalid offTargetKind"
            end
        end
    end

    if profile.supplies ~= nil then
        if type(profile.supplies) ~= "table" then
            return false, "operationsProfile.supplies must be a table"
        end
        for supplyId, supply in pairs(profile.supplies) do
            if not validId(supplyId) or type(supply) ~= "table" or not validId(supply.category) then
                return false, "operationsProfile supply definition is invalid"
            end
            local perResident = tonumber(supply.minimumPerResident) or 0
            local reserve = tonumber(supply.reserve) or 0
            if perResident < 0 or reserve < 0 then
                return false, "operationsProfile supply thresholds must be non-negative"
            end
        end
    end
    return true
end

Registry.definitions = definitions
LCCQF.FactionJobRegistry = Registry
return Registry
