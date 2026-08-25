require("PZShareMapNotes_Shared")

local MOD_ID = PZShareMapNotes.MOD_ID

-- In-memory store of all shared strokes, keyed by ID
local allStrokes = {}
local strokeCount = 0

-- Rate limiting: playerLastCommand[username][command] = last accepted timestamp
local playerLastCommand = {}

-- Per-command rate limits, in milliseconds. ShareStroke is deliberately short:
-- it has to stay below the fastest a player can plausibly draw two separate
-- strokes (quick tick marks), or legitimate drawings get dropped. A sync
-- request is once-per-connect in normal use, so it gets a full second.
local RATE_LIMIT_MS = {
    [PZShareMapNotes.CMD_SHARE_STROKE]        = 250,
    [PZShareMapNotes.CMD_REQUEST_STROKE_SYNC] = 1000,
}
-- RemoveStroke is intentionally absent: it is already self-limiting (each call
-- deletes one stroke you own, and an unknown id returns before any persist),
-- and throttling it would make erase mode unusable.

--- Get sandbox option value with fallback.
local function getSandboxOption(name, default)
    if SandboxVars and SandboxVars.PZShareMapNotes and SandboxVars.PZShareMapNotes[name] ~= nil then
        return SandboxVars.PZShareMapNotes[name]
    end
    return default
end

-- ============================================================
-- Faction / Safehouse helpers
-- ============================================================
-- IMPORTANT: neither Faction:getPlayers() nor SafeHouse:getPlayers() includes
-- the group's OWNER. The engine keeps the owner in a separate field and tests
-- it separately everywhere it matters (Faction.getPlayerFaction,
-- Faction.isInSameFaction, SafeHouse.hasSafehouse and SafeHouse.playerAllowed
-- all do `owner.equals(x) || players.contains(x)`). Testing only the players
-- list silently excludes every faction leader and safehouse owner from their
-- own group — they would see no shared drawings and share none.

--- Does `username` belong to this faction (owner or member)?
local function factionHas(faction, username)
    return faction:isOwner(username) or faction:isMember(username)
end

--- Does `username` belong to this safehouse (owner or member)?
local function safehouseHas(safehouse, username)
    return tostring(safehouse:getOwner()) == username
        or safehouse:getPlayers():contains(username)
end

--- Find the faction a player belongs to, or nil.
local function getPlayerFaction(username)
    if not Faction then return nil end
    local factions = Faction.getFactions()
    if not factions then return nil end
    for i = 0, factions:size() - 1 do
        local faction = factions:get(i)
        if factionHas(faction, username) then
            return faction
        end
    end
    return nil
end

--- Find the safehouse a player belongs to, or nil.
--- The engine method is getSafehouseList(). There is no SafeHouse.getSafehouses()
--- in B42 — calling it throws "Object tried to call nil", which is what broke
--- Safehouse sharing mode entirely until this was corrected.
local function getPlayerSafehouse(username)
    if not SafeHouse then return nil end
    local safehouses = SafeHouse.getSafehouseList()
    if not safehouses then return nil end
    for i = 0, safehouses:size() - 1 do
        local sh = safehouses:get(i)
        if safehouseHas(sh, username) then
            return sh
        end
    end
    return nil
end

