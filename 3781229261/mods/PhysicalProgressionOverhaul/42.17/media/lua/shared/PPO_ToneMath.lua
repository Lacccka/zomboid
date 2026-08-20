require "PPO_Config"

PPO = PPO or {}
PPO.ToneMath = PPO.ToneMath or {}

local ToneMath = PPO.ToneMath

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

local function settingsOr(settings)
    if type(settings) == "table" then return settings end
    return PPO.Config.Tone
end

local STAGE_MEDIUM = 0.34
local STAGE_HIGH = 0.67

-- Load no longer enters tone quality at any point. It is expressed once, as the
-- Expressed-band gate in PPO.ToneEngine, so a player who trains hard enough to
-- earn a tone is not also punished for the load that earned it.
function ToneMath.quality(adaptation, execQuality)
    local form = clamp(finiteOr(adaptation, 0), 0, 1)
    local exec = clamp(finiteOr(execQuality, 0), 0, 1)
    return clamp(form * exec, 0, 1)
end

function ToneMath.stage(quality)
    local resolved = clamp(finiteOr(quality, 0), 0, 1)
    if resolved <= 0 then return 0 end
    if resolved >= STAGE_HIGH then return 3 end
    if resolved >= STAGE_MEDIUM then return 2 end
    return 1
end

function ToneMath.durationMinutes(quality, settings)
    local resolved = settingsOr(settings)
    return clamp(finiteOr(quality, 0), 0, 1)
        * math.max(0, finiteOr(resolved.MaxDurationHours, 24)) * 60
end

-- The Sandbox option keeps its documented 0..15 range and stays the base; the
-- course multiplies above it on purpose. A drug that could not exceed the
-- configured ceiling would not be a drug.
local function courseScaleOr(scale)
    if type(scale) ~= "number" then return 1 end
    local resolved = finiteOr(scale, 1)
    return clamp(resolved, 0, 2.5)
end

-- Quantized by stage so the number in the tooltip and the number in the
-- arithmetic are the same number.
local DEFAULT_CARRY_STAGES = { 2, 4, 6 }
local DEFAULT_ENDURANCE_STAGES = { 5, 10, 15 }

local function stageValue(list, stage, fallback)
    if type(list) ~= "table" then return fallback end
    local value = list[stage]
    if type(value) ~= "number" then return fallback end
    return math.max(0, value)
end

function ToneMath.effectStrength(quality, settings, courseScale)
    local resolved = settingsOr(settings)
    local stage = ToneMath.stage(quality)
    if stage <= 0 then return 0 end
    local percent = stageValue(resolved.EnduranceStages, stage,
        DEFAULT_ENDURANCE_STAGES[stage])
    return clamp(percent / 100, 0, 1) * courseScaleOr(courseScale)
end

function ToneMath.carryBonus(quality, settings, courseScale)
    local resolved = settingsOr(settings)
    local stage = ToneMath.stage(quality)
    if stage <= 0 then return 0 end
    local kilograms = stageValue(resolved.CarryStages, stage,
        DEFAULT_CARRY_STAGES[stage])
    return math.max(0,
        math.floor(kilograms * courseScaleOr(courseScale) + 0.5))
end

-- The carry stages are kilograms the player can carry, and the only seam that
-- survives a tick is `maxWeightBase`: `BodyDamage.UpdateStrength` recomputes
-- `maxWeight` from it every update as
-- `floor(maxWeightBase * getWeightMod()) - moodlePenalty`. `getWeightMod` is a
-- ladder of the Strength level -- 0.9 at level 1, 2.26 at 9, 2.5 at 10 -- so
-- kilograms written straight into the base arrive multiplied by it, and the
-- shipped six turned into fifteen on a strong character. Dividing here is what
-- makes the Sandbox option mean what it says.
--
-- A base point is indivisible, so at the top of the ladder one point is worth
-- two and a half carried kilograms and neighbouring stages can round onto the
-- same point. Nearest is still the honest rule: rounding up would hand the
-- strongest characters more than the option promises, which is the defect this
-- function exists to end.
function ToneMath.carryBaseDelta(kilograms, weightMod)
    local bonus = math.max(0, finiteOr(kilograms, 0))
    local ladder = finiteOr(weightMod, 1)
    if ladder <= 0 then ladder = 1 end
    return math.max(0, math.floor(bonus / ladder + 0.5))
end

-- Used only when a physical seam is unavailable, so tone is never a no-op.
function ToneMath.fallbackReadinessBonus(quality, settings)
    local resolved = settingsOr(settings)
    local ceiling = clamp(
        math.max(0, finiteOr(resolved.FallbackReadinessBonus, 0.05)), 0, 1)
    return clamp(finiteOr(quality, 0), 0, 1) * ceiling
end
