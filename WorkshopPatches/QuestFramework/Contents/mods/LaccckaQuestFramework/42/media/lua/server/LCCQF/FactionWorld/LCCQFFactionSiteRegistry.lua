require "LCCQF/LCCQFConstants"
require "LCCQF/Content/LCCQFFactionDefinitions"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local FactionRegistry = LCCQF.FactionRegistry
local Sites = LCCQF.FactionSiteRegistry or {}

local LIVE_STATES = {
    RESERVED = true,
    VALIDATING = true,
    ACTIVE = true,
    DORMANT = true,
    RELOCATING = true,
}

-- RELOCATING is intentionally excluded: maxSites limits settled destinations, while
-- an old site may coexist transiently with one replacement reservation.
local CAPACITY_STATES = {
    RESERVED = true,
    VALIDATING = true,
    ACTIVE = true,
    DORMANT = true,
}

local TRANSITIONS = {
    RESERVED = { VALIDATING = true, ABANDONED = true },
    VALIDATING = { ACTIVE = true, RESERVED = true, ABANDONED = true },
    ACTIVE = { DORMANT = true, RELOCATING = true, ABANDONED = true },
    DORMANT = { ACTIVE = true, RELOCATING = true, ABANDONED = true },
    RELOCATING = { ACTIVE = true, ABANDONED = true },
    ABANDONED = {},
}

local function log(message)
    print(C.LOG_PREFIX .. "[FACTION:SITE:REGISTRY] " .. tostring(message))
end

local function worldHours()
    if not getGameTime then return 0 end
    local gameTime = getGameTime()
    if not gameTime or not gameTime.getWorldAgeHours then return 0 end
    return tonumber(gameTime:getWorldAgeHours()) or 0
end

local function validId(value)
    return type(value) == "string" and value ~= "" and #value <= C.MAX_IDENTIFIER_LENGTH * 3
end

local function ensureStore()
    if not ModData or not ModData.getOrCreate then return nil end
    local store = ModData.getOrCreate(C.FACTION_SITE_MODDATA_KEY)
    if type(store) ~= "table" then return nil end
    if tonumber(store.schemaVersion) == nil then
        store.schemaVersion = C.FACTION_SITE_PERSISTENCE_SCHEMA_VERSION
    end
    if tonumber(store.revision) == nil then store.revision = 0 end
    if tonumber(store.nextSiteSequence) == nil then store.nextSiteSequence = 1 end
    if type(store.sitesById) ~= "table" then store.sitesById = {} end
    if type(store.siteIdsByFaction) ~= "table" then store.siteIdsByFaction = {} end
    if type(store.reservationsByCandidateKey) ~= "table" then store.reservationsByCandidateKey = {} end
    return store
end

-- Faction-site ModData is persistent server world state, not a gameplay replication
-- surface. Privileged diagnostics expose only sanitized projections.
local function touch(store)
    store.revision = math.max(0, math.floor(tonumber(store.revision) or 0)) + 1
end

local function copyTable(input)
    if type(input) ~= "table" then return nil end
    local out = {}
    for key, value in pairs(input) do
        if type(value) == "number" or type(value) == "string" or type(value) == "boolean" then
            out[key] = value
        end
    end
    return out
end

local function sanitizeCandidate(candidate)
    return {
        candidateKey = tostring(candidate.candidateKey),
        kind = tostring(candidate.kind or "building"),
        anchor = copyTable(candidate.anchor) or {},
        sample = copyTable(candidate.sample) or {},
        bounds = copyTable(candidate.bounds) or {},
        buildingFingerprint = tostring(candidate.buildingFingerprint or candidate.candidateKey),
        roomCount = math.max(0, math.floor(tonumber(candidate.roomCount) or 0)),
        area = math.max(0, tonumber(candidate.area) or 0),
        zoneType = tostring(candidate.zoneType or "Unknown"),
        score = tonumber(candidate.score) or 0,
        scoreBreakdown = copyTable(candidate.scoreBreakdown) or {},
    }
end

