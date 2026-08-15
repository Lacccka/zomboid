PPO = PPO or {}
PPO.BonusMath = PPO.BonusMath or {}

local BonusMath = PPO.BonusMath

-- Three points. The curve reaches zero exactly at the overtraining line of
-- stimulus 2.0, which is sixty game training minutes at the shipped rate, so
-- stopping at the line forfeits nothing.
local RETURN_POINTS = {
    { stimulus = 0, value = 1.00 },
    { stimulus = 1, value = 0.55 },
    { stimulus = 2, value = 0.00 },
}

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

function BonusMath.bonusReturn(stimulus, decayEnabled)
    if decayEnabled == false then return 1 end

    local load = math.max(0, finiteOr(stimulus, 0))
    if load >= RETURN_POINTS[#RETURN_POINTS].stimulus then return 0 end

    for index = 1, #RETURN_POINTS - 1 do
        local left = RETURN_POINTS[index]
        local right = RETURN_POINTS[index + 1]
        if load <= right.stimulus then
            local share = (load - left.stimulus)
                / (right.stimulus - left.stimulus)
            return left.value + ((right.value - left.value) * share)
        end
    end
    return 0
end

function BonusMath.effectiveMultiplier(fullMultiplier, bonusReturn)
    local full = math.max(1, finiteOr(fullMultiplier, 1))
    local returned = clamp(finiteOr(bonusReturn, 0), 0, 1)
    return 1 + ((full - 1) * returned)
end

-- `AddXP` scales Strength XP by a nutrition factor of its own before it reads
-- anything the mod owns, and it scales PPO's own payment by the same factor.
-- Left alone that factor multiplies the drawn multiplier instead of living
-- inside it, so the same character trains at two different rates depending on
-- what is in the stomach and the panel says nothing about it. Absorbing divides
-- the mod's multiplier by the factor: vanilla and PPO together then land on the
-- drawn number, and Strength counts the way Fitness already does.
--
-- The floor at the vanilla multiplier is deliberate. Below it, absorption would
-- have to take away XP vanilla already granted, so the protein bonus becomes a
-- floor the model cannot reclaim. That corner is a nearly spent direction and
-- it is bounded; subtracting earned XP is not.
function BonusMath.absorbed(effectiveMultiplier, vanillaFactor)
    local multiplier = math.max(1, finiteOr(effectiveMultiplier, 1))
    local factor = finiteOr(vanillaFactor, 1)
    if factor <= 0 then factor = 1 end
    return math.max(1, multiplier / factor)
end

-- The award and the skill panel read one function, so a change to one can never
-- leave the other showing a number the player does not receive.
--
-- `vanillaFactor` is not optional. Kahlua does not clear a declared parameter
-- the caller left out: the slot keeps whatever the caller's stack had there, so
-- an omitted argument arrives as garbage rather than nil. Measured inside this
-- very function, called from `BonusAwarder.award` with four arguments. Every
-- call site therefore passes all five, and `absorbed` refuses anything that is
-- not a positive finite number so a leaked table or boolean cannot change an
-- award. A leaked number still would, which is why omitting is not allowed.
function BonusMath.rawBonus(rawVanillaXp, fullMultiplier, bonusReturn, capped,
        vanillaFactor)
    if capped then return 0 end

    local raw = math.max(0, finiteOr(rawVanillaXp, 0))
    local applied = BonusMath.absorbed(
        BonusMath.effectiveMultiplier(fullMultiplier, bonusReturn),
        vanillaFactor)
    return raw * (applied - 1)
end

function BonusMath.isExerciseBonusDecayEnabled()
    if SandboxVars == nil then return true end
    local modOptions = SandboxVars.PhysicalProgressionOverhaul
    if modOptions == nil or modOptions.ExerciseBonusDecay == nil then
        return true
    end
    return modOptions.ExerciseBonusDecay ~= false
end
