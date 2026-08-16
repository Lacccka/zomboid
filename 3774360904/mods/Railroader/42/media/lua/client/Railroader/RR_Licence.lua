--***********************************************************************
-- Railroader / RR_Licence  -- the engine half of the training system: who may work
-- a locomotive, and how reading a book changes that answer.
--
-- The rules are the pure, unit-tested RR_Training. This file does the two things
-- that need the engine: ASK the character what he knows, and WRITE the permit when
-- he finishes a book. Plus one small chore -- putting a locomotive's own journal in
-- her cab when she is first found.
--
-- ==== WHY THERE IS NO CUSTOM READ ACTION, NO CUSTOM UI, AND NO GATE ====
--
-- ISReadABook already is the whole reading mechanic: the context menu offers it
-- for anything of category Literature (ISInventoryPaneContextMenu:539 ->
-- doLiteratureMenu:1030 -> onLiteratureItems:2809 -> readItem, which even moves the
-- book into the player's inventory first), it animates, it plays page turns, and it
-- resumes a long book from the page this character stopped at. Writing our own
-- would be re-implementing all of that to add one line.
--
-- So we take over exactly ONE method and defer to the original:
--
--   complete -- our grant. Called once, only on a read that actually finished.
--
-- There is deliberately NO isValid takeover any more (owner, 2026-08-09): our books
-- read exactly like every other book in the game, with no condition of ours on top.
-- The rule that used to live there -- a model journal readable only in the cab of
-- its own model -- was inert whenever the player complied and punitive the one time
-- he had sensibly carried the book away with him. RR_Training's header carries the
-- full reasoning; two unit tests guard the deletion. Do not bring it back.
--
-- The one hook is keyed on "is this one of OUR items"; for every other book in the
-- game the original runs and nothing of ours is consulted. Same shape as
-- RR_JournalJoypadFix, which takes over ISUIWriteJournal:setJoypadButtons only in
-- the case it exists to fix.
--
-- ==== THE STORE IS knownRecipes, NOT ModData ====
--
-- learnRecipe(name) with no recipe script by that name simply adds the string to
-- the character's knownRecipes (IsoGameCharacter:11668), which
-- IsoGameCharacter.save (:5358) writes and load (:5237) reads back. Persistent, per
-- character, no serialisation of ours, and no recipe script to register.
--
-- READ IT BACK ONLY WITH isRecipeActuallyKnown. The plain isRecipeKnown(name)
-- returns TRUE for everyone when the sandbox option SeeNotLearntRecipe is on
-- (:11649) -- that option is about showing unlearnt recipes in the crafting UI, and
-- used as a gate it would silently hand every player a locomotive. The vanilla
-- gates that matter use the "Actually" form too (ISVehicleMechanics.lua:800).
--
-- ==== MULTIPLAYER ====
--
-- On a connected client the mod stands down (RR_MP), so there is no locomotive for
-- a permit to be about. The grant is therefore skipped when blocked: the book reads
-- like any other piece of literature and teaches nothing, because there is nothing
-- here to drive. Fail open, the same rule the rest of the mod's guards follow.
--
-- Console: RR.Licence.report()  -- walks the WHOLE chain and prints every link: is
--            the module loaded, is the read hook installed, is the permit really in
--            this character's knownRecipes, what does the rule say, and what has
--            RR_Ride got cached on the record. Start here when a book was read and
--            the cab is still dead.
--          RR.Licence.refresh() -- drop RR_Ride's cached answer and re-read it.
--          RR.Licence.teach("gp7"|"consist") / RR.Licence.forget() -- debug.
--***********************************************************************

print("[Railroader] RR_Licence.lua: loading...")
require("Railroader/RR_Training")
require("Railroader/RR_Profession")
-- RR_Manual is the notebook's page list (pure). Its client half, RR_ManualPlace, is
-- NOT required here: it is a sibling client file that registers an OnNewGame handler,
-- and we only ever reach into it at seeding time via RR.ManualPlace -- by which point
-- every client file has loaded, whatever order they came in.
require("Railroader/RR_Manual")

local Training = RR and RR.Training

local Licence = {}

--------------------------------------------------------------------------
-- blocked(): the mod has stood down (connected MP client). See the header.
--------------------------------------------------------------------------
local function blocked()
    return RR.MP ~= nil and RR.MP.blocked() == true
end

--------------------------------------------------------------------------
-- knows(chr, recipe): does this character hold this permit?
-- pcall'd: a query that throws must not lock anybody out of anything.
--------------------------------------------------------------------------
local function knows(chr, recipe)
    if not (chr and recipe) then return false end
    local has = false
    pcall(function() has = chr:isRecipeActuallyKnown(recipe) == true end)
    return has
end

--------------------------------------------------------------------------
-- isPro(chr): does he hold the Railroader licence trait? (Task 1.F.) The trait is
-- still the only thing that grants the START advantage (RR_Engine env.pro), so the
-- profession keeps a benefit no book can be read for.
--------------------------------------------------------------------------
function Licence.isPro(chr)
    local P = RR and RR.Profession
    if not P then return true end          -- no profession module: fail open, as it does
    return P.hasLicense(chr) == true
end

--------------------------------------------------------------------------
-- knowledge(chr): the plain-data view the pure rules take.
--   { pro = bool, models = { gp7 = bool, ... }, consist = bool }
--------------------------------------------------------------------------
function Licence.knowledge(chr)
    local k = { pro = Licence.isPro(chr), models = {}, consist = false }
    if not Training then return k end
    for id, m in pairs(Training.MODELS) do
        k.models[id] = knows(chr, m.recipe)
    end
    k.consist = knows(chr, Training.BASE.recipe)
    return k
end

--------------------------------------------------------------------------
-- modelOf(rec): which model this locomotive is. There is one today; the field on
-- the record is what a second one will set, and until then the catalogue's default
-- answers for every loco in the world.
--------------------------------------------------------------------------
function Licence.modelOf(rec)
    if rec and rec.model then return rec.model end
    return Training and Training.DEFAULT_MODEL or "gp7"
end

--------------------------------------------------------------------------
-- mayOperate(chr, modelId) / mayConsist(chr): the two public answers. RR_Ride asks
-- the first one at boarding time and caches it on the record.
--------------------------------------------------------------------------
function Licence.mayOperate(chr, modelId)
    if not Training then return true end               -- module missing: fail open
    return Training.mayOperate(Licence.knowledge(chr), modelId or Licence.modelOf(nil))
end

function Licence.mayConsist(chr)
    if not Training then return true end
    return Training.mayConsist(Licence.knowledge(chr))
end

--------------------------------------------------------------------------
-- say(chr, good, key, arg): the vanilla "you can't / you did" channel -- a halo over
-- the character's head. getText returns the KEY itself when a translation is
-- missing, so a missing string is visible rather than silent.
--
-- The CHARACTER comes from the timed action rather than from getPlayer(), so in
-- split screen the message lands over the man who is actually reading.
--
-- ONE substitution argument, spelled out rather than varargs: a `...` cannot be
-- forwarded into the pcall'd closure (it is not itself a vararg function), and
-- every message here has at most a %1.
--------------------------------------------------------------------------
local function say(chr, good, key, arg)
    local p = chr or getPlayer()
    if not (p and key) then return end
    pcall(function()
        local txt = arg and getText(key, arg) or getText(key)
        if not txt or txt == "" then txt = key end
        if good then HaloTextHelper.addGoodText(p, txt)
        else HaloTextHelper.addBadText(p, txt) end
    end)
end

--------------------------------------------------------------------------
-- modelName(id): the printable name of a model, for the "you learned it" halo.
--------------------------------------------------------------------------
local function modelName(id)
    local key = Training and Training.modelNameKey(id)
    if not key then return tostring(id) end
    local s = key
    pcall(function() s = getText(key) end)
    return s
end

--------------------------------------------------------------------------
-- WE DO NOT GATE READING AT ALL (owner, 2026-08-09). Both books are ordinary
-- literature: vanilla's ISReadABook decides everything about whether, where and
-- how long they may be read, and we add nothing to it. The single hook below is
-- `complete` -- the grant.
--
-- What was here and is gone: a `Licence.allow` fed from ISReadABook:isValid,
-- refusing a model journal outside the cab of its own model (and while she
-- rolled). See the block in RR_Training's header for why it was wrong: the
-- journal is seeded in the cab, so the rule was inert whenever the player
-- complied with it and hostile the one time he had sensibly carried the book
-- away with him. Do not bring back an isValid takeover for these items.
--------------------------------------------------------------------------

--------------------------------------------------------------------------
-- SHOW THE PERMITS IN THE CHARACTER SHEET (owner, 2026-08-09).
--
-- The player's own record of what he knows is the "Recipes" tab of the character
-- info window, and it is built from the ITEM SCRIPTS: ISLiteratureUI:setLists
-- (:358-380) walks every literature item, files anything with LearnedRecipes under
-- "Recipe Books", and puts each recipe id into the Recipes tab. Our two items carry
-- LearnedRecipes, so both permits are listed there with no work from us -- named
-- through Translator.getRecipeName, i.e. Translate/<lang>/Recipes.json.
--
-- What DOES need a line of ours is the icon and the tooltip, because a raw-string
-- recipe has no script to take them from. ISLiteratureUI.miscRecipes (:591) is
-- vanilla's own escape hatch for exactly this case -- it is where "Basic Mechanics",
-- "Herbalist" and "Generator" get theirs, and every one of those is a raw string
-- with no recipe script behind it, granted by a magazine's LearnedRecipes. Same
-- shape, same table.
--------------------------------------------------------------------------
-- Run on OnGameStart, NOT at file load. ISLiteratureUI lives in client/ISUI, and
-- nothing guarantees a vanilla client file is loaded before a mod's -- at load time
-- the global can still be nil, and the guard below would then silently skip the
-- registration for the whole session, costing the icons and tooltips with no error
-- anywhere. By OnGameStart every Lua file exists.
function Licence.registerRecipeEntries()
    if not Training then return end
    if not (ISLiteratureUI and ISLiteratureUI.miscRecipes) then
        print("[Railroader] licence: ISLiteratureUI.miscRecipes missing -- the permits will still "
              .. "be listed on the character sheet, without our icon and tooltip.")
        return
    end
    local rows = { Training.BASE }
    for _, m in pairs(Training.MODELS) do rows[#rows + 1] = m end
    for _, r in ipairs(rows) do
        if r.recipe then
            ISLiteratureUI.miscRecipes[r.recipe] = { tooltip = r.tipKey, icon = r.icon }
        end
    end
end
Events.OnGameStart.Add(function() Licence.registerRecipeEntries() end)

--------------------------------------------------------------------------
-- alreadyHeld(action): did this reader hold this book's permit BEFORE the read?
-- Sampled by the complete() wrapper ahead of the original -- see grant().
--------------------------------------------------------------------------
function Licence.alreadyHeld(action)
    if not Training then return false end
    local chr = action and action.character
    if not chr then return false end
    local itemType
    pcall(function() itemType = action.item:getFullType() end)
    local t = Training.teaches(itemType)
    if not t then return false end
    return knows(chr, t.recipe)
end

--------------------------------------------------------------------------
-- invalidate(): drop RR_Ride's cached "may he work her?" answer for the cab the
-- player is sitting in, so the next tick re-derives it.
--
-- THE CACHE IS THE ONLY THING IN THE CHAIN THAT CAN LIE, and this is the whole
-- reason reading in the seat works at all. Ride.isLicensed answers from e.licensed,
-- which is filled at boarding time and then read every tick; nothing else notices
-- that the man in the seat has just learned something. Without this he would sit in
-- a dead cab until he climbed out and back in.
--
-- Called the MOMENT we know the finished book was one of ours -- before anything
-- that can return early. It used to live at the bottom of the grant, after a
-- learnRecipe check, and an early return there cost the owner exactly this bug
-- in-game (2026-08-09).
--------------------------------------------------------------------------
function Licence.invalidate()
    local rec = RR.Ride and RR.Ride.current
    if not rec then return end
    rec.licensed, rec.pro, rec._licGate = nil, nil, nil
end

--------------------------------------------------------------------------
-- grant(action, knewAlready): the permit, called from our complete() takeover.
--
-- `knewAlready` is sampled BEFORE the original complete() runs, and that is not a
-- detail. Vanilla now grants the permit itself -- ISReadABook:complete ->
-- IsoGameCharacter.ReadLiterature (:4843) walks getLearnedRecipes() and calls
-- learnRecipe on each -- so by the time we are asked the recipe is already known,
-- and a naive "do I know it?" test here would swallow the halo on the very read
-- that earned it.
--
-- WE STILL CALL learnRecipe, AND ITS RETURN VALUE IS NOT A SUCCESS FLAG. IsoGame-
-- Character.learnRecipe (:11672-11683) returns TRUE only when it actually added the
-- string, and FALSE when the character already knew it. Since vanilla now gets there
-- first, false is the NORMAL outcome of our backstop call -- and reading it as a
-- failure is precisely the bug that shipped to the owner on 2026-08-09: the grant
-- printed "FAILED to record", returned, and skipped both the halo and the cache
-- drop, so a permit that WAS recorded left the cab dead. The honest test is not what
-- the call returned but whether the permit is there afterwards, whoever put it there.
--
-- Re-reading a book you have already read says nothing, which is honest: nothing
-- changed.
--------------------------------------------------------------------------
function Licence.grant(action, knewAlready)
    if not (Training and Training.C.ENABLED) then return end
    local chr = action and action.character
    if not chr then return end

    local itemType
    pcall(function() itemType = action.item:getFullType() end)
    local t = Training.teaches(itemType)
    if not t then return end                                     -- not ours: not our business

    -- One of ours finished. Whatever else happens below, the seated driver's cached
    -- answer is now out of date.
    Licence.invalidate()

    if knewAlready then return end                               -- already qualified: say nothing

    pcall(function() chr:learnRecipe(t.recipe) end)
    if not knows(chr, t.recipe) then
        print("[Railroader] licence: '" .. tostring(t.recipe) .. "' did NOT stick -- "
              .. "neither vanilla's LearnedRecipes nor our own learnRecipe recorded it.")
        return
    end

    if t.kind == "model" then
        say(chr, true, "IGUI_RR_LearnedModel", modelName(t.model))
        print("[Railroader] licence: read the " .. tostring(itemType)
              .. " -- qualified to run a " .. tostring(t.model) .. " light engine.")
    else
        say(chr, true, "IGUI_RR_LearnedConsist")
        print("[Railroader] licence: read the train-handling manual -- qualified to work a consist.")
    end
end

--------------------------------------------------------------------------
-- The takeover. Guarded on ISReadABook existing (it is shared/TimedActions, loaded
-- long before us, but a missing class must not kill this file: hard rule 2's
-- failure mode is a whole file going silent).
--------------------------------------------------------------------------
if ISReadABook and not ISReadABook._rrLicenceHooked then
    ISReadABook._rrLicenceHooked = true       -- a second pass would wrap our own wrapper
    local origComplete = ISReadABook.complete

    function ISReadABook:complete()
        -- Sample BEFORE the original runs: vanilla's own ReadLiterature grants our
        -- LearnedRecipes in there, so afterwards "does he know it?" is true either way
        -- and the halo for the read that earned it would never fire. See Licence.grant.
        local knew = false
        if not blocked() then
            local okq, res = pcall(Licence.alreadyHeld, self)
            knew = okq and res == true
        end
        local out = true
        if origComplete then out = origComplete(self) end
        if not blocked() then pcall(Licence.grant, self, knew) end
        return out
    end
elseif not ISReadABook then
    print("[Railroader] licence: ISReadABook is missing -- the books teach nothing this session.")
end

--------------------------------------------------------------------------
-- seedCab(rec, cont): a locomotive carries her own manual. Called once, by
-- RR_CabLocker, the moment a brand-new locker is built.
--
-- WHY THIS IS DETERMINISTIC AND NOT A ROLL. It is the entire first-contact loop:
-- walk to the 800 -> climb in (boarding was never gated) -> open the locker -> find
-- the manual -> read it where you are sitting -> drive away. A roll that came up
-- empty would leave a player in a cab he cannot use with nothing telling him why.
-- Loot tables (server/Railroader/RR_Distributions.lua) are the SECOND copy, for the
-- player who lost this one.
--
-- ==== THE MANUAL BELONGS TO THE LOCOMOTIVE'S DISCOVERY, NOT TO HER LOCKER ====
--
-- ONCE PER LOCOMOTIVE, AND ONLY ONE THAT IS BEING FOUND FOR THE FIRST TIME (owner,
-- 2026-08-09, on loading a save that predates all of this and finding a manual in a
-- cab he had already been through). Hanging the decision on the LOCKER was wrong in
-- exactly that case: a locomotive that has been driven for weeks gets her locker
-- reconciled on every load like any other, so a patch materialised loot inside a
-- container the player had already emptied. That is the same trick as a mod
-- restocking looted shelves, and it is not what "she was found with her manual
-- aboard" means.
--
-- So the decision is ARMED AT SPAWN and the locker merely carries it out:
--   * TrainEntity's genuine creation path calls armCabSeed -> rrJournalPending on
--     the loco's own modData;
--   * the first locker built for her honours it, drops the pending flag and stamps
--     rrJournalSeeded;
--   * re-adopting a locomotive on load calls armLegacyCabSeed, which arms her ONCE
--     if she has never been seeded and is not already owed one -- see that function
--     for why an old save is a first opening rather than a restock. Whatever the
--     route in, rrJournalSeeded is the single guarantee of "once ever".
-- Pending lives in modData rather than on the record so it survives a save made
-- between the spawn and the first locker build.
--
-- WHY THE SEED IS DETERMINISTIC AND NOT A ROLL. It is the entire first-contact loop:
-- walk to the 800 -> climb in (boarding was never gated) -> open the locker -> find
-- the manual -> read it -> drive away. A roll that came up empty would leave a
-- player in a cab he cannot use with nothing telling him why. Loot tables
-- (server/Railroader/RR_Distributions.lua) are the SECOND copy, for the player who
-- lost this one.
--------------------------------------------------------------------------
local SEED_FLAG    = "rrJournalSeeded"    -- her cab has been stocked
local PENDING_FLAG = "rrJournalPending"   -- ...and this one is owed it
local NOTE_FLAG    = "rrNotebookSeeded"   -- the engineer's notebook was left aboard too

--------------------------------------------------------------------------
-- THE NOTEBOOK IS LEFT IN HER CAB FOR ANYONE WHO IS NOT A RAILROADER (owner,
-- 2026-08-09, after the first full in-game pass).
--
-- The operating journal grants the PERMIT. It does not teach the controls -- which
-- lever is the reverser, that W and S are the throttle, that the diesel only cranks
-- with the handle centred. All of that lives in the engineer's notebook, and until
-- now only a character who took the profession carried one. That was right while the
-- profession was the only way into the cab; the moment a survivor could read his way
-- in, it left him qualified and with no idea which control does what.
--
-- So the 800 keeps the notebook of the man who ran her, in the cab, beside her
-- manual -- which is where it would be. A RAILROADER gets nothing extra: he starts
-- with his own copy (RR_ManualPlace, OnNewGame) and a second one is clutter.
--
-- The PHOTO ALBUM stays his alone, and the difference is the point: the album is who
-- he was and teaches nothing, the notebook is the mod's manual and has to reach
-- whoever is driving.
--
-- Pages are not free with the item: a notebook is blank until Literature.addPage
-- fills it, which is RR_ManualPlace.write's job. Placing one without writing it
-- would hand the player an empty pad -- worse than nothing, because it looks like
-- the feature working.
--------------------------------------------------------------------------
local function leaveNotebook(cont)
    local Manual, Place = RR.Manual, RR.ManualPlace
    if not (Manual and Place and Place.write) then
        print("[Railroader] cab locker: RR_Manual/RR_ManualPlace missing -- no notebook left aboard.")
        return false
    end
    local book
    pcall(function() book = cont:AddItem(Manual.ITEM_TYPE) end)
    if not book then
        print("[Railroader] cab locker: could NOT leave " .. tostring(Manual.ITEM_TYPE) .. ".")
        return false
    end
    local n = 0
    pcall(function() n = Place.write(book) end)
    print(string.format("[Railroader] cab locker: left the engineer's notebook aboard (%d/%d pages) "
                        .. "-- the driver is not a Railroader and would otherwise have no manual.",
                        n, Manual.pageCount()))
    return true
end

--------------------------------------------------------------------------
-- armCabSeed(rec): this locomotive is being created NOW -- she is owed her manual.
-- Called only from the genuine spawn path, never from re-adoption on load.
--------------------------------------------------------------------------
function Licence.armCabSeed(rec)
    if not (Training and Training.C.SEED_IN_CAB) then return end
    if not (rec and rec.animal) then return end
    pcall(function()
        local md = rec.animal:getModData()
        if md and not md[SEED_FLAG] then md[PENDING_FLAG] = true end
    end)
end

--------------------------------------------------------------------------
-- armLegacyCabSeed(rec): a locomotive that was ALREADY IN THE WORLD when this
-- update landed is owed her manual too -- once (owner, 2026-08-09).
--
-- THIS REVERSES "re-adoption arms nothing", AND THE REASON IS THAT THE LOCKER SHIPS
-- IN THE SAME UPDATE AS THE MANUAL. The rule above was written against restocking:
-- a locker is reconciled on every load forever, so deciding there would drop loot
-- into a container the player had already emptied. That argument does not reach a
-- locomotive from 1.1 or earlier, because she has never had a container at all --
-- the first locker ever built for her is a genuine first opening, and finding the
-- last crew's manual in it is exactly what the fiction says should be there.
-- Without this, every existing save would have to hunt the manual in loot for a
-- locomotive that is defined as carrying her own copy, and the 800 is usually the
-- FIRST one a player meets.
--
-- ONCE EVER, and the guarantee is not a new flag: seedCab stamps SEED_FLAG the
-- moment it places the journal, and this refuses to arm anything that carries it.
-- So a player who reads the manual and drops it, sells it, or burns it is not
-- handed a fresh one on the next load -- which is the whole difference between
-- "she was found with it aboard" and a mod that respawns loot.
--
-- Called from TrainEntity.adoptAnimal, i.e. on EVERY load. That is intentional and
-- safe: the second call is a no-op through SEED_FLAG, and arming a locomotive whose
-- locker never gets built this session simply leaves the debt standing in modData
-- until it does.
--------------------------------------------------------------------------
function Licence.armLegacyCabSeed(rec)
    if not (Training and Training.C.SEED_IN_CAB and Training.C.SEED_LEGACY) then return false end
    if not (rec and rec.animal) then return false end

    local armed = false
    pcall(function()
        local md = rec.animal:getModData()
        if not md then return end                      -- cannot promise "once": refuse
        if md[SEED_FLAG] or md[PENDING_FLAG] then return end
        md[PENDING_FLAG] = true
        armed = true
    end)
    if armed then
        print("[Railroader] licence: this locomotive predates the cab locker -- her operating "
              .. "manual is owed and goes in the first locker built for her (once ever).")
    end
    return armed
end

function Licence.seedCab(rec, cont)
    if not (Training and Training.C.SEED_IN_CAB) then return end
    if not (rec and rec.animal and cont) then return end

    local md
    pcall(function() md = rec.animal:getModData() end)
    -- No modData means we cannot promise "once ever", and a manual appearing twice is
    -- worse than one never appearing. Refuse rather than guess.
    if not md then return end
    if md[SEED_FLAG] then return end
    -- Not armed: she was already in the world before this feature, or before this
    -- session. She keeps whatever is in her locker, including nothing.
    if not md[PENDING_FLAG] then return end

    local item = Training.journalFor(Licence.modelOf(rec))
    if not item then return end

    local added = false
    pcall(function() added = cont:AddItem(item) ~= nil end)
    md[PENDING_FLAG] = nil
    md[SEED_FLAG]    = true
    print("[Railroader] cab locker: " .. (added and ("seeded " .. item) or
          ("could NOT seed " .. tostring(item))) .. ".")

    -- ...and the notebook, for a driver who did not come with one. Decided HERE
    -- rather than at arming time because this is the moment items are placed, and
    -- the answer ("is the man who found her a Railroader?") cannot change afterwards.
    if not md[NOTE_FLAG] and not Licence.isPro(getPlayer()) then
        if leaveNotebook(cont) then md[NOTE_FLAG] = true end
    end
end

--------------------------------------------------------------------------
-- backfillProfession(chr): a Railroader holds every permit, and his CHARACTER
-- SHEET has to say so.
--
-- RR_Profession hangs the permits on the licence trait, which the engine applies at
-- character CREATION (IsoWorld:2244-2257 / LuaManager:6414-6425). That reaches
-- nobody who was rolled before it existed -- including the owner's own save -- so
-- those characters would keep driving fine while their Recipes tab listed both of
-- their capabilities as unknown. This closes that, and doubles as the safety net if
-- the creation-time grant ever misses.
--
-- Idempotent by construction: learnRecipe is a no-op on a permit already held
-- (:11673), so this is a couple of list lookups per player per load.
--
-- Only for the PROFESSION. A reader's permits come from the books and nothing else
-- may hand them out -- that is the point of the whole system.
--------------------------------------------------------------------------
function Licence.backfillProfession(chr)
    if blocked() then return end
    if not (Training and Training.C.ENABLED and chr) then return end
    if not Licence.isPro(chr) then return end

    local added = 0
    for _, recipe in ipairs(Training.allRecipes()) do
        if not knows(chr, recipe) then
            local ok = false
            pcall(function() ok = chr:learnRecipe(recipe) ~= false end)
            if ok then added = added + 1 end
        end
    end
    if added > 0 then
        print(("[Railroader] licence: Railroader profession -- recorded %d permit(s) "
               .. "that predate the character sheet listing them."):format(added))
        local rec = RR.Ride and RR.Ride.current
        if rec then rec.licensed = nil end
    end
end

-- OnCreatePlayer fires for every player on a new game AND on every load, and in
-- split screen it fires per player -- which is why the character comes in as an
-- argument rather than being looked up with getPlayer().
Events.OnCreatePlayer.Add(function(_, playerObj)
    pcall(Licence.backfillProfession, playerObj)
end)

--------------------------------------------------------------------------
-- Console helpers (never gated -- they cannot be hit by accident and they are how
-- a tester reproduces a report).
--------------------------------------------------------------------------
-- report(): the ONE command that answers "I read it and the cab is still dead".
-- It walks the whole chain in order and prints every link, so the answer is a line
-- in console.txt rather than a guess: is the module loaded, is the read hook
-- installed, is the permit actually in this character's knownRecipes, what does the
-- rule say about it, and what has RR_Ride got CACHED on the locomotive record --
-- which is the one link that can disagree with all the others.
function Licence.report()
    local p = getPlayer()
    print("[Railroader] ==== licence report ====")
    print("  RR.Training loaded : " .. tostring(Training ~= nil))
    print("  read hook installed: " .. tostring(ISReadABook ~= nil and ISReadABook._rrLicenceHooked == true)
          .. "   (false = the books teach nothing; look for a load error above)")
    if not Training then print("  -- nothing else can be answered without RR_Training."); return end

    -- The raw list, straight out of the character. If a permit is missing HERE the
    -- read never granted it; if it is present and the cab is still dead, the fault
    -- is downstream.
    local raw = {}
    pcall(function()
        local list = p and p:getKnownRecipes()
        if list then for i = 0, list:size() - 1 do raw[#raw + 1] = list:get(i) end end
    end)
    local ours = {}
    for _, r in ipairs(Training.allRecipes()) do
        ours[#ours + 1] = r .. "=" .. tostring(knows(p, r))
    end
    print("  knownRecipes total : " .. #raw .. "   ours: " .. table.concat(ours, "  "))

    local k = Licence.knowledge(p)
    print(string.format("  ENABLED=%s  SEED_IN_CAB=%s  (books read anywhere -- no cab rule)",
          tostring(Training.C.ENABLED), tostring(Training.C.SEED_IN_CAB)))
    print("  Railroader profession (licence trait): " .. tostring(k.pro))
    for id in pairs(Training.MODELS) do
        print(string.format("  model %-6s journal read: %s  -> may operate: %s",
              id, tostring(k.models[id]), tostring(Training.mayOperate(k, id))))
    end
    print("  train-handling manual read: " .. tostring(k.consist)
          .. "  -> may work a consist: " .. tostring(Training.mayConsist(k)))

    local rec = RR.Ride and RR.Ride.current
    print("  in a cab right now: " .. (rec and (Licence.modelOf(rec) .. ", speed "
          .. string.format("%.2f m/s", rec.speed or 0)) or "no"))
    if rec then
        -- THE LINK THAT LIES. RR_Ride caches the answer on the record; if this says
        -- false while "may operate" above says true, the cache went stale and
        -- RR.Licence.refresh() fixes it on the spot.
        print("  cached on the record: e.licensed=" .. tostring(rec.licensed)
              .. "  e.pro=" .. tostring(rec.pro))
        local live
        pcall(function() live = RR.Ride.isLicensed(rec) end)
        print("  RR_Ride.isLicensed(): " .. tostring(live))
    end
    print("[Railroader] ==== end ====")
end

--------------------------------------------------------------------------
-- refresh(): drop RR_Ride's cached answer for the locomotive you are sitting in and
-- re-read it. The grant does this itself; this is the manual lever for the case
-- where it did not, so a session is not lost to climbing out and back in.
--------------------------------------------------------------------------
function Licence.refresh()
    local rec = RR.Ride and RR.Ride.current
    if not rec then print("[Railroader] licence: not in a cab -- nothing to refresh."); return end
    Licence.invalidate()
    local live
    pcall(function() live = RR.Ride.isLicensed(rec) end)
    print("[Railroader] licence: cache dropped -- may operate her now: " .. tostring(live))
end

function Licence.teach(what)
    local p = getPlayer()
    if not (p and Training) then return end
    local recipe = (what == "consist") and Training.BASE.recipe or Training.recipeFor(what or Training.DEFAULT_MODEL)
    if not recipe then print("[Railroader] licence: no such course '" .. tostring(what) .. "'"); return end
    pcall(function() p:learnRecipe(recipe) end)
    local rec = RR.Ride and RR.Ride.current
    if rec then rec.licensed = nil end
    print("[Railroader] licence: granted '" .. recipe .. "'.")
end

--------------------------------------------------------------------------
-- forget(): drop every book permit this character holds, so a tester can replay
-- the untrained approach to the locomotive without rolling a new save.
--
-- IT IS ONLY MEANINGFUL ON A NON-RAILROADER, and it says so rather than leaving
-- someone to conclude the gate is broken. On a Railroader it changes nothing that
-- matters: he drives on the TRAIT (mayOperate short-circuits on `pro`), so wiping
-- the recipe entries only blanks his character sheet -- and backfillProfession puts
-- them straight back on the next load, because holding the trait is the condition
-- it tests. To play an untrained character, roll one without the profession.
--------------------------------------------------------------------------
function Licence.forget()
    local p = getPlayer()
    if not (p and Training) then return end
    -- getKnownRecipes() is the live ArrayList, so removing from it really forgets.
    pcall(function()
        local list = p:getKnownRecipes()
        local drop = { Training.BASE.recipe }
        for _, m in pairs(Training.MODELS) do drop[#drop + 1] = m.recipe end
        for _, r in ipairs(drop) do
            for i = list:size() - 1, 0, -1 do
                if list:get(i) == r then list:remove(i) end
            end
        end
    end)
    local rec = RR.Ride and RR.Ride.current
    if rec then rec.licensed = nil end
    print("[Railroader] licence: forgot every book permit.")
    if Licence.isPro(p) then
        print("[Railroader]   ...but this character is a RAILROADER, so nothing about driving "
              .. "changed: the profession qualifies him on the trait, not on these entries. "
              .. "All this did was blank his character sheet, and backfillProfession will put "
              .. "them back on the next load. To test an untrained driver, roll a character "
              .. "WITHOUT the Railroader profession.")
    end
end

RR = RR or {}
RR.Licence = Licence
print("[Railroader] RR_Licence.lua: loaded OK (the books teach; the profession still outranks them)")

return Licence
