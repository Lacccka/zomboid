--
-- Bandits NPC - Companions Overhaul - Compatibility patches
--
-- Defensive fixes for Bandits-engine issues that affect our companions, plus
-- friendly-fire protection so the player can't hurt a recruited companion.
--

BanditsNPC = BanditsNPC or {}

local function isRecruitedCompanion(z)
    if not z then return false end
    local ok, isB = pcall(function() return z.getVariableBoolean and z:getVariableBoolean("Bandit") end)
    if not (ok and isB) then return false end
    local brain = BanditBrain and BanditBrain.Get(z)
    return brain and brain.recruited and true or false
end
BanditsNPC.IsRecruitedCompanion = isRecruitedCompanion

-- Bandits' BanditPlayer.CheckFriendlyFire calls attacker:isNPC(), which can be
-- unavailable on some builds and throws, aborting the hit handler. We wrap it:
-- a recruited companion NEVER reacts to a hit (no turning hostile); otherwise the
-- original runs (wrapped so a missing isNPC can't crash the game).
--
-- WEEK ONE COMPAT: Bandits Week One force-replaces this function at ITS file load
-- ("BanditPlayer.CheckFriendlyFire = BWOPlayer.CheckFriendlyFire"), which used to WIPE
-- our wrapper whenever BWO loaded after us. We now re-wrap at OnGameStart (all mod
-- files are loaded by then) and detect replacement by identity, so whichever version
-- is live -- Bandits' or BWO's -- ends up wrapped. Additionally, BWO's version treats
-- EVERY bandit in BanditZombie.GetAllB() as a crime witness and schedules CallCops/
-- CallSWAT on the player: recruited companions must never report their own master
-- (community report, 5 Jul). Its witness loop skips brains with hostileP set, so we
-- shield companions behind that flag just for the duration of the original call.
local function patchCheckFriendlyFire()
    if not (BanditPlayer and BanditPlayer.CheckFriendlyFire) then return end
    if BanditPlayer.CheckFriendlyFire == BanditsNPC._cffWrapper then return end

    local orig = BanditPlayer.CheckFriendlyFire
    local wrapper = function(bandit, attacker)
        if isRecruitedCompanion(bandit) then return end   -- friendly: ignore the hit
        local shielded = {}
        pcall(function()
            if not (BanditZombie and BanditZombie.GetAllB) then return end
            local all = BanditZombie.GetAllB()
            if not all then return end
            for _, w in pairs(all) do
                if w and w.brain and w.brain.recruited and not w.brain.hostileP then
                    w.brain.hostileP = true
                    table.insert(shielded, w.brain)
                end
            end
        end)
        pcall(orig, bandit, attacker)
        for _, b in ipairs(shielded) do b.hostileP = false end
    end
    BanditsNPC._cffWrapper = wrapper
    BanditPlayer.CheckFriendlyFire = wrapper
end

-- When the local player strikes a recruited companion, undo the damage so she
-- can't be hurt/killed by the player. (Damage from zombies/bandits is unaffected.)
local function onWeaponHitCharacter(wielder, victim, weapon, damage)
    local ok = pcall(function()
        if BanditsNPC.Opt and not BanditsNPC.Opt("ProtectCompanions", true) then return end
        if not (wielder and victim and wielder.isLocalPlayer and wielder:isLocalPlayer()) then return end
        if not isRecruitedCompanion(victim) then return end
        local brain = BanditBrain.Get(victim)
        local hp = (brain and brain.health) or 1.5
        victim:setHealth(hp)              -- restore to full (negate the hit)
    end)
    -- ignore failures; protection is best-effort across engine versions
end

-- Recruited companions must NOT run off to scavenge/equip random weapons. Bandits'
-- resupply kicks in whenever a gun slot is empty (true for most melee companions),
-- sending them to loot containers where ZALootWeapons overwrites their melee with
-- whatever it finds (the infamous frying pan). We no-op Resupply for our companions.
local function patchResupply()
    if not (BanditPrograms and BanditPrograms.Weapon and BanditPrograms.Weapon.Resupply) then return end
    if BanditsNPC._resupplyPatched then return end
    BanditsNPC._resupplyPatched = true

    local orig = BanditPrograms.Weapon.Resupply
    BanditPrograms.Weapon.Resupply = function(bandit)
        local brain = BanditBrain and BanditBrain.Get(bandit)
        if brain and brain.recruited then return {} end   -- companions keep what you gave them
        return orig(bandit)
    end
end

-- ===== panic/stress damping near friendly NPCs =====
-- Companions are IsoZombies, so the engine's panic system counts them as visible
-- zombies and the player stands next to their own follower permanently panicked
-- (reports 6-9 Jul). There is no proven Lua setter for the visible-zombie counts or
-- the panic increase rate (Bandits' own PanicHandler tried setPanicIncreaseValue and
-- is shipped DISABLED), so we do the next best proven thing: while at least one
-- friendly NPC is close and NO real threat is visible (same scan that gates
-- sleep), steadily decay the panic stat. The engine re-adds panic per tick from the
-- NPC "zombies"; this decay outpaces that ramp, and we stop touching panic the
-- moment a genuine zombie or hostile shows up.
--
-- Feature round 12 Jul: widened from recruited-only to ANY non-hostile bandit NPC.
-- Talking to a neutral survivor (recruit pitch, trade) kept the player panicked and
-- e.g. forced them back up out of chairs. Bandits' own disabled PanicHandler used
-- the same non-hostile test (BanditPlayer.lua:156), so the intent matches upstream.
local function isFriendlyBrain(brain)
    return (brain and (brain.recruited or not (brain.hostile or brain.hostileP))) and true or false
end

local lastPanicDampMs = 0
local function dampCompanionPanic(player)
    pcall(function()
        if not player or player:isDead() then return end
        local now = getTimestampMs()
        if now - lastPanicDampMs < 300 then return end
        lastPanicDampMs = now
        local panic = player:getStats():get(CharacterStat.PANIC)
        if not panic or panic <= 0 then return end
        -- HOW MANY friendly NPCs are nearby, not merely whether any is: the damping
        -- below scales with the count, because three followers frighten the engine's
        -- panic system three times as much as one. (Cheap: Bandits' own light cache.)
        local near = 0
        local cache = BanditZombie and BanditZombie.CacheLight
        if not cache then return end
        local px, py = player:getX(), player:getY()
        for _, z in pairs(cache) do
            if z.brain and isFriendlyBrain(z.brain) then
                local dx, dy = z.x - px, z.y - py
                if dx * dx + dy * dy <= 144 then near = near + 1 end   -- 12 tiles
            end
        end
        if near == 0 then return end

        -- WHEN REAL ZOMBIES ARE ALSO AROUND, DAMP LESS -- do not stop damping.
        --
        -- This used to `return` outright the moment any real zombie was in range, and
        -- that is why the terrified moodle kept being reported as unfixed: the whole
        -- point is that companions ARE IsoZombies, so the instant a genuine threat
        -- showed up the player was panicking at the horde AND at their own followers,
        -- with nothing subtracting the followers' share. The player was most panicked
        -- exactly when the companions were least relevant to it.
        --
        -- So: subtract roughly what the companions themselves are contributing, and
        -- leave the rest. Real danger still frightens the player, standing next to
        -- your own people does not.
        --
        -- -12/300ms with no zombies about (HDX field-tuned this exact symptom against
        -- our companions -- their PanicDamp v5.5.54 note; the decay must WIN, not tie,
        -- because any residual panic makes the engine cancel Rest and Sit-on-furniture).
        -- With zombies about, 4 per nearby companion, capped, so a crowd of followers
        -- cannot cancel out a horde.
        local amount = 12
        if BanditsNPC.RealZombiesNear and BanditsNPC.RealZombiesNear(player) then
            amount = math.min(12, 4 * near)
        end
        player:getStats():set(CharacterStat.PANIC, math.max(0, panic - amount))
    end)
end

-- ===== companions must not herd real zombies =====
--
-- IsoZombie carries a PUBLIC `group` field of type ZombieGroup, and ZombieGroup runs
-- the engine's herding: members drift toward the group and follow its leader. Bandits
-- never touches it -- a grep of the whole mod finds no reference -- so every companion
-- has been an ordinary member of a zombie herd since the day the mod shipped.
--
-- That is a very plausible mechanism for the reported "I cleared the area silently and
-- have been swarmed from every direction ever since": the companion is not making
-- noise, she IS the attractor, and she walks around with you all day.
--
-- The group is cleared rather than prevented, because the engine assigns it: remove()
-- is the public method for exactly this. Re-run on a slow sweep since the engine will
-- re-group her whenever it likes.
--
-- DO NOT ADD `z.group = nil` BACK. v0.74.2-v0.74.4 did, and it was an unclosable error
-- loop the moment a companion existed: 4 Aug, BanditsNPCCompat.lua:197,
-- "attempted index of non-table" out of KahluaThread.tableSet, repeating every sweep.
--
-- KAHLUA CAN READ A PUBLIC JAVA FIELD BUT CANNOT WRITE ONE. The read on the line below
-- is fine -- the log proves it, since the failure was on the write and never on the
-- read -- but tableSet has no path for a Java object and calls KahluaUtil.fail, which
-- PROPAGATES THROUGH pcall. Wrapping it, as that version did, bought nothing.
--
-- The write was also POINTLESS. javap -c on ZombieGroup.remove:
--      9: aload_1
--     10: aconst_null
--     11: putfield  // Field zombie/characters/IsoZombie.group
-- remove() nulls the zombie's own group field itself. One call does both halves.
local lastUngroupMs = 0
local function ungroupCompanions()
    pcall(function()
        local now = getTimestampMs()
        if now - lastUngroupMs < 2000 then return end
        lastUngroupMs = now

        local cell = getCell()
        local zl = cell and cell:getZombieList()
        if not zl then return end
        for i = 0, zl:size() - 1 do
            local z = zl:get(i)
            if z and z:getVariableBoolean("Bandit") then
                local b = BanditBrain and BanditBrain.Get(z)
                if b and (b.recruited or not (b.hostile or b.hostileP)) then
                    pcall(function()
                        local g = z.group
                        if g and g.remove then g:remove(z) end
                    end)
                end
            end
        end
    end)
end
if Events.OnPlayerUpdate then
    Events.OnPlayerUpdate.Add(ungroupCompanions)
end
if Events.OnPlayerUpdate then
    Events.OnPlayerUpdate.Add(dampCompanionPanic)
end

-- ===== animal stress damping near friendly NPCs =====
-- Same root cause as player panic: livestock treat our NPC "zombies" as zombies,
-- so animals penned near a guard post or followed home stay stressed (stress > 40
-- ruins milking/shearing per vanilla ISMilkAnimal/ISShearAnimal, and rams/bulls
-- have attackIfStressed). The stress ramp is Java-side with no increase-rate
-- setter, so mirror the panic damper: while a friendly NPC is within 12 tiles of
-- the ANIMAL and no real threat is near the player, walk the animal's stress back
-- down. animal:isDead (ISHutchUI:427), getStress (ISShearAnimal:16), setDebugStress
-- (ISAnimalContextMenu:601 -- its non-MP branch; the MP path is an admin-gated
-- server command, so in MP we do nothing rather than desync).
--
-- 29 Jul (42.20 audit, B5): this used to build its scan box around the PLAYER and
-- then ask "is an NPC within 12 tiles of this animal?" -- which can only ever be
-- true while the player personally stands next to both. The whole point of the
-- feature is livestock penned near a GUARDING companion, i.e. exactly when the
-- player is off looting somewhere else, so in its intended scenario the damper
-- never ran at all. It also cost 625 getGridSquare+getAnimals calls every second
-- for that nothing. Now we iterate the cell's own animal list (IsoCell.getAnimals(),
-- zero-arg, verified against projectzomboid.jar for 42.20; existence-guarded, and a
-- nil list just skips the tick) and test each animal against the companion
-- positions we already collected -- O(animals x companions), both tiny, and it
-- reaches the base pen whether or not the player is standing in it.
local lastAnimalDampMs = 0
local function dampAnimalStress(player)
    pcall(function()
        if isClient() then return end   -- MP: no non-admin stress setter; SP/host only
        if not player or player:isDead() then return end
        local now = getTimestampMs()
        if now - lastAnimalDampMs < 1000 then return end
        lastAnimalDampMs = now

        -- collect friendly-NPC positions once (usually a handful of entries)
        local cache = BanditZombie and BanditZombie.CacheLight
        if not cache then return end
        local npcs = {}
        for _, z in pairs(cache) do
            if z.brain and isFriendlyBrain(z.brain) then
                npcs[#npcs + 1] = { x = z.x, y = z.y, z = z.z }
            end
        end
        if #npcs == 0 then return end
        if BanditsNPC.RealZombiesNear and BanditsNPC.RealZombiesNear(player) then return end

        local cell = getCell()
        if not (cell and cell.getAnimals) then return end
        local animals = cell:getAnimals()
        if not animals then return end
        for i = 0, animals:size() - 1 do
            local a = animals:get(i)
            if a and not a:isDead() then
                local s = a:getStress()
                if s and s > 0 then
                    -- only calm animals a friendly NPC is actually near (the NPC is
                    -- the stressor; leave every other source of stress alone)
                    local ax, ay, az = a:getX(), a:getY(), a:getZ()
                    for _, n in ipairs(npcs) do
                        local ddx, ddy = n.x - ax, n.y - ay
                        if ddx * ddx + ddy * ddy <= 144                       -- 12 tiles
                                and math.abs((n.z or az) - az) < 1 then       -- same floor
                            a:setDebugStress(math.max(0, s - 5))
                            break
                        end
                    end
                end
            end
        end
    end)
end
if Events.OnPlayerUpdate then
    Events.OnPlayerUpdate.Add(dampAnimalStress)
end

-- ===========================================================================
-- THE SPAWN THAT LEAVES A SURVIVOR FROZEN (v0.75.62)
-- ===========================================================================
--
-- "When I click debug spawn NPC it shows an error ... I then walked over and found her
-- standing at the spot which is fine but she was like frozen in place" (author, 6 Aug).
-- The log says exactly what happened, and it is not a movement bug at all:
--
--     Lua((MOD:Bandits)).AddId(BanditCompatibility.lua:214)
--     Lua((MOD:Bandits)).UpdateItemsToSpawnAtDeath(Bandit.lua:721)
--     Lua((MOD:Bandits)).ApplyVisuals(Bandit.lua:283)
--     Lua((MOD:Bandits)).banditize(BanditServerSpawner.lua:420)
--     java.lang.NullPointerException ... at ReturnValues.put(ReturnValues.java:61)
--
-- AddId throws PART WAY THROUGH banditize. Everything after line 420 of the spawner --
-- the program, the stage, the rest of the setup -- never runs, so what is standing there
-- is a body with no working brain. It is not frozen; it was never finished. That is also
-- why Unstuck "unfroze" her: Unstuck rebuilds her state.
--
-- WHY IT THROWS. AddId is three lines and two of them are `instanceItem(itemName)` /
-- `item:setName(...)`. Both Base.IDcard and Base.IDcard_Female DO exist in this build
-- (scripts/generated/items/literature.txt:4799, 4834), so the id is not the problem; the
-- fault is inside Kahlua's own multi-overload dispatch for the global instanceItem, which
-- is the same "Cannot assign field callFrame" shape this project hit once already on
-- ISBaseTimedAction:begin. UNVERIFIED as to root cause -- the invoker is compiled and
-- could not be read -- so this does not try to explain it. It routes around it.
--
-- v0.75.62 SHIPPED THIS BROKEN AND MADE IT WORSE. It reached for
-- InventoryItemFactory.CreateItem -- which is Bandits' own B41 branch two lines further
-- down, so it looked like their precedent rather than my invention. It is B41 ONLY:
-- InventoryItemFactory does not exist in B42, so indexing it threw
-- "attempted index: CreateItem of non-table: null" -- a KahluaThread.tableget throw, which
-- goes straight THROUGH the pcall around it -- and the spawn died one line earlier than
-- before. Never reach for a global that has not been proven present on this build.
--
-- SO DO NOT MAKE AN ITEM DURING THE SPAWN AT ALL. The ID card is loot on a corpse; the
-- survivor is the point. AddId now only records the request, and a throttled tick drains
-- the queue afterwards, outside banditize, where the same call failing costs nothing but a
-- missing card. Anything still pending after one attempt is dropped rather than retried,
-- so a persistently broken engine call cannot turn into a log flood.
local pendingIds = {}

local function patchAddId()
    if not BanditCompatibility then return end
    if BanditCompatibility.tcAddIdPatched then return end
    BanditCompatibility.AddId = function(zombie, fullname)
        if not zombie then return end
        pendingIds[#pendingIds + 1] = { z = zombie, name = tostring(fullname) }
    end
    BanditCompatibility.tcAddIdPatched = true
end

local nextIdDrain = 0

local function drainPendingIds()
    if #pendingIds == 0 then return end
    local now = (getTimestampMs and getTimestampMs()) or 0
    if now < nextIdDrain then return end
    nextIdDrain = now + 500
    local job = table.remove(pendingIds, 1)      -- one per pass, always removed first
    if not job or not job.z then return end
    pcall(function()
        local itemName = "Base.IDcard"
        if job.z:isFemale() then itemName = "Base.IDcard_Female" end
        local item = instanceItem(itemName)
        if not item then return end
        item:setName("ID Card:" .. job.name)
        job.z:addItemToSpawnAtDeath(item)
    end)
end

Events.OnTick.Remove(drainPendingIds)
Events.OnTick.Add(drainPendingIds)

Events.OnGameStart.Add(patchCheckFriendlyFire)
Events.OnGameStart.Add(patchResupply)
Events.OnGameStart.Add(patchAddId)
patchCheckFriendlyFire()  -- also try immediately in case BanditPlayer is loaded
patchResupply()
patchAddId()

if Events.OnWeaponHitCharacter then
    Events.OnWeaponHitCharacter.Add(onWeaponHitCharacter)
end
