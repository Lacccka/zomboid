--
-- Bandits NPC - Companions Overhaul - Companion guardian (immortality)
--
-- Recruited companions are kept ALIVE but not invincible: every update we floor
-- their health so a hit can hurt them (they still bleed, limp, and show damage)
-- but can't drop them to a lethal value, and we strip any infection so they can't
-- zombify. Needs (hunger/fatigue/etc.) are untouched. Toggle with the sandbox
-- option "Companions can't be killed".
--
-- This also tends to save a companion from third-party mods that "kill" an NPC by
-- draining health. (A mod that outright Kill()s or removes the entity in one call
-- can't be stopped from here - see notes; that's a separate problem.)
--

local HEALTH_FLOOR_FRAC = 0.35   -- never let health fall below this fraction of max
local KNOCKDOWN_FRAC    = 0.30   -- below this, she goes DOWN rather than dying
-- Health a companion is helped up WITH. It MUST clear KNOCKDOWN_FRAC, or the knockdown
-- check below re-downs her on the very next tick -- see the v0.77.13 note. The margin is
-- deliberate rather than +0.01: it has to survive one tick of rounding and any small
-- drain before the counter-drain gets a turn, and 5% of max is still visibly hurt.
local HELPUP_FRAC       = KNOCKDOWN_FRAC + 0.05
-- Published so BanditsNPCCombat can use the SAME numbers rather than its own copies: the
-- two paths that can knock a companion down must agree on when (v0.75.27), and the path
-- that stands her back up must agree with both (v0.77.13).
BanditsNPC.Guardian = BanditsNPC.Guardian or {}
BanditsNPC.Guardian.KNOCKDOWN_FRAC = KNOCKDOWN_FRAC
BanditsNPC.Guardian.HELPUP_FRAC    = HELPUP_FRAC
local DOWNED_FLOOR_FRAC = 0.10   -- floor while already downed, so a mob cannot finish her

local function immortalOn()
    return (BanditsNPC.Opt and BanditsNPC.Opt("ImmortalCompanions", true)) ~= false
end

