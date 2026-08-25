-- ============================================================
-- PagerMod_Server.lua
-- Authoritative routing + persistence for the pager network.
-- Runs on the host (dedicated server or singleplayer host).
-- ============================================================

require "PagerMod_Shared"

local C2S = PagerMod.C2S
local S2C = PagerMod.S2C

-- ── Server state (persisted via global ModData) ────────────
-- md.numbers[number]   = { owner=username, name=pagerName, lastPos={x,y,z} }
-- md.queue[number]     = { msg, msg, ... }   (awaiting delivery)
-- md.holders[number]   = username            (transient, current online holder)
-- md.online[username]  = { x, y, z }         (transient, last known position)
-- md.seq               = running message id counter
local function getData()
    local md = ModData.getOrCreate(PagerMod.MODDATA)
    md.numbers   = md.numbers   or {}
    md.queue     = md.queue     or {}
    md.holders   = md.holders   or {}
    md.online    = md.online    or {}
    md.blockedBy = md.blockedBy or {}  -- username -> { fromNumber=true }
    md.factionOf = md.factionOf or {}  -- username -> factionName
    md.lastSend  = md.lastSend  or {}  -- username -> timestamp (transient)
    md.towers    = md.towers    or {}  -- towerNumber -> { number, index, owner, x, y, z, powered, graceUntil }
    md.seq       = md.seq       or 0
    return md
end

-- ── Pager towers (Ultra tier: network needs a built, powered tower) ─────
-- Is a tile powered (grid electricity or a running generator)? Only meaningful
-- when the chunk is loaded; returns nil when it can't be determined so callers
-- keep the last known state.
-- Is there an activated, fuelled generator within TOWER_GEN_RANGE of (x,y,z)?
-- Needed because vanilla square:haveElectricity() does NOT count a generator for
-- an EXTERIOR tile unless the AllowExteriorGenerator sandbox option is on — and
-- pager towers are usually placed outdoors next to a generator.
local function runningGeneratorNear(cell, x, y, z)
    -- The generator that powers the tower is placed right beside it, so a small
    -- scan radius is enough and keeps the per-send live check cheap.
    local r = 8
    for dx = -r, r do
        for dy = -r, r do
            local sq = cell:getGridSquare(x + dx, y + dy, z)
            local objs = sq and sq:getObjects()
            if objs then
                for i = 0, objs:size() - 1 do
                    local o = objs:get(i)
                    if o and instanceof(o, "IsoGenerator") then
                        local ok, on = pcall(function() return o:isActivated() and (o:getFuel() or 0) > 0 end)
                        if ok and on then return true end
                    end
                end
            end
        end
    end
    return false
end

-- Is the tower tile powered? Grid power, indoor/allowed generator electricity,
-- OR a running generator nearby (the outdoor case). Returns nil when the chunk
-- is unloaded so callers keep the last known state.
local function isTilePowered(x, y, z)
    PagerMod.refreshConfig()
    local mode = PagerMod.Config.towerPower or PagerMod.TowerPower.GRID_AND_GEN
    if mode == PagerMod.TowerPower.NONE then return true end  -- towers need no power
    local cell = getCell and getCell()
    if not cell then return nil end
    local sq = cell:getGridSquare(x, y, z)
    if not sq then return nil end  -- chunk not loaded -> unknown
    -- Grid power counts only in GRID_AND_GEN mode.
    if mode == PagerMod.TowerPower.GRID_AND_GEN then
        local ok, has = pcall(function()
            return ((sq.hasGridPower and sq:hasGridPower()) or sq:haveElectricity()) == true
        end)
        if ok and has then return true end
    end
    -- A running generator nearby counts in both GRID_AND_GEN and GEN_ONLY.
    if runningGeneratorNear(cell, x, y, z) then return true end
    return false
end

-- The deployed tower object on a square, or nil. B41 drops the tower as a world
-- inventory item (rendering its model), so we also scan getWorldObjects().
local function isTowerObjectOnSquare(sq)
    local function scan(objs)
        if not objs then return nil end
        for i = 0, objs:size() - 1 do
            local o = objs:get(i)
            if o and PagerMod.isTowerObject and PagerMod.isTowerObject(o) then return o end
        end
        return nil
    end
    return scan(sq:getObjects()) or scan(sq:getSpecialObjects())
        or (sq.getWorldObjects and scan(sq:getWorldObjects()))
end

-- Is a deployed pager tower still sitting on this tile? Returns true/false when
-- the chunk is loaded and we can look, or nil when it can't be determined
-- (chunk unloaded) so callers keep the last known registration.
local function isTowerPresent(x, y, z)
    local cell = getCell and getCell()
    if not cell then return nil end
    local sq = cell:getGridSquare(x, y, z)
    if not sq then return nil end  -- chunk not loaded -> unknown
    local ok, present = pcall(function()
        return isTowerObjectOnSquare(sq) ~= nil
    end)
    if ok then return present end
    return nil
end

-- Fully remove a tower node: its registration, its network number and any
-- pages queued to it.
local function dropTower(md, number)
    md.towers[number] = nil
    md.numbers[number] = nil
    md.queue[number] = nil
end

