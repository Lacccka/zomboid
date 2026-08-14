NMServerSPZombieAssignmentFlow = NMServerSPZombieAssignmentFlow or {}
require "zombies/NMZombieAudioVisualSupport"
require "zombies/NMZombieDeviceVariantCatalog"
require "zombies/NMZombieMediaPayloadResolver"
require "zombies/NMZombieMediaPayloadRuntime"
require "zombies/NMServerZombieScanHelpers"
require "zombies/NMServerZombieAssignmentShared"
require "zombies/NMServerZombieAssignmentApplyShared"
require "zombies/NMServerZombieAssignmentOutcomeShared"

local STRATEGY_NAME = "sp_runtime_attach"
local HEARTBEAT_TICKS = 900
local SCAN_INTERVAL_TICKS = 120
local SCAN_RADIUS_SQ = 50 * 50
local SCAN_ZOMBIE_LIMIT = 32
local SCAN_PLAYER_LIMIT = 4
local FAILED_RETRY_TICKS = 600
local LOADED_SWEEP_LIMIT = 12

NMServerSPZombieAssignmentFlow._diag = NMServerSPZombieAssignmentFlow._diag or {
    ticks = 0,
    updateCalls = 0,
    attachAttempts = 0,
    attachSuccess = 0,
    attachFailure = 0,
    locationFailures = 0,
    scanCalls = 0,
    scanCandidates = 0,
    scanNearby = 0,
    scanLoadedSweep = 0,
    listCursor = 0,
    attachInventoryFallback = 0,
    attachExcluded = 0,
    attachExcludedScrubbed = 0,
    lastReportedAttachSuccess = 0,
    lastReportedAttachFailure = 0,
    lastReportedAttachFallback = 0,
    attachSuppressed = 0
}

local function canRunAuthoritativeMutation()
    if NMCore and NMCore.isMPClientRuntime and NMCore.isMPClientRuntime() == true then
        return false
    end
    if NMAuthorityContract and NMAuthorityContract.canMutateDurableStateAtRuntime then
        return NMAuthorityContract.canMutateDurableStateAtRuntime() == true
    end
    return true
end

local function shouldRun()
    return canRunAuthoritativeMutation()
        and NMZombieLiveStrategy
        and NMZombieLiveStrategy.shouldRunSPRuntimeAttach
        and NMZombieLiveStrategy.shouldRunSPRuntimeAttach() == true
end

local function shouldLogProofVerbose()
    return NMCore and NMCore.isSubsystemDebugEnabled and NMCore.isSubsystemDebugEnabled("zombie_assignment") == true
end

local function logProof(tag, detail, force)
    if not force and not shouldLogProofVerbose() then
        return
    end
    print("[NewMusic] [ZombieProof] " .. tostring(tag or "") .. " " .. tostring(detail or ""))
end

local function zombieDebugId(zombie)
    return tostring(zombie and zombie.getOnlineID and zombie:getOnlineID() or zombie and zombie.getObjectID and zombie:getObjectID() or "unknown")
end

local function isAliveZombie(zombie)
    return NMServerZombieScanHelpers.isAliveZombie(zombie)
end

local function getModData(holder)
    return NMServerZombieAssignmentShared.getModData(holder)
end

local function collectCandidatePlayers()
    return NMServerZombieScanHelpers.collectCandidatePlayers({
        playerLimit = SCAN_PLAYER_LIMIT,
        includeSpecificPlayers = true,
        includeLocalPlayer = true
    })
end

local function isZombieNearAnyPlayer(zombie, players)
    return NMServerZombieScanHelpers.isZombieNearAnyPlayer(zombie, players, SCAN_RADIUS_SQ)
end

local function getSpecForVariantId(variantId)
    return NMServerZombieAssignmentShared.getSpecForVariantId(variantId)
end

local function getStampedVariantSpec(zombie)
    return NMServerZombieAssignmentShared.getStampedVariantSpec(zombie)
end

local function findAttachedProofItem(zombie, spec)
    local item, meta = NMServerZombieAssignmentShared.findAttachedProofItem(zombie, spec, { allowInventoryFallback = true })
    if meta and meta.usedInventoryFallback == true then
        NMServerSPZombieAssignmentFlow._diag.attachInventoryFallback = (NMServerSPZombieAssignmentFlow._diag.attachInventoryFallback or 0) + 1
    end
    return item
end

local function findInventoryProofItem(zombie, spec)
    return NMServerZombieAssignmentShared.findInventoryProofItem(zombie, spec)
end

