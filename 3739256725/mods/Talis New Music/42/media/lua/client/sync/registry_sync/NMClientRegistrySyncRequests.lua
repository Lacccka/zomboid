local NMClientRegistrySyncState = require "sync/registry_sync/NMClientRegistrySyncState"
local NMClientRegistrySyncTiming = require "sync/registry_sync/NMClientRegistrySyncTiming"
local NMClientRegistrySyncLogging = require "sync/registry_sync/NMClientRegistrySyncLogging"

local NMClientRegistrySyncRequests = {}

function NMClientRegistrySyncRequests.sendSyncRequest(player)
    if not player or not sendClientCommand then
        return false
    end
    sendClientCommand(player, NMCore.NetModule, "request_registry_sync", {})
    return true
end

function NMClientRegistrySyncRequests.sendInventorySyncRequest(player)
    if not player or not sendClientCommand then
        return false
    end
    sendClientCommand(player, NMCore.NetModule, "request_inventory_state_sync", {})
    return true
end

function NMClientRegistrySyncRequests.requestNow(state, player, reason)
    if not NMCore.isMPClientRuntime() then
        return false
    end
    local snapshot = NMClientRegistrySyncState.snapshot(state)
    local timing = NMClientRegistrySyncTiming.resolveRequestWindow()
    if not NMClientRegistrySyncTiming.isCooldownReady(snapshot.resyncLastRequestMs, timing.nowMs, timing.cooldownMs) then
        return false
    end
    if not NMClientRegistrySyncRequests.sendSyncRequest(player) then
        return false
    end
    NMClientRegistrySyncState.markResyncRequestNormalized(snapshot.state, snapshot.tick, timing.nowMs)
    NMClientRegistrySyncLogging.emitRequestNow(reason, timing.nowMs)
    return true
end

return NMClientRegistrySyncRequests