-- Refresh every tower: drop registrations whose object is gone (picked up,
-- destroyed by any means), and update the powered flag. Tiles whose chunk is
-- unloaded keep their last known state.
local function refreshTowerPower(md)
    local now = getTimestamp()
    local stale = nil
    for number, t in pairs(md.towers) do
        local past_grace = (not t.graceUntil) or now >= t.graceUntil
        if past_grace and isTowerPresent(t.x, t.y, t.z) == false then
            stale = stale or {}
            stale[#stale + 1] = number
        else
            local p = isTilePowered(t.x, t.y, t.z)
            if p ~= nil then t.powered = p end
        end
    end
    if stale then
        for _, number in ipairs(stale) do dropTower(md, number) end
    end
end

-- Is at least one pager tower currently powered?
local function anyPoweredTower(md)
    for _, t in pairs(md.towers) do
        if t.powered then return true end
    end
    return false
end

-- Is the electricity GRID still on (used by the "after power dies" tower mode)?
-- The map-wide grid carries pages until the world power shuts off, so we sample
-- grid power (NOT generators) at the sender's square — the sender is always on a
-- loaded chunk. Returns true only while the grid is genuinely up.
local function isGridPowerOn(player)
    local sq = player and player.getSquare and player:getSquare()
    if not sq then return false end
    local ok, on = pcall(function()
        return (sq.hasGridPower and sq:hasGridPower()) == true
    end)
    return ok and on == true
end

-- The nearest powered tower to (x,y) within the coverage radius, or nil.
local function coveringTower(md, x, y)
    local range = PagerMod.Config.signalRange or 7000
    local best, bestDist = nil, nil
    for _, t in pairs(md.towers) do
        if t.powered then
            local d = PagerMod.distance(x, y, t.x, t.y)
            if d <= range and (not bestDist or d < bestDist) then
                best, bestDist = t, d
            end
        end
    end
    return best
end

-- Is (x,y) within the coverage circle of any powered tower?
local function isWithinTowerRange(md, x, y)
    return coveringTower(md, x, y) ~= nil
end

-- Is (x,y) right next to the powered tower with this number (to operate it)?
-- Power is re-checked live here so a just-fuelled generator works immediately,
-- without waiting for the 30s background refresh.
local function nearSpecificTower(md, x, y, number)
    local t = md.towers[number]
    if not t then return false end
    if PagerMod.distance(x, y, t.x, t.y) > 2 then return false end
    local p = isTilePowered(t.x, t.y, t.z)
    if p ~= nil then t.powered = p end
    return t.powered == true
end

-- The deployed tower object on a tile, if any (matches by recorded position).
local function towerAtPos(md, x, y, z)
    for _, t in pairs(md.towers) do
        if t.x == x and t.y == y and t.z == z then return t end
    end
    return nil
end

-- Lowest free tower number ("0000001"..), reused after pickup. nil if full.
local function nextTowerNumber(md)
    for i = 1, PagerMod.TOWER_NUMBER_MAX do
        local n = PagerMod.towerNumberFor(i)
        if not md.towers[n] then return n, i end
    end
    return nil
end

-- How many towers a player currently has set up.
local function countTowersOwnedBy(md, username)
    local c = 0
    for _, t in pairs(md.towers) do
        if t.owner == username then c = c + 1 end
    end
    return c
end

-- Find an un-numbered pager in a player's inventory (recursive, server-side).
local function findFreshPager(container)
    if not container then return nil end
    local items = container:getItems()
    if not items then return nil end
    for i = 0, items:size() - 1 do
        local it = items:get(i)
        if it:getFullType() == PagerMod.ITEM and not it:getModData().pagerNumber then
            return it
        end
        if instanceof(it, "InventoryContainer") then
            local f = findFreshPager(it:getInventory())
            if f then return f end
        end
    end
    return nil
end

-- Push an item's (server-set) modData to its owning client. Decompiled fact:
-- syncItemModData only acts when GameServer.server (sends SyncItemModDataPacket,
-- which the client applies to its copy). In SP it's a no-op but unnecessary
-- (same Lua VM / same item object). Either way the SERVER's copy is what saves.
local function pushItemModData(player, item)
    if syncItemModData then pcall(function() syncItemModData(player, item) end) end
end

-- Faction name of a player, or nil. Guarded: the faction API may be absent
-- or the player may be factionless.
local function getFactionName(player)
    if not player then return nil end
    local ok, name = pcall(function()
        if not Faction or not Faction.getPlayerFaction then return nil end
        local f = Faction.getPlayerFaction(player)
        return f and f:getName() or nil
    end)
    if ok then return name end
    return nil
end

-- ── Utilities ──────────────────────────────────────────────

-- Iterate every player the server should reach. getOnlinePlayers() is correct on
-- a dedicated server / in MP, but it is EMPTY in single-player (and can be on a
-- listen host before the list populates), where the only player is the local
-- one — so fall back to getPlayer(). Without this, contact pushes (tower
-- auto-add, rename sync) and delivery lookups silently reach nobody in SP.
local function forEachPlayer(fn)
    local players = getOnlinePlayers()
    if players and players:size() > 0 then
        for i = 0, players:size() - 1 do
            local p = players:get(i)
            if p then fn(p) end
        end
        return
    end
    local p = getPlayer and getPlayer()
    if p then fn(p) end
end

local function findOnlinePlayer(username)
    local found = nil
    forEachPlayer(function(p)
        if not found and p:getUsername() == username then found = p end
    end)
    return found
end

local function toClient(player, command, args)
    args = args or {}
    if isServer() then
        -- Dedicated server / co-op host: route over the network to that client.
        if player then sendServerCommand(player, PagerMod.MODULE, command, args) end
    elseif PagerMod.handleServerCommand then
        -- Singleplayer: OnServerCommand never fires (the command bus is one-way
        -- in SP). Client and server share one Lua VM, so deliver directly.
        PagerMod.handleServerCommand(command, args)
    else
        -- Fallback if the client module somehow isn't loaded yet.
        sendServerCommand(PagerMod.MODULE, command, args)
    end
end

local function info(player, text, isError)
    toClient(player, S2C.INFO, { text = text, isError = isError == true })
end

-- Push one tower node as a saved contact to one player.
local function pushTowerContact(player, number, name, updateOnly)
    if not player or not number then return end
    toClient(player, S2C.ADD_CONTACT, {
        number = number, name = name, isTower = true, updateOnly = updateOnly == true,
    })
end

-- Auto-add every tower within signal range of the player as a saved contact.
local function pushInRangeTowerContacts(md, player)
    if not player then return end
    PagerMod.refreshConfig()
    local range = PagerMod.Config.signalRange or 7000
    local px, py = player:getX(), player:getY()
    for number, t in pairs(md.towers) do
        if PagerMod.distance(px, py, t.x, t.y) <= range then
            local entry = md.numbers[number]
            pushTowerContact(player, number, entry and entry.name or getText("IGUI_PagerMod_TowerName"))
        end
    end
end

-- Push a single tower contact to everyone online within its range (on set-up).
local function broadcastTowerContact(md, number)
    local t, entry = md.towers[number], md.numbers[number]
    if not t or not entry then return end
    PagerMod.refreshConfig()
    local range = PagerMod.Config.signalRange or 7000
    forEachPlayer(function(p)
        if PagerMod.distance(p:getX(), p:getY(), t.x, t.y) <= range then
            pushTowerContact(p, number, entry.name)
        end
    end)
end

-- Push a renamed tower's new name to everyone (update-only: never adds the
-- contact for players who don't already have it).
local function broadcastTowerRename(md, number)
    local entry = md.numbers[number]
    if not entry then return end
    forEachPlayer(function(p) pushTowerContact(p, number, entry.name, true) end)
end

-- Push a renamed (ordinary) pager's new name to everyone online (update-only:
-- only players who already saved this number as a contact change anything).
local function broadcastPagerRename(number, name)
    forEachPlayer(function(p)
        toClient(p, S2C.ADD_CONTACT, { number = number, name = name, updateOnly = true })
    end)
