-- Player panel server core: own dispatcher on the AegisPlayer module.
-- Identity always comes from the player argument, every request is
-- throttled per user and gated on the panel unlock from the roles file.
if isClient() then return end

require "Aegis_Store"
require "Aegis_Roles"
require "Aegis_Log"
require "Aegis_Moderation"
require "Aegis_PlayerStats"
require "Aegis_Boost"

local MODULE = "AegisPlayer"

-- reply to the caller: over the network in MP, in solo directly to the
-- OnServerCommand listeners of the same process
local function toClient(player, command, args)
    if isServer() then
        sendServerCommand(player, MODULE, command, args)
    else
        triggerEvent("OnServerCommand", MODULE, command, args)
    end
end

local function cap(v, max)
    -- control characters would let a client smuggle forged lines into
    -- the logs admins read as evidence
    local s = tostring(v):gsub("%c", " ")
    if #s > max then s = s:sub(1, max) end
    return s
end

local function username(player)
    local name = player and player:getUsername()
    if type(name) ~= "string" or name == "" then return nil end
    return name
end

local lastRequest = {}
local function throttled(name, kind, window)
    local key = name .. "|" .. kind
    local now = AegisShared.realTime()
    if lastRequest[key] and now - lastRequest[key] < window then return true end
    lastRequest[key] = now
    return false
end

-- nil unless the roles file unlocks the panel for this user
local function panelFor(name)
    if not AegisRoles.playerPanelFor then return nil end
    local pp = AegisRoles.playerPanelFor(name)
    if type(pp) == "table" then return pp end
    return nil
end

-- solo has no server roles; the sandbox switch gates both modes, so a
-- turned off player area also stops stats, tops, safehouse info and sos
local function unlocked(name)
    if not AegisPlayerPanel.allowed() then return false end
    if not isServer() then return true end
    return panelFor(name) ~= nil
end

-- exported so the roles side can push a fresh grant the moment an admin
-- assigns or edits a role (the client only asked at game
-- start, a grant during the session never arrived until reconnect)
AegisPlayerPanel = AegisPlayerPanel or {}

-- server switch (sandbox): with the
-- player area turned off nobody gets the blue panel, admins included.
-- Missing option means on, so existing servers keep what they had
function AegisPlayerPanel.allowed()
    local on = true
    local v = SandboxVars and SandboxVars.AegisEvents and SandboxVars.AegisEvents.PlayerPanel
    if v ~= nil then on = v == true end
    return on
end

-- Knox Claim brings its own vehicle claim. Where it runs, two mods would
-- offer the same thing side by side and each would only know its own half,
-- so Aegis pulls its remember button and leaves the field. Asked at push
-- time, never while loading: the load order between two mods is not ours
-- to decide, and the global only exists once the other side has run.
-- The tile count decides as well, a server that set it to zero turned the
-- vehicle part off and then Aegis has to stay
local knoxSaid = nil

function AegisPlayerPanel.knoxVehicles()
    local on = false
    local why = "mod not loaded"
    pcall(function()
        if type(KnoxClaim) ~= "table" then return end
        local limits = KnoxClaim.limits and KnoxClaim.limits()
        local n = type(limits) == "table" and tonumber(limits.vehicles) or nil
        -- no readable limit means the mod is there and left at default
        on = (n == nil) or (n > 0)
        why = (n == nil) and "mod loaded, limit not readable"
            or ("mod loaded, vehicles per player = " .. tostring(n))
    end)
    -- one line per change, not per login: without it the only way to tell
    -- a missing mod from a zero limit was guessing
    if knoxSaid ~= on then
        knoxSaid = on
        print("[Aegis] Knox Claim owns vehicles: " .. tostring(on) .. " (" .. why .. ")")
    end
    return on
end

function AegisPlayerPanel.push(player)
    local name = username(player)
    if not name then return end
    if not AegisPlayerPanel.allowed() then
        -- an explicit refusal, not silence: a client that had the panel
        -- open closes it instead of keeping a dead window
        toClient(player, "ppSync", { enabled = false })
        return
    end
    if not isServer() then
        toClient(player, "ppSync", { enabled = true, role = "Solo", claimTiles = 0,
            kits = AegisShared.featureOn("PlayerKits"),
            knoxVehicles = AegisPlayerPanel.knoxVehicles() })
        return
    end
    local pp = panelFor(name)
    if pp then
        -- a booster without the kits flag still needs the redeem strip;
        -- boostReady opens the kits page for the boost slice, the kit
        -- list side enforces the matching scope
        local boostReady = AegisBoost.keySet() == true
        -- enabled carries the grant, role may be nil for the default
        -- panel every player gets
        -- the sandbox switches trim the grant before it travels: claims off
        -- means a zero tile budget, kits off hides the page and the strip
        local claimsOn = AegisShared.featureOn("PlayerClaims")
        local kitsOn = AegisShared.featureOn("PlayerKits")
        toClient(player, "ppSync", {
            enabled = true,
            role = pp.role, color = pp.color,
            claimTiles = claimsOn and pp.claimTiles or 0,
            kits = kitsOn and pp.kits or false,
            boostReady = kitsOn and boostReady or false,
            knoxVehicles = AegisPlayerPanel.knoxVehicles(),
        })
    else
        -- empty state, the client reads this as panel off
        toClient(player, "ppSync", {})
    end
