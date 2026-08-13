local distributionTable = VehicleDistributions[1]

VehicleDistributions.HearseGloveBox = {
    rolls = 4,
    items = {
        "AlcoholWipes", 8,
        "Aluminum", 8,
        "Bandage", 4,
        "Bandaid", 10,
        "Battery", 10,
        "BluePen", 8,
        "Cigarettes", 8,
        "Cologne", 4,
        "Comb", 4,
        "CreditCard", 4,
        "Disc_Retail", 2,
        "DuctTape", 2,
        "Earbuds", 2,
        "Eraser", 6,
        "Lighter", 4,
        "Lipstick", 6,
        "Magazine", 10,
        "MakeupEyeshadow", 6,
        "MakeupFoundation", 6,
        "Matches", 8,
        "Mirror", 4,
        "Notebook", 10,
        "Paperclip", 4,
        "Pen", 8,
        "Pencil", 10,
        "Perfume", 4,
        "Razor", 4,
        "RedPen", 8,
        "RubberBand", 6,
        "Scotchtape", 8,
        "Tissue", 10,
        "Twine", 10,
    },
    junk = {
        rolls = 1,
        items = {

            "Camera", 0.03,
            "CameraDisposable", 0.05,
            "CameraExpensive", 0.001,
            "Glasses_Aviators", 0.05,
            "Glasses_SafetyGoggles", 20,
            "Glasses_Sun", 0.1,
            "Gloves_LeatherGloves", 20,
            "Gloves_LeatherGlovesBlack", 0.05,
            "HandTorch", 4,
            "HuntingKnife", 0.1,
            "LouisvilleMap1", 4,
            "LouisvilleMap2", 4,
            "LouisvilleMap3", 4,
            "LouisvilleMap4", 4,
            "LouisvilleMap5", 4,
            "LouisvilleMap6", 4,
            "LouisvilleMap7", 4,
            "LouisvilleMap8", 4,
            "LouisvilleMap9", 4,
            "MarchRidgeMap", 4,
            "MuldraughMap", 4,
            "Pistol", 0.8,
            "Pistol2", 0.6,
            "Radio.CDplayer", 2,
            "Radio.WalkieTalkie2", 2,
            "Radio.WalkieTalkie3", 1,
            "Revolver_Short", 0.8,
            "RiversideMap", 4,
            "RosewoodMap", 4,
            "ToiletPaper", 4,
            "Wallet", 4,
            "Wallet2", 4,
            "Wallet3", 4,
            "Wallet4", 4,
            "WestpointMap", 4,
            "WhiskeyFull", 0.5,
        }
    }
}

VehicleDistributions.HearseTruckBed = {
    rolls = 4,
    items = {
        "Garbagebag", 6,
        "PopBottleEmpty", 4,
        "PopEmpty", 4,
        "RubberBand", 6,
        "WaterBottleEmpty", 24,

    },
    junk = {
        rolls = 1,
        items = {
	    "CorpseMale", 10,
	    "CorpseFemale", 10,
            "FirstAidKit", 4,
            "Jack", 2,
            "LugWrench", 8,
            "Screwdriver", 10,
            "TirePump", 8,
            "Wrench", 8,
        }
    }
}

VehicleDistributions.Hearse = {

    TruckBed = VehicleDistributions.HearseTruckBed;

    TruckBedOpen = VehicleDistributions.HearseTruckBed;

    TrailerTrunk =  VehicleDistributions.HearseTruckBed;

    GloveBox = VehicleDistributions.HearseGloveBox;

    SeatRearLeft = VehicleDistributions.Seat;
    SeatRearRight = VehicleDistributions.Seat;
}

