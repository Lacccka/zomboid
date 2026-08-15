local HDCP_IVP_EquipHandItem = {}

function HDCP_IVP_EquipHandItem.new(deps)
    local module            = {}

    local ObjectContextMenu = deps and deps.ISWorldObjectContextMenu or ISWorldObjectContextMenu
    local Queue             = deps and deps.ISTimedActionQueue or ISTimedActionQueue
    local Unequip           = deps and deps.ISUnequipAction or ISUnequipAction

    module.equip            = function(player, itemType, hand)
        local hands = {
            PRIMARY   = function() return true, false end,
            SECONDARY = function() return false, false end,
            BOTH      = function() return false, true end
        }

        local primary, twoHands = hands[hand]()

        if not instanceof(itemType, "InventoryItem") then
            itemType = player:getInventory():getFirstTypeRecurse(itemType)
        end

        local primaryHandItem = player:getPrimaryHandItem()

        if primaryHandItem == itemType then return end

        if primaryHandItem then
            Queue.add(Unequip:new(player, primaryHandItem, 50))
        end

        ObjectContextMenu.equip(
            player,
            player:getPrimaryHandItem(),
            itemType,
            primary,
            twoHands
        )
    end

    return module
end

return HDCP_IVP_EquipHandItem