--- Build a predicate answering "does `otherUsername` share `subjectUsername`'s
--- group?" under the current sharing mode. The subject's group is resolved once
--- here, so a caller iterating many players (or many strokes) doesn't rescan
--- the whole faction/safehouse list per item.
---
--- Group membership is symmetric, so one builder serves both directions: the
--- sync path passes the viewer as subject and tests each stroke's author, the
--- broadcast path passes the author as subject and tests each online viewer.
---
--- The admin bypass is deliberately NOT handled here — it always refers to the
--- viewer, which is the subject in one direction and the other in the other.
local function makeGroupFilter(subjectUsername)
    local option = getSandboxOption("AutoShareSymbols", 1)

    -- Disabled (4): drawings stay private to their author
    if option == 4 then
        return function(otherUsername) return otherUsername == subjectUsername end
    end

    local group, has
    if option == 2 then      -- Faction
        group, has = getPlayerFaction(subjectUsername), factionHas
    elseif option == 3 then  -- Safehouse
        group, has = getPlayerSafehouse(subjectUsername), safehouseHas
    else
        -- Everyone (1), and any unrecognised value: no filtering
        return function() return true end
    end

    return function(otherUsername)
        if otherUsername == subjectUsername then return true end
        if not group then return false end
        return has(group, otherUsername)
    end
end

--- Hand a command to the local client half, in-process.
---
--- Singleplayer only. sendServerCommand cannot reach the client there:
--- LuaManager wraps both overloads in `if (GameServer.server)`, false solo, and
--- nothing forwards them to SinglePlayerServer.sendServerCommand (Java-only).
--- The inbound direction is fine — sendClientCommand loops through
--- SinglePlayerClient into SinglePlayerServer and fires OnClientCommand — so
--- until this existed, solo play accepted every stroke, stored it and saved it,
--- then never echoed it back. The client renders only what OnServerCommand
--- gives it, so a drawing vanished the instant the mouse was released, and a
--- sync request after reload returned nothing. (Workshop report, realch,
--- 42.20 solo.)
---
--- Both halves share one Lua VM in singleplayer, so the client dispatcher is a
--- plain function call away.
local function deliverToLocalClient(command, args)
    local deliver = PZShareMapNotes.deliverLocalServerCommand
    if not deliver then
        print("[PZShareMapNotes] Singleplayer: client half not loaded, dropped " .. tostring(command))
        return
    end
    -- In multiplayer these halves are separate processes and a client-side
    -- error cannot touch the server's bookkeeping. Keep that true solo: without
    -- the pcall a throw in a client handler would unwind back through
    -- handleShareStroke and skip persistStrokes(), leaving allStrokes and the
    -- save data out of step.
    local ok, err = pcall(deliver, command, PZShareMapNotes.copyPayload(args))
    if not ok then
        print("[PZShareMapNotes] ERROR delivering " .. tostring(command)
            .. " locally: " .. tostring(err))
    end
end

--- Broadcast a stroke command respecting the sharing mode.
--- For "Everyone" this is a plain broadcast (zero overhead).
--- For "Faction" / "Safehouse" it sends only to matching online players.
--- For "Disabled" it sends only to the author (private drawings).
local function broadcastStroke(command, args, authorUsername)
    -- Singleplayer: the author is the only viewer, and every sharing mode
    -- resolves to "you see your own drawings" — makeGroupFilter short-circuits
    -- on viewer == author, Disabled included. getOnlinePlayers() is a
    -- multiplayer construct, so don't walk it here.
    if PZShareMapNotes.isSinglePlayer() then
        deliverToLocalClient(command, args)
        return
    end

    local option = getSandboxOption("AutoShareSymbols", 1)

    if option == 1 then
        -- Everyone: broadcast to all
        sendServerCommand(MOD_ID, command, args)
        return
    end

    -- Disabled (4), Faction (2), or Safehouse (3): filtered send
    local sees = makeGroupFilter(authorUsername)
    local players = getOnlinePlayers()
    for i = 0, players:size() - 1 do
        local p = players:get(i)
        -- Admins see everything regardless of mode.
        if tostring(p:getAccessLevel()) == "admin" or sees(p:getUsername()) then
            sendServerCommand(p, MOD_ID, command, args)
        end
    end
end

--- Send a command to a specific player.
local function sendToPlayer(player, command, args)
    if PZShareMapNotes.isSinglePlayer() then
        deliverToLocalClient(command, args)
        return
    end
    sendServerCommand(player, MOD_ID, command, args)
end

