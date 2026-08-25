--
-- True Companions - controller support (v0.76.1)
--
-- ===========================================================================
-- THE RULE THIS FILE IS BUILT AROUND
-- ===========================================================================
--
-- "Make sure nothing breaks if your code fails." (author, 9 Aug.) So the whole module is
-- written to be DISABLE-ABLE BY ITS OWN FAILURE, and that shapes every decision below:
--
--   * ONE-STRIKE KILL SWITCH. Every entry point runs through J.Safe. The first time any
--     handler errors, controller support turns ITSELF off for the rest of the session and
--     says so once in the log. A broken feature must not become a broken UI, and a UI
--     handler that throws every frame is worse than one that does nothing.
--
--   * NOTHING IS OVERRIDDEN UNCONDITIONALLY. The panel keeps its own mouse behaviour
--     exactly as it was; the joypad methods are additive, and each returns immediately
--     when this module is off. Deleting this file would leave the mod working.
--
--   * FOCUS IS ALWAYS RELEASABLE. Grabbing joypad focus and not giving it back would
--     take the controller away from the whole game, which is the single worst thing this
--     file could do. B releases it, closing releases it, and a panel that is no longer
--     visible releases it on the next tick even if nothing called close().
--
-- WHAT IT COVERS. The roster panel: D-pad moves between the buttons that are on screen,
-- A presses one, B closes. That is the panel a controller player actually needs, because
-- it is the only one with no keyboard route in. It is deliberately not "complete
-- controller support" -- the dialog, beacon and editor windows are still mouse-driven --
-- and calling it minimal is honest rather than modest.
--

BanditsNPC = BanditsNPC or {}
BanditsNPC.Joypad = BanditsNPC.Joypad or {}
local J = BanditsNPC.Joypad

J.dead = false          -- set true by the first failure, never reset in this session

-- ---------------------------------------------------------------------------
-- the kill switch
-- ---------------------------------------------------------------------------

function J.Safe(what, fn, ...)
    if J.dead then return nil end
    local ok, a, b = pcall(fn, ...)
    if ok then return a, b end
    J.dead = true
    -- Released here too: if the failure happened WHILE we held focus, leaving it held
    -- would lock the player out of their own controller with no way back.
    pcall(function() J.Release() end)
    print("[BanditsNPC] controller support disabled after an error in " .. tostring(what)
        .. ": " .. tostring(a) .. " -- everything else is unaffected, mouse and keyboard"
        .. " work as normal.")
    return nil
end

-- ---------------------------------------------------------------------------
-- is there a controller at all
-- ---------------------------------------------------------------------------
--
-- JoypadState.players is indexed from 1 for player 0, which is the vanilla idiom
-- (ISUIElement.lua:858, ISUIWriteJournal.lua:119). A nil entry means this player is on
-- keyboard and mouse, and every path here then does nothing at all.
function J.Data(playerNum)
    if J.dead then return nil end
    local d
    pcall(function()
        if JoypadState and JoypadState.players then
            d = JoypadState.players[(playerNum or 0) + 1]
        end
    end)
    return d
end

function J.Present(playerNum)
    return J.Data(playerNum) ~= nil
end

-- ---------------------------------------------------------------------------
-- focus
-- ---------------------------------------------------------------------------

J.holder = nil          -- the panel we last gave focus to, so Release knows what to undo

function J.Grab(panel, playerNum)
    if J.dead or not panel then return false end
    if not J.Present(playerNum) then return false end
    local got = false
    J.Safe("Grab", function()
        -- setJoypadFocus is the global vanilla uses to hand a window the controller
        -- (ISModalDialog.lua:51 restores the previous focus with the same call).
        setJoypadFocus(playerNum or 0, panel)
        J.holder = { panel = panel, player = playerNum or 0 }
        got = true
    end)
    return got
end

function J.Release()
    local h = J.holder
    J.holder = nil
    if not h then return end
    -- Not routed through J.Safe on purpose: this is the function that has to work even
    -- when everything else has failed, so it swallows its own errors and never disables
    -- anything.
    pcall(function() setJoypadFocus(h.player, nil) end)
end

-- The safety net for the case nobody planned for: the panel went away without telling us.
-- Without this a crash mid-close, or a window removed by something else, would leave the
-- controller pointed at a dead element for the rest of the session.
local function watchdog()
    local h = J.holder
    if not h then return end
    local alive = false
    pcall(function()
        alive = h.panel ~= nil and h.panel:getIsVisible() and h.panel:isReallyVisible()
    end)
    if not alive then J.Release() end
end

Events.OnTick.Remove(watchdog)
Events.OnTick.Add(watchdog)

