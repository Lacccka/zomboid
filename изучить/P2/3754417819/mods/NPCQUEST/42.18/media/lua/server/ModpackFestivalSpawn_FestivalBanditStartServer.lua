-- Start-area festival Party NPCs.
-- Spawns friendly Party clan Bandits around the player's festival start once per save.

if isClient() and not isServer() then
    return
end

ModpackFestivalStartBandits = ModpackFestivalStartBandits or {}

local MOD_ID = "ModpackFestivalSpawn"
local LOG_PREFIX = "[" .. MOD_ID .. "][StartBandits] "

local FESTIVAL_SPAWN_X = 13737
local FESTIVAL_SPAWN_Y = 1962
local FESTIVAL_SPAWN_Z = 0
local PARTY_CLAN_UUID = "42364b66-ab03-4c38-b374-5575a0c24868"

local START_ARM_RADIUS = 80
local NPC_COUNT = 2
local SPAWN_RADIUS_MIN = 6
local SPAWN_RADIUS_MAX = 46
local START_DELAY_TICKS = 120
local RETRY_INTERVAL = 30
local MAX_ACTIVE_TICKS = 1800
local BATCH_SIZE = 15
local FEATURE_VERSION = 6

local tickCount = 0
local done = false
local banditsLoaded = false

local function getState()
    local md = ModData.getOrCreate(MOD_ID)
    md.startFestivalBandits = md.startFestivalBandits or {}
    return md.startFestivalBandits
end

local function resetOldStateIfNeeded(st)
    if st.version == FEATURE_VERSION then
        return
    end
    st.done = nil
    st.disabled = nil
    st.finishedReason = nil
    st.converted = nil
    st.spawned = nil
    st.version = FEATURE_VERSION
end

local function distSqXY(ax, ay, bx, by)
    local dx = ax - bx
    local dy = ay - by
    return dx * dx + dy * dy
end

local function playerNearFestivalSpawn(player)
    return distSqXY(player:getX(), player:getY(), FESTIVAL_SPAWN_X, FESTIVAL_SPAWN_Y)
        <= (START_ARM_RADIUS * START_ARM_RADIUS)
end

local function ensureStartLocation(player)
    local st = getState()
    resetOldStateIfNeeded(st)

    if st.done then
        done = true
        return nil
    end
    if st.disabled then
        done = true
        return nil
    end
    if st.x and st.y then
        return st
    end

    if not playerNearFestivalSpawn(player) then
        st.disabled = true
        print(LOG_PREFIX .. "not near festival start; Party NPC spawn disabled for this save")
        done = true
        return nil
    end

    st.x = player:getX()
    st.y = player:getY()
    st.z = player:getZ() or FESTIVAL_SPAWN_Z
    st.startedAtTick = tickCount
    st.spawned = 0
    print(LOG_PREFIX .. "armed at start location "
        .. math.floor(st.x) .. "," .. math.floor(st.y) .. "," .. tostring(st.z))
    return st
end

local function banditsReady()
    if not BanditServer or not BanditServer.Spawner or not BanditServer.Spawner.Clan then
        return false
    end
    if not banditsLoaded and BanditCustom and BanditCustom.Load then
        pcall(BanditCustom.Load)
        banditsLoaded = true
    end
    return true
end

local function isValidSpawnSquare(sq)
    if not sq then
        return false
    end
    if sq.getZombie and sq:getZombie() then
        return false
    end
    if sq.getVehicleContainer and sq:getVehicleContainer() then
        return false
    end
    if sq.isVehicleIntersecting and sq:isVehicleIntersecting() then
        return false
    end
    if sq.isFree and sq:isFree(false) then
        return true
    end
    return sq.isOutside and sq:isOutside() and not (sq.isSolid and sq:isSolid())
end

local function collectSpawnPoints(cell, st, wanted)
    local points = {}
    local seen = {}
    local zFloor = math.floor((st.z or FESTIVAL_SPAWN_Z) + 0.5)

    local function addSquare(sq)
        if not isValidSpawnSquare(sq) then
            return
        end
        local key = tostring(sq:getX()) .. "," .. tostring(sq:getY()) .. "," .. tostring(zFloor)
        if seen[key] then
            return
        end
        seen[key] = true
        points[#points + 1] = {
            x = sq:getX(),
            y = sq:getY(),
            z = st.z or zFloor,
        }
    end

    for ring = SPAWN_RADIUS_MIN, SPAWN_RADIUS_MAX, 4 do
        for deg = 0, 359, 18 do
            if #points >= wanted then
                return points
            end
            local angle = deg * (math.pi / 180)
            local x = math.floor(st.x + math.cos(angle) * ring + 0.5)
            local y = math.floor(st.y + math.sin(angle) * ring + 0.5)
            addSquare(cell:getGridSquare(x, y, zFloor))
        end
    end

    for _ = 1, wanted * 8 do
        if #points >= wanted then
            return points
        end
        local angle = ZombRand(360) * (math.pi / 180)
        local dist = SPAWN_RADIUS_MIN + ZombRand(math.max(1, SPAWN_RADIUS_MAX - SPAWN_RADIUS_MIN))
        local x = math.floor(st.x + math.cos(angle) * dist + 0.5)
        local y = math.floor(st.y + math.sin(angle) * dist + 0.5)
        addSquare(cell:getGridSquare(x, y, zFloor))
    end

    return points
