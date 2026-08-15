local HDCP_IVP_SurfaceAreaFinder = {}

local tableInsert = table.insert

function HDCP_IVP_SurfaceAreaFinder.new(deps)
    local M = {}

    M.Constants = deps and deps.Constants or require('HDCP_IVP_Constants')

    local VEHICLE_AREAS = {
        "Engine",
        "TireFrontLeft",
        "SeatFrontLeft",
        "SeatMiddleLeft",
        "SeatRearLeft",
        "TireRearLeft",
        "TruckBed",
        "TireRearRight",
        "SeatRearRight",
        "SeatMiddleRight",
        "SeatFrontRight",
        "TireFrontRight",
    }

    local function orderByVehicleArea(list)
        local ordered = {}

        for _, order in ipairs(VEHICLE_AREAS) do
            for _, item in ipairs(list) do
                if order == item then
                    tableInsert(ordered, order)
                end
            end
        end

        return ordered
    end

    M.find = function(vehicle)
        local found = {}

        for partIndex = 1, vehicle:getPartCount() do
            local vehiclePart = vehicle:getPartByIndex(partIndex - 1)

            local partId = vehiclePart:getId()

            for _, surface in ipairs(M.Constants.VEHICLE_SURFACES) do
                if partId == surface then
                    tableInsert(found, vehiclePart:getArea())
                end
            end
        end

        return orderByVehicleArea(found)
    end

    return M
end

return HDCP_IVP_SurfaceAreaFinder
