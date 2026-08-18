require "PPO_Config"
require "PPO_MultiplierMath"
require "PPO_BonusMath"
require "PPO_ExerciseDefinitions"
require "PPO_ExerciseState"
require "PPO_RepetitionMatcher"
require "PPO_MultiplierOwnership"
require "PPO_BonusAwarder"
require "PPO_RecoveryContext"
require "PPO_AdaptationEngine"
require "PPO_TrainingSession"

PPO = PPO or {}
PPO.ExerciseAuthority = PPO.ExerciseAuthority or {}

local Authority = PPO.ExerciseAuthority

local function defaultClock()
    return getTimestampMs()
end

local function defaultIssueXp(character, perk, amount)
    addXp(character, perk, amount)
end

-- `Core.getVersionNumber()` is `GameVersion.toString()`, and that formats
-- `"%d.%d%s"` from the major and the minor alone: 42.17 reports `42.17` and
-- 42.20.2 reports `42.20`. The patch number never reaches this string, so the
-- gate can only read two numbers, and a prefix comparison is the wrong tool --
-- `42.170` shares five characters with `42.17` and is a different build.
--
-- The floor is the build the mod was written against. There is no ceiling:
-- the six `ISFitnessAction` methods this file wraps are unchanged between
-- 42.17 and 42.20 (42.20 only adds `forceStop`, which vanilla already routed
-- through `stop`), `Nutrition` and `Fitness` disassemble identically, and
-- `IsoGameCharacter$XP.AddXP` differs only in a `getText` overload. A ceiling
-- turns the next patch into a silent no-op rather than a visible failure. A
-- new major is a different matter and is still refused.
local SUPPORTED_MAJOR = 42
local MINIMUM_MINOR = 17

-- Build 42.20 added `player.isCurrentState(FitnessState.instance())` in front of
-- `Fitness.exerciseRepeat()` inside `StatePacket.processServer()`. A live packet
-- agent observed every `State/Execute` packet on 42.20.2 arriving while the
-- server character is in `IdleState`, so the packet is rejected and no complete
-- repeat survives once the server timer is suppressed. From this minor on, the
-- dedicated server delegates the vanilla timer loop instead.
--
-- 42.17 through 42.19 keep the old flow, and that is not tidiness: their
-- `processServer` has no such guard and performs the repeat unconditionally, so
-- delegating the timer there as well would produce two complete repeats per
-- repetition -- double XP, regularity, endurance drain and stiffness.
local TIMER_AUTHORITY_MINOR = 20

local function versionString()
    if getCore == nil then return nil end
    local ok, version = pcall(function()
        return getCore():getVersionNumber()
    end)
    if not ok or version == nil then return nil end
    return tostring(version)
end

local function buildVersion()
    local version = versionString()
    if version == nil then return nil, nil end
    local major, minor = string.match(version, "^(%d+)%.(%d+)")
    if major == nil or minor == nil then return nil, nil end
    return tonumber(major), tonumber(minor)
end

local function supportedBuild()
    local major, minor = buildVersion()
    if major == nil or minor == nil then return false end
    if major ~= SUPPORTED_MAJOR then return false end
    return minor >= MINIMUM_MINOR
end

local function usesServerTimerAuthority()
    if not isServer() then return false end
    local major, minor = buildVersion()
    if major == nil or minor == nil then return false end
    if major ~= SUPPORTED_MAJOR then return false end
    return minor >= TIMER_AUTHORITY_MINOR
end

-- Every refusal in this file is silent by construction: the wrappers simply do
-- not take, or the session simply does not arm, and the vanilla exercise keeps
-- paying XP and regularity exactly as it would without the mod. From outside,
-- a dead install and a session that drops every repetition are the same
-- picture, and telling them apart cost a whole investigation on a live 42.20.2
-- server.
--
-- A reason is announced once per authority instance, which is once per server
-- process in production. The repetition loop runs every 1.3 to 3 seconds, so a
-- line per call would bury the log; a line per reason is bounded by the number
-- of reasons. Logging never changes behaviour, so a failing `print` is
-- swallowed and not retried -- a log that cannot be written must not turn into
-- a flood of attempts.
local INSTALL_SCOPE = "exercise authority not installed"
local SESSION_SCOPE = "exercise session refused"
local REPEAT_SCOPE = "exercise repetition dropped"

local function reportOnce(authority, scope, reason)
    if authority == nil or type(authority.reported) ~= "table" then
        return false
    end
    local key = scope .. ": " .. reason
    if authority.reported[key] then return false end
    authority.reported[key] = true
    pcall(print, "[PPO] " .. key)
    return true
