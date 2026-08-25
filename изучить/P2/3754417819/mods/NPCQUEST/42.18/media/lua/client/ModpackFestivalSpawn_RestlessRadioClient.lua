-- Restless Dreams FM (88.880): plays only when the vehicle radio is on AND tuned to this station.
-- Volume follows the in-game radio volume slider (deviceVolume / 3, same as vanilla radios).
--
-- SILENCE_TEST_MODE: true = no audio/display (diagnostic). Music is RestlessDreamsSeg## via BWORadio only.
-- Channel XML has no broadcasts so vanilla never feeds static/BZZZ lines on 88.880 FM.

if isServer() and not isClient() then return end

ModpackFestivalRestlessRadio = ModpackFestivalRestlessRadio or {}
local Restless = ModpackFestivalRestlessRadio

Restless.SILENCE_TEST_MODE = false

Restless.CHANNEL = (ModpackFestivalVehicleRadio and ModpackFestivalVehicleRadio.CHANNEL) or 88880
-- Playback gain on top of in-game radio slider (deviceVolume / 3).
Restless.VOLUME_MULT = (ModpackFestivalVehicleRadio and ModpackFestivalVehicleRadio.VOLUME_MULT) or 1.25
Restless.LINE_SOUND_ID = "b7e4c9f2-3a1d-4e8b-9c6f-2d5a8e1b0c93"
Restless.LINE_SOUND_ID_LEGACY = "RestlessDreamsRadio"
Restless.SEGMENT_SOUNDS = {
    "RestlessDreamsSeg01",
    "RestlessDreamsSeg02",
    "RestlessDreamsSeg03",
    "RestlessDreamsSeg04",
    "RestlessDreamsSeg05",
}
Restless.SEGMENT_COUNT = #Restless.SEGMENT_SOUNDS
Restless.segmentBag = {}
Restless.segmentBagIndex = 1
Restless.tick = 0
Restless.vehicleState = {}

