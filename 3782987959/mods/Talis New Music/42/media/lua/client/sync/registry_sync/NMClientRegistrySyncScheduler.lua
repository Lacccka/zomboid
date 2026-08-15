local NMClientRegistrySyncState = require "sync/registry_sync/NMClientRegistrySyncState"
local NMClientRegistrySyncTiming = require "sync/registry_sync/NMClientRegistrySyncTiming"
local NMClientRegistrySyncRequests = require "sync/registry_sync/NMClientRegistrySyncRequests"
local NMClientRegistrySyncLifecycle = require "sync/registry_sync/NMClientRegistrySyncLifecycle"
local NMClientRegistrySyncMovement = require "sync/registry_sync/NMClientRegistrySyncMovement"

local NMClientRegistrySyncScheduler = {}

local function runInitialSync(snapshot, player, timing)
    local state = snapshot.state
    if state.syncPending ~= true then
        return false
    end
    if state.initialSyncInFlight == true then
        if not NMClientRegistrySyncLifecycle.resolveInitialSyncTimeout(state, snapshot.tick, timing.timeoutTicks) then
            return true
        end
    end
    if snapshot.syncAttempts >= 5 then
        state.syncPending = false
        return false
    end
    if not NMClientRegistrySyncTiming.isDueNow(snapshot.tick, snapshot.syncNextTick) then
        return false
    end
    if NMClientRegistrySyncTiming.isCooldownReady(snapshot.resyncLastRequestMs, timing.nowMs, timing.cooldownMs) then
        if NMClientRegistrySyncRequests.sendSyncRequest(player) then
            NMClientRegistrySyncState.markInitialSyncRequestNormalized(
                state,
                snapshot.tick,
                timing.nowMs,
                snapshot.syncAttempts + 1
            )
        end
    else
        state.syncNextTick = snapshot.tick + 30
    end
    return false
end

local function runInventorySync(snapshot, player, timing)
    local state = snapshot.state
    if state.inventorySyncPending ~= true or state.inventorySyncSent == true then
        return
    end
    if not NMClientRegistrySyncTiming.isCooldownReady(snapshot.resyncLastRequestMs, timing.nowMs, timing.cooldownMs) then
        return
    end
    if NMClientRegistrySyncRequests.sendInventorySyncRequest(player) then
        NMClientRegistrySyncState.markInventorySyncSentNormalized(state, timing.nowMs)
    end
end

local function runMovementResync(snapshot, player, timing)
    local state = snapshot.state
    local sample = NMClientRegistrySyncMovement.samplePlayerPosition(player)
    if not sample then
        return
    end
    local moved = NMClientRegistrySyncMovement.hasMoved(snapshot, sample, timing.moveDist2)
    local dueNow = NMClientRegistrySyncTiming.isDueNow(snapshot.tick, snapshot.resyncNextTick)
    local cooldownReady = NMClientRegistrySyncTiming.isCooldownReady(snapshot.resyncLastRequestMs, timing.nowMs, timing.cooldownMs)
    if moved and state.resyncInFlight ~= true and dueNow and cooldownReady then
        if NMClientRegistrySyncRequests.sendSyncRequest(player) then
            NMClientRegistrySyncState.markResyncRequestNormalized(state, snapshot.tick, timing.nowMs)
            NMClientRegistrySyncState.setResyncNextTickNormalized(state, snapshot.tick + timing.intervalTicks)
            NMClientRegistrySyncMovement.updatePositionCache(state, sample)
        end
    end
end

function NMClientRegistrySyncScheduler.onTick(state, player)
    if not NMCore.isMPClientRuntime() then
        return
    end
    local snapshot = NMClientRegistrySyncState.advanceTickSnapshot(state)
    local timing = NMClientRegistrySyncTiming.resolveTickWindow()
    local initialSyncBlocked = runInitialSync(snapshot, player, timing)
    if initialSyncBlocked == true then
        return
    end
    runInventorySync(snapshot, player, timing)
    NMClientRegistrySyncLifecycle.resolveResyncTimeout(snapshot.state, snapshot.tick, timing.timeoutTicks)
    runMovementResync(snapshot, player, timing)
end

return NMClientRegistrySyncScheduler
