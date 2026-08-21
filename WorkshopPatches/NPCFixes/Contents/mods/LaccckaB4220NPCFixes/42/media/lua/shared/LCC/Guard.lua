-- Lacccka B42 optional Patch Core bootstrap.
-- Prefer the shared Patch Core guard. If Core is absent or incompatible, keep
-- the functional patch loadable through a local degraded fallback.

local function isGuard(candidate)
    return type(candidate) == "table"
        and type(candidate.isEnabled) == "function"
        and type(candidate.disable) == "function"
        and type(candidate.safeRequire) == "function"
        and type(candidate.protect) == "function"
        and type(candidate.install) == "function"
        and type(candidate.wrapBefore) == "function"
end

local coreResult = { pcall(require, "LCC/CoreGuard") }
if coreResult[1] and isGuard(coreResult[2]) then
    local CoreGuard = coreResult[2]
    CoreGuard.MODE = "GUARDED"
    CoreGuard.CORE_AVAILABLE = true
    if not LCCGuardCoreModeReported then
        LCCGuardCoreModeReported = true
        print("[LCC][Guard][OK][core] Lacccka B42 Patch Core detected; GUARDED mode enabled")
    end
    return CoreGuard
end

LCCDegradedGuard = LCCDegradedGuard or {}
local Guard = LCCDegradedGuard

if Guard.__initialized then
    return Guard
end

Guard.VERSION = "degraded-1.0.0"
Guard.MODE = "DEGRADED"
Guard.CORE_AVAILABLE = false
Guard._features = Guard._features or {}
Guard._warned = Guard._warned or {}

local unpackFn = unpack or table.unpack

local function pack(...)
    return { n = select("#", ...), ... }
end

local function getState(id)
    id = tostring(id or "unknown")
    local state = Guard._features[id]
    if not state then
        state = {
            enabled = true,
            installed = false,
            failures = 0,
        }
        Guard._features[id] = state
    end
    return state
end

local function log(level, id, message)
    print("[LCC][GuardFallback][" .. tostring(level) .. "][" .. tostring(id) .. "] " .. tostring(message))
end

function Guard.status(id)
    return getState(id)
end

function Guard.isEnabled(id)
    return getState(id).enabled ~= false
end

function Guard.warnOnce(key, id, message)
    key = tostring(key or id or message or "warning")
    if Guard._warned[key] then return end
    Guard._warned[key] = true
    log("WARN", id or "core", message or key)
end

function Guard.disable(id, reason)
    local state = getState(id)
    state.failures = (state.failures or 0) + 1
    if state.enabled ~= false then
        state.enabled = false
        state.reason = tostring(reason or "unknown compatibility failure")
        log("DISABLED", id, state.reason)
    end
    return false
end

function Guard.safeRequire(id, moduleName, fallback)
    if not Guard.isEnabled(id) then
        return fallback
    end

    local result = pack(pcall(require, moduleName))
    if not result[1] then
        Guard.disable(id, "require \"" .. tostring(moduleName) .. "\" failed: " .. tostring(result[2]))
        return fallback
    end

    return result[2]
end

function Guard.protect(id, phase, callback, ...)
    if not Guard.isEnabled(id) then
        return false
    end
    if type(callback) ~= "function" then
        Guard.disable(id, tostring(phase or "callback") .. " is not callable")
        return false
    end

    local result = pack(pcall(callback, ...))
    if not result[1] then
        Guard.disable(id, tostring(phase or "callback") .. " failed: " .. tostring(result[2]))
        return false
    end

    return true, unpackFn(result, 2, result.n)
end

function Guard.install(spec)
    if type(spec) ~= "table" then
        return Guard.disable("fallback", "invalid install specification")
    end

    local id = tostring(spec.id or "unknown")
    local state = getState(id)
    if state.installed and not spec.reinstall then return true end
    if state.enabled == false then return false end

    if spec.validate then
        local validation = pack(pcall(spec.validate))
        if not validation[1] then
            return Guard.disable(id, "validation failed: " .. tostring(validation[2]))
        end
        if validation[2] == false or validation[2] == nil then
            return Guard.disable(id, tostring(validation[3] or "upstream contract is unavailable"))
        end
    end

    if type(spec.install) ~= "function" then
        return Guard.disable(id, "install callback is missing")
    end

    local installed = pack(pcall(spec.install))
    if not installed[1] then
        return Guard.disable(id, "install failed: " .. tostring(installed[2]))
    end
    if state.enabled == false then return false end

    state.installed = true
    state.reason = nil
    log("OK", id, "installed in DEGRADED fallback mode")
    return true
end

function Guard.wrapBefore(id, target, methodName, hook)
    if type(target) ~= "table" then
        return Guard.disable(id, "target table is unavailable for " .. tostring(methodName))
    end

    local original = target[methodName]
    if type(original) ~= "function" then
        return Guard.disable(id, "target method is unavailable: " .. tostring(methodName))
    end

    target.__LCCGuardWrappers = target.__LCCGuardWrappers or {}
    local marker = tostring(id) .. ":before:" .. tostring(methodName)
    if target.__LCCGuardWrappers[marker] then return true end

    target[methodName] = function(...)
        if Guard.isEnabled(id) then
            Guard.protect(id, "hook " .. tostring(methodName), hook, ...)
        end
        return original(...)
    end

    target.__LCCGuardWrappers[marker] = true
    return true
end

Guard.__initialized = true
print("[LCC][GuardFallback][WARN][core] Lacccka B42 Patch Core is missing or incompatible; running in DEGRADED mode. Correct operation is not guaranteed")
return Guard
