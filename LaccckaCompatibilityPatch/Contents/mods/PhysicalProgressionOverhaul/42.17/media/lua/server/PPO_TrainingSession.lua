require "PPO_Config"
require "PPO_AdaptationMath"
require "PPO_ExerciseDefinitions"
require "PPO_ExerciseState"
require "PPO_GameClock"
require "PPO_ToneEngine"

PPO = PPO or {}
PPO.TrainingSession = PPO.TrainingSession or {}

local TrainingSession = PPO.TrainingSession
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

-- Server-observed monotonic world minute. A missing or throwing seam returns
-- nil, which contributes zero duration instead of inventing proof of training.
local function defaultWorldMinute()
    local ok, minutes = pcall(function()
        return getGameTime():getWorldAgeHours() * 60
    end)
    if not ok then return nil end
    return minutes
end

-- Vanilla regularity is read after the vanilla repeat already updated it.
local function defaultRegularity(character, exerciseId)
    local ok, value = pcall(function()
        return character:getFitness():getRegularity(exerciseId)
    end)
    if not ok then return nil end
    return value
end

local function defaultSettings()
    return PPO.Config.getAdaptationSettings()
end

-- The injection table is checked by type, not against `nil`: an unpassed
-- parameter carries whatever the caller left on the stack, and reading fields
-- off that value is an error rather than a fallback.
function TrainingSession.new(options)
    local injected = {}
    if type(options) == "table" then injected = options end
    return {
        worldMinute = injected.worldMinute or defaultWorldMinute,
        regularity = injected.regularity or defaultRegularity,
        settings = injected.settings or defaultSettings,
        fragments = {},
    }
end

-- The authority and the Adaptation engine must observe the same fragments, so
-- production shares one manager instance.
function TrainingSession.ensureDefault()
    if TrainingSession.Default == nil then
        TrainingSession.Default = TrainingSession.new(nil)
    end
    return TrainingSession.Default
end

local function currentMinute(manager)
    local ok, value = pcall(manager.worldMinute)
    if not ok then return nil end
    return finiteOr(value, nil)
end

local function resolveSettings(manager)
    local ok, settings = pcall(manager.settings)
    if not ok or type(settings) ~= "table" then
        return PPO.Config.Adaptation
    end
    return settings
end

local function sampleRegularity(manager, character, exerciseId)
    local ok, value = pcall(manager.regularity, character, exerciseId)
    if not ok then return nil end
    return finiteOr(value, nil)
end

local function emptyResult()
    return {
        Strength = { finalized = false, creditEarned = 0, qualified = false },
        Fitness = { finalized = false, creditEarned = 0, qualified = false },
    }
end

function TrainingSession.beginFragment(manager, character, exerciseId)
    if manager == nil or character == nil then return false end
    if PPO.ExerciseState.get(character) == nil then return false end

    -- A replaced fragment still contributes its own eligible duration, but a
    -- replacement is never a normal finish and cannot create credit.
    if manager.fragments[character] ~= nil then
        TrainingSession.finishFragment(manager, character, false)
    end

    local startedAt = currentMinute(manager)
    manager.fragments[character] = {
        exerciseId = exerciseId,
        startedAt = startedAt,
        lastLoadMinute = startedAt,
        accepted = { Strength = 0, Fitness = 0 },
    }
    return true
end

-- Physical effects ask this before refunding endurance, so a tone can never
-- pay back the very session that is still producing it.
function TrainingSession.isFragmentOpen(manager, character)
    if manager == nil or character == nil then return false end
    return manager.fragments[character] ~= nil
end

-- The directions whose action is running right now, which the engine tick asks
-- for before recovering load: training is not rest. Membership comes from the
-- exercise definition rather than from accepted repetitions, so the head of an
-- action counts too.
function TrainingSession.trainingDirections(manager, character)
    local result = {}
    if manager == nil or character == nil then return result end

    local fragment = manager.fragments[character]
    if fragment == nil then return result end

    local definition = PPO.ExerciseDefinitions.get(fragment.exerciseId)
    if definition == nil then return result end

    for _, direction in ipairs(DIRECTIONS) do
        if definition.loadRate[direction] ~= nil then
            result[direction] = true
        end
    end
    return result
