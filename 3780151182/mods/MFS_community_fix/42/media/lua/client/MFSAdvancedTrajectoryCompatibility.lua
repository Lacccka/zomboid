--[[---------------------------------------------------------------------------
    MFSAdvancedTrajectoryCompatibility.lua

    Keeps Advanced Trajectory's rifle front end away from the rebuilt MTL30.
    The launcher remains a ranged firearm so vanilla owns firing, magazines and
    reloads, but its projectile and aiming are owned by MFS_GrenadeBallistics.

    Advanced Trajectory returns its module table from Advanced_trajectory_core,
    so unlike the file-local legacy Ballistic_lite callback its registered shot
    callback can be removed and replaced without editing the third-party mod.
-----------------------------------------------------------------------------]]

require "MFSUnderbarrelRegistry"

MFSATROCompatibility = MFSATROCompatibility or {}
local Compat = MFSATROCompatibility

local ATRO_MOD_ID = "Advanced_Trajectorys_Realistic_Overhaul"

local function getATROModule()
    if type(Compat.atroModule) == "table" then
        return Compat.atroModule
    end

    -- ATRO keeps Advanced_trajectory local to Advanced_trajectory_core.lua and
    -- exposes it only as that module's return value. It is not a Lua global.
    local activatedMods = getActivatedMods and getActivatedMods() or nil
    if not activatedMods or not activatedMods:contains(ATRO_MOD_ID) then
        return nil
    end

    local ok, atro = pcall(require, "Advanced_trajectory_core")
    if ok and type(atro) == "table" then
        Compat.atroModule = atro
        return atro
    end

    return nil
end

-- Full types explicitly owned by the rebuilt launcher system. Keep this table
-- on the public compatibility object so later pseudo-launchers can register
-- themselves without replacing this file's local functions.
Compat.launcherTypes = Compat.launcherTypes or {
    ["Base.MFS_MTL30_cat"] = true,
}
for _, definition in pairs(MFSUnderbarrelRegistry.LAUNCHERS) do
    Compat.launcherTypes[definition.pseudoType] = true
end

function Compat.registerLauncher(fullType)
    if type(fullType) ~= "string" or fullType == "" then return false end
    Compat.launcherTypes[fullType] = true
    return true
end

function Compat.unregisterLauncher(fullType)
    if type(fullType) ~= "string" then return false end
    Compat.launcherTypes[fullType] = nil
    return true
end

local function isMFSLauncher(item)
    if not item then return false end

    local fullType = item:getFullType()
    if Compat.launcherTypes[fullType] then return true end
    if MFSUnderbarrelRegistry.isPseudo(fullType) then return true end

    -- Every weapon with a grenade-ballistics config is also a launcher. This
    -- makes the future single underbarrel pseudo-gun compatible as soon as its
    -- config is added, even if it was not prelisted above.
    return type(MFS_GrenadeBallistics) == "table"
        and type(MFS_GrenadeBallistics.CONFIGS) == "table"
        and MFS_GrenadeBallistics.CONFIGS[fullType] ~= nil
end

Compat.isLauncher = isMFSLauncher

local function neutralizeNativeHit(item)
    if not isMFSLauncher(item) then return end

    if item.setMaxHitCount then item:setMaxHitCount(0) end
    if item.setRangeFalloff then item:setRangeFalloff(true) end
    if item.setProjectileCount then item:setProjectileCount(0) end
end

local function install()
    if Compat.installed then return true end
    local atro = getATROModule()
    if not atro then return false end

    local originalSwing = atro.OnWeaponSwing
    if type(originalSwing) == "function" then
        Events.OnWeaponSwingHitPoint.Remove(originalSwing)

        Compat.originalSwing = originalSwing
        Compat.filteredSwing = function(character, handWeapon)
            if isMFSLauncher(handWeapon) then
                neutralizeNativeHit(handWeapon)
                return
            end
            return originalSwing(character, handWeapon)
        end

        Events.OnWeaponSwingHitPoint.Add(Compat.filteredSwing)
    end

    -- checkontick() calls this table member dynamically, so wrapping the member
    -- prevents ATRO from hiding the cursor or drawing its rifle reticle while
    -- the rebuilt launcher is equipped.
    local originalPlayerUpdate = atro.OnPlayerUpdate
    if type(originalPlayerUpdate) == "function" then
        Compat.originalPlayerUpdate = originalPlayerUpdate
        Compat.filteredPlayerUpdate = function(...)
            local player = getPlayer()
            local weapon = player and player:getPrimaryHandItem() or nil
            if isMFSLauncher(weapon) then
                -- ATRO normally changes the live weapon instance to one native
                -- hit while drawing outlines. Never allow that value to persist
                -- on the rebuilt launcher.
                neutralizeNativeHit(weapon)
                atro.hasFlameWeapon = false
                if Mouse then Mouse.setCursorVisible(true) end
                return
            end
            return originalPlayerUpdate(...)
        end
        atro.OnPlayerUpdate = Compat.filteredPlayerUpdate
    end

    Compat.installed = Compat.filteredSwing ~= nil or Compat.filteredPlayerUpdate ~= nil
    if Compat.installed and not Compat.installLogged then
        print("[MFSGrenade][ATRO] launcher compatibility installed")
        Compat.installLogged = true
    end
    return Compat.installed
end

Compat.install = install

-- Final event-boundary guard. OnWeaponSwing occurs before
-- OnWeaponSwingHitPoint, where ATRO creates its projectile. Removing ATRO's
-- original callback here guarantees that even a late or duplicate third-party
-- registration cannot create a rifle bullet for an MFS launcher. The filtered
-- wrapper remains registered and continues forwarding every other firearm.
local function onLauncherSwing(character, handWeapon)
    if not isMFSLauncher(handWeapon) then return end

    install()
    neutralizeNativeHit(handWeapon)

    local atro = getATROModule()
    if atro then
        local atroSwing = Compat.originalSwing or atro.OnWeaponSwing
        if type(atroSwing) == "function" then
            Events.OnWeaponSwingHitPoint.Remove(atroSwing)
        end
    end
end

if Compat.preSwingGuard then
    Events.OnWeaponSwing.Remove(Compat.preSwingGuard)
end
Compat.preSwingGuard = onLauncherSwing
Events.OnWeaponSwing.Add(onLauncherSwing)

Events.OnGameStart.Add(install)