-- ---------------------------------------------------------------------------
-- navigating a list of hit rectangles
-- ---------------------------------------------------------------------------
--
-- Our panels already build a per-frame list of clickable rectangles for the mouse (the
-- `_hits` table). That list is the navigation model for free: it is in draw order, it
-- only contains what is actually on screen, and it is exactly what a click would have
-- hit -- so the controller and the mouse can never disagree about what is pressable.
--
-- No separate widget tree, no focus graph to keep in step with the renderer. The cost is
-- that the selection is an INDEX into a list that is rebuilt every frame, which is why
-- Step clamps rather than wraps blindly and why the caller re-reads the entry each time.

-- TWO KINDS OF PANEL, ONE NAVIGATOR (v0.76.2).
--
-- The roster panel draws its buttons and hit-tests rectangles (`_hits`); the dialog window
-- uses 31 real ISButton children. Rather than teach this module about both, a panel may
-- publish `getJoypadTargets()` returning {x,y,w,h,go} entries and own the question itself.
-- `_hits` stays the default because it needs no cooperation at all.
--
-- Sorted top-to-bottom then left-to-right, which is reading order and therefore the order
-- a player expects the D-pad to walk -- child order is creation order, which is not the
-- same thing once a layout has been revised a few times.
local function targets(panel)
    if panel.getJoypadTargets then
        local t
        pcall(function() t = panel:getJoypadTargets() end)
        if t then return t end
    end
    local out = {}
    for _, h in ipairs(panel._hits or {}) do
        out[#out + 1] = { x = h.x, y = h.y, w = h.w, h = h.h,
                          go = function() if panel.onHit then panel:onHit(h) end end }
    end
    return out
end

-- Every visible, enabled ISButton child of a panel, in reading order. Offered here so a
-- window of real widgets gets navigation by writing one line rather than a sort.
function J.ButtonTargets(panel)
    local out = {}
    pcall(function()
        for _, c in ipairs(panel:getChildrenInOrder() or {}) do
            local ok = false
            pcall(function()
                ok = c.onclick ~= nil and c:getIsVisible()
                     and (c.enable == nil or c.enable) and (c.width or 0) > 0
            end)
            if ok then
                out[#out + 1] = { x = c:getX(), y = c:getY(), w = c.width, h = c.height,
                                  go = function() pcall(function() c.onclick(c.target or panel, c) end) end }
            end
        end
    end)
    table.sort(out, function(a, b)
        if math.abs(a.y - b.y) > 4 then return a.y < b.y end
        return a.x < b.x
    end)
    return out
end

function J.Step(panel, delta)
    if J.dead then return end
    J.Safe("Step", function()
        local hits = targets(panel)
        panel._joyTargets = hits
        if not hits or #hits == 0 then panel.joySel = nil; return end
        local i = panel.joySel or 0
        i = i + delta
        if i < 1 then i = #hits end
        if i > #hits then i = 1 end
        panel.joySel = i
        -- Keep the selection on screen. A selection the player cannot see reads as the
        -- controller having stopped responding.
        local h = hits[i]
        if h and panel.EnsureVisible then panel:EnsureVisible(h) end
    end)
end

function J.Activate(panel)
    if J.dead then return end
    J.Safe("Activate", function()
        local hits = panel._joyTargets or targets(panel)
        local h = hits and panel.joySel and hits[panel.joySel]
        if h and h.go then h.go() end
    end)
end

-- Drawn by the panel after its own render, so it sits on top of the card it marks.
function J.DrawSelection(panel)
    if J.dead then return end
    J.Safe("DrawSelection", function()
        -- Re-resolved rather than cached: the roster rebuilds its list every frame, so a
        -- marker drawn from a stale list would sit over the wrong button.
        local hits = targets(panel)
        panel._joyTargets = hits
        if panel.joySel and panel.joySel > #hits then panel.joySel = #hits end
        local h = hits and panel.joySel and hits[panel.joySel]
        if not h then return end
        panel:drawRectBorder(h.x - 1, h.y - 1, h.w + 2, h.h + 2, 0.95, 0.45, 0.75, 1.0)
        panel:drawRectBorder(h.x - 2, h.y - 2, h.w + 4, h.h + 4, 0.55, 0.45, 0.75, 1.0)
    end)
end

