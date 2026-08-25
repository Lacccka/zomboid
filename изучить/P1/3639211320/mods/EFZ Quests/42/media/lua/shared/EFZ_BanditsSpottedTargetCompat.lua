local PATCH_TAG = "[EFZ_BanditsSpottedTarget_Compat]"
local BANDITS_MOD_ID = "Bandits2"

if not EFZ then
    EFZ = {}
end

EFZ.BanditsSpottedTargetCompat = EFZ.BanditsSpottedTargetCompat or {}
local Compat = EFZ.BanditsSpottedTargetCompat

if _G.__EFZ_BANDITS_SPOTTED_TARGET_COMPAT_SHARED_FILE_LOADED then
    return
end
_G.__EFZ_BANDITS_SPOTTED_TARGET_COMPAT_SHARED_FILE_LOADED = true
DebugLog.log(PATCH_TAG .. " Loaded EFZ_BanditsSpottedTargetCompat.lua")

local function debugPrint(message)
    DebugLog.log(PATCH_TAG .. " " .. message)
end

local function logOnce(key, message)
    Compat.logKeys = Compat.logKeys or {}
    if Compat.logKeys[key] then
        return
    end

    Compat.logKeys[key] = true
    debugPrint(message)
end

local function isBanditsActive()
    if type(getActivatedMods) ~= "function" then
        return false
    end

    local mods = getActivatedMods()
    return mods and mods:contains(BANDITS_MOD_ID)
end

function Compat.isBanditsActive()
    return isBanditsActive()
end

-- Fixed Bandits 42.18+ Shoot.onComplete: drops broken `local patched = zombie.isBanditPatch`
-- before the nearby-zombie loop and keeps shooter-based spottedNew targeting.
Compat.patchedShootOnComplete = Compat.patchedShootOnComplete or function(bandit, task)
    local bumpType = bandit:getBumpType()
    if bumpType ~= task.anim then
        return true
    end

    local shooter = bandit
    local sx, sy, sz, sd = shooter:getX(), shooter:getY(), shooter:getZ(), shooter:getDirectionAngle()
    local brainShooter = BanditBrain.Get(shooter)
    local weapon = brainShooter.weapons[task.slot]
    local weaponItem = BanditCompatibility.InstanceItem(weapon.name)
    if not weaponItem then
        return true
    end

    weaponItem = BanditUtils.ModifyWeapon(weaponItem, brainShooter)

    local enemy = BanditZombie.Cache[task.eid] or BanditPlayer.GetPlayerById(task.eid)
    if not enemy then
        return true
    end

    if not BanditUtils.IsFacing(sx, sy, sd, enemy:getX(), enemy:getY(), 5) then
        return true
    end

    weapon.bulletsLeft = weapon.bulletsLeft - 1
    Bandit.UpdateItemsToSpawnAtDeath(shooter, brainShooter)

    BanditCompatibility.StartMuzzleFlash(shooter)
    local reloadType = weaponItem:getWeaponReloadType()
    local projectiles = BanditUtils.GetProjectileCount(reloadType)
    BanditProjectile.Add(brainShooter.id, sx, sy, sz, sd, projectiles)

    local emitter = shooter:getEmitter()
    local swingSound = weaponItem:getSwingSound()
    emitter:playSound(swingSound)

    local radius = weaponItem:getSoundRadius()
    local zombieList = BanditZombie.CacheLightZ
    for id, zombieLight in pairs(zombieList) do
        local dist = math.abs(sx - zombieLight.x) + math.abs(sy - zombieLight.y)
        if dist < radius then
            local zombie = BanditZombie.Cache[id]
            if zombie and not zombie:isMoving() then
                local asn = zombie:getActionStateName()
                if asn == "idle" then
                    zombie:spottedNew(shooter, true)
                    zombie:addAggro(shooter, 1)
                    zombie:setTarget(shooter)
                end
            end
        end
    end

    if BanditUtils.LineClear(shooter, enemy) then
        BanditUtils.ManageLineOfFire(shooter, enemy, weaponItem)
    end

    if not weaponItem:isManuallyRemoveSpentRounds() then
        shooter:playSound(weaponItem:getShellFallSound())
    end

    if weaponItem:isRackAfterShoot() then
        weapon.racked = false
    end

    return true
end

local function applyShootPatch()
    if not isBanditsActive() then
        return
    end

    if not ZombieActions or not ZombieActions.Shoot or type(ZombieActions.Shoot.onComplete) ~= "function" then
        logOnce("shoot-structure-mismatch", "Skipped ZombieActions.Shoot patch: unexpected structure.")
        return
    end

    if ZombieActions.Shoot.onComplete == Compat.patchedShootOnComplete then
        return
    end

    ZombieActions.Shoot.onComplete = Compat.patchedShootOnComplete
    logOnce("shoot-patched", "Patched ZombieActions.Shoot.onComplete for Bandits2 spotted-target compat.")
end

local function applyPatch()
    if not isBanditsActive() then
        return
    end

    applyShootPatch()
end

applyPatch()
Events.OnGameBoot.Add(applyPatch)
Events.OnCreatePlayer.Add(applyPatch)
Events.OnGameStart.Add(applyPatch)