-- LCC observation-only pursuit stall trace for Bandits on B42.20.3.
--
-- v2 proved that repeated pathToLocationF() was cancelling in-flight pathfind
-- requests: after coordinate pursuit throttling, pathfind stalls fell from 64 to
-- zero. v3 removed normal combat-reaction noise and separated the real MP
-- controller from the temporary IsController throttle wrapper.
--
-- v4 follows the source-integrated pursuit throttle in BanditUpdate.lua. The
-- global IsController wrapper is gone, so controller state is read directly from
-- BanditUtils.IsController. This tracer remains observation-only: no target, PFB,
-- state, task or ownership mutation.
if isServer() then return end

require "BanditZombie"

local MARKER = "pursuit-stall-trace-v4"
LCC_BANDITS_PURSUIT_STALL_TRACE = MARKER

local SAMPLE_MS = 250
local STALL_MS = 2500
local MOVE_EPS2 = 0.0009
local BANDIT_MAX_DIST2 = 400
local CLOSE_PURSUIT_DIST2 = 9
local STALE_LOCATION_DIST2 = 0.5625

local samples = setmetatable({}, { __mode = "k" })
local nowMs = getTimestampMs()
local detailBudget = 64
local tickCount = 0
local rebound = false

local stats = {
    updates = 0,
    samples = 0,
    pursuitCandidates = 0,
    actionableSamples = 0,
    reactionSamplesIgnored = 0,
    realControllerFalseSamples = 0,
    zombieStalls = 0,
    pairStalls = 0,
    pathfindStalls = 0,
    walktowardStalls = 0,
    idleStalls = 0,
    turnAlertedStalls = 0,
    otherActionableStalls = 0,
    realControllerFalseStalls = 0,
    pfbLocationStalls = 0,
    pfbCharacterStalls = 0,
    pfbNoneStalls = 0,
    staleLocationStalls = 0,
    zombieResumes = 0,
    resumeNearPlayer = 0,
    banditMoveTaskSamples = 0,
    banditMoveTaskStalls = 0,
    banditMoveTaskResumes = 0,
    errors = 0,
    lateRebinds = 0,
}

local function isBandit(character)
    return character ~= nil
        and instanceof(character, "IsoZombie")
        and character:getVariableBoolean("Bandit") == true
end

local function characterId(character)
    if not character then return "nil" end
    if BanditUtils and type(BanditUtils.GetCharacterID) == "function" then
        local ok, value = pcall(BanditUtils.GetCharacterID, character)
        if ok and value ~= nil then return tostring(value) end
    end
    return tostring(character)
end

local function actionState(character)
    if not character then return "nil" end
    local ok, value = pcall(function() return character:getActionStateName() end)
    if not ok then
        stats.errors = stats.errors + 1
        return "<error>"
    end
    return tostring(value or "<none>")
end

local function isActionableMovementState(state)
    return state == "pathfind"
        or state == "walktoward"
        or state == "idle"
        or state == "turnalerted"
end

local function realIsController(zombie)
    local fn = BanditUtils and BanditUtils.IsController or nil
    if type(fn) ~= "function" then return nil end

    local ok, value = pcall(fn, zombie)
    if not ok then
        stats.errors = stats.errors + 1
        return nil
    end
    return value == true
end

local function nearestBandit(zombie)
    if not BanditZombie or not BanditZombie.CacheLightB then return nil end

    local zx, zy, zz = zombie:getX(), zombie:getY(), zombie:getZ()
    local bestCached, bestDist2 = nil, BANDIT_MAX_DIST2

    for _, cached in pairs(BanditZombie.CacheLightB) do
        if cached and cached.x and cached.y and cached.z then
            local dx = cached.x - zx
            local dy = cached.y - zy
            local dist2 = dx * dx + dy * dy
            if math.abs(cached.z - zz) < 0.8 and dist2 < bestDist2 then
                bestCached = cached
                bestDist2 = dist2
            end
        end
    end

    if not bestCached then return nil end
    local bandit = BanditZombie.Cache and BanditZombie.Cache[bestCached.id] or nil
    if not bandit or not bandit:isAlive() or not isBandit(bandit) then return nil end

    return {
        id = bestCached.id,
        object = bandit,
        x = bandit:getX(),
        y = bandit:getY(),
        z = bandit:getZ(),
        dist2 = bestDist2,
    }
