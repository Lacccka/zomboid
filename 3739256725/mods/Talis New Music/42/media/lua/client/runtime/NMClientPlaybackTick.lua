require "runtime/NMClientDetachedPlaybackPass"
require "runtime/NMClientDetachedOrchestration"
require "runtime/NMClientOwnershipConflictPolicy"
require "runtime/NMClientPlaybackInventoryCollector"
require "runtime/NMClientVehicleContinuity"

-- Client playback tick orchestration for inventory devices and detached cached sources.
NMClientPlaybackTick = NMClientPlaybackTick or {}
NMClientPlaybackTick.tick = NMClientPlaybackTick.tick or 0
NMClientPlaybackTick.lastInventoryByUuid = NMClientPlaybackTick.lastInventoryByUuid or {}
NMClientPlaybackTick.vehiclePowerTickMs = NMClientPlaybackTick.vehiclePowerTickMs or {}
NMClientPlaybackTick.zombieAttractionPulseState = NMClientPlaybackTick.zombieAttractionPulseState or {}
NMClientPlaybackTick.ownershipConflictState = NMClientPlaybackTick.ownershipConflictState or {}
NMClientPlaybackTick.detachedRemoveLogMs = NMClientPlaybackTick.detachedRemoveLogMs or {}
NMClientPlaybackTick.detachedSyncSigSeen = NMClientPlaybackTick.detachedSyncSigSeen or {}
NMClientPlaybackTick.detachedSyncHeartbeatMs = NMClientPlaybackTick.detachedSyncHeartbeatMs or {}
NMClientPlaybackTick.modeResolutionSigSeen = NMClientPlaybackTick.modeResolutionSigSeen or {}
NMClientPlaybackTick.modeResolutionHeartbeatMs = NMClientPlaybackTick.modeResolutionHeartbeatMs or {}
NMClientPlaybackTick.modeResolutionBootstrapMs = NMClientPlaybackTick.modeResolutionBootstrapMs or {}
NMClientPlaybackTick.stableOffInventorySigSeen = NMClientPlaybackTick.stableOffInventorySigSeen or {}
NMClientPlaybackTick.corpseInventoryReboundSeen = NMClientPlaybackTick.corpseInventoryReboundSeen or {}
NMClientPlaybackTick.idleFullCadenceTicks = NMClientPlaybackTick.idleFullCadenceTicks or 10
NMClientPlaybackTick.activeFullCadenceTicks = NMClientPlaybackTick.activeFullCadenceTicks or 10
NMClientPlaybackTick.fastPositionCadenceTicks = NMClientPlaybackTick.fastPositionCadenceTicks or 3
NMClientPlaybackTick.schedulerTick = NMClientPlaybackTick.schedulerTick or 0
NMClientPlaybackTick._playbackDirty = NMClientPlaybackTick._playbackDirty ~= false
NMClientPlaybackTick._hasManagedInventoryDevices = NMClientPlaybackTick._hasManagedInventoryDevices or false
NMClientPlaybackTick._startupBootstrapUntilTick = NMClientPlaybackTick._startupBootstrapUntilTick or 0
NMClientPlaybackTick._lastFullTick = NMClientPlaybackTick._lastFullTick or 0
NMClientPlaybackTick._lastFullSchedulerTick = NMClientPlaybackTick._lastFullSchedulerTick or 0
NMClientPlaybackTick._forceFullPass = NMClientPlaybackTick._forceFullPass or false
NMClientPlaybackTick._lastPassKind = NMClientPlaybackTick._lastPassKind or "none"
NMClientPlaybackTick._fastSources = NMClientPlaybackTick._fastSources or {}
NMClientPlaybackTick._playbackLossProbe = NMClientPlaybackTick._playbackLossProbe or {
    lastSigByUuid = {},
    lastLogMsByUuid = {},
    lastSourceByUuid = {},
    lastTickByUuid = {},
    lastReasonByUuid = {}
}
NMClientPlaybackTick._interestDiag = NMClientPlaybackTick._interestDiag or {
    lastLogMs = 0,
    lastTick = -1,
    counters = {}
}
NMClientPlaybackTick._lastFollowPlayerPos = NMClientPlaybackTick._lastFollowPlayerPos or nil
NMClientPlaybackTick._scratch = NMClientPlaybackTick._scratch or {
    valid = {},
    inventoryOwners = {},
    currentInventoryByUuid = {},
    spPulseCandidates = {},
    inventory = {},
    detached = {},
    pendingPlayback = {}
}

local function clearMap(tbl)
    for key, _ in pairs(tbl) do
        tbl[key] = nil
    end
    return tbl
end

local function clearArray(tbl)
    for i = #tbl, 1, -1 do
        tbl[i] = nil
    end
    return tbl
end

local function logTransitionProbe(msg, detail)
    if NMCore and NMCore.logChannel and NMCore.isSubsystemDebugEnabled and NMCore.isSubsystemDebugEnabled("playback_transition") then
        NMCore.logChannel("playback_transition", msg, detail)
    end
end

local function runtimeProbeEnabled()
    return NMCore and NMCore.logChannel and NMCore.isSubsystemDebugEnabled and NMCore.isSubsystemDebugEnabled("runtime") == true
end

local function nowRealMs()
    if getTimestampMs then
        local ms = tonumber(getTimestampMs())
        if ms then return ms end
    end
    if getTimestamp then
        local ts = tonumber(getTimestamp())
        if ts then return ts * 1000 end
    end
    return 0
end

local function isMPClientRuntime()
    return NMCore and NMCore.isMPClientRuntime and NMCore.isMPClientRuntime() == true
end

local function roundProbeCoord(value)
    local n = tonumber(value)
    if n == nil then
        return "nil"
    end
    return string.format("%.2f", n)
end

local function quantizeProbeCoord(value, step)
    local n = tonumber(value)
    if n == nil then
        return "nil"
    end
    local bucket = tonumber(step) or 1.0
    if bucket <= 0 then
        bucket = 1.0
    end
    return string.format("%.1f", math.floor((n / bucket) + 0.5) * bucket)
end

local function isSteadyStatePlaybackProbeReason(reason)
    local normalizedReason = tostring(reason or "unknown")
    return normalizedReason == "inventory_sync"
        or normalizedReason == "detached_world_sync"
        or normalizedReason == "pending_world_sync"
        or normalizedReason == "full_pass_valid"
end

local function shouldEmitPlaybackLossProbe(uuid, signature, minIntervalMs)
    if runtimeProbeEnabled() ~= true then
        return false
    end
    local key = tostring(uuid or "")
    if key == "" then
        return false
    end
    local probe = NMClientPlaybackTick._playbackLossProbe
    local nowMs = nowRealMs()
    local lastSig = tostring(probe.lastSigByUuid[key] or "")
    local lastMs = tonumber(probe.lastLogMsByUuid[key]) or 0
    local intervalMs = math.max(250, tonumber(minIntervalMs) or 1000)
    if lastSig ~= tostring(signature or "") or (nowMs - lastMs) >= intervalMs then
        probe.lastSigByUuid[key] = tostring(signature or "")
        probe.lastLogMsByUuid[key] = nowMs
        return true
    end
    return false
