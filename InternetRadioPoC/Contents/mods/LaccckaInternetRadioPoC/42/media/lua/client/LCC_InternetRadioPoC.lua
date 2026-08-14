-- Internet Vehicle Radio - WIVK-FM proof of concept for Project Zomboid B42.
-- Pure Workshop implementation: Lua binds PZ's already-loaded javafmod class.
-- No external loader, copied class, executable, or server-side audio transport.

local MOD_TAG = "[LCC Internet Radio PoC]"
local FMOD_CREATESTREAM = 128
local FMOD_3D = 16

local CONFIG = {
    stationUuid = "dea0ad58-9bd8-4a2c-b4e5-ca6f3714ae7e",
    stationName = "WIVK-FM",
    frequency = 104600,
    streamUrl = "https://playerservices.streamtheworld.com/api/livestream-redirect/WIVKFMAAC.aac",
    minDistance = 2,
    maxDistance = 60,
    updateEveryTicks = 15,
    verifyEveryTicks = 300,
    retryAfterTicks = 1800,
}

local javafmod = nil
local fmodBindingChecked = false
local fmodBindingAvailable = false
local activeStreams = {}
local retryAt = {}
local tickNumber = 0

local function log(message)
    print(MOD_TAG .. " " .. tostring(message))
end

local function initializeFmodBinding()
    if fmodBindingChecked then return fmodBindingAvailable end
    fmodBindingChecked = true

    if not luajava or not luajava.bindClass then
        log("luajava is unavailable; this B42 client cannot expose the built-in FMOD wrapper")
        return false
    end

    -- fmod.javafmod ships with Project Zomboid. Binding a game class does not
    -- load external code and requires no files outside this Workshop mod.
    javafmod = luajava.bindClass("fmod.javafmod")
    if not javafmod then
        log("could not bind the built-in fmod.javafmod class")
        return false
    end

    fmodBindingAvailable = true
    log("bound built-in fmod.javafmod; pure Workshop streaming path is ready")
    return true
end

local function vehicleKey(vehicle)
    local id = vehicle and vehicle:getId() or nil
    if id ~= nil then return tostring(id) end
    return tostring(vehicle)
end

local function getRadioData(vehicle)
    local part = vehicle and vehicle:getPartById("Radio") or nil
    if not part or not part:getInventoryItem() then return nil end
    return part:getDeviceData()
end

local function ensureStationPreset(deviceData)
    if not deviceData then return end
    local presets = deviceData:getDevicePresets()
    local entries = presets and presets:getPresets() or nil
    if not presets or not entries then return end

    local entryCount = entries:size()
    for index = 0, entryCount - 1 do
        local entry = entries:get(index)
        if entry and entry:getFrequency() == CONFIG.frequency then return end
    end

    if entryCount >= presets:getMaxPresets() then
        presets:setMaxPresets(entryCount + 1)
    end
    presets:addPreset(CONFIG.stationName, CONFIG.frequency)
    log("added local preset " .. CONFIG.stationName .. " on 104.6 MHz")
end

local function distanceSquared(player, vehicle)
    local dx = player:getX() - vehicle:getX()
    local dy = player:getY() - vehicle:getY()
    local dz = player:getZ() - vehicle:getZ()
    return dx * dx + dy * dy + dz * dz
end

local function setChannelPosition(channel, vehicle)
    javafmod.FMOD_Channel_Set3DAttributes(
        channel,
        vehicle:getX(),
        vehicle:getY(),
        vehicle:getZ() * 3,
        0, 0, 0
    )
end

local function releaseSound(sound)
    if sound and sound ~= 0 then
        javafmod.FMOD_Sound_Release(sound)
    end
end

local function stopStream(key, reason)
    local state = activeStreams[key]
    if not state then return end

    if state.channel and state.channel ~= 0 then
        javafmod.FMOD_Channel_Stop(state.channel)
    end
    releaseSound(state.sound)
    activeStreams[key] = nil

    if reason then log("stopped vehicle " .. key .. " (" .. reason .. ")") end
end

