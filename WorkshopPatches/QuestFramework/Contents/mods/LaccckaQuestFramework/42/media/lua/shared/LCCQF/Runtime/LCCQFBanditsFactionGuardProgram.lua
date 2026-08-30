-- Minimal Bandits2 program for autonomous LCCQF faction guards.
-- It deliberately avoids player ownership, looting, sabotage and arbitrary roaming.
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

local function homeFor(brain)
    if type(brain) ~= "table" then return nil end
    local x = finite(brain.lccqHomeX)
    local y = finite(brain.lccqHomeY)
    local z = finite(brain.lccqHomeZ)
    if x == nil or y == nil or z == nil then return nil end
    return {
        x = x,
        y = y,
        z = z,
        homeRadius = math.max(2, finite(brain.lccqHomeRadius) or 12),
        returnRadius = math.max(4, finite(brain.lccqReturnRadius) or 24),
        guardRadius = math.max(4, finite(brain.lccqGuardRadius) or 18),
    }
end

ZombiePrograms.LCCQFFactionGuard.Prepare = function(bandit)
    Bandit.ForceStationary(bandit, false)
    Bandit.SetSleeping(bandit, false)
    return { status = true, next = "Main", tasks = {} }
end

ZombiePrograms.LCCQFFactionGuard.Main = function(bandit)
    local tasks = {}
    local brain = BanditBrain.Get(bandit)
    local home = homeFor(brain)
    if not home then
        local idle = BanditPrograms.Idle(bandit)
        for _, task in pairs(idle or {}) do tasks[#tasks + 1] = task end
        return { status = true, next = "Main", tasks = tasks }
    end

    local bx, by, bz = bandit:getX(), bandit:getY(), bandit:getZ()
    local homeDistance = BanditUtils.DistTo(bx, by, home.x, home.y)

    -- Home leash wins over combat pursuit. The NPC may fight around the site, but it
    -- must not turn into a roaming Bandit/Looter after leaving the building.
    if math.abs(bz - home.z) >= 0.5 or homeDistance > home.returnRadius then
        Bandit.SetSleeping(bandit, false)
        Bandit.ForceStationary(bandit, false)
        tasks[#tasks + 1] = BanditUtils.GetMoveTask(0, home.x, home.y, home.z, "Run", homeDistance, false)
        return { status = true, next = "Main", tasks = tasks }
    end

    local config = { mustSee = true, hearDist = home.guardRadius }
    local target, enemy = BanditUtils.GetTarget(bandit, config)
    if type(target) == "table" and target.x and target.y and target.z
        and tonumber(target.dist) and tonumber(target.dist) <= home.guardRadius
    then
        local walkType = Bandit.GetCombatWalktype(bandit, enemy, target.dist)
        tasks[#tasks + 1] = BanditUtils.GetMoveTaskTarget(
            0,
            target.x,
            target.y,
            target.z,
            target.id,
            target.player,
            walkType,
            target.dist
        )
        return { status = true, next = "Main", tasks = tasks }
    end

    if homeDistance > home.homeRadius then
        tasks[#tasks + 1] = BanditUtils.GetMoveTask(0, home.x, home.y, home.z, "Walk", homeDistance, false)
        return { status = true, next = "Main", tasks = tasks }
    end

    local idle = BanditPrograms.Idle(bandit)
    for _, task in pairs(idle or {}) do tasks[#tasks + 1] = task end
    return { status = true, next = "Main", tasks = tasks }
end

return ZombiePrograms.LCCQFFactionGuard
