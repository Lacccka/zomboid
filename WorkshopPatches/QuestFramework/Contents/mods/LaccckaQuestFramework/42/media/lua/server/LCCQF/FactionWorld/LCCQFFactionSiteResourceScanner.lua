-- Server-authoritative, read-only inspection of a reserved faction building.
-- The scanner stores only plain coordinates/counts. It never keeps IsoGridSquare,
-- IsoObject, IsoRoom or IsoBuilding references in persistent state.
if isClient and isClient() and not (isServer and isServer()) then
    return {}
end

require "LCCQF/LCCQFConstants"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Scanner = LCCQF.FactionSiteResourceScanner or {}

local MAX_POINTS_PER_KIND = 16
local ROOM_PROBE_CAP = 1024
local CHAIR_PROPS = { "chairN", "chairS", "chairE", "chairW" }
local SEAT_WORDS = { "chair", "sofa", "couch", "armchair", "bench", "stool", "seat" }
local NOT_A_BED_NAMES = {
    "chair", "sofa", "couch", "armchair", "bench", "stool", "seat",
    "seating", "table", "toilet",
}
local READ_CONTAINERS = {
    shelves = true,
    shelvesmag = true,
    metal_shelves = true,
    desk = true,
}

local function log(message)
    print(C.LOG_PREFIX .. "[FACTION:SITE:SCAN] " .. tostring(message))
end

local function worldHours()
    if not getGameTime then return 0 end
    local gameTime = getGameTime()
    if not gameTime or not gameTime.getWorldAgeHours then return 0 end
    return tonumber(gameTime:getWorldAgeHours()) or 0
end

local function safeInstanceof(object, className)
    if not object or not instanceof then return false end
    local ok, value = pcall(function() return instanceof(object, className) end)
    return ok and value == true
end

local function buildingFingerprint(building)
    if not building then return nil end
    local def
    pcall(function() def = building:getDef() end)
    if not def then return nil end

    local x, y, w, h
    pcall(function()
        x = math.floor(tonumber(def:getX()) or 0)
        y = math.floor(tonumber(def:getY()) or 0)
        w = math.floor(tonumber(def:getW()) or 0)
        h = math.floor(tonumber(def:getH()) or 0)
    end)
    if not x or not y or not w or not h or w <= 0 or h <= 0 then return nil end
    return "building:" .. tostring(x) .. ":" .. tostring(y)
        .. ":" .. tostring(w) .. ":" .. tostring(h)
end

local function findLoadedBuilding(site)
    if type(site) ~= "table" or type(site.buildingFingerprint) ~= "string" then
        return nil, "site building fingerprint unavailable"
    end

    local cell = getCell and getCell()
    if not cell or not cell.getRoomList then return nil, "cell unavailable" end

    -- New reservations may carry a sample point in future schemas. Use it first,
    -- while retaining a loaded-room fallback for existing schema-v1 reservations.
    if type(site.sample) == "table" then
        local square
        pcall(function()
            square = cell:getGridSquare(
                math.floor(tonumber(site.sample.x) or 0),
                math.floor(tonumber(site.sample.y) or 0),
                math.floor(tonumber(site.sample.z) or 0)
            )
        end)
        if square then
            local building
            pcall(function() building = square:getBuilding() end)
            if building and buildingFingerprint(building) == site.buildingFingerprint then
                return building
            end
        end
    end

    local rooms
    pcall(function() rooms = cell:getRoomList() end)
    local size = rooms and rooms:size() or 0
    local maximum = math.min(size, ROOM_PROBE_CAP)
    for i = 0, maximum - 1 do
        local room = rooms:get(i)
        local building
        pcall(function() building = room and room:getBuilding() end)
        if building and buildingFingerprint(building) == site.buildingFingerprint then
            return building
        end
    end
    return nil, "reserved building is not currently loaded"
end

local function nameContains(text, words)
    if type(text) ~= "string" or text == "" then return false end
    text = string.lower(text)
    for _, word in ipairs(words) do
        if string.find(text, word, 1, true) then return true end
    end
    return false
end

local function propertiesOf(object)
    local props
    pcall(function()
        if object and object.getProperties then props = object:getProperties() end
    end)
    return props
end

local function isChair(object)
    local props = propertiesOf(object)
    if not props then return false end
    for _, propertyName in ipairs(CHAIR_PROPS) do
        local ok, value = pcall(function() return props:has(propertyName) end)
        if ok and value then return true end
    end
    return false
end

local function isSleepable(object)
    if not object or not object.getSprite or not object.getProperties then return false end
    local ok, value = pcall(function()
        local sprite = object:getSprite()
        local props = object:getProperties()
        return sprite ~= nil and props ~= nil and props:has(IsoFlagType.bed)
    end)
    return ok and value == true
end

