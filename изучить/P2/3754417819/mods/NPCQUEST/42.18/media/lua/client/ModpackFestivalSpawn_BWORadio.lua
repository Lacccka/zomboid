-- Week One-style 3D music on radios/TVs (bundled; plays party track GUIDs from scripts).

ModpackFestivalBWORadio = ModpackFestivalBWORadio or {}
local BWORadio = ModpackFestivalBWORadio
BWORadio.tick = 0
BWORadio.cache = {}

local RESTLESS_END_GRACE_MS = 2500

local function getGUID(codes)
    return codes:match("GUID:([%w%-]+)")
end

-- B42: playSound returns a long handle; isPlaying(long) — not isPlaying(string).
function BWORadio.emitterPlay(emitter, soundName)
    if not emitter or not soundName then
        return nil
    end
    local handle
    pcall(function()
        handle = emitter:playSound(soundName)
    end)
    if type(handle) == "number" then
        return handle
    end
    return nil
end

function BWORadio.emitterIsPlaying(emitter, handle)
    if not emitter or type(handle) ~= "number" then
        return false
    end
    local ok, playing = pcall(function()
        return emitter:isPlaying(handle)
    end)
    return ok and playing == true
end

-- Restless car tracks: stop only when not already playing this clip (avoid skip/stutter).
function BWORadio.restlessPlayFromStart(wemitter, sound, existingHandle)
    if not wemitter or not sound then
        return nil
    end
    if type(existingHandle) == "number"
        and BWORadio.emitterIsPlaying(wemitter, existingHandle) then
        return existingHandle
    end
    pcall(function()
        wemitter:stopAll()
    end)
    return BWORadio.emitterPlay(wemitter, sound)
end

local function isRestlessWaveDevice(device)
    return device ~= nil and device._vehicle ~= nil
end

local function isFestivalDevice(device)
    return false
end

local function deviceCacheId(device)
    if not device then
        return nil
    end
    if device._vehicle then
        if ModpackFestivalVehicleRadio and ModpackFestivalVehicleRadio.getRestlessCacheId then
            return ModpackFestivalVehicleRadio.getRestlessCacheId(device._vehicle)
        end
        if ModpackFestivalRestlessRadio and ModpackFestivalRestlessRadio.getBwCacheIdForVehicle then
            return ModpackFestivalRestlessRadio.getBwCacheIdForVehicle(device._vehicle)
        end
    end
    if device.getX and device.getY and device.getZ then
        return device:getX() .. "-" .. device:getY() .. "-" .. device:getZ()
    end
    return nil
end

local function isRestlessSoundName(sound)
    if not sound or not ModpackFestivalRestlessRadio then
        return false
    end
    if ModpackFestivalRestlessRadio.isRestlessLineSound(sound) then
        return true
    end
    if sound == ModpackFestivalRestlessRadio.LINE_SOUND_ID then
        return true
    end
    if ModpackFestivalRestlessRadio.isSegmentSound
        and ModpackFestivalRestlessRadio.isSegmentSound(sound) then
        return true
    end
    return false
end

local function getEmitter(device)
    if not device or not device.getDeviceData then return nil, nil, nil, nil end
    local deviceData = device:getDeviceData()
    if not deviceData then return nil, nil, nil, nil end

    local emitter, vehicle, id
    if deviceData.isVehicleDevice and deviceData:isVehicleDevice() then
        local vehiclePart = deviceData:getParent()
        if vehiclePart then
            vehicle = vehiclePart:getVehicle()
            if vehicle then
                if ModpackFestivalVehicleRadio and ModpackFestivalVehicleRadio.getRestlessCacheId then
                    id = ModpackFestivalVehicleRadio.getRestlessCacheId(vehicle)
                elseif vehicle.getId then
                    id = "veh-" .. tostring(vehicle:getId())
                else
                    local x, y, z = vehicle:getX(), vehicle:getY(), vehicle:getZ()
                    id = x .. "-" .. y .. "-" .. z
                end
                emitter = vehicle:getEmitter()
            end
        end
    end

    if not emitter then
        local x, y, z = device:getX(), device:getY(), device:getZ()
        id = x .. "-" .. y .. "-" .. z
        emitter = deviceData:getEmitter()
    end

    local wemitter = getWorld():getFreeEmitter()
    return emitter, wemitter, vehicle, id
