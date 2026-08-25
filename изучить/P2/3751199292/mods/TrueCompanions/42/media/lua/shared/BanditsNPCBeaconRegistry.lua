--
-- True Companions - Beacon registry (shared data layer)
--
-- WHY THIS FILE EXISTS. The beacon registry is the mod's central store: sites, their
-- stage/capacity/broadcast state, the zones drawn on them and the containers assigned to
-- them. Everything downstream reads it -- Base.SiteNear, the relax and job programs, the
-- wild-spawn arrival roll.
--
-- It lived entirely inside client/BanditsNPCBeacon.lua, which meant two things in
-- multiplayer:
--   * A DEDICATED SERVER HAD NO BEACON MODULE AT ALL, so server/BanditsNPCWildSpawn.lua
--     took its `elseif not force then return false` branch on every roll -- wild arrivals
--     were dead, silently, for the entire mode.
--   * Each client only ever saw the beacons IT had registered, so Base.SiteNear returned
--     different answers on different machines and the base-derived decisions built on it
--     (where home is, where to relax, where work is delivered) disagreed.
--
-- Only the DATA moves here. The 1,225-line client file keeps its build cursor, context
-- menus, drag handling, key binding and windows -- none of which belong on a server.
--
-- WRITES GO THROUGH THE SERVER, AND THAT IS THE WHOLE DESIGN.
-- Vanilla only ever transmits global mod data from server/ (forageServer.lua:49). Whether
-- a dedicated server honours a CLIENT-originated ModData.transmit is a question this
-- codebase cannot answer by reading, so it is sidestepped rather than assumed: a client
-- never writes the shared store directly, it asks; the server validates, writes and
-- transmits. That is the same shape as BanditsNPC.SetOpt and Editor.SetSpawnConfig, and it
-- is correct whichever way the unanswered question would have gone.
--
-- THE STORE KEY IS UNCHANGED ("TrueCompanionsBeacons"), so existing saves carry straight
-- over with no migration -- the data was always global mod data, it was simply never
-- shared.
--

BanditsNPC = BanditsNPC or {}
BanditsNPC.BeaconRegistry = BanditsNPC.BeaconRegistry or {}
local R = BanditsNPC.BeaconRegistry

R.KEY = "TrueCompanionsBeacons"

function R.Store()
    local r = ModData.getOrCreate(R.KEY)
    if type(r.sites) ~= "table" then r.sites = {} end
    return r
end

function R.Sites()
    return R.Store().sites
end

function R.KeyFor(x, y, z)
    return tostring(math.floor(x)) .. "," .. tostring(math.floor(y)) .. "," .. tostring(math.floor(z))
end

