--
-- Bandits NPC - Companions Overhaul - Backstory generator (Phase 2)
--
-- Generates a "what were you doing before the apocalypse" blurb for an NPC,
-- derived from the skills/expertise it already has plus its personality and
-- physical stats. Never overwrites a Bandit Maker author's expertise.
--
-- If an NPC has more than one expertise, ONE is chosen as the real profession
-- (drives the story) and the rest are mentioned as hobbies/side skills.
--
-- We only ever render "he"/"she" (from brain.female), so all present-tense
-- verbs in templates are written in third-person singular.
--
-- Tokens: {name} {they} {their} {them} {they_cap} {their_cap}
--
-- TRANSLATION (per-gender keys). English does gendered text with pronoun tokens,
-- but that doesn't survive inflected languages (Polish past-tense verbs, gendered
-- adjectives...). So every template line has a stable `id`, and fill() resolves a
-- GENDERED key UI_BN_Bs_<id>_m / _f -- the translator writes each gender's full
-- sentence (only {name} stays a token). If the key is untranslated, fill() falls
-- back to the English token template and substitutes pronouns as before, so the
-- English output is unchanged. Titles (UI_BN_Bs_<id>_title) and hobby phrases
-- (UI_BN_Bs_hobby_<slug>) are genderless single keys.
--

BanditsNPC = BanditsNPC or {}
BanditsNPC.Backstory = {}

local E = Bandit and Bandit.Expertise or {}

-- Profession pools keyed by expertise id. Each entry: {title=, intro=}.
local OCC = {}
-- Registered one at a time and nil-keys skipped (v0.75.7, audit R10). As a literal
-- table this was `[E.Medic] = {...}`, and with Bandits absent E is {} -- so every key
-- was nil and the constructor raised "table index is nil", a hard load error. That
-- crash also pre-empted CheckDependencies, which runs on OnGameBoot (after all files
-- load) and exists to print a friendly "requires the Bandits mod" line for precisely
-- this case. Skipping nil keys lets the file load so that message can be seen.
local function addOcc(id, list)
    if id ~= nil then OCC[id] = list end
end
addOcc(E.Medic, {
        {id="medic_nurse", title = "nurse", intro = "Before the outbreak {name} pulled long shifts as an ER nurse. Patching people up is second nature now."},
        {id="medic_paramedic", title = "paramedic", intro = "{name} rode an ambulance for years as a paramedic, so blood and broken bones don't faze {them}."},
        {id="medic_student", title = "med student", intro = "{name} was a med student when it all fell apart, halfway to a white coat that never came."},
})
addOcc(E.Mechanic, {
        {id="mech_auto", title = "auto mechanic", intro = "{name} spent years under the hood at a roadside garage. Engines and generators are an open book."},
        {id="mech_machinist", title = "machinist", intro = "{name} worked a machine shop floor, and can still coax life out of dead machinery."},
})
addOcc(E.Electrician, {
        {id="elec_electrician", title = "electrician", intro = "{name} wired houses for a living, and now eyes every dead fuse box like an old friend."},
        {id="elec_lineman", title = "power lineman", intro = "{name} climbed poles as a lineman through every kind of weather before the lights went out for good."},
})
addOcc(E.Cook, {
        {id="cook_line", title = "line cook", intro = "{name} ran the line at a busy diner and can make a meal out of almost nothing."},
        {id="cook_caterer", title = "caterer", intro = "{name} cooked for weddings and wakes alike, and still takes pride in a hot meal."},
})
addOcc(E.Recon, {
        {id="recon_ranger", title = "park ranger", intro = "{name} worked the backcountry as a park ranger. Reading the land and moving quiet comes easy."},
        {id="recon_scout", title = "scout", intro = "{name} grew up scouting and hiking, and the wilderness never scared {them} the way the towns do now."},
})
addOcc(E.Tracker, {
        {id="track_hunter", title = "hunter", intro = "{name} hunted deer every season. Following a trail is just muscle memory at this point."},
        {id="track_guide", title = "wilderness guide", intro = "{name} guided hikers through the hills for a living and knows the land like the back of {their} hand."},
})
addOcc(E.Trapper, {
        {id="trap_trapper", title = "trapper", intro = "{name} ran a trapline out in the sticks long before any of this, living off what the woods gave up."},
        {id="trap_gamekeeper", title = "gamekeeper", intro = "{name} kept game on a private reserve, equal parts woodsman and warden."},
})
addOcc(E.Repairman, {
        {id="repair_handyman", title = "handyman", intro = "{name} was the handyman everyone called. If it was broken, {they} fixed it."},
        {id="repair_super", title = "building super", intro = "{name} kept an apartment block running as the super, so nothing falling apart is a surprise."},
})
addOcc(E.Breaker, {
        {id="breaker_demo", title = "demolition worker", intro = "{name} took buildings apart for a living. Doors and barricades are barely an inconvenience."},
        {id="breaker_locksmith", title = "locksmith", intro = "{name} worked as a locksmith, and few things stay shut for long once {they} sets {their} mind to it."},
})
addOcc(E.Thief, {
        {id="thief_burglar", title = "burglar", intro = "{name} made a quiet living taking what wasn't {their} own. Light fingers, lighter conscience."},
        {id="thief_pickpocket", title = "pickpocket", intro = "{name} worked crowds and pockets in the city, never staying anywhere too long."},
})
addOcc(E.Assasin, {
        {id="ex_soldier", title = "ex-soldier", intro = "{name} did a couple of tours before coming home. The training never really left."},
        {id="security", title = "security contractor", intro = "{name} worked private security, the kind that doesn't end up in the brochure."},
})
addOcc(E.Goblin, {
        {id="scrapper", title = "scrapper", intro = "{name} scraped a living out of junkyards and scrap heaps, and can find use in anything."},
})

