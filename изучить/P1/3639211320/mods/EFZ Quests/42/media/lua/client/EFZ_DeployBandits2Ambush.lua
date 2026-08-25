if isServer() then
    return
end

if not EFZ then
    EFZ = {}
end

EFZ.DeployBandits2Ambush = EFZ.DeployBandits2Ambush or {}
local Ambush = EFZ.DeployBandits2Ambush

if Ambush._installed then
    return
end
Ambush._installed = true

local LOG_PREFIX = "[EFZ][DeployBandits2Ambush] "
local MOD_ID = "Bandits2"
local MODDATA_KEY = "EFZDeployBandits2Ambush"
local GLOBAL_MODDATA_KEY = "EFZDeployBandits2AmbushGlobal"
local DEPLOY_AMBUSH_GRACE_MINUTES = 60
local DEPLOY_AMBUSH_HOURS_PER_DAY = 24
local DEPLOY_AMBUSH_HOURS_PER_SEGMENT = 8
local DEPLOY_AMBUSH_SEGMENTS_PER_DAY = 3

Ambush.loggedDailySchedule = Ambush.loggedDailySchedule or {}

local function logInfo(message)
    DebugLog.log(LOG_PREFIX .. message)
end

local function isAmbushEnabled()
    return Ambush.enabled ~= false
end

local function setAmbushEnabled(enabled)
    local desired = enabled ~= false
    if Ambush.enabled == desired then
        return desired
    end

    Ambush.enabled = desired
    logInfo("Ambush spawn " .. (desired and "enabled." or "disabled."))
    return desired
end

Ambush.enabled = Ambush.enabled ~= false
Ambush.isEnabled = isAmbushEnabled
Ambush.setEnabled = setAmbushEnabled
Ambush.toggle = function()
    return setAmbushEnabled(not isAmbushEnabled())
end

function EFZ.IsDeployBandits2AmbushEnabled()
    return isAmbushEnabled()
end

function EFZ.SetDeployBandits2AmbushEnabled(enabled)
    return setAmbushEnabled(enabled)
end

function EFZ.ToggleDeployBandits2Ambush()
    return Ambush.toggle()
end

local function isBandits2Active()
    local mods = getActivatedMods and getActivatedMods() or nil
    if not mods then
        return false
    end

    if mods.contains and mods:contains(MOD_ID) then
        return true
    end

    if not mods.size or not mods.get then
        return false
    end

    for i = 0, mods:size() - 1 do
        local modId = tostring(mods:get(i) or ""):gsub("^\\", "")
        if modId == MOD_ID then
            return true
        end
    end

    return false
end

local function isDeployInProgress(playerObj)
    return playerObj
        and type(EFZ.IsDeployInProgress) == "function"
        and EFZ.IsDeployInProgress(playerObj) == true
end

local function getDeployAmbushDayKey()
    local gameTime = getGameTime and getGameTime() or nil
    if not gameTime then
        return nil
    end

    local year = tonumber(gameTime:getYear()) or 0
    local month = tonumber(gameTime:getMonth()) or 0
    local day = tonumber(gameTime:getDay()) or 0
    local hour = tonumber(gameTime:getHour()) or 0
    local segment = math.floor(hour / DEPLOY_AMBUSH_HOURS_PER_SEGMENT)
    if segment >= DEPLOY_AMBUSH_SEGMENTS_PER_DAY then
        segment = DEPLOY_AMBUSH_SEGMENTS_PER_DAY - 1
    end

    return (((year * 12) + month) * 31 + day) * DEPLOY_AMBUSH_SEGMENTS_PER_DAY + segment
end

local function getCurrentHourOfDay()
    local gameTime = getGameTime and getGameTime() or nil
    if not gameTime then
        return nil
    end

    return tonumber(gameTime:getHour()) or 0
end

