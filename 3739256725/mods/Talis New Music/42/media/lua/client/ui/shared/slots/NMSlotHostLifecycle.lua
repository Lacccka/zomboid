NMSlotHostLifecycle = NMSlotHostLifecycle or {}

local function resolveNowMs()
    local nowMs = (getTimestampMs and tonumber(getTimestampMs()))
        or (getTimeInMillis and tonumber(getTimeInMillis()))
        or 0
    if nowMs <= 0 and getTimestamp then
        nowMs = (tonumber(getTimestamp()) or 0) * 1000
    end
    return nowMs
end

local function resolveTargetCacheKey(target)
    if not target then
        return ""
    end
    if tostring(target.kind or "") == "vehicle" then
        return table.concat({
            "vehicle",
            tostring(target.vehicleId or ""),
            tostring(target.partId or "")
        }, "|")
    end
    return table.concat({
        tostring(target.kind or "item"),
        tostring(target.itemId or ""),
        tostring(target.uuid or "")
    }, "|")
end

local function resolveDraggedItemsSnapshot()
    if resolveDraggedInventoryItemsSnapshot then
        return resolveDraggedInventoryItemsSnapshot()
    end
    if NMSlotActionCommon and NMSlotActionCommon.getDraggedInventoryItems then
        return NMSlotActionCommon.getDraggedInventoryItems()
    end
    return {}, true
end

local function logHostExtractEvent(eventName, window, drag, extra)
    if NMSlotActionCommon and NMSlotActionCommon.logSlotExtractEvent then
        local context = extra or {}
        context.window = window
        context.drag = drag
        context.slotType = context.slotType or (drag and drag.slotType) or ""
        NMSlotActionCommon.logSlotExtractEvent(eventName, context)
    end
end

function NMSlotHostLifecycle.initHostState(window)
    if not window then
        return nil
    end
    window._nmFrameEpoch = tonumber(window._nmFrameEpoch) or 0
    window._nmFrameNowMs = tonumber(window._nmFrameNowMs) or 0
    window._nmContextCache = window._nmContextCache or nil
    window._nmContextCacheEpoch = window._nmContextCacheEpoch or nil
    window._nmContextCacheTargetKey = window._nmContextCacheTargetKey or nil
    window._nmSlotHostFrame = window._nmSlotHostFrame or nil
    window._nmSlotHostFrameEpoch = window._nmSlotHostFrameEpoch or nil
    window._nmSlotFrameModel = window._nmSlotFrameModel or nil
    window._nmSlotFrameModelEpoch = window._nmSlotFrameModelEpoch or nil
    window._nmSlotRemoveInFlightByType = window._nmSlotRemoveInFlightByType or {}
    return window
end

function NMSlotHostLifecycle.invalidateContextCache(window)
    if not window then
        return
    end
    window._nmContextCache = nil
    window._nmContextCacheEpoch = nil
    window._nmContextCacheTargetKey = nil
    if window.onInvalidateSlotHostContextCache then
        window:onInvalidateSlotHostContextCache()
    end
end

function NMSlotHostLifecycle.invalidateSlotFrameModel(window)
    if not window then
        return
    end
    window._nmSlotHostFrame = nil
    window._nmSlotHostFrameEpoch = nil
    window._nmSlotFrameModel = nil
    window._nmSlotFrameModelEpoch = nil
end

function NMSlotHostLifecycle.markAwaitingAuthoritativeMediaEject(window, fullType)
    if not window then
        return
    end
    local pendingFullType = tostring(fullType or "")
    if pendingFullType == "" then
        window._nmAwaitingAuthoritativeMediaEject = nil
        return
    end
    window._nmAwaitingAuthoritativeMediaInsert = nil
    window._nmAwaitingAuthoritativeMediaEject = {
        active = true,
        fullType = pendingFullType,
        startedAtMs = resolveNowMs()
    }
end

function NMSlotHostLifecycle.clearAwaitingAuthoritativeMediaEject(window)
    if window then
        window._nmAwaitingAuthoritativeMediaEject = nil
    end
end

