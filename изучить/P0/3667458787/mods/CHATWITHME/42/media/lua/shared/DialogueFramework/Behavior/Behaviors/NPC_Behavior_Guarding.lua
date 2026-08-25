local NPC_Behavior_Guarding = {}

local NPC_GuardConfig = require("DialogueFramework/Behavior/NPC_GuardConfig")
local NPC_GuardStateManager = require("DialogueFramework/Behavior/NPC_GuardStateManager")
local NPC_GuardZoneBoundaryEnforcer = require("DialogueFramework/Behavior/NPC_GuardZoneBoundaryEnforcer")
local NPC_BehaviorNPCRegistry = require("DialogueFramework/Behavior/NPC_BehaviorNPCRegistry")
local NPC_BehaviorController = require("DialogueFramework/Behavior/NPC_BehaviorController")
local MUGGY_ZoneDefinitions = require("MuggyMod/MUGGY_ZoneDefinitions")

local activeGuards = {}
local lastZombieDetectionCheck = {}
local zombieDetectionHandlerActive = false

local function getZombieDetectionInterval(player, zone)
    if not player or not zone then
        return nil
    end

    local playerX = player:getX()
    local playerY = player:getY()

    if MUGGY_ZoneDefinitions.isPlayerInZone(playerX, playerY, zone) then
        local min = NPC_GuardConfig.ZOMBIE_DETECTION.PLAYER_IN_ZONE_MIN
        local max = NPC_GuardConfig.ZOMBIE_DETECTION.PLAYER_IN_ZONE_MAX
        return (ZombRand(min, max + 1) * 60)
    end

    local distance = MUGGY_ZoneDefinitions.getApproxDistanceToZone(playerX, playerY, zone)

    if distance <= NPC_GuardConfig.ZOMBIE_DETECTION.PLAYER_NEAR_ZONE_DISTANCE then
        return NPC_GuardConfig.ZOMBIE_DETECTION.PLAYER_NEAR_ZONE_MINUTES * 60
    elseif distance <= NPC_GuardConfig.ZOMBIE_DETECTION.PLAYER_FAR_ZONE_DISTANCE then
        return NPC_GuardConfig.ZOMBIE_DETECTION.PLAYER_FAR_ZONE_MINUTES * 60
    else
        if NPC_GuardConfig.ZOMBIE_DETECTION.PLAYER_BEYOND_RENDER_DORMANT then
            return nil
        end
    end

    return nil
end

local function scanForZombiesInZone(zone)
    if not zone then
        return {}
    end

    local cell = getCell()
    if not cell then
        return {}
    end

    local zombies = {}

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
                        if obj and instanceof(obj, "IsoZombie") then
                            if not obj:isDead() then
                                table.insert(zombies, obj)
                            end
                        end
                    end
                end
            end
        end
    end

    return zombies
end

local function shouldCheckForZombies(npcID, player, zone)
    if not npcID or not player or not zone then
        return false
    end

    local interval = getZombieDetectionInterval(player, zone)
    if not interval then
        return false
    end

    local currentTime = getTimestampMs() / 1000.0
    local lastCheck = lastZombieDetectionCheck[npcID] or 0

    if currentTime - lastCheck < interval then
        return false
    end

    lastZombieDetectionCheck[npcID] = currentTime

    return true
end

local function checkForZombiesAndTriggerAttack(npc, npcID, player, zone)
    if not npc or not npcID or not player or not zone then
        return false
    end

    if not shouldCheckForZombies(npcID, player, zone) then
        return false
    end

    local zombies = scanForZombiesInZone(zone)

    if #zombies > 0 then
        local attackConfig = NPC_GuardConfig.ATTACK.MUGGY

        local success, behaviorEntry = NPC_BehaviorController.queueBehavior(
            npc,
            "attacking",
            {
                zone = zone,
                attackConfig = attackConfig,
                player = player
            },
            {
                priority = NPC_GuardConfig.PRIORITIES.ATTACKING
            }
        )

        return success
    end

    return false
end

