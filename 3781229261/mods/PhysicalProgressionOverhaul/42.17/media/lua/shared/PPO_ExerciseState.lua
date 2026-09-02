require "PPO_Directions"
require "PPO_Num"
require "PPO_Config"
require "PPO_BonusMath"
require "PPO_AdaptationMath"
require "PPO_SupplementState"
require "PPO_WindowState"
require "PPO_CourseCost"

PPO = PPO or {}
PPO.ExerciseState = PPO.ExerciseState or {}

local ExerciseState = PPO.ExerciseState
-- Schemas 1 to 9 existed during development and carried a migration ladder
-- until 2026-08-10. It was deleted before release, unread: the mod had never
-- shipped, so no save outside the author's own machine ever carried one of
-- those numbers, and every branch of that ladder was unreachable in every copy
-- that will ever exist. A schema this file does not recognise is replaced by a
-- fresh state, which is the same path an unknown future schema already took.
local SCHEMA = 10
-- Reservoir decay is "one serving per window", so each reservoir only needs the
-- credit one nominal serving or dose grants. Protein and creatine share the
-- number: equal fill per serving is worth nothing if one of them then drains
-- seven times slower.
local SERVING_CREDIT = { protein = 0.75, creatine = 0.75 }
local DOSE_CREDIT = 0.22
-- The daily budget is a Sandbox setting now. At its shipped 1.5 stimulus per 24
-- active online hours that is forty-five training minutes a day, indefinitely
-- sustainable. A full hour costs 2.0 and carries 0.5 into the next day, so an
-- hour a day reaches the overtraining line on the fourth day and a rest day
-- becomes part of the cycle.
local DIRECTIONS = PPO.Directions.order()

local Num = PPO.Num

local function loadSettings()
    if PPO.Config == nil or PPO.Config.getLoadSettings == nil then
        return { RecoverableStimulus = 1.5, RecoveryHours = 24 }
    end
    local ok, resolved = pcall(PPO.Config.getLoadSettings)
    if not ok or type(resolved) ~= "table" then
        return { RecoverableStimulus = 1.5, RecoveryHours = 24 }
    end
    return resolved
end

local function wholeCount(value)
    return math.max(0, math.floor(Num.positive(value, 0)))
end

local function adaptationSettings()
    if PPO.Config == nil or PPO.Config.getAdaptationSettings == nil then
        return { GraceHours = 72 }
    end
    return PPO.Config.getAdaptationSettings()
end

-- Perks and the perk API are absent in some loaders, so the level lookup is
-- always guarded and falls back to the lowest baseline.
local function componentLevel(character, direction)
    if character == nil then return 0 end
    local ok, level = pcall(function()
        return character:getPerkLevel(Perks[direction])
    end)
    if not ok then return 0 end
    return Num.clamp(Num.positive(level, 0), 0, 10)
end

local function newSession(activeMinute)
    return {
        coveredMinutes = 0,
        activeTrainingMinutes = 0,
        -- Clock minutes above, work below. activeTrainingMinutes is how long
        -- the action ran and is the coverage denominator; workMinutes is what
        -- the repetitions were worth and is what credit and load read.
        workMinutes = 0,
        loadReturnSum = 0,
        acceptedRepeats = 0,
        regularitySum = 0,
        regularitySamples = 0,
        lastAcceptedRepeatMinute = activeMinute,
        qualified = false,
        awaitingFinalization = false,
    }
end

local function newSupplements()
    return { protein = 0, creatine = 0 }
end

local function normalizeSupplements(source)
    local supplements = source
    if type(supplements) ~= "table" then supplements = newSupplements() end
    supplements.protein = PPO.SupplementState.normalize(supplements.protein)
    supplements.creatine = PPO.SupplementState.normalize(supplements.creatine)
    return supplements
end

-- Both Class B windows live outside the two directions on purpose: neither
-- muscle strain nor the calorie stock belongs to Strength or to Fitness, and a
-- window filed under one of them would have to be read from both.
local function newUtility()
    return { stimulantMinutes = 0, thermogenicMinutes = 0 }
end

