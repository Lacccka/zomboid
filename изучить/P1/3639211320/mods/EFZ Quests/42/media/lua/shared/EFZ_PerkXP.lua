local PerkXP = {}

local MAX_PERK_LEVEL = 10

local function getLevelFloorXp(perk, level)
    if level <= 0 then
        return 0
    end

    return perk:getTotalXpForLevel(level)
end

local function getCarryXp(currentXp, currentLevelFloor)
    if currentXp <= 0 then
        return 0
    end

    if currentLevelFloor > 0 and currentXp >= currentLevelFloor then
        return currentXp - currentLevelFloor
    end

    return currentXp
end

function PerkXP.levelUpPerkKeepingProgress(playerObj, perk)
    local beforeLevel = playerObj:getPerkLevel(perk)
    if beforeLevel >= MAX_PERK_LEVEL then
        return false
    end

    local xp = playerObj:getXp()
    local currentXp = xp:getXP(perk)
    local currentLevelFloor = getLevelFloorXp(perk, beforeLevel)
    local carryXp = getCarryXp(currentXp, currentLevelFloor)

    -- Raise the actual perk level first, then rebuild XP for the new level.
    playerObj:LevelPerk(perk, false)

    local afterLevel = playerObj:getPerkLevel(perk)
    if afterLevel <= beforeLevel then
        return false
    end

    xp:setXPToLevel(perk, afterLevel)

    if afterLevel < MAX_PERK_LEVEL and carryXp > 0 then
        xp:AddXPNoMultiplier(perk, carryXp)
    end

    return true
end

return PerkXP
