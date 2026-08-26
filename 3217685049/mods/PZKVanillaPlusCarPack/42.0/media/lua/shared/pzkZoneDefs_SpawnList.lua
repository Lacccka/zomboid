---------------------------------------------------------------------------------------------------------------
---------------------------------- Tread's Military Vehicle Zone definitions ----------------------------------
---------------------------------------------------------------------------------------------------------------
-------------------------------------- Code by Tread (Trealak on Steam) ---------------------------------------
---------------------------------------------------------------------------------------------------------------

---------------------------------------------------------------------------------------------------------------
---------------------------------------- Custom Util Functions --------------------------------------------
---------------------------------------------------------------------------------------------------------------

local function setContains(set, key) -- function for checking if value is in the table
    return set[key] ~= nil
end

local function tableConcat(...) -- function for merging multiple tables
local counter = 1
local result = {}
  for _, t in ipairs({...}) do
    for i, v in pairs(t) do
      result[i] = v
	  counter = counter + 1
    end
  end
  return result;
end


if VehicleZoneDistribution then	

---------------------------------------------------------------------------------------------------------------
---------------------------------------------------  PZK ------------------------------------------------------ 
---------------------------------------------------------------------------------------------------------------
		--Church Buses
		VehicleZoneDistribution.buschurch = VehicleZoneDistribution.buschurch or {};
		VehicleZoneDistribution.buschurch.vehicles = VehicleZoneDistribution.buschurch.vehicles or {};
		if not VehicleZoneDistribution.buschurch.spawnRate then VehicleZoneDistribution.buschurch.spawnRate = 80 end
		VehicleZoneDistribution.buschurch.chanceToPartDamage = 60;
		VehicleZoneDistribution.buschurch.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.buschurch.chanceToSpawnSpecial = 0;
		--VehicleZoneDistribution.buschurch.chanceToSpawnNormal = 10;

		-- Army March Ridge BLEND
		VehicleZoneDistribution.armymr = VehicleZoneDistribution.armymr or {};
		VehicleZoneDistribution.armymr.vehicles = VehicleZoneDistribution.armymr.vehicles or {};
		if not VehicleZoneDistribution.armymr.spawnRate then VehicleZoneDistribution.armymr.spawnRate = 5 end
		VehicleZoneDistribution.armymr.chanceToPartDamage = 60;
		VehicleZoneDistribution.armymr.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.armymr.chanceToSpawnSpecial = 5;
		VehicleZoneDistribution.armymr.chanceToSpawnNormal = 10;

		-- army contractor (Hazmat)
		VehicleZoneDistribution.hazmat = VehicleZoneDistribution.hazmat or {};
		VehicleZoneDistribution.hazmat.vehicles = VehicleZoneDistribution.hazmat.vehicles or {};
		if not VehicleZoneDistribution.hazmat.spawnRate then VehicleZoneDistribution.hazmat.spawnRate = 100 end
		VehicleZoneDistribution.hazmat.chanceToPartDamage = 60;
		VehicleZoneDistribution.hazmat.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.hazmat.chanceToSpawnSpecial = 0;
		--VehicleZoneDistribution.hazmat.chanceToSpawnNormal = 10;
		
		-- BFRF
		VehicleZoneDistribution.bfrf = VehicleZoneDistribution.bfrf or {};
		VehicleZoneDistribution.bfrf.vehicles = VehicleZoneDistribution.bfrf.vehicles or {};
		if not VehicleZoneDistribution.bfrf.spawnRate then VehicleZoneDistribution.bfrf.spawnRate = 45 end
		VehicleZoneDistribution.bfrf.chanceToPartDamage = 60;
		VehicleZoneDistribution.bfrf.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.bfrf.chanceToSpawnSpecial = 0;
		--VehicleZoneDistribution.bfrf.chanceToSpawnNormal = 0;
		
		-- golfcart
		VehicleZoneDistribution.golfcart = VehicleZoneDistribution.golfcart or {};
		VehicleZoneDistribution.golfcart.vehicles = VehicleZoneDistribution.golfcart.vehicles or {};
		if not VehicleZoneDistribution.golfcart.spawnRate then VehicleZoneDistribution.golfcart.spawnRate = 60 end
		VehicleZoneDistribution.golfcart.chanceToPartDamage = 60;
		VehicleZoneDistribution.golfcart.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.golfcart.chanceToSpawnSpecial = 5;
		VehicleZoneDistribution.golfcart.chanceToSpawnNormal = 10;
		
		
		-- school busses
		VehicleZoneDistribution.schoolbus = VehicleZoneDistribution.schoolbus or {};
		VehicleZoneDistribution.schoolbus.vehicles = VehicleZoneDistribution.schoolbus.vehicles or {};
		if not VehicleZoneDistribution.schoolbus.spawnRate then VehicleZoneDistribution.schoolbus.spawnRate = 65 end
		VehicleZoneDistribution.schoolbus.chanceToPartDamage = 60;
		VehicleZoneDistribution.schoolbus.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.schoolbus.chanceToSpawnSpecial = 0;
		--VehicleZoneDistribution.schoolbus.chanceToSpawnNormal = 0;
		
		-- Transit busses
		VehicleZoneDistribution.busservice = VehicleZoneDistribution.busservice or {};
		VehicleZoneDistribution.busservice.vehicles = VehicleZoneDistribution.busservice.vehicles or {};
		if not VehicleZoneDistribution.busservice.spawnRate then VehicleZoneDistribution.busservice.spawnRate = 60 end
		VehicleZoneDistribution.busservice.chanceToPartDamage = 60;
		VehicleZoneDistribution.busservice.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.busservice.chanceToSpawnSpecial = 0;
		--VehicleZoneDistribution.busservice.chanceToSpawnNormal = 0;
		
		-- busstation
		VehicleZoneDistribution.busstation = VehicleZoneDistribution.busstation or {};
		VehicleZoneDistribution.busstation.vehicles = VehicleZoneDistribution.busstation.vehicles or {};
		if not VehicleZoneDistribution.busstation.spawnRate then VehicleZoneDistribution.busstation.spawnRate = 60 end
		VehicleZoneDistribution.busstation.chanceToPartDamage = 60;
		VehicleZoneDistribution.busstation.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.busstation.chanceToSpawnSpecial = 0;
		--VehicleZoneDistribution.busstation.chanceToSpawnNormal = 0;
		
		-- Mall security
		VehicleZoneDistribution.mall = VehicleZoneDistribution.mall or {};
		VehicleZoneDistribution.mall.vehicles = VehicleZoneDistribution.mall.vehicles or {};
		if not VehicleZoneDistribution.mall.spawnRate then VehicleZoneDistribution.mall.spawnRate = 65 end
		VehicleZoneDistribution.mall.chanceToPartDamage = 60;
		VehicleZoneDistribution.mall.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.mall.chanceToSpawnSpecial = 0;
		--VehicleZoneDistribution.mall.chanceToSpawnNormal = 0;
		
		-- LSU security
		VehicleZoneDistribution.lsu = VehicleZoneDistribution.lsu or {};
		VehicleZoneDistribution.lsu.vehicles = VehicleZoneDistribution.lsu.vehicles or {};
		if not VehicleZoneDistribution.lsu.spawnRate then VehicleZoneDistribution.lsu.spawnRate = 65 end
		VehicleZoneDistribution.lsu.chanceToPartDamage = 60;
		VehicleZoneDistribution.lsu.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.lsu.chanceToSpawnSpecial = 0;
		--VehicleZoneDistribution.lsu.chanceToSpawnNormal = 0;
		
		-- Mafia
		VehicleZoneDistribution.mafia = VehicleZoneDistribution.mafia or {};
		VehicleZoneDistribution.mafia.vehicles = VehicleZoneDistribution.mafia.vehicles or {};
		if not VehicleZoneDistribution.mafia.spawnRate then VehicleZoneDistribution.mafia.spawnRate = 65 end
		VehicleZoneDistribution.mafia.chanceToPartDamage = 60;
		VehicleZoneDistribution.mafia.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.mafia.chanceToSpawnSpecial = 0;
		--VehicleZoneDistribution.mafia.chanceToSpawnNormal = 0;
		VehicleZoneDistribution.mafia.specialCar = true;


		-- Cementery
		VehicleZoneDistribution.cementery = VehicleZoneDistribution.cementery or {};
		VehicleZoneDistribution.cementery.vehicles = VehicleZoneDistribution.cementery.vehicles or {};
		if not VehicleZoneDistribution.cementery.spawnRate then VehicleZoneDistribution.cementery.spawnRate = 55 end
		VehicleZoneDistribution.cementery.chanceToPartDamage = 60;
		VehicleZoneDistribution.cementery.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.cementery.chanceToSpawnSpecial = 0;
		--VehicleZoneDistribution.cementery.chanceToSpawnNormal = 0;


		--Music festivals
		VehicleZoneDistribution.music_festival = VehicleZoneDistribution.music_festival or {};
		VehicleZoneDistribution.music_festival.vehicles = VehicleZoneDistribution.music_festival.vehicles or {};
		if not VehicleZoneDistribution.music_festival.spawnRate then VehicleZoneDistribution.music_festival.spawnRate = 35 end 
		VehicleZoneDistribution.music_festival.chanceToPartDamage = 60;
		VehicleZoneDistribution.music_festival.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.music_festival.chanceToSpawnSpecial = 0;


		-- bank
		VehicleZoneDistribution.bank = VehicleZoneDistribution.bank or {};
		VehicleZoneDistribution.bank.vehicles = VehicleZoneDistribution.bank.vehicles or {};
		if not VehicleZoneDistribution.bank.spawnRate then VehicleZoneDistribution.bank.spawnRate = 35 end  
		VehicleZoneDistribution.bank.chanceToPartDamage = 60;
		VehicleZoneDistribution.bank.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.bank.chanceToSpawnSpecial = 0;
		--VehicleZoneDistribution.bank.chanceToSpawnNormal = 0;

		-- wasteservice - dupster, garbage trucks
		VehicleZoneDistribution.wasteservice = VehicleZoneDistribution.wasteservice or {};
		VehicleZoneDistribution.wasteservice.vehicles = VehicleZoneDistribution.wasteservice.vehicles or {};
		if not VehicleZoneDistribution.wasteservice.spawnRate then VehicleZoneDistribution.wasteservice.spawnRate = 35 end  
		VehicleZoneDistribution.wasteservice.chanceToPartDamage = 60;
		VehicleZoneDistribution.wasteservice.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.wasteservice.chanceToSpawnSpecial = 0;
		--VehicleZoneDistribution.wasteservice.chanceToSpawnNormal = 0;

		-- field_farm
		VehicleZoneDistribution.field_farm = VehicleZoneDistribution.field_farm or {};
		VehicleZoneDistribution.field_farm.vehicles = VehicleZoneDistribution.field_farm.vehicles or {};
		if not VehicleZoneDistribution.field_farm.spawnRate then VehicleZoneDistribution.field_farm.spawnRate = 35 end
		VehicleZoneDistribution.field_farm.chanceToPartDamage = 60;
		VehicleZoneDistribution.field_farm.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.field_farm.chanceToSpawnSpecial = 0;
		--VehicleZoneDistribution.field_farm.chanceToSpawnNormal = 0;


		-- nightclub
		VehicleZoneDistribution.nightclub = VehicleZoneDistribution.nightclub or {};
		VehicleZoneDistribution.nightclub.vehicles = VehicleZoneDistribution.nightclub.vehicles or {};
		if not VehicleZoneDistribution.nightclub.spawnRate then VehicleZoneDistribution.nightclub.spawnRate = 35 end
		VehicleZoneDistribution.nightclub.chanceToPartDamage = 60;
		VehicleZoneDistribution.nightclub.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.nightclub.chanceToSpawnSpecial = 2;
		

		-- semis
		VehicleZoneDistribution.semi = VehicleZoneDistribution.semi or {};
		VehicleZoneDistribution.semi.vehicles = VehicleZoneDistribution.semi.vehicles or {};
		if not VehicleZoneDistribution.semi.spawnRate then VehicleZoneDistribution.semi.spawnRate = 65 end
		VehicleZoneDistribution.semi.chanceToPartDamage = 60;
		VehicleZoneDistribution.semi.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.semi.chanceToSpawnSpecial = 0;
		--VehicleZoneDistribution.semi.chanceToSpawnNormal = 0;

		-- semi trailers
		VehicleZoneDistribution.bigtrailerparkinglot = VehicleZoneDistribution.bigtrailerparkinglot or {};
		VehicleZoneDistribution.bigtrailerparkinglot.vehicles = VehicleZoneDistribution.bigtrailerparkinglot.vehicles or {};
		if not VehicleZoneDistribution.bigtrailerparkinglot.spawnRate then VehicleZoneDistribution.bigtrailerparkinglot.spawnRate = 65 end
		VehicleZoneDistribution.bigtrailerparkinglot.chanceToPartDamage = 60;
		VehicleZoneDistribution.bigtrailerparkinglot.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.bigtrailerparkinglot.chanceToSpawnSpecial = 0;
		--VehicleZoneDistribution.bigtrailerparkinglot.chanceToSpawnNormal = 0;

		-- PizzaWhirled
		VehicleZoneDistribution.pizzawhirled = VehicleZoneDistribution.pizzawhirled or {};
		VehicleZoneDistribution.pizzawhirled.vehicles = VehicleZoneDistribution.pizzawhirled.vehicles or {};
		if not VehicleZoneDistribution.pizzawhirled.spawnRate then VehicleZoneDistribution.pizzawhirled.spawnRate = 45 end
		VehicleZoneDistribution.pizzawhirled.chanceToPartDamage = 60;
		VehicleZoneDistribution.pizzawhirled.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.pizzawhirled.chanceToSpawnSpecial = 0;
		--VehicleZoneDistribution.pizzawhirled.chanceToSpawnNormal = 0;
		-- ChurnRUs
		VehicleZoneDistribution.churnrus = VehicleZoneDistribution.churnrus or {};
		VehicleZoneDistribution.churnrus.vehicles = VehicleZoneDistribution.churnrus.vehicles or {};
		if not VehicleZoneDistribution.semi.spawnRate then VehicleZoneDistribution.churnrus.spawnRate = 45 end
		VehicleZoneDistribution.churnrus.chanceToPartDamage = 60;
		VehicleZoneDistribution.churnrus.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.churnrus.chanceToSpawnSpecial = 0;
		--VehicleZoneDistribution.churnrus.chanceToSpawnNormal = 0;
		-- MilkMonarchy
		VehicleZoneDistribution.milkmonarchy = VehicleZoneDistribution.milkmonarchy or {};
		VehicleZoneDistribution.milkmonarchy.vehicles = VehicleZoneDistribution.milkmonarchy.vehicles or {};
		if not VehicleZoneDistribution.milkmonarchy.spawnRate then VehicleZoneDistribution.milkmonarchy.spawnRate = 45 end
		VehicleZoneDistribution.milkmonarchy.chanceToPartDamage = 60;
		VehicleZoneDistribution.milkmonarchy.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.milkmonarchy.chanceToSpawnSpecial = 0;
		--VehicleZoneDistribution.milkmonarchy.chanceToSpawnNormal = 0;
		-- TacoDelPancho
		VehicleZoneDistribution.tacodelpancho = VehicleZoneDistribution.tacodelpancho or {};
		VehicleZoneDistribution.tacodelpancho.vehicles = VehicleZoneDistribution.tacodelpancho.vehicles or {};
		if not VehicleZoneDistribution.tacodelpancho.spawnRate then VehicleZoneDistribution.tacodelpancho.spawnRate = 45 end
		VehicleZoneDistribution.tacodelpancho.chanceToPartDamage = 60;
		VehicleZoneDistribution.tacodelpancho.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.tacodelpancho.chanceToSpawnSpecial = 0;
		--VehicleZoneDistribution.tacodelpancho.chanceToSpawnNormal = 0;
		-- FoodTruckRandomSpot
		VehicleZoneDistribution.foodtruckrandomspot = VehicleZoneDistribution.foodtruckrandomspot or {};
		VehicleZoneDistribution.foodtruckrandomspot.vehicles = VehicleZoneDistribution.foodtruckrandomspot.vehicles or {};
		if not VehicleZoneDistribution.foodtruckrandomspot.spawnRate then VehicleZoneDistribution.foodtruckrandomspot.spawnRate = 45 end
		VehicleZoneDistribution.foodtruckrandomspot.chanceToPartDamage = 60;
		VehicleZoneDistribution.foodtruckrandomspot.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.foodtruckrandomspot.chanceToSpawnSpecial = 0;
		--VehicleZoneDistribution.foodtruckrandomspot.chanceToSpawnNormal = 0;

		-- fueltanker
		VehicleZoneDistribution.fueltanker = VehicleZoneDistribution.fueltanker or {};
		VehicleZoneDistribution.fueltanker.vehicles = VehicleZoneDistribution.fueltanker.vehicles or {};
		if not VehicleZoneDistribution.fueltanker.spawnRate then VehicleZoneDistribution.fueltanker.spawnRate = 35 end
		VehicleZoneDistribution.fueltanker.chanceToPartDamage = 60;
		VehicleZoneDistribution.fueltanker.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.fueltanker.chanceToSpawnSpecial = 0;
		--VehicleZoneDistribution.fueltanker.chanceToSpawnNormal = 0;

		-- waterservice
		VehicleZoneDistribution.waterservice = VehicleZoneDistribution.waterservice or {};
		VehicleZoneDistribution.waterservice.vehicles = VehicleZoneDistribution.waterservice.vehicles or {};
		if not VehicleZoneDistribution.waterservice.spawnRate then VehicleZoneDistribution.waterservice.spawnRate = 35 end
		VehicleZoneDistribution.waterservice.chanceToPartDamage = 60;
		VehicleZoneDistribution.waterservice.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.waterservice.chanceToSpawnSpecial = 0;
		--VehicleZoneDistribution.waterservice.chanceToSpawnNormal = 0;



		-- IronRodentMC
		VehicleZoneDistribution.ironrodentmc = VehicleZoneDistribution.ironrodentmc or {};
		VehicleZoneDistribution.ironrodentmc.vehicles = VehicleZoneDistribution.ironrodentmc.vehicles or {};
		if not VehicleZoneDistribution.ironrodentmc.spawnRate then VehicleZoneDistribution.ironrodentmc.spawnRate = 35 end
		VehicleZoneDistribution.ironrodentmc.chanceToPartDamage = 60;
		VehicleZoneDistribution.ironrodentmc.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.ironrodentmc.chanceToSpawnSpecial = 0;
		--VehicleZoneDistribution.ironrodentmc.chanceToSpawnNormal = 0;

		-- WildRaccoonsMC
		VehicleZoneDistribution.wildraccoonsmc = VehicleZoneDistribution.wildraccoonsmc or {};
		VehicleZoneDistribution.wildraccoonsmc.vehicles = VehicleZoneDistribution.wildraccoonsmc.vehicles or {};
		if not VehicleZoneDistribution.wildraccoonsmc.spawnRate then VehicleZoneDistribution.wildraccoonsmc.spawnRate = 35 end
		VehicleZoneDistribution.wildraccoonsmc.chanceToPartDamage = 60;
		VehicleZoneDistribution.wildraccoonsmc.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.wildraccoonsmc.chanceToSpawnSpecial = 0;
		--VehicleZoneDistribution.wildraccoonsmc.chanceToSpawnNormal = 0;

		-- BarrelDogsMC
		VehicleZoneDistribution.barreldogsmc = VehicleZoneDistribution.barreldogsmc or {};
		VehicleZoneDistribution.barreldogsmc.vehicles = VehicleZoneDistribution.barreldogsmc.vehicles or {};
		if not VehicleZoneDistribution.barreldogsmc.spawnRate then VehicleZoneDistribution.barreldogsmc.spawnRate = 35 end
		VehicleZoneDistribution.barreldogsmc.chanceToPartDamage = 60;
		VehicleZoneDistribution.barreldogsmc.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.barreldogsmc.chanceToSpawnSpecial = 0;
		--VehicleZoneDistribution.barreldogsmc.chanceToSpawnNormal = 0;
		
		-- SonsOfAnarchyMC
		VehicleZoneDistribution.sonsanarchymc = VehicleZoneDistribution.sonsanarchymc or {};
		VehicleZoneDistribution.sonsanarchymc.vehicles = VehicleZoneDistribution.sonsanarchymc.vehicles or {};
		if not VehicleZoneDistribution.sonsanarchymc.spawnRate then VehicleZoneDistribution.sonsanarchymc.spawnRate = 35 end
		VehicleZoneDistribution.sonsanarchymc.chanceToPartDamage = 60;
		VehicleZoneDistribution.sonsanarchymc.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.sonsanarchymc.chanceToSpawnSpecial = 0;
		--VehicleZoneDistribution.sonsanarchymc.chanceToSpawnNormal = 0;
			
		-- construction
		VehicleZoneDistribution.construction = VehicleZoneDistribution.construction or {};
		VehicleZoneDistribution.construction.vehicles = VehicleZoneDistribution.construction.vehicles or {};
		if not VehicleZoneDistribution.construction.spawnRate then VehicleZoneDistribution.construction.spawnRate = 45 end
		VehicleZoneDistribution.construction.chanceToPartDamage = 60;
		VehicleZoneDistribution.construction.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.construction.chanceToSpawnSpecial = 0;
		
		
		-- hotpursuit
		VehicleZoneDistribution.hotpursuit = VehicleZoneDistribution.hotpursuit or {};
		VehicleZoneDistribution.hotpursuit.vehicles = VehicleZoneDistribution.hotpursuit.vehicles or {};
		if not VehicleZoneDistribution.hotpursuit.spawnRate then VehicleZoneDistribution.hotpursuit.spawnRate = 45 end
		VehicleZoneDistribution.hotpursuit.chanceToPartDamage = 60;
		VehicleZoneDistribution.hotpursuit.baseVehicleQuality = 0.43;
		VehicleZoneDistribution.hotpursuit.chanceToSpawnSpecial = 0;
		--VehicleZoneDistribution.hotpursuit.chanceToSpawnNormal = 0;
		VehicleZoneDistribution.hotpursuit.specialCar = true;
		
		

		-- Public Works
        VehicleZoneDistribution.public_works = VehicleZoneDistribution.public_works or {};
        VehicleZoneDistribution.public_works.vehicles = VehicleZoneDistribution.public_works.vehicles or {};
        if not VehicleZoneDistribution.public_works.spawnRate then VehicleZoneDistribution.public_works.spawnRate = 55 end
        VehicleZoneDistribution.public_works.chanceToPartDamage = 60;
        VehicleZoneDistribution.public_works.baseVehicleQuality = 0.43;
        VehicleZoneDistribution.public_works.chanceToSpawnSpecial = 0;
        --VehicleZoneDistribution.public_works.chanceToSpawnNormal = 0;
		VehicleZoneDistribution.public_works.specialCar = true;
		
		


