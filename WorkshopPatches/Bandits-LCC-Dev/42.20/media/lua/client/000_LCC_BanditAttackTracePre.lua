-- LCC controlled diagnostic for Bandits B42.20.3.
-- This file is intentionally named 000_* so its OnZombieUpdate callback is
-- registered before BanditUpdate.lua. A zzz_* companion observes the state
-- after BanditUpdate.lua and before NPCCombatExperimental's safety guard.
if isServer() then return end

local TRACE_MARKER = "coordinate-target-trace-v3"
LCC_BANDITS_ATTACK_TRACE = TRACE_MARKER

local Trace = {
    marker = TRACE_MARKER,
    sequence = 0,
    pre = setmetatable({}, { __mode = "k" }),
    lastEntryPair = setmetatable({}, { __mode = "k" }),
    stats = {
        preUpdates = 0,
        entryTargets = 0,
        controllerCalls = 0,
        controllerBeforeTargets = 0,
        controllerAfterTargets = 0,
        controllerCreatedTargets = 0,
        postUpdates = 0,
        postTargets = 0,
        acquiredDuringUpdate = 0,
        persistedTargets = 0,
        switchedTargets = 0,
        acquiredAfterControllerWindow = 0,
        orderMisses = 0,
    },
    controllerWrapped = false,
}
_G.LCC_BanditsAttackTraceV3 = Trace

local function isBandit(character)
    if not character or not instanceof(character, "IsoZombie") then return false end
    local ok, value = pcall(function()
        return character:getVariableBoolean("Bandit")
    end)
    return ok and value == true
end

local function characterId(character)
    if not character then return "nil" end
    if BanditUtils and type(BanditUtils.GetCharacterID) == "function" then
        local ok, value = pcall(BanditUtils.GetCharacterID, character)
        if ok and value ~= nil then return tostring(value) end
    end
    local ok, value = pcall(function()
        return character:getPersistentOutfitID()
    end)
    if ok and value ~= nil then return tostring(value) end
    return tostring(character)
end

local function targetSnapshot(zombie)
    local target = zombie and zombie:getTarget() or nil
    local banditTarget = isBandit(target)
    return {
        target = target,
        isBandit = banditTarget,
        id = banditTarget and characterId(target) or "nil",
    }
end

local function preUpdate(zombie)
    if not zombie or isBandit(zombie) then return end

    Trace.sequence = Trace.sequence + 1
    Trace.stats.preUpdates = Trace.stats.preUpdates + 1

    local entry = targetSnapshot(zombie)
    local record = {
        sequence = Trace.sequence,
        entryBandit = entry.isBandit,
        entryTarget = entry.id,
        entryState = tostring(zombie:getActionStateName() or "<none>"),
        entryBAttack = zombie:getVariableBoolean("bAttack") == true,
        controllerCalls = 0,
        beforeControllerBandit = false,
        beforeControllerTarget = "nil",
        afterControllerBandit = false,
        afterControllerTarget = "nil",
    }
    Trace.pre[zombie] = record

    if entry.isBandit then
        Trace.stats.entryTargets = Trace.stats.entryTargets + 1
        if Trace.lastEntryPair[zombie] ~= entry.id then
            Trace.lastEntryPair[zombie] = entry.id
            print(string.format(
                "[LCC][BanditsAttackTraceV3][ENTRY_TARGET] attacker=%s target=%s state=%s bAttack=%s",
                characterId(zombie),
                entry.id,
                record.entryState,
                tostring(record.entryBAttack)
            ))
        end
    else
        Trace.lastEntryPair[zombie] = nil
    end
end

Events.OnZombieUpdate.Add(preUpdate)

-- PathZombieToBanditLocation() calls BanditUtils.IsController(zombie)
-- immediately before zombie:pathToLocationF(...). Wrapping this function gives
-- us a checkpoint immediately before the coordinate-path call without changing
-- BanditUpdate.lua itself.
if BanditUtils and type(BanditUtils.IsController) == "function" then
    local originalIsController = BanditUtils.IsController
    BanditUtils.IsController = function(zombie, ...)
        local record = zombie and Trace.pre[zombie] or nil
        local traceThis = record ~= nil and not isBandit(zombie)

        if traceThis then
            record.controllerCalls = record.controllerCalls + 1
            Trace.stats.controllerCalls = Trace.stats.controllerCalls + 1
            local before = targetSnapshot(zombie)
            if before.isBandit then
                record.beforeControllerBandit = true
                record.beforeControllerTarget = before.id
                Trace.stats.controllerBeforeTargets = Trace.stats.controllerBeforeTargets + 1
            end
        end

        local result = originalIsController(zombie, ...)

        if traceThis then
            local after = targetSnapshot(zombie)
            if after.isBandit then
                record.afterControllerBandit = true
                record.afterControllerTarget = after.id
                Trace.stats.controllerAfterTargets = Trace.stats.controllerAfterTargets + 1
            end
            if not record.beforeControllerBandit and record.afterControllerBandit then
                Trace.stats.controllerCreatedTargets = Trace.stats.controllerCreatedTargets + 1
                print(string.format(
                    "[LCC][BanditsAttackTraceV3][ISCONTROLLER_CREATED_TARGET] attacker=%s target=%s intervention=false",
                    characterId(zombie),
                    record.afterControllerTarget
                ))
            end
        end

        return result
    end
    Trace.controllerWrapped = true
end

print(string.format(
    "[LCC][BanditsAttackTraceV3][PRE_BOOT] marker=%s controllerWrapped=%s",
    TRACE_MARKER,
    tostring(Trace.controllerWrapped)
))
