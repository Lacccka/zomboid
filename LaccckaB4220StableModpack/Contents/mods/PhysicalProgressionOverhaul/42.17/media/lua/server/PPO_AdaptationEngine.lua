require "PPO_Config"
require "PPO_BonusMath"
require "PPO_AdaptationMath"
require "PPO_MultiplierMath"
require "PPO_ExerciseState"
require "PPO_WindowState"
require "PPO_StimulantState"
require "PPO_ConsumeAuthority"
require "PPO_CourseCost"
require "PPO_RecoveryContext"
require "PPO_ToneEngine"
require "PPO_BonusAwarder"
require "PPO_StateReport"
require "PPO_PhysicalEffects"
require "PPO_TrainingSession"

PPO = PPO or {}
PPO.AdaptationEngine = PPO.AdaptationEngine or {}

local AdaptationEngine = PPO.AdaptationEngine
local DIRECTIONS = { "Strength", "Fitness" }

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

local function positiveOr(value, fallback)
    return math.max(0, finiteOr(value, fallback))
end

local function defaultRecovery(character, direction)
    return PPO.RecoveryContext.get(character, direction)
end

local function defaultSettings()
    return PPO.Config.getAdaptationSettings()
end

local function defaultLevel(character, direction)
    local ok, level = pcall(function()
        return character:getPerkLevel(Perks[direction])
    end)
    if not ok then return 0 end
    return clamp(positiveOr(level, 0), 0, 10)
end

-- The injection table is checked by type, not against `nil`: an unpassed
-- parameter carries whatever the caller left on the stack, and reading fields
-- off that value is an error rather than a fallback.
function AdaptationEngine.new(options)
    local injected = {}
    if type(options) == "table" then injected = options end
    local instance = {
        recovery = injected.recovery or defaultRecovery,
        settings = injected.settings or defaultSettings,
        level = injected.level or defaultLevel,
        sessions = injected.sessions or PPO.TrainingSession.ensureDefault(),
        recoveryHours = injected.recoveryHours,
        effects = injected.effects or PPO.PhysicalEffects.new(),
        reports = injected.reports or PPO.StateReport.new(),
        applied = {},
    }

    -- Both call styles are supported so the server bootstrap can use method
    -- syntax without a metatable in persisted-adjacent code.
    instance.tickCharacter = function(self, character, elapsedMinutes)
        return AdaptationEngine.tickCharacter(self, character, elapsedMinutes)
    end
    instance.multiplierInputs = function(self, character, direction)
        return AdaptationEngine.multiplierInputs(self, character, direction)
    end
    instance.recordApplied = function(self, character, direction, multiplier, level)
        return AdaptationEngine.recordApplied(
            self, character, direction, multiplier, level)
    end
    instance.freezeCharacter = function(self, character)
        return AdaptationEngine.freezeCharacter(self, character)
    end
    instance.discardCharacter = function(self, character)
        return AdaptationEngine.discardCharacter(self, character)
    end
    return instance
end

local function resolveSettings(engine)
    local ok, settings = pcall(engine.settings)
    if not ok or type(settings) ~= "table" then
        return PPO.Config.Adaptation
    end
    return settings
end

-- Returns the whole recovery table, bounded. An injected provider may report
-- only the composite, so every field the multiplier reads is defaulted here
-- rather than at each use; a missing field can then only understate.
local function resolveRecovery(engine, character, direction)
    local ok, recovery = pcall(engine.recovery, character, direction)
    if not ok or type(recovery) ~= "table" then
        error("recovery provider failed for " .. tostring(direction))
    end
    local readiness = PPO.RecoveryContext.bounded(recovery.readiness)
    local rested = recovery.restedReadiness
    if type(rested) ~= "number" then rested = readiness end
    return {
        readiness = readiness,
        recoverySupport = PPO.RecoveryContext.bounded(
            recovery.recoverySupport),
        restedReadiness = PPO.RecoveryContext.bounded(rested),
        sleepFactor = PPO.RecoveryContext.bounded(recovery.sleepFactor),
        sleepShare = PPO.RecoveryContext.bounded(recovery.sleepShare),
        fuelFactor = PPO.RecoveryContext.bounded(recovery.fuelFactor),
        fuelShare = PPO.RecoveryContext.bounded(recovery.fuelShare),
        loadFactor = PPO.RecoveryContext.bounded(recovery.loadFactor),
        sleepRequired = recovery.sleepRequired ~= false,
        toneFallback = math.max(0,
            finiteOr(recovery.toneFallback, 0)),
    }
