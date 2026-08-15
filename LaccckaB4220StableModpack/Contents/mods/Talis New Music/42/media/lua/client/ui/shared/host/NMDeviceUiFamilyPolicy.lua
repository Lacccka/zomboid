require "ui/shared/host/NMDeviceUiChromePolicy"

NMDeviceUiFamilyPolicy = NMDeviceUiFamilyPolicy or {}

local policies = {
    generic = {
        familyId = "generic",
        chrome = "vanilla_window",
        chromePolicy = NMDeviceUiChromePolicy.get("vanilla_window"),
        slots = { "media", "headphones", "battery" },
        controls = {
            "close_window",
            "play",
            "stop",
            "next_track",
            "prev_track",
            "power_on",
            "power_off",
            "mute_on",
            "mute_off",
            "cycle_mode",
            "set_volume",
        },
        supportsMute = true,
        supportsHold = false,
        supportsLid = false,
        supportsShuffleMode = true,
    },
    cdplayer = {
        familyId = "cdplayer",
        chrome = "fancy_window",
        chromePolicy = NMDeviceUiChromePolicy.get("fancy_window"),
        slots = { "media", "headphones", "battery" },
        controls = {
            "close_window",
            "play",
            "stop",
            "next_track",
            "prev_track",
            "power_on",
            "power_off",
            "cycle_mode",
            "hold_on",
            "hold_off",
            "set_volume",
            "open_lid",
            "close_lid",
        },
        supportsMute = false,
        supportsHold = true,
        supportsLid = true,
        supportsShuffleMode = true,
    },
    walkman = {
        familyId = "walkman",
        chrome = "fancy_window",
        chromePolicy = NMDeviceUiChromePolicy.get("fancy_window"),
        slots = { "media", "headphones", "battery" },
        controls = {
            "close_window",
            "play",
            "stop",
            "next_track",
            "prev_track",
            "power_on",
            "power_off",
            "cycle_mode",
            "set_volume",
            "open_lid",
            "close_lid",
        },
        supportsMute = false,
        supportsHold = false,
        supportsLid = true,
        supportsShuffleMode = false,
    },
    boombox = {
        familyId = "boombox",
        chrome = "fancy_window",
        chromePolicy = NMDeviceUiChromePolicy.get("fancy_window"),
        slots = { "media", "battery", "headphones" },
        controls = {
            "close_window",
            "play",
            "stop",
            "next_track",
            "prev_track",
            "power_on",
            "power_off",
            "set_volume",
            "open_lid",
            "close_lid",
            "eject_media",
            "set_playback_policy",
        },
        supportsMute = false,
        supportsHold = false,
        supportsLid = true,
        supportsShuffleMode = true,
    }
}

function NMDeviceUiFamilyPolicy.get(familyId)
    return policies[tostring(familyId or "")] or nil
end

function NMDeviceUiFamilyPolicy.resolveForWindow(window)
    if window and window._nmUiFamilyPolicy then
        return window._nmUiFamilyPolicy
    end
    return NMDeviceUiFamilyPolicy.get(window and window._nmUiPerfKind or nil)
end

return NMDeviceUiFamilyPolicy
