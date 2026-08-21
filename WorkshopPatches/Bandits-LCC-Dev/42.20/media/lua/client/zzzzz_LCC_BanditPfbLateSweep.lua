-- LCC late safety sweep for B42.20.3 PathFindBehavior2 Goal.Character -> Bandit.
--
-- Runtime v6 showed that the hit-callback sanitation can clear attackedBy before
-- the unsafe PFB character goal exists. A few frames later Java may still build
-- Goal.Character from the hit reaction, and NetworkZombieMind.set() rejects that
-- non-IsoPlayer character goal.
--
-- This callback is intentionally registered after BanditUpdate and the relation
-- wrapper. It only cancels a PFB Goal.Character whose target is a Bandit. It does
-- not change IsoZombie.target, attackedBy, bump state or issue replacement paths.
if isServer() then return end

local MARKER = "pfb-bandit-character-goal-late-sweep-v1"
LCC_BANDITS_PFB_LATE_SWEEP = MARKER

local stats = {
    updates = 0,
    characterGoals = 0,
    banditCharacterGoals = 0,
    cancels = 0,
    cancelErrors = 0,
}

local detailBudget = 24

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

local function onZombieUpdate(zombie)
    if not zombie or not zombie:isAlive() or isBandit(zombie) then return end
    stats.updates = stats.updates + 1

    local okPfb, pfb = pcall(function() return zombie:getPathFindBehavior2() end)
    if not okPfb or not pfb then return end

    local okGoal, isCharacterGoal = pcall(function() return pfb:isGoalCharacter() end)
    if not okGoal or not isCharacterGoal then return end
    stats.characterGoals = stats.characterGoals + 1

    local okTarget, goalTarget = pcall(function() return pfb:getTargetChar() end)
    if not okTarget or not isBandit(goalTarget) then return end
    stats.banditCharacterGoals = stats.banditCharacterGoals + 1

    local okCancel = pcall(function() pfb:cancel() end)
    if okCancel then
        stats.cancels = stats.cancels + 1
        if detailBudget > 0 then
            detailBudget = detailBudget - 1
            local state = zombie:getActionStateName()
            local target = nil
            pcall(function() target = zombie:getTarget() end)
            print(string.format(
                "[LCC][BanditsPfbLateSweep][CANCEL] marker=%s zombie=%s goalTarget=%s state=%s luaTarget=%s",
                MARKER,
                characterId(zombie),
                characterId(goalTarget),
                tostring(state or "<none>"),
                characterId(target)
            ))
        end
    else
        stats.cancelErrors = stats.cancelErrors + 1
        if stats.cancelErrors <= 3 then
            print(string.format(
                "[LCC][BanditsPfbLateSweep][ERROR] marker=%s zombie=%s goalTarget=%s action=cancel",
                MARKER, characterId(zombie), characterId(goalTarget)
            ))
        end
    end
end

Events.OnZombieUpdate.Add(onZombieUpdate)

Events.EveryOneMinute.Add(function()
    print(string.format(
        "[LCC][BanditsPfbLateSweep][SUMMARY] marker=%s updates=%d characterGoals=%d banditCharacterGoals=%d cancels=%d cancelErrors=%d",
        MARKER,
        stats.updates,
        stats.characterGoals,
        stats.banditCharacterGoals,
        stats.cancels,
        stats.cancelErrors
    ))
end)

print(string.format(
    "[LCC][BanditsPfbLateSweep][BOOT] marker=%s mode=late-OnZombieUpdate unsafeGoal=Character->Bandit action=cancel replacementPath=false",
    MARKER
))
