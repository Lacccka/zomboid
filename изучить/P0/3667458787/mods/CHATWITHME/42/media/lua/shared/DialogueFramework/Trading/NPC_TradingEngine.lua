local NPC_TradingEngine = {}

local NPC_TradingItemTables = require("DialogueFramework/Trading/NPC_TradingItemTables")

function NPC_TradingEngine.validatePlayerInventory(player, system)
    if not player then
        return {}
    end

    system = system or 1

    local inventory = player:getInventory()
    if not inventory then
        return {}
    end

    local foundItems = {}
    local acceptableItems = NPC_TradingItemTables.getAcceptableItems(system)

    for itemType, itemData in pairs(acceptableItems) do
        local items = inventory:getAllType(itemType)
        if items and items:size() > 0 then
            foundItems[itemType] = items
        end
    end

    return foundItems
end

function NPC_TradingEngine.hasAcceptableItems(player, system)
    local foundItems = NPC_TradingEngine.validatePlayerInventory(player, system)
    local count = 0
    for _ in pairs(foundItems) do
        count = count + 1
    end
    return count > 0, foundItems
end

function NPC_TradingEngine.countFoundItems(foundItems)
    local totalCount = 0
    for itemType, items in pairs(foundItems) do
        if items and items.size then
            totalCount = totalCount + items:size()
        end
    end
    return totalCount
end

return NPC_TradingEngine
