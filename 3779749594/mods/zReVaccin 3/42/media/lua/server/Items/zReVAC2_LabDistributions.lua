-----------------------------------------------------
----------------------- PATCH -----------------------
-----------------------------------------------------
require 'Items/ItemPicker'	
require 'Items/Distributions'
require 'Items/ProceduralDistributions'
require 'Items/SuburbsDistributions'
require 'Vehicles/VehicleDistributions'

-----------------------------------------------------
----------------------- BOOKS -----------------------
-----------------------------------------------------
	-- 																				  ShelfGeneric		BookstoreBooks
	-- BkLaboratoryEquipment1	"Лаборатория, Том 1 - Обородувание",					1				3
	-- BkLaboratoryEquipment2	"Лаборатория, Том 2 - Декорации",						1				3
	-- BkLaboratoryEquipment3	"Лаборатория, Том 3 - Плавление",						0.1				1

	-- SchoolLockers, GigamartSchool, ClassroomDesk, ClassroomShelves, ClassroomMisc, ShelfGeneric		BookstoreBooks
	-- BkVirologyCourses1		"Лаборатория, Том 4 - Крат. курс химии",				1				3
	-- BkVirologyCourses2		"Лаборатория, Том 5 - Вирусология ч.1",					1				3
	-- BkChemistryCourse		"Лаборатория, Том 6 - Вирусология ч.2",					0.1				1

																		--	сандбокс от 1	до	 2
	-- по умолчанию 1
	local LabBookSpawnchanceLow = SandboxVars.zReV2.BookInWorldSpawnChance * 0.1;	--= 0.1		0.2
	local LabBookSpawnchanceHigh = SandboxVars.zReV2.BookInWorldSpawnChance;		--= 1		 2

	local LabBookSpawnchanceBookstoreLow = LabBookSpawnchanceLow * 10; 				--= 1		 2
	local LabBookSpawnchanceBookstoreHigh = LabBookSpawnchanceHigh * 3; 			--= 3		 6

	-- BkVirologyCourses1, BkVirologyCourses2, BkChemistryCourse
	table.insert(VehicleDistributions.DoctorGloveBox.items, "zReLabItems.BkVirologyCourses1");
	table.insert(VehicleDistributions.DoctorGloveBox.items, LabBookSpawnchanceBookstoreLow);
	table.insert(VehicleDistributions.DoctorGloveBox.items, "zReLabItems.BkVirologyCourses2");
	table.insert(VehicleDistributions.DoctorGloveBox.items, LabBookSpawnchanceBookstoreLow);
	table.insert(VehicleDistributions.DoctorGloveBox.items, "zReLabItems.BkChemistryCourse");
	table.insert(VehicleDistributions.DoctorGloveBox.items, LabBookSpawnchanceBookstoreLow);

	table.insert(VehicleDistributions.SurvivalistTruckBed.items, "zReLabItems.BkVirologyCourses1");
	table.insert(VehicleDistributions.SurvivalistTruckBed.items, LabBookSpawnchanceHigh);
	table.insert(VehicleDistributions.SurvivalistTruckBed.items, "zReLabItems.BkVirologyCourses2");
	table.insert(VehicleDistributions.SurvivalistTruckBed.items, LabBookSpawnchanceHigh);
	table.insert(VehicleDistributions.SurvivalistTruckBed.items, "zReLabItems.BkChemistryCourse");
	table.insert(VehicleDistributions.SurvivalistTruckBed.items, LabBookSpawnchanceLow);

	table.insert(ProceduralDistributions.list["SchoolLockers"].items, "zReLabItems.BkVirologyCourses1");
	table.insert(ProceduralDistributions.list["SchoolLockers"].items, LabBookSpawnchanceHigh);
	table.insert(ProceduralDistributions.list["SchoolLockers"].items, "zReLabItems.BkVirologyCourses2");
	table.insert(ProceduralDistributions.list["SchoolLockers"].items, LabBookSpawnchanceHigh);
	table.insert(ProceduralDistributions.list["SchoolLockers"].items, "zReLabItems.BkChemistryCourse");
	table.insert(ProceduralDistributions.list["SchoolLockers"].items, LabBookSpawnchanceLow);

	table.insert(ProceduralDistributions.list["GigamartSchool"].items, "zReLabItems.BkVirologyCourses1");
	table.insert(ProceduralDistributions.list["GigamartSchool"].items, LabBookSpawnchanceHigh);
	table.insert(ProceduralDistributions.list["GigamartSchool"].items, "zReLabItems.BkVirologyCourses2");
	table.insert(ProceduralDistributions.list["GigamartSchool"].items, LabBookSpawnchanceHigh);
	table.insert(ProceduralDistributions.list["GigamartSchool"].items, "zReLabItems.BkChemistryCourse");
	table.insert(ProceduralDistributions.list["GigamartSchool"].items, LabBookSpawnchanceLow);

	table.insert(ProceduralDistributions.list["ClassroomDesk"].items, "zReLabItems.BkVirologyCourses1");
	table.insert(ProceduralDistributions.list["ClassroomDesk"].items, LabBookSpawnchanceHigh);
	table.insert(ProceduralDistributions.list["ClassroomDesk"].items, "zReLabItems.BkVirologyCourses2");
	table.insert(ProceduralDistributions.list["ClassroomDesk"].items, LabBookSpawnchanceHigh);
	table.insert(ProceduralDistributions.list["ClassroomDesk"].items, "zReLabItems.BkChemistryCourse");
	table.insert(ProceduralDistributions.list["ClassroomDesk"].items, LabBookSpawnchanceLow);

	table.insert(ProceduralDistributions.list["ClassroomMisc"].items, "zReLabItems.BkVirologyCourses1");
	table.insert(ProceduralDistributions.list["ClassroomMisc"].items, LabBookSpawnchanceHigh);
	table.insert(ProceduralDistributions.list["ClassroomMisc"].items, "zReLabItems.BkVirologyCourses2");
	table.insert(ProceduralDistributions.list["ClassroomMisc"].items, LabBookSpawnchanceHigh);
	table.insert(ProceduralDistributions.list["ClassroomMisc"].items, "zReLabItems.BkChemistryCourse");
	table.insert(ProceduralDistributions.list["ClassroomMisc"].items, LabBookSpawnchanceLow);

	table.insert(ProceduralDistributions.list["ClassroomShelves"].items, "zReLabItems.BkVirologyCourses1");
	table.insert(ProceduralDistributions.list["ClassroomShelves"].items, LabBookSpawnchanceHigh);
	table.insert(ProceduralDistributions.list["ClassroomShelves"].items, "zReLabItems.BkVirologyCourses2");
	table.insert(ProceduralDistributions.list["ClassroomShelves"].items, LabBookSpawnchanceHigh);
	table.insert(ProceduralDistributions.list["ClassroomShelves"].items, "zReLabItems.BkChemistryCourse");
	table.insert(ProceduralDistributions.list["ClassroomShelves"].items, LabBookSpawnchanceLow);

	table.insert(ProceduralDistributions.list["CrateBooks"].items, "zReLabItems.BkVirologyCourses1");
	table.insert(ProceduralDistributions.list["CrateBooks"].items, LabBookSpawnchanceHigh);
	table.insert(ProceduralDistributions.list["CrateBooks"].items, "zReLabItems.BkVirologyCourses2");
	table.insert(ProceduralDistributions.list["CrateBooks"].items, LabBookSpawnchanceHigh);
	table.insert(ProceduralDistributions.list["CrateBooks"].items, "zReLabItems.BkChemistryCourse");
	table.insert(ProceduralDistributions.list["CrateBooks"].items, LabBookSpawnchanceLow);
	
	table.insert(ProceduralDistributions.list["ShelfGeneric"].items, "zReLabItems.BkVirologyCourses1");
	table.insert(ProceduralDistributions.list["ShelfGeneric"].items, LabBookSpawnchanceHigh);
	table.insert(ProceduralDistributions.list["ShelfGeneric"].items, "zReLabItems.BkVirologyCourses2");
	table.insert(ProceduralDistributions.list["ShelfGeneric"].items, LabBookSpawnchanceHigh);
	table.insert(ProceduralDistributions.list["ShelfGeneric"].items, "zReLabItems.BkChemistryCourse");
	table.insert(ProceduralDistributions.list["ShelfGeneric"].items, LabBookSpawnchanceLow);

	table.insert(ProceduralDistributions.list["BookstoreBooks"].items, "zReLabItems.BkVirologyCourses1");
	table.insert(ProceduralDistributions.list["BookstoreBooks"].items, LabBookSpawnchanceBookstoreHigh);
	table.insert(ProceduralDistributions.list["BookstoreBooks"].items, "zReLabItems.BkVirologyCourses2");
	table.insert(ProceduralDistributions.list["BookstoreBooks"].items, LabBookSpawnchanceBookstoreHigh);
	table.insert(ProceduralDistributions.list["BookstoreBooks"].items, "zReLabItems.BkChemistryCourse");
	table.insert(ProceduralDistributions.list["BookstoreBooks"].items, LabBookSpawnchanceBookstoreLow);
	
	table.insert(ProceduralDistributions.list["MedicalOfficeBooks"].items, "zReLabItems.BkVirologyCourses1");
	table.insert(ProceduralDistributions.list["MedicalOfficeBooks"].items, LabBookSpawnchanceBookstoreHigh);
	table.insert(ProceduralDistributions.list["MedicalOfficeBooks"].items, "zReLabItems.BkVirologyCourses2");
	table.insert(ProceduralDistributions.list["MedicalOfficeBooks"].items, LabBookSpawnchanceBookstoreHigh);
	table.insert(ProceduralDistributions.list["MedicalOfficeBooks"].items, "zReLabItems.BkChemistryCourse");
	table.insert(ProceduralDistributions.list["MedicalOfficeBooks"].items, LabBookSpawnchanceBookstoreHigh);

	-- BkLaboratoryEquipment1, BkLaboratoryEquipment2, BkLaboratoryEquipment3
	table.insert(ProceduralDistributions.list["ShelfGeneric"].items, "zReLabItems.BkLaboratoryEquipment1");
	table.insert(ProceduralDistributions.list["ShelfGeneric"].items, LabBookSpawnchanceHigh);
	table.insert(ProceduralDistributions.list["ShelfGeneric"].items, "zReLabItems.BkLaboratoryEquipment2");
	table.insert(ProceduralDistributions.list["ShelfGeneric"].items, LabBookSpawnchanceHigh);
	table.insert(ProceduralDistributions.list["ShelfGeneric"].items, "zReLabItems.BkLaboratoryEquipment3");
	table.insert(ProceduralDistributions.list["ShelfGeneric"].items, LabBookSpawnchanceLow);

	table.insert(ProceduralDistributions.list["BookstoreBooks"].items, "zReLabItems.BkLaboratoryEquipment1");
	table.insert(ProceduralDistributions.list["BookstoreBooks"].items, LabBookSpawnchanceBookstoreHigh);
	table.insert(ProceduralDistributions.list["BookstoreBooks"].items, "zReLabItems.BkLaboratoryEquipment2");
	table.insert(ProceduralDistributions.list["BookstoreBooks"].items, LabBookSpawnchanceBookstoreHigh);
	table.insert(ProceduralDistributions.list["BookstoreBooks"].items, "zReLabItems.BkLaboratoryEquipment3");
	table.insert(ProceduralDistributions.list["BookstoreBooks"].items, LabBookSpawnchanceBookstoreLow);


