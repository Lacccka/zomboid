local MiscRecipeRegistryFactory = require("service/HDCP_IVP_MiscRecipeRegistry")
MiscRecipeRegistryFactory.new().register()

local OnGameStartFactory = require("event/HDCP_IVP_OnGameStart")
local HDCP_IVP_OnGameStart = OnGameStartFactory.new()
Events.OnGameStart.Add(HDCP_IVP_OnGameStart.run)

local OnFillMenuOutsideVehicleFactory = require("event/HDCP_IVP_OnFillMenuOutsideVehicle")
local HDCP_IVP_OnFillMenuOutsideVehicle = OnFillMenuOutsideVehicleFactory.new()
local originalFillMenuOutsideVehicle = ISVehicleMenu.FillMenuOutsideVehicle
local customFillMenuOutsideVehicle = function(player, context, vehicle, test)
    if originalFillMenuOutsideVehicle then
        originalFillMenuOutsideVehicle(player, context, vehicle, test)
    end
    HDCP_IVP_OnFillMenuOutsideVehicle.run(player, context, vehicle, test)
end
ISVehicleMenu.FillMenuOutsideVehicle = customFillMenuOutsideVehicle
