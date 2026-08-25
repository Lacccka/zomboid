local NPC_BehaviorExecutor = {}

function NPC_BehaviorExecutor.sendToServer(npc, behaviorEntry)
    if not isClient() then
        return false, "Not on client"
    end

    local npcID = npc:getOnlineID() or npc:getID()

    sendClientCommand(getPlayer(), "NPCBehavior", "ExecuteBehavior", {
        npcID = npcID,
        behaviorID = behaviorEntry.behaviorID,
        params = behaviorEntry.params
    })

    return true, "Sent to server"
end

return NPC_BehaviorExecutor
