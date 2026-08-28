-- Privileged diagnostics only. Gameplay clients never receive faction site world state
-- through this path; admins/debug users can request a sanitized snapshot for testing.
if isClient and isClient() and not (isServer and isServer()) then
    return {}
end

require "LCCQF/LCCQFConstants"
require "LCCQF/FactionWorld/LCCQFFactionSiteRegistry"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Sites = LCCQF.FactionSiteRegistry
local DebugServer = LCCQF.FactionSiteDebugServer or {}
local lastRequestMs = {}

local function log(message)
    print(C.LOG_PREFIX .. "[FACTION:SITE:DEBUG:SERVER] " .. tostring(message))
end

local function playerKey(player)
    if not player then return nil end
    if player.getOnlineID then return tostring(player:getOnlineID()) end
    if player.getUsername then return tostring(player:getUsername()) end
    return nil
end

local function isPrivileged(player)
    if not player then return false end
    if isDebugEnabled and isDebugEnabled() then return true end
    if not player.getAccessLevel then return false end
    local access = tostring(player:getAccessLevel() or ""):lower()
    return access ~= "" and access ~= "none"
end

local function copyPoint(input)
    if type(input) ~= "table" then return nil end
    local x, y, z = tonumber(input.x), tonumber(input.y), tonumber(input.z)
    if not x or not y then return nil end
    return { x = x, y = y, z = z or 0 }
end

local function copyCounts(input)
    local out = {}
    if type(input) ~= "table" then return out end
    for _, key in ipairs({
        "beds", "chairs", "televisions", "storage", "food", "water",
        "stoves", "washers", "windows", "bookshelves", "freeSpawnPoints", "objects",
    }) do
        out[key] = math.max(0, tonumber(input[key]) or 0)
    end
    return out
end

local function sanitizeSite(site)
    local derived = type(site.derived) == "table" and site.derived or nil
    return {
        siteId = tostring(site.siteId or ""),
        factionId = tostring(site.factionId or ""),
        state = tostring(site.state or "UNKNOWN"),
        source = tostring(site.source or "unknown"),
        anchor = copyPoint(site.anchor),
        zoneType = tostring(site.zoneType or "Unknown"),
        roomCount = math.max(0, tonumber(site.roomCount) or 0),
        area = math.max(0, tonumber(site.area) or 0),
        score = tonumber(site.score) or 0,
        lastReason = site.lastReason and tostring(site.lastReason) or nil,
        validationRevision = math.max(0, tonumber(site.validationRevision) or 0),
        resources = derived and {
            complete = derived.complete == true,
            safeHouseOverlap = derived.safeHouseOverlap == true,
            tilesVisited = math.max(0, tonumber(derived.tilesVisited) or 0),
            scannedWorldHours = tonumber(derived.scannedWorldHours) or 0,
            counts = copyCounts(derived.counts),
        } or nil,
    }
end

local function buildSnapshot()
    local out = {}
    for _, site in ipairs(Sites.ListSites()) do
        out[#out + 1] = sanitizeSite(site)
    end
    return out
end

local function sendSnapshot(player)
    local sites = buildSnapshot()
    sendServerCommand(player, C.MODULE, C.COMMAND.FACTION_SITES_DEBUG, {
        revision = Sites.GetStoreRevision(),
        sites = sites,
    })
    log("snapshot player=" .. tostring(player:getUsername())
        .. " revision=" .. tostring(Sites.GetStoreRevision())
        .. " sites=" .. tostring(#sites))
end

local function onClientCommand(module, command, player, args)
    if module ~= C.MODULE or command ~= C.COMMAND.REQUEST_FACTION_SITES_DEBUG then return end
    if not isPrivileged(player) then
        log("request rejected player=" .. tostring(player and player:getUsername()) .. " reason=no-permission")
        return
    end

    local key = playerKey(player)
    if not key then return end
    local now = getTimestampMs and getTimestampMs() or 0
    local previous = tonumber(lastRequestMs[key]) or -100000
    if now - previous < 500 then return end
    lastRequestMs[key] = now
    sendSnapshot(player)
end

if isServer and isServer() and Events and Events.OnClientCommand then
    Events.OnClientCommand.Add(onClientCommand)
end

DebugServer.BuildSnapshot = buildSnapshot
LCCQF.FactionSiteDebugServer = DebugServer
return DebugServer
