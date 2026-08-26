require "LCCQF/Core/LCCQFNPCRegistry"

LCCQF = LCCQF or {}

local Runtime = LCCQF.NPCRuntime or {}
local adapters = Runtime.adapters or {}
local runtimeBindings = Runtime.runtimeBindings or {}
local runtimeAnchors = Runtime.runtimeAnchors or {}
local activeRuntimeByNPCId = Runtime.activeRuntimeByNPCId or {}

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

local function clearRuntimeKey(key)
    if not key then return end

    local npcId = runtimeBindings[key]
    runtimeBindings[key] = nil
    runtimeAnchors[key] = nil

    if npcId and activeRuntimeByNPCId[npcId] == key then
        activeRuntimeByNPCId[npcId] = nil
    end
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

    local previousNpcId = runtimeBindings[key]
    if previousNpcId and previousNpcId ~= npcId and activeRuntimeByNPCId[previousNpcId] == key then
        activeRuntimeByNPCId[previousNpcId] = nil
    end

    local previousRuntimeId = activeRuntimeByNPCId[npcId]
    if previousRuntimeId and previousRuntimeId ~= key then
        clearRuntimeKey(previousRuntimeId)
    end

    runtimeBindings[key] = npcId
    activeRuntimeByNPCId[npcId] = key

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

    clearRuntimeKey(key)
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

function Runtime.GetActiveRuntimeId(npcId)
    if type(npcId) ~= "string" then return nil end
    return activeRuntimeByNPCId[npcId]
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
    for npcId in pairs(activeRuntimeByNPCId) do
        activeRuntimeByNPCId[npcId] = nil
    end

    if type(entries) == "table" then
        for _, entry in ipairs(entries) do
            if type(entry) == "table" then
                Runtime.BindRuntime(entry.runtimeId, entry.npcId, entry)
            end
        end
    end

    local count = 0
    for _ in pairs(runtimeBindings) do
        count = count + 1
    end
    return count
end

-- Provider-neutral classification for systems that must distinguish physical
-- NPC implementations from ordinary world entities (for example Kill/ClearArea).
-- Each adapter decides whether it owns the concrete entity; quest code never
-- imports Bandits2 or another provider directly.
function Runtime.IsFrameworkEntity(entity)
    if not entity then return false end
    for _, adapter in pairs(adapters) do
        if type(adapter) == "table" and type(adapter.OwnsEntity) == "function" then
            local ok, owned = pcall(adapter.OwnsEntity, entity)
            if ok and owned == true then return true end
        end
    end
    return false
end

-- Client prompt discovery is provider-neutral. A synchronized framework binding
-- plus its server-owned interaction anchor is sufficient to select a candidate.
-- Provider adapters are reserved for authoritative Spawn/ResolveForPlayer work.
function Runtime.FindNearestInteractive(player, range)
    if not player or player:isDead() then return nil end

    local numericRange = finiteNumber(range)
    if not numericRange or numericRange <= 0 then return nil end

    local px = player:getX()
    local py = player:getY()
    local pz = player:getZ()
    local rangeSq = numericRange * numericRange
    local best = nil

    for runtimeId, npcId in pairs(runtimeBindings) do
        local definition = LCCQF.NPCRegistry.Get(npcId)
        local anchor = runtimeAnchors[runtimeId]

        if definition and anchor and definition.interactive ~= false then
            local dz = math.abs(anchor.z - pz)
            if dz < 0.5 then
                local dx = anchor.x - px
                local dy = anchor.y - py
                local distanceSq = dx * dx + dy * dy

                if distanceSq <= rangeSq and (not best or distanceSq < best.distanceSq) then
                    best = {
                        npcId = definition.npcId,
                        runtimeId = runtimeId,
                        displayNameKey = definition.displayNameKey,
                        distanceSq = distanceSq,
                        anchorX = anchor.x,
                        anchorY = anchor.y,
                        anchorZ = anchor.z,
                    }
                end
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
Runtime.activeRuntimeByNPCId = activeRuntimeByNPCId
LCCQF.NPCRuntime = Runtime

return Runtime
