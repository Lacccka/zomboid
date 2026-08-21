-- LCC late safety sweep for B42.20.3 PathFindBehavior2 Goal.Character states.
--
-- v1 only recognized a target as a Bandit while the target still exposed the
-- "Bandit" animation variable. Runtime evidence from 2026-08-21 showed two
-- NetworkZombieMind warnings while v1 reported zero Bandit character goals; one
-- warning followed a Bandit death cleanup by ~0.36 s, after that variable had
-- already been cleared.
--
-- v2 therefore remembers live Bandit Java objects in a weak-key table. A stale
-- Goal.Character that still points at the same IsoZombie can be recognized and
-- cancelled after death/cleanup even when getVariableBoolean("Bandit") is false.
-- v2 also re-registers the OnZombieUpdate callback after startup so it actually
-- runs behind handlers registered later by other client mods.
--
-- Scope remains deliberately narrow:
--   * valid Goal.Character -> IsoPlayer is untouched;
--   * current or previously-observed Bandit targets are cancelled;
--   * nil/dead non-player character goals are cancelled because the engine
--     cannot serialize them and they have no viable live character destination;
--   * unknown living non-player goals are logged but not mutated yet.
if isServer() then return end

local MARKER = "pfb-bandit-character-goal-late-sweep-v2"
LCC_BANDITS_PFB_LATE_SWEEP = MARKER

local stats = {
    updates = 0,
    trackedBandits = 0,
    characterGoals = 0,
    playerCharacterGoals = 0,
    nonPlayerCharacterGoals = 0,
    currentBanditGoals = 0,
    knownBanditGoals = 0,
    nilGoalTargets = 0,
    deadGoalTargets = 0,
    unknownLiveNonPlayerGoals = 0,
    cancels = 0,
    cancelErrors = 0,
    lateRebinds = 0,
}

local knownBandits = setmetatable({}, { __mode = "k" })
local seenDetails = {}
local detailBudget = 48
local rebindTicks = 0
local rebound = false

local function isBandit(character)
    if not character or not instanceof(character, "IsoZombie") then return false end
    local ok, value = pcall(function() return character:getVariableBoolean("Bandit") end)
    return ok and value == true
end

local function isPlayer(character)
    return character ~= nil and instanceof(character, "IsoPlayer")
end

local function characterKind(character)
    if not character then return "nil" end
    if instanceof(character, "IsoPlayer") then return "IsoPlayer" end
    if instanceof(character, "IsoZombie") then return "IsoZombie" end
    if instanceof(character, "IsoGameCharacter") then return "IsoGameCharacter" end
    return "other"
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

local function aliveState(character)
    if not character then return nil end
    local ok, value = pcall(function() return character:isAlive() end)
    if ok then return value == true end
    return nil
end

local function safeCurrentTarget(character)
    if not character then return nil end
    local ok, value = pcall(function() return character:getTarget() end)
    return ok and value or nil
end

local function safeAttackedBy(character)
    if not character then return nil end
    local ok, value = pcall(function() return character:getAttackedBy() end)
    return ok and value or nil
end

local function safeActionState(character)
    if not character then return "<none>" end
    local ok, value = pcall(function() return character:getActionStateName() end)
    return ok and tostring(value or "<none>") or "<error>"
end

local function rememberBandit(character)
    if not character or knownBandits[character] ~= nil then return end
    knownBandits[character] = characterId(character)
    stats.trackedBandits = stats.trackedBandits + 1
end

local function detailKey(zombie, goalTarget, reason)
    return table.concat({ characterId(zombie), characterId(goalTarget), tostring(reason) }, ":")
end

local function logNonPlayerGoal(zombie, pfb, goalTarget, currentBandit, knownBandit, targetAlive, cancelReason)
    if detailBudget <= 0 then return end

    local reason = cancelReason or "observe-only"
    local key = detailKey(zombie, goalTarget, reason)
    if seenDetails[key] then return end
    seenDetails[key] = true
    detailBudget = detailBudget - 1

    local luaTarget = safeCurrentTarget(zombie)
    local attackedBy = safeAttackedBy(zombie)
    local okX, targetX = pcall(function() return pfb:getTargetX() end)
    local okY, targetY = pcall(function() return pfb:getTargetY() end)
    local okZ, targetZ = pcall(function() return pfb:getTargetZ() end)

    print(string.format(
        "[LCC][BanditsPfbLateSweep][NONPLAYER] marker=%s zombie=%s state=%s goalTarget=%s goalKind=%s goalAlive=%s banditNow=%s banditKnown=%s luaTarget=%s luaTargetKind=%s attackedBy=%s attackedByKind=%s pathTarget=%.3f,%.3f,%.3f action=%s",
        MARKER,
        characterId(zombie),
        safeActionState(zombie),
        characterId(goalTarget),
        characterKind(goalTarget),
        tostring(targetAlive),
        tostring(currentBandit),
        tostring(knownBandit),
        characterId(luaTarget),
        characterKind(luaTarget),
        characterId(attackedBy),
        characterKind(attackedBy),
        okX and tonumber(targetX) or -9999,
        okY and tonumber(targetY) or -9999,
        okZ and tonumber(targetZ) or -9999,
        reason
    ))
