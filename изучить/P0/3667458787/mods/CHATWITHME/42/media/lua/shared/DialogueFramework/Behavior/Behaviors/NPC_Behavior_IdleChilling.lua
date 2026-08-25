local NPC_Behavior_IdleChilling = {}

local NPC_GuardConfig = require("DialogueFramework/Behavior/NPC_GuardConfig")
local NPC_BehaviorNPCRegistry = require("DialogueFramework/Behavior/NPC_BehaviorNPCRegistry")
local NPC_BehaviorController = require("DialogueFramework/Behavior/NPC_BehaviorController")
local MUGGY_ZoneDefinitions = require("MuggyMod/MUGGY_ZoneDefinitions")

local idleChillingStates = {}
local idleUpdateActive = false

local function getCurrentTime()
    return getTimestampMs() / 1000.0
end

local function randomBetween(min, max)
    if not min or not max then
        return min or 1
    end

    return min + (ZombRand(0, 100) / 100.0) * (max - min)
end

local function performRandomMove(npc, zone)
    if not npc or not zone then
        return false
    end

    local npcX = npc:getX()
    local npcY = npc:getY()

    if not npcX or not npcY then
        return false
    end

    local moveDistance = randomBetween(
        NPC_GuardConfig.IDLE_CHILLING.DISTANCE_MIN,
        NPC_GuardConfig.IDLE_CHILLING.DISTANCE_MAX
    )

    local angle = math.random() * 2 * math.pi
    local targetX = npcX + (math.cos(angle) * moveDistance)
    local targetY = npcY + (math.sin(angle) * moveDistance)

    local minX = math.min(zone.minX, zone.maxX)
    local maxX = math.max(zone.minX, zone.maxX)
    local minY = math.min(zone.minY, zone.maxY)
    local maxY = math.max(zone.minY, zone.maxY)

    targetX = math.max(minX, math.min(maxX, targetX))
    targetY = math.max(minY, math.min(maxY, targetY))

    local success, error = pcall(function()
        npc:setX(targetX)
        npc:setY(targetY)
    end)

    if not success then
        return false
    end

    if isClient() and not isServer() then
        sendClientCommand(NPC_GuardConfig.COMMANDS.IDLE_SYSTEM.MODULE,
                        NPC_GuardConfig.COMMANDS.IDLE_SYSTEM.EXECUTE_MOVE, {
            npcID = NPC_BehaviorNPCRegistry.getNPCID(npc),
            targetX = targetX,
            targetY = targetY
        })
    end

    return true
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

local function checkForZombiesAndTransitionToAttack(npc, npcID, zone)
    if not npc or not npcID or not zone then
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
                player = getSpecificPlayer(0)
            },
            {
                priority = NPC_GuardConfig.PRIORITIES.ATTACKING
            }
        )

        if success then
            idleChillingStates[npcID] = nil
            return true
        end
    end

    return false
end

local function onIdleUpdate()
    if not idleUpdateActive then
        return
    end

    local currentTime = getCurrentTime()
    local toComplete = {}

    for npcID, state in pairs(idleChillingStates) do
        local npc = state.npc

        if not NPC_BehaviorNPCRegistry.isValidNPC(npc) then
            table.insert(toComplete, {npc = npc, reason = "NPC invalid"})
        elseif not npc:isExistInTheWorld() then
            table.insert(toComplete, {npc = npc, reason = "NPC removed from world"})
        elseif npc:isDead() then
            table.insert(toComplete, {npc = npc, reason = "NPC died"})
        else
            if checkForZombiesAndTransitionToAttack(npc, npcID, state.zone) then
                table.insert(toComplete, {npc = npc, reason = "Transitioning to attack"})
            else
                local elapsed = currentTime - state.lastMoveTime

                if elapsed >= state.nextMoveInterval then
                    performRandomMove(npc, state.zone)

                    state.lastMoveTime = currentTime
                    state.nextMoveInterval = randomBetween(
                        NPC_GuardConfig.IDLE_CHILLING.INTERVAL_MIN,
                        NPC_GuardConfig.IDLE_CHILLING.INTERVAL_MAX
                    )
                end
            end
        end
    end

    for _, entry in ipairs(toComplete) do
        NPC_BehaviorController.completeBehaviorByID(entry.npc, "idlechilling", entry.reason)
    end

    local hasActiveIdleStates = false
    for _ in pairs(idleChillingStates) do
        hasActiveIdleStates = true
        break
    end

    if not hasActiveIdleStates and idleUpdateActive then
        Events.OnTick.Remove(onIdleUpdate)
        idleUpdateActive = false
    end
end

function NPC_Behavior_IdleChilling.execute(npc, params, player)
    if not npc then
        return false, "NPC is nil"
    end

    if not params then
        return false, "Params is nil"
    end

    if not params.zone then
        return false, "Zone is nil"
    end

    local npcID = NPC_BehaviorNPCRegistry.getNPCID(npc)
    if not npcID then
        return false, "Failed to get NPC ID"
    end

    local state = {
        npc = npc,
        zone = params.zone,
        lastMoveTime = getCurrentTime(),
        nextMoveInterval = randomBetween(
            NPC_GuardConfig.IDLE_CHILLING.INTERVAL_MIN,
            NPC_GuardConfig.IDLE_CHILLING.INTERVAL_MAX
        ),
        startTime = getCurrentTime()
    }

    idleChillingStates[npcID] = state

    if not idleUpdateActive then
        Events.OnTick.Add(onIdleUpdate)
        idleUpdateActive = true
    end

    if isClient() and not isServer() then
        sendClientCommand(NPC_GuardConfig.COMMANDS.IDLE_SYSTEM.MODULE,
                        NPC_GuardConfig.COMMANDS.IDLE_SYSTEM.ACTIVATE, {
            npcID = npcID,
            zoneID = params.zone.id
        })
    end

    return true, "IdleChilling behavior started"
end

function NPC_Behavior_IdleChilling.canExecute(npc, params)
    if not npc then
        return false
    end

    if not params or not params.zone then
        return false
    end

    return true
end

function NPC_Behavior_IdleChilling.cleanup(npc)
    if not npc then
        return
    end

    local npcID = NPC_BehaviorNPCRegistry.getNPCID(npc)
    if not npcID then
        return
    end

    idleChillingStates[npcID] = nil
end

function NPC_Behavior_IdleChilling.onComplete(npc, params, result)
    NPC_Behavior_IdleChilling.cleanup(npc)
end

function NPC_Behavior_IdleChilling.onFailed(npc, params, reason)
    NPC_Behavior_IdleChilling.cleanup(npc)
end

return NPC_Behavior_IdleChilling