end

local function shouldSkipSteadyStatePlaybackProbe(uuid, reason)
    local key = tostring(uuid or "")
    if key == "" then
        return false
    end
    if isSteadyStatePlaybackProbeReason(reason) ~= true then
        return false
    end
    local probe = NMClientPlaybackTick._playbackLossProbe
    local currentTick = tonumber(NMClientPlaybackTick.tick) or 0
    local lastTick = tonumber(probe.lastTickByUuid[key]) or -1
    local lastReason = tostring(probe.lastReasonByUuid[key] or "")
    return lastTick == currentTick and isSteadyStatePlaybackProbeReason(lastReason) == true
end

local function rememberPlaybackLossSource(uuid, state, source, extras)
    local key = tostring(uuid or state and state.deviceUUID or "")
    if key == "" then
        return
    end
    local probe = NMClientPlaybackTick._playbackLossProbe
    local payload = type(extras) == "table" and extras or {}
    local activeSnapshot = NMPlaybackRuntime
        and NMPlaybackRuntime.getActiveRouteProbeSnapshot
        and NMPlaybackRuntime.getActiveRouteProbeSnapshot(key)
        or nil
    if activeSnapshot and activeSnapshot.localVehiclePersonalOverride == true then
        probe.lastSourceByUuid[key] = {
            source = activeSnapshot.source,
            playbackMode = tostring(activeSnapshot.playbackMode or "personal"),
            mode = tostring(activeSnapshot.mode or activeSnapshot.context or "vehicle"),
            resolvedOutput = tostring(activeSnapshot.resolvedOutput or "personal"),
            isOn = activeSnapshot.isOn == true,
            isPlaying = activeSnapshot.isPlaying == true,
            media = tostring(activeSnapshot.media or state and state.mediaFullType or "nil"),
            sharedSource = source and {
                mode = source.mode,
                context = source.context,
                x = tonumber(source.x),
                y = tonumber(source.y),
                z = tonumber(source.z)
            } or nil,
            sharedPlaybackMode = tostring(state and state.playbackMode or payload.playbackMode or ""),
            sharedMode = tostring(payload.mode or ""),
            sharedResolvedOutput = tostring(payload.resolvedOutput or ""),
            sharedIsOn = state and state.isOn == true or false,
            sharedIsPlaying = state and state.isPlaying == true or false,
            sharedMedia = tostring(state and state.mediaFullType or "nil")
        }
        return
    end
    probe.lastSourceByUuid[key] = {
        source = source and {
            mode = source.mode,
            context = source.context,
            x = tonumber(source.x),
            y = tonumber(source.y),
            z = tonumber(source.z)
        } or nil,
        playbackMode = tostring(state and state.playbackMode or payload.playbackMode or ""),
        mode = tostring(payload.mode or ""),
        resolvedOutput = tostring(payload.resolvedOutput or ""),
        isOn = state and state.isOn == true or false,
        isPlaying = state and state.isPlaying == true or false,
        media = tostring(state and state.mediaFullType or "nil")
    }
end

local function emitPlaybackLossProbe(player, uuid, valid, reason, extras)
    if runtimeProbeEnabled() ~= true then
        return
    end
    local key = tostring(uuid or "")
    if key == "" then
        return
    end
    local probe = NMClientPlaybackTick._playbackLossProbe
    local cached = probe.lastSourceByUuid[key] or {}
    local payload = type(extras) == "table" and extras or {}
    local activeSnapshot = NMPlaybackRuntime
        and NMPlaybackRuntime.getActiveRouteProbeSnapshot
        and NMPlaybackRuntime.getActiveRouteProbeSnapshot(key)
        or nil
    if activeSnapshot and activeSnapshot.localVehiclePersonalOverride == true then
        if payload.source == nil then
            payload.source = activeSnapshot.source
        end
        if payload.context == nil then
            payload.context = activeSnapshot.context
        end
        payload.playbackMode = tostring(activeSnapshot.playbackMode or payload.playbackMode or "personal")
        payload.mode = tostring(activeSnapshot.mode or payload.mode or activeSnapshot.context or "vehicle")
        payload.resolvedOutput = tostring(activeSnapshot.resolvedOutput or payload.resolvedOutput or "personal")
        if payload.isOn == nil then
            payload.isOn = activeSnapshot.isOn == true
        end
        if payload.isPlaying == nil then
            payload.isPlaying = activeSnapshot.isPlaying == true
        end
        if payload.media == nil then
            payload.media = activeSnapshot.media
        end
    end
    local source = payload.source or cached.source or nil
    if shouldSkipSteadyStatePlaybackProbe(key, reason) == true then
        return
    end
    local isPlaying = payload.isPlaying
    if isPlaying == nil then
        isPlaying = cached.isPlaying == true
    end
    local active = NMPlaybackRuntime and NMPlaybackRuntime.Active and NMPlaybackRuntime.Active[key] or nil
    if isPlaying ~= true and valid == true and active == nil then
        return
    end
    local playerX = tonumber(player and player.getX and player:getX()) or nil
    local playerY = tonumber(player and player.getY and player:getY()) or nil
    local playerZ = tonumber(player and player.getZ and player:getZ()) or nil
    local context = tostring(payload.context or source and source.context or source and source.mode or cached.mode or "nil")
    local playbackMode = tostring(payload.playbackMode or cached.playbackMode or "nil")
    local mode = tostring(payload.mode or cached.mode or "nil")
    local resolvedOutput = tostring(payload.resolvedOutput or cached.resolvedOutput or "nil")
    local missingTick = tonumber(NMPlaybackRuntime and NMPlaybackRuntime.MissingSinceTick and NMPlaybackRuntime.MissingSinceTick[key]) or -1
    local missingMs = tonumber(NMPlaybackRuntime and NMPlaybackRuntime.MissingSinceMs and NMPlaybackRuntime.MissingSinceMs[key]) or -1
    local normalizedReason = tostring(reason or "unknown")
    local steadyStateReason = valid == true
        and missingTick < 0
        and isSteadyStatePlaybackProbeReason(normalizedReason) == true
    local occupantLocalActive = activeSnapshot and activeSnapshot.localVehiclePersonalOverride == true
    local sharedSource = occupantLocalActive and (payload.sharedSource or cached.sharedSource) or nil
    local sharedContext = tostring(payload.sharedContext or sharedSource and sharedSource.context or sharedSource and sharedSource.mode or cached.sharedMode or "nil")
    local sharedPlaybackMode = tostring(payload.sharedPlaybackMode or cached.sharedPlaybackMode or "nil")
    local sharedMode = tostring(payload.sharedMode or cached.sharedMode or "nil")
    local sharedResolvedOutput = tostring(payload.sharedResolvedOutput or cached.sharedResolvedOutput or "nil")
    local signatureReason = steadyStateReason and "steady_state_valid" or normalizedReason
    local sourceCoordStep = steadyStateReason and (occupantLocalActive and 4.0 or 1.0) or 0.1
    local sourceX = source and source.x
    local sourceY = source and source.y
    local sourceZ = source and source.z
    if steadyStateReason and occupantLocalActive then
        sourceX = nil
        sourceY = nil
        sourceZ = source and source.z
        if payload.minIntervalMs == nil then
            payload.minIntervalMs = NMRuntimeProbeAdapter and NMRuntimeProbeAdapter.shortHeartbeatMs and NMRuntimeProbeAdapter.shortHeartbeatMs() or 15000
        end
    end
    local signature = table.concat({
        signatureReason,
        tostring(valid == true),
        tostring(context),
        tostring(playbackMode),
        tostring(mode),
        tostring(resolvedOutput),
        tostring(quantizeProbeCoord(sourceX, sourceCoordStep)),
        tostring(quantizeProbeCoord(sourceY, sourceCoordStep)),
        tostring(quantizeProbeCoord(sourceZ, sourceCoordStep)),
        tostring(roundProbeCoord(playerZ)),
        tostring(missingTick),
        tostring(active ~= nil)
    }, "|")
    if shouldEmitPlaybackLossProbe(key, signature, payload.minIntervalMs) ~= true then
        return
    end
    NMCore.logChannel(
        "runtime",
        "playback_loss_probe",
        string.format(
            "uuid=%s reason=%s pass=%s valid=%s active=%s player=%s,%s,%s source=%s,%s,%s ctx=%s mode=%s playbackMode=%s output=%s effectiveLocal=%s sharedCtx=%s sharedMode=%s sharedPlaybackMode=%s sharedOutput=%s missingTick=%s missingMs=%s isOn=%s isPlaying=%s media=%s",
            tostring(key),
            tostring(reason or "unknown"),
            tostring(NMClientPlaybackTick._lastPassKind or "none"),
            tostring(valid == true),
            tostring(active ~= nil),
            roundProbeCoord(playerX),
            roundProbeCoord(playerY),
            roundProbeCoord(playerZ),
            roundProbeCoord(source and source.x),
            roundProbeCoord(source and source.y),
            roundProbeCoord(source and source.z),
            tostring(context),
            tostring(mode),
            tostring(playbackMode),
            tostring(resolvedOutput),
            tostring(occupantLocalActive == true),
            tostring(occupantLocalActive and sharedContext or "nil"),
            tostring(occupantLocalActive and sharedMode or "nil"),
            tostring(occupantLocalActive and sharedPlaybackMode or "nil"),
            tostring(occupantLocalActive and sharedResolvedOutput or "nil"),
            tostring(missingTick >= 0 and missingTick or "nil"),
            tostring(missingMs >= 0 and missingMs or "nil"),
            tostring(payload.isOn ~= nil and payload.isOn == true or cached.isOn == true),
            tostring(isPlaying == true),
            tostring(payload.media or cached.media or "nil")
        )
    )
    probe.lastTickByUuid[key] = tonumber(NMClientPlaybackTick.tick) or 0
    probe.lastReasonByUuid[key] = tostring(reason or "unknown")
