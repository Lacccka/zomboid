require "QPReputation_Config"
require "QPReputation_Shared"
require "QPReputation_AutomationRegistry"

QPReputation.Server = QPReputation.Server or {}
local S = QPReputation.Server

-- QPSR_V041_RELEASE_CANDIDATE_CLEANUP

local function nowStamp()
    return tostring(os.time())
end

local function getStore()
    local store = ModData.getOrCreate(QPReputation.Config.DataKey)
    store.schemaVersion = store.schemaVersion or 1
    store.profiles = store.profiles or {}
    return store
end

local function defaultAutomationSettings()
    local auto = QPReputation.Config.Automation or {}
    local hunter = auto.Hunter or {}
    local community = auto.Community or {}
    local paths = QPReputation.AutomationRegistry
        .defaultPathSettings()

    paths.hunter.preset =
        hunter.UseTestMilestones == true and 2 or 1

    paths.hunter.dailyCap = math.max(
        0,
        math.floor(tonumber(hunter.DailyPointCap) or 0)
    )

    paths.hunter.maxProgressPerScan = math.max(
        1,
        math.floor(tonumber(auto.MaxKillsPerScan) or 50)
    )

    paths.community.dailyCap = math.max(
        0,
        math.floor(tonumber(community.DailyPointCap) or 25)
    )
    paths.community.supplyRequestsEnabled =
        community.SupplyRequestsEnabled ~= false
    paths.community.supplyRequestPoints = math.max(
        1,
        math.floor(tonumber(community.SupplyRequestPoints) or 5)
    )
    paths.community.requireDifferentCreator =
        community.RequireDifferentCreator ~= false

    return {
        schemaVersion = 3,
        registryVersion =
            QPReputation.AutomationRegistry.SchemaVersion,
        enabled = auto.Enabled == true,
        scanInterval = math.max(
            1,
            math.floor(tonumber(auto.ScanEveryMinutes) or 1)
        ),
        paths = paths,
    }
end

local function clampInteger(value, minimum, maximum, fallback)
    value = math.floor(
        tonumber(value)
            or tonumber(fallback)
            or minimum
    )

    if value < minimum then value = minimum end
    if value > maximum then value = maximum end

    return value
end

local function applyLegacyHunterAliases(settings)
    local hunter = settings.paths
        and settings.paths.hunter
        or {}

    settings.hunterEnabled = hunter.enabled == true
    settings.preset = clampInteger(
        hunter.preset,
        1,
        2,
        1
    )
    settings.dailyCap = clampInteger(
        hunter.dailyCap,
        0,
        10000,
        145
    )
    settings.maxKillsPerScan = clampInteger(
        hunter.maxProgressPerScan,
        1,
        1000,
        50
    )
end

local function applyCommunityAliases(settings)
    local community = settings.paths
        and settings.paths.community
        or {}

    settings.communityEnabled = community.enabled == true
    settings.communityDailyCap = clampInteger(
        community.dailyCap,
        0,
        10000,
        25
    )
    settings.supplyRequestsEnabled =
        community.supplyRequestsEnabled ~= false
    settings.supplyRequestPoints = clampInteger(
        community.supplyRequestPoints,
        1,
        1000,
        5
    )
    settings.communityRequireDifferentCreator =
        community.requireDifferentCreator ~= false
end

local function applyRegistryStatus(settings)
    local status =
        QPReputation.AutomationRegistry.status(settings)

    settings.registryVersion = status.registryVersion
    settings.registeredCount = status.registeredCount
    settings.implementedCount = status.implementedCount
    settings.activeCount = status.activeCount
end

