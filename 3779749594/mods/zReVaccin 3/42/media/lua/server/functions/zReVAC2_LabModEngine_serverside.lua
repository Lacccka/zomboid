local researchReduceHealth = 8
local researchMinHealthNotification = 42
local antibodyPower = 2
local antibodyPower2 = 12
local researchType = 3
local rResearchRand = 10

local function zReVAC2_OnInitGlobalModData()
	researchReduceHealth = SandboxVars.zReV2.ResearchReduceHealth
	researchMinHealthNotification = SandboxVars.zReV2.ResearchMinHealthNotification
	antibodyPower = SandboxVars.zReV2.AntibodyPower
	antibodyPower2 = SandboxVars.zReV2.AntibodyPower2
	researchType = SandboxVars.zReV2.ResearchType
	rResearchRand = SandboxVars.zReV2.ResearchRand
end
Events.OnInitGlobalModData.Add(zReVAC2_OnInitGlobalModData);

local function zMathCeil(x)
    local int = x - x % 1
    if x > int then
        return int + 1
    else
        return int
    end
end
local function zMathFloor(x)
    local int = x - x % 1
    if x >= 0 or x == int then
        return int
    else
        return int - 1
    end
end
local function FastWeightedRand() -- Рандомайзер с весом от 9 до 26 с склонением к 16
    local result = (ZombRand(9, 26) + ZombRand(9, 26) + 16) / 3
    if result < 9 then result = 9 end
    if result > 25 then result = 25 end
    local integer = result - result % 1
    if result % 1 >= 0.5 then integer = integer + 1 end
    return integer
end

-- Return zombie virus infection rate within 0..1 interval
local function LabRecipes_InfectionRate(player)
    local body = player:getBodyDamage()
    if not body:isInfected() then return 0 end
    return (player:getHoursSurvived() - body:getInfectionTime()) / body:getInfectionMortalityDuration()
end



--------------------------------------------------------------------------------------------
---------------------------------------- ПЕРЕМЕННЫЕ ----------------------------------------


-- ЭФФЕКТИВНОСТЬ ВАКЦИН																							Time: 1 = 1h, 24 = 1d										(количество тиков, сколько раз происходит рецессия)
-- 										Сброс заражения		Не действует																	Общее количество альбумина,			Замедление развития вируса 
local vaccineEffect = {					--    до: 			после:			Шанс излечиться:	Антитела:		Время и защита от вируса:	Min + Rand Дельты:					на ранней стадии:
	CmpSyringeWithPlainVaccine = 			{ Min = 0.05,	Max = 0.87,		CureChance = 0,		IsSerum = 0,	Time = 0,  Strength = 0,	AlbuminMin = 5, AlbuminDelta = 4,	Recess = 12, },
	CmpSyringeWithQualityVaccine = 			{ Min = 0.05,	Max = 0.87, 	CureChance = 0,		IsSerum = 1,	Time = 25, Strength = 105,	AlbuminMin = 9, AlbuminDelta = 5,	Recess = 18, },
	CmpSyringeWithCure = 					{ Min = -1,		Max = 0.87, 	CureChance = 105,	IsSerum = 2,	Time = 25, Strength = 105,	AlbuminMin = 0, AlbuminDelta = 0,	Recess = 0, },
	CmpSyringeWithSerum = 					{ Min = -1,		Max = 0.87, 	CureChance = 105,	IsSerum = 3,	Time = 49, Strength = 105,	AlbuminMin = 0, AlbuminDelta = 0,	Recess = 0, },

	CmpSyringeReusableWithPlainVaccine = 	{ Min = 0.05,	Max = 0.87,		CureChance = 0,		IsSerum = 0,	Time = 0,  Strength = 0,	AlbuminMin = 5, AlbuminDelta = 4,	Recess = 12, },
	CmpSyringeReusableWithQualityVaccine = 	{ Min = 0.05,	Max = 0.87, 	CureChance = 0,		IsSerum = 1,	Time = 25, Strength = 105,	AlbuminMin = 9, AlbuminDelta = 5,	Recess = 18, },
	CmpSyringeReusableWithCure = 			{ Min = -1,		Max = 0.87, 	CureChance = 105,	IsSerum = 2,	Time = 25, Strength = 105,	AlbuminMin = 0, AlbuminDelta = 0,	Recess = 0, },
	CmpSyringeReusableWithSerum = 			{ Min = -1,		Max = 0.87, 	CureChance = 105,	IsSerum = 3,	Time = 49, Strength = 105,	AlbuminMin = 0, AlbuminDelta = 0,	Recess = 0, },
}