end

local function observeMemoryDuration(key, elapsedMs)
    if NMPlaybackRuntimeDiagnostics and NMPlaybackRuntimeDiagnostics.observeDuration then
        NMPlaybackRuntimeDiagnostics.observeDuration(NMPlaybackRuntime, key, elapsedMs)
    end
end

local function countMemoryEvent(key, delta)
    if NMPlaybackRuntimeDiagnostics and NMPlaybackRuntimeDiagnostics.countEvent then
        NMPlaybackRuntimeDiagnostics.countEvent(NMPlaybackRuntime, key, delta)
    end
end

local function hasActivePlaybackRuntime()
    local active = NMPlaybackRuntime and NMPlaybackRuntime.Active or nil
    if type(active) ~= "table" then
        return false
    end
    for activeUuid in pairs(active) do
        return true
    end
    return false
end

local function isBootstrapActive()
    local currentTick = tonumber(NMClientPlaybackTick.schedulerTick) or 0
    return currentTick <= (tonumber(NMClientPlaybackTick._startupBootstrapUntilTick) or 0)
end

local function hasForcedFullPassPending()
    return NMClientPlaybackTick._forceFullPass == true
end

local function hasFastSchedulingHint(targetHint)
    for _, cached in pairs(NMClientPlaybackTick._fastSources or {}) do
        if type(cached) == "table" and tostring(cached.schedulingHint or "") == tostring(targetHint or "") then
            return true
        end
    end
    return false
end

local function hasAnyFastSources()
    for _, cached in pairs(NMClientPlaybackTick._fastSources or {}) do
        if type(cached) == "table" then
            return true
        end
    end
    return false
end

local function isPlayerFollowMovementActive(player)
    if not player then
        return false
    end
    local x = tonumber(player.getX and player:getX()) or 0
    local y = tonumber(player.getY and player:getY()) or 0
    local z = tonumber(player.getZ and player:getZ()) or 0
    local last = NMClientPlaybackTick._lastFollowPlayerPos
    NMClientPlaybackTick._lastFollowPlayerPos = { x = x, y = y, z = z }
    if not last then
        return true
    end
    return math.abs(x - (tonumber(last.x) or 0)) > 0.001
        or math.abs(y - (tonumber(last.y) or 0)) > 0.001
        or math.abs(z - (tonumber(last.z) or 0)) > 0.001
end

local function isFullPlaybackPassDue()
    local tick = tonumber(NMClientPlaybackTick.schedulerTick) or 0
    if hasForcedFullPassPending() then
        return true
    end
    local hasActive = hasActivePlaybackRuntime()
    local cadence = hasActive
        and math.max(1, tonumber(NMClientPlaybackTick.activeFullCadenceTicks) or 10)
        or math.max(1, tonumber(NMClientPlaybackTick.idleFullCadenceTicks) or 10)
    local lastFullTick = math.max(
        tonumber(NMClientPlaybackTick._lastFullSchedulerTick) or 0,
        tonumber(NMClientPlaybackTick._lastFullTick) or 0
    )
    return lastFullTick <= 0 or (tick - lastFullTick) >= cadence
end

local function consumeForcedFullPass()
    if hasForcedFullPassPending() ~= true then
        return false
    end
    NMClientPlaybackTick._forceFullPass = false
    return true
end

local function isStableOffInventoryMode(mode)
    local normalized = tostring(mode or "")
    return normalized ~= "attached"
        and normalized ~= "stowed"
        and normalized ~= "drop_pending"
        and normalized ~= "pickup_pending"
        and normalized ~= "placed"
        and normalized ~= "vehicle"
end

