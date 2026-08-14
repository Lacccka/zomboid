require "runtime/NMClientInventoryItemVisualSanitizer"
require "runtime/NMClientModOptions"
require "runtime/NMClientPortableUiDragBlockProbe"
require "runtime/NMClientVehicleLootRefresh"
require "runtime/NMClientVanillaMusicSuppressor"
require "runtime/NMClientWorldItemVisualSanitizer"
require "runtime/NMClientZombieVisualTargetCache"
require "runtime/NMClientZombieVisualProbe"
require "ui/shared/host/NMGamepadWindowTracker"
require "ui/boombox/NMBoomboxWindowBootstrap"
require "ui/cdplayer/NMCDPlayerWindowBootstrap"
require "ui/NMGamepadRadial"
require "ui/walkman/NMWalkmanWindowBootstrap"
require "zombies/NMZombieVisualTargetContract"
require "zombies/NMZombieAttachedDefinitions"
require "zombies/NMZombieLiveStrategy"

-- Client bootstrap that wires engine events to modular handlers.
NMDevicesClient = NMDevicesClient or {}
NMDevicesClient._uiLagProbeAutoEnable = false
NMDevicesClient._uiAutoCloseProbeAutoEnable = false
local radialHookRetryTick = 0

local function onFillInventoryObjectContextMenu(playerNum, context, items)
    NMContextMenus.onFillInventoryObjectContextMenu(playerNum, context, items)
end

local function onFillWorldObjectContextMenu(playerNum, context, worldObjects, test)
    NMContextMenus.onFillWorldObjectContextMenu(playerNum, context, worldObjects, test)
end

local function onTick()
    local player = getPlayer and getPlayer() or nil
    radialHookRetryTick = (tonumber(radialHookRetryTick) or 0) + 1
    if NMVehicleRadial and NMVehicleRadial.hookInstalled ~= true and not NMVehicleRadial.baseShowRadialMenu then
        if (radialHookRetryTick % 120) == 1 then
            NMVehicleRadial.installHook()
        end
    end
    if NMGamepadRadial and NMGamepadRadial.hookInstalled ~= true and not NMGamepadRadial.baseOnDisplayDown then
        if (radialHookRetryTick % 120) == 1 then
            NMGamepadRadial.installHook()
        end
    end
    NMClientRegistrySync.onTick(player)
    if NMClientPlaybackTick and NMClientPlaybackTick.onTick then
        NMClientPlaybackTick.onTick(player)
    elseif NMCore and NMCore.logChannel then
        NMCore.logChannel("core", "client_playback_tick_missing", "NMClientPlaybackTick.onTick=nil")
    end
    if NMWalkmanWindow and NMWalkmanWindow.tickPersistedRestore then
        NMWalkmanWindow.tickPersistedRestore()
    end
    if NMCDPlayerWindow and NMCDPlayerWindow.tickPersistedRestore then
        NMCDPlayerWindow.tickPersistedRestore()
    end
    if NMBoomboxWindow and NMBoomboxWindow.tickPersistedRestore then
        NMBoomboxWindow.tickPersistedRestore()
    end
    if NMClientWorldItemVisualSanitizer and NMClientWorldItemVisualSanitizer.onTick then
        NMClientWorldItemVisualSanitizer.onTick(player)
    end
    if NMClientInventoryItemVisualSanitizer and NMClientInventoryItemVisualSanitizer.onTick then
        NMClientInventoryItemVisualSanitizer.onTick(player)
    end
    if NMClientZombieVisualTargetCache and NMClientZombieVisualTargetCache.onTick then
        NMClientZombieVisualTargetCache.onTick()
    end
    if NMClientZombieVisualProbe and NMClientZombieVisualProbe.onTick then
        NMClientZombieVisualProbe.onTick(player)
    end
    if NMClientPortableUiDragBlockProbe and NMClientPortableUiDragBlockProbe.onTick then
        NMClientPortableUiDragBlockProbe.onTick()
    end
    if NMDevicesClient.onTick then
        NMDevicesClient.onTick()
    end
end

