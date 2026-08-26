LCCQF = LCCQF or {}
LCCQF.QuestObjectives = LCCQF.QuestObjectives or {}

local ReachArea = {}

local function numberOr(value, fallback)
    local n = tonumber(value)
    if n == nil then return fallback end
    return n
end

local function normalizeMarker(spec)
    local marker = type(spec.marker) == "table" and spec.marker or nil
    if not marker or marker.visible == false then return nil end

    local mode = tostring(marker.mode or "EXACT")
    if mode ~= "EXACT" and mode ~= "APPROXIMATE" and mode ~= "AREA" and mode ~= "HIDDEN" then
        mode = "EXACT"
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

function ReachArea.Create(spec, context)
    if type(spec) ~= "table" then return nil, "invalid ReachArea objective" end

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

    if x == nil or y == nil or z == nil then
        return nil, "ReachArea target unavailable"
    end

    local radius = math.max(0.5, math.min(20, numberOr(spec.radius, 2.0)))
    return {
        id = spec.id,
        type = "ReachArea",
        titleKey = spec.titleKey,
        state = "pending",
        x = x,
        y = y,
        z = z,
        radius = radius,
        marker = normalizeMarker(spec),
    }
end

function ReachArea.MakeMarkerView(objective)
    if not objective or objective.state ~= "active" or not objective.marker then return nil end

    local marker = objective.marker
    if marker.visible == false or marker.mode == "HIDDEN" then return nil end

    local x = marker.x
    local y = marker.y
    if marker.mode == "EXACT" or x == nil or y == nil then
        x = objective.x
        y = objective.y
    end

    return {
        visible = true,
        mode = marker.mode,
        x = x,
        y = y,
        z = objective.z,
        labelKey = marker.labelKey,
        showOnWorldMap = marker.showOnWorldMap,
        showOnMiniMap = marker.showOnMiniMap,
        minZoom = marker.minZoom,
        maxZoom = marker.maxZoom,
    }
end

function ReachArea.Evaluate(player, objective)
    if not player or not objective or objective.state ~= "active" then return false end
    if player.isDead and player:isDead() then return false end

    local px, py, pz = player:getX(), player:getY(), player:getZ()
    if math.abs(pz - objective.z) >= 0.5 then return false end

    local dx = px - objective.x
    local dy = py - objective.y
    return (dx * dx + dy * dy) <= (objective.radius * objective.radius)
end

LCCQF.QuestObjectives.ReachArea = ReachArea

return ReachArea