end

local function generateNumber(md)
    for _ = 1, 200 do
        local n = tostring(ZombRand(1000000, 9999999))
        if not md.numbers[n] then
            return n
        end
    end
    -- Fallback: linear scan upward from a random seed.
    local seed = ZombRand(1000000, 9999999)
    for i = 0, 8999999 do
        local n = tostring(1000000 + ((seed - 1000000 + i) % 9000000))
        if not md.numbers[n] then return n end
    end
    return nil
end

-- ── Position tracking ──────────────────────────────────────

local function updatePosition(player)
    if not player then return end
    local md = getData()
    local username = player:getUsername()
    local pos = {
        x = math.floor(player:getX()),
        y = math.floor(player:getY()),
        z = math.floor(player:getZ()),
    }
    md.online[username] = pos
    md.factionOf[username] = getFactionName(player)
    -- Refresh last known position of every number this user holds.
    for number, holder in pairs(md.holders) do
        if holder == username and md.numbers[number] then
            md.numbers[number].lastPos = pos
        end
    end
end

-- PvPvE: when faction scoping is on, members of two different factions
-- cannot page each other. Factionless players are never cut off.
local function factionAllows(md, senderFaction, holderUser)
    PagerMod.refreshConfig()
    if PagerMod.Config.factionScope ~= PagerMod.FactionScope.SAME_ONLY then
        return true
    end
    local holderFaction = md.factionOf[holderUser]
    if not senderFaction or not holderFaction then return true end
    return senderFaction == holderFaction
end

-- Anti-grief: a recipient can block a sender's number.
local function isBlocked(md, holderUser, fromNumber)
    PagerMod.refreshConfig()
    if not PagerMod.Config.allowBlocking then return false end
    local set = md.blockedBy[holderUser]
    return set ~= nil and set[fromNumber] == true
end

-- Anti-spam send cooldown (PvP server health). Returns ok, secondsLeft.
local function cooldownOk(md, username)
    PagerMod.refreshConfig()
    local cd = PagerMod.Config.sendCooldown or 0
    if cd <= 0 then return true, 0 end
    local now = getTimestamp()
    local last = md.lastSend[username] or 0
    local elapsed = now - last
    if elapsed < cd then
        return false, math.ceil(cd - elapsed)
    end
    md.lastSend[username] = now
    return true, 0
end

-- ── Delivery ───────────────────────────────────────────────

-- Optional page log: appends every routed page to a text file in the server's
-- Zomboid directory when the LogPages sandbox option is on.
local function logPage(msg, number, result)
    if not PagerMod.Config.logPages then return end
    pcall(function()
        local w = getFileWriter("PagerMod_PageLog.txt", true, true)  -- create path, append
        if not w then return end
        local s = msg.stamp or {}
        local when = string.format("%04d-%02d-%02d %02d:%02d",
            s.y or 0, s.mo or 0, s.d or 0, s.h or 0, s.mi or 0)
        local fromName = (msg.fromName and msg.fromName ~= "" and msg.fromName ~= msg.from)
            and (" \"" .. msg.fromName .. "\"") or ""
        local byUser = msg.fromUser and (" by " .. msg.fromUser) or ""
        w:writeln(string.format("[%s] #%s%s%s -> #%s : %s  (%s)",
            when, tostring(msg.from), fromName, byUser, tostring(number),
            tostring(msg.text), tostring(result)))
        w:close()
    end)
end

-- Push a single message to whoever holds `number` if they are online,
-- otherwise enqueue it for later catch-up. `ctx` carries the sender's
-- faction for PvPvE scoping. Returns "delivered", "queued" or "blocked".
local function deliverOrQueue(md, number, msg, ctx)
    ctx = ctx or {}
    local holder = md.holders[number]
    local result

    -- Block list & faction scoping are evaluated against the (known) holder,
    -- even if they are currently offline. Global chat bypasses faction scoping
    -- (it's a town-wide channel) but blocking is still respected.
    if holder and (isBlocked(md, holder, msg.from)
            or (not ctx.bypassFaction and not factionAllows(md, ctx.senderFaction, holder))) then
        result = "blocked"
    else
        local player = holder and findOnlinePlayer(holder) or nil
        if player then
            toClient(player, S2C.DELIVER, { number = number, messages = { msg } })
            result = "delivered"
        else
            -- Offline: queue, respecting the per-pager cap.
            md.queue[number] = md.queue[number] or {}
            local q = md.queue[number]
            table.insert(q, msg)
            local cap = PagerMod.Config.maxMessages or 50
            while #q > cap do table.remove(q, 1) end
            result = "queued"
        end
    end
    logPage(msg, number, result)
    return result
end

-- ── Command handlers ───────────────────────────────────────

local function onAssign(player, args)
    local md = getData()
    local number = generateNumber(md)
    if not number then
        info(player, getText("IGUI_PagerMod_NoNumbersLeft"), true)
        return
    end
    md.numbers[number] = {
        owner = player:getUsername(),
        name  = args.name or getText("IGUI_PagerMod_DefaultName"),
        lastPos = { x = math.floor(player:getX()), y = math.floor(player:getY()), z = math.floor(player:getZ()) },
    }
    md.holders[number] = player:getUsername()

    -- Write the number onto the player's pager on the SERVER (authoritative copy
    -- that persists across logout). Prefer the exact pager clicked (by network
    -- id), else the first un-numbered one carried.
    local item, byId = nil, false
    if args.itemId then
        local ok, found = pcall(function() return player:getInventory():getItemById(args.itemId) end)
        if ok and found and found:getFullType() == PagerMod.ITEM then item = found; byId = true end
    end
    if not item then item = findFreshPager(player:getInventory()) end
    local readback = "no-item"
    if item then
        local imd = item:getModData()
        imd.pagerNumber = number
        imd.pagerName   = imd.pagerName or args.name or getText("IGUI_PagerMod_DefaultName")
        imd.messages    = imd.messages or {}
        imd.unread      = imd.unread or 0
        if imd.batteryAge == nil then
            local gt = getGameTime()
            imd.batteryAge = (gt and gt:getWorldAgeHours()) or 0
        end
        pushItemModData(player, item)
        readback = tostring(item:getModData().pagerNumber)
    end

    toClient(player, S2C.ASSIGNED, { number = number, reqId = args.reqId })
    -- Auto-add any tower the player is currently within range of.
    pushInRangeTowerContacts(md, player)
    -- DIAG (dedicated-save investigation): confirms the server wrote the number
    -- onto a real inventory item. readback should equal `number`; syncFn=false
    -- would mean the persist/sync API is missing on this build.
    print(string.format("[PagerMod][DIAG] onAssign user=%s number=%s itemId=%s foundById=%s item=%s readback=%s syncFn=%s isServer=%s",
        player:getUsername(), number, tostring(args.itemId), tostring(byId),
        tostring(item ~= nil), readback, tostring(syncItemModData ~= nil),
        tostring(isServer and isServer())))