local function normalizeUtility(source)
    local utility = source
    if type(utility) ~= "table" then utility = newUtility() end
    utility.stimulantMinutes =
        PPO.WindowState.normalize(utility.stimulantMinutes)
    utility.thermogenicMinutes =
        PPO.WindowState.normalize(utility.thermogenicMinutes)
    return utility
end

-- A course lives inside its direction's component, which is what makes the
-- isolation rule a property of the data shape: anabolic credit has nowhere to
-- go but Strength, cardio credit nowhere but Fitness.
-- `level` and `peak` are the accounting: what was taken and how deep the
-- reservoir ever ran. `active` and `activePeak` are what the character actually
-- feels, which lags the accounting by the onset window. The debt is charged
-- from `activePeak`, because giving back more than the course ever delivered
-- would break the symmetry the cap arithmetic promises.
local function newCourse()
    return {
        level = 0, peak = 0, lastDoseMinute = 0,
        active = 0, activePeak = 0, withdrawal = 0,
        withdrawalActive = 0, withdrawalFreeze = 0,
    }
end

local function normalizeCourse(source)
    if type(source) ~= "table" then return newCourse() end
    source.level = PPO.SupplementState.normalize(source.level)
    source.peak = PPO.SupplementState.normalize(source.peak)
    source.lastDoseMinute = Num.positive(source.lastDoseMinute, 0)
    source.active = PPO.SupplementState.normalize(source.active)
    source.activePeak = PPO.SupplementState.normalize(source.activePeak)
    source.withdrawal = PPO.SupplementState.normalize(source.withdrawal)
    -- A save written before the debt had a felt layer carries neither field.
    -- Both read zero, so an owed character serves the descent again rather than
    -- inheriting a bottom it never fell to.
    source.withdrawalActive = PPO.SupplementState.normalize(
        source.withdrawalActive)
    source.withdrawalFreeze = Num.positive(source.withdrawalFreeze, 0)
    return source
end

local function newTone()
    return { quality = 0, minutesRemaining = 0, lastAppliedMinute = 0 }
end

local function normalizeTone(source)
    if type(source) ~= "table" then return newTone() end
    source.quality = Num.clamp(Num.positive(source.quality, 0), 0, 1)
    source.minutesRemaining = Num.positive(source.minutesRemaining, 0)
    source.lastAppliedMinute = Num.positive(source.lastAppliedMinute, 0)
    if source.minutesRemaining <= 0 then
        source.quality = 0
        source.minutesRemaining = 0
    end
    return source
end

-- Normalization is in place so a caller holding a component or session table
-- keeps writing to the persisted table across later reads.
local function normalizeSession(source, activeMinute)
    if type(source) ~= "table" then return newSession(activeMinute) end

    source.coveredMinutes = Num.positive(source.coveredMinutes, 0)
    source.volume = nil
    source.activeTrainingMinutes = Num.positive(source.activeTrainingMinutes, 0)
    source.workMinutes = Num.positive(source.workMinutes, 0)
    source.loadReturnSum = Num.positive(source.loadReturnSum, 0)
    source.acceptedRepeats = wholeCount(source.acceptedRepeats)
    source.regularitySum = Num.positive(source.regularitySum, 0)
    source.regularitySamples = wholeCount(source.regularitySamples)
    source.lastAcceptedRepeatMinute =
        Num.positive(source.lastAcceptedRepeatMinute, activeMinute)
    source.qualified = source.qualified == true
    source.awaitingFinalization = source.awaitingFinalization == true
    return source
end

local function newComponent(character, direction, activeMinute)
    return {
        adaptation = PPO.AdaptationMath.initialAdaptation(
            componentLevel(character, direction)),
        adaptationCredit = 0,
        dailyStimulus = 0,
        lastActiveMinute = activeMinute,
        lastQualifiedSessionMinute = activeMinute,
        lastSessionStartedMinute = activeMinute,
        lastSessionCompletedMinute = activeMinute,
        adaptationGraceRemaining = Num.positive(
            adaptationSettings().GraceHours, 72) * 60,
        session = newSession(activeMinute),
        tone = newTone(),
        course = newCourse(),
    }
end

