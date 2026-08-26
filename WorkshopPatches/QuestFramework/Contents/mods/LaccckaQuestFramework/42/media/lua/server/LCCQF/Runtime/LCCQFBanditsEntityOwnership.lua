require "BanditBrain"
require "BanditUtils"
require "LCCQF/Core/LCCQFNPCRuntime"
require "LCCQF/Core/LCCQFNPCRegistry"
require "LCCQF/Runtime/LCCQFBanditsServerRuntime"

local adapter = LCCQF.NPCRuntime.GetAdapter("Bandits")
local NPC_ID_FIELD = "lccqNpcId"

local function getBrain(entity)
    if not entity then return nil end
    local brain = BanditBrain.Get(entity)
    if brain then return brain end

    local runtimeId = BanditUtils.GetZombieID(entity)
    local cluster = runtimeId ~= nil and GetBanditClusterData(runtimeId) or nil
    if not cluster then return nil end
    return cluster[runtimeId] or cluster[tostring(runtimeId)]
end

if adapter then
    function adapter.OwnsEntity(entity)
        local brain = getBrain(entity)
        local npcId = brain and brain[NPC_ID_FIELD] or nil
        return type(npcId) == "string" and LCCQF.NPCRegistry.IsRegistered(npcId)
    end
end

return adapter
