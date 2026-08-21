-- LCC B42.20.3 terminal Die-task progress guard.
--
-- Runtime 2026-08-21 captured a living Bandit (Elias_Thomas, id=11338181)
-- remaining in actionState=onground for >30 seconds while 50+ ordinary zombies
-- kept a valid Goal.Location at his position. The custom drag-down path had enough
-- nearby zombies to enqueue a locked Die task, but BanditUpdate.ManageActionState()
-- returns false for onground before its local ProcessTask() runs. Bandit.ClearTasks
-- correctly preserves the locked Die task, so the defect is starvation of the
-- terminal task rather than task deletion.
--
-- This compatibility guard only pumps an already-existing Die task while a live
-- Bandit is blocked in the exact onground state. It does not create Die tasks,
-- choose targets, change ordinary-zombie paths, or install character targets.
if isServer() then return end

local MARKER = "terminal-die-onground-pump-v1"
LCC_BANDITS_TERMINAL_DIE_PUMP = MARKER

local stats = {
    updates = 0,
    terminalSeen = 0,
    starts = 0,
    workingTicks = 0,
    completedTransitions = 0,
    completes = 0,
    removed = 0,
    errors = 0,
    lateRebinds = 0,
}

local detailBudget = 24
local tickCount = 0
local rebound = false
local seen = setmetatable({}, { __mode = "k" })

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

local function logOnce(bandit, task, event)
    if detailBudget <= 0 then return end
    local key = tostring(bandit) .. ":" .. tostring(event)
    if seen[key] then return end
    seen[key] = true
    detailBudget = detailBudget - 1
    print(string.format(
        "[LCC][BanditsTerminalDie][%s] marker=%s bandit=%s state=%s taskState=%s time=%s bump=%s",
        event,
        MARKER,
        characterId(bandit),
        tostring(bandit:getActionStateName() or "<none>"),
        tostring(task and task.state or "nil"),
        tostring(task and task.time or "nil"),
        tostring(bandit:getBumpType() or "<none>")
    ))
end

local function processBlockedDie(bandit, task)
    if not task.state then task.state = "NEW" end

    if task.state == "NEW" then
        if not task.time then task.time = 1000 end

        if Bandit and type(Bandit.SetAim) == "function" then
            Bandit.SetAim(bandit, false)
        end
        if Bandit and type(Bandit.IsMoving) == "function" and Bandit.IsMoving(bandit) then
            Bandit.SetMoving(bandit, false)
        end
        if task.anim then
            bandit:setBumpType(task.anim)
        end

        local ok, done = pcall(ZombieActions.Die.onStart, bandit, task)
        if not ok then
            stats.errors = stats.errors + 1
            return
        end
        if done then
            task.state = "WORKING"
            stats.starts = stats.starts + 1
            logOnce(bandit, task, "START")
        end
        return
    end

    if task.state == "WORKING" then
        local fps = getAverageFPS()
        if not fps or fps < 1 then fps = 60 end
        local decrement = 1 / ((fps + 0.5) * 0.01666667)
        task.time = (task.time or 0) - decrement
        stats.workingTicks = stats.workingTicks + 1

        local ok, done = pcall(ZombieActions.Die.onWorking, bandit, task)
        if not ok then
            stats.errors = stats.errors + 1
            return
        end
        if done or task.time <= 0 then
            task.state = "COMPLETED"
            stats.completedTransitions = stats.completedTransitions + 1
            logOnce(bandit, task, "READY")
        end
        return
    end

    if task.state == "COMPLETED" then
        local ok, done = pcall(ZombieActions.Die.onComplete, bandit, task)
        if not ok then
            stats.errors = stats.errors + 1
            return
        end
        if done then
            stats.completes = stats.completes + 1
            logOnce(bandit, task, "COMPLETE")
            if Bandit and type(Bandit.RemoveTask) == "function" then
                pcall(Bandit.RemoveTask, bandit)
                stats.removed = stats.removed + 1
            end
        end
    end
end

local function onZombieUpdate(bandit)
    if not bandit or not bandit:isAlive() or not isBandit(bandit) then return end
    stats.updates = stats.updates + 1

    -- ManageActionState() only starves the terminal task in this exact state.
    -- Do not interfere with hitreaction/getup/ragdoll or normal task processing.
    if bandit:getActionStateName() ~= "onground" then return end
    if not Bandit or type(Bandit.GetTask) ~= "function" then return end

    local okTask, task = pcall(Bandit.GetTask, bandit)
    if not okTask then
        stats.errors = stats.errors + 1
        return
    end
    if not task or task.action ~= "Die" then return end
    if not ZombieActions or not ZombieActions.Die then return end

    stats.terminalSeen = stats.terminalSeen + 1
    processBlockedDie(bandit, task)
end

Events.OnZombieUpdate.Add(onZombieUpdate)

local function lateRebind()
    tickCount = tickCount + 1
    if rebound or tickCount < 120 then return end

    local okRemove = pcall(function() Events.OnZombieUpdate.Remove(onZombieUpdate) end)
    local okAdd = pcall(function() Events.OnZombieUpdate.Add(onZombieUpdate) end)
    if okAdd then
        rebound = true
        stats.lateRebinds = stats.lateRebinds + 1
        print(string.format(
            "[LCC][BanditsTerminalDie][REBIND] marker=%s tick=%d removeOk=%s addOk=%s",
            MARKER, tickCount, tostring(okRemove), tostring(okAdd)
        ))
        pcall(function() Events.OnTick.Remove(lateRebind) end)
    end
end
Events.OnTick.Add(lateRebind)

Events.EveryOneMinute.Add(function()
    print(string.format(
        "[LCC][BanditsTerminalDie][SUMMARY] marker=%s updates=%d terminalSeen=%d starts=%d workingTicks=%d completedTransitions=%d completes=%d removed=%d errors=%d lateRebinds=%d",
        MARKER,
        stats.updates,
        stats.terminalSeen,
        stats.starts,
        stats.workingTicks,
        stats.completedTransitions,
        stats.completes,
        stats.removed,
        stats.errors,
        stats.lateRebinds
    ))
end)

print(string.format(
    "[LCC][BanditsTerminalDie][BOOT] marker=%s scope=existing-Die+onground mutation=terminal-task-progress-only",
    MARKER
))
