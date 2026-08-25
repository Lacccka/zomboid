-- Copyright (c) 2025 Oneline/D.Borovsky
-- All rights reserved
require "Scripting/Commands/CommandList_a"
require "Communications/QSystem"

SRN = {}
SRN.active = false;

SRN.canvas = { 1920, 1080 }
local scale_factor = 1.0;
local offset_x = 0;

local function updateScaleFactor()
    scale_factor = math.abs(getCore():getScreenHeight() / SRN.canvas[2]);
    offset_x = (getCore():getScreenWidth() / 2) - ((SRN.canvas[1] * scale_factor) / 2);
end

Events.OnResolutionChange.Add(updateScaleFactor);
Events.OnGameStart.Add(updateScaleFactor);


local motions = {}
local motions_size = 0;


local Motion = {}

function Motion:prepare()
    for i=1, #self.images do
        if not self.images[i]:isReady() then
            return false;
        end
    end
    if self.callback then self.callback() end
    return true;
end

function Motion:set_start(value)
    if value < 1 or value > self._end then
        return "Invalid start index"
    end
    self._start = value;
    self.index = value;
end

function Motion:set_end(value)
    if value then
        if value < 1 or value > self.queue_size then
            return "Invalid end index"
        end
        self._end = value;
        if self.index > value then self.index = value; end
    else
        self._end = #self.queue;
    end
end

function Motion:reset()
    self.index = self._start;
    self.image_id = nil;
    self.next_id = nil;
    self.x = 0;
    self.y = 0;
    self.scale = { x=1, y=1 };
    self.alpha = 1;
    self.active = false;
    self.paused = false;
end

function Motion:release()
    if self.slave then
        self.slave.active = self.active;
        self.slave:reset();
        self.slave:release();
    end
    if self.callback and not self.master and not self.async then SSRTimer.add_ms(self.callback, 0, false) end
    self.master = nil; self.slave = nil;
end

function Motion:getX()
    return self.master and self.x + (self.master.master and self.master:getX() or self.master.x) or self.x;
end

function Motion:getY()
    return self.master and self.y + (self.master.master and self.master:getY() or self.master.y) or self.y;
end

function Motion:getScaleX()
    return self.master and self.scale.x * (self.master.master and self.master:getScaleX() or self.master.scale.x) or self.scale.x;
end

function Motion:getScaleY()
    return self.master and self.scale.y * (self.master.master and self.master:getScaleY() or self.master.scale.y) or self.scale.y;
end

function Motion:getAlpha()
    return self.master and self.alpha * (self.master.master and self.master:getAlpha() or self.master.alpha) or self.alpha;
end

function Motion:render(r)
    while not self.paused and self.queue[self.index][1]() do
        self.index = self.index + 1;
        if self.index > (self._end or self.queue_size) then
            self.index = self.endless and self.index - 1 or self._start;
            self.active = self.loop or self.endless;
            if self.endless and not self.loop then
                self.paused = true;
                if self.master and not self.rc then
                    if self.master.index < (self._end or self.queue_size) then self.master.index = self.master.index + 1; end
                elseif not self.async then
                    self.rc = nil;
                    SSRTimer.add_ms(self.callback, 0, false);
                end
            end
            break;
        end
    end

    --- (self.scale.x < 0 and self.images[self.image_id]:getWidth()*(self.scale.x*-1) or 0)
    if self.next_id then
        if self.image_id then
            r:drawTextureScaled(self.images[self.image_id], (self:getX() - (self:getScaleX() < 0 and self.images[self.image_id]:getWidth()*(self:getScaleX()) or 0)) * scale_factor + offset_x, (self:getY() - (self:getScaleY() < 0 and self.images[self.image_id]:getHeight()*(self:getScaleY()) or 0)) * scale_factor, self.images[self.image_id]:getWidth()*self:getScaleX()*scale_factor, self.images[self.image_id]:getHeight()*self:getScaleY()*scale_factor, 1, 1, 1, 1);
        end
        if self.next_id == self.image_id and self:getAlpha() == 1 then
            self.next_id = nil;
        elseif self.image_id then -- image to image transition
            r:drawTextureScaled(self.images[self.next_id], (self:getX() - (self:getScaleX() < 0 and self.images[self.image_id]:getWidth()*(self:getScaleX()) or 0)) * scale_factor + offset_x, (self:getY() - (self:getScaleY() < 0 and self.images[self.image_id]:getHeight()*(self:getScaleY()) or 0)) * scale_factor, self.images[self.next_id]:getWidth()*self:getScaleX()*scale_factor, self.images[self.next_id]:getHeight()*self:getScaleY()*scale_factor, self:getAlpha(), 1, 1, 1);
            if self:getAlpha() == 1 then self.image_id = self.next_id; end
        else  -- 'none' to image transition
            r:drawTextureScaled(self.images[self.next_id], (self:getX() - (self:getScaleX() < 0 and self.images[self.next_id]:getWidth()*(self:getScaleX()) or 0)) * scale_factor + offset_x, (self:getY() - (self:getScaleY() < 0 and self.images[self.next_id]:getHeight()*(self:getScaleY()) or 0)) * scale_factor, self.images[self.next_id]:getWidth()*self:getScaleX()*scale_factor, self.images[self.next_id]:getHeight()*self:getScaleY()*scale_factor, self:getAlpha(), 1, 1, 1);
            if self:getAlpha() == 1 then self.image_id = self.next_id; end
        end
    elseif self.image_id then
        r:drawTextureScaled(self.images[self.image_id], (self:getX() - (self:getScaleX() < 0 and self.images[self.image_id]:getWidth()*(self:getScaleX()) or 0)) * scale_factor + offset_x, (self:getY() - (self:getScaleY() < 0 and self.images[self.image_id]:getHeight()*(self:getScaleY()) or 0)) * scale_factor, self.images[self.image_id]:getWidth()*self:getScaleX()*scale_factor, self.images[self.image_id]:getHeight()*self:getScaleY()*scale_factor, self:getAlpha(), 1, 1, 1);
    end
