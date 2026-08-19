-- Bandits farming compatibility for B42.20 with failure isolation.

local Guard = require "LCC/Guard"
local FEATURE = "bandits.stomp-plant-action"

ZombieActions = ZombieActions or {}

local function onStart(zombie, task)
    return true
end

local function onWorking(zombie, task)
    zombie:faceLocationF(task.x, task.y)
    if zombie:getBumpType() ~= task.anim then return true end
    return false
end

local function onComplete(zombie, task)
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

local function runAction(phase, callback, zombie, task)
    if not Guard.isEnabled(FEATURE) then return true end
    local ok, result = Guard.protect(FEATURE, phase, callback, zombie, task)
    if not ok then return true end
    return result
end

ZombieActions.StompPlant = {}
ZombieActions.StompPlant.onStart = function(zombie, task)
    return runAction("onStart", onStart, zombie, task)
end
ZombieActions.StompPlant.onWorking = function(zombie, task)
    return runAction("onWorking", onWorking, zombie, task)
end
ZombieActions.StompPlant.onComplete = function(zombie, task)
    return runAction("onComplete", onComplete, zombie, task)
end
