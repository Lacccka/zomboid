NMClientTickGate = NMClientTickGate or {}

local function addTickListener()
    if NMClientTickGate._registered == true then
        return false
    end
    if Events and Events.OnTick and Events.OnTick.Add then
        Events.OnTick.Add(NMClientTickGate.onTick)
        NMClientTickGate._registered = true
        return true
    end
    return false
end

local function removeTickListener()
    if NMClientTickGate._registered ~= true then
        return false
    end
    if Events and Events.OnTick and Events.OnTick.Remove then
        Events.OnTick.Remove(NMClientTickGate.onTick)
        NMClientTickGate._registered = false
        return true
    end
    return false
end

function NMClientTickGate.wake(_reason)
    addTickListener()
end

function NMClientTickGate.onTick()
    local advanceTick = NMClientTickGate._advanceTick
    if advanceTick then
        advanceTick()
    end

    local hasAnyTickWork = NMClientTickGate._hasAnyTickWork
    local hasWork = false
    local reason = "idle"
    if hasAnyTickWork then
        hasWork, reason = hasAnyTickWork()
    end

    local isHookInstalled = NMClientTickGate._isHookInstalled and NMClientTickGate._isHookInstalled() == true or false
    if hasWork == true then
        if isHookInstalled ~= true then
            if NMClientTickGate._installHook then
                NMClientTickGate._installHook()
            end
        end
    elseif isHookInstalled == true then
        if NMClientTickGate._removeHook then
            NMClientTickGate._removeHook()
        end
    end

    local keepGateRegistered = NMClientTickGate._shouldKeepGateRegistered
        and NMClientTickGate._shouldKeepGateRegistered() == true
        or false
    if hasWork ~= true and isHookInstalled ~= true and keepGateRegistered ~= true then
        removeTickListener()
    end
end

function NMClientTickGate.register(config)
    if type(config) ~= "table" then
        return
    end
    NMClientTickGate._advanceTick = config.advanceTick
    NMClientTickGate._hasAnyTickWork = config.hasAnyTickWork
    NMClientTickGate._isHookInstalled = config.isHookInstalled
    NMClientTickGate._installHook = config.installHook
    NMClientTickGate._removeHook = config.removeHook
    NMClientTickGate._shouldKeepGateRegistered = config.shouldKeepGateRegistered
    NMClientTickGate.wake("register")
end

return NMClientTickGate
