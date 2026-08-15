---Like DoParam but for vehicles
---@param vehicle string Name of the vehicle script
---@param param string The parameter(s) to apply to this script
---@param module string Optional: the module of the vehicle
---@see Item#DoParam
---@see VehicleScript#Load
local DoVehicleParam = function(vehicle, param, module)
	module = module or "Base"
	local vehicleScript = ScriptManager.instance:getVehicle(module .. "." .. vehicle)
	if not vehicleScript then return end
	vehicleScript:Load(vehicle, "{" .. param .. "}")
end

---Utility to change the armor of a vehicle
---@param vehicle string Name of the vehicle script
---@param armor string Name of a armor template
---@see DoVehicleParam
local SetArmor = function(vehicle, armor, module)
	module = module or "Base"
	DoVehicleParam(vehicle, "template! = " .. armor .. ",")
end

---Utility to change the horn sound of a vehicle
---@param vehicle string Name of the vehicle script
---@param sound string Name of a sound
---@see DoVehicleParam
local SetHornSound = function(vehicle, sound)
	DoVehicleParam(vehicle, "sound { horn = " .. sound .. ",}")
end


-- ONLY EDIT BELOW THIS LINE!!! --

-- It's pretty simple.
-- Specify the vehicle's script name, and the armor's script name for said vehicle.

-- Skins
--    SetArmor("PickUpVanLightsFire","pzkChevalierF6FireSkin");
--    SetArmor("PickUpTruckLightsFire","pzkChevalierF6FireSkin");
 --   SetArmor("PickUpVanLightsPolice","pzkChevalierF6PoliceSkin");
--    SetArmor("PickUpVanLights","pzkChevalierF6RangerSkin");
 --   SetArmor("PickUpTruckLights","pzkChevalierF6RangerSkin");
 --   SetArmor("PickUpVanLights","pzkChevalierF6FossoilSkin");
 --   SetArmor("PickUpTruckLights","pzkChevalierF6FossoilSkin");


--PickUpTruckMccoy
--PickUpVanMccoy

--Additional parts for vanilla
 SetArmor("OffRoad","pzkSpareWheelDashRancher");




if getActivatedMods():contains("HTowTruckFix") then
    SetArmor("Chevalier_Rhino_TowTruck","PU_Hook_TowTruck");
end