local function buildStableOffInventorySignature(item, profile, state, mode, resolvedOutput)
    if not (item and profile and state and state.deviceUUID) then
        return nil
    end
    if state.isOn == true or state.isPlaying == true then
        return nil
    end
    if isStableOffInventoryMode(mode) ~= true then
        return nil
    end
    if NMDeviceState and NMDeviceState.isZombieDormant and NMDeviceState.isZombieDormant(state) then
        return nil
    end
    local worldItem = item.getWorldItem and item:getWorldItem() or nil
    if worldItem ~= nil then
        return nil
    end
    return table.concat({
        tostring(item.getFullType and item:getFullType() or ""),
        tostring(NMCore and NMCore.itemId and NMCore.itemId(item) or ""),
        tostring(profile and profile.id or profile and profile.type or ""),
        tostring(mode or ""),
        tostring(resolvedOutput or ""),
        tostring(state.mediaFullType or "nil"),
        tostring(tonumber(state.trackIndex) or 0),
        tostring(tonumber(state.playbackEpoch) or 0),
        tostring(tonumber(state.sourceGeneration) or 0),
        tostring(tonumber(state.revision) or 0),
        tostring(state.headphoneItemFullType or "nil"),
        tostring(state.batteryPresent == true),
        string.format("%.3f", tonumber(state.batteryCharge) or 0),
        tostring(state.isMuted == true),
        string.format("%.3f", tonumber(state.volume) or 0),
        tostring(state.playbackMode or ""),
        tostring(state.authoritativeMode or ""),
        tostring(state.sourceKind or "")
    }, "|")
end

local function pruneStableOffInventorySignatures(valid)
    for uuid, _ in pairs(NMClientPlaybackTick.stableOffInventorySigSeen or {}) do
        if not (valid and valid[uuid] == true) then
            NMClientPlaybackTick.stableOffInventorySigSeen[uuid] = nil
        end
    end
end

local function noteInterestReason(reason)
    if not (NMCore and NMCore.logChannel and NMCore.isSubsystemDebugEnabled and NMCore.isSubsystemDebugEnabled("memory") == true) then
        return
    end
    local diag = NMClientPlaybackTick._interestDiag
    local currentTick = tonumber(NMClientPlaybackTick.schedulerTick) or 0
    if tonumber(diag.lastTick) == currentTick then
        return
    end
    diag.lastTick = currentTick
    local key = tostring(reason or "unknown")
    diag.counters[key] = (tonumber(diag.counters[key]) or 0) + 1
end

