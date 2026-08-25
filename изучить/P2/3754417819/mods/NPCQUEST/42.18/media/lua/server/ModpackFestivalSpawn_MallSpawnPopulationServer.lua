-- One-time mall-start population refill.
-- PlayerSpawnZombieRemoval clears the start bubble; refill once so Mall Spawn is not empty.

if isClient() and not isServer() then
    return
end

ModpackFestivalMallSpawnPopulation = ModpackFestivalMallSpawnPopulation or {}

local MOD_ID = "ModpackFestivalSpawn"
local LOG_PREFIX = "[" .. MOD_ID .. "][MallSpawnPopulation] "

local MALL_SPAWN_X = 13565
local MALL_SPAWN_Y = 1292
local MALL_SPAWN_Z = 0
local SISTER_SPAWN_X = 13595
local SISTER_SPAWN_Y = 1292
local MALL_START_RADIUS = 24
local SPAWN_DELAY_TICKS = 180
local RETRY_INTERVAL = 60
local MAX_ACTIVE_TICKS = 2400
local ZOMBIE_COUNT = 36
local SPAWN_RADIUS = 55
local LOAD_CHECK_RADIUS = 24
local INNER_CLEAR_RADIUS = 12
local POPULATION_THIN_RADIUS = 500
local POPULATION_THIN_PERCENT = 60
local FEATURE_VERSION = 3

local tickCount = 0
local done = false

local function getState()
    local md = ModData.getOrCreate(MOD_ID)
    md.mallSpawnPopulation = md.mallSpawnPopulation or {}
    return md.mallSpawnPopulation
end

local function resetOldStateIfNeeded(st)
    if st.version == FEATURE_VERSION then
        return
    end
    if st.done then
        st.version = FEATURE_VERSION
        return
    end
    st.done = nil
    st.disabled = nil
    st.spawned = nil
    st.finishedReason = nil
    st.startedAtTick = nil
    st.version = FEATURE_VERSION
end

local function removeZombie(zombie)
    pcall(function()
        if BanditBrain and BanditBrain.Remove and zombie.getVariableBoolean
            and zombie:getVariableBoolean("Bandit") then
            BanditBrain.Remove(zombie)
        end
    end)
    pcall(function()
        if zombie.removeFromWorld then
            zombie:removeFromWorld()
        end
    end)
    pcall(function()
        if zombie.removeFromSquare then
            zombie:removeFromSquare()
        end
    end)
end

local function isAlyssaSister(zombie)
    if ModpackFestivalSister and ModpackFestivalSister.isSisterBandit
        and ModpackFestivalSister.isSisterBandit(zombie) then
        return true
    end
    local md = zombie and zombie.getModData and zombie:getModData() or nil
    if not md or not ModpackFestivalSister then
        return false
    end
    return md.modpackFestivalSister == true
        or md.modpackFestivalSisterProtected == true
        or md.modpackFestivalSisterClanId == ModpackFestivalSister.CLAN_ID
        or md.modpackFestivalSisterBanditId == ModpackFestivalSister.BANDIT_ID
end

local function distSqXY(ax, ay, bx, by)
    local dx = ax - bx
    local dy = ay - by
    return dx * dx + dy * dy
end

local function playerAtMallSpawn(player)
    if not player then
        return false
    end
    if math.floor((player:getZ() or 0) + 0.5) ~= MALL_SPAWN_Z then
        return false
    end
    return distSqXY(player:getX(), player:getY(), MALL_SPAWN_X, MALL_SPAWN_Y)
        <= (MALL_START_RADIUS * MALL_START_RADIUS)
end

local function ensureArmed(player)
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
    if st.armed then
        return st
    end
    if not playerAtMallSpawn(player) then
        st.disabled = true
        st.finishedReason = "not mall spawn"
        done = true
        return nil
    end
    st.armed = true
    st.startedAtTick = tickCount
    print(LOG_PREFIX .. "armed at mall spawn")
    return st
end

local function hasLoadedArea(player)
    local cell = player and player.getCell and player:getCell()
    if not cell then
        return false
    end
    local checks = {
        { x = MALL_SPAWN_X - LOAD_CHECK_RADIUS, y = MALL_SPAWN_Y },
        { x = MALL_SPAWN_X + LOAD_CHECK_RADIUS, y = MALL_SPAWN_Y },
        { x = MALL_SPAWN_X, y = MALL_SPAWN_Y - LOAD_CHECK_RADIUS },
        { x = MALL_SPAWN_X, y = MALL_SPAWN_Y + LOAD_CHECK_RADIUS },
        { x = MALL_SPAWN_X, y = MALL_SPAWN_Y },
    }
    for i = 1, #checks do
        local sq = cell:getGridSquare(checks[i].x, checks[i].y, MALL_SPAWN_Z)
        if not sq then
            return false
        end
    end
    return true
