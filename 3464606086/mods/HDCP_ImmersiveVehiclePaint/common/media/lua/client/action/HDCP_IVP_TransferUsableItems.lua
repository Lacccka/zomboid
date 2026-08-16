local tableInsert = table.insert
local tableRemove = table.remove

local HDCP_IVP_TransferUsableItems = {}

function HDCP_IVP_TransferUsableItems.new(deps)
    local module = {}

    local PaneContextMenu = deps and deps.ISInventoryPaneContextMenu or ISInventoryPaneContextMenu

    local function transferItemsIfNeeded(player, items, usesNeeded)
        if usesNeeded <= 0 then return end

        local item = items[1]

        PaneContextMenu.transferIfNeeded(player, item)

        tableRemove(items, 1)

        transferItemsIfNeeded(player, items, usesNeeded - item:getCurrentUses())
    end

    module.transfer = function(player, itemType, usesNeeded)
        local items = player:getInventory():getAllTypeRecurse(itemType)

        local itemList = {}

        for i = 1, items:size() do
            tableInsert(itemList, items:get(i - 1))
        end

        transferItemsIfNeeded(player, itemList, usesNeeded)
    end

    return module
end

return HDCP_IVP_TransferUsableItems
