local HDCP_IVP_SurfaceAreaCalculator = {}

local tableInsert = table.insert
local stringFind = string.find

function HDCP_IVP_SurfaceAreaCalculator.new()
    local M = {}

    local function sideSurfaceCount(surfaces)
        local sideSurfaces = {}

        for _, partId in ipairs(surfaces) do
            if stringFind(partId, "Left") then
                tableInsert(sideSurfaces, partId)
            end
        end

        return #sideSurfaces
    end

    M.calculate = function(surfaces, x, y, z)
        local surfaceAreasCalculated = {}

        local numSideSurfaces = sideSurfaceCount(surfaces)

        local frontArea = x * (z / 3)
        local rearArea = frontArea
        local sideArea = (y * z) / numSideSurfaces

        surfaceAreasCalculated["Engine"] = frontArea
        surfaceAreasCalculated["TruckBed"] = rearArea

        for _, surface in ipairs(surfaces) do
            if stringFind(surface, "Left") or stringFind(surface, "Right") then
                surfaceAreasCalculated[surface] = sideArea
            end
        end

        return surfaceAreasCalculated
    end

    return M
end

return HDCP_IVP_SurfaceAreaCalculator
