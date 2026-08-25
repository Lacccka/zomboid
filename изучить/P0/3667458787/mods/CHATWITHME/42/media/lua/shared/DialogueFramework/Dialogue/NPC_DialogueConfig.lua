local NPC_DialogueConfig = {}

NPC_DialogueConfig.SPECIAL_NODES = {
    EXIT = "EXIT",
    BACK = "BACK"
}

NPC_DialogueConfig.NODE_TYPES = {
    STANDARD = "standard",
    NPC_RESPONSE = "npc_response",
    NPC_TRADING = "npc_trading",
    NPC_INFO = "npc_info"
}

NPC_DialogueConfig.TIMING = {
    FADE_DURATION = 0.3,
    MIN_RESPONSE_DURATION = 1.5,
    DEFAULT_DISPLAY_DURATION = 3.0
}

NPC_DialogueConfig.FEATURES = {
    NPC_RESPONSE_NODES_ENABLED = true
}

NPC_DialogueConfig.RESUME_SYSTEM = {
    ENABLED = true,
    AUTO_CLEANUP_DAYS = 7,
    MAX_VISITED_NODES = 20,
    ALLOW_TRADING_NODE_RESUME = false,
    SHOW_RESUME_NOTIFICATION = false
}

NPC_DialogueConfig.SESSION_IDS = {
    MUGGY_GREETING_FIRST = "muggy_greeting_first",
    MUGGY_GREETING_REPEAT = "muggy_greeting_repeat"
}

NPC_DialogueConfig.NPC_IDS = {
    MUGGY = "muggy"
}

NPC_DialogueConfig.SOUND_PREFIX = {
    MUGGY = "Muggy_"
}

function NPC_DialogueConfig.isSpecialNode(nodeID)
    return nodeID == NPC_DialogueConfig.SPECIAL_NODES.EXIT
        or nodeID == NPC_DialogueConfig.SPECIAL_NODES.BACK
end

function NPC_DialogueConfig.isExitNode(nodeID)
    return nodeID == NPC_DialogueConfig.SPECIAL_NODES.EXIT
end

function NPC_DialogueConfig.isBackNode(nodeID)
    return nodeID == NPC_DialogueConfig.SPECIAL_NODES.BACK
end

function NPC_DialogueConfig.getNodeType(node)
    if not node then
        return nil
    end

    if not NPC_DialogueConfig.FEATURES.NPC_RESPONSE_NODES_ENABLED then
        return NPC_DialogueConfig.NODE_TYPES.STANDARD
    end

    if node.nodeType then
        return node.nodeType
    end

    if node.options and #node.options > 0 then
        return NPC_DialogueConfig.NODE_TYPES.STANDARD
    elseif node.nextNode then
        return NPC_DialogueConfig.NODE_TYPES.NPC_RESPONSE
    end

    return NPC_DialogueConfig.NODE_TYPES.STANDARD
end

function NPC_DialogueConfig.isNPCResponseNode(node)
    return NPC_DialogueConfig.getNodeType(node) == NPC_DialogueConfig.NODE_TYPES.NPC_RESPONSE
end

function NPC_DialogueConfig.isStandardNode(node)
    return NPC_DialogueConfig.getNodeType(node) == NPC_DialogueConfig.NODE_TYPES.STANDARD
end

function NPC_DialogueConfig.isTradingNode(node)
    return NPC_DialogueConfig.getNodeType(node) == NPC_DialogueConfig.NODE_TYPES.NPC_TRADING
end

function NPC_DialogueConfig.isInfoNode(node)
    return NPC_DialogueConfig.getNodeType(node) == NPC_DialogueConfig.NODE_TYPES.NPC_INFO
end

function NPC_DialogueConfig.getDefaultDisplayDuration()
    return NPC_DialogueConfig.TIMING.DEFAULT_DISPLAY_DURATION
end

return NPC_DialogueConfig