-- police

-- fire

-- ranger

-- mccoy

-- Fossoil

-- scarlet dist

-- ambulance

-- military (default military zone used in mods - Most modders add ALL their military vehicles here)



VehicleZoneDistribution.military = VehicleZoneDistribution.military or {}
VehicleZoneDistribution.military.vehicles = VehicleZoneDistribution.military.vehicles or {}
if VehicleZoneDistribution.military.spawnRate == nil then -- spawn rate as mods set it, or as native army or 20%	
	if VehicleZoneDistribution.army ~= nil and VehicleZoneDistribution.army.spawnRate ~= nil then 
		VehicleZoneDistribution.military.spawnRate = VehicleZoneDistribution.army.spawnRate   
	else VehicleZoneDistribution.military.spawnRate = 20 end
end

-- military Light Vehicles (light & regular vehicles, NO trailers, NO Heavy vehicles)
VehicleZoneDistribution.military_light_veh = VehicleZoneDistribution.military_light_veh or {}
VehicleZoneDistribution.military_light_veh.vehicles = VehicleZoneDistribution.military_light_veh.vehicles or {}
if VehicleZoneDistribution.military_light_veh.spawnRate == nil then -- spawn rate as it was, as military or 20%
	if VehicleZoneDistribution.military.spawnRate ~= nil then 
		VehicleZoneDistribution.military_light_veh.spawnRate = VehicleZoneDistribution.military.spawnRate 
	else VehicleZoneDistribution.military_light_veh.spawnRate = 20 end