VehicleDistributions.MilkTruckGloveBox = {
    rolls = 4,
    items = {
        "AlcoholWipes", 8,
        "Aluminum", 8,
        "Bandage", 4,
        "Bandaid", 10,
        "Battery", 10,
        "BluePen", 8,
        "Cigarettes", 8,
        "Cologne", 4,
        "Comb", 4,
        "CreditCard", 4,
        "Disc_Retail", 2,
        "DuctTape", 2,
        "Earbuds", 2,
        "Eraser", 6,
        "Lighter", 4,
        "Lipstick", 6,
        "Magazine", 10,
        "MakeupEyeshadow", 6,
        "MakeupFoundation", 6,
        "Matches", 8,
        "Mirror", 4,
        "Notebook", 10,
        "Paperclip", 4,
        "Pen", 8,
        "Pencil", 10,
        "Perfume", 4,
        "Razor", 4,
        "RedPen", 8,
        "RubberBand", 6,
        "Scotchtape", 8,
        "Tissue", 10,
        "Twine", 10,
    },
    junk = {
        rolls = 1,
        items = {

            "Camera", 0.03,
            "CameraDisposable", 0.05,
            "CameraExpensive", 0.001,
            "Glasses_Aviators", 0.05,
            "Glasses_SafetyGoggles", 20,
            "Glasses_Sun", 0.1,
            "Gloves_LeatherGloves", 20,
            "Gloves_LeatherGlovesBlack", 0.05,
            "HandTorch", 4,
            "HuntingKnife", 0.1,
            "LouisvilleMap1", 4,
            "LouisvilleMap2", 4,
            "LouisvilleMap3", 4,
            "LouisvilleMap4", 4,
            "LouisvilleMap5", 4,
            "LouisvilleMap6", 4,
            "LouisvilleMap7", 4,
            "LouisvilleMap8", 4,
            "LouisvilleMap9", 4,
            "MarchRidgeMap", 4,
            "MuldraughMap", 4,
            "Pistol", 0.8,
            "Pistol2", 0.6,
            "Radio.CDplayer", 2,
            "Radio.WalkieTalkie2", 2,
            "Radio.WalkieTalkie3", 1,
            "Revolver_Short", 0.8,
            "RiversideMap", 4,
            "RosewoodMap", 4,
            "ToiletPaper", 4,
            "Wallet", 4,
            "Wallet2", 4,
            "Wallet3", 4,
            "Wallet4", 4,
            "WestpointMap", 4,
            "WhiskeyFull", 0.5,
        }
    }
}

VehicleDistributions.MilkTruckTruckBed = {
    rolls = 4,
    items = {
        "Garbagebag", 6,
        "Milk", 50,
        "Cheese", 40,
        "Tofu", 10,
        "EggCarton", 4,
        "Butter", 30,
        "Icecream", 10,
        "ConeIcecream", 20,
        "Yoghurt", 20,
        "Plasticbag", 10,
        "CakeRedVelvet", 10,
        "PiePumpkin", 10,
        "CakeChocolate", 10,
        "CakeStrawberryShortcake", 10,
        "Cupcake", 10,
        "PopBottleEmpty", 4,
        "PopEmpty", 4,
        "Hat_ChefHat", 6,
        "Gloves_WhiteTINT", 4,
        "RubberBand", 6,
        "WaterBottleEmpty", 24,

    },
    junk = {
        rolls = 1,
        items = {
            "BaseballBat", 1,
            "FirstAidKit", 4,
            "Jack", 2,
            "LugWrench", 8,
            "Screwdriver", 10,
            "TirePump", 8,
            "Wrench", 8,
        }
    }
}
VehicleDistributions.PizzaTruckTruckBed = {
    rolls = 4,
    items = {
        "Garbagebag", 6,
	"PizzaRecipe", 30,
	"Pizza", 50,
	"PizzaWhole", 40,
	"Pop2", 40,
        "PopBottleEmpty", 4,
        "PopEmpty", 4,
        "Hat_ChefHat", 6,
        "Gloves_WhiteTINT", 4,
        "RubberBand", 6,
        "WaterBottleEmpty", 24,

    },
    junk = {
        rolls = 1,
        items = {
            "BaseballBat", 1,
            "FirstAidKit", 4,
            "Jack", 2,
            "LugWrench", 8,
            "Screwdriver", 10,
            "TirePump", 8,
            "Wrench", 8,
        }
    }
}

