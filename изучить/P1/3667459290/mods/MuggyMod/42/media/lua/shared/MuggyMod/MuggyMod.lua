

local MuggyCoreUtilities = require("MuggyMod/Muggy_CoreUtils")
local Muggy_VariableBridge = require("MuggyMod/Muggy_VariableBridge")

--- Initializes muggy-specific properties on an animal entity
--- @param animalEntity IsoAnimal
local function initializeMuggyProperties(animalEntity)
    animalEntity:setVariable("currentAnimalMuggy", true)
    animalEntity:setIsInvincible(true)
    local swiftnessGene = animalEntity:getUsedGene("swiftness")
    local swiftnessValue = swiftnessGene:getCurrentValue()
    animalEntity:setVariable("geneswiftness", swiftnessValue)

    Muggy_VariableBridge.initializeMuggyVariables(animalEntity)
end

local AnimalProcessingQueue = {}

---@type IsoAnimal[]
AnimalProcessingQueue.pendingAnimals = table.newarray()

--- Adds an animal to the processing queue
--- @param animalEntity IsoAnimal
function AnimalProcessingQueue.enqueue(animalEntity)
    table.insert(AnimalProcessingQueue.pendingAnimals, animalEntity)
end

--- Processes all queued animals and initializes securitron-specific properties
function AnimalProcessingQueue.processQueue()
    if #AnimalProcessingQueue.pendingAnimals == 0 then
        return
    end

    local queueSize = #AnimalProcessingQueue.pendingAnimals

    for index = queueSize, 1, -1 do
        local currentAnimal = AnimalProcessingQueue.pendingAnimals[index]

        if currentAnimal and MuggyCoreUtilities.currentAnimalMuggy(currentAnimal) then
            initializeMuggyProperties(currentAnimal)
        end

        AnimalProcessingQueue.pendingAnimals[index] = nil
    end
end

Events.OnCreateLivingCharacter.Add(function(characterEntity, characterDescriptor)
    if characterEntity:isAnimal() then
        ---@cast characterEntity IsoAnimal
        AnimalProcessingQueue.enqueue(characterEntity)
    end
end)

Events.OnTick.Add(function()
    AnimalProcessingQueue.processQueue()
end)