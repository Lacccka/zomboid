local NPC_GiftingCooldownManager = {}

local NPC_GiftingConfig = require("DialogueFramework/Gifting/NPC_GiftingConfig")

function NPC_GiftingCooldownManager.getLastGiftTime(player, npcID)
    if not player or not npcID then
        return nil
    end

    local modData = player:getModData()
    if not modData.npc_gifting_cooldowns then
        modData.npc_gifting_cooldowns = {}
    end

    return modData.npc_gifting_cooldowns[npcID]
end

function NPC_GiftingCooldownManager.setLastGiftTime(player, npcID, time)
    if not player or not npcID then
        return false
    end

    local modData = player:getModData()
    if not modData.npc_gifting_cooldowns then
        modData.npc_gifting_cooldowns = {}
    end

    modData.npc_gifting_cooldowns[npcID] = time
    return true
end

function NPC_GiftingCooldownManager.canReceiveGift(player, npcID)
    if not player or not npcID then
        return false
    end

    local lastGiftTime = NPC_GiftingCooldownManager.getLastGiftTime(player, npcID)

    if not lastGiftTime then
        return true
    end

    local currentTime = getGameTime():getWorldAgeHours()
    local timeSinceLastGift = currentTime - lastGiftTime
    local cooldownHours = NPC_GiftingConfig.getCooldownHours()

    return timeSinceLastGift >= cooldownHours
end

function NPC_GiftingCooldownManager.getTimeUntilNextGift(player, npcID)
    if not player or not npcID then
        return nil
    end

    local lastGiftTime = NPC_GiftingCooldownManager.getLastGiftTime(player, npcID)

    if not lastGiftTime then
        return 0
    end

    local currentTime = getGameTime():getWorldAgeHours()
    local timeSinceLastGift = currentTime - lastGiftTime
    local cooldownHours = NPC_GiftingConfig.getCooldownHours()
    local timeRemaining = cooldownHours - timeSinceLastGift

    return math.max(0, timeRemaining)
end

return NPC_GiftingCooldownManager
