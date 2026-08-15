PPO = PPO or {}

PPO.Config = {
    Runtime = {
        Enabled = true,
        Debug = false,
        ExerciseBonusDecay = true,
    },
    -- Approved moderate Stage 2 Adaptation balance. Public Sandbox controls may
    -- override the first seven values; the rest stay internal until balance
    -- evidence justifies exposing them.
    Adaptation = {
        GainScale = 1.0,
        CreditCap = 0.60,
        ConversionHours = 24,
        GraceHours = 72,
        DecayDays = 30,
        -- Form is the only reservoir that empties on its own, and a server may
        -- decide it should not. Turned off, the two values above mean nothing
        -- and form holds wherever training left it.
        DecayEnabled = true,
        MinimumTrainingMinutes = 10,
        FullQualityTrainingMinutes = 20,
        MinimumDirectionCoverage = 0.50,
        FullQualityDirectionCoverage = 0.80,
        IncompleteSessionGapMinutes = 30,
        MultiplierRefreshEpsilon = 0.01,
        BaseSessionCredit = 0.30,
    },
    -- The daily training load and how fast it drains. One stimulus is thirty
    -- game training minutes, so the shipped 1.5 is forty-five minutes a day.
    -- The overtraining line is not here: it lives in PPO.AdaptationMath and
    -- does not move with this budget.
    Load = {
        RecoverableStimulus = 1.5,
        RecoveryHours = 24,
        MinutesPerStimulus = 30,
    },
    -- Approved moderate Stage 3 recovery balance. Only SleepInfluence is a
    -- public Sandbox control; the ramps and floors stay internal until balance
    -- evidence justifies exposing them.
    Recovery = {
        ReadinessFloor = 0.50,
        FatigueRampStart = 0.30,
        FatigueRampEnd = 0.90,
        FatiguePenalty = 0.30,
        -- The multiplier share answers to fatigue on its own curve. Readiness
        -- keeps the shallow one because it multiplies into Adaptation
        -- conversion and sleep is the cheapest resource in the game to
        -- restore; the share takes the full penalty because a term that can
        -- only ever cost thirty percent of its own weight is decorative.
        ShareFatigueRampEnd = 1.00,
        ShareFatiguePenalty = 1.00,
        FuelRampStart = 0.25,
        FuelRampEnd = 0.70,
        FuelPenalty = 0.20,
        RecoverySupportFloor = 0.60,
        SleepInfluence = "Auto",
    },
    Tone = {
        -- One value per tone stage, not a ceiling divided by three: the number
        -- the tooltip shows and the number the arithmetic uses are the same
        -- number, and a server may flatten or steepen the ladder freely.
        CarryStages = { 2, 4, 6 },
        EnduranceStages = { 5, 10, 15 },
        MaxDurationHours = 24,
        EnduranceRefundEnabled = true,
        EnduranceRecoveryPerMinute = 0.02,
        FallbackReadinessBonus = 0.05,
        MaxPlausibleEnduranceDrop = 0.25,
    },
    -- The price of a risky course and the profile benefit that justifies it.
    -- Every coefficient is set against a vanilla moodle threshold rather than
    -- chosen freely; see the 2026-08-06 course side effects spec for the
    -- reachable-depth arithmetic behind each one.
    CourseEffects = {
        StressFloorCourse = 0.35,
        StressFloorWithdrawal = 0.65,
        UnhappinessFloorCourse = 28,
        UnhappinessFloorWithdrawal = 50,
        -- Thirst is the one channel vanilla does not let PPO scale, because
        -- its accumulation is not interceptable from Lua. Stated absolutely
        -- against a reference of one full bar over sixteen waking game hours,
        -- which makes these two 40% and 70% of that reference.
        ThirstPerHourCourse = 0.025,
        ThirstPerHourWithdrawal = 0.044,
        -- The debt does two things to muscle strain: it deepens it and it slows
        -- its clearing. 10 per part at full depth keeps PPO's own contribution
        -- inside "Minor Muscle Strain" (5..19) and never reaches the 20 that
        -- makes it "Muscle Strain".
        StrainPerPartWithdrawal = 10,
        -- The debt puts sixty percent of vanilla's muscle-strain clearing
        -- back. Vanilla clears in BodyPart.DamageUpdate at a rate Lua cannot
        -- reproduce, so this is applied to the observed drop, not to a rate.
        StrainDecayBrake = 0.60,
        -- The same guard MaxPlausibleEnduranceDrop already applies to
        -- endurance: a player who clears strain deliberately through the health
        -- panel or an admin heal must not have most of it handed straight back.
        MaxPlausibleStrainDrop = 10,
        EnduranceCeilingWithdrawal = 0.30,
        -- Nominal, not delivered. The felt course chases a decaying
        -- reservoir and the two meet at 0.9009, so a coefficient priced
        -- at 0.444 is what actually hands the player 40%. This is the
        -- same reasoning and the same number as CourseCapBonus, and the
        -- two are pinned equal by a contract: the drug must widen the
        -- tone ceiling exactly as much as it widens the multiplier's.
        ToneCourseBonus = 0.444,
        ToneWithdrawalPenalty = 0.444,
        RecoveryCourseBonus = 0.40,
        RecoveryWithdrawalPenalty = 0.50,
        SideEffectScale = 1.0,
    },
    Nutrition = {
        SupportDurationHours = 24,
    },
    Supplements = {
        ServingHours = 24,
        CourseDoseHours = 48,
        OnsetHours = 24,
    },
    -- Every PPO container holds five servings, and a Class B tank holds one
    -- container: a container drunk in one sitting wastes nothing, while a
    -- hoarder with three of them still cannot hold the effect on permanently.
    --
    -- The pre-workout is the one exception, and it is priced rather than
    -- inherited. Its window does not scale vanilla's pending stiffness, it
    -- cancels it: `PhysicalEffects.cancelFutureStrain` zeroes the armed timer of
    -- every group within a game minute of the repetition that armed it, so a
    -- session trained inside the window produces no delayed stiffness at all.
    -- Vanilla caps what one group accumulates at 150 and caps nothing about how
    -- many sessions are cancelled, so tank size is the only bound there is. At
    -- five servings a single container buys 120 game hours -- five game days --
    -- with the training brake removed outright, against the 24 hours the
    -- refreshing model could ever reach. Two servings hold that at 48 and leave
    -- the brake in the game.
    --
    -- The thermogenic keeps the whole container because its effect bounds
    -- itself: the calorie ceiling is a constant the writer never digs past, so
    -- more window buys more time at the same depth, and a full tank is the
    -- ~0.9 kg the item was priced at.
    Windows = {
        ContainerServings = 5,
        StimulantServings = 2,
    },
    -- The pre-workout's window and the one number that says how much faster it
    -- clears muscle strain. Only the window is a public Sandbox control: the
    -- strength is a constant by design, because a scalable strength is how a
    -- non-stacking effect turns into a stacking one.
    Stimulant = {
        WindowHours = 24,
        StrainAcceleration = 0.50,
    },
    -- The thermogenic's window and the three numbers that describe the hole it
    -- digs in the calorie stock. Only the window is a public Sandbox control:
    -- duration now accumulates, and a scalable depth on top of an accumulating
    -- duration is how a course becomes a permanent mode.
    Thermogenic = {
        WindowHours = 12,
        CalorieCeiling = -1250,
        DescentPerHour = 700,
        HeatFloor = 38.0,
    },
    Multiplier = {
        -- The ladder, by level bracket: 0-1, 2-3, 4-5, 6-7, 8-9. Level 10 owns
        -- no multiplier and has no entry.
        LevelCaps = { 3, 5, 8, 12, 16 },
        AdaptationShare = 0.60,
        ProteinShare = 0.10,
        CreatineShare = 0.10,
        -- Sleep is worth the same as the other three reservoirs and no more.
        -- Scarcity is what the shares are supposed to price: protein and
        -- creatine have to be found on the map, food and sleep are refilled by
        -- playing the day out, and a free channel outweighing a scavenged one
        -- inverted that. The 0.05 moved to form rather than to the supplements,
        -- so the baseline of a rested, fed character without a single can is
        -- unchanged at 0.80 and only the earned term grew.
        SleepShare = 0.10,
        FuelShare = 0.10,
        ToneFallbackCeiling = 0.05,
        -- Nominal 0.444, reachable 0.400. The felt course chases the reservoir
        -- at 1.0 per OnsetHours while the reservoir decays at DOSE_CREDIT per
        -- CourseDoseHours, so the two meet at
        -- `1 / (1 + DOSE_CREDIT * OnsetHours / CourseDoseHours)` = 0.9009 and
        -- no dosing volley makes `active` read 1. Pricing the coefficient at
        -- 0.40 therefore delivered 36.4% at the ceiling a player can reach,
        -- which the 2026-08-05 live run measured. Both directions move
        -- together so the trough stays exactly as deep as the crest was high.
        CourseCapBonus = 0.444,
        WithdrawalCapPenalty = 0.444,
        WithdrawalDecayDays = 10,
        -- The debt is felt through a chase of its own, so it arrives as a slope
        -- instead of a step. The window is derived rather than chosen: the boost
        -- chases its reservoir 9.09 times faster than that reservoir decays
        -- (`1/OnsetHours` against `DOSE_CREDIT/CourseDoseHours`), and the debt
        -- keeps the same ratio against its own decay of one per
        -- `WithdrawalDecayDays`:
        --   OnsetHours * (DOSE_CREDIT / CourseDoseHours)
        --     * (WithdrawalDecayDays * 24) = 24 * 0.004583 * 240 = 26.4
        WithdrawalOnsetHours = 26.4,
        -- The pause at the bottom, expressed as a multiple of the descent. The
        -- descent itself takes `depth * WithdrawalOnsetHours`, so `2` means the
        -- debt spends as long sitting at its deepest as it spent getting there,
        -- and only then starts repaying. The boost has no equivalent hold
        -- because a player can hold it themselves by dosing; nobody can hold
        -- the bottom, which is why it is granted.
        WithdrawalHoldFactor = 2,
        -- The debt is what makes a course a trade rather than a free ceiling,
        -- and a server may decide not to charge it. Turned off, the ceiling
        -- returns to the level cap when a course ends instead of dipping below
        -- it. Every coefficient above keeps its value: what disappears is the
        -- depth they are multiplied by, not the arithmetic.
        WithdrawalEnabled = true,
    },
}

