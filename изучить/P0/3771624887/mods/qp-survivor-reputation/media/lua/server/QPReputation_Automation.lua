require "QPReputation_Config"
require "QPReputation_Shared"
require "QPReputation_AutomationRegistry"
require "QPReputation_Server"

QPReputation.Automation =
    QPReputation.Automation or {}

local A = QPReputation.Automation
local S = QPReputation.Server
local R = QPReputation.AutomationRegistry

A.minuteCounter = A.minuteCounter or 0
A.handlers = A.handlers or {}

local function nowStamp()
    return tostring(os.time())
end

local function currentDayKey()
    return os.date("%Y-%m-%d")
end

local function configAutomation()
    return QPReputation.Config.Automation or {}
end

local function configHunter()
    return configAutomation().Hunter or {}
end

local function playerUsername(player)
    if not player then return nil end

    local username = player:getUsername()

    if not username or username == "" then
        return nil
    end

    return username
end

local function ensureDailyState(state)
    state.daily = state.daily or {
        day = currentDayKey(),
        points = 0,
    }

    if state.daily.day ~= currentDayKey() then
        state.daily.day = currentDayKey()
        state.daily.points = 0
    end
end

function A.settings()
    return S.getAutomationSettings()
end

function A.pathSettings(pathId, settings)
    settings = settings or A.settings()

    local paths = settings.paths or {}
    return paths[R.normalizeId(pathId)] or {}
end

function A.register(pathId, handler)
    pathId = R.normalizeId(pathId)

    if not R.get(pathId) then
        return false, "unknown_path"
    end

    if type(handler) ~= "table"
        or type(handler.scan) ~= "function"
    then
        return false, "invalid_handler"
    end

    A.handlers[pathId] = handler
    return true
end

function A.isPathEnabled(pathId, settings)
    pathId = R.normalizeId(pathId)
    settings = settings or A.settings()

    if settings.enabled ~= true then
        return false
    end

    local definition = R.get(pathId)

    if not definition
        or definition.implemented ~= true
    then
        return false
    end

    local row = A.pathSettings(pathId, settings)

    return row.enabled == true
        and A.handlers[pathId] ~= nil
end

function A.ensureProfileAutomation(profile)
    profile.automation = profile.automation or {}

    local container = profile.automation
    local previousSchema =
        tonumber(container.schemaVersion) or 0
    local changed = false

    if previousSchema < 4 then
        if previousSchema > 0
            and container.migratedFromSchema == nil
        then
            container.migratedFromSchema =
                previousSchema
            container.migratedAt = nowStamp()
        end

        container.schemaVersion = 4
        changed = true
    end

    if container.registryVersion ~= R.SchemaVersion then
        container.registryVersion = R.SchemaVersion
        changed = true
    end

    return container, changed
end

function A.ensurePathState(profile, pathId, normalizer)
    pathId = R.normalizeId(pathId)

    local definition = R.get(pathId)

    if not definition then
        return nil, false
    end

    local container, changed =
        A.ensureProfileAutomation(profile)

    local state = container[pathId]

    if type(state) ~= "table" then
        state = {}
        container[pathId] = state
        changed = true
    end

    if state.schemaVersion ~=
        definition.stateSchemaVersion
    then
        state.schemaVersion =
            definition.stateSchemaVersion
        changed = true
    end

    if type(normalizer) == "function" then
        local normalizedChanged = normalizer(state)

        if normalizedChanged == true then
            changed = true
        end
    end

    return state, changed
end

function A.buildSourceId(
    pathId,
    username,
    metric,
    target
)
    return "automation:"
        .. R.normalizeId(pathId)
        .. ":"
        .. string.lower(tostring(username or ""))
        .. ":"
        .. string.lower(tostring(metric or "progress"))
        .. ":"
        .. tostring(target)
end

function A.canAwardDaily(state, points, dailyCap)
    ensureDailyState(state)

    dailyCap = math.max(
        0,
        math.floor(tonumber(dailyCap) or 0)
    )

    if dailyCap <= 0 then
        return true
    end

    return (
        tonumber(state.daily.points) or 0
    ) + points <= dailyCap
end

