if not EFZ then
    EFZ = {}
end

EFZ.BanditsOptionLock = EFZ.BanditsOptionLock or EFZ.BanditsArrivalIconLock or {}
EFZ.BanditsArrivalIconLock = EFZ.BanditsOptionLock
local Lock = EFZ.BanditsOptionLock

local LOG_PREFIX = "[EFZ][BanditsOptionLock] "
local FORCE_LOG_COOLDOWN_MS = 5000
local FORCE_FALSE_OPTIONS = {
    { name = "Bandits.General_ArrivalIcon", field = "General_ArrivalIcon" },
    { name = "Bandits.General_Captions", field = "General_Captions" },
}

local function logInfo(message)
    print(LOG_PREFIX .. message)
end

local function getNowMs()
    if getTimestampMs then
        return getTimestampMs()
    end
    return 0
end

local function shouldLogForce(optionName)
    Lock.nextForceLogMs = Lock.nextForceLogMs or {}

    local now = getNowMs()
    if now == 0 then
        return true
    end

    local nextLogMs = Lock.nextForceLogMs[optionName] or 0
    if now < nextLogMs then
        return false
    end

    Lock.nextForceLogMs[optionName] = now + FORCE_LOG_COOLDOWN_MS
    return true
end

local function getSandboxOptionsInstance()
    if not SandboxOptions or not SandboxOptions.getInstance then
        return nil
    end

    local sandboxOptions = SandboxOptions.getInstance()
    if not sandboxOptions or not sandboxOptions.getOptionByName then
        return nil
    end

    return sandboxOptions
end

local function getLuaBanditsOption(field)
    if not SandboxVars or not SandboxVars.Bandits then
        return false, nil
    end

    local value = SandboxVars.Bandits[field]
    if value == nil then
        return false, nil
    end

    return true, value
end

local function enforceOptionDisabled(optionDef)
    Lock.missingLogged = Lock.missingLogged or {}

    local hasLuaOption, luaValue = getLuaBanditsOption(optionDef.field)
    local sandboxOptions = getSandboxOptionsInstance()
    local option = sandboxOptions and sandboxOptions:getOptionByName(optionDef.name) or nil

    if not hasLuaOption and not option then
        if not Lock.missingLogged[optionDef.name] then
            logInfo("Sandbox option " .. optionDef.name .. " is unavailable.")
            Lock.missingLogged[optionDef.name] = true
        end
        return
    end

    if Lock.missingLogged[optionDef.name] then
        logInfo("Sandbox option " .. optionDef.name .. " detected.")
        Lock.missingLogged[optionDef.name] = false
    end

    if option and not hasLuaOption and sandboxOptions.toLua then
        sandboxOptions:toLua()
        hasLuaOption, luaValue = getLuaBanditsOption(optionDef.field)
    end

    local changed = false
    local optionNeedsForce = false

    if option then
        if option.getValue then
            optionNeedsForce = option:getValue() ~= false
        elseif hasLuaOption then
            optionNeedsForce = luaValue ~= false
        else
            optionNeedsForce = true
        end
    end

    if option and optionNeedsForce then
        local appliedJavaChange = false

        if sandboxOptions.set then
            sandboxOptions:set(optionDef.name, false)
            appliedJavaChange = true
        elseif option.setValue then
            option:setValue(false)
            appliedJavaChange = true
        end

        if appliedJavaChange then
            if sandboxOptions.toLua then
                sandboxOptions:toLua()
            end
            if sandboxOptions.applySettings then
                sandboxOptions:applySettings()
            end
            changed = true
        end
    end

    hasLuaOption, luaValue = getLuaBanditsOption(optionDef.field)
    if hasLuaOption and luaValue ~= false then
        SandboxVars.Bandits[optionDef.field] = false
        changed = true
    end

    if changed and shouldLogForce(optionDef.name) then
        logInfo("Forced " .. optionDef.name .. " = false.")
    end
end

local function enforceBanditsOptionsDisabled()
    for _, optionDef in ipairs(FORCE_FALSE_OPTIONS) do
        enforceOptionDisabled(optionDef)
    end
end

local function installHookOnce(event, fn, flagName, label)
    if Lock[flagName] then
        return
    end
    if not event or type(event.Add) ~= "function" then
        return
    end
    event.Add(fn)
    Lock[flagName] = true
    logInfo(label .. " hook installed.")
end

installHookOnce(Events and Events.OnGameStart, enforceBanditsOptionsDisabled, "gameStartHooked", "OnGameStart")
installHookOnce(Events and Events.OnCreatePlayer, function()
    enforceBanditsOptionsDisabled()
end, "createPlayerHooked", "OnCreatePlayer")

installHookOnce(Events and Events.OnTick, enforceBanditsOptionsDisabled, "tickHooked", "OnTick")
if not Lock.tickHooked then
    installHookOnce(Events and Events.EveryOneMinute, enforceBanditsOptionsDisabled, "everyMinuteHooked", "EveryOneMinute")
end
if not Lock.tickHooked and not Lock.everyMinuteHooked then
    installHookOnce(Events and Events.EveryTenMinutes, enforceBanditsOptionsDisabled, "everyTenMinutesHooked", "EveryTenMinutes")
end

enforceBanditsOptionsDisabled()