local function onGuardUpdate()
    if not zombieDetectionHandlerActive then
        return
    end

    local player = getSpecificPlayer(0)
    if not player then
        return
    end

    for npcID, guardData in pairs(activeGuards) do
        local npc = guardData.npc
        local zone = guardData.zone

        if not NPC_BehaviorNPCRegistry.isValidNPC(npc) then
            activeGuards[npcID] = nil
        elseif not npc:isExistInTheWorld() then
            activeGuards[npcID] = nil
        elseif npc:isDead() then
            activeGuards[npcID] = nil
        else
            NPC_GuardZoneBoundaryEnforcer.onNPCPositionChange(npc, player)

            checkForZombiesAndTriggerAttack(npc, npcID, player, zone)
        end
    end

    local hasActiveGuards = false
    for _ in pairs(activeGuards) do
        hasActiveGuards = true
        break
    end

    if not hasActiveGuards and zombieDetectionHandlerActive then
        Events.EveryTenMinutes.Remove(onGuardUpdate)
        zombieDetectionHandlerActive = false
    end
end

function NPC_Behavior_Guarding.execute(npc, params, player)
    if not npc then
        return false, "NPC is nil"
    end

    if not params then
        return false, "Params is nil"
    end

    if not params.zone then
        return false, "Zone is nil"
    end

    if not player then
        player = getSpecificPlayer(0)
    end

    if not player then
        return false, "Player is nil"
    end

    local npcID = NPC_BehaviorNPCRegistry.getNPCID(npc)
    if not npcID then
        return false, "Failed to get NPC ID"
    end

    local success = NPC_GuardStateManager.activateGuarding(player, npcID, params.zone.id)
    if not success then
        return false, "Failed to activate guarding state"
    end

    activeGuards[npcID] = {
        npc = npc,
        zone = params.zone,
        player = player,
        startTime = getTimestampMs() / 1000.0
    }

    if not zombieDetectionHandlerActive then
        Events.EveryTenMinutes.Add(onGuardUpdate)
        zombieDetectionHandlerActive = true
    end

    if isClient() and not isServer() then
        sendClientCommand(NPC_GuardConfig.COMMANDS.GUARD_SYSTEM.MODULE,
                        NPC_GuardConfig.COMMANDS.GUARD_SYSTEM.ACTIVATE, {
            npcID = npcID,
            zoneID = params.zone.id,
            playerID = player:getOnlineID()
        })
    end

    return true, "Guarding behavior started"
end

function NPC_Behavior_Guarding.canExecute(npc, params)
    if not npc then
        return false
    end

    if not params or not params.zone then
        return false
    end

    return true
end

function NPC_Behavior_Guarding.refresh(npc, player)
    if not npc or not player then
        return false
    end

    local npcID = NPC_BehaviorNPCRegistry.getNPCID(npc)
    if not npcID then
        return false
    end

    local guardState = NPC_GuardStateManager.getGuardState(player, npcID)
    if not guardState then
        return false
    end

    if not guardState.assignedZoneID then
        return false
    end

    local zone = MUGGY_ZoneDefinitions.getZoneById(guardState.assignedZoneID)
    if not zone then
        return false
    end

    if not activeGuards[npcID] then
        activeGuards[npcID] = {
            npc = npc,
            zone = zone,
            player = player,
            startTime = getTimestampMs() / 1000.0
        }

        if not zombieDetectionHandlerActive then
            Events.EveryTenMinutes.Add(onGuardUpdate)
            zombieDetectionHandlerActive = true
        end
    end

    if isClient() and not isServer() then
        sendClientCommand(NPC_GuardConfig.COMMANDS.GUARD_SYSTEM.MODULE,
                        NPC_GuardConfig.COMMANDS.GUARD_SYSTEM.REFRESH, {
            npcID = npcID,
            zoneID = zone.id,
            playerID = player:getOnlineID()
        })
    end

    return true
end

function NPC_Behavior_Guarding.cleanup(npc)
    if not npc then
        return
    end

    local npcID = NPC_BehaviorNPCRegistry.getNPCID(npc)
    if not npcID then
        return
    end

    activeGuards[npcID] = nil
    lastZombieDetectionCheck[npcID] = nil

    NPC_GuardZoneBoundaryEnforcer.cleanup(npcID)

    local hasActiveGuards = false
    for _ in pairs(activeGuards) do
        hasActiveGuards = true
        break
    end

    if not hasActiveGuards and zombieDetectionHandlerActive then
        Events.EveryTenMinutes.Remove(onGuardUpdate)
        zombieDetectionHandlerActive = false
    end
end

function NPC_Behavior_Guarding.onComplete(npc, params, result)
    NPC_Behavior_Guarding.cleanup(npc)
end

function NPC_Behavior_Guarding.onFailed(npc, params, reason)
    NPC_Behavior_Guarding.cleanup(npc)
end

return NPC_Behavior_Guarding