end

local function spawnMallZombies()
    if not addZombiesInOutfitArea then
        print(LOG_PREFIX .. "addZombiesInOutfitArea unavailable")
        return false
    end
    local x1 = MALL_SPAWN_X - SPAWN_RADIUS
    local y1 = MALL_SPAWN_Y - SPAWN_RADIUS
    local x2 = MALL_SPAWN_X + SPAWN_RADIUS
    local y2 = MALL_SPAWN_Y + SPAWN_RADIUS
    local ok, err = pcall(function()
        addZombiesInOutfitArea(x1, y1, x2, y2, MALL_SPAWN_Z, ZOMBIE_COUNT, nil, nil)
    end)
    if not ok then
        print(LOG_PREFIX .. "spawn failed: " .. tostring(err))
        return false
    end
    return true
end

local function clearImmediateSpawnBubble(player)
    local cell = player and player.getCell and player:getCell()
    if not cell or not cell.getZombieList then
        return
    end
    local zombies = cell:getZombieList()
    if not zombies or not zombies.size then
        return
    end
    local radiusSq = INNER_CLEAR_RADIUS * INNER_CLEAR_RADIUS
    for i = zombies:size() - 1, 0, -1 do
        local zombie = zombies:get(i)
        if zombie and zombie.getVariableBoolean and not zombie:getVariableBoolean("Bandit")
            and math.abs((zombie:getZ() or 0) - MALL_SPAWN_Z) <= 0.75
            and distSqXY(zombie:getX(), zombie:getY(), player:getX(), player:getY()) <= radiusSq then
            removeZombie(zombie)
        end
    end
end

local function thinLoadedPopulationNearSister(player)
    local cell = player and player.getCell and player:getCell()
    if not cell or not cell.getZombieList then
        return 0, 0
    end
    local zombies = cell:getZombieList()
    if not zombies or not zombies.size then
        return 0, 0
    end

    local candidates = {}
    local radiusSq = POPULATION_THIN_RADIUS * POPULATION_THIN_RADIUS
    for i = 0, zombies:size() - 1 do
        local zombie = zombies:get(i)
        if zombie and not (zombie.isDead and zombie:isDead())
            and distSqXY(zombie:getX(), zombie:getY(), SISTER_SPAWN_X, SISTER_SPAWN_Y) <= radiusSq
            and not isAlyssaSister(zombie) then
            table.insert(candidates, zombie)
        end
    end

    local targetRemove = math.floor(#candidates * POPULATION_THIN_PERCENT / 100)
    local removed = 0
    for _ = 1, targetRemove do
        if #candidates == 0 then
            break
        end
        local index = ZombRand and (ZombRand(#candidates) + 1) or 1
        local zombie = candidates[index]
        candidates[index] = candidates[#candidates]
        candidates[#candidates] = nil
        removeZombie(zombie)
        removed = removed + 1
    end
    return removed, #candidates + removed
end

local function onTick()
    if done then
        return
    end
    if ModpackFestivalFeatures and ModpackFestivalFeatures.isMallSpawnEnabled
        and not ModpackFestivalFeatures.isMallSpawnEnabled() then
        done = true
        return
    end
    tickCount = tickCount + 1
    if tickCount < SPAWN_DELAY_TICKS then
        return
    end
    if not ModpackFestivalTick.every(tickCount, RETRY_INTERVAL) then
        return
    end

    local player = getSpecificPlayer and getSpecificPlayer(0)
    if not player or not player.getSquare or not player:getSquare() then
        return
    end

    local st = ensureArmed(player)
    if not st then
        return
    end
    if tickCount - (st.startedAtTick or tickCount) > MAX_ACTIVE_TICKS then
        st.done = true
        st.finishedReason = "startup window expired"
        done = true
        print(LOG_PREFIX .. "gave up waiting for mall area")
        return
    end
    if not hasLoadedArea(player) then
        return
    end

    if spawnMallZombies() then
        clearImmediateSpawnBubble(player)
        local removed, considered = thinLoadedPopulationNearSister(player)
        st.done = true
        st.spawned = ZOMBIE_COUNT
        st.thinned = removed
        st.thinCandidates = considered
        st.finishedReason = "spawned"
        done = true
        print(LOG_PREFIX .. "spawned " .. tostring(ZOMBIE_COUNT)
            .. " normal zombies around Mall Spawn; thinned "
            .. tostring(removed) .. "/" .. tostring(considered)
            .. " loaded zombies/NPCs near Alyssa (target "
            .. tostring(POPULATION_THIN_PERCENT) .. "%)")
    end
end

Events.OnTick.Add(onTick)
print(LOG_PREFIX .. "loaded")
