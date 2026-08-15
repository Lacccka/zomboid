require "ATA2TuningTable"
require "SVU3_PZKVLCCars_Stuffs"
require "SVUC_TuningTable"
require "SVUV_TuningTable"

local function copy(obj, seen)
	if type(obj) ~= 'table' then return obj end
	if seen and seen[obj] then return seen[obj] end
	local s = seen or {}
	local res = setmetatable({}, getmetatable(obj))
	s[obj] = res
	for k, v in pairs(obj) do res[copy(k, s)] = copy(v, s) end
	return res
end

local function SVU_TuningTable()

	local SVUV = require "SVUV_CheckFile"
	if SVUV then
		local TemplateTuningTable = SVUC_TemplateVehicle()
		local NewCarTuningTable = {}
--		local carRecipe = "ATAVanillaTuningMag"

-- preventing installing armor & engine mods on hood spare wheel
		TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"].Small.install.requireUninstalled = {"ATA2ProtectionHood", "pzkSpareWheelHood"}
		TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"].SmallRusted.install.requireUninstalled = {"ATA2ProtectionHood", "pzkSpareWheelHood"}
		TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"].Medium.install.requireUninstalled = {"ATA2ProtectionHood", "pzkSpareWheelHood"}
		TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"].MediumRusted.install.requireUninstalled = {"ATA2ProtectionHood", "pzkSpareWheelHood"}
		TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"].Large.install.requireUninstalled = {"ATA2ProtectionHood", "pzkSpareWheelHood"}
		TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"].LargeRusted.install.requireUninstalled = {"ATA2ProtectionHood", "pzkSpareWheelHood"}
		TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"].Piped.install.requireUninstalled = {"ATA2ProtectionHood", "pzkSpareWheelHood"}
		TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"].PipedRusted.install.requireUninstalled = {"ATA2ProtectionHood", "pzkSpareWheelHood"}
		TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"].SmallRound.install.requireUninstalled = {"ATA2ProtectionHood", "pzkSpareWheelHood"}
		TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"].SmallRoundRusted.install.requireUninstalled = {"ATA2ProtectionHood", "pzkSpareWheelHood"}
		TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"].LargeRound.install.requireUninstalled = {"ATA2ProtectionHood", "pzkSpareWheelHood"}
		TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"].LargeRoundRusted.install.requireUninstalled = {"ATA2ProtectionHood", "pzkSpareWheelHood"}
		
		TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHoodNoScoop"].Light.install.requireUninstalled = {"pzkSpareWheelHood"}
		TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHoodNoScoop"].LightSpiked.install.requireUninstalled = {"pzkSpareWheelHood"}
		TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHoodNoScoop"].HeavySpiked.install.requireUninstalled = {"pzkSpareWheelHood"}
		TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHoodNoScoop"].LightRusted.install.requireUninstalled = {"pzkSpareWheelHood"}
		TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHoodNoScoop"].LightSpikedRusted.install.requireUninstalled = {"pzkSpareWheelHood"}
		TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHoodNoScoop"].Reinforced.install.requireUninstalled = {"pzkSpareWheelHood"}
		
		TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"].Light.install.requireUninstalled = {"pzkSpareWheelHood", "ATA2AirScoop"}
		TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"].LightSpiked.install.requireUninstalled = {"pzkSpareWheelHood", "ATA2AirScoop"}
		TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"].LightRusted.install.requireUninstalled = {"pzkSpareWheelHood", "ATA2AirScoop"}
		TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"].LightSpikedRusted.install.requireUninstalled = {"pzkSpareWheelHood", "ATA2AirScoop"}
		TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"].Reinforced.install.requireUninstalled = {"pzkSpareWheelHood", "ATA2AirScoop"}
		
		TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"].LightScoop.install.requireUninstalled = {"pzkSpareWheelHood"}		
		TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"].HeavyScoop.install.requireUninstalled = {"pzkSpareWheelHood"}
		TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"].LightSpikedScoop.install.requireUninstalled = {"pzkSpareWheelHood"}
		TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"].HeavySpikedScoop.install.requireUninstalled = {"pzkSpareWheelHood"}
		TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"].LightRustedScoop.install.requireUninstalled = {"pzkSpareWheelHood"}
		TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"].HeavyRustedScoop.install.requireUninstalled = {"pzkSpareWheelHood"}
		TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"].LightSpikedRustedScoop.install.requireUninstalled = {"pzkSpareWheelHood"}	
		TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"].HeavySpikedRustedScoop.install.requireUninstalled = {"pzkSpareWheelHood"}
		TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"].ReinforcedScoop.install.requireUninstalled = {"pzkSpareWheelHood"}
		TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"].ReinforcedRustedScoop.install.requireUninstalled = {"pzkSpareWheelHood"}
		

		-- Entries
		NewCarTuningTable["pzkChevalierE6"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkChevalierE6Van"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkFranklin350FPickupCrewLong"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkFranklin250FWagonLong"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkFranklin250FPickupCrewLong"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkDashIntruder250WagonLong"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkChevalierRoadrunner"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkContinentalBug"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkContinentalCruiser"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkDashOhio"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkFranklinGalloper"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkChevalierLaserPoliceBulletinSheriff"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkFranklinBankTruck"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkFranklinStallionPoliceLouisvillePD"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkFranklinTruckBed"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkFranklinTruckMcCoy"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkFranklinTruckBox"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkFranklinTruckBoxLectromax"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkFranklinTruckBus"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkFranklinTruckCab"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkFranklinTruckFireTanker"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkStepVanMilk"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkStepVanIceCream"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkHMMV3"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkHMMV4Mil"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkMinivanMPV"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkMinivan2"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkMinivanT3C"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkVanSeatsTaxi"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkVanPoliceLouisvillePD"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkVanCamper"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkVanMultivan"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkChevalierVan70"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkVanBoxAmbulance"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkVanBrig"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkMinivanStellarisMail"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkVanBox"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkF150Utility"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		
		NewCarTuningTable["pzkTrailerRegularCourtains"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkTrailerRegularFuelTanker"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkTrailerRegularFlatbed"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkFreightlinerFlat"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkPeterbuilt"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkTrailerHorseBox"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkContinentalTRK"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkFranklinTruckSemi"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkFireTruckFlatPumper"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkFireTruckFlatLadder"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkFireTruckFlatSemi"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkTrailerRegularFTSemi"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkTractor"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkContinentalPfeiffer901c"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkTrailerCamping"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkTrailerTankSmall"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkTrailerBoxDual"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkTrailerTankMedium"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkDualTrailerCover"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkDualTrailer"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkTrailerTankSprayer"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkTrailerArmyCover"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkTrailerBoxPoliceDual"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkTransitBus"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkDashHEMTT6x6semi"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkChevalierLaserRanger"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkChevalierCeriseSedanFire"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}

        NewCarTuningTable["pzkDashVan70Riddle"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkVanMultivanPayday"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkVanZSquad"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}

        NewCarTuningTable["pzkDashNavajoP"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkDashNavajoW"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}

        NewCarTuningTable["pzkF350BoxAmbulance"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkF350BoxCUCV"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkF350BoxUmoveit"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkF350BoxSwat"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}

        NewCarTuningTable["pzkHearseGhoulbusters"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkMastersonRotaryC"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkVanGigamart"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkTruckD70BFRFHazmat"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkTruckDashW35BedMil"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkTruckDashW35CabrioMil"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkTruckDashW35FuelMil"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
        NewCarTuningTable["pzkTruckD70Dump"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}

		-- pzkChevalierE6 Pickup
		NewCarTuningTable["pzkChevalierE6"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkChevalierE6"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkChevalierE6"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkChevalierE6"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkChevalierE6"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkChevalierE6"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTruckBedOpen"])
		NewCarTuningTable["pzkChevalierE6"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkChevalierE6"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkChevalierE6"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkChevalierE6"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkChevalierE6"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkChevalierE6"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkChevalierE6"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkChevalierE6Van
		NewCarTuningTable["pzkChevalierE6Van"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkChevalierE6Van"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkChevalierE6Van"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkChevalierE6Van"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkChevalierE6Van"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkChevalierE6Van"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTrunk"])
		NewCarTuningTable["pzkChevalierE6Van"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkChevalierE6Van"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkChevalierE6Van"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkChevalierE6Van"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkChevalierE6Van"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkChevalierE6Van"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkChevalierE6Van"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkChevalierE6Van"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkFranklin350FPickupCrewLong Pickup
		NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2ProtectionWindowRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearLeft"])
		NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2ProtectionWindowRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearRight"])
		NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTruckBedOpen"])
		NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2ProtectionDoorRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearLeft"])
		NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2ProtectionDoorRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearRight"])
		NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkFranklin250FWagonLong SUV
		NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2ProtectionWindowRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearLeft"])
		NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2ProtectionWindowRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearRight"])
		NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTrunk"])
		NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2ProtectionDoorRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearLeft"])
		NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2ProtectionDoorRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearRight"])
		NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 100
		NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkFranklin250FPickupCrewLong Pickup
		NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2ProtectionWindowRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearLeft"])
		NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2ProtectionWindowRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearRight"])
		NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 160
		NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTruckBedOpen"])
		NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkDashIntruder250WagonLong SUV
		NewCarTuningTable["pzkDashIntruder250WagonLong"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkDashIntruder250WagonLong"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkDashIntruder250WagonLong"].parts["ATA2ProtectionWindowRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearLeft"])
		NewCarTuningTable["pzkDashIntruder250WagonLong"].parts["ATA2ProtectionWindowRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearRight"])
		NewCarTuningTable["pzkDashIntruder250WagonLong"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkDashIntruder250WagonLong"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkDashIntruder250WagonLong"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkDashIntruder250WagonLong"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTrunk"])
		NewCarTuningTable["pzkDashIntruder250WagonLong"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkDashIntruder250WagonLong"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkDashIntruder250WagonLong"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkDashIntruder250WagonLong"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkDashIntruder250WagonLong"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkDashIntruder250WagonLong"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 100
		NewCarTuningTable["pzkDashIntruder250WagonLong"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkDashIntruder250WagonLong"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkDashIntruder250WagonLong"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkChevalierRoadrunner
		NewCarTuningTable["pzkChevalierRoadrunner"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkChevalierRoadrunner"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkChevalierRoadrunner"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkChevalierRoadrunner"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkChevalierRoadrunner"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkChevalierRoadrunner"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTruckBedOpen"])
		NewCarTuningTable["pzkChevalierRoadrunner"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkChevalierRoadrunner"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkChevalierRoadrunner"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkChevalierRoadrunner"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		--NewCarTuningTable["pzkChevalierRoadrunner"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkChevalierRoadrunner"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkChevalierRoadrunner"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkChevalierRoadrunner"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkContinentalBug
		NewCarTuningTable["pzkContinentalBug"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkContinentalBug"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		--NewCarTuningTable["pzkContinentalBug"].parts["ATA2ProtectionWindowRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearLeft"])
		--NewCarTuningTable["pzkContinentalBug"].parts["ATA2ProtectionWindowRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearRight"])
		NewCarTuningTable["pzkContinentalBug"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkContinentalBug"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkContinentalBug"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkContinentalBug"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTrunk"])
		NewCarTuningTable["pzkContinentalBug"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHoodNoScoop"])
		NewCarTuningTable["pzkContinentalBug"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkContinentalBug"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkContinentalBug"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkContinentalBug"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkContinentalBug"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkContinentalBug"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkContinentalBug"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkContinentalCruiser SUV
		NewCarTuningTable["pzkContinentalCruiser"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkContinentalCruiser"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkContinentalCruiser"].parts["ATA2ProtectionWindowRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearLeft"])
		NewCarTuningTable["pzkContinentalCruiser"].parts["ATA2ProtectionWindowRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearRight"])
		NewCarTuningTable["pzkContinentalCruiser"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkContinentalCruiser"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkContinentalCruiser"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkContinentalCruiser"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTrunk"])
		NewCarTuningTable["pzkContinentalCruiser"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkContinentalCruiser"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkContinentalCruiser"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkContinentalCruiser"].parts["ATA2ProtectionDoorRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearLeft"])
		NewCarTuningTable["pzkContinentalCruiser"].parts["ATA2ProtectionDoorRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearRight"])
		NewCarTuningTable["pzkContinentalCruiser"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkContinentalCruiser"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkContinentalCruiser"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkContinentalCruiser"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkContinentalCruiser"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkDashOhio
		NewCarTuningTable["pzkDashOhio"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkDashOhio"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkDashOhio"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTrunk"])
		NewCarTuningTable["pzkDashOhio"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkDashOhio"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkDashOhio"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkDashOhio"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkDashOhio"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkDashOhio"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkFranklinGalloper
		NewCarTuningTable["pzkFranklinGalloper"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkFranklinGalloper"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		--NewCarTuningTable["pzkFranklinGalloper"].parts["ATA2ProtectionWindowRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearLeft"])
		--NewCarTuningTable["pzkFranklinGalloper"].parts["ATA2ProtectionWindowRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearRight"])
		NewCarTuningTable["pzkFranklinGalloper"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkFranklinGalloper"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkFranklinGalloper"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkFranklinGalloper"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTrunk"])
		NewCarTuningTable["pzkFranklinGalloper"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkFranklinGalloper"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkFranklinGalloper"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkFranklinGalloper"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkFranklinGalloper"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkFranklinGalloper"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkFranklinGalloper"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkFranklinGalloper"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkChevalierLaserRanger
		NewCarTuningTable["pzkChevalierLaserRanger"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkChevalierLaserRanger"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		--NewCarTuningTable["pzkChevalierLaserRanger"].parts["ATA2ProtectionWindowRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearLeft"])
		--NewCarTuningTable["pzkChevalierLaserRanger"].parts["ATA2ProtectionWindowRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearRight"])
		NewCarTuningTable["pzkChevalierLaserRanger"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkChevalierLaserRanger"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkChevalierLaserRanger"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkChevalierLaserRanger"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTrunk"])
		NewCarTuningTable["pzkChevalierLaserRanger"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkChevalierLaserRanger"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkChevalierLaserRanger"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkChevalierLaserRanger"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkChevalierLaserRanger"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkChevalierLaserRanger"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkChevalierLaserRanger"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkChevalierCeriseSedanFire
		NewCarTuningTable["pzkChevalierCeriseSedanFire"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkChevalierCeriseSedanFire"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkChevalierCeriseSedanFire"].parts["ATA2ProtectionWindowRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearLeft"])
		NewCarTuningTable["pzkChevalierCeriseSedanFire"].parts["ATA2ProtectionWindowRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearRight"])
		NewCarTuningTable["pzkChevalierCeriseSedanFire"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkChevalierCeriseSedanFire"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkChevalierCeriseSedanFire"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkChevalierCeriseSedanFire"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTrunk"])
		NewCarTuningTable["pzkChevalierCeriseSedanFire"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkChevalierCeriseSedanFire"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkChevalierCeriseSedanFire"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkChevalierCeriseSedanFire"].parts["ATA2ProtectionDoorRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearLeft"])
		NewCarTuningTable["pzkChevalierCeriseSedanFire"].parts["ATA2ProtectionDoorRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearRight"])
		NewCarTuningTable["pzkChevalierCeriseSedanFire"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkChevalierCeriseSedanFire"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkChevalierCeriseSedanFire"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkChevalierCeriseSedanFire"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkFranklinBankTruck
		NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2ProtectionWindowRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearRight"])
		NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2BullbarTruck"])
		NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRear"])
		NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2ProtectionDoorRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearRight"])
		NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 100
		NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkFranklinStallionPolice
		NewCarTuningTable["pzkFranklinStallionPoliceLouisvillePD"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkFranklinStallionPoliceLouisvillePD"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkFranklinStallionPoliceLouisvillePD"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkFranklinStallionPoliceLouisvillePD"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkFranklinStallionPoliceLouisvillePD"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2BullbarPolice"])
		NewCarTuningTable["pzkFranklinStallionPoliceLouisvillePD"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTrunk"])
		NewCarTuningTable["pzkFranklinStallionPoliceLouisvillePD"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkFranklinStallionPoliceLouisvillePD"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkFranklinStallionPoliceLouisvillePD"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkFranklinStallionPoliceLouisvillePD"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkFranklinStallionPoliceLouisvillePD"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkFranklinStallionPoliceLouisvillePD"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkFranklinStallionPoliceLouisvillePD"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkFranklinTruckBed
		NewCarTuningTable["pzkFranklinTruckBed"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkFranklinTruckBed"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkFranklinTruckBed"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkFranklinTruckBed"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkFranklinTruckBed"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2BullbarTruck"])
		NewCarTuningTable["pzkFranklinTruckBed"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTruckBedOpen"])
		NewCarTuningTable["pzkFranklinTruckBed"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkFranklinTruckBed"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkFranklinTruckBed"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkFranklinTruckBed"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkFranklinTruckBed"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkFranklinTruckBed"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkFranklinTruckBed"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkFranklinTruckBox
		NewCarTuningTable["pzkFranklinTruckBox"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkFranklinTruckBox"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkFranklinTruckBox"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkFranklinTruckBox"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkFranklinTruckBox"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2BullbarTruck"])
		NewCarTuningTable["pzkFranklinTruckBox"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRear"])
		NewCarTuningTable["pzkFranklinTruckBox"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkFranklinTruckBox"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkFranklinTruckBox"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkFranklinTruckBox"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkFranklinTruckBox"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkFranklinTruckBox"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 100
		NewCarTuningTable["pzkFranklinTruckBox"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkFranklinTruckBox"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkFranklinTruckBox"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkFranklinTruckBus
		NewCarTuningTable["pzkFranklinTruckBus"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkFranklinTruckBus"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkFranklinTruckBus"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkFranklinTruckBus"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkFranklinTruckBus"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2BullbarTruck"])
		NewCarTuningTable["pzkFranklinTruckBus"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRear"])
		NewCarTuningTable["pzkFranklinTruckBus"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkFranklinTruckBus"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkFranklinTruckBus"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkFranklinTruckBus"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkFranklinTruckBus"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 100
		NewCarTuningTable["pzkFranklinTruckBus"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkFranklinTruckBus"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkFranklinTruckBus"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkFranklinTruckCab
		NewCarTuningTable["pzkFranklinTruckCab"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkFranklinTruckCab"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkFranklinTruckCab"].parts["ATA2ProtectionWindowRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearLeft"])
		NewCarTuningTable["pzkFranklinTruckCab"].parts["ATA2ProtectionWindowRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearRight"])
		NewCarTuningTable["pzkFranklinTruckCab"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkFranklinTruckCab"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkFranklinTruckCab"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2BullbarTruck"])
		NewCarTuningTable["pzkFranklinTruckCab"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTruckBedOpen"])
		NewCarTuningTable["pzkFranklinTruckCab"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkFranklinTruckCab"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkFranklinTruckCab"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkFranklinTruckCab"].parts["ATA2ProtectionDoorRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearLeft"])
		NewCarTuningTable["pzkFranklinTruckCab"].parts["ATA2ProtectionDoorRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearRight"])
		NewCarTuningTable["pzkFranklinTruckCab"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkFranklinTruckCab"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkFranklinTruckCab"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkFranklinTruckCab"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkFranklinTruckCab"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkFranklinTruckFireTanker
		NewCarTuningTable["pzkFranklinTruckFireTanker"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkFranklinTruckFireTanker"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkFranklinTruckFireTanker"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkFranklinTruckFireTanker"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkFranklinTruckFireTanker"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2BullbarTruck"])
		NewCarTuningTable["pzkFranklinTruckFireTanker"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRear"])
		NewCarTuningTable["pzkFranklinTruckFireTanker"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkFranklinTruckFireTanker"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkFranklinTruckFireTanker"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkFranklinTruckFireTanker"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkFranklinTruckFireTanker"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkFranklinTruckFireTanker"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkFranklinTruckFireTanker"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkStepVanMilk
		NewCarTuningTable["pzkStepVanMilk"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkStepVanMilk"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkStepVanMilk"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkStepVanMilk"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkStepVanMilk"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkStepVanMilk"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRear"])
		NewCarTuningTable["pzkStepVanMilk"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkStepVanMilk"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkStepVanMilk"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkStepVanMilk"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkStepVanMilk"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkStepVanMilk"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 100
		NewCarTuningTable["pzkStepVanMilk"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkStepVanMilk"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkStepVanMilk"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkStepVanIceCream
		NewCarTuningTable["pzkStepVanIceCream"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkStepVanIceCream"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkStepVanIceCream"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkStepVanIceCream"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkStepVanIceCream"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkStepVanIceCream"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRear"])
		NewCarTuningTable["pzkStepVanIceCream"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkStepVanIceCream"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkStepVanIceCream"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkStepVanIceCream"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkStepVanIceCream"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkStepVanIceCream"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkStepVanIceCream"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkHMMV3
		NewCarTuningTable["pzkHMMV3"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkHMMV3"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkHMMV3"].parts["ATA2ProtectionWindowRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearLeft"])
		NewCarTuningTable["pzkHMMV3"].parts["ATA2ProtectionWindowRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearRight"])
		NewCarTuningTable["pzkHMMV3"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkHMMV3"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkHMMV3"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkHMMV3"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTruckBedOpen"])
		NewCarTuningTable["pzkHMMV3"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkHMMV3"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkHMMV3"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkHMMV3"].parts["ATA2ProtectionDoorRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearLeft"])
		NewCarTuningTable["pzkHMMV3"].parts["ATA2ProtectionDoorRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearRight"])
		NewCarTuningTable["pzkHMMV3"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkHMMV3"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkHMMV3"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkHMMV3"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkHMMV3"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkHMMV4
		NewCarTuningTable["pzkHMMV4Mil"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkHMMV4Mil"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkHMMV4Mil"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkHMMV4Mil"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkHMMV4Mil"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkHMMV4Mil"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTrunk"])
		NewCarTuningTable["pzkHMMV4Mil"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkHMMV4Mil"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkHMMV4Mil"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkHMMV4Mil"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		--NewCarTuningTable["pzkHMMV4Mil"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkHMMV4Mil"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkHMMV4Mil"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkHMMV4Mil"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkMinivanMPV
		NewCarTuningTable["pzkMinivanMPV"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkMinivanMPV"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkMinivanMPV"].parts["ATA2ProtectionWindowRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearLeft"])
		NewCarTuningTable["pzkMinivanMPV"].parts["ATA2ProtectionWindowRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearRight"])
		NewCarTuningTable["pzkMinivanMPV"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkMinivanMPV"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkMinivanMPV"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkMinivanMPV"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRear"])
		NewCarTuningTable["pzkMinivanMPV"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkMinivanMPV"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkMinivanMPV"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkMinivanMPV"].parts["ATA2ProtectionDoorRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearRight"])
		NewCarTuningTable["pzkMinivanMPV"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkMinivanMPV"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkMinivanMPV"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 100
		NewCarTuningTable["pzkMinivanMPV"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkMinivanMPV"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkMinivanMPV"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkMinivan2
		NewCarTuningTable["pzkMinivan2"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkMinivan2"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkMinivan2"].parts["ATA2ProtectionWindowRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearLeft"])
		NewCarTuningTable["pzkMinivan2"].parts["ATA2ProtectionWindowRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearRight"])
		NewCarTuningTable["pzkMinivan2"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkMinivan2"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkMinivan2"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkMinivan2"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRear"])
		NewCarTuningTable["pzkMinivan2"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkMinivan2"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkMinivan2"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkMinivan2"].parts["ATA2ProtectionDoorRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearRight"])
		NewCarTuningTable["pzkMinivan2"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkMinivan2"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkMinivan2"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkMinivan2"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkMinivan2"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkMinivanT3C
		NewCarTuningTable["pzkMinivanT3C"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkMinivanT3C"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkMinivanT3C"].parts["ATA2ProtectionWindowRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearLeft"])
		NewCarTuningTable["pzkMinivanT3C"].parts["ATA2ProtectionWindowRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearRight"])
		NewCarTuningTable["pzkMinivanT3C"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkMinivanT3C"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkMinivanT3C"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkMinivanT3C"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRear"])
		NewCarTuningTable["pzkMinivanT3C"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkMinivanT3C"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkMinivanT3C"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkMinivanT3C"].parts["ATA2ProtectionDoorRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearRight"])
		NewCarTuningTable["pzkMinivanT3C"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkMinivanT3C"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkMinivanT3C"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkVanSeatsTaxi
		NewCarTuningTable["pzkVanSeatsTaxi"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkVanSeatsTaxi"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkVanSeatsTaxi"].parts["ATA2ProtectionWindowMiddleLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowMiddleLeft"])
		NewCarTuningTable["pzkVanSeatsTaxi"].parts["ATA2ProtectionWindowMiddleRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowMiddleRight"])
		NewCarTuningTable["pzkVanSeatsTaxi"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkVanSeatsTaxi"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkVanSeatsTaxi"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkVanSeatsTaxi"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRear"])
		NewCarTuningTable["pzkVanSeatsTaxi"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkVanSeatsTaxi"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkVanSeatsTaxi"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkVanSeatsTaxi"].parts["ATA2ProtectionDoorMiddleLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorMiddleLeft"])
		NewCarTuningTable["pzkVanSeatsTaxi"].parts["ATA2ProtectionDoorMiddleRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorMiddleRight"])
		NewCarTuningTable["pzkVanSeatsTaxi"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkVanSeatsTaxi"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 100
		NewCarTuningTable["pzkVanSeatsTaxi"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkVanSeatsTaxi"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkVanSeatsTaxi"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkVanPolice
		NewCarTuningTable["pzkVanPoliceLouisvillePD"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkVanPoliceLouisvillePD"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkVanPoliceLouisvillePD"].parts["ATA2ProtectionWindowRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearLeft"])
		NewCarTuningTable["pzkVanPoliceLouisvillePD"].parts["ATA2ProtectionWindowRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearRight"])
		NewCarTuningTable["pzkVanPoliceLouisvillePD"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkVanPoliceLouisvillePD"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkVanPoliceLouisvillePD"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2BullbarPoliceSUV"])
		NewCarTuningTable["pzkVanPoliceLouisvillePD"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRear"])
		NewCarTuningTable["pzkVanPoliceLouisvillePD"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkVanPoliceLouisvillePD"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkVanPoliceLouisvillePD"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkVanPoliceLouisvillePD"].parts["ATA2ProtectionDoorRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearLeft"])
		NewCarTuningTable["pzkVanPoliceLouisvillePD"].parts["ATA2ProtectionDoorRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearRight"])
		NewCarTuningTable["pzkVanPoliceLouisvillePD"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkVanPoliceLouisvillePD"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 100
		NewCarTuningTable["pzkVanPoliceLouisvillePD"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkVanPoliceLouisvillePD"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkVanPoliceLouisvillePD"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkVanCamper
		NewCarTuningTable["pzkVanCamper"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkVanCamper"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkVanCamper"].parts["ATA2ProtectionWindowRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearLeft"])
		NewCarTuningTable["pzkVanCamper"].parts["ATA2ProtectionWindowRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearRight"])
		NewCarTuningTable["pzkVanCamper"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkVanCamper"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkVanCamper"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkVanCamper"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRear"])
		NewCarTuningTable["pzkVanCamper"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkVanCamper"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkVanCamper"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkVanCamper"].parts["ATA2ProtectionDoorRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearRight"])
		NewCarTuningTable["pzkVanCamper"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkVanCamper"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkVanCamper"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkVanMultivan
		NewCarTuningTable["pzkVanMultivan"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkVanMultivan"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkVanMultivan"].parts["ATA2ProtectionWindowRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearLeft"])
		NewCarTuningTable["pzkVanMultivan"].parts["ATA2ProtectionWindowRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearRight"])
		NewCarTuningTable["pzkVanMultivan"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkVanMultivan"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkVanMultivan"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkVanMultivan"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRear"])
		NewCarTuningTable["pzkVanMultivan"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkVanMultivan"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkVanMultivan"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkVanMultivan"].parts["ATA2ProtectionDoorRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearLeft"])
		NewCarTuningTable["pzkVanMultivan"].parts["ATA2ProtectionDoorRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearRight"])
		NewCarTuningTable["pzkVanMultivan"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkVanMultivan"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkVanMultivan"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 100
		NewCarTuningTable["pzkVanMultivan"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkVanMultivan"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkVanMultivan"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkChevalierVan70
		NewCarTuningTable["pzkChevalierVan70"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkChevalierVan70"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkChevalierVan70"].parts["ATA2ProtectionWindowRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowMiddleLeft"])
		NewCarTuningTable["pzkChevalierVan70"].parts["ATA2ProtectionWindowRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowMiddleRight"])
		NewCarTuningTable["pzkChevalierVan70"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkChevalierVan70"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkChevalierVan70"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkChevalierVan70"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRear"])
		NewCarTuningTable["pzkChevalierVan70"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkChevalierVan70"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkChevalierVan70"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkChevalierVan70"].parts["ATA2ProtectionDoorRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorMiddleLeft"])
		NewCarTuningTable["pzkChevalierVan70"].parts["ATA2ProtectionDoorRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorMiddleRight"])
		NewCarTuningTable["pzkChevalierVan70"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkChevalierVan70"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkChevalierVan70"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 100
		NewCarTuningTable["pzkChevalierVan70"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkChevalierVan70"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkChevalierVan70"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkVanBoxAmbulance
		NewCarTuningTable["pzkVanBoxAmbulance"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkVanBoxAmbulance"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkVanBoxAmbulance"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkVanBoxAmbulance"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkVanBoxAmbulance"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkVanBoxAmbulance"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRear"])
		NewCarTuningTable["pzkVanBoxAmbulance"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkVanBoxAmbulance"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkVanBoxAmbulance"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkVanBoxAmbulance"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkVanBoxAmbulance"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 100
		NewCarTuningTable["pzkVanBoxAmbulance"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkVanBoxAmbulance"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkVanBoxAmbulance"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkVanBrig
		NewCarTuningTable["pzkVanBrig"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkVanBrig"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkVanBrig"].parts["ATA2ProtectionWindowRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearLeft"])
		NewCarTuningTable["pzkVanBrig"].parts["ATA2ProtectionWindowRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearRight"])
		NewCarTuningTable["pzkVanBrig"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkVanBrig"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkVanBrig"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkVanBrig"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTruckBedOpen"])
		NewCarTuningTable["pzkVanBrig"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkVanBrig"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkVanBrig"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkVanBrig"].parts["ATA2ProtectionDoorRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearLeft"])
		NewCarTuningTable["pzkVanBrig"].parts["ATA2ProtectionDoorRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearRight"])
		NewCarTuningTable["pzkVanBrig"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkVanBrig"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkVanBrig"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkVanBrig"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkVanBrig"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkMinivanStellarisMail
		NewCarTuningTable["pzkMinivanStellarisMail"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkMinivanStellarisMail"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkMinivanStellarisMail"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkMinivanStellarisMail"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkMinivanStellarisMail"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkMinivanStellarisMail"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRear"])
		NewCarTuningTable["pzkMinivanStellarisMail"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkMinivanStellarisMail"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkMinivanStellarisMail"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkMinivanStellarisMail"].parts["ATA2ProtectionDoorRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearRight"])
		NewCarTuningTable["pzkMinivanStellarisMail"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkMinivanStellarisMail"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkMinivanStellarisMail"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 100
		NewCarTuningTable["pzkMinivanStellarisMail"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkMinivanStellarisMail"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkMinivanStellarisMail"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkVanBox
		NewCarTuningTable["pzkVanBox"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkVanBox"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkVanBox"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkVanBox"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkVanBox"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRear"])
		NewCarTuningTable["pzkVanBox"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkVanBox"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkVanBox"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkVanBox"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkVanBox"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkVanBox"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 100
		NewCarTuningTable["pzkVanBox"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkVanBox"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkVanBox"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])
		
		-- pzkF150Utility
		NewCarTuningTable["pzkF150Utility"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkF150Utility"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkF150Utility"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkF150Utility"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkF150Utility"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkF150Utility"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRear"])
		NewCarTuningTable["pzkF150Utility"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkF150Utility"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkF150Utility"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkF150Utility"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkF150Utility"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkF150Utility"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 150
		NewCarTuningTable["pzkF150Utility"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkF150Utility"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkF150Utility"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkTrailerRegularCourtains
		NewCarTuningTable["pzkTrailerRegularCourtains"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRearTrailer"])
		NewCarTuningTable["pzkTrailerRegularCourtains"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkTrailerRegularCourtains"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 500
		NewCarTuningTable["pzkTrailerRegularCourtains"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])

		-- pzkTrailerRegularFuelTanker
		NewCarTuningTable["pzkTrailerRegularFuelTanker"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])

		-- pzkTrailerRegularFlatbed
		NewCarTuningTable["pzkTrailerRegularFlatbed"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTrunkTrailer"])
		NewCarTuningTable["pzkTrailerRegularFlatbed"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])

		-- pzkFreightlinerFlat
		NewCarTuningTable["pzkFreightlinerFlat"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkFreightlinerFlat"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkFreightlinerFlat"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkFreightlinerFlat"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2BullbarTruck"])
		NewCarTuningTable["pzkFreightlinerFlat"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkFreightlinerFlat"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkFreightlinerFlat"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkFreightlinerFlat"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkFreightlinerFlat"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkFreightlinerFlat"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkFreightlinerFlat"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkFreightlinerFlat"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkPeterbuilt
		NewCarTuningTable["pzkPeterbuilt"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkPeterbuilt"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkPeterbuilt"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkPeterbuilt"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2BullbarTruck"])
		NewCarTuningTable["pzkPeterbuilt"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkPeterbuilt"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkPeterbuilt"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkPeterbuilt"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkPeterbuilt"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkPeterbuilt"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkPeterbuilt"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkTrailerHorseBox
		--NewCarTuningTable["pzkTrailerHorseBox"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRearTrailer"])
		NewCarTuningTable["pzkTrailerHorseBox"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkTrailerHorseBox"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])

		-- pzkContinentalTRK
		NewCarTuningTable["pzkContinentalTRK"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkContinentalTRK"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkContinentalTRK"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkContinentalTRK"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2BullbarTruck"])
		NewCarTuningTable["pzkContinentalTRK"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRear"])
		NewCarTuningTable["pzkContinentalTRK"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkContinentalTRK"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkContinentalTRK"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkContinentalTRK"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkContinentalTRK"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkContinentalTRK"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkContinentalTRK"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkContinentalTRK"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkFranklinTruckSemi
		NewCarTuningTable["pzkFranklinTruckSemi"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkFranklinTruckSemi"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkFranklinTruckSemi"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkFranklinTruckSemi"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkFranklinTruckSemi"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2BullbarTruck"])
		NewCarTuningTable["pzkFranklinTruckSemi"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkFranklinTruckSemi"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkFranklinTruckSemi"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkFranklinTruckSemi"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkFranklinTruckSemi"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkFranklinTruckSemi"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkFranklinTruckSemi"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkFireTruckFlatPumper
		NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2ProtectionWindowRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearLeft"])
		NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2ProtectionWindowRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearRight"])
		NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2BullbarTruck"])
		NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTrunk"])
		NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2ProtectionDoorRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearLeft"])
		NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2ProtectionDoorRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearRight"])
		NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkFireTruckFlatLadder
		NewCarTuningTable["pzkFireTruckFlatLadder"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkFireTruckFlatLadder"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkFireTruckFlatLadder"].parts["ATA2ProtectionWindowRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearLeft"])
		NewCarTuningTable["pzkFireTruckFlatLadder"].parts["ATA2ProtectionWindowRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearRight"])
		NewCarTuningTable["pzkFireTruckFlatLadder"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkFireTruckFlatLadder"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2BullbarTruck"])
		NewCarTuningTable["pzkFireTruckFlatLadder"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTrunk"])
		NewCarTuningTable["pzkFireTruckFlatLadder"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkFireTruckFlatLadder"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkFireTruckFlatLadder"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkFireTruckFlatLadder"].parts["ATA2ProtectionDoorRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearLeft"])
		NewCarTuningTable["pzkFireTruckFlatLadder"].parts["ATA2ProtectionDoorRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearRight"])
		NewCarTuningTable["pzkFireTruckFlatLadder"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkFireTruckFlatLadder"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkFireTruckFlatLadder"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkFireTruckFlatLadder"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkFireTruckFlatSemi
		NewCarTuningTable["pzkFireTruckFlatSemi"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkFireTruckFlatSemi"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkFireTruckFlatSemi"].parts["ATA2ProtectionWindowRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearLeft"])
		NewCarTuningTable["pzkFireTruckFlatSemi"].parts["ATA2ProtectionWindowRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearRight"])
		NewCarTuningTable["pzkFireTruckFlatSemi"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkFireTruckFlatSemi"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2BullbarTruck"])
		NewCarTuningTable["pzkFireTruckFlatSemi"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkFireTruckFlatSemi"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkFireTruckFlatSemi"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkFireTruckFlatSemi"].parts["ATA2ProtectionDoorRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearLeft"])
		NewCarTuningTable["pzkFireTruckFlatSemi"].parts["ATA2ProtectionDoorRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearRight"])
		NewCarTuningTable["pzkFireTruckFlatSemi"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkFireTruckFlatSemi"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkFireTruckFlatSemi"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkFireTruckFlatSemi"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkFireTruckFlatSemi"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkTrailerRegularFTSemi
		NewCarTuningTable["pzkTrailerRegularFTSemi"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRearTrailer"])
		NewCarTuningTable["pzkTrailerRegularFTSemi"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])

		-- pzkTractor
		NewCarTuningTable["pzkTractor"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkTractor"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkTractor"].parts["ATA2ProtectionWindowRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearLeft"])
		NewCarTuningTable["pzkTractor"].parts["ATA2ProtectionWindowRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearRight"])
		NewCarTuningTable["pzkTractor"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkTractor"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkTractor"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		--NewCarTuningTable["pzkTractor"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTrunk"])
		NewCarTuningTable["pzkTractor"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkTractor"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkTractor"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkTractor"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkTractor"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkTractor"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkTractor"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkContinentalPfeiffer901c
		NewCarTuningTable["pzkContinentalPfeiffer901c"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkContinentalPfeiffer901c"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkContinentalPfeiffer901c"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTrunk"])
		NewCarTuningTable["pzkContinentalPfeiffer901c"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHoodNoScoop"])
		NewCarTuningTable["pzkContinentalPfeiffer901c"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkContinentalPfeiffer901c"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkContinentalPfeiffer901c"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkContinentalPfeiffer901c"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkContinentalPfeiffer901c"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkTransitBus
		NewCarTuningTable["pzkTransitBus"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkTransitBus"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkTransitBus"].parts["ATA2ProtectionWindowRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearLeft"])
		NewCarTuningTable["pzkTransitBus"].parts["ATA2ProtectionWindowRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearRight"])
		NewCarTuningTable["pzkTransitBus"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkTransitBus"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkTransitBus"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2BullbarTruck"])
		NewCarTuningTable["pzkTransitBus"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRear"])
		NewCarTuningTable["pzkTransitBus"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkTransitBus"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkTransitBus"].parts["ATA2ProtectionDoorRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearRight"])
		NewCarTuningTable["pzkTransitBus"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkTransitBus"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkTransitBus"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 300
		NewCarTuningTable["pzkTransitBus"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkTransitBus"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkTransitBus"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkDashHEMTT6x6semi
		NewCarTuningTable["pzkDashHEMTT6x6semi"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkDashHEMTT6x6semi"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkDashHEMTT6x6semi"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkDashHEMTT6x6semi"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2BullbarTruck"])
		NewCarTuningTable["pzkDashHEMTT6x6semi"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkDashHEMTT6x6semi"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkDashHEMTT6x6semi"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkDashHEMTT6x6semi"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkDashHEMTT6x6semi"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkDashHEMTT6x6semi"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkDashHEMTT6x6semi"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkDashHEMTT6x6semi"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkTruckD70BFRFHazmat
		NewCarTuningTable["pzkTruckD70BFRFHazmat"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkTruckD70BFRFHazmat"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkTruckD70BFRFHazmat"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkTruckD70BFRFHazmat"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkTruckD70BFRFHazmat"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkTruckD70BFRFHazmat"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTruckBedOpen"])
		NewCarTuningTable["pzkTruckD70BFRFHazmat"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkTruckD70BFRFHazmat"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkTruckD70BFRFHazmat"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkTruckD70BFRFHazmat"].parts["ATA2ProtectionDoorRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkTruckD70BFRFHazmat"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkTruckD70BFRFHazmat"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkTruckD70BFRFHazmat"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkTruckD70BFRFHazmat"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkTruckDashW35BedMil
		NewCarTuningTable["pzkTruckDashW35BedMil"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkTruckDashW35BedMil"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkTruckDashW35BedMil"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkTruckDashW35BedMil"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkTruckDashW35BedMil"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkTruckDashW35BedMil"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRear"])
		NewCarTuningTable["pzkTruckDashW35BedMil"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkTruckDashW35BedMil"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkTruckDashW35BedMil"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkTruckDashW35BedMil"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkTruckDashW35BedMil"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkTruckDashW35BedMil"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkTruckDashW35BedMil"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkTruckDashW35CabrioMil
		NewCarTuningTable["pzkTruckDashW35CabrioMil"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkTruckDashW35CabrioMil"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkTruckDashW35CabrioMil"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTruckBedOpen"])
		NewCarTuningTable["pzkTruckDashW35CabrioMil"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkTruckDashW35CabrioMil"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkTruckDashW35CabrioMil"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkTruckDashW35CabrioMil"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkTruckDashW35CabrioMil"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkTruckDashW35CabrioMil"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkTruckDashW35CabrioMil"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkTruckDashW35FuelMil
		NewCarTuningTable["pzkTruckDashW35FuelMil"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkTruckDashW35FuelMil"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkTruckDashW35FuelMil"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkTruckDashW35FuelMil"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkTruckDashW35FuelMil"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRear"])
		NewCarTuningTable["pzkTruckDashW35FuelMil"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkTruckDashW35FuelMil"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkTruckDashW35FuelMil"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkTruckDashW35FuelMil"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkTruckDashW35FuelMil"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkTruckDashW35FuelMil"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkTruckDashW35FuelMil"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkTruckD70Dump
		NewCarTuningTable["pzkTruckD70Dump"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkTruckD70Dump"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkTruckD70Dump"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkTruckD70Dump"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkTruckD70Dump"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkTruckD70Dump"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRear"])
		NewCarTuningTable["pzkTruckD70Dump"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkTruckD70Dump"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkTruckD70Dump"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkTruckD70Dump"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkTruckD70Dump"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkTruckD70Dump"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkTruckD70Dump"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])


		NewCarTuningTable["pzkChevalierLaserPoliceBulletinSheriff"] = copy(NewCarTuningTable["pzkChevalierLaserRanger"])
		NewCarTuningTable["pzkChevalierLaserPoliceBulletinSheriff"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2BullbarPoliceSUV"])

		NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"] = copy(NewCarTuningTable["pzkChevalierCeriseSedanFire"])
		NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2BullbarPolice"])

		NewCarTuningTable["pzkFranklinStallionPoliceMeadeSheriff"] = NewCarTuningTable["pzkFranklinStallionPoliceLouisvillePD"]
		NewCarTuningTable["pzkFranklinStallionPoliceWestPoint"] = NewCarTuningTable["pzkFranklinStallionPoliceLouisvillePD"]

		NewCarTuningTable["pzkFranklinGalloperPoliceBulletinSheriff"] = NewCarTuningTable["pzkChevalierLaserPoliceBulletinSheriff"]
		NewCarTuningTable["pzkFranklinGalloperPoliceKST"] = NewCarTuningTable["pzkChevalierLaserPoliceBulletinSheriff"]
		NewCarTuningTable["pzkFranklinGalloperPoliceLouisvilleCounty"] = NewCarTuningTable["pzkChevalierLaserPoliceBulletinSheriff"]
		NewCarTuningTable["pzkFranklinGalloperPoliceMuldraughPolice"] = NewCarTuningTable["pzkChevalierLaserPoliceBulletinSheriff"]
		NewCarTuningTable["pzkChevalierLaserPoliceKST"] = NewCarTuningTable["pzkChevalierLaserPoliceBulletinSheriff"]
		NewCarTuningTable["pzkChevalierLaserPoliceLouisvilleCounty"] = NewCarTuningTable["pzkChevalierLaserPoliceBulletinSheriff"]
		NewCarTuningTable["pzkChevalierLaserPoliceMuldraughPolice"] = NewCarTuningTable["pzkChevalierLaserPoliceBulletinSheriff"]

		NewCarTuningTable["pzkChevalierCeriseSedanPoliceKST"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
		NewCarTuningTable["pzkChevalierCeriseSedanPoliceLouisvilleCounty"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
		NewCarTuningTable["pzkChevalierCeriseSedanPoliceMuldraughPolice"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
		NewCarTuningTable["pzkDashMayorPoliceBulletinSheriff"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
		NewCarTuningTable["pzkDashMayorPoliceKST"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
		NewCarTuningTable["pzkDashMayorPoliceLouisvilleCounty"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
		NewCarTuningTable["pzkFranklinTriumphTWD91PoliceBulletinSheriff"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
		NewCarTuningTable["pzkFranklinTriumphTWD91PoliceKST"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
		NewCarTuningTable["pzkFranklinTriumphTWD91PoliceLouisvilleCounty"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
		NewCarTuningTable["pzkFranklinTriumphTWD91PoliceMuldraughPolice"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
		NewCarTuningTable["pzkFranklinTriumphTWDPoliceBulletinSheriff"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
		NewCarTuningTable["pzkFranklinTriumphTWDPoliceKST"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
		NewCarTuningTable["pzkFranklinTriumphTWDPoliceLouisvilleCounty"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
		NewCarTuningTable["pzkFranklinTriumphTWDPoliceMuldraughPolice"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
		NewCarTuningTable["pzkFranklinTriumphPoliceLouisvillePD"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
		NewCarTuningTable["pzkFranklinTriumphPoliceMeadeSheriff"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
		NewCarTuningTable["pzkFranklinTriumphPoliceWestPoint"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
		NewCarTuningTable["pzkChevalierCerise93PoliceLouisvillePD"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
		NewCarTuningTable["pzkChevalierCerise93PoliceMeadeSheriff"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
		NewCarTuningTable["pzkChevalierCerise93PoliceWestPoint"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
		NewCarTuningTable["pzkSuvPoliceLouisvillePD"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
		NewCarTuningTable["pzkSuvMeadeSheriff"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
		NewCarTuningTable["pzkSuvWestPoint"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]

		NewCarTuningTable["pzkVanPoliceMeadeSheriff"] = NewCarTuningTable["pzkVanPoliceLouisvillePD"]
		NewCarTuningTable["pzkVanPoliceWestPoint"] = NewCarTuningTable["pzkVanPoliceLouisvillePD"]

		NewCarTuningTable["pzkDashCheyenePoliceLouisvillePD"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
		NewCarTuningTable["pzkDashCheyeneMeadeSheriff"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
		NewCarTuningTable["pzkDashCheyeneWestPoint"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]

		NewCarTuningTable["pzkDashCheyeneAirportSecurity"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]
		NewCarTuningTable["pzkSuvAirportSecurity"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]
		NewCarTuningTable["pzkDashCheyeneBFRFSec"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]

		NewCarTuningTable["pzkDashHellionDetective"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]
		NewCarTuningTable["pzkChevalierCeriseDetective"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]
		NewCarTuningTable["pzkDashMayorDetective"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]
		NewCarTuningTable["pzkDashRapierDetective"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]
		NewCarTuningTable["pzkFranklinHomelanderDetective"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]
		NewCarTuningTable["pzkFranklinTriumphTWDDetective"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]
		NewCarTuningTable["pzkFranklinTriumphTWD91Detective"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]

		--NewCarTuningTable["pzkChevalierVan70"] = NewCarTuningTable["pzkVanMultivan"]
		NewCarTuningTable["pzkDashVan70"] = NewCarTuningTable["pzkChevalierVan70"]
		NewCarTuningTable["pzkFranklinVan70"] = NewCarTuningTable["pzkChevalierVan70"]

		NewCarTuningTable["pzkHMMV3Mil"] = NewCarTuningTable["pzkHMMV3"]


		NewCarTuningTable["pzkChevalierF6"] = NewCarTuningTable["pzkChevalierE6"]
		NewCarTuningTable["pzkFranklin150FPickupReg"] = NewCarTuningTable["pzkChevalierE6"]
		NewCarTuningTable["pzkFtypeTowTruck"] = NewCarTuningTable["pzkChevalierE6"]
		NewCarTuningTable["pzkPickUpTruckWoodboarded"] = NewCarTuningTable["pzkChevalierE6"]
		NewCarTuningTable["pzkPickupFranklin"] = NewCarTuningTable["pzkChevalierE6"]
		NewCarTuningTable["pzkPickUpTruck93"] = NewCarTuningTable["pzkChevalierE6"]
		NewCarTuningTable["pzkChevalierTowTruck"] = NewCarTuningTable["pzkChevalierE6"]
		NewCarTuningTable["pzkChevalierTowTruckFire"] = NewCarTuningTable["pzkChevalierE6"]
		NewCarTuningTable["pzkChevalierTowTruckPoliceLouisvilleCounty"] = NewCarTuningTable["pzkChevalierE6"]
		NewCarTuningTable["pzkChevalierTowTruckStatePolice"] = NewCarTuningTable["pzkChevalierE6"]

		NewCarTuningTable["pzkChevalierF6Van"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkFranklin150van"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkDashIntruder150RegVan"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkCarMuscle"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkChevalierMaroca80"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkDashPhoenix80"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkDashChampion"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkDashDeluxo"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkDashGTA"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkDashNoble"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkContinentalSpirit"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkDashPhoenix"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkDashPhoenixBandit"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkDashPiranha"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkDashRoyal"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkDashRoyalGrand"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkDashRoyalGrandNascar86"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkDashRoyalGrandNascar22"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkDashRoyalGrandNascar16"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkChevalierDownhill"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkDashTornado"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkDashRunner"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkDashRunnerGeneral"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkChevalierMaroca"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkFranklinStallion"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkFranklinStallion2"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkFranklinStallionSport"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkFranklinIslander"] = NewCarTuningTable["pzkChevalierE6Van"]

		NewCarTuningTable["pzkFranklinStallionKing"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkFranklinStallionKingPeterGleen"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkFranklinStallionKing2"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkFranklinStallionKing3"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkFranklinStallionKing4"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkFranklinStallionKing5"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkFranklinStallionKingTheKing"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkFranklinStallionKingKenMiles"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkFranklinStallionKingFrankBullitt"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkFranklinStallionKingEleanor"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkFranklinStallionKingSeanBoswell"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkFranklinStallionKingJohnWick"] = NewCarTuningTable["pzkChevalierE6Van"]

		NewCarTuningTable["pzkMastersonLadyZ"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkMastersonLady"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkDashElite2D"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkContinentalPyrenean310"] = NewCarTuningTable["pzkChevalierE6Van"]

		NewCarTuningTable["pzkFranklinPony"] = NewCarTuningTable["pzkFranklinGalloper"]
		NewCarTuningTable["pzkChevalierLaserCUCV"] = NewCarTuningTable["pzkFranklinGalloper"]
		NewCarTuningTable["pzkChevalierLaserModern"] = NewCarTuningTable["pzkFranklinGalloper"]
		NewCarTuningTable["pzkDashIntruder150short"] = NewCarTuningTable["pzkFranklinGalloper"]
		NewCarTuningTable["pzkMastersonInitial"] = NewCarTuningTable["pzkFranklinGalloper"]
		NewCarTuningTable["pzkMastersonInitialFuji"] = NewCarTuningTable["pzkFranklinGalloper"]
		NewCarTuningTable["pzkMastersonRotaryCRyosuke"] = NewCarTuningTable["pzkFranklinGalloper"]
		NewCarTuningTable["pzkMastersonSunrise"] = NewCarTuningTable["pzkFranklinGalloper"]
		NewCarTuningTable["pzkMastersonXSR"] = NewCarTuningTable["pzkFranklinGalloper"]
		NewCarTuningTable["pzkContinentalBayer3302D"] = NewCarTuningTable["pzkFranklinGalloper"]
		NewCarTuningTable["pzkContinentalBayer330Sport"] = NewCarTuningTable["pzkFranklinGalloper"]
		NewCarTuningTable["pzkMerciaLang12402D"] = NewCarTuningTable["pzkFranklinGalloper"]
		NewCarTuningTable["pzkDashRancherDinoPark"] = NewCarTuningTable["pzkFranklinGalloper"]
		NewCarTuningTable["pzkDashRancherMail"] = NewCarTuningTable["pzkFranklinGalloper"]

		NewCarTuningTable["pzkChevalierLaserFire"] = NewCarTuningTable["pzkChevalierLaserRanger"]
		NewCarTuningTable["pzkFranklinGalloperRanger"] = NewCarTuningTable["pzkChevalierLaserRanger"]
		NewCarTuningTable["pzkFranklinGalloperFire"] = NewCarTuningTable["pzkChevalierLaserRanger"]
		NewCarTuningTable["pzkDashRancherRanger"] = NewCarTuningTable["pzkChevalierLaserRanger"]

		NewCarTuningTable["pzkChevalierPickupCrewMedium"] = NewCarTuningTable["pzkFranklin250FPickupCrewLong"]
		NewCarTuningTable["pzkFranklin150FPickupMedium"] = NewCarTuningTable["pzkFranklin250FPickupCrewLong"]
		NewCarTuningTable["pzkDashIntruder250PickupLong"] = NewCarTuningTable["pzkFranklin250FPickupCrewLong"]

		NewCarTuningTable["pzkDashMayor"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkChevalierCeriseSedan"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkDashHellion"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkDashRapier"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkFranklinTriumphTWD"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkDashPrince"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkDashPrinceBluesmobile"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkFranklinTriumphTWD91"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkDashMayorTaxi"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkDashHellionTaxi"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkFranklinTriumphTWDTaxi"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkFranklinHomelander"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkFranklinTriumph"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkHMMV"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkHMMV2"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkHMMV2Mil"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkMastersonCrown"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkMastersonExpander"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkMastersonSensation"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkMastersonScout4D"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkContinentalBayer534"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkContinentalBayer732"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkContinentalBayer3304D"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkMerciaLang1240"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkMerciaLangBerg"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkContinentalHammermanKnight"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkChevalierCerise93"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkChevalierCerise93Wagon"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkMastersonHarmony"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkMastersonHarmonyWagon"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkContinentalNord"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkContinentalNordWagon"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkDashCheyene"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkChevalierLaserOffroader"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkTriumphTWDStationWagon"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkTriumphTWDStationWagonTaxi"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkTriumphTWDStationWagonGriswold"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkDashCheyeneBFRF"] = NewCarTuningTable["pzkContinentalCruiser"]

		NewCarTuningTable["pzkFranklinTriumphTWDFire"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]
		NewCarTuningTable["pzkChevalierCeriseSedanTaxi"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]
		NewCarTuningTable["pzkFranklinTriumphTaxi"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]
		NewCarTuningTable["pzkChevalierCerise93Taxi"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]
		NewCarTuningTable["pzkChevalierCerise93Fire"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]
		NewCarTuningTable["pzkChevalierCerise93WagonFire"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]

		NewCarTuningTable["pzkDashMayorStationWagon"] = NewCarTuningTable["pzkFranklin250FWagonLong"]
		NewCarTuningTable["pzkCeriseStationWagon"] = NewCarTuningTable["pzkFranklin250FWagonLong"]
		NewCarTuningTable["pzkRapierStationWagon"] = NewCarTuningTable["pzkFranklin250FWagonLong"]
		NewCarTuningTable["pzkTriumphStationWagon"] = NewCarTuningTable["pzkFranklin250FWagonLong"]
		NewCarTuningTable["pzkChevalierProvince"] = NewCarTuningTable["pzkFranklin250FWagonLong"]
		NewCarTuningTable["pzkChevalierProvinceLong"] = NewCarTuningTable["pzkFranklin250FWagonLong"]
		NewCarTuningTable["pzkChevalierProvinceLongCUCV"] = NewCarTuningTable["pzkFranklin250FWagonLong"]
		NewCarTuningTable["pzkFranklin350FWagonLong"] = NewCarTuningTable["pzkFranklin250FWagonLong"]
		NewCarTuningTable["pzkHearse"] = NewCarTuningTable["pzkFranklin250FWagonLong"]
		NewCarTuningTable["pzkLimo"] = NewCarTuningTable["pzkFranklin250FWagonLong"]
		NewCarTuningTable["pzkChevalierCeriseLimo"] = NewCarTuningTable["pzkFranklin250FWagonLong"]
		NewCarTuningTable["pzkDashHellionLimo"] = NewCarTuningTable["pzkFranklin250FWagonLong"]
		NewCarTuningTable["pzkDashRapierLimo"] = NewCarTuningTable["pzkFranklin250FWagonLong"]
		NewCarTuningTable["pzkFranklinHomelanderLimo"] = NewCarTuningTable["pzkFranklin250FWagonLong"]
		NewCarTuningTable["pzkSuvDinoPark"] = NewCarTuningTable["pzkFranklin250FWagonLong"]
		NewCarTuningTable["pzkSuvPleistoceneLand"] = NewCarTuningTable["pzkFranklin250FWagonLong"]
		NewCarTuningTable["pzkSuvFire"] = NewCarTuningTable["pzkFranklin250FWagonLong"]
		NewCarTuningTable["pzkPickupFranklinFire"] = NewCarTuningTable["pzkChevalierLaserPoliceBulletinSheriff"]
		NewCarTuningTable["pzkPickupFranklinRanger"] = NewCarTuningTable["pzkChevalierLaserPoliceBulletinSheriff"]
		NewCarTuningTable["pzkSuvRanger"] = NewCarTuningTable["pzkFranklin250FWagonLong"]
	
		NewCarTuningTable["pzkFranklinTriumphWagon"] = NewCarTuningTable["pzkFranklin250FWagonLong"]

		NewCarTuningTable["pzkChevalierPickupCrewLong"] = NewCarTuningTable["pzkFranklin350FPickupCrewLong"]

		NewCarTuningTable["pzkFranklin250FPickupWagonLong"] = NewCarTuningTable["pzkDashIntruder250WagonLong"]
		NewCarTuningTable["pzkFranklin150FWagonMedium"] = NewCarTuningTable["pzkDashIntruder250WagonLong"]

		NewCarTuningTable["pzkContinentalBugHerbie"] = NewCarTuningTable["pzkContinentalBug"]

		NewCarTuningTable["pzkCarMuscleCabrio"] = NewCarTuningTable["pzkDashOhio"]
		NewCarTuningTable["pzkContinentalBayer330Cabrio"] = NewCarTuningTable["pzkDashOhio"]
		NewCarTuningTable["pzkMerciaLang4000Cabrio"] = NewCarTuningTable["pzkDashOhio"]
		NewCarTuningTable["pzkChevalierCosetteCabrio"] = NewCarTuningTable["pzkDashOhio"]
		NewCarTuningTable["pzkDashRancherCabrio"] = NewCarTuningTable["pzkDashOhio"]

		NewCarTuningTable["pzkFranklinSwatTruckLouisvilleSWAT"] = NewCarTuningTable["pzkFranklinBankTruck"]
		NewCarTuningTable["pzkFranklinTruckRV"] = NewCarTuningTable["pzkFranklinBankTruck"]

		NewCarTuningTable["pzkFranklinTruckBusPrison"] = NewCarTuningTable["pzkFranklinTruckBus"]
		NewCarTuningTable["pzkFranklinTruckBusArmy"] = NewCarTuningTable["pzkFranklinTruckBus"]
		NewCarTuningTable["pzkFranklinTruckBusAirport"] = NewCarTuningTable["pzkFranklinTruckBus"]

		NewCarTuningTable["pzkFranklinTruckFire"] = NewCarTuningTable["pzkFranklinTruckBox"]
		NewCarTuningTable["pzkFranklinTruckBoxLectromax"] = NewCarTuningTable["pzkFranklinTruckBox"]
		NewCarTuningTable["pzkTruckD70Box"] = NewCarTuningTable["pzkFranklinTruckBox"]
		NewCarTuningTable["pzkTruckD70Box2"] = NewCarTuningTable["pzkFranklinTruckBox"]

		NewCarTuningTable["pzkStepVanUPZ"] = NewCarTuningTable["pzkStepVanMilk"]
		NewCarTuningTable["pzkStepVanFedLog"] = NewCarTuningTable["pzkStepVanMilk"]
		NewCarTuningTable["pzkVanMcCoy"] = NewCarTuningTable["pzkStepVanMilk"]

		NewCarTuningTable["pzkStepVanPizza"] = NewCarTuningTable["pzkStepVanIceCream"]
		NewCarTuningTable["pzkStepVanCoffe"] = NewCarTuningTable["pzkStepVanIceCream"]
		NewCarTuningTable["pzkStepVanSwatLouisvilleSWAT"] = NewCarTuningTable["pzkStepVanIceCream"]

		NewCarTuningTable["pzkFranklinTruckShort"] = NewCarTuningTable["pzkFranklinTruckBed"]
		NewCarTuningTable["pzkFranklinTruckFlatbed"] = NewCarTuningTable["pzkFranklinTruckBed"]
		NewCarTuningTable["pzkFranklinTruckMcCoy"] = NewCarTuningTable["pzkFranklinTruckBed"]
		NewCarTuningTable["pzkFranklinTruckShort"] = NewCarTuningTable["pzkFranklinTruckBed"]
		NewCarTuningTable["pzkFranklinTruckMil"] = NewCarTuningTable["pzkFranklinTruckBed"]
		NewCarTuningTable["pzkFranklinTruckTow"] = NewCarTuningTable["pzkFranklinTruckBed"]
		NewCarTuningTable["pzkFranklinTruckUtility"] = NewCarTuningTable["pzkFranklinTruckBed"]

		NewCarTuningTable["pzkFranklinTruckDump"] = NewCarTuningTable["pzkFranklinTruckFireTanker"]
		NewCarTuningTable["pzkFranklinTruckGarbage"] = NewCarTuningTable["pzkFranklinTruckFireTanker"]
		NewCarTuningTable["pzkFranklinTruckTankerFossoil"] = NewCarTuningTable["pzkFranklinTruckFireTanker"]
		NewCarTuningTable["pzkFranklinTruckTankerMil"] = NewCarTuningTable["pzkFranklinTruckFireTanker"]
		NewCarTuningTable["pzkFranklinTruckMilTankerWater"] = NewCarTuningTable["pzkFranklinTruckFireTanker"]
		NewCarTuningTable["pzkFranklinTruckPropane"] = NewCarTuningTable["pzkFranklinTruckFireTanker"]
		NewCarTuningTable["pzkFranklinTruckPropane2"] = NewCarTuningTable["pzkFranklinTruckFireTanker"]
		NewCarTuningTable["pzkFranklinTruckTankerSeptic"] = NewCarTuningTable["pzkFranklinTruckFireTanker"]
		NewCarTuningTable["pzkFranklinTruckTankerWater"] = NewCarTuningTable["pzkFranklinTruckFireTanker"]

		NewCarTuningTable["pzkMinivanC22"] = NewCarTuningTable["pzkMinivanMPV"]
		NewCarTuningTable["pzkMinivanPrev"] = NewCarTuningTable["pzkMinivanMPV"]
		NewCarTuningTable["pzkMinivanStellaris"] = NewCarTuningTable["pzkMinivanMPV"]
		NewCarTuningTable["pzkMinivanStellarisTaxi"] = NewCarTuningTable["pzkMinivanMPV"]
		NewCarTuningTable["pzkMinivanT3"] = NewCarTuningTable["pzkMinivanMPV"]
		NewCarTuningTable["pzkMinivanChev"] = NewCarTuningTable["pzkMinivanMPV"]
		NewCarTuningTable["pzkMinivanConvoy"] = NewCarTuningTable["pzkMinivanMPV"]
		NewCarTuningTable["pzkMinivanTask"] = NewCarTuningTable["pzkMinivanMPV"]

		NewCarTuningTable["pzkVanBoxFiretruck"] = NewCarTuningTable["pzkVanBoxAmbulance"]

		NewCarTuningTable["pzkFreightlinerFlat2"] = NewCarTuningTable["pzkFreightlinerFlat"]
		NewCarTuningTable["pzkPeterbuiltSleeper"] = NewCarTuningTable["pzkFreightlinerFlat"]
		NewCarTuningTable["pzkPeterbuiltSleeperBandit"] = NewCarTuningTable["pzkFreightlinerFlat"]

		NewCarTuningTable["pzkTrailerRegularCourtainsBandit"] = NewCarTuningTable["pzkTrailerRegularCourtains"]

		NewCarTuningTable["pzkStepVanHotDog"] = NewCarTuningTable["pzkStepVanIceCream"]
		NewCarTuningTable["pzkStepVanTacoVan"] = NewCarTuningTable["pzkStepVanIceCream"]

		NewCarTuningTable["pzkContinentalPfeiffer901"] = NewCarTuningTable["pzkContinentalBug"]
		NewCarTuningTable["pzkContinentalPfeiffer930"] = NewCarTuningTable["pzkContinentalBug"]

		NewCarTuningTable["pzkContinentalPfeiffer930c"] = NewCarTuningTable["pzkContinentalPfeiffer901c"]

		NewCarTuningTable["pzkF350BoxAmbulance"] = NewCarTuningTable["pzkVanBoxAmbulance"]
		NewCarTuningTable["pzkF350BoxSwat"] = NewCarTuningTable["pzkVanBoxAmbulance"]
		NewCarTuningTable["pzkF350BoxCUCV"] = NewCarTuningTable["pzkVanBoxAmbulance"]
		NewCarTuningTable["pzkF350BoxUmoveit"] = NewCarTuningTable["pzkVanBoxAmbulance"]
		
		NewCarTuningTable["pzkChevalierRookie"] = NewCarTuningTable["pzkFranklinGalloper"]
		NewCarTuningTable["pzkAutowagenBunny"] = NewCarTuningTable["pzkFranklinGalloper"]
		NewCarTuningTable["pzkMastersonRice"] = NewCarTuningTable["pzkFranklinGalloper"]
		
		NewCarTuningTable["pzkMastersonIberiaVan1"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkMastersonIberiaVan2"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkMastersonIberiaPickup"] = NewCarTuningTable["pzkChevalierE6"]



		-- pzkDashVan70Riddle no windows
		NewCarTuningTable["pzkDashVan70Riddle"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkDashVan70Riddle"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkDashVan70Riddle"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkDashVan70Riddle"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkDashVan70Riddle"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRear"])
		NewCarTuningTable["pzkDashVan70Riddle"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkDashVan70Riddle"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkDashVan70Riddle"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkDashVan70Riddle"].parts["ATA2ProtectionDoorRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearLeft"])
		NewCarTuningTable["pzkDashVan70Riddle"].parts["ATA2ProtectionDoorRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearRight"])
		NewCarTuningTable["pzkDashVan70Riddle"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkDashVan70Riddle"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkDashVan70Riddle"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 100
		NewCarTuningTable["pzkDashVan70Riddle"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkDashVan70Riddle"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkDashVan70Riddle"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkVan no windows
		NewCarTuningTable["pzkVanMultivanPayday"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkVanMultivanPayday"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkVanMultivanPayday"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkVanMultivanPayday"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkVanMultivanPayday"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkVanMultivanPayday"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRear"])
		NewCarTuningTable["pzkVanMultivanPayday"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkVanMultivanPayday"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkVanMultivanPayday"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkVanMultivanPayday"].parts["ATA2ProtectionDoorRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearLeft"])
		NewCarTuningTable["pzkVanMultivanPayday"].parts["ATA2ProtectionDoorRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearRight"])
		NewCarTuningTable["pzkVanMultivanPayday"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkVanMultivanPayday"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkVanMultivanPayday"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 100
		NewCarTuningTable["pzkVanMultivanPayday"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkVanMultivanPayday"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkVanMultivanPayday"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkDashNavajoW
		NewCarTuningTable["pzkDashNavajoW"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkDashNavajoW"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkDashNavajoW"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkDashNavajoW"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkDashNavajoW"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkDashNavajoW"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTrunk"])
		NewCarTuningTable["pzkDashNavajoW"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkDashNavajoW"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkDashNavajoW"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkDashNavajoW"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		--NewCarTuningTable["pzkDashNavajoW"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkDashNavajoW"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkDashNavajoW"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkDashNavajoW"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])


		NewCarTuningTable["pzkVanZSquad"] = NewCarTuningTable["pzkVanMultivanPayday"]

		NewCarTuningTable["pzkDashNavajoP"] = NewCarTuningTable["pzkChevalierE6"]
		--NewCarTuningTable["pzkDashNavajoW"] = NewCarTuningTable["pzkChevalierE6Van"]

		NewCarTuningTable["pzkHearseGhoulbusters"] = NewCarTuningTable["pzkFranklin250FWagonLong"]
		NewCarTuningTable["pzkMastersonRotaryC"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkVanGigamart"] = NewCarTuningTable["pzkStepVanMilk"]
		
		
	NewCarTuningTable["pzkFreightlinerTerminatorTow"] = NewCarTuningTable["pzkFreightlinerFlat"]
	NewCarTuningTable["pzkVanBoxSwat"] = NewCarTuningTable["pzkVanBoxAmbulance"]

	NewCarTuningTable["pzkFranklinTriumphTWDLSU"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]
	NewCarTuningTable["pzkChevalierCeriseSedanLSU"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]
	NewCarTuningTable["pzkChevalierNyalaLSU"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]
	NewCarTuningTable["pzkDashMayorLSU"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]
	NewCarTuningTable["pzkFranklinTriumphTWD91LSU"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]
	NewCarTuningTable["pzkChevalierLaserLSU"] = NewCarTuningTable["pzkChevalierLaserPoliceBulletinSheriff"]
	NewCarTuningTable["pzkFranklinGalloperLSU"] = NewCarTuningTable["pzkChevalierLaserPoliceBulletinSheriff"]
	NewCarTuningTable["pzkFranklinTriumphWagonLSU"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]
	NewCarTuningTable["pzkFranklinTriumphLSU"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]
	NewCarTuningTable["pzkChevalierCerise93LSU"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]
	NewCarTuningTable["pzkChevalierCerise93WagonLSU"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]
		
	NewCarTuningTable["pzkFranklinTriumphTWDMall"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]
	NewCarTuningTable["pzkChevalierCeriseSedanMall"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]
	NewCarTuningTable["pzkChevalierNyalaMall"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]
	NewCarTuningTable["pzkDashMayorMall"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]
	NewCarTuningTable["pzkFranklinTriumphTWD91Mall"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]
	NewCarTuningTable["pzkChevalierLaserMall"] = NewCarTuningTable["pzkChevalierLaserPoliceBulletinSheriff"]
	NewCarTuningTable["pzkFranklinGalloperMall"] = NewCarTuningTable["pzkChevalierLaserPoliceBulletinSheriff"]
	NewCarTuningTable["pzkFranklinTriumphWagonMall"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]
	NewCarTuningTable["pzkFranklinTriumphMall"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]
	NewCarTuningTable["pzkChevalierCerise93Mall"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]
	NewCarTuningTable["pzkChevalierCerise93WagonMall"] = NewCarTuningTable["pzkChevalierCeriseSedanFire"]
	
	NewCarTuningTable["pzkChevalierMaroca80Bulletin"] = NewCarTuningTable["pzkFranklinStallionPoliceLouisvillePD"]
	NewCarTuningTable["pzkChevalierMaroca80KST"] = NewCarTuningTable["pzkFranklinStallionPoliceLouisvillePD"]
	NewCarTuningTable["pzkChevalierMaroca80LV"] = NewCarTuningTable["pzkFranklinStallionPoliceLouisvillePD"]
	NewCarTuningTable["pzkChevalierMaroca80Muld"] = NewCarTuningTable["pzkFranklinStallionPoliceLouisvillePD"]
	
	NewCarTuningTable["pzkChevalierMarocaPoliceLV"] = NewCarTuningTable["pzkFranklinStallionPoliceLouisvillePD"]
	NewCarTuningTable["pzkChevalierMarocaPoliceWP"] = NewCarTuningTable["pzkFranklinStallionPoliceLouisvillePD"]
	NewCarTuningTable["pzkChevalierMarocaPoliceMeade"] = NewCarTuningTable["pzkFranklinStallionPoliceLouisvillePD"]
	
	NewCarTuningTable["pzkDash600"] = NewCarTuningTable["pzkContinentalCruiser"]
	NewCarTuningTable["pzkDashCirilla"] = NewCarTuningTable["pzkContinentalCruiser"]
	NewCarTuningTable["pzkDashDecade"] = NewCarTuningTable["pzkContinentalCruiser"]
	
	NewCarTuningTable["pzkDashPhoenix75"] = NewCarTuningTable["pzkChevalierE6Van"]
	NewCarTuningTable["pzkDashPhoenix75JP"] = NewCarTuningTable["pzkChevalierE6Van"]

		NewCarTuningTable["pzkDashPhoenix80SmashedFront"] = NewCarTuningTable["pzkChevalierE6Van"]
		
	NewCarTuningTable["pzkContinentalBugRedT"] = NewCarTuningTable["pzkContinentalBug"]
		
	NewCarTuningTable["pzkTrailerRegularWaterTankerTainted"] = NewCarTuningTable["pzkTrailerRegularFuelTanker"]
	NewCarTuningTable["pzkTrailerRegularWaterTanker"] = NewCarTuningTable["pzkTrailerRegularFuelTanker"]
	NewCarTuningTable["pzkTrailerRegularCourtainsWhite"] = NewCarTuningTable["pzkTrailerRegularCourtains"]
	NewCarTuningTable["pzkTrailerRegularContainer"] = NewCarTuningTable["pzkTrailerRegularCourtains"]
	NewCarTuningTable["pzkTrailerRegularFedLog"] = NewCarTuningTable["pzkTrailerRegularCourtains"]
	NewCarTuningTable["pzkTrailerRegularGigamart"] = NewCarTuningTable["pzkTrailerRegularCourtains"]
	NewCarTuningTable["pzkTrailerRegularPharmahung"] = NewCarTuningTable["pzkTrailerRegularCourtains"]
	NewCarTuningTable["pzkTrailerRegularValutech"] = NewCarTuningTable["pzkTrailerRegularCourtains"]
	NewCarTuningTable["pzkTrailerRegularUStoreIt"] = NewCarTuningTable["pzkTrailerRegularCourtains"]
	NewCarTuningTable["pzkTrailerRegularCourtainsKnight"] = NewCarTuningTable["pzkTrailerRegularCourtains"]
	NewCarTuningTable["pzkTrailerRegularPropaneTanker"] = NewCarTuningTable["pzkTrailerRegularCourtains"]
	NewCarTuningTable["pzkTrailerRegularLivestock"] = NewCarTuningTable["pzkTrailerRegularCourtains"]
	NewCarTuningTable["pzkTrailerRegularLivestock2"] = NewCarTuningTable["pzkTrailerRegularCourtains"]

	NewCarTuningTable["pzkTrailerRegularWaterTankerArmy"] = NewCarTuningTable["pzkTrailerRegularFuelTanker"]
	NewCarTuningTable["pzkTrailerRegularFuelTankerArmy"] = NewCarTuningTable["pzkTrailerRegularFuelTanker"]
	NewCarTuningTable["pzkVanilaVanAmbulance"] = NewCarTuningTable["pzkStepVanIceCream"]
	
	NewCarTuningTable["pzkFreightlinerFlatOptimus"] = NewCarTuningTable["pzkFreightlinerFlat"]
	NewCarTuningTable["pzkFreightlinerFlatSpiffo"] = NewCarTuningTable["pzkFreightlinerFlat"]
	NewCarTuningTable["pzkPeterbuiltFossoil"] = NewCarTuningTable["pzkFreightlinerFlat"]
	
	NewCarTuningTable["pzkPeterbuiltPop"] = NewCarTuningTable["pzkFreightlinerFlat"]
	NewCarTuningTable["pzkPeterbuiltSleeperPop"] = NewCarTuningTable["pzkFreightlinerFlat"]
	NewCarTuningTable["pzkPeterbuiltSleeperOptimus"] = NewCarTuningTable["pzkFreightlinerFlat"]
	
	NewCarTuningTable["pzkTrailerRegularSpiffo"] = NewCarTuningTable["pzkTrailerRegularCourtains"]
	NewCarTuningTable["pzkTrailerRegularPop"] = NewCarTuningTable["pzkTrailerRegularCourtains"]
	NewCarTuningTable["pzkTrailerRegularUStoreIt"] = NewCarTuningTable["pzkTrailerRegularCourtains"]
	NewCarTuningTable["pzkTrailerRegularValutech"] = NewCarTuningTable["pzkTrailerRegularCourtains"]
	NewCarTuningTable["pzkTrailerRegularPharmahung"] = NewCarTuningTable["pzkTrailerRegularCourtains"]
	NewCarTuningTable["pzkTrailerRegularGigamart"] = NewCarTuningTable["pzkTrailerRegularCourtains"]
	NewCarTuningTable["pzkTrailerRegularFedLog"] = NewCarTuningTable["pzkTrailerRegularCourtains"]
	NewCarTuningTable["pzkTrailerRegularContainer"] = NewCarTuningTable["pzkTrailerRegularCourtains"]
	NewCarTuningTable["pzkTrailerRegularCourtainsWhite"] = NewCarTuningTable["pzkTrailerRegularCourtains"]
	NewCarTuningTable["pzkTrailerRegularCourtains"] = NewCarTuningTable["pzkTrailerRegularCourtains"]
	
	NewCarTuningTable["pzkTractor2"] = NewCarTuningTable["pzkTractor"]
	NewCarTuningTable["pzkTractor3"] = NewCarTuningTable["pzkTractor"]
	
	NewCarTuningTable["pzkContinentalGuardian"] = NewCarTuningTable["pzkContinentalCruiser"]
	NewCarTuningTable["pzkContinentalGuardianLlama"] = NewCarTuningTable["pzkContinentalCruiser"]
	NewCarTuningTable["pzkContinentalGuardianService"] = NewCarTuningTable["pzkContinentalCruiser"]
	NewCarTuningTable["pzkMastersonApex4D"] = NewCarTuningTable["pzkContinentalCruiser"]
	
	NewCarTuningTable["pzkChevalierLaserK5"] = NewCarTuningTable["pzkFranklinGalloper"]
	NewCarTuningTable["pzkStepVanPierogi"] = NewCarTuningTable["pzkStepVanIceCream"]
	
	NewCarTuningTable["pzkFranklinTruckSemiMadMax"] = NewCarTuningTable["pzkFranklinTruckSemi"]
	NewCarTuningTable["pzkF150UtilityAirport"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkF150UtilityMoore"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkF150UtilityNewCoalfieldMechanic"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkF150UtilityIrvingtonSpeedway"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkF150UtilityRanger"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkF150UtilityFire"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkF150UtilityLVPD"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkF150UtilityMuldPD"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkF150UtilityKSTPD"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkF150UtilityBulletinPD"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkF150BoxFlatbed"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkF150BoxFlatbedPublicWorks"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkF150UtilityPublicWorks"] = NewCarTuningTable["pzkF150Utility"]
	
	NewCarTuningTable["pzkFranklinTruckFlatbedPublicWorks"] = NewCarTuningTable["pzkFranklinTruckBed"]
	NewCarTuningTable["pzkPickUpTruckPublicWorks"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkVanPublicWorks"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkFranklinTruckDumpPublicWorks"] = NewCarTuningTable["pzkFranklinTruckFireTanker"]
	
	NewCarTuningTable["pzkDash150Utility"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkDash150UtilityAirport"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkDash150UtilityMoore"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkDash150UtilityNewCoalfieldMechanic"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkDash150UtilityIrvingtonSpeedway"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkDash150UtilityRanger"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkDash150UtilityFire"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkDash150UtilityLVPD"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkDash150UtilityMuldPD"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkDash150UtilityKSTPD"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkDash150UtilityBulletinPD"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkDash150BoxFlatbed"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkDash150BoxFlatbedPublicWorks"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkDash150UtilityPublicWorks"] = NewCarTuningTable["pzkF150Utility"]
	
	NewCarTuningTable["pzkChevalier150Utility"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkChevalier150UtilityAirport"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkChevalier150UtilityMoore"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkChevalier150UtilityNewCoalfieldMechanic"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkChevalier150UtilityIrvingtonSpeedway"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkChevalier150UtilityRanger"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkChevalier150UtilityFire"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkChevalier150UtilityLVPD"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkChevalier150UtilityMuldPD"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkChevalier150UtilityKSTPD"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkChevalier150UtilityBulletinPD"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkChevalier150BoxFlatbed"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkChevalier150BoxFlatbedPublicWorks"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkChevalier150UtilityPublicWorks"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkChevalier150AnimalBFRF"] = NewCarTuningTable["pzkF150Utility"]
	
	
	NewCarTuningTable["pzkMastersonTR2Kouki"] = NewCarTuningTable["pzkChevalierE6Van"]
	NewCarTuningTable["pzkMastersonTR2Zenki"] = NewCarTuningTable["pzkChevalierE6Van"]
	
	NewCarTuningTable["pzkCVanCargo"] = NewCarTuningTable["pzkStepVanMilk"]
	NewCarTuningTable["pzkCVan"] = NewCarTuningTable["pzkStepVanMilk"]
	NewCarTuningTable["pzkCVanMultivan"] = NewCarTuningTable["pzkVanMultivan"]
	NewCarTuningTable["pzkCVan6Seats"] = NewCarTuningTable["pzkVanSeatsTaxi"]
	NewCarTuningTable["pzkCVanCargo3"] = NewCarTuningTable["pzkStepVanMilk"]
	NewCarTuningTable["pzkCVan3"] = NewCarTuningTable["pzkStepVanMilk"]
	NewCarTuningTable["pzkCVanMultivan3"] = NewCarTuningTable["pzkVanMultivan"]
	NewCarTuningTable["pzkCVan6Seats3"] = NewCarTuningTable["pzkVanSeatsTaxi"]
	
	NewCarTuningTable["pzkDVanCargo"] = NewCarTuningTable["pzkStepVanMilk"]
	NewCarTuningTable["pzkDVan"] = NewCarTuningTable["pzkStepVanMilk"]
	NewCarTuningTable["pzkDVanMultivan"] = NewCarTuningTable["pzkVanMultivan"]
	NewCarTuningTable["pzkDVan6Seats"] = NewCarTuningTable["pzkVanSeatsTaxi"]
	NewCarTuningTable["pzkDVanCargo2"] = NewCarTuningTable["pzkStepVanMilk"]
	NewCarTuningTable["pzkDVan2"] = NewCarTuningTable["pzkStepVanMilk"]
	NewCarTuningTable["pzkDVanMultivan2"] = NewCarTuningTable["pzkVanMultivan"]
	NewCarTuningTable["pzkDVan6Seats2"] = NewCarTuningTable["pzkVanSeatsTaxi"]
	NewCarTuningTable["pzkDVanCargo3"] = NewCarTuningTable["pzkStepVanMilk"]
	NewCarTuningTable["pzkDVan3"] = NewCarTuningTable["pzkStepVanMilk"]
	NewCarTuningTable["pzkDVanMultivan3"] = NewCarTuningTable["pzkVanMultivan"]
	NewCarTuningTable["pzkDVan6Seats3"] = NewCarTuningTable["pzkVanSeatsTaxi"]
	
	NewCarTuningTable["pzkFVanCargo"] = NewCarTuningTable["pzkStepVanMilk"]
	NewCarTuningTable["pzkFVan"] = NewCarTuningTable["pzkStepVanMilk"]
	NewCarTuningTable["pzkFVanMultivan"] = NewCarTuningTable["pzkVanMultivan"]
	NewCarTuningTable["pzkFVan6Seats"] = NewCarTuningTable["pzkVanSeatsTaxi"]
	
	NewCarTuningTable["pzkFVanSpiffo"] = NewCarTuningTable["pzkStepVanMilk"]
	NewCarTuningTable["pzkDVanSpiffo"] = NewCarTuningTable["pzkStepVanMilk"]
	NewCarTuningTable["pzkDVan2Spiffo"] = NewCarTuningTable["pzkStepVanMilk"]
	NewCarTuningTable["pzkDVan3Spiffo"] = NewCarTuningTable["pzkStepVanMilk"]
	
	NewCarTuningTable["pzkMastersonSil80"] = NewCarTuningTable["pzkFranklinGalloper"]
	NewCarTuningTable["pzkMastersonSil80Mako"] = NewCarTuningTable["pzkFranklinGalloper"]

	NewCarTuningTable["pzkTruckDashW35WaterMil"] = NewCarTuningTable["pzkTruckDashW35FuelMil"]

	NewCarTuningTable["pzkTruckD70Dump2"] = NewCarTuningTable["pzkTruckD70Dump"]
	NewCarTuningTable["pzkTruckD70Tow"] = NewCarTuningTable["pzkTruckD70Dump"]
	NewCarTuningTable["pzkTruckD70Tow2Wallace"] = NewCarTuningTable["pzkTruckD70Dump"]
	NewCarTuningTable["pzkTruckD70Tow2"] = NewCarTuningTable["pzkTruckD70Dump"]
	NewCarTuningTable["pzkTruckD70Tow2Bernie"] = NewCarTuningTable["pzkTruckD70Dump"]
	
	NewCarTuningTable["pzkStepVanB30"] = NewCarTuningTable["pzkStepVanMilk"]
	NewCarTuningTable["pzkStepVanB30Christmas"] = NewCarTuningTable["pzkStepVanMilk"]
	
	NewCarTuningTable["pzkChevalierD100Pickup"] = NewCarTuningTable["pzkChevalierE6"]
	NewCarTuningTable["pzkChevalierD100PickupCustom"] = NewCarTuningTable["pzkChevalierE6"]
	
	NewCarTuningTable["pzkStepVanB30Rail"] = NewCarTuningTable["pzkStepVanMilk"]
	NewCarTuningTable["pzkStepVanB30BusWoodcrafterMiddle"] = NewCarTuningTable["pzkFranklinTruckBus"]
	NewCarTuningTable["pzkStepVanB30BusChurchWoodcrafterMiddle"] = NewCarTuningTable["pzkFranklinTruckBus"]
	NewCarTuningTable["pzkDashNational980"] = NewCarTuningTable["pzkFreightlinerFlat"]
	NewCarTuningTable["pzkChevalier150UtilityRail"] = NewCarTuningTable["pzkF150Utility"]
	NewCarTuningTable["pzkChevalierE6Rail"] = NewCarTuningTable["pzkChevalierE6Van"]
	NewCarTuningTable["pzkChevalierF6Rail"] = NewCarTuningTable["pzkChevalierE6Van"]
	NewCarTuningTable["pzkChevalierPickupCrewLongRail"] = NewCarTuningTable["pzkChevalierPickupCrewLong"]
	NewCarTuningTable["pzkChevalierPickupCrewMediumRail"] = NewCarTuningTable["pzkChevalierPickupCrewMedium"]
	NewCarTuningTable["pzkCVanRail"] = NewCarTuningTable["pzkStepVanMilk"]
	NewCarTuningTable["pzkCVan2Rail"] = NewCarTuningTable["pzkVanMultivan"]
	NewCarTuningTable["pzkCVan3Rail"] = NewCarTuningTable["pzkStepVanMilk"]
	
	NewCarTuningTable["pzkDashMayorFire"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
	NewCarTuningTable["pzkChevalierNyalaFire"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
	NewCarTuningTable["pzkFranklinTriumphTWD91Fire"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
	NewCarTuningTable["pzkDashCheyeneFire"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
	NewCarTuningTable["pzkChevalierCeriseSedanRanger"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
	NewCarTuningTable["pzkDashMayorRanger"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
	NewCarTuningTable["pzkFranklinTriumphTWDRanger"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
	NewCarTuningTable["pzkFranklinTriumphTWD91Ranger"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]
	NewCarTuningTable["pzkDashCheyeneRanger"] = NewCarTuningTable["pzkChevalierCeriseSedanPoliceBulletinSheriff"]

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierE6", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierE6", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierE6", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierE6", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkChevalierE6", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierE6", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkChevalierE6", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierE6", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierE6", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkChevalierE6", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkChevalierE6", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkChevalierE6", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkChevalierE6", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierE6Van", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierE6Van", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierE6Van", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierE6Van", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkChevalierE6Van", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierE6Van", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkChevalierE6Van", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierE6Van", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierE6Van", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkChevalierE6Van", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkChevalierE6Van", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkChevalierE6Van", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkChevalierE6Van", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkChevalierE6Van", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin350FPickupCrewLong", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin350FPickupCrewLong", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin350FPickupCrewLong", "ATA2ProtectionWindowRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin350FPickupCrewLong", "ATA2ProtectionWindowRearRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin350FPickupCrewLong", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin350FPickupCrewLong", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkFranklin350FPickupCrewLong", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin350FPickupCrewLong", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkFranklin350FPickupCrewLong", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin350FPickupCrewLong", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin350FPickupCrewLong", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin350FPickupCrewLong", "ATA2ProtectionDoorRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin350FPickupCrewLong", "ATA2ProtectionDoorRearRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkFranklin350FPickupCrewLong", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkFranklin350FPickupCrewLong", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkFranklin350FPickupCrewLong", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkFranklin350FPickupCrewLong", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin250FWagonLong", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin250FWagonLong", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin250FWagonLong", "ATA2ProtectionWindowRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin250FWagonLong", "ATA2ProtectionWindowRearRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin250FWagonLong", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin250FWagonLong", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkFranklin250FWagonLong", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin250FWagonLong", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkFranklin250FWagonLong", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin250FWagonLong", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin250FWagonLong", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin250FWagonLong", "ATA2ProtectionDoorRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin250FWagonLong", "ATA2ProtectionDoorRearRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkFranklin250FWagonLong", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkFranklin250FWagonLong", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkFranklin250FWagonLong", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkFranklin250FWagonLong", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkFranklin250FWagonLong", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin250FPickupCrewLong", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin250FPickupCrewLong", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin250FPickupCrewLong", "ATA2ProtectionWindowRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin250FPickupCrewLong", "ATA2ProtectionWindowRearRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin250FPickupCrewLong", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin250FPickupCrewLong", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkFranklin250FPickupCrewLong", "ATA2Bullbar")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkFranklin250FPickupCrewLong", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin250FPickupCrewLong", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkFranklin250FPickupCrewLong", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin250FPickupCrewLong", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin250FPickupCrewLong", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkFranklin250FPickupCrewLong", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkFranklin250FPickupCrewLong", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkFranklin250FPickupCrewLong", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkFranklin250FPickupCrewLong", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashIntruder250WagonLong", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashIntruder250WagonLong", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashIntruder250WagonLong", "ATA2ProtectionWindowRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashIntruder250WagonLong", "ATA2ProtectionWindowRearRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashIntruder250WagonLong", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashIntruder250WagonLong", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkDashIntruder250WagonLong", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashIntruder250WagonLong", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkDashIntruder250WagonLong", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashIntruder250WagonLong", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashIntruder250WagonLong", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkDashIntruder250WagonLong", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkDashIntruder250WagonLong", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkDashIntruder250WagonLong", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkDashIntruder250WagonLong", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkDashIntruder250WagonLong", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierRoadrunner", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierRoadrunner", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierRoadrunner", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierRoadrunner", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkChevalierRoadrunner", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierRoadrunner", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkChevalierRoadrunner", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierRoadrunner", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierRoadrunner", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkChevalierRoadrunner", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkChevalierRoadrunner", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkChevalierRoadrunner", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkChevalierRoadrunner", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalBug", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalBug", "ATA2ProtectionWindowFrontRight")
		--SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalBug", "ATA2ProtectionWindowRearLeft")
		--SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalBug", "ATA2ProtectionWindowRearRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalBug", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalBug", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkContinentalBug", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalBug", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalBug", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalBug", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalBug", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkContinentalBug", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkContinentalBug", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkContinentalBug", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkContinentalBug", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkContinentalBug", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalCruiser", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalCruiser", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalCruiser", "ATA2ProtectionWindowRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalCruiser", "ATA2ProtectionWindowRearRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalCruiser", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalCruiser", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkContinentalCruiser", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalCruiser", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkContinentalCruiser", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalCruiser", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalCruiser", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalCruiser", "ATA2ProtectionDoorRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalCruiser", "ATA2ProtectionDoorRearRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkContinentalCruiser", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkContinentalCruiser", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkContinentalCruiser", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkContinentalCruiser", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkContinentalCruiser", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashOhio", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkDashOhio", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashOhio", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkDashOhio", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashOhio", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashOhio", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkDashOhio", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkDashOhio", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkDashOhio", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinGalloper", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinGalloper", "ATA2ProtectionWindowFrontRight")
		--SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinGalloper", "ATA2ProtectionWindowRearLeft")
		--SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinGalloper", "ATA2ProtectionWindowRearRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinGalloper", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinGalloper", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkFranklinGalloper", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinGalloper", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkFranklinGalloper", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinGalloper", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinGalloper", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkFranklinGalloper", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkFranklinGalloper", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkFranklinGalloper", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkFranklinGalloper", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkFranklinGalloper", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinBankTruck", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinBankTruck", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinBankTruck", "ATA2ProtectionWindowRearRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinBankTruck", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinBankTruck", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbarsTruck(NewCarTuningTable, carRecipe, "pzkFranklinBankTruck", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinBankTruck", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkFranklinBankTruck", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinBankTruck", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinBankTruck", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinBankTruck", "ATA2ProtectionDoorRearRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkFranklinBankTruck", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkFranklinBankTruck", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkFranklinBankTruck", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkFranklinBankTruck", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkFranklinBankTruck", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinStallionPoliceLouisvillePD", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinStallionPoliceLouisvillePD", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinStallionPoliceLouisvillePD", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinStallionPoliceLouisvillePD", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkFranklinStallionPoliceLouisvillePD", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinStallionPoliceLouisvillePD", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkFranklinStallionPoliceLouisvillePD", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinStallionPoliceLouisvillePD", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinStallionPoliceLouisvillePD", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkFranklinStallionPoliceLouisvillePD", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkFranklinStallionPoliceLouisvillePD", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkFranklinStallionPoliceLouisvillePD", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkFranklinStallionPoliceLouisvillePD", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckBed", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckBed", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckBed", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckBed", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbarsTruck(NewCarTuningTable, carRecipe, "pzkFranklinTruckBed", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckBed", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkFranklinTruckBed", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckBed", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckBed", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkFranklinTruckBed", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkFranklinTruckBed", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkFranklinTruckBed", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkFranklinTruckBed", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckMcCoy", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckMcCoy", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckMcCoy", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckMcCoy", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbarsTruck(NewCarTuningTable, carRecipe, "pzkFranklinTruckMcCoy", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckMcCoy", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkFranklinTruckMcCoy", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckMcCoy", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckMcCoy", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkFranklinTruckMcCoy", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkFranklinTruckMcCoy", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkFranklinTruckMcCoy", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkFranklinTruckMcCoy", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckBox", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckBox", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckBox", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckBox", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbarsTruck(NewCarTuningTable, carRecipe, "pzkFranklinTruckBox", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckBox", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkFranklinTruckBox", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckBox", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckBox", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkFranklinTruckBox", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkFranklinTruckBox", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkFranklinTruckBox", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkFranklinTruckBox", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkFranklinTruckBox", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckBoxLectromax", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckBoxLectromax", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckBoxLectromax", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckBoxLectromax", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbarsTruck(NewCarTuningTable, carRecipe, "pzkFranklinTruckBoxLectromax", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckBoxLectromax", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkFranklinTruckBoxLectromax", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckBoxLectromax", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckBoxLectromax", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkFranklinTruckBoxLectromax", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkFranklinTruckBoxLectromax", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkFranklinTruckBoxLectromax", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkFranklinTruckBoxLectromax", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkFranklinTruckBoxLectromax", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckBus", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckBus", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckBus", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckBus", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbarsTruck(NewCarTuningTable, carRecipe, "pzkFranklinTruckBus", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckBus", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkFranklinTruckBus", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckBus", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkFranklinTruckBus", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkFranklinTruckBus", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkFranklinTruckBus", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkFranklinTruckBus", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkFranklinTruckBus", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckCab", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckCab", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckCab", "ATA2ProtectionWindowRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckCab", "ATA2ProtectionWindowRearRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckCab", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckCab", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkFranklinTruckCab", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckCab", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkFranklinTruckCab", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckCab", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckCab", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckCab", "ATA2ProtectionDoorRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckCab", "ATA2ProtectionDoorRearRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkFranklinTruckCab", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkFranklinTruckCab", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkFranklinTruckCab", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkFranklinTruckCab", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkFranklinTruckCab", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckFireTanker", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckFireTanker", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckFireTanker", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckFireTanker", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbarsTruck(NewCarTuningTable, carRecipe, "pzkFranklinTruckFireTanker", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckFireTanker", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkFranklinTruckFireTanker", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckFireTanker", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckFireTanker", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkFranklinTruckFireTanker", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkFranklinTruckFireTanker", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkFranklinTruckFireTanker", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkFranklinTruckFireTanker", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkStepVanMilk", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkStepVanMilk", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkStepVanMilk", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkStepVanMilk", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkStepVanMilk", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkStepVanMilk", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkStepVanMilk", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkStepVanMilk", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkStepVanMilk", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkStepVanMilk", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkStepVanMilk", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkStepVanMilk", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkStepVanMilk", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkStepVanMilk", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkStepVanIceCream", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkStepVanIceCream", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkStepVanIceCream", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkStepVanIceCream", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkStepVanIceCream", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkStepVanIceCream", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkStepVanIceCream", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkStepVanIceCream", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkStepVanIceCream", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkStepVanIceCream", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkStepVanIceCream", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkStepVanIceCream", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkStepVanIceCream", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkHMMV3", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkHMMV3", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkHMMV3", "ATA2ProtectionWindowRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkHMMV3", "ATA2ProtectionWindowRearRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkHMMV3", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkHMMV3", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkHMMV3", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkHMMV3", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkHMMV3", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkHMMV3", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkHMMV3", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkHMMV3", "ATA2ProtectionDoorRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkHMMV3", "ATA2ProtectionDoorRearRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkHMMV3", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkHMMV3", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkHMMV3", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkHMMV3", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkHMMV3", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivanMPV", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivanMPV", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivanMPV", "ATA2ProtectionWindowRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivanMPV", "ATA2ProtectionWindowRearRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivanMPV", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivanMPV", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkMinivanMPV", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivanMPV", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkMinivanMPV", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivanMPV", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivanMPV", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivanMPV", "ATA2ProtectionDoorRearRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkMinivanMPV", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkMinivanMPV", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkMinivanMPV", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkMinivanMPV", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkMinivanMPV", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivan2", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivan2", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivan2", "ATA2ProtectionWindowRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivan2", "ATA2ProtectionWindowRearRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivan2", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivan2", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkMinivan2", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivan2", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkMinivan2", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivan2", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivan2", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivan2", "ATA2ProtectionDoorRearRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkMinivan2", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkMinivan2", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkMinivan2", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkMinivan2", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkMinivan2", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivanT3C", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivanT3C", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivanT3C", "ATA2ProtectionWindowRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivanT3C", "ATA2ProtectionWindowRearRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivanT3C", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivanT3C", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkMinivanT3C", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivanT3C", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkMinivanT3C", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivanT3C", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivanT3C", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivanT3C", "ATA2ProtectionDoorRearRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkMinivanT3C", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkMinivanT3C", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkMinivanT3C", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanSeatsTaxi", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanSeatsTaxi", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanSeatsTaxi", "ATA2ProtectionWindowMiddleLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanSeatsTaxi", "ATA2ProtectionWindowMiddleRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanSeatsTaxi", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanSeatsTaxi", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkVanSeatsTaxi", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanSeatsTaxi", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkVanSeatsTaxi", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanSeatsTaxi", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanSeatsTaxi", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanSeatsTaxi", "ATA2ProtectionDoorMiddleLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanSeatsTaxi", "ATA2ProtectionDoorMiddleRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkVanSeatsTaxi", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkVanSeatsTaxi", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkVanSeatsTaxi", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkVanSeatsTaxi", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanPoliceLouisvillePD", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanPoliceLouisvillePD", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanPoliceLouisvillePD", "ATA2ProtectionWindowRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanPoliceLouisvillePD", "ATA2ProtectionWindowRearRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanPoliceLouisvillePD", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanPoliceLouisvillePD", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkVanPoliceLouisvillePD", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanPoliceLouisvillePD", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkVanPoliceLouisvillePD", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanPoliceLouisvillePD", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanPoliceLouisvillePD", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanPoliceLouisvillePD", "ATA2ProtectionDoorRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanPoliceLouisvillePD", "ATA2ProtectionDoorRearRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkVanPoliceLouisvillePD", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkVanPoliceLouisvillePD", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkVanPoliceLouisvillePD", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkVanPoliceLouisvillePD", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanCamper", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanCamper", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanCamper", "ATA2ProtectionWindowRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanCamper", "ATA2ProtectionWindowRearRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanCamper", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanCamper", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkVanCamper", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanCamper", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkVanCamper", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanCamper", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanCamper", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanCamper", "ATA2ProtectionDoorRearRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkVanCamper", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkVanCamper", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkVanCamper", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanMultivan", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanMultivan", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanMultivan", "ATA2ProtectionWindowRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanMultivan", "ATA2ProtectionWindowRearRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanMultivan", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanMultivan", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkVanMultivan", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanMultivan", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkVanMultivan", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanMultivan", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanMultivan", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanMultivan", "ATA2ProtectionDoorRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanMultivan", "ATA2ProtectionDoorRearRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkVanMultivan", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkVanMultivan", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkVanMultivan", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkVanMultivan", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkVanMultivan", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierVan70", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierVan70", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierVan70", "ATA2ProtectionWindowRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierVan70", "ATA2ProtectionWindowRearRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierVan70", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierVan70", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkChevalierVan70", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierVan70", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkChevalierVan70", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierVan70", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierVan70", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierVan70", "ATA2ProtectionDoorRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierVan70", "ATA2ProtectionDoorRearRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkChevalierVan70", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkChevalierVan70", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkChevalierVan70", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkChevalierVan70", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkChevalierVan70", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanBoxAmbulance", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanBoxAmbulance", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanBoxAmbulance", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanBoxAmbulance", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkVanBoxAmbulance", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanBoxAmbulance", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkVanBoxAmbulance", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanBoxAmbulance", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanBoxAmbulance", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkVanBoxAmbulance", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkVanBoxAmbulance", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkVanBoxAmbulance", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkVanBoxAmbulance", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkF350BoxSwat", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkF350BoxSwat", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkF350BoxSwat", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkF350BoxSwat", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkF350BoxSwat", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkF350BoxSwat", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkF350BoxSwat", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkF350BoxSwat", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkF350BoxSwat", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkF350BoxSwat", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkF350BoxSwat", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkF350BoxSwat", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkF350BoxSwat", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkF350BoxCUCV", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkF350BoxCUCV", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkF350BoxCUCV", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkF350BoxCUCV", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkF350BoxCUCV", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkF350BoxCUCV", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkF350BoxCUCV", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkF350BoxCUCV", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkF350BoxCUCV", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkF350BoxCUCV", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkF350BoxCUCV", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkF350BoxCUCV", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkF350BoxCUCV", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkF350BoxUmoveit", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkF350BoxUmoveit", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkF350BoxUmoveit", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkF350BoxUmoveit", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkF350BoxUmoveit", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkF350BoxUmoveit", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkF350BoxUmoveit", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkF350BoxUmoveit", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkF350BoxUmoveit", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkF350BoxUmoveit", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkF350BoxUmoveit", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkF350BoxUmoveit", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkF350BoxUmoveit", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkF350BoxAmbulance", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkF350BoxAmbulance", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkF350BoxAmbulance", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkF350BoxAmbulance", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkF350BoxAmbulance", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkF350BoxAmbulance", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkF350BoxAmbulance", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkF350BoxAmbulance", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkF350BoxAmbulance", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkF350BoxAmbulance", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkF350BoxAmbulance", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkF350BoxAmbulance", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkF350BoxAmbulance", "ATA2Snorkel")


		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanBrig", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanBrig", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanBrig", "ATA2ProtectionWindowRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanBrig", "ATA2ProtectionWindowRearRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanBrig", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanBrig", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkVanBrig", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanBrig", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkVanBrig", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanBrig", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanBrig", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanBrig", "ATA2ProtectionDoorRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanBrig", "ATA2ProtectionDoorRearRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkVanBrig", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkVanBrig", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkVanBrig", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkVanBrig", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkVanBrig", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivanStellarisMail", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivanStellarisMail", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivanStellarisMail", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivanStellarisMail", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkMinivanStellarisMail", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivanStellarisMail", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkMinivanStellarisMail", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivanStellarisMail", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivanStellarisMail", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkMinivanStellarisMail", "ATA2ProtectionDoorRearRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkMinivanStellarisMail", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkMinivanStellarisMail", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkMinivanStellarisMail", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkMinivanStellarisMail", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkMinivanStellarisMail", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanBox", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanBox", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanBox", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkVanBox", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanBox", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkVanBox", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanBox", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanBox", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkVanBox", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkVanBox", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkVanBox", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkVanBox", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkVanBox", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTrailerRegularCourtains", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkTrailerRegularCourtains", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkTrailerRegularCourtains", "ATA2InteractiveTrunkRoofRack")

		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkTrailerRegularFuelTanker", "ATA2ProtectionWheels")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTrailerRegularFlatbed", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkTrailerRegularFlatbed", "ATA2ProtectionWheels")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFreightlinerFlat", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFreightlinerFlat", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFreightlinerFlat", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesBullbarsTruck(NewCarTuningTable, carRecipe, "pzkFreightlinerFlat", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkFreightlinerFlat", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFreightlinerFlat", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFreightlinerFlat", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkFreightlinerFlat", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkFreightlinerFlat", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkFreightlinerFlat", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkFreightlinerFlat", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkFreightlinerFlat", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkPeterbuilt", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkPeterbuilt", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkPeterbuilt", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesBullbarsTruck(NewCarTuningTable, carRecipe, "pzkPeterbuilt", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkPeterbuilt", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkPeterbuilt", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkPeterbuilt", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkPeterbuilt", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkPeterbuilt", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkPeterbuilt", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkPeterbuilt", "ATA2Snorkel")

		--SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTrailerHorseBox", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkTrailerHorseBox", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkTrailerHorseBox", "ATA2InteractiveTrunkRoofRack")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalTRK", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalTRK", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalTRK", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesBullbarsTruck(NewCarTuningTable, carRecipe, "pzkContinentalTRK", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalTRK", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkContinentalTRK", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalTRK", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalTRK", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkContinentalTRK", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkContinentalTRK", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkContinentalTRK", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkContinentalTRK", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkContinentalTRK", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckSemi", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckSemi", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckSemi", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckSemi", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbarsTruck(NewCarTuningTable, carRecipe, "pzkFranklinTruckSemi", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkFranklinTruckSemi", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckSemi", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinTruckSemi", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkFranklinTruckSemi", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkFranklinTruckSemi", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkFranklinTruckSemi", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkFranklinTruckSemi", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFireTruckFlatPumper", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFireTruckFlatPumper", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFireTruckFlatPumper", "ATA2ProtectionWindowRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFireTruckFlatPumper", "ATA2ProtectionWindowRearRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFireTruckFlatPumper", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesBullbarsTruck(NewCarTuningTable, carRecipe, "pzkFireTruckFlatPumper", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFireTruckFlatPumper", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkFireTruckFlatPumper", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFireTruckFlatPumper", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFireTruckFlatPumper", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFireTruckFlatPumper", "ATA2ProtectionDoorRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFireTruckFlatPumper", "ATA2ProtectionDoorRearRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkFireTruckFlatPumper", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkFireTruckFlatPumper", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkFireTruckFlatPumper", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkFireTruckFlatPumper", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkFireTruckFlatPumper", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFireTruckFlatLadder", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFireTruckFlatLadder", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFireTruckFlatLadder", "ATA2ProtectionWindowRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFireTruckFlatLadder", "ATA2ProtectionWindowRearRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFireTruckFlatLadder", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesBullbarsTruck(NewCarTuningTable, carRecipe, "pzkFireTruckFlatLadder", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFireTruckFlatLadder", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkFireTruckFlatLadder", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFireTruckFlatLadder", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFireTruckFlatLadder", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFireTruckFlatLadder", "ATA2ProtectionDoorRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFireTruckFlatLadder", "ATA2ProtectionDoorRearRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkFireTruckFlatLadder", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkFireTruckFlatLadder", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkFireTruckFlatLadder", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkFireTruckFlatLadder", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFireTruckFlatSemi", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFireTruckFlatSemi", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFireTruckFlatSemi", "ATA2ProtectionWindowRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFireTruckFlatSemi", "ATA2ProtectionWindowRearRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFireTruckFlatSemi", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesBullbarsTruck(NewCarTuningTable, carRecipe, "pzkFireTruckFlatSemi", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkFireTruckFlatSemi", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFireTruckFlatSemi", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFireTruckFlatSemi", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFireTruckFlatSemi", "ATA2ProtectionDoorRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFireTruckFlatSemi", "ATA2ProtectionDoorRearRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkFireTruckFlatSemi", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkFireTruckFlatSemi", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkFireTruckFlatSemi", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkFireTruckFlatSemi", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkFireTruckFlatSemi", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTrailerRegularFTSemi", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkTrailerRegularFTSemi", "ATA2ProtectionWheels")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTractor", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTractor", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTractor", "ATA2ProtectionWindowRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTractor", "ATA2ProtectionWindowRearRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTractor", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTractor", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkTractor", "ATA2Bullbar")
		--SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTractor", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTractor", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTractor", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTractor", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkTractor", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkTractor", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkTractor", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkTractor", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalPfeiffer901c", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkContinentalPfeiffer901c", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalPfeiffer901c", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalPfeiffer901c", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalPfeiffer901c", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalPfeiffer901c", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkContinentalPfeiffer901c", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkContinentalPfeiffer901c", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkContinentalPfeiffer901c", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTransitBus", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTransitBus", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTransitBus", "ATA2ProtectionWindowRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTransitBus", "ATA2ProtectionWindowRearRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTransitBus", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTransitBus", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbarsTruck(NewCarTuningTable, carRecipe, "pzkTransitBus", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTransitBus", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkTransitBus", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTransitBus", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTransitBus", "ATA2ProtectionDoorRearRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkTransitBus", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkTransitBus", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkTransitBus", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkTransitBus", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkTransitBus", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashHEMTT6x6semi", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashHEMTT6x6semi", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashHEMTT6x6semi", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesBullbarsTruck(NewCarTuningTable, carRecipe, "pzkDashHEMTT6x6semi", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkDashHEMTT6x6semi", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashHEMTT6x6semi", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashHEMTT6x6semi", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkDashHEMTT6x6semi", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkDashHEMTT6x6semi", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkDashHEMTT6x6semi", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkDashHEMTT6x6semi", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkDashHEMTT6x6semi", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierLaserPoliceBulletinSheriff", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierLaserPoliceBulletinSheriff", "ATA2ProtectionWindowFrontRight")
		--SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierLaserPoliceBulletinSheriff", "ATA2ProtectionWindowRearLeft")
		--SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierLaserPoliceBulletinSheriff", "ATA2ProtectionWindowRearRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierLaserPoliceBulletinSheriff", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierLaserPoliceBulletinSheriff", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkChevalierLaserPoliceBulletinSheriff", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierLaserPoliceBulletinSheriff", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkChevalierLaserPoliceBulletinSheriff", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierLaserPoliceBulletinSheriff", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierLaserPoliceBulletinSheriff", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkChevalierLaserPoliceBulletinSheriff", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkChevalierLaserPoliceBulletinSheriff", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkChevalierLaserPoliceBulletinSheriff", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkChevalierLaserPoliceBulletinSheriff", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPoliceBulletinSheriff", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPoliceBulletinSheriff", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPoliceBulletinSheriff", "ATA2ProtectionWindowRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPoliceBulletinSheriff", "ATA2ProtectionWindowRearRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPoliceBulletinSheriff", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPoliceBulletinSheriff", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPoliceBulletinSheriff", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPoliceBulletinSheriff", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPoliceBulletinSheriff", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPoliceBulletinSheriff", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPoliceBulletinSheriff", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPoliceBulletinSheriff", "ATA2ProtectionDoorRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPoliceBulletinSheriff", "ATA2ProtectionDoorRearRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPoliceBulletinSheriff", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPoliceBulletinSheriff", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPoliceBulletinSheriff", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPoliceBulletinSheriff", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTriumphTWDStationWagonGriswold", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTriumphTWDStationWagonGriswold", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTriumphTWDStationWagonGriswold", "ATA2ProtectionWindowRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTriumphTWDStationWagonGriswold", "ATA2ProtectionWindowRearRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTriumphTWDStationWagonGriswold", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTriumphTWDStationWagonGriswold", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkTriumphTWDStationWagonGriswold", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTriumphTWDStationWagonGriswold", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkTriumphTWDStationWagonGriswold", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTriumphTWDStationWagonGriswold", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTriumphTWDStationWagonGriswold", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTriumphTWDStationWagonGriswold", "ATA2ProtectionDoorRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTriumphTWDStationWagonGriswold", "ATA2ProtectionDoorRearRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkTriumphTWDStationWagonGriswold", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkTriumphTWDStationWagonGriswold", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkTriumphTWDStationWagonGriswold", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkTriumphTWDStationWagonGriswold", "ATA2Snorkel")


		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashVan70Riddle", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashVan70Riddle", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashVan70Riddle", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkDashVan70Riddle", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashVan70Riddle", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkDashVan70Riddle", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashVan70Riddle", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashVan70Riddle", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkDashVan70Riddle", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkDashVan70Riddle", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkDashVan70Riddle", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkDashVan70Riddle", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkDashVan70Riddle", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanMultivanPayday", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanMultivanPayday", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanMultivanPayday", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanMultivanPayday", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkVanMultivanPayday", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanMultivanPayday", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkVanMultivanPayday", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanMultivanPayday", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanMultivanPayday", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanMultivanPayday", "ATA2ProtectionDoorRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanMultivanPayday", "ATA2ProtectionDoorRearRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkVanMultivanPayday", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkVanMultivanPayday", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkVanMultivanPayday", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkVanMultivanPayday", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkVanMultivanPayday", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanZSquad", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanZSquad", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanZSquad", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanZSquad", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkVanZSquad", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanZSquad", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkVanZSquad", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanZSquad", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanZSquad", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanZSquad", "ATA2ProtectionDoorRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanZSquad", "ATA2ProtectionDoorRearRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkVanZSquad", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkVanZSquad", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkVanZSquad", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkVanZSquad", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkVanZSquad", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashNavajoP", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashNavajoP", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashNavajoP", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashNavajoP", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkDashNavajoP", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashNavajoP", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkDashNavajoP", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashNavajoP", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashNavajoP", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkDashNavajoP", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkDashNavajoP", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkDashNavajoP", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkDashNavajoP", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashNavajoW", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashNavajoW", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashNavajoW", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashNavajoW", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkDashNavajoW", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashNavajoW", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkDashNavajoW", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashNavajoW", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkDashNavajoW", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkDashNavajoW", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkDashNavajoW", "ATA2RoofLightFront")
		--SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkDashNavajoW", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkDashNavajoW", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkDashNavajoW", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanGigamart", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanGigamart", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanGigamart", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanGigamart", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkVanGigamart", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanGigamart", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkVanGigamart", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanGigamart", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanGigamart", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkVanGigamart", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkVanGigamart", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkVanGigamart", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkVanGigamart", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkVanGigamart", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkHearseGhoulbusters", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkHearseGhoulbusters", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkHearseGhoulbusters", "ATA2ProtectionWindowRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkHearseGhoulbusters", "ATA2ProtectionWindowRearRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkHearseGhoulbusters", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkHearseGhoulbusters", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkHearseGhoulbusters", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkHearseGhoulbusters", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkHearseGhoulbusters", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkHearseGhoulbusters", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkHearseGhoulbusters", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkHearseGhoulbusters", "ATA2ProtectionDoorRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkHearseGhoulbusters", "ATA2ProtectionDoorRearRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkHearseGhoulbusters", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkHearseGhoulbusters", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkHearseGhoulbusters", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkHearseGhoulbusters", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckD70BFRFHazmat", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckD70BFRFHazmat", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckD70BFRFHazmat", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckD70BFRFHazmat", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbarsTruck(NewCarTuningTable, carRecipe, "pzkTruckD70BFRFHazmat", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckD70BFRFHazmat", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkTruckD70BFRFHazmat", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckD70BFRFHazmat", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckD70BFRFHazmat", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckD70BFRFHazmat", "ATA2ProtectionDoorRearLeft")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkTruckD70BFRFHazmat", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkTruckD70BFRFHazmat", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkTruckD70BFRFHazmat", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkTruckD70BFRFHazmat", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkTruckD70BFRFHazmat", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckDashW35BedMil", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckDashW35BedMil", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckDashW35BedMil", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckDashW35BedMil", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbarsTruck(NewCarTuningTable, carRecipe, "pzkTruckDashW35BedMil", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckDashW35BedMil", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkTruckDashW35BedMil", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckDashW35BedMil", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckDashW35BedMil", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkTruckDashW35BedMil", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkTruckDashW35BedMil", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkTruckDashW35BedMil", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkTruckDashW35BedMil", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckDashW35CabrioMil", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckDashW35CabrioMil", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckDashW35CabrioMil", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesBullbarsTruck(NewCarTuningTable, carRecipe, "pzkTruckDashW35CabrioMil", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckDashW35CabrioMil", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkTruckDashW35CabrioMil", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckDashW35CabrioMil", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckDashW35CabrioMil", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkTruckDashW35CabrioMil", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkTruckDashW35CabrioMil", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkTruckDashW35CabrioMil", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkTruckDashW35CabrioMil", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckDashW35FuelMil", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckDashW35FuelMil", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckDashW35FuelMil", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesBullbarsTruck(NewCarTuningTable, carRecipe, "pzkTruckDashW35FuelMil", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckDashW35FuelMil", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkTruckDashW35FuelMil", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckDashW35FuelMil", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckDashW35FuelMil", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkTruckDashW35FuelMil", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkTruckDashW35FuelMil", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkTruckDashW35FuelMil", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkTruckDashW35FuelMil", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckD70Dump", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckD70Dump", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckD70Dump", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckD70Dump", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbarsTruck(NewCarTuningTable, carRecipe, "pzkTruckD70Dump", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckD70Dump", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkTruckD70Dump", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckD70Dump", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTruckD70Dump", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkTruckD70Dump", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkTruckD70Dump", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkTruckD70Dump", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkTruckD70Dump", "ATA2Snorkel")




		--SVUC_setVehiclePickup(NewCarTuningTable, "pzkTrailerRegularFlatbed")
		--SVUC_setVehiclePickup(NewCarTuningTable, "pzkVanBrig")
		--SVUC_setVehiclePickupTrunkDoor(NewCarTuningTable, "pzkHMMV3")
		--SVUC_setVehiclePickupTrunkDoor(NewCarTuningTable, "pzkHMMV4Mil")
		--SVUC_setVehiclePickupDoorsRear(NewCarTuningTable, "pzkFranklinTruckCab")
		--SVUC_setVehiclePickupDoorsRear(NewCarTuningTable, "pzkFranklinTruckBed")
		--SVUC_setVehiclePickupDoorsRear(NewCarTuningTable, "pzkFranklinTruckMcCoy")
		--SVUC_setVehiclePickup(NewCarTuningTable, "pzkChevalierRoadrunner")
		--SVUC_setVehiclePickup(NewCarTuningTable, "pzkFranklin250FPickupCrewLong")
		--SVUC_setVehiclePickup(NewCarTuningTable, "pzkFranklin350FPickupCrewLong")
		--SVUC_setVehiclePickup(NewCarTuningTable, "pzkChevalierE6")
		--SVUC_setVehiclePickup(NewCarTuningTable, "pzkDashNavajoP")
		--SVUC_setVehiclePickup(NewCarTuningTable, "pzkMastersonIberiaPickup")



		ATA2Tuning_AddNewCars(NewCarTuningTable)
	end
end
Events.OnInitGlobalModData.Add(SVU_TuningTable)