local function startStream(vehicle, deviceData, key)
    if retryAt[key] and tickNumber < retryAt[key] then return end
    if not initializeFmodBinding() then
        retryAt[key] = tickNumber + CONFIG.retryAfterTicks
        return
    end

    local sound = javafmod.FMOD_System_CreateSound(CONFIG.streamUrl, FMOD_CREATESTREAM)
    if not sound or sound == 0 then
        retryAt[key] = tickNumber + CONFIG.retryAfterTicks
        log("FMOD_System_CreateSound returned no stream for vehicle " .. key)
        return
    end

    local channel = javafmod.FMOD_System_PlaySound(sound, true)
    if not channel or channel == 0 then
        releaseSound(sound)
        retryAt[key] = tickNumber + CONFIG.retryAfterTicks
        log("FMOD_System_PlaySound returned no channel for vehicle " .. key)
        return
    end

    javafmod.FMOD_Channel_SetMode(channel, FMOD_3D)
    setChannelPosition(channel, vehicle)
    javafmod.FMOD_Channel_Set3DMinMaxDistance(channel, CONFIG.minDistance, CONFIG.maxDistance)
    javafmod.FMOD_Channel_SetVolume(channel, deviceData:getDeviceVolume())
    javafmod.FMOD_Channel_SetPaused(channel, false)

    activeStreams[key] = {
        channel = channel,
        sound = sound,
        lastVerifiedAt = tickNumber,
    }
    retryAt[key] = nil
    log("started WIVK-FM for vehicle " .. key .. " (FMOD channel " .. tostring(channel) .. ")")
end

local function updateActiveStream(vehicle, deviceData, key)
    local state = activeStreams[key]
    if not state then return end

    setChannelPosition(state.channel, vehicle)
    javafmod.FMOD_Channel_SetVolume(state.channel, deviceData:getDeviceVolume())

    if tickNumber - state.lastVerifiedAt < CONFIG.verifyEveryTicks then return end
    state.lastVerifiedAt = tickNumber
    if not javafmod.FMOD_Channel_IsPlaying(state.channel) then
        stopStream(key, "FMOD channel ended")
        retryAt[key] = tickNumber + CONFIG.retryAfterTicks
    end
end

local function shouldPlay(deviceData)
    return deviceData
        and deviceData:getIsTurnedOn()
        and deviceData:getChannel() == CONFIG.frequency
        and deviceData:getDeviceVolume() > 0
end

local function updateVehicle(player, vehicle, seen)
    local key = vehicleKey(vehicle)
    seen[key] = true
    local deviceData = getRadioData(vehicle)
    if deviceData then ensureStationPreset(deviceData) end

    if distanceSquared(player, vehicle) > CONFIG.maxDistance * CONFIG.maxDistance then
        stopStream(key, "out of range")
        return
    end
    if not shouldPlay(deviceData) then
        stopStream(key, "radio off, muted, or tuned away")
        return
    end

    if activeStreams[key] then
        updateActiveStream(vehicle, deviceData, key)
    else
        startStream(vehicle, deviceData, key)
    end
end

local function update()
    tickNumber = tickNumber + 1
    if tickNumber % CONFIG.updateEveryTicks ~= 0 then return end

    local player = getPlayer()
    local cell = getCell()
    local vehicles = cell and cell:getVehicles() or nil
    if not player or not vehicles then return end

    local seen = {}
    for index = 0, vehicles:size() - 1 do
        local vehicle = vehicles:get(index)
        if vehicle then updateVehicle(player, vehicle, seen) end
    end

    local unloaded = {}
    for key, _ in pairs(activeStreams) do
        if not seen[key] then unloaded[#unloaded + 1] = key end
    end
    for _, key in ipairs(unloaded) do stopStream(key, "vehicle unloaded") end
end

local function stopAll()
    local keys = {}
    for key, _ in pairs(activeStreams) do keys[#keys + 1] = key end
    for _, key in ipairs(keys) do stopStream(key, "session ended") end
end

Events.OnGameStart.Add(function()
    log("loaded; WIVK-FM=104.6 MHz, UUID=" .. CONFIG.stationUuid)
    log("pure Workshop mode; no Leaf, Java mod loader, or manual client installation")
    initializeFmodBinding()
end)
Events.OnTick.Add(update)
if Events.OnGameExit then Events.OnGameExit.Add(stopAll) end