-- ---------------------------------------------------------------------------
-- installing the handlers on a panel
-- ---------------------------------------------------------------------------
--
-- Attached to the CLASS once rather than to each instance, so reopening the window does
-- not stack handlers. Every one of them is a no-op while J.dead, which is what makes the
-- kill switch total rather than partial.
function J.Install(cls)
    if not cls or cls._tcJoypadInstalled then return end
    cls._tcJoypadInstalled = true

    function cls:onJoypadDirUp()    J.Step(self, -1) end
    function cls:onJoypadDirDown()  J.Step(self,  1) end
    -- Left/right move by a whole card rather than one button, which is what the shape of
    -- the list implies: rows of three chips inside a card, cards stacked down the panel.
    function cls:onJoypadDirLeft()  J.Step(self, -3) end
    function cls:onJoypadDirRight() J.Step(self,  3) end

    function cls:onJoypadDown(button)
        if J.dead then return end
        J.Safe("onJoypadDown", function()
            if button == Joypad.AButton then
                J.Activate(self)
            elseif button == Joypad.BButton then
                -- B is the way out, and it must work even if nothing else here does.
                J.Release()
                if self.close then self:close()
                elseif self.onClose then self:onClose() end
            end
        end)
    end
end

-- ---------------------------------------------------------------------------
-- GETTING IN: the D-pad wheel
-- ---------------------------------------------------------------------------
--
-- "Everything in our mod must be at least reachable through controller input." Navigation
-- inside a window is useless if a pad cannot OPEN one, and PZ's key bindings are keyboard
-- only -- the V key that opens Talk can never be bound to a controller button. The pad's
-- actual context surface is the D-pad wheels, so that is where the mod has to appear.
--
-- ISDPadWheels.onDisplayRight is the "menus and tools" wheel (map, minimap, survival
-- guide). Wrapping a vanilla global like this is the same technique BanditsNPCSleep
-- already uses on ISVehicleMenu.showRadialMenu, so it is a proven pattern in this codebase
-- rather than a new risk.
--
-- WRAPPED, NOT REPLACED, AND GUARDED TWICE. The original is called first and its result
-- kept; our slices are added afterwards inside a pcall, so if anything here throws the
-- player still gets the vanilla wheel exactly as it was. The `_tcWheelPatched` flag makes
-- re-entry impossible if this file is ever loaded twice.
local function nearestCompanion(playerObj)
    local best, bestD
    pcall(function()
        local zl = getCell():getZombieList()
        for i = 0, zl:size() - 1 do
            local z = zl:get(i)
            if z and z:isAlive() and z:getVariableBoolean("Bandit") then
                local brain = BanditBrain.Get(z)
                if brain and brain.recruited and BanditsNPC.IsMine and BanditsNPC.IsMine(brain) then
                    local d = BanditUtils.DistTo(playerObj:getX(), playerObj:getY(), z:getX(), z:getY())
                    if d <= 6 and (not bestD or d < bestD) then bestD = d; best = z end
                end
            end
        end
    end)
    return best
end

if ISDPadWheels and ISDPadWheels.onDisplayRight and not BanditsNPC._tcWheelPatched then
    BanditsNPC._tcWheelPatched = true
    local origRight = ISDPadWheels.onDisplayRight

    function ISDPadWheels.onDisplayRight(joypadData)
        origRight(joypadData)
        pcall(function()
            if J.dead then return end
            local playerIndex = joypadData and joypadData.player or 0
            local playerObj = getSpecificPlayer(playerIndex)
            local menu = getPlayerRadialMenu(playerIndex)
            -- The original returns without building a menu while the game is paused, and
            -- it toggles the wheel off if it was already up. Both leave us with a menu we
            -- must not add to.
            if not (playerObj and menu and menu:isReallyVisible()) then return end

            local icon = getTexture("media/ui/group.png")
            menu:addSlice(BanditsNPC.T("UI_BN_Roster_Title", "Companions"), icon, function()
                pcall(function() BanditsNPCRosterPanel.Toggle() end)
            end)

            -- Talk is offered only when there is someone to talk to, so the wheel never
            -- carries a slice that would do nothing.
            local who = nearestCompanion(playerObj)
            if who then
                local nm = ""
                pcall(function()
                    local b = BanditBrain.Get(who)
                    nm = (b and b.fullname) or ""
                end)
                menu:addSlice(BanditsNPC.TF("UI_BN_TalkTo", "Talk to %1", nm), icon, function()
                    pcall(function()
                        if BanditsNPCDialogWindow and BanditsNPCDialogWindow.OpenFor then
                            BanditsNPCDialogWindow.OpenFor(who)
                        end
                    end)
                end)
            end

            -- Re-centre: the wheel sizes itself to its slices and vanilla positioned it
            -- before ours existed, so without this the two extra entries push it off centre.
            pcall(function()
                menu:setX(getPlayerScreenLeft(playerIndex) + getPlayerScreenWidth(playerIndex) / 2 - menu:getWidth() / 2)
                menu:setY(getPlayerScreenTop(playerIndex) + getPlayerScreenHeight(playerIndex) / 2 - menu:getHeight() / 2)
            end)
        end)
    end
end
