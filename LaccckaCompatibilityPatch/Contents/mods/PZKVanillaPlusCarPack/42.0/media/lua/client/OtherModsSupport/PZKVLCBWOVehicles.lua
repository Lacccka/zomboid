if getActivatedMods():contains("BanditsWeekOne") then

require "BWOVehicles"

BWOVehicles = BWOVehicles or {}
BWOVehicles.carChoices = BWOVehicles.carChoices or {}


--BWOVehicles.playerCarChoicesOccupation["fireofficer"] = {"Base.PickUpTruckLightsFire"}
--BWOVehicles.playerCarChoicesOccupation["policeofficer"] = {"Base.PickUpVanLightsPolice"}
--BWOVehicles.playerCarChoicesOccupation["mechanics"] = {"Base.SportsCar"}

local pzkProffesionFireEntries = {
"Base.PickUpVanLightsFire",
"Base.PickUpTruckLightsFire",
"Base.pzkVanBoxFiretruck",
"Base.pzkFranklinTriumphTWDFire",
"Base.pzkChevalierCeriseSedanFire",
"Base.pzkFranklinGalloperFire",
"Base.pzkChevalierLaserFire",
"Base.pzkChevalierTowTruckFire",
"Base.pzkChevalierCerise93Fire",
"Base.pzkChevalierCerise93WagonFire"
}

local pzkProffesionDefaultEntries = {
"Base.pzkMastersonCrown",
"Base.pzkFranklinPony",
"Base.pzkMastersonXSR",
"Base.pzkContinentalBug",
"Base.pzkDashVan70",
"Base.pzkFranklinVan70",
"Base.pzkChevalierVan70",
"Base.pzkChevalierMaroca80",
"Base.pzkMinivanStellaris",
"Base.pzkDashTornado",
"Base.pzkCarMuscle",

}

local pzkProffesionPoliceEntries = {
"Base.CarLightsPolice"
}

local pzkProffesionMechanicsEntries = {
"Base.pzkDashDeluxo",
"Base.pzkChevalierMaroca",
"Base.pzkContinentalBayer330Sport",
"Base.pzkFranklinStallionSport",
"Base.pzkMerciaLang4000Cabrio",
"Base.pzkChevalierCosetteCabrio",
"Base.pzkContinentalPyrenean310",
"Base.pzkMastersonRotaryC",
"Base.pzkContinentalSpirit",
"Base.pzkMastersonInitial",
"Base.pzkChevalierRoadrunner",
"Base.pzkDashPhoenixBandit",
"Base.pzkDashPhoenix",
"Base.pzkDashRoyalGrand",
"Base.pzkMastersonSunrise",
"Base.pzkMastersonSensation",
"Base.pzkMastersonExpander",
"Base.pzkDashChampion",
"Base.pzkDashPiranha",
"Base.pzkDashRunner",
"Base.pzkDashGTA",
"Base.pzkMastersonLadyZ",
"Base.pzkChevalierTowTruck",
"Base.pzkFtypeTowTruck",
"Base.pzkMastersonLady"
}

 --   ParkRanger
local pzkProffesionParkRangerEntries = {
"Base.CarLights",
"Base.PickUpVanLights",
"Base.PickUpTruckLights",
"Base.pzkDashOhio",
"Base.pzkFranklinGalloperRanger",
"Base.pzkChevalierLaserRanger",
"Base.pzkDashRancherRanger"
}

  --  ConstructionWorker
local pzkProffesionConstructionWorkerEntries = {
"Base.pzkFranklinTruckCab",
"Base.pzkVanBrig",
"Base.pzkVanMultivan"
}

 --   MilitaryOfficer
local pzkProffesionMilitaryOfficerEntries = {
"Base.pzkDashOhio",
"Base.pzkHMMV2Mil",
"Base.pzkHMMV3Mil",
"Base.pzkHMMV4Mil",
"Base.pzkChevalierProvinceLongCUCV",
"Base.pzkChevalierLaserCUCV"
} 

 --   MilitarySoldier
