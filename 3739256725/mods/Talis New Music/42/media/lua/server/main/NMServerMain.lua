require "zombies/NMZombieLiveStrategy"
require "zombies/NMZombieAttachedDefinitions"
require "zombies/NMZombieAudioVisualSupport"
require "zombies/NMZombieVisualTargetContract"
require "zombies/NMZombieVisualTargetLedger"
require "zombies/NMZombieAttraction"
require "death/NMServerZombieCorpseCarry"
require "NMServerSandboxLootController"
require "zombies/NMServerMPZombieAssignmentFlow"
require "zombies/NMServerZombieVisualTargetPublisher"
require "zombies/NMServerSPZombieAssignmentFlow"

-- Thin server bootstrap that wires engine events to modular handlers.
NMDevicesServer = NMDevicesServer or {}

local function canRunAuthoritativeWorldMutation()
    if NMCore and NMCore.isMPClientRuntime and NMCore.isMPClientRuntime() == true then
        return false
    end
    if NMAuthorityContract and NMAuthorityContract.canMutateDurableStateAtRuntime then
        return NMAuthorityContract.canMutateDurableStateAtRuntime() == true
    end
    return true
end

local function logServerDebugBootstrap(stage)
    if not (NMCore and NMCore.isSubsystemDebugEnabled and NMCore.isSubsystemDebugEnabled("core")) then
        return
    end
    local subsystems = NMRuntimeConfig and NMRuntimeConfig.getSubsystemDebugSnapshot and NMRuntimeConfig.getSubsystemDebugSnapshot() or {}
    local subsystemSummary = NMRuntimeConfig and NMRuntimeConfig.formatSubsystemDebugSummary and NMRuntimeConfig.formatSubsystemDebugSummary(subsystems) or ""
    print(string.format(
        "[NewMusic] [DebugBootstrap] side=server stage=%s subsystems=%s",
        tostring(stage or "unknown"),
        tostring(subsystemSummary)
    ))
end

local function isMultiplayerRuntime()
    return NMCore and NMCore.isMultiplayerMode and NMCore.isMultiplayerMode() == true
end

local function getActiveZombieExecutor()
    local strategy = NMZombieLiveStrategy and NMZombieLiveStrategy.getLiveVisualStrategy and NMZombieLiveStrategy.getLiveVisualStrategy() or ""
    if strategy == "sp_runtime_attach" then
        return NMServerSPZombieAssignmentFlow
    end
    if strategy == "mp_assignment_flow" then
        return NMServerMPZombieAssignmentFlow
    end
    return nil
end

local function shouldRunTargetPublisher()
    return canRunAuthoritativeWorldMutation()
        and isMultiplayerRuntime()
        and NMServerZombieVisualTargetPublisher
        and NMServerZombieVisualTargetPublisher.onTick
end

local function onClientCommand(module, command, player, args)
    if NMServerIntentRouter and NMServerIntentRouter.onClientCommand then
        NMServerIntentRouter.onClientCommand(module, command, player, args)
    end
    if type(NMDevicesServer) == "table" and NMDevicesServer.onClientCommand then
        NMDevicesServer.onClientCommand(module, command, player, args)
    end
end

local function onTick()
    if NMServerVehicleTrackSchedulerTick and NMServerVehicleTrackSchedulerTick.onTick then
        NMServerVehicleTrackSchedulerTick.onTick()
    end
    if NMServerSourceRefreshTick and NMServerSourceRefreshTick.onTick then
        NMServerSourceRefreshTick.onTick()
    end
    if NMServerModeReconcile and NMServerModeReconcile.onTick then
        NMServerModeReconcile.onTick()
    end
    if NMServerZombiePulseTick and NMServerZombiePulseTick.onTick then
        NMServerZombiePulseTick.onTick()
    end
    if NMServerZombieCorpseCarry and NMServerZombieCorpseCarry.onTick and canRunAuthoritativeWorldMutation() then
        NMServerZombieCorpseCarry.onTick()
    end
    local activeZombieExecutor = getActiveZombieExecutor()
    if activeZombieExecutor and activeZombieExecutor.onTick and canRunAuthoritativeWorldMutation() then
        activeZombieExecutor.onTick()
    end
    if shouldRunTargetPublisher() then
        NMServerZombieVisualTargetPublisher.onTick()
    end
    if NMServerRegistryTick and NMServerRegistryTick.onTick then
        NMServerRegistryTick.onTick()
    end
    if type(NMDevicesServer) == "table" and NMDevicesServer.onTick then
        NMDevicesServer.onTick()
    end
