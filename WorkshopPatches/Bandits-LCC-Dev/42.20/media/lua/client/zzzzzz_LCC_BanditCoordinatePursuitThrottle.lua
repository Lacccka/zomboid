-- LCC experimental throttle for coordinate-only zombie -> Bandit pursuit.
--
-- The current BanditUpdate PoC replaces pathToCharacter(Bandit) with
-- pathToLocationF(banditX, banditY, banditZ). That removes the unsafe character
-- relation, but BanditUpdate calls the helper from OnZombieUpdate and therefore
-- can issue the same location goal every frame.
--
-- Build 42.20.3 PathFindBehavior2.setData() cancels the current path request.
-- Reissuing an unchanged location every frame can keep a zombie in pathfind
-- without allowing the request to finish. Runtime pursuit-stall tracing confirmed
-- this signature: controller remained true, Goal.Location remained valid, and
-- most sustained stalls were state=pathfind with a destination already at the
-- Bandit's current position.
--
-- Until this logic is folded into the source-clean BanditUpdate override, this
-- wrapper throttles only the IsController() gate used by the local
-- PathZombieToBanditLocation() helper. It suppresses the redundant call when an
-- ordinary zombie already owns an active Goal.Location close to the nearest
-- Bandit and is not idle. A materially moved Bandit, a cancelled/non-location
-- goal, or idle recovery is allowed through.
if isServer() then return end

require "BanditZombie"

if not BanditUtils or type(BanditUtils.IsController) ~= "function" then
    print("[LCC][BanditsPursuitThrottle][ERROR] BanditUtils.IsController unavailable")
    return
end

local MARKER = "coordinate-pursuit-throttle-v1"
LCC_BANDITS_PURSUIT_THROTTLE = MARKER

local ALIGN_DIST2 = 0.5625 -- 0.75 tile
local MAX_BANDIT_DIST2 = 400
local IDLE_RETRY_MS = 750

local originalIsController = BanditUtils.IsController
local idleRetryAt = setmetatable({}, { __mode = "k" })

local stats = {
    calls = 0,
    controllerFalse = 0,
    ordinaryControllerCalls = 0,
    suppressedAligned = 0,
    allowedNoBandit = 0,
    allowedNoPfb = 0,
    allowedNonLocation = 0,
    allowedCancelled = 0,
    allowedStale = 0,
    allowedIdleRecovery = 0,
    idleRetrySuppressed = 0,
    errors = 0,
}

local detailBudget = 24

local function isBandit(character)
    return character ~= nil
        and instanceof(character, "IsoZombie")
        and character:getVariableBoolean("Bandit") == true
end

local function nearestBanditCached(zombie)
    if not BanditZombie or not BanditZombie.CacheLightB then return nil end

    local zx, zy, zz = zombie:getX(), zombie:getY(), zombie:getZ()
    local best, bestDist2 = nil, MAX_BANDIT_DIST2

    for _, cached in pairs(BanditZombie.CacheLightB) do
        if cached and cached.x and cached.y and cached.z then
            local dz = math.abs(zz - cached.z)
            if dz < 0.8 then
                local dx = cached.x - zx
                local dy = cached.y - zy
                local dist2 = dx * dx + dy * dy
                if dist2 < bestDist2 then
                    best = cached
                    bestDist2 = dist2
                end
            end
        end
    end

    return best
end

local function alignedWithBandit(pfb, cached)
    local okX, tx = pcall(function() return pfb:getTargetX() end)
    local okY, ty = pcall(function() return pfb:getTargetY() end)
    local okZ, tz = pcall(function() return pfb:getTargetZ() end)
    if not okX or not okY or not okZ then
        stats.errors = stats.errors + 1
        return false, math.huge
    end

    local dx = tx - cached.x
    local dy = ty - cached.y
    local dz = math.abs(tz - cached.z)
    local dist2 = dx * dx + dy * dy
    return dz < 0.5 and dist2 <= ALIGN_DIST2, dist2
end

