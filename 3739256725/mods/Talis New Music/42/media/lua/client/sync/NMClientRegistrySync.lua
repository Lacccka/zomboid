local NMClientRegistrySyncState = require "sync/registry_sync/NMClientRegistrySyncState"
local NMClientRegistrySyncRequests = require "sync/registry_sync/NMClientRegistrySyncRequests"
local NMClientRegistrySyncLifecycle = require "sync/registry_sync/NMClientRegistrySyncLifecycle"
local NMClientRegistrySyncScheduler = require "sync/registry_sync/NMClientRegistrySyncScheduler"

-- Client-side registry snapshot sync cadence, retries, and ACK handling.
NMClientRegistrySync = NMClientRegistrySync or {}
NMClientRegistrySync.state = NMClientRegistrySyncState.ensure(NMClientRegistrySync.state)

function NMClientRegistrySync.requestInitialSync()
    NMClientRegistrySyncState.resetInitialSync(NMClientRegistrySync.state, NMCore.isMPClientRuntime())
end

function NMClientRegistrySync.requestNow(player, reason)
    return NMClientRegistrySyncRequests.requestNow(NMClientRegistrySync.state, player, reason)
end

function NMClientRegistrySync.onServerCommand(command, args)
    NMClientRegistrySyncLifecycle.onServerCommand(NMClientRegistrySync.state, command)
end

function NMClientRegistrySync.onTick(player)
    NMClientRegistrySyncScheduler.onTick(NMClientRegistrySync.state, player)
end