local function onServerCommand(module, command, args)
    if module == NMCore.NetModule then
        if NMClientZombieVisualTargetCache and NMClientZombieVisualTargetCache.onServerCommand and NMClientZombieVisualTargetCache.onServerCommand(command, args) == true then
            return
        end
        if NMClientSessionProjection and NMClientSessionProjection.observeServerSessionToken then
            NMClientSessionProjection.observeServerSessionToken(args and args.serverSessionToken, command)
        end
        NMClientRegistrySync.onServerCommand(command, args)
        if command == "state" then
            local player = getPlayer and getPlayer() or nil
            NMClientStateSync.onServerState(player, args)
        end
        if command == "vehicle_loot_stale_reject" then
            NMClientStateSync.onVehicleLootStaleReject(args or {})
        end
        if command == "debug_sync" then
            local enabled = args and args.enabled == true
            local subsystem = tostring(args and args.subsystem or "")
            NMCore.setSubsystemDebugEnabled(subsystem, enabled)
            if NMCore and NMCore.logChannel then
                NMCore.logChannel("core", "debug_sync_applied", "enabled=" .. tostring(enabled) .. " subsystem=" .. tostring(subsystem))
                NMCore.logChannel(
                    "core",
                    "client_debug_sync",
                    string.format(
                        "enabled=%s subsystem=%s authority=%s",
                        tostring(enabled),
                        tostring(subsystem),
                        tostring(NMCore.getRuntimeAuthorityMode and NMCore.getRuntimeAuthorityMode() or "unknown")
                    )
                )
                if NMCore.dumpDebugState then
                    NMCore.dumpDebugState()
                end
            end
        end
        if command == "registry_update" and args and args.op and args.payload then
            NMClientWorldSourceCache.onRegistryUpdate(args.op, args.payload, args.serverSessionToken)
        end
        if command == "registry_snapshot_chunk" and args then
            NMClientWorldSourceCache.onRegistrySnapshotChunk(args)
        end
        if command == "media_flip_result" then
            local player = getPlayer and getPlayer() or nil
            if NMContextMenus and NMContextMenus.onMediaFlipResult then
                NMContextMenus.onMediaFlipResult(player, args or {})
            end
        end
    end
    if NMDevicesClient.onServerCommand then
        NMDevicesClient.onServerCommand(module, command, args)
    end
end

local function logClientDebugBootstrap(stage)
    if not (NMCore and NMCore.isSubsystemDebugEnabled and NMCore.isSubsystemDebugEnabled("core")) then
        return
    end
    local subsystems = NMRuntimeConfig and NMRuntimeConfig.getSubsystemDebugSnapshot and NMRuntimeConfig.getSubsystemDebugSnapshot() or {}
    local subsystemSummary = NMRuntimeConfig and NMRuntimeConfig.formatSubsystemDebugSummary and NMRuntimeConfig.formatSubsystemDebugSummary(subsystems) or ""
    print(string.format(
        "[NewMusic] [DebugBootstrap] side=client stage=%s subsystems=%s",
        tostring(stage or "unknown"),
        tostring(subsystemSummary)
    ))
end

local function sendDebugSet(subsystem, enabled)
    if sendClientCommand and NMCore and NMCore.NetModule then
        sendClientCommand(NMCore.NetModule, "debug_set", { subsystem = tostring(subsystem or ""), enabled = enabled == true })
    end
end

local function setLocalDebug(subsystem, enabled)
    if NMCore and NMCore.setSubsystemDebugEnabled then
        NMCore.setSubsystemDebugEnabled(tostring(subsystem or ""), enabled == true)
    end
end

-- Console helpers for MP UI lag forensics.
function NMDevicesClient.enableMemoryProbe()
    setLocalDebug("core", true)
    setLocalDebug("memory", true)
    sendDebugSet("core", true)
    sendDebugSet("memory", true)
end

function NMDevicesClient.disableMemoryProbe()
    setLocalDebug("memory", false)
    sendDebugSet("memory", false)
end

function NMDevicesClient.dumpMemoryProbeSnapshot()
    local snapshot = NMPlaybackRuntime and NMPlaybackRuntime.snapshot and NMPlaybackRuntime.snapshot() or nil
    local memorySnapshot = snapshot and snapshot.memoryProbe or nil
    local formatter = NMPlaybackRuntimeDiagnostics and NMPlaybackRuntimeDiagnostics.formatMemoryProbeSnapshot or nil
    local line = formatter and formatter(memorySnapshot) or "memory_probe_snapshot unavailable=true"
    print("[NewMusic] [MemoryProbe] dump " .. tostring(line))
    return snapshot
end

function NMDevicesClient.enableUiLagProbe()
    NMDevicesClient._uiLagProbeAutoEnable = true
    setLocalDebug("core", true)
    setLocalDebug("runtime", true)
    setLocalDebug("ui_render", true)
    sendDebugSet("core", true)
    sendDebugSet("runtime", true)
    sendDebugSet("ui_render", true)
end

function NMDevicesClient.disableUiLagProbe()
    NMDevicesClient._uiLagProbeAutoEnable = false
    setLocalDebug("ui_render", false)
    sendDebugSet("ui_render", false)
end

function NMDevicesClient.enableVehiclePersonalPlaybackProbe()
    setLocalDebug("core", true)
    setLocalDebug("runtime", true)
    setLocalDebug("playback_transition", true)
    setLocalDebug("vehicle", true)
    sendDebugSet("core", true)
    sendDebugSet("runtime", true)
    sendDebugSet("playback_transition", true)
    sendDebugSet("vehicle", true)
    logClientDebugBootstrap("vehicle_personal_playback_probe")
end

function NMDevicesClient.enableUiAutoCloseProbe()
    setLocalDebug("core", true)
    setLocalDebug("ui_auto_close", true)
end

function NMDevicesClient.disableUiAutoCloseProbe()
    setLocalDebug("ui_auto_close", false)
end

function NMDevicesClient.enableLootProbe()
    setLocalDebug("loot_probe", true)
    sendDebugSet("loot_probe", true)
end

function NMDevicesClient.disableLootProbe()
    setLocalDebug("loot_probe", false)
    sendDebugSet("loot_probe", false)
