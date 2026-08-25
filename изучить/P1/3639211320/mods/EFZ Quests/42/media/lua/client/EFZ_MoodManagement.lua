if not EFZ then
    EFZ = {}
end

EFZ.MoodManagement = EFZ.MoodManagement or {}
local Mood = EFZ.MoodManagement

Mood.config = Mood.config or {
    intervalMinutes = 10,
    multiplier = 0.5,
}

Mood._minuteAccumulator = Mood._minuteAccumulator or 0
Mood._tickerInstalled = Mood._tickerInstalled or false
Mood._warnedMissingBoredomAccessor = Mood._warnedMissingBoredomAccessor or false
Mood._warnedMissingTickerEvent = Mood._warnedMissingTickerEvent or false
Mood._warnedMissingBoredomStatToken = Mood._warnedMissingBoredomStatToken or false

local function isIsoPlayer(obj)
    return obj ~= nil and instanceof(obj, "IsoPlayer")
end

local function getActiveLocalPlayers()
    local players = {}
    local numPlayers = getNumActivePlayers()
    for idx = 0, numPlayers - 1 do
        local playerObj = getSpecificPlayer(idx)
        if isIsoPlayer(playerObj) and (not playerObj:isDead()) then
            players[#players + 1] = playerObj
        end
    end

    if #players == 0 then
        local playerObj = getPlayer()
        if isIsoPlayer(playerObj) and (not playerObj:isDead()) then
            players[#players + 1] = playerObj
        end
    end
    return players
end

local function isDeployInProgress(playerObj)
    if not isIsoPlayer(playerObj) then
        return false
    end

    -- Prefer the interface exposed by EFZ_Deploy.lua.
    if EFZ and type(EFZ.IsDeployInProgress) == "function" then
        return EFZ.IsDeployInProgress(playerObj) == true
    end

    -- Fallback for load-order cases where the function is not exposed yet.
    local playerNum = playerObj:getPlayerNum()
    if EFZ and EFZ.Deploy and type(EFZ.Deploy.activePlayers) == "table" then
        if EFZ.Deploy.activePlayers[playerNum] ~= nil then
            return true
        end
    end

    local modData = playerObj:getModData()
    local saved = modData.EFZDeploy
    return saved and saved.active == true
end

local function getBoredomAccessor(playerObj)
    if not isIsoPlayer(playerObj) then
        return nil, nil
    end

    local stats = playerObj:getStats()
    if stats and type(stats.get) == "function" and type(stats.set) == "function" then
        local boredomStat = CharacterStat and CharacterStat.BOREDOM or nil
        if boredomStat ~= nil then
            return function()
                return tonumber(stats:get(boredomStat))
            end, function(v)
                stats:set(boredomStat, v)
            end
        end
        if not Mood._warnedMissingBoredomStatToken then
            print("[EFZ][MoodManagement] CharacterStat.BOREDOM is not available in this build.")
            Mood._warnedMissingBoredomStatToken = true
        end
    end

    if stats and type(stats.getBoredom) == "function" and type(stats.setBoredom) == "function" then
        return function()
            return tonumber(stats:getBoredom())
        end, function(v)
            stats:setBoredom(v)
        end
    end

    return nil, nil
end

local function clampNonNegative(n)
    n = tonumber(n)
    if n == nil then
        return nil
    end
    if n < 0 then
        return 0
    end
    return n
end

local function applyBoredomDecayForPlayer(playerObj)
    if not isIsoPlayer(playerObj) or playerObj:isDead() then
        return
    end
    if isDeployInProgress(playerObj) then
        return
    end

    local getter, setter = getBoredomAccessor(playerObj)
    if not getter or not setter then
        if not Mood._warnedMissingBoredomAccessor then
            print("[EFZ][MoodManagement] Boredom accessor not found for the current build.")
            Mood._warnedMissingBoredomAccessor = true
        end
        return
    end

    local current = getter()
    current = clampNonNegative(current)
    if current == nil then
        return
    end

    local mult = tonumber(Mood.config and Mood.config.multiplier) or 0.5
    local nextValue = current * mult
    nextValue = clampNonNegative(nextValue)
    if nextValue == nil then
        return
    end

    setter(nextValue)
end

local function onTenMinuteTick()
    local players = getActiveLocalPlayers()
    for _, playerObj in ipairs(players) do
        applyBoredomDecayForPlayer(playerObj)
    end
end

local function getIntervalMinutes()
    local interval = tonumber(Mood.config and Mood.config.intervalMinutes) or 10
    if interval < 1 then
        interval = 10
    end
    return math.floor(interval)
end

local function onMinuteStep(stepMinutes)
    Mood._minuteAccumulator = (Mood._minuteAccumulator or 0) + stepMinutes
    local interval = getIntervalMinutes()
    while Mood._minuteAccumulator >= interval do
        Mood._minuteAccumulator = Mood._minuteAccumulator - interval
        onTenMinuteTick()
    end
end

local function installTicker()
    if Mood._tickerInstalled then
        return
    end
    Mood._tickerInstalled = true

    if Events and Events.EveryOneMinute and type(Events.EveryOneMinute.Add) == "function" then
        Events.EveryOneMinute.Add(function()
            onMinuteStep(1)
        end)
    elseif Events and Events.EveryTenMinutes and type(Events.EveryTenMinutes.Add) == "function" then
        Events.EveryTenMinutes.Add(function()
            onMinuteStep(10)
        end)
    elseif not Mood._warnedMissingTickerEvent then
        print("[EFZ][MoodManagement] No supported timer event found (EveryOneMinute/EveryTenMinutes).")
        Mood._warnedMissingTickerEvent = true
    end
end

if Events and Events.OnGameStart and Events.OnGameStart.Add then
    Events.OnGameStart.Add(installTicker)
end

installTicker()