-----------------------------------------------------
--------------------- SUBVANILLA --------------------
-----------------------------------------------------
--- More Cotton Balls
	table.insert(SuburbsDistributions.FirstAidKit.items, "zReLabItems.CottonBalls");
	table.insert(SuburbsDistributions.FirstAidKit.items, 5);

	table.insert(ProceduralDistributions.list["BathroomCounter"].items, "Base.CottonBalls");
	table.insert(ProceduralDistributions.list["BathroomCounter"].items, 5);

	table.insert(ProceduralDistributions.list["StoreShelfMedical"].items, "Base.CottonBalls");
	table.insert(ProceduralDistributions.list["StoreShelfMedical"].items, 10);

--- More Safety Goggles
	table.insert(ProceduralDistributions.list["ToolStoreMisc"].items, "Base.Glasses_SafetyGoggles");
	table.insert(ProceduralDistributions.list["ToolStoreMisc"].items, 3);
	table.insert(ProceduralDistributions.list["MechanicShelfTools"].items, "Base.Glasses_SafetyGoggles");
	table.insert(ProceduralDistributions.list["MechanicShelfTools"].items, 5);

--- ChAmmonia
	table.insert(SuburbsDistributions.FirstAidKit.items, "zReLabItems.ChAmmonia");
	table.insert(SuburbsDistributions.FirstAidKit.items, 5);