end

local function pfbSnapshot(zombie)
    local out = {goal="Unavailable", x=nil, y=nil, z=nil, targetChar=nil, cancelled=nil}
    local okPfb, pfb = pcall(function() return zombie:getPathFindBehavior2() end)
    if not okPfb or not pfb then
        if not okPfb then stats.errors = stats.errors + 1 end
        return out
    end

    local okChar, goalChar = pcall(function() return pfb:isGoalCharacter() end)
    local okLoc, goalLoc = pcall(function() return pfb:isGoalLocation() end)
    local okNone, goalNone = pcall(function() return pfb:isGoalNone() end)
    if okChar and goalChar then
        out.goal = "Character"
        local okTarget, target = pcall(function() return pfb:getTargetChar() end)
        if okTarget then out.targetChar = target else stats.errors = stats.errors + 1 end
    elseif okLoc and goalLoc then
        out.goal = "Location"
    elseif okNone and goalNone then
        out.goal = "None"
    else
        out.goal = "Other"
    end

    local okX, x = pcall(function() return pfb:getTargetX() end)
    local okY, y = pcall(function() return pfb:getTargetY() end)
    local okZ, z = pcall(function() return pfb:getTargetZ() end)
    local okCancel, cancelled = pcall(function() return pfb:getIsCancelled() end)
    if okX then out.x = x end
    if okY then out.y = y end
    if okZ then out.z = z end
    if okCancel then out.cancelled = cancelled end
    return out
end

local function playerDistance(zombie)
    local player = getSpecificPlayer(0)
    if not player then return math.huge end
    local dx = player:getX() - zombie:getX()
    local dy = player:getY() - zombie:getY()
    return math.sqrt(dx * dx + dy * dy)
end

local function logZombieStall(zombie, sample, bandit, state, controller, pairStalled)
    if detailBudget <= 0 then return end
    detailBudget = detailBudget - 1

    local pfb = pfbSnapshot(zombie)
    local pathBanditDist = nil
    local staleLocation = false
    if pfb.goal == "Location" and pfb.x ~= nil and pfb.y ~= nil then
        local dx = pfb.x - bandit.x
        local dy = pfb.y - bandit.y
        local d2 = dx * dx + dy * dy
        pathBanditDist = math.sqrt(d2)
        staleLocation = d2 > STALE_LOCATION_DIST2
    end

    stats.zombieStalls = stats.zombieStalls + 1
    if pairStalled then stats.pairStalls = stats.pairStalls + 1 end
    if state == "pathfind" then stats.pathfindStalls = stats.pathfindStalls + 1
    elseif state == "walktoward" then stats.walktowardStalls = stats.walktowardStalls + 1
    elseif state == "idle" then stats.idleStalls = stats.idleStalls + 1
    elseif state == "turnalerted" then stats.turnAlertedStalls = stats.turnAlertedStalls + 1
    else stats.otherActionableStalls = stats.otherActionableStalls + 1 end
    if controller == false then stats.realControllerFalseStalls = stats.realControllerFalseStalls + 1 end
    if pfb.goal == "Location" then stats.pfbLocationStalls = stats.pfbLocationStalls + 1 end
    if pfb.goal == "Character" then stats.pfbCharacterStalls = stats.pfbCharacterStalls + 1 end
    if pfb.goal == "None" then stats.pfbNoneStalls = stats.pfbNoneStalls + 1 end
    if staleLocation then stats.staleLocationStalls = stats.staleLocationStalls + 1 end

    print(string.format(
        "[LCC][BanditsPursuitStall][ZOMBIE_STALL] marker=%s zombie=%s stationaryMs=%d state=%s realController=%s bandit=%s dist=%.3f pairStalled=%s banditState=%s canSee=%s playerDist=%.3f pfbGoal=%s pfbCancelled=%s pfbTarget=%s,%s,%s pathBanditDist=%s staleLocation=%s pfbTargetChar=%s",
        MARKER,
        characterId(zombie),
        math.floor(nowMs - (sample.stationarySince or nowMs)),
        state,
        tostring(controller),
        tostring(bandit.id),
        math.sqrt(bandit.dist2),
        tostring(pairStalled),
        actionState(bandit.object),
        tostring(zombie:CanSee(bandit.object)),
        playerDistance(zombie),
        pfb.goal,
        tostring(pfb.cancelled),
        tostring(pfb.x), tostring(pfb.y), tostring(pfb.z),
        pathBanditDist and string.format("%.3f", pathBanditDist) or "nil",
        tostring(staleLocation),
        characterId(pfb.targetChar)
    ))
