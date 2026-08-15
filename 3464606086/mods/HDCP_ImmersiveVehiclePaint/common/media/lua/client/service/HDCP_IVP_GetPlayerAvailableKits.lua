local HDCP_IVP_GetPlayerAvailableKits = {}

local tableInsert = table.insert

function HDCP_IVP_GetPlayerAvailableKits.new(deps)
    local M = {}

    local Contants = deps and deps.Contants or require('HDCP_IVP_Constants')

    M.get = function(player)
        local inventory = player:getInventory()

        local availableKits = {}

        for kitNumber, kit in pairs(Contants.ITEMS.PPE_KITS) do
            local kitItems = 0
            local availableItems = 0

            for _, item in pairs(kit) do
                if inventory:containsTypeRecurse(item) then
                    availableItems = availableItems + 1
                end

                kitItems = kitItems + 1
            end

            if kitItems == availableItems then
                tableInsert(availableKits, kitNumber)
            end
        end

        return availableKits
    end

    return M
end

return HDCP_IVP_GetPlayerAvailableKits
