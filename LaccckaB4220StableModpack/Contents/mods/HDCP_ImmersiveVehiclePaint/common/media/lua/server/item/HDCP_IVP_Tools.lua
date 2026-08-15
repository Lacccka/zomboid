local SandboxFactory      = require('HDCP_IVP_Sandbox')
local DistributionFactory = require('service/HDCP_IVP_Distribution')

local HDCP_IVP_Tools      = {}

function HDCP_IVP_Tools.new(deps)
    local Constants         = deps and deps.Constants or require('HDCP_IVP_Constants')
    local Sandbox           = deps and deps.Sandbox or SandboxFactory.new()
    local Distribution      = deps and deps.Distribution or DistributionFactory.new()
    local bagsAndContainers = deps and deps.BagsAndContainers
    local clutterTables     = deps and deps.ClutterTables
    local proceduralDist    = deps and deps.ProceduralDistributions
    local VehicleDist       = deps and deps.VehicleDistributions
    local distributions     = deps and deps.Distributions

    local itemType          = Constants.ITEMS.SANDING_BLOCK

    local common            = Sandbox.applySpawnRate('tool', Constants.DROP_RATE.COMMON)
    local uncommon          = Sandbox.applySpawnRate('tool', Constants.DROP_RATE.UNCOMMON)
    local rare              = Sandbox.applySpawnRate('tool', Constants.DROP_RATE.RARE)
    local epic              = Sandbox.applySpawnRate('tool', Constants.DROP_RATE.EPIC)
    local legendary         = Sandbox.applySpawnRate('tool', Constants.DROP_RATE.LEGENDARY)
    local mythic            = Sandbox.applySpawnRate('tool', Constants.DROP_RATE.MYTHIC)
    local relic             = Sandbox.applySpawnRate('tool', Constants.DROP_RATE.RELIC)
    local divine            = Sandbox.applySpawnRate('tool', Constants.DROP_RATE.DIVINE)
    local godlike           = Sandbox.applySpawnRate('tool', Constants.DROP_RATE.GODLIKE)

    local module            = {}

    module.include          = function()
        Distribution.include(bagsAndContainers, {
            BanditBag       = { items = { itemType, relic } },
            BanditBag_Early = { items = { itemType, relic } },
            BanditBag_Mid   = { items = { itemType, relic } },
            BanditBag_Late  = { items = { itemType, relic } },
        })

        Distribution.include(clutterTables, {
            ClosetJunk = { items = { itemType, divine } },
            BinJunk    = { items = { itemType, godlike } },
            TrunkJunk  = { items = { itemType, epic } },
        })

        Distribution.include(proceduralDist.list, {
            CarSupplyTools          = { items = { itemType, uncommon } },
            CrateMechanics          = { items = { itemType, epic } },
            CrateRandomJunk         = { items = { itemType, divine } },
            CrateTools              = { items = { itemType, epic } },
            CrateToolsOld           = { items = { itemType, epic } },
            GarageMechanics         = { items = { itemType, relic } },
            GarageTools             = { items = { itemType, epic } },
            GigamartTools           = { items = { itemType, epic } },
            MechanicTools           = { items = { itemType, epic } },
            MechanicShelfSuspension = { items = { itemType, rare } },
            MechanicShelfTools      = { items = { itemType, epic } },
            MechanicShelfWheels     = { items = { itemType, epic } },
            MechanicSpecial         = { items = { itemType, rare } },
            StoreShelfMechanics     = { items = { itemType, mythic } },
            ToolCabinetMechanics    = { items = { itemType, epic } },
            ToolStoreTools          = { items = { itemType, epic } },
        })

        Distribution.include(VehicleDist, {
            MechanicGloveBox  = { items = { itemType, epic } },
            MechanicTruckBed  = { items = { itemType, rare } },
            MechanicSeatFront = { items = { itemType, epic } },
        })

        table.insert(distributions, #distributions + 1, {
            Bag_BreakdownBag = { items = { itemType, common } },
            Bag_ToolBag      = { items = { itemType, legendary } },
            Toolbox_Mechanic = { items = { itemType, rare } },
            ToolsCache1      = {
                ToolsBox          = { items = { itemType, epic } },
                counter           = { items = { itemType, epic } },
                Bag_DuffelBagTINT = { items = { itemType, epic } },
            }
        })
    end

    return module
end

return HDCP_IVP_Tools