if getActivatedMods():contains("StandardizedVehicleUpgradesV") then
  SetArmor("pzkMinivanStellaris","PU_Armor_Car_pzkMinivanStellaris");
  SetArmor("pzkVanBrig","PU_Armor_Car_pzkVanBrig");
  SetArmor("pzkVanCamper","PU_Armor_Car_pzkVanCamper");
  SetArmor("pzkStepVanMilk","PU_Armor_StepVan");
  SetArmor("pzkStepVanUPZ","PU_Armor_StepVan");
  SetArmor("pzkStepVanSwat","PU_Armor_Car_pzkVanSwat");
  SetArmor("pzkVanPolice","PU_Armor_VanSeats");
  SetArmor("pzkVanMcCoy","PU_Armor_Van");
  SetArmor("pzkChevalierMaroca","PU_Armor_SportsCar");
  SetArmor("pzkPickUpTruck93","PU_Armor_PickUpTruck");

  SetArmor("pzkDashDeluxo","PU_Armor_Car_pzkDashDeluxo");
  SetArmor("pzkCarMuscle","PU_Armor_Car_pzkCarMuscle");
  SetArmor("pzkDashOhio","PU_Armor_Car_pzkDashOhio");
  SetArmor("pzkFranklinGalloper","PU_Armor_Car_pzkFranklinGalloper");
  SetArmor("pzkFranklinGalloperPolice","PU_Armor_Car_pzkFranklinGalloper");
  SetArmor("pzkHearse","PU_Armor_Car_pzkHearse");
  SetArmor("pzkHMMV","PU_Armor_Car_pzkHMMV");
  SetArmor("pzkHMMV2","PU_Armor_Car_pzkHMMV2");
  SetArmor("pzkHMMV3","PU_Armor_Car_pzkHMMV3");
  SetArmor("pzkLimo","PU_Armor_Car_pzkLimo");
  SetArmor("pzkPickupFranklin","PU_Armor_Car_pzkPickupFranklin");
  SetArmor("pzkVanBox","PU_Armor_Car_pzkVanBox2");
  SetArmor("pzkVanBoxAmbulance","PU_Armor_Car_pzkVanBox");
  SetArmor("pzkVanBoxFiretruck","PU_Armor_Car_pzkVanBox");
  SetArmor("pzkPickUpTruckWoodboarded","PU_Armor_PickUpTruck");
  SetArmor("pzkStepVanIceCream","PU_Armor_StepVan");
  SetArmor("pzkStepVanPizza","PU_Armor_StepVan");

  SetArmor("pzkFranklinTriumphPolice","PU_Armor_Car_pzkFranklinTriumph");
  SetArmor("pzkFranklinTriumph","PU_Armor_Car_pzkFranklinTriumph");
  SetArmor("pzkFranklinTriumphTaxi","PU_Armor_Car_pzkFranklinTriumph");
  SetArmor("pzkFranklinPony","PU_Armor_Car_pzkFranklinPony");

  SetArmor("pzkChevalierCeriseSedan","PU_Armor_Car");
  SetArmor("pzkChevalierCeriseSedanPolice","PU_Armor_CarLights");
  SetArmor("pzkChevalierCeriseSedanTaxi","PU_Armor_CarLights");
  SetArmor("pzkDashHellion","PU_Armor_Car");
  SetArmor("pzkDashHellionTaxi","PU_Armor_Car");
  SetArmor("Vehicles_pzkDashMayor","PU_Armor_Car");
  SetArmor("pzkDashMayorPolice","PU_Armor_CarLights");
  SetArmor("pzkDashMayorTaxi","PU_Armor_Car");
  SetArmor("Vehicles_pzkDashRapier","PU_Armor_Car");
  SetArmor("pzkFranklinTriumphTWD","PU_Armor_Car");
  SetArmor("pzkFranklinTriumphTWDPolice","PU_Armor_CarLights");
  SetArmor("pzkFranklinTriumphTWDTaxi","PU_Armor_Car");
  SetArmor("pzkCeriseStationWagon","PU_Armor_CarWagon");
  SetArmor("pzkDashMayorStationWagon","PU_Armor_CarWagon");
  SetArmor("pzkRapierStationWagon","PU_Armor_CarWagon");
  SetArmor("pzkTriumphStationWagon","PU_Armor_CarWagon");
  SetArmor("pzkTriumphStationWagonTaxi","PU_Armor_CarWagon");
  SetArmor("Vehicles_VanSeatsTaxi","PU_Armor_VanSeats");
  SetArmor("pzkFranklinGalloperRanger","PU_Armor_Car_pzkFranklinGalloper");

  SetArmor("pzkDashRoyal","PU_Armor_Car_pzkDashRegal");
  SetArmor("pzkDashRoyalGrand","PU_Armor_Car_pzkDashRegal");
  SetArmor("pzkChevalierDownhill","PU_Armor_Car_pzkDashRegal");
  SetArmor("pzkDashTornado","PU_Armor_Car_pzkDashRegal");
  SetArmor("pzkMastersonLady","PU_Armor_SportsCar");
  SetArmor("pzkDashPhoenix","PU_Armor_Car_pzkDashPhoenix");
  SetArmor("pzkDashPhoenixBandit","PU_Armor_Car_pzkDashPhoenix");
  SetArmor("pzkChevalierE6","PU_Armor_Car_pzk150PReg");
  SetArmor("pzkChevalierF6","PU_Armor_Car_pzk150PReg");
  SetArmor("pzkFranklinTriumphTWDFire","PU_Armor_Car_pzkFranklinTriumph");
  SetArmor("pzkChevalierCeriseSedanFire","PU_Armor_Car_pzkFranklinTriumph");
  SetArmor("pzkFranklinGalloperFire","PU_Armor_Car_pzkFranklinGalloper");
  SetArmor("pzkChevalierRoadrunner","PU_Armor_Car_pzkChevalierRoadrunner");
  SetArmor("pzkMastersonInitial","PU_Armor_Car_pzkMastersonInitial");
  SetArmor("pzkMastersonXSR","PU_Armor_Car_pzkMastersonXSR");
  SetArmor("pzkContinentalSpirit","PU_Armor_Car_pzkContinentalSpirit");
  SetArmor("pzkDashNoble","PU_Armor_Car_pzkDashNoble");
  SetArmor("pzkFranklinStallion","PU_Armor_Car_pzkFranklinStallion");
  SetArmor("pzkFranklinStallion2","PU_Armor_Car_pzkFranklinStallion");
  SetArmor("pzkFranklinStallionSport","PU_Armor_Car_pzkFranklinStallion");
  SetArmor("pzkFranklinStallionPolice","PU_Armor_Car_pzkFranklinStallion");
  SetArmor("pzkFranklinIslander","PU_Armor_Car_pzkFranklinStallion");
  SetArmor("pzkFranklinHomelander","PU_Armor_Car_pzkFranklinTriumph");
  SetArmor("pzkMastersonCrown","PU_Armor_Car_pzkMastersonCrown");

  SetArmor("pzkFranklinBankTruck","PU_Armor_Car_pzkFranklinTruckBank");
  SetArmor("pzkFranklinSwatTruck","PU_Armor_Car_pzkFranklinTruckBank");
  SetArmor("pzkFranklinTruckBed","PU_Armor_Car_pzkFranklinTruckBed");

  SetArmor("pzkFranklinTruckShort","PU_Armor_Car_pzkFranklinTruckShort");
  SetArmor("pzkFranklinTruckCab","PU_Armor_Car_pzkFranklinTruckCab");
			
  SetArmor("pzkFranklinTruckBus","PU_Armor_Car_pzkFranklinTruckBus");
  SetArmor("pzkFranklinTruckBusPrison","PU_Armor_Car_pzkFranklinTruckBus");
  SetArmor("pzkFranklinTruckBusArmy","PU_Armor_Car_pzkFranklinTruckBus");
  SetArmor("pzkFranklinTruckBox","PU_Armor_Car_pzkFranklinTruckBox");
  SetArmor("pzkFranklinTruckDump","PU_Armor_Car_pzkFranklinTruckShort");
  SetArmor("pzkFranklinTruckFire","PU_Armor_Car_pzkFranklinTruckFire");
  SetArmor("pzkFranklinTruckFireTanker","PU_Armor_Car_pzkFranklinTruckTanker1");
  SetArmor("pzkFranklinTruckFlatbed","PU_Armor_Car_pzkFranklinTruckBed");
  SetArmor("pzkFranklinTruckGarbage","PU_Armor_Car_pzkFranklinTruckBed");
  SetArmor("pzkFranklinTruckPropane","PU_Armor_Car_pzkFranklinTruckTanker2");
  SetArmor("pzkFranklinTruckPropane2","PU_Armor_Car_pzkFranklinTruckTanker2");
  SetArmor("pzkFranklinTruckRV","PU_Armor_Car_pzkFranklinTruckRV");		
  SetArmor("pzkFranklinTruckTankerFossoil","PU_Armor_Car_pzkFranklinTruckTanker1");		
  SetArmor("pzkFranklinTruckTankerSeptic","PU_Armor_Car_pzkFranklinTruckTanker2");
  SetArmor("pzkFranklinTruckTankerWater","PU_Armor_Car_pzkFranklinTruckTanker2");
  SetArmor("pzkFranklinTruckTow","PU_Armor_Car_pzkFranklinTruckTow");
  SetArmor("pzkFranklinTruckUtility","PU_Armor_Car_pzkFranklinTruckShort");


  SetArmor("pzkFranklinTruckMilTankerWater","PU_Armor_Car_pzkFranklinTruckTanker2");
  SetArmor("pzkFranklinTruckMil","PU_Armor_Car_pzkFranklinTruckBed");

  SetArmor("pzkMinivanC22","PU_Armor_Car_pzkMinivanC22");
  SetArmor("pzkMinivanChev","PU_Armor_Car_pzkMinivanStellaris");
  SetArmor("pzkMinivanConvoy","PU_Armor_Car_pzkMinivanStellaris");
  SetArmor("pzkMinivanMPV","PU_Armor_Car_pzkMinivanMPV");
  SetArmor("pzkMinivanStellarisMail","PU_Armor_Car_pzkMinivanStellaris");
  SetArmor("pzkMinivanStellarisTaxi","PU_Armor_Car_pzkMinivanStellaris");
  SetArmor("pzkMinivanT3","PU_Armor_Car_pzkMinivanT3");
  SetArmor("pzkMinivanT3C","PU_Armor_Car_pzkMinivanT3C");
  SetArmor("pzkMinivanTask","PU_Armor_Car_pzkMinivanStellaris");
  SetArmor("pzkMinivan2","PU_Armor_Car_pzkMinivanAPV");
  SetArmor("pzkMinivanPrev","PU_Armor_Car_pzkMinivanPrev");
  SetArmor("pzkVanMultivan","PU_Armor_Car_pzkVanMultivan");


  SetArmor("pzkMastersonScout4D","PU_Armor_Car_pzkMastersonScout4D");
  SetArmor("pzkChevalierPickupCrewLong","PU_Armor_Car_pzk250PLong");
  SetArmor("pzkChevalierPickupCrewMedium","PU_Armor_Car_pzk250P");
  SetArmor("pzkChevalierProvince","PU_Armor_Car_pzk250P");
  SetArmor("pzkChevalierProvinceLong","PU_Armor_Car_pzk2502DLong");
  SetArmor("pzkChevalierLaserModern","PU_Armor_Car_pzkFranklinGalloper");
  SetArmor("pzkChevalierE6Van","PU_Armor_Car_pzk150Reg");
  SetArmor("pzkChevalierF6Van","PU_Armor_Car_pzk150Reg");
  SetArmor("pzkChevalierLaserCUCV","PU_Armor_Car_pzkFranklinGalloper");
  SetArmor("pzkChevalierProvinceLongCUCV","PU_Armor_Car_pzk2502DLong");
  SetArmor("pzkChevalierLaserOffroader","PU_Armor_Car_pzkFranklinGalloper5D");
  SetArmor("pzkChevalierLaserFire","PU_Armor_Car_pzkFranklinGalloper");
  SetArmor("pzkChevalierLaserPolice","PU_Armor_Car_pzkFranklinGalloper");
  SetArmor("pzkChevalierLaserRanger","PU_Armor_Car_pzkFranklinGalloper");
  SetArmor("pzkFtypeTowTruck","PU_Armor_Car_pzkFTowTruck");
  SetArmor("pzkChevalierTowTruck","PU_Armor_Car_pzkTowTruck");
  SetArmor("pzkChevalierTowTruckPolice","PU_Armor_Car_pzkTowTruck");
  SetArmor("pzkChevalierTowTruckFire","PU_Armor_Car_pzkTowTruck");
  SetArmor("pzkMerciaLangBerg","PU_Armor_Car_pzkMerciaLangBerg");
  SetArmor("pzkDashCheyene","PU_Armor_Car_pzkDashCheyene");
  SetArmor("pzkContinentalCruiser","PU_Armor_Car_pzkContinentalCruiser");
  SetArmor("pzkFranklin350FWagonLong","PU_Armor_Car_pzk2502DLong");
  SetArmor("pzkFranklin350FPickupCrewLong","PU_Armor_Car_pzk350PLong");
  SetArmor("pzkFranklin250FPickupWagonLong","PU_Armor_Car_pzk2502DLong");
  SetArmor("pzkFranklin250FPickupCrewLong","PU_Armor_Car_pzk250PLong");
  SetArmor("pzkFranklin250FWagonLong","PU_Armor_Car_pzk250PLong");
  SetArmor("pzkFranklin150van","PU_Armor_Car_pzk150Reg");
  SetArmor("pzkFranklin150FPickupReg","PU_Armor_Car_pzk150PReg");
  SetArmor("pzkFranklin150FWagonMedium","PU_Armor_Car_pzk2502D");
  SetArmor("pzkFranklin150FPickupMedium","PU_Armor_Car_pzk250P");
  SetArmor("pzkDashIntruder250WagonLong","PU_Armor_Car_pzk2502DLong");
  SetArmor("pzkDashIntruder250PickupLong","PU_Armor_Car_pzk250PLong");
  SetArmor("pzkDashIntruder150short","PU_Armor_Car_pzkFranklinGalloper");
  SetArmor("pzkDashIntruder150RegVan","PU_Armor_Car_pzk150Reg");

  SetArmor("pzkMastersonLadyZ","PU_Armor_Car_pzkMastersonLadyZ");
  SetArmor("pzkDashRunnerGeneral","PU_Armor_Car_pzkDashRunner");
  SetArmor("pzkDashGTA","PU_Armor_Car_pzkDashGTA");
  SetArmor("pzkFranklinStallionKing","PU_Armor_Car_pzkFranklinStallionKing");
  SetArmor("pzkDashRunner","PU_Armor_Car_pzkDashRunner");
  SetArmor("pzkDashPiranha","PU_Armor_Car_pzkDashPiranha");
  SetArmor("pzkDashChampion","PU_Armor_Car_pzkDashChampion");
  SetArmor("pzkDashPhoenix80","PU_Armor_Car_pzkChevalierMaroca80");
  SetArmor("pzkChevalierMaroca80","PU_Armor_Car_pzkChevalierMaroca80");
  SetArmor("pzkDashPhoenix80SmashedFront","PU_Armor_Car_pzkChevalierMaroca80Crashed");
  SetArmor("pzkContinentalBug","PU_Armor_Car_pzkContinentalBug");
  SetArmor("pzkContinentalBugHerbie","PU_Armor_Car_pzkContinentalBug");

  SetArmor("pzkContinentalBayer330Sport","PU_Armor_Car_pzkContinentalBayer330Sport");
  SetArmor("pzkContinentalBayer3304D","PU_Armor_Car_pzkContinentalBayer3304D");
  SetArmor("pzkContinentalBayer3302D","PU_Armor_Car_pzkContinentalBayer330Sport");
  SetArmor("pzkContinentalBayer534","PU_Armor_Car_pzkContinentalBayer534");
  SetArmor("pzkContinentalBayer732","PU_Armor_Car_pzkContinentalBayer732");
  SetArmor("pzkMerciaLang1240","PU_Armor_Car_pzkContinentalBayer534");
  SetArmor("pzkMerciaLang12402D","PU_Armor_Car_pzkMerciaLang12402D");
  SetArmor("pzkFranklinTruckSemi","PU_Armor_Car_pzkFranklinTruckSemi");
  SetArmor("pzkContinentalTRK","PU_Armor_Car_pzkContinentalTRK");
  SetArmor("pzkFreightlinerFlat2","PU_Armor_Car_pzkFreightlinerFlat2");
  SetArmor("pzkFreightlinerFlat","PU_Armor_Car_pzkFreightlinerFlat");
  SetArmor("pzkPeterbuiltSleeper","PU_Armor_Car_pzkPeterbuiltSleeper");
  SetArmor("pzkPeterbuiltSleeperBandit","PU_Armor_Car_pzkPeterbuiltSleeper");
  SetArmor("pzkPeterbuilt","PU_Armor_Car_pzkPeterbuilt");
  SetArmor("pzkMerciaLang4000Cabrio","PU_Armor_LuxuryCar");
  SetArmor("pzkContinentalBayer330Cabrio","PU_Armor_Car_pzkContinentalBayer330Sport");
  SetArmor("pzkCarMuscleCabrio","PU_Armor_Car_pzkCarMuscle");
  SetArmor("pzkDashRancherRanger","PU_Armor_OffRoad");
  SetArmor("pzkDashRancherCustom","PU_Armor_OffRoad");
  SetArmor("pzkDashRancherCabrio","PU_Armor_OffRoad");
  SetArmor("pzkChevalierCosetteCabrio","PU_Armor_SportsCar");
  SetArmor("pzkMastersonSunrise","PU_Armor_Car_pzkMastersonSunrise");
  SetArmor("pzkMastersonSensation","PU_Armor_Car_pzkMastersonExpander");
  SetArmor("pzkMastersonExpander","PU_Armor_Car_pzkMastersonExpander");
  SetArmor("pzkSuvCustom","PU_Armor_SUV");
  SetArmor("pzkContinentalHammermanKnight","PU_Armor_Car_pzkContinentalHammermanKnight");
  SetArmor("pzkTrailerRegularCourtains","PU_Armor_Car_pzkTrailerRegularCourtains");
  SetArmor("pzkTrailerRegularCourtainsBandit","PU_Armor_Car_pzkTrailerRegularCourtains");
  SetArmor("pzkTrailerRegularFuelTanker","PU_Armor_Car_pzkTrailerRegularFuelTanker");
  SetArmor("pzkTrailerRegularFlatbed","PU_Armor_Car_pzkTrailerRegularFlatbed");
  SetArmor("pzkTrailerRegularFTSemi","PU_Armor_Car_pzkTrailerRegularFlatbed");

  SetArmor("pzkStepVanHotDog","PU_Armor_StepVan");

  SetArmor("pzkFireTruckFlatPumper","PU_Armor_Car_pzkFireTruckFlat");
  SetArmor("pzkFireTruckFlatLadder","PU_Armor_Car_pzkFireTruckFlat");
  SetArmor("pzkFireTruckFlatSemi","PU_Armor_Car_pzkFireTruckFlatSemi");
  SetArmor("pzkTractor","PU_Armor_Car_pzkTractor");
  SetArmor("pzkContinentalPfeiffer901","PU_Armor_Car_pzkContinentalPfeiffer901");
  SetArmor("pzkContinentalPfeiffer901c","PU_Armor_Car_pzkContinentalPfeiffer901");
  SetArmor("pzkContinentalPfeiffer930","PU_Armor_Car_pzkContinentalPfeiffer901");
  SetArmor("pzkContinentalPfeiffer930c","PU_Armor_Car_pzkContinentalPfeiffer901");

  SetArmor("pzkFranklinTriumphWagon","PU_Armor_Car_pzkFranklinTriumphWagon");
  SetArmor("pzkChevalierCerise93","PU_Armor_Car_pzkFranklinTriumph");
  SetArmor("pzkChevalierCerise93Taxi","PU_Armor_Car_pzkFranklinTriumph");
  SetArmor("pzkChevalierCerise93Police","PU_Armor_Car_pzkFranklinTriumph");
  SetArmor("pzkChevalierCerise93Fire","PU_Armor_Car_pzkFranklinTriumph");
  SetArmor("pzkChevalierCerise93WagonFire","PU_Armor_Car_pzkChevalierCerise93Wagon");
  SetArmor("pzkChevalierCerise93Wagon","PU_Armor_Car_pzkChevalierCerise93Wagon");
  SetArmor("pzkDashElite2D","PU_Armor_Car_pzkDashElite2D");
  SetArmor("pzkMastersonHarmonyWagon","PU_Armor_Car_pzkMastersonHarmonyWagon");
  SetArmor("pzkMastersonHarmony","PU_Armor_Car_pzkMastersonHarmony");
  SetArmor("pzkContinentalNord","PU_Armor_Car_pzkContinentalNord");
  SetArmor("pzkContinentalNordWagon","PU_Armor_Car_pzkContinentalNordWagon");
  SetArmor("pzkContinentalPyrenean310","PU_Armor_Car_pzkContinentalPyrenean310");
  SetArmor("pzkTransitBus","PU_Armor_Car_pzkTransitBus");
  SetArmor("pzkDashHEMTT6x6semi","PU_Armor_Car_pzkDashHEMTT6x6semi");
  SetArmor("pzkTriumphTWDStationWagonGriswold","PU_Armor_CarWagon");

--  SetArmor("pzkTrailerArmyCover","PU_Armor_Car_pzkContinentalPfeiffer901");
 -- SetArmor("pzkTrailerBoxPoliceDual","PU_Armor_Car_pzkContinentalPfeiffer901");
 -- SetArmor("pzkTrailerTankSprayer","PU_Armor_Car_pzkContinentalPfeiffer901");
--  SetArmor("pzkTrailerCamping","PU_Armor_Car_pzkContinentalPfeiffer901");
 -- SetArmor("pzkTrailerTankSmall","PU_Armor_Car_pzkContinentalPfeiffer901");
--  SetArmor("pzkTrailerBoxDual","PU_Armor_Car_pzkContinentalPfeiffer901");
 -- SetArmor("pzkTrailerTankMedium","PU_Armor_Car_pzkContinentalPfeiffer901");
--  SetArmor("pzkDualTrailerCover","PU_Armor_Car_pzkContinentalPfeiffer901");
 -- SetArmor("pzkDualTrailer","PU_Armor_Car_pzkContinentalPfeiffer901");

end
