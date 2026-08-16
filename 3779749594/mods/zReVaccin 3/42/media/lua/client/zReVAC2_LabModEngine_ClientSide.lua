-- Check vaccine effects
local function LabRecipes_AdjustPlayerHealth()
    local player = getPlayer()
    if not player then return end
	
    local pMod = player:getModData()
    local recess = pMod.VaccineRecess or 0
    local vTime = pMod.VaccineTime or 0
    local strength = pMod.VaccineStrength or 0
    
    -- ЭТА ХУЙНЯ ОПТИМИЗАЦИИ ПРОСИТ! Сокращаем лишние вызовы сервера, хуле.
    local infectionLevel = player:getStats():get(CharacterStat.ZOMBIE_INFECTION) or 0
    if infectionLevel <= 0 then
        -- Если инфекции нет, только уменьшаем VaccineTime
        if vTime > 0 then
            pMod.VaccineTime = vTime - 1
			player:transmitModData()
        end
        return -- Выходим раньше, не отправляя лишних команд, сука, в миграции без комментариев запутаться можно...
    end
    
    -- Инфекция есть - ебашим проверки на сервер для работы с бодидэмэджем, епта, гопник-кодинг стайл.
	-- Интересно, сюда вообще кто-нибудь заходит?
    if recess > 0 then
        sendClientCommand(player, 'zReVAC2', 'zReRecess', {})
    end
    if vTime > 0 and strength > ZombRand(100) then
		sendClientCommand(player, 'zReVAC2', 'zReStrength', {})
    end
    
    -- Счетчик уменьшаем в любом случае, если инфекция была
    if vTime > 0 then
        pMod.VaccineTime = vTime - 1
		player:transmitModData()
    end
end

local function LabRecipes_AdjustPlayerAntibodies()
	local player = getPlayer();
	if not player then return end
	
    local infectionLevel = player:getStats():get(CharacterStat.ZOMBIE_INFECTION) or 0
	if infectionLevel <= 0 then return end
	if not player:hasTrait(zReVAC2Traits.ZREVAC2_ANTIBODIES) then return end
	
    sendClientCommand(player, 'zReVAC2', 'zReAntibodies', {})
end

local function LabRecipes_AdjustPlayerStress()
	local player = getPlayer();
	if not player then return end
	
    local infectionLevel = player:getStats():get(CharacterStat.ZOMBIE_INFECTION) or 0
	if infectionLevel <= 0 then return end
	
	local pMod = player:getModData();
	
	local hasAntibodies = player:hasTrait(zReVAC2Traits.ZREVAC2_ANTIBODIES)
	local hasVaccine = pMod.VaccineRecess and pMod.VaccineRecess > 0

	if not (hasAntibodies or hasVaccine) then
		return
	end
	
    local currentStress = player:getStats():get(CharacterStat.STRESS)
    
    if pMod.prevStress and currentStress > pMod.prevStress then
        local deltaAdjust = currentStress - (currentStress - pMod.prevStress) * 1.02
        if deltaAdjust < 0 then deltaAdjust = 0 end
        player:getStats():set(CharacterStat.STRESS, deltaAdjust)
        pMod.prevStress = deltaAdjust
     else
        pMod.prevStress = currentStress
    end
	player:transmitModData()
end

local function zReVAC2_ClientCalledFromServer(module, command, args)
	if module ~= 'zReVACser' then return end
	if command == 'zReNameTestResultPos' then
		local pl = getPlayer()
		local item = pl:getInventory():getItemById(args.ZitemId)
		if item then
			item:setName(item:getName() .. " (" .. args.ZinfectionRate .. "%)")
			item:setCustomName(true)
			item:syncItemFields()
		end
	end
	if command == 'zReNameTestTube' then
		local pl = getPlayer()
		local item = pl:getInventory():getItemById(args.ZitemId)
		if item then
			if args.translationIncident == 1 then
				item:setName(getText("IGUI_CmpTestTubeWithInfectedBloodVmAm"))
			 elseif args.translationIncident == 2 then
				item:setName(getText("IGUI_CmpTestTubeWithInfectedBloodVmAp"))
			 elseif args.translationIncident == 3 then
				item:setName(getText("IGUI_CmpTestTubeWithInfectedBloodVpAm", args.ItemInfectionRate))
			 else -- 4
				item:setName(getText("IGUI_CmpTestTubeWithInfectedBloodVpAp", args.ItemInfectionRate))
			end
			item:setCustomName(true)
			item:syncItemFields()
		end
	end
end
---------------------------------------- Events ----------------------------------------
Events.EveryHours.Add(LabRecipes_AdjustPlayerHealth)
Events.EveryHours.Add(LabRecipes_AdjustPlayerAntibodies)
Events.EveryOneMinute.Add(LabRecipes_AdjustPlayerStress)
Events.OnServerCommand.Add(zReVAC2_ClientCalledFromServer)