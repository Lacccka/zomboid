local MUGGY_ZoneSpawningCore = {}

local MUGGY_EnvironmentDetector = require("MuggyMod/MUGGY_EnvironmentDetector")

local systemActive = false
local pendingZoneSpawns = {}
local tickHandlerActive = false
local SPAWN_DELAY_TICKS = 300

function MUGGY_ZoneSpawningCore.getRandomPositionInZone(zone, playerZ)
    local actualMinX = math.min(zone.minX, zone.maxX)
    local actualMaxX = math.max(zone.minX, zone.maxX)
    local actualMinY = math.min(zone.minY, zone.maxY)
    local actualMaxY = math.max(zone.minY, zone.maxY)

    local x = ZombRand(actualMinX, actualMaxX + 1)
    local y = ZombRand(actualMinY, actualMaxY + 1)
    local z = playerZ or 0
    return x, y, z
end

function MUGGY_ZoneSpawningCore.selectWeightedVariant()
    local muggySpawnOptions = {
        {variant = "babycup", weight = 50},
        {variant = "coffee", weight = 25},
        {variant = "tea", weight = 25}
    }

    local totalWeight = 0
    for _, entry in ipairs(muggySpawnOptions) do
        totalWeight = totalWeight + entry.weight
    end

    local randomValue = ZombRand(totalWeight * 100) / 100

    local currentWeight = 0
    for _, entry in ipairs(muggySpawnOptions) do
        currentWeight = currentWeight + entry.weight
        if randomValue <= currentWeight then
            return entry.variant
        end
    end

    return "babycup"
end

function MUGGY_ZoneSpawningCore.spawnAnimalDirectly(variant, x, y, z)
    local success, result = pcall(function()
        if not AnimalDefinitions then
            print("[MUGGY_ZoneSpawning] ERROR: AnimalDefinitions not available")
            return false
        end

        local animalDef = AnimalDefinitions.getDef(variant)
        if not animalDef then
            print("[MUGGY_ZoneSpawning] ERROR: Could not get definition for " .. tostring(variant))
            return false
        end

        local breeds = animalDef:getBreeds()
        if not breeds or breeds:size() == 0 then
            print("[MUGGY_ZoneSpawning] ERROR: No breeds available for " .. tostring(variant))
            return false
        end

        local breedObj = breeds:get(0)
        if not breedObj then
            print("[MUGGY_ZoneSpawning] ERROR: No breed object available for " .. tostring(variant))
            return false
        end

        local cell = getCell()
        if not cell then
            print("[MUGGY_ZoneSpawning] ERROR: Could not get cell")
            return false
        end

        local square = cell:getGridSquare(x, y, z)
        if not square then
            print("[MUGGY_ZoneSpawning] ERROR: No grid square at coordinates (" .. x .. ", " .. y .. ", " .. z .. ")")
            return false
        end

        local targetCell = square:getCell()
        if not targetCell then
            print("[MUGGY_ZoneSpawning] ERROR: Square has no cell reference")
            return false
        end

        local animal = addAnimal(targetCell, x, y, z, variant, breedObj)
        if not animal then
            print("[MUGGY_ZoneSpawning] ERROR: addAnimal failed for " .. tostring(variant))
            return false
        end

        if animal.addToWorld then
            animal:addToWorld()
        end

        print("[MUGGY_ZoneSpawning] SUCCESS: " .. variant .. " spawned at (" .. x .. ", " .. y .. ", " .. z .. ")")
        return true
    end)

    if not success then
        print("[MUGGY_ZoneSpawning] ERROR: Spawn execution failed: " .. tostring(result))
        return false
    end

    return result
end

function MUGGY_ZoneSpawningCore.addToPendingZoneSpawns(spawnData)
    print("[MUGGY_ZoneSpawning] Adding to pending spawns: zone " .. spawnData.zoneId)
    table.insert(pendingZoneSpawns, spawnData)

    if not tickHandlerActive then
        MUGGY_ZoneSpawningCore.activateTickHandler()
    end
end

function MUGGY_ZoneSpawningCore.onTick()
    if #pendingZoneSpawns == 0 then
        return
    end

    for i = #pendingZoneSpawns, 1, -1 do
        local spawnData = pendingZoneSpawns[i]
        spawnData.tickCount = spawnData.tickCount + 1

        if spawnData.tickCount >= SPAWN_DELAY_TICKS then
            local spawnTypeLabel = spawnData.spawnType and string.upper(spawnData.spawnType) or "UNKNOWN"
            print("[MUGGY_ZoneSpawning] " .. spawnTypeLabel .. " spawn delay reached for zone " .. spawnData.zoneId)

            local variant = MUGGY_ZoneSpawningCore.selectWeightedVariant()
            local x, y, z = MUGGY_ZoneSpawningCore.getRandomPositionInZone(spawnData.zone, spawnData.playerZ)

            if MUGGY_ZoneSpawningCore.spawnAnimalDirectly(variant, x, y, z) then
                print("[MUGGY_ZoneSpawning] " .. spawnTypeLabel .. " spawn completed: " .. variant)
            else
                print("[MUGGY_ZoneSpawning] " .. spawnTypeLabel .. " spawn failed")
            end

            table.remove(pendingZoneSpawns, i)
        end
    end

    if #pendingZoneSpawns == 0 and tickHandlerActive then
        MUGGY_ZoneSpawningCore.deactivateTickHandler()
    end
end

function MUGGY_ZoneSpawningCore.activateTickHandler()
    if not Events or not Events.OnTick then
        print("[MUGGY_ZoneSpawning] WARNING: OnTick event not available")
        return
    end

    if not tickHandlerActive then
        Events.OnTick.Add(MUGGY_ZoneSpawningCore.onTick)
        tickHandlerActive = true
        print("[MUGGY_ZoneSpawning] Tick handler activated")
    end
end

function MUGGY_ZoneSpawningCore.deactivateTickHandler()
    if Events and Events.OnTick and tickHandlerActive then
        Events.OnTick.Remove(MUGGY_ZoneSpawningCore.onTick)
        tickHandlerActive = false
        print("[MUGGY_ZoneSpawning] Tick handler deactivated")
    end
end

function MUGGY_ZoneSpawningCore.initialize()
    print("[MUGGY_ZoneSpawning] Initializing spawning core")
    pendingZoneSpawns = {}
    tickHandlerActive = false
    systemActive = true
    print("[MUGGY_ZoneSpawning] Spawning core initialized")
end

function MUGGY_ZoneSpawningCore.shutdown()
    print("[MUGGY_ZoneSpawning] Shutting down spawning core")

    if tickHandlerActive then
        MUGGY_ZoneSpawningCore.deactivateTickHandler()
    end

    pendingZoneSpawns = {}
    systemActive = false
    print("[MUGGY_ZoneSpawning] Spawning core shutdown complete")
end

return MUGGY_ZoneSpawningCore
