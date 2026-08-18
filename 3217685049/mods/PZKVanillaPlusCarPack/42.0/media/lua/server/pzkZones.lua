

-------------------------------PZK------------------------------------------
-- bfrf 			-- Black Forest Research Facility
-- hazmat 			-- BFRF hazmat spawns
-- lsu  			-- university security
-- golfcart 			-- golf carts zones
-- mall 			--mall security
-- military	        -- default military zone used by many mods and on many custom maps
-- cementery            -- custom zone for hearses
-- music_festival       -- music festivals near scenes & audio technical cars
-- musician_parking     -- musicians tour cars
-- mafia_parking        -- good, mafia, exotics near hotels
-- bank                 -- for bank trucks
-- nightclub            -- spots for limos, exotic, mafia cars near nighclubs
-- field_farm           -- for tractors and farming equipment
-- farm_trailers        -- for plows and other farming trailers
-- semi		        -- for semi trucks
-- semi_trailers        -- for semi trailers
-- pizzawhirled		-- for pizza van, scooters
-- churnrus		-- for ice cream van
-- milkmonarchy		-- for milk truck
-- tacodelpancho	-- for taco van
-- foodtruckrandomspot	-- randomized food trucks near crowded squares, festivals & monuments
-- fueltankers		-- fuel tankers - near gas stations, refineries
-- waterservice		-- septic tanks, water tanks trucks, mostly near water threatment plants
-- wasteservice	-- Garbage trucks near garbage bins, landfills
-- BarrelDogsmc		-- Barrel Dogs MC spot near Dixie trailer park (HTH)
-- wildraccoonsmc	-- Wild Raccons MC spot near LV trailer park (HTH)
-- ironrodentmc	-- Iron Rodents MC spot near Riverside trailer park (HTH)




----------------------------------------------------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------- Zone Placement ---------------------------------------------------------------------

if isClient() then return end

local PZKZones = require 'pzkUtils/pzkZonesFunction'

