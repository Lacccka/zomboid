local MuggyCoreUtilities = {}

local VALID_MUGGY_ANIMAL_TYPES = {
    ["coffee"] = true,
    ["tea"] = true,
    ["babycup"] = true
}

--- Validates and identifies whether or not a given animal entity is a securitron
--- @param animalEntity IsoAnimal The animal entity to validate
--- @return boolean currentAnimalKttr True if the animal is actually a securitron, false otherwise
function MuggyCoreUtilities.currentAnimalMuggy(animalEntity)
    if not animalEntity then
        return false
    end

    local animalType = animalEntity:getAnimalType()
    return VALID_MUGGY_ANIMAL_TYPES[animalType] == true
end

return MuggyCoreUtilities