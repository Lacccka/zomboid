-- Internet Vehicle Radio - WIVK-FM proof of concept for Project Zomboid B42.
-- Vanilla DeviceData owns power/channel/volume. A client-only Leaf mixin adds
-- URL-stream methods to FMODSoundEmitter. The server never relays audio.

local MOD_TAG = "[LCC Internet Radio PoC]"
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

local activeStreams = {}
local retryAt = {}
local tickNumber = 0
local bridgeVersion = nil
local bridgeMissingLogged = false

local function log(message)
    print(MOD_TAG .. " " .. tostring(message))
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
        -- Preserve existing presets. Vanilla SetChannel synchronizes the choice.
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

local function emitterHasBridge(emitter)
    if not emitter or emitter.lccInternetRadioBridgeVersion == nil then
        if not bridgeMissingLogged then
            bridgeMissingLogged = true
            log("audio bridge is missing; install Leaf Loader and restart the client")
        end
        return false
    end
    if not bridgeVersion then
        bridgeVersion = tostring(emitter:lccInternetRadioBridgeVersion())
        log("detected audio bridge " .. bridgeVersion)
    end
    return true
end

local function stopStream(key, reason)
    local state = activeStreams[key]
    if not state then return end
    if state.emitter and state.emitter.lccStopInternetStream ~= nil then
        state.emitter:lccStopInternetStream(state.handle)
    end
    activeStreams[key] = nil
    if reason then log("stopped vehicle " .. key .. " (" .. reason .. ")") end
end

local function bridgeError(emitter)
    if emitter and emitter.lccInternetRadioBridgeLastError ~= nil then
        return tostring(emitter:lccInternetRadioBridgeLastError())
    end
    return "unknown bridge error"
end

local function startStream(vehicle, deviceData, key)
    if retryAt[key] and tickNumber < retryAt[key] then return end

    local emitter = vehicle:getEmitter()
    if not emitterHasBridge(emitter) then
        retryAt[key] = tickNumber + CONFIG.retryAfterTicks
        return
    end

    local handle = emitter:lccPlayInternetStream(
        CONFIG.streamUrl,
        CONFIG.minDistance,
        CONFIG.maxDistance,
        deviceData:getDeviceVolume()
    )
    if not handle or handle == 0 then
        retryAt[key] = tickNumber + CONFIG.retryAfterTicks
        log("stream start failed for vehicle " .. key .. ": " .. bridgeError(emitter))
        return
    end

    activeStreams[key] = {
        emitter = emitter,
        handle = handle,
        lastVerifiedAt = tickNumber,
    }
    retryAt[key] = nil
    log("started WIVK-FM for vehicle " .. key .. " (channel " .. tostring(handle) .. ")")
end

local function updateActiveStream(deviceData, key)
    local state = activeStreams[key]
    if not state then return end

    if not state.emitter:lccUpdateInternetStream(state.handle, deviceData:getDeviceVolume()) then
        stopStream(key, "FMOD channel update failed")
        retryAt[key] = tickNumber + CONFIG.retryAfterTicks
        return
    end

    if tickNumber - state.lastVerifiedAt < CONFIG.verifyEveryTicks then return end
    state.lastVerifiedAt = tickNumber
    if not state.emitter:lccIsInternetStreamPlaying(state.handle) then
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
        updateActiveStream(deviceData, key)
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

    -- B42 exposes a Java ArrayList here. Use only its concrete size()/get() API;
    -- the removed generic collection probing caused the earlier nil-call errors.
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
    log("client FMOD bridge mode; server carries only vanilla vehicle-radio state")
end)
Events.OnTick.Add(update)
if Events.OnGameExit then Events.OnGameExit.Add(stopAll) end