local function normalizeAutomationSettings(settings)
    local defaults = defaultAutomationSettings()
    local originalSchema =
        tonumber(settings.schemaVersion) or 1
    local changed = false

    local normalizedEnabled =
        settings.enabled == true

    if settings.enabled ~= normalizedEnabled then
        settings.enabled = normalizedEnabled
        changed = true
    end

    local normalizedInterval = clampInteger(
        settings.scanInterval,
        1,
        60,
        defaults.scanInterval
    )

    if settings.scanInterval ~= normalizedInterval then
        settings.scanInterval = normalizedInterval
        changed = true
    end

    if type(settings.paths) ~= "table" then
        settings.paths = {}
        changed = true
    end

    for _, definition in ipairs(
        QPReputation.AutomationRegistry.list()
    ) do
        local pathId = definition.id
        local row = settings.paths[pathId]
        local defaultRow = defaults.paths[pathId]

        if type(row) ~= "table" then
            row = {}
            settings.paths[pathId] = row
            changed = true
        end

        if pathId == "hunter" then
            local enabledSource = row.enabled

            if enabledSource == nil then
                if settings.hunterEnabled ~= nil then
                    enabledSource =
                        settings.hunterEnabled
                else
                    enabledSource =
                        defaultRow.enabled
                end
            end

            local normalizedPathEnabled =
                enabledSource == true

            if row.enabled ~= normalizedPathEnabled then
                row.enabled = normalizedPathEnabled
                changed = true
            end

            local presetSource = row.preset

            if presetSource == nil then
                presetSource = settings.preset
            end

            local normalizedPreset = clampInteger(
                presetSource,
                1,
                2,
                defaultRow.preset
            )

            if row.preset ~= normalizedPreset then
                row.preset = normalizedPreset
                changed = true
            end

            local capSource = row.dailyCap

            if capSource == nil then
                capSource = settings.dailyCap
            end

            local normalizedCap = clampInteger(
                capSource,
                0,
                10000,
                defaultRow.dailyCap
            )

            if row.dailyCap ~= normalizedCap then
                row.dailyCap = normalizedCap
                changed = true
            end

            local jumpSource =
                row.maxProgressPerScan

            if jumpSource == nil then
                jumpSource = row.maxKillsPerScan
            end

            if jumpSource == nil then
                jumpSource = settings.maxKillsPerScan
            end

            local normalizedMaximum = clampInteger(
                jumpSource,
                1,
                1000,
                defaultRow.maxProgressPerScan
            )

            if row.maxProgressPerScan ~=
                normalizedMaximum
            then
                row.maxProgressPerScan =
                    normalizedMaximum
                changed = true
            end
        elseif pathId == "community" then
            local enabledSource = row.enabled

            if enabledSource == nil then
                if settings.communityEnabled ~= nil then
                    enabledSource = settings.communityEnabled
                else
                    enabledSource = defaultRow.enabled
                end
            end

            local normalizedPathEnabled =
                enabledSource == true

            if row.enabled ~= normalizedPathEnabled then
                row.enabled = normalizedPathEnabled
                changed = true
            end

            local capSource = row.dailyCap

            if capSource == nil then
                capSource = settings.communityDailyCap
            end

            local normalizedCap = clampInteger(
                capSource,
                0,
                10000,
                defaultRow.dailyCap
            )

            if row.dailyCap ~= normalizedCap then
                row.dailyCap = normalizedCap
                changed = true
            end

            local supplyEnabledSource =
                row.supplyRequestsEnabled

            if supplyEnabledSource == nil then
                supplyEnabledSource =
                    settings.supplyRequestsEnabled
            end

            if supplyEnabledSource == nil then
                supplyEnabledSource =
                    defaultRow.supplyRequestsEnabled
            end

            local normalizedSupplyEnabled =
                supplyEnabledSource ~= false

            if row.supplyRequestsEnabled ~=
                normalizedSupplyEnabled
            then
                row.supplyRequestsEnabled =
                    normalizedSupplyEnabled
                changed = true
            end

            local supplyPointsSource =
                row.supplyRequestPoints

            if supplyPointsSource == nil then
                supplyPointsSource =
                    settings.supplyRequestPoints
            end

            local normalizedSupplyPoints = clampInteger(
                supplyPointsSource,
                1,
                1000,
                defaultRow.supplyRequestPoints
            )

            if row.supplyRequestPoints ~=
                normalizedSupplyPoints
            then
                row.supplyRequestPoints =
                    normalizedSupplyPoints
                changed = true
            end

            -- Survivor Tasks are intentionally not an automatic
            -- Community source. Remove stale v0.4.1 test settings.
            if row.survivorTasksEnabled ~= nil then
                row.survivorTasksEnabled = nil
                changed = true
            end

            if row.survivorTaskPoints ~= nil then
                row.survivorTaskPoints = nil
                changed = true
            end

            if settings.survivorTasksEnabled ~= nil then
                settings.survivorTasksEnabled = nil
                changed = true
            end

            if settings.survivorTaskPoints ~= nil then
                settings.survivorTaskPoints = nil
                changed = true
            end

            local requireDifferentSource =
                row.requireDifferentCreator

            if requireDifferentSource == nil then
                requireDifferentSource =
                    settings.communityRequireDifferentCreator
            end

            if requireDifferentSource == nil then
                requireDifferentSource =
                    defaultRow.requireDifferentCreator
            end

            local normalizedRequireDifferent =
                requireDifferentSource ~= false

            if row.requireDifferentCreator ~=
                normalizedRequireDifferent
            then
                row.requireDifferentCreator =
                    normalizedRequireDifferent
                changed = true
            end

            -- Remove the local-test override from saved settings.
            -- Production always blocks self-created Community rewards.
            if row.allowSelfCreatedForTesting ~= nil then
                row.allowSelfCreatedForTesting = nil
                changed = true
            end

            if settings.communityAllowSelfCreatedForTesting ~= nil then
                settings.communityAllowSelfCreatedForTesting = nil
                changed = true
            end
        elseif definition.implemented ~= true then
            if row.enabled ~= false then
                row.enabled = false
                changed = true
            end
        else
            local normalizedPathEnabled =
                row.enabled == true

            if row.enabled ~= normalizedPathEnabled then
                row.enabled = normalizedPathEnabled
                changed = true
            end
        end

        if row.schemaVersion ~= 1 then
            row.schemaVersion = 1
            changed = true
        end

        local implemented =
            definition.implemented == true

        if row.implemented ~= implemented then
            row.implemented = implemented
            changed = true
        end
    end

    if settings.schemaVersion ~= 3 then
        settings.schemaVersion = 3
        changed = true
    end

    if settings.registryVersion ~=
        QPReputation.AutomationRegistry.SchemaVersion
    then
        settings.registryVersion =
            QPReputation.AutomationRegistry.SchemaVersion
        changed = true
    end

    applyLegacyHunterAliases(settings)
    applyCommunityAliases(settings)
    applyRegistryStatus(settings)

    return settings, changed, originalSchema
