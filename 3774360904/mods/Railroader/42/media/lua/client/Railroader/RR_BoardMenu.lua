--***********************************************************************
-- Railroader / RR_BoardMenu  -- ways into the cab that are NOT the E key
-- (player report, Steam Deck, 2026-08-05: "im on steam deck n there no way to get
-- into it if u could add a option to where u can get in like car or could u add it
-- in a action like as if you was to build n sit?")
--
-- THE PROBLEM: boarding was a raw keyboard scancode and nothing else --
-- Keyboard.KEY_E in RR_Ride.onKeyDown. A player on a controller never sends one, so
-- for him the locomotive had no door at all: he could walk up to it, right-click it
-- (nothing), press every button on the pad (nothing). A car does not behave that
-- way, and he said so.
--
-- TWO WAYS IN, both of them the vanilla channels a car already uses:
--
--   1. THE WORLD CONTEXT MENU -- "Climb into the cab" / "Leave the cab". Reached with
--      the mouse (right-click near the loco) AND with a controller: the interact
--      button builds the very same menu out of the player's own square plus the three
--      in front of him (ISButtonPrompt:interact -> getInteractOptionsButtonObjects
--      -> ISContextManager.createWorldMenu). For that path the handler MUST answer
--      the TEST call -- ISWorldObjectContextMenu.setTest() -- because the prompt only
--      opens a menu when some handler says it has something to add
--      (ISWorldObjectContextMenu.createMenu:216, and the comment at :122 says so in
--      as many words: "This is for controller users").
--
--   2. THE ON-SCREEN INTERACT PROMPT -- the same button, and the same wording, that a
--      car gets when you stand at its door. ISButtonPrompt:getBestInteractButtonAction
--      is Lua, and its FIRST act is exactly this special case for a vehicle
--      (:442-446: in one => "Exit"). We wrap it: seated in our cab, the prompt is
--      "Leave the cab"; on foot within reach of the hull it is "Climb into the cab" --
--      but ONLY if vanilla found nothing else to offer, so we never steal a door, a
--      window or a corpse from under the player's thumb.
--
-- REACH IS RR_Ride.MOUNT_REACH, THE SAME TWO TILES THE E KEY USES, measured off the
-- DRAWN HULL (RR.Body.hullDistance), never a radius from the loco's centre -- the body
-- is ~15 tiles long, so a centre radius cannot mean the same thing at the cab door and
-- off the nose. The offer and the key therefore always agree.
--
-- NO WALK-TO, on purpose. Vanilla's "walk over and get in" leans on a parked object
-- with a fixed door square; ours is a 15-tile body that may be rolling, whose cab end
-- swaps with the reverser, and whose footprint is a chord of the rails recomputed every
-- tick. Pathing to a moving target and boarding at the end of it is a mechanic, not a
-- menu entry -- so the entry appears when you are already standing at her, which is
-- what E has always required.
--
-- The ANIMAL path (right-clicking one of our own animals: the rr_loco body itself, or one
-- of the two rr_truck entities standing at its ends since Task 3.C) comes in through
-- RR_AnimalMenuFilter, the same hook RR_RerailMenu uses -- with the same de-dupe, because
-- both paths can fire for one click.
--
-- Gated on RR_MP like every other entry point: on a connected client the mod stands
-- down and offers nothing.
--***********************************************************************

print("[Railroader] RR_BoardMenu.lua: loading...")

local BoardMenu = {}

--------------------------------------------------------------------------
-- txt(key, fallback): getText echoes the key back when a string is missing, so every
-- label falls back to readable English instead of "IGUI_RR_...". (Same helper as
-- RR_CabMenu / RR_RerailMenu -- one string is not worth a shared module.)
--------------------------------------------------------------------------
local function txt(key, fallback)
    local s = key
    pcall(function() s = getText(key) end)
    if not s or s == "" or s == key then return fallback or key end
    return s
end

local function blocked()
    return (RR.MP and RR.MP.blocked()) and true or false
end

-- Asleep in the cab: no interaction at all, exactly as vanilla bails out of its own
-- prompt and menu handlers on isAsleep().
local function asleep()
    return (RR.Sleep and RR.Sleep.isAsleep()) and true or false
end

function BoardMenu.seated()
    return (RR.Ride and RR.Ride.current) or nil
end

-- The loco we could climb into from where we stand, plus the distance to her hull -- or
-- nil. WRAPPED IN pcall, unlike the E key's identical call, and the reason is worth the
-- closure: this runs inside a VANILLA per-frame callback (getBestInteractButtonAction,
-- once per button binding), and it reaches into a Java IsoAnimal that a chunk unload or a
-- despawn could have taken away. A throw there is not a missing menu entry -- it is an
-- error every frame, which is exactly the bug this file was opened to fix.
function BoardMenu.boardable()
    if not (RR.Ride and RR.Ride.nearestBoardable) then return nil end
    local rec, dist
    pcall(function() rec, dist = RR.Ride.nearestBoardable(RR.Ride.MOUNT_REACH or 2.0) end)
    return rec, dist
end

--------------------------------------------------------------------------
-- The two commands. Both go through RR_Ride.toggle's own halves, so the menu, the
-- controller prompt and the E key are one behaviour: leaving a ROLLING loco cuts the
-- throttle, sets the brake and steps off once she is at rest (requestDismount).
--------------------------------------------------------------------------
function BoardMenu.doBoard(e)
    if not (e and RR.Ride and RR.Ride.mountRecord) then return end
    RR.Ride.mountRecord(e)
end

function BoardMenu.doLeave()
    if RR.Ride and RR.Ride.requestDismount then RR.Ride.requestDismount() end
end

-- Context-menu option shapes. ISContextMenu passes the option's param1 first, so the
-- world path (param1 = worldobjects) and the animal path (param1 = the record) need
-- their own entry points into the same call.
function BoardMenu.onBoard(worldobjects, e)  BoardMenu.doBoard(e) end
function BoardMenu.onBoardTarget(e)          BoardMenu.doBoard(e) end
function BoardMenu.onLeave(worldobjects)     BoardMenu.doLeave()  end

--------------------------------------------------------------------------
-- offered(context, label): is this entry already on the menu? The animal path runs
-- first and once PER clicked animal (the engine collects them from the 3x3, and the
-- hull is lined with collider segments), then the world path runs -- so without this
-- one right-click on the flank would sprout four identical entries.
--------------------------------------------------------------------------
local function offered(context, label)
    for i = 1, (context.numOptions or 1) - 1 do
        local o = context.options and context.options[i]
        if o and o.name == label then return true end
    end
    return false
end

--------------------------------------------------------------------------
-- OnFillWorldObjectContextMenu: (playerNum, context, worldobjects, test)
--
-- Anchored on where the PLAYER stands, not on which square was clicked. That is the
-- deliberate difference from RR_RerailMenu (which has to match a click against a
-- wreck): boarding is "can I reach the door from here", the click only says "I am
-- asking about the world in front of me". It is also what makes the controller path
-- work at all -- there is no cursor there, only the squares the survivor is facing.
--------------------------------------------------------------------------
function BoardMenu.OnFill(playerNum, context, worldobjects, test)
    if blocked() or asleep() then return end

    local e = BoardMenu.seated()
    if e then
        -- In the cab: every click is a click at the loco, so the way out is always here.
        if test then return ISWorldObjectContextMenu.setTest() end
        local label = txt("IGUI_RR_LeaveCab", "Leave the cab")
        if offered(context, label) then return end
        context:addOption(label, worldobjects, BoardMenu.onLeave)
        return
    end

    local rec = BoardMenu.boardable()
    if not rec then return end
    if test then return ISWorldObjectContextMenu.setTest() end
    local label = txt("IGUI_RR_BoardCab", "Climb into the cab")
    if offered(context, label) then return end
    context:addOption(label, worldobjects, BoardMenu.onBoard, rec)
end

--------------------------------------------------------------------------
-- addForAnimal(playerNum, context, animal, test): the ANIMAL path -- called from
-- RR_AnimalMenuFilter at the point where it drops the vanilla livestock menu for one
-- of ours, i.e. when the click landed ON the locomotive (or on one of the invisible
-- collider segments lining her). The reach test is still the player's, so clicking a
-- loco from across the yard offers nothing.
--------------------------------------------------------------------------
function BoardMenu.addForAnimal(playerNum, context, animal, test)
    if blocked() or asleep() then return end
    if not context then return end

    if BoardMenu.seated() then
        if test then return ISWorldObjectContextMenu.setTest() end
        local label = txt("IGUI_RR_LeaveCab", "Leave the cab")
        if offered(context, label) then return end
        context:addOption(label, nil, BoardMenu.onLeave)
        return
    end

    local rec = BoardMenu.boardable()
    if not rec then return end
    if test then return ISWorldObjectContextMenu.setTest() end
    local label = txt("IGUI_RR_BoardCab", "Climb into the cab")
    if offered(context, label) then return end
    context:addOption(label, rec, BoardMenu.onBoardTarget)
end

Events.OnFillWorldObjectContextMenu.Add(BoardMenu.OnFill)

--------------------------------------------------------------------------
-- THE CONTROLLER'S INTERACT PROMPT (path 2 in the header).
--
-- The prompt functions are methods on ISButtonPrompt (one instance per player), so the
-- patch goes on the class -- the same shape as RR_Ride's claim on ItemBindingHandler.
-- On OnGameStart, not at load: this is a vanilla global we are wrapping, and the file
-- that defines it is not guaranteed to have run when a mod's client Lua is compiled.
--
-- setButtonBindingPrompt(CharacterJoypadButtonBinding.Interact, ...) is verbatim what
-- vanilla does for "Exit Vehicle" (ISButtonPrompt:444), so a player who has rebound
-- his interact button gets the loco on the button he chose.
--------------------------------------------------------------------------

-- Did vanilla already put something on the interact button? Vanilla asks this as
-- `self.aPrompt` (:464), which is only true while Interact is bound to A -- the
-- default. We ask it of whichever button the binding actually resolves to.
local function interactPromptTaken(prompt)
    local taken = nil
    pcall(function()
        local b = CharacterJoypadButtonBinding.Interact:getJoypadButton()
        if     b == JoypadButton.A         then taken = prompt.aPrompt
        elseif b == JoypadButton.B         then taken = prompt.bPrompt
        elseif b == JoypadButton.X         then taken = prompt.xPrompt
        elseif b == JoypadButton.Y         then taken = prompt.yPrompt
        elseif b == JoypadButton.LeftBump  then taken = prompt.lbPrompt
        elseif b == JoypadButton.RightBump then taken = prompt.rbPrompt
        end
    end)
    return taken ~= nil
end

-- The prompt calls its function as a METHOD on the ISButtonPrompt (self:aFunc(...)),
-- so these take the prompt as their first argument and ignore it.
function BoardMenu.promptLeave(prompt)      BoardMenu.doLeave() end
function BoardMenu.promptBoard(prompt, e)   BoardMenu.doBoard(e) end

local function claimInteractPrompt()
    if blocked() then return end                       -- stood down: the pad stays vanilla's (RR_MP)
    if not ISButtonPrompt or ISButtonPrompt.rrBoardPatched then return end
    local vanilla = ISButtonPrompt.getBestInteractButtonAction
    if not vanilla then return end

    ISButtonPrompt.getBestInteractButtonAction = function(self, dir)
        if not asleep() then
            -- Seated: the way out, before vanilla -- exactly where vanilla puts its own
            -- "Exit Vehicle" special case, and for the same reason. A driver must never
            -- be able to look at a prompt that isn't the door.
            if BoardMenu.seated() then
                pcall(function()
                    self:setButtonBindingPrompt(CharacterJoypadButtonBinding.Interact,
                        txt("IGUI_RR_LeaveCab", "Leave the cab"), BoardMenu.promptLeave)
                end)
                return
            end
        end

        vanilla(self, dir)

        if asleep() then return end
        if interactPromptTaken(self) then return end    -- vanilla found something: leave it alone
        local rec = BoardMenu.boardable()
        if not rec then return end
        pcall(function()
            self:setButtonBindingPrompt(CharacterJoypadButtonBinding.Interact,
                txt("IGUI_RR_BoardCab", "Climb into the cab"), BoardMenu.promptBoard, rec)
        end)
    end

    ISButtonPrompt.rrBoardPatched = true
    print("[Railroader] controller: the interact button boards / leaves the locomotive.")
end
Events.OnGameStart.Add(claimInteractPrompt)

--------------------------------------------------------------------------
-- report(): why is there no way in? Answers the whole chain in one line --
--   RR.BoardMenu.report()
--------------------------------------------------------------------------
function BoardMenu.report()
    local rec, d
    if RR.Ride and RR.Ride.nearestBoardable then rec, d = RR.Ride.nearestBoardable() end
    print(string.format("[Railroader] board menu: mpBlocked=%s  seated=%s  joypad=%s  "
          .. "promptPatched=%s  nearest hull %.1f tiles (reach %.1f)",
          tostring(blocked()),
          tostring(BoardMenu.seated() ~= nil),
          tostring(JoypadState and JoypadState.players and JoypadState.players[1] ~= nil),
          tostring(ISButtonPrompt and ISButtonPrompt.rrBoardPatched == true),
          (rec and d) or -1,
          (RR.Ride and RR.Ride.MOUNT_REACH) or -1))
end

RR = RR or {}
RR.BoardMenu = BoardMenu
print("[Railroader] RR_BoardMenu.lua: loaded OK -- right-click or the controller's interact button boards her.")
return BoardMenu
