require "PPO_Config"
require "PPO_BonusMath"
require "PPO_ReadinessMath"
require "PPO_ExerciseState"

PPO = PPO or {}
PPO.ReadinessEngine = PPO.ReadinessEngine or {}

local ReadinessEngine = PPO.ReadinessEngine

local function settings()
    return PPO.Config.resolve("getRecoverySettings", PPO.Config.Recovery)
end

-- A missing or throwing seam returns nil, which ramps to zero penalty.
function ReadinessEngine.stat(character, statName)
    if character == nil then return nil end
    local ok, value = pcall(function()
        return character:getStats():get(CharacterStat[statName])
    end)
    if not ok then return nil end
    if type(value) ~= "number" then return nil end
    return value
end

-- Sleeping is never mandatory: an unreadable option is treated as "sleep not
-- required", so a failure can only remove a penalty.
function ReadinessEngine.sleepRequired(resolved)
    local mode = resolved.SleepInfluence
    if mode == "Ignore" then return false end

    local isMultiplayer = false
    local clientOk, client = pcall(isClient)
    if clientOk and client == true then isMultiplayer = true end
    local serverOk, server = pcall(isServer)
    if serverOk and server == true then isMultiplayer = true end
    if not isMultiplayer then return true end

    local ok, allowed = pcall(function()
        return getServerOptions():getBoolean("SleepAllowed") == true
            and getServerOptions():getBoolean("SleepNeeded") == true
    end)
    if not ok then return false end
    return allowed == true
end

function ReadinessEngine.evaluate(character, direction)
    local resolved = settings()
    local result = {
        readiness = 1,
        recoverySupport = 1,
        loadReturn = 1,
        sleepFactor = 1,
        sleepShare = 1,
        fuelFactor = 1,
        sleepRequired = true,
    }
    if character == nil then return result end

    local loadReturn = 1
    local componentOk, component = pcall(
        PPO.ExerciseState.getComponent, character, direction)
    if componentOk and component ~= nil then
        local ok, value = pcall(PPO.BonusMath.bonusReturn,
            component.dailyStimulus, true)
        if ok then loadReturn = value end
    end

    -- Resolved once and published, because the multiplier has to know whether
    -- the sleep share can be earned at all before it weighs it.
    local sleepRequired = ReadinessEngine.sleepRequired(resolved)
    local sleepFactor = PPO.ReadinessMath.sleepFactor(
        ReadinessEngine.stat(character, "FATIGUE"),
        sleepRequired,
        resolved)
    local fuelFactor = PPO.ReadinessMath.fuelFactor(
        ReadinessEngine.stat(character, "HUNGER"),
        ReadinessEngine.stat(character, "THIRST"),
        resolved)

    result.loadReturn = loadReturn
    result.sleepFactor = sleepFactor
    result.sleepShare = PPO.ReadinessMath.sleepShare(
        ReadinessEngine.stat(character, "FATIGUE"),
        sleepRequired,
        resolved)
    result.fuelFactor = fuelFactor
    result.fuelShare = PPO.ReadinessMath.fuelShare(
        ReadinessEngine.stat(character, "HUNGER"),
        ReadinessEngine.stat(character, "THIRST"))
    result.sleepRequired = sleepRequired == true
    result.readiness = PPO.ReadinessMath.readiness(
        PPO.ReadinessMath.loadFactor(loadReturn),
        sleepFactor, fuelFactor, resolved)
    result.recoverySupport = PPO.ReadinessMath.recoverySupport(
        sleepFactor, fuelFactor, resolved)
    return result
end
