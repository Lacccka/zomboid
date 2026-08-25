--
-- True Companions - Combat protection + knockdown
--
-- Companions are hijacked zombies, and a single bandit bullet one-shots a zombie -- so the
-- per-tick health floor can't save them (death lands the same frame as the shot). We
-- intercept damage at its source (BanditUtils.Hit, used by bandit line-of-fire) so:
--   * friendly fire from the player or another companion does NOTHING,
--   * enemy fire CHIPS a companion's health and, when low, KNOCKS IT DOWN instead of
--     killing it. A downed companion is incapacitated + protected until you help it up
--     ("Help up" in the V window). Gated by the "Companions can't be killed" sandbox option.
--

BanditsNPC = BanditsNPC or {}
BanditsNPC.Combat = {}
local C = BanditsNPC.Combat

-- FIX(BUG-001): how many EMPTY magazines of one type a companion may keep.
--
-- Reloading fabricates a magazine. There is no real mag in the virtual-counter model, so
-- Bandits INSTANCES one and drops it (ZAUnload.lua:29) -- correct for a hostile bandit,
-- that is loot the player earned. patchUnload below then took it into her inventory, and
-- nothing anywhere removed it again: every consumer in both codebases skips a mag holding
-- no rounds (BanditPrograms.lua:403, ZALootWeapons.lua:46, BanditsNPCInteract.lua:780).
-- One new empty magazine per reload, permanently, unbounded -- which is the reported ammo
-- "duplication", plus a growing inventory walk on every SyncAmmo sweep, plus carry weight
-- that eventually makes ItemList.Fits refuse every gift.
--
-- ZERO IS DELIBERATE AND IS NOT THE SAME AS DELETING THE FEATURE. The keep-them rationale
-- below claims the mag is "reused rather than merely carried"; it is not. Nothing reads an
-- empty magazine, so retaining one buys no resupply -- only litter avoidance. At 0 every
-- spent mag stays where Bandits dropped it, which is a small pile at the fight site rather
-- than the map-wide trail this wrapper was written to prevent.
--
-- The COUNT is type-aware on purpose: a companion carrying mags for a different gun must
-- keep reclaiming her own. Raise this to 1 or 2 to retain a few; nothing else changes.
local RECLAIM_EMPTY_MAG_CAP = 0

local function immortalOn()
    return (BanditsNPC.Opt and BanditsNPC.Opt("ImmortalCompanions", true)) ~= false
end

local function maxHp(brain)
    return (brain and brain.health and brain.health > 0) and brain.health or 2.0
end

local function isCompanion(z)
    return BanditsNPC.IsRecruitedCompanion and BanditsNPC.IsRecruitedCompanion(z)
end

-- a hit from the local player or another recruited companion is "friendly"
local function shooterIsAlly(shooter)
    if not shooter then return false end
    -- ANY player, not just the LOCAL one (v0.75.15, audit Cluster A leftovers).
    -- isLocalPlayer() is true only for the client running this code, so in multiplayer a
    -- shot from anybody else read as hostile: another player firing near your companion
    -- could knock her down, and the friendly-fire protection that exists to prevent
    -- exactly that only ever covered one of the people able to trigger it.
    -- instanceof IsoPlayer is the question actually being asked -- "was this a person?".
    local isPlayer = false
    pcall(function() isPlayer = instanceof(shooter, "IsoPlayer") end)
    if isPlayer then return true end
    local sb = BanditBrain and BanditBrain.Get(shooter)
    return (sb and sb.recruited) and true or false
end

-- knock a companion down (incapacitated + protected until helped up)
function C.KnockDown(zombie, brain)
    if not (zombie and brain) or brain.downed then return end
    brain.downed = true
    brain.downedPosed = nil   -- let NPCDowned play the fall once for this knockdown
    -- flush the queue DIRECTLY (Bandit.ClearTasks preserves lock=true tasks): stale move/
    -- combat tasks would keep her walking around while downed, and the engine won't
    -- dispatch NPCDowned until the queue is empty. Stop looping action sounds too: an
    -- interrupted Bandage/Wash never reaches the onComplete that stops its emitter.
    brain.tasks = {}
    pcall(function() zombie:getEmitter():stopAll() end)
    if (brain.program and brain.program.name) ~= "NPCDowned" then
        brain.prevProgram = brain.program
    end
    brain.program = { name = "NPCDowned", stage = "Prepare" }
    pcall(function() zombie:setHealth(maxHp(brain) * 0.12) end)
    BanditBrain.Update(zombie, brain)
    if Bandit and Bandit.ForceSyncPart then
        pcall(function() Bandit.ForceSyncPart(zombie, { id = brain.id, downed = true, program = brain.program, prevProgram = brain.prevProgram }) end)
    end
    pcall(function() zombie:addLineChatElement(BanditsNPC.T("UI_BN_Bark_Down", "(down!)"), 0.95, 0.5, 0.4) end)
end

