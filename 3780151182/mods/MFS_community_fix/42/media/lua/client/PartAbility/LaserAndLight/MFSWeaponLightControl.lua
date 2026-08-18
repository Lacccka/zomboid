-- MFS community fix: gun-mounted flashlight control.
--
-- WHAT WAS WRONG
-- --------------
-- Upstream ships two competing implementations of the weapon light:
--
--   client/WeaponAbility/WeaponLightgun.lua                (ACTIVE)
--       On every player update: if aiming and a "Light" part is attached,
--       setTorchCone(true) / setLightDistance(30) / setLightStrength(9);
--       otherwise clear them. No state, no hotkey, no battery. This is the
--       "light is always on while aiming" behaviour players report.
--
--   client/PartAbility/LaserAndLight/OnUpdateLaserAndLight   (DISABLED)
--       The intended toggle system. Shipped with no .lua extension, so the
--       game never loads it. It is dead for several independent reasons:
--         1. AWCWF_LaserAndGunLightSet is declared {} in AWCWF_GlobalVarsSet.lua
--            and never populated, so the laser branch can never fire.
--         2. LocalLaserUpdate builds its modData table locally when nil and
--            never writes it back to getModData().NowLightSet, so no state
--            can persist between frames.
--         3. PowerDown() gates battery drain on the globals IsLaserOn /
--            IsGunLightOn, which are never defined anywhere - the real flags
--            live on the modData table. They read nil forever, which is why
--            the battery always shows 100.
--         4. The four Light parts in scripts/Part/Light.txt declare no
--            LightDistance / LightStrength / TorchCone, so the part getters
--            the old code reads return 0 - even a working toggle would have
--            produced an invisible light.
--         5. ISSwitchLasetAction:perform can compare a nil .index, and the
--            tooltip reads the battery off the part in one branch and off the
--            gun in another.
--
-- WHAT THIS FILE DOES
-- -------------------
-- Replaces the state/toggle/battery layer only. The engine layer upstream
-- uses - setTorchCone / setLightDistance / setLightStrength on the equipped
-- HandWeapon - is the correct B42 API and demonstrably works today; that is
-- precisely why the always-on light is visible. Vanilla's own flashlight route
-- (ActivatedItem + setActivated) is not available to us, because the engine
-- only treats equipped items as light sources and an attached weapon part is
-- not one. So nothing is ported and nothing new is needed at the engine layer.
--
-- Behaviour:
--   * Light is OFF by default. Toggled with the existing "SwtichLightSet"
--     binding (Options > Key Bindings > Escape From Kentucky Key Settings >
--     Toggle Light Mode; default F, registered in AWCWF_KeyBind.lua).
--   * While ON the light emits whenever the gun is equipped, aiming or not.
--   * Battery actually drains, and at 0 the light stops emitting while the
--     switch stays ON, so it resumes when a battery is fitted.
--
-- COEXISTENCE WITH THE UPSTREAM HANDLER  (important - read before editing)
-- -----------------------------------------------------------------------
-- Per MFS_ARCHIVE_POLICY.txt, under the separate-mod install model a Lua file
-- present in both mods is NOT shadowed - BOTH copies execute. WeaponLightgun.lua
-- registers its handler as a local function via Events.OnPlayerUpdate.Add, so
-- there is no reference we can pass to Events.OnPlayerUpdate.Remove. We cannot
-- unregister it.
--
-- Therefore this file does not try to. It wins by ordering and by asserting:
--   * The patch requires ModernFirearmsSystem, so MFS loads first and its
--     handler is registered first, i.e. it runs first within each frame.
--   * We register our handler inside OnGameStart, which is strictly after all
--     file-load-time registrations, so ours always runs last in the frame.
--   * Our handler compares the live torch state against the state we want and
--     corrects it. Anything the upstream handler wrote earlier in the same
--     frame is overwritten before the frame renders.
--
-- A neutered copy of WeaponLightgun.lua also ships alongside this file. That
-- copy only matters for the legacy "extract over MFS" overlay install, where
-- files really are replaced. Under the current separate-mod model it is inert,
-- and it is deliberately free of side-effecting calls so it adds nothing to the
-- "OVERRIDING Lua with side effects" list in the build tool.
--
-- KNOWN LIMITATIONS
--   * Single-player / client-local. setTorchCone is applied to the item on this
--     client; remote players are not expected to see another player's gun light.
--     Not addressed in this RC - see the RC note test matrix.
--   * Laser is untouched. AWCWF_LaserAndGunLightSet is empty upstream, so the
--     laser needs new content rather than a fix, and is out of scope here.

