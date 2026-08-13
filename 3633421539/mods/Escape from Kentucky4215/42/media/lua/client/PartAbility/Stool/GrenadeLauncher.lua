require "Grenade_Tajectory_core"
local AWCWF_Stool_Grenadelauncher = {
    ["GP25_cat"] = {LaunchSound = "LauncherFire", AmmoType = "GrenadeAmmo", EmptyType = "Gunpart.GP25_cat_empty", LoadSound = "LauncherReload", LoadType = "Gunpart.GP25_cat", ExplosionRange = 10, ExplosionDamage = 3, ExplosionSound = "Explosion4"},
    ["M203_cat"] = {LaunchSound = "LauncherFire", AmmoType = "GrenadeAmmo", EmptyType = "Gunpart.M203_cat_empty", LoadSound = "LauncherReload", LoadType = "Gunpart.M203_cat", ExplosionRange = 10, ExplosionDamage = 3, ExplosionSound = "Explosion4"},
    ["M28_cat"] = {LaunchSound = "LauncherFire", AmmoType = "GrenadeAmmo", EmptyType = "Gunpart.M28_cat_empty", LoadSound = "LauncherReload", LoadType = "Gunpart.M28_cat", ExplosionRange = 10, ExplosionDamage = 3, ExplosionSound = "Explosion4"},
    ["GP25_cat_empty"] = {LaunchSound = "LauncherFire", AmmoType = "GrenadeAmmo", EmptyType = false, LoadSound = "LauncherReload", LoadType = "Gunpart.GP25_cat", ExplosionRange = 10, ExplosionDamage = 3, ExplosionSound = "Explosion4"},
    ["M203_cat_empty"] = {LaunchSound = "LauncherFire", AmmoType = "GrenadeAmmo", EmptyType = false, LoadSound = "LauncherReload", LoadType = "Gunpart.M203_cat", ExplosionRange = 10, ExplosionDamage = 3, ExplosionSound = "Explosion4"},
    ["M28_cat_empty"] = {LaunchSound = "LauncherFire", AmmoType = "GrenadeAmmo", EmptyType = false, LoadSound = "LauncherReload", LoadType = "Gunpart.M28_cat", ExplosionRange = 10, ExplosionDamage = 3, ExplosionSound = "Explosion4"},
}

local HasEnterFireModeFlag = HasEnterFireModeFlag or false
local function HasGrenadelauncher(playerObj)
    local MainGun = playerObj:getPrimaryHandItem()
    if not MainGun then
        return {false, false}
    end
    if MainGun:IsWeapon() and MainGun:isRanged() then
        if MainGun:getWeaponPart("Stool") then
            local GrenadelauncherType = AWCWF_Stool_Grenadelauncher[MainGun:getWeaponPart("Stool"):getType()]
            if GrenadelauncherType then
                if GrenadelauncherType.EmptyType then
                    return {true, true}
                else
                    return {true, false}
                end
            else
                return {false, false}
            end
        end
    end
    return {false, false}
end
local function SetLauncherStatus(playerObj, type)
    local MainGun = playerObj:getPrimaryHandItem()
    local MagPart = instanceItem(type)
    MainGun:setWeaponPart("Stool", MagPart)
end

local function CheckGrenadelauncher(playerObj)
    local FlagNow = HasGrenadelauncher(playerObj)
    if FlagNow[1] and playerObj:isAiming() then
        local weaitem = instanceItem("Base.PipeBomb")
        if Grenade_Tajectory.GrenadeAimCursor == nil then
            Grenade_Tajectory.GrenadeAimCursor = ISShootGrenade:new("", "", playerObj, weaitem)
            getCell():setDrag(Grenade_Tajectory.GrenadeAimCursor, 0)
        end
        HasEnterFireModeFlag = true
        return
    end
    if HasEnterFireModeFlag and not playerObj:isAiming() then
        local DragNow = getCell():getDrag(0)
        if DragNow and DragNow.Type == "ISShootGrenade" then
            getCell():setDrag(nil, 0)
        end
        Grenade_Tajectory.GrenadeAimCursor = nil
        HasEnterFireModeFlag = false
    end
end
local function LaunchGrenadeLauncher(_key)
    if _key == getCore():getKey("LauchGrenadelauncherat") then
        local playerObj = getPlayer()
        local FlagNow = HasGrenadelauncher(playerObj)
        if FlagNow[1] and playerObj:isAiming() then
            if FlagNow[2] then
                local cell = getCell():getDrag(0)
                if cell == nil then
                    return
                end
            
                local GrenadelauncherType = nil
                for i = 0, playerObj:getPrimaryHandItem():getAllWeaponParts():size() - 1 do
                    local partType = playerObj:getPrimaryHandItem():getAllWeaponParts():get(i):getType()
                    for _,v in pairs(AWCWF_Stool_Grenadelauncher) do
                        if partType == _ then
                            GrenadelauncherType = v
                        end
                    end
                end
            
                if GrenadelauncherType == nil then return end
                
                playerObj:getEmitter():playSound(GrenadelauncherType.LaunchSound)
                local TargetSquare = Grenade_Tajectory.aimcursorsq
                if TargetSquare then
                    Grenade_Tajectory.ShootGrenade(playerObj, playerObj:getPrimaryHandItem(), GrenadelauncherType)
                    SetLauncherStatus(playerObj, GrenadelauncherType.EmptyType)
                end
            else
                local inv = playerObj:getInventory()
                
                local GrenadelauncherType = nil
                for i = 0, playerObj:getPrimaryHandItem():getAllWeaponParts():size() - 1 do
                    local partType = playerObj:getPrimaryHandItem():getAllWeaponParts():get(i):getType()
                    for _,v in pairs(AWCWF_Stool_Grenadelauncher) do
                        if partType == _ then
                            GrenadelauncherType = v
                        end
                    end
                end
            
                if GrenadelauncherType == nil then return end
                local AmmoCanno = inv:FindAndReturn(GrenadelauncherType.AmmoType)
                if AmmoCanno then
                    ISTimedActionQueue.add(ISShootGrenadeReload:new(playerObj, 100, playerObj:getPrimaryHandItem(),
                        GrenadelauncherType))
                end


            end
        end
    end
end
local function Check()
    Events.OnKeyPressed.Add(LaunchGrenadeLauncher)
end
local function OnGameStartCheckModinfo()
    Events.OnGameStart.Add(Check)
    Events.OnPlayerUpdate.Add(CheckGrenadelauncher)
end
Events.OnGameStart.Add(OnGameStartCheckModinfo)