end

local function migrateSandboxSettings(settings)
    local sandbox = SandboxVars
        and SandboxVars.QPSR
        or nil

    if not sandbox then
        return settings
    end

    local hunter = settings.paths.hunter

    if sandbox.AutomationEnabled ~= nil then
        settings.enabled =
            sandbox.AutomationEnabled == true
    end

    if sandbox.HunterEnabled ~= nil then
        hunter.enabled =
            sandbox.HunterEnabled == true
    end

    if sandbox.HunterMilestonePreset ~= nil then
        hunter.preset = clampInteger(
            sandbox.HunterMilestonePreset,
            1,
            2,
            hunter.preset
        )
    end

    if sandbox.ScanIntervalMinutes ~= nil then
        settings.scanInterval = clampInteger(
            sandbox.ScanIntervalMinutes,
            1,
            60,
            settings.scanInterval
        )
    end

    if sandbox.HunterDailyPointCap ~= nil then
        hunter.dailyCap = clampInteger(
            sandbox.HunterDailyPointCap,
            0,
            10000,
            hunter.dailyCap
        )
    end

    if sandbox.HunterMaxKillsPerScan ~= nil then
        hunter.maxProgressPerScan = clampInteger(
            sandbox.HunterMaxKillsPerScan,
            1,
            1000,
            hunter.maxProgressPerScan
        )
    end

    applyLegacyHunterAliases(settings)
    applyCommunityAliases(settings)
    applyRegistryStatus(settings)

    return settings
end

function S.getAutomationSettings()
    local store = getStore()

    if not store.automationSettings then
        local settings = migrateSandboxSettings(
            defaultAutomationSettings()
        )

        settings.createdAt = nowStamp()
        settings.updatedAt = nowStamp()
        settings.migratedFromSandbox = true

        store.automationSettings = settings
        ModData.transmit(QPReputation.Config.DataKey)
    end

    local settings = store.automationSettings
    local normalized, changed, originalSchema =
        normalizeAutomationSettings(settings)

    if changed then
        normalized.migratedFromSchema = originalSchema
        normalized.migratedAt = nowStamp()
        normalized.migrationVersion = "0.4.1"
        normalized.updatedAt = nowStamp()

        ModData.transmit(QPReputation.Config.DataKey)
    end

    return normalized
end