-- Opt-in mortality: when ON, a bite infection can genuinely turn a companion. The
-- reload/id-race zombify BUG stays fixed regardless -- a legit turn is recognized by
-- brain.infection >= 100 (the engine's own turn threshold), everything else is the bug.
local function zombificationAllowed()
    return (BanditsNPC.Opt and BanditsNPC.Opt("AllowZombification", false)) == true
end

-- ===== duplicate-companion cleanup =====
-- PZ's own zombie save/load can occasionally duplicate a zombie, leaving TWO copies of
-- the same recruited companion (identical bandit id + name) following you. Periodically
-- we scan loaded companions; if two share the same id AND name we remove the extra
-- (keeping the cached/canonical one). The survivor keeps the brain, so nothing is lost.
-- This does NOT bring back a companion that PZ culled/lost on load -- that's engine-side.
local lastDedup = 0
local function dedupCompanions()
    local now = (getTimestampMs and getTimestampMs()) or 0
    if now - lastDedup < 12000 then return end   -- at most once every 12s
    lastDedup = now
    local cell = getCell()
    local zlist = cell and cell.getZombieList and cell:getZombieList()
    if not zlist then return end

    local groups = {}
    for i = 0, zlist:size() - 1 do
        pcall(function()
            local z = zlist:get(i)
            if z and z:getVariableBoolean("Bandit") then
                local brain = BanditBrain and BanditBrain.Get(z)
                if brain and brain.recruited then
                    -- prefer the STABLE npcUid (also catches a restore-race double, which has
                    -- a different bandit id but the same uid); else fall back to id + name.
                    local key
                    if brain.npcUid then
                        key = "uid|" .. tostring(brain.npcUid)
                    else
                        local id = BanditUtils.GetCharacterID(z)
                        if id ~= nil then key = "id|" .. tostring(id) .. "|" .. tostring(brain.fullname or "") end
                    end
                    if key then
                        groups[key] = groups[key] or {}
                        table.insert(groups[key], { z = z, real = true })
                    end
                end
                -- (v0.60 REMOVED: the UNCLAIMED-SHELL branch. It grouped bodies by a uid
                -- parsed out of a "bnwsrestore_<uid>_<rand>" key, because the old
                -- async restore could leave shells nobody had claimed yet -- the "6 of
                -- the same person" pile-up. Persistence.BeginRestore now creates the
                -- body itself and holds the object, so such a shell can no longer be
                -- produced and this branch could only ever match leftovers from a
                -- pre-v0.60 save. Duplicates are still caught: any real companion has
                -- brain.npcUid and is grouped by it above.)
            end
        end)
    end

    for _, zs in pairs(groups) do
        if #zs > 1 then
            -- v0.60: NEVER keep a body that a restore is still holding when an older
            -- one is present. Persistence.BeginRestore creates its body and stores the
            -- object in P.pending; if the original streams back in during that window
            -- we have two, and despawning the ORIGINAL would leave the pending pointing
            -- at a body we just destroyed -- the restore then quietly expires and the
            -- whole cycle repeats a minute later. The original is the real one; drop
            -- ours. (Self-healing either way, but this stops the churn.)
            local pendingBodies = {}
            if BanditsNPC.Persistence and BanditsNPC.Persistence.pending then
                for _, pend in pairs(BanditsNPC.Persistence.pending) do
                    if pend.zombie then pendingBodies[pend.zombie] = true end
                end
            end
            local keep = nil
            for _, e in ipairs(zs) do
                if e.real and not pendingBodies[e.z] then keep = e.z; break end
            end
            -- everything in this group is a pending body: fall through and keep one
            if not keep then
                for _, e in ipairs(zs) do
                    if e.real then keep = e.z; break end
                end
            end
            if not keep and BanditZombie and BanditZombie.Cache then
                for _, e in ipairs(zs) do
                    local zid = BanditUtils.GetCharacterID(e.z)
                    if zid ~= nil and BanditZombie.Cache[zid] == e.z then keep = e.z; break end
                end
            end
            keep = keep or zs[1].z
            for _, e in ipairs(zs) do
                if e.z ~= keep then
                    -- If this was a restore's own body, retire the pending with it --
                    -- otherwise Persistence waits out the full 8s deadline on an object
                    -- that no longer exists before it will try again.
                    if pendingBodies[e.z] and BanditsNPC.Persistence then
                        for puid, pend in pairs(BanditsNPC.Persistence.pending) do
                            if pend.zombie == e.z then BanditsNPC.Persistence.pending[puid] = nil; break end
                        end
                    end
                    pcall(function() e.z:removeFromSquare() end)
                    pcall(function() e.z:removeFromWorld() end)
                end
            end
        end
    end
end

-- companions re-dressed this session, by stable npcUid (NOT by zombie object -- that would
-- pin streamed-out zombies in memory). See the redress block in onZombieUpdate.
local sessionRedressed = {}

-- "She has the base keys": when the engine wants a companion to ATTACK a door that
-- blocks her path (see the Destroy-task strip in onZombieUpdate), step her through to
-- the door's other side instead. The door's open/lock state is never touched -- no
-- security hole, no smashed base door; she "unlocks it and locks up behind herself".
local function passDoor(zombie, brain, task)
    if not (task.x and task.y) then return end
    local now = (getTimestampMs and getTimestampMs()) or 0
    if brain.npcDoorPassMs and now - brain.npcDoorPassMs < 3000 then return end
    local z = task.z or zombie:getZ()
    local sq = getCell():getGridSquare(task.x, task.y, z)
    if not sq then return end
    local door
    local objs = sq:getObjects()
    for i = 0, objs:size() - 1 do
        local o = objs:get(i)
        if o and (instanceof(o, "IsoDoor") or (instanceof(o, "IsoThumpable") and o:isDoor())) then door = o; break end
    end
    if not door then return end
    -- a north door separates (x, y-1) from (x, y); a west door separates (x-1, y) from (x, y)
    local north = false
    pcall(function() north = door:getNorth() end)
    local tx, ty = task.x, task.y
    if north then
        if zombie:getY() >= task.y then ty = task.y - 1 end
    else
        if zombie:getX() >= task.x then tx = task.x - 1 end
    end
    local target = getCell():getGridSquare(tx, ty, z)
    if not (target and target:isFree(false) and target:getFloor() ~= nil) then return end
    -- v0.60: routed through Nav.Teleport for the TRANSIT guard. The safety check
    -- above was already correct, but nothing stopped this from firing while she was
    -- mid-climb -- and popping her to the far side of a door during a window climb
    -- leaves the climb state running against a position it no longer owns.
    if not BanditsNPC.Nav.Teleport(zombie, target, "door pass") then return end
    brain.npcDoorPassMs = now
    pcall(function() zombie:playSound("WoodDoorOpen") end)
    brain.tasks = {}   -- path is stale from the new side; force a fresh dispatch
end

-- stuck-watchdog state per companion (session-local, keyed by stable npcUid so a
-- streamed-out zombie object is never pinned in memory)
local stuckState = {}

-- Action-state dead-end watchdog. Same keying and the same reason; the long account of
-- what these prefixes mean and why the release is safe is at the call site below.
-- (Declared HERE, above every use. A `local` below its first reference reads as a nil
-- global and has cost this mod three shipped crashes.)
local frozenSince = {}
local FROZEN_ACTION_PREFIXES = {
    "getup", "staggerback", "onground", "falldown",
    "hitreaction", "knockeddown", "vehiclecollision",
}

-- is this item type a firearm? cached per type -- the answer never changes and
-- the check runs inside the per-tick weapon watchdog
local rangedCache = {}
local function isRangedType(itemType)
    if rangedCache[itemType] == nil then
        local r = false
        pcall(function()
            local it = BanditCompatibility.InstanceItem(itemType)
            r = (it and it:IsWeapon() and it:isRanged()) == true
        end)
        rangedCache[itemType] = r
    end
    return rangedCache[itemType]
end

-- ===========================================================================
-- THE HEARTBEAT (v0.75.64)
-- ===========================================================================
--
-- This handler is already called on EVERY firing of Events.OnZombieUpdate for every
-- companion, which makes it the only place in the mod that can measure the thing five
-- builds of guesswork could not: HOW OFTEN SHE ACTUALLY GETS A TURN. Everything she
-- does -- Bandits' AI, the task state machine, her animations -- advances one step per
-- firing, so a companion who is merely starved of firings is indistinguishable, from
-- the outside, from one who is stuck. `tps` below tells those two apart in one glance.
--
-- The other four columns are here because each one is a live hypothesis:
--
--   coll=  isCollided/isCollidedThisFrame/isCollidedWithDoor. Bandits 42.20 added
--          `if collided then return false end` to ZAMove.onWorking, ABOVE the
--          pathfinder update -- so a collided companion is never stepped and cannot
--          move, while her Move task (time=15+ZombRand(10), BanditUtils.lua:1012)
--          expires anyway and the program issues another. That is a permanent
--          motionless loop, and it would look exactly like the author's report.
--   task=  which task is at the head of the queue and what state it is in. A task that
--          keeps restarting is a loop; one stuck in WORKING is a genuine hang.
--   stat=  Bandit.IsForceStationary, which Bandits feeds to setUseless.
--   tier=  whether the engine's tiered-zombie-updates option is on RIGHT NOW, rather
--          than whether we asked for it to be off once at startup.
--
-- Emitted once a second, so a two-hour session is a few thousand lines.
local tickCount, tickMark = {}, {}
local lastZ = {}

local function traceHeartbeat(zombie, brain)
    local T = BanditsNPC.Trace
    if not (T and T.On()) then return end
    local uid = brain.npcUid
    if not uid then return end

    tickCount[uid] = (tickCount[uid] or 0) + 1

    local sq
    pcall(function() sq = zombie:getCurrentSquare() end)
    if sq then T.Crumb(zombie, sq) end

    -- ===== ZFALL: a level change nobody asked for =====
    -- setX/setY/setZ move coordinates without re-registering the object on its new
    -- square, and an object the engine thinks is on a floorless square gets dropped.
    -- That was the v0.75.63 "fell through the floor, died, resurrected, looped" report.
    -- Two independent symptoms, both logged the instant they appear: an unexplained Z
    -- change, and getSquare() disagreeing with getCurrentSquare() -- which is the
    -- de-registered state itself, visible BEFORE she falls.
    pcall(function()
        local z = zombie:getZ()
        local prev = lastZ[uid]
        lastZ[uid] = z
        if prev ~= nil and math.abs(z - prev) > 0.45 and not BanditsNPC.Nav.InTransit(zombie) then
            T.Line(zombie, "ZFALL", "z " .. T.N(prev) .. " -> " .. T.N(z)
                .. " with no stair/climb state -- sq=" .. T.Sq(sq)
                .. " state=" .. tostring(zombie.getCurrentStateName and zombie:getCurrentStateName()))
            T.Dump(zombie, "zfall")
        end
        local plain = zombie:getSquare()
        if plain and sq and plain ~= sq then
            T.Change(zombie, "ZFALL", "desync", "getSquare=" .. T.Sq(plain)
                .. " but getCurrentSquare=" .. T.Sq(sq) .. " (she is not registered where she stands)")
        end
    end)

    local now = (getTimestampMs and getTimestampMs()) or 0
    local mark = tickMark[uid]
    if mark and now - mark < 1000 then return end
    local elapsed = mark and (now - mark) or 0
    local ticks = tickCount[uid]
    tickMark[uid] = now
    tickCount[uid] = 0

    pcall(function()
        local tps = (elapsed > 0) and (ticks * 1000 / elapsed) or -1
        local t = brain.tasks and brain.tasks[1]
        local taskStr = "none"
        if t then
            taskStr = tostring(t.action) .. "/" .. tostring(t.state or "NEW")
                .. " t=" .. T.N(t.time, 1)
            if t.x then taskStr = taskStr .. " ->" .. tostring(t.x) .. "," .. tostring(t.y) end
        end

        local c1, c2, c3
        if zombie.isCollided then c1 = zombie:isCollided() end
        if zombie.isCollidedThisFrame then c2 = zombie:isCollidedThisFrame() end
        if zombie.isCollidedWithDoor then c3 = zombie:isCollidedWithDoor() end

        local tier
        pcall(function() tier = getCore():getOptionTieredZombieUpdates() end)

        -- ===== THE PATHFINDER'S OWN ANSWER (v0.75.73) =====
        --
        -- Six builds blamed BumpedState for the freeze. It was never the cause:
        -- BumpedState.setCharacterBlockMovement is
        --     if (character instanceof IsoPlayer) ((IsoPlayer)c).setBlockMovement(b);
        -- jar-verified -- so it blocks PLAYERS and does nothing whatever to a zombie. The
        -- v0.75.64 correlation that started it all ("bump set = 0.000 tiles moved") was
        -- real but backwards: she has a work animation BECAUSE she is standing still doing
        -- work, not the other way round. I read a symptom as a cause and built five builds
        -- on it.
        --
        -- So this asks the only component that actually moves her what it thinks is going
        -- on: `path=0` means no route was found and she is being asked to walk somewhere
        -- unreachable; `path=n` with no movement means a route EXISTS and she is simply not
        -- following it, which is a completely different bug.
        --
        -- FIX(TASK-005): the `next=` column is GONE, and it never worked. It read
        -- `f.pathNextIsSet`, which javap confirms is a public FIELD on
        -- zombie.pathfind.PathFindBehavior2 with NO getter anywhere on the class -- unlike
        -- getPathLength() and getIsCancelled(), which are methods and do work. It returned
        -- nil in all 142 samples of the 8 Aug trace and printed "?" every time. pathNextX
        -- and pathNextY are fields too and would fail identically, so there is nothing to
        -- swap in; getPathLength() > 0 already answers "is there a route", which is what the
        -- column was for. Removing a diagnostic that never worked beats leaving one that
        -- silently answers nothing.
        local pathLen, cancelled, useless = nil, nil, nil
        pcall(function()
            local f = zombie:getPathFindBehavior2()
            if f then
                if f.getPathLength then pathLen = f:getPathLength() end
                if f.getIsCancelled then cancelled = f:getIsCancelled() end
            end
        end)
        pcall(function()
            if zombie.isUseless then useless = zombie:isUseless() end
        end)

        -- ===== THE STUCK ANCHOR, MADE VISIBLE (BUG-015 instrumentation) =====
        --
        -- The watchdog's three actions all key off stuckState[uid]: the anchor
        -- (st.x, st.y) and the dispatch count (st.ticks). Until now NONE of it
        -- was traceable, so every question about why the watchdog fired -- or
        -- did not -- was answered by INFERENCE from pos= plus a reading of the
        -- code. That inference was wrong at least once: the BUG-015 gate was
        -- believed to throttle the teleport loop in logs/probe-001-trace.txt,
        -- and reconstructing the anchor by hand showed it does nothing there.
        -- A number you have to derive from two other columns is a number people
        -- will derive incorrectly.
        --
        -- WHAT `drift` IS, AND WHY IT IS COMPUTED HERE RATHER THAN READ OFF.
        -- Both thresholds in the watchdog are PER-AXIS maxima, not Euclidean
        -- distance:
        --      UNSTICK_MOVED 0.25  (:836)  she counts as pinned below this
        --      the 1.2 anchor reset (:880) she counts as moving above this
        -- so the number that decides both is max(|x-st.x|, |y-st.y|). Eyeballing
        -- that from four separate columns on a 200-character line is exactly how
        -- a trace gets misread. One column, computed the same way the gates
        -- compute it, removes the arithmetic from the reader.
        --
        -- READ THE VALUES AS ONE DISPATCH STALE. traceHeartbeat runs at :364,
        -- BEFORE the anchor block at :871-885 updates st for this dispatch. So
        -- anchor/sticks/drift describe the state the watchdog SAW when it made
        -- this dispatch's decision -- which is what you want when asking "why
        -- did it fire" -- but they are not the values it leaves behind. Do not
        -- expect sticks to match a teleport count.
        --
        -- st is nil until her first dispatch and after any code that drops the
        -- entry, so every field goes through T.N/tostring rather than being
        -- indexed directly. T.N returns "?" for a non-number (Trace.lua:301-304),
        -- which is why a missing anchor prints "?" and not an error.
        local st = stuckState[uid]
        local drift
        if st and type(st.x) == "number" and type(st.y) == "number" then
            drift = math.max(math.abs(zombie:getX() - st.x), math.abs(zombie:getY() - st.y))
        end

        T.Line(zombie, "TICK",
            "pos=" .. T.N(zombie:getX()) .. "," .. T.N(zombie:getY()) .. "," .. T.N(zombie:getZ(), 1)
            .. " sq=" .. T.Sq(sq)
            .. " tps=" .. T.N(tps, 1)
            .. " coll=" .. T.B(c1) .. T.B(c2) .. T.B(c3)
            .. " state=" .. tostring(zombie.getCurrentStateName and zombie:getCurrentStateName() or "?")
            .. " bump=" .. tostring(zombie.getBumpType and zombie:getBumpType() or "?")
            .. " task=" .. taskStr
            .. " n=" .. tostring(brain.tasks and #brain.tasks or 0)
            .. " stat=" .. T.B(brain.stationary)
            .. " prog=" .. tostring(brain.program and brain.program.name or "-")
            .. " tier=" .. (tier and "ON" or "off")
            .. " path=" .. T.N(pathLen, 1)
            .. " pcancel=" .. T.B(cancelled)
            .. " useless=" .. T.B(useless)
            .. " anchor=" .. T.N(st and st.x) .. "," .. T.N(st and st.y)
            .. " drift=" .. T.N(drift)
            .. " sticks=" .. tostring((st and st.ticks) or "-"))
    end)
end

local function onZombieUpdate(zombie)
    pcall(dedupCompanions)
    local ok = pcall(function()
        if not (zombie and zombie.getVariableBoolean and zombie:getVariableBoolean("Bandit")) then return end
        local brain = BanditBrain and BanditBrain.Get(zombie)
        if not brain then return end

        pcall(function() traceHeartbeat(zombie, brain) end)

        -- ===== OUR-NPC BASICS (recruited or not) =====
        -- Applies to every bandit this mod manages: recruited companions, wild/neutral
        -- NPC-program survivors, and not-yet-adopted restore shells (key "bnwsrestore_*").
        local pname0 = brain.program and brain.program.name
        local isOurNpc = brain.recruited
            or (type(pname0) == "string" and pname0:sub(1, 3) == "NPC")
            or (type(brain.key) == "string" and brain.key:sub(1, 11) == "bnwsrestore")
        if isOurNpc then
            -- (a) NO ZOMBIE VOCALS. The engine mutes bandits inside its own update
            --     (Bandit.SurpressZombieSounds: voice prefix "NotAZombie" + stops the
            --     six Male/FemaleZombieVoiceA-C sounds), but several early-return paths
            --     in BanditUpdate skip it -- distant/uncached bandits among them --
            --     which is the "companions make zombie noises" report. Re-run the
            --     engine's own suppression here, plus the SPRINTER vocal variants its
            --     list doesn't cover. (stopSoundByName is engine-proven: Bandits calls
            --     it unguarded every tick; a stop on a non-playing name is a no-op.)
            if Bandit and Bandit.SurpressZombieSounds then
                pcall(function() Bandit.SurpressZombieSounds(zombie) end)
            end
            pcall(function()
                local emitter = zombie:getEmitter()
                if emitter and emitter.stopSoundByName then
                    emitter:stopSoundByName("FemaleZombieSprinterVoiceA")
                    emitter:stopSoundByName("FemaleZombieSprinterVoiceB")
                    emitter:stopSoundByName("MaleZombieSprinterVoiceA")
                    emitter:stopSoundByName("MaleZombieSprinterVoiceB")
                end
            end)
            -- (b) NEVER A REAL VEHICLE OCCUPANT. Any engine occupant iteration assumes
            --     IsoPlayer (getBodyDamage() NPE -> crash to menu on the first fender
            --     bender). Newer Bandits builds can SEAT bandits when their own vehicle
            --     option is on -- including our restore shells, which run the clan's
            --     ZPCompanion until adopted; a seated shell also drops out of the
            --     roster scans and P.Tick spawned copies of her (the "duplicates with
            --     both vehicle options on" report). Program-agnostic eviction, same
            --     proven exit sequence as the follow program's.
            pcall(function()
                local veh = zombie.getVehicle and zombie:getVehicle()
                if veh then
                    zombie:setVariable("BanditImmediateAnim", true)
                    local seat = veh:getSeat(zombie)
                    veh:exit(zombie)
                    if seat and seat >= 0 then
                        veh:setCharacterPosition(zombie, seat, "outside")
                    end
                    zombie:playSound("VehicleDoorClose")
                end
            end)
        end

        if not brain.recruited then return end

        -- ===== BRAIN SANITIZER (always-on) =====
        -- Saves made on OLDER mod versions can hand us brains with missing/renamed pieces.
        -- Instead of letting the engine's update loop error on them every tick ("my old
        -- save is corrupted"), repair the shape in place:
        --   * tasks must be a table (Bandit.AddTask/GetTask index it unguarded),
        --   * weapons must have the barehands shape (BanditBrain.IsOutOfAmmo indexes
        --     weapons.primary.bulletsLeft unguarded),
        --   * program must NAME A PROGRAM THAT EXISTS in ZombiePrograms, else the dispatch
        --     `ZombiePrograms[name][stage]` throws -- reset unknown ones to NPCCompanion.
        if type(brain.tasks) ~= "table" then brain.tasks = {} end
        if type(brain.weapons) ~= "table" then
            brain.weapons = { melee = "Base.BareHands",
                              primary = { bulletsLeft = 0, magCount = 0 },
                              secondary = { bulletsLeft = 0, magCount = 0 } }
        else
            if type(brain.weapons.primary) ~= "table" then brain.weapons.primary = { bulletsLeft = 0, magCount = 0 } end
            if type(brain.weapons.secondary) ~= "table" then brain.weapons.secondary = { bulletsLeft = 0, magCount = 0 } end
        end
        local progName = brain.program and brain.program.name
        if not (progName and ZombiePrograms and ZombiePrograms[progName]) then
            brain.program = { name = "NPCCompanion", stage = "Prepare" }
        end
        -- ONE re-dress per session: Bandits' provision-once ApplyVisuals only renders body
        -- locations from its curated list, so clothing on MODDED/custom locations (and any
        -- wear/undress done in a previous session) needs our unconditional redress on load.
        if brain.npcUid and not sessionRedressed[brain.npcUid]
           and BanditsNPC.Interact and BanditsNPC.Interact.ApplyClothingVisuals then
            local hasAny = false
            for _ in pairs(brain.clothing or {}) do hasAny = true; break end
            if hasAny then
                sessionRedressed[brain.npcUid] = true
                BanditsNPC.Interact.ApplyClothingVisuals(zombie, brain)
                -- ApplyClothingVisuals FILLS brain.npcClothVar; without this the
                -- captured appearance stayed on whichever client happened to run the
                -- session redress, and everyone else re-rolled her garment colours
                -- (v0.75.7, audit Cluster C). The two bag paths had the same gap.
                if Bandit and Bandit.ForceSyncPart then
                    pcall(function()
                        Bandit.ForceSyncPart(zombie, { id = brain.id, npcClothVar = brain.npcClothVar })
                    end)
                end
            end
        end
        -- purge NON-STRING clothing/tint keys: the pre-v0.35.6 wear bug stored B42
        -- ItemBodyLocation OBJECTS as keys -- they never render and can poison the
        -- cluster GlobalModData serialization at save time (collect first, then delete)
        for _, tbl in pairs({ brain.clothing, brain.tint }) do
            if type(tbl) == "table" then
                local bad
                for k in pairs(tbl) do
                    if type(k) ~= "string" then bad = bad or {}; bad[#bad + 1] = k end
                end
                if bad then for _, k in ipairs(bad) do tbl[k] = nil end end
            end
        end

        -- ===== ANTI-ZOMBIFY (regardless of the immortal toggle) =====
        -- A recruited companion must never revert to a hostile zombie BY ACCIDENT. There are
        -- TWO ways Bandits turns one back into a zombie; the reload/id-race one is a bug and
        -- is always blocked, the bite-infection one is a feature players can opt back into
        -- with the "Companions can zombify" sandbox option.
        local allowZ = zombificationAllowed()
        local turning = allowZ and (brain.infection or 0) >= 100   -- a LEGIT bite turn in progress
        --
        -- (1) CLUSTER MEMBERSHIP. BanditUpdate zombifies any Bandit-flagged zombie that is
        --     MISSING from its cluster GlobalModData (gmd[id] == nil -> Zombify(zombie)).
        --     The cluster is keyed by the live bandit id, so an id change on reload/reconnect
        --     leaves the brain orphaned and the engine zombifies her ("she zombified and
        --     attacked me"). Re-home the brain under the current id so that branch never
        --     fires -- EXCEPT while a legit turn is in progress (ZAZombify itself removes the
        --     cluster entry; re-homing would cancel the player's opted-in turn).
        if not turning then
            pcall(function()
                local id = BanditUtils and BanditUtils.GetCharacterID(zombie)
                if id and GetBanditClusterData then
                    local gmd = GetBanditClusterData(id)
                    if gmd and gmd[id] == nil then
                        gmd[id] = brain
                        if isServer and isServer() and TransmitBanditCluster then TransmitBanditCluster(id) end
                    end
                end
            end)
        end
        -- (2) BITE INFECTION. Bandits tracks it as the NUMBER brain.infection (climbs to 100,
        --     then queues a Zombify task). With zombification disallowed (default) keep it at
        --     zero, drop any legacy Infected tag, and cancel a queued turn. With it allowed,
        --     leave the infection alone and let the engine turn her.
        if not allowZ then
            brain.infection = 0
            if Bandit and Bandit.Expertise and brain.exp then
                local infId = Bandit.Expertise.Infected
                if infId then
                    for i = #brain.exp, 1, -1 do
                        if brain.exp[i] == infId then table.remove(brain.exp, i) end
                    end
                end
            end
            if brain.tasks then
                for i = #brain.tasks, 1, -1 do
                    if brain.tasks[i] and brain.tasks[i].action == "Zombify" then table.remove(brain.tasks, i) end
                end
            end
        end
        -- (3) FRIENDLY-FIRE HOSTILITY. BanditPlayer.CheckFriendlyFire flips EVERY friendly
        --     bandit within 12 tiles that saw a player hit one (hostileP=true + program
        --     "Bandit") -- including this companion herself, whose melee positioning (#50)
        --     routinely puts her inside the player's swing arc. A recruited companion must
        --     never turn on her side: clear the flags and restore the program the engine
        --     stomped. We shadow the last NPC* program each tick because the engine's flip
        --     doesn't save what it overwrote.
        local pname = brain.program and brain.program.name
        if pname and pname ~= "Bandit" and pname:sub(1, 3) == "NPC" and brain.npcLastProgram ~= pname then
            brain.npcLastProgram = pname
        end
        if brain.hostile or brain.hostileP then
            brain.hostile = false
            brain.hostileP = false
            if pname == "Bandit" or pname == nil then
                brain.program = { name = brain.npcLastProgram or "NPCCompanion", stage = "Prepare" }
            end
            if Bandit and Bandit.ForceSyncPart then
                pcall(function() Bandit.ForceSyncPart(zombie, { id = brain.id, hostile = false, hostileP = false, program = brain.program }) end)
            end
        end

        -- ===== COMPANIONS NEVER DAMAGE STRUCTURES (+ "she has the keys") =====
        -- BanditUpdate's collision handler queues {action="Destroy"} on ANY bandit whose
        -- path is blocked by a locked/obstructed door -- hostile or NOT -- and Breaker
        -- bandits rip barricades off. Testers: "she attacks the locked door on her
        -- patrol", "she breaks windows". Strip every structure-damaging task; when the
        -- blocker was a door, step her through instead (passDoor above).
        brain.demolish = nil   -- inherited from her bandit past; enables window-smashing
        do
            local doorTask
            for i = #brain.tasks, 1, -1 do
                local t = brain.tasks[i]
                local a = t and t.action
                if a == "Destroy" or a == "SmashWindow" or a == "Unbarricade" or a == "UnbarricadeMetal" then
                    if a == "Destroy" and not doorTask then doorTask = t end
                    table.remove(brain.tasks, i)
                end
            end
            if doorTask and not brain.downed then pcall(function() passDoor(zombie, brain, doorTask) end) end
        end

        -- ===== WEAPON WATCHDOG =====
        do
            -- (1) GUN PARKED IN THE MELEE SLOT (pre-v0.42 equips): the engine can only
            --     SWING weapons.melee -- melee damage with the gun's shot sound, and it
            --     never fires (firing needs a spec table in weapons.primary/secondary).
            --     Migrate it to its real gun slot once; mags she carries count right away.
            local melee = brain.weapons.melee
            if melee and melee ~= "Base.BareHands" and isRangedType(melee)
               and BanditsNPC.Interact and BanditsNPC.Interact.BuildGunSpec then
                local slot, spec
                pcall(function()
                    local it = BanditCompatibility.InstanceItem(melee)
                    if it then slot, spec = BanditsNPC.Interact.BuildGunSpec(it) end
                end)
                if slot and spec then
                    if brain.npcNoGuns then
                        -- melee-only mode: park the migrated gun in the stash instead
                        local stash = (type(brain.npcStashedGuns) == "table") and brain.npcStashedGuns or {}
                        if not (type(stash[slot]) == "table" and stash[slot].name) then stash[slot] = spec end
                        brain.npcStashedGuns = stash
                        brain.weapons.melee = "Base.BareHands"
                        brain.npcMelee = "Base.BareHands"
                        melee = "Base.BareHands"
                    else
                        -- don't stomp a slot that already holds a working gun
                        local cur = brain.weapons[slot]
                        if not (type(cur) == "table" and cur.name) then brain.weapons[slot] = spec end
                        brain.weapons.melee = "Base.BareHands"
                        brain.npcMelee = "Base.BareHands"
                        melee = "Base.BareHands"
                        if BanditsNPC.Interact.SyncAmmo then BanditsNPC.Interact.SyncAmmo(zombie, brain) end
                        if Bandit and Bandit.SetHands and brain.weapons[slot].name then
                            pcall(function() Bandit.SetHands(zombie, brain.weapons[slot].name) end)
                        end
                    end
                end
            end

            -- (2) MELEE YANK RE-ARM. ZASmack's floor-kill has a 25% "weapon stuck in the
            --     victim" event that sets the ATTACKER's brain.weapons.melee to
            --     Base.BareHands for good -- the crowbar the player gave her is simply
            --     gone after the fight ("she finished him with fists and stayed
            --     unequipped"). Shadow the last real melee type and give it back when the
            --     engine yanks it. Our Equip/Take UI updates the shadow too, so a
            --     deliberate unequip by the player is never fought.
            if melee and melee ~= "Base.BareHands" then
                brain.npcMelee = melee
            elseif brain.npcMelee and brain.npcMelee ~= "Base.BareHands" then
                brain.weapons.melee = brain.npcMelee
                -- never rip a gun out of her hands mid-firefight; the engine re-equips
                -- melee itself when an enemy gets close
                local busyFiring = false
                for _, t in pairs(brain.tasks) do
                    local a = t and t.action
                    if a == "Shoot" or a == "Aim" or a == "Rack" or a == "Load" or a == "Unload" then
                        busyFiring = true; break
                    end
                end
                if not busyFiring and Bandit and Bandit.SetHands then
                    pcall(function() Bandit.SetHands(zombie, brain.npcMelee) end)
                end
            end

            -- (3) FAILSAFE AMMO SWEEP. However mags/rounds landed in her bag (Give
            --     button, MP teammates, other mods), bank them into the virtual
            --     counters every ~10s so her gun never dry-fires with a full bag.
            local w = brain.weapons
            if (type(w.primary) == "table" and w.primary.name)
               or (type(w.secondary) == "table" and w.secondary.name) then
                local now = (getTimestampMs and getTimestampMs()) or 0
                if not brain.npcAmmoSyncMs or now - brain.npcAmmoSyncMs > 10000 then
                    brain.npcAmmoSyncMs = now
                    -- INFINITE AMMO (sandbox option): keep the virtual reserve topped up
                    -- so she reloads forever without being fed mags (tester ask, 7-8 Jul)
                    if BanditsNPC.Opt and BanditsNPC.Opt("InfiniteAmmo", false) == true then
                        for _, sn in ipairs({ "primary", "secondary" }) do
                            local g = w[sn]
                            if type(g) == "table" and g.name then
                                if g.type == "mag" then
                                    if (g.magCount or 0) < 3 then g.magCount = 3 end
                                elseif g.type == "nomag" then
                                    local size = g.ammoSize or 6
                                    if (g.ammoCount or 0) < size * 3 then g.ammoCount = size * 3 end
                                end
                            end
                        end
                    end
                    if BanditsNPC.Interact and BanditsNPC.Interact.SyncAmmo then
                        BanditsNPC.Interact.SyncAmmo(zombie, brain)
                    end
                end
            end
        end

        -- ===== STALE SLEEP FLAG / ORPHANED ROUTINE =====
        -- A companion is only ever asleep inside her NPCRoutine bed step
        -- (Bandit.SetSleeping sets brain.sleeping). If a reload/crash/interrupt left the
        -- flag on outside that program, she'd stay inert to the engine forever -- clear it.
        if brain.sleeping and not (brain.program and brain.program.name == "NPCRoutine") then
            brain.sleeping = false
        end
        -- While she IS asleep, the engine's self-care loop (ManageCombat "healing" ->
        -- {action="Bandage"}) keeps queueing tasks that fight the sleep hold -- she
        -- oscillates between lying and standing in the bed all night. Sleep first,
        -- bandage after waking.
        if brain.sleeping and brain.tasks then
            for i = #brain.tasks, 1, -1 do
                local t = brain.tasks[i]
                if t and t.action == "Bandage" then table.remove(brain.tasks, i) end
            end
        end
        -- An order/schedule change that bypassed NPCRoutine's finish() (manual order,
        -- schedule toggled off mid-routine) leaves routineTask set -- and MaybeStart
        -- refuses to start ANY future routine while it is. Clear the orphan.
        if brain.routineTask and brain.program and brain.program.name ~= "NPCRoutine" then
            brain.routineTask = false
            brain.routinePhase = nil
        end

        -- =====================================================================
        -- ACTION-STATE DEAD END: the freeze, and it is not ours (v0.77.13)
        -- =====================================================================
        --
        -- Seven builds looked for this in our code and in BumpedState. It is neither. It
        -- is four lines of Bandits, and they are load-bearing:
        --
        --   BanditUpdate.lua:392-394   elseif ClearTaskActionStates[asn] then
        --                                  Bandit.ClearTasks(bandit)
        --                                  return false
        --   BanditUpdate.lua:2094      if not continue then return end
        --
        -- While getActionStateName() is one of those ~60 states, Bandits FLUSHES HER TASK
        -- QUEUE AND ABANDONS THE ENTIRE UPDATE, every tick. No program dispatch, no task
        -- generation, no combat, no movement. There is no exit condition anywhere in that
        -- branch -- it assumes the engine's animation will leave the action state on its
        -- own, and when the animation stalls, nothing in Bandits will ever free her again.
        --
        -- That is the reported freeze, in all of its shapes at once: "frozen in place, no
        -- idle animation" (a stalled hitreaction/staggerback), "glitched into a ball on the
        -- ground until i talk to or bump into them" (onground/falldown -- and BUMPING HER
        -- IS THE FIX because a collision changes the action state, which is the reporter
        -- handing us the diagnosis), and "they leave cars and get stuck" (the six
        -- vehiclecollision-* states). It also explains why it hits some players and not
        -- others at random: it needs an animation to stall, not a feature to be used.
        --
        -- THE RELEASE IS BANDITS' OWN. It calls changeState(ZombieIdleState.instance())
        -- itself, three lines above the dead end (BanditUpdate.lua:381, the "turnalerted"
        -- branch), so the single-argument form is proven in shipping code on this exact
        -- kind of object rather than guessed at from a jar.
        --
        -- Matched by PREFIX, not by copying the table. Every one of its ~60 keys begins
        -- with one of these seven, so this is exact today and stays correct when Bandits
        -- adds another variant -- whereas a copied table would silently rot.
        --
        -- FOUR SECONDS, and never while downed. Every one of these animations is well
        -- under a second in normal play, so the threshold cannot fire on a companion who
        -- is merely being hit; and a DOWNED companion is inert ON PURPOSE (that is the
        -- knockdown), so she is left exactly where she is until someone helps her up.
        if brain.recruited and not brain.downed and not brain.sleeping then
            pcall(function()
                local asn = zombie.getActionStateName and zombie:getActionStateName()
                local frozen = false
                if type(asn) == "string" then
                    for _, p in ipairs(FROZEN_ACTION_PREFIXES) do
                        if asn:sub(1, #p) == p then frozen = true; break end
                    end
                end
                local uid = brain.npcUid
                if not frozen then
                    if uid then frozenSince[uid] = nil end
                    return
                end
                if not uid then return end
                local now = (getTimestampMs and getTimestampMs()) or 0
                if not frozenSince[uid] then frozenSince[uid] = now; return end
                if now - frozenSince[uid] < 4000 then return end
                frozenSince[uid] = now      -- re-arm, so a state that will not clear is
                                            -- retried every 4s rather than once ever
                zombie:changeState(ZombieIdleState.instance())
                print("[BanditsNPC] unfroze " .. tostring(brain.name)
                    .. " from a dead-end action state (" .. tostring(asn) .. ").")
            end)
        end

        -- ===== STUCK WATCHDOG =====
        -- Bandit pathfinding gives up ugly inside real player bases: a permanently-locked
        -- window is an endless bump loop (the engine's window handler has NO branch for
        -- it), and big multi-room bases can defeat the pather outright ("she stands at the
        -- window instead of using the door"). Count the time she spends STATIONARY while
        -- holding a Move/GoTo task -- the clock pauses while she idles on purpose (guard
        -- pauses, workstations) and resets whenever she actually moves. At ~9s flush her
        -- tasks for a fresh dispatch/path; at ~20s step her straight to the move
        -- destination ("she found her way").
        if brain.npcUid and not brain.downed and not zombie:getVehicle() then
            local moveTask
            for _, t in ipairs(brain.tasks) do
                local a = t and t.action
                if a == "Move" or a == "GoTo" then moveTask = t; break end
            end

            -- =============================================================
            -- THE RELEASE MOVED OUT OF HERE (v0.75.71)
            -- =============================================================
            --
            -- v0.75.65 to v0.75.70 tried to free her from BumpedState right here, and the
            -- 8 Aug trace shows every version of it failing: `state=BumpedState bump=ZSLoot
            -- task=none` for a whole session. Two reasons it could never have worked here.
            --
            -- FIRST, IT WAS GATED ON A MOVE TASK BEING AT THE HEAD OF THE QUEUE, and since
            -- v0.75.69 the work queue creates no tasks at all -- so on the exact jobs that
            -- needed it, this never ran once. Nor did it ever cover the case the author hit
            -- first: an UN-RECRUITED survivor freezing while walking over, with no task and
            -- no job anywhere in the picture.
            --
            -- SECOND, AND WORSE, v0.75.69 replaced setBumpType("") with
            -- postAnimationFinishing("BumpAnimFinished") -- I took out the one call that
            -- changed anything and put in one the state's animEvent handler does not act
            -- on. The bytecode is plain: BumpedState.execute() contains NO exit condition,
            -- and BumpedState.exit() is what CLEARS bumpType rather than what reacts to it.
            --
            -- (The release experiment and its harness were removed in v0.75.76 along
            -- with the work jobs: BumpedState was never the cause -- it blocks players,
            -- not zombies.)
            -- UNIVERSAL FURNITURE-UNSTICK: a positioned sit/sleep pins her onto the
            -- furniture, whose collision then blocks ANY order's first walk (Follow
            -- included: "she stood there a minute until she teleported to me"). The
            -- instant she wants to move while standing on a non-free square, pop her
            -- to an adjacent free tile -- program-agnostic, so no order can miss it.
            -- FIX(BUG-015): `st` is created HERE, above the unstick, rather than below it.
            -- The unstick now reads st.ticks and the anchor, and a nil guard would only be
            -- hiding the ordering rather than fixing it. This block depends on nothing the
            -- unstick does, so moving it up is behaviour-neutral.
            -- `local x, y` deliberately STAYS below the unstick: reading it above would
            -- capture her position BEFORE a teleport instead of after, which would shift
            -- when the anchor test below resets by one tick. The gate reads position itself.
            local now = (getTimestampMs and getTimestampMs()) or 0
            local st = stuckState[brain.npcUid]
            if not st then
                st = { x = zombie:getX(), y = zombie:getY(), acc = 0, ticks = 0, last = now }
                stuckState[brain.npcUid] = st
            end

            -- FIX(BUG-015): THIS PATH USED TO FIRE ON A COMPANION WHO WAS WALKING FINE.
            --
            -- It was the only one of the three watchdog actions with NO qualification at
            -- all -- no budget, no task check, no progress check -- so it teleported on any
            -- tick where she held a Move task and her square was not free. The 8 Aug trace
            -- caught it: she covered ~3 tiles in the preceding second and her path had
            -- shortened 7.6 -> 4.6, and it yanked her ~0.85 tiles sideways anyway.
            --
            -- WHY IT MISFIRES: isFree(false) means "no solid STATIC content" (the boolean
            -- gates the moving-objects test, so occupants are ignored -- v0.76.3 bytecode
            -- note). Doorways, furniture-adjacent tiles and cluttered interiors are all
            -- not-free, and are crossed constantly during ordinary movement. The check
            -- could not tell "pinned on furniture and unable to begin" -- the STATIONARY
            -- case it exists for -- from "walking across a non-free square".
            --
            -- DISPLACEMENT IS THE DISCRIMINATOR, NOT THE TICK COUNT. A tick count alone
            -- cannot separate a slow walker from a pinned one: a limping companion at 60fps
            -- can spend a hundred dispatches without clearing the 1.2-tile anchor. But a
            -- PINNED companion does not move at all, while any walker's displacement grows
            -- immediately. So the gate is "has not gone anywhere", not "has been a while".
            --
            -- THE BACKSTOP IS NOT OPTIONAL. If a pinned companion jitters against the
            -- furniture by more than UNSTICK_MOVED, the displacement clause would never
            -- fire and she would be pinned for ever -- which is the failure this whole path
            -- exists to prevent. Past UNSTICK_BACKSTOP dispatches it acts regardless, so
            -- stranding is impossible by construction rather than by calibration.
            --
            -- COUNTED IN DISPATCHES, like the budgets below. Worst case for a genuinely
            -- pinned companion is UNSTICK_TICKS dispatches: ~0.25s at 60 tps, ~4s at the
            -- 3.7 tps the trace recorded under tiered updates.
            local UNSTICK_TICKS    = 15     -- dispatches of going nowhere before we act
            local UNSTICK_MOVED    = 0.25   -- tiles; beyond this she is walking, not pinned
            local UNSTICK_BACKSTOP = 240    -- dispatches; act regardless, jitter-proof
            if moveTask then
                pcall(function()
                    local px, py = zombie:getX(), zombie:getY()
                    local stillHere = math.abs(px - st.x) < UNSTICK_MOVED
                                  and math.abs(py - st.y) < UNSTICK_MOVED
                    local pinned = st.ticks >= UNSTICK_BACKSTOP
                                or (st.ticks >= UNSTICK_TICKS and stillHere)
                    if not pinned then return end
                    local sq = zombie:getSquare()
                    if sq and not sq:isFree(false) then
                        -- FIXED (v0.60): was setX/setY with NO setZ and NO floor
                        -- check. A stair square is not `isFree`, so this fired on
                        -- staircases, slid her sideways while she kept a fractional
                        -- stair Z, and left her standing over nothing -> fall damage
                        -- -> downed. This was the primary cause of "companions fall
                        -- down the stairs". Nav.Teleport refuses an unsafe target AND
                        -- refuses to move her at all while she is in transit.
                        local off = AdjacentFreeTileFinder.Find(sq, zombie)
                        if off then BanditsNPC.Nav.Teleport(zombie, off, "unstick: square not free") end
                    end
                end)
            end
            local x, y = zombie:getX(), zombie:getY()
            -- v0.60 TRANSIT GUARD. A companion crossing a staircase or climbing a
            -- window barely moves in X/Y for several seconds, which reads as
            -- "stuck" to the anchor test below -- and the 9s branch flushes her
            -- task queue mid-climb while the 20s branch tries to teleport her.
            -- Nav.Teleport already refuses the move, but the task flush would still
            -- fire and cancel the traversal she was in the middle of. She is not
            -- stuck; reset the anchor and let her finish.
            -- (chained into the anchor test below rather than an early return --
            -- returning here would skip the SURROUNDED -> DOWNED interception that
            -- follows, which is what keeps a mobbed companion alive.)
            if BanditsNPC.Nav.InTransit(zombie) then
                -- FIX(BUG-023C): goalN/runN go with ticks. A stale goal from a previous
                -- struggle would otherwise out-count the new one and win the teleport.
                st.x, st.y, st.acc, st.ticks, st.flushed, st.goalN, st.runN = x, y, 0, 0, nil, nil, nil
            -- ANCHOR displacement, not a per-tick delta: bumping into a tree/fence
            -- oscillates her by about half a tile each way, which reset the old 0.4
            -- check forever -- the watchdog never fired and she bounced off the same
            -- obstacle indefinitely ("companions get stuck bumping into objects,
            -- travelling through forests is difficult", report 11 Jul). She only
            -- counts as MOVING once she gets >1.2 tiles from where the struggle
            -- started; anyone genuinely walking crosses that in about a second.
            elseif math.abs(x - st.x) > 1.2 or math.abs(y - st.y) > 1.2 then
                -- really moved: clean slate, including the one-shot climb attempt
                -- and the FIX(BUG-023C) destination tally
                st.x, st.y, st.acc, st.ticks, st.flushed, st.climbTried, st.goalN, st.runN = x, y, 0, 0, nil, nil, nil, nil
            elseif moveTask and moveTask.x then
                st.acc = st.acc + math.max(0, now - st.last)        -- stationary but wants to move
                st.ticks = st.ticks + 1

                -- FIX(BUG-023C): REMEMBER WHICH DESTINATION ACTUALLY SPENT THE BUDGET.
                --
                -- The teleport below used to aim at `moveTask` -- the task at the head of the
                -- queue on the dispatch it happened to fire. That is NOT the destination the
                -- 1200 ticks were counted against. Measured over every trace in logs/: in 10
                -- of 22 past-FLUSH accumulations the counted destination changes partway, and
                -- in logs/task012-trace.txt it changed inside the last 0.4 SECONDS -- the head
                -- task read `->417.5,9750.5` at 001260.4 (:1140) and the teleport 0.4s later
                -- used 410.42,9761.88 (:1141), a destination that appears in NO tick of that
                -- 96-second freeze. She was moved somewhere she had never been going, and was
                -- back on `->417.5,9750.5` eight seconds later (:1151).
                --
                -- SO: record the destination that holds the LONGEST UNBROKEN STRETCH of
                -- counted dispatches. That is the one she demonstrably could not reach from
                -- here, and it is what the rescue should aim at. On the two freezes we can
                -- check it picks 417.5,9750.5 (5622 of 5739 ticks) and 332.5,9691.5 (7022 of
                -- 7111) -- in both cases the destination she went straight back to.
                --
                -- TWO THINGS THIS DELIBERATELY IS NOT, both rejected on measurement:
                --   * "record moveTask on every increment and read it back" is a NO-OP. The
                --     rung below runs in the SAME iteration as this increment, so the stored
                --     value would always equal `moveTask` and nothing would change.
                --   * "reset st.ticks when the destination changes" DISARMS THE RESCUE. One
                --     of the 22 runs (task012, eight destinations in 21 samples) never
                --     assembles 540 consecutive ticks against any single destination, so the
                --     watchdog would never fire at all -- and the traces sample at 1Hz against
                --     ~60Hz dispatches, so real churn is worse than what is visible. The
                --     budget must go on counting "she is not moving"; only the AIM is
                --     per-destination.
                if moveTask.x == st.runX and moveTask.y == st.runY then
                    st.runN = (st.runN or 0) + 1
                else
                    st.runX, st.runY, st.runZ, st.runN = moveTask.x, moveTask.y, moveTask.z, 1
                end
                if st.runN > (st.goalN or 0) then
                    st.goalX, st.goalY, st.goalZ, st.goalN = st.runX, st.runY, st.runZ, st.runN
                end

                -- IS SHE STUCK AGAINST SOMETHING SHE COULD CLIMB?
                --
                -- This is where the traversal half of BanditsNPCNav finally gets used.
                -- It was written in v0.60 and then never called by anything -- the
                -- module's safety half (IsSafeSquare, Teleport, InTransit) was wired in
                -- but ClassifyEdge and BeginTraverse were dead code, which is why
                -- windows never improved: a companion pressed against one had no way to
                -- ask for a climb, so she shoved at the glass until the watchdog below
                -- teleported her through it. That is the reported "she tried to enter
                -- the base through a window and after a while just got teleported".
                --
                -- Tried EARLY, at 3 seconds, and only once per struggle: a climb is the
                -- cheap correct answer and it should happen long before anything
                -- considers moving her by hand. If it is issued, InTransit goes true and
                -- every position-asserting path in the mod stands off until she lands.
                -- FIX(TASK-012): ONE TRACE LINE PER ATTEMPT, WHATEVER HAPPENS.
                --
                -- This used to announce only SUCCESS, and only with a `print`, while every
                -- other event in this ladder -- FLUSH, TELEPORT, ZFALL -- goes through
                -- Trace. That asymmetry cost a whole test run: BUG-033 caught a companion
                -- frozen outside a window she had just climbed out of, and the one fact
                -- needed to tell "the climb was issued and the engine ignored it" from
                -- "the climb was never issued" existed only in a console log nobody had
                -- captured. The informative half of a rescue is the half where it DECLINES,
                -- so the decline path is now louder than the success path, not silent.
                --
                -- EVERY attempt emits exactly one CLIMB line. `why` is assigned before each
                -- early return rather than at it, so a bail-out cannot escape unnamed and
                -- the reason is always the last gate actually reached.
                --
                -- HOW TO READ kind= AND obj= -- this is the U-015 answer, in the trace.
                -- ClassifyEdge returns (kind, obj). The three getWindow*To branches
                -- (Nav.lua:303-310) set BOTH; the isWindowTo fallback at Nav.lua:312 sets
                -- only kind. BeginTraverse then requires `kind == WINDOW and obj`
                -- (Nav.lua:374), so:
                --     kind=window obj=1  -> a handle was obtained; a refusal after this is
                --                           the engine's, i.e. BUG-010 territory
                --     kind=window obj=0  -> the fallback fired and BeginTraverse silently
                --                           dropped it. getWindowTo did not answer for this
                --                           direction -- U-015 resolved, and the Nav.lua:312
                --                           hole is live
                --     kind=blocked       -> she is shoving at something that is not a window
                -- getWindowTo has NO precedent anywhere in refs/, so this pair is the only
                -- evidence about it we can collect without a hand-run console probe.
                --
                -- FIX(BUG-037): ON A DIAGONAL, PROBE BOTH CARDINALS. NEVER GUESS.
                --
                -- This used to collapse a diagonal onto the LONGER axis and classify that
                -- edge alone. Measured failure, BUG-036's trace: she stood at
                -- (331.50, 9688.46) wanting (330.5, 9687.5), so |dx| = 1.00 and |dy| = 0.96,
                -- and `1.00 >= 0.96` sent the probe WEST. It reported kind=open, twice,
                -- while she spent 22 seconds shoving at the shed's NORTH wall -- proven by
                -- this same instrumentation after the watchdog teleported her inside:
                -- `edge 330,9687,0 -> 330,9688,0 kind=blocked`. A four-hundredth of a tile
                -- chose the wrong wall.
                --
                -- THAT IS WORSE THAN NO DIAGNOSTIC. A missing CLIMB line reads as "unknown";
                -- `kind=open` reads as "there is nothing there", which is a positive claim,
                -- and it is the claim that made BUG-033's window edge look absent when it
                -- was simply on the other axis. A probe that can lie is not a probe.
                --
                -- So both neighbours are classified and the answer is chosen by what was
                -- actually FOUND, not by arithmetic on the approach vector: something
                -- climbable outranks a wall, a wall outranks open air. Open air is the one
                -- verdict that can never explain why she is stuck, so it loses every tie it
                -- is in. Only when both sides agree does the longer axis break the tie,
                -- which is exactly the old behaviour in the only case it was ever right.
                --
                -- BOTH VERDICTS GO IN THE LINE, as `diag[x=.. y=.. took=..]`. The next
                -- person reading a declined climb needs to see the CHOICE, not just its
                -- outcome -- that is the whole reason this bug survived a test run.
                --
                -- Cost: one extra ClassifyEdge, once per struggle, on diagonals only.
                --
                -- BUG-010 PROBE: READ THE THREE PRECONDITIONS BOTH PRECEDENTS GATE ON.
                --
                -- 2026-08-19 the rescue issued a climb with a real IsoWindow handle
                -- (`kind=window obj=1`) and NOTHING HAPPENED -- no state change, no Z
                -- change, no movement, then FLUSH and TELEPORT. `climbThroughWindow` was a
                -- no-op. The entry was opened on an overload-dispatch theory; the evidence
                -- does not support it, because `obj` came from the getWindowTo branch and
                -- is an IsoWindow, which is the overload that should bind.
                --
                -- WHAT THE EVIDENCE DOES SUPPORT IS THAT WE INVENTED THE CALL. There is no
                -- precedent for `climbThroughWindow` on a ZOMBIE receiver anywhere in
                -- refs/. Bandits does not call the method at all -- it changes the state
                -- (BanditUpdate.lua:683-686) -- and its whole window block sits inside
                -- `if bandit:isFacingObject(object, 0.5)` (:603). The method form exists
                -- only on a PLAYER, inside a TimedAction that faces first and waits for the
                -- turn to finish (ISClimbThroughWindow.lua:18-22, :40), and the engine's own
                -- direct call at ISObjectClickHandler.lua:187 is COMMENTED OUT.
                --
                -- SO THE THREE THINGS BOTH PRECEDENTS CHECK GO IN THE LINE, and each has a
                -- precedent on this exact receiver pair (IsoWindow / bandit):
                --     can=     object:canClimbThrough(bandit)      BanditUpdate.lua:683
                --     facing=  bandit:isFacingObject(object, 0.5)  BanditUpdate.lua:603
                --     open=    object:IsOpen()                     BanditUpdate.lua:672
                --
                -- HOW TO READ THE RESULT -- this is the whole point of the probe:
                --     facing=0  -> she is not facing the window. Both climb paths, ours AND
                --                  Bandits' own, are gated on this, which would explain why
                --                  NEITHER fired. Fix = face first, and `st.climbTried` has
                --                  to become a retry budget because the precedent faces on
                --                  one tick and climbs on a later one.
                --     facing=1 can=1 open=1 and ISSUED and she still does not move
                --               -> preconditions were met and the MECHANISM is wrong. Fix =
                --                  ClimbThroughWindowState + changeState, per Bandits.
                --     can=0     -> the engine itself says this window is not climbable, and
                --                  the bug is upstream in ClassifyEdge offering it.
                --     any of them `?` -> the method is not on that receiver. That is a
                --                  citation failure, not a measurement; log it in UNKNOWNS.
                --
                -- Read once per struggle, each in its own pcall so one missing method
                -- cannot hide the other two answers. Existence-guarded the same way the
                -- collision flags are at :295-297.
                --
                -- ClassifyEdge is called here AND again inside BeginTraverse. Deliberate:
                -- BeginTraverse returns only a boolean, and asking it to report its verdict
                -- would mean changing Nav.lua for a diagnostic. It is a pure query under
                -- pcall, once per struggle.
                if st.acc > 3000 and not st.climbTried then
                    st.climbTried = true
                    local T = BanditsNPC.Trace
                    local why, kind, obj, edge = "declined: no nav/square/cell", nil, nil, "?"
                    local diag, gate = "", ""
                    pcall(function()
                        local Nv = BanditsNPC.Nav
                        local here = zombie:getCurrentSquare()
                        local cell = getCell()
                        if not (Nv and here and cell) then return end
                        -- Step one tile from her position toward the move target: that
                        -- is the edge she is failing to cross.
                        local dx = (moveTask.x > x and 1) or (moveTask.x < x and -1) or 0
                        local dy = (moveTask.y > y and 1) or (moveTask.y < y and -1) or 0
                        why = "declined: target square is her own"
                        if dx == 0 and dy == 0 then return end

                        -- ClassifyEdge is defined on the shared N/W edge between two
                        -- ADJACENT squares, so a diagonal has no single edge to ask about.
                        -- Ask about both. rank() decides which answer is worth reporting.
                        local hx, hy, hz = here:getX(), here:getY(), here:getZ()
                        -- FIX(BUG-040): RANK WHAT SHE CAN DO, NOT WHAT THE EDGE IS.
                        --
                        -- This scored every WINDOW a 2 on the strength of its KIND, so a
                        -- SHUT window outranked a walkable OPEN edge on the other axis and
                        -- the rescue spent its one attempt on it -- measured at
                        -- 000299.6, `diag[x=open y=window took=y]` with `can=0 open=0`.
                        --
                        -- "Prefer the open edge" is the tempting correction and it is
                        -- wrong: this rescue's only verbs are climb and vault, it cannot
                        -- walk her anywhere, so preferring OPEN would mean always
                        -- declining and there would be no rescue at all. The narrow, true
                        -- statement is that an UNCROSSABLE window is not an actionable
                        -- edge. It is a wall she cannot pass, which is what BLOCKED means,
                        -- so it is ranked as one -- still above OPEN, because it is still
                        -- the honest answer to "what is she stuck on".
                        --
                        -- A FENCE has no object, so `o` is nil and it keeps its 2: a fence
                        -- has no open/shut state to ask about.
                        local function rank(k, o)
                            if k == Nv.EDGE.WINDOW or k == Nv.EDGE.FRAME
                               or k == Nv.EDGE.THUMP or k == Nv.EDGE.FENCE then
                                local can = true
                                if o and o.canClimbThrough then
                                    can = false
                                    pcall(function() can = o:canClimbThrough(zombie) end)
                                end
                                return can and 2 or 1
                            end
                            if k == Nv.EDGE.BLOCKED then return 1 end
                            return 0                        -- open, stairs: not why she is stuck
                        end
                        -- -1 means "no square that way at all", which is not a verdict and
                        -- must never win a comparison against one.
                        local function probe(sx, sy)
                            if sx == 0 and sy == 0 then return nil, nil, nil, -1 end
                            local sq = cell:getGridSquare(hx + sx, hy + sy, hz)
                            if not sq then return nil, nil, nil, -1 end
                            local k, o = Nv.ClassifyEdge(here, sq)
                            return sq, k, o, rank(k, o)
                        end
                        local sqX, kX, oX, rX = probe(dx, 0)
                        local sqY, kY, oY, rY = probe(0, dy)

                        local useY
                        if rX < 0 then useY = true
                        elseif rY < 0 then useY = false
                        elseif rY ~= rX then useY = rY > rX
                        else useY = math.abs(moveTask.y - y) > math.abs(moveTask.x - x) end

                        -- Assigned as a triple, NOT with `useY and kY or kX`: obj is nil for
                        -- an open edge, and the and/or idiom would then silently report the
                        -- OTHER axis's object next to this axis's kind.
                        local nextSq
                        if useY then nextSq, kind, obj = sqY, kY, oY
                        else nextSq, kind, obj = sqX, kX, oX end
                        if dx ~= 0 and dy ~= 0 then
                            diag = " diag[x=" .. tostring(kX) .. " y=" .. tostring(kY)
                                .. " took=" .. (useY and "y" or "x") .. "]"
                        end
                        edge = (T and T.Sq(here) or "?") .. " -> " .. (T and T.Sq(nextSq) or "?")
                        why = "declined: no square across that edge"
                        if not nextSq then return end

                        -- BUG-010 probe. BEFORE the attempt, so the reading describes the
                        -- state the climb was issued from and not whatever it left behind.
                        --
                        -- `can=` IS THE VERDICT. THE OTHERS EXIST TO EXPLAIN IT.
                        --
                        -- A WINDOW HAS FOUR INDEPENDENT STATES and only some of them bear
                        -- on climbing. Bandits' chain is the authority, BanditUpdate.lua:
                        -- 672-683, in order:
                        --     not IsOpen() and not isSmashed() -> smash it, or if
                        --                                         not isPermaLocked()
                        --                                         queue an OpenWindow task
                        --     elseif canClimbThrough(bandit)   -> climb
                        -- So `open=0` ALONE MEANS NOTHING. A smashed or glass-removed
                        -- window reports IsOpen() == false and is climbable anyway, which
                        -- is why `smashed=` is here: without it, `open=0 can=1` looks like
                        -- a contradiction instead of the ordinary case it is.
                        --
                        -- `perma=` DOES NOT GATE CLIMBING -- it gates OPENING (:677), and
                        -- it is read because it answers the next question rather than this
                        -- one: when `can=0 open=0 smashed=0`, perma=1 says nobody will ever
                        -- open this window and the destination is climb-impossible, while
                        -- perma=0 says Bandits' own opener could have run and did not,
                        -- which is a different bug (see BUG-010's open thread).
                        --
                        -- NOT READ: `isLocked()`. It is a real state (DebugContextMenu.lua
                        -- :357, :853) but it bears on neither climbing nor opening in the
                        -- chain above, so it would be a column that explains nothing. Add
                        -- it only to confirm what a debug toggle actually did.
                        --
                        -- `barricaded=` IS BUG-045's COLUMN, AND IT READS THE ONE STATE IN
                        -- BANDITS' CHAIN THIS PROBE WAS MISSING.
                        --
                        -- It is the FIRST branch of the window chain, above everything the
                        -- other columns explain: `if object:isBarricaded() then` at
                        -- BanditUpdate.lua:605, which then splits on `if brain.hostile`
                        -- (:606). A COMPANION IS NEVER HOSTILE, so she takes the else at
                        -- :669-670 -- and that calls `RemoveWindowFromPathing`, which begins
                        -- `if true then return end` (BanditUpdate.lua:484-485) and is a
                        -- DISABLED STUB. No unbarricade task, no destroy task, no OpenWindow
                        -- task, no climb, no pathing repair. Nothing, every tick, forever.
                        --
                        -- So `barricaded=1` on a CLIMB line means Bandits was never going to
                        -- act on this window no matter how long she stands there, and the
                        -- watchdog teleport is the ONLY thing that can move her. That is a
                        -- different diagnosis from every other combination this line can
                        -- print, and players barricade windows deliberately, so it is worth
                        -- a column of its own rather than an inference.
                        -- Precedent: `object:isBarricaded()` at BanditUpdate.lua:605, same
                        -- receiver type (an IsoWindow) as `obj` here.
                        if obj and T then
                            local cc, fo, op, sm, pl, br
                            if obj.canClimbThrough then
                                pcall(function() cc = obj:canClimbThrough(zombie) end)
                            end
                            if zombie.isFacingObject then
                                pcall(function() fo = zombie:isFacingObject(obj, 0.5) end)
                            end
                            if obj.IsOpen then
                                pcall(function() op = obj:IsOpen() end)
                            end
                            if obj.isSmashed then
                                pcall(function() sm = obj:isSmashed() end)
                            end
                            if obj.isPermaLocked then
                                pcall(function() pl = obj:isPermaLocked() end)
                            end
                            if obj.isBarricaded then
                                pcall(function() br = obj:isBarricaded() end)
                            end
                            gate = " can=" .. T.B(cc) .. " facing=" .. T.B(fo)
                                   .. " open=" .. T.B(op) .. " smashed=" .. T.B(sm)
                                   .. " perma=" .. T.B(pl) .. " barricaded=" .. T.B(br)
                        end
                        if Nv.BeginTraverse(zombie, here, nextSq) then
                            st.acc = 0                      -- she is doing something now
                            why = "ISSUED: climbing instead of shoving"
                        else
                            why = "declined: BeginTraverse refused"
                        end
                    end)
                    if T and T.On() then
                        T.Line(zombie, "CLIMB", why .. " -- edge " .. edge
                            .. " kind=" .. tostring(kind) .. " obj=" .. T.B(obj ~= nil)
                            .. gate .. diag
                            .. " want=" .. tostring(moveTask.x) .. "," .. tostring(moveTask.y))
                    end
                end

                -- WALL-CLOCK IS NOT ENOUGH ON ITS OWN (v0.75.59).
                --
                -- `acc` counts REAL milliseconds, but a companion only makes progress on her
                -- own update ticks -- this handler and the whole of Bandits' AI hang off
                -- Events.OnZombieUpdate, and her Move task advances one pathfinder step per
                -- firing. When those firings are throttled the two clocks come apart: twenty
                -- real seconds can be twenty ticks instead of six hundred, so she has barely
                -- had a chance to take a step and the watchdog teleports her anyway. Then she
                -- works, is pinned again, and is teleported again.
                --
                -- THIS THROTTLING IS REAL AND IT IS NOT OURS: Bandits turns the engine's
                -- tiered zombie updates ON whenever it caches 50+ zombies or 50+ bandits
                -- (BanditZombie.lua:151-156), above its own comment "without this enforcement,
                -- bandits can only move and not do any other action while outside of player
                -- view -- but it tanks performance, so only enable if necessary". The author's
                -- log has that line every few seconds for the entire session, at 100-110
                -- zombies. (Measured later at only ~6% of her tick rate, and the module
                -- that fought it was removed in v0.75.76.)
                --
                -- So both clocks must agree before anything drastic happens. At a healthy tick
                -- rate the tick budget is met long before the time budget and behaviour is
                -- exactly what it was; when she is starved, the watchdog waits instead of
                -- teleporting a companion who is not actually stuck. Genuinely stuck companions
                -- keep ticking normally, so they are caught as before.
                --
                -- IT CAN BE TURNED OFF, AND THAT IS AN EXPERIMENT (v0.75.64). Sandbox
                -- "Stuck watchdog", ON by default so normal play is unchanged. With it
                -- OFF a session answers a question nothing else can: if she still freezes
                -- but stops teleporting, the freeze is real and this was only ever
                -- reacting to it; if she frees herself instead, this was the thing making
                -- it worse. Five builds have argued that both ways with no evidence.
                --
                -- NOT AN EARLY RETURN. This is inside the pcall that also carries the
                -- SURROUNDED -> DOWNED interception below, and returning from here would
                -- skip it -- which is what keeps a mobbed companion alive. Same trap the
                -- transit guard above documents; the switch is a condition, not a jump.
                local watchdogOn = BanditsNPC.Opt("DbgWatchdog", true)

                -- FIX(BUG-002): BOTH BUDGETS ARE NOW COUNTED IN TICKS, NOT MILLISECONDS.
                --
                -- THESE NUMBERS ARE DISPATCHES, NOT SECONDS. 540 is nine seconds' worth of
                -- ticks at 60fps (9 x 60); 1200 is twenty seconds' worth (20 x 60). Do not
                -- read them as times.
                --
                -- WHY. The old pair was `st.acc > 9000ms and st.ticks > 60`, and the tick
                -- half never discriminated: st.ticks is CUMULATIVE across the struggle while
                -- 60 is one second's worth at 60fps, so after nine seconds it is satisfied at
                -- any rate above 6.64/sec. The 8 Aug trace caught it flushing a companion
                -- whose rate had collapsed to 3.7/sec -- 237 ticks in 9036ms, average 26.2 --
                -- while a path existed (path=2.0, pcancel=0) and the target was 1.75 tiles
                -- away. She was STARVED OF UPDATE TICKS, not stuck: the tick rate recovered
                -- one second after the flush and she walked off immediately.
                --
                -- The starvation is not ours: Bandits turns the engine's tiered zombie
                -- updates on at 50+ cached zombies (BanditZombie.lua:151-156), and that
                -- session ran tier=ON throughout.
                --
                -- Counting purely in HER OWN dispatches makes starvation a non-case by
                -- construction: a starved companion banks them slowly and simply takes
                -- proportionally longer in wall-clock to reach the threshold, which is the
                -- correct behaviour rather than a special case to detect. This is also why
                -- the budget must NOT be normalised the way Bandits normalises task.time --
                -- that normalisation is against game FPS, which is exactly the quantity that
                -- decouples from her update rate under tiered updates (see the note at the
                -- flush below). THE COST, ACCEPTED: at 3.7 ticks/sec, 540
                -- ticks is about 146 real seconds before a flush. A genuinely stuck companion
                -- on a heavily loaded server waits far longer than she used to. That is the
                -- price of not acting on false positives, and the sandbox "Stuck watchdog"
                -- switch is there to measure it.
                local FLUSH_TICKS, PORT_TICKS = 540, 1200   -- 9s and 20s at 60fps

                if not watchdogOn then
                    local T = BanditsNPC.Trace
                    if T and T.On() and st.ticks > FLUSH_TICKS then
                        T.Change(zombie, "WATCH", "off",
                            "watchdog DISABLED by sandbox -- it would have acted by now (ticks="
                            .. tostring(st.ticks) .. " acc=" .. tostring(math.floor(st.acc))
                            .. "ms pos=" .. T.N(zombie:getX()) .. "," .. T.N(zombie:getY())
                            .. " want=" .. tostring(moveTask.x) .. "," .. tostring(moveTask.y) .. ")")
                    end
                -- FIX(BUG-023AB): `moveTask.state == "COMPLETED"` REPLACES
                -- `(moveTask.time or 0) <= 0`. IT SUPERSEDES IT. DO NOT RE-ADD THE TIME TEST.
                --
                -- Both rungs used to carry the time test, and in a livelock NEITHER could
                -- fire: the task does not EXPIRE, it COMPLETES and is replaced with a fresh
                -- time=15+ZombRand(10) (BanditUtils.lua:1012). So the boundary the ladder was
                -- waiting for was essentially never presented, the 9s flush was unreachable,
                -- and the 20s teleport fired only when a collision streak happened to let a
                -- task time out -- 44448ms in guard-livelock-trace.txt, 119769ms in
                -- climb-probe-trace.txt. That is the inversion: the DRASTIC remedy first, by
                -- luck, and the gentle one not at all.
                --
                -- WHY IT SUPERSEDES RATHER THAN JOINS. There are exactly two routes to a task
                -- boundary and they set the SAME FIELD:
                --     BanditUpdate.lua:1805   local done = ZombieActions[task.action].onWorking(bandit, task)
                --     BanditUpdate.lua:1806   if done or task.time <= 0 then
                --     BanditUpdate.lua:1807       task.state = "COMPLETED"
                -- `done` is replacement, `task.time <= 0` is expiry, and both arrive at
                -- COMPLETED. So the state test already covers everything the time test covered.
                -- **Writing `... <= 0 or state == "COMPLETED"` as a belt-and-braces guard adds
                -- NOTHING and silently restores the bug's shape.** Measured across every trace
                -- in logs/: of 1959 sampled dispatches with a Move/GoTo at the head, the time
                -- test was open on 77 and EVERY ONE of those 77 was also COMPLETED -- a strict
                -- subset, 77/77, and 61/61 and 8/8 on the two narrower slices below.
                --
                -- (One trap in re-checking that from a trace: `t=` prints at ONE DECIMAL, so a
                -- WORKING task with a true time of 0.03 prints `t=0.0` and looks like an open
                -- time gate. 39 samples look that way and none of them are. Every genuinely
                -- expired sample prints strictly negative.)
                --
                -- HOW MUCH MORE OFTEN THE GATE OPENS: 14.3% of sampled dispatches against
                -- 3.9% overall (3.6x); 15.8% against 3.9% while the watchdog is counting
                -- (4.0x); and inside the flush window 540 < ticks <= 1200, **18.9% against
                -- 2.8% -- 6.6x**. That last figure is the fix: the flush stops being a lottery.
                --
                -- BUG-002's PROTECTION IS UNAFFECTED, AND THIS IS THE FIRST THING TO WORRY
                -- ABOUT, SO IT IS ANSWERED HERE. BUG-002 was the watchdog acting on a
                -- companion who was only STARVED OF UPDATE TICKS -- her task still running,
                -- just running slowly in wall-clock because OnZombieUpdate is throttled
                -- independently of the frame rate. **A tick-starved companion's task sits in
                -- WORKING.** It reaches COMPLETED only when Bandits itself is done with it, so
                -- the gate stays SHUT for exactly the case BUG-002 was about. If anything this
                -- is stricter than the old test: `task.time <= 0` could be true for a task
                -- Bandits had not yet finished processing, while COMPLETED is Bandits' own
                -- statement that the task is over. The real protection was always the dispatch
                -- counting (see the FIX(BUG-002) block below the teleport, which stays as
                -- written); this clause is the secondary guard and it still guards.
                elseif st.ticks > PORT_TICKS and moveTask.state == "COMPLETED" then
                    -- FIX(BUG-023C): aim at the destination the budget was spent on, recorded
                    -- by the tally at the top of this branch, NOT at whatever Move happens to
                    -- be at the head of the queue on the dispatch this fires.
                    --
                    -- WHAT A STALE RECORDED GOAL LOOKS LIKE, since it is the obvious worry.
                    -- "Stale" is bounded by the struggle: st.goalN dies with st.ticks at every
                    -- reset above (a 1.2-tile move, or Nav.InTransit) and again after this
                    -- teleport, so the goal can only ever come from the SAME accumulation that
                    -- armed this rung. Worst case is therefore: she spends most of the budget
                    -- failing to reach A, switches to B in the last seconds, and is teleported
                    -- to A -- a destination her own program issued and was pathing her toward
                    -- within the last ~20 seconds, i.e. a few tiles of unwanted displacement.
                    -- That is the same magnitude of error as today's wrong target, but chosen
                    -- for a measured reason instead of by accident, and it is the direction
                    -- both measured cases went back to on their own.
                    local px = st.goalX or moveTask.x
                    local py = st.goalY or moveTask.y
                    local tz = st.goalZ or zombie:getZ()
                    -- the target must be free AND have a floor: teleporting to a
                    -- floorless upper-level square dropped a companion through the
                    -- air (fall damage -> downed)
                    local function usable(s)
                        return s ~= nil and s:isFree(false) and s:getFloor() ~= nil
                    end
                    local sq = getCell():getGridSquare(math.floor(px), math.floor(py), tz)
                    if not usable(sq) then
                        local alt
                        pcall(function() alt = AdjacentFreeTileFinder.Find(sq, zombie) end)
                        sq = (alt ~= nil and usable(alt)) and alt or nil
                    end
                    -- v0.60: the usable() test above is now redundant with
                    -- Nav.Teleport's own, but kept because it also selects the
                    -- fallback square. What Nav adds here is the TRANSIT guard --
                    -- this fires 20s into a struggle, and a companion working her
                    -- way up a long staircase can trip that timer while making
                    -- perfectly good progress.
                    -- EVERY TELEPORT IS NOW ON THE RECORD (v0.75.64). This branch is the
                    -- source of every "and then she teleported" in a year of reports, and
                    -- until now it fired in complete silence -- so the single most
                    -- frequently reported event in this mod's history left no evidence at
                    -- all. The breadcrumbs go with it: the route she took to get stuck is
                    -- the only way to tell a bad destination from a bad path.
                    local T = BanditsNPC.Trace
                    if T and T.On() then
                        -- FIX(BUG-023C): `from` IS NOW HER LIVE POSITION. DO NOT PUT IT BACK.
                        -- It used to be T.Sq(zombie:getCurrentSquare()) -- NOT the anchor, which
                        -- is what docs/BUGS.md claimed; the anchor was never printed on this
                        -- line. It was the square she is REGISTERED on, and that drifts away
                        -- from where she actually stands. logs/climb-probe-trace.txt is the
                        -- proof: at 000231.4 (:233) pos=333.32,9689.21, and 0.3s later this
                        -- line said "from 332,9688,0" while she had not moved at all -- the
                        -- PATH crumbs on the next line, fed by the same getCurrentSquare(),
                        -- show it wandering 333,9689 > 333,9688 > 332,9688 under a stationary
                        -- companion. Freezes were read off this field and located on the wrong
                        -- square more than once. It now matches the TICK line's pos= exactly,
                        -- from the same accessors, so the two compare without arithmetic.
                        T.Line(zombie, "WATCH", "TELEPORT after " .. tostring(math.floor(st.acc))
                            .. "ms / " .. tostring(st.ticks) .. " ticks stationary -- from "
                            .. T.N(zombie:getX()) .. "," .. T.N(zombie:getY()) .. "," .. T.N(zombie:getZ(), 1)
                            .. " to " .. T.Sq(sq)
                            .. " (move target " .. tostring(px) .. "," .. tostring(py)
                            .. " held=" .. tostring(st.goalN) .. "/" .. tostring(st.ticks)
                            .. " head=" .. tostring(moveTask.x) .. "," .. tostring(moveTask.y)
                            .. ") tasksDropped=" .. tostring(#brain.tasks))
                        T.Dump(zombie, "teleport")
                    end
                    if sq then
                        BanditsNPC.Nav.Teleport(zombie, sq, "stuck watchdog")
                    end
                    brain.tasks = {}
                    -- climbTried is reset with the rest: after a teleport she is
                    -- somewhere new, so a fresh obstacle deserves a fresh attempt.
                    -- goalN/runN go with ticks -- see FIX(BUG-023C) at the recorder above
                    st.acc, st.ticks, st.flushed, st.climbTried, st.goalN, st.runN = 0, 0, nil, nil, nil, nil
                -- FIX(BUG-002): `(moveTask.time or 0) <= 0` -- NEVER ACT MID-TASK.
                --
                -- SUPERSEDED IN FORM, NOT IN INTENT, BY FIX(BUG-023AB) ABOVE. The test below
                -- is now `moveTask.state == "COMPLETED"`, which is the same rule -- act only
                -- at a task boundary -- stated in the field Bandits actually sets. Everything
                -- this block says about WHY is still true and is why the rule is kept at all;
                -- only the expression changed. Read the BUG-023AB block above the teleport
                -- before touching either rung.
                --
                -- `task.time` is a budget in SIXTIETHS OF A SECOND, normalised against the
                -- game's frame rate -- NOT a tick count and NOT milliseconds:
                --     BanditUpdate.lua:1802   local decrement = 1 / ((getAverageFPS() + 0.5) * 0.01666667)
                --     BanditUpdate.lua:1803   task.time = task.time - decrement
                -- i.e. 60/(avgFPS+0.5) subtracted per PROCESSING tick, so at 60fps it burns
                -- ~1 per tick and a time=20 Move task lasts ~0.33s.
                --
                -- THE NORMALISATION IS AGAINST GAME FPS, NOT AGAINST HER UPDATE RATE, and
                -- that is the whole problem. Her tasks are processed on OnZombieUpdate, which
                -- tiered zombie updates throttle independently of the frame rate. When the
                -- two diverge the budget stretches by the ratio between them: the 8 Aug trace
                -- shows a time=20 task taking 5.3 REAL SECONDS at 3.7 updates/sec.
                --
                -- So acting while time remains destroys an attempt that was still running and
                -- forces a full re-dispatch. In that trace the flush was harmless ONLY because
                -- the task happened to sit at t=0.0 on that tick; two seconds earlier it still
                -- had 12 units left. This makes "act only at a task boundary" a property of
                -- the design rather than luck.
                elseif st.ticks > FLUSH_TICKS and moveTask.state == "COMPLETED" and not st.flushed then
                    st.flushed = true
                    local T = BanditsNPC.Trace
                    if T and T.On() then
                        -- FIX(BUG-023C): live position, same reason as the teleport above.
                        -- `wanting` stays on moveTask -- this rung does not move her, it clears
                        -- the queue, so the head task is the correct thing to name here.
                        T.Line(zombie, "WATCH", "FLUSH after " .. tostring(math.floor(st.acc))
                            .. "ms / " .. tostring(st.ticks) .. " ticks stationary -- at "
                            .. T.N(zombie:getX()) .. "," .. T.N(zombie:getY()) .. "," .. T.N(zombie:getZ(), 1)
                            .. " wanting "
                            .. tostring(moveTask.x) .. "," .. tostring(moveTask.y)
                            .. " tasksDropped=" .. tostring(#brain.tasks))
                    end
                    brain.tasks = {}   -- fresh dispatch -> fresh path
                end
            end
            st.last = now
        end

        -- ===== ANTI-BLEED-OUT, AND IT IS *NOT* PART OF IMMORTALITY (v0.77.10) =====
        --
        -- This used to sit below the immortality gate, and that is the "companions that
        -- never stop bleeding to death, treating and helping up does nothing" report.
        --
        -- Bandits drains a bandit's health on its own: below 0.7 it splatters blood
        -- constantly, below 0.4 it makes her self-bandage forever. That drain is an
        -- artefact of a companion being a hijacked IsoZombie, not a wound anybody
        -- inflicted -- and with ImmortalCompanions OFF, nothing opposed it. She bled down
        -- while standing safe at base, Treat healed her to full and the drain immediately
        -- started again (so treating "did nothing"), and she never got the knockdown that
        -- would have let you help her up, because that is gated too. It happened at random
        -- across players because it happened to exactly the ones who turned immortality off.
        --
        -- Turning immortality off means "she can be killed by damage". It does not mean
        -- "she should slowly bleed out on her own", so the counter-drain runs either way.
        -- Enemy fire (-0.18 max HP per hit) still vastly outpaces it, so real combat still
        -- kills a mortal companion exactly as the option promises.
        --
        -- THE FLOOR IS DOWNED_FLOOR_FRAC, NOT KNOCKDOWN_FRAC (v0.77.13). It was the
        -- knockdown threshold, and that is the second half of the Help Up loop: helped up
        -- BELOW that threshold, she was under the floor of the one thing that could have
        -- healed her back over it. The counter-drain was locked out at exactly the health
        -- where it was needed most. `not brain.downed` is what keeps this from lifting a
        -- downed companion off the floor -- that was always the real guard; the fraction
        -- was only ever meant to leave the knockdown path alone, and below 10% that path
        -- (or a genuinely lethal situation) owns her.
        do
            local maxh0 = (brain.health and brain.health > 0) and brain.health or 1.0
            local h0 = zombie:getHealth()
            if not brain.downed and h0 >= maxh0 * DOWNED_FLOOR_FRAC and h0 < maxh0 then
                pcall(function() zombie:setHealth(math.min(maxh0, h0 + maxh0 * 0.0008)) end)
            end
        end

        if not immortalOn() then return end

        -- ===== SURROUNDED -> DOWNED (not dead) =====
        -- When 3+ zombies maul a bandit, the engine queues a {action="Die"} task. Intercept
        -- it (like the Zombify task above) and KNOCK HER DOWN instead -- she's incapacitated,
        -- becomes invisible to zombies (GetClosestBanditLocation wrap in Programs) so the mob
        -- disperses, and keeps her gear (a real death would drop her inventory). Help her up
        -- when it's safe.
        -- NOTE: the engine's Die task is lock=true and Bandit.ClearTasks PRESERVES locked
        -- tasks -- so "clearing" left the Die task alive and she died ~5s later anyway.
        -- Remove it from the queue by index (like the Zombify intercept above). Runs even
        -- while already downed: the mob can re-queue Die during the retarget window before
        -- zombies lose interest in her.
        if brain.tasks then
            local hadDie = false
            for i = #brain.tasks, 1, -1 do
                local t = brain.tasks[i]
                if t and t.action == "Die" then
                    table.remove(brain.tasks, i)
                    hadDie = true
                end
            end
            if hadDie and not brain.downed and BanditsNPC.Combat and BanditsNPC.Combat.KnockDown then
                BanditsNPC.Combat.KnockDown(zombie, brain)
            end
        end

        -- ===== hurt-able, not kill-able =====
        -- When critically wounded a companion is KNOCKED DOWN (incapacitated until helped up)
        -- instead of dying. Backup path for melee/other damage; ranged bandit fire is caught
        -- at the source in BanditsNPCCombat.
        local maxh = (brain.health and brain.health > 0) and brain.health or 1.0
        if brain.downed then
            if zombie:getHealth() < maxh * DOWNED_FLOOR_FRAC then
                zombie:setHealth(maxh * DOWNED_FLOOR_FRAC)
            end
        elseif zombie:getHealth() < maxh * KNOCKDOWN_FRAC then
            if BanditsNPC.Combat and BanditsNPC.Combat.KnockDown then
                BanditsNPC.Combat.KnockDown(zombie, brain)
            elseif zombie:getHealth() < maxh * HEALTH_FLOOR_FRAC then
                -- LAST-DITCH ONLY, and it can never run in a healthy install: Combat is a
                -- core file, so KnockDown is always there and takes the branch above.
                -- Kept because the alternative if Combat ever fails to load is a companion
                -- who simply dies, and named constants now instead of the bare 0.30/0.35
                -- that made this pair look like a threshold bug rather than a fallback
                -- (v0.75.25).
                zombie:setHealth(maxh * HEALTH_FLOOR_FRAC)
            end
        end
        -- (The regen that used to be the `else` of this branch now runs ABOVE the
        -- immortality gate, under the same conditions -- see the note there. Leaving a
        -- copy here would apply it twice per tick to an immortal companion.)
    end)
    -- failures are non-fatal; protection is best-effort across engine versions
end

-- (registration is consolidated at the bottom of this file -- see the note there)

-- Hide a recruited companion's model while she is riding in the player's
-- vehicle. The engine otherwise draws the seated body floating on top of the
-- car (it has no real "passenger" pose for hijacked-zombie NPCs), which looks
-- broken. We simply stop rendering her while she has a vehicle and restore it
-- the moment she doesn't.
--
-- This is deliberately a separate, always-on handler (not gated on the immortal
-- toggle) and it RE-DERIVES visibility from getVehicle() every tick, so a
-- companion can never get stuck invisible: any path that takes her out of the
-- vehicle - exiting, dying, being unrecruited, the program switching - lands on
-- "no vehicle" within a tick and makes her visible again.
-- THE `inVeh` BRANCH BELOW IS UNREACHABLE, AND SAYING SO IS THE POINT (v0.75.45).
-- onZombieUpdate runs FIRST in the consolidated handler and evicts a companion from any
-- vehicle unconditionally, so by the time this evaluates getVehicle() it is already nil.
-- The ~20 lines that follow read as live visibility management and have never run.
--
-- NOT DELETED and NOT "fixed" by reordering, because both are behaviour changes dressed
-- as cleanups: the vehicle RIDE feature works by removing the body and restoring it
-- (Persistence.BeginRide), not by hiding it in a seat, so hiding is a second, older
-- mechanism for a job something else already does. Reordering the handlers to make this
-- reachable would give a riding companion two competing implementations. If the ride
-- feature is ever reworked to keep the body in the seat, this is the code to revive --
-- until then it is a comment, not a feature.
local function onCompanionVisibility(zombie)
    pcall(function()
        if not (zombie and zombie.getVariableBoolean and zombie:getVariableBoolean("Bandit")) then return end
        local brain = BanditBrain and BanditBrain.Get(zombie)
        local recruited = brain and brain.recruited
        local inVeh = recruited and zombie.getVehicle and (zombie:getVehicle() ~= nil)
        local md = zombie:getModData()
        if inVeh then
            -- Hide the seated body. IMPORTANT: B42's alpha SETTERS on IsoGameCharacter are
            -- THE ALPHA CALLS ARE GONE, AND THE OLD COMMENT HERE WAS BACKWARDS
            -- (v0.75.19). It claimed the CAPITALIZED forms were the real ones and the
            -- lowercase calls resolved to nil. Dumping IsoZombie / IsoMovingObject /
            -- IsoObject shows there is NO alpha SETTER in either case: `alpha` is a public
            -- FIELD (`public alpha [F`) and `targetAlpha` is protected, so both
            -- `SetTargetAlpha` and `setAlpha` were nil and both were silently skipped.
            -- Kahlua cannot write a public Java field either, so the field is no route in.
            -- setDoRender(false) is what actually hides her; it is kept below.
            -- All stomped per tick. NOT setSceneCulled: if it paused her updates entirely,
            -- nothing could ever un-hide her or walk her out of the car.
            pcall(function() if zombie.setDoRender then zombie:setDoRender(false) end end)
            pcall(function() if zombie.SetAlpha then zombie:SetAlpha(0) end end)
            pcall(function() if zombie.setInvisible then zombie:setInvisible(true) end end)
            md.tcSeatHidden = true
        elseif md.tcSeatHidden then
            -- one-shot restore on leaving the seat (marker survives save/load, so a
            -- companion saved while hidden can never get stuck invisible)
            md.tcSeatHidden = nil
            pcall(function() if zombie.setDoRender then zombie:setDoRender(true) end end)
            pcall(function() if zombie.SetAlpha then zombie:SetAlpha(1) end end)
            pcall(function() if zombie.SetTargetAlpha then zombie:SetTargetAlpha(1) end end)
            pcall(function() if zombie.setAlpha then zombie:setAlpha(1) end end)
            pcall(function() if zombie.setInvisible then zombie:setInvisible(false) end end)
        end
    end)
end


-- Slow healing back to full after a knockdown. Hung off the same per-zombie event as
-- everything else here rather than a timer: Combat.Regen measures elapsed GAME HOURS
-- itself, so being called irregularly (or not at all while she is streamed out) is
-- exactly what it expects.
-- ONE HANDLER, NOT THREE (v0.75.16). OnZombieUpdate fires per zombie per tick, so with
-- a horde on screen the registration count is a real multiplier -- three separate
-- listeners meant three dispatches and three independent brain lookups for every zombie,
-- every tick, most of which are not companions at all. Order is preserved exactly as the
-- three registrations ran, because onZombieUpdate can change state the other two read.
--
-- Each is still individually pcall-guarded: one throwing must not stop the others, which
-- is what the separate registrations gave us for free and is worth keeping deliberately.
local function onZombieUpdateAll(z)
    if not z then return end
    pcall(onZombieUpdate, z)
    pcall(onCompanionVisibility, z)
    if BanditsNPC.Combat and BanditsNPC.Combat.Regen then
        pcall(BanditsNPC.Combat.Regen, z)
    end
end

if Events.OnZombieUpdate then
    Events.OnZombieUpdate.Add(onZombieUpdateAll)
end