--- Persist strokes to GlobalModData.
--- Flattens the points array to a string to avoid deep nesting that PZ's
--- serializer silently drops.
local function persistStrokes()
    local modData = getGameTime():getModData()
    local saveTable = {}
    for id, stroke in pairs(allStrokes) do
        saveTable[id] = {
            id = stroke.id,
            author = stroke.author,
            r = stroke.r,
            g = stroke.g,
            b = stroke.b,
            pointData = stroke.pointData or PZShareMapNotes.serializePoints(stroke.points),
        }
    end
    modData["PZShareMapNotes_Strokes"] = saveTable
end

--- Load strokes from GlobalModData.
--- Parses pointData strings back into points arrays for runtime use.
--- Backward compatible: strokes with an existing points field are used as-is.
local function loadStrokes()
    local modData = getGameTime():getModData()
    local saved = modData["PZShareMapNotes_Strokes"]
    if saved and type(saved) == "table" then
        allStrokes = {}
        strokeCount = 0
        for id, stroke in pairs(saved) do
            if stroke.pointData and type(stroke.pointData) == "string" then
                stroke.points = PZShareMapNotes.deserializePoints(stroke.pointData)
                stroke.pointData = nil
            end
            if stroke.points and #stroke.points >= 2 then
                allStrokes[id] = stroke
                strokeCount = strokeCount + 1
            end
        end
        print("[PZShareMapNotes] Loaded " .. strokeCount .. " strokes from save data.")
    else
        print("[PZShareMapNotes] No saved strokes found, starting fresh.")
    end
end

--- Count strokes for a given author.
local function countStrokesByAuthor(author)
    local count = 0
    for _, stroke in pairs(allStrokes) do
        if stroke.author == author then
            count = count + 1
        end
    end
    return count
end

--- Validate a stroke structure.
local function validateStroke(stroke)
    if type(stroke) ~= "table" then return false end
    if not stroke.points or type(stroke.points) ~= "table" then return false end
    if #stroke.points < 2 then return false end
    if #stroke.points > PZShareMapNotes.MAX_POINTS_PER_STROKE then return false end
    for _, pt in ipairs(stroke.points) do
        if type(pt) ~= "table" then return false end
        if not pt.x or not pt.y then return false end
        if type(pt.x) ~= "number" or type(pt.y) ~= "number" then return false end
    end
    return true
end

--- Check the rate limit for a player/command pair. Returns true if allowed.
--- Buckets are per command so a burst of stroke uploads can't block a sync
--- request (and vice versa). Commands with no configured limit always pass.
local function checkRateLimit(username, command)
    local limit = RATE_LIMIT_MS[command]
    if not limit then return true end

    local buckets = playerLastCommand[username]
    if not buckets then
        buckets = {}
        playerLastCommand[username] = buckets
    end

    local now = getTimestampMs()
    if (now - (buckets[command] or 0)) < limit then
        return false
    end
    buckets[command] = now
    return true
end