local function normalizeComponent(source, character, direction, activeMinutes)
    if type(source) ~= "table" then
        return newComponent(character, direction, activeMinutes)
    end

    if Num.finite(source.adaptation, nil) == nil then
        source.adaptation = PPO.AdaptationMath.initialAdaptation(
            componentLevel(character, direction))
    end
    source.adaptation = Num.clamp(source.adaptation, 0, 1)
    source.adaptationCredit = Num.positive(source.adaptationCredit, 0)
    source.dailyStimulus = Num.positive(source.dailyStimulus, 0)
    source.lastActiveMinute = Num.clamp(
        Num.positive(source.lastActiveMinute, activeMinutes), 0, activeMinutes)
    source.lastQualifiedSessionMinute = Num.positive(
        source.lastQualifiedSessionMinute, activeMinutes)
    source.lastSessionStartedMinute = Num.positive(
        source.lastSessionStartedMinute, activeMinutes)
    source.lastSessionCompletedMinute = Num.positive(
        source.lastSessionCompletedMinute, activeMinutes)
    if Num.finite(source.adaptationGraceRemaining, nil) == nil then
        source.adaptationGraceRemaining =
            Num.positive(adaptationSettings().GraceHours, 72) * 60
    else
        source.adaptationGraceRemaining =
            math.max(0, source.adaptationGraceRemaining)
    end
    source.tone = normalizeTone(source.tone)
    source.course = normalizeCourse(source.course)
    source.session = normalizeSession(source.session, activeMinutes)
    return source
end

local function newState(character)
    local state = {
        schema = SCHEMA,
        activeMinutes = 0,
        supplements = newSupplements(),
        utility = newUtility(),
    }
    for _, direction in ipairs(DIRECTIONS) do
        state[direction] = newComponent(character, direction, 0)
    end
    return state
end

-- A player's own mod data is not the server's to own. On a dedicated server the
-- client's copy of it is echoed back over the server's within seconds, so every
-- write made between two echoes is discarded: measured live on 2026-08-17, the
-- state object under this key was replaced 38 times in one four-minute session,
-- each time arriving with a session that had never seen a repetition. The minute
-- tick survived that only because it recomputes rather than accumulates.
--
-- Global mod data is server-side and no client can overwrite it, so that is
-- where the authoritative state lives whenever there is a server to own it.
local STATE_KEY = "PhysicalProgressionOverhaul"
local GLOBAL_STORE = "PhysicalProgressionOverhaulPlayers"

local function playerKey(character)
    local ok, name = pcall(function() return character:getUsername() end)
    if not ok or type(name) ~= "string" or name == "" then return nil end
    return name
end

local function globalStore()
    if type(isServer) ~= "function" then return nil end
    local flagOk, server = pcall(isServer)
    if not flagOk or server ~= true then return nil end
    if type(ModData) ~= "table" or type(ModData.getOrCreate) ~= "function" then
        return nil
    end
    local ok, store = pcall(ModData.getOrCreate, GLOBAL_STORE)
    if not ok or type(store) ~= "table" then return nil end
    return store
end

-- Returns the table the state is stored in and the key it is stored under. The
-- character's own mod data is still the holder in single player and on a
-- client, where nothing else writes it and there is no echo to lose it to.
local function holderFor(character)
    local store = globalStore()
    if store ~= nil then
        local key = playerKey(character)
        if key ~= nil then return store, key end
    end
    return character:getModData(), STATE_KEY
end

-- Saves written before the state moved carry it on the character. It is adopted
-- once, on the first read, so a live save keeps its progress; the stale copy is
-- dropped so nothing can read it later by mistake.
local function adoptFromCharacter(character, holder, key)
    if holder == nil or key == STATE_KEY then return nil end
    local ok, modData = pcall(function() return character:getModData() end)
    if not ok or type(modData) ~= "table" then return nil end
    local carried = modData[STATE_KEY]
    if type(carried) ~= "table" or carried.schema ~= SCHEMA then return nil end
    holder[key] = carried
    pcall(function() modData[STATE_KEY] = nil end)
    return carried
end

-- Death is the one event that ends a character's progress for good, and on a
-- dedicated server nothing removes the state by itself: the holder is a global
-- store keyed by the account name, not the character, so the account's next
-- character reads the dead one's load, credit and adaptation. Measured live
-- 2026-08-25 on `PPOTest420Mods`, twice: a brand new character started with
-- `dailyStimulus 0.4562` and credit `0.153`.
--
-- In single player the holder is the character's own mod data, where this is
-- already unreachable; clearing it there costs nothing and keeps one rule for
-- both worlds.
function ExerciseState.discard(character)
    if character == nil or character.getModData == nil then return false end

    local holder, key = holderFor(character)
    if type(holder) ~= "table" or holder[key] == nil then return false end
    holder[key] = nil
    return true
