require "LCCQF/Core/LCCQFNPCRuntime"

LCCQF = LCCQF or {}
LCCQF.QuestObjectives = LCCQF.QuestObjectives or {}

local ClearArea = {}

local function numberOr(value, fallback)
    local n = tonumber(value)
    if n == nil then return fallback end
    return n
end

local function normalizeMarker(spec)
    local marker = type(spec.marker) == "table" and spec.marker or nil
    if not marker or marker.visible == false then return nil end

    local mode = tostring(marker.mode or "AREA")
    if mode ~= "EXACT" and mode ~= "APPROXIMATE" and mode ~= "AREA" and mode ~= "HIDDEN" then
        mode = "AREA"
    end

    return {
        visible = marker.visible ~= false,
        mode = mode,
        labelKey = type(marker.labelKey) == "string" and marker.labelKey or spec.titleKey,
        showOnWorldMap = marker.showOnWorldMap ~= false,
        showOnMiniMap = marker.showOnMiniMap == true,
        x = tonumber(marker.x),
        y = tonumber(marker.y),
        minZoom = tonumber(marker.minZoom),
        maxZoom = tonumber(marker.maxZoom),
    }
end

function ClearArea.Create(spec, context)
    if type(spec) ~= "table" then return nil, "invalid ClearArea objective" end

    local target = type(spec.target) == "table" and spec.target or {}
    local x, y, z
    if target.kind == "giverOffset" then
        local giver = context and context.giverHandle or nil
        if not giver or giver.x == nil or giver.y == nil or giver.z == nil then
            return nil, "giver anchor unavailable"
        end
        x = giver.x + numberOr(target.dx, 0)
        y = giver.y + numberOr(target.dy, 0)
        z = giver.z + numberOr(target.dz, 0)
    else
        x = tonumber(target.x)
        y = tonumber(target.y)
        z = tonumber(target.z)
    end

    if x == nil or y == nil or z == nil then return nil, "ClearArea target unavailable" end

    return {
        id = spec.id,
        type = "ClearArea",
        titleKey = spec.titleKey,
        state = "pending",
        x = x,
        y = y,
        z = z,
        radius = math.max(1, math.min(30, numberOr(spec.radius, 6))),
        maxRemaining = math.max(0, math.min(20, math.floor(numberOr(spec.maxRemaining, 0)))),
        lastRemaining = nil,
        marker = normalizeMarker(spec),
    }
end

function ClearArea.ValidatePersisted(objective)
    return type(objective) == "table"
        and tonumber(objective.x) ~= nil
        and tonumber(objective.y) ~= nil
        and tonumber(objective.z) ~= nil
        and tonumber(objective.radius) ~= nil
        and tonumber(objective.maxRemaining) ~= nil
end

function ClearArea.MakeMarkerView(objective)
    if not objective or objective.state ~= "active" or not objective.marker then return nil end
    local marker = objective.marker
    if marker.visible == false or marker.mode == "HIDDEN" then return nil end

    local x = marker.x
    local y = marker.y
    if marker.mode == "EXACT" or marker.mode == "AREA" or x == nil or y == nil then
        x = objective.x
        y = objective.y
    end

    return {
        visible = true,
        mode = marker.mode,
        x = x,
        y = y,
        z = objective.z,
        radius = objective.radius,
        labelKey = marker.labelKey,
        showOnWorldMap = marker.showOnWorldMap,
        showOnMiniMap = marker.showOnMiniMap,
        minZoom = marker.minZoom,
        maxZoom = marker.maxZoom,
    }
end

local function countsAsHostileZombie(zombie)
    if not zombie or zombie:isDead() then return false end
    if zombie.isUseless and zombie:isUseless() then return false end
    if LCCQF.NPCRuntime and LCCQF.NPCRuntime.IsFrameworkEntity
        and LCCQF.NPCRuntime.IsFrameworkEntity(zombie)
    then
        return false
    end
    return true
end

function ClearArea.EvaluateTick(player, objective)
    if not player or not objective or objective.state ~= "active" or player:isDead() then
        return false, false
    end
    if math.abs(player:getZ() - objective.z) >= 0.5 then return false, false end

    local dx = player:getX() - objective.x
    local dy = player:getY() - objective.y
    local activationRadius = objective.radius + 10
    if dx * dx + dy * dy > activationRadius * activationRadius then return false, false end

    local cell = player:getCell()
    if not cell then return false, false end

    local remaining = 0
    local tileRadius = math.ceil(objective.radius)
    local radiusSq = objective.radius * objective.radius
    local z = math.floor(objective.z)

    for x = math.floor(objective.x) - tileRadius, math.floor(objective.x) + tileRadius do
        for y = math.floor(objective.y) - tileRadius, math.floor(objective.y) + tileRadius do
            local sx = (x + 0.5) - objective.x
            local sy = (y + 0.5) - objective.y
            if sx * sx + sy * sy <= radiusSq then
                local square = cell:getGridSquare(x, y, z)
                if square then
                    local movingObjects = square:getMovingObjects()
                    for i = 0, movingObjects:size() - 1 do
                        local object = movingObjects:get(i)
                        if object and instanceof(object, "IsoZombie") and countsAsHostileZombie(object) then
                            remaining = remaining + 1
                            if remaining > objective.maxRemaining then break end
                        end
                    end
                end
            end
            if remaining > objective.maxRemaining then break end
        end
        if remaining > objective.maxRemaining then break end
    end

    local changed = tonumber(objective.lastRemaining) ~= remaining
    objective.lastRemaining = remaining
    return remaining <= objective.maxRemaining, changed, "clear_area"
end

function ClearArea.MakeProgressView(objective)
    local complete = objective and tonumber(objective.lastRemaining) ~= nil
        and tonumber(objective.lastRemaining) <= tonumber(objective.maxRemaining or 0)
    return complete and 1 or 0, 1
end

LCCQF.QuestObjectives.ClearArea = ClearArea

return ClearArea
