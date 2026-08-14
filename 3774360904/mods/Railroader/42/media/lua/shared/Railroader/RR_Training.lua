--***********************************************************************
-- Railroader / RR_Training  -- WHO MAY WORK A LOCOMOTIVE, and how a survivor who
-- did not take the profession learns to.
--
-- Until now the answer was one bit: the Railroader profession, or nothing. This
-- module is the other road in -- the books -- and it keeps the answer BINARY on
-- purpose. There is no driving skill, no 0..10 bar, no partial competence. A
-- locomotive engineer is a federally certified job (49 CFR Part 240): you are
-- certified or you may not touch the controller. A scale would add difficulty
-- without adding truth, so there isn't one. See docs/TRAINING_DESIGN.md.
--
-- THREE THINGS GRANT COMPETENCE, and they are independent permits rather than
-- rungs on a ladder:
--
--   1. A MODEL JOURNAL  ("EMD Operating Manual No. 2312 (GP7)")
--      Read ANYWHERE, like any other literature -- short, one sitting. Grants:
--      run THAT model light engine (no cars). One journal per model. (It is
--      FOUND in the cab, which is a different thing entirely from being readable
--      only there; see the deleted cab rule further down.)
--
--   2. THE BASE MANUAL  (train handling + air brakes)
--      Read anywhere, long, over several sittings. Grants: work with a CONSIST --
--      coupling, uncoupling, moving with cars. One book for the whole fleet.
--      The real documentation splits exactly here: the GP7 manual deliberately
--      skips the brake sections, assuming the engineer already knows the 6ET
--      system. The most dangerous part was always a separate book.
--
--   3. THE RAILROADER PROFESSION
--      All of the above at once, on every model, plus a real edge at the starter
--      (RR_Engine: START_BASE_PRO and the halved crank drain).
--
-- The order of reading is free. No dependency between the two books is needed:
-- someone who has only read the base manual still cannot sit down at a machine he
-- has never seen a manual for, so the restriction falls out by itself.
--
-- WHERE THE KNOWLEDGE IS STORED (the engine half, RR_Licence): in the character's
-- own knownRecipes list, via learnRecipe(name) / isRecipeActuallyKnown(name).
-- Verified in the decompile: IsoGameCharacter.learnRecipe (:11668) adds the raw
-- STRING when no recipe script by that name exists, isRecipeKnown(name, true)
-- (:11649) then tests exactly knownRecipes.contains(name), and
-- IsoGameCharacter.save (:5358-5362) round-trips the whole list. So it persists,
-- costs us no ModData, and needs no recipe script.
--   THE TRAP, and why isRecipeActuallyKnown is the only form we may call:
--   isRecipeKnown(name) WITHOUT ignoreSandbox returns TRUE for everybody when the
--   sandbox option SeeNotLearntRecipe is on. That option is a UI convenience about
--   showing unlearnt recipes; used as our gate it would hand every player a
--   locomotive. isRecipeActuallyKnown == isRecipeKnown(name, true) is the honest
--   query, and it is the one vanilla itself uses for real gates
--   (ISVehicleMechanics.lua:800, Vehicles.lua:1092).
--
-- THE RECIPE STRINGS ARE A SAVE FORMAT. They are written into every character
-- that ever learns one. Never rename one; add new ids instead.
--
-- PURE LUA, NO ENGINE DEPENDENCIES -- unit-tested in a bare interpreter
-- (tests/run_tests.lua). Everything here takes plain booleans and strings; the
-- client adapter (client/Railroader/RR_Licence.lua) does the asking and telling.
--***********************************************************************

local Training = {}

--------------------------------------------------------------------------
-- Tunables. Live: changing one from the console takes effect on the next query,
-- the same contract as RR.Locker.C and RR.Obstacle.SOLID_IS_HARD.
--------------------------------------------------------------------------
Training.C = {
    ENABLED     = true,   -- false: the books do nothing and only the profession counts
                          -- (i.e. exactly the pre-1.1.1 rule -- an escape hatch, not a plan)
    SEED_IN_CAB = true,   -- a spawned locomotive carries her own journal in the cab locker
    SEED_LEGACY = true,   -- ...and so does one that was already in the world when 1.1.1
                          -- landed, ONCE, the first time a locker is built for her. False
                          -- restores the 1.1.1-development rule (loot only for old saves).
                          -- See Licence.armLegacyCabSeed for why this is not a restock.

    -- THE BASE MANUAL DOES NOT SPAWN YET, AND THAT IS THE POINT (owner, 2026-08-09).
    -- Its permit has no consumer until rolling stock lands (ROADMAP task 3.B), so a
    -- player who finds it spends six and a half in-game hours reading, is told he has
    -- learned train handling, and then discovers there is nothing in the world to
    -- couple. Wording alone cannot fix that -- the honest fix is that the book is not
    -- there to be found. Everything else about it stays built and tested: the item,
    -- the permit, the character-sheet entry, the profession's copy. Turning it on when
    -- the first boxcar exists is THIS ONE FLAG, and the loot rows are still written
    -- down in server/Railroader/RR_Distributions.lua waiting for it.
    CONSIST_BOOK_IN_WORLD = false,

    -- NO REQUIRE_CAB. There is deliberately no knob for "read it in the cab" --
    -- see the block further down; the rule was deleted, not switched off.
}

