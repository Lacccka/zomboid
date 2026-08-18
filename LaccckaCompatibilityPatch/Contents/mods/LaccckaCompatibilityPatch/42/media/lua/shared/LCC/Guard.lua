-- Lacccka B42.20 Compatibility Patch
-- Shared failure-isolation helpers for compatibility shims and wrappers.
--
-- Design rule: protect only LCC compatibility code. Calls into the original
-- upstream function stay outside pcall so real upstream failures remain visible.

LCCGuard = LCCGuard or {}
local Guard = LCCGuard

if Guard.__initialized then
    return Guard
end

Guard.VERSION = "1.0.0"
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
    print("[LCC][Guard][" .. tostring(level) .. "][" .. tostring(id) .. "] " .. tostring(message))
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

-- Run only LCC-owned compatibility logic behind a circuit breaker.
-- On the first failure the feature is disabled for the rest of the session.
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
        return Guard.disable("core", "invalid install specification")
    end

    local id = tostring(spec.id or "unknown")
    local state = getState(id)

    if state.installed and not spec.reinstall then
        return true
    end
    if state.enabled == false then
        return false
    end

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
    if state.enabled == false then
        return false
    end

    state.installed = true
    state.reason = nil
    log("OK", id, "installed")
    return true
end

-- Install an LCC pre-hook while preserving upstream failures and return values.
-- If the hook breaks after a mod update, only the hook is disabled and the
-- original function is still called normally.
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
    if target.__LCCGuardWrappers[marker] then
        return true
    end

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
return Guard
