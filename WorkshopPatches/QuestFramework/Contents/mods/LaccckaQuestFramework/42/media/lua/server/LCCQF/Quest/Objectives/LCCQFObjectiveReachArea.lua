LCCQF = LCCQF or {}
LCCQF.QuestObjectives = LCCQF.QuestObjectives or {}

local ReachArea = {}

local function numberOr(value, fallback)
    local n = tonumber(value)
    if n == nil then return fallback end
    return n
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
