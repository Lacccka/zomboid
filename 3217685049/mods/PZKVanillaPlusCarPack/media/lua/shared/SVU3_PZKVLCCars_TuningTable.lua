require "ATA2TuningTable"
require "SVUC_TuningTable"
require "SVU3_PZKVLCCars_Stuffs"
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
	if getActivatedMods():contains("StandardizedVehicleUpgrades3Core") then
		local TemplateTuningTable = SVUC_TemplateVehicle()
		local NewCarTuningTable = {}
		local carRecipe = "ATAPZKTuningMag"

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
		NewCarTuningTable["pzkChevalierLaserPolice"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkChevalierCeriseSedanPolice"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkFranklinBankTruck"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkFranklinStallionPolice"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkFranklinTruckBed"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkFranklinTruckBox"] = {
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
		NewCarTuningTable["Vehicles_VanSeatsTaxi"] = {
			addPartsFromVehicleScript = "",
			parts = {}
		}
		NewCarTuningTable["pzkVanPolice"] = {
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

		-- pzkChevalierE6 Pickup
		NewCarTuningTable["pzkChevalierE6"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkChevalierE6"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkChevalierE6"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkChevalierE6"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkChevalierE6"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkChevalierE6"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTrunk"])
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
		NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTrunk"])
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
		NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTrunk"])
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
		NewCarTuningTable["pzkChevalierRoadrunner"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTrunk"])
		NewCarTuningTable["pzkChevalierRoadrunner"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkChevalierRoadrunner"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkChevalierRoadrunner"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkChevalierRoadrunner"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkChevalierRoadrunner"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkChevalierRoadrunner"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkChevalierRoadrunner"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkChevalierRoadrunner"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkContinentalBug
		NewCarTuningTable["pzkContinentalBug"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkContinentalBug"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkContinentalBug"].parts["ATA2ProtectionWindowRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearLeft"])
		NewCarTuningTable["pzkContinentalBug"].parts["ATA2ProtectionWindowRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearRight"])
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
		NewCarTuningTable["pzkFranklinGalloper"].parts["ATA2ProtectionWindowRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearLeft"])
		NewCarTuningTable["pzkFranklinGalloper"].parts["ATA2ProtectionWindowRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearRight"])
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
		NewCarTuningTable["pzkChevalierLaserRanger"].parts["ATA2ProtectionWindowRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearLeft"])
		NewCarTuningTable["pzkChevalierLaserRanger"].parts["ATA2ProtectionWindowRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearRight"])
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
		NewCarTuningTable["pzkFranklinStallionPolice"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkFranklinStallionPolice"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkFranklinStallionPolice"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkFranklinStallionPolice"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkFranklinStallionPolice"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2BullbarPolice"])
		NewCarTuningTable["pzkFranklinStallionPolice"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTrunk"])
		NewCarTuningTable["pzkFranklinStallionPolice"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkFranklinStallionPolice"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkFranklinStallionPolice"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkFranklinStallionPolice"].parts["ATA2RoofLightFront"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2RoofLightFront"])
		NewCarTuningTable["pzkFranklinStallionPolice"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkFranklinStallionPolice"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkFranklinStallionPolice"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkFranklinTruckBed
		NewCarTuningTable["pzkFranklinTruckBed"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkFranklinTruckBed"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkFranklinTruckBed"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkFranklinTruckBed"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkFranklinTruckBed"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2BullbarTruck"])
		NewCarTuningTable["pzkFranklinTruckBed"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRear"])
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
		NewCarTuningTable["pzkFranklinTruckCab"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRear"])
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
		NewCarTuningTable["pzkHMMV3"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTrunk"])
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

		-- Vehicles_VanSeatsTaxi
		NewCarTuningTable["Vehicles_VanSeatsTaxi"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["Vehicles_VanSeatsTaxi"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["Vehicles_VanSeatsTaxi"].parts["ATA2ProtectionWindowMiddleLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowMiddleLeft"])
		NewCarTuningTable["Vehicles_VanSeatsTaxi"].parts["ATA2ProtectionWindowMiddleRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowMiddleRight"])
		NewCarTuningTable["Vehicles_VanSeatsTaxi"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["Vehicles_VanSeatsTaxi"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["Vehicles_VanSeatsTaxi"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["Vehicles_VanSeatsTaxi"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRear"])
		NewCarTuningTable["Vehicles_VanSeatsTaxi"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["Vehicles_VanSeatsTaxi"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["Vehicles_VanSeatsTaxi"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["Vehicles_VanSeatsTaxi"].parts["ATA2ProtectionDoorMiddleLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorMiddleLeft"])
		NewCarTuningTable["Vehicles_VanSeatsTaxi"].parts["ATA2ProtectionDoorMiddleRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorMiddleRight"])
		NewCarTuningTable["Vehicles_VanSeatsTaxi"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["Vehicles_VanSeatsTaxi"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 100
		NewCarTuningTable["Vehicles_VanSeatsTaxi"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["Vehicles_VanSeatsTaxi"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["Vehicles_VanSeatsTaxi"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

		-- pzkVanPolice
		NewCarTuningTable["pzkVanPolice"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkVanPolice"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkVanPolice"].parts["ATA2ProtectionWindowRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearLeft"])
		NewCarTuningTable["pzkVanPolice"].parts["ATA2ProtectionWindowRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearRight"])
		NewCarTuningTable["pzkVanPolice"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkVanPolice"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkVanPolice"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkVanPolice"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRear"])
		NewCarTuningTable["pzkVanPolice"].parts["ATA2ProtectionHood"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionHood"])
		NewCarTuningTable["pzkVanPolice"].parts["ATA2ProtectionDoorFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontLeft"])
		NewCarTuningTable["pzkVanPolice"].parts["ATA2ProtectionDoorFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorFrontRight"])
		NewCarTuningTable["pzkVanPolice"].parts["ATA2ProtectionDoorRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearLeft"])
		NewCarTuningTable["pzkVanPolice"].parts["ATA2ProtectionDoorRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorRearRight"])
		NewCarTuningTable["pzkVanPolice"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkVanPolice"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 100
		NewCarTuningTable["pzkVanPolice"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])
		NewCarTuningTable["pzkVanPolice"].parts["ATA2AirScoop"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2AirScoop"])
		NewCarTuningTable["pzkVanPolice"].parts["ATA2Snorkel"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Snorkel"])

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
		NewCarTuningTable["pzkVanBrig"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTrunk"])
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

		-- pzkTrailerRegularCourtains
		NewCarTuningTable["pzkTrailerRegularCourtains"].parts["ATA2ProtectionDoorsRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionDoorsRear"])
		NewCarTuningTable["pzkTrailerRegularCourtains"].parts["ATA2InteractiveTrunkRoofRack"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2InteractiveTrunkRoofRack"])
		NewCarTuningTable["pzkTrailerRegularCourtains"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 500
		NewCarTuningTable["pzkTrailerRegularCourtains"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])

		-- pzkTrailerRegularFuelTanker
		NewCarTuningTable["pzkTrailerRegularFuelTanker"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])

		-- pzkTrailerRegularFlatbed
		NewCarTuningTable["pzkTrailerRegularFlatbed"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTrunk"])
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
		NewCarTuningTable["pzkTrailerHorseBox"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTrunk"])
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
		NewCarTuningTable["pzkTrailerRegularFTSemi"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTrunk"])
		NewCarTuningTable["pzkTrailerRegularFTSemi"].parts["ATA2ProtectionWheels"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWheels"])

		-- pzkTractor
		NewCarTuningTable["pzkTractor"].parts["ATA2ProtectionWindowFrontLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontLeft"])
		NewCarTuningTable["pzkTractor"].parts["ATA2ProtectionWindowFrontRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowFrontRight"])
		NewCarTuningTable["pzkTractor"].parts["ATA2ProtectionWindowRearLeft"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearLeft"])
		NewCarTuningTable["pzkTractor"].parts["ATA2ProtectionWindowRearRight"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindowRearRight"])
		NewCarTuningTable["pzkTractor"].parts["ATA2ProtectionWindshield"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshield"])
		NewCarTuningTable["pzkTractor"].parts["ATA2ProtectionWindshieldRear"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionWindshieldRear"])
		NewCarTuningTable["pzkTractor"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2Bullbar"])
		NewCarTuningTable["pzkTractor"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTrunk"])
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
		NewCarTuningTable["pzkTransitBus"].parts["ATA2ProtectionTrunk"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2ProtectionTrunk"])
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

		NewCarTuningTable["pzkChevalierLaserPolice"] = copy(NewCarTuningTable["pzkChevalierLaserRanger"])
		NewCarTuningTable["pzkChevalierLaserPolice"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2BullbarPoliceSUV"])

		NewCarTuningTable["pzkChevalierCeriseSedanPolice"] = copy(NewCarTuningTable["pzkChevalierCeriseSedanFire"])
		NewCarTuningTable["pzkChevalierCeriseSedanPolice"].parts["ATA2Bullbar"] = copy(TemplateTuningTable["TemplateVehicle"].parts["ATA2BullbarPolice"])

		NewCarTuningTable["pzkFranklinGalloperPolice"] = NewCarTuningTable["pzkChevalierLaserPolice"]

		NewCarTuningTable["pzkDashMayorPolice"] = NewCarTuningTable["pzkChevalierCeriseSedanPolice"]
		NewCarTuningTable["pzkFranklinTriumphTWDPolice"] = NewCarTuningTable["pzkChevalierCeriseSedanPolice"]
		NewCarTuningTable["pzkFranklinTriumphPolice"] = NewCarTuningTable["pzkChevalierCeriseSedanPolice"]
		NewCarTuningTable["pzkChevalierCerise93Police"] = NewCarTuningTable["pzkChevalierCeriseSedanPolice"]

		NewCarTuningTable["pzkChevalierF6"] = NewCarTuningTable["pzkChevalierE6"]
		NewCarTuningTable["pzkFranklin150FPickupReg"] = NewCarTuningTable["pzkChevalierE6"]
		NewCarTuningTable["pzkFtypeTowTruck"] = NewCarTuningTable["pzkChevalierE6"]
		NewCarTuningTable["pzkPickUpTruckWoodboarded"] = NewCarTuningTable["pzkChevalierE6"]
		NewCarTuningTable["pzkPickupFranklin"] = NewCarTuningTable["pzkChevalierE6"]
		NewCarTuningTable["pzkChevalierTowTruck"] = NewCarTuningTable["pzkChevalierE6"]
		NewCarTuningTable["pzkChevalierTowTruckPolice"] = NewCarTuningTable["pzkChevalierE6"]
		NewCarTuningTable["pzkChevalierTowTruckFire"] = NewCarTuningTable["pzkChevalierE6"]
		NewCarTuningTable["pzkPickUpTruck93"] = NewCarTuningTable["pzkChevalierE6"]

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
		NewCarTuningTable["pzkMastersonLadyZ"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkMastersonLady"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkDashElite2D"] = NewCarTuningTable["pzkChevalierE6Van"]
		NewCarTuningTable["pzkContinentalPyrenean310"] = NewCarTuningTable["pzkChevalierE6Van"]

		NewCarTuningTable["pzkFranklinPony"] = NewCarTuningTable["pzkFranklinGalloper"]
		NewCarTuningTable["pzkChevalierLaserCUCV"] = NewCarTuningTable["pzkFranklinGalloper"]
		NewCarTuningTable["pzkChevalierLaserModern"] = NewCarTuningTable["pzkFranklinGalloper"]
		NewCarTuningTable["pzkDashIntruder150short"] = NewCarTuningTable["pzkFranklinGalloper"]
		NewCarTuningTable["pzkMastersonInitial"] = NewCarTuningTable["pzkFranklinGalloper"]
		NewCarTuningTable["pzkMastersonSunrise"] = NewCarTuningTable["pzkFranklinGalloper"]
		NewCarTuningTable["pzkMastersonXSR"] = NewCarTuningTable["pzkFranklinGalloper"]
		NewCarTuningTable["pzkContinentalBayer3302D"] = NewCarTuningTable["pzkFranklinGalloper"]
		NewCarTuningTable["pzkContinentalBayer330Sport"] = NewCarTuningTable["pzkFranklinGalloper"]
		NewCarTuningTable["pzkMerciaLang12402D"] = NewCarTuningTable["pzkFranklinGalloper"]
		NewCarTuningTable["pzkDashRancherCustom"] = NewCarTuningTable["pzkFranklinGalloper"]

		NewCarTuningTable["pzkChevalierLaserFire"] = NewCarTuningTable["pzkChevalierLaserRanger"]
		NewCarTuningTable["pzkFranklinGalloperRanger"] = NewCarTuningTable["pzkChevalierLaserRanger"]
		NewCarTuningTable["pzkFranklinGalloperFire"] = NewCarTuningTable["pzkChevalierLaserRanger"]
		NewCarTuningTable["pzkDashRancherRanger"] = NewCarTuningTable["pzkChevalierLaserRanger"]

		NewCarTuningTable["pzkChevalierPickupCrewMedium"] = NewCarTuningTable["pzkFranklin250FPickupCrewLong"]
		NewCarTuningTable["pzkFranklin150FPickupMedium"] = NewCarTuningTable["pzkFranklin250FPickupCrewLong"]
		NewCarTuningTable["pzkDashIntruder250PickupLong"] = NewCarTuningTable["pzkFranklin250FPickupCrewLong"]

		NewCarTuningTable["Vehicles_pzkDashMayor"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkChevalierCeriseSedan"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkDashHellion"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["Vehicles_pzkDashRapier"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkFranklinTriumphTWD"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkDashMayorTaxi"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkDashHellionTaxi"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkFranklinTriumphTWDTaxi"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkFranklinHomelander"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkFranklinTriumph"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkHMMV"] = NewCarTuningTable["pzkContinentalCruiser"]
		NewCarTuningTable["pzkHMMV2"] = NewCarTuningTable["pzkContinentalCruiser"]
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
		NewCarTuningTable["pzkTriumphStationWagonTaxi"] = NewCarTuningTable["pzkFranklin250FWagonLong"]
		NewCarTuningTable["pzkChevalierProvince"] = NewCarTuningTable["pzkFranklin250FWagonLong"]
		NewCarTuningTable["pzkChevalierProvinceLong"] = NewCarTuningTable["pzkFranklin250FWagonLong"]
		NewCarTuningTable["pzkChevalierProvinceLongCUCV"] = NewCarTuningTable["pzkFranklin250FWagonLong"]
		NewCarTuningTable["pzkFranklin350FWagonLong"] = NewCarTuningTable["pzkFranklin250FWagonLong"]
		NewCarTuningTable["pzkHearse"] = NewCarTuningTable["pzkFranklin250FWagonLong"]
		NewCarTuningTable["pzkLimo"] = NewCarTuningTable["pzkFranklin250FWagonLong"]
		NewCarTuningTable["pzkSuvCustom"] = NewCarTuningTable["pzkFranklin250FWagonLong"]
		NewCarTuningTable["pzkFranklinTriumphWagon"] = NewCarTuningTable["pzkFranklin250FWagonLong"]

		NewCarTuningTable["pzkChevalierPickupCrewLong"] = NewCarTuningTable["pzkFranklin350FPickupCrewLong"]

		NewCarTuningTable["pzkFranklin250FPickupWagonLong"] = NewCarTuningTable["pzkDashIntruder250WagonLong"]
		NewCarTuningTable["pzkFranklin150FWagonMedium"] = NewCarTuningTable["pzkDashIntruder250WagonLong"]

		NewCarTuningTable["pzkContinentalBugHerbie"] = NewCarTuningTable["pzkContinentalBug"]

		NewCarTuningTable["pzkDashCheyene"] = NewCarTuningTable["pzkContinentalCruiser"]

		NewCarTuningTable["pzkChevalierLaserOffroader"] = NewCarTuningTable["pzkContinentalCruiser"]

		NewCarTuningTable["pzkCarMuscleCabrio"] = NewCarTuningTable["pzkDashOhio"]
		NewCarTuningTable["pzkContinentalBayer330Cabrio"] = NewCarTuningTable["pzkDashOhio"]
		NewCarTuningTable["pzkMerciaLang4000Cabrio"] = NewCarTuningTable["pzkDashOhio"]
		NewCarTuningTable["pzkChevalierCosetteCabrio"] = NewCarTuningTable["pzkDashOhio"]
		NewCarTuningTable["pzkDashRancherCabrio"] = NewCarTuningTable["pzkDashOhio"]

		NewCarTuningTable["pzkFranklinSwatTruck"] = NewCarTuningTable["pzkFranklinBankTruck"]
		NewCarTuningTable["pzkFranklinTruckRV"] = NewCarTuningTable["pzkFranklinBankTruck"]

		NewCarTuningTable["pzkFranklinTruckBusPrison"] = NewCarTuningTable["pzkFranklinTruckBus"]
		NewCarTuningTable["pzkFranklinTruckBusArmy"] = NewCarTuningTable["pzkFranklinTruckBus"]

		NewCarTuningTable["pzkFranklinTruckFire"] = NewCarTuningTable["pzkFranklinTruckBox"]
		NewCarTuningTable["pzkStepVanUPZ"] = NewCarTuningTable["pzkStepVanMilk"]
		NewCarTuningTable["pzkVanMcCoy"] = NewCarTuningTable["pzkStepVanMilk"]

		NewCarTuningTable["pzkStepVanPizza"] = NewCarTuningTable["pzkStepVanIceCream"]
		NewCarTuningTable["pzkStepVanSwat"] = NewCarTuningTable["pzkStepVanIceCream"]

		NewCarTuningTable["pzkFranklinTruckShort"] = NewCarTuningTable["pzkFranklinTruckBed"]
		NewCarTuningTable["pzkFranklinTruckFlatbed"] = NewCarTuningTable["pzkFranklinTruckBed"]
		NewCarTuningTable["pzkFranklinTruckShort"] = NewCarTuningTable["pzkFranklinTruckBed"]
		NewCarTuningTable["pzkFranklinTruckMil"] = NewCarTuningTable["pzkFranklinTruckBed"]
		NewCarTuningTable["pzkFranklinTruckTow"] = NewCarTuningTable["pzkFranklinTruckBed"]
		NewCarTuningTable["pzkFranklinTruckUtility"] = NewCarTuningTable["pzkFranklinTruckBed"]

		NewCarTuningTable["pzkFranklinTruckDump"] = NewCarTuningTable["pzkFranklinTruckFireTanker"]
		NewCarTuningTable["pzkFranklinTruckGarbage"] = NewCarTuningTable["pzkFranklinTruckFireTanker"]
		NewCarTuningTable["pzkFranklinTruckTankerFossoil"] = NewCarTuningTable["pzkFranklinTruckFireTanker"]
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

		NewCarTuningTable["pzkContinentalPfeiffer901"] = NewCarTuningTable["pzkContinentalBug"]
		NewCarTuningTable["pzkContinentalPfeiffer930"] = NewCarTuningTable["pzkContinentalBug"]

		NewCarTuningTable["pzkContinentalPfeiffer930c"] = NewCarTuningTable["pzkContinentalPfeiffer901c"]

		NewCarTuningTable["pzkTriumphTWDStationWagonGriswold"] = NewCarTuningTable["pzkContinentalCruiser"]

--		NewCarTuningTable["pzkDashPhoenix80SmashedFront"] = NewCarTuningTable["pzkChevalierE6Van"]



		SVUC_setVehiclePickup(NewCarTuningTable, "pzkChevalierE6")
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

		SVUC_setVehiclePickup(NewCarTuningTable, "pzkFranklin350FPickupCrewLong")
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

		SVUC_setVehiclePickup(NewCarTuningTable, "pzkFranklin250FPickupCrewLong")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin250FPickupCrewLong", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin250FPickupCrewLong", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin250FPickupCrewLong", "ATA2ProtectionWindowRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin250FPickupCrewLong", "ATA2ProtectionWindowRearRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin250FPickupCrewLong", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklin250FPickupCrewLong", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkFranklin250FPickupCrewLong", "ATA2Bullbar")
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

		SVUC_setVehiclePickup(NewCarTuningTable, "pzkChevalierRoadrunner")
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
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalBug", "ATA2ProtectionWindowRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkContinentalBug", "ATA2ProtectionWindowRearRight")
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
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinGalloper", "ATA2ProtectionWindowRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinGalloper", "ATA2ProtectionWindowRearRight")
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

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinStallionPolice", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinStallionPolice", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinStallionPolice", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinStallionPolice", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkFranklinStallionPolice", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinStallionPolice", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkFranklinStallionPolice", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinStallionPolice", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkFranklinStallionPolice", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkFranklinStallionPolice", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkFranklinStallionPolice", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkFranklinStallionPolice", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkFranklinStallionPolice", "ATA2Snorkel")

		SVUC_setVehiclePickupDoorsRear(NewCarTuningTable, "pzkFranklinTruckBed")
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

		SVUC_setVehiclePickupDoorsRear(NewCarTuningTable, "pzkFranklinTruckCab")
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

		SVUC_setVehiclePickupTrunkDoor(NewCarTuningTable, "pzkHMMV3")
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

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "Vehicles_VanSeatsTaxi", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "Vehicles_VanSeatsTaxi", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "Vehicles_VanSeatsTaxi", "ATA2ProtectionWindowMiddleLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "Vehicles_VanSeatsTaxi", "ATA2ProtectionWindowMiddleRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "Vehicles_VanSeatsTaxi", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "Vehicles_VanSeatsTaxi", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "Vehicles_VanSeatsTaxi", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "Vehicles_VanSeatsTaxi", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "Vehicles_VanSeatsTaxi", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "Vehicles_VanSeatsTaxi", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "Vehicles_VanSeatsTaxi", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "Vehicles_VanSeatsTaxi", "ATA2ProtectionDoorMiddleLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "Vehicles_VanSeatsTaxi", "ATA2ProtectionDoorMiddleRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "Vehicles_VanSeatsTaxi", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "Vehicles_VanSeatsTaxi", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "Vehicles_VanSeatsTaxi", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "Vehicles_VanSeatsTaxi", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanPolice", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanPolice", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanPolice", "ATA2ProtectionWindowRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanPolice", "ATA2ProtectionWindowRearRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanPolice", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanPolice", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkVanPolice", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanPolice", "ATA2ProtectionDoorsRear")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkVanPolice", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanPolice", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanPolice", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanPolice", "ATA2ProtectionDoorRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkVanPolice", "ATA2ProtectionDoorRearRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkVanPolice", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkVanPolice", "ATA2InteractiveTrunkRoofRack")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkVanPolice", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkVanPolice", "ATA2Snorkel")

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

		SVUC_setVehiclePickup(NewCarTuningTable, "pzkVanBrig")
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

		SVUC_setVehiclePickup(NewCarTuningTable, "pzkTrailerRegularFlatbed")
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

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTrailerHorseBox", "ATA2ProtectionTrunk")
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
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkTractor", "ATA2ProtectionTrunk")
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

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierLaserPolice", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierLaserPolice", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierLaserPolice", "ATA2ProtectionWindowRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierLaserPolice", "ATA2ProtectionWindowRearRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierLaserPolice", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierLaserPolice", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkChevalierLaserPolice", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierLaserPolice", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkChevalierLaserPolice", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierLaserPolice", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierLaserPolice", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkChevalierLaserPolice", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkChevalierLaserPolice", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkChevalierLaserPolice", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkChevalierLaserPolice", "ATA2Snorkel")

		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPolice", "ATA2ProtectionWindowFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPolice", "ATA2ProtectionWindowFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPolice", "ATA2ProtectionWindowRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPolice", "ATA2ProtectionWindowRearRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPolice", "ATA2ProtectionWindshield")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPolice", "ATA2ProtectionWindshieldRear")
		SVUC_setVehicleRecipesBullbars(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPolice", "ATA2Bullbar")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPolice", "ATA2ProtectionTrunk")
		SVUC_setVehicleRecipesArmorHood(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPolice", "ATA2ProtectionHood")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPolice", "ATA2ProtectionDoorFrontLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPolice", "ATA2ProtectionDoorFrontRight")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPolice", "ATA2ProtectionDoorRearLeft")
		SVUC_setVehicleRecipesArmor(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPolice", "ATA2ProtectionDoorRearRight")
		SVUC_setVehicleRecipesWheels(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPolice", "ATA2ProtectionWheels")
		SVUC_setVehicleRecipesMods(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPolice", "ATA2RoofLightFront")
		SVUC_setVehicleRecipesScoops(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPolice", "ATA2AirScoop")
		SVUC_setVehicleRecipesSnorkels(NewCarTuningTable, carRecipe, "pzkChevalierCeriseSedanPolice", "ATA2Snorkel")

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


		ATA2Tuning_AddNewCars(NewCarTuningTable)
	end
end
Events.OnInitGlobalModData.Add(SVU_TuningTable)
