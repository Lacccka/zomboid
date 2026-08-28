require "LCCQF/LCCQFConstants"
require "LCCQF/FactionWorld/LCCQFFactionSiteRegistry"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Sites = LCCQF.FactionSiteRegistry
local Index = LCCQF.FactionSiteCandidateIndex or {}

local candidates = Index.candidates or {}
local rejectionHistory = Index.rejectionHistory or {}
local roomCursor = tonumber(Index.roomCursor) or 0
local discoveryPass = tonumber(Index.discoveryPass) or 0

local function worldHours()
    if not getGameTime then return 0 end
    local gameTime = getGameTime()
    if not gameTime or not gameTime.getWorldAgeHours then return 0 end
    return tonumber(gameTime:getWorldAgeHours()) or 0
end

local function callNumber(object, methodName)
    local value
    pcall(function()
        local method = object and object[methodName]
        if method then value = method(object) end
    end)
    return tonumber(value)
end

local function getZoneType(x, y)
    local zoneType
    pcall(function()
        local world = getWorld and getWorld()
        local metaGrid = world and world:getMetaGrid()
        local zone = metaGrid and metaGrid:getZoneAt(math.floor(x), math.floor(y), 0)
        if zone then zoneType = zone:getType() end
    end)
    if type(zoneType) ~= "string" or zoneType == "" then return "Unknown" end
    return zoneType
end

local function getRoomCount(buildingDef)
    local count = 0
    pcall(function()
        local rooms = buildingDef and buildingDef:getRooms()
        count = rooms and rooms:size() or 0
    end)
    return math.max(0, math.floor(tonumber(count) or 0))
end

local function freeSquareFromRoom(room)
    local square
    pcall(function()
        if room and room.getRandomFreeSquare then square = room:getRandomFreeSquare() end
    end)
    if square then return square end
    pcall(function()
        local squares = room and room:getSquares()
        if squares and squares:size() > 0 then square = squares:get(0) end
    end)
    return square
end

local function candidateCount()
    local count = 0
    for _ in pairs(candidates) do count = count + 1 end
    return count
end

local function pruneIfNeeded()
    local maximum = math.max(16, math.floor(tonumber(C.FACTION_SITE_MAX_CANDIDATES) or 128))
    while candidateCount() > maximum do
        local oldestKey, oldestPass, oldestHours
        for key, candidate in pairs(candidates) do
            local pass = tonumber(candidate.lastObservedPass) or 0
            local hours = tonumber(candidate.lastObservedWorldHours) or 0
            if oldestKey == nil or pass < oldestPass or (pass == oldestPass and hours < oldestHours) then
                oldestKey, oldestPass, oldestHours = key, pass, hours
            end
        end
        if not oldestKey then return end
        candidates[oldestKey] = nil
    end
end

function Index.ObserveRoom(room)
    if not room then return nil, "room unavailable" end
    local building
    pcall(function() building = room:getBuilding() end)
    if not building then return nil, "building unavailable" end

    local buildingDef
    pcall(function() buildingDef = building:getDef() end)
    if not buildingDef then return nil, "building definition unavailable" end

    local x = callNumber(buildingDef, "getX")
    local y = callNumber(buildingDef, "getY")
    local w = callNumber(buildingDef, "getW")
    local h = callNumber(buildingDef, "getH")
    if not x or not y or not w or not h or w <= 0 or h <= 0 then
        return nil, "building bounds unavailable"
    end

    x, y, w, h = math.floor(x), math.floor(y), math.floor(w), math.floor(h)
    local anchorX = x + math.floor(w / 2)
    local anchorY = y + math.floor(h / 2)

    -- getCell():getRoomList() can expose a room/building definition before any square
    -- from that room is currently streamed in. Such metadata is useful later, but it is
    -- not a loaded-world candidate yet. Never fall back to the BuildingDef centre: doing
    -- so produced candidates that the validator immediately had to DEFER as unloaded.
    local sampleSquare = freeSquareFromRoom(room)
    if not sampleSquare then
        return nil, "room has no loaded sample square"
    end

    local sampleX, sampleY, sampleZ
    pcall(function()
        sampleX = sampleSquare:getX()
        sampleY = sampleSquare:getY()
        sampleZ = sampleSquare:getZ()
    end)
    if tonumber(sampleX) == nil or tonumber(sampleY) == nil or tonumber(sampleZ) == nil then
        return nil, "loaded sample coordinates unavailable"
    end
    sampleX = math.floor(tonumber(sampleX))
    sampleY = math.floor(tonumber(sampleY))
    sampleZ = math.floor(tonumber(sampleZ))

    local key = "building:" .. tostring(x) .. ":" .. tostring(y)
        .. ":" .. tostring(w) .. ":" .. tostring(h)
    local candidate = candidates[key] or {}
    candidate.candidateKey = key
    candidate.kind = "building"
    candidate.anchor = { x = anchorX, y = anchorY, z = 0 }
    candidate.sample = { x = sampleX, y = sampleY, z = sampleZ }
    candidate.bounds = { x = x, y = y, w = w, h = h, x2 = x + w - 1, y2 = y + h - 1 }
    candidate.buildingFingerprint = key
    candidate.roomCount = getRoomCount(buildingDef)
    candidate.area = w * h
    candidate.zoneType = getZoneType(anchorX, anchorY)
    candidate.lastObservedWorldHours = worldHours()
    candidate.lastObservedPass = discoveryPass
    candidates[key] = candidate
    pruneIfNeeded()
    return candidate
end