VehicleDistributions.MilkTruck = {

    TruckBed = VehicleDistributions.MilkTruckTruckBed;

    TruckBedOpen = VehicleDistributions.MilkTruckTruckBed;

    TrailerTrunk =  VehicleDistributions.MilkTruckTruckBed;

    GloveBox = VehicleDistributions.GloveBox;

    SeatRearLeft = VehicleDistributions.Seat;
    SeatRearRight = VehicleDistributions.Seat;
}
VehicleDistributions.PizzaTruck = {

    TruckBed = VehicleDistributions.PizzaTruckTruckBed;

    TruckBedOpen = VehicleDistributions.PizzaTruckTruckBed;

    TrailerTrunk =  VehicleDistributions.PizzaTruckTruckBed;

    GloveBox = VehicleDistributions.GloveBox;

    SeatRearLeft = VehicleDistributions.Seat;
    SeatRearRight = VehicleDistributions.Seat;
}







distributionTable["pzkHearse"] = { Normal = VehicleDistributions.Hearse; }
distributionTable["pzkStepVanMilk"] = { Normal = VehicleDistributions.MilkTruck; }
distributionTable["pzkStepVanIceCream"] = { Normal = VehicleDistributions.MilkTruck; }
distributionTable["pzkStepVanPizza"] = { Normal = VehicleDistributions.PizzaTruck; }
distributionTable["pzkStepVanHotDog"] = { Normal = VehicleDistributions.PizzaTruck; }


distributionTable["pzkMinivanStellaris"] = distributionTable["ModernCar"]
distributionTable["pzkVanCamper"] = distributionTable["ModernCar"]
distributionTable["pzkVanBrig"] = distributionTable["ModernCar"]
distributionTable["pzkChevalierMaroca"] = distributionTable["SportsCar"]

distributionTable["pzkVanBoxFiretruck"] = distributionTable["PickUpVanLightsFire"]
distributionTable["pzkVanBoxAmbulance"] = distributionTable["VanAmbulance"]

distributionTable["pzkDashOhio"] = distributionTable["PickUpVanLightsPolice"] 
distributionTable["pzkFranklinGalloperPolice"] = distributionTable["PickUpVanLightsPolice"] 
distributionTable["pzkVanPolice"] = distributionTable["PickUpVanLightsPolice"] 
distributionTable["pzkStepVanSwat"] = distributionTable["PickUpVanLightsPolice"] 

distributionTable["pzkPickupFranklin"] = distributionTable["PickUpTruck"]
distributionTable["pzkFranklinGalloper"] = distributionTable["PickUpVan"]
distributionTable["pzkVanMcCoy"] = distributionTable["VanSpecial1"] 
distributionTable["pzkStepVanUPZ"] = distributionTable["VanSpecial2"] 

distributionTable["pzkDashDeluxo"] = distributionTable["SportsCar"] 

distributionTable["pzkHMMV"] = distributionTable["OffRoad"] 
distributionTable["pzkHMMV2"] = distributionTable["PickUpVanLightsPolice"] 
distributionTable["pzkHMMV3"] = distributionTable["PickUpVanLightsPolice"] 

distributionTable["pzkLimo"] = distributionTable["CarNormal"] 

distributionTable["pzkPickUpTruck93"] = distributionTable["PickUpTruck"] 
distributionTable["pzkVanBox"] = distributionTable["StepVan"] 
distributionTable["pzkCarMuscle"] = distributionTable["CarNormal"] 
distributionTable["pzkFranklinPony"] = distributionTable["CarNormal"] 
distributionTable["pzkPickUpTruckWoodboarded"] = distributionTable["PickUpTruck"] 

distributionTable["pzkFranklinTriumph"] = distributionTable["CarNormal"] 
distributionTable["pzkFranklinTriumphTaxi"] = distributionTable["CarNormal"] 
distributionTable["pzkFranklinTriumphPolice"] = distributionTable["PickUpVanLightsPolice"] 



