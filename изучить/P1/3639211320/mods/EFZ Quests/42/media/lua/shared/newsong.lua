if isServer() then
    return
end

require "ISUI/ISButton"
require "OptionScreens/MainScreen"

local LOG_PREFIX = "[EFZ Quests] newsong.lua: "
local UI_BORDER_SPACING = 10

local introSong = {
    currentSong = 0,
    tick = 0,
    track = "NewSong_1",
    songEmitter = FMODSoundEmitter:new(),
    pauseButton = nil,
    wasMainMenuVisible = false,
    restorePending = false,
    customTrackUnavailable = false,
    missingTrackReported = false,
    userPaused = false,
}

local function logNewsong(message)
    DebugLog.log(LOG_PREFIX .. message)
end

local function getButtonHeight()
    return getTextManager():getFontHeight(UIFont.Small) + 6
end

local function getTopLeftButtonPosition()
    local screenWidth = getCore():getScreenWidth()
    local scale = screenWidth / 1920
    local x = UI_BORDER_SPACING * 2 * scale
    local y = UI_BORDER_SPACING * 2 * scale
    return x, y
end

local function isMainMenuVisible()
    return MainScreen and MainScreen.instance and MainScreen.instance:isReallyVisible()
end

local function isBottomPanelVisible()
    local screen = MainScreen.instance
    return screen and screen.bottomPanel and screen.bottomPanel:getIsVisible()
end

local function applyConfiguredMusicVolume()
    getSoundManager():setMusicVolume(getCore():getOptionMusicVolume())
end

local function muteVanillaMenuMusic()
    getSoundManager():setMusicVolume(0)
end

local function restoreMusicAfterLoad()
    if introSong.restorePending then
        applyConfiguredMusicVolume()
        introSong.restorePending = false
    end
end

local function stopIntroSong()
    introSong.songEmitter:stopSoundByName(introSong.track)
    introSong.currentSong = 0
    introSong.tick = 0
end

local function isIntroSongPlaying()
    return introSong.currentSong ~= 0 and introSong.songEmitter:isPlaying(introSong.currentSong)
end

local function syncEmitterVolume()
    if introSong.currentSong == 0 then
        return
    end

    if introSong.userPaused then
        introSong.songEmitter:setVolume(introSong.currentSong, 0.0)
        return
    end

    introSong.songEmitter:setVolume(introSong.currentSong, 1.0)
end

local function startIntroSong()
    if introSong.userPaused or introSong.customTrackUnavailable then
        return
    end

    if isIntroSongPlaying() then
        syncEmitterVolume()
        return
    end

    introSong.currentSong = introSong.songEmitter:playSound(introSong.track)
    if introSong.currentSong == 0 then
        if not introSong.missingTrackReported then
            logNewsong("failed to play '" .. introSong.track .. "'. Check the B42 sound alias or bank.")
            introSong.missingTrackReported = true
        end

        introSong.customTrackUnavailable = true
        applyConfiguredMusicVolume()
        return
    end

    introSong.customTrackUnavailable = false
    introSong.missingTrackReported = false
    syncEmitterVolume()
end

local function pauseIntroSong()
    introSong.userPaused = true
    syncEmitterVolume()
end

local function resumeIntroSong()
    introSong.userPaused = false

    if isIntroSongPlaying() then
        syncEmitterVolume()
        return
    end

    startIntroSong()
end

local function updatePauseButtonTitle()
    local button = introSong.pauseButton
    if not button then
        return
    end

    if introSong.userPaused then
        button:setTitle(getText("IGUI_EFZ_MenuMusicPlay"))
    else
        button:setTitle(getText("IGUI_EFZ_MenuMusicPause"))
    end
end

local function onMusicToggleClick()
    if introSong.customTrackUnavailable then
        return
    end

    if introSong.userPaused then
        resumeIntroSong()
    else
        pauseIntroSong()
    end

    updatePauseButtonTitle()
end

