local NPC_MuggyIdleConditions = {}

local REUNION_COOLDOWN_HOURS = 6

function NPC_MuggyIdleConditions.getLastSeenTime(player)
    if not player then
        return nil
    end

    local modData = player:getModData()
    if not modData.muggy_last_seen then
        return nil
    end

    return modData.muggy_last_seen
end

function NPC_MuggyIdleConditions.setLastSeenTime(player, worldAge)
    if not player then
        return false
    end

    local modData = player:getModData()
    modData.muggy_last_seen = worldAge
    return true
end

function NPC_MuggyIdleConditions.canPlayReunionIdle(player, npc)
    if not player then
        return false
    end

    local lastSeen = NPC_MuggyIdleConditions.getLastSeenTime(player)

    if not lastSeen then
        return false
    end

    local currentWorldAge = getGameTime():getWorldAgeHours()
    local timeSinceLastSeen = currentWorldAge - lastSeen

    return timeSinceLastSeen >= REUNION_COOLDOWN_HOURS
end

return NPC_MuggyIdleConditions
