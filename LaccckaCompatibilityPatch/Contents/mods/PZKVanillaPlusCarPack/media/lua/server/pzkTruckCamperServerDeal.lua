if getActivatedMods():contains("RV_Interior_Vanilla") then
require ('TrailerHomeServerDeal')

local patchVehicles = {
	"Base.pzkFranklinTruckRV",
}

for i=1,#patchVehicles do
	RVInterior.shareInterior(patchVehicles[i], "Base.TrailerHome")

end
end