end

function ExerciseState.get(character)
    if character == nil or character.getModData == nil then return nil end

    local holder, key = holderFor(character)
    if type(holder) ~= "table" then return nil end
    local state = holder[key]

    if type(state) ~= "table" then
        state = adoptFromCharacter(character, holder, key)
    end

    if type(state) ~= "table" then
        state = newState(character)
        holder[key] = state
        return state
    end

    -- Any schema but this one is replaced rather than interpreted. There is no
    -- ladder to climb: see the note beside SCHEMA.
    if state.schema ~= SCHEMA then
        state = newState(character)
        holder[key] = state
        return state
    end

    state.activeMinutes = Num.positive(state.activeMinutes, 0)
    state.supplements = normalizeSupplements(state.supplements)
    state.utility = normalizeUtility(state.utility)
    for _, direction in ipairs(DIRECTIONS) do
        state[direction] = normalizeComponent(
            state[direction], character, direction, state.activeMinutes)
    end
    return state
end

function ExerciseState.getSupplements(character)
    local state = ExerciseState.get(character)
    if state == nil then return nil end
    return state.supplements
end

function ExerciseState.getUtility(character)
    local state = ExerciseState.get(character)
    if state == nil then return nil end
    return state.utility
end

-- Accumulates the window and stops at one full container: see
-- PPO.WindowState.add. A window is the only thing a Class B utility consumable
-- writes, which is why one function serves every kind.
local WINDOW_FIELD = {
    stimulant = "stimulantMinutes",
    thermogenic = "thermogenicMinutes",
}
local WINDOW_SETTINGS = {
    stimulant = "getStimulantSettings",
    thermogenic = "getThermogenicSettings",
}

function ExerciseState.extendWindow(character, kind, servings)
    local field = WINDOW_FIELD[kind]
    if field == nil then return nil end
    local utility = ExerciseState.getUtility(character)
    if utility == nil then return nil end
    local settings = PPO.Config[WINDOW_SETTINGS[kind]]()
    utility[field] = PPO.WindowState.add(utility[field], servings,
        settings.WindowHours, settings.ContainerServings)
    return utility[field]
end

function ExerciseState.getComponent(character, direction)
    if not PPO.Directions.supported(direction) then return nil end
    local state = ExerciseState.get(character)
    if state == nil then return nil end
    return state[direction]
end

function ExerciseState.getCourse(character, direction)
    local component = ExerciseState.getComponent(character, direction)
    if component == nil then return nil end
    return component.course
end

-- Tone refreshes, it never stacks: a new session may raise the quality or
-- extend the remaining minutes, never add to either.
function ExerciseState.setTone(character, direction, quality, minutesRemaining)
    local component = ExerciseState.getComponent(character, direction)
    if component == nil then return nil end

    local tone = component.tone
    local newQuality = Num.clamp(Num.positive(quality, 0), 0, 1)
    local newMinutes = Num.positive(minutesRemaining, 0)
    tone.quality = math.max(tone.quality, newQuality)
    tone.minutesRemaining = math.max(tone.minutesRemaining, newMinutes)
    if tone.minutesRemaining <= 0 then tone.quality = 0 end
    return tone
end

-- Only the active-minute engine tick may call this, so offline time removes no
-- tone.
function ExerciseState.advanceTone(character, direction, elapsedMinutes)
    local component = ExerciseState.getComponent(character, direction)
    if component == nil then return nil end

    local tone = component.tone
    local elapsed = Num.positive(elapsedMinutes, 0)
    if elapsed <= 0 then return tone end

    tone.minutesRemaining = math.max(0, tone.minutesRemaining - elapsed)
    if tone.minutesRemaining <= 0 then
        tone.minutesRemaining = 0
        tone.quality = 0
    end
    return tone
end

