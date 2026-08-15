local GetVehiclePartsFactory = require('service/HDCP_IVP_GetVehicleParts')

local HDCP_IVP_IsVehicleInGoodCondition = {}

function HDCP_IVP_IsVehicleInGoodCondition.new(deps)
    local module = {}

    local constants = deps and deps.Constants or require("HDCP_IVP_Constants")
    local GetVehicleParts = deps and deps.GetVehicleParts or GetVehiclePartsFactory.new()

    module.isSatisfiedBy = function(context)
        local isOk = true

        local vehicleParts = GetVehicleParts.get(context.vehicle)

        local bodyworkRequirement = constants.BODYWORK_REQUIREMENT

        for _, vehiclePart in ipairs(vehicleParts) do
            if vehiclePart.condition < bodyworkRequirement then
                isOk = false
            end
        end

        if isOk then
            return true, nil
        end

        return false, {
            code = "FIX_BODYWORK",
            bodyworkRequirement = bodyworkRequirement,
            vehicle = context.vehicle
        }
    end

    return module
end

return HDCP_IVP_IsVehicleInGoodCondition
