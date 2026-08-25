--
-- Bandits NPC - Companions Overhaul - Wild friendly spawns (server)
--
-- Occasionally drops a lone neutral survivor (recruitable) into the area, off-screen,
-- so you can meet companions naturally instead of always spawning them by hand.
-- Base Bandits almost never spawns friendly groups -- its friendly clans are gated to
-- a couple of specific in-game days -- so this fills that gap.
--
-- Kept deliberately rare and CAPPED: it won't add anyone while un-recruited neutral
-- survivors are already nearby, so the world never fills up with friendlies.
--

-- The per-hour chance AND the guaranteed-by hour count both live on
-- BanditsNPC.Beacon.ARRIVAL, so the beacon window explains exactly the numbers this
-- file uses. Read at call time, never copied here: two copies is how they drift.
local SPAWN_RANGE = 90   -- tiles: cap counts loose friendlies within this radius

local function opt(name, default)
    -- NO trailing `or default` (v0.75.28). It fired whenever BanditsNPC.Opt returned
    -- FALSE, so a boolean option deliberately set to off would read back as its default --
    -- silently, and in the "enabled" direction. Unreachable today (both callers are a
    -- string and an integer, and every number including 0 is truthy in Lua) but the mod
    -- has nine boolean world rules and adding a server-side read of any one of them would
    -- have produced exactly the class of silent wrong answer this audit kept finding.
    -- BanditsNPC.Opt already applies the default itself.
    if not (BanditsNPC and BanditsNPC.Opt) then return default end
    return BanditsNPC.Opt(name, default)
end

-- count alive, un-recruited, non-hostile bandit-NPCs already loaded near the player
local function looseFriendliesNear(player)
    local cell = getCell()
    local zl = cell and cell:getZombieList()
    if not zl then return 0 end
    local px, py = player:getX(), player:getY()
    local n = 0
    for i = 0, zl:size() - 1 do
        local z = zl:get(i)
        if z and z:isAlive() and z.getVariableBoolean and z:getVariableBoolean("Bandit") then
            local b = BanditBrain and BanditBrain.Get(z)
            if b and not (b.hostile or b.hostileP) and not b.recruited then
                if BanditUtils.DistTo(px, py, z:getX(), z:getY()) <= SPAWN_RANGE then
                    n = n + 1
                end
            end
        end
    end
    return n
end

local function trim(s) return (s and s:gsub("^%s*(.-)%s*$", "%1")) or "" end

-- one warning per bad Target Group name per session (see pickClan)
local warnedClan = {}

