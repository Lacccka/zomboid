-- The numeric guards every module needs before it trusts a value. They were
-- copied into twenty files verbatim, which is twenty places a fix would have to
-- land; they are arithmetic with no state and no dependency, so one module
-- serves all of them.
--
-- `finite` takes its fallback explicitly rather than defaulting to zero. Kahlua
-- leaves an unpassed parameter holding whatever the caller's stack had in that
-- slot, so a default here would be a leaked value rather than a default, and
-- every call site passes both arguments for the same reason.

PPO = PPO or {}
PPO.Num = PPO.Num or {}

local Num = PPO.Num

function Num.finite(value, fallback)
    if type(value) ~= "number" or value ~= value
            or value == math.huge or value == -math.huge then
        return fallback
    end
    return value
end

function Num.clamp(value, minimum, maximum)
    if value < minimum then return minimum end
    if value > maximum then return maximum end
    return value
end

function Num.positive(value, fallback)
    return math.max(0, Num.finite(value, fallback))
end

-- Three modules carried this predicate, two of them under a name of their own.
-- A non-finite value is not positive, which is what every caller meant.
function Num.isPositive(value)
    return Num.finite(value, 0) > 0
end

-- The bare question, for callers that want to refuse a value rather than
-- substitute one.
function Num.isFinite(value)
    return Num.finite(value, nil) ~= nil
end

-- Non-finite reads as zero rather than as a fallback the caller chose, because
-- every user of this shape is a share, a fill or a fraction: absent means none.
function Num.unit(value)
    return Num.clamp(Num.finite(value, 0), 0, 1)
end

-- The Sandbox resolver's shape: garbage falls back, a legal number is bounded.
-- Distinct from `unit` because the fallback is not necessarily inside the range
-- and must survive when the option was never written.
function Num.bounded(value, minimum, maximum, fallback)
    local resolved = Num.finite(value, nil)
    if resolved == nil then return fallback end
    return Num.clamp(resolved, minimum, maximum)
end

return Num
