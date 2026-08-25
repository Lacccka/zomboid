if not EFZ then
    EFZ = {}
end

EFZ.DeployZombieClear = EFZ.DeployZombieClear or {}
local ZombieClear = EFZ.DeployZombieClear

local function normalizeRadius(radius)
    radius = tonumber(radius) or 0
    if radius < 0 then
        radius = 0
    end
    return radius
end

local function normalizePoint(point)
    if not point or point.x == nil or point.y == nil then
        return nil
    end
    return {
        x = tonumber(point.x) or 0,
        y = tonumber(point.y) or 0,
        z = math.floor(tonumber(point.z) or 0),
    }
end

function ZombieClear.resolveRadius(radius)
    return normalizeRadius(radius)
end

function ZombieClear.normalizePoint(point)
    return normalizePoint(point)
end

function ZombieClear.clearAtPoint(point, radius)
    local clearRadius = normalizeRadius(radius)
    local center = normalizePoint(point)
    if clearRadius <= 0 or not center then
        return 0, 0
    end

    local cell = getCell and getCell() or nil
    if not cell or not cell.getGridSquare then
        return 0, 0
    end

    local cx = center.x
    local cy = center.y
    local cz = center.z

    local radiusSq = clearRadius * clearRadius
    local minX = math.floor(cx - clearRadius)
    local maxX = math.floor(cx + clearRadius)
    local minY = math.floor(cy - clearRadius)
    local maxY = math.floor(cy + clearRadius)

    local removed = 0
    local loadedSquares = 0

    for _x = minX, maxX do
        for _y = minY, maxY do
            local sq = cell:getGridSquare(_x, _y, cz)
            if sq and sq.getMovingObjects then
                loadedSquares = loadedSquares + 1
                local moving = sq:getMovingObjects()
                if moving then
                    for i = moving:size(), 1, -1 do
                        local obj = moving:get(i - 1)
                        if obj and instanceof(obj, "IsoZombie") and (not obj:isDead()) then
                            local dx = (obj.getX and obj:getX() or 0) - cx
                            local dy = (obj.getY and obj:getY() or 0) - cy
                            if (dx * dx + dy * dy) <= radiusSq then
                                if obj.removeFromWorld then
                                    obj:removeFromWorld()
                                end
                                if obj.removeFromSquare then
                                    obj:removeFromSquare()
                                end
                                removed = removed + 1
                            end
                        end
                    end
                end
            end
        end
    end

    return removed, loadedSquares
end

function ZombieClear.clearAroundPlayer(playerObj, radius)
    if not playerObj then
        return 0, 0
    end
    return ZombieClear.clearAtPoint({
        x = playerObj:getX(),
        y = playerObj:getY(),
        z = playerObj:getZ(),
    }, radius)
end
