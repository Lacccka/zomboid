-- LCC fallback for the Bandits server death-clothing race on B42.20.3.
--
-- Client BanditUpdate sends Commands/BanditRemove as soon as a Bandit dies.
-- BanditServer.Commands.BanditRemove deletes the cluster brain immediately. If
-- that command reaches the server before the server's own OnZombieDead callback,
-- server-authoritative-death-worn-v2 can no longer resolve brain.clothing.
--
-- v2 also handles the inverse ordering. When primary OnZombieDead wins the race,
-- BanditRemove may arrive later; v1 would capture a snapshot that could never be
-- consumed. We remember the already-handled id briefly so that late BanditRemove
-- skips snapshot creation, and prune any stale race state after two minute ticks.
-- No live InventoryItem/WornItems mutation is performed before death.
if not isServer() then return end

local MARKER = "server-death-worn-remove-snapshot-v2"
local PRIMARY_MARKER = "server-authoritative-death-worn-v2"
LCC_BANDITS_SERVER_CLOTHING_REMOVE_SNAPSHOT = MARKER

if type(BanditCompatibility) ~= "table" or type(BanditCompatibility.InstanceItem) ~= "function" then
    print("[LCC][BanditsServerClothingFallback][DISABLED] BanditCompatibility.InstanceItem unavailable")
    return
end
if type(GetBanditClusterData) ~= "function" then
    print("[LCC][BanditsServerClothingFallback][DISABLED] GetBanditClusterData unavailable")
    return
end
if type(BanditServer) ~= "table" or type(BanditServer.Commands) ~= "table"
        or type(BanditServer.Commands.BanditRemove) ~= "function" then
    print("[LCC][BanditsServerClothingFallback][DISABLED] BanditServer.Commands.BanditRemove unavailable")
    return
end

local snapshots = {}
local handledIds = {}
local epoch = 0
local warned = {}
local stats = {
    removeCalls = 0,
    removeAfterPrimary = 0,
    snapshotsCaptured = 0,
    snapshotMissesAtRemove = 0,
    snapshotsPruned = 0,
    handledPruned = 0,
    deathsSeen = 0,
    primaryAlreadyHandled = 0,
    fallbackMatches = 0,
    fallbackRepairs = 0,
    expected = 0,
    wearableExpected = 0,
    restored = 0,
    created = 0,
    reusedInventory = 0,
    inventoryAdds = 0,
    alreadyWorn = 0,
    noLocation = 0,
    conflicts = 0,
    errors = 0,
}

local function warnOnce(key, message)
    if warned[key] then return end
    warned[key] = true
    print(message)
end

local function copyScalars(source)
    local result = {}
    if type(source) ~= "table" then return result end
    for key, value in pairs(source) do
        if type(value) ~= "table" then result[key] = value end
    end
    return result
end

local function clusterBrain(id)
    if id == nil then return nil end
    local ok, cluster = pcall(GetBanditClusterData, id)
    if not ok or type(cluster) ~= "table" then return nil end
    local brain = cluster[id]
    if brain == nil then brain = cluster[tostring(id)] end
    return type(brain) == "table" and brain or nil
end

local function captureBrain(id, brain)
    if id == nil or type(brain) ~= "table" or type(brain.clothing) ~= "table" then return false end
    snapshots[tostring(id)] = {
        id = brain.id ~= nil and brain.id or id,
        fullname = brain.fullname,
        clothing = copyScalars(brain.clothing),
        tint = copyScalars(brain.tint),
        _epoch = epoch,
    }
    stats.snapshotsCaptured = stats.snapshotsCaptured + 1
    return true
end

local originalBanditRemove = BanditServer.Commands.BanditRemove
BanditServer.Commands.BanditRemove = function(player, args)
    stats.removeCalls = stats.removeCalls + 1
    local id = args and args.id or nil
    if id ~= nil then
        local key = tostring(id)
        if handledIds[key] ~= nil then
            -- Primary OnZombieDead already repaired this Bandit. This remove is
            -- the late half of the opposite race ordering; no snapshot is useful.
            stats.removeAfterPrimary = stats.removeAfterPrimary + 1
            handledIds[key] = nil
            snapshots[key] = nil
        else
            local brain = clusterBrain(id)
            if not captureBrain(id, brain) then
                stats.snapshotMissesAtRemove = stats.snapshotMissesAtRemove + 1
            end
        end
    end
    return originalBanditRemove(player, args)
