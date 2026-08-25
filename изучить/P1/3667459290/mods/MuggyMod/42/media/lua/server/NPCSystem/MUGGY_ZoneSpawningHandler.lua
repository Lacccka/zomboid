local MUGGY_ZoneSpawningHandler = {}

local MUGGY_ZoneSpawningCore = require("MuggyMod/MUGGY_ZoneSpawningCore")
local MUGGY_EnvironmentDetector = require("MuggyMod/MUGGY_EnvironmentDetector")

function MUGGY_ZoneSpawningHandler.initialize()
    print("[MUGGY_ZoneSpawning] SERVER: Setting up event handlers")

    MUGGY_ZoneSpawningCore.initialize()

    print("[MUGGY_ZoneSpawning] SERVER: Zone spawning handler initialized")
end

return MUGGY_ZoneSpawningHandler