end

local function playSoundOnEmitter(wemitter, sound)
    if not wemitter or not sound then
        return sound, nil
    end
    wemitter:stopAll()
    return sound, BWORadio.emitterPlay(wemitter, sound)
end

BWORadio.PlaySound = function(device, sound)
    if not device or not sound then
        return false
    end
    local isRestlessPlay = isRestlessWaveDevice(device) and isRestlessSoundName(sound)
    if isRestlessPlay and ModpackFestivalRestlessRadio then
        if ModpackFestivalRestlessRadio.isSilenceTestMode
            and ModpackFestivalRestlessRadio.isSilenceTestMode() then
            return false
        end
        if not ModpackFestivalRestlessRadio.shouldPlayOnDevice(device) then
            return false
        end
        if ModpackFestivalRestlessRadio.isRestlessLineSound(sound)
            or sound == ModpackFestivalRestlessRadio.LINE_SOUND_ID then
            sound = ModpackFestivalRestlessRadio.pickSegmentSound()
        end
    end
    local emitter, wemitter, vehicle, id = getEmitter(device)
    if not wemitter or not id then return false end

    local stableId = isRestlessPlay and deviceCacheId(device) or nil

    if isRestlessPlay and vehicle then
        for cacheId, entry in pairs(BWORadio.cache) do
            if entry.restless and entry.vehicle == vehicle then
                if stableId and cacheId ~= stableId then
                    if ModpackFestivalRestlessRadio and ModpackFestivalRestlessRadio.stopCacheEntry then
                        ModpackFestivalRestlessRadio.stopCacheEntry(entry, cacheId)
                    else
                        BWORadio.cache[cacheId] = nil
                    end
                elseif entry.wemitter then
                    wemitter = entry.wemitter
                    id = stableId or cacheId
                end
            end
        end
    end

    if isRestlessPlay and vehicle and ModpackFestivalRestlessRadio
        and ModpackFestivalRestlessRadio.isSegmentSound
        and ModpackFestivalRestlessRadio.isSegmentPlaying
        and ModpackFestivalRestlessRadio.isSegmentSound(sound)
        and ModpackFestivalRestlessRadio.isSegmentPlaying(vehicle, sound) then
        return true
    end

    if isRestlessPlay and vehicle and ModpackFestivalRestlessRadio.stopVehiclePlayback
        and not (stableId and BWORadio.cache[stableId]
            and BWORadio.cache[stableId].restless
            and BWORadio.cache[stableId].sound == sound
            and BWORadio.emitterIsPlaying(
                BWORadio.cache[stableId].wemitter, BWORadio.cache[stableId].soundHandle)) then
        ModpackFestivalRestlessRadio.stopVehiclePlayback(vehicle)
    elseif isRestlessPlay and ModpackFestivalRestlessRadio.stopAllRestlessPlayback then
        ModpackFestivalRestlessRadio.stopAllRestlessPlayback()
    end

    if isRestlessPlay and stableId then
        id = stableId
    end

    local existing = BWORadio.cache[id]
    if existing and existing.restless and isRestlessPlay and existing.sound == sound
        and existing.wemitter and BWORadio.emitterIsPlaying(existing.wemitter, existing.soundHandle) then
        return true
    end

    local resolved = sound
    local soundHandle = nil
    if not isRestlessPlay then
        resolved, soundHandle = playSoundOnEmitter(wemitter, sound)
        if getWorld and getWorld().takeOwnershipOfEmitter then
            getWorld():takeOwnershipOfEmitter(wemitter)
        end
    end

    local entry = {
        device = device,
        vehicle = vehicle,
        wemitter = wemitter,
        emitter = emitter,
        sound = resolved,
        soundHandle = soundHandle,
        ffHandle = nil,
        ts = getTimestampMs(),
        playRetries = 0,
        started = not isRestlessPlay,
        lastEndCheckMs = 0,
    }
    if ModpackFestivalRestlessRadio and ModpackFestivalRestlessRadio.shouldPlayMusicOnDevice
        and ModpackFestivalRestlessRadio.shouldPlayMusicOnDevice(device) then
        entry.restless = true
    end
    BWORadio.cache[id] = entry

    if entry.restless and wemitter and resolved then
        entry.soundHandle = BWORadio.restlessPlayFromStart(wemitter, resolved, nil)
        entry.started = entry.soundHandle ~= nil
        entry.playRetries = entry.started and 0 or 1
        if entry.soundHandle and getWorld and getWorld().takeOwnershipOfEmitter then
            getWorld():takeOwnershipOfEmitter(wemitter)
        end
        if device and device.getDeviceData and ModpackFestivalRestlessRadio.getEmitterVolume then
            local dd = device:getDeviceData()
            local vol = ModpackFestivalRestlessRadio.getEmitterVolume(dd)
            if vol and vol > 0 then
                pcall(function() wemitter:setVolumeAll(vol) end)
            end
        end
        if vehicle then
            pcall(function()
                wemitter:setPos(vehicle:getX(), vehicle:getY(), vehicle:getZ())
            end)
        end
        pcall(function() wemitter:tick() end)
    end

    return true
