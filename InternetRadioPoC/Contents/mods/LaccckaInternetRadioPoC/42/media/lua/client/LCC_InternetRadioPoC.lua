-- Internet Vehicle Radio - WIVK-FM proof of concept for Project Zomboid B42.
--
-- This intentionally tests the shortest possible path first:
--     HTTPS AAC URL -> BaseVehicle emitter -> 3D FMOD sound
--
-- Vanilla vehicle DeviceData remains authoritative for power, channel and volume.
-- No audio is proxied through the dedicated server.

local MOD_TAG = "[LCC Internet Radio PoC]"

local CONFIG = {
    stationUuid = "dea0ad58-9bd8-4a2c-b4e5-ca6f3714ae7e",
    stationName = "WIVK-FM",
    frequency = 104600,
    streamUrl = "https://playerservices.streamtheworld.com/api/livestream-redirect/WIVKFMAAC.aac",
    maxDistance = 60,
    scanSquaresPerUpdate = 2048,
    updateEveryTicks = 15,
    verifyAfterTicks = 900,
    retryAfterTicks = 3600,
}

local activeStreams = {}
local retryAt = {}
local tickNumber = 0
local knownVehicles = {}
local scanCursor = 1

local scanOffsets = {}
for dy = -CONFIG.maxDistance, CONFIG.maxDistance do
    for dx = -CONFIG.maxDistance, CONFIG.maxDistance do
        local distance = dx * dx + dy * dy
        if distance <= CONFIG.maxDistance * CONFIG.maxDistance then
            scanOffsets[#scanOffsets + 1] = {
                x = dx,
                y = dy,
                distance = distance,
            }
        end
    end
end
table.sort(scanOffsets, function(a, b)
    return a.distance < b.distance
end)

local function log(message)
    print(MOD_TAG .. " " .. tostring(message))
end

local function vehicleKey(vehicle)
    local ok, id = pcall(function()
        return vehicle:getId()
    end)
    if ok and id ~= nil then
        return tostring(id)
    end
    return tostring(vehicle)
end

local function getRadioData(vehicle)
    local part = vehicle and vehicle:getPartById("Radio") or nil
    if not part or not part:getInventoryItem() then
        return nil
    end
    return part:getDeviceData()
end

local function ensureStationPreset(deviceData)
    if not deviceData then
        return
    end

    local presets = deviceData:getDevicePresets()
    local entries = presets and presets:getPresets() or nil
    if not presets or not entries then
        return
    end

    local entryCount = entries:size()
    for index = 0, entryCount - 1 do
        local entry = entries:get(index)
        if entry and entry:getFrequency() == CONFIG.frequency then
            return
        end
    end

    if entryCount >= presets:getMaxPresets() then
        -- Preserve every existing preset. This max-count change stays local because
        -- the PoC deliberately never calls transmitPresets().
        presets:setMaxPresets(entryCount + 1)
    end

    -- Client-only UI hint. Do not call transmitPresets(): vanilla SetChannel is
    -- already synchronized when the player tunes the radio to this preset.
    presets:addPreset(CONFIG.stationName, CONFIG.frequency)
    log("added local preset " .. CONFIG.stationName .. " on 104.6 MHz")
end

local function rememberVehicle(vehicle)
    if vehicle then
        knownVehicles[vehicleKey(vehicle)] = vehicle
    end
end

