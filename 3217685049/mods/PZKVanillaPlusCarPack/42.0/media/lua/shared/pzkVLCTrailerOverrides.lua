---Like DoParam but for vehicles
---@param vehicle string Name of the vehicle script
---@param param string The parameter(s) to apply to this script
---@param module string Optional: the module of the vehicle
---@see Item#DoParam
---@see VehicleScript#Load



local DoVehicleParam = function(vehicle, param, module)
	module = module or "Base"
	local vehicleScript = ScriptManager.instance:getVehicle(module .. "." .. vehicle)
	if not vehicleScript then return end
	vehicleScript:Load(vehicle, "{" .. param .. "}")
end

---Utility to change models on vehicles
---@param vehicle string Name of the vehicle script
---@param model string The new model
---@see DoVehicleParam
local ChangeVehicleModel = function(vehicle, model, module)
	module = module or "Base"
	if not DoVehicleParam(vehicle, "model { file = " .. model .. ",}", module) then return end
	local fullName = module .. "." .. vehicle
	vehicleToSkin[fullName] = vehicleToSkin[fullName] or {}
end

---Utility to change the template of a vehicle
---@param vehicle string Name of the vehicle script
---@param template string Name of a template
---@see DoVehicleParam
local SetTemplate = function(vehicle, template, module)
	module = module or "Base"
	DoVehicleParam(vehicle, "template = " .. template .. ",", module)
end

if not ATA2TuningTable then



	ChangeVehicleModel("pzkTrailerRegularFlatbed", "pzkTrailerRegularFlatbedBurned")
	SetTemplate("pzkTrailerRegularFlatbed", "pzkTrailerRegularFlatbedBurnedOverride")

	ChangeVehicleModel("pzkTrailerRegularCourtains", "pzkTrailerRegularFlatbedBurned")
	SetTemplate("pzkTrailerRegularCourtains", "pzkTrailerRegularFlatbedBurnedOverride")

	ChangeVehicleModel("pzkTrailerRegularFuelTanker", "pzkTrailerRegularFlatbedBurned")
	SetTemplate("pzkTrailerRegularFuelTanker", "pzkTrailerRegularFlatbedBurnedOverride")

	ChangeVehicleModel("pzkTrailerRegularCourtainsBandit", "pzkTrailerRegularFlatbedBurned")
	SetTemplate("pzkTrailerRegularCourtainsBandit", "pzkTrailerRegularFlatbedBurnedOverride")

	ChangeVehicleModel("pzkTrailerRegularFTSemi", "pzkTrailerRegularFlatbedBurned")
	SetTemplate("pzkTrailerRegularFTSemi", "pzkTrailerRegularFlatbedBurnedOverride")
	
	
	
ChangeVehicleModel("pzkTrailerRegularWaterTankerArmy", "pzkTrailerRegularFlatbedBurned")
ChangeVehicleModel("pzkTrailerRegularFuelTankerArmy", "pzkTrailerRegularFlatbedBurned")
ChangeVehicleModel("pzkTrailerRegularWaterTankerTainted", "pzkTrailerRegularFlatbedBurned")
ChangeVehicleModel("pzkTrailerRegularWaterTanker", "pzkTrailerRegularFlatbedBurned")
ChangeVehicleModel("pzkTrailerRegularCourtainsWhite", "pzkTrailerRegularFlatbedBurned")
ChangeVehicleModel("pzkTrailerRegularContainer", "pzkTrailerRegularFlatbedBurned")
ChangeVehicleModel("pzkTrailerRegularFedLog", "pzkTrailerRegularFlatbedBurned")
ChangeVehicleModel("pzkTrailerRegularGigamart", "pzkTrailerRegularFlatbedBurned")
ChangeVehicleModel("pzkTrailerRegularPharmahung", "pzkTrailerRegularFlatbedBurned")
ChangeVehicleModel("pzkTrailerRegularValutech", "pzkTrailerRegularFlatbedBurned")
ChangeVehicleModel("pzkTrailerRegularUStoreIt", "pzkTrailerRegularFlatbedBurned")
ChangeVehicleModel("pzkTrailerRegularCourtainsKnight", "pzkTrailerRegularFlatbedBurned")
ChangeVehicleModel("pzkTrailerRegularSpiffo", "pzkTrailerRegularFlatbedBurned")
ChangeVehicleModel("pzkTrailerRegularPop", "pzkTrailerRegularFlatbedBurned")
ChangeVehicleModel("pzkTrailerRegularPropaneTanker", "pzkTrailerRegularFlatbedBurned")
ChangeVehicleModel("pzkTrailerRegularLivestock", "pzkTrailerRegularFlatbedBurned")
ChangeVehicleModel("pzkTrailerRegularLivestock2", "pzkTrailerRegularFlatbedBurned")

