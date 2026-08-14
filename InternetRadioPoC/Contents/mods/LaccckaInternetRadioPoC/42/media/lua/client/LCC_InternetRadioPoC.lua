-- Registered GameSound and vehicle-emitter probe for Project Zomboid B42.20.2.
-- No OnTick callback, vehicle scan, HTTP request, luajava, or external loader.

local MOD_TAG = "[LCC Internet Radio PoC]"
local SOUND_NAME = "LCCInternetRadioTest"

-- LWJGL/PZ key codes. Numeric constants avoid depending on an exposed
-- Keyboard Java class in the restricted multiplayer Lua environment.
local KEY_HTTP_REPORT = 66 -- F8
local KEY_REGISTERED_CLIP = 67 -- F9
local KEY_NAMED_WORLD = 68 -- F10
local KEY_VEHICLE_NAMED = 87 -- F11

local lastWorldAudio = nil
local lastEmitter = nil
local lastEmitterHandle = nil

local function log(message)
    print(MOD_TAG .. " " .. tostring(message))
end

local function describe(value)
    if value == nil then return "nil" end
    local ok, text = pcall(tostring, value)
    if ok then return text end
    return "<value could not be converted to text>"
end

local function attempt(label, callback)
    local ok, result = pcall(callback)
    if ok then
        log(label .. ": OK; returned=" .. describe(result))
    else
        log(label .. ": FAILED; " .. describe(result))
    end
    return ok, result
end

local function getManager()
    local ok, manager = attempt("getSoundManager", function()
        return getSoundManager()
    end)
    if not ok or not manager then
        log("test stopped: no sound manager")
        return nil
    end
    return manager
end

local function stopPrevious(manager)
    if lastWorldAudio then
        attempt("StopSound(previous world audio)", function()
            manager:StopSound(lastWorldAudio)
        end)
        lastWorldAudio = nil
    end
    if lastEmitter and lastEmitterHandle then
        attempt("emitter:stopSound(previous)", function()
            return lastEmitter:stopSound(lastEmitterHandle)
        end)
        lastEmitter = nil
        lastEmitterHandle = nil
    end
end

local function getPlayerAndSquare()
    local playerOk, player = attempt("getPlayer", function()
        return getPlayer()
    end)
    if not playerOk or not player then return nil, nil end

    local squareOk, square = attempt("player:getSquare", function()
        return player:getSquare()
    end)
    if not squareOk or not square then return player, nil end
    return player, square
end

local function getVehicleAndEmitter()
    local player = getPlayerAndSquare()
    if not player then
        log("vehicle test stopped: no player")
        return nil, nil
    end

    local vehicleOk, vehicle = attempt("player:getVehicle", function()
        return player:getVehicle()
    end)
    if not vehicleOk or not vehicle then
        log("vehicle test stopped: sit inside a vehicle first")
        return nil, nil
    end

    local emitterOk, emitter = attempt("vehicle:getEmitter", function()
        return vehicle:getEmitter()
    end)
    if not emitterOk or not emitter then
        log("vehicle test stopped: vehicle has no emitter")
        return nil, nil
    end
    return vehicle, emitter
end

local function rememberEmitterPlayback(emitter, handle)
    if not handle or handle == 0 then
        log("vehicle playback failed: emitter returned handle 0")
        return
    end
    attempt("vehicle emitter:set3D", function()
        return emitter:set3D(handle, true)
    end)
    attempt("vehicle emitter:setVolume", function()
        return emitter:setVolume(handle, 1.0)
    end)
    attempt("vehicle emitter:isPlaying", function()
        return emitter:isPlaying(handle)
    end)
    lastEmitter = emitter
    lastEmitterHandle = handle
end

local function runNamedWorldTest()
    log("registered GameSound world control started")
    local manager = getManager()
    if not manager then return end
    stopPrevious(manager)

    local _, square = getPlayerAndSquare()
    if not square then
        log("registered world control stopped: no player square")
        return
    end

    local playOk, audio = attempt("PlayWorldSoundWav(registered name)", function()
        return manager:PlayWorldSoundWav(
            SOUND_NAME,
            false,
            square,
            0.0,
            30.0,
            1.0,
            false
        )
    end)
    if playOk and audio then lastWorldAudio = audio end
    log("registered GameSound world control finished")
end

local function runVehicleNamedTest()
    log("registered GameSound vehicle-emitter control started")
    local manager = getManager()
    if not manager then return end
    stopPrevious(manager)

    local _, emitter = getVehicleAndEmitter()
    if not emitter then return end

    local playOk, handle = attempt("vehicle emitter:playSound(registered name)", function()
        return emitter:playSound(SOUND_NAME)
    end)
    if playOk then rememberEmitterPlayback(emitter, handle) end
    log("registered GameSound vehicle-emitter control finished")
end

local function runRegisteredClipTest()
    log("registered GameSoundClip vehicle test started")
    local manager = getManager()
    if not manager then return end
    stopPrevious(manager)

    local vehicle, emitter = getVehicleAndEmitter()
    if not vehicle or not emitter then return end

    local soundOk, gameSound = attempt("GameSounds.getSound", function()
        return GameSounds.getSound(SOUND_NAME)
    end)
    if not soundOk or not gameSound then
        log("registered clip test stopped: GameSound is unavailable")
        return
    end

    local clipOk, clip = attempt("gameSound:getRandomClip", function()
        return gameSound:getRandomClip()
    end)
    if not clipOk or not clip then
        log("registered clip test stopped: GameSound has no clip")
        return
    end

    attempt("clip:getFile", function()
        return clip:getFile()
    end)
    local playOk, handle = attempt("vehicle emitter:playClip(registered clip)", function()
        return emitter:playClip(clip, vehicle)
    end)
    if playOk then rememberEmitterPlayback(emitter, handle) end
    log("registered GameSoundClip vehicle test finished")
end

local function runHttpCapabilityReport()
    log("HTTP capability report started")
    log("type(getUrlInputStream)=" .. type(getUrlInputStream))
    log("client-side HTTP download is unavailable in B42.20.2 multiplayer Lua")
    log("HTTP capability report finished")
end

local function onKeyPressed(key)
    if key == KEY_HTTP_REPORT then
        runHttpCapabilityReport()
    elseif key == KEY_REGISTERED_CLIP then
        runRegisteredClipTest()
    elseif key == KEY_NAMED_WORLD then
        runNamedWorldTest()
    elseif key == KEY_VEHICLE_NAMED then
        runVehicleNamedTest()
    end
end

Events.OnGameStart.Add(function()
    log("0.8.4 B42 single-client server voice-injection probe control loaded")
    log("F8=report HTTP availability only; F9=packaged clip in vehicle")
    log("F10=packaged world; F11=packaged vehicle")
    attempt("GameSounds.isKnownSound(" .. SOUND_NAME .. ")", function()
        return GameSounds.isKnownSound(SOUND_NAME)
    end)
end)
Events.OnKeyPressed.Add(onKeyPressed)

if Events.OnGameExit then
    Events.OnGameExit.Add(function()
        if not lastWorldAudio and not lastEmitterHandle then return end
        local ok, manager = pcall(getSoundManager)
        if ok and manager then stopPrevious(manager) end
    end)
end
