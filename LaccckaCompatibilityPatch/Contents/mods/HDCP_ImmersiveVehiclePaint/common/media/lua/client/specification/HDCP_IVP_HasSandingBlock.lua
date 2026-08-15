local HDCP_IVP_HasSandingBlock = {}

function HDCP_IVP_HasSandingBlock.new(deps)
    local module = {}

    local constants = deps and deps.Constants or require('HDCP_IVP_Constants')

    module.isSatisfiedBy = function(context)
        local itemType = constants.ITEMS.SANDING_BLOCK

        local inventory = context.player:getInventory()

        local playerHasItem = inventory:containsTypeRecurse(itemType)

        if playerHasItem then
            return true, nil
        end

        return false, {
            code     = "NO_TOOL",
            itemType = itemType
        }
    end

    return module
end

return HDCP_IVP_HasSandingBlock
