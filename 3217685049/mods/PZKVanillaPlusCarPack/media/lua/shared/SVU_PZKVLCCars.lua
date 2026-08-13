
if getActivatedMods():contains("StandardizedVehicleUpgradesCore") then

require "ATA2TuningTable"

local function copy(obj, seen)
  if type(obj) ~= 'table' then return obj end
  if seen and seen[obj] then return seen[obj] end
  local s = seen or {}
  local res = setmetatable({}, getmetatable(obj))
  s[obj] = res
  for k, v in pairs(obj) do res[copy(k, s)] = copy(v, s) end
  return res
end

local carRecipe = "ATAPZKTuningMag"
local timeLong = 45
local timeLong2 = 30
local timeMid = 20
local timeMid2 = 15
local timeShort = 10
local protectionLightHealthDelta = 5
local protectionSpikedLightHealthDelta = 6
local protectionSpikedHeavyHealthDelta = 2
local protectionBullbarHealthDelta = 3
local protectionWheelsHealthDelta = 2
local protectionLight = "protectionLight"
local protectionHeavy = "protectionHeavy"
local protectionSpikedLight = "protectionSpikedLight"
local protectionSpikedHeavy = "protectionSpikedHeavy"
local protectionMods = "protectionMods"
local protectionAddon = "protectionAddon"

local NewCarTuningTable = {}

