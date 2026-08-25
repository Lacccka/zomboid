--
-- Bandits NPC - Companions Overhaul - Spawn handler (server)
--
-- Handles the 'BanditsNPC.Spawn' client command. We reuse the Bandits mod's
-- own, well-tested BanditServer.Spawner.Clan path (size=1) rather than its
-- buggy Spawner.Individual.
--
-- To spawn a SPECIFIC NPC by bid, we briefly wrap BanditCustom.GetFromClan so
-- the clan spawner only "sees" the chosen NPC, then restore it. This keeps the
-- engine's tested banditize/visuals path intact while letting us pick one.
--

local function doSpawn(player, args)
    if not args or not args.cid then return end

    if not (BanditCustom and BanditServer and BanditServer.Spawner and BanditServer.Spawner.Clan) then
        print("[BanditsNPC] Bandits server API not available; cannot spawn.")
        return
    end

    BanditCustom.Load()

    local clanArgs = {
        cid = args.cid,
        x = args.x or player:getX(),
        y = args.y or player:getY(),
        z = args.z or player:getZ(),
        program = args.program or "Companion",
        size = 1,
    }

    -- A companion (and a neutral wanderer) must not be hostile to the player
    -- regardless of the clan's default hostility, so force it here.
    if clanArgs.program == "Companion" or clanArgs.program == "NPCCompanion"
        or clanArgs.program == "NPCNeutral" then
        clanArgs.hostile = false
        clanArgs.hostileP = false
    end

    local bid = args.bid
    if bid then
        local orig = BanditCustom.GetFromClan
        BanditCustom.GetFromClan = function(cid)
            local all = orig(cid)
            if all and all[bid] then
                return { [bid] = all[bid] }
            end
            return all
        end

        local ok, err = pcall(function() BanditServer.Spawner.Clan(player, clanArgs) end)
        BanditCustom.GetFromClan = orig
        if not ok then
            print("[BanditsNPC] Spawn error: " .. tostring(err))
        end
    else
        BanditServer.Spawner.Clan(player, clanArgs)
    end

    if TransmitBanditModData then
        TransmitBanditModData()
    end
end

-- THE ONE PERMISSION CHECK. Every command in this file that changes world state goes
-- through it, so there is one place to read and one place to get right.
--
-- Only meaningful on a real server: in single player there is no access level and the
-- player IS the admin, so it passes. A CLIENT-SIDE check is a UI convenience and never
-- a permission -- anything reachable from a client can be sent by a modified client.
local function isPrivileged(player)
    if not (isServer and isServer()) then return true end
    local lvl = player and player.getAccessLevel and player:getAccessLevel()
    return lvl ~= nil and lvl ~= "None"
end

local function onClientCommand(module, command, player, args)
    if module ~= "BanditsNPC" then return end
    if command == "Spawn" then
        -- ADMIN-GATED since v0.75.2. Spawning is a debug/admin tool by design -- the
        -- context option that sends this is created only under
        -- `isDebugEnabled() or isAdmin()` (BanditsNPCSpawn.lua:139-146), and in normal
        -- play companions arrive through the wild-spawn system instead. Until now the
        -- handler trusted that UI gate, so any client could send the command directly
        -- and spawn arbitrary NPCs anywhere, with x/y/z/program/cid unvalidated
        -- (audit BLOCKER 1). The sibling ForceArrival below was already gated; this is
        -- the same check, hoisted so both share it.
        if not isPrivileged(player) then return end
        doSpawn(player, args)
    elseif command == "ForceArrival" then
        -- Debug button in the beacon window. In single player the window calls
        -- BanditsNPC.WildSpawn.Force directly and never gets here -- this exists so the
        -- button still works from a multiplayer client, where the server Lua is the only
        -- side that has the wild-spawn module at all. Admin-gated: the client-side check
        -- is a UI convenience, not a permission.
        if not isPrivileged(player) then return end
        if BanditsNPC.WildSpawn and BanditsNPC.WildSpawn.Force then
            BanditsNPC.WildSpawn.Force(player)
        end
    elseif command == "SetOpt" then
        -- World rules from the beacon's Settings tab (audit BLOCKER 4). The store is
        -- global and gets transmitted to everyone, so the server owns the write; the
        -- client only asks. BanditsNPC.ApplyOpt re-validates the name and value types
        -- itself -- this handler is reachable from any client, so it must not assume
        -- the payload came from our own UI.
        if not isPrivileged(player) then return end
        if type(args) ~= "table" then return end
        local value = args.value
        if args.clear then value = nil end
        BanditsNPC.ApplyOpt(args.name, value)
    elseif command == "SetSpawnConfig" then
        -- The Editor's Spawning tab. Its consumer (BanditsNPCWildSpawn:98) is server-side
        -- but the tab is client UI, so before this the admin's clan selection was written
        -- to a file on their OWN machine and the server never saw it -- the tab looked
        -- like it worked and did nothing. ApplySpawnConfig sanitises the payload itself,
        -- since this handler is reachable from any client.
        if not isPrivileged(player) then return end
        if type(args) ~= "table" then return end
        if BanditsNPC.Editor and BanditsNPC.Editor.ApplySpawnConfig then
            BanditsNPC.Editor.ApplySpawnConfig(args.clans)
        end
    elseif command == "SetSite" or command == "RemoveSite" then
        -- BEACON REGISTRY (v0.75.11). NOT admin-gated: any player may build and configure
        -- their own beacon. The check is OWNERSHIP instead -- R.MayWrite lets a player
        -- write a site they own, or one that is unowned/absent (a new placement, or a
        -- pre-v0.75.5 beacon with no owner stamped). That stops one player editing or
        -- deleting another's base while keeping the feature open to everyone.
        --
        -- The server owns the store and does the transmit, which is the vanilla shape
        -- (forageServer.lua:49) and sidesteps the open question of whether a
        -- client-originated ModData.transmit is honoured at all.
        local R = BanditsNPC.BeaconRegistry
        if not R then return end
        if type(args) ~= "table" then return end
        local who
        pcall(function() who = player and player:getUsername() end)
        if type(who) ~= "string" or who == "" then who = nil end
        if command == "SetSite" then
            R.ApplySite(args.key, args.site, who)
        else
            R.ApplyRemove(args.key, who)
        end
    end
end

Events.OnClientCommand.Add(onClientCommand)
