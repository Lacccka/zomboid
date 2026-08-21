-- MFS community fix: modifier-aware key binding check.
--
-- THE PROBLEM
-- -----------
-- Build 42 added modifier support to key bindings - Shift, Ctrl or Alt, one
-- modifier per binding. Every MFS hotkey ignored it, testing only:
--
--     key == getCore():getKey("SomeBind")
--
-- getKey() returns the BARE key code. A binding of Shift+F stores keyValue 33
-- with a separate shift flag, so pressing F ALONE still matched and fired the
-- action. Reported in testing: "setting the key to Shift+F, pressing F alone
-- still triggers the function." Vanilla does not behave this way - a movement
-- key bound to Shift+W correctly ignores a bare W.
--
-- THE ENGINE DOES NOT FILTER FOR US
-- ---------------------------------
-- Confirmed by probe. OnKeyPressed fires with the bare key code regardless of
-- which modifiers are physically held:
--
--     OnKeyPressed  key=33  getKey(BIND)=33  modifiers held=none
--     OnKeyPressed  key=33  getKey(BIND)=33  modifiers held=LSHIFT
--
-- So the modifier test has to happen here.
--
-- THE ACCESSOR
-- ------------
-- Found by probe, because no reachable javadoc documents it - every mirror is
-- B41-era, and the official modding site is JavaScript-rendered and returns an
-- empty document to a plain fetch. Of nine candidates tried in game, exactly
-- one worked:
--
--     getCore():getKeyBinding(name)
--       -> KeyBinding[name=SwtichLightSet, keyValue=33, altKey=0,
--                     shift=true, ctrl=false, alt=false]
--
-- These FAILED with java.lang.RuntimeException. Do not try them again:
--     getKeyModifier, getKeyModifiers, getKeyBind, getKeyWithModifier,
--     getKeyMaps, getKeyModifierMaps
--
-- TRAP: "altKey=0" is a SECONDARY KEY slot, not the Alt modifier. The Alt
-- modifier is the separate "alt" flag. The patterns below are anchored on the
-- preceding comma so "altKey" can never be mistaken for "alt".
--
-- WHY THIS PARSES toString RATHER THAN CALLING GETTERS
-- ----------------------------------------------------
-- Version 1.0.0 tried a list of plausible getter names - isShift, getShift,
-- isShiftDown and so on - inside pcall, intending to fall back to toString.
-- That shipped and produced this, four times per keypress:
--
--     ERROR: General Lua((MOD: MFS_fix_beta)).try> Exception thrown
--         readFlagsByGetter(MFSKeyBindModifiers.lua:97)
--
-- >>> pcall CATCHES a Java exception in Lua, but PZ STILL LOGS IT as a console
--     ERROR. A caught exception is NOT a silent one.
--
-- That makes speculative method calls acceptable in a one-shot diagnostic
-- probe and unacceptable in a runtime path that runs on every key press. The
-- getter probing is therefore gone. toString is the only route: it exists on
-- every Java object, throws nothing, and its exact format is known from the
-- probe output quoted above.
--
-- If the format ever changes, the match fails, getRequired returns nil, and
-- satisfied() answers true - i.e. the hotkey degrades to its old behaviour
-- rather than swallowing the player's keypress.
--
-- MATCHING RULE - ASYMMETRIC, ON PURPOSE
-- --------------------------------------
--     binding HAS a modifier  -> that modifier MUST be held
--     binding has NO modifier -> modifier state is IGNORED
--
-- This mirrors vanilla. A bare binding still fires while Shift is held, so a
-- player who toggles the light while sprinting is unaffected; that is existing
-- behaviour and breaking it would be a regression nobody asked for.
--
-- Set MFSKeyBindModifiers.STRICT = true to make a bare binding refuse to fire
-- while any modifier is held. Off by default: behaviour change, not bug fix.

MFSKeyBindModifiers = MFSKeyBindModifiers or {}

local Mods = MFSKeyBindModifiers

Mods.VERSION = "1.1.0"
Mods.STRICT = false

local function log(message)
    print("[MFSKeyBind] " .. tostring(message))
end

-- Deliberately narrow. Only ever wraps calls that are KNOWN to exist, so it
-- cannot generate the caught-but-logged exceptions described above.
local function try(fn, fallback)
    local ok, value = pcall(fn)
    if ok then
        return value
    end
    return fallback
end

local warnedUnparsed = false

-- Anchored on ", " so altKey cannot be read as alt. Format is known exactly:
--   KeyBinding[name=X, keyValue=33, altKey=0, shift=true, ctrl=false, alt=false]
local function parseFlags(text)
    local shift = text:match(",%s*shift=(%a+)")
    local ctrl = text:match(",%s*ctrl=(%a+)")
    local alt = text:match(",%s*alt=(%a+)")

    if not shift or not ctrl or not alt then
        return nil
    end

    return {
        shift = (shift == "true"),
        ctrl = (ctrl == "true"),
        alt = (alt == "true")
    }
end

-- Returns {shift=,ctrl=,alt=} for a binding, or nil if it cannot be determined.
function Mods.getRequired(bindName)
    local core = getCore()
    if not core then
        return nil
    end

    -- getKeyBinding is confirmed present; see the header. Still guarded, since
    -- an unknown bind name is a plausible caller error.
    local binding = try(function() return core:getKeyBinding(bindName) end, nil)
    if not binding then
        return nil
    end

    local text = try(function() return tostring(binding) end, nil)
    if type(text) ~= "string" then
        return nil
    end

    local flags = parseFlags(text)

    if not flags and not warnedUnparsed then
        warnedUnparsed = true
        log("could not read modifier flags from: " .. text ..
            "  - hotkeys will ignore modifiers until this is fixed")
    end

    return flags
end

-- True when the currently held modifiers satisfy the binding.
function Mods.satisfied(bindName)
    local required = Mods.getRequired(bindName)
    if not required then
        -- Cannot determine the binding. Behave exactly as before rather than
        -- silently swallowing the player's keypress.
        return true
    end

    if not (required.shift or required.ctrl or required.alt) then
        if Mods.STRICT then
            return not (isShiftKeyDown() or isCtrlKeyDown() or isAltKeyDown())
        end
        return true
    end

    if required.shift ~= (isShiftKeyDown() == true) then
        return false
    end
    if required.ctrl ~= (isCtrlKeyDown() == true) then
        return false
    end
    if required.alt ~= (isAltKeyDown() == true) then
        return false
    end

    return true
end

-- Convenience: the whole test a hotkey handler needs.
function Mods.matches(key, bindName)
    local core = getCore()
    if not core then
        return false
    end
    if key ~= core:getKey(bindName) then
        return false
    end
    return Mods.satisfied(bindName)
end

log("version " .. Mods.VERSION .. " loaded; strict=" .. tostring(Mods.STRICT))
