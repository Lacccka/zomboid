-- This is a editED version from xixi's function to apply capacity overwrite when equip

local MFSBagCapacityPatch = {}

local WRAPPERS_KEY = "__mfs_bag_capacity_wrappers_v5"

-- Only add backpack that has capacity > 49
-- MFSBagCapacityPatch.fullTypes = {
--     ["Base.BagS_cat_14"] = true,
--     ["Base.BagS_cat_19"] = true,
--     ["Base.BagS_cat_21"] = true,
--     ["Base.BagS_cat_22"] = true,
--     ["Base.BagS_cat_23"] = true,
--     ["Base.BagS_cat_24"] = true,
--     ["Base.BagS_cat_25"] = true,
-- }

-- MFSBagCapacityPatch.shortTypes = {
--     ["BagS_cat_14"] = true,
--     ["BagS_cat_19"] = true,
--     ["BagS_cat_21"] = true,
--     ["BagS_cat_22"] = true,
--     ["BagS_cat_23"] = true,
--     ["BagS_cat_24"] = true,
--     ["BagS_cat_25"] = true,
-- }

MFSBagCapacityPatch.capacities = {
    ["Base.BagS_cat_14"] = 56,
    ["Base.BagS_cat_19"] = 65,
    ["Base.BagS_cat_21"] = 52,
    ["Base.BagS_cat_22"] = 52,
    ["Base.BagS_cat_23"] = 54,
    ["Base.BagS_cat_24"] = 55,
    ["Base.BagS_cat_25"] = 52,
}

-- Build a short-type lookup automatically from the full-type capacity table.
MFSBagCapacityPatch.shortCapacities = {}

local function rebuildShortCapacityTable()
    MFSBagCapacityPatch.shortCapacities = {}

    for fullType, capacity in pairs(MFSBagCapacityPatch.capacities) do
        local shortType = string.match(fullType, "%.([^%.]+)$")
        if shortType then
            MFSBagCapacityPatch.shortCapacities[shortType] = capacity
        end
    end
end

rebuildShortCapacityTable()

function MFSBagCapacityPatch.resolveAdjustedCapacity(player, capacity)
    if player and player:hasTrait(CharacterTrait.ORGANIZED) then
        return math.ceil(capacity * 1.3)
    end

    if player and player:hasTrait(CharacterTrait.DISORGANIZED) then
        return math.ceil(capacity * 0.7)
    end

    return capacity
end

function MFSBagCapacityPatch.getTargetCapacity(container)
    if not container then
        return nil
    end

    -- Preferred lookup: containing item's full type.
    if container.getContainingItem then
        local item = container:getContainingItem()
        if item and item.getFullType then
            local capacity = MFSBagCapacityPatch.capacities[item:getFullType()]
            if capacity then
                return capacity
            end
        end
    end

    -- Fallback lookup: container short type.
    if container.getType then
        return MFSBagCapacityPatch.shortCapacities[container:getType()]
    end

    return nil
end

function MFSBagCapacityPatch.isTargetContainer(container)
    return MFSBagCapacityPatch.getTargetCapacity(container) ~= nil
end

function MFSBagCapacityPatch.normalizeHasRoomForArgs(arg1, arg2)
    if arg2 ~= nil then
        return arg1, arg2
    end

    if type(arg1) == "number" then
        return nil, arg1
    end

    if arg1 and instanceof(arg1, "InventoryItem") then
        return nil, arg1
    end

    return arg1, arg2
end

function MFSBagCapacityPatch.wrapGetCapacity(original)
    return function(self)
        local capacity = MFSBagCapacityPatch.getTargetCapacity(self)
        if capacity then
            return capacity
        end

        return original(self)
    end
end

function MFSBagCapacityPatch.wrapGetEffectiveCapacity(original)
    return function(self, player)
        local capacity = MFSBagCapacityPatch.getTargetCapacity(self)
        if capacity then
            return MFSBagCapacityPatch.resolveAdjustedCapacity(player, capacity)
        end

        return original(self, player)
    end
