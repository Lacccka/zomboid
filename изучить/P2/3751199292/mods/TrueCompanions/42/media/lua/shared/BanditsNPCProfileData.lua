--
-- True Companions - the numbers the profile panels read (v0.77.0)
--
-- ===========================================================================
-- THE RULE THIS FILE ENFORCES
-- ===========================================================================
--
-- The UI rework's mock-ups show a lot of information the mod did not compute, and the
-- standing instruction is that a panel never invents a number. So everything a panel wants
-- to say about a companion is answered HERE, once, and each answer is either real or
-- honestly absent -- never a plausible-looking placeholder wired into the layout.
--
-- WHAT IS REAL, and where it comes from:
--   age             rolled once at recruit and stored (no engine field gives a bandit one)
--   kills           tallied from OnZombieDead via getAttackedBy (BanditsNPCPersistence)
--   met day         world-age hour captured at recruit
--   days together   the same hour, subtracted from now
--   protection      summed off her WORN clothing: getBiteDefense / getScratchDefense /
--                   getInsulation are real methods on Clothing, and weight is the items'
--   relationship    the existing affinity value and its five-tier ladder
--
-- WHAT IS HONESTLY ABSENT, and why -- these return nil and the panels render a TODO rather
-- than a number:
--   perk levels     a companion is an IsoZombie; getPerkLevel exists but nothing ever sets
--                   a perk on one, so every skill would read 0. A real answer needs a
--                   progression system, which is a feature and not a label.
--   traits          the mock lists "Wary - bond gains halved below 5" and similar. The
--                   backstory generator has trait FLAVOUR lines but no trait objects with
--                   mechanical effects, so there is nothing to enumerate.
--   place name      "Met - Rosewood" needs a coordinate-to-town lookup. IsoMetaGrid has no
--                   getRegionName and no vanilla Lua does this, so the panel shows the day.
--

BanditsNPC = BanditsNPC or {}
BanditsNPC.ProfileData = BanditsNPC.ProfileData or {}
local D = BanditsNPC.ProfileData

local function hoursNow()
    local h
    pcall(function() h = getGameTime():getWorldAgeHours() end)
    return h or 0
end

function D.Age(brain)
    return brain and brain.age or nil
end

function D.Kills(brain)
    return (brain and brain.kills) or 0
end

-- Day number she was recruited, counting from world day 1 -- the same clock the player's
-- own "Day 12" comes from, so the two agree.
function D.MetDay(brain)
    if not (brain and brain.metHour) then return nil end
    return math.floor(brain.metHour / 24) + 1
end

function D.DaysTogether(brain)
    if not (brain and brain.metHour) then return nil end
    return math.max(0, math.floor((hoursNow() - brain.metHour) / 24))
end