function S.setAutomationSettings(values, actor)
    local settings = S.getAutomationSettings()
    values = values or {}

    if values.enabled ~= nil then
        settings.enabled = values.enabled == true
    end

    settings.scanInterval = clampInteger(
        values.scanInterval,
        1,
        60,
        settings.scanInterval
    )

    local inputPaths =
        type(values.paths) == "table"
            and values.paths
            or {}

    for _, definition in ipairs(
        QPReputation.AutomationRegistry.list()
    ) do
        local pathId = definition.id
        local row = settings.paths[pathId]
        local input = inputPaths[pathId]

        if type(input) ~= "table" then
            input = {}
        end

        if pathId == "hunter" then
            if values.hunterEnabled ~= nil then
                row.enabled =
                    values.hunterEnabled == true
            elseif input.enabled ~= nil then
                row.enabled =
                    input.enabled == true
            end

            local preset = input.preset

            if preset == nil then
                preset = values.preset
            end

            row.preset = clampInteger(
                preset,
                1,
                2,
                row.preset
            )

            local dailyCap = input.dailyCap

            if dailyCap == nil then
                dailyCap = values.dailyCap
            end

            row.dailyCap = clampInteger(
                dailyCap,
                0,
                10000,
                row.dailyCap
            )

            local maxProgress =
                input.maxProgressPerScan

            if maxProgress == nil then
                maxProgress = input.maxKillsPerScan
            end

            if maxProgress == nil then
                maxProgress = values.maxKillsPerScan
            end

            row.maxProgressPerScan = clampInteger(
                maxProgress,
                1,
                1000,
                row.maxProgressPerScan
            )
        elseif pathId == "community" then
            if values.communityEnabled ~= nil then
                row.enabled =
                    values.communityEnabled == true
            elseif input.enabled ~= nil then
                row.enabled = input.enabled == true
            end

            local communityCap = input.dailyCap

            if communityCap == nil then
                communityCap = values.communityDailyCap
            end

            row.dailyCap = clampInteger(
                communityCap,
                0,
                10000,
                row.dailyCap
            )

            local supplyEnabled =
                input.supplyRequestsEnabled

            if supplyEnabled == nil then
                supplyEnabled =
                    values.supplyRequestsEnabled
            end

            if supplyEnabled ~= nil then
                row.supplyRequestsEnabled =
                    supplyEnabled == true
            end

            local supplyPoints =
                input.supplyRequestPoints

            if supplyPoints == nil then
                supplyPoints =
                    values.supplyRequestPoints
            end

            row.supplyRequestPoints = clampInteger(
                supplyPoints,
                1,
                1000,
                row.supplyRequestPoints
            )

            -- Ignore and purge any test-only override sent by
            -- older local clients or preserved world settings.
            row.allowSelfCreatedForTesting = nil
            settings.communityAllowSelfCreatedForTesting = nil
        elseif definition.implemented == true then
            if input.enabled ~= nil then
                row.enabled = input.enabled == true
            end
        else
            row.enabled = false
        end
    end

    settings.updatedAt = nowStamp()
    settings.updatedBy = tostring(actor or "admin")

    normalizeAutomationSettings(settings)
    ModData.transmit(QPReputation.Config.DataKey)

    return settings
end

function S.resetAutomationSettings(actor)
    local store = getStore()
    local defaults = defaultAutomationSettings()

    defaults.createdAt = nowStamp()
    defaults.updatedAt = nowStamp()
    defaults.updatedBy = tostring(actor or "admin")
    defaults.migratedFromSandbox = false

    normalizeAutomationSettings(defaults)

    store.automationSettings = defaults
    ModData.transmit(QPReputation.Config.DataKey)

    return defaults
end

function S.getAutomationRegistryStatus()
    return QPReputation.AutomationRegistry.status(
        S.getAutomationSettings()
    )
end

local function sendAutomationSettings(player)
    if not player then return end

    sendServerCommand(
        player,
        "QPReputation",
        "AutomationSettings",
        {
            settings = S.getAutomationSettings()
        }
    )
end

S.sendAutomationSettings = sendAutomationSettings

local function playerKey(player)
    if not player then return nil end
    local username = player:getUsername()
    if username and username ~= "" then return string.lower(username), username end
    return nil
end

function S.getProfileByUsername(username, create)
    if not username or username == "" then return nil end
    local store = getStore()
    local key = string.lower(username)
    local profile = store.profiles[key]
    if not profile and create then
        profile = {
            schemaVersion = 1,
            username = username,
            createdAt = nowStamp(),
            updatedAt = nowStamp(),
            reputation = {},
            history = {},
        }
        for _, path in ipairs(QPReputation.Paths) do
            profile.reputation[path] = { points = 0, lifetimePoints = 0 }
        end
        store.profiles[key] = profile
        ModData.transmit(QPReputation.Config.DataKey)
    end
    return profile