-- Works from to 15% to 85%, max eff after 50%
local albuminEff = {
	[1] = 2,
	[2] = 3,
	[3] = 5,
	[4] = 7,
	[5] = 9,
	[6] = 11,
	[7] = 12,
	[8] = 11,
	[9] = 9,
	[10] = 8,
};

local antibodyPowerRATE = {
	[1] = 0.8,
	[2] = 0.6,
	[3] = 0.4,
	[4] = 0.2,
};
---------------------------------------------------------------------------------------------------
---------------------------------------- ОСНОВЫ МИРОЗДАНИЯ ----------------------------------------
-- Синка
local function zReVAC2_ServerCalledFromClient(module, command, player, args)
    if module ~= 'zReVAC2' then return end

	if command == 'zReRecess' then
		local pBody = player:getBodyDamage()
		local infected = pBody:isInfected()
		local pMod = player:getModData()
		local recess = pMod.VaccineRecess or 0
		
		if infected then	-- на всякий случай тут делаем проверку
			if recess > 0 and LabRecipes_InfectionRate(player) > 0.30 then
				pMod.VaccineRecess = recess - 1
				pBody:setInfectionTime(pBody:getInfectionTime() + pBody:getInfectionMortalityDuration() * FastWeightedRand() / 100)
				player:transmitModData()
			end
		end
     elseif command == 'zReStrength' then
		local pBody = player:getBodyDamage()
		local infected = pBody:isInfected()
		local pMod = player:getModData()
		local strength = pMod.VaccineStrength or 0
		local vTime = pMod.VaccineTime or 0
		
		if infected then -- на всякий случай тут делаем проверку
			if vTime > 0 and strength > ZombRand(100) then
				pBody:setInfected(false)
				--pBody:setInfectionLevel(0)
				player:getStats():set(CharacterStat.ZOMBIE_INFECTION, 0)
				pBody:setInfectionMortalityDuration(-1)
				pBody:setInfectionTime(-1)
				local parts = pBody:getBodyParts()
				local partsSize = parts:size()
				for i = 0, partsSize-1 do
					parts:get(i):SetInfected(false)
				end
			end
		end
     elseif command == 'zReAntibodies' then
		local pBody = player:getBodyDamage()
		local pMod = player:getModData()
		
		if (LabRecipes_InfectionRate(player) > antibodyPowerRATE[antibodyPower]) and (pMod.zReAntibodyed == nil or pMod.zReAntibodyed == 0) then
			pMod.zReAntibodyed = 1
		end
		if pMod.zReAntibodyed == 1 then
			pBody:setInfectionTime(pBody:getInfectionTime()+pBody:getInfectionMortalityDuration()*antibodyPower2/100);
			if LabRecipes_InfectionRate(player) < 0.1 then
				pBody:setInfected(false)
				--pBody:setInfectionLevel(0)
				player:getStats():set(CharacterStat.ZOMBIE_INFECTION, 0)
				pBody:setInfectionMortalityDuration(-1)
				pBody:setInfectionTime(-1)
				local parts = pBody:getBodyParts()
				local partsSize = parts:size()
				for i = 0, partsSize-1 do
					parts:get(i):SetInfected(false)
				end
				pMod.zReAntibodyed = 0
			end
		end
		player:transmitModData()
    end
end

--Ремувер с рецептов
local function zReVAC_remover(item)
	local srcContainer = item:getContainer()
    item:getContainer():DoRemoveItem(item)
    sendRemoveItemFromContainer(srcContainer, item);
end

local function LabRecipes_CurePlayer(player)
	local body = player:getBodyDamage();
	body:setInfected(false);
	--body:setInfectionLevel(0);
	player:getStats():set(CharacterStat.ZOMBIE_INFECTION, 0)
	body:setInfectionMortalityDuration(-1);
	body:setInfectionTime(-1);
	body:setIsFakeInfected(false);
	local parts = body:getBodyParts();
	for i = 0, parts:size()-1 do
		parts:get(i):SetInfected(false);
	end
