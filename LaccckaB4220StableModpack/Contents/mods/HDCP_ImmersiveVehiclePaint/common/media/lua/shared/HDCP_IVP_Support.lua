local Constants = require('HDCP_IVP_Constants')

local tableInsert = table.insert

_G.ImmersiveVehiclePaint = {}

function _G.ImmersiveVehiclePaint.includeVehicles(vehicleIds)
    for _, vehicleId in pairs(vehicleIds) do
        assert(type(vehicleId) == "string", "vehicleId must be a string")

        assert(vehicleId:find("%."), "vehicleId must include a module name")

        tableInsert(Constants.ALLOWED_VEHICLES, { id = vehicleId })
    end
end
