-- Title-screen + world-load screen: "Night 1" (No, I'm Not a Human OST).
-- Vanilla FMOD menu/intro music and SFX are muted; our UI track keeps playing.

if isServer() and not isClient() then return end

ModpackFestivalMainMenuMusic = ModpackFestivalMainMenuMusic or {}
local MMM = ModpackFestivalMainMenuMusic

local MOD_ID = "ModpackFestivalSpawn"
MMM.SOUND = "ModpackFestivalMainMenuNight1"
MMM.emitter = nil
MMM.soundId = nil
MMM.savedMusicVolume = nil
MMM.savedSoundVolume = nil
MMM.active = false
MMM.worldLoading = false
MMM.tick = 0
MMM.worldTick = 0
MMM.enteredWorld = false
MMM.forceWorldStopTicks = 0

local function soundManager()
    return getSoundManager and getSoundManager() or nil
end

function MMM.isInActiveWorld()
    if MMM.enteredWorld then
        return true
    end
    if isIngameState and isIngameState() then
        return true
    end
    local player = getSpecificPlayer and getSpecificPlayer(0)
    if player and player.getSquare then
        local ok, sq = pcall(function()
            return player:getSquare()
        end)
        if ok and sq then
            return true
        end
    end
    return false
end

function MMM.isTitleMainMenu()
    if MMM.isInActiveWorld() then
        return false
    end
    if not MainScreen or not MainScreen.instance then
        return false
    end
    if MainScreen.instance.inGame then
        return false
    end
    if MainScreen.instance.isVisible and not MainScreen.instance:isVisible() then
        return false
    end
    return true
end

function MMM.suppressVanillaMenuMusic()
    local sm = soundManager()
    if not sm then
        return
    end
    if sm.StopMusic then
        pcall(function()
            sm:StopMusic()
        end)
    end
    if sm.setMusicVolume and sm.getMusicVolume then
        if MMM.savedMusicVolume == nil then
            MMM.savedMusicVolume = sm:getMusicVolume()
        end
        sm:setMusicVolume(0)
    end
end

function MMM.restoreVanillaMenuMusic()
    local sm = soundManager()
    if not sm then
        return
    end
    if sm.setMusicVolume and MMM.savedMusicVolume ~= nil then
        sm:setMusicVolume(MMM.savedMusicVolume)
    end
    MMM.savedMusicVolume = nil
end

function MMM.restoreVanillaGameAudio()
    MMM.restoreVanillaMenuMusic()
    local sm = soundManager()
    if sm and sm.setSoundVolume and MMM.savedSoundVolume ~= nil then
        pcall(function()
            sm:setSoundVolume(MMM.savedSoundVolume)
        end)
    end
    MMM.savedSoundVolume = nil
end

function MMM.shouldPlayCustomMenuMusic()
    if MMM.isInActiveWorld() then
        return false
    end
    return MMM.isTitleMainMenu()
end

function MMM.suppressVanillaGameAudio()
    MMM.suppressVanillaMenuMusic()
    if not MMM.worldLoading then
        return
    end
    local sm = soundManager()
    if not sm then
        return
    end
    if sm.setSoundVolume and sm.getSoundVolume then
        if MMM.savedSoundVolume == nil then
            MMM.savedSoundVolume = sm:getSoundVolume()
        end
        pcall(function()
            sm:setSoundVolume(0)
        end)
    end
end

function MMM.menuTrackVolume()
    if MMM.savedSoundVolume ~= nil then
        return MMM.savedSoundVolume * 0.9
    end
    local sm = soundManager()
    if sm and sm.getSoundVolume then
        return (sm:getSoundVolume() or 1) * 0.9
    end
    return 0.9
end

function MMM.refreshMenuTrackVolume()
    if not MMM.emitter or not MMM.soundId then
        return
    end
    pcall(function()
        MMM.emitter:setVolume(MMM.soundId, MMM.menuTrackVolume())
    end)
end

function MMM.isOurTrackPlaying()
    if not MMM.emitter or not MMM.soundId then
        return false
    end
    if MMM.emitter.isPlaying then
        local ok, playing = pcall(function()
            return MMM.emitter:isPlaying(MMM.soundId)
        end)
        return ok and playing == true
    end
    return false
end

local function stopPlaybackOnly()
    if MMM.emitter and MMM.soundId then
        pcall(function()
            MMM.emitter:stopSound(MMM.soundId)
        end)
    end
    MMM.emitter = nil
    MMM.soundId = nil
    MMM.active = false
end

local function stopUiMenuSoundByNameOnly()
    local sm = soundManager()
    if not sm or not sm.getUIEmitter then
        return
    end
    local emitter = sm:getUIEmitter()
    if not emitter then
        return
    end
    -- stopOrTriggerSoundByName intentionally excluded: it can re-play the sound
    -- if a prior stop already silenced it, causing the music to leak into the world.
    if emitter.stopSoundByName then
        pcall(function()
            emitter:stopSoundByName(MMM.SOUND)
        end)
    end
    -- Nuclear fallback: stopAll kills any orphaned instance whose ID we lost on Lua reset.
    -- Safe here because the UI emitter only carries our menu track.
    if emitter.stopAll then
        pcall(function()
            emitter:stopAll()
        end)
    end
end

-- UI emitter sounds survive Lua reset; stop by name so orphaned copies cannot stack.
function MMM.killUiMenuSound()
    stopPlaybackOnly()
    local sm = soundManager()
    if not sm then
        return
    end
    if sm.StopMusic then
        pcall(function()
            sm:StopMusic()
        end)
    end
    if sm.getUIEmitter then
        local emitter = sm:getUIEmitter()
        if emitter then
            if emitter.stopSoundByName then
                pcall(function()
                    emitter:stopSoundByName(MMM.SOUND)
                end)
            end
            -- stopAll as last resort: catches orphaned IDs lost across Lua resets
            if emitter.stopAll then
                pcall(function()
                    emitter:stopAll()
                end)
            end
        end
    end
end

function MMM.stop()
    MMM.worldLoading = false
    MMM.killUiMenuSound()
    MMM.restoreVanillaGameAudio()
end

-- Always stop first so playback begins at the start of the track.
function MMM.playFromStart()
    local sm = soundManager()
    if not sm or not sm.getUIEmitter then
        return false
    end

    MMM.killUiMenuSound()
    MMM.suppressVanillaMenuMusic()

    local emitter = sm:getUIEmitter()
    if not emitter then
        return false
    end
    emitter:setPos(0, 0, 0)

    local soundId = emitter:playSound(MMM.SOUND)
    if not soundId then
        return false
    end

    emitter:setVolume(soundId, MMM.menuTrackVolume())

    MMM.emitter = emitter
    MMM.soundId = soundId
    MMM.active = true
    return true
end

function MMM.onMainMenuEnter()
    MMM.enteredWorld = false
    MMM.forceWorldStopTicks = 0
    if MMM.isTitleMainMenu() then
        MMM.playFromStart()
    end
end

function MMM.maintain()
    if not MMM.shouldPlayCustomMenuMusic() then
        if MMM.active or MMM.emitter then
            MMM.stop()
        end
        return
    end

    MMM.suppressVanillaGameAudio()
    MMM.refreshMenuTrackVolume()

    if not MMM.isOurTrackPlaying() then
        MMM.playFromStart()
    end
end

function MMM.onGameStateEnter(javaStateObj)
    if not javaStateObj or not instanceof then
        return
    end
    if instanceof(javaStateObj, "GameLoadingState") then
        -- Character creation -> loading transition must be silent from this mod.
        MMM.worldLoading = false
        MMM.stop()
        return
    end
    if MMM.worldLoading then
        MMM.worldLoading = false
    end
    if MMM.isInActiveWorld()
        or (javaStateObj and instanceof(javaStateObj, "IngameState")) then
        MMM.stop()
        return
    end
    if MMM.active or MMM.emitter then
        MMM.stop()
    end
end

function MMM.onPreUIDraw()
    if MMM.isInActiveWorld() then
        if MMM.active or MMM.emitter or MMM.worldLoading then
            MMM.stop()
        end
        return
    end
    MMM.tick = (MMM.tick or 0) + 1
    if MMM.worldLoading then
        MMM.suppressVanillaGameAudio()
        MMM.refreshMenuTrackVolume()
    end
    if (MMM.tick % 15) ~= 0 and not MMM.worldLoading then
        return
    end
    MMM.patchResetLuaButton()
    MMM.maintain()
end

function MMM.onLeaveMainMenu()
    MMM.stop()
end

-- Safety net: occasionally state/UI events miss during world transition.
-- If a world player exists, hard-stop menu music regardless of menu flags.
function MMM.enforceStopInWorld()
    if not MMM.isInActiveWorld() then
        return
    end
    -- Handle both tracked playback and orphaned UI sounds (common on Continue Save).
    stopPlaybackOnly()
    stopUiMenuSoundByNameOnly()
    MMM.worldLoading = false
    MMM.restoreVanillaGameAudio()
end

function MMM.markWorldEntered()
    MMM.enteredWorld = true
    MMM.worldLoading = false
    MMM.forceWorldStopTicks = math.max(MMM.forceWorldStopTicks or 0, 600)
    MMM.killUiMenuSound()
    MMM.restoreVanillaGameAudio()
end

function MMM.onWorldTick()
    MMM.worldTick = (MMM.worldTick or 0) + 1
    if (MMM.forceWorldStopTicks or 0) > 0 then
        MMM.forceWorldStopTicks = MMM.forceWorldStopTicks - 1
        MMM.killUiMenuSound()
        MMM.restoreVanillaGameAudio()
        return
    end
    if not ModpackFestivalTick.every(MMM.worldTick, ModpackFestivalTick.GAME) then
        return
    end
    MMM.enforceStopInWorld()
end

function MMM.onResetLua()
    MMM.savedMusicVolume = nil
    MMM.savedSoundVolume = nil
    MMM.worldLoading = false
    MMM.enteredWorld = false
    MMM.forceWorldStopTicks = 0
    MMM.tick = 0
    MMM.worldTick = 0
    -- clear Java refs before calling stop — during Lua reset the emitter may be stale
    MMM.emitter = nil
    MMM.soundId = nil
    MMM.active = false
    pcall(MMM.killUiMenuSound)
end

-- Stop before ResetLua while the old script still runs (same pattern as BWOAMusic).
function MMM.patchResetLuaButton()
    if not MainScreen or not MainScreen.instance or not MainScreen.instance.resetLua then
        return
    end
    local btn = MainScreen.instance.resetLua
    if btn._mmmResetPatched then
        return
    end
    local prevOnclick = btn.onclick
    btn.onclick = function(target, x, y)
        MMM.killUiMenuSound()
        if prevOnclick then
            return prevOnclick(target, x, y)
        end
    end
    btn._mmmResetPatched = true
end

local function registerEvent(event, key, fn)
    if MMM[key] then
        event.Remove(MMM[key])
    end
    MMM[key] = fn
    event.Add(MMM[key])
end

registerEvent(Events.OnMainMenuEnter, "_evMainMenuEnter", MMM.onMainMenuEnter)
registerEvent(Events.OnPreUIDraw, "_evPreUIDraw", MMM.onPreUIDraw)
if Events.OnGameStateEnter then
    registerEvent(Events.OnGameStateEnter, "_evGameStateEnter", MMM.onGameStateEnter)
end
registerEvent(Events.OnGameStart, "_evGameStart", function()
    MMM.markWorldEntered()
end)
registerEvent(Events.OnGameBoot, "_evGameBoot", MMM.onLeaveMainMenu)
registerEvent(Events.OnTick, "_evTick", MMM.onWorldTick)
if Events.OnCreatePlayer then
    registerEvent(Events.OnCreatePlayer, "_evCreatePlayer", function(playerIndex)
        if playerIndex == 0 then
            MMM.markWorldEntered()
        end
    end)
end
if Events.OnLoad then
    registerEvent(Events.OnLoad, "_evLoad", MMM.enforceStopInWorld)
end
if Events.OnResetLua then
    registerEvent(Events.OnResetLua, "_evResetLua", MMM.onResetLua)
end

print("[" .. MOD_ID .. "] main menu music: Night 1 (mutes vanilla audio on title + world load)")
