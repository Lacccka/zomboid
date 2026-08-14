local WearAnyPPEKitFactory       = require('action/HDCP_IVP_WearAnyPPEKit')
local EquipHandItemFactory       = require('action/HDCP_IVP_EquipHandItem')
local TransferUsableItemsFactory = require('action/HDCP_IVP_TransferUsableItems')
local PaintVehicleFactory        = require('action/HDCP_IVP_PaintVehicle')

local HDCP_IVP_OnPaintVehicle    = {}

function HDCP_IVP_OnPaintVehicle.new(deps)
    local module              = {}

    local Constants           = deps and deps.Constants or require('HDCP_IVP_Constants')
    local Queue               = deps and deps.ISTimedActionQueue or ISTimedActionQueue
    local PathFind            = deps and deps.ISPathFindAction or ISPathFindAction
    local wearAnyPPEKit       = deps and deps.WearAnyPPEKit or WearAnyPPEKitFactory.new()
    local equipHandItem       = deps and deps.EquipHandItem or EquipHandItemFactory.new()
    local transferUsableItems = deps and deps.TransferUsableItems or TransferUsableItemsFactory.new()
    local paintVehicle        = deps and deps.PaintVehicle or PaintVehicleFactory.new()

    module.handle             = function(player, vehicle, paint)
        local firstVehicleArea = Constants.VEHICLE_SURFACES[1]

        Queue.add(PathFind:pathToVehicleArea(
            player, vehicle, firstVehicleArea
        ))

        wearAnyPPEKit.wear(player)

        local data = vehicle:getModData().IVP

        transferUsableItems.transfer(player, paint.type, data.requiredPaintUses)

        equipHandItem.equip(player, paint.type, 'SECONDARY')

        Queue.add(paintVehicle:new(
            player, vehicle, firstVehicleArea, paint
        ))
    end

    return module
end

return HDCP_IVP_OnPaintVehicle
