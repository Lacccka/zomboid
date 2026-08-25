local NPC_GiftingEngine = {}

local NPC_GiftingConfig = require("DialogueFramework/Gifting/NPC_GiftingConfig")
local NPC_GiftingCooldownManager = require("DialogueFramework/Gifting/NPC_GiftingCooldownManager")
local NPC_DialogueConditions = require("DialogueFramework/Dialogue/NPC_DialogueConditions")

function NPC_GiftingEngine.canStartGifting(player, npcID)
    if not player or not npcID then
        return false, "Invalid parameters"
    end

    local giftTable = NPC_GiftingConfig.getGiftTable(npcID)
    if not giftTable or not giftTable.enabled then
        return false, "Gifting not enabled for this NPC"
    end

    if giftTable.requiredFlag then
        if not NPC_DialogueConditions.hasModDataFlag(player, giftTable.requiredFlag) then
            return false, "Required flag not met"
        end
    end

    if not NPC_GiftingCooldownManager.canReceiveGift(player, npcID) then
        local timeRemaining = NPC_GiftingCooldownManager.getTimeUntilNextGift(player, npcID)
        return false, "Cooldown active: " .. tostring(math.floor(timeRemaining)) .. " hours remaining"
    end

    return true
end

function NPC_GiftingEngine.getGiftItems(npcID)
    local giftTable = NPC_GiftingConfig.getGiftTable(npcID)
    if not giftTable then
        return nil
    end

    local resolvedItems = {}
    for _, itemConfig in ipairs(giftTable.items) do
        local quantity = itemConfig.quantity

        if not quantity and itemConfig.minQuantity and itemConfig.maxQuantity then
            quantity = ZombRand(itemConfig.maxQuantity - itemConfig.minQuantity + 1) + itemConfig.minQuantity
        elseif not quantity then
            quantity = 1
        end

        table.insert(resolvedItems, {
            itemType = itemConfig.itemType,
            quantity = quantity
        })
    end

    return resolvedItems
end

function NPC_GiftingEngine.markGiftReceived(player, npcID)
    if not player or not npcID then
        return false
    end

    local currentTime = getGameTime():getWorldAgeHours()
    NPC_GiftingCooldownManager.setLastGiftTime(player, npcID, currentTime)
    return true
end

return NPC_GiftingEngine
