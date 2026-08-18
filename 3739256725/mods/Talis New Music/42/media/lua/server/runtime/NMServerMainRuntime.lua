NMServerMainRuntime = NMServerMainRuntime or {}

local SERVER_ACTIVE_LANE_INTERVAL_TICKS = 10
local SERVER_SLOW_LANE_INTERVAL_TICKS = 30
local SERVER_MAINTENANCE_LANE_INTERVAL_TICKS = 120
local SERVER_SCHEDULER_LOG_INTERVAL_MS = 5000
local SERVER_WRAPPER_LOG_INTERVAL_MS = 5000

local serverSchedulerState = NMServerMainRuntime._serverSchedulerState or {
    tick = 0,
    lastLogMs = 0,
    counters = {}
}
local serverWrapperDiag = NMServerMainRuntime._serverWrapperDiag or {
    lastLogMs = 0,
    counters = {},
    stages = {}
}

NMServerMainRuntime._serverSchedulerState = serverSchedulerState
NMServerMainRuntime._serverWrapperDiag = serverWrapperDiag
NMServerMainRuntime._tickHookInstalled = NMServerMainRuntime._tickHookInstalled == true

local function canRunAuthoritativeWorldMutation()
    if NMCore and NMCore.isMPClientRuntime and NMCore.isMPClientRuntime() == true then
        return false
    end
    if NMAuthorityContract and NMAuthorityContract.canMutateDurableStateAtRuntime then
        return NMAuthorityContract.canMutateDurableStateAtRuntime() == true
    end
    return true
end

local function isMultiplayerRuntime()
    return NMCore and NMCore.isMultiplayerMode and NMCore.isMultiplayerMode() == true
end

local function isMPServerAuthority()
    return NMCore and NMCore.isMPServerAuthority and NMCore.isMPServerAuthority() == true
end

local function nowRealMs()
    if getTimestampMs then
        local ms = tonumber(getTimestampMs())
        if ms then
            return ms
        end
    end
    if getTimestamp then
        local ts = tonumber(getTimestamp())
        if ts then
            return ts * 1000
        end
    end
    return 0
end

local function shouldRunCadence(interval)
    local tick = tonumber(serverSchedulerState.tick) or 0
    return interval > 0 and (tick % interval) == 0
end

local function countScheduler(name)
    if not (NMCore and NMCore.isSubsystemDebugEnabled and NMCore.isSubsystemDebugEnabled("memory") == true) then
        return
    end
    local key = tostring(name or "unknown")
    serverSchedulerState.counters[key] = (tonumber(serverSchedulerState.counters[key]) or 0) + 1
end

local function countWrapper(name)
    if not (NMCore and NMCore.isSubsystemDebugEnabled and NMCore.isSubsystemDebugEnabled("memory") == true) then
        return
    end
    local key = tostring(name or "unknown")
    serverWrapperDiag.counters[key] = (tonumber(serverWrapperDiag.counters[key]) or 0) + 1
end

local function beginWrapperStage()
    if not (NMCore and NMCore.isSubsystemDebugEnabled and NMCore.isSubsystemDebugEnabled("memory") == true) then
        return 0
    end
    return nowRealMs()
end

local function recordWrapperStage(name, startedMs)
    if not (NMCore and NMCore.isSubsystemDebugEnabled and NMCore.isSubsystemDebugEnabled("memory") == true) then
        return
    end
    local key = tostring(name or "unknown")
    local stage = serverWrapperDiag.stages[key]
    if not stage then
        stage = { count = 0, sumMs = 0, maxMs = 0 }
        serverWrapperDiag.stages[key] = stage
    end
    local elapsedMs = math.max(0, nowRealMs() - (tonumber(startedMs) or 0))
    stage.count = (tonumber(stage.count) or 0) + 1
    stage.sumMs = (tonumber(stage.sumMs) or 0) + elapsedMs
    stage.maxMs = math.max(tonumber(stage.maxMs) or 0, elapsedMs)
end