-- Choose a clan to spawn from. Returns cid, clan.
--   * if WildSpawnClan names a clan -> that one only (targeted)
--   * otherwise prefer friendly / companion-capable clans (they look like
--     survivors, not themed raiders); fall back to any clan.
local function pickClan()
    if not (BanditCustom and BanditCustom.ClanGetAll) then return nil end
    local all = BanditCustom.ClanGetAll()

    -- 1) sandbox option: one or more clan names (comma-separated). Player-facing.
    local raw = trim(opt("WildSpawnClan", ""))
    if raw ~= "" and raw:lower() ~= "any" then
        local wanted = {}
        for part in raw:gmatch("[^,]+") do
            local nm = trim(part):lower()
            if nm ~= "" then wanted[nm] = true end
        end
        local chosen = {}
        for cid, clan in pairs(all) do
            local nm = clan.general and clan.general.name
            if nm and wanted[trim(nm):lower()] then table.insert(chosen, cid) end
        end
        if #chosen > 0 then
            local cid = chosen[ZombRand(#chosen) + 1]
            return cid, all[cid], true
        end
        -- NAMED CLAN NOT FOUND -> we spawn nothing, deliberately (better than the wrong
        -- group). But this used to be SILENT, and silence here is indistinguishable from
        -- "wild spawns are broken": one typo in the Target Group option turns the entire
        -- wild-spawn system off for the whole save with no feedback anywhere.
        -- (This guard is precautionary. It was written on the mistaken belief that the
        -- author's save had a bad name here -- it did not; see the retraction in the v0.52.1
        -- version note. The trap is real regardless, so the warning stays.)
        -- Say so, once per session per name, and list what the player could have typed.
        if not warnedClan[raw] then
            warnedClan[raw] = true
            local names = {}
            for _, clan in pairs(all) do
                local nm = clan.general and clan.general.name
                if nm then table.insert(names, nm) end
            end
            table.sort(names)
            print("[BanditsNPC] WILD SPAWNS ARE OFF: the 'Target Group' sandbox option is \""
                .. tostring(raw) .. "\", which matches no clan, so no survivors will ever"
                .. " spawn. Clear the option to meet everyone, or use one of: "
                .. table.concat(names, ", "))
        end
        return nil
    end

    -- 2) clans ticked in the in-game editor's Spawning tab
    local sel = BanditsNPC.SpawnConfig and BanditsNPC.SpawnConfig.clans
    if sel then
        local chosen = {}
        for cid in pairs(sel) do if all[cid] then table.insert(chosen, cid) end end
        if #chosen > 0 then
            local cid = chosen[ZombRand(#chosen) + 1]
            return cid, all[cid], true
        end
    end

    local preferred, any = {}, {}
    for cid, clan in pairs(all) do
        table.insert(any, { cid, clan })
        local s = clan.spawn
        if s and (s.friendly or s.companion) then table.insert(preferred, { cid, clan }) end
    end
    local pool = (#preferred > 0) and preferred or any
    if #pool == 0 then return nil end
    local pick = pool[ZombRand(#pool) + 1]
    return pick[1], pick[2], false
end

-- THROTTLED, NOT ONE-SHOT. The old `warnedNoBeacon` latch printed the "no beacon"
-- explanation once and then went quiet for the rest of the session -- so the very
-- first in-game hour, before the player had built anything, consumed the only warning
-- there was ever going to be, and every later hour passed in total silence. A session
-- that produced no arrivals left nothing in the log to say which gate had closed.
-- Now every distinct reason is reported once per hour-block, and a repeat of the same
-- reason is suppressed rather than the whole message.
-- THE REASON HAS TO REACH THE PLAYER, NOT JUST THE CONSOLE (v0.77.13).
--
-- "the beacon says wait an hour and nobody ever came" is the second most common report
-- about this feature, and every single refusal below was already both known and explained
-- -- to a log file nobody opens. Meanwhile B.ArrivalStatus, the line in the beacon window,
-- checks a DIFFERENT and smaller set of conditions (rate/welcome/wanted/here) and so
-- cheerfully promises an arrival while this function is refusing for a reason it does not
-- know about. A UI that contradicts the system it describes is worse than one that says
-- nothing, because it turns "there are already three survivors nearby" into "this mod is
-- broken".
--
-- The reason is stored on the SITE, which costs nothing: it is a live reference into the
-- registry ModData (R.NearestWelcoming returns the stored table, not a copy), it is
-- already persisted with the save, and it already replicates to clients through pushSite.
-- So the window can read it on a dedicated server too, which is exactly where the
-- invisible refusals were universal rather than occasional.
-- PUSHED ONLY WHEN IT CHANGES. Writing site.arrWhy is enough in single player, where
-- server/ code runs on the client and this IS the client's store -- but on a dedicated
-- server the client has its own copy, and the whole point of surfacing the reason is that
-- dedicated servers are where the invisible refusals were universal. R.Push from the
-- server side goes straight to ApplySite -> ModData.transmit, and wireCopy carries every
-- field except the two derived caches, so arrWhy travels with no new plumbing.
--
-- Change-gated because it is otherwise a transmit per beacon per in-game hour forever,
-- for a string that is usually the same string it already was.
local lastReason
local function setWhy(site, reason)
    if not site or site.arrWhy == reason then return end
    site.arrWhy = reason
    pcall(function() BanditsNPC.BeaconRegistry.PushSite(site) end)
end

local function explain(reason, text, site)
    setWhy(site, reason)
    if lastReason == reason then return end
    lastReason = reason
    print("[BanditsNPC] no arrival this hour: " .. text)
end

-- Nothing is wrong: either someone arrived or the dice simply said no this hour. Both
-- must clear the stored reason, or a single bad hour would leave a stale explanation
-- sitting in the window for the rest of the save.
local function explainClear(site)
    lastReason = nil
    setWhy(site, nil)
end

-- ===== FORCED ARRIVALS (debug) =====
--
-- `force` runs the SAME pipeline the hourly roll runs -- same clan pick, same engine
-- spawner, same "did anybody actually appear" verification, same marker -- with only the
-- WAITING removed. That is deliberate: a debug button that spawns a survivor by some
-- other route tests the button, not the feature. Everything downstream (recruitment,
-- the neutral wander program, the arrival marker) is then exercised for real.
--
-- What force skips, and only this:
--   * the hourly probability roll        -- the entire point
--   * the beacon-broadcasting gate       -- so it works before one is built
--   * the "at your requested headcount" stop
--   * the nearby-un-recruited cap
--
-- What it does NOT skip is the loaded-square probe. generateSpawnPointHere returns an
-- empty list for an unloaded chunk and then does nothing SILENTLY, so a forced spawn
-- with no valid square would look exactly like a broken button.
--
-- Forced arrivals appear around the PLAYER, not around the beacon. A natural arrival
-- walks in to the transmitter, which is right when you are at base anyway; a debug
-- spawn is asked for because someone wants a survivor NOW, and putting them at a beacon
-- two hundred tiles away would defeat the purpose.
local function trySpawn(player, force)
    if not player or player:isDead() then
        return false, BanditsNPC.T("UI_BN_Dbg_NoPlayer", "No player.")
    end

    -- ===== BEACON GATE (v0.60) =====
    -- Survivors no longer wander in on a hidden dice roll; they answer a broadcast.
    -- No beacon, or every beacon switched off, means nobody arrives -- that is the
    -- whole mechanic, not a failure state. The old behaviour was an invisible
    -- ~8%/hour roll that a short session almost never triggered, which is why it
    -- was still unproven several versions after it shipped.
    -- Prefer the SHARED registry: on a dedicated server BanditsNPC.Beacon does not exist
    -- at all (it is a client/ module), which sent every roll down the `return false`
    -- branch below and killed wild arrivals for the whole mode (v0.75.11).
    local B = BanditsNPC.BeaconRegistry or BanditsNPC.Beacon
    local site
    if B and B.NearestWelcoming then
        if B.AnyWelcoming() then
            site = B.NearestWelcoming(player:getX(), player:getY(), player:getZ())
        end
        if not force then
            if not B.AnyWelcoming() then
                explain("nobeacon", "no broadcasting Survivors Beacon Station exists."
                    .. " Build one from the Build menu under Furniture, place it,"
                    .. " and switch it on.")
                return false
            end
            if not site then
                explain("nositehere", "a station is broadcasting but none on this floor.")
                return false
            end
        end
    elseif not force then
        return false   -- beacon module absent: no arrivals, rather than silent old behaviour
    end

    -- Forced with no usable station: the player is the origin and there is no registry
    -- entry to credit the arrival to. `real` keeps NoteArrival off a throwaway table.
    local real = site ~= nil
    if not site then
        site = { x = player:getX(), y = player:getY(), z = player:getZ() }
    end

    -- THE CAPACITY CHECK COMES BEFORE THE ROLL, not after, and that ordering is the
    -- whole reason the wait counter is trustworthy: hours spent with a full house are
    -- not hours spent waiting for someone, so they must not advance the guarantee.
    -- Counted from the ROSTER, so companions who are away or streamed out still count
    -- -- an unloaded companion is not a free slot.
    if not force then
        local room = true
        local counter = BanditsNPC.Persistence and BanditsNPC.Persistence.RosterCount
        if site.maxHere == 0 then
            room = false        -- the player has asked for nobody
        elseif site.maxHere and site.maxHere > 0 then
            if counter then
                room = counter() < site.maxHere
            elseif type(site.hereCount) == "number" then
                -- THE COUNT THE OWNER PUBLISHED (v0.77.10). The roster is a CLIENT store, so
                -- BanditsNPC.Persistence does not exist here and this branch used to fail
                -- closed -- correctly refusing to ignore a cap it could not check, but also
                -- meaning NO ARRIVALS EVER ON A DEDICATED SERVER. That is the "the beacon
                -- says wait an hour and nobody ever comes" report, and it was universal on
                -- dedicated servers rather than random.
                --
                -- The owning client now writes its companion count onto the site record,
                -- which already replicates here (BanditsNPCBeacon.State -> pushSite). It can
                -- be a little stale -- it refreshes whenever the owner's client touches the
                -- beacon -- and stale is fine for a cap: the worst case is one arrival too
                -- many or one hour's wait too long, against the alternative of never.
                room = site.hereCount < site.maxHere
            else
                -- Still fail closed when there is no count from either source: a cap that is
                -- too strict is a quiet disappointment, one that does not apply is a flood.
                -- Reachable now only for a site no owning client has touched since this
                -- build, so it resolves itself the first time they open the beacon.
                room = false
                explain("nocount", "no companion count has reached this server for that"
                    .. " beacon yet, so the per-beacon limit cannot be honoured. Open the"
                    .. " beacon once on the owner's client and arrivals will resume.",
                    real and site or nil)
                return false
            end
        end
        if not room then
            explain("full", "the station is at the number of companions you asked for.",
                real and site or nil)
            return false
        end

        -- THIS IS THE ONE THAT DEADLOCKS, AND IT DEADLOCKS ON ITS OWN OUTPUT (v0.77.13).
        -- The cap counts every un-recruited neutral within 90 tiles -- which includes the
        -- survivors THIS BEACON ALREADY DELIVERED and the player has not got round to
        -- recruiting. Three of those (the default) and the station goes quiet forever
        -- while the window still says someone is coming. The cap itself is right and is
        -- staying -- it is what stops the world filling up with friendlies -- so the fix
        -- is that the player can now see it, and knows that recruiting or moving on
        -- clears it.
        local cap = opt("WildSpawnCap", 3)
        if cap > 0 and looseFriendliesNear(player) >= cap then   -- 0 = unlimited
            explain("loose", "there are already un-recruited survivors nearby.",
                real and site or nil)
            return false
        end

        if not B.RollArrival(site) then
            explainClear(real and site or nil)   -- a plain unlucky hour is not a reason
            return false
        end
    end

    local cid, clan, targeted = pickClan()
    if not cid then
        if not force then
            explain("noclan", "no survivor group is available to draw from.",
                real and site or nil)
        end
        return false, BanditsNPC.T("UI_BN_Dbg_NoClan",
            "No survivor group to draw from - check the Target Group sandbox option.")
    end

    -- group size: targeted -> follow the clan's Group Size (min..max, capped);
    -- untargeted -> usually one, sometimes a pair. FORCED -> exactly one, because the
    -- button says "a survivor" and a targeted clan with a Group Size of six would
    -- otherwise drop six of them on the player.
    local size
    if force then
        size = 1
    elseif targeted and clan and clan.spawn then
        local lo = math.max(1, clan.spawn.groupMin or 1)
        local hi = math.max(lo, clan.spawn.groupMax or lo)
        size = math.min(8, lo + ZombRand(hi - lo + 1))   -- per-spawn group size, not a total cap
    else
        size = (ZombRand(100) < 15) and 2 or 1
    end

    -- THE SPAWN POINT MUST BE ON A LOADED SQUARE (29 Jul). Bandits' clan spawner ends in:
    --     spawnPoints = generateSpawnPointHere(player, x, y, z, size)
    --     if #spawnPoints > 0 then spawnGroup(spawnPoints, args) end
    -- and generateSpawnPointHere returns an EMPTY list whenever cell:getGridSquare(x,y,z)
    -- is nil -- i.e. whenever the target chunk is not loaded. So it does nothing, silently,
    -- with no error and no log. We were picking a blind point 50-65 tiles away, which is
    -- often outside the loaded area, and then setting the arrival icon REGARDLESS -- so the
    -- player got an icon, ran to it, and found nobody, because nobody was ever created.
    -- That is the "an icon appeared but the survivor never showed up" report.
    -- Probe outward-to-inward for a point whose square actually exists.
    -- v0.60: arrivals home in on the BEACON, not on the player. Someone answering
    -- a broadcast is walking to the transmitter, and it also means the player finds
    -- new people at base rather than materialising behind them in a field.
    -- The loaded-square requirement above still applies, so in practice arrivals
    -- happen while the player is near their own base -- which is when you want to
    -- meet them anyway.
    -- A forced arrival walks in from just outside view rather than from the far ring:
    -- "spawn me a survivor" means one you can go and talk to now, and a near ring is
    -- also far likelier to find a loaded square wherever the player happens to be.
    local ox, oy = site.x, site.y
    local rings = { 30, 26, 22, 18, 14 }
    if force then
        ox, oy = player:getX(), player:getY()
        rings = { 12, 9, 15, 7, 18 }
    end

    local function findLoadedSpawnPoint()
        local cell = getCell()
        if not cell then return nil end
        local oz = (force and player:getZ()) or site.z or player:getZ()
        for _ = 1, 10 do
            local ang = ZombRandFloat(0, 6.2831853)
            for _, d in ipairs(rings) do
                local x = math.floor(ox + math.cos(ang) * d)
                local y = math.floor(oy + math.sin(ang) * d)
                if cell:getGridSquare(x, y, oz) then return x, y, oz end
            end
        end
        return nil
    end

    -- count the bandit NPCs that exist right now, so we can tell whether the spawn
    -- actually produced anybody instead of assuming it did
    local function countBandits()
        local n = 0
        pcall(function()
            local zl = getCell():getZombieList()
            for i = 0, zl:size() - 1 do
                local z = zl:get(i)
                if z and z:getVariableBoolean("Bandit") then n = n + 1 end
            end
        end)
        return n
    end

    local x, y, z = findLoadedSpawnPoint()
    if not x then
        -- The wait counter is deliberately NOT reset here. The roll succeeded; only
        -- the geometry failed, so the next hour should try again immediately rather
        -- than starting the clock over. Once the counter is past the guarantee it
        -- stays past it, and the arrival happens the moment the player is somewhere
        -- the engine can actually place a person.
        if not force then
            explain("noloaded", "nowhere loaded near the station to walk in from -"
                .. " spend some time at your base and they will turn up.",
                real and site or nil)
        end
        return false, BanditsNPC.T("UI_BN_Dbg_NoSquare",
            "Nowhere loaded nearby to put anyone. Move somewhere open and try again.")
    end

    local spawned = false
    pcall(function()
        local before = countBandits()
        BanditServer.Spawner.Clan(player, {
            cid = cid, x = x, y = y, z = z, size = size,
            program = "NPCNeutral", hostile = false, hostileP = false,
        })
        if TransmitBanditModData then TransmitBanditModData() end

        -- ONLY promise a survivor if one now exists. An icon with nobody behind it is worse
        -- than no icon: the player drops what they are doing and walks to an empty field.
        if countBandits() <= before then
            print("[BanditsNPC] wild spawn produced no survivor at " .. x .. "," .. y
                .. " (engine spawner found no valid point) -- no icon shown.")
            return
        end

        -- Somebody actually exists: the station has been answered, so the clock that
        -- guarantees the NEXT arrival starts again from zero. Only for a REAL site --
        -- a forced spawn with no beacon has no registry entry to credit.
        spawned = true
        if real then B.NoteArrival(site) end
        explainClear(real and site or nil)
        print("[BanditsNPC] a survivor " .. (force and "was summoned (debug)" or "answered the beacon")
            .. " at " .. x .. "," .. y .. ".")

        -- top-of-screen marker pointing to the new survivor (reuses Bandits' event
        -- marker; respects their "arrival icon" sandbox toggle). The marker is given a
        -- STABLE id so the live tracker in BanditsNPCSpawn.lua can keep moving it to the
        -- survivor's real position instead of leaving it pinned to this spawn tile.
        -- UNSET MEANS ON, matching client/BanditsNPCSpawn.lua:176-179 (v0.75.42). These
        -- two read the SAME Bandits option with OPPOSITE nil-handling: this side treated
        -- "not set" as disabled, the client side treats it as enabled. So on any install
        -- where Bandits has not written that sandbox var, the client believed the arrival
        -- marker was on and the server never sent one -- a feature that is off while the
        -- code that draws it is waiting. `== false` is the same test the client uses, so
        -- an explicit disable still disables and only the default differs.
        if not (SandboxVars and SandboxVars.Bandits
                and SandboxVars.Bandits.General_ArrivalIcon == false) then
            local icon, color, desc = "media/ui/friend.png", { r = 0.5, g = 1, b = 0.5 }, BanditsNPC.T("UI_BN_Marker_Survivor", "Survivor nearby")
            if isServer and isServer() then
                sendServerCommand('Commands', 'SetMarker', { icon = icon, time = 1800, x = x, y = y, color = color, desc = desc })
            elseif BanditEventMarkerHandler and BanditEventMarkerHandler.set then
                BanditEventMarkerHandler.set("bnwsArrival", icon, 1800, x, y, color, desc)
            end
        end
    end)

    if not spawned then
        return false, BanditsNPC.T("UI_BN_Dbg_NoSpawn",
            "The engine spawner found no valid point. Try again from open ground.")
    end
    return true, BanditsNPC.TF("UI_BN_Dbg_Spawned",
        "A survivor is on their way in - %1 tiles away.",
        math.floor(BanditUtils.DistTo(player:getX(), player:getY(), x, y)))
end

-- Public entry point for the debug button in the beacon window. Same pipeline, no wait.
-- Returns ok, message -- the caller shows the message, so a failure says WHY rather than
-- looking like a dead button.
BanditsNPC.WildSpawn = BanditsNPC.WildSpawn or {}
function BanditsNPC.WildSpawn.Force(player)
    if not (BanditServer and BanditServer.Spawner and BanditServer.Spawner.Clan) then
        return false, BanditsNPC.T("UI_BN_Dbg_NoAPI", "The Bandits spawner is not available.")
    end
    local ok, res, msg = pcall(trySpawn, player or getSpecificPlayer(0), true)
    if not ok then
        print("[BanditsNPC] forced arrival failed: " .. tostring(res))
        return false, BanditsNPC.T("UI_BN_Dbg_Error", "The spawn failed - see the console.")
    end
    return res and true or false, msg
end

local function onEveryHours()
    if not (BanditServer and BanditServer.Spawner and BanditServer.Spawner.Clan) then return end
    pcall(function()
        -- ONE ROLL PER HOUR, NOT ONE PER PLAYER (v0.75.14). This looped every online
        -- player and called trySpawn for each, so the arrival CHANCE THE PLAYER SET was
        -- multiplied by the population: on an eight-player server "Rare" (8%/hour) became
        -- roughly eight rolls an hour, and the guarantee counter advanced eight times as
        -- fast. It never showed up because arrivals could not fire on a dedicated server
        -- at all until v0.75.11 -- the same way the capacity cap could not. Fixing the
        -- feature is what made both live.
        --
        -- The arrival still happens NEAR a player (trySpawn uses them to find the nearest
        -- broadcasting beacon and somewhere to stand), so one is picked at random each
        -- hour rather than dropping the concept. Single player is unchanged: one player
        -- means one roll, exactly as before.
        local players = getOnlinePlayers and getOnlinePlayers()
        if players and players:size() > 0 then
            trySpawn(players:get(ZombRand(players:size())))
        else
            trySpawn(getSpecificPlayer(0))
        end
    end)
end

Events.EveryHours.Add(onEveryHours)