local function isBed(object)
    if not isSleepable(object) or isChair(object) then return false end

    local customName
    pcall(function()
        local props = propertiesOf(object)
        if props and props.get then customName = props:get("CustomName") end
    end)
    if nameContains(customName, NOT_A_BED_NAMES) then return false end

    local spriteName
    pcall(function()
        local sprite = object:getSprite()
        if sprite then spriteName = sprite:getName() end
    end)
    return not nameContains(spriteName, SEAT_WORDS)
end

local function getContainer(object)
    local container
    pcall(function()
        if object and object.getContainer then container = object:getContainer() end
    end)
    return container
end

local function containerHasFood(container)
    if not container then return false end

    -- Preferred B42 path: recurse through nested inventory containers while keeping
    -- the scan bounded to containers that physically exist in this building.
    if ArrayList and ArrayList.new and container.getAllEvalRecurse then
        local ok, value = pcall(function()
            local found = ArrayList.new()
            container:getAllEvalRecurse(function(item)
                return item and item.IsFood and item:IsFood()
            end, found)
            return found:size() > 0
        end)
        if ok then return value == true end
    end

    -- Conservative fallback if the recursive helper changes: direct contents only.
    local ok, value = pcall(function()
        local items = container:getItems()
        if not items then return false end
        for i = 0, items:size() - 1 do
            local item = items:get(i)
            if item and item.IsFood and item:IsFood() then return true end
        end
        return false
    end)
    return ok and value == true
end

local function isWaterSource(object)
    if not object then return false end

    local probes = {
        function()
            return object.hasWater and object:hasWater() == true
        end,
        function()
            return object.getFluidAmount and (tonumber(object:getFluidAmount()) or 0) > 0
        end,
        function()
            return object.getWaterAmount and (tonumber(object:getWaterAmount()) or 0) > 0
        end,
        function()
            local props = propertiesOf(object)
            return object.getSprite and object:getSprite() ~= nil
                and props ~= nil and props:has(IsoFlagType.waterPiped)
        end,
    }
    for _, probe in ipairs(probes) do
        local ok, value = pcall(probe)
        if ok and value then return true end
    end
    return false
end

local function isBookshelf(container)
    if not container then return false end
    local containerType
    pcall(function() containerType = container:getType() end)
    return containerType ~= nil
        and READ_CONTAINERS[string.lower(tostring(containerType))] == true
end

local function classifySquare(square)
    local result = {
        beds = 0,
        chairs = 0,
        televisions = 0,
        storage = 0,
        food = 0,
        water = 0,
        stoves = 0,
        washers = 0,
        windows = 0,
        bookshelves = 0,
        objects = 0,
    }

    local ok = pcall(function()
        local objects = square:getObjects()
        if not objects then return end
        for i = 0, objects:size() - 1 do
            local object = objects:get(i)
            result.objects = result.objects + 1

            local chair = isChair(object)
            if chair then
                result.chairs = result.chairs + 1
            elseif isBed(object) then
                result.beds = result.beds + 1
            end

            if safeInstanceof(object, "IsoTelevision") then
                result.televisions = result.televisions + 1
            end
            if safeInstanceof(object, "IsoStove") or safeInstanceof(object, "IsoFireplace") then
                result.stoves = result.stoves + 1
            end
            if safeInstanceof(object, "IsoClothingWasher")
                or safeInstanceof(object, "IsoClothingDryer")
            then
                result.washers = result.washers + 1
            end
            if safeInstanceof(object, "IsoWindow") then
                result.windows = result.windows + 1
            end

            local container = getContainer(object)
            if container then
                result.storage = result.storage + 1
                if containerHasFood(container) then result.food = result.food + 1 end
                if isBookshelf(container) then result.bookshelves = result.bookshelves + 1 end
            end
            if isWaterSource(object) then result.water = result.water + 1 end
        end
    end)
    if not ok then return nil end
    return result
end