end
local function LabRecipes_DoInjection(player, vaccine) --- /setaccesslevel "kERHUS" admin
	local body = player:getBodyDamage();
	local pMod = player:getModData();
	pMod.AlbuminDoses = 0;
	pMod.VaccineRecess = 0;
	pMod.VaccineTime = 0;
	pMod.VaccineStrength = 0;
	--local traitSusceptible = player:hasTrait(CharacterTrait.SUSCEPTIBLE)
	local traitAntibodies = player:hasTrait(zReVAC2Traits.ZREVAC2_ANTIBODIES)
	if body:isInfected() then	-- ЗАРАЖЁН
		if vaccine.CureChance > ZombRand(100) then
			LabRecipes_CurePlayer(player);		--- /setaccesslevel "kERHUS" admin
		 else
			local rate = LabRecipes_InfectionRate(player);
			if rate <= vaccine.Max then
				body:setInfectionTime(player:getHoursSurvived()-vaccine.Min*body:getInfectionMortalityDuration());
				if vaccine.AlbuminMin > 0 then
					player:getModData().AlbuminDoses = vaccine.AlbuminMin+ZombRand(vaccine.AlbuminDelta);
				end
			end
			if rate <= 30 then
				player:getModData().VaccineRecess = vaccine.Recess;
			end
		end
		if vaccine.IsSerum == 3 then
			if not traitAntibodies then
				if (pMod.VaccineSerumDoses == nil) then
					pMod.VaccineSerumDoses = ZombRand(5,13);
				end
				if pMod.VaccineSerumDoses > ZombRand(100) then
					player:getCharacterTraits():add(zReVAC2Traits.ZREVAC2_ANTIBODIES)
					HaloTextHelper.addTextWithArrow(player, getText("UI_trait_zReAntibodies"), 	true, HaloTextHelper.getColorGreen())
					getSoundManager():PlaySound("GainExperienceLevel", false, 0):setVolume(0.50)
				 else
					pMod.VaccineSerumDoses = pMod.VaccineSerumDoses + ZombRand(1,6);
				end
			end
		end
		--if traitSusceptible and (vaccine.IsSerum >= 2) then
		--	player:getCharacterTraits():remove(CharacterTrait.SUSCEPTIBLE)
		--	HaloTextHelper.addTextWithArrow(player, getText("UI_trait_susceptible"), false, HaloTextHelper.getColorGreen())
		--	getSoundManager():PlaySound("GainExperienceLevel", false, 0):setVolume(0.50)
		--end
	 else	-- НЕ ЗАРАЖЁН
		--if traitSusceptible and (vaccine.IsSerum >= 1) then
		--	player:getCharacterTraits():remove(CharacterTrait.SUSCEPTIBLE)
		--	HaloTextHelper.addTextWithArrow(player, getText("UI_trait_susceptible"), false, HaloTextHelper.getColorGreen())
		--	getSoundManager():PlaySound("GainExperienceLevel", false, 0):setVolume(0.50)
		--end
		pMod.VaccineTime = vaccine.Time;
		pMod.VaccineStrength = vaccine.Strength;
	end
	player:transmitModData()
end




----------------------------------------- OnTest -----------------------------------------
--[[
function zReVAC2_LabRecipes_ElectricityCheck()
	local player = getPlayer()
	if not player then return false end
	--if player:isPlayerMoving() then return false end
	local playerCurrentSquare = player:getCurrentSquare()
	if not playerCurrentSquare then return false end
	if playerCurrentSquare:haveElectricity() or (playerCurrentSquare:hasGridPower() and playerCurrentSquare:getRoom()) then
		return true
	 else 
		return false
	end
end
]]

function zReVAC2_LabRecipes_TestTo2lvl()
	local player = getPlayer()
	if not player then return false end
	--if player:isPlayerMoving() then return false end
	if player:getKnownRecipes():contains("zReLabChmSynthesizeQualityVaccine") then
		return false
	 else
		return true
	end
end
function zReVAC2_LabRecipes_TestTo3lvl()
	local player = getPlayer()
	if not player then return false end
	--if player:isPlayerMoving() then return false end
	if player:getKnownRecipes():contains("zReLabChmSynthesizeCure") then
		return false
	 else
		return true
	end
