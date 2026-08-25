local MUGGY_EnvironmentDetector = {}

local environmentCache = {
    detected = false,
    isSinglePlayer = false,
    isMultiplayer = false,
    detectionTime = 0,
    detectionTrigger = nil
}

local function detectEnvironmentNow()
    if environmentCache.detected then
        return
    end

    local success, result = pcall(function()
        return isMultiplayer()
    end)

    if not success then
        environmentCache.isSinglePlayer = true
        environmentCache.isMultiplayer = false
    else
        environmentCache.isMultiplayer = result or false
        environmentCache.isSinglePlayer = not environmentCache.isMultiplayer
    end

    environmentCache.detected = true
    environmentCache.detectionTime = getTimestamp()

    if environmentCache.detectionTrigger == nil then
        environmentCache.detectionTrigger = "lazy"
    end
end

local function onPlayerMove(player)
    if environmentCache.detected and environmentCache.detectionTrigger == "onplayermove" then
        return
    end

    local success, result = pcall(function()
        return isMultiplayer()
    end)

    if not success then
        environmentCache.isSinglePlayer = true
        environmentCache.isMultiplayer = false
    else
        environmentCache.isMultiplayer = result or false
        environmentCache.isSinglePlayer = not environmentCache.isMultiplayer
    end

    environmentCache.detected = true
    environmentCache.detectionTrigger = "onplayermove"
    environmentCache.detectionTime = getTimestamp()

    print("[MUGGY_EnvironmentDetector] Environment detected (OnPlayerMove): " ..
          (environmentCache.isSinglePlayer and "SP" or "MP"))

    if Events and Events.MUGGY_EnvironmentDetected then
        Events.MUGGY_EnvironmentDetected.Trigger({
            isSinglePlayer = environmentCache.isSinglePlayer,
            isMultiplayer = environmentCache.isMultiplayer,
            detectionTrigger = "onplayermove",
            timestamp = environmentCache.detectionTime
        })
    end

    if Events and Events.OnPlayerMove then
        Events.OnPlayerMove.Remove(onPlayerMove)
    end
end

function MUGGY_EnvironmentDetector.isSinglePlayer()
    if not environmentCache.detected then
        detectEnvironmentNow()
    end
    return environmentCache.isSinglePlayer
end

function MUGGY_EnvironmentDetector.isMultiplayer()
    if not environmentCache.detected then
        detectEnvironmentNow()
    end
    return environmentCache.isMultiplayer
end

function MUGGY_EnvironmentDetector.getEnvironmentInfo()
    if not environmentCache.detected then
        detectEnvironmentNow()
    end

    return {
        detected = environmentCache.detected,
        isSinglePlayer = environmentCache.isSinglePlayer,
        isMultiplayer = environmentCache.isMultiplayer,
        detectionTrigger = environmentCache.detectionTrigger,
        detectionTime = environmentCache.detectionTime
    }
end

if Events and Events.OnPlayerMove then
    Events.OnPlayerMove.Add(onPlayerMove)
end

return MUGGY_EnvironmentDetector
