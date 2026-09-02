require "PPO_Num"
require "PPO_Config"

PPO = PPO or {}
PPO.ReadinessMath = PPO.ReadinessMath or {}

local ReadinessMath = PPO.ReadinessMath

local Num = PPO.Num

local function settingsOr(settings)
    if type(settings) == "table" then return settings end
    return PPO.Config.Recovery
end

-- An unreadable stat ramps to zero, so a missing seam can only remove a
-- penalty and never invent one.
function ReadinessMath.ramp(value, from, to)
    local sample = Num.finite(value, nil)
    if sample == nil then return 0 end
    local low = Num.finite(from, 0)
    local high = Num.finite(to, 1)
    if high <= low then return 0 end
    return Num.clamp((sample - low) / (high - low), 0, 1)
end

-- Load always comes from the underlying recovery curve, never from the Sandbox
-- XP override, so overtraining still costs Readiness with decay disabled.
function ReadinessMath.loadFactor(loadReturn)
    local sample = Num.clamp(Num.finite(loadReturn, 1), 0, 1)
    return 0.50 + 0.50 * sample
end

function ReadinessMath.sleepFactor(fatigue, sleepRequired, settings)
    if sleepRequired ~= true then return 1 end
    local resolved = settingsOr(settings)
    return 1 - resolved.FatiguePenalty * ReadinessMath.ramp(
        fatigue, resolved.FatigueRampStart, resolved.FatigueRampEnd)
end

-- The multiplier's own answer to fatigue, deliberately not the same curve as
-- `sleepFactor`. Readiness multiplies into Adaptation conversion and must stay
-- survivable, so it floors at `1 - FatiguePenalty`; the share is one term of a
-- sum and can afford to reach zero, which is the only way a ten percent share
-- is worth defending. `RecoverySupport` keeps reading `sleepFactor`,
-- because it normalizes by `FatiguePenalty` and would misread this curve.
function ReadinessMath.sleepShare(fatigue, sleepRequired, settings)
    if sleepRequired ~= true then return 1 end
    local resolved = settingsOr(settings)
    return Num.clamp(1 - resolved.ShareFatiguePenalty * ReadinessMath.ramp(
        fatigue, resolved.FatigueRampStart, resolved.ShareFatigueRampEnd),
        0, 1)
end

function ReadinessMath.fuelFactor(hunger, thirst, settings)
    local resolved = settingsOr(settings)
    local worst = math.max(
        ReadinessMath.ramp(hunger, resolved.FuelRampStart,
            resolved.FuelRampEnd),
        ReadinessMath.ramp(thirst, resolved.FuelRampStart,
            resolved.FuelRampEnd))
    return 1 - resolved.FuelPenalty * worst
end

-- The multiplier's own answer to food and water, and the same split as sleep:
-- `fuelFactor` floors at `1 - FuelPenalty` because Readiness multiplies into
-- Adaptation conversion and must stay survivable, while a share is one term of
-- a sum and can afford to reach zero. This one is deliberately literal -- half
-- the share is food, half is water, each spent exactly as fast as its stat
-- empties -- so it takes no settings: there is nothing here to tune.
function ReadinessMath.fuelShare(hunger, thirst)
    local food = 1 - Num.clamp(Num.finite(hunger, 0), 0, 1)
    local water = 1 - Num.clamp(Num.finite(thirst, 0), 0, 1)
    return Num.clamp(0.5 * food + 0.5 * water, 0, 1)
end

function ReadinessMath.readiness(loadFactor, sleepFactor, fuelFactor, settings)
    local resolved = settingsOr(settings)
    local floor = Num.clamp(Num.finite(resolved.ReadinessFloor, 0.50), 0, 1)
    local product = Num.clamp(Num.finite(loadFactor, 1), 0, 1)
        * Num.clamp(Num.finite(sleepFactor, 1), 0, 1)
        * Num.clamp(Num.finite(fuelFactor, 1), 0, 1)
    return Num.clamp(floor + (1 - floor) * product, 0, 1)
end

-- The ceiling is exactly one: a provider may slow conversion but can never
-- convert credit faster than the designed rate.
-- A penalty of zero is a legal Sandbox setting, and the normalization below
-- divides by it. Nothing missing means nothing withheld, which is the same
-- answer the ramp gives at the top of its range.
local function normalized(value, penalty)
    local limit = Num.clamp(Num.finite(penalty, 0), 0, 1)
    if limit <= 0 then return 1 end
    return Num.clamp((Num.clamp(Num.finite(value, 1), 0, 1) - (1 - limit)) / limit, 0, 1)
end

function ReadinessMath.recoverySupport(sleepFactor, fuelFactor, settings)
    local resolved = settingsOr(settings)
    local floor = Num.clamp(Num.finite(resolved.RecoverySupportFloor, 0.60), 0, 1)
    return Num.clamp(
        floor + (1 - floor)
            * normalized(sleepFactor, resolved.FatiguePenalty)
            * normalized(fuelFactor, resolved.FuelPenalty),
        floor, 1)
end
