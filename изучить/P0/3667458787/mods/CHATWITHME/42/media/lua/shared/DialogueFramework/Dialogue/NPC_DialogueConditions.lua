local NPC_DialogueConditions = {}

function NPC_DialogueConditions.alwaysTrue()
    return true
end

function NPC_DialogueConditions.alwaysFalse()
    return false
end

function NPC_DialogueConditions.hasItem(player, itemType, count)
    if not player then
        return false
    end

    count = count or 1

    local inventory = player:getInventory()
    if not inventory then
        return false
    end

    local itemCount = inventory:getItemCount(itemType)
    return itemCount >= count
end

function NPC_DialogueConditions.hasItemRecursive(player, itemType)
    if not player then
        return false
    end

    local inventory = player:getInventory()
    if not inventory then
        return false
    end

    local item = inventory:getFirstTypeRecurse(itemType)
    return item ~= nil
end

function NPC_DialogueConditions.hasModDataFlag(player, flagKey)
    if not player then
        return false
    end

    local modData = player:getModData()
    if not modData then
        return false
    end

    return modData[flagKey] == true
end

function NPC_DialogueConditions.notHasModDataFlag(player, flagKey)
    return not NPC_DialogueConditions.hasModDataFlag(player, flagKey)
end

function NPC_DialogueConditions.checkModDataValue(player, key, expectedValue)
    if not player then
        return false
    end

    local modData = player:getModData()
    if not modData then
        return false
    end

    return modData[key] == expectedValue
end

function NPC_DialogueConditions.hasMetNPCBefore(player, npcID)
    if not player then
        return false
    end

    local modData = player:getModData()
    if not modData then
        return false
    end

    if not modData.npcDialogueHistory then
        return false
    end

    return modData.npcDialogueHistory[npcID] ~= nil
end

function NPC_DialogueConditions.createCustomCondition(conditionFunc)
    return conditionFunc
end

function NPC_DialogueConditions.hasCompletedSession(player, npcID, sessionID)
    local NPC_DialogueSessionManager = require("DialogueFramework/Dialogue/NPC_DialogueSessionManager")
    return NPC_DialogueSessionManager.isSessionCompleted(player, npcID, sessionID)
end

return NPC_DialogueConditions