distributionTable["pzkChevalierCeriseSedan"] = distributionTable["CarNormal"] 
distributionTable["pzkChevalierCeriseSedanPolice"] = distributionTable["PickUpVanLightsPolice"] 
distributionTable["pzkChevalierCeriseSedanTaxi"] = distributionTable["CarNormal"] 
distributionTable["pzkDashHellion"] = distributionTable["CarNormal"] 
distributionTable["pzkDashHellionTaxi"] = distributionTable["CarNormal"] 
distributionTable["Vehicles_pzkDashMayor"] = distributionTable["CarNormal"] 
distributionTable["pzkDashMayorPolice"] = distributionTable["PickUpVanLightsPolice"] 
distributionTable["pzkDashMayorTaxi"] = distributionTable["CarNormal"] 
distributionTable["Vehicles_pzkDashRapier"] = distributionTable["CarNormal"] 
distributionTable["pzkFranklinTriumphTWD"] = distributionTable["CarNormal"] 
distributionTable["pzkFranklinTriumphTWDPolice"] = distributionTable["PickUpVanLightsPolice"] 
distributionTable["pzkFranklinTriumphTWDTaxi"] = distributionTable["CarNormal"] 
distributionTable["pzkCeriseStationWagon"] = distributionTable["CarNormal"] 
distributionTable["pzkDashMayorStationWagon"] = distributionTable["CarNormal"] 
distributionTable["pzkRapierStationWagon"] = distributionTable["CarNormal"] 
distributionTable["pzkTriumphStationWagon"] = distributionTable["CarNormal"] 
distributionTable["pzkTriumphStationWagonTaxi"] = distributionTable["CarNormal"] 
distributionTable["Vehicles_VanSeatsTaxi"] = distributionTable["CarNormal"] 
distributionTable["pzkFranklinGalloperRanger"] = distributionTable["CarNormal"] 

distributionTable["pzkDashRoyal"] = distributionTable["CarNormal"]
distributionTable["pzkDashRoyalGrand"] = distributionTable["CarNormal"]
distributionTable["pzkChevalierDownhill"] = distributionTable["CarNormal"]
distributionTable["pzkDashTornado"] = distributionTable["CarNormal"]
distributionTable["pzkMastersonLady"] = distributionTable["ModernCar"]
distributionTable["pzkDashPhoenix"] = distributionTable["SportsCar"]
distributionTable["pzkDashPhoenixBandit"] = distributionTable["ModernCar"]
distributionTable["pzkChevalierE6"] = distributionTable["PickUpTruck"] 
distributionTable["pzkChevalierF6"] = distributionTable["PickUpTruck"] 
distributionTable["pzkFranklinTriumphTWDFire"] = distributionTable["PickUpVanLightsFire"]
distributionTable["pzkChevalierCeriseSedanFire"] = distributionTable["PickUpVanLightsFire"]
distributionTable["pzkFranklinGalloperFire"] = distributionTable["PickUpVanLightsFire"]
distributionTable["pzkChevalierRoadrunner"] = distributionTable["SportsCar"]
distributionTable["pzkMastersonInitial"] = distributionTable["SportsCar"]
distributionTable["pzkMastersonXSR"] = distributionTable["ModernCar"]
distributionTable["pzkContinentalSpirit"] = distributionTable["SportsCar"]
distributionTable["pzkDashNoble"] = distributionTable["SportsCar"]
distributionTable["pzkFranklinStallion"] = distributionTable["ModernCar"]
distributionTable["pzkFranklinStallion2"] = distributionTable["ModernCar"]
distributionTable["pzkFranklinStallionSport"] = distributionTable["SportsCar"]
distributionTable["pzkFranklinStallionPolice"] = distributionTable["PickUpVanLightsPolice"] 
distributionTable["pzkFranklinIslander"] = distributionTable["SportsCar"]
distributionTable["pzkFranklinHomelander"] = distributionTable["CarNormal"]
distributionTable["pzkMastersonCrown"] = distributionTable["CarNormal"]