end
VehicleZoneDistribution.military_light_veh.chanceToPartDamage = 25;
VehicleZoneDistribution.military_light_veh.baseVehicleQuality = 0.8;
--VehicleZoneDistribution.military_light_veh.chanceToSpawnSpecial = 0;
VehicleZoneDistribution.military_light_veh.chanceToSpawnNormal = 0;
VehicleZoneDistribution.military_light_veh.specialCar = true;

-- military Light Trailers (regular trailers, but of military kind)
VehicleZoneDistribution.military_light_trailers = VehicleZoneDistribution.military_light_trailers or {}
VehicleZoneDistribution.military_light_trailers.vehicles = VehicleZoneDistribution.military_light_trailers.vehicles or {}
if not VehicleZoneDistribution.military_light_trailers.spawnRate then VehicleZoneDistribution.military_light_trailers.spawnRate = 25 end -- higher spawn, since those places are few
VehicleZoneDistribution.military_light_trailers.chanceToPartDamage = 25;
VehicleZoneDistribution.military_light_trailers.baseVehicleQuality = 0.8;
VehicleZoneDistribution.military_light_trailers.chanceToSpawnKey = 0; -- we want no keys for trailers
VehicleZoneDistribution.military_light_trailers.chanceToSpawnSpecial = 0;

