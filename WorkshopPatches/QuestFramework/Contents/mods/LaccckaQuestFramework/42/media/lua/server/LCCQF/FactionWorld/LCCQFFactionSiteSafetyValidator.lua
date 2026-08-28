require "LCCQF/LCCQFConstants"

LCCQF = LCCQF or {}

local Validator = LCCQF.FactionSiteSafetyValidator or {}

local function squareAt(point)
    if type(point) ~= "table" then return nil end
    local cell = getCell and getCell()
    if not cell then return nil end
    local square
    pcall(function()
        square = cell:getGridSquare(
            math.floor(tonumber(point.x) or 0),
            math.floor(tonumber(point.y) or 0),
            math.floor(tonumber(point.z) or 0)
        )
    end)
    return square
end

local function isSafeHouseSquare(square)
    if not square or not SafeHouse or not SafeHouse.isSafeHouse then return false end
    local result = false
    pcall(function() result = SafeHouse.isSafeHouse(square, nil, true) and true or false end)
    return result
end

local function distanceToBounds(x, y, bounds)
    if type(bounds) ~= "table" then return math.huge end
    local x1 = tonumber(bounds.x) or 0
    local y1 = tonumber(bounds.y) or 0
    local x2 = tonumber(bounds.x2) or (x1 + (tonumber(bounds.w) or 1) - 1)
    local y2 = tonumber(bounds.y2) or (y1 + (tonumber(bounds.h) or 1) - 1)
    local closestX = math.max(x1, math.min(x, x2))
    local closestY = math.max(y1, math.min(y, y2))
    local dx = x - closestX
    local dy = y - closestY
    return math.sqrt(dx * dx + dy * dy)
end

local function playersTooClose(candidate, minimumDistance)
    minimumDistance = math.max(0, tonumber(minimumDistance) or 0)
    if minimumDistance <= 0 then return false end

    local players
    pcall(function()
        if getOnlinePlayers then players = getOnlinePlayers() end
    end)
    if not players then return false end

    local size = 0
    pcall(function() size = players:size() end)
    if size <= 0 then return false end

    for i = 0, size - 1 do
        local player
        pcall(function() player = players:get(i) end)
        if player then
            local alive = true
            pcall(function() alive = not player:isDead() end)
            if alive then
                local px, py
                pcall(function()
                    px = player:getX()
                    py = player:getY()
                end)
                if px and py and distanceToBounds(px, py, candidate.bounds) < minimumDistance then
                    return true, player
                end
            end
        end
    end
    return false
end

local function resolveBuilding(candidate)
    local sampleSquare = squareAt(candidate.sample)
    if not sampleSquare then return nil, nil, "candidate not currently loaded" end

    local building
    pcall(function() building = sampleSquare:getBuilding() end)
    if not building then return nil, nil, "sample square no longer belongs to a building" end

    local buildingDef
    pcall(function() buildingDef = building:getDef() end)
    if not buildingDef then return nil, nil, "building definition unavailable" end

    local x, y, w, h
    pcall(function()
        x = math.floor(tonumber(buildingDef:getX()) or 0)
        y = math.floor(tonumber(buildingDef:getY()) or 0)
        w = math.floor(tonumber(buildingDef:getW()) or 0)
        h = math.floor(tonumber(buildingDef:getH()) or 0)
    end)
    local fingerprint = x and y and w and h
        and ("building:" .. tostring(x) .. ":" .. tostring(y) .. ":" .. tostring(w) .. ":" .. tostring(h))
        or nil
    if fingerprint ~= candidate.buildingFingerprint then
        return nil, nil, "building fingerprint changed"
    end
    return building, buildingDef
end

local function collectFreeRoomSquares(buildingDef, wantsIndoor, needed)
    local rooms
    pcall(function() rooms = buildingDef:getRooms() end)
    if not rooms then return {}, "room definitions unavailable" end

    local result = {}
    local size = 0
    pcall(function() size = rooms:size() end)
    if size <= 0 then return result end

    -- We intentionally inspect every room definition in the candidate building before
    -- accepting it. This makes SafeHouse overlap fail closed instead of stopping after
    -- the first few convenient spawn squares. No world object is changed by this scan.
    for i = 0, size - 1 do
        local roomDef
        pcall(function() roomDef = rooms:get(i) end)
        local square
        pcall(function() square = roomDef and roomDef:getFreeSquare() end)
        if square then
            if isSafeHouseSquare(square) then
                return nil, "candidate overlaps a player SafeHouse"
            end

            local outside = false
            pcall(function() outside = square:isOutside() and true or false end)
            if not wantsIndoor or not outside then
                if #result < needed then
                    local x, y, z
                    pcall(function()
                        x = square:getX()
                        y = square:getY()
                        z = square:getZ()
                    end)
                    result[#result + 1] = {
                        x = math.floor(tonumber(x) or 0),
                        y = math.floor(tonumber(y) or 0),
                        z = math.floor(tonumber(z) or 0),
                    }
                end
            end
        end
    end
    return result
end

function Validator.Validate(definition, candidate)
    if type(definition) ~= "table" or type(candidate) ~= "table" then
        return "REJECT", "invalid validation input"
    end
    local profile = definition.siteProfile or {}

    local tooClose = playersTooClose(candidate, profile.minDistanceFromPlayers)
    if tooClose then return "DEFER", "active player too close to candidate" end

    local building, buildingDef, resolveError = resolveBuilding(candidate)
    if not building then
        return "DEFER", resolveError or "candidate building unavailable"
    end

    if isSafeHouseSquare(squareAt(candidate.sample)) then
        return "REJECT", "candidate sample is inside a player SafeHouse"
    end

    local minimumRooms = math.max(1, math.floor(tonumber(profile.minRooms) or 1))
    local freeSquares, freeError = collectFreeRoomSquares(buildingDef, profile.wantsIndoor == true, minimumRooms)
    if not freeSquares then return "REJECT", freeError end
    if #freeSquares < minimumRooms then
        return "REJECT", "insufficient validated free room squares"
    end

    return "PASS", {
        validatedFreeSquares = freeSquares,
        freeSquareCount = #freeSquares,
        safeHouseOverlap = false,
        playerProximityClear = true,
    }
end

LCCQF.FactionSiteSafetyValidator = Validator
return Validator