distributionTable["pzkFranklinTruckBox"] = distributionTable["ModernCar"]
distributionTable["pzkFranklinBankTruck"] = distributionTable["PickUpVanLightsPolice"] 
distributionTable["pzkFranklinSwatTruck"] = distributionTable["PickUpVanLightsPolice"] 
distributionTable["pzkFranklinTruckBed"] = distributionTable["PickUpTruck"]
distributionTable["pzkFranklinTruckShort"] = distributionTable["PickUpTruck"]
distributionTable["pzkFranklinTruckCab"] = distributionTable["PickUpTruck"]
distributionTable["pzkFranklinTruckMil"] = distributionTable["PickUpVanLightsPolice"] 
distributionTable["pzkFranklinTruckBus"] = distributionTable["CarNormal"]
distributionTable["pzkFranklinTruckBusPrison"] = distributionTable["PickUpVanLightsPolice"] 
distributionTable["pzkFranklinTruckDump"] = distributionTable["PickUpTruck"]
distributionTable["pzkFranklinTruckFire"] = distributionTable["PickUpVanLightsFire"]
distributionTable["pzkFranklinTruckFireTanker"] = distributionTable["PickUpVanLightsFire"]
distributionTable["pzkFranklinTruckFlatbed"] = distributionTable["PickUpTruck"]
distributionTable["pzkFranklinTruckGarbage"] = distributionTable["PickUpTruck"]
distributionTable["pzkFranklinTruckPropane"] = distributionTable["PickUpTruck"]
distributionTable["pzkFranklinTruckPropane2"] = distributionTable["PickUpTruck"]
distributionTable["pzkFranklinTruckRV"] = distributionTable["CarNormal"]
distributionTable["pzkFranklinTruckTankerFossoil"] = distributionTable["PickUpTruck"]
distributionTable["pzkFranklinTruckTankerSeptic"] = distributionTable["PickUpTruck"]
distributionTable["pzkFranklinTruckTankerWater"] = distributionTable["PickUpTruck"]
distributionTable["pzkFranklinTruckTow"] = distributionTable["PickUpTruck"]
distributionTable["pzkFranklinTruckUtility"] = distributionTable["PickUpTruck"]
distributionTable["pzkFranklinTruckMilTankerWater"] = distributionTable["PickUpVanLightsPolice"] 

distributionTable["pzkMinivanC22"] = distributionTable["ModernCar"]
distributionTable["pzkMinivanChev"] = distributionTable["CarNormal"] 
distributionTable["pzkMinivanConvoy"] = distributionTable["CarNormal"] 
distributionTable["pzkMinivanMPV"] = distributionTable["ModernCar"]
distributionTable["pzkMinivanStellarisMail"] = distributionTable["VanSpecial2"] 
distributionTable["pzkMinivanStellarisTaxi"] = distributionTable["CarNormal"] 
distributionTable["pzkMinivanT3"] = distributionTable["CarNormal"] 
distributionTable["pzkMinivanT3C"] = distributionTable["CarNormal"] 
distributionTable["pzkMinivanTask"] = distributionTable["ModernCar"]
distributionTable["pzkMinivan2"] = distributionTable["CarNormal"] 
distributionTable["pzkMinivanPrev"] = distributionTable["ModernCar"]
distributionTable["pzkVanMultivan"] = distributionTable["PickUpTruck"]
distributionTable["pzkFranklinTruckBusArmy"] = distributionTable["PickUpVanLightsPolice"] 

distributionTable["pzkMastersonScout4D"] = distributionTable["ModernCar"]
distributionTable["pzkChevalierPickupCrewLong"] = distributionTable["CarNormal"]
distributionTable["pzkChevalierPickupCrewMedium"] = distributionTable["CarNormal"]
distributionTable["pzkChevalierProvince"] = distributionTable["CarNormal"]
distributionTable["pzkChevalierProvinceLong"] = distributionTable["CarNormal"]
distributionTable["pzkChevalierLaserModern"] = distributionTable["CarNormal"]
distributionTable["pzkChevalierE6Van"] = distributionTable["PickUpTruck"]
distributionTable["pzkChevalierF6Van"] = distributionTable["PickUpTruck"]
distributionTable["pzkChevalierLaserCUCV"] = distributionTable["PickUpVanLightsPolice"] 
distributionTable["pzkChevalierProvinceLongCUCV"] = distributionTable["PickUpVanLightsPolice"] 
distributionTable["pzkChevalierLaserOffroader"] = distributionTable["ModernCar"]
distributionTable["pzkChevalierLaserFire"] = distributionTable["PickUpVanLightsFire"]
distributionTable["pzkChevalierLaserPolice"] = distributionTable["PickUpVanLightsPolice"] 
distributionTable["pzkChevalierLaserRanger"] = distributionTable["PickUpVanLightsPolice"] 
distributionTable["pzkFtypeTowTruck"] = distributionTable["PickUpTruck"]
distributionTable["pzkChevalierTowTruck"] = distributionTable["PickUpTruck"]
distributionTable["pzkChevalierTowTruckPolice"] = distributionTable["PickUpVanLightsPolice"] 
distributionTable["pzkChevalierTowTruckFire"] = distributionTable["PickUpVanLightsFire"]
distributionTable["pzkMerciaLangBerg"] = distributionTable["ModernCar"]
distributionTable["pzkDashCheyene"] = distributionTable["ModernCar"]
distributionTable["pzkContinentalCruiser"] = distributionTable["ModernCar"]
distributionTable["pzkFranklin350FWagonLong"] = distributionTable["CarNormal"]
distributionTable["pzkFranklin350FPickupCrewLong"] = distributionTable["CarNormal"]
distributionTable["pzkFranklin250FPickupWagonLong"] = distributionTable["CarNormal"]
distributionTable["pzkFranklin250FPickupCrewLong"] = distributionTable["PickUpTruck"]
distributionTable["pzkFranklin250FWagonLong"] = distributionTable["CarNormal"]
distributionTable["pzkFranklin150van"] = distributionTable["CarNormal"]
distributionTable["pzkFranklin150FPickupReg"] = distributionTable["PickUpTruck"]
distributionTable["pzkFranklin150FWagonMedium"] = distributionTable["CarNormal"]
distributionTable["pzkFranklin150FPickupMedium"] = distributionTable["PickUpTruck"]
distributionTable["pzkDashIntruder250WagonLong"] = distributionTable["CarNormal"]
distributionTable["pzkDashIntruder250PickupLong"] = distributionTable["PickUpTruck"]
distributionTable["pzkDashIntruder150short"] = distributionTable["PickUpTruck"]
distributionTable["pzkDashIntruder150RegVan"] = distributionTable["CarNormal"]

