-- Local WAV playback probe for Project Zomboid B42.20.2.
-- No HTTP, vehicle scan, OnTick callback, luajava, or external loader.

local MOD_TAG = "[LCC Internet Radio PoC]"
local MOD_ID = "LaccckaInternetRadioPoC"
local SOUND_NAME = "test"
local SOUND_RELATIVE_PATH = "media/sound/test.wav"

-- LWJGL/PZ key codes. Numeric constants avoid depending on an exposed
-- Keyboard Java class in the restricted multiplayer Lua environment.
local KEY_DIRECT_PATH = 66 -- F8
local KEY_NAMED_WAV = 67 -- F9
local KEY_WORLD_WAV = 68 -- F10

local lastAudio = nil

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
    return tostring(modDir) .. "/" .. SOUND_RELATIVE_PATH
end

local function stopPrevious(manager)
    if not lastAudio then return end
    attempt("StopSound(previous)", function()
        manager:StopSound(lastAudio)
    end)
    lastAudio = nil
end

local function runDirectPathTest()
    log("WAV direct-path test started")
    local manager = getManager()
    if not manager then return end
    stopPrevious(manager)

    local wavPath = getAbsoluteWavPath()
    if not wavPath then
        log("direct-path test stopped: mod WAV path could not be resolved")
        return
    end
    log("file=" .. wavPath)

    attempt("CacheSound(absolute path)", function()
        return manager:CacheSound(wavPath)
    end)
    local _, audio = attempt("PlaySoundWav(absolute path)", function()
        return manager:PlaySoundWav(wavPath, false, 1.0)
    end)
    lastAudio = audio
    log("WAV direct-path test finished")
end

local function runNamedWavTest()
    log("WAV named-file test started")
    local manager = getManager()
    if not manager then return end
    stopPrevious(manager)
    log("file=" .. SOUND_RELATIVE_PATH .. "; name=" .. SOUND_NAME)

    attempt("CacheSound(relative path)", function()
        return manager:CacheSound(SOUND_RELATIVE_PATH)
    end)
    local _, audio = attempt("PlaySoundWav(name)", function()
        return manager:PlaySoundWav(SOUND_NAME, false, 1.0)
    end)
    lastAudio = audio
    log("WAV named-file test finished")
end

local function runWorldWavTest()
    log("WAV positional-world test started")
    local manager = getManager()
    if not manager then return end
    stopPrevious(manager)

    local playerOk, player = attempt("getPlayer", function()
        return getPlayer()
    end)
    if not playerOk or not player then
        log("positional-world test stopped: no player")
        return
    end

    local squareOk, square = attempt("player:getSquare", function()
        return player:getSquare()
    end)
    if not squareOk or not square then
        log("positional-world test stopped: no player square")
        return
    end

    attempt("CacheSound(relative path)", function()
        return manager:CacheSound(SOUND_RELATIVE_PATH)
    end)
    local _, audio = attempt("PlayWorldSoundWav(name)", function()
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
    lastAudio = audio
    log("WAV positional-world test finished")
end

local function onKeyPressed(key)
    if key == KEY_DIRECT_PATH then
        runDirectPathTest()
    elseif key == KEY_NAMED_WAV then
        runNamedWavTest()
    elseif key == KEY_WORLD_WAV then
        runWorldWavTest()
    end
end

Events.OnGameStart.Add(function()
    log("0.4.0 local WAV probe loaded")
    log("F8=absolute path; F9=unregistered WAV name; F10=positional world WAV")
end)
Events.OnKeyPressed.Add(onKeyPressed)

if Events.OnGameExit then
    Events.OnGameExit.Add(function()
        if not lastAudio then return end
        local manager = getSoundManager()
        if manager then pcall(function() manager:StopSound(lastAudio) end) end
        lastAudio = nil
    end)
end
