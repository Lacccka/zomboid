local HDCP_IVP_Sandbox = {}

function HDCP_IVP_Sandbox.new(deps)
    local vars = deps and deps.SandboxVars or SandboxVars

    local module = {}

    function module.getSpawnRate(itemType)
        local rateMap = {
            ["spray"]    = vars.ImmersiveVehiclePaint.SpraySpawnRate or 3,
            ["magazine"] = vars.ImmersiveVehiclePaint.MagazineSpawnRate or 3,
            ["tool"]     = vars.ImmersiveVehiclePaint.ToolSpawnRate or 3,
        }

        return rateMap[itemType] or 3
    end

    function module.getSpawnMultiplier(spawnRate)
        local multipliers = {
            [1] = 0.25,
            [2] = 0.50,
            [3] = 1.00,
            [4] = 1.50,
            [5] = 2.00,
        }

        return multipliers[spawnRate] or 1.0
    end

    function module.applySpawnRate(itemType, value)
        local spawnRate = module.getSpawnRate(itemType)

        local multiplier = module.getSpawnMultiplier(spawnRate)

        return value * multiplier
    end

    return module
end

return HDCP_IVP_Sandbox