local function appendFactionSite(store, factionId, siteId)
    local ids = store.siteIdsByFaction[factionId]
    if type(ids) ~= "table" then
        ids = {}
        store.siteIdsByFaction[factionId] = ids
    end
    for _, existing in ipairs(ids) do
        if existing == siteId then return end
    end
    ids[#ids + 1] = siteId
end

local function newSiteId(store)
    local sequence = math.max(1, math.floor(tonumber(store.nextSiteSequence) or 1))
    store.nextSiteSequence = sequence + 1
    return "lccqf_site_" .. tostring(sequence)
end

function Sites.Initialize()
    local store = ensureStore()
    if not store then return false, "global ModData unavailable" end
    log("initialized schemaVersion=" .. tostring(store.schemaVersion)
        .. " revision=" .. tostring(store.revision))
    return true
end

function Sites.GetStoreRevision()
    local store = ensureStore()
    return store and math.max(0, math.floor(tonumber(store.revision) or 0)) or 0
end

function Sites.MarkDirty(siteId, reason)
    local site = Sites.GetSite(siteId)
    if not site then return false, "site not found" end
    local store = ensureStore()
    if not store then return false, "global ModData unavailable" end
    site.updatedWorldHours = worldHours()
    if reason ~= nil then site.lastUpdateReason = tostring(reason) end
    touch(store)
    return true, site
end

function Sites.GetSite(siteId)
    if not validId(siteId) then return nil end
    local store = ensureStore()
    if not store then return nil end
    local site = store.sitesById[siteId]
    return type(site) == "table" and site or nil
end

function Sites.ListSites()
    local store = ensureStore()
    local out = {}
    if not store then return out end
    for _, site in pairs(store.sitesById) do
        if type(site) == "table" then out[#out + 1] = site end
    end
    table.sort(out, function(a, b) return tostring(a.siteId) < tostring(b.siteId) end)
    return out
end

function Sites.ListFactionSites(factionId, liveOnly)
    local store = ensureStore()
    local out = {}
    if not store or not validId(factionId) then return out end
    local ids = store.siteIdsByFaction[factionId]
    if type(ids) ~= "table" then return out end
    for _, siteId in ipairs(ids) do
        local site = store.sitesById[siteId]
        if type(site) == "table" and (not liveOnly or LIVE_STATES[site.state] == true) then
            out[#out + 1] = site
        end
    end
    return out
end

function Sites.FindRelocatingSite(factionId)
    for _, site in ipairs(Sites.ListFactionSites(factionId, true)) do
        if site.state == "RELOCATING" then return site end
    end
    return nil
end

function Sites.HasCapacity(factionId, maxSites)
    local limit = math.max(1, math.floor(tonumber(maxSites) or 1))
    local count = 0
    for _, site in ipairs(Sites.ListFactionSites(factionId, true)) do
        if CAPACITY_STATES[site.state] then count = count + 1 end
    end
    return count < limit
end

function Sites.IsCandidateReserved(candidateKey)
    if not validId(candidateKey) then return false, nil end
    local store = ensureStore()
    if not store then return false, nil end
    local siteId = store.reservationsByCandidateKey[candidateKey]
    if not siteId then return false, nil end
    local site = store.sitesById[siteId]
    if type(site) ~= "table" or LIVE_STATES[site.state] ~= true then
        store.reservationsByCandidateKey[candidateKey] = nil
        return false, nil
    end
    return true, site
end

function Sites.DistanceToNearestOtherFactionSite(factionId, x, y)
    x = tonumber(x)
    y = tonumber(y)
    if not x or not y then return nil, nil end
    local bestDistance, bestSite
    for _, site in ipairs(Sites.ListSites()) do
        if LIVE_STATES[site.state] and site.factionId ~= factionId and type(site.anchor) == "table" then
            local sx = tonumber(site.anchor.x)
            local sy = tonumber(site.anchor.y)
            if sx and sy then
                local dx = sx - x
                local dy = sy - y
                local distance = math.sqrt(dx * dx + dy * dy)
                if bestDistance == nil or distance < bestDistance then
                    bestDistance, bestSite = distance, site
                end
            end
        end
    end
    return bestDistance, bestSite
end

function Sites.ReserveCandidate(factionId, candidate, source)
    if not validId(factionId) or not FactionRegistry.Get(factionId) then
        return false, "unknown faction"
    end
    if type(candidate) ~= "table" or not validId(candidate.candidateKey) then
        return false, "invalid candidate"
    end

    local store = ensureStore()
    if not store then return false, "global ModData unavailable" end
    local profile = FactionRegistry.Get(factionId).siteProfile or {}
    if not Sites.HasCapacity(factionId, profile.maxSites or 1) then
        return false, "faction site capacity reached"
    end

    local reserved, reservedSite = Sites.IsCandidateReserved(candidate.candidateKey)
    if reserved then
        return false, "candidate already reserved by " .. tostring(reservedSite.factionId)
    end

    local siteId = newSiteId(store)
    local snapshot = sanitizeCandidate(candidate)
    local now = worldHours()
    local site = {
        siteId = siteId,
        factionId = factionId,
        state = "RESERVED",
        source = tostring(source or "allocator"),
        createdWorldHours = now,
        updatedWorldHours = now,
        candidateKey = snapshot.candidateKey,
        kind = snapshot.kind,
        anchor = snapshot.anchor,
        sample = snapshot.sample,
        bounds = snapshot.bounds,
        buildingFingerprint = snapshot.buildingFingerprint,
        roomCount = snapshot.roomCount,
        area = snapshot.area,
        zoneType = snapshot.zoneType,
        score = snapshot.score,
        scoreBreakdown = snapshot.scoreBreakdown,
        validationRevision = 0,
    }

    local relocating = Sites.FindRelocatingSite(factionId)
    if relocating and not relocating.replacementSiteId then
        site.relocatesSiteId = relocating.siteId
        relocating.replacementSiteId = siteId
        relocating.updatedWorldHours = now
        relocating.lastUpdateReason = "replacement site reserved"
    end

    store.sitesById[siteId] = site
    store.reservationsByCandidateKey[site.candidateKey] = siteId
    appendFactionSite(store, factionId, siteId)
    touch(store)

    log("reserved siteId=" .. tostring(siteId)
        .. " factionId=" .. tostring(factionId)
        .. " candidate=" .. tostring(site.candidateKey)
        .. " zone=" .. tostring(site.zoneType)
        .. " rooms=" .. tostring(site.roomCount)
        .. " score=" .. string.format("%.2f", tonumber(site.score) or 0)
        .. " relocatesSiteId=" .. tostring(site.relocatesSiteId or "none"))
    return true, site
end

function Sites.Transition(siteId, nextState, reason)
    local site = Sites.GetSite(siteId)
    if not site then return false, "site not found" end
    if type(nextState) ~= "string" or not TRANSITIONS[site.state] or not TRANSITIONS[site.state][nextState] then
        return false, "invalid site transition"
    end

    local store = ensureStore()
    if not store then return false, "global ModData unavailable" end
    local previousState = site.state
    site.state = nextState
    site.updatedWorldHours = worldHours()
    site.lastReason = reason and tostring(reason) or nil
    site.validationRevision = math.max(0, math.floor(tonumber(site.validationRevision) or 0)) + 1

    if LIVE_STATES[nextState] ~= true and store.reservationsByCandidateKey[site.candidateKey] == siteId then
        store.reservationsByCandidateKey[site.candidateKey] = nil
    end
    touch(store)
    log("transition siteId=" .. tostring(siteId)
        .. " " .. tostring(previousState) .. "->" .. tostring(nextState)
        .. " reason=" .. tostring(reason or "none"))
    return true, site
end

function Sites.BeginRelocation(siteId, reason)
    local site = Sites.GetSite(siteId)
    if not site then return false, "site not found" end
    if site.state ~= "ACTIVE" and site.state ~= "DORMANT" then
        return false, "site is not relocatable"
    end
    site.relocationRequestedWorldHours = worldHours()
    site.relocationReason = tostring(reason or "server relocation request")
    return Sites.Transition(siteId, "RELOCATING", site.relocationReason)
end

LCCQF.FactionSiteRegistry = Sites
return Sites