end
function zReVAC2_LabRecipes_TestTo4lvl()
	local player = getPlayer()
	if not player then return false end
	--if player:isPlayerMoving() then return false end
	if player:getKnownRecipes():contains("zReLabChmSynthesizeSerum") then
		return false
	 else
		return true
	end
end


---------------------------------------- OnCreate ----------------------------------------

-- СДЕЛАТЬ ОБЫЧНЫЙ ТЕСТ НА ЗАРАЖЕНИЕ -- chemistry, spectrometer 		/setaccesslevel "kERHUS" admin
function zReVAC2_LabRecipes_ChmDoBloodTest(craftRecipeData, player)
	local inv = player:getInventory()
	if not (player:getBodyDamage():isInfected()) then
		local testresult = instanceItem("zReLabItems.LabTestResultNegative")
		inv:AddItem(testresult);
		sendAddItemToContainer(inv, testresult)
	 else
		local testresult = instanceItem("zReLabItems.LabTestResultPositive")
		inv:AddItem(testresult)
		sendAddItemToContainer(inv, testresult)
		
		local infectionRate = LabRecipes_InfectionRate(player)*100
		if isClient() or isServer() then -- мультиплеер
			local args = { ZitemId = testresult:getID(), ZinfectionRate = zMathFloor(infectionRate) }
			sendServerCommand(player, 'zReVACser', 'zReNameTestResultPos', args)
		 else	-- сингл
			testresult:setName(testresult:getName().." ("..zMathFloor(infectionRate).."%)")
			testresult:setCustomName(true)
		end
	end
end


--- ZRETESTUDO 
-- 


-- ВЗЯТЬ КРОВЬ ДЛЯ ДАЛЬНЕЙШЕЙ РАБОТЫ -- medical, handcraft
-- 500ml = 100%hp | 300ml = 60%hp | 25ml = 5%hp but x DeltaHP = ~7% = 1 Syringe
function LabRecipes_ChmTakeBloodForNextWork(craftRecipeData, player)
	local result = craftRecipeData:getAllCreatedItems():get(0);
	local iMod = result:getModData();
	iMod.IsInfected = player:getBodyDamage():isInfected()
	iMod.InfectionRate = LabRecipes_InfectionRate(player)*100
	iMod.IsAntibodied = player:hasTrait(zReVAC2Traits.ZREVAC2_ANTIBODIES)

	player:getBodyDamage():ReduceGeneralHealth(researchReduceHealth)
	if player:getBodyDamage():getOverallBodyHealth() < researchMinHealthNotification then
		player:Say(getText("IGUI_zReVaccineLessHp"));
	end
	result:syncItemModData()
end

-- СДЕЛАТЬ ПОДРОБНЫЙ АНАЛИЗ КРОВИ -- chemistry, spectrometer		-- /setaccesslevel "kERHUS" admin
function zReVAC2_LabRecipes_ChmDoBloodAnalysis(craftRecipeData, player)
	local item = craftRecipeData:getAllConsumedItems():get(0)
	local result = craftRecipeData:getAllCreatedItems():get(0)
	
	local isInfected = item:getModData().IsInfected
	local infectionRate = item:getModData().InfectionRate
	local isAntibodied = item:getModData().IsAntibodied
	
	local ZtranslationKey 
	local ZtranslationIncident
	local zItemInfectionRate = zMathFloor(infectionRate)
	local zItemBloodCellsChance
	
	if isInfected then
	    if isAntibodied then	-- заражён, есть устойчивые антитела
			zItemBloodCellsChance = 40;
			ZtranslationKey = getText("IGUI_CmpTestTubeWithInfectedBloodVpAp", zItemInfectionRate)
			ZtranslationIncident = 4
		 else						-- заражён, нет устойчивых антител
			zItemBloodCellsChance = 30;
			ZtranslationKey = getText("IGUI_CmpTestTubeWithInfectedBloodVpAm", zItemInfectionRate)
			ZtranslationIncident = 3				
		end
	 else
		if isAntibodied then		-- не заражён, есть устойчивые антитела
			zItemBloodCellsChance = 25;
			ZtranslationKey = getText("IGUI_CmpTestTubeWithInfectedBloodVmAp")
			ZtranslationIncident = 2
		 else						-- не заражён, нет устойчивых антител
			zItemBloodCellsChance = -20;
			ZtranslationKey = getText("IGUI_CmpTestTubeWithInfectedBloodVmAm")
			ZtranslationIncident = 1
		end
	end
	
	if isClient() or isServer() then -- мультиплеер
		local args = { ZitemId = result:getID(), translationIncident = ZtranslationIncident, ItemInfectionRate = zItemInfectionRate, ItemBloodCellsChance = zItemBloodCellsChance }
		sendServerCommand(player, 'zReVACser', 'zReNameTestTube', args)
	 else	-- сингл
		result:setName(ZtranslationKey)
		result:setCustomName(true)
	end
