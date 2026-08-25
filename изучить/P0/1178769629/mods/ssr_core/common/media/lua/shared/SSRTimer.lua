-- Copyright (c) 2022-2025 Oneline/D.Borovsky
-- All rights reserved
SSRTimer = {};

local timestamp = 0;
local timers = {};

local id = 0;
SSRTimer.add_ms = function (f, interval, loop) -- interval in millis
    local timer = {};
    id = id + 1;
    timer.id = id;
    timer.callback = f;
    timer.interval = interval;
    timer.timestamp = getTimeInMillis() + timer.interval;
    timer.loop = loop;
    table.insert(timers, timer);
    return timer.id;
end

SSRTimer.add_s = function (f, interval, loop) -- interval in seconds
    return SSRTimer.add_ms(f, interval * 1000, loop);
end

SSRTimer.add_m = function (f, interval, loop) -- interval in minutes
    return SSRTimer.add_ms(f, interval * 60000, loop);
end

SSRTimer.remove = function (t)
    for i=#timers, 1, -1 do
        if timers[i].id == t then
            table.remove(timers, i);
            break;
        end
    end
end

SSRTimer.run = function (t)
    for i=#timers, 1, -1 do
        if timers[i].id == t then
            timers[i].callback();
            if timers[i] and timers[i].id == t then
                if timers[i].loop then
                    timers[i].timestamp = getTimeInMillis() + timers[i].interval;
                else
                    table.remove(timers, i);
                end
            end
            break;
        end
    end
end

if isServer() then
    SSRTimer.loop = function ()
        timestamp = getTimeInMillis();
        for i=#timers, 1, -1 do
            if timestamp > timers[i].timestamp then
                timers[i].callback();
                if timers[i].loop then
                    timers[i].timestamp = getTimeInMillis() + timers[i].interval;
                else
                    table.remove(timers, i);
                end
            end
        end
    end
    Events.EveryOneMinute.Add(SSRTimer.loop);
else
    local index = 1;
    SSRTimer.loop = function ()
        timestamp = getTimeInMillis();
        if timers[index] then
            if timestamp > timers[index].timestamp then
                local _id = timers[index].id;
                timers[index].callback();
                if timers[index] and timers[index].id == _id then
                    if timers[index].loop then
                        timers[index].timestamp = getTimeInMillis() + timers[index].interval;
                    else
                        table.remove(timers, index);
                        return;
                    end
                else
                    return;
                end
            end
            index = index + 1;
        else
            index = 1;
        end
    end
    Events.OnTick.Add(SSRTimer.loop);
end