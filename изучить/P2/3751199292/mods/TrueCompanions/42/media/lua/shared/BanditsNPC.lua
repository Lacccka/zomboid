--
-- ************************************************
-- *** Bandits NPC - Companions Overhaul        ***
-- ************************************************
-- *** Add-on layer built on Slayer's Bandits   ***
-- ************************************************
--
-- Shared namespace + bootstrap. Loaded on client and server.
-- This mod is an add-on layer: it does NOT reimplement the bandit/zombie
-- engine, it reuses the Bandits mod (id "Bandits2"). See the engine notes
-- in the project memory for how that works.
--

BanditsNPC = BanditsNPC or {}
BanditsNPC.version = "0.77.14"
-- Version history: see CHANGELOG.md at the repo root (moved there by TASK-001).
BanditsNPC.MOD_ID = "TrueCompanions"

-- TRANSLATION LOOKUP. Returns the translated text for `key` from the player's
-- language pack (media/lua/shared/Translate/<LANG>/UI.json, standard PZ system), or
-- the built-in English `fallback` when the key isn't translated. Every user-facing
-- string in the mod should flow through this: translators only need to ship a UI.json
-- with the keys they've covered -- anything missing silently stays English, so a
-- partial translation can never break the mod. (Community ask, 5-9 Jul; a PT-BR
-- translator is waiting on this hook.)
function BanditsNPC.T(key, fallback)
    local ok, txt = pcall(getTextOrNull, key)
    if ok and txt and txt ~= "" then return txt end
    return fallback
end