end

-- The six wrapped methods plus the two globals the authority cannot work
-- without. The first absent one is named, because a reader needs the name to
-- know which mod or which build moved it.
local WRAPPED_SEAMS = {
    "start", "serverStart", "exeLooped", "stop", "perform", "serverStop",
}

local function missingSeam()
    if Events == nil then return "Events" end
    if Events.AddXP == nil then return "Events.AddXP" end
    if ISFitnessAction == nil then return "ISFitnessAction" end
    for _, name in ipairs(WRAPPED_SEAMS) do
        if ISFitnessAction[name] == nil then
            return "ISFitnessAction." .. name
        end
    end
    return nil
end

-- The injection table is checked by type, not against `nil`: an unpassed
-- parameter carries whatever the caller left on the stack, and reading fields
-- off that value is an error rather than a fallback.
function Authority.new(options)
    local settings = {}
    if type(options) == "table" then settings = options end
    local clock = settings.clock or defaultClock
    local instance = {
        clock = clock,
        matcher = PPO.RepetitionMatcher.new(clock),
        ownership = PPO.MultiplierOwnership.new(),
        sessions = {},
        activeByCharacter = {},
        installed = false,
        enabled = false,
        reported = {},
    }
    instance.awarder = PPO.BonusAwarder.new(
        settings.issueXp or defaultIssueXp)
    instance.training = settings.training or PPO.TrainingSession.ensureDefault()
    return instance
end

-- Every abnormal closure path lands here. A closed session freezes its
-- ephemeral training fragment; a network drop or exception must never turn a
-- partial session into credit.
local function closeSession(authority, session)
    if session == nil or not session.open then return false end
    session.open = false
    pcall(PPO.TrainingSession.freezeCharacter,
        authority.training, session.character)
    PPO.RepetitionMatcher.close(authority.matcher, session.character)
    PPO.MultiplierOwnership.releaseAll(
        authority.ownership, session.character)
    if authority.activeByCharacter[session.character] == session then
        authority.activeByCharacter[session.character] = nil
    end
    return true
end

local function closeAction(authority, action)
    local session = authority.sessions[action]
    if session == nil then return false end
    return closeSession(authority, session)
end

local function beginSession(authority, action)
    if isClient ~= nil and isClient() then return false end
    if action == nil or action.character == nil then
        reportOnce(authority, SESSION_SCOPE, "action carries no character")
        return false
    end

    local definition = PPO.ExerciseDefinitions.get(action.exeDataType)
    if definition == nil then
        reportOnce(authority, SESSION_SCOPE,
            "unknown exercise " .. tostring(action.exeDataType))
        return false
    end

    local previous = authority.activeByCharacter[action.character]
    if previous ~= nil then closeSession(authority, previous) end

    local acquired = true
    for _, component in ipairs({ "Strength", "Fitness" }) do
        if definition.spXp[component] ~= nil then
            local perk = Perks[component]
            local level = action.character:getPerkLevel(perk)
            if not PPO.MultiplierOwnership.acquire(
                    authority.ownership, action.character, perk, level) then
                reportOnce(authority, SESSION_SCOPE,
                    "multiplier ownership refused for " .. component)
                acquired = false
                break
            end
        end
    end
    if not acquired then
        PPO.MultiplierOwnership.releaseAll(
            authority.ownership, action.character)
        return false
    end

    local session = {
        action = action,
        character = action.character,
        definition = definition,
        open = true,
        inOriginalLoop = false,
        pendingToken = nil,
        trainingWarningEmitted = false,
        unexpectedXpWarningEmitted = false,
    }
    PPO.RepetitionMatcher.open(
        authority.matcher, action.character, definition.ttlMs)
    authority.sessions[action] = session
    authority.activeByCharacter[action.character] = session
    -- Only an armed action that already owns its multipliers may open a
    -- training fragment.
    pcall(PPO.TrainingSession.beginFragment,
        authority.training, action.character, definition.id)
    return true
end