end

local Commands = {}

Commands.ppReq = function(player, args)
    local name = username(player)
    if not name then return end
    if throttled(name, "pp", 3) then return end
    AegisPlayerPanel.push(player)
end

Commands.statsReq = function(player, args)
    local name = username(player)
    if not name or not unlocked(name) then return end
    if throttled(name, "stats", 2) then return end
    local s = AegisPlayerStats.get(name)
    s.playtimeH = AegisPlayerStats.playtimeHours(name)
    -- the ledger holds the finished lives, the running one is added on
    -- top. The server copy of hoursSurvived lags a full player sync
    -- behind, the client value is current, so the larger of the two
    -- counts; display only, the ledger itself is fed at death
    local live = tonumber(player and player:getHoursSurvived()) or 0
    local sent = tonumber(args and args.hours) or 0
    if sent > live and sent < 1000000 then live = sent end
    s.totalHours = math.floor(((s.totalHours or 0) + math.max(0, live)) * 10 + 0.5) / 10
    toClient(player, "statsSync", s)
end

local TOP_KINDS = { kills = true, dist = true, deaths = true, best = true }

Commands.topReq = function(player, args)
    local name = username(player)
    if not name or not unlocked(name) then return end
    local kind = args and args.kind
    if type(kind) ~= "string" or not TOP_KINDS[kind] then return end
    if throttled(name, "top", 2) then return end
    toClient(player, "topSync", { kind = kind, entries = AegisPlayerStats.top(kind) })
end

-- the zone a player belongs to, owner or member. Read here and not from
-- SafeHouse.hasSafehouse: its exact rule is not measured, and this is the
-- same test the page used to run on the client, only on the side that has
-- the truth. One pcall per entry, a single unreadable zone costs one row
local function safehouseOf(name)
    local found = nil
    pcall(function()
        local list = SafeHouse.getSafehouseList()
        if not list then return end
        for i = 0, list:size() - 1 do
            pcall(function()
                if found then return end
                local sh = list:get(i)
                if not sh then return end
                local owner = tostring(sh:getOwner() or "")
                local mine = owner == name
                local names = {}
                local pl = sh:getPlayers()
                if pl then
                    for j = 0, pl:size() - 1 do
                        local n = tostring(pl:get(j))
                        if n ~= owner then table.insert(names, n) end
                        if n == name then mine = true end
                    end
                end
                if mine then
                    table.insert(names, 1, owner)
                    found = { sh = sh, owner = owner, members = names,
                              title = tostring(sh:getTitle() or ""),
                              w = sh:getW(), h = sh:getH(),
                              x = sh:getX(), y = sh:getY() }
                end
            end)
        end
    end)
    return found
end

-- the safehouse card of the blue panel. It used to read the list on the
-- player's own client, and a zone another mod registers server side does
-- not have to be in that copy. The gold zone page has always read it on
-- the server, which is why a fresh claim showed up there and not here
Commands.shInfoReq = function(player, args)
    local name = username(player)
    if not name or not unlocked(name) then return end
    if throttled(name, "shinfo", 2) then return end
    local rec = safehouseOf(name)
    local days = nil
    if rec then
        pcall(function()
            local removal = getServerOptions():getInteger("SafeHouseRemovalTime")
            if type(removal) ~= "number" or removal <= 0 then return end
            local last = rec.sh:getLastVisited()
            if type(last) ~= "number" or last <= 0 then return end
            -- lastVisited is epoch millis: addSafeHouse stamps
            -- Calendar.getTimeInMillis and SafeHouse.update compares
            -- currentTimeMillis against lastVisited plus removal hours
            local remain = (last + removal * 3600000) - AegisShared.realTime() * 1000
            if remain < 0 then remain = 0 end
            days = math.floor(remain / 86400000 * 10 + 0.5) / 10
        end)
    end
    -- known says the answer is complete: no zone means no zone, and the
    -- client may drop what its own scan found. Without the flag an older
    -- server would silently erase the card
    -- respawn state and whether the server allows it at all: both come
    -- from here, because the zone may not exist in the client's own copy
    -- (a mod can register it server side) and then the client could
    -- neither read the flag nor find the object to set it
    local respawn, respawnAllowed = nil, false
    if rec then
        respawnAllowed = getServerOptions():getBoolean("SafehouseAllowRespawn") == true
        if respawnAllowed then
            respawn = rec.sh:isRespawnInSafehouse(name) == true
        end
    end
    toClient(player, "shInfoSync", {
        days = days, known = true,
        respawn = respawn, respawnAllowed = respawnAllowed,
        sh = rec and { owner = rec.owner, members = rec.members,
                       title = rec.title, w = rec.w, h = rec.h,
                       x = rec.x, y = rec.y } or nil,
    })
