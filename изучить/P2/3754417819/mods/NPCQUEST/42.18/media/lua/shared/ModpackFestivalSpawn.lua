-- Bare baseline bootstrap.
-- Only mod-owned spawn regions are exposed; world/NPC startup systems stay stripped.

ModpackFestivalTick = ModpackFestivalTick or {}

ModpackFestivalTick.PER_SEC = 60
ModpackFestivalTick.UI_FAST = 15
ModpackFestivalTick.UI = 30
ModpackFestivalTick.GAME = 60
ModpackFestivalTick.SLOW = 120
ModpackFestivalTick.MAINT = 300
ModpackFestivalTick.RARE = 600

function ModpackFestivalTick.every(counter, interval)
    interval = interval or ModpackFestivalTick.GAME or 60
    if interval < 1 then
        interval = 1
    end
    return counter % interval == 0
end

function ModpackFestivalTick.sec(seconds)
    return math.max(1, math.floor((seconds or 1) * ModpackFestivalTick.PER_SEC))
end

function ModpackFestivalTick.interval(name, fallback)
    local value = ModpackFestivalTick[name]
    if type(value) == "number" and value > 0 then
        return value
    end
    return fallback or ModpackFestivalTick.GAME or 60
end

function ModpackFestivalEvery(counter, interval)
    if ModpackFestivalTick and ModpackFestivalTick.every then
        return ModpackFestivalTick.every(counter, interval)
    end
    return counter % (interval or 60) == 0
end

local FESTIVAL_REGION = {
    name = "Festival Grounds",
    file = "media/maps/ModpackFestivalSpawn/spawnpoints.lua",
}

local MALL_REGION = {
    name = "Mall Spawn",
    file = "media/maps/ModpackFestivalSpawn/spawnpoints_mall.lua",
}

local function loadRegionPoints(file)
    if not SpawnRegionMgr or not SpawnRegionMgr.loadSpawnPointsFile then
        return nil
    end
    return SpawnRegionMgr.loadSpawnPointsFile(file, false)
end

local function buildSpawnRegion(region)
    local points = loadRegionPoints(region.file)
    if not points then
        return nil
    end
    return {
        name = region.name,
        file = region.file,
        points = points,
    }
end

local function getModpackSpawnRegions()
    local regions = {}
    local festival = buildSpawnRegion(FESTIVAL_REGION)
    if festival then
        table.insert(regions, festival)
    end
    local mall = buildSpawnRegion(MALL_REGION)
    if mall then
        table.insert(regions, mall)
    end
    return regions
end

local function patchSpawnRegionMgr()
    if not SpawnRegionMgr or type(SpawnRegionMgr.getSpawnRegionsAux) ~= "function" then
        return false
    end
    SpawnRegionMgr.getSpawnRegionsAux = function(...)
        return getModpackSpawnRegions()
    end
    SpawnRegionMgr._ModpackFestivalSpawnPatched = true
    local regions = getModpackSpawnRegions()
    print("[ModpackFestivalSpawn] spawn regions: modpack allowed only (" .. #regions .. ")")
    return #regions > 0
end

local function tryPatchSpawnRegionMgr()
    pcall(patchSpawnRegionMgr)
end

tryPatchSpawnRegionMgr()

if Events and Events.OnGameBoot then
    Events.OnGameBoot.Add(tryPatchSpawnRegionMgr)
end

print("[ModpackFestivalSpawn] bare baseline bootstrap loaded")
