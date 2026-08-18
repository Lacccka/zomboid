AWCWF_Options = AWCWF_Options or {}

-- Keep controls out of PZAPI Mod Options. Register current and future keybinds
-- in AWCWF_KeyBind.lua so Options > Key Bindings remains the single source.

local SOUND_CATEGORY_SHOT = "AW_Weapon_Shot"
local SOUND_CATEGORY_RELOAD = "AW_Weapon_Reload"

function AWCWF_Options.isNewGunshotEnabled()
    local options = PZAPI.ModOptions:getOptions("AWCWF_42_Patch")
    if not options then
        return true
    end
    local opt = options:getOption("enable_new_gunshot")
    if not opt then
        return true
    end
    return opt:getValue()
end

local function getCategorySoundVolume(category)
    local sounds = GameSounds.getSoundsInCategory(category)
    local total = 0
    local count = 0

    if sounds then
        for i = 1, sounds:size() do
            local sound = sounds:get(i - 1)
            if sound then
                total = total + sound:getUserVolume()
                count = count + 1
            end
        end
    end

    if count == 0 then
        return 1
    end

    return total / count
end

local function applyCategorySoundVolume(category, volume)
    local changed = false
    local sounds = GameSounds.getSoundsInCategory(category)

    if sounds then
        for i = 1, sounds:size() do
            local sound = sounds:get(i - 1)
            if sound and sound:getUserVolume() ~= volume then
                sound:setUserVolume(volume)
                changed = true
            end
        end
    end

    if changed then
        GameSounds.saveINI()
    end
end

function AWCWF_Options.Init()
    local options = PZAPI.ModOptions:create("AWCWF_42_Patch", getText("UI_options_AWCWF_title"))

    options:addTitle(getText("UI_options_AWCWF_section_general"))
    options:addDescription(getText("UI_options_AWCWF_desc_general"))

    options:addTitle(getText("UI_options_AWCWF_section_weapon_sounds"))
    options:addDescription(getText("UI_options_AWCWF_desc_weapon_sounds"))

    local enableNewGunshot = options:addTickBox("enable_new_gunshot", getText("UI_options_AWCWF_enable_new_gunshot"),
        true, getText("UI_options_AWCWF_enable_new_gunshot_tooltip"))

    local weaponShotVolume = options:addSlider("weapon_shot_volume", getText("UI_options_AWCWF_weapon_shot_volume"), 0,
        2, 0.01, getCategorySoundVolume(SOUND_CATEGORY_SHOT), getText("UI_options_AWCWF_weapon_shot_volume_tooltip"))

    weaponShotVolume.onChange = function(_, volume)
        applyCategorySoundVolume(SOUND_CATEGORY_SHOT, volume)
    end

    weaponShotVolume.onChangeApply = function(_, volume)
        applyCategorySoundVolume(SOUND_CATEGORY_SHOT, volume)
    end

    local weaponReloadVolume = options:addSlider("weapon_reload_volume",
        getText("UI_options_AWCWF_weapon_reload_volume"), 0, 2, 0.01, getCategorySoundVolume(SOUND_CATEGORY_RELOAD),
        getText("UI_options_AWCWF_weapon_reload_volume_tooltip"))

    weaponReloadVolume.onChange = function(_, volume)
        applyCategorySoundVolume(SOUND_CATEGORY_RELOAD, volume)
    end

    weaponReloadVolume.onChangeApply = function(_, volume)
        applyCategorySoundVolume(SOUND_CATEGORY_RELOAD, volume)
    end
end

AWCWF_Options.Init()
SystemDisabler.setEnableAdvancedSoundOptions(true)