function NMSlotHostLifecycle.isAwaitingAuthoritativeMediaEject(window, fullType)
    local awaiting = window and window._nmAwaitingAuthoritativeMediaEject or nil
    if not (awaiting and awaiting.active == true) then
        return false
    end
    local awaitingFullType = tostring(awaiting.fullType or "")
    if tostring(fullType or "") == "" then
        return awaitingFullType ~= ""
    end
    return awaitingFullType ~= "" and awaitingFullType == tostring(fullType or "")
end

function NMSlotHostLifecycle.markAwaitingAuthoritativeMediaInsert(window, fullType)
    if not window then
        return
    end
    local pendingFullType = tostring(fullType or "")
    if pendingFullType == "" then
        window._nmAwaitingAuthoritativeMediaInsert = nil
        return
    end
    window._nmAwaitingAuthoritativeMediaEject = nil
    window._nmAwaitingAuthoritativeMediaInsert = {
        active = true,
        fullType = pendingFullType,
        startedAtMs = resolveNowMs()
    }
end

function NMSlotHostLifecycle.clearAwaitingAuthoritativeMediaInsert(window)
    if window then
        window._nmAwaitingAuthoritativeMediaInsert = nil
    end
end

function NMSlotHostLifecycle.isAwaitingAuthoritativeMediaInsert(window, fullType)
    local awaiting = window and window._nmAwaitingAuthoritativeMediaInsert or nil
    if not (awaiting and awaiting.active == true) then
        return false
    end
    local awaitingFullType = tostring(awaiting.fullType or "")
    if tostring(fullType or "") == "" then
        return awaitingFullType ~= ""
    end
    return awaitingFullType ~= "" and awaitingFullType == tostring(fullType or "")
end

local function resolveAwaitingSlotEjectMap(window, create)
    if not window then
        return nil
    end
    local map = window._nmAwaitingAuthoritativeSlotEjectByType
    if map == nil and create == true then
        map = {}
        window._nmAwaitingAuthoritativeSlotEjectByType = map
    end
    return map
end

function NMSlotHostLifecycle.markAwaitingAuthoritativeSlotEject(window, slotType, fullType)
    local key = tostring(slotType or "")
    local pendingFullType = tostring(fullType or "")
    if key == "" or not window then
        return
    end
    local map = resolveAwaitingSlotEjectMap(window, true)
    if pendingFullType == "" then
        map[key] = nil
        return
    end
    map[key] = {
        active = true,
        fullType = pendingFullType,
        startedAtMs = resolveNowMs()
    }
end

function NMSlotHostLifecycle.clearAwaitingAuthoritativeSlotEject(window, slotType)
    local key = tostring(slotType or "")
    if key == "" or not window then
        return
    end
    local map = resolveAwaitingSlotEjectMap(window, false)
    if map then
        map[key] = nil
    end
end

function NMSlotHostLifecycle.getAwaitingAuthoritativeSlotEject(window, slotType)
    local key = tostring(slotType or "")
    if key == "" then
        return nil
    end
    local map = resolveAwaitingSlotEjectMap(window, false)
    local awaiting = map and map[key] or nil
    if awaiting and awaiting.active == true then
        return awaiting
    end
    return nil
end

function NMSlotHostLifecycle.isAwaitingAuthoritativeSlotEject(window, slotType, fullType)
    local awaiting = NMSlotHostLifecycle.getAwaitingAuthoritativeSlotEject(window, slotType)
    if not awaiting then
        return false
    end
    local awaitingFullType = tostring(awaiting.fullType or "")
    if tostring(fullType or "") == "" then
        return awaitingFullType ~= ""
    end
    return awaitingFullType ~= "" and awaitingFullType == tostring(fullType or "")
end

