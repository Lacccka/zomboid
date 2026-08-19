-- PZK's pzkZones.lua requires this legacy directory.
local Guard = require "LCC/Guard"
local FEATURE = "shim.pzk.zones-function"

local module = Guard.safeRequire(FEATURE, "pzkUtils/pzkZonesFunction", {})
return module or pzkZonesFunction or {}
