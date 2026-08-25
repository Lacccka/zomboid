--
-- True Companions - Companion persistence (roster + lost-companion restore)
--
-- PZ's own zombie save/load can occasionally LOSE a recruited companion (its zombie is
-- culled / not restored on load). Bandits stores a bandit's brain on the zombie's ModData
-- (saved per-chunk) AND in cluster GlobalModData; there is no active respawn-on-load. So a
-- lost companion is genuinely gone unless we bring it back.
--
-- This module:
--   * gives each companion a STABLE id (brain.npcUid) that survives a respawn (the bandit
--     id / persistentOutfitID does NOT),
--   * keeps a ROSTER in GlobalModData of each companion's full brain + its "post" (Stay
--     tile / Guard post / current spot, by order),
--   * when a rostered companion is MISSING but its post is loaded (you're there) and a grace
--     period has passed, RESTORES it -- at its post, not at you -- using Bandits' own
--     (cluster-correct) Spawner.Restore to spawn a shell, then patching our companion
--     fields back onto it. The de-dup in the Guardian is the safety net against doubles.
--
-- The restore is GATED behind the sandbox option "Restore lost companions" (default OFF)
-- because it respawns entities and touches save data, and needs an in-game save/load test.
--

BanditsNPC = BanditsNPC or {}
BanditsNPC.Persistence = {}
local P = BanditsNPC.Persistence

local function roster()
    return ModData.getOrCreate("TrueCompanionsRoster")
end

-- Copy only SERIALIZABLE content (tables of numbers/strings/booleans). Functions and
-- userdata (java object refs) are skipped: one such value inside a brain stored into
-- GlobalModData could fail the java-side save serialization -- i.e. corrupt/lose the
-- whole ModData table on save. Brains are normally plain, but third-party code can and
-- does attach transients, so never trust a live brain blindly.
local function deepcopy(t, seen)
    local tt = type(t)
    if tt == "function" or tt == "userdata" or tt == "thread" then return nil end
    if tt ~= "table" then return t end
    seen = seen or {}
    if seen[t] then return seen[t] end
    local c = {}
    seen[t] = c
    for k, v in pairs(t) do
        local ck = deepcopy(k, seen)
        local cv = deepcopy(v, seen)
        if ck ~= nil and cv ~= nil then c[ck] = cv end
    end
    return c
end

-- ---------------------------------------------------------------------------
-- carried items
-- ---------------------------------------------------------------------------
--
-- DEFINED HERE, ABOVE P.Record, DELIBERATELY. A `local function` is invisible to code
-- written above it -- an earlier call compiles as a nil GLOBAL lookup and errors at
-- runtime -- and P.Record is the first caller.

-- A serializable manifest of everything a companion is carrying, INCLUDING the
-- contents of bags she is carrying.
--
-- Recorded as a flat list in depth-first order with each entry naming the INDEX of
-- its container, so a bag always appears before the things inside it and the restore
-- can rebuild the nesting in one forward pass. A nested table would be prettier and
-- would be one more shape to trust through ModData serialization.
--
-- Condition and drainable uses travel too: a restore that handed back a pristine axe
-- and a full water bottle every time would be a quiet exploit.
--
-- CAPPED. This goes into GlobalModData and into the bandit cluster, both serialized
-- on every save; an unbounded walk of a hoarder's backpack belongs in neither.
local MAX_ITEMS = 60

local function captureItems(zombie)
    local out = {}
    if not zombie then return out end

    local function walk(container, parentIdx, depth)
        if depth > 2 or #out >= MAX_ITEMS then return end
        local items = container:getItems()
        for i = 0, items:size() - 1 do
            if #out >= MAX_ITEMS then return end
            local it = items:get(i)
            local ft
            pcall(function() ft = it and it:getFullType() end)
            if ft then
                local rec = { t = ft, p = parentIdx }
                pcall(function()
                    if it.getConditionMax and it:getConditionMax() > 0 then
                        rec.c = it:getCondition()
                    end
                end)
                pcall(function()
                    if instanceof(it, "DrainableComboItem") and it.getCurrentUsesFloat then
                        rec.u = it:getCurrentUsesFloat()
                    end
                end)
                -- APPEARANCE TRAVELS TOO (v0.75.17). A restore rebuilds every item with
                -- AddItem(type), and the engine RE-ROLLS a fresh item's look -- so the
                -- red shirt she was carrying came back a random colour, every knockdown,
                -- every reload. Condition and uses were already carried for exactly this
                -- reason; the five visual fields were the missing third. Same capture the
                -- clothing system uses, so the two cannot disagree.
                pcall(function()
                    local iv = it.getVisual and it:getVisual()
                    if iv and BanditsNPC.Interact and BanditsNPC.Interact.CaptureLook then
                        local look = BanditsNPC.Interact.CaptureLook(iv, {})
                        -- NOT `next(look)`: Kahlua has no global `next`, and calling it
                        -- raises KahluaUtil.fail, which propagates THROUGH pcall. Same
                        -- trap the door-closing code documents at length.
                        local any = false
                        for _ in pairs(look) do any = true; break end
                        if any then rec.v = look end
                    end
                end)
                out[#out + 1] = rec
                local myIdx = #out
                pcall(function()
                    if instanceof(it, "InventoryContainer") then
                        walk(it:getInventory(), myIdx, depth + 1)
                    end
                end)
            end
        end
    end

    pcall(function() walk(zombie:getInventory(), nil, 1) end)
    return out
end

-- Rebuilds the manifest onto a freshly spawned body. Anything that fails is skipped
-- rather than aborting the restore: a companion who comes back missing one item is a
-- far better outcome than one who does not come back at all.
local function restoreItems(zombie, list)
    if not (zombie and type(list) == "table" and #list > 0) then return end
    local made = {}
    pcall(function()
        local inv = zombie:getInventory()
        for i = 1, #list do
            local rec = list[i]
            local into = inv
            if rec.p and made[rec.p] then
                pcall(function() into = made[rec.p]:getInventory() end)
            end
            local it
            pcall(function() it = into:AddItem(rec.t) end)
            if it then
                made[i] = it
                if rec.c then pcall(function() it:setCondition(rec.c) end) end
                if rec.u then pcall(function() it:setCurrentUsesFloat(rec.u) end) end
                if rec.v then
                    pcall(function()
                        local iv = it.getVisual and it:getVisual()
                        if iv and BanditsNPC.Interact and BanditsNPC.Interact.ApplyLook then
                            BanditsNPC.Interact.ApplyLook(iv, rec.v)
                        end
                    end)
                end
            end
        end
    end)
end

-- stable per-companion id (survives respawn, unlike the volatile bandit/outfit id)
function P.EnsureUid(brain)
    if brain and not brain.npcUid then
        brain.npcUid = "npc_" .. tostring((getTimestampMs and getTimestampMs()) or 0) .. "_" .. ZombRand(1000000)
    end
    return brain and brain.npcUid
end

-- where should this companion be restored (its "post")?
local function postFor(zombie, brain)
    local prog = brain.program and brain.program.name
    if prog == "NPCStay" and brain.stayPos then
        return { x = brain.stayPos.x, y = brain.stayPos.y, z = brain.stayPos.z or 0 }
    end
    if prog == "NPCGuard" and brain.guardA then
        return { x = brain.guardA.x, y = brain.guardA.y, z = brain.guardA.z or 0 }
    end
    return { x = math.floor(zombie:getX()), y = math.floor(zombie:getY()), z = zombie:getZ() }
end

-- ===== WHOSE COMPANION IS THIS? (v0.75.4, audit BLOCKER 3) =====
--
-- The roster is a CLIENT-LOCAL store -- ModData.getOrCreate with no transmit, per
-- client. That is workable for a player's own companions (the whole Bandits stack is
-- per-client), but until now nothing filtered by owner, so every client rostered every
-- player's companions and then treated them as its own to re-home and respawn.
--
-- TWO TESTS, NOT ONE, AND THE ASYMMETRY IS THE POINT:
--   * ownedByMe -- POSITIVE proof this is mine. Required before we ACT on an entry
--     (re-home its post, restore a body). "Not provably mine" is a transient state --
--     player 0 may not exist yet during load -- and acting on it is what caused the
--     damage.
--   * ownedByOther -- POSITIVE proof it is somebody ELSE's: two known usernames that
--     differ. Required before we DELETE an entry. Deleting on merely "not mine" would
--     throw away real companions during that same transient window, which is a worse
--     bug than the one being fixed.
-- Nothing happens in the gap between them, which is exactly what should happen when we
-- do not yet know.
local function ownedByMe(brain)
    if not brain then return false end
    -- Fail OPEN if the namespace is somehow absent, unlike RosterPanel's list filter
    -- which fails closed. The costs are opposite: an unlisted companion is a cosmetic
    -- gap in a panel, an unrostered one can be lost for good.
    if not (BanditsNPC and BanditsNPC.IsMine) then return true end
    return BanditsNPC.IsMine(brain) and true or false
end

local function ownedByOther(brain)
    if not brain then return false end
    local other = brain.masterName
    if type(other) ~= "string" or other == "" then return false end
    local p = getSpecificPlayer(0)
    if not p then return false end
    local ok, un = pcall(function() return p:getUsername() end)
    if not (ok and type(un) == "string" and un ~= "") then return false end
    return un ~= other
end

-- record a loaded companion into the roster (keeps its brain + post fresh)
function P.Record(zombie, brain)
    if not (brain and brain.recruited) then return end
    -- Never roster somebody else's companion -- this is the single write point, so
    -- gating here keeps the store clean no matter who calls it.
    if ownedByOther(brain) then return end
    local uid = P.EnsureUid(brain)
    if not uid then return end

    -- ===========================================================================
    -- NEVER SNAPSHOT A BODY THAT IS STILL WAITING TO BE FILLED (v0.77.9).
    -- ===========================================================================
    --
    -- This is the "my companion's inventory reset" report, and it is a race with our own
    -- restore. A restore runs in two steps that are NOT adjacent in time: BeginRestore
    -- creates the body, and applyRestore -- which is what calls restoreItems and actually
    -- puts her belongings back -- only runs once Bandits has adopted the shell and
    -- ResolvePending next sees it on a zombie update.
    --
    -- In between, she is a fully-fledged recruited companion carrying NOTHING. And P.Tick
    -- calls RecordAllLoaded on every loaded companion, which captures the live body's
    -- inventory over the roster's copy. Land one tick in that window and the good manifest
    -- is replaced by an empty one -- so the restore that was about to give everything back
    -- finds nothing to give, and it is gone for good. Every path that rebuilds a body is
    -- affected: knockdowns, vehicle rides, walking back into range, reloading a save.
    --
    -- The guard belongs HERE rather than in the tick, because Record is the single write
    -- point into the roster and the appearance and interact paths reach it too.
    -- `P.pending and` because the table is created further down this file than Record is
    -- defined. Nothing calls Record during load today, but Record is not wrapped in a pcall
    -- by all of its callers and a nil index here would take the tick down with it.
    if P.pending and P.pending[uid] then return end

    local snap = deepcopy(brain)
    snap.tasks = {}   -- never persist a live task queue; a restored shell starts fresh
    -- Taken from the LIVE BODY, not from the brain: the brain has no idea what is in
    -- her pockets, and this is the only moment we are holding both.
    snap.npcItems = captureItems(zombie)
    roster()[uid] = {
        brain = snap,
        post = postFor(zombie, brain),
        hour = getGameTime():getWorldAgeHours(),
    }
end

-- A TOMBSTONE, NOT A DELETE (v0.77.13).
--
-- Deleting the entry leaves nothing behind that says "this was deliberate", so the very
-- next P.Record for that companion -- and RecordAllLoaded runs over every loaded companion
-- on the minute tick -- writes a fresh live entry and she is rostered again. Dismiss sets
-- recruited = false first, which is what Record checks, so the window is small; but it is
-- not zero (the sync is asynchronous, the brain is the live modData table, and Record is
-- reachable from the appearance and interact paths as well as the tick), and "she came back
-- on her own" is the exact symptom being reported.
--
-- The marker is a table with no `brain`, so every consumer treats it as absent: the sweep
-- reads entry.brain (nil -> ownedByMe(nil) is false -> the entry is left alone and nothing
-- is ever restored from it), and RosterCount is the one place that needs teaching.
-- Cheaper and safer than a second store to keep in step.
function P.Forget(brain)
    if not (brain and brain.npcUid) then return end
    roster()[brain.npcUid] = { dismissed = true, hour = getGameTime():getWorldAgeHours() }
end

-- How many companions we know about, loaded or not. Counts the ROSTER rather than
-- scanning the world, so a companion left at a base the player has walked away
-- from still counts -- she exists, she is just not streamed in. (Kahlua has no
-- global `next` and no table length for hash parts, so this is a pairs walk.)
-- Empty the whole store. Only the prepare-for-uninstall path uses this; it is deliberately
-- a named function rather than letting another module reach into the ModData key, so there
-- stays exactly one writer.
function P.ClearRoster()
    local r = roster()
    if not r then return end
    for uid in pairs(r) do r[uid] = nil end
end

function P.RosterCount()
    local n = 0
    -- Tombstones are NOT companions (v0.77.13). This counts rows, and P.Forget now leaves
    -- a marker row behind instead of deleting -- so without the entry.brain test every
    -- dismissal would permanently occupy a slot in the beacon's capacity check and the
    -- station would go quiet after enough of them. That is the very report this build is
    -- also fixing from the other end, so it must not be reintroduced here.
    for _, e in pairs(roster()) do
        if e and e.brain then n = n + 1 end
    end
    return n
end

local function eachLoadedBandit(fn)
    local cell = getCell()
    local zlist = cell and cell.getZombieList and cell:getZombieList()
    if not zlist then return end
    for i = 0, zlist:size() - 1 do
        local z = zlist:get(i)
        local stop = false
        pcall(function()
            if z and z:getVariableBoolean("Bandit") then stop = fn(z) end
        end)
        if stop then return end
    end
end

-- NOTE: this MUST stay below eachLoadedBandit. A `local function` is invisible to
-- code defined ABOVE it -- the call compiles as a nil GLOBAL lookup and errors at
-- runtime. This function was originally placed higher up and crashed on the first
-- dismantle for exactly that reason.
-- Puts EVERY companion back on Follow, whether she is loaded or not.
--
-- Used when the last beacon is removed. Without a beacon there is no base and no
-- base zone, so orders that mean "be somewhere" -- Stay, Guard, Relax at Base,
-- and every work assignment -- no longer refer to anything. Leaving a companion on
-- Stay at a base that no longer exists is how you lose her: she waits at a post
-- the player has abandoned and is never seen again.
--
-- BOTH halves matter. Loaded companions get the order through the normal path so
-- their current task queue is flushed; the ROSTER copy is rewritten too, so a
-- companion who is streamed out (or gets restored later) comes back following
-- rather than returning to a post that is gone.
function P.ForceAllFollow(reason)
    local n = 0

    eachLoadedBandit(function(z)
        local b = BanditBrain.Get(z)
        if b and b.recruited and BanditsNPC.IsMine and BanditsNPC.IsMine(b) then
            pcall(function() BanditsNPC.Interact.SetOrder(z, "NPCCompanion") end)
            n = n + 1
        end
    end)

    for _, entry in pairs(roster()) do
        -- Same ownership test the loaded half above uses. After v0.75.4 the roster only
        -- holds our own anyway, but this loop REWRITES ORDERS -- it must not be the one
        -- place that would still reach into a pre-fix save's foreign entries.
        if entry.brain and ownedByMe(entry.brain) then
            entry.brain.program = { name = "NPCCompanion", stage = "Prepare" }
            -- Drop the stale geography with it. These fields are what the old
            -- orders pointed at; keeping them would let a later order silently
            -- resume aiming at the removed base.
            entry.brain.stayPos = nil
            entry.brain.relaxPos = nil
            entry.brain.guardA = nil
            entry.brain.guardB = nil
            entry.brain.guardTarget = nil
        end
    end

    print("[BanditsNPC] All companions set to Follow (" .. tostring(reason or "no reason given")
        .. "); " .. tostring(n) .. " loaded companion(s) re-ordered.")
    return n
end

local function findLoadedByUid(uid)
    local found
    eachLoadedBandit(function(z)
        local b = BanditBrain.Get(z)
        if b and b.npcUid == uid then found = z; return true end
    end)
    return found
end

-- An ORPHAN SHELL: a restore spawn whose pending expired before we patched it (slow
-- spawn/claim). The engine's banditize keeps only brain.key of our seed -- no npcUid, no
-- recruited -- so neither findLoadedByUid nor the Guardian dedupe used to see it, and the
-- minute-tick kept declaring the companion missing and spawning ANOTHER copy ("6 of the
-- same person", tester 8 Jul). The key prefix embeds the uid, so we can find and ADOPT
-- the shell instead of duplicating it.
-- (v0.60 REMOVED: P.ShellKeyPrefix / findShellByUid. They existed only to recognise
-- a body the SERVER spawned on our behalf, by embedding a magic string in
-- brain.key and scanning every loaded bandit for it. BeginRestore now creates the
-- body itself and keeps the object, so there is nothing to tag and nothing to
-- search for -- and no window in which an unclaimed shell can exist to be
-- duplicated. The Guardian's matching shell branch went with it.)

-- custom fields we re-apply onto a freshly-restored companion (Bandits' spawn restores
-- only a shell; these carry our companion identity/state back).
local CUSTOM_FIELDS = { "npcUid", "recruited", "master", "masterName", "npcStance", "affinity",
    "exp", "npcProfRolled", "fullname", "clothing", "tint", "weapons", "spots", "stayPos",
    "guardA", "guardB", "guardTarget", "quest", "questUnlockHour", "occupationName", "storyParts",
    "storyRevealed", "partner", "health", "needs", "workstation", "program", "prevProgram",
    "downed", "schedule", "scheduleBlock", "preSchedule", "npcLastProgram", "npcMelee",
    -- "bag" added 29 Jul: a bag the PLAYER gave her lives on brain.bag, and without it here
    -- a restore silently reverted her to whatever bag her Bandit Creator profile ships with
    -- (banditize sets brain.bag from the profile), so the backpack you handed over vanished.
    -- "npcItems" added 3 Aug: WHAT SHE IS CARRYING. Its absence was silent data loss.
    -- A restore builds a BRAND NEW zombie and copies only the brain, so everything in
    -- her pockets was destroyed every time one ran -- being knocked down and helped
    -- up, a save and reload, or the lost-companion recovery. Anything she had
    -- gathered was gone the moment a zombie put her on the floor.
    -- hairStyle/beardStyle added 3 Aug with the Appearance Workstation. Those two are
    -- BANDITS' OWN field names -- Bandit.ApplyVisuals has an explicit-override branch
    -- that reads exactly them -- so the engine and our station cannot disagree about
    -- what a companion looks like. Without them here, a restyle would be undone by the
    -- next save, knockdown or reload.
    --
    -- COLOUR IS OURS ALONE (tcHairColor/tcBeardColor, 4 Aug). Bandits' hairColor field
    -- holds a palette INDEX for clan NPCs and a table on its override path; sharing it
    -- crashed both sides. hairColor/beardColor stay listed so a save written by
    -- v0.74.0-v0.74.6 still round-trips into the migration in Appearance.Apply.
    "relaxPos", "npcClothVar", "npcNoGuns", "npcStashedGuns", "bag", "npcItems",
    -- "npcFollowDist" (v0.76.1): near/mid/far. A per-companion preference the player set
    -- deliberately, so losing it on a knockdown or a reload would be exactly the kind of
    -- quiet reset that makes a setting feel broken.
    "npcFollowDist",
    -- "age" / "kills" / "metHour" (v0.77.0): what the Profile panel says about her. All
    -- three are recorded once and can never be re-derived -- an age would change between
    -- openings, a kill tally would reset to zero, and the day you met her is gone the
    -- moment it is not written down. Losing any of them on a knockdown would turn a
    -- companion's history into a blank, which is worse than not showing it at all.
    "age", "kills", "metHour",
    -- "npcMeleeState" added 5 Aug (v0.75.40): the condition and appearance of the melee
    -- weapon she is holding. Without it a restore loses the state and the weapon is handed
    -- back pristine -- which is the exact repair exploit the capture exists to close, so
    -- carrying one without the other would have been pointless.
    "npcMeleeState",
    "hairStyle", "beardStyle", "hairColor", "beardColor",
    "tcHairColor", "tcBeardColor" }


P.pending = {}

-- brains captured by the BanditBrain.Remove hook at death time (see below), keyed by the
-- zombie object; consumed by ReviveDowned in the same tick.
P.removedBrains = {}

local function restoreEnabled()
    return (BanditsNPC.Opt and BanditsNPC.Opt("RestoreLostCompanions", true)) ~= false
end

-- ===== last-moment brain capture: hook BanditBrain.Remove =====
-- Bandits' own OnZombieDead handler runs BEFORE ours (Bandits loads first) and it strips the
-- "Bandit" variable and calls BanditBrain.Remove -- exactly the two things our revive used to
-- guard on, so revive-on-death silently never fired (headshot = permanent death + corpse).
-- The same Remove is also the last step of Zombify() on the cluster-missing path. Hooking it
-- hands us the brain at the last moment it exists, for both cases:
--   * zombie DEAD  -> stash the brain so our OnZombieDead handler can revive her downed.
--   * zombie ALIVE -> she is being ZOMBIFIED (or her brain recycled off a de-flagged zombie):
--     re-home the brain into the cluster under the CURRENT id and let the engine itself
--     re-banditize her next tick (the gmd[id]-present branch in BanditUpdate). This also wins
--     the first-tick race on reload, where the engine zombifies a companion whose id changed
--     BEFORE our per-tick Guardian re-home ever ran for her.
-- The hook is hot (the engine calls Remove per-tick on ordinary zombies to recycle leftover
-- brains), so the common path must stay one table lookup.
if BanditBrain and BanditBrain.Remove and not BanditsNPC._brainRemoveHooked then
    BanditsNPC._brainRemoveHooked = true
    local origRemove = BanditBrain.Remove
    BanditBrain.Remove = function(zombie)
        pcall(function()
            local md = zombie and zombie.getModData and zombie:getModData()
            local brain = md and md.brain
            if brain and brain.recruited then
                local dead = false
                pcall(function() dead = zombie:isDead() end)
                if dead then
                    P.removedBrains[zombie] = brain
                elseif (BanditsNPC.Opt and BanditsNPC.Opt("AllowZombification", false)) == true
                        and (brain.infection or 0) >= 100 then
                    -- legit opted-in bite turn: let her go, and drop her from the roster so
                    -- the lost-companion restore doesn't resurrect a copy of her
                    P.Forget(brain)
                else
                    local id = BanditUtils and BanditUtils.GetCharacterID(zombie)
                    if id and GetBanditClusterData then
                        local gmd = GetBanditClusterData(id)
                        if gmd then
                            gmd[id] = brain
                            -- TRANSMIT (v0.75.15). A cluster write with no transmit is a
                            -- LOCAL edit: the server and every other client keep the old
                            -- brain, and since BanditUpdate reseeds from the cluster on
                            -- re-banditize, this one would simply be undone.
                            if TransmitBanditCluster then
                                pcall(function() TransmitBanditCluster(id) end)
                            end
                        end
                    end
                end
            end
        end)
        return origRemove(zombie)
    end
end

-- ===== RESTORE PREFLIGHT (29 Jul) =====
-- The engine's spawnRestore (BanditServerSpawner.lua:495) opens with TWO silent bail-outs
-- and no logging whatsoever:
--     local bandit = BanditCustom.GetById(brain.bid);  if not bandit then return end
--     local clan   = BanditCustom.ClanGet(brain.cid);  if not clan   then return end
-- When either fires, NOTHING happens: no spawn, no error, no console line. The companion
-- simply never comes back, and our caller has no way to know. That is the mechanism behind
-- every "my companion vanished / disappeared on reload / died and never returned" report.
--
-- Proven from the 13 Jul session log: 15 consecutive "Restoring companion 'Scarlett
-- Taylor'" lines, ~34s apart, right up to the moment the session ended -- while the
-- engine's own "bandit count" stayed pinned at 4 for all 128 samples across that window.
-- Not one zombie was ever spawned. Fifteen doomed commands, zero feedback.
--
-- BanditCustom.banditData/clanData are populated by BanditCustom.Load() reading
-- Zomboid/Lua/bandits/{bandits,clans}.txt. If nothing has called Load() in this session,
-- BOTH tables are empty and BOTH lookups fail -- which is very likely the actual cause,
-- since our own code only calls Load() on a manual spawn. So: check, and if a lookup
-- Returns a reason string when a companion CANNOT be brought back, or nil when she can.
--
-- MASSIVELY NARROWED IN v0.60, and this is a bug fix rather than a cleanup. It used
-- to require that BanditCustom still hold the companion's ORIGINAL Creator profile
-- (brain.bid) and clan (brain.cid), because the old restore went through the
-- server's spawnRestore, which looks both up and silently does nothing if either
-- misses. That made a companion's continued existence depend on a bandits.txt entry
-- the player can edit or delete at any time -- change your Bandit Creator setup and
-- every affected companion could no longer be restored, revived, or brought back
-- out of a vehicle. She was simply gone, with only a console line to say so. That
-- is a large part of "missing companions".
--
-- BeginRestore no longer uses spawnRestore at all: it creates the body with
-- createZombie and registers the brain in the bandit cluster itself. The roster
-- snapshot is the only thing required, so those are the only things checked.
--
-- The two callers still need this, for their own reasons: ReviveDowned must not
-- delete the corpse if she cannot come back, and BeginRide must refuse to board
-- rather than despawn her into a trip with no return.
function P.RestoreBlocker(brain)
    if not brain then return "no brain snapshot" end
    if not (brain.program and brain.program.name) then return "brain has no program" end
    return nil
end

-- ===== SYNCHRONOUS RESTORE (v0.60) =====
--
-- Creates the body ourselves and hands Bandits the brain, instead of asking the
-- server spawner for one and then trying to work out which zombie we got.
--
-- WHY THIS REPLACED THE OLD PATH. The old restore was
-- `sendClientCommand(player, 'Spawner', 'Restore', seed)`: an async request whose
-- reply we could only recognise by embedding a magic string in `brain.key` and
-- scanning every loaded bandit for it. That single design choice produced, in
-- order: orphan shells nobody claimed (the "6 of the same person" duplication),
-- a shell-adoption heuristic to catch them, a 10-attempt cap to stop the retry
-- loop, and a 20-second pending window during which anything could happen.
-- It also could not work at all unless BanditCustom still held the companion's
-- ORIGINAL Creator profile and clan (see RestoreBlocker) -- so editing or removing
-- a Bandit Creator profile silently made every one of that companion's restores a
-- no-op, i.e. she was simply gone. That is a large share of "missing companions",
-- and none of it is needed: the roster already holds her whole brain.
--
-- The mechanism we use instead is Bandits' OWN intended one, from
-- BanditUpdate.lua:1984-1992 -- if a zombie's id is present in the bandit cluster
-- store, BanditUpdate calls Banditize(zombie, brain) on it itself on the next tick
-- (and Zombify()s any flagged bandit that is NOT in the cluster, which is why the
-- registration is mandatory rather than optional). So: create the body, register
-- the brain under its id, and let Bandits adopt it. We keep the actual zombie
-- object in P.pending, so there is nothing to search for and nothing to guess.
--
-- Returns true if a body was created.
function P.BeginRestore(uid, entry)
    if P.pending[uid] then return false end
    if not (entry and entry.brain and entry.post) then return false end

    local post = entry.post
    local sq = getCell():getGridSquare(post.x, post.y, post.z or 0)
    if not BanditsNPC.Nav.IsSafeSquare(sq) then
        local alt
        pcall(function() alt = AdjacentFreeTileFinder.Find(sq, getSpecificPlayer(0)) end)
        if alt and BanditsNPC.Nav.IsSafeSquare(alt) then
            sq = alt
        else
            return false   -- nowhere safe to stand; try again next tick
        end
    end

    local zombie
    pcall(function()
        zombie = createZombie(sq:getX(), sq:getY(), sq:getZ(), nil,
                              (entry.brain.female and 100) or 0, IsoDirections.S)
    end)
    if not zombie then
        print("[BanditsNPC] Restore: createZombie failed for '" .. tostring(entry.brain.fullname or uid) .. "'")
        return false
    end

    local id = BanditUtils.GetZombieID(zombie)
    local gmd = id ~= nil and GetBanditClusterData(id) or nil
    if not gmd then
        pcall(function() zombie:removeFromWorld(); zombie:removeFromSquare() end)
        return false
    end

    -- OUTFIT-ID COLLISION GUARD. BanditUtils.GetZombieID derives from
    -- getPersistentOutfitID(), so it is NOT unique per zombie -- two survivors in
    -- the same outfit share an id. Writing over an occupied slot would hand our
    -- companion's brain to somebody else's body (and vice versa), which is a
    -- second, independent way to produce duplicates. If the slot is taken by a
    -- DIFFERENT companion, throw this body away and let the next pass roll again.
    -- ANY occupant blocks, not just one of OURS (v0.75.18). This required
    -- occupant.npcUid -- a field only our companions carry -- so when the colliding slot
    -- held a plain Bandits NPC (a raider, a neutral wanderer, another mod's bandit) the
    -- guard fell through and the write below overwrote THAT NPC's brain with our
    -- companion's. Missing from the cluster means Zombify() on its next BanditUpdate, so
    -- the victim quietly turned into an ordinary zombie -- and it looked like a Bandits
    -- bug, not ours. GetZombieID derives from getPersistentOutfitID() and is documented
    -- right above as not unique, so the collision is a matter of odds, not of misuse.
    --
    -- The slot being occupied by anyone at all means this body cannot have it. Backing
    -- out is already the correct, self-healing response: BeginRestore returns false, the
    -- caller treats that as transient, and the next attempt rolls a different id.
    local occupant = gmd[id]
    local occupied = occupant ~= nil
    if occupied and occupant.npcUid and occupant.npcUid == uid then
        occupied = false   -- our own entry from a previous attempt: reclaim it
    end
    if occupied then
        pcall(function() zombie:removeFromWorld(); zombie:removeFromSquare() end)
        return false
    end

    local brain = deepcopy(entry.brain)
    brain.id = id
    brain.key = nil
    brain.tasks = {}          -- never restore a live task queue
    brain.hostile = false
    brain.hostileP = false

    gmd[id] = brain
    pcall(function() TransmitBanditCluster(id) end)
    BanditBrain.Update(zombie, brain)

    -- Bandits banditizes it on its next update; ResolvePending then patches our
    -- own fields on. Short deadline -- if the flag has not appeared within a few
    -- seconds something is wrong and we would rather retry than leave a shell.
    P.pending[uid] = {
        zombie = zombie,
        brain = entry.brain,
        deadline = (getTimestampMs and getTimestampMs() or 0) + 8000,
    }
    print("[BanditsNPC] Restoring companion '" .. tostring(entry.brain.fullname or uid)
        .. "' at " .. tostring(sq:getX()) .. "," .. tostring(sq:getY()) .. " (id=" .. tostring(id) .. ")")
    return true
end

-- patch our custom fields onto the just-spawned shell, making it our companion again.
local function applyRestore(zombie, fullBrain)
    local brain = BanditBrain.Get(zombie)
    if not brain then return end
    for _, f in ipairs(CUSTOM_FIELDS) do
        if fullBrain[f] ~= nil then brain[f] = deepcopy(fullBrain[f]) end
    end
    brain.id = BanditUtils.GetCharacterID(zombie)   -- keep id consistent with the new zombie
    brain.key = nil
    brain.hostile = false
    brain.hostileP = false

    -- BEFORE Bandit.ApplyVisuals, NOT AFTER, AND THIS ORDERING IS THE WHOLE POINT.
    --
    -- v0.75.0 moved our hair colour out of Bandits' hairColor field (which holds a
    -- palette INDEX for clan NPCs) and put the migration in Appearance.Apply. That was
    -- too late: on a save written by v0.74.x, ApplyVisuals runs on the line below and
    -- reaches Bandit.GetHairColor(<our table>) -> tab[<table>] -> nil -> nil.r, dying
    -- INSIDE Bandits before our code gets a turn. Loading an existing save threw four
    -- times and the companions only came back after clicking through it (4 Aug log:
    -- "attempted index: r of non-table: null", Bandit.lua:171).
    --
    -- The brain has to be clean before anything else looks at it, so the migration
    -- happens here, on the restore path every companion goes through.
    if BanditsNPC.Appearance and BanditsNPC.Appearance.Migrate then
        BanditsNPC.Appearance.Migrate(brain)
    end
    BanditBrain.Update(zombie, brain)
    pcall(function() Bandit.ApplyVisuals(zombie, brain) end)
    -- ...then OUR redress on top. Bandit.ApplyVisuals builds brand-new ItemVisuals from the
    -- clothing TYPE STRINGS and knows nothing about brain.npcClothVar, so the engine re-rolled
    -- hue/texture-variant on every restore -- that is "her clothes changed colour after a
    -- load". ApplyClothingVisuals re-applies the saved appearance record (which does travel,
    -- npcClothVar is in CUSTOM_FIELDS above). For a companion recruited before v0.54 there is
    -- no record yet, so this captures whatever Bandits just rolled and pins it from then on:
    -- her colours settle permanently after one load instead of changing every time.
    if BanditsNPC.Interact and BanditsNPC.Interact.ApplyClothingVisuals then
        pcall(function() BanditsNPC.Interact.ApplyClothingVisuals(zombie, brain) end)
    end
    -- Bandit.SetHands takes an item TYPE STRING; passing the brain table made the engine
    -- call instanceItem(table) -> "no implementation found" error on every restore
    if Bandit.SetHands then
        pcall(function() Bandit.SetHands(zombie, (brain.weapons and brain.weapons.melee) or "Base.BareHands") end)
    end

    -- HER POCKETS. The restore builds a brand new body, so without this everything she
    -- was carrying is destroyed by any restore -- a knockdown, a reload, a recovery.
    -- Cleared from the brain immediately afterwards: the manifest is a snapshot to be
    -- re-taken from the live body on the next Record, and leaving it behind risks a
    -- later restore duplicating an inventory that has since changed.
    restoreItems(zombie, brain.npcItems)
    brain.npcItems = nil
    BanditBrain.Update(zombie, brain)

    if Bandit.ForceSyncPart then pcall(function() Bandit.ForceSyncPart(zombie, brain) end) end
    pcall(function() zombie:addLineChatElement("...", 0.7, 0.9, 0.7) end)
end

-- resolve any pending restore when its tagged shell appears (called per zombie update)
-- v0.60: identity is now the ZOMBIE OBJECT ITSELF. BeginRestore created the body,
-- so the only question left is whether Bandits has flagged it yet; there is no
-- key to embed, no scan, and no window in which an unclaimed shell can exist.
function P.ResolvePending(zombie)
    -- (PZ's Lua doesn't expose the global `next`, so test emptiness with pairs.)
    local hasPending = false
    for _ in pairs(P.pending) do hasPending = true; break end
    if not hasPending then return end
    pcall(function()
        if not (zombie and zombie:getVariableBoolean("Bandit")) then return end
        for uid, pend in pairs(P.pending) do
            if pend.zombie == zombie then
                applyRestore(zombie, pend.brain)
                P.pending[uid] = nil
                return
            end
        end
    end)
end

-- REVIVE-AS-DOWNED: the last line of defence against losing a companion. The engine can
-- KILL a bandit outright (e.g. a headshot -> bandit:Kill(), which no Lua handler can stop),
-- so once she's actually dead we re-spawn her DOWNED at the spot instead of leaving a corpse.
-- Any lethal hit thus becomes a knockdown you can recover from with "Help up".
--   * Gated on the immortal toggle. With immortality OFF, death is final and we simply drop
--     her from the roster so the lost-companion restore doesn't bring her back (that was the
--     "I killed her and she just spawned again" bug).
--   * Reuses BeginRestore (Bandits' cluster-correct spawn) with a forced downed program, and
--     removes the corpse so nothing lingers on the floor.
function P.ReviveDowned(zombie)
    if not zombie then return end
    -- By the time we run, Bandits' own OnZombieDead has already stripped the "Bandit"
    -- variable and removed the brain -- so identify her via the Remove-hook stash (the live
    -- brain is the fallback for any path where the engine handler didn't run first).
    local brain = P.removedBrains[zombie] or (BanditBrain and BanditBrain.Get(zombie))
    P.removedBrains[zombie] = nil
    if not (brain and brain.recruited) then return end

    local immortal = not (BanditsNPC.Opt and BanditsNPC.Opt("ImmortalCompanions", true) == false)
    if not immortal then
        P.Forget(brain)          -- mortal: stay dead, don't let the restore respawn her
        return
    end

    local uid = P.EnsureUid(brain)
    if not uid or P.pending[uid] then return end   -- a revive/restore already in flight

    local x, y, z = math.floor(zombie:getX()), math.floor(zombie:getY()), zombie:getZ()
    local snap = deepcopy(brain)
    -- Captured from the body that is about to be destroyed. Without this the revive
    -- spawns her empty, which is what "I helped her up and her inventory was gone"
    -- was: a day of felled timber deleted by a knockdown.
    snap.npcItems = captureItems(zombie)
    snap.downed = true
    snap.hostile = false
    snap.hostileP = false
    snap.downedPosed = nil
    snap.prevProgram = (brain.program and brain.program.name ~= "NPCDowned") and brain.program
                       or { name = "NPCCompanion", stage = "Prepare" }
    snap.program = { name = "NPCDowned", stage = "Prepare" }

    -- If the engine would discard the revive spawn, DON'T also delete the corpse: removing
    -- it on top of a restore that never lands is what turns "she went down" into "she
    -- evaporated with all her gear". Leave the body so the player at least sees what
    -- happened, keep her out of the restore roster so we don't loop on her forever, and say
    -- why in the console.
    local blocked = P.RestoreBlocker(snap)
    if blocked then
        print("[BanditsNPC] '" .. tostring(brain.fullname or uid) .. "' died and CANNOT be"
            .. " revived: " .. blocked .. " -- leaving the body instead of removing it."
            .. " Please report this line.")
        P.Forget(brain)
        return
    end

    P.BeginRestore(uid, { brain = snap, post = { x = x, y = y, z = z } })

    -- No corpse: the actual IsoDeadBody spawns AFTER OnZombieDead and INHERITS the zombie's
    -- modData, so mark it here and remove it in our OnDeadBodySpawn handler below. Also
    -- un-mark isDeadBandit so Bandits doesn't register the (about to vanish) corpse.
    pcall(function()
        local md = zombie:getModData()
        md.isDeadBandit = false
        md.tcRemoveCorpse = true
    end)
    pcall(function() zombie:removeFromSquare() end)
    pcall(function() zombie:removeFromWorld() end)
end

-- remove the corpse of a revived companion the moment it spawns (see ReviveDowned)
local function onDeadBodySpawn(body)
    pcall(function()
        local md = body and body.getModData and body:getModData()
        if not (md and md.tcRemoveCorpse) then return end
        md.tcRemoveCorpse = nil
        local sq = body.getSquare and body:getSquare()
        if sq and sq.removeCorpse then
            sq:removeCorpse(body, false)
        else
            pcall(function() body:removeFromSquare() end)
            pcall(function() body:removeFromWorld() end)
        end
    end)
end

-- ===== virtual vehicle ride =====
-- A zombie must NEVER be a real vehicle occupant: BaseVehicle.crash -> damagePlayers ->
-- addRandomDamageFromCrash calls getBodyDamage() on every seated character, and a zombie's
-- is null -> NPE escapes into IngameState.update -> the session aborts to the main menu
-- (tester crash, 4 Jul: "drove 2 meters, screen went dark"). So a riding companion is
-- DESPAWNED and tracked in the roster (entry.riding); when her master leaves the vehicle
-- she is respawned next to him through the same restore machinery.
function P.BeginRide(zombie, brain)
    if not (zombie and brain and brain.recruited) then return false end
    local uid = P.EnsureUid(brain)
    if not uid then return false end
    P.Record(zombie, brain)                     -- fresh snapshot is what she respawns from
    local entry = roster()[uid]
    if not entry then return false end
    -- NEVER despawn her for a ride we cannot undo. Boarding removes her from the world and
    -- relies entirely on the restore to put her back when the master steps out -- if the
    -- engine would silently discard that restore, riding is a one-way trip and the player
    -- just loses the companion. Refuse to board instead: she stays on foot, which is a
    -- visible inconvenience rather than an invisible permanent loss.
    local blocked = P.RestoreBlocker(entry.brain)
    if blocked then
        print("[BanditsNPC] '" .. tostring(brain.fullname or uid) .. "' will not ride: "
            .. blocked .. " -- she would not come back. Staying on foot.")
        return false
    end
    entry.riding = true
    P.ridingCount = (P.ridingCount or 0) + 1
    pcall(function() zombie:playSound("VehicleDoorOpen") end)
    pcall(function() zombie:removeFromSquare() end)
    pcall(function() zombie:removeFromWorld() end)
    return true
end

-- disembark riders whose master is on foot again (runs per player tick; the roster is a
-- handful of entries, and it early-outs while the player is still driving)
local lastInVehicleMs = {}   -- per player key: when we last saw them inside a vehicle
local function onPlayerUpdate(player)
    pcall(function()
        if not player then return end
        local un; pcall(function() un = player:getUsername() end)
        local key = un or 0
        local nowMs = (getTimestampMs and getTimestampMs()) or 0
        if player:getVehicle() then lastInVehicleMs[key] = nowMs; return end
        -- DEBOUNCE: require ~1s continuously ON FOOT before respawning riders. A
        -- momentary getVehicle()==nil while actually driving (seat switch, vehicle
        -- streaming hiccup) otherwise dumps the rider next to the moving car, and the
        -- follow program immediately re-boards her -- the "companion keeps getting in
        -- and out of my vehicle" report (11 Jul). A player who was never seen in a
        -- vehicle this session (fresh load with riding entries) restores immediately.
        local last = lastInVehicleMs[key]
        if last and nowMs - last < 1000 then return end
        -- EARLY-OUT WHEN NOBODY IS RIDING (v0.75.38). This walked the WHOLE ROSTER on
        -- every OnPlayerUpdate -- the debounce above only applies to a player who has
        -- recently been in a vehicle, so for anyone simply walking around this fell
        -- straight through to the loop, every tick, to do nothing. Harmless with three
        -- companions; it grows with the roster. Same shape as ResolvePending, which
        -- already early-outs on an empty P.pending.
        -- nil means "not counted yet" -- on a fresh load the roster can ALREADY hold
        -- riding entries (saved mid-ride) and starting the counter at 0 would skip them
        -- forever, stranding a companion inside a vehicle permanently. So the first call
        -- after load counts once and every call after that is free.
        if P.ridingCount == nil then
            local n = 0
            for _, e in pairs(roster()) do
                if e and e.riding then n = n + 1 end
            end
            P.ridingCount = n
        end
        if P.ridingCount <= 0 then return end
        for uid, entry in pairs(roster()) do
            if entry.riding then
                -- A rider with NO recorded owner used to match `owner == nil` and be
                -- disembarked beside whichever client happened to run this tick -- in MP
                -- that is a companion stepping out of a stranger's car. Legacy brains
                -- (recruited before masterName existed) are now only claimed by the
                -- player whose id matches, via the shared ownership test (v0.75.15).
                local owner = entry.brain and entry.brain.masterName
                local mine = (owner ~= nil and owner == un)
                    or (owner == nil and BanditsNPC.IsMine and BanditsNPC.IsMine(entry.brain))
                if mine then
                    -- spawn on a FREE adjacent tile, not blindly at player+1: that tile can
                    -- be inside the car or the door path (blocked the player's own
                    -- enter-vehicle pathfind right after a disembark)
                    local post = { x = math.floor(player:getX()) + 1,
                                   y = math.floor(player:getY()),
                                   z = math.floor(player:getZ()) }
                    pcall(function()
                        local sq = player:getCurrentSquare()
                        local free = sq and AdjacentFreeTileFinder and AdjacentFreeTileFinder.Find(sq, player)
                        if free then post = { x = free:getX(), y = free:getY(), z = free:getZ() } end
                    end)
                    entry.post = post

                    -- THE RIDING FLAG IS ONLY CLEARED WHEN A BODY IS ACTUALLY COMING
                    -- BACK. It used to be cleared first, unconditionally, and
                    -- BeginRestore returns false for perfectly ordinary reasons -- no
                    -- safe square beside the car yet, the outfit-id slot momentarily
                    -- held by another companion. When that happened the entry was left
                    -- neither riding nor embodied, which is a companion who got into a
                    -- vehicle and never came out: the reported "they enter but do not
                    -- leave and just disappear". Staying flagged as riding means this
                    -- runs again on the next player update and keeps trying.
                    if P.BeginRestore(uid, entry) then
                        entry.riding = nil
                        P.ridingCount = math.max(0, (P.ridingCount or 1) - 1)
                        pcall(function() player:playSound("VehicleDoorClose") end)
                    end
                end
            end
        end
    end)
end
if Events.OnPlayerUpdate then
    Events.OnPlayerUpdate.Add(onPlayerUpdate)
end

-- periodic: record loaded companions; restore missing ones whose post is loaded.
local missingSince = {}
local restoreAttempts = {}   -- per-uid, session-local; caps the spawn loop if the engine
                             -- can never materialize the restore (e.g. stale bid)
-- ===== MIGRATION: sweep pre-v0.60 orphan shells =====
--
-- A save made before v0.60 can contain bodies left over from the old async
-- restore: flagged as bandits, carrying a "bnwsrestore_<uid>_<rand>" key, but
-- never patched into a companion (no npcUid, not recruited). Until v0.60 the
-- Guardian dedupe recognised and swept them. That branch is gone with the rest of
-- the shell machinery, so without this they would simply stay in the world -- as
-- unowned bandits, most likely hostile, quite possibly standing in the player's
-- base. Runs on every tick that finds one; it is a cheap string compare on the
-- handful of loaded bandits, and stops finding anything once the save is clean.
local LEGACY_SHELL_PREFIX = "bnwsrestore_"
local function sweepLegacyShells()
    local swept = 0
    local n = #LEGACY_SHELL_PREFIX
    eachLoadedBandit(function(z)
        local b = BanditBrain.Get(z)
        if b and not b.npcUid and not b.recruited
           and type(b.key) == "string" and b.key:sub(1, n) == LEGACY_SHELL_PREFIX then
            pcall(function() z:removeFromSquare(); z:removeFromWorld() end)
            swept = swept + 1
        end
    end)
    if swept > 0 then
        print("[BanditsNPC] Migration: removed " .. tostring(swept)
            .. " orphan restore shell(s) left by a pre-v0.60 save.")
    end
end

function P.Tick()
    -- record loaded companions
    P.RecordAllLoaded()
    pcall(sweepLegacyShells)

    -- Expire stale pendings. v0.60: a pending now OWNS a body we created, so
    -- expiring one without cleaning up would leave exactly the orphan shell this
    -- rewrite exists to abolish. If Bandits never flagged it, it is not a
    -- companion and never will be -- remove it and let the next pass try again.
    local nowMs = getTimestampMs and getTimestampMs() or 0
    for uid, pend in pairs(P.pending) do
        if nowMs > pend.deadline then
            local flagged = false
            pcall(function()
                flagged = pend.zombie ~= nil and pend.zombie:getVariableBoolean("Bandit")
            end)
            if pend.zombie and not flagged then
                print("[BanditsNPC] Restore of '" .. tostring(uid)
                    .. "' was never adopted by Bandits; removing the unclaimed body.")
                pcall(function()
                    local id = BanditUtils.GetZombieID(pend.zombie)
                    local gmd = id ~= nil and GetBanditClusterData(id) or nil
                    if gmd and gmd[id] and gmd[id].npcUid == uid then
                        gmd[id] = nil
                        if TransmitBanditCluster then
                            pcall(function() TransmitBanditCluster(id) end)
                        end
                    end
                end)
                pcall(function()
                    pend.zombie:removeFromWorld()
                    pend.zombie:removeFromSquare()
                end)
            end
            P.pending[uid] = nil
        end
    end

    -- drop any death-stash entries that were never consumed (they're used same-tick;
    -- anything left after a minute is garbage and would pin dead zombie objects)
    for z in pairs(P.removedBrains) do P.removedBrains[z] = nil end

    if not restoreEnabled() then return end

    -- restore missing companions whose post is loaded (with a grace period).
    -- riding entries are HANDLED BY onPlayerUpdate -- she has no zombie on purpose.
    -- NOTE (29 Jul): the counter reset below used to live in a single `else` that covered
    -- "she's loaded" AND "she's riding" AND "a restore is already pending". That last one
    -- is the bug: BeginRestore sets P.pending for 20s, so on the very next minute-tick the
    -- pending entry sent us down the else branch and wiped restoreAttempts -- the 10-try
    -- give-up could therefore NEVER be reached, and a doomed restore retried forever. The
    -- 13 Jul log proves it: 15 attempts, no give-up line. Pending is now handled as its own
    -- state that leaves the counter alone.
    -- ===== DON'T FIGHT THE STREAMER (29 Jul) =====
    -- Bandits rebuilds its whole bandit cache from cell:getZombieList() every minute
    -- (BanditZombie.flush), and findLoadedByUid scans that same live list -- so a companion
    -- who is merely STREAMED OUT because the player walked away reads as "missing" here,
    -- identically to one who was genuinely lost. She isn't lost: her brain rides her
    -- zombie's modData (chunk-saved) and the cluster GMD, and she comes back with the chunk.
    --
    -- That distinction is the whole ballgame for a companion left on Stay/Guard/Relax at
    -- base while the player travels. Restoring her at that point SPAWNS A SECOND COPY that
    -- collides with the original when the chunk reloads -- which is where a good share of
    -- the historic "6 of the same person" duplication came from. The old `if sq then`
    -- guard was meant to cover this, but a loaded square does NOT imply a materialised
    -- zombie: chunks stay loaded well past the range where the engine stops keeping the
    -- zombie in the cell list, and that gap is exactly the restore/despawn tug-of-war.
    --
    -- So: only treat a companion as missing while the player is close enough that she
    -- SHOULD be materialised. Further out we do nothing at all -- no restore, no counter,
    -- no timer -- and let the engine's own chunk persistence do its job.
    local px, py, pz
    do
        local pl = getSpecificPlayer(0)
        if pl then px, py, pz = pl:getX(), pl:getY(), pl:getZ() end
    end
    local RESTORE_NEAR = 25   -- tiles; well inside the streaming radius (watch-list tunable)

    local nowHour = getGameTime():getWorldAgeHours()

    for uid, entry in pairs(roster()) do
        local rbrain = entry and entry.brain

        -- A TOMBSTONE. Nothing to restore and nothing to own; it exists only so a racing
        -- Record cannot re-roster somebody who was deliberately let go. Retired after a
        -- week of world time, by which point any stale snapshot that would have rebuilt
        -- her has had every chance to fire and the row is just weight in the save.
        if entry and entry.dismissed and not rbrain then
            if (nowHour - (entry.hour or 0)) > (24 * 7) then roster()[uid] = nil end
            missingSince[uid] = nil
            restoreAttempts[uid] = nil

        -- SOMEBODY ELSE'S -- drop it and move on. Pre-v0.75.4 saves accumulated other
        -- players' companions here, and every line below this treats a roster entry as
        -- ours to reposition and respawn. Only a POSITIVE identification deletes; see
        -- the note on ownedByOther.
        elseif ownedByOther(rbrain) then
            roster()[uid] = nil
            missingSince[uid] = nil
            restoreAttempts[uid] = nil
        -- Not provably ours yet (no player 0 during load, a brain with no recorded
        -- owner). Leave the entry completely alone rather than guessing in either
        -- direction.
        elseif not ownedByMe(rbrain) then
            missingSince[uid] = nil
        else

        -- A FOLLOWER'S POST IS THE PLAYER. postFor() records a following companion at her
        -- last known tile, which is wrong the moment she falls behind: outrun her (vehicle,
        -- sprint, a long stretch of road) and she streams out somewhere back down the map,
        -- so her stale post is both far from you and far from anywhere useful. Restoring her
        -- there would strand her; with the proximity gate below it would do nothing at all.
        -- Re-home a follower onto the player instead, which is where she was trying to get.
        -- ONLY EVER FOR OUR OWN: this line moved another player's companion onto us.
        --
        -- NOT GATED ON HOW RECENTLY SHE WAS SEEN, and that was considered and rejected
        -- (v0.77.14). Gating it would suppress the "she pops into existence next to you
        -- when you zone to an interior cell" duplicate, because after a cell teleport this
        -- line asserts she is missing from a tile she was never near. But the same gate
        -- also switches OFF the recovery this whole module exists for -- outrun a follower
        -- for a few minutes and she would stop being restorable to you at all, which is a
        -- far more common situation than RV Interiors and a far worse outcome than a
        -- duplicate. That trade needs a way to tell a teleport from ordinary travel, and a
        -- once-a-minute position sample cannot: a car covers more ground in a minute than
        -- most teleports do. The dismissal half of that report IS fixed, by the tombstone
        -- in P.Forget; the interior-cell half is still open.
        local prog = entry.brain and entry.brain.program and entry.brain.program.name
        if px and (prog == "NPCCompanion" or prog == nil) and not entry.riding then
            entry.post = { x = math.floor(px), y = math.floor(py), z = math.floor(pz) }
        end
        local post = entry.post
        local farAway = true
        if px and post then
            farAway = math.abs((post.z or 0) - pz) >= 1
                or BanditUtils.DistTo(px, py, post.x + 0.5, post.y + 0.5) > RESTORE_NEAR
        end
        -- Resolved once, because the two branches below both need it and findLoadedByUid
        -- walks every loaded bandit.
        local live = findLoadedByUid(uid)
        local liveRecruited = true
        if live then
            local lb = BanditBrain.Get(live)
            liveRecruited = (lb and lb.recruited) and true or false
        end

        if farAway and not P.pending[uid] and not entry.riding then
            -- out of range: she is streamed out, not lost. Leave her entirely alone.
            missingSince[uid] = nil
        elseif P.pending[uid] then
            -- a restore is in flight; wait for it to land or expire. Deliberately does NOT
            -- touch restoreAttempts -- that is what made the cap unreachable.
            missingSince[uid] = nil
        elseif live and not liveRecruited then
            -- SHE IS STANDING RIGHT THERE AND SHE IS NOT YOURS ANY MORE (v0.77.10).
            --
            -- This is the "I dismissed her, then met someone who looked like her and she
            -- re-joined by herself" report. Dismiss clears the brain and calls Forget, but a
            -- roster entry that survives anywhere -- a Record that raced the dismiss, a
            -- stale client store, the entry dropped on a different machine -- is enough: the
            -- restore rebuilds a body from the SNAPSHOT, and that snapshot still says
            -- recruited with you as master. The companion who "looks like the dismissed one"
            -- IS her, rebuilt from a copy taken before you let her go.
            --
            -- The live body is the authority over a snapshot of it. Drop the entry.
            roster()[uid] = nil
            missingSince[uid] = nil
            restoreAttempts[uid] = nil
        elseif not entry.riding and not live then
            -- v0.60: no more shell adoption. BeginRestore creates the body itself and
            -- keeps the object, so an unclaimed shell cannot exist for us to adopt.
            local post = entry.post
            local sq = post and getCell():getGridSquare(post.x, post.y, post.z or 0)
            if sq then
                missingSince[uid] = missingSince[uid] or nowMs
                if nowMs - missingSince[uid] > 8000 then
                    restoreAttempts[uid] = (restoreAttempts[uid] or 0) + 1
                    if restoreAttempts[uid] <= 30 then
                        -- v0.60: a `false` return is now TRANSIENT -- no safe square to
                        -- stand on yet, or the outfit-id slot is momentarily taken by
                        -- another companion. Both clear on their own. The old code
                        -- burned the entire attempt budget on a false because back then
                        -- it meant "the engine will refuse this forever" (a stale
                        -- Creator bid); doing that now would strand a companion for the
                        -- session over a passing obstruction. Just try again.
                        P.BeginRestore(uid, entry)
                    elseif restoreAttempts[uid] == 31 then
                        print("[BanditsNPC] restore for '" .. tostring((entry.brain and entry.brain.fullname) or uid)
                            .. "' has failed 30 times -- giving up this session, please report this line.")
                    end
                    missingSince[uid] = nil
                end
            else
                missingSince[uid] = nil   -- post not loaded; leave it (saved in its chunk)
            end
        else
            missingSince[uid] = nil
            restoreAttempts[uid] = nil
        end

        end   -- ownership gate opened above (ownedByOther / not ownedByMe / ours)
    end
end

-- refresh the roster from every loaded companion RIGHT NOW (used at save time and on
-- recruit, so the roster is never more than moments behind what gets written to disk)
-- ---------------------------------------------------------------------------
-- UNSTUCK -- the player's own recovery button
-- ---------------------------------------------------------------------------
--
-- Every automatic safeguard in this file is a guess about what went wrong. This is the
-- one the player drives, for when a companion is wedged in scenery, frozen mid-task,
-- standing in a field on the other side of the map, or has quietly become two of her.
-- It is deliberately blunt, because the situations it exists for are ones where being
-- careful has already failed.
--
--   1. gather EVERY loaded body claiming this uid
--   2. keep the one nearest the player and destroy the rest -- that is the duplicate
--      removal, and it runs first so the survivor is the one we then move
--   3. if nothing was loaded at all, spawn her from the roster beside the player
--   4. clear the task queue and the stuck tracker, and put her back on Follow
--   5. move her next to the player through Nav.Teleport, which refuses a square with
--      no floor -- an unstick that drops her off a balcony is not an improvement
--
-- Returns true plus a short reason for the UI to echo.
function P.Unstuck(uid)
    if not uid then return false end
    local player = getSpecificPlayer(0)
    if not player then return false end

    local bodies = {}
    eachLoadedBandit(function(z)
        local b = BanditBrain.Get(z)
        if b and b.npcUid == uid then bodies[#bodies + 1] = z end
    end)

    -- keep the nearest, remove the others
    local keep, keepD
    for i = 1, #bodies do
        local d = BanditUtils.DistTo(player:getX(), player:getY(), bodies[i]:getX(), bodies[i]:getY())
        if keepD == nil or d < keepD then keep, keepD = bodies[i], d end
    end
    local removed = 0
    for i = 1, #bodies do
        if bodies[i] ~= keep then
            pcall(function()
                bodies[i]:removeFromSquare()
                bodies[i]:removeFromWorld()
            end)
            removed = removed + 1
        end
    end

    local entry = roster()[uid]
    local sq = player:getCurrentSquare()
    local target
    pcall(function()
        target = sq and (AdjacentFreeTileFinder.Find(sq, player) or sq)
    end)

    if not keep then
        -- Nothing of her exists here. Respawn from the roster at the player's feet;
        -- this is the same path a lost-companion recovery uses, just without waiting
        -- for the eight-second confirmation.
        -- entry.brain, not just entry: a dismissed companion leaves a tombstone row with no
        -- brain, and respawning from one would rebuild somebody the player let go.
        if not (entry and entry.brain) then return false end
        if target then
            entry.post = { x = target:getX(), y = target:getY(), z = target:getZ() }
        end
        P.pending[uid] = nil          -- cancel any in-flight attempt so this one wins
        local ok = P.BeginRestore(uid, entry)
        return ok, ok and "respawned" or "failed"
    end

    local brain = BanditBrain.Get(keep)
    if brain then
        -- CLEARED WITH `false`, NOT `nil` (v0.75.7, audit Cluster C). Six fields are
        -- reset here but only `program` used to be transmitted, and the three upstream
        -- mechanisms compose badly: BanditBrain.Update writes LOCAL modData only; the
        -- server handler MERGES, copying just the keys it receives; and BanditUpdate
        -- reseeds the whole brain FROM THE CLUSTER whenever a body is re-banditized. So
        -- the five omitted fields survived in the cluster and came back the moment she
        -- streamed out and back -- the companion the player had just unstuck got stuck
        -- again by walking away and returning.
        --
        -- A nil cannot travel: the handler iterates pairs(args) and a nil key is simply
        -- absent, which is indistinguishable from "don't touch this field". `false` is
        -- the mod's established clear sentinel (nine other sync sites use it) and every
        -- consumer here already treats false as absent.
        brain.tasks = {}
        brain.routineTask = false
        brain.sleeping = false
        brain.program = { name = "NPCCompanion", stage = "Prepare" }
        BanditBrain.Update(keep, brain)
        pcall(function() Bandit.ForceStationary(keep, false) end)
        pcall(function() keep:getEmitter():stopAll() end)
        if Bandit and Bandit.ForceSyncPart then
            pcall(function() Bandit.ForceSyncPart(keep, {
                id = brain.id, program = brain.program, tasks = {},
                routineTask = false, sleeping = false,
            }) end)
        end
    end
    if BanditsNPC.Nav and BanditsNPC.Nav.ClearStuck then
        pcall(function() BanditsNPC.Nav.ClearStuck(uid) end)
    end
    if target and BanditsNPC.Nav then
        pcall(function() BanditsNPC.Nav.Teleport(keep, target, "player asked to unstick") end)
    end

    print("[BanditsNPC] Unstuck '" .. tostring((brain and brain.fullname) or uid)
        .. "': " .. tostring(removed) .. " duplicate(s) removed.")
    return true, (removed > 0) and "duplicates" or "moved"
end

function P.RecordAllLoaded()
    eachLoadedBandit(function(z)
        local b = BanditBrain.Get(z)
        if b and b.recruited then P.Record(z, b) end
    end)
end

-- Make sure the Bandit Creator profiles are in memory BEFORE anything needs to restore.
-- BanditCustom.banditData/clanData are only populated by BanditCustom.Load(); our own code
-- previously called it just on a manual spawn, so in a normal session (recruit a wild
-- survivor, never open the spawner) the tables can still be empty when spawnRestore does
-- its GetById/ClanGet lookups -- and both of those failing is exactly the silent no-op that
-- loses companions. Guarded two ways: only when the tables look EMPTY (so an in-memory
-- Creator edit is never discarded by a reload), and only once.
local ensuredCustomData = false
local function ensureBanditCustomLoaded()
    if ensuredCustomData then return end
    ensuredCustomData = true
    pcall(function()
        if not (BanditCustom and BanditCustom.Load) then return end
        local haveBandits = false
        for _ in pairs(BanditCustom.banditData or {}) do haveBandits = true; break end
        local haveClans = false
        for _ in pairs(BanditCustom.clanData or {}) do haveClans = true; break end
        if haveBandits and haveClans then return end   -- already populated, leave it alone
        BanditCustom.Load()
        print("[BanditsNPC] Bandit Creator profiles were not in memory at start -- loaded"
            .. " them so companion restores can resolve their bid/cid.")
    end)
end
Events.OnGameStart.Add(ensureBanditCustomLoaded)

Events.EveryOneMinute.Add(function() pcall(P.Tick) end)
if Events.OnSave then
    Events.OnSave.Add(function() pcall(P.RecordAllLoaded) end)
end
if Events.OnZombieUpdate then
    Events.OnZombieUpdate.Add(function(z) pcall(P.ResolvePending, z) end)
end
-- ===== KILL TALLY (v0.77.0) =====
--
-- "Kills - 34" is one of the lines the reworked Profile tab carries, and nothing counted
-- them. This does, from the one event that can: a zombie died, and getAttackedBy names who
-- put it down. Bandits reads the same field two lines apart in its own death handling
-- (BanditUpdate.lua:2323/2331), so the attribution is theirs as much as ours.
--
-- Ordering matters and is why this is a SEPARATE handler registered BEFORE ReviveDowned:
-- by the time the revive path runs, Bandits has already stripped the Bandit variable and
-- the brain, so asking "was the killer one of ours?" afterwards would always answer no.
local function tallyKill(deadZombie)
    if not deadZombie then return end
    pcall(function()
        local killer = deadZombie.getAttackedBy and deadZombie:getAttackedBy()
        if not killer then return end
        -- Only OUR companions keep a tally; the player has their own counter and a hostile
        -- bandit's score is nobody's business.
        if not (killer.getVariableBoolean and killer:getVariableBoolean("Bandit")) then return end
        local kb = BanditBrain and BanditBrain.Get(killer)
        if not (kb and kb.recruited) then return end
        kb.kills = (kb.kills or 0) + 1
        BanditBrain.Update(killer, kb)
    end)
end

if Events.OnZombieDead then
    Events.OnZombieDead.Add(function(z) pcall(tallyKill, z) end)
    Events.OnZombieDead.Add(function(z) pcall(P.ReviveDowned, z) end)
end
if Events.OnDeadBodySpawn then
    Events.OnDeadBodySpawn.Add(onDeadBodySpawn)
end