function NMSlotHostLifecycle.beginFrameEpoch(window, reason)
    if not window then
        return 0
    end
    NMSlotHostLifecycle.initHostState(window)
    window._nmFrameEpoch = (tonumber(window._nmFrameEpoch) or 0) + 1
    window._nmFrameReason = tostring(reason or "frame")
    window._nmFrameNowMs = resolveNowMs()
    if window._nmFrameResolveCount ~= nil then
        window._nmFrameResolveCount = 0
    end
    if window._nmFrameFallbackCount ~= nil then
        window._nmFrameFallbackCount = 0
    end
    NMSlotHostLifecycle.invalidateContextCache(window)
    NMSlotHostLifecycle.invalidateSlotFrameModel(window)
    if window.onBeginSlotHostFrameEpoch then
        window:onBeginSlotHostFrameEpoch(window._nmFrameReason)
    end
    return window._nmFrameEpoch
end

function NMSlotHostLifecycle.resolveContext(window)
    if not window then
        return nil
    end
    NMSlotHostLifecycle.initHostState(window)
    local key = resolveTargetCacheKey(window.target)
    local epoch = tonumber(window._nmFrameEpoch) or 0
    if window._nmContextCache
        and window._nmContextCacheEpoch == epoch
        and window._nmContextCacheTargetKey == key then
        return window._nmContextCache
    end
    if not window.resolveContextFreshUncached then
        return nil
    end
    local resolved = window:resolveContextFreshUncached()
    window._nmContextCache = resolved
    window._nmContextCacheEpoch = epoch
    window._nmContextCacheTargetKey = key
    return resolved
end

function NMSlotHostLifecycle.resolveContextFresh(window)
    if not window then
        return nil
    end
    NMSlotHostLifecycle.invalidateContextCache(window)
    if not window.resolveContextFreshUncached then
        return nil
    end
    local resolved = window:resolveContextFreshUncached()
    local key = resolveTargetCacheKey(window.target)
    window._nmContextCache = resolved
    window._nmContextCacheEpoch = tonumber(window._nmFrameEpoch) or 0
    window._nmContextCacheTargetKey = key
    return resolved
end

function NMSlotHostLifecycle.buildHostFrame(window)
    if not window then
        return nil
    end
    NMSlotHostLifecycle.initHostState(window)
    local epoch = tonumber(window._nmFrameEpoch) or 0
    if window._nmSlotHostFrame and window._nmSlotHostFrameEpoch == epoch then
        return window._nmSlotHostFrame
    end

    local resolved = window.resolveContextCached and window:resolveContextCached() or NMSlotHostLifecycle.resolveContext(window)
    local dragItems, dragOk = resolveDraggedItemsSnapshot()
    local frame = nil
    if window.buildSlotHostFrame then
        frame = window:buildSlotHostFrame(epoch, tonumber(window._nmFrameNowMs) or resolveNowMs(), resolved, dragItems, dragOk == true)
    end
    if type(frame) ~= "table" then
        frame = {}
    end
    frame.resolved = frame.resolved or resolved
    frame.dragItems = frame.dragItems or dragItems
    frame.dragOk = frame.dragOk == true or dragOk == true
    frame.nowMs = tonumber(frame.nowMs) or tonumber(window._nmFrameNowMs) or resolveNowMs()
    frame.epoch = tonumber(frame.epoch) or epoch
    frame.window = frame.window or window

    window._nmSlotHostFrame = frame
    window._nmSlotHostFrameEpoch = epoch
    return frame
end

function NMSlotHostLifecycle.buildSlotFrameModel(window)
    if not window then
        return nil
    end
    NMSlotHostLifecycle.initHostState(window)
    local epoch = tonumber(window._nmFrameEpoch) or 0
    if window._nmSlotFrameModel and window._nmSlotFrameModelEpoch == epoch then
        return window._nmSlotFrameModel
    end

    local frame = NMSlotHostLifecycle.buildHostFrame(window)
    local model = {}
    if NMMediaSlot and NMMediaSlot.buildRenderState then
        model.media = NMMediaSlot.buildRenderState(window, frame)
    end
    if NMHeadphoneSlot and NMHeadphoneSlot.buildRenderState then
        model.headphones = NMHeadphoneSlot.buildRenderState(window, frame)
    end
    if NMBatterySlot and NMBatterySlot.buildRenderState then
        model.battery = NMBatterySlot.buildRenderState(window, frame)
    end
    window._nmSlotFrameModel = model
    window._nmSlotFrameModelEpoch = epoch
    return model