end

local function onEveryOneMinute()
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

local function shouldLogProofVerbose()
    return NMCore and NMCore.isSubsystemDebugEnabled and NMCore.isSubsystemDebugEnabled("zombie_assignment") == true
end

local function shouldLogCorpseVerbose()
    return NMCore and NMCore.isSubsystemDebugEnabled and NMCore.isSubsystemDebugEnabled("zombie_corpse") == true
end

if NMCore and NMCore.logChannel then
    if NMCore.logBuildVersionLine then
        NMCore.logBuildVersionLine("server")
    end
    logServerDebugBootstrap("bootstrap")
    NMCore.logChannel(
        "zombie_assignment",
        "server_boot",
        string.format(
            "authority=%s canMutate=%s liveStrategy=%s",
            tostring(NMCore.getRuntimeAuthorityMode and NMCore.getRuntimeAuthorityMode() or "unknown"),
            tostring(canRunAuthoritativeWorldMutation()),
            tostring(NMZombieLiveStrategy and NMZombieLiveStrategy.getLiveVisualStrategy and NMZombieLiveStrategy.getLiveVisualStrategy() or "unknown")
        )
    )
    if NMServerBootReset and NMServerBootReset.initSession then
        NMServerBootReset.initSession()
    end
    if NMServerSandboxLootController and NMServerSandboxLootController.registerEventHooks then
        NMServerSandboxLootController.registerEventHooks()
    end
    if NMServerSandboxLootController and NMServerSandboxLootController.ensureInitialized then
        NMServerSandboxLootController.ensureInitialized()
    end
end

if Events then
    if Events.OnClientCommand and Events.OnClientCommand.Add then
        Events.OnClientCommand.Add(onClientCommand)
    end
    if Events.OnTick and Events.OnTick.Add then
        Events.OnTick.Add(onTick)
    end
    if Events.EveryOneMinute and Events.EveryOneMinute.Add then
        Events.EveryOneMinute.Add(onEveryOneMinute)
    end
    if Events.OnZombieDead and Events.OnZombieDead.Add and canRunAuthoritativeWorldMutation() then
        if NMServerZombieCorpseCarry and NMServerZombieCorpseCarry.onZombieDead then
            Events.OnZombieDead.Add(NMServerZombieCorpseCarry.onZombieDead)
            if NMCore and NMCore.logChannel and shouldLogCorpseVerbose() then
                NMCore.logChannel("zombie_corpse", "hook_on_zombie_dead_registered", "handler=NMServerZombieCorpseCarry.onZombieDead")
            end
        end
    end
    if Events.OnDeadBodySpawn and Events.OnDeadBodySpawn.Add and canRunAuthoritativeWorldMutation() then
        if NMServerZombieCorpseCarry and NMServerZombieCorpseCarry.onDeadBodySpawn then
            Events.OnDeadBodySpawn.Add(NMServerZombieCorpseCarry.onDeadBodySpawn)
            if NMCore and NMCore.logChannel and shouldLogCorpseVerbose() then
                NMCore.logChannel("zombie_corpse", "hook_on_dead_body_spawn_registered", "handler=NMServerZombieCorpseCarry.onDeadBodySpawn")
            end
        end
    end
    if Events.OnZombieUpdate and Events.OnZombieUpdate.Add and canRunAuthoritativeWorldMutation() then
        local registeredAny = false
        local activeZombieExecutor = getActiveZombieExecutor()
        if activeZombieExecutor and activeZombieExecutor.onZombieUpdate then
            Events.OnZombieUpdate.Add(activeZombieExecutor.onZombieUpdate)
            registeredAny = true
        end
        if registeredAny then
            if NMCore and NMCore.logChannel then
                NMCore.logChannel(
                    "zombie_assignment",
                    "hook_registered",
                    "event=OnZombieUpdate strategy=" .. tostring(NMZombieLiveStrategy and NMZombieLiveStrategy.getLiveVisualStrategy and NMZombieLiveStrategy.getLiveVisualStrategy() or "unknown")
                )
            end
        elseif NMCore and NMCore.logChannel and shouldLogProofVerbose() then
            NMCore.logChannel("zombie_assignment", "hook_missing", "event=OnZombieUpdate")
        end
    elseif canRunAuthoritativeWorldMutation() then
        if NMCore and NMCore.logChannel and shouldLogProofVerbose() then
            NMCore.logChannel("zombie_assignment", "hook_missing", "event=OnZombieUpdate")
        end
    end
end



