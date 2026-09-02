-- zombie RE engine uses the old root-level vanilla module name.
local Guard = require "LCC/Guard"
local FEATURE = "shim.zre.body-locations"

Guard.safeRequire(FEATURE, "NPCs/BodyLocations")
return BodyLocations or {}
