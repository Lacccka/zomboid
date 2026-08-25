-- OnTick intervals (see also inline bootstrap in ModpackFestivalSpawn.lua).
-- This file merges if ModpackFestivalSpawn.lua did not run first (should not happen).

if ModpackFestivalTick and ModpackFestivalTick.every then
    return
end

ModpackFestivalTick = ModpackFestivalTick or {}

ModpackFestivalTick.PER_SEC = 60

ModpackFestivalTick.UI_FAST = 15   -- ~4/sec
ModpackFestivalTick.UI = 30          -- ~2/sec
ModpackFestivalTick.GAME = 60        -- ~1/sec
ModpackFestivalTick.SLOW = 120       -- ~0.5/sec
ModpackFestivalTick.MAINT = 300      -- ~0.2/sec
ModpackFestivalTick.RARE = 600       -- ~0.1/sec

function ModpackFestivalTick.every(counter, interval)
    interval = interval or ModpackFestivalTick.GAME or 60
    if interval < 1 then
        interval = 1
    end
    return counter % interval == 0
end

function ModpackFestivalTick.sec(seconds)
    return math.max(1, math.floor((seconds or 1) * ModpackFestivalTick.PER_SEC))
end

function ModpackFestivalTick.interval(name, fallback)
    local value = ModpackFestivalTick[name]
    if type(value) == "number" and value > 0 then
        return value
    end
    return fallback or ModpackFestivalTick.GAME or 60
end