-- military Heavy Vehicles (APCs and heavy trucks)
VehicleZoneDistribution.military_heavy_veh = VehicleZoneDistribution.military_heavy_veh or {}
VehicleZoneDistribution.military_heavy_veh.vehicles = VehicleZoneDistribution.military_heavy_veh.vehicles or {}
if not VehicleZoneDistribution.military_heavy_veh.spawnRate then VehicleZoneDistribution.military_heavy_veh.spawnRate = 25 end -- higher spawn, since those places are few
VehicleZoneDistribution.military_heavy_veh.chanceToPartDamage = 25;
VehicleZoneDistribution.military_heavy_veh.baseVehicleQuality = 0.8;
VehicleZoneDistribution.military_heavy_veh.chanceToSpawnSpecial = 0;

-- military Heavy Trailers
VehicleZoneDistribution.military_heavy_trailers = VehicleZoneDistribution.military_heavy_trailers or {}
VehicleZoneDistribution.military_heavy_trailers.vehicles = VehicleZoneDistribution.military_heavy_trailers.vehicles or {}
if not VehicleZoneDistribution.military_heavy_trailers.spawnRate then VehicleZoneDistribution.military_heavy_trailers.spawnRate = 25 end -- higher spawn, since those places are few
VehicleZoneDistribution.military_heavy_trailers.chanceToPartDamage = 25;
VehicleZoneDistribution.military_heavy_trailers.baseVehicleQuality = 0.8;
VehicleZoneDistribution.military_heavy_trailers.chanceToSpawnKey = 0; -- we want no keys for trailers
VehicleZoneDistribution.military_heavy_trailers.chanceToSpawnSpecial = 0;

