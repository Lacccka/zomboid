local NPC_BehaviorDefinitionRegistry = {}

local NPC_BehaviorConfig = require("DialogueFramework/Behavior/NPC_BehaviorConfig")

NPC_BehaviorDefinitionRegistry.definitions = {}

function NPC_BehaviorDefinitionRegistry.register(behaviorDef)
    if not behaviorDef or not behaviorDef.id then
        return false
    end

    NPC_BehaviorDefinitionRegistry.definitions[behaviorDef.id] = behaviorDef
    return true
end

function NPC_BehaviorDefinitionRegistry.get(behaviorID)
    return NPC_BehaviorDefinitionRegistry.definitions[behaviorID]
end

function NPC_BehaviorDefinitionRegistry.isRegistered(behaviorID)
    return NPC_BehaviorDefinitionRegistry.definitions[behaviorID] ~= nil
end

function NPC_BehaviorDefinitionRegistry.getAllBehaviors()
    local behaviors = {}
    for id, def in pairs(NPC_BehaviorDefinitionRegistry.definitions) do
        table.insert(behaviors, def)
    end
    return behaviors
end

function NPC_BehaviorDefinitionRegistry.unregister(behaviorID)
    if not behaviorID then return false end

    NPC_BehaviorDefinitionRegistry.definitions[behaviorID] = nil
    return true
end

NPC_BehaviorDefinitionRegistry.register({
    id = "placeholder",
    name = "Placeholder Behavior",
    moduleFile = "DialogueFramework/Behavior/Behaviors/NPC_Behavior_Placeholder",
    requiresServer = false,
    canQueue = true,
    priority = NPC_BehaviorConfig.PRIORITY.LOW,
    supportsAsync = false,
    params = {}
})

NPC_BehaviorDefinitionRegistry.register({
    id = "follow_player",
    name = "Follow Player",
    moduleFile = "DialogueFramework/Behavior/Behaviors/NPC_Behavior_FollowPlayer",
    requiresServer = true,
    canQueue = true,
    priority = NPC_BehaviorConfig.PRIORITY.MEDIUM,
    supportsAsync = false,
    params = {
        player = "IsoPlayer"
    }
})

NPC_BehaviorDefinitionRegistry.register({
    id = "guarding",
    name = "Zone Guarding",
    moduleFile = "DialogueFramework/Behavior/Behaviors/NPC_Behavior_Guarding",
    requiresServer = true,
    canQueue = true,
    priority = NPC_BehaviorConfig.PRIORITY.MEDIUM,
    supportsAsync = false,
    params = {
        zone = "table",
        playerID = "string"
    }
})

NPC_BehaviorDefinitionRegistry.register({
    id = "attacking",
    name = "Combat Attack",
    moduleFile = "DialogueFramework/Behavior/Behaviors/NPC_Behavior_Attacking",
    requiresServer = true,
    canQueue = true,
    priority = NPC_BehaviorConfig.PRIORITY.HIGH,
    supportsAsync = false,
    params = {
        zone = "table",
        attackConfig = "table"
    }
})

NPC_BehaviorDefinitionRegistry.register({
    id = "idlechilling",
    name = "Idle Chilling",
    moduleFile = "DialogueFramework/Behavior/Behaviors/NPC_Behavior_IdleChilling",
    requiresServer = true,
    canQueue = true,
    priority = NPC_BehaviorConfig.PRIORITY.LOW,
    supportsAsync = false,
    params = {
        zone = "table"
    }
})

NPC_BehaviorDefinitionRegistry.register({
    id = "talking",
    name = "Dialogue Talking",
    moduleFile = "DialogueFramework/Behavior/Behaviors/NPC_Behavior_Talking",
    requiresServer = true,
    canQueue = true,
    priority = NPC_BehaviorConfig.PRIORITY.TALKING,
    supportsAsync = false,
    params = {
        player = "IsoPlayer"
    }
})

return NPC_BehaviorDefinitionRegistry