end


-- ШИРАНУТЬСЯ ВАКЦИНОЙ -- medical, handcraft
function zReVAC2_LabRecipes_ChmGetAnInjection(craftRecipeData, player)

	local item = craftRecipeData:getAllConsumedItems():get(0);
	if vaccineEffect[item:getType()] then
		LabRecipes_DoInjection(player, vaccineEffect[item:getType()]);
	end
end
-- ЗАКИНУТЬСЯ ТАБЛЕТОСИКАМИ
function zReVAC2_LabRecipes_ChmGetAnAlbumin(craftRecipeData, player)

	local body = player:getBodyDamage()
	local health = body:getOverallBodyHealth()
	local newHealth = health + 14
		
	if newHealth > 100 then newHealth = 100 end
	body:setOverallBodyHealth(newHealth)
		
	local pMod = player:getModData()
	local infected = body:isInfected()
	local albuminDoses = pMod.AlbuminDoses or 0
	if infected and albuminDoses > 0 then
		local rate = zMathCeil((LabRecipes_InfectionRate(player)*100-14)/7)
		if albuminEff and albuminEff[rate] then
			body:setInfectionTime(body:getInfectionTime() + body:getInfectionMortalityDuration() * albuminEff[rate] / 100)
		end
		pMod.AlbuminDoses = albuminDoses - 1
		player:transmitModData()
    end
end

-- РАЗДЕЛИТЬ КРОВЬ НА КОМПОНЕНТЫ -- chemistry, centrifuge		-- /setaccesslevel "kERHUS" admin
function zReVAC2_LabRecipes_ChmDivideBloodIntoComponents(craftRecipeData, player)
	local result = craftRecipeData:getAllCreatedItems():get(0);
	local result2 = craftRecipeData:getAllCreatedItems():get(1);
	zReVAC_remover(result)
	zReVAC_remover(result2)
	local items = craftRecipeData:getAllConsumedItems();
	local testTubeWBlood1 = items:get(0)
	local testTubeWBlood2 = items:get(1)
	local testTubeWBlood3 = items:get(2)
	local iModFrom1 = testTubeWBlood1:getModData().BloodCellsChance or -20
	local iModFrom2 = testTubeWBlood2:getModData().BloodCellsChance or -20
	local iModFrom3 = testTubeWBlood3:getModData().BloodCellsChance or -20
	local zReBloodCellsChance = iModFrom1 + iModFrom2 + iModFrom3; 
	local inv = player:getInventory()

	if zReBloodCellsChance > ZombRand(100) then
		local FlaskWithBloodCells = instanceItem("zReLabItems.CmpFlaskWithBloodCells")
		local FlaskWithBloodPlasma = instanceItem("zReLabItems.CmpFlaskWithBloodPlasma")
		inv:AddItem(FlaskWithBloodCells)
		sendAddItemToContainer(inv, FlaskWithBloodCells)
		inv:AddItem(FlaskWithBloodPlasma)
		sendAddItemToContainer(inv, FlaskWithBloodPlasma)
	 else
		local FlasksWithBloodPlasma = inv:AddItems("zReLabItems.CmpFlaskWithBloodPlasma", 2)
		sendAddItemsToContainer(inv, FlasksWithBloodPlasma);
	end
end