distributionTable["pzkMastersonLadyZ"] = distributionTable["CarNormal"]
distributionTable["pzkDashRunnerGeneral"] = distributionTable["CarNormal"]
distributionTable["pzkDashGTA"] = distributionTable["CarNormal"]
distributionTable["pzkFranklinStallionKing"] = distributionTable["CarNormal"]
distributionTable["pzkDashRunner"] = distributionTable["CarNormal"]
distributionTable["pzkDashPiranha"] = distributionTable["CarNormal"]
distributionTable["pzkDashChampion"] = distributionTable["CarNormal"]
distributionTable["pzkDashPhoenix80"] = distributionTable["CarNormal"]
distributionTable["pzkChevalierMaroca80"] = distributionTable["CarNormal"]
distributionTable["pzkContinentalBug"] = distributionTable["CarNormal"]
distributionTable["pzkContinentalBugHerbie"] = distributionTable["SportsCar"]

distributionTable["pzkContinentalBayer330Sport"] = distributionTable["SportsCar"]
distributionTable["pzkContinentalBayer3304D"] = distributionTable["SportsCar"]
distributionTable["pzkContinentalBayer3302D"] = distributionTable["SportsCar"]
distributionTable["pzkContinentalBayer534"] = distributionTable["CarNormal"]
distributionTable["pzkContinentalBayer732"] = distributionTable["CarNormal"]
distributionTable["pzkMerciaLang1240"] = distributionTable["CarNormal"]
distributionTable["pzkMerciaLang12402D"] = distributionTable["SportsCar"]
distributionTable["pzkFranklinTruckSemi"] = distributionTable["PickUpTruck"]
distributionTable["pzkContinentalTRK"] = distributionTable["PickUpVanLightsPolice"] 
distributionTable["pzkFreightlinerFlat2"] = distributionTable["PickUpTruck"]
distributionTable["pzkFreightlinerFlat"] = distributionTable["PickUpTruck"]
distributionTable["pzkPeterbuiltSleeper"] = distributionTable["PickUpTruck"]
distributionTable["pzkPeterbuiltSleeperBandit"] = distributionTable["PickUpTruck"]
distributionTable["pzkPeterbuilt"] = distributionTable["PickUpTruck"]
distributionTable["pzkMerciaLang4000Cabrio"] = distributionTable["SportsCar"]
distributionTable["pzkContinentalBayer330Cabrio "] = distributionTable["SportsCar"]
distributionTable["pzkCarMuscleCabrio"] = distributionTable["SportsCar"]
distributionTable["pzkDashRancherRanger"] = distributionTable["PickUpTruck"]
distributionTable["pzkDashRancherCustom"] = distributionTable["PickUpTruck"]
distributionTable["pzkDashRancherCabrio"] = distributionTable["PickUpTruck"]
distributionTable["pzkChevalierCosetteCabrio"] = distributionTable["SportsCar"]
distributionTable["pzkMastersonSunrise"] = distributionTable["SportsCar"]
distributionTable["pzkMastersonSensation"] = distributionTable["SportsCar"]
distributionTable["pzkMastersonExpander"] = distributionTable["SportsCar"]
distributionTable["pzkSuvCustom"] = distributionTable["PickUpTruck"]