-- enemy bullet hits a companion: chip its health; knock it down when low; never lethal.
function C.EnemyHitCompanion(zombie, brain)
    if not (zombie and brain) or brain.downed then return end
    local mh = maxHp(brain)
    local cur = mh; pcall(function() cur = zombie:getHealth() end)
    local nh = cur - mh * 0.18
    -- SAME THRESHOLD AS THE GUARDIAN (v0.75.27). This was a bare 0.30 here and a bare
    -- 0.30 there, two copies of one rule that nothing kept in step -- change one and a
    -- companion knocks down at a different point depending on which path damaged her.
    -- The Guardian owns the constants; this reads its value and falls back to the same
    -- number if it is somehow unavailable.
    local KD = (BanditsNPC.Guardian and BanditsNPC.Guardian.KNOCKDOWN_FRAC) or 0.30
    if nh <= mh * KD then
        C.KnockDown(zombie, brain)
    else
        pcall(function() zombie:setHealth(nh) end)
    end
end

-- restore a downed companion to its feet (called from the V window "Help up" button).
function C.HelpUp(zombie)
    local brain = BanditBrain and BanditBrain.Get(zombie)
    if not (brain and brain.downed) then return false end
    brain.downed = false
    -- flush the queue DIRECTLY: the downed hold is a lock=true Time task (~1000000 ticks)
    -- that Bandit.ClearTasks preserves, and the engine never dispatches a program while any
    -- task is queued -- without this she stays frozen on the ground after being helped up.
    -- Stop leftover looping sounds too (interrupted self-bandage kept its sound forever).
    brain.tasks = {}
    pcall(function() zombie:getEmitter():stopAll() end)
    local prev = brain.prevProgram or { name = "NPCCompanion" }
    brain.prevProgram = nil
    brain.program = { name = prev.name or "NPCCompanion", stage = "Prepare" }
    -- BACK ON HER FEET, NOT BACK IN THE FIGHT. 70% instantly made a knockdown a
    -- formality; a little over a third plus a day of recovery makes it something that
    -- costs you. The rest is healed by the slow regen in BanditsNPC.Combat.Regen, which
    -- runs from the Guardian tick and takes roughly an in-game day to bring her to full.
    --
    -- IT MUST CLEAR THE GUARDIAN'S KNOCKDOWN THRESHOLD (v0.77.13, diagnosed by HaddockGER
    -- on the Workshop page). This was a bare 0.20 against a knockdown threshold of 0.30,
    -- and that is the "help up, instantly down again, bleeding forever" report: helped up
    -- at 20%, the Guardian saw <30% on its very next tick and knocked her straight back
    -- down at 12%, so every press of Help up ratcheted her DOWNWARD. Both healers were
    -- locked out of the cycle too -- Regen returns early while downed, and the Guardian's
    -- counter-drain had a 30% floor -- so there was no exit at all, and Bandits' own
    -- below-40% behaviour (constant blood, endless self-bandage) is what players saw.
    -- Read the published fraction rather than a fourth hardcoded copy of the same rule.
    local UP = (BanditsNPC.Guardian and BanditsNPC.Guardian.HELPUP_FRAC) or 0.35
    pcall(function() zombie:setHealth(maxHp(brain) * UP) end)
    BanditBrain.Update(zombie, brain)
    if Bandit and Bandit.ForceSyncPart then
        pcall(function() Bandit.ForceSyncPart(zombie, { id = brain.id, downed = false, program = brain.program }) end)
    end
    pcall(function() zombie:addLineChatElement(BanditsNPC.T("UI_BN_Bark_Up", "(up)"), 0.6, 0.95, 0.6) end)
    return true
end

-- ===== slow healing =====
--
-- Being helped up now returns 20% health rather than 70%, so there has to be a way
-- back to full or a companion would accumulate knockdowns until she was permanently
-- one hit from the floor.
--
-- Paced in GAME HOURS, not ticks: roughly a full day from 20% to full, so a bad fight
-- costs you a day of her being fragile. Driven off brain.hpHour rather than a timer,
-- which means it keeps working across a save, a reload and a chunk unload -- the
-- elapsed time is read from the world clock, not counted while she happens to be
-- loaded.
local HEAL_PER_HOUR = 0.8 / 24     -- fraction of max health

function C.Regen(zombie)
    local brain = BanditBrain and BanditBrain.Get(zombie)
    if not (brain and brain.recruited) or brain.downed then return end
    pcall(function()
        local now = getGameTime():getWorldAgeHours()
        local last = brain.hpHour
        brain.hpHour = now
        if not last or now <= last then return end

        local max = maxHp(brain)
        local hp = zombie:getHealth()
        if hp >= max then return end

        local gain = max * HEAL_PER_HOUR * (now - last)
        if gain <= 0 then return end
        zombie:setHealth(math.min(max, hp + gain))
    end)
end

