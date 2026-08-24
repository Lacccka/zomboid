-- LCC B42.20.3 server-authoritative corpse clothing repair for Bandits.
--
-- Exact 42.20.3 Java lifecycle:
--   IsoZombie.DoZombieInventory()
--   Events.OnZombieDead
--   IsoZombie.DoDeath() -> new IsoDeadBody(died)
--
-- IsoDeadBody then takes died.getInventory() as its container and copies
-- died.getWornItems(). During corpse save every worn entry is serialized as an
-- index into that container. Therefore the durable invariant is:
--
--   the SAME InventoryItem object must be in zombie inventory AND WornItems
--   before IsoDeadBody is constructed.
--
-- We enforce that invariant just-in-time on the dedicated server. This avoids
-- modifying live server inventory throughout the NPC lifetime and also covers
-- persistent Bandits that never pass through Bandit.ApplyVisuals after restart.
if not isServer() then return end

local MARKER = "server-authoritative-death-worn-v2"
LCC_BANDITS_SERVER_CLOTHING_RESTORE = MARKER

if type(BanditCompatibility) ~= "table" or type(BanditCompatibility.InstanceItem) ~= "function" then
    print("[LCC][BanditsServerClothing][DISABLED] BanditCompatibility.InstanceItem unavailable")
    return
end
if type(GetBanditClusterData) ~= "function" then
    print("[LCC][BanditsServerClothing][DISABLED] GetBanditClusterData unavailable")
    return
end

