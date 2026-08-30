-- Bandits2 physical program for autonomous LCCQF faction residents.
-- Server-owned assignments select guard/command/work/rest duty while the program keeps
-- every resident site-bound and avoids player ownership, looting and sabotage.
require "Bandit"
require "BanditBrain"
require "BanditPrograms"
require "BanditUtils"

ZombiePrograms = ZombiePrograms or {}
ZombiePrograms.LCCQFFactionGuard = ZombiePrograms.LCCQFFactionGuard or {}

local function finite(value)
    local n = tonumber(value)
    if n == nil or n ~= n or n == math.huge or n == -math.huge then return nil end
    return n
end

local function pointFromBrain(brain, prefix)
    if type(brain) ~= "table" then return nil end
    local x = finite(brain[prefix .. "X"])
    local y = finite(brain[prefix .. "Y"])
    local z = finite(brain[prefix .. "Z"])
    if x == nil or y == nil or z == nil then return nil end
    return { x = x, y = y, z = z }
end

local function homeFor(brain)
    local point = pointFromBrain(brain, "lccqHome")
    if not point then return nil end
    point.homeRadius = math.max(2, finite(brain.lccqHomeRadius) or 12)
    point.returnRadius = math.max(4, finite(brain.lccqReturnRadius) or 24)
    point.guardRadius = math.max(4, finite(brain.lccqGuardRadius) or 18)
    return point
end

local function idleTasks(bandit)
    local tasks = {}
    local idle = BanditPrograms.Idle(bandit)
    for _, task in pairs(idle or {}) do tasks[#tasks + 1] = task end
    return tasks
end

local function moveTo(tasks, bandit, point, walkType, tolerance)
    if not point then return false end
    local distance = BanditUtils.DistTo(bandit:getX(), bandit:getY(), point.x, point.y)
    if math.abs(bandit:getZ() - point.z) < 0.5 and distance <= (tolerance or 1.5) then return false end
    tasks[#tasks + 1] = BanditUtils.GetMoveTask(0, point.x, point.y, point.z, walkType or "Walk", distance, false)
    return true
end

local function acquireThreat(bandit, guardRadius)
    local config = { mustSee = true, hearDist = guardRadius }
    local target, enemy = BanditUtils.GetTarget(bandit, config)
    if type(target) ~= "table" or not target.x or not target.y or not target.z
        or not tonumber(target.dist) or tonumber(target.dist) > guardRadius
    then
        return nil, nil
    end
    return target, enemy
end

ZombiePrograms.LCCQFFactionGuard.Prepare = function(bandit)
    Bandit.ForceStationary(bandit, false)
    Bandit.SetSleeping(bandit, false)
    return { status = true, next = "Main", tasks = {} }
end

ZombiePrograms.LCCQFFactionGuard.Main = function(bandit)
    local brain = BanditBrain.Get(bandit)
    local home = homeFor(brain)
    if not home then return { status = true, next = "Main", tasks = idleTasks(bandit) } end

    local tasks = {}
    local bx, by, bz = bandit:getX(), bandit:getY(), bandit:getZ()
    local homeDistance = BanditUtils.DistTo(bx, by, home.x, home.y)

    -- The home leash is stronger than any assigned duty or combat target.
    if math.abs(bz - home.z) >= 0.5 or homeDistance > home.returnRadius then
        Bandit.SetSleeping(bandit, false)
        Bandit.ForceStationary(bandit, false)
        tasks[#tasks + 1] = BanditUtils.GetMoveTask(0, home.x, home.y, home.z, "Run", homeDistance, false)
        return { status = true, next = "Main", tasks = tasks }
    end

    local dutyMode = tostring(brain and brain.lccqDutyMode or "guard")
    local dutyTarget = pointFromBrain(brain, "lccqDuty") or home

    -- Rest and work are deliberately non-combat proactive states. Work currently means
    -- presence at a server-selected work point; inventory production is a later system.
    if dutyMode == "rest" or dutyMode == "work" then
        Bandit.SetSleeping(bandit, false)
        if moveTo(tasks, bandit, dutyTarget, "Walk", dutyMode == "rest" and 1.5 or 1.25) then
            return { status = true, next = "Main", tasks = tasks }
        end
        return { status = true, next = "Main", tasks = idleTasks(bandit) }
    end

    -- Guards and command staff may react to threats, but only inside the site's guard
    -- envelope. Command staff otherwise remain near their assigned home/command point.
    local target, enemy = acquireThreat(bandit, home.guardRadius)
    if target then
        local walkType = Bandit.GetCombatWalktype(bandit, enemy, target.dist)
        tasks[#tasks + 1] = BanditUtils.GetMoveTaskTarget(
            0, target.x, target.y, target.z, target.id, target.player, walkType, target.dist
        )
        return { status = true, next = "Main", tasks = tasks }
    end

    if dutyMode == "command" then
        if moveTo(tasks, bandit, dutyTarget, "Walk", 2.0) then
            return { status = true, next = "Main", tasks = tasks }
        end
        return { status = true, next = "Main", tasks = idleTasks(bandit) }
    end

    if homeDistance > home.homeRadius then
        tasks[#tasks + 1] = BanditUtils.GetMoveTask(0, home.x, home.y, home.z, "Walk", homeDistance, false)
        return { status = true, next = "Main", tasks = tasks }
    end

    return { status = true, next = "Main", tasks = idleTasks(bandit) }
end

return ZombiePrograms.LCCQFFactionGuard
