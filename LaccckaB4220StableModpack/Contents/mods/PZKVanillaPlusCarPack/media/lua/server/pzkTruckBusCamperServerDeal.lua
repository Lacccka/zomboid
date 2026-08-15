if getActivatedMods():contains("RV_Interior_Vanilla") then
require ('TrailerHomeServerDeal')

local patchVehicles = {
	"Base.pzkFranklinTruckBusPrison",
	"Base.pzkFranklinTruckBus",
	"Base.pzkTransitBus",
}

for i=1,#patchVehicles do
	RVInterior.shareInterior(patchVehicles[i], "Base.schoolbus")

end
end

