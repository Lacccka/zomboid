local SandboxFactory      = require('HDCP_IVP_Sandbox')
local DistributionFactory = require('service/HDCP_IVP_Distribution')

local HDCP_IVP_Magazines  = {}

function HDCP_IVP_Magazines.new(deps)
    local Constants         = deps and deps.Constants or require('HDCP_IVP_Constants')
    local Sandbox           = deps and deps.Sandbox or SandboxFactory.new()
    local Distribution      = deps and deps.Distribution or DistributionFactory.new()
    local bagsAndContainers = deps and deps.BagsAndContainers
    local clutterTables     = deps and deps.ClutterTables
    local proceduralDist    = deps and deps.ProceduralDistributions
    local VehicleDist       = deps and deps.VehicleDistributions
    local distributions     = deps and deps.Distributions

    local itemName          = Constants.ITEMS.MAGAZINE

    local common            = Sandbox.applySpawnRate("magazine", Constants.DROP_RATE.COMMON)
    local uncommon          = Sandbox.applySpawnRate("magazine", Constants.DROP_RATE.UNCOMMON)
    local rare              = Sandbox.applySpawnRate("magazine", Constants.DROP_RATE.RARE)
    local epic              = Sandbox.applySpawnRate("magazine", Constants.DROP_RATE.EPIC)
    local legendary         = Sandbox.applySpawnRate("magazine", Constants.DROP_RATE.LEGENDARY)
    local mythic            = Sandbox.applySpawnRate("magazine", Constants.DROP_RATE.MYTHIC)
    local relic             = Sandbox.applySpawnRate("magazine", Constants.DROP_RATE.RELIC)
    local divine            = Sandbox.applySpawnRate("magazine", Constants.DROP_RATE.DIVINE)
    local godlike           = Sandbox.applySpawnRate("magazine", Constants.DROP_RATE.GODLIKE)

    local module            = {}

    module.include          = function()
        Distribution.include(bagsAndContainers, {
            SurvivorBag      = { items = { itemName, rare } },
            SurvivorBag_Mid  = { items = { itemName, rare } },
            SurvivorBag_Late = { items = { itemName, rare } },
        })

        Distribution.include(clutterTables, {
            ClosetJunk = { items = { itemName, divine } },
            BinJunk    = { items = { itemName, godlike } },
        })

        Distribution.include(proceduralDist.list, {
            BookstoreAutomotive        = { items = { itemName, epic } },
            BookstoreMisc              = { items = { itemName, epic } },
            CarSupplyLiterature        = { items = { itemName, rare } },
            CarSupplyMagazines         = { items = { itemName, uncommon } },
            CarDealerDesk              = { items = { itemName, common } },
            CrateMagazines             = { items = { itemName, legendary } },
            CrateMechanics             = { items = { itemName, legendary } },
            GasStorageMechanics        = { items = { itemName, legendary } },
            LibraryMagazines           = { items = { itemName, legendary } },
            LivingRoomShelf            = { items = { itemName, relic } },
            LivingRoomShelfClassy      = { items = { itemName, relic } },
            LivingRoomShelfRedneck     = { items = { itemName, relic } },
            LivingRoomSideTable        = { items = { itemName, relic } },
            LivingRoomSideTableClassy  = { items = { itemName, relic } },
            LivingRoomSideTableRedneck = { items = { itemName, relic } },
            LivingRoomWardrobe         = { items = { itemName, relic } },
            MagazineRackMixed          = { items = { itemName, legendary } },
            MechanicOutfit             = { items = { itemName, epic } },
            MechanicTools              = { items = { itemName, epic } },
            MechanicShelfBooks         = { items = { itemName, epic } },
            MechanicSpecial            = { items = { itemName, legendary } },
            NolansDesk                 = { items = { itemName, common } },
            PostOfficeMagazines        = { items = { itemName, legendary } },
            RecRoomShelf               = { items = { itemName, relic } },
            SafehouseBookShelf         = { items = { itemName, legendary } },
            SafehouseFireplace         = { items = { itemName, relic } },
            SafehouseFireplace_Late    = { items = { itemName, relic } },
            ShelfGeneric               = { items = { itemName, relic } },
            StoreShelfMechanics        = { items = { itemName, legendary } },
            ToolStoreBooks             = { items = { itemName, epic } },
            UniversityLibraryMagazines = { items = { itemName, legendary } },
        })

        Distribution.include(VehicleDist, {
            SurvivalistTruckBed                = { items = { itemName, legendary } },
            PostalTruckBed                     = { items = { itemName, mythic } },
            MechanicGloveBox                   = { items = { itemName, epic } },
            MechanicTruckBed                   = { items = { itemName, epic } },
            MechanicSeatFront                  = { items = { itemName, epic } },
            PickUpTruckLights_AirportGloveBox  = { items = { itemName, epic } },
            PickUpTruckLights_AirportSeatFront = { items = { itemName, epic } },
            MobileLibraryTruckBed              = { items = { itemName, rare } },
        })

        table.insert(distributions, #distributions + 1, {
            all              = {
                postbox   = { items = { itemName, relic } },
                sidetable = { items = { itemName, relic } },
            },
            Bag_BreakdownBag = { items = { itemName, rare } },
            Bag_Mail         = { items = { itemName, legendary } },
            Bag_Satchel_Mail = { items = { itemName, legendary } },
            Toolbox_Mechanic = { items = { itemName, epic } },
        })
    end

    return module
end

return HDCP_IVP_Magazines