--- Handle ShareStroke command from a client.
local function handleShareStroke(player, args)
    local username = player:getUsername()

    if not args then
        print("[PZShareMapNotes] Invalid stroke data from " .. username)
        return
    end

    if not checkRateLimit(username, PZShareMapNotes.CMD_SHARE_STROKE) then
        print("[PZShareMapNotes] Rate limited ShareStroke from " .. username)
        return
    end

    -- Reject oversized payloads before parsing: deserializePoints would
    -- otherwise walk the whole string just for validateStroke to reject it.
    if type(args.pointData) == "string"
        and #args.pointData > PZShareMapNotes.MAX_POINT_DATA_CHARS then
        print("[PZShareMapNotes] Oversized stroke payload from " .. username
            .. " (" .. #args.pointData .. " chars)")
        return
    end

    -- Deserialize pointData string from client into points array for validation
    if args.pointData and type(args.pointData) == "string" then
        args.points = PZShareMapNotes.deserializePoints(args.pointData)
    end

    if not validateStroke(args) then
        print("[PZShareMapNotes] Invalid stroke data from " .. username)
        return
    end

    local maxTotal = getSandboxOption("MaxStrokesTotal", PZShareMapNotes.MAX_STROKES)
    if strokeCount >= maxTotal then
        print("[PZShareMapNotes] Server stroke limit reached (" .. maxTotal .. ")")
        return
    end

    local maxPerPlayer = getSandboxOption("MaxStrokesPerPlayer", PZShareMapNotes.MAX_STROKES_PER_PLAYER)
    local playerCount = countStrokesByAuthor(username)
    if playerCount >= maxPerPlayer then
        print("[PZShareMapNotes] Player " .. username .. " stroke limit reached (" .. maxPerPlayer .. ")")
        return
    end

    args.author = username
    args.id = PZShareMapNotes.generateStrokeId(username)

    -- Sanitize optional color values
    if args.r then args.r = tonumber(args.r) end
    if args.g then args.g = tonumber(args.g) end
    if args.b then args.b = tonumber(args.b) end

    -- Keep pointData string in sync for network/persistence
    args.pointData = PZShareMapNotes.serializePoints(args.points)

    allStrokes[args.id] = args
    strokeCount = strokeCount + 1

    -- Broadcast a network-safe copy with pointData string instead of points table
    local networkStroke = {
        id = args.id,
        author = args.author,
        r = args.r,
        g = args.g,
        b = args.b,
        pointData = args.pointData,
    }
    broadcastStroke(PZShareMapNotes.CMD_BROADCAST_STROKE_ADD, networkStroke, username)
    persistStrokes()

    print("[PZShareMapNotes] Stroke added by " .. username .. ": " .. args.id .. " (" .. #args.points .. " points)")
end

--- Handle RemoveStroke command from a client.
local function handleRemoveStroke(player, args)
    local username = player:getUsername()

    if not args or not args.id then
        return
    end

    local stroke = allStrokes[args.id]
    if not stroke then
        return
    end

    if stroke.author ~= username and tostring(player:getAccessLevel()) ~= "admin" then
        print("[PZShareMapNotes] Player " .. username .. " tried to remove stroke owned by " .. stroke.author)
        return
    end

    local strokeAuthor = stroke.author
    allStrokes[args.id] = nil
    strokeCount = strokeCount - 1

    broadcastStroke(PZShareMapNotes.CMD_BROADCAST_STROKE_REMOVE, { id = args.id }, strokeAuthor)
    persistStrokes()

    print("[PZShareMapNotes] Stroke removed by " .. username .. ": " .. args.id)
end

--- Build a network-safe stroke copy with pointData string instead of points table.
local function toNetworkStroke(stroke)
    return {
        id = stroke.id,
        author = stroke.author,
        r = stroke.r,
        g = stroke.g,
        b = stroke.b,
        pointData = stroke.pointData or PZShareMapNotes.serializePoints(stroke.points),
    }
end

--- Handle RequestStrokeSync command from a client.
local function handleRequestStrokeSync(player, args)
    local username = player:getUsername()

    if not checkRateLimit(username, PZShareMapNotes.CMD_REQUEST_STROKE_SYNC) then
        print("[PZShareMapNotes] Rate limited RequestStrokeSync from " .. username)
        return
    end

    local isAdmin = tostring(player:getAccessLevel()) == "admin"
    print("[PZShareMapNotes] Stroke sync requested by " .. tostring(username) .. " (isAdmin=" .. tostring(isAdmin) .. ")")

    -- Resolve this viewer's group once, then test each stroke's author against
    -- it, rather than re-resolving the author's group for every stroke.
    local sees = makeGroupFilter(username)
    local strokeList = {}
    for id, stroke in pairs(allStrokes) do
        if isAdmin or sees(stroke.author) then
            table.insert(strokeList, toNetworkStroke(stroke))
        end
    end

    local batchSize = PZShareMapNotes.STROKE_SYNC_BATCH_SIZE
    local totalStrokes = #strokeList
    print("[PZShareMapNotes] Sending " .. totalStrokes .. " strokes (batchSize=" .. batchSize .. ")")

    if totalStrokes <= batchSize then
        sendToPlayer(player, PZShareMapNotes.CMD_FULL_STROKE_SYNC, { strokes = strokeList })
    else
        local totalBatches = math.ceil(totalStrokes / batchSize)
        for i = 1, totalBatches do
            local batchStart = (i - 1) * batchSize + 1
            local batchEnd = math.min(i * batchSize, totalStrokes)
            local batch = {}
            for j = batchStart, batchEnd do
                table.insert(batch, strokeList[j])
            end
            sendToPlayer(player, PZShareMapNotes.CMD_FULL_STROKE_SYNC_BATCH, {
                strokes = batch,
                batchIndex = i,
                totalBatches = totalBatches,
            })
        end
    end
    print("[PZShareMapNotes] Stroke sync sent successfully")
end

--- Main command dispatcher.
local function onClientCommand(module, command, player, args)
    if module ~= MOD_ID then return end

    -- Every handler dereferences the player; without one there is nothing to
    -- validate a request against.
    if not player then
        print("[PZShareMapNotes] Ignoring " .. tostring(command) .. " with no player")
        return
    end

    -- PZ drops empty tables during network serialization, so a client that
    -- sent {} arrives here with args == nil. Normalise before any handler
    -- indexes it.
    args = args or {}

    print("[PZShareMapNotes] Server received command: " .. tostring(command) .. " from " .. tostring(player:getUsername()))

    local handler
    if command == PZShareMapNotes.CMD_SHARE_STROKE then
        handler = handleShareStroke
    elseif command == PZShareMapNotes.CMD_REMOVE_STROKE then
        handler = handleRemoveStroke
    elseif command == PZShareMapNotes.CMD_REQUEST_STROKE_SYNC then
        handler = handleRequestStrokeSync
    else
        return
    end

    -- All three handlers mutate shared state (allStrokes/strokeCount) before
    -- broadcasting, so an uncaught error would leave the store inconsistent
    -- AND kill the dispatcher for the rest of the call.
    local ok, err = pcall(handler, player, args)
    if not ok then
        print("[PZShareMapNotes] ERROR in " .. tostring(command) .. ": " .. tostring(err))
    end
end

--- Load strokes when global mod data initializes.
local function onInitGlobalModData(isNewGame)
    loadStrokes()
end

--- Persist on server save events.
local function onServerSave()
    persistStrokes()
    print("[PZShareMapNotes] Strokes persisted on server save.")
end

-- Register events
Events.OnClientCommand.Add(onClientCommand)
Events.OnInitGlobalModData.Add(onInitGlobalModData)

if Events.OnServerStartSaving then
    Events.OnServerStartSaving.Add(onServerSave)
end

-- Periodic persistence every in-game hour
if Events.EveryHours then
    Events.EveryHours.Add(onServerSave)
end

-- Exposed for PZTestRunner (T8/T9 in PZShareMapNotes_Tests.lua). The visibility
-- filter short-circuits on "viewer == author", so normal solo play can never
-- reach these lookups — yet they are exactly where the owner-not-in-getPlayers()
-- and getSafehouses() bugs lived. Exporting them lets a single player verify
-- both against the real engine instead of only against an offline harness.
-- Absent on a remote client of a dedicated server (server Lua isn't loaded
-- there); the tests skip in that case rather than fail.
PZShareMapNotes._debugGetPlayerFaction   = getPlayerFaction
PZShareMapNotes._debugGetPlayerSafehouse = getPlayerSafehouse

print("[PZShareMapNotes] Server module loaded.")
