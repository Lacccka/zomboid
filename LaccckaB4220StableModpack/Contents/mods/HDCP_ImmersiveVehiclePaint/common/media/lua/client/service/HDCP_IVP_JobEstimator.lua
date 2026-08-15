local SprayUsageCalculatorFactory = require('service/HDCP_IVP_SprayUsageCalculator')
local SurfaceAreaFinderFactory = require('service/HDCP_IVP_SurfaceAreaFinder')
local SurfaceAreaCalculatorFactory = require('service/HDCP_IVP_SurfaceAreaCalculator')

local HDCP_IVP_JobEstimator = {}

function HDCP_IVP_JobEstimator.new(deps)
    local M = {}

    M.SprayUsageCalculator = deps and deps.SprayUsageCalculator
        or SprayUsageCalculatorFactory.new()
    M.SurfaceAreaFinder = deps and deps.SurfaceAreaFinder
        or SurfaceAreaFinderFactory.new()
    M.SurfaceAreaCalculator = deps and deps.SurfaceAreaCalculator
        or SurfaceAreaCalculatorFactory.new()

    M.calculate = function(vehicle)
        local extents = vehicle:getScript():getExtents()
        local extX = extents:x()
        local extY = extents:y()
        local extZ = extents:z()

        local requiredUses = M.SprayUsageCalculator.calculate(extX, extY, extZ)
        local surfaceAreas = M.SurfaceAreaFinder.find(vehicle)
        local surfaceAreaSizes = M.SurfaceAreaCalculator.calculate(
            surfaceAreas, extX, extY, extZ
        )

        return {
            isSanded = false,
            isPrimed = false,
            requiredPaintUses = requiredUses,
            requiredPrimerUses = math.ceil(requiredUses / 2),
            surfaceAreas = surfaceAreas,
            totalAreas = #surfaceAreas,
            surfaceAreaSizes = surfaceAreaSizes
        }
    end

    return M
end

return HDCP_IVP_JobEstimator