local function makeToken(authority, session)
    local character = session.character
    local definition = session.definition
    local rawXp = isServer() and definition.mpXp or definition.spXp
    local stateSnapshot = PPO.ExerciseState.snapshot(
        character, PPO.BonusMath.isExerciseBonusDecayEnabled())
    if stateSnapshot == nil then return nil end

    -- One span per repetition, shared by every direction the exercise trains.
    -- The span is measured here and charged only when the repetition is
    -- accepted, so a token that is minted and then never matched leaves its
    -- minutes to the next accepted repetition or to the fragment tail.
    local loadMinutes = 0
    local mintMinute = nil
    local spanOk, span, measuredAt = pcall(PPO.TrainingSession.peekLoadMinutes,
        authority.training, character, definition.periodMs)
    if spanOk then
        loadMinutes = span or 0
        mintMinute = measuredAt
    end

    local token = {
        exerciseId = definition.id,
        ttlMs = definition.ttlMs,
        rawXp = {},
        capped = {},
        fullMultiplier = {},
        bonusReturn = {},
        loadReturn = {},
        stimulus = {},
        loadMinutes = {},
        workMinutes = {},
        mintMinute = mintMinute,
    }
    for _, component in ipairs({ "Strength", "Fitness" }) do
        if rawXp[component] ~= nil then
            local perk = Perks[component]
            local level = character:getPerkLevel(perk)
            local capped = level >= 10
            -- Assembled through the engine's accessor so the awarded number
            -- and the number the skill panel shows come from one formula and
            -- one set of reservoirs.
            local inputs = PPO.AdaptationEngine.multiplierInputs(
                nil, character, component)
            token.rawXp[component] = rawXp[component]
            token.capped[component] = capped
            token.fullMultiplier[component] =
                PPO.AdaptationEngine.multiplierFor(
                    level, inputs, inputs.loadFactor)
            -- bonusReturn drives XP and follows the Sandbox override;
            -- loadReturn is the underlying recovery state used by session
            -- quality and never follows that override.
            token.bonusReturn[component] =
                stateSnapshot[component].bonusReturn
            token.loadReturn[component] =
                stateSnapshot[component].loadReturn
            token.loadMinutes[component] = loadMinutes
            -- A token is exactly one repetition, so its load is the exercise's
            -- constant. The span beside it stays clock time and feeds only the
            -- coverage gate.
            token.stimulus[component] = definition.loadPerRepeat[component]
            token.workMinutes[component] =
                definition.minutesPerRepeat[component]
        end
    end
    return token
end

-- The training layer runs after the vanilla/PPO XP repetition already
-- succeeded, in its own guard. A training failure skips Adaptation input only;
-- it never closes or undoes the accepted repetition.
local function submitTraining(authority, session, token)
    local ok = pcall(PPO.TrainingSession.acceptRepeat,
        authority.training, session.character, token)
    if not ok and not session.trainingWarningEmitted then
        session.trainingWarningEmitted = true
        print("[PPO] training session input failed; "
            .. "adaptation input skipped for this action")
    end
end

-- The span belongs to a repetition only once that repetition is accepted, so it
-- is charged here rather than when the token was minted. The commit recomputes
-- the delta instead of trusting the token's preview, because a preceding
-- rejection widens the span and the token cannot know that. The span no longer
-- buys load -- a repetition's load is its own constant -- so this charges
-- coverage only, and it still has to run before the award so the marker moves
-- exactly once per accepted repetition.
local function commitTokenSpan(authority, session, token)
    if token.mintMinute == nil then return nil end
    local definition = session.definition

    local markerOk, previous = pcall(PPO.TrainingSession.markerMinute,
        authority.training, session.character)
    if not markerOk then previous = nil end

    local ok, charged = pcall(PPO.TrainingSession.commitLoadMinutes,
        authority.training, session.character, token.mintMinute,
        definition.periodMs)
    if not ok then return nil end
    charged = charged or 0

    for _, component in ipairs({ "Strength", "Fitness" }) do
        if token.stimulus[component] ~= nil
                and definition.loadPerRepeat[component] ~= nil then
            token.loadMinutes[component] = charged
        end
    end
    return previous
end

local function acceptToken(authority, session, token)
    local previousMarker = commitTokenSpan(authority, session, token)
    local result = PPO.BonusAwarder.award(
        authority.awarder, session.character, token)
    if result.ok then
        submitTraining(authority, session, token)
    elseif previousMarker ~= nil then
        -- The award failed behind an already committed span. Under-charging is
        -- the safe direction on this path, so the marker goes back.
        pcall(PPO.TrainingSession.rewindLoadMinute,
            authority.training, session.character, previousMarker)
    end
    if result.close then closeSession(authority, session) end
    return result.ok
end

local function handleMatch(authority, session, match)
    if match.kind == "closed" then
        reportOnce(authority, REPEAT_SCOPE, "repetition matcher closed")
        closeSession(authority, session)
        return false
    end
    if match.kind ~= "accepted" then return false end

    if session.inOriginalLoop then
        session.pendingToken = match.token
        return true
    end
    return acceptToken(authority, session, match.token)
end

