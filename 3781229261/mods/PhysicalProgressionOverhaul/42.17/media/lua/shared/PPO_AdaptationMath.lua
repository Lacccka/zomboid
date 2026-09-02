require "PPO_Num"
require "PPO_Config"

PPO = PPO or {}
PPO.AdaptationMath = PPO.AdaptationMath or {}

local AdaptationMath = PPO.AdaptationMath

-- Overtraining stages are stimulus thresholds, expressed directly in training
-- minutes at the shipped rate of 1/30 per game minute. They are deliberately
-- independent of the XP return curve, so the curve can be retuned later without
-- moving a single moodle.
local STAGE_FRESH_LIMIT = 1 / 3          -- 10 training minutes
local STAGE_WARMED_LIMIT = 4 / 3         -- 40 training minutes
-- 60 training minutes plus half a minute of tolerance, so a nominal sixty
-- minute session measured through getWorldAgeHours cannot fall past the line
-- and lose the tone it just earned.
local STAGE_WORKED_LIMIT = 2 + 0.5 / 30

local BASELINE_MINIMUM = 0.25
local BASELINE_PER_LEVEL = 0.05
local BASELINE_MAXIMUM = 0.75

local REGULARITY_FLOOR = 0.75
local REGULARITY_RANGE = 0.25
local REGULARITY_MAXIMUM = 100

local QUALITY_FLOOR = 0.50
-- Duration is measured in game minutes through getWorldAgeHours, which rounds:
-- a nominal ten-minute session routinely measures 9.998. Coverage is a
-- dimensionless 0..1 ratio, where half a unit of slack would forgive the whole
-- gate. One tolerance cannot serve both.
local DURATION_THRESHOLD_TOLERANCE = 0.5
local COVERAGE_THRESHOLD_TOLERANCE = 0.000000001
local GAIN_SCALE_MINIMUM = 0.25
local GAIN_SCALE_MAXIMUM = 4

local Num = PPO.Num

local function baseSessionCredit()
    local configured = PPO.Config ~= nil and PPO.Config.Adaptation ~= nil
        and PPO.Config.Adaptation.BaseSessionCredit or nil
    return math.max(0, Num.finite(configured, 0.30))
end

function AdaptationMath.initialAdaptation(level)
    local bounded = Num.clamp(Num.finite(level, 0), 0, 10)
    return Num.clamp(BASELINE_MINIMUM + BASELINE_PER_LEVEL * bounded,
        BASELINE_MINIMUM, BASELINE_MAXIMUM)
end

function AdaptationMath.reachedThreshold(amount, minimum, tolerance)
    local slack = math.max(0,
        Num.finite(tolerance, COVERAGE_THRESHOLD_TOLERANCE))
    return Num.finite(amount, 0) >= Num.finite(minimum, 0) - slack
end

-- A direction below its minimum threshold does not qualify at all; once it
-- qualifies it keeps at least the half-quality floor.
local function boundedQualifiedQuality(value, minimum, full, tolerance)
    local amount = math.max(0, Num.finite(value, 0))
    local minimumValue = math.max(0.001, Num.finite(minimum, 1))
    local fullValue = math.max(minimumValue, Num.finite(full, minimumValue))
    local slack = math.max(0,
        Num.finite(tolerance, COVERAGE_THRESHOLD_TOLERANCE))
    if amount < minimumValue - slack then return 0 end
    return Num.clamp(amount / fullValue, QUALITY_FLOOR, 1)
end

function AdaptationMath.durationQuality(minutes, minimum, full)
    return boundedQualifiedQuality(minutes, minimum, full,
        DURATION_THRESHOLD_TOLERANCE)
end

-- Coverage is the share of a session's minutes that actually had accepted work
-- in it. It is the same quantity charged as load, in different units, so it is
-- independent of DayLength by construction. A completed action covers all of its
-- own minutes, so in normal play it sits at 1.0 — measured live on 2026-07-28 at
-- three day lengths. This is an integrity gate, not a quality dial.
function AdaptationMath.coverageQuality(coveredMinutes, elapsedMinutes,
        minimum, full)
    local elapsed = math.max(0, Num.finite(elapsedMinutes, 0))
    if elapsed <= 0 then return 0 end

    local covered = math.max(0, Num.finite(coveredMinutes, 0))
    return boundedQualifiedQuality(
        Num.clamp(covered / elapsed, 0, 1), minimum, full,
        COVERAGE_THRESHOLD_TOLERANCE)
end

function AdaptationMath.regularityQuality(regularity)
    return REGULARITY_FLOOR + REGULARITY_RANGE
        * Num.clamp(Num.finite(regularity, 0) / REGULARITY_MAXIMUM, 0, 1)
end

function AdaptationMath.sessionQuality(duration, volume, regularity, load)
    return Num.clamp(Num.finite(duration, 0), 0, 1)
        * Num.clamp(Num.finite(volume, 0), 0, 1)
        * Num.clamp(Num.finite(regularity, REGULARITY_FLOOR), REGULARITY_FLOOR, 1)
        * Num.clamp(Num.finite(load, 0), 0, 1)
end

function AdaptationMath.creditEarned(baseCredit, quality, gainScale)
    return math.max(0, Num.finite(baseCredit, 0))
        * Num.clamp(Num.finite(quality, 0), 0, 1)
        * Num.clamp(Num.finite(gainScale, 1), GAIN_SCALE_MINIMUM, GAIN_SCALE_MAXIMUM)
end

-- Credit converts only during loaded online minutes. Readiness and recovery
-- support are bounded to [0,1] so a future provider can slow conversion but
-- never convert faster than the designed rate and never create credit.
function AdaptationMath.convert(adaptation, credit, elapsedMinutes,
        conversionHours, readiness, recoverySupport)
    local currentAdaptation = Num.clamp(Num.finite(adaptation, 0), 0, 1)
    local pool = math.max(0, Num.finite(credit, 0))
    local elapsed = math.max(0, Num.finite(elapsedMinutes, 0))
    local hours = math.max(0.001, Num.finite(conversionHours, 24))
    local boundedReadiness = Num.clamp(Num.finite(readiness, 1), 0, 1)
    local boundedSupport = Num.clamp(Num.finite(recoverySupport, 1), 0, 1)

    local ratePerMinute = baseSessionCredit() / (hours * 60)
    local converted = math.min(pool,
        ratePerMinute * boundedReadiness * boundedSupport * elapsed)

    return {
        adaptation = Num.clamp(
            currentAdaptation + converted * (1 - currentAdaptation), 0, 1),
        credit = math.max(0, pool - converted),
        converted = converted,
    }
end

function AdaptationMath.decay(adaptation, elapsedMinutes, decayDays)
    local currentAdaptation = Num.clamp(Num.finite(adaptation, 0), 0, 1)
    local elapsed = math.max(0, Num.finite(elapsedMinutes, 0))
    local days = math.max(0.001, Num.finite(decayDays, 30))
    return Num.clamp(currentAdaptation - elapsed / (days * 24 * 60), 0, 1)
end

function AdaptationMath.overtrainingStage(dailyStimulus)
    local load = math.max(0, Num.finite(dailyStimulus, 0))
    if load < STAGE_FRESH_LIMIT then return "Fresh" end
    if load < STAGE_WARMED_LIMIT then return "Warmed" end
    if load <= STAGE_WORKED_LIMIT then return "Worked" end
    return "Overtrained"
end