-- ===== patch BanditUtils.Hit (bandit line-of-fire damage) =====
local function patchHit()
    -- IDENTITY, NOT A LATCH (v0.75.37). This was `if BanditsNPC._hitPatched then return`,
    -- a one-shot flag -- so the OnGameStart re-install existed but could never do
    -- anything. Any mod that assigns BanditUtils.Hit after us silently removes companion
    -- damage interception (friendly fire stops being ignored, knockdown-instead-of-death
    -- stops firing) and the latch guaranteed we would never notice or repair it. Testing
    -- whether OUR wrapper is still installed is the pattern the friendly-fire compat
    -- already uses (BanditsNPCCompat.lua:35) -- and it also means a mod that CHAINS us
    -- (calls ours) is left alone, because re-wrapping their wrapper would double our
    -- interception.
    if not (BanditUtils and BanditUtils.Hit) then return end
    if BanditsNPC._hitWrapper and BanditUtils.Hit == BanditsNPC._hitWrapper then return end
    local orig = BanditUtils.Hit
    BanditUtils.Hit = function(shooter, item, victim, damageSplit)
        local intercepted, landed = false, false
        pcall(function()
            if isCompanion(victim) then
                if shooterIsAlly(shooter) then
                    intercepted, landed = true, false          -- friendly fire: ignore
                elseif immortalOn() then
                    intercepted, landed = true, true           -- enemy fire: chip + knock down
                    C.EnemyHitCompanion(victim, BanditBrain.Get(victim))
                end
                -- (immortal OFF + enemy: fall through to normal lethal damage)
            end
        end)
        if intercepted then return landed end
        return orig(shooter, item, victim, damageSplit)
    end
    -- remembered so the OnGameStart re-install can tell ours from a replacement
    BanditsNPC._hitWrapper = BanditUtils.Hit
end

-- ===== keep spent magazines instead of littering with them =====
--
-- ZombieActions.Unload.onComplete drops the empty magazine on the floor
-- (AddWorldInventoryItem). For a raider that is fine -- it is loot. For a companion
-- following you round it means a trail of magazines across the map and a gun that
-- cannot be resupplied with the mags she already had.
--
-- CHAINED, NOT REPLACED, and specifically chained AFTER: rather than reimplementing
-- Bandits' unload (which would silently drift the day they change it), the original
-- runs untouched -- mag hits the floor -- and then we pick that exact item straight
-- back up. One extra square scan, no duplicated logic, and it keeps working if the
-- upstream action gains new behaviour.
--
-- Companions only. A hostile bandit still drops hers, because that is loot the player
-- has earned.
--
-- FIX(BUG-001): the line that used to sit here claimed picking the magazine up "feeds it
-- back to Interact.SyncAmmo on the next sweep, so the magazine is reused rather than merely
-- carried". That was wrong about its own code: SyncAmmo only banks a mag holding rounds
-- (BanditsNPCInteract.lua:780), so an empty one is merely carried, for ever. See
-- RECLAIM_EMPTY_MAG_CAP at the top of this file.

-- How many EMPTY magazines of `magType` she is already carrying. Counted rather than
-- tracked so it stays correct no matter how the mags got there (reclaimed, given, looted).
local function countEmptyMags(inv, magType)
    local n = 0
    pcall(function()
        local list = ArrayList.new()
        inv:getAllEvalRecurse(function(it)
            return it:getFullType() == magType and (it:getCurrentAmmoCount() or 0) == 0
        end, list)
        n = list:size()
    end)
    return n
end

local function patchUnload()
    if not (ZombieActions and ZombieActions.Unload and ZombieActions.Unload.onComplete) then return end
    -- Identity rather than a latch, same reason as the Hit patch above.
    if not (ZombieActions and ZombieActions.Unload and ZombieActions.Unload.onComplete) then return end
    if BanditsNPC._unloadWrapper
       and ZombieActions.Unload.onComplete == BanditsNPC._unloadWrapper then return end
    local orig = ZombieActions.Unload.onComplete
    ZombieActions.Unload.onComplete = function(zombie, task)
        local result = orig(zombie, task)
        pcall(function()
            if not (task and task.drop) then return end
            if not isCompanion(zombie) then return end
            local sq = zombie:getSquare()
            local inv = zombie:getInventory()
            if not (sq and inv) then return end
            local objs = sq:getWorldObjects()
            if not objs then return end
            -- Backwards: the magazine we want is the one just added, and taking the
            -- newest match avoids stealing an identical mag that was already lying
            -- there before she reloaded.
            for i = objs:size() - 1, 0, -1 do
                local o = objs:get(i)
                local it = o and o:getItem()
                if it and it:getFullType() == task.drop then
                    -- FIX(BUG-001): at or over the cap, leave it where Bandits dropped it.
                    -- The mag is ALREADY on the floor at this point, so declining to pick
                    -- it up needs no drop path -- it simply stays as loot.
                    if countEmptyMags(inv, task.drop) >= RECLAIM_EMPTY_MAG_CAP then return end
                    inv:AddItem(it)
                    sq:transmitRemoveItemFromSquare(o)
                    return
                end
            end
        end)
        return result
    end
    BanditsNPC._unloadWrapper = ZombieActions.Unload.onComplete
end

Events.OnGameStart.Add(patchHit)
patchHit()
Events.OnGameStart.Add(patchUnload)
patchUnload()
