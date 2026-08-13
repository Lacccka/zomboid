if VehicleZoneDistribution then



------------------------------------------- PZK CUSTOM ZONES -------------------------------
-- army contractor (Hazmat)
		VehicleZoneDistribution.hazmat = VehicleZoneDistribution.hazmat or {};
		VehicleZoneDistribution.hazmat.vehicles = VehicleZoneDistribution.hazmat.vehicles or {};
		VehicleZoneDistribution.hazmat.vehicles["Base.pzkTruckD70BFRFHazmat"] = {index = -1, spawnChance = 100};
-- BFRF
		VehicleZoneDistribution.bfrf = VehicleZoneDistribution.bfrf or {};
		VehicleZoneDistribution.bfrf.vehicles = VehicleZoneDistribution.bfrf.vehicles or {};
		VehicleZoneDistribution.bfrf.vehicles["Base.pzkTruckD70BFRFHazmat"] = {index = -1, spawnChance = 3};
		VehicleZoneDistribution.bfrf.vehicles["Base.pzkDashCheyeneBFRF"] = {index = -1, spawnChance = 40};
		VehicleZoneDistribution.bfrf.vehicles["Base.pzkDashCheyeneBFRFSec"] = {index = -1, spawnChance = 20};
		VehicleZoneDistribution.bfrf.vehicles["Base.pzkChevalier150AnimalBFRF"] = {index = -1, spawnChance = 40};
		VehicleZoneDistribution.bfrf.vehicles["Base.pzkHMMV2Mil"] = {index = -1, spawnChance = 30};
		VehicleZoneDistribution.bfrf.vehicles["Base.pzkHMMV3Mil"] = {index = -1, spawnChance = 30};
		VehicleZoneDistribution.bfrf.vehicles["Base.pzkHMMV5Mil"] = {index = -1, spawnChance = 30};
		VehicleZoneDistribution.bfrf.vehicles["Base.pzkHMMV6Mil"] = {index = -1, spawnChance = 30};
		VehicleZoneDistribution.bfrf.vehicles["Base.pzkFranklinTruckMil"] = {index = -1, spawnChance = 25};
		VehicleZoneDistribution.bfrf.vehicles["Base.pzkFranklinTruckMilTankerWater"] = {index = -1, spawnChance = 25};
		VehicleZoneDistribution.bfrf.vehicles["Base.pzkContinentalTRK"] = {index = 1, spawnChance = 3};
		VehicleZoneDistribution.bfrf.vehicles["Base.pzkDashHEMTT6x6semi"] = {index = -1, spawnChance = 3};
		VehicleZoneDistribution.bfrf.vehicles["Base.pzkTruckDashW35BedMil"] = {index = -1, spawnChance = 30};
		VehicleZoneDistribution.bfrf.vehicles["Base.pzkTruckDashW35CabrioMil"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.bfrf.vehicles["Base.pzkTruckDashW35WaterMil"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.bfrf.vehicles["Base.pzkTruckDashW35FuelMil"] = {index = -1, spawnChance = 10};
		

-- Public Works -- Road Crew Vehicles (PZK)
        VehicleZoneDistribution.public_works = VehicleZoneDistribution.public_works or {};
        VehicleZoneDistribution.public_works.vehicles = VehicleZoneDistribution.public_works.vehicles or {};
        VehicleZoneDistribution.public_works.vehicles["Base.pzkF150UtilityPublicWorks"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.public_works.vehicles["Base.pzkDash150UtilityPublicWorks"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.public_works.vehicles["Base.pzkChevalier150UtilityPublicWorks"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.public_works.vehicles["Base.pzkFranklinTruckFlatbedPublicWorks"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.public_works.vehicles["Base.pzkPickUpTruckPublicWorks"] = {index = -1, spawnChance = 15};
		VehicleZoneDistribution.public_works.vehicles["Base.pzkVanPublicWorks"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.public_works.vehicles["Base.pzkFranklinTruckDumpPublicWorks"] = {index = -1, spawnChance = 5};
		VehicleZoneDistribution.public_works.vehicles["Base.pzkF150BoxFlatbedPublicWorks"] = {index = -1, spawnChance = 3};
		VehicleZoneDistribution.public_works.vehicles["Base.pzkDash150BoxFlatbedPublicWorks"] = {index = -1, spawnChance = 3};
		VehicleZoneDistribution.public_works.vehicles["Base.pzkChevalier150BoxFlatbedPublicWorks"] = {index = -1, spawnChance = 3};

-- Cementery -- Hearses near cementeries (PZK)
		VehicleZoneDistribution.cementery = VehicleZoneDistribution.cementery or {};
		VehicleZoneDistribution.cementery.vehicles = VehicleZoneDistribution.cementery.vehicles or {};
		VehicleZoneDistribution.cementery.vehicles["Base.pzkHearse"] = {index = -1, spawnChance = 100};

--Music festivals -- Musicians tour cars (mostly near music concerts, or nightclubs (PZK)
		VehicleZoneDistribution.music_festival = VehicleZoneDistribution.music_festival or {};
		VehicleZoneDistribution.music_festival.vehicles = VehicleZoneDistribution.music_festival.vehicles or {};
		VehicleZoneDistribution.music_festival.vehicles["Base.pzkLimo"] = {index = -1, spawnChance = 1};

-- bank -- bank trucks mostly near banks or ATM (PZK)
		VehicleZoneDistribution.bank = VehicleZoneDistribution.bank or {};
		VehicleZoneDistribution.bank.vehicles = VehicleZoneDistribution.bank.vehicles or {};
		VehicleZoneDistribution.bank.vehicles["Base.pzkFranklinBankTruck"] = {index = -1, spawnChance = 99};
		VehicleZoneDistribution.bank.vehicles["Base.pzkVanMultivanPayday"] = {index = -1, spawnChance = 1};


-- field_farm -- vehicles on fields (Tractors, Harvesters etc) (PZK)
		VehicleZoneDistribution.field_farm = VehicleZoneDistribution.field_farm or {};
		VehicleZoneDistribution.field_farm.vehicles = VehicleZoneDistribution.field_farm.vehicles or {};
		VehicleZoneDistribution.field_farm.vehicles["Base.pzkTractor"] = {index = -1, spawnChance = 30};
		VehicleZoneDistribution.field_farm.vehicles["Base.pzkTractor2"] = {index = -1, spawnChance = 40};
		VehicleZoneDistribution.field_farm.vehicles["Base.pzkTractor3"] = {index = -1, spawnChance = 30};
		VehicleZoneDistribution.field_farm.randomAngle = true;

-- farm_trailers -- farming equimpent trailers mostly near vehicles on field (PZK)
		VehicleZoneDistribution.farm_trailers = VehicleZoneDistribution.farm_trailers or {};
		VehicleZoneDistribution.farm_trailers.vehicles = VehicleZoneDistribution.farm_trailers.vehicles or {};
		VehicleZoneDistribution.farm_trailers.vehicles["Base.pzkTrailerTankSprayer"] = {index = -1, spawnChance = 100};
		VehicleZoneDistribution.farm_trailers.randomAngle = true;

-- nightclub -- mafia cars (PZK)
		VehicleZoneDistribution.nightclub = VehicleZoneDistribution.nightclub or {};
		VehicleZoneDistribution.nightclub.vehicles = VehicleZoneDistribution.nightclub.vehicles or {};
		VehicleZoneDistribution.nightclub.vehicles["Base.pzkHMMV"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.nightclub.vehicles["Base.pzkContinentalBayer732"] = {index = -1, spawnChance = 40};
		VehicleZoneDistribution.nightclub.vehicles["Base.pzkMerciaLangBerg"] = {index = -1, spawnChance = 30};
		VehicleZoneDistribution.nightclub.vehicles["Base.pzkContinentalPfeiffer901"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.nightclub.vehicles["Base.pzkContinentalPfeiffer901c"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.nightclub.vehicles["Base.pzkContinentalPfeiffer930"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.nightclub.vehicles["Base.pzkContinentalPfeiffer930c"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.nightclub.vehicles["Base.pzkContinentalPyrenean310"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.nightclub.vehicles["Base.pzkFranklinStallionKingPeterGleen"] = {index = -1, spawnChance = 1};

-- PizzaWhirled (PZK)
		VehicleZoneDistribution.pizzawhirled = VehicleZoneDistribution.pizzawhirled or {};
		VehicleZoneDistribution.pizzawhirled.vehicles = VehicleZoneDistribution.pizzawhirled.vehicles or {};
		VehicleZoneDistribution.pizzawhirled.vehicles ["Base.pzkStepVanPizza"] = {index = -1, spawnChance = 100};

-- ChurnRUs (PZK)
		VehicleZoneDistribution.churnrus = VehicleZoneDistribution.churnrus or {};
		VehicleZoneDistribution.churnrus.vehicles = VehicleZoneDistribution.churnrus.vehicles or {};
		VehicleZoneDistribution.churnrus.vehicles ["Base.pzkStepVanIceCream"] = {index = -1, spawnChance = 100};

-- MilkMonarchy (PZK)
		VehicleZoneDistribution.milkmonarchy = VehicleZoneDistribution.milkmonarchy or {};
		VehicleZoneDistribution.milkmonarchy.vehicles = VehicleZoneDistribution.milkmonarchy.vehicles or {};

-- TacoDelPancho (PZK)
		VehicleZoneDistribution.tacodelpancho = VehicleZoneDistribution.tacodelpancho or {};
		VehicleZoneDistribution.tacodelpancho.vehicles = VehicleZoneDistribution.tacodelpancho.vehicles or {};

-- FoodTruckRandomSpot (PZK)
		VehicleZoneDistribution.foodtruckrandomspot = VehicleZoneDistribution.foodtruckrandomspot or {};
		VehicleZoneDistribution.foodtruckrandomspot.vehicles = VehicleZoneDistribution.foodtruckrandomspot.vehicles or {};
		VehicleZoneDistribution.foodtruckrandomspot.vehicles ["Base.pzkStepVanPizza"] = {index = -1, spawnChance = 20};
		VehicleZoneDistribution.foodtruckrandomspot.vehicles ["Base.pzkStepVanIceCream"] = {index = -1, spawnChance = 20};
		VehicleZoneDistribution.foodtruckrandomspot.vehicles ["Base.pzkStepVanTacoVan"] = {index = -1, spawnChance = 20};
		VehicleZoneDistribution.foodtruckrandomspot.vehicles ["Base.pzkStepVanHotDog"] = {index = -1, spawnChance = 20};
		VehicleZoneDistribution.foodtruckrandomspot.vehicles ["Base.pzkStepVanPierogi"] = {index = -1, spawnChance = 20};
		VehicleZoneDistribution.foodtruckrandomspot.vehicles ["Base.pzkStepVanCoffe"] = {index = -1, spawnChance = 20};
		VehicleZoneDistribution.foodtruckrandomspot.vehicles ["Base.pzkStepVanCatfish"] = {index = -1, spawnChance = 20};
		
--fueltanker (PZK)
		
		VehicleZoneDistribution.fueltanker = VehicleZoneDistribution.fueltanker or {};
		VehicleZoneDistribution.fueltanker.vehicles = VehicleZoneDistribution.fueltanker.vehicles or {};
		VehicleZoneDistribution.fueltanker.vehicles["Base.pzkTrailerRegularFuelTanker"] = {index = -1, spawnChance = 45};
		VehicleZoneDistribution.fueltanker.vehicles["Base.pzkFranklinTruckTankerFossoil"] = {index = -1, spawnChance = 45};
		VehicleZoneDistribution.fueltanker.vehicles["Base.pzkTrailerRegularPropaneTanker"] = {index = -1, spawnChance = 10};
		
	-- bigtrailerparkinglot (TSARLIB)
	VehicleZoneDistribution.bigtrailerparkinglot = VehicleZoneDistribution.bigtrailerparkinglot or {};
	VehicleZoneDistribution.bigtrailerparkinglot.vehicles = VehicleZoneDistribution.bigtrailerparkinglot.vehicles or {};
	VehicleZoneDistribution.bigtrailerparkinglot.vehicles["Base.pzkTrailerRegularCourtains"] = {index = -1, spawnChance = 30};
	VehicleZoneDistribution.bigtrailerparkinglot.vehicles["Base.pzkTrailerRegularCourtainsBandit"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.bigtrailerparkinglot.vehicles["Base.pzkTrailerRegularFlatbed"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.bigtrailerparkinglot.vehicles["Base.pzkTrailerRegularTanker"] = {index = -1, spawnChance = 5};	
	VehicleZoneDistribution.bigtrailerparkinglot.vehicles["Base.pzkTrailerRegularWaterTankerTainted"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.bigtrailerparkinglot.vehicles["Base.pzkTrailerRegularWaterTanker"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.bigtrailerparkinglot.vehicles["Base.pzkTrailerRegularFuelTanker"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.bigtrailerparkinglot.vehicles["Base.pzkTrailerRegularCourtainsWhite"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.bigtrailerparkinglot.vehicles["Base.pzkTrailerRegularContainer"] = {index = -1, spawnChance = 30};
	VehicleZoneDistribution.bigtrailerparkinglot.vehicles["Base.pzkTrailerRegularFedLog"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.bigtrailerparkinglot.vehicles["Base.pzkTrailerRegularGigamart"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.bigtrailerparkinglot.vehicles["Base.pzkTrailerRegularPharmahung"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.bigtrailerparkinglot.vehicles["Base.pzkTrailerRegularValutech"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.bigtrailerparkinglot.vehicles["Base.pzkTrailerRegularUStoreIt"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.bigtrailerparkinglot.vehicles["Base.pzkTrailerRegularCourtainsKnight"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.bigtrailerparkinglot.vehicles["Base.pzkTrailerRegularSpiffo"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.bigtrailerparkinglot.vehicles["Base.pzkTrailerRegularPop"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.bigtrailerparkinglot.vehicles["Base.pzkTrailerRegularUStoreIt"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.bigtrailerparkinglot.vehicles["Base.pzkTrailerRegularValutech"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.bigtrailerparkinglot.vehicles["Base.pzkTrailerRegularPharmahung"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.bigtrailerparkinglot.vehicles["Base.pzkTrailerRegularGigamart"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.bigtrailerparkinglot.vehicles["Base.pzkTrailerRegularFedLog"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.bigtrailerparkinglot.vehicles["Base.pzkTrailerRegularContainer"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.bigtrailerparkinglot.vehicles["Base.pzkTrailerRegularCourtainsWhite"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.bigtrailerparkinglot.vehicles["Base.pzkTrailerRegularCourtains"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.bigtrailerparkinglot.vehicles["Base.pzkTrailerRegularPropaneTanker"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.bigtrailerparkinglot.vehicles["Base.pzkTrailerRegularLivestock"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.bigtrailerparkinglot.vehicles["Base.pzkTrailerRegularLivestock2"] = {index = -1, spawnChance = 1};
	
	
	-- smalltrailerparkinglot(TSARLIB)
	VehicleZoneDistribution.smalltrailerparkinglot = VehicleZoneDistribution.smalltrailerparkinglot or {};
	VehicleZoneDistribution.smalltrailerparkinglot.vehicles = VehicleZoneDistribution.smalltrailerparkinglot.vehicles or {};
	VehicleZoneDistribution.smalltrailerparkinglot.vehicles["Base.pzkTrailerHorseBox"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.smalltrailerparkinglot.vehicles["Base.pzkTrailerCamping"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.smalltrailerparkinglot.vehicles["Base.pzkTrailerTankSmall"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.smalltrailerparkinglot.vehicles["Base.pzkTrailerBoxDual"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.smalltrailerparkinglot.vehicles["Base.pzkTrailerTankMedium"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.smalltrailerparkinglot.vehicles["Base.pzkDualTrailerCover"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.smalltrailerparkinglot.vehicles["Base.pzkDualTrailer"] = {index = -1, spawnChance = 1};
	
				
	-- truckparkinglot (TSARLIB)
	VehicleZoneDistribution.truckparkinglot = VehicleZoneDistribution.truckparkinglot or {};
	VehicleZoneDistribution.truckparkinglot.vehicles = VehicleZoneDistribution.truckparkinglot.vehicles or {};
	VehicleZoneDistribution.truckparkinglot.vehicles["Base.pzkFranklinTruckSemi"] = {index = -1, spawnChance = 20};
	VehicleZoneDistribution.truckparkinglot.vehicles["Base.pzkFreightlinerFlat2"] = {index = -1, spawnChance = 20};
	VehicleZoneDistribution.truckparkinglot.vehicles["Base.pzkFreightlinerFlat"] = {index = -1, spawnChance = 20};
	VehicleZoneDistribution.truckparkinglot.vehicles["Base.pzkPeterbuiltSleeper"] = {index = -1, spawnChance = 20};
	VehicleZoneDistribution.truckparkinglot.vehicles["Base.pzkPeterbuiltSleeperBandit"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.truckparkinglot.vehicles["Base.pzkPeterbuilt"] = {index = -1, spawnChance = 20};
	VehicleZoneDistribution.truckparkinglot.vehicles["Base.pzkPeterbuiltPop"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.truckparkinglot.vehicles["Base.pzkPeterbuiltSleeperPop"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.truckparkinglot.vehicles["Base.pzkPeterbuiltSleeperOptimus"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.truckparkinglot.vehicles["Base.pzkDashNational980"] = {index = -1, spawnChance = 20};
	
	
	--semi (PZK)
	VehicleZoneDistribution.semi = VehicleZoneDistribution.semi or {};
	VehicleZoneDistribution.semi.vehicles = VehicleZoneDistribution.semi.vehicles or {};
	VehicleZoneDistribution.semi.vehicles["Base.pzkFranklinTruckSemi"] = {index = -1, spawnChance = 20};
	VehicleZoneDistribution.semi.vehicles["Base.pzkFreightlinerFlat2"] = {index = -1, spawnChance = 20};
	VehicleZoneDistribution.semi.vehicles["Base.pzkFreightlinerFlat"] = {index = -1, spawnChance = 20};
	VehicleZoneDistribution.semi.vehicles["Base.pzkPeterbuiltSleeper"] = {index = -1, spawnChance = 20};
	VehicleZoneDistribution.semi.vehicles["Base.pzkPeterbuiltSleeperBandit"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.semi.vehicles["Base.pzkPeterbuilt"] = {index = -1, spawnChance = 20};
	VehicleZoneDistribution.semi.vehicles["Base.pzkFreightlinerFlatSpiffo"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.semi.vehicles["Base.pzkPeterbuiltFossoil"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.semi.vehicles["Base.pzkPeterbuiltPop"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.semi.vehicles["Base.pzkPeterbuiltSleeperPop"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.semi.vehicles["Base.pzkPeterbuiltSleeperOptimus"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.semi.vehicles["Base.pzkDashNational980"] = {index = -1, spawnChance = 20};
	
	
	--kytc (P. Indiana + vanila)
		VehicleZoneDistribution.kytc = VehicleZoneDistribution.kytc or {};
		VehicleZoneDistribution.kytc.vehicles = VehicleZoneDistribution.kytc.vehicles or {};
		
	--power (P. Indiana + vanila)
		VehicleZoneDistribution.power = VehicleZoneDistribution.power or {};
		VehicleZoneDistribution.power.vehicles = VehicleZoneDistribution.power.vehicles or {};
		
	--indot (P. Indiana only)
		VehicleZoneDistribution.indot = VehicleZoneDistribution.indot or {};
		VehicleZoneDistribution.indot.vehicles = VehicleZoneDistribution.indot.vehicles or {};
		
	--phone (P. Indiana only)
		VehicleZoneDistribution.phone = VehicleZoneDistribution.phone or {};
		VehicleZoneDistribution.phone.vehicles = VehicleZoneDistribution.phone.vehicles or {};
		
	--water service (P. Indiana only)
		VehicleZoneDistribution.water = VehicleZoneDistribution.water or {};
		VehicleZoneDistribution.water.vehicles = VehicleZoneDistribution.water.vehicles or {};
	
	--rail (P. Indiana + vanila)
		VehicleZoneDistribution.rail = VehicleZoneDistribution.rail or {};
		VehicleZoneDistribution.rail.vehicles = VehicleZoneDistribution.rail.vehicles or {};
		VehicleZoneDistribution.rail.vehicles["Base.pzkChevalier150UtilityRail"] = {index = -1, spawnChance = 30};
		VehicleZoneDistribution.rail.vehicles["pzkChevalierE6Rail"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.rail.vehicles["pzkChevalierF6Rail"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.rail.vehicles["pzkChevalierPickupCrewLongRail"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.rail.vehicles["pzkChevalierPickupCrewMediumRail"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.rail.vehicles["pzkChevalierVansRail"] = {index = -1, spawnChance = 20};
		VehicleZoneDistribution.rail.vehicles["pzkStepvanB30Rail"] = {index = -1, spawnChance = 20};
	
		--Church Bus (PZK)
		VehicleZoneDistribution.buschurch = VehicleZoneDistribution.buschurch or {};
		VehicleZoneDistribution.buschurch.vehicles = VehicleZoneDistribution.buschurch.vehicles or {};
		VehicleZoneDistribution.buschurch.vehicles["Base.pzkStepVanB30BusChurchWoodcrafterMiddle"] = {index = -1, spawnChance = 100};
	
	--schoolBus (PZK)
		VehicleZoneDistribution.schoolbus = VehicleZoneDistribution.schoolbus or {};
		VehicleZoneDistribution.schoolbus.vehicles = VehicleZoneDistribution.schoolbus.vehicles or {};
		VehicleZoneDistribution.schoolbus.vehicles["Base.pzkFranklinTruckBus"] = {index = -1, spawnChance = 80}; 
		VehicleZoneDistribution.schoolbus.vehicles["Base.pzkStepVanB30BusWoodcrafterMiddle"] = {index = -1, spawnChance = 20};

			--	BUS SERVICE (PZK)
		VehicleZoneDistribution.busservice = VehicleZoneDistribution.busservice or {};
		VehicleZoneDistribution.busservice.vehicles = VehicleZoneDistribution.busservice.vehicles or {};
		VehicleZoneDistribution.busservice.vehicles["Base.pzkTransitBus"] = {index = 2, spawnChance = 100};
 
		--	BUS STATION (PZK)
		VehicleZoneDistribution.busstation = VehicleZoneDistribution.busstation or {};
		VehicleZoneDistribution.busstation.vehicles = VehicleZoneDistribution.busstation.vehicles or {};
		VehicleZoneDistribution.busstation.vehicles["Base.pzkVanSeatsTaxi"] = {index = 2, spawnChance = 10};
		VehicleZoneDistribution.busstation.vehicles["Base.pzkTransitBus"] = {index = 2, spawnChance = 90};

---------------------------------------- VANILLA ZONES --------------------------------------------------


	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkVanCamper"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMinivanStellaris"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkVanBrig"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalierMaroca"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinGalloper"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkPickupFranklin"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkStepVanMilk"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkHearse"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkLimo"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkPickUpTruck93"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkVanBox"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkCarMuscle"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkHMMV"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkStepVanUPZ"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinPony"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkStepVanIceCream"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkStepVanPizza"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkStepVanCoffe"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinTriumph"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinTriumphTaxi"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkPickUpTruckWoodboarded"] = {index = -1, spawnChance = 5};

	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalierCeriseSedan"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalierCeriseSedanTaxi"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashHellion"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashHellionTaxi"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashMayor"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashMayorTaxi"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashRapier"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinTriumphTWD"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinTriumphTWDTaxi"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkCeriseStationWagon"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashMayorStationWagon"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkRapierStationWagon"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTriumphTWDStationWagon"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTriumphTWDStationWagonTaxi"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkVanSeatsTaxi"] = {index = -1, spawnChance = 5};

	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashRoyal"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashRoyalGrand"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalierDownhill"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashTornado"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMastersonLady"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashPhoenix"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashPhoenixBandit"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalierE6"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalierF6"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalierRoadrunner"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMastersonInitial"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMastersonXSR"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkContinentalSpirit"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashNoble"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinStallion"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinStallion2"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinStallionSport"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinIslander"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinHomelander"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMastersonCrown"] = {index = -1, spawnChance = 15};

	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinBankTruck"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinTruckRV"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinTruckBed"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinTruckShort"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinTruckCab"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinTruckBus"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinTruckBox"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinTruckDump"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinTruckFlatbed"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinTruckGarbage"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinTruckPropane"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinTruckPropane2"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinTruckTankerFossoil"] = {index = 0, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinTruckTankerSeptic"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinTruckTankerWater"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinTruckTow"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinTruckUtility"] = {index = -1, spawnChance = 1};
	
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTruckD70Box"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTruckD70Box2"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTruckD70Dump"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTruckD70Dump2"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTruckD70Tow"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTruckD70Tow2"] = {index = -1, spawnChance = 1};
	

	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMinivanC22"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMinivanChev"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMinivanConvoy"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMinivanMPV"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMinivanStellarisMail"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMinivanStellarisTaxi"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMinivanT3"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMinivanT3C"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMinivanTask"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMinivan2"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMinivanPrev"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkVanMultivan"] = {index = -1, spawnChance = 5};

	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMastersonScout4D"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalierPickupCrewLong"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalierPickupCrewMedium"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalierProvince"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalierProvinceLong"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalierLaserModern"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalierE6Van"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalierF6Van"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalierLaserOffroader"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFtypeTowTruck"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalierTowTruck"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMerciaLangBerg"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashCheyene"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkContinentalCruiser"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklin350FWagonLong"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklin350FPickupCrewLong"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklin250FPickupWagonLong"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklin250FPickupCrewLong"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklin250FWagonLong"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklin150van"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklin150FPickupReg"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklin150FWagonMedium"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklin150FPickupMedium"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashIntruder250WagonLong"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashIntruder250PickupLong"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashIntruder150short"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashIntruder150RegVan"] = {index = -1, spawnChance = 5};

	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMastersonLadyZ"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashRunnerGeneral"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashGTA"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinStallionKing"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashRunner"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashPiranha"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashChampion"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashPhoenix80"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalierMaroca80"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkContinentalBug"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkContinentalBugHerbie"] = {index = -1, spawnChance = 1};

	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkContinentalBayer330Sport"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkContinentalBayer3304D"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkContinentalBayer3302D"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkContinentalBayer534"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkContinentalBayer732"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMerciaLang1240"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMerciaLang12402D"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMerciaLang4000Cabrio"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkContinentalBayer330Cabrio"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkCarMuscleCabrio"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashRancherMail"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashRancherCabrio"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalierCosetteCabrio"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMastersonSunrise"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMastersonSensation"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMastersonExpander"] = {index = -1, spawnChance = 4};

	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinTruckSemi"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFreightlinerFlat2"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFreightlinerFlat"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkPeterbuiltSleeper"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkPeterbuiltSleeperBandit"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkPeterbuilt"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMastersonApex4D"] = {index = -1, spawnChance = 1};
	
--	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTrailerRegularCourtains"] = {index = -1, spawnChance = 1};
--	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTrailerRegularCourtainsBandit"] = {index = -1, spawnChance = 1};
--	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTrailerRegularFlatbed"] = {index = -1, spawnChance = 1};
--	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTrailerRegularTanker"] = {index = -1, spawnChance = 1};	
--	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTrailerRegularWaterTankerTainted"] = {index = -1, spawnChance = 1};
--	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTrailerRegularWaterTanker"] = {index = -1, spawnChance = 1};
--	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTrailerRegularFuelTanker"] = {index = -1, spawnChance = 1};
--	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTrailerRegularCourtainsWhite"] = {index = -1, spawnChance = 1};
--	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTrailerRegularContainer"] = {index = -1, spawnChance = 1};
--	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTrailerRegularFedLog"] = {index = -1, spawnChance = 1};
--	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTrailerRegularGigamart"] = {index = -1, spawnChance = 1};
--	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTrailerRegularPharmahung"] = {index = -1, spawnChance = 1};
--	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTrailerRegularValutech"] = {index = -1, spawnChance = 1};
--	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTrailerRegularUStoreIt"] = {index = -1, spawnChance = 1};
--	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTrailerRegularCourtainsKnight"] = {index = -1, spawnChance = 1};
--	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTrailerRegularSpiffo"] = {index = -1, spawnChance = 1};
--	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTrailerRegularPop"] = {index = -1, spawnChance = 1};
--	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTrailerRegularPropaneTanker"] = {index = -1, spawnChance = 1};
	

	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTrailerHorseBox"] = {index = -1, spawnChance = 1};


	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkStepVanHotDog"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkContinentalPfeiffer901"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkContinentalPfeiffer901c"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkContinentalPfeiffer930"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkContinentalPfeiffer930c"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTrailerCamping"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTrailerTankSmall"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTrailerBoxDual"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTrailerTankMedium"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDualTrailerCover"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDualTrailer"] = {index = -1, spawnChance = 1};

	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinTriumphWagon"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalierCerise93"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalierCerise93Taxi"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalierCerise93Wagon"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashElite2D"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMastersonHarmonyWagon"] = {index = -1, spawnChance = 8};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMastersonHarmony"] = {index = -1, spawnChance = 8};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkContinentalNord"] = {index = -1, spawnChance = 8};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkContinentalNordWagon"] = {index = -1, spawnChance = 8};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkContinentalPyrenean310"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTriumphTWDStationWagonGriswold"] = {index = -1, spawnChance = 1};

	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalierVan70"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashVan70"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinVan70"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalierCeriseLimo"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashHellionLimo"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashRapierLimo"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinHomelanderLimo"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinTriumphTWD91"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTriumphTWDStationWagon"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkTriumphTWDStationWagonTaxi"] = {index = -1, spawnChance = 15};

	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkStepVanTacoVan"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinGalloperPrimal"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalierLaserK5"] = {index = -1, spawnChance = 3};

	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashVan70Riddle"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkVanMultivanPayday"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkVanZSquad"] = {index = -1, spawnChance = 1};

	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashNavajoP"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashNavajoW"] = {index = -1, spawnChance = 4};

	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkF350BoxUmoveit"] = {index =  2, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinStallionKingPeterGleen"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinStallionKing2"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinStallionKing3"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinStallionKing4"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinStallionKing5"] = {index = -1, spawnChance = 3};

	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinStallionKingTheKing"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinStallionKingKenMiles"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinStallionKingFrankBullitt"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinStallionKingEleanor"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinStallionKingSeanBoswell"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinStallionKingJohnWick"] = {index = -1, spawnChance = 1};

	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkHearseGhoulbusters"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMastersonRotaryC"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkVanGigamart"] = {index = -1, spawnChance = 5};

	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMastersonInitialFuji"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMastersonRotaryCRyosuke"] = {index = -1, spawnChance = 1};

	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalierRookie"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkAutowagenBunny"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMastersonRice"] = {index = -1, spawnChance = 5};
	
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFreightlinerTerminatorTow"] = {index = -1, spawnChance = 5};
		
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMastersonIberiaVan1"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMastersonIberiaVan2"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMastersonIberiaPickup"] = {index = -1, spawnChance = 5};
	
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDash600"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashCirilla"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashDecade"] = {index = -1, spawnChance = 5};
	
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashPhoenix75"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashPhoenix75JP"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkContinentalBugRedT"] = {index = -1, spawnChance = 1};
	
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFreightlinerFlatOptimus"] = {index = -1, spawnChance = 1};
	
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkPeterbuiltPop"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkPeterbuiltSleeperPop"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkPeterbuiltSleeperOptimus"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashPrince"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashPrinceBluesmobile"] = {index = -1, spawnChance = 1};
	
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkContinentalGuardian"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkContinentalGuardianLlama"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkContinentalGuardianService"] = {index = -1, spawnChance = 1};
	
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkStepVanPierogi"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinTruckSemiMadMax"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDashNational980"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalierLaserK5"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkF150Utility"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkF150UtilityMoore"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkF150UtilityNewCoalfieldMechanic"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkF150UtilityIrvingtonSpeedway"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkF150UtilityPublicWorks"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkF150BoxFlatbedPublicWorks"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkF150BoxFlatbed"] = {index = -1, spawnChance = 3};
	
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDash150Utility"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDash150UtilityMoore"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDash150UtilityNewCoalfieldMechanic"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDash150UtilityIrvingtonSpeedway"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDash150UtilityPublicWorks"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDash150BoxFlatbedPublicWorks"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDash150BoxFlatbed"] = {index = -1, spawnChance = 3};
	
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalier150Utility"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalier150UtilityMoore"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalier150UtilityNewCoalfieldMechanic"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalier150UtilityIrvingtonSpeedway"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalier150UtilityPublicWorks"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalier150BoxFlatbedPublicWorks"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalier150BoxFlatbed"] = {index = -1, spawnChance = 3};
	
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinTruckFlatbedPublicWorks"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkPickUpTruckPublicWorks"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkVanPublicWorks"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFranklinTruckDumpPublicWorks"] = {index = -1, spawnChance = 1};

	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMastersonTR2Kouki"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMastersonTR2Zenki"] = {index = -1, spawnChance = 2};
	
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkCVanCargo"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkCVan"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkCVanMultivan"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkCVan6Seats"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkCVanCargo3"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkCVan3"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkCVanMultivan3"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkCVan6Seats3"] = {index = -1, spawnChance = 4};
	
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDVanCargo"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDVan"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDVanMultivan"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDVan6Seats"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDVanCargo2"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDVan2"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDVanMultivan2"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDVan6Seats2"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDVanCargo3"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDVan3"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDVanMultivan3"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkDVan6Seats3"] = {index = -1, spawnChance = 3};
	
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFVanCargo"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFVan"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFVanMultivan"] = {index = -1, spawnChance = 6};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkFVan6Seats"] = {index = -1, spawnChance = 7};
	
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMastersonSil80"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkMastersonSil80Mako"] = {index = -1, spawnChance = 1};
	
	VehicleZoneDistribution.parkingstall.vehicles["Base.SUV"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.OffRoad"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkStepVanB30"] = {index = -1, spawnChance = 6};
	
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalierD100Pickup"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.parkingstall.vehicles["Base.pzkChevalierD100PickupCustom"] = {index = -1, spawnChance = 1};

	
	

	


--	VehicleZoneDistribution.trailerpark.vehicles["Base.CarNormal"] = {index = -1, spawnChance = 2};
--	VehicleZoneDistribution.trailerpark.vehicles["Base.CarStationWagon"] = {index = -1, spawnChance = 1};
--	VehicleZoneDistribution.trailerpark.vehicles["Base.CarStationWagon2"] = {index = -1, spawnChance = 1};
--	VehicleZoneDistribution.trailerpark.vehicles["Base.SmallCar"] = {index = -1, spawnChance = 3};
--	VehicleZoneDistribution.trailerpark.vehicles["Base.SmallCar02"] = {index = -1, spawnChance = 3};

	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkVanCamper"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkCarMuscle"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkPickupFranklin"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkFranklinGalloper"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkVanBrig"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkVanBox"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkHearse"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkFranklinPony"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkStepVanIceCream"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkStepVanPizza"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkPickUpTruckWoodboarded"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkChevalierCeriseSedan"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkDashHellion"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkDashMayor"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkDashRapier"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkFranklinTriumphTWD"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkCeriseStationWagon"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkDashMayorStationWagon"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkRapierStationWagon"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkTriumphTWDStationWagon"] = {index = -1, spawnChance = 10};

	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkChevalierDownhill"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkDashTornado"] = {index = -1, spawnChance = 10};

	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkFranklinTruckRV"] = {index = -1, spawnChance = 5};

	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkMastersonLadyZ"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkDashRunnerGeneral"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkFranklinStallionKing"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkDashRunner"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkDashChampion"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkDashPhoenix80"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkChevalierMaroca80"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkContinentalBug"] = {index = -1, spawnChance = 5};

	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkStepVanHotDog"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkTrailerCamping"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkTrailerTankSmall"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkTrailerBoxDual"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkTrailerTankMedium"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkDualTrailerCover"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkDualTrailer"] = {index = -1, spawnChance = 4};

	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkVanBox"] = {index = 7, spawnChance = 1};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkFranklinTruckBox"] = {index = 6, spawnChance = 1};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkTrailerBoxDual"] = {index =  2, spawnChance = 1};

	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkChevalierVan70"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkDashVan70"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkFranklinVan70"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkTriumphTWDStationWagon"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkTriumphTWDStationWagonTaxi"] = {index = -1, spawnChance = 5};


	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkFranklinGalloperPrimal"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkChevalierLaserK5"] = {index = -1, spawnChance = 5};

	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkFranklinStallionKing3"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkFranklinStallionKing4"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkDashPhoenix75"] = {index = -1, spawnChance = 3};
	
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkStepVanPierogi"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkChevalierLaserK5"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkChevalierD100Pickup"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.trailerpark.vehicles["Base.pzkChevalierD100PickupCustom"] = {index = -1, spawnChance = 1};



--	VehicleZoneDistribution.bad.vehicles["Base.CarNormal"] = {index = -1, spawnChance = 2};
--	VehicleZoneDistribution.bad.vehicles["Base.CarStationWagon"] = {index = -1, spawnChance = 1};
--	VehicleZoneDistribution.bad.vehicles["Base.CarStationWagon2"] = {index = -1, spawnChance = 1};
--	VehicleZoneDistribution.bad.vehicles["Base.SmallCar"] = {index = -1, spawnChance = 3};
--	VehicleZoneDistribution.bad.vehicles["Base.SmallCar02"] = {index = -1, spawnChance = 3};


	VehicleZoneDistribution.bad.vehicles["Base.pzkMinivanStellaris"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.bad.vehicles["Base.pzkVanBrig"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.bad.vehicles["Base.pzkFranklinGalloper"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.bad.vehicles["Base.pzkPickupFranklin"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.bad.vehicles["Base.pzkHearse"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.bad.vehicles["Base.pzkVanBox"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.bad.vehicles["Base.pzkCarMuscle"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.bad.vehicles["Base.pzkStepVanUPZ"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.bad.vehicles["Base.pzkFranklinPony"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.bad.vehicles["Base.pzkStepVanIceCream"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.bad.vehicles["Base.pzkStepVanPizza"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.bad.vehicles["Base.pzkPickUpTruckWoodboarded"] = {index = -1, spawnChance = 5};

	VehicleZoneDistribution.bad.vehicles["Base.pzkChevalierCeriseSedan"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.bad.vehicles["Base.pzkDashHellion"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.bad.vehicles["Base.pzkDashMayor"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.bad.vehicles["Base.pzkDashRapier"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.bad.vehicles["Base.pzkFranklinTriumphTWD"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.bad.vehicles["Base.pzkChevalierE6"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.bad.vehicles["Base.pzkCeriseStationWagon"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.bad.vehicles["Base.pzkDashMayorStationWagon"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.bad.vehicles["Base.pzkRapierStationWagon"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.bad.vehicles["Base.pzkTriumphTWDStationWagon"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.bad.vehicles["Base.pzkDashRoyal"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.bad.vehicles["Base.pzkChevalierDownhill"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.bad.vehicles["Base.pzkDashTornado"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.bad.vehicles["Base.pzkMinivanChev"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.bad.vehicles["Base.pzkVanMultivan"] = {index = -1, spawnChance = 4};

	VehicleZoneDistribution.bad.vehicles["Base.pzkMastersonLadyZ"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.bad.vehicles["Base.pzkDashRunnerGeneral"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.bad.vehicles["Base.pzkDashGTA"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.bad.vehicles["Base.pzkFranklinStallionKing"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.bad.vehicles["Base.pzkDashRunner"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.bad.vehicles["Base.pzkDashPiranha"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.bad.vehicles["Base.pzkDashChampion"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.bad.vehicles["Base.pzkDashPhoenix80"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.bad.vehicles["Base.pzkChevalierMaroca80"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.bad.vehicles["Base.pzkContinentalBug"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.bad.vehicles["Base.pzkContinentalBugHerbie"] = {index = -1, spawnChance = 1};

	VehicleZoneDistribution.bad.vehicles["Base.pzkStepVanHotDog"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.bad.vehicles["Base.pzkStepVanCatfish"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.bad.vehicles["Base.pzkTrailerTankSmall"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.bad.vehicles["Base.pzkTrailerBoxDual"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.bad.vehicles["Base.pzkTrailerTankMedium"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.bad.vehicles["Base.pzkDualTrailerCover"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.bad.vehicles["Base.pzkDualTrailer"] = {index = -1, spawnChance = 2};

	VehicleZoneDistribution.bad.vehicles["Base.pzkVanBox"] = {index = 7, spawnChance = 3};
	VehicleZoneDistribution.bad.vehicles["Base.pzkFranklinTruckBox"] = {index = 6, spawnChance = 3};
	VehicleZoneDistribution.bad.vehicles["Base.pzkTrailerBoxDual"] = {index =  2, spawnChance = 3};
	VehicleZoneDistribution.bad.vehicles["Base.pzkF350BoxUmoveit"] = {index =  2, spawnChance = 3};

	VehicleZoneDistribution.bad.vehicles["Base.pzkChevalierVan70"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.bad.vehicles["Base.pzkDashVan70"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.bad.vehicles["Base.pzkFranklinVan70"] = {index = -1, spawnChance = 3};

	VehicleZoneDistribution.bad.vehicles["Base.pzkStepVanTacoVan"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.bad.vehicles["Base.pzkFranklinGalloperPrimal"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.bad.vehicles["Base.pzkChevalierLaserK5"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.bad.vehicles["Base.pzkDashPhoenix75"] = {index = -1, spawnChance = 5};
	
	VehicleZoneDistribution.bad.vehicles["Base.pzkChevalierLaserK5"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.bad.vehicles["Base.pzkChevalierD100Pickup"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.bad.vehicles["Base.pzkChevalierD100PickupCustom"] = {index = -1, spawnChance = 1};




--	VehicleZoneDistribution.medium.vehicles["Base.CarNormal"] = {index = -1, spawnChance = 2};
--	VehicleZoneDistribution.medium.vehicles["Base.CarStationWagon"] = {index = -1, spawnChance = 1};
--	VehicleZoneDistribution.medium.vehicles["Base.CarStationWagon2"] = {index = -1, spawnChance = 1};
--	VehicleZoneDistribution.medium.vehicles["Base.ModernCar"] = {index = -1, spawnChance = 2};
--	VehicleZoneDistribution.medium.vehicles["Base.ModernCar02"] = {index = -1, spawnChance = 2};

	VehicleZoneDistribution.medium.vehicles["Base.pzkVanCamper"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.medium.vehicles["Base.pzkMinivanStellaris"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.medium.vehicles["Base.pzkVanBrig"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.medium.vehicles["Base.pzkChevalierMaroca"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.medium.vehicles["Base.pzkFranklinGalloper"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.medium.vehicles["Base.pzkPickupFranklin"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.medium.vehicles["Base.pzkStepVanMilk"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.medium.vehicles["Base.pzkHearse"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.medium.vehicles["Base.pzkPickUpTruck93"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.medium.vehicles["Base.pzkVanBox"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.medium.vehicles["Base.pzkCarMuscle"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.medium.vehicles["Base.pzkCarMuscleCabrio"] = {index = -1, spawnChance = 7};
	VehicleZoneDistribution.medium.vehicles["Base.pzkStepVanUPZ"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.medium.vehicles["Base.pzkFranklinPony"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.medium.vehicles["Base.pzkChevalierCeriseSedan"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.medium.vehicles["Base.pzkDashHellion"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.medium.vehicles["Base.pzkDashMayor"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.medium.vehicles["Base.pzkDashRapier"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.medium.vehicles["Base.pzkFranklinTriumphTWD"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.medium.vehicles["Base.pzkCeriseStationWagon"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.medium.vehicles["Base.pzkDashMayorStationWagon"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.medium.vehicles["Base.pzkRapierStationWagon"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.medium.vehicles["Base.pzkTriumphTWDStationWagon"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.medium.vehicles["Base.pzkFranklinTriumphWagon"] = {index = -1, spawnChance = 5};

	VehicleZoneDistribution.medium.vehicles["Base.pzkDashRoyal"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.medium.vehicles["Base.pzkChevalierDownhill"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.medium.vehicles["Base.pzkDashTornado"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.medium.vehicles["Base.pzkMastersonLady"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.medium.vehicles["Base.pzkDashPhoenix"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.medium.vehicles["Base.pzkChevalierF6"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.medium.vehicles["Base.pzkChevalierRoadrunner"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.medium.vehicles["Base.pzkMastersonInitial"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.medium.vehicles["Base.pzkMastersonXSR"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.medium.vehicles["Base.pzkDashNoble"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.medium.vehicles["Base.pzkFranklinStallion"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.medium.vehicles["Base.pzkFranklinStallion2"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.medium.vehicles["Base.pzkFranklinIslander"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.medium.vehicles["Base.pzkFranklinHomelander"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.medium.vehicles["Base.pzkMastersonCrown"] = {index = -1, spawnChance = 15};

	VehicleZoneDistribution.medium.vehicles["Base.pzkFranklinTruckRV"] = {index = -1, spawnChance = 5};

	VehicleZoneDistribution.medium.vehicles["Base.pzkMinivanC22"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.medium.vehicles["Base.pzkMinivanChev"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.medium.vehicles["Base.pzkMinivanConvoy"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.medium.vehicles["Base.pzkMinivanMPV"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.medium.vehicles["Base.pzkMinivanStellarisMail"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.medium.vehicles["Base.pzkMinivanStellarisTaxi"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.medium.vehicles["Base.pzkMinivanT3"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.medium.vehicles["Base.pzkMinivanT3C"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.medium.vehicles["Base.pzkMinivanTask"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.medium.vehicles["Base.pzkMinivan2"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.medium.vehicles["Base.pzkMinivanPrev"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.medium.vehicles["Base.pzkVanMultivan"] = {index = -1, spawnChance = 5};

	VehicleZoneDistribution.medium.vehicles["Base.pzkChevalierPickupCrewLong"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.medium.vehicles["Base.pzkChevalierPickupCrewMedium"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.medium.vehicles["Base.pzkChevalierProvince"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.medium.vehicles["Base.pzkChevalierProvinceLong"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.medium.vehicles["Base.pzkChevalierLaserModern"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.medium.vehicles["Base.pzkChevalierE6Van"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.medium.vehicles["Base.pzkChevalierF6Van"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.medium.vehicles["Base.pzkChevalierLaserOffroader"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.medium.vehicles["Base.pzkFranklin350FWagonLong"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.medium.vehicles["Base.pzkFranklin350FPickupCrewLong"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.medium.vehicles["Base.pzkFranklin250FPickupWagonLong"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.medium.vehicles["Base.pzkFranklin250FPickupCrewLong"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.medium.vehicles["Base.pzkFranklin250FWagonLong"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.medium.vehicles["Base.pzkFranklin150van"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.medium.vehicles["Base.pzkFranklin150FPickupReg"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.medium.vehicles["Base.pzkFranklin150FWagonMedium"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.medium.vehicles["Base.pzkFranklin150FPickupMedium"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.medium.vehicles["Base.pzkDashIntruder250WagonLong"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.medium.vehicles["Base.pzkDashIntruder250PickupLong"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.medium.vehicles["Base.pzkDashIntruder150short"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.medium.vehicles["Base.pzkDashIntruder150RegVan"] = {index = -1, spawnChance = 2};

	VehicleZoneDistribution.medium.vehicles["Base.pzkMastersonLadyZ"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.medium.vehicles["Base.pzkDashGTA"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.medium.vehicles["Base.pzkFranklinStallionKing"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.medium.vehicles["Base.pzkDashRunner"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.medium.vehicles["Base.pzkDashPiranha"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.medium.vehicles["Base.pzkDashChampion"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.medium.vehicles["Base.pzkDashPhoenix80"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.medium.vehicles["Base.pzkChevalierMaroca80"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.medium.vehicles["Base.pzkContinentalBug"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.medium.vehicles["Base.pzkContinentalBugHerbie"] = {index = -1, spawnChance = 1};

	VehicleZoneDistribution.medium.vehicles["Base.pzkStepVanHotDog"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.medium.vehicles["Base.pzkTrailerCamping"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.medium.vehicles["Base.pzkTrailerTankSmall"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.medium.vehicles["Base.pzkTrailerBoxDual"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.medium.vehicles["Base.pzkTrailerTankMedium"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.medium.vehicles["Base.pzkDualTrailerCover"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.medium.vehicles["Base.pzkDualTrailer"] = {index = -1, spawnChance = 1};

	VehicleZoneDistribution.medium.vehicles["Base.pzkFranklinTriumphWagon"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.medium.vehicles["Base.pzkChevalierCerise93"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.medium.vehicles["Base.pzkChevalierCerise93Taxi"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.medium.vehicles["Base.pzkChevalierCerise93Wagon"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.medium.vehicles["Base.pzkDashElite2D"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.medium.vehicles["Base.pzkMastersonHarmonyWagon"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.medium.vehicles["Base.pzkMastersonHarmony"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.medium.vehicles["Base.pzkContinentalNord"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.medium.vehicles["Base.pzkContinentalNordWagon"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.medium.vehicles["Base.pzkTriumphTWDStationWagonGriswold"] = {index = -1, spawnChance = 1};

	VehicleZoneDistribution.medium.vehicles["Base.pzkVanBox"] = {index = 7, spawnChance = 3};
	VehicleZoneDistribution.medium.vehicles["Base.pzkFranklinTruckBox"] = {index = 6, spawnChance = 3};
	VehicleZoneDistribution.medium.vehicles["Base.pzkTrailerBoxDual"] = {index =  2, spawnChance = 3};
	VehicleZoneDistribution.medium.vehicles["Base.pzkF350BoxUmoveit"] = {index =  2, spawnChance = 3};

	VehicleZoneDistribution.medium.vehicles["Base.pzkDashNavajoP"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.medium.vehicles["Base.pzkDashNavajoW"] = {index = -1, spawnChance = 4};

	VehicleZoneDistribution.medium.vehicles["Base.pzkFranklinStallionKing2"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.medium.vehicles["Base.pzkFranklinStallionKing3"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.medium.vehicles["Base.pzkFranklinStallionKing4"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.medium.vehicles["Base.pzkFranklinStallionKing5"] = {index = -1, spawnChance = 3};


	VehicleZoneDistribution.medium.vehicles["Base.pzkChevalierRookie"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.medium.vehicles["Base.pzkAutowagenBunny"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.medium.vehicles["Base.pzkMastersonRice"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.medium.vehicles["Base.pzkDash600"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.medium.vehicles["Base.pzkDashCirilla"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.medium.vehicles["Base.pzkDashDecade"] = {index = -1, spawnChance = 5};


	--VehicleZoneDistribution.good.vehicles["Base.ModernCar"] = {index = -1, spawnChance = 2};
	--VehicleZoneDistribution.good.vehicles["Base.ModernCar02"] = {index = -1, spawnChance = 2};

	VehicleZoneDistribution.good.vehicles["Base.pzkVanCamper"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.good.vehicles["Base.pzkMinivanStellaris"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.good.vehicles["Base.pzkVanBrig"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.good.vehicles["Base.pzkChevalierMaroca"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.good.vehicles["Base.pzkFranklinGalloper"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.good.vehicles["Base.pzkPickupFranklin"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.good.vehicles["Base.pzkDashDeluxo"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.good.vehicles["Base.pzkStepVanMilk"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.good.vehicles["Base.pzkLimo"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.good.vehicles["Base.pzkHMMV"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.good.vehicles["Base.pzkHMMV2"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.good.vehicles["Base.pzkHMMV3"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.good.vehicles["Base.pzkPickUpTruck93"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.good.vehicles["Base.pzkStepVanUPZ"] = {index = -1, spawnChance = 1};

	VehicleZoneDistribution.good.vehicles["Base.pzkStepVanIceCream"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.good.vehicles["Base.pzkStepVanPizza"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.good.vehicles["Base.pzkFranklinTriumph"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.good.vehicles["Base.pzkFranklinTriumphTaxi"] = {index = -1, spawnChance = 5};

	VehicleZoneDistribution.good.vehicles["Base.pzkDashRoyalGrand"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.good.vehicles["Base.pzkMastersonLady"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.good.vehicles["Base.pzkDashPhoenix"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.good.vehicles["Base.pzkDashPhoenixBandit"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.good.vehicles["Base.pzkChevalierF6"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.good.vehicles["Base.pzkChevalierRoadrunner"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.good.vehicles["Base.pzkMastersonInitial"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.good.vehicles["Base.pzkContinentalSpirit"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.good.vehicles["Base.pzkDashNoble"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.good.vehicles["Base.pzkFranklinStallion"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.good.vehicles["Base.pzkFranklinStallion2"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.good.vehicles["Base.pzkFranklinStallionSport"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.good.vehicles["Base.pzkFranklinIslander"] = {index = -1, spawnChance = 15};

	VehicleZoneDistribution.good.vehicles["Base.pzkFranklinBankTruck"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.good.vehicles["Base.pzkFranklinTruckRV"] = {index = -1, spawnChance = 5};


	VehicleZoneDistribution.good.vehicles["Base.pzkMinivanMPV"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.good.vehicles["Base.pzkMinivanT3C"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.good.vehicles["Base.pzkMinivanTask"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.good.vehicles["Base.pzkMinivan2"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.good.vehicles["Base.pzkMinivanPrev"] = {index = -1, spawnChance = 5};

	VehicleZoneDistribution.good.vehicles["Base.pzkMastersonScout4D"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.good.vehicles["Base.pzkChevalierLaserOffroader"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.good.vehicles["Base.pzkMerciaLangBerg"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.good.vehicles["Base.pzkDashCheyene"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.good.vehicles["Base.pzkContinentalCruiser"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.good.vehicles["Base.pzkChevalierProvince"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.good.vehicles["Base.pzkChevalierProvinceLong"] = {index = -1, spawnChance = 10};

	VehicleZoneDistribution.good.vehicles["Base.pzkContinentalBayer330Sport"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.good.vehicles["Base.pzkContinentalBayer3304D"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.good.vehicles["Base.pzkContinentalBayer3302D"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.good.vehicles["Base.pzkContinentalBayer534"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.good.vehicles["Base.pzkContinentalBayer732"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.good.vehicles["Base.pzkMerciaLang1240"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.good.vehicles["Base.pzkMerciaLang12402D"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.good.vehicles["Base.pzkMerciaLang4000Cabrio"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.good.vehicles["Base.pzkContinentalBayer330Cabrio"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.good.vehicles["Base.pzkDashRancherCabrio"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.good.vehicles["Base.pzkChevalierCosetteCabrio"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.good.vehicles["Base.pzkMastersonSunrise"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.good.vehicles["Base.pzkMastersonSensation"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.good.vehicles["Base.pzkMastersonExpander"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.good.vehicles["Base.pzkContinentalPfeiffer901"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.good.vehicles["Base.pzkContinentalPfeiffer901c"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.good.vehicles["Base.pzkContinentalPfeiffer930"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.good.vehicles["Base.pzkContinentalPfeiffer930c"] = {index = -1, spawnChance = 1};

	VehicleZoneDistribution.good.vehicles["Base.pzkContinentalPyrenean310"] = {index = -1, spawnChance = 1};

	VehicleZoneDistribution.good.vehicles["Base.pzkChevalierCeriseLimo"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.good.vehicles["Base.pzkDashHellionLimo"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.good.vehicles["Base.pzkDashRapierLimo"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.good.vehicles["Base.pzkFranklinHomelanderLimo"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.good.vehicles["Base.pzkFranklinTriumphTWD91"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.good.vehicles["Base.pzkTriumphTWDStationWagon"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.good.vehicles["Base.pzkTriumphTWDStationWagonTaxi"] = {index = -1, spawnChance = 15};

	VehicleZoneDistribution.good.vehicles["Base.pzkDashNavajoP"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.good.vehicles["Base.pzkDashNavajoW"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.good.vehicles["Base.pzkMastersonApex4D"] = {index = -1, spawnChance = 4};



--	VehicleZoneDistribution.junkyard.vehicles["Base.CarNormal"] = {index = -1, spawnChance = 2};
--	VehicleZoneDistribution.junkyard.vehicles["Base.CarStationWagon"] = {index = -1, spawnChance = 1};
--	VehicleZoneDistribution.junkyard.vehicles["Base.CarStationWagon2"] = {index = -1, spawnChance = 1};
--	VehicleZoneDistribution.junkyard.vehicles["Base.CarTaxi"] = {index = -1, spawnChance = 1};
--	VehicleZoneDistribution.junkyard.vehicles["Base.CarTaxi2"] = {index = -1, spawnChance = 1};
--	VehicleZoneDistribution.junkyard.vehicles["Base.SmallCar"] = {index = -1, spawnChance = 1};
--	VehicleZoneDistribution.junkyard.vehicles["Base.SmallCar02"] = {index = -1, spawnChance = 1};
--	VehicleZoneDistribution.junkyard.vehicles["Base.ModernCar"] = {index = -1, spawnChance = 1};
--	VehicleZoneDistribution.junkyard.vehicles["Base.ModernCar02"] = {index = -1, spawnChance = 1};

	VehicleZoneDistribution.junkyard.vehicles["Base.pzkVanCamper"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkMinivanStellaris"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkVanBrig"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkChevalierMaroca"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDashOhio"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinGalloper"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkPickupFranklin"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkStepVanMilk"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkHMMV"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkHMMV2"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkHMMV3"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkPickUpTruck93"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkCarMuscle"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkVanBox"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkStepVanUPZ"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinPony"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkStepVanIceCream"] = {index = -1, spawnChance = 1};    
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkStepVanPizza"] = {index = -1, spawnChance = 1};       
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinTriumph"] = {index = -1, spawnChance = 15};   
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinTriumphTaxi"] = {index = -1, spawnChance = 5};      
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkPickUpTruckWoodboarded"] = {index = -1, spawnChance = 5}; 

	VehicleZoneDistribution.junkyard.vehicles["Base.pzkChevalierCeriseSedan"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkChevalierCeriseSedanTaxi"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDashHellion"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDashHellionTaxi"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDashMayor"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDashMayorTaxi"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDashRapier"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinTriumphTWD"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinTriumphTWDTaxi"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkCeriseStationWagon"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDashMayorStationWagon"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkRapierStationWagon"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkTriumphTWDStationWagon"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkTriumphTWDStationWagonTaxi"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkVanSeatsTaxi"] = {index = -1, spawnChance = 5};

	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDashRoyal"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkChevalierDownhill"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDashTornado"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkMastersonLady"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDashPhoenix"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkChevalierE6"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkChevalierF6"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkChevalierRoadrunner"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkMastersonInitial"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkMastersonXSR"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDashNoble"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinStallion"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinStallion2"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinIslander"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinHomelander"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkMastersonCrown"] = {index = -1, spawnChance = 15};

	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinBankTruck"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinTruckRV"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinTruckBed"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinTruckShort"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinTruckCab"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinTruckBus"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinTruckBox"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinTruckDump"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinTruckFlatbed"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinTruckGarbage"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinTruckPropane"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinTruckPropane2"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinTruckTankerFossoil"] = {index = 0, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinTruckTankerSeptic"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinTruckTankerWater"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinTruckTow"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinTruckUtility"] = {index = -1, spawnChance = 10};

	VehicleZoneDistribution.junkyard.vehicles["Base.pzkMinivanC22"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkMinivanChev"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkMinivanConvoy"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkMinivanMPV"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkMinivanT3"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkMinivanT3C"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkMinivanTask"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkMinivan2"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkMinivanPrev"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkVanMultivan"] = {index = -1, spawnChance = 5};

	VehicleZoneDistribution.junkyard.vehicles["Base.pzkMastersonScout4D"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkChevalierPickupCrewLong"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkChevalierPickupCrewMedium"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkChevalierProvince"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkChevalierProvinceLong"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkChevalierLaserModern"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkChevalierE6Van"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkChevalierF6Van"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkChevalierLaserOffroader"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFtypeTowTruck"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkChevalierTowTruck"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkMerciaLangBerg"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDashCheyene"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkContinentalCruiser"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklin350FWagonLong"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklin350FPickupCrewLong"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklin250FPickupWagonLong"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklin250FPickupCrewLong"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklin250FWagonLong"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklin150van"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklin150FPickupReg"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklin150FWagonMedium"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklin150FPickupMedium"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDashIntruder250WagonLong"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDashIntruder250PickupLong"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDashIntruder150short"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDashIntruder150RegVan"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkChevalierLaserCUCV"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkChevalierProvinceLongCUCV"] = {index = -1, spawnChance = 1};

	VehicleZoneDistribution.junkyard.vehicles["Base.pzkMastersonLadyZ"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDashRunnerGeneral"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDashGTA"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinStallionKing"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDashRunner"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDashPiranha"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDashChampion"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDashPhoenix80"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkChevalierMaroca80"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkContinentalBug"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkContinentalBugHerbie"] = {index = -1, spawnChance = 1};

	VehicleZoneDistribution.junkyard.vehicles["Base.pzkContinentalBayer330Sport"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkContinentalBayer3304D"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkContinentalBayer3302D"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkContinentalBayer534"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkContinentalBayer732"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkMerciaLang1240"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkMerciaLang12402D"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkMerciaLang4000Cabrio"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkContinentalBayer330Cabrio"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkCarMuscleCabrio"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDashRancherMail"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDashRancherCabrio"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkChevalierCosetteCabrio"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkMastersonSunrise"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkMastersonSensation"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkMastersonExpander"] = {index = -1, spawnChance = 4};

	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinTruckSemi"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFreightlinerFlat2"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFreightlinerFlat"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkPeterbuiltSleeper"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkPeterbuiltSleeperBandit"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkPeterbuilt"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkTrailerRegularCourtains"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkTrailerRegularCourtainsBandit"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkTrailerRegularFlatbed"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkTrailerRegularTanker"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkTrailerHorseBox"] = {index = -1, spawnChance = 4};

	VehicleZoneDistribution.junkyard.vehicles["Base.pzkStepVanHotDog"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkContinentalPfeiffer901"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkContinentalPfeiffer901c"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkContinentalPfeiffer930"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkContinentalPfeiffer930c"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkTrailerCamping"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkTrailerTankSmall"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkTrailerBoxDual"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkTrailerTankMedium"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDualTrailerCover"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDualTrailer"] = {index = -1, spawnChance = 1};

	VehicleZoneDistribution.junkyard.vehicles["Base.pzkMastersonHarmonyWagon"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkMastersonHarmony"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkContinentalNord"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkContinentalNordWagon"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkTriumphTWDStationWagonGriswold"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkTransitBus"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDashHEMTT6x6semi"] = {index = -1, spawnChance = 1};

	VehicleZoneDistribution.junkyard.vehicles["Base.pzkChevalierVan70"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDashVan70"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinVan70"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkChevalierCeriseLimo"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDashHellionLimo"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDashRapierLimo"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinHomelanderLimo"] = {index = -1, spawnChance = 1};

	VehicleZoneDistribution.junkyard.vehicles["Base.pzkStepVanTacoVan"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinGalloperPrimal"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkChevalierLaserK5"] = {index = -1, spawnChance = 4};

	VehicleZoneDistribution.junkyard.vehicles["Base.pzkF350BoxUmoveit"] = {index =  2, spawnChance = 2};

	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDashNavajoP"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDashNavajoW"] = {index = -1, spawnChance = 3};

	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinStallionKing2"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinStallionKing3"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinStallionKing4"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFranklinStallionKing5"] = {index = -1, spawnChance = 3};
	
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFreightlinerTerminatorTow"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDashPhoenix75"] = {index = -1, spawnChance = 5};
	
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkChevalierLaserK5	"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkF150Utility"] = {index = -1, spawnChance = 3};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkF150BoxFlatbed"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkCVanCargo"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkCVan"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkCVanMultivan"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkCVan6Seats"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkCVanCargo3"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkCVan3"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkCVanMultivan3"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkCVan6Seats3"] = {index = -1, spawnChance = 4};
	
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDVanCargo"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDVan"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDVanMultivan"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDVan6Seats"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDVanCargo2"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDVan2"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDVanMultivan2"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDVan6Seats2"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDVanCargo3"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDVan3"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDVanMultivan3"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkDVan6Seats3"] = {index = -1, spawnChance = 4};
	
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFVanCargo"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFVan"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFVanMultivan"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkFVan6Seats"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.junkyard.vehicles["Base.pzkChevalierD100Pickup"] = {index = -1, spawnChance = 4};

	



	VehicleZoneDistribution.sport.vehicles["Base.CarLuxury"] = {index = -1, spawnChance = 20};
	VehicleZoneDistribution.sport.vehicles["Base.SportsCar"] = {index = -1, spawnChance = 20};
	VehicleZoneDistribution.sport.vehicles["Base.pzkChevalierMaroca"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.sport.vehicles["Base.pzkDashDeluxo"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.sport.vehicles["Base.pzkLimo"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.sport.vehicles["Base.pzkHMMV"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.sport.vehicles["Base.pzkHMMV2"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.sport.vehicles["Base.pzkHMMV3"] = {index = -1, spawnChance = 5};

	VehicleZoneDistribution.sport.vehicles["Base.pzkDashRoyalGrand"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.sport.vehicles["Base.pzkDashPhoenix"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.sport.vehicles["Base.pzkDashPhoenixBandit"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.sport.vehicles["Base.pzkChevalierRoadrunner"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.sport.vehicles["Base.pzkMastersonInitial"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.sport.vehicles["Base.pzkContinentalSpirit"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.sport.vehicles["Base.pzkDashNoble"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.sport.vehicles["Base.pzkFranklinStallionSport"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.sport.vehicles["Base.pzkFranklinIslander"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.sport.vehicles["Base.pzkMerciaLangBerg"] = {index = -1, spawnChance = 2};

	VehicleZoneDistribution.sport.vehicles["Base.pzkContinentalBayer330Sport"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.sport.vehicles["Base.pzkMerciaLang4000Cabrio"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.sport.vehicles["Base.pzkContinentalBayer330Cabrio"] = {index = -1, spawnChance = 4};
	VehicleZoneDistribution.sport.vehicles["Base.pzkChevalierCosetteCabrio"] = {index = -1, spawnChance = 7};

	VehicleZoneDistribution.sport.vehicles["Base.pzkContinentalPfeiffer901"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.sport.vehicles["Base.pzkContinentalPfeiffer901c"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.sport.vehicles["Base.pzkContinentalPfeiffer930"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.sport.vehicles["Base.pzkContinentalPfeiffer930c"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.sport.vehicles["Base.pzkContinentalPyrenean310"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.sport.vehicles["Base.pzkMastersonRotaryC"] = {index = -1, spawnChance = 3};

	VehicleZoneDistribution.sport.vehicles["Base.pzkMastersonInitialFuji"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.sport.vehicles["Base.pzkMastersonRotaryCRyosuke"] = {index = -1, spawnChance = 1};
	
	VehicleZoneDistribution.sport.vehicles["Base.pzkMastersonTR2Kouki"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.sport.vehicles["Base.pzkMastersonTR2Zenki"] = {index = -1, spawnChance = 1};


 	VehicleZoneDistribution.ambulance.vehicles["Base.VanAmbulance"] = {index = -1, spawnChance = 50};
	VehicleZoneDistribution.ambulance.vehicles["Base.pzkVanBoxAmbulance"] = {index = -1, spawnChance = 50};

	VehicleZoneDistribution.fire.vehicles["Base.PickUpVanLightsFire"] = {index = -1, spawnChance = 35};
	VehicleZoneDistribution.fire.vehicles["Base.PickUpTruckLightsFire"] = {index = -1, spawnChance = 35};
	VehicleZoneDistribution.fire.vehicles["Base.pzkVanBoxFiretruck"] = {index = -1, spawnChance = 30};
	VehicleZoneDistribution.fire.vehicles["Base.pzkFranklinTriumphTWDFire"] = {index = -1, spawnChance = 30};
	VehicleZoneDistribution.fire.vehicles["Base.pzkChevalierCeriseSedanFire"] = {index = -1, spawnChance = 30};
	VehicleZoneDistribution.fire.vehicles["Base.pzkFranklinGalloperFire"] = {index = -1, spawnChance = 30};

	VehicleZoneDistribution.fire.vehicles["Base.pzkFranklinTruckFire"] = {index = -1, spawnChance = 25};
	VehicleZoneDistribution.fire.vehicles["Base.pzkFranklinTruckFireTanker"] = {index = -1, spawnChance = 25};
	VehicleZoneDistribution.fire.vehicles["Base.pzkChevalierLaserFire"] = {index = -1, spawnChance = 30};
	VehicleZoneDistribution.fire.vehicles["Base.pzkChevalierTowTruckFire"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.fire.vehicles["Base.pzkFireTruckFlatPumper"] = {index = -1, spawnChance = 25};
	VehicleZoneDistribution.fire.vehicles["Base.pzkFireTruckFlatLadder"] = {index = -1, spawnChance = 25};
	VehicleZoneDistribution.fire.vehicles["Base.pzkFireTruckFlatSemi"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.fire.vehicles["Base.pzkTrailerRegularFTSemi"] = {index = -1, spawnChance = 5};

	VehicleZoneDistribution.fire.vehicles["Base.pzkChevalierCerise93Fire"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.fire.vehicles["Base.pzkChevalierCerise93WagonFire"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.fire.vehicles["Base.pzkF150UtilityFire"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.fire.vehicles["Base.pzkSuvFire"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.fire.vehicles["Base.pzkPickupFranklinFire"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.fire.vehicles["Base.pzkDashMayorFire"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.fire.vehicles["Base.pzkChevalierNyalaFire"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.fire.vehicles["Base.pzkFranklinTriumphTWD91Fire"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.fire.vehicles["Base.pzkDashCheyeneFire"] = {index = -1, spawnChance = 5};

	VehicleZoneDistribution.ranger.vehicles["Base.CarLights"] = {index = 0, spawnChance = 20};
	VehicleZoneDistribution.ranger.vehicles["Base.PickUpVanLights"] = {index = 0, spawnChance = 10};
	VehicleZoneDistribution.ranger.vehicles["Base.PickUpTruckLights"] = {index = 0, spawnChance = 10};
	VehicleZoneDistribution.ranger.vehicles["Base.pzkDashOhio"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.ranger.vehicles["Base.pzkFranklinGalloperRanger"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.ranger.vehicles["Base.pzkChevalierLaserRanger"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.ranger.vehicles["Base.pzkDashRancherRanger"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.ranger.vehicles["Base.pzkF150UtilityRanger"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.ranger.vehicles["Base.pzkPickupFranklinRanger"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.ranger.vehicles["Base.pzkSuvRanger"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.ranger.vehicles["Base.pzkChevalierCeriseSedanRanger"] = {index = 0, spawnChance = 10};
	VehicleZoneDistribution.ranger.vehicles["Base.pzkDashMayorRanger"] = {index = 0, spawnChance = 10};
	VehicleZoneDistribution.ranger.vehicles["Base.pzkFranklinTriumphTWDRanger"] = {index = 0, spawnChance = 10};
	VehicleZoneDistribution.ranger.vehicles["Base.pzkFranklinTriumphTWD91Ranger"] = {index = 0, spawnChance = 10};
	VehicleZoneDistribution.ranger.vehicles["Base.pzkDashCheyeneRanger"] = {index = 0, spawnChance = 10};


--	VehicleZoneDistribution.police.vehicles["Base.PickUpVanLightsPolice"] = {index = 0, spawnChance = 20};
--	VehicleZoneDistribution.police.vehicles["Base.CarLightsPolice"] = {index = 0, spawnChance = 25};
--	VehicleZoneDistribution.police.vehicles["Base.pzkStepVanSwat"] = {index = -1, spawnChance = 5};
--	VehicleZoneDistribution.police.vehicles["Base.pzkVanPolice"] = {index = -1, spawnChance = 15};
--	VehicleZoneDistribution.police.vehicles["Base.pzkFranklinGalloperPolice"] = {index = -1, spawnChance = 20};
--	VehicleZoneDistribution.police.vehicles["Base.pzkHMMV2Mil"] = {index = -1, spawnChance = 1};
--	VehicleZoneDistribution.police.vehicles["Base.pzkHMMV3Mil"] = {index = -1, spawnChance = 1};
--	VehicleZoneDistribution.police.vehicles["Base.pzkFranklinTriumphPolice"] = {index = -1, spawnChance = 20};
--	VehicleZoneDistribution.police.vehicles["Base.pzkDashMayorPolice"] = {index = -1, spawnChance = 10};
--	VehicleZoneDistribution.police.vehicles["Base.pzkChevalierCeriseSedanPolice"] = {index = -1, spawnChance = 10};
--	VehicleZoneDistribution.police.vehicles["Base.pzkFranklinTriumphTWDPolice"] = {index = -1, spawnChance = 10};
--	VehicleZoneDistribution.police.vehicles["Base.pzkFranklinStallionPolice"] = {index = -1, spawnChance = 10};

--	VehicleZoneDistribution.police.vehicles["Base.pzkFranklinSwatTruck"] = {index = -1, spawnChance = 5};
--	VehicleZoneDistribution.police.vehicles["Base.pzkChevalierTowTruckPolice"] = {index = -1, spawnChance = 5};
--	VehicleZoneDistribution.police.vehicles["Base.pzkChevalierLaserPolice"] = {index = -1, spawnChance = 20};
--	VehicleZoneDistribution.police.vehicles["Base.pzkTrailerBoxPoliceDual"] = {index = -1, spawnChance = 10};
--	VehicleZoneDistribution.police.vehicles["Base.pzkChevalierCerise93Police"] = {index = -1, spawnChance = 10};

	--	lsu Security (PZK)
	VehicleZoneDistribution.lsu = VehicleZoneDistribution.lsu or {};
	VehicleZoneDistribution.lsu.vehicles = VehicleZoneDistribution.lsu.vehicles or {};
	VehicleZoneDistribution.lsu.vehicles["Base.pzkFranklinTriumphTWDLSU"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.lsu.vehicles["Base.pzkChevalierCeriseSedanLSU"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.lsu.vehicles["Base.pzkChevalierNyalaLSU"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.lsu.vehicles["Base.pzkDashMayorLSU"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.lsu.vehicles["Base.pzkFranklinTriumphTWD91LSU"] = {index = -1, spawnChance = 10};	
	VehicleZoneDistribution.lsu.vehicles["Base.pzkChevalierLaserLSU"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.lsu.vehicles["Base.pzkFranklinGalloperLSU"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.lsu.vehicles["Base.pzkChevalierLaserLSU"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.lsu.vehicles["Base.pzkFranklinGalloperLSU"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.lsu.vehicles["Base.pzkFranklinTriumphWagonLSU"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.lsu.vehicles["Base.pzkFranklinTriumphLSU"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.lsu.vehicles["Base.pzkChevalierCerise93LSU"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.lsu.vehicles["Base.pzkChevalierCerise93WagonLSU"] = {index = -1, spawnChance = 10};

	--	mall Security (PZK)
	VehicleZoneDistribution.mall = VehicleZoneDistribution.mall or {};
	VehicleZoneDistribution.mall.vehicles = VehicleZoneDistribution.mall.vehicles or {};
	VehicleZoneDistribution.mall.vehicles["Base.pzkFranklinTriumphTWDMall"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.mall.vehicles["Base.pzkChevalierCeriseSedanMall"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.mall.vehicles["Base.pzkChevalierNyalaMall"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.mall.vehicles["Base.pzkDashMayorMall"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.mall.vehicles["Base.pzkFranklinTriumphTWD91Mall"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.mall.vehicles["Base.pzkFranklinGalloperMall"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.mall.vehicles["Base.pzkChevalierLaserMall"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.mall.vehicles["Base.pzkChevalierLaserMall"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.mall.vehicles["Base.pzkFranklinGalloperMall"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.mall.vehicles["Base.pzkFranklinTriumphWagonMall"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.mall.vehicles["Base.pzkFranklinTriumphMall"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.mall.vehicles["Base.pzkChevalierCerise93Mall"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.mall.vehicles["Base.pzkChevalierCerise93WagonMall"] = {index = -1, spawnChance = 10};


	VehicleZoneDistribution.prison.vehicles["Base.pzkFranklinTruckBusPrison"] = {index = -1, spawnChance = 10};


	VehicleZoneDistribution.mccoy.vehicles["Base.pzkVanMcCoy"] = {index = -1, spawnChance = 40};
	VehicleZoneDistribution.mccoy.vehicles["Base.pzkFranklinTruckMcCoy"] = {index = -1, spawnChance = 30};

	VehicleZoneDistribution.postal.vehicles["Base.pzkStepVanUPZ"] = {index = -1, spawnChance = 20};
	VehicleZoneDistribution.postal.vehicles["Base.pzkMinivanStellarisMail"] = {index = -1, spawnChance = 20};
	VehicleZoneDistribution.postal.vehicles["Base.pzkDashRancherMail"] = {index = -1, spawnChance = 20};
	VehicleZoneDistribution.postal.vehicles["Base.pzkStepVanFedLog"] = {index = -1, spawnChance = 20};

	VehicleZoneDistribution.airportshuttle.vehicles["Base.pzkFranklinTruckBusAirport"] = {index = -1, spawnChance = 15};

	VehicleZoneDistribution.airportservice.vehicles["Base.pzkDashCheyeneAirportSecurity"] = {index = -1, spawnChance = 20};
	VehicleZoneDistribution.airportservice.vehicles["Base.pzkSuvAirportSecurity"] = {index = -1, spawnChance = 20};


	VehicleZoneDistribution.lectromax.vehicles["Base.pzkFranklinTruckBoxLectromax"] = {index = -1, spawnChance = 50};
	

	VehicleZoneDistribution.spiffo.vehicles["Base.pzkFVanSpiffo"] = {index = -1, spawnChance = 20};
	VehicleZoneDistribution.spiffo.vehicles["Base.pzkDVanSpiffo"] = {index = -1, spawnChance = 20};
	VehicleZoneDistribution.spiffo.vehicles["Base.pzkDVan2Spiffo"] = {index = -1, spawnChance = 20};
	VehicleZoneDistribution.spiffo.vehicles["Base.pzkDVan3Spiffo"] = {index = -1, spawnChance = 20};

	VehicleZoneDistribution.business.vehicles["Base.pzkVanBox"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.business.vehicles["Base.pzkFranklinTruckBed"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.business.vehicles["Base.pzkFranklinTruckShort"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.business.vehicles["Base.pzkFranklinTruckCab"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.business.vehicles["Base.pzkFranklinTruckBox"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.business.vehicles["Base.pzkFranklinTruckFlatbed"] = {index = -1, spawnChance = 1};
	--VehicleZoneDistribution.business.vehicles["Base.pzkTrailerRegularCourtains"] = {index = -1, spawnChance = 0.5};
	--VehicleZoneDistribution.business.vehicles["Base.pzkTrailerRegularFlatbed"] = {index = -1, spawnChance = 0.5};
	--VehicleZoneDistribution.business.vehicles["Base.pzkTrailerRegularTanker"] = {index = -1, spawnChance = 0.5};
	VehicleZoneDistribution.business.vehicles["Base.pzkFreightlinerFlat2"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.business.vehicles["Base.pzkFreightlinerFlat"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.business.vehicles["Base.pzkPeterbuiltSleeper"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.business.vehicles["Base.pzkPeterbuiltSleeperBandit"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.business.vehicles["Base.pzkPeterbuilt"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.business.vehicles["Base.pzkFranklinTruckSemi"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.business.vehicles["Base.pzkDashNational980"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.business.vehicles["Base.pzkVanBrig"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.business.vehicles["Base.pzkFranklinTruckUtility"] = {index = 1, spawnChance = 1};
	VehicleZoneDistribution.business.vehicles["Base.pzkVanMultivan"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.business.vehicles["Base.pzkFranklinTruckTow"] = {index = -1, spawnChance = 0.5};
	VehicleZoneDistribution.business.vehicles["Base.pzkFranklinTruckUtility"] = {index = -1, spawnChance = 0.5};
	VehicleZoneDistribution.business.vehicles["Base.pzkFtypeTowTruck"] = {index = -1, spawnChance = 0.5};
	VehicleZoneDistribution.business.vehicles["Base.pzkChevalierTowTruck"] = {index = -1, spawnChance = 0.5};
	VehicleZoneDistribution.business.vehicles["Base.pzkStepVanUPZ"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.business.vehicles["Base.pzkMinivanStellarisMail"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.business.vehicles["Base.pzkDashRancherMail"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.business.vehicles["Base.pzkStepVanFedLog"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.business.vehicles["Base.pzkF150UtilityMoore"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.business.vehicles["Base.pzkF150UtilityNewCoalfieldMechanic"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.business.vehicles["Base.pzkF150UtilityIrvingtonSpeedway"] = {index = -1, spawnChance = 1};

	VehicleZoneDistribution.business.vehicles["Base.pzkDash150UtilityMoore"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.business.vehicles["Base.pzkDash150UtilityNewCoalfieldMechanic"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.business.vehicles["Base.pzkDash150UtilityIrvingtonSpeedway"] = {index = -1, spawnChance = 1};

	VehicleZoneDistribution.business.vehicles["Base.pzkChevalier150UtilityMoore"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.business.vehicles["Base.pzkChevalier150UtilityNewCoalfieldMechanic"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.business.vehicles["Base.pzkChevalier150UtilityIrvingtonSpeedway"] = {index = -1, spawnChance = 1};

	local pzktrafficjamVehicles = {};
	pzktrafficjamVehicles["Base.CarNormal"] = {index = -1, spawnChance = 2};
	pzktrafficjamVehicles["Base.CarTaxi"] = {index = -1, spawnChance = 1};
	pzktrafficjamVehicles["Base.CarTaxi2"] = {index = -1, spawnChance = 1};
	pzktrafficjamVehicles["Base.CarStationWagon"] = {index = -1, spawnChance = 1};
	pzktrafficjamVehicles["Base.CarStationWagon2"] = {index = -1, spawnChance = 1};


	pzktrafficjamVehicles["Base.pzkChevalierCeriseSedan"] = {index = -1, spawnChance = 15};
	pzktrafficjamVehicles["Base.pzkChevalierCeriseSedanTaxi"] = {index = -1, spawnChance = 5};
	pzktrafficjamVehicles["Base.pzkDashHellion"] = {index = -1, spawnChance = 15};
	pzktrafficjamVehicles["Base.pzkDashHellionTaxi"] = {index = -1, spawnChance = 5};
	pzktrafficjamVehicles["Base.pzkDashMayor"] = {index = -1, spawnChance = 15};
	pzktrafficjamVehicles["Base.pzkDashMayorTaxi"] = {index = -1, spawnChance = 5};
	pzktrafficjamVehicles["Base.pzkDashRapier"] = {index = -1, spawnChance = 15};
	pzktrafficjamVehicles["Base.pzkFranklinTriumphTWD"] = {index = -1, spawnChance = 15};
	pzktrafficjamVehicles["Base.pzkFranklinTriumphTWDTaxi"] = {index = -1, spawnChance = 5};
	pzktrafficjamVehicles["Base.pzkCeriseStationWagon"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkDashMayorStationWagon"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkRapierStationWagon"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkTriumphTWDStationWagon"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkTriumphTWDStationWagonTaxi"] = {index = -1, spawnChance = 5};
	pzktrafficjamVehicles["Base.pzkVanSeatsTaxi"] = {index = -1, spawnChance = 5};

	pzktrafficjamVehicles["Base.pzkDashRoyal"] = {index = -1, spawnChance = 15};
	pzktrafficjamVehicles["Base.pzkChevalierDownhill"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkDashTornado"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkMastersonLady"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkChevalierE6"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkChevalierF6"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkChevalierRoadrunner"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkMastersonXSR"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkFranklinStallion"] = {index = -1, spawnChance = 15};
	pzktrafficjamVehicles["Base.pzkFranklinStallion2"] = {index = -1, spawnChance = 15};
	pzktrafficjamVehicles["Base.pzkFranklinIslander"] = {index = -1, spawnChance = 15};
	pzktrafficjamVehicles["Base.pzkFranklinHomelander"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkMastersonCrown"] = {index = -1, spawnChance = 15};

	pzktrafficjamVehicles["Base.pzkFranklinBankTruck"] = {index = -1, spawnChance = 5};
	pzktrafficjamVehicles["Base.pzkFranklinTruckRV"] = {index = -1, spawnChance = 5};
	pzktrafficjamVehicles["Base.pzkFranklinTruckBed"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkFranklinTruckShort"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkFranklinTruckCab"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkFranklinTruckBus"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkFranklinTruckBox"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkFranklinTruckDump"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkFranklinTruckFlatbed"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkFranklinTruckGarbage"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkFranklinTruckPropane"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkFranklinTruckPropane2"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkFranklinTruckTankerFossoil"] = {index = 0, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkFranklinTruckTankerSeptic"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkFranklinTruckTankerWater"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkFranklinTruckTow"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkFranklinTruckUtility"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkMinivanC22"] = {index = -1, spawnChance = 15};
	pzktrafficjamVehicles["Base.pzkMinivanChev"] = {index = -1, spawnChance = 15};
	pzktrafficjamVehicles["Base.pzkMinivanConvoy"] = {index = -1, spawnChance = 15};
	pzktrafficjamVehicles["Base.pzkMinivanMPV"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkMinivanStellarisTaxi"] = {index = -1, spawnChance = 5};
	pzktrafficjamVehicles["Base.pzkMinivanT3"] = {index = -1, spawnChance = 5};
	pzktrafficjamVehicles["Base.pzkMinivanT3C"] = {index = -1, spawnChance = 5};
	pzktrafficjamVehicles["Base.pzkMinivanTask"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkMinivan2"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkMinivanPrev"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkVanMultivan"] = {index = -1, spawnChance = 5};

	pzktrafficjamVehicles["Base.pzkMastersonScout4D"] = {index = -1, spawnChance = 1};
	pzktrafficjamVehicles["Base.pzkChevalierPickupCrewLong"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkChevalierPickupCrewMedium"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkChevalierProvince"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkChevalierProvinceLong"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkChevalierLaserModern"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkChevalierE6Van"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkChevalierF6Van"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkChevalierLaserOffroader"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkFtypeTowTruck"] = {index = -1, spawnChance = 2};
	pzktrafficjamVehicles["Base.pzkChevalierTowTruck"] = {index = -1, spawnChance = 2};
	pzktrafficjamVehicles["Base.pzkMerciaLangBerg"] = {index = -1, spawnChance = 1};
	pzktrafficjamVehicles["Base.pzkDashCheyene"] = {index = -1, spawnChance = 1};
	pzktrafficjamVehicles["Base.pzkContinentalCruiser"] = {index = -1, spawnChance = 5};
	pzktrafficjamVehicles["Base.pzkFranklin350FWagonLong"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkFranklin350FPickupCrewLong"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkFranklin250FPickupWagonLong"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkFranklin250FPickupCrewLong"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkFranklin250FWagonLong"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkFranklin150van"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkFranklin150FPickupReg"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkFranklin150FWagonMedium"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkFranklin150FPickupMedium"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkDashIntruder250WagonLong"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkDashIntruder250PickupLong"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkDashIntruder150short"] = {index = -1, spawnChance = 10};
	pzktrafficjamVehicles["Base.pzkDashIntruder150RegVan"] = {index = -1, spawnChance = 10};

	pzktrafficjamVehicles["Base.pzkMastersonLadyZ"] = {index = -1, spawnChance = 5};
	pzktrafficjamVehicles["Base.pzkDashRunnerGeneral"] = {index = -1, spawnChance = 1};
	pzktrafficjamVehicles["Base.pzkDashGTA"] = {index = -1, spawnChance = 5};
	pzktrafficjamVehicles["Base.pzkFranklinStallionKing"] = {index = -1, spawnChance = 5};
	pzktrafficjamVehicles["Base.pzkDashRunner"] = {index = -1, spawnChance = 5};
	pzktrafficjamVehicles["Base.pzkDashPiranha"] = {index = -1, spawnChance = 5};
	pzktrafficjamVehicles["Base.pzkDashChampion"] = {index = -1, spawnChance = 5};
	pzktrafficjamVehicles["Base.pzkDashPhoenix80"] = {index = -1, spawnChance = 5};
	pzktrafficjamVehicles["Base.pzkChevalierMaroca80"] = {index = -1, spawnChance = 5};
	pzktrafficjamVehicles["Base.pzkContinentalBug"] = {index = -1, spawnChance = 5};

	pzktrafficjamVehicles["Base.pzkMastersonHarmonyWagon"] = {index = -1, spawnChance = 5};
	pzktrafficjamVehicles["Base.pzkMastersonHarmony"] = {index = -1, spawnChance = 5};
	pzktrafficjamVehicles["Base.pzkContinentalNord"] = {index = -1, spawnChance = 5};
	pzktrafficjamVehicles["Base.pzkContinentalNordWagon"] = {index = -1, spawnChance = 5};
	pzktrafficjamVehicles["Base.pzkTransitBus"] = {index = -1, spawnChance = 5};
	pzktrafficjamVehicles["Base.pzkDashHEMTT6x6semi"] = {index = -1, spawnChance = 5};

	pzktrafficjamVehicles["Base.pzkFranklinGalloperPrimal"] = {index = -1, spawnChance = 4};
	pzktrafficjamVehicles["Base.pzkChevalierLaserK5"] = {index = -1, spawnChance = 4};

	pzktrafficjamVehicles["Base.pzkDashNavajoP"] = {index = -1, spawnChance = 3};
	pzktrafficjamVehicles["Base.pzkDashNavajoW"] = {index = -1, spawnChance = 3};
	pzktrafficjamVehicles["Base.pzkChevalierLaserK5"] = {index = -1, spawnChance = 10};
	
	pzktrafficjamVehicles["Base.pzkTrailerRegularCourtains"] = {index = -1, spawnChance = 1};
	pzktrafficjamVehicles["Base.pzkTrailerRegularCourtainsBandit"] = {index = -1, spawnChance = 1};
	pzktrafficjamVehicles["Base.pzkTrailerRegularFlatbed"] = {index = -1, spawnChance = 1};
	pzktrafficjamVehicles["Base.pzkTrailerRegularTanker"] = {index = -1, spawnChance = 1};	
	pzktrafficjamVehicles["Base.pzkTrailerRegularWaterTankerTainted"] = {index = -1, spawnChance = 1};
	pzktrafficjamVehicles["Base.pzkTrailerRegularWaterTanker"] = {index = -1, spawnChance = 1};
	pzktrafficjamVehicles["Base.pzkTrailerRegularFuelTanker"] = {index = -1, spawnChance = 1};
	pzktrafficjamVehicles["Base.pzkTrailerRegularCourtainsWhite"] = {index = -1, spawnChance = 1};
	pzktrafficjamVehicles["Base.pzkTrailerRegularContainer"] = {index = -1, spawnChance = 1};
	pzktrafficjamVehicles["Base.pzkTrailerRegularFedLog"] = {index = -1, spawnChance = 1};
	pzktrafficjamVehicles["Base.pzkTrailerRegularGigamart"] = {index = -1, spawnChance = 1};
	pzktrafficjamVehicles["Base.pzkTrailerRegularPharmahung"] = {index = -1, spawnChance = 1};
	pzktrafficjamVehicles["Base.pzkTrailerRegularValutech"] = {index = -1, spawnChance = 1};
	pzktrafficjamVehicles["Base.pzkTrailerRegularUStoreIt"] = {index = -1, spawnChance = 1};
	pzktrafficjamVehicles["Base.pzkTrailerRegularCourtainsKnight"] = {index = -1, spawnChance = 1};
	pzktrafficjamVehicles["Base.pzkTrailerRegularSpiffo"] = {index = -1, spawnChance = 1};
	pzktrafficjamVehicles["Base.pzkTrailerRegularPop"] = {index = -1, spawnChance = 1};
	pzktrafficjamVehicles["Base.pzkTrailerRegularPropaneTanker"] = {index = -1, spawnChance = 1};
	pzktrafficjamVehicles["Base.pzkChevalierD100Pickup"] = {index = -1, spawnChance = 6};

	VehicleZoneDistribution.trafficjamw.vehicles = pzktrafficjamVehicles;
	VehicleZoneDistribution.trafficjame.vehicles = pzktrafficjamVehicles;
	VehicleZoneDistribution.trafficjamn.vehicles = pzktrafficjamVehicles;
	VehicleZoneDistribution.trafficjams.vehicles = pzktrafficjamVehicles;
	
	
	VehicleZoneDistribution.rtrafficjamw = VehicleZoneDistribution.rtrafficjamw or {};
	VehicleZoneDistribution.rtrafficjamw.vehicles = VehicleZoneDistribution.rtrafficjamw.vehicles or {};
	
	VehicleZoneDistribution.rtrafficjame = VehicleZoneDistribution.rtrafficjame or {};
	VehicleZoneDistribution.rtrafficjame.vehicles = VehicleZoneDistribution.rtrafficjame.vehicles or {};
	
	VehicleZoneDistribution.rtrafficjamn = VehicleZoneDistribution.rtrafficjamn or {};
	VehicleZoneDistribution.rtrafficjamn.vehicles = VehicleZoneDistribution.rtrafficjamn.vehicles or {};
	
	VehicleZoneDistribution.rtrafficjams = VehicleZoneDistribution.rtrafficjams or {};
	VehicleZoneDistribution.rtrafficjams.vehicles = VehicleZoneDistribution.rtrafficjams.vehicles or {};
	
	VehicleZoneDistribution.rtrafficjamw.vehicles = pzktrafficjamVehicles;
	VehicleZoneDistribution.rtrafficjame.vehicles = pzktrafficjamVehicles;
	VehicleZoneDistribution.rtrafficjamn.vehicles = pzktrafficjamVehicles;
	VehicleZoneDistribution.rtrafficjams.vehicles = pzktrafficjamVehicles;


--	VehicleZoneDistribution.military = VehicleZoneDistribution.army;

	VehicleZoneDistribution.military = VehicleZoneDistribution.military or {};
	VehicleZoneDistribution.military.vehicles = VehicleZoneDistribution.military.vehicles or {};

	--	MILITARY
	VehicleZoneDistribution.military.vehicles["Base.pzkDashOhio"] = {index = -1, spawnChance = 20};
	VehicleZoneDistribution.military.vehicles["Base.pzkHMMV2Mil"] = {index = -1, spawnChance = 30};
	VehicleZoneDistribution.military.vehicles["Base.pzkHMMV3Mil"] = {index = -1, spawnChance = 30};
	VehicleZoneDistribution.military.vehicles["Base.pzkHMMV4Mil"] = {index = -1, spawnChance = 30};
	VehicleZoneDistribution.military.vehicles["Base.pzkHMMV5Mil"] = {index = -1, spawnChance = 30};
	VehicleZoneDistribution.military.vehicles["Base.pzkHMMV6Mil"] = {index = -1, spawnChance = 30};
	VehicleZoneDistribution.military.vehicles["Base.pzkFranklinTruckMil"] = {index = -1, spawnChance = 30};
	VehicleZoneDistribution.military.vehicles["Base.pzkFranklinTruckBusArmy"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.military.vehicles["Base.pzkChevalierLaserCUCV"] = {index = 1, spawnChance = 30};
	VehicleZoneDistribution.military.vehicles["Base.pzkChevalierProvinceLongCUCV"] = {index = 1, spawnChance = 30};
	VehicleZoneDistribution.military.vehicles["Base.pzkContinentalTRK"] = {index = -1, spawnChance = 30};
	VehicleZoneDistribution.military.vehicles["Base.pzkTrailerArmyCover"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.military.vehicles["Base.pzkFranklinTruckTankerMil"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.military.vehicles["Base.pzkF350BoxCUCV"] = {index = -1, spawnChance = 30};
	
	VehicleZoneDistribution.military.vehicles["Base.pzkTrailerRegularWaterTankerArmy"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.military.vehicles["Base.pzkTrailerRegularFuelTankerArmy"] = {index = -1, spawnChance = 10};
	
	VehicleZoneDistribution.military.vehicles["Base.pzkTruckDashW35BedMil"] = {index = -1, spawnChance = 40};
	VehicleZoneDistribution.military.vehicles["Base.pzkTruckDashW35CabrioMil"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.military.vehicles["Base.pzkTruckDashW35WaterMil"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.military.vehicles["Base.pzkTruckDashW35FuelMil"] = {index = -1, spawnChance = 15};

--	VehicleZoneDistribution.military.baseVehicleQuality = VehicleZoneDistribution.military.baseVehicleQuality or 1;
--	VehicleZoneDistribution.military.chanceToSpawnSpecial = VehicleZoneDistribution.military.chanceToSpawnSpecial or 0;
--	VehicleZoneDistribution.military.spawnRate = VehicleZoneDistribution.military.spawnRate or 25;

	VehicleZoneDistribution.militarycar = VehicleZoneDistribution.militarycar or {};
	VehicleZoneDistribution.militarycar.vehicles = VehicleZoneDistribution.militarycar.vehicles or {};
	--	MILITARY CAR
	VehicleZoneDistribution.militarycar.vehicles["Base.pzkDashOhio"] = {index = -1, spawnChance = 20};
	VehicleZoneDistribution.militarycar.vehicles["Base.pzkHMMV2Mil"] = {index = -1, spawnChance = 30};
	VehicleZoneDistribution.militarycar.vehicles["Base.pzkHMMV3Mil"] = {index = -1, spawnChance = 30};
	VehicleZoneDistribution.militarycar.vehicles["Base.pzkHMMV4Mil"] = {index = -1, spawnChance = 30};
	VehicleZoneDistribution.militarycar.vehicles["Base.pzkHMMV5Mil"] = {index = -1, spawnChance = 30};
	VehicleZoneDistribution.militarycar.vehicles["Base.pzkFranklinTruckMil"] = {index = -1, spawnChance = 25};
	VehicleZoneDistribution.militarycar.vehicles["Base.pzkFranklinTruckMilTankerWater"] = {index = -1, spawnChance = 25};
	VehicleZoneDistribution.militarycar.vehicles["Base.pzkFranklinTruckTankerFossoil"] = {index = 1, spawnChance = 20};
	VehicleZoneDistribution.militarycar.vehicles["Base.pzkFranklinTruckBusArmy"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.militarycar.vehicles["Base.pzkChevalierLaserCUCV"] = {index = 1, spawnChance = 30};
	VehicleZoneDistribution.militarycar.vehicles["Base.pzkChevalierProvinceLongCUCV"] = {index = 1, spawnChance = 30};
	VehicleZoneDistribution.militarycar.vehicles["Base.pzkContinentalTRK"] = {index = 1, spawnChance = 30};
	VehicleZoneDistribution.militarycar.vehicles["Base.pzkTrailerArmyCover"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.militarycar.vehicles["Base.pzkDashHEMTT6x6semi"] = {index = -1, spawnChance = 15};
	
	--	MILITARY CAR March Ridge Spawns
	VehicleZoneDistribution.armymr = VehicleZoneDistribution.armymr or {}	
	VehicleZoneDistribution.armymr.vehicles = VehicleZoneDistribution.armymr.vehicles or {}
	
	VehicleZoneDistribution.armymr.vehicles["Base.pzkDashOhio"] = {index = -1, spawnChance = 20};
	VehicleZoneDistribution.armymr.vehicles["Base.pzkHMMV2Mil"] = {index = -1, spawnChance = 30};
	VehicleZoneDistribution.armymr.vehicles["Base.pzkHMMV3Mil"] = {index = -1, spawnChance = 30};
	VehicleZoneDistribution.armymr.vehicles["Base.pzkHMMV4Mil"] = {index = -1, spawnChance = 30};
	VehicleZoneDistribution.armymr.vehicles["Base.pzkHMMV5Mil"] = {index = -1, spawnChance = 30};
	VehicleZoneDistribution.armymr.vehicles["Base.pzkHMMV6Mil"] = {index = -1, spawnChance = 30};
	VehicleZoneDistribution.armymr.vehicles["Base.pzkFranklinTruckMil"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.armymr.vehicles["Base.pzkFranklinTruckMilTankerWater"] = {index = -1, spawnChance = 5};
	VehicleZoneDistribution.armymr.vehicles["Base.pzkFranklinTruckTankerFossoil"] = {index = 1, spawnChance = 5};
	VehicleZoneDistribution.armymr.vehicles["Base.pzkFranklinTruckBusArmy"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.armymr.vehicles["Base.pzkChevalierLaserCUCV"] = {index = 1, spawnChance = 30};
	VehicleZoneDistribution.armymr.vehicles["Base.pzkChevalierProvinceLongCUCV"] = {index = 1, spawnChance = 30};
	VehicleZoneDistribution.armymr.vehicles["Base.pzkContinentalTRK"] = {index = 1, spawnChance = 10};
	VehicleZoneDistribution.armymr.vehicles["Base.pzkDashHEMTT6x6semi"] = {index = -1, spawnChance = 5};

--	VehicleZoneDistribution.militarycar.baseVehicleQuality = VehicleZoneDistribution.militarycar.baseVehicleQuality or 1;
--	VehicleZoneDistribution.militarycar.chanceToSpawnSpecial = VehicleZoneDistribution.militarycar.chanceToSpawnSpecial or 0;
--	VehicleZoneDistribution.militarycar.spawnRate = VehicleZoneDistribution.militarycar.spawnRate or 25;

	VehicleZoneDistribution.militaryfuel = VehicleZoneDistribution.militaryfuel or {};
	VehicleZoneDistribution.militaryfuel.vehicles = VehicleZoneDistribution.militaryfuel.vehicles or {};
	--	MILITARY FUEL
	VehicleZoneDistribution.militaryfuel.vehicles["Base.pzkFranklinTruckTankerMil"] = {index = -1, spawnChance = 100};

--	VehicleZoneDistribution.militaryfuel.baseVehicleQuality = VehicleZoneDistribution.militaryfuel.baseVehicleQuality or 1;
--	VehicleZoneDistribution.militaryfuel.chanceToSpawnSpecial = VehicleZoneDistribution.militaryfuel.chanceToSpawnSpecial or 0;
--	VehicleZoneDistribution.militaryfuel.spawnRate = VehicleZoneDistribution.militaryfuel.spawnRate or 25;

	VehicleZoneDistribution.farm = VehicleZoneDistribution.farm or {};
	VehicleZoneDistribution.farm.vehicles = VehicleZoneDistribution.farm.vehicles or {};
	--	FARM
	VehicleZoneDistribution.farm.vehicles["Base.pzkPickUpTruckWoodboarded"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.farm.vehicles["Base.pzkChevalierE6"] = {index = -1, spawnChance = 15};
	VehicleZoneDistribution.farm.vehicles["Base.pzkChevalierF6"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.farm.vehicles["Base.pzkChevalierRoadrunner"] = {index = -1, spawnChance = 20};
	VehicleZoneDistribution.farm.vehicles["Base.pzkFranklinTruckShort"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.farm.vehicles["Base.pzkFranklinTruckCab"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.farm.vehicles["Base.pzkFranklinTruckBed"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.farm.vehicles["Base.pzkFranklinTruckFlatbed"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.farm.vehicles["Base.pzkVanMultivan"] = {index = -1, spawnChance = 5};

	VehicleZoneDistribution.farm.vehicles["Base.pzkMastersonLadyZ"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.farm.vehicles["Base.pzkDashRunnerGeneral"] = {index = -1, spawnChance = 1};
	VehicleZoneDistribution.farm.vehicles["Base.pzkDashGTA"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.farm.vehicles["Base.pzkFranklinStallionKing"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.farm.vehicles["Base.pzkDashRunner"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.farm.vehicles["Base.pzkDashPiranha"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.farm.vehicles["Base.pzkDashChampion"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.farm.vehicles["Base.pzkDashPhoenix80"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.farm.vehicles["Base.pzkChevalierMaroca80"] = {index = -1, spawnChance = 2};
	VehicleZoneDistribution.farm.vehicles["Base.pzkTrailerHorseBox"] = {index = -1, spawnChance = 10};
	VehicleZoneDistribution.farm.vehicles["Base.pzkTractor"] = {index = -1, spawnChance = 35};
	VehicleZoneDistribution.farm.vehicles["Base.pzkTractor2"] = {index = -1, spawnChance = 45};
	VehicleZoneDistribution.farm.vehicles["Base.pzkTractor3"] = {index = -1, spawnChance = 35};
	VehicleZoneDistribution.farm.vehicles["Base.pzkTrailerTankSprayer"] = {index = -1, spawnChance = 20};
	VehicleZoneDistribution.farm.vehicles["Base.pzkF150BoxFlatbed"] = {index = -1, spawnChance = 7};
	VehicleZoneDistribution.farm.vehicles["Base.pzkDash150BoxFlatbed"] = {index = -1, spawnChance = 7};
	VehicleZoneDistribution.farm.vehicles["Base.pzkChevalier150BoxFlatbed"] = {index = -1, spawnChance = 7};
	
	VehicleZoneDistribution.farm.vehicles["Base.pzkChevalierD100Pickup"] = {index = -1, spawnChance = 15};

--	VehicleZoneDistribution.farm.baseVehicleQuality = VehicleZoneDistribution.farm.baseVehicleQuality or 0.8;
--	VehicleZoneDistribution.farm.chanceToSpawnSpecial = VehicleZoneDistribution.farm.chanceToSpawnSpecial or 0;
--	VehicleZoneDistribution.farm.chanceToPartDamage = VehicleZoneDistribution.farm.chanceToPartDamage or 20;
--	VehicleZoneDistribution.farm.spawnRate = VehicleZoneDistribution.farm.spawnRate or 25;

----------------------------------------------------    FHQ EXTENDED VEHICLE ZONES -----------------------------------------------------
	if getActivatedMods():contains("fhqExpVehSpawn") and VehicleZoneDistribution then    

		VehicleZoneDistribution.recreational = VehicleZoneDistribution.recreational or {};
		VehicleZoneDistribution.recreational.vehicles = VehicleZoneDistribution.recreational.vehicles or {};
		--	RECREATIONAL
		VehicleZoneDistribution.recreational.vehicles["Base.pzkVanCamper"] = {index = -1, spawnChance = 30};
		VehicleZoneDistribution.recreational.vehicles["Base.pzkFranklinTruckRV"] = {index = -1, spawnChance = 30};
		VehicleZoneDistribution.recreational.vehicles["Base.pzkMinivanT3C"] = {index = -1, spawnChance = 30};
		VehicleZoneDistribution.recreational.vehicles["Base.pzkTrailerCamping"] = {index = -1, spawnChance = 30};

		---COLLECTORS
		-- Rare spawn zones with a high chance for rare cars. Cars here should be exotics, and especially rare vintage cars. High end cars can also be included, but at a lower spawn rate than exotics---
		--	VehicleZoneDistribution.collectors.vehicles[""] = {index = -1, spawnChance = 25};

		--	VehicleZoneDistribution.exotic.vehicles[""] = {index = -1, spawnChance = 25};

		---Barn Find: Rare spawn zone usually near barns. Prioritize classics, especially rare ones, here. Cars in this list will almost be wrecked and may be undrivable, requiring towing. High key chance
		--	VehicleZoneDistribution.barnfind.vehicles[""] = {index = -1, spawnChance = 25};

		--	EXPOCARSHOWS
		---Expo Car Show: These zones will be near expo centers, or other places where there may be large car shows. Here you will find exotics, race cars, and vintage classics---
		VehicleZoneDistribution.expocarshow = VehicleZoneDistribution.expocarshow or {};
		VehicleZoneDistribution.expocarshow.vehicles = VehicleZoneDistribution.expocarshow.vehicles or {};
		VehicleZoneDistribution.expocarshow.vehicles["Base.pzkDashDeluxo"] = {index = -1, spawnChance = 15};
		VehicleZoneDistribution.expocarshow.vehicles["Base.pzkContinentalSpirit"] = {index = -1, spawnChance = 15};
		VehicleZoneDistribution.expocarshow.vehicles["Base.pzkDashPhoenixBandit"] = {index = -1, spawnChance = 15};
		VehicleZoneDistribution.expocarshow.vehicles["Base.pzkContinentalBugHerbie"] = {index = -1, spawnChance = 15};
		VehicleZoneDistribution.expocarshow.vehicles["Base.pzkDashRunnerGeneral"] = {index = -1, spawnChance = 15};

		---Special Dealer: Specialty car dealer zone. This should contain exotics and imports mostly, as well as restored classics. Possibility for a race/concept car as a display car. ---
		--	VehicleZoneDistribution.specialdealer.vehicles[""] = {index = -1, spawnChance = 10};

		--	VehicleZoneDistribution.newdealer.vehicles[""] = {index = -1, spawnChance = 10};

		--	COMMERCIAL
		VehicleZoneDistribution.commercial = VehicleZoneDistribution.commercial or {};
		VehicleZoneDistribution.commercial.vehicles = VehicleZoneDistribution.commercial.vehicles or {};
		VehicleZoneDistribution.commercial.vehicles["Base.pzkVanBox"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.commercial.vehicles["Base.pzkFranklinTruckBed"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.commercial.vehicles["Base.pzkFranklinTruckShort"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.commercial.vehicles["Base.pzkFranklinTruckCab"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.commercial.vehicles["Base.pzkFranklinTruckBox"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.commercial.vehicles["Base.pzkFranklinTruckFlatbed"] = {index = -1, spawnChance = 10};

		--	UTILITY
		VehicleZoneDistribution.utility = VehicleZoneDistribution.utility or {};
		VehicleZoneDistribution.utility.vehicles = VehicleZoneDistribution.utility.vehicles or {};
		VehicleZoneDistribution.utility.vehicles["Base.pzkFranklinTruckPropane"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.utility.vehicles["Base.pzkFranklinTruckPropane2"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.utility.vehicles["Base.pzkFranklinTruckTankerFossoil"] = {index = 0, spawnChance = 10};
		VehicleZoneDistribution.utility.vehicles["Base.pzkFranklinTruckTankerSeptic"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.utility.vehicles["Base.pzkFranklinTruckTankerWater"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.utility.vehicles["Base.pzkFranklinTruckUtility"] = {index = -1, spawnChance = 10};

		--	COMMERCIAL LARGE
		VehicleZoneDistribution.commerciallarge = VehicleZoneDistribution.commerciallarge or {};
		VehicleZoneDistribution.commerciallarge.vehicles = VehicleZoneDistribution.commerciallarge.vehicles or {};
		VehicleZoneDistribution.commerciallarge.vehicles["Base.pzkVanBox"] = {index = -1, spawnChance = 30};
		VehicleZoneDistribution.commerciallarge.vehicles["Base.pzkFranklinTruckBed"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.commerciallarge.vehicles["Base.pzkFranklinTruckBox"] = {index = -1, spawnChance = 60};
		VehicleZoneDistribution.commerciallarge.vehicles["Base.pzkTrailerRegularCourtains"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.commerciallarge.vehicles["Base.pzkTrailerRegularCourtainsBandit"] = {index = -1, spawnChance = 5};
		VehicleZoneDistribution.commerciallarge.vehicles["Base.pzkTrailerRegularFlatbed"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.commerciallarge.vehicles["Base.pzkTrailerRegularTanker"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.commerciallarge.vehicles["Base.pzkFreightlinerFlat2"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.commerciallarge.vehicles["Base.pzkFreightlinerFlat"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.commerciallarge.vehicles["Base.pzkPeterbuiltSleeper"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.commerciallarge.vehicles["Base.pzkPeterbuiltSleeperBandit"] = {index = -1, spawnChance = 5};
		VehicleZoneDistribution.commerciallarge.vehicles["Base.pzkPeterbuilt"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.commerciallarge.vehicles["Base.pzkFranklinTruckSemi"] = {index = -1, spawnChance = 10};




		--	VehicleZoneDistribution.trailerhuge.vehicles[""] = {index = -1, spawnChance = 8};

		---      AMATEUR MECHANIC
		VehicleZoneDistribution.amateurmechanic = VehicleZoneDistribution.amateurmechanic or {};
		VehicleZoneDistribution.amateurmechanic.vehicles = VehicleZoneDistribution.amateurmechanic.vehicles or {};
		VehicleZoneDistribution.amateurmechanic.vehicles["Base.pzkChevalierMaroca"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.amateurmechanic.vehicles["Base.pzkCarMuscle"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.amateurmechanic.vehicles["Base.pzkDashPhoenix"] = {index = -1, spawnChance = 15};
		VehicleZoneDistribution.amateurmechanic.vehicles["Base.pzkFranklinStallionSport"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.amateurmechanic.vehicles["Base.pzkChevalierRoadrunner"] = {index = -1, spawnChance = 10};

		--	TOW SERVICE
		VehicleZoneDistribution.towservice = VehicleZoneDistribution.towservice or {};
		VehicleZoneDistribution.towservice.vehicles = VehicleZoneDistribution.towservice.vehicles or {};
		VehicleZoneDistribution.towservice.vehicles["Base.pzkFranklinTruckTow"] = {index = -1, spawnChance = 20};
		VehicleZoneDistribution.towservice.vehicles["Base.pzkFranklinTruckUtility"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.towservice.vehicles["Base.pzkFtypeTowTruck"] = {index = -1, spawnChance = 20};
		VehicleZoneDistribution.towservice.vehicles["Base.pzkChevalierTowTruck"] = {index = -1, spawnChance = 20};
		VehicleZoneDistribution.towservice.vehicles["Base.pzkFreightlinerTerminatorTow"] = {index = -1, spawnChance = 1};
		VehicleZoneDistribution.towservice.vehicles["Base.pzkTruckD70Tow"] = {index = -1, spawnChance = 20};
		VehicleZoneDistribution.towservice.vehicles["Base.pzkTruckD70Tow2"] = {index = -1, spawnChance = 20};



		---Import: Cars unavailable in the US, but available elsewhere. Near shipping areas, such as docks, railyards, and airports.---
		--	IMPORT
		VehicleZoneDistribution.import = VehicleZoneDistribution.import or {};
		VehicleZoneDistribution.import.vehicles = VehicleZoneDistribution.import.vehicles or {};
		VehicleZoneDistribution.import.vehicles["Base.pzkContinentalSpirit"] = {index = -1, spawnChance = 30};
		VehicleZoneDistribution.import.vehicles["Base.pzkMastersonInitial"] = {index = -1, spawnChance = 30};
		VehicleZoneDistribution.import.vehicles["Base.pzkMastersonLady"] = {index = -1, spawnChance = 30};
		VehicleZoneDistribution.import.vehicles["Base.pzkContinentalPyrenean310"] = {index = -1, spawnChance = 30};

		---USED DEALER: Zone for used car dealers. Should contain older cars, but newer cars can also be included as well. Higher key chance, lower condition.---
		VehicleZoneDistribution.useddealer = VehicleZoneDistribution.useddealer or {};
		VehicleZoneDistribution.useddealer.vehicles = VehicleZoneDistribution.useddealer.vehicles or {};
		VehicleZoneDistribution.useddealer.vehicles["Base.pzkMinivanChev"] = {index = -1, spawnChance = 20};
		VehicleZoneDistribution.useddealer.vehicles["Base.pzkMinivanConvoy"] = {index = -1, spawnChance = 20};
		VehicleZoneDistribution.useddealer.vehicles["Base.pzkVanMultivan"] = {index = -1, spawnChance = 5};
		VehicleZoneDistribution.useddealer.vehicles["Base.pzkStepVanHotDog"] = {index = -1, spawnChance = 5};
		VehicleZoneDistribution.useddealer.vehicles["Base.pzkStepVanCatfish"] = {index = -1, spawnChance = 3};

		--	DRIFT
		VehicleZoneDistribution.drift = VehicleZoneDistribution.drift or {};
		VehicleZoneDistribution.drift.vehicles = VehicleZoneDistribution.drift.vehicles or {};
		VehicleZoneDistribution.drift.vehicles["Base.pzkMastersonInitial"] = {index = -1, spawnChance = 15};
		VehicleZoneDistribution.drift.vehicles["Base.pzkFranklinStallionSport"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.drift.vehicles["Base.pzkContinentalBayer330Sport"] = {index = -1, spawnChance = 10};


		--	VehicleZoneDistribution.trailerutility.vehicles[""] = {index = -1, spawnChance = 10};

		--  RACING - Racing: Race cars and other track-based vehicles should go here.---
		VehicleZoneDistribution.racing = VehicleZoneDistribution.racing or {};
		VehicleZoneDistribution.racing.vehicles = VehicleZoneDistribution.racing.vehicles or {};
		VehicleZoneDistribution.racing.vehicles["Base.pzkChevalierMaroca"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.racing.vehicles["Base.pzkDashRoyalGrand"] = {index = -1, spawnChance = 4};
		VehicleZoneDistribution.racing.vehicles["Base.pzkDashPhoenix"] = {index = -1, spawnChance = 15};
		VehicleZoneDistribution.racing.vehicles["Base.pzkDashPhoenixBandit"] = {index = -1, spawnChance = 2};
		VehicleZoneDistribution.racing.vehicles["Base.pzkChevalierRoadrunner"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.racing.vehicles["Base.pzkMastersonInitial"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.racing.vehicles["Base.pzkContinentalSpirit"] = {index = -1, spawnChance = 5};
		VehicleZoneDistribution.racing.vehicles["Base.pzkDashNoble"] = {index = -1, spawnChance = 15};
		VehicleZoneDistribution.racing.vehicles["Base.pzkFranklinStallionSport"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.racing.vehicles["Base.pzkFranklinIslander"] = {index = -1, spawnChance = 5};
		VehicleZoneDistribution.racing.vehicles["Base.pzkContinentalBayer330Sport"] = {index = -1, spawnChance = 5};
		VehicleZoneDistribution.racing.vehicles["Base.pzkContinentalHammermanKnight"] = {index = -1, spawnChance = 2};

		--	VehicleZoneDistribution.movers.vehicles[""] = {index = -1, spawnChance = 10};

		--	CONSTRUCTION
		VehicleZoneDistribution.construction = VehicleZoneDistribution.construction or {};
		VehicleZoneDistribution.construction.vehicles = VehicleZoneDistribution.construction.vehicles or {};
		VehicleZoneDistribution.construction.vehicles["Base.pzkVanBrig"] = {index = -1, spawnChance = 20};
		VehicleZoneDistribution.construction.vehicles["Base.pzkVanBox"] = {index = 0, spawnChance = 20};
		VehicleZoneDistribution.construction.vehicles["Base.pzkVanBox"] = {index = 1, spawnChance = 20};
		VehicleZoneDistribution.construction.vehicles["Base.pzkFranklinTruckBed"] = {index = 1, spawnChance = 30};
		VehicleZoneDistribution.construction.vehicles["Base.pzkFranklinTruckShort"] = {index = 1, spawnChance = 30};
		VehicleZoneDistribution.construction.vehicles["Base.pzkFranklinTruckCab"] = {index = 1, spawnChance = 20};
		VehicleZoneDistribution.construction.vehicles["Base.pzkFranklinTruckUtility"] = {index = 1, spawnChance = 30};
		VehicleZoneDistribution.construction.vehicles["Base.pzkFranklinTruckFlatbed"] = {index = 1, spawnChance = 30};
		VehicleZoneDistribution.construction.vehicles["Base.pzkVanMultivan"] = {index = -1, spawnChance = 5};

		-- WASTE
		VehicleZoneDistribution.wasteservice = VehicleZoneDistribution.wasteservice or {};
		VehicleZoneDistribution.wasteservice.vehicles = VehicleZoneDistribution.wasteservice.vehicles or {};
		VehicleZoneDistribution.wasteservice.vehicles["Base.pzkFranklinTruckTankerSeptic"] = {index = -1, spawnChance = 40};
		VehicleZoneDistribution.wasteservice.vehicles["Base.pzkFranklinTruckGarbage"] = {index = -1, spawnChance = 50};
		VehicleZoneDistribution.wasteservice.vehicles["Base.pzkFranklinTruckTankerWater"] = {index = -1, spawnChance = 10};


		--	VehicleZoneDistribution.pacecar.vehicles[""] = {index = -1, spawnChance = 10};
		--	VehicleZoneDistribution.heistvehicle.vehicles[""] = {index = -1, spawnChance = 10};

		--	POLICE LARGE
		VehicleZoneDistribution.policelarge = VehicleZoneDistribution.policelarge or {};
	--		VehicleZoneDistribution.policelarge.vehicles = VehicleZoneDistribution.policelarge.vehicles or {};
	--		VehicleZoneDistribution.policelarge.vehicles["Base.pzkStepVanSwat"] = {index = -1, spawnChance = 10};
	--		VehicleZoneDistribution.policelarge.vehicles["Base.pzkVanPolice"] = {index = -1, spawnChance = 20};
	--		VehicleZoneDistribution.policelarge.vehicles["Base.pzkFranklinTruckBusPrison"] = {index = -1, spawnChance = 10};
	--		VehicleZoneDistribution.policelarge.vehicles["Base.pzkFranklinSwatTruck"] = {index = -1, spawnChance = 30};


		--	POLICE ONLY
		VehicleZoneDistribution.policeonly = VehicleZoneDistribution.policeonly or {};
		VehicleZoneDistribution.policeonly.vehicles = VehicleZoneDistribution.policeonly.vehicles or {};
	--		VehicleZoneDistribution.policeonly.vehicles["Base.PickUpVanLightsPolice"] = {index = 0, spawnChance = 20};
	--		VehicleZoneDistribution.policeonly.vehicles["Base.CarLightsPolice"] = {index = 0, spawnChance = 10};
	--		VehicleZoneDistribution.policeonly.vehicles["Base.pzkStepVanSwat"] = {index = -1, spawnChance = 5};
	--		VehicleZoneDistribution.policeonly.vehicles["Base.pzkVanPolice"] = {index = -1, spawnChance = 6};
	--		VehicleZoneDistribution.policeonly.vehicles["Base.pzkFranklinGalloperPolice"] = {index = -1, spawnChance = 20};
	--		VehicleZoneDistribution.policeonly.vehicles["Base.pzkFranklinTriumphPolice"] = {index = -1, spawnChance = 10};
	--		VehicleZoneDistribution.policeonly.vehicles["Base.pzkDashMayorPolice"] = {index = -1, spawnChance = 10};
	--		VehicleZoneDistribution.policeonly.vehicles["Base.pzkChevalierCeriseSedanPolice"] = {index = -1, spawnChance = 10};
	--		VehicleZoneDistribution.policeonly.vehicles["Base.pzkFranklinTriumphTWDPolice"] = {index = -1, spawnChance = 10};
	--		VehicleZoneDistribution.policeonly.vehicles["Base.pzkFranklinStallionPolice"] = {index = -1, spawnChance = 10};
	--		VehicleZoneDistribution.policeonly.vehicles["Base.pzkFranklinTruckBusPrison"] = {index = -1, spawnChance = 20};
	--		VehicleZoneDistribution.policeonly.vehicles["Base.pzkFranklinSwatTruck"] = {index = -1, spawnChance = 30};
	--		VehicleZoneDistribution.policeonly.vehicles["Base.pzkChevalierTowTruckPolice"] = {index = -1, spawnChance = 30};
	--		VehicleZoneDistribution.policeonly.vehicles["Base.pzkChevalierLaserPolice"] = {index = -1, spawnChance = 30};
	--		VehicleZoneDistribution.policeonly.vehicles["Base.pzkTrailerBoxPoliceDual"] = {index = -1, spawnChance = 5};
	--		VehicleZoneDistribution.policeonly.vehicles["Base.pzkChevalierCerise93Police"] = {index = -1, spawnChance = 30};


		--	AMBULANCE ONLY
		VehicleZoneDistribution.ambulanceonly = VehicleZoneDistribution.ambulanceonly or {};
		VehicleZoneDistribution.ambulanceonly.vehicles = VehicleZoneDistribution.ambulanceonly.vehicles or {};
		VehicleZoneDistribution.ambulanceonly.vehicles["Base.pzkVanBoxAmbulance"] = {index = -1, spawnChance = 35};

		--	FIRE ONLY
		VehicleZoneDistribution.fireonly = VehicleZoneDistribution.fireonly or {};
		VehicleZoneDistribution.fireonly.vehicles = VehicleZoneDistribution.fireonly.vehicles or {};
		VehicleZoneDistribution.fireonly.vehicles["Base.pzkVanBoxFiretruck"] = {index = -1, spawnChance = 30};
		VehicleZoneDistribution.fireonly.vehicles["Base.pzkFranklinTriumphTWDFire"] = {index = -1, spawnChance = 30};
		VehicleZoneDistribution.fireonly.vehicles["Base.pzkChevalierCeriseSedanFire"] = {index = -1, spawnChance = 30};
		VehicleZoneDistribution.fireonly.vehicles["Base.pzkFranklinGalloperFire"] = {index = -1, spawnChance = 30};
		VehicleZoneDistribution.fireonly.vehicles["Base.pzkFranklinTruckFire"] = {index = -1, spawnChance = 30};
		VehicleZoneDistribution.fireonly.vehicles["Base.pzkFranklinTruckFireTanker"] = {index = -1, spawnChance = 30};
		VehicleZoneDistribution.fireonly.vehicles["Base.pzkChevalierLaserFire"] = {index = -1, spawnChance = 30};
		VehicleZoneDistribution.fireonly.vehicles["Base.pzkChevalierTowTruckFire"] = {index = -1, spawnChance = 30};
		VehicleZoneDistribution.fireonly.vehicles["Base.pzkFireTruckFlatPumper"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.fireonly.vehicles["Base.pzkFireTruckFlatLadder"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.fireonly.vehicles["Base.pzkFireTruckFlatSemi"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.fireonly.vehicles["Base.pzkTrailerRegularFTSemi"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.fireonly.vehicles["Base.pzkChevalierCerise93Fire"] = {index = -1, spawnChance = 20};
		VehicleZoneDistribution.fireonly.vehicles["Base.pzkChevalierCerise93WagonFire"] = {index = -1, spawnChance = 20};

		--	FIRE LARGE
		VehicleZoneDistribution.firelarge = VehicleZoneDistribution.firelarge or {};
		VehicleZoneDistribution.firelarge.vehicles = VehicleZoneDistribution.firelarge.vehicles or {};
		VehicleZoneDistribution.firelarge.vehicles["Base.pzkVanBoxFiretruck"] = {index = -1, spawnChance = 30};
		VehicleZoneDistribution.firelarge.vehicles["Base.pzkFranklinTruckFire"] = {index = -1, spawnChance = 30};
		VehicleZoneDistribution.firelarge.vehicles["Base.pzkFranklinTruckFireTanker"] = {index = -1, spawnChance = 30};
		VehicleZoneDistribution.firelarge.vehicles["Base.pzkFireTruckFlatPumper"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.firelarge.vehicles["Base.pzkFireTruckFlatLadder"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.firelarge.vehicles["Base.pzkFireTruckFlatSemi"] = {index = -1, spawnChance = 10};
		VehicleZoneDistribution.firelarge.vehicles["Base.pzkTrailerRegularFTSemi"] = {index = -1, spawnChance = 10};

		--	MCOYONLY
		VehicleZoneDistribution.mccoyonly = VehicleZoneDistribution.mccoyonly or {};
		VehicleZoneDistribution.mccoyonly.vehicles = VehicleZoneDistribution.mccoyonly.vehicles or {};
		VehicleZoneDistribution.mccoyonly.vehicles["Base.pzkVanMcCoy"] = {index = -1, spawnChance = 10};

		--	POSTAL LARGE
		VehicleZoneDistribution.postallarge = VehicleZoneDistribution.postallarge or {};
		VehicleZoneDistribution.postallarge.vehicles = VehicleZoneDistribution.postallarge.vehicles or {};
		VehicleZoneDistribution.postallarge.vehicles["Base.pzkStepVanUPZ"] = {index = -1, spawnChance = 10};

		--	VehicleZoneDistribution.vehicletiny.vehicles[""] = {index = -1, spawnChance = 10};
		--	VehicleZoneDistribution.mower.vehicles[""] = {index = -1, spawnChance = 10};
		--	VehicleZoneDistribution.trailertiny.vehicles[""] = {index = -1, spawnChance = 10};

		--	HOT PURSUIT - Interceptors
		VehicleZoneDistribution.hotpursuit = VehicleZoneDistribution.hotpursuit or {};
		VehicleZoneDistribution.hotpursuit.vehicles = VehicleZoneDistribution.hotpursuit.vehicles or {};
	--		VehicleZoneDistribution.hotpursuit.vehicles["Base.pzkFranklinTriumphPolice"] = {index = -1, spawnChance = 20};
	--		VehicleZoneDistribution.hotpursuit.vehicles["Base.pzkFranklinStallionPolice"] = {index = -1, spawnChance = 25};

	end
end