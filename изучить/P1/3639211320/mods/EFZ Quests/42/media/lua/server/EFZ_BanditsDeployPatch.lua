if isClient() then
    return
end

if not EFZ then
    EFZ = {}
end

EFZ.BanditsZonePatch = EFZ.BanditsZonePatch or {}
local Patch = EFZ.BanditsZonePatch

local PATCH_LOG_PREFIX = "[EFZ][BanditsZone] "

local NO_BANDIT_MIN_X = 19968
local NO_BANDIT_MIN_Y = 0
local NO_BANDIT_MAX_X = 20479
local NO_BANDIT_MAX_Y = 767
local BASE_GUARD_EXPAND = 128
local SPAWN_CONTEXT_TTL_MS = 1000
local DEPLOY_AMBUSH_STATE_KEY = "EFZDeployBandits2AmbushGlobal"
local DEPLOY_AMBUSH_HOURS_PER_DAY = 24
local DEPLOY_AMBUSH_HOURS_PER_SEGMENT = 8
local DEPLOY_AMBUSH_SEGMENTS_PER_DAY = 3
local DEPLOY_AMBUSH_FRIENDLY_CHANCE_PERCENT = 30
local DEFAULT_AMBUSH_TARGETS = "00_Hunters,01_Rednecks,06_Robbers,08_Hikers,10_Mafia,12_Militia"
local AMBUSH_SEARCH_RADIUS = 30
local AMBUSH_MIN_SEARCH_RADIUS = 15
local AMBUSH_GROUP_CLUSTER_RADIUS = 3
local AMBUSH_MAX_GROUP_SIZE = 3
local AMBUSH_PRIMARY_POINT_ATTEMPTS = 64
local AMBUSH_CLUSTER_POINT_ATTEMPTS = 48

local EMPTY_ZOMBIE_LIST = {
    size = function(self)
        return 0
    end,
    get = function(self, idx)
        return nil
    end,
}

local function logInfo(message)
    DebugLog.log(PATCH_LOG_PREFIX .. message)
end

local function syncDeployAmbushResult(playerObj, success, reason, slotKey)
    if not sendServerCommand then
        return
    end

    local payload = {
        success = success == true,
        reason = reason or "",
        slotKey = slotKey,
    }
    if playerObj and playerObj.getPlayerNum then
        payload.playerNum = playerObj:getPlayerNum()
    end
    sendServerCommand("EFZ", "SyncDeployAmbushResult", payload)
end

local ensureSpawnerPatch
local markDeployAmbushTriggered
local applyDeployAmbushBehaviorOverrides

local function toNumber(value)
    local n = tonumber(value)
    if n == nil then
        return nil
    end
    return n
end

local function floorCoord(value)
    local n = toNumber(value)
    if n == nil then
        return "nil"
    end
    return tostring(math.floor(n))
end

local function isInsideNoBanditZone(x, y)
    x = toNumber(x)
    y = toNumber(y)
    if x == nil or y == nil then
        return false
    end
    return x >= NO_BANDIT_MIN_X and x <= NO_BANDIT_MAX_X and y >= NO_BANDIT_MIN_Y and y <= NO_BANDIT_MAX_Y
end

local function isAreaIntersectNoBanditZone(x, y, w, h)
    x = toNumber(x)
    y = toNumber(y)
    w = toNumber(w) or 0
    h = toNumber(h) or 0
    if x == nil or y == nil then
        return false
    end

    local minX = math.min(x, x + w)
    local maxX = math.max(x, x + w)
    local minY = math.min(y, y + h)
    local maxY = math.max(y, y + h)

    if maxX < NO_BANDIT_MIN_X then return false end
    if minX > NO_BANDIT_MAX_X then return false end
    if maxY < NO_BANDIT_MIN_Y then return false end
    if minY > NO_BANDIT_MAX_Y then return false end
    return true
end

local function isInsideExpandedNoBanditZone(x, y, expand)
    x = toNumber(x)
    y = toNumber(y)
    expand = toNumber(expand) or 0
    if x == nil or y == nil then
        return false
    end
    return x >= (NO_BANDIT_MIN_X - expand)
        and x <= (NO_BANDIT_MAX_X + expand)
        and y >= (NO_BANDIT_MIN_Y - expand)
        and y <= (NO_BANDIT_MAX_Y + expand)
end

local function getDeployAmbushDayKey()
    local gameTime = getGameTime and getGameTime() or nil
    if not gameTime then
        return nil
    end

    local year = tonumber(gameTime:getYear()) or 0
    local month = tonumber(gameTime:getMonth()) or 0
    local day = tonumber(gameTime:getDay()) or 0
    local hour = tonumber(gameTime:getHour()) or 0
    local segment = math.floor(hour / DEPLOY_AMBUSH_HOURS_PER_SEGMENT)
    if segment >= DEPLOY_AMBUSH_SEGMENTS_PER_DAY then
        segment = DEPLOY_AMBUSH_SEGMENTS_PER_DAY - 1
    end

    return (((year * 12) + month) * 31 + day) * DEPLOY_AMBUSH_SEGMENTS_PER_DAY + segment
end

local function getDeployAmbushHourOfDay()
    local gameTime = getGameTime and getGameTime() or nil
    if not gameTime then
        return nil
    end

    return tonumber(gameTime:getHour()) or 0
end

local function getDeployAmbushState()
    if ModData and ModData.getOrCreate then
        return ModData.getOrCreate(DEPLOY_AMBUSH_STATE_KEY)
    end

    Patch._deployAmbushState = Patch._deployAmbushState or {}
    return Patch._deployAmbushState