end

local function recoveryHours(engine)
    if engine.recoveryHours ~= nil then
        return math.max(0.001, finiteOr(engine.recoveryHours, 24))
    end
    local ok, resolved = pcall(PPO.Config.getLoadSettings)
    if ok and type(resolved) == "table" then
        return math.max(0.001, finiteOr(resolved.RecoveryHours, 24))
    end
    return 24
end

local function hasMeaningfulChange(engine, character, direction, multiplier,
        level, settings)
    local record = engine.applied[character]
    local previous = nil
    if record ~= nil then previous = record[direction] end
    if previous == nil then return true end
    if previous.level ~= level then return true end

    local epsilon = math.max(0,
        finiteOr(settings.MultiplierRefreshEpsilon, 0.01))
    return math.abs(multiplier - previous.multiplier) >= epsilon
end

function AdaptationEngine.recordApplied(engine, character, direction,
        multiplier, level)
    if engine == nil or character == nil then return false end
    if direction ~= "Strength" and direction ~= "Fitness" then return false end

    local record = engine.applied[character]
    if record == nil then
        record = {}
        engine.applied[character] = record
    end
    record[direction] = {
        multiplier = math.max(1, finiteOr(multiplier, 1)),
        level = clamp(positiveOr(level, 0), 0, 10),
    }
    return true
end

-- The one place the multiplier's inputs are assembled. The returned table is
-- both the `inputs` argument PPO.MultiplierMath.fill reads and the carrier for
-- the three arguments that sit outside `fill`, so no caller has to know which
-- reservoir belongs to which share. `engine` may be nil: the default recovery
-- provider is then used directly, which is what lets the exercise authority
-- reach the same numbers without holding an engine.
function AdaptationEngine.multiplierInputs(engine, character, direction)
    local recovery = nil
    if engine ~= nil then
        local ok, value = pcall(resolveRecovery, engine, character, direction)
        if ok then recovery = value end
    else
        local ok, value = pcall(defaultRecovery, character, direction)
        if ok and type(value) == "table" then recovery = value end
    end
    if type(recovery) ~= "table" then
        recovery = {
            readiness = 1, sleepFactor = 1, fuelFactor = 1, loadFactor = 1,
            sleepRequired = true, toneFallback = 0,
        }
    end

    local component = nil
    local componentOk, resolved = pcall(
        PPO.ExerciseState.getComponent, character, direction)
    if componentOk then component = resolved end

    local supplements = nil
    local supplementOk, resolvedSupplements = pcall(
        PPO.ExerciseState.getSupplements, character)
    if supplementOk then supplements = resolvedSupplements end

    local adaptation = 0
    local course = 0
    local courseLevel = 0
    local withdrawal = 0
    if component ~= nil then
        adaptation = clamp(finiteOr(component.adaptation, 0), 0, 1)
        if type(component.course) == "table" then
            -- The felt course, never the raw reservoir: accounting is instant,
            -- the effect is not. The reservoir rides along untouched by the
            -- formula, purely so the panel can show the gap between the two.
            course = clamp(finiteOr(component.course.active, 0), 0, 1)
            courseLevel = clamp(finiteOr(component.course.level, 0), 0, 1)
            -- The felt debt, mirroring `course.active` above: the charge is
            -- accounting and lands in one tick, the crash slopes in.
            withdrawal = clamp(
                finiteOr(component.course.withdrawalActive, 0), 0, 1)
        end
    end

    return {
        adaptation = adaptation,
        protein = clamp(finiteOr(supplements and supplements.protein, 0), 0, 1),
        creatine = clamp(
            finiteOr(supplements and supplements.creatine, 0), 0, 1),
        -- The multiplier's sleep term is the share curve, not the Readiness
        -- one. `readiness` below still carries the shallow curve, so a tired
        -- character loses the whole share while conversion only slows.
        sleepFactor = PPO.RecoveryContext.bounded(recovery.sleepShare),
        -- Same substitution for food and water: one-to-one into the share,
        -- while Readiness keeps the curve with the floor.
        fuelFactor = PPO.RecoveryContext.bounded(recovery.fuelShare),
        toneFallback = math.max(0, finiteOr(recovery.toneFallback, 0)),
        readiness = PPO.RecoveryContext.bounded(recovery.readiness),
        sleepRequired = recovery.sleepRequired ~= false,
        loadFactor = PPO.RecoveryContext.bounded(recovery.loadFactor),
        course = course,
        courseLevel = courseLevel,
        withdrawal = withdrawal,
        -- The ceiling a course buys is one knob per direction, so the formula
        -- has to know which direction it is answering for.
        direction = direction,
    }