MFSWeaponLightControl = MFSWeaponLightControl or {}

local Control = MFSWeaponLightControl

Control.VERSION = "1.0.0"
Control.BIND = "SwtichLightSet"

-- Battery is stored on the WEAPON's modData under this key, 0-100. That is the
-- same key and scale the upstream ISGunAddBatteryAction / ISGunRemoveBatteryAction
-- and the BatterySet.lua context menu already use, so "Add/Remove Battery" keeps
-- working unmodified. Do not rename it.
Control.BATTERY_KEY = "LightBatteryReamin"

-- On/off switch, per gun, persisted in the save. A new key rather than the old
-- NowLightSet table, which is left alone so any stale save data is inert.
Control.STATE_KEY = "MFSGunLightOn"

-- Drain per in-game minute. 0.1 matches the rate the dead PowerDown() intended,
-- giving 1000 in-game minutes (~16.7 in-game hours) of continuous use per battery.
Control.DRAIN_PER_MINUTE = 0.1

-- Charge (0-100) at or below which the player gets a one-off low-battery warning.
-- There is no battery readout anywhere in the mod's UI - the only tooltip that
-- ever showed one lived inside the disabled OnUpdateLaserAndLight file - so
-- without this the light would simply die with no warning at all.
Control.LOW_WARN_AT = 20

-- Per-gun latch so the low warning fires once rather than every in-game minute.
-- Cleared automatically when the charge rises back above the threshold.
Control.WARNED_KEY = "MFSGunLightLowWarned"

-- The Light parts declare no light values of their own (see note 4 above), so
-- the numbers live here. The default deliberately equals the 30 / 9 that
-- WeaponLightgun.lua hardcodes today, so this RC changes WHEN the light is on
-- and nothing about how it looks. Per-part tuning can be added later without
-- touching any other file.
Control.DEFAULT_PROFILE = {distance = 30, strength = 9}
Control.PROFILES = {
    -- ["ArmytekPredator_Bottom"]  = {distance = 30, strength = 9},
    -- ["ArmytekPredator_Bottom1"] = {distance = 30, strength = 9},
    -- ["ArmytekPredator_Bottom2"] = {distance = 30, strength = 9},  -- pistol light
    -- ["ArmytekPredator_Bottom3"] = {distance = 30, strength = 9},
}

local function log(message)
    print("[MFSGunLight] " .. tostring(message))
end

local function try(fn, fallback)
    local ok, value = pcall(fn)
    if ok then
        return value
    end
    return fallback
end

local function isGun(item)
    if not item then
        return false
    end
    if try(function() return item:IsWeapon() end, false) ~= true then
        return false
    end
    return try(function() return item:isRanged() end, false) == true
end

local function getLightPart(weapon)
    return try(function() return weapon:getWeaponPart("Light") end, nil)
end

local function profileFor(part)
    local partType = try(function() return part:getType() end, nil)
    if partType and Control.PROFILES[partType] then
        return Control.PROFILES[partType]
    end
    return Control.DEFAULT_PROFILE
end

local function round1(value)
    return math.floor((value * 10) + 0.5) / 10
end