end

local function updateOrdinaryZombie(zombie, sample)
    local bandit = nearestBandit(zombie)
    if not bandit then
        sample.stationarySince = nowMs
        sample.lastX, sample.lastY = zombie:getX(), zombie:getY()
        sample.banditId = nil
        sample.stalled = false
        return
    end

    local canSee = zombie:CanSee(bandit.object)
    if bandit.dist2 > CLOSE_PURSUIT_DIST2 and not canSee then
        sample.stationarySince = nowMs
        sample.lastX, sample.lastY = zombie:getX(), zombie:getY()
        sample.banditId = bandit.id
        sample.stalled = false
        return
    end

    stats.pursuitCandidates = stats.pursuitCandidates + 1
    local state = actionState(zombie)
    if not isActionableMovementState(state) then
        stats.reactionSamplesIgnored = stats.reactionSamplesIgnored + 1
        sample.stationarySince = nowMs
        sample.lastX, sample.lastY = zombie:getX(), zombie:getY()
        sample.lastBanditX, sample.lastBanditY = bandit.x, bandit.y
        sample.banditId = bandit.id
        sample.stalled = false
        return
    end
    stats.actionableSamples = stats.actionableSamples + 1

    local controller = realIsController(zombie)
    if controller == false then stats.realControllerFalseSamples = stats.realControllerFalseSamples + 1 end

    local zx, zy = zombie:getX(), zombie:getY()
    local moved2 = (zx - sample.lastX) * (zx - sample.lastX) + (zy - sample.lastY) * (zy - sample.lastY)
    local sameBandit = sample.banditId == bandit.id

    local banditMoved2 = math.huge
    if sameBandit and sample.lastBanditX ~= nil and sample.lastBanditY ~= nil then
        local bdx = bandit.x - sample.lastBanditX
        local bdy = bandit.y - sample.lastBanditY
        banditMoved2 = bdx * bdx + bdy * bdy
    end

    if not sameBandit then
        sample.stationarySince = nowMs
        sample.stalled = false
    elseif moved2 > MOVE_EPS2 then
        if sample.stalled then
            stats.zombieResumes = stats.zombieResumes + 1
            if playerDistance(zombie) < 3 then stats.resumeNearPlayer = stats.resumeNearPlayer + 1 end
        end
        sample.stationarySince = nowMs
        sample.stalled = false
    elseif not sample.stalled and nowMs - sample.stationarySince >= STALL_MS then
        local pairStalled = banditMoved2 <= MOVE_EPS2
        sample.stalled = true
        logZombieStall(zombie, sample, bandit, state, controller, pairStalled)
    end

    sample.lastX, sample.lastY = zx, zy
    sample.lastBanditX, sample.lastBanditY = bandit.x, bandit.y
    sample.banditId = bandit.id
end

local function updateBandit(bandit, sample)
    if not Bandit or type(Bandit.GetTask) ~= "function" then return end
    local okTask, task = pcall(Bandit.GetTask, bandit)
    if not okTask then
        stats.errors = stats.errors + 1
        return
    end

    local movingTask = task and (task.action == "Move" or task.action == "GoTo")
    if not movingTask then
        sample.banditMoveSince = nowMs
        sample.banditMoveStalled = false
        sample.lastX, sample.lastY = bandit:getX(), bandit:getY()
        return
    end

    stats.banditMoveTaskSamples = stats.banditMoveTaskSamples + 1
    local bx, by = bandit:getX(), bandit:getY()
    local moved2 = (bx - sample.lastX) * (bx - sample.lastX) + (by - sample.lastY) * (by - sample.lastY)

    if moved2 > MOVE_EPS2 then
        if sample.banditMoveStalled then stats.banditMoveTaskResumes = stats.banditMoveTaskResumes + 1 end
        sample.banditMoveSince = nowMs
        sample.banditMoveStalled = false
    elseif not sample.banditMoveStalled and nowMs - (sample.banditMoveSince or nowMs) >= STALL_MS then
        sample.banditMoveStalled = true
        stats.banditMoveTaskStalls = stats.banditMoveTaskStalls + 1
        if detailBudget > 0 then
            detailBudget = detailBudget - 1
            print(string.format(
                "[LCC][BanditsPursuitStall][BANDIT_MOVE_STALL] marker=%s bandit=%s stationaryMs=%d state=%s task=%s/%s target=%s,%s,%s",
                MARKER,
                characterId(bandit),
                math.floor(nowMs - (sample.banditMoveSince or nowMs)),
                actionState(bandit),
                tostring(task.action), tostring(task.state),
                tostring(task.x), tostring(task.y), tostring(task.z)
            ))
        end
    end

    sample.lastX, sample.lastY = bx, by