end

-- respawn in the own safehouse, set here instead of through the vanilla
-- client call: that one needs the zone OBJECT, and a zone another mod
-- registered server side is not necessarily in the client's copy. The
-- player may only ever set this for the zone they belong to
Commands.shRespawn = function(player, args)
    local name = username(player)
    if not name or not unlocked(name) then return end
    if throttled(name, "shrespawn", 2) then return end
    local rec = safehouseOf(name)
    if not rec then return end
    local on = args and args.on == true
    local ok = false
    pcall(function()
        if getServerOptions():getBoolean("SafehouseAllowRespawn") ~= true then return end
        rec.sh:setRespawnInSafehouse(on, name)
        ok = true
    end)
    if ok then
        AegisLog.write("Actions", name, name,
            "Respawn in safehouse " .. (on and "on" or "off"))
    end
    Commands.shInfoReq(player, {})
end

local SOS_COOLDOWN = 120
local lastSos = {}

-- calls no admin has dealt with yet: answered or closed cards clear
-- their entry, the blinking badge feeds off the count. The file keeps
-- the backlog across restarts, stale calls expire on load
local SOS_FILE = AegisStore.ROOT .. "/Status/sos.txt"
local SOS_MAX = 30
local SOS_KEEP = 24 * 3600

local sosOpen = nil

local function sosLoad()
    if sosOpen then return end
    sosOpen = {}
    local lines = AegisStore.readLines(SOS_FILE, 200)
    local now = AegisShared.realTime()
    for _, line in ipairs(lines or {}) do
        local epoch, who, x, y, z, text = line:match("^O|(%d+)|([^|]*)|(%-?%d+)|(%-?%d+)|(%-?%d+)|(.*)$")
        if epoch and now - (tonumber(epoch) or 0) < SOS_KEEP then
            table.insert(sosOpen, { at = tonumber(epoch), name = who,
                x = tonumber(x), y = tonumber(y), z = tonumber(z), text = text })
        end
    end
end

local function sosSave()
    local lines = {}
    for _, e in ipairs(sosOpen) do
        table.insert(lines, "O|" .. e.at .. "|" .. tostring(e.name):gsub("|", "_")
            .. "|" .. math.floor(e.x or 0) .. "|" .. math.floor(e.y or 0)
            .. "|" .. math.floor(e.z or 0) .. "|" .. e.text)
    end
    local content = table.concat(lines, "\n")
    if #lines > 0 then content = content .. "\n" end
    AegisStore.write(SOS_FILE, content)
end

