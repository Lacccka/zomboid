require "PPO_Num"
require "PPO_Config"

PPO = PPO or {}
PPO.CourseCost = PPO.CourseCost or {}

local CourseCost = PPO.CourseCost

local Num = PPO.Num

local function settings()
    return PPO.Config.resolve("getCourseEffectSettings", PPO.Config.CourseEffects)
end

local function depth(state, direction, field)
    if type(state) ~= "table" then return 0 end
    local component = state[direction]
    if type(component) ~= "table" then return 0 end
    local course = component.course
    if type(course) ~= "table" then return 0 end
    return Num.clamp(Num.finite(course[field], 0), 0, 1)
end

-- Systemic costs read the sum, profile costs and every benefit read one
-- direction. Summing is not a leak: §6.3 prices two courses as two costs, and
-- a shared cost is never a bonus to the other direction.
function CourseCost.depths(state)
    local aStrength = depth(state, "Strength", "active")
    local aFitness = depth(state, "Fitness", "active")
    local wStrength = depth(state, "Strength", "withdrawalActive")
    local wFitness = depth(state, "Fitness", "withdrawalActive")
    return {
        aStrength = aStrength,
        aFitness = aFitness,
        wStrength = wStrength,
        wFitness = wFitness,
        aSum = Num.clamp(aStrength + aFitness, 0, 1),
        wSum = Num.clamp(wStrength + wFitness, 0, 1),
    }
end

-- The larger of the course term and the debt term, never their sum. The two
-- overlap only during the descent and the debt term is always the larger by
-- design, so the transition is monotone and never dips.
local function staged(courseValue, withdrawalValue)
    if withdrawalValue > courseValue then return withdrawalValue end
    return courseValue
end

function CourseCost.writes(depths, elapsedMinutes)
    if type(depths) ~= "table" then return {} end
    local resolved = settings()
    local scale = math.max(0, Num.finite(resolved.SideEffectScale, 1))
    local elapsed = math.max(0, Num.finite(elapsedMinutes, 0))
    local hours = elapsed / 60
    local writes = {}

    local stress = staged(
        resolved.StressFloorCourse * depths.aSum,
        resolved.StressFloorWithdrawal * depths.wSum) * scale
    if stress > 0 then
        writes[#writes + 1] =
            { stat = "STRESS", shape = "floor", value = Num.clamp(stress, 0, 1) }
    end

    local unhappiness = staged(
        resolved.UnhappinessFloorCourse * depths.aStrength,
        resolved.UnhappinessFloorWithdrawal * depths.wStrength) * scale
    if unhappiness > 0 then
        writes[#writes + 1] = { stat = "UNHAPPINESS", shape = "floor",
            value = Num.clamp(unhappiness, 0, 100) }
    end

    local thirst = staged(
        resolved.ThirstPerHourCourse * depths.aFitness,
        resolved.ThirstPerHourWithdrawal * depths.wFitness) * scale * hours
    if thirst > 0 then
        writes[#writes + 1] =
            { stat = "THIRST", shape = "rate", value = thirst }
    end

    if depths.wFitness > 0 then
        local drop = resolved.EnduranceCeilingWithdrawal * depths.wFitness
            * scale
        writes[#writes + 1] = { stat = "ENDURANCE", shape = "ceiling",
            value = Num.clamp(1 - drop, 0, 1) }
    end

    return writes
end

-- How deep the debt makes the strain.
function CourseCost.strainPerPart(withdrawalStrength)
    local resolved = settings()
    local scale = math.max(0, Num.finite(resolved.SideEffectScale, 1))
    return math.max(0, resolved.StrainPerPartWithdrawal
        * Num.clamp(Num.finite(withdrawalStrength, 0), 0, 1) * scale)
end

-- The benefit and its mirror, both on the direction's own numbers.
--
-- The rail is a safety bound, not balance. It has to sit outside what the
-- ceiling option can produce at full depth in either direction: the option tops
-- out at 100% delivered against a shipped 40, so the highest scale is 2.5, the
-- crest is 1 + 1.11 and the raw trough is 1 - 1.11. The upper bound is set
-- above the crest so it cannot bind.
--
-- The lower bound is 0 rather than the mirror of the crest, and that is a
-- meaning, not a rounding: a tone the debt has taken away entirely gives
-- nothing, and a negative scale would flip the carry bonus into a penalty this
-- channel has never had.
function CourseCost.toneScale(active, withdrawal, direction)
    local resolved = settings()
    local scale = 1
    local scales = resolved.CourseCeilingScale
    if type(scales) == "table" and type(scales[direction]) == "number" then
        scale = Num.clamp(scales[direction], 0, 2.5)
    end
    return Num.clamp(
        1 + resolved.ToneCourseBonus * scale * Num.clamp(Num.finite(active, 0), 0, 1)
          - resolved.ToneWithdrawalPenalty * scale
            * Num.clamp(Num.finite(withdrawal, 0), 0, 1),
        0, 2.20)
end

function CourseCost.recoveryScale(active, withdrawal)
    local resolved = settings()
    return Num.clamp(
        1 - resolved.RecoveryCourseBonus * Num.clamp(Num.finite(active, 0), 0, 1)
          + resolved.RecoveryWithdrawalPenalty
            * Num.clamp(Num.finite(withdrawal, 0), 0, 1),
        0.50, 2.00)
end

return CourseCost