end

local function onGameStart()
    if NMCore and NMCore.logBuildVersionLine then
        NMCore.logBuildVersionLine("client")
    end
    logClientDebugBootstrap("onGameStart")
    if NMCore and NMCore.logChannel then
        NMCore.logChannel(
            "core",
            "client_boot",
            string.format(
                "authority=%s isClient=%s isServer=%s liveStrategy=%s",
                tostring(NMCore.getRuntimeAuthorityMode and NMCore.getRuntimeAuthorityMode() or "unknown"),
                tostring(isClient and isClient() or false),
                tostring(isServer and isServer() or false),
                tostring(NMZombieLiveStrategy and NMZombieLiveStrategy.getLiveVisualStrategy and NMZombieLiveStrategy.getLiveVisualStrategy() or "unknown")
            )
        )
    end
    if NMDevicesClient._uiLagProbeAutoEnable == true then
        NMDevicesClient.enableUiLagProbe()
    end
    if NMDevicesClient._uiAutoCloseProbeAutoEnable == true then
        NMDevicesClient.enableUiAutoCloseProbe()
    end
    if NMClientPortableUiDragBlockProbe and NMClientPortableUiDragBlockProbe.onGameStart then
        NMClientPortableUiDragBlockProbe.onGameStart()
    end
    if NMClientSessionProjection and NMClientSessionProjection.onGameStart then
        NMClientSessionProjection.onGameStart()
    end
    if NMDeviceUI and NMDeviceUI.beginSessionStartAutoOpenSuppression then
        NMDeviceUI.beginSessionStartAutoOpenSuppression(0, "on_game_start")
    end
    NMClientRegistrySync.requestInitialSync()
    if not NMCore.isMPClientRuntime() then
        local player = getPlayer and getPlayer() or nil
        if player then
            local seeded = NMWorldRegistrySnapshot.seedCacheForPlayerSP(player, function(entry)
                if not entry or not entry.state then
                    return false
                end
                local sourceMode = tostring(entry.sourceMode or ((entry.kind == "vehicle") and "vehicle" or "placed"))
                local profileType = tostring(entry.profileType or ((entry.kind == "vehicle") and "vehicle_radio" or entry.itemFullType or ""))
                NMClientWorldSourceCache.upsertFromPayload({
                    kind = tostring(entry.kind or "item"),
                    uuid = tostring(entry.uuid or ""),
                    profileType = profileType ~= "" and profileType or nil,
                    sourceMode = sourceMode,
                    x = tonumber(entry.x) or 0,
                    y = tonumber(entry.y) or 0,
                    z = tonumber(entry.z) or 0,
                    sourceEpoch = tonumber(entry.sourceEpoch) or tonumber(entry.state.sourceGeneration) or 0,
                    itemId = entry.itemId,
                    itemFullType = entry.itemFullType,
                    vehicleId = entry.vehicleId,
                    vehicleIdHint = entry.vehicleIdHint,
                    vehicleSqlId = entry.vehicleSqlId,
                    vehicleSqlIdHint = entry.vehicleSqlIdHint,
                    partId = entry.partId,
                    windowsOpen = entry.windowsOpen == true,
                    state = entry.state
                })
                return true
            end)
            if NMCore and NMCore.logChannel and NMCore.isSubsystemDebugEnabled and NMCore.isSubsystemDebugEnabled("core") then
                NMCore.logChannel("core", "sp_snapshot_seed", "seeded=" .. tostring(seeded or 0))
            end
        end
    end
    NMVehicleRadial.installHook()
    if NMGamepadRadial and NMGamepadRadial.installHook then
        NMGamepadRadial.installHook()
    end
    if NMDevicesClient.onGameStart then
        NMDevicesClient.onGameStart()
    end
    if NMClientWorldItemVisualSanitizer and NMClientWorldItemVisualSanitizer.onGameStart then
        NMClientWorldItemVisualSanitizer.onGameStart()
    end
    if NMClientInventoryItemVisualSanitizer and NMClientInventoryItemVisualSanitizer.onGameStart then
        NMClientInventoryItemVisualSanitizer.onGameStart()
    end
end

local function onObjectAdded(obj)
    if NMClientWorldItemVisualSanitizer and NMClientWorldItemVisualSanitizer.onObjectAdded then
        NMClientWorldItemVisualSanitizer.onObjectAdded(obj)
    end
end

if Events then
    if Events.OnFillInventoryObjectContextMenu then
        Events.OnFillInventoryObjectContextMenu.Add(onFillInventoryObjectContextMenu)
    end
    if Events.OnFillWorldObjectContextMenu then
        Events.OnFillWorldObjectContextMenu.Add(onFillWorldObjectContextMenu)
    end
    if Events.OnTick then
        Events.OnTick.Add(onTick)
    end
    if Events.OnServerCommand then
        Events.OnServerCommand.Add(onServerCommand)
    end
    if Events.OnGameStart then
        Events.OnGameStart.Add(onGameStart)
    end
    if Events.OnObjectAdded then
        Events.OnObjectAdded.Add(onObjectAdded)
    end
end



