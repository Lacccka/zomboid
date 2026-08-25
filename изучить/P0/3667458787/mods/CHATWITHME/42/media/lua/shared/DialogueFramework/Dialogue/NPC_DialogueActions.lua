local NPC_DialogueActions = {}

function NPC_DialogueActions.setModDataFlag(player, flagKey, value)
    if not player then
        return false
    end

    local modData = player:getModData()
    if not modData then
        return false
    end

    modData[flagKey] = value or true
    return true
end

function NPC_DialogueActions.removeItem(player, itemType, count)
    if not player then
        return false
    end

    count = count or 1

    local inventory = player:getInventory()
    if not inventory then
        return false
    end

    for i = 1, count do
        local item = inventory:getFirstTypeRecurse(itemType)
        if item then
            inventory:Remove(item)
        else
            return false
        end
    end

    return true
end

function NPC_DialogueActions.giveItem(player, itemType, count)
    if not player then
        return false
    end

    count = count or 1

    local inventory = player:getInventory()
    if not inventory then
        return false
    end

    for i = 1, count do
        inventory:AddItem(itemType)
    end

    return true
end

function NPC_DialogueActions.recordNPCMeeting(player, npcID)
    if not player then
        return false
    end

    local modData = player:getModData()
    if not modData then
        return false
    end

    if not modData.npcDialogueHistory then
        modData.npcDialogueHistory = {}
    end

    if not modData.npcDialogueHistory[npcID] then
        modData.npcDialogueHistory[npcID] = {}
    end

    local history = modData.npcDialogueHistory[npcID]

    history.firstMeeting = history.firstMeeting or (getTimestampMs() / 1000.0)
    history.meetingCount = (history.meetingCount or 0) + 1
    history.lastMeeting = getTimestampMs() / 1000.0

    return true
end

function NPC_DialogueActions.incrementModDataCounter(player, counterKey, amount)
    if not player then
        return false
    end

    amount = amount or 1

    local modData = player:getModData()
    if not modData then
        return false
    end

    if not modData[counterKey] then
        modData[counterKey] = 0
    end

    modData[counterKey] = modData[counterKey] + amount

    return true
end

function NPC_DialogueActions.createCustomAction(actionFunc)
    return actionFunc
end

return NPC_DialogueActions
