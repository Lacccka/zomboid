-- Copyright (c) 2022-2024 Oneline/D.Borovsky
-- All rights reserved

-- string
function string:trim()
	return string.match(self,'^()%s*$') and '' or string.match(self,'^%s*(.*%S)')
end

function string:ssplit(sep)
   local sep, fields = sep or ":", {}
   local pattern = string.format("([^%s]+)", sep)
   self:gsub(pattern, function(c) fields[#fields+1] = c end)
   return fields
end

function string:indexOf(s, i)
	if not i then
		return string.find(self, s, 1, true) or -1
	else
		local str = string.sub(self, i)
		local index = string.find(str, s, 1, true)
		if index then
			return index+i-1
		else
			return -1
		end
	end
end

function string:lastIndexOf(s)
	local index = string.find(self, s, 1, true);
	
	if index then
		local strlen = string.len(s)
		while string.find(self, s, index+strlen, true) do
			index = string.find(self, s, index+strlen, true)
		end
		
		return index-1
	else
		return -1
	end
end

function string:substring(_start, _end)
	if not _end then
		return string.sub(self, _start)
	else
		local length = string.len(self);
		if length < _end then
			return string.sub(self, _start, length)
		else
			return string.sub(self, _start, _end)
		end
	end
end

function string:replace(a, b)
	return string.gsub(self, a, b)
end

function string:starts_with(start)
   return self:sub(1, #start) == start
end

function string:ends_with(ending)
   return ending == "" or self:sub(-#ending) == ending
end

function string:count(s)
	local _, count = string.gsub(self, '%'..s, "")
	return count
end

-- table
function table.clone(obj, seen)
  if type(obj) ~= 'table' then return obj end
  if seen and seen[obj] then return seen[obj] end
  local s = seen or {}
  local res = setmetatable({}, getmetatable(obj))
  s[obj] = res
  for k=1, #obj do
	res[table.clone(k, s)] = table.clone(obj[k], s)
  end
  return res
end

-- scripting
local tab_char1 = "    "; -- 4 spaces
local tab_char2 = "	";    -- 1 tab

function GetBlockLayer(line)
	if not line then
		return -1;
	elseif line:starts_with(tab_char1) then
		line = line:sub(5)
		local layer = 1;
		while line:starts_with(tab_char1) do
			line = line:sub(5)
			layer = layer + 1;
		end
		return layer;
	elseif line:starts_with(tab_char2) then
		line = line:sub(2)
		local layer = 1;
		while line:starts_with(tab_char2) do
			line = line:sub(2)
			layer = layer + 1;
		end
		return layer;
	else
		return 0;
	end
end

function GetModIDFromPath(file)
    local mods = getActivatedMods()
    for i=mods:size()-1, 0, -1 do
        local mod = mods:get(i)
        local reader = getModFileReader(mod, file, false);
        if reader then
            reader:close()
            return mod
        end
    end
    return nil
end

local months = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31}
local function remainder(a, b)
    return a - math.floor(a / b) * b;
end

function GetWorldAgeSeconds()
    local gt = getGameTime();
    local year = gt:getYear();
    local month = gt:getMonth();
    local day = gt:getDay();

    local days = (year-1970) * 365;
    for i=1970, year do
        if remainder(i, 4) == 0 and (remainder(i, 100) ~= 0 or remainder(i, 400) == 0) then
            if i < year or (i == year and month > 2) then
                days = days + 1;
            end
        end
    end
    for i=1, month-1 do
        days = days + months[i];
    end
    days = days + day;
    local millis = days * 24 * 60 * 60;
    millis = math.floor(millis + (gt:getTimeOfDay()*60)*60);
    return millis;
end

function getDeltaTimeInMillis(ts)
	return getTimeInMillis() - ts;
end

function GetCurrentTime(timezone, timestamp)
    if not timezone then
        timezone = SSRLoader.timezone;
    end
    local tm = {};
    local daysInMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    local seconds, minutes, hours, days, year, month;
    local dayOfWeek;
    local tm_yday = 0;

    if timestamp then
        seconds = math.floor(timestamp) + (timezone or 0)*3600;
    else
        seconds = getTimestamp() + (timezone or 0)*3600;
    end
    -- calculate minutes
    minutes = math.floor(seconds / 60);
    seconds = seconds - (minutes * 60);
    --calculate hours
    hours = math.floor(minutes / 60);
    minutes = minutes - (hours * 60);
    --calculate days
    days = math.floor(hours / 24);
    hours = hours - (days * 24);

    -- Unix time starts in 1970 on a Thursday
    year      = 1970;
    dayOfWeek = 4;

    while true do
        local leapYear = remainder(year, 4) == 0 and (remainder(year, 100) ~= 0 or remainder(year, 400) == 0);
        local daysInYear = 365;
        if leapYear then
            daysInYear = 366;
        end

        if days >= daysInYear then
            if leapYear then
                dayOfWeek = dayOfWeek + 2;
            else
                dayOfWeek = dayOfWeek + 1;
            end
            days = days - daysInYear;
            if dayOfWeek >= 7 then
                dayOfWeek = dayOfWeek - 7;
            end
            year = year + 1;
        else
            tm.tm_yday = days;
            dayOfWeek  = dayOfWeek + days;
            dayOfWeek  = remainder(dayOfWeek, 7);
            -- calculate the month and day

            month = 1;
            while month <= 12 do
                local dim = daysInMonth[month];

                -- add a day to feburary if this is a leap year
                if month == 2 and leapYear then
                    dim = dim + 1;
                end

                if days >= dim then
                    days = days - dim;
                else
                    break;
                end
                month = month + 1;
            end
            break;
        end
    end

    tm.tm_sec  = seconds;
    tm.tm_min  = minutes;
    tm.tm_hour = hours;
    tm.tm_mday = days + 1;
    tm.tm_mon  = month;
    tm.tm_year = year;
    tm.tm_wday = dayOfWeek;
    return tm;
end

function GetPlayerByUserName(username)
    for i=getOnlinePlayers():size()-1, 0, -1 do
        local player = getOnlinePlayers():get(i);
        if player then
            if player:getUsername() == username then
                return player;
            end
        end
    end
end