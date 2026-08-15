local HDCP_IVP_GetVehicleParts = {}

local tableInsert = table.insert

function HDCP_IVP_GetVehicleParts.new()
    local M = {}

    local BODYWORK_PARTS = {
        "EngineDoor",
        "DoorFrontLeft",
        "DoorFrontRight",
        "DoorMiddleLeft",
        "DoorMiddleRight",
        "DoorRear",
        "DoorRearLeft",
        "DoorRearRight",
        "TrunkDoor",
    }

    local function contains(list, value)
        for _, v in ipairs(list) do
            if v == value then
                return true
            end
        end

        return false
    end

    M.get = function(vehicle)
        local parts = {}

        for partIndex = 1, vehicle:getPartCount() do
            local vehiclePart = vehicle:getPartByIndex(partIndex - 1)

            local vehiclePartId = vehiclePart:getId()

            if contains(BODYWORK_PARTS, vehiclePartId) then
                tableInsert(parts, {
                    name = getText('IGUI_VehiclePart' .. vehiclePartId),
                    condition = vehiclePart:getCondition()
                })
            end
        end

        return parts
    end

    return M
end

return HDCP_IVP_GetVehicleParts
