local NPC_GiftingConfig = {}

NPC_GiftingConfig.COOLDOWN_HOURS = 24

NPC_GiftingConfig.GIFT_TABLES = {
    muggy = {
        enabled = true,
        requiredFlag = "muggy_found_module",
        items = {
            {
                itemType = "Base.ElectronicsScrap",
                minQuantity = 3,
                maxQuantity = 6
            }
        }
    }
}

function NPC_GiftingConfig.getGiftTable(npcID)
    return NPC_GiftingConfig.GIFT_TABLES[npcID]
end

function NPC_GiftingConfig.getCooldownHours()
    return NPC_GiftingConfig.COOLDOWN_HOURS
end

return NPC_GiftingConfig
