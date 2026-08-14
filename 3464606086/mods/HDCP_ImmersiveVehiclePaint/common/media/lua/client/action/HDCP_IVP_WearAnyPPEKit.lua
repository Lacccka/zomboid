local PlayerKitsFactory = require('service/HDCP_IVP_GetPlayerAvailableKits')

local tableInsert = table.insert

local HDCP_IVP_WearAnyPPEKit = {}

function HDCP_IVP_WearAnyPPEKit.new(deps)
    local module          = {}

    local Constants       = deps and deps.Constants or require('HDCP_IVP_Constants')
    local PlayerKits      = deps and deps.GetPlayerAvailableKits or PlayerKitsFactory.new()
    local PaneContextMenu = deps and deps.ISInventoryPaneContextMenu or ISInventoryPaneContextMenu

    local function playerIsWearingAnyKit(player, availableKits)
        local inventory = player:getInventory()

        local wornKits = {}

        for _, kitNumber in pairs(availableKits) do
            local kitItems  = 0
            local wornItems = 0

            for _, item in pairs(Constants.ITEMS.PPE_KITS[kitNumber]) do
                local itemType = inventory:getFirstTypeRecurse(item)

                if player:isEquipped(itemType) then
                    wornItems = wornItems + 1
                end

                kitItems = kitItems + 1
            end

            if kitItems == wornItems then
                tableInsert(wornKits, kitNumber)
            end
        end

        return #wornKits > 0
    end

    local function wearSelectedKit(player, kitNumber)
        local inventory = player:getInventory()

        for _, item in pairs(Constants.ITEMS.PPE_KITS[kitNumber]) do
            local itemType = inventory:getFirstTypeRecurse(item)

            if not player:isEquipped(itemType) then
                PaneContextMenu.wearItem(itemType, player:getPlayerNum())
            end
        end
    end

    module.wear = function(player)
        local availableKits = PlayerKits.get(player)

        if playerIsWearingAnyKit(player, availableKits) then return end

        wearSelectedKit(player, availableKits[1])
    end

    return module
end

return HDCP_IVP_WearAnyPPEKit