-- military Burnt Vehicles and Trailers
VehicleZoneDistribution.military_burnt = VehicleZoneDistribution.military_burnt or {}
VehicleZoneDistribution.military_burnt.vehicles = VehicleZoneDistribution.military_burnt.vehicles or {}
if not VehicleZoneDistribution.military_burnt.spawnRate then VehicleZoneDistribution.military_burnt.spawnRate = 45 end -- spawn rate as it was or 45%

-- ********************************************************************************************************* --
-- ********************************* NATIVE army tag - New in Zomboid 41.65 ******************************** --
-- ********************************************************************************************************* --


VehicleZoneDistribution.army = VehicleZoneDistribution.army or {}		-- parking places with this tag already exist in Native, however there were no spawn declarations for them (as of 41.65)
VehicleZoneDistribution.army.vehicles = VehicleZoneDistribution.army.vehicles or {} -- those 2 lines initiate spawn list for the "Army" tag, It probably will contain ALL NATIVE army vehicles
-- merge "Army" and "Military" spawn lists (future native and modded "military" vehicles) count in. This merge allows filter/splitter function to parse only one (Army) table.
VehicleZoneDistribution.army.vehicles = tableConcat(VehicleZoneDistribution.army.vehicles, VehicleZoneDistribution.military.vehicles);
if VehicleZoneDistribution.army.spawnRate == nil then --spawn rate as NATIVE "army", or as military mods, or 20%
	if VehicleZoneDistribution.military.spawnRate ~= nil then 
		VehicleZoneDistribution.army.spawnRate = VehicleZoneDistribution.military.spawnRate 
	else VehicleZoneDistribution.army.spawnRate = 20 end
