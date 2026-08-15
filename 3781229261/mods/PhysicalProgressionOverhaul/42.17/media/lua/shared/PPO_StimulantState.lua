require "PPO_WindowState"

PPO = PPO or {}
PPO.StimulantState = PPO.StimulantState or {}

local StimulantState = PPO.StimulantState

local function finiteOr(value, fallback)
    if type(value) ~= "number" or value ~= value
            or value == math.huge or value == -math.huge then
        return fallback
    end
    return value
end

-- A constant, never a ramp: the strength of the effect does not depend on how
-- much window is left, on how many servings were drunk, or on anything else.
-- Negative by construction -- it is subtracted from the debt's brake in the one
-- muscle-strain call.
--
-- The window itself lives in PPO.WindowState, which both Class B items share.
-- This module is what is left once that moved out: the one number no other item
-- has.
function StimulantState.strainShare(minutes, acceleration)
    if not PPO.WindowState.active(minutes) then return 0 end
    return -math.max(0, finiteOr(acceleration, 0))
end

return StimulantState
