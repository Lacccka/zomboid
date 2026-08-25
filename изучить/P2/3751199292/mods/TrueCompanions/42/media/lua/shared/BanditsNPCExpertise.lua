--
-- True Companions - Professions
--
-- Adds new profession "expertises" on top of the Bandits engine list so our
-- companions can be tradespeople the base Bandits mod never modeled (Scientist,
-- Farmer, Builder, Tailor, Reloader, Blacksmith, Hunter), and assigns every
-- recruited companion a RANDOM profession from the full trade list.
--
-- We deliberately do NOT touch the Bandits Creator UI (its expertise dropdown is
-- a 3rd-party, versioned hook that proved fragile). Instead the profession is
-- rolled by us when a companion's brain becomes ours (on recruit). Ids are plain
-- tags appended to Bandit.Expertise; the engine's hostile-AI branches only match
-- its own ids, so ours are behaviorally inert -- perfect for peaceful trades.
-- Everything downstream (production stations, backstory, the Skills tab) already
-- reads brain.exp, so storing the rolled profession there is all that's needed.
--

BanditsNPC = BanditsNPC or {}

-- name -> id for the professions we add. Ids continue after the engine's last (16).
BanditsNPC.AddedExpertise = {
    Scientist  = 17,
    Farmer     = 18,
    Builder    = 19,
    Tailor     = 20,
    Reloader   = 21,
    Blacksmith = 22,
    Hunter     = 23,
}

-- The full trade pool a companion can be randomised into (by Bandit.Expertise name):
-- the 4 the engine already produces with, plus our 7 additions.
BanditsNPC.ProfessionNames = {
    "Cook", "Medic", "Mechanic", "Electrician",
    "Scientist", "Farmer", "Builder", "Tailor", "Reloader", "Blacksmith", "Hunter",
}

-- Append our ids to the engine's Bandit.Expertise table, only where absent, so a
-- future Bandits update that adds some of these natively won't be clobbered/duplicated.
function BanditsNPC.RegisterExpertise()
    if not (Bandit and Bandit.Expertise) then return false end
    for name, id in pairs(BanditsNPC.AddedExpertise) do
        if Bandit.Expertise[name] == nil then
            Bandit.Expertise[name] = id
        end
    end
    return true
end

-- Resolve ProfessionNames -> id list + lookup set (run after RegisterExpertise so our
-- ids exist). ProfessionSet lets us cheaply ask "does this brain already have a trade?".
function BanditsNPC.BuildProfessionPool()
    BanditsNPC.ProfessionPool = {}
    BanditsNPC.ProfessionSet = {}
    if not (Bandit and Bandit.Expertise) then return end
    for _, name in ipairs(BanditsNPC.ProfessionNames) do
        local id = Bandit.Expertise[name]
        if id then
            table.insert(BanditsNPC.ProfessionPool, id)
            BanditsNPC.ProfessionSet[id] = true
        end
    end
end

-- Give a brain a profession, ONCE. If it already carries one of our trade ids
-- (e.g. a clan Cook), we keep it; otherwise we roll a random trade into slot 1.
-- Marks brain.npcProfRolled so it never re-rolls. Returns the resulting trade id.
function BanditsNPC.AssignRandomProfession(brain)
    if not brain then return nil end
    if brain.npcProfRolled then return brain.exp and brain.exp[1] end
    if not BanditsNPC.ProfessionPool then BanditsNPC.BuildProfessionPool() end

    brain.exp = brain.exp or {0, 0, 0}

    local hasTrade = false
    for _, id in ipairs(brain.exp) do
        if BanditsNPC.ProfessionSet[id] then hasTrade = true break end
    end

    if not hasTrade and BanditsNPC.ProfessionPool and #BanditsNPC.ProfessionPool > 0 then
        brain.exp[1] = BanditsNPC.ProfessionPool[ZombRand(#BanditsNPC.ProfessionPool) + 1]
    end

    brain.npcProfRolled = true
    return brain.exp[1]
end

-- Our shared files load after the Bandits engine, so Bandit.Expertise already exists;
-- register + build now, and again at boot against load-order surprises.
local function setup()
    BanditsNPC.RegisterExpertise()
    BanditsNPC.BuildProfessionPool()
end
setup()
if Events and Events.OnGameBoot then
    Events.OnGameBoot.Add(setup)
end