local function flushInterestDiag()
    if not (NMCore and NMCore.logChannel and NMCore.isSubsystemDebugEnabled and NMCore.isSubsystemDebugEnabled("memory") == true) then
        return
    end
    local diag = NMClientPlaybackTick._interestDiag
    local nowMs = nowRealMs()
    if (nowMs - (tonumber(diag.lastLogMs) or 0)) < 5000 then
        return
    end
    diag.lastLogMs = nowMs
    local parts = {}
    for name, count in pairs(diag.counters) do
        parts[#parts + 1] = string.format("%s=%d", tostring(name), tonumber(count) or 0)
        diag.counters[name] = nil
    end
    if #parts > 0 then
        NMCore.logChannel("memory", "playback_interest_diag", table.concat(parts, " | "))
    end
end

local function resolveInterestReason()
    if hasForcedFullPassPending() then
        return true, "forced_full"
    end
    if NMClientPlaybackTick._playbackDirty == true then
        return true, "dirty"
    end
    if isBootstrapActive() then
        return true, "bootstrap"
    end
    if hasActivePlaybackRuntime() then
        return true, "active_runtime"
    end
    if NMClientPlaybackTick._hasManagedInventoryDevices == true and isMPClientRuntime() ~= true then
        return true, "managed_inventory"
    end
    return false, "idle"
end

function NMClientPlaybackTick.observeSchedulerTick(tickStep)
    NMClientPlaybackTick.schedulerTick = (tonumber(NMClientPlaybackTick.schedulerTick) or 0) + math.max(1, tonumber(tickStep) or 1)
    flushInterestDiag()
end

function NMClientPlaybackTick.markDirty(_reason)
    NMClientPlaybackTick._playbackDirty = true
    if NMClientMainRuntime and NMClientMainRuntime.requestTickGateWake then
        NMClientMainRuntime.requestTickGateWake("playback_dirty")
    end
end

function NMClientPlaybackTick.beginStartupBootstrap(tickBudget)
    local nowTick = tonumber(NMClientPlaybackTick.schedulerTick) or 0
    NMClientPlaybackTick._startupBootstrapUntilTick = math.max(
        tonumber(NMClientPlaybackTick._startupBootstrapUntilTick) or 0,
        nowTick + math.max(1, tonumber(tickBudget) or 120)
    )
    NMClientPlaybackTick._playbackDirty = true
    if NMClientMainRuntime and NMClientMainRuntime.requestTickGateWake then
        NMClientMainRuntime.requestTickGateWake("playback_startup_bootstrap")
    end
end

function NMClientPlaybackTick.hasInterest()
    local interested, reason = resolveInterestReason()
    noteInterestReason(reason)
    return interested
end

function NMClientPlaybackTick.getSchedulingDecision()
    if NMClientPlaybackTick.hasInterest and NMClientPlaybackTick.hasInterest() ~= true then
        return {
            class = "idle",
            hasActive = false,
            forcedFull = false,
            fullDue = false,
            runEveryTickPosition = false,
            runFastLanePosition = false,
            runStableActive = false
        }
    end
    local hasActive = hasActivePlaybackRuntime()
    local forcedFull = hasForcedFullPassPending()
    local fullDue = isFullPlaybackPassDue()
    local hasMovingFastSource = hasFastSchedulingHint("every_tick")
    local hasFastLaneSource = hasFastSchedulingHint("fast_lane")
    local hasFastSource = hasAnyFastSources()

    if forcedFull == true then
        return {
            class = "full_reconcile_due",
            hasActive = hasActive,
            forcedFull = true,
            fullDue = true,
            runEveryTickPosition = false,
            runFastLanePosition = false,
            runStableActive = false
        }
    end
    if fullDue == true then
        return {
            class = "full_reconcile_due",
            hasActive = hasActive,
            forcedFull = false,
            fullDue = true,
            runEveryTickPosition = false,
            runFastLanePosition = false,
            runStableActive = hasActive
        }
    end
    if hasMovingFastSource == true then
        return {
            class = "position_only_due",
            hasActive = hasActive,
            forcedFull = false,
            fullDue = false,
            runEveryTickPosition = true,
            runFastLanePosition = hasFastLaneSource == true,
            runStableActive = hasActive and hasFastSource ~= true
        }
    end
    if hasFastLaneSource == true then
        return {
            class = "position_only_due",
            hasActive = hasActive,
            forcedFull = false,
            fullDue = false,
            runEveryTickPosition = false,
            runFastLanePosition = true,
            runStableActive = false
        }
    end
    if hasActive == true then
        return {
            class = "stable_active",
            hasActive = true,
            forcedFull = false,
            fullDue = false,
            runEveryTickPosition = false,
            runFastLanePosition = false,
            runStableActive = true
        }
    end
    return {
        class = "idle",
        hasActive = false,
        forcedFull = false,
        fullDue = false,
        runEveryTickPosition = false,
        runFastLanePosition = false,
        runStableActive = false
    }
end

function NMClientPlaybackTick.requestFullPass(reason)
    NMClientPlaybackTick._forceFullPass = true
    NMClientPlaybackTick._forceFullReason = tostring(reason or "requested")
    if NMClientMainRuntime and NMClientMainRuntime.requestTickGateWake then
        NMClientMainRuntime.requestTickGateWake("playback_full_pass")
    end
    NMClientPlaybackTick.markDirty(reason)
end

local function shouldLogModeResolution(uuid, signature, changed)
    if changed == true then
        return true
    end
    local key = tostring(uuid or "")
    if key == "" then
        return false
    end
    local nowMs = nowRealMs()
    local previousSig = tostring(NMClientPlaybackTick.modeResolutionSigSeen[key] or "")
    local previousMs = tonumber(NMClientPlaybackTick.modeResolutionHeartbeatMs[key]) or 0
    local currentSig = tostring(signature or "")
    local bootstrapMs = tonumber(NMClientPlaybackTick.modeResolutionBootstrapMs[key]) or 0
    if bootstrapMs <= 0 then
        bootstrapMs = nowMs
        NMClientPlaybackTick.modeResolutionBootstrapMs[key] = bootstrapMs
    end
    if previousSig == "" then
        NMClientPlaybackTick.modeResolutionSigSeen[key] = currentSig
        NMClientPlaybackTick.modeResolutionHeartbeatMs[key] = nowMs
        return false
    end
    local heartbeatMs = (nowMs - bootstrapMs) < 5000 and 60000
        or (NMRuntimeProbeAdapter and NMRuntimeProbeAdapter.longHeartbeatMs and NMRuntimeProbeAdapter.longHeartbeatMs() or 60000)
    if previousSig ~= currentSig or (nowMs - previousMs) >= heartbeatMs then
        NMClientPlaybackTick.modeResolutionSigSeen[key] = currentSig
        NMClientPlaybackTick.modeResolutionHeartbeatMs[key] = nowMs
        return previousSig ~= currentSig or (nowMs - bootstrapMs) >= 5000
    end
    return false
end

local function shouldLogDetachedSync(uuid, state, src)
    local key = tostring(uuid or "")
    if key == "" then
        return false
    end
    local detachedContext = tostring(src and src.context or "")
    local heartbeatMs = NMRuntimeProbeAdapter and NMRuntimeProbeAdapter.shortHeartbeatMs
        and NMRuntimeProbeAdapter.shortHeartbeatMs()
        or 15000
    local coordStep = 1.0
    if detachedContext == "vehicle" then
        heartbeatMs = math.max(heartbeatMs, 60000)
        coordStep = 8.0
    elseif detachedContext == "attached" or detachedContext == "stowed" then
        heartbeatMs = math.max(heartbeatMs, 30000)
        coordStep = 4.0
    end
    return NMRuntimeProbeAdapter and NMRuntimeProbeAdapter.shouldEmitBucketedTransitionOrHeartbeat
        and NMRuntimeProbeAdapter.shouldEmitBucketedTransitionOrHeartbeat(
            NMClientPlaybackTick.detachedSyncSigSeen,
            NMClientPlaybackTick.detachedSyncHeartbeatMs,
            key,
            {
                parts = {
                    tostring(detachedContext ~= "" and detachedContext or "nil"),
                    tostring(state and state.isOn == true),
                    tostring(state and state.isPlaying == true),
                    tostring(state and state.mediaFullType or "nil")
                },
                x = src and src.x or nil,
                y = src and src.y or nil,
                z = src and src.z or nil,
                coordStep = coordStep,
                zStep = 1.0,
                intervalMs = heartbeatMs
            }
        ) == true
end

local continuity = NMClientVehicleContinuity
local detachedOrchestration = NMClientDetachedOrchestration

local setVehicleIdentityState = continuity.setVehicleIdentityState
local resolveVehicleCanonicalGeneration = continuity.resolveVehicleCanonicalGeneration
local persistVehicleCanonicalGeneration = continuity.persistVehicleCanonicalGeneration
local function consumeAndDispatchTrackFinished(player, profile, state, entry, item, sourceKind, uuid)
    NMClientTrackFinishedDispatch.consumeAndDispatchTrackFinished(player, profile, state, entry, item, sourceKind, uuid)
end

local function applySPLocalVehiclePowerGuard(profile, state, source, uuid)
    NMClientSPLocalRuntime.applyVehiclePowerGuard(profile, state, source, uuid, {
        vehiclePowerTickMs = NMClientPlaybackTick.vehiclePowerTickMs,
        nowMs = nowRealMs
    })
end

local function reconcileDroppedInventoryToPlacedSP(player, currentInventoryByUuid)
    NMClientSPDropReconcile.reconcile({
        player = player,
        currentInventoryByUuid = currentInventoryByUuid,
        previousInventoryByUuid = NMClientPlaybackTick.lastInventoryByUuid,
        logTransitionProbe = logTransitionProbe
    })

    local snapshot = NMClientPlaybackTick.lastInventoryByUuid or {}
    clearMap(snapshot)
    for uuid, item in pairs(currentInventoryByUuid or {}) do
        snapshot[uuid] = item
    end
    NMClientPlaybackTick.lastInventoryByUuid = snapshot
end

local function clearPlaybackLossProbeState(uuid)
    local key = tostring(uuid or "")
    if key == "" then
        return
    end
    local probe = NMClientPlaybackTick._playbackLossProbe or nil
    if type(probe) ~= "table" then
        return
    end
    if type(probe.lastSigByUuid) == "table" then
        probe.lastSigByUuid[key] = nil
    end
    if type(probe.lastLogMsByUuid) == "table" then
        probe.lastLogMsByUuid[key] = nil
    end
    if type(probe.lastSourceByUuid) == "table" then
        probe.lastSourceByUuid[key] = nil
    end
    if type(probe.lastTickByUuid) == "table" then
        probe.lastTickByUuid[key] = nil
    end
    if type(probe.lastReasonByUuid) == "table" then
        probe.lastReasonByUuid[key] = nil
    end
end

local function clearCorpseRecoveredClientState(uuid)
    local key = tostring(uuid or "")
    if key == "" then
        return
    end
    if NMPlaybackRuntime and NMPlaybackRuntime.clearRecoveredPortableRuntime then
        NMPlaybackRuntime.clearRecoveredPortableRuntime(key, "corpse_recovered_inventory_rebind")
    elseif NMPlaybackRuntime and NMPlaybackRuntime.forceStop then
        NMPlaybackRuntime.forceStop(nil, key, "corpse_recovered_inventory_rebind")
    end
    NMClientPlaybackTick._fastSources[key] = nil
    NMClientPlaybackTick.vehiclePowerTickMs[key] = nil
    NMClientPlaybackTick.ownershipConflictState[key] = nil
    NMClientPlaybackTick.detachedRemoveLogMs[key] = nil
    NMClientPlaybackTick.detachedSyncSigSeen[key] = nil
    NMClientPlaybackTick.detachedSyncHeartbeatMs[key] = nil
    NMClientPlaybackTick.modeResolutionSigSeen[key] = nil
    NMClientPlaybackTick.modeResolutionHeartbeatMs[key] = nil
    NMClientPlaybackTick.modeResolutionBootstrapMs[key] = nil
    NMClientPlaybackTick.corpseInventoryReboundSeen[key] = nil
    NMClientPlaybackTick.lastInventoryByUuid[key] = nil
    clearPlaybackLossProbeState(key)
    if NMClientWorldSourceCache and NMClientWorldSourceCache.remove then
        NMClientWorldSourceCache.remove(key)
    end
    if NMClientPortableDropHandoff and NMClientPortableDropHandoff.clear then
        NMClientPortableDropHandoff.clear(key, "corpse_recovered_inventory_rebind")
    end
    if NMClientPortableDropHandoff and NMClientPortableDropHandoff.consumePickupRebind then
        NMClientPortableDropHandoff.consumePickupRebind(key)
    end
end

local function isFastPositionCandidate(state, source)
    if not (state and source and source.mode == "world" and source.x and source.y and source.z) then
        return false
    end
    return state.isOn == true and state.isPlaying == true and state.mediaFullType ~= nil
end

local function resolveFastSourceSchedulingHint(source, player)
    if type(source) ~= "table" then
        return nil
    end
    if source._nmFastFollowPlayer == true then
        return isPlayerFollowMovementActive(player) and "every_tick" or "fast_lane"
    end
    local context = tostring(source.context or source.mode or "")
    if context == "placed" or context == "vehicle" or context == "attached" or context == "stowed" or context == "drop_pending" then
        return "fast_lane"
    end
    if source._nmFastWorldItem then
        return "fast_lane"
    end
    return "fast_lane"
end

local function rememberFastSource(uuid, profile, state, source, player)
    local key = tostring(uuid or state and state.deviceUUID or "")
    if key == "" or isFastPositionCandidate(state, source) ~= true then
        return
    end
    NMClientPlaybackTick._fastSources[key] = {
        profile = profile,
        state = state,
        source = source,
        schedulingHint = resolveFastSourceSchedulingHint(source, player)
    }
end

local function refreshFastSource(player, cached)
    if not cached then
        return nil
    end
    local state = cached.state
    local source = cached.source
    if not isFastPositionCandidate(state, source) then
        return nil
    end

    if source._nmFastFollowPlayer == true and player then
        source.x = player.getX and player:getX() or source.x
        source.y = player.getY and player:getY() or source.y
        source.z = player.getZ and player:getZ() or source.z
        cached.schedulingHint = resolveFastSourceSchedulingHint(source, player)
        return source
    end

    local uuid = tostring(state and state.deviceUUID or "")
    local liveEntry = uuid ~= "" and NMClientWorldSourceCache and NMClientWorldSourceCache.get and NMClientWorldSourceCache.get(uuid) or nil
    if liveEntry and liveEntry.source and liveEntry.source.x and liveEntry.source.y and liveEntry.source.z then
        cached.state = liveEntry.stateSnapshot or state
        cached.source = liveEntry.source
        return cached.source
    elseif source._nmFastWorldItem and source._nmFastWorldItem.getWorldItem then
        local w = source._nmFastWorldItem:getWorldItem()
        local s = w and w.getSquare and w:getSquare() or nil
        if s then
            source.x = s:getX() + 0.5
            source.y = s:getY() + 0.5
            source.z = s:getZ()
        end
    end
    return source
end

local function runFastPositionPass(player, laneMode)
    local active = NMPlaybackRuntime and NMPlaybackRuntime.Active or nil
    if type(active) ~= "table" then
        return 0
    end

    local updated = 0
    for uuid, _ in pairs(active) do
        local key = tostring(uuid)
        local cached = NMClientPlaybackTick._fastSources[key]
        local hint = tostring(cached and cached.schedulingHint or "")
        local shouldRunCached = (laneMode == "every_tick" and hint == "every_tick")
            or (laneMode == "fast_lane" and hint == "fast_lane")
            or (laneMode == "all" and (hint == "every_tick" or hint == "fast_lane"))
            or (laneMode == nil and hint ~= "")
        if shouldRunCached ~= true then
            cached = nil
        end
        local source = refreshFastSource(player, cached)
        if cached and source == nil then
            NMClientPlaybackTick._fastSources[key] = nil
        end
        if cached and source and NMPlaybackRuntime.updateActiveEmitterPositionOnly then
            if NMPlaybackRuntime.updateActiveEmitterPositionOnly(player, uuid, cached.state, source) == true then
                updated = updated + 1
            else
                NMClientPlaybackTick._fastSources[key] = nil
            end
        end
    end
    return updated
end

function NMClientPlaybackTick.onTick(player, tickStep, passMode)
    NMClientPlaybackTick._lastPassKind = "none"
    if not player or not player.getInventory then
        return
    end

    NMClientPlaybackTick.tick = (tonumber(NMClientPlaybackTick.tick) or 0) + math.max(1, tonumber(tickStep) or 1)
    local tickStartedMs = nowRealMs()
    local mode = tostring(passMode or "auto")
    local forceFullConsumed = false
    local shouldRunFullPass = false
    local fastLaneMode = nil
    if mode == "full" then
        forceFullConsumed = consumeForcedFullPass()
        shouldRunFullPass = true
    elseif mode == "position_every_tick" then
        fastLaneMode = "every_tick"
    elseif mode == "position_all" then
        fastLaneMode = "all"
    elseif mode == "position_fast_lane" then
        fastLaneMode = "fast_lane"
    elseif isFullPlaybackPassDue() == true then
        forceFullConsumed = consumeForcedFullPass()
        shouldRunFullPass = true
    end
    if shouldRunFullPass ~= true then
        NMClientPlaybackTick._lastPassKind = "fast_position"
        local updated = runFastPositionPass(player, fastLaneMode)
        countMemoryEvent("playback_fast_position_count", 1)
        countMemoryEvent("playback_fast_position_updates", updated)
        observeMemoryDuration("playback_fast_position_tick_ms", math.max(0, nowRealMs() - tickStartedMs))
        return
    end

    NMClientPlaybackTick._lastPassKind = "full"
    NMClientPlaybackTick._lastFullTick = tonumber(NMClientPlaybackTick.tick) or 0
    NMClientPlaybackTick._lastFullSchedulerTick = tonumber(NMClientPlaybackTick.schedulerTick) or tonumber(NMClientPlaybackTick.tick) or 0
    if forceFullConsumed == true then
        NMClientPlaybackTick._forceFullReason = nil
    end
    clearMap(NMClientPlaybackTick._fastSources)

    if NMClientVanillaMusicSuppressor and NMClientVanillaMusicSuppressor.beginTick then
        NMClientVanillaMusicSuppressor.beginTick()
    end

    local tickCount = getGameTime and getGameTime():getWorldAgeHours() or 0
    local scratch = NMClientPlaybackTick._scratch
    local valid = clearMap(scratch.valid)
    local inventoryOwners = clearMap(scratch.inventoryOwners)
    local currentInventoryByUuid = clearMap(scratch.currentInventoryByUuid)
    local spPulseCandidates = clearMap(scratch.spPulseCandidates)

    local inventory = clearArray(scratch.inventory)
    local inventoryCollectStartedMs = nowRealMs()
    NMClientPlaybackInventoryCollector.collectManaged(player, inventory)
    NMClientPlaybackTick._hasManagedInventoryDevices = #inventory > 0
    local inventoryCollectElapsedMs = math.max(0, nowRealMs() - inventoryCollectStartedMs)
    observeMemoryDuration("inventory_collect_ms", inventoryCollectElapsedMs)
    local inventorySyncCount = 0
    local detachedSyncCount = 0
    local stableOffInventorySkipCount = 0
    local stableOffInventorySyncCount = 0

    for i = 1, #inventory do
        local e = inventory[i]
        NMClientPlaybackInventoryCollector.normalizeCorpseRecoveredInventoryState(e.item, e.profile, e.state, e.uuid, {
            corpseInventoryReboundSeen = NMClientPlaybackTick.corpseInventoryReboundSeen,
            cleanupRuntime = clearCorpseRecoveredClientState,
            logRuntime = function(tag, detail)
                if NMCore and NMCore.logChannel and NMCore.isSubsystemDebugEnabled and NMCore.isSubsystemDebugEnabled("runtime") then
                    NMCore.logChannel("runtime", tag, detail)
                end
            end
        })
        local mode = NMClientModeReconcile.resolveModeForItem(player, e.item, e.profile, e.state)
        local resolvedOutputForMode = NMDeviceProfiles.resolveOutputMode(e.profile, e.state, mode, false)
        local modeChanged = NMClientModeReconcile.applyResolvedMode(e.uuid, e.state, mode)
        if modeChanged == true
            and mode == "attached"
            and NMDeviceProfiles.isPortableTrackedProfile
            and NMDeviceProfiles.isPortableTrackedProfile(e.profile) == true
            and NMDeviceUI
            and NMDeviceUI.canAutoOpenForAttachedTransition
            and NMDeviceUI.canAutoOpenForAttachedTransition(player:getPlayerNum(), e.item) == true
            and NMDeviceUI.openForItemIfNeeded then
            local openedWindow = NMDeviceUI.openForItemIfNeeded(player:getPlayerNum(), e.item) ~= nil
            logTransitionProbe(
                "portable_ui_auto_open",
                string.format(
                    "uuid=%s mode=%s opened=%s item=%s",
                    tostring(e.uuid),
                    tostring(mode),
                    tostring(openedWindow),
                    tostring(e.item and e.item.getFullType and e.item:getFullType() or "unknown")
                )
            )
        end
        local modeSignature = table.concat({
            tostring(mode),
            tostring(modeChanged == true),
            tostring(e.state and e.state.isOn == true),
            tostring(e.state and e.state.isPlaying == true),
            tostring(e.state and e.state.mediaFullType or "nil")
        }, "|")
        local shouldLogMode = shouldLogModeResolution(e.uuid, modeSignature, modeChanged == true)
        if shouldLogMode then
            local worldItem = e.item and e.item.getWorldItem and e.item:getWorldItem() or nil
            local stateActive = e.state and (e.state.isOn == true or e.state.isPlaying == true)
            local interestingMode = mode == "attached" or mode == "placed" or mode == "drop_pending" or mode == "pickup_pending" or mode == "vehicle"
            if not (interestingMode or stateActive or worldItem ~= nil) then
                shouldLogMode = false
            end
        end
        if shouldLogMode then
            local worldItem = e.item and e.item.getWorldItem and e.item:getWorldItem() or nil
            logTransitionProbe(
                "mode_resolution",
                string.format(
                    "uuid=%s mode=%s changed=%s worldItem=%s isOn=%s isPlaying=%s media=%s",
                    tostring(e.uuid),
                    tostring(mode),
                    tostring(modeChanged == true),
                    tostring(worldItem ~= nil),
                    tostring(e.state and e.state.isOn == true),
                    tostring(e.state and e.state.isPlaying == true),
                    tostring(e.state and e.state.mediaFullType or "nil")
                )
            )
        end

        if NMClientModeSync and NMClientModeSync.emit then
            NMClientModeSync.emit(player, e.item, e.profile, e.state, mode)
        end

        valid[e.uuid] = true
        inventoryOwners[e.uuid] = true
        currentInventoryByUuid[e.uuid] = e.item

        local stableOffSig = buildStableOffInventorySignature(e.item, e.profile, e.state, mode, resolvedOutputForMode)
        if stableOffSig ~= nil and tostring(NMClientPlaybackTick.stableOffInventorySigSeen[e.uuid] or "") == stableOffSig then
            stableOffInventorySkipCount = stableOffInventorySkipCount + 1
        else
            if stableOffSig == nil then
                NMClientPlaybackTick.stableOffInventorySigSeen[e.uuid] = nil
            else
                stableOffInventorySyncCount = stableOffInventorySyncCount + 1
            end

            local source = nil
            local trackedPortable = NMDeviceProfiles.isPortableTrackedProfile and NMDeviceProfiles.isPortableTrackedProfile(e.profile)
            if mode == "attached" or mode == "stowed" or mode == "drop_pending" or mode == "pickup_pending" then
                if mode == "pickup_pending"
                    and NMClientPortableDropHandoff
                    and NMClientPortableDropHandoff.buildPickupSource then
                    source = NMClientPortableDropHandoff.buildPickupSource(player, e.uuid)
                end
                if not source then
                    source = {
                        mode = "world",
                        context = mode,
                        x = player.getX and player:getX() or 0,
                        y = player.getY and player:getY() or 0,
                        z = player.getZ and player:getZ() or 0,
                        _nmFastFollowPlayer = true
                    }
                end
                e.state.playbackMode = ((resolvedOutputForMode == "world" or resolvedOutputForMode == "silent") or trackedPortable) and "world" or "inventory"
            elseif mode == "placed" then
                local w = e.item.getWorldItem and e.item:getWorldItem() or nil
                local s = w and w.getSquare and w:getSquare() or nil
                if s then
                    source = {
                        mode = "world",
                        context = "placed",
                        x = s:getX() + 0.5,
                        y = s:getY() + 0.5,
                        z = s:getZ(),
                        _nmFastWorldItem = e.item
                    }
                    e.state.playbackMode = ((resolvedOutputForMode == "world" or resolvedOutputForMode == "silent") or trackedPortable) and "world" or "inventory"
                end
            else
                source = { mode = "inventory", context = "inventory" }
                e.state.playbackMode = "inventory"
            end

            rememberPlaybackLossSource(e.uuid, e.state, source, {
                mode = mode,
                resolvedOutput = resolvedOutputForMode
            })
            emitPlaybackLossProbe(player, e.uuid, true, "inventory_sync", {
                source = source,
                mode = mode,
                playbackMode = e.state.playbackMode,
                resolvedOutput = resolvedOutputForMode,
                isOn = e.state.isOn == true,
                isPlaying = e.state.isPlaying == true,
                media = e.state.mediaFullType
            })

            local syncStartedMs = nowRealMs()
            NMPlaybackRuntime.syncDevice(player, e.profile, e.state, source, tickCount * 60)
            if NMClientVanillaMusicSuppressor and NMClientVanillaMusicSuppressor.observeAudibility then
                NMClientVanillaMusicSuppressor.observeAudibility(player, e.profile, e.state, source)
            end
            observeMemoryDuration("sync_device_ms", math.max(0, nowRealMs() - syncStartedMs))
            inventorySyncCount = inventorySyncCount + 1
            if stableOffSig ~= nil then
                NMClientPlaybackTick.stableOffInventorySigSeen[e.uuid] = stableOffSig
            end
            rememberFastSource(e.uuid, e.profile, e.state, source, player)
            local sourceKind = (source and source.mode == "world") and "world_item" or "inventory"
            consumeAndDispatchTrackFinished(player, e.profile, e.state, nil, e.item, sourceKind, e.uuid)
            if source and source.mode == "world" and source.x and source.y and source.z then
                spPulseCandidates[e.uuid] = {
                    profile = e.profile,
                    state = e.state,
                    source = source
                }
            end
        end
    end

    reconcileDroppedInventoryToPlacedSP(player, currentInventoryByUuid)
    if NMDeviceUI and NMDeviceUI.endSessionStartAutoOpenSuppression then
        NMDeviceUI.endSessionStartAutoOpenSuppression(player:getPlayerNum(), "first_inventory_reconciliation_complete")
    end

    local detachedResult = NMClientDetachedPlaybackPass.run(player, {
        valid = valid,
        inventoryOwners = inventoryOwners,
        currentInventoryByUuid = currentInventoryByUuid,
        spPulseCandidates = spPulseCandidates,
        tickCount = tickCount,
        nowRealMs = nowRealMs,
        observeMemoryDuration = observeMemoryDuration,
        consumeAndDispatchTrackFinished = consumeAndDispatchTrackFinished,
        shouldLogDetachedSync = shouldLogDetachedSync,
        applySPLocalVehiclePowerGuard = applySPLocalVehiclePowerGuard,
        resolveVehicleCanonicalGeneration = resolveVehicleCanonicalGeneration,
        persistVehicleCanonicalGeneration = persistVehicleCanonicalGeneration,
        setVehicleIdentityState = setVehicleIdentityState,
        detachedOrchestration = detachedOrchestration,
        continuity = continuity,
        ownershipConflictState = NMClientPlaybackTick.ownershipConflictState,
        detachedRemoveLogMs = NMClientPlaybackTick.detachedRemoveLogMs,
        rememberFastSource = rememberFastSource,
        rememberPlaybackLossSource = rememberPlaybackLossSource,
        emitPlaybackLossProbe = emitPlaybackLossProbe,
        detachedOut = clearArray(scratch.detached),
        pendingPlaybackOut = clearArray(scratch.pendingPlayback),
        logTransitionProbe = logTransitionProbe,
        logRuntime = function(tag, detail)
            if NMCore and NMCore.logChannel and NMCore.isSubsystemDebugEnabled and NMCore.isSubsystemDebugEnabled("runtime") then
                NMCore.logChannel("runtime", tag, detail)
            end
        end
    })
    NMClientPlaybackTick._playbackDirty = false
    if NMClientPlaybackTick._hasManagedInventoryDevices ~= true
        and not hasActivePlaybackRuntime()
        and not (detachedResult and tonumber(detachedResult.detachedSyncCount) or 0 > 0) then
        NMClientPlaybackTick._startupBootstrapUntilTick = math.min(
            tonumber(NMClientPlaybackTick._startupBootstrapUntilTick) or 0,
            tonumber(NMClientPlaybackTick.schedulerTick) or 0
        )
    end
    local detached = detachedResult.detached or {}
    detachedSyncCount = detachedSyncCount + (tonumber(detachedResult.detachedSyncCount) or 0)

    NMClientSPLocalRuntime.emitZombiePulses(player, spPulseCandidates, {
        zombieAttractionPulseState = NMClientPlaybackTick.zombieAttractionPulseState,
        nowMs = nowRealMs
    })

    if NMClientModeSync and NMClientModeSync.prune then
        NMClientModeSync.prune(valid)
    end
    for ownedUuid, _ in pairs(NMClientPlaybackTick.ownershipConflictState or {}) do
        if not valid[ownedUuid] then
            NMClientPlaybackTick.ownershipConflictState[ownedUuid] = nil
        end
    end
    pruneStableOffInventorySignatures(valid)
    NMClientModeReconcile.pruneAuthority(valid)
    if NMPlaybackRuntime and type(NMPlaybackRuntime.Active) == "table" then
        for activeUuid, _ in pairs(NMPlaybackRuntime.Active) do
            emitPlaybackLossProbe(player, activeUuid, valid[activeUuid] == true, valid[activeUuid] == true and "full_pass_valid" or "full_pass_missing_candidate")
        end
    end
    local stopMissingStartedMs = nowRealMs()
    NMPlaybackRuntime.stopMissing(player, valid, NMClientPlaybackTick.tick)
    local stopMissingElapsedMs = math.max(0, nowRealMs() - stopMissingStartedMs)
    observeMemoryDuration("stop_missing_ms", stopMissingElapsedMs)

    countMemoryEvent("inventory_sync_count", inventorySyncCount)
    countMemoryEvent("detached_sync_count", detachedSyncCount)
    countMemoryEvent("playback_inventory_stable_off_skip", stableOffInventorySkipCount)
    countMemoryEvent("playback_inventory_stable_off_sync", stableOffInventorySyncCount)
    if NMClientVanillaMusicSuppressor and NMClientVanillaMusicSuppressor.endTick then
        NMClientVanillaMusicSuppressor.endTick(NMClientPlaybackTick.tick)
    end
    observeMemoryDuration("tick_ms", math.max(0, nowRealMs() - tickStartedMs))
    if NMPlaybackRuntimeDiagnostics and NMPlaybackRuntimeDiagnostics.sampleMemoryProbe then
        NMPlaybackRuntimeDiagnostics.sampleMemoryProbe(NMPlaybackRuntime, {
            nowMs = nowRealMs(),
            inventoryDevices = #inventory,
            detachedSources = #detached,
            playbackActive = (inventorySyncCount + detachedSyncCount) > 0
        })
    end
end

