local MUGGY_ZoneDefinitions = require("MuggyMod/MUGGY_ZoneDefinitions")
local MUGGY_EnvironmentDetector = require("MuggyMod/MUGGY_EnvironmentDetector")

local MUGGY_ZoneResponseSystem = nil
local function getZoneResponseSystem()
    if not MUGGY_ZoneResponseSystem and MUGGY_EnvironmentDetector.isSinglePlayer() then
        local success, result = pcall(function()
            return require("NPCSystem/MUGGY_ZoneResponseSystem")
        end)
        if success and result then
            MUGGY_ZoneResponseSystem = result
        end
    end
    return MUGGY_ZoneResponseSystem
end

local MUGGY_ZoneResponseClient = {}

local lastPlayerPosition = nil
local lastZoneCheck = 0
local playerActiveZones = {}

local MOVEMENT_THRESHOLD = 5
local ZONE_CHECK_COOLDOWN = 3
local MAX_ZONE_PROXIMITY = 100

local function checkPlayerZones(player)
    if not player or player:isDead() then
        return
    end

    local currentTime = getGameTime():getWorldAgeHours()
    local playerX, playerY = player:getX(), player:getY()

    local hasMovedSignificantly = false

    if not lastPlayerPosition then
        hasMovedSignificantly = true
        lastPlayerPosition = {x = playerX, y = playerY}
    else
        local distance = math.sqrt((playerX - lastPlayerPosition.x)^2 + (playerY - lastPlayerPosition.y)^2)
        if distance >= MOVEMENT_THRESHOLD then
            hasMovedSignificantly = true
            lastPlayerPosition = {x = playerX, y = playerY}
        end
    end

    if not hasMovedSignificantly then
        return
    end

    if currentTime - lastZoneCheck < (ZONE_CHECK_COOLDOWN / 3600) then
        return
    end

    lastZoneCheck = currentTime

    local currentZones = {}

    for _, zone in ipairs(MUGGY_ZoneDefinitions.spawnZones) do
        if zone.enabled then
            local distanceToZone = MUGGY_ZoneDefinitions.getApproxDistanceToZone(playerX, playerY, zone)
            if distanceToZone <= MAX_ZONE_PROXIMITY then
                if MUGGY_ZoneDefinitions.isPlayerInZone(playerX, playerY, zone) then
                    currentZones[zone.id] = true

                    if not playerActiveZones[zone.id] then
                        print("[MUGGY_ZoneClient] Player entered zone: " .. zone.id)

                        playerActiveZones[zone.id] = true

                        if MUGGY_EnvironmentDetector.isMultiplayer() then
                            sendClientCommand("MUGGY_ZoneSystem", "playerZoneEntry", {
                                x = playerX,
                                y = playerY,
                                z = player:getZ(),
                                zoneId = zone.id,
                                entryTime = currentTime
                            })
                        else
                            local zoneSystem = getZoneResponseSystem()
                            if zoneSystem and zoneSystem.processDirectZoneEntry then
                                zoneSystem.processDirectZoneEntry(player, zone, currentTime)
                            end
                        end
                    end
                end
            end
        end
    end

    for zoneId, wasInZone in pairs(playerActiveZones) do
        if wasInZone and not currentZones[zoneId] then
            print("[MUGGY_ZoneClient] Player exited zone: " .. zoneId)

            playerActiveZones[zoneId] = nil

            if MUGGY_EnvironmentDetector.isMultiplayer() then
                sendClientCommand("MUGGY_ZoneSystem", "playerZoneExit", {
                    zoneId = zoneId,
                    exitTime = currentTime
                })
            else
                local zoneSystem = getZoneResponseSystem()
                if zoneSystem and zoneSystem.processDirectZoneExit then
                    zoneSystem.processDirectZoneExit(player, zoneId, currentTime)
                end
            end
        end
    end
end

local function onPlayerUpdate()
    local player = getSpecificPlayer(0)
    if player then
        local success, result = pcall(function()
            checkPlayerZones(player)
        end)

        if not success then
            print("[MUGGY_ZoneClient] Error in zone check: " .. tostring(result))
        end
    end
end

function MUGGY_ZoneResponseClient.initialize()
    print("[MUGGY_ZoneClient] Initializing client-side zone movement detection")

    lastPlayerPosition = nil
    lastZoneCheck = 0
    playerActiveZones = {}

    if Events and Events.OnPlayerUpdate then
        Events.OnPlayerUpdate.Add(onPlayerUpdate)
        print("[MUGGY_ZoneClient] Registered OnPlayerUpdate handler")
    else
        print("[MUGGY_ZoneClient] WARNING: OnPlayerUpdate event not available")
    end

    print("[MUGGY_ZoneClient] Zone movement detection initialized")
end

function MUGGY_ZoneResponseClient.shutdown()
    print("[MUGGY_ZoneClient] Shutting down client-side zone detection")

    if Events and Events.OnPlayerUpdate then
        Events.OnPlayerUpdate.Remove(onPlayerUpdate)
    end

    lastPlayerPosition = nil
    lastZoneCheck = 0
    playerActiveZones = {}
end

if Events and Events.OnGameEnd then
    Events.OnGameEnd.Add(MUGGY_ZoneResponseClient.shutdown)
end

if Events and Events.OnDisconnect then
    Events.OnDisconnect.Add(MUGGY_ZoneResponseClient.shutdown)
end

return MUGGY_ZoneResponseClient
