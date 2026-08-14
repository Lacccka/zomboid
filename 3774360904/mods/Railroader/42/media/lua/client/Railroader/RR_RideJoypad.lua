--***********************************************************************
-- Railroader / RR_RideJoypad  -- DRIVING HER WITH A CONTROLLER (Task 2.H step 2)
--
-- Owner, 2026-08-07, reversing the decision of 2026-08-05 ("document the limit,
-- build no control scheme until someone here has a pad"). Step 1 gave a pad player
-- a way IN (RR_BoardMenu) and a way into our menus; he still could not move her,
-- because throttle, brake, reverser, horn and bell were raw Keyboard.KEY_*.
--
-- THE LAYOUT AND THE REASONING BEHIND IT LIVE IN shared/Railroader/RR_JoyMap.lua.
-- Read that header first: it is where the "we cannot take a button from vanilla"
-- constraint is written down, and it is the half of this feature the test suite can
-- actually check. This file is the engine half -- the per-tick poll, the wheel
-- interception and the calls into the cab.
--
-- HOW IT WORKS, IN ONE PARAGRAPH: while the driver is seated, once per tick, we ask
-- JoypadButton[name]:isDown(joypadBind) for the seven bound buttons, hand the before
-- and after to RR.JoyMap.edges, and turn the resulting presses into the SAME calls
-- the keys make -- RR_Ride.control, RR_CabMenu.show, RR_Sound.hornStart/toggleBell.
-- Not one control rule is re-implemented here. That is deliberate and it is the
-- answer to "nobody here has a controller": whatever the pad can reach is code that
-- has already been driven, in game, from the keyboard.
--
-- THE FOUR DPAD DIRECTIONS ARE *INTERCEPTED*, NOT SHARED. Vanilla dispatches them in
-- Lua -- JoypadControllerData:onPressUp -> ISDPadWheels.onDisplayUp, and the same for
-- down/left/right (JoyPadSetup:691, :736, :777, :820) -- so we wrap those four
-- functions and swallow the ones we have bound WHILE SEATED. Without that, notching
-- the throttle would also open the vehicle radial on every press.
--
-- AND IT FIXES A LIVE BUG FOR PAD PLAYERS ON FOOT. ISDPadWheels.onDisplayUp calls
-- ISVehicleMenu.showRadialMenu and, if that came up empty, AnimalContextMenu.
-- showRadialMenu -- so DPad-up at the 800 opened the LIVESTOCK radial on her ("Pet",
-- "Animal Info", and with a weapon in hand "Kill Animal"). That is precisely the bug
-- RR_CabMenu fixed for the V key in Task 1.O; the fix only ever covered the keyboard,
-- because V is a key and this is not. Same rule, same shape, now on the pad path.
--
-- MP: gated like every other entry point (RR_MP). On a connected client the mod
-- stands down, and a control scheme for a locomotive that will not answer is worse
-- than no control scheme -- so we neither poll nor wrap.
--
-- NOT VERIFIED IN-GAME. Nobody here owns a controller; the layout is a considered
-- guess and every binding is one console line from moving:
--     RR.RideJoypad.report()                  -- pad, seat, and the whole layout
--     RR.JoyMap.set("brake", "B", true)       -- rebind (true = take it from vanilla)
--     RR.JoyMap.reset()                       -- back to the shipped defaults
--***********************************************************************

require("Railroader/RR_JoyMap")

local Joy = {}

RR = RR or {}
RR.RideJoypad = Joy

local function blocked()
    return (RR.MP and RR.MP.blocked()) and true or false
end

local function asleep()
    return (RR.Sleep and RR.Sleep.isAsleep()) and true or false
end

--------------------------------------------------------------------------
-- Button lookup. JoypadButton is a Java enum exposed to Lua (@UsedFromLua), indexed
-- by its constant names -- JoypadButton.DPadUp:isDown(bind). Resolved ONCE and
-- cached: the miss case (a name that is not in the enum) must not be a per-tick
-- pcall, and a nil here has to be visible in the console at load, not swallowed
-- sixty times a second.
--------------------------------------------------------------------------
local BTN = {}

-- btn(name): the enum constant, resolved ON DEMAND and cached. Lazy rather than
-- resolved once at OnGameStart on purpose -- that would make every control in this
-- file depend on one event having fired first, and a load-order surprise (or a
-- console `RR.RideJoypad._tick()` before the world is up) would then read as "the
-- pad does nothing" with nothing in the log to say why. `false` is cached for a
-- name the enum does not have, so a miss costs one lookup, not one per tick.
local function btn(name)
    if name == nil then return nil end
    local b = BTN[name]
    if b ~= nil then return b or nil end
    local found
    pcall(function() found = JoypadButton and JoypadButton[name] end)
    BTN[name] = found or false
    if not found then
        print("[Railroader] RR_RideJoypad: JoypadButton." .. tostring(name)
              .. " does not exist -- that binding is dead")
    end
    return found
end

local function resolveButtons()
    local ok = true
    for _, name in ipairs(RR.JoyMap.BUTTONS) do
        if not btn(name) then ok = false end
    end
    return ok
end

--------------------------------------------------------------------------
-- padId(): the joypad "bind" index this player's controller sits on, or nil when he
-- is on the keyboard. -1 is vanilla's "no pad" (ISInventoryPage and the tutorial
-- both test it exactly this way), and it is the ONLY gate we need: a player with no
-- controller never reaches a single line below, so a keyboard game pays one method
-- call per tick for all of this.
--------------------------------------------------------------------------
function Joy.padId()
    local p = getPlayer()
    if not p then return nil end
    local id = -1
    pcall(function() id = p:getJoypadBind() end)
    if not id or id < 0 then return nil end
    return id