function Index.DiscoverLoadedBuildings(maxRooms)
    discoveryPass = discoveryPass + 1
    local limit = math.max(1, math.floor(tonumber(maxRooms) or C.FACTION_SITE_MAX_ROOMS_PER_PASS or 256))
    local cell = getCell and getCell()
    if not cell or not cell.getRoomList then return 0, 0 end

    local rooms
    pcall(function() rooms = cell:getRoomList() end)
    local size = rooms and rooms:size() or 0
    if size <= 0 then
        roomCursor = 0
        Index.roomCursor = roomCursor
        Index.discoveryPass = discoveryPass
        return 0, 0
    end

    if roomCursor >= size then roomCursor = 0 end
    local processed = math.min(limit, size)
    local observed = 0
    for offset = 0, processed - 1 do
        local index = (roomCursor + offset) % size
        local room = rooms:get(index)
        local candidate = Index.ObserveRoom(room)
        if candidate then observed = observed + 1 end
    end
    roomCursor = (roomCursor + processed) % size
    Index.roomCursor = roomCursor
    Index.discoveryPass = discoveryPass
    return observed, size
end

local function rejectionPenalty(factionId, candidateKey)
    local byFaction = rejectionHistory[factionId]
    local entry = byFaction and byFaction[candidateKey]
    local attempts = entry and tonumber(entry.attempts) or 0
    return math.min(10, attempts * 1.5)
end

function Index.NoteRejection(factionId, candidateKey, reason)
    if type(factionId) ~= "string" or type(candidateKey) ~= "string" then return end
    local byFaction = rejectionHistory[factionId]
    if type(byFaction) ~= "table" then
        byFaction = {}
        rejectionHistory[factionId] = byFaction
    end
    local entry = byFaction[candidateKey] or { attempts = 0 }
    entry.attempts = math.max(0, math.floor(tonumber(entry.attempts) or 0)) + 1
    entry.reason = tostring(reason or "rejected")
    entry.worldHours = worldHours()
    byFaction[candidateKey] = entry
end

function Index.Score(definition, candidate)
    if type(definition) ~= "table" or type(candidate) ~= "table" then
        return nil, "invalid scoring input"
    end
    local profile = definition.siteProfile
    if type(profile) ~= "table" or profile.enabled == false then
        return nil, "site allocation disabled"
    end

    local minimumRooms = math.max(0, math.floor(tonumber(profile.minRooms) or 0))
    if (tonumber(candidate.roomCount) or 0) < minimumRooms then
        return nil, "insufficient rooms"
    end

    local reserved, site = Sites.IsCandidateReserved(candidate.candidateKey)
    if reserved then
        return nil, "reserved by " .. tostring(site and site.factionId or "another faction")
    end

    local minimumOtherDistance = math.max(0, tonumber(profile.minDistanceFromOtherFactionSites) or 0)
    if minimumOtherDistance > 0 and type(candidate.anchor) == "table" then
        local distance = Sites.DistanceToNearestOtherFactionSite(
            definition.factionId,
            candidate.anchor.x,
            candidate.anchor.y
        )
        if distance and distance < minimumOtherDistance then
            return nil, "too close to another faction site"
        end
    end

    local breakdown = {}
    local zoneType = tostring(candidate.zoneType or "Unknown")
    local preferred = type(profile.preferredZones) == "table" and tonumber(profile.preferredZones[zoneType]) or nil
    local avoided = type(profile.avoidedZones) == "table" and tonumber(profile.avoidedZones[zoneType]) or nil
    breakdown.zone = (preferred or 0) - (avoided or 0)
    breakdown.rooms = math.min(8, math.max(0, (tonumber(candidate.roomCount) or 0) - minimumRooms) * 0.75)
    breakdown.capacity = math.min(10, math.max(0, tonumber(candidate.area) or 0) / 75)
    breakdown.indoor = profile.wantsIndoor == true and 2 or 0
    breakdown.rejectionHistory = -rejectionPenalty(definition.factionId, candidate.candidateKey)

    local score = 0
    for _, value in pairs(breakdown) do score = score + (tonumber(value) or 0) end
    return score, breakdown
end

function Index.RankForFaction(definition, limit)
    local ranked = {}
    local minScore = tonumber(definition and definition.siteProfile and definition.siteProfile.minScore) or -math.huge
    for _, candidate in pairs(candidates) do
        local score, breakdownOrReason = Index.Score(definition, candidate)
        if score and score >= minScore then
            local scored = {}
            for key, value in pairs(candidate) do scored[key] = value end
            scored.score = score
            scored.scoreBreakdown = breakdownOrReason
            ranked[#ranked + 1] = scored
        end
    end
    table.sort(ranked, function(a, b)
        if a.score == b.score then return tostring(a.candidateKey) < tostring(b.candidateKey) end
        return a.score > b.score
    end)

    local maximum = math.max(1, math.floor(tonumber(limit) or #ranked))
    while #ranked > maximum do table.remove(ranked) end
    return ranked
end

function Index.GetCandidate(candidateKey)
    return candidates[candidateKey]
end

function Index.ListCandidates()
    local out = {}
    for _, candidate in pairs(candidates) do out[#out + 1] = candidate end
    table.sort(out, function(a, b) return tostring(a.candidateKey) < tostring(b.candidateKey) end)
    return out
end

Index.candidates = candidates
Index.rejectionHistory = rejectionHistory
Index.roomCursor = roomCursor
Index.discoveryPass = discoveryPass
LCCQF.FactionSiteCandidateIndex = Index
return Index
