if getActivatedMods():contains("TreadsFuelTypesFramework") then
	--- Fuels available in my mod: "Gasoline", "Diesel", "LPG"

	--- Tables Index:
	--- 1) Ignored vehicles (always Gasoline)
	--- 2) Forced fuel type (always one type)
	--- 3) Multiple available fuel types - roll based (each instance of a car can use different fuel type [from allowed])


	--- 1) Table of Vehicles ignored while Fuel Type assignment function runs (Gasoline stays no matter assignment type) ---
	RSFuelSwapIgnoreCars = RSFuelSwapIgnoreCars or {}	--- init the table (only once per file)
	RSFuelSwapIgnoreCars["Base.pzkFranklinTriumph"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkTriumphStationWagon"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkFranklinTriumphTaxi"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkTriumphStationWagonTaxi"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkFranklinTriumphPolice"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkFranklinTriumphTWD"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkFranklinTriumphTWDTaxi"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkFranklinTriumphTWDPolice"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkFranklinTriumphTWDFire"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.Vehicles_pzkDashRapier"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkRapierStationWagon"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkMinivanStellaris"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkMinivanC22"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkMinivanChev"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkMinivanConvoy"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkMinivanMPV"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkMinivanStellarisMail"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkMinivanStellarisTaxi"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkMinivanT3"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkMinivanT3C"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkMinivanTask"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkMinivan2"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkMinivanPrev"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkMastersonXSR"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkMastersonLady"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkMastersonInitial"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkMastersonCrown"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkLimo"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkHearse"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.Vehicles_pzkDashMayor"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkDashMayorStationWagon"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkDashMayorTaxi"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkDashMayorPolice"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkFranklinStallion"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkFranklinStallion2"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkFranklinStallionSport"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkFranklinStallionPolice"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkFranklinPony"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkFranklinIslander"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkFranklinHomelander"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkCarMuscle"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkDashRoyal"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkDashRoyalGrand"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkDashTornado"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkDashNoble"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkDashPhoenix"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkDashPhoenixBandit"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkChevalierCeriseSedan"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkCeriseStationWagon"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkChevalierCeriseSedanTaxi"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkChevalierCeriseSedanPolice"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkChevalierCeriseSedanFire"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkDashHellion"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkDashHellionTaxi"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkChevalierMaroca"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkChevalierRoadrunner"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkContinentalSpirit"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkDashDeluxo"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkChevalierDownhill"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkMastersonLadyZ"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkDashRunnerGeneral"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkDashGTA"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkFranklinStallionKing"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkDashRunner"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkDashPiranha"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkDashChampion"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkDashPhoenix80"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkChevalierMaroca80"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkContinentalBug"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkContinentalBugHerbie"] = true --- add FULL car type name to the table

	RSFuelSwapIgnoreCars["Base.pzkContinentalBayer330Sport"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkContinentalBayer3304D"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkContinentalBayer3302D"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkContinentalBayer534"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkContinentalBayer732"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkMerciaLang1240"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkMerciaLang12402D"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkMerciaLang4000Cabrio"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkContinentalBayer330Cabrio"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkCarMuscleCabrio"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkChevalierCosetteCabrio"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkMastersonSunrise"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkMastersonSensation"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkMastersonExpander"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkContinentalHammermanKnight"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkRegularCourtains"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkRegularFlatbed"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkRegularTanker"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkTrailerHorseBox"] = true --- add FULL car type name to the table

	RSFuelSwapIgnoreCars["Base.pzkContinentalPfeiffer901"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkContinentalPfeiffer901c"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkContinentalPfeiffer930"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkContinentalPfeiffer930c"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkTrailerCamping"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkTrailerRegularFTSemi"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkTrailerTankSmall"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkTrailerArmyCover"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkTrailerBoxDual"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkTrailerBoxPoliceDual"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkTrailerTankMedium"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkTrailerTankSprayer"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkDualTrailerCover"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkDualTrailer"] = true --- add FULL car type name to the table

	RSFuelSwapIgnoreCars["Base.pzkFranklinTriumphWagon"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkChevalierCerise93"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkChevalierCerise93Taxi"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkChevalierCerise93Police"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkChevalierCerise93Fire"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkChevalierCerise93WagonFire"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkDashElite2D"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkMastersonHarmonyWagon"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkMastersonHarmony"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkContinentalNord"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkContinentalNordWagon"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkContinentalPyrenean310"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkChevalierCerise93Wagon"] = true --- add FULL car type name to the table
	RSFuelSwapIgnoreCars["Base.pzkTriumphTWDStationWagonGriswold"] = true --- add FULL car type name to the table





	--- 2) Table of Vehicles with forced Fuel Type while assignment function runs (assign and force ONE fuel type) ---
	RSForceFuelSwapCars = RSForceFuelSwapCars or {} --- init the table (only once per file)
	RSForceFuelSwapCars["Base.pzkVanPolice"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkVanMcCoy"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkVanBrig"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkVanBox"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkVanBoxAmbulance"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkVanBoxFiretruck"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkStepVanPolice"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkStepVanPizza"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkStepVanIceCream"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkStepVanMilk"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkStepVanUPZ"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkHMMV"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkHMMV2"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkHMMV3"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkDashOhio"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type


	-- Here you go.

	RSForceFuelSwapCars["Base.pzkFranklinBankTruck"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkFranklinSwatTruck"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkFranklinTruckBed"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkFranklinTruckShort"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkFranklinTruckCab"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkFranklinTruckMil"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkFranklinTrucKBus"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkFranklinTrucKBusPrison"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkFranklinTrucKBusArmy"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkFranklinTruckBox"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkFranklinTruckDump"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkFranklinTruckFire"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkFranklinTruckFireTanker"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkFranklinTruckFlatbed"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkFranklinTruckGarbage"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkFranklinTruckPropane"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkFranklinTruckPropane2"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkFranklinTruckRV"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkFranklinTruckTankerFossoil"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkFranklinTruckTankerSeptic"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkFranklinTruckTankerWater"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkFranklinTruckTow"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkFranklinTruckUtility"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkFranklinTruckMilTankerWater"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkFranklinTruckSemi"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkContinentalTRK"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkFreightlinerFlat2"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkFreightlinerFlat"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkPeterbuiltSleeper"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkPeterbuiltSleeperBandit"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkPeterbuilt"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type

	RSForceFuelSwapCars["Base.pzkFireTruckFlatPumper"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkFireTruckFlatLadder"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkFireTruckFlatSemi"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkTractor"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkTrailerGeneratorDual"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkStepVanHotDog"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type
	RSForceFuelSwapCars["Base.pzkTransitBus"] = "Diesel" --- add FULL car type name to the table and assign existing fuel type

	--- 3) Table of Vehicles with multiple Fuel Types while assignment function runs ---
	---Each vehicle added should keep format as in examples below.
	---Sum of values per FuelType (of each vehicle) should be 100.
	---Gasoline 60, Diesel 50, LPG 30 - Give 60% for Gasoline, 40% for Diesel. Each "chance" above 100 is ignored.
	---Gasoline 20, Diesel 30 - Give 70% for Gasoline (requested 20 + unassigned 50) and 30% for Diesel.

	RSMultiFuelSwapCars = RSMultiFuelSwapCars or {} --- init the table (only once per file)


	RSMultiFuelSwapCars["Base.pzkDashRancherRanger"] = {} 
	RSMultiFuelSwapCars["Base.pzkDashRancherRanger"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkDashRancherRanger"]["Diesel"]  = 40		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkDashRancherCustom"] = {} 
	RSMultiFuelSwapCars["Base.pzkDashRancherCustom"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkDashRancherCustom"]["Diesel"]  = 40		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkDashRancherCabrio"] = {} 
	RSMultiFuelSwapCars["Base.pzkDashRancherCabrio"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkDashRancherCabrio"]["Diesel"]  = 40		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkSuvCustom"] = {} 
	RSMultiFuelSwapCars["Base.pzkSuvCustom"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkSuvCustom"]["Diesel"]  = 40		--- assign the car entry a fuel type and its chance

	RSMultiFuelSwapCars["Base.pzkVanMultivan"] = {} 
	RSMultiFuelSwapCars["Base.pzkVanMultivan"]["Gasoline"]  = 50		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkVanMultivan"]["Diesel"]  = 50		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkVanCamper"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkVanCamper"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkVanCamper"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.Vehicles_VanSeatsTaxi"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.Vehicles_VanSeatsTaxi"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.Vehicles_VanSeatsTaxi"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkPickupFranklin"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkPickupFranklin"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkPickupFranklin"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkPickUpTruckWoodboarded"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkPickUpTruckWoodboarded"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkPickUpTruckWoodboarded"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkPickUpTruck93"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkPickUpTruck93"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkPickUpTruck93"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkFranklinGalloper"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkFranklinGalloper"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkFranklinGalloper"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkFranklinGalloperPolice"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkFranklinGalloperPolice"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkFranklinGalloperPolice"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkFranklinGalloperFire"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkFranklinGalloperFire"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkFranklinGalloperFire"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkFranklinGalloperRanger"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkFranklinGalloperRanger"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkFranklinGalloperRanger"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkChevalierE6"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkChevalierE6"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkChevalierE6"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkChevalierF6"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkChevalierF6"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkChevalierF6"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkChevalierE6Van"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkChevalierE6Van"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkChevalierE6Van"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkChevalierF6Van"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkChevalierF6Van"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkChevalierF6Van"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkMastersonScout4D"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkMastersonScout4D"] ["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkMastersonScout4D"] ["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkChevalierPickupCrewLong"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkChevalierPickupCrewLong"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkChevalierPickupCrewLong"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkChevalierPickupCrewMedium"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkChevalierPickupCrewMedium"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkChevalierPickupCrewMedium"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkChevalierProvince"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkChevalierProvince"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkChevalierProvince"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkChevalierProvinceLong"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkChevalierProvinceLong"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkChevalierProvinceLong"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkChevalierLaserModern"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkChevalierLaserModern"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkChevalierLaserModern"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkChevalierLaserCUCV"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkChevalierLaserCUCV"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkChevalierLaserCUCV"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkChevalierProvinceLongCUCV"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkChevalierProvinceLongCUCV"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkChevalierProvinceLongCUCV"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkChevalierLaserOffroader"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkChevalierLaserOffroader"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkChevalierLaserOffroader"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkChevalierLaserFire"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkChevalierLaserFire"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkChevalierLaserFire"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkChevalierLaserPolice"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkChevalierLaserPolice"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkChevalierLaserPolice"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkChevalierLaserRanger"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkChevalierLaserRanger"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkChevalierLaserRanger"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkFtypeTowTruck"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkFtypeTowTruck"] ["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkFtypeTowTruck"] ["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkChevalierTowTruck"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkChevalierTowTruck"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkChevalierTowTruck"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkChevalierTowTruckPolice"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkChevalierTowTruckPolice"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkChevalierTowTruckPolice"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkChevalierTowTruckFire"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkChevalierTowTruckFire"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkChevalierTowTruckFire"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkMerciaLangBerg"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkMerciaLangBerg"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkMerciaLangBerg"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkDashCheyene"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkDashCheyene"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkDashCheyene"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkContinentalCruiser"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkContinentalCruiser"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkContinentalCruiser"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkFranklin350FWagonLong"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkFranklin350FWagonLong"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkFranklin350FWagonLong"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkFranklin350FPickupCrewLong"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkFranklin350FPickupCrewLong"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkFranklin350FPickupCrewLong"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkFranklin250FPickupWagonLong"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkFranklin250FPickupWagonLong"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkFranklin250FPickupWagonLong"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkFranklin250FPickupCrewLong"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkFranklin250FPickupCrewLong"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkFranklin250FPickupCrewLong"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkFranklin250FWagonLong"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkFranklin250FWagonLong"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkFranklin250FWagonLong"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkFranklin150van"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkFranklin150van"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkFranklin150van"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkFranklin150FPickupReg"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkFranklin150FPickupReg"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkFranklin150FPickupReg"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkFranklin150FWagonMedium"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkFranklin150FWagonMedium"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkFranklin150FWagonMedium"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkFranklin150FPickupMedium"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkFranklin150FPickupMedium"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkFranklin150FPickupMedium"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkDashIntruder250WagonLong"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkDashIntruder250WagonLong"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkDashIntruder250WagonLong"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkDashIntruder250PickupLong"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkDashIntruder250PickupLong"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkDashIntruder250PickupLong"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkDashIntruder150short"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkDashIntruder150short"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkDashIntruder150short"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 
	RSMultiFuelSwapCars["Base.pzkDashIntruder150RegVan"] = {}		--- add FULL car type name to the table
	RSMultiFuelSwapCars["Base.pzkDashIntruder150RegVan"]["Gasoline"]  = 60		--- assign the car entry a fuel type and its chance
	RSMultiFuelSwapCars["Base.pzkDashIntruder150RegVan"]["Diesel"]  = 40		--- assign the car entry a fuel type and its 

end