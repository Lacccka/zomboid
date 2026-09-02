-- Optional diagnostics for stable LCC Bandits fixes.
--
-- Stable NPCFixes keeps production behavior and counters but does not emit
-- periodic heartbeat summaries. Enabling NPCCombatExperimental restores those
-- summaries here, keeping diagnostic noise out of normal server runs.
if not isServer() then return end

local function countEntries(t)
    if type(t) ~= "table" then return 0 end
    local count = 0
    for _ in pairs(t) do count = count + 1 end
    return count
end

local function printPrimarySummary()
    local diagnostics = LCC_BANDITS_SERVER_CLOTHING_DIAGNOSTICS
    if type(diagnostics) ~= "table" or type(diagnostics.stats) ~= "table" then return end

    local stats = diagnostics.stats
    print(string.format(
        "[LCC][BanditsServerClothing][SUMMARY] marker=%s deathsSeen=%d banditDeathsMatched=%d deathRepairs=%d expected=%d wearableExpected=%d restored=%d created=%d reusedInventory=%d inventoryAdds=%d alreadyWorn=%d noLocation=%d conflicts=%d errors=%d source=NPCCombatExperimental",
        tostring(diagnostics.marker or "<unknown>"),
        tonumber(stats.deathsSeen or 0),
        tonumber(stats.banditDeathsMatched or 0),
        tonumber(stats.deathRepairs or 0),
        tonumber(stats.expected or 0),
        tonumber(stats.wearableExpected or 0),
        tonumber(stats.restored or 0),
        tonumber(stats.created or 0),
        tonumber(stats.reusedInventory or 0),
        tonumber(stats.inventoryAdds or 0),
        tonumber(stats.alreadyWorn or 0),
        tonumber(stats.noLocation or 0),
        tonumber(stats.conflicts or 0),
        tonumber(stats.errors or 0)
    ))
end

local function printFallbackSummary()
    local diagnostics = LCC_BANDITS_SERVER_CLOTHING_FALLBACK_DIAGNOSTICS
    if type(diagnostics) ~= "table" or type(diagnostics.stats) ~= "table" then return end

    local stats = diagnostics.stats
    print(string.format(
        "[LCC][BanditsServerClothingFallback][SUMMARY] marker=%s removeCalls=%d removeAfterPrimary=%d snapshotsCaptured=%d snapshotMissesAtRemove=%d activeSnapshots=%d activeHandled=%d snapshotsPruned=%d handledPruned=%d deathsSeen=%d primaryAlreadyHandled=%d fallbackMatches=%d fallbackRepairs=%d expected=%d wearableExpected=%d restored=%d created=%d reusedInventory=%d inventoryAdds=%d alreadyWorn=%d noLocation=%d conflicts=%d errors=%d source=NPCCombatExperimental",
        tostring(diagnostics.marker or "<unknown>"),
        tonumber(stats.removeCalls or 0),
        tonumber(stats.removeAfterPrimary or 0),
        tonumber(stats.snapshotsCaptured or 0),
        tonumber(stats.snapshotMissesAtRemove or 0),
        countEntries(diagnostics.snapshots),
        countEntries(diagnostics.handledIds),
        tonumber(stats.snapshotsPruned or 0),
        tonumber(stats.handledPruned or 0),
        tonumber(stats.deathsSeen or 0),
        tonumber(stats.primaryAlreadyHandled or 0),
        tonumber(stats.fallbackMatches or 0),
        tonumber(stats.fallbackRepairs or 0),
        tonumber(stats.expected or 0),
        tonumber(stats.wearableExpected or 0),
        tonumber(stats.restored or 0),
        tonumber(stats.created or 0),
        tonumber(stats.reusedInventory or 0),
        tonumber(stats.inventoryAdds or 0),
        tonumber(stats.alreadyWorn or 0),
        tonumber(stats.noLocation or 0),
        tonumber(stats.conflicts or 0),
        tonumber(stats.errors or 0)
    ))
end

Events.EveryOneMinute.Add(function()
    printPrimarySummary()
    printFallbackSummary()
end)

print("[LCC][BanditsStableDiagnostics][BOOT] periodicStableSummaries=true source=NPCCombatExperimental")
