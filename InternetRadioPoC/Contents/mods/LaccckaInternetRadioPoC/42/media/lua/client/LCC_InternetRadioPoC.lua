-- Local WAV playback probe for Project Zomboid B42.20.2.
-- No HTTP, vehicle scan, OnTick callback, luajava, or external loader.

local MOD_TAG = "[LCC Internet Radio PoC]"
local MOD_ID = "LaccckaInternetRadioPoC"
local VERSION_DIR = "42"
local SOUND_NAME = "test"
local SOUND_RELATIVE_PATH = "media/sound/test.wav"

-- LWJGL/PZ key codes. Numeric constants avoid depending on an exposed
-- Keyboard Java class in the restricted multiplayer Lua environment.
local KEY_DIRECT_WORLD = 66 -- F8
local KEY_DIRECT_2D = 67 -- F9
local KEY_NAMED_WORLD = 68 -- F10
local KEY_VEHICLE_NAMED = 87 -- F11
local KEY_VEHICLE_DIRECT = 88 -- F12

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

local function getAbsoluteWavPath()
    local ok, modInfo = attempt("getModInfoByID", function()
        return getModInfoByID(MOD_ID)
    end)
    if not ok or not modInfo then return nil end

    local dirOk, modDir = attempt("modInfo:getDir", function()
        return modInfo:getDir()
    end)
    if not dirOk or not modDir then return nil end
    -- getDir() returns .../mods/LaccckaInternetRadioPoC, while B42 versioned
    -- content lives below the additional /42 directory.
    return tostring(modDir) .. "/" .. VERSION_DIR .. "/" .. SOUND_RELATIVE_PATH
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

local function runDirectWorldTest()
    log("WAV corrected absolute-path world test started")
    local manager = getManager()
    if not manager then return end
    stopPrevious(manager)

    local wavPath = getAbsoluteWavPath()
    if not wavPath then
        log("absolute-path world test stopped: mod WAV path could not be resolved")
        return
    end
    log("file=" .. wavPath)

    local _, square = getPlayerAndSquare()
    if not square then
        log("absolute-path world test stopped: no player square")
        return
    end

    attempt("CacheSound(absolute path)", function()
        return manager:CacheSound(wavPath)
    end)
    local playOk, audio = attempt("PlayWorldSoundWav(absolute path)", function()
        return manager:PlayWorldSoundWav(wavPath, false, square, 0.0, 30.0, 1.0, false)
    end)
    if playOk and audio then lastWorldAudio = audio end
    log("WAV corrected absolute-path world test finished")
end

local function runDirect2DTest()
    log("WAV corrected absolute-path 2D test started")
    local manager = getManager()
    if not manager then return end
    stopPrevious(manager)

    local wavPath = getAbsoluteWavPath()
    if not wavPath then
        log("absolute-path 2D test stopped: mod WAV path could not be resolved")
        return
    end
    log("file=" .. wavPath)

    attempt("CacheSound(absolute path)", function()
        return manager:CacheSound(wavPath)
    end)
    local playOk, audio = attempt("PlaySoundWav(absolute path)", function()
        return manager:PlaySoundWav(wavPath, false, 1.0)
    end)
    if playOk and audio then lastWorldAudio = audio end
    log("WAV corrected absolute-path 2D test finished")
end

local function runNamedWorldTest()
    log("WAV named positional-world control test started")
    local manager = getManager()
    if not manager then return end
    stopPrevious(manager)

    local _, square = getPlayerAndSquare()
    if not square then
        log("named positional-world test stopped: no player square")
        return
    end

    attempt("CacheSound(relative path)", function()
        return manager:CacheSound(SOUND_RELATIVE_PATH)
    end)
    local playOk, audio = attempt("PlayWorldSoundWav(name)", function()
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
    log("WAV named positional-world control test finished")
end

local function runVehicleEmitterTest(useAbsolutePath)
    local testName = useAbsolutePath and "absolute-path" or "named-file"
    log("WAV vehicle-emitter " .. testName .. " test started")
    local manager = getManager()
    if not manager then return end
    stopPrevious(manager)

    local player = getPlayerAndSquare()
    if not player then
        log("vehicle-emitter test stopped: no player")
        return
    end

    local vehicleOk, vehicle = attempt("player:getVehicle", function()
        return player:getVehicle()
    end)
    if not vehicleOk or not vehicle then
        log("vehicle-emitter test stopped: sit inside a vehicle first")
        return
    end

    local emitterOk, emitter = attempt("vehicle:getEmitter", function()
        return vehicle:getEmitter()
    end)
    if not emitterOk or not emitter then
        log("vehicle-emitter test stopped: vehicle has no emitter")
        return
    end

    local sound = SOUND_NAME
    if useAbsolutePath then
        sound = getAbsoluteWavPath()
        if not sound then
            log("vehicle-emitter test stopped: mod WAV path could not be resolved")
            return
        end
        attempt("CacheSound(absolute path)", function()
            return manager:CacheSound(sound)
        end)
    end
    log("emitter sound=" .. tostring(sound))

    local playOk, handle = attempt("vehicle emitter:playSound", function()
        return emitter:playSound(sound)
    end)
    if playOk and handle and handle ~= 0 then
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
    log("WAV vehicle-emitter " .. testName .. " test finished")
end

local function onKeyPressed(key)
    if key == KEY_DIRECT_WORLD then
        runDirectWorldTest()
    elseif key == KEY_DIRECT_2D then
        runDirect2DTest()
    elseif key == KEY_NAMED_WORLD then
        runNamedWorldTest()
    elseif key == KEY_VEHICLE_NAMED then
        runVehicleEmitterTest(false)
    elseif key == KEY_VEHICLE_DIRECT then
        runVehicleEmitterTest(true)
    end
end

Events.OnGameStart.Add(function()
    log("0.4.1 local WAV and vehicle-emitter probe loaded")
    log("F8=corrected absolute world; F9=corrected absolute 2D; F10=named world control")
    log("F11=vehicle emitter by name; F12=vehicle emitter by absolute path")
end)
Events.OnKeyPressed.Add(onKeyPressed)

if Events.OnGameExit then
    Events.OnGameExit.Add(function()
        if not lastWorldAudio and not lastEmitterHandle then return end
        local ok, manager = pcall(getSoundManager)
        if ok and manager then stopPrevious(manager) end
    end)
end
