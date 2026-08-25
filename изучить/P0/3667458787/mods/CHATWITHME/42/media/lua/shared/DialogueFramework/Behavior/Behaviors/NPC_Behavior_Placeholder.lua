local NPC_Behavior_Placeholder = {}

function NPC_Behavior_Placeholder.execute(npc, params, player)
    if not npc then
        return false, "NPC is nil"
    end

    if npc.setVariable then
        npc:setVariable("PlaceholderExecuted", true)
    end

    return true, "Placeholder behavior executed successfully"
end

function NPC_Behavior_Placeholder.canExecute(npc, params)
    return npc ~= nil
end

function NPC_Behavior_Placeholder.onComplete(npc, params, result)
end

function NPC_Behavior_Placeholder.onFailed(npc, params, reason)
end

return NPC_Behavior_Placeholder
