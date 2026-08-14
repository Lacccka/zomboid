require 'NPCs/ZombiesZoneDefinition'

local function zReVAC2_SpawnRate()

	local specZombieSpawnChance = SandboxVars.zReV2.SpecZombieSpawnChance * 0.001;
	table.insert(ZombiesZoneDefinition.Default,{name = "zReVAC2_ZombieECO1", chance= specZombieSpawnChance*0.7});
	table.insert(ZombiesZoneDefinition.Default,{name = "zReVAC2_ZombieECO2", chance= specZombieSpawnChance*0.3});
	
end
Events.OnInitGlobalModData.Add(zReVAC2_SpawnRate);