end

function S.getProfile(player, create)
    local key, username = playerKey(player)
    if not key then return nil end
    local profile = S.getProfileByUsername(username, create)
    if profile then profile.username = username end
    return profile
end

local function pushHistory(profile, entry)
    profile.history = profile.history or {}
    table.insert(profile.history, 1, entry)
    while #profile.history > QPReputation.Config.MaxHistoryEntries do
        table.remove(profile.history)
    end
end

local function syncPlayer(player, notification)
    if not player then return end
    local profile = S.getProfile(player, true)
    sendServerCommand(
        player,
        "QPReputation",
        "Profile",
        {
            profile = profile,
            notification = notification,
            automationSettings = S.getAutomationSettings()
        }
    )
end

S.syncPlayer = syncPlayer

function S.change(username, path, delta, actor, reason, mode)
    if not QPReputation.isValidPath(path) then return false, "invalid_path" end
    local profile = S.getProfileByUsername(username, true)
    if not profile then return false, "profile_not_found" end
    path = string.lower(path)
    local row = profile.reputation[path] or { points = 0, lifetimePoints = 0 }
    local oldPoints = QPReputation.clampPoints(row.points)
    local oldLevel = QPReputation.getLevel(oldPoints)
    local newPoints
    if mode == "set" then newPoints = QPReputation.clampPoints(delta) else newPoints = QPReputation.clampPoints(oldPoints + (tonumber(delta) or 0)) end
    local effective = newPoints - oldPoints
    row.points = newPoints
    if effective > 0 then row.lifetimePoints = QPReputation.clampPoints(row.lifetimePoints + effective) end
    profile.reputation[path] = row
    profile.updatedAt = nowStamp()
    pushHistory(profile, { time = nowStamp(), path = path, change = effective, value = newPoints, actor = actor or "system", reason = reason or "" })
    ModData.transmit(QPReputation.Config.DataKey)

    local online = getPlayerFromUsername(username)
    if online then
        local newLevel = QPReputation.getLevel(newPoints)
        syncPlayer(online, { path = path, change = effective, reason = reason or "", levelUp = newLevel > oldLevel, level = newLevel })
    end
    return true, profile
end

function S.reset(username, target, actor, reason)
    local profile = S.getProfileByUsername(username, false)
    if not profile then return false, "profile_not_found" end
    if target == "all" then
        for _, path in ipairs(QPReputation.Paths) do S.change(username, path, 0, actor, reason, "set") end
        return true
    end
    return S.change(username, target, 0, actor, reason, "set")
end


-- QPSR_EXTERNAL_AWARD_API_V1
-- Public, server-only integration API for QP and third-party mods.
-- sourceId must be stable and unique for the accomplishment being rewarded.
local function getAwardLedger()
    local store = getStore()
    store.awardLedger = store.awardLedger or {}
    return store.awardLedger
end

function S.hasExternalAward(sourceId)
    sourceId = tostring(sourceId or "")
    if sourceId == "" then return false end
    return getAwardLedger()[sourceId] ~= nil
end

function S.awardExternal(
    username,
    path,
    points,
    sourceId,
    reason,
    actor,
    metadata
)
    username = tostring(username or "")
    path = string.lower(tostring(path or ""))
    points = math.floor(tonumber(points) or 0)
    sourceId = tostring(sourceId or "")

    if username == "" then return false, "invalid_username" end
    if not QPReputation.isValidPath(path) then return false, "invalid_path" end
    if points <= 0 then return false, "invalid_points" end
    if sourceId == "" then return false, "missing_source_id" end

    local ledger = getAwardLedger()
    if ledger[sourceId] ~= nil then
        return false, "duplicate_award"
    end

    local ok, result = S.change(
        username,
        path,
        points,
        actor or "integration",
        reason or "External reputation award",
        "add"
    )

    if not ok then return false, result end

    local awardedProfile = S.getProfileByUsername(username, false)
    local newestHistory = awardedProfile
        and awardedProfile.history
        and awardedProfile.history[1]

    if newestHistory then
        newestHistory.sourceId = sourceId
        newestHistory.sourceType = metadata
            and metadata.sourceType
            or "integration"
        newestHistory.automation = metadata
            and metadata.automation == true
            or false
        newestHistory.progress = metadata
            and metadata.progress
            or nil
        newestHistory.target = metadata
            and metadata.target
            or nil
    end

    ledger[sourceId] = {
        username = username,
        path = path,
        points = points,
        reason = reason or "",
        actor = actor or "integration",
        awardedAt = nowStamp(),
        sourceType = metadata and metadata.sourceType or "integration",
        automation = metadata and metadata.automation == true or false,
        progress = metadata and metadata.progress or nil,
        target = metadata and metadata.target or nil,
    }
    ModData.transmit(QPReputation.Config.DataKey)
    return true, result
