local NMClientRegistrySyncState = {}

function NMClientRegistrySyncState.defaults()
    return {
        syncPending = false,
        syncAttempts = 0,
        syncNextTick = 0,
        inventorySyncPending = false,
        inventorySyncSent = false,
        initialSyncInFlight = false,
        initialSyncRequestTick = 0,
        resyncInFlight = false,
        resyncRequestTick = 0,
        resyncNextTick = 0,
        resyncLastRequestMs = 0,
        lastX = nil,
        lastY = nil,
        lastZ = nil,
        tick = 0
    }
end

function NMClientRegistrySyncState.ensure(state)
    local target = type(state) == "table" and state or {}
    local defaults = NMClientRegistrySyncState.defaults()
    for key, value in pairs(defaults) do
        if target[key] == nil then
            target[key] = value
        end
    end
    return target
end

function NMClientRegistrySyncState.resetInitialSync(state, pending)
    local target = NMClientRegistrySyncState.ensure(state)
    local enabled = pending == true
    target.syncPending = enabled
    target.syncAttempts = 0
    target.syncNextTick = 0
    target.inventorySyncPending = enabled
    target.inventorySyncSent = false
    target.initialSyncInFlight = false
    target.initialSyncRequestTick = 0
    return target
end

function NMClientRegistrySyncState.clearInitialSyncFlight(state)
    local target = NMClientRegistrySyncState.ensure(state)
    target.initialSyncInFlight = false
    target.initialSyncRequestTick = 0
    return target
end

function NMClientRegistrySyncState.clearResyncFlight(state)
    local target = NMClientRegistrySyncState.ensure(state)
    target.resyncInFlight = false
    target.resyncRequestTick = 0
    return target
end

function NMClientRegistrySyncState.clearAllFlights(state)
    local target = NMClientRegistrySyncState.clearInitialSyncFlight(state)
    NMClientRegistrySyncState.clearResyncFlight(target)
    return target
end

function NMClientRegistrySyncState.snapshot(state)
    local target = NMClientRegistrySyncState.ensure(state)
    return {
        state = target,
        tick = tonumber(target.tick) or 0,
        syncAttempts = tonumber(target.syncAttempts) or 0,
        syncNextTick = tonumber(target.syncNextTick) or 0,
        resyncNextTick = tonumber(target.resyncNextTick) or 0,
        resyncLastRequestMs = tonumber(target.resyncLastRequestMs) or 0,
        lastX = target.lastX ~= nil and (tonumber(target.lastX) or 0) or nil,
        lastY = target.lastY ~= nil and (tonumber(target.lastY) or 0) or nil,
        lastZ = target.lastZ ~= nil and (tonumber(target.lastZ) or 0) or nil
    }
end

function NMClientRegistrySyncState.advanceTickSnapshot(state)
    local target = NMClientRegistrySyncState.ensure(state)
    target.tick = (tonumber(target.tick) or 0) + 1
    local snapshot = NMClientRegistrySyncState.snapshot(target)
    return snapshot
end

function NMClientRegistrySyncState.markResyncRequestNormalized(state, tick, nowMs)
    local target = NMClientRegistrySyncState.ensure(state)
    target.resyncInFlight = true
    target.resyncRequestTick = tick
    target.resyncLastRequestMs = nowMs
    return target
end

function NMClientRegistrySyncState.markInitialSyncRequestNormalized(state, tick, nowMs, syncAttempts)
    local target = NMClientRegistrySyncState.ensure(state)
    target.syncAttempts = syncAttempts
    target.syncNextTick = tick + 120
    target.initialSyncInFlight = true
    target.initialSyncRequestTick = tick
    target.resyncLastRequestMs = nowMs
    return target
end

function NMClientRegistrySyncState.markInventorySyncSentNormalized(state, nowMs)
    local target = NMClientRegistrySyncState.ensure(state)
    target.inventorySyncSent = true
    target.inventorySyncPending = false
    target.resyncLastRequestMs = nowMs
    return target
end

function NMClientRegistrySyncState.setResyncNextTickNormalized(state, nextTick)
    local target = NMClientRegistrySyncState.ensure(state)
    target.resyncNextTick = nextTick
    return target
end

return NMClientRegistrySyncState
