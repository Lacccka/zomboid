PPO = PPO or {}
PPO.SupplementState = PPO.SupplementState or {}

local SupplementState = PPO.SupplementState

local function finiteOr(value, fallback)
    if type(value) ~= "number" or value ~= value
            or value == math.huge or value == -math.huge then
        return fallback
    end
    return value
end

local function clamp(value, minimum, maximum)
    if value < minimum then return minimum end
    if value > maximum then return maximum end
    return value
end

function SupplementState.normalize(value)
    return clamp(finiteOr(value, 0), 0, 1)
end

function SupplementState.add(reservoir, credit)
    local current = SupplementState.normalize(reservoir)
    local amount = finiteOr(credit, 0)
    if amount <= 0 then return current end
    return clamp(current + amount, 0, 1)
end

-- The decay rate is expressed as "one serving per window", so every reservoir
-- shares one formula and only its constants differ.
function SupplementState.advance(reservoir, elapsedMinutes, servingCredit,
        servingHours)
    local current = SupplementState.normalize(reservoir)
    local elapsed = math.max(0, finiteOr(elapsedMinutes, 0))
    local credit = math.max(0, finiteOr(servingCredit, 0))
    local hours = finiteOr(servingHours, 0)
    if elapsed <= 0 or credit <= 0 or hours <= 0 then return current end
    return math.max(0, current - elapsed * credit / (hours * 60))
end

-- Linear approach, one full range per window, never overshooting the target.
-- The felt course chases the reservoir with this: accounting is instant, the
-- effect is not, so a lone dose never reaches its nominal value.
function SupplementState.chase(current, target, elapsedMinutes, windowHours)
    local from = SupplementState.normalize(current)
    local to = SupplementState.normalize(target)
    local elapsed = math.max(0, finiteOr(elapsedMinutes, 0))
    local hours = finiteOr(windowHours, 0)
    if hours <= 0 then return to end
    if elapsed <= 0 then return from end
    local step = elapsed / (hours * 60)
    if to > from then return math.min(to, from + step) end
    if to < from then return math.max(to, from - step) end
    return from
end