-- loadReturn is the underlying recovery state and never follows the Sandbox XP
-- override; bonusReturn is the XP-facing value that does.
function ExerciseState.snapshot(character, decayEnabled)
    local state = ExerciseState.get(character)
    if state == nil then return nil end

    local result = {}
    for _, direction in ipairs(DIRECTIONS) do
        local component = state[direction]
        local loadReturn = PPO.BonusMath.bonusReturn(
            component.dailyStimulus, true)
        local bonusReturn = loadReturn
        if decayEnabled == false then bonusReturn = 1 end
        result[direction] = {
            dailyStimulus = component.dailyStimulus,
            loadReturn = loadReturn,
            bonusReturn = bonusReturn,
            adaptation = component.adaptation,
            adaptationCredit = component.adaptationCredit,
        }
    end
    return result
end

function ExerciseState.applyAcceptedRepeat(character, stimulus)
    local state = ExerciseState.get(character)
    if state == nil or type(stimulus) ~= "table" then return false end

    for _, direction in ipairs(DIRECTIONS) do
        local addition = Num.positive(stimulus[direction], 0)
        state[direction].dailyStimulus =
            state[direction].dailyStimulus + addition
    end
    return true
end

-- Replaces one direction's accumulator with a clean session. The empty session
-- is what makes a finalized credit transaction idempotent.
function ExerciseState.resetSession(character, direction, anchorMinute)
    local component = ExerciseState.getComponent(character, direction)
    if component == nil then return false end

    local state = ExerciseState.get(character)
    component.session = newSession(
        Num.positive(anchorMinute, state.activeMinutes))
    return true
end

function ExerciseState.clearIncompleteSessions(character)
    local state = ExerciseState.get(character)
    if state == nil then return false end

    for _, direction in ipairs(DIRECTIONS) do
        state[direction].session = newSession(state.activeMinutes)
        state[direction].tone = newTone()
    end
    return true
end

-- The course empties into a debt of the same size, and both halves of that
-- exchange live here. Whether the debt is charged at all is a server decision,
-- so the two paths are two functions rather than one function with a flag: the
-- charged one is arithmetic on four fields, the released one is the assertion
-- that none of them carries anything.
--
-- The end-of-course condition is shared by both, and deliberately so: the peaks
-- record what one course was worth, not what is owed for it, and a later course
-- has to be able to record its own either way.
local function courseEnded(course)
    return course.level <= 0 and course.active <= 0 and course.activePeak > 0
end

local function advanceCourseDebt(course, elapsed)
    -- The existing debt is repaid before a new one is charged: minutes
    -- cannot repay a debt that was not owed during them. Repayment waits
    -- out the freeze first, so the bottom is a pause rather than a corner.
    local multiplierSettings = PPO.Config.Multiplier
    local repaying = elapsed
    if course.withdrawalFreeze > 0 then
        local held = math.min(course.withdrawalFreeze, repaying)
        course.withdrawalFreeze = course.withdrawalFreeze - held
        repaying = repaying - held
    end
    course.withdrawal = PPO.SupplementState.advance(
        course.withdrawal, repaying, 1,
        multiplierSettings.WithdrawalDecayDays * 24)

    -- The felt debt chases the accounted one exactly as the felt course
    -- chases its reservoir. Charged below rather than above, so the tick
    -- that incurs a debt is never the tick that already feels it.
    course.withdrawalActive = PPO.SupplementState.chase(
        course.withdrawalActive, course.withdrawal, elapsed,
        multiplierSettings.WithdrawalOnsetHours)
    -- Stopping costs what the course was worth at its deepest felt point,
    -- charged only once both the reservoir and the effect are gone, so the
    -- crash never overlaps the effect it replaces.
    -- Normalized rather than raised through the reservoir API: the debt is
    -- not a reservoir, and the dispatcher stays the only module that
    -- raises one.
    if courseEnded(course) then
        course.withdrawal = PPO.SupplementState.normalize(
            course.withdrawal + course.activePeak)
        -- The freeze is measured from the whole debt, not from the part
        -- just added, so a relapse deepens the hold along with the hole.
        -- Half of it is the descent -- the chase needs exactly
        -- `withdrawal * WithdrawalOnsetHours` to cover that depth -- and
        -- the other half is the pause the boost never needed.
        course.withdrawalFreeze = multiplierSettings.WithdrawalHoldFactor
            * course.withdrawal * multiplierSettings.WithdrawalOnsetHours
            * 60
        course.activePeak = 0
        course.peak = 0
    end
