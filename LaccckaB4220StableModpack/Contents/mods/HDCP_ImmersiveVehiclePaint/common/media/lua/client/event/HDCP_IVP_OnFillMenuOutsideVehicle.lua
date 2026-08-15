local JobEstimatorFactory = require("service/HDCP_IVP_JobEstimator")
local SandOptionFactory = require("ui/HDCP_IVP_ContextMenuSandOption")
local PrimeOptionFactory = require("ui/HDCP_IVP_ContextMenuPrimeOption")
local PaintOptionFactory = require("ui/HDCP_IVP_ContextMenuPaintOption")

local HDCP_IVP_OnFillMenuOutsideVehicle = {}

function HDCP_IVP_OnFillMenuOutsideVehicle.new(deps)
    local M        = {}

    M.Constants    = deps and deps.Constants or require("HDCP_IVP_Constants")
    M.JobEstimator = deps and deps.JobEstimator or JobEstimatorFactory.new()
    M.SandOption   = deps and deps.SandOption or SandOptionFactory.new()
    M.PrimeOption  = deps and deps.PrimeOption or PrimeOptionFactory.new()
    M.PaintOption  = deps and deps.PaintOption or PaintOptionFactory.new()

    local function vehicleIsAllowed(vehicleId)
        local isAllowed = false

        for _, vehicle in pairs(M.Constants.ALLOWED_VEHICLES) do
            if vehicleId == vehicle.id then
                isAllowed = true
                break
            end
        end

        return isAllowed
    end

    M.run = function(playerIndex, context, vehicle, test)
        if test then return end

        local player = getSpecificPlayer(playerIndex)
        if not player then return end

        local known = player:getKnownRecipes()
        if known and not known:contains(M.Constants.RECIPE) then return end

        if not vehicleIsAllowed(vehicle:getScriptName()) then return end

        local data = vehicle:getModData()

        if type(data.IVP) ~= 'table' then
            data['IVP'] = M.JobEstimator.calculate(vehicle)
            vehicle:transmitModData()
        end

        if not data.IVP.isSanded and not data.IVP.isPrimed then
            M.SandOption.add(context, player, vehicle)
        elseif data.IVP.isSanded and not data.IVP.isPrimed then
            M.PrimeOption.add(context, player, vehicle)
        elseif data.IVP.isSanded and data.IVP.isPrimed then
            M.PaintOption.add(context, player, vehicle)
        end
    end

    return M
end

return HDCP_IVP_OnFillMenuOutsideVehicle
