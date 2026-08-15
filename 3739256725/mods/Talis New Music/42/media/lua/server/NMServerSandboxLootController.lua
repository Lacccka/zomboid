NMServerSandboxLootController = NMServerSandboxLootController or {}

require "loot/NMManagedSpawnCatalog"
require "loot/NMFallbackRepresentativeResolver"
require "loot/NMLootBuildContext"
require "loot/NMLootDiagnostics"
require "loot/NMLootRoutePlanner"
require "loot/NMServerLootProbe"
require "loot/NMVanillaCDLootConverter"

local controller = NMServerSandboxLootController
local formatRawSandboxLootSettings = NMLootDiagnostics.formatRawSandboxLootSettings
local lastLootProbeConfig = nil
local sandboxLootApplied = false
local sandboxLootApplying = false
local postDistributionHookRegistered = false
local fillContainerHookRegistered = false

local function nowMs()
    if getTimestampMs then
        local ms = tonumber(getTimestampMs())
        if ms then
            return ms
        end
    end
    if getTimestamp then
        local ts = tonumber(getTimestamp())
        if ts then
            return ts * 1000
        end
    end
    return 0
end

local function elapsedMs(startedAt)
    local startMs = tonumber(startedAt) or 0
    local delta = nowMs() - startMs
    if delta < 0 then
        return 0
    end
    return delta
end

local function isLootDebugEnabled()
    return NMCore and NMCore.isSubsystemDebugEnabled and NMCore.isSubsystemDebugEnabled("loot") == true
end

local function isLootProbeEnabled()
    return NMCore and NMCore.isSubsystemDebugEnabled and NMCore.isSubsystemDebugEnabled("loot_probe") == true
end

local function logLoot(tag, detail)
    NMLootDiagnostics.logSandboxLoot(isLootDebugEnabled(), tag, detail)
end

local function buildLootProbeConfig(buildContext)
    return {
        preset = "subsystem:loot_probe",
        rawRatesText = formatRawSandboxLootSettings(buildContext.rawSandboxLoot),
        managedLootMap = buildContext.mediaFootprint.managedLootMap
    }
end

local function areDistributionTablesReady()
    local hasProcedural = ProceduralDistributions and type(ProceduralDistributions.list) == "table"
    local hasSuburbs = type(SuburbsDistributions) == "table"
    return hasProcedural or hasSuburbs
end

