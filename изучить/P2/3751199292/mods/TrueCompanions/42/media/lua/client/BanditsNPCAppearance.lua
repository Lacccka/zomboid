--
-- True Companions - Appearance Workstation (v0.74)
--
-- A mirrored vanity you build and then use to rename a companion and change her hair,
-- beard and hair colour. See media/scripts/entity_tcappearance.txt for the entity.
--
-- WHERE THE LOOK IS STORED, AND WHY IT SURVIVES.
--
-- Styles go on the brain under the names BANDITS ITSELF USES -- brain.hairStyle and
-- brain.beardStyle -- because Bandit.ApplyVisuals has an explicit-override branch that
-- reads exactly those (Bandit.lua:250-264), so the engine and this module cannot
-- disagree about what a companion looks like.
--
-- COLOUR IS THE EXCEPTION, AND IT HAS TO BE. `brain.hairColor` IS TWO DIFFERENT TYPES
-- IN BANDITS DEPENDING ON WHICH SPAWNER MADE THE NPC:
--
--   clan NPCs (brain.cid set, which is EVERY survivor the beacon brings in)
--     BanditServerSpawner.lua:327   brain.hairColor = general.hairColor or 1
--     Bandit.lua:169-171            Bandit.GetHairColor(brain.hairColor)  -- an INDEX
--
--   the override branch
--     Bandit.lua:257-258            ImmutableColor.new(brain.hairColor.r, ...)  -- a TABLE
--
-- v0.74.0-v0.74.6 wrote the table form into that one field and read it back assuming a
-- table. On a beacon-recruited companion it was an index, so A.Apply reached
-- ImmutableColor.new(nil, nil, nil, 1) -- a Java CONSTRUCTOR call, whose failure BYPASSES
-- pcall -- and the player got an error screen the moment the recruit's visuals refreshed
-- (4 Aug, BanditsNPCAppearance.lua:202, reported on recruiting with a food item).
--
-- Writing a table into it was the more dangerous half of the same mistake: the clan path
-- would then do Bandit.GetHairColor(<table>) -> tab[<table>] -> nil -> nil.r, an error
-- inside BANDITS' code that we caused and could not guard.
--
-- So our chosen colour lives under OUR OWN names, tcHairColor / tcBeardColor, and
-- brain.hairColor is left exactly as Bandits set it. normColor() below reads either
-- convention, so the workstation still starts from the colour she actually has.
--
-- We ALSO apply them ourselves from ApplyClothingVisuals on every restore, because
-- that override branch is an `else` -- it does not run on every path -- and a
-- companion whose hair reverted after a save would be worse than no feature at all.
-- Belt and braces on something the player has explicitly chosen.
--
-- The fields are in Persistence.CUSTOM_FIELDS, so they travel through the roster and
-- back onto a rebuilt body.
--
-- MODDED HAIR IS INCLUDED FOR FREE. getAllHairStyles(isFemale) is the engine's own
-- registry, which is what mods add to -- the same call vanilla's character creator
-- makes -- so anything a hair mod installs appears here without this file knowing the
-- mod exists. Styles flagged isNoChoose() are filtered out exactly as the creator
-- filters them: those are internal entries not meant to be picked.
--

BanditsNPC = BanditsNPC or {}
BanditsNPC.Appearance = BanditsNPC.Appearance or {}
local A = BanditsNPC.Appearance

-- Every facing of the vanity. This is how the object is recognised in the world, so
-- it must cover all four or a station built facing north stops being a station.
A.SPRITES = {
    N = "furniture_storage_01_41", E = "furniture_storage_01_42",
    S = "furniture_storage_01_43", W = "furniture_storage_01_40",
}

local SPRITE_SET = {}
for facing, name in pairs(A.SPRITES) do SPRITE_SET[name] = facing end