end

local function loadCeilingFor(periodMs)
    local ceiling = 1
    local ok, resolved = pcall(PPO.GameClock.loadCeiling, periodMs)
    if ok then ceiling = positiveOr(resolved, 1) end
    return ceiling
end

-- Game minutes since the previous charge, bounded by a ceiling derived from the
-- day length. Peeking advances nothing: a token is only a proposal, and a token
-- that is built and then never matched must not take its span with it. Returns
-- the span and the minute it was measured at, which the token carries so the
-- commit can charge the same instant later.
function TrainingSession.peekLoadMinutes(manager, character, periodMs)
    if manager == nil or character == nil then return 0, nil end
    local fragment = manager.fragments[character]
    if fragment == nil or fragment.lastLoadMinute == nil then return 0, nil end

    local minute = currentMinute(manager)
    if minute == nil then return 0, nil end

    return clamp(minute - fragment.lastLoadMinute, 0,
        loadCeilingFor(periodMs)), minute
end

-- Charges the span up to `minute` and consumes it: the marker moves to that
-- instant and only ever forward, so committing the same token twice charges
-- nothing the second time and a backwards clock charges nothing at all.
function TrainingSession.commitLoadMinutes(manager, character, minute, periodMs)
    if manager == nil or character == nil then return 0 end
    local fragment = manager.fragments[character]
    if fragment == nil or fragment.lastLoadMinute == nil then return 0 end
    if finiteOr(minute, nil) == nil then return 0 end

    local delta = clamp(minute - fragment.lastLoadMinute, 0,
        loadCeilingFor(periodMs))
    if minute > fragment.lastLoadMinute then
        fragment.lastLoadMinute = minute
    end
    return delta
end

-- Restores the marker when an award failed behind an already committed span.
-- Under-charging is the safe direction on the abnormal paths.
function TrainingSession.rewindLoadMinute(manager, character, minute)
    if manager == nil or character == nil then return false end
    local fragment = manager.fragments[character]
    if fragment == nil then return false end
    if finiteOr(minute, nil) == nil then return false end
    fragment.lastLoadMinute = minute
    return true
end

-- Read-only marker, so a caller can restore it after a failure.
function TrainingSession.markerMinute(manager, character)
    if manager == nil or character == nil then return nil end
    local fragment = manager.fragments[character]
    if fragment == nil then return nil end
    return fragment.lastLoadMinute
end

-- Only an accepted repeat from the exercise authority reaches this function,
-- and only while its own action fragment is armed.
function TrainingSession.acceptRepeat(manager, character, token)
    if manager == nil or character == nil or type(token) ~= "table" then
        return false
    end
    local fragment = manager.fragments[character]
    if fragment == nil or type(token.stimulus) ~= "table" then return false end

    local state = PPO.ExerciseState.get(character)
    if state == nil then return false end

    local minute = currentMinute(manager)
    local exerciseId = token.exerciseId or fragment.exerciseId
    local regularity = sampleRegularity(manager, character, exerciseId)
    local accepted = false

    for _, direction in ipairs(DIRECTIONS) do
        local stimulus = token.stimulus[direction]
        if stimulus ~= nil then
            local session = state[direction].session
            local loadReturn = 0
            if type(token.loadReturn) == "table" then
                loadReturn = clamp(
                    positiveOr(token.loadReturn[direction], 0), 0, 1)
            end

            local covered = 0
            if type(token.loadMinutes) == "table" then
                covered = positiveOr(token.loadMinutes[direction], 0)
            end
            session.coveredMinutes = session.coveredMinutes + covered
            session.acceptedRepeats = session.acceptedRepeats + 1
            session.loadReturnSum = session.loadReturnSum + loadReturn
            if regularity ~= nil then
                session.regularitySum = session.regularitySum
                    + math.max(0, regularity)
                session.regularitySamples = session.regularitySamples + 1
            end
            if minute ~= nil then
                session.lastAcceptedRepeatMinute = minute
            end
            fragment.accepted[direction] = fragment.accepted[direction] + 1
            accepted = true
        end
    end
    return accepted
