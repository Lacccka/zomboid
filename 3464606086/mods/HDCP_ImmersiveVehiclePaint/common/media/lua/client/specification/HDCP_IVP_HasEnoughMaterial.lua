local GetItemUsesRemainingFactory = require('service/HDCP_IVP_GetItemUsesRemaining')

local HDCP_IVP_HasEnoughMaterial = {}

function HDCP_IVP_HasEnoughMaterial.new(deps)
    local module            = {}

    local Constants         = deps and deps.Constants or require('HDCP_IVP_Constants')
    local ItemUsesRemaining = deps and deps.GetItemUsesRemaining or GetItemUsesRemainingFactory.new()

    module.isSatisfiedBy    = function(context)
        local remainingUses = ItemUsesRemaining.get(context.player, context.itemType)

        if remainingUses >= context.requiredUses then
            return true, nil
        end

        return false, {
            code          = "NO_MATERIAL",
            sprayDelta    = Constants.SPRAY_DELTA,
            remainingUses = remainingUses,
            requiredUses  = context.requiredUses,
            itemType      = context.itemType
        }
    end

    return module
end

return HDCP_IVP_HasEnoughMaterial