-- Entries
-- Specify each vehicle script here.
        NewCarTuningTable["TemplateVLC"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["NewTemplateVLC"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkMinivanStellaris"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkVanBrig"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkVanCamper"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkStepVanMilk"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkStepVanUPZ"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkStepVanSwat"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkVanPolice"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkVanMcCoy"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkChevalierMaroca"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkDashDeluxo"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkCarMuscle"] = {
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
        NewCarTuningTable["pzkFranklinGalloperPolice"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkHearse"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkHMMV"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkHMMV2"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkHMMV3"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkLimo"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkPickupFranklin"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkPickUpTruck93"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkVanBox"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkVanBoxAmbulance"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkVanBoxFiretruck"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkStepVanPizza"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkStepVanIceCream"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinPony"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkPickUpTruckWoodboarded"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinTriumph"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinTriumphTaxi"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinTriumphPolice"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }


        NewCarTuningTable["pzkChevalierCeriseSedan"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkChevalierCeriseSedanPolice"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkChevalierCeriseSedanTaxi"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkDashHellion"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkDashHellionTaxi"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["Vehicles_pzkDashMayor"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkDashMayorPolice"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkDashMayorTaxi"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["Vehicles_pzkDashRapier"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkFranklinTriumphTWD"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkFranklinTriumphTWDPolice"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkFranklinTriumphTWDTaxi"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkCeriseStationWagon"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkDashMayorStationWagon"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkRapierStationWagon"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkTriumphStationWagon"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkTriumphStationWagonTaxi"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["Vehicles_VanSeatsTaxi"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }


        NewCarTuningTable["pzkFranklinGalloperRanger"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkDashRoyal"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkDashRoyalGrand"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkChevalierDownhill"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkDashTornado"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkMastersonLady"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkDashPhoenix"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkDashPhoenixBandit"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkChevalierE6"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkChevalierF6"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinTriumphTWDFire"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkChevalierCeriseSedanFire"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinGalloperFire"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkChevalierRoadrunner"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkMastersonInitial"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkMastersonXSR"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkContinentalSpirit"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkDashNoble"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinStallion"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinStallion2"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinStallionSport"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinStallionPolice"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinIslander"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinHomelander"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkMastersonCrown"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkFranklinBankTruck"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinSwatTruck"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinTruckBed"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinTruckShort"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinTruckCab"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinTruckBox"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinTruckFlatbed"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinTruckMil"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinTruckRV"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinTruckTow"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinTruckUtility"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinTruckBusPrison"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinTruckBus"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinTruckDump"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinTruckFire"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinTruckFireTanker"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinTruckGarbage"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinTruckMilTankerWater"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinTruckPropane"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinTruckPropane2"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinTruckTankerFossoil"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinTruckTankerSeptic"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinTruckTankerWater"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkMinivanC22"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkMinivanChev"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkMinivanConvoy"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkMinivanMPV"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkMinivanStellarisMail"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkMinivanStellarisTaxi"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkMinivanT3"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkMinivanT3C"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkMinivanTask"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkMinivan2"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkMinivanPrev"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkVanMultivan"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkFranklinTruckBusArmy"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }


        NewCarTuningTable["pzkMastersonScout4D"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkChevalierPickupCrewLong"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkChevalierPickupCrewMedium"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkChevalierProvince"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkChevalierProvinceLong"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkChevalierLaserModern"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkChevalierE6Van"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkChevalierF6Van"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkChevalierLaserCUCV"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkChevalierProvinceLongCUCV"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkChevalierLaserOffroader"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkChevalierLaserFire"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkChevalierLaserPolice"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkChevalierLaserRanger"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFtypeTowTruck"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkChevalierTowTruck"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkChevalierTowTruckPolice"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkChevalierTowTruckFire"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkMerciaLangBerg"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkDashCheyene"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkContinentalCruiser"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklin350FWagonLong"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklin350FPickupCrewLong"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklin250FPickupWagonLong"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklin250FPickupCrewLong"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklin250FWagonLong"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklin150van"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklin150FPickupReg"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklin150FWagonMedium"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklin150FPickupMedium"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkDashIntruder250WagonLong"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkDashIntruder250PickupLong"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkDashIntruder150short"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkDashIntruder150RegVan"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkMastersonLadyZ"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkDashRunnerGeneral"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkDashGTA"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinStallionKing"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkDashRunner"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkDashPiranha"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkDashChampion"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkDashPhoenix80"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkChevalierMaroca80"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkContinentalBug"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkContinentalBugHerbie"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

----This one is for YT series
        NewCarTuningTable["pzkDashPhoenix80SmashedFront"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkContinentalBayer330Sport"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }

        NewCarTuningTable["pzkContinentalBayer3304D"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkContinentalBayer3302D"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkContinentalBayer534"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkContinentalBayer732"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkMerciaLang1240"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkMerciaLang12402D"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFranklinTruckSemi"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkContinentalTRK"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFreightlinerFlat2"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkFreightlinerFlat"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkPeterbuiltSleeper"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkPeterbuiltSleeperBandit"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkPeterbuilt"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkMerciaLang4000Cabrio"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkContinentalBayer330Cabrio"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkCarMuscleCabrio"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkDashRancherRanger"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkDashRancherCustom"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkDashRancherCabrio"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkChevalierCosetteCabrio"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkMastersonSunrise"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkMastersonSensation"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkMastersonExpander"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkSuvCustom"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkContinentalHammermanKnight"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkTrailerRegularCourtains"] = {
	addPartsFromVehicleScript = "",
	parts = {}
        }
        NewCarTuningTable["pzkTrailerRegularCourtainsBandit"] = {
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
        NewCarTuningTable["pzkStepVanHotDog"] = {
	addPartsFromVehicleScript = "",
	parts = {}
	}

        NewCarTuningTable["pzkContinentalPfeiffer901"] = {
	addPartsFromVehicleScript = "",
	parts = {}
	}
        NewCarTuningTable["pzkContinentalPfeiffer901c"] = {
	addPartsFromVehicleScript = "",
	parts = {}
	}
        NewCarTuningTable["pzkContinentalPfeiffer930"] = {
	addPartsFromVehicleScript = "",
	parts = {}
	}
        NewCarTuningTable["pzkContinentalPfeiffer930c"] = {
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
        NewCarTuningTable["pzkTractor"] = {
	addPartsFromVehicleScript = "",
	parts = {}
	}
        NewCarTuningTable["pzkTrailerTankSprayer"] = {
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

        NewCarTuningTable["pzkTrailerArmyCover"] = {
	addPartsFromVehicleScript = "",
	parts = {}
	}
        NewCarTuningTable["pzkTrailerBoxPoliceDual"] = {
	addPartsFromVehicleScript = "",
	parts = {}
	}

        NewCarTuningTable["pzkTrailerRegularFTSemi"] = {
	addPartsFromVehicleScript = "",
	parts = {}
	}

        NewCarTuningTable["pzkFranklinTriumphWagon"] = {
	addPartsFromVehicleScript = "",
	parts = {}
	}
        NewCarTuningTable["pzkChevalierCerise93"] = {
	addPartsFromVehicleScript = "",
	parts = {}
	}
        NewCarTuningTable["pzkChevalierCerise93Taxi"] = {
	addPartsFromVehicleScript = "",
	parts = {}
	}
        NewCarTuningTable["pzkChevalierCerise93Police"] = {
	addPartsFromVehicleScript = "",
	parts = {}
	}
        NewCarTuningTable["pzkChevalierCerise93Fire"] = {
	addPartsFromVehicleScript = "",
	parts = {}
	}
        NewCarTuningTable["pzkChevalierCerise93WagonFire"] = {
	addPartsFromVehicleScript = "",
	parts = {}
	}
        NewCarTuningTable["pzkChevalierCerise93Wagon"] = {
	addPartsFromVehicleScript = "",
	parts = {}
	}
        NewCarTuningTable["pzkDashElite2D"] = {
	addPartsFromVehicleScript = "",
	parts = {}
	}
        NewCarTuningTable["pzkMastersonHarmonyWagon"] = {
	addPartsFromVehicleScript = "",
	parts = {}
	}
        NewCarTuningTable["pzkMastersonHarmony"] = {
	addPartsFromVehicleScript = "",
	parts = {}
	}
        NewCarTuningTable["pzkContinentalNord"] = {
	addPartsFromVehicleScript = "",
	parts = {}
	}
        NewCarTuningTable["pzkContinentalNordWagon"] = {
	addPartsFromVehicleScript = "",
	parts = {}
	}
        NewCarTuningTable["pzkContinentalPyrenean310"] = {
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
        NewCarTuningTable["pzkTriumphTWDStationWagonGriswold"] = {
	addPartsFromVehicleScript = "",
	parts = {}
	}


-- A template of sorts to streamline code. DO NOT EDIT!!!
-- NewTemplateVLC

--ATA2LightBar
NewCarTuningTable["NewTemplateVLC"].parts["ATA2RoofLights"] = {
	ATA2RoofLightFront = {
		icon = "media/ui/tuning2/roof_light.png",
		category = protectionMods,
		
		install = {
			area = "TireFrontLeft",
			use = {
				ATARoofLightItem = 4,
				MetalPipe = 2,
				MetalBar=2,
				--ElectricWire = 4,
				BlowTorch = 4,
				Screws=12,

			},
			tools = {
				bodylocation = "Base.WeldingMask",
				primary = "Base.Wrench",
			},
			skills = {
				MetalWelding = 4,
			},
			recipes = {"Intermediate Mechanics",carRecipe},
			time = timeLong, 
		},
		uninstall = {
			area = "TireFrontLeft",
			animation = "ATA_IdleLeverOpenHigh",
			use = {
				BlowTorch=4,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				both = "Base.Crowbar",
			},
			skills = {
				MetalWelding = 3,
			},
			result = "auto",
			time = timeMid,
		}
	}
}


NewCarTuningTable["NewTemplateVLC"].parts["ATA2Bullbar"] = {
	Default = {
		icon = "media/ui/tuning2/van_bullbar_1.png",
		name = "IGUI_ATA2_Bullbar",
		category = protectionMods,
		protectionHealthDelta = protectionBullbarHealthDelta,
		protection = {"HeadlightLeft", "HeadlightRight", "EngineDoor"},
		removeIfBroken = true,
		install = {
			weight = "auto",
			animation = "ATA_PickLock",
			use = {
				MetalPipe = 4,
				MetalBar=2,
				Screws=6,
				BlowTorch = 6,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				primary = "Base.Wrench",
			},
			skills = {
				MetalWelding = 4,
				Mechanics = 2,
			},
			recipes = {"Intermediate Mechanics", carRecipe},
			time = timeMid, 
		},
		uninstall = {
			weight = "auto",
			animation = "ATA_Crowbar_DoorLeft",
			use = {
				BlowTorch=3,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				both = "Base.Crowbar",
			},
			skills = {
				MetalWelding = 3,
			},
			result = "auto",
			time = timeShort,
		}
	},
	Light = {
		icon = "media/ui/tuning2/mustang_bullbar_1.png",
		name = "IGUI_ATA2_Bullbar1",
		category = protectionMods,
		protectionHealthDelta = protectionBullbarHealthDelta,
		protection = {"EngineDoor"},
		removeIfBroken = true,
		install = {
			weight = "auto",
			animation = "ATA_PickLock",
			use = {
				MetalPipe = 4,
				MetalBar=2,
				Screws=6,
				BlowTorch = 6,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				primary = "Base.Wrench",
			},
			skills = {
				MetalWelding = 4,
				Mechanics = 2,
			},
			recipes = {"Intermediate Mechanics", carRecipe},
			time = timeMid, 
		},
		uninstall = {
			weight = "auto",
			animation = "ATA_Crowbar_DoorLeft",
			use = {
				BlowTorch=3,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				both = "Base.Crowbar",
			},
			skills = {
				MetalWelding = 3,
			},
			result = "auto",
			time = timeShort,
		   }
		},
	Heavy = {
		icon = "media/ui/tuning2/van_bullbar_1.png",
		name = "IGUI_ATA2_Bullbar2",
		category = protectionMods,
		--protectionHealthDelta = protectionBullbarHealthDelta,
		protection = {"HeadlightLeft", "HeadlightRight", "EngineDoor"},
		install = {
			weight = "auto",
			animation = "ATA_PickLock",
			use = {
				MetalPipe = 4,
				MetalBar=2,
				Screws=6,
				BlowTorch = 6,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				primary = "Base.Wrench",
			},
			skills = {
				MetalWelding = 4,
				Mechanics = 2,
			},
			recipes = {"Intermediate Mechanics", carRecipe},
			time = timeLong2, 
		    
		},
		uninstall = {
			weight = "auto",
			animation = "ATA_Crowbar_DoorLeft",
			use = {
				BlowTorch=3,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				both = "Base.Crowbar",
			},
			skills = {
				MetalWelding = 3,
			},
			result = "auto",
			time = timeShort,
		}

	      },
	Plow = {
		icon = "media/ui/tuning2/van_bullbar_3.png",
		name = "IGUI_ATA2_Plow1",
		category = protectionMods,
		protectionHealthDelta = protectionBullbarHealthDelta,
		protection = {"EngineDoor"},
		removeIfBroken = true,
		install = {
			weight = "auto",
			animation = "ATA_PickLock",
			use = {
				MetalPipe = 4,
				MetalBar=2,
				Screws=6,
				BlowTorch = 6,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				primary = "Base.Wrench",
			},
			skills = {
				MetalWelding = 4,
				Mechanics = 2,
			},
			recipes = {"Intermediate Mechanics", carRecipe},
			time = timeMid, 
		},
		uninstall = {
			weight = "auto",
			animation = "ATA_Crowbar_DoorLeft",
			use = {
				BlowTorch=3,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				both = "Base.Crowbar",
			},
			skills = {
				MetalWelding = 3,
			},
			result = "auto",
			time = timeShort,
		   }
		}
}


NewCarTuningTable["NewTemplateVLC"].parts["ATA2ProtectionTrunk"] = {
	Light = {
		icon = "media/ui/tuning2/bus_protection_window_side.png",
		category = protectionLight,
		protection = {"TruckBed", "TrunkDoor", "GasTank"},
		protectionHealthDelta = protectionLightHealthDelta,
		removeIfBroken = true,
		install = {
			use = {
				MetalPipe = 4,
				MetalBar = 2,
				Screws = 4,
				BlowTorch = 4,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				primary = "Base.Wrench",
			},
			skills = {
				MetalWelding = 4,
			},
			recipes = {carRecipe},
			requireInstalled = {"TruckBed"},
			time = timeLong2, 
		},
		uninstall = {
			animation = "ATA_IdleLeverOpenMid",
			use = {
				BlowTorch=4,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				both = "Base.Crowbar",
			},
			skills = {
				MetalWelding = 3,
			},
			result = "auto",
			time = timeMid2,
		}
	},
	Spiked = {
		icon = "media/ui/tuning2/bus_protection_window_side.png",
		category = protectionSpikedLight,
		protection = {"TruckBed", "TrunkDoor", "GasTank"},
		protectionHealthDelta = protectionSpikedLightHealthDelta,
		removeIfBroken = true,
		install = {
			use = {
				MetalPipe = 4,
				MetalBar = 2,
				Screws = 4,
				BlowTorch = 4,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				primary = "Base.Wrench",
			},
			skills = {
				MetalWelding = 4,
			},
			recipes = {carRecipe},
			requireInstalled = {"TruckBed"},
			time = timeLong2, 
		},
		uninstall = {
			animation = "ATA_IdleLeverOpenMid",
			use = {
				BlowTorch=4,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				both = "Base.Crowbar",
			},
			skills = {
				MetalWelding = 3,
			},
			result = "auto",
			time = timeMid2,
		}
	},
	Heavy = {
		icon = "media/ui/tuning2/van_hood_protection.png",
		category = protectionHeavy,
		protection = {"TruckBed", "TrunkDoor", "GasTank"},
		removeIfBroken = true,
		install = {
			use = {
				SheetMetal = 4,
				MetalPipe = 4,
				MetalBar = 2,
				Screws = 6,
				BlowTorch = 4,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				primary = "Base.Wrench",
			},
			skills = {
				MetalWelding = 6,
			},
			recipes = {carRecipe},
			requireInstalled = {"TruckBed"},
			time = timeLong2, 
		},
		uninstall = {
			animation = "ATA_IdleLeverOpenMid",
			use = {
				BlowTorch=4,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				both = "Base.Crowbar",
			},
			skills = {
				MetalWelding = 5,
			},
			result = "auto",
			time = timeMid2,
		}
	}
}

NewCarTuningTable["NewTemplateVLC"].parts["ATA2ProtectionHood"] = copy(NewCarTuningTable["NewTemplateVLC"].parts["ATA2ProtectionTrunk"])
NewCarTuningTable["NewTemplateVLC"].parts["ATA2ProtectionHood"].Light.protection = {"EngineDoor"}
NewCarTuningTable["NewTemplateVLC"].parts["ATA2ProtectionHood"].Light.install.requireInstalled = {"EngineDoor"}

NewCarTuningTable["NewTemplateVLC"].parts["ATA2ProtectionHood"].Heavy.protection = {"EngineDoor"}
NewCarTuningTable["NewTemplateVLC"].parts["ATA2ProtectionHood"].Heavy.install.requireInstalled = {"EngineDoor"}

NewCarTuningTable["NewTemplateVLC"].parts["ATA2ProtectionHood"].Spiked.protection = {"EngineDoor"}
NewCarTuningTable["NewTemplateVLC"].parts["ATA2ProtectionHood"].Spiked.install.requireInstalled = {"EngineDoor"}

-- TemplateVLC
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontLeft"] = {
	Light = {
		shader = "vehiclewheel",
		icon = "media/ui/tuning2/protection_window_side.png",
		--secondModel = "StaticPart",
		category = protectionLight,
		-- Not used in SVU:2 for now.
		--protectionTriger = protectionLighthealthTriger,
		-- Just using the value from the local at the top of this file.
		protection = {"WindowFrontLeft"},
		protectionHealthDelta = protectionLightHealthDelta,
		removeIfBroken = true,
		install = {
			area = "TireFrontLeft",
			weight = "auto",
			use = {
				MetalPipe = 2,
				MetalBar=2,
				Screws=4,
				BlowTorch = 5,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				primary = "Base.Wrench",
				secondary = "Base.Screwdriver",
			},
			skills = {
				MetalWelding = 3,
			},
			recipes = {carRecipe},
			requireInstalled = {"WindowFrontLeft"},
			time = timeLong,
		},
		uninstall = {
			area = "TireFrontLeft",
			animation = "ATA_IdleLeverOpenMid",
			use = {
				BlowTorch=4,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				both = "Base.Crowbar",
			},
			skills = {
				MetalWelding = 2,
			},
			result = "auto",
			time = timeLong,
		}
	},
	Heavy = {
		shader = "vehiclewheel",
		icon = "media/ui/tuning2/protection_window_sheet_side.png",
		--secondModel = "StaticPart",
		category = protectionHeavy,
		protection = {"WindowFrontLeft"},
		disableOpenWindowFromSeat = "SeatFrontLeft",
		removeIfBroken = true,
		install = {
			area = "TireFrontLeft",
			weight = "auto",
			use = {
				MetalPipe = 2,
				SheetMetal = 2,
				Screws=4,
				BlowTorch = 5,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				primary = "Base.Wrench",
				secondary = "Base.Screwdriver",
			},
			skills = {
				MetalWelding = 5,
			},
			recipes = {carRecipe},
			requireInstalled = {"WindowFrontLeft"},
			time = timeLong,
		},
		uninstall = {
			area = "TireFrontLeft",
			animation = "ATA_IdleLeverOpenMid",
			use = {
				BlowTorch=4,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				both = "Base.Crowbar",
			},
			skills = {
				MetalWelding = 4,
			},
			result = "auto",
			time = timeLong,
		}
	}
}

NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontLeft"])
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontRight"].Light.protection = {"WindowFrontRight"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontRight"].Light.install.requireInstalled = {"WindowFrontRight"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontRight"].Light.install.area = "TireFrontRight"
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontRight"].Light.uninstall.area = "TireFrontRight"

NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontLeft"])
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearLeft"].Light.protection = {"WindowRearLeft"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearLeft"].Light.install.requireInstalled = {"WindowRearLeft"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearLeft"].Light.install.area = "TireRearLeft"
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearLeft"].Light.uninstall.area = "TireRearLeft"

NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontLeft"])
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearRight"].Light.protection = {"WindowRearRight"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearRight"].Light.install.requireInstalled = {"WindowRearRight"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearRight"].Light.install.area = "TireRearRight"
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearRight"].Light.uninstall.area = "TireRearRight"

NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontRight"].Heavy.protection = {"WindowFrontRight"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontRight"].Heavy.install.requireInstalled = {"WindowFrontRight"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontRight"].Heavy.disableOpenWindowFromSeat = "SeatFrontRight"
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontRight"].Heavy.install.area = "TireFrontRight"
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontRight"].Heavy.uninstall.area = "TireFrontRight"

NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearLeft"].Heavy.protection = {"WindowRearLeft"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearLeft"].Heavy.install.requireInstalled = {"WindowRearLeft"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearLeft"].Heavy.disableOpenWindowFromSeat = "SeatRearLeft"
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearLeft"].Heavy.install.area = "TireRearLeft"
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearLeft"].Heavy.uninstall.area = "TireRearLeft"

NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearRight"].Heavy.protection = {"WindowRearRight"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearRight"].Heavy.install.requireInstalled = {"WindowRearRight"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearRight"].Heavy.disableOpenWindowFromSeat = "SeatRearRight"
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearRight"].Heavy.install.area = "TireRearRight"
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearRight"].Heavy.uninstall.area = "TireRearRight"

NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowMiddleLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontLeft"])
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowMiddleLeft"].Light.protection = {"WindowMiddleLeft"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowMiddleLeft"].Light.install.requireInstalled = {"WindowMiddleLeft"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowMiddleLeft"].Heavy.install.area = "TireFrontLeft"
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowMiddleLeft"].Heavy.uninstall.area = "TireFrontLeft"

NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowMiddleRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontLeft"])
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowMiddleRight"].Light.protection = {"WindowMiddleRight"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowMiddleRight"].Light.install.requireInstalled = {"WindowMiddleRight"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowMiddleRight"].Heavy.install.area = "TireFrontRight"
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowMiddleRight"].Heavy.uninstall.area = "TireFrontRight"

NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowMiddleLeft"].Heavy.protection = {"WindowMiddleLeft"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowMiddleLeft"].Heavy.install.requireInstalled = {"WindowMiddleLeft"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowMiddleLeft"].Heavy.disableOpenWindowFromSeat = {"SeatMiddleLeft"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowMiddleLeft"].Heavy.install.area = "TireFrontLeft"
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowMiddleLeft"].Heavy.uninstall.area = "TireFrontLeft"

NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowMiddleRight"].Heavy.protection = {"WindowMiddleRight"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowMiddleRight"].Heavy.install.requireInstalled = {"WindowMiddleRight"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowMiddleRight"].Heavy.disableOpenWindowFromSeat = {"SeatMiddleRight"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowMiddleRight"].Heavy.install.area = "TireFrontRight"
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowMiddleRight"].Heavy.uninstall.area = "TireFrontRight"

NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshield"] = {
	Light = {
		icon = "media/ui/tuning2/protection_window_windshield.png",
		category = protectionLight,
		protection = {"Windshield"},
		protectionHealthDelta = protectionLightHealthDelta,
		removeIfBroken = true,
		install = {
			area = "TireFrontRight",
			weight = "auto",
			use = {
				MetalPipe = 4,
				MetalBar=4,
				Screws=6,
				BlowTorch = 5,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				primary = "Base.Screwdriver",
			},
			skills = {
				MetalWelding = 3,
			},
			recipes = {carRecipe},
			requireInstalled = {"Windshield"},
			time = timeLong,
		},
		uninstall = {
			area = "TireFrontRight",
			animation = "ATA_IdleLeverOpenMid",
			use = {
				BlowTorch=4,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				both = "Base.Crowbar",
			},
			skills = {
				MetalWelding = 2,
			},
			result = "auto",
			time = timeLong,
		}
	},
	Heavy = {
		icon = "media/ui/tuning2/protection_window_sheet_windshield.png",
		category = protectionHeavy,
		protection = {"Windshield"},
		removeIfBroken = true,
		install = {
			area = "TireFrontRight",
			weight = "auto",
			use = {
				MetalPipe = 4,
				SheetMetal = 2,
				MetalBar=4,
				Screws=6,
				BlowTorch = 5,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				primary = "Base.Screwdriver",
			},
			skills = {
				MetalWelding = 5,
			},
			recipes = {carRecipe},
			requireInstalled = {"Windshield"},
			time = timeLong,
		},
		uninstall = {
			area = "TireFrontRight",
			animation = "ATA_IdleLeverOpenMid",
			use = {
				BlowTorch=4,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				both = "Base.Crowbar",
			},
			skills = {
				MetalWelding = 4,
			},
			result = "auto",
			time = timeLong,
		}
	}
}

NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshieldRear"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshield"])
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshieldRear"].Light.protection = {"WindshieldRear"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshieldRear"].Light.install.requireInstalled = {"WindshieldRear"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshieldRear"].Light.install.area = "TireRearRight"
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshieldRear"].Light.uninstall.area = "TireRearRight"

NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshieldRear"].Heavy.protection = {"WindshieldRear"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshieldRear"].Heavy.install.requireInstalled = {"WindshieldRear"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshieldRear"].Heavy.install.area = "TireRearRight"
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshieldRear"].Heavy.uninstall.area = "TireRearRight"

NewCarTuningTable["TemplateVLC"].parts["ATA2Bullbar"] = {
	Default = {
		icon = "media/ui/tuning2/van_bullbar_1.png",
		name = "IGUI_ATA2_Bullbar",
		category = protectionMods,
		protectionHealthDelta = protectionBullbarHealthDelta,
		protection = {"HeadlightLeft", "HeadlightRight", "EngineDoor"},
		removeIfBroken = true,
		install = {
			weight = "auto",
			animation = "ATA_PickLock",
			use = {
				MetalPipe = 4,
				MetalBar=2,
				Screws=6,
				BlowTorch = 6,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				primary = "Base.Wrench",
			},
			skills = {
				MetalWelding = 4,
				Mechanics = 2,
			},
			recipes = {"Intermediate Mechanics", carRecipe},
			time = timeMid, 
		},
		uninstall = {
			weight = "auto",
			animation = "ATA_Crowbar_DoorLeft",
			use = {
				BlowTorch=3,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				both = "Base.Crowbar",
			},
			skills = {
				MetalWelding = 3,
			},
			result = "auto",
			time = timeShort,

		}

	}
}


NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionTrunk"] = {
	Light = {
		icon = "media/ui/tuning2/bus_protection_window_side.png",
		category = protectionLight,
		protection = {"TruckBed", "TrunkDoor", "GasTank"},
		protectionHealthDelta = protectionLightHealthDelta,
		removeIfBroken = true,
		install = {
			use = {
				MetalPipe = 4,
				MetalBar = 2,
				Screws = 4,
				BlowTorch = 4,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				primary = "Base.Wrench",
			},
			skills = {
				MetalWelding = 4,
			},
			recipes = {carRecipe},
			requireInstalled = {"TruckBed"},
			time = timeLong2, 
		},
		uninstall = {
			animation = "ATA_IdleLeverOpenMid",
			use = {
				BlowTorch=4,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				both = "Base.Crowbar",
			},
			skills = {
				MetalWelding = 3,
			},
			result = "auto",
			time = timeMid2,
		}
	},
	Heavy = {
		icon = "media/ui/tuning2/van_hood_protection.png",
		category = protectionHeavy,
		protection = {"TruckBed", "TrunkDoor", "GasTank"},
		removeIfBroken = true,
		install = {
			use = {
				SheetMetal = 4,
				MetalPipe = 4,
				MetalBar = 2,
				Screws = 6,
				BlowTorch = 4,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				primary = "Base.Wrench",
			},
			skills = {
				MetalWelding = 6,
			},
			recipes = {carRecipe},
			requireInstalled = {"TruckBed"},
			time = timeLong2, 
		},
		uninstall = {
			animation = "ATA_IdleLeverOpenMid",
			use = {
				BlowTorch=4,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				both = "Base.Crowbar",
			},
			skills = {
				MetalWelding = 5,
			},
			result = "auto",
			time = timeMid2,
		}
	}
}

NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorsRear"] = {
	Light = {
		icon = "media/ui/tuning2/bus_protection_window_side.png",
		category = protectionLight,
		protection = {"TruckBed", "DoorRear"},
		protectionHealthDelta = protectionLightHealthDelta,
		removeIfBroken = true,
		install = {
			use = {
				MetalPipe = 4,
				MetalBar = 2,
				Screws = 4,
				BlowTorch = 4,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				primary = "Base.Wrench",
			},
			skills = {
				MetalWelding = 4,
			},
			recipes = {carRecipe},
			requireInstalled = {"TruckBed"},
			time = timeLong2, 
		},
		uninstall = {
			animation = "ATA_IdleLeverOpenMid",
			use = {
				BlowTorch=4,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				both = "Base.Crowbar",
			},
			skills = {
				MetalWelding = 3,
			},
			result = "auto",
			time = timeMid2,
		}
	},
	Heavy = {
		icon = "media/ui/tuning2/van_hood_protection.png",
		category = protectionHeavy,
		protection = {"TruckBed", "DoorRear"},
		removeIfBroken = true,
		install = {
			use = {
				SheetMetal = 4,
				MetalPipe = 4,
				MetalBar = 2,
				Screws = 6,
				BlowTorch = 4,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				primary = "Base.Wrench",
			},
			skills = {
				MetalWelding = 6,
			},
			recipes = {carRecipe},
			requireInstalled = {"TruckBed"},
			time = timeLong2, 
		},
		uninstall = {
			animation = "ATA_IdleLeverOpenMid",
			use = {
				BlowTorch=4,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				both = "Base.Crowbar",
			},
			skills = {
				MetalWelding = 5,
			},
			result = "auto",
			time = timeMid2,
		}
	}
}

NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionHood"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionTrunk"])
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionHood"].Light.protection = {"EngineDoor"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionHood"].Light.install.requireInstalled = {"EngineDoor"}

NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionHood"].Heavy.protection = {"EngineDoor"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionHood"].Heavy.install.requireInstalled = {"EngineDoor"}

NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontLeft"] = {
	Light = {
		icon = "media/ui/tuning2/bus_protection_window_side.png",
		secondModel = "StaticPart",
		category = protectionLight,
		protection = {"DoorFrontLeft"},
		protectionHealthDelta = protectionLightHealthDelta,
		removeIfBroken = true,
		install = {
			area = "TireFrontLeft",
			weight = "auto",
			use = {
				MetalPipe = 4,
				MetalBar=4,
				Screws=4,
				BlowTorch = 8,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				primary = "Base.Wrench",
			},
			skills = {
				MetalWelding = 4,
			},
			recipes = {carRecipe},
			requireInstalled = {"DoorFrontLeft"},
			time = timeLong,
		},
		uninstall = {
			area = "TireFrontLeft",
			animation = "ATA_IdleLeverOpenMid",
			use = {
				BlowTorch=8,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				both = "Base.Crowbar",
			},
			skills = {
				MetalWelding = 3,
			},
			result = "auto",
			time = timeMid,
		}
	},
	Heavy = {
		icon = "media/ui/tuning2/van_hood_protection.png",
		secondModel = "StaticPart",
		category = protectionHeavy,
		protection = {"DoorFrontLeft"},
		removeIfBroken = true,
		install = {
			area = "TireFrontLeft",
			weight = "auto",
			use = {
				MetalPipe = 4,
				MetalBar=4,
				SheetMetal=6,
				Screws=10,
				BlowTorch = 8,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				primary = "Base.Wrench",
			},
			skills = {
				MetalWelding = 6,
			},
			recipes = {carRecipe},
			requireInstalled = {"DoorFrontLeft"},
			time = timeLong,
		},
		uninstall = {
			area = "TireFrontLeft",
			animation = "ATA_IdleLeverOpenMid",
			use = {
				BlowTorch=8,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				both = "Base.Crowbar",
			},
			skills = {
				MetalWelding = 5,
			},
			result = "auto",
			time = timeMid,
		}
	}
}

NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontLeft"])
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontRight"].Light.protection = {"DoorFrontRight"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontRight"].Light.install.requireInstalled = {"DoorFrontRight"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontRight"].Light.install.area = "TireFrontRight"
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontRight"].Light.uninstall.area = "TireFrontRight"

NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontLeft"])
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearLeft"].Light.protection = {"DoorRearLeft"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearLeft"].Light.install.requireInstalled = {"DoorRearLeft"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearLeft"].Light.install.area = "TireRearLeft"
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearLeft"].Light.uninstall.area = "TireRearLeft"

NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontLeft"])
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearRight"].Light.protection = {"DoorRearRight"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearRight"].Light.install.requireInstalled = {"DoorRearRight"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearRight"].Light.install.area = "TireRearRight"
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearRight"].Light.uninstall.area = "TireRearRight"

NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontRight"].Heavy.protection = {"DoorFrontRight"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontRight"].Heavy.install.requireInstalled = {"DoorFrontRight"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontRight"].Heavy.install.area = "TireFrontRight"
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontRight"].Heavy.uninstall.area = "TireFrontRight"

NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearLeft"].Heavy.protection = {"DoorRearLeft"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearLeft"].Heavy.install.requireInstalled = {"DoorRearLeft"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearLeft"].Heavy.install.area = "TireRearLeft"
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearLeft"].Heavy.uninstall.area = "TireRearLeft"

NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearRight"].Heavy.protection = {"DoorRearRight"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearRight"].Heavy.install.requireInstalled = {"DoorRearRight"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearRight"].Heavy.install.area = "TireRearRight"
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearRight"].Heavy.uninstall.area = "TireRearRight"

NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorMiddleLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontLeft"])
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorMiddleLeft"].Light.protection = {"DoorMiddleLeft"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorMiddleLeft"].Light.install.requireInstalled = {"DoorMiddleLeft"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorMiddleLeft"].Light.install.area = "TireFrontRight"
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorMiddleLeft"].Light.uninstall.area = "TireFrontRight"

NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorMiddleRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontLeft"])
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorMiddleRight"].Light.protection = {"DoorMiddleRight"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorMiddleRight"].Light.install.requireInstalled = {"DoorMiddleRight"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorMiddleRight"].Light.install.area = "TireFrontRight"
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorMiddleRight"].Light.uninstall.area = "TireFrontRight"

NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorMiddleLeft"].Heavy.protection = {"DoorMiddleLeft"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorMiddleLeft"].Heavy.install.requireInstalled = {"DoorMiddleLeft"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorMiddleLeft"].Heavy.install.area = "TireFrontRight"
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorMiddleLeft"].Heavy.uninstall.area = "TireFrontRight"

NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorMiddleRight"].Heavy.protection = {"DoorMiddleRight"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorMiddleRight"].Heavy.install.requireInstalled = {"DoorMiddleRight"}
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorMiddleRight"].Heavy.install.area = "TireFrontRight"
NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorMiddleRight"].Heavy.uninstall.area = "TireFrontRight"

NewCarTuningTable["TemplateVLC"].parts["ATA2InteractiveTrunkRoofRack"] = {
	Default = {
		icon = "media/ui/tuning2/roof_rack_2.png",
		category = protectionMods,
		--interactiveTrunk = {
			--filling = {"ATA_VanDeRumba_roof_bag1", "ATA_VanDeRumba_roof_bag2", "ATA_VanDeRumba_roof_bag3", "ATA_VanDeRumba_roof_bag4", "ATA_VanDeRumba_roof_bag5", "ATA_VanDeRumba_roof_bag6"},
			--items = {
				--{
					--itemTypes = {"MetalDrum"},
					--modelNameByCount = {"ATA_VanDeRumba_roof_barrel"},
				--},
				--{
					--itemTypes = {"PetrolCan", "EmptyPetrolCan"},
					--modelNameByCount = {"ATA_VanDeRumba_roof_gascan0", "ATA_VanDeRumba_roof_gascan1", "ATA_VanDeRumba_roof_gascan2", "ATA_VanDeRumba_roof_gascan3", "ATA_VanDeRumba_roof_gascan4", "ATA_VanDeRumba_roof_gascan5", "ATA_VanDeRumba_roof_gascan6", "ATA_VanDeRumba_roof_gascan7", "ATA_VanDeRumba_roof_gascan8", },
				--},
			--}
		--},
		containerCapacity = 50,
		install = {
			area = "TruckBed",
			use = {
				MetalPipe = 6,
				SheetMetal = 6,
				MetalBar=4,
				BlowTorch = 10,
				Screws=12,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				primary = "Base.Wrench",
			},
			skills = {
				MetalWelding = 4,
			},
			recipes = {carRecipe},
			time = timeLong, 
		},
		uninstall = {
			area = "TruckBed",
			animation = "ATA_IdleLeverOpenHigh",
			use = {
				BlowTorch=8,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				both = "Base.Crowbar",
			},
			skills = {
				MetalWelding = 3,
			},
			result = "auto",
			time = timeMid,
		}
	}
}

NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"] = {
	ATAProtection = {
		icon = "media/ui/tuning2/wheel_chain.png",
		category = protectionMods, 
		protectionModel = true, 
		protection = {"TireFrontLeft", "TireFrontRight", "TireRearLeft", "TireRearRight"}, 
		protectionHealthDelta = protectionWheelsHealthDelta,
		removeIfBroken = true,
		install = {
			area = "TireFrontLeft",
			sound = "ATA2InstallWheelChain",
			use = { 
				ATAProtectionWheelsChain = 1,
				BlowTorch = 4,
			},
			tools = { 
				bodylocation = "Base.WeldingMask", 
				primary = "Base.Wrench",
			},
			skills = {
				Mechanics = 2,
				MetalWelding = 3,
			},
			recipes = {carRecipe},
			requireInstalled = {"TireFrontLeft", "TireFrontRight", "TireRearLeft", "TireRearRight"},
			time = timeLong, 
		},
		uninstall = {
			area = "TireFrontLeft",
			sound = "ATA2InstallWheelChain",
			use = {
				BlowTorch=4,
			},
			tools = {
				bodylocation = "Base.WeldingMask",
				both = "Base.Crowbar",
			},
			skills = {
				MetalWelding = 2,
			},
			result = {
				UnusableMetal=2,
			},
			time = timeMid,
		}
	}
}

-- Here you actually specify what goes on the car, based on what's in the armor script file.
-- Again, if it's not in the armor script file it won't work!



if getActivatedMods():contains("StandardizedVehicleUpgradesV") then


