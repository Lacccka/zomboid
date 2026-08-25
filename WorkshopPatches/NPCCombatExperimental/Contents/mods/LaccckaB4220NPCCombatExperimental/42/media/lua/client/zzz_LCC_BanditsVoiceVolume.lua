require "PZAPI/ModOptions"

LCCBanditsVoiceVolume = LCCBanditsVoiceVolume or {}

local VoiceVolume = LCCBanditsVoiceVolume
local MOD_OPTIONS_ID = "LaccckaB4220NPCCombatExperimentalVoice"
local OPTION_ID = "BanditsVoiceVolume"
local DEFAULT_PERCENT = 250
local MIN_PERCENT = 0
local MAX_PERCENT = 300
local STEP_PERCENT = 10

local options = PZAPI.ModOptions:getOptions(MOD_OPTIONS_ID)
if not options then
    options = PZAPI.ModOptions:create(MOD_OPTIONS_ID, "Lacccka NPC Combat Experimental")
end

options:addTitle("Bandits")
VoiceVolume.option = options:addSlider(
    OPTION_ID,
    "Bandits voice volume (%)",
    MIN_PERCENT,
    MAX_PERCENT,
    STEP_PERCENT,
    DEFAULT_PERCENT,
    "Client-side multiplier for Bandits speech. 100% is the original volume; 0% mutes speech."
)

function VoiceVolume.GetPercent()
    local value = DEFAULT_PERCENT
    if VoiceVolume.option and VoiceVolume.option.getValue then
        value = tonumber(VoiceVolume.option:getValue()) or DEFAULT_PERCENT
    end

    if value < MIN_PERCENT then value = MIN_PERCENT end
    if value > MAX_PERCENT then value = MAX_PERCENT end
    return value
end

function VoiceVolume.GetMultiplier()
    return VoiceVolume.GetPercent() / 100
end

function VoiceVolume.InstallHook()
    if VoiceVolume.installed then return true end

    if type(Bandit) ~= "table" or type(Bandit.Say) ~= "function" then
        print("[LCC][BanditsVoiceVolume] Bandit.Say unavailable; hook not installed")
        return false
    end

    if type(BanditUtils) ~= "table" or type(BanditUtils.FixVolume) ~= "function" then
        print("[LCC][BanditsVoiceVolume] BanditUtils.FixVolume unavailable; hook not installed")
        return false
    end

    VoiceVolume.originalSay = Bandit.Say

    Bandit.Say = function(zombie, phrase, force)
        local multiplier = VoiceVolume.GetMultiplier()
        if multiplier == 1 then
            return VoiceVolume.originalSay(zombie, phrase, force)
        end

        local originalFixVolume = BanditUtils.FixVolume
        BanditUtils.FixVolume = function(volume)
            return originalFixVolume(volume) * multiplier
        end

        local ok, result = pcall(VoiceVolume.originalSay, zombie, phrase, force)
        BanditUtils.FixVolume = originalFixVolume

        if not ok then
            error(result)
        end

        return result
    end

    VoiceVolume.installed = true
    print(string.format(
        "[LCC][BanditsVoiceVolume] installed; client volume=%d%%",
        VoiceVolume.GetPercent()
    ))
    return true
end

Events.OnGameStart.Add(VoiceVolume.InstallHook)