function A.awardMilestone(options)
    options = options or {}

    local player = options.player
    local state = options.state
    local username = playerUsername(player)
    local pathId = R.normalizeId(options.pathId)
    local target = math.floor(
        tonumber(options.target) or 0
    )
    local points = math.floor(
        tonumber(options.points) or 0
    )
    local progress = math.floor(
        tonumber(options.progress) or 0
    )
    local metric = tostring(
        options.metric or "progress"
    )

    if not username then
        return false, "missing_username"
    end

    if not state then
        return false, "missing_state"
    end

    if not QPReputation.isValidPath(pathId) then
        return false, "invalid_path"
    end

    if target <= 0 or points <= 0 then
        return false, "invalid_milestone"
    end

    state.awarded = state.awarded or {}
    state.deferred = state.deferred or {}
    ensureDailyState(state)

    if state.awarded[tostring(target)] == true then
        return false, "already_awarded"
    end

    if not A.canAwardDaily(
        state,
        points,
        options.dailyCap
    ) then
        state.deferred[tostring(target)] = true
        return false, "daily_cap"
    end

    local sourceId = A.buildSourceId(
        pathId,
        username,
        metric,
        target
    )

    local metadata = options.metadata or {}
    metadata.sourceType =
        metadata.sourceType
            or (pathId .. "_milestone")
    metadata.automation = true
    metadata.progress = progress
    metadata.target = target

    local ok, result = S.awardExternal(
        username,
        pathId,
        points,
        sourceId,
        tostring(
            options.reason
                or "Automatic reputation milestone reached"
        ),
        "automation",
        metadata
    )

    if ok or result == "duplicate_award" then
        state.awarded[tostring(target)] = true
        state.deferred[tostring(target)] = nil

        if ok then
            state.daily.points = (
                tonumber(state.daily.points) or 0
            ) + points
        end

        return true, result
    end

    return false, result
end

function A.processMilestones(options)
    options = options or {}

    local awardedAny = false
    local milestones = options.milestones or {}
    local targetField = tostring(
        options.targetField or "target"
    )
    local progress = math.max(
        0,
        math.floor(tonumber(options.progress) or 0)
    )
    local state = options.state

    if not state then
        return false
    end

    state.awarded = state.awarded or {}

    for _, row in ipairs(milestones) do
        local target = math.floor(
            tonumber(row[targetField]) or 0
        )

        if target > 0
            and progress >= target
            and state.awarded[tostring(target)]
                ~= true
        then
            local reason = options.reasonBuilder
                and options.reasonBuilder(target, row)
                or options.reason

            local awarded = A.awardMilestone({
                player = options.player,
                state = state,
                pathId = options.pathId,
                target = target,
                points = row.points,
                progress = progress,
                metric = options.metric,
                dailyCap = options.dailyCap,
                reason = reason,
                metadata = options.metadata,
            })

            if awarded then
                awardedAny = true
            end
        end
    end

    return awardedAny
end

local function activeHunterMilestones(settings)
    local hunterSettings =
        A.pathSettings("hunter", settings)
    local hunterConfig = configHunter()

    if tonumber(hunterSettings.preset) == 2 then
        return hunterConfig.TestMilestones or {}
    end

    return hunterConfig.Milestones or {}
end

local function normalizeHunterState(state)
    local changed = false

    if state.lastLifetimeKills == nil then
        state.lastLifetimeKills = 0
        changed = true
    else
        state.lastLifetimeKills =
            tonumber(state.lastLifetimeKills) or 0
    end

    if state.trackedKills == nil then
        state.trackedKills = 0
        changed = true
    else
        state.trackedKills =
            tonumber(state.trackedKills) or 0
    end

    if type(state.awarded) ~= "table" then
        state.awarded = {}
        changed = true
    end

    if type(state.deferred) ~= "table" then
        state.deferred = {}
        changed = true
    end

    if type(state.daily) ~= "table" then
        state.daily = {
            day = currentDayKey(),
            points = 0,
        }
        changed = true
    end

    ensureDailyState(state)

    state.suspiciousJumps =
        tonumber(state.suspiciousJumps) or 0

    state.lastSuspiciousDelta =
        tonumber(state.lastSuspiciousDelta) or 0

    return changed
end

local function updateHunterNextMilestone(
    state,
    settings
)
    state.nextMilestone = nil
    state.nextReward = nil
    state.nextDeferred = false

    for _, row in ipairs(
        activeHunterMilestones(settings)
    ) do
        local target = math.floor(
            tonumber(row.kills) or 0
        )

        if target > 0
            and state.awarded[tostring(target)]
                ~= true
        then
            state.nextMilestone = target
            state.nextReward = math.floor(
                tonumber(row.points) or 0
            )
            state.nextDeferred =
                state.deferred[tostring(target)]
                    == true
            return
        end
    end
end

