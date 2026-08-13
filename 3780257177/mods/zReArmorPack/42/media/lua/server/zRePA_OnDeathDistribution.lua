if isClient() then return end -- STOP GENERATE GHOST ITEMS, idk how it work, but it work.

local function zRePA_CheckDrops(zombie)
	if not zombie:getOutfitName() then return end
	local outfit = tostring(zombie:getOutfitName());
	local inv = zombie:getInventory();
	
-----------------------
-- Стандартные вояки --
-----------------------
	if outfit == "zRe_Military_1" or outfit == "zRe_Military_2" or outfit == "zRe_Military_3" or outfit == "zRe_Military_5" or outfit == "zRe_Military_6" then
	
		if 15 >= ZombRand(1, 101) then
			inv:AddItems("Base.AlcoholBandage", ZombRand(4));
		end
		if 15 >= ZombRand(1, 101) then
			inv:AddItems("Base.Lighter", 1);
		end
		if 15 >= ZombRand(1, 101) then
			inv:AddItems("Base.GranolaBar", ZombRand(5));
		end
		
	end
-------------
-- Полицаи --
-------------
	if outfit == "zRe_Military_4" then
	
		if 15 >= ZombRand(1, 101) then
			inv:AddItems("Base.AlcoholBandage", ZombRand(4));
		end
		if 15 >= ZombRand(1, 101) then
			inv:AddItems("Base.Lighter", 1);
		end
		if 15 >= ZombRand(1, 101) then
			inv:AddItems("Base.GranolaBar", ZombRand(5));
		end
		if 15 >= ZombRand(1, 101) then
			inv:AddItems("Base.308Box", ZombRand(3));
		end
		if 15 >= ZombRand(1, 101) then
			inv:AddItems("Base.308Bullets", ZombRand(36));
		end
		
	end
----------------
-- Экзоскелетчики --
----------------
	if outfit == "zRe_Military_exo1" or outfit == "zRe_Military_exo2" or outfit == "zRe_Military_exo3" then
	
		if 15 >= ZombRand(1, 101) then
			inv:AddItems("Base.AlcoholBandage", ZombRand (4));
		end
		if 15 >= ZombRand(1, 101) then
			inv:AddItems("Base.Lighter", 1);
		end
		if 15 >= ZombRand(1, 101) then
			inv:AddItems("Base.GranolaBar", ZombRand(5));
		end
		if 15 >= ZombRand(1, 101) then
			inv:AddItems("Base.ShotgunShellsBox", ZombRand(3));
		end
		if 15 >= ZombRand(1, 101) then
			inv:AddItems("Base.ShotgunShells", ZombRand(16));
		end
		
	end

----------------
-- Тракторист --
----------------
	if outfit == "zRe_Military_Tractor" then
	
		if 15 >= ZombRand(1, 101) then
			inv:AddItems("Base.AlcoholBandage", ZombRand (4));
		end
		if 15 >= ZombRand(1, 101) then
			inv:AddItems("Base.Lighter", 1);
		end
		if 15 >= ZombRand(1, 101) then
			inv:AddItems("Base.GranolaBar", ZombRand(5));
		end
		if 15 >= ZombRand(1, 101) then
			inv:AddItems("Base.556Box", ZombRand(3));
		end
		if 15 >= ZombRand(1, 101) then
			inv:AddItems("Base.556Bullets", ZombRand(36));
		end
		if 15 >= ZombRand(1, 101) then
			inv:AddItems("Base.556Clip", ZombRand(4));
		end
		
	end
end

Events.OnZombieDead.Add(zRePA_CheckDrops);