end
VehicleZoneDistribution.army.chanceToPartDamage = 25;
VehicleZoneDistribution.army.baseVehicleQuality = 0.8;
VehicleZoneDistribution.army.chanceToSpawnSpecial = 0;

-- military Chaos - any vehicle, higher chance of damage, random angle
VehicleZoneDistribution.military_chaos = VehicleZoneDistribution.military_chaos or {}
VehicleZoneDistribution.military_chaos.vehicles = VehicleZoneDistribution.military_chaos.vehicles or {}
VehicleZoneDistribution.military_chaos.vehicles = tableConcat(VehicleZoneDistribution.military_chaos.vehicles, VehicleZoneDistribution.army.vehicles);	-- merge "Army" and "Military" spawn lists (future native and modded "military" vehicles)
if not VehicleZoneDistribution.military_chaos.spawnRate then VehicleZoneDistribution.military_chaos.spawnRate = 35 end
VehicleZoneDistribution.military_chaos.chanceToPartDamage = 60;
VehicleZoneDistribution.military_chaos.baseVehicleQuality = 0.43;
VehicleZoneDistribution.military_chaos.chanceToSpawnSpecial = 0;
VehicleZoneDistribution.military_chaos.randomAngle = true;



-- military Vehicles (light, regular & heavy vehicles)
VehicleZoneDistribution.military_vehicles = VehicleZoneDistribution.military_vehicles or {} -- initiate list with all Military Vehicles (by merging existing custom lists)
VehicleZoneDistribution.military_vehicles.vehicles = VehicleZoneDistribution.military_vehicles.vehicles or {}
VehicleZoneDistribution.military_vehicles.vehicles = tableConcat(VehicleZoneDistribution.military_vehicles.vehicles, VehicleZoneDistribution.military_light_veh.vehicles, VehicleZoneDistribution.military_heavy_veh.vehicles);
if VehicleZoneDistribution.military_vehicles.spawnRate == nil then -- spawn rate as it was, as military or 20%
	if VehicleZoneDistribution.military.spawnRate ~= nil then 
		VehicleZoneDistribution.military_vehicles.spawnRate = VehicleZoneDistribution.military.spawnRate
	else VehicleZoneDistribution.military_vehicles.spawnRate = 20 end
