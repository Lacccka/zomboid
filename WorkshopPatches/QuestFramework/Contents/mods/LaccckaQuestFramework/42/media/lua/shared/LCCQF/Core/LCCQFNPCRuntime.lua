require "LCCQF/Core/LCCQFNPCRegistry"

LCCQF = LCCQF or {}

local Runtime = LCCQF.NPCRuntime or {}
local adapters = Runtime.adapters or {}
local runtimeBindings = Runtime.runtimeBindings or {}
local runtimeAnchors = Runtime.runtimeAnchors or {}

local function normalizeRuntimeId(runtimeId)
    if runtimeId == nil then return nil end
    local value = tostring(runtimeId)
    if value == "" then return nil end
    return value
end

local function finiteNumber(value)
    local number = tonumber(value)
    if number == nil or number ~= number or number == math.huge or number == -math.huge then
        return nil
    end
    return number
end

local function normalizeAnchor(anchor)
    if type(anchor) ~= "table" then return nil end

    local x = finiteNumber(anchor.x)
    local y = finiteNumber(anchor.y)
    local z = finiteNumber(anchor.z)
    if x == nil or y == nil or z == nil then return nil end

    return { x = x, y = y, z = z }
end

function Runtime.RegisterAdapter(adapterId, adapter)
    if type(adapterId) ~= "string" or adapterId == "" or type(adapter) ~= "table" then
        return false
    end
    adapters[adapterId] = adapter
    return true
end

function Runtime.GetAdapter(adapterId)
    return adapters[adapterId]
end

function Runtime.GetAdapterForNPC(npcId)
    local definition = LCCQF.NPCRegistry.Get(npcId)
    if not definition then return nil, nil end
    return adapters[definition.runtime.adapter], definition
end

function Runtime.BindRuntime(runtimeId, npcId, anchor)
    local key = normalizeRuntimeId(runtimeId)
    if not key or type(npcId) ~= "string" or not LCCQF.NPCRegistry.IsRegistered(npcId) then
        return false
    end

    runtimeBindings[key] = npcId

    local normalizedAnchor = normalizeAnchor(anchor)
    if normalizedAnchor then
        runtimeAnchors[key] = normalizedAnchor
    end

    return true
end

function Runtime.UnbindRuntime(runtimeId, npcId)
    local key = normalizeRuntimeId(runtimeId)
    if not key then return false end
    if npcId ~= nil and runtimeBindings[key] ~= npcId then return false end

    runtimeBindings[key] = nil
    runtimeAnchors[key] = nil
    return true
end

function Runtime.GetBoundNPCId(runtimeId)
    local key = normalizeRuntimeId(runtimeId)
    return key and runtimeBindings[key] or nil
end

function Runtime.GetRuntimeAnchor(runtimeId)
    local key = normalizeRuntimeId(runtimeId)
    return key and runtimeAnchors[key] or nil
end

function Runtime.ExportRuntimeBindings()
    local result = {}
    for runtimeId, npcId in pairs(runtimeBindings) do
        local entry = {
            runtimeId = runtimeId,
            npcId = npcId,
        }
        local anchor = runtimeAnchors[runtimeId]
        if anchor then
            entry.x = anchor.x
            entry.y = anchor.y
            entry.z = anchor.z
        end
        result[#result + 1] = entry
    end
    return result
end

function Runtime.ReplaceRuntimeBindings(entries)
    for runtimeId in pairs(runtimeBindings) do
        runtimeBindings[runtimeId] = nil
    end
    for runtimeId in pairs(runtimeAnchors) do
        runtimeAnchors[runtimeId] = nil
    end

    local count = 0
    if type(entries) ~= "table" then return count end
    for _, entry in ipairs(entries) do
        if type(entry) == "table" and Runtime.BindRuntime(entry.runtimeId, entry.npcId, entry) then
            count = count + 1
        end
    end
    return count
end

function Runtime.FindNearestInteractive(player, range)
    local best = nil
    for _, adapter in pairs(adapters) do
        if adapter.FindNearestInteractive then
            local candidate = adapter.FindNearestInteractive(player, range)
            if candidate and (not best or candidate.distanceSq < best.distanceSq) then
                best = candidate
            end
        end
    end
    return best
end

function Runtime.ResolveForPlayer(player, npcId, runtimeId, range)
    local adapter, definition = Runtime.GetAdapterForNPC(npcId)
    if not adapter or not adapter.ResolveForPlayer then return nil, definition end
    return adapter.ResolveForPlayer(player, definition, runtimeId, range), definition
end

function Runtime.Spawn(player, npcId)
    local adapter, definition = Runtime.GetAdapterForNPC(npcId)
    if not adapter or not adapter.Spawn then
        return nil, "runtime adapter unavailable"
    end
    return adapter.Spawn(player, definition)
end

Runtime.adapters = adapters
Runtime.runtimeBindings = runtimeBindings
Runtime.runtimeAnchors = runtimeAnchors
LCCQF.NPCRuntime = Runtime

return Runtime
