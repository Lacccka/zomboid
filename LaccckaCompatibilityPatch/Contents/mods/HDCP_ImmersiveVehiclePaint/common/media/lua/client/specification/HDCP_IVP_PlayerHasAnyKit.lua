local PlayerKitsFactory = require('service/HDCP_IVP_GetPlayerAvailableKits')

local HDCP_IVP_PlayerHasAnyKit = {}

function HDCP_IVP_PlayerHasAnyKit.new(deps)
    local module = {}

    local constants = deps and deps.Constants or require('HDCP_IVP_Constants')
    local playerKits = deps and deps.GetPlayerAvailableKits or PlayerKitsFactory.new()

    module.isSatisfiedBy = function(context)
        local availableKits = playerKits.get(context.player)

        if #availableKits > 0 then
            return true, nil
        end

        return false, {
            code = "NO_PPE",
            kits = constants.ITEMS.PPE_KITS
        }
    end

    return module
end

return HDCP_IVP_PlayerHasAnyKit