local function dumpAvailableAttachmentLocations(zombie)
    local attachedItems = zombie and zombie.getAttachedItems and zombie:getAttachedItems() or nil
    local group = attachedItems and attachedItems.getGroup and attachedItems:getGroup() or nil
    if not (group and group.size and group.getLocationByIndex) then
        return "locations=nil"
    end
    local out = {}
    for i = 0, group:size() - 1 do
        local entry = group:getLocationByIndex(i)
        out[#out + 1] = tostring(entry and entry.getId and entry:getId() or "")
    end
    return table.concat(out, ",")
end

local function stampSelectionOutcome(zombie, spec, outcome)
    NMServerZombieAssignmentOutcomeShared.stampSelectionState(zombie, spec, {
        strategyName = STRATEGY_NAME,
        selection = outcome and outcome.selection or nil,
        status = outcome and outcome.status or "excluded",
        reason = outcome and outcome.reason or "selection_state",
        payload = outcome and outcome.payload or nil,
        lastAttemptTick = tonumber(NMServerSPZombieAssignmentFlow._diag and NMServerSPZombieAssignmentFlow._diag.ticks or 0) or 0
    })
end

local function stampFailedOutcome(zombie, outcome)
    NMServerZombieAssignmentOutcomeShared.stampFailedState(zombie, outcome and outcome.spec or nil, {
        strategyName = STRATEGY_NAME,
        selection = outcome and outcome.selection or nil,
        item = outcome and outcome.item or nil,
        reason = outcome and outcome.reason or "unknown",
        payload = outcome and outcome.payload or nil,
        lastAttemptTick = tonumber(NMServerSPZombieAssignmentFlow._diag and NMServerSPZombieAssignmentFlow._diag.ticks or 0) or 0
    })
end

local function stampAttachedOutcome(zombie, outcome)
    NMServerZombieAssignmentOutcomeShared.stampAttachedState(zombie, outcome and outcome.spec or nil, {
        strategyName = STRATEGY_NAME,
        selection = outcome and outcome.selection or nil,
        item = outcome and outcome.item or nil,
        payload = outcome and outcome.payload or nil,
        lastAttemptTick = tonumber(NMServerSPZombieAssignmentFlow._diag and NMServerSPZombieAssignmentFlow._diag.ticks or 0) or 0
    })
end

local function shouldAttemptAttach(zombie)
    local md = getModData(zombie)
    local status = tostring(md and md.status or "")
    local spec = getStampedVariantSpec(zombie)
    local mediaMode = tostring(md and md.mediaMode or "")
    if status == "attached" then
        local selected = md and md.selected == true
        local selectionSource = tostring(md and md.selectionSource or "")
        if selected and selectionSource == "server_ledger" and spec and findAttachedProofItem(zombie, spec) and mediaMode ~= "" then
            return false
        end
        return true
    end
    if status == "media_only" then
        return mediaMode == ""
    end
    if status == "excluded" or status == "suppressed" then
        if spec and (findAttachedProofItem(zombie, spec) or findInventoryProofItem(zombie, spec)) then
            return true
        end
        return false
    end
    if status ~= "failed" then
        return true
    end
    local currentTick = tonumber(NMServerSPZombieAssignmentFlow._diag and NMServerSPZombieAssignmentFlow._diag.ticks or 0) or 0
    local lastAttemptTick = tonumber(md and md.lastAttemptTick or 0) or 0
    return (currentTick - lastAttemptTick) >= FAILED_RETRY_TICKS
end

local function countZombiesWithModulo(zombies, startIndex, count, visitor)
    return NMServerZombieScanHelpers.countZombiesWithModulo(zombies, startIndex, count, visitor)
end

local function ensureZombieHasProofDevice(zombie)
    NMServerSPZombieAssignmentFlow._diag.attachAttempts = (NMServerSPZombieAssignmentFlow._diag.attachAttempts or 0) + 1
    -- Invariant: SP orchestrates scan/retry policy only; shared apply owns assignment mutation, payload finalization, and companion-case registration.
    local outcome = NMServerZombieAssignmentApplyShared.applyAssignment(zombie, {
        strategyName = STRATEGY_NAME,
        allowAttachedInventoryFallback = true,
        companionCaseSource = "sp_runtime_attach",
        companionCaseRuntimeLabel = "sp"
    })
    if outcome.status == "suppressed" then
        NMServerSPZombieAssignmentFlow._diag.attachSuppressed = (NMServerSPZombieAssignmentFlow._diag.attachSuppressed or 0) + 1
        stampSelectionOutcome(zombie, getSpecForVariantId(outcome.selection and outcome.selection.variantId or ""), outcome)
        return false
    end
    if outcome.status == "excluded" or outcome.status == "media_only" then
        NMServerSPZombieAssignmentFlow._diag.attachExcluded = (NMServerSPZombieAssignmentFlow._diag.attachExcluded or 0) + 1
        NMServerSPZombieAssignmentFlow._diag.attachExcludedScrubbed = (NMServerSPZombieAssignmentFlow._diag.attachExcludedScrubbed or 0) + (tonumber(outcome.removedCount) or 0)
        stampSelectionOutcome(zombie, getSpecForVariantId(outcome.selection and outcome.selection.variantId or ""), outcome)
        return false
    end

    if not outcome.ok and outcome.reason == "missing_proof_location" then
        NMServerSPZombieAssignmentFlow._diag.attachFailure = (NMServerSPZombieAssignmentFlow._diag.attachFailure or 0) + 1
        NMServerSPZombieAssignmentFlow._diag.locationFailures = (NMServerSPZombieAssignmentFlow._diag.locationFailures or 0) + 1
        stampFailedOutcome(zombie, {
            spec = outcome.spec,
            selection = outcome.selection,
            item = nil,
            reason = "missing_proof_location",
            payload = outcome.payload
        })
        logProof(
            "attach_failed",
            string.format(
                "zombie=%s reason=missing_proof_location location=%s model=%s locations=%s",
                zombieDebugId(zombie),
                tostring(outcome.spec and outcome.spec.attachmentLocation or ""),
                tostring(outcome.spec and outcome.spec.modelAttachmentName or ""),
                dumpAvailableAttachmentLocations(zombie)
            ),
            true
        )
        return false
    end

    if not outcome.ok then
        NMServerSPZombieAssignmentFlow._diag.attachFailure = (NMServerSPZombieAssignmentFlow._diag.attachFailure or 0) + 1
        stampFailedOutcome(zombie, outcome)
        logProof(
            "attach_failed",
            string.format(
                "zombie=%s reason=%s locations=%s",
                zombieDebugId(zombie),
                tostring(outcome.reason or "unknown"),
                dumpAvailableAttachmentLocations(zombie)
            ),
            true
        )
        return false
    end

    if outcome.usedInventoryFallback == true then
        NMServerSPZombieAssignmentFlow._diag.attachInventoryFallback = (NMServerSPZombieAssignmentFlow._diag.attachInventoryFallback or 0) + 1
    end
    NMServerSPZombieAssignmentFlow._diag.attachSuccess = (NMServerSPZombieAssignmentFlow._diag.attachSuccess or 0) + 1
    stampAttachedOutcome(zombie, outcome)
    return true
end

function NMServerSPZombieAssignmentFlow.onZombieUpdate(zombie)
    if not shouldRun() then
        return
    end
    if not isAliveZombie(zombie) then
        return
    end
    NMServerSPZombieAssignmentFlow._diag.updateCalls = (NMServerSPZombieAssignmentFlow._diag.updateCalls or 0) + 1
    if not shouldAttemptAttach(zombie) then
        return
    end

    ensureZombieHasProofDevice(zombie)
end

function NMServerSPZombieAssignmentFlow.onTick()
    if not shouldRun() then
        return
    end
    local diag = NMServerSPZombieAssignmentFlow._diag
    diag.ticks = (tonumber(diag.ticks) or 0) + 1
    if (diag.ticks % SCAN_INTERVAL_TICKS) == 0 then
        local players = collectCandidatePlayers()
        local cell = getCell and getCell() or nil
        local zombies = cell and cell.getZombieList and cell:getZombieList() or nil
        diag.scanCalls = (diag.scanCalls or 0) + 1
        if zombies and zombies.size and #players > 0 then
            local processed = 0
            local nearby = 0
            local candidates = 0
            local loadedSweepProcessed = 0
            local total = zombies:size()
            local startIndex = tonumber(diag.listCursor or 0) or 0
            countZombiesWithModulo(zombies, startIndex, total, function(zombie)
                if isAliveZombie(zombie) then
                    candidates = candidates + 1
                    if isZombieNearAnyPlayer(zombie, players) then
                        nearby = nearby + 1
                        if processed < SCAN_ZOMBIE_LIMIT and shouldAttemptAttach(zombie) then
                            processed = processed + 1
                            ensureZombieHasProofDevice(zombie)
                        end
                    end
                end
            end)
            diag.listCursor = total > 0 and ((startIndex + LOADED_SWEEP_LIMIT) % total) or 0
            if processed < SCAN_ZOMBIE_LIMIT and total > 0 then
                countZombiesWithModulo(zombies, startIndex, LOADED_SWEEP_LIMIT, function(zombie)
                    if processed >= SCAN_ZOMBIE_LIMIT then
                        return
                    end
                    if isAliveZombie(zombie) and shouldAttemptAttach(zombie) and not isZombieNearAnyPlayer(zombie, players) then
                        processed = processed + 1
                        loadedSweepProcessed = loadedSweepProcessed + 1
                        ensureZombieHasProofDevice(zombie)
                    end
                end)
            end
            diag.scanCandidates = (diag.scanCandidates or 0) + candidates
            diag.scanNearby = (diag.scanNearby or 0) + nearby
            diag.scanLoadedSweep = (diag.scanLoadedSweep or 0) + loadedSweepProcessed
            diag.lastReportedAttachSuccess = diag.attachSuccess or 0
            diag.lastReportedAttachFailure = diag.attachFailure or 0
            diag.lastReportedAttachFallback = diag.attachInventoryFallback or 0
        else
            diag.scanCandidates = (diag.scanCandidates or 0) + 0
        end
    end
    if (diag.ticks % HEARTBEAT_TICKS) ~= 0 then
        return
    end
end

return NMServerSPZombieAssignmentFlow