distributionTable["pzkTrailerRegularCourtains"] = distributionTable["PickUpTruck"]
distributionTable["pzkTrailerRegularCourtainsBandit"] = distributionTable["PickUpTruck"]
distributionTable["pzkTrailerRegularFlatbed"] = distributionTable["PickUpTruck"]
distributionTable["pzkTrailerRegularFuelTanker"] = distributionTable["PickUpTruck"]
distributionTable["pzkTrailerHorseBox"] = distributionTable["PickUpTruck"]

distributionTable["pzkContinentalHammermanKnight"] = distributionTable["CarNormal"]

distributionTable["pzkFireTruckFlatPumper"] = distributionTable["PickUpTruck"]
distributionTable["pzkFireTruckFlatLadder"] = distributionTable["PickUpTruck"]
distributionTable["pzkFireTruckFlatSemi"] = distributionTable["PickUpTruck"]

distributionTable["pzkTrailerCamping"] = distributionTable["PickUpTruck"]
distributionTable["pzkTrailerRegularFTSemi"] = distributionTable["PickUpVanLights"]
distributionTable["pzkTrailerTankSmall"] = distributionTable["Trailer"]
distributionTable["pzkTrailerArmyCover"] = distributionTable["Trailer"]
distributionTable["pzkTrailerBoxDual"] = distributionTable["Trailer"]
distributionTable["pzkTrailerBoxPoliceDual"] = distributionTable["PickUpVanLights"]
distributionTable["pzkTrailerTankMedium"] = distributionTable["Trailer"]
distributionTable["pzkTrailerTankSprayer"] = distributionTable["Trailer"]
distributionTable["pzkDualTrailerCover"] = distributionTable["Trailer"]
distributionTable["pzkDualTrailer"] = distributionTable["Trailer"]

distributionTable["pzkTractor"] = distributionTable["PickUpTruck"]
distributionTable["pzkContinentalPfeiffer901"] = distributionTable["SportsCar"]
distributionTable["pzkContinentalPfeiffer901c"] = distributionTable["SportsCar"]
distributionTable["pzkContinentalPfeiffer930"] = distributionTable["SportsCar"]
distributionTable["pzkContinentalPfeiffer930c"] = distributionTable["SportsCar"]

distributionTable["pzkFranklinTriumphWagon"] = distributionTable["CarNormal"]
distributionTable["pzkChevalierCerise93"] = distributionTable["CarNormal"]
distributionTable["pzkChevalierCerise93Taxi"] = distributionTable["CarNormal"]
distributionTable["pzkChevalierCerise93Police"] = distributionTable["PickUpVanLightsPolice"] 
distributionTable["pzkChevalierCerise93Fire"] = distributionTable["PickUpVanLightsFire"]
distributionTable["pzkChevalierCerise93WagonFire"] = distributionTable["PickUpVanLightsFire"]
distributionTable["pzkDashElite2D"] = distributionTable["SportsCar"]
distributionTable["pzkMastersonHarmonyWagon"] = distributionTable["CarNormal"]
distributionTable["pzkMastersonHarmony"] = distributionTable["CarNormal"]
distributionTable["pzkContinentalNord"] = distributionTable["CarNormal"]
distributionTable["pzkContinentalNordWagon"] = distributionTable["CarNormal"]
distributionTable["pzkContinentalPyrenean310"] = distributionTable["SportsCar"]
distributionTable["pzkTransitBus"] = distributionTable["PickUpTruck"]
distributionTable["pzkChevalierCerise93Wagon"] = distributionTable["CarNormal"]
distributionTable["pzkDashHEMTT6x6semi"] = distributionTable["PickUpVanLightsPolice"] 
distributionTable["pzkTriumphTWDStationWagonGriswold"] = distributionTable["CarNormal"]