function R.AllSites()
    local out = {}
    for _, site in pairs(R.Sites()) do out[#out + 1] = site end
    return out
end

-- ===== queries the SERVER needs (pure data -- no client state is touched) =====

function R.AnyWelcoming()
    for _, site in pairs(R.Sites()) do
        if site.welcome then return true end
    end
    return false
end

function R.NearestWelcoming(x, y, z)
    local best, bestD
    for _, site in pairs(R.Sites()) do
        if site.welcome then
            local d = BanditUtils.DistTo(x, y, site.x, site.y)
            if math.abs((site.z or 0) - (z or 0)) < 1 and (bestD == nil or d < bestD) then
                best, bestD = site, d
            end
        end
    end
    return best, bestD
end

function R.NoteArrival(site)
    if site then site.waitHours = 0 end
end

-- ===== replication =====

local function localName()
    local un
    pcall(function()
        local p = getSpecificPlayer(0)
        un = p and p:getUsername()
    end)
    if type(un) == "string" and un ~= "" then return un end
    return nil
end

-- A site record stripped to what is worth sending. autoSpots is a DERIVED cache that
-- Base.Rescan rebuilds from the world every 90s and can hold hundreds of entries -- it
-- must never ride the wire, and a receiving client must rebuild it against its own view
-- of the map rather than trust someone else's.
local function wireCopy(site)
    if type(site) ~= "table" then return nil end
    local out = {}
    for k, v in pairs(site) do
        if k ~= "autoSpots" and k ~= "autoSpotsAt" then out[k] = v end
    end
    return out
end

-- May `who` (a username, or nil when unknown) write over the stored site at `key`?
-- Unowned and absent entries are claimable -- that covers single player, pre-v0.75.5
-- beacons and a brand-new placement. A site with an owner may only be changed by them.
function R.MayWrite(key, who)
    local cur = R.Sites()[key]
    if type(cur) ~= "table" then return true end
    local owner = cur.owner
    if type(owner) ~= "string" or owner == "" then return true end
    return who ~= nil and who == owner
end

-- THE AUTHORITATIVE WRITE. Server-side in MP, direct in SP. Never call this from a client
-- in multiplayer -- call R.Push, which routes.
function R.ApplySite(key, site, who)
    if type(key) ~= "string" or key == "" then return false end
    if not key:match("^%-?%d+,%-?%d+,%-?%d+$") then return false end   -- x,y,z only
    if type(site) ~= "table" then return false end
    if not R.MayWrite(key, who) then return false end

    local store = R.Store()
    local cur = store.sites[key]
    local incoming = wireCopy(site)
    -- Keep the receiver's own derived cache; only the shared fields travel.
    if type(cur) == "table" then
        incoming.autoSpots = cur.autoSpots
        incoming.autoSpotsAt = cur.autoSpotsAt
    end
    store.sites[key] = incoming
    pcall(function() ModData.transmit(R.KEY) end)
    return true
end

function R.ApplyRemove(key, who)
    if type(key) ~= "string" or key == "" then return false end
    if not R.MayWrite(key, who) then return false end
    local store = R.Store()
    if store.sites[key] == nil then return false end
    store.sites[key] = nil
    pcall(function() ModData.transmit(R.KEY) end)
    return true
end

-- Called by the CLIENT after it has changed a site locally. In single player the store is
-- already the real one, so this only needs to transmit; in multiplayer the change has to
-- be asked for, because the client's copy is not authoritative.
function R.Push(key)
    if type(key) ~= "string" or key == "" then return end
    local site = R.Sites()[key]
    if site == nil then return R.PushRemove(key) end
    if isClient and isClient() then
        local p = getSpecificPlayer(0)
        if not p then return end
        sendClientCommand(p, 'BanditsNPC', 'SetSite', { key = key, site = wireCopy(site) })
        return
    end
    R.ApplySite(key, site, localName())
end

function R.PushRemove(key)
    if type(key) ~= "string" or key == "" then return end
    if isClient and isClient() then
        local p = getSpecificPlayer(0)
        if not p then return end
        sendClientCommand(p, 'BanditsNPC', 'RemoveSite', { key = key })
        return
    end
    R.ApplyRemove(key, localName())
end

-- Convenience for callers holding a site rather than a key.
function R.PushSite(site)
    if type(site) ~= "table" or site.x == nil then return end
    R.Push(R.KeyFor(site.x, site.y, site.z or 0))
end

-- ===== the arrival roll (pure data; the SERVER runs this) =====
--
-- Rates are the sandbox enum 1..4 (Off / Rare / Occasional / Often). `guarantee` is the
-- backstop: after that many waiting hours a survivor turns up regardless, so a run of bad
-- luck cannot leave a broadcasting beacon empty forever.
R.ARRIVAL = {
    chance    = { 0,  8, 18, 35 },
    guarantee = { 0, 36, 16,  6 },
}

function R.Waited(site)
    if not site then return 0 end
    return (type(site.waitHours) == "number") and site.waitHours or 0
end

function R.RollArrival(site)
    local rate = BanditsNPC.Opt("WildSpawn", 2)
    if type(rate) ~= "number" then rate = 2 end
    local chance = R.ARRIVAL.chance[rate] or 0
    if chance <= 0 then return false end

    site.waitHours = R.Waited(site) + 1
    if ZombRandFloat(0, 100) < chance then return true end

    local guarantee = R.ARRIVAL.guarantee[rate] or 0
    return guarantee > 0 and site.waitHours >= guarantee
end
