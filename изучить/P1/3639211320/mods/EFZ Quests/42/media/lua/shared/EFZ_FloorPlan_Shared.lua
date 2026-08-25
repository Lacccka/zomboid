if not EFZ then
    EFZ = {}
end

EFZ.FloorPlan = EFZ.FloorPlan or {}
local FloorPlan = EFZ.FloorPlan

-- ============================================================
-- Persistent state (saved per-world) via ModData
-- ============================================================

FloorPlan.MODDATA_KEY = FloorPlan.MODDATA_KEY or "EFZ_FloorPlanState"

local function getStateTable()
    if ModData and ModData.getOrCreate then
        return ModData.getOrCreate(FloorPlan.MODDATA_KEY)
    end
    return nil
end

function FloorPlan.IsOpened(fullType)
    if not fullType then
        return false
    end
    local state = getStateTable()
    return state ~= nil and state[tostring(fullType)] == true
end

function FloorPlan.MarkOpened(fullType)
    if not fullType then
        return false
    end
    local state = getStateTable()
    if not state then
        return false
    end
    state[tostring(fullType)] = true
    return true
end

-- NOTE:
-- B42/MP 환경에선 서버에서 스프라이트/플래그 판별이 안 되는 경우가 있어서,
-- 서버가 브로드캐스트한 커맨드를 받은 "클라이언트들이 직접 벽 오브젝트를 찾아"
-- transmitRemoveItemFromSquare()로 제거 요청을 보내는 방식으로 동작하도록 한다.

local wallFlags = {
    W = { "WallW", "WindowW", "DoorW", "GarageDoorW", "cutW", "collideW", "HoppableW" },
    N = { "WallN", "WindowN", "DoorN", "GarageDoorN", "cutN", "collideN", "HoppableN" },
}

local function hasFlag(props, flagName)
    if not props or not flagName then
        return false
    end

    local isoFlag = IsoFlagType and IsoFlagType[flagName] or nil
    if isoFlag and props.Is and props:Is(isoFlag) then
        return true
    end

    if props.Is and props:Is(flagName) then
        return true
    end

    return false
end

local function removeSide(square, side)
    if not square or not side then
        return false
    end

    local function recalcSquare(sq)
        if not sq then
            return
        end
        if sq.RecalcAllWithNeighbours then
            pcall(sq.RecalcAllWithNeighbours, sq, true)
        end
        if sq.RecalcProperties then
            pcall(sq.RecalcProperties, sq)
        end
    end

    local function removeObjectFromSquare(sq, obj)
        if not sq or not obj then
            return false
        end
        if sq.transmitRemoveItemFromSquare then
            pcall(sq.transmitRemoveItemFromSquare, sq, obj)
            return true
        end
        if obj.RemoveFromSquare then
            pcall(obj.RemoveFromSquare, obj)
            return true
        end
        return false
    end

    -- Prefer engine getters when available (more robust across builds)
    local removedAny = false
    local isNorth = side == "N"
    local isWest = side == "W"
    if isNorth or isWest then
        local function tryGetter(methodName, ...)
            if not square[methodName] then
                return false
            end
            local ok, obj = pcall(square[methodName], square, ...)
            if ok and obj then
                if removeObjectFromSquare(square, obj) then
                    removedAny = true
                    return true
                end
            end
            return false
        end

        -- Common signatures: getWall(bool north), getDoor(bool north), getWindow(bool north)
        tryGetter("getWall", isNorth)
        tryGetter("getDoor", isNorth)
        tryGetter("getWindow", isNorth)

        -- Some builds expose directional getters
        if isNorth then
            tryGetter("getWallN")
            tryGetter("getDoorN")
            tryGetter("getWindowN")
        elseif isWest then
            tryGetter("getWallW")
            tryGetter("getDoorW")
            tryGetter("getWindowW")
        end

        if removedAny then
            recalcSquare(square)
        end
    end

    local flags = wallFlags[side]
    local objects = flags and square.getObjects and square:getObjects() or nil
    if not flags or not objects then
        return removedAny
    end

    for i = objects:size() - 1, 0, -1 do
        local obj = objects:get(i)
        local sprite = obj and obj.getSprite and obj:getSprite() or nil
        local props = sprite and sprite.getProperties and sprite:getProperties() or nil
        if props then
            for _, flagName in ipairs(flags) do
                if hasFlag(props, flagName) then
                    removeObjectFromSquare(square, obj)
                    removedAny = true
                    break
                end
            end
        end
    end

    if removedAny then
        recalcSquare(square)
    end

    return removedAny
end

-- Returns true when the target squares exist (loaded) and we've attempted removal.
-- Returns false when the cell/squares are missing (not loaded), or invalid coords.
local function removeWallBetween(x1, y1, z1, x2, y2, z2)
    local cell = getCell and getCell() or nil
    if not cell then
        return false
    end

    local squareA = cell:getGridSquare(x1, y1, z1 or 0)
    local squareB = cell:getGridSquare(x2, y2, z2 or 0)
    if not squareA or not squareB then
        return false
    end

    if x1 ~= x2 then
        -- Try both squares to be safe (object ownership can vary by build/map export)
        if x2 > x1 then
            removeSide(squareB, "W")
            removeSide(squareA, "W")
        else
            removeSide(squareA, "W")
            removeSide(squareB, "W")
        end
        return true
    elseif y1 ~= y2 then
        if y2 > y1 then
            removeSide(squareB, "N")
            removeSide(squareA, "N")
        else
            removeSide(squareA, "N")
            removeSide(squareB, "N")
        end
        return true
    end

    return false
end

function FloorPlan.OpenLower()
    return removeWallBetween(20396, 6, 1, 20397, 6, 1)
end

function FloorPlan.OpenUpper()
    return removeWallBetween(20396, 5, 1, 20397, 5, 1)
end

function FloorPlan.OpenByItem(item)
    if not item or not item.getFullType then
        return false
    end

    local ok, fullType = pcall(function()
        return item:getFullType()
    end)
    if not ok or not fullType then
        return false
    end

    if fullType == "EFZ.LivingSpaceFloorPlanLower" then
        return FloorPlan.OpenLower() == true
    end
    if fullType == "EFZ.LivingSpaceFloorPlanUpper" then
        return FloorPlan.OpenUpper() == true
    end

    return false
end

function FloorPlan.OpenByFullType(fullType)
    if not fullType then
        return false
    end
    local ft = tostring(fullType)
    if ft == "EFZ.LivingSpaceFloorPlanLower" then
        return FloorPlan.OpenLower() == true
    end
    if ft == "EFZ.LivingSpaceFloorPlanUpper" then
        return FloorPlan.OpenUpper() == true
    end
    return false
end