end

local function onZombieUpdate(zombie)
    if not zombie or not zombie:isAlive() then return end
    stats.updates = stats.updates + 1

    local sample = samples[zombie]
    if not sample then
        sample = {
            lastSample = 0,
            lastX = zombie:getX(),
            lastY = zombie:getY(),
            stationarySince = nowMs,
            banditMoveSince = nowMs,
            stalled = false,
            banditMoveStalled = false,
        }
        samples[zombie] = sample
    end

    if nowMs - sample.lastSample < SAMPLE_MS then return end
    sample.lastSample = nowMs
    stats.samples = stats.samples + 1

    if isBandit(zombie) then
        updateBandit(zombie, sample)
    else
        updateOrdinaryZombie(zombie, sample)
    end
end

Events.OnZombieUpdate.Add(onZombieUpdate)

local function onTick()
    nowMs = getTimestampMs()
    tickCount = tickCount + 1
    if rebound or tickCount < 120 then return end

    local okRemove = pcall(function() Events.OnZombieUpdate.Remove(onZombieUpdate) end)
    local okAdd = pcall(function() Events.OnZombieUpdate.Add(onZombieUpdate) end)
    if okAdd then
        rebound = true
        stats.lateRebinds = stats.lateRebinds + 1
        print(string.format(
            "[LCC][BanditsPursuitStall][REBIND] marker=%s tick=%d removeOk=%s addOk=%s controllerSource=BanditUtils.IsController",
            MARKER, tickCount, tostring(okRemove), tostring(okAdd)
        ))
    end
end
Events.OnTick.Add(onTick)

Events.EveryOneMinute.Add(function()
    print(string.format(
        "[LCC][BanditsPursuitStall][SUMMARY] marker=%s updates=%d samples=%d pursuitCandidates=%d actionableSamples=%d reactionSamplesIgnored=%d realControllerFalseSamples=%d zombieStalls=%d pairStalls=%d pathfindStalls=%d walktowardStalls=%d idleStalls=%d turnAlertedStalls=%d otherActionableStalls=%d realControllerFalseStalls=%d pfbLocationStalls=%d pfbCharacterStalls=%d pfbNoneStalls=%d staleLocationStalls=%d zombieResumes=%d resumeNearPlayer=%d banditMoveTaskSamples=%d banditMoveTaskStalls=%d banditMoveTaskResumes=%d errors=%d lateRebinds=%d",
        MARKER,
        stats.updates,
        stats.samples,
        stats.pursuitCandidates,
        stats.actionableSamples,
        stats.reactionSamplesIgnored,
        stats.realControllerFalseSamples,
        stats.zombieStalls,
        stats.pairStalls,
        stats.pathfindStalls,
        stats.walktowardStalls,
        stats.idleStalls,
        stats.turnAlertedStalls,
        stats.otherActionableStalls,
        stats.realControllerFalseStalls,
        stats.pfbLocationStalls,
        stats.pfbCharacterStalls,
        stats.pfbNoneStalls,
        stats.staleLocationStalls,
        stats.zombieResumes,
        stats.resumeNearPlayer,
        stats.banditMoveTaskSamples,
        stats.banditMoveTaskStalls,
        stats.banditMoveTaskResumes,
        stats.errors,
        stats.lateRebinds
    ))
end)

print(string.format(
    "[LCC][BanditsPursuitStall][BOOT] marker=%s sampleMs=%d stallMs=%d actionable=pathfind+walktoward+idle+turnalerted reactionStates=ignored realController=direct observationOnly=true",
    MARKER, SAMPLE_MS, STALL_MS
))
