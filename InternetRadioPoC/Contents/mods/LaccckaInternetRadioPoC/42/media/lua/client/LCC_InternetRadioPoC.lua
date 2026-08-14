-- Registered GameSound and vehicle-emitter probe for Project Zomboid B42.20.2.
-- No OnTick callback, vehicle scan, HTTP request, luajava, or external loader.

local MOD_TAG = "[LCC Internet Radio PoC]"
local SOUND_NAME = "LCCInternetRadioTest"
local DOWNLOADED_SOUND_NAME = "LCCInternetRadioDownloadedTest"
local DOWNLOAD_URL = "https://raw.githubusercontent.com/ArtskydJ/test-audio/master/audio/75344__neotone__drip2.wav"
local CACHE_WAV_RELATIVE = "LCCInternetRadioPoC/downloaded-test.wav"
local CACHE_SCRIPT_RELATIVE = "LCCInternetRadioPoC/downloaded-sound.txt"

-- LWJGL/PZ key codes. Numeric constants avoid depending on an exposed
-- Keyboard Java class in the restricted multiplayer Lua environment.
local KEY_DOWNLOAD_AND_PLAY = 66 -- F8
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

local function closeQuietly(stream, label)
    if not stream then return end
    local ok, errorText = pcall(function()
        stream:close()
    end)
    if not ok then log(label .. ": close failed; " .. describe(errorText)) end
end

local function getLuaCacheAbsolutePath(relativePath)
    local rootOk, root = attempt("getMyDocumentFolder", function()
        return getMyDocumentFolder()
    end)
    if not rootOk or not root then return nil end
    local normalizedRoot = tostring(root):gsub("\\", "/")
    return normalizedRoot .. "/Lua/" .. relativePath
end

local function downloadBinaryFile(url, relativePath)
    log("download URL=" .. url)
    local inputOk, input = attempt("getUrlInputStream", function()
        return getUrlInputStream(url)
    end)
    if not inputOk or not input then return false end

    local bytesOk, bytes = attempt("input:readAllBytes", function()
        return input:readAllBytes()
    end)
    closeQuietly(input, "download input")
    if not bytesOk or not bytes then return false end

    local outputOk, output = attempt("getFileOutput", function()
        return getFileOutput(relativePath)
    end)
    if not outputOk or not output then return false end

    local writeOk = attempt("output:write(downloaded bytes)", function()
        output:write(bytes)
        output:flush()
        return true
    end)
    closeQuietly(output, "download output")
    if not writeOk then return false end

    local verifyOk, verificationInput = attempt("getFileInput(downloaded WAV)", function()
        return getFileInput(relativePath)
    end)
    if verifyOk and verificationInput then
        closeQuietly(verificationInput, "download verification input")
        return true
    end
    return false
end

local function writeDynamicSoundScript(scriptPath, wavAbsolutePath)
    local writerOk, writer = attempt("getFileWriter(dynamic sound script)", function()
        return getFileWriter(scriptPath, true, false)
    end)
    if not writerOk or not writer then return false end

    local soundFile = tostring(wavAbsolutePath):gsub("\\", "/")
    local scriptText =
        "module Base\n" ..
        "{\n" ..
        "    sound " .. DOWNLOADED_SOUND_NAME .. "\n" ..
        "    {\n" ..
        "        category = Vehicle,\n" ..
        "        loop = true,\n" ..
        "        is3D = true,\n" ..
        "        clip\n" ..
        "        {\n" ..
        "            file = " .. soundFile .. ",\n" ..
        "            volume = 1.0,\n" ..
        "            distanceMin = 3,\n" ..
        "            distanceMax = 30,\n" ..
        "        }\n" ..
        "    }\n" ..
        "}\n"

    local writeOk = attempt("write dynamic sound script", function()
        writer:write(scriptText)
        writer:close()
        return true
    end)
    if not writeOk then closeQuietly(writer, "dynamic script writer") end
    return writeOk
end

local function runDownloadedVehicleTest()
    log("automatic WAV download and dynamic vehicle playback test started")
    local manager = getManager()
    if not manager then return end
    stopPrevious(manager)

    local _, emitter = getVehicleAndEmitter()
    if not emitter then return end

    local wavAbsolutePath = getLuaCacheAbsolutePath(CACHE_WAV_RELATIVE)
    local scriptAbsolutePath = getLuaCacheAbsolutePath(CACHE_SCRIPT_RELATIVE)
    if not wavAbsolutePath or not scriptAbsolutePath then
        log("download test stopped: Lua cache paths are unavailable")
        return
    end
    log("download target=" .. wavAbsolutePath)
    log("dynamic script=" .. scriptAbsolutePath)

    if not downloadBinaryFile(DOWNLOAD_URL, CACHE_WAV_RELATIVE) then
        log("download test stopped: WAV download/write failed")
        return
    end
    if not writeDynamicSoundScript(CACHE_SCRIPT_RELATIVE, wavAbsolutePath) then
        log("download test stopped: dynamic sound script write failed")
        return
    end

    local reloadOk = attempt("GameSounds.ReloadFile(dynamic script)", function()
        GameSounds.ReloadFile(scriptAbsolutePath)
        return true
    end)
    if not reloadOk then return end

    local knownOk, known = attempt("GameSounds.isKnownSound(downloaded)", function()
        return GameSounds.isKnownSound(DOWNLOADED_SOUND_NAME)
    end)
    if not knownOk or known ~= true then
        log("download test stopped: dynamic GameSound was not registered")
        return
    end

    local _, dynamicSound = attempt("GameSounds.getSound(downloaded)", function()
        return GameSounds.getSound(DOWNLOADED_SOUND_NAME)
    end)
    if dynamicSound then
        local _, dynamicClip = attempt("downloaded sound:getRandomClip", function()
            return dynamicSound:getRandomClip()
        end)
        if dynamicClip then
            attempt("downloaded clip:getFile", function()
                return dynamicClip:getFile()
            end)
        end
    end

    local playOk, handle = attempt("vehicle emitter:playSound(downloaded name)", function()
        return emitter:playSound(DOWNLOADED_SOUND_NAME)
    end)
    if playOk then rememberEmitterPlayback(emitter, handle) end
    log("automatic WAV download and dynamic vehicle playback test finished")
end

local function onKeyPressed(key)
    if key == KEY_DOWNLOAD_AND_PLAY then
        runDownloadedVehicleTest()
    elseif key == KEY_REGISTERED_CLIP then
        runRegisteredClipTest()
    elseif key == KEY_NAMED_WORLD then
        runNamedWorldTest()
    elseif key == KEY_VEHICLE_NAMED then
        runVehicleNamedTest()
    end
end

Events.OnGameStart.Add(function()
    log("0.6.0 automatic WAV download and dynamic GameSound probe loaded")
    log("F8=download WAV and play in vehicle; F9=packaged clip in vehicle")
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
