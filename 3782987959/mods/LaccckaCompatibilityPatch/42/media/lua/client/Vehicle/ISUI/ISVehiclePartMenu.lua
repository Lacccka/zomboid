-- PZK water-tank integration still requires this legacy path.
local Guard = require "LCC/Guard"
local FEATURE = "shim.pzk.vehicle-part-menu-isui"

Guard.safeRequire(FEATURE, "Vehicles/ISUI/ISVehiclePartMenu")
return ISVehiclePartMenu or {}