local function flushScheduler()
    if not (NMCore and NMCore.logChannel and NMCore.isSubsystemDebugEnabled and NMCore.isSubsystemDebugEnabled("memory") == true) then
        return
    end
    local nowMs = nowRealMs()
    if (nowMs - (tonumber(serverSchedulerState.lastLogMs) or 0)) < SERVER_SCHEDULER_LOG_INTERVAL_MS then
        return
    end
    serverSchedulerState.lastLogMs = nowMs
    local parts = {}
    for name, count in pairs(serverSchedulerState.counters) do
        parts[#parts + 1] = string.format("%s=%d", tostring(name), tonumber(count) or 0)
        serverSchedulerState.counters[name] = nil
    end
    if #parts > 0 then
        NMCore.logChannel("memory", "server_tick_scheduler", table.concat(parts, " | "))
    end
end

local function flushWrapperDiag()
    if not (NMCore and NMCore.logChannel and NMCore.isSubsystemDebugEnabled and NMCore.isSubsystemDebugEnabled("memory") == true) then
        return
    end
    local nowMs = nowRealMs()
    if (nowMs - (tonumber(serverWrapperDiag.lastLogMs) or 0)) < SERVER_WRAPPER_LOG_INTERVAL_MS then
        return
    end
    serverWrapperDiag.lastLogMs = nowMs
    local parts = {}
    for name, count in pairs(serverWrapperDiag.counters) do
        parts[#parts + 1] = string.format("%s=%d", tostring(name), tonumber(count) or 0)
        serverWrapperDiag.counters[name] = nil
    end
    for name, stage in pairs(serverWrapperDiag.stages) do
        local count = tonumber(stage.count) or 0
        if count > 0 then
            parts[#parts + 1] = string.format(
                "%s=count:%d avgMs:%.3f maxMs:%.3f",
                tostring(name),
                count,
                (tonumber(stage.sumMs) or 0) / count,
                tonumber(stage.maxMs) or 0
            )
        end
        serverWrapperDiag.stages[name] = nil
    end
    if #parts > 0 then
        NMCore.logChannel("memory", "server_tick_wrapper", table.concat(parts, " | "))
    end
end

function NMServerMainRuntime.getActiveZombieExecutor()
    local strategy = NMZombieLiveStrategy and NMZombieLiveStrategy.getLiveVisualStrategy and NMZombieLiveStrategy.getLiveVisualStrategy() or ""
    if strategy == "sp_runtime_attach" then
        return NMServerSPZombieAssignmentFlow
    end
    if strategy == "mp_assignment_flow" then
        return NMServerMPZombieAssignmentFlow
    end
    return nil
end

function NMServerMainRuntime.shouldRegisterZombieUpdateHook(activeZombieExecutor)
    local executor = activeZombieExecutor or NMServerMainRuntime.getActiveZombieExecutor()
    if executor == NMServerSPZombieAssignmentFlow then
        return false, "sp_scan_only"
    end
    if not (executor and executor.onZombieUpdate) then
        return false, "missing_handler"
    end
    return true, "direct_executor"
end

local function shouldRunTargetPublisher()
    return canRunAuthoritativeWorldMutation()
        and isMultiplayerRuntime()
        and NMServerZombieVisualTargetPublisher
        and NMServerZombieVisualTargetPublisher.onTick
end

local function hasLegacyServerTickHandler()
    return type(NMDevicesServer) == "table" and NMDevicesServer.onTick ~= nil
end

local function hasLegacyServerTickPending(mpAuthority)
    if hasLegacyServerTickHandler() ~= true then
        return false
    end
    if mpAuthority == true then
        return true
    end
    return type(NMDevicesServer) == "table"
        and NMDevicesServer.hasPendingWork
        and NMDevicesServer.hasPendingWork() == true
end

function NMServerMainRuntime.advanceSchedulerTick()
    serverSchedulerState.tick = (tonumber(serverSchedulerState.tick) or 0) + 1
    local activeZombieExecutor = NMServerMainRuntime.getActiveZombieExecutor()
    if activeZombieExecutor and activeZombieExecutor.observeSchedulerTick and canRunAuthoritativeWorldMutation() then
        activeZombieExecutor.observeSchedulerTick(1)
    end
end

function NMServerMainRuntime.hasAnyTickWork()
    local canMutate = canRunAuthoritativeWorldMutation()
    local mpAuthority = isMPServerAuthority()
    if mpAuthority then
        return true, "mp_runtime"
    end
    if canMutate and NMServerZombieCorpseCarry and NMServerZombieCorpseCarry.hasPendingWork and NMServerZombieCorpseCarry.hasPendingWork() == true then
        return true, "corpse_carry_pending"
    end
    local activeZombieExecutor = NMServerMainRuntime.getActiveZombieExecutor()
    local hasZombieExecutor = activeZombieExecutor ~= nil and activeZombieExecutor.onTick ~= nil and canMutate
    if hasZombieExecutor then
        local executorActiveInterest = activeZombieExecutor.hasActiveWork and activeZombieExecutor.hasActiveWork() == true or false
        local executorMaintenanceInterest = activeZombieExecutor.hasMaintenanceWork and activeZombieExecutor.hasMaintenanceWork() == true or false
        if executorActiveInterest then
            return true, "executor_active_interest"
        end
        if executorActiveInterest ~= true and executorMaintenanceInterest == true then
            return true, "executor_maintenance_interest"
        end
    end
    if hasLegacyServerTickPending(mpAuthority) == true then
        return true, "legacy_tick_pending"
    end
    return false, "idle"
end

function NMServerMainRuntime.isTickHookInstalled()
    return NMServerMainRuntime._tickHookInstalled == true
end

function NMServerMainRuntime.installTickHook()
    if NMServerMainRuntime._tickHookInstalled == true then
        return
    end
    if Events and Events.OnTick and Events.OnTick.Add then
        Events.OnTick.Add(NMServerMainRuntime.onTick)
        NMServerMainRuntime._tickHookInstalled = true
    end
end

function NMServerMainRuntime.removeTickHook()
    if NMServerMainRuntime._tickHookInstalled ~= true then
        return
    end
    if Events and Events.OnTick and Events.OnTick.Remove then
        Events.OnTick.Remove(NMServerMainRuntime.onTick)
        NMServerMainRuntime._tickHookInstalled = false
    end
end

function NMServerMainRuntime.onClientCommand(module, command, player, args)
    if NMServerIntentRouter and NMServerIntentRouter.onClientCommand then
        NMServerIntentRouter.onClientCommand(module, command, player, args)
    end
    if type(NMDevicesServer) == "table" and NMDevicesServer.onClientCommand then
        NMDevicesServer.onClientCommand(module, command, player, args)
    end
end

function NMServerMainRuntime.onTick()
    local wrapperPreStartedMs = beginWrapperStage()
    countWrapper("wrapper_tick")
    local activeLaneDue = shouldRunCadence(SERVER_ACTIVE_LANE_INTERVAL_TICKS)
    local slowLaneDue = shouldRunCadence(SERVER_SLOW_LANE_INTERVAL_TICKS)
    local maintenanceLaneDue = shouldRunCadence(SERVER_MAINTENANCE_LANE_INTERVAL_TICKS)
    local mpAuthority = isMPServerAuthority()
    local canMutate = canRunAuthoritativeWorldMutation()
    local hasZombieExecutor = false
    local executorActiveInterest = false
    local executorShouldRunActiveLane = false
    local executorShouldRunMaintenanceLane = false
    local executorMaintenanceInterest = false
    local activeZombieExecutor = nil
    local corpseCarryPending = false
    local hasLegacyServerTick = hasLegacyServerTickHandler()
    local legacyServerTickPending = hasLegacyServerTickPending(mpAuthority)

    local executorLookupStartedMs = beginWrapperStage()
    activeZombieExecutor = NMServerMainRuntime.getActiveZombieExecutor()
    recordWrapperStage("server_wrapper_executor_lookup", executorLookupStartedMs)
    hasZombieExecutor = activeZombieExecutor ~= nil and activeZombieExecutor.onTick ~= nil and canMutate
    corpseCarryPending = canMutate
        and NMServerZombieCorpseCarry
        and NMServerZombieCorpseCarry.hasPendingWork
        and NMServerZombieCorpseCarry.hasPendingWork() == true
    countWrapper(activeLaneDue and "active_lane_due" or "active_lane_not_due")
    countWrapper(slowLaneDue and "slow_lane_due" or "slow_lane_not_due")
    countWrapper(maintenanceLaneDue and "maintenance_lane_due" or "maintenance_lane_not_due")
    countWrapper(mpAuthority and "mp_authority" or "sp_authority_or_client")
    countWrapper(canMutate and "can_mutate" or "cannot_mutate")
    countWrapper(hasZombieExecutor and "has_zombie_executor" or "no_zombie_executor")
    countWrapper(corpseCarryPending and "corpse_carry_pending" or "corpse_carry_idle")
    countWrapper(hasLegacyServerTick and "legacy_server_tick_present" or "legacy_server_tick_absent")
    countWrapper(legacyServerTickPending and "legacy_server_tick_pending" or "legacy_server_tick_idle")
    countWrapper(hasZombieExecutor and "executor_observe_scheduler_tick" or "executor_observe_scheduler_tick_skip")
    recordWrapperStage("server_wrapper_pre", wrapperPreStartedMs)

    local mpBranchStartedMs = beginWrapperStage()
    if mpAuthority and NMServerVehicleTrackSchedulerTick and NMServerVehicleTrackSchedulerTick.onTick and NMServerVehicleTrackSchedulerTick.hasWorldSources and NMServerVehicleTrackSchedulerTick.hasWorldSources() then
        countScheduler("vehicle_track_run")
        NMServerVehicleTrackSchedulerTick.onTick()
    else
        countScheduler("vehicle_track_skip")
    end
    if mpAuthority and NMServerSourceRefreshTick and NMServerSourceRefreshTick.onTick and NMServerSourceRefreshTick.hasWorldSources and NMServerSourceRefreshTick.hasWorldSources() then
        countScheduler("source_refresh_run")
        NMServerSourceRefreshTick.onTick()
    else
        countScheduler("source_refresh_skip")
    end
    if mpAuthority and slowLaneDue and NMServerModeReconcile and NMServerModeReconcile.onTick then
        countScheduler("mode_reconcile_run")
        NMServerModeReconcile.onTick()
    else
        countScheduler("mode_reconcile_skip")
    end
    if mpAuthority and activeLaneDue and NMServerZombiePulseTick and NMServerZombiePulseTick.onTick and NMServerZombiePulseTick.hasActiveWork and NMServerZombiePulseTick.hasActiveWork() then
        countScheduler("zombie_pulse_run")
        NMServerZombiePulseTick.onTick()
    else
        countScheduler("zombie_pulse_skip")
    end
    recordWrapperStage("server_wrapper_mp_branches", mpBranchStartedMs)

    if corpseCarryPending and NMServerZombieCorpseCarry and NMServerZombieCorpseCarry.onTick then
        countWrapper("corpse_carry_tick")
        NMServerZombieCorpseCarry.onTick()
    else
        countWrapper(corpseCarryPending and "corpse_carry_no_handler" or "corpse_carry_noop_exit")
    end

    local executorRoutingStartedMs = beginWrapperStage()
    if hasZombieExecutor then
        local executorInterestStartedMs = beginWrapperStage()
        executorActiveInterest = activeZombieExecutor.hasActiveWork and activeZombieExecutor.hasActiveWork() == true or false
        executorMaintenanceInterest = activeZombieExecutor.hasMaintenanceWork and activeZombieExecutor.hasMaintenanceWork() == true or false
        recordWrapperStage("server_wrapper_executor_interest", executorInterestStartedMs)
        executorShouldRunActiveLane = executorActiveInterest and activeLaneDue
        executorShouldRunMaintenanceLane = executorActiveInterest ~= true and executorMaintenanceInterest == true and maintenanceLaneDue
        countWrapper(executorActiveInterest and "executor_active_interest" or "executor_idle_interest")
        countWrapper(executorMaintenanceInterest and "executor_maintenance_interest" or "executor_maintenance_idle")
        countWrapper(executorShouldRunActiveLane and "executor_active_lane_due" or "executor_active_lane_not_due")
        countWrapper(executorShouldRunMaintenanceLane and "executor_maintenance_due" or "executor_maintenance_not_due")
        if executorShouldRunActiveLane or executorShouldRunMaintenanceLane then
            countScheduler("active_zombie_executor_run")
            activeZombieExecutor.onTick(executorShouldRunActiveLane and SERVER_ACTIVE_LANE_INTERVAL_TICKS or SERVER_MAINTENANCE_LANE_INTERVAL_TICKS)
        else
            countScheduler("active_zombie_executor_skip")
            countWrapper("executor_noop_exit")
        end
    else
        countScheduler("active_zombie_executor_skip")
        countWrapper("executor_missing_noop_exit")
    end
    recordWrapperStage("server_wrapper_executor_routing", executorRoutingStartedMs)

    if mpAuthority and shouldRunTargetPublisher() and activeLaneDue then
        countScheduler("target_publisher_run")
        NMServerZombieVisualTargetPublisher.onTick()
    else
        countScheduler("target_publisher_skip")
    end
    if mpAuthority and maintenanceLaneDue and NMServerRegistryTick and NMServerRegistryTick.onTick and NMServerRegistryTick.hasWorldSources and NMServerRegistryTick.hasWorldSources() then
        countScheduler("registry_tick_run")
        NMServerRegistryTick.onTick()
    else
        countScheduler("registry_tick_skip")
    end
    if legacyServerTickPending and slowLaneDue then
        countScheduler("legacy_server_tick_run")
        NMDevicesServer.onTick()
    else
        countScheduler("legacy_server_tick_skip")
        countWrapper(hasLegacyServerTick and "legacy_server_tick_noop_exit" or "legacy_server_tick_missing")
    end
    flushScheduler()
    flushWrapperDiag()
end

function NMServerMainRuntime.onEveryOneMinute()
    if NMServerVehiclePowerTick and NMServerVehiclePowerTick.onEveryOneMinute then
        NMServerVehiclePowerTick.onEveryOneMinute()
    end
    if NMServerItemPowerTick and NMServerItemPowerTick.onEveryOneMinute then
        NMServerItemPowerTick.onEveryOneMinute()
    end
    if type(NMDevicesServer) == "table" and NMDevicesServer.onEveryOneMinute then
        NMDevicesServer.onEveryOneMinute()
    end
end

function NMServerMainRuntime.canRunAuthoritativeWorldMutation()
    return canRunAuthoritativeWorldMutation()
end

function NMServerMainRuntime.shouldLogProofVerbose()
    return NMCore and NMCore.isSubsystemDebugEnabled and NMCore.isSubsystemDebugEnabled("zombie_assignment") == true
end

function NMServerMainRuntime.shouldLogCorpseVerbose()
    return NMCore and NMCore.isSubsystemDebugEnabled and NMCore.isSubsystemDebugEnabled("zombie_corpse") == true
end

function NMServerMainRuntime.registerTickGate()
    NMServerTickGate.register({
        advanceTick = NMServerMainRuntime.advanceSchedulerTick,
        hasAnyTickWork = NMServerMainRuntime.hasAnyTickWork,
        isHookInstalled = NMServerMainRuntime.isTickHookInstalled,
        installHook = NMServerMainRuntime.installTickHook,
        removeHook = NMServerMainRuntime.removeTickHook
    })
end

return NMServerMainRuntime
