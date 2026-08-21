-- LCC observation-only pursuit stall trace for Bandits on B42.20.3.
--
-- The coordinate-only combat PoC intentionally avoids pathToCharacter(Bandit),
-- vanilla target(Bandit), spotted(Bandit) and addAggro(Bandit). Runtime testing
-- showed a separate symptom where an ordinary zombie and/or Bandit can appear to
-- stand still until the local player approaches or the Bandit starts a new task.
--
-- This tracer does NOT alter movement, targets, PathFindBehavior2, tasks or
-- ownership. It samples each entity only four times per second and keeps the
-- OnZombieUpdate hot path limited to table lookup/time gating plus isAlive().
-- Expensive PFB/task snapshots are taken only when a sustained stall is detected.
if isServer() then return end

require "BanditZombie"

local MARKER = "pursuit-stall-trace-v2"
LCC_BANDITS_PURSUIT_STALL_TRACE = MARKER

local SAMPLE_MS = 250
local STALL_MS = 2500
local MOVE_EPS2 = 0.0009            -- ~0.03 tile between samples
local BANDIT_MAX_DIST2 = 400        -- 20 tiles, same combat-search envelope
local CLOSE_PURSUIT_DIST2 = 9       -- <=3 tiles is always pursued by current PoC
local STALE_LOCATION_DIST2 = 0.5625 -- path destination >0.75 tile behind Bandit
local MOVE_TASK_TARGET_DIST2 = 0.5625

local samples = setmetatable({}, { __mode = "k" })
local detailBudget = 96
local nowMs = getTimestampMs()
local tickCount = 0
local rebound = false

local stats = {
    updates = 0,
    samples = 0,
    pursuitCandidates = 0,
    playerNearSamples = 0,
    controllerFalseSamples = 0,
    zombieStalls = 0,
    pairStalls = 0,
    closeStalls = 0,
    turnAlertedStalls = 0,
    pathfindStalls = 0,
    idleStalls = 0,
    controllerFalseStalls = 0,
    pfbNoneStalls = 0,
    pfbLocationStalls = 0,
    pfbCharacterStalls = 0,
    staleLocationStalls = 0,
    zombieResumes = 0,
    resumeNearPlayer = 0,
    banditMoveTaskSamples = 0,
    banditMoveTaskStalls = 0,
    banditMoveTaskResumes = 0,
    errors = 0,
    lateRebinds = 0,
}

local function optionalCall(default, fn)
    local ok, value = pcall(fn)
    if ok then return value end
    return default
end

local function characterId(character)
    if not character then return "nil" end
    if BanditUtils and type(BanditUtils.GetCharacterID) == "function" then
        local ok, value = pcall(BanditUtils.GetCharacterID, character)
        if ok and value ~= nil then return tostring(value) end
    end
    local value = optionalCall(nil, function() return character:getPersistentOutfitID() end)
    return value ~= nil and tostring(value) or "unknown"
end

local function actionState(character)
    if not character then return "nil" end
    local value = optionalCall(nil, function() return character:getActionStateName() end)
    return value ~= nil and tostring(value) or "<none>"
end

local function isController(character)
    if not character or not BanditUtils or type(BanditUtils.IsController) ~= "function" then
        return nil
    end
    local ok, value = pcall(BanditUtils.IsController, character)
    if not ok then
        stats.errors = stats.errors + 1
        return nil
    end
    return value == true
end

local function playerSnapshot(character)
    local player = getSpecificPlayer(0)
    if not player then return math.huge, false, false end

    local dx = player:getX() - character:getX()
    local dy = player:getY() - character:getY()
    local dist2 = dx * dx + dy * dy
    local ghost = optionalCall(false, function() return player:isGhostMode() end) == true
    local invisible = optionalCall(false, function() return player:isInvisible() end) == true
    return dist2, ghost, invisible
end