end

local function finalizeDirection(character, direction, settings,
        startedMinute, completedMinute)
    local component = PPO.ExerciseState.getComponent(character, direction)
    local session = component.session

    local durationQuality = PPO.AdaptationMath.durationQuality(
        session.activeTrainingMinutes,
        settings.MinimumTrainingMinutes,
        settings.FullQualityTrainingMinutes)
    local coverageQuality = PPO.AdaptationMath.coverageQuality(
        session.coveredMinutes,
        session.activeTrainingMinutes,
        settings.MinimumDirectionCoverage,
        settings.FullQualityDirectionCoverage)

    local meanRegularity = 0
    if session.regularitySamples > 0 then
        meanRegularity = session.regularitySum / session.regularitySamples
    end
    local meanLoadReturn = 0
    if session.acceptedRepeats > 0 then
        meanLoadReturn = session.loadReturnSum / session.acceptedRepeats
    end

    local regularityQuality = PPO.AdaptationMath.regularityQuality(
        meanRegularity)
    -- Execution quality is the session without its load factor. Credit keeps
    -- the load factor, because a repeated same-day session really is worth less
    -- long-term form; tone does not, because the Worked gate already owns
    -- load.
    local execQuality = durationQuality * coverageQuality * regularityQuality

    local quality = PPO.AdaptationMath.sessionQuality(
        durationQuality,
        coverageQuality,
        regularityQuality,
        meanLoadReturn)
    local earned = PPO.AdaptationMath.creditEarned(
        settings.BaseSessionCredit, quality, settings.GainScale)

    -- The credit commit and the accumulator reset belong to one transaction.
    session.awaitingFinalization = true
    component.adaptationCredit = clamp(
        component.adaptationCredit + earned,
        0,
        positiveOr(settings.CreditCap, 0.60))
    component.lastQualifiedSessionMinute = completedMinute
    component.lastSessionStartedMinute = startedMinute
    component.lastSessionCompletedMinute = completedMinute
    component.adaptationGraceRemaining =
        positiveOr(settings.GraceHours, 72) * 60
    PPO.ExerciseState.resetSession(character, direction, completedMinute)

    -- Tone is created inside the same transaction that commits credit, so an
    -- unqualified or discarded session can never produce a tone.
    pcall(PPO.ToneEngine.onSessionFinalized,
        character, direction, execQuality)

    return {
        finalized = true,
        qualified = true,
        creditEarned = earned,
        quality = quality,
        startedMinute = startedMinute,
        completedMinute = completedMinute,
    }
end

-- A repetition charges its span when its token is built, so the span between
-- the last token and the end of the action would otherwise never be charged.
-- That tail is real time — the last repetition's animation plus the stop — and
-- therefore more game minutes the shorter the day, which is exactly how the
-- DayLength dependence the rebalance removed crept back in. The live run of
-- 2026-07-28 measured one nominal twenty-minute squat session charging 0.5563
-- at DayLength 2 against 0.6515 at DayLength 5.
--
-- Only a direction that already accepted a repetition in this fragment is
-- charged, so an action whose every repetition was rejected still covers
-- nothing and gains nothing: coverage stays an integrity gate. The span goes
-- through the same consume path as any other, so it obeys the derived ceiling
-- and can never be charged twice.
local function chargeFragmentTail(manager, character, fragment)
    if fragment == nil then return end

    local definition = PPO.ExerciseDefinitions.get(fragment.exerciseId)
    if definition == nil then return end

    local charged = false
    for _, direction in ipairs(DIRECTIONS) do
        if definition.loadRate[direction] ~= nil
                and fragment.accepted[direction] > 0 then
            charged = true
        end
    end
    if not charged then return end

    local minute = currentMinute(manager)
    if minute == nil then return end
    local tail = TrainingSession.commitLoadMinutes(
        manager, character, minute, definition.periodMs)
    if tail <= 0 then return end

    local state = PPO.ExerciseState.get(character)
    if state == nil then return end

    local stimulus = {}
    for _, direction in ipairs(DIRECTIONS) do
        local rate = definition.loadRate[direction]
        if rate ~= nil and fragment.accepted[direction] > 0 then
            stimulus[direction] = rate * tail
            local session = state[direction].session
            session.coveredMinutes = session.coveredMinutes + tail
        end
    end
    PPO.ExerciseState.applyAcceptedRepeat(character, stimulus)
