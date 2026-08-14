-- Server handlers for vehicle radio intents and state replication.

NMServerVehicleIntentHandlers = NMServerVehicleIntentHandlers or {}
NMServerVehicleIntentHandlers._progressionHintSig = NMServerVehicleIntentHandlers._progressionHintSig or {}
NMServerVehicleIntentHandlers._acceptedTrackFinishedToken = NMServerVehicleIntentHandlers._acceptedTrackFinishedToken or {}

local function resolveTrackCountFromMedia(state)
    if not state or not state.mediaFullType or not NMMusic or not NMMusic.resolveTracks then
        return 0
    end
    local ok, resolved = pcall(NMMusic.resolveTracks, state.mediaFullType)
    if not ok or type(resolved) ~= "table" or type(resolved.tracks) ~= "table" then
        return 0
    end
    return #resolved.tracks
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

local function logProgressionHint(tag, key, detail)
    if not (NMCore and NMCore.logChannel and NMCore.isDebugKnobOn and NMCore.isDebugKnobOn("progressionProbe")) then
        return
    end
    local sigKey = tostring(key or "")
    if sigKey ~= "" then
        if tostring(NMServerVehicleIntentHandlers._progressionHintSig[sigKey] or "") == tostring(detail or "") then
            return
        end
        NMServerVehicleIntentHandlers._progressionHintSig[sigKey] = tostring(detail or "")
    end
    NMCore.logChannel("progressionProbe", tostring(tag or "progression"), tostring(detail or ""))
end

local function logSlotAuthority(tag, detail)
    if not (NMCore and NMCore.logChannel and NMCore.isDebugKnobOn and NMCore.isDebugKnobOn("slotAuthorityProbe")) then
        return
    end
    NMCore.logChannel("slotAuthorityProbe", tostring(tag or "slot_authority"), tostring(detail or ""))
end

local function logVehicleSlotTrace(stage, detail)
    logSlotAuthority(stage or "vehicle_slot_trace", detail)
end

local function describeSourceDescriptorArgs(args, prefix)
    local readFn = NMInventoryHelpers and NMInventoryHelpers.readSourceDescriptorFromArgs or nil
    local descriptor = readFn and readFn(args, prefix or "mediaSource") or nil
    if type(descriptor) ~= "table" then
        return "none"
    end
    return string.format(
        "kind=%s itemId=%s itemUuid=%s vehicleId=%s partId=%s parentItemId=%s square=%s,%s,%s objectIndex=%s deadBodyIndex=%s",
        tostring(descriptor.kind or ""),
        tostring(descriptor.itemId or ""),
        tostring(descriptor.itemUuid or ""),
        tostring(descriptor.vehicleId or ""),
        tostring(descriptor.partId or ""),
        tostring(descriptor.parentItemId or ""),
        tostring(descriptor.squareX or ""),
        tostring(descriptor.squareY or ""),
        tostring(descriptor.squareZ or ""),
        tostring(descriptor.objectIndex or ""),
        tostring(descriptor.deadBodyIndex or "")
    )
end

local function resolveCurrentTrackDurationInfo(state)
    local fallbackMs = tonumber(
        NMRuntimeConfig and NMRuntimeConfig.getServerVehicleTrackDurationMs and NMRuntimeConfig.getServerVehicleTrackDurationMs()
        or NMRuntimeConfig and NMRuntimeConfig.get and NMRuntimeConfig.get("serverVehicleTrackDurationMs", 210000)
        or 210000
    ) or 210000
    return NMTrackProgressionContract.resolve(state, {
        fallbackMs = fallbackMs,
        context = "vehicle",
        worldAuthoritative = true
    })
end

local function resolveTrackFinishedHintMinAgeMs()
    local minAge = tonumber(
        NMRuntimeConfig and NMRuntimeConfig.get and NMRuntimeConfig.get("serverVehicleTrackHintMinAgeMs", 5000)
        or 5000
    ) or 5000
    return math.max(0, math.floor(minAge + 0.5))
end

local function resolveStartedAtForHint(state, registryEntry)
    local timeline = NMServerTrackTimeline.read(registryEntry, state)
    local startedAtMs = tonumber(timeline and timeline.startedAtMs) or 0
    if startedAtMs > 0 then
        return startedAtMs, "state_started"
    end
    local dueAtMs = tonumber(timeline and timeline.dueAtMs) or 0
    local durationMs = tonumber(timeline and timeline.durationMs) or 0
    if dueAtMs > 0 and durationMs > 0 then
        local derived = math.max(0, math.floor(dueAtMs - durationMs))
        if derived > 0 then
            return derived, "derived_due_minus_duration"
        end
    end
    return 0, "missing"
end

local function validateTrackFinishedHintExpectedState(state, args)
    local expectedRevision = tonumber(args and args.expectedRevision)
    if expectedRevision ~= nil and expectedRevision >= 0 and (tonumber(state and state.revision) or 0) ~= expectedRevision then
        return false, "stale_revision"
    end
    local expectedEpoch = tonumber(args and args.expectedPlaybackEpoch)
    if expectedEpoch ~= nil and expectedEpoch >= 0 and (tonumber(state and state.playbackEpoch) or 0) ~= expectedEpoch then
        return false, "stale_epoch"
    end
    local expectedTrack = tonumber(args and args.expectedTrackIndex)
    if expectedTrack ~= nil and expectedTrack >= 0 and (tonumber(state and state.trackIndex) or 0) ~= expectedTrack then
        return false, "stale_track"
    end
    return true, nil
end

local function setServerTrackTimeline(state, entry, reason)
    if not state then
        return nil
    end
    return NMServerTrackTimeline.armForReason(entry, state, nowRealMs(), "vehicle", tostring(reason or "start_playback"))
end

local function removeItemById(inventory, itemId)
    local id = tostring(itemId or "")
    if not inventory or id == "" then
        return false, "invalid_remove_args", nil, nil
    end
    local item = NMInventoryHelpers.findItemById(inventory, id)
    if not item then
        return false, "source_item_not_found", nil, nil
    end
    local container = item.getContainer and item:getContainer() or nil
    if container and container.DoRemoveItem then
        container:DoRemoveItem(item)
        return true, nil, container, item
    end
    if inventory.Remove then
        inventory:Remove(item)
        return true, nil, inventory, item
    end
    return false, "source_remove_failed", nil, nil
end

local function replicateNormalizedItemMove(meta, liveItem)
    if not (meta and meta.moved == true and liveItem) then
        return
    end
    local sourceItem = meta.sourceItem or liveItem
    local targetItem = meta.targetItem or liveItem
    if sendRemoveItemFromContainer and meta.sourceContainer and sourceItem then
        sendRemoveItemFromContainer(meta.sourceContainer, sourceItem)
    end
    if sendAddItemToContainer and meta.targetContainer and targetItem then
        sendAddItemToContainer(meta.targetContainer, targetItem)
    end