end

function Motion:new(id, images)
    local o = {};
	setmetatable(o, self)
    self.__index = self;
    o.id = id;
    o.images = images or {};

    o.index = 1;
    o.queue = {}
    o.queue_size = 0;

    o.image_id = nil;
    o.next_id = nil;
    o.x = 0;
    o.y = 0;
    o.scale = { x=1, y=1 };
    o.alpha = 1;

    o.active = false;
    o.loop = false;
    o.master = nil;
    o.slave = nil;

    o.paused = false;

    o._start = 1;
    o._end = nil;
	return o;
end

SRN.getMotionIndex = function (id)
    for i=1, motions_size do
        if motions[i].id == id then
            return i;
        end
    end
end

local busy;
SRN.render = function (r)
    if QuestLogger.verbose then
        r:drawRect(0 + offset_x, 0, SRN.canvas[1] * scale_factor, SRN.canvas[2] * scale_factor, 0.2, 1, 0, 0);
        r:drawRectBorderStatic(0 + offset_x, 0, SRN.canvas[1] * scale_factor, SRN.canvas[2] * scale_factor, 0.2, 1, 0, 0);
        r:drawRectBorderStatic(0 + offset_x, 0, (SRN.canvas[1] * scale_factor)/ 2, SRN.canvas[2] * scale_factor, 1.5, 1, 0, 0);
        r:drawRectBorderStatic(0 + offset_x, 0, SRN.canvas[1] * scale_factor, (SRN.canvas[2] * scale_factor) / 2, 0.5, 1, 0, 0);
    end
    busy = false;
    local i = 1;
    while i <= motions_size do
        if motions[i].ready then
            if QuestLogger.verbose then
                r:drawText(string.format("ACTIVE=%s, ID=%s, LINE=%s/%s, LOOP=%s, MASTER=%s, CUR_IMG=%s, NEXT_IMG=%s, X=%i, Y=%i, A=%.2f", tostring(motions[i].active), tostring(motions[i].id),  tostring(motions[i].index), tostring(motions[i].queue_size), tostring(motions[i].loop), tostring(motions[i].master and motions[i].master.id or "none"), tostring(motions[i].image_id), tostring(motions[i].next_id), motions[i].x, motions[i].y, motions[i].alpha), 70, 200 + 20*(i-1), 1.0, 1.0, 1.0, 1.0, UIFont.NewSmall)
            end
            if motions[i].active then
                SRN.active = true;
                motions[i]:render(r);
                busy = true;
                if not motions[i].active then
                    motions[i]:reset();
                    motions[i]:release();
                end
            end
        else
            motions[i].ready = motions[i]:prepare();
        end
        i = i + 1;
    end
    if busy then return end
    SRN.active = false;
end

