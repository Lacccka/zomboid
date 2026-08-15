---Like DoParam but for vehicles
---@param vehicle string Name of the vehicle script
---@param param string The parameter(s) to apply to this script
---@param module string Optional: the module of the vehicle
---@see Item#DoParam
---@see VehicleScript#Load






local pzkDoVehicleParam = function(vehicle, param, module)
    module = module or "Base"
    local vehicleScript = ScriptManager.instance:getVehicle(module .. "." .. vehicle)
    if not vehicleScript then return end
    vehicleScript:Load(vehicle, "{" .. param .. "}")
end

---Utility to add new skins to vehicles
---@param vehicle string Name of the vehicle script
---@param texture string The new skin's texture
---@see DoVehicleParam
local pzkAddVehicleSkin = function(vehicle, texture)
    module = module or "Base"
    if not pzkDoVehicleParam(vehicle, "skin { texture = " .. texture .. ",}", module) then return end
    local fullName = module .. "." .. vehicle
    vehicleToSkin[fullName] = vehicleToSkin[fullName] or {}
end
pzkAddVehicleSkin("CarLuxury", "Vehicles/vehicle_luxurycar2shell")
return vehicleToSkin