-----------------------------------------------------
---------------------- COMMON -----------------------
-----------------------------------------------------
--- АВТО
	local zReVehicle_AllDistr = {
		['TrunkStandard'] = 'junk',
		['TrunkHeavy'] = 'items',
		['SurvivalistTruckBed'] = 'items',
	}

	local zReVehicle_MedicalDistr = {
		['SurvivalistGloveBox'] = 'items',
		['SurvivalistTruckBed'] = 'items',
		['DoctorGloveBox'] = 'junk',
		['DoctorTruckBed'] = 'items',
		['AmbulanceGloveBox'] = 'junk',
		['AmbulanceTruckBed'] = 'items',
	}

	local zReVehicle_InstrumentalDistr = {
		['MetalWelderTruckBed'] = 'items',
		['CarpenterTruckBed'] = 'items',
		['ConstructionWorkerTruckBed'] = 'items',
		['PainterTruckBed'] = 'junk',
		['SurvivalistTruckBed'] = 'items',
	}

	local zReVehicle_FarmerDistr = {
		['FarmerTruckBed'] = 'items',
		['TrunkHeavy'] = 'items',
		['SurvivalistTruckBed'] = 'items',
	}


--- ОБЪЕКТЫ
	local zReProcedural_MedicalDistr = {
		'MedicalClinicTools',
		'MedicalClinicDrugs',
		'MedicalClinicOutfit',
		'MedicalStorageTools',
		'MedicalStorageDrugs',
	--	'StoreShelfMedical',
		'ArmyStorageMedical',
	--	'SafehouseMedical',
	--	'BathroomCounter',
	}

	local zReProcedural_InstrumentalDistr = {
		'ArmySurplusTools',
		'ArmyHangarTools',
		'GarageCarpentry',
		'GarageMetalwork',
		'GarageMechanics',
		'GarageTools',
		'MechanicShelfTools',
		'MechanicShelfMisc',
		'StoreShelfMechanics',
		'EngineerTools',
		'ToolStoreTools',
		'ToolStoreMisc',
		'CrateTools',
		'CrateCarpentry',
		'CrateMechanics',
	}

	local zReProcedural_FarmerDistr = {
		'StoreCounterCleaning',
		'JanitorTools',
		'JanitorChemicals',
		'JanitorCleaning',
		'JanitorMisc',
		'CrateFertilizer',
	}