end

-- uiHasPad(): is some UI holding the joypad focus (our own radial included)? While a
-- menu owns the pad, its DPad presses are NAVIGATION -- JoypadControllerData:onPressUp
-- hands them to joypadData.focus and never reaches the wheels. If we kept polling, a
-- driver picking "Sleep in the cab" out of the radial would notch the throttle up on
-- the way to it.
local function uiHasPad()
    local p = getPlayer()
    if not p then return false end
    local has = false
    pcall(function()
        local d = getJoypadData(p:getPlayerNum())
        has = (d ~= nil) and (d.focus ~= nil)
    end)
    return has
end

--------------------------------------------------------------------------
-- State. `prev` is the previous tick's button map -- the whole memory of this file.
-- It is cleared whenever we stop polling (step-off, sleep, a menu taking the pad) so
-- that resuming can never manufacture a press edge out of a button that was already
-- held: releasing the DPad inside a radial and coming back to the cab must not notch.
--------------------------------------------------------------------------
local prev  = {}
local hornDown = false

local function clearState()
    prev     = {}
    hornDown = false                -- RR_Sound's own edge detector reads hornDown()
end

-- Read by RR_Sound's horn tick, which OR-s it with the Q key so there is exactly one
-- pair of hornStart/hornStop callers in the mod (see the comment there).
function Joy.hornDown()
    return hornDown
end

