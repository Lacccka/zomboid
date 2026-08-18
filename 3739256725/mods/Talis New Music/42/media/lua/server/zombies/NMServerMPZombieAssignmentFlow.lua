NMServerMPZombieAssignmentFlow = NMServerMPZombieAssignmentFlow or {}
require "zombies/NMZombieAudioVisualSupport"
require "zombies/NMServerZombieScanHelpers"
require "zombies/NMServerMPZombieIntakeQueue"
require "zombies/NMServerMPZombieScanPolicy"
require "zombies/NMServerMPZombieAssignmentEligibility"
require "zombies/NMServerMPZombieAssignmentExecutor"

local STRATEGY_NAME = "mp_assignment_flow"
local SCAN_RADIUS = 14
local MAX_ZOMBIES_PER_PLAYER = 32
local HEARTBEAT_TICKS = 900
local TICK_INTERVAL = 90
local FAILURE_COOLDOWN_TICKS = 900
local NATURAL_INTAKE_SCAN_INTERVAL = 10
local NATURAL_INTAKE_RADIUS = 24
local NATURAL_INTAKE_MAX_ZOMBIES_PER_PLAYER = 192
local NATURAL_INTAKE_PROCESS_LIMIT = 96
local SAMPLE_LIMIT = 3
local SAMPLE_RECHECK_DELAY_TICKS = 120

NMServerMPZombieAssignmentFlow._diag = NMServerMPZombieAssignmentFlow._diag or {
    ticks = 0,
    attachAttempts = 0,
    attachSuccess = 0,
    attachFailure = 0,
    attachExcluded = 0,
    attachExcludedScrubbed = 0,
    attachSuppressed = 0,
    supportApplied = 0,
    queueEnqueued = 0,
    queueProcessed = 0,
    queueScanned = 0,
    fallbackApplied = 0,
    strategyAssignments = 0,
    attachedListVisible = 0,
    attachedListMissing = 0,
    inventoryStillHasProof = 0,
    supportStillWorn = 0,
    sampleBudget = 0,
    pendingSamples = {},
    lastReportedAttachSuccess = 0,
    lastReportedAttachFailure = 0,
    lastReportedSupportApplied = 0,
    lastReportedStrategyAssignments = 0
}

