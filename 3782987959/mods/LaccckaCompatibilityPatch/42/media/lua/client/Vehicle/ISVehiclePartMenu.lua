-- PZK still requires the singular pre-B42.20 vehicle path.
local Guard = require "LCC/Guard"
local FEATURE = "shim.pzk.vehicle-part-menu"

Guard.safeRequire(FEATURE, "Vehicles/ISUI/ISVehiclePartMenu")
return ISVehiclePartMenu or {}