--- ИНФО
-- "Base.Tongs"									КЛЕЩИ	-- Инструмент

-- "zReLabItems.LabTestTubePack"			 ПРОБИРКИ ПАК
-- "zReLabItems.LabTestTube"					 ПРОБИРКИ
-- "zReLabItems.LabFlaskPack"					КОЛБЫ ПАК	-- Медицина
-- "zReLabItems.LabFlask"							КОЛБЫ
-- "zReLabItems.LabSyringePack"			   ШПРИЦЫ ПАК
-- "zReLabItems.LabSyringe"					   ШПРИЦЫ

-- "zReLabItems.ChHydrochloricAcidCan"	 	 КАНИСТРА СОЛЯНОЙ КИСЛОТЫ -- Чистящее средство и в металлургии
-- "zReLabItems.ChSodiumHydroxideBag"		  ПАКЕТ ГИДРОКСИДА НАТРИЯ -- Чистящее средство и в садоводстве
-- "zReLabItems.ChSulfuricAcidCan"		  	  КАНИСТРА СЕРНОЙ КИСЛОТЫ -- В садоводстве, в металлургии, в автомобилях (электроил акум)
-- "zReLabItems.ChAmmonia"							 НАШАТЫРНЫЙ СПИРТ -- Медицина, чистящее, садоводство
-- "zReLabItems.ChAmmoniumChlorideBag"		 	ПАКЕТ ХЛОРИДА АММОНИЯ -- В садоводстве, в металлургии, в электронике (пайка), в медицине


--- ТРАНСПОРТ
	-- "zReLabItems.ChAmmonia" "zReLabItems.ChHydrochloricAcidCan"  "zReLabItems.ChSodiumHydroxideBag" "zReLabItems.ChSulfuricAcidCan" "zReLabItems.ChAmmoniumChlorideBag"