local function bounded(value, minimum, maximum, fallback)
    if type(value) ~= "number" or value ~= value
            or value == math.huge or value == -math.huge then
        return fallback
    end
    if value < minimum then return minimum end
    if value > maximum then return maximum end
    return value
end

-- `bounded` is for numbers, and the `and`/`or` idiom collapses on a legitimate
-- `false`. Absent means the option was never written, which is the shipped
-- behaviour rather than the off state.
local function switched(value, fallback)
    if type(value) ~= "boolean" then return fallback end
    return value
end

-- Server-safe resolver. Returns a fresh table every call so a caller cannot
-- mutate the shared defaults.
function PPO.Config.getAdaptationSettings()
    local defaults = PPO.Config.Adaptation
    local source = SandboxVars
        and SandboxVars.PhysicalProgressionOverhaul or nil
    return {
        GainScale = bounded(source and source.AdaptationGainScale,
            0.25, 4.0, defaults.GainScale),
        CreditCap = bounded(source and source.AdaptationCreditCap,
            0.15, 1.20, defaults.CreditCap),
        ConversionHours = bounded(source and source.AdaptationConversionHours,
            6, 96, defaults.ConversionHours),
        GraceHours = bounded(source and source.AdaptationGraceHours,
            0, 336, defaults.GraceHours),
        DecayDays = bounded(source and source.AdaptationDecayDays,
            5, 120, defaults.DecayDays),
        -- Rides this table rather than taking a resolver of its own, unlike
        -- `courseWithdrawalEnabled`: the tick builds these settings exactly
        -- once, outside the direction loop, and the decay reads `DecayDays`
        -- from the same call.
        DecayEnabled = switched(source and source.AdaptationDecay,
            defaults.DecayEnabled),
        MinimumTrainingMinutes = bounded(
            source and source.AdaptationMinimumTrainingMinutes,
            10, 30, defaults.MinimumTrainingMinutes),
        FullQualityTrainingMinutes = bounded(
            source and source.AdaptationFullQualityTrainingMinutes,
            10, 60, defaults.FullQualityTrainingMinutes),
        MinimumDirectionCoverage = defaults.MinimumDirectionCoverage,
        FullQualityDirectionCoverage = defaults.FullQualityDirectionCoverage,
        IncompleteSessionGapMinutes = defaults.IncompleteSessionGapMinutes,
        MultiplierRefreshEpsilon = defaults.MultiplierRefreshEpsilon,
        BaseSessionCredit = defaults.BaseSessionCredit,
    }