local function feedback(playerObj, key, suffix)
    pcall(function()
        if HaloTextHelper and HaloTextHelper.addText then
            local text = getText(key)
            if suffix then
                text = text .. " " .. suffix
            end
            HaloTextHelper.addText(playerObj, text)
        end
    end)
end

-- Charge is shown as a whole-number percentage because the stored value is
-- already 0-100. Built in Lua rather than through getText substitution so the
-- literal % cannot collide with the translation system's %1 placeholders.
local function chargeSuffix(value)
    return "(" .. tostring(math.floor(value + 0.5)) .. "%)"
end

-- Battery ---------------------------------------------------------------------

-- Only ever called for a gun that actually has a Light part, so guns without one
-- never get this key written into their modData.
local function getBattery(weapon)
    local modData = weapon:getModData()
    local value = modData[Control.BATTERY_KEY]
    if type(value) ~= "number" then
        -- 100 on first sight, matching the upstream default. A freshly attached
        -- light therefore arrives charged rather than dead, which is what the
        -- original design intended and avoids "my new light does nothing".
        value = 100
        modData[Control.BATTERY_KEY] = value
    end
    return value
end

local function setBattery(weapon, value)
    if value < 0 then
        value = 0
    end
    weapon:getModData()[Control.BATTERY_KEY] = round1(value)
end

-- Switch state ----------------------------------------------------------------

local function isSwitchedOn(weapon)
    return weapon:getModData()[Control.STATE_KEY] == true
end

local function setSwitchedOn(weapon, value)
    weapon:getModData()[Control.STATE_KEY] = (value == true)
end

-- Applying the state to the engine ---------------------------------------------

-- Counts frames in which we found the torch enabled while we wanted it off,
-- i.e. frames in which the upstream handler re-enabled it behind us. Purely
-- diagnostic; logged once so the console shows which install model is in play.
local reassertCount = 0
local reassertLogged = false

local function applyLight(weapon, wantOn, profile)
    local current = try(function() return weapon:isTorchCone() end, false)

    if wantOn then
        -- The strength check matters because the upstream handler forces 30 / 9
        -- while aiming. Without it, a custom PROFILES entry would be silently
        -- overwritten every time the player raised the gun.
        local strength = try(function() return weapon:getLightStrength() end, nil)
        if current ~= true or strength ~= profile.strength then
            pcall(function()
                weapon:setTorchCone(true)
                weapon:setLightDistance(profile.distance)
                weapon:setLightStrength(profile.strength)
            end)
        end
        return
    end

    if current == true then
        reassertCount = reassertCount + 1
        if reassertCount >= 60 and not reassertLogged then
            reassertLogged = true
            log("upstream WeaponLightgun.lua is live and re-enabling the torch each " ..
                "frame; this handler is overriding it. Expected under the separate-mod " ..
                "install, where both copies of a Lua file execute.")
        end
        pcall(function()
            weapon:setTorchCone(false)
            weapon:setLightDistance(0)
            weapon:setLightStrength(0.0)
        end)
    end
end

-- Clears the engine flags on a weapon we are no longer holding, without touching
-- its switch state, so re-equipping restores whatever the player had set.
local function clearLight(weapon)
    if not weapon then
        return
    end
    pcall(function()
        if weapon:isTorchCone() then
            weapon:setTorchCone(false)
            weapon:setLightDistance(0)
            weapon:setLightStrength(0.0)
        end
    end)
end

-- Per-player, so split-screen does not cross-clear.
Control._lastWeapon = Control._lastWeapon or {}

local function playerKey(playerObj)
    return try(function() return playerObj:getPlayerNum() end, 0) or 0
end