end

local function onRegister(player, args)
    local md = getData()
    md.heldByUser = md.heldByUser or {}
    local username = player:getUsername()
    updatePosition(player)

    -- DIAG (dedicated-save investigation): THE key line. On a relog,
    -- reported=[] but serverOwns=[N] means the pager's item modData did NOT
    -- persist (number lost client-side though the server still owns it).
    -- reported=[] AND serverOwns=[] means even the server's global data was lost.
    do
        local reported = {}
        for _, n in ipairs(args.numbers or {}) do reported[#reported + 1] = tostring(n) end
        local owned = {}
        for n, rec in pairs(md.numbers) do
            if rec.owner == username and not rec.isTower then owned[#owned + 1] = n end
        end
        print(string.format("[PagerMod][DIAG] onRegister user=%s reported=[%s] serverOwns=[%s]",
            username, table.concat(reported, ","), table.concat(owned, ",")))
    end

    -- Store this player's block list (number set) for delivery filtering.
    if args.blocked then
        local set = {}
        for _, n in ipairs(args.blocked) do set[tostring(n)] = true end
        md.blockedBy[username] = set
    end

    -- Release holder bindings this user previously claimed; we re-add the
    -- ones they still carry below. Numbers they dropped become "offline"
    -- so future pages queue instead of vanishing into thin air.
    local prev = md.heldByUser[username]
    if prev then
        for _, n in ipairs(prev) do
            if md.holders[n] == username then md.holders[n] = nil end
        end
    end

    local numbers = args.numbers or {}
    local current = {}
    for _, number in ipairs(numbers) do
        number = tostring(number)
        if not md.numbers[number] then
            -- A pager that exists but the server never saw (e.g. wiped save).
            md.numbers[number] = {
                owner = username,
                name = getText("IGUI_PagerMod_DefaultName"),
                lastPos = md.online[username],
            }
        end
        md.holders[number] = username
        md.numbers[number].lastPos = md.online[username]
        table.insert(current, number)

        -- Flush any messages queued while this pager was offline.
        local q = md.queue[number]
        if q and #q > 0 then
            toClient(player, S2C.DELIVER, { number = number, messages = q })
            md.queue[number] = nil
        end
    end
    md.heldByUser[username] = current

    -- Auto-add any tower the returning player is within range of.
    pushInRangeTowerContacts(md, player)
end

-- A page was delivered to a player who no longer holds the pager (dropped /
-- traded in a race). Detach the holder and re-queue for the next holder.
local function onRequeue(player, args)
    local md = getData()
    local number = tostring(args.number or "")
    if number == "" or not md.numbers[number] then return end
    if md.holders[number] == player:getUsername() then
        md.holders[number] = nil
    end
    md.queue[number] = md.queue[number] or {}
    for _, msg in ipairs(args.messages or {}) do
        table.insert(md.queue[number], msg)
    end
    local cap = PagerMod.Config.maxMessages or 50
    while #md.queue[number] > cap do
        table.remove(md.queue[number], 1)
    end
end

-- Whether coordinates may be attached to this page.
local function locationAllowed(wantsLocation, forceForSOS)
    PagerMod.refreshConfig()
    local mode = PagerMod.Config.locationSharing
    if mode == PagerMod.LocationMode.OFF then return false end
    if mode == PagerMod.LocationMode.ALWAYS then return true end
    -- OPT_IN: only if the sender asked, or it's an SOS.
    return wantsLocation == true or forceForSOS == true
end

local function buildMessage(md, args, toNumber, text, player, includeLoc, isSOS)
    md.seq = md.seq + 1
    local msg = {
        id = md.seq,
        from = args.from or "SYSTEM",
        fromName = args.fromName or "SYSTEM",
        fromUser = player and player:getUsername() or nil,
        to = toNumber,
        text = text,
        stamp = PagerMod.gameStamp(),
        sos = isSOS == true,
        global = args.global == true,
    }
    if includeLoc and player then
        msg.x = math.floor(player:getX())
        msg.y = math.floor(player:getY())
    end
    return msg
end

-- Decide whether a message can physically reach `toNumber` from the sender.
-- `fromTower` is true when the sender IS the tower node (operating its console),
-- which is always considered in-coverage on the sending side.
local function hasSignal(md, player, toNumber, fromTower)
    PagerMod.refreshConfig()
    local mode = PagerMod.Config.signalMode
    if PagerMod.isTowerMode(mode) then
        -- "After power dies": while the electricity grid is still up the network
        -- works map-wide; only once the power goes out does it need a tower.
        -- "Regardless of power" (TOWER): a powered tower is always required.
        if mode == PagerMod.SignalMode.TOWER_AFTER and isGridPowerOn(player) then
            return true
        end
        -- Network only works while a built pager tower is powered.
        if not anyPoweredTower(md) then return false end
        -- Sending side: a player must stand inside a tower's coverage circle;
        -- a tower node itself always counts as in-coverage.
        if not fromTower and not isWithinTowerRange(md, player:getX(), player:getY()) then
            return false
        end
        -- Receiving side: a tower node is reachable while it is powered; any
        -- other recipient must have last been seen inside a coverage circle.
        local target = md.numbers[toNumber]
        if not target then return false end
        if target.isTower then
            local t = md.towers[toNumber]
            return t ~= nil and t.powered == true
        end
        if not target.lastPos then return false end
        return isWithinTowerRange(md, target.lastPos.x, target.lastPos.y)
    elseif mode == PagerMod.SignalMode.RANGE then
        local target = md.numbers[toNumber]
        if not target or not target.lastPos then
            return false -- never seen on the network -> no signal
        end
        local dist = PagerMod.distance(player:getX(), player:getY(), target.lastPos.x, target.lastPos.y)
        return dist <= (PagerMod.Config.signalRange or 7000)
    end
    return true -- GLOBAL
end

-- Is this command coming from the tower console (sender = the 000-0001 node)?
-- Validates the player is actually next to a powered tower. Returns true if it
-- is a tower-origin command that passed validation; false + SENT_FAIL if it
-- claimed to be but isn't; nil if it is an ordinary pager send.
local function validateTowerOrigin(md, player, args)
    local from = tostring(args.from or "")
    if not (args.viaTower and PagerMod.isTowerNumber(from)) then
        return nil
    end
    -- Must be standing next to THAT specific powered tower.
    if not nearSpecificTower(md, player:getX(), player:getY(), from) then
        toClient(player, S2C.SENT_FAIL, { reason = getText("IGUI_PagerMod_TowerTooFar") })
        return false
    end
    return true
end

local function onSend(player, args)
    local md = getData()
    PagerMod.refreshConfig()
    updatePosition(player)

    local towerOrigin = validateTowerOrigin(md, player, args)
    if towerOrigin == false then return end
    -- The tower node can always send (it is not a pager); ordinary pagers obey
    -- receive-only mode.
    if not towerOrigin and not PagerMod.canSend() then
        toClient(player, S2C.SENT_FAIL, { reason = getText("IGUI_PagerMod_ReceiveOnly") })
        return
    end
    local ok, left = cooldownOk(md, player:getUsername())
    if not ok then
        toClient(player, S2C.SENT_FAIL, { reason = string.format(getText("IGUI_PagerMod_Cooldown"), left) })
        return
    end

    local toNumber = tostring(args.to or "")
    local text = PagerMod.trimMessage(args.text)
    if text == "" then
        toClient(player, S2C.SENT_FAIL, { reason = getText("IGUI_PagerMod_EmptyMessage") })
        return
    end
    if not md.numbers[toNumber] then
        toClient(player, S2C.SENT_FAIL, { reason = getText("IGUI_PagerMod_UnknownNumber"), to = toNumber })
        return
    end
    -- Paging a tower node only works if towers accept incoming pages.
    if md.numbers[toNumber] and md.numbers[toNumber].isTower and not PagerMod.towerCanReceive() then
        toClient(player, S2C.SENT_FAIL, { reason = getText("IGUI_PagerMod_TowerSendOnly"), to = toNumber })
        return
    end
    if not hasSignal(md, player, toNumber, towerOrigin == true) then
        toClient(player, S2C.SENT_FAIL, { reason = getText("IGUI_PagerMod_NoSignal"), to = toNumber })
        return
    end

    local includeLoc = locationAllowed(args.location, false)
    local ctx = { senderFaction = getFactionName(player) }
    local msg = buildMessage(md, args, toNumber, text, player, includeLoc, false)
    local result = deliverOrQueue(md, toNumber, msg, ctx)
    if result == "blocked" then
        -- Don't reveal blocks/faction filtering; report as delivered.
        toClient(player, S2C.SENT_OK, { to = toNumber, delivered = true })
    else
        toClient(player, S2C.SENT_OK, { to = toNumber, delivered = (result == "delivered") })
    end
end

-- Shared fan-out used by Broadcast / Channel / SOS.
local function fanOut(player, args, numbers, text, includeLoc, isSOS, fromTower)
    local md = getData()
    local ctx = { senderFaction = getFactionName(player) }
    local from = args.from
    local towerReceives = PagerMod.towerCanReceive()
    local count = 0
    for _, number in ipairs(numbers) do
        number = tostring(number)
        -- Don't fan a page back to its sender, and only include tower nodes
        -- when they actually accept incoming pages.
        local entry = md.numbers[number]
        local skip = (number == from) or (entry and entry.isTower and not towerReceives)
        if not skip and entry and hasSignal(md, player, number, fromTower) then
            local msg = buildMessage(md, args, number, text, player, includeLoc, isSOS)
            local r = deliverOrQueue(md, number, msg, ctx)
            if r ~= "blocked" then count = count + 1 end
        end
    end
    return count
end

local function onBroadcast(player, args)
    local md = getData()
    PagerMod.refreshConfig()
    local towerOrigin = validateTowerOrigin(md, player, args)
    if towerOrigin == false then return end
    if not towerOrigin and not PagerMod.canSend() then
        toClient(player, S2C.SENT_FAIL, { reason = getText("IGUI_PagerMod_ReceiveOnly") })
        return
    end
    if not PagerMod.Config.allowBroadcast then
        toClient(player, S2C.SENT_FAIL, { reason = getText("IGUI_PagerMod_BroadcastDisabled") })
        return
    end
    updatePosition(player)
    local ok, left = cooldownOk(md, player:getUsername())
    if not ok then
        toClient(player, S2C.SENT_FAIL, { reason = string.format(getText("IGUI_PagerMod_Cooldown"), left) })
        return
    end
    local text = PagerMod.trimMessage(args.text)
    if text == "" then
        toClient(player, S2C.SENT_FAIL, { reason = getText("IGUI_PagerMod_EmptyMessage") })
        return
    end

    local includeLoc = locationAllowed(args.location, false)
    local all = {}
    for number, _ in pairs(md.numbers) do table.insert(all, number) end
    local count = fanOut(player, args, all, text, includeLoc, false, towerOrigin == true)
    toClient(player, S2C.SENT_OK, { broadcast = true, count = count })
end

local function onChannel(player, args)
    local md = getData()
    PagerMod.refreshConfig()
    updatePosition(player)
    local towerOrigin = validateTowerOrigin(md, player, args)
    if towerOrigin == false then return end
    if not towerOrigin and not PagerMod.canSend() then
        toClient(player, S2C.SENT_FAIL, { reason = getText("IGUI_PagerMod_ReceiveOnly") })
        return
    end
    local ok, left = cooldownOk(md, player:getUsername())
    if not ok then
        toClient(player, S2C.SENT_FAIL, { reason = string.format(getText("IGUI_PagerMod_Cooldown"), left) })
        return
    end
    local text = PagerMod.trimMessage(args.text)
    if text == "" then
        toClient(player, S2C.SENT_FAIL, { reason = getText("IGUI_PagerMod_EmptyMessage") })
        return
    end
    local members = args.members or {}
    if #members == 0 then
        toClient(player, S2C.SENT_FAIL, { reason = getText("IGUI_PagerMod_EmptyChannel") })
        return
    end
    local includeLoc = locationAllowed(args.location, false)
    local count = fanOut(player, args, members, text, includeLoc, false, towerOrigin == true)
    toClient(player, S2C.SENT_OK, { channel = args.channelName, count = count })
end

-- Town-wide global channel: every pager is implicitly a member. Reaches all
-- real pagers regardless of saved contacts or faction, but still obeys the
-- signal mode, receive-only mode, send cooldown and per-recipient blocking.
local function onGlobalSend(player, args)
    local md = getData()
    PagerMod.refreshConfig()
    if not PagerMod.Config.globalChat then
        toClient(player, S2C.SENT_FAIL, { reason = getText("IGUI_PagerMod_GlobalOff") })
        return
    end
    if not PagerMod.canSend() then
        toClient(player, S2C.SENT_FAIL, { reason = getText("IGUI_PagerMod_ReceiveOnly") })
        return
    end
    updatePosition(player)
    local ok, left = cooldownOk(md, player:getUsername())
    if not ok then
        toClient(player, S2C.SENT_FAIL, { reason = string.format(getText("IGUI_PagerMod_Cooldown"), left) })
        return
    end
    local text = PagerMod.trimMessage(args.text)
    if text == "" then
        toClient(player, S2C.SENT_FAIL, { reason = getText("IGUI_PagerMod_EmptyMessage") })
        return
    end
    args.global = true
    local includeLoc = locationAllowed(args.location, false)
    local ctx = { senderFaction = getFactionName(player), bypassFaction = true }
    -- Build ONE message (one id) and deliver that same object to everyone, so a
    -- player holding several pagers dedupes it to a single feed entry. The
    -- sender IS included so they see their own post (client suppresses the beep).
    local msg = buildMessage(md, args, "GLOBAL", text, player, includeLoc, false)
    local count = 0
    for number, entry in pairs(md.numbers) do
        if not entry.isTower and hasSignal(md, player, number, false) then
            local r = deliverOrQueue(md, number, msg, ctx)
            if r ~= "blocked" then count = count + 1 end
        end
    end
    toClient(player, S2C.SENT_OK, { global = true, count = count })
end

local function onSOS(player, args)
    local md = getData()
    PagerMod.refreshConfig()
    local towerOrigin = validateTowerOrigin(md, player, args)
    if towerOrigin == false then return end
    if not towerOrigin and not PagerMod.canSend() then
        toClient(player, S2C.SENT_FAIL, { reason = getText("IGUI_PagerMod_ReceiveOnly") })
        return
    end
    if not PagerMod.Config.allowSOS then
        toClient(player, S2C.SENT_FAIL, { reason = getText("IGUI_PagerMod_SOSDisabled") })
        return
    end
    updatePosition(player)
    local ok, left = cooldownOk(md, player:getUsername())
    if not ok then
        toClient(player, S2C.SENT_FAIL, { reason = string.format(getText("IGUI_PagerMod_Cooldown"), left) })
        return
    end
    local text = PagerMod.trimMessage(args.text)
    if text == "" then text = getText("IGUI_PagerMod_SOSDefault") end
    -- SOS forces location (unless sharing is fully OFF) and pages everyone.
    local includeLoc = locationAllowed(true, true)
    local all = {}
    for number, _ in pairs(md.numbers) do table.insert(all, number) end
    local count = fanOut(player, args, all, text, includeLoc, true, towerOrigin == true)
    toClient(player, S2C.SENT_OK, { sos = true, count = count })
end

-- "Share my number nearby": push the sender's number+name to every pager
-- whose holder is online and standing within SHARE_RANGE tiles.
local function onShareNearby(player, args)
    local md = getData()
    updatePosition(player)
    local fromNumber = args.from
    local fromName = args.fromName or "?"
    if not fromNumber then return end

    local px, py = player:getX(), player:getY()
    local range = PagerMod.SHARE_RANGE or 40
    local count = 0
    local seen = {}
    for number, info in pairs(md.numbers) do
        local holder = md.holders[number]
        if number ~= fromNumber and holder and not seen[holder] then
            local pos = info.lastPos
            if pos and PagerMod.distance(px, py, pos.x, pos.y) <= range then
                local target = findOnlinePlayer(holder)
                if target then
                    seen[holder] = true
                    toClient(target, S2C.ADD_CONTACT, { number = fromNumber, name = fromName })
                    count = count + 1
                end
            end
        end
    end
    toClient(player, S2C.INFO, {
        text = string.format(getText("IGUI_PagerMod_SharedNearby"), count),
    })
end

-- Read receipts: notify the original senders that their page was read.
local function onMarkRead(player, args)
    PagerMod.refreshConfig()
    if not PagerMod.Config.readReceipts then return end
    local readerLabel = args.readerName or "?"
    local notified = {}
    for _, ack in ipairs(args.acks or {}) do
        local toUser = ack.toUser
        if toUser and not notified[toUser] then
            notified[toUser] = true
            local target = findOnlinePlayer(toUser)
            if target then
                info(target, string.format(getText("IGUI_PagerMod_ReadReceipt"), readerLabel), false)
            end
        end
    end
end

local function onFetch(player, args)
    local md = getData()
    local username = player:getUsername()
    updatePosition(player)

    local result = {}
    for _, number in ipairs(args.numbers or {}) do
        number = tostring(number)
        -- Tower nodes have no single holder — anyone at a console reads the
        -- inbox — so don't bind them to whoever fetched last.
        if not PagerMod.isTowerNumber(number) then
            md.holders[number] = username
        end
        local q = md.queue[number]
        if q and #q > 0 then
            result[number] = q
            md.queue[number] = nil
        end
    end
    toClient(player, S2C.INBOX, { inbox = result })
end

local function onPing(player, args)
    PagerMod.refreshConfig()
    local md = getData()
    local known = 0
    for _ in pairs(md.numbers) do known = known + 1 end
    local onlineCount = 0
    local players = getOnlinePlayers()
    if players then onlineCount = players:size() end
    local mine = countTowersOwnedBy(md, player:getUsername())
    local cover = coveringTower(md, player:getX(), player:getY())
    local towerCount = 0
    for _ in pairs(md.towers) do towerCount = towerCount + 1 end
    toClient(player, S2C.STATUS, {
        signalMode = PagerMod.Config.signalMode,
        signalRange = PagerMod.Config.signalRange,
        pagerMode = PagerMod.Config.pagerMode,
        allowBroadcast = PagerMod.Config.allowBroadcast,
        locationSharing = PagerMod.Config.locationSharing,
        allowSOS = PagerMod.Config.allowSOS,
        allowBlocking = PagerMod.Config.allowBlocking,
        factionScope = PagerMod.Config.factionScope,
        sendCooldown = PagerMod.Config.sendCooldown,
        knownPagers = known,
        onlinePlayers = onlineCount,
        towerOnline = anyPoweredTower(md),
        towerCount = towerCount,
        myTowers = mine,
        towerComms = PagerMod.Config.towerComms,
        towerLimit = PagerMod.Config.towerLimit,
        -- Grid still up? In "after power dies" mode the network is map-wide while
        -- this is true, and only needs a tower once it goes false.
        gridPower = isGridPowerOn(player),
        -- Which powered tower currently covers the player (nil if none).
        inTowerRange = cover ~= nil,
        coveringTowerNum = cover and cover.index or nil,
    })
end

-- Admin players may pick up / ignore limits on any tower.
local function isAdmin(player)
    local ok, lvl = pcall(function() return player:getAccessLevel() end)
    return ok and lvl ~= nil and lvl ~= "" and lvl ~= "None"
end

-- Remove the deployed tower object on a tile (server-authoritative). Returns
-- true if an object was found and removed. Safe no-op if the chunk is unloaded.
local function removeTowerObjectAt(x, y, z)
    local cell = getCell and getCell()
    if not cell then return false end
    local sq = cell:getGridSquare(x, y, z)
    if not sq then return false end
    local removed = false
    pcall(function()
        -- Remove every tower object on the tile (there should be exactly one).
        for _ = 1, 4 do
            local o = isTowerObjectOnSquare(sq)
            if not o then break end
            sq:transmitRemoveItemFromSquare(o)
            removed = true
        end
        if removed then sq:RecalcAllWithNeighbours(true) end
    end)
    return removed
end

-- ── Pager tower deploy / pickup / operate ──────────────────
-- Each tower is its own node with its own number, coverage circle and inbox.
local function onDeployTower(player, args)
    local md = getData()
    local username = player:getUsername()
    local x = math.floor(args.x or player:getX())
    local y = math.floor(args.y or player:getY())
    local z = math.floor(args.z or player:getZ())

    -- The client already placed the world object optimistically; if we reject
    -- the deploy here, tell it to remove that object and hand the item back.
    local function refund(reasonKey)
        info(player, getText(reasonKey), true)
        toClient(player, S2C.TOWER_REFUND, { x = x, y = y, z = z })
    end

    -- Enforce the per-player tower limit (admins are exempt).
    local limit = PagerMod.Config.towerLimit or 0
    if limit > 0 and not isAdmin(player) and countTowersOwnedBy(md, username) >= limit then
        info(player, string.format(getText("IGUI_PagerMod_TowerLimitHit"), limit), true)
        toClient(player, S2C.TOWER_REFUND, { x = x, y = y, z = z })
        return
    end

    local number, index = nextTowerNumber(md)
    if not number then
        refund("IGUI_PagerMod_TowerNoNumbers")
        return
    end
    local powered = isTilePowered(x, y, z)
    -- graceUntil: in MP the freshly placed object may not be visible to the
    -- server for a moment, so don't run the "is it still there?" check yet.
    md.towers[number] = {
        number = number, index = index, owner = username,
        x = x, y = y, z = z, powered = (powered == true),
        graceUntil = getTimestamp() + 15,
    }
    local name = string.format("%s %d", getText("IGUI_PagerMod_TowerName"), index)
    md.numbers[number] = { owner = "__TOWER__", name = name, lastPos = { x = x, y = y, z = z }, isTower = true }

    -- Announce the new node to everyone as a saved contact.
    broadcastTowerContact(md, number)

    if powered then
        info(player, string.format(getText("IGUI_PagerMod_TowerUpPoweredN"), PagerMod.formatNumber(number)), false)
    else
        info(player, string.format(getText("IGUI_PagerMod_TowerUpUnpoweredN"), PagerMod.formatNumber(number)), true)
    end
end

-- Pickup is fully server-authoritative: the client only asks. We validate
-- ownership BEFORE removing the world object or handing back the item, so the
-- owner/admin check can't be bypassed by a client removing it locally.
local function onPickupTower(player, args)
    local md = getData()
    local x = math.floor(args.x or 0)
    local y = math.floor(args.y or 0)
    local z = math.floor(args.z or 0)
    local t = towerAtPos(md, x, y, z)
    if not t then
        info(player, getText("IGUI_PagerMod_TowerGone"), true)
        return
    end
    -- Only the owner (or an admin) may pack a tower up.
    if t.owner ~= player:getUsername() and not isAdmin(player) then
        info(player, getText("IGUI_PagerMod_TowerNotYours"), true)
        return
    end
    dropTower(md, t.number)
    removeTowerObjectAt(x, y, z)              -- server-side removal (loaded chunks)
    player:getInventory():AddItem(PagerMod.TOWER)
    -- Also tell the requesting client to remove the object locally — server-side
    -- removal doesn't always reflect on the host's own view, which made pickup
    -- look like it did nothing.
    toClient(player, S2C.PICKUP_OK, { x = x, y = y, z = z })
    info(player, getText("IGUI_PagerMod_TowerRemoved"), false)
end

-- Operate the tower at a tile: resolve its number and hand its inbox to the
-- player's console.
local function onOpenTower(player, args)
    local md = getData()
    local x = math.floor(args.x or 0)
    local y = math.floor(args.y or 0)
    local z = math.floor(args.z or 0)
    local t = towerAtPos(md, x, y, z)
    if not t then
        info(player, getText("IGUI_PagerMod_TowerGone"), true)
        return
    end
    -- Can't operate an unpowered tower (when power is required). Re-check live so
    -- a just-fuelled generator works immediately.
    local powered = isTilePowered(x, y, z)
    if powered ~= nil then t.powered = powered end
    if t.powered ~= true then
        info(player, getText("IGUI_PagerMod_TowerNeedsPower"), true)
        return
    end
    -- Drain any pages queued to this tower so the operator sees them.
    local inbox = md.queue[t.number]
    md.queue[t.number] = nil
    local entry = md.numbers[t.number]
    toClient(player, S2C.TOWER_OPEN, {
        number  = t.number,
        name    = entry and entry.name or getText("IGUI_PagerMod_TowerName"),
        powered = t.powered == true,
        inbox   = inbox or {},
    })
end

-- Rename a tower (owner/admin). The new name syncs to everyone's saved contact,
-- and the tower's console title / outgoing fromName use it from then on.
local function onRenameTower(player, args)
    local md = getData()
    local t = towerAtPos(md, math.floor(args.x or 0), math.floor(args.y or 0), math.floor(args.z or 0))
    if not t then return end
    if t.owner ~= player:getUsername() and not isAdmin(player) then
        info(player, getText("IGUI_PagerMod_TowerNotYours"), true)
        return
    end
    local name = tostring(args.name or ""):gsub("^%s+", ""):gsub("%s+$", ""):sub(1, 24)
    if name == "" then return end
    local entry = md.numbers[t.number]
    if entry then entry.name = name end
    broadcastTowerRename(md, t.number)
    info(player, string.format(getText("IGUI_PagerMod_TowerRenamed"), name), false)
end

-- Rename my own pager. Persists the name onto the item server-side (so it sticks
-- in B42, where client modData writes are a no-op) and pushes the new name to
-- everyone who has this number saved, keeping their contact entry in sync.
local function onRenamePager(player, args)
    local md = getData()
    local number = tostring(args.number or "")
    local name = tostring(args.name or ""):gsub("^%s+", ""):gsub("%s+$", ""):sub(1, 24)
    if name == "" then return end

    -- Persist onto the exact pager the player renamed (by network id).
    if args.itemId then
        local ok, item = pcall(function() return player:getInventory():getItemById(args.itemId) end)
        if ok and item and item:getFullType() == PagerMod.ITEM then
            item:getModData().pagerName = name
            pushItemModData(player, item)
        end
    end

    -- Update the registry + sync the name to everyone who has it saved. Only the
    -- owner may rename, so a stolen/duplicated number can't be relabelled by others.
    local entry = md.numbers[number]
    if entry and entry.owner == player:getUsername() then
        entry.name = name
        broadcastPagerRename(number, name)
    end
end

-- Find a held pager by its number anywhere in the player's inventory (fallback
-- when the network itemId round-trip misses).
local function findPagerByNumber(container, number)
    if not container or not number then return nil end
    local items = container:getItems()
    if not items then return nil end
    for i = 0, items:size() - 1 do
        local it = items:get(i)
        if it:getFullType() == PagerMod.ITEM and tostring(it:getModData().pagerNumber) == tostring(number) then
            return it
        end
        if instanceof(it, "InventoryContainer") then
            local f = findPagerByNumber(it:getInventory(), number)
            if f then return f end
        end
    end
    return nil
end

-- Fit a fresh battery: stamp batteryAge on the SERVER's authoritative item copy
-- and sync it back, so the recharge survives logout / drop / moving the pager
-- between containers. Client modData writes alone don't persist in B42.
local function onReplaceBattery(player, args)
    local item = nil
    if args.itemId then
        local ok, found = pcall(function() return player:getInventory():getItemById(args.itemId) end)
        if ok and found and found:getFullType() == PagerMod.ITEM then item = found end
    end
    if not item then item = findPagerByNumber(player:getInventory(), args.number) end
    if not item then return end
    local gt = getGameTime()
    item:getModData().batteryAge = (gt and gt:getWorldAgeHours()) or 0
    pushItemModData(player, item)
end

-- ── Event wiring ───────────────────────────────────────────

local handlers = {
    [C2S.ASSIGN]    = onAssign,
    [C2S.REGISTER]  = onRegister,
    [C2S.SEND]      = onSend,
    [C2S.BROADCAST] = onBroadcast,
    [C2S.CHANNEL]   = onChannel,
    [C2S.SOS]       = onSOS,
    [C2S.FETCH]        = onFetch,
    [C2S.REQUEUE]      = onRequeue,
    [C2S.MARK_READ]    = onMarkRead,
    [C2S.SHARE_NEARBY] = onShareNearby,
    [C2S.GLOBAL]       = onGlobalSend,
    [C2S.DEPLOY_TOWER] = onDeployTower,
    [C2S.PICKUP_TOWER] = onPickupTower,
    [C2S.OPEN_TOWER]   = onOpenTower,
    [C2S.RENAME_TOWER] = onRenameTower,
    [C2S.RENAME]       = onRenamePager,
    [C2S.REPLACE_BATT] = onReplaceBattery,
    [C2S.PING]         = onPing,
}

local function onClientCommand(module, command, player, args)
    if module ~= PagerMod.MODULE then return end
    local h = handlers[command]
    if h then
        h(player, args or {})
    end
end

-- Periodic position refresh so RANGE mode stays current. OnTick's argument
-- is a frame counter, so we throttle on real wall-clock seconds instead.
local lastTick = 0
local TICK_INTERVAL = 30 -- seconds
local function onTick()
    local now = getTimestamp()
    if now - lastTick < TICK_INTERVAL then return end
    lastTick = now
    local md = getData()
    forEachPlayer(function(p)
        updatePosition(p)
        -- Auto-add towers the player has moved into range of.
        pushInRangeTowerContacts(md, p)
    end)
    -- Keep tower power state current (only re-checks loaded chunks).
    refreshTowerPower(md)
end

local function onServerStart()
    PagerMod.refreshConfig()
    local md = getData()
    -- DIAG (dedicated-save investigation): does the global ModData survive a
    -- server RESTART? After a restart with active pagers/towers, all-zero here
    -- means the global store (queued messages, number ownership, tower
    -- registrations) was NOT persisted -> that's why messages/towers vanish.
    local nNum, nQ, nT = 0, 0, 0
    for _ in pairs(md.numbers) do nNum = nNum + 1 end
    for _, q in pairs(md.queue) do nQ = nQ + (#q or 0) end
    for _ in pairs(md.towers) do nT = nT + 1 end
    print(string.format("[PagerMod][DIAG] onServerStart loaded md: numbers=%d queuedMsgs=%d towers=%d seq=%d",
        nNum, nQ, nT, md.seq or 0))
    print("[PagerMod] Server routing online. Signal mode: " .. tostring(PagerMod.Config.signalMode))
end

Events.OnClientCommand.Add(onClientCommand)
Events.OnTick.Add(onTick)
Events.OnServerStarted.Add(onServerStart)
Events.OnInitGlobalModData.Add(function() getData() end)

print("[PagerMod] Server loaded")