-------- UPGRADE VACCINE ---------------------------------
------- НАПИСАТЬ ДНЕВНИК --------
function zReVAC2_LabRecipes_ResearchNotebookVaccineStart(craftRecipeData, player)
	local result = craftRecipeData:getAllCreatedItems():get(0)
	local inv = player:getInventory()
	local notebookvaccine = instanceItem("zReLabItems.bkNotebookVaccineStart")
	
	if player:getKnownRecipes():contains("zReLabChmSynthesizeSerum") then
		notebookvaccine = instanceItem("zReLabItems.bkNotebookVaccine4")
	 elseif player:getKnownRecipes():contains("zReLabChmSynthesizeCure") then
		notebookvaccine = instanceItem("zReLabItems.bkNotebookVaccine3")
	 elseif player:getKnownRecipes():contains("zReLabChmSynthesizeQualityVaccine") then
		notebookvaccine = instanceItem("zReLabItems.bkNotebookVaccine2")
	 elseif player:getKnownRecipes():contains("zReLabChmSynthesizePlainVaccine") then
		if not player:getKnownRecipes():contains("zReLabResearchToVaccineTo2lvl") then
			player:getKnownRecipes():add("zReLabResearchToVaccineTo2lvl")
			sendSyncPlayerFields(player, 0x00000001)
			HaloTextHelper.addGoodText(player, getText("IGUI_HaloNote_LearnedRecipe", getText("Recipe_zReLabResearchToVaccineTo2lvl")));
		end
	end
	
	inv:AddItem(notebookvaccine)
	sendAddItemToContainer(inv, notebookvaccine)
	
	zReVAC_remover(result)
end

function zReVAC2_LabRecipes_LabResearchToVaccineTo2lvl(craftRecipeData, player)	-- 1) Улучшение до 2 ур
	local result = craftRecipeData:getAllCreatedItems():get(0);
	local item = craftRecipeData:getAllConsumedItems():get(0);
	local inv = player:getInventory();
	local zreResearchStep = 0;
	local zreFirstAid = player:getPerkLevel(Perks.Doctor);
	local zreFirstAidBonus = 0;
	if zreFirstAid >= 3 then	-- 
		zreFirstAidBonus = zMathCeil(((zreFirstAid*zreFirstAid)/4));			-- ~ +3 +4 +7 +9 +13 +16 +21 +25
	end
	local zreIsDoctor = player:getDescriptor():getCharacterProfession():getName() == "doctor";
	local zreResearchChance = player:getModData().zReResearchChance1 or ZombRand(8,15);
	local zReTotalChance = zreResearchChance + zreFirstAidBonus + (zreIsDoctor and 10 or 0);
	-- RESEARCH TYPE
	local researchRand = ZombRand(rResearchRand, rResearchRand*2+1);
	if researchType == 1 then -- Upgratefull Chance
		if zReTotalChance < ZombRand(100) then
			zReVAC_remover(result)
			local notebookvaccine = instanceItem("zReLabItems.bkNotebookVaccineStart")
			inv:AddItem(notebookvaccine)
			sendAddItemToContainer(inv, notebookvaccine)
			player:getModData().zReResearchChance1 = zreResearchChance + researchRand;
			player:Say(getText("IGUI_zReVaccineResearchProgress"));
		 else
			--inv:AddItem("zReLabItems.bkNotebookVaccine2");
			player:getModData().zReResearchChance1 = 105;
			player:Say(getText("IGUI_zReVaccineResearchSucces"));
			player:getKnownRecipes():add("zReLabResearchToVaccineTo3lvl");
			player:getKnownRecipes():add("zReLabChmSynthesizeQualityVaccine");
			sendSyncPlayerFields(player, 0x00000001)
		end
	 elseif researchType == 2 then	-- Static Chance
		if 30 < ZombRand(100) then
			zReVAC_remover(result)
			local notebookvaccine = instanceItem("zReLabItems.bkNotebookVaccineStart")
			inv:AddItem(notebookvaccine)
			sendAddItemToContainer(inv, notebookvaccine)
			player:Say(getText("IGUI_zReVaccineResearchUnSucces"));
		 else
			--inv:AddItem("zReLabItems.bkNotebookVaccine2");
			player:getModData().zReResearchChance1 = 105;
			player:Say(getText("IGUI_zReVaccineResearchSucces"));
			player:getKnownRecipes():add("zReLabResearchToVaccineTo3lvl");
			player:getKnownRecipes():add("zReLabChmSynthesizeQualityVaccine");
			sendSyncPlayerFields(player, 0x00000001)
		end
	 else -- researchType == 3	-- Fill Research
		if zReTotalChance < 100 then
			zReVAC_remover(result)
			local notebookvaccine = instanceItem("zReLabItems.bkNotebookVaccineStart")
			inv:AddItem(notebookvaccine)
			sendAddItemToContainer(inv, notebookvaccine)
			player:getModData().zReResearchChance1 = zreResearchChance + researchRand;
			player:Say(getText("IGUI_zReVaccineResearchProgress"));
		 else
			--inv:AddItem("zReLabItems.bkNotebookVaccine2");
			player:getModData().zReResearchChance1 = 105;
			player:Say(getText("IGUI_zReVaccineResearchSucces"));
			player:getKnownRecipes():add("zReLabResearchToVaccineTo3lvl");
			player:getKnownRecipes():add("zReLabChmSynthesizeQualityVaccine");
			sendSyncPlayerFields(player, 0x00000001)
		end
	end
	player:transmitModData()
