local SandboxFactory            = require('HDCP_IVP_Sandbox')
local DistributionFactory       = require('service/HDCP_IVP_Distribution')

local tableInsert               = table.insert

local HDCP_IVP_AutomotiveSprays = {}

function HDCP_IVP_AutomotiveSprays.new(deps)
    local Constants         = deps and deps.Constants or require('HDCP_IVP_Constants')
    local Sandbox           = deps and deps.Sandbox or SandboxFactory.new()
    local Distribution      = deps and deps.Distribution or DistributionFactory.new()
    local bagsAndContainers = deps and deps.BagsAndContainers
    local clutterTables     = deps and deps.ClutterTables
    local proceduralDist    = deps and deps.ProceduralDistributions
    local VehicleDist       = deps and deps.VehicleDistributions
    local distributions     = deps and deps.Distributions

    local emptyPaintCan     = Constants.ITEMS.EMPTY_PAINT_CAN
    local emptyPrimerCan    = Constants.ITEMS.EMPTY_PRIMER_CAN
    local warmBox           = Constants.ITEMS.SPRAY_PAINT_BOX_WARM
    local coolBox           = Constants.ITEMS.SPRAY_PAINT_BOX_COOL
    local neutralBox        = Constants.ITEMS.SPRAY_PAINT_BOX_NEUTRAL
    local usedPaintDummy    = Constants.ITEMS.USED_SPRAY_PAINT_DUMMY
    local usedPrimerDummy   = Constants.ITEMS.USED_SPRAY_PRIMER_DUMMY
    local newPaintDummy     = Constants.ITEMS.NEW_SPRAY_PAINT_DUMMY
    local newPrimerDummy    = Constants.ITEMS.NEW_SPRAY_PRIMER_DUMMY

    local common            = Sandbox.applySpawnRate("spray", Constants.DROP_RATE.COMMON)
    local uncommon          = Sandbox.applySpawnRate("spray", Constants.DROP_RATE.UNCOMMON)
    local rare              = Sandbox.applySpawnRate("spray", Constants.DROP_RATE.RARE)
    local epic              = Sandbox.applySpawnRate("spray", Constants.DROP_RATE.EPIC)
    local legendary         = Sandbox.applySpawnRate("spray", Constants.DROP_RATE.LEGENDARY)
    local mythic            = Sandbox.applySpawnRate("spray", Constants.DROP_RATE.MYTHIC)
    local relic             = Sandbox.applySpawnRate("spray", Constants.DROP_RATE.RELIC)
    local divine            = Sandbox.applySpawnRate("spray", Constants.DROP_RATE.DIVINE)
    local godlike           = Sandbox.applySpawnRate("spray", Constants.DROP_RATE.GODLIKE)

    local module            = {}

    module.include          = function()
        Distribution.include(bagsAndContainers, {
            BanditBag         = { items = { usedPaintDummy, common } },
            BanditBag_Early   = { items = { usedPaintDummy, common } },
            BanditBag_Mid     = { items = { usedPaintDummy, common } },
            BanditBag_Late    = { items = { usedPaintDummy, common } },
            Parcel_ExtraSmall = { items = { newPaintDummy, rare } },
            Parcel_Small      = { items = { newPaintDummy, rare } },
        })

        Distribution.include(clutterTables, {
            ClosetJunk = { items = { usedPaintDummy, divine } },
            BinJunk    = { items = { usedPaintDummy, godlike } },
            TrunkJunk  = { items = { usedPaintDummy, relic } },
        })

        Distribution.include(proceduralDist.list, {
            -- retail: cans still on the shelf, plus the boxes they arrived in
            CarSupplyTools       = {
                items = {
                    newPaintDummy, common,
                    newPrimerDummy, common,
                    warmBox, rare,
                    coolBox, rare,
                    neutralBox, rare,
                }
            },
            ToolStorePaint       = {
                items = {
                    newPaintDummy, common,
                    newPrimerDummy, common,
                    warmBox, rare,
                    coolBox, rare,
                    neutralBox, rare,
                }
            },
            ToolStoreMisc        = {
                items = {
                    newPaintDummy, uncommon,
                    newPrimerDummy, uncommon,
                }
            },
            GigamartTools        = {
                items = {
                    newPaintDummy, uncommon,
                    newPrimerDummy, uncommon,
                    warmBox, epic,
                    coolBox, epic,
                    neutralBox, epic,
                }
            },
            StoreShelfMechanics  = {
                items = {
                    newPaintDummy, uncommon,
                    newPrimerDummy, uncommon,
                }
            },

            -- back rooms and warehouses: boxes are wholesale packaging, so they
            -- read best in crates
            CratePaint           = {
                items = {
                    newPaintDummy, common,
                    newPrimerDummy, common,
                    warmBox, uncommon,
                    coolBox, uncommon,
                    neutralBox, uncommon,
                }
            },
            -- a box holds six cans, so wherever one shows up a loose can has to be
            -- easier to find than the box itself
            CrateTools           = {
                items = {
                    newPaintDummy, uncommon,
                    newPrimerDummy, uncommon,
                    warmBox, rare,
                    coolBox, rare,
                    neutralBox, rare,
                }
            },
            CrateMechanics       = {
                items = {
                    newPaintDummy, rare,
                    newPrimerDummy, rare,
                    warmBox, epic,
                    coolBox, epic,
                    neutralBox, epic,
                }
            },
            CrateRandomJunk      = {
                items = {
                    usedPaintDummy, legendary,
                    warmBox, mythic,
                    coolBox, mythic,
                    neutralBox, mythic,
                }
            },

            -- garages and workshops: half-used cans left over from past jobs
            GarageMechanics      = {
                items = {
                    usedPaintDummy, uncommon,
                    usedPrimerDummy, uncommon,
                }
            },
            GarageTools          = {
                items = {
                    usedPaintDummy, uncommon,
                    usedPrimerDummy, uncommon,
                }
            },
            MechanicTools        = {
                items = {
                    usedPaintDummy, rare,
                    usedPrimerDummy, rare,
                }
            },
            MechanicShelfTools   = { items = { usedPaintDummy, rare } },
            MechanicShelfMisc    = {
                items = {
                    usedPaintDummy, rare,
                    usedPrimerDummy, rare,
                }
            },
            ToolCabinetMechanics = { items = { usedPaintDummy, rare } },

            -- households
            WardrobeClassy       = { items = { usedPaintDummy, legendary } },
            WardrobeGeneric      = { items = { usedPaintDummy, legendary } },
            LivingRoomWardrobe   = { items = { usedPaintDummy, legendary } },
        })

        Distribution.include(VehicleDist, {
            SurvivalistTruckBed = { items = { usedPaintDummy, common } },
            PainterTruckBed     = { items = { usedPaintDummy, common } },
            MechanicTruckBed    = { items = { usedPaintDummy, common } },
            BanditTruckBed      = { items = { usedPaintDummy, common } },
        })

        tableInsert(distributions, #distributions + 1, {
            all = {
                bin                 = {
                    items = {
                        emptyPaintCan, epic,
                        emptyPrimerCan, epic,
                    }
                },
                inventorymale       = { items = { usedPaintDummy, legendary } },
                inventoryfemale     = { items = { usedPaintDummy, legendary } },
                Outfit_Bandit       = { items = { usedPaintDummy, common } },
                Outfit_Bandit_Early = { items = { usedPaintDummy, common } },
                Outfit_Bandit_Mid   = { items = { usedPaintDummy, common } },
                Outfit_Bandit_Late  = { items = { usedPaintDummy, common } },
                Outfit_Hobbo        = { items = { usedPaintDummy, common } },
                Outfit_Punk         = { items = { usedPaintDummy, common } },
                Outfit_Rocker       = { items = { usedPaintDummy, common } },
                Outfit_Student      = { items = { usedPaintDummy, common } },
            },
        })
    end

    return module
end

return HDCP_IVP_AutomotiveSprays