end

local function fullType(item)
    if not item then return nil end
    local ok, value = pcall(function() return item:getFullType() end)
    return ok and value and tostring(value) or nil
end

local function typedBodyLocation(item)
    if not item then return nil end
    local ok, value = pcall(function() return item:getBodyLocation() end)
    return ok and value or nil
end

local function inventoryItems(inventory)
    if not inventory then return nil, -1 end
    local ok, items = pcall(function() return inventory:getItems() end)
    if not ok or not items then return nil, -1 end
    local okSize, size = pcall(function() return items:size() end)
    return items, okSize and tonumber(size) or -1
end

local function inventoryCount(character)
    if not character then return -1 end
    local ok, inventory = pcall(function() return character:getInventory() end)
    if not ok or not inventory then return -1 end
    local _, size = inventoryItems(inventory)
    return size
end

local function wornSize(character)
    if not character then return -1 end
    local ok, worn = pcall(function() return character:getWornItems() end)
    if not ok or not worn then return -1 end
    local okSize, size = pcall(function() return worn:size() end)
    return okSize and tonumber(size) or -1
end

local function characterId(character)
    if not character then return nil end
    local ok, value = pcall(function() return character:getPersistentOutfitID() end)
    return ok and value ~= nil and value or nil
end

local function isWorn(character, item)
    if not character or not item then return false end
    local ok, worn = pcall(function() return character:getWornItems() end)
    if not ok or not worn then return false end
    local okContains, result = pcall(function() return worn:contains(item) end)
    return okContains and result == true
end

local function ensureInInventory(inventory, item)
    if not inventory or not item then return false, false end
    local okContainer, container = pcall(function() return item:getContainer() end)
    if okContainer and container == inventory then return true, false end
    if okContainer and container ~= nil and container ~= inventory then return false, false end

    local okAdd = pcall(function() inventory:AddItem(item) end)
    if not okAdd then return false, false end
    local okAfter, after = pcall(function() return item:getContainer() end)
    return okAfter and after == inventory, true
end

local function applyTint(item, snapshot, brainLocation)
    if not item or not snapshot or type(snapshot.tint) ~= "table" then return end
    local packed = snapshot.tint[brainLocation]
    if packed == nil or not BanditUtils or type(BanditUtils.dec2rgb) ~= "function" then return end
    pcall(function()
        local visual = item:getVisual()
        if not visual then return end
        local color = BanditUtils.dec2rgb(packed)
        visual:setTint(ImmutableColor.new(color.r, color.g, color.b, 1))
    end)
end

local function markItem(item, id, brainLocation)
    if not item then return end
    pcall(function()
        local md = item:getModData()
        if not md then return end
        md.LCC_BanditsServerClothing = MARKER
        md.LCC_BanditsBrainId = id
        md.LCC_BanditsBrainLocation = tostring(brainLocation)
        md.preserve = true
    end)
end

local function reusableInventoryItem(zombie, inventory, itemType)
    local items, size = inventoryItems(inventory)
    if not items or size < 0 then return nil end
    for i = 0, size - 1 do
        local ok, item = pcall(function() return items:get(i) end)
        if ok and item and fullType(item) == tostring(itemType) and not isWorn(zombie, item) then
            return item
        end
    end
    return nil
end

