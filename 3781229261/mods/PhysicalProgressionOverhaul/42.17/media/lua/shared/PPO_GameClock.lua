require "PPO_Num"

PPO = PPO or {}
PPO.GameClock = PPO.GameClock or {}

local GameClock = PPO.GameClock

-- SandboxOptions.getDayLengthMinutes() maps DayLength 1..4 to 15/30/60/90 real
-- minutes and any higher value to (value - 3) * 60, then feeds
-- GameTime.setMinutesPerDay. DayLength 4 is the default of every shipped
-- preset, so 90 is the honest fallback when the seam cannot be read.
local DEFAULT_MINUTES_PER_DAY = 90
local GAME_MINUTES_PER_DAY = 1440
local MILLISECONDS_PER_MINUTE = 60000

-- Three repetitions of slack tolerates two dropped animation events; the floor
-- keeps a hiccup survivable on fast days.
local CEILING_FACTOR = 3
local CEILING_FLOOR_MINUTES = 1

local Num = PPO.Num

function GameClock.minutesPerDay()
    local ok, value = pcall(function()
        return getGameTime():getMinutesPerDay()
    end)
    if not ok then return DEFAULT_MINUTES_PER_DAY end

    local minutes = Num.finite(value, 0)
    if minutes <= 0 then return DEFAULT_MINUTES_PER_DAY end
    return minutes
end

-- One repetition costs periodMs of real time. Converting that into game minutes
-- is the only place the day length enters the load model.
function GameClock.gameMinutesPerRepeat(periodMs)
    local period = math.max(0, Num.finite(periodMs, 0))
    return period / MILLISECONDS_PER_MINUTE
        * GAME_MINUTES_PER_DAY / GameClock.minutesPerDay()
end

-- The ceiling bounds a clock jump, a stall between repetitions and a resumed
-- action. A fixed ceiling would reintroduce the DayLength dependency this
-- module exists to remove: one legitimate squat repetition spans 4.8 game
-- minutes at DayLength 1 and 0.2 at DayLength 9.
function GameClock.loadCeiling(periodMs)
    return math.max(CEILING_FLOOR_MINUTES,
        CEILING_FACTOR * GameClock.gameMinutesPerRepeat(periodMs))
end
