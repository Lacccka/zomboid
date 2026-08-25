local MUGGY_ZoneResponseSystem = {}

local MUGGY_EnvironmentDetector = require("MuggyMod/MUGGY_EnvironmentDetector")
local MUGGY_ZoneDefinitions = require("MuggyMod/MUGGY_ZoneDefinitions")
local MUGGY_ZoneSpawningCore = require("MuggyMod/MUGGY_ZoneSpawningCore")

local playerZoneState = {}
local zoneMovementCache = {}
local lastPlayerPositions = {}

function MUGGY_ZoneResponseSystem.initializeZoneSystem()
    print("[MUGGY_ZoneResponse] Initializing zone system")

    local success, result = pcall(function()
        local modData = ModData.getOrCreate("MUGGY_ZoneSpawning_LastSeen")

        if not modData.primarySpawnCompleted then
            modData.primarySpawnCompleted = false
        end

        if not modData.primarySpawnWorldAge then
            modData.primarySpawnWorldAge = nil
        end

        if not modData.primarySpawnZoneId then
            modData.primarySpawnZoneId = nil
        end

        if not modData.contingentSpawnEnabled then
            modData.contingentSpawnEnabled = false
        end

        if not modData.contingentSpawnCompleted then
            modData.contingentSpawnCompleted = false
        end

        if not modData.zones then
            print("[MUGGY_ZoneResponse] Creating zones table for first time")
            modData.zones = {}
        else
            print("[MUGGY_ZoneResponse] Zones table exists")
        end

        local zoneCount = 0
        for _, zone in ipairs(MUGGY_ZoneDefinitions.spawnZones) do
            if zone.enabled then
                if not modData.zones[zone.id] then
                    print("[MUGGY_ZoneResponse] Creating zone data for: " .. zone.id)
                    modData.zones[zone.id] = {
                        lastVisited = 0,
                        lastSpawnTime = 0,
                        primarySpawnDisabled = false
                    }
                else
                    print("[MUGGY_ZoneResponse] Zone data exists for: " .. zone.id)
                    if modData.zones[zone.id].primarySpawnDisabled == nil then
                        modData.zones[zone.id].primarySpawnDisabled = false
                    end
                end
                zoneCount = zoneCount + 1
            end
        end

        modData.systemInitialized = true
        modData.lastSystemCheck = getGameTime():getWorldAgeHours()

        print("[MUGGY_ZoneResponse] Primary spawn completed: " .. tostring(modData.primarySpawnCompleted))
        if modData.primarySpawnCompleted then
            print("[MUGGY_ZoneResponse] Primary spawn zone: " .. tostring(modData.primarySpawnZoneId))
            print("[MUGGY_ZoneResponse] Primary spawn world age: " .. tostring(modData.primarySpawnWorldAge))
        end
        print("[MUGGY_ZoneResponse] Contingent spawn completed: " .. tostring(modData.contingentSpawnCompleted))

        print("[MUGGY_ZoneResponse] Saving zone system initialization data")
        ModData.add("MUGGY_ZoneSpawning_LastSeen", modData)

        print("[MUGGY_ZoneResponse] Server environment - transmitting ModData")
        return true
    end)

    if not success then
        print("[MUGGY_ZoneResponse] ERROR: Zone system initialization failed: " .. tostring(result))
    else
        print("[MUGGY_ZoneResponse] Zone system initialization SUCCESS")
    end
end

function MUGGY_ZoneResponseSystem.updateZoneLastSeen(zoneId, currentTime)
    local modData = ModData.getOrCreate("MUGGY_ZoneSpawning_LastSeen")

    if not modData.zones then
        modData.zones = {}
    end

    if not modData.zones[zoneId] then
        modData.zones[zoneId] = {
            lastVisited = currentTime,
            lastSpawnTime = 0,
            primarySpawnDisabled = false
        }
    else
        modData.zones[zoneId].lastVisited = currentTime
    end

    ModData.add("MUGGY_ZoneSpawning_LastSeen", modData)
end

function MUGGY_ZoneResponseSystem.disableAllOtherZones(primaryZoneId)
    local modData = ModData.getOrCreate("MUGGY_ZoneSpawning_LastSeen")

    if not modData.zones then
        return
    end

    print("[MUGGY_ZoneResponse] Disabling all zones except: " .. primaryZoneId)

    for zoneId, zoneData in pairs(modData.zones) do
        if zoneId ~= primaryZoneId then
            zoneData.primarySpawnDisabled = true
            print("[MUGGY_ZoneResponse] Disabled zone: " .. zoneId)
        end
    end

    ModData.add("MUGGY_ZoneSpawning_LastSeen", modData)
end

function MUGGY_ZoneResponseSystem.isPrimarySpawnEligible(modData, zoneId)
    local settings = MUGGY_ZoneDefinitions.spawnSettings

    if not settings.primarySpawn.enabled then
        print("[MUGGY_ZoneResponse] Primary spawn disabled in settings")
        return false
    end

    if modData.primarySpawnCompleted then
        print("[MUGGY_ZoneResponse] Primary spawn already completed globally")
        return false
    end

    if not modData.zones or not modData.zones[zoneId] then
        print("[MUGGY_ZoneResponse] Zone data not initialized: " .. zoneId)
        return false
    end

    if modData.zones[zoneId].primarySpawnDisabled then
        print("[MUGGY_ZoneResponse] Zone disabled for primary spawn: " .. zoneId)
        return false
    end

    print("[MUGGY_ZoneResponse] Primary spawn eligible for zone: " .. zoneId)
    return true
end

function MUGGY_ZoneResponseSystem.isContingentSpawnEligible(modData, zoneId, currentTime)
    local settings = MUGGY_ZoneDefinitions.spawnSettings

    if not settings.contingentSpawn.enabled then
        print("[MUGGY_ZoneResponse] Contingent spawn disabled in settings")
        return false
    end

    if not modData.primarySpawnCompleted then
        print("[MUGGY_ZoneResponse] Primary spawn not yet completed, contingent unavailable")
        return false
    end

    if modData.contingentSpawnCompleted then
        print("[MUGGY_ZoneResponse] Contingent spawn already completed")
        return false
    end

    if not modData.primarySpawnWorldAge then
        print("[MUGGY_ZoneResponse] ERROR: Primary spawn world age not recorded")
        return false
    end

    local elapsedHours = currentTime - modData.primarySpawnWorldAge
    local requiredHours = settings.contingentSpawn.hoursAfterPrimary

    print("[MUGGY_ZoneResponse] Time since primary spawn: " .. string.format("%.2f", elapsedHours) .. " hours")
    print("[MUGGY_ZoneResponse] Required time: " .. requiredHours .. " hours")

    if elapsedHours < requiredHours then
        print("[MUGGY_ZoneResponse] Not enough time elapsed for contingent spawn")
        return false
    end

    local failureRoll = ZombRand(100)
    local failureThreshold = settings.contingentSpawn.failureRate * 100

    print("[MUGGY_ZoneResponse] Contingent spawn roll: " .. failureRoll .. " vs threshold: " .. failureThreshold)

    if failureRoll < failureThreshold then
        print("[MUGGY_ZoneResponse] Contingent spawn failed (rolled: " .. failureRoll .. ")")
        return false
    end

    print("[MUGGY_ZoneResponse] Contingent spawn eligible (rolled: " .. failureRoll .. ")")
    return true
end

function MUGGY_ZoneResponseSystem.processDirectZoneEntry(player, zone, currentTime)
    print("[MUGGY_ZoneResponse] ========== ZONE ENTRY: " .. zone.id .. " ==========")

    local success, result = pcall(function()
        MUGGY_ZoneResponseSystem.updateZoneLastSeen(zone.id, currentTime)

        local modData = ModData.getOrCreate("MUGGY_ZoneSpawning_LastSeen")
        local zoneData = modData.zones[zone.id]

        if not zoneData then
            print("[MUGGY_ZoneResponse] ERROR: Zone data not initialized for " .. zone.id)
            return false
        end

        local shouldSpawn = false
        local spawnType = nil

        if MUGGY_ZoneResponseSystem.isPrimarySpawnEligible(modData, zone.id) then
            print("[MUGGY_ZoneResponse] PRIMARY SPAWN TRIGGERED")
            shouldSpawn = true
            spawnType = "primary"

            modData.primarySpawnCompleted = true
            modData.primarySpawnWorldAge = currentTime
            modData.primarySpawnZoneId = zone.id

            print("[MUGGY_ZoneResponse] Primary spawn timestamp: " .. currentTime)
            print("[MUGGY_ZoneResponse] Primary spawn zone: " .. zone.id)

            local settings = MUGGY_ZoneDefinitions.spawnSettings
            if settings.primarySpawn.disableOtherZonesAfterSpawn then
                MUGGY_ZoneResponseSystem.disableAllOtherZones(zone.id)
            end

        elseif MUGGY_ZoneResponseSystem.isContingentSpawnEligible(modData, zone.id, currentTime) then
            print("[MUGGY_ZoneResponse] CONTINGENT SPAWN TRIGGERED")
            shouldSpawn = true
            spawnType = "contingent"

            modData.contingentSpawnCompleted = true
            modData.contingentSpawnEnabled = true

            print("[MUGGY_ZoneResponse] Contingent spawn completed at world age: " .. currentTime)
        else
            print("[MUGGY_ZoneResponse] No spawn triggered")
        end

        if shouldSpawn then
            zoneData.lastSpawnTime = currentTime
            ModData.add("MUGGY_ZoneSpawning_LastSeen", modData)

            local spawnData = {
                zoneId = zone.id,
                zone = zone,
                playerZ = player:getZ(),
                tickCount = 0,
                spawnType = spawnType
            }

            MUGGY_ZoneSpawningCore.addToPendingZoneSpawns(spawnData)
            print("[MUGGY_ZoneResponse] " .. string.upper(spawnType) .. " spawn scheduled for zone: " .. zone.id)
        end

        return true
    end)

    if not success then
        print("[MUGGY_ZoneResponse] ERROR: processDirectZoneEntry failed: " .. tostring(result))
    end
end

function MUGGY_ZoneResponseSystem.onPlayerEnterZone(player, args)
    local zoneId = args.zoneId
    local zone = MUGGY_ZoneDefinitions.getZoneById(zoneId)

    if not zone then
        print("[MUGGY_ZoneResponse] ERROR: Zone not found: " .. zoneId)
        return
    end

    local currentTime = getGameTime():getWorldAgeHours()
    MUGGY_ZoneResponseSystem.processDirectZoneEntry(player, zone, currentTime)
end

function MUGGY_ZoneResponseSystem.processDirectZoneExit(player, zoneId, currentTime)
    print("[MUGGY_ZoneResponse] Processing zone exit for zone: " .. zoneId)
end

function MUGGY_ZoneResponseSystem.onPlayerExitZone(player, args)
    local zoneId = args.zoneId
    local currentTime = getGameTime():getWorldAgeHours()
    MUGGY_ZoneResponseSystem.processDirectZoneExit(player, zoneId, currentTime)
end

function MUGGY_ZoneResponseSystem.initialize()
    print("[MUGGY_ZoneResponse] Initializing zone response system")
    MUGGY_ZoneResponseSystem.initializeZoneSystem()
    print("[MUGGY_ZoneResponse] Zone response system initialized")
end

return MUGGY_ZoneResponseSystem
