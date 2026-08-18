NMServerTickGate = NMServerTickGate or {}

local LOG_INTERVAL_MS = 5000

NMServerTickGate._diag = NMServerTickGate._diag or {
    lastLogMs = 0,
    counters = {}
}

local function nowRealMs()
    if getTimestampMs then
        local ms = tonumber(getTimestampMs())
        if ms then return ms end
    end
    if getTimestamp then
        local ts = tonumber(getTimestamp())
        if ts then return ts * 1000 end
    end
    return 0
end

local function memoryDiagEnabled()
    return NMCore and NMCore.isSubsystemDebugEnabled and NMCore.isSubsystemDebugEnabled("memory") == true
end

local function countDiag(name)
    if memoryDiagEnabled() ~= true then
        return
    end
    local key = tostring(name or "unknown")
    local diag = NMServerTickGate._diag
    diag.counters[key] = (tonumber(diag.counters[key]) or 0) + 1
end

local function flushDiag()
    if memoryDiagEnabled() ~= true or not (NMCore and NMCore.logChannel) then
        return
    end
    local diag = NMServerTickGate._diag
    local nowMs = nowRealMs()
    if (nowMs - (tonumber(diag.lastLogMs) or 0)) < LOG_INTERVAL_MS then
        return
    end
    diag.lastLogMs = nowMs
    local parts = {}
    for name, count in pairs(diag.counters) do
        parts[#parts + 1] = string.format("%s=%d", tostring(name), tonumber(count) or 0)
        diag.counters[name] = nil
    end
    if #parts > 0 then
        NMCore.logChannel("memory", "server_tick_hook", table.concat(parts, " | "))
    end
end

local function normalizeReason(reason)
    local value = tostring(reason or "unknown")
    if value == "" then
        return "unknown"
    end
    return value
end

function NMServerTickGate.onTick()
    local startedMs = nowRealMs()
    local advanceTick = NMServerTickGate._advanceTick
    if advanceTick then
        advanceTick()
    end

    local hasAnyTickWork = NMServerTickGate._hasAnyTickWork
    local hasWork = false
    local reason = "idle"
    if hasAnyTickWork then
        hasWork, reason = hasAnyTickWork()
    end

    local isHookInstalled = NMServerTickGate._isHookInstalled and NMServerTickGate._isHookInstalled() == true or false
    if hasWork == true then
        if isHookInstalled ~= true then
            if NMServerTickGate._installHook then
                NMServerTickGate._installHook()
            end
            countDiag("tick_hook_add")
            countDiag("tick_hook_wake_reason_" .. normalizeReason(reason))
        end
    elseif isHookInstalled == true then
        if NMServerTickGate._removeHook then
            NMServerTickGate._removeHook()
        end
        countDiag("tick_hook_remove")
        countDiag("tick_hook_idle_exit")
    end

    flushDiag()
end

function NMServerTickGate.register(config)
    if type(config) ~= "table" then
        return
    end
    NMServerTickGate._advanceTick = config.advanceTick
    NMServerTickGate._hasAnyTickWork = config.hasAnyTickWork
    NMServerTickGate._isHookInstalled = config.isHookInstalled
    NMServerTickGate._installHook = config.installHook
    NMServerTickGate._removeHook = config.removeHook
    if NMServerTickGate._registered == true then
        return
    end
    if Events and Events.OnTick and Events.OnTick.Add then
        Events.OnTick.Add(NMServerTickGate.onTick)
        NMServerTickGate._registered = true
    end
end

return NMServerTickGate