local function nearestBandit(zombie)
    if not BanditZombie or not BanditZombie.CacheLightB then return nil end

    local zx, zy, zz = zombie:getX(), zombie:getY(), zombie:getZ()
    local bestCached, bestDist2 = nil, BANDIT_MAX_DIST2

    for _, cached in pairs(BanditZombie.CacheLightB) do
        if cached and cached.x and cached.y then
            local dx = cached.x - zx
            local dy = cached.y - zy
            local dist2 = dx * dx + dy * dy
            if dist2 < bestDist2 then
                bestCached = cached
                bestDist2 = dist2
            end
        end
    end

    if not bestCached then return nil end

    local id = bestCached.id
    local bandit = id and BanditZombie.Cache and BanditZombie.Cache[id] or nil
    if not bandit or not bandit:isAlive() or not bandit:getVariableBoolean("Bandit") then
        return nil
    end

    local bx, by, bz = bandit:getX(), bandit:getY(), bandit:getZ()
    return {
        id = id,
        object = bandit,
        x = bx,
        y = by,
        z = bz,
        dist2 = (bx - zx) * (bx - zx) + (by - zy) * (by - zy),
        zDelta = math.abs(zz - bz),
    }
end

local function pfbSnapshot(character)
    local pfb = optionalCall(nil, function() return character:getPathFindBehavior2() end)
    if not pfb then
        return { goal = "unavailable" }
    end

    local out = {
        goal = "Other",
        cancelled = optionalCall(nil, function() return pfb:getIsCancelled() end),
        stopping = optionalCall(nil, function() return pfb.stopping end),
        x = optionalCall(nil, function() return pfb:getTargetX() end),
        y = optionalCall(nil, function() return pfb:getTargetY() end),
        z = optionalCall(nil, function() return pfb:getTargetZ() end),
        targetChar = nil,
    }

    if optionalCall(false, function() return pfb:isGoalCharacter() end) then
        out.goal = "Character"
        out.targetChar = optionalCall(nil, function() return pfb:getTargetChar() end)
    elseif optionalCall(false, function() return pfb:isGoalLocation() end) then
        out.goal = "Location"
    elseif optionalCall(false, function() return pfb:isGoalSound() end) then
        out.goal = "Sound"
    elseif optionalCall(false, function() return pfb:isGoalNone() end) then
        out.goal = "None"
    end

    return out
end

local function banditTaskSnapshot(bandit)
    if not Bandit or type(Bandit.GetTask) ~= "function" then return nil end

    local ok, task = pcall(Bandit.GetTask, bandit)
    if not ok then
        stats.errors = stats.errors + 1
        return nil
    end
    if not task then return nil end

    return {
        action = tostring(task.action or "<none>"),
        state = tostring(task.state or "<none>"),
        time = tonumber(task.time),
        x = tonumber(task.x),
        y = tonumber(task.y),
        z = tonumber(task.z),
    }
end

local function fmtNumber(value)
    return value ~= nil and string.format("%.3f", tonumber(value) or -9999) or "nil"
end