local function ensureSlot(zombie, id, snapshot, brainLocation, itemType, localStats)
    localStats.expected = localStats.expected + 1
    stats.expected = stats.expected + 1

    local probe = BanditCompatibility.InstanceItem(itemType)
    if not probe then
        localStats.errors = localStats.errors + 1
        stats.errors = stats.errors + 1
        warnOnce("instance:" .. tostring(itemType), string.format(
            "[LCC][BanditsServerClothingFallback][INSTANCE_FAILED] id=%s item=%s",
            tostring(id), tostring(itemType)
        ))
        return
    end

    local location = typedBodyLocation(probe)
    if not location then
        localStats.noLocation = localStats.noLocation + 1
        stats.noLocation = stats.noLocation + 1
        return
    end
    localStats.wearableExpected = localStats.wearableExpected + 1
    stats.wearableExpected = stats.wearableExpected + 1

    local inventory = zombie:getInventory()
    if not inventory then
        localStats.errors = localStats.errors + 1
        stats.errors = stats.errors + 1
        return
    end

    local okCurrent, current = pcall(function() return zombie:getWornItem(location) end)
    if not okCurrent then
        localStats.errors = localStats.errors + 1
        stats.errors = stats.errors + 1
        return
    end

    if current then
        if fullType(current) ~= tostring(itemType) then
            localStats.conflicts = localStats.conflicts + 1
            stats.conflicts = stats.conflicts + 1
            return
        end
        markItem(current, id, brainLocation)
        applyTint(current, snapshot, brainLocation)
        local inInventory, attemptedAdd = ensureInInventory(inventory, current)
        if attemptedAdd then
            localStats.inventoryAdds = localStats.inventoryAdds + 1
            stats.inventoryAdds = stats.inventoryAdds + 1
        end
        if not inInventory then
            localStats.errors = localStats.errors + 1
            stats.errors = stats.errors + 1
            return
        end
        localStats.alreadyWorn = localStats.alreadyWorn + 1
        stats.alreadyWorn = stats.alreadyWorn + 1
        return
    end

    local item = reusableInventoryItem(zombie, inventory, itemType)
    if item then
        localStats.reusedInventory = localStats.reusedInventory + 1
        stats.reusedInventory = stats.reusedInventory + 1
    else
        item = probe
        local inInventory, attemptedAdd = ensureInInventory(inventory, item)
        if attemptedAdd then
            localStats.inventoryAdds = localStats.inventoryAdds + 1
            stats.inventoryAdds = stats.inventoryAdds + 1
        end
        if not inInventory then
            localStats.errors = localStats.errors + 1
            stats.errors = stats.errors + 1
            return
        end
        localStats.created = localStats.created + 1
        stats.created = stats.created + 1
    end

    markItem(item, id, brainLocation)
    applyTint(item, snapshot, brainLocation)
    local okSet = pcall(function() zombie:setWornItem(location, item) end)
    if not okSet then
        localStats.errors = localStats.errors + 1
        stats.errors = stats.errors + 1
        return
    end

    local okVerify, verify = pcall(function() return zombie:getWornItem(location) end)
    local okContainer, container = pcall(function() return item:getContainer() end)
    if not okVerify or verify ~= item or not okContainer or container ~= inventory then
        localStats.errors = localStats.errors + 1
        stats.errors = stats.errors + 1
        return
    end

    localStats.restored = localStats.restored + 1
    stats.restored = stats.restored + 1
end

local function repairFromSnapshot(zombie, id, snapshot)
    local localStats = {
        expected = 0, wearableExpected = 0, restored = 0, created = 0,
        reusedInventory = 0, inventoryAdds = 0, alreadyWorn = 0,
        noLocation = 0, conflicts = 0, errors = 0,
    }

    local beforeWorn = wornSize(zombie)
    local beforeInventory = inventoryCount(zombie)
    local processed = {}

    if type(BanditCompatibility.GetBodyLocationsOrdered) == "function" then
        for _, brainLocation in pairs(BanditCompatibility.GetBodyLocationsOrdered()) do
            local itemType = snapshot.clothing[brainLocation]
            if itemType then
                processed[brainLocation] = true
                ensureSlot(zombie, id, snapshot, brainLocation, itemType, localStats)
            end
        end
    end
    for brainLocation, itemType in pairs(snapshot.clothing) do
        if not processed[brainLocation] then
            ensureSlot(zombie, id, snapshot, brainLocation, itemType, localStats)
        end
    end

    local md = zombie:getModData()
    if md then
        md.LCC_BanditsServerDeathRepair = MARKER
        md.LCC_BanditsBrainId = id
    end

    stats.fallbackRepairs = stats.fallbackRepairs + 1
    print(string.format(
        "[LCC][BanditsServerClothingFallback][DEATH_REPAIR] marker=%s id=%s fullname=%s expected=%d wearableExpected=%d beforeWorn=%d afterWorn=%d beforeInventory=%d afterInventory=%d restored=%d created=%d reusedInventory=%d inventoryAdds=%d alreadyWorn=%d noLocation=%d conflicts=%d errors=%d source=BanditRemoveSnapshot invariant=inventory+same-worn-object",
        MARKER,
        tostring(id),
        tostring(snapshot.fullname or "<unknown>"):gsub("%s+", "_"),
        localStats.expected,
        localStats.wearableExpected,
        beforeWorn,
        wornSize(zombie),
        beforeInventory,
        inventoryCount(zombie),
        localStats.restored,
        localStats.created,
        localStats.reusedInventory,
        localStats.inventoryAdds,
        localStats.alreadyWorn,
        localStats.noLocation,
        localStats.conflicts,
        localStats.errors
    ))