end

function zReVAC2_LabRecipes_LabResearchToVaccineTo3lvl(craftRecipeData, player)	-- 2) Улучшение до 3 ур
	local result = craftRecipeData:getAllCreatedItems():get(0);
	local item = craftRecipeData:getAllConsumedItems():get(0);
	local inv = player:getInventory();
	local zreResearchStep = 0;
	local zreFirstAid = player:getPerkLevel(Perks.Doctor);
	local zreFirstAidBonus = 0;
	if zreFirstAid >= 5 then	-- 
		zreFirstAidBonus = zMathCeil((-4) + ((zreFirstAid*zreFirstAid)/4.5));	-- ~ +2 +4 +7 +11 +14 +19
	end
	local zreIsDoctor = player:getDescriptor():getProfession() == "doctor";
	local zreResearchChance = player:getModData().zReResearchChance2 or ZombRand(6,13);
	local zReTotalChance = zreResearchChance + zreFirstAidBonus + (zreIsDoctor and 10 or 0)
	-- RESEARCH TYPE
	local researchRand = ZombRand(zMathCeil(rResearchRand*0.75), zMathCeil(rResearchRand*1.5+1));
	if researchType == 1 then -- Upgratefull Chance
		if zReTotalChance < ZombRand(100) then
			zReVAC_remover(result)
			local notebookvaccine = instanceItem("zReLabItems.bkNotebookVaccine2")
			inv:AddItem(notebookvaccine)
			sendAddItemToContainer(inv, notebookvaccine)
			player:getModData().zReResearchChance2 = zreResearchChance + researchRand;
			player:Say(getText("IGUI_zReVaccineResearchProgress"));
		 else
			--inv:AddItem("zReLabItems.bkNotebookVaccine3");
			player:getModData().zReResearchChance2 = 105;
			player:Say(getText("IGUI_zReVaccineResearchSucces"));
			player:getKnownRecipes():add("zReLabResearchToVaccineTo4lvl");
			player:getKnownRecipes():add("zReLabChmSynthesizeCure");
			sendSyncPlayerFields(player, 0x00000001)
		end
	 elseif researchType == 2 then -- Static Chance
		if 20 < ZombRand(100) then
			zReVAC_remover(result)
			local notebookvaccine = instanceItem("zReLabItems.bkNotebookVaccine2")
			inv:AddItem(notebookvaccine)
			sendAddItemToContainer(inv, notebookvaccine)
			player:Say(getText("IGUI_zReVaccineResearchUnSucces"));
		 else
			--inv:AddItem("zReLabItems.bkNotebookVaccine3");
			player:getModData().zReResearchChance2 = 105;
			player:Say(getText("IGUI_zReVaccineResearchSucces"));
			player:getKnownRecipes():add("zReLabResearchToVaccineTo4lvl");
			player:getKnownRecipes():add("zReLabChmSynthesizeCure");
			sendSyncPlayerFields(player, 0x00000001)
		end
	 else -- researchType == 3 -- Fill Research
		if zReTotalChance < 100 then
			zReVAC_remover(result)
			local notebookvaccine = instanceItem("zReLabItems.bkNotebookVaccine2")
			inv:AddItem(notebookvaccine)
			sendAddItemToContainer(inv, notebookvaccine)
			player:getModData().zReResearchChance2 = zreResearchChance + researchRand;
			player:Say(getText("IGUI_zReVaccineResearchProgress"));
		 else
			--inv:AddItem("zReLabItems.bkNotebookVaccine3");
			player:getModData().zReResearchChance2 = 105;
			player:Say(getText("IGUI_zReVaccineResearchSucces"));
			player:getKnownRecipes():add("zReLabResearchToVaccineTo4lvl");
			player:getKnownRecipes():add("zReLabChmSynthesizeCure");
			sendSyncPlayerFields(player, 0x00000001)
		end
	end
	player:transmitModData()
