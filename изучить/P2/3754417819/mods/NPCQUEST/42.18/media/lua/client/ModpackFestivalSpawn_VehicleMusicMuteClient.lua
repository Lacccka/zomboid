-- Mute vanilla soundtrack (SoundManager music channel) while driving.
-- Mod audio (Restless Dreams car radio, festival BWORadio emitters) is unchanged.

if isServer() and not isClient() then return end

ModpackFestivalVehicleMusicMute = ModpackFestivalVehicleMusicMute or {}

local VMM = ModpackFestivalVehicleMusicMute
local MOD_ID = "ModpackFestivalSpawn"

VMM.savedMusicVolume = nil
VMM.active = false
VMM.tick = 0

local function soundManager()
    return getSoundManager and getSoundManager() or nil
end

function VMM.isPlayerInVehicle(player)
    if not player or (player.isDead and player:isDead()) then
        return false
    end
    return player.getVehicle and player:getVehicle() ~= nil
end

function VMM.enterVehicle()
    if VMM.active then
        return
    end
    local sm = soundManager()
    if not sm then
        return
    end
    if sm.getMusicVolume then
        VMM.savedMusicVolume = sm:getMusicVolume()
    else
        VMM.savedMusicVolume = nil
    end
    if sm.setMusicVolume then
        sm:setMusicVolume(0)
    end
    if sm.StopMusic then
        sm:StopMusic()
    end
    VMM.active = true
end

function VMM.leaveVehicle()
    if not VMM.active then
        return
    end
    local sm = soundManager()
    if sm and sm.setMusicVolume then
        local restore = VMM.savedMusicVolume
        if restore == nil and getCore and getCore() then
            local core = getCore()
            if core.getOptionMusicVolume then
                restore = core:getOptionMusicVolume() / 10
            end
        end
        if restore ~= nil then
            sm:setMusicVolume(restore)
        end
    end
    VMM.active = false
    VMM.savedMusicVolume = nil
end

function VMM.maintainMute()
    if not VMM.active then
        return
    end
    local sm = soundManager()
    if not sm then
        return
    end
    if sm.getMusicVolume and sm.setMusicVolume then
        if sm:getMusicVolume() > 0.001 then
            sm:setMusicVolume(0)
        end
    end
    if sm.isPlayingMusic and sm.StopMusic and sm:isPlayingMusic() then
        sm:StopMusic()
    end
end

local function onTick()
    VMM.tick = (VMM.tick or 0) + 1
    local interval = (ModpackFestivalTick and ModpackFestivalTick.UI_FAST) or 15
    if ModpackFestivalTick and ModpackFestivalTick.every then
        if not ModpackFestivalTick.every(VMM.tick, interval) then
            return
        end
    elseif (VMM.tick % interval) ~= 0 then
        return
    end

    local player = getSpecificPlayer and getSpecificPlayer(0)
    if not player then
        if VMM.active then
            VMM.leaveVehicle()
        end
        return
    end

    if VMM.isPlayerInVehicle(player) then
        VMM.enterVehicle()
        VMM.maintainMute()
    else
        VMM.leaveVehicle()
    end
end

Events.OnTick.Add(onTick)

Events.OnGameStart.Add(function()
    VMM.leaveVehicle()
end)

Events.OnPlayerDeath.Add(function(player)
    if player == getSpecificPlayer(0) then
        VMM.leaveVehicle()
    end
end)

print("[" .. MOD_ID .. "] vanilla music muted while in vehicles (mod radio unchanged)")