local function sosPushCount()
    local players = getOnlinePlayers()
    if not players then return end
    for i = 0, players:size() - 1 do
        local p = players:get(i)
        if p and AegisRoles.isVanillaAdmin(p) then
            toClient(p, "sosPending", { n = #sosOpen })
        end
    end
end

Commands.sos = function(player, args)
    local name = username(player)
    if not name or not unlocked(name) then return end
    if throttled(name, "sos", 2) then return end
    local now = AegisShared.realTime()
    if lastSos[name] and now - lastSos[name] < SOS_COOLDOWN then
        toClient(player, "sosAck", { wait = SOS_COOLDOWN - (now - lastSos[name]) })
        return
    end
    lastSos[name] = now
    local text = cap(args and args.text or "", 200)
    local x, y, z = player:getX(), player:getY(), player:getZ()
    -- toast for every online admin over the player module, the client
    -- listener shows it only when the receiver is an admin; solo has
    -- nobody else to notify
    if isServer() then
        sosLoad()
        table.insert(sosOpen, { at = now, name = cap(name, 48), x = x, y = y, z = z, text = text })
        while #sosOpen > SOS_MAX do table.remove(sosOpen, 1) end
        sosSave()
        local players = getOnlinePlayers()
        if players then
            for i = 0, players:size() - 1 do
                local p = players:get(i)
                if p and AegisRoles.isVanillaAdmin(p) then
                    toClient(p, "sosNotice", {
                        key = "UI_Aegis_SosNotice", par = cap(name, 48),
                        x = x, y = y, z = z, text = text, at = now,
                    })
                    toClient(p, "sosPending", { n = #sosOpen })
                end
            end
        end
    end
    local pos = ""
    if x and y then
        pos = " at " .. math.floor(x) .. "," .. math.floor(y) .. "," .. math.floor(z or 0)
    end
    AegisLog.write("Actions", name, name, "SOS: " .. text .. pos)
    toClient(player, "sosAck", { wait = 0 })
end

-- the badge asks once on boot; only staff gets an answer
Commands.sosCountReq = function(player, args)
    if not isServer() then return end
    if not AegisRoles.isVanillaAdmin(player) then return end
    sosLoad()
    toClient(player, "sosPending", { n = #sosOpen })
end

-- the blinking badge was clicked: hand the newest calls over as the
-- usual alert cards and consider the whole backlog seen
Commands.sosPendingReq = function(player, args)
    if not isServer() then return end
    if not AegisRoles.isVanillaAdmin(player) then return end
    sosLoad()
    if #sosOpen == 0 then return end
    for i = math.max(1, #sosOpen - 3), #sosOpen do
        local e = sosOpen[i]
        toClient(player, "sosNotice", {
            key = "UI_Aegis_SosNotice", par = cap(e.name, 48),
            x = e.x, y = e.y, z = e.z, text = e.text, at = e.at,
        })
    end
    sosOpen = {}
    sosSave()
    sosPushCount()
    AegisLog.write("Actions", username(player) or "?", "players", "Missed SOS calls reviewed")
end

-- a live card was answered or closed: that call is settled for everyone
Commands.sosSeen = function(player, args)
    if not isServer() then return end
    if not AegisRoles.isVanillaAdmin(player) then return end
    local at = tonumber(args and args.at)
    if not at then return end
    sosLoad()
    for i = #sosOpen, 1, -1 do
        if sosOpen[i].at == at then table.remove(sosOpen, i) end
    end
    sosSave()
    sosPushCount()
end

-- the one-shot count request races the boot: it can arrive before the
-- server knows the sender is an admin and is then dropped for good. A
-- slow pulse catches every admin a minute in at the latest, and stays
-- silent while nothing is open
local sosPulseAt = 0
Events.OnTick.Add(function()
    if not isServer() then return end
    local now = AegisShared.realTime()
    if now < sosPulseAt then return end
    sosPulseAt = now + 60
    sosLoad()
    if #sosOpen > 0 then sosPushCount() end
end)

-- kitClaim is handled ONLY by the player dispatcher in Aegis_Kits.lua;
-- a routing copy here double dispatched every claim and passed the raw
-- args table where the kit id was expected

-- the sandbox switches flip live on the server (the vanilla channel
-- broadcasts new SandboxVars), but nothing pushed a fresh grant to
-- connected clients: panels stayed up after a turn off, and a client
-- refused at join never asked again after a turn on.
-- This watcher pushes everyone whenever one of the three values changes.
-- PlayerVehicles is deliberately absent: the client reads that cap straight
-- out of SandboxVars, which vanilla broadcasts on its own
local lastFeat = nil

local function featSnapshot()
    return (AegisPlayerPanel.allowed() and "1" or "0")
        .. (AegisShared.featureOn("PlayerClaims") and "1" or "0")
        .. (AegisShared.featureOn("PlayerKits") and "1" or "0")
end

Events.EveryOneMinute.Add(function()
    local now = featSnapshot()
    if lastFeat == nil then
        lastFeat = now
        return
    end
    if now == lastFeat then return end
    lastFeat = now
    if isServer() then
        pcall(function()
            local players = getOnlinePlayers()
            for i = 0, players:size() - 1 do
                local pl = players:get(i)
                if pl then AegisPlayerPanel.push(pl) end
            end
        end)
    else
        local p = getPlayer()
        if p then AegisPlayerPanel.push(p) end
    end
end)

local function onClientCommand(module, command, player, args)
    if module ~= MODULE then return end
    -- suspended senders keep no authority in any Aegis area
    if AegisModeration.isSuspended(player) then return end
    if Commands[command] then Commands[command](player, args) end
end

Events.OnClientCommand.Add(onClientCommand)