-- Short side-skill phrases per expertise, used when an NPC has more than one.
-- {slug=, text=}; the slug names the translation key UI_BN_Bs_hobby_<slug>
-- (genderless -- the phrase carries no verb subject).
local HOBBY = {
    [E.Medic]       = {slug="medic",      text="patching up cuts and scrapes"},
    [E.Mechanic]    = {slug="mechanic",   text="tinkering with engines"},
    [E.Electrician] = {slug="electrician",text="fiddling with wiring"},
    [E.Cook]        = {slug="cook",       text="cooking"},
    [E.Recon]       = {slug="recon",      text="long hikes off the trail"},
    [E.Tracker]     = {slug="tracker",    text="tracking game"},
    [E.Trapper]     = {slug="trapper",    text="setting snares"},
    [E.Repairman]   = {slug="repairman",  text="fixing whatever broke around the house"},
    [E.Breaker]     = {slug="breaker",    text="taking broken things apart"},
    [E.Thief]       = {slug="thief",      text="sleight-of-hand card tricks"},
    [E.Assasin]     = {slug="assasin",    text="keeping in fighting shape"},
    [E.Goblin]      = {slug="goblin",     text="scrounging through scrap"},
}

local CIVILIAN = {
    {id="civ_office", title = "office worker", intro = "{name} pushed paper in an office downtown, a life that feels like someone else's now."},
    {id="civ_teacher", title = "schoolteacher", intro = "{name} taught school before the outbreak and still has a teacher's stubborn patience."},
    {id="civ_trucker", title = "truck driver", intro = "{name} drove long-haul routes for years, so being far from home is nothing new."},
    {id="civ_student", title = "student", intro = "{name} was just a student when the world ended, growing up fast ever since."},
    {id="civ_clerk", title = "retail clerk", intro = "{name} worked a register at a big-box store, watching the panic-buying start before anyone admitted what it was."},
    {id="civ_farmhand", title = "farmhand", intro = "{name} worked the land on a family farm and knows hard, honest work."},
}

