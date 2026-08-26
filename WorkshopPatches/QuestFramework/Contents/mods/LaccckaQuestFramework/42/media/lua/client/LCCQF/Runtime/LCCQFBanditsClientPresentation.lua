require "LCCQF/Core/LCCQFNPCRuntime"

local ok, BanditZombieModule = pcall(require, "BanditZombie")
if not ok then
    print("[LCCQF][PRESENTATION:CLIENT] BanditZombie unavailable; live NPC portraits disabled")
    return false
end

local resolver = {}

function resolver.Resolve(npcId, runtimeId, definition)
    if not BanditZombie or type(BanditZombie.GetInstanceById) ~= "function" then return nil end

    local numericId = tonumber(runtimeId)
    local entity = numericId and BanditZombie.GetInstanceById(numericId) or nil
    if not entity then
        entity = BanditZombie.GetInstanceById(runtimeId)
    end
    if not entity or (entity.isDead and entity:isDead()) then return nil end
    return entity
end

LCCQF.NPCRuntime.RegisterClientResolver("Bandits", resolver)
print("[LCCQF][PRESENTATION:CLIENT] Bandits live portrait resolver registered")
return true