-- HAIR DYES. Our own palette rather than the engine's getCommonHairColor(), which is a
-- method on a SurvivorDesc and returns only the NATURAL range -- no use at all for
-- "dye her hair blue", which is half of what the player asked for. Naturals first so
-- the sensible choices are not buried behind the novelty ones.
A.COLORS = {
    { key = "black",     r = 0.10, g = 0.09, b = 0.08 },
    { key = "darkbrown", r = 0.24, g = 0.15, b = 0.09 },
    { key = "brown",     r = 0.40, g = 0.26, b = 0.15 },
    { key = "auburn",    r = 0.51, g = 0.24, b = 0.13 },
    { key = "ginger",    r = 0.72, g = 0.36, b = 0.14 },
    { key = "blonde",    r = 0.83, g = 0.72, b = 0.44 },
    { key = "platinum",  r = 0.92, g = 0.90, b = 0.82 },
    { key = "grey",      r = 0.62, g = 0.62, b = 0.62 },
    { key = "white",     r = 0.95, g = 0.95, b = 0.95 },
    { key = "red",       r = 0.72, g = 0.12, b = 0.12 },
    { key = "pink",      r = 0.90, g = 0.45, b = 0.70 },
    { key = "purple",    r = 0.52, g = 0.25, b = 0.72 },
    { key = "blue",      r = 0.20, g = 0.38, b = 0.78 },
    { key = "teal",      r = 0.15, g = 0.60, b = 0.60 },
    { key = "green",     r = 0.22, g = 0.58, b = 0.28 },
}

-- Written as literal T() calls: the key scanner only sees keys spelled out at the call
-- site, so a lookup built from A.COLORS[i].key would never reach UI.json and could
-- never be translated.
function A.ColorName(key)
    if key == "darkbrown" then return BanditsNPC.T("UI_BN_Hair_darkbrown", "Dark brown") end
    if key == "brown"     then return BanditsNPC.T("UI_BN_Hair_brown", "Brown") end
    if key == "auburn"    then return BanditsNPC.T("UI_BN_Hair_auburn", "Auburn") end
    if key == "ginger"    then return BanditsNPC.T("UI_BN_Hair_ginger", "Ginger") end
    if key == "blonde"    then return BanditsNPC.T("UI_BN_Hair_blonde", "Blonde") end
    if key == "platinum"  then return BanditsNPC.T("UI_BN_Hair_platinum", "Platinum") end
    if key == "grey"      then return BanditsNPC.T("UI_BN_Hair_grey", "Grey") end
    if key == "white"     then return BanditsNPC.T("UI_BN_Hair_white", "White") end
    if key == "red"       then return BanditsNPC.T("UI_BN_Hair_red", "Red") end
    if key == "pink"      then return BanditsNPC.T("UI_BN_Hair_pink", "Pink") end
    if key == "purple"    then return BanditsNPC.T("UI_BN_Hair_purple", "Purple") end
    if key == "blue"      then return BanditsNPC.T("UI_BN_Hair_blue", "Blue") end
    if key == "teal"      then return BanditsNPC.T("UI_BN_Hair_teal", "Teal") end
    if key == "green"     then return BanditsNPC.T("UI_BN_Hair_green", "Green") end
    return BanditsNPC.T("UI_BN_Hair_black", "Black")
end

-- ---------------------------------------------------------------------------
-- the object
-- ---------------------------------------------------------------------------

-- THE SPRITE ALONE IS NOT ENOUGH, and that shipped as a real bug: furniture_storage_01_40..43
-- is a STOCK VANILLA DRESSER. Every dresser of that type already standing in the world was
-- being treated as an Appearance Workstation -- "there is a dresser in the house that has the
-- E to interact notice and opens the interface even though that is not the one I placed"
-- (author, 6 Aug). The `tcAppearance` ModData fallback was never going to save it either:
-- nothing in the mod ever set that flag, so it was dead code and the sprite was the only test.
--
-- v0.75.58 TRIED getScriptName() AND BROKE THE FEATURE OUTRIGHT: the real station stopped
-- offering "E to interact" at all (author, 6 Aug). That call is unproven on IsoObject -- vanilla
-- Lua only ever uses it on vehicles -- and it evidently does not answer with the entity name
-- here. Worse, the result was CACHED IN MODDATA INCLUDING THE `false`, so one bad evaluation
-- put the station permanently beyond recognition. Both mistakes are undone below: the test is
-- something vanilla itself relies on, and a negative is never written down.
--
-- WHAT ACTUALLY SEPARATES THE TWO OBJECTS: ours was BUILT, and everything the entity build
-- system places is an IsoThumpable --
--   ISBuildIsoEntity:setInfo -> IsoThumpable.new(getCell(), square, sprite, north, self)
--   (ISBuildIsoEntity.lua:603-605, the only branch our non-prop entity can take)
-- while a dresser the map generator put in a house is a plain IsoObject. `instanceof(o,
-- "IsoThumpable")` is vanilla's own idiom for exactly this question (ISPaintAction.lua:103,
-- ISWallpaperAction.lua:37, buildRecipeCode.lua:27).
--
-- This needs no migration: the station the author already built went through that same
-- constructor, so it is recognised again the moment this loads.
local function isBuilt(obj)
    local built = false
    pcall(function() built = instanceof(obj, "IsoThumpable") end)
    return built and true or false
