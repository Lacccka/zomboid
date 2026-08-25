require "Items/ProceduralDistributions"
require "ExtractionMode/Config"

local cureType = ExtractionMode.Config.INFECTION_CURE_TYPE
local rareWeight = 0.05

local function addRareCure(distributionName)
    local distribution = ProceduralDistributions
        and ProceduralDistributions.list
        and ProceduralDistributions.list[distributionName]
    local items = distribution and distribution.items
    if items == nil then return end

    for index = 1, #items, 2 do
        if items[index] == cureType then return end
    end
    items[#items + 1] = cureType
    items[#items + 1] = rareWeight
end

addRareCure("MedicalClinicDrugs")
addRareCure("MedicalCabinet")
addRareCure("StoreShelfMedical")
addRareCure("FridgeMedical")

return {}