end

-- The budget a player sets is a number of training minutes, not a stimulus:
-- minutes are what the exercise window counts, and one stimulus is thirty of
-- them.
function PPO.Config.getLoadSettings()
    local defaults = PPO.Config.Load
    local source = SandboxVars
        and SandboxVars.PhysicalProgressionOverhaul or nil
    local minutes = bounded(source and source.TrainingBudgetMinutes, 0, 180,
        defaults.RecoverableStimulus * defaults.MinutesPerStimulus)
    return {
        RecoverableStimulus = minutes / defaults.MinutesPerStimulus,
        RecoveryHours = bounded(source and source.TrainingBudgetRecoveryHours,
            6, 168, defaults.RecoveryHours),
        MinutesPerStimulus = defaults.MinutesPerStimulus,
    }
end

local SHARE_ORDER = { "AdaptationShare", "ProteinShare", "CreatineShare",
    "SleepShare", "FuelShare" }
local SHARE_KEYS = {
    AdaptationShare = "ShareAdaptationPercent",
    ProteinShare = "ShareProteinPercent",
    CreatineShare = "ShareCreatinePercent",
    SleepShare = "ShareSleepPercent",
    FuelShare = "ShareFuelPercent",
}

local MULTIPLIER_CAP_KEYS = {
    "MultiplierCapLevels01", "MultiplierCapLevels23", "MultiplierCapLevels45",
    "MultiplierCapLevels67", "MultiplierCapLevels89",
}