local function pushPoint(points, kind, x, y, z)
    local list = points[kind]
    if not list or #list >= MAX_POINTS_PER_KIND then return end
    list[#list + 1] = { x = x, y = y, z = z }
end

local function isSafeHouseSquare(square)
    if not square or not SafeHouse or not SafeHouse.isSafeHouse then return false end
    local ok, value = pcall(function() return SafeHouse.isSafeHouse(square, nil, true) end)
    return ok and value == true
end

local function isFreeIndoorSquare(square)
    if not square then return false end
    local ok, value = pcall(function()
        if square:isOutside() then return false end
        if not square:getRoom() then return false end
        if isSafeHouseSquare(square) then return false end
        return square:isFree(false) == true
    end)
    return ok and value == true
end

local function addCounts(total, part)
    for key, value in pairs(part) do
        total[key] = (tonumber(total[key]) or 0) + (tonumber(value) or 0)
    end
end

local function scanBuilding(site, building)
    local cell = getCell and getCell()
    if not cell or not cell.getRoomList then return nil, "cell unavailable" end

    local roomList
    pcall(function() roomList = cell:getRoomList() end)
    if not roomList then return nil, "loaded room list unavailable" end

    local budget = math.max(64, math.floor(tonumber(C.FACTION_SITE_RESOURCE_SCAN_MAX_TILES) or 4096))
    local counts = {
        beds = 0,
        chairs = 0,
        televisions = 0,
        storage = 0,
        food = 0,
        water = 0,
        stoves = 0,
        washers = 0,
        windows = 0,
        bookshelves = 0,
        freeSpawnPoints = 0,
        objects = 0,
    }
    local points = {
        beds = {},
        water = {},
        storage = {},
        food = {},
        spawn = {},
        windows = {},
    }
    local seenSquares = {}
    local visited = 0
    local safeHouseOverlap = false
    local budgetExhausted = false

    for roomIndex = 0, roomList:size() - 1 do
        local room = roomList:get(roomIndex)
        local roomBuilding
        pcall(function() roomBuilding = room and room:getBuilding() end)
        if roomBuilding == building or buildingFingerprint(roomBuilding) == site.buildingFingerprint then
            local squares
            pcall(function() squares = room:getSquares() end)
            local squareCount = squares and squares:size() or 0
            for squareIndex = 0, squareCount - 1 do
                if budget <= 0 then
                    budgetExhausted = true
                    break
                end
                local square = squares:get(squareIndex)
                if square then
                    local x, y, z
                    pcall(function()
                        x = square:getX()
                        y = square:getY()
                        z = square:getZ()
                    end)
                    local key = tostring(x) .. ":" .. tostring(y) .. ":" .. tostring(z)
                    if not seenSquares[key] then
                        seenSquares[key] = true
                        budget = budget - 1
                        visited = visited + 1

                        if isSafeHouseSquare(square) then safeHouseOverlap = true end
                        local classified = classifySquare(square)
                        if classified then
                            addCounts(counts, classified)
                            if classified.beds > 0 then pushPoint(points, "beds", x, y, z) end
                            if classified.water > 0 then pushPoint(points, "water", x, y, z) end
                            if classified.storage > 0 then pushPoint(points, "storage", x, y, z) end
                            if classified.food > 0 then pushPoint(points, "food", x, y, z) end
                            if classified.windows > 0 then pushPoint(points, "windows", x, y, z) end
                        end
                        if isFreeIndoorSquare(square) then
                            counts.freeSpawnPoints = counts.freeSpawnPoints + 1
                            pushPoint(points, "spawn", x, y, z)
                        end
                    end
                end
            end
        end
        if budgetExhausted then break end
    end

    if visited == 0 then return nil, "reserved building has no loaded room squares" end

    return {
        schemaVersion = 1,
        scannedWorldHours = worldHours(),
        tilesVisited = visited,
        tileBudget = math.max(64, math.floor(tonumber(C.FACTION_SITE_RESOURCE_SCAN_MAX_TILES) or 4096)),
        complete = not budgetExhausted,
        safeHouseOverlap = safeHouseOverlap,
        counts = counts,
        points = points,
    }
end

function Scanner.Scan(site)
    local building, errorText = findLoadedBuilding(site)
    if not building then return nil, errorText end

    local result, scanError = scanBuilding(site, building)
    if not result then return nil, scanError end

    log("siteId=" .. tostring(site.siteId)
        .. " factionId=" .. tostring(site.factionId)
        .. " tiles=" .. tostring(result.tilesVisited)
        .. " complete=" .. tostring(result.complete)
        .. " beds=" .. tostring(result.counts.beds)
        .. " water=" .. tostring(result.counts.water)
        .. " storage=" .. tostring(result.counts.storage)
        .. " food=" .. tostring(result.counts.food)
        .. " freeSpawn=" .. tostring(result.counts.freeSpawnPoints)
        .. " safeHouse=" .. tostring(result.safeHouseOverlap))
    return result
end

function Scanner.Evaluate(definition, result)
    if type(definition) ~= "table" or type(result) ~= "table" then
        return false, "invalid resource validation input"
    end
    local profile = definition.siteProfile or {}
    local counts = result.counts or {}

    if result.safeHouseOverlap then
        return false, "candidate overlaps a player SafeHouse"
    end
    if result.complete == false then
        return false, "resource scan exceeded tile budget"
    end
    if profile.wantsBeds == true and (tonumber(counts.beds) or 0) < 1 then
        return false, "site has no validated beds"
    end
    if profile.wantsWater == true and (tonumber(counts.water) or 0) < 1 then
        return false, "site has no validated water source"
    end

    local minimumStorage = math.max(0, math.floor(tonumber(profile.minStorageContainers) or 0))
    if (tonumber(counts.storage) or 0) < minimumStorage then
        return false, "site has insufficient storage containers"
    end

    local minimumSpawn = math.max(1, math.floor(tonumber(profile.minFreeSpawnPoints) or 1))
    if (tonumber(counts.freeSpawnPoints) or 0) < minimumSpawn then
        return false, "site has insufficient free indoor spawn points"
    end
    return true
end

LCCQF.FactionSiteResourceScanner = Scanner
return Scanner
