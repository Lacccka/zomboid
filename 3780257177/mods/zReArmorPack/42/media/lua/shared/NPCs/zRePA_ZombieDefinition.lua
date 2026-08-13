require 'NPCs/ZombiesZoneDefinition'

-- ZONES
-- ZombiesZoneDefinition.Default
-- ZombiesZoneDefinition.School
-- ZombiesZoneDefinition.Gigamart
-- ZombiesZoneDefinition.Offices

-- ZombiesZoneDefinition.Prison
-- ZombiesZoneDefinition.Army
-- ZombiesZoneDefinition.Police

local function zRePAspawn()
	local SpawnChanceMillitary = SandboxVars.zRePA.ChanceMillitary * 0.001;
	--local SpawnChanceMillitary = tonumber(string.format("%.4f", preSpawnChanceMillitary))
	
	local SpawnChanceMilEXO = SandboxVars.zRePA.ChanceMilEXO * 0.001;
	--local SpawnChanceMilEXO = tonumber(string.format("%.4f", preSpawnChanceMilEXO))
	
	local SpawnChanceTractor = SandboxVars.zRePA.ChanceTractor * 0.001;
	
	if SpawnChanceMillitary > 0 then
		table.insert(ZombiesZoneDefinition.Default,{name = "zRe_Military_1", chance=SpawnChanceMillitary});
		table.insert(ZombiesZoneDefinition.Default,{name = "zRe_Military_2", chance=SpawnChanceMillitary});
		table.insert(ZombiesZoneDefinition.Default,{name = "zRe_Military_3", chance=SpawnChanceMillitary});
		table.insert(ZombiesZoneDefinition.Default,{name = "zRe_Military_4", chance=SpawnChanceMillitary});
		table.insert(ZombiesZoneDefinition.Default,{name = "zRe_Military_5", chance=SpawnChanceMillitary});
		table.insert(ZombiesZoneDefinition.Default,{name = "zRe_Military_6", chance=SpawnChanceMillitary});
	end
	if SpawnChanceMilEXO > 0 then
		table.insert(ZombiesZoneDefinition.Default,{name = "zRe_Military_exo1", chance=SpawnChanceMilEXO});
		table.insert(ZombiesZoneDefinition.Default,{name = "zRe_Military_exo2", chance=SpawnChanceMilEXO});
		table.insert(ZombiesZoneDefinition.Default,{name = "zRe_Military_exo3", chance=SpawnChanceMilEXO});
	end
	if SpawnChanceTractor > 0 then
		table.insert(ZombiesZoneDefinition.Default,{name = "zRe_Military_Tractor", chance=SpawnChanceTractor});
	end
end

Events.OnInitGlobalModData.Add(zRePAspawn);