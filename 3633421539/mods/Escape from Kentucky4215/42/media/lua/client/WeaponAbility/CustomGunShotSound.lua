require "Gun_Vars/Weapon_Ability/AWCWF_Gun_Shot_Profiles"
require "AWCWF_Mod_Options"

local function getGunshotProfile(weapon)
    if AWCWF_GunShotProfiles[weapon:getType()] then
        return AWCWF_GunShotProfiles[weapon:getType()]
    end
end

local function hasSuppressor(weapon)
    if not AWCWF_SilencerSet then
        return false
    end

    for partType, silencerSet in pairs(AWCWF_SilencerSet) do
        local part = weapon:getWeaponPart(partType)
        if part and silencerSet[part:getType()] then
            return true
        end
    end

    return false
end

local function hasShootableRound(weapon)
    if weapon:getCurrentAmmoCount() and weapon:getCurrentAmmoCount() > 0 then
        return true
    end

    if weapon.isRoundChambered and weapon:isRoundChambered() then
        return true
    end

    return false
end

local function getRemainingAmmoAfterShot(weapon)
    local currentAmmo = 0
    if weapon.getCurrentAmmoCount and weapon:getCurrentAmmoCount() then
        currentAmmo = weapon:getCurrentAmmoCount()
    end

    if currentAmmo > 0 then
        return currentAmmo - 1
    end

    if weapon.isRoundChambered and weapon:isRoundChambered() then
        return 0
    end

    return currentAmmo
end

local function getMaxAmmo(weapon)
    if weapon.getMaxAmmo and weapon:getMaxAmmo() then
        return weapon:getMaxAmmo()
    end

    return 0
end

local function getLowAmmoWarning(profile, weapon)
    local lowAmmo = profile.lowAmmo
    if not lowAmmo then
        return nil
    end

    local maxAmmo = getMaxAmmo(weapon)
    if maxAmmo <= 0 then
        return nil
    end

    local remainingAmmo = getRemainingAmmoAfterShot(weapon)
    local warningLimit = math.max(1, math.floor(maxAmmo * lowAmmo.lastRoundsPercent))
    if remainingAmmo > warningLimit then
        return nil
    end

    for _, warning in ipairs(lowAmmo.sounds) do
        local threshold = math.floor(maxAmmo * warning.maxRemainingPercent)
        if remainingAmmo <= threshold then
            return warning
        end
    end

    return nil
end

local function playLowAmmoWarning(emitter, profile, weapon)
    local warning = getLowAmmoWarning(profile, weapon)
    if warning then
        local sound = emitter:playSound(warning.soundName)
        if warning.volume and sound then
            emitter:setVolume(sound, warning.volume)
        end
    end
end

local function playLayers(emitter, layers)
    for _, soundName in ipairs(layers) do
        emitter:playSound(soundName)
    end
end

local function TriggerGunShot(playerObj, weapon)
    if not weapon or not weapon:IsWeapon() or not weapon:isRanged() then
        return
    end

    if not AWCWF_Options.isNewGunshotEnabled() then
        return
    end

    local profile = getGunshotProfile(weapon)
    if not profile then
        return
    end

    weapon:setSwingSound("nil")

    local emitter = playerObj:getEmitter()
    if not hasShootableRound(weapon) then
        playLayers(emitter, profile.triggerLayers)
        return
    end

    if hasSuppressor(weapon) then
        playLayers(emitter, profile.suppressorLayers)
    else
        playLayers(emitter, profile.normalLayers)
    end

    -- if weapon.isAutomatic and weapon:isAutomatic() then
    playLowAmmoWarning(emitter, profile, weapon)
    -- end
end

Events.OnWeaponSwing.Add(TriggerGunShot)