-- Vanilla radio hiss / program bed (mod music is RestlessDreamsSeg## via BWORadio only).
Restless.VANILLA_RADIO_NOISE_NAMES = {
    "VehicleRadioButton",
    "VehicleRadioStatic",
    "VehicleRadioNoise",
    "RadioStatic",
    "RadioNoise",
    "VehicleRadioZap",
    "RadioZap",
    "VehicleRadioProgram",
    "RadioTalk",
    "VehicleRadioTuneIn",
    "TuneIn",
    "Static",
    "Bzzt",
    "BZZT",
}

local MOD_ID = "ModpackFestivalSpawn"

function Restless.isSilenceTestMode()
    return Restless.SILENCE_TEST_MODE == true
end

function Restless.shouldPlayMusicOnDevice(device)
    if Restless.isSilenceTestMode() then
        return false
    end
    return Restless.shouldPlayOnDevice(device)
end

function Restless.isRadioNoiseLine(text)
    if not text or text == "" then
        return false
    end
    local lower = string.lower(tostring(text))
    if lower:find("static", 1, true) or lower:find("<bzzt>", 1, true)
        or lower:find("<wzzt>", 1, true) or lower:find("<fzzt>", 1, true)
        or lower:find("<szzt>", 1, true) or lower:find("[img=", 1, true) then
        return true
    end
    return false
end

function Restless.cleanVehicleRadioDevice(deviceData)
    if not deviceData then
        return
    end
    pcall(function()
        if deviceData.cleanSoundsAndEmitter then
            deviceData:cleanSoundsAndEmitter()
        end
    end)
    pcall(function()
        if deviceData.setNoTransmit then
            deviceData:setNoTransmit(true)
        end
    end)
end

function Restless.clearVehicleRadioDisplay(device)
    local deviceData
    if device and device.getDeviceData then
        deviceData = device:getDeviceData()
    end
    Restless.cleanVehicleRadioDevice(deviceData)
    Restless.muteDeviceRadioEmitter(deviceData)
end

local function stopEmitterNoiseByName(emitter)
    if not emitter then
        return
    end
    pcall(function()
        if emitter.stopAll then
            emitter:stopAll()
        end
    end)
    for i = 1, #Restless.VANILLA_RADIO_NOISE_NAMES do
        local name = Restless.VANILLA_RADIO_NOISE_NAMES[i]
        pcall(function()
            if emitter.stopOrTriggerSoundByName then
                emitter:stopOrTriggerSoundByName(name)
            end
        end)
    end
end

local function stopDeviceNoise(deviceData)
    if not deviceData then
        return
    end
    pcall(function()
        if deviceData.cleanSoundsAndEmitter then
            deviceData:cleanSoundsAndEmitter()
        end
    end)
    pcall(function()
        if deviceData.setNoTransmit then
            deviceData:setNoTransmit(true)
        end
    end)
    for i = 1, #Restless.VANILLA_RADIO_NOISE_NAMES do
        local name = Restless.VANILLA_RADIO_NOISE_NAMES[i]
        pcall(function()
            if deviceData.stopOrTriggerSoundByName then
                deviceData:stopOrTriggerSoundByName(name)
            end
        end)
    end
    pcall(function()
        if deviceData.getEmitter then
            stopEmitterNoiseByName(deviceData:getEmitter())
        end
    end)
end

local function getVehicleRadioPart(vehicle)
    if not vehicle or not vehicle.getPartById then
        return nil, nil
    end
    local partIds = { "Radio", "HamRadio" }
    for i = 1, #partIds do
        local part = vehicle:getPartById(partIds[i])
        if part and part.getDeviceData then
            local deviceData = part:getDeviceData()
            if deviceData then
                return part, deviceData
            end
        end
    end
    local parts = vehicle:getParts()
    if not parts then
        return nil, nil
    end
    for pi = 0, parts:size() - 1 do
        local part = parts:get(pi)
        if part and part.getDeviceData then
            local deviceData = part:getDeviceData()
            if deviceData and deviceData.isVehicleDevice and deviceData:isVehicleDevice() then
                return part, deviceData
            end
        end
    end
    return nil, nil
end

-- Stop hiss/program on the part emitter only (music uses a separate world emitter).
function Restless.muteDeviceRadioEmitter(deviceData)
    if not deviceData then
        return
    end
    if deviceData.getIsTurnedOn and not deviceData:getIsTurnedOn() then
        return
    end
    stopDeviceNoise(deviceData)
    if deviceData.getEmitter then
        local partEmitter = deviceData:getEmitter()
        stopEmitterNoiseByName(partEmitter)
        pcall(function()
            if partEmitter and partEmitter.setVolumeAll then
                partEmitter:setVolumeAll(0)
            end
        end)
    end
end

function Restless.silenceVehicleRadioIfOn(vehicle)
    if not vehicle then
        return
    end
    local _, deviceData = getVehicleRadioPart(vehicle)
    if not deviceData or not deviceData.getIsTurnedOn or not deviceData:getIsTurnedOn() then
        return
    end
    Restless.muteVanillaRadioPart(vehicle)
end

-- Light mute (tick): part + vehicle emitters, no cleanSounds (avoids fighting BWORadio playback).
function Restless.muteVanillaRadioPart(vehicle, device)
    local deviceData
    if device and device.getDeviceData then
        deviceData = device:getDeviceData()
    end
    if not deviceData and vehicle then
        local _, dd = getVehicleRadioPart(vehicle)
        deviceData = dd
    end
    stopDeviceNoise(deviceData)
    Restless.muteDeviceRadioEmitter(deviceData)
    if vehicle and vehicle.getEmitter then
        stopEmitterNoiseByName(vehicle:getEmitter())
    end
end

-- Full mute (tune-in / block vanilla line): includes cleanSoundsAndEmitter on the radio part.
function Restless.stopVanillaRadioStatic(vehicle, device)
    Restless.muteVanillaRadioPart(vehicle, device)
    local deviceData
    if device and device.getDeviceData then
        deviceData = device:getDeviceData()
    end
    if not deviceData and vehicle then
        local _, dd = getVehicleRadioPart(vehicle)
        deviceData = dd
    end
    Restless.cleanVehicleRadioDevice(deviceData)
end

function Restless.silenceVanillaRadioIfOnStation(vehicle)
    Restless.silenceVehicleRadioIfOn(vehicle)
end

local function getGUID(codes)
    if not codes then return nil end
    return codes:match("GUID:([%w%-]+)")
end

function Restless.isRestlessLineSound(soundOrGuid)
    return soundOrGuid == Restless.LINE_SOUND_ID
        or soundOrGuid == Restless.LINE_SOUND_ID_LEGACY
end

function Restless.getMusicDisplayLine()
    return ""
end

function Restless.channelMatches(channel)
    if channel == nil then
        return false
    end
    local n = tonumber(channel)
    if not n then
        return false
    end
    return n == Restless.CHANNEL or math.abs(n - Restless.CHANNEL) <= 1
end

function Restless.isRestlessChannel(channel)
    if Restless.channelMatches(channel) then
        return true
    end
    local zr = getZomboidRadio and getZomboidRadio()
    if zr and zr.getChannelName and channel ~= nil then
        local name = zr:getChannelName(channel)
        if name and string.find(string.lower(tostring(name)), "restless", 1, true) then
            return true
        end
    end
    return false
end

local function shuffleBag(list)
    for i = #list, 2, -1 do
        local j = 1
        if ZombRand then
            j = ZombRand(i) + 1
        else
            j = ((getTimestampMs and getTimestampMs() or i) % i) + 1
        end
        list[i], list[j] = list[j], list[i]
    end
end

local function refillSegmentBag(excludeSound)
    local sounds = Restless.SEGMENT_SOUNDS or {}
    local bag = {}
    for i = 1, #sounds do
        if sounds[i] ~= excludeSound then
            bag[#bag + 1] = sounds[i]
        end
    end
    if #bag == 0 then
        for i = 1, #sounds do
            bag[#bag + 1] = sounds[i]
        end
    end
    if #bag > 1 then
        shuffleBag(bag)
    end
    Restless.segmentBag = bag
    Restless.segmentBagIndex = 1
end

function Restless.pickSegmentSound(excludeSound)
    local sounds = Restless.SEGMENT_SOUNDS or {}
    if #sounds <= 0 then
        return "RestlessDreamsSeg01"
    end
    if #sounds == 1 then
        return sounds[1]
    end
    if not Restless.segmentBag
        or #Restless.segmentBag == 0
        or Restless.segmentBagIndex > #Restless.segmentBag then
        refillSegmentBag(excludeSound)
    end
    local pick = Restless.segmentBag[Restless.segmentBagIndex]
    Restless.segmentBagIndex = (Restless.segmentBagIndex or 1) + 1
    if not pick then
        refillSegmentBag(excludeSound)
        pick = Restless.segmentBag[1] or sounds[1]
        Restless.segmentBagIndex = 2
    end
    return pick
end

-- Same scaling as vanilla BWORadio / IsoRadio (0 when off or wrong station).
function Restless.getEmitterVolume(deviceData)
    if Restless.isSilenceTestMode() then
        return 0
    end
    if not deviceData then
        return 0
    end
    if deviceData.getIsTurnedOn and not deviceData:getIsTurnedOn() then
        return 0
    end
    if deviceData.getChannel and not Restless.isRestlessChannel(deviceData:getChannel()) then
        return 0
    end
    if deviceData.getDeviceVolume then
        local mult = Restless.VOLUME_MULT or 1.25
        return (deviceData:getDeviceVolume() / 3) * mult
    end
    return 0
end

function Restless.deviceDataOnStation(deviceData)
    if not deviceData then
        return false
    end
    if deviceData.getIsTurnedOn and not deviceData:getIsTurnedOn() then
        return false
    end
    if deviceData.getChannel then
        return Restless.isRestlessChannel(deviceData:getChannel())
    end
    return false
end

function Restless.shouldPlayOnDevice(device)
    if not device or not device.getDeviceData then
        return false
    end
    local deviceData = device:getDeviceData()
    if not deviceData then
        return false
    end
    if device._vehicle then
        return Restless.deviceDataOnStation(deviceData)
    end
    if deviceData.isVehicleDevice and not deviceData:isVehicleDevice() then
        return false
    end
    return Restless.deviceDataOnStation(deviceData)
end

function Restless.getBwCacheIdForVehicle(vehicle)
    if ModpackFestivalVehicleRadio and ModpackFestivalVehicleRadio.getRestlessCacheId then
        return ModpackFestivalVehicleRadio.getRestlessCacheId(vehicle)
    end
    if not vehicle then
        return nil
    end
    if vehicle.getId then
        local ok, vid = pcall(function()
            return vehicle:getId()
        end)
        if ok and vid ~= nil then
            return "veh-" .. tostring(vid)
        end
    end
    return "vehpos-" .. tostring(vehicle:getX()) .. "-" .. tostring(vehicle:getY())
        .. "-" .. tostring(vehicle:getZ() or 0)
end

function Restless.vehicleStateKey(vehicle)
    if not vehicle or not vehicle.getId then
        return nil
    end
    return tostring(vehicle:getId())
end

function Restless.stopCacheEntry(entry, cacheId)
    if not entry then
        return
    end
    if entry.wemitter then
        entry.wemitter:stopAll()
        if getWorld and getWorld().returnOwnershipOfEmitter then
            getWorld():returnOwnershipOfEmitter(entry.wemitter)
        end
    end
    if cacheId and ModpackFestivalBWORadio and ModpackFestivalBWORadio.cache then
        ModpackFestivalBWORadio.cache[cacheId] = nil
    end
end

function Restless.stopAllRestlessPlayback()
    if not ModpackFestivalBWORadio or not ModpackFestivalBWORadio.cache then
        return
    end
    local cache = ModpackFestivalBWORadio.cache
    for cacheId, entry in pairs(cache) do
        if entry.restless then
            Restless.stopCacheEntry(entry, cacheId)
        end
    end
end

function Restless.stopVehiclePlayback(vehicle)
    if not vehicle then
        return
    end
    -- Do not stopAll() on vehicle:getEmitter() — that kills engine/UI audio on B42.
    if not ModpackFestivalBWORadio or not ModpackFestivalBWORadio.cache then
        return
    end
    local cache = ModpackFestivalBWORadio.cache
    local id = Restless.getBwCacheIdForVehicle(vehicle)
    if id and cache[id] and cache[id].restless then
        Restless.stopCacheEntry(cache[id], id)
    end
    for cacheId, entry in pairs(cache) do
        if entry.restless and entry.vehicle == vehicle then
            Restless.stopCacheEntry(entry, cacheId)
        end
    end
end

function Restless.isSegmentSound(sound)
    return type(sound) == "string" and sound:match("^RestlessDreamsSeg%d%d$") ~= nil
end

function Restless.isSegmentPlaying(vehicle, sound)
    if not vehicle or not sound or not ModpackFestivalBWORadio or not ModpackFestivalBWORadio.cache then
        return false
    end
    local isPlaying = ModpackFestivalBWORadio.emitterIsPlaying
    for _, entry in pairs(ModpackFestivalBWORadio.cache) do
        if entry.restless and entry.vehicle == vehicle and entry.sound == sound
            and entry.wemitter and isPlaying
            and isPlaying(entry.wemitter, entry.soundHandle) then
            return true
        end
    end
    return false
end

-- True while a segment is playing or the emitter is still starting playback.
function Restless.isAnySegmentPlaying(vehicle)
    if Restless.isSilenceTestMode() then
        return false
    end
    if not vehicle or not ModpackFestivalBWORadio or not ModpackFestivalBWORadio.cache then
        return false
    end
    local now = getTimestampMs and getTimestampMs() or 0
    local isPlaying = ModpackFestivalBWORadio.emitterIsPlaying
    for _, entry in pairs(ModpackFestivalBWORadio.cache) do
        if entry.restless and entry.vehicle == vehicle and entry.wemitter and entry.sound then
            if isPlaying and isPlaying(entry.wemitter, entry.soundHandle) then
                return true
            end
            if not entry.started and entry.ts and (now - entry.ts) < 4000
                and (entry.playRetries or 0) < 12 then
                return true
            end
        end
    end
    return false
end

function Restless.advanceToNextSegment(vehicle, excludeSound)
    if Restless.isSilenceTestMode() then
        if vehicle then
            Restless.stopVehiclePlayback(vehicle)
            Restless.silenceVehicleRadioIfOn(vehicle)
        end
        return false
    end
    if not vehicle then
        return false
    end
    local _, deviceData = getVehicleRadioPart(vehicle)
    if not Restless.deviceDataOnStation(deviceData) then
        Restless.stopVehiclePlayback(vehicle)
        return false
    end
    local sound = Restless.pickSegmentSound(excludeSound)
    local key = Restless.vehicleStateKey(vehicle)
    if key then
        local st = Restless.vehicleState[key] or {}
        st.segmentSound = sound
        st.on = true
        st.onStation = true
        Restless.vehicleState[key] = st
    end
    return Restless.playSegmentSound(vehicle, sound) == true
end

function Restless.playSegmentSound(vehicle, sound)
    if Restless.isSilenceTestMode() then
        if vehicle then
            Restless.stopVehiclePlayback(vehicle)
            Restless.silenceVehicleRadioIfOn(vehicle)
        end
        return false
    end
    if not vehicle or not sound then
        return false
    end
    local waveDevice, deviceData = Restless.findVehicleWaveDevice(vehicle)
    if not Restless.deviceDataOnStation(deviceData) then
        Restless.stopVehiclePlayback(vehicle)
        return false
    end
    if Restless.isSegmentPlaying(vehicle, sound) then
        return true
    end
    local cacheId = Restless.getBwCacheIdForVehicle(vehicle)
    local cache = ModpackFestivalBWORadio and ModpackFestivalBWORadio.cache
    local entry = cacheId and cache and cache[cacheId]
    if entry and entry.restless and entry.sound == sound and entry.wemitter then
        local isPlaying = ModpackFestivalBWORadio.emitterIsPlaying
        if isPlaying and isPlaying(entry.wemitter, entry.soundHandle) then
            return true
        end
        if not entry.started and entry.ts and (getTimestampMs and getTimestampMs() or 0) - entry.ts < 4000
            and (entry.playRetries or 0) < 12 then
            return true
        end
    end
    Restless.stopVehiclePlayback(vehicle)
    if not Restless.isAnySegmentPlaying(vehicle) then
        Restless.stopVanillaRadioStatic(vehicle, waveDevice)
    else
        Restless.muteVanillaRadioPart(vehicle, waveDevice)
    end
    if not waveDevice or not ModpackFestivalBWORadio or not ModpackFestivalBWORadio.PlaySound then
        return false
    end
    local key = Restless.vehicleStateKey(vehicle)
    if key then
        local st = Restless.vehicleState[key] or {}
        st.segmentSound = sound
        Restless.vehicleState[key] = st
    end
    return ModpackFestivalBWORadio.PlaySound(waveDevice, sound) == true
end

function Restless.makeVehicleRadioDevice(vehicle, part, deviceData)
    if not vehicle or not deviceData then
        return nil
    end
    local proxy = {
        _vehicle = vehicle,
        _part = part,
        _deviceData = deviceData,
    }
    function proxy:getDeviceData()
        if self._part and self._part.getDeviceData then
            local dd = self._part:getDeviceData()
            if dd then
                return dd
            end
        end
        return self._deviceData
    end
    function proxy:getX()
        return self._vehicle:getX()
    end
    function proxy:getY()
        return self._vehicle:getY()
    end
    function proxy:getZ()
        return self._vehicle:getZ()
    end
    function proxy:getModData()
        return {}
    end
    return proxy
end

function Restless.findVehicleWaveDevice(vehicle)
    if not vehicle then
        return nil, nil
    end
    local part, deviceData = getVehicleRadioPart(vehicle)
    if not deviceData then
        return nil, nil
    end
    -- Always use proxy: part:getDevice() / getObject() often lack getModData and break BWORadio checks.
    return Restless.makeVehicleRadioDevice(vehicle, part, deviceData), deviceData
end

function Restless.playOnVehicle(vehicle, forceNewSegment)
    if Restless.isSilenceTestMode() then
        if vehicle then
            Restless.stopVehiclePlayback(vehicle)
            Restless.silenceVehicleRadioIfOn(vehicle)
        end
        return false
    end
    if not vehicle then
        return false
    end
    local key = Restless.vehicleStateKey(vehicle)
    local st = key and Restless.vehicleState[key]
    local sound = st and st.segmentSound
    if forceNewSegment or not sound then
        sound = Restless.pickSegmentSound()
        if key then
            Restless.vehicleState[key] = Restless.vehicleState[key] or {}
            Restless.vehicleState[key].segmentSound = sound
        end
    end
    if not forceNewSegment and Restless.isSegmentPlaying(vehicle, sound) then
        return true
    end
    return Restless.playSegmentSound(vehicle, sound) == true
end

function Restless.onDeviceText(guid, codes, x, y, z, text, device)
    if not device or not device.getDeviceData then
        return false
    end
    if not Restless.shouldPlayOnDevice(device) then
        return false
    end
    local deviceData = device:getDeviceData()
    local vehiclePart = deviceData and deviceData.getParent and deviceData:getParent()
    local vehicle = device._vehicle
        or (vehiclePart and vehiclePart.getVehicle and vehiclePart:getVehicle())
    local musicUp = vehicle and Restless.isAnySegmentPlaying(vehicle)
    if vehicle then
        if text and Restless.isRadioNoiseLine(text) then
            Restless.stopVanillaRadioStatic(vehicle, device)
        elseif musicUp then
            Restless.muteVanillaRadioPart(vehicle, device)
        else
            Restless.stopVanillaRadioStatic(vehicle, device)
        end
    else
        Restless.clearVehicleRadioDisplay(device)
    end
    if Restless.isSilenceTestMode() then
        return true
    end
    if text and Restless.isRadioNoiseLine(text) then
        return true
    end
    local soundId = getGUID(codes) or guid
    if Restless.isRestlessLineSound(soundId) and vehicle then
        if musicUp then
            return true
        end
        return Restless.playOnVehicle(vehicle, true) == true
    end
    -- Swallow any other line on 88.880 (vanilla static/program); music starts from Lua only.
    return true
end

local function cacheHasRestlessForVehicle(vehicle)
    if not vehicle or not ModpackFestivalBWORadio or not ModpackFestivalBWORadio.cache then
        return false
    end
    for _, entry in pairs(ModpackFestivalBWORadio.cache) do
        if entry.restless and entry.vehicle == vehicle then
            return true
        end
    end
    return false
end

local function updateVehicleState(vehicle, deviceData)
    local key = Restless.vehicleStateKey(vehicle)
    if not key then
        return
    end

    local on = deviceData and deviceData.getIsTurnedOn and deviceData:getIsTurnedOn()
    local channel = deviceData and deviceData.getChannel and deviceData:getChannel() or 0
    local onStation = on == true and Restless.isRestlessChannel(channel)
    local prev = Restless.vehicleState[key] or { on = false, onStation = false, channel = 0 }

    if not onStation then
        if prev.onStation or cacheHasRestlessForVehicle(vehicle) then
            Restless.stopVehiclePlayback(vehicle)
        end
        Restless.vehicleState[key] = { on = on == true, onStation = false, channel = channel, segmentSound = nil }
        return
    end

    Restless.silenceVehicleRadioIfOn(vehicle)

    if Restless.isSilenceTestMode() then
        Restless.stopVehiclePlayback(vehicle)
        Restless.vehicleState[key] = {
            on = true,
            onStation = true,
            channel = channel,
            segmentSound = nil,
        }
        return
    end

    local activated = (not prev.on) or (not prev.onStation) or (prev.channel ~= channel)
    local playing = Restless.isAnySegmentPlaying(vehicle)
    local hasCache = cacheHasRestlessForVehicle(vehicle)
    local segmentSound = prev.segmentSound

    if activated and not playing and not hasCache then
        segmentSound = Restless.pickSegmentSound()
        Restless.vehicleState[key] = {
            on = true,
            onStation = true,
            channel = channel,
            segmentSound = segmentSound,
        }
        Restless.playSegmentSound(vehicle, segmentSound)
    elseif onStation and not playing then
        segmentSound = segmentSound or Restless.pickSegmentSound()
        Restless.vehicleState[key] = {
            on = true,
            onStation = true,
            channel = channel,
            segmentSound = segmentSound,
        }
        Restless.playSegmentSound(vehicle, segmentSound)
    else
        Restless.vehicleState[key] = {
            on = true,
            onStation = true,
            channel = channel,
            segmentSound = segmentSound,
        }
    end
    Restless.silenceVehicleRadioIfOn(vehicle)
end

local function onDeviceTextSilenceGuard(guid, codes, x, y, z, text, device)
    if not Restless.isSilenceTestMode() then
        return
    end
    if device and device.getDeviceData and Restless.shouldPlayOnDevice(device) then
        Restless.onDeviceText(guid, codes, x, y, z, text, device)
        return
    end
    local player = getSpecificPlayer and getSpecificPlayer(0)
    if not player then
        return
    end
    local vehicle = player.getVehicle and player:getVehicle()
    if not vehicle then
        return
    end
    local _, deviceData = getVehicleRadioPart(vehicle)
    if not Restless.deviceDataOnStation(deviceData) then
        return
    end
    Restless.stopVanillaRadioStatic(vehicle, nil)
    if text and Restless.isRadioNoiseLine(text) then
        Restless.cleanVehicleRadioDevice(deviceData)
    end
end

local function onTick()
    Restless.tick = (Restless.tick or 0) + 1
    if not ModpackFestivalTick or not ModpackFestivalTick.every then
        return
    end
    local player = getSpecificPlayer and getSpecificPlayer(0)
    if not player then
        return
    end

    local vehicle = player.getVehicle and player:getVehicle()
    local silenceInterval = ModpackFestivalTick.UI_FAST or 15
    if vehicle then
        local _, deviceData = getVehicleRadioPart(vehicle)
        if deviceData and Restless.deviceDataOnStation(deviceData) then
            silenceInterval = ModpackFestivalTick.UI_FAST or 15
        end
    end
    if not ModpackFestivalTick.every(Restless.tick, silenceInterval) then
        return
    end

    if vehicle then
        local _, deviceData = getVehicleRadioPart(vehicle)
        if deviceData then
            Restless.silenceVehicleRadioIfOn(vehicle)
            updateVehicleState(vehicle, deviceData)
        end
        return
    end

    if ModpackFestivalBWORadio and ModpackFestivalBWORadio.cache then
        for cacheId, entry in pairs(ModpackFestivalBWORadio.cache) do
            if entry.restless then
                Restless.stopCacheEntry(entry, cacheId)
            end
        end
    end
end

Events.OnTick.Add(onTick)
Events.OnDeviceText.Add(onDeviceTextSilenceGuard)

if Restless.isSilenceTestMode() then
    print("[" .. MOD_ID .. "] Restless Dreams 88.880 FM — SILENCE TEST (no audio, no display, no music)")
else
    print("[" .. MOD_ID .. "] Restless Dreams 88.880 FM — mod music only (RestlessDreamsSeg##, no vanilla static)")
end
