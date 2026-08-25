require "ISUI/ISPanel"
require "ExtractionMode/Util"
require "ExtractionMode/Localization"

ExtractionMode = ExtractionMode or {}
local Util = ExtractionMode.Util
local Localization = ExtractionMode.Localization
local Epilogue = ISPanel:derive("ExtractionModeCampaignEpilogue")

local FADE_TO_BLACK_MS = 5000
local TEXT_FADE_IN_MS = 2000
local TEXT_HOLD_MS = 11000
local TEXT_FADE_OUT_MS = 2000
local FADE_FROM_BLACK_MS = 10000
local TOTAL_MS = FADE_TO_BLACK_MS + TEXT_FADE_IN_MS + TEXT_HOLD_MS
    + TEXT_FADE_OUT_MS + FADE_FROM_BLACK_MS

local function clamp01(value)
    return math.max(0, math.min(1, tonumber(value) or 0))
end

local function captureAudioState()
    local manager = getSoundManager and getSoundManager()
    if manager == nil then return nil end
    local state = { manager = manager }
    pcall(function() state.sound = manager:getSoundVolume() end)
    pcall(function() state.music = manager:getMusicVolume() end)
    pcall(function() state.ambient = manager:getAmbientVolume() end)
    pcall(function() state.vehicle = manager:getVehicleEngineVolume() end)
    pcall(function() state.uiMuted = manager:isUiSoundMuted() end)
    return state
end

local function setAudioFactor(state, factor)
    if state == nil or state.manager == nil then return end
    factor = clamp01(factor)
    if state.lastFactor and math.abs(state.lastFactor - factor) < 0.005 then return end
    state.lastFactor = factor
    if state.sound ~= nil then
        pcall(function() state.manager:setSoundVolume(state.sound * factor) end)
    end
    if state.music ~= nil then
        pcall(function() state.manager:setMusicVolume(state.music * factor) end)
    end
    if state.ambient ~= nil then
        pcall(function() state.manager:setAmbientVolume(state.ambient * factor) end)
    end
    if state.vehicle ~= nil then
        pcall(function() state.manager:setVehicleEngineVolume(state.vehicle * factor) end)
    end
    pcall(function()
        state.manager:setUiSoundMuted(factor <= 0 and true or state.uiMuted == true)
    end)
end

function Epilogue:restoreAudio()
    if self.audioRestored == true then return end
    self.audioRestored = true
    setAudioFactor(self.audioState, 1)
    if self.audioState and self.audioState.manager then
        pcall(function()
            self.audioState.manager:setUiSoundMuted(self.audioState.uiMuted == true)
        end)
    end
end

function Epilogue:finish()
    self:restoreAudio()
    self:removeFromUIManager()
    if ExtractionMode.CampaignEpilogueInstance == self then
        ExtractionMode.CampaignEpilogueInstance = nil
    end
end

function Epilogue:prerender()
    self:setWidth(getCore():getScreenWidth())
    self:setHeight(getCore():getScreenHeight())
    ISPanel.prerender(self)
end

function Epilogue:render()
    local elapsed = Util.nowMs() - self.startedAt
    if elapsed >= TOTAL_MS then
        self:finish()
        return
    end

    local audioFactor = 0
    if elapsed < FADE_TO_BLACK_MS then
        audioFactor = 1 - clamp01(elapsed / FADE_TO_BLACK_MS)
    elseif elapsed > TOTAL_MS - FADE_FROM_BLACK_MS then
        audioFactor = clamp01((elapsed - (TOTAL_MS - FADE_FROM_BLACK_MS)) / FADE_FROM_BLACK_MS)
    end
    setAudioFactor(self.audioState, audioFactor)

    local blackAlpha = 1
    if elapsed < FADE_TO_BLACK_MS then
        blackAlpha = clamp01(elapsed / FADE_TO_BLACK_MS)
    elseif elapsed > TOTAL_MS - FADE_FROM_BLACK_MS then
        blackAlpha = clamp01((TOTAL_MS - elapsed) / FADE_FROM_BLACK_MS)
    end
    self:drawRect(0, 0, self.width, self.height, blackAlpha, 0, 0, 0)

    local textStart = FADE_TO_BLACK_MS
    local textFadeOutStart = textStart + TEXT_FADE_IN_MS + TEXT_HOLD_MS
    local textAlpha = 0
    if elapsed >= textStart and elapsed < textStart + TEXT_FADE_IN_MS then
        textAlpha = clamp01((elapsed - textStart) / TEXT_FADE_IN_MS)
    elseif elapsed >= textStart + TEXT_FADE_IN_MS and elapsed < textFadeOutStart then
        textAlpha = 1
    elseif elapsed >= textFadeOutStart and elapsed < textFadeOutStart + TEXT_FADE_OUT_MS then
        textAlpha = clamp01(1 - ((elapsed - textFadeOutStart) / TEXT_FADE_OUT_MS))
    end

    if textAlpha > 0 then
        local centerX = self.width / 2
        local centerY = self.height / 2
        self:drawTextCentre(Localization.get("IGUI_ExtractionMode_CampaignEpilogue_1",
            "The vaccine candidate is on its way out of the Exclusion Zone..."),
            centerX, centerY - 38, 1, 1, 1, textAlpha, UIFont.Medium)
        self:drawTextCentre(Localization.get("IGUI_ExtractionMode_CampaignEpilogue_2",
            "You've done everything you can. Now your only goal is to survive"),
            centerX, centerY, 0.9, 0.9, 0.9, textAlpha, UIFont.Medium)
        self:drawTextCentre(Localization.get("IGUI_ExtractionMode_CampaignEpilogue_3",
            "long enough to see if it mattered."),
            centerX, centerY + 30, 0.9, 0.9, 0.9, textAlpha, UIFont.Medium)
    end
end

function Epilogue:new()
    local object = ISPanel:new(0, 0, getCore():getScreenWidth(), getCore():getScreenHeight())
    setmetatable(object, self)
    self.__index = self
    object.background = false
    object.border = false
    object.moveWithMouse = false
    object.startedAt = Util.nowMs()
    object.audioState = captureAudioState()
    object.audioRestored = false
    return object
end

function ExtractionMode.playCampaignEpilogue()
    local existing = ExtractionMode.CampaignEpilogueInstance
    if existing then existing:finish() end
    local panel = Epilogue:new()
    panel:initialise()
    panel:addToUIManager()
    panel:setAlwaysOnTop(true)
    pcall(function() panel:setWantMouseEvents(false) end)
    ExtractionMode.CampaignEpilogueInstance = panel
    return panel
end

ExtractionMode.CampaignEpilogue = Epilogue
return Epilogue