local function logZombieStall(zombie, sample, bandit, controller, playerDist2, playerGhost, playerInvisible, canSee, pairStalled)
    stats.zombieStalls = stats.zombieStalls + 1

    local pfb = pfbSnapshot(zombie)
    local task = banditTaskSnapshot(bandit.object)
    local state = actionState(zombie)
    local luaTarget = optionalCall(nil, function() return zombie:getTarget() end)
    local hitBy = optionalCall(nil, function() return zombie:getAttackedBy() end)
    local allowRepathDelay = optionalCall(nil, function() return zombie.allowRepathDelay end)

    local staleLocation = false
    local pathBanditDist2 = nil
    if pfb.goal == "Location" and pfb.x ~= nil and pfb.y ~= nil then
        local dx = pfb.x - bandit.x
        local dy = pfb.y - bandit.y
        pathBanditDist2 = dx * dx + dy * dy
        staleLocation = pathBanditDist2 > STALE_LOCATION_DIST2
    end

    if pairStalled then stats.pairStalls = stats.pairStalls + 1 end
    if bandit.dist2 <= CLOSE_PURSUIT_DIST2 then stats.closeStalls = stats.closeStalls + 1 end
    if state == "turnalerted" then stats.turnAlertedStalls = stats.turnAlertedStalls + 1 end
    if state == "pathfind" then stats.pathfindStalls = stats.pathfindStalls + 1 end
    if state == "idle" then stats.idleStalls = stats.idleStalls + 1 end
    if controller == false then stats.controllerFalseStalls = stats.controllerFalseStalls + 1 end
    if pfb.goal == "None" then stats.pfbNoneStalls = stats.pfbNoneStalls + 1 end
    if pfb.goal == "Location" then stats.pfbLocationStalls = stats.pfbLocationStalls + 1 end
    if pfb.goal == "Character" then stats.pfbCharacterStalls = stats.pfbCharacterStalls + 1 end
    if staleLocation then stats.staleLocationStalls = stats.staleLocationStalls + 1 end

    if detailBudget <= 0 then return end
    detailBudget = detailBudget - 1

    print(string.format(
        "[LCC][BanditsPursuitStall][ZOMBIE_STALL] marker=%s zombie=%s stationaryMs=%d state=%s controller=%s allowRepathDelay=%s bandit=%s dist=%.3f pairStalled=%s banditState=%s banditTask=%s/%s taskTime=%s canSee=%s playerDist=%.3f playerGhost=%s playerInvisible=%s pfbGoal=%s pfbCancelled=%s pfbStopping=%s pfbTarget=%s,%s,%s pathBanditDist=%s staleLocation=%s pfbTargetChar=%s luaTarget=%s attackedBy=%s",
        MARKER,
        characterId(zombie),
        math.floor(sample.stationaryMs or 0),
        state,
        tostring(controller),
        tostring(allowRepathDelay),
        tostring(bandit.id),
        math.sqrt(bandit.dist2),
        tostring(pairStalled),
        actionState(bandit.object),
        task and task.action or "nil",
        task and task.state or "nil",
        task and tostring(task.time) or "nil",
        tostring(canSee),
        math.sqrt(playerDist2),
        tostring(playerGhost),
        tostring(playerInvisible),
        pfb.goal,
        tostring(pfb.cancelled),
        tostring(pfb.stopping),
        fmtNumber(pfb.x), fmtNumber(pfb.y), fmtNumber(pfb.z),
        pathBanditDist2 and string.format("%.3f", math.sqrt(pathBanditDist2)) or "nil",
        tostring(staleLocation),
        characterId(pfb.targetChar),
        characterId(luaTarget),
        characterId(hitBy)
    ))
end

local function logZombieResume(zombie, sample, bandit, controller, playerDist2)
    stats.zombieResumes = stats.zombieResumes + 1
    if playerDist2 < 9 then stats.resumeNearPlayer = stats.resumeNearPlayer + 1 end

    if detailBudget <= 0 then return end
    detailBudget = detailBudget - 1

    print(string.format(
        "[LCC][BanditsPursuitStall][ZOMBIE_RESUME] marker=%s zombie=%s stalledMs=%d state=%s controller=%s bandit=%s dist=%.3f playerDist=%.3f pfbGoal=%s",
        MARKER,
        characterId(zombie),
        math.floor(nowMs - (sample.stationarySince or nowMs)),
        actionState(zombie),
        tostring(controller),
        bandit and tostring(bandit.id) or "nil",
        bandit and math.sqrt(bandit.dist2) or -1,
        math.sqrt(playerDist2),
        pfbSnapshot(zombie).goal
    ))
end