end
VehicleZoneDistribution.military_vehicles.chanceToPartDamage = 25;
VehicleZoneDistribution.military_vehicles.baseVehicleQuality = 0.8;
VehicleZoneDistribution.military_vehicles.chanceToSpawnSpecial = 0;

-- military Trailers (light, regular & heavy Trailers)
VehicleZoneDistribution.military_trailers = VehicleZoneDistribution.military_trailers or {} -- initiate list with all Military Trailes (by merging existing custom lists)
VehicleZoneDistribution.military_trailers.vehicles = VehicleZoneDistribution.military_trailers.vehicles or  {}
VehicleZoneDistribution.military_trailers.vehicles = tableConcat(VehicleZoneDistribution.military_trailers.vehicles, VehicleZoneDistribution.military_light_trailers.vehicles, VehicleZoneDistribution.military_heavy_trailers.vehicles);
if not VehicleZoneDistribution.military_trailers.spawnRate then VehicleZoneDistribution.military_trailers.spawnRate = 25 end -- higher spawn, since those places are few
VehicleZoneDistribution.military_trailers.chanceToPartDamage = 25;
VehicleZoneDistribution.military_trailers.baseVehicleQuality = 0.8;
VehicleZoneDistribution.military_trailers.chanceToSpawnKey = 0;	-- we want no keys for trailers
VehicleZoneDistribution.military_trailers.chanceToSpawnSpecial = 0;

-- Military Light Vehicles and Trailers
VehicleZoneDistribution.military_light = VehicleZoneDistribution.military_light or {} -- initiate list with all Military Vehicles (by merging existing custom lists)
VehicleZoneDistribution.military_light.vehicles = VehicleZoneDistribution.military_light.vehicles or {}
VehicleZoneDistribution.military_light.vehicles = tableConcat(VehicleZoneDistribution.military_light.vehicles, VehicleZoneDistribution.military_light_veh.vehicles, VehicleZoneDistribution.military_light_trailers.vehicles);
if not  VehicleZoneDistribution.military_light.spawnRate then VehicleZoneDistribution.military_light.spawnRate = 25 end -- higher spawn, since those places are few
VehicleZoneDistribution.military_light.chanceToPartDamage = 25;
VehicleZoneDistribution.military_light.baseVehicleQuality = 0.8;
VehicleZoneDistribution.military_light.chanceToSpawnSpecial = 0;

-- Military Heavy Vehicles and Trailers
VehicleZoneDistribution.military_heavy = VehicleZoneDistribution.military_heavy or {} -- initiate list with all Military Trailes (by merging existing custom lists)
VehicleZoneDistribution.military_heavy.vehicles = VehicleZoneDistribution.military_heavy.vehicles or  {}
VehicleZoneDistribution.military_heavy.vehicles = tableConcat(VehicleZoneDistribution.military_heavy.vehicles, VehicleZoneDistribution.military_heavy_veh.vehicles, VehicleZoneDistribution.military_heavy_trailers.vehicles);
if not VehicleZoneDistribution.military_heavy.spawnRate then VehicleZoneDistribution.military_heavy.spawnRate = 25 end -- higher spawn, since those places are few
VehicleZoneDistribution.military_heavy.chanceToPartDamage = 25;
VehicleZoneDistribution.military_heavy.baseVehicleQuality = 0.8;
VehicleZoneDistribution.military_heavy.chanceToSpawnSpecial = 0;

--PZK FIX --
-- merge "Army" spawn lists (future native and modded "military" vehicles) count in. 
VehicleZoneDistribution.military = VehicleZoneDistribution.army;


-- Ranger
        VehicleZoneDistribution.ranger = VehicleZoneDistribution.ranger or {};
        VehicleZoneDistribution.ranger.vehicles = VehicleZoneDistribution.ranger.vehicles or {};
        VehicleZoneDistribution.ranger.spawnRate = 65;
        VehicleZoneDistribution.ranger.chanceToPartDamage = 50;
        VehicleZoneDistribution.ranger.baseVehicleQuality = 0.73;
        VehicleZoneDistribution.ranger.chanceToSpawnSpecial = 0;
        --VehicleZoneDistribution.ranger.chanceToSpawnNormal = 0;
		VehicleZoneDistribution.ranger.specialCar = true;	




---------------------------------------------------------------------------------------------------------------
------------------------------- Project Indiana by Conan the Librarian ---------------------------------
---------------------------------------------------------------------------------------------------------------

-- bell system (Telecommunication Indiana)

VehicleZoneDistribution.phone = VehicleZoneDistribution.phone or {};
VehicleZoneDistribution.phone.vehicles = VehicleZoneDistribution.phone.vehicles or {};
if not VehicleZoneDistribution.phone.spawnRate then VehicleZoneDistribution.phone.spawnRate = 65 end
VehicleZoneDistribution.phone.chanceToSpawnNormal = 5;
VehicleZoneDistribution.phone.chanceToPartDamage = 30;
VehicleZoneDistribution.phone.baseVehicleQuality = 0.43;
VehicleZoneDistribution.phone.specialCar = true;



