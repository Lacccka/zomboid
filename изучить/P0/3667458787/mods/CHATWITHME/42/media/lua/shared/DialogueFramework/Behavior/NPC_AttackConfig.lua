local NPC_AttackConfig = {}

NPC_AttackConfig.MUGGY = {
    damageRange = {
        min = 0.5,
        max = 3.0
    },
    attackRange = 6.0,
    multiHit = 1,
    attackSpeed = {
        baseMin = 1.5,
        baseMax = 3.0,
        modifier = 1.0
    },
    targetTypes = {"IsoZombie"},
    animationName = "attack",
    animationEstimatedDuration = 1.0
}

function NPC_AttackConfig.getNPCAttackConfig(npcID)
    if not npcID then
        return NPC_AttackConfig.MUGGY
    end

    return NPC_AttackConfig.MUGGY
end

function NPC_AttackConfig.calculateCooldown(attackConfig)
    if not attackConfig then
        return 2.0
    end

    if not attackConfig.attackSpeed then
        return 2.0
    end

    local baseMin = attackConfig.attackSpeed.baseMin or 1.5
    local baseMax = attackConfig.attackSpeed.baseMax or 3.0
    local modifier = attackConfig.attackSpeed.modifier or 1.0

    local baseTime = baseMin + (ZombRand(0, 100) / 100.0) * (baseMax - baseMin)

    return baseTime * modifier
end

function NPC_AttackConfig.calculateDamage(attackConfig)
    if not attackConfig then
        return 1.0
    end

    if not attackConfig.damageRange then
        return 1.0
    end

    local min = attackConfig.damageRange.min or 0.5
    local max = attackConfig.damageRange.max or 3.0

    local minInt = math.floor(min * 100)
    local maxInt = math.floor(max * 100)

    local damage = ZombRand(minInt, maxInt + 1) / 100.0

    return damage
end

function NPC_AttackConfig.isValidTarget(target, attackConfig)
    if not target then
        return false
    end

    if target:isDead() then
        return false
    end

    if not attackConfig then
        return false
    end

    if not attackConfig.targetTypes then
        return false
    end

    for _, targetType in ipairs(attackConfig.targetTypes) do
        if instanceof(target, targetType) then
            return true
        end
    end

    return false
end

return NPC_AttackConfig