local function onAddXp(authority, character, perk, amount)
    if not authority.enabled
            or PPO.BonusAwarder.isInternal(authority.awarder) then
        return
    end
    local session = authority.activeByCharacter[character]
    if session == nil or not session.open then return end

    local component = nil
    if perk == Perks.Strength then component = "Strength" end
    if perk == Perks.Fitness then component = "Fitness" end
    if component == nil then return end

    -- Under server timer authority the vanilla repeat runs inside the original
    -- loop, so trained XP arriving outside that window is not this repetition's
    -- evidence. Matching it would pay a bonus for work PPO cannot attribute, so
    -- the session closes instead and the character keeps plain vanilla rates.
    if usesServerTimerAuthority() and not session.inOriginalLoop then
        if not session.unexpectedXpWarningEmitted then
            session.unexpectedXpWarningEmitted = true
            print("[PPO] physical XP arrived outside the server timer loop; "
                .. "exercise authority closed for this action")
        end
        closeSession(authority, session)
        return
    end

    local ok, match = pcall(
        PPO.RepetitionMatcher.observeEvidence,
        authority.matcher, character, component, amount)
    if not ok then
        reportOnce(authority, REPEAT_SCOPE, "evidence matcher failed")
        closeSession(authority, session)
        return
    end
    handleMatch(authority, session, match)
end

local function authorityLoop(authority, action)
    local session = authority.sessions[action]
    if session == nil or not session.open then return false end

    local ok, token = pcall(makeToken, authority, session)
    if not ok then
        reportOnce(authority, REPEAT_SCOPE, "token build failed")
        closeSession(authority, session)
        return false
    end
    if token == nil then
        reportOnce(authority, REPEAT_SCOPE, "state snapshot unavailable")
        closeSession(authority, session)
        return false
    end

    if isServer() and not usesServerTimerAuthority() then
        local matchedOk, match = pcall(
            PPO.RepetitionMatcher.mintToken,
            authority.matcher, session.character, token)
        if not matchedOk then
            reportOnce(authority, REPEAT_SCOPE, "repetition matcher failed")
            closeSession(authority, session)
            return false
        end
        action.repnb = (action.repnb or 0) + 1
        action:setFitnessSpeed()
        handleMatch(authority, session, match)
        return true
    end

    local matchedOk, match = pcall(
        PPO.RepetitionMatcher.mintToken,
        authority.matcher, session.character, token)
    if not matchedOk then
        reportOnce(authority, REPEAT_SCOPE, "repetition matcher failed")
        closeSession(authority, session)
        return false
    end
    if match.kind == "closed" then
        reportOnce(authority, REPEAT_SCOPE, "repetition matcher closed")
        closeSession(authority, session)
        return false
    end
    if match.kind == "accepted" then
        session.pendingToken = match.token
    end
    return true
end

function Authority.isSessionOpen(authority, action)
    local session = authority.sessions[action]
    return session ~= nil and session.open == true
end

function Authority.closeActiveCharacter(character)
    local authority = Authority.ActiveInstance
    if authority == nil then return false end
    return closeSession(authority, authority.activeByCharacter[character])
end

function Authority.refreshActiveLevel(character, perk, level, applyMultiplier)
    local authority = Authority.ActiveInstance
    if authority == nil or not authority.enabled then return false end
    local session = authority.activeByCharacter[character]
    if session == nil or not session.open then return false end

    local component = nil
    if perk == Perks.Strength then component = "Strength" end
    if perk == Perks.Fitness then component = "Fitness" end
    if component == nil or session.definition.spXp[component] == nil then
        return false
    end

    if not PPO.MultiplierOwnership.release(
            authority.ownership, character, perk) then
        closeSession(authority, session)
        return true
    end
    local applied = pcall(applyMultiplier)
    if not applied or not PPO.MultiplierOwnership.acquire(
            authority.ownership, character, perk, level) then
        closeSession(authority, session)
    end
    return true
end

