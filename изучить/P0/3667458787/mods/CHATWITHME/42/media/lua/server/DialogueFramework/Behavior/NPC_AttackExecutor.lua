if isServer() then
    local NPC_AttackExecutor = {}

    local NPC_AttackConfig = require("DialogueFramework/Behavior/NPC_AttackConfig")
    local Muggy_VariableBridge = require("MuggyMod/Muggy_VariableBridge")

    local function calculateDistance(npc, target)
        if not npc or not target then
            return 999999
        end

        local npcX = npc:getX()
        local npcY = npc:getY()
        local targetX = target:getX()
        local targetY = target:getY()

        if not npcX or not npcY or not targetX or not targetY then
            return 999999
        end

        return math.sqrt((npcX - targetX) ^ 2 + (npcY - targetY) ^ 2)
    end

    local function triggerAttackAnimation(npc, animationName)
        if not npc then
            return false, 1.0
        end

        if not animationName then
            return false, 1.0
        end

        local success, error = pcall(function()
        end)

        return true, 1.0
    end

    local function schedulePostAnimationDamage(npc, target, attackConfig, callback)
        if not npc or not target or not attackConfig then
            return false
        end

        local animDuration = attackConfig.animationEstimatedDuration or 1.0

        local timerID = "attack_" .. tostring(npc:getOnlineID()) .. "_" .. tostring(getTimestampMs())

        local success, error = pcall(function()
            local startTime = getTimestampMs() / 1000.0

            local checkCallback = function()
                local currentTime = getTimestampMs() / 1000.0
                local elapsed = currentTime - startTime

                if elapsed >= animDuration then
                    if callback then
                        callback()
                    end
                    return true
                end

                return false
            end

            local maxChecks = math.ceil(animDuration * 10) + 5
            local checkCount = 0

            local periodicCheck = nil
            periodicCheck = function()
                checkCount = checkCount + 1

                if checkCallback() then
                    Events.OnTick.Remove(periodicCheck)
                    return
                end

                if checkCount >= maxChecks then
                    if callback then
                        callback()
                    end
                    Events.OnTick.Remove(periodicCheck)
                    return
                end
            end

            Events.OnTick.Add(periodicCheck)
        end)

        return success
    end

    function NPC_AttackExecutor.executeAttackSequence(npc, target, attackConfig, onComplete)
        if not npc then
            return false, "NPC is nil"
        end

        if not target then
            return false, "Target is nil"
        end

        if not attackConfig then
            return false, "Attack config is nil"
        end

        if not NPC_AttackConfig.isValidTarget(target, attackConfig) then
            return false, "Invalid target"
        end

        local preDistance = calculateDistance(npc, target)
        if preDistance > attackConfig.attackRange then
            return false, "Target out of range (pre-check)"
        end

        local animSuccess, animDuration = triggerAttackAnimation(npc, attackConfig.animationName)

        Muggy_VariableBridge.setMuggyAttacking(npc, true)

        schedulePostAnimationDamage(npc, target, attackConfig, function()
            if not target or target:isDead() then
                Muggy_VariableBridge.setMuggyAttacking(npc, false)
                if onComplete then
                    onComplete(false, "Target died during animation")
                end
                return
            end

            local postDistance = calculateDistance(npc, target)
            if postDistance > attackConfig.attackRange then
                Muggy_VariableBridge.setMuggyAttacking(npc, false)
                if onComplete then
                    onComplete(false, "Target out of range (post-check)")
                end
                return
            end

            local damage = NPC_AttackConfig.calculateDamage(attackConfig)

            local success, error = pcall(function()
                local currentHealth = target:getHealth()
                if currentHealth <= damage then
                    target:Kill(npc)
                else
                    target:setHealth(currentHealth - damage)
                end
            end)

            Muggy_VariableBridge.setMuggyAttacking(npc, false)

            if not success then
                if onComplete then
                    onComplete(false, "Damage dealing failed")
                end
                return
            end

            if onComplete then
                onComplete(true, damage)
            end
        end)

        return true, "Attack sequence initiated"
    end

    function NPC_AttackExecutor.selectNearestTarget(npc, zombiesInZone, attackedTargets)
        if not npc or not zombiesInZone then
            return nil, nil
        end

        local npcX = npc:getX()
        local npcY = npc:getY()

        if not npcX or not npcY then
            return nil, nil
        end

        local nearestZombie = nil
        local nearestDistance = 999999

        for _, zombie in ipairs(zombiesInZone) do
            if zombie and not zombie:isDead() then
                local zombieID = zombie:getOnlineID()

                local alreadyAttacked = false
                if attackedTargets then
                    for _, targetID in ipairs(attackedTargets) do
                        if targetID == zombieID then
                            alreadyAttacked = true
                            break
                        end
                    end
                end

                if not alreadyAttacked then
                    local zx = zombie:getX()
                    local zy = zombie:getY()

                    if zx and zy then
                        local distance = math.sqrt((npcX - zx) ^ 2 + (npcY - zy) ^ 2)
                        if distance < nearestDistance then
                            nearestZombie = zombie
                            nearestDistance = distance
                        end
                    end
                end
            end
        end

        return nearestZombie, nearestDistance
    end

    return NPC_AttackExecutor
end