local function Registry()
  if PZKZones then
	local PzkVanillaPlusCarPack = "PzkVanillaPlusCarPack"

  --------------------- Secret Base ----------------------
      PZKZones.addZone("military_vehicles", "ParkingStall", 5601, 12433, 0, 5,30, "E", PzkVanillaPlusCarPack)--secret base along fence 1 
      PZKZones.addZone("military_heavy_trailers", "ParkingStall", 5583, 12464, 0, 12,5, "N", PzkVanillaPlusCarPack)--secret base along fence 2
      PZKZones.addZone("military_light_veh", "ParkingStall", 5529, 12495, 0, 5,3, "W", PzkVanillaPlusCarPack)--secret base in front of garage 
      PZKZones.addZone("army", "ParkingStall", 5633, 12453, 0, 3,5, "S", PzkVanillaPlusCarPack)--secret base road 1
      PZKZones.addZone("army", "ParkingStall", 5813, 12478, 0, 5,3, "W", PzkVanillaPlusCarPack)--secret base road 2
      PZKZones.addZone("army", "ParkingStall", 6833, 12033, 0, 3,5, "N", PzkVanillaPlusCarPack)--secret base, approach
  ------------------------ Prison ------------------------
      PZKZones.addZone("military_vehicles", "ParkingStall", 7742, 11810, 0, 5,9, "W", PzkVanillaPlusCarPack)
      PZKZones.addZone("military_vehicles", "ParkingStall", 7742, 11795, 0, 5,6, "W", PzkVanillaPlusCarPack)
  --------------------- Barracks 1 -----------------------
      PZKZones.addZone("military_light_veh", "ParkingStall", 8118, 10236, 0, 3,5, "N", PzkVanillaPlusCarPack)-- near building
      PZKZones.addZone("military_vehicles", "ParkingStall", 8149, 10250, 0, 5,3, "E", PzkVanillaPlusCarPack)-- near tables 1
      PZKZones.addZone("military_light", "ParkingStall", 8149, 10253, 0, 5,3, "W", PzkVanillaPlusCarPack) -- near tables 2
      PZKZones.addZone("army", "ParkingStall", 8149, 10257, 0, 5,3, "E", PzkVanillaPlusCarPack) -- near tables 3
      PZKZones.addZone("military_light_veh", "ParkingStall", 8171, 10240, 0, 5,3, "E", PzkVanillaPlusCarPack) -- near blue house
      PZKZones.addZone("army", "ParkingStall", 8092, 10602, 0, 3,5, "N", PzkVanillaPlusCarPack)-- beside one of access roads
      PZKZones.addZone("military_heavy_trailers", "ParkingStall", 8132, 10259, 0, 5,3, "E", PzkVanillaPlusCarPack)-- semi-trailer along "table road"
  --------------------- Barracks 2 -----------------------
      PZKZones.addZone("military_light", "ParkingStall", 6779, 9922, 0, 5,6, "E", PzkVanillaPlusCarPack)	-- near building
      PZKZones.addZone("military_light_veh", "ParkingStall", 6779, 9931, 0, 5,3, "W", PzkVanillaPlusCarPack)-- near building
      PZKZones.addZone("army", "ParkingStall", 6790, 9982, 0, 15,5, "S", PzkVanillaPlusCarPack) 
  --------------------- Barracks 3 -----------------------
      PZKZones.addZone("military_light", "ParkingStall", 9106, 11807, 0, 5,3, "E", PzkVanillaPlusCarPack) -- near building
      PZKZones.addZone("army", "ParkingStall", 9110, 11832, 0, 5,15, "W", PzkVanillaPlusCarPack) -- main parking row
      PZKZones.addZone("military_light", "ParkingStall", 9103, 11834, 0, 3,15, "N", PzkVanillaPlusCarPack) --parallel parking
  -------------- Military "Fence" locations --------------
      PZKZones.addZone("military_vehicles", "ParkingStall", 14538, 4021, 0, 5,3, "W", PzkVanillaPlusCarPack) -- most E like outpost, near tent
      PZKZones.addZone("army", "ParkingStall", 14537, 3996, 0, 3,5, "N", PzkVanillaPlusCarPack) -- most E like outpost, near gate
      PZKZones.addZone("military_vehicles", "ParkingStall", 13449, 4074, 0, 3,5, "N", PzkVanillaPlusCarPack) -- bend outpost, near satellite
      PZKZones.addZone("military_light_trailers", "ParkingStall", 13427, 3958, 0, 5,3, "W", PzkVanillaPlusCarPack) -- bend outpost, blockade nearby
      PZKZones.addZone("military_light_veh", "ParkingStall", 13412, 3955, 0, 3,5, "N", PzkVanillaPlusCarPack)-- bend outpost, blockade nearby
      PZKZones.addZone("army", "ParkingStall", 13611, 4093, 0, 3,5, "N", PzkVanillaPlusCarPack) -- near watchtower between outposts
      PZKZones.addZone("military_burnt", "ParkingStall", 13554, 4124, 0, 5,3, "E", PzkVanillaPlusCarPack)-- near burned houses, near military fence - damage if possible
      PZKZones.addZone("army", "ParkingStall", 12729, 3966, 0, 6,5, "N", PzkVanillaPlusCarPack) -- "house" outpost, blind road
      PZKZones.addZone("military_vehicles", "ParkingStall", 12696, 3965, 0, 5,3, "W", PzkVanillaPlusCarPack) -- "house" outpost by first house
      PZKZones.addZone("military_light_trailers", "ParkingStall", 12778, 3989, 0, 5,3, "W", PzkVanillaPlusCarPack) -- "house" outpost by first house
  ------ Military "Fence" Base on way to Louisville ------
      PZKZones.addZone("military_light_veh", "ParkingStall", 12505, 4349, 0, 5,3, "E", PzkVanillaPlusCarPack) -- S gate
      PZKZones.addZone("military_light_veh", "ParkingStall", 12533, 4345, 0, 3,5, "N", PzkVanillaPlusCarPack) -- near S tower
      PZKZones.addZone("army", "ParkingStall", 12470, 4253, 0, 5,3, "E", PzkVanillaPlusCarPack) -- W part of camp, near white trailer
      PZKZones.addZone("army", "ParkingStall", 12524, 4246, 0, 3,5, "S", PzkVanillaPlusCarPack) -- E part of camp, near container
      PZKZones.addZone("military_light_veh", "ParkingStall", 12547, 4289, 0, 3,5, "N", PzkVanillaPlusCarPack)-- E part of camp, between tents, road
      PZKZones.addZone("army", "ParkingStall", 12576, 4269, 0, 5,3, "W", PzkVanillaPlusCarPack) -- E part of camp, between tents, parked
      PZKZones.addZone("army", "ParkingStall", 12499, 4238, 0, 3,5, "N", PzkVanillaPlusCarPack) -- W part of camp, between fence and tent
      PZKZones.addZone("army", "ParkingStall", 12518, 4202, 0, 3,5, "N", PzkVanillaPlusCarPack)-- N Gate 
      PZKZones.addZone("military_light_veh", "ParkingStall", 12526, 4214, 0, 5,3, "W", PzkVanillaPlusCarPack) -- near N tower
      PZKZones.addZone("army", "ParkingStall", 12478, 4225, 0, 5,3, "E", PzkVanillaPlusCarPack)-- W part of camp, between tents
      PZKZones.addZone("military_vehicles", "ParkingStall", 12565, 4343, 0, 6,5, "N", PzkVanillaPlusCarPack) -- near "fence" gate
      PZKZones.addZone("military_vehicles", "ParkingStall", 12591, 4068, 0, 3,5, "N", PzkVanillaPlusCarPack) -- fence blockade near refugee camp
      PZKZones.addZone("army", "ParkingStall", 12503, 4491, 0, 3,5, "S", PzkVanillaPlusCarPack) -- near outer fence S
      PZKZones.addZone("military_burnt", "ParkingStall", 12531, 4360, 0, 5,3, "E", PzkVanillaPlusCarPack)-- below S tower - in kill zone
      PZKZones.addZone("military_chaos", "ParkingStall", 12543, 4495, 0, 5,3, "E", PzkVanillaPlusCarPack)-- near outer fence S
  ------------------ army Surplus Shop -------------------
      PZKZones.addZone("military_vehicles", "ParkingStall", 12236, 1303, 0, 6,5, "N", PzkVanillaPlusCarPack) 
  ---------- Hospital on the way to Louisville -----------
      PZKZones.addZone("military_vehicles", "ParkingStall", 12478, 3639, 0, 3,5, "N", PzkVanillaPlusCarPack)
      PZKZones.addZone("army", "ParkingStall", 12476, 3708, 0, 3,5, "N", PzkVanillaPlusCarPack)
      PZKZones.addZone("military_light_veh", "ParkingStall", 12400, 3641, 0, 5,6, "W", PzkVanillaPlusCarPack)
      PZKZones.addZone("army", "ParkingStall", 12476, 3692, 0, 3,5, "N", PzkVanillaPlusCarPack)
  ---------- Ranger "forest base" near Riverside ---------
      PZKZones.addZone("army", "ParkingStall", 4673, 8620, 0, 3,5, "N", PzkVanillaPlusCarPack)
  ---------------- Hospital in Louisville ----------------
      PZKZones.addZone("military_light_veh", "ParkingStall", 12927, 2094, 0, 5,3, "E", PzkVanillaPlusCarPack)
      PZKZones.addZone("army", "ParkingStall", 12952, 2094, 0, 5,3, "E", PzkVanillaPlusCarPack)
      PZKZones.addZone("military_vehicles", "ParkingStall", 13014, 2002, 0, 3,5, "N", PzkVanillaPlusCarPack)
  --------- Bridge out of Louisville - map end -----------
      PZKZones.addZone("military_vehicles", "ParkingStall", 12594, 953, 0, 5,3, "E", PzkVanillaPlusCarPack)
  -------- East road out of Louisville - map end ---------
      PZKZones.addZone("military_vehicles", "ParkingStall", 14974, 3443, 0, 5,3, "E", PzkVanillaPlusCarPack)
      PZKZones.addZone("military_vehicles", "ParkingStall", 14981, 3453, 0, 5,3, "E", PzkVanillaPlusCarPack)
  --------------- Rail Station Louisville ----------------
      PZKZones.addZone("military_vehicles", "ParkingStall", 12683, 2355, 0, 5,3, "E", PzkVanillaPlusCarPack)
      PZKZones.addZone("military_light_veh", "ParkingStall", 12702, 2353, 0, 3,5, "S", PzkVanillaPlusCarPack)
  -------------- Police Station Louisville ---------------
      PZKZones.addZone("military_light_veh", "ParkingStall", 12494, 1607, 0, 3,5, "N", PzkVanillaPlusCarPack)
  ---------------- Refinery  Louisville ------------------
      PZKZones.addZone("army", "ParkingStall", 12065, 1393, 0, 3,5, "N", PzkVanillaPlusCarPack)
  --------------- "Barracks"  Louisville -----------------
      PZKZones.addZone("military_vehicles", "ParkingStall", 12442, 1421, 0, 5,6, "W", PzkVanillaPlusCarPack)
  ------------- March Ridge - Military town --------------
      PZKZones.addZone("military_vehicles", "ParkingStall", 10079, 12641, 0, 5,3, "E", PzkVanillaPlusCarPack)
      PZKZones.addZone("military_vehicles", "ParkingStall", 10141, 12815, 0, 5,3, "E", PzkVanillaPlusCarPack)
      PZKZones.addZone("military_vehicles", "ParkingStall", 9810, 12654, 0, 5,3, "E", PzkVanillaPlusCarPack)
      PZKZones.addZone("military_vehicles", "ParkingStall", 9934, 13015, 0, 5,3, "E", PzkVanillaPlusCarPack)
      PZKZones.addZone("military_vehicles", "ParkingStall", 9645, 12781, 0, 5,3, "E", PzkVanillaPlusCarPack)
      PZKZones.addZone("military_heavy_trailers", "ParkingStall", 10046, 12758, 0, 5,3, "E", PzkVanillaPlusCarPack) -- near community center
      PZKZones.addZone("military_heavy_trailers", "ParkingStall", 10058, 12758, 0, 5,3, "E", PzkVanillaPlusCarPack) -- near community center
      PZKZones.addZone("military_vehicles", "ParkingStall", 10018, 12710, 0, 3,5, "N", PzkVanillaPlusCarPack) -- near community center
      PZKZones.addZone("military_light_veh", "ParkingStall", 10008, 12677, 0, 5,3, "E", PzkVanillaPlusCarPack)-- near school
      PZKZones.addZone("military_light_veh", "ParkingStall", 9857, 12993, 0, 5,3, "W", PzkVanillaPlusCarPack) -- near house
      PZKZones.addZone("military_vehicles", "ParkingStall", 9984, 13018, 0, 3,5, "N", PzkVanillaPlusCarPack) -- near house
      PZKZones.addZone("military_burnt", "ParkingStall", 10352, 12417, 0, 3,5, "N", PzkVanillaPlusCarPack)-- near entry gate
      PZKZones.addZone("military_light_veh", "ParkingStall", 10354, 12404, 0, 3,5, "N", PzkVanillaPlusCarPack) -- near entry gate
      PZKZones.addZone("military_light_veh", "ParkingStall", 9858, 12616, 0, 5,3, "W", PzkVanillaPlusCarPack) -- near house
      PZKZones.addZone("military_burnt", "ParkingStall", 9933, 12824, 0, 5,3, "W", PzkVanillaPlusCarPack) -- near house
      PZKZones.addZone("military_chaos", "ParkingStall", 9783, 12882, 0, 5,3, "W", PzkVanillaPlusCarPack)-- near house/fence hole
      PZKZones.addZone("military_heavy_veh", "ParkingStall", 10353, 12761, 0, 3,5, "N", PzkVanillaPlusCarPack)-- near house/church
      PZKZones.addZone("military_chaos", "ParkingStall", 10374, 12517, 0, 3,5, "N", PzkVanillaPlusCarPack)-- beside road
      PZKZones.addZone("military_light_veh", "ParkingStall", 10227, 12751, 0, 5,3, "W", PzkVanillaPlusCarPack)-- near house
      PZKZones.addZone("military_light", "ParkingStall", 10293, 12819, 0, 3,5, "S", PzkVanillaPlusCarPack) -- near house
      PZKZones.addZone("military_heavy", "ParkingStall", 10116, 12724, 0, 5,3, "E", PzkVanillaPlusCarPack) -- near shopping area garage
  --------------------------------------------------------
  ------------------- Random locations -------------------
      PZKZones.addZone("army", "ParkingStall", 11271, 6598, 0, 5,3, "W", PzkVanillaPlusCarPack)-- West Point Pier
      PZKZones.addZone("military_light_veh", "ParkingStall", 11914, 6940, 0, 3,5, "N", PzkVanillaPlusCarPack)	-- West Point Police Station
      PZKZones.addZone("military_light_veh", "ParkingStall", 11626, 8310, 0, 5,3, "W", PzkVanillaPlusCarPack)	-- West Point -> Muldraught on intersection
      PZKZones.addZone("military_light_veh", "ParkingStall", 13244, 5471, 0, 9,5, "N", PzkVanillaPlusCarPack)-- Shooting range west of West Point
      PZKZones.addZone("military_light_veh", "ParkingStall", 13114, 5313, 0, 3,5, "N", PzkVanillaPlusCarPack)-- Hunting Lodge near shooting range
      PZKZones.addZone("army", "ParkingStall", 3801, 8500, 0, 5,3, "W", PzkVanillaPlusCarPack)	-- Hunting Shop, S of Riverside
      PZKZones.addZone("military_vehicles", "ParkingStall", 6418, 5336, 0, 5,6, "W", PzkVanillaPlusCarPack)	-- Shopping zone of Riverside
      PZKZones.addZone("military_vehicles", "ParkingStall", 7248, 8190, 0, 3,5, "N", PzkVanillaPlusCarPack)-- Burgers on Dixie outskirts
      PZKZones.addZone("military_chaos", "ParkingStall", 11610, 10184, 0, 3,5, "S", PzkVanillaPlusCarPack)-- Train yard E of Muldraught,
      PZKZones.addZone("military_light", "ParkingStall", 10898, 10131, 0, 5,3, "E", PzkVanillaPlusCarPack)-- "Doctors" House in Muldraught
      PZKZones.addZone("military_vehicles", "ParkingStall", 8502, 8553, 0, 5,3, "E", PzkVanillaPlusCarPack)-- Vegetables stall near Ponies
      PZKZones.addZone("military_light", "ParkingStall", 5152, 5524, 0, 3,5, "N", PzkVanillaPlusCarPack)-- House above riverside, by river
      PZKZones.addZone("army", "ParkingStall", 8407, 7494, 0, 5,3, "E", PzkVanillaPlusCarPack)-- road by the river, between west point and riverside
      PZKZones.addZone("military_light_veh", "ParkingStall", 9821, 7631, 0, 3,5, "N", PzkVanillaPlusCarPack)	-- Farm, W of West Point, S of river
      PZKZones.addZone("military_light", "ParkingStall", 11591, 9291, 0, 3,5, "S", PzkVanillaPlusCarPack)	-- Isolated forest house (muldraught)
      PZKZones.addZone("military_light_veh", "ParkingStall", 12106, 9612, 0, 5,3, "E", PzkVanillaPlusCarPack) -- road below Muldraught
      PZKZones.addZone("military_light_veh", "ParkingStall", 13674, 2144, 0, 5,3, "E", PzkVanillaPlusCarPack) -- random house Louisville
      PZKZones.addZone("military_burnt", "ParkingStall", 13418, 4126, 0, 3,5, "S", PzkVanillaPlusCarPack) -- S of military fence - near blockade
      PZKZones.addZone("military_burnt", "ParkingStall", 3839, 6173, 0, 3,5, "S", PzkVanillaPlusCarPack) -- abandoned warehouse near Riverside
      PZKZones.addZone("military_chaos", "ParkingStall", 9501, 8759, 0, 3,5, "S", PzkVanillaPlusCarPack)	-- forest "cut-out" near forest cabin
      PZKZones.addZone("military_burnt", "ParkingStall", 11044, 9056, 0, 5,3, "E", PzkVanillaPlusCarPack) -- abandoned warehouse near Riverside

      PZKZones.addZone("army", "ParkingStall", 1514, 5576, 0, 5,30, "E", PzkVanillaPlusCarPack) -- Brandenburg Bridge
      PZKZones.addZone("army", "ParkingStall", 293, 9892, 0, 3,5, "S", PzkVanillaPlusCarPack) -- Ekron road blockade
      PZKZones.addZone("army", "ParkingStall", 293, 9897, 0, 3,5, "N", PzkVanillaPlusCarPack) -- Ekron road blockade
	  
	  PZKZones.addZone("army", "ParkingStall", 6788, 9980, 0, 18,5, "N", PzkVanillaPlusCarPack) -- 6 warehouses and army barracks
	  PZKZones.addZone("army", "ParkingStall", 5461, 9525, 0, 5,6, "E", PzkVanillaPlusCarPack) -- Doe valley military surplus

  

      PZKZones.addZone("army", "ParkingStall", 8307, 12205, 0, 5,3, "W", PzkVanillaPlusCarPack)	-- Gas Station below Rosewood
	  
	  




  ------------------- PZK locations -------------------
  
  ------------------- Church Busses ----------------------
	PZKZones.addZone("buschurch", "ParkingStall", 12736, 1364, 0, 3, 5, "N", PzkVanillaPlusCarPack)       --LV Cathedral
    PZKZones.addZone("buschurch", "ParkingStall", 12593, 3366, 0, 3, 5, "N", PzkVanillaPlusCarPack)     -- LV Cementary
	PZKZones.addZone("buschurch", "ParkingStall", 10756, 10177, 0, 5, 3, "E", PzkVanillaPlusCarPack)    -- Muldlol
	PZKZones.addZone("buschurch", "ParkingStall", 10311, 12776, 0, 3, 5, "S", PzkVanillaPlusCarPack)    -- March Ridge
	PZKZones.addZone("buschurch", "ParkingStall", 8128, 11572, 0, 5, 3, "E", PzkVanillaPlusCarPack)    -- Rosewood
	PZKZones.addZone("buschurch", "ParkingStall", 7394, 8367, 0, 5, 3, "E", PzkVanillaPlusCarPack)    -- Fallas lake
	PZKZones.addZone("buschurch", "ParkingStall", 456, 9905, 0, 5, 3, "E", PzkVanillaPlusCarPack)    -- Ekron
	PZKZones.addZone("buschurch", "ParkingStall", 8293, 11558, 0, 3, 5, "N", PzkVanillaPlusCarPack)    -- Rosewood
 
  
    ------------------- BFRF ----------------------
	PZKZones.addZone("bfrf", "ParkingStall", 5615, 12467, 0, 21, 5, "S", PzkVanillaPlusCarPack) -- BFRF parking
	PZKZones.addZone("bfrf", "ParkingStall", 5645, 12467, 0, 21, 5, "S", PzkVanillaPlusCarPack) -- BFRF parking
	PZKZones.addZone("bfrf", "ParkingStall", 5624, 12482, 0, 33, 5, "S", PzkVanillaPlusCarPack) -- BFRF parking
	PZKZones.addZone("bfrf", "ParkingStall", 5624, 12490, 0, 33, 5, "S", PzkVanillaPlusCarPack) -- BFRF parking
	PZKZones.addZone("bfrf", "ParkingStall", 5615, 12504, 0, 51, 5, "S", PzkVanillaPlusCarPack) -- BFRF parking
	
	  ------------------- HAZMATS ----------------------
	PZKZones.addZone("hazmat", "ParkingStall", 5520, 12485, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- BFRF parking
	PZKZones.addZone("hazmat", "ParkingStall", 5584, 12463, 0, 3, 5, "S", PzkVanillaPlusCarPack) -- BFRF parking
	PZKZones.addZone("hazmat", "ParkingStall", 1511, 5544, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- Brandenburg
	PZKZones.addZone("hazmat", "ParkingStall", 12489, 4331, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- LV Camp
	PZKZones.addZone("hazmat", "ParkingStall", 15424, 2923, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- LV AIRPORT
	PZKZones.addZone("hazmat", "ParkingStall", 12603, 951, 0, 3, 5, "S", PzkVanillaPlusCarPack) -- LV Behind Bridge
  
  
  ------------------- GOLF CARTS ----------------------
  
  PZKZones.addZone("golfcart", "ParkingStall", 5714, 6422, 0, 15, 5, "S", PzkVanillaPlusCarPack) -- Golf course parking
  PZKZones.addZone("golfcart", "ParkingStall", 5717, 6410, 0, 24, 5, "N", PzkVanillaPlusCarPack) -- Golf course parking
  PZKZones.addZone("golfcart", "ParkingStall", 5717, 6405, 0, 24, 5, "S", PzkVanillaPlusCarPack) -- Golf course parking
  PZKZones.addZone("golfcart", "ParkingStall", 5729, 6393, 0, 12, 5, "N", PzkVanillaPlusCarPack) -- Golf course parking
  
  PZKZones.addZone("golfcart", "ParkingStall", 12925, 2279, 0, 5, 24, "W", PzkVanillaPlusCarPack) -- LV Golf course parking
  PZKZones.addZone("golfcart", "ParkingStall", 12937, 2279, 0, 5, 24, "E", PzkVanillaPlusCarPack) -- LV Golf course parking
  


--------------------- HEARSES FOR CEMENTERIES -----------------------------
    PZKZones.addZone("cementery", "ParkingStall", 12619, 3366, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- LV cementery 1
    PZKZones.addZone("cementery", "ParkingStall", 12607, 3226, 0, 3, 5, "N", PzkVanillaPlusCarPack) -- LV cementery 1
    PZKZones.addZone("cementery", "ParkingStall", 14536, 4969, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- LV cementery 2
    PZKZones.addZone("cementery", "ParkingStall", 11102, 6702, 0, 3, 5, "N", PzkVanillaPlusCarPack) -- WP cementery 
    PZKZones.addZone("cementery", "ParkingStall", 5719, 5346, 0, 3, 5, "N", PzkVanillaPlusCarPack) -- Riverside cementery 
    PZKZones.addZone("cementery", "ParkingStall", 3549, 11213, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- Echo Creek cementery 
    PZKZones.addZone("cementery", "ParkingStall", 451, 1246, 0, 3, 5, "N", PzkVanillaPlusCarPack) -- Ekron cementery 
    PZKZones.addZone("cementery", "ParkingStall", 2712, 13834, 0, 3, 5, "N", PzkVanillaPlusCarPack) -- New south town cementery 
    PZKZones.addZone("cementery", "ParkingStall", 1622, 5818, 0, 3, 5, "N", PzkVanillaPlusCarPack) -- Brandenburg cementery 
	PZKZones.addZone("cementery", "ParkingStall", 8366, 11397, 0, 5, 3, "W", PzkVanillaPlusCarPack) -- Rosewood cementery 

--------------------- MUSIC FESTIVALS MUSICIANS TOUR CARS ----------------------------------
    PZKZones.addZone("music_festival", "ParkingStall", 13703, 1946, 0, 5, 3, "W", PzkVanillaPlusCarPack)

-------------------ING FOR musician bands & tour buses near motels ------------------------------------
    PZKZones.addZone("musicians_parking", "ParkingStall", 6332, 5235, 0, 5, 3, "E", PzkVanillaPlusCarPack)



--------------------- BANK TRUCKS AND CONVOYENTS --------------------------------------------------------------
    PZKZones.addZone("bank", "ParkingStall", 6506, 5321, 0, 5, 3, "W", PzkVanillaPlusCarPack)  -- Riverside Bank
    PZKZones.addZone("bank", "ParkingStall", 11903, 6932, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- West Point Bank
    PZKZones.addZone("bank", "ParkingStall", 10602, 9729, 0, 5, 3, "W", PzkVanillaPlusCarPack) -- Muldraugh bank
    PZKZones.addZone("bank", "ParkingStall", 8058, 11624, 0, 5, 3, "W", PzkVanillaPlusCarPack) -- Rosewood bank
    PZKZones.addZone("bank", "ParkingStall", 12559, 1684, 0, 5, 3, "E", PzkVanillaPlusCarPack)  -- LV Bank
    PZKZones.addZone("bank", "ParkingStall", 12611, 1568, 0, 3, 5, "N", PzkVanillaPlusCarPack) -- LV bank 2
    PZKZones.addZone("bank", "ParkingStall", 13134, 2160, 0, 5, 3, "E", PzkVanillaPlusCarPack)  -- LV Bank 3
    PZKZones.addZone("bank", "ParkingStall", 13596, 3039, 0, 5, 3, "E", PzkVanillaPlusCarPack)  -- LV Bank 4
    PZKZones.addZone("bank", "ParkingStall", 13422, 1343, 0, 5, 3, "E", PzkVanillaPlusCarPack)  -- LV Bank 5
    PZKZones.addZone("bank", "ParkingStall", 12549, 322, 0, 3, 5, "S", PzkVanillaPlusCarPack) -- LV bank 6
    PZKZones.addZone("bank", "ParkingStall", 13668, 5749, 0, 3, 5, "S", PzkVanillaPlusCarPack) -- Crossroads bank 
    PZKZones.addZone("bank", "ParkingStall", 2087, 5846, 0, 5, 3, "E", PzkVanillaPlusCarPack)  -- Brandenburg

--------------------- NIGHTCLUB RICH GUESTS / MAFIA -----------------------------------------------------------
    PZKZones.addZone("nightclub", "ParkingStall", 12331, 1296, 0, 5, 3, "W", PzkVanillaPlusCarPack) -- LV
    PZKZones.addZone("nightclub", "ParkingStall", 12440, 1297, 0, 3, 5, "N", PzkVanillaPlusCarPack)
    PZKZones.addZone("nightclub", "ParkingStall", 12435, 1297, 0, 3, 5, "N", PzkVanillaPlusCarPack)
    PZKZones.addZone("nightclub", "ParkingStall", 12430, 1297, 0, 3, 5, "N", PzkVanillaPlusCarPack)
    PZKZones.addZone("nightclub", "ParkingStall", 12425, 1297, 0, 3, 5, "N", PzkVanillaPlusCarPack)
    PZKZones.addZone("nightclub", "ParkingStall", 12420, 1297, 0, 3, 5, "N", PzkVanillaPlusCarPack)
    PZKZones.addZone("nightclub", "ParkingStall", 12694, 6420, 0, 3, 5, "N", PzkVanillaPlusCarPack) -- Valley Station 
    PZKZones.addZone("nightclub", "ParkingStall", 12679, 6426, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- Valley Station

    PZKZones.addZone("churnrus", "ParkingStall", 6452, 5210, 0, 3, 5, "S", PzkVanillaPlusCarPack) -- Riverside
    PZKZones.addZone("churnrus", "ParkingStall", 13329, 2485, 0, 3, 5, "N", PzkVanillaPlusCarPack) -- LV park
    PZKZones.addZone("churnrus", "ParkingStall", 13643, 5778, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- LV Crossroads
    PZKZones.addZone("churnrus", "ParkingStall", 12678, 1123, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- LV Crossroads
    PZKZones.addZone("churnrus", "ParkingStall", 1868, 14789, 0, 3, 5, "N", PzkVanillaPlusCarPack) -- Irvingtown

    PZKZones.addZone("pizzawhirled", "ParkingStall", 13214, 2101, 0, 3, 5, "N", PzkVanillaPlusCarPack) -- LV
    PZKZones.addZone("pizzawhirled", "ParkingStall", 12484, 1342, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- LV
    PZKZones.addZone("pizzawhirled", "ParkingStall", 12625, 1870, 0, 3, 5, "N", PzkVanillaPlusCarPack) -- LV
    PZKZones.addZone("pizzawhirled", "ParkingStall", 12473, 1998, 0, 3, 5, "N", PzkVanillaPlusCarPack) -- LV
    PZKZones.addZone("pizzawhirled", "ParkingStall", 13531, 2121, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- LV
    PZKZones.addZone("pizzawhirled", "ParkingStall", 13183, 3032, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- LV
    PZKZones.addZone("pizzawhirled", "ParkingStall", 12437, 3844, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- LV
    PZKZones.addZone("pizzawhirled", "ParkingStall", 13486, 1403, 0, 3, 5, "S", PzkVanillaPlusCarPack) -- LV
    PZKZones.addZone("pizzawhirled", "ParkingStall", 10608, 10125, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- Muldraugh
    PZKZones.addZone("pizzawhirled", "ParkingStall", 8063, 11314, 0, 3, 5, "N", PzkVanillaPlusCarPack) -- Rosewood
    PZKZones.addZone("pizzawhirled", "ParkingStall", 11685, 7093, 0, 5, 3, "W", PzkVanillaPlusCarPack) -- WP

    PZKZones.addZone("foodtruckrandomspot", "ParkingStall", 13330, 3090, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- LV S park
    PZKZones.addZone("foodtruckrandomspot", "ParkingStall", 13322, 3090, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- LV S park
    PZKZones.addZone("foodtruckrandomspot", "ParkingStall", 2424, 14477, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- Irvingtown
    PZKZones.addZone("foodtruckrandomspot", "ParkingStall", 1751, 14817, 0, 3, 5, "N", PzkVanillaPlusCarPack) -- Irvingtown
    PZKZones.addZone("foodtruckrandomspot", "ParkingStall", 445, 9867, 0, 3, 5, "N", PzkVanillaPlusCarPack) -- Ekron
    PZKZones.addZone("foodtruckrandomspot", "ParkingStall", 1854, 6377, 0, 3, 5, "N", PzkVanillaPlusCarPack) -- Brandenburg
    PZKZones.addZone("foodtruckrandomspot", "ParkingStall", 6503, 5190, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- Riverside
    PZKZones.addZone("foodtruckrandomspot", "ParkingStall", 8115, 11495, 0, 3, 5, "N", PzkVanillaPlusCarPack) -- Rosewood
    PZKZones.addZone("foodtruckrandomspot", "ParkingStall", 10096, 12775, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- March Ridge
    PZKZones.addZone("foodtruckrandomspot", "ParkingStall", 12048, 6867, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- West Point
    PZKZones.addZone("foodtruckrandomspot", "ParkingStall", 12822, 6350, 0, 3, 5, "N", PzkVanillaPlusCarPack) -- Drag strip
    PZKZones.addZone("foodtruckrandomspot", "ParkingStall", 13963, 5924, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- Crossroads
    PZKZones.addZone("foodtruckrandomspot", "ParkingStall", 10623, 9933, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- Muldraugh
    PZKZones.addZone("foodtruckrandomspot", "ParkingStall", 12940, 1575, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- LV
    PZKZones.addZone("foodtruckrandomspot", "ParkingStall", 12228, 2614, 0, 3, 5, "N", PzkVanillaPlusCarPack) -- LV

--------------------- FIELD FARM EQUIPMENT TRACTORS, PLOWS ETC -------------------------------------------------
    PZKZones.addZone("field_farm", "ParkingStall", 7030, 5459, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- Riverside
    PZKZones.addZone("farm_trailers ", "ParkingStall", 7035, 5459, 0, 5, 3, "E", PzkVanillaPlusCarPack)

    PZKZones.addZone("field_farm", "ParkingStall", 5376, 10033, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- Doe Valley
    PZKZones.addZone("farm_trailers ", "ParkingStall", 5381, 10033, 0, 5, 3, "E", PzkVanillaPlusCarPack)

    PZKZones.addZone("field_farm", "ParkingStall", 9315, 8471, 0, 3, 5, "S", PzkVanillaPlusCarPack) -- near Muldraugh
    PZKZones.addZone("farm_trailers ", "ParkingStall", 9315, 8467, 0, 5, 3, "E", PzkVanillaPlusCarPack)
	
	
	-- Grand Ohio MallSecurity
    PZKZones.addZone("mall", "ParkingStall", 13774, 1218, 0, 5, 30, "W", PzkVanillaPlusCarPack)

    -- Valley Station MallSecurity
    PZKZones.addZone("mall", "ParkingStall", 13970, 5903, 0, 5, 24, "W", PzkVanillaPlusCarPack)
    PZKZones.addZone("mall", "ParkingStall", 13900, 5896, 0, 5, 24, "E", PzkVanillaPlusCarPack)

    -- South Louisville MallSecurity
    PZKZones.addZone("mall", "ParkingStall", 13336, 3088, 0, 66, 5, "N", PzkVanillaPlusCarPack)
	
	-- Brandenburg MallSecurity
  --  PZKZones.addZone("mall", "ParkingStall", 1850, 6325, 0, 21, 5, "N", PzkVanillaPlusCarPack)
	PZKZones.addZone("mall", "ParkingStall", 1850, 6331, 0, 21, 5, "S", PzkVanillaPlusCarPack)
	
	-- South LSU Security
    PZKZones.addZone("lsu", "ParkingStall", 12360, 2125, 0, 15, 5, "S", PzkVanillaPlusCarPack)
	PZKZones.addZone("lsu", "ParkingStall", 12374, 2185, 0, 15, 3, "W", PzkVanillaPlusCarPack)
	PZKZones.addZone("lsu", "ParkingStall", 12390, 2185, 0, 15, 3, "W", PzkVanillaPlusCarPack)
	PZKZones.addZone("lsu", "ParkingStall", 12352, 2260, 0, 3, 10, "S", PzkVanillaPlusCarPack)
	PZKZones.addZone("lsu", "ParkingStall", 12421, 2386, 0, 5, 15, "E", PzkVanillaPlusCarPack)
	
	-- MAFIA

-------------------ING FOR Mafia & gang cars ----------------------------------------
    PZKZones.addZone("mafia", "ParkingStall", 12632, 1345, 0, 3, 5, "S", PzkVanillaPlusCarPack)
	
	-- Valley station	
	PZKZones.addZone("mafia", "ParkingStall", 12686, 6429, 0, 5, 15, "E", PzkVanillaPlusCarPack) --Valley Station club
	PZKZones.addZone("mafia", "ParkingStall", 12850, 6400, 0, 6, 5, "N", PzkVanillaPlusCarPack) --Dragstrip
	
	--MuldLOL
	PZKZones.addZone("mafia", "ParkingStall", 10815, 10399, 0, 5, 15, "E", PzkVanillaPlusCarPack) --Muldraugh south workshop	
	PZKZones.addZone("mafia", "ParkingStall", 10882, 10034, 0, 5, 3, "E", PzkVanillaPlusCarPack) --Muldraugh Cortman	
	PZKZones.addZone("mafia", "ParkingStall", 10845, 9629, 0, 15, 3, "E", PzkVanillaPlusCarPack) --Muldraugh Junkyard	
	PZKZones.addZone("mafia", "ParkingStall", 10880, 10076, 0, 5, 3, "E", PzkVanillaPlusCarPack) --Muldraugh House
	
	PZKZones.addZone("mafia", "ParkingStall", 10631, 9360, 0, 6, 5, "S", PzkVanillaPlusCarPack) --Muldraugh Secure storage N
	PZKZones.addZone("mafia", "ParkingStall", 10330, 9655, 0, 5, 6, "W", PzkVanillaPlusCarPack) --Muldraugh MCCoy logging open storage	
	PZKZones.addZone("mafia", "ParkingStall", 11820, 9759, 0, 27, 5, "S", PzkVanillaPlusCarPack) --Abandoned Warehouses behind Muldlol
	PZKZones.addZone("mafia", "ParkingStall", 11518, 9633, 0, 27, 5, "S", PzkVanillaPlusCarPack) --Abandoned Warehouses behind Muldlol
	PZKZones.addZone("mafia", "ParkingStall", 11578, 9298, 0, 9, 5, "N", PzkVanillaPlusCarPack) --DrugLab
	
	--Westpoint
	PZKZones.addZone("mafia", "ParkingStall", 12069, 6808, 0, 3, 5, "N", PzkVanillaPlusCarPack) --Twiggys
	PZKZones.addZone("mafia", "ParkingStall", 11890, 6850, 0, 5, 6, "E", PzkVanillaPlusCarPack) --Restaurants
	
	--LV
	PZKZones.addZone("mafia", "ParkingStall", 12304, 1257, 0, 5, 9, "E", PzkVanillaPlusCarPack) --Velvet tassel club
	PZKZones.addZone("mafia", "ParkingStall", 12426, 1291, 0, 15, 3, "W", PzkVanillaPlusCarPack) --Behind FLO club
	PZKZones.addZone("mafia", "ParkingStall", 13298, 1215, 0, 5, 9, "E", PzkVanillaPlusCarPack) --Bats factory
	
	--Riverside
  	PZKZones.addZone("mafia", "ParkingStall", 5565, 6058, 0, 3, 15, "N", PzkVanillaPlusCarPack) --UstoreIt
	PZKZones.addZone("mafia", "ParkingStall", 3842, 6165, 0, 5, 25, "N", PzkVanillaPlusCarPack) --GCECORP
	PZKZones.addZone("mafia", "ParkingStall", 5714, 6422, 0, 15, 5, "S", PzkVanillaPlusCarPack) --Golf Club
	
	--Rosewood
	PZKZones.addZone("mafia", "ParkingStall", 8074, 11611, 0, 5, 9, "E", PzkVanillaPlusCarPack) --near bank
	PZKZones.addZone("mafia", "ParkingStall", 8021, 11450, 0, 9, 5, "S", PzkVanillaPlusCarPack) --near bar
	PZKZones.addZone("mafia", "ParkingStall", 8168, 11336, 0, 15, 3, "N", PzkVanillaPlusCarPack) --behind car repair shop
	
	--Irvington
	PZKZones.addZone("mafia", "ParkingStall", 866, 13018, 0, 5, 18, "E", PzkVanillaPlusCarPack) --speedway
	PZKZones.addZone("mafia", "ParkingStall", 2918, 12587, 0, 5, 9, "E", PzkVanillaPlusCarPack) --junkyard
	
	--Echo Creek
	PZKZones.addZone("mafia", "ParkingStall", 1915, 10776, 0, 20, 3, "W", PzkVanillaPlusCarPack) --steel mill
	PZKZones.addZone("mafia", "ParkingStall", 2608, 10917, 0, 5, 9, "E", PzkVanillaPlusCarPack) --Guns unlimited
	
	-- SEMI
	--Echo Creek
	PZKZones.addZone("semi", "ParkingStall", 3672, 10912, 0, 15, 5, "S", PzkVanillaPlusCarPack) --Logistics
	
	--West Point
	PZKZones.addZone("semi", "ParkingStall", 12038, 7125, 0, 5, 3, "W", PzkVanillaPlusCarPack) --TruckStop
	PZKZones.addZone("semi", "ParkingStall", 12038, 7129, 0, 5, 3, "W", PzkVanillaPlusCarPack) --TruckStop
	PZKZones.addZone("semi", "ParkingStall", 12038, 7133, 0, 5, 3, "W", PzkVanillaPlusCarPack) --TruckStop
	PZKZones.addZone("semi", "ParkingStall", 12038, 7137, 0, 5, 3, "W", PzkVanillaPlusCarPack) --TruckStop
	PZKZones.addZone("semi", "ParkingStall", 12038, 7141, 0, 5, 3, "W", PzkVanillaPlusCarPack) --TruckStop
	PZKZones.addZone("semi", "ParkingStall", 12038, 7145, 0, 5, 3, "W", PzkVanillaPlusCarPack) --TruckStop
	PZKZones.addZone("semi", "ParkingStall", 12038, 7153, 0, 5, 3, "W", PzkVanillaPlusCarPack) --TruckStop
	
	PZKZones.addZone("semi", "ParkingStall", 11984, 7117, 0, 5, 3, "E", PzkVanillaPlusCarPack) --TruckStop
	PZKZones.addZone("semi", "ParkingStall", 11984, 7121, 0, 5, 3, "E", PzkVanillaPlusCarPack) --TruckStop
	PZKZones.addZone("semi", "ParkingStall", 11984, 7125, 0, 5, 3, "E", PzkVanillaPlusCarPack) --TruckStop
	PZKZones.addZone("semi", "ParkingStall", 11984, 7129, 0, 5, 3, "E", PzkVanillaPlusCarPack) --TruckStop
	PZKZones.addZone("semi", "ParkingStall", 11984, 7133, 0, 5, 3, "E", PzkVanillaPlusCarPack) --TruckStop
	
	
	
	-- SEMI TRAILERS
	
	

PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 11853, 9801, 0, 23, 5, "N", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 11861, 9774, 0, 15, 5, "S", PzkVanillaPlusCarPack) 

-- T Mul JD
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 11730, 9616, 0, 4, 5, "E", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 11713, 9616, 0, 4, 5, "W", PzkVanillaPlusCarPack) 

-- T Dixie Camp
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 11544, 8805, 0, 3, 5, "S", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 11550, 8805, 0, 3, 5, "S", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 11557, 8805, 0, 3, 5, "S", PzkVanillaPlusCarPack) 
--PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 11578, 8805, 0, 3, 5, "S", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 11572, 8840, 0, 3, 5, "N", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 11585, 8840, 0, 3, 5, "N", PzkVanillaPlusCarPack) 

-- Dixie
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 11613, 8238, 0, 4, 5, "E", PzkVanillaPlusCarPack) 

-- Muldlol
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 11013, 9507, 0, 4, 5, "W", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 10623, 9424, 0, 4, 5, "W", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 10639, 10597, 0, 4, 5, "W", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 10753, 10319, 0, 4, 5, "W", PzkVanillaPlusCarPack) 

-- Muldlol McCoy Log Corp
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 10262, 9314, 0, 23, 5, "N", PzkVanillaPlusCarPack) 

-- Muldlol Lumber Mill
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 10352, 9654, 0, 4, 5, "W", PzkVanillaPlusCarPack) 

-- March Warehouses
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 10261, 10985, 0, 4, 5, "W", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 9209, 11817, 0, 12, 5, "S", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 10277, 7786, 0, 3, 5, "N", PzkVanillaPlusCarPack) 

-- Rosewood

PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 8309, 11601, 0, 3, 5, "N", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 5569, 12447, 0, 12, 5, "N", PzkVanillaPlusCarPack) 

PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 7978, 11284, 0, 5, 3, "W", PzkVanillaPlusCarPack) -- behind market

-- Prison
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 7697, 11799, 0, 5, 3, "E", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 7697, 11806, 0, 5, 3, "E", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 7697, 11813, 0, 5, 3, "E", PzkVanillaPlusCarPack) 


-- Riverside
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 7235, 8358, 0, 4, 5, "E", PzkVanillaPlusCarPack) 
--PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 6094, 5321, 0, 10, 5, "N", PzkVanillaPlusCarPack) 

-- Factory
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 5558, 5963, 0, 4, 5, "E", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 5629, 5928, 0, 4, 5, "W", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 3692, 8511, 0, 4, 5, "E", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 6553, 8967, 0, 24, 5, "N", PzkVanillaPlusCarPack) 


-- West Point
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 12019, 7124, 0, 5, 3, "E", PzkVanillaPlusCarPack)  -- Truck stop
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 12019, 7136, 0, 5, 3, "E", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 12019, 7140, 0, 5, 3, "E", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 12019, 7144, 0, 5, 3, "E", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 12019, 7148, 0, 5, 3, "E", PzkVanillaPlusCarPack) 

PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 12010, 7124, 0, 5, 3, "W", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 12010, 7128, 0, 5, 3, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 12010, 7132, 0, 5, 3, "W", PzkVanillaPlusCarPack)  
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 12010, 7140, 0, 5, 3, "W", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 12010, 7144, 0, 5, 3, "W", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 12010, 7148, 0, 5, 3, "W", PzkVanillaPlusCarPack) 

PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 12130, 7057, 0, 5, 3, "E", PzkVanillaPlusCarPack) --Factory
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 12144, 7057, 0, 5, 3, "E", PzkVanillaPlusCarPack) --Factory

PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 12038, 6844, 0, 3, 5, "N", PzkVanillaPlusCarPack) --Megamart delivery

-- Valley Station
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 13966, 5762, 0, 20, 5, "N", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 14001, 5772, 0, 4, 5, "W", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 14111, 5439, 0, 20, 5, "S", PzkVanillaPlusCarPack) 

-- Louisville
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 12621, 4310, 0, 4, 5, "W", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 12834, 4422, 0, 10, 5, "S", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 12609, 3812, 0, 4, 5, "E", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 12609, 3798, 0, 4, 5, "E", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 12623, 3798, 0, 4, 5, "W", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 12623, 3805, 0, 4, 5, "W", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 12535, 2447, 0, 40, 5, "N", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 12793, 2352, 0, 10, 5, "S", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 12693, 2573, 0, 4, 5, "W", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 13434, 3025, 0, 4, 5, "W", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 12072, 1282, 0, 14, 5, "N", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 12042, 1918, 0, 4, 5, "E", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 12042, 1923, 0, 4, 5, "E", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 12042, 1928, 0, 4, 5, "E", PzkVanillaPlusCarPack) 
PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 12086, 2081, 0, 4, 5, "E", PzkVanillaPlusCarPack) 


	--Echo Creek
	PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 3712, 10905, 0, 5, 6, "W", PzkVanillaPlusCarPack) --Logistics
	PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 3712, 10885, 0, 5, 6, "W", PzkVanillaPlusCarPack) --Logistics
	
	PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 4218, 11514, 0, 5, 3, "W", PzkVanillaPlusCarPack) --Double warehouse
	PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 4226, 11514, 0, 5, 3, "W", PzkVanillaPlusCarPack) --Double warehouse
	PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 4234, 11514, 0, 5, 3, "W", PzkVanillaPlusCarPack) --Double warehouse
	
	--Irvington
	PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 2865, 14438, 0, 3, 5, "S", PzkVanillaPlusCarPack) --Hay
	PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 2865, 14446, 0, 3, 5, "S", PzkVanillaPlusCarPack) --Hay
	PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 2865, 14454, 0, 3, 5, "S", PzkVanillaPlusCarPack) --Hay
	PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 2865, 14462, 0, 3, 5, "S", PzkVanillaPlusCarPack) --Hay
	
	PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 2985, 14432, 0, 5, 3, "W", PzkVanillaPlusCarPack) --Hay
	PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 2985, 14439, 0, 5, 3, "W", PzkVanillaPlusCarPack) --Hay
	PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 2985, 14453, 0, 5, 3, "W", PzkVanillaPlusCarPack) --Hay
	PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 2985, 14460, 0, 5, 3, "W", PzkVanillaPlusCarPack) --Hay
	PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 2985, 14474, 0, 5, 3, "W", PzkVanillaPlusCarPack) --Hay
	PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 2985, 14481, 0, 5, 3, "W", PzkVanillaPlusCarPack) --Hay
	
	--County
	PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 6771, 10032, 0, 3, 5, "S", PzkVanillaPlusCarPack) -- 6 warehouses
	PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 6771, 10042, 0, 3, 5, "S", PzkVanillaPlusCarPack) -- 6 warehouses
	
	PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 5892, 9855, 0, 3, 5, "S", PzkVanillaPlusCarPack) -- 2 warehouses
	PZKZones.addZone("bigtrailerparkinglot", "ParkingStall", 5925, 9861, 0, 3, 5, "S", PzkVanillaPlusCarPack) -- 2 warehouses
	
	-- SCHOOL BUSSES
	--Irvington
	PZKZones.addZone("schoolbus", "ParkingStall", 2272, 14397, 0, 3, 5, "S", PzkVanillaPlusCarPack) --School
	PZKZones.addZone("schoolbus", "ParkingStall", 2300, 14370, 0, 5, 3, "E", PzkVanillaPlusCarPack) --School
	--Ekron
	PZKZones.addZone("schoolbus", "ParkingStall", 761, 9858, 0, 5, 3, "W", PzkVanillaPlusCarPack) --School
	PZKZones.addZone("schoolbus", "ParkingStall", 773, 9858, 0, 5, 3, "W", PzkVanillaPlusCarPack) --School
	-- Brandenburg
	PZKZones.addZone("schoolbus", "ParkingStall", 2086, 6182, 0, 3, 5, "S", PzkVanillaPlusCarPack) --School
	PZKZones.addZone("schoolbus", "ParkingStall", 2086, 6192, 0, 3, 5, "S", PzkVanillaPlusCarPack) --School
	--Riverside
	PZKZones.addZone("schoolbus", "ParkingStall", 6472, 5463, 0, 3, 5, "S", PzkVanillaPlusCarPack) --School
	PZKZones.addZone("schoolbus", "ParkingStall", 6487, 5463, 0, 5, 3, "W", PzkVanillaPlusCarPack) --School
	PZKZones.addZone("schoolbus", "ParkingStall", 6500, 5463, 0, 5, 3, "W", PzkVanillaPlusCarPack) --School
	--Rosewood
	PZKZones.addZone("schoolbus", "ParkingStall", 8321, 11585, 0, 5, 3, "W", PzkVanillaPlusCarPack) --School
	PZKZones.addZone("schoolbus", "ParkingStall", 8331, 11585, 0, 5, 3, "W", PzkVanillaPlusCarPack) --School
	PZKZones.addZone("schoolbus", "ParkingStall", 8341, 11585, 0, 5, 3, "W", PzkVanillaPlusCarPack) --School
	--West Point
	PZKZones.addZone("schoolbus", "ParkingStall", 11390, 6792, 0, 3, 5, "S", PzkVanillaPlusCarPack) --School
	PZKZones.addZone("schoolbus", "ParkingStall", 11349, 6758, 0, 5, 3, "W", PzkVanillaPlusCarPack) --School
	PZKZones.addZone("schoolbus", "ParkingStall", 11359, 6758, 0, 5, 3, "W", PzkVanillaPlusCarPack) --School
	PZKZones.addZone("schoolbus", "ParkingStall", 11369, 6758, 0, 5, 3, "W", PzkVanillaPlusCarPack) --School
	--Muldlol
	PZKZones.addZone("schoolbus", "ParkingStall", 10639, 10012, 0, 3, 5, "S", PzkVanillaPlusCarPack) --School
	--Valley station
	PZKZones.addZone("schoolbus", "ParkingStall", 12878, 4858, 0, 3, 5, "S", PzkVanillaPlusCarPack) --School
	PZKZones.addZone("schoolbus", "ParkingStall", 12878, 4868, 0, 3, 5, "S", PzkVanillaPlusCarPack) --School
	
	
	-- BUSSES ON ROADS
	PZKZones.addZone("busservice", "ParkingStall", 5508, 9629, 0, 5, 3, "W", PzkVanillaPlusCarPack) -- DOE VALLEY bus stop
	PZKZones.addZone("busservice", "ParkingStall", 6297, 5279, 0, 5, 3, "W", PzkVanillaPlusCarPack) -- Riverside bus stop
	PZKZones.addZone("busservice", "ParkingStall", 11918, 6941, 0, 3, 5, "S", PzkVanillaPlusCarPack) --West Point bus stop
	PZKZones.addZone("busservice", "ParkingStall", 8103, 11557, 0, 3, 5, "S", PzkVanillaPlusCarPack) --Rosewood bus stop
	PZKZones.addZone("busservice", "ParkingStall", 10739, 10598, 0, 3, 5, "N", PzkVanillaPlusCarPack) --Muldlol bus stop
	PZKZones.addZone("busservice", "ParkingStall", 10597, 9752, 0, 3, 5, "N", PzkVanillaPlusCarPack) --Muldlol bus on road
	PZKZones.addZone("busservice", "ParkingStall", 13674, 5864, 0, 3, 5, "S", PzkVanillaPlusCarPack) --Crossroad mall bus station
	PZKZones.addZone("busservice", "ParkingStall", 13674, 5895, 0, 3, 5, "S", PzkVanillaPlusCarPack) --Crossroad mall bus station
	
	PZKZones.addZone("busservice", "ParkingStall", 800, 11816, 0, 3, 5, "N", PzkVanillaPlusCarPack) --Irvington train station
	PZKZones.addZone("busservice", "ParkingStall", 451, 9832, 0, 3, 5, "N", PzkVanillaPlusCarPack) --Ekron bus stop
	
	PZKZones.addZone("busservice", "ParkingStall", 15597, 3276, 0, 3, 5, "S", PzkVanillaPlusCarPack) --LV Airport bus stop
	PZKZones.addZone("busservice", "ParkingStall", 15327, 3268, 0, 3, 5, "N", PzkVanillaPlusCarPack) --LV Airport bus stop
	PZKZones.addZone("busservice", "ParkingStall", 15386, 3109, 0, 5, 3, "E", PzkVanillaPlusCarPack) --LV Airport bus stop
	
	--Louisville
	PZKZones.addZone("busservice", "ParkingStall", 13977, 3260, 0, 3, 5, "S", PzkVanillaPlusCarPack) --LV East bus stop
	
	
	-- BUS Stations
	PZKZones.addZone("busstation", "ParkingStall", 8276, 12211, 0, 5, 3, "W", PzkVanillaPlusCarPack) --Rosewood
	PZKZones.addZone("busstation", "ParkingStall", 8249, 12198, 0, 5, 3, "E", PzkVanillaPlusCarPack) --Rosewood
	
	PZKZones.addZone("busstation", "ParkingStall", 12730, 5734, 0, 3, 5, "N", PzkVanillaPlusCarPack) --Valley Station
	PZKZones.addZone("busstation", "ParkingStall", 12720, 5745, 0, 5, 3, "E", PzkVanillaPlusCarPack) --Valley Station
	PZKZones.addZone("busstation", "ParkingStall", 12709, 5724, 0, 3, 5, "N", PzkVanillaPlusCarPack) --Valley Station
	PZKZones.addZone("busstation", "ParkingStall", 12709, 5712, 0, 3, 5, "N", PzkVanillaPlusCarPack) --Valley Station
	
	-- Public Works
    PZKZones.addZone("public_works", "ParkingStall", 10854, 9990, 0, 3, 5, "N", PzkVanillaPlusCarPack) --Utility Shed - Muldraugh
    PZKZones.addZone("public_works", "ParkingStall", 10788, 10223, 0, 5, 3, "W", PzkVanillaPlusCarPack) --Utility Shed - Muldraugh

    PZKZones.addZone("public_works", "ParkingStall", 674, 9872, 0, 5, 3, "W", PzkVanillaPlusCarPack) --Utility Shed - Ekron
    PZKZones.addZone("public_works", "ParkingStall", 455, 9874, 0, 3, 5, "N", PzkVanillaPlusCarPack) --Utility Shed - Ekron

    PZKZones.addZone("public_works", "ParkingStall", 4508, 6068, 0, 5, 3, "E", PzkVanillaPlusCarPack) --Utility Shed - Rural

    PZKZones.addZone("public_works", "ParkingStall", 10385, 10074, 0, 5, 3, "E", PzkVanillaPlusCarPack) --Muldraugh Substation
    PZKZones.addZone("public_works", "ParkingStall", 10392, 10065, 0, 3, 5, "N", PzkVanillaPlusCarPack) --Muldraugh Substation

    PZKZones.addZone("public_works", "ParkingStall", 14731, 4071, 0, 5, 3, "W", PzkVanillaPlusCarPack) --Louisville Substation
    PZKZones.addZone("public_works", "ParkingStall", 14731, 4076, 0, 5, 3, "W", PzkVanillaPlusCarPack) --Louisville Substation

    PZKZones.addZone("public_works", "ParkingStall", 800, 11754, 0, 3, 5, "S", PzkVanillaPlusCarPack) --Rural Railyard

    PZKZones.addZone("public_works", "ParkingStall", 1907, 10923, 0, 3, 5, "S", PzkVanillaPlusCarPack) --Steel Mill
    PZKZones.addZone("public_works", "ParkingStall", 1917, 10923, 0, 3, 5, "N", PzkVanillaPlusCarPack) --Steel Mill

    PZKZones.addZone("public_works", "ParkingStall", 5974, 6504, 0, 3, 5, "N", PzkVanillaPlusCarPack) --Riverside Country Club

    PZKZones.addZone("public_works", "ParkingStall", 5595, 5916, 0, 3, 5, "N", PzkVanillaPlusCarPack) --Riverside Warehouse Complex
    PZKZones.addZone("public_works", "ParkingStall", 5595, 5916, 0, 3, 5, "N", PzkVanillaPlusCarPack) --Riverside Warehouse Complex
    PZKZones.addZone("public_works", "ParkingStall", 5562, 5945, 0, 5, 3, "W", PzkVanillaPlusCarPack) --Riverside Warehouse Complex
    PZKZones.addZone("public_works", "ParkingStall", 5530, 5865, 0, 5, 3, "E", PzkVanillaPlusCarPack) --Riverside Warehouse Complex

    PZKZones.addZone("public_works", "ParkingStall", 10300, 9361, 0, 5, 3, "W", PzkVanillaPlusCarPack) --Muldraugh McCoy

    PZKZones.addZone("public_works", "ParkingStall", 12127, 7112, 0, 5, 3, "E", PzkVanillaPlusCarPack) --Westpoint Warehouse

    PZKZones.addZone("public_works", "ParkingStall", 12717, 6384, 0, 3, 5, "N", PzkVanillaPlusCarPack) --Valley Station Strip Club

    PZKZones.addZone("public_works", "ParkingStall", 12602, 4708, 0, 3, 5, "N", PzkVanillaPlusCarPack) --Valley Station Warehouse
    PZKZones.addZone("public_works", "ParkingStall", 12597, 4728, 0, 5, 3, "E", PzkVanillaPlusCarPack) --Valley Station Warehouse

    PZKZones.addZone("public_works", "ParkingStall", 12562, 3642, 0, 5, 3, "E", PzkVanillaPlusCarPack) --Louisville Warehouse
    PZKZones.addZone("public_works", "ParkingStall", 12562, 3650, 0, 5, 3, "W", PzkVanillaPlusCarPack) --Louisville Warehouse

    PZKZones.addZone("public_works", "ParkingStall", 12048, 1514, 0, 5, 3, "W", PzkVanillaPlusCarPack) --Gas 2 Go Refinery
    PZKZones.addZone("public_works", "ParkingStall", 12106, 1439, 0, 3, 5, "N", PzkVanillaPlusCarPack) --Gas 2 Go Refinery

    PZKZones.addZone("public_works", "ParkingStall", 8031, 15273, 0, 3, 5, "S", PzkVanillaPlusCarPack) --Water Treatment Plant
    PZKZones.addZone("public_works", "ParkingStall", 8024, 15352, 0, 3, 5, "S", PzkVanillaPlusCarPack) --Water Treatment Plant


	-- waterservice
	-- wasteservice
	-- fueltanker
	-- construction
	-- racecar
	-- SonsAnarchyMC
	-- BarrelDogsMC
	-- WildRaccoonsMC
	-- IronRodentMC
	
	
	------------------------------PROJECT INDIANA SUPPORT-------------------------------------
	PZKZones.addZone("rail", "ParkingStall", 2130, 6632, 0, 5, 3, "E", PzkVanillaPlusCarPack) --Brandenburg destroyed railstation
	PZKZones.addZone("rail", "ParkingStall", 2132, 6644, 0, 5, 3, "E", PzkVanillaPlusCarPack) --Brandenburg destroyed railstation
	PZKZones.addZone("rail", "ParkingStall", 2152, 6615, 0, 30, 5, "N", PzkVanillaPlusCarPack) --Brandenburg destroyed railstation
	PZKZones.addZone("rail", "ParkingStall", 2158, 6626, 0, 24, 5, "S", PzkVanillaPlusCarPack) --Brandenburg destroyed railstation
	PZKZones.addZone("rail", "ParkingStall", 2173, 6633, 0, 5, 3, "E", PzkVanillaPlusCarPack) --Brandenburg destroyed railstation
	PZKZones.addZone("rail", "ParkingStall", 2136, 6698, 0, 5, 3, "E", PzkVanillaPlusCarPack) --Brandenburg destroyed railstation
	PZKZones.addZone("rail", "ParkingStall", 2114, 6739, 0, 5, 3, "E", PzkVanillaPlusCarPack) --Brandenburg destroyed railstation
	PZKZones.addZone("rail", "ParkingStall", 1979, 6852, 0, 3, 15, "S", PzkVanillaPlusCarPack) --Brandenburg destroyed tracks
	
	PZKZones.addZone("rail", "ParkingStall", 12589, 4438, 0, 5, 18, "E", PzkVanillaPlusCarPack) --LV rail offices
	
	PZKZones.addZone("rail", "ParkingStall", 12678, 2612, 0, 21, 5, "S", PzkVanillaPlusCarPack) --LV rail station
	
	PZKZones.addZone("rail", "ParkingStall", 539, 9885, 0, 3, 5, "S", PzkVanillaPlusCarPack) --Ekron destroyed tracks
	PZKZones.addZone("rail", "ParkingStall", 482, 9895, 0, 5, 3, "E", PzkVanillaPlusCarPack) --Ekron destroyed tracks
	
	PZKZones.addZone("rail", "ParkingStall", 11720, 9954, 0, 18, 5, "S", PzkVanillaPlusCarPack) --Muldlol railyard
	
	----------------------------------------------------------------- FIXES TO VANILLA MAP ----------------------------------------------------------------
	-- Rosewood 
	PZKZones.addZone("", "ParkingStall", 8161, 11488, 0, 5, 21, "E", PzkVanillaPlusCarPack) --behind supermarket
	
	
	-- DOE VALLEY GOLF CLUB - HUGE PARKING
	PZKZones.addZone("sport", "ParkingStall", 5734, 6601, 0, 24, 4, "N", PzkVanillaPlusCarPack)
	PZKZones.addZone("medium", "ParkingStall", 5734, 6610, 0, 24, 4, "S", PzkVanillaPlusCarPack)
	PZKZones.addZone("good", "ParkingStall", 5734, 6616, 0, 24, 4, "N", PzkVanillaPlusCarPack)
	PZKZones.addZone("", "ParkingStall", 5734, 6625, 0, 24, 4, "S", PzkVanillaPlusCarPack)
	PZKZones.addZone("medium", "ParkingStall", 5734, 6631, 0, 24, 4, "N", PzkVanillaPlusCarPack)
	
	PZKZones.addZone("good", "ParkingStall", 5769, 6601, 0, 24, 4, "N", PzkVanillaPlusCarPack)
	PZKZones.addZone("sport", "ParkingStall", 5769, 6610, 0, 24, 4, "S", PzkVanillaPlusCarPack)
	PZKZones.addZone("sport", "ParkingStall", 5769, 6616, 0, 24, 4, "N", PzkVanillaPlusCarPack)
	PZKZones.addZone("medium", "ParkingStall", 5769, 6625, 0, 24, 4, "S", PzkVanillaPlusCarPack)
	PZKZones.addZone("good", "ParkingStall", 5769, 6631, 0, 24, 4, "N", PzkVanillaPlusCarPack)
	

	
	-- Muldraugh Police Parking
	PZKZones.addZone("police", "ParkingStall", 10650, 10432, 0, 5, 3, "E", PzkVanillaPlusCarPack)  -- Muldlol Police parking
	PZKZones.addZone("police", "ParkingStall", 10650, 10435, 0, 5, 3, "E", PzkVanillaPlusCarPack)  -- Muldlol Police parking
	PZKZones.addZone("police", "ParkingStall", 10650, 10438, 0, 5, 3, "E", PzkVanillaPlusCarPack)  -- Muldlol Police parking
	PZKZones.addZone("police", "ParkingStall", 10650, 10441, 0, 5, 3, "E", PzkVanillaPlusCarPack)  -- Muldlol Police parking
	PZKZones.addZone("police", "ParkingStall", 10665, 10418, 0, 5, 3, "E", PzkVanillaPlusCarPack)  -- Muldlol Police parking
	PZKZones.addZone("police", "ParkingStall", 10665, 10421, 0, 5, 3, "E", PzkVanillaPlusCarPack)  -- Muldlol Police parking
	PZKZones.addZone("police", "ParkingStall", 10665, 10424, 0, 5, 3, "E", PzkVanillaPlusCarPack)  -- Muldlol Police parking
	PZKZones.addZone("police", "ParkingStall", 10665, 10427, 0, 5, 3, "E", PzkVanillaPlusCarPack)  -- Muldlol Police parking
	
	-- LV Large Apartment Complex garage
	PZKZones.addZone("good", "ParkingStall", 12772, 1832, 0, 9, 5, "N", PzkVanillaPlusCarPack)
	PZKZones.addZone("sport", "ParkingStall", 12788, 1832, 0, 15, 5, "N", PzkVanillaPlusCarPack)
	PZKZones.addZone("sport", "ParkingStall", 12784, 1832, 0, 3, 5, "N", PzkVanillaPlusCarPack)
	PZKZones.addZone("sport", "ParkingStall", 12772, 1843, 0, 15, 5, "S", PzkVanillaPlusCarPack)
	PZKZones.addZone("good", "ParkingStall", 12788, 1843, 0, 15, 5, "S", PzkVanillaPlusCarPack)
	PZKZones.addZone("good", "ParkingStall", 12772, 1849, 0, 15, 5, "N", PzkVanillaPlusCarPack)
	PZKZones.addZone("sport", "ParkingStall", 12788, 1849, 0, 15, 5, "N", PzkVanillaPlusCarPack)
	PZKZones.addZone("good", "ParkingStall", 12772, 1860, 0, 15, 5, "S", PzkVanillaPlusCarPack)
	PZKZones.addZone("sport", "ParkingStall", 12788, 1860, 0, 15, 5, "S", PzkVanillaPlusCarPack)


	-- MARCH RIDGE VANILLA ARMY SPAWNS