local function getDeployAmbushSlotKey(dayKey, hourOfDay)
    dayKey = tonumber(dayKey)
    hourOfDay = tonumber(hourOfDay)
    if dayKey == nil or hourOfDay == nil then
        return nil
    end
    return (dayKey * DEPLOY_AMBUSH_HOURS_PER_DAY) + hourOfDay
end

local function formatHourSlotClockTime(hourOfDay)
    hourOfDay = tonumber(hourOfDay)
    if hourOfDay == nil then
        return "nil"
    end
    return string.format("%02d:00-%02d:59", hourOfDay, hourOfDay)
end

local function getDeployAmbushSegmentStartHour(dayKey)
    dayKey = tonumber(dayKey)
    if dayKey == nil then
        return nil
    end

    local segmentIndex = dayKey % DEPLOY_AMBUSH_SEGMENTS_PER_DAY
    return segmentIndex * DEPLOY_AMBUSH_HOURS_PER_SEGMENT
end

local function requestGlobalAmbushState()
    if ModData and ModData.request then
        ModData.request(GLOBAL_MODDATA_KEY)
    end
end

local function getGlobalAmbushState()
    if ModData and ModData.getOrCreate then
        return ModData.getOrCreate(GLOBAL_MODDATA_KEY)
    end
    return nil
end

local function logScheduledDailySlotOnce(playerObj, dayKey, hourOfDay)
    if not playerObj then
        return
    end

    local playerNum = playerObj:getPlayerNum()
    if playerNum == nil or Ambush.loggedDailySchedule[playerNum] == dayKey then
        return
    end

    Ambush.loggedDailySchedule[playerNum] = dayKey
    logInfo("Scheduled 8-hour ambush slot for player " .. tostring(playerNum)
        .. " at " .. formatHourSlotClockTime(hourOfDay) .. ".")
end

local function getCurrentWorldAgeMinutes()
    return math.floor(getGameTime():getWorldAgeHours() * 60)
end

local function getDeployStartedWorldAgeMinutes(playerObj)
    local modData = playerObj and playerObj.getModData and playerObj:getModData() or nil
    local deployState = modData and modData.EFZDeploy or nil
    if type(deployState) ~= "table" then
        return nil
    end
    return tonumber(deployState.startedWorldAgeMinutes)
end

local function getDeployAmbushGraceRemainingMinutes(playerObj)
    local startedWorldAgeMinutes = getDeployStartedWorldAgeMinutes(playerObj)
    if startedWorldAgeMinutes == nil then
        return 0
    end

    local elapsedMinutes = getCurrentWorldAgeMinutes() - startedWorldAgeMinutes
    if elapsedMinutes < 0 then
        elapsedMinutes = 0
    end

    local remainingMinutes = DEPLOY_AMBUSH_GRACE_MINUTES - elapsedMinutes
    if remainingMinutes > 0 then
        return remainingMinutes
    end

    return 0
end

local function persistAmbushState(playerObj)
    if playerObj and playerObj.transmitModData then
        playerObj:transmitModData()
    end
end

local function getScheduledDailySlot(playerObj, dayKey)
    local globalState = getGlobalAmbushState()
    if type(globalState) ~= "table" then
        return nil, nil, nil
    end

    local scheduledDayKey = tonumber(globalState.scheduledDayKey)
    local scheduledHourOfDay = tonumber(globalState.scheduledHourOfDay)
    local segmentStartHour = getDeployAmbushSegmentStartHour(dayKey)
    local segmentEndHour = segmentStartHour and (segmentStartHour + DEPLOY_AMBUSH_HOURS_PER_SEGMENT - 1) or nil
    if scheduledDayKey ~= dayKey
        or scheduledHourOfDay == nil
        or segmentStartHour == nil
        or segmentEndHour == nil
        or scheduledHourOfDay < segmentStartHour
        or scheduledHourOfDay > segmentEndHour then
        return nil, nil, globalState
    end

    local scheduledSlotKey = tonumber(globalState.scheduledSlotKey)
    if scheduledSlotKey == nil then
        scheduledSlotKey = getDeployAmbushSlotKey(dayKey, scheduledHourOfDay)
    end
    if scheduledSlotKey == nil then
        return nil, nil, globalState
    end

    logScheduledDailySlotOnce(playerObj, dayKey, scheduledHourOfDay)
    return scheduledSlotKey, scheduledHourOfDay, globalState
