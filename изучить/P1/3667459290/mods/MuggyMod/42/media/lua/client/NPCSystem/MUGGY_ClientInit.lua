local MUGGY_ClientInit = {}

local MuggyCoreUtilities = require("MuggyMod/Muggy_CoreUtils")

local clientSystems = {
    ZoneResponseClient = nil,
    InteractionCoordinator = nil,
    DialogueInputHandler = nil,
    InteractionScanner = nil,
    PassiveSoundInit = nil
}

local initialized = false

local function isValidMuggyNPC(obj)
    if not instanceof(obj, "IsoAnimal") then
        return false
    end

    if not obj:isExistInTheWorld() then
        return false
    end

    if obj:isDead() then
        return false
    end

    if not MuggyCoreUtilities.currentAnimalMuggy(obj) then
        return false
    end

    local isMuggy = obj:getVariable("currentAnimalMuggy")
    if not isMuggy then
        return false
    end

    return true
end

function MUGGY_ClientInit.initialize()
    if initialized then
        print("[MUGGY_ClientInit] Already initialized, skipping")
        return
    end
    initialized = true
    print("[MUGGY_ClientInit] Initializing client systems...")

    clientSystems.ZoneResponseClient = require("NPCSystem/MUGGY_ZoneResponseClient")
    clientSystems.InteractionCoordinator = require("DialogueFramework/Interaction/NPC_InteractionCoordinator")
    clientSystems.DialogueInputHandler = require("DialogueFramework/UI/NPC_DialogueInputHandler")
    clientSystems.InteractionScanner = require("DialogueFramework/Interaction/NPC_InteractionScanner")
    clientSystems.PassiveSoundInit = require("NPCSystem/MUGGY_PassiveSoundInit")

    clientSystems.InteractionScanner.setValidationCallback(isValidMuggyNPC)

    local NPC_DialogueEngine = require("DialogueFramework/Dialogue/NPC_DialogueEngine")
    local NPC_DialogueDefinitions = require("NPCSystem/Dialogue/NPC_DialogueDefinitions")
    NPC_DialogueEngine.registerDefinitionProvider("muggy", NPC_DialogueDefinitions)

    for name, system in pairs(clientSystems) do
        if system and system.initialize then
            system.initialize()
            print("[MUGGY_ClientInit] Initialized:", name)
        end
    end

    print("[MUGGY_ClientInit] Client initialization complete")
end

function MUGGY_ClientInit.isInitialized()
    return initialized
end

return MUGGY_ClientInit
