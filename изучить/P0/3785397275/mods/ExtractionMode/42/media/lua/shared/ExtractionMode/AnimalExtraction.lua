require "ExtractionMode/Config"
require "ExtractionMode/Util"
require "ExtractionMode/CompanionDogsIntegration"

ExtractionMode = ExtractionMode or {}

local Config = ExtractionMode.Config
local Util = ExtractionMode.Util
local CompanionDogsIntegration = ExtractionMode.CompanionDogsIntegration
local AnimalExtraction = {}

local pending = {}
local claimedAnimals = {}
local DESTINATION_SEARCH_RADIUS = 20

local function livingTamedAnimal(animal)
    if animal == nil or CompanionDogsIntegration.usesTeleportRecovery(animal) then return false end
    local ok, result = pcall(function()
        return instanceof(animal, "IsoAnimal")
            and animal:isExistInTheWorld()
            and not animal:isDead()
            and (tonumber(animal:getHealth()) or 0) > 0
            and animal:isWild() == false
    end)
    return ok and result == true
end

-- A cross-cell move may temporarily remove an animal from the cell's active
-- world list while its source chunk unloads. The retained Java object still
-- carries its health, wild state, and full animal data and can be reattached at
-- the streamed hideout square.
local function retainedTamedAnimal(animal)
    if animal == nil or CompanionDogsIntegration.usesTeleportRecovery(animal) then return false end
    local ok, result = pcall(function()
        return instanceof(animal, "IsoAnimal")
            and not animal:isDead()
            and (tonumber(animal:getHealth()) or 0) > 0
            and animal:isWild() == false
    end)
    return ok and result == true
end

local function distanceSquared(animal, point)
    local dx = (tonumber(animal:getX()) or 0) - (tonumber(point.x) or 0)
    local dy = (tonumber(animal:getY()) or 0) - (tonumber(point.y) or 0)
    return dx * dx + dy * dy
end

function AnimalExtraction.queueNearbyTamedAnimals(point, radius)
    local cell = getCell and getCell()
    if cell == nil or point == nil then return 0 end
    local animals = cell:getAnimals()
    if animals == nil then return 0 end

    local maximumDistance = math.max(1, tonumber(radius)
        or Config.TAMED_ANIMAL_EXTRACTION_RADIUS or 50)
    local maximumDistanceSquared = maximumDistance * maximumDistance
    local pointZ = math.floor(tonumber(point.z) or 0)
    local queued = 0
    for index = 0, animals:size() - 1 do
        local animal = animals:get(index)
        if livingTamedAnimal(animal) and claimedAnimals[animal] ~= true
            and math.floor(tonumber(animal:getZ()) or 0) == pointZ
            and distanceSquared(animal, point) <= maximumDistanceSquared then
            claimedAnimals[animal] = true
            pending[#pending + 1] = { animal = animal }
            queued = queued + 1
        end
    end
    if queued > 0 then
        Util.log("Queued " .. tostring(queued)
            .. " nearby tamed animal(s) for hideout extraction")
    end
    return queued
end

local function fallbackAnimalSquare(square)
    if square == nil then return false end
    local ok, valid = pcall(function()
        return square:getFloor() ~= nil and square:TreatAsSolidFloor()
            and not square:isSolid() and not square:isSolidTrans()
            and not square:has(IsoFlagType.water)
    end)
    return ok and valid == true
end

local function squareKey(square)
    return tostring(square:getX()) .. ":" .. tostring(square:getY())
        .. ":" .. tostring(square:getZ())
end

local function visitRing(cell, anchorX, anchorY, anchorZ, radius, visitor)
    if radius == 0 then
        return visitor(cell:getGridSquare(anchorX, anchorY, anchorZ))
    end
    for x = anchorX - radius, anchorX + radius do
        local result = visitor(cell:getGridSquare(x, anchorY - radius, anchorZ))
        if result then return result end
        result = visitor(cell:getGridSquare(x, anchorY + radius, anchorZ))
        if result then return result end
    end
    for y = anchorY - radius + 1, anchorY + radius - 1 do
        local result = visitor(cell:getGridSquare(anchorX - radius, y, anchorZ))
        if result then return result end
        result = visitor(cell:getGridSquare(anchorX + radius, y, anchorZ))
        if result then return result end
    end
    return nil
end

local function targetSquare(cell, hideout, claimedSquares)
    local anchorX = math.floor(tonumber(hideout.x) or 0)
    local anchorY = math.floor(tonumber(hideout.y) or 0)
    local anchorZ = math.floor(tonumber(hideout.z) or 0)

    -- Livestock fares best outdoors. Search the streamed hideout surroundings
    -- first, then accept any solid-floor tile as a map-edit-safe fallback.
    for pass = 1, 2 do
        for radius = 0, DESTINATION_SEARCH_RADIUS do
            local square = visitRing(cell, anchorX, anchorY, anchorZ, radius, function(candidate)
                if candidate == nil then return nil end
                local key = squareKey(candidate)
                if claimedSquares[key] then return nil end
                local valid = pass == 1 and Util.isSafeOutdoorLandSquare(candidate)
                    or pass == 2 and fallbackAnimalSquare(candidate)
                if valid then
                    claimedSquares[key] = true
                    return candidate
                end
                return nil
            end)
            if square then return square end
        end
    end
    return nil
end

local function relocate(animal, square)
    local behavior = nil
    local ok = pcall(function()
        animal:stopAllMovementNow()
        behavior = animal:getBehavior()
        if behavior then behavior:setBlockMovement(true) end
        local x = square:getX() + 0.5
        local y = square:getY() + 0.5
        local z = square:getZ()
        animal:setX(x)
        animal:setY(y)
        animal:setNextX(x)
        animal:setNextY(y)
        animal:setZ(z)
        animal:setCurrent(square)
        if not animal:isExistInTheWorld() then animal:addToWorld() end
        if isServer and isServer() then animal:sendExtraUpdateToClients() end
    end)
    if behavior then pcall(function() behavior:setBlockMovement(false) end) end
    return ok
end

function AnimalExtraction.processPending()
    if #pending == 0 then return 0 end
    local cell = getCell and getCell()
    if cell == nil then return 0 end
    local hideout = Config.hideout()
    local anchor = cell:getGridSquare(math.floor(hideout.x), math.floor(hideout.y),
        math.floor(tonumber(hideout.z) or 0))
    if anchor == nil then return 0 end

    local remaining = {}
    local claimedSquares = {}
    local moved = 0
    for _, entry in ipairs(pending) do
        local animal = entry.animal
        if not retainedTamedAnimal(animal) then
            claimedAnimals[animal] = nil
        else
            local square = targetSquare(cell, hideout, claimedSquares)
            if square and relocate(animal, square) then
                claimedAnimals[animal] = nil
                moved = moved + 1
            else
                remaining[#remaining + 1] = entry
            end
        end
    end
    pending = remaining
    if moved > 0 then
        Util.log("Moved " .. tostring(moved) .. " tamed animal(s) to the hideout")
    end
    return moved
end

ExtractionMode.AnimalExtraction = AnimalExtraction
return AnimalExtraction