--------------------------------------------------------------------------
-- The brake is a LATCH on the pad, not a hold (RR_JoyMap's header explains why).
-- e.brakeLatch is OR-ed into e.brakeInput by RR_Ride.onTick and cleared on step-off
-- and on derailment, so nothing here has to unwind it.
--------------------------------------------------------------------------
-- A latch you cannot see is a latch you leave on, so each flip says so in the halo --
-- the same channel every other cab refusal and confirmation uses. (addGoodText /
-- addText, the two-argument overloads this codebase already calls; the arrow variants
-- exist but pick between eight overloads, which Kahlua resolves by argument count.)
local function toggleBrake(e)
    e.brakeLatch = (not e.brakeLatch) and true or nil
    print("[Railroader] pad brake " .. (e.brakeLatch and "SET" or "RELEASED"))
    local p = getPlayer()
    if not (p and HaloTextHelper) then return end
    pcall(function()
        if e.brakeLatch then
            HaloTextHelper.addGoodText(p, getText("IGUI_RR_PadBrakeOn"))
        else
            HaloTextHelper.addText(p, getText("IGUI_RR_PadBrakeOff"))
        end
    end)
end

--------------------------------------------------------------------------
-- The tick. Seven isDown calls and a table compare when a pad player is in the cab;
-- one getJoypadBind when he is not.
--------------------------------------------------------------------------
local function onTick()
    if blocked() then return end

    local e = RR.Ride and RR.Ride.current
    if not e then clearState(); return end

    local id = Joy.padId()
    if not id then clearState(); return end

    -- Asleep: swallow everything, exactly as the key handlers do. The horn must be
    -- released first, or a driver who dozed off mid-blast would sound it all night.
    if asleep() or uiHasPad() then
        if hornDown and RR.Sound then pcall(function() RR.Sound.hornStop(e) end) end
        clearState()
        return
    end

    local now = {}
    for _, name in ipairs(RR.JoyMap.buttonsInUse()) do
        local b = btn(name)
        if b then
            local down = false
            pcall(function() down = b:isDown(id) end)
            now[name] = down
        end
    end

    local ed = RR.JoyMap.edges(prev, now)
    prev = now

    -- Throttle and reverser. One step per press, every rule (starter latch, reverser
    -- interlock, shutdown gate, licence, derailment) inside RR_Ride.control.
    if ed.notchUp.pressed   then RR.Ride.control(e, "up")       end
    if ed.notchDown.pressed then RR.Ride.control(e, "down")     end
    if ed.reverser.pressed  then RR.Ride.control(e, "reverser") end

    if ed.brake.pressed then toggleBrake(e) end

    -- The cab radial: sleep, headlight, start/shut down, leave. Opened with the pad's
    -- own button so releasing it picks the highlighted slice -- vanilla's hold-and-pick
    -- gesture, and the reason CabMenu.show takes the button rather than assuming it.
    if ed.cabMenu.pressed and RR.CabMenu then
        local hideOn = btn(RR.JoyMap.get("cabMenu"))
        pcall(function() RR.CabMenu.show(e, hideOn) end)
    end

    -- Horn: HELD. We only carry the flag; RR_Sound owns start and stop.
    hornDown = ed.horn.down and true or false

    if ed.bell.pressed and RR.Sound then
        pcall(function() RR.Sound.toggleBell() end)
    end
end

--------------------------------------------------------------------------
-- The DPad wheels. Wrapped, not replaced: every direction we have NOT bound keeps
-- its vanilla wheel even in the cab (a player who moves the cab menu off DPad-right
-- gets the map wheel back there, which is what he asked for by unbinding it), and
-- off the loco nothing changes at all -- except the one thing that was broken.
--
-- Patched on OnGameStart rather than at load, for the reason RR_CabMenu documents:
-- the vanilla globals may not exist yet when a mod file runs.
--------------------------------------------------------------------------
local function isOurAnimal(a)
    if not a then return false end
    local t
    pcall(function() t = a:getAnimalType() end)
    if type(t) ~= "string" then return false end
    return t:sub(1, 3) == "rr_"
end

-- swallow(dir, isRadialDir): should this DPad press be OURS this tick?
--   seated  -- yes, if RR_JoyMap has that direction bound (our tick poll acts on it)
--   on foot -- yes for the ONE direction that opens vanilla's radial, and only when
--              the "useable animal" it would open on is one of ours. Same test, same
--              reason and the same shape as RR_CabMenu's on-foot branch. The other
--              three keep their wheels on foot: swallowing the map wheel because a
--              locomotive happens to be parked nearby would be our bug, not a fix.
local function swallow(dir, isRadialDir)
    if blocked() then return false end
    if RR.Ride and RR.Ride.current then
        return RR.JoyMap.actionOn(dir) ~= nil
    end
    if not isRadialDir then return false end
    local p = getPlayer()
    if not p or p:isDead() then return false end
    local a
    pcall(function() a = p:getUseableAnimal() end)
    return isOurAnimal(a)
end

local function patchWheels()
    -- Resolve the enum first and OUTSIDE the stand-down test: report() has to be able
    -- to answer "is this button name even real" on a client where the mod is off.
    resolveButtons()
    if blocked() then return end                       -- stood down: the DPad stays vanilla's
    if not ISDPadWheels or ISDPadWheels.rrPatched then return end

    local dirs = {
        onDisplayUp    = "DPadUp",
        onDisplayDown  = "DPadDown",
        onDisplayLeft  = "DPadLeft",
        onDisplayRight = "DPadRight",
    }
    for fn, dir in pairs(dirs) do
        local vanilla = ISDPadWheels[fn]
        if vanilla then
            ISDPadWheels[fn] = function(joypadData)
                -- Only the UP wheel has an on-foot case: it is the one that falls
                -- through to the livestock radial. Down/left/right are the emote,
                -- weapon and map wheels, which have no business with our animals.
                if swallow(dir, dir == "DPadUp") then return end
                return vanilla(joypadData)
            end
        end
    end
    ISDPadWheels.rrPatched = true
    print("[Railroader] RR_RideJoypad: DPad wheels wrapped (cab controls + the livestock-radial fix)")
end
Events.OnGameStart.Add(patchWheels)
Events.OnTick.Add(onTick)

-- Exposed for tests/harness_joypad.lua, which stands a fake pad, a fake seat and a
-- fake ISDPadWheels around this file and steps the tick by hand. It is the only way
-- anything here gets exercised before someone with a controller runs it, so the two
-- entry points the harness needs are exported rather than left local. Also a usable
-- console probe: RR.RideJoypad._tick() runs exactly one input frame.
Joy._tick        = onTick
Joy._patchWheels = patchWheels

--------------------------------------------------------------------------
-- report(): the console answer to "what does my controller do in this cab", and the
-- first thing to ask for in a bug report from a pad. Not gated -- a console helper
-- cannot be hit by accident, and this is how a tester tells "no pad detected" apart
-- from "the layout is wrong".
--------------------------------------------------------------------------
function Joy.report()
    print("---- Railroader / joypad ----")
    print("mod stood down (MP guard): " .. tostring(blocked()))
    local id = Joy.padId()
    print("joypad bind: " .. (id and tostring(id) or "none (keyboard)"))
    print("seated in a cab: " .. tostring((RR.Ride and RR.Ride.current) ~= nil))
    print("a UI holds the pad: " .. tostring(uiHasPad()))
    print("wheels wrapped: " .. tostring(ISDPadWheels and ISDPadWheels.rrPatched == true))
    for _, line in ipairs(RR.JoyMap.lines()) do print("  " .. line) end
    print("rebind: RR.JoyMap.set(\"brake\", \"B\", true)   reset: RR.JoyMap.reset()")
    print("-----------------------------")
end

print("[Railroader] RR_RideJoypad.lua: loaded OK (cab controls on a controller -- RR.RideJoypad.report())")

return Joy