end

local function onZombieDead(zombie)
    stats.deathsSeen = stats.deathsSeen + 1
    if not zombie then return end
    local id = characterId(zombie)
    if id == nil then return end

    local key = tostring(id)
    local md = zombie:getModData()

    -- Primary callback is registered before this fallback. Remember the id so a
    -- later BanditRemove does not create a snapshot after the death already won.
    if md and md.LCC_BanditsServerDeathRepair == PRIMARY_MARKER then
        stats.primaryAlreadyHandled = stats.primaryAlreadyHandled + 1
        handledIds[key] = epoch
        snapshots[key] = nil
        return
    end

    local snapshot = snapshots[key]
    if not snapshot then return end

    stats.fallbackMatches = stats.fallbackMatches + 1
    local ok, err = pcall(repairFromSnapshot, zombie, id, snapshot)
    snapshots[key] = nil
    if not ok then
        stats.errors = stats.errors + 1
        print(string.format(
            "[LCC][BanditsServerClothingFallback][DEATH_REPAIR_ERROR] marker=%s id=%s error=%s",
            MARKER, tostring(id), tostring(err)
        ))
    end
end

Events.OnZombieDead.Add(onZombieDead)

local function countEntries(t)
    local count = 0
    for _ in pairs(t) do count = count + 1 end
    return count
end

Events.EveryOneMinute.Add(function()
    epoch = epoch + 1

    for key, snapshot in pairs(snapshots) do
        if type(snapshot) ~= "table" or epoch - tonumber(snapshot._epoch or epoch) > 2 then
            snapshots[key] = nil
            stats.snapshotsPruned = stats.snapshotsPruned + 1
        end
    end
    for key, handledEpoch in pairs(handledIds) do
        if epoch - tonumber(handledEpoch or epoch) > 2 then
            handledIds[key] = nil
            stats.handledPruned = stats.handledPruned + 1
        end
    end

    print(string.format(
        "[LCC][BanditsServerClothingFallback][SUMMARY] marker=%s removeCalls=%d removeAfterPrimary=%d snapshotsCaptured=%d snapshotMissesAtRemove=%d activeSnapshots=%d activeHandled=%d snapshotsPruned=%d handledPruned=%d deathsSeen=%d primaryAlreadyHandled=%d fallbackMatches=%d fallbackRepairs=%d expected=%d wearableExpected=%d restored=%d created=%d reusedInventory=%d inventoryAdds=%d alreadyWorn=%d noLocation=%d conflicts=%d errors=%d",
        MARKER,
        stats.removeCalls,
        stats.removeAfterPrimary,
        stats.snapshotsCaptured,
        stats.snapshotMissesAtRemove,
        countEntries(snapshots),
        countEntries(handledIds),
        stats.snapshotsPruned,
        stats.handledPruned,
        stats.deathsSeen,
        stats.primaryAlreadyHandled,
        stats.fallbackMatches,
        stats.fallbackRepairs,
        stats.expected,
        stats.wearableExpected,
        stats.restored,
        stats.created,
        stats.reusedInventory,
        stats.inventoryAdds,
        stats.alreadyWorn,
        stats.noLocation,
        stats.conflicts,
        stats.errors
    ))
end)

print(string.format(
    "[LCC][BanditsServerClothingFallback][BOOT] marker=%s source=BanditRemove preDeleteSnapshot=true primary=%s lateRemoveSkip=true staleRetentionMinutes=2 liveInventoryMutation=false",
    MARKER, PRIMARY_MARKER
))
