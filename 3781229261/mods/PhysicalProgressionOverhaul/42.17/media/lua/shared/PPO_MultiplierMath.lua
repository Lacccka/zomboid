require "PPO_Num"
require "PPO_BonusMath"

PPO = PPO or {}
PPO.MultiplierMath = PPO.MultiplierMath or {}

local MultiplierMath = PPO.MultiplierMath

function MultiplierMath.clamp(value, minimum, maximum)
    if value < minimum then return minimum end
    if value > maximum then return maximum end
    return value
end

local FALLBACK = {
    AdaptationShare = 0.60, ProteinShare = 0.10, CreatineShare = 0.10,
    SleepShare = 0.10, FuelShare = 0.10, ToneFallbackCeiling = 0.05,
    CourseCapBonus = 0.444, WithdrawalCapPenalty = 0.444,
    LevelCaps = { 3, 5, 8, 12, 16 },
}

local function settings()
    if PPO.Config == nil then return FALLBACK end
    if PPO.Config.getMultiplierSettings == nil then
        return PPO.Config.Multiplier or FALLBACK
    end
    local ok, resolved = pcall(PPO.Config.getMultiplierSettings)
    if not ok or type(resolved) ~= "table" then
        return PPO.Config.Multiplier or FALLBACK
    end
    return resolved
end

function MultiplierMath.levelCap(level)
    if level >= 10 then return nil end

    local boundedLevel = MultiplierMath.clamp(level, 0, 9)
    local caps = settings().LevelCaps
    if type(caps) ~= "table" then caps = FALLBACK.LevelCaps end
    local step = caps[math.floor(boundedLevel / 2) + 1]
    if type(step) ~= "number" then return FALLBACK.LevelCaps[1] end
    return step
end

local Num = PPO.Num

-- When sleep is not required its share is neither granted for free nor lost:
-- the remaining shares are renormalized so the ceiling stays reachable.
function MultiplierMath.shares(sleepRequired)
    local resolved = settings()
    local shares = {
        adaptation = resolved.AdaptationShare,
        protein = resolved.ProteinShare,
        creatine = resolved.CreatineShare,
        sleep = resolved.SleepShare,
        fuel = resolved.FuelShare,
    }
    if sleepRequired ~= false then return shares end

    local remaining = shares.adaptation + shares.protein + shares.creatine
        + shares.fuel
    if remaining <= 0 then return shares end
    return {
        adaptation = shares.adaptation / remaining,
        protein = shares.protein / remaining,
        creatine = shares.creatine / remaining,
        sleep = 0,
        fuel = shares.fuel / remaining,
    }
end

function MultiplierMath.fill(inputs, sleepRequired)
    if type(inputs) ~= "table" then return 0 end
    local shares = MultiplierMath.shares(sleepRequired)
    local base = shares.adaptation * Num.unit(inputs.adaptation)
        + shares.protein * Num.unit(inputs.protein)
        + shares.creatine * Num.unit(inputs.creatine)
        + shares.sleep * Num.unit(inputs.sleepFactor)
        + shares.fuel * Num.unit(inputs.fuelFactor)

    local ceiling = settings().ToneFallbackCeiling
    local fallback = MultiplierMath.clamp(
        Num.unit(inputs.toneFallback), 0, ceiling)
    return MultiplierMath.clamp(base + fallback, 0, 1)
end

-- The course and its debt use the same coefficient, so stopping gives back
-- exactly what the course granted. `course` here is the felt course
-- (`course.active`), never the raw reservoir: accounting is instant, the effect
-- is not.
local function ceilingScale(resolved, direction)
    local scales = resolved.CourseCapScale
    if type(scales) ~= "table" then return 1 end
    local scale = scales[direction]
    if type(scale) ~= "number" then return 1 end
    -- The option tops out at 100% delivered against a shipped 40, so 2.5 is
    -- the highest scale the page can hand this function.
    return MultiplierMath.clamp(scale, 0, 2.5)
end

function MultiplierMath.effectiveCap(level, course, withdrawal, direction)
    local cap = MultiplierMath.levelCap(level)
    if cap == nil then return nil end

    local resolved = settings()
    local scale = ceilingScale(resolved, direction)
    local factor = 1 + resolved.CourseCapBonus * scale * Num.unit(course)
        - resolved.WithdrawalCapPenalty * scale * Num.unit(withdrawal)
    return cap * math.max(0, factor)
end

function MultiplierMath.multiplier(level, inputs, sleepRequired, loadFactor,
        course, withdrawal)
    local direction = nil
    if type(inputs) == "table" then direction = inputs.direction end
    local cap = MultiplierMath.effectiveCap(level, course, withdrawal,
        direction)
    if cap == nil then return nil end

    local load = Num.unit(loadFactor)
    if type(loadFactor) ~= "number" then load = 1 end
    local result = 1 + (cap - 1) * MultiplierMath.fill(inputs, sleepRequired)
        * load
    return math.max(1, result)
end

-- Follows the Sandbox override, exactly like the in-exercise bonus does through
-- PPO.ExerciseState.snapshot. Both XP surfaces must answer to the same switch:
-- a server that disabled decay was otherwise still losing its out-of-exercise
-- multiplier to load, which is the opposite of what the option promises.
function MultiplierMath.dailyReturn(stimulus)
    return PPO.BonusMath.bonusReturn(
        stimulus, PPO.BonusMath.isExerciseBonusDecayEnabled())
end