-- The option is the delivered percentage itself, not a percentage of it. A
-- knob priced as "percent of the shipped strength" hides one percentage under
-- another: a server that wants a course worth 100% would have to know the
-- shipped figure is 40 and type 250. So SHIPPED_COURSE_PERCENT is the divisor
-- rather than 100, and the option reads in the same unit as the answer.
--
-- 40 is the delivered figure, not the nominal one. The coefficient is 0.444,
-- but the felt course chases a decaying reservoir and the two meet at 0.9009,
-- so 0.444 * 0.9009 = 0.400 is what a player can actually hold - the number the
-- 2026-08-05 live run measured.
local SHIPPED_COURSE_PERCENT = 40

-- One helper, two readers. The multiplier ceiling and the tone ceiling are
-- pinned equal by design, so they must not be able to drift apart in code
-- either: both scale by this table, per direction.
function PPO.Config.courseCeilingScale()
    local source = SandboxVars
        and SandboxVars.PhysicalProgressionOverhaul or nil
    return {
        Strength = bounded(source and source.CourseCeilingAnabolic,
            0, 100, SHIPPED_COURSE_PERCENT) / SHIPPED_COURSE_PERCENT,
        Fitness = bounded(source and source.CourseCeilingCardio,
            0, 100, SHIPPED_COURSE_PERCENT) / SHIPPED_COURSE_PERCENT,
    }
end

-- One boolean, read once per course tick. It has a resolver of its own rather
-- than riding getMultiplierSettings because that one copies the whole defaults
-- table and rebuilds the cap ladder and the five shares on every call, and the
-- tick reads its multiplier settings inside the per-direction loop.
--
-- Absent rather than false is the shipped behaviour: a save written before this
-- option existed carries no value, and a course there still costs a debt.
function PPO.Config.courseWithdrawalEnabled()
    local source = SandboxVars
        and SandboxVars.PhysicalProgressionOverhaul or nil
    if source == nil or source.SupplementCourseWithdrawal == nil then
        return PPO.Config.Multiplier.WithdrawalEnabled
    end
    return source.SupplementCourseWithdrawal ~= false
end

