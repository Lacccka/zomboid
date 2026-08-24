-- MFS community fix: weapon light / laser battery context menu.
--
-- Same-path override of the upstream BatterySet.lua. Companion to
-- client/PartAbility/LaserAndLight/MFSWeaponLightControl.lua - read that file
-- first, it carries the full root-cause record for the flashlight work.
--
-- WHAT WAS WRONG WITH THE UPSTREAM VERSION
-- ----------------------------------------
--   P1  "Add battery to light" was gated on  Battery == 0  exactly. You could
--       not top a light up; you had to run it completely flat first. Combined
--       with the fact that nothing in the mod displays the charge, the practical
--       result was that players never found the recharge option at all. This is
--       the reported "I don't know how to recharge".
--
--   P2  The charge is stored per GUN, and until the light had been switched on
--       once the key did not exist at all - so `Battery == 0` was false and
--       `Battery > 0` was false, and NEITHER menu option appeared. A gun with a
--       brand new light showed nothing. MFSWeaponLightControl now initialises
--       the key to 100 on sight, and this file treats nil as 100 as well so the
--       two agree even if the light has never been equipped.
--
--   P3  ISGunAddBatteryAction:perform REPLACES the stored charge:
--
--           Weapon:getModData().LightBatteryReamin = Battery:getCurrentUsesFloat() * 100
--
--       Harmless when the only legal moment to use it was at exactly 0, but the
--       moment topping up is allowed (P1) it becomes a charge-destroying bug:
--       adding a 40% battery to a light sitting at 90% would leave it at 40%.
--       This file patches that method to COMBINE and cap at 100 instead.
--
--   P4  Battery selection took the highest-charged battery in the inventory
--       (GetMaxBattery), which is the worst choice when topping up - it wastes
--       the most charge against the 100 cap. This file picks the smallest
--       battery that still fills the gap, and only falls back to the largest
--       when nothing can fill it.
--
-- The laser side is left functionally as upstream wrote it. The laser itself
-- does not work (AWCWF_LaserAndGunLightSet is empty upstream and the beam swap
-- never fires), so its battery menu is cosmetic either way; it is kept in step
-- with the light purely so the two do not behave differently for no reason.
--
-- DOUBLE-EXECUTION GUARD
-- ----------------------
-- MFS_ARCHIVE_POLICY.txt records that a Lua file present in both mods may have
-- BOTH copies execute, in which case upstream's OnFillInventoryObjectContextMenu
-- handler would still run and the player would see two of every battery option.
-- RC7 testing showed shadowing actually working for WeaponLightgun.lua (the
-- conflict diagnostic never fired), but that is one observation, not a
-- guarantee, and it may differ by file or by install model. So this file removes
-- any battery option already present before adding its own. It runs after
-- upstream's handler because our mod loads after MFS.

-- NO require HERE. RC7B had  require "TimedActions/ISGunAddBatteryAction"  at
-- this point and it failed silently as a WARN, not an ERROR:
--
--   WARN : Lua at Lua((MOD: MFS_fix_beta)).BatterySet.lua>
--          require("TimedActions/ISGunAddBatteryAction") failed
--
-- Two reasons, both worth remembering:
--   * The directory is client/TimeActions, NOT client/TimedActions. The
--     upstream folder name is missing the "d". Vanilla's own folder IS
--     TimedActions, which is what makes the typo so easy to reproduce.
--   * Even spelled correctly it would be the wrong tool. See patchAddAction().
--
-- When checking a test console, grep WARN as well as ERROR and Exception.
-- RC7B was declared clean on an ERROR/Exception grep alone and this line was
-- sitting in the log the whole time.

MFSBatterySet = MFSBatterySet or {}

local Battery = MFSBatterySet

Battery.VERSION = "1.2.0"
Battery.FULL = 100

-- Which part types have a battery at all, and - for the LASER only - the item
-- modData key that still holds its charge.
--
-- The LIGHT entry is now only a validity marker: readCharge and writeCharge
-- route "Light" to MFSGunLightStore before ever consulting this table, because
-- item modData is destroyed on every shot in multiplayer. The key string is
-- kept because MFSGunLightStore reads it once when migrating an older save.
-- Do not use it as a live location for the light charge.
Battery.KEYS = {
    Laser = "LaserBatteryReamin",
    Light = "LightBatteryReamin"
}

local function log(message)
    print("[MFSBatterySet] " .. tostring(message))
end

local function try(fn, fallback)
    local ok, value = pcall(fn)
    if ok then
        return value
    end
    return fallback
end