function A.scanHunter(
    player,
    forceSync,
    suppliedSettings
)
    local settings =
        suppliedSettings or A.settings()

    if not A.isPathEnabled("hunter", settings) then
        return false
    end

    local username = playerUsername(player)

    if not username then
        return false
    end

    local profile = S.getProfile(player, true)

    if not profile then
        return false
    end

    local state = A.ensurePathState(
        profile,
        "hunter",
        normalizeHunterState
    )

    local hunterSettings =
        A.pathSettings("hunter", settings)

    local lifetimeKills = math.max(
        0,
        math.floor(
            tonumber(player:getZombieKills()) or 0
        )
    )

    if state.baselineKills == nil then
        state.baselineKills = lifetimeKills
        state.lastLifetimeKills = lifetimeKills
        state.trackedKills = 0
        state.lastCheck = nowStamp()

        updateHunterNextMilestone(
            state,
            settings
        )

        profile.updatedAt = nowStamp()
        ModData.transmit(
            QPReputation.Config.DataKey
        )

        if forceSync and S.syncPlayer then
            S.syncPlayer(player)
        end

        return true
    end

    local previousLifetime =
        tonumber(state.lastLifetimeKills)
            or lifetimeKills

    local delta = lifetimeKills - previousLifetime

    -- Kill totals moving backwards can happen after an admin reset,
    -- character replacement or save rollback. Rebase without awarding.
    if delta < 0 then
        state.baselineKills =
            lifetimeKills - state.trackedKills
        state.lastLifetimeKills = lifetimeKills
        state.lastCheck = nowStamp()

        updateHunterNextMilestone(
            state,
            settings
        )

        profile.updatedAt = nowStamp()
        ModData.transmit(
            QPReputation.Config.DataKey
        )

        if S.syncPlayer then
            S.syncPlayer(player)
        end

        return true
    end

    local maximumDelta = math.max(
        1,
        math.floor(
            tonumber(
                hunterSettings.maxProgressPerScan
            ) or 50
        )
    )

    -- Reject abnormal jumps rather than converting them into automated
    -- Reputation. The current lifetime value becomes the new baseline,
    -- while already validated tracked progress is preserved.
    if delta > maximumDelta then
        state.suspiciousJumps =
            state.suspiciousJumps + 1
        state.lastSuspiciousDelta = delta
        state.baselineKills =
            lifetimeKills - state.trackedKills
        state.lastLifetimeKills = lifetimeKills
        state.lastCheck = nowStamp()

        print(
            "[QPSR][Automation] Suspicious Hunter kill jump ignored"
                .. " username="
                .. tostring(username)
                .. " delta="
                .. tostring(delta)
                .. " limit="
                .. tostring(maximumDelta)
        )

        updateHunterNextMilestone(
            state,
            settings
        )

        profile.updatedAt = nowStamp()
        ModData.transmit(
            QPReputation.Config.DataKey
        )

        if S.syncPlayer then
            S.syncPlayer(player)
        end

        return true
    end

    state.lastLifetimeKills = lifetimeKills
    state.trackedKills =
        state.trackedKills + delta
    state.lastCheck = nowStamp()

    local awardedAny = A.processMilestones({
        player = player,
        state = state,
        pathId = "hunter",
        progress = state.trackedKills,
        milestones =
            activeHunterMilestones(settings),
        targetField = "kills",
        metric = "kills",
        dailyCap = hunterSettings.dailyCap,
        metadata = {
            sourceType = "hunter_milestone",
        },
        reasonBuilder = function(target)
            return "Automatic: Hunter milestone reached ("
                .. tostring(target)
                .. " tracked zombie kills)"
        end,
    })

    updateHunterNextMilestone(
        state,
        settings
    )

    profile.updatedAt = nowStamp()
    ModData.transmit(
        QPReputation.Config.DataKey
    )

    if not awardedAny and S.syncPlayer then
        S.syncPlayer(player)
    end

    return true
end

function A.scanPlayer(player, forceSync)
    local settings = A.settings()
    local scannedAny = false

    for _, definition in ipairs(R.list()) do
        local pathId = definition.id
        local handler = A.handlers[pathId]

        if A.isPathEnabled(pathId, settings)
            and handler
            and type(handler.scan) == "function"
        then
            local scanned = handler.scan(
                player,
                forceSync,
                settings
            )

            if scanned then
                scannedAny = true
            end
        end
    end

    return scannedAny
end

function A.registryStatus()
    local settings = A.settings()
    local status = R.status(settings)
    local handlerCount = 0

    for _, definition in ipairs(R.list()) do
        if A.handlers[definition.id] then
            handlerCount = handlerCount + 1
        end
    end

    status.handlerCount = handlerCount
    return status
end

local registered, registerError = A.register(
    "hunter",
    {
        scan = A.scanHunter,
    }
)

if not registered then
    print(
        "[QPSR][Automation] Hunter handler registration failed: "
            .. tostring(registerError)
    )
end

local function scanOnlinePlayers()
    local settings = A.settings()

    A.minuteCounter = A.minuteCounter + 1

    if A.minuteCounter < settings.scanInterval then
        return
    end

    A.minuteCounter = 0

    local players = getOnlinePlayers()

    if not players then
        return
    end

    for index = 0, players:size() - 1 do
        local player = players:get(index)

        if player then
            A.scanPlayer(player, false)
        end
    end
end

local function onCreatePlayer(index, player)
    A.scanPlayer(player, true)
end

Events.OnCreatePlayer.Add(onCreatePlayer)
Events.EveryOneMinute.Add(scanOnlinePlayers)
