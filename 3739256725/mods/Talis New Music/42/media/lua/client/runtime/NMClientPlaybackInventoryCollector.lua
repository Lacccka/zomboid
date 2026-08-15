-- Helpers for collecting managed inventory devices and normalizing rebound state.
NMClientPlaybackInventoryCollector = NMClientPlaybackInventoryCollector or {}

function NMClientPlaybackInventoryCollector.collectManaged(player, out)
    if not (player and player.getInventory and out) then
        return out
    end

    local allItems = {}
    NMInventoryHelpers.collectItemsRecursive(player:getInventory(), allItems)
    for i = 1, #allItems do
        local item = allItems[i]
        local profile = NMDeviceProfiles.getForItem(item)
        if profile then
            local state = NMDeviceState.ensure(item, profile)
            if state and state.deviceUUID then
                local itemMd = item and item.getModData and item:getModData() or nil
                if itemMd and itemMd.nmCorpseRecovered == true then
                    state._nmCorpseRecovered = true
                end
                out[#out + 1] = {
                    item = item,
                    profile = profile,
                    state = state,
                    uuid = tostring(state.deviceUUID)
                }
            end
        end
    end

    return out
end

function NMClientPlaybackInventoryCollector.normalizeCorpseRecoveredInventoryState(profile, state, uuid, options)
    if NMCore and NMCore.isMPClientRuntime and NMCore.isMPClientRuntime() then
        return false
    end
    if not (profile and state and uuid and uuid ~= "") then
        return false
    end
    if not (NMDeviceProfiles and NMDeviceProfiles.isPortableTrackedProfile and NMDeviceProfiles.isPortableTrackedProfile(profile) == true) then
        return false
    end
    if tostring(state.lastStopReason or "") ~= "corpse_reconcile" then
        return false
    end

    local reboundSeen = options and options.corpseInventoryReboundSeen or nil
    if reboundSeen and reboundSeen[uuid] == true then
        return false
    end
    if reboundSeen then
        reboundSeen[uuid] = true
    end

    state.authoritativeMode = "off"
    state.sourceKind = "inventory"
    state.sourceOwner = nil
    state.sourceX = nil
    state.sourceY = nil
    state.sourceZ = nil
    state.sourceGeneration = 0
    state.playbackMode = "inventory"
    state.zombieDormant = false
    state.zombieDormantReason = nil
    state.zombieDormantStrategy = nil

    local logRuntime = options and options.logRuntime or nil
    if logRuntime then
        logRuntime(
            "corpse_inventory_rebind",
            string.format(
                "uuid=%s playbackMode=%s media=%s headphones=%s sourceKind=%s sourceGeneration=%s",
                tostring(uuid),
                tostring(state.playbackMode or ""),
                tostring(state.mediaFullType or "nil"),
                tostring(state.headphoneItemFullType or "nil"),
                tostring(state.sourceKind or ""),
                tostring(state.sourceGeneration or 0)
            )
        )
    end

    return true
end

return NMClientPlaybackInventoryCollector