-- P3: combine rather than replace ------------------------------------------------
--
-- Patched on the class rather than shipped as a same-path override of
-- client/TimeActions/ISGunAddBatteryAction.lua, so the diff stays in one file.
--
-- WHY THIS RUNS AT OnGameStart AND NOT AT FILE SCOPE
-- --------------------------------------------------
-- RC7B ran this block at file-load time and it never applied. Confirmed in
-- testing: a light at 80% given a 21% battery ended at 21%, and one at 56%
-- given a 60% battery ended at 60% - both results exactly equal to the new
-- battery's own value, i.e. untouched upstream replace behaviour.
--
-- Lua files load in path order, and within client/ that is alphabetical:
--
--     client/PartAbility/LaserAndLight/BatterySet.lua      <- P, loads first
--     client/TimeActions/ISGunAddBatteryAction.lua         <- T, loads later
--
-- so ISGunAddBatteryAction did not exist yet, the `if ISGunAddBatteryAction`
-- guard was false, and the patch was skipped in silence. Upstream's own
-- BatterySet.lua gets away with naming the class because it only does so inside
-- a runtime callback, by which point everything has loaded.
--
-- OnGameStart is strictly after every file has loaded, which removes the
-- ordering dependency entirely. This is the same reasoning that puts the event
-- registrations in MFSWeaponLightControl.lua inside its install().
--
-- If this ever silently stops working again, the console will say so - the
-- install line reports whether the patch was applied.

local function patchAddAction()
    if not ISGunAddBatteryAction then
        log("ISGunAddBatteryAction is still undefined at OnGameStart - the " ..
            "combine-and-cap fix is NOT active and adding a battery will " ..
            "REPLACE the charge. Check that MFS is loaded.")
        return false
    end

    if ISGunAddBatteryAction.MFSCombinePatched then
        return true
    end
    ISGunAddBatteryAction.MFSCombinePatched = true

    function ISGunAddBatteryAction:perform()
        ISBaseTimedAction.perform(self)

        if self.Weapon and self.BatteryItem and MFSBatterySet.KEYS[self.PartType] then
            local current = MFSBatterySet.readCharge(self.Weapon, self.PartType)
            local added = (try(function() return self.BatteryItem:getCurrentUsesFloat() end, 0) or 0) * 100

            local combined = current + added
            if combined > MFSBatterySet.FULL then
                combined = MFSBatterySet.FULL
            end
            combined = math.floor((combined * 10) + 0.5) / 10
            MFSBatterySet.writeCharge(self.Weapon, self.PartType, combined)

            -- Logged because this is the exact value the RC7B bug got wrong,
            -- and it only fires when a battery is actually fitted.
            log(tostring(self.PartType) .. " battery: " .. tostring(current) ..
                "% + " .. tostring(added) .. "% -> " .. tostring(combined) .. "%")
        end

        self.character:getInventory():Remove(self.BatteryItem)
    end

    -- RC7E: the remove action also wrote item modData directly, so in MP it
    -- returned a battery whose charge came from a table that had already been
    -- wiped by the last shot - i.e. always full. Routed through the same store.
    if ISGunRemoveBatteryAction and not ISGunRemoveBatteryAction.MFSStorePatched then
        ISGunRemoveBatteryAction.MFSStorePatched = true

        function ISGunRemoveBatteryAction:perform()
            ISBaseTimedAction.perform(self)

            local remaining = 0
            if self.Weapon and MFSBatterySet.KEYS[self.PartType] then
                remaining = MFSBatterySet.readCharge(self.Weapon, self.PartType)
                MFSBatterySet.writeCharge(self.Weapon, self.PartType, 0)
            end

            local batteryItem = self.character:getInventory():AddItem("Base.Battery")
            if batteryItem then
                batteryItem:setCurrentUsesFloat(remaining / 100)
            end

            log(tostring(self.PartType) .. " battery removed at " ..
                tostring(remaining) .. "%")
        end
    end

    return true
end

-- Charge helpers -----------------------------------------------------------------

-- RC7E: the LIGHT charge no longer lives on the item. In multiplayer the
-- weapon's entire modData table is replaced a few frames after every shot and
-- again on reload, which reset the charge to full continuously - so battery
-- drain could never accumulate in MP and nobody had noticed. It is now held on
-- the player, in MFSGunLightStore. See that file's header for the evidence.
--
-- The LASER charge is deliberately left on item modData. The laser does not
-- function at all (AWCWF_LaserAndGunLightSet is empty upstream, defect D1), so
-- its menu is cosmetic and moving it would be churn for no behaviour.
-- Public on the table, not local: the patched timed-action methods above call
-- these at runtime through MFSBatterySet, so they must be reachable from
-- outside this file's local scope.
function Battery.readCharge(weapon, partType)
    if partType == "Light" then
        if not MFSGunLightStore then
            return nil
        end
        return MFSGunLightStore.getBattery(getPlayer(), weapon)
    end

    local key = Battery.KEYS[partType]
    if not key then
        return nil
    end
    local value = weapon:getModData()[key]
    if type(value) ~= "number" then
        -- P2: a nil charge reads as full, so a gun whose light has never been
        -- switched on still offers the correct menu entries.
        return Battery.FULL
    end
    return value
end