function Authority.install(authority)
    if authority == nil or authority.installed then return false end
    if Authority.ActiveInstance ~= nil then
        reportOnce(authority, INSTALL_SCOPE,
            "another authority instance is already active")
        return false
    end
    if not PPO.Config.Runtime.Enabled then
        reportOnce(authority, INSTALL_SCOPE, "runtime disabled")
        return false
    end
    if not supportedBuild() then
        reportOnce(authority, INSTALL_SCOPE,
            "unsupported build " .. (versionString() or "unreadable"))
        return false
    end
    local absent = missingSeam()
    if absent ~= nil then
        reportOnce(authority, INSTALL_SCOPE, "vanilla seam missing: " .. absent)
        return false
    end

    authority.originals = {
        start = ISFitnessAction.start,
        serverStart = ISFitnessAction.serverStart,
        exeLooped = ISFitnessAction.exeLooped,
        stop = ISFitnessAction.stop,
        perform = ISFitnessAction.perform,
        serverStop = ISFitnessAction.serverStop,
    }

    -- Build 42.17 standalone actions enter through start(); serverStart() is
    -- the network lifecycle and is not called in ordinary single-player.
    ISFitnessAction.start = function(action)
        local armed = false
        if not isClient() and not isServer() then
            local ok = pcall(function()
                armed = beginSession(authority, action)
            end)
            if not ok then armed = false end
        end

        local originalOk, errorValue = pcall(
            authority.originals.start, action)
        if not originalOk then
            if armed then closeAction(authority, action) end
            error(errorValue)
        end
    end

    ISFitnessAction.serverStart = function(action)
        local armed = false
        local ok = pcall(function()
            armed = beginSession(authority, action)
        end)
        if not ok then armed = false end

        local originalOk, errorValue = pcall(
            authority.originals.serverStart, action)
        if not originalOk then
            if armed then closeAction(authority, action) end
            error(errorValue)
        end
    end

    ISFitnessAction.exeLooped = function(action)
        local session = authority.sessions[action]
        if session == nil or not session.open then
            return authority.originals.exeLooped(action)
        end

        if isServer() and not usesServerTimerAuthority() then
            if authorityLoop(authority, action) then return end
            return authority.originals.exeLooped(action)
        end

        local prepared = authorityLoop(authority, action)
        if not prepared then return authority.originals.exeLooped(action) end

        session.inOriginalLoop = true
        local originalOk, errorValue = pcall(
            authority.originals.exeLooped, action)
        session.inOriginalLoop = false
        if not originalOk then
            closeSession(authority, session)
            error(errorValue)
        end
        if session.pendingToken ~= nil and session.open then
            local pendingToken = session.pendingToken
            session.pendingToken = nil
            acceptToken(authority, session, pendingToken)
        end
    end

    -- A normal action end is the only path allowed to finalize credit, exactly
    -- once, before the authority session closes.
    local function wrapFinish(methodName)
        ISFitnessAction[methodName] = function(action)
            local originalOk, errorValue = pcall(
                authority.originals[methodName], action)
            local session = authority.sessions[action]
            if session ~= nil and session.open then
                pcall(PPO.TrainingSession.finishFragment,
                    authority.training, session.character, true)
            end
            closeAction(authority, action)
            if not originalOk then error(errorValue) end
        end
    end
    wrapFinish("stop")
    wrapFinish("perform")
    wrapFinish("serverStop")

    authority.addXpHandler = function(character, perk, amount)
        onAddXp(authority, character, perk, amount)
    end
    Events.AddXP.Add(authority.addXpHandler)
    authority.installed = true
    authority.enabled = true
    Authority.ActiveInstance = authority
    -- An absent refusal proves nothing by itself: a build of the mod that
    -- predates those refusals is silent for the same reason a healthy one is.
    -- This line is the one thing a reader can point at to say which version
    -- the server actually loaded, which matters because a Workshop copy can
    -- lag behind the tree the payload was installed from.
    pcall(print, "[PPO] exercise authority installed on build "
        .. (versionString() or "unreadable"))
    return true
end

function Authority.uninstall(authority)
    if authority == nil or not authority.installed then return false end
    authority.enabled = false

    local actions = {}
    for action in pairs(authority.sessions) do table.insert(actions, action) end
    for _, action in ipairs(actions) do closeAction(authority, action) end

    ISFitnessAction.start = authority.originals.start
    ISFitnessAction.serverStart = authority.originals.serverStart
    ISFitnessAction.exeLooped = authority.originals.exeLooped
    ISFitnessAction.stop = authority.originals.stop
    ISFitnessAction.perform = authority.originals.perform
    ISFitnessAction.serverStop = authority.originals.serverStop
    if Events.AddXP.Remove ~= nil and authority.addXpHandler ~= nil then
        Events.AddXP.Remove(authority.addXpHandler)
    end
    authority.addXpHandler = nil
    authority.installed = false
    if Authority.ActiveInstance == authority then
        Authority.ActiveInstance = nil
    end
    return true
end

function Authority.ensureInstalled()
    if Authority.Default == nil then
        Authority.Default = Authority.new(nil)
    end
    if Authority.Default.installed then return true end
    return Authority.install(Authority.Default)
end
