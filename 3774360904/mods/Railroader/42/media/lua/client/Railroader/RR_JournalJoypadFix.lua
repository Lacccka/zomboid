--***********************************************************************
-- Railroader / RR_JournalJoypadFix  -- reading the engineer's notebook ON A
-- CONTROLLER must not spam Lua errors (player report, Steam Deck, 2026-08-05:
-- "when u read how to drive u get error spam").
--
-- THE BUG IS VANILLA'S. Ours is simply the first written note most players ever
-- open -- the Railroader is handed one at character creation (RR_ManualPlace), so
-- a controller player meets this crash inside his first minute, on our item, and
-- reads it as our bug.
--
-- THE MECHANISM, end to end (all line numbers B42 vanilla Lua):
--   1. "Read <name> note" opens ISUIWriteJournal (ISInventoryPaneContextMenu:2702)
--      with `editable` = DOES THE PLAYER CARRY SOMETHING TO WRITE WITH (:567 --
--      any WRITE / PEN / PENCIL tag in his inventory). No pen => editable false
--      => `o.locked = true` (ISUIWriteJournal:320).
--   2. A locked journal is built WITHOUT the trash and lock buttons -- they live
--      behind `if not self.locked` (:37-65). So self.lockButton is nil.
--   3. setJoypadButtons opens with `self.lockButton.internal == "LOCKBOOK"` (:127),
--      unguarded. On a keyboard nothing ever calls it; with a joypad plugged in
--      onWriteSomething hands the window joypad focus (:2705-2707), so
--      onGainJoypadFocus (:256) calls it -- and it throws on the nil.
--   4. That is why it SPAMS. The call comes from updateJoypadFocus
--      (Gamepad/JoyPadSetup:608), which records the focus change by writing
--      `joypadData.lastfocus = joypadData.focus` -- but that write is BELOW the
--      onGainJoypadFocus call (:637). The exception aborts the function before it,
--      so the focus change is never recorded, and the next frame walks in with
--      `lastfocus ~= focus` still true. updateJoypadFocus runs once per RENDER TICK
--      (onJoypadRenderTick -> JoypadControllerData:update -> :975), so it is one
--      error per frame for as long as the notebook is open.
--
-- WHAT WE DO: take the function over ONLY in the state vanilla cannot survive
-- (lockButton nil) and build the joypad rows vanilla would have built for a book
-- with no editing buttons -- the page arrows, if the book has more than one page.
-- OK / Cancel need no row: ISUIWriteJournal:render (:238-249) binds them to A and B
-- whenever the cursor is past the last row, which is where D-pad down lands
-- (:272-279). With lockButton present we call the original untouched, so a notebook
-- opened WITH a pen behaves exactly as before -- and so does any other mod that
-- wrapped this function before us.
--
-- NOT GATED ON RR_MP: this is a UI guard over a vanilla crash. It writes nothing to
-- the world, claims no authority, and a stood-down client still opens vanilla
-- notebooks -- which still crash. There is nothing here for a server to disagree with.
--
-- Scope note: this fixes EVERY read-only note in the game, not only ours. That is
-- deliberate -- the failing state is unconditionally fatal in vanilla, so taking it
-- over cannot make anything worse, and narrowing the guard to our own item would
-- leave the player crashing on the next scrap of paper he picks up.
--***********************************************************************

print("[Railroader] RR_JournalJoypadFix.lua: loading...")

local Fix = {}

--------------------------------------------------------------------------
-- rows(self, joypadData): the locked-journal case of vanilla's setJoypadButtons.
-- Deliberately close to the original so the two can be diffed by eye if TIS ever
-- fixes theirs: clear the focus, rebuild the row list, restore the focus.
--------------------------------------------------------------------------
local function lockedSetJoypadButtons(self, joypadData)
    if not joypadData then return end

    self:clearJoypadFocus(joypadData)
    self.joypadButtonsY    = {}
    self.allJoypadButtons  = {}

    local buttons = {}
    if self.nextPage then
        table.insert(buttons, self.previousPage)
        table.insert(buttons, self.nextPage)
    end
    if #buttons > 0 then
        self:insertNewListOfButtons(buttons)
    end

    -- Row 1 when there are page arrows, otherwise straight to the A/B row that
    -- render() draws (joypadIndexY past the end of joypadButtonsY).
    self.joypadIndexY = 1
    self.joypadButtons = self.joypadButtonsY[self.joypadIndexY] or {}
    self.joypadIndex   = math.min(math.max(self.joypadIndex or 1, 1),
                                  math.max(#self.joypadButtons, 1))
    self:restoreJoypadFocus(joypadData)
end

--------------------------------------------------------------------------
-- patch(): wrap ISUIWriteJournal.setJoypadButtons. Idempotent (a flag on the
-- class), and a no-op if the class isn't there -- a missing vanilla global must
-- never take this file's load down with it (hard rule 2: a dead file is silent).
--------------------------------------------------------------------------
function Fix.patch()
    if not ISUIWriteJournal or ISUIWriteJournal.rrJoypadPatched then return false end
    local vanilla = ISUIWriteJournal.setJoypadButtons
    if not vanilla then return false end

    ISUIWriteJournal.setJoypadButtons = function(self, joypadData)
        if self.lockButton then return vanilla(self, joypadData) end
        return lockedSetJoypadButtons(self, joypadData)
    end

    ISUIWriteJournal.rrJoypadPatched = true
    print("[Railroader] journal joypad guard: read-only notes are safe on a controller.")
    return true
end

-- Try at load (vanilla client Lua is compiled before a mod's), and again on
-- OnGameStart in case the class arrives late.
if not Fix.patch() then
    Events.OnGameStart.Add(function() Fix.patch() end)
end

RR = RR or {}
RR.JournalJoypadFix = Fix
print("[Railroader] RR_JournalJoypadFix.lua: loaded OK")
return Fix