-- railroad
VehicleZoneDistribution.rail = VehicleZoneDistribution.rail or {};
VehicleZoneDistribution.rail.vehicles = VehicleZoneDistribution.rail.vehicles or {};
if not VehicleZoneDistribution.rail.spawnRate then VehicleZoneDistribution.rail.spawnRate = 65 end
VehicleZoneDistribution.rail.chanceToSpawnNormal = 5;
VehicleZoneDistribution.rail.chanceToPartDamage = 30;
VehicleZoneDistribution.rail.baseVehicleQuality = 0.43;
VehicleZoneDistribution.rail.specialCar = true;


-- power
VehicleZoneDistribution.power = VehicleZoneDistribution.power or {};
VehicleZoneDistribution.power.vehicles = VehicleZoneDistribution.power.vehicles or {};
if not VehicleZoneDistribution.power.spawnRate then VehicleZoneDistribution.power.spawnRate = 65 end
VehicleZoneDistribution.power.chanceToSpawnNormal = 5;
VehicleZoneDistribution.power.chanceToPartDamage = 30;
VehicleZoneDistribution.power.baseVehicleQuality = 0.43;
VehicleZoneDistribution.power.specialCar = true;

-- water 
VehicleZoneDistribution.water = VehicleZoneDistribution.water or {};
VehicleZoneDistribution.water.vehicles = VehicleZoneDistribution.water.vehicles or {};
if not VehicleZoneDistribution.water.spawnRate then VehicleZoneDistribution.water.spawnRate = 65 end
VehicleZoneDistribution.water.vehicles = tableConcat(VehicleZoneDistribution.water.vehicles, VehicleZoneDistribution.waterservice.vehicles); -- adding (merging table from waterservice)
VehicleZoneDistribution.water.chanceToSpawnNormal = 10;
VehicleZoneDistribution.water.chanceToPartDamage = 30;
VehicleZoneDistribution.water.baseVehicleQuality = 0.43;
VehicleZoneDistribution.water.specialCar = true;


-- INDOT  (Dpt. of Transportation Indiana)
VehicleZoneDistribution.indot = VehicleZoneDistribution.indot or {};
VehicleZoneDistribution.indot.vehicles = VehicleZoneDistribution.indot.vehicles or {};
if not VehicleZoneDistribution.indot.spawnRate then VehicleZoneDistribution.indot.spawnRate = 65 end
VehicleZoneDistribution.indot.chanceToSpawnNormal = 10;
VehicleZoneDistribution.indot.chanceToPartDamage = 30;
VehicleZoneDistribution.indot.baseVehicleQuality = 0.43;
VehicleZoneDistribution.indot.specialCar = true;

-- KYTC ( Kentucky Transportation Cabinet - Highway maintenance)
VehicleZoneDistribution.kytc = VehicleZoneDistribution.kytc or {};
VehicleZoneDistribution.kytc.vehicles = VehicleZoneDistribution.kytc.vehicles or {};
if not VehicleZoneDistribution.kytc.spawnRate then VehicleZoneDistribution.kytc.spawnRate = 65 end
VehicleZoneDistribution.kytc.chanceToSpawnNormal = 10;
VehicleZoneDistribution.kytc.chanceToPartDamage = 30;
VehicleZoneDistribution.kytc.baseVehicleQuality = 0.43;
VehicleZoneDistribution.kytc.specialCar = true;



---------------------------------------------------------------------------------------------------------------
------------------------------- Debug 100% Spawns (decomment for debugging) ---------------------------------
---------------------------------------------------------------------------------------------------------------

--[[
VehicleZoneDistribution.army.spawnRate = 100;				---Test line
VehicleZoneDistribution.military.spawnRate = 100;			---Test line
VehicleZoneDistribution.military_light_veh.spawnRate = 100;		---Test line
VehicleZoneDistribution.military_light_trailers.spawnRate = 100;	---Test line
VehicleZoneDistribution.military_heavy_veh.spawnRate = 100;		---Test line
VehicleZoneDistribution.military_heavy_trailers.spawnRate = 100;	---Test line
VehicleZoneDistribution.military_burnt.spawnRate = 100;			---Test line
VehicleZoneDistribution.military_vehicles.spawnRate = 100;		---Test line
VehicleZoneDistribution.military_trailers.spawnRate = 100;		---Test line
VehicleZoneDistribution.military_light.spawnRate = 100;			---Test line
VehicleZoneDistribution.military_heavy.spawnRate = 100;			---Test line
VehicleZoneDistribution.military_chaos.spawnRate = 100;			---Test line
VehicleZoneDistribution.cementery.spawnRate = 100;			---Test line
VehicleZoneDistribution.music_festival.spawnRate = 100;			---Test line
VehicleZoneDistribution.musician_parking.spawnRate = 100;		---Test line
VehicleZoneDistribution.mafia_parking.spawnRate = 100;			---Test line
VehicleZoneDistribution.bank.spawnRate = 100;				---Test line
VehicleZoneDistribution.nightclub.spawnRate = 100;			---Test line
VehicleZoneDistribution.field_farm.spawnRate = 100;			---Test line
VehicleZoneDistribution.farm_trailers.spawnRate = 100;			---Test line
VehicleZoneDistribution.semi.spawnRate = 100;				---Test line
VehicleZoneDistribution.semi_trailer.spawnRate = 100;			---Test line
]]--

end