end

function A.IsStation(obj)
    if not obj then return false end

    -- POSITIVE ONLY. A stored `true` is something we concluded and wrote ourselves; there is
    -- deliberately no stored `false`, because a wrong negative persists in the save and there
    -- is then no way back short of rebuilding the furniture.
    local flagged
    pcall(function()
        local md = obj:getModData()
        flagged = md and md.tcAppearance
    end)
    if flagged == true then return true end

    local name
    pcall(function()
        local spr = obj:getSprite()
        name = spr and spr:getName()
    end)
    if not (name and SPRITE_SET[name]) then return false end   -- wrong sprite: never ours
    if not isBuilt(obj) then return false end                  -- map furniture: never ours

    -- Remember the answer so the scan does not repeat per frame for the prompt.
    pcall(function()
        local md = obj:getModData()
        if md then md.tcAppearance = true end
    end)
    return true
end

function A.OnSquare(sq)
    if not sq then return nil end
    local found
    pcall(function()
        local objs = sq:getObjects()
        for i = 0, objs:size() - 1 do
            local o = objs:get(i)
            if A.IsStation(o) then found = o; return end
        end
    end)
    return found
end

-- ---------------------------------------------------------------------------
-- hair and beard catalogues
-- ---------------------------------------------------------------------------

-- All pickable hair style ids for a sex, in engine order with "" (bald) first.
--
-- Cached per sex because getAllHairStyles walks the registry and this is read by a
-- UI that cycles on every click. Cleared on game start so a newly installed hair mod
-- is picked up rather than served from a stale list.
local hairCache = {}