end

function MFSBagCapacityPatch.wrapHasRoomFor(original)
    return function(self, arg1, arg2)
        local capacity = MFSBagCapacityPatch.getTargetCapacity(self)

        if capacity then
            local player, item = MFSBagCapacityPatch.normalizeHasRoomForArgs(arg1, arg2)
            local currentWeight = self:getContentsWeight()
            local effectiveCapacity = self:getEffectiveCapacity(player)

            if type(item) == "number" then
                return currentWeight + item <= effectiveCapacity
            end

            if item and instanceof(item, "InventoryItem") then
                return currentWeight + item:getUnequippedWeight() <= effectiveCapacity
            end
        end

        if arg2 == nil then
            return original(self, arg1)
        end

        return original(self, arg1, arg2)
    end
end

function MFSBagCapacityPatch.getItemContainerIndex()
    return __classmetatables
        and __classmetatables[ItemContainer.class]
        and __classmetatables[ItemContainer.class].__index
end

function MFSBagCapacityPatch.install()
    local index = MFSBagCapacityPatch.getItemContainerIndex()
    if not index then
        return false
    end

    local wrappers = rawget(index, WRAPPERS_KEY)

    if wrappers
        and index["getCapacity"] == wrappers.getCapacity
        and index["getEffectiveCapacity"] == wrappers.getEffectiveCapacity
        and index["hasRoomFor"] == wrappers.hasRoomFor then
        return true
    end

    wrappers = {
        sourceGetCapacity = index["getCapacity"],
        sourceGetEffectiveCapacity = index["getEffectiveCapacity"],
        sourceHasRoomFor = index["hasRoomFor"],
    }

    if wrappers.sourceGetCapacity then
        wrappers.getCapacity = MFSBagCapacityPatch.wrapGetCapacity(wrappers.sourceGetCapacity)
        index["getCapacity"] = wrappers.getCapacity
    end

    if wrappers.sourceGetEffectiveCapacity then
        wrappers.getEffectiveCapacity = MFSBagCapacityPatch.wrapGetEffectiveCapacity(wrappers.sourceGetEffectiveCapacity)
        index["getEffectiveCapacity"] = wrappers.getEffectiveCapacity
    end

    if wrappers.sourceHasRoomFor then
        wrappers.hasRoomFor = MFSBagCapacityPatch.wrapHasRoomFor(wrappers.sourceHasRoomFor)
        index["hasRoomFor"] = wrappers.hasRoomFor
    end

    rawset(index, WRAPPERS_KEY, wrappers)
    return true
end

function MFSBagCapacityPatch.applyScriptCapacity()
    if not ScriptManager or not ScriptManager.instance then
        return
    end

    rebuildShortCapacityTable()

    for fullType, capacity in pairs(MFSBagCapacityPatch.capacities) do
        local item = ScriptManager.instance:FindItem(fullType)
        if item then
            item:DoParam("Capacity = " .. tostring(capacity))
        end
    end
end

local function MFS_bag_bootstrap()
    MFSBagCapacityPatch.applyScriptCapacity()
    MFSBagCapacityPatch.install()
end

MFS_bag_bootstrap()

if Events.OnInitGlobalModData then
    Events.OnInitGlobalModData.Add(MFSBagCapacityPatch.applyScriptCapacity)
end

if Events.OnGameStart then
    Events.OnGameStart.Add(MFS_bag_bootstrap)
end

if Events.OnServerStarted then
    Events.OnServerStarted.Add(MFS_bag_bootstrap)
end

if Events.OnCreatePlayer then
    Events.OnCreatePlayer.Add(MFS_bag_bootstrap)
end

if Events.OnClothingUpdated then
    Events.OnClothingUpdated.Add(MFS_bag_bootstrap)
end