SRN.clear = function ()
    motions_size = 0;
    motions = {}
end

Events.OnScriptExit.Add(SRN.clear);


local type_dialogue = "DialoguePanel";

local motionCommands = {}
local function processCommand(command, target)
    for i=1, #motionCommands do
        if motionCommands[i]:validate_command(command) then
            return motionCommands[i]:execute(target);
        end
    end

    return string.format("Unknown command '%s'", tostring(command));
end

-- motion id|image1.png,image2.png,image3.png,...
local _motion = Command:derive("motion")
function _motion:execute(sender)
    self:debug();
    if SRN.getMotionIndex(self.args[1]) then
        return string.format("Motion with id '%s' already exists", self.args[1]);
    end
    local images;
    if #self.args == 2 then
        images = self.args[2]:ssplit(',');
        for i=1, #images do
            local path = tostring(images[i]);
            if not path:starts_with("media/ui/") then
                path = "media/ui/"..path;
            end
            useTextureFiltering(true);
            local status, tex = pcall(getTexture, path);
            useTextureFiltering(false);
            if status and tex then
                images[i] = tex; -- name, texture
            else
                return "Unable to load image";
            end
        end
    end
    local motion = Motion:new(self.args[1], images);
    motion.callback = function ()
        sender.input.enable = true;
        sender:showNext();
    end

    local skipped = 1;
    while GetBlockLayer(sender.script.lines[sender.script.index+skipped]) > sender.script.layer do
        local line = sender.script.lines[sender.script.index+skipped]:trim();
        if line:starts_with('#') then
            local result = processCommand(string.sub(line, 2), motion);
            if type(result) == "string" then
                sender.script.index = sender.script.index + skipped;
                return result;
            end
        end

        skipped = skipped + 1;
    end

    if motion.queue_size == 0 then
        return "Motion block is empty";
    end

    motions_size = motions_size + 1; motions[motions_size] = motion;
    sender.script.index = sender.script.index+skipped-1;
    sender.script.skip = -1;

    return -2;
end

-- motion_start id|async|loop|endless
local _motion_start = Command:derive("motion_start")
function _motion_start:execute(sender)
    self:debug();
    local index = SRN.getMotionIndex(self.args[1]);
    local async = self.args[2] == "true";
    local loop = self.args[3] == "true";
    local endless = self.args[4] == "true";
    local status, _start, _end;
    if self.args[5] then
        status, _start = pcall(tonumber, self.args[5]);
        if not status or not _start then return string.format("'start' is not a number", self.args[5]); end
    end
    if self.args[6] then
        status, _end = pcall(tonumber, self.args[6]);
        if not status or not _end then return string.format("'end' is not a number", self.args[6]); end
    end
    if index then
        if _start then
            local r = motions[index]:set_end(_end);
            if r then return r; end
            r = motions[index]:set_start(_start);
            if r then return r; end
            motions[index].paused = false;
        end
        if self.command == "motion_start" then
            motions[index]:reset();
        end
        motions[index].async = async;
        motions[index].loop = loop;
        motions[index].endless = endless;
        motions[index].active = true;
        SRN.active = true;
        if not motions[index].async then
            if self.command == "motion_update" then
                motions[index].rc = true;
            end
            return -2;
        end
    else
        return string.format("Motion with id '%s' doesn't exist", self.args[1]);
    end
end

-- motion_update id|async|loop|endless|start|end
local _motion_update = Command:derive("motion_update")
_motion_update.execute = _motion_start.execute;

-- motion_end id1|id2|id3
local _motion_end = Command:derive("motion_end")
function _motion_end:execute(sender)
    self:debug();
    for i=1, #self.args do
        local index = SRN.getMotionIndex(self.args[i]);
        if index then
            motions[index].active = false;
        end
    end
end

