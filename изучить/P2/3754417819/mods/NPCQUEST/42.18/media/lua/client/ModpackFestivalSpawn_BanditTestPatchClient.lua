-- Suppress Bandits2's startup diagnostics popup.

if isServer() then
    return
end

local LOG_PREFIX = "[ModpackFestivalSpawn][BanditTestPatch] "
local suppressed = false

local function suppressBanditTestPopup()
    if suppressed then
        return true
    end
    if not BanditTest or not BanditTest.Check then
        return false
    end

    local originalCheck = BanditTest.Check
    if Events and Events.OnGameStart then
        pcall(function()
            Events.OnGameStart.Remove(originalCheck)
        end)
    end

    BanditTest.Check = function()
    end
    suppressed = true
    print(LOG_PREFIX .. "suppressed Bandits2 automated tests popup")
    return true
end

suppressBanditTestPopup()

Events.OnGameStart.Add(function()
    suppressBanditTestPopup()
end)
