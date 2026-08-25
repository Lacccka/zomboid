local MUGGY_PassiveSoundInit = {}

local NPC_IdleSoundSystem = require("DialogueFramework/Sound/NPC_IdleSoundSystem")
local NPC_GreetingSystem = require("DialogueFramework/Sound/NPC_GreetingSystem")
local NPC_ProximityDetector = require("DialogueFramework/Proximity/NPC_ProximityDetector")
local NPC_PassiveSoundManager = require("DialogueFramework/Sound/NPC_PassiveSoundManager")
local MUGGY_SoundDefinitions = require("NPCSystem/Sound/MUGGY_SoundDefinitions")
local MuggyCoreUtilities = require("MuggyMod/Muggy_CoreUtils")

local registeredMuggys = {}
local scanInterval = 5.0
local lastScanTime = 0

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

local function scanForMuggy()
    local currentTime = getTimestampMs() / 1000.0

    if currentTime - lastScanTime < scanInterval then
        return
    end

    lastScanTime = currentTime

    local player = getPlayer()
    if not player then
        return
    end

    local playerX = math.floor(player:getX())
    local playerY = math.floor(player:getY())
    local playerZ = player:getZ()

    local cell = getCell()
    if not cell then
        return
    end

    local scanRadius = 50

    for xOffset = -scanRadius, scanRadius do
        for yOffset = -scanRadius, scanRadius do
            local checkX = playerX + xOffset
            local checkY = playerY + yOffset

            local square = cell:getGridSquare(checkX, checkY, playerZ)

            if square then
                local objects = square:getMovingObjects()

                if objects and objects:size() > 0 then
                    for i = 0, objects:size() - 1 do
                        local obj = objects:get(i)

                        if isValidMuggyNPC(obj) then
                            local npcID = tostring(obj)

                            if not registeredMuggys[npcID] then
                                local NPC_IdleDefinitions = require("NPCSystem/Idle/NPC_IdleDefinitions")
                                local idleKeys = NPC_IdleDefinitions.getAllIdleKeys("muggy")

                                NPC_IdleSoundSystem.registerNPC(obj, "muggy", idleKeys)
                                NPC_GreetingSystem.registerNPC(obj, MUGGY_SoundDefinitions.greetingSound)
                                NPC_ProximityDetector.registerNPC(obj)

                                registeredMuggys[npcID] = true
                            end
                        end
                    end
                end
            end
        end
    end
end

function MUGGY_PassiveSoundInit.initialize()
    NPC_PassiveSoundManager.initialize()

    Events.OnTick.Add(scanForMuggy)
end

return MUGGY_PassiveSoundInit
