require "LCCQF/Core/LCCQFNPCRuntime"
require "LCCQF/Runtime/LCCQFBanditsEntityOwnership"
require "LCCQF/Quest/LCCQFQuestService"

local function onZombieDead(zombie)
    if not zombie then return end
    if LCCQF.NPCRuntime.IsFrameworkEntity(zombie) then return end
    LCCQF.QuestService.NotifyZombieDead(zombie)
end

if isServer and isServer() and Events.OnZombieDead then
    Events.OnZombieDead.Add(onZombieDead)
end

return true
