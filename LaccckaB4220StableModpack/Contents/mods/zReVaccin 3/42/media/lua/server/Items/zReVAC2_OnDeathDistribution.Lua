--- zRe OMEGAVARIATOR DROP MOTHERFUCKER 40k B42 ULTRA VERSION
if isClient() then return end -- STOP GENERATE GHOST ITEMS, idk how it work, but it work.

local function zReVAC2_CheckECOzombie(zombie)

	if not zombie:getOutfitName() then return end
	local outfit = tostring(zombie:getOutfitName());
	if outfit ~= "zReVAC2_ZombieECO1" and outfit ~= "zReVAC2_ZombieECO2" then return end
	
    local inv = zombie:getInventory()
    
    -- SandПox и Variables
    local zReTongs = SandboxVars.zReV2.TongsSpawnChance	-- это теперь стеклодув и этот, крудицибл...
    local zReDefaultVaccine = SandboxVars.zReV2.DefaultVaccineSpawnChance	-- шанс шприца с вакциной 1 ур
    local zReTubeOrSyringeOrFlask = SandboxVars.zReV2.TubeOrSyringeOrFlaskSpawnChance	-- пробирки или шприцы или колбы
	
    local zReRandForStuff = ZombRand(100)  -- 0-99, для процентов
    local zReRandForStuffForStuff = ZombRand(96)  -- 0-95, для процентов поменьш
	
	local zreVHS = instanceItem("Base.VHS_Retail")
    zreVHS:setRecordedMediaData(getZomboidRadio():getRecordedMedia():getMediaData("18271e35-d316-47c1-ae05-eda2fa515b01"))
    inv:AddItem(zreVHS)
	
    inv:AddItem("zReLabItems.LabCorks")
	inv:AddItem("Base.Disinfectant")
    inv:AddItems("Base.AlcoholedCottonBalls", ZombRand(7))
    inv:AddItems("Base.CottonBalls", ZombRand(7))
	
    -- Vaccine 1 lvl spawn chance
    if zReDefaultVaccine > ZombRand(100) then
        inv:AddItem("zReLabItems.CmpSyringeWithPlainVaccine")
    end

    -- Tube or Syringe or Flask
    if zReTubeOrSyringeOrFlask > ZombRand(100) then
        -- Packs: 5% (0-4)
        if zReRandForStuff < 5 then
            if zReRandForStuffForStuff < 5 then
                inv:AddItem("zReLabItems.LabSyringePack")
                inv:AddItem("zReLabItems.LabTestTubePack")
                inv:AddItem("zReLabItems.LabFlaskPack")
            elseif zReRandForStuffForStuff < 15 then
                inv:AddItem("zReLabItems.LabSyringePack")
                inv:AddItem("zReLabItems.LabTestTubePack")
            elseif zReRandForStuffForStuff < 25 then
                inv:AddItem("zReLabItems.LabSyringePack")
                inv:AddItem("zReLabItems.LabFlaskPack")
            elseif zReRandForStuffForStuff < 35 then
                inv:AddItem("zReLabItems.LabTestTubePack")
                inv:AddItem("zReLabItems.LabFlaskPack")
            elseif zReRandForStuffForStuff < 55 then
                inv:AddItem("zReLabItems.LabSyringePack")
            elseif zReRandForStuffForStuff < 75 then
                inv:AddItem("zReLabItems.LabTestTubePack")
            else
                inv:AddItem("zReLabItems.LabFlaskPack")
            end
        -- Clean: 20% (5-24)
        elseif zReRandForStuff < 25 then
            if zReRandForStuffForStuff < 5 then
                inv:AddItem("zReLabItems.LabSyringeReusable")
                inv:AddItems("zReLabItems.LabTestTube", ZombRand(5)+1)
                inv:AddItems("zReLabItems.LabFlask", ZombRand(3)+1)
            elseif zReRandForStuffForStuff < 15 then
                inv:AddItem("zReLabItems.LabSyringeReusable")
                inv:AddItems("zReLabItems.LabTestTube", ZombRand(5)+1)
            elseif zReRandForStuffForStuff < 25 then
                inv:AddItems("zReLabItems.LabSyringeUsed", ZombRand(3)+1)
                inv:AddItems("zReLabItems.LabFlask", ZombRand(3)+1)
            elseif zReRandForStuffForStuff < 35 then
                inv:AddItems("zReLabItems.LabTestTube", ZombRand(5)+1)
                inv:AddItems("zReLabItems.LabFlask", ZombRand(3)+1)
            elseif zReRandForStuffForStuff < 55 then
                inv:AddItems("zReLabItems.LabSyringeUsed", ZombRand(3)+1)
            elseif zReRandForStuffForStuff < 75 then
                inv:AddItems("zReLabItems.LabTestTube", ZombRand(5)+1)
            else
                inv:AddItems("zReLabItems.LabFlask", ZombRand(3)+1)
            end
        -- Dirty: 75% (25-99)
        else
            if zReRandForStuffForStuff < 5 then
                inv:AddItems("zReLabItems.LabSyringeReusableUsed", ZombRand(2))
                inv:AddItems("zReLabItems.LabTestTubeDirty", ZombRand(4))
                inv:AddItems("zReLabItems.LabFlaskDirty", ZombRand(2))
            elseif zReRandForStuffForStuff < 15 then
                inv:AddItems("zReLabItems.LabSyringeReusableUsed", ZombRand(2))
                inv:AddItems("zReLabItems.LabTestTubeDirty", ZombRand(4))
            elseif zReRandForStuffForStuff < 25 then
                inv:AddItems("zReLabItems.LabSyringeUsed", ZombRand(3)+1)
                inv:AddItems("zReLabItems.LabFlaskDirty", ZombRand(2))
            elseif zReRandForStuffForStuff < 35 then
                inv:AddItems("zReLabItems.LabTestTubeDirty", ZombRand(4))
                inv:AddItems("zReLabItems.LabFlaskDirty", ZombRand(2))
            elseif zReRandForStuffForStuff < 55 then
                inv:AddItems("zReLabItems.LabSyringeUsed", ZombRand(3)+1)
            elseif zReRandForStuffForStuff < 75 then
                inv:AddItems("zReLabItems.LabTestTubeDirty", ZombRand(4))
            else
                inv:AddItems("zReLabItems.LabFlaskDirty", ZombRand(2))
            end
        end
    end

    -- GlassBlowingPipe
    if zReTongs > ZombRand(100) then
        inv:AddItem("Base.GlassBlowingPipe")
        inv:AddItem("Base.CeramicCrucible")
    end

    -- BOOKS OR CHEMS (50/50)
    if ZombRand(100) < 50 then  -- 50% книги
        local booksRoll = ZombRand(100)
        if booksRoll < 33 then
            inv:AddItem("zReLabItems.BkLaboratoryEquipment1")
            inv:AddItem("zReLabItems.BkLaboratoryEquipment3")
        elseif booksRoll < 66 then
            inv:AddItem("zReLabItems.BkVirologyCourses1")
            inv:AddItem("zReLabItems.BkVirologyCourses2")
        else
            inv:AddItem("zReLabItems.BkChemistryCourse")
            inv:AddItem("zReLabItems.BkLaboratoryEquipment2")
        end
    else  -- 50% химикаты
        local chemsRoll = ZombRand(120) -- 0-119
        if chemsRoll < 10 then
            inv:AddItem("zReLabItems.ChAmmonia")
            inv:AddItem("zReLabItems.ChHydrochloricAcidCan")
        elseif chemsRoll < 20 then
            inv:AddItem("zReLabItems.ChSodiumHydroxideBag")
            inv:AddItem("zReLabItems.ChSulfuricAcidCan")
        elseif chemsRoll < 30 then
            inv:AddItem("zReLabItems.ChAmmonia")
            inv:AddItem("zReLabItems.ChSodiumHydroxideBag")
        elseif chemsRoll < 40 then
            inv:AddItem("zReLabItems.ChHydrochloricAcidCan")
            inv:AddItem("zReLabItems.ChSulfuricAcidCan")
        elseif chemsRoll < 60 then
            inv:AddItem("zReLabItems.ChAmmonia")
            inv:AddItem("zReLabItems.ChAmmoniumChlorideBag")
        elseif chemsRoll < 80 then
            inv:AddItem("zReLabItems.ChHydrochloricAcidCan")
        elseif chemsRoll < 100 then
            inv:AddItem("zReLabItems.ChSodiumHydroxideBag")
        else  -- 100-119
            inv:AddItem("zReLabItems.ChSulfuricAcidCan")
        end
    end