local function ensurePauseButton(screen)
    if introSong.pauseButton or not screen then
        return
    end

    local buttonHeight = getButtonHeight()
    local button = ISButton:new(0, 0, 120, buttonHeight, getText("IGUI_EFZ_MenuMusicPause"), introSong, onMusicToggleClick)
    button:initialise()
    button.borderColor = { r = 1, g = 1, b = 1, a = 0.7 }
    button.textColor = { r = 1, g = 1, b = 1, a = 1 }
    button:setAnchorLeft(true)
    button:setAnchorTop(true)
    button:setAnchorRight(false)
    button:setAnchorBottom(false)
    screen:addChild(button)
    introSong.pauseButton = button
end

local function updatePauseButtonLayout(screen)
    screen = screen or MainScreen.instance
    if not screen then
        return
    end

    if not isBottomPanelVisible() then
        if introSong.pauseButton then
            introSong.pauseButton:setVisible(false)
        end
        return
    end

    ensurePauseButton(screen)

    local button = introSong.pauseButton
    local buttonHeight = getButtonHeight()
    local buttonX, buttonY = getTopLeftButtonPosition()

    updatePauseButtonTitle()
    button:setWidthToTitle(120)
    button:setHeight(buttonHeight)
    button:setX(buttonX)
    button:setY(buttonY)
    button:setVisible(true)
    button:setEnable(not introSong.customTrackUnavailable)
end

local function hookMainScreenInstantiate()
    if MainScreen._efzNewsongInstantiateHooked then
        return
    end
    if not MainScreen or not MainScreen.instantiate then
        return
    end

    local vanillaInstantiate = MainScreen.instantiate
    function MainScreen:instantiate()
        vanillaInstantiate(self)
        ensurePauseButton(self)
    end
    MainScreen._efzNewsongInstantiateHooked = true
end

local function hookMainScreenPrerender()
    if MainScreen._efzNewsongPrerenderHooked then
        return
    end
    if not MainScreen or not MainScreen.prerender then
        return
    end

    local previousPrerender = MainScreen.prerender
    function MainScreen:prerender()
        previousPrerender(self)
        updatePauseButtonLayout(self)
    end
    MainScreen._efzNewsongPrerenderHooked = true
end

local function onMainMenuEnter()
    stopIntroSong()
    introSong.wasMainMenuVisible = false
    introSong.restorePending = false
    introSong.customTrackUnavailable = false
    introSong.missingTrackReported = false
    introSong.userPaused = false
    updatePauseButtonTitle()
end

local function onLeaveFrontEnd()
    stopIntroSong()
    introSong.wasMainMenuVisible = false
    introSong.restorePending = true
    if introSong.pauseButton then
        introSong.pauseButton:setVisible(false)
    end
end

local function onFrontEndTick()
    introSong.songEmitter:tick()

    if not isMainMenuVisible() then
        if introSong.wasMainMenuVisible then
            onLeaveFrontEnd()
        elseif introSong.pauseButton and not isBottomPanelVisible() then
            introSong.pauseButton:setVisible(false)
        end
        return
    end

    introSong.wasMainMenuVisible = true

    if introSong.customTrackUnavailable then
        applyConfiguredMusicVolume()
        return
    end

    muteVanillaMenuMusic()
    introSong.tick = introSong.tick + 1
    syncEmitterVolume()

    if introSong.tick >= 10 and not introSong.userPaused and not isIntroSongPlaying() then
        startIntroSong()
    end
end

local function onWorldInit()
    stopIntroSong()
    restoreMusicAfterLoad()
end

local function onGameBoot()
    hookMainScreenInstantiate()
    hookMainScreenPrerender()
end

Events.OnGameBoot.Add(onGameBoot)
Events.OnFETick.Add(onFrontEndTick)
Events.OnInitWorld.Add(onWorldInit)
Events.OnGameStart.Add(restoreMusicAfterLoad)
Events.OnMainMenuEnter.Add(onMainMenuEnter)