end

-- The three arguments the pure formula takes beside `inputs`, unpacked from the
-- bundle above so every call site spells the formula the same way.
function AdaptationEngine.multiplierFor(level, inputs, loadFactor)
    -- A type check, not a nil check: Kahlua leaves an unpassed parameter
    -- holding the caller's stack slot, so `== nil` would let a leaked number
    -- through as a load. Call sites pass the load explicitly regardless.
    local load = loadFactor
    if type(load) ~= "number" then load = inputs.loadFactor end
    return PPO.MultiplierMath.multiplier(level, inputs, inputs.sleepRequired,
        load, inputs.course, inputs.withdrawal)
end

-- Costs are systemic and land once per character, not once per direction. The
-- endurance ceiling is deliberately absent here: it belongs to the single
-- endurance writer and is passed into applyFitnessTone instead.
function AdaptationEngine.applyCourseCost(engine, character, elapsedMinutes)
    if engine == nil or character == nil then return false end
    if engine.effects == nil then return false end

    local applied = false
    pcall(function()
        local state = PPO.ExerciseState.get(character)
        if state == nil then return end
        local depths = PPO.CourseCost.depths(state)
        for _, write in ipairs(PPO.CourseCost.writes(depths, elapsedMinutes)) do
            if write.shape == "floor" then
                engine.effects:floorStat(character, write.stat, write.value)
                applied = true
            elseif write.shape == "rate" then
                engine.effects:rateStat(character, write.stat, write.value)
                applied = true
            end
        end
        -- Called unconditionally so the seam can drop a stale sample when a
        -- debt ends. With both arguments at zero it writes nothing at all.
        local resolvedSettings = PPO.Config.getCourseEffectSettings()
        local brake = resolvedSettings.StrainDecayBrake * depths.wStrength
            * resolvedSettings.SideEffectScale

        -- The stimulant's acceleration is a benefit, so SideEffectScale -- which
        -- scales costs only -- deliberately does not touch it. The two shares
        -- compose here rather than in two calls: muscle strain has one writer.
        local stimulant = PPO.Config.getStimulantSettings()
        local utility = state.utility
        local minutes = utility ~= nil and utility.stimulantMinutes or 0
        local share = brake
            + PPO.StimulantState.strainShare(
                minutes, stimulant.StrainAcceleration)

        engine.effects:applyMuscleStrain(character,
            PPO.CourseCost.strainPerPart(depths.wStrength),
            share, resolvedSettings.MaxPlausibleStrainDrop)

        -- Cancelling costs a Fitness read per group, so it only runs while a
        -- window is actually open.
        if PPO.WindowState.active(minutes) then
            engine.effects:cancelFutureStrain(character)
            applied = true
        end
    end)
    return applied
end