end

-- With the debt turned off there is nothing to repay, nothing to feel and
-- nothing to hold. The three fields are zeroed on every tick rather than merely
-- left uncharged, so a debt written into the save while the option was on is
-- released at once instead of serving out its ten day decay after the player
-- asked for it to stop.
--
-- Cutting at the writer rather than at the readers is what keeps this to one
-- edit: the multiplier ceiling, the tone scale, the recovery scale, the four
-- stat writes, the muscle strain and the panel bar all read these fields and
-- all go quiet on a zero.
local function releaseCourseDebt(course)
    course.withdrawal = 0
    course.withdrawalActive = 0
    course.withdrawalFreeze = 0
    if courseEnded(course) then
        course.activePeak = 0
        course.peak = 0
    end
end

-- `training` names the directions whose action is running right now. They spend
-- their minutes instead of recovering them: the marker still advances, so the
-- minutes are gone rather than banked for a later tick. Without this a session
-- charges 1/30 per game minute and hands 1/960 straight back over the same
-- minutes, which moves every stage boundary off the 10/40/60 grid the model
-- promises.
function ExerciseState.advanceActiveMinutes(character, elapsedMinutes,
        recoveryHours, training)
    local state = ExerciseState.get(character)
    if state == nil then return false end

    local elapsed = Num.positive(elapsedMinutes, 0)
    local hours = math.max(0.001, Num.finite(recoveryHours, 24))
    state.activeMinutes = state.activeMinutes + elapsed

    local supplementSettings = PPO.Config.getSupplementSettings()
    for kind, credit in pairs(SERVING_CREDIT) do
        state.supplements[kind] = PPO.SupplementState.advance(
            state.supplements[kind], elapsed, credit,
            supplementSettings.ServingHours)
    end

    -- Spent on the same active-minute tick the reservoirs decay on, so offline
    -- time costs no window.
    for _, field in pairs(WINDOW_FIELD) do
        state.utility[field] = PPO.WindowState.advance(
            state.utility[field], elapsed)
    end

    -- Read once per tick rather than once per direction: the answer is the same
    -- for both, and the resolver is the only Sandbox read in this loop.
    local chargesDebt = true
    local debtOk, debtEnabled = pcall(PPO.Config.courseWithdrawalEnabled)
    if debtOk then chargesDebt = debtEnabled ~= false end

    local recoverable = loadSettings().RecoverableStimulus
    for _, direction in ipairs(DIRECTIONS) do
        local component = state[direction]
        local activeDelta = math.max(0,
            state.activeMinutes - component.lastActiveMinute)
        -- Checked by type, not against `nil`: an unpassed parameter carries a
        -- leaked stack value, and anything but a table here is not a set of
        -- directions.
        if type(training) ~= "table" or not training[direction] then
            -- Read before this tick advances the course below, so a dose
            -- cannot speed up the very minutes that delivered it. The depths
            -- move by at most one tick's chase, which is nothing at this
            -- resolution.
            local scaled = hours
            local scaleOk, scale = pcall(PPO.CourseCost.recoveryScale,
                component.course.active, component.course.withdrawalActive)
            if scaleOk and type(scale) == "number" then
                scaled = math.max(0.001, hours * scale)
            end
            local recovered = recoverable * activeDelta / (scaled * 60)
            component.dailyStimulus = math.max(0,
                component.dailyStimulus - recovered)
        end
        local course = component.course
        course.level = PPO.SupplementState.advance(
            course.level, elapsed, DOSE_CREDIT,
            supplementSettings.CourseDoseHours)

        -- The felt course chases the reservoir. Accounting is instant; the
        -- effect is not, so a lone dose never reaches its nominal value.
        course.active = PPO.SupplementState.chase(
            course.active, course.level, elapsed,
            supplementSettings.OnsetHours)
        if course.active > course.activePeak then
            course.activePeak = course.active
        end

        if chargesDebt then
            advanceCourseDebt(course, elapsed)
        else
            releaseCourseDebt(course)
        end
        component.lastActiveMinute = state.activeMinutes
    end
    return true
end