end

local function isSourceDescriptorNearby(player, descriptor)
    local sq = player and player.getSquare and player:getSquare() or nil
    if not sq or type(descriptor) ~= "table" then
        return false
    end
    local kind = tostring(descriptor.kind or "")
    if kind == "player_inventory" then
        return true
    end
    if kind == "inventory_container_item" and descriptor.squareX == nil then
        return true
    end
    if kind == "vehicle_part_container" then
        local vehicleId = tonumber(descriptor.vehicleId)
        local vehicle = vehicleId ~= nil and getVehicleById and getVehicleById(vehicleId) or nil
        if not (vehicle and vehicle.getX and vehicle.getY and vehicle.getZ) then
            return false
        end
        local px, py, pz = sq:getX(), sq:getY(), sq:getZ()
        local dx = (tonumber(vehicle:getX()) or px) - px
        local dy = (tonumber(vehicle:getY()) or py) - py
        local dz = math.abs((tonumber(vehicle:getZ()) or pz) - pz)
        return ((dx * dx) + (dy * dy)) <= 25 and dz <= 2
    end
    local sx = tonumber(descriptor.squareX)
    local sy = tonumber(descriptor.squareY)
    local sz = tonumber(descriptor.squareZ)
    if sx == nil or sy == nil or sz == nil then
        return false
    end
    local px, py, pz = sq:getX(), sq:getY(), sq:getZ()
    local dx = sx - px
    local dy = sy - py
    local dz = math.abs(sz - pz)
    return ((dx * dx) + (dy * dy)) <= 9 and dz <= 2
end

local function sendVehicleLootStaleReject(player, args, descriptor, reason)
    if not (player and sendServerCommand and NMCore and NMCore.NetModule) then
        return
    end
    if tostring(reason or "") ~= "source_item_missing" then
        return
    end
    if type(descriptor) ~= "table" or tostring(descriptor.kind or "") ~= "vehicle_part_container" then
        return
    end
    sendServerCommand(player, NMCore.NetModule, "vehicle_loot_stale_reject", {
        slotTraceId = tostring(args and args.slotTraceId or ""),
        vehicleId = tostring(args and args.vehicleId or descriptor.vehicleId or ""),
        partId = tostring(args and args.partId or "Radio"),
        sourcePartId = tostring(descriptor.partId or ""),
        mediaItemId = tostring(args and args.mediaItemId or ""),
        mediaItemUuid = tostring(args and args.mediaItemUuid or ""),
        containerType = tostring(descriptor.containerType or ""),
        reason = tostring(reason or "")
    })
end

local function normalizeIngressArgsToMainInventory(player, args, action)
    if NMServerMediaIngress and NMServerMediaIngress.normalizeInsertArgsToMainInventory then
        return NMServerMediaIngress.normalizeInsertArgsToMainInventory(player, args, action, {
            hostTag = "vehicle",
            beginTag = "server_vehicle_normalize_begin",
            okTag = "server_vehicle_normalize_ok",
            rejectTag = "server_vehicle_normalize_reject",
            rootOkTag = "server_vehicle_normalize_root_ok",
            logSlotAuthority = logSlotAuthority,
            describeSourceDescriptorArgs = describeSourceDescriptorArgs,
            isSourceDescriptorNearby = isSourceDescriptorNearby,
            replicateNormalizedItemMove = replicateNormalizedItemMove,
            traceContext = {
                host = "vehicle",
                slotTraceId = tostring(args and args.slotTraceId or "")
            },
            onSourceResolveReject = function(targetPlayer, targetArgs, descriptor, reason)
                sendVehicleLootStaleReject(targetPlayer, targetArgs, descriptor, reason)
            end,
            logTraceReject = function(targetArgs, act, reason)
                logVehicleSlotTrace(
                    "vehicle_slot_transfer",
                    string.format(
                        "trace=%s host=vehicle ui=vehicle action=%s stage=transfer result=reject reason=%s vehicleId=%s partId=%s",
                        tostring(targetArgs and targetArgs.slotTraceId or ""),
                        tostring(act),
                        tostring(reason or "normalize_failed"),
                        tostring(targetArgs and targetArgs.vehicleId or ""),
                        tostring(targetArgs and targetArgs.partId or "")
                    )
                )
            end
        })
    end
    return false, "normalize_helper_missing"
end

local function addItemByFullType(inventory, fullType)
    if not inventory or not inventory.AddItem then
        return nil, "missing_inventory_add", nil
    end
    local typeName = tostring(fullType or "")
    if typeName == "" then
        return nil, "missing_item_type", nil
    end
    local item, err = NMWorldItemVisuals.addItemWithVisual(inventory, typeName)
    if not item then
        return nil, err or "add_item_failed", nil
    end
    return item, nil, inventory
end

local function setDrainableFraction(item, fraction)
    local value = NMCore.clamp(tonumber(fraction) or 0.0, 0.0, 1.0)
    if item and item.setCurrentUsesFloat then
        local ok = pcall(item.setCurrentUsesFloat, item, value)
        if ok then return true end
    end
    if item and item.setUsedDelta then
        local ok = pcall(item.setUsedDelta, item, value)
        if ok then return true end
    end
    if item and item.setDelta then
        local ok = pcall(item.setDelta, item, value)
        if ok then return true end
    end
    return false
end

local function setRecordedMediaIndex(item, index)
    local value = tonumber(index)
    if not item or value == nil or value < 0 then
        return false
    end
    if item.setRecordedMediaIndex then
        local ok = pcall(item.setRecordedMediaIndex, item, value)
        if ok then return true end
    end
    if item.setRecordedMediaIndexInteger then
        local ok = pcall(item.setRecordedMediaIndexInteger, item, value)
        if ok then return true end
    end
    return false
end