local pzkProffesionMilitarySoldierEntries = {
"Base.pzkDashOhio",
"Base.pzkChevalierProvinceLongCUCV",
"Base.pzkChevalierLaserCUCV"
} 

 --   SecurityGuard
local pzkProffesionSecurityGuardEntries = {
"Base.pzkFranklinTriumphTWDMall",
"Base.pzkChevalierCeriseSedanMall",
"Base.pzkChevalierNyalaMall",
"Base.pzkDashMayorMall",
"Base.pzkFranklinTriumphTWD91Mall",
"Base.pzkFranklinGalloperMall",
"Base.pzkChevalierLaserMall",
"Base.pzkChevalierLaserMall",
"Base.pzkFranklinGalloperMall",
"Base.pzkFranklinTriumphWagonMall",
"Base.pzkFranklinTriumphMall",
"Base.pzkChevalierCerise93Mall",
"Base.pzkChevalierCerise93WagonMall"
} 

--Farmer
local pzkProffesionFarmerEntries = {
"Base.pzkPickUpTruckWoodboarded",
"Base.pzkPickUpTruck93",
"Base.pzkTractor"
}


 --   ParkRanger
  --  ConstructionWorker
 --   MilitaryOfficer
 --   MilitarySoldier
 --   SecurityGuard
  --   Farmer
 
 
 
 
 --   Salesperson
 --   ITWorker
 --   OfficeWorker
 --   Unemployed
 --   TruckDriver
 --   Cashier
 --   ShopClerk
 --   FastFoodCook
 --   Cook
 --   Chef
 --   Burglar
 --   Drugdealer
 --   Nurse
 --   Doctor
 --   Waiter
  --  CustomerService
 --   Janitor
 --   Secretary
 --   Bookkeeper
 --   Accountant
 --   Teacher



local pzkPoliceEntries = {
"Base.CarLightsPolice",
"Base.CarLightsPolice",
"Base.CarLightsPolice",
"Base.CarLightsPolice",
"Base.CarLightsPolice"
}

local pzkFiremanEntries = {
"Base.PickUpVanLightsFire",
"Base.PickUpTruckLightsFire",
"Base.pzkVanBoxFiretruck",
"Base.pzkFranklinTriumphTWDFire",
"Base.pzkChevalierCeriseSedanFire",
"Base.pzkFranklinGalloperFire",
"Base.pzkFranklinTruckFire",
"Base.pzkFranklinTruckFireTanker",
"Base.pzkChevalierLaserFire",
"Base.pzkChevalierTowTruckFire",
"Base.pzkFireTruckFlatPumper",
"Base.pzkFireTruckFlatLadder",
"Base.pzkChevalierCerise93Fire",
"Base.pzkChevalierCerise93WagonFire"
}


local pzkSWATEntries = {
"Base.pzkFranklinSwatTruck",
"Base.pzkFranklinSwatTruckLouisvilleSWAT",
"Base.pzkStepVanSwatLouisvilleSWAT",
"Base.pzkF350BoxSwat",
"Base.pzkVanBoxSwat"
}

local pzkMedicalEntries = {
"Base.pzkVanBoxAmbulance",
"Base.pzkF350BoxAmbulance"
}

local pzkHazmatsEntries = {
"Base.pzkF350BoxCUCV",
"Base.pzkChevalierLaserCUCV",
"Base.pzkChevalierProvinceLongCUCV",
"Base.pzkHMMV2Mil",
"Base.pzkHMMV3Mil",
"Base.pzkHMMV4Mil",
"Base.pzkFranklinTruckMil"
}

