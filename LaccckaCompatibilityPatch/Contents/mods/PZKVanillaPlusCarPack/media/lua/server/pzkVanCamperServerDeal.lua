if getActivatedMods():contains("RV_Interior_Vanilla") then
require ('TrailerHomeServerDeal')

local patchVehicles = {
	"Base.pzkVanCamper",
	"Base.pzkMinivanT3C",
	"Base.pzkTrailerCamping",
}

for i=1,#patchVehicles do
	RVInterior.shareInterior(patchVehicles[i], "Base.TrailerHome")

end
end

