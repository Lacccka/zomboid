require "LCCQF/Core/LCCQFNPCRegistry"

LCCQF = LCCQF or {}

local Runtime = LCCQF.NPCRuntime or {}
local adapters = Runtime.adapters or {}

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
LCCQF.NPCRuntime = Runtime

return Runtime