end

local function transmitDeployAmbushState()
    if ModData and ModData.transmit then
        ModData.transmit(DEPLOY_AMBUSH_STATE_KEY)
    end
end

local function getDeployAmbushSlotKey(dayKey, hourOfDay)
    dayKey = tonumber(dayKey)
    hourOfDay = tonumber(hourOfDay)
    if dayKey == nil or hourOfDay == nil then
        return nil
    end
    return (dayKey * DEPLOY_AMBUSH_HOURS_PER_DAY) + hourOfDay
end

local function formatDeployAmbushHourSlot(hourOfDay)
    hourOfDay = tonumber(hourOfDay)
    if hourOfDay == nil then
        return "nil"
    end
    return string.format("%02d:00-%02d:59", hourOfDay, hourOfDay)
end

local function getDeployAmbushSegmentStartHour(dayKey)
    dayKey = tonumber(dayKey)
    if dayKey == nil then
        return nil
    end

    local segmentIndex = dayKey % DEPLOY_AMBUSH_SEGMENTS_PER_DAY
    return segmentIndex * DEPLOY_AMBUSH_HOURS_PER_SEGMENT
end

local function isDeployAmbushFriendlyForState(state)
    return state and state.friendlyRoll == true
end

local function getDeployAmbushPlayerIdentifiers(playerObj)
    local onlineID = playerObj and playerObj.getOnlineID and playerObj:getOnlineID() or nil
    local playerNum = playerObj and playerObj.getPlayerNum and playerObj:getPlayerNum() or nil
    return onlineID, playerNum
end

local function isAliveDeployAmbushPlayer(playerObj)
    if not playerObj then
        return false
    end
    if playerObj.isDead and playerObj:isDead() then
        return false
    end
    return true
end

local function isDeployAmbushCandidate(playerObj)
    if not isAliveDeployAmbushPlayer(playerObj) then
        return false
    end

    local modData = playerObj.getModData and playerObj:getModData() or nil
    local deployState = modData and modData.EFZDeploy or nil
    if type(deployState) ~= "table" or deployState.active ~= true then
        return false
    end

    if playerObj.getX and playerObj.getY and isInsideNoBanditZone(playerObj:getX(), playerObj:getY()) then
        return false
    end

    return true
end

local function findOnlinePlayerByIdentifiers(onlineID, playerNum)
    local onlinePlayers = getOnlinePlayers and getOnlinePlayers() or nil
    if not onlinePlayers then
        return nil
    end

    local playerNumMatch = nil
    for i = 0, onlinePlayers:size() - 1 do
        local candidate = onlinePlayers:get(i)
        if candidate then
            if onlineID ~= nil and candidate.getOnlineID and candidate:getOnlineID() == onlineID then
                return candidate
            end
            if playerNum ~= nil and candidate.getPlayerNum and candidate:getPlayerNum() == playerNum then
                playerNumMatch = candidate
            end
        end
    end

    return playerNumMatch
end

local function clearDeployAmbushTarget(state, slotKey)
    state.selectedSlotKey = slotKey
    state.targetOnlineID = nil
    state.targetPlayerNum = nil
end

local function ensureDeployAmbushSchedule(state, dayKey)
    if not state or dayKey == nil then
        return nil, nil
    end

    local scheduledDayKey = tonumber(state.scheduledDayKey)
    local scheduledHourOfDay = tonumber(state.scheduledHourOfDay)
    local scheduledSlotKey = getDeployAmbushSlotKey(dayKey, scheduledHourOfDay)
    local segmentStartHour = getDeployAmbushSegmentStartHour(dayKey)
    local segmentEndHour = segmentStartHour and (segmentStartHour + DEPLOY_AMBUSH_HOURS_PER_SEGMENT - 1) or nil
    local needsNewSchedule = scheduledDayKey ~= dayKey
        or scheduledHourOfDay == nil
        or segmentStartHour == nil
        or segmentEndHour == nil
        or scheduledHourOfDay < segmentStartHour
        or scheduledHourOfDay > segmentEndHour
        or scheduledSlotKey == nil
        or tonumber(state.consumeRollDayKey) ~= dayKey
        or type(state.consumeRoll) ~= "boolean"
        or tonumber(state.friendlyRollDayKey) ~= dayKey
        or type(state.friendlyRoll) ~= "boolean"

    if not needsNewSchedule then
        if tonumber(state.scheduledSlotKey) ~= scheduledSlotKey then
            state.scheduledSlotKey = scheduledSlotKey
            transmitDeployAmbushState()
        end
        return scheduledHourOfDay, scheduledSlotKey
    end

    scheduledHourOfDay = segmentStartHour + ZombRand(DEPLOY_AMBUSH_HOURS_PER_SEGMENT)
    scheduledSlotKey = getDeployAmbushSlotKey(dayKey, scheduledHourOfDay)

    state.scheduledDayKey = dayKey
    state.scheduledHourOfDay = scheduledHourOfDay
    state.scheduledSlotKey = scheduledSlotKey
    state.consumeRollDayKey = dayKey
    state.consumeRoll = ZombRand(100) < 50
    state.friendlyRollDayKey = dayKey
    state.friendlyRoll = ZombRand(100) < DEPLOY_AMBUSH_FRIENDLY_CHANCE_PERCENT
    clearDeployAmbushTarget(state, scheduledSlotKey)

    transmitDeployAmbushState()

    logInfo("Scheduled deploy ambush 8-hour slot " .. tostring(scheduledSlotKey)
        .. " at " .. formatDeployAmbushHourSlot(scheduledHourOfDay)
        .. " (consumeRoll=" .. tostring(state.consumeRoll)
        .. ", friendlyRoll=" .. tostring(state.friendlyRoll) .. ").")

    return scheduledHourOfDay, scheduledSlotKey