if QSystem.preview then
    CommandList_a[#CommandList_a+1] = _motion:new(_motion.Type, 1, 2, type_dialogue);
    CommandList_a[#CommandList_a+1] = _motion_start:new(_motion_start.Type, 1, 6, type_dialogue);
    CommandList_a[#CommandList_a+1] = _motion_update:new(_motion_update.Type, 5, 6, type_dialogue);
    CommandList_a[#CommandList_a+1] = _motion_end:new(_motion_end.Type, 1, 10, type_dialogue);
end

-- draw image_id|x,y|fade_out_speed
local command_1 = Command:derive("draw")
command_1.allow_tags = true;
function command_1:execute(target)
    self:debug();
    local status, image_id = pcall(tonumber, self.args[1]);
    if status and image_id then
        if not target.images[image_id] then
            return string.format("Image with id %s doesn't exist in motion '%s'", image_id, target.id);
        end
    else
        return "Image id is not a number";
    end
    local coords = self.args[2]:ssplit(',');
    if #coords == 2 then
        for i=1, 2 do
            status, coords[i] = pcall(tonumber, coords[i]);
            if not status or not coords[i] then
                return "Coordinate is not a number";
            end
        end
        local function callback()
            if target.queue[target.index][2] then -- fade out
                if target.queue[target.index][3] then
                    if target.alpha == 1 then
                        target.queue[target.index][3] = nil;
                        return true;
                    else
                        local a = target.alpha + 0.04 * (UIManager.getMillisSinceLastRender() / 33.3) * target.queue[target.index][2];
                        target.alpha = a > 1 and 1 or a;
                    end
                else
                    target.next_id = image_id;
                    target.x = coords[1];
                    target.y = coords[2];
                    target.alpha = 0;  -- set initial alpha
                    target.queue[target.index][3] = true;
                end
            else -- instant
                target.image_id = image_id;
                target.x = coords[1];
                target.y = coords[2];
                return true;
            end
        end
        if self.args[3] then
            local speed;
            status, speed = pcall(tonumber, self.args[3]);
            if not status or not speed then
                return "Fade out speed is not a number";
            end
            target.queue_size = target.queue_size + 1; target.queue[target.queue_size] = { callback, speed }
        else
            target.queue_size = target.queue_size + 1; target.queue[target.queue_size] = { callback }
        end
    else
        return "Invalid coordinates";
    end
end

-- fade_out speed
-- fade_out motion|speed
local command_2a = Command:derive("fade_out")
function command_2a:execute(target)
    self:debug();
    local m_index = nil;
    if #self.args == 2 then
        m_index = SRN.getMotionIndex(self.args[1]);
        if not m_index then return "Invalid motion id"; end
    end
    local motion = m_index and motions[m_index] or target;
    local speed = m_index and self.args[2] or self.args[1];
    local function callback()
        if motion.queue[motion.index][2] then -- fade out
            if motion.queue[motion.index][3] then
                if motion.alpha == 1 then
                    motion.queue[motion.index][3] = nil;
                    return true;
                else
                    local a = motion.alpha + 0.04 * (UIManager.getMillisSinceLastRender() / 33.3) * motion.queue[motion.index][2];
                    motion.alpha = a > 1 and 1 or a;
                end
            else
                motion.alpha = 0; -- set initial alpha
                motion.queue[motion.index][3] = true;
            end
        else -- instant
            motion.alpha = 1;
            motion.queue[motion.index][2] = 100;
            motion.queue[motion.index][3] = true;
        end
    end
    if speed then
        local status;
        status, speed = pcall(tonumber, speed);
        if not status or not speed then
            return "Fade out speed is not a number";
        end
        target.queue_size = target.queue_size + 1; target.queue[target.queue_size] = { callback, speed }
    else
        target.queue_size = target.queue_size + 1; target.queue[target.queue_size] = { callback }
    end
end

-- fade_in speed
-- fade_in motion|speed
local command_2b = Command:derive("fade_in")
function command_2b:execute(target)
    self:debug();
    local m_index = nil;
    if #self.args == 2 then
        m_index = SRN.getMotionIndex(self.args[1]);
        if not m_index then return "Invalid motion id"; end
    end
    local motion = m_index and motions[m_index] or target;
    local speed = m_index and self.args[2] or self.args[1];
    local function callback()
        if motion.queue[motion.index][2] then -- fade in
            if motion.queue[motion.index][3] then
                if motion.alpha == 0 then
                    motion.image_id = nil;
                    motion.queue[motion.index][3] = nil;
                    return true;
                else
                    local a = motion.alpha - 0.04 * (UIManager.getMillisSinceLastRender() / 33.3) * motion.queue[motion.index][2];
                    motion.alpha = a < 0 and 0 or a;
                end
            else
                motion.alpha = 1; -- set initial alpha
                motion.queue[motion.index][3] = true;
            end
        else -- instant
            motion.alpha = 0;
            motion.queue[motion.index][2] = 100;
            motion.queue[motion.index][3] = true;
        end
    end
    if speed then
        local status;
        status, speed = pcall(tonumber, speed);
        if not status or not speed then
            return "Fade in speed is not a number";
        end
        target.queue_size = target.queue_size + 1; target.queue[target.queue_size] = { callback, speed }
    else
        target.queue_size = target.queue_size + 1; target.queue[target.queue_size] = { callback }
    end
end

-- move x,y|speed
-- move x,y|speed|async
local command_3 = Command:derive("move")
command_3.allow_tags = true;
function command_3:execute(target)
    self:debug();
    local status;
    local coords = self.args[1]:ssplit(',');
    if #coords == 2 then
        for i=1, 2 do
            status, coords[i] = pcall(tonumber, coords[i]);
            if not status or not coords[i] then
                return "Coordinate is not a number";
            end
        end
        local function callback()
            if target.queue[target.index][2] then -- normal
                if target.queue[target.index][4] then
                    if target.x == coords[1] and target.y == coords[2] then
                        target.queue[target.index][4] = nil;
                        return true;
                    else
                        if target.x ~= coords[1] then
                            local x = target.x + 4 * (UIManager.getMillisSinceLastRender() / 33.3) * target.queue[target.index][2] * target.queue[target.index][3][1];
                            target.x = ((x > coords[1] and target.queue[target.index][3][1] == 1) or (x < coords[1] and target.queue[target.index][3][1] == -1)) and coords[1] or x;
                        end
                        if target.y ~= coords[2] then
                            local y = target.y + 4 * (UIManager.getMillisSinceLastRender() / 33.3) * target.queue[target.index][2] * target.queue[target.index][3][2];
                            target.y = ((y > coords[2] and target.queue[target.index][3][2] == 1) or (y < coords[2] and target.queue[target.index][3][2] == -1)) and coords[2] or y;
                        end
                    end
                else
                    target.queue[target.index][3] = { target.x < coords[1] and 1 or -1, target.y < coords[2] and 1 or -1 }
                    target.queue[target.index][4] = true;
                end
            else -- instant
                target.x = coords[1];
                target.y = coords[2];
                return true;
            end
        end
        if self.args[2] then
            local speed;
            status, speed = pcall(tonumber, self.args[2]);
            if not status or not speed then
                return "Speed is not a number";
            end
            target.queue_size = target.queue_size + 1; target.queue[target.queue_size] = { callback, speed }
        else
            target.queue_size = target.queue_size + 1; target.queue[target.queue_size] = { callback }
        end
    else
        return "Invalid scaling multipliers";
    end
end

-- scale x,y|speed
local command_4 = Command:derive("scale")
command_4.allow_tags = true;
function command_4:execute(target)
    self:debug();
    local status;
    local coords = self.args[1]:ssplit(',');
    if #coords == 2 then
        for i=1, 2 do
            status, coords[i] = pcall(tonumber, coords[i]);
            if not status or not coords[i] then
                return "Coordinate is not a number";
            end
        end
        local function callback()
            if target.queue[target.index][2] then -- normal
                if target.queue[target.index][4] then
                    if target.scale.x == coords[1] and target.scale.y == coords[2] then
                        target.queue[target.index][4] = nil;
                        return true;
                    else
                        if target.scale.x ~= coords[1] then
                            local x = target.scale.x + 0.04 * (UIManager.getMillisSinceLastRender() / 33.3) * target.queue[target.index][2] * target.queue[target.index][3][1];
                            target.scale.x = ((x > coords[1] and target.queue[target.index][3][1] == 1) or (x < coords[1] and target.queue[target.index][3][1] == -1)) and coords[1] or x;
                        end
                        if target.scale.y ~= coords[2] then
                            local y = target.scale.y + 0.04 * (UIManager.getMillisSinceLastRender() / 33.3) * target.queue[target.index][2] * target.queue[target.index][3][2];
                            target.scale.y = ((y > coords[2] and target.queue[target.index][3][2] == 1) or (y < coords[2] and target.queue[target.index][3][2] == -1)) and coords[2] or y;
                        end
                    end
                else
                    target.queue[target.index][3] = { target.scale.x < coords[1] and 1 or -1, target.scale.y < coords[2] and 1 or -1 }
                    target.queue[target.index][4] = true;
                end
            else -- instant
                target.scale.x = coords[1];
                target.scale.y = coords[2];
                return true;
            end
        end
        if self.args[2] then
            local speed;
            status, speed = pcall(tonumber, self.args[2]);
            if not status or not speed then
                return "Speed is not a number";
            end
            target.queue_size = target.queue_size + 1; target.queue[target.queue_size] = { callback, speed }
        else
            target.queue_size = target.queue_size + 1; target.queue[target.queue_size] = { callback }
        end
    else
        return "Invalid scaling multipliers";
    end
end

-- wait ms
local command_5 = Command:derive("wait")
function command_5:execute(target)
    self:debug();
    local status, ms = pcall(tonumber, self.args[1]);
    if status and ms then
        local function callback()
            if target.queue[target.index][2] then
                if getTimeInMillis() > target.queue[target.index][2] then
                    target.queue[target.index][2] = nil;
                    return true;
                end
            else
                target.queue[target.index][2] = getTimeInMillis() + ms;
            end
        end
        target.queue_size = target.queue_size + 1; target.queue[target.queue_size] = { callback }
    else
        return "Time is not a number";
    end
end

-- motion_start id|async|loop|endless
local command_6a = Command:derive("motion_start")
function command_6a:execute(target)
    self:debug();
    local id = tostring(self.args[1]);
    local async = self.args[2] == "true";
    local loop = self.args[3] == "true";
    local endless = self.args[4] == "true";
    local status, _start, _end;
    if self.args[5] then
        status, _start = pcall(tonumber, self.args[5]);
        if not status or not _start then return string.format("'start' is not a number", self.args[5]); end
    end
    if self.args[6] then
        status, _end = pcall(tonumber, self.args[6]);
        if not status or not _end then return string.format("'end' is not a number", self.args[6]); end
    end
    local index = SRN.getMotionIndex(id);
    if index then
        local function callback()
            if target.queue[target.index][2] then
                if motions[index].active then
                    if motions[index].async or motions[index].paused then
                        return true;
                    end
                else
                    target.queue[target.index][2] = false;
                    return true;
                end
            else
                if _start then
                    local r = motions[index]:set_end(_end);
                    if r then return r; end
                    r = motions[index]:set_start(_start);
                    if r then return r; end
                    motions[index].paused = false;
                end
                if self.command == "motion_start" then
                    motions[index]:reset();
                end
                motions[index].async = async;
                motions[index].loop = loop;
                motions[index].endless = endless;
                motions[index].master = target;
                target.slave = motions[index];
                motions[index].active = true;
                target.queue[target.index][2] = true;
                if motions[index].async then return true; end
            end
        end
        target.queue_size = target.queue_size + 1; target.queue[target.queue_size] = { callback }
    else
        return string.format("Motion with id '%s' doesn't exist", self.args[1]);
    end
end

-- motion_update id|async|loop|endless|start|end
local command_6b = Command:derive("motion_update")
command_6b.execute = command_6a.execute;

-- motion_end
local command_7 = Command:derive("motion_end") -- TODO: протестировать
function command_7:execute(target)
    self:debug();
    local function callback()
        for i=1, motions_size do
            if motions[i].master.id == target.id then
                motions[i].active = false;
                motions[i].master = nil;
                return true;
            end
        end
    end
    target.queue_size = target.queue_size + 1; target.queue[target.queue_size] = { callback }
end

-- exit
local command_8 = Command:derive("exit")
function command_8:execute(target)
    self:debug();
    local function callback()
        target:set_end(target.index-1);
        return true;
    end
    target.queue_size = target.queue_size + 1; target.queue[target.queue_size] = { callback }
end

motionCommands[#motionCommands+1] = command_1:new(command_1.Type, 2, 3);
motionCommands[#motionCommands+1] = command_2a:new(command_2a.Type, 0, 2);
motionCommands[#motionCommands+1] = command_2b:new(command_2b.Type, 0, 2);
motionCommands[#motionCommands+1] = command_3:new(command_3.Type, 1, 2);
motionCommands[#motionCommands+1] = command_4:new(command_4.Type, 1, 2);
motionCommands[#motionCommands+1] = command_5:new(command_5.Type, 1, nil);
motionCommands[#motionCommands+1] = command_6a:new(command_6a.Type, 1, 6);
motionCommands[#motionCommands+1] = command_6b:new(command_6b.Type, 5, 6);
motionCommands[#motionCommands+1] = command_7:new(command_7.Type, 0, nil);
motionCommands[#motionCommands+1] = command_8:new(command_8.Type, 0, nil);
