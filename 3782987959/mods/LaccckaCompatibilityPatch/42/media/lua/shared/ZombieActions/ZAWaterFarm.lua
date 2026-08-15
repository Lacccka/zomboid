-- Resolve the vanilla client-only farming global when the action runs.
ZombieActions = ZombieActions or {}

local function getFakeItem(itemType)
    local fakeItemType
    if itemType == "farming.WateredCanFull"
            or itemType == "farming.WateredCan"
            or itemType == "Base.WateredCan" then
        fakeItemType = "Bandits.WateringCan"
    elseif itemType == "Base.BucketWaterFull"
            or itemType == "Base.BucketEmpty"
            or itemType == "Base.Bucket" then
        fakeItemType = "Bandits.Bucket"
    end
    if not fakeItemType then return nil end
    return BanditCompatibility.InstanceItem(fakeItemType)
end

ZombieActions.WaterFarm = {}

ZombieActions.WaterFarm.onStart = function(zombie, task)
    local item = zombie:getInventory():getItemFromType(task.itemType)
    if not instanceof(item, "DrainableComboItem") then return end
    zombie:setPrimaryHandItem(getFakeItem(item:getFullType()))
    zombie:playSound("WaterCrops")
    return true
end

ZombieActions.WaterFarm.onWorking = function(zombie, task)
    zombie:faceLocationF(task.x, task.y)
    if zombie:getBumpType() ~= task.anim then return true end
    return false
end

ZombieActions.WaterFarm.onComplete = function(zombie, task)
    zombie:setPrimaryHandItem(nil)
    local item = zombie:getInventory():getItemFromType(task.itemType)
    if not instanceof(item, "DrainableComboItem") then return end

    local square = zombie:getCell():getGridSquare(task.x, task.y, task.z)
    if not square then return true end

    local farming = CFarmingSystem and CFarmingSystem.instance
    if not farming then return true end

    local plant = farming:getLuaObjectAt(task.x, task.y, task.z)
    if not plant then return true end

    local waterToPour = plant.waterNeeded - plant.waterLvl
    local waterAvailable = math.floor((item:getUsedDelta() / item:getUseDelta()) + 0.5) * 4
    if waterAvailable < waterToPour then waterToPour = waterAvailable end
    local waterLeft = waterAvailable - waterToPour

    if BanditUtils.IsController(zombie) then
        farming:sendCommand(getSpecificPlayer(0), "water", {
            x = task.x, y = task.y, z = task.z, uses = waterToPour,
        })
    end
    item:setUsedDelta(math.min(1, waterLeft * item:getUseDelta() / 4))
    return true
end

