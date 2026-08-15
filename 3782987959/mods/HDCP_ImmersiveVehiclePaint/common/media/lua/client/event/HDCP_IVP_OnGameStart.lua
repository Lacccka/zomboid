local SandboxFactory = require('HDCP_IVP_Sandbox')

local HDCP_IVP_OnGameStart = {}

function HDCP_IVP_OnGameStart.new(deps)
    local Constants = deps and deps.Constants or require("HDCP_IVP_Constants")
    local Helpers   = deps and deps.Helpers or require("HDCP_IVP_Helpers")
    local Sandbox   = deps and deps.Sandbox or SandboxFactory.new()
    local Noises    = deps and deps.Noises or require("HDCP_IVP_Noises")

    local function toOption(multiplier)
        local result = (multiplier - 1)

        local symbol = result > 0 and "+" or ""

        if result == 0 then
            return "100%"
        else
            return string.format("%s%.0f%%", symbol, result * 100)
        end
    end

    local module = {}

    module.hasRun = false

    module.run = function()
        if not getDebug() then return end

        if module.hasRun then return end

        module.hasRun = true

        Helpers.noise(Noises.CLIENT_SCRIPT_LOADED)
        Helpers.noise(Noises.FETCHING_SETTINGS)
        Helpers.noise(Noises.MOD_VERSION:format(Constants.MOD_VERSION))

        local spraySpawnRate     = Sandbox.getSpawnRate("spray")
        local magazineSpawnRate  = Sandbox.getSpawnRate("magazine")
        local toolSpawnRate      = Sandbox.getSpawnRate("tool")

        local sprayMultiplier    = Sandbox.getSpawnMultiplier(spraySpawnRate)
        local magazineMultiplier = Sandbox.getSpawnMultiplier(magazineSpawnRate)
        local toolMultiplier     = Sandbox.getSpawnMultiplier(toolSpawnRate)

        if sprayMultiplier == 1.0 and magazineMultiplier == 1.0 and toolMultiplier == 1.0 then
            Helpers.noise(Noises.DEFAULT_MODE)
        else
            Helpers.noise(Noises.CUSTOM_MODE)
        end

        Helpers.noise(Noises.SPRAY_SPAWN_RATE:format(toOption(sprayMultiplier)))
        Helpers.noise(Noises.MAGAZINE_SPAWN_RATE:format(toOption(magazineMultiplier)))
        Helpers.noise(Noises.TOOL_SPAWN_RATE:format(toOption(toolMultiplier)))
    end

    return module
end

return HDCP_IVP_OnGameStart