end

BWORadio.IsPlaying = function(device)
    local id = deviceCacheId(device)
    return id ~= nil and BWORadio.cache[id] ~= nil
end

BWORadio.IsAudible = function(device)
    local id = deviceCacheId(device)
    local entry = id and BWORadio.cache[id]
    if not entry or not entry.wemitter or not entry.sound then return false end
    return BWORadio.emitterIsPlaying(entry.wemitter, entry.soundHandle)
end

local function onDeviceText(guid, codes, x, y, z, text, device)
    if isFestivalDevice(device) then return end
    if ModpackFestivalRestlessRadio and device and ModpackFestivalRestlessRadio.shouldPlayOnDevice
        and ModpackFestivalRestlessRadio.shouldPlayOnDevice(device) then
        if ModpackFestivalRestlessRadio.onDeviceText then
            if ModpackFestivalRestlessRadio.onDeviceText(guid, codes, x, y, z, text, device) then
                return
            end
        elseif ModpackFestivalRestlessRadio.stopVanillaRadioStatic then
            ModpackFestivalRestlessRadio.stopVanillaRadioStatic(device._vehicle, device)
        end
        if ModpackFestivalRestlessRadio.isSilenceTestMode
            and ModpackFestivalRestlessRadio.isSilenceTestMode() then
            return
        end
    end
    local sound = getGUID(codes)
    if ModpackFestivalRestlessRadio and ModpackFestivalRestlessRadio.isRestlessLineSound
        and ModpackFestivalRestlessRadio.isRestlessLineSound(sound) then
        return
    end
    if sound and device then
        BWORadio.PlaySound(device, sound)
    end
end