local function updateOrdinaryZombie(zombie, sample)
    stats.samples = stats.samples + 1

    local bandit = nearestBandit(zombie)
    if not bandit or bandit.zDelta >= 0.8 then
        sample.banditId = nil
        sample.stalled = false
        sample.stationarySince = nowMs
        sample.lastX, sample.lastY = zombie:getX(), zombie:getY()
        return
    end

    local canSee = optionalCall(false, function() return zombie:CanSee(bandit.object) end) == true
    local expectedOpportunity = bandit.dist2 <= CLOSE_PURSUIT_DIST2 or canSee
    if not expectedOpportunity then
        sample.banditId = bandit.id
        sample.stalled = false
        sample.stationarySince = nowMs
        sample.lastX, sample.lastY = zombie:getX(), zombie:getY()
        sample.lastBanditX, sample.lastBanditY = bandit.x, bandit.y
        return
    end

    stats.pursuitCandidates = stats.pursuitCandidates + 1

    local playerDist2, playerGhost, playerInvisible = playerSnapshot(zombie)
    local playerNear = playerDist2 < 4
    if playerNear then stats.playerNearSamples = stats.playerNearSamples + 1 end

    local controller = isController(zombie)
    if controller == false then stats.controllerFalseSamples = stats.controllerFalseSamples + 1 end

    local zx, zy = zombie:getX(), zombie:getY()
    local moved2 = (zx - sample.lastX) * (zx - sample.lastX) + (zy - sample.lastY) * (zy - sample.lastY)

    local sameBandit = sample.banditId == bandit.id
    local banditMoved2 = math.huge
    if sameBandit and sample.lastBanditX ~= nil and sample.lastBanditY ~= nil then
        banditMoved2 = (bandit.x - sample.lastBanditX) * (bandit.x - sample.lastBanditX)
            + (bandit.y - sample.lastBanditY) * (bandit.y - sample.lastBanditY)
    end

    if not sameBandit then
        sample.stationarySince = nowMs
        sample.stalled = false
    elseif moved2 > MOVE_EPS2 then
        if sample.stalled then
            logZombieResume(zombie, sample, bandit, controller, playerDist2)
        end
        sample.stationarySince = nowMs
        sample.stalled = false
    else
        sample.stationaryMs = nowMs - (sample.stationarySince or nowMs)

        -- Current UpdateZombies() deliberately returns when the local player is
        -- inside two tiles. Do not classify a new stall during that window; a
        -- stall detected before approach can still emit ZOMBIE_RESUME there.
        if not sample.stalled and not playerNear and sample.stationaryMs >= STALL_MS then
            sample.stalled = true
            local pairStalled = banditMoved2 <= MOVE_EPS2
            logZombieStall(
                zombie,
                sample,
                bandit,
                controller,
                playerDist2,
                playerGhost,
                playerInvisible,
                canSee,
                pairStalled
            )
        end
    end

    sample.banditId = bandit.id
    sample.lastX, sample.lastY = zx, zy
    sample.lastBanditX, sample.lastBanditY = bandit.x, bandit.y
end

local function updateBandit(bandit, sample)
    local task = banditTaskSnapshot(bandit)
    local movingTask = task and (task.action == "Move" or task.action == "GoTo")

    if not movingTask then
        sample.stalled = false
        sample.stationarySince = nowMs
        sample.lastX, sample.lastY = bandit:getX(), bandit:getY()
        return
    end

    stats.banditMoveTaskSamples = stats.banditMoveTaskSamples + 1

    local bx, by = bandit:getX(), bandit:getY()
    local moved2 = (bx - sample.lastX) * (bx - sample.lastX) + (by - sample.lastY) * (by - sample.lastY)

    local targetFarEnough = true
    if task.x ~= nil and task.y ~= nil then
        local dx = task.x - bx
        local dy = task.y - by
        targetFarEnough = dx * dx + dy * dy > MOVE_TASK_TARGET_DIST2
    end

    local playerDist2 = playerSnapshot(bandit)
    local controller = isController(bandit)

    if moved2 > MOVE_EPS2 then
        if sample.stalled then
            stats.banditMoveTaskResumes = stats.banditMoveTaskResumes + 1
            if detailBudget > 0 then
                detailBudget = detailBudget - 1
                print(string.format(
                    "[LCC][BanditsPursuitStall][BANDIT_MOVE_RESUME] marker=%s bandit=%s stalledMs=%d state=%s controller=%s task=%s/%s playerDist=%.3f",
                    MARKER,
                    characterId(bandit),
                    math.floor(nowMs - (sample.stationarySince or nowMs)),
                    actionState(bandit),
                    tostring(controller),
                    task.action,
                    task.state,
                    math.sqrt(playerDist2)
                ))
            end
        end
        sample.stationarySince = nowMs
        sample.stalled = false
    else
        sample.stationaryMs = nowMs - (sample.stationarySince or nowMs)
        if targetFarEnough and not sample.stalled and sample.stationaryMs >= STALL_MS then
            sample.stalled = true
            stats.banditMoveTaskStalls = stats.banditMoveTaskStalls + 1

            if detailBudget > 0 then
                detailBudget = detailBudget - 1
                local pfb = pfbSnapshot(bandit)
                print(string.format(
                    "[LCC][BanditsPursuitStall][BANDIT_MOVE_STALL] marker=%s bandit=%s stationaryMs=%d state=%s controller=%s task=%s/%s taskTime=%s taskTarget=%s,%s,%s playerDist=%.3f pfbGoal=%s pfbTarget=%s,%s,%s",
                    MARKER,
                    characterId(bandit),
                    math.floor(sample.stationaryMs),
                    actionState(bandit),
                    tostring(controller),
                    task.action,
                    task.state,
                    tostring(task.time),
                    fmtNumber(task.x), fmtNumber(task.y), fmtNumber(task.z),
                    math.sqrt(playerDist2),
                    pfb.goal,
                    fmtNumber(pfb.x), fmtNumber(pfb.y), fmtNumber(pfb.z)
                ))
            end
        end
    end

    sample.lastX, sample.lastY = bx, by
