-- Companion to 000_LCC_BanditAttackTracePre.lua.
-- Named zzz_* so this observer runs after BanditUpdate.lua but, because Bandits2
-- loads before NPCCombatExperimental, before the late safety disconnect guard.
if isServer() then return end

local TRACE_MARKER = "coordinate-target-trace-v3"
local Trace = rawget(_G, "LCC_BanditsAttackTraceV3")
if type(Trace) ~= "table" or Trace.marker ~= TRACE_MARKER then
    print("[LCC][BanditsAttackTraceV3][POST_DISABLED] pre-trace state unavailable or marker mismatch")
    return
end

local lastPostPair = setmetatable({}, { __mode = "k" })
local lastHeartbeat = 0
local reportedOrderMiss = false

local function nowMs()
    if type(getTimestampMs) == "function" then return getTimestampMs() end
    local gt = getGameTime and getGameTime()
    if gt then return math.floor(gt:getWorldAgeHours() * 3600000) end
    return 0
end

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

local function distance(a, b)
    if not a or not b then return -1 end
    local dx = a:getX() - b:getX()
    local dy = a:getY() - b:getY()
    return math.sqrt(dx * dx + dy * dy)
end

local function maybeSummary()
    local now = nowMs()
    if lastHeartbeat == 0 then
        lastHeartbeat = now
        return
    end
    if now - lastHeartbeat < 15000 then return end
    lastHeartbeat = now

    local s = Trace.stats
    print(string.format(
        "[LCC][BanditsAttackTraceV3][SUMMARY] preUpdates=%d postUpdates=%d entryTargets=%d postTargets=%d acquiredDuringUpdate=%d persistedTargets=%d switchedTargets=%d controllerCalls=%d controllerBeforeTargets=%d controllerAfterTargets=%d controllerCreatedTargets=%d acquiredAfterControllerWindow=%d orderMisses=%d",
        s.preUpdates,
        s.postUpdates,
        s.entryTargets,
        s.postTargets,
        s.acquiredDuringUpdate,
        s.persistedTargets,
        s.switchedTargets,
        s.controllerCalls,
        s.controllerBeforeTargets,
        s.controllerAfterTargets,
        s.controllerCreatedTargets,
        s.acquiredAfterControllerWindow,
        s.orderMisses
    ))
end

local function postUpdate(zombie)
    if not zombie or isBandit(zombie) then return end

    local s = Trace.stats
    s.postUpdates = s.postUpdates + 1
    local record = Trace.pre[zombie]
    if not record then
        s.orderMisses = s.orderMisses + 1
        if not reportedOrderMiss then
            reportedOrderMiss = true
            print("[LCC][BanditsAttackTraceV3][ORDER_MISS] post observer ran without pre snapshot; file/event order must be checked")
        end
        maybeSummary()
        return
    end

    local target = zombie:getTarget()
    local postBandit = isBandit(target)
    local postTarget = postBandit and characterId(target) or "nil"

    if postBandit then
        s.postTargets = s.postTargets + 1

        if not record.entryBandit then
            s.acquiredDuringUpdate = s.acquiredDuringUpdate + 1
            if record.controllerCalls > 0
                    and not record.beforeControllerBandit
                    and not record.afterControllerBandit then
                s.acquiredAfterControllerWindow = s.acquiredAfterControllerWindow + 1
            end

            if lastPostPair[zombie] ~= postTarget then
                lastPostPair[zombie] = postTarget
                print(string.format(
                    "[LCC][BanditsAttackTraceV3][ACQUIRED_DURING_UPDATE] attacker=%s target=%s dist=%.3f entryTarget=nil entryState=%s controllerCalls=%d beforeControllerTarget=%s afterControllerTarget=%s postState=%s bAttack=%s bump=%s",
                    characterId(zombie),
                    postTarget,
                    distance(zombie, target),
                    tostring(record.entryState),
                    record.controllerCalls,
                    tostring(record.beforeControllerTarget),
                    tostring(record.afterControllerTarget),
                    tostring(zombie:getActionStateName() or "<none>"),
                    tostring(zombie:getVariableBoolean("bAttack") == true),
                    tostring(zombie:getBumpType() or "<none>")
                ))
            end
        elseif record.entryTarget == postTarget then
            s.persistedTargets = s.persistedTargets + 1
            lastPostPair[zombie] = postTarget
        else
            s.switchedTargets = s.switchedTargets + 1
            if lastPostPair[zombie] ~= postTarget then
                lastPostPair[zombie] = postTarget
                print(string.format(
                    "[LCC][BanditsAttackTraceV3][SWITCHED_DURING_UPDATE] attacker=%s entryTarget=%s postTarget=%s controllerCalls=%d beforeControllerTarget=%s afterControllerTarget=%s",
                    characterId(zombie),
                    tostring(record.entryTarget),
                    postTarget,
                    record.controllerCalls,
                    tostring(record.beforeControllerTarget),
                    tostring(record.afterControllerTarget)
                ))
            end
        end
    else
        lastPostPair[zombie] = nil
    end

    Trace.pre[zombie] = nil
    maybeSummary()
end

Events.OnZombieUpdate.Add(postUpdate)

print(string.format(
    "[LCC][BanditsAttackTraceV3][POST_BOOT] marker=%s mode=post-BanditUpdate-pre-guard",
    TRACE_MARKER
))