end

function NMSlotHostLifecycle.getSlotRenderState(window, slotKey)
    local model = NMSlotHostLifecycle.buildSlotFrameModel(window)
    return model and model[slotKey] or nil
end

function NMSlotHostLifecycle.refreshSlotVisibility(window)
    if window and window.refreshSlotVisibility then
        return window:refreshSlotVisibility()
    end
    return nil
end

function NMSlotHostLifecycle.invalidateForTimedAction(window, options)
    if not window then
        return
    end
    local opts = options or {}
    if opts.context == true and window.invalidateContextCache then
        window:invalidateContextCache()
    end
    if opts.slotFrame ~= false and window.invalidateSlotFrameModel then
        window:invalidateSlotFrameModel()
    end
    if opts.render ~= false and window.invalidateRenderModel then
        window:invalidateRenderModel()
    end
    if opts.visibility ~= false then
        NMSlotHostLifecycle.refreshSlotVisibility(window)
    end
end

function NMSlotHostLifecycle.finalizeSharedSlotDrags(window)
    local portableInteraction = rawget(_G, "NMPortableMediaInteraction") or nil
    if portableInteraction and portableInteraction.finalizePendingExtract then
        portableInteraction.finalizePendingExtract()
    end
    if NMBatterySlot and NMBatterySlot.finalizeExtractDrag then
        NMBatterySlot.finalizeExtractDrag(window)
    end
    if NMHeadphoneSlot and NMHeadphoneSlot.finalizeExtractDrag then
        NMHeadphoneSlot.finalizeExtractDrag(window)
    end
end

function NMSlotHostLifecycle.finalizeOwnedSlotDrag(window)
    if not window then
        return false
    end
    local activeSource = nil
    local activeDrag = nil
    if NMSlotGhostManager and NMSlotGhostManager.getActiveDrag then
        activeSource, activeDrag = NMSlotGhostManager.getActiveDrag()
    end

    local slotType = nil
    if activeSource == window and activeDrag then
        slotType = tostring(activeDrag.slotType or "")
    elseif window._nmMediaExtractDrag then
        slotType = "media"
    elseif window._nmBatteryExtractDrag then
        slotType = "battery"
    elseif window._nmHeadphoneExtractDrag then
        slotType = "headphones"
    end

    if slotType ~= "" then
        logHostExtractEvent("slot_extract_mouseup_host", window, activeDrag, {
            slotType = slotType,
            entrypoint = tostring(window._nmPendingSlotHostMouseUpTag or "host"),
            detail = "host_finalize_enter"
        })
    end

    if slotType == "media" then
        local handled = NMMediaSlot and NMMediaSlot.finalizeExtractDrag and NMMediaSlot.finalizeExtractDrag(window) == true or false
        local outcome = handled and "handled_extract" or "not_ours"
        local consume = handled
        logHostExtractEvent("slot_extract_finalize_result", window, activeDrag, {
            slotType = "media",
            returnValue = tostring(outcome or ""),
            consumeDecision = tostring(consume),
            entrypoint = tostring(window._nmPendingSlotHostMouseUpTag or "host")
        })
        logHostExtractEvent(consume and "slot_extract_release_consumed" or "slot_extract_release_passed_through", window, activeDrag, {
            slotType = "media",
            returnValue = tostring(outcome or ""),
            consumeDecision = tostring(consume),
            entrypoint = tostring(window._nmPendingSlotHostMouseUpTag or "host")
        })
        window._nmPendingSlotHostMouseUpTag = nil
        return consume
    end
    if slotType == "battery" then
        local handled = NMBatterySlot and NMBatterySlot.finalizeExtractDrag and NMBatterySlot.finalizeExtractDrag(window) == true or false
        logHostExtractEvent("slot_extract_finalize_result", window, activeDrag, {
            slotType = "battery",
            returnValue = tostring(handled),
            consumeDecision = tostring(handled),
            entrypoint = tostring(window._nmPendingSlotHostMouseUpTag or "host")
        })
        logHostExtractEvent(handled and "slot_extract_release_consumed" or "slot_extract_release_passed_through", window, activeDrag, {
            slotType = "battery",
            returnValue = tostring(handled),
            consumeDecision = tostring(handled),
            entrypoint = tostring(window._nmPendingSlotHostMouseUpTag or "host")
        })
        window._nmPendingSlotHostMouseUpTag = nil
        return handled
    end
    if slotType == "headphones" then
        local handled = NMHeadphoneSlot and NMHeadphoneSlot.finalizeExtractDrag and NMHeadphoneSlot.finalizeExtractDrag(window) == true or false
        logHostExtractEvent("slot_extract_finalize_result", window, activeDrag, {
            slotType = "headphones",
            returnValue = tostring(handled),
            consumeDecision = tostring(handled),
            entrypoint = tostring(window._nmPendingSlotHostMouseUpTag or "host")
        })
        logHostExtractEvent(handled and "slot_extract_release_consumed" or "slot_extract_release_passed_through", window, activeDrag, {
            slotType = "headphones",
            returnValue = tostring(handled),
            consumeDecision = tostring(handled),
            entrypoint = tostring(window._nmPendingSlotHostMouseUpTag or "host")
        })
        window._nmPendingSlotHostMouseUpTag = nil
        return handled
    end
    window._nmPendingSlotHostMouseUpTag = nil
    return false