end

local function onZombieUpdate(zombie)
    if not zombie then return end
    stats.updates = stats.updates + 1

    local sample = samples[zombie]
    if not sample then
        sample = {
            lastSample = nowMs,
            lastX = zombie:getX(),
            lastY = zombie:getY(),
            stationarySince = nowMs,
            stalled = false,
        }
        samples[zombie] = sample
        return
    end

    if nowMs - sample.lastSample < SAMPLE_MS then return end
    sample.lastSample = nowMs

    if not zombie:isAlive() then return end

    if zombie:getVariableBoolean("Bandit") then
        updateBandit(zombie, sample)
    else
        updateOrdinaryZombie(zombie, sample)
    end
end

Events.OnZombieUpdate.Add(onZombieUpdate)

-- Maintain one timestamp per frame instead of calling getTimestampMs() for every
-- OnZombieUpdate invocation. After 120 ticks, re-append our observer so snapshots
-- are taken after BanditUpdate and the other experiment callbacks.
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
            "[LCC][BanditsPursuitStall][REBIND] marker=%s tick=%d removeOk=%s addOk=%s",
            MARKER, tickCount, tostring(okRemove), tostring(okAdd)
        ))
    end
end
Events.OnTick.Add(onTick)

Events.EveryOneMinute.Add(function()
    print(string.format(
        "[LCC][BanditsPursuitStall][SUMMARY] marker=%s updates=%d samples=%d pursuitCandidates=%d playerNearSamples=%d controllerFalseSamples=%d zombieStalls=%d pairStalls=%d closeStalls=%d turnAlertedStalls=%d pathfindStalls=%d idleStalls=%d controllerFalseStalls=%d pfbNoneStalls=%d pfbLocationStalls=%d pfbCharacterStalls=%d staleLocationStalls=%d zombieResumes=%d resumeNearPlayer=%d banditMoveTaskSamples=%d banditMoveTaskStalls=%d banditMoveTaskResumes=%d errors=%d lateRebinds=%d",
        MARKER,
        stats.updates,
        stats.samples,
        stats.pursuitCandidates,
        stats.playerNearSamples,
        stats.controllerFalseSamples,
        stats.zombieStalls,
        stats.pairStalls,
        stats.closeStalls,
        stats.turnAlertedStalls,
        stats.pathfindStalls,
        stats.idleStalls,
        stats.controllerFalseStalls,
        stats.pfbNoneStalls,
        stats.pfbLocationStalls,
        stats.pfbCharacterStalls,
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
    "[LCC][BanditsPursuitStall][BOOT] marker=%s mode=observation-only sampleMs=%d stallMs=%d timestamp=once-per-frame movementMutation=false targetMutation=false pfbMutation=false taskMutation=false",
    MARKER, SAMPLE_MS, STALL_MS
))