local function onTick()
    BWORadio.tick = (BWORadio.tick or 0) + 1
    if not ModpackFestivalTick.every(BWORadio.tick, ModpackFestivalTick.UI) then
        return
    end

    local cache = BWORadio.cache
    local timeMultiplier = 1
    if UIManager and UIManager.getSpeedControls then
        local sc = UIManager.getSpeedControls()
        if sc and sc.getCurrentGameSpeed then
            timeMultiplier = sc:getCurrentGameSpeed() or 1
        end
    end
    local now = getTimestampMs()

    for id, v in pairs(cache) do
        repeat
            if not v.wemitter or not v.device then
                cache[id] = nil
                break
            end

            if now - v.ts > 300000 then
                v.wemitter:stopAll()
                getWorld():returnOwnershipOfEmitter(v.wemitter)
                cache[id] = nil
                break
            end

            local deviceData = nil
            pcall(function()
                if v.device and v.device.getDeviceData then
                    deviceData = v.device:getDeviceData()
                end
            end)
            if not deviceData then
                cache[id] = nil
                break
            end

            if v.restless and ModpackFestivalRestlessRadio then
                if ModpackFestivalRestlessRadio.isSilenceTestMode
                    and ModpackFestivalRestlessRadio.isSilenceTestMode() then
                    if v.vehicle and ModpackFestivalRestlessRadio.stopVehiclePlayback then
                        ModpackFestivalRestlessRadio.stopVehiclePlayback(v.vehicle)
                    end
                    if deviceData and ModpackFestivalRestlessRadio.muteDeviceRadioEmitter then
                        ModpackFestivalRestlessRadio.muteDeviceRadioEmitter(deviceData)
                    end
                    if v.vehicle and ModpackFestivalRestlessRadio.silenceVehicleRadioIfOn then
                        ModpackFestivalRestlessRadio.silenceVehicleRadioIfOn(v.vehicle)
                    end
                    cache[id] = nil
                    break
                end
                if ModpackFestivalRestlessRadio.silenceVehicleRadioIfOn then
                    ModpackFestivalRestlessRadio.silenceVehicleRadioIfOn(v.vehicle)
                elseif ModpackFestivalRestlessRadio.stopVanillaRadioStatic then
                    ModpackFestivalRestlessRadio.stopVanillaRadioStatic(v.vehicle, v.device)
                end
                if deviceData and ModpackFestivalRestlessRadio.muteDeviceRadioEmitter then
                    ModpackFestivalRestlessRadio.muteDeviceRadioEmitter(deviceData)
                end
                if not ModpackFestivalRestlessRadio.shouldPlayOnDevice(v.device) then
                    v.wemitter:stopAll()
                    getWorld():returnOwnershipOfEmitter(v.wemitter)
                    cache[id] = nil
                    break
                end
                if not v.started then
                    v.playRetries = (v.playRetries or 0) + 1
                    if BWORadio.emitterIsPlaying(v.wemitter, v.soundHandle) then
                        v.started = true
                    elseif (v.playRetries or 0) <= 24 then
                        v.soundHandle = BWORadio.restlessPlayFromStart(
                            v.wemitter, v.sound, v.soundHandle)
                        if not v.soundHandle and ModpackFestivalRestlessRadio.pickSegmentSound then
                            v.sound = ModpackFestivalRestlessRadio.pickSegmentSound(v.sound)
                            v.soundHandle = BWORadio.restlessPlayFromStart(
                                v.wemitter, v.sound, nil)
                        end
                        if v.soundHandle and getWorld and getWorld().takeOwnershipOfEmitter then
                            getWorld():takeOwnershipOfEmitter(v.wemitter)
                        end
                        v.started = v.soundHandle ~= nil
                    else
                        cache[id] = nil
                        break
                    end
                elseif timeMultiplier <= 1
                    and not BWORadio.emitterIsPlaying(v.wemitter, v.soundHandle) then
                    local sinceCheck = now - (v.lastEndCheckMs or 0)
                    if sinceCheck >= RESTLESS_END_GRACE_MS then
                        v.lastEndCheckMs = now
                        if v.vehicle and ModpackFestivalRestlessRadio.advanceToNextSegment
                            and ModpackFestivalRestlessRadio.advanceToNextSegment(v.vehicle, v.sound) then
                            break
                        end
                    end
                end
            end

            if not v.restless then
                if not BWORadio.emitterIsPlaying(v.wemitter, v.soundHandle) then
                    v.playRetries = (v.playRetries or 0) + 1
                    if v.playRetries <= 8 then
                        v.sound, v.soundHandle = playSoundOnEmitter(v.wemitter, v.sound)
                        if getWorld and getWorld().takeOwnershipOfEmitter then
                            getWorld():takeOwnershipOfEmitter(v.wemitter)
                        end
                    else
                        v.wemitter:stopAll()
                        getWorld():returnOwnershipOfEmitter(v.wemitter)
                        cache[id] = nil
                        break
                    end
                else
                    v.playRetries = 0
                end
            end

            if deviceData:isInventoryDevice() and not isFestivalDevice(v.device) then
                v.wemitter:setVolumeAll(0)
                v.wemitter:tick()
                cache[id] = nil
                break
            end

            local volume = 0
            if v.restless and ModpackFestivalRestlessRadio then
                volume = ModpackFestivalRestlessRadio.getEmitterVolume(deviceData)
            elseif deviceData:getIsTurnedOn() then
                volume = deviceData:getDeviceVolume() / 3
            end
            v.wemitter:setVolumeAll(volume)
            v.wemitter:tick()

            local x, y, z
            if v.vehicle then
                x, y, z = v.vehicle:getX(), v.vehicle:getY(), v.vehicle:getZ()
            else
                x, y, z = v.device:getX(), v.device:getY(), v.device:getZ()
            end
            if x and y and z then
                v.wemitter:setPos(x, y, z)
            end
        until true
    end
end

Events.OnDeviceText.Add(onDeviceText)
Events.OnTick.Add(onTick)

BWORadio = ModpackFestivalBWORadio