-- Server-safe resolver, same contract as the rest: a fresh table every call.
function PPO.Config.getMultiplierSettings()
    local defaults = PPO.Config.Multiplier
    local source = SandboxVars
        and SandboxVars.PhysicalProgressionOverhaul or nil
    local resolved = {}
    for key, value in pairs(defaults) do resolved[key] = value end

    local caps = {}
    for index = 1, #MULTIPLIER_CAP_KEYS do
        caps[index] = bounded(source and source[MULTIPLIER_CAP_KEYS[index]],
            1, 50, defaults.LevelCaps[index])
    end
    resolved.LevelCaps = caps

    -- Normalized at read, which is what lets a free-form option set keep the
    -- invariant the constants used to carry: the five always sum to one. A set
    -- of five zeroes is not a configuration, it is an empty formula, so it
    -- falls back rather than dividing by zero.
    local weights = {}
    local total = 0
    for index = 1, #SHARE_ORDER do
        local field = SHARE_ORDER[index]
        local percent = bounded(source and source[SHARE_KEYS[field]],
            0, 100, defaults[field] * 100)
        weights[field] = percent
        total = total + percent
    end
    if total > 0 then
        for index = 1, #SHARE_ORDER do
            local field = SHARE_ORDER[index]
            resolved[field] = weights[field] / total
        end
    end

    resolved.CourseCapScale = PPO.Config.courseCeilingScale()

    return resolved
end

-- Two values, not three. `EnabledWhenAvailable` was meant to be "Auto, but
-- never disabled in single-player", and `ReadinessEngine.sleepRequired` already
-- returns true for every single-player call, so it was never distinguishable
-- from `Auto` in any branch. A saved config still carrying the old `3` falls
-- through to the fallback, which is `Auto` -- the same behaviour it already had.
local SLEEP_INFLUENCE = { [1] = "Auto", [2] = "Ignore" }
local SLEEP_INFLUENCE_NAMES = { Auto = true, Ignore = true }

-- The string branch keeps an already-resolved value idempotent, so a caller may
-- pass a resolved table back in later.
local function sleepInfluence(value, fallback)
    if type(value) == "string" and SLEEP_INFLUENCE_NAMES[value] == true then
        return value
    end
    if SLEEP_INFLUENCE[value] ~= nil then return SLEEP_INFLUENCE[value] end
    return fallback
end

function PPO.Config.getRecoverySettings()
    local defaults = PPO.Config.Recovery
    local source = SandboxVars
        and SandboxVars.PhysicalProgressionOverhaul or nil
    return {
        ReadinessFloor = bounded(source and source.ReadinessFloorPercent,
            0, 100, defaults.ReadinessFloor * 100) / 100,
        FatigueRampStart = defaults.FatigueRampStart,
        FatigueRampEnd = defaults.FatigueRampEnd,
        FatiguePenalty = bounded(source and source.ReadinessFatiguePenalty,
            0, 100, defaults.FatiguePenalty * 100) / 100,
        ShareFatigueRampEnd = defaults.ShareFatigueRampEnd,
        ShareFatiguePenalty = defaults.ShareFatiguePenalty,
        FuelRampStart = defaults.FuelRampStart,
        FuelRampEnd = defaults.FuelRampEnd,
        FuelPenalty = bounded(source and source.ReadinessFuelPenalty,
            0, 100, defaults.FuelPenalty * 100) / 100,
        RecoverySupportFloor = defaults.RecoverySupportFloor,
        SleepInfluence = sleepInfluence(source and source.SleepInfluence,
            defaults.SleepInfluence),
    }
end

function PPO.Config.getToneSettings()
    local defaults = PPO.Config.Tone
    local source = SandboxVars
        and SandboxVars.PhysicalProgressionOverhaul or nil
    local refund = defaults.EnduranceRefundEnabled
    if source ~= nil and type(source.EnduranceRefundEnabled) == "boolean" then
        refund = source.EnduranceRefundEnabled
    end
    local carry = {}
    local endurance = {}
    for stage = 1, 3 do
        carry[stage] = bounded(source and source["ToneCarryStage" .. stage],
            0, 30, defaults.CarryStages[stage])
        endurance[stage] = bounded(
            source and source["ToneEnduranceStage" .. stage],
            0, 100, defaults.EnduranceStages[stage])
    end
    return {
        CarryStages = carry,
        EnduranceStages = endurance,
        MaxDurationHours = bounded(source and source.ToneMaxDurationHours,
            1, 24, defaults.MaxDurationHours),
        EnduranceRefundEnabled = refund,
        EnduranceRecoveryPerMinute = defaults.EnduranceRecoveryPerMinute,
        FallbackReadinessBonus = defaults.FallbackReadinessBonus,
        MaxPlausibleEnduranceDrop = defaults.MaxPlausibleEnduranceDrop,
    }
