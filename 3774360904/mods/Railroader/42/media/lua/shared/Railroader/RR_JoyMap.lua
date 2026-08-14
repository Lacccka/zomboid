--***********************************************************************
-- Railroader / RR_JoyMap  -- WHICH PAD BUTTON DOES WHAT IN THE CAB, and nothing else.
--
-- Task 2.H step 2 (owner, 2026-08-07, reversing the "document the limit" decision of
-- 2026-08-05). The driving controls were raw Keyboard.KEY_*, so a Steam Deck player
-- could board the 800, sit in her, shelter in her and get out again -- but not move
-- her. This module is the half of the fix that can be TESTED WITHOUT A CONTROLLER:
-- the mapping table, the rebinding rules and the edge/hold bookkeeping are pure, so
-- the suite exercises them; RR_RideJoypad does the polling and the engine calls.
--
-- WHY A TABLE AND NOT SEVEN CONSTANTS: nobody here owns a pad. A default we cannot
-- try is a guess, and a guess baked into `if button == X` is a guess that needs a
-- rebuild to correct. Every action is therefore one console line away from moving --
-- RR.JoyMap.set("brake", "B") -- so the first person who actually holds a controller
-- can find the right layout in one session instead of one release cycle.
--
-- =====================================================================
-- THE CONSTRAINT THAT SHAPED THE WHOLE LAYOUT: WE CANNOT TAKE A BUTTON.
-- =====================================================================
-- Vanilla reads the pad in JAVA, not Lua. CharacterInputComponent:360-368 asks
-- CharacterJoypadButtonBinding.Melee/Attack.isDown(joypadBind) every tick, and
-- IsoPlayer:3959-3960 turns that straight into a swing. setBlockMovement(true) --
-- what we do to a seated driver -- does NOT gate it: it is a MOVEMENT-command flag
-- (IsoPlayer:6635, and the states that read it are the crawling/knocked-down ones).
-- So a button we "use" also does its vanilla job, and there is no Lua event in
-- between to swallow it.
--
-- The bindings CAN be rewritten from Lua (CharacterJoypadButtonBinding:setBinding is
-- @UsedFromLua) and that is exactly what we do NOT do: it is global, it survives our
-- own file, and if an exception lands between "unbind while seated" and "restore on
-- step-off" the player's pad is broken for everything -- walking, fighting, driving a
-- real car -- until he restarts the game. A locomotive mod is not allowed to do that.
--
-- What is left is what the tables below call the RISK of a button:
--
--   "free"   -- the DPad. Its four directions are dispatched in LUA
--               (JoypadControllerData:onPressUp -> ISDPadWheels.onDisplayUp, and the
--               same for down/left/right in JoyPadSetup:691-820), so RR_RideJoypad
--               can wrap those four functions and stop the vanilla wheel from opening
--               while the driver is seated. A real interception, not a hope.
--   "shared" -- the bumpers and the stick clicks. Vanilla still acts on them, but
--               while seated its action is inert or cosmetic: Sprint needs a movement
--               axis that setBlockMovement has already killed, ReloadWeapon/RackFirearm
--               need a firearm in the hands, Crouch is a pose. Usable, with the caveat
--               written down.
--   "taken"  -- A, B, X, Y, Back, Start, Guide. A is Interact, which is how the pad
--               boards and leaves the cab (RR_BoardMenu); X is InteractOptions, which
--               is how it opens our context menus; Y is the inventory. B looks free and
--               is not: besides CancelAction it carries ClimbThrough and SmashWindow
--               (ISButtonPrompt:390-394), so a press beside a window would try to put
--               a pinned driver through it. Nothing is bound here by default.
--
-- The defaults therefore live on the DPad plus three "shared" buttons, and the module
-- REFUSES to bind an action to a "taken" button unless the caller says force. That
-- refusal is the point: it is the one piece of this feature that a session without a
-- controller can still get right.
--
-- Pure: no engine calls, no globals but RR. Everything the engine touches --
-- JoypadButton[name]:isDown(id), the wheel wrappers, the halos -- is in
-- client/Railroader/RR_RideJoypad.lua.
--***********************************************************************

RR = RR or {}

local JoyMap = {}

--------------------------------------------------------------------------
-- The buttons. These are the ENUM CONSTANT NAMES of zombie/input/JoypadButton --
-- the enum is @UsedFromLua, and Lua indexes it by exactly these names:
-- JoypadButton[name]:isDown(joypadBind). Keep the spelling identical to the Java
-- or the lookup silently yields nil. (Note that vanilla's own `Joypad` table in
-- JoyPadSetup:13-28 renames them -- Joypad.AButton, Joypad.LBumper -- so do not
-- copy names from there.)
--------------------------------------------------------------------------
JoyMap.BUTTONS = {
    "A", "B", "X", "Y",
    "LeftStick", "RightStick",
    "LeftBump", "RightBump",
    "Back", "Start", "Guide",
    "DPadLeft", "DPadRight", "DPadUp", "DPadDown",
}