end

local function getAmbushState(playerObj)
    local modData = playerObj and playerObj.getModData and playerObj:getModData() or nil
    if not modData then
        return nil
    end

    local state = modData[MODDATA_KEY]
    if type(state) ~= "table" then
        state = {}
        modData[MODDATA_KEY] = state
    end

    return state
end

local function isSlotResolvedOnServer(globalState, dayKey, scheduledSlotKey)
    if type(globalState) ~= "table" then
        return false
    end

    local resolvedDayKey = tonumber(globalState.lastResolvedDayKey)
    local resolvedSlotKey = tonumber(globalState.lastResolvedSlotKey)
    local triggeredSlotKey = tonumber(globalState.lastTriggeredSlot)

    if resolvedSlotKey == scheduledSlotKey then
        return true
    end
    if triggeredSlotKey == scheduledSlotKey then
        return true
    end
    if resolvedDayKey == dayKey and globalState.consumeRoll ~= true then
        return true
    end

    return false
end

local function requestAmbushSpawn(playerObj)
    local state = getAmbushState(playerObj)
    if not state then
        return
    end

    local dayKey = getDeployAmbushDayKey()
    local currentHourOfDay = getCurrentHourOfDay()
    if dayKey == nil or currentHourOfDay == nil then
        return
    end

    local scheduledSlotKey, scheduledHourOfDay, globalState = getScheduledDailySlot(playerObj, dayKey)
    if scheduledSlotKey == nil or scheduledHourOfDay == nil then
        return
    end

    local currentSlotKey = getDeployAmbushSlotKey(dayKey, currentHourOfDay)
    if currentSlotKey ~= scheduledSlotKey then
        return
    end

    if isSlotResolvedOnServer(globalState, dayKey, scheduledSlotKey) then
        return
    end

    local graceRemainingMinutes = getDeployAmbushGraceRemainingMinutes(playerObj)
    if graceRemainingMinutes > 0 then
        if state.lastGracePeriodSlot ~= scheduledSlotKey then
            state.lastGracePeriodSlot = scheduledSlotKey
            persistAmbushState(playerObj)
            logInfo("Skipped ambush request: deploy grace period active for "
                .. formatHourSlotClockTime(scheduledHourOfDay)
                .. " (" .. tostring(graceRemainingMinutes) .. " minute(s) remaining).")
        end
        return
    end

    if type(globalState.consumeRoll) ~= "boolean" then
        if state.lastMissingGlobalSlot ~= scheduledSlotKey then
            state.lastMissingGlobalSlot = scheduledSlotKey
            persistAmbushState(playerObj)
            requestGlobalAmbushState()
            logInfo("Missing global ambush state for slot "
                .. tostring(scheduledSlotKey) .. "; requested ModData sync.")
        end
        return
    end

    if globalState.consumeRoll ~= true then
        if state.lastConsumeRollMissSlot ~= scheduledSlotKey then
            state.lastConsumeRollMissSlot = scheduledSlotKey
            persistAmbushState(playerObj)
            logInfo("Skipped ambush request: 8-hour 50% consume roll failed for "
                .. formatHourSlotClockTime(scheduledHourOfDay) .. ".")
        end
        return
    end

    local args = {
        efzDeployAmbushSlotKey = scheduledSlotKey,
        efzDeployAmbushDayKey = dayKey,
        efzDeployAmbushHourOfDay = scheduledHourOfDay,
    }

    sendClientCommand(playerObj, "EFZ", "RequestDeployAmbush", args)

    if state.lastRequestLogSlot ~= scheduledSlotKey then
        state.lastRequestLogSlot = scheduledSlotKey
        logInfo("Requested deploy ambush for player "
            .. tostring(playerObj:getPlayerNum())
            .. " during " .. formatHourSlotClockTime(scheduledHourOfDay) .. ".")
    end