local function pick(list, seed)
    if not list or #list == 0 then return nil end
    return list[(math.abs(seed) % #list) + 1]
end

-- tpl = English token template; id (optional) = gendered translation key stem.
-- When id is given and a translation exists for the brain's gender, that string
-- is used (already grammatically correct, only {name} needs substituting);
-- otherwise the English template is pronoun-substituted as before.
local function fill(tpl, brain, id)
    local female = brain.female
    if id then
        tpl = BanditsNPC.T("UI_BN_Bs_" .. id .. "_" .. (female and "f" or "m"), tpl)
    end
    local they  = female and "she" or "he"
    local their = female and "her" or "his"
    local them  = female and "her" or "him"
    local map = {
        name = brain.fullname or BanditsNPC.T("UI_BN_Bs_ThisSurvivor", "This survivor"),
        they = they, their = their, them = them,
        they_cap  = they:gsub("^%l", string.upper),
        their_cap = their:gsub("^%l", string.upper),
    }
    return (tpl:gsub("{([%w_]+)}", function(k) return map[k] or ("{" .. k .. "}") end))
end

-- personality/trait lines return {tpl, id} so fill() can resolve the gendered key
local function personalityLine(brain, seed)
    local p = brain.personality or {}
    local lines = {}
    if p.alcoholic then table.insert(lines, {"These days a bottle is never far from {their} reach.", "pers_alcoholic"}) end
    if p.smoker then table.insert(lines, {"{they_cap} still keeps a crumpled pack of cigarettes close.", "pers_smoker"}) end
    if p.compulsiveCleaner then table.insert(lines, {"{they_cap} can't abide a mess and keeps {their} gear spotless.", "pers_cleaner"}) end
    if p.fromPoland then table.insert(lines, {"{they_cap} grew up in Poland and curses fluently in two languages.", "pers_poland"}) end
    if p.comicsCollector then table.insert(lines, {"{they_cap} still picks up any comic book {they} finds, apocalypse or not.", "pers_comics"}) end
    if p.gameCollector then table.insert(lines, {"{they_cap} hoards old video games for a console that may never turn on again.", "pers_games"}) end
    if #lines == 0 then return nil end
    return pick(lines, seed)
end

local function traitLine(brain, seed)
    local lines = {}
    if (brain.strengthBoost or 1) > 1.2 then table.insert(lines, {"{they_cap} is stronger than {they} looks.", "trait_strong"}) end
    if (brain.enduranceBoost or 1) > 1.2 then table.insert(lines, {"{they_cap} can keep going long after others drop.", "trait_endure"}) end
    if (brain.accuracyBoost or 0) > 2 then table.insert(lines, {"{they_cap} has a sharp eye, especially at distance.", "trait_eye"}) end
    if (brain.health or 1) > 1.9 then table.insert(lines, {"{they_cap} is tough -- hard to put down.", "trait_tough"}) end
    if #lines == 0 then table.insert(lines, {"{they_cap} is just trying to make it through another day.", "trait_default"}) end
    return pick(lines, seed)
end

-- Returns {occupation=<title>, text=<paragraph>} for a brain. Deterministic.
function BanditsNPC.Backstory.Generate(brain)
    local seed = math.abs(brain.id or 0)
    if brain.rnd then
        for _, v in ipairs(brain.rnd) do seed = seed + (v or 0) end
    end

    -- collect all real expertise; one becomes the profession, others hobbies
    local expIds = {}
    if brain.exp then
        for _, id in ipairs(brain.exp) do
            if id and id > 0 then table.insert(expIds, id) end
        end
    end

    local primExp, hobbyExp
    if #expIds >= 1 then
        local pidx = (seed % #expIds) + 1
        primExp = expIds[pidx]
        if #expIds >= 2 then
            local rest = {}
            for i, id in ipairs(expIds) do
                if i ~= pidx then table.insert(rest, id) end
            end
            hobbyExp = rest[((seed + 5) % #rest) + 1]
        end
    end

    local pool = (primExp and OCC[primExp]) or CIVILIAN
    local occ = pick(pool, seed) or pick(CIVILIAN, seed)

    local parts = {}
    table.insert(parts, fill(occ.intro, brain, occ.id and (occ.id .. "_intro")))

    if hobbyExp and HOBBY[hobbyExp] then
        local hb = HOBBY[hobbyExp]
        local hobbyText = BanditsNPC.T("UI_BN_Bs_hobby_" .. hb.slug, hb.text)
        -- gendered connector, %s = the (already translated) hobby phrase
        local female = brain.female
        -- The key is built at runtime (one line per gender), so this cannot be a
        -- literal TF call. BanditsNPC.Fill is TF's substitution half, applied to the
        -- string T already returned, so the placeholder rules stay identical to
        -- everywhere else: the line may use %1, %1$s or a bare %s and all three fill.
        local raw = BanditsNPC.T("UI_BN_Bs_hobby_line_" .. (female and "f" or "m"),
                                 "Before all this, {they} also had a knack for %1.")
        local line = BanditsNPC.Fill(raw, hobbyText)
        table.insert(parts, fill(line, brain))   -- fill resolves {they}/{name} for the English fallback
    end

    local pl = personalityLine(brain, seed + 7)
    if pl then table.insert(parts, fill(pl[1], brain, pl[2])) end

    local tl = traitLine(brain, seed + 13)
    if tl then table.insert(parts, fill(tl[1], brain, tl[2])) end

    -- occupation title (genderless single key)
    local title = occ.title
    if occ.id then title = BanditsNPC.T("UI_BN_Bs_" .. occ.id .. "_title", occ.title) end
    return { occupation = title, parts = parts }
end

-- Generates (once) the story PARTS, hidden until discovered via Talk.
-- brain.storyParts = {sentence, ...}, brain.storyRevealed = how many are known.
function BanditsNPC.Backstory.Ensure(zombie)
    local brain = BanditBrain.Get(zombie)
    if not brain then return end
    if brain.storyParts then return end

    local res = BanditsNPC.Backstory.Generate(brain)
    brain.storyParts = res.parts
    brain.occupationName = res.occupation
    brain.storyRevealed = 0
    BanditBrain.Update(zombie, brain)

    if Bandit and Bandit.ForceSyncPart then
        Bandit.ForceSyncPart(zombie, {
            id = brain.id,
            storyParts = brain.storyParts,
            occupationName = brain.occupationName,
            storyRevealed = 0,
        })
    end
end

-- Reveals the next story part (called from the Talk tab). Returns the newly
-- revealed sentence, or nil if everything is already known.
function BanditsNPC.Backstory.Reveal(zombie)
    local brain = BanditBrain.Get(zombie)
    if not brain or not brain.storyParts then return nil end
    local n = brain.storyRevealed or 0
    if n >= #brain.storyParts then return nil end
    n = n + 1
    brain.storyRevealed = n
    BanditBrain.Update(zombie, brain)
    if Bandit and Bandit.ForceSyncPart then
        Bandit.ForceSyncPart(zombie, { id = brain.id, storyRevealed = n })
    end
    return brain.storyParts[n]
end