-- pzkMinivanStellaris  3 door 4 window with rack & bullbar
NewCarTuningTable["pzkMinivanStellaris"].parts["ATA2ProtectionWindowFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontLeft"])
NewCarTuningTable["pzkMinivanStellaris"].parts["ATA2ProtectionWindowFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontRight"])
NewCarTuningTable["pzkMinivanStellaris"].parts["ATA2ProtectionWindowRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearLeft"])
NewCarTuningTable["pzkMinivanStellaris"].parts["ATA2ProtectionWindowRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearRight"])
NewCarTuningTable["pzkMinivanStellaris"].parts["ATA2ProtectionWindshield"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshield"])
NewCarTuningTable["pzkMinivanStellaris"].parts["ATA2ProtectionWindshieldRear"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshieldRear"])
NewCarTuningTable["pzkMinivanStellaris"].parts["ATA2Bullbar"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2Bullbar"])
NewCarTuningTable["pzkMinivanStellaris"].parts["ATA2ProtectionTrunk"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionTrunk"])
NewCarTuningTable["pzkMinivanStellaris"].parts["ATA2ProtectionHood"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionHood"])
NewCarTuningTable["pzkMinivanStellaris"].parts["ATA2ProtectionDoorFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontLeft"])
NewCarTuningTable["pzkMinivanStellaris"].parts["ATA2ProtectionDoorFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontRight"])
--NewCarTuningTable["pzkMinivanStellaris"].parts["ATA2ProtectionDoorRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearLeft"])
NewCarTuningTable["pzkMinivanStellaris"].parts["ATA2ProtectionDoorRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearRight"])
NewCarTuningTable["pzkMinivanStellaris"].parts["ATA2InteractiveTrunkRoofRack"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2InteractiveTrunkRoofRack"])
NewCarTuningTable["pzkMinivanStellaris"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 60
NewCarTuningTable["pzkMinivanStellaris"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])

-- pzkVan 2 door 2 window with rack & bullbar
NewCarTuningTable["pzkStepVanUPZ"].parts["ATA2ProtectionWindowFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontLeft"])
NewCarTuningTable["pzkStepVanUPZ"].parts["ATA2ProtectionWindowFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontRight"])
--NewCarTuningTable["pzkStepVanUPZ"].parts["ATA2ProtectionWindowRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearLeft"])
--NewCarTuningTable["pzkStepVanUPZ"].parts["ATA2ProtectionWindowRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearRight"])
NewCarTuningTable["pzkStepVanUPZ"].parts["ATA2ProtectionWindshield"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshield"])
NewCarTuningTable["pzkStepVanUPZ"].parts["ATA2ProtectionWindshieldRear"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshieldRear"])
NewCarTuningTable["pzkStepVanUPZ"].parts["ATA2Bullbar"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2Bullbar"])
NewCarTuningTable["pzkStepVanUPZ"].parts["ATA2ProtectionTrunk"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionTrunk"])
NewCarTuningTable["pzkStepVanUPZ"].parts["ATA2ProtectionHood"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionHood"])
NewCarTuningTable["pzkStepVanUPZ"].parts["ATA2ProtectionDoorFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontLeft"])
NewCarTuningTable["pzkStepVanUPZ"].parts["ATA2ProtectionDoorFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontRight"])
--NewCarTuningTable["pzkStepVanUPZ"].parts["ATA2ProtectionDoorRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearLeft"])
--NewCarTuningTable["pzkStepVanUPZ"].parts["ATA2ProtectionDoorRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearRight"])
NewCarTuningTable["pzkStepVanUPZ"].parts["ATA2InteractiveTrunkRoofRack"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2InteractiveTrunkRoofRack"])
NewCarTuningTable["pzkStepVanUPZ"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 60
NewCarTuningTable["pzkStepVanUPZ"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])

--  2 door 2 window with rack & bullbar
NewCarTuningTable["pzkChevalierMaroca"].parts["ATA2ProtectionWindowFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontLeft"])
NewCarTuningTable["pzkChevalierMaroca"].parts["ATA2ProtectionWindowFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontRight"])
--NewCarTuningTable["pzkChevalierMaroca"].parts["ATA2ProtectionWindowRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearLeft"])
--NewCarTuningTable["pzkChevalierMaroca"].parts["ATA2ProtectionWindowRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearRight"])
NewCarTuningTable["pzkChevalierMaroca"].parts["ATA2ProtectionWindshield"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshield"])
NewCarTuningTable["pzkChevalierMaroca"].parts["ATA2ProtectionWindshieldRear"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshieldRear"])
NewCarTuningTable["pzkChevalierMaroca"].parts["ATA2Bullbar"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2Bullbar"])
NewCarTuningTable["pzkChevalierMaroca"].parts["ATA2ProtectionTrunk"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionTrunk"])
NewCarTuningTable["pzkChevalierMaroca"].parts["ATA2ProtectionHood"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionHood"])
NewCarTuningTable["pzkChevalierMaroca"].parts["ATA2ProtectionDoorFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontLeft"])
NewCarTuningTable["pzkChevalierMaroca"].parts["ATA2ProtectionDoorFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontRight"])
--NewCarTuningTable["pzkChevalierMaroca"].parts["ATA2ProtectionDoorRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearLeft"])
--NewCarTuningTable["pzkChevalierMaroca"].parts["ATA2ProtectionDoorRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearRight"])
NewCarTuningTable["pzkChevalierMaroca"].parts["ATA2InteractiveTrunkRoofRack"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2InteractiveTrunkRoofRack"])
NewCarTuningTable["pzkChevalierMaroca"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])

NewCarTuningTable["pzkDashDeluxo"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkCarMuscle"] = NewCarTuningTable["pzkChevalierMaroca"]


-- jeep
--NewCarTuningTable["pzkDashOhio"].parts["ATA2ProtectionWindowFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontLeft"])
--NewCarTuningTable["pzkDashOhio"].parts["ATA2ProtectionWindowFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontRight"])
--NewCarTuningTable["pzkDashOhio"].parts["ATA2ProtectionWindowRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearLeft"])
--NewCarTuningTable["pzkDashOhio"].parts["ATA2ProtectionWindowRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearRight"])
NewCarTuningTable["pzkDashOhio"].parts["ATA2ProtectionWindshield"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshield"])
--NewCarTuningTable["pzkDashOhio"].parts["ATA2ProtectionWindshieldRear"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshieldRear"])
NewCarTuningTable["pzkDashOhio"].parts["ATA2Bullbar"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2Bullbar"])
NewCarTuningTable["pzkDashOhio"].parts["ATA2ProtectionTrunk"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionTrunk"])
NewCarTuningTable["pzkDashOhio"].parts["ATA2ProtectionHood"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionHood"])
NewCarTuningTable["pzkDashOhio"].parts["ATA2ProtectionDoorFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontLeft"])
NewCarTuningTable["pzkDashOhio"].parts["ATA2ProtectionDoorFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontRight"])
--NewCarTuningTable["pzkDashOhio"].parts["ATA2ProtectionDoorRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearLeft"])
--NewCarTuningTable["pzkDashOhio"].parts["ATA2ProtectionDoorRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearRight"])
--NewCarTuningTable["pzkDashOhio"].parts["ATA2InteractiveTrunkRoofRack"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2InteractiveTrunkRoofRack"])
NewCarTuningTable["pzkDashOhio"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])


-- pzkVanCamper 4 door 4 window with rack & bullbar
NewCarTuningTable["pzkVanCamper"].parts["ATA2ProtectionWindowFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontLeft"])
NewCarTuningTable["pzkVanCamper"].parts["ATA2ProtectionWindowFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontRight"])
NewCarTuningTable["pzkVanCamper"].parts["ATA2ProtectionWindowRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearLeft"])
NewCarTuningTable["pzkVanCamper"].parts["ATA2ProtectionWindowRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearRight"])
NewCarTuningTable["pzkVanCamper"].parts["ATA2ProtectionWindshield"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshield"])
NewCarTuningTable["pzkVanCamper"].parts["ATA2ProtectionWindshieldRear"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshieldRear"])
NewCarTuningTable["pzkVanCamper"].parts["ATA2Bullbar"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2Bullbar"])
NewCarTuningTable["pzkVanCamper"].parts["ATA2ProtectionTrunk"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionTrunk"])
NewCarTuningTable["pzkVanCamper"].parts["ATA2ProtectionHood"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionHood"])
NewCarTuningTable["pzkVanCamper"].parts["ATA2ProtectionDoorFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontLeft"])
NewCarTuningTable["pzkVanCamper"].parts["ATA2ProtectionDoorFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontRight"])
--NewCarTuningTable["pzkVanCamper"].parts["ATA2ProtectionDoorRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearLeft"])
NewCarTuningTable["pzkVanCamper"].parts["ATA2ProtectionDoorRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearRight"])
NewCarTuningTable["pzkVanCamper"].parts["ATA2InteractiveTrunkRoofRack"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2InteractiveTrunkRoofRack"])
NewCarTuningTable["pzkVanCamper"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 90
NewCarTuningTable["pzkVanCamper"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])

-- pzkLimo 4 door 4 window with rack & bullbar
NewCarTuningTable["pzkLimo"].parts["ATA2ProtectionWindowFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontLeft"])
NewCarTuningTable["pzkLimo"].parts["ATA2ProtectionWindowFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontRight"])
NewCarTuningTable["pzkLimo"].parts["ATA2ProtectionWindowRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearLeft"])
NewCarTuningTable["pzkLimo"].parts["ATA2ProtectionWindowRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearRight"])
NewCarTuningTable["pzkLimo"].parts["ATA2ProtectionWindshield"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshield"])
NewCarTuningTable["pzkLimo"].parts["ATA2ProtectionWindshieldRear"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshieldRear"])
NewCarTuningTable["pzkLimo"].parts["ATA2Bullbar"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2Bullbar"])
NewCarTuningTable["pzkLimo"].parts["ATA2ProtectionTrunk"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionTrunk"])
NewCarTuningTable["pzkLimo"].parts["ATA2ProtectionHood"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionHood"])
NewCarTuningTable["pzkLimo"].parts["ATA2ProtectionDoorFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontLeft"])
NewCarTuningTable["pzkLimo"].parts["ATA2ProtectionDoorFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontRight"])
NewCarTuningTable["pzkLimo"].parts["ATA2ProtectionDoorRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearLeft"])
NewCarTuningTable["pzkLimo"].parts["ATA2ProtectionDoorRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearRight"])
NewCarTuningTable["pzkLimo"].parts["ATA2InteractiveTrunkRoofRack"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2InteractiveTrunkRoofRack"])
NewCarTuningTable["pzkLimo"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 90
NewCarTuningTable["pzkLimo"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])

--  2 door 2 window  bullbar no rack
NewCarTuningTable["pzkPickupFranklin"].parts["ATA2ProtectionWindowFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontLeft"])
NewCarTuningTable["pzkPickupFranklin"].parts["ATA2ProtectionWindowFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontRight"])
--NewCarTuningTable["pzkPickupFranklin"].parts["ATA2ProtectionWindowRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearLeft"])
--NewCarTuningTable["pzkPickupFranklin"].parts["ATA2ProtectionWindowRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearRight"])
NewCarTuningTable["pzkPickupFranklin"].parts["ATA2ProtectionWindshield"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshield"])
NewCarTuningTable["pzkPickupFranklin"].parts["ATA2ProtectionWindshieldRear"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshieldRear"])
NewCarTuningTable["pzkPickupFranklin"].parts["ATA2Bullbar"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2Bullbar"])
NewCarTuningTable["pzkPickupFranklin"].parts["ATA2ProtectionTrunk"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionTrunk"])
NewCarTuningTable["pzkPickupFranklin"].parts["ATA2ProtectionHood"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionHood"])
NewCarTuningTable["pzkPickupFranklin"].parts["ATA2ProtectionDoorFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontLeft"])
NewCarTuningTable["pzkPickupFranklin"].parts["ATA2ProtectionDoorFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontRight"])
--NewCarTuningTable["pzkPickupFranklin"].parts["ATA2ProtectionDoorRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearLeft"])
--NewCarTuningTable["pzkPickupFranklin"].parts["ATA2ProtectionDoorRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearRight"])
--NewCarTuningTable["pzkPickupFranklin"].parts["ATA2InteractiveTrunkRoofRack"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2InteractiveTrunkRoofRack"])
NewCarTuningTable["pzkPickupFranklin"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])


NewCarTuningTable["pzkStepVanSwat"] = NewCarTuningTable["pzkStepVanUPZ"]
NewCarTuningTable["pzkStepVanMilk"] = NewCarTuningTable["pzkStepVanUPZ"]
NewCarTuningTable["pzkVanMcCoy"] = NewCarTuningTable["pzkStepVanUPZ"]
NewCarTuningTable["pzkVanBox"] = NewCarTuningTable["pzkStepVanUPZ"]
NewCarTuningTable["pzkVanBoxAmbulance"] = NewCarTuningTable["pzkStepVanUPZ"]
NewCarTuningTable["pzkVanBoxFiretruck"] = NewCarTuningTable["pzkStepVanUPZ"]



NewCarTuningTable["pzkHearse"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkHMMV"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkHMMV2"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkHMMV3"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkVanBrig"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkPickUpTruck93"] = NewCarTuningTable["pzkPickupFranklin"]

NewCarTuningTable["pzkFranklinGalloper"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkFranklinGalloperPolice"] = NewCarTuningTable["pzkChevalierMaroca"]



NewCarTuningTable["pzkVanPolice"].parts["ATA2ProtectionWindowFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontLeft"])
NewCarTuningTable["pzkVanPolice"].parts["ATA2ProtectionWindowFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontRight"])
NewCarTuningTable["pzkVanPolice"].parts["ATA2ProtectionWindowMiddleLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowMiddleLeft"])
NewCarTuningTable["pzkVanPolice"].parts["ATA2ProtectionWindowMiddleRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowMiddleRight"])
NewCarTuningTable["pzkVanPolice"].parts["ATA2ProtectionWindshield"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshield"])
NewCarTuningTable["pzkVanPolice"].parts["ATA2ProtectionWindshieldRear"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshieldRear"])
NewCarTuningTable["pzkVanPolice"].parts["ATA2Bullbar"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2Bullbar"])
NewCarTuningTable["pzkVanPolice"].parts["ATA2ProtectionTrunk"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionTrunk"])
NewCarTuningTable["pzkVanPolice"].parts["ATA2ProtectionHood"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionHood"])
NewCarTuningTable["pzkVanPolice"].parts["ATA2ProtectionDoorFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontLeft"])
NewCarTuningTable["pzkVanPolice"].parts["ATA2ProtectionDoorFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontRight"])
NewCarTuningTable["pzkVanPolice"].parts["ATA2ProtectionDoorMiddleLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorMiddleLeft"])
NewCarTuningTable["pzkVanPolice"].parts["ATA2ProtectionDoorMiddleRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorMiddleRight"])
NewCarTuningTable["pzkVanPolice"].parts["ATA2InteractiveTrunkRoofRack"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2InteractiveTrunkRoofRack"])
NewCarTuningTable["pzkVanPolice"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])

NewCarTuningTable["pzkPickUpTruckWoodboarded"] = NewCarTuningTable["pzkPickupFranklin"]   
NewCarTuningTable["pzkFranklinPony"] = NewCarTuningTable["pzkCarMuscle"]   
NewCarTuningTable["pzkFranklinTriumph"] = NewCarTuningTable["pzkLimo"]


-- pzkStepVanPizza 2 door 2 window with no rack & bullbar
NewCarTuningTable["pzkStepVanPizza"].parts["ATA2ProtectionWindowFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontLeft"])
NewCarTuningTable["pzkStepVanPizza"].parts["ATA2ProtectionWindowFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontRight"])
--NewCarTuningTable["pzkStepVanPizza"].parts["ATA2ProtectionWindowRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearLeft"])
--NewCarTuningTable["pzkStepVanPizza"].parts["ATA2ProtectionWindowRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearRight"])
NewCarTuningTable["pzkStepVanPizza"].parts["ATA2ProtectionWindshield"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshield"])
NewCarTuningTable["pzkStepVanPizza"].parts["ATA2ProtectionWindshieldRear"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshieldRear"])
NewCarTuningTable["pzkStepVanPizza"].parts["ATA2Bullbar"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2Bullbar"])
NewCarTuningTable["pzkStepVanPizza"].parts["ATA2ProtectionTrunk"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionTrunk"])
NewCarTuningTable["pzkStepVanPizza"].parts["ATA2ProtectionHood"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionHood"])
NewCarTuningTable["pzkStepVanPizza"].parts["ATA2ProtectionDoorFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontLeft"])
NewCarTuningTable["pzkStepVanPizza"].parts["ATA2ProtectionDoorFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontRight"])
--NewCarTuningTable["pzkStepVanPizza"].parts["ATA2ProtectionDoorRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearLeft"])
--NewCarTuningTable["pzkStepVanPizza"].parts["ATA2ProtectionDoorRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearRight"])
--NewCarTuningTable["pzkStepVanPizza"].parts["ATA2InteractiveTrunkRoofRack"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2InteractiveTrunkRoofRack"])
--NewCarTuningTable["pzkStepVanPizza"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 60
NewCarTuningTable["pzkStepVanPizza"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])

NewCarTuningTable["pzkStepVanIceCream"] = NewCarTuningTable["pzkStepVanPizza"]

-- pzkFranklinTriumphTaxi 4 door 4 window with  no rack & bullbar
NewCarTuningTable["pzkFranklinTriumphTaxi"].parts["ATA2ProtectionWindowFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontLeft"])
NewCarTuningTable["pzkFranklinTriumphTaxi"].parts["ATA2ProtectionWindowFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontRight"])
NewCarTuningTable["pzkFranklinTriumphTaxi"].parts["ATA2ProtectionWindowRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearLeft"])
NewCarTuningTable["pzkFranklinTriumphTaxi"].parts["ATA2ProtectionWindowRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearRight"])
NewCarTuningTable["pzkFranklinTriumphTaxi"].parts["ATA2ProtectionWindshield"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshield"])
NewCarTuningTable["pzkFranklinTriumphTaxi"].parts["ATA2ProtectionWindshieldRear"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshieldRear"])
NewCarTuningTable["pzkFranklinTriumphTaxi"].parts["ATA2Bullbar"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2Bullbar"])
NewCarTuningTable["pzkFranklinTriumphTaxi"].parts["ATA2ProtectionTrunk"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionTrunk"])
NewCarTuningTable["pzkFranklinTriumphTaxi"].parts["ATA2ProtectionHood"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionHood"])
NewCarTuningTable["pzkFranklinTriumphTaxi"].parts["ATA2ProtectionDoorFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontLeft"])
NewCarTuningTable["pzkFranklinTriumphTaxi"].parts["ATA2ProtectionDoorFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontRight"])
NewCarTuningTable["pzkFranklinTriumphTaxi"].parts["ATA2ProtectionDoorRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearLeft"])
NewCarTuningTable["pzkFranklinTriumphTaxi"].parts["ATA2ProtectionDoorRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearRight"])
--NewCarTuningTable["pzkFranklinTriumphTaxi"].parts["ATA2InteractiveTrunkRoofRack"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2InteractiveTrunkRoofRack"])
--NewCarTuningTable["pzkFranklinTriumphTaxi"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 90
NewCarTuningTable["pzkFranklinTriumphTaxi"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])


NewCarTuningTable["pzkFranklinTriumphPolice"] = NewCarTuningTable["pzkFranklinTriumphTaxi"]


NewCarTuningTable["pzkChevalierCeriseSedan"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkChevalierCeriseSedanPolice"] = NewCarTuningTable["pzkFranklinTriumph"]
NewCarTuningTable["pzkChevalierCeriseSedanTaxi"] = NewCarTuningTable["pzkFranklinTriumph"]
NewCarTuningTable["pzkDashHellion"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkDashHellionTaxi"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["Vehicles_pzkDashMayor"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkDashMayorPolice"] = NewCarTuningTable["pzkFranklinTriumph"]
NewCarTuningTable["pzkDashMayorTaxi"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["Vehicles_pzkDashRapier"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkFranklinTriumphTWD"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkFranklinTriumphTWDPolice"] = NewCarTuningTable["pzkFranklinTriumph"]
NewCarTuningTable["pzkFranklinTriumphTWDTaxi"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkCeriseStationWagon"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkDashMayorStationWagon"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkRapierStationWagon"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkTriumphStationWagon"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkTriumphStationWagonTaxi"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["Vehicles_VanSeatsTaxi"] = NewCarTuningTable["pzkVanPolice"]

NewCarTuningTable["pzkFranklinGalloperRanger"] = NewCarTuningTable["pzkChevalierMaroca"]


NewCarTuningTable["pzkDashRoyal"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkDashRoyalGrand"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkChevalierDownhill"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkDashTornado"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkMastersonLady"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkDashPhoenix"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkDashPhoenixBandit"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkChevalierE6"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkChevalierF6"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkFranklinTriumphTWDFire"] = NewCarTuningTable["pzkFranklinTriumphTaxi"]
NewCarTuningTable["pzkChevalierCeriseSedanFire"] = NewCarTuningTable["pzkFranklinTriumphTaxi"]
NewCarTuningTable["pzkFranklinGalloperFire"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkChevalierRoadrunner"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkMastersonInitial"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkMastersonXSR"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkContinentalSpirit"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkDashNoble"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkFranklinStallion"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkFranklinStallion2"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkFranklinStallionSport"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkFranklinStallionPolice"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkFranklinIslander"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkFranklinHomelander"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkMastersonCrown"] = NewCarTuningTable["pzkLimo"]



-- pzkFranklinBankTruck 3 door (on right) 3 window + bullbar + double roofrack
NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2ProtectionWindowFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontLeft"])
NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2ProtectionWindowFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontRight"])
--NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2ProtectionWindowRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearLeft"])
NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2ProtectionWindowRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearRight"])
NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2ProtectionWindshield"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshield"])
NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2ProtectionWindshieldRear"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshieldRear"])
NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2Bullbar"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2Bullbar"])
NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2ProtectionTrunk"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionTrunk"])
NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2ProtectionHood"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionHood"])
NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2ProtectionDoorFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontLeft"])
NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2ProtectionDoorFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontRight"])
--NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2ProtectionDoorRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearLeft"])
NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2ProtectionDoorRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearRight"])
NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2InteractiveTrunkRoofRack"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2InteractiveTrunkRoofRack"])
NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 130
NewCarTuningTable["pzkFranklinBankTruck"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])


