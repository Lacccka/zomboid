require "PZAPI/ModOptions"

NMClientModOptions = NMClientModOptions or {}

local MOD_OPTIONS_ID = "newmusic"
local OPTION_ID_MASTER_VOLUME = "masterVolume"
local OPTION_DEFAULT_MASTER_VOLUME = 1.0
local OPTION_ID_SHOW_TRACK_NUMBER_PREFIX = "showTrackNumberPrefix"
local OPTION_DEFAULT_SHOW_TRACK_NUMBER_PREFIX = true

local options = nil
local masterVolumeOption = nil
local showTrackNumberPrefixOption = nil

local function invalidateWindow(win)
    if not win then
        return
    end
    if win.invalidateContextCache then
        win:invalidateContextCache()
    end
    if win.invalidateSlotFrameModel then
        win:invalidateSlotFrameModel()
    end
    if win.invalidateRenderModel then
        win:invalidateRenderModel()
    end
end

local function invalidateWindowMap(windowsByPlayer)
    if type(windowsByPlayer) ~= "table" then
        return
    end
    for _, win in pairs(windowsByPlayer) do
        invalidateWindow(win)
    end
end

function NMClientModOptions.invalidateOpenWindows()
    local genericEnv = rawget(_G, "NMDeviceWindowEnv")
    local walkmanEnv = rawget(_G, "NMWalkmanWindowEnv")
    local cdPlayerEnv = rawget(_G, "NMCDPlayerWindowEnv")

    invalidateWindowMap(genericEnv and genericEnv.windowsByPlayer or nil)
    invalidateWindowMap(walkmanEnv and walkmanEnv.windowsByPlayer or nil)
    invalidateWindowMap(cdPlayerEnv and cdPlayerEnv.windowsByPlayer or nil)
end

function NMClientModOptions.getShowTrackNumberPrefix()
    if showTrackNumberPrefixOption and showTrackNumberPrefixOption.getValue then
        return showTrackNumberPrefixOption:getValue() ~= false
    end
    return OPTION_DEFAULT_SHOW_TRACK_NUMBER_PREFIX
end

function NMClientModOptions.getMasterVolume()
    if masterVolumeOption and masterVolumeOption.getValue then
        local value = tonumber(masterVolumeOption:getValue())
        if value ~= nil then
            return NMCore and NMCore.clamp and NMCore.clamp(value, 0, 1) or math.max(0, math.min(1, value))
        end
    end
    return OPTION_DEFAULT_MASTER_VOLUME
end

local function clampMasterVolume(value)
    local numeric = tonumber(value)
    if numeric == nil then
        numeric = OPTION_DEFAULT_MASTER_VOLUME
    end
    if NMCore and NMCore.clamp then
        return NMCore.clamp(numeric, 0, 1)
    end
    return math.max(0, math.min(1, numeric))
end

local function applyMasterVolume(value)
    local volume = clampMasterVolume(value)
    if NMRuntimeConfig and NMRuntimeConfig.setNewMusicMasterVolume then
        NMRuntimeConfig.setNewMusicMasterVolume(volume)
    end
end

local function applyShowTrackNumberPrefix(value)
    local enabled = value ~= false
    if NMRuntimeConfig and NMRuntimeConfig.setShowTrackNumberPrefix then
        NMRuntimeConfig.setShowTrackNumberPrefix(enabled)
    end
    NMClientModOptions.invalidateOpenWindows()
end

local function registerOptions()
    if not (PZAPI and PZAPI.ModOptions and PZAPI.ModOptions.create) then
        return
    end
    options = PZAPI.ModOptions:getOptions(MOD_OPTIONS_ID)
    if options then
        masterVolumeOption = options:getOption(OPTION_ID_MASTER_VOLUME)
        showTrackNumberPrefixOption = options:getOption(OPTION_ID_SHOW_TRACK_NUMBER_PREFIX)
        applyMasterVolume(NMClientModOptions.getMasterVolume())
        applyShowTrackNumberPrefix(NMClientModOptions.getShowTrackNumberPrefix())
        return
    end

    options = PZAPI.ModOptions:create(MOD_OPTIONS_ID, "New Music")
    masterVolumeOption = options:addSlider(
        OPTION_ID_MASTER_VOLUME,
        NMTranslations and NMTranslations.ui and NMTranslations.ui("WorldPlaybackMaxVolume", "Playback Max Volume") or "Playback Max Volume",
        0.0,
        1.0,
        0.05,
        OPTION_DEFAULT_MASTER_VOLUME,
        NMTranslations and NMTranslations.ui and NMTranslations.ui(
            "NewMusicMasterVolumeTooltip",
            "Controls the local maximum volume for New Music world playback and in-car vehicle radio playback you hear."
        ) or "Controls the local maximum volume for New Music world playback and in-car vehicle radio playback you hear."
    )
    showTrackNumberPrefixOption = options:addTickBox(
        OPTION_ID_SHOW_TRACK_NUMBER_PREFIX,
        NMTranslations and NMTranslations.ui and NMTranslations.ui("ShowTrackNumberPrefix", "Show Track Number Prefix") or "Show Track Number Prefix",
        OPTION_DEFAULT_SHOW_TRACK_NUMBER_PREFIX,
        NMTranslations and NMTranslations.ui and NMTranslations.ui(
            "ShowTrackNumberPrefixTooltip",
            "When enabled, song labels in device UIs begin with their track number."
        ) or "When enabled, song labels in device UIs begin with their track number."
    )

    function masterVolumeOption:onChange(value)
        applyMasterVolume(value)
    end

    function masterVolumeOption:onChangeApply(value)
        applyMasterVolume(value)
    end

    function showTrackNumberPrefixOption:onChangeApply(value)
        applyShowTrackNumberPrefix(value)
    end

    function options:apply()
        applyMasterVolume(NMClientModOptions.getMasterVolume())
        applyShowTrackNumberPrefix(NMClientModOptions.getShowTrackNumberPrefix())
    end

    if PZAPI.ModOptions.load then
        PZAPI.ModOptions:load()
    end
    applyMasterVolume(NMClientModOptions.getMasterVolume())
    applyShowTrackNumberPrefix(NMClientModOptions.getShowTrackNumberPrefix())
end

registerOptions()

return NMClientModOptions
