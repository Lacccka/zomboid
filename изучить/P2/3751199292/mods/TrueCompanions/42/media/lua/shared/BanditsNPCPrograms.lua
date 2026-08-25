--
-- Bandits NPC - Companions Overhaul - AI programs
--
-- Registers our own companion program into the shared ZombiePrograms table
-- (the Bandits engine dispatches ZombiePrograms[name][stage] each tick).
--
-- NPCCompanion is based on Bandits' Companion program, but the "proactive
-- engage" block (run toward any enemy within 8 tiles) is removed and gated
-- behind a combat stance. By default an NPC stays with its master and only
-- defends itself at close range (the engine's combat loop still lets it swing
-- at adjacent enemies) instead of charging out and aggroing the neighbourhood.
--

ZombiePrograms = ZombiePrograms or {}

-- FORWARD DECLARATION. stepOffFurniture's body lives further down (near the other
-- furniture helpers) but NPCRelax.Main above it calls it -- without this upvalue the
-- call compiles as a nil GLOBAL lookup and every relax tick errors ("Relax at Base
-- error loop", tester reports 7 Jul, trace line 310).
local stepOffFurniture

-- Facing for a seat tile: chairs/sofas carry their orientation in the sprite's own
-- "Facing" tile property (the same property Week One's NPCs read to sit the right way
-- round). Lets a sit look correct even when the spot was saved without a direction.
-- Defined up here because both NPCRelax (below) and NPCRoutine use it.
local function seatFacing(x, y, z)
    local f
    pcall(function()
        local sq = getCell():getGridSquare(x, y, z or 0)
        if not sq then return end
        local objs = sq:getObjects()
        for i = 0, objs:size() - 1 do
            local o = objs:get(i)
            local sprite = o and o.getSprite and o:getSprite()
            local props = sprite and sprite:getProperties()
            if props and props:has("Facing") then f = props:get("Facing"); return end
        end
    end)
    if f == "N" or f == "S" or f == "E" or f == "W" then return f end
    return nil
end

-- Direction from a seat tile to the nearest TELEVISION within a few tiles (same
-- floor), so a "TV" sit always faces the set no matter which way the spot's blue
-- arrow was pointed (tester set the arrow "in front of the TV" and she sat with her
-- back to it, 11 Jul -- auto-facing makes the TV spot foolproof). IsoTelevision is
-- the engine's own class for every TV object (vanilla ISMoveableSpriteProps creates
-- them by that isoType). Returns "N"/"S"/"E"/"W", or nil when no TV is around.
-- The nearest television to a tile, as the object plus its offset. ONE scan serves both
-- callers: which way to face, and which set to switch on. Since v0.75.57 the "tv" spot is
-- a SEAT rather than the television's own tile (she used to sit inside the set), so the
-- turn-on can no longer look at the spot square alone -- it has to find the TV the same
-- way the facing does.
local function nearestTV(x, y, z)
    local best, bestD, bestObj
    pcall(function()
        for dy = -4, 4 do
            for dx = -4, 4 do
                local sq = getCell():getGridSquare(x + dx, y + dy, z or 0)
                if sq then
                    local objs = sq:getObjects()
                    for i = 0, objs:size() - 1 do
                        local o = objs:get(i)
                        if o and instanceof(o, "IsoTelevision") then
                            local d = dx * dx + dy * dy
                            if not bestD or d < bestD then bestD = d; best = { dx, dy }; bestObj = o end
                        end
                    end
                end
            end
        end
    end)
    return bestObj, best
end

-- Switch the nearest set on. Silent no-op when there is none, or it is already on.
--
-- FIX(BUG-026): IT NOW REPORTS WHETHER IT ACTUALLY SWITCHED SOMETHING ON.
-- She used to leave every television running when she moved to the next pastime -- a
-- persistent world change the player never asked for, sighted twice independently.
--
-- `not d:getIsTurnedOn()` is the one place in this mod that knows "this set was OFF and I
-- am the one turning it on", so THAT branch, and nothing else, decides whether an undo is
-- owed. **A set the player left on returns false here, so it is never recorded and can
-- never be switched off by her.** There is no second ownership test to keep in sync.
--
-- The RECORDING is not done here. It is done by the pastime framework below, so that a
-- future world-mutating pastime -- a light, a radio, a stove, a tap -- gets the same
-- bookkeeping without repeating any of it. This function is now just the appliance verb.
--     d:getIsTurnedOn()      precedent: refs/pz-lua/client/RadioCom/ISRadioAction.lua:45
--     d:setIsTurnedOn(true)  precedent: refs/pz-lua/client/RadioCom/ISRadioWindow.lua:153
local function turnOnTV(x, y, z)
    local o = nearestTV(x, y, z)
    if not o then return false end
    local did = false
    pcall(function()
        local d = o:getDeviceData()
        if d and not d:getIsTurnedOn() then
            d:setIsTurnedOn(true)
            d:setDeviceVolume(0.4)
            did = true
        end
    end)
    return did
end

-- FIX(BUG-026): the missing half, and the other appliance verb. Takes the SPOT she was
-- sent to, not the television: the object cannot live on the brain (modData is serialised)
-- and reading an object's own coordinates has no cited precedent in refs/, so this re-runs
-- the SAME nearestTV scan from the SAME tile. Deterministic over an unchanged world. The
-- one case that misdirects it -- a different set becoming nearest to that tile while she
-- watched -- needs the player to place AND switch on a second television within four tiles
-- of her seat, and costs one wrongly darkened screen.
--
-- WHAT HAPPENS WHEN THE WORLD CHANGED UNDERNEATH HER:
--   * the set was destroyed, or the room unloaded -> nearestTV finds nothing, and it is
--     itself pcall-wrapped, so the scan cannot throw.
--   * a player switched it off by hand -> getIsTurnedOn() is false, and we do not touch it.
--   * the power went out -> the engine keeps the flag and the power separately (its own
--     "really on" test is `getIsTurnedOn() and getPower()>0`, ISRadioAction.lua:58), so the
--     flag is still true and clearing it is still right. Do NOT add a power test.
--   * a player switched it off and then ON again while she watched -> she darkens a set
--     someone is using. Unavoidable without object identity, and bounded to ONE occurrence
--     because the framework consumes the record before calling this.
--     d:setIsTurnedOn(false) precedent: refs/pz-lua/client/RadioCom/ISRadioWindow.lua:153
local function turnOffTV(x, y, z)
    pcall(function()
        local o = nearestTV(x, y, z)
        if not o then return end
        local d = o:getDeviceData()
        if d and d:getIsTurnedOn() then
            d:setIsTurnedOn(false)
        end
    end)
end

local function tvFacing(x, y, z)
    local _, best = nearestTV(x, y, z)
    -- Standing ON the set (dx=dy=0) gives no direction to face; treat it as no TV.
    if best and best[1] == 0 and best[2] == 0 then best = nil end
    if not best then return nil end
    if math.abs(best[1]) >= math.abs(best[2]) then
        return (best[1] > 0) and "E" or "W"
    end
    return (best[2] > 0) and "S" or "N"
end

-- ===========================================================================
-- FIX(TASK-018): WHO IS ALLOWED TO SMOKE.
-- FOUR SITES IN THIS FILE, PLUS EVERY DELEGATION TO BANDITS' OWN IDLE.
-- ===========================================================================
--
-- THIS SITS AT THE TOP OF THE FILE BECAUSE IT HAS TO. `idleTasks` is called from
-- NPCCompanion.Main, which is the first program defined below -- a definition further
-- down would not be in scope there. The block was written next to PASTIMES first, when
-- the change was believed to be about a pastime. It is not.
--
-- Reported as "the smoking pastime reads as out of character on female companions".
-- Measured before changing anything, off the `bump=` column in logs/ (that column is
-- read back from the engine -- `zombie:getBumpType()`, client/BanditsNPCGuardian.lua:384):
-- `bump=Smoke` appears in ALL 23 traces, 1394 samples, the third most common bump state
-- in the project after BNSitChair and empty, and 500 of 2399 samples (21%) in the most
-- recent one. It was not a rare flourish; it was most of what a companion did on screen.
--
-- TWO CONDITIONS, AND THEY ARE ORTHOGONAL -- NEITHER ONE ALONE IS THE FIX:
--   * `not female` -- the reported complaint.
--   * `personality.smoker` -- a female non-smoker would still smoke under a smoker-only
--     gate, and a male non-smoker would still smoke under a sex-only gate. The mod
--     ALREADY contradicts itself on the second: `brain.personality.smoker` is what puts
--     "still keeps a crumpled pack of cigarettes close" in her backstory
--     (BanditsNPCBackstory.lua:149) and what unlocks the cigarette quest
--     (BanditsNPCQuests.lua:44), so three companions in four have been smoking while
--     their own biography says they do not.
--
-- NIL SAFETY, ESTABLISHED FROM THE SOURCE, NOT ASSUMED:
--   * `brain.female` -- BanditServerSpawner.lua:324, `general.female or false`. The
--     `or false` makes it a boolean on every brain that spawner ever built, and every
--     companion we spawn goes through it (BanditsNPCSpawnServer.lua:51/:56,
--     BanditsNPCWildSpawn.lua:413). Never nil.
--   * `brain.personality` -- BanditServerSpawner.lua:377, `brain.personality = {}`,
--     unconditional and in the same straight-line block; `.smoker` is set two lines
--     later to `(ZombRand(4) == 0)`, a boolean. Identical in all seven vendored Bandits
--     versions back to 42.12, so no drift.
--   * IT IS STILL GUARDED BELOW. A brain arriving from anywhere other than that spawner
--     is a table we did not build, and the two existing readers both guard it the same
--     way -- `brain.personality or {}` (Backstory.lua:146) and `brain.personality and
--     brain.personality[...]` (Quests.lua:156). A nil `personality` must mean "not a
--     smoker", never an error inside a program that runs three times a second.
--
-- WHAT THIS DOES NOT FIX, AND IT IS LOGGED AS BUG-051: `personality` is NOT in
-- Persistence's CUSTOM_FIELDS, so `banditize` re-rolls it on every restore while the
-- backstory it wrote is cached in `storyParts`, which IS restored. Whether a given
-- companion smokes therefore changes between sessions. That was true before this change
-- and this change does not make it worse -- but it is now load-bearing for something
-- visible, so it is written down.
local function maySmoke(brain)
    if not brain or brain.female then return false end
    local p = brain.personality
    return (p and p.smoker) == true
end

-- ===========================================================================
-- FIX(TASK-018b): THE FOURTH SITE IS NOT IN THIS MOD AT ALL.
-- ===========================================================================
--
-- TASK-018 gated the three `"Smoke"` literals in our own code and shipped a comment
-- claiming that was all of them. It was not, and the miss is the exact failure that
-- comment warned about. `BanditPrograms.Idle` (refs/.../shared/BanditPrograms.lua:453)
-- has its own smoking branch:
--
--     local action = ZombRand(10)              -- BanditPrograms.lua:455
--     ...
--     elseif action == 3 then                  -- BanditPrograms.lua:473
--         local task = {action="Time", anim="Smoke", time=200}
--         table.insert(tasks, task)            -- THREE TIMES, the same table
--         table.insert(tasks, task)
--         table.insert(tasks, task)
--
-- WE CALL IT FROM EIGHT PLACES -- NPCCompanion.Main and .Guard, NPCStay.Main,
-- NPCGuard.Main (twice), NPCWork.Main, NPCNeutral.Main (twice). That is following,
-- staying, guarding, working and wandering: EVERY PROGRAM EXCEPT THE TWO TASK-018
-- GATED. A companion spends most of her day in these.
--
-- HOW IT WAS CAUGHT, AND THE SIGNATURE TO REMEMBER: a trace line read
-- `bump=Smoke task=Time/WORKING t=180.0 n=3 prog=NPCStay`. `t` counting from 200 ruled
-- out the pastime (600) and the combat sites (250); `n=3` is `#brain.tasks`
-- (BanditsNPCGuardian.lua:386) and NOTHING ELSE IN EITHER CODEBASE QUEUES THREE TASKS.
-- The arity identified the branch before the file was opened.
--
-- SUBSTITUTE, NEVER DROP. Removing the three tasks can return an empty list, and a
-- program that returns zero tasks is re-evaluated immediately (BanditUpdate.lua:1889).
-- That is livelock bait and this project has had three of those already (BUG-017,
-- BUG-035, BUG-042). The replacement keeps the task count and the durations exactly.
--
-- SUBSTITUTE FROM BANDITS' OWN POOL, AT RANDOM, NOT WITH ONE FIXED ANIMATION.
-- Always-ChewNails was the first idea and the arithmetic kills it: Smoke is 1-in-10 by
-- CHANCE but 600 ticks by DURATION where every other outcome is 200, so it is 25% of all
-- idle TIME. ChewNails is already 3-in-10 by chance (actions 2, 8 and 9), so folding
-- Smoke into it would take ChewNails from 25% to 50% of idle time -- a companion biting
-- her nails half of every day, which is a more visible defect than the one being fixed.
-- Spreading it over the seven gives ChewNails ~29% and the rest ~12% each, which is
-- Bandits' own idle mix minus the cigarette. That is the target: an ineligible companion
-- should look NORMAL, not like a loop.
--
-- ALL SEVEN NAMES ARE BANDITS' OWN, FROM THAT SAME FUNCTION (BanditPrograms.lua:465-491),
-- SO NO NEW ANIMATION NAME IS INTRODUCED. That is a hard constraint, not a preference:
-- Bandits' AnimSets are not vendored in refs/, so a name from anywhere else would be
-- uncitable, and a wrong one fails silently to the idle sway (see UNKNOWNS.md).
--
-- ONE REPLACEMENT PER CALL, APPLIED TO ALL THREE TASKS, because the original is 600
-- continuous ticks of ONE animation. Three different 200-tick anims would read as
-- fidgeting and would not be the rhythm Bandits designed.
--
-- THE BRAIN IS FETCHED LAZILY AND ONLY WHEN A SMOKE TASK IS ACTUALLY PRESENT -- nine
-- calls in ten do no lookup at all. Two of the eight call sites (NPCCompanion.Main and
-- .Guard) have no `brain` in scope and would otherwise have needed one added on a path
-- that runs whenever her task queue empties.
local IDLE_SUBS = { "ShiftWeight", "Cough", "ChewNails", "PullAtCollar",
                    "Sneeze", "WipeBrow", "WipeHead" }