end

function TrainingSession.finishFragment(manager, character, allowFinalize)
    local result = emptyResult()
    if manager == nil or character == nil then return result end

    local fragment = manager.fragments[character]
    -- Charged before the fragment is released, because the consume path reads
    -- the fragment's own marker.
    pcall(chargeFragmentTail, manager, character, fragment)
    manager.fragments[character] = nil

    local state = PPO.ExerciseState.get(character)
    if state == nil then return result end

    local finishedAt = currentMinute(manager)
    local elapsed = 0
    if fragment ~= nil and fragment.startedAt ~= nil and finishedAt ~= nil then
        elapsed = math.max(0, finishedAt - fragment.startedAt)
    end

    local settings = resolveSettings(manager)
    local minimumMinutes = positiveOr(settings.MinimumTrainingMinutes, 10)
    local minimumCoverage = positiveOr(settings.MinimumDirectionCoverage, 0.50)

    for _, direction in ipairs(DIRECTIONS) do
        local session = state[direction].session
        if fragment ~= nil and fragment.accepted[direction] > 0 then
            session.activeTrainingMinutes =
                session.activeTrainingMinutes + elapsed
        end

        local covered = 0
        if session.activeTrainingMinutes > 0 then
            covered = session.coveredMinutes / session.activeTrainingMinutes
        end
        local qualified = PPO.AdaptationMath.reachedThreshold(
                session.activeTrainingMinutes, minimumMinutes, 0.5)
            and PPO.AdaptationMath.reachedThreshold(
                covered, minimumCoverage, nil)
        session.qualified = qualified
        result[direction].qualified = qualified

        if qualified and allowFinalize == true then
            local completedMinute = finishedAt
            if completedMinute == nil then
                completedMinute = session.lastAcceptedRepeatMinute
            end
            local startedMinute = nil
            if fragment ~= nil then startedMinute = fragment.startedAt end
            if startedMinute == nil then
                startedMinute = math.max(0,
                    completedMinute - session.activeTrainingMinutes)
            end
            result[direction] = finalizeDirection(
                character, direction, settings, startedMinute, completedMinute)
        end
    end
    return result
end

-- Disconnect, reload and replacement drop only ephemeral fragment data. The
-- persisted partial session survives for the same character.
function TrainingSession.freezeCharacter(manager, character)
    if manager == nil or character == nil then return false end
    if manager.fragments[character] == nil then return false end
    manager.fragments[character] = nil
    return true
end

-- Death discards the incomplete session; it never converts it into credit.
function TrainingSession.discardCharacter(manager, character)
    if manager == nil or character == nil then return false end
    manager.fragments[character] = nil
    return PPO.ExerciseState.clearIncompleteSessions(character)
end

-- Called only from the active-minute engine tick, so offline time can never
-- expire an accumulator.
function TrainingSession.expireIncomplete(manager, character)
    if manager == nil or character == nil then return false end

    local state = PPO.ExerciseState.get(character)
    if state == nil then return false end

    local minute = currentMinute(manager)
    if minute == nil then return false end

    local settings = resolveSettings(manager)
    local gap = positiveOr(settings.IncompleteSessionGapMinutes, 30)
    local expired = false

    for _, direction in ipairs(DIRECTIONS) do
        local session = state[direction].session
        if session.acceptedRepeats > 0
                and minute - session.lastAcceptedRepeatMinute > gap then
            PPO.ExerciseState.resetSession(character, direction, minute)
            expired = true
        end
    end
    return expired
end
