--
-- Bandits NPC - Companions Overhaul - Needs (hunger / fatigue / boredom)
--
-- Companion needs rise over (game) time. 0 = satisfied, 100 = critical. The
-- routine AI (later) acts on them using the assigned spots/containers. Decay is
-- delta-time based off the world clock, so it catches up after the NPC has been
-- unloaded. Persisted to the brain (throttled to ~once per game-hour).
--

BanditsNPC = BanditsNPC or {}
BanditsNPC.Needs = {}

-- rise per game-hour (tune later / sandbox option #19)
-- Points per in-game HOUR. Halved on 3 Aug after a report of companions emptying a
-- base's entire food storage.
--
-- The old hunger rate of 2.5 filled the bar in 40 hours, and a companion relaxing at
-- base goes to eat at 40 rather than waiting for critical -- so she ate roughly every
-- SIXTEEN in-game hours, or three times a day each. Multiply that by a full house and
-- the larder is gone in a week. At 1.2 she eats about once a day, which is what a
-- person does, and a base of five is a real but survivable food bill.
--
-- Fatigue is deliberately the least reduced: it drives the go-to-bed-at-night rhythm,
-- and slowing it too far means nobody ever sleeps.
BanditsNPC.Needs.Rate = { hunger = 1.2, fatigue = 1.6, boredom = 1.8, hygiene = 0.9 }

-- ordered list (drives UI + iteration)
BanditsNPC.Needs.List = { "hunger", "fatigue", "boredom", "hygiene" }

-- need >= this is "critical" (routine kicks in). Sandbox-overridable.
BanditsNPC.Needs.Critical = 70
function BanditsNPC.Needs.GetCritical()
    return BanditsNPC.Opt and BanditsNPC.Opt("NeedsCritical", BanditsNPC.Needs.Critical) or BanditsNPC.Needs.Critical
end

function BanditsNPC.Needs.Get(brain)
    if not brain.needs then brain.needs = { hunger = 0, fatigue = 0, boredom = 0, hygiene = 0 } end
    if brain.needs.hygiene == nil then brain.needs.hygiene = 0 end   -- migrate older saves
    return brain.needs
end

-- Advance the needs by the time elapsed since the last update.
function BanditsNPC.Needs.Update(zombie)
    local brain = BanditBrain.Get(zombie)
    if not brain or not brain.master then return end   -- only companions

    local n = BanditsNPC.Needs.Get(brain)
    local now = getGameTime():getWorldAgeHours()
    local last = brain.needsHour or now
    local dt = now - last
    if dt <= 0 then brain.needsHour = now; return end
    brain.needsHour = now

    local R = BanditsNPC.Needs.Rate
    local m = (BanditsNPC.Opt and BanditsNPC.Opt("NeedsRateMult", 1.0)) or 1.0
    -- affinity perk: a happier companion's needs rise more slowly
    if BanditsNPC.Affinity and BanditsNPC.Affinity.NeedsFactor then
        m = m * BanditsNPC.Affinity.NeedsFactor(brain)
    end
    local fatigueM = m

    n.hunger  = math.min(100, (n.hunger or 0)  + R.hunger  * dt * m)
    n.fatigue = math.min(100, (n.fatigue or 0) + R.fatigue * dt * fatigueM)
    n.boredom = math.min(100, (n.boredom or 0) + R.boredom * dt * m)
    n.hygiene = math.min(100, (n.hygiene or 0) + R.hygiene * dt * m)

    -- persist at most once per crossed game-hour
    if math.floor(now) > math.floor(last) and Bandit and Bandit.ForceSyncPart then
        Bandit.ForceSyncPart(zombie, { id = brain.id, needs = n, needsHour = now })
    end
end

function BanditsNPC.Needs.Reduce(zombie, key, amount, noSync)
    local brain = BanditBrain.Get(zombie)
    if not brain then return end
    local n = BanditsNPC.Needs.Get(brain)
    n[key] = math.max(0, (n[key] or 0) - (amount or 100))
    BanditBrain.Update(zombie, brain)
    if not noSync and Bandit and Bandit.ForceSyncPart then
        Bandit.ForceSyncPart(zombie, { id = brain.id, needs = n })
    end
end

-- Raise a need (debug / testing). Clamped 0..100.
function BanditsNPC.Needs.Add(zombie, key, amount)
    local brain = BanditBrain.Get(zombie)
    if not brain then return end
    local n = BanditsNPC.Needs.Get(brain)
    n[key] = math.max(0, math.min(100, (n[key] or 0) + (amount or 0)))
    brain.needsHour = getGameTime():getWorldAgeHours()   -- don't let Update snap it back
    BanditBrain.Update(zombie, brain)
    if Bandit and Bandit.ForceSyncPart then
        Bandit.ForceSyncPart(zombie, { id = brain.id, needs = n, needsHour = brain.needsHour })
    end
end
