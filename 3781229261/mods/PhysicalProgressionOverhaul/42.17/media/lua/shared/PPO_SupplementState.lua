require "PPO_Num"

PPO = PPO or {}
PPO.SupplementState = PPO.SupplementState or {}

local SupplementState = PPO.SupplementState

local Num = PPO.Num

function SupplementState.normalize(value)
    return Num.clamp(Num.finite(value, 0), 0, 1)
end

function SupplementState.add(reservoir, credit)
    local current = SupplementState.normalize(reservoir)
    local amount = Num.finite(credit, 0)
    if amount <= 0 then return current end
    return Num.clamp(current + amount, 0, 1)
end

-- The decay rate is expressed as "one serving per window", so every reservoir
-- shares one formula and only its constants differ.
function SupplementState.advance(reservoir, elapsedMinutes, servingCredit,
        servingHours)
    local current = SupplementState.normalize(reservoir)
    local elapsed = math.max(0, Num.finite(elapsedMinutes, 0))
    local credit = math.max(0, Num.finite(servingCredit, 0))
    local hours = Num.finite(servingHours, 0)
    if elapsed <= 0 or credit <= 0 or hours <= 0 then return current end
    return math.max(0, current - elapsed * credit / (hours * 60))
end

-- Linear approach, one full range per window, never overshooting the target.
-- The felt course chases the reservoir with this: accounting is instant, the
-- effect is not, so a lone dose never reaches its nominal value.
function SupplementState.chase(current, target, elapsedMinutes, windowHours)
    local from = SupplementState.normalize(current)
    local to = SupplementState.normalize(target)
    local elapsed = math.max(0, Num.finite(elapsedMinutes, 0))
    local hours = Num.finite(windowHours, 0)
    if hours <= 0 then return to end
    if elapsed <= 0 then return from end
    local step = elapsed / (hours * 60)
    if to > from then return math.min(to, from + step) end
    if to < from then return math.max(to, from - step) end
    return from
end
