-- MFS community fix: hotkey fire-mode selector.
--
-- Fire mode could previously only be changed through the weapon's right-click
-- context menu. This binds it to a configurable key, matching how the inspect
-- window is already bound (Options > Key Bindings > Escape From Kentucky Key
-- Settings > Switch Fire Mode). Default Backspace; the binding lives in AWCWF_KeyBind.lua.
--
-- Behaviour mirrors the context menu: it only calls weapon:setFireMode(), the
-- same setter the menu uses. No ammo, chamber, part, or save data is touched.
--
-- Every MFS select-fire weapon ships FireModePossibilities = Auto/Single (105 of
-- 105 gun scripts), so this toggles between those two. "Safe" is handled if a
-- weapon ever reports it, rather than assumed away.

MFSFireModeHotkey = MFSFireModeHotkey or {}

local Hotkey = MFSFireModeHotkey
Hotkey.VERSION = "1.0.0"
Hotkey.BIND = "SwitchFireModeCat"

local function log(message)
    print("[MFSFireMode] " .. tostring(message))
end

local function try(fn, fallback)
    local ok, value = pcall(fn)
    if ok then return value end
    return fallback
end

-- Returns the next mode, or nil if this weapon should not respond to the key.
local function nextMode(weapon)
    local current = try(function() return weapon:getFireMode() end, nil)
    if current == nil then return nil end
    current = tostring(current)

    if current == "Auto" then return "Single" end
    if current == "Single" then return "Auto" end
    -- A weapon parked on Safe (or an unexpected value) moves to Single, which is
    -- always the safer of the two live modes to hand back to the player.
    return "Single"
end

local function switchFireMode(playerObj)
    if not playerObj then return end

    local weapon = playerObj:getPrimaryHandItem()
    if not weapon then return end
    if not try(function() return weapon:IsWeapon() end, false) then return end
    if not try(function() return weapon:isRanged() end, false) then return end

    -- isSelectFire() is the engine's own "can this weapon change mode" test and
    -- is what gates the context-menu entry. Non-select-fire guns stay untouched.
    if not try(function() return weapon:isSelectFire() end, false) then return end
    if not weapon.setFireMode then return end

    local target = nextMode(weapon)
    if not target then return end

    local ok = pcall(function() weapon:setFireMode(target) end)
    if not ok then
        log("setFireMode failed on " .. tostring(try(function() return weapon:getType() end, "?")))
        return
    end

    -- Feedback, since there is no menu open to confirm the change.
    pcall(function()
        if HaloTextHelper and HaloTextHelper.addText then
            HaloTextHelper.addText(playerObj, getText("IGUI_MFS_FireMode_" .. target))
        end
    end)
    pcall(function() playerObj:playSound("MFSFireModeSwitch") end)
end

local function onKeyPressed(key)
    local core = getCore()
    if not core then return end
    if key ~= core:getKey(Hotkey.BIND) then return end
    switchFireMode(getPlayer())
end

local function install()
    if Hotkey._installed then return end
    Hotkey._installed = true
    Events.OnKeyPressed.Add(onKeyPressed)
    log("version " .. Hotkey.VERSION .. " installed; bind=" .. Hotkey.BIND)
end

Events.OnGameStart.Add(install)