function Battery.writeCharge(weapon, partType, value)
    if partType == "Light" then
        if MFSGunLightStore then
            MFSGunLightStore.setBattery(getPlayer(), weapon, value)
        end
        return
    end

    local key = Battery.KEYS[partType]
    if key then
        weapon:getModData()[key] = math.floor((value * 10) + 0.5) / 10
    end
end

-- P4: smallest battery that still fills the gap; largest if none can.
local function pickBattery(gap)
    local inventory = getPlayer():getInventory()
    local list = inventory:FindAll("Base.Battery")
    if not list then
        return nil
    end

    local bestFit, largest = nil, nil
    for i = 0, list:size() - 1 do
        local item = list:get(i)
        local charge = (try(function() return item:getCurrentUsesFloat() end, 0) or 0) * 100
        if charge > 0 then
            if not largest or charge > ((try(function() return largest:getCurrentUsesFloat() end, 0) or 0) * 100) then
                largest = item
            end
            if charge >= gap then
                if not bestFit or charge < ((try(function() return bestFit:getCurrentUsesFloat() end, 0) or 0) * 100) then
                    bestFit = item
                end
            end
        end
    end

    return bestFit or largest
end

-- Actions ------------------------------------------------------------------------

local function removeBattery(player, item, partType)
    ISTimedActionQueue.add(ISInventoryTransferAction:new(getPlayer(), item, item:getContainer(),
        getPlayer():getInventory()))
    ISInventoryPaneContextMenu.equipWeapon(item, true, item:isRequiresEquippedBothHands(), 0)
    ISTimedActionQueue.add(ISGunRemoveBatteryAction:new(player, 20, item, partType))
end

local function addBattery(player, item, partType, batteryItem)
    ISTimedActionQueue.add(ISInventoryTransferAction:new(getPlayer(), item, item:getContainer(),
        getPlayer():getInventory()))
    ISInventoryPaneContextMenu.equipWeapon(item, true, item:isRequiresEquippedBothHands(), 0)
    ISTimedActionQueue.add(ISGunAddBatteryAction:new(player, 20, item, partType, batteryItem))
end

-- Menu -----------------------------------------------------------------------------

-- Strips an option upstream may already have added, so a double-executed
-- BatterySet.lua cannot produce duplicate entries. See the header.
local function removeExisting(context, label)
    pcall(function()
        if context.removeOptionByName then
            context:removeOptionByName(label)
            return
        end
        for i = #context.options, 1, -1 do
            if context.options[i] and context.options[i].name == label then
                table.remove(context.options, i)
            end
        end
    end)
end

local function addOption(context, label, weapon, fn, ...)
    removeExisting(context, label)
    context:addOption(label, getPlayer(), fn, weapon, ...)
end

local function buildFor(context, weapon, partType, removeKey, addKey)
    local part = try(function() return weapon:getWeaponPart(partType) end, nil)
    if not part then
        return
    end

    local charge = Battery.readCharge(weapon, partType)
    if not charge then
        return
    end

    local removeLabel = getText(removeKey)
    local addLabel = getText(addKey)

    if charge > 0 then
        addOption(context, removeLabel, weapon, removeBattery, partType)
    else
        removeExisting(context, removeLabel)
    end

    -- P1: offer a top-up at any charge below full, not only at exactly 0.
    if charge < Battery.FULL then
        local candidate = pickBattery(Battery.FULL - charge)
        if candidate then
            addOption(context, addLabel, weapon, addBattery, partType, candidate)
        else
            removeExisting(context, addLabel)
        end
    else
        removeExisting(context, addLabel)
    end
end

local function onFillMenu(_player, _context, _items)
    local seen = {}
    for _, entry in ipairs(_items) do
        if not instanceof(entry, "InventoryItem") then
            for _, it in ipairs(entry.items) do
                seen[it] = true
            end
        else
            seen[entry] = true
        end
    end

    for item, _ in pairs(seen) do
        if instanceof(item, "HandWeapon") and try(function() return item:IsWeapon() end, false) and
            try(function() return item:isRanged() end, false) then

            buildFor(_context, item, "Laser", "ContextMenu_Remove_Battery_To_Laser",
                "ContextMenu_ADD_Battery_To_Laser")
            buildFor(_context, item, "Light", "ContextMenu_Remove_Battery_To_Light",
                "ContextMenu_ADD_Battery_To_Light")

            -- Upstream returned after the first gun as well; with several guns
            -- selected the options would be ambiguous anyway.
            return
        end
    end
end

local function install()
    if Battery._installed then
        return
    end
    Battery._installed = true

    local patched = patchAddAction()

    Events.OnFillInventoryObjectContextMenu.Add(onFillMenu)
    log("version " .. Battery.VERSION .. " installed; top-up allowed below " ..
        tostring(Battery.FULL) .. "; combine-and-cap " ..
        (patched and "ACTIVE" or "FAILED TO APPLY") ..
        "; light charge store " ..
        (MFSGunLightStore and "ACTIVE" or "MISSING - light battery will not persist"))
end

Events.OnGameStart.Add(install)
