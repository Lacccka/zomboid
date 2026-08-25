require "QPReputation_Config"
require "QPReputation_Shared"

QPReputation.Client = QPReputation.Client or { profile = nil }
local C = QPReputation.Client
-- QPSR_SERVER_PLAYER_EDITOR_V1
C.profileListeners = C.profileListeners or {}
C.adminRequestPending = C.adminRequestPending or false

C.adminProfile = C.adminProfile or nil
C.adminProfileListeners = C.adminProfileListeners or {}
C.adminProfileRequestPending =
    C.adminProfileRequestPending or false

C.automationSettings = C.automationSettings or nil
C.settingsListeners = C.settingsListeners or {}
C.settingsRequestPending = C.settingsRequestPending or false

local function sayNotification(data)
    if not data or not QPReputation.Config.EnableNotifications then return end
    local player = getPlayer()
    if not player then return end
    local path = tostring(data.path or "reputation")
    local change = tonumber(data.change) or 0
    local prefix = change >= 0 and "+" or ""
    player:Say(prefix .. tostring(change) .. " " .. path .. " reputation")
    if data.levelUp and QPReputation.Config.EnableLevelUpNotifications then
        player:Say("Reputation level increased: " .. QPReputation.getTitle(data.level or 0))
    end
end

local function notifyProfileListeners(profile)
    for listener, callback in pairs(C.profileListeners) do
        if listener and callback then
            local ok = pcall(callback, listener, profile)
            if not ok then C.profileListeners[listener] = nil end
        else
            C.profileListeners[listener] = nil
        end
    end
end

local function notifyAdminProfileListeners(
    profile,
    response
)
    for listener, callback in pairs(
        C.adminProfileListeners
    ) do
        if listener and callback then
            local ok = pcall(
                callback,
                listener,
                profile,
                response
            )

            if not ok then
                C.adminProfileListeners[listener] = nil
            end
        else
            C.adminProfileListeners[listener] = nil
        end
    end
end
local function onServerCommand(module, command, args)
    if module ~= "QPReputation" then return end
    if command == "Profile" then
        C.profile = args and args.profile or nil
        C.adminRequestPending = false

        if args and args.automationSettings then
            -- QPSR_SETTINGS_UI_BACKGROUND_SYNC_GUARD_V041
            -- Profile packets are also sent by periodic automation scans.
            -- Cache their settings, but do not overwrite an open settings
            -- window or clear a pending explicit settings request/save.
            C.automationSettings = args.automationSettings
        end

        notifyProfileListeners(C.profile)
        sayNotification(args and args.notification)
    elseif command == "AdminProfile" then
        C.adminProfile =
            args and args.profile or nil

        C.adminRequestPending = false
        C.adminProfileRequestPending = false

        notifyAdminProfileListeners(
            C.adminProfile,
            args
        )
    elseif command == "AutomationSettings" then
        C.automationSettings = args and args.settings or nil
        C.settingsRequestPending = false

        for listener, callback in pairs(C.settingsListeners) do
            if listener and callback then
                local ok = pcall(
                    callback,
                    listener,
                    C.automationSettings,
                    args
                )
                if not ok then
                    C.settingsListeners[listener] = nil
                end
            end
        end
    end
end

function C.addProfileListener(owner, callback)
    if owner and callback then C.profileListeners[owner] = callback end
end

function C.removeProfileListener(owner)
    if owner then C.profileListeners[owner] = nil end
end

function C.requestProfile() sendClientCommand("QPReputation", "RequestProfile", {}) end

local function sendAdmin(command, payload)
    if C.adminRequestPending then return false end
    C.adminRequestPending = true
    sendClientCommand("QPReputation", command, payload)
    return true
end

function C.adminAdd(username, path, points, reason)
    return sendAdmin("AdminAdd", {username=username,path=path,points=points,reason=reason})
end
function C.adminSet(username, path, points, reason)
    return sendAdmin("AdminSet", {username=username,path=path,points=points,reason=reason})
end
function C.adminReset(username, path, reason)
    return sendAdmin("AdminReset", {username=username,path=path,reason=reason})
end

function C.addAdminProfileListener(owner, callback)
    if owner and callback then
        C.adminProfileListeners[owner] = callback
    end
end

function C.removeAdminProfileListener(owner)
    if owner then
        C.adminProfileListeners[owner] = nil
    end
end

function C.requestAdminProfile(username)
    username = tostring(username or "")

    username = string.gsub(
        username,
        "^%s+",
        ""
    )

    username = string.gsub(
        username,
        "%s+$",
        ""
    )

    if username == "" then
        return false
    end

    if C.adminProfileRequestPending then
        return false
    end

    C.adminProfileRequestPending = true

    sendClientCommand(
        "QPReputation",
        "RequestAdminProfile",
        {
            username = username
        }
    )

    return true
end
function C.addSettingsListener(owner, callback)
    if owner and callback then
        C.settingsListeners[owner] = callback
    end
end

function C.removeSettingsListener(owner)
    if owner then
        C.settingsListeners[owner] = nil
    end
end

function C.requestAutomationSettings()
    if C.settingsRequestPending then return false end
    C.settingsRequestPending = true
    sendClientCommand(
        "QPReputation",
        "RequestAutomationSettings",
        {}
    )
    return true
end

function C.saveAutomationSettings(settings)
    if C.settingsRequestPending then return false end
    C.settingsRequestPending = true
    sendClientCommand(
        "QPReputation",
        "SaveAutomationSettings",
        { settings = settings }
    )
    return true
end

function C.resetAutomationSettings()
    if C.settingsRequestPending then return false end
    C.settingsRequestPending = true
    sendClientCommand(
        "QPReputation",
        "ResetAutomationSettings",
        {}
    )
    return true
end

Events.OnServerCommand.Add(onServerCommand)
Events.OnCreatePlayer.Add(function() C.requestProfile() end)