-- Class B windows land here rather than inside applyCourseCost: a window is not
-- a cost, it carries no depth, and the two share no arithmetic. The stimulant's
-- composition into the strain call stays where it is, because muscle strain
-- still has exactly one writer.
--
-- Gated on the authority, unlike every other write in this file. The tick runs
-- in both processes -- observed live 2026-08-06 -- and a client's copy of
-- Nutrition never decays on its own, so an ungated calorie write would drive
-- the client's copy down twice as fast as the server's.
function AdaptationEngine.applyUtilityEffects(engine, character, elapsedMinutes)
    if engine == nil or character == nil then return false end
    if engine.effects == nil then return false end
    if not PPO.ConsumeAuthority.authoritative() then return false end

    local applied = false
    pcall(function()
        local state = PPO.ExerciseState.get(character)
        if state == nil or state.utility == nil then return end
        if not PPO.WindowState.active(state.utility.thermogenicMinutes) then
            return
        end

        local settings = PPO.Config.getThermogenicSettings()

        -- The heat is independent of the vanilla nutrition option: it is a
        -- stat, not part of the nutrition model, and it is what makes the
        -- window visible and thirsty.
        engine.effects:floorStat(character, "TEMPERATURE", settings.HeatFloor)
        applied = true

        if PPO.Config.vanillaNutritionEnabled() then
            engine.effects:capCalories(character, settings.CalorieCeiling,
                settings.DescentPerHour / 60, elapsedMinutes)
        end
    end)
    return applied
end

-- The endurance ceiling the Fitness debt imposes, read once per tick so the
-- tone call and the debt agree on one number.
function AdaptationEngine.enduranceCeiling(character)
    local ceiling = nil
    pcall(function()
        local state = PPO.ExerciseState.get(character)
        if state == nil then return end
        local depths = PPO.CourseCost.depths(state)
        for _, write in ipairs(PPO.CourseCost.writes(depths, 0)) do
            if write.stat == "ENDURANCE" and write.shape == "ceiling" then
                ceiling = write.value
            end
        end
    end)
    return ceiling
end

