PPO = PPO or {}
PPO.RepetitionMatcher = PPO.RepetitionMatcher or {}

local Matcher = PPO.RepetitionMatcher
local MAX_QUEUE = 2
local EXCESS_WINDOW_MS = 10000
local EXCESS_LIMIT = 3

local function finitePositive(value)
    return type(value) == "number" and value == value
        and value ~= math.huge and value ~= -math.huge and value > 0
end

local function deepCopy(value)
    if type(value) ~= "table" then return value end
    local result = {}
    for key, nested in pairs(value) do
        result[key] = deepCopy(nested)
    end
    return result
end

local function clearQueues(state)
    state.tokens = {}
    state.evidence = {}
end

local function closeState(state)
    state.open = false
    clearQueues(state)
end

local function recordExcess(state, now)
    local recent = {}
    for _, timestamp in ipairs(state.excessAt) do
        if now - timestamp <= EXCESS_WINDOW_MS then
            table.insert(recent, timestamp)
        end
    end
    table.insert(recent, now)
    state.excessAt = recent
    if #recent >= EXCESS_LIMIT then
        closeState(state)
        return true
    end
    return false
end

local function expectedComponents(token)
    local expected = {}
    for _, component in ipairs({ "Strength", "Fitness" }) do
        local raw = token.rawXp and token.rawXp[component]
        local capped = token.capped and token.capped[component]
        if finitePositive(raw) and not capped then
            table.insert(expected, component)
        end
    end
    return expected
end

local function removeEvidence(state, indices)
    table.sort(indices, function(left, right) return left > right end)
    for _, index in ipairs(indices) do
        table.remove(state.evidence, index)
    end
end

local function tryMatch(state)
    for tokenIndex, token in ipairs(state.tokens) do
        local expected = expectedComponents(token)
        local used = {}
        local matchedIndices = {}

        for _, component in ipairs(expected) do
            local found = nil
            for evidenceIndex, evidence in ipairs(state.evidence) do
                if not used[evidenceIndex]
                        and evidence.generation == token.generation
                        and evidence.component == component
                        and math.abs(evidence.atMs - token.atMs) <= token.ttlMs then
                    found = evidenceIndex
                    break
                end
            end
            if found ~= nil then
                used[found] = true
                table.insert(matchedIndices, found)
            end
        end

        if #matchedIndices == #expected then
            removeEvidence(state, matchedIndices)
            table.remove(state.tokens, tokenIndex)
            return { kind = "accepted", token = token }
        end
    end
    return nil
end

local function expireOld(state, now)
    local tokens = {}
    local expiredTokens = 0
    for _, token in ipairs(state.tokens) do
        if now - token.atMs > token.ttlMs then
            expiredTokens = expiredTokens + 1
        else
            table.insert(tokens, token)
        end
    end
    state.tokens = tokens

    local evidence = {}
    local expiredEvidence = 0
    for _, item in ipairs(state.evidence) do
        if now - item.atMs > state.ttlMs then
            expiredEvidence = expiredEvidence + 1
            if recordExcess(state, now) then
                return expiredTokens, expiredEvidence, true
            end
        else
            table.insert(evidence, item)
        end
    end
    state.evidence = evidence
    return expiredTokens, expiredEvidence, false
end

function Matcher.new(clock)
    return {
        clock = clock,
        players = {},
    }
end

function Matcher.open(matcher, playerKey, ttlMs)
    local previous = matcher.players[playerKey]
    local generation = 1
    if previous ~= nil then generation = previous.generation + 1 end

    matcher.players[playerKey] = {
        generation = generation,
        open = true,
        ttlMs = finitePositive(ttlMs) and ttlMs or 5000,
        tokens = {},
        evidence = {},
        excessAt = {},
    }
    return generation
end

function Matcher.close(matcher, playerKey)
    local state = matcher.players[playerKey]
    if state == nil then return false end
    closeState(state)
    return true
end

function Matcher.mintToken(matcher, playerKey, sourceToken)
    local state = matcher.players[playerKey]
    if state == nil or not state.open or type(sourceToken) ~= "table" then
        return { kind = "ignored" }
    end

    local now = matcher.clock()
    local _, _, closed = expireOld(state, now)
    if closed then return { kind = "closed" } end

    local token = deepCopy(sourceToken)
    token.generation = state.generation
    token.atMs = now
    if not finitePositive(token.ttlMs) then token.ttlMs = state.ttlMs end

    if #expectedComponents(token) == 0 then
        return { kind = "accepted", token = token }
    end

    if #state.tokens >= MAX_QUEUE then table.remove(state.tokens, 1) end
    table.insert(state.tokens, token)
    local accepted = tryMatch(state)
    if accepted ~= nil then return accepted end
    return { kind = "pending" }
end

function Matcher.observeEvidence(matcher, playerKey, component, amount)
    local state = matcher.players[playerKey]
    if state == nil or not state.open then return { kind = "ignored" } end
    if component ~= "Strength" and component ~= "Fitness" then
        return { kind = "ignored" }
    end
    if not finitePositive(amount) then return { kind = "ignored" } end

    local now = matcher.clock()
    local _, _, closed = expireOld(state, now)
    if closed then return { kind = "closed" } end

    if #state.evidence >= MAX_QUEUE then
        table.remove(state.evidence, 1)
        if recordExcess(state, now) then return { kind = "closed" } end
    end

    table.insert(state.evidence, {
        generation = state.generation,
        component = component,
        atMs = now,
    })
    local accepted = tryMatch(state)
    if accepted ~= nil then return accepted end
    return { kind = "pending" }
end

function Matcher.expire(matcher, playerKey)
    local state = matcher.players[playerKey]
    if state == nil or not state.open then return { kind = "ignored" } end

    local expiredTokens, expiredEvidence, closed =
        expireOld(state, matcher.clock())
    if closed then return { kind = "closed" } end
    return {
        kind = "expired",
        tokenCount = expiredTokens,
        evidenceCount = expiredEvidence,
    }
end

function Matcher.status(matcher, playerKey)
    local state = matcher.players[playerKey]
    if state == nil then
        return {
            generation = 0,
            open = false,
            tokenCount = 0,
            evidenceCount = 0,
            excessCount = 0,
        }
    end
    return {
        generation = state.generation,
        open = state.open,
        tokenCount = #state.tokens,
        evidenceCount = #state.evidence,
        excessCount = #state.excessAt,
    }
end

