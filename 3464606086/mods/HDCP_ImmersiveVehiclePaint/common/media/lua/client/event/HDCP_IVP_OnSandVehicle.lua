local WearAnyPPEKitFactory   = require('action/HDCP_IVP_WearAnyPPEKit')
local EquipHandItemFactory   = require('action/HDCP_IVP_EquipHandItem')
local SandVehicleFactory     = require('action/HDCP_IVP_SandVehicle')

local HDCP_IVP_OnSandVehicle = {}

function HDCP_IVP_OnSandVehicle.new(deps)
    local module        = {}

    local Constants     = deps and deps.Constants or require('HDCP_IVP_Constants')
    local Queue         = deps and deps.ISTimedActionQueue or ISTimedActionQueue
    local PathFind      = deps and deps.ISPathFindAction or ISPathFindAction
    local wearAnyPPEKit = deps and deps.WearAnyPPEKit or WearAnyPPEKitFactory.new()
    local equipHandItem = deps and deps.EquipHandItem or EquipHandItemFactory.new()
    local sandVehicle   = deps and deps.SandVehicle or SandVehicleFactory.new()

    module.handle       = function(player, vehicle)
        local firstVehicleArea = Constants.VEHICLE_SURFACES[1]

        Queue.add(PathFind:pathToVehicleArea(
            player, vehicle, firstVehicleArea
        ))

        wearAnyPPEKit.wear(player)

        equipHandItem.equip(player, Constants.ITEMS.SANDING_BLOCK, 'SECONDARY')

        Queue.add(sandVehicle:new(
            player, vehicle, firstVehicleArea
        ))
    end

    return module
end

return HDCP_IVP_OnSandVehicle
