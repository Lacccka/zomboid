local HDCP_IVP_IsVehicleClean = {}

function HDCP_IVP_IsVehicleClean.new(deps)
    local module = {}

    local isWashVehicle = deps and deps.ISWashVehicle or ISWashVehicle

    module.isSatisfiedBy = function(context)
        if not isWashVehicle.hasBlood(context.vehicle) then
            return true, nil
        end

        return false, {
            code = "WASH_VEHICLE"
        }
    end

    return module
end

return HDCP_IVP_IsVehicleClean
