local HDCP_IVP_GetItemUsesRemaining = {}

function HDCP_IVP_GetItemUsesRemaining.new()
    local module = {}

    module.get = function(player, itemType)
        local items = player:getInventory():getAllTypeRecurse(itemType)

        local itemUses = 0

        for i = 1, items:size() do
            itemUses = itemUses + items:get(i - 1):getCurrentUses()
        end

        return itemUses
    end

    return module
end

return HDCP_IVP_GetItemUsesRemaining