-- The model a record is assumed to be when nothing says otherwise. There is
-- exactly one locomotive in the world today; the field exists so the second one
-- needs no new mechanism, only a row in MODELS and its own journal item.
Training.DEFAULT_MODEL = "gp7"

--------------------------------------------------------------------------
-- The catalogue. One row per locomotive model:
--   item    the journal item (full type) that teaches it
--   recipe  the string written into the character's knownRecipes -- A SAVE FORMAT
--   nameKey the translation key naming the model in messages
--------------------------------------------------------------------------
Training.MODELS = {
    gp7 = {
        id      = "gp7",
        item    = "Base.RR_ModelManual_GP7",
        recipe  = "RR_Operate_GP7",
        nameKey = "IGUI_RR_Model_GP7",
        -- Shown in the character sheet's "Recipes" tab (RR_Licence registers these
        -- with ISLiteratureUI.miscRecipes). `icon` is a texture name -- vanilla's
        -- if it starts with a vanilla item, ours (media/textures/<icon>.png) if it
        -- starts with Item_RR_. A unit test checks the file for the latter.
        tipKey  = "Tooltip_Recipe_RR_Operate_GP7",
        icon    = "Item_PaperReport1",
    },
}

-- The one book that is not about a model.
Training.BASE = {
    item    = "Base.RR_TrainHandlingBook",
    recipe  = "RR_TrainHandling",
    nameKey = "IGUI_RR_ConsistWork",
    tipKey  = "Tooltip_Recipe_RR_TrainHandling",
    icon    = "Item_RR_TrainHandling",
}

--------------------------------------------------------------------------
-- modelOfJournal(itemType) -> model id, or nil if this is not a model journal.
-- Accepts the full type ("Base.RR_ModelManual_GP7") or the bare one.
--------------------------------------------------------------------------
local function bare(t)
    t = tostring(t or "")
    local dot = string.find(t, ".", 1, true)
    return dot and string.sub(t, dot + 1) or t
end

function Training.modelOfJournal(itemType)
    if not itemType then return nil end
    local b = bare(itemType)
    for id, m in pairs(Training.MODELS) do
        if bare(m.item) == b then return id end
    end
    return nil
end

--------------------------------------------------------------------------
-- isBaseManual(itemType) -> is this the train-handling book?
--------------------------------------------------------------------------
function Training.isBaseManual(itemType)
    if not itemType then return false end
    return bare(itemType) == bare(Training.BASE.item)
end

--------------------------------------------------------------------------
-- teaches(itemType) -> what reading this item grants, or nil if it teaches
-- nothing of ours. { kind = "model"|"consist", recipe = , model = }
--------------------------------------------------------------------------
function Training.teaches(itemType)
    local id = Training.modelOfJournal(itemType)
    if id then
        return { kind = "model", recipe = Training.MODELS[id].recipe, model = id }
    end
    if Training.isBaseManual(itemType) then
        return { kind = "consist", recipe = Training.BASE.recipe }
    end
    return nil
end

--------------------------------------------------------------------------
-- spawnsInWorld(itemType) -> may the loot tables place this book at all?
--
-- The one caller is server/Railroader/RR_Distributions.lua. It is a function rather
-- than a line in that file so the rule is unit-tested and lives beside the flag it
-- reads: a book whose permit has no consumer must not be findable (see
-- C.CONSIST_BOOK_IN_WORLD).
--
-- Anything that is not ours answers true -- we are not in the business of holding
-- back somebody else's loot.
--------------------------------------------------------------------------
function Training.spawnsInWorld(itemType)
    if not Training.isBaseManual(itemType) then return true end
    return Training.C.CONSIST_BOOK_IN_WORLD == true
end

