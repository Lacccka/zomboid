local WearAnyPPEKitFactory       = require('action/HDCP_IVP_WearAnyPPEKit')
local EquipHandItemFactory       = require('action/HDCP_IVP_EquipHandItem')
local TransferUsableItemsFactory = require('action/HDCP_IVP_TransferUsableItems')
local PrimeVehicleFactory        = require('action/HDCP_IVP_PrimeVehicle')

local HDCP_IVP_OnPrimeVehicle    = {}

function HDCP_IVP_OnPrimeVehicle.new(deps)
    local module              = {}

    local Constants           = deps and deps.Constants or require('HDCP_IVP_Constants')
    local Queue               = deps and deps.ISTimedActionQueue or ISTimedActionQueue
    local PathFind            = deps and deps.ISPathFindAction or ISPathFindAction
    local wearAnyPPEKit       = deps and deps.WearAnyPPEKit or WearAnyPPEKitFactory.new()
    local equipHandItem       = deps and deps.EquipHandItem or EquipHandItemFactory.new()
    local transferUsableItems = deps and deps.TransferUsableItems or TransferUsableItemsFactory.new()
    local primeVehicle        = deps and deps.PrimeVehicle or PrimeVehicleFactory.new()

    module.handle             = function(player, vehicle)
        local firstVehicleArea = Constants.VEHICLE_SURFACES[1]

        Queue.add(PathFind:pathToVehicleArea(
            player, vehicle, firstVehicleArea
        ))

        wearAnyPPEKit.wear(player)

        local itemType = Constants.ITEMS.AUTOMOTIVE_PRIMER_SPRAY

        local data = vehicle:getModData().IVP

        transferUsableItems.transfer(player, itemType, data.requiredPrimerUses)

        equipHandItem.equip(player, itemType, 'SECONDARY')

        Queue.add(primeVehicle:new(
            player, vehicle, firstVehicleArea
        ))
    end

    return module
end

return HDCP_IVP_OnPrimeVehicle