-- ---------------------------------------------------------------------------
-- protection, summed off what she is actually wearing
-- ---------------------------------------------------------------------------
--
-- Bite and scratch defence are per-garment PERCENTAGES covering the body parts that
-- garment protects, so they cannot be meaningfully added into one number -- two 30% items
-- on different limbs are not 60% protection anywhere. The honest summary is the BEST cover
-- any single garment provides, which is what the player is really asking ("am I sending
-- her out in a t-shirt?"), and it never exceeds 100.
--
-- Insulation is reported as a coarse word rather than a float: the raw value is a 0..1
-- scale nobody can interpret, and "Low / Fair / Warm" is what it means.
-- READS brain.clothing, NOT THE INVENTORY, and that is the whole correction (v0.77.7).
--
-- A companion is a hijacked IsoZombie and Bandits does not dress it with real items: it
-- clears the worn items and PAINTS the outfit on, keeping only the item TYPE STRINGS in
-- brain.clothing (bodyLocation -> "Base.Shirt_Denim"). Walking her inventory therefore finds
-- no clothing at all, which is why the panel said "wearing nothing" over a fully dressed
-- companion whose outfit doll was showing every layer.
--
-- The types are instantiated through the same BanditCompatibility.InstanceItem the icons use
-- and thrown away; nothing is added to any container.
function D.Protection(zombie, brain)
    local out = { bite = 0, scratch = 0, insul = 0, weight = 0, any = false }
    brain = brain or (zombie and BanditBrain and BanditBrain.Get(zombie))
    if not brain then return out end

    local function measure(itype)
        if not itype then return end
        pcall(function()
            local it = BanditCompatibility.InstanceItem(itype)
            if not it then return end
            out.any = true
            if it.getBiteDefense then out.bite = math.max(out.bite, it:getBiteDefense() or 0) end
            if it.getScratchDefense then out.scratch = math.max(out.scratch, it:getScratchDefense() or 0) end
            if it.getInsulation then out.insul = math.max(out.insul, it:getInsulation() or 0) end
            local w
            if it.getUnequippedWeight then w = it:getUnequippedWeight()
            elseif it.getActualWeight then w = it:getActualWeight() end
            out.weight = out.weight + ((type(w) == "number") and w or 0)
        end)
    end

    for _, itype in pairs(brain.clothing or {}) do measure(itype) end
    -- The bag is worn too and lives on its own field, the same way the doll's Back slot
    -- has to look for it separately.
    if brain.bag and brain.bag.name then measure(brain.bag.name) end
    return out
end

function D.InsulWord(v)
    v = v or 0
    if v >= 0.66 then return BanditsNPC.T("UI_BN_Insul_Warm", "Warm") end
    if v >= 0.33 then return BanditsNPC.T("UI_BN_Insul_Fair", "Fair") end
    return BanditsNPC.T("UI_BN_Insul_Low", "Low")
end

-- ---------------------------------------------------------------------------
-- relationship
-- ---------------------------------------------------------------------------
--
-- The ladder shown on the Profile tab is the mod's OWN five tiers, not the mock-up's
-- (which read Wary / Neutral / Friendly / Loyal / Devoted). Renaming them in the UI alone
-- would make the panel disagree with every bark, milestone and tooltip that already uses
-- the real names, so the panel follows the code.
function D.Relationship(brain)
    local out = { value = 0, tier = 0, label = "", ladder = {}, max = 100 }
    if not brain then return out end
    pcall(function()
        out.value = BanditsNPC.Affinity.Get(brain) or 0
        out.tier  = BanditsNPC.Affinity.GetTier(brain) or 0
        out.label = BanditsNPC.Affinity.GetTierLabel(brain) or ""
        local rom = BanditsNPC.Affinity.IsRomance(brain)
        local names = rom and BanditsNPC.Affinity.TierLabelRom or BanditsNPC.Affinity.TierLabel
        for i = 0, 4 do out.ladder[i + 1] = names[i] end
    end)
    return out
end

-- ---------------------------------------------------------------------------
-- mood
-- ---------------------------------------------------------------------------
--
-- DERIVED, not invented, and that distinction is the whole reason it is allowed here. There
-- is no mood field on a companion and inventing a number for one would be exactly what this
-- file exists to prevent -- but "how is she doing" is answerable from figures the panel is
-- ALREADY SHOWING: the four needs in the Vitals bars, and the affinity below them. So this
-- names the worst pressing need, and falls back to the relationship tier when nothing is
-- pressing. It cannot disagree with the bars because it is reading them.
--
-- Read-only: it takes the needs table rather than calling Needs.Get, which CREATES the table
-- when it is missing, and nothing on a profile panel should be writing to the brain.
function D.Mood(brain)
    if not brain then return nil end
    local n = brain.needs
    if n then
        local worst, worstKey = 0, nil
        for _, k in ipairs({ "hunger", "fatigue", "boredom", "hygiene" }) do
            local v = n[k] or 0
            if v > worst then worst, worstKey = v, k end
        end
        if worst >= 75 then
            return ({ hunger  = BanditsNPC.T("UI_BN_Mood_Starving", "Starving"),
                      fatigue = BanditsNPC.T("UI_BN_Mood_Exhausted", "Exhausted"),
                      boredom = BanditsNPC.T("UI_BN_Mood_Restless", "Restless"),
                      hygiene = BanditsNPC.T("UI_BN_Mood_Filthy", "Filthy") })[worstKey]
        end
        if worst >= 45 then
            return ({ hunger  = BanditsNPC.T("UI_BN_Mood_Hungry", "Hungry"),
                      fatigue = BanditsNPC.T("UI_BN_Mood_Tired", "Tired"),
                      boredom = BanditsNPC.T("UI_BN_Mood_Bored", "Bored"),
                      hygiene = BanditsNPC.T("UI_BN_Mood_Grubby", "Grubby") })[worstKey]
        end
    end
    -- Nothing pressing: how she feels about YOU is the remaining answer.
    local tier = 0
    pcall(function() tier = BanditsNPC.Affinity.GetTier(brain) or 0 end)
    if tier >= 3 then return BanditsNPC.T("UI_BN_Mood_Happy", "Happy") end
    if tier >= 1 then return BanditsNPC.T("UI_BN_Mood_Content", "Content") end
    return BanditsNPC.T("UI_BN_Mood_Guarded", "Guarded")
end

-- What the NEXT tier actually changes, for the line under the ladder.
--
-- The mock-up's version of this line reads "At Friendly (5) she'll guard away from base and
-- share food unprompted" -- neither of which the mod does, at any tier. The one real,
-- verifiable effect of climbing the ladder is Affinity.NeedsFactor: a higher tier slows how
-- fast her needs rise. So that is what this reports, computed from the same table the
-- behaviour reads rather than described in prose that could drift away from it.
--
-- Returns nil at the top tier (there is no next), and slower = 0 when the next tier changes
-- nothing mechanically -- tiers 0 and 1 share a factor, and claiming an improvement there
-- would be a lie the player could measure.
function D.NextTier(brain)
    if not brain then return nil end
    local out
    pcall(function()
        local A = BanditsNPC.Affinity
        local t = A.GetTier(brain) or 0
        if t >= 4 then return end
        local rom = A.IsRomance(brain)
        local names = rom and A.TierLabelRom or A.TierLabel
        local curF = A.NeedsFactor(brain) or 1
        -- NeedsFactor takes a brain, not a tier, so ask it about a stand-in brain sitting at
        -- the next tier's threshold rather than duplicating its table here.
        local at = A.Tiers[t + 2] or 100
        local nextF = A.NeedsFactor({ affinity = at }) or curF
        local slower = 0
        if curF > 0 and nextF < curF then
            slower = math.floor(((curF - nextF) / curF) * 100 + 0.5)
        end
        out = { label = names[t + 1] or "?", at = at, slower = slower }
    end)
    return out
end

-- ---------------------------------------------------------------------------
-- the honest gaps
-- ---------------------------------------------------------------------------
--
-- Returned as nil with a reason so a panel can render one consistent TODO line instead of
-- each tab inventing its own way of saying "not built yet".
function D.PerkLevels()
    return nil, BanditsNPC.T("UI_BN_TODO_Perks",
        "Skill levels need a companion progression system - not built yet.")
end

function D.Traits()
    return nil, BanditsNPC.T("UI_BN_TODO_Traits",
        "Traits with effects are not implemented yet.")
end