--------------------------------------------------------------------------
-- allRecipes() -> every permit string this system can hand out, in a stable
-- order (the base manual first, then models by id).
--
-- This is the list the RAILROADER PROFESSION grants wholesale: the profession has
-- always implied all of it, but until the permits became real entries in the
-- character's knownRecipes there was nothing to hand him, and his own character
-- sheet listed both of his capabilities as unknown (owner, 2026-08-09).
-- RR_Profession feeds this to the licence trait's CharacterTraitDefinition, and
-- RR_Licence backfills it for characters rolled before that existed.
--------------------------------------------------------------------------
function Training.allRecipes()
    local ids = {}
    for id in pairs(Training.MODELS) do ids[#ids + 1] = id end
    table.sort(ids)
    local out = { Training.BASE.recipe }
    for _, id in ipairs(ids) do out[#out + 1] = Training.MODELS[id].recipe end
    return out
end

--------------------------------------------------------------------------
-- recipeFor(modelId) -> the knownRecipes string for a model, or nil.
--------------------------------------------------------------------------
function Training.recipeFor(modelId)
    local m = Training.MODELS[modelId or ""]
    return m and m.recipe or nil
end

--------------------------------------------------------------------------
-- journalFor(modelId) -> the journal item type for a model, or nil.
--------------------------------------------------------------------------
function Training.journalFor(modelId)
    local m = Training.MODELS[modelId or ""]
    return m and m.item or nil
end

--------------------------------------------------------------------------
-- mayOperate(k, modelId) -> may this survivor start and drive THIS model?
--
--   k.pro    bool            holds the Railroader licence trait (the profession)
--   k.models {id -> bool}    models whose journal he has read
--
-- The profession is a blanket yes -- it is the certification, and a certified
-- engineer is qualified on the road, not on one machine. Anyone else needs the
-- journal for the machine in front of him.
--
-- ENABLED == false collapses this back to the profession-only rule the mod
-- shipped with, so a save can be put back the way it was without a rebuild.
--------------------------------------------------------------------------
function Training.mayOperate(k, modelId)
    k = k or {}
    if k.pro == true then return true end
    if Training.C.ENABLED ~= true then return false end
    local models = k.models
    if type(models) ~= "table" then return false end
    return models[modelId or Training.DEFAULT_MODEL] == true
end

--------------------------------------------------------------------------
-- mayConsist(k) -> may he couple cars and move a train rather than a light engine?
--
-- NOTHING READS THIS YET. Rolling stock is ROADMAP task 3.B and is designed but
-- not built; the permit exists now so the book can be found, read and remembered
-- from this release on, and so a save made today already knows who is qualified
-- when the first boxcar lands. Wire the coupling code to this function, not to a
-- second copy of the rule.
--------------------------------------------------------------------------
function Training.mayConsist(k)
    k = k or {}
    if k.pro == true then return true end
    if Training.C.ENABLED ~= true then return false end
    return k.consist == true
end

--------------------------------------------------------------------------
-- THERE IS NO RULE ABOUT WHERE A BOOK MAY BE READ, AND THERE MUST NOT BE ONE
-- AGAIN (owner, 2026-08-09). Both books read like every other piece of
-- literature in the game: anywhere, no extra condition, vanilla ISReadABook and
-- nothing of ours on top.
--
-- What used to be here: `readVerdict` + `refusalKey` + `C.REQUIRE_CAB`, gating a
-- model journal to the cab of its own model (and refusing while she rolled). The
-- reasoning was real -- a builder's manual is a reference AT the machine, written
-- in cross-references to panels you have to look at -- but as a GAME RULE it was
-- redundant and it punished the player:
--
--   * The journal is SEEDED IN THE CAB (Licence.seedCab). So the player is
--     already standing in the only place the rule allowed, every time. The rule
--     could therefore never teach him anything he was not about to do anyway.
--   * The one moment it DID fire was when he had done the sensible thing and
--     taken the book with him. A refusal is the whole of what it added.
--
-- A rule that is inert when you comply and hostile when you use your head is not
-- a rule, it is a trap. Deleted rather than switched off with a flag -- a flag
-- that only has to be flipped is a flag that gets shipped flipped. Two unit tests
-- guard the deletion (readVerdict and REQUIRE_CAB must not exist), the same way
-- the WET_FAC deletion is guarded in RR_Engine.
--
-- The realism argument survives where it belongs: in the tooltip, as flavour
-- describing what the book IS, not as a lock on when it opens.
--------------------------------------------------------------------------

--------------------------------------------------------------------------
-- modelNameKey(modelId) -> the translation key naming a model, or nil.
--------------------------------------------------------------------------
function Training.modelNameKey(modelId)
    local m = Training.MODELS[modelId or ""]
    return m and m.nameKey or nil
end

RR = RR or {}
RR.Training = Training

return Training
