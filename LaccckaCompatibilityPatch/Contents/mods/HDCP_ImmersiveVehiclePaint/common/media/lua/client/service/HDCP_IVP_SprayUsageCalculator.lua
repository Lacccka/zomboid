local HDCP_IVP_SprayUsageCalculator = {}

function HDCP_IVP_SprayUsageCalculator.new(deps)
    local M = {}

    M.Constants = deps and deps.Constants or require('HDCP_IVP_Constants')

    local function calculateVehiclePaintSurfaceArea(x, y, z)
        local front = x * (z / 2)
        local rear = front
        local side = y * z

        return front + rear + side * 2
    end

    local function calculateCansRequired(surfaceArea)
        local paintCoverage = 1.5

        return surfaceArea / paintCoverage
    end

    local function calculateRequiredSprayUses(cansRequired)
        local delta = M.Constants.SPRAY_DELTA

        local usesBySpray = 1 / delta

        return cansRequired * usesBySpray
    end

    local function roundByDelta(uses)
        local fullSprayUses = math.floor(uses)

        local partialSprayUse = uses - fullSprayUses

        return partialSprayUse > 0.5 and fullSprayUses + 1 or fullSprayUses
    end

    M.calculate = function(x, y, z)
        local surfaceArea = calculateVehiclePaintSurfaceArea(x, y, z)

        local cansRequired = calculateCansRequired(surfaceArea)

        local requiredSprayUses = calculateRequiredSprayUses(cansRequired)

        return roundByDelta(requiredSprayUses)
    end

    return M
end

return HDCP_IVP_SprayUsageCalculator