end

function S.getExternalAward(sourceId)
    sourceId = tostring(sourceId or "")
    if sourceId == "" then return nil end
    return getAwardLedger()[sourceId]
end


-- QPSR_SURVIVOR_TASK_API_V042

function S.awardSurvivorTask(username, path, points, eventId, reason, actor, metadata)
    username = tostring(username or "")
    path = string.lower(tostring(path or ""))
    points = math.floor(tonumber(points) or 0)
    eventId = string.lower(tostring(eventId or ""))
    metadata = type(metadata) == "table" and metadata or {}
    if username == "" then return false, "invalid_username" end
    if not QPReputation.isValidPath(path) then return false, "invalid_path" end
    if points < 1 or points > 3 then return false, "invalid_task_points" end
    if eventId == "" then return false, "missing_event_id" end
    local creator = string.lower(tostring(metadata.creatorUsername or metadata.createdBy or ""))
    if creator ~= "" and creator == string.lower(username) then return false, "self_created_activity" end
    local sourceId = "automation:survivor_task:" .. eventId .. ":participant:" .. string.lower(username)
    if S.hasExternalAward(sourceId) then return false, "duplicate_award" end
    local settings = S.getAutomationSettings()
    if settings.enabled ~= true then return false, "automation_disabled" end
    local pathSettings = settings.paths and settings.paths[path] or {}
    if pathSettings.enabled ~= true then return false, "path_automation_disabled" end
    local profile = S.getProfileByUsername(username, true)
    if not profile then return false, "profile_not_found" end
    profile.automation = profile.automation or {}
    profile.automation.survivorTasks = profile.automation.survivorTasks or {}
    local state = profile.automation.survivorTasks[path]
    if type(state) ~= "table" then state = {}; profile.automation.survivorTasks[path] = state end
    local day = os.date("%Y-%m-%d")
    state.daily = type(state.daily) == "table" and state.daily or {day=day, points=0}
    if state.daily.day ~= day then state.daily.day = day; state.daily.points = 0 end
    local dailyCap = math.max(0, math.floor(tonumber(pathSettings.dailyCap) or 0))
    if dailyCap > 0 and (tonumber(state.daily.points) or 0) + points > dailyCap then
        return false, "daily_cap"
    end
    local ok, result = S.awardExternal(username, path, points, sourceId,
        tostring(reason or "QP Survivor Task validated"), tostring(actor or "qpst_validation"),
        {sourceType="survivor_task", automation=true})
    if ok then
        state.daily.points = (tonumber(state.daily.points) or 0) + points
        state.awardedEvents = (tonumber(state.awardedEvents) or 0) + 1
        state.lastAwardAt = nowStamp()
        profile.updatedAt = nowStamp()
        ModData.transmit(QPReputation.Config.DataKey)
    end
    return ok, result
end

-- QPSR_COMMUNITY_AUTOMATION_API_V041
-- QPSR_COMMUNITY_LOCAL_TEST_OVERRIDE_HOTFIX_V041
-- QPSR_COMMUNITY_SUPPLY_ONLY_HOTFIX_V041
local function normalizeCommunitySource(value)
    local source = string.lower(tostring(value or ""))

    source = string.gsub(source, "%s+", "_")
    source = string.gsub(source, "%-", "_")

    if source == "supply_request"
        or source == "supply_requests"
    then
        return "supply_request"
    end

    return ""
end

local function communityDayKey()
    return os.date("%Y-%m-%d")
end