end

local function spawnBatch(player, batch)
    local args = {
        cid = PARTY_CLAN_UUID,
        program = "Defend",
        size = #batch,
        hostileP = false,
        spawnPoints = batch,
    }
    BanditServer.Spawner.Clan(player, args)
end

local function isPartyBandit(zombie)
    if not zombie or not zombie.getVariableBoolean or not zombie:getVariableBoolean("Bandit") then
        return false
    end
    local md = zombie.getModData and zombie:getModData() or nil
    local brain = md and md.brain
    return brain and brain.cid == PARTY_CLAN_UUID
end

local function tagNearbyPartyBandits(player, st)
    local cell = player:getCell()
    if not cell or not cell.getZombieList then
        return 0
    end
    local zombies = cell:getZombieList()
    if not zombies or not zombies.size then
        return 0
    end

    local tagged = 0
    local radiusSq = (SPAWN_RADIUS_MAX + 10) * (SPAWN_RADIUS_MAX + 10)
    for i = 0, zombies:size() - 1 do
        local zombie = zombies:get(i)
        if isPartyBandit(zombie)
            and math.abs((zombie:getZ() or 0) - (st.z or 0)) <= 0.75
            and distSqXY(zombie:getX(), zombie:getY(), st.x, st.y) <= radiusSq then
            local md = zombie:getModData()
            md.modpackFestivalStartBandit = true
            md.modpackFestivalDialoguePhase = "concert"
            if zombie.transmitModData then
                zombie:transmitModData()
            end
            tagged = tagged + 1
        end
    end
    return tagged
end

local function spawnPartyNpcs(player, st)
    local cell = player:getCell()
    if not cell then
        return false
    end

    local points = collectSpawnPoints(cell, st, NPC_COUNT)
    if #points == 0 then
        print(LOG_PREFIX .. "no valid Party NPC spawn tiles yet")
        return false
    end

    local spawned = 0
    local i = 1
    while i <= #points and spawned < NPC_COUNT do
        local batch = {}
        for _ = 1, BATCH_SIZE do
            if i > #points or spawned + #batch >= NPC_COUNT then
                break
            end
            batch[#batch + 1] = points[i]
            i = i + 1
        end
        if #batch > 0 then
            spawnBatch(player, batch)
            spawned = spawned + #batch
        end
    end

    st.done = true
    st.spawned = spawned
    st.finishedReason = "spawned"
    done = true
    print(LOG_PREFIX .. "spawned ~" .. tostring(spawned)
        .. " Party NPCs around festival start")
    return spawned > 0
end

local function onTick()
    if done then
        return
    end
    if ModpackFestivalFeatures and ModpackFestivalFeatures.isStartFestivalBanditsEnabled
        and not ModpackFestivalFeatures.isStartFestivalBanditsEnabled() then
        done = true
        return
    end

    tickCount = tickCount + 1
    if tickCount < START_DELAY_TICKS then
        return
    end
    if not ModpackFestivalTick.every(tickCount, RETRY_INTERVAL) then
        return
    end

    local player = getSpecificPlayer and getSpecificPlayer(0)
    if not player or not player.getSquare or not player:getSquare() then
        return
    end

    local st = ensureStartLocation(player)
    if not st then
        return
    end
    if not banditsReady() then
        return
    end

    local elapsed = tickCount - (st.startedAtTick or tickCount)
    if elapsed > MAX_ACTIVE_TICKS then
        st.done = true
        st.finishedReason = "startup window expired"
        done = true
        print(LOG_PREFIX .. "gave up waiting for spawn tiles")
        return
    end

    if spawnPartyNpcs(player, st) then
        -- Bandits are usually available immediately in SP, but tag on the next few
        -- ticks too in case the spawn finalizes asynchronously.
        tagNearbyPartyBandits(player, st)
    end
end

local tagTick = 0
local function onTagTick()
    local st = getState()
    if not st.done or not st.spawned or st.tagDone or (st.tagged or 0) >= st.spawned then
        return
    end
    tagTick = tagTick + 1
    if not ModpackFestivalTick.every(tagTick, ModpackFestivalTick.UI) then
        return
    end
    local player = getSpecificPlayer and getSpecificPlayer(0)
    if not player then
        return
    end
    st.tagged = tagNearbyPartyBandits(player, st)
    st.tagAttempts = (st.tagAttempts or 0) + 1
    if st.tagged >= st.spawned or st.tagAttempts >= 20 then
        st.tagDone = true
        print(LOG_PREFIX .. "tagged Party NPCs=" .. tostring(st.tagged or 0)
            .. "/" .. tostring(st.spawned or 0))
    end
end

Events.OnTick.Add(onTick)
Events.OnTick.Add(onTagTick)
print(LOG_PREFIX .. "loaded: spawning Party NPCs around the festival start")
