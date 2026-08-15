-- Resolve the vanilla client-only farming global when the action runs.
ZombieActions = ZombieActions or {}
ZombieActions.StompPlant = {}

ZombieActions.StompPlant.onStart = function(zombie, task)
    return true
end

ZombieActions.StompPlant.onWorking = function(zombie, task)
    zombie:faceLocationF(task.x, task.y)
    if zombie:getBumpType() ~= task.anim then return true end
    return false
end

ZombieActions.StompPlant.onComplete = function(zombie, task)
    local square = zombie:getCell():getGridSquare(task.x, task.y, task.z)
    if not square then return true end

    local farming = CFarmingSystem and CFarmingSystem.instance
    if not farming then return true end

    local plant = farming:getLuaObjectAt(task.x, task.y, task.z)
    if not plant then return true end

    if BanditUtils.IsController(zombie) and ZombRand(4) == 0 then
        farming:sendCommand(getSpecificPlayer(0), "destroy", {
            x = task.x, y = task.y, z = task.z,
        })
    end
    return true
end

