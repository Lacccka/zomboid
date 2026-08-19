-- Publication-oriented Bandits farming guards for B42.20.
--
-- The original Bandits ZombieActions callbacks remain installed and are called
-- outside LCC's protected code. LCC only performs small prechecks for the B42
-- Farming states that previously caused repeated task failures.

local Guard = require "LCC/Guard"

local WATER = "bandits.water-farm-action"
local STOMP = "bandits.stomp-plant-action"

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

local function shouldSkipWaterStart(zombie, task)
    if not zombie or not task or not task.itemType then return true end
    local inventory = zombie:getInventory()
    if not inventory then return true end
    local item = inventory:getItemFromType(task.itemType)
    if not item or not instanceof(item, "DrainableComboItem") then return true end
    return not supportedWaterItems[item:getFullType()]
end

local function installSkipWrapper(feature, target, methodName, shouldSkip)
    local original = target[methodName]
    local marker = "__LCC_" .. feature:gsub("[^%w]", "_") .. "_" .. methodName
    if target[marker] then return end

    target[methodName] = function(...)
        if Guard.isEnabled(feature) then
            local ok, skip = Guard.protect(feature, methodName .. " precheck", shouldSkip, ...)
            if not ok or skip then
                return true
            end
        end

        -- Keep the real Bandits callback outside Guard.protect: upstream errors
        -- remain visible instead of being silently swallowed by the patch.
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
        installSkipWrapper(WATER, ZombieActions.WaterFarm, "onStart", shouldSkipWaterStart)
        installSkipWrapper(WATER, ZombieActions.WaterFarm, "onComplete", farmingUnavailable)
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