local function onPlayerUpdate(playerObj)
    if not playerObj then
        return
    end

    local key = playerKey(playerObj)
    local weapon = playerObj:getPrimaryHandItem()

    local part = nil
    if isGun(weapon) then
        part = getLightPart(weapon)
    end

    -- Only ever track and clear guns that carry a Light part. Tracking whatever
    -- happens to be in the primary hand would mean clearing the torch cone of a
    -- vanilla flashlight the moment the player swapped away from it, breaking
    -- an item this patch has no business touching.
    local tracked = (part ~= nil) and weapon or nil
    local previous = Control._lastWeapon[key]
    if previous and previous ~= tracked then
        clearLight(previous)
    end
    Control._lastWeapon[key] = tracked

    if not part then
        return
    end

    -- Emit only if the switch is on AND there is charge. A flat battery leaves
    -- the switch on, so fitting a new one brings the light straight back.
    local wantOn = isSwitchedOn(weapon) and getBattery(weapon) > 0
    applyLight(weapon, wantOn, profileFor(part))
end

-- Drain -----------------------------------------------------------------------

local function drainBattery()
    local playerObj = getPlayer()
    if not playerObj then
        return
    end

    local weapon = playerObj:getPrimaryHandItem()
    if not isGun(weapon) then
        return
    end
    if not getLightPart(weapon) then
        return
    end
    if not isSwitchedOn(weapon) then
        return
    end

    local remaining = getBattery(weapon)
    if remaining <= 0 then
        return
    end

    remaining = remaining - Control.DRAIN_PER_MINUTE
    setBattery(weapon, remaining)

    local modData = weapon:getModData()

    if remaining <= 0 then
        modData[Control.WARNED_KEY] = nil
        feedback(playerObj, "IGUI_MFS_GunLight_Depleted")
        return
    end

    if remaining <= Control.LOW_WARN_AT then
        if not modData[Control.WARNED_KEY] then
            modData[Control.WARNED_KEY] = true
            feedback(playerObj, "IGUI_MFS_GunLight_Low", chargeSuffix(remaining))
        end
    else
        -- Recharged past the threshold, so re-arm the warning for next time.
        modData[Control.WARNED_KEY] = nil
    end
end

-- Toggle ----------------------------------------------------------------------

local function toggleLight(playerObj)
    if not playerObj then
        return
    end

    local weapon = playerObj:getPrimaryHandItem()
    if not isGun(weapon) then
        return
    end
    if not getLightPart(weapon) then
        return
    end

    local charge = getBattery(weapon)

    if isSwitchedOn(weapon) then
        setSwitchedOn(weapon, false)
        -- Charge is reported on the way out too, so the player can check the
        -- level deliberately by tapping the key twice.
        feedback(playerObj, "IGUI_MFS_GunLight_Off", chargeSuffix(charge))
        return
    end

    if charge <= 0 then
        -- Do not flip the switch on a dead light; say so instead, otherwise the
        -- key looks broken in exactly the way this patch is meant to fix.
        -- Recharge is right-click the gun > "Add battery to light".
        feedback(playerObj, "IGUI_MFS_GunLight_NoBattery")
        return
    end

    setSwitchedOn(weapon, true)
    feedback(playerObj, "IGUI_MFS_GunLight_On", chargeSuffix(charge))
end

local function onKeyPressed(key)
    local core = getCore()
    if not core then
        return
    end
    if key ~= core:getKey(Control.BIND) then
        return
    end
    toggleLight(getPlayer())
end

-- Install ----------------------------------------------------------------------

local function install()
    if Control._installed then
        return
    end
    Control._installed = true

    -- Registered here rather than at file scope on purpose: OnGameStart runs
    -- after every file-load-time Events.Add, which guarantees this handler runs
    -- after the upstream WeaponLightgun.lua handler within each frame.
    Events.OnPlayerUpdate.Add(onPlayerUpdate)
    Events.EveryOneMinute.Add(drainBattery)
    Events.OnKeyPressed.Add(onKeyPressed)

    log("version " .. Control.VERSION .. " installed; bind=" .. Control.BIND ..
        "; drain=" .. tostring(Control.DRAIN_PER_MINUTE) .. "/min")
end

Events.OnGameStart.Add(install)