for k, v in pairs(zReVehicle_AllDistr) do
    if v == 'junk' then
        table.insert(VehicleDistributions[k].junk.items, "zReLabItems.ChAmmonia");
        table.insert(VehicleDistributions[k].junk.items, 1)
        table.insert(VehicleDistributions[k].junk.items, "zReLabItems.ChHydrochloricAcidCan");
        table.insert(VehicleDistributions[k].junk.items, 0.5)
        table.insert(VehicleDistributions[k].junk.items, "zReLabItems.ChSodiumHydroxideBag");
        table.insert(VehicleDistributions[k].junk.items, 0.5)
        table.insert(VehicleDistributions[k].junk.items, "zReLabItems.ChSulfuricAcidCan");
        table.insert(VehicleDistributions[k].junk.items, 0.5)
        table.insert(VehicleDistributions[k].junk.items, "zReLabItems.ChAmmoniumChlorideBag");
        table.insert(VehicleDistributions[k].junk.items, 0.5)
    else
        table.insert(VehicleDistributions[k].items, "zReLabItems.ChAmmonia");
        table.insert(VehicleDistributions[k].items, 3)
        table.insert(VehicleDistributions[k].items, "zReLabItems.ChHydrochloricAcidCan");
        table.insert(VehicleDistributions[k].items, 1)
        table.insert(VehicleDistributions[k].items, "zReLabItems.ChSodiumHydroxideBag");
        table.insert(VehicleDistributions[k].items, 1)
        table.insert(VehicleDistributions[k].items, "zReLabItems.ChSulfuricAcidCan");
        table.insert(VehicleDistributions[k].items, 1)
        table.insert(VehicleDistributions[k].items, "zReLabItems.ChAmmoniumChlorideBag");
        table.insert(VehicleDistributions[k].items, 1)
    end
end
	-- "zReLabItems.ChAmmonia"	"zReLabItems.ChAmmoniumChlorideBag"	
	-- "zReLabItems.LabTestTubePack" "zReLabItems.LabTestTube" 
	-- "zReLabItems.LabFlaskPack" "zReLabItems.LabFlask"
	-- "zReLabItems.LabSyringePack" "zReLabItems.LabSyringe"
for k, v in pairs(zReVehicle_MedicalDistr) do
    if v == 'junk' then
        table.insert(VehicleDistributions[k].junk.items, "zReLabItems.ChAmmonia");
        table.insert(VehicleDistributions[k].junk.items, 3)
	--	table.insert(VehicleDistributions[k].junk.items, "zReLabItems.ChAmmoniumChlorideBag");
	--	table.insert(VehicleDistributions[k].junk.items, 0.5)
        table.insert(VehicleDistributions[k].junk.items, "zReLabItems.LabTestTubePack");
        table.insert(VehicleDistributions[k].junk.items, 0.2)
        table.insert(VehicleDistributions[k].junk.items, "zReLabItems.LabTestTube");
        table.insert(VehicleDistributions[k].junk.items, 0.7)
        table.insert(VehicleDistributions[k].junk.items, "zReLabItems.LabFlaskPack");
        table.insert(VehicleDistributions[k].junk.items, 0.2)
        table.insert(VehicleDistributions[k].junk.items, "zReLabItems.LabFlask");
        table.insert(VehicleDistributions[k].junk.items, 0.7)
        table.insert(VehicleDistributions[k].junk.items, "zReLabItems.LabSyringePack");
        table.insert(VehicleDistributions[k].junk.items, 0.2)
        table.insert(VehicleDistributions[k].junk.items, "zReLabItems.LabSyringe");
        table.insert(VehicleDistributions[k].junk.items, 0.7)
    else
        table.insert(VehicleDistributions[k].items, "zReLabItems.ChAmmonia");
        table.insert(VehicleDistributions[k].items, 4)
	--	table.insert(VehicleDistributions[k].items, "zReLabItems.ChAmmoniumChlorideBag");
	--	table.insert(VehicleDistributions[k].items, 1)
        table.insert(VehicleDistributions[k].items, "zReLabItems.LabTestTubePack");
        table.insert(VehicleDistributions[k].items, 2)
        table.insert(VehicleDistributions[k].items, "zReLabItems.LabTestTube");
        table.insert(VehicleDistributions[k].items, 5)
        table.insert(VehicleDistributions[k].items, "zReLabItems.LabFlaskPack");
        table.insert(VehicleDistributions[k].items, 2)
        table.insert(VehicleDistributions[k].items, "zReLabItems.LabFlask");
        table.insert(VehicleDistributions[k].items, 5)
        table.insert(VehicleDistributions[k].items, "zReLabItems.LabSyringePack");
        table.insert(VehicleDistributions[k].items, 2)
        table.insert(VehicleDistributions[k].items, "zReLabItems.LabSyringe");
        table.insert(VehicleDistributions[k].items, 5)
    end