PZKZones.addZone("armymr", "ParkingStall", 9833, 12827, 0, 5, 17, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9833, 12808, 0, 5, 9, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9833, 12786, 0, 5, 11, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9833, 12772, 0, 5, 10, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9834, 12715, 0, 5, 10, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9834, 12737, 0, 5, 10, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9834, 12686, 0, 5, 13, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9834, 12670, 0, 5, 9, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9834, 12642, 0, 5, 10, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9834, 12625, 0, 5, 9, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9880, 12685, 0, 5, 3, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9882, 12659, 0, 5, 3, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9881, 12653, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9883, 12626, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9882, 12621, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10009, 12917, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10014, 12917, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10041, 12917, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10009, 12882, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10015, 12878, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10042, 12873, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10031, 12852, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10029, 12952, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10006, 12951, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10008, 12956, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10032, 12957, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10029, 12984, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10033, 12990, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10031, 13016, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10033, 13021, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10031, 13048, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10034, 13053, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10030, 13080, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10034, 13086, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10031, 13112, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10033, 13117, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10008, 13116, 0, 4, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10004, 13111, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10006, 13084, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10004, 13079, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10006, 13052, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10005, 13047, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10006, 13020, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10005, 13015, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9969, 12991, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9964, 12993, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9969, 12956, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9964, 12951, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9937, 12949, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9932, 12950, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9905, 12951, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9900, 12950, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9873, 12953, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9963, 12916, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9968, 12917, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9936, 12914, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9931, 12915, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9904, 12916, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9899, 12914, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9872, 12915, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9855, 12917, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9828, 12917, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9823, 12917, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9821, 12879, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9827, 12877, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9854, 12874, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9873, 12994, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9900, 12994, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9904, 12995, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9909, 13016, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9909, 13043, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9910, 13048, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9909, 13075, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9911, 13080, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9907, 13107, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9911, 13112, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9881, 13107, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9883, 13112, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9883, 13080, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9945, 13112, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9943, 13107, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9969, 13113, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9967, 13108, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9969, 13081, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9967, 13076, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9968, 13050, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9968, 13044, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9970, 13017, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9944, 13015, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9943, 13042, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9944, 13047, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9942, 13075, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9945, 13080, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9937, 12991, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10249, 12626, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10265, 12627, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10277, 12626, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10290, 12626, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10300, 12627, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10325, 12628, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10322, 12672, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10306, 12672, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10292, 12673, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10276, 12673, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10262, 12672, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10245, 12673, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10352, 12666, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10353, 12643, 0, 3, 5, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10352, 12632, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10352, 12695, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10352, 12709, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10351, 12725, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10320, 12686, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10306, 12685, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10276, 12686, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10291, 12685, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10319, 12734, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10306, 12733, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10288, 12733, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10275, 12733, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10264, 12734, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10248, 12734, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10248, 12685, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10261, 12686, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10369, 12647, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10370, 12664, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10403, 12670, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10419, 12672, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10370, 12696, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10404, 12686, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10418, 12686, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10432, 12686, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10433, 12671, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10452, 12672, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10452, 12687, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10464, 12732, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10450, 12732, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10433, 12732, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10416, 12732, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10402, 12732, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10370, 12725, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10371, 12710, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10371, 12756, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10370, 12771, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10370, 12788, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10311, 12760, 0, 30, 5, "N", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10321, 12774, 0, 21, 5, "S", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10204, 12783, 0, 5, 21, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10204, 12769, 0, 5, 9, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10204, 12755, 0, 5, 6, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10204, 12739, 0, 5, 9, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10204, 12711, 0, 5, 9, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10204, 12699, 0, 5, 6, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10204, 12677, 0, 5, 9, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10204, 12656, 0, 5, 15, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10204, 12644, 0, 5, 9, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10422, 12833, 0, 6, 5, "N", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10443, 12833, 0, 9, 5, "N", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10457, 12833, 0, 6, 5, "N", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10431, 12833, 0, 6, 5, "N", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10472, 12833, 0, 18, 5, "N", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10416, 12771, 0, 12, 5, "N", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10436, 12771, 0, 12, 5, "N", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10452, 12771, 0, 14, 5, "N", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10475, 12771, 0, 12, 5, "N", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9950, 12681, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9945, 12675, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9918, 12680, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9913, 12672, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9916, 12709, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9937, 12707, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9964, 12718, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9969, 12720, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9971, 12754, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9975, 12759, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9969, 12786, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9973, 12791, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9968, 12818, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9973, 12823, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9970, 12850, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10006, 12852, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9945, 12755, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9946, 12760, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9945, 12787, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9945, 12792, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9945, 12819, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9945, 12824, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9945, 12851, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9964, 12874, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9969, 12878, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9937, 12877, 0, 3, 5, "N",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9909, 12736, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9913, 12741, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9910, 12768, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9914, 12773, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9912, 12800, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9915, 12805, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9910, 12832, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9912, 12837, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9910, 12864, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9912, 12869, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9882, 12863, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9882, 12868, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9882, 12836, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9882, 12831, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9884, 12804, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9882, 12799, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9884, 12772, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9883, 12767, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9884, 12740, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9884, 12735, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 9886, 12708, 0, 5, 3, "W",PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10188, 12755, 0, 5, 18, "E", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10188, 12739, 0, 5, 15, "E", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10175, 12739, 0, 5, 24, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10147, 12717, 0, 5, 24, "E", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10140, 12733, 0, 5, 9, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10140, 12717, 0, 5, 9, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10124, 12737, 0, 15, 5, "S", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10125, 12723, 0, 6, 5, "N", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10114, 12730, 0, 5, 9, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10095, 12818, 0, 33, 5, "S", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10103, 12809, 0, 18, 5, "N", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10095, 12783, 0, 5, 18, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10132, 12794, 0, 9, 5, "N", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10148, 12794, 0, 6, 5, "N", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10036, 12773, 0, 5, 48, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10045, 12780, 0, 5, 30, "E", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10051, 12780, 0, 5, 30, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10060, 12773, 0, 5, 48, "E", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10065, 12668, 0, 12, 5, "N", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10065, 12678, 0, 18, 5, "S", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10093, 12667, 0, 36, 5, "S", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10138, 12625, 0, 5, 45, "E", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10102, 12656, 0, 27, 5, "N", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10102, 12651, 0, 27, 5, "S", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10099, 12640, 0, 24, 5, "N", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10125, 12613, 0, 5, 27, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10018, 12658, 0, 5, 12, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10034, 12643, 0, 5, 30, "E", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10181, 12668, 0, 5, 18, "W", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10191, 12670, 0, 5, 21, "E", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10196, 12633, 0, 5, 33, "E", PzkVanillaPlusCarPack)
PZKZones.addZone("armymr", "ParkingStall", 10188, 12633, 0, 5, 24, "W", PzkVanillaPlusCarPack)

  
 ----------------------------------RANGER-------------------------------------------------
