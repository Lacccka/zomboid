--ITEM INJECTOR BY PZK FORGE - PETER HAMMERMAN


local Rusteeze = {
	"Base.pzkRustSolvent",
}



-- GROUP OF CONTAINERS



local CarStores = {
	"MechanicShelfOutfit", "MechanicSpecial", "GasStorageMechanics", "CarSupplyTools", "CarSupplyGasCans", "ToolCabinetMechanics"
}



-- MAIN FUNCTION
local function addItemsToContainers(items, containers, chance)
    for _, item in ipairs(items) do
        for _, container in ipairs(containers) do
            table.insert(ProceduralDistributions.list[container].items, item)
            table.insert(ProceduralDistributions.list[container].items, chance)
        end
    end
end

-- INJECTING TABLES:

-- Normal items


addItemsToContainers(Rusteeze, CarStores, 0.20)