NewCarTuningTable["pzkFranklinSwatTruck"] = NewCarTuningTable["pzkFranklinBankTruck"]
NewCarTuningTable["pzkFranklinTruckBed"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkFranklinTruckShort"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkFranklinTruckCab"] = NewCarTuningTable["pzkLimo"]
	


-- SCHOOLBUS 1 door (on right) 3 window + bullbar + double roofrack
NewCarTuningTable["pzkFranklinTruckBus"].parts["ATA2ProtectionWindowFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontLeft"])
NewCarTuningTable["pzkFranklinTruckBus"].parts["ATA2ProtectionWindowFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontRight"])
NewCarTuningTable["pzkFranklinTruckBus"].parts["ATA2ProtectionWindowRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearLeft"])
NewCarTuningTable["pzkFranklinTruckBus"].parts["ATA2ProtectionWindowRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearRight"])
NewCarTuningTable["pzkFranklinTruckBus"].parts["ATA2ProtectionWindshield"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshield"])
NewCarTuningTable["pzkFranklinTruckBus"].parts["ATA2ProtectionWindshieldRear"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshieldRear"])
NewCarTuningTable["pzkFranklinTruckBus"].parts["ATA2Bullbar"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2Bullbar"])
NewCarTuningTable["pzkFranklinTruckBus"].parts["ATA2ProtectionTrunk"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionTrunk"])
NewCarTuningTable["pzkFranklinTruckBus"].parts["ATA2ProtectionHood"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionHood"])
--NewCarTuningTable["pzkFranklinTruckBus"].parts["ATA2ProtectionDoorFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontLeft"])
NewCarTuningTable["pzkFranklinTruckBus"].parts["ATA2ProtectionDoorFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontRight"])
--NewCarTuningTable["pzkFranklinTruckBus"].parts["ATA2ProtectionDoorRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearLeft"])
--NewCarTuningTable["pzkFranklinTruckBus"].parts["ATA2ProtectionDoorRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearRight"])
NewCarTuningTable["pzkFranklinTruckBus"].parts["ATA2InteractiveTrunkRoofRack"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2InteractiveTrunkRoofRack"])
NewCarTuningTable["pzkFranklinTruckBus"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 130
NewCarTuningTable["pzkFranklinTruckBus"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])
		

NewCarTuningTable["pzkFranklinTruckBusPrison"] = NewCarTuningTable["pzkFranklinTruckBus"]
NewCarTuningTable["pzkFranklinTruckBox"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkFranklinTruckDump"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkFranklinTruckFire"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkFranklinTruckFireTanker"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkFranklinTruckFlatbed"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkFranklinTruckGarbage"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkFranklinTruckPropane"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkFranklinTruckPropane2"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkFranklinTruckRV"] = NewCarTuningTable["pzkFranklinBankTruck"]		
NewCarTuningTable["pzkFranklinTruckTankerFossoil"] = NewCarTuningTable["pzkPickupFranklin"]		
NewCarTuningTable["pzkFranklinTruckTankerSeptic"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkFranklinTruckTankerWater"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkFranklinTruckTow"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkFranklinTruckUtility"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkFranklinTruckMilTankerWater"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkFranklinTruckMil"] = NewCarTuningTable["pzkPickupFranklin"]

NewCarTuningTable["pzkMinivanC22"] = NewCarTuningTable["pzkMinivanStellaris"]
NewCarTuningTable["pzkMinivanChev"] = NewCarTuningTable["pzkMinivanStellaris"]
NewCarTuningTable["pzkMinivanConvoy"] = NewCarTuningTable["pzkMinivanStellaris"]
NewCarTuningTable["pzkMinivanMPV"] = NewCarTuningTable["pzkMinivanStellaris"]
NewCarTuningTable["pzkMinivanStellarisMail"] = NewCarTuningTable["pzkStepVanUPZ"]
NewCarTuningTable["pzkMinivanStellarisTaxi"] = NewCarTuningTable["pzkMinivanStellaris"]
NewCarTuningTable["pzkMinivanT3"] = NewCarTuningTable["pzkMinivanStellaris"]
NewCarTuningTable["pzkMinivanT3C"] = NewCarTuningTable["pzkMinivanStellaris"]
NewCarTuningTable["pzkMinivanTask"] = NewCarTuningTable["pzkMinivanStellaris"]
NewCarTuningTable["pzkMinivan2"] = NewCarTuningTable["pzkMinivanStellaris"]
NewCarTuningTable["pzkMinivanPrev"] = NewCarTuningTable["pzkMinivanStellaris"]

NewCarTuningTable["pzkFranklinTruckBusArmy"] = NewCarTuningTable["pzkFranklinTruckBus"]
NewCarTuningTable["pzkVanMultivan"] = NewCarTuningTable["pzkLimo"]


-- PICKUPS  2 door (on right) 4 window + bullbar no roofrack
NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2ProtectionWindowFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontLeft"])
NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2ProtectionWindowFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontRight"])
NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2ProtectionWindowRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearLeft"])
NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2ProtectionWindowRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearRight"])
NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2ProtectionWindshield"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshield"])
NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2ProtectionWindshieldRear"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshieldRear"])
NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2Bullbar"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2Bullbar"])
NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2ProtectionTrunk"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionTrunk"])
NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2ProtectionHood"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionHood"])
NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2ProtectionDoorFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontLeft"])
NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2ProtectionDoorFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontRight"])
--NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2ProtectionDoorRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearLeft"])
--NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2ProtectionDoorRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearRight"])
--NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2InteractiveTrunkRoofRack"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2InteractiveTrunkRoofRack"])
--NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 130
NewCarTuningTable["pzkFranklin250FPickupCrewLong"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])