-- One deterministic active-minute tick per loaded online character. Nothing in
-- this function may run for an offline character.
function AdaptationEngine.tickCharacter(engine, character, elapsedMinutes)
    if engine == nil or character == nil then return nil end

    local state = PPO.ExerciseState.get(character)
    if state == nil then return nil end

    local elapsed = positiveOr(elapsedMinutes, 0)
    local settings = resolveSettings(engine)

    -- One lookup per tick, shared by recovery suspension and the warm-up.
    local training = nil
    if engine.sessions ~= nil then
        local ok, resolved = pcall(PPO.TrainingSession.trainingDirections,
            engine.sessions, character)
        if ok then training = resolved end
    end

    -- Charged before the course advances below, for the same reason the load
    -- recovery scale is read there: these minutes were lived at the depth the
    -- character is carrying now, not at the one the chase leaves behind.
    pcall(AdaptationEngine.applyCourseCost, engine, character, elapsed)
    -- Charged before the window is spent below, for the same reason the course
    -- cost is charged before the course advances: these minutes were lived with
    -- the window open.
    pcall(AdaptationEngine.applyUtilityEffects, engine, character, elapsed)
    local enduranceCeiling = AdaptationEngine.enduranceCeiling(character)

    if elapsed > 0 then
        -- A direction being trained right now spends these minutes instead of
        -- recovering them, so the load a session charges is the load it keeps.
        pcall(PPO.ExerciseState.advanceActiveMinutes,
            character, elapsed, recoveryHours(engine), training)
        if engine.sessions ~= nil then
            pcall(PPO.TrainingSession.expireIncomplete,
                engine.sessions, character)
        end
    end

    local result = {}
    -- Read straight off the state rather than recomputed: the windows are not
    -- per-direction, so they have no entry to hang from, and the report must
    -- describe the same numbers the writers just used.
    local utility = state.utility or {}
    result.windows = {
        stimulant = math.max(0, finiteOr(utility.stimulantMinutes, 0)),
        thermogenic = math.max(0, finiteOr(utility.thermogenicMinutes, 0)),
    }
    for _, direction in ipairs(DIRECTIONS) do
        local entry = {
            changed = false,
            adaptation = 0,
            credit = 0,
            readiness = 1,
            loadReturn = 1,
            loadStage = "Fresh",
            multiplier = 1,
            displayMultiplier = 1,
            restedMultiplier = 1,
            levelCap = 0,
            level = 0,
            toneQuality = 0,
            toneMinutes = 0,
            toneStage = 0,
            carryBonus = 0,
            capEffective = 0,
            fill = 0,
            sleepRequired = true,
            course = 0,
            courseLevel = 0,
            withdrawal = 0,
            shares = { adaptation = 0, protein = 0, creatine = 0,
                       sleep = 0, fuel = 0, tone = 0 },
        }

        local ok = pcall(function()
            local component = state[direction]
            local recovery = resolveRecovery(engine, character, direction)
            local readiness = recovery.readiness
            local recoverySupport = recovery.recoverySupport

            -- Readiness owns the multiplier and new tone quality only.
            -- Conversion speed belongs to RecoverySupport, so a bad day never
            -- multiplies one penalty into two.
            local conversion = PPO.AdaptationMath.convert(
                component.adaptation,
                component.adaptationCredit,
                elapsed,
                settings.ConversionHours,
                1,
                recoverySupport)
            component.adaptation = conversion.adaptation
            component.adaptationCredit = conversion.credit

            -- The one place in the mod that lowers form, and the only one that
            -- answers to the Sandbox switch. With the switch off nothing in
            -- this block runs: the grace clock is dead state once there is
            -- nothing to lose, so freezing it keeps a save that turns the
            -- option back on from spending a countdown that never ran.
            if settings.DecayEnabled ~= false then
                local graceUsed = math.min(
                    positiveOr(component.adaptationGraceRemaining, 0), elapsed)
                component.adaptationGraceRemaining = math.max(0,
                    component.adaptationGraceRemaining - graceUsed)

                local decayMinutes = elapsed - graceUsed
                if decayMinutes > 0 then
                    component.adaptation = PPO.AdaptationMath.decay(
                        component.adaptation, decayMinutes, settings.DecayDays)
                end
            end

            local loadReturn = PPO.BonusMath.bonusReturn(
                component.dailyStimulus, true)
            local level = engine.level(character, direction)

            -- Assembled from the same accessor every other call site uses, so
            -- the panel, the award and the out-of-exercise map can never read
            -- three different characters.
            local inputs = AdaptationEngine.multiplierInputs(
                engine, character, direction)
            inputs.adaptation = component.adaptation
            inputs.sleepFactor = recovery.sleepShare
            inputs.fuelFactor = recovery.fuelShare
            inputs.toneFallback = recovery.toneFallback
            inputs.sleepRequired = recovery.sleepRequired
            inputs.loadFactor = recovery.loadFactor

            -- Level 10 owns no multiplier above the vanilla floor and never
            -- creates a level 11 target.
            local earned = AdaptationEngine.multiplierFor(
                level, inputs, inputs.loadFactor) or 1
            -- The XP-facing value, which answers to the Sandbox decay switch.
            -- It must be the number the out-of-exercise map applies and
            -- records, or the refresh comparison never settles and every
            -- minute costs a packet.
            local multiplier = PPO.BonusMath.effectiveMultiplier(
                earned,
                PPO.MultiplierMath.dailyReturn(component.dailyStimulus))

            entry.adaptation = component.adaptation
            entry.credit = component.adaptationCredit
            entry.readiness = readiness
            entry.loadReturn = loadReturn
            entry.loadStage = PPO.AdaptationMath.overtrainingStage(
                component.dailyStimulus)
            entry.level = level
            entry.multiplier = multiplier

            entry.displayMultiplier = multiplier
            -- restedMultiplier neutralizes load only. Sleep and food still
            -- cost, because "once recovered" promises the end of the load
            -- penalty and nothing else.
            entry.restedMultiplier =
                AdaptationEngine.multiplierFor(level, inputs, 1) or 1
            entry.levelCap = PPO.MultiplierMath.levelCap(level) or 0
            entry.capEffective = PPO.MultiplierMath.effectiveCap(
                level, inputs.course, inputs.withdrawal, direction) or 0
            entry.fill = PPO.MultiplierMath.fill(
                inputs, inputs.sleepRequired)
            entry.sleepRequired = inputs.sleepRequired
            entry.shares = {
                adaptation = inputs.adaptation,
                protein = inputs.protein,
                creatine = inputs.creatine,
                sleep = inputs.sleepFactor,
                fuel = inputs.fuelFactor,
                tone = inputs.toneFallback,
            }
            entry.course = inputs.course
            entry.courseLevel = inputs.courseLevel
            entry.withdrawal = inputs.withdrawal

            -- The comparison runs on what the map actually carries, which is the
            -- drawn multiplier divided by the factor `AddXP` applies before it
            -- reads the map. Crossing a protein threshold moves that quotient
            -- while the drawn number stands still, so this is what keeps the
            -- entry from going stale on a meal; comparing the drawn value would
            -- leave the wrong entry in place until Adaptation happened to move.
            local applied = PPO.BonusMath.absorbed(
                multiplier,
                PPO.BonusAwarder.vanillaFactor(character, Perks[direction]))
            entry.changed = hasMeaningfulChange(
                engine, character, direction, applied, level, settings)

            -- The burn runs before the entry is populated, so the report the
            -- client receives in the same tick already shows the tone gone.
            pcall(PPO.ToneEngine.burnIfOvertrained, character, direction)

            local tone = PPO.ToneEngine.advance(character, direction, elapsed)
            if tone ~= nil then
                entry.toneQuality = tone.quality
                entry.toneMinutes = tone.minutesRemaining
            end
            entry.toneStage = PPO.ToneEngine.stage(character, direction)
            entry.carryBonus = PPO.ToneEngine.carryBonus(character, direction)

            if elapsed > 0 then
                local strengthOfEffect = PPO.ToneEngine.effectStrength(
                    character, direction)
                local seamOk = true
                if direction == "Strength" then
                    -- The carry seam is driven every tick, including at zero,
                    -- because zero is what restores the original base when a
                    -- tone expires.
                    seamOk = engine.effects:applyStrengthTone(
                        character, entry.carryBonus)
                else
                    -- The refund must never pay back endurance the running
                    -- action is still spending, so the live fragment state is
                    -- asked here rather than assumed.
                    local exercising = false
                    local openOk, open = pcall(
                        PPO.TrainingSession.isFragmentOpen,
                        engine.sessions, character)
                    if openOk then exercising = open == true end
                    seamOk = engine.effects:applyFitnessTone(
                        character, strengthOfEffect, elapsed, exercising,
                        enduranceCeiling)
                end
                if strengthOfEffect > 0 then
                    PPO.RecoveryContext.setSeamAvailability(
                        character, direction, seamOk)
                end
            end
        end)
        if not ok then entry.changed = false end

        result[direction] = entry
    end

    if engine.reports ~= nil then
        pcall(function()
            PPO.StateReport.publish(engine.reports, character,
                PPO.StateReport.build(result, character))
        end)
    end
    return result
end

function AdaptationEngine.freezeCharacter(engine, character)
    if engine == nil or character == nil then return false end

    local tracked = engine.applied[character] ~= nil
    engine.applied[character] = nil
    local frozen = false
    if engine.sessions ~= nil then
        frozen = PPO.TrainingSession.freezeCharacter(
            engine.sessions, character) == true
    end
    if engine.effects ~= nil then
        pcall(engine.effects.forget, engine.effects, character)
    end
    if engine.reports ~= nil then
        pcall(PPO.StateReport.forget, engine.reports, character)
    end
    PPO.RecoveryContext.SeamAvailability[character] = nil
    return tracked or frozen
end

function AdaptationEngine.discardCharacter(engine, character)
    if engine == nil or character == nil then return false end

    engine.applied[character] = nil
    if engine.effects ~= nil then
        pcall(engine.effects.forget, engine.effects, character)
    end
    if engine.reports ~= nil then
        pcall(PPO.StateReport.forget, engine.reports, character)
    end
    PPO.RecoveryContext.SeamAvailability[character] = nil
    if engine.sessions == nil then return false end
    return PPO.TrainingSession.discardCharacter(engine.sessions, character)
end