end

local function onEveryMinute()
    if not isAmbushEnabled() then
        return
    end

    if not isBandits2Active() then
        return
    end

    if not sendClientCommand then
        return
    end

    local activePlayers = getNumActivePlayers and getNumActivePlayers() or 1
    for playerIndex = 0, activePlayers - 1 do
        local playerObj = getSpecificPlayer and getSpecificPlayer(playerIndex) or nil
        if playerObj and not playerObj:isDead() and isDeployInProgress(playerObj) then
            requestAmbushSpawn(playerObj)
        end
    end
end

local function onInitGlobalModData()
    requestGlobalAmbushState()
    logInfo("Requested global ambush ModData on init.")
end

local function onReceiveGlobalModData(key, data)
    if key ~= GLOBAL_MODDATA_KEY then
        return
    end

    local consumeRoll = data and data.consumeRoll
    local scheduledSlotKey = data and tonumber(data.scheduledSlotKey)
    logInfo("Received global ambush ModData (scheduledSlotKey="
        .. tostring(scheduledSlotKey)
        .. ", consumeRoll=" .. tostring(consumeRoll) .. ").")
end

local function onServerCommand(module, command, args)
    if module ~= "EFZ" or command ~= "SyncDeployAmbushResult" then
        return
    end

    local playerNum = args and tonumber(args.playerNum)
    local playerObj = nil
    if playerNum ~= nil and getSpecificPlayer then
        playerObj = getSpecificPlayer(playerNum)
    end
    if not playerObj and getPlayer then
        playerObj = getPlayer()
    end

    local state = playerObj and getAmbushState(playerObj) or nil
    if not state then
        return
    end

    local slotKey = tonumber(args and args.slotKey)
    local success = args and args.success == true
    local reason = args and tostring(args.reason) or ""

    if success then
        if slotKey ~= nil then
            state.lastTriggeredSlot = slotKey
        end
        state.lastGracePeriodSlot = nil
        state.lastMissingGlobalSlot = nil
        state.lastConsumeRollMissSlot = nil
        persistAmbushState(playerObj)
        logInfo("Server confirmed deploy ambush spawn for slot " .. tostring(slotKey) .. ".")
        return
    end

    if reason == "deployAmbushRollMiss" and slotKey ~= nil then
        state.lastConsumeRollMissSlot = slotKey
        persistAmbushState(playerObj)
    end

    logInfo("Server rejected deploy ambush for slot " .. tostring(slotKey) .. " (reason=" .. reason .. ").")
end

if Events and Events.OnInitGlobalModData and type(Events.OnInitGlobalModData.Add) == "function" then
    Events.OnInitGlobalModData.Add(onInitGlobalModData)
end

if Events and Events.OnReceiveGlobalModData and type(Events.OnReceiveGlobalModData.Add) == "function" then
    Events.OnReceiveGlobalModData.Add(onReceiveGlobalModData)
end

if Events and Events.OnServerCommand and type(Events.OnServerCommand.Add) == "function" then
    Events.OnServerCommand.Add(onServerCommand)
end

if Events and Events.EveryOneMinute and type(Events.EveryOneMinute.Add) == "function" then
    Events.EveryOneMinute.Add(onEveryMinute)
    logInfo("EveryOneMinute hook installed.")
elseif Events and Events.EveryTenMinutes and type(Events.EveryTenMinutes.Add) == "function" then
    Events.EveryTenMinutes.Add(onEveryMinute)
    logInfo("EveryTenMinutes hook installed.")
else
    logInfo("No supported timer event found (EveryOneMinute/EveryTenMinutes).")
end

requestGlobalAmbushState()