-- TRANSLATE AND FORMAT. Same as T, then substitutes %1, %2, ... positionally.
--
-- ALWAYS USE THIS INSTEAD OF gsub FOR PLACEHOLDERS. Two things make hand-rolled
-- `:gsub("%%1", value)` unsafe, and both bit us in v0.63:
--   * gsub returns TWO values (string, count). `foo(s:gsub(...))` silently passes
--     the count as a second argument, and chained gsubs leak it too.
--   * the REPLACEMENT string is not literal -- a `%` inside a value is interpreted,
--     so a substituted item name or number containing one corrupts the output.
-- A function replacement sidesteps both: its return value is used verbatim.
--
-- Substitution runs highest-index-first so %10 is not eaten by the %1 pattern.
--
-- TWO PLACEHOLDER FORMS HAVE TO BE HANDLED, and this is not a style choice.
-- UI.json is written with `%1` (that is what vanilla's own EN/UI.json uses), but the
-- engine REWRITES it on load into Java's String.format form, `%1$s`, so that its own
-- getText(key, ...) can hand the string to String.format. getTextOrNull therefore
-- hands US `%1$s`, never `%1`. Substituting only `%1` left the `$s` behind, and the
-- player saw "Companions: 0$s of 2$s" -- v0.66 shipped that in every formatted string
-- in the mod, because the untranslated FALLBACK path (plain `%1`) is the one that gets
-- exercised while developing and it looked correct.
--
-- The engine form is replaced FIRST: `%1` is a prefix of `%1$s`, so doing it the other
-- way round consumes the number and orphans the suffix again. The width/precision and
-- conversion letter are matched loosely (`[-+ #0-9.]*[a-zA-Z]`) so a translator who
-- writes %1$d or %1$.2f is also handled rather than being left with visible debris.
--
-- A THIRD FORM: BARE printf DIRECTIVES (%s, %d, %.1f), filled left to right.
-- Most of the mod used to call string.format on a translated string, so the strings
-- translators have already been given -- the pending Russian pack has 61 of them --
-- are written with %s. Those files must keep working after the call sites moved to
-- TF, or converting the code would silently blank a value in every one of them. So
-- all three forms are accepted: %1, %1$s and %s. A translator can write whichever
-- their language pack already uses and be right.
--
-- The bare pass runs ONLY when no numbered placeholder matched. Mixing the two forms
-- in one string is ambiguous -- which argument does the bare %s take when %1 has
-- already consumed one? -- and this rule resolves it without guessing: numbered wins
-- outright, bare is the fallback for a string that has no numbers in it at all.
--
-- Two details in the bare pass that are easy to get wrong:
--   * `%%` is an ESCAPED percent and must survive; it is parked on \1 first and put
--     back afterwards, or a "100%%" would be eaten.
--   * SPACE IS NOT ACCEPTED as a printf flag here, although printf allows it. With
--     the space in, "100% chance" parses as the directive "% c" and comes out as
--     "100hance". None of our own strings contain a bare percent (checked), but a
--     translator's will eventually.
--
-- select("#", ...) rather than #args: an explicit nil argument makes the table
-- shorter than the real argument count, so the later placeholders would go unfilled
-- rather than showing the "nil" that says what actually went wrong.
function BanditsNPC.TF(key, fallback, ...)
    return BanditsNPC.Fill(BanditsNPC.T(key, fallback), ...)
end

-- The substitution half of TF, for the few strings whose KEY is built at runtime and
-- so cannot be written out at a TF call site (the gendered backstory lines). Those
-- have to call T themselves and fill afterwards.
--
-- This exists rather than allowing TF(nil, alreadyTranslated, ...) because that would
-- put a nil through getTextOrNull -- a Java call, and a dispatch error there BYPASSES
-- the pcall wrapped round it.
function BanditsNPC.Fill(s, ...)
    if type(s) ~= "string" then return s end
    local args = { ... }
    local n = select("#", ...)

    local numbered = false
    for i = n, 1, -1 do
        local v = tostring(args[i])
        local sub = function() return v end
        local c1, c2
        s, c1 = s:gsub("%%" .. tostring(i) .. "%$[-+ #0-9.]*[a-zA-Z]", sub)
        s, c2 = s:gsub("%%" .. tostring(i), sub)
        if c1 + c2 > 0 then numbered = true end
    end
    if numbered or n == 0 then return s end

    local idx = 0
    s = s:gsub("%%%%", "\1")
    s = s:gsub("%%[-+#0-9.]*[diouxXeEfgGqsc]", function()
        idx = idx + 1
        if idx > n then return "" end
        return tostring(args[idx])
    end)
    s = s:gsub("\1", "%%")
    return s
end

-- Human-readable, TRANSLATED label for a program name (the companion's current
-- activity). Shared by the dialog window header and the roster panel so there is
-- ONE keyed source. `mode` = "short" gives the roster's terser wording.
function BanditsNPC.ProgramLabel(name, mode)
    if mode == "short" then
        local m = {
            NPCCompanion = BanditsNPC.T("UI_BN_Prog_Following_Short", "Following you"),
            NPCStay      = BanditsNPC.T("UI_BN_Prog_Staying_Short", "Staying put"),
            NPCGuard     = BanditsNPC.T("UI_BN_Prog_Guarding", "Guarding"),
            NPCWork      = BanditsNPC.T("UI_BN_Prog_Working", "Working"),
            NPCRoutine   = BanditsNPC.T("UI_BN_Prog_Needs", "Tending to needs"),
            NPCNeutral   = BanditsNPC.T("UI_BN_Prog_Wandering", "Wandering"),
            NPCDowned    = BanditsNPC.T("UI_BN_Prog_Downed", "Knocked down"),
            NPCRelax     = BanditsNPC.T("UI_BN_Prog_Relax_Short", "Relaxing at base"),
            NPCAnimPlay  = BanditsNPC.T("UI_BN_Prog_Performing", "Performing"),
        }
        return m[name] or (name or "?")
    end
    local m = {
        NPCCompanion = BanditsNPC.T("UI_BN_Prog_Following", "Following"),
        NPCStay      = BanditsNPC.T("UI_BN_Prog_Staying", "Staying"),
        NPCGuard     = BanditsNPC.T("UI_BN_Prog_Guarding", "Guarding"),
        NPCWork      = BanditsNPC.T("UI_BN_Prog_Working", "Working"),
        NPCRoutine   = BanditsNPC.T("UI_BN_Prog_Needs", "Tending to needs"),
        NPCNeutral   = BanditsNPC.T("UI_BN_Prog_Wandering", "Wandering"),
        Looter       = BanditsNPC.T("UI_BN_Prog_Wandering", "Wandering"),
        Bandit       = BanditsNPC.T("UI_BN_Prog_Hostile", "Hostile"),
        NPCDowned    = BanditsNPC.T("UI_BN_Prog_Downed", "Knocked down"),
        NPCRelax     = BanditsNPC.T("UI_BN_Prog_Relax", "Relaxing at Base"),
        NPCAnimPlay  = BanditsNPC.T("UI_BN_Prog_Performing", "Performing"),
    }
    return m[name] or (name or "?")
end

-- Reads a BanditsNPC sandbox option, falling back to a default if sandbox vars
-- aren't available (e.g. main menu) or the option is unset.
-- IN-GAME OVERRIDES (v0.64).
--
-- Sandbox options can only be set when the world is created, which is the wrong
-- moment to decide most of these -- you find out you want companions immortal
-- after one dies, not before you have met anyone. So every option can now also be
-- set from the Beacon's Settings tab, and that override wins.
--
-- Stored GLOBALLY, not per beacon, and that is deliberate: these are world rules,
-- not station settings. A player can have two beacons or none, so "protect
-- companions" cannot sensibly have a different answer at each one. Only the
-- companion CAP is genuinely per-station, and that lives on the beacon itself.
--
-- Sandbox therefore still sets the starting values; this layer lets them change.
local OVERRIDES = "TrueCompanionsOptions"

function BanditsNPC.OptStore()
    return ModData.getOrCreate(OVERRIDES)
end

function BanditsNPC.Opt(name, default)
    local ov
    pcall(function() ov = BanditsNPC.OptStore()[name] end)
    if ov ~= nil then return ov end
    local sv = SandboxVars and SandboxVars.BanditsNPC
    local v = sv and sv[name]
    if v == nil then return default end
    return v
end

-- THE WRITE ITSELF. Only ever runs where the write is authoritative: single player,
-- or the SERVER after it has checked the caller's access level. Never call this
-- directly from client code -- call SetOpt, which routes.
--
-- Values are type-checked because this store is TRANSMITTED to every client: without
-- it a crafted command could push an arbitrary nested table into global mod data and
-- have the server broadcast it. Names are checked for the same reason -- junk keys
-- would persist in the save forever.
-- `[%l%u]` and not `%a`: same meaning, but %l/%u/%w are classes this codebase already
-- relies on, while %a appears in NO vanilla Lua file and nothing here proves Kahlua
-- implements it. Same rule as the engine-call audit -- do not use what is not proven.
local function optNameOk(name)
    return type(name) == "string" and name:match("^[%l%u][%w_]*$") ~= nil
end

local function optValueOk(value)
    local t = type(value)
    return value == nil or t == "boolean" or t == "number" or t == "string"
end

function BanditsNPC.ApplyOpt(name, value)
    if not (optNameOk(name) and optValueOk(value)) then return false end
    local ok = false
    pcall(function()
        BanditsNPC.OptStore()[name] = value
        ModData.transmit(OVERRIDES)
        ok = true
    end)
    return ok
end

-- Sets an in-game override. Passing nil clears it, falling back to the sandbox
-- value -- which is how "reset to world defaults" works.
--
-- THESE ARE WORLD RULES, NOT CLIENT SETTINGS (see the note above OptStore), so in
-- multiplayer the decision is the server's. v0.75.1 and earlier wrote the store and
-- called ModData.transmit straight from the beacon's Settings tab, with no access
-- check anywhere on the path -- nine toggles including ImmortalCompanions and
-- InfiniteAmmo, settable by any player from any beacon (audit BLOCKER 4). Vanilla
-- only ever transmits global mod data from server/ (forageServer.lua:49), which is
-- the shape copied here.
--
-- The nil case cannot survive a command payload -- the server's handler iterates
-- pairs(args) and a nil key is simply absent -- so it travels as an explicit `clear`
-- flag. Same reason the brain sync uses `false` as its clear sentinel.
function BanditsNPC.SetOpt(name, value)
    if not (optNameOk(name) and optValueOk(value)) then return end
    if isClient and isClient() then
        local p = getSpecificPlayer(0)
        if not p then return end
        sendClientCommand(p, 'BanditsNPC', 'SetOpt',
                          { name = name, value = value, clear = (value == nil) or nil })
        return
    end
    BanditsNPC.ApplyOpt(name, value)
end

-- Combat stance for our NPCs. Stored on the brain so it persists/syncs like
-- everything else. Default "defensive": stay with the master, only fight at
-- close range (no running out to confront distant enemies).
--   passive    = ignore enemies (handled via an AreEnemies hook, added later)
--   defensive  = don't seek enemies; defend self at close range
--   aggressive = proactively move to engage nearby enemies
function BanditsNPC.GetStance(bandit)
    local brain = BanditBrain and BanditBrain.Get(bandit)
    return (brain and brain.npcStance) or "defensive"
end

function BanditsNPC.SetStance(bandit, stance)
    local brain = BanditBrain and BanditBrain.Get(bandit)
    if brain then brain.npcStance = stance end
end

-- ===========================================================================
-- HOW CLOSE SHE FOLLOWS (v0.76.1)
-- ===========================================================================
--
-- The follow program stopped at a hardcoded one tile, which glues her to the player's
-- back: fine in a corridor, awkward in a doorway and actively unhelpful when you are
-- trying to aim past her.
--
-- Three settings rather than a slider, because the useful range is small and a number
-- would invite fiddling for no gain. NEAR is the previous hardcoded behaviour and stays
-- the DEFAULT, so nobody's companion changes how she behaves just because this shipped.
--
-- The values are the distance at which she stops closing, in tiles. FAR is deliberately
-- inside the ~8-tile radius the combat code uses to decide she should help, so choosing
-- it can never make her too far away to defend you.
BanditsNPC.FOLLOW_DIST = { near = 1, mid = 3, far = 6 }

function BanditsNPC.GetFollowDist(bandit)
    local brain = BanditBrain and BanditBrain.Get(bandit)
    local k = brain and brain.npcFollowDist
    if k and BanditsNPC.FOLLOW_DIST[k] then return k end
    return "near"
end

function BanditsNPC.GetFollowStop(bandit)
    return BanditsNPC.FOLLOW_DIST[BanditsNPC.GetFollowDist(bandit)] or 1
end

function BanditsNPC.SetFollowDist(bandit, key)
    if not BanditsNPC.FOLLOW_DIST[key] then return end
    -- THE SAME GATE AS EVERY OTHER MUTATOR. This one was written for the roster panel,
    -- which only ever lists your OWN companions, so the UI happened to be the gate -- and
    -- that is exactly the arrangement MayCommand exists to replace ("gating the buttons
    -- would mean auditing every handler in three windows and getting it right again after
    -- every UI change"). v0.77.2 put a distance strip on the dialog window, which opens on
    -- anyone, and that is the second window it would have had to be right in.
    if not BanditsNPC.MayCommand(bandit) then return end
    local brain = BanditBrain and BanditBrain.Get(bandit)
    if not brain then return end
    brain.npcFollowDist = key
    -- BanditBrain.Get hands back the live modData table, so the line above has already
    -- taken effect -- but every other setter in the mod calls Update and this one did not.
    -- Relying on Get's aliasing is relying on a detail of somebody else's mod.
    if BanditBrain.Update then BanditBrain.Update(bandit, brain) end
    -- Push it the way every other per-companion setting is pushed, so a multiplayer
    -- client's choice reaches the machine actually running her program.
    if Bandit and Bandit.ForceSyncPart then
        pcall(function()
            Bandit.ForceSyncPart(bandit, { id = brain.id, npcFollowDist = key })
        end)
    end
end

-- DOES THIS PLAYER OWN THE COMPANION described by (masterId, masterName)?
--
-- THE USERNAME IS THE IDENTITY; THE CHARACTER ID IS ONLY A CACHE. That distinction is
-- the whole point of this function and it was got wrong in both callers until v0.75.3
-- (audit BLOCKER 2). BanditUtils.GetCharacterID(player) is getOnlineID() in MP -- a
-- per-connection number the server REUSES as players come and go -- so treating an id
-- match as proof of ownership means a reconnecting player can inherit whatever the
-- previous holder of that id owned. It is not a cosmetic mix-up: Interact.Recruit:229
-- refuses to re-recruit someone else's companion by calling IsMine, so passing this
-- check wrongly hands over ownership PERMANENTLY.
--
-- The rule: when a username was recorded AND this player has a readable one, the
-- username decides -- and it decides BOTH ways, so a mismatch is a refusal rather than
-- a reason to go and ask the id. The id is consulted only when there is no name to
-- compare (legacy brains recruited before masterName existed, or a player object whose
-- username cannot be read).
--
-- SINGLE PLAYER IS DELIBERATELY LEFT ALONE. There is one player, so nothing can be
-- contested and the strictness buys nothing -- while a username that changed for any
-- reason would silently orphan every companion in the save. `isClient()` is false in SP
-- and true for every multiplayer client including a co-op host, which is exactly the
-- population that needs the strict rule.
local function ownerMatches(p, masterId, masterName)
    if not p then return false end

    if isClient and isClient() and masterName ~= nil and masterName ~= "" then
        local okName, un = pcall(function() return p:getUsername() end)
        if okName and un and un ~= "" then
            return un == masterName          -- authoritative, in both directions
        end
        -- unreadable username: fall through to the id rather than orphan the companion
    end

    if masterName ~= nil and masterName ~= "" then
        local okName, un = pcall(function() return p:getUsername() end)
        if okName and un and un == masterName then return true end
    end
    if masterId ~= nil then
        local okId, cid = pcall(function() return BanditUtils.GetCharacterID(p) end)
        if okId and cid == masterId then return true end
    end
    return false
end

-- Resolve a companion's OWNER (the recruiter) -- strictly, so a companion never
-- follows/obeys a player who didn't recruit it. Matching is delegated to ownerMatches
-- above, so this and IsMine can never drift apart. Returns the IsoPlayer or nil.
-- Used by the follow program in place of the engine's GetMasterPlayer, which in MP can
-- resolve to the wrong/nearest player. Returns nil if the owner isn't present, so the
-- companion idles rather than tagging along with a stranger.
function BanditsNPC.GetOwner(bandit)
    local brain = BanditBrain and BanditBrain.Get(bandit)
    if not brain then return nil end
    local masterId, masterName = brain.master, brain.masterName
    if masterId == nil and not masterName then return nil end

    local function matches(p)
        if not p or p:isDead() then return false end
        return ownerMatches(p, masterId, masterName)
    end

    -- online players first (MP), then local/split-screen players
    local ok, online = pcall(function() return getOnlinePlayers() end)
    if ok and online then
        for i = 0, online:size() - 1 do
            local p = online:get(i)
            if matches(p) then return p end
        end
    end
    for i = 0, 3 do
        local p = getSpecificPlayer(i)
        if matches(p) then return p end
    end
    return nil
end

-- True if the LOCAL player (player 0) recruited/owns this companion. Used to gate
-- "mine only" controls and -- via Interact.Recruit:229 -- to refuse another player
-- claiming a companion that already belongs to someone else, which is why getting it
-- wrong costs the companion permanently rather than just showing a button.
--
-- Shares ownerMatches with GetOwner above; see the long note there for why the username
-- outranks the character id in multiplayer and why single player is left permissive.
-- Resolve the owning IsoPlayer from a BRAIN (GetOwner above needs the zombie). Used
-- wherever a decision belongs to the recruiter rather than to whoever is looking --
-- the romance track being the case that prompted it.
function BanditsNPC.GetOwnerOf(brain)
    if not brain then return nil end
    local masterId, masterName = brain.master, brain.masterName
    if masterId == nil and not masterName then return nil end
    local function pick(p)
        if not p then return false end
        local dead = false
        pcall(function() dead = p:isDead() end)
        if dead then return false end
        return ownerMatches(p, masterId, masterName)
    end
    local ok, online = pcall(function() return getOnlinePlayers() end)
    if ok and online then
        for i = 0, online:size() - 1 do
            local p = online:get(i)
            if pick(p) then return p end
        end
    end
    for i = 0, 3 do
        local p = getSpecificPlayer(i)
        if pick(p) then return p end
    end
    return nil
end

-- EVERY LOCAL PLAYER, not just player 0 (v0.75.15). Split-screen has up to four, and
-- asking only getSpecificPlayer(0) meant player 2 could never own anything: her
-- companions failed every ownership test, so she could not order, dress or dismiss them
-- and the Roster panel showed her nothing.
--
-- The trade-off, stated plainly: on a split-screen couch game this makes either local
-- player able to command either's companions, because "mine" is answered for the screen
-- rather than per-seat. That is the right way round -- shared-sofa play is cooperative,
-- and the alternative is one player locked out of the mod entirely. Networked MP is
-- unaffected: there is exactly one local player, so this reduces to the old behaviour.
function BanditsNPC.IsMine(brain)
    if not (brain and brain.recruited and brain.master) then return false end
    for i = 0, 3 do
        local p = getSpecificPlayer(i)
        if p and ownerMatches(p, brain.master, brain.masterName) then return true end
    end
    return false
end

-- Same question for an ARBITRARY player rather than the local one. Exported so nothing
-- outside this file has to hand-roll `brain.master == someId` again -- that comparison
-- is the bug ownerMatches exists to prevent, and it had already reappeared once in
-- Talk.lua's jealousy scan.
function BanditsNPC.IsOwnedBy(brain, player)
    if not (brain and brain.recruited and brain.master and player) then return false end
    return ownerMatches(player, brain.master, brain.masterName)
end

-- ONE LINE-BREAKING ALGORITHM (v0.75.53).
--
-- Three windows carried their own word wrap: DialogWindow and GiveWindow (identical
-- greedy fill, differing only in the font constant) and TalkWindow's wrapLines. Same
-- algorithm written three times is three places for it to drift.
--
-- ONLY THE BREAKING MOVES HERE, NOT THE DRAWING, and that distinction is deliberate: the
-- two drawTextWrapped are NOT interchangeable -- the dialog one RETURNS the y it finished
-- at and advances past the final line, the give one does neither, and callers depend on
-- both behaviours. Sharing the draw would have silently changed layout in one of them.
-- Callers keep their own drawing loop and their own return contract.
function BanditsNPC.WrapLines(text, font, maxWidth)
    local out = {}
    if type(text) ~= "string" or text == "" then return out end
    local tm = getTextManager()
    local line = ""
    for w in string.gmatch(text, "%S+") do
        local test = (line == "") and w or (line .. " " .. w)
        if tm:MeasureStringX(font, test) > maxWidth then
            -- guard `line ~= ""`: a single word WIDER than maxWidth arriving first would
            -- otherwise emit an empty leading line. TalkWindow's copy had this guard and
            -- the two drawTextWrapped copies did not -- so the three were not quite the
            -- same algorithm after all, and the shared one keeps the correct behaviour.
            if line ~= "" then out[#out + 1] = line end
            line = w
        else
            line = test
        end
    end
    if line ~= "" then out[#out + 1] = line end
    return out
end

-- ONE DEFINITION OF THE UI SCALING POLICY (v0.75.47).
--
-- BanditsNPCDialogWindow and BanditsNPCRosterPanel each carried a byte-identical
-- bnApplyScale() -- same UIScale read, same font thresholds (1.5 / 1.2 / 1.35), same
-- padding maths. Two copies of a policy that nothing kept in step: change a threshold in
-- one and the two windows scale differently, which reads as one of them being broken.
--
-- Only the POLICY moves here, not the locals. Each window keeps its own SCALE / FONT_HGT /
-- PAD / BTN_H upvalues, because those are referenced on nearly every line of both files
-- and rewriting those call sites is a large untestable change for no behavioural gain.
-- This returns the values; the caller assigns them.
-- Auto sizing, and it deliberately does NOTHING below 4K.
--
-- v0.77.11 also scaled 1440p, and measured the game's Font Size option to grow the spacing
-- with the text. Both were too broad. The font measurement in particular assumed the layout
-- had been authored against a 13px UIFont.Small -- but the author works at the 16px setting,
-- so that ratio would have quietly enlarged the UI of every player already at 16px,
-- including the one it looked correct for. Resizing people who are fine is a worse bug than
-- the one being fixed.
--
-- What the two 4K testers actually established: at Font Size 16px the layout is CORRECT but
-- small, and at Font Size Auto -- which is PZ enlarging the font for a high-DPI screen --
-- the text outgrows the boxes and labels collide. One scale step up at 4K addresses both:
-- the boxes grow with the screen, and they grow by roughly what PZ's own Auto adds to the
-- font. Height, not width: an ultrawide is not a reason to enlarge anything.
local function autoScaleForScreen()
    local h = 1080
    pcall(function() h = getCore():getScreenHeight() or 1080 end)
    if h >= 2000 then return 1.5 end      -- 4K and up, and nothing else
    return 1.0
end

function BanditsNPC.UIScaleValues()
    local sv = SandboxVars and SandboxVars.BanditsNPC
    local scale = (sv and sv.UIScale) or 0

    -- 0 = AUTO, and it is the default now. "Not every player goes into sandbox" applies
    -- twice over here: a 4K player who never opens the options page got a window laid out
    -- for 1080p and no reason to suspect a setting existed. Anything above 0 is a deliberate
    -- choice and is honoured exactly, so a save that already stores 1.0 keeps 1.0.
    if type(scale) ~= "number" or scale <= 0 then scale = autoScaleForScreen() end

    local small = (scale >= 1.5 and UIFont.Large) or (scale >= 1.2 and UIFont.Medium) or UIFont.Small
    local med   = (scale >= 1.35 and UIFont.Large) or UIFont.Medium
    local tm = getTextManager()
    local fhSmall, fhMed = tm:getFontHeight(small), tm:getFontHeight(med)
    local px = scale

    -- IT MUST STILL FIT ON THE SCREEN. A pure safety bound -- it can only ever shrink the
    -- scale, never raise it -- so a deliberate 2.0 on a small monitor cannot lay the
    -- companion window out beyond the display and strand the player with no way back to the
    -- option. Measured against the biggest panel the mod opens (900x560), so every smaller
    -- panel is covered by the same bound.
    pcall(function()
        local sw, sh = getCore():getScreenWidth(), getCore():getScreenHeight()
        if sw and sh and sw > 0 and sh > 0 then
            local fit = math.min((sw - 40) / 900, (sh - 40) / 560)
            if fit > 0 and px > fit then px = fit end
        end
    end)
    if px < 1 then px = 1 end

    return px, small, med, fhSmall, fhMed,
           math.floor(10 * px), fhSmall + math.floor(8 * px)
end

-- MAY THE LOCAL PLAYER GIVE THIS NPC ORDERS / CHANGE HER? The single gate for every
-- model-layer mutator (audit Cluster A).
--
-- WHY THE MODEL LAYER AND NOT THE UI. The dialog window deliberately OPENS on anyone --
-- you must be able to look at a stranger to recruit them, and at another player's
-- companion to see whose it is -- and then offered nearly every action on whatever it
-- had opened. Gating the buttons would mean auditing every handler in three windows and
-- getting it right again after every UI change; gating here cannot be bypassed by any
-- caller, present or future.
--
-- UNOWNED IS PERMISSIVE, and that is not an oversight: an NPC nobody has recruited has
-- no owner to protect, and the recruitment path itself must keep working on strangers.
-- The precondition mirrors DialogWindow:isOwnedByOther, so the two can never disagree
-- about what "somebody's companion" means.
function BanditsNPC.MayCommand(zombie)
    if not zombie then return false end
    local brain = BanditBrain and BanditBrain.Get(zombie)
    if not brain then return false end
    if not (brain.recruited and brain.master) then return true end
    return BanditsNPC.IsMine(brain) and true or false
end

-- Verifies the Bandits engine is present. Returns true if everything we
-- depend on is loaded, false (and prints why) otherwise.
function BanditsNPC.CheckDependencies()
    local missing = {}
    if not Bandit then table.insert(missing, "Bandit") end
    if not BanditBrain then table.insert(missing, "BanditBrain") end
    if not BanditCustom then table.insert(missing, "BanditCustom") end
    if not ZombiePrograms then table.insert(missing, "ZombiePrograms") end

    if #missing > 0 then
        print("[BanditsNPC] ERROR: Bandits engine not found. Missing globals: " .. table.concat(missing, ", "))
        print("[BanditsNPC] This mod requires the Bandits mod (Bandits2) to be enabled and loaded first.")
        return false
    end
    return true
end

local function onGameBoot()
    if BanditsNPC.CheckDependencies() then
        print("[BanditsNPC] v" .. BanditsNPC.version .. " loaded OK. Bandits engine detected.")
    end
end

Events.OnGameBoot.Add(onGameBoot)
