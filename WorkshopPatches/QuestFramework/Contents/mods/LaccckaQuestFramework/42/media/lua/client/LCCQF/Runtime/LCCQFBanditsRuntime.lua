require "LCCQF/Core/LCCQFNPCRuntime"
require "LCCQF/Content/LCCQFNPCDefinitions"

local Adapter = {}

local function makeCandidate(player, binding, rangeSq)
    if type(binding) ~= "table" then return nil end

    local definition = LCCQF.NPCRegistry.Get(binding.npcId)
    if not definition or definition.runtime.adapter ~= "Bandits" then return nil end

    -- The current vertical slice contains a stationary quest NPC. Client prompt
    -- discovery therefore does not need to rediscover Bandits' physical
    -- IsoZombie at all. The server synchronizes the interaction anchor alongside
    -- the opaque runtime id, while the real physical entity is validated again
    -- server-side when the player presses E.
    if not definition.stationary then return nil end

    local anchor = LCCQF.NPCRuntime.GetRuntimeAnchor(binding.runtimeId)
    if not anchor then return nil end

    local playerZ = player:getZ()
    if math.abs(anchor.z - playerZ) >= 0.5 then return nil end

    local dx = anchor.x - player:getX()
    local dy = anchor.y - player:getY()
    local distanceSq = dx * dx + dy * dy
    if distanceSq > rangeSq then return nil end

    return {
        npcId = definition.npcId,
        runtimeId = tostring(binding.runtimeId),
        displayNameKey = definition.displayNameKey,
        distanceSq = distanceSq,
        anchorX = anchor.x,
        anchorY = anchor.y,
        anchorZ = anchor.z,
    }
end

function Adapter.FindNearestInteractive(player, range)
    if not player or player:isDead() or player:getVehicle() then return nil end

    local rangeSq = range * range
    local best = nil

    for _, binding in ipairs(LCCQF.NPCRuntime.ExportRuntimeBindings()) do
        local candidate = makeCandidate(player, binding, rangeSq)
        if candidate and (not best or candidate.distanceSq < best.distanceSq) then
            best = candidate
        end
    end

    return best
end

print("[LCCQF][RUNTIME:BANDITS] client discovery=server-anchor physicalLookup=false")
LCCQF.NPCRuntime.RegisterAdapter("Bandits", Adapter)

return Adapter