local newPzkVLCEntries = {
"Base.pzkVanBrig",
"Base.pzkChevalierMaroca",
"Base.pzkFranklinGalloper",
"Base.pzkMinivanStellaris",
"Base.pzkPickupFranklin",
"Base.pzkVanCamper",
"Base.pzkVanMcCoy",
"Base.pzkStepVanUPZ",
"Base.pzkStepVanMilk",
"Base.pzkDashDeluxo",
"Base.pzkHMMV",
"Base.pzkHMMV2",
"Base.pzkHMMV3",
"Base.pzkLimo",
"Base.pzkHearse",
"Base.pzkPickUpTruck93",
"Base.pzkPickUpTruckWoodboarded",
"Base.pzkVanBox",
"Base.pzkCarMuscle",
"Base.pzkFranklinPony",
"Base.pzkChevalierCeriseSedan",
"Base.pzkChevalierCeriseSedanTaxi",
"Base.pzkFranklinTriumphTaxi",
"Base.pzkDashHellion",
"Base.pzkDashHellionTaxi",
"Base.pzkDashMayor",
"Base.pzkDashMayorTaxi",
"Base.pzkDashRapier",
"Base.pzkFranklinTriumph",
"Base.pzkFranklinTriumphTWD",
"Base.pzkFranklinTriumphTWDTaxi",
"Base.pzkCeriseStationWagon",
"Base.pzkDashMayorStationWagon",
"Base.pzkRapierStationWagon",
"Base.pzkTriumphStationWagon",
"Base.pzkTriumphStationWagonTaxi",
"Base.pzkVanSeatsTaxi",
"Base.pzkDashRoyal",
"Base.pzkDashRoyalGrand",
"Base.pzkChevalierDownhill",
"Base.pzkDashTornado",
"Base.pzkMastersonLady",
"Base.pzkDashPhoenix",
"Base.pzkDashPhoenixBandit",
"Base.pzkChevalierE6",
"Base.pzkChevalierF6",
"Base.pzkChevalierRoadrunner",
"Base.pzkMastersonInitial",
"Base.pzkMastersonXSR",
"Base.pzkContinentalSpirit",
"Base.pzkDashNoble",
"Base.pzkFranklinStallion",
"Base.pzkFranklinStallion2",
"Base.pzkFranklinStallionSport",
"Base.pzkFranklinIslander",
"Base.pzkFranklinHomelander",
"Base.pzkMastersonCrown",
"Base.pzkFranklinTruckBed",
"Base.pzkFranklinTruckMcCoy",
"Base.pzkFranklinTruckShort",
"Base.pzkFranklinTruckCab",
"Base.pzkFranklinTruckBus",
"Base.pzkFranklinTruckBox",
"Base.pzkFranklinTruckBoxLectromax",
"Base.pzkFranklinTruckDump",
"Base.pzkFranklinTruckFlatbed",
"Base.pzkFranklinTruckGarbage",
"Base.pzkFranklinTruckPropane",
"Base.pzkFranklinTruckPropane2",
"Base.pzkFranklinTruckRV",
"Base.pzkFranklinTruckTankerFossoil",
"Base.pzkFranklinTruckTankerSeptic",
"Base.pzkFranklinTruckTankerWater",
"Base.pzkFranklinTruckTow",
"Base.pzkFranklinTruckUtility",
"Base.pzkMinivanC22",
"Base.pzkMinivanChev",
"Base.pzkMinivanConvoy",
"Base.pzkMinivanMPV",
"Base.pzkMinivanStellarisMail",
"Base.pzkMinivanStellarisTaxi",
"Base.pzkMinivanT3",
"Base.pzkMinivanT3C",
"Base.pzkMinivanTask",
"Base.pzkMinivan2",
"Base.pzkMinivanPrev",
"Base.pzkVanMultivan",
"Base.pzkMastersonScout4D",
"Base.pzkChevalierPickupCrewLong",
"Base.pzkChevalierPickupCrewMedium",
"Base.pzkChevalierProvince",
"Base.pzkChevalierProvinceLong",
"Base.pzkChevalierLaserModern",
"Base.pzkChevalierE6Van",
"Base.pzkChevalierF6Van",
"Base.pzkChevalierLaserOffroader",
"Base.pzkFtypeTowTruck",
"Base.pzkChevalierTowTruck",
"Base.pzkMerciaLangBerg",
"Base.pzkDashCheyene",
"Base.pzkContinentalCruiser",
"Base.pzkFranklin350FWagonLong",
"Base.pzkFranklin350FPickupCrewLong",
"Base.pzkFranklin250FPickupWagonLong",
"Base.pzkFranklin250FPickupCrewLong",
"Base.pzkFranklin250FWagonLong",
"Base.pzkFranklin150van",
"Base.pzkFranklin150FPickupReg",
"Base.pzkFranklin150FWagonMedium",
"Base.pzkFranklin150FPickupMedium",
"Base.pzkDashIntruder250WagonLong",
"Base.pzkDashIntruder250PickupLong",
"Base.pzkDashIntruder150short",
"Base.pzkDashIntruder150RegVan",
"Base.pzkMastersonLadyZ",
"Base.pzkDashGTA",
"Base.pzkFranklinStallionKing",
"Base.pzkFranklinStallionKing2",
"Base.pzkFranklinStallionKing3",
"Base.pzkFranklinStallionKing4",
"Base.pzkFranklinStallionKing5",
"Base.pzkDashRunner",
"Base.pzkDashPiranha",
"Base.pzkDashChampion",
"Base.pzkDashPhoenix80",
"Base.pzkChevalierMaroca80",
"Base.pzkContinentalBug",
"Base.pzkContinentalBayer330Sport",
"Base.pzkContinentalBayer330Cabrio",
"Base.pzkContinentalBayer3304D",
"Base.pzkContinentalBayer3302D",
"Base.pzkContinentalBayer534",
"Base.pzkContinentalBayer732",
"Base.pzkMerciaLang1240",
"Base.pzkMerciaLang12402D",
"Base.pzkFranklinTruckSemi",
"Base.pzkFreightlinerFlat2",
"Base.pzkFreightlinerFlat",
"Base.pzkPeterbuiltSleeper",
"Base.pzkPeterbuilt",
"Base.pzkMerciaLang4000Cabrio",
"Base.pzkContinentalBayer330Cabrio ",
"Base.pzkCarMuscleCabrio",
"Base.pzkDashRancherCabrio",
"Base.pzkChevalierCosetteCabrio",
"Base.pzkMastersonSunrise",
"Base.pzkMastersonSensation",
"Base.pzkMastersonExpander",
"Base.pzkStepVanHotDog",
"Base.pzkStepVanTacoVan",
"Base.pzkContinentalPfeiffer901",
"Base.pzkContinentalPfeiffer901c",
"Base.pzkContinentalPfeiffer930",
"Base.pzkContinentalPfeiffer930c",
"Base.pzkFranklinTriumphWagon",
"Base.pzkChevalierCerise93",
"Base.pzkChevalierCerise93Taxi",
"Base.pzkDashElite2D",
"Base.pzkMastersonHarmonyWagon",
"Base.pzkMastersonHarmony",
"Base.pzkContinentalNord",
"Base.pzkContinentalNordWagon",
"Base.pzkContinentalPyrenean310",
"Base.pzkTransitBus",
"Base.pzkChevalierCerise93Wagon",
"Base.pzkDashHEMTT6x6semi",
"Base.pzkChevalierCeriseLimo",
"Base.pzkChevalierVan70",
"Base.pzkDashHellionLimo",
"Base.pzkDashRancherMail",
"Base.pzkDashRapierLimo",
"Base.pzkDashVan70",
"Base.pzkFranklinHomelanderLimo",
"Base.pzkFranklinTriumphTWD91",
"Base.pzkFranklinVan70",
"Base.pzkStepVanFedLog",
"Base.pzkTriumphTWDStationWagon",
"Base.pzkTriumphTWDStationWagonTaxi",
"Base.pzkDashNavajoP",
"Base.pzkDashNavajoW",
"Base.pzkF350BoxUmoveit",
"Base.pzkMastersonRotaryC",
"Base.pzkVanGigamart",
"Base.pzkChevalierRookie",
"Base.pzkAutowagenBunny",
"Base.pzkMastersonRice",
"Base.pzkMastersonIberiaVan1",
"Base.pzkMastersonIberiaVan2",
"Base.pzkMastersonIberiaPickup",
"Base.pzkDash600",
"Base.pzkDashCirilla"
}