end
	-- "Base.Tongs" "zReLabItems.ChHydrochloricAcidCan"  "zReLabItems.ChSodiumHydroxideBag" "zReLabItems.ChSulfuricAcidCan" "zReLabItems.ChAmmoniumChlorideBag"
for k, v in pairs(zReVehicle_InstrumentalDistr) do
    if v == 'junk' then
        table.insert(VehicleDistributions[k].junk.items, "Base.Tongs");
        table.insert(VehicleDistributions[k].junk.items, 0.2)
        table.insert(VehicleDistributions[k].junk.items, "zReLabItems.ChHydrochloricAcidCan");
        table.insert(VehicleDistributions[k].junk.items, 0.3)
        table.insert(VehicleDistributions[k].junk.items, "zReLabItems.ChSodiumHydroxideBag");
        table.insert(VehicleDistributions[k].junk.items, 0.3)
        table.insert(VehicleDistributions[k].junk.items, "zReLabItems.ChSulfuricAcidCan");
        table.insert(VehicleDistributions[k].junk.items, 0.3)
        table.insert(VehicleDistributions[k].junk.items, "zReLabItems.ChAmmoniumChlorideBag");
        table.insert(VehicleDistributions[k].junk.items, 0.3)
    else
        table.insert(VehicleDistributions[k].items, "Base.Tongs");
        table.insert(VehicleDistributions[k].items, 0.5)
        table.insert(VehicleDistributions[k].items, "zReLabItems.ChHydrochloricAcidCan");
        table.insert(VehicleDistributions[k].items, 2)
        table.insert(VehicleDistributions[k].items, "zReLabItems.ChSodiumHydroxideBag");
        table.insert(VehicleDistributions[k].items, 2)
        table.insert(VehicleDistributions[k].items, "zReLabItems.ChSulfuricAcidCan");
        table.insert(VehicleDistributions[k].items, 2)
        table.insert(VehicleDistributions[k].items, "zReLabItems.ChAmmoniumChlorideBag");
        table.insert(VehicleDistributions[k].items, 1)
    end
end
	-- "zReLabItems.ChSodiumHydroxideBag" "zReLabItems.ChSulfuricAcidCan" "zReLabItems.ChAmmonia" "zReLabItems.ChAmmoniumChlorideBag"
for k, v in pairs(zReVehicle_FarmerDistr) do
    if v == 'junk' then
        table.insert(VehicleDistributions[k].junk.items, "zReLabItems.ChSodiumHydroxideBag");
        table.insert(VehicleDistributions[k].junk.items, 0.5)
        table.insert(VehicleDistributions[k].junk.items, "zReLabItems.ChSulfuricAcidCan");
        table.insert(VehicleDistributions[k].junk.items, 0.5)
        table.insert(VehicleDistributions[k].junk.items, "zReLabItems.ChAmmonia");
        table.insert(VehicleDistributions[k].junk.items, 0.5)
        table.insert(VehicleDistributions[k].junk.items, "zReLabItems.ChAmmoniumChlorideBag");
        table.insert(VehicleDistributions[k].junk.items, 0.5)
    else
        table.insert(VehicleDistributions[k].items, "zReLabItems.ChSodiumHydroxideBag");
        table.insert(VehicleDistributions[k].items, 1)
        table.insert(VehicleDistributions[k].items, "zReLabItems.ChSulfuricAcidCan");
        table.insert(VehicleDistributions[k].items, 1)
        table.insert(VehicleDistributions[k].items, "zReLabItems.ChAmmoniumChlorideBag");
        table.insert(VehicleDistributions[k].items, 1)
        table.insert(VehicleDistributions[k].items, "zReLabItems.ChAmmonia");
        table.insert(VehicleDistributions[k].items, 2)
		table.insert(VehicleDistributions[k].items, "zReLabItems.LabFlaskPack");
        table.insert(VehicleDistributions[k].items, 2)
        table.insert(VehicleDistributions[k].items, "zReLabItems.LabFlask");
        table.insert(VehicleDistributions[k].items, 5)
    end
end