-- define smashed car like their normal counterpart
distributionTable.pzkFranklinTriumphTWDSmashedRear = distributionTable.pzkFranklinTriumphTWD
distributionTable.pzkFranklinTriumphTWDSmashedFront = distributionTable.pzkFranklinTriumphTWD
distributionTable.pzkFranklinTriumphTWDSmashedLeft = distributionTable.pzkFranklinTriumphTWD
distributionTable.pzkFranklinTriumphTWDSmashedRight = distributionTable.pzkFranklinTriumphTWD

distributionTable.pzkChevalierCeriseSedanSmashedRear = distributionTable.pzkChevalierCeriseSedan
distributionTable.pzkChevalierCeriseSedanSmashedFront = distributionTable.pzkChevalierCeriseSedan
distributionTable.pzkChevalierCeriseSedanSmashedLeft = distributionTable.pzkChevalierCeriseSedan
distributionTable.pzkChevalierCeriseSedanSmashedRight = distributionTable.pzkChevalierCeriseSedan

distributionTable.pzkFranklinHomelanderSmashedRear = distributionTable.pzkFranklinHomelander
distributionTable.pzkFranklinHomelanderSmashedFront = distributionTable.pzkFranklinHomelander
distributionTable.pzkFranklinHomelanderSmashedLeft = distributionTable.pzkFranklinHomelander
distributionTable.pzkFranklinHomelanderSmashedRight = distributionTable.pzkFranklinHomelander

distributionTable.pzkDashMayorSmashedRear = distributionTable.pzkDashMayor
distributionTable.pzkDashMayorSmashedFront = distributionTable.pzkDashMayor
distributionTable.pzkDashMayorSmashedLeft = distributionTable.pzkDashMayor
distributionTable.pzkDashMayorSmashedRight = distributionTable.pzkDashMayor

distributionTable.pzkDashRapierSmashedRear = distributionTable.pzkDashRapier
distributionTable.pzkDashRapierSmashedFront = distributionTable.pzkDashRapier
distributionTable.pzkDashRapierSmashedLeft = distributionTable.pzkDashRapier
distributionTable.pzkDashRapierSmashedRight = distributionTable.pzkDashRapier

distributionTable.pzkCeriseStationWagonSmashedRear = distributionTable.pzkCeriseStationWagon
distributionTable.pzkCeriseStationWagonSmashedFront = distributionTable.pzkCeriseStationWagon
distributionTable.pzkCeriseStationWagonSmashedLeft = distributionTable.pzkCeriseStationWagon
distributionTable.pzkCeriseStationWagonSmashedRight = distributionTable.pzkCeriseStationWagon

distributionTable.pzkDashMayorStationWagonSmashedRear = distributionTable.pzkDashMayorStationWagon
distributionTable.pzkDashMayorStationWagonSmashedFront = distributionTable.pzkDashMayorStationWagon
distributionTable.pzkDashMayorStationWagonSmashedLeft = distributionTable.pzkDashMayorStationWagon
distributionTable.pzkDashMayorStationWagonSmashedRight = distributionTable.pzkDashMayorStationWagon

distributionTable.pzkRapierStationWagonSmashedRear = distributionTable.pzkRapierStationWagon
distributionTable.pzkRapierStationWagonSmashedFront = distributionTable.pzkRapierStationWagon
distributionTable.pzkRapierStationWagonSmashedLeft = distributionTable.pzkRapierStationWagon
distributionTable.pzkRapierStationWagonSmashedRight = distributionTable.pzkRapierStationWagon

distributionTable.pzkTriumphStationWagonSmashedRear = distributionTable.pzkTriumphStationWagon
distributionTable.pzkTriumphStationWagonSmashedFront = distributionTable.pzkTriumphStationWagon
distributionTable.pzkTriumphStationWagonSmashedLeft = distributionTable.pzkTriumphStationWagon
distributionTable.pzkTriumphStationWagonSmashedRight = distributionTable.pzkTriumphStationWagon

--distributionTable.pzkChevalierMaroca80Crashed = distributionTable.pzkChevalierMaroca80