end

function PPO.Config.getNutritionSettings()
    local defaults = PPO.Config.Nutrition
    local source = SandboxVars
        and SandboxVars.PhysicalProgressionOverhaul or nil
    return {
        SupportDurationHours = bounded(
            source and source.NutritionSupportDurationHours,
            6, 336, defaults.SupportDurationHours),
    }
end

-- The serving window is shared with the nutrition support option, because one
-- protein or creatine serving is exactly what that option has always measured.
function PPO.Config.getSupplementSettings()
    local defaults = PPO.Config.Supplements
    local source = SandboxVars
        and SandboxVars.PhysicalProgressionOverhaul or nil
    return {
        ServingHours = bounded(
            source and source.NutritionSupportDurationHours,
            6, 336, defaults.ServingHours),
        CourseDoseHours = bounded(
            source and source.SupplementCourseDurationHours,
            12, 720, defaults.CourseDoseHours),
        -- The felt course chases the reservoir over this window. Accounting is
        -- instant; the effect is not, so a lone dose never reaches its nominal
        -- value.
        OnsetHours = bounded(
            source and source.SupplementOnsetHours,
            1, 168, defaults.OnsetHours),
    }
end

-- The window is a duration, so a server may shorten or disable it; zero hours
-- is a legal setting and turns both stimulant effects off. The acceleration is
-- not exposed, because scaling the strength of an effect whose duration
-- accumulates is how a course becomes a permanent mode.
function PPO.Config.getStimulantSettings()
    local defaults = PPO.Config.Stimulant
    local source = SandboxVars
        and SandboxVars.PhysicalProgressionOverhaul or nil
    return {
        WindowHours = bounded(
            source and source.SupplementStimulantHours,
            0, 72, defaults.WindowHours),
        StrainAcceleration = defaults.StrainAcceleration,
        ContainerServings = bounded(
            source and source.StimulantTankServings,
            1, 10, PPO.Config.Windows.StimulantServings),
    }
end

-- The window is a duration, so a server may shorten or disable it; zero hours
-- is a legal setting and turns the item off entirely. Depth, descent rate and
-- heat are constants for the same reason the stimulant's acceleration is one.
function PPO.Config.getThermogenicSettings()
    local defaults = PPO.Config.Thermogenic
    local source = SandboxVars
        and SandboxVars.PhysicalProgressionOverhaul or nil
    return {
        WindowHours = bounded(
            source and source.SupplementThermogenicHours,
            0, 72, defaults.WindowHours),
        CalorieCeiling = defaults.CalorieCeiling,
        DescentPerHour = defaults.DescentPerHour,
        HeatFloor = defaults.HeatFloor,
        ContainerServings = bounded(
            source and source.ThermogenicTankServings,
            1, 10, PPO.Config.Windows.ContainerServings),
    }
end

-- The vanilla option, not a PPO one. With nutrition off, Nutrition.update()
-- returns before updateWeight(), so a calorie write buys nothing -- and would
-- park the stock at the ceiling until an admin switched the option back on and
-- collapsed the character's weight in a single tick.
function PPO.Config.vanillaNutritionEnabled()
    if SandboxVars == nil then return true end
    return SandboxVars.Nutrition ~= false
end

-- Only the cost is exposed here, and it scales both ends of the same dial: the
-- course-side floors and the debt-side ones move together, so this option
-- cannot flatten the negative end alone and leave a free benefit behind.
--
-- Selling the benefit outright is a separate decision and a separate option.
-- `courseWithdrawalEnabled` removes the debt itself rather than its price, so
-- the coefficients this table pins equal never drift apart.
function PPO.Config.getCourseEffectSettings()
    local defaults = PPO.Config.CourseEffects
    local source = SandboxVars
        and SandboxVars.PhysicalProgressionOverhaul or nil
    local resolved = {}
    for key, value in pairs(defaults) do resolved[key] = value end
    resolved.SideEffectScale = bounded(
        source and source.SupplementCourseSideEffects,
        0, 200, 100) / 100
    resolved.CourseCeilingScale = PPO.Config.courseCeilingScale()
    return resolved
end