-- WAGONS 2 door  4 window + bullbar roofrack
NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2ProtectionWindowFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontLeft"])
NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2ProtectionWindowFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontRight"])
NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2ProtectionWindowRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearLeft"])
NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2ProtectionWindowRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearRight"])
NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2ProtectionWindshield"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshield"])
NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2ProtectionWindshieldRear"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshieldRear"])
NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2Bullbar"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2Bullbar"])
NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2ProtectionTrunk"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionTrunk"])
NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2ProtectionHood"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionHood"])
NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2ProtectionDoorFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontLeft"])
NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2ProtectionDoorFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontRight"])
--NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2ProtectionDoorRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearLeft"])
--NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2ProtectionDoorRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearRight"])
NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2InteractiveTrunkRoofRack"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2InteractiveTrunkRoofRack"])
NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 130
NewCarTuningTable["pzkFranklin250FWagonLong"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])

-- PICKUPS  4 door  4 window + bullbar no roofrack
NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2ProtectionWindowFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontLeft"])
NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2ProtectionWindowFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontRight"])
NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2ProtectionWindowRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearLeft"])
NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2ProtectionWindowRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearRight"])
NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2ProtectionWindshield"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshield"])
NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2ProtectionWindshieldRear"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshieldRear"])
NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2Bullbar"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2Bullbar"])
NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2ProtectionTrunk"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionTrunk"])
NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2ProtectionHood"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionHood"])
NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2ProtectionDoorFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontLeft"])
NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2ProtectionDoorFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontRight"])
NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2ProtectionDoorRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearLeft"])
NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2ProtectionDoorRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearRight"])
--NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2InteractiveTrunkRoofRack"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2InteractiveTrunkRoofRack"])
--NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 130
NewCarTuningTable["pzkFranklin350FPickupCrewLong"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])


NewCarTuningTable["pzkMastersonScout4D"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkChevalierPickupCrewLong"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkChevalierPickupCrewMedium"] = NewCarTuningTable["pzkFranklin250FPickupCrewLong"]
NewCarTuningTable["pzkChevalierProvince"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkChevalierProvinceLong"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkChevalierLaserModern"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkChevalierE6Van"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkChevalierF6Van"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkChevalierLaserCUCV"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkChevalierProvinceLongCUCV"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkChevalierLaserOffroader"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkChevalierLaserFire"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkChevalierLaserPolice"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkChevalierLaserRanger"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkFtypeTowTruck"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkChevalierTowTruck"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkChevalierTowTruckPolice"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkChevalierTowTruckFire"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkMerciaLangBerg"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkDashCheyene"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkContinentalCruiser"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkFranklin350FWagonLong"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkFranklin250FPickupWagonLong"] = NewCarTuningTable["pzkFranklin250FWagonLong"]
NewCarTuningTable["pzkFranklin150van"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkFranklin150FPickupReg"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkFranklin150FWagonMedium"] = NewCarTuningTable["pzkFranklin250FWagonLong"]
NewCarTuningTable["pzkFranklin150FPickupMedium"] = NewCarTuningTable["pzkFranklin250FPickupCrewLong"]
NewCarTuningTable["pzkDashIntruder250WagonLong"] = NewCarTuningTable["pzkFranklin250FWagonLong"]
NewCarTuningTable["pzkDashIntruder250PickupLong"] = NewCarTuningTable["pzkFranklin250FPickupCrewLong"]
NewCarTuningTable["pzkDashIntruder150short"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkDashIntruder150RegVan"] = NewCarTuningTable["pzkChevalierMaroca"]

NewCarTuningTable["pzkMastersonLadyZ"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkDashRunnerGeneral"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkDashGTA"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkFranklinStallionKing"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkDashRunner"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkDashPiranha"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkDashChampion"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkDashPhoenix80"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkChevalierMaroca80"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkDashPhoenix80SmashedFront"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkContinentalBug"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkContinentalBugHerbie"] = NewCarTuningTable["pzkChevalierMaroca"]

--  2 door 4 window with rack & bullbar
NewCarTuningTable["pzkContinentalBayer330Sport"].parts["ATA2ProtectionWindowFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontLeft"])
NewCarTuningTable["pzkContinentalBayer330Sport"].parts["ATA2ProtectionWindowFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontRight"])
NewCarTuningTable["pzkContinentalBayer330Sport"].parts["ATA2ProtectionWindowRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearLeft"])
NewCarTuningTable["pzkContinentalBayer330Sport"].parts["ATA2ProtectionWindowRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearRight"])
NewCarTuningTable["pzkContinentalBayer330Sport"].parts["ATA2ProtectionWindshield"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshield"])
NewCarTuningTable["pzkContinentalBayer330Sport"].parts["ATA2ProtectionWindshieldRear"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshieldRear"])
NewCarTuningTable["pzkContinentalBayer330Sport"].parts["ATA2Bullbar"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2Bullbar"])
NewCarTuningTable["pzkContinentalBayer330Sport"].parts["ATA2ProtectionTrunk"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionTrunk"])
NewCarTuningTable["pzkContinentalBayer330Sport"].parts["ATA2ProtectionHood"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionHood"])
NewCarTuningTable["pzkContinentalBayer330Sport"].parts["ATA2ProtectionDoorFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontLeft"])
NewCarTuningTable["pzkContinentalBayer330Sport"].parts["ATA2ProtectionDoorFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontRight"])
--NewCarTuningTable["pzkContinentalBayer330Sport"].parts["ATA2ProtectionDoorRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearLeft"])
--NewCarTuningTable["pzkContinentalBayer330Sport"].parts["ATA2ProtectionDoorRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearRight"])
NewCarTuningTable["pzkContinentalBayer330Sport"].parts["ATA2InteractiveTrunkRoofRack"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2InteractiveTrunkRoofRack"])
NewCarTuningTable["pzkContinentalBayer330Sport"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])

--  4 door 4 window with rack & bullbar - no rearshield (PUMPER)
NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2ProtectionWindowFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontLeft"])
NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2ProtectionWindowFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontRight"])
NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2ProtectionWindowRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearLeft"])
NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2ProtectionWindowRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearRight"])
NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2ProtectionWindshield"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshield"])
--NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2ProtectionWindshieldRear"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshieldRear"])
NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2Bullbar"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2Bullbar"])
NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2ProtectionTrunk"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionTrunk"])
NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2ProtectionHood"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionHood"])
NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2ProtectionDoorFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontLeft"])
NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2ProtectionDoorFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontRight"])
NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2ProtectionDoorRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearLeft"])
NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2ProtectionDoorRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearRight"])
NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2InteractiveTrunkRoofRack"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2InteractiveTrunkRoofRack"])
NewCarTuningTable["pzkFireTruckFlatPumper"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])

