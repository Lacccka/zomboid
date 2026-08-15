require "runtime/NMClientDetachedPlaybackPass"
require "runtime/NMClientOwnershipConflictPolicy"
require "runtime/NMClientPlaybackInventoryCollector"

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
NMClientPlaybackTick.corpseInventoryReboundSeen = NMClientPlaybackTick.corpseInventoryReboundSeen or {}

local function logTransitionProbe(msg, detail)
    if NMCore and NMCore.logChannel and NMCore.isSubsystemDebugEnabled and NMCore.isSubsystemDebugEnabled("playback_transition") then
        NMCore.logChannel("playback_transition", msg, detail)
    end
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
    if previousSig ~= currentSig or (nowMs - previousMs) >= 20000 then
        NMClientPlaybackTick.modeResolutionSigSeen[key] = currentSig
        NMClientPlaybackTick.modeResolutionHeartbeatMs[key] = nowMs
        return true
    end
    return false
end

local function shouldLogDetachedSync(uuid, state, src)
    local key = tostring(uuid or "")
    if key == "" then
        return false
    end
    local sig = table.concat({
        tostring(src and src.context or "nil"),
        tostring(state and state.isOn == true),
        tostring(state and state.isPlaying == true),
        tostring(state and state.mediaFullType or "nil"),
        tostring(math.floor((tonumber(src and src.x) or 0) * 10 + 0.5) / 10),
        tostring(math.floor((tonumber(src and src.y) or 0) * 10 + 0.5) / 10),
        tostring(math.floor((tonumber(src and src.z) or 0) * 10 + 0.5) / 10)
    }, "|")
    local nowMs = nowRealMs()
    local lastSig = tostring(NMClientPlaybackTick.detachedSyncSigSeen[key] or "")
    local lastMs = tonumber(NMClientPlaybackTick.detachedSyncHeartbeatMs[key]) or 0
    if lastSig ~= sig or (nowMs - lastMs) >= 60000 then
        NMClientPlaybackTick.detachedSyncSigSeen[key] = sig
        NMClientPlaybackTick.detachedSyncHeartbeatMs[key] = nowMs
        return true
    end
    return false
end

local function _fallbackSetVehicleIdentityState(entry, liveEntry, _, nextState, _)
    local target = tostring(nextState or "")
    if target == "" then
        return
    end
    if entry then
        entry._vehicleIdentityState = target
    end
    if liveEntry and liveEntry ~= entry then
        liveEntry._vehicleIdentityState = target
    end
end

local function _fallbackResolveVehicleCanonicalGeneration(state, entry, liveEntry, _)
    local stateGen = tonumber(state and state.sourceGeneration) or 0
    local entryGen = math.max(
        tonumber(entry and entry.sourceGeneration) or 0,
        tonumber(entry and entry.sourceEpoch) or 0,
        tonumber(entry and entry.stateSnapshot and entry.stateSnapshot.sourceGeneration) or 0
    )
    local liveGen = math.max(
        tonumber(liveEntry and liveEntry.sourceGeneration) or 0,
        tonumber(liveEntry and liveEntry.sourceEpoch) or 0,
        tonumber(liveEntry and liveEntry.stateSnapshot and liveEntry.stateSnapshot.sourceGeneration) or 0
    )
    local chosen = math.max(stateGen, entryGen, liveGen)
    return chosen
end

local function _fallbackPersistVehicleCanonicalGeneration(entry, liveEntry, state, canonicalGen)
    local gen = tonumber(canonicalGen) or 0
    local function apply(target)
        if not target then
            return
        end
        target.sourceGeneration = math.max(tonumber(target.sourceGeneration) or 0, gen)
        target.sourceEpoch = math.max(tonumber(target.sourceEpoch) or 0, gen)
        if target.stateSnapshot then
            target.stateSnapshot.sourceGeneration = math.max(tonumber(target.stateSnapshot.sourceGeneration) or 0, gen)
        end
    end
    apply(entry)
    apply(liveEntry)
    if state then
        state.sourceGeneration = math.max(tonumber(state.sourceGeneration) or 0, gen)
    end
end

local continuity = type(NMClientVehicleContinuity) == "table" and NMClientVehicleContinuity or {}
local detachedOrchestration = type(NMClientDetachedOrchestration) == "table" and NMClientDetachedOrchestration or {}

local setVehicleIdentityState = continuity.setVehicleIdentityState or _fallbackSetVehicleIdentityState
local resolveVehicleCanonicalGeneration = continuity.resolveVehicleCanonicalGeneration or _fallbackResolveVehicleCanonicalGeneration
local persistVehicleCanonicalGeneration = continuity.persistVehicleCanonicalGeneration or _fallbackPersistVehicleCanonicalGeneration

