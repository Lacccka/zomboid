-- LCC observation-only outcome trace for the Bandits custom Bite/BiteLow path.
--
-- BanditUpdate owns a local biteTab that cannot be inspected from another Lua
-- file. It does expose two reliable runtime signals, though:
--   zombie:getModData().zid is set while the manual bite pipeline is active;
--   BumpType is Bite/BiteLow and action state becomes "bumped".
--
-- At biteTab tick 14 upstream calls bandit:Hit(...), then syncs Bandit health.
-- This observer follows that active window and reports whether Bandit health
-- actually decreases. It never changes pathing, bump state, target or health.
if isServer() then return end

local MARKER = "bite-outcome-trace-v1"
LCC_BANDITS_BITE_OUTCOME_TRACE = MARKER

local stats = {
    updates = 0,
    activeStarts = 0,
    activeSamples = 0,
    activeEnds = 0,
    banditResolved = 0,
    banditResolveMisses = 0,
    healthDropEvents = 0,
    successfulWindows = 0,
    noHealthDropWindows = 0,
    shortWindows = 0,
    maxActiveTicks = 0,
}

local stateByZombie = setmetatable({}, { __mode = "k" })
local detailBudget = 30

local function isBandit(character)
    if not character or not instanceof(character, "IsoZombie") then return false end
    local ok, value = pcall(function() return character:getVariableBoolean("Bandit") end)
    return ok and value == true
end

local function characterId(character)
    if not character then return "nil" end
    if BanditUtils and type(BanditUtils.GetCharacterID) == "function" then
        local ok, value = pcall(BanditUtils.GetCharacterID, character)
        if ok and value ~= nil then return tostring(value) end
    end
    local ok, value = pcall(function() return character:getPersistentOutfitID() end)
    return ok and value ~= nil and tostring(value) or "unknown"
end

local function healthOf(character)
    if not character then return nil end
    local ok, value = pcall(function() return character:getHealth() end)
    return ok and tonumber(value) or nil
end

local function nearestBandit(zombie, maxDist2)
    if not zombie or not BanditZombie or type(BanditZombie.CacheLightB) ~= "table" then return nil end
    local zx, zy, zz = zombie:getX(), zombie:getY(), zombie:getZ()
    local best, bestDist2
    for id, cached in pairs(BanditZombie.CacheLightB) do
        if cached and cached.x and cached.y and cached.z and math.abs(zz - cached.z) < 0.5 then
            local dx = cached.x - zx
            local dy = cached.y - zy
            local dist2 = dx * dx + dy * dy
            if dist2 <= maxDist2 and (bestDist2 == nil or dist2 < bestDist2) then
                local actual = BanditZombie.Cache and BanditZombie.Cache[id] or nil
                if isBandit(actual) then
                    best = actual
                    bestDist2 = dist2
                end
            end
        end
    end
    return best, bestDist2
end

local function detail(kind, zombie, state, reason)
    if detailBudget <= 0 then return end
    detailBudget = detailBudget - 1
    local delta = state and state.startHealth and state.minHealth and (state.startHealth - state.minHealth) or 0
    print(string.format(
        "[LCC][BanditsBiteOutcome][%s] marker=%s zombie=%s bandit=%s ticks=%d startHealth=%s minHealth=%s delta=%.4f state=%s bump=%s zid=%s reason=%s",
        kind,
        MARKER,
        characterId(zombie),
        state and characterId(state.bandit) or "nil",
        state and state.ticks or 0,
        state and tostring(state.startHealth) or "nil",
        state and tostring(state.minHealth) or "nil",
        tonumber(delta) or 0,
        tostring(zombie and zombie:getActionStateName() or "<none>"),
        tostring(zombie and zombie:getBumpType() or "<none>"),
        tostring(zombie and zombie:getModData().zid or "nil"),
        tostring(reason or "")
    ))
end

local function finishWindow(zombie, state, reason)
    if not state then return end
    stats.activeEnds = stats.activeEnds + 1
    if state.ticks < 14 then stats.shortWindows = stats.shortWindows + 1 end
    local delta = 0
    if state.startHealth ~= nil and state.minHealth ~= nil then
        delta = state.startHealth - state.minHealth
    end
    if delta > 0 then
        stats.successfulWindows = stats.successfulWindows + 1
    else
        stats.noHealthDropWindows = stats.noHealthDropWindows + 1
    end
    detail("END", zombie, state, reason)
end

local function onZombieUpdate(zombie)
    if not zombie or isBandit(zombie) then return end
    stats.updates = stats.updates + 1

    local md = zombie:getModData()
    local bump = zombie:getBumpType()
    local asn = zombie:getActionStateName()
    local activeSignal = md and md.zid ~= nil
        and (bump == "Bite" or bump == "BiteLow")
        and asn == "bumped"

    local state = stateByZombie[zombie]
    if not activeSignal then
        if state then
            finishWindow(zombie, state, md and md.zid == nil and "zid-cleared" or "pipeline-signal-lost")
            stateByZombie[zombie] = nil
        end
        return
    end

    if not state then
        local bandit = nearestBandit(zombie, 1.44)
        if not bandit then
            stats.banditResolveMisses = stats.banditResolveMisses + 1
            return
        end
        stats.banditResolved = stats.banditResolved + 1
        local health = healthOf(bandit)
        state = {
            bandit = bandit,
            ticks = 0,
            startHealth = health,
            minHealth = health,
            sawDrop = false,
        }
        stateByZombie[zombie] = state
        stats.activeStarts = stats.activeStarts + 1
        detail("START", zombie, state, "bumped-bite-active")
    end

    stats.activeSamples = stats.activeSamples + 1
    state.ticks = state.ticks + 1
    if state.ticks > stats.maxActiveTicks then stats.maxActiveTicks = state.ticks end

    local health = healthOf(state.bandit)
    if health ~= nil then
        if state.minHealth == nil or health < state.minHealth then
            state.minHealth = health
        end
        if state.startHealth ~= nil and health < state.startHealth and not state.sawDrop then
            state.sawDrop = true
            stats.healthDropEvents = stats.healthDropEvents + 1
            detail("HEALTH_DROP", zombie, state, "bandit-health-decreased")
        end
    end
end

Events.OnZombieUpdate.Add(onZombieUpdate)

Events.EveryOneMinute.Add(function()
    print(string.format(
        "[LCC][BanditsBiteOutcome][SUMMARY] marker=%s updates=%d activeStarts=%d activeSamples=%d activeEnds=%d banditResolved=%d banditResolveMisses=%d healthDropEvents=%d successfulWindows=%d noHealthDropWindows=%d shortWindows=%d maxActiveTicks=%d",
        MARKER,
        stats.updates,
        stats.activeStarts,
        stats.activeSamples,
        stats.activeEnds,
        stats.banditResolved,
        stats.banditResolveMisses,
        stats.healthDropEvents,
        stats.successfulWindows,
        stats.noHealthDropWindows,
        stats.shortWindows,
        stats.maxActiveTicks
    ))
end)

print(string.format(
    "[LCC][BanditsBiteOutcome][BOOT] marker=%s mode=observe-only signal=zid+Bite+Bumped outcome=bandit-health-delta mutation=false",
    MARKER
))