end

function zReVAC2_LabRecipes_LabResearchToVaccineTo4lvl(craftRecipeData, player)	-- 3) Улучшение до 4 ур
	local result = craftRecipeData:getAllCreatedItems():get(0);
	local item = craftRecipeData:getAllConsumedItems():get(0);
	local inv = player:getInventory();
	local zreResearchStep = 0;
	local zreFirstAid = player:getPerkLevel(Perks.Doctor);
	local zreFirstAidBonus = 0;
	if zreFirstAid >= 7 then	-- 
		zreFirstAidBonus = zMathCeil((-7) + ((zreFirstAid*zreFirstAid)/6));		-- ~ +2 +4 +8 +10
	end
	local zreIsDoctor = player:getDescriptor():getProfession() == "doctor";
	local zreResearchChance = player:getModData().zReResearchChance3 or ZombRand(4,11);
	local zReTotalChance = zreResearchChance + zreFirstAidBonus + (zreIsDoctor and 10 or 0);
	-- RESEARCH TYPE
	local researchRand = ZombRand(zMathCeil(rResearchRand*0.5), rResearchRand+1);
	if researchType == 1 then -- Upgratefull Chance
		if zReTotalChance < ZombRand(100) then
			zReVAC_remover(result)
			local notebookvaccine = instanceItem("zReLabItems.bkNotebookVaccine3")
			inv:AddItem(notebookvaccine)
			sendAddItemToContainer(inv, notebookvaccine)
			player:getModData().zReResearchChance3 = zreResearchChance + researchRand;
			player:Say(getText("IGUI_zReVaccineResearchProgress"));
		 else
			--inv:AddItem("zReLabItems.bkNotebookVaccine4");
			player:getModData().zReResearchChance3 = 105;
			player:Say(getText("IGUI_zReVaccineResearchSucces"));
			player:getKnownRecipes():add("zReLabChmSynthesizeSerum");
			sendSyncPlayerFields(player, 0x00000001)
		end
	 elseif researchType == 2 then	-- Static Chance
		if 14 < ZombRand(100) then
			zReVAC_remover(result)
			local notebookvaccine = instanceItem("zReLabItems.bkNotebookVaccine3")
			inv:AddItem(notebookvaccine)
			sendAddItemToContainer(inv, notebookvaccine)
			player:Say(getText("IGUI_zReVaccineResearchUnSucces"));
		 else
			--inv:AddItem("zReLabItems.bkNotebookVaccine4");
			player:getModData().zReResearchChance3 = 105;
			player:Say(getText("IGUI_zReVaccineResearchSucces"));
			player:getKnownRecipes():add("zReLabChmSynthesizeSerum");
			sendSyncPlayerFields(player, 0x00000001)
		end
	 else -- researchType == 3	-- Fill Research
		if zReTotalChance < 100 then
			zReVAC_remover(result)
			local notebookvaccine = instanceItem("zReLabItems.bkNotebookVaccine3")
			inv:AddItem(notebookvaccine)
			sendAddItemToContainer(inv, notebookvaccine)
			player:getModData().zReResearchChance3 = zreResearchChance + researchRand;
			player:Say(getText("IGUI_zReVaccineResearchProgress"));
		 else
			--inv:AddItem("zReLabItems.bkNotebookVaccine4");
			player:getModData().zReResearchChance3 = 105;
			player:Say(getText("IGUI_zReVaccineResearchSucces"));
			player:getKnownRecipes():add("zReLabChmSynthesizeSerum");
			sendSyncPlayerFields(player, 0x00000001)
		end
	end
	player:transmitModData()
end

Events.OnClientCommand.Add(zReVAC2_ServerCalledFromClient)