if NMCore and NMCore.logChannel and NMCore.isSubsystemDebugEnabled and NMCore.isSubsystemDebugEnabled("runtime") then
    if not continuity.setVehicleIdentityState then
        NMCore.logChannel("runtime", "module_fallback_active", "module=NMClientVehicleContinuity")
    end
    if not detachedOrchestration.buildConflictStateKey then
        NMCore.logChannel("runtime", "module_fallback_active", "module=NMClientDetachedOrchestration")
    end
end
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
    NMClientPlaybackTick.lastInventoryByUuid = NMClientSPDropReconcile.reconcile({
        player = player,
        currentInventoryByUuid = currentInventoryByUuid,
        previousInventoryByUuid = NMClientPlaybackTick.lastInventoryByUuid,
        logTransitionProbe = logTransitionProbe
    }) or (currentInventoryByUuid or {})
end

function NMClientPlaybackTick.onTick(player)
    if not player or not player.getInventory then
        return
    end

    if NMClientVanillaMusicSuppressor and NMClientVanillaMusicSuppressor.beginTick then
        NMClientVanillaMusicSuppressor.beginTick()
    end

    local tickStartedMs = nowRealMs()
    NMClientPlaybackTick.tick = (tonumber(NMClientPlaybackTick.tick) or 0) + 1
    local tickCount = getGameTime and getGameTime():getWorldAgeHours() or 0
    local valid = {}
    local inventoryOwners = {}
    local currentInventoryByUuid = {}
    local spPulseCandidates = {}

    local inventory = {}
    local inventoryCollectStartedMs = nowRealMs()
    NMClientPlaybackInventoryCollector.collectManaged(player, inventory)
    local inventoryCollectElapsedMs = math.max(0, nowRealMs() - inventoryCollectStartedMs)
    observeMemoryDuration("inventory_collect_ms", inventoryCollectElapsedMs)
    local inventorySyncCount = 0
    local detachedSyncCount = 0

    for i = 1, #inventory do
        local e = inventory[i]
        NMClientPlaybackInventoryCollector.normalizeCorpseRecoveredInventoryState(e.profile, e.state, e.uuid, {
            corpseInventoryReboundSeen = NMClientPlaybackTick.corpseInventoryReboundSeen,
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
            local interestingMode = mode == "attached" or mode == "placed" or mode == "drop_pending" or mode == "vehicle"
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

        local source = nil
        local trackedPortable = NMDeviceProfiles.isPortableTrackedProfile and NMDeviceProfiles.isPortableTrackedProfile(e.profile)
        if mode == "attached" or mode == "stowed" or mode == "drop_pending" then
            source = {
                mode = "world",
                context = mode,
                x = player.getX and player:getX() or 0,
                y = player.getY and player:getY() or 0,
                z = player.getZ and player:getZ() or 0
            }
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
                    z = s:getZ()
                }
                e.state.playbackMode = ((resolvedOutputForMode == "world" or resolvedOutputForMode == "silent") or trackedPortable) and "world" or "inventory"
            end
        else
            source = { mode = "inventory", context = "inventory" }
            e.state.playbackMode = "inventory"
        end

        local syncStartedMs = nowRealMs()
        NMPlaybackRuntime.syncDevice(player, e.profile, e.state, source, tickCount * 60)
        if NMClientVanillaMusicSuppressor and NMClientVanillaMusicSuppressor.observeAudibility then
            NMClientVanillaMusicSuppressor.observeAudibility(player, e.profile, e.state, source)
        end
        observeMemoryDuration("sync_device_ms", math.max(0, nowRealMs() - syncStartedMs))
        inventorySyncCount = inventorySyncCount + 1
        local sourceKind = (source and source.mode == "world") and "world_item" or "inventory"
        consumeAndDispatchTrackFinished(player, e.profile, e.state, nil, e.item, sourceKind, e.uuid)
        valid[e.uuid] = true
        inventoryOwners[e.uuid] = true
        currentInventoryByUuid[e.uuid] = e.item
        if source and source.mode == "world" and source.x and source.y and source.z then
            spPulseCandidates[e.uuid] = {
                profile = e.profile,
                state = e.state,
                source = source
            }
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
        logTransitionProbe = logTransitionProbe,
        logRuntime = function(tag, detail)
            if NMCore and NMCore.logChannel and NMCore.isSubsystemDebugEnabled and NMCore.isSubsystemDebugEnabled("runtime") then
                NMCore.logChannel("runtime", tag, detail)
            end
        end
    })
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
    NMClientModeReconcile.pruneAuthority(valid)
    local stopMissingStartedMs = nowRealMs()
    NMPlaybackRuntime.stopMissing(player, valid, NMClientPlaybackTick.tick)
    local stopMissingElapsedMs = math.max(0, nowRealMs() - stopMissingStartedMs)
    observeMemoryDuration("stop_missing_ms", stopMissingElapsedMs)

    countMemoryEvent("inventory_sync_count", inventorySyncCount)
    countMemoryEvent("detached_sync_count", detachedSyncCount)
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