local function normalizeCommunityState(profile)
    profile.automation = profile.automation or {}

    local automation = profile.automation
    local previousSchema =
        tonumber(automation.schemaVersion) or 0

    if previousSchema < 4 then
        if previousSchema > 0
            and automation.migratedFromSchema == nil
        then
            automation.migratedFromSchema =
                previousSchema
            automation.migratedAt = nowStamp()
        end

        automation.schemaVersion = 4
    end

    automation.registryVersion =
        QPReputation.AutomationRegistry.SchemaVersion

    local state = automation.community

    if type(state) ~= "table" then
        state = {}
        automation.community = state
    end

    state.schemaVersion = 1
    state.daily = type(state.daily) == "table"
        and state.daily
        or {
            day = communityDayKey(),
            points = 0,
        }

    if state.daily.day ~= communityDayKey() then
        state.daily.day = communityDayKey()
        state.daily.points = 0
    end

    state.daily.points = math.max(
        0,
        math.floor(tonumber(state.daily.points) or 0)
    )
    state.awardedEvents = math.max(
        0,
        math.floor(tonumber(state.awardedEvents) or 0)
    )
    state.skippedDailyCap = math.max(
        0,
        math.floor(tonumber(state.skippedDailyCap) or 0)
    )
    state.skippedSelfCreated = math.max(
        0,
        math.floor(tonumber(state.skippedSelfCreated) or 0)
    )

    return state
end

function S.awardCommunityEvent(
    username,
    sourceKind,
    eventId,
    reason,
    actor,
    metadata
)
    username = tostring(username or "")
    sourceKind = normalizeCommunitySource(sourceKind)
    eventId = tostring(eventId or "")
    metadata = type(metadata) == "table"
        and metadata
        or {}

    if username == "" then
        return false, "invalid_username"
    end

    if sourceKind == "" then
        return false, "invalid_community_source"
    end

    if eventId == "" then
        return false, "missing_event_id"
    end

    local settings = S.getAutomationSettings()
    local community = settings.paths
        and settings.paths.community
        or {}

    if settings.enabled ~= true
        or community.enabled ~= true
    then
        return false, "community_automation_disabled"
    end

    local points = 0

    if sourceKind == "supply_request" then
        if community.supplyRequestsEnabled ~= true then
            return false, "community_source_disabled"
        end

        points = clampInteger(
            community.supplyRequestPoints,
            1,
            1000,
            5
        )
    end

    local creator = tostring(
        metadata.creatorUsername
            or metadata.createdBy
            or ""
    )

    local isSelfCreated =
        creator ~= ""
        and string.lower(creator)
            == string.lower(username)

    if community.requireDifferentCreator ~= false
        and isSelfCreated
    then
        local profile =
            S.getProfileByUsername(username, true)

        if profile then
            local state = normalizeCommunityState(profile)
            state.skippedSelfCreated =
                state.skippedSelfCreated + 1
            state.lastSkippedAt = nowStamp()
            state.lastSkippedReason = "self_created"
            profile.updatedAt = nowStamp()
            ModData.transmit(QPReputation.Config.DataKey)
        end

        return false, "self_created_activity"
    end

    local normalizedEventId = string.lower(eventId)
    local sourceId =
        "automation:community:"
        .. sourceKind
        .. ":"
        .. normalizedEventId
        .. ":participant:"
        .. string.lower(username)

    if S.hasExternalAward(sourceId) then
        return false, "duplicate_award"
    end

    local profile =
        S.getProfileByUsername(username, true)

    if not profile then
        return false, "profile_not_found"
    end

    local state = normalizeCommunityState(profile)
    local dailyCap = clampInteger(
        community.dailyCap,
        0,
        10000,
        25
    )

    if dailyCap > 0
        and state.daily.points + points > dailyCap
    then
        state.skippedDailyCap =
            state.skippedDailyCap + 1
        state.lastSkippedAt = nowStamp()
        state.lastSkippedReason = "daily_cap"
        profile.updatedAt = nowStamp()
        ModData.transmit(QPReputation.Config.DataKey)

        return false, "daily_cap"
    end

    local awardMetadata = {
        sourceType = "community_" .. sourceKind,
        automation = true,
        progress = metadata.progress,
        target = metadata.target,
    }

    local awardReason = tostring(
        reason
            or "Automatic Community reputation activity"
    )

    if sourceKind == "supply_request" then
        awardReason =
            "Automatic Community: Supply Request completed"
    end

    local ok, result = S.awardExternal(
        username,
        "community",
        points,
        sourceId,
        awardReason,
        tostring(actor or "community_automation"),
        awardMetadata
    )

    if ok then
        state.daily.points = state.daily.points + points
        state.awardedEvents = state.awardedEvents + 1
        state.lastAwardAt = nowStamp()
        state.lastSource = sourceKind
        profile.updatedAt = nowStamp()
        ModData.transmit(QPReputation.Config.DataKey)

        return true, result
    end

    return false, result
