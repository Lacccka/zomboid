-- Spawns extra zombies inside a specific map cell when chunks stream in.

if isClient() then return end -- Ensure this runs only on the server Lua state

local TARGET_CELL_X = 67  -- Change this
local TARGET_CELL_Y = 1  -- Change this

local EXTRA_ZOMBIES_PER_CHUNK = 6  -- Tune carefully (too high = performance hit)
local Z_LEVEL = 0

-- Use nil outfit for "random outfit" behavior; you can also set a specific outfit string.
local OUTFIT = nil
local FEMALE_CHANCE = nil

-- Safety options: avoid spawning on water / blocked squares
local AVOID_WATER_TILES = true
local REQUIRE_FREE_SQUARE = true

local function getMajorBuild()
    local ver = getCore():getVersionNumber()
    local major = tonumber(string.match(ver, "^(%d+)"))
    return major or 41
end

local function getCellTileSize(major)
    -- B41: 300x300 tiles per cell, B42: 256x256
    if major >= 42 then return 256 end
    return 300
end

local function getChunkTileSize(major)
    -- B41 chunks are 10x10 tiles, B42 chunks are 8x8 tiles
    if major >= 42 then return 8 end
    return 10
end

local major = getMajorBuild()
local CELL_TILES = getCellTileSize(major)
local CHUNK_TILES = getChunkTileSize(major)
local CHUNKS_PER_CELL = math.floor(CELL_TILES / CHUNK_TILES)

-- Persistent record so each chunk is boosted once per save
local MODDATA_KEY = "CellZombiePopBoost_BoostedChunks"
local boosted = ModData.getOrCreate(MODDATA_KEY)

local function getSquareAt(x, y, z)
    if getSquare then return getSquare(x, y, z) end
    if getCell then
        local cell = getCell()
        if cell and cell.getGridSquare then
            return cell:getGridSquare(x, y, z)
        end
    end
    return nil
end

local function isWaterSquare(square)
    if not square then return false end

    if square.isWater then
        local ok, res = pcall(function() return square:isWater() end)
        if ok and res then return true end
    end

    if square.hasFlag and IsoFlagType and IsoFlagType.water then
        local ok, res = pcall(function() return square:hasFlag(IsoFlagType.water) end)
        if ok and res then return true end
    end

    if square.getProperties and IsoFlagType and IsoFlagType.water then
        local props = square:getProperties()
        if props and props.Is then
            local ok, res = pcall(function() return props:Is(IsoFlagType.water) end)
            if ok and res then return true end
        end
    end

    return false
end

local function isSquareSpawnable(square)
    if not square then return false end

    if AVOID_WATER_TILES and isWaterSquare(square) then
        return false
    end

    if REQUIRE_FREE_SQUARE and square.isFree then
        local ok, res = pcall(function() return square:isFree(false) end)
        if ok and (not res) then return false end
    end

    return true
end

local function addZombiesAtTile(x, y, z, total, outfit, femaleChance)
    if total <= 0 then return 0 end

    if addZombiesInOutfit then
        addZombiesInOutfit(x, y, z, total, outfit, femaleChance)
        return total
    end

    addZombiesInOutfitArea(x, y, x, y, z, total, outfit, femaleChance)
    return total
end

local function addZombiesAvoidingBadTiles(x1, y1, x2, y2, z, total, outfit, femaleChance)
    local candidates = {}

    for x = x1, x2 do
        for y = y1, y2 do
            local sq = getSquareAt(x, y, z)
            if isSquareSpawnable(sq) then
                candidates[#candidates + 1] = { x = x, y = y }
            end
        end
    end

    if #candidates == 0 then
        return 0
    end

    for i = 1, total do
        local c = candidates[ZombRand(#candidates) + 1]
        addZombiesAtTile(c.x, c.y, z, 1, outfit, femaleChance)
    end

    return total
end

local function boostChunkByChunkCoord(chunkWX, chunkWY)
    local cellX = math.floor(chunkWX / CHUNKS_PER_CELL)
    local cellY = math.floor(chunkWY / CHUNKS_PER_CELL)
    if cellX ~= TARGET_CELL_X or cellY ~= TARGET_CELL_Y then return end

    local key = tostring(chunkWX) .. "_" .. tostring(chunkWY)
    if boosted[key] then return end

    local x1 = chunkWX * CHUNK_TILES
    local y1 = chunkWY * CHUNK_TILES
    local x2 = x1 + (CHUNK_TILES - 1)
    local y2 = y1 + (CHUNK_TILES - 1)

    local spawned = 0
    if AVOID_WATER_TILES or REQUIRE_FREE_SQUARE then
        spawned = addZombiesAvoidingBadTiles(x1, y1, x2, y2, Z_LEVEL, EXTRA_ZOMBIES_PER_CHUNK, OUTFIT, FEMALE_CHANCE)
    else
        addZombiesInOutfitArea(x1, y1, x2, y2, Z_LEVEL, EXTRA_ZOMBIES_PER_CHUNK, OUTFIT, FEMALE_CHANCE)
        spawned = EXTRA_ZOMBIES_PER_CHUNK
    end

    if spawned <= 0 then return end

    boosted[key] = true
    ModData.transmit(MODDATA_KEY)
end

if major >= 42 then
    -- Build 42+: chunk event exists
    local function onLoadChunk(chunk)
        -- B42+: IsoChunk may expose world chunk coords as fields or getters depending on build.
        if not chunk then return end
        local chunkWX = chunk.wx
        local chunkWY = chunk.wy
        if chunkWX == nil and chunk.getX then chunkWX = chunk:getX() end
        if chunkWY == nil and chunk.getY then chunkWY = chunk:getY() end
        if chunkWX == nil and chunk.getWX then chunkWX = chunk:getWX() end
        if chunkWY == nil and chunk.getWY then chunkWY = chunk:getWY() end
        if chunkWX == nil or chunkWY == nil then return end
        boostChunkByChunkCoord(chunkWX, chunkWY)
    end
    Events.LoadChunk.Add(onLoadChunk)
else
    -- Build 41 fallback: LoadGridsquare fires per-tile, so boost a chunk
    -- the first time we see a suitable (non-water/free) square in it.
    local function onLoadGridsquare(square)
        if not square then return end
        -- Reduce duplicate calls from the same (x,y) across multiple Z levels.
        if square:getZ() ~= Z_LEVEL then return end
        local x = square:getX()
        local y = square:getY()

        local chunkWX = math.floor(x / CHUNK_TILES)
        local chunkWY = math.floor(y / CHUNK_TILES)

        -- Quick filter: only the target cell
        local cellX = math.floor(chunkWX / CHUNKS_PER_CELL)
        local cellY = math.floor(chunkWY / CHUNKS_PER_CELL)
        if cellX ~= TARGET_CELL_X or cellY ~= TARGET_CELL_Y then return end

        local key = tostring(chunkWX) .. "_" .. tostring(chunkWY)
        if boosted[key] then return end

        if not isSquareSpawnable(square) then return end

        boostChunkByChunkCoord(chunkWX, chunkWY)
    end
    Events.LoadGridsquare.Add(onLoadGridsquare)
end