PZKZones.addZone("ranger", "ParkingStall", 4852, 6270, 0, 6, 5, "N", PzkVanillaPlusCarPack) -- radio station doe
PZKZones.addZone("ranger", "ParkingStall", 3736, 6356, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- near train tracks sanatorium
PZKZones.addZone("ranger", "ParkingStall", 3452, 8304, 0, 5, 6, "E", PzkVanillaPlusCarPack) -- coalfield
PZKZones.addZone("ranger", "ParkingStall", 2401, 9869, 0, 3, 5, "N", PzkVanillaPlusCarPack) -- road to orphanage
PZKZones.addZone("ranger", "ParkingStall", 4688, 8605, 0, 5, 12, "E", PzkVanillaPlusCarPack) -- Deerhead RS
PZKZones.addZone("ranger", "ParkingStall", 4621, 8606, 0, 5, 6, "E", PzkVanillaPlusCarPack) -- Deerhead RS
PZKZones.addZone("ranger", "ParkingStall", 1925, 11319, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- lake near farms
PZKZones.addZone("ranger", "ParkingStall", 3156, 12147, 0, 12, 5, "N", PzkVanillaPlusCarPack) -- animal shelter
PZKZones.addZone("ranger", "ParkingStall", 12403, 8879, 0, 6, 5, "N", PzkVanillaPlusCarPack) -- Muldlol camping ground
PZKZones.addZone("ranger", "ParkingStall", 12034, 7394, 0, 9, 5, "N", PzkVanillaPlusCarPack) -- WP picnick area
PZKZones.addZone("ranger", "ParkingStall", 13742, 6686, 0, 5, 6, "E", PzkVanillaPlusCarPack) -- Camp Valley Station
PZKZones.addZone("ranger", "ParkingStall", 13260, 5462, 0, 5, 6, "E", PzkVanillaPlusCarPack) -- Valley Station Shooting range
PZKZones.addZone("ranger", "ParkingStall", 13112, 5312, 0, 6, 5, "N", PzkVanillaPlusCarPack) -- VS hunting lodge
PZKZones.addZone("ranger", "ParkingStall", 9656, 10162, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- Remote logging containers Muldlol
PZKZones.addZone("ranger", "ParkingStall", 10272, 8756, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- Radio tower Muldlol
PZKZones.addZone("ranger", "ParkingStall", 10080, 8198, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- MCCoy res
PZKZones.addZone("ranger", "ParkingStall", 8113, 7493, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- River
PZKZones.addZone("ranger", "ParkingStall", 5917, 5226, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- Riverside fishing spot
PZKZones.addZone("ranger", "ParkingStall", 3683, 5781, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- Riverside fishing spot2
PZKZones.addZone("ranger", "ParkingStall", 5473, 9519, 0, 3, 5, "N", PzkVanillaPlusCarPack) -- Doe surplus
PZKZones.addZone("ranger", "ParkingStall", 5016, 8016, 0, 6, 5, "N", PzkVanillaPlusCarPack) -- Busy Beaver camp
PZKZones.addZone("ranger", "ParkingStall", 8739, 14087, 0, 3, 5, "N", PzkVanillaPlusCarPack) -- south lake fuel station
PZKZones.addZone("ranger", "ParkingStall", 8537, 14381, 0, 9, 5, "N", PzkVanillaPlusCarPack) -- south lake resort
PZKZones.addZone("ranger", "ParkingStall", 8327, 14598, 0, 3, 5, "N", PzkVanillaPlusCarPack) -- south lake resort2
PZKZones.addZone("ranger", "ParkingStall", 6057, 14539, 0, 5, 6, "E", PzkVanillaPlusCarPack) -- south lake picnic lake
PZKZones.addZone("ranger", "ParkingStall", 7684, 11495, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- Rosewood End of road in woods near prison
PZKZones.addZone("ranger", "ParkingStall", 10105, 11122, 0, 9, 5, "N", PzkVanillaPlusCarPack) -- Muldlol junction dinner
PZKZones.addZone("ranger", "ParkingStall", 9490, 9323, 0, 3, 5, "N", PzkVanillaPlusCarPack) -- Muldlol power lines
PZKZones.addZone("ranger", "ParkingStall", 6116, 8047, 0, 5, 3, "E", PzkVanillaPlusCarPack) -- loine cabin Fallas Lake


  end
end

Events.OnLoadMapZones.Add(Registry)



  