local function idleTasks(bandit)
    local tasks = BanditPrograms.Idle(bandit)
    local sub
    for i = 1, #tasks do
        local t = tasks[i]
        if t.anim == "Smoke" then
            if sub == nil then
                if maySmoke(BanditBrain.Get(bandit)) then return tasks end
                sub = IDLE_SUBS[1 + ZombRand(#IDLE_SUBS)]
            end
            -- a NEW table per slot: Bandits inserts one shared table three times, so
            -- mutating in place would be invisible here but is not worth relying on.
            tasks[i] = { action = t.action, anim = sub, time = t.time }
        end
    end
    return tasks
end

ZombiePrograms.NPCCompanion = {}

ZombiePrograms.NPCCompanion.Prepare = function(bandit)
    local tasks = {}
    Bandit.ForceStationary(bandit, false)
    return {status = true, next = "Main", tasks = tasks}
end

ZombiePrograms.NPCCompanion.Main = function(bandit)
    local tasks = {}

    if BanditsNPC.Needs then BanditsNPC.Needs.Update(bandit) end
    if BanditsNPC.Routine and BanditsNPC.Routine.MaybeStart(bandit) then
        return {status = true, next = "Main", tasks = tasks}
    end
    if BanditsNPC.Schedule and BanditsNPC.Schedule.Apply(bandit) then
        return {status = true, next = "Main", tasks = tasks}
    end
    Bandit.ForceStationary(bandit, false)

    -- If standing on a guardpost, switch to the Guard stage.
    if BanditPost.At(bandit, "guard") then
        return {status = true, next = "Guard", tasks = tasks}
    end

    -- Without a master there's nothing to do but idle. Resolve the OWNER strictly
    -- (only the recruiter -- matched by id OR stable username, never a nearby stranger).
    -- NO engine fallback on purpose: BanditPlayer.GetMasterPlayer resolves by raw online
    -- id, which MP can reassign to a different player who joined later -- that would make
    -- the companion tag along with a stranger. If the real owner isn't present we leave
    -- master nil and idle instead.
    local master = BanditsNPC.GetOwner(bandit)
    if not master then
        table.insert(tasks, {action = "Time", anim = "Shrug", time = 200})
        return {status = true, next = "Main", tasks = tasks}
    end

    -- Match the master's pace.
    local walkType = "Walk"
    local endurance = 0.00
    local dist = BanditUtils.DistTo(bandit:getX(), bandit:getY(), master:getX(), master:getY())

    -- VIRTUAL vehicle ride. A zombie must NEVER be a real occupant: the engine's crash
    -- handling (BaseVehicle.crash -> damagePlayers -> addRandomDamageFromCrash) calls
    -- getBodyDamage() on every seated character, which is null for zombies -> NPE in the
    -- main loop -> session aborts to the main menu (proven by tester crash, 4 Jul). So when
    -- the master drives off, a close-enough companion is DESPAWNED and tracked in the
    -- roster; Persistence.onPlayerUpdate respawns her beside the master when he gets out.
    --
    -- The EVICTION below is UNCONDITIONAL (outside our CompanionsEnterVehicles gate):
    -- newer Bandits builds can physically SEAT bandits when their own "bandits may enter
    -- vehicles" sandbox option is on -- that is the crash-NPE scenario again, and a
    -- seated companion also vanishes from the roster scans, so P.Tick spawned copies of
    -- her (the "duplicates with both vehicle options enabled" report, 11 Jul). Whatever
    -- put her in a seat -- legacy save, Bandits' vehicle feature, another mod -- she gets
    -- out at once. (The Guardian evicts the same way for non-Companion programs/shells.)
    local banditVeh = bandit:getVehicle()
    if banditVeh then
        pcall(function()
            bandit:setVariable("BanditImmediateAnim", true)
            local seat = banditVeh:getSeat(bandit)
            banditVeh:exit(bandit)
            if seat and seat >= 0 then
                banditVeh:setCharacterPosition(bandit, seat, "outside")
            end
            bandit:playSound("VehicleDoorClose")
        end)
    elseif (BanditsNPC.Opt and BanditsNPC.Opt("CompanionsEnterVehicles", true)) ~= false then
        local masterVeh = master:getVehicle()
        if masterVeh and dist < 3.0 and BanditsNPC.Persistence and BanditsNPC.Persistence.BeginRide then
            local brain = BanditBrain.Get(bandit)
            if brain and BanditsNPC.Persistence.BeginRide(bandit, brain) then
                return {status = true, next = "Main", tasks = tasks}
            end
        end
        -- not close enough yet: fall through to the follow logic (walks her to the car)
    end

    if master:isSprinting() or dist > 10 then
        walkType, endurance = "Run", -0.07
    elseif master:isSneaking() and dist < 12 then
        walkType, endurance = "SneakWalk", -0.01
    end

    local outOfAmmo = Bandit.IsOutOfAmmo(bandit)
    if master:isAiming() and not outOfAmmo and dist < 8 then
        walkType, endurance = "WalkAim", 0
    end

    if bandit:getHealth() < 0.4 then
        walkType, endurance = "Limp", 0
    end

    -- Take a nearby guardpost if one is free.
    local guardpost = BanditPost.GetClosestFree(bandit, "guard", 40)
    if guardpost then
        table.insert(tasks, BanditUtils.GetMoveTask(endurance, guardpost.x, guardpost.y, guardpost.z, walkType, dist))
        return {status = true, next = "Main", tasks = tasks}
    end

    -- STANCE GATE: only aggressive companions proactively move to engage.
    -- (defensive/passive simply stay with the master; close-range defense is
    -- still handled by the engine's combat loop. Full "passive = ignore" comes
    -- with the AreEnemies hook in a later phase.)
    if BanditsNPC.GetStance(bandit) == "aggressive" and dist < 20 then
        local closestZombie = BanditUtils.GetClosestZombieLocation(bandit)
        local closestBandit = BanditUtils.GetClosestEnemyBanditLocation(bandit)
        local closestEnemy = closestZombie
        if closestBandit.dist < closestZombie.dist then
            closestEnemy = closestBandit
        end
        if closestEnemy.dist < 8 then
            table.insert(tasks, BanditUtils.GetMoveTask(endurance, closestEnemy.x, closestEnemy.y, closestEnemy.z, "WalkAim", closestEnemy.dist))
            return {status = true, next = "Main", tasks = tasks}
        end
    end

    -- Otherwise stick with the master, then idle when next to them.
    local dx, dy, dz = master:getX(), master:getY(), master:getZ()
    local did = BanditUtils.GetCharacterID(master)
    local distTarget = BanditUtils.DistTo(bandit:getX(), bandit:getY(), dx, dy)

    -- HOW CLOSE SHE STOPS is the player's choice now (v0.76.1) -- near/mid/far from the
    -- roster panel. This was a hardcoded 1, which put her permanently on your heels.
    -- A floor change always closes regardless: "far" must not mean she stays upstairs.
    local stopAt = 1
    pcall(function() stopAt = BanditsNPC.GetFollowStop(bandit) or 1 end)
    if distTarget > stopAt or math.abs(dz - bandit:getZ()) >= 1 then
        table.insert(tasks, BanditUtils.GetMoveTaskTarget(endurance, dx, dy, dz, did, true, walkType, distTarget))
        return {status = true, next = "Main", tasks = tasks}
    else
        local subTasks = idleTasks(bandit)
        for _, subTask in pairs(subTasks) do
            table.insert(tasks, subTask)
        end
    end

    return {status = true, next = "Main", tasks = tasks}
end

ZombiePrograms.NPCCompanion.Guard = function(bandit)
    local tasks = {}

    if not BanditPost.At(bandit, "guard") then
        Bandit.ForceStationary(bandit, true)
        return {status = true, next = "Main", tasks = tasks}
    end

    local closestZombie = BanditUtils.GetClosestZombieLocation(bandit)
    local closestBandit = BanditUtils.GetClosestEnemyBanditLocation(bandit)
    local closestEnemy = closestZombie
    if closestBandit.dist < closestZombie.dist then
        closestEnemy = closestBandit
    end

    -- ENGAGE a close threat: close to striking range so ManageCombat can actually hit it
    -- (see NPCGuard). Otherwise hold the post and keep watch.
    if BanditsNPC.GetStance(bandit) ~= "passive" and closestEnemy.dist < 10 then
        if closestEnemy.dist > 1.6 then
            Bandit.ForceStationary(bandit, false)
            table.insert(tasks, BanditUtils.GetMoveTask(0, closestEnemy.x, closestEnemy.y, closestEnemy.z or 0, "WalkAim", closestEnemy.dist))
        else
            table.insert(tasks, {action = "FaceLocation", x = closestEnemy.x, y = closestEnemy.y, time = 100})
        end
        return {status = true, next = "Guard", tasks = tasks}
    end

    Bandit.ForceStationary(bandit, true)
    if closestEnemy.dist < 24 then
        -- anim: an anim-less FaceLocation hold leaves the ZOMBIE idle sway playing
        -- ("NPCs wobble around like a zombie" report) -- give the hold a human idle
        table.insert(tasks, {action = "FaceLocation", anim = "ShiftWeight", x = closestEnemy.x, y = closestEnemy.y, time = 100})
    else
        local subTasks = idleTasks(bandit)
        for _, subTask in pairs(subTasks) do
            table.insert(tasks, subTask)
        end
    end

    return {status = true, next = "Guard", tasks = tasks}
end

-- NPCStay: walk to the chosen tile (brain.stayPos), then hold position there.
-- Without a tile, just holds wherever it stands (used by Hide). Combat loop
-- still lets it defend at close range.
ZombiePrograms.NPCStay = {}
ZombiePrograms.NPCStay.Prepare = function(bandit)
    return {status = true, next = "Main", tasks = {}}
end
ZombiePrograms.NPCStay.Main = function(bandit)
    local tasks = {}
    if BanditsNPC.Needs then BanditsNPC.Needs.Update(bandit) end
    if BanditsNPC.Routine and BanditsNPC.Routine.MaybeStart(bandit) then
        return {status = true, next = "Main", tasks = tasks}
    end
    if BanditsNPC.Schedule and BanditsNPC.Schedule.Apply(bandit) then
        return {status = true, next = "Main", tasks = tasks}
    end
    local brain = BanditBrain.Get(bandit)
    local pos = brain and brain.stayPos

    if pos then
        local tx, ty = pos.x + 0.5, pos.y + 0.5
        local dist = BanditUtils.DistTo(bandit:getX(), bandit:getY(), tx, ty)
        if dist > 0.6 or math.abs((pos.z or 0) - bandit:getZ()) >= 1 then
            Bandit.ForceStationary(bandit, false)
            table.insert(tasks, BanditUtils.GetMoveTask(0, tx, ty, pos.z or 0, "Walk", dist))
            return {status = true, next = "Main", tasks = tasks}
        end
    end

    Bandit.ForceStationary(bandit, true)
    for _, t in pairs(idleTasks(bandit)) do table.insert(tasks, t) end
    return {status = true, next = "Main", tasks = tasks}
end

-- ===========================================================================
-- PASTIMES -- what a companion actually DOES at home
-- ===========================================================================
--
-- "I followed one companion to see what she does and she only sat in a chair all day in
-- the kitchen and did nothing else." (4 Aug.) That was accurate: relaxing was a 30%
-- wander, a 30% chance of the ONE chair the base scan picked for her, and otherwise one
-- of four standing idles. Nothing in it referred to anything else in the house.
--
-- A pastime is a piece of furniture, a place to stand, an animation, and a line over her
-- head. Which ones exist depends entirely on what the player has built: a house with a
-- cooker gets someone cooking in it, a house without one does not. `spot` is the key the
-- base scan files that furniture under.
--
-- `effect` runs once on arrival, for the two that change the world rather than just
-- looking busy.
--
-- The anims are Bandits' zombie AnimSets, same rule as the work animations: the bump
-- type is the AnimSet filename minus the ZS prefix and .xml. Naming one that does not
-- exist falls back to the idle sway rather than erroring, so a wrong name here is a
-- cosmetic bug, not a crash -- but they were checked all the same.
-- ===========================================================================
-- THE PASTIME ANIMATIONS WERE NEVER AN ART PROBLEM (v0.77.14)
-- ===========================================================================
--
-- The note that used to sit on the TV entry said a ground sit "needs an animation
-- RETARGETED FOR THE ZOMBIE RIG -- vanilla's Bob_SitGround_* live in anims_X/Bob (the
-- player skeleton)... That is an art asset, not a code change." That is wrong, and it
-- cost this mod every idle animation its companions could have had.
--
-- Bandits' own zombie bump AnimSets name Bob_* assets 186 times out of 190, and 161 of
-- those resolve to VANILLA anims_X/Bob files with no retargeted copy anywhere -- Bandits
-- ships only 14 .x files, exactly the ones vanilla lacks. A bumped companion plays player
-- animations perfectly well. Our own two .x files were never retargets either; they are
-- custom POSES (a chair-height sit, a bed-height sleep) that vanilla simply does not have.
--
-- So everything below is already installed and needed no new asset:
--     Sit           Bob_SitGround_Idle        SitRubHands  Bob_SitGround_RubHands
--     SitMaking     Bob_SitGround_Making      SitAction    Bob_SitGround_ActionIdle
--     ReadBook      Bob_IdleReadBook          washFace     Bob_IdleWashFace
--     Smoke         Bob_IdleSmoke             Refuel       Bob_IdleGasCan_RefuelCar
--     Hammer        Bob_IdleHammering         LootLow      Bob_IdleLooting_Low
--     Forage        Bob_IdleForage            Drink        Bob_DrinkFromBottle
--
-- NAMES ARE THE BUMPTYPE CONDITION, NOT THE FILENAME. Bandits sets the bump type straight
-- from task.anim (BanditUpdate.lua:1789) and each node fires on its m_StringValue -- which
-- for the file ZSReadBook.xml is "ReadBook". The ZS prefix appears as a condition value in
-- ZERO of the 190 nodes, and every anim string in this mod used to carry it. See the
-- v0.77.14 changelog entry for what that actually did.
--
-- ADDING AN OUTDOOR PASTIME? SET `outdoor = true` ON IT. READ THIS FIRST.
--
-- Every entry in this table is filtered by `spotOnAnchorSide` (BUG-033): when her anchor
-- is indoors, a spot whose square has no room is REJECTED. That is deliberate and it is
-- what stops her walking out of a window she cannot climb back through. All eight entries
-- below are indoor furniture, so today the filter never rejects anything it should keep.
--
-- THE FAILURE MODE IF YOU FORGET IS SILENT. A rejected entry is simply not appended to
-- the candidate list. No error, no trace line, no chat line, nothing in the console --
-- the only symptom is a pastime that quietly stops happening, months later, with nobody
-- able to connect it to this table. That is why the flag is explicit rather than the gate
-- being clever about which spots "look" outdoor.
--
-- A garden, a campfire, a generator, a rain collector, a washing line: all of those are
-- legitimately outdoors and all of them need `outdoor = true`. The car pastime does not
-- carry the flag only because it is built on the fly further down and never enters this
-- loop at all -- see the note where it is constructed.
local PASTIMES = {
    {   id = "tv", spot = "tv", weight = 3, time = 900,
        -- SHE SITS DOWN TO WATCH IT NOW, in the chair, properly.
        --
        -- The old entry played nothing at all, on the reasoning that BNSitChair "is a
        -- SEATED pose and this pastime STANDS HER AT THE TELEVISION". That was true when
        -- it was written and stopped being true at v0.75.57, when the tv spot became a
        -- CHAIR FACING THE SET rather than the set itself -- the comment three lines
        -- below has said so ever since. Two comments in one entry disagreeing is how it
        -- survived.
        --
        -- `sit` routes it through the BNSit action instead of a plain Time task, which is
        -- what pins her onto the furniture at seat height with the right facing (and the
        -- couch nudge). A ground-sit here would be wrong for exactly the reason the old
        -- note gave -- it is the FLOOR poses that cannot go on a chair, and those now live
        -- in the ambient idle, which runs only after stepOffFurniture.
        sit = true,
        face = true,
        say = function() return BanditsNPC.T("UI_BN_Do_TV", "Watching TV") end,
        -- SHE TURNS IT ON. A companion sitting in front of a dead television is the
        -- kind of detail that reads as broken rather than quiet. Searches OUTWARD from
        -- the seat (v0.75.57): the spot is a chair facing the set now, not the set.
        -- FIX(BUG-026): `effect` RETURNS whether it really changed the world, and `undo`
        -- is its counterpart. See the contract over relaxUndo below -- everything else
        -- (recording, occupancy, deferral) is the framework's job, not this entry's.
        effect = function(bandit, sq)
            return turnOnTV(sq:getX(), sq:getY(), sq:getZ())
        end,
        undo = function(bandit, rec)
            turnOffTV(rec.x, rec.y, rec.z)
        end },
    -- Every one of these used the generic mid-height rummage. They now use the animation
    -- that matches what the line of text over her head claims she is doing.
    {   id = "cook", spot = "stove", weight = 2, time = 700, anim = "Loot", face = true,
        say = function() return BanditsNPC.T("UI_BN_Do_Cook", "Cooking a meal") end },
    {   id = "wash", spot = "washer", weight = 2, time = 700, anim = "LootLow", face = true,
        say = function() return BanditsNPC.T("UI_BN_Do_Wash", "Washing clothes") end },
    {   id = "read", spot = "bookshelf", weight = 2, time = 600, anim = "ReadBook", face = true,
        say = function() return BanditsNPC.T("UI_BN_Do_Read", "Reading") end },
    -- FIX(TASK-018): `smokes` is the ONLY declaration here. WHO may smoke is `maySmoke`
    -- below, because the same animation is played from two other places that are not
    -- pastimes at all. A flag that read `maleOnly` would have to be repeated, out of
    -- sight, at both of them -- and the first one anybody forgot would put the cigarette
    -- straight back on screen with nothing in this table to explain why.
    {   id = "window", spot = "window", weight = 1, time = 600, anim = "Smoke", face = true,
        smokes = true,
        say = function() return BanditsNPC.T("UI_BN_Do_Window", "Having a smoke by the window") end },
    -- ===== NEW, and all on spots the base scan ALREADY finds =====
    -- No new scan key, so a base that works today gains these with no rescan: bathroom and
    -- food are in Base.KEYS, bookshelf is in Base.PASTIME_KEYS.
    --
    -- All three are STANDING animations, deliberately. A pastime is played by a plain Time
    -- task, which does not move her -- so a floor pose on a furniture tile would put her
    -- through the furniture. Seated belongs on `sit` above, floor poses in the ambient
    -- idle below, and everything here stands at the thing it is using.
    {   id = "washup", spot = "bathroom", weight = 2, time = 500, anim = "washFace", face = true,
        say = function() return BanditsNPC.T("UI_BN_Do_WashUp", "Washing up") end },
    {   id = "brew", spot = "food", weight = 1, time = 500, anim = "Drink", face = true,
        say = function() return BanditsNPC.T("UI_BN_Do_Brew", "Getting a drink") end },
    {   id = "tidy", spot = "bookshelf", weight = 1, time = 600, anim = "LootLow", face = true,
        say = function() return BanditsNPC.T("UI_BN_Do_Tidy", "Tidying up") end },
}

-- Vehicles are not furniture and the base scan cannot file them, so this is its own
-- lookup: the nearest vehicle inside the base area. getCell():getVehicles() is the
-- engine's own live list.
-- atHome IS THE BOUNDARY, NOT `zone` (v0.77.13). This took `zone` and tested
-- `not zone or Contains(...)` -- so with NO base area drawn, the `not zone` arm was true
-- for EVERY vehicle in the loaded cell and the nearest car anywhere on the map became a
-- valid thing to go and do. That is "I tell them to relax at base and they go outside to
-- my car" and "npc always tryin to fix engine car while chillin on base", and it is also
-- the walking-in-circles: NPCRelax.Main sends a strayed companion home BEFORE it resumes a
-- committed pastime, so she walked toward a car outside the radius, tripped the come-home
-- branch, walked back, re-resumed the car, forever. Passing the caller's atHome closure
-- makes that impossible by construction -- it is the same test the chair already used, and
-- it means "inside the drawn area" when there is one and "near the anchor" when there is
-- not, instead of "anywhere at all".
local function vehicleInBase(bandit, inHome)
    local best, bestD
    pcall(function()
        -- getVehicles() RETURNS A java.util.Set, NOT A LIST (IsoCell.class:
        -- `public getVehicles ()Ljava/util/Set;`). A Set has size() but NO get(int),
        -- so the obvious indexed loop calls a nil and the failure is a KahluaUtil.fail
        -- that walks straight OUT of this pcall -- it killed availablePastimes, and
        -- with it the whole bandit update tick, every time a pastime was picked
        -- (v0.75.57, author test; 5 stack dumps in one session).
        --
        -- Vanilla's own ISVehicleBloodUI.lua:80 does `vehicles:get(i-1)` and is just as
        -- broken; it lives in an admin UI that nobody runs, which is why it survives.
        -- THE PROVEN PATTERN IS BANDITS' OWN (BanditClientCommands.lua:6): toArray()
        -- then pairs. Same engine build, same call, and it is exercised constantly.
        local vs = getCell() and getCell():getVehicles()
        if not vs then return end
        local list
        pcall(function() list = vs:toArray() end)
        if not list then return end
        local bx, by = bandit:getX(), bandit:getY()
        for _, v in pairs(list) do
            local sq = v and v:getSquare()
            if sq and inHome(sq:getX() + 0.5, sq:getY() + 0.5, sq:getZ()) then
                local d = BanditUtils.DistTo(bx, by, sq:getX(), sq:getY())
                if not bestD or d < bestD then
                    best, bestD = { x = sq:getX(), y = sq:getY(), z = sq:getZ() }, d
                end
            end
        end
    end)
    return best
end

-- FIX(BUG-033): A FURNITURE PASTIME SPOT MUST BE ON THE SAME SIDE OF THE WALL AS HER
-- ANCHOR. She climbed out of a window, smoked, and then could not get back in -- 96s of
-- livelock ending in a watchdog teleport.
--
-- WHY A WINDOW SPOT CAN BE OUTDOORS AT ALL, which is not obvious and is the whole bug.
-- PZ stores wall objects on the N and W EDGES of a square (Bandits states it at
-- BanditUpdate.lua:533 and depends on it at :610-614). The window pastime spot IS the
-- IsoWindow's own square (Base.PASTIME_KEYS, Base.lua:47; P.window, Base.lua:199-201),
-- and window squares are isFree(false), so BUG-021's redirect never fires and the Move
-- target is that square itself. Therefore a window in a building's SOUTH or EAST wall
-- has its spot on the tile OUTSIDE the building; north and west walls keep it inside.
-- That is about HALF the windows of any building, so this is not one badly-drawn zone --
-- a zone edge that clears a wall by one tile exposes every one of them, and players draw
-- zones loosely.
--
-- WHY NOT FIX IT DOWNSTREAM. Rescue is the wrong layer: `atHome` is zone membership, so
-- the "strayed out, come home first" branch reads her as home and never fires; the sit
-- branch has no budget; and the watchdog teleport is the symptom everybody reports. The
-- only place that removes the whole family is the moment the spot is offered.
--
-- THE REFERENCE IS THE ANCHOR, NOT HER LIVE POSITION. "The far side of the wall from
-- her" is the right instinct and the wrong variable: the spot is committed to
-- brain.relaxDoSpot once and she moves afterwards, so at commit time there is no stable
-- "her side". brain.relaxPos is the anchor, is always set by the block above before this
-- runs, and Base.Anchor already retries up to 20 squares to land on one INDOORS
-- (Base.lua:670-680).
--
-- getRoom() ~= nil IS THIS CODEBASE'S OWN INDOORS TEST, already live in this very
-- program: Programs.lua:988 and :993 gate the wander destination on
-- `not anchorIndoors or wsq:getRoom() ~= nil`. This is that idiom applied one function
-- earlier. `square:isOutside()` would also work and is well precedented (pz-lua
-- ISZoneDisplay.lua:400; bandits BanditUpdate.lua:912) but is not what we already speak.
--
-- IT IS OFF UNLESS HER ANCHOR IS INDOORS. A camp with no building, an outdoor anchor,
-- anything without a room: `anchorInside` is false and every spot passes exactly as
-- before. This can only ever remove candidates from an indoor base.
--
-- KNOWN LIMITS, BOTH ACCEPTED. (1) A room test is not a reachability test: an enclosed
-- porch or attached garage is a room and can still be behind a door she cannot open.
-- It is a very good proxy, not a proof. (2) An interior window between two rooms has
-- BOTH candidate tiles in rooms, so it is kept -- correct, either side is fine there.
--
-- FIX(TASK-014): a pastime that is SUPPOSED to be outdoors sets `outdoor = true` and is
-- waved through. Without that flag this gate's rule -- "every PASTIMES entry must be
-- indoors when the anchor is indoors" -- was true only by accident of what the table
-- happens to contain, and the first outdoor pastime anyone added would have been deleted
-- in silence. The opt-out is read at the call site so the two halves of the rule sit on
-- one line and neither can be read without the other.
local function spotOnAnchorSide(s, anchorInside)
    if not anchorInside then return true end
    local ok = false
    pcall(function()
        local sq = getCell():getGridSquare(s.x, s.y, s.z or 0)
        ok = sq ~= nil and sq:getRoom() ~= nil
    end)
    return ok
end

-- FIX(BUG-047): forward declaration. `availablePastimes` asks the occupancy query about
-- each candidate, and that query is defined ~150 lines below with the rest of the BUG-026
-- machinery it was written for. Declared here rather than moved, because moving it would
-- separate it from `OCCUPANCY_R` and `relaxUndo` and would churn a verified fix for
-- cosmetic reasons. Same idiom as `stepOffFurniture` at the top of this file -- and the
-- same hazard: WITHOUT THIS LINE the call below compiles as a nil GLOBAL lookup and every
-- pastime roll errors.
local spotInUseByOther

-- Everything she could do right now, as { spot = {x,y,z}, def = <pastime> } entries,
-- weighted. Anything the house does not have simply is not in the list.
--
-- Takes atHome rather than the zone, for the reason spelled out over vehicleInBase: with
-- no area drawn, `not zone` waved through every candidate, and the only reason the
-- furniture pastimes were not as badly affected as the car is that their spots come from
-- the beacon's own scan and are therefore near the base by construction. Testing them all
-- the same way costs nothing and removes the difference.
local function availablePastimes(brain, bandit, inHome)
    local out = {}
    -- FIX(BUG-033): the anchor's own side, read once per roll. brain.relaxPos is
    -- guaranteed non-nil here -- all three branches of the anchor block above assign it.
    local anchorInside = false
    pcall(function()
        local a = brain.relaxPos
        local aSq = a and getCell():getGridSquare(a.x, a.y, a.z or 0)
        anchorInside = aSq ~= nil and aSq:getRoom() ~= nil
    end)
    local spots = BanditsNPC.Base and BanditsNPC.Base.Spots
                  and BanditsNPC.Base.SiteFor and BanditsNPC.Base.Spots(BanditsNPC.Base.SiteFor(bandit))
    -- FIX(TASK-015): COUNT THE REJECTIONS, DO NOT JUST DROP THEM.
    --
    -- An empty list is the single most opaque failure this program has: she stands
    -- perfectly still, forever, with no error, no chat line and no trace line, and the
    -- report that arrives is "she does nothing". These three counters are the difference
    -- between that and one readable line. They cost three increments per roll.
    --
    -- The condition below is the SAME condition it has always been -- `s and inHome and
    -- (outdoor or side)` -- only unrolled so each rejection can be attributed. Do not
    -- re-flatten it; the attribution is the point.
    local nspot, nzone, nside, nsmoke, nbusy = 0, 0, 0, 0, 0
    for _, def in ipairs(PASTIMES) do
        local list = spots and spots[def.spot]
        if list and #list > 0 then
            local s = list[1 + ZombRand(#list)]
            if s then
                nspot = nspot + 1
                -- FIX(BUG-033) + FIX(TASK-014): the side test sits HERE and deliberately
                -- not around the vehicle below -- a car is an outdoor errand she reaches
                -- through a door, and gating it on "indoors" would delete the pastime
                -- outright. `def.outdoor` is the opt-out for anything in PASTIMES that
                -- belongs outside; see the note over the table. It is checked FIRST so an
                -- outdoor pastime never pays for the grid lookup.
                if not inHome(s.x + 0.5, s.y + 0.5, s.z or 0) then
                    nzone = nzone + 1
                elseif not (def.outdoor or spotOnAnchorSide(s, anchorInside)) then
                    nside = nside + 1
                -- FIX(TASK-018): THIRD REJECTION, FOURTH COUNTER -- `nspot` counts
                -- candidates, not rejections, so the two numbers never match.
                -- TASK-015's rule above
                -- is not decoration -- a rejection with no counter leaves `nspot` no
                -- longer accounted for, and the one failure this diagnostic exists to
                -- explain ("she does nothing") loses a possible cause in silence.
                -- THE COUNTER IS `offsmoke`, NOT `offsex`, DELIBERATELY: a rejected male
                -- non-smoker has nothing to do with sex, and a counter that named the
                -- wrong reason would be worse than no counter at all.
                elseif def.smokes and not maySmoke(brain) then
                    nsmoke = nsmoke + 1
                -- FIX(BUG-047): IS ANOTHER COMPANION ALREADY COMMITTED TO THIS TILE?
                --
                -- LAST IN THE CHAIN ON PURPOSE. This is the only test here that walks the
                -- zombie list; the three above are arithmetic and one grid lookup. Putting
                -- it last means it runs only on candidates that were going to be kept, and
                -- it makes `offbusy` mean "otherwise valid, but taken" rather than "taken,
                -- and possibly out of the zone as well".
                --
                -- FOURTH REJECTION, FIFTH COUNTER -- TASK-015's rule, same as `offsmoke`.
                -- The chain rejects on zone, side, smoke and this; `nspot` is the fifth
                -- counter only because it counts what was OFFERED, ahead of all four.
                elseif spotInUseByOther(bandit, { id = def.id, x = s.x, y = s.y,
                                                  z = s.z or 0 }, def, true) then
                    nbusy = nbusy + 1
                else
                    for _ = 1, (def.weight or 1) do out[#out + 1] = { spot = s, def = def } end
                end
            end
        end
    end
    -- THE CAR IS EXEMPT FROM BUG-033's SIDE TEST BY CONSTRUCTION, NOT BY A FLAG: it is
    -- appended here, AFTER the PASTIMES loop closes, so `spotOnAnchorSide` never sees it.
    -- If this is ever moved INTO that table it must carry `outdoor = true` or it will be
    -- deleted in silence for every indoor base -- a driveway tile has no room. (TASK-014)
    local v = vehicleInBase(bandit, inHome)
    if v then
        local def = { id = "car", time = 800, anim = "Loot", face = true,
                      say = function() return BanditsNPC.T("UI_BN_Do_Car", "Fixing the engine") end }
        out[#out + 1] = { spot = v, def = def }
        out[#out + 1] = { spot = v, def = def }
    end
    -- Second return is DIAGNOSTIC ONLY and there is exactly one caller. `spots` is how
    -- many pastimes the base scan could offer a tile for at all -- spots=0 means the base
    -- has no recognised furniture and neither gate is implicated. `offzone` is the zone
    -- test, `offside` is BUG-033's indoor test, `offsmoke` is TASK-018's, `offbusy` is
    -- BUG-047's.
    --
    -- FIX(TASK-019): THE CALLER WRITES THIS ON EVERY ROLL, INCLUDING THE ONES THAT
    -- SUCCEED. It used to reach the trace only when the list came back empty, which is
    -- the one case a working base never produces -- so two shipped gates went three
    -- sessions with no path to verification. **If you add a sixth rejection, its counter
    -- is already reachable; do not re-route this.**
    return out, "spots=" .. nspot .. " offzone=" .. nzone .. " offside=" .. nside
                .. " offsmoke=" .. nsmoke .. " offbusy=" .. nbusy
end

-- TWO COMPANIONS IN THE SAME ROOM TALK TO EACH OTHER. Returns the nearest other
-- relaxing companion within a few tiles, or nil.
local function chatPartner(bandit)
    local found, bestD
    pcall(function()
        local zl = getCell():getZombieList()
        local bx, by, bz = bandit:getX(), bandit:getY(), bandit:getZ()
        for i = 0, zl:size() - 1 do
            local z = zl:get(i)
            if z and z ~= bandit and z:getVariableBoolean("Bandit") and math.abs(z:getZ() - bz) < 1 then
                local ob = BanditBrain.Get(z)
                if ob and ob.master and ob.program and ob.program.name == "NPCRelax" then
                    local d = BanditUtils.DistTo(bx, by, z:getX(), z:getY())
                    if d < 6 and (not bestD or d < bestD) then found, bestD = z, d end
                end
            end
        end
    end)
    return found
end

-- Pastimes by id, so a committed goal can be resolved back to its definition on a later
-- tick. Seeded from PASTIMES; the vehicle entry is built at pick time and registers
-- itself here, because there is no fixed list of cars in a base.
local PASTIME_BY_ID = {}
for _, d in ipairs(PASTIMES) do PASTIME_BY_ID[d.id] = d end

-- ===== FIX(BUG-026): THE PASTIME UNDO CONTRACT =====
--
-- THE DEFECT THIS EXISTS FOR WAS IN THE HOOK, NOT IN THE TELEVISION. `effect` was a
-- one-way, per-companion, world-mutating callback with no occupancy concept anywhere in
-- its design (see the note over PASTIMES). The tv entry is its only definition today, so
-- the bug looked like a TV bug -- but a light, a radio, a stove or a tap would each have
-- inherited it whole. The fix is therefore here, and a second world-mutating pastime added
-- later gets all of this for free.
--
-- THE CONTRACT, and it is the entire API:
--     effect(bandit, sq)  -> return TRUTHY if you actually changed the world.
--                            Return false/nil when you found it already in the state you
--                            wanted -- that is what keeps her off the player's things.
--     undo(bandit, rec)   -> reverse it. rec = { id, x, y, z }, the spot she was sent to.
--                            Called at most once per effect, never while somebody else is
--                            still using the spot.
-- An entry with an `effect` and no `undo` simply never records, so adding a harmless
-- effect costs nothing.
--
-- WHY A LIVE QUERY AND NOT A SHARED OWNERSHIP TABLE. `ModData.getOrCreate` is what this
-- mod uses for cross-object state, and Persistence.lua:187 records what it actually is:
-- "a CLIENT-LOCAL store -- ModData.getOrCreate with no transmit, per client". Two clients
-- driving two companions would not see each other's entries, which is the exact bug being
-- fixed. And a refcount that misses a decrement -- despawn, death, dismissal, a reload
-- mid-episode -- leaks silently and permanently, so the set is never switched off again.
-- Bandits' own "is this taken" test is a live query for the same reasons
-- (`if not square:getZombie()`, BanditPost.lua:56).
--
-- THE RADIUS IS DELIBERATELY LONG. nearestTV searches +/-4 tiles, so two spots served by
-- one set can be 8 apart. A false "somebody is still using it" leaves an appliance running
-- -- the old, cosmetic symptom. A false "nobody is using it" darkens a set someone is
-- watching -- the new, visible one. Err towards over-detecting.
local OCCUPANCY_R = 8

-- FIX(BUG-047): THE CONTENTION RADIUS. THE SAME TILE, AND NOTHING ELSE.
--
-- 0.5, not 0, and not 1. Adjacent tiles are EXACTLY 1.0 apart under `BanditUtils.DistTo`,
-- so 0.5 admits the same tile and excludes every neighbour, while still tolerating a
-- half-tile offset if a spot list ever stores centres instead of corners. **A radius of 1
-- would reject the second of two chairs facing one television**, which is a pastime the
-- base legitimately offers twice -- the opposite of the bug being fixed.
--
-- IT IS A SEPARATE CONSTANT FROM `OCCUPANCY_R` BECAUSE THE TWO QUESTIONS ARE DIFFERENT.
-- OCCUPANCY_R answers "is anyone still watching this television", where the set serves
-- spots up to 8 apart and over-detecting merely leaves an appliance running. This answers
-- "is anyone sitting in this chair", where over-detecting at radius 8 would reserve every
-- seat in the house to the first companion who sat down.
local CLAIM_R = 0.5

-- Is ANOTHER companion still using the spot this record belongs to?
--
-- TWO PROGRAMS CAN BE AT ONE SPOT AND BOTH ARE CHECKED. NPCRelax commits via
-- `relaxDo`/`relaxDoSpot`, so match the pastime id against her committed spot -- that
-- counts a companion still walking in, which is what we want. NPCRoutine reaches the same
-- furniture by a different path with `routineTask.spotKey`, which is the SPOT key rather
-- than the pastime id (they coincide for "tv" and do not for e.g. read/bookshelf), and it
-- has no committed-spot field, so match her actual position instead.
--
-- `square:getZombie()` is NOT used here: it returns only ONE zombie (Interact.lua:1668
-- says so and avoids it for the same reason), and two companions on one tile is exactly
-- the case that matters. The live list is the only thing that can see both.
--     cell:getZombieList()  precedent: refs/bandits/.../client/BanditZombie.lua:107
--     list:get(i)           precedent: refs/bandits/.../client/BanditPlayer.lua:34
--     z:isAlive()           precedent: refs/bandits/.../client/BanditUpdate.lua:973
--     BanditBrain.Get(z)              refs/bandits/.../shared/BanditBrain.lua:3
--     BanditUtils.DistTo              refs/bandits/.../shared/BanditUtils.lua:1233
-- A hostile bandit has no relaxDo and no routineTask, so the field tests are also the
-- "is she one of ours" test; no separate predicate is needed.
-- ===========================================================================
-- FIX(BUG-047): THE SAME QUERY ANSWERS A SECOND QUESTION -- `claim` SELECTS WHICH.
-- ===========================================================================
--
-- `claim = false` (BUG-026, unchanged): IS ANOTHER COMPANION STILL USING WHAT THIS RECORD
-- TURNED ON? Keyed on the pastime id, radius 8, and it deliberately does NOT test whether
-- she is still in NPCRelax -- over-detecting there leaves a television on, which is the
-- old cosmetic symptom, and under-detecting darkens a set somebody is watching.
--
-- `claim = true` (BUG-047): IS ANOTHER COMPANION COMMITTED TO THIS EXACT TILE? Keyed on
-- COORDINATES, radius CLAIM_R, and the claimant must still be relaxing.
--
-- WHY COORDINATES AND NOT THE ID. `read` and `tidy` both declare `spot = "bookshelf"`, so
-- one companion reading and another tidying can draw the SAME TILE with different
-- `relaxDo` values. An id test would not match them and the two would stack -- which is
-- precisely the bug. The question here is about the tile, so the test is about the tile.
--
-- WHY THE PROGRAM TEST, AND WHY ONLY IN THIS MODE. **All three sites that clear `relaxDo`
-- are inside NPCRelax.Main** (:1115, and the two abandonment paths in the relax roll). A
-- companion switched to Follow, Stay or Guard mid-pastime therefore carries a stale
-- commitment on her brain for as long as she stays out of that program -- and without this
-- term she would hold a chair nobody is sitting in, indefinitely. **That is "a pastime
-- silently disappears", which is the exact failure the reservation design was rejected
-- for, and it is the ONLY way this fix could leak one.** `chatPartner` in this same file
-- already tests a companion's program the same way (`ob.program.name == "NPCRelax"`), so
-- the shape is established here, not invented.
--
-- WHY THIS LEAKS NOTHING ELSE, WHICH IS THE WHOLE ARGUMENT FOR A QUERY OVER A REGISTRY:
-- the claim IS `brain.relaxDoSpot`, it lives on the claimant, and this walks the live
-- zombie list -- so a companion who despawns, unloads or dies simply stops being seen.
-- **A claim cannot outlive its claimant, so there is nothing to release.** A registry
-- would have needed a release on every one of those paths, and the missed one is what
-- makes a spot vanish for the rest of the save.
--
-- AND IT SEES A COMPANION WHO IS STILL WALKING IN, twenty tiles out, because it reads her
-- COMMITMENT rather than her position. `square:getZombie()` cannot -- and both-committed-
-- then-both-arrived is the observed collision, not two bodies appearing at once.
--
-- KNOWN AND ACCEPTED: two companions evaluating in the same tick both see the tile free
-- and both walk in. Programs run on their own dispatches so this is possible and not the
-- common case, and it self-corrects on the next roll. **The fix is the same query called
-- again on arrival, which is BUG-031's shape** -- do not add a second mechanism for it.
--
-- COST, STATED BECAUSE IT IS NEW: up to one zombie-list walk per surviving candidate per
-- pastime roll (5-7 in a furnished base), against zero before. It is bounded -- a roll only
-- happens when her task queue empties -- and it is small beside Bandits' own baseline,
-- which walks the entire zombie list and calls `getModData()` on every bandit EVERY TICK
-- to rebuild its caches (BanditZombie.lua:111-139). If it ever measures, the upgrade is
-- `BanditZombie.CacheLightB` -- bandits only, carries `{x, y, z, brain}`, rebuilt by that
-- same flush, and already read this way in Base.lua's spot allocator. Not done now: it
-- would change the data source under BUG-026's verified path for a cost nobody has felt.
--
--     cell:getZombieList()  precedent: refs/bandits/.../client/BanditZombie.lua:107
--     list:get(i)           precedent: refs/bandits/.../client/BanditPlayer.lua:34
--     z:isAlive()           precedent: refs/bandits/.../client/BanditUpdate.lua:973
--     BanditBrain.Get(z)              refs/bandits/.../shared/BanditBrain.lua:3
--     BanditUtils.DistTo              refs/bandits/.../shared/BanditUtils.lua:1233
spotInUseByOther = function(bandit, rec, def, claim)
    local used = false
    local r = claim and CLAIM_R or OCCUPANCY_R
    -- HER POSITION IS THE TILE CENTRE AND `rec` IS THE TILE CORNER. At radius 8 that
    -- 0.707 of a tile is noise; at CLAIM_R it is the difference between matching and
    -- never matching, so the routine branch below compares against the centre. The
    -- appliance mode keeps the corner it has always used -- this changes nothing there.
    local px, py = rec.x, rec.y
    if claim then px, py = rec.x + 0.5, rec.y + 0.5 end
    pcall(function()
        local zl = getCell():getZombieList()
        for i = 0, zl:size() - 1 do
            local z = zl:get(i)
            if z and z ~= bandit and z:isAlive() and math.abs(z:getZ() - (rec.z or 0)) < 1 then
                local b = BanditBrain.Get(z)
                if b then
                    local sp
                    if claim then
                        sp = (b.program and b.program.name == "NPCRelax") and b.relaxDoSpot
                    else
                        sp = (b.relaxDo == rec.id) and b.relaxDoSpot
                    end
                    if sp and BanditUtils.DistTo(sp.x, sp.y, rec.x, rec.y) <= r then
                        used = true
                        return
                    end
                    local rt = b.routineTask
                    if rt and def.spot and rt.spotKey == def.spot
                       and BanditUtils.DistTo(z:getX(), z:getY(), px, py) <= r then
                        used = true
                        return
                    end
                end
            end
        end
    end)
    return used
end

-- Reverse whatever she switched on, once nobody else is using it.
--
-- CONSUME ON PERMANENT FAILURE, RETAIN ON DELIBERATE DEFERRAL -- the two branches below,
-- and getting them the wrong way round is how the first version of this fix broke:
--   * somebody else is still there -> RETURN WITHOUT CLEARING. She retries at every later
--     fall-through, so whoever holds the record eventually switches it off. Clearing here
--     instead would leave NOBODY holding it -- the second companion never got a record,
--     because the set was already on when she arrived -- and the appliance would run
--     forever, which is the original bug reintroduced by its own fix.
--   * anything else (no such pastime any more, no undo declared, or we are going ahead)
--     -> CLEAR FIRST, then try once. A set that has been destroyed or unloaded must not
--     leave a note that re-fires and eventually acts on whatever took its place.
-- RESIDUAL, ACCEPTED: if the record-holder dies, is dismissed, or never relaxes near that
-- spot again, the appliance stays on. That is the old symptom in a strictly rarer case,
-- and it fails in the safe direction.
local function relaxUndo(bandit, brain)
    local rec = brain and brain.npcUndo
    if not rec then return end
    local def = PASTIME_BY_ID[rec.id]
    if not (def and def.undo) then brain.npcUndo = nil; return end
    if spotInUseByOther(bandit, rec, def) then return end
    brain.npcUndo = nil
    pcall(function() def.undo(bandit, rec) end)
end

-- Written as literal T calls rather than a key list plus a fallback list, so the key
-- extractor can actually see them -- a computed key never reaches EN/UI.json and would
-- have shipped untranslatable.
local SMALL_TALK = {
    function() return BanditsNPC.T("UI_BN_Chat_1", "Think it'll rain again tonight?") end,
    function() return BanditsNPC.T("UI_BN_Chat_2", "I keep dreaming about proper coffee.") end,
    function() return BanditsNPC.T("UI_BN_Chat_3", "Quiet today. I'll take quiet.") end,
    function() return BanditsNPC.T("UI_BN_Chat_4", "You holding up all right?") end,
    function() return BanditsNPC.T("UI_BN_Chat_5", "We should check the fences before dark.") end,
}

-- NPCRelax: "Relax at Base" -- the home mode between Stay's rigid post and
-- Follow's shadowing. She lives in a small radius around an anchor tile: wanders,
-- plays ambient idles, sits on her assigned Chair spot properly (the engine's
-- positioned Sleep action again -- pinned onto the furniture, facing spot.dir),
-- and runs her need-routines PROACTIVELY (MaybeStart uses a lower threshold while
-- relaxing, and SleepCheck treats her as idle-at-base -> she goes to bed on time).
-- Defends herself per stance via the engine combat loop, like Stay.
local RELAX_RADIUS = 9

-- FIX(BUG-029): ARRIVAL IS A GRID QUESTION, NOT A DISTANCE QUESTION.
--
-- The old test was `BanditUtils.DistTo(...) > 1.5`, and it accepted her stopping a
-- whole tile short. Measured, not argued: logs/bug021-test.txt carries the Move target
-- ->415.5,9755.5 in 18 samples and she NEVER reaches it -- zero positions anywhere on
-- that row in 29 minutes of play. The Move expires mid-route (GetMoveTask hands out
-- only 15+ZombRand(10), BanditUtils.lua:1012, so expiring is ROUTINE, not exceptional),
-- the program re-evaluates, DistTo reads 0.78-1.01, the gate calls that arrival and the
-- animation plays wherever she happens to be standing. That is her washing clothes on a
-- staircase; the mid-kitchen wash with no sink (BUG-025) is the same defect in the same
-- trace, 0.99 tiles short of ->412.5,9760.5.
--
-- WHY NOT Nav.CanWorkAcross, WHICH NPCRoutine USES FOR WHAT LOOKS LIKE THIS BUG.
-- Because it cannot fire here, and copying it would be a SILENT NO-OP:
--   * it only judges CARDINAL neighbours (`if dx + dy ~= 1 then return end`,
--     Nav.lua:227) and both observed stops were diagonal;
--   * it vetoes WINDOW / FRAME / THUMP only (:242) and neither stop was a window;
--   * NPCRoutine's gate is `dist > 0.8` and the veto forces dist to 1.0, which clears
--     it. Ours was 1.5. Forcing 1.0 into a `> 1.5` test changes NOTHING AT ALL.
-- Anyone "applying the same fix here" ships a line that cannot alter behaviour and
-- believes the bug is closed. Do not.
--
-- WHAT THIS ASKS INSTEAD. Standing ON the destination square is arrival, full stop. One
-- CARDINAL step away is also arrival, but only across an edge she could actually walk
-- (ClassifyEdge == OPEN). Diagonals are NOT arrival -- that is exactly the geometry that
-- produced the bug, and it is where DistTo under-reads worst: 1.41 by the ruler for a
-- step that may be unwalkable or may be a detour of ten tiles.
--
-- WHICH SQUARE `tsq` ACTUALLY IS -- READ THIS BEFORE TRUSTING THE EDGE TEST.
-- An earlier version of this comment claimed the edge tested is "never to the furniture"
-- and that tsq is "free by construction". BOTH WERE FALSE ABOUT THIS CODE and the note
-- is corrected here rather than deleted, because the wrong version was load-bearing in a
-- diagnosis. What the caller below actually does:
--     tsq = ssq                                     -- the SPOT square, always, first
--     ...only if `not IsStandable(ssq)` does the AdjacentFreeTileFinder result replace it
-- So when the spot square IS standable, tsq stays the SPOT square -- and "standable" is
-- `isFree(false)`, which is about the square's own flags. It does NOT mean the square is
-- empty and it says nothing about the EDGE onto it, which is what isBlockedTo answers.
-- A sink or a TV on a tile that still reports isFree therefore reaches ClassifyEdge as
-- its own destination.
--
-- THAT IS A LATENT TRAP, NOT A LIVE BUG, AND IT IS DELIBERATELY LEFT ALONE. It is the
-- v0.75.57 case written up at Nav.lua:229-237 -- solid furniture makes the edge to its
-- OWN square BLOCKED, so a sink or counter can report "can't reach". It has never been
-- observed to fire: in logs/bug029-test.txt every give-up happened 12-20 tiles of route
-- out, where the cardinal-adjacency test exits long before ClassifyEdge is reached, so
-- there is no evidence about what it does when it IS reached. Do not "fix" it on
-- evidence that does not cover it. If it ever does fire the signature is a `gave up`
-- line for a spot she is standing right next to.
--
-- CLASSIFYEDGE IS FAIL-CLOSED -- nil squares and any internal failure return BLOCKED
-- (Nav.lua:291, :332). THAT IS THE SAFE DIRECTION HERE, and only because BLOCKED means
-- "not arrived yet, keep walking" and never "give up". A wrong BLOCKED costs her some
-- extra steps and then SELF-HEALS: once she is standing on the square the first test
-- returns true without consulting ClassifyEdge at all. It would be the UNSAFE direction
-- if an edge verdict could abandon a spot, which is why abandonment is driven by the
-- try counter below and by nothing else.
local function relaxArrived(bandit, tsq, wz)
    if not tsq then return false end
    local ok, yes = pcall(function()
        -- Z FIRST. BanditUtils.DistTo is strictly 2D (refs/bandits/.../BanditUtils.lua:
        -- 1233-1236 is sqrt(dx*dx + dy*dy), no z term anywhere), so without this a spot
        -- one floor up counts as reached from directly underneath it. NPCWork:1010 and
        -- NPCRoutine:1198 both carry this term already; NPCRelax was the only one of the
        -- three that did not.
        if math.abs((wz or 0) - bandit:getZ()) >= 1 then return false end
        local hs = bandit:getCurrentSquare()
        if not hs then return false end
        if hs:getX() == tsq:getX() and hs:getY() == tsq:getY()
           and hs:getZ() == tsq:getZ() then return true end
        if hs:getZ() ~= tsq:getZ() then return false end
        if math.abs(hs:getX() - tsq:getX()) + math.abs(hs:getY() - tsq:getY()) ~= 1 then
            return false
        end
        local Nv = BanditsNPC.Nav
        return Nv ~= nil and Nv.ClassifyEdge(hs, tsq) == Nv.EDGE.OPEN
    end)
    return ok and yes == true
end

-- THE UNIT IS PROGRAM EVALUATIONS, AT ROUGHLY THREE PER SECOND. NOT trace samples, NOT
-- ticks, NOT task.time. Getting this wrong is not hypothetical -- the first version of
-- this constant was 20 because "the longest successful approach is 8", and that 8 was
-- counted off TRACE SAMPLES, which are one per second. The program re-runs every time its
-- Move task is replaced (`#tasks == 0 and not Bandit.HasTask`, BanditUpdate.lua:1889) and
-- a Move task lives `15 + ZombRand(10)` decrements at ~1/tick (BanditUtils.lua:1012),
-- i.e. about a THIRD of a second. Measured in logs/bug029-test.txt: 20 evaluations burned
-- in 6.6s and in 7.5s, so 2.7-3.0 evaluations per second. The cap was therefore three
-- times smaller than intended and killed 12 healthy walks in 24 minutes.
--
-- Deliberately not tighter: a route around a building walks her AWAY from the target for
-- a while, and straight-line distance rises the whole time (see below).
--
-- THE ORIGINAL SECOND HALF OF THAT SENTENCE WAS THE DEFECT, AND IT IS KEPT HERE BECAUSE IT
-- LOOKED LIKE DESIGN. It read: "Deliberately not looser: 60 x ~20 dispatches is ~1200,
-- around the stuck watchdog's PORT_TICKS, so a spot she truly cannot reach is written off
-- at roughly the moment the watchdog would step in rather than long after it." Landing ON
-- the watchdog's threshold is not a boundary, it is a TIE: ~1170 dispatches against
-- PORT_TICKS 1200 means whichever fires first is decided by jitter, and the two outcomes
-- are not interchangeable -- a traced give-up names the destination and the reason, a
-- teleport names neither and is the thing users report. Consistent with the evidence:
-- `grep -c "gave up"` is 0 in every trace we hold, and logs/bug033-test.txt has the
-- teleport winning at 1210 ticks.
--
-- THE CAP IS THEREFORE 40, AND IT NO LONGER LIVES HERE. There is ONE cap in this file --
-- `MOVE_TRY_CAP`, declared with TASK-016's shared budget below -- and this site uses it
-- directly rather than keeping a second constant that can drift from it. The arithmetic
-- for 40, and what the tighter cap costs, is written up at that declaration. Do not
-- reintroduce a RELAX_TRY_CAP: two names for one rule is how they end up different.

-- WHAT "MADE PROGRESS" MEANS, AND WHY IT IS NOT `path=`.
-- The budget must measure a STALL, not time spent walking -- that was the whole defect.
-- So the counter resets whenever she reaches a new closest-ever straight-line distance to
-- the walk target, and only climbs while she is failing to beat it.
--
-- NOT PathFindBehavior2:getPathLength() (the trace's `path=` column), tempting though
-- route-length-remaining is. It IS NOT RELIABLY CLEARED: in logs/bug015-attempt1-trace.txt
-- it held 0.4 unchanged for 36.7s of wall time while the real target was 4.4 tiles away,
-- residue from an earlier completed hop, despite ZAMove.onComplete calling cancel(),
-- reset() and setPath2(nil). A stale value that never changes is bad enough as a readout;
-- as the RESET CONDITION for this counter it would be actively dangerous in one direction
-- and useless in the other -- a residue value LOWER than the true remaining route would
-- look like a permanent new best, reset the counter forever, and she would never give up.
-- That is the exact failure this budget exists to prevent, reintroduced through the back
-- door. Straight-line distance is recomputed from her live coordinates every evaluation
-- and cannot go stale.
--
-- KNOWN COST, ACCEPTED: straight-line distance is not route length, so walking around a
-- wall consumes budget even though she is making real progress. That is what the 20
-- seconds of headroom is for. If it still proves too tight the symptom is VISIBLE and
-- names itself -- a `gave up` line for a spot with an obvious detour -- which is the
-- whole reason abandonment is traced rather than silent.
--
-- The 0.1 margin is float noise insurance only. She covers ~0.4 tiles per evaluation
-- while walking, so a real approach clears it comfortably; it exists so that a companion
-- jittering in place cannot reset her own budget one hundredth of a tile at a time.
local RELAX_PROGRESS = 0.1

-- ABANDONMENT MUST NOT BE SILENT. A spot that is unreachable in a given base is
-- unreachable FOREVER, so without this line she quietly skips it on every roll for the
-- life of the save, and the report that eventually arrives is "she never watches TV"
-- with nothing at all behind it. That is strictly harder to diagnose than the visible
-- symptom this fix removes. Name the pastime, name the spot, name the reason.
local function relaxGiveUp(bandit, brain, s, why)
    local T = BanditsNPC.Trace
    if T and T.Line then
        pcall(function()
            T.Line(bandit, "RELAX", "gave up pastime " .. tostring(brain.relaxDo)
                   .. " spot=" .. tostring(s.x) .. "," .. tostring(s.y)
                   .. "," .. tostring(s.z or 0) .. " -- reason: " .. tostring(why))
        end)
    end
    brain.relaxGoal, brain.relaxDo, brain.relaxDoSpot, brain.relaxDoRan,
        brain.relaxDoTries, brain.relaxDoBest = nil, nil, nil, nil, nil, nil
end

-- FIX(TASK-016): ONE STALL BUDGET FOR EVERY MOVE THAT COMMITS TO A DESTINATION.
--
-- BUG-029 gave the pastime approach a progress-based give-up and it works. Every OTHER
-- Move that keeps the same destination across evaluations had none, and the cost is
-- measured, not argued -- logs/climb-probe-trace.txt, 000060.5 to 000272.6: 126
-- consecutive samples of `prog=NPCRelax task=Move ->332.5,9691.5`, position pinned at
-- 333,9689, `path=0.3` for a target 2.37 tiles away, `sticks` climbing 46 -> 7125. That is
-- 212 SECONDS walking to a seat she never reaches, and the program had no way to notice,
-- because ZAMove.onWorking returns true for Failed exactly as for Succeeded
-- (ZAMove.lua:34-39, BUG-018) -- "arrived" and "no route" are the same value.
--
-- THE BUDGET DOES NOT NEED TO KNOW WHY SHE IS STUCK, which is the whole reason it beats
-- the proxy gates (a room match, a window check, an indoor filter). It measures one
-- thing: is she getting closer. Same MECHANISM as BUG-029 and, since 2026-08-22, the same
-- CONSTANT -- `RELAX_TRY_CAP` was deleted and BUG-029's site reads MOVE_TRY_CAP directly.
-- One stall rule in this file, and one number, so the two cannot drift apart.
--
-- KEYED PER DESTINATION, NOT PER SITE, and that is load-bearing. A write-off belongs to a
-- TILE: the seat that cannot be reached stays written off while she goes and does
-- something else, and it costs one evaluation to re-refuse instead of another 60 to
-- re-discover. A single shared slot would be wiped by the next site to run -- the wander
-- branch rolls a fresh tile every time and would evict the seat's verdict within a second.
--
-- WHICH IS ALSO WHY EVICTION IS BY LOWEST `tries`, NOT BY AGE OR WHOLESALE. The entries
-- worth keeping are exactly the ones with a high count; wander's throwaway tiles sit at
-- tries=1 and recycle among themselves. Eight slots is enough for every destination a
-- companion holds at once (home, seat, pastime, two patrol ends) with room to spare, and
-- it is bounded because this table lives on the persisted brain.
--
-- THE REST WINDOW IS NOT OPTIONAL AND IT IS NOT POLITENESS. Guardian.lua:880 gates the
-- ENTIRE watchdog ladder -- flush and teleport both -- on `if moveTask then`. A give-up
-- that stops issuing the Move therefore also switches off the only backstop she has, and
-- a bounded 20-second freeze becomes a permanent one. So a written-off destination
-- expires: after MOVE_REST evaluations the entry is dropped, the Move resumes with a
-- fresh budget, and the watchdog gets its chance. Nothing here may ever write a
-- destination off forever.
--
-- AND THE BACKSTOP IS ONLY DEFERRED, NOT LOST -- better than this was designed for, and
-- read out of the code rather than hoped for. `st.ticks` is incremented on exactly one
-- branch, `elseif moveTask and moveTask.x then` (Guardian.lua:925-927), and the branches
-- above it reset it only when she is InTransit or has moved >1.2 tiles. With no Move task
-- NONE of the three runs, so the counter FREEZES rather than resetting. When the Move
-- resumes it carries on from where it stopped. At :885 -- the one site that idles with no
-- Move at all -- that means the teleport still arrives, about one rest window late instead
-- of never. Do not "tidy" that into an else-branch that zeroes st.ticks.
--
-- Z GUARD: NO ACCRUAL WHILE THE DESTINATION IS ON ANOTHER FLOOR. BanditUtils.DistTo is
-- strictly 2D (refs/bandits/.../BanditUtils.lua:1233-1236, sqrt(dx*dx+dy*dy), no z term),
-- so "getting closer" is meaningless while she is walking to a staircase -- the honest
-- route takes her AWAY in x/y and a budget would fire on a companion doing exactly the
-- right thing. KNOWN AND ACCEPTED CONSEQUENCE: a cross-floor stall is not covered by this
-- fix at any site. The watchdog still is, because a Move task is still being issued.
--
-- WHY 40 AND NOT BUG-029's 60: THIS BUDGET HAS TO BEAT THE WATCHDOG, AND 60 TIES WITH IT.
--
-- A Move task lives `15 + ZombRand(10)` decrements at ~1 per dispatch (BanditUtils.lua:
-- 1012), i.e. 15-24 with a mean of 19.5, and the program re-evaluates each time one is
-- replaced. So N evaluations is about 19.5N dispatches, and the watchdog's PORT_TICKS is
-- 1200 dispatches (Guardian.lua:1233):
--     60 evaluations ~= 1170 dispatches   vs 1200 -- INSIDE the noise. Which fires first
--                                            is chance, and the two outcomes are not
--                                            similar: a traced give-up names the
--                                            destination and the reason, a teleport names
--                                            neither and is the thing users report.
--     40 evaluations ~=  780 dispatches   vs 1200 -- 420 dispatches of margin, ~7 seconds.
--                                            The per-Move spread is only ~2.9 dispatches,
--                                            so over 40 draws the total is 780 +/- 18.
--                                            That is not a race any more, it is an order.
-- Measured confirmation that 60 really was a tie: logs/bug033-test.txt has the teleport
-- winning at 1210 ticks, and `grep -c "gave up"` is 0 in every trace we hold.
--
-- IT STILL LOSES TO THE FLUSH AT 540 DISPATCHES (~28 evaluations), DELIBERATELY. The flush
-- is cheap, invisible and sometimes sufficient; there is no reason to pre-empt a remedy
-- that costs nothing and abandons nothing. Only the TELEPORT has to be beaten.
--
-- WHAT IT COSTS, AND IT IS THE SAME COST BUG-029 WROTE UP AT 60: false give-ups on long
-- legitimate detours. Straight-line distance is the progress measure, so walking around a
-- building raises it for the whole traverse. The symptom names itself when it happens --
-- a STALL line for a destination with an obvious route -- which is exactly why abandonment
-- is traced.
--
-- CORRECTION, MEASURED 2026-08-22 IN logs/task016-test.txt: THE CAP IS NOT A STALL TIMER.
-- It counts CUMULATIVE evaluations in which this destination failed to improve, and the
-- entry outlives any single approach, so those evaluations need not be contiguous and
-- usually are not. In the measured give-up the chair target appears in only TWO of 747
-- TICK samples across 824 seconds, yet 40 tries had accrued -- they were collected in
-- short bursts over about nine minutes, because program switching (NPCRelax <-> the
-- follow program) and Routine/Schedule early-returns mean the sit branch runs only some
-- of the time.
--
-- SO "40 evaluations ~= 13-15 seconds" WAS WRONG AND IS STRUCK. It holds only for a
-- CONTIGUOUS stall. The ordering claim against the watchdog SURVIVES, and a fortiori:
-- cumulative counting can only fire EARLIER in wall-clock than contiguous counting, never
-- later, and where accrual is bursty she is moving, which resets st.ticks and means the
-- watchdog was never going to fire at all. Measured in the same run: max `sticks` was 171
-- against FLUSH_TICKS 540, so nothing came within a third of the watchdog's first rung.
--
-- AND `best` IS A BIASED SAMPLE OF HER CLOSEST APPROACH, WHICH IS THE REAL LIMIT HERE.
-- It only updates on evaluations where this site runs. In the measured run she stood 1.19
-- tiles from the chair -- inside the 1.4 arrival gate -- while the FOLLOW program was
-- driving, so the sit branch never saw it and `best` stayed at 2.55. The budget therefore
-- wrote off a destination she had physically reached minutes earlier. The rest window is
-- what makes that survivable rather than permanent: one lost pastime, then a retry with a
-- fresh budget. Do not raise the cap to compensate -- that trades a cheap, self-healing,
-- traced error for the 212-second freeze this exists to stop.
--
-- THIS CAP GOVERNS BUG-029's PASTIME SITE TOO. Its own RELAX_TRY_CAP was 60 and therefore
-- tied with the watchdog at the one site where the give-up has never been observed to fire
-- (`grep -c "gave up"` is 0 in every trace we hold). The constant was deleted rather than
-- lowered, so there is one number here and no second one to forget. Changing it changes
-- BOTH sites -- that is the point, not a side effect.
local MOVE_TRY_CAP     = 40   -- evaluations without progress before a destination is off
-- FIX(BUG-048): THE UNIT HERE IS CALLS ON THIS KEY, NOT EVALUATIONS, AND THE OLD COMMENT
-- ("evaluations it stays off before she may try again (~60s)") WAS TRUE AT ONE SITE ONLY.
-- It holds at `home`, where a give-up leaves her standing still and calling moveStalled for
-- that key every single evaluation. At `sit`, `guard` and `animplay` the give-up moves her
-- OFF the key -- relaxGoal is cleared, guardTarget flips, finish() is called -- so the
-- counter advances AT MOST ONCE PER RE-COMMITMENT. A sit re-commitment needs a
-- fall-through, a roll of 7-9 (:1610) and one more evaluation, and between rolls she may
-- spend a 600-tick pastime. **So the seat is written off for hundreds to thousands of
-- evaluations, not sixty seconds.** This is the same class of error as the "40 evaluations
-- ~= 13-15 seconds" claim struck above: counting a per-key counter in wall-clock units.
local MOVE_REST        = 180  -- further CALLS ON THIS KEY before the entry is dropped
local MOVE_PROGRESS    = 0.1  -- tiles; float-noise margin, same as RELAX_PROGRESS
local MOVE_STALL_SLOTS = 8    -- destinations remembered at once, on the persisted brain

-- FIX(BUG-048): HOW FAR SHE MUST BE FROM `run` BEFORE THIS COUNTS AS A NEW APPROACH.
--
-- The entry is keyed per DESTINATION and deliberately outlives any single approach (see
-- above -- that is load-bearing, do not undo it), so the approach boundary has to be
-- RECOVERED rather than stored. This is the recovery: if she is now substantially farther
-- than the closest this site has seen, the previous excursion is over and `run` re-seeds
-- from where she actually is.
--
-- 2 TILES, AND THE ARITHMETIC IS BOUNDED FROM BOTH ENDS BY MEASURED DATA:
--   * TOO SMALL and she re-seeds by drifting, the budget weakens toward never firing, and
--     BUG-035's 212-second livelock returns SILENTLY. 2 is five times the ~0.4 tiles she
--     covers per evaluation while walking and twenty times MOVE_PROGRESS's noise margin,
--     so neither jitter nor a normal step can trip it.
--   * TOO LARGE and an approach beginning close inherits the previous approach's minimum.
--     The two measured false give-ups began 4.76 and 7.62 tiles out (logs/bug026-test.txt
--     000322.8, logs/bug026-v2-test.txt 000104.7) against a frozen minimum of ~1.45, so
--     anything below ~3.3 re-seeds both with margin.
-- RESIDUAL, BENIGN AND DELIBERATE: an approach that begins INSIDE the radius keeps the old
-- minimum, so `tries` climbs from its first evaluation. At `sit` that means starting within
-- ~3.4 tiles and needing 40 evaluations to close ~2 tiles, which at 0.4 tiles/evaluation
-- she does in about five. Only a genuinely stalled close approach can reach the cap, which
-- is the correct outcome.
local MOVE_RESTART     = 2    -- tiles farther than `run` = a new approach, re-seed it

-- Returns TRUE when the caller must NOT issue this Move. Every caller pairs that with
-- clearing whatever goal pointed at the destination, so she does something else instead
-- of re-entering the same branch on the next evaluation.
local function moveStalled(bandit, brain, site, tx, ty, tz)
    if not brain then return false end
    local ok, blocked = pcall(function()
        if math.abs((tz or 0) - bandit:getZ()) >= 1 then return false end
        -- ALPHANUMERIC AND UNDERSCORE ONLY, DELIBERATELY. `brain.spots` (Base.lua:652)
        -- is the precedent for a string-keyed table living on the persisted brain, and
        -- its keys are plain identifiers -- "chair", "tv". Nothing read this session
        -- establishes that punctuation survives modData serialisation, so none is used
        -- here. The readable form goes in the trace text, where it costs nothing.
        local key = tostring(site) .. "_" .. tostring(math.floor(tx))
                    .. "_" .. tostring(math.floor(ty)) .. "_" .. tostring(math.floor(tz or 0))
        local m = brain.moveStall
        if not m then m = {} ; brain.moveStall = m end
        local e = m[key]
        if not e then
            local n, lowK, lowT = 0, nil, nil
            for k2, e2 in pairs(m) do
                n = n + 1
                if lowT == nil or (e2.tries or 0) < lowT then lowK, lowT = k2, e2.tries or 0 end
            end
            if n >= MOVE_STALL_SLOTS and lowK then m[lowK] = nil end
            -- FIX(BUG-042a): the destination is stored so OTHER sites can refresh this
            -- entry's `best` while she is nowhere near it. See the loop below.
            e = { best = 1e9, tries = 0, x = tx, y = ty, z = tz or 0 }
            m[key] = e
        end
        local d = BanditUtils.DistTo(bandit:getX(), bandit:getY(), tx, ty)

        -- FIX(BUG-048): `best` NO LONGER GATES ANYTHING. It is the cross-site diagnostic
        -- and nothing reads it but the trace, which is what BUG-042a always described it
        -- as. No margin, so this matches the refresh loop below exactly.
        if d < (e.best or 1e9) then e.best = d end

        -- FIX(BUG-048): THE RESET TEST READS `run` -- A PER-APPROACH MINIMUM THIS SITE
        -- OWNS -- BECAUSE READING A PERSISTED "BEST EVER" MADE THE BUDGET BREAK ON SUCCESS.
        --
        -- THE DEFECT, WHICH IS ARITHMETIC AND NOT A RACE. Every caller gates this function
        -- above a distance: sit at `d > 1.4` (:1300), guard at `> 0.6` (:1695), animplay at
        -- `> 0.8` (:2152). So the smallest value the owning site can ever hand in is just
        -- above its own gate. When an approach ARRIVES the site stops being called at the
        -- gate and the minimum freezes at roughly `gate + epsilon`; the old reset then
        -- needed `d < gate + epsilon - MOVE_PROGRESS`, which is BELOW THE GATE and
        -- therefore a value this site is structurally incapable of presenting again.
        --     arrived once  -> froze ~1.45 -> needed d < 1.35 -> gate is d > 1.4 -> EMPTY
        --     gave up once  -> froze  2.55 -> needed d < 2.45 -> (1.4, 2.45) reachable
        -- `tries` could then only climb, so the budget stopped being a stall detector and
        -- became an unconditional MOVE_TRY_CAP-evaluation deadline on that destination.
        -- That is why the project's one honest give-up is at a chair she never reached
        -- (closest=2.55) and both false ones are at a chair she sat in.
        --
        -- BUG-042a MADE IT TOTAL RATHER THAN CREATING IT. The refresh loop below drove the
        -- minimum to EXACTLY 0.00 -- `pinSit` parks her on the tile centre (Actions.lua:
        -- 196-198), `stepOffFurniture` does not move her off a chair because a chair square
        -- is FREE (BUG-021), and the next site to evaluate refreshes the seat's own entry.
        -- Measured end to end in logs/bug026-test.txt: pinned at 410.50,9751.50 from
        -- 000110.6, still on the tile and walking away at 000141.8, and at 000322.8 walking
        -- BACK with `path=5.1` -- a live route to the exact destination -- written off 0.6s
        -- later. It also crosses sessions: brain.moveStall is persisted, and the v2 run gave
        -- up on the same seat at 000104.7 without her ever sitting in it.
        --
        -- WHY NOT SIMPLY KEEP THE OWNING SITE'S MINIMUM OUT OF THE REFRESH LOOP: that is a
        -- one-line change and it is NOT A FIX. It restores the pre-042a number (~1.45
        -- instead of 0.00) and, per the arithmetic above, ~1.45 is already inside the
        -- gate's dead band. It repairs the damage of one week and leaves the older defect
        -- in place while making the trace look healthier. Do not take it because it is
        -- smaller.
        --
        -- NPCRELAX'S PASTIME APPROACH IS THE WORKING CONTROL FOR ALL OF THIS: same budget
        -- shape, same constant, no defect -- because it clears `relaxDoBest` when a new
        -- spot is picked (:1598-1601). It has an approach boundary and this did not.
        -- ==================================================================================
        -- FIX(BUG-048b): `run` AND `tries` ARE ONE MEASUREMENT, NOT TWO FIELDS.
        --
        -- `run` is a BOUND -- the closest this site has seen on the current approach.
        -- `tries` is A COUNT OF FAILURES TO BEAT THAT BOUND.
        -- A count is meaningless against a bound it was not counted against, so:
        --
        --      **ANY PATH THAT MOVES ONE OF THESE TWO MUST MOVE THE OTHER.**
        --
        -- Both branches below obey that. If you add a third writer of `run` anywhere in
        -- this function, it obeys it too, or it reintroduces one of the two bugs below.
        --
        -- THIS EXACT MISTAKE HAS NOW BEEN MADE TWICE IN THIS FUNCTION, IN OPPOSITE
        -- DIRECTIONS, WHICH IS WHY THE RULE IS WRITTEN HERE AND NOT ONLY IN INVARIANTS.md:
        --   * BUG-048  -- a BOUND that outlived its approach while the COUNT kept climbing.
        --                 `best` was persisted per destination and read as the reset test,
        --                 so once she had ARRIVED once the reset became unsatisfiable and
        --                 the budget degraded into a fixed deadline.
        --   * BUG-048b -- a COUNT that outlived its bound. The re-seed below used to read
        --                 `then e.run = d end` and leave `tries` alone, so it declared a
        --                 new approach and kept the previous approach's verdict. After a
        --                 re-seed `run == d`, so the entry holds ZERO evidence of failure
        --                 to improve -- and a give-up could still fire in the same
        --                 evaluation. Measured 2026-08-24: three samples closing 9.073 ->
        --                 8.288 -> 7.121 tiles, and the third printed
        --                 `gave up sit -> 410,9751,1 ... run=7.12`. A new approach that
        --                 inherited 39 tries survived exactly one more evaluation.
        -- ==================================================================================
        if e.run == nil or d > e.run + MOVE_RESTART then
            e.run = d
            -- THE `<= MOVE_TRY_CAP` GATE IS LOAD-BEARING. DO NOT SIMPLIFY IT AWAY.
            --
            -- It means: a new approach gets a new counter, UNLESS she is already written
            -- off, in which case the write-off keeps counting down and only `run` moves.
            --
            -- WHAT REMOVING IT WOULD DO -- silently, with no error and no trace signature:
            -- the give-up at `sit`, `guard` and `animplay` RELOCATES HER (relaxGoal is
            -- cleared, guardTarget flips, finish() is called), so within a few evaluations
            -- she is more than MOVE_RESTART tiles away and the re-seed fires. Zeroing
            -- `tries` there would LIFT THE WRITE-OFF almost immediately, every time. The
            -- rest window would become inoperative at exactly the three sites whose give-up
            -- moves her, and an unreachable seat would cost a fresh 40-evaluation budget on
            -- every roll -- the pre-BUG-029 waste, restored, while the code still looks as
            -- though it rests for 180.
            --
            -- IT IS ALSO WHAT KEEPS THE "REST MAY END EARLY" DECISION BELOW HONEST.
            -- **IMPROVEMENT FALSIFIES A VERDICT; RETREAT DOES NOT.** A companion who
            -- demonstrably closes on a destination has evidence against the write-off and
            -- may lift it (the second branch, deliberately). A companion who merely walked
            -- AWAY has produced no evidence about reachability at all.
            if (e.tries or 0) <= MOVE_TRY_CAP then
                e.tries, e.offBest, e.offRun = 0, nil, nil
            end
        end
        if d < e.run - MOVE_PROGRESS then
            -- moves BOTH, per the rule above: a new bound and a fresh count against it
            e.run, e.tries = d, 0
            -- the numbers a give-up reported belong to a decision that has been reversed
            e.offBest, e.offRun = nil, nil
        end
        e.tries = (e.tries or 0) + 1

        -- FIX(BUG-042a): REFRESH EVERY OTHER LIVE ENTRY'S `best`. DIAGNOSTIC ONLY.
        --
        -- `best` used to update on ONE line -- the one above -- which runs only when the
        -- OWNING site evaluates. So it never meant "closest she has been"; it meant
        -- "closest she has been AT A MOMENT THIS SITE HAPPENED TO RUN". Measured in
        -- logs/task016-test.txt: the budget wrote off the chair at 332,9691 with
        -- `closest=2.55`, but at 000530.3 she stood 1.19 tiles from it -- INSIDE the 1.4
        -- arrival gate -- and the sit branch was not running, so nothing recorded it.
        -- `closest=` is the column BUGS.md tells a reader to sort by to separate false
        -- give-ups (below ~2.0) from correct ones (above ~5), so the one diagnostic meant
        -- to catch this class of error was the thing concealing it.
        --
        -- `tries` IS DELIBERATELY NOT RESET HERE, AND THAT IS THE WHOLE DESIGN.
        -- The line above resets it because getting closer WHILE PURSUING this destination
        -- is evidence the route works. Resetting it from here would mean any incidental
        -- wander past the target clears the budget -- she follows the player past the shed,
        -- `tries` goes to 0, and BUG-035's 212-second livelock comes back on a trigger
        -- nobody can reproduce on demand. That is the exact nondeterminism the cap change
        -- removed.
        --
        -- THIS COMMENT USED TO CONTINUE: "Lowering `best` alone can only make a give-up
        -- EARLIER and more honest, never later, so this cannot mask a stall." THAT IS
        -- STRUCK -- IT WAS WRONG, AND IT IS BUG-048. `best` was not only printed; it was
        -- also the RESET CONDITION, so lowering it from here did not merely re-time a
        -- give-up, it made the reset unsatisfiable. It cannot mask a stall; it can INVENT
        -- one, and it invented both give-ups recorded since this loop shipped. The loop is
        -- now genuinely diagnostic-only because the reset reads `run` above, and `run` is
        -- written by the owning site alone. **DO NOT ADD `run` TO THIS LOOP.** Refreshing
        -- it from here would restore BUG-048 exactly and reintroduce the nondeterminism the
        -- paragraph above refuses.
        --
        -- WHAT IT DOES NOT FIX, STATED PLAINLY BECAUSE IT WOULD OTHERWISE READ AS CLOSED:
        -- this runs inside moveStalled, so it needs SOME site to be evaluating. All five
        -- sites live in NPCRelax (home/sit/wander), NPCGuard and NPCAnimPlay -- NPCCompanion
        -- has NONE. During the 108 `prog=NPCCompanion` samples of the measured run this
        -- loop would not have run at all, so IT WOULD NOT HAVE CAUGHT THE 1.19 READING.
        -- It fixes the cross-SITE case (a stale `sit` entry refreshed while `wander`
        -- evaluates), not the cross-PROGRAM one. Closing that needs a per-dispatch hook,
        -- and the only one available is in Guardian.lua -- a module boundary this is not
        -- worth crossing for a diagnostic. Do not record BUG-042 as fixed on this change.
        --
        -- Z GUARD, same reasoning as the one above: DistTo is 2D, so a destination on
        -- another floor would record a meaningless "closest". `e2.x` also guards brains
        -- persisted before this change, whose entries have no coordinates.
        local bx, by, bz = bandit:getX(), bandit:getY(), bandit:getZ()
        for k2, e2 in pairs(m) do
            if k2 ~= key and e2.x and math.abs((e2.z or 0) - bz) < 1 then
                local d2 = BanditUtils.DistTo(bx, by, e2.x, e2.y)
                if d2 < (e2.best or 1e9) then e2.best = d2 end
            end
        end
        -- FIX(BUG-048): THE REST WINDOW MAY NOW END EARLY, AND THAT IS A DECISION.
        -- The reset above runs BEFORE this test, so getting genuinely closer while she is
        -- written off sets `tries = 0` and lifts the write-off there and then. This was
        -- unreachable before only because the minimum was poisoned to 0.00; it becomes
        -- reachable now. It is WANTED: a companion who demonstrably closes on a destination
        -- has falsified the verdict against it, and the budget noticing it was wrong is the
        -- right response. NOT AN OVERSIGHT -- do not add a guard to hold the rest open.
        if e.tries > MOVE_TRY_CAP + MOVE_REST then
            m[key] = nil        -- rest over: fresh budget, and the watchdog gets its Move back
            return false
        end
        if e.tries > MOVE_TRY_CAP then
            -- FIX(BUG-048): BOTH NUMBERS ARE CAPTURED ONCE, AT THE WRITE-OFF, AND THE LINE
            -- REPORTS THE CAPTURES -- NOT THE LIVE FIELDS.
            --
            -- The line has to self-classify, so it needs `run` (the value the decision was
            -- actually made on) beside `closest` (all-time, cross-site). But BOTH live
            -- fields keep moving while she is written off -- `run` from the owning line
            -- above, `best` from the refresh loop -- and Tr.Change is CHANGE-ONLY
            -- (Trace.lua:218-234), so live values would defeat its dedup and turn one line
            -- into one per evaluation. That is the flood CHANGE-ONLY exists to prevent.
            -- The old comment here claimed "`best` is frozen while she is written off";
            -- that stopped being true when the refresh loop landed. It is frozen now
            -- because this freezes it.
            --
            -- READING THE LINE:
            --   closest=0.00 run=6.20  CORRECT. She has stood there before, but on THIS
            --                          approach never got closer than 6.2 tiles.
            --   run= BELOW the calling site's own gate (1.4 sit / 0.6 guard / 0.8 animplay)
            --                          IMPOSSIBLE BY CONSTRUCTION -- only the owning site
            --                          writes `run` and it is gated above. If such a line
            --                          ever appears, BUG-048's fix is wrong. Free
            --                          falsifier, no new code, applies to every trace.
            --
            -- "resting N evaluations" IS NOW "re-commitments": see MOVE_REST's declaration.
            -- AND NOTE Tr.Change COLLAPSES REPEATS WITHOUT FLUSHING THE COUNT UNTIL THE
            -- TEXT CHANGES, so ONE STALL LINE IS A LOWER BOUND OF ONE GIVE-UP, NOT A COUNT
            -- OF ONE. Do not read a single line as a single refusal.
            if e.offBest == nil then e.offBest, e.offRun = e.best, e.run end
            local T = BanditsNPC.Trace
            if T and T.Change then
                T.Change(bandit, "STALL", key, "gave up " .. tostring(site) .. " -> "
                         .. tostring(math.floor(tx)) .. "," .. tostring(math.floor(ty))
                         .. "," .. tostring(math.floor(tz or 0))
                         .. " -- no progress in " .. MOVE_TRY_CAP .. " tries, closest="
                         .. string.format("%.2f", e.offBest or -1)
                         .. " run=" .. string.format("%.2f", e.offRun or -1)
                         .. "; resting " .. MOVE_REST .. " re-commitments")
            end
            return true
        end
        return false
    end)
    return ok and blocked == true
end

ZombiePrograms.NPCRelax = {}
ZombiePrograms.NPCRelax.Prepare = function(bandit)
    Bandit.ForceStationary(bandit, false)
    return {status = true, next = "Main", tasks = {}}
end
ZombiePrograms.NPCRelax.Main = function(bandit)
    local tasks = {}
    if BanditsNPC.Needs then BanditsNPC.Needs.Update(bandit) end
    if BanditsNPC.Routine and BanditsNPC.Routine.MaybeStart(bandit) then
        return {status = true, next = "Main", tasks = tasks}
    end
    if BanditsNPC.Schedule and BanditsNPC.Schedule.Apply(bandit) then
        return {status = true, next = "Main", tasks = tasks}
    end
    local brain = BanditBrain.Get(bandit)
    if not brain then return {status = true, next = "Main", tasks = tasks} end

    -- ANCHOR AND BOUNDARY.
    --
    -- If the player has drawn a base AREA, that area IS the boundary: she may go
    -- anywhere inside it and nowhere outside it, whatever shape it is. That beats the
    -- old circle-around-a-tile in the case that actually matters -- a house is not a
    -- circle, and a radius that covers the far bedroom also covers the street.
    --
    -- The hand-picked relaxPos still wins as the anchor (it is where she gravitates,
    -- and the player chose it), but with an area drawn she is no longer confined to a
    -- 9-tile ring around it. With no area, everything below behaves exactly as before.
    local site = BanditsNPC.Base and BanditsNPC.Base.SiteFor(bandit)
    local baseZone = site and BanditsNPC.Base.Zone(site)

    local anchor = brain.relaxPos
    if not anchor and baseZone then
        -- No pick and a base exists: settle somewhere sensible inside it, preferring
        -- indoors. This is what lets "relax at base" be an order with no cursor.
        anchor = BanditsNPC.Base.Anchor(site)
        if anchor then brain.relaxPos = anchor end
    end
    if not anchor then
        anchor = { x = math.floor(bandit:getX()), y = math.floor(bandit:getY()), z = math.floor(bandit:getZ()) }
        brain.relaxPos = anchor
    end

    local ax, ay, az = anchor.x + 0.5, anchor.y + 0.5, anchor.z or 0

    -- Only treat the drawn area as her home if her anchor is actually in it -- a
    -- companion relaxing at an outpost must not be dragged to the main base.
    local Zc = BanditsNPC.Zones
    if baseZone and not (Zc and Zc.Contains(baseZone, ax, ay, az)) then baseZone = nil end
    -- A NIL z MEANS "DON'T KNOW", NOT "GROUND FLOOR". Threat locations from
    -- BanditUtils may not carry one, and treating that as z=0 would silently switch
    -- base defence off for anyone relaxing upstairs.
    local function atHome(x, y, z)
        if z ~= nil and baseZone == nil and math.abs(z - az) >= 1 then return false end
        if baseZone then return Zc.Contains(baseZone, x, y, z) end
        return BanditUtils.DistTo(x, y, ax, ay) <= RELAX_RADIUS + 4
    end

    -- BASE DEFENSE: relaxing companions are off duty, not oblivious -- engage any
    -- threat inside the relax area (or right on top of her), same close-the-distance
    -- logic as NPCGuard (the engine's combat loop only swings within weapon range).
    if BanditsNPC.GetStance(bandit) ~= "passive" then
        local cz = BanditUtils.GetClosestZombieLocation(bandit)
        local cb = BanditUtils.GetClosestEnemyBanditLocation(bandit)
        local ce = cz
        if cb and cz and cb.dist < cz.dist then ce = cb end
        if ce and ce.x and ce.dist and
           (ce.dist < 6 or atHome(ce.x, ce.y, ce.z)) then
            stepOffFurniture(bandit)
            brain.relaxGoal = nil
            if ce.dist > 1.6 then
                Bandit.ForceStationary(bandit, false)
                table.insert(tasks, BanditUtils.GetMoveTask(0, ce.x, ce.y, ce.z, "WalkAim", ce.dist))
            else
                table.insert(tasks, {action = "FaceLocation", x = ce.x, y = ce.y, time = 60})
            end
            return {status = true, next = "Main", tasks = tasks}
        end
    end

    -- strayed out (combat chase, door-pass, teleport): come home first
    if not atHome(bandit:getX(), bandit:getY(), bandit:getZ()) then
        stepOffFurniture(bandit)
        -- FIX(TASK-016): GIVING UP HERE MEANS STANDING STILL WHERE SHE IS, and that is
        -- accepted rather than ideal -- there is no other goal to fall back to, because
        -- every branch below this one assumes she is at home. It is strictly better than
        -- shoving at a wall for 96 seconds, and unlike shoving it SAYS SO: the STALL line
        -- names the anchor she could not reach, so "she just stands there in the street"
        -- is diagnosable instead of mysterious. The rest window is what keeps this from
        -- stranding her -- the walk home resumes, and with it the watchdog teleport that
        -- is the actual remedy for a companion who cannot path back.
        if moveStalled(bandit, brain, "home", ax, ay, az) then
            Bandit.ForceStationary(bandit, true)
            table.insert(tasks, {action = "Time", anim = "ShiftWeight", time = 200})
            return {status = true, next = "Main", tasks = tasks}
        end
        Bandit.ForceStationary(bandit, false)
        local adist = BanditUtils.DistTo(bandit:getX(), bandit:getY(), ax, ay)
        table.insert(tasks, BanditUtils.GetMoveTask(0, ax, ay, az, "Walk", adist))
        return {status = true, next = "Main", tasks = tasks}
    end

    -- A chair to sit in: hers if the player assigned one, otherwise any chair the
    -- base scan found. Only counts if it is somewhere she is allowed to be.
    local chair = BanditsNPC.Base and BanditsNPC.Base.Resolve(brain, "chair", bandit)
    local chairOk = chair and atHome(chair.x + 0.5, chair.y + 0.5, chair.z or 0)

    -- committed to sitting: keep walking to the chair, then hold the seat
    if brain.relaxGoal == "sit" and chairOk then
        local csq = getCell():getGridSquare(chair.x, chair.y, chair.z or 0)
        if csq then
            local d = BanditUtils.DistTo(bandit:getX(), bandit:getY(), chair.x + 0.5, chair.y + 0.5)
            if d > 1.4 then
                -- FIX(TASK-016): THIS IS THE MEASURED ONE -- 126 evaluations, 212 seconds,
                -- one seat, no budget. Clearing relaxGoal drops her back to the roll below,
                -- so she wanders or finds a pastime instead of leaning on a chair she
                -- cannot reach; the seat stays written off meanwhile, so a re-roll onto
                -- "sit" costs one evaluation rather than another 60. Returning with an
                -- empty `tasks` is how this function already abandons a decision (the
                -- pastime pick does exactly that) -- she idles one evaluation, then re-enters.
                if moveStalled(bandit, brain, "sit", chair.x + 0.5, chair.y + 0.5, chair.z or 0) then
                    brain.relaxGoal = nil
                    return {status = true, next = "Main", tasks = tasks}
                end
                Bandit.ForceStationary(bandit, false)
                table.insert(tasks, BanditUtils.GetMoveTask(0, chair.x + 0.5, chair.y + 0.5, chair.z or 0, "Walk", d))
                return {status = true, next = "Main", tasks = tasks}
            end
            Bandit.ForceStationary(bandit, true)
            if ZombRand(4) == 0 then brain.relaxGoal = nil end   -- eventually gets up
            -- BNSit: real chair-sit pose + seat-edge offsets (our action; the stock
            -- "Sit" bump anim is a GROUND sit -- she phased through the seat)
            table.insert(tasks, {action = "BNSit", anim = "BNSitChair", x = chair.x, y = chair.y,
                                 z = chair.z or 0,
                                 facing = chair.dir or seatFacing(chair.x, chair.y, chair.z) or "S",
                                 time = 300})
            return {status = true, next = "Main", tasks = tasks}
        end
    end
    -- ===== COMMITTED TO A PASTIME =====
    -- Walk to it, then stand there doing it with a line over her head until the timer
    -- runs out. Held on the brain so she does not change her mind every tick, and
    -- re-resolved from coordinates rather than an object reference so it survives the
    -- furniture being moved or destroyed.
    if brain.relaxGoal == "do" and brain.relaxDo and brain.relaxDoSpot then
        local d0 = PASTIME_BY_ID[brain.relaxDo]
        local s = brain.relaxDoSpot
        -- atHome, not `not baseZone or Contains` -- a goal committed to before the fix
        -- above (or a car that has since been DRIVEN AWAY) is re-validated against the
        -- same boundary every tick, so a stale out-of-range spot is dropped rather than
        -- chased forever. Without this the saved brain.relaxDo keeps the old behaviour
        -- alive across the update for anyone already looping.
        -- FIX(BUG-021): WALK TO A TILE SHE CAN STAND ON, NOT ONTO THE FURNITURE.
        --
        -- Six of the nine pastime spots are on BLOCKED squares -- measured, not
        -- assumed: tv, bathroom, food, bed, stove and washer all returned
        -- isFree(false) = false; only chair, bookshelf and window were free.
        -- This branch used to Move straight onto `s`, so those six issued a Move
        -- to a square with no route to it. That is the BUG-017 livelock
        -- (BUG-018 for why an unreachable Move never reports failure), except
        -- reached through the DEFAULT program with no player order involved.
        --
        -- THE d > 1.5 GATE DOES NOT SAVE HER, and it looks as though it should.
        -- 1.5 tiles from the tile CENTRE is outside the tile, so geometrically she
        -- could stop on a neighbour and be done. She never gets the chance: the
        -- pathfinder fails OUTRIGHT rather than approaching. Two traces, zero
        -- movement in both -- logs/bug015-attempt1-trace.txt froze 4.38 tiles out
        -- for 1311 ticks, logs/guard-livelock-trace.txt 3.34 tiles out for 2622.
        -- She does not walk as close as she can and stop. She does not move.
        --
        -- THE WATCHDOG WAS ALREADY DOING THIS WORK, 20-44 SECONDS LATE.
        -- Guardian.lua:1057-1062 teleports her to floor(moveTask.x/y) and, when
        -- that square is unusable, falls back to AdjacentFreeTileFinder.Find --
        -- the same call, on the same square, to the same end. So the old symptom
        -- was "stands still for half a minute, then blinks over to the TV and
        -- watches it", which is why this never read as a hard freeze in reports.
        -- DO NOT "SIMPLIFY" THIS AWAY ON THE GROUNDS THAT THE WATCHDOG HANDLES
        -- IT: the watchdog is the last line of defence, its budget is unreliable
        -- in exactly this failure mode (BUG-023), and its gentle remedy is gated
        -- out so the teleport is the FIRST thing the player sees.
        --
        -- WHY THIS IS GUARDED AND NPCWork:942 IS NOT. That line reads
        --     local stand = AdjacentFreeTileFinder.Find(ssq, bandit) or ssq
        -- unconditionally, which is right there because a workstation is ALWAYS
        -- occupied. Find never returns the square you hand it -- it builds its
        -- choices from getAdjacentSquare only (AdjacentFreeTileFinder.lua:142-153;
        -- only FindEdge has preferSameSquare) -- so calling it unconditionally
        -- here would push chair, bookshelf and window one tile short of somewhere
        -- she reaches perfectly well today. Those three must not move.
        --
        -- NO `or ssq` FALLBACK, DELIBERATELY. If Find comes back nil the spot has
        -- no free neighbour at all and nothing we pick can be walked to; `or ssq`
        -- would send her at the blocked tile and livelock exactly as before,
        -- preserving the bug for the worst cases. Instead reachable stays false,
        -- the branch is skipped, and control falls to the clear at :712 -- the
        -- same drop the atHome re-validation above already relies on. She loses
        -- one pastime roll and picks another. An unreachable goal is abandoned,
        -- not chased.
        --
        -- `s` ITSELF IS UNCHANGED. BNSit below pins her ONTO the furniture by
        -- design (see INVARIANTS on pinSit), and faceLocationF and d0.effect both
        -- want the spot, not the standing tile. Only the walk destination moves.
        local wx, wy, wz, reachable = s.x + 0.5, s.y + 0.5, s.z or 0, true
        local tsq                      -- FIX(BUG-029): the SQUARE she must reach, so
        if d0 then                     -- arrival can be asked of the grid, not a ruler
            local ssq = getCell():getGridSquare(s.x, s.y, s.z or 0)
            tsq = ssq
            if ssq and BanditsNPC.Base and BanditsNPC.Base.IsStandable
               and not BanditsNPC.Base.IsStandable(ssq) then
                local stand
                pcall(function() stand = AdjacentFreeTileFinder.Find(ssq, bandit) end)
                if stand then
                    wx, wy, wz, tsq = stand:getX() + 0.5, stand:getY() + 0.5, stand:getZ(), stand
                else
                    reachable = false
                    -- FIX(BUG-029): this abandonment already existed and was SILENT.
                    relaxGiveUp(bandit, brain, s, "no free tile beside it")
                end
            end
        end
        -- FIX(BUG-029): count the attempts BEFORE the walk, so the arrival body below
        -- never has to know that giving up is possible -- it just does not run.
        local go = d0 and reachable and atHome(s.x + 0.5, s.y + 0.5, s.z or 0)
        local arrived = go and relaxArrived(bandit, tsq, wz)
        local d = 0
        if go and not arrived then
            d = BanditUtils.DistTo(bandit:getX(), bandit:getY(), wx, wy)
            -- FIX(BUG-029): a new closest-ever distance means she is still getting there,
            -- so the stall budget starts again. Only failure to improve costs her.
            if d < (brain.relaxDoBest or 1e9) - RELAX_PROGRESS then
                brain.relaxDoBest, brain.relaxDoTries = d, 0
            end
            brain.relaxDoTries = (brain.relaxDoTries or 0) + 1
            if brain.relaxDoTries > MOVE_TRY_CAP then
                relaxGiveUp(bandit, brain, s, "no route in " .. MOVE_TRY_CAP
                            .. " tries, stand tile " .. tostring(math.floor(wx))
                            .. "," .. tostring(math.floor(wy)) .. "," .. tostring(wz or 0))
                go = false
            end
        end
        if go then
            if not arrived then
                Bandit.ForceStationary(bandit, false)
                table.insert(tasks, BanditUtils.GetMoveTask(0, wx, wy, wz, "Walk", d))
                return {status = true, next = "Main", tasks = tasks}
            end
            brain.relaxDoTries, brain.relaxDoBest = nil, nil
            Bandit.ForceStationary(bandit, true)
            if d0.face then
                pcall(function() bandit:faceLocationF(s.x + 0.5, s.y + 0.5) end)
            end
            if not brain.relaxDoRan then
                brain.relaxDoRan = true
                -- FIX(BUG-026): the framework owns the bookkeeping. `effect` reports
                -- whether it really changed the world; only then, and only if the entry
                -- declares an `undo`, is a record written. See the contract over relaxUndo.
                if d0.effect then
                    local sq = getCell():getGridSquare(s.x, s.y, s.z or 0)
                    if sq and d0.effect(bandit, sq) and d0.undo then
                        brain.npcUndo = { id = d0.id, x = s.x, y = s.y, z = s.z or 0 }
                    end
                end
                pcall(function() bandit:addLineChatElement(d0.say(), 0.8, 0.9, 1.0) end)
            end
            -- Boredom is what "something to do" is FOR, so doing it has to actually
            -- help -- otherwise she is only miming a hobby.
            if BanditsNPC.Needs and ZombRand(6) == 0 then
                BanditsNPC.Needs.Reduce(bandit, "boredom", 2, true)
            end
            -- FIX(BUG-026): THE ROLL NOW ACTUALLY STOPS HER, AND IT TEARS DOWN.
            --
            -- It used to clear the pastime state and then FALL THROUGH to the two
            -- table.insert lines below, issuing one more task for the pastime it had just
            -- abandoned -- `d0` is a local that was already resolved, so nothing below
            -- noticed the state was gone. "The roll fired" and "she stopped" were one
            -- ANIMATION apart, measured at 2-6 seconds, which would have put the teardown
            -- one animation late as well.
            --
            -- Returning here makes them the same moment. The empty task list is this
            -- file's own idle-return shape (see :287, :1029, :1459): the queue is empty,
            -- so NPCRelax.Main runs again on the next tick with `relaxDo` nil, falls
            -- through to the block below, and does stepOffFurniture plus a fresh pick.
            -- `tasks` is empty at this point in the arrived branch; it is returned rather
            -- than a literal {} so anything an earlier stage queued still travels.
            if ZombRand(8) == 0 then
                relaxUndo(bandit, brain)
                brain.relaxGoal, brain.relaxDo, brain.relaxDoSpot, brain.relaxDoRan,
                    brain.relaxDoTries, brain.relaxDoBest = nil, nil, nil, nil, nil, nil
                return {status = true, next = "Main", tasks = tasks}
            end
            -- A `sit` pastime is PINNED onto its furniture (BNSit sets position, seat
            -- height and facing, and nudges on couches); everything else is played where
            -- she stands by a plain Time task, which moves nothing. Same task shape the
            -- chair branch above uses, so there is one implementation of sitting.
            if d0.sit then
                table.insert(tasks, {action = "BNSit", anim = "BNSitChair",
                                     x = s.x, y = s.y, z = s.z or 0,
                                     facing = s.dir or seatFacing(s.x, s.y, s.z) or "S",
                                     time = d0.time or 600})
            else
                table.insert(tasks, {action = "Time", anim = d0.anim or "ShiftWeight",
                                     time = d0.time or 600})
            end
            return {status = true, next = "Main", tasks = tasks}
        end
    end
    brain.relaxGoal, brain.relaxDo, brain.relaxDoSpot, brain.relaxDoRan,
        brain.relaxDoTries, brain.relaxDoBest = nil, nil, nil, nil, nil, nil
    -- FIX(BUG-026): the second abandonment path. Every route out of a pastime that is not
    -- the roll above arrives here -- interrupted, spot gone, list empty, or simply never
    -- started -- so this is where the undo runs for all of them. It is also the RETRY site
    -- for a deferred undo: she reaches this on every evaluation while she has no pastime,
    -- so a record held back because somebody else was still watching is re-tested until it
    -- can be honoured. The nil-record early return keeps that free in the common case.
    -- stepOffFurniture on the next line is the existing precedent for per-pastime teardown
    -- and this sits beside it. Both are no-ops when there is nothing to undo.
    relaxUndo(bandit, brain)
    stepOffFurniture(bandit)   -- done sitting: off the couch before anything else

    -- TWO OF THEM IN A ROOM TALK. Cheap, occasional, and only ever a line of text --
    -- but it is the difference between a base with people in it and a base with
    -- furniture in it.
    if ZombRand(30) == 0 then
        local other = chatPartner(bandit)
        if other then
            local i = 1 + ZombRand(#SMALL_TALK)
            pcall(function()
                bandit:faceLocationF(other:getX(), other:getY())
                bandit:addLineChatElement(SMALL_TALK[i](), 0.85, 0.95, 0.85)
            end)
            Bandit.ForceStationary(bandit, true)
            table.insert(tasks, {action = "Time", anim = "ShiftWeight", time = 200})
            return {status = true, next = "Main", tasks = tasks}
        end
    end

    -- pick something to do.
    --
    -- FIX(TASK-015): THREE DIFFERENT FAILURES FALL THROUGH TO THE SAME AMBIENT IDLE and
    -- until now they were indistinguishable from each other AND from a companion who is
    -- simply between activities -- a bad wander tile, an empty pastime list, and no chair
    -- assigned. All three look identical in game and in the trace: she stands there. One
    -- of them is normal and two are bugs, and there was no way to tell which you were
    -- looking at. `idleWhy` is set at each fall-through and printed once below.
    local idleWhy = "no branch reached"
    local roll = ZombRand(10)
    if roll < 2 then
        -- Wander. With a base area drawn, anywhere inside it is fair game -- that is
        -- what makes her look like she LIVES there rather than orbiting one tile.
        -- Without one, the old behaviour: a random tile near the anchor, and if the
        -- anchor is indoors then indoor tiles only, so she does not drift into the
        -- street.
        -- wz IS TAKEN FROM THE CHOSEN SQUARE, NOT FROM THE ANCHOR (v0.75.8, audit
        -- Cluster F). This used to read only wx/wy off the square and then hand the
        -- ANCHOR's z to the move task, so the destination mixed a horizontal position
        -- from the zone's floor with a height from wherever she happened to be standing.
        -- Zones.RandomFreeSquare always resolves at zone.z, so the two differ exactly
        -- when she is on another storey -- upstairs in a ground-floor base, or partway up
        -- a staircase, where z is fractional. Walking her to (ground-floor x, y) at
        -- stair-height z is the best lead we have for the "she took the stairs and FELL"
        -- report: no error is logged, which fits a bad destination rather than a crash.
        local wsq, wx, wy, wz
        if baseZone then
            wsq = Zc.RandomFreeSquare(baseZone)
            if wsq then wx, wy, wz = wsq:getX(), wsq:getY(), wsq:getZ() end
        else
            local anchorSq = getCell():getGridSquare(anchor.x, anchor.y, az)
            local anchorIndoors = anchorSq and anchorSq:getRoom() ~= nil
            wx = anchor.x + ZombRand(RELAX_RADIUS * 2 + 1) - RELAX_RADIUS
            wy = anchor.y + ZombRand(RELAX_RADIUS * 2 + 1) - RELAX_RADIUS
            wz = az   -- deliberate here: this branch builds a tile AROUND the anchor
            wsq = getCell():getGridSquare(wx, wy, az)
            if wsq and not (wsq:isFree(false) and (not anchorIndoors or wsq:getRoom() ~= nil)) then
                wsq = nil
            end
        end
        -- FIX(TASK-016): THE BUDGET IS NEARLY INERT HERE AND THAT IS A PROPERTY OF THE
        -- SITE, NOT A FAULT IN THE BUDGET. This branch rolls a FRESH tile every evaluation,
        -- so the key changes every evaluation and nothing accrues; it can only reach the
        -- cap where the same tile comes back 60 times running, i.e. a zone with one free
        -- square in it. It is here for that degenerate case and for uniformity, and it is
        -- SAFE to have here only because eviction is by lowest `tries` -- these throwaway
        -- entries can never displace the seat or the anchor. Do not conclude from a quiet
        -- wander site that the budget is broken.
        if wsq and wz then
            if moveStalled(bandit, brain, "wander", wx + 0.5, wy + 0.5, wz) then
                -- no goal to clear: the wander destination lives on the stack, not on the
                -- brain, so giving up IS falling through to the idle below.
                idleWhy = "wander: gave up on " .. tostring(wx) .. "," .. tostring(wy)
                          .. "," .. tostring(wz)
            else
                Bandit.ForceStationary(bandit, false)
                table.insert(tasks, BanditUtils.GetMoveTask(0, wx + 0.5, wy + 0.5, wz, "Walk", 10))
                return {status = true, next = "Main", tasks = tasks}
            end
        else
            -- bad tile rolled: fall through to an idle
            idleWhy = "wander: no free tile (" .. (baseZone and "zone" or "radius") .. ")"
        end
    elseif roll < 7 then
        -- SOMETHING IN THE HOUSE. Tried before sitting, because the chair was the only
        -- thing she ever did and it needs to stop being the default.
        local opts, optsWhy = availablePastimes(brain, bandit, atHome)

        -- FIX(TASK-019): EMIT THE REJECTION COUNTERS HERE, WHERE THEY ARE REACHABLE.
        --
        -- They used to reach the trace only through `idleWhy` at the bottom of this
        -- function, which is written ONLY when the list came back empty AND no chair was
        -- assigned. **In a working base that is unreachable by construction** -- a base
        -- with furniture returns a non-empty list and returns early, three lines below.
        -- So `offsmoke=` and `offbusy=` never printed across three test sessions, and
        -- BOTH SHIPPED GATES HAD NO PATH TO VERIFICATION: a per-candidate rejection
        -- counter that only surfaces on total failure cannot measure a rejection.
        --
        -- Written here, before the branch, so all three outcomes carry it -- picked,
        -- fell through to the chair, fell through to ambient idle.
        --
        -- CHANGE-ONLY, NOT `Tr.Every`. The counters are a STATE, not a rate: what matters
        -- is that `offbusy` went from 0 to 1 when the second companion sat down, and a
        -- change-only line reports exactly that and then goes quiet. A heartbeat would
        -- write the same numbers every second for the whole session. Its own key, so it
        -- does not share the `idle` stream's repeat counter.
        --
        -- `n=` is the SURVIVING count, and it is here because `spots=` is not it: `spots`
        -- counts candidates the base scan offered, before the four rejections. n=0 with
        -- spots=0 is a base with no furniture; n=0 with spots=6 is four gates eating
        -- everything, which is a bug and looks nothing alike.
        local TP = BanditsNPC.Trace
        if TP and TP.Change then
            pcall(function() TP.Change(bandit, "RELAX", "pastimes",
                  "pastimes n=" .. #opts .. " " .. tostring(optsWhy)) end)
        end

        if #opts > 0 then
            local pick = opts[1 + ZombRand(#opts)]
            brain.relaxGoal = "do"
            brain.relaxDo = pick.def.id
            -- `dir` travels with the spot now: a `sit` pastime needs the seat's facing,
            -- and dropping it here meant even a player-assigned spot that HAD one fell
            -- back to guessing the facing from the surrounding tiles.
            brain.relaxDoSpot = { x = pick.spot.x, y = pick.spot.y, z = pick.spot.z or 0,
                                  dir = pick.spot.dir }
            brain.relaxDoRan = nil
            -- FIX(BUG-029): a NEW spot starts with a fresh budget AND a fresh best
            -- distance. Without this a count left over from an unreachable spot writes off
            -- the next one immediately, and a stale best from a spot she was standing on
            -- would make every farther spot look like zero progress from its first tick.
            brain.relaxDoTries, brain.relaxDoBest = nil, nil
            PASTIME_BY_ID[pick.def.id] = pick.def   -- the car def is built on the fly
            return {status = true, next = "Main", tasks = tasks}
        end
        if chairOk then
            brain.relaxGoal = "sit"
            return {status = true, next = "Main", tasks = tasks}
        end
        idleWhy = "pastimes: none [" .. tostring(optsWhy) .. "] chair=0"
    elseif chairOk then
        brain.relaxGoal = "sit"
        return {status = true, next = "Main", tasks = tasks}
    else
        idleWhy = "roll " .. tostring(roll) .. ": chair branch, but no chair assigned"
    end

    -- FIX(TASK-015): CHANGE-ONLY, because this runs about three times a second. A
    -- companion pottering normally writes one line and then nothing; a companion stuck in
    -- one of the two broken branches writes one line and a repeat count that climbs into
    -- the thousands, which is exactly the shape that makes a livelock visible. Reason
    -- first in the text so a changed reason always changes the line.
    local T = BanditsNPC.Trace
    if T and T.Change then
        pcall(function() T.Change(bandit, "RELAX", "idle", "ambient idle -- " .. idleWhy) end)
    end

    -- Ambient idle in place. THE ONLY PLACE A FLOOR POSE IS SAFE, and it is safe here for
    -- a specific reason rather than by luck: stepOffFurniture() ran a few lines above, so
    -- she is standing on a clear tile and a ground sit cannot put her through a chair.
    --
    -- Four emotes became twelve (v0.77.14). They were four because the mod believed it had
    -- almost no animations available; it has ~174, all already installed. The three
    -- SitGround entries are weighted in by being listed once each against EIGHT standing
    -- ones, NINE for a companion who may smoke, so she mostly potters and occasionally
    -- sits down for a moment.
    --
    -- FIX(TASK-018): `Smoke` IS APPENDED, NOT LISTED, and the count above says eight-or-
    -- nine for that reason. It was the FIRST entry in this list. Appending keeps the pool
    -- at the same twelve for a smoker and drops exactly one entry for everybody else,
    -- with no other weight disturbed.
    --
    -- FIX(TASK-018b): THIS COMMENT USED TO SAY "the second of the three places the
    -- animation is played". THERE WERE FOUR, AND THE FOURTH WAS NOT IN THIS MOD --
    -- `BanditPrograms.Idle`, reached from eight call sites in five other programs. The
    -- count was written from a grep of our own string literals, which is not the same
    -- question as "what can make her smoke". See `idleTasks` at the top of this file.
    Bandit.ForceStationary(bandit, true)
    local anims = {
        "ChewNails", "ShiftWeight", "Cough", "WipeBrow", "PullAtCollar",
        "Shrug", "Sneeze", "WaveHi",
        "Sit", "SitRubHands", "SitMaking",
    }
    if maySmoke(brain) then anims[#anims + 1] = "Smoke" end
    table.insert(tasks, {action = "Time", anim = anims[1 + ZombRand(#anims)], time = 200})
    return {status = true, next = "Main", tasks = tasks}
end

-- NPCGuard: patrol between brain.guardA and brain.guardB with a random pause at
-- each end. Falls back to standing watch if no patrol points are set.
ZombiePrograms.NPCGuard = {}
ZombiePrograms.NPCGuard.Prepare = function(bandit)
    return {status = true, next = "Main", tasks = {}}
end
ZombiePrograms.NPCGuard.Main = function(bandit)
    local tasks = {}
    if BanditsNPC.Needs then BanditsNPC.Needs.Update(bandit) end
    if BanditsNPC.Routine and BanditsNPC.Routine.MaybeStart(bandit) then
        return {status = true, next = "Main", tasks = tasks}
    end
    if BanditsNPC.Schedule and BanditsNPC.Schedule.Apply(bandit) then
        return {status = true, next = "Main", tasks = tasks}
    end

    -- VIGILANCE: a guard scans for threats and ENGAGES them. The engine combat loop
    -- (ManageCombat) runs before this program and does the actual hitting/shooting -- but
    -- it only swings once the enemy is within weapon range (melee ~1.2-2.6 tiles). So a
    -- melee guard has to CLOSE the distance itself, otherwise she just stands and faces an
    -- attacker out of reach (the "guarding but ignores the bandits attacking us" bug). We
    -- walk her right up to striking range; ManageCombat then takes over and Smacks/fires.
    if BanditsNPC.GetStance(bandit) ~= "passive" then
        local cz = BanditUtils.GetClosestZombieLocation(bandit)
        local cb = BanditUtils.GetClosestEnemyBanditLocation(bandit)
        local ce = cz
        if cb.dist < cz.dist then ce = cb end
        -- aggressive guards chase further; defensive guards hold a tighter area
        local engage = (BanditsNPC.GetStance(bandit) == "aggressive") and 16 or 10
        if ce.dist < engage then
            if ce.dist > 1.6 then
                Bandit.ForceStationary(bandit, false)
                table.insert(tasks, BanditUtils.GetMoveTask(0, ce.x, ce.y, ce.z, "WalkAim", ce.dist))
            else
                table.insert(tasks, {action = "FaceLocation", x = ce.x, y = ce.y, time = 60})
            end
            return {status = true, next = "Main", tasks = tasks}
        end
    end

    local brain = BanditBrain.Get(bandit)
    local a = brain and brain.guardA
    local b = brain and brain.guardB

    if a and b then
        if brain.guardTarget ~= "a" and brain.guardTarget ~= "b" then brain.guardTarget = "a" end
        local tgt = (brain.guardTarget == "a") and a or b
        local tx, ty = tgt.x + 0.5, tgt.y + 0.5
        local dist = BanditUtils.DistTo(bandit:getX(), bandit:getY(), tx, ty)

        if dist > 0.6 then
            -- FIX(TASK-016): the goal here is `guardTarget`, so clearing it means going to
            -- the OTHER end of the patrol -- the one give-up on this site that still leaves
            -- her guarding. If that end is unreachable too it gets its own budget and its
            -- own rest window, so the worst case is a slow oscillation between two written
            -- off posts rather than one endless shove. The wait counter is reset with it:
            -- it belongs to the end she never arrived at.
            if moveStalled(bandit, brain, "guard", tx, ty, tgt.z or 0) then
                brain.guardTarget = (brain.guardTarget == "a") and "b" or "a"
                brain.guardWait, brain.guardWaitMax = 0, nil
                return {status = true, next = "Main", tasks = tasks}
            end
            Bandit.ForceStationary(bandit, false)
            table.insert(tasks, BanditUtils.GetMoveTask(0, tx, ty, tgt.z or 0, "Walk", dist))
            return {status = true, next = "Main", tasks = tasks}
        else
            -- reached an end: wait a random spell (counted in idle cycles), then
            -- head to the other end.
            Bandit.ForceStationary(bandit, true)
            brain.guardWaitMax = brain.guardWaitMax or (3 + ZombRand(6))
            brain.guardWait = (brain.guardWait or 0) + 1
            if brain.guardWait >= brain.guardWaitMax then
                brain.guardWait = 0
                brain.guardWaitMax = nil
                brain.guardTarget = (brain.guardTarget == "a") and "b" or "a"
            else
                for _, t in pairs(idleTasks(bandit)) do table.insert(tasks, t) end
            end
            return {status = true, next = "Main", tasks = tasks}
        end
    end

    -- no patrol points: stand and watch nearby enemies
    Bandit.ForceStationary(bandit, true)
    local cz = BanditUtils.GetClosestZombieLocation(bandit)
    local cb = BanditUtils.GetClosestEnemyBanditLocation(bandit)
    local ce = cz
    if cb.dist < cz.dist then ce = cb end
    if ce.dist < 24 then
        -- anim: see the guard-watch note above (zombie idle sway otherwise)
        table.insert(tasks, {action = "FaceLocation", anim = "ShiftWeight", x = ce.x, y = ce.y, time = 100})
    else
        for _, t in pairs(idleTasks(bandit)) do table.insert(tasks, t) end
    end
    return {status = true, next = "Main", tasks = tasks}
end

-- NPCWork: walk to the assigned workstation and play a work animation while a
-- production job runs. When the job's time elapses, deposit the outputs and
-- return to whatever the NPC was doing before (Follow/Stay/Guard).
ZombiePrograms.NPCWork = {}
ZombiePrograms.NPCWork.Prepare = function(bandit)
    Bandit.ForceStationary(bandit, false)
    return {status = true, next = "Main", tasks = {}}
end
ZombiePrograms.NPCWork.Main = function(bandit)
    local tasks = {}
    local brain = BanditBrain.Get(bandit)
    local job = brain and brain.prodJob

    local function finishUp()
        if BanditsNPC and BanditsNPC.Production then
            BanditsNPC.Production.Update(bandit)   -- deposit outputs if the job is done
        end
        local prev = (brain and brain.prevProgram) or { name = "NPCCompanion" }
        if brain then
            brain.prevProgram = nil
            brain.program = { name = prev.name or "NPCCompanion", stage = "Prepare" }
            BanditBrain.Update(bandit, brain)
            if Bandit.ForceSyncPart then Bandit.ForceSyncPart(bandit, { id = brain.id, program = brain.program }) end
        end
        Bandit.ForceStationary(bandit, false)
    end

    -- no job, or job finished -> wrap up and hand control back
    if not job or job == false or getGameTime():getWorldAgeHours() >= job.finishHour then
        finishUp()
        return {status = true, next = "Prepare", tasks = tasks}
    end

    -- find the station tile
    local ws = brain.workstation
    local ssq = ws and getCell():getGridSquare(ws.x, ws.y, ws.z or 0)
    if not ssq then
        -- station location unknown; wait it out (job still completes on time)
        for _, t in pairs(idleTasks(bandit)) do table.insert(tasks, t) end
        return {status = true, next = "Main", tasks = tasks}
    end

    local stand = AdjacentFreeTileFinder.Find(ssq, bandit) or ssq
    local tx, ty, tz = stand:getX() + 0.5, stand:getY() + 0.5, stand:getZ()
    local dist = BanditUtils.DistTo(bandit:getX(), bandit:getY(), tx, ty)

    if dist > 0.8 or math.abs((tz or 0) - bandit:getZ()) >= 1 then
        Bandit.ForceStationary(bandit, false)
        table.insert(tasks, BanditUtils.GetMoveTask(0, tx, ty, tz, "Walk", dist))
        return {status = true, next = "Main", tasks = tasks}
    end

    -- at the station: face it and play a random chunk of the work animation
    Bandit.ForceStationary(bandit, true)
    local stationType = BanditsNPC.Production and BanditsNPC.Production.GetStationType(brain)
    local anim = (BanditsNPC.Production and BanditsNPC.Production.GetWorkAnim(stationType)) or "Loot"
    table.insert(tasks, {action = "FaceLocation", x = ssq:getX(), y = ssq:getY(), time = 20})
    table.insert(tasks, {action = "Time", anim = anim, time = 160})
    return {status = true, next = "Main", tasks = tasks}
end

-- over-head status text for routine actions (so you can see what she's doing).
-- Shown once when she starts heading out, and again when she begins the action.
-- {key, English} pairs; translated at say-time (not baked at load)
local ROUTINE_VERBS = {
    hunger  = { go = {"UI_BN_Rt_hunger_go", "Hungry - getting food"}, at = {"UI_BN_Rt_hunger_at", "Eating"} },
    fatigue = { go = {"UI_BN_Rt_fatigue_go", "Tired - going to rest"}, at = {"UI_BN_Rt_fatigue_at", "Sleeping"} },
    boredom = { go = {"UI_BN_Rt_boredom_go", "Bored - going to relax"}, at = {"UI_BN_Rt_boredom_at", "Relaxing"} },
    hygiene = { go = {"UI_BN_Rt_hygiene_go", "Going to wash up"}, at = {"UI_BN_Rt_hygiene_at", "Washing up"} },
}
local function routineSay(bandit, brain, phase)
    if not brain or brain.routinePhase == phase then return end
    brain.routinePhase = phase
    local v = ROUTINE_VERBS[brain.routineTask and brain.routineTask.need] or {}
    local entry = (phase == "go") and v.go or v.at
    local txt = entry and BanditsNPC.T(entry[1], entry[2])
    if txt and bandit.addLineChatElement then
        pcall(function() bandit:addLineChatElement(txt, 0.55, 0.9, 0.6) end)
    end
end

-- True if the square holds BED furniture (the engine's sleepable flag; modded
-- beds carry it too). Used to re-check the bed at sleep time -- the player may
-- have moved/destroyed it since the spot was assigned.
local function squareHasBed(sq)
    if not sq then return false end
    local ok, res = pcall(function()
        local objs = sq:getObjects()
        for i = 0, objs:size() - 1 do
            local o = objs:get(i)
            if o and o.getSprite and o.getProperties and o:getSprite()
               and o:getProperties():has(IsoFlagType.bed) then return true end
        end
        return false
    end)
    return ok and res == true
end

-- Step off furniture: a positioned sit/sleep pins her ONTO the furniture tile and
-- its collision then traps her (tester: "stuck in the couch for half a minute
-- until she teleported"). Whenever she needs to move and her square isn't free,
-- pop her to an adjacent free tile first.
--
-- FIXED (v0.60): this used to be `setX`/`setY` with NO setZ and NO floor check --
-- the same shape as the Guardian's stuck teleport, and one of the two places that
-- dropped a companion off a staircase. Her square is "not free" on a stair too, so
-- this fired there, moved her sideways while she kept a fractional stair Z, and
-- left her over open air. Nav.Teleport refuses both the unsafe square and the
-- in-transit case, and always writes all three coordinates.
--
stepOffFurniture = function(bandit)   -- declared near the top of the file; see note there
    pcall(function()
        local sq = bandit:getSquare()
        if sq and not sq:isFree(false) then
            local off = AdjacentFreeTileFinder.Find(sq, bandit)
            if off then BanditsNPC.Nav.Teleport(bandit, off, "step off furniture") end
        end
    end)
end

-- Facing for a bed spot saved before the direction picker existed: look for the
-- bed's second tile among the neighbors and face along the bed's axis.
local function deriveBedFacing(sq)
    local x, y, z = sq:getX(), sq:getY(), sq:getZ()
    for _, d in ipairs({ {0,-1,"N"}, {0,1,"S"}, {1,0,"E"}, {-1,0,"W"} }) do
        if squareHasBed(getCell():getGridSquare(x + d[1], y + d[2], z)) then return d[3] end
    end
    return "S"
end

-- Real threats near a sleeper: any non-bandit zombie, or a HOSTILE bandit, close by
-- on the same floor. Friendly companions and neutral wanderers don't count.
local function threatNear(bandit, radius)
    local found = false
    pcall(function()
        local zl = getCell():getZombieList()
        local bx, by, bz = bandit:getX(), bandit:getY(), bandit:getZ()
        for i = 0, zl:size() - 1 do
            local z = zl:get(i)
            if z and z ~= bandit and z:isAlive() and math.abs(z:getZ() - bz) < 1
               and BanditUtils.DistTo(z:getX(), z:getY(), bx, by) <= radius then
                if not z:getVariableBoolean("Bandit") then found = true; return end
                local b = BanditBrain.Get(z)
                if b and (b.hostile or b.hostileP) then found = true; return end
            end
        end
    end)
    return found
end

-- NPCRoutine: walk to the assigned spot for the active need and satisfy it
-- (eat from the food container, or rest/relax to drain fatigue/boredom), then
-- return to the previous order. Triggered by BanditsNPC.Routine.MaybeStart.
ZombiePrograms.NPCRoutine = {}
ZombiePrograms.NPCRoutine.Prepare = function(bandit)
    Bandit.ForceStationary(bandit, false)
    return {status = true, next = "Main", tasks = {}}
end
ZombiePrograms.NPCRoutine.Main = function(bandit)
    local tasks = {}
    local brain = BanditBrain.Get(bandit)
    local rt = brain and brain.routineTask

    local function finish()
        -- wake cleanly if she was in the bed: drop the engine's sleeping flag, then get
        -- up. The GetUp task is locked, so it plays out before the restored program
        -- dispatches anything.
        if rt and rt.sleeping then
            pcall(function() Bandit.SetSleeping(bandit, false) end)
            table.insert(tasks, {action = "Time", lock = true, anim = "GetUp", time = 150})
        end
        -- off the furniture (bed OR chair/tv sit) so its collision can't trap her, and
        -- kill any looping action sound (an interrupted Bandage/Wash never reaches its
        -- onComplete, which is what normally stops the emitter -- tester heard the
        -- bandage loop forever)
        stepOffFurniture(bandit)
        pcall(function() bandit:getEmitter():stopAll() end)
        local prev = (brain and brain.prevProgram) or { name = "NPCCompanion" }
        if brain then
            brain.prevProgram = nil
            brain.routineTask = false
            brain.routinePhase = nil
            brain.program = { name = prev.name or "NPCCompanion", stage = "Prepare" }
            BanditBrain.Update(bandit, brain)
            if Bandit.ForceSyncPart then Bandit.ForceSyncPart(bandit, { id = brain.id, program = brain.program, routineTask = false }) end
        end
        Bandit.ForceStationary(bandit, false)
    end

    if not rt or rt == false then finish(); return {status = true, next = "Prepare", tasks = tasks} end

    -- rt.spot is the coordinate resolved when the routine STARTED (assigned spot if
    -- there is one, otherwise a stable pick from the base scan). The brain.spots
    -- lookup stays as the fallback so a routine saved by an older build still runs.
    local spot = rt.spot or (brain.spots and brain.spots[rt.spotKey])
    if not spot then finish(); return {status = true, next = "Prepare", tasks = tasks} end

    -- already satisfied? (a scheduled sleep SHIFT ignores the meter: she sleeps the
    -- whole block through instead of getting up rested at 3am and standing around)
    local n = BanditsNPC.Needs and BanditsNPC.Needs.Get(brain)
    if n and (n[rt.need] or 0) <= 5 and rt.mode ~= "shift" then finish(); return {status = true, next = "Prepare", tasks = tasks} end

    local ssq = getCell():getGridSquare(spot.x, spot.y, spot.z or 0)
    if not ssq then finish(); return {status = true, next = "Prepare", tasks = tasks} end

    local stand = AdjacentFreeTileFinder.Find(ssq, bandit) or ssq
    local tx, ty, tz = stand:getX() + 0.5, stand:getY() + 0.5, stand:getZ()
    local dist = BanditUtils.DistTo(bandit:getX(), bandit:getY(), tx, ty)
    -- BEING ON THE SPOT TILE COUNTS AS ARRIVED (13 Jul): positioned holds
    -- (BNSleep/BNSit) pin her ONTO the spot tile, which is ~1.0 from the adjacent
    -- stand tile -- the old stand-tile-only check therefore ordered a walk-back on
    -- EVERY hold re-issue: stepOffFurniture pop + Move + re-pin = "she teleports a
    -- meter away and instantly back to bed every ~5 seconds" (the BNSleep task's
    -- time=200 expiry cycle). The same cycle silently jolted chair/TV sits.
    local distSpot = BanditUtils.DistTo(bandit:getX(), bandit:getY(), spot.x + 0.5, spot.y + 0.5)
    if distSpot < dist then dist = distSpot end
    -- Same straight-line-through-glass problem as the work zones (v0.75.57): a sink or a
    -- bed on the far side of a wall is 1.0 tiles away by DistTo. Only vetoes a CARDINAL
    -- neighbour with a real barrier between, so it cannot affect a normal approach.
    local reach = true
    pcall(function() reach = BanditsNPC.Nav.CanWorkAcross(bandit:getCurrentSquare(), ssq) end)
    if not reach then dist = math.max(dist, 1.0) end
    -- NEVER ORDER A WALK TO THE SQUARE SHE IS STANDING ON (v0.75.63). Same defect, same
    -- shape, as the one written up over `approach` in BanditsNPCJobs: pathToLocation for
    -- her own square returns a zero-length path, the Move task completes before a walk
    -- animation can start, the program re-runs and orders it again -- she stands perfectly
    -- still while the stuck watchdog counts down to a teleport. "I told her to relax at the
    -- base and she still stayed in frozen idle mode" (author, 6 Aug).
    local onStandTile = false
    pcall(function()
        local hs = bandit:getCurrentSquare()
        onStandTile = hs ~= nil and stand ~= nil
                      and hs:getX() == stand:getX() and hs:getY() == stand:getY()
                      and hs:getZ() == stand:getZ()
    end)
    if not onStandTile and (dist > 0.8 or math.abs((tz or 0) - bandit:getZ()) >= 1) then
        routineSay(bandit, brain, "go")
        stepOffFurniture(bandit)   -- a routine can start while she's seated (Relax sit)
        Bandit.ForceStationary(bandit, false)
        table.insert(tasks, BanditUtils.GetMoveTask(0, tx, ty, tz, "Walk", dist))
        return {status = true, next = "Main", tasks = tasks}
    end

    -- at the spot: perform the action
    routineSay(bandit, brain, "at")
    Bandit.ForceStationary(bandit, true)
    if rt.need == "hunger" then
        -- spotKey "self" means she is eating out of her own bag, wherever she is --
        -- no container, no travel. Set by Routine.MaybeStart when there is no food
        -- store she can reach but there is food on her.
        if rt.spotKey == "self" then
            local what = BanditsNPC.Routine.EatFromInventory(bandit)
            if BanditsNPC.Needs then BanditsNPC.Needs.Reduce(bandit, "hunger", what and 100 or 0) end
            if what then
                -- Says what she is eating, because "she wandered off and played an
                -- animation" is indistinguishable from a bug.
                pcall(function()
                    bandit:addLineChatElement(
                        BanditsNPC.TF("UI_BN_Bark_Eating", "Eating %1", what), 0.85, 0.95, 0.75)
                end)
                Bandit.ForceStationary(bandit, true)
                table.insert(tasks, {action = "Time", anim = "Eat", time = 260})
            end
            return {status = true, next = "Main", tasks = tasks}
        end
        local ate = BanditsNPC.Routine.EatFromContainer(ssq)
        if BanditsNPC.Needs then BanditsNPC.Needs.Reduce(bandit, "hunger", ate and 100 or 40) end
        table.insert(tasks, {action = "FaceLocation", x = ssq:getX(), y = ssq:getY(), time = 20})
        table.insert(tasks, {action = "Time", anim = "Eat", time = 260})
        return {status = true, next = "Main", tasks = tasks}
    elseif rt.need == "fatigue" then
        -- SLEEP IN THE BED: the engine's positioned Sleep action (ZASleep) pins her
        -- onto the bed tile every tick, oriented by `facing`, in the lying bump-anim.
        -- Re-issued each dispatch like our other holds. eoffset "2" (two-tile bed
        -- shift) is only honored by the engine for facing S.
        if not squareHasBed(ssq) then
            -- bed moved/destroyed since the spot was set: forget it and go back
            brain.spots.bed = nil
            pcall(function() bandit:addLineChatElement(BanditsNPC.T("UI_BN_Bark_BedGone", "My bed is gone..."), 0.9, 0.75, 0.5) end)
            finish(); return {status = true, next = "Prepare", tasks = tasks}
        end
        -- WAKE RULES: a threat snaps her up; a "go to bed on time" night ends when the
        -- window does; a scheduled sleep shift ends when the schedule block changes
        -- (Schedule.Apply can't do it -- it skips while the program is NPCRoutine).
        if threatNear(bandit, 7) then
            pcall(function() bandit:addLineChatElement("!", 1.0, 0.45, 0.4) end)
            finish(); return {status = true, next = "Prepare", tasks = tasks}
        end
        local hour = getGameTime():getHour()
        if rt.mode == "night" and not (BanditsNPC.Routine and BanditsNPC.Routine.InSleepWindow(hour)) then
            finish(); return {status = true, next = "Prepare", tasks = tasks}
        end
        if rt.mode == "shift" and rt.block and BanditsNPC.Schedule
           and BanditsNPC.Schedule.BlockForHour(hour) ~= rt.block then
            finish(); return {status = true, next = "Prepare", tasks = tasks}
        end
        rt.sleeping = true
        pcall(function() Bandit.SetSleeping(bandit, true) end)
        -- fatigue drains on GAME time: a full night (~8h) restores a full meter
        -- (15/h gross; Needs.Update still ticks +2/h underneath -> ~13/h net)
        local nowH = getGameTime():getWorldAgeHours()
        local dtH = math.max(0, nowH - (rt.sleepHour or nowH))
        rt.sleepHour = nowH
        if BanditsNPC.Needs and dtH > 0 then BanditsNPC.Needs.Reduce(bandit, "fatigue", 15 * dtH, true) end
        local dir = spot.dir or deriveBedFacing(ssq)
        -- our own positioned sleep (BNSleep, BanditsNPCActions.lua): pins her to the
        -- spot tile in EVERY task stage (the re-issue gap used to let collision pop
        -- her out of the bed, tester report 11 Jul). Anim "BNSleepBed" =
        -- Bob_Asleep_Bed, a community-CONTRIBUTED lying pose shipped with this mod
        -- (thanks!) that positions itself from the plain tile center. The old
        -- two-tile centering (x2/y2 scan) was REVERTED 13 Jul: with this anim it
        -- double-offset the pose, and the between-tiles position flip-flopped her
        -- occupied square -> the visible/invisible/pop-out flicker (details in the
        -- BNSleep header in BanditsNPCActions.lua).
        table.insert(tasks, {action = "BNSleep", anim = "BNSleepBed", x = spot.x, y = spot.y, z = spot.z or 0,
                             facing = dir, time = 200})
        return {status = true, next = "Main", tasks = tasks}
    elseif rt.need == "boredom" and (rt.spotKey == "chair" or rt.spotKey == "tv") then
        -- sit on the furniture properly: same positioned engine action as the bed,
        -- without the sleep state. A TV spot is the tile she SITS on.
        if BanditsNPC.Needs then BanditsNPC.Needs.Reduce(bandit, "boredom", 8, true) end
        local facing = spot.dir or seatFacing(spot.x, spot.y, spot.z) or "S"
        -- TV spot: whatever the arrow says, she should WATCH the set -- face the
        -- nearest television (found once per routine, cached on the task). The picked
        -- direction stays the fallback for TV-less tiles ("sat with her back to the
        -- TV" report, 11 Jul).
        if rt.spotKey == "tv" then
            if rt.tvDir == nil then rt.tvDir = tvFacing(spot.x, spot.y, spot.z or 0) or false end
            if rt.tvDir then facing = rt.tvDir end
            -- AND SWITCH IT ON. The Relax pastime did this via its `effect`; the boredom
            -- ROUTINE reached the same seat by a different path and never did, so a
            -- scheduled TV break sat her in front of a dead set (author test, v0.75.57).
            -- FIX(BUG-026): record here too, or a scheduled TV break would be the one
            -- path that still leaves a set running forever. It writes the SAME generic
            -- record the pastime framework writes, so NPCRelax's relaxUndo resolves and
            -- reverses it with no special case -- `PASTIME_BY_ID["tv"]` is the entry
            -- declared above with `id = "tv"`, and it carries the `undo`. This program has
            -- no abandonment path of its own, so the switch-off happens the next time she
            -- relaxes: later than the pastime case, bounded, and occupancy-checked either
            -- way (spotInUseByOther matches a routine TV break through routineTask.spotKey).
            if turnOnTV(spot.x, spot.y, spot.z or 0) then
                brain.npcUndo = { id = "tv", x = spot.x, y = spot.y, z = spot.z or 0 }
            end
        end
        -- BNSit: real chair-sit pose (see NPCRelax) instead of the ground-sit anim
        table.insert(tasks, {action = "BNSit", anim = "BNSitChair", x = spot.x, y = spot.y, z = spot.z or 0,
                             facing = facing, time = 200})
        return {status = true, next = "Main", tasks = tasks}
    elseif rt.need == "hygiene" then
        -- the engine's own Wash action: washFace anim at the water source; its
        -- onComplete clears per-part body blood + dirt. Once she's nearly done, run
        -- the FULL CleanUp (aggregate blood/dirt layers, ZedDmg gore overlays, skin
        -- re-provision AND the clothing re-dress) -- ZAWash's per-part zeroing alone
        -- left her visibly gory, exactly like the old Clean up button did ("washed
        -- but neither body nor portrait cleaned", report 11 Jul).
        if BanditsNPC.Needs then BanditsNPC.Needs.Reduce(bandit, "hygiene", 12, true) end
        local nn = BanditsNPC.Needs and BanditsNPC.Needs.Get(brain)
        if nn and (nn.hygiene or 0) <= 5 and BanditsNPC.Interact and BanditsNPC.Interact.CleanUp then
            pcall(function() BanditsNPC.Interact.CleanUp(bandit) end)
        end
        table.insert(tasks, {action = "Wash", anim = "washFace", x = ssq:getX(), y = ssq:getY(), z = ssq:getZ(), time = 400})
        return {status = true, next = "Main", tasks = tasks}
    else
        -- reading-shelf boredom and anything else: drain gradually with a fitting
        -- stand-in animation at the spot. Reading = sit down with a book (seated
        -- fiddling anim, unpositioned -- she sits where she stands, like the
        -- engine's campers), not a smoke break.
        if BanditsNPC.Needs then BanditsNPC.Needs.Reduce(bandit, rt.need, 8, true) end
        if rt.spotKey == "reading" then
            table.insert(tasks, {action = "Sleep", anim = "SitAction", time = 200})
        else
            -- FIX(TASK-018): third and last site. `ShiftWeight` was already this
            -- line's fallback for every other need, so a non-smoker takes a path that
            -- was always here -- no new animation name is introduced anywhere in this
            -- change, which matters because Bandits' AnimSets are not vendored in refs/
            -- and a wrong name fails silently to the idle sway (see UNKNOWNS.md).
            local anim = (maySmoke(brain) and ({ boredom = "Smoke" })[rt.need])
                         or "ShiftWeight"
            table.insert(tasks, {action = "FaceLocation", x = ssq:getX(), y = ssq:getY(), time = 20})
            table.insert(tasks, {action = "Time", anim = anim, time = 160})
        end
        return {status = true, next = "Main", tasks = tasks}
    end
end

-- NPCAnimPlay: debug/preview - walk to the chosen tile and perform a chosen
-- animation a few times, then return to the previous program.
ZombiePrograms.NPCAnimPlay = {}
ZombiePrograms.NPCAnimPlay.Prepare = function(bandit)
    Bandit.ForceStationary(bandit, false)
    return {status = true, next = "Main", tasks = {}}
end
ZombiePrograms.NPCAnimPlay.Main = function(bandit)
    local tasks = {}
    local brain = BanditBrain.Get(bandit)
    local ap = brain and brain.animPlay

    local function finish()
        local prev = (brain and brain.prevProgram) or { name = "NPCCompanion" }
        if brain then
            brain.prevProgram = nil
            brain.animPlay = false
            brain.program = { name = prev.name or "NPCCompanion", stage = "Prepare" }
            BanditBrain.Update(bandit, brain)
            if Bandit.ForceSyncPart then Bandit.ForceSyncPart(bandit, { id = brain.id, program = brain.program, animPlay = false }) end
        end
        Bandit.ForceStationary(bandit, false)
    end

    if not ap or ap == false then finish(); return {status = true, next = "Prepare", tasks = tasks} end

    local ssq = getCell():getGridSquare(ap.x, ap.y, ap.z or 0)
    if not ssq then finish(); return {status = true, next = "Prepare", tasks = tasks} end

    local tx, ty, tz = ssq:getX() + 0.5, ssq:getY() + 0.5, ssq:getZ()
    local dist = BanditUtils.DistTo(bandit:getX(), bandit:getY(), tx, ty)
    if dist > 0.8 or math.abs((tz or 0) - bandit:getZ()) >= 1 then
        -- FIX(TASK-016): a debug preview that cannot reach its tile should stop being a
        -- preview, so the give-up is finish() -- the same exit the count-exhausted path
        -- takes, which hands her back to the program she was running. NOTE the overlap
        -- with the Z guard: the `>= 1` arm of this very condition is the case the budget
        -- refuses to accrue on, so a cross-floor preview walks unbudgeted and relies on
        -- the watchdog. That is the documented cost of the Z guard, not an oversight here.
        if moveStalled(bandit, brain, "animplay", tx, ty, tz) then
            finish()
            return {status = true, next = "Prepare", tasks = tasks}
        end
        Bandit.ForceStationary(bandit, false)
        table.insert(tasks, BanditUtils.GetMoveTask(0, tx, ty, tz, "Walk", dist))
        return {status = true, next = "Main", tasks = tasks}
    end

    Bandit.ForceStationary(bandit, true)
    if (ap.count or 0) <= 0 then finish(); return {status = true, next = "Prepare", tasks = tasks} end
    ap.count = ap.count - 1
    BanditBrain.Update(bandit, brain)
    table.insert(tasks, {action = "FaceLocation", x = ssq:getX(), y = ssq:getY(), time = 20})
    table.insert(tasks, {action = "Time", anim = ap.anim or "Loot", time = 200})
    return {status = true, next = "Main", tasks = tasks}
end

-- NPCNeutral: a neutral wanderer / dismissed companion. It does NOT go hunting
-- zombies or loot (the Bandits "Looter" program would path out to fight and get
-- itself killed). It wanders gently, but the moment a player stands close it
-- STOPS and faces them -- so you can walk up and talk before it moves on. Defends
-- only at close range via the engine's combat loop. Used by spawn "Neutral
-- wanderer" and by Interact.Dismiss.
local NEUTRAL_PAUSE_RANGE = 5

local function neutralNearbyPlayer(bandit)
    local bx, by, bz = bandit:getX(), bandit:getY(), bandit:getZ()
    local best, bestD = nil, NEUTRAL_PAUSE_RANGE
    for i = 0, 3 do
        local p = getSpecificPlayer(i)
        if p and not p:isDead() and math.abs(p:getZ() - bz) < 1 then
            local d = BanditUtils.DistTo(bx, by, p:getX(), p:getY())
            if d <= bestD then best, bestD = p, d end
        end
    end
    return best
end

ZombiePrograms.NPCNeutral = {}
ZombiePrograms.NPCNeutral.Prepare = function(bandit)
    Bandit.ForceStationary(bandit, true)
    return {status = true, next = "Main", tasks = {}}
end
ZombiePrograms.NPCNeutral.Main = function(bandit)
    local tasks = {}
    local brain = BanditBrain.Get(bandit)

    -- pause & face a nearby player (lets you talk before it wanders off)
    local pauseOn = (BanditsNPC.Opt and BanditsNPC.Opt("PauseNearPlayer", true)) ~= false
    local player = pauseOn and neutralNearbyPlayer(bandit)
    if player then
        if brain then brain.wanderGoal = nil end
        Bandit.ForceStationary(bandit, true)
        -- anim: this hold fires exactly while a player walks up to TALK -- without an
        -- anim override the ZOMBIE idle sway plays underneath ("some NPCs wobble
        -- around like a zombie which hasn't spotted the player", report 11 Jul)
        table.insert(tasks, {action = "FaceLocation", anim = "ShiftWeight", x = player:getX(), y = player:getY(), time = 100})
        return {status = true, next = "Main", tasks = tasks}
    end

    -- gentle wander: walk to a random nearby tile, rest, repeat (no zombie seeking)
    if brain then
        if brain.wanderGoal then
            local g = brain.wanderGoal
            local dist = BanditUtils.DistTo(bandit:getX(), bandit:getY(), g.x + 0.5, g.y + 0.5)
            brain.wanderTries = (brain.wanderTries or 0) + 1
            if dist > 0.8 and brain.wanderTries < 12 then
                Bandit.ForceStationary(bandit, false)
                table.insert(tasks, BanditUtils.GetMoveTask(0, g.x + 0.5, g.y + 0.5, g.z or bandit:getZ(), "Walk", dist))
                return {status = true, next = "Main", tasks = tasks}
            end
            brain.wanderGoal = nil
            brain.wanderTries = 0
            brain.wanderRest = 5 + ZombRand(10)
        end
        if (brain.wanderRest or 0) > 0 then
            brain.wanderRest = brain.wanderRest - 1
            Bandit.ForceStationary(bandit, true)
            for _, t in pairs(idleTasks(bandit)) do table.insert(tasks, t) end
            return {status = true, next = "Main", tasks = tasks}
        end
        -- pick a new nearby goal
        -- FLOOR THE Z (v0.75.8, audit Cluster F). x and y are floored to a tile but z was
        -- taken raw, and getZ() is FRACTIONAL while she is on a staircase -- so a goal
        -- rolled mid-step became (some tile, some tile, 0.4), a destination on no storey
        -- at all. Same hazard the pose pins document ("never pin between two squares");
        -- this is its walking equivalent, and it is a second candidate mechanism for the
        -- stairs report alongside the NPCRelax anchor-z fix above.
        brain.wanderGoal = {
            x = math.floor(bandit:getX()) + (ZombRand(9) - 4),
            y = math.floor(bandit:getY()) + (ZombRand(9) - 4),
            z = math.floor(bandit:getZ()),
        }
        return {status = true, next = "Main", tasks = tasks}
    end

    Bandit.ForceStationary(bandit, true)
    for _, t in pairs(idleTasks(bandit)) do table.insert(tasks, t) end
    return {status = true, next = "Main", tasks = tasks}
end

-- NPCDowned: a knocked-down companion (see BanditsNPCCombat). It lies incapacitated and
-- can't move or fight until you help it up via the V window. While brain.downed is set it
-- holds a faint pose; once cleared (helped up) it hands control back to its previous order.
ZombiePrograms.NPCDowned = {}
ZombiePrograms.NPCDowned.Prepare = function(bandit)
    Bandit.ForceStationary(bandit, true)
    return {status = true, next = "Main", tasks = {}}
end
ZombiePrograms.NPCDowned.Main = function(bandit)
    local tasks = {}
    local brain = BanditBrain.Get(bandit)
    if not (brain and brain.downed) then
        local prev = (brain and brain.prevProgram) or { name = "NPCCompanion" }
        if brain then
            brain.prevProgram = nil
            brain.downedPosed = nil
            brain.program = { name = prev.name or "NPCCompanion", stage = "Prepare" }
            BanditBrain.Update(bandit, brain)
        end
        Bandit.ForceStationary(bandit, false)
        return {status = true, next = "Prepare", tasks = tasks}
    end
    Bandit.ForceStationary(bandit, true)
    -- HOLD the downed pose with one long locked Time task. This program is only dispatched
    -- when the task queue is EMPTY (BanditUpdate), and the engine completes a Time task
    -- early whenever the bump-type changes (any bite/stagger does it) -- so if we get
    -- control back while still downed, the hold was interrupted and must be re-issued.
    -- (The old one-shot brain.downedPosed guard left her standing in an idle pose,
    -- unresponsive, after the first interruption. No per-tick anim restart happens here:
    -- while the hold task runs, this program isn't dispatched at all.)
    brain.downedPosed = true
    table.insert(tasks, {action = "Time", anim = "Faint", time = 1000000, lock = true})
    return {status = true, next = "Main", tasks = tasks}
end

-- Passive-stance combat hook: a passive NPC treats nobody as an enemy, so the
-- engine's combat loop won't make it attack.
--
-- "(It can still be attacked.)" WAS WRONG AND IS CORRECTED HERE (v0.75.35). AreEnemies is
-- SYMMETRIC -- the engine asks "are these two enemies", not "does A want to fight B" --
-- so returning false for a passive companion makes her UNTARGETABLE as well as
-- non-aggressive. Hostiles ignore her entirely.
--
-- BEHAVIOUR LEFT AS IT IS, deliberately, because the fix is a design decision rather than
-- a bug fix and it cannot be tested here: passive currently doubles as "invisible to
-- hostile NPCs", which is either a useful way to keep a non-combatant alive or an exploit,
-- depending on what you want the stance to mean. Making it one-directional is not possible
-- through this hook anyway -- it is not told which side is the actor -- so it would need a
-- different mechanism. The DOWNED half of the same test is intentional and stays: that is
-- what "playing dead" is, and it has its own second hook in GetClosestBanditLocation.
-- We
-- wrap BanditUtils.AreEnemies once, leaving non-passive bandits untouched. Also: a
-- DOWNED companion is never a target, and two of the player's companions are never
-- enemies of each other.
if BanditUtils and BanditUtils.AreEnemies and not BanditsNPC._areEnemiesHooked then
    BanditsNPC._areEnemiesHooked = true
    local origAreEnemies = BanditUtils.AreEnemies
    BanditUtils.AreEnemies = function(brainA, brainB)
        if (brainA and (brainA.npcStance == "passive" or brainA.downed)) or
           (brainB and (brainB.npcStance == "passive" or brainB.downed)) then
            return false
        end
        if brainA and brainB and brainA.recruited and brainB.recruited then
            return false
        end
        return origAreEnemies(brainA, brainB)
    end
    -- remembered so reassertHooks (bottom of file) can tell ours from a replacement
    BanditsNPC._areEnemiesFn = BanditUtils.AreEnemies
end

-- DOWNED = INVISIBLE TO ZOMBIES. Zombies choose which bandit to chase/attack via
-- BanditUtils.GetClosestBanditLocation (a scan of BanditZombie.CacheLightB, whose entries
-- carry .brain). We wrap it to skip any DOWNED recruited companion, so a knocked-down
-- companion is never picked as a target and the mob disperses ("playing dead") until she's
-- helped up. Fast-path: when nobody is downed we just call the original, so normal targeting
-- is completely unchanged.
if BanditUtils and BanditUtils.GetClosestBanditLocation and not BanditsNPC._gcblHooked then
    BanditsNPC._gcblHooked = true
    local origGCBL = BanditUtils.GetClosestBanditLocation
    BanditUtils.GetClosestBanditLocation = function(character, config)
        local cl = BanditZombie and BanditZombie.CacheLightB
        if not cl then return origGCBL(character, config) end
        -- ASK UPSTREAM FIRST, ALWAYS (v0.75.49). This used to walk the whole cache looking
        -- for ANY downed companion and, if it found one, throw the original away and
        -- rebuild the result itself -- so while a single companion was down, every
        -- targeting query in the game ran our private copy of Bandits' distance /
        -- levelDiff / self-exclusion rules. Two problems: it is a REPLACEMENT of upstream
        -- semantics that will drift silently the day Bandits changes them (HANDOFF's
        -- "CHAIN, DON'T REPLACE"), and being downed is precisely when there is a mob
        -- present, so the cost landed when the frame budget was tightest.
        --
        -- Now: take upstream's answer, and only re-scan if THAT ANSWER is itself a downed
        -- companion. Normal targeting is untouched, the slow path is rare, and upstream's
        -- rules stay upstream's.
        local base = origGCBL(character, config)
        local picked = base and base.id
        local pe = picked and cl[picked]
        if not (pe and pe.brain and pe.brain.downed and pe.brain.recruited) then
            return base
        end

        -- upstream picked someone who is down: re-run, skipping downed companions
        config = config or {}
        local cid = BanditUtils.GetCharacterID(character)
        local cx, cy, cz = character:getX(), character:getY(), character:getZ()
        local result = { dist = math.huge, x = false, y = false, z = false, id = false }
        for id, e in pairs(cl) do
            if not (e.brain and e.brain.downed and e.brain.recruited) then
                local dist = math.sqrt(((cx - e.x) * (cx - e.x)) + ((cy - e.y) * (cy - e.y)))
                local levelDiff = math.abs(e.z - cz)
                if dist < result.dist and cid ~= id and (not config.levelDiff or levelDiff <= config.levelDiff) then
                    result.dist = dist; result.x = e.x; result.y = e.y; result.z = e.z; result.d = e.d; result.id = e.id
                end
            end
        end
        return result
    end
    BanditsNPC._closestFn = BanditUtils.GetClosestBanditLocation
end

-- ===== SAFETY NET: contain program errors =====
-- One Lua error inside a program stage must never reach the per-dispatch error
-- screen (it re-fires every few ticks -- "clicking continue takes me to the next
-- error forever"). Every stage of OUR programs is wrapped: on error the companion
-- idles for that dispatch and the real message goes to the console, once per
-- distinct error, prefixed [BanditsNPC] -- ask testers for exactly that line.
-- (Java method-DISPATCH errors still surface on the error screen -- pcall cannot
-- intercept those; the only defense is never calling engine methods with unverified
-- signatures. All engine calls in this mod were audited against vanilla/Bandits
-- usage after the getDecal arity bug.)
local reportedProgramErrors = {}
local function guardStage(pname, stage, fn)
    return function(bandit)
        local ok, ret = pcall(fn, bandit)
        if ok and type(ret) == "table" then return ret end
        if not ok then
            local key = tostring(ret)
            if not reportedProgramErrors[key] then
                reportedProgramErrors[key] = true
                print("[BanditsNPC] contained error in " .. pname .. "." .. stage .. ": " .. key)
            end
        end
        return {status = true, next = "Main", tasks = {}}
    end
end
-- OUR TEN BY NAME, NOT ANY "NPC*" (v0.75.48). This matched on the NPC prefix, so another
-- NPC mod registering e.g. NPCTrader before this file loads had its stages silently
-- wrapped by us: its errors would be SUPPRESSED (the net idles the companion for that
-- dispatch instead of letting the error surface) and REPORTED UNDER A [BanditsNPC] PREFIX,
-- so we would be blamed for a third party's crash and hide the evidence at the same time.
-- The coverage half was always right -- every one is inside the net, guaranteed by load
-- order. Naming them explicitly loses nothing and stops us touching code that is
-- not ours.
local OUR_PROGRAMS = {
    NPCAnimPlay = true, NPCCompanion = true, NPCDowned = true, NPCGuard = true,
    NPCNeutral = true, NPCRelax = true, NPCRoutine = true, NPCStay = true,
    NPCWork = true,
}

for pname, prog in pairs(ZombiePrograms) do
    if OUR_PROGRAMS[pname] and type(prog) == "table" then
        for stage, fn in pairs(prog) do
            if type(fn) == "function" then prog[stage] = guardStage(pname, stage, fn) end
        end
    end
end

-- ===== RE-ASSERT THE ENGINE HOOKS AT GAME START (v0.75.24) =====
--
-- Both hooks above latch at FILE LOAD and are never checked again. Any mod that loads
-- after us and assigns BanditUtils.AreEnemies or GetClosestBanditLocation wholesale --
-- rather than chaining -- silently replaces ours, and the symptoms are the ones that look
-- like our bugs: passive companions start being attacked again, downed ones get targeted,
-- two of your own companions fight each other.
--
-- Same defence the sleep gate uses against HDX_Strangers (BanditsNPCSleep.lua:208-213):
-- remember our function and put it back by IDENTITY at OnGameStart, which runs after every
-- file has loaded. If ours is still live, nothing happens. If someone WRAPPED us (their
-- function calls ours), the identity differs but replacing it would discard their wrapper
-- -- so this only restores when the current value is neither ours nor a wrapper we can
-- detect, which is the case that actually loses behaviour.
local function reassertHooks()
    if BanditsNPC._areEnemiesFn and BanditUtils.AreEnemies ~= BanditsNPC._areEnemiesFn then
        print("[BanditsNPC] BanditUtils.AreEnemies was replaced by another mod; restoring"
            .. " ours (passive/downed companions would otherwise be attacked).")
        BanditUtils.AreEnemies = BanditsNPC._areEnemiesFn
    end
    if BanditsNPC._closestFn and BanditUtils.GetClosestBanditLocation ~= BanditsNPC._closestFn then
        print("[BanditsNPC] BanditUtils.GetClosestBanditLocation was replaced by another"
            .. " mod; restoring ours (downed companions would otherwise be targeted).")
        BanditUtils.GetClosestBanditLocation = BanditsNPC._closestFn
    end
end

if Events.OnGameStart then Events.OnGameStart.Add(reassertHooks) end