for _, entry in ipairs(newPzkVLCEntries) do
    table.insert(BWOVehicles.carChoices, entry)
end


for _, entry in ipairs(pzkPoliceEntries) do
    table.insert(BWOVehicles.policeCarChoices, entry)
end

for _, entry in ipairs(pzkSWATEntries) do
    table.insert(BWOVehicles.SWATCarChoices , entry)
end

for _, entry in ipairs(pzkMedicalEntries) do
    table.insert(BWOVehicles.medicalCarChoices, entry)
end

for _, entry in ipairs(pzkHazmatsEntries) do
    table.insert(BWOVehicles.hazmatsCarChoices, entry)
end

for _, entry in ipairs(pzkFiremanEntries) do
    table.insert(BWOVehicles.firemanCarChoices, entry)
end






--BWOVehicles.playerCarChoicesOccupation = {}
--BWOVehicles.playerCarChoicesOccupation["fireofficer"] = {"Base.PickUpTruckLightsFire"}
--BWOVehicles.playerCarChoicesOccupation["policeofficer"] = {"Base.PickUpVanLightsPolice"}
--BWOVehicles.playerCarChoicesOccupation["mechanics"] = {"Base.SportsCar"}
--BWOVehicles.playerCarChoicesDefault = {"Base.SmallCar"}

 --   ParkRanger
  --  ConstructionWorker
 --   MilitaryOfficer
 --   MilitarySoldier
 --   SecurityGuard
  --   Farmer
  
  