end

local function cancelGoal(pfb)
    local ok = pcall(function() pfb:cancel() end)
    if ok then
        stats.cancels = stats.cancels + 1
        return true
    end

    stats.cancelErrors = stats.cancelErrors + 1
    if stats.cancelErrors <= 3 then
        print(string.format(
            "[LCC][BanditsPfbLateSweep][ERROR] marker=%s action=cancel",
            MARKER
        ))
    end
    return false
end

local function onZombieUpdate(zombie)
    if not zombie then return end

    -- Record Bandit identity while the animation variable is still reliable.
    -- The weak-key table survives later variable cleanup without retaining dead
    -- Java objects indefinitely.
    if isBandit(zombie) then
        rememberBandit(zombie)
        return
    end

    if not zombie:isAlive() then return end
    stats.updates = stats.updates + 1

    local okPfb, pfb = pcall(function() return zombie:getPathFindBehavior2() end)
    if not okPfb or not pfb then return end

    local okGoal, isCharacterGoal = pcall(function() return pfb:isGoalCharacter() end)
    if not okGoal or not isCharacterGoal then return end
    stats.characterGoals = stats.characterGoals + 1

    local okTarget, goalTarget = pcall(function() return pfb:getTargetChar() end)
    if not okTarget then goalTarget = nil end

    if isPlayer(goalTarget) then
        stats.playerCharacterGoals = stats.playerCharacterGoals + 1
        return
    end

    stats.nonPlayerCharacterGoals = stats.nonPlayerCharacterGoals + 1

    local currentBandit = isBandit(goalTarget)
    local knownBandit = goalTarget ~= nil and knownBandits[goalTarget] ~= nil
    local targetAlive = aliveState(goalTarget)

    if currentBandit then stats.currentBanditGoals = stats.currentBanditGoals + 1 end
    if knownBandit then stats.knownBanditGoals = stats.knownBanditGoals + 1 end
    if goalTarget == nil then stats.nilGoalTargets = stats.nilGoalTargets + 1 end
    if goalTarget ~= nil and targetAlive == false then stats.deadGoalTargets = stats.deadGoalTargets + 1 end

    local cancelReason = nil
    if currentBandit then
        cancelReason = "cancel-current-bandit"
    elseif knownBandit then
        cancelReason = "cancel-known-bandit"
    elseif goalTarget == nil then
        cancelReason = "cancel-nil-target"
    elseif targetAlive == false then
        cancelReason = "cancel-dead-nonplayer"
    else
        stats.unknownLiveNonPlayerGoals = stats.unknownLiveNonPlayerGoals + 1
    end

    logNonPlayerGoal(zombie, pfb, goalTarget, currentBandit, knownBandit, targetAlive, cancelReason)

    if cancelReason ~= nil then
        cancelGoal(pfb)
    end
end

Events.OnZombieUpdate.Add(onZombieUpdate)

-- File load order is not sufficient to guarantee event-handler order because
-- later-loaded mods can still register OnZombieUpdate callbacks. Re-append this
-- handler after startup, once all ordinary client Lua registration has settled.
local function lateRebind()
    rebindTicks = rebindTicks + 1
    if rebound or rebindTicks < 120 then return end

    local okRemove = pcall(function() Events.OnZombieUpdate.Remove(onZombieUpdate) end)
    local okAdd = pcall(function() Events.OnZombieUpdate.Add(onZombieUpdate) end)
    if okAdd then
        rebound = true
        stats.lateRebinds = stats.lateRebinds + 1
        print(string.format(
            "[LCC][BanditsPfbLateSweep][REBIND] marker=%s tick=%d removeOk=%s addOk=%s mode=append-after-startup",
            MARKER, rebindTicks, tostring(okRemove), tostring(okAdd)
        ))
    end

    if rebound then
        pcall(function() Events.OnTick.Remove(lateRebind) end)
    end
end
Events.OnTick.Add(lateRebind)

Events.EveryOneMinute.Add(function()
    print(string.format(
        "[LCC][BanditsPfbLateSweep][SUMMARY] marker=%s updates=%d trackedBandits=%d characterGoals=%d playerCharacterGoals=%d nonPlayerCharacterGoals=%d currentBanditGoals=%d knownBanditGoals=%d nilGoalTargets=%d deadGoalTargets=%d unknownLiveNonPlayerGoals=%d cancels=%d cancelErrors=%d lateRebinds=%d",
        MARKER,
        stats.updates,
        stats.trackedBandits,
        stats.characterGoals,
        stats.playerCharacterGoals,
        stats.nonPlayerCharacterGoals,
        stats.currentBanditGoals,
        stats.knownBanditGoals,
        stats.nilGoalTargets,
        stats.deadGoalTargets,
        stats.unknownLiveNonPlayerGoals,
        stats.cancels,
        stats.cancelErrors,
        stats.lateRebinds
    ))
end)

print(string.format(
    "[LCC][BanditsPfbLateSweep][BOOT] marker=%s mode=late-OnZombieUpdate+delayed-rebind validGoal=Character->IsoPlayer banditIdentity=weak-ref action=selective-cancel unknownLiveNonPlayer=observe-only",
    MARKER
))
