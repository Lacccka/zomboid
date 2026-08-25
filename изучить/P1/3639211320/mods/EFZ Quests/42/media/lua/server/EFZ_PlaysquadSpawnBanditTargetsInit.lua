if isClient() then
    return
end

if not EFZ then
    EFZ = {}
end

EFZ.PlaysquadSpawnBanditTargetsInit = EFZ.PlaysquadSpawnBanditTargetsInit or {}
local Init = EFZ.PlaysquadSpawnBanditTargetsInit

local LOG_PREFIX = "[EFZ][PlaysquadSpawnBanditTargetsInit] "
local OPTION_NAME = "Playsquad.SpawnBanditTargets"
local SOURCE_VALUE = "clan-07_Heretics"
local TARGET_VALUE = "clan-00_Hunters,clan-01_Rednecks,clan-06_Robbers,clan-08_Hikers,clan-10_Mafia,clan-12_Militia"

local function logInfo(message)
    print(LOG_PREFIX .. message)
end

local function getSandboxOption()
    if not SandboxOptions or not SandboxOptions.getInstance then
        return nil, nil
    end

    local sandboxOptions = SandboxOptions.getInstance()
    if not sandboxOptions or not sandboxOptions.getOptionByName then
        return nil, nil
    end

    return sandboxOptions, sandboxOptions:getOptionByName(OPTION_NAME)
end

local function hasLuaOption()
    return SandboxVars and SandboxVars.Playsquad and SandboxVars.Playsquad.SpawnBanditTargets ~= nil
end

local function getLuaValue()
    if not hasLuaOption() then
        return nil
    end
    return tostring(SandboxVars.Playsquad.SpawnBanditTargets)
end

local function syncSandboxVars(sandboxOptions)
    if sandboxOptions and sandboxOptions.toLua then
        sandboxOptions:toLua()
    end
end

local function applyAndSaveSandboxOptions(sandboxOptions)
    if not sandboxOptions then
        return false, "SandboxOptions unavailable"
    end

    if sandboxOptions.applySettings then
        sandboxOptions:applySettings()
    end

    local isServerRuntime = isServer and isServer() or false
    local serverName = getServerName and getServerName() or nil
    if isServerRuntime and serverName and serverName ~= "" and sandboxOptions.saveServerLuaFile then
        local saved = sandboxOptions:saveServerLuaFile(serverName)
        return saved == true, "saveServerLuaFile(" .. tostring(serverName) .. ")"
    end

    local worldName = Core and Core.GameSaveWorld or nil
    if worldName and worldName ~= "" and sandboxOptions.saveGameFile then
        local saved = sandboxOptions:saveGameFile(worldName)
        return saved == true, "saveGameFile(" .. tostring(worldName) .. ")"
    end

    if serverName and serverName ~= "" and sandboxOptions.saveServerLuaFile then
        local saved = sandboxOptions:saveServerLuaFile(serverName)
        return saved == true, "saveServerLuaFile(" .. tostring(serverName) .. ")"
    end

    return false, "No sandbox save target available"
end

local function initializeSpawnBanditTargets(newGame)
    if newGame ~= true then
        return
    end

    if Init.didRun then
        return
    end
    Init.didRun = true

    local hasLua = hasLuaOption()
    local sandboxOptions, option = getSandboxOption()

    if not hasLua and not option then
        logInfo("Sandbox option " .. OPTION_NAME .. " is unavailable on new game.")
        return
    end

    if option and not hasLua then
        syncSandboxVars(sandboxOptions)
        hasLua = hasLuaOption()
    end

    local currentValue = nil
    if option and option.getValue then
        currentValue = tostring(option:getValue())
    else
        currentValue = getLuaValue()
    end

    if currentValue == TARGET_VALUE then
        logInfo("Sandbox option " .. OPTION_NAME .. " already matches target on new game.")
        return
    end

    if currentValue ~= SOURCE_VALUE then
        logInfo("Skipped initializing " .. OPTION_NAME .. " because current value is " .. tostring(currentValue) .. " instead of " .. SOURCE_VALUE .. ".")
        return
    end

    local applied = false

    if option then
        if sandboxOptions.set then
            sandboxOptions:set(OPTION_NAME, TARGET_VALUE)
            applied = true
        elseif option.setValue then
            option:setValue(TARGET_VALUE)
            applied = true
        end
    end

    if hasLuaOption() then
        SandboxVars.Playsquad.SpawnBanditTargets = TARGET_VALUE
        applied = true
    end

    if not applied then
        logInfo("Sandbox option " .. OPTION_NAME .. " exists but could not be changed from Lua.")
        return
    end

    syncSandboxVars(sandboxOptions)

    if hasLuaOption() then
        SandboxVars.Playsquad.SpawnBanditTargets = TARGET_VALUE
    end

    local saved, savePath = applyAndSaveSandboxOptions(sandboxOptions)
    if saved then
        logInfo("Initialized " .. OPTION_NAME .. " on new game via " .. savePath .. ".")
        return
    end

    logInfo("Initialized " .. OPTION_NAME .. " on new game, but persistence failed via " .. savePath .. ".")
end

if Events and Events.OnInitGlobalModData and type(Events.OnInitGlobalModData.Add) == "function" then
    Events.OnInitGlobalModData.Add(initializeSpawnBanditTargets)
    logInfo("OnInitGlobalModData hook installed.")
else
    logInfo("OnInitGlobalModData is unavailable.")
end
