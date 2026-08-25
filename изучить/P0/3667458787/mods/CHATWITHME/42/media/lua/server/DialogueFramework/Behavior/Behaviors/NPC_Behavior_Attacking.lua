if isServer() then
    local NPC_Behavior_Attacking = {}

    local NPC_GuardConfig = require("DialogueFramework/Behavior/NPC_GuardConfig")
    local NPC_AttackConfig = require("DialogueFramework/Behavior/NPC_AttackConfig")
    local NPC_AttackExecutor = require("DialogueFramework/Behavior/NPC_AttackExecutor")
    local NPC_BehaviorNPCRegistry = require("DialogueFramework/Behavior/NPC_BehaviorNPCRegistry")
    local NPC_BehaviorController = require("DialogueFramework/Behavior/NPC_BehaviorController")
    local MUGGY_ZoneDefinitions = require("MuggyMod/MUGGY_ZoneDefinitions")

    local activeAttacks = {}

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

    local function canAttackNow(attackState)
        if not attackState then
            return false
        end

        local currentTime = getTimestampMs() / 1000.0
        local elapsed = currentTime - attackState.lastAttackTime

        return elapsed >= attackState.currentCooldown
    end

    local function performAttack(npc, npcID, attackState)
        if not npc or not npcID or not attackState then
            return false
        end

        if not canAttackNow(attackState) then
            return false
        end

        local zombies = scanForZombiesInZone(attackState.zone)

        if #zombies == 0 then
            NPC_BehaviorController.completeBehaviorByID(npc, "attacking", "All zombies eliminated")

            NPC_BehaviorController.queueBehavior(
                npc,
                "idlechilling",
                {
                    zone = attackState.zone
                },
                {
                    priority = NPC_GuardConfig.PRIORITIES.IDLE_CHILLING
                }
            )

            return false
        end

        local target, distance = NPC_AttackExecutor.selectNearestTarget(
            npc,
            zombies,
            attackState.attackedTargets
        )

        if not target then
            NPC_BehaviorController.completeBehaviorByID(npc, "attacking", "No valid targets")

            NPC_BehaviorController.queueBehavior(
                npc,
                "idlechilling",
                {
                    zone = attackState.zone
                },
                {
                    priority = NPC_GuardConfig.PRIORITIES.IDLE_CHILLING
                }
            )

            return false
        end

        local success, message = NPC_AttackExecutor.executeAttackSequence(
            npc,
            target,
            attackState.attackConfig,
            function(damageSuccess, damageOrReason)
                if damageSuccess then
                    attackState.lastAttackTime = getTimestampMs() / 1000.0
                    attackState.currentCooldown = NPC_AttackConfig.calculateCooldown(attackState.attackConfig)

                    local targetID = target:getOnlineID()
                    if targetID then
                        table.insert(attackState.attackedTargets, targetID)
                    end

                    if target:isDead() then
                        if isClient() and not isServer() then
                            sendClientCommand(NPC_GuardConfig.COMMANDS.ATTACK_SYSTEM.MODULE,
                                            NPC_GuardConfig.COMMANDS.ATTACK_SYSTEM.TARGET_ELIMINATED, {
                                npcID = npcID,
                                targetID = targetID
                            })
                        end
                    end
                else
                    attackState.lastAttackTime = getTimestampMs() / 1000.0
                    attackState.currentCooldown = 1.0
                end
            end
        )

        return success
    end

    local function onAttackUpdate()
        local toComplete = {}

        for npcID, attackState in pairs(activeAttacks) do
            local npc = attackState.npc

            if not NPC_BehaviorNPCRegistry.isValidNPC(npc) then
                table.insert(toComplete, {npc = npc, reason = "NPC invalid"})
            elseif not npc:isExistInTheWorld() then
                table.insert(toComplete, {npc = npc, reason = "NPC removed from world"})
            elseif npc:isDead() then
                table.insert(toComplete, {npc = npc, reason = "NPC died"})
            else
                performAttack(npc, npcID, attackState)
            end
        end

        for _, entry in ipairs(toComplete) do
            NPC_BehaviorController.completeBehaviorByID(entry.npc, "attacking", entry.reason)
        end

        local hasActiveAttacks = false
        for _ in pairs(activeAttacks) do
            hasActiveAttacks = true
            break
        end

        if not hasActiveAttacks then
            Events.EveryTenMinutes.Remove(onAttackUpdate)
        end
    end

    function NPC_Behavior_Attacking.execute(npc, params, player)
        if not npc then
            return false, "NPC is nil"
        end

        if not params then
            return false, "Params is nil"
        end

        if not params.zone then
            return false, "Zone is nil"
        end

        if not params.attackConfig then
            return false, "Attack config is nil"
        end

        local npcID = NPC_BehaviorNPCRegistry.getNPCID(npc)
        if not npcID then
            return false, "Failed to get NPC ID"
        end

        activeAttacks[npcID] = {
            npc = npc,
            zone = params.zone,
            attackConfig = params.attackConfig,
            lastAttackTime = 0,
            currentCooldown = 0,
            attackedTargets = {},
            startTime = getTimestampMs() / 1000.0
        }

        local hasHandler = false
        for _ in pairs(activeAttacks) do
            hasHandler = true
            break
        end

        if hasHandler and not Events.EveryTenMinutes.contains(onAttackUpdate) then
            Events.EveryTenMinutes.Add(onAttackUpdate)
        end

        performAttack(npc, npcID, activeAttacks[npcID])

        if isClient() and not isServer() then
            sendClientCommand(NPC_GuardConfig.COMMANDS.ATTACK_SYSTEM.MODULE,
                            NPC_GuardConfig.COMMANDS.ATTACK_SYSTEM.INITIATE, {
                npcID = npcID,
                zoneID = params.zone.id
            })
        end

        return true, "Attacking behavior started"
    end

    function NPC_Behavior_Attacking.canExecute(npc, params)
        if not npc then
            return false
        end

        if not params or not params.zone or not params.attackConfig then
            return false
        end

        return true
    end

    function NPC_Behavior_Attacking.cleanup(npc)
        if not npc then
            return
        end

        local npcID = NPC_BehaviorNPCRegistry.getNPCID(npc)
        if not npcID then
            return
        end

        activeAttacks[npcID] = nil
    end

    function NPC_Behavior_Attacking.onComplete(npc, params, result)
        NPC_Behavior_Attacking.cleanup(npc)
    end

    function NPC_Behavior_Attacking.onFailed(npc, params, reason)
        NPC_Behavior_Attacking.cleanup(npc)
    end

    return NPC_Behavior_Attacking
end