local function applyTransitionOps(player, inventory, ops)
    if not ops then
        return false, nil
    end
    local applied = false
    local failures = {}

    if ops.wearHeadphoneItemId then
        local ok, err = false, "wear_helper_missing"
        if NMAttachmentHelpers.equipHeadphonesByItemId then
            ok, err = NMAttachmentHelpers.equipHeadphonesByItemId(player, ops.wearHeadphoneItemId)
        end
        if ok then
            applied = true
        else
            failures[#failures + 1] = "wear_headphones:" .. tostring(err)
        end
    end
    if ops.unequipHeadphones then
        local ok, err = false, "unequip_helper_missing"
        if NMAttachmentHelpers.unequipWornHeadphones then
            ok, err = NMAttachmentHelpers.unequipWornHeadphones(player)
        end
        if ok then
            applied = true
        else
            failures[#failures + 1] = "unequip_headphones:" .. tostring(err)
        end
    end

    if ops.consumeMediaItemId then
        local ok, err, container, consumed = removeItemById(inventory, ops.consumeMediaItemId)
        if ok then
            applied = true
            logSlotAuthority(
                "server_vehicle_consume_media_ok",
                string.format(
                    "itemId=%s consumedFullType=%s containerType=%s",
                    tostring(ops.consumeMediaItemId or ""),
                    tostring(consumed and consumed.getFullType and consumed:getFullType() or ""),
                    tostring(container and container.getType and container:getType() or "")
                )
            )
            if sendRemoveItemFromContainer and container and consumed then
                sendRemoveItemFromContainer(container, consumed)
            end
        else
            logSlotAuthority("server_vehicle_consume_media_fail", "itemId=" .. tostring(ops.consumeMediaItemId or "") .. " reason=" .. tostring(err or ""))
            failures[#failures + 1] = "consume_media:" .. tostring(err)
        end
    end
    if ops.consumeHeadphoneItemId then
        local ok, err, container, consumed = removeItemById(inventory, ops.consumeHeadphoneItemId)
        if ok then
            applied = true
            if sendRemoveItemFromContainer and container and consumed then
                sendRemoveItemFromContainer(container, consumed)
            end
        else
            failures[#failures + 1] = "consume_headphones:" .. tostring(err)
        end
    end
    if ops.consumeBatteryItemId then
        local ok, err, container, consumed = removeItemById(inventory, ops.consumeBatteryItemId)
        if ok then
            applied = true
            if sendRemoveItemFromContainer and container and consumed then
                sendRemoveItemFromContainer(container, consumed)
            end
        else
            failures[#failures + 1] = "consume_battery:" .. tostring(err)
        end
    end

    if ops.produceMediaFullType then
        local out, err, container = addItemByFullType(inventory, ops.produceMediaFullType)
        if out then
            setRecordedMediaIndex(out, ops.produceMediaRecordedMediaIndex)
            applied = true
            logSlotAuthority(
                "server_vehicle_produce_media_ok",
                string.format(
                    "fullType=%s outItemId=%s outItemUuid=%s containerType=%s",
                    tostring(ops.produceMediaFullType or ""),
                    tostring(NMCore.itemId(out) or ""),
                    tostring(NMInventoryHelpers.getItemStateUuid and NMInventoryHelpers.getItemStateUuid(out) or ""),
                    tostring(container and container.getType and container:getType() or "")
                )
            )
            if sendAddItemToContainer and container then
                sendAddItemToContainer(container, out)
            end
        else
            logSlotAuthority("server_vehicle_produce_media_fail", "fullType=" .. tostring(ops.produceMediaFullType or "") .. " reason=" .. tostring(err or ""))
            failures[#failures + 1] = "produce_media:" .. tostring(err)
        end
    end
    if ops.produceHeadphoneFullType then
        local out, err, container = addItemByFullType(inventory, ops.produceHeadphoneFullType)
        if out then
            applied = true
            if sendAddItemToContainer and container then
                sendAddItemToContainer(container, out)
            end
        else
            failures[#failures + 1] = "produce_headphones:" .. tostring(err)
        end
    end
    if ops.produceBatteryCharge ~= nil then
        local out, err, container = addItemByFullType(inventory, "Base.Battery")
        if out then
            setDrainableFraction(out, ops.produceBatteryCharge)
            applied = true
            if sendAddItemToContainer and container then
                sendAddItemToContainer(container, out)
            end
        else
            failures[#failures + 1] = "produce_battery:" .. tostring(err)
        end
    end

    if #failures > 0 then
        return applied, table.concat(failures, ";")
    end
    return applied, nil
end

local function applyObservedDurationHint(state, args)
    if type(state) ~= "table" then
        return false, nil
    end
    local observed = tonumber(args and args.observedDurationMs) or 0
    if observed <= 0 then
        return false, nil
    end
    local clamped = math.max(1000, math.floor(observed + 0.5))
    local idx = math.max(1, math.floor(tonumber(state.trackIndex) or 1))
    state.observedTrackDurationHints = state.observedTrackDurationHints or {}
    state.observedTrackDurationHints[idx] = clamped
    return true, clamped
end

local function resolveSourceOwnerIdentity(player)
    if not player then
        return nil
    end
    local onlineId = player.getOnlineID and tostring(player:getOnlineID() or "") or ""
    if onlineId ~= "" then
        return onlineId
    end
    local username = player.getUsername and tostring(player:getUsername() or "") or ""
    if username ~= "" then
        return username
    end
    return nil
end

local function applyVehicleAuthority(player, vehicle, state)
    local authority = NMAuthorityV4 or NMAuthorityV3
    if not (authority and authority.applyIntent and state) then
        return
    end
    local sourceOwner = resolveSourceOwnerIdentity(player)
    authority.applyIntent(state, "request_vehicle", {
        sourceX = vehicle and tonumber(vehicle:getX()) or nil,
        sourceY = vehicle and tonumber(vehicle:getY()) or nil,
        sourceZ = vehicle and tonumber(vehicle:getZ()) or nil,
        sourceOwner = sourceOwner
    })
end

local function resolveVehicleAndPart(args)
    local vehicleId = tonumber(args and args.vehicleId)
    if vehicleId == nil or not getVehicleById then
        return nil, nil
    end
    local vehicle = getVehicleById(vehicleId)
    if not vehicle then
        return nil, nil
    end
    local partId = tostring(args and args.partId or "Radio")
    local part = vehicle.getPartById and vehicle:getPartById(partId) or nil
    return vehicle, part
end

local function resolveCanonicalVehicleSourceGeneration(state, existingEntry)
    return math.max(
        0,
        tonumber(state and state.sourceGeneration) or 0,
        tonumber(existingEntry and existingEntry.sourceEpoch) or 0,
        tonumber(existingEntry and existingEntry.sourceGeneration) or 0
    )
end

local function resolveCanonicalEntrySourceGeneration(entry, state)
    return math.max(
        0,
        tonumber(entry and entry.sourceEpoch) or 0,
        tonumber(entry and entry.sourceGeneration) or 0,
        tonumber(state and state.sourceGeneration) or 0
    )
end

local function findRegistryEntryByVehiclePart(vehicleId, partId)
    local vid = tostring(vehicleId or "")
    local pid = tostring(partId or "Radio")
    if vid == "" then
        return nil
    end
    local world = NMServerRegistryState and NMServerRegistryState.worldRegistry or nil
    if type(world) ~= "table" then
        return nil
    end
    for _, entry in pairs(world) do
        if type(entry) == "table"
            and tostring(entry.kind or "") == "vehicle"
            and tostring(entry.vehicleId or "") == vid
            and tostring(entry.partId or "Radio") == pid then
            return entry
        end
    end
    return nil
end

local function sendVehicleState(player, vehicle, part, state, canonicalSourceGen, transitionReason, slotTraceId)
    if not player or not vehicle or not part or not state or not sendServerCommand then
        return
    end
    local canonicalGen = math.max(0, tonumber(canonicalSourceGen) or tonumber(state.sourceGeneration) or 0)
    state.sourceGeneration = math.max(tonumber(state.sourceGeneration) or 0, canonicalGen)
    if vehicle.transmitPartModData then
        vehicle:transmitPartModData(part)
        vehicle:updateParts()
    end
    sendServerCommand(player, NMCore.NetModule, "state", {
        vehicleId = tostring(vehicle:getId()),
        vehicleSqlId = NMVehicleHelpers and NMVehicleHelpers.getVehicleSqlIdString and NMVehicleHelpers.getVehicleSqlIdString(vehicle) or "",
        partId = tostring(part:getId()),
        state = NMDeviceState.export(state),
        sourceGeneration = canonicalGen,
        slotTraceId = slotTraceId ~= nil and tostring(slotTraceId) or nil,
        transitionReason = transitionReason ~= nil and tostring(transitionReason) or nil,
        serverSessionToken = NMServerBootReset and NMServerBootReset.getSessionToken and NMServerBootReset.getSessionToken() or nil
    })
end

local function upsertRegistry(vehicle, part, profile, state, transitionReason)
    if not vehicle or not part or not profile or not state or not state.deviceUUID then
        return nil, tonumber(state and state.sourceGeneration) or 0
    end
    local vehicleSqlId = NMVehicleHelpers and NMVehicleHelpers.getVehicleSqlIdString and NMVehicleHelpers.getVehicleSqlIdString(vehicle) or ""
    if vehicleSqlId == "" then
        if NMCore and NMCore.logChannel and NMCore.isDebugKnobOn and NMCore.isDebugKnobOn("runtimeProbe") then
            NMCore.logChannel(
                "runtimeProbe",
                "server_vehicle_registry_missing_sql",
                string.format(
                    "uuid=%s vehicleId=%s partId=%s",
                    tostring(state and state.deviceUUID or ""),
                    tostring(vehicle and vehicle.getId and vehicle:getId() or "nil"),
                    tostring(part and part.getId and part:getId() or "Radio")
                )
            )
        end
        return nil, tonumber(state and state.sourceGeneration) or 0
    end
    local uuid = tostring(state.deviceUUID)
    local existing = NMServerRegistryState.worldRegistry[uuid]
    local canonicalGen = resolveCanonicalVehicleSourceGeneration(state, existing)
    state.sourceGeneration = canonicalGen

    local entry = existing or {}
    entry.kind = "vehicle"
    entry.uuid = uuid
    entry.vehicleId = tostring(vehicle:getId())
    entry.vehicleIdHint = tostring(vehicle:getId())
    entry.vehicleSqlId = vehicleSqlId
    entry.vehicleSqlIdHint = tostring(entry.vehicleSqlId or "")
    entry.ownerId = tostring(state.sourceOwner or "")
    entry.partId = tostring(part:getId())
    entry.profileType = "vehicle_radio"
    entry.x = tonumber(vehicle:getX()) or 0
    entry.y = tonumber(vehicle:getY()) or 0
    entry.z = tonumber(vehicle:getZ()) or 0
    entry.sourceMode = "vehicle"
    entry.sourceEpoch = canonicalGen
    entry.sourceGeneration = canonicalGen
    entry.sourceRebind = false
    entry.windowsOpen = NMVehicleHelpers.vehicleWindowsOpen(vehicle)
    entry.stateSnapshot = NMDeviceState.export(state)
    entry.playbackEpoch = tonumber(state.playbackEpoch) or 0
    entry.trackIndex = tonumber(state.trackIndex) or 1
    entry.trackCount = math.max(
        0,
        math.floor(
            tonumber(state.trackCount)
                or tonumber(existing and existing.trackCount)
                or resolveTrackCountFromMedia(state)
                or 0
        )
    )
    entry.playbackPolicy = tostring(state.playbackPolicy or "autoplay")
    entry.transitionReason = transitionReason ~= nil and tostring(transitionReason) or nil
    if entry._vehicleResolved == nil then
        entry._vehicleResolved = true
    end
    if tostring(entry._vehicleContinuityMode or "") == "" then
        entry._vehicleContinuityMode = "LIVE_RESOLVED"
    end
    if NMServerRegistryBroadcast and NMServerRegistryBroadcast.touchEntry then
        NMServerRegistryBroadcast.touchEntry(entry, state)
    end

    NMServerRegistryState.worldRegistry[entry.uuid] = entry

    if NMCore and NMCore.logChannel and NMCore.isDebugKnobOn and NMCore.isDebugKnobOn("vehicleTruthProbe") then
        local nowStamp = nowRealMs()
        local liveVehicleSqlId = NMVehicleHelpers and NMVehicleHelpers.getVehicleSqlIdString and NMVehicleHelpers.getVehicleSqlIdString(vehicle) or ""
        local liveRuntimeId = tostring(vehicle and vehicle.getId and vehicle:getId() or "")
        local md = part and part.getModData and part:getModData() or nil
        local node = md and md[NMCore.StateKey] or nil
        local currentPartUuid = tostring(node and node.deviceUUID or "")
        local stateDeviceUuid = tostring(state and state.deviceUUID or "")
        local sqlConsistency = (tostring(entry.vehicleSqlId or "") == tostring(liveVehicleSqlId or "")) and "match" or "mismatch"
        local uuidKey = tostring(entry.uuid or "")
        local proofSig = table.concat({
            tostring(entry.vehicleSqlId or ""),
            tostring(liveVehicleSqlId or ""),
            tostring(entry.vehicleId or ""),
            tostring(liveRuntimeId or ""),
            tostring(entry.partId or "Radio"),
            tostring(currentPartUuid or ""),
            tostring(stateDeviceUuid or ""),
            tostring(sqlConsistency),
            tostring(transitionReason or "upsert")
        }, "|")
        local lastProofSig = tostring(NMServerRegistryState.vehicleTruthSqlProofSigByUuid[uuidKey] or "")
        local lastProofMs = tonumber(NMServerRegistryState.vehicleTruthSqlProofMsByUuid[uuidKey]) or 0
        if proofSig ~= lastProofSig or (nowStamp - lastProofMs) >= 20000 then
            NMServerRegistryState.vehicleTruthSqlProofSigByUuid[uuidKey] = proofSig
            NMServerRegistryState.vehicleTruthSqlProofMsByUuid[uuidKey] = nowStamp
            local traceToken = table.concat({
                tostring(entry.uuid or ""),
                tostring(entry.sourceGeneration or 0),
                tostring(tonumber(state and state.revision) or 0),
                tostring(tonumber(state and state.playbackEpoch) or 0)
            }, "|")
            NMCore.logChannel(
                "vehicleTruthProbe",
                "vehicle_truth_sql_anchor_proof",
                string.format(
                    "traceToken=%s uuid=%s reasonContext=%s sqlConsistency=%s authoritativeSql=%s liveVehicleSql=%s authoritativeRuntimeHint=%s liveRuntimeId=%s partId=%s partUuid=%s stateDeviceUuid=%s",
                    tostring(traceToken),
                    tostring(entry.uuid or ""),
                    tostring(transitionReason or "upsert"),
                    tostring(sqlConsistency),
                    tostring(entry.vehicleSqlId or ""),
                    tostring(liveVehicleSqlId or ""),
                    tostring(entry.vehicleId or ""),
                    tostring(liveRuntimeId or ""),
                    tostring(entry.partId or "Radio"),
                    tostring(currentPartUuid),
                    tostring(stateDeviceUuid)
                )
            )
        end

        local identityKey = table.concat({
            tostring(entry.uuid or ""),
            tostring(entry.vehicleSqlId or ""),
            tostring(entry.partId or "Radio")
        }, "|")
        local prevPartUuid = tostring(NMServerRegistryState.vehiclePartUuidByIdentity[identityKey] or "")
        if prevPartUuid ~= "" and currentPartUuid ~= "" and prevPartUuid ~= currentPartUuid then
            local traceToken = table.concat({
                tostring(entry.uuid or ""),
                tostring(entry.sourceGeneration or 0),
                tostring(tonumber(state and state.revision) or 0),
                tostring(tonumber(state and state.playbackEpoch) or 0)
            }, "|")
            NMCore.logChannel(
                "vehicleTruthProbe",
                "vehicle_truth_part_uuid_stability",
                string.format(
                    "traceToken=%s uuid=%s vehicleSqlId=%s partId=%s previousPartUuid=%s currentPartUuid=%s reasonContext=%s",
                    tostring(traceToken),
                    tostring(entry.uuid or ""),
                    tostring(entry.vehicleSqlId or ""),
                    tostring(entry.partId or "Radio"),
                    tostring(prevPartUuid),
                    tostring(currentPartUuid),
                    tostring(transitionReason or "upsert")
                )
            )
        end
        if currentPartUuid ~= "" then
            NMServerRegistryState.vehiclePartUuidByIdentity[identityKey] = currentPartUuid
        end
    end
    return entry, canonicalGen
end

local function suppressVanillaVehicleRadio(part)
    local data = part and part.getDeviceData and part:getDeviceData() or nil
    if data and data.getIsTurnedOn and data.setIsTurnedOn and data:getIsTurnedOn() then
        data:setIsTurnedOn(false)
    end
end

local function isPowerOnRequest(action, args, state)
    local name = tostring(action or "")
    if name == "power_on" then
        return true
    end
    if name == "toggle_power" then
        if args and args.isOn ~= nil then
            return args.isOn == true
        end
        return not (state and state.isOn == true)
    end
    return false
end

function NMServerVehicleIntentHandlers.applyVehicleIntent(player, args)
    local vehicle, part = resolveVehicleAndPart(args)
    if not vehicle or not part then
        return false, "vehicle_or_part_missing"
    end

    local profile = NMDeviceProfiles.getVehicleProfile(part)
    if not profile then
        return false, "vehicle_profile_missing"
    end

    local existingRegistryEntry = findRegistryEntryByVehiclePart(
        vehicle and vehicle.getId and vehicle:getId() or nil,
        part and part.getId and part:getId() or "Radio"
    )
    local state = nil
    if existingRegistryEntry then
        state = NMDeviceState.peek and NMDeviceState.peek(part) or nil
        if not state then
            if NMCore and NMCore.logChannel and NMCore.isDebugKnobOn and NMCore.isDebugKnobOn("runtimeProbe") then
                NMCore.logChannel(
                    "runtimeProbe",
                    "identity_state_probe",
                    string.format(
                        "uuid=%s hasState=false hasUuid=false source=server_vehicle_intent_existing",
                        tostring(existingRegistryEntry.uuid or "")
                    )
                )
            end
            return false, "identity_missing_readonly"
        end
        local existingUuid = tostring(state.deviceUUID or "")
        if NMCore and NMCore.logChannel and NMCore.isDebugKnobOn and NMCore.isDebugKnobOn("runtimeProbe") then
            NMCore.logChannel(
                "runtimeProbe",
                "identity_state_probe",
                string.format(
                    "uuid=%s hasState=true hasUuid=%s source=server_vehicle_intent_existing",
                    tostring(existingUuid ~= "" and existingUuid or existingRegistryEntry.uuid or ""),
                    tostring(existingUuid ~= "")
                )
            )
        end
        if existingUuid == "" then
            return false, "identity_uuid_missing_readonly"
        end
        local expectedUuid = tostring(existingRegistryEntry.uuid or "")
        if expectedUuid ~= "" and expectedUuid ~= existingUuid then
            if NMCore and NMCore.logChannel and NMCore.isDebugKnobOn and NMCore.isDebugKnobOn("runtimeProbe") then
                NMCore.logChannel(
                    "runtimeProbe",
                    "identity_conflict",
                    string.format(
                        "uuid=%s observed=%s expected=%s stage=server_vehicle_intent_existing",
                        tostring(expectedUuid),
                        tostring(existingUuid),
                        tostring(expectedUuid)
                    )
                )
                NMCore.logChannel(
                    "runtimeProbe",
                    "identity_uuid_conflict_drop",
                    string.format(
                        "kind=vehicle vehicleId=%s partId=%s",
                        tostring(vehicle and vehicle.getId and vehicle:getId() or "nil"),
                        tostring(part and part.getId and part:getId() or "Radio")
                    )
                )
            end
            return false, "identity_uuid_conflict_drop"
        end
    else
        state = NMDeviceState.ensureInitialized and NMDeviceState.ensureInitialized(part, profile, "explicit_init") or NMDeviceState.ensure(part, profile)
    end
    if not state then
        return false, "identity_init_forbidden"
    end
    if NMServerBootReset and NMServerBootReset.normalizeState then
        NMServerBootReset.normalizeState(state, "vehicle", tostring(state and state.deviceUUID or ""))
    end
    local wasPlaying = state and state.isPlaying == true
    local stateBeforeIntent = NMDeviceState.export(state)
    local trackCount = tonumber(args.trackCount) or 0
    if trackCount < 1 then
        trackCount = resolveTrackCountFromMedia(state)
    end
    local inv = player and player.getInventory and player:getInventory() or nil
    local action = tostring(args and args.action or "")
    logVehicleSlotTrace(
        "vehicle_slot_server_receive",
        string.format(
            "trace=%s host=vehicle ui=vehicle action=%s stage=server_receive result=ok vehicleId=%s partId=%s mediaItemId=%s mediaItemUuid=%s",
            tostring(args and args.slotTraceId or ""),
            tostring(action),
            tostring(args and args.vehicleId or ""),
            tostring(args and args.partId or ""),
            tostring(args and args.mediaItemId or ""),
            tostring(args and args.mediaItemUuid or "")
        )
    )
    local normalizedOk, normalizedErr = normalizeIngressArgsToMainInventory(player, args, action)
    if not normalizedOk then
        logVehicleSlotTrace(
            "vehicle_slot_server_receive",
            string.format(
                "trace=%s host=vehicle ui=vehicle action=%s stage=server_receive result=reject vehicleId=%s partId=%s reason=%s",
                tostring(args and args.slotTraceId or ""),
                tostring(action),
                tostring(args and args.vehicleId or ""),
                tostring(args and args.partId or ""),
                tostring(normalizedErr or "")
            )
        )
        return false, "normalize_failed:" .. tostring(normalizedErr or "unknown")
    end
    local mediaPayload = nil
    if inv and args.mediaItemId then
        local mediaItem = NMInventoryHelpers.findItemById(inv, args.mediaItemId)
        mediaPayload = NMMediaHelpers.resolveMediaInsertPayload(mediaItem)
    end
    local payload = {
        isOn = args.isOn,
        isPlaying = args.isPlaying,
        volume = args.volume,
        playbackMode = "world",
        playbackPolicy = args.playbackPolicy,
        observedDurationMs = args.observedDurationMs,
        mediaItemId = args.mediaItemId,
        mediaFullType = mediaPayload and mediaPayload.mediaFullType or args.mediaFullType,
        mediaItemFullType = mediaPayload and (mediaPayload.mediaEjectFullType or mediaPayload.mediaFullType) or args.mediaEjectFullType or args.mediaFullType,
        mediaCarrier = mediaPayload and mediaPayload.mediaCarrier or args.mediaCarrier,
        mediaEjectFullType = mediaPayload and mediaPayload.mediaEjectFullType or args.mediaEjectFullType,
        mediaCanonicalFullType = mediaPayload and mediaPayload.mediaCanonicalFullType or args.mediaCanonicalFullType,
        mediaRecordedMediaIndex = mediaPayload and mediaPayload.mediaRecordedMediaIndex or args.mediaRecordedMediaIndex,
        mediaDisplayName = mediaPayload and mediaPayload.mediaDisplayName or args.mediaDisplayName,
        externalPowerAvailable = NMVehicleHelpers.vehicleHasUsableBatteryPower and NMVehicleHelpers.vehicleHasUsableBatteryPower(vehicle, part) or NMVehicleHelpers.vehicleHasPower(vehicle, part),
        trackCount = trackCount,
        hasTrack = trackCount > 0
    }
    state.trackCount = math.max(0, math.floor(trackCount))

    if isPowerOnRequest(action, args, state) and payload.externalPowerAvailable ~= true then
        return false, "vehicle_battery_unavailable"
    end
    local serverOwnedVehicleProgression = NMCore and NMCore.isMPServerAuthority and NMCore.isMPServerAuthority() and action == "track_finished"
    if NMCore and NMCore.logChannel and NMCore.isDebugKnobOn and NMCore.isDebugKnobOn("intent") then
        NMCore.logChannel(
            "intent",
            "server_vehicle_intent_attempt",
            string.format(
                "action=%s vehicle=%s part=%s media=%s isOn=%s isPlaying=%s trackCount=%d hasTrack=%s",
                tostring(action),
                tostring(vehicle and vehicle.getId and vehicle:getId() or "unknown"),
                tostring(part and part.getId and part:getId() or "unknown"),
                tostring(state and state.mediaFullType or "nil"),
                tostring(state and state.isOn == true),
                tostring(state and state.isPlaying == true),
                tonumber(trackCount) or 0,
                tostring(trackCount > 0)
            )
        )
    end
    if serverOwnedVehicleProgression then
        local durationInfo = resolveCurrentTrackDurationInfo(state)
        local expectedOk, expectedReason = validateTrackFinishedHintExpectedState(state, args)
        local validation = ItemIntentValidation or NMServerItemIntentValidation
        if not expectedOk then
            if expectedReason == "stale_revision"
                and validation
                and validation.canBypassStaleRevisionForTrackFinished
                and validation.canBypassStaleRevisionForTrackFinished(state, args) then
                logProgressionHint(
                    "server_vehicle_track_finished_hint_revision_bypass",
                    string.format("rev_bypass:%s:%s:%s", tostring(state and state.deviceUUID or ""), tostring(state and state.playbackEpoch or 0), tostring(state and state.trackIndex or 0)),
                    string.format(
                        "reason=stale_revision_bypass vehicle=%s part=%s expectedRevision=%s currentRevision=%s token=%s:%s",
                        tostring(vehicle and vehicle.getId and vehicle:getId() or "unknown"),
                        tostring(part and part.getId and part:getId() or "unknown"),
                        tostring(tonumber(args and args.expectedRevision)),
                        tostring(tonumber(state and state.revision) or 0),
                        tostring(tonumber(state and state.playbackEpoch) or 0),
                        tostring(tonumber(state and state.trackIndex) or 0)
                    )
                )
                expectedOk = true
            else
            logProgressionHint(
                "server_vehicle_track_finished_hint_rejected",
                string.format("stale:%s:%s:%s:%s", tostring(expectedReason or "stale"), tostring(state and state.deviceUUID or ""), tostring(state and state.playbackEpoch or 0), tostring(state and state.trackIndex or 0)),
                string.format(
                        "reason=%s vehicle=%s part=%s trackIndex=%s playbackEpoch=%s",
                        tostring(expectedReason or "stale_state"),
                        tostring(vehicle and vehicle.getId and vehicle:getId() or "unknown"),
                        tostring(part and part.getId and part:getId() or "unknown"),
                        tostring(state and state.trackIndex or 1),
                        tostring(state and state.playbackEpoch or 0)
                    )
            )
            return false, expectedReason or "stale_state"
            end
        end

        local minHintAgeMs = resolveTrackFinishedHintMinAgeMs()
        local startedAtMs, startedSource = resolveStartedAtForHint(state, existingRegistryEntry)
        local nowMsValue = nowRealMs()
        local ageMs = math.max(0, nowMsValue - startedAtMs)
        local missingStartedAt = startedAtMs <= 0
        if (not missingStartedAt) and ageMs < minHintAgeMs then
            logProgressionHint(
                "server_vehicle_track_finished_hint_rejected",
                string.format("early:%s:%s:%s", tostring(state and state.deviceUUID or ""), tostring(state and state.playbackEpoch or 0), tostring(state and state.trackIndex or 0)),
                string.format(
                        "reason=too_early vehicle=%s part=%s ageMs=%s minHintAgeMs=%s startedAtMs=%s startedSource=%s nowMs=%s",
                        tostring(vehicle and vehicle.getId and vehicle:getId() or "unknown"),
                        tostring(part and part.getId and part:getId() or "unknown"),
                        tostring(ageMs),
                        tostring(minHintAgeMs),
                        tostring(startedAtMs),
                        tostring(startedSource or "unknown"),
                        tostring(nowMsValue)
                    )
            )
            return false, "too_early"
        end
        if missingStartedAt then
            logProgressionHint(
                "server_vehicle_track_finished_hint_started_missing_bypass",
                string.format("missing:%s:%s:%s", tostring(state and state.deviceUUID or ""), tostring(state and state.playbackEpoch or 0), tostring(state and state.trackIndex or 0)),
                string.format("reason=started_at_missing_bypass vehicle=%s part=%s nowMs=%s", tostring(vehicle and vehicle.getId and vehicle:getId() or "unknown"), tostring(part and part.getId and part:getId() or "unknown"), tostring(nowMsValue))
            )
        end

        local dedupeToken = string.format(
            "%s:%s:%s",
            tostring(state and state.deviceUUID or ""),
            tostring(state and state.playbackEpoch or 0),
            tostring(state and state.trackIndex or 0)
        )
        if tostring(NMServerVehicleIntentHandlers._acceptedTrackFinishedToken[dedupeToken] or "") ~= "" then
            logProgressionHint(
                "server_vehicle_track_finished_hint_rejected",
                "dup:" .. tostring(dedupeToken),
                string.format("reason=duplicate vehicle=%s part=%s token=%s",
                        tostring(vehicle and vehicle.getId and vehicle:getId() or "unknown"),
                        tostring(part and part.getId and part:getId() or "unknown"),
                        tostring(dedupeToken))
            )
            return false, "duplicate"
        end
        NMServerVehicleIntentHandlers._acceptedTrackFinishedToken[dedupeToken] = "accepted"

        logProgressionHint(
            "server_vehicle_track_finished_hint_accepted",
            "ok:" .. tostring(dedupeToken),
            string.format(
                    "cause=hint_accept token=%s vehicle=%s part=%s ageMs=%s minHintAgeMs=%s trackIndex=%s playbackEpoch=%s durationKnown=%s",
                    tostring(dedupeToken),
                    tostring(vehicle and vehicle.getId and vehicle:getId() or "unknown"),
                    tostring(part and part.getId and part:getId() or "unknown"),
                    tostring(ageMs),
                    tostring(minHintAgeMs),
                    tostring(state and state.trackIndex or 1),
                    tostring(state and state.playbackEpoch or 0),
                    tostring(durationInfo and durationInfo.knownDuration == true)
                )
        )
        local hinted, hintedMs = applyObservedDurationHint(state, args)
        if hinted then
            logProgressionHint(
                "server_vehicle_track_finished_hint_duration_recorded",
                "hint_ms:" .. tostring(dedupeToken),
                string.format(
                    "uuid=%s trackIndex=%s playbackEpoch=%s observedDurationMs=%s",
                    tostring(state and state.deviceUUID or ""),
                    tostring(state and state.trackIndex or 1),
                    tostring(state and state.playbackEpoch or 0),
                    tostring(hintedMs or 0)
                )
            )
        end
    end

    local changed, reason, ops = NMDeviceTransitions.apply(profile, state, action, payload)
    logVehicleSlotTrace(
        "vehicle_slot_transition_apply",
        string.format(
            "trace=%s host=vehicle ui=vehicle action=%s stage=transition_apply result=%s vehicleId=%s partId=%s reason=%s mediaItemId=%s payloadMedia=%s ejectFullType=%s",
            tostring(args and args.slotTraceId or ""),
            tostring(action),
            tostring(changed and "ok" or "reject"),
            tostring(args and args.vehicleId or ""),
            tostring(args and args.partId or ""),
            tostring(reason or ""),
            tostring(args and args.mediaItemId or ""),
            tostring(payload and payload.mediaFullType or ""),
            tostring(payload and payload.mediaEjectFullType or "")
        )
    )
    if part and profile.vehicleUsesCarBattery and state.isOn and (not NMVehicleHelpers.vehicleHasPower(vehicle, part)) then
        state.isOn = false
        state.desiredIsOn = false
        state.isPlaying = false
        state.desiredIsPlaying = false
        state.lastStopReason = "vehicle_battery_empty"
        changed = true
    end

    local _, opsError = applyTransitionOps(player, inv, ops)
    if opsError then
        logVehicleSlotTrace(
            "vehicle_slot_transition_apply",
            string.format(
                "trace=%s host=vehicle ui=vehicle action=%s stage=transition_apply result=reject vehicleId=%s partId=%s reason=ops_failed:%s",
                tostring(args and args.slotTraceId or ""),
                tostring(action),
                tostring(args and args.vehicleId or ""),
                tostring(args and args.partId or ""),
                tostring(opsError or "")
            )
        )
        if stateBeforeIntent then
            NMDeviceState.import(state, stateBeforeIntent)
            changed = false
        end
        if NMCore and NMCore.logChannel then
            NMCore.logChannel(
                "intent",
                "server_vehicle_ops_failed",
                string.format(
                    "action=%s player=%s vehicle=%s part=%s mediaItemId=%s resolvedMedia=%s opsError=%s",
                    tostring(action),
                    tostring(player and player.getUsername and player:getUsername() or "unknown"),
                    tostring(vehicle and vehicle.getId and vehicle:getId() or "unknown"),
                    tostring(part and part.getId and part:getId() or "unknown"),
                    tostring(args and args.mediaItemId or ""),
                    tostring(payload.mediaFullType or ""),
                    tostring(opsError)
                )
            )
        end
        return false, "ops_failed:" .. tostring(opsError)
    end

    if changed then
        if (action == "set_mute" or action == "toggle_mute" or action == "set_volume")
            and NMCore and NMCore.logChannel and NMCore.isDebugKnobOn and NMCore.isDebugKnobOn("intent") then
            NMCore.logChannel(
                "intent",
                "server_vehicle_level_change_applied",
                string.format(
                    "action=%s vehicle=%s part=%s wasPlaying=%s isPlaying=%s muted=%s volume=%.2f",
                    tostring(action),
                    tostring(vehicle and vehicle.getId and vehicle:getId() or "unknown"),
                    tostring(part and part.getId and part:getId() or "unknown"),
                    tostring(wasPlaying),
                    tostring(state and state.isPlaying == true),
                    tostring(state and state.isMuted == true),
                    tonumber(state and state.volume) or 0
                )
            )
        end
        if action == "toggle_play" or action == "start_playback" or action == "stop_playback"
            or action == "next_track" or action == "prev_track"
            or action == "set_playback_mode" or action == "power_on" or action == "power_off"
            or action == "toggle_power" or action == "track_finished" then
            NMDeviceState.bumpPlaybackEpoch(state)
        end
        applyVehicleAuthority(player, vehicle, state)
        NMDeviceState.bumpRevision(state)
        if NMCore and NMCore.logChannel and NMCore.isDebugKnobOn and NMCore.isDebugKnobOn("intent") then
            NMCore.logChannel(
                "intent",
                "server_vehicle_intent_applied",
                string.format(
                    "action=%s vehicle=%s part=%s media=%s isOn=%s isPlaying=%s trackCount=%d",
                    tostring(action),
                    tostring(vehicle and vehicle.getId and vehicle:getId() or "unknown"),
                    tostring(part and part.getId and part:getId() or "unknown"),
                    tostring(state and state.mediaFullType or "nil"),
                    tostring(state and state.isOn == true),
                    tostring(state and state.isPlaying == true),
                    tonumber(trackCount) or 0
                )
            )
        end
    elseif NMCore and NMCore.logChannel and NMCore.isDebugKnobOn and NMCore.isDebugKnobOn("intent") then
        NMCore.logChannel(
            "intent",
            "server_vehicle_intent_rejected",
            string.format(
                "action=%s reason=%s vehicle=%s part=%s media=%s isOn=%s isPlaying=%s trackCount=%d",
                tostring(action),
                tostring(reason or "none"),
                tostring(vehicle and vehicle.getId and vehicle:getId() or "unknown"),
                tostring(part and part.getId and part:getId() or "unknown"),
                tostring(state and state.mediaFullType or "nil"),
                tostring(state and state.isOn == true),
                tostring(state and state.isPlaying == true),
                tonumber(trackCount) or 0
            )
        )
    end

    local keep = NMRegistryPolicy.shouldKeepWorldSourceState(state)
    local entry, canonicalSourceGen = upsertRegistry(vehicle, part, profile, state, action)
    local shouldRefreshTimeline = changed
        and entry
        and state.isOn == true
        and state.isPlaying == true
        and (
            action == "start_playback"
            or action == "next_track"
            or action == "prev_track"
            or action == "track_finished"
            or (action == "toggle_play" and state.isPlaying == true)
        )
    if shouldRefreshTimeline then
        setServerTrackTimeline(state, entry, action)
        entry.stateSnapshot = NMDeviceState.export(state)
        if NMServerRegistryBroadcast and NMServerRegistryBroadcast.touchEntry then
            NMServerRegistryBroadcast.touchEntry(entry, state)
        end
    end

    suppressVanillaVehicleRadio(part)
    logVehicleSlotTrace(
        "vehicle_slot_state_emit",
        string.format(
            "trace=%s host=vehicle ui=vehicle action=%s stage=state_emit result=ok vehicleId=%s partId=%s media=%s isOn=%s isPlaying=%s sourceGeneration=%s",
            tostring(args and args.slotTraceId or ""),
            tostring(action),
            tostring(vehicle and vehicle.getId and vehicle:getId() or ""),
            tostring(part and part.getId and part:getId() or ""),
            tostring(state and state.mediaFullType or ""),
            tostring(state and state.isOn == true),
            tostring(state and state.isPlaying == true),
            tostring(canonicalSourceGen or 0)
        )
    )
    sendVehicleState(player, vehicle, part, state, canonicalSourceGen, action, args and args.slotTraceId)

    if entry and sendServerCommand then
        local function recipients(applyFn)
            local players = getOnlinePlayers and getOnlinePlayers() or nil
            if not players then
                return
            end
            for i = 0, players:size() - 1 do
                local p = players:get(i)
                if p then
                    applyFn(p)
                end
            end
        end
        NMServerRegistryBroadcast.broadcastEntry(
            NMServerRegistryState.worldRegistry,
            tostring(entry.uuid or ""),
            nil,
            entry.stateSnapshot,
            keep and "upsert" or "remove",
            recipients
        )
        logVehicleSlotTrace(
            "vehicle_slot_registry_broadcast",
            string.format(
                "trace=%s host=vehicle ui=vehicle action=%s stage=registry_broadcast result=ok uuid=%s vehicleId=%s partId=%s op=%s media=%s",
                tostring(args and args.slotTraceId or ""),
                tostring(action),
                tostring(entry.uuid or ""),
                tostring(entry.vehicleId or ""),
                tostring(entry.partId or ""),
                tostring(keep and "upsert" or "remove"),
                tostring(state and state.mediaFullType or "")
            )
        )
        if not keep then
            NMServerRegistryState.worldRegistry[tostring(entry.uuid)] = nil
        end
        if NMCore and NMCore.logChannel and NMCore.isDebugKnobOn and NMCore.isDebugKnobOn("runtimeProbe") then
            NMCore.logChannel(
                "runtimeProbe",
                "server_vehicle_registry",
                string.format(
                    "uuid=%s vehicleId=%s op=%s isOn=%s isPlaying=%s media=%s",
                    tostring(entry.uuid or "nil"),
                    tostring(entry.vehicleId or "nil"),
                    keep and "upsert" or "remove",
                    tostring(state and state.isOn == true),
                    tostring(state and state.isPlaying == true),
                    tostring(state and state.mediaFullType or "nil")
                )
            )
        end
    end

    if changed then
        return true, nil
    end
    return false, reason
end