--- ОБЪЕКТЫ
	-- "zReLabItems.ChAmmonia"	"zReLabItems.ChAmmoniumChlorideBag"
	-- "zReLabItems.LabTestTubePack" "zReLabItems.LabTestTube" 
	-- "zReLabItems.LabFlaskPack" "zReLabItems.LabFlask"
	-- "zReLabItems.LabSyringePack" "zReLabItems.LabSyringe"
for _, v in pairs(zReProcedural_MedicalDistr) do
    table.insert(ProceduralDistributions["list"][v].items, "zReLabItems.ChAmmonia")
    table.insert(ProceduralDistributions["list"][v].items, 5)
    table.insert(ProceduralDistributions["list"][v].items, "zReLabItems.ChAmmoniumChlorideBag")
    table.insert(ProceduralDistributions["list"][v].items, 3)
    table.insert(ProceduralDistributions["list"][v].items, "zReLabItems.LabTestTubePack")
    table.insert(ProceduralDistributions["list"][v].items, 2)
    table.insert(ProceduralDistributions["list"][v].items, "zReLabItems.LabTestTube")
    table.insert(ProceduralDistributions["list"][v].items, 5)
    table.insert(ProceduralDistributions["list"][v].items, "zReLabItems.LabFlaskPack")
    table.insert(ProceduralDistributions["list"][v].items, 2)
    table.insert(ProceduralDistributions["list"][v].items, "zReLabItems.LabFlask")
    table.insert(ProceduralDistributions["list"][v].items, 5)
    table.insert(ProceduralDistributions["list"][v].items, "zReLabItems.LabSyringePack")
    table.insert(ProceduralDistributions["list"][v].items, 2)
    table.insert(ProceduralDistributions["list"][v].items, "zReLabItems.LabSyringe")
    table.insert(ProceduralDistributions["list"][v].items, 5)
end
	-- "Base.Tongs" "zReLabItems.ChHydrochloricAcidCan"  "zReLabItems.ChSodiumHydroxideBag" "zReLabItems.ChSulfuricAcidCan" "zReLabItems.ChAmmoniumChlorideBag"
for _, v in pairs(zReProcedural_InstrumentalDistr) do
    table.insert(ProceduralDistributions["list"][v].items, "Base.Tongs")
	table.insert(ProceduralDistributions["list"][v].items, 0.5)
    table.insert(ProceduralDistributions["list"][v].items, "zReLabItems.ChHydrochloricAcidCan")
	table.insert(ProceduralDistributions["list"][v].items, 1)
    table.insert(ProceduralDistributions["list"][v].items, "zReLabItems.ChSodiumHydroxideBag")
	table.insert(ProceduralDistributions["list"][v].items, 1)
    table.insert(ProceduralDistributions["list"][v].items, "zReLabItems.ChSulfuricAcidCan")
	table.insert(ProceduralDistributions["list"][v].items, 1)
    table.insert(ProceduralDistributions["list"][v].items, "zReLabItems.ChAmmoniumChlorideBag")
	table.insert(ProceduralDistributions["list"][v].items, 1)
end
	-- "zReLabItems.ChSodiumHydroxideBag" "zReLabItems.ChSulfuricAcidCan" "zReLabItems.ChAmmonia" "zReLabItems.ChAmmoniumChlorideBag"
for _, v in pairs(zReProcedural_FarmerDistr) do
    table.insert(ProceduralDistributions["list"][v].items, "zReLabItems.ChSodiumHydroxideBag")
	table.insert(ProceduralDistributions["list"][v].items, 1)
    table.insert(ProceduralDistributions["list"][v].items, "zReLabItems.ChSulfuricAcidCan")
	table.insert(ProceduralDistributions["list"][v].items, 1)
    table.insert(ProceduralDistributions["list"][v].items, "zReLabItems.ChAmmonia")
	table.insert(ProceduralDistributions["list"][v].items, 1)
    table.insert(ProceduralDistributions["list"][v].items, "zReLabItems.ChAmmoniumChlorideBag")
	table.insert(ProceduralDistributions["list"][v].items, 1)
end


--- Another cases: 
	table.insert(ProceduralDistributions.list["CrateBatteries"].items, "zReLabItems.ChSulfuricAcidCan");
	table.insert(ProceduralDistributions.list["CrateBatteries"].items, 4);
	table.insert(ProceduralDistributions.list["CrateBatteries"].items, "zReLabItems.ChAmmoniumChlorideBag");
	table.insert(ProceduralDistributions.list["CrateBatteries"].items, 4);