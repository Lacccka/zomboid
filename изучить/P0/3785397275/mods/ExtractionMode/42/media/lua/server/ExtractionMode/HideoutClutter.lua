require "ExtractionMode/Config"
require "ExtractionMode/Util"

if isClient and isClient() then return end

ExtractionMode = ExtractionMode or {}
local Config = ExtractionMode.Config
local Util = ExtractionMode.Util
local Clutter = {}

local CLUTTER_VERSION = 1
local CANDLE_VERSION = 1
local RETRY_SECONDS = 5
local MINIMUM_LOADED_SQUARES = 10
local CANDLE_MAX_SPAWN_DISTANCE = 15
local EXCLUDED_ROOM_NAMES = {
    -- The authored one-tile shell around the usable hideout is a separate room.
    -- It exists to prevent light leaking through the exterior walls and should
    -- remain free of both decorative light sources and generated floor clutter.
    darkroom = true,
}

-- Weights favor obvious rubbish while still mixing in mundane abandoned items.
-- Every entry is a vanilla Build 42 full type with a visible world model.
local ITEMS = {
    { type = "Base.BeerCanEmpty", weight = 10 },
    { type = "Base.BeerEmpty", weight = 8 },
    { type = "Base.PopEmpty", weight = 9 },
    { type = "Base.Pop2Empty", weight = 9 },
    { type = "Base.Pop3Empty", weight = 9 },
    { type = "Base.TinCanEmpty", weight = 8 },
    { type = "Base.PlasticCup", weight = 5 },
    { type = "Base.FountainCup", weight = 5 },
    { type = "Base.MayonnaiseEmpty", weight = 3 },
    { type = "Base.RemouladeEmpty", weight = 2 },
    { type = "Base.EmptyJar", weight = 3 },
    { type = "Base.Newspaper", weight = 7 },
    { type = "Base.Magazine", weight = 5 },
    { type = "Base.SheetPaper2", weight = 5 },
    { type = "Base.PaperBag", weight = 3 },
    { type = "Base.Plasticbag", weight = 3 },
    { type = "Base.RippedSheetsDirty", weight = 3 },
    { type = "Base.RubberBand", weight = 2 },
    { type = "Base.Paperclip", weight = 2 },
    { type = "Base.Cork", weight = 2 },
}

local totalWeight = 0
for _, entry in ipairs(ITEMS) do totalWeight = totalWeight + entry.weight end

local lastAttemptSecond = -RETRY_SECONDS