local warned = {}
local stats = {
    deathsSeen = 0,
    banditDeathsMatched = 0,
    deathRepairs = 0,
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

-- Export counters for optional NPCCombatExperimental diagnostics. The stable
-- patch itself stays quiet and never emits periodic SUMMARY heartbeat lines.
LCC_BANDITS_SERVER_CLOTHING_DIAGNOSTICS = {
    marker = MARKER,
    stats = stats,
}

local function warnOnce(key, message)
    if warned[key] then return end
    warned[key] = true
    print(message)
end

local function fullType(item)
    if not item then return nil end
    local ok, value = pcall(function() return item:getFullType() end)
    return ok and value and tostring(value) or nil
end

local function typedBodyLocation(item)
    if not item then return nil end
    local ok, location = pcall(function() return item:getBodyLocation() end)
    return ok and location or nil
end

local function wornSize(character)
    if not character then return -1 end
    local ok, worn = pcall(function() return character:getWornItems() end)
    if not ok or not worn then return -1 end
    local okSize, size = pcall(function() return worn:size() end)
    return okSize and tonumber(size) or -1
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

local function characterId(character)
    if not character then return nil end
    local ok, value = pcall(function() return character:getPersistentOutfitID() end)
    if ok and value ~= nil then return value end
    return nil
end

local function resolveBrain(zombie)
    local id = characterId(zombie)
    if id == nil then return nil, nil end

    local okCluster, cluster = pcall(GetBanditClusterData, id)
    if not okCluster or type(cluster) ~= "table" then return id, nil end

    local brain = cluster[id]
    if brain == nil then brain = cluster[tostring(id)] end
    if type(brain) ~= "table" then return id, nil end
    if brain.id ~= nil and tostring(brain.id) ~= tostring(id) then return id, nil end
    return id, brain
end

local function applyTint(item, brain, brainLocation)
    if not item or not brain or type(brain.tint) ~= "table" then return end
    local packed = brain.tint[brainLocation]
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

local function findReusableInventoryItem(character, inventory, id, brainLocation, itemType)
    local items, size = inventoryItems(inventory)
    if not items or size < 0 then return nil end

    local wantedType = tostring(itemType)
    local wantedLocation = tostring(brainLocation)
    local fallback = nil

    for i = 0, size - 1 do
        local okItem, item = pcall(function() return items:get(i) end)
        if okItem and item and fullType(item) == wantedType and not isWorn(character, item) then
            local okMd, md = pcall(function() return item:getModData() end)
            if okMd and md
                    and md.LCC_BanditsServerClothing == MARKER
                    and tostring(md.LCC_BanditsBrainId or "") == tostring(id)
                    and tostring(md.LCC_BanditsBrainLocation or "") == wantedLocation then
                return item
            end
            if fallback == nil then fallback = item end
        end
    end
    return fallback
end

local function ensureSlot(zombie, id, brain, brainLocation, itemType, localStats)
    if not brainLocation or not itemType then return end
    localStats.expected = localStats.expected + 1
    stats.expected = stats.expected + 1

    local probe = BanditCompatibility.InstanceItem(itemType)
    if not probe then
        localStats.errors = localStats.errors + 1
        stats.errors = stats.errors + 1
        warnOnce("instance:" .. tostring(itemType), string.format(
            "[LCC][BanditsServerClothing][INSTANCE_FAILED] id=%s item=%s",
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
        warnOnce("inventory", "[LCC][BanditsServerClothing][ERROR] zombie inventory unavailable")
        return
    end

    local okCurrent, current = pcall(function() return zombie:getWornItem(location) end)
    if not okCurrent then
        localStats.errors = localStats.errors + 1
        stats.errors = stats.errors + 1
        warnOnce("get-worn", "[LCC][BanditsServerClothing][ERROR] getWornItem(ItemBodyLocation) failed")
        return
    end

    if current then
        if fullType(current) ~= tostring(itemType) then
            localStats.conflicts = localStats.conflicts + 1
            stats.conflicts = stats.conflicts + 1
            print(string.format(
                "[LCC][BanditsServerClothing][SLOT_CONFLICT] marker=%s id=%s brainLocation=%s expected=%s actual=%s intervention=false",
                MARKER, tostring(id), tostring(brainLocation), tostring(itemType), tostring(fullType(current) or "<unknown>")
            ))
            return
        end

        markItem(current, id, brainLocation)
        applyTint(current, brain, brainLocation)
        local inInventory, attemptedAdd = ensureInInventory(inventory, current)
        if attemptedAdd then
            localStats.inventoryAdds = localStats.inventoryAdds + 1
            stats.inventoryAdds = stats.inventoryAdds + 1
        end
        if not inInventory then
            localStats.errors = localStats.errors + 1
            stats.errors = stats.errors + 1
            warnOnce("worn-not-in-inventory:" .. tostring(itemType), string.format(
                "[LCC][BanditsServerClothing][INVARIANT_ERROR] id=%s worn item=%s is not inventory-backed",
                tostring(id), tostring(itemType)
            ))
            return
        end
        localStats.alreadyWorn = localStats.alreadyWorn + 1
        stats.alreadyWorn = stats.alreadyWorn + 1
        return
    end

    local item = findReusableInventoryItem(zombie, inventory, id, brainLocation, itemType)
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
            warnOnce("add:" .. tostring(itemType), string.format(
                "[LCC][BanditsServerClothing][INVENTORY_ADD_FAILED] id=%s item=%s",
                tostring(id), tostring(itemType)
            ))
            return
        end
        localStats.created = localStats.created + 1
        stats.created = stats.created + 1
    end

    markItem(item, id, brainLocation)
    applyTint(item, brain, brainLocation)

    local okSet = pcall(function() zombie:setWornItem(location, item) end)
    if not okSet then
        localStats.errors = localStats.errors + 1
        stats.errors = stats.errors + 1
        warnOnce("set-worn", "[LCC][BanditsServerClothing][ERROR] setWornItem(ItemBodyLocation, item) failed")
        return
    end

    local okVerify, verify = pcall(function() return zombie:getWornItem(location) end)
    local okContainer, container = pcall(function() return item:getContainer() end)
    if not okVerify or verify ~= item or not okContainer or container ~= inventory then
        localStats.errors = localStats.errors + 1
        stats.errors = stats.errors + 1
        warnOnce("verify:" .. tostring(itemType), string.format(
            "[LCC][BanditsServerClothing][INVARIANT_ERROR] id=%s item=%s sameObject=false",
            tostring(id), tostring(itemType)
        ))
        return
    end

    localStats.restored = localStats.restored + 1
    stats.restored = stats.restored + 1
end

local function repairBeforeCorpse(zombie, id, brain)
    if not zombie or not brain or type(brain.clothing) ~= "table" then return end

    local md = zombie:getModData()
    if md and md.LCC_BanditsServerDeathRepair == MARKER then return end

    local localStats = {
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

    local beforeWorn = wornSize(zombie)
    local beforeInventory = inventoryCount(zombie)
    local processed = {}

    if type(BanditCompatibility.GetBodyLocationsOrdered) == "function" then
        for _, brainLocation in pairs(BanditCompatibility.GetBodyLocationsOrdered()) do
            local itemType = brain.clothing[brainLocation]
            if itemType then
                processed[brainLocation] = true
                ensureSlot(zombie, id, brain, brainLocation, itemType, localStats)
            end
        end
    end
    for brainLocation, itemType in pairs(brain.clothing) do
        if not processed[brainLocation] then
            ensureSlot(zombie, id, brain, brainLocation, itemType, localStats)
        end
    end

    if md then
        md.LCC_BanditsServerDeathRepair = MARKER
        md.LCC_BanditsBrainId = id
    end

    stats.deathRepairs = stats.deathRepairs + 1
    local afterWorn = wornSize(zombie)
    local afterInventory = inventoryCount(zombie)

    print(string.format(
        "[LCC][BanditsServerClothing][DEATH_REPAIR] marker=%s id=%s fullname=%s expected=%d wearableExpected=%d beforeWorn=%d afterWorn=%d beforeInventory=%d afterInventory=%d restored=%d created=%d reusedInventory=%d inventoryAdds=%d alreadyWorn=%d noLocation=%d conflicts=%d errors=%d invariant=inventory+same-worn-object",
        MARKER,
        tostring(id),
        tostring(brain.fullname or "<unknown>"):gsub("%s+", "_"),
        localStats.expected,
        localStats.wearableExpected,
        beforeWorn,
        afterWorn,
        beforeInventory,
        afterInventory,
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

    local id, brain = resolveBrain(zombie)
    if not brain then return end
    stats.banditDeathsMatched = stats.banditDeathsMatched + 1

    local ok, err = pcall(repairBeforeCorpse, zombie, id, brain)
    if not ok then
        stats.errors = stats.errors + 1
        print(string.format(
            "[LCC][BanditsServerClothing][DEATH_REPAIR_ERROR] marker=%s id=%s error=%s",
            MARKER, tostring(id), tostring(err)
        ))
    end
end

Events.OnZombieDead.Add(onZombieDead)

print(string.format(
    "[LCC][BanditsServerClothing][BOOT] marker=%s authority=dedicated-server boundary=OnZombieDead timing=after-DoZombieInventory-before-IsoDeadBody invariant=inventory+same-worn-object liveInventoryMutation=false",
    MARKER
))