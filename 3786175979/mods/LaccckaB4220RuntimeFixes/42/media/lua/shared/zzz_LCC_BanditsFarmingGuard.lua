-- Source-clean Bandits farming compatibility guards for B42.20.
--
-- The original ZombieActions callbacks remain installed. LCC only prechecks
-- transient/unsupported B42 states and otherwise calls the installed callback
-- unchanged and outside Guard.protect, so upstream failures stay visible.

local Guard = require "LCC/Guard"

local WATER = "bandits.water-farm-action"
local STOMP = "bandits.stomp-plant-action"

Guard.safeRequire(WATER, "ZombieActions/ZAWaterFarm")
Guard.safeRequire(STOMP, "ZombieActions/ZAStompPlant")

local supportedWaterItems = {
    ["farming.WateredCanFull"] = true,
    ["farming.WateredCan"] = true,
    ["Base.WateredCan"] = true,
    ["Base.BucketWaterFull"] = true,
    ["Base.BucketEmpty"] = true,
    ["Base.Bucket"] = true,
}

local function farmingUnavailable()
    return not CFarmingSystem or not CFarmingSystem.instance
end

local function waterItemUnavailable(zombie, task)
    if not zombie or not task or not task.itemType then return true end
    local inventory = zombie:getInventory()
    if not inventory then return true end
    local item = inventory:getItemFromType(task.itemType)
    if not item or not instanceof(item, "DrainableComboItem") then return true end
    return not supportedWaterItems[item:getFullType()]
end

local function shouldSkipWaterComplete(zombie, task)
    if waterItemUnavailable(zombie, task) then return true end
    return farmingUnavailable()
end

local function installSkipWrapper(feature, target, methodName, shouldSkip)
    local original = target[methodName]
    local marker = "__LCC_" .. feature:gsub("[^%w]", "_") .. "_" .. methodName
    if target[marker] then return end

    target[methodName] = function(...)
        if Guard.isEnabled(feature) then
            local ok, skip = Guard.protect(feature, methodName .. " precheck", shouldSkip, ...)
            if ok and skip then
                -- Returning true follows Bandits' own task callback contract:
                -- onStart advances the task; onComplete lets it be removed.
                return true
            end
            -- If our precheck itself fails, Guard disables only this LCC feature
            -- and we deliberately fall through to the installed callback.
        end

        return original(...)
    end

    target[marker] = true
end

Guard.install {
    id = WATER,
    validate = function()
        return type(ZombieActions) == "table"
            and type(ZombieActions.WaterFarm) == "table"
            and type(ZombieActions.WaterFarm.onStart) == "function"
            and type(ZombieActions.WaterFarm.onComplete) == "function",
            "Bandits WaterFarm callbacks are unavailable"
    end,
    install = function()
        installSkipWrapper(WATER, ZombieActions.WaterFarm, "onStart", waterItemUnavailable)
        installSkipWrapper(WATER, ZombieActions.WaterFarm, "onComplete", shouldSkipWaterComplete)
    end,
}

Guard.install {
    id = STOMP,
    validate = function()
        return type(ZombieActions) == "table"
            and type(ZombieActions.StompPlant) == "table"
            and type(ZombieActions.StompPlant.onComplete) == "function",
            "Bandits StompPlant callback is unavailable"
    end,
    install = function()
        installSkipWrapper(STOMP, ZombieActions.StompPlant, "onComplete", farmingUnavailable)
    end,
}