for _, entry in ipairs(pzkProffesionFireEntries) do
    table.insert(BWOVehicles.playerCarChoicesOccupation["fireofficer"], entry)
end

for _, entry in ipairs(pzkProffesionPoliceEntries) do
    table.insert(BWOVehicles.playerCarChoicesOccupation["policeofficer"], entry)
end

for _, entry in ipairs(pzkProffesionMechanicsEntries) do
    table.insert(BWOVehicles.playerCarChoicesOccupation["mechanics"], entry)
end

BWOVehicles.playerCarChoicesOccupation["parkranger"] = BWOVehicles.playerCarChoicesOccupation["parkranger"] or {}
BWOVehicles.playerCarChoicesOccupation["constructionworker"] = BWOVehicles.playerCarChoicesOccupation["constructionworker"] or {}
BWOVehicles.playerCarChoicesOccupation["militaryofficer"] = BWOVehicles.playerCarChoicesOccupation["militaryofficer"] or {}
BWOVehicles.playerCarChoicesOccupation["militarysoldier"] = BWOVehicles.playerCarChoicesOccupation["militarysoldier"] or {}
BWOVehicles.playerCarChoicesOccupation["securityguard"] = BWOVehicles.playerCarChoicesOccupation["securityguard"] or {}
BWOVehicles.playerCarChoicesOccupation["farmer"] = BWOVehicles.playerCarChoicesOccupation["farmer"] or {}


for _, entry in ipairs(pzkProffesionParkRangerEntries) do
    table.insert(BWOVehicles.playerCarChoicesOccupation["parkranger"], entry)
	
end

for _, entry in ipairs(pzkProffesionConstructionWorkerEntries) do
    table.insert(BWOVehicles.playerCarChoicesOccupation["constructionworker"], entry)
end

for _, entry in ipairs(pzkProffesionMilitaryOfficerEntries) do
    table.insert(BWOVehicles.playerCarChoicesOccupation["militaryofficer"], entry)
end

for _, entry in ipairs(pzkProffesionMilitarySoldierEntries) do
    table.insert(BWOVehicles.playerCarChoicesOccupation["militarysoldier"], entry)
end

for _, entry in ipairs(pzkProffesionSecurityGuardEntries) do
    table.insert(BWOVehicles.playerCarChoicesOccupation["securityguard"], entry)
end

for _, entry in ipairs(pzkProffesionFarmerEntries) do
    table.insert(BWOVehicles.playerCarChoicesOccupation["farmer"], entry)
end

end