local function scanNearbySquares(cell, player)
    rememberVehicle(player:getVehicle())

    local centerX = math.floor(player:getX())
    local centerY = math.floor(player:getY())
    local centerZ = math.floor(player:getZ())
    local scanCount = math.min(CONFIG.scanSquaresPerUpdate, #scanOffsets)

    for _ = 1, scanCount do
        local offset = scanOffsets[scanCursor]
        local square = cell:getGridSquare(centerX + offset.x, centerY + offset.y, centerZ)
        if square then
            rememberVehicle(square:getVehicleContainer())
        end

        scanCursor = scanCursor + 1
        if scanCursor > #scanOffsets then
            scanCursor = 1
        end
    end
end

local function distanceSquared(player, vehicle)
    local dx = player:getX() - vehicle:getX()
    local dy = player:getY() - vehicle:getY()
    local dz = player:getZ() - vehicle:getZ()
    return dx * dx + dy * dy + dz * dz
end

local function stopStream(key, reason)
    local state = activeStreams[key]
    if not state then
        return
    end

    if state.emitter and state.handle and state.handle ~= 0 then
        local ok, err = pcall(function()
            state.emitter:stopSound(state.handle)
        end)
        if not ok then
            log("stop failed for vehicle " .. key .. ": " .. tostring(err))
        end
    end

    activeStreams[key] = nil
    if reason then
        log("stopped vehicle " .. key .. " (" .. reason .. ")")
    end
end

local function startStream(vehicle, deviceData, key)
    if retryAt[key] and tickNumber < retryAt[key] then
        return
    end

    local emitter = vehicle:getEmitter()
    if not emitter then
        retryAt[key] = tickNumber + CONFIG.retryAfterTicks
        log("vehicle " .. key .. " has no sound emitter")
        return
    end

    local ok, handleOrError = pcall(function()
        -- Core PoC call. B42 may reject this because the public emitter API
        -- normally resolves registered local GameSound resources, not URLs.
        return emitter:playSound(CONFIG.streamUrl)
    end)

    if not ok then
        retryAt[key] = tickNumber + CONFIG.retryAfterTicks
        log("direct HTTPS AAC call threw for vehicle " .. key .. ": " .. tostring(handleOrError))
        return
    end

    local handle = handleOrError
    if not handle or handle == 0 then
        retryAt[key] = tickNumber + CONFIG.retryAfterTicks
        log("direct HTTPS AAC call returned no handle for vehicle " .. key .. "; B42 direct streaming is unavailable on this client")
        return
    end

    pcall(function()
        emitter:set3D(handle, true)
        emitter:setVolume(handle, deviceData:getDeviceVolume())
    end)

    activeStreams[key] = {
        emitter = emitter,
        handle = handle,
        startedAt = tickNumber,
        verified = false,
    }
    retryAt[key] = nil
    log("FMOD accepted the stream request for vehicle " .. key .. "; waiting for playback verification")
end

local function updateActiveStream(deviceData, key)
    local state = activeStreams[key]
    if not state then
        return
    end

    pcall(function()
        state.emitter:setVolume(state.handle, deviceData:getDeviceVolume())
    end)

    local shouldVerify = state.verified or tickNumber - state.startedAt >= CONFIG.verifyAfterTicks
    if not shouldVerify then
        return
    end

    local ok, playing = pcall(function()
        return state.emitter:isPlaying(state.handle)
    end)

    if ok and playing then
        if not state.verified then
            state.verified = true
            log("FMOD reports the HTTPS handle as playing for vehicle " .. key .. "; confirm audible AAC in-game")
        end
        return
    end

    stopStream(key, "FMOD handle is not playing")
    retryAt[key] = tickNumber + CONFIG.retryAfterTicks
    log("RESULT: direct HTTPS AAC was not playable; a client streaming/decoder bridge is required")
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
    if deviceData then
        ensureStationPreset(deviceData)
    end

    local inRange = distanceSquared(player, vehicle) <= CONFIG.maxDistance * CONFIG.maxDistance
    if not inRange then
        stopStream(key, "out of range")
        return
    end

    if not shouldPlay(deviceData) then
        stopStream(key, "radio off, muted, or tuned away")
        return
    end

    if not activeStreams[key] then
        startStream(vehicle, deviceData, key)
    else
        updateActiveStream(deviceData, key)
    end
end

local function update()
    tickNumber = tickNumber + 1
    if tickNumber % CONFIG.updateEveryTicks ~= 0 then
        return
    end

    local player = getPlayer()
    local cell = getCell()
    if not player or not cell then
        return
    end

    scanNearbySquares(cell, player)

    local seen = {}
    local forgotten = {}
    for key, vehicle in pairs(knownVehicles) do
        if vehicle and distanceSquared(player, vehicle) <= CONFIG.maxDistance * CONFIG.maxDistance then
            updateVehicle(player, vehicle, seen)
        else
            forgotten[#forgotten + 1] = key
            stopStream(key, "out of range or unloaded")
        end
    end
    for _, key in ipairs(forgotten) do
        knownVehicles[key] = nil
    end

    local unloaded = {}
    for key, _ in pairs(activeStreams) do
        if not seen[key] then
            unloaded[#unloaded + 1] = key
        end
    end
    for _, key in ipairs(unloaded) do
        stopStream(key, "vehicle unloaded")
    end
end

local function stopAll()
    local keys = {}
    for key, _ in pairs(activeStreams) do
        keys[#keys + 1] = key
    end
    for _, key in ipairs(keys) do
        stopStream(key, "session ended")
    end
end

Events.OnGameStart.Add(function()
    log("loaded; WIVK-FM=" .. CONFIG.frequency .. ", UUID=" .. CONFIG.stationUuid)
    log("the dedicated server carries only vanilla vehicle-radio state; every client opens its own stream")
end)
Events.OnTick.Add(update)
if Events.OnGameExit then
    Events.OnGameExit.Add(stopAll)
end