BanditUtils.IsController = function(character)
    stats.calls = stats.calls + 1

    local okOriginal, controller = pcall(originalIsController, character)
    if not okOriginal then
        stats.errors = stats.errors + 1
        return false
    end
    if controller ~= true then
        stats.controllerFalse = stats.controllerFalse + 1
        return false
    end

    -- Do not affect Bandits, players or unrelated callers. The current pursuit
    -- helper passes an ordinary IsoZombie here.
    if not character or not instanceof(character, "IsoZombie") or isBandit(character) then
        return true
    end
    stats.ordinaryControllerCalls = stats.ordinaryControllerCalls + 1

    local cached = nearestBanditCached(character)
    if not cached then
        stats.allowedNoBandit = stats.allowedNoBandit + 1
        return true
    end

    local okPfb, pfb = pcall(function() return character:getPathFindBehavior2() end)
    if not okPfb or not pfb then
        stats.allowedNoPfb = stats.allowedNoPfb + 1
        if not okPfb then stats.errors = stats.errors + 1 end
        return true
    end

    local okCancelled, cancelled = pcall(function() return pfb:getIsCancelled() end)
    if okCancelled and cancelled then
        stats.allowedCancelled = stats.allowedCancelled + 1
        return true
    end

    local okLocation, isLocation = pcall(function() return pfb:isGoalLocation() end)
    if not okLocation or not isLocation then
        stats.allowedNonLocation = stats.allowedNonLocation + 1
        if not okLocation then stats.errors = stats.errors + 1 end
        return true
    end

    local aligned, dist2 = alignedWithBandit(pfb, cached)
    if not aligned then
        stats.allowedStale = stats.allowedStale + 1
        return true
    end

    local state = tostring(character:getActionStateName() or "<none>")
    if state == "idle" then
        local now = getTimestampMs()
        local last = idleRetryAt[character] or 0
        if now - last >= IDLE_RETRY_MS then
            idleRetryAt[character] = now
            stats.allowedIdleRecovery = stats.allowedIdleRecovery + 1
            if detailBudget > 0 then
                detailBudget = detailBudget - 1
                print(string.format(
                    "[LCC][BanditsPursuitThrottle][ALLOW_IDLE] marker=%s zombie=%s bandit=%s pathBanditDist=%.3f",
                    MARKER,
                    tostring(BanditUtils.GetCharacterID and BanditUtils.GetCharacterID(character) or character),
                    tostring(cached.id or "nil"),
                    math.sqrt(dist2)
                ))
            end
            return true
        end

        stats.idleRetrySuppressed = stats.idleRetrySuppressed + 1
        return false
    end

    -- The existing goal is already a suitable Bandit coordinate. Returning false
    -- here prevents PathZombieToBanditLocation() from calling pathToLocationF()
    -- again and cancelling the in-flight path request.
    stats.suppressedAligned = stats.suppressedAligned + 1
    return false
end

Events.EveryOneMinute.Add(function()
    print(string.format(
        "[LCC][BanditsPursuitThrottle][SUMMARY] marker=%s calls=%d controllerFalse=%d ordinaryControllerCalls=%d suppressedAligned=%d allowedNoBandit=%d allowedNoPfb=%d allowedNonLocation=%d allowedCancelled=%d allowedStale=%d allowedIdleRecovery=%d idleRetrySuppressed=%d errors=%d",
        MARKER,
        stats.calls,
        stats.controllerFalse,
        stats.ordinaryControllerCalls,
        stats.suppressedAligned,
        stats.allowedNoBandit,
        stats.allowedNoPfb,
        stats.allowedNonLocation,
        stats.allowedCancelled,
        stats.allowedStale,
        stats.allowedIdleRecovery,
        stats.idleRetrySuppressed,
        stats.errors
    ))
end)

print(string.format(
    "[LCC][BanditsPursuitThrottle][BOOT] marker=%s alignDistance=0.75 idleRetryMs=%d mutation=IsController-gate-only characterGoal=false",
    MARKER, IDLE_RETRY_MS
))
