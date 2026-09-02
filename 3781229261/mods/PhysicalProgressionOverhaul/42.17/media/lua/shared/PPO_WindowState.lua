require "PPO_Num"

PPO = PPO or {}
PPO.WindowState = PPO.WindowState or {}

local WindowState = PPO.WindowState

local Num = PPO.Num

function WindowState.normalize(minutes)
    return math.max(0, Num.finite(minutes, 0))
end

-- The tank holds exactly one full container. That is what makes a container
-- drunk in one sitting waste nothing while a hoarder with three of them still
-- cannot hold the effect on permanently.
function WindowState.cap(windowHours, servingsPerContainer)
    local hours = math.max(0, Num.finite(windowHours, 0))
    local servings = math.max(0, Num.finite(servingsPerContainer, 0))
    return hours * 60 * servings
end

-- Duration accumulates; strength never does. Every Class B effect is a
-- constant while its window is open, so adding time cannot deepen anything --
-- which is exactly why adding time is safe and refreshing was unnecessary.
--
-- A window already longer than the cap is left alone rather than trimmed: a
-- server that shortens the Sandbox hours mid-save should let what was bought
-- burn down, not confiscate it.
function WindowState.add(currentMinutes, servings, windowHours,
        servingsPerContainer)
    local current = WindowState.normalize(currentMinutes)
    local portion = math.max(0, Num.finite(servings, 0))
    local hours = math.max(0, Num.finite(windowHours, 0))
    local ceiling = WindowState.cap(hours, servingsPerContainer)
    local total = current + hours * 60 * portion
    if total > ceiling then
        if current > ceiling then return current end
        return ceiling
    end
    return total
end

-- Only the active-minute engine tick may call this, so offline time spends no
-- window.
function WindowState.advance(currentMinutes, elapsedMinutes)
    local current = WindowState.normalize(currentMinutes)
    local elapsed = math.max(0, Num.finite(elapsedMinutes, 0))
    return math.max(0, current - elapsed)
end

function WindowState.active(minutes)
    return WindowState.normalize(minutes) > 0
end

return WindowState