local function randomItemType()
    local roll = ZombRand(totalWeight)
    for _, entry in ipairs(ITEMS) do
        if roll < entry.weight then return entry.type end
        roll = roll - entry.weight
    end
    return ITEMS[#ITEMS].type
end

local function spawnLaneSquare(square, hideout)
    return math.abs(square:getY() - math.floor(hideout.y)) <= 2
        and math.abs(square:getX() - math.floor(hideout.x)) <= 7
        and square:getZ() == math.floor(tonumber(hideout.z) or 0)
end

local function excludedRoom(room)
    if room == nil then return true end
    local name = nil
    pcall(function()
        local definition = room:getRoomDef()
        name = definition and definition:getName()
    end)
    name = tostring(name or ""):lower():gsub("%s+", "")
    return EXCLUDED_ROOM_NAMES[name] == true
end

local function eligibleSquare(square, building, hideout)
    if square == nil or square:getBuilding() ~= building then return false end
    if Config.isHideoutGarageProtectedSquare(square) then return false end
    local room = square:getRoom()
    if room == nil or excludedRoom(room) then return false end
    if not square:TreatAsSolidFloor() or square:HasStairs() then return false end
    if spawnLaneSquare(square, hideout) then return false end

    local worldObjects = square:getWorldObjects()
    if worldObjects and worldObjects:size() > 0 then return false end

    local free = false
    pcall(function() free = square:isFree(false) end)
    return free
end

local function loadedCandidates(hideout, distanceAnchor, maximumDistance)
    local cell = getCell and getCell()
    if cell == nil then return {} end

    local anchor = cell:getGridSquare(math.floor(hideout.x), math.floor(hideout.y),
        math.floor(tonumber(hideout.z) or 0))
    local building = anchor and anchor:getBuilding()
    if building == nil then return {} end

    local radius = math.max(4, math.floor(tonumber(hideout.radius) or 14))
    local minimumZ = math.floor(tonumber(hideout.z) or 0)
    local maximumZ = minimumZ
    pcall(function() maximumZ = math.max(minimumZ, tonumber(cell:getMaxZ()) or minimumZ) end)
    maximumZ = math.min(31, maximumZ)

    local result = {}
    for x = math.floor(hideout.x) - radius, math.floor(hideout.x) + radius do
        for y = math.floor(hideout.y) - radius, math.floor(hideout.y) + radius do
            for z = minimumZ, maximumZ do
                local square = cell:getGridSquare(x, y, z)
                local withinDistance = true
                if distanceAnchor and maximumDistance then
                    local dx = x - tonumber(distanceAnchor.x)
                    local dy = y - tonumber(distanceAnchor.y)
                    withinDistance = square ~= nil
                        and square:getZ() == math.floor(tonumber(distanceAnchor.z) or 0)
                        and dx * dx + dy * dy <= maximumDistance * maximumDistance
                end
                if withinDistance and eligibleSquare(square, building, hideout) then
                    result[#result + 1] = square
                end
            end
        end
    end
    return result
end

local function candleSpawnAnchor(hideout)
    for _, player in ipairs(Util.players()) do
        local square = player and player:getSquare()
        if square and math.floor(square:getZ()) == math.floor(tonumber(hideout.z) or 0)
            and Util.playerNear(player, hideout, math.max(4, tonumber(hideout.radius) or 14)) then
            return { x = player:getX(), y = player:getY(), z = player:getZ() }
        end
    end
    return { x = hideout.x, y = hideout.y, z = hideout.z }
end

local function shuffle(values)
    for index = #values, 2, -1 do
        local swapIndex = ZombRand(index) + 1
        values[index], values[swapIndex] = values[swapIndex], values[index]
    end
end

function Clutter.tryInitialize()
    local data = ModData.getOrCreate(Config.DATA_KEY)
    if (tonumber(data.hideoutClutterVersion) or 0) >= CLUTTER_VERSION then return true end

    local requested = math.max(0, math.min(100,
        math.floor(tonumber(Config.value("HideoutClutterCount")) or 30)))
    if requested == 0 then
        data.hideoutClutterVersion = CLUTTER_VERSION
        data.hideoutClutterSpawned = 0
        return true
    end

    local hideout = Config.hideout()
    local candidates = loadedCandidates(hideout)
    if #candidates < math.min(MINIMUM_LOADED_SQUARES, requested) then return false end
    shuffle(candidates)

    -- Use at most three items on a square, distributing each pass across all
    -- candidates before forming another small pile.
    local target = math.min(requested, #candidates * 3)
    local spawned = 0
    for index = 1, target do
        local square = candidates[((index - 1) % #candidates) + 1]
        local success, item = pcall(function()
            return square:AddWorldInventoryItem(randomItemType(),
                ZombRandFloat(0.12, 0.88), ZombRandFloat(0.12, 0.88), 0, false)
        end)
        if success and item then
            spawned = spawned + 1
            pcall(function()
                item:setWorldZRotation(ZombRandFloat(0, 360))
                local worldItem = item:getWorldItem()
                if worldItem then
                    -- Treat generated clutter like a player-placed item so the
                    -- sandbox world-item cleanup cannot erase the cleanup project.
                    worldItem:setIgnoreRemoveSandbox(true)
                    worldItem:transmitCompleteItemToClients()
                end
            end)
        end
    end

    if spawned == 0 then return false end
    data.hideoutClutterVersion = CLUTTER_VERSION
    data.hideoutClutterSpawned = spawned
    Util.log("Scattered " .. tostring(spawned) .. " removable clutter item(s) in the hideout")
    return true
end

function Clutter.tryInitializeCandles()
    local data = ModData.getOrCreate(Config.DATA_KEY)
    if (tonumber(data.hideoutCandleVersion) or 0) >= CANDLE_VERSION then return true end

    local requested = math.max(0, math.min(20,
        math.floor(tonumber(Config.value("HideoutCandleCount")) or 6)))
    local spawned = math.max(0, math.floor(tonumber(data.hideoutCandlesSpawned) or 0))
    if requested == 0 then
        data.hideoutCandleVersion = CANDLE_VERSION
        return true
    end

    local hideout = Config.hideout()
    -- Capture the player's initial position once so a chunk-loading retry cannot
    -- make the candle cluster follow someone who has already crossed the hideout.
    data.hideoutCandleSpawnAnchor = data.hideoutCandleSpawnAnchor or candleSpawnAnchor(hideout)
    local candidates = loadedCandidates(hideout, data.hideoutCandleSpawnAnchor, CANDLE_MAX_SPAWN_DISTANCE)
    if #candidates < requested - spawned then return false end
    shuffle(candidates)

    for index = 1, math.min(requested - spawned, #candidates) do
        local square = candidates[index]
        local success, candle = pcall(function()
            return square:AddWorldInventoryItem("Base.CandleLit",
                ZombRandFloat(0.25, 0.75), ZombRandFloat(0.25, 0.75), 0, false)
        end)
        if success and candle then
            candle:setUsedDelta(1.0)
            candle:setActivated(true)
            candle:getModData().ExtractionModeHideoutCandle = true
            local worldItem = candle:getWorldItem()
            if worldItem then
                worldItem:setIgnoreRemoveSandbox(true)
                worldItem:transmitCompleteItemToClients()
            end
            spawned = spawned + 1
            data.hideoutCandlesSpawned = spawned
        end
    end

    if spawned < requested then return false end
    data.hideoutCandleVersion = CANDLE_VERSION
    Util.log("Placed " .. tostring(spawned) .. " lit candle(s) in the hideout")
    return true
end

local function onTick()
    local nowSecond = math.floor(Util.nowMs() / 1000)
    if nowSecond - lastAttemptSecond < RETRY_SECONDS then return end
    lastAttemptSecond = nowSecond
    Clutter.tryInitialize()
    Clutter.tryInitializeCandles()
end

Events.OnTick.Add(onTick)

ExtractionMode.HideoutClutter = Clutter
return Clutter