SetTemplate("pzkTrailerRegularWaterTankerArmy", "pzkTrailerRegularFlatbedBurnedOverride")
SetTemplate("pzkTrailerRegularFuelTankerArmy", "pzkTrailerRegularFlatbedBurnedOverride")
SetTemplate("pzkTrailerRegularWaterTankerTainted", "pzkTrailerRegularFlatbedBurnedOverride")
SetTemplate("pzkTrailerRegularWaterTanker", "pzkTrailerRegularFlatbedBurnedOverride")
SetTemplate("pzkTrailerRegularCourtainsWhite", "pzkTrailerRegularFlatbedBurnedOverride")
SetTemplate("pzkTrailerRegularContainer", "pzkTrailerRegularFlatbedBurnedOverride")
SetTemplate("pzkTrailerRegularFedLog", "pzkTrailerRegularFlatbedBurnedOverride")
SetTemplate("pzkTrailerRegularGigamart", "pzkTrailerRegularFlatbedBurnedOverride")
SetTemplate("pzkTrailerRegularPharmahung", "pzkTrailerRegularFlatbedBurnedOverride")
SetTemplate("pzkTrailerRegularValutech", "pzkTrailerRegularFlatbedBurnedOverride")
SetTemplate("pzkTrailerRegularUStoreIt", "pzkTrailerRegularFlatbedBurnedOverride")
SetTemplate("pzkTrailerRegularCourtainsKnight", "pzkTrailerRegularFlatbedBurnedOverride")
SetTemplate("pzkTrailerRegularSpiffo", "pzkTrailerRegularFlatbedBurnedOverride")
SetTemplate("pzkTrailerRegularPop", "pzkTrailerRegularFlatbedBurnedOverride")
SetTemplate("pzkTrailerRegularPropaneTanker", "pzkTrailerRegularFlatbedBurnedOverride")	
SetTemplate("pzkTrailerRegularLivestock", "pzkTrailerRegularFlatbedBurnedOverride")
SetTemplate("pzkTrailerRegularLivestock2", "pzkTrailerRegularFlatbedBurnedOverride")	
	
end



--------------------------HOOD ARMORS REQUIRE THAT HOOD SPARE WHEEL NEED TO BE UNINSTALLED ---------------------------

if getActivatedMods():contains("StandardizedVehicleUpgrades3V") then
SetTemplate("pzkContinentalGuardian", "pzkSpareWheelHoodSVU") --Landrover Hoodsparewheel
SetTemplate("pzkContinentalGuardianLlama", "pzkSpareWheelHoodSVU") --Landrover Hoodsparewheel
SetTemplate("pzkContinentalGuardianService", "pzkSpareWheelHoodSVU") --Landrover Hoodsparewheel
else
SetTemplate("pzkContinentalGuardian", "pzkSpareWheelHood") --Landrover Hoodsparewheel	 SVU
SetTemplate("pzkContinentalGuardianLlama", "pzkSpareWheelHood") --Landrover Hoodsparewheel
SetTemplate("pzkContinentalGuardianService", "pzkSpareWheelHood") --Landrover Hoodsparewheel
end