end

function NMSlotHostLifecycle.cancelOwnedSlotDrag(window, slotType)
    if not window then
        return false
    end
    local kind = tostring(slotType or "")
    if kind == "media" then
        return NMMediaSlot and NMMediaSlot.cancelExtractDrag and (NMMediaSlot.cancelExtractDrag(window) or true) or false
    end
    if kind == "battery" then
        return NMBatterySlot and NMBatterySlot.cancelExtractDrag and (NMBatterySlot.cancelExtractDrag(window) or true) or false
    end
    if kind == "headphones" then
        return NMHeadphoneSlot and NMHeadphoneSlot.cancelExtractDrag and (NMHeadphoneSlot.cancelExtractDrag(window) or true) or false
    end
    return false
end

function NMSlotHostLifecycle.finishActiveDragAfterMouseRelease()
    if not isMouseButtonDown or isMouseButtonDown(0) == true then
        return false
    end
    local activeSource = nil
    local activeDrag = nil
    if NMSlotGhostManager and NMSlotGhostManager.getActiveDrag then
        activeSource, activeDrag = NMSlotGhostManager.getActiveDrag()
    end
    if not (activeSource and activeDrag) then
        return false
    end

    logHostExtractEvent("slot_extract_mouseup_global_guard", activeSource, activeDrag, {
        slotType = tostring(activeDrag.slotType or ""),
        entrypoint = "global_release_guard",
        detail = "guard_enter"
    })

    NMSlotHostLifecycle.finalizeOwnedSlotDrag(activeSource)

    local remainingSource = nil
    local remainingDrag = nil
    if NMSlotGhostManager and NMSlotGhostManager.getActiveDrag then
        remainingSource, remainingDrag = NMSlotGhostManager.getActiveDrag()
    end
    if remainingSource == activeSource and remainingDrag == activeDrag then
        logHostExtractEvent("slot_extract_mouseup_global_guard", activeSource, activeDrag, {
            slotType = tostring(activeDrag.slotType or ""),
            entrypoint = "global_release_guard",
            detail = "guard_cancel_remaining"
        })
        NMSlotHostLifecycle.cancelOwnedSlotDrag(activeSource, activeDrag.slotType)
    end

    if NMBatterySlot and NMBatterySlot.ensureGhostOverlay then
        NMBatterySlot.ensureGhostOverlay()
    end
    if NMMediaSlot and NMMediaSlot.ensureGhostOverlay then
        NMMediaSlot.ensureGhostOverlay()
    end
    if NMHeadphoneSlot and NMHeadphoneSlot.ensureGhostOverlay then
        NMHeadphoneSlot.ensureGhostOverlay()
    end
    logHostExtractEvent("slot_extract_cleanup_result", activeSource, activeDrag, {
        slotType = tostring(activeDrag.slotType or ""),
        entrypoint = "global_release_guard",
        detail = "guard_exit"
    })
    return true