--  4 door 4 window with  bullbar - no rearshield, no rack (LADDER)
NewCarTuningTable["pzkFireTruckFlatLadder"].parts["ATA2ProtectionWindowFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontLeft"])
NewCarTuningTable["pzkFireTruckFlatLadder"].parts["ATA2ProtectionWindowFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontRight"])
NewCarTuningTable["pzkFireTruckFlatLadder"].parts["ATA2ProtectionWindowRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearLeft"])
NewCarTuningTable["pzkFireTruckFlatLadder"].parts["ATA2ProtectionWindowRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearRight"])
NewCarTuningTable["pzkFireTruckFlatLadder"].parts["ATA2ProtectionWindshield"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshield"])
--NewCarTuningTable["pzkFireTruckFlatLadder"].parts["ATA2ProtectionWindshieldRear"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshieldRear"])
NewCarTuningTable["pzkFireTruckFlatLadder"].parts["ATA2Bullbar"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2Bullbar"])
NewCarTuningTable["pzkFireTruckFlatLadder"].parts["ATA2ProtectionTrunk"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionTrunk"])
NewCarTuningTable["pzkFireTruckFlatLadder"].parts["ATA2ProtectionHood"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionHood"])
NewCarTuningTable["pzkFireTruckFlatLadder"].parts["ATA2ProtectionDoorFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontLeft"])
NewCarTuningTable["pzkFireTruckFlatLadder"].parts["ATA2ProtectionDoorFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontRight"])
NewCarTuningTable["pzkFireTruckFlatLadder"].parts["ATA2ProtectionDoorRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearLeft"])
NewCarTuningTable["pzkFireTruckFlatLadder"].parts["ATA2ProtectionDoorRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearRight"])
--NewCarTuningTable["pzkFireTruckFlatLadder"].parts["ATA2InteractiveTrunkRoofRack"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2InteractiveTrunkRoofRack"])
NewCarTuningTable["pzkFireTruckFlatLadder"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])

NewCarTuningTable["pzkContinentalBayer3304D"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkContinentalBayer3302D"] = NewCarTuningTable["pzkContinentalBayer330Sport"]
NewCarTuningTable["pzkContinentalBayer534"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkContinentalBayer732"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkMerciaLang1240"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkMerciaLang12402D"] = NewCarTuningTable["pzkContinentalBayer330Sport"]
NewCarTuningTable["pzkFranklinTruckSemi"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkContinentalTRK"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkFreightlinerFlat2"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkFreightlinerFlat"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkPeterbuiltSleeper"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkPeterbuiltSleeperBandit"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkPeterbuilt"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkMerciaLang4000Cabrio"] = NewCarTuningTable["pzkDashOhio"]
NewCarTuningTable["pzkContinentalBayer330Cabrio"] = NewCarTuningTable["pzkDashOhio"]
NewCarTuningTable["pzkCarMuscleCabrio"] = NewCarTuningTable["pzkDashOhio"]
NewCarTuningTable["pzkDashRancherRanger"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkDashRancherCustom"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkDashRancherCabrio"] = NewCarTuningTable["pzkDashOhio"]
NewCarTuningTable["pzkChevalierCosetteCabrio"] = NewCarTuningTable["pzkDashOhio"]
NewCarTuningTable["pzkMastersonSunrise"] = NewCarTuningTable["pzkContinentalBayer330Sport"]
NewCarTuningTable["pzkMastersonSensation"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkMastersonExpander"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkSuvCustom"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkContinentalHammermanKnight"] = NewCarTuningTable["pzkLimo"]

NewCarTuningTable["pzkStepVanHotDog"] = NewCarTuningTable["pzkStepVanPizza"]
NewCarTuningTable["pzkFireTruckFlatSemi"] = NewCarTuningTable["pzkFireTruckFlatPumper"]
NewCarTuningTable["pzkTractor"] = NewCarTuningTable["pzkContinentalBayer330Sport"]
NewCarTuningTable["pzkContinentalPfeiffer901"] = NewCarTuningTable["pzkContinentalBayer330Sport"]
NewCarTuningTable["pzkContinentalPfeiffer901c"] = NewCarTuningTable["pzkDashOhio"]
NewCarTuningTable["pzkContinentalPfeiffer930"] = NewCarTuningTable["pzkContinentalBayer330Sport"]
NewCarTuningTable["pzkContinentalPfeiffer930c"] = NewCarTuningTable["pzkDashOhio"]


NewCarTuningTable["pzkFranklinTriumphWagon"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkChevalierCerise93"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkChevalierCerise93Taxi"] = NewCarTuningTable["pzkFranklinTriumphTaxi"]
NewCarTuningTable["pzkChevalierCerise93Police"] = NewCarTuningTable["pzkFranklinTriumphTaxi"]
NewCarTuningTable["pzkChevalierCerise93Fire"] = NewCarTuningTable["pzkFranklinTriumphTaxi"]
NewCarTuningTable["pzkChevalierCerise93WagonFire"] = NewCarTuningTable["pzkFranklinTriumph"]
NewCarTuningTable["pzkChevalierCerise93Wagon"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkDashElite2D"] = NewCarTuningTable["pzkContinentalBayer330Sport"]
NewCarTuningTable["pzkMastersonHarmonyWagon"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkMastersonHarmony"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkContinentalNord"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkContinentalNordWagon"] = NewCarTuningTable["pzkLimo"]
NewCarTuningTable["pzkContinentalPyrenean310"] = NewCarTuningTable["pzkChevalierMaroca"]
NewCarTuningTable["pzkDashHEMTT6x6semi"] = NewCarTuningTable["pzkPickupFranklin"]
NewCarTuningTable["pzkTriumphTWDStationWagonGriswold"] = NewCarTuningTable["pzkLimo"]

--  BUS Coach
NewCarTuningTable["pzkTransitBus"].parts["ATA2ProtectionWindowFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontLeft"])
NewCarTuningTable["pzkTransitBus"].parts["ATA2ProtectionWindowFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowFrontRight"])
NewCarTuningTable["pzkTransitBus"].parts["ATA2ProtectionWindowRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearLeft"])
NewCarTuningTable["pzkTransitBus"].parts["ATA2ProtectionWindowRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindowRearRight"])
NewCarTuningTable["pzkTransitBus"].parts["ATA2ProtectionWindshield"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshield"])
NewCarTuningTable["pzkTransitBus"].parts["ATA2ProtectionWindshieldRear"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWindshieldRear"])
NewCarTuningTable["pzkTransitBus"].parts["ATA2Bullbar"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2Bullbar"])
NewCarTuningTable["pzkTransitBus"].parts["ATA2ProtectionTrunk"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionTrunk"])
NewCarTuningTable["pzkTransitBus"].parts["ATA2ProtectionHood"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionHood"])
--NewCarTuningTable["pzkTransitBus"].parts["ATA2ProtectionDoorFrontLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontLeft"])
NewCarTuningTable["pzkTransitBus"].parts["ATA2ProtectionDoorFrontRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorFrontRight"])
--NewCarTuningTable["pzkTransitBus"].parts["ATA2ProtectionDoorRearLeft"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearLeft"])
NewCarTuningTable["pzkTransitBus"].parts["ATA2ProtectionDoorRearRight"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorRearRight"])
NewCarTuningTable["pzkTransitBus"].parts["ATA2InteractiveTrunkRoofRack"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2InteractiveTrunkRoofRack"])
NewCarTuningTable["pzkTransitBus"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 300
NewCarTuningTable["pzkTransitBus"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])



--  trailer
NewCarTuningTable["pzkTrailerRegularCourtains"].parts["ATA2ProtectionTrunk"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorsRear"])
NewCarTuningTable["pzkTrailerRegularCourtains"].parts["ATA2InteractiveTrunkRoofRack"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2InteractiveTrunkRoofRack"])
NewCarTuningTable["pzkTrailerRegularCourtains"].parts["ATA2InteractiveTrunkRoofRack"].Default.containerCapacity = 500
NewCarTuningTable["pzkTrailerRegularCourtains"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])

NewCarTuningTable["pzkTrailerRegularCourtainsBandit"] = NewCarTuningTable["pzkTrailerRegularCourtains"]

--  trailer
NewCarTuningTable["pzkTrailerRegularFlatbed"].parts["ATA2ProtectionTrunk"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionDoorsRear"])
NewCarTuningTable["pzkTrailerRegularFlatbed"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])

--  trailer
NewCarTuningTable["pzkTrailerRegularFuelTanker"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])


--NewCarTuningTable["pzkTrailerArmyCover"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])
--NewCarTuningTable["pzkTrailerBoxPoliceDual"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])
--NewCarTuningTable["pzkTrailerTankSprayer"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])
--NewCarTuningTable["pzkTrailerCamping"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])
--NewCarTuningTable["pzkTrailerTankSmall"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])
--NewCarTuningTable["pzkTrailerBoxDual"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])
--NewCarTuningTable["pzkTrailerTankMedium"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])
--NewCarTuningTable["pzkDualTrailerCover"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])
--NewCarTuningTable["pzkDualTrailer"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])

NewCarTuningTable["pzkTrailerRegularFTSemi"].parts["ATA2ProtectionWheels"] = copy(NewCarTuningTable["TemplateVLC"].parts["ATA2ProtectionWheels"])





end
ATA2Tuning_AddNewCars(NewCarTuningTable)
end




