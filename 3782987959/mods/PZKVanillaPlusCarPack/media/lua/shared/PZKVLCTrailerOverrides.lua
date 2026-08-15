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

if not getActivatedMods():contains("tsarslib") then
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
end