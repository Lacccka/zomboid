require "zombies/NMZombieAudioVisualSupport"
require "zombies/NMZombieMediaPayloadRuntime"
require "zombies/NMServerZombieAssignmentShared"

NMServerZombieAssignmentApplyShared = NMServerZombieAssignmentApplyShared or {}

local function syncCompanionCaseRegistration(zombie, payload, state, opts)
    local runtimeLabel = tostring(opts and opts.companionCaseRuntimeLabel or "")
    if runtimeLabel == "" then
        return nil
    end
    local source = tostring(opts and opts.companionCaseSource or opts and opts.strategyName or "")
    local result = NMZombieAudioVisualSupport.registerManagedCompanionCase(zombie, payload, state, source, runtimeLabel)
    if result and result.ok then
        return result
    end
    NMZombieAudioVisualSupport.recordCompanionCaseRegistrationFailure(
        zombie,
        payload,
        state,
        source,
        runtimeLabel,
        result and result.reason or "unknown"
    )
    return result
end

local function buildAttachedOutcome(zombie, previousPayload, selection, spec, payload, item, state, opts, attachMeta)
    -- Invariant: payload finalization is part of the authoritative assignment seam and must happen before returning.
    NMServerZombieAssignmentShared.finalizePayloadSync(zombie, payload, previousPayload)
    local companionCaseResult = syncCompanionCaseRegistration(zombie, payload, state, opts)
    return {
        ok = true,
        status = "attached",
        reason = nil,
        selection = selection,
        spec = spec,
        payload = payload,
        item = item,
        state = state,
        companionCaseResult = companionCaseResult,
        usedInventoryFallback = attachMeta and attachMeta.usedInventoryFallback == true
    }
end

function NMServerZombieAssignmentApplyShared.applyAssignment(zombie, opts)
    opts = opts or {}
    local strategyName = tostring(opts.strategyName or "")
    local selection = opts.selection
        or NMZombieVisualTargetLedger and NMZombieVisualTargetLedger.getOrAssignZombieSelection
        and NMZombieVisualTargetLedger.getOrAssignZombieSelection(zombie, strategyName)
        or nil
    local inventory = zombie and zombie.getInventory and zombie:getInventory() or nil
    if not inventory then
        return {
            ok = false,
            status = "failed",
            reason = "missing_inventory",
            selection = selection
        }
    end

    local spec, payload, selectionReason = NMServerZombieAssignmentShared.resolveSelectionContext(zombie, selection)
    local previousPayload = NMServerZombieAssignmentShared.getStoredPayload(zombie, opts.deps)
    if not spec then
        local removed = NMServerZombieAssignmentShared.clearKnownProofStates(zombie)
        if opts.pruneManagedCompanionCase == true then
            NMZombieAudioVisualSupport.pruneManagedCompanionCaseItems(zombie, "", "")
        end
        NMServerZombieAssignmentShared.finalizePayloadSync(zombie, payload, previousPayload)
        local baseReason = selectionReason == "device_disabled" and "device_disabled" or "ledger_selected_false"
        local status = payload and payload.mediaMode == "media_only" and "media_only" or (selectionReason == "device_disabled" and "suppressed" or "excluded")
        return {
            ok = false,
            status = status,
            reason = removed > 0 and (baseReason .. "_scrubbed") or baseReason,
            selection = selection,
            spec = NMServerZombieAssignmentShared.getSpecForVariantId(selection and selection.variantId or ""),
            payload = payload,
            removedCount = removed
        }
    end

    local removed = NMServerZombieAssignmentShared.clearKnownProofStates(zombie, spec.variantId)
    if not NMServerZombieAssignmentShared.zombieSupportsProofLocation(zombie, spec) then
        return {
            ok = false,
            status = "failed",
            reason = "missing_proof_location",
            selection = selection,
            spec = spec,
            payload = payload,
            removedCount = removed
        }
    end

    local attachedItem, attachMeta = NMServerZombieAssignmentShared.findAttachedProofItem(zombie, spec, {
        deps = opts.deps,
        allowInventoryFallback = opts.allowAttachedInventoryFallback == true,
        wantedUuid = opts.wantedUuid
    })
    if attachedItem then
        local state, stateReason = NMServerZombieAssignmentShared.applyPayloadToProofState(zombie, attachedItem, spec, payload, strategyName)
        if stateReason then
            return {
                ok = false,
                status = "failed",
                reason = stateReason,
                selection = selection,
                spec = spec,
                payload = payload,
                item = attachedItem,
                usedInventoryFallback = attachMeta and attachMeta.usedInventoryFallback == true
            }
        end
        return buildAttachedOutcome(zombie, previousPayload, selection, spec, payload, attachedItem, state, opts, attachMeta)
    end

    local item = NMServerZombieAssignmentShared.findInventoryProofItem(zombie, spec, {
        deps = opts.deps,
        wantedUuid = opts.wantedUuid
    })
    local createdItem = false
    if not item then
        local addReason = nil
        item, addReason = NMServerZombieAssignmentShared.addInventoryProofItem(zombie, spec)
        if not item then
            return {
                ok = false,
                status = "failed",
                reason = addReason or "item_add_failed",
                selection = selection,
                spec = spec,
                payload = payload
            }
        end
        createdItem = true
    end

    local state, stateReason = NMServerZombieAssignmentShared.applyPayloadToProofState(zombie, item, spec, payload, strategyName)
    if stateReason then
        return {
            ok = false,
            status = "failed",
            reason = stateReason,
            selection = selection,
            spec = spec,
            payload = payload,
            item = item,
            state = state
        }
    end

    local ok, attachReason = NMZombieAudioVisualSupport.attachProofItem(zombie, item, spec)
    if not ok then
        if createdItem then
            NMServerZombieAssignmentShared.removeInventoryItem(zombie, item)
        end
        return {
            ok = false,
            status = "failed",
            reason = attachReason,
            selection = selection,
            spec = spec,
            payload = payload,
            item = item,
            state = state
        }
    end

    return buildAttachedOutcome(zombie, previousPayload, selection, spec, payload, item, state, opts, nil)
end

return NMServerZombieAssignmentApplyShared