function A.HairStyles(female)
    local key = female and "f" or "m"
    if hairCache[key] then return hairCache[key] end
    local out = {}
    pcall(function()
        local styles = getAllHairStyles(female)
        local inst = getHairStylesInstance()
        for i = 0, styles:size() - 1 do
            local id = styles:get(i)
            local style = female and inst:FindFemaleStyle(id) or inst:FindMaleStyle(id)
            -- isNoChoose marks internal entries the character creator also hides.
            local pickable = true
            pcall(function() pickable = not (style and style:isNoChoose()) end)
            if pickable then out[#out + 1] = id end
        end
    end)
    -- DON'T CACHE A FAILED BUILD (v0.75.26). An empty result was replaced with { "" } and
    -- then cached, so one transient failure of getAllHairStyles left the appearance
    -- station permanently offering a single blank hairstyle with no way to retry. Milder
    -- than the other two sites -- it is a cosmetic list, not a lookup everything depends
    -- on -- but the shape of the bug is identical, so the fix is too.
    if #out == 0 then return { "" } end
    hairCache[key] = out
    return out
end

function A.BeardStyles()
    if hairCache.beard then return hairCache.beard end
    local out = {}
    pcall(function()
        local styles = getAllBeardStyles()
        for i = 0, styles:size() - 1 do out[#out + 1] = styles:get(i) end
    end)
    if #out == 0 then out = { "" } end
    hairCache.beard = out
    return out
end

-- Human-readable label for a style id.
--
-- getText returns the KEY ITSELF when a translation is missing, which is what happens
-- for most modded hair -- so a raw "IGUI_Hair_Shaggy2" would be shown to the player.
-- Detect that and fall back to the bare id, which is at least the name the hair mod's
-- author chose.
local function styleLabel(prefix, id)
    if id == nil or id == "" then
        return (prefix == "IGUI_Beard_") and BanditsNPC.T("UI_BN_Beard_None", "Clean shaven")
                                          or BanditsNPC.T("UI_BN_Hair_Bald", "Bald")
    end
    local key = prefix .. id
    local txt
    pcall(function() txt = getText(key) end)
    if txt and txt ~= "" and txt ~= key then return txt end
    return id
end

function A.HairLabel(id) return styleLabel("IGUI_Hair_", id) end
function A.BeardLabel(id) return styleLabel("IGUI_Beard_", id) end

-- ---------------------------------------------------------------------------
-- applying
-- ---------------------------------------------------------------------------

-- Anything Bandits or we might have stored, reduced to { r, g, b } or nil.
--
-- Both conventions are accepted because both exist in a live save (see the header):
-- a NUMBER is Bandits' palette index and goes through their own Bandit.GetHairColor,
-- a TABLE is already a colour. Anything else -- including a Java colour object, whose
-- r/g/b are private and read back as nil -- is refused.
--
-- The three-component check at the end is not belt and braces. ImmutableColor.new is a
-- Java CONSTRUCTOR: a nil where it wants a float throws out of LuaJavaInvoker, and that
-- is a dispatch failure, which PROPAGATES THROUGH pcall. It cannot be caught after the
-- fact, so it has to be made impossible before the call.
function A.NormColor(c)
    if c == nil then return nil end
    if type(c) == "number" then
        local t
        pcall(function() t = Bandit and Bandit.GetHairColor and Bandit.GetHairColor(c) end)
        c = t
    end
    if type(c) ~= "table" then return nil end
    if type(c.r) ~= "number" or type(c.g) ~= "number" or type(c.b) ~= "number" then
        return nil
    end
    return c
end

-- The colour to show and apply: ours if the player has chosen one, otherwise whatever
-- Bandits gave her, in whichever form it is stored.
function A.ColorOf(brain)
    if not brain then return nil end
    return A.NormColor(brain.tcHairColor) or A.NormColor(brain.hairColor)
end

-- SILENT MIGRATION for saves written by v0.74.0-v0.74.6, which put our {r,g,b} table
-- into Bandits' index field.
--
-- IT MUST RUN BEFORE ANYTHING CALLS Bandit.ApplyVisuals. A table left in hairColor is
-- not merely ignored by the clan path -- it kills it: GetHairColor(<table>) indexes its
-- palette with a table, gets nil, and then reads .r off nil. Doing this inside A.Apply
-- alone (v0.75.0) was too late, because the restore path calls ApplyVisuals first; see
-- the note at the call site in BanditsNPCPersistence.applyRestore.
--
-- Idempotent and cheap, so it is safe to call on any path that touches a brain.
function A.Migrate(brain)
    if not brain then return brain end
    if type(brain.hairColor) == "table" then
        brain.tcHairColor = brain.tcHairColor or brain.hairColor
        brain.hairColor = nil
    end
    if type(brain.beardColor) == "table" then
        brain.tcBeardColor = brain.tcBeardColor or brain.beardColor
        brain.beardColor = nil
    end
    return brain
end

-- Push whatever the brain says onto the live body. Called when the player changes
-- something and again from ApplyClothingVisuals after every restore.
function A.Apply(zombie, brain)
    if not zombie then return end
    brain = brain or (BanditBrain and BanditBrain.Get(zombie))
    if not brain then return end
    A.Migrate(brain)

    -- ==================================================================
    -- STAND DOWN BANDITS' OWN HAIR OVERRIDE, OR IT WINS EVERY TIME
    -- ==================================================================
    --
    -- "Hairstyles don't save when changed and reset after a while" (F242, 13 Aug), and the
    -- header of this file had the reason backwards. It says the styles are stored "under
    -- the names BANDITS ITSELF USES -- brain.hairStyle and brain.beardStyle". They are not:
    -- Bandit.ApplyVisuals reads brain.hairTYPE and brain.beardTYPE
    --
    --     Bandit.lua:158-159   if brain.hairType then
    --                              banditVisuals:setHairModel(Bandit.GetHairStyle(brain.female, brain.hairType))
    --     Bandit.lua:162-163   if not bandit:isFemale() and brain.beardType then ...
    --
    -- and the spawner sets both to 1 on every companion (BanditServerSpawner.lua:326,328).
    -- So ApplyVisuals repainted the ORIGINAL hair over ours on the next pass, and our
    -- setHairModel below only ever held until then. The player's choice was never saved
    -- wrong -- it was overwritten, which looks identical and is why it "resets after a
    -- while" rather than immediately.
    --
    -- FALSE, NOT nil. Both engine reads are plain truthiness tests, so false disables them
    -- exactly as nil would -- but nil is an ABSENT KEY, which neither survives a table copy
    -- through ForceSyncPart nor clears a value that is already set on the other side.
    --
    -- Safe to clear: these two fields are read in exactly those two places in the whole of
    -- Bandits (grep-verified against 42.20) and written only as spawn/editor defaults.
    -- Nothing computes with them, so a false can never reach GetHairStyle as an index.
    --
    -- Done HERE rather than in A.Set so it also covers a RESTORE, where Bandits builds a
    -- brand new body and its spawner puts hairType back to a number.
    if brain.hairStyle ~= nil then brain.hairType = false end
    if brain.beardStyle ~= nil then brain.beardType = false end

    local c = A.ColorOf(brain)
    pcall(function()
        local v = zombie:getHumanVisual()
        if not v then return end
        if brain.hairStyle ~= nil then v:setHairModel(brain.hairStyle) end
        if brain.beardStyle ~= nil and not brain.female then v:setBeardModel(brain.beardStyle) end
        if c then
            local ic = ImmutableColor.new(c.r, c.g, c.b, 1)
            v:setHairColor(ic)
            v:setBeardColor(ic)
        end
    end)
    -- The model is rebuilt from the visual; without this the change does not appear
    -- until the zombie happens to be re-created.
    pcall(function() zombie:resetModelNextFrame() end)
end

-- ===========================================================================
-- FIX(BUG-053): A RENAME MUST REACH THE PROSE THAT ALREADY HAS HER NAME IN IT.
-- ===========================================================================
--
-- Reported as "I renamed her, then asked about herself and the dialogue used her old
-- name". `Backstory.Generate` substitutes `{name}` -> `brain.fullname` AT GENERATION TIME
-- (BanditsNPCBackstory.lua:136) and the result is cached in `brain.storyParts`, which
-- `Backstory.Ensure` refuses to rebuild once it exists (`if brain.storyParts then
-- return end`) and which IS in Persistence's CUSTOM_FIELDS. **So the old name was frozen
-- into the sentence and no amount of renaming could ever reach it.**
--
-- SUBSTITUTION, NOT REGENERATION, AND THE REASON IS NOT STYLE. Regenerating would
-- re-render a backstory the player may be attached to and would re-roll the personality
-- and trait lines with it. This rewrites the one sentence in place and leaves the
-- occupation, the hobby, the personality line and the trait line untouched.
--
-- **THE OLD NAME IS KNOWN EXACTLY AND IS NEVER GUESSED AT.** It is `brain.fullname`
-- read immediately before the field loop overwrites it, so this needs no pattern
-- matching against the templates, works in every translation, and works on existing
-- saves with no migration pass.
--
-- WHOLE-WORD ONLY, AND THAT MATTERS. A bare gsub of a companion renamed "Al" would hit
-- "Also" and "alone" in her own backstory. The string is padded and the match requires a
-- non-letter on each side, which is exactly how `{name}` sits in every template -- start
-- of sentence or after a space, always before a space. A possessive ("Al's") still
-- matches, which is wanted.
--
-- ONE OCCURRENCE PER SENTENCE IS ASSUMED, and it holds because every template carries
-- exactly one `{name}`. gsub consumes the trailing delimiter, so two occurrences
-- separated by a SINGLE space would leave the second unmatched. **If you ever put a
-- second `{name}` in one template, run the gsub twice or this quietly half-renames.**
--
-- SWEPT THIS SESSION FOR THE SAME SHAPE, SO NOBODY HAS TO REPEAT IT:
--   * `brain.quest` (desc/success, cached at BanditsNPCQuests.lua:185) -- CLEAN. No quest
--     template contains `{name}`; they are all first-person. `{count}`/`{item}` only.
--   * `brain.occupationName` -- CLEAN, a job title with no name in it.
--   * `BanditsNPCTalk.lua` -- CLEAN, the dialogue lines never interpolate a name.
--   * roster, profile panel, dialog rail, tooltips -- CLEAN, all 20 sites read
--     `brain.fullname` live and cache nothing.
--   * `o.title` on the dialog and inventory windows -- baked when the window OPENS, so a
--     rename shows on the next open. Cosmetic and self-healing; deliberately not touched.
--   * `BanditCompatibility.AddId` ID card / key ring -- a real stale name, but it is an
--     ITEM IN THE WORLD, not text on the brain, and an ID card bearing her original name
--     is defensible. Logged in BUGS.md, NOT fixed here.
-- `storyParts` was the only name-bearing cached field on the brain.
local function renameInStory(brain, oldName, newName)
    if type(oldName) ~= "string" or oldName == "" then return false end
    if type(newName) ~= "string" or newName == "" then return false end
    if oldName == newName then return false end
    local parts = brain.storyParts
    if type(parts) ~= "table" then return false end
    -- `%` in the replacement and any magic character in the pattern must be escaped, or
    -- a companion named "Bob (Doc)" turns the gsub into a malformed pattern and the
    -- whole rename errors.
    local pat = "([^%a])" .. oldName:gsub("(%W)", "%%%1") .. "([^%a])"
    local rep = "%1" .. newName:gsub("%%", "%%%%") .. "%2"
    local hit = false
    for i = 1, #parts do
        if type(parts[i]) == "string" then
            local out, n = (" " .. parts[i] .. " "):gsub(pat, rep)
            if n > 0 then parts[i] = out:sub(2, -2); hit = true end
        end
    end
    return hit
end

-- Store AND apply, in one call, so the two can never be done separately by mistake.
--
-- OWNER ONLY (audit Cluster A). This renames and restyles a companion permanently; the
-- appearance station happily opened on anyone standing near it. A.Apply below is NOT
-- gated -- it only repaints what the brain already says, and every client needs to be
-- able to do that for every companion it can see.
function A.Set(zombie, fields)
    if not (BanditsNPC and BanditsNPC.MayCommand and BanditsNPC.MayCommand(zombie)) then return false end
    local brain = BanditBrain and BanditBrain.Get(zombie)
    if not brain then return false end
    -- FIX(BUG-053): READ BEFORE THE LOOP OVERWRITES IT. This is the only place the old
    -- name still exists, and one line later it is gone for good.
    local oldName = brain.fullname
    for k, v in pairs(fields) do brain[k] = v end
    local storyChanged = fields.fullname ~= nil
                         and renameInStory(brain, oldName, brain.fullname)
    BanditBrain.Update(zombie, brain)
    A.Apply(zombie, brain)
    if Bandit and Bandit.ForceSyncPart then
        pcall(function()
            -- Our own colour fields only. Syncing brain.hairColor would push whatever
            -- shape we happened to hold back into Bandits' index field.
            -- hairType/beardType travel too: A.Apply sets them false to stand down
            -- Bandits' override, and without syncing that, every OTHER client keeps
            -- repainting her original hair from its own copy of the brain.
            local payload = {
                id = brain.id, fullname = brain.fullname,
                hairStyle = brain.hairStyle, beardStyle = brain.beardStyle,
                hairType = brain.hairType, beardType = brain.beardType,
                tcHairColor = brain.tcHairColor, tcBeardColor = brain.tcBeardColor,
            }
            -- FIX(BUG-053): only when the prose actually changed. A.Set is also the hair
            -- and colour path, and pushing the whole story on every swatch click would
            -- put a list of sentences on the wire for nothing. Same field and the same
            -- call Backstory.Ensure already syncs it with (BanditsNPCBackstory.lua:245).
            if storyChanged then payload.storyParts = brain.storyParts end
            Bandit.ForceSyncPart(zombie, payload)
        end)
    end
    if BanditsNPC.Persistence and BanditsNPC.Persistence.Record then
        pcall(function() BanditsNPC.Persistence.Record(zombie, brain) end)
    end
    return true
end

-- ---------------------------------------------------------------------------
-- context menu
-- ---------------------------------------------------------------------------
--
-- The build entry and its materials test used to live here. Both are gone with the entry
-- itself (v0.77.13) -- the Build menu path has never needed either, because ISBuildPanel
-- sets blockBuild from the recipe. The long version of that is in BanditsNPCBeacon.lua.

function A.OnFillWorldObjectContextMenu(player, context, worldobjects, test)
    if test and ISWorldObjectContextMenu.Test then return true end
    local playerObj = getSpecificPlayer(player)
    if not playerObj or playerObj:getVehicle() then return end

    local station
    for _, o in ipairs(worldobjects) do
        if A.IsStation(o) then station = o; break end
        local sq = o:getSquare()
        if sq then
            local s = A.OnSquare(sq)
            if s then station = s; break end
        end
    end

    -- ONLY THE "OPEN IT" HALF SURVIVES (v0.77.13). The "Build an Appearance Workstation"
    -- entry is gone for the same reason as the beacon's: it is a once-per-save action that
    -- was living in the ground context menu forever, and three reporters asked. The recipe
    -- is in the Build menu under Furniture, and that path arms the cursor through
    -- ISBuildPanel, which sets blockBuild from the recipe (the right-click path did not --
    -- see the long note in BanditsNPCBeacon.lua).
    if not station then return end
    if test then return ISWorldObjectContextMenu.setTest() end
    context:addOption(BanditsNPC.T("UI_BN_App_Open", "Appearance Workstation..."), station,
        function(s) BanditsNPCAppearanceWindow.OpenFor(s) end)
end

Events.OnFillWorldObjectContextMenu.Add(A.OnFillWorldObjectContextMenu)

-- A newly enabled hair mod registers its styles at load; drop the cache so the first
-- session after installing one is not served the old list.
Events.OnGameStart.Add(function() hairCache = {} end)