local function applySandboxLootControl()
    if sandboxLootApplied or sandboxLootApplying then
        return sandboxLootApplied
    end
    sandboxLootApplying = true

    local totalStartedAt = nowMs()
    local distributionAuditEnabled = isLootDebugEnabled()
    local buildContext = NMLootBuildContext.create(distributionAuditEnabled)

    if distributionAuditEnabled then
        for i = 1, #(buildContext.childPackAuditEntries or {}) do
            logLoot("sandbox.loot child pack", buildContext.childPackAuditEntries[i])
        end
    end

    local phaseStartedAt = nowMs()
    local routeResult = NMLootRoutePlanner.applyBuildContext(buildContext, {
        distributionAuditEnabled = distributionAuditEnabled
    })
    local injectBudgetMs = elapsedMs(phaseStartedAt)

    if NMFallbackRepresentativeResolver and NMFallbackRepresentativeResolver.configure then
        NMFallbackRepresentativeResolver.configure(buildContext.fallbackMediaPool, buildContext.fallbackDevicePool)
    end
    if NMVanillaCDLootConverter and NMVanillaCDLootConverter.configure then
        NMVanillaCDLootConverter.configure()
    end
    lastLootProbeConfig = buildLootProbeConfig(buildContext)
    if NMServerLootProbe then
        if isLootProbeEnabled()
            and NMServerLootProbe.configure
        then
            NMServerLootProbe.configure(lastLootProbeConfig)
        elseif NMServerLootProbe.disable then
            NMServerLootProbe.disable()
        end
    end

    local vanillaCDConfig = NMVanillaCDLootConverter
        and NMVanillaCDLootConverter.getConfigurationSnapshot
        and NMVanillaCDLootConverter.getConfigurationSnapshot()
        or nil
    local distroPatchStats = NMServerDistroPatch
        and NMServerDistroPatch.getStats
        and NMServerDistroPatch.getStats()
        or nil

    NMLootDiagnostics.emitSandboxLootSummary({
        distributionAuditEnabled = distributionAuditEnabled,
        activeDebugPreset = "subsystem:loot",
        rawSandboxLoot = buildContext.rawSandboxLoot,
        lootBuildZomboidOST = buildContext.lootBuildZomboidOST,
        filteredBaseZomboidOST = buildContext.filteredBaseZomboidOST,
        mediaSpawnsWithCases = buildContext.mediaSpawnsWithCases,
        rewrites = buildContext.rewrites,
        compatibleChildMods = buildContext.compatibleChildMods,
        boundMediaCount = buildContext.boundMediaCount,
        vehicleTargetCount = #buildContext.vehicleTargets,
        vehicleRoleCounts = buildContext.vehicleRoleCounts,
        tableShape = buildContext.tableShape,
        mediaFootprint = buildContext.mediaFootprint,
        baseCategoryCounts = buildContext.baseCategoryCounts,
        basePresentCounts = buildContext.basePresentCounts,
        baseMissingCounts = buildContext.baseMissingCounts,
        childPoolCounts = buildContext.childPoolCounts,
        fallbackMediaCounts = routeResult.fallbackMediaCounts,
        fallbackDeviceCounts = routeResult.fallbackDeviceCounts,
        fallbackChildCounts = buildContext.fallbackChildCounts,
        fallbackMediaOwnership = routeResult.fallbackMediaOwnership,
        fallbackRepresentatives = routeResult.fallbackRepresentatives,
        injected = routeResult.injected,
        budgetDiagnostics = routeResult.budgetDiagnostics,
        backfill = routeResult.backfill,
        storeTopUp = routeResult.storeTopUp,
        standardRoute = routeResult.standardRoute,
        musicStoreCategoryWeights = routeResult.musicStoreCategoryWeights,
        musicStoreLaneWeights = routeResult.musicStoreLaneWeights,
        vanillaCDEnabled = NMVanillaCDLootConverter and NMVanillaCDLootConverter.isEnabled and NMVanillaCDLootConverter.isEnabled() == true,
        vanillaCDConfig = vanillaCDConfig,
        distroPatchStats = distroPatchStats,
        cdPlayerVariantCount = NMZombieDeviceVariantCatalog
            and NMZombieDeviceVariantCatalog.getVariantConfig
            and #(NMZombieDeviceVariantCatalog.getVariantConfig("cd_player").itemPool or {})
            or 0,
        bareMediaRepresentatives = buildContext.bareMediaRepresentatives,
        loadedRepresentatives = buildContext.overrides and buildContext.overrides.loadedRepresentatives or nil,
        skippedMods = buildContext.skippedMods,
        skippedFalsePositives = buildContext.skippedFalsePositives,
        baseTouched = buildContext.baseTouched,
        childTouched = buildContext.childTouched,
        elapsedMs = elapsedMs(totalStartedAt),
        allItemCount = buildContext.allItemCount,
        compatibleChildModsMs = buildContext.compatibleChildModsMs,
        overridesMs = buildContext.overridesMs,
        rewritesMs = buildContext.rewritesMs,
        vehicleTargetsMs = buildContext.vehicleTargetsMs,
        presenceIndexMs = buildContext.presenceIndexMs,
        managedPoolsMs = buildContext.managedPoolsMs,
        filterBaseOstMs = buildContext.filterBaseOstMs,
        baseSplitPresenceMs = buildContext.baseSplitPresenceMs,
        baseTouchMs = buildContext.baseTouchMs,
        childFanoutMs = buildContext.childFanoutMs,
        injectBudgetMs = injectBudgetMs,
        baseMediaCount = buildContext.baseMediaCount,
        baseDeviceCount = buildContext.baseDeviceCount,
        childPackCount = buildContext.childPackCount
    })

    sandboxLootApplied = true
    sandboxLootApplying = false
    return true
end

function controller.ensureInitialized()
    if not areDistributionTablesReady() then
        return false
    end
    if sandboxLootApplied then
        return true
    end
    if sandboxLootApplying then
        return false
    end
    return applySandboxLootControl() == true
end

local function isLiveFillContainer(container)
    return container and instanceof and instanceof(container, "ItemContainer") == true
end

function controller.onPostDistributionMerge()
    controller.ensureInitialized()
end

function controller.onFillContainer(roomName, containerType, container)
    if not isLiveFillContainer(container) then
        return
    end
    if NMFallbackRepresentativeResolver and NMFallbackRepresentativeResolver.replaceRepresentativesInContainer then
        NMFallbackRepresentativeResolver.replaceRepresentativesInContainer(container)
    end
    if isLootProbeEnabled()
        and NMServerLootProbe
    then
        if lastLootProbeConfig
            and NMServerLootProbe.isConfigured
            and NMServerLootProbe.isConfigured() ~= true
            and NMServerLootProbe.configure
        then
            NMServerLootProbe.configure(lastLootProbeConfig)
        end
    end
    if NMServerLootProbe
        and NMServerLootProbe.isFillObservationActive
        and NMServerLootProbe.isFillObservationActive()
        and NMServerLootProbe.onFillContainer
    then
        NMServerLootProbe.onFillContainer(roomName, containerType, container)
    end
end

function controller.registerEventHooks()
    if Events and Events.OnPostDistributionMerge and Events.OnPostDistributionMerge.Add and not postDistributionHookRegistered then
        Events.OnPostDistributionMerge.Add(controller.onPostDistributionMerge)
        postDistributionHookRegistered = true
    end
    if Events and Events.OnFillContainer and Events.OnFillContainer.Add and not fillContainerHookRegistered then
        Events.OnFillContainer.Add(controller.onFillContainer)
        fillContainerHookRegistered = true
    end
end

return controller
