-- LCC B42.20.3 server-authoritative clothing repair for Bandits.
--
-- Decompiled IsoDeadBody proves that corpse construction copies the dying
-- character's WornItems, and corpse serialization stores each worn item as an
-- index into the corpse ItemContainer. Therefore a durable fix must restore the
-- same InventoryItem on the dedicated server, keep it in the Bandit's inventory,
-- and wear that exact object before death.
if not isServer() then return end

local MARKER = "server-authoritative-worn-v1"
local CLIENT_MARKER = "real-worn-reconnect-v2"
LCC_BANDITS_SERVER_CLOTHING_RESTORE = MARKER

if type(Bandit) ~= "table" or type(Bandit.ApplyVisuals) ~= "function" then
    print("[LCC][BanditsServerClothing][DISABLED] Bandit.ApplyVisuals unavailable")
    return
end
if type(BanditCompatibility) ~= "table" or type(BanditCompatibility.InstanceItem) ~= "function" then
    print("[LCC][BanditsServerClothing][DISABLED] BanditCompatibility.InstanceItem unavailable")
    return
end

local originalApplyVisuals = Bandit.ApplyVisuals
local warned = {}
local stats = {
    applyCalls = 0,
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

local function fullType(item)
    if not item then return nil end
    local ok, value = pcall(function() return item:getFullType() end)
    return ok and value and tostring(value) or nil
end

local function characterId(character, brain)
    if brain and brain.id ~= nil then return tostring(brain.id) end
    if character and BanditUtils and type(BanditUtils.GetCharacterID) == "function" then
        local ok, value = pcall(BanditUtils.GetCharacterID, character)
        if ok and value ~= nil then return tostring(value) end
    end
    return "nil"
end

local function typedBodyLocation(item)
    if not item then return nil end
    local ok, location = pcall(function() return item:getBodyLocation() end)
    return ok and location or nil
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

local function markItem(item, brainLocation)
    if not item then return end
    pcall(function()
        local md = item:getModData()
        if md then
            md.LCC_BanditsServerClothing = MARKER
            md.LCC_BanditsRealClothing = CLIENT_MARKER
            md.LCC_BanditsBrainLocation = tostring(brainLocation)
            md.preserve = true
        end
    end)
end

local function inventoryItems(inventory)
    if not inventory then return nil, -1 end
    local ok, items = pcall(function() return inventory:getItems() end)
    if not ok or not items then return nil, -1 end
    local okSize, size = pcall(function() return items:size() end)
    return items, okSize and tonumber(size) or -1
end

local function findReusableInventoryItem(inventory, brainLocation, itemType)
    local items, size = inventoryItems(inventory)
    if not items or size < 0 then return nil end
    local wantedLocation = tostring(brainLocation)
    local wantedType = tostring(itemType)

    for i = 0, size - 1 do
        local okItem, item = pcall(function() return items:get(i) end)
        if okItem and item and fullType(item) == wantedType then
            local okMd, md = pcall(function() return item:getModData() end)
            if okMd and md
                    and md.LCC_BanditsServerClothing == MARKER
                    and tostring(md.LCC_BanditsBrainLocation or "") == wantedLocation then
                return item
            end
        end
    end
    return nil
end

local function ensureInInventory(inventory, item)
    if not inventory or not item then return false, false end

    local okContainer, container = pcall(function() return item:getContainer() end)
    if okContainer and container == inventory then return true, false end
    if okContainer and container ~= nil and container ~= inventory then
        return false, false
    end

    local okAdd, added = pcall(function() return inventory:AddItem(item) end)
    if not okAdd then return false, false end
    -- AddItem(InventoryItem) may return the same item or nil depending on binding;
    -- membership is authoritative, not the return value.
    local okAfter, after = pcall(function() return item:getContainer() end)
    return okAfter and after == inventory, true
end

local function ensureSlot(bandit, brain, brainLocation, itemType)
    if not bandit or not brainLocation or not itemType then return end

    local inventory = bandit:getInventory()
    if not inventory then
        stats.errors = stats.errors + 1
        warnOnce("inventory", "[LCC][BanditsServerClothing][ERROR] Bandit inventory unavailable")
        return
    end

    local probe = BanditCompatibility.InstanceItem(itemType)
    if not probe then
        stats.errors = stats.errors + 1
        warnOnce("instance:" .. tostring(itemType), string.format(
            "[LCC][BanditsServerClothing][INSTANCE_FAILED] id=%s item=%s",
            characterId(bandit, brain), tostring(itemType)
        ))
        return
    end

    local location = typedBodyLocation(probe)
    if not location then
        stats.noLocation = stats.noLocation + 1
        return
    end

    local okCurrent, current = pcall(function() return bandit:getWornItem(location) end)
    if not okCurrent then
        stats.errors = stats.errors + 1
        warnOnce("get-worn", "[LCC][BanditsServerClothing][ERROR] getWornItem(ItemBodyLocation) failed")
        return
    end

    if current then
        if fullType(current) ~= tostring(itemType) then
            stats.conflicts = stats.conflicts + 1
            print(string.format(
                "[LCC][BanditsServerClothing][SLOT_CONFLICT] id=%s brainLocation=%s expected=%s actual=%s intervention=false",
                characterId(bandit, brain), tostring(brainLocation), tostring(itemType), tostring(fullType(current) or "<unknown>")
            ))
            return
        end

        markItem(current, brainLocation)
        applyTint(current, brain, brainLocation)
        local inInventory, attemptedAdd = ensureInInventory(inventory, current)
        if attemptedAdd then stats.inventoryAdds = stats.inventoryAdds + 1 end
        if not inInventory then
            stats.errors = stats.errors + 1
            warnOnce("current-not-in-inventory", "[LCC][BanditsServerClothing][ERROR] existing worn item is not inventory-backed")
            return
        end
        stats.alreadyWorn = stats.alreadyWorn + 1
        return
    end

    local item = findReusableInventoryItem(inventory, brainLocation, itemType)
    if item then
        stats.reusedInventory = stats.reusedInventory + 1
    else
        item = probe
        markItem(item, brainLocation)
        applyTint(item, brain, brainLocation)
        local inInventory, attemptedAdd = ensureInInventory(inventory, item)
        if attemptedAdd then stats.inventoryAdds = stats.inventoryAdds + 1 end
        if not inInventory then
            stats.errors = stats.errors + 1
            warnOnce("add:" .. tostring(itemType), string.format(
                "[LCC][BanditsServerClothing][INVENTORY_ADD_FAILED] id=%s item=%s",
                characterId(bandit, brain), tostring(itemType)
            ))
            return
        end
        stats.created = stats.created + 1
    end

    markItem(item, brainLocation)
    applyTint(item, brain, brainLocation)

    local okSet = pcall(function() bandit:setWornItem(location, item) end)
    if not okSet then
        stats.errors = stats.errors + 1
        warnOnce("set-worn", "[LCC][BanditsServerClothing][ERROR] setWornItem(ItemBodyLocation, item) failed")
        return
    end
    stats.restored = stats.restored + 1
end

local function restoreAuthoritativeWorn(bandit, brain)
    if not bandit or not brain or type(brain.clothing) ~= "table" then return end
    if not bandit:isAlive() then return end

    local before = bandit:getWornItems() and bandit:getWornItems():size() or -1
    local restoredBefore = stats.restored
    local createdBefore = stats.created
    local reusedBefore = stats.reusedInventory
    local addedBefore = stats.inventoryAdds

    if BanditCompatibility.GetBodyLocationsOrdered then
        for _, brainLocation in pairs(BanditCompatibility.GetBodyLocationsOrdered()) do
            local itemType = brain.clothing[brainLocation]
            if itemType then ensureSlot(bandit, brain, brainLocation, itemType) end
        end
    else
        for brainLocation, itemType in pairs(brain.clothing) do
            ensureSlot(bandit, brain, brainLocation, itemType)
        end
    end

    -- Catch any valid custom locations absent from the compatibility ordering.
    for brainLocation, itemType in pairs(brain.clothing) do
        local probe = BanditCompatibility.InstanceItem(itemType)
        local location = typedBodyLocation(probe)
        if location then
            local ok, current = pcall(function() return bandit:getWornItem(location) end)
            if ok and not current then ensureSlot(bandit, brain, brainLocation, itemType) end
        end
    end

    local after = bandit:getWornItems() and bandit:getWornItems():size() or -1
    local inventory = bandit:getInventory()
    local _, inventoryCount = inventoryItems(inventory)
    print(string.format(
        "[LCC][BanditsServerClothing][RESTORE] marker=%s id=%s beforeWorn=%d afterWorn=%d restored=%d created=%d reusedInventory=%d inventoryAdds=%d inventoryItems=%d",
        MARKER,
        characterId(bandit, brain),
        before,
        after,
        stats.restored - restoredBefore,
        stats.created - createdBefore,
        stats.reusedInventory - reusedBefore,
        stats.inventoryAdds - addedBefore,
        inventoryCount
    ))
end

Bandit.ApplyVisuals = function(bandit, brain)
    originalApplyVisuals(bandit, brain)
    stats.applyCalls = stats.applyCalls + 1
    local ok, err = pcall(restoreAuthoritativeWorn, bandit, brain)
    if not ok then
        stats.errors = stats.errors + 1
        warnOnce("runtime", "[LCC][BanditsServerClothing][RESTORE_ERROR] " .. tostring(err))
    end
end

Events.EveryOneMinute.Add(function()
    print(string.format(
        "[LCC][BanditsServerClothing][SUMMARY] marker=%s applyCalls=%d restored=%d created=%d reusedInventory=%d inventoryAdds=%d alreadyWorn=%d noLocation=%d conflicts=%d errors=%d",
        MARKER,
        stats.applyCalls,
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
    "[LCC][BanditsServerClothing][BOOT] marker=%s authority=dedicated-server inventoryBacked=true corpseSource=died.WornItems",
    MARKER
))