end

function NMSlotHostLifecycle.noteHostMouseUp(window, entrypoint)
    if not window then
        return
    end
    window._nmPendingSlotHostMouseUpTag = tostring(entrypoint or "host")
end

function NMSlotHostLifecycle.cancelSharedSlotDrags(window)
    if NMBatterySlot and NMBatterySlot.cancelExtractDrag then
        NMBatterySlot.cancelExtractDrag(window)
    end
    if NMMediaSlot and NMMediaSlot.cancelExtractDrag then
        NMMediaSlot.cancelExtractDrag(window)
    end
    if NMHeadphoneSlot and NMHeadphoneSlot.cancelExtractDrag then
        NMHeadphoneSlot.cancelExtractDrag(window)
    end
end

function NMSlotHostLifecycle.buildSharedMediaSlotZone(spec)
    local window = spec and spec.window or nil
    local rect = spec and spec.rect or nil
    if not (window and rect) then
        return nil
    end
    local zoneKind = tostring(spec.zoneKind or "slot")
    local canAcceptDraggedMedia = spec.canAcceptDraggedMedia
    local canEjectMedia = spec.canEjectMedia
    local canStartExtractDrag = spec.canStartExtractDrag
    local performInsertFromDrag = spec.performInsertFromDrag
    local performBeginExtract = spec.performBeginExtract
    local performEject = spec.performEject
    local performShowInsertContext = spec.performShowInsertContext
    local consumeDraggedMediaInsert = spec.consumeDraggedMediaInsert
    local beginMediaExtractDrag = spec.beginMediaExtractDrag
    local handleRightClick = spec.handleRightClick
    local dragItems = spec.dragItems
    local canAccept = false
    if canAcceptDraggedMedia then
        canAccept = canAcceptDraggedMedia(dragItems) == true
    end
    return {
        uiFamily = tostring(spec.uiFamily or window.slotHostFamily or "generic"),
        zoneKind = zoneKind,
        priority = tonumber(spec.priority) or 10,
        zOrder = tonumber(spec.zOrder) or 0,
        playerNum = tonumber(window.playerNum) or 0,
        itemId = spec.itemId or window.target and window.target.itemId or nil,
        uuid = spec.uuid or window.target and window.target.uuid or nil,
        window = window,
        rect = rect,
        visible = spec.visible ~= false,
        enabled = spec.enabled ~= false,
        interactive = spec.interactive ~= false,
        canAccept = canAccept,
        canAcceptDraggedMedia = function(items)
            return canAcceptDraggedMedia and canAcceptDraggedMedia(items) == true or false
        end,
        canEjectMedia = function()
            return canEjectMedia and canEjectMedia() == true or false
        end,
        canStartExtractDrag = function()
            return canStartExtractDrag and canStartExtractDrag() == true or false
        end,
        performInsertFromDrag = function(items, sourceTag)
            return performInsertFromDrag and performInsertFromDrag(items, sourceTag) == true or false
        end,
        performBeginExtract = function()
            return performBeginExtract and performBeginExtract() == true or false
        end,
        performEject = function(sourceTag)
            return performEject and performEject(sourceTag) == true or false
        end,
        performShowInsertContext = function(btn, xArg, yArg)
            return performShowInsertContext and performShowInsertContext(btn, xArg, yArg) == true or false
        end,
        consumeDraggedMediaInsert = function(items, sourceDescriptor)
            return consumeDraggedMediaInsert and consumeDraggedMediaInsert(items, sourceDescriptor) == true or false
        end,
        beginMediaExtractDrag = function()
            return beginMediaExtractDrag and beginMediaExtractDrag() == true or false
        end,
        handleRightClick = function(btn, xArg, yArg)
            return handleRightClick and handleRightClick(btn, xArg, yArg) == true or false
        end
    }
end

return NMSlotHostLifecycle