end

local function zReVAC2_CheckDrops(zombie)
    local inv = zombie:getInventory()
    local roll = ZombRand(0, 100) -- 0-99
    local rareChance = SandboxVars.zReV2.chanceToRare -- 5-30
    local normalChance = SandboxVars.zReV2.chanceToNormal -- 20-60
    
   --- pizdec randomizer by kERHUS
	--- v6.0 ABSOLUTE (Chrono-Enhanced Edition)
	--- Changelog:
	-- v0.1alpha   : Первая мысль "а что если дроп?"
	-- v0.2beta    : Добавлен ZombRand(1,100)
	-- v0.3gamma   : Осознание, что 100 это много
	-- v0.4huyama  : Добавлена первая if-else ветка
	-- v0.5kakama  : Код работает, но только для редких
	-- v0.6        : "Ой, а обычные тоже нужны"
	-- v0.7        : Добавлен нормальный дроп
	-- v0.8        : Понял, что шансы накладываются
	-- v0.9        : Фикс overlapping chances
	-- v1.0        : Первый стабильный релиз
	-- v1.1        : Добавлены сандбокс переменные
	-- v1.5        : Рефакторинг "чтоб красиво было"
	-- v2.0        : Полный перепис кода
	-- v2.1        : Добавлена защита от дурака
	-- v2.5        : Оптимизация производительности
	-- v3.0        : Новая система весов
	-- v3.1        : Фикс бага с 0% шансом
	-- v3.2        : Добавлены комментарии
	-- v3.5        : Рефакторинг if-лестницы
	-- v4.0        : Внедрение таблиц вероятностей
	-- v4.1        : Добавлен редкий+обычный микс
	-- v4.2        : Балансировка шансов
	-- v4.5        : Кэширование настроек
	-- v5.0        : Базовая система (редкий/обычный/ничего)
	-- v5.1		   : Добавлен 10% шанс на редкий+обычный
	-- v6.0		   : Три вложенных исхода внутри редкого сектора - 10% редкий+обычный / 5% 2 редких / 85% 1 редкий
	
    if roll < rareChance then
     -- 1) РЕДКИЙ СЕКТОР (5-30%)
        local rareRoll = ZombRand(100)
        
        if rareRoll < 3 then -- 3%
          -- Вариант B: 2 редких
			inv:AddItems("zReLabItems.MatPlagueSamplesRare", 2)
         elseif rareRoll < 10 then -- 7%
         -- Вариант A: 1 редкий + 1 обычный
            inv:AddItem("zReLabItems.MatPlagueSamplesRare")
            inv:AddItem("zReLabItems.MatPlagueSamplesNormal")
         else 						-- 90%
          -- Вариант C: 1 редкий
            inv:AddItem("zReLabItems.MatPlagueSamplesRare")
        end
        
     elseif roll < (rareChance + normalChance) then
      -- 2) ОБЫЧНЫЙ СЕКТОР (20-60%)
        local normalRoll = ZombRand(0, 100)
        if normalRoll < 3 then -- 3%
			inv:AddItems("zReLabItems.MatPlagueSamplesNormal", 3)
         elseif normalRoll < 10 then -- 7%
			inv:AddItems("zReLabItems.MatPlagueSamplesNormal", 2)
         else -- 91% (15-99)
            inv:AddItem("zReLabItems.MatPlagueSamplesNormal")
        end
     -- else -- 3) ничего: оставшиеся %
    end
end

Events.OnZombieDead.Add(zReVAC2_CheckECOzombie);
Events.OnZombieDead.Add(zReVAC2_CheckDrops);