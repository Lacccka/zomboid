local NPC_GuardZoneScanner = {}

local NPC_GuardConfig = require("DialogueFramework/Behavior/NPC_GuardConfig")
local NPC_GuardStateManager = require("DialogueFramework/Behavior/NPC_GuardStateManager")
local NPC_BehaviorNPCRegistry = require("DialogueFramework/Behavior/NPC_BehaviorNPCRegistry")
local MUGGY_ZoneDefinitions = require("MuggyMod/MUGGY_ZoneDefinitions")

local scanActive = false
local lastPlayerPosition = nil
local lastZoneCheck = 0
local previousZones = {}
local nextTimedRefresh = 0

local timedRefreshHandlerActive = false
local hourlyRefreshHandlerActive = false
local playerUpdateHandlerActive = false

local function getNearestZoneInRange(playerX, playerY, maxRange)
    if not playerX or not playerY or not maxRange then
        return nil, nil
    end

    local zones = MUGGY_ZoneDefinitions.getEnabledZones()
    if not zones then
        return nil, nil
    end

    local nearestZone = nil
    local nearestDistance = maxRange + 1

    for _, zone in ipairs(zones) do
        local distance = MUGGY_ZoneDefinitions.getApproxDistanceToZone(playerX, playerY, zone)
        if distance < nearestDistance and distance <= maxRange then
            nearestZone = zone
            nearestDistance = distance
        end
    end

    return nearestZone, nearestDistance
end

local function scanForNPCInZone(zone)
    if not zone then
        return nil
    end

    local cell = getCell()
    if not cell then
        return nil
    end

    local minX = math.min(zone.minX, zone.maxX)
    local maxX = math.max(zone.minX, zone.maxX)
    local minY = math.min(zone.minY, zone.maxY)
    local maxY = math.max(zone.minY, zone.maxY)

    for x = minX, maxX do
        for y = minY, maxY do
            local square = cell:getGridSquare(x, y, 0)
            if square then
                local objects = square:getObjects()
                if objects then
                    for i = 0, objects:size() - 1 do
                        local obj = objects:get(i)
                        if obj and NPC_BehaviorNPCRegistry.isValidNPC(obj) then
                            return obj, zone
                        end
                    end
                end
            end
        end
    end

    return nil, nil
end

local function performNPCDetectionScan(zone)
    if not zone then
        return false
    end

    local npc, detectedZone = scanForNPCInZone(zone)

    if npc and detectedZone then
        local player = getSpecificPlayer(0)
        if not player then
            return false
        end

        local npcID = NPC_BehaviorNPCRegistry.getNPCID(npc)
        if not npcID then
            return false
        end

        local success = NPC_GuardStateManager.activateGuarding(player, npcID, detectedZone.id)

        if success then
            local NPC_Behavior_Guarding = require("DialogueFramework/Behavior/Behaviors/NPC_Behavior_Guarding")
            NPC_Behavior_Guarding.refresh(npc, player)

            if isClient() and not isServer() then
                sendClientCommand(NPC_GuardConfig.COMMANDS.SCAN_SYSTEM.MODULE,
                                NPC_GuardConfig.COMMANDS.SCAN_SYSTEM.NPC_DETECTED, {
                    npcID = npcID,
                    zoneID = detectedZone.id,
                    playerID = player:getOnlineID()
                })
            end

            NPC_GuardZoneScanner.setScanDormant()

            return true
        end
    end

    return false
end

local function scheduleNextTimedRefresh()
    local minMinutes = NPC_GuardConfig.ZONE_SCAN.TIMED_REFRESH.MIN_MINUTES
    local maxMinutes = NPC_GuardConfig.ZONE_SCAN.TIMED_REFRESH.MAX_MINUTES

    local randomMinutes = ZombRand(minMinutes, maxMinutes + 1)

    local currentTime = getTimestampMs() / 1000.0
    nextTimedRefresh = currentTime + (randomMinutes * 60)
end

local function onTimedRefresh()
    if not scanActive then
        return
    end

    local currentTime = getTimestampMs() / 1000.0

    if currentTime < nextTimedRefresh then
        return
    end

    local player = getSpecificPlayer(0)
    if not player then
        scheduleNextTimedRefresh()
        return
    end

    local playerX = player:getX()
    local playerY = player:getY()

    local nearestZone, distance = getNearestZoneInRange(
        playerX,
        playerY,
        NPC_GuardConfig.ZONE_SCAN.MAX_RENDER_DISTANCE
    )

    if nearestZone then
        performNPCDetectionScan(nearestZone)
    else
        NPC_GuardZoneScanner.setScanDormant()
    end

    scheduleNextTimedRefresh()
end

local function getZonesPlayerIsIn(player)
    if not player then
        return {}
    end

    local playerX = player:getX()
    local playerY = player:getY()

    local zones = MUGGY_ZoneDefinitions.getEnabledZones()
    if not zones then
        return {}
    end

    local currentZones = {}

    for _, zone in ipairs(zones) do
        if MUGGY_ZoneDefinitions.isPlayerInZone(playerX, playerY, zone) then
            table.insert(currentZones, zone)
        end
    end

    return currentZones
end

local function onPlayerReEntry()
    local player = getSpecificPlayer(0)
    if not player then
        return
    end

    local currentZones = getZonesPlayerIsIn(player)

    for _, zone in ipairs(currentZones) do
        local wasInZone = false
        for _, prevZone in ipairs(previousZones) do
            if prevZone.id == zone.id then
                wasInZone = true
                break
            end
        end

        if not wasInZone then
            if scanActive then
                performNPCDetectionScan(zone)
            else
                NPC_GuardZoneScanner.activateScan()
                performNPCDetectionScan(zone)
            end
        end
    end

    previousZones = currentZones
end

local function onPlayerUpdate()
    if not scanActive then
        return
    end

    local player = getSpecificPlayer(0)
    if not player then
        return
    end

    local playerX = player:getX()
    local playerY = player:getY()

    local hasMovedSignificantly = false

    if not lastPlayerPosition then
        hasMovedSignificantly = true
        lastPlayerPosition = {x = playerX, y = playerY}
    else
        local distance = math.sqrt(
            (playerX - lastPlayerPosition.x) ^ 2 +
            (playerY - lastPlayerPosition.y) ^ 2
        )
        if distance >= NPC_GuardConfig.ZONE_SCAN.MOVEMENT_THRESHOLD then
            hasMovedSignificantly = true
            lastPlayerPosition = {x = playerX, y = playerY}
        end
    end

    if not hasMovedSignificantly then
        return
    end

    local currentTime = getTimestampMs() / 1000.0
    local gameTime = getGameTime()
    if not gameTime then
        return
    end

    local worldAge = gameTime:getWorldAgeHours()

    if currentTime - lastZoneCheck < NPC_GuardConfig.ZONE_SCAN.ZONE_CHECK_COOLDOWN then
        return
    end

    lastZoneCheck = currentTime

    onPlayerReEntry()
end

local function onHourlyExtendedRangeCheck()
    local player = getSpecificPlayer(0)
    if not player then
        return
    end

    local playerX = player:getX()
    local playerY = player:getY()

    local nearestZone, distance = getNearestZoneInRange(
        playerX,
        playerY,
        NPC_GuardConfig.ZONE_SCAN.EXTENDED_RANGE_DISTANCE
    )

    if nearestZone and distance <= NPC_GuardConfig.ZONE_SCAN.EXTENDED_RANGE_DISTANCE then
        if not scanActive then
            NPC_GuardZoneScanner.activateScan()
        end
        performNPCDetectionScan(nearestZone)
    end
end

function NPC_GuardZoneScanner.activateScan()
    if scanActive then
        return true
    end

    scanActive = true

    if not timedRefreshHandlerActive then
        Events.EveryTenMinutes.Add(onTimedRefresh)
        timedRefreshHandlerActive = true
        scheduleNextTimedRefresh()
    end

    if not hourlyRefreshHandlerActive then
        Events.EveryHours.Add(onHourlyExtendedRangeCheck)
        hourlyRefreshHandlerActive = true
    end

    if not playerUpdateHandlerActive then
        Events.OnPlayerUpdate.Add(onPlayerUpdate)
        playerUpdateHandlerActive = true
    end

    if isClient() and not isServer() then
        sendClientCommand(NPC_GuardConfig.COMMANDS.SCAN_SYSTEM.MODULE,
                        NPC_GuardConfig.COMMANDS.SCAN_SYSTEM.ACTIVATE, {})
    end

    return true
end

function NPC_GuardZoneScanner.setScanDormant()
    if not scanActive then
        return true
    end

    scanActive = false

    if timedRefreshHandlerActive then
        Events.EveryTenMinutes.Remove(onTimedRefresh)
        timedRefreshHandlerActive = false
    end

    if hourlyRefreshHandlerActive then
        Events.EveryHours.Remove(onHourlyExtendedRangeCheck)
        hourlyRefreshHandlerActive = false
    end

    if playerUpdateHandlerActive then
        Events.OnPlayerUpdate.Remove(onPlayerUpdate)
        playerUpdateHandlerActive = false
    end

    if isClient() and not isServer() then
        sendClientCommand(NPC_GuardConfig.COMMANDS.SCAN_SYSTEM.MODULE,
                        NPC_GuardConfig.COMMANDS.SCAN_SYSTEM.SCAN_DORMANT, {})
    end

    return true
end

function NPC_GuardZoneScanner.refreshScan()
    if not scanActive then
        NPC_GuardZoneScanner.activateScan()
    end

    local player = getSpecificPlayer(0)
    if not player then
        return false
    end

    local playerX = player:getX()
    local playerY = player:getY()

    local nearestZone, distance = getNearestZoneInRange(
        playerX,
        playerY,
        NPC_GuardConfig.ZONE_SCAN.MAX_RENDER_DISTANCE
    )

    if nearestZone then
        performNPCDetectionScan(nearestZone)
        return true
    end

    return false
end

function NPC_GuardZoneScanner.isScanActive()
    return scanActive
end

function NPC_GuardZoneScanner.initialize()
    scanActive = false
    lastPlayerPosition = nil
    lastZoneCheck = 0
    previousZones = {}
    nextTimedRefresh = 0

    NPC_GuardZoneScanner.activateScan()

    return true
end

function NPC_GuardZoneScanner.shutdown()
    NPC_GuardZoneScanner.setScanDormant()

    lastPlayerPosition = nil
    previousZones = {}

    return true
end

return NPC_GuardZoneScanner