end

local function selectDeployAmbushTarget(state, slotKey, requester)
    local candidates = {}
    local onlinePlayers = getOnlinePlayers and getOnlinePlayers() or nil
    if onlinePlayers then
        for i = 0, onlinePlayers:size() - 1 do
            local candidate = onlinePlayers:get(i)
            if isDeployAmbushCandidate(candidate) then
                candidates[#candidates + 1] = candidate
            end
        end
    end

    local targetPlayer = nil
    local selectionSource = "deployCandidates"
    if #candidates > 0 then
        targetPlayer = candidates[ZombRand(#candidates) + 1]
    elseif isAliveDeployAmbushPlayer(requester) then
        targetPlayer = requester
        selectionSource = "requesterFallback"
    else
        clearDeployAmbushTarget(state, slotKey)
        return false
    end

    local onlineID, playerNum = getDeployAmbushPlayerIdentifiers(targetPlayer)
    if onlineID == nil and playerNum == nil then
        clearDeployAmbushTarget(state, slotKey)
        return false
    end

    state.selectedSlotKey = slotKey
    state.targetOnlineID = onlineID
    state.targetPlayerNum = playerNum

    logInfo("Selected deploy ambush target for slot " .. tostring(slotKey)
        .. " (onlineID=" .. tostring(onlineID)
        .. ", playerNum=" .. tostring(playerNum)
        .. ", source=" .. selectionSource .. ").")

    return true
end

local function matchesDeployAmbushTarget(state, playerObj)
    if not state or not playerObj then
        return false
    end

    local onlineID, playerNum = getDeployAmbushPlayerIdentifiers(playerObj)
    if state.targetOnlineID ~= nil and onlineID ~= nil then
        return state.targetOnlineID == onlineID
    end
    if state.targetPlayerNum ~= nil and playerNum ~= nil then
        return state.targetPlayerNum == playerNum
    end

    return false
end

local function resolveDeployAmbushRequest(playerObj, requestedSlotKey)
    local currentDayKey = getDeployAmbushDayKey()
    local currentHourOfDay = getDeployAmbushHourOfDay()
    local currentSlotKey = getDeployAmbushSlotKey(currentDayKey, currentHourOfDay)
    if currentDayKey == nil or currentHourOfDay == nil or currentSlotKey == nil then
        return false, "deployAmbushNoSlot", nil, nil, nil
    end

    requestedSlotKey = tonumber(requestedSlotKey)
    if requestedSlotKey ~= nil and requestedSlotKey ~= currentSlotKey then
        return false, "deployAmbushStaleSlot", nil, nil, nil
    end

    local state = getDeployAmbushState()
    local scheduledHourOfDay, scheduledSlotKey = ensureDeployAmbushSchedule(state, currentDayKey)
    if scheduledHourOfDay == nil or scheduledSlotKey == nil then
        return false, "deployAmbushNoSchedule", nil, nil, nil
    end

    if currentSlotKey ~= scheduledSlotKey then
        return false, "deployAmbushDifferentHour", nil, nil, nil
    end

    if tonumber(state.lastResolvedDayKey) == currentDayKey then
        return false, "deployAmbushDayUsed", nil, nil, nil
    end

    if state.selectedSlotKey ~= scheduledSlotKey then
        clearDeployAmbushTarget(state, scheduledSlotKey)
    end

    local targetPlayer = findOnlinePlayerByIdentifiers(state.targetOnlineID, state.targetPlayerNum)
    if (state.targetOnlineID ~= nil or state.targetPlayerNum ~= nil) and not isDeployAmbushCandidate(targetPlayer) then
        clearDeployAmbushTarget(state, scheduledSlotKey)
    end

    if state.targetOnlineID == nil and state.targetPlayerNum == nil then
        if not selectDeployAmbushTarget(state, scheduledSlotKey, playerObj) then
            return false, "deployAmbushNoTarget", nil, nil, nil
        end
    end

    if not matchesDeployAmbushTarget(state, playerObj) then
        return false, "deployAmbushDifferentTarget", nil, nil, nil
    end

    if state.consumeRoll ~= true then
        state.lastResolvedDayKey = currentDayKey
        state.lastResolvedSlotKey = scheduledSlotKey
        clearDeployAmbushTarget(state, scheduledSlotKey)
        transmitDeployAmbushState()
        return false, "deployAmbushRollMiss", scheduledSlotKey, state, currentDayKey
    end

    return true, "", scheduledSlotKey, state, currentDayKey
end

local function shouldBlockDeployAmbushSlot(name, playerObj, args)
    if name ~= "Clan" or type(args) ~= "table" or args.efzDeployAmbush ~= true then
        return false, "", nil, nil, nil
    end

    local requestedSlotKey = args.efzDeployAmbushSlotKey
    local allowed, reason, slotKey, state, dayKey = resolveDeployAmbushRequest(playerObj, requestedSlotKey)
    if not allowed then
        return true, reason, nil, nil, nil
    end

    return false, "", slotKey, state, dayKey
end

local function splitCsv(text)
    local result = {}
    for token in string.gmatch(text, "([^,]+)") do
        result[#result + 1] = token:match("^%s*(.-)%s*$")
    end
    return result
end

local function parseTargetToken(token)
    if not token or token == "" then
        return nil, 0
    end

    local clanName, weightText = token:match("^(.-)%-(%d+)$")
    if clanName then
        return clanName, tonumber(weightText) or 0
    end

    return token, 1
end

local function buildClanLookup()
    if not BanditCustom or type(BanditCustom.ClanGetAll) ~= "function" then
        return nil
    end

    local clans = BanditCustom.ClanGetAll()
    local lookup = {}
    for cid, clan in pairs(clans) do
        local name = clan and clan.general and clan.general.name or nil
        if name then
            lookup[tostring(name)] = tostring(cid)
        end
    end

    return lookup
end

local function buildWeightedTargets()
    local clanLookup = buildClanLookup()
    if not clanLookup then
        return nil, 0, DEFAULT_AMBUSH_TARGETS, "BanditCustom.ClanGetAll unavailable"
    end

    local entries = {}
    local totalWeight = 0

    for _, token in ipairs(splitCsv(DEFAULT_AMBUSH_TARGETS)) do
        local clanName, weight = parseTargetToken(token)
        local cid = clanName and clanLookup[clanName] or nil
        if cid and weight > 0 then
            entries[#entries + 1] = {
                cid = cid,
                name = clanName,
                weight = weight,
            }
            totalWeight = totalWeight + weight
        end
    end

    if #entries == 0 or totalWeight <= 0 then
        return nil, 0, DEFAULT_AMBUSH_TARGETS, "No valid Bandits2 clans matched the target list"
    end

    return entries, totalWeight, DEFAULT_AMBUSH_TARGETS, nil
end

local function pickWeightedTarget(entries, totalWeight)
    if not entries or totalWeight <= 0 then
        return nil
    end

    local roll = ZombRand(totalWeight) + 1
    local sum = 0

    for _, entry in ipairs(entries) do
        sum = sum + entry.weight
        if roll <= sum then
            return entry
        end
    end

    return entries[#entries]
end

local function isWithinAmbushRadius(px, py, x, y)
    local dx = x - px
    local dy = y - py
    local d2 = dx * dx + dy * dy
    return d2 >= (AMBUSH_MIN_SEARCH_RADIUS * AMBUSH_MIN_SEARCH_RADIUS)
        and d2 <= (AMBUSH_SEARCH_RADIUS * AMBUSH_SEARCH_RADIUS)
end

local function getPlayerVisionIndex(playerObj)
    if not playerObj then
        return nil
    end
    if playerObj.getPlayerNum then
        return playerObj:getPlayerNum()
    end
    if playerObj.playerIndex ~= nil then
        return playerObj.playerIndex
    end
    return nil
end

local function isSquareOutsidePlayerVision(playerObj, square)
    if not playerObj or not square then
        return false
    end

    local playerIndex = getPlayerVisionIndex(playerObj)
    if playerIndex == nil then
        return false
    end
    if not square.isCanSee then
        return false
    end

    return square:isCanSee(playerIndex) == false
end

local function isSquareValidForAmbushSpawn(playerObj, square)
    if not square then
        return false
    end
    if isInsideNoBanditZone(square:getX(), square:getY()) then
        return false
    end
    if not square.isFree or not square:isFree(false) then
        return false
    end

    return isSquareOutsidePlayerVision(playerObj, square)
end

local function findPrimaryAmbushSpawnPoint(playerObj)
    local cell = playerObj and playerObj:getCell() or nil
    if not cell then
        return nil
    end

    local px = math.floor(playerObj:getX())
    local py = math.floor(playerObj:getY())
    local pz = math.floor(playerObj:getZ())

    for _ = 1, AMBUSH_PRIMARY_POINT_ATTEMPTS do
        local dx = ZombRand(AMBUSH_SEARCH_RADIUS * 2 + 1) - AMBUSH_SEARCH_RADIUS
        local dy = ZombRand(AMBUSH_SEARCH_RADIUS * 2 + 1) - AMBUSH_SEARCH_RADIUS
        local x = px + dx
        local y = py + dy

        if isWithinAmbushRadius(px, py, x, y) then
            local square = cell:getGridSquare(x, y, pz)
            if isSquareValidForAmbushSpawn(playerObj, square) then
                return {
                    x = math.floor(x),
                    y = math.floor(y),
                    z = math.floor(pz),
                }
            end
        end
    end

    return nil
end

local function collectAmbushSpawnPoints(playerObj, desiredCount)
    local primaryPoint = findPrimaryAmbushSpawnPoint(playerObj)
    if not primaryPoint then
        return {}
    end

    local points = { primaryPoint }
    local used = {
        [string.format("%d:%d:%d", primaryPoint.x, primaryPoint.y, primaryPoint.z)] = true,
    }

    if desiredCount <= 1 then
        return points
    end

    local cell = playerObj:getCell()
    local px = math.floor(playerObj:getX())
    local py = math.floor(playerObj:getY())
    local pz = math.floor(playerObj:getZ())

    for _ = 1, AMBUSH_CLUSTER_POINT_ATTEMPTS do
        local x = primaryPoint.x + ZombRand(AMBUSH_GROUP_CLUSTER_RADIUS * 2 + 1) - AMBUSH_GROUP_CLUSTER_RADIUS
        local y = primaryPoint.y + ZombRand(AMBUSH_GROUP_CLUSTER_RADIUS * 2 + 1) - AMBUSH_GROUP_CLUSTER_RADIUS
        local key = string.format("%d:%d:%d", x, y, pz)

        if not used[key] and isWithinAmbushRadius(px, py, x, y) then
            local square = cell:getGridSquare(x, y, pz)
            if isSquareValidForAmbushSpawn(playerObj, square) then
                used[key] = true
                points[#points + 1] = {
                    x = math.floor(x),
                    y = math.floor(y),
                    z = math.floor(pz),
                }
                if #points >= desiredCount then
                    break
                end
            end
        end
    end

    return points
end

local function invokeOriginalSpawnerClan(playerObj, args)
    Patch._originalSpawner = Patch._originalSpawner or {}
    local original = Patch._originalSpawner.Clan
    if type(original) ~= "function" then
        ensureSpawnerPatch()
        original = Patch._originalSpawner.Clan
    end
    if type(original) ~= "function" then
        return false
    end

    original(playerObj, args)
    return true
end

local function canForceDeployAmbush(playerObj)
    if not playerObj then
        return false
    end

    if playerObj.isAccessLevel and playerObj:isAccessLevel("admin") then
        return true
    end
    if isAdmin and isAdmin() then
        return true
    end
    if getDebug and getDebug() then
        if playerObj.canUseDebugContextMenu then
            return playerObj:canUseDebugContextMenu()
        end
        return true
    end

    return false
end

local function executeDeployAmbushSpawn(playerObj, options)
    options = options or {}
    local force = options.force == true
    local slotKey = options.slotKey
    local state = options.state
    local dayKey = options.dayKey
    local logTag = force and "ForceDeployAmbush" or "RequestDeployAmbush"

    if isInsideNoBanditZone(playerObj:getX(), playerObj:getY()) then
        logInfo("Rejected " .. logTag .. ": requester inside no-bandit zone.")
        syncDeployAmbushResult(playerObj, false, "deployAmbushNoAmbushZone", slotKey)
        return false
    end

    local entries, totalWeight, source, err = buildWeightedTargets()
    if not entries then
        logInfo("Rejected " .. logTag .. ": " .. tostring(err) .. " (" .. tostring(source) .. ").")
        syncDeployAmbushResult(playerObj, false, "deployAmbushNoClan", slotKey)
        return false
    end

    local target = pickWeightedTarget(entries, totalWeight)
    if not target then
        logInfo("Rejected " .. logTag .. ": weighted clan pick failed.")
        syncDeployAmbushResult(playerObj, false, "deployAmbushNoClan", slotKey)
        return false
    end

    local desiredCount = 1 + ZombRand(AMBUSH_MAX_GROUP_SIZE)
    local spawnPoints = collectAmbushSpawnPoints(playerObj, desiredCount)
    if #spawnPoints == 0 then
        logInfo("Rejected " .. logTag .. ": no valid spawn point within 15-30 tiles outside player vision.")
        syncDeployAmbushResult(playerObj, false, "deployAmbushNoSpot", slotKey)
        return false
    end

    local behaviorState = state
    if force then
        behaviorState = {
            friendlyRoll = ZombRand(100) < DEPLOY_AMBUSH_FRIENDLY_CHANCE_PERCENT,
        }
    end

    local spawnArgs = {
        cid = target.cid,
        program = "Looter",
        size = #spawnPoints,
        spawnPoints = spawnPoints,
        efzDeployAmbush = not force,
        efzDeployAmbushSlotKey = slotKey,
        efzDeployAmbushDayKey = dayKey,
        efzDeployAmbushHourOfDay = state and tonumber(state.scheduledHourOfDay) or nil,
        efzForceDeployAmbush = force,
    }

    applyDeployAmbushBehaviorOverrides(spawnArgs, behaviorState)

    logInfo("Executing " .. logTag .. " for clan " .. tostring(target.name)
        .. " with " .. tostring(#spawnPoints) .. " spawn point(s)"
        .. " near " .. tostring(spawnPoints[1].x) .. "," .. tostring(spawnPoints[1].y) .. "," .. tostring(spawnPoints[1].z)
        .. (force and " (forced)." or "."))

    if not invokeOriginalSpawnerClan(playerObj, spawnArgs) then
        logInfo("Rejected " .. logTag .. ": BanditServer.Spawner.Clan unavailable.")
        syncDeployAmbushResult(playerObj, false, "deployAmbushSpawnerUnavailable", slotKey)
        return false
    end

    if type(TransmitBanditModData) == "function" then
        TransmitBanditModData()
    end

    if not force and slotKey ~= nil and state and dayKey ~= nil then
        markDeployAmbushTriggered(slotKey, state, dayKey)
        logInfo("Consumed deploy ambush slot " .. tostring(slotKey)
            .. " for clan " .. tostring(target.name) .. ".")
    end

    syncDeployAmbushResult(playerObj, true, "", slotKey)
    return true
end

local function handleRequestDeployAmbush(playerObj, args)
    if not isAliveDeployAmbushPlayer(playerObj) then
        logInfo("Rejected RequestDeployAmbush: dead or missing player.")
        return
    end

    local requestedSlotKey = args and tonumber(args.efzDeployAmbushSlotKey)
    local allowed, reason, slotKey, state, dayKey = resolveDeployAmbushRequest(playerObj, requestedSlotKey)
    if not allowed then
        logInfo("Rejected RequestDeployAmbush (onlineID="
            .. tostring(playerObj.getOnlineID and playerObj:getOnlineID() or "nil")
            .. ", reason=" .. tostring(reason) .. ").")
        syncDeployAmbushResult(playerObj, false, reason, slotKey or requestedSlotKey)
        return
    end

    executeDeployAmbushSpawn(playerObj, {
        slotKey = slotKey,
        state = state,
        dayKey = dayKey,
    })
end

local function handleForceDeployAmbush(playerObj, args)
    if not isAliveDeployAmbushPlayer(playerObj) then
        logInfo("Rejected ForceDeployAmbush: dead or missing player.")
        syncDeployAmbushResult(playerObj, false, "deployAmbushNoTarget", nil)
        return
    end

    if not canForceDeployAmbush(playerObj) then
        logInfo("Rejected ForceDeployAmbush: insufficient permissions (onlineID="
            .. tostring(playerObj.getOnlineID and playerObj:getOnlineID() or "nil") .. ").")
        syncDeployAmbushResult(playerObj, false, "deployAmbushNoPermission", nil)
        return
    end

    executeDeployAmbushSpawn(playerObj, {
        force = true,
    })
end

markDeployAmbushTriggered = function(slotKey, state, dayKey)
    if slotKey == nil or not state or dayKey == nil then
        return
    end

    state.lastTriggeredSlot = slotKey
    state.lastResolvedDayKey = dayKey
    state.lastResolvedSlotKey = slotKey
    clearDeployAmbushTarget(state, slotKey)
    transmitDeployAmbushState()
end

applyDeployAmbushBehaviorOverrides = function(args, state)
    if type(args) ~= "table" or not state then
        return
    end

    local isFriendly = isDeployAmbushFriendlyForState(state)
    args.hostile = not isFriendly
    args.hostileP = not isFriendly
end

local function nowMs()
    if getTimestampMs then
        return getTimestampMs()
    end
    return 0
end

local function setSpawnContextPlayer(playerObj)
    if not playerObj or not playerObj.getX or not playerObj.getY then
        return
    end
    Patch._spawnContextPlayer = playerObj
    Patch._spawnContextExpireMs = nowMs() + SPAWN_CONTEXT_TTL_MS
end

local function consumeSpawnContextPlayer()
    local playerObj = Patch._spawnContextPlayer
    if not playerObj then
        return nil
    end
    local expireAt = Patch._spawnContextExpireMs or 0
    if expireAt > 0 and nowMs() > expireAt then
        Patch._spawnContextPlayer = nil
        Patch._spawnContextExpireMs = 0
        return nil
    end
    Patch._spawnContextPlayer = nil
    Patch._spawnContextExpireMs = 0
    return playerObj
end

local function cloneTable(source)
    local copy = {}
    for k, v in pairs(source) do
        copy[k] = v
    end
    return copy
end

local function hasSpawnPointInNoBanditZone(spawnPoints)
    if not spawnPoints then
        return false
    end

    if type(spawnPoints) == "table" then
        for _, sp in pairs(spawnPoints) do
            if sp and isInsideNoBanditZone(sp.x, sp.y) then
                return true
            end
        end
        return false
    end

    if spawnPoints.size and spawnPoints.get then
        for i = 0, spawnPoints:size() - 1 do
            local sp = spawnPoints:get(i)
            if sp and isInsideNoBanditZone(sp.x, sp.y) then
                return true
            end
        end
    end
    return false
end

local function shouldBlockSpawnerCall(name, playerObj, args)
    args = args or {}

    if name == "Restore" then
        local brain = args
        local born = brain and brain.bornCoords
        if born and isInsideNoBanditZone(born.x, born.y) then
            return true, "restoreCoords"
        end
        return false, ""
    end

    if args.x ~= nil and args.y ~= nil and isInsideNoBanditZone(args.x, args.y) then
        return true, "targetCoords"
    end

    if args.spawnPoints and hasSpawnPointInNoBanditZone(args.spawnPoints) then
        return true, "spawnPoints"
    end

    if args.dist ~= nil and playerObj and playerObj.getX and playerObj.getY then
        local expand = (toNumber(args.dist) or 0) + 16
        if isInsideExpandedNoBanditZone(playerObj:getX(), playerObj:getY(), expand) then
            return true, "distCouldHitZone"
        end
    end

    return false, ""
end

local function exportOriginalAddZombiesInOutfit(original)
    if type(original) ~= "function" then
        return false
    end

    if type(Patch.OriginalAddZombiesInOutfit) ~= "function" then
        Patch.OriginalAddZombiesInOutfit = original
    end

    if type(EFZ_BanditsOriginalAddZombiesInOutfit) ~= "function" then
        EFZ_BanditsOriginalAddZombiesInOutfit = Patch.OriginalAddZombiesInOutfit
        logInfo("Exported original AddZombiesInOutfit as EFZ_BanditsOriginalAddZombiesInOutfit")
    end

    return true
end

local function wrapAddZombiesInOutfit()
    if not BanditCompatibility or type(BanditCompatibility.AddZombiesInOutfit) ~= "function" then
        return false
    end
    if Patch._wrappedAddZombiesInOutfit then
        return true
    end

    local original = BanditCompatibility.AddZombiesInOutfit
    exportOriginalAddZombiesInOutfit(original)
    Patch._wrappedAddZombiesInOutfit = true

    BanditCompatibility.AddZombiesInOutfit = function(x, y, z, outfit, femaleChance, crawler, isFallOnFront, isFakeDead, knockedDown, isInvulnerable, isSitting, health)
        if isInsideNoBanditZone(x, y) then
            logInfo("Blocked bandit spawn at (" .. floorCoord(x) .. ", " .. floorCoord(y) .. ", " .. floorCoord(z) .. ")")
            return EMPTY_ZOMBIE_LIST
        end
        return original(x, y, z, outfit, femaleChance, crawler, isFallOnFront, isFakeDead, knockedDown, isInvulnerable, isSitting, health)
    end

    logInfo("Wrapped BanditCompatibility.AddZombiesInOutfit")
    return true
end

local function wrapAddVehicleDebug()
    if type(addVehicleDebug) ~= "function" then
        return false
    end
    if Patch._wrappedAddVehicleDebug then
        return true
    end

    local original = addVehicleDebug
    Patch._wrappedAddVehicleDebug = true

    addVehicleDebug = function(vtype, dir, skin, square)
        if square and isInsideNoBanditZone(square:getX(), square:getY()) then
            logInfo("Blocked bandit vehicle/base at (" .. floorCoord(square:getX()) .. ", " .. floorCoord(square:getY()) .. ", " .. floorCoord(square:getZ()) .. ")")
            return nil
        end
        return original(vtype, dir, skin, square)
    end

    logInfo("Wrapped addVehicleDebug")
    return true
end

local function wrapBanditBasePlacements()
    if not BanditBasePlacements then
        return false
    end

    Patch._wrappedBasePlacements = Patch._wrappedBasePlacements or {}

    local function wrap(name, xIndex, yIndex)
        if Patch._wrappedBasePlacements[name] then
            return false
        end
        local original = BanditBasePlacements[name]
        if type(original) ~= "function" then
            return false
        end

        BanditBasePlacements[name] = function(...)
            local args = { ... }
            local x = args[xIndex]
            local y = args[yIndex]
            if isInsideNoBanditZone(x, y) then
                logInfo("Blocked BanditBasePlacements." .. name .. " at (" .. floorCoord(x) .. ", " .. floorCoord(y) .. ")")
                return nil
            end
            return original(...)
        end

        Patch._wrappedBasePlacements[name] = true
        return true
    end

    local changed = false
    if wrap("Matress", 1, 2) then changed = true end
    if wrap("IsoObject", 2, 3) then changed = true end
    if wrap("IsoThumpable", 2, 3) then changed = true end
    if wrap("IsoDoor", 2, 3) then changed = true end
    if wrap("IsoWindow", 2, 3) then changed = true end
    if wrap("IsoCurtain", 2, 3) then changed = true end
    if wrap("IsoLightSwitch", 2, 3) then changed = true end
    if wrap("IsoGenerator", 2, 3) then changed = true end
    if wrap("Container", 2, 3) then changed = true end
    if wrap("WaterContainer", 2, 3) then changed = true end
    if wrap("Fireplace", 2, 3) then changed = true end
    if wrap("Fridge", 2, 3) then changed = true end
    if wrap("Journal", 3, 4) then changed = true end
    if wrap("Item", 2, 3) then changed = true end
    if wrap("Blood", 1, 2) then changed = true end
    if wrap("Body", 1, 2) then changed = true end

    if changed then
        logInfo("Wrapped BanditBasePlacements functions")
    end
    return changed
end

local function wrapBanditBaseGroupPlacements()
    if not BanditBaseGroupPlacements then
        return false
    end

    Patch._wrappedBaseGroupPlacements = Patch._wrappedBaseGroupPlacements or {}

    local function wrapArea(name, xIndex, yIndex, wIndex, hIndex, blockedReturn)
        if Patch._wrappedBaseGroupPlacements[name] then
            return false
        end
        local original = BanditBaseGroupPlacements[name]
        if type(original) ~= "function" then
            return false
        end

        BanditBaseGroupPlacements[name] = function(...)
            local args = { ... }
            local x = args[xIndex]
            local y = args[yIndex]
            local w = args[wIndex]
            local h = args[hIndex]
            if isAreaIntersectNoBanditZone(x, y, w, h) then
                logInfo("Blocked BanditBaseGroupPlacements." .. name .. " area")
                return blockedReturn
            end
            return original(...)
        end

        Patch._wrappedBaseGroupPlacements[name] = true
        return true
    end

    local changed = false
    if wrapArea("CheckSpace", 1, 2, 3, 4, false) then changed = true end
    if wrapArea("ClearSpace", 1, 2, 4, 5, nil) then changed = true end
    if wrapArea("Junk", 1, 2, 4, 5, nil) then changed = true end
    if wrapArea("Papers", 1, 2, 4, 5, nil) then changed = true end
    if wrapArea("Item", 2, 3, 5, 6, nil) then changed = true end
    if wrapArea("Blood", 1, 2, 4, 5, nil) then changed = true end

    if changed then
        logInfo("Wrapped BanditBaseGroupPlacements functions")
    end
    return changed
end

local function wrapBanditSpawnContext()
    local changed = false

    if BanditUtils and type(BanditUtils.GetCharacterID) == "function" and not Patch._wrappedBanditGetCharacterID then
        local originalGetCharacterID = BanditUtils.GetCharacterID
        BanditUtils.GetCharacterID = function(playerObj)
            setSpawnContextPlayer(playerObj)
            return originalGetCharacterID(playerObj)
        end
        Patch._wrappedBanditGetCharacterID = true
        changed = true
        logInfo("Wrapped BanditUtils.GetCharacterID")
    end

    if BanditCustom and type(BanditCustom.ClanGet) == "function" and not Patch._wrappedBanditClanGet then
        local originalClanGet = BanditCustom.ClanGet
        BanditCustom.ClanGet = function(cid)
            local clan = originalClanGet(cid)
            if not clan then
                return clan
            end

            local playerObj = consumeSpawnContextPlayer()
            if not playerObj or not playerObj.getX or not playerObj.getY then
                return clan
            end

            if not isInsideExpandedNoBanditZone(playerObj:getX(), playerObj:getY(), BASE_GUARD_EXPAND) then
                return clan
            end

            local clonedClan = cloneTable(clan)
            if type(clan.spawn) == "table" then
                clonedClan.spawn = cloneTable(clan.spawn)
                clonedClan.spawn.roadblock = false
                clonedClan.spawn.campers = false
                clonedClan.spawn.defenders = false
            end

            logInfo("Disabled bandit base patterns near no-bandit zone for clan " .. tostring(cid))
            return clonedClan
        end
        Patch._wrappedBanditClanGet = true
        changed = true
        logInfo("Wrapped BanditCustom.ClanGet")
    end

    return changed
end

local function wrapSpawnerFunction(name)
    if not BanditServer or not BanditServer.Spawner then
        return false
    end
    local spawner = BanditServer.Spawner
    local original = spawner[name]
    if type(original) ~= "function" then
        return false
    end

    Patch._wrappedSpawner = Patch._wrappedSpawner or {}
    if Patch._wrappedSpawner[name] then
        return true
    end

    Patch._wrappedSpawner[name] = true
    Patch._originalSpawner = Patch._originalSpawner or {}
    Patch._originalSpawner[name] = original

    spawner[name] = function(playerObj, args)
        local onlineID = "nil"
        if playerObj and playerObj.getOnlineID then
            onlineID = tostring(playerObj:getOnlineID())
        end

        local blocked, reason = shouldBlockSpawnerCall(name, playerObj, args)
        if blocked then
            logInfo("Blocked Spawner." .. name .. " (onlineID=" .. onlineID .. ", reason=" .. reason .. ")")
            return
        end

        local slotBlocked, slotReason, slotKey, slotState, dayKey = shouldBlockDeployAmbushSlot(name, playerObj, args)
        if slotBlocked then
            logInfo("Blocked Spawner." .. name .. " (onlineID=" .. onlineID .. ", reason=" .. slotReason .. ")")
            return
        end

        applyDeployAmbushBehaviorOverrides(args, slotState)

        local result = original(playerObj, args)

        if slotKey ~= nil then
            markDeployAmbushTriggered(slotKey, slotState, dayKey)
            logInfo("Consumed deploy ambush slot " .. tostring(slotKey)
                .. " (onlineID=" .. onlineID
                .. ", friendly=" .. tostring(isDeployAmbushFriendlyForState(slotState)) .. ").")
        end

        return result
    end

    logInfo("Wrapped BanditServer.Spawner." .. name)
    return true
end

ensureSpawnerPatch = function()
    local wrappedType = wrapSpawnerFunction("Type")
    local wrappedClan = wrapSpawnerFunction("Clan")
    local wrappedIndividual = wrapSpawnerFunction("Individual")
    local wrappedVehicle = wrapSpawnerFunction("Vehicle")
    local wrappedRestore = wrapSpawnerFunction("Restore")
    return wrappedType or wrappedClan or wrappedIndividual or wrappedVehicle or wrappedRestore
end

local function logSpawnerAvailability()
    local available = BanditServer
        and BanditServer.Spawner
        and type(BanditServer.Spawner.Clan) == "function"

    if available then
        if Patch._loggedSpawnerUnavailable then
            logInfo("BanditServer.Spawner.Clan is now available.")
        end
        Patch._loggedSpawnerUnavailable = false
        return
    end

    if not Patch._loggedSpawnerUnavailable then
        Patch._loggedSpawnerUnavailable = true
        logInfo("BanditServer.Spawner.Clan unavailable; deploy ambush cannot spawn. Check Bandits2 in the server Mods list/load order.")
    end
end

local function updateDeployAmbushSchedule()
    local dayKey = getDeployAmbushDayKey()
    if dayKey == nil then
        return
    end

    local state = getDeployAmbushState()
    ensureDeployAmbushSchedule(state, dayKey)
end

local function updateBanditsZonePatch()
    updateDeployAmbushSchedule()
    wrapAddZombiesInOutfit()
    wrapAddVehicleDebug()
    wrapBanditBasePlacements()
    wrapBanditBaseGroupPlacements()
    wrapBanditSpawnContext()
    ensureSpawnerPatch()
    logSpawnerAvailability()
end

if Events and Events.OnServerStarted and type(Events.OnServerStarted.Add) == "function" then
    Events.OnServerStarted.Add(updateBanditsZonePatch)
end

if Events and Events.EveryOneMinute and type(Events.EveryOneMinute.Add) == "function" then
    Events.EveryOneMinute.Add(updateBanditsZonePatch)
elseif Events and Events.EveryTenMinutes and type(Events.EveryTenMinutes.Add) == "function" then
    Events.EveryTenMinutes.Add(updateBanditsZonePatch)
end

updateBanditsZonePatch()

local function onClientCommand(module, command, playerObj, args)
    if module ~= "EFZ" then
        return
    end
    if command == "RequestDeployAmbush" then
        handleRequestDeployAmbush(playerObj, args)
    elseif command == "ForceDeployAmbush" then
        handleForceDeployAmbush(playerObj, args)
    end
end

if Events and Events.OnClientCommand and type(Events.OnClientCommand.Add) == "function" then
    Events.OnClientCommand.Add(onClientCommand)
end

