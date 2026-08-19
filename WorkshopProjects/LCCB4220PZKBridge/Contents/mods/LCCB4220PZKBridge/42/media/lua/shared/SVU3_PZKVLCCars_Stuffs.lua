-- Legacy shared-root bridge for optional vehicle-upgrade support.
-- Keep the installed support module authoritative and fail soft if a future
-- target version removes/renames it instead of aborting the whole bridge.
local Guard = require "LCC/Guard"
local FEATURE = "shim.vehicle-integration.svu-support"

local module = Guard.safeRequire(FEATURE, "OtherModsSupport/SVU3_PZKVLCCars_Stuffs", {})
return module or SVU3_PZKVLCCars_Stuffs or {}