local CONSTANTS = {
    STRATEGY_NAME = STRATEGY_NAME,
    SCAN_RADIUS = SCAN_RADIUS,
    MAX_ZOMBIES_PER_PLAYER = MAX_ZOMBIES_PER_PLAYER,
    HEARTBEAT_TICKS = HEARTBEAT_TICKS,
    TICK_INTERVAL = TICK_INTERVAL,
    FAILURE_COOLDOWN_TICKS = FAILURE_COOLDOWN_TICKS,
    NATURAL_INTAKE_SCAN_INTERVAL = NATURAL_INTAKE_SCAN_INTERVAL,
    NATURAL_INTAKE_RADIUS = NATURAL_INTAKE_RADIUS,
    NATURAL_INTAKE_MAX_ZOMBIES_PER_PLAYER = NATURAL_INTAKE_MAX_ZOMBIES_PER_PLAYER,
    NATURAL_INTAKE_PROCESS_LIMIT = NATURAL_INTAKE_PROCESS_LIMIT,
    SAMPLE_LIMIT = SAMPLE_LIMIT,
    SAMPLE_RECHECK_DELAY_TICKS = SAMPLE_RECHECK_DELAY_TICKS
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
        and NMZombieLiveStrategy.shouldRunMPAssignmentFlow
        and NMZombieLiveStrategy.shouldRunMPAssignmentFlow() == true
end

local function getModData(holder)
    return NMZombieAudioVisualSupport.getProofModData(holder)
end

local function buildEligibilityDeps(diag)
    return {
        isAliveZombie = NMServerZombieScanHelpers.isAliveZombie,
        getModData = getModData,
        nowTick = tonumber(diag and diag.ticks or 0) or 0,
        cooldownTicks = FAILURE_COOLDOWN_TICKS
    }
end

local function buildExecutorDeps(diag)
    return {
        diag = diag,
        flow = NMServerMPZombieAssignmentFlow,
        constants = CONSTANTS,
        getModData = getModData
    }
end

local function shouldQueueNaturalZombie(zombie, diag)
    return NMServerMPZombieAssignmentEligibility.shouldQueueNaturalZombie(zombie, buildEligibilityDeps(diag))
end

function NMServerMPZombieAssignmentFlow.onZombieUpdate(zombie)
    if not shouldRun() then
        return
    end
    local diag = NMServerMPZombieAssignmentFlow._diag
    NMServerMPZombieIntakeQueue.enqueue(
        NMServerMPZombieAssignmentFlow,
        zombie,
        "zombie_update",
        function(candidate)
            return shouldQueueNaturalZombie(candidate, diag)
        end,
        NMServerMPZombieIntakeQueue.getZombieQueueKey,
        diag
    )
end

function NMServerMPZombieAssignmentFlow.onTick(tickStep)
    if not shouldRun() then
        return
    end
    local diag = NMServerMPZombieAssignmentFlow._diag
    diag.ticks = (tonumber(diag.ticks) or 0) + math.max(1, tonumber(tickStep) or 1)

    local executorDeps = buildExecutorDeps(diag)
    NMServerMPZombieAssignmentExecutor.processPendingSamples(diag, executorDeps)

    local players = getOnlinePlayers and getOnlinePlayers() or nil
    local hasOnlinePlayers = players and players.size and players:size() > 0
    local fallbackPlayer = nil
    if not hasOnlinePlayers then
        fallbackPlayer = getPlayer and getPlayer() or nil
    end

    if NMServerMPZombieScanPolicy.shouldRunNaturalIntakeScan(diag.ticks, NATURAL_INTAKE_SCAN_INTERVAL) then
        NMServerMPZombieScanPolicy.observeNaturalCandidates(
            hasOnlinePlayers and players or nil,
            fallbackPlayer,
            function(zombie, source)
                NMServerMPZombieIntakeQueue.enqueue(
                    NMServerMPZombieAssignmentFlow,
                    zombie,
                    source,
                    function(candidate)
                        return shouldQueueNaturalZombie(candidate, diag)
                    end,
                    NMServerMPZombieIntakeQueue.getZombieQueueKey,
                    diag
                )
            end,
            diag,
            {
                source = "first_seen_scan",
                radius = NATURAL_INTAKE_RADIUS,
                maxZombies = NATURAL_INTAKE_MAX_ZOMBIES_PER_PLAYER
            }
        )
    end

    NMServerMPZombieIntakeQueue.drain(
        NMServerMPZombieAssignmentFlow,
        NATURAL_INTAKE_PROCESS_LIMIT,
        function(zombie)
            return shouldQueueNaturalZombie(zombie, diag)
        end,
        function(zombie, source)
            NMServerMPZombieAssignmentExecutor.applyAssignmentOutcome(zombie, source, executorDeps)
        end,
        diag
    )

    if not NMServerMPZombieScanPolicy.shouldRunFallbackScan(diag.ticks, TICK_INTERVAL) then
        return
    end

    diag.sampleBudget = SAMPLE_LIMIT
    local fallbackApplied = NMServerMPZombieScanPolicy.runFallback(
        hasOnlinePlayers and players or nil,
        fallbackPlayer,
        function(zombie, source)
            if shouldQueueNaturalZombie(zombie, diag) ~= true then
                return false
            end
            return NMServerMPZombieAssignmentExecutor.applyAssignmentOutcome(zombie, source, executorDeps)
        end,
        {
            source = "fallback_scan",
            radius = SCAN_RADIUS,
            maxZombies = MAX_ZOMBIES_PER_PLAYER
        }
    )
    diag.fallbackApplied = (diag.fallbackApplied or 0) + fallbackApplied

    diag.lastReportedAttachSuccess = diag.attachSuccess or 0
    diag.lastReportedAttachFailure = diag.attachFailure or 0
    diag.lastReportedSupportApplied = diag.supportApplied or 0
    diag.lastReportedStrategyAssignments = diag.strategyAssignments or 0

    if (diag.ticks % HEARTBEAT_TICKS) ~= 0 then
        return
    end
end

return NMServerMPZombieAssignmentFlow
