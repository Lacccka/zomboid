require "LCCQF/Core/LCCQFNPCRegistry"

LCCQF = LCCQF or {}

local Runtime = LCCQF.NPCRuntime or {}
local adapters = Runtime.adapters or {}
local clientResolvers = Runtime.clientResolvers or {}
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
    if number == nil or number ~= number or number == math.huge or number == -math.huge then return nil end
    return number
end

local function normalizeAnchor(anchor)
    if type(anchor) ~= "table" then return nil end
    local x, y, z = finiteNumber(anchor.x), finiteNumber(anchor.y), finiteNumber(anchor.z)
    if x == nil or y == nil or z == nil then return nil end
    return { x = x, y = y, z = z }
end

local function clearRuntimeKey(key)
    if not key then return end
    local npcId = runtimeBindings[key]
    runtimeBindings[key] = nil
    runtimeAnchors[key] = nil
    if npcId and activeRuntimeByNPCId[npcId] == key then activeRuntimeByNPCId[npcId] = nil end
end

function Runtime.RegisterAdapter(adapterId, adapter)
    if type(adapterId) ~= "string" or adapterId == "" or type(adapter) ~= "table" then return false end
    adapters[adapterId] = adapter
    return true
end

function Runtime.RegisterClientResolver(adapterId, resolver)
    if type(adapterId) ~= "string" or adapterId == "" or type(resolver) ~= "table"
        or type(resolver.Resolve) ~= "function"
    then return false end
    clientResolvers[adapterId] = resolver
    return true
end

function Runtime.GetAdapter(adapterId) return adapters[adapterId] end

function Runtime.GetAdapterForNPC(npcId)
    local definition = LCCQF.NPCRegistry.Get(npcId)
    if not definition then return nil, nil end
    return adapters[definition.runtime.adapter], definition
end

function Runtime.ResolveClientEntity(npcId)
    local definition = LCCQF.NPCRegistry.Get(npcId)
    if not definition or type(definition.runtime) ~= "table" then return nil end
    local resolver = clientResolvers[definition.runtime.adapter]
    if not resolver then return nil end
    local runtimeId = Runtime.GetActiveRuntimeId(npcId)
    if not runtimeId then return nil end
    local ok, entity = pcall(resolver.Resolve, npcId, runtimeId, definition)
    if not ok then return nil end
    return entity
end

function Runtime.BindRuntime(runtimeId, npcId, anchor)
    local key = normalizeRuntimeId(runtimeId)
    if not key or type(npcId) ~= "string" then return false end

    -- On clients, generated world NPCs are learned only when the server exposes a
    -- physical runtime binding. The projection contains no faction/site/population state.
    if not LCCQF.NPCRegistry.IsRegistered(npcId)
        and type(anchor) == "table" and type(anchor.publicDefinition) == "table"
    then
        LCCQF.NPCRegistry.ApplyPublicDefinition(anchor.publicDefinition)
    end
    if not LCCQF.NPCRegistry.IsRegistered(npcId) then return false end

    local previousNpcId = runtimeBindings[key]
    if previousNpcId and previousNpcId ~= npcId and activeRuntimeByNPCId[previousNpcId] == key then
        activeRuntimeByNPCId[previousNpcId] = nil
    end
    local previousRuntimeId = activeRuntimeByNPCId[npcId]
    if previousRuntimeId and previousRuntimeId ~= key then clearRuntimeKey(previousRuntimeId) end

    runtimeBindings[key] = npcId
    activeRuntimeByNPCId[npcId] = key
    local normalizedAnchor = normalizeAnchor(anchor)
    if normalizedAnchor then runtimeAnchors[key] = normalizedAnchor end
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
            publicDefinition = LCCQF.NPCRegistry.MakePublicDefinition(npcId),
        }
        local anchor = runtimeAnchors[runtimeId]
        if anchor then entry.x, entry.y, entry.z = anchor.x, anchor.y, anchor.z end
        result[#result + 1] = entry
    end
    return result
end

function Runtime.ReplaceRuntimeBindings(entries)
    for runtimeId in pairs(runtimeBindings) do runtimeBindings[runtimeId] = nil end
    for runtimeId in pairs(runtimeAnchors) do runtimeAnchors[runtimeId] = nil end
    for npcId in pairs(activeRuntimeByNPCId) do activeRuntimeByNPCId[npcId] = nil end

    if type(entries) == "table" then
        for _, entry in ipairs(entries) do
            if type(entry) == "table" then Runtime.BindRuntime(entry.runtimeId, entry.npcId, entry) end
        end
    end
    local count = 0
    for _ in pairs(runtimeBindings) do count = count + 1 end
    return count
end

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

function Runtime.FindNearestInteractive(player, range)
    if not player or player:isDead() then return nil end
    local numericRange = finiteNumber(range)
    if not numericRange or numericRange <= 0 then return nil end
    local px, py, pz = player:getX(), player:getY(), player:getZ()
    local rangeSq, best = numericRange * numericRange, nil

    for runtimeId, npcId in pairs(runtimeBindings) do
        local definition = LCCQF.NPCRegistry.Get(npcId)
        local anchor = runtimeAnchors[runtimeId]
        if definition and anchor and definition.interactive ~= false then
            local dz = math.abs(anchor.z - pz)
            if dz < 0.5 then
                local dx, dy = anchor.x - px, anchor.y - py
                local distanceSq = dx * dx + dy * dy
                if distanceSq <= rangeSq and (not best or distanceSq < best.distanceSq) then
                    best = {
                        npcId = definition.npcId, runtimeId = runtimeId,
                        displayNameKey = definition.displayNameKey, distanceSq = distanceSq,
                        anchorX = anchor.x, anchorY = anchor.y, anchorZ = anchor.z,
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
    if not adapter or not adapter.Spawn then return nil, "runtime adapter unavailable" end
    if definition.spawnable == false then return nil, "NPC is not manually spawnable" end
    return adapter.Spawn(player, definition)
end

Runtime.adapters = adapters
Runtime.clientResolvers = clientResolvers
Runtime.runtimeBindings = runtimeBindings
Runtime.runtimeAnchors = runtimeAnchors
Runtime.activeRuntimeByNPCId = activeRuntimeByNPCId
LCCQF.NPCRuntime = Runtime
return Runtime