-- risk -> see the header. Only "free" and "shared" may carry a default binding.
JoyMap.RISK = {
    DPadUp = "free", DPadDown = "free", DPadLeft = "free", DPadRight = "free",
    RightStick = "shared", LeftStick = "shared",
    LeftBump = "shared", RightBump = "shared",
    A = "taken", B = "taken", X = "taken", Y = "taken",
    Back = "taken", Start = "taken", Guide = "taken",
}

-- What vanilla does with the button anyway, so RR.JoyMap.report() can tell a tester
-- what he is trading away before he moves something. Straight out of
-- CharacterJoypadButtonBinding (the enum's own default bindings) and ISDPadWheels.
JoyMap.VANILLA = {
    A          = "Interact (boards/leaves our cab)",
    B          = "CancelAction, ClimbThrough, SmashWindow, vehicle Brakes",
    X          = "InteractOptions (opens our context menus), Loot",
    Y          = "Inventory",
    LeftStick  = "Crouch",
    RightStick = "Sprint (needs the movement axis -- dead while seated)",
    LeftBump   = "RackFirearm, ManualFloorAtk (both need a weapon/target)",
    RightBump  = "ReloadWeapon (needs a firearm)",
    Back       = "game menus",
    Start      = "game menus",
    Guide      = "system",
    DPadUp     = "vehicle/animal radial menu",
    DPadDown   = "emote radial menu",
    DPadLeft   = "weapon + light wheel",
    DPadRight  = "map + zone wheel",
}

--------------------------------------------------------------------------
-- The actions. Order is the order RR.JoyMap.report() and the notebook print them,
-- which is the order a driver needs them in.
--
--   kind = "press"  -- acts once on the DOWN edge (a notch, a reverser step). The
--                      keyboard equivalents are OnKeyStartPressed edges too.
--   kind = "hold"   -- acts while the button is down and stops when it is released
--                      (the horn, which is a protracted sound, not a beep).
--
-- THE BRAKE IS THE ONE PLACE THE PAD DELIBERATELY DIFFERS FROM THE KEYBOARD: Space
-- is held, the pad's brake LATCHES (press = set, press again = release). Holding a
-- button through a two-minute descent is miserable on a pad, and a latching brake is
-- the more prototypical of the two anyway -- an independent brake handle on a real
-- locomotive stays where the engineer put it. e.brakeInput is already a 0/1 scalar
-- (RR_Ride.onTick), so a latch is exactly representable; the BRAKE PSI dial on the
-- control stand shows the state, so a latched brake cannot be forgotten silently.
--------------------------------------------------------------------------
JoyMap.ACTIONS = {
    { id = "notchUp",   kind = "press", default = "DPadUp",
      key = "W",     label = "Throttle up / start the diesel" },
    { id = "notchDown", kind = "press", default = "DPadDown",
      key = "S",     label = "Throttle down / shut down" },
    { id = "reverser",  kind = "press", default = "DPadLeft",
      key = "R",     label = "Reverser  N - FORWARD - REVERSE" },
    { id = "brake",     kind = "press", default = "RightStick",
      key = "Space", label = "Air brake (latches on the pad)" },
    { id = "cabMenu",   kind = "press", default = "DPadRight",
      key = "V",     label = "Cab menu: sleep, headlight, leave" },
    { id = "horn",      kind = "hold",  default = "LeftBump",
      key = "Q",     label = "Horn" },
    { id = "bell",      kind = "press", default = "RightBump",
      key = "L",     label = "Bell" },
}

--------------------------------------------------------------------------
-- Lookups. Kept as functions rather than exposed tables so the binding state has
-- exactly one owner and a typo cannot half-write it.
--------------------------------------------------------------------------
function JoyMap.isButton(name)
    if type(name) ~= "string" then return false end
    for _, b in ipairs(JoyMap.BUTTONS) do
        if b == name then return true end
    end
    return false
end

function JoyMap.action(id)
    for _, a in ipairs(JoyMap.ACTIONS) do
        if a.id == id then return a end
    end
    return nil
end

function JoyMap.risk(button)
    return JoyMap.RISK[button] or "unknown"
end

JoyMap.binding = {}

function JoyMap.reset()
    JoyMap.binding = {}
    for _, a in ipairs(JoyMap.ACTIONS) do
        JoyMap.binding[a.id] = a.default
    end
    return JoyMap.binding
end

JoyMap.reset()

function JoyMap.get(id)
    return JoyMap.binding[id]
end

-- actionOn(button): which action owns this button right now, or nil. This is what
-- the DPad wrappers ask before swallowing a press -- an unbound direction must keep
-- its vanilla wheel, even in the cab, or moving the cab menu off DPadRight would
-- silently cost the player the map wheel as well.
function JoyMap.actionOn(button)
    for _, a in ipairs(JoyMap.ACTIONS) do
        if JoyMap.binding[a.id] == button then return a.id end
    end
    return nil
end

--------------------------------------------------------------------------
-- set(id, button [, force]): rebind. button = nil / "none" / "off" unbinds.
-- Returns ok, message -- the message is for the console, always non-nil, because a
-- silent refusal in a debug helper is worse than no helper.
--------------------------------------------------------------------------
function JoyMap.set(id, button, force)
    local a = JoyMap.action(id)
    if not a then
        return false, "no such action: " .. tostring(id)
    end
    if button == nil or button == "none" or button == "off" then
        JoyMap.binding[id] = nil
        return true, id .. " unbound"
    end
    if not JoyMap.isButton(button) then
        return false, "no such button: " .. tostring(button)
            .. " (see RR.JoyMap.BUTTONS)"
    end
    local risk = JoyMap.risk(button)
    if risk == "taken" and not force then
        return false, button .. " is spoken for by vanilla -- "
            .. (JoyMap.VANILLA[button] or "?")
            .. ". Pass true as the third argument to take it anyway."
    end
    local held = JoyMap.actionOn(button)
    if held and held ~= id then
        return false, button .. " is already the " .. held
            .. " control -- unbind that first"
    end
    JoyMap.binding[id] = button
    local note = ""
    if risk == "shared" then
        note = "  (vanilla also: " .. (JoyMap.VANILLA[button] or "?") .. ")"
    elseif risk == "taken" then
        note = "  (FORCED over vanilla: " .. (JoyMap.VANILLA[button] or "?") .. ")"
    end
    return true, id .. " -> " .. button .. note
end

--------------------------------------------------------------------------
-- edges(prev, now): the whole of our input bookkeeping, and the reason it is here
-- rather than in the client file -- it is where an off-by-one costs you a runaway
-- throttle, and it is testable without a controller.
--
-- prev/now are button-name -> boolean maps (a missing key reads as false, so the
-- caller may poll only the buttons that are bound). The result is action-id keyed:
--   .down     -- the button is down THIS tick   (hold actions ride this)
--   .pressed  -- it went down between the ticks (press actions ride this)
--   .released -- it came up between the ticks   (the horn stops on this)
-- An unbound action appears with all three false, so callers never nil-check.
--------------------------------------------------------------------------
function JoyMap.edges(prev, now)
    prev = prev or {}
    now  = now  or {}
    local out = {}
    for _, a in ipairs(JoyMap.ACTIONS) do
        local b = JoyMap.binding[a.id]
        local wasDown = (b ~= nil) and (prev[b] == true)
        local isDown  = (b ~= nil) and (now[b]  == true)
        out[a.id] = {
            down     = isDown,
            pressed  = isDown and not wasDown,
            released = wasDown and not isDown,
        }
    end
    return out
end

-- buttonsInUse(): the set of button names to poll this tick. Polling only what is
-- bound keeps the per-tick work at seven isDown calls whatever the layout is.
function JoyMap.buttonsInUse()
    local t = {}
    for _, a in ipairs(JoyMap.ACTIONS) do
        local b = JoyMap.binding[a.id]
        if b then t[#t + 1] = b end
    end
    return t
end

--------------------------------------------------------------------------
-- lines(): the console report and the "what does this pad do" answer, as plain
-- strings so the suite can read them. RR_RideJoypad prints them.
--------------------------------------------------------------------------
function JoyMap.lines()
    local out = {}
    for _, a in ipairs(JoyMap.ACTIONS) do
        local b = JoyMap.binding[a.id]
        local kind = (a.kind == "hold") and " (hold)" or ""
        if b then
            out[#out + 1] = string.format("%-10s %-11s%-7s %s  [keyboard: %s]",
                a.id, b, kind, a.label, a.key)
        else
            out[#out + 1] = string.format("%-10s %-11s%-7s %s  [keyboard: %s]",
                a.id, "-- unbound", "", a.label, a.key)
        end
    end
    return out
end

RR.JoyMap = JoyMap

return JoyMap
