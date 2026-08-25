-- Alyssa companion program.
-- Priority: follow player → fight enemies near player → idle when right next to player.

ZombiePrograms = ZombiePrograms or {}
ZombiePrograms.ModpackCompanion = ZombiePrograms.ModpackCompanion or {}

-- Only engage enemies within this distance of the PLAYER (not sister).
local ENGAGE_PLAYER_RADIUS    = 8
local ENGAGE_PLAYER_RADIUS_SQ = ENGAGE_PLAYER_RADIUS * ENGAGE_PLAYER_RADIUS
-- Don't look for enemies at all when sister is this far from player.
local MAX_ENGAGE_MASTER_DIST  = 20
-- Stop following when this close to player.
local FOLLOW_STOP_DIST = 2.0

local function stopLocomotion(bandit)
    if Bandit and Bandit.SetMoving then
        pcall(Bandit.SetMoving, bandit, false)
    end
    pcall(function()
        if bandit.setTarget then bandit:setTarget(nil) end
    end)
    pcall(function()
        if bandit.setVariable then
            bandit:setVariable("BanditWalkType", "Walk")
        end
    end)
    pcall(function()
        if bandit.setWalkType then bandit:setWalkType("Walk") end
    end)
    pcall(function()
        local pf = bandit.getPathFindBehavior2 and bandit:getPathFindBehavior2()
        if pf then
            if pf.cancel then pf:cancel() end
            if pf.reset then pf:reset() end
        end
        if bandit.setPath2 then bandit:setPath2(nil) end
    end)
end

local function distSqXY(ax, ay, bx, by)
    local dx = ax - bx
    local dy = ay - by
    return dx * dx + dy * dy
end

local function getMaster(bandit)
    if BanditPlayer and BanditPlayer.GetMasterPlayer then
        return BanditPlayer.GetMasterPlayer(bandit)
    end
    return getSpecificPlayer and getSpecificPlayer(0)
end

local function getBrain(bandit)
    if BanditBrain and BanditBrain.Get then
        local ok, b = pcall(BanditBrain.Get, bandit)
        if ok and b then return b end
    end
    return bandit and bandit.getModData and bandit:getModData() and bandit:getModData().brain
end

local function setCombatAllowed(bandit, allowed)
    local brain = bandit and bandit.getModData and bandit:getModData() and bandit:getModData().brain
    if BanditBrain and BanditBrain.Get then
        local ok, b = pcall(BanditBrain.Get, bandit)
        if ok and b then brain = b end
    end
    if brain then
        brain.modpackFestivalSisterCombatAllowed = allowed == true
    end
end

local function getWalkType(bandit, master, dist)
    if master:isSprinting() or dist > 10 then
        return "Run", 0
    elseif master:isSneaking() and dist < 12 then
        return "SneakWalk", 0
    end
    if bandit:getHealth() < 0.4 then
        return "Limp", 0
    end
    return "Walk", 0
end

ZombiePrograms.ModpackCompanion.Prepare = function(bandit)
    Bandit.ForceStationary(bandit, false)
    setCombatAllowed(bandit, false)
    return { status = true, next = "Main", tasks = {} }
end

ZombiePrograms.ModpackCompanion.Main = function(bandit)
    local tasks = {}

    -- Stand Watch: freeze in place until the player calls her over or toggles it off
    local brain = getBrain(bandit)
    if brain and brain.modpackFestivalStandWatch then
        Bandit.ForceStationary(bandit, true)
        stopLocomotion(bandit)
        return { status = true, next = "Main", tasks = { { action = "Time", anim = "Idle", time = 60 } } }
    end

    Bandit.ForceStationary(bandit, false)

    if ModpackFestivalSister and ModpackFestivalSister.applyFollowSpeed then
        ModpackFestivalSister.applyFollowSpeed(bandit)
    end
    if ModpackFestivalSister and ModpackFestivalSister.applyUnlimitedStamina then
        ModpackFestivalSister.applyUnlimitedStamina(bandit)
    end
    if ModpackFestivalSister and ModpackFestivalSister.syncEquippedFirearmAmmo then
        ModpackFestivalSister.syncEquippedFirearmAmmo(bandit)
    end

    local master = getMaster(bandit)
    if not master then
        setCombatAllowed(bandit, false)
        table.insert(tasks, { action = "Time", anim = "Shrug", time = 200 })
        return { status = true, next = "Main", tasks = tasks }
    end

    local dist = BanditUtils.DistTo(bandit:getX(), bandit:getY(), master:getX(), master:getY())
    local walkType, endurance = getWalkType(bandit, master, dist)

    -- Only look for enemies when close enough to the player that combat makes sense
    if dist < MAX_ENGAGE_MASTER_DIST then
        local closestZombie   = BanditUtils.GetClosestZombieLocation(bandit)
        local closestBandit   = BanditUtils.GetClosestEnemyBanditLocation(bandit)
        local closestEnemy    = closestZombie
        if closestBandit.dist < closestZombie.dist then
            closestEnemy = closestBandit
        end

        -- Only engage if the enemy is also near the player, not just near sister
        local nearPlayer = closestEnemy.x and closestEnemy.y
            and distSqXY(closestEnemy.x, closestEnemy.y, master:getX(), master:getY()) <= ENGAGE_PLAYER_RADIUS_SQ

        if closestEnemy.dist < 8 and nearPlayer then
            setCombatAllowed(bandit, true)
            table.insert(tasks, BanditUtils.GetMoveTask(endurance, closestEnemy.x, closestEnemy.y, closestEnemy.z or 0, "Run", closestEnemy.dist, true))
            return { status = true, next = "Main", tasks = tasks }
        end
    end

    setCombatAllowed(bandit, false)

    -- Follow or idle
    local did = BanditUtils.GetCharacterID(master)
    local dz  = master:getZ()
    if dist > FOLLOW_STOP_DIST or math.abs((dz or 0) - (bandit:getZ() or 0)) >= 1 then
        table.insert(tasks, BanditUtils.GetMoveTaskTarget(endurance, master:getX(), master:getY(), dz, did, true, walkType, dist))
    else
        -- Close to player with nothing to fight — stop moving and play idle
        stopLocomotion(bandit)
        local idleTasks = BanditPrograms.Idle(bandit)
        if idleTasks and #idleTasks > 0 then
            for _, t in pairs(idleTasks) do
                table.insert(tasks, t)
            end
        else
            table.insert(tasks, { action = "FaceLocation", x = master:getX(), y = master:getY(), time = 60 })
        end
    end

    return { status = true, next = "Main", tasks = tasks }
end

print("[ModpackFestivalSpawn] Alyssa ModpackCompanion program loaded")