end

local function isAdmin(player)
    if not player then return false end

    local level = string.lower(
        tostring(player:getAccessLevel() or "")
    )

    return level == "admin"
        or level == "moderator"
        or level == "overseer"
end

-- QPSR_SERVER_PLAYER_EDITOR_SERVER_V1
local function QPSR_trimUsername(value)
    local username = tostring(value or "")

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

    return username
end

local function QPSR_sameUsername(left, right)
    return string.lower(tostring(left or ""))
        == string.lower(tostring(right or ""))
end

local function QPSR_findOnlinePlayer(username)
    username = QPSR_trimUsername(username)

    if username == "" then
        return nil
    end

    local players = getOnlinePlayers()

    if players == nil then
        return nil
    end

    for index = 0, players:size() - 1 do
        local candidate = players:get(index)

        if candidate ~= nil then
            local candidateUsername = ""

            local ok, result = pcall(function()
                return candidate:getUsername()
            end)

            if ok and result ~= nil then
                candidateUsername = tostring(result)
            end

            if QPSR_sameUsername(
                candidateUsername,
                username
            ) then
                return candidate
            end
        end
    end

    return nil
end

local function QPSR_sendAdminProfile(
    adminPlayer,
    username,
    profile,
    ok,
    edited
)
    sendServerCommand(
        adminPlayer,
        "QPReputation",
        "AdminProfile",
        {
            requestedUsername =
                tostring(username or ""),
            profile = profile,
            ok = ok == true,
            edited = edited == true
        }
    )
end

local function onClientCommand(
    module,
    command,
    player,
    args
)
    if module ~= "QPReputation" then
        return
    end

    args = args or {}

    if command == "RequestProfile" then
        syncPlayer(player)
        return
    end

    if command == "RequestAdminProfile" then
        if not isAdmin(player) then
            return
        end

        local targetUsername =
            QPSR_trimUsername(args.username)

        local profile = nil

        if targetUsername ~= "" then
            profile = S.getProfileByUsername(
                targetUsername,
                false
            )
        end

        QPSR_sendAdminProfile(
            player,
            targetUsername,
            profile,
            profile ~= nil,
            false
        )

        return
    end

    if command == "RequestAutomationSettings" then
        if isAdmin(player) then
            sendAutomationSettings(player)
        end

        return
    end

    if not isAdmin(player) then
        return
    end

    local actor = player:getUsername()

    if command == "SaveAutomationSettings" then
        local settings = S.setAutomationSettings(
            args.settings,
            actor
        )

        sendServerCommand(
            player,
            "QPReputation",
            "AutomationSettings",
            {
                settings = settings,
                saved = true
            }
        )

        syncPlayer(player)
        return
    elseif command == "ResetAutomationSettings" then
        local settings = S.resetAutomationSettings(
            actor
        )

        sendServerCommand(
            player,
            "QPReputation",
            "AutomationSettings",
            {
                settings = settings,
                reset = true
            }
        )

        syncPlayer(player)
        return
    end

    if command ~= "AdminAdd"
        and command ~= "AdminSet"
        and command ~= "AdminReset" then
        return
    end

    local targetUsername =
        QPSR_trimUsername(args.username)

    local ok = false

    if targetUsername ~= "" then
        if command == "AdminAdd" then
            ok = S.change(
                targetUsername,
                args.path,
                args.points,
                actor,
                args.reason,
                "add"
            )
        elseif command == "AdminSet" then
            ok = S.change(
                targetUsername,
                args.path,
                args.points,
                actor,
                args.reason,
                "set"
            )
        elseif command == "AdminReset" then
            ok = S.reset(
                targetUsername,
                args.path,
                actor,
                args.reason
            )
        end
    end

    local updated = nil

    if targetUsername ~= "" then
        updated = S.getProfileByUsername(
            targetUsername,
            false
        )
    end

    QPSR_sendAdminProfile(
        player,
        targetUsername,
        updated,
        ok == true,
        true
    )

    local targetPlayer =
        QPSR_findOnlinePlayer(targetUsername)

    if targetPlayer ~= nil then
        syncPlayer(targetPlayer)
    end
end

local function onCreatePlayer(index, player)
    S.getProfile(player, true)
    syncPlayer(player)
end

Events.OnClientCommand.Add(onClientCommand)
Events.OnCreatePlayer.Add(onCreatePlayer)
