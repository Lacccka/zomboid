-- Copyright (c) 2022-2025 Oneline/D.Borovsky
-- All rights reserved
require "Communications/QSystem"

local screen, screen_width, screen_height;

VFX = {};

VFX.Dissolve = {}
local timer = 0;
local timestamp = nil

VFX.Dissolve.color = { r = 0, g = 0, b = 0 }; -- target color
VFX.Dissolve.r, VFX.Dissolve.g, VFX.Dissolve.b, VFX.Dissolve.a = 0, 0, 0, 0; -- current color
VFX.Dissolve.speed = 1;
VFX.Dissolve.fade = 0;
VFX.Dissolve.callback = nil;

local delta_r, delta_g, delta_b = 0, 0, 0;
VFX.Dissolve.setFadeOut = function (callback, color, speed)
    if VFX.Dissolve.color.r == color.r and VFX.Dissolve.color.g == color.g and VFX.Dissolve.color.b == color.b and VFX.Dissolve.a == 1 then -- already faded
        if callback then
            SSRTimer.add_s(callback, 1, false);
            return true;
        end
        return;
    elseif VFX.Dissolve.a == 0 then -- set intial color
        VFX.Dissolve.r, VFX.Dissolve.g, VFX.Dissolve.b = color.r, color.g, color.b;
    end

    if VFX.Dissolve.fade == 0 then
        timer = 0;
        VFX.Dissolve.callback = callback;
        timestamp = getTimeInMillis();
        VFX.Dissolve.color = color;
        VFX.Dissolve.speed = speed or 1;
        delta_r, delta_g, delta_b = (VFX.Dissolve.color.r - VFX.Dissolve.r) * 0.04 * VFX.Dissolve.speed, (VFX.Dissolve.color.g - VFX.Dissolve.g) * 0.04 * VFX.Dissolve.speed, (VFX.Dissolve.color.b - VFX.Dissolve.b) * 0.04 * VFX.Dissolve.speed;
        VFX.Dissolve.fade = 1;
        return true;
    else -- if still in progress
        VFX.Dissolve.r, VFX.Dissolve.g, VFX.Dissolve.b,  VFX.Dissolve.a = VFX.Dissolve.color.r, VFX.Dissolve.color.g, VFX.Dissolve.color.b, VFX.Dissolve.fade == 1 and 1 or 0;
        VFX.Dissolve.fade = 0;
        return VFX.Dissolve.setFadeOut(callback, color, speed);
    end
end

VFX.Dissolve.setFadeIn = function (callback, speed)
    if VFX.Dissolve.fade == 0 then
        if VFX.Dissolve.a ~= 0 then
            timer = 0;
            VFX.Dissolve.callback = callback;
            timestamp = getTimeInMillis();
            VFX.Dissolve.fade = 2;
            VFX.Dissolve.speed = speed or 1;
            return true;
        elseif callback then
            SSRTimer.add_s(callback, 1, false);
            return true;
        end
    else -- if still in progress
        VFX.Dissolve.r, VFX.Dissolve.g, VFX.Dissolve.b,  VFX.Dissolve.a = VFX.Dissolve.color.r, VFX.Dissolve.color.g, VFX.Dissolve.color.b, VFX.Dissolve.fade == 1 and 1 or 0;
        VFX.Dissolve.fade = 0;
        return VFX.Dissolve.setFadeIn(callback, speed);
    end
end

VFX.Dissolve.cancel = function ()
    VFX.Dissolve.fade = 0;
	VFX.Dissolve.a = 0;
	VFX.Dissolve.callback = nil;
end

VFX.Dissolve.update = function ()
    if VFX.Dissolve.fade == 0 then return end

    if timer > 0 then
        timer = timer - getDeltaTimeInMillis(timestamp)
        timestamp = getTimeInMillis();
    else
        timer = 5;
        if VFX.Dissolve.fade == 1 then
            if VFX.Dissolve.r ~= VFX.Dissolve.color.r or VFX.Dissolve.g ~= VFX.Dissolve.color.g or VFX.Dissolve.b ~= VFX.Dissolve.color.b then
                local r = delta_r + VFX.Dissolve.r;
                if (delta_r > 0 and r - VFX.Dissolve.color.r > -0.04) or (delta_r < 0 and r - VFX.Dissolve.color.r < 0.04) then
                    VFX.Dissolve.r = VFX.Dissolve.color.r;
                else
                    VFX.Dissolve.r = r;
                end
                local g = delta_g + VFX.Dissolve.g;
                if (delta_g > 0 and g - VFX.Dissolve.color.g > -0.04) or (delta_g < 0 and g - VFX.Dissolve.color.g < 0.04) then
                    VFX.Dissolve.g = VFX.Dissolve.color.g;
                else
                    VFX.Dissolve.g = g;
                end
                local b = delta_b + VFX.Dissolve.b;
                if (delta_b > 0 and b - VFX.Dissolve.color.b > -0.04) or (delta_b < 0 and b - VFX.Dissolve.color.b < 0.04) then
                    VFX.Dissolve.b = VFX.Dissolve.color.b;
                else
                    VFX.Dissolve.b = b;
                end
            elseif VFX.Dissolve.a == 1 then
                VFX.Dissolve.fade = 0;
                if VFX.Dissolve.callback then
                    VFX.Dissolve.callback();
                end
            end
        elseif VFX.Dissolve.fade == 2 then
            if VFX.Dissolve.a == 0 then
                VFX.Dissolve.fade = 0;
                if VFX.Dissolve.callback then
                    VFX.Dissolve.callback();
                end
            end
        end
        timestamp = getTimeInMillis();
    end
end


VFX.SpriteRenderer = {};
local bg_tex, fg_tex, bg_buf, fg_buf;

VFX.SpriteRenderer.setBackground = function (texture)
    local width, height = getCore():getScreenWidth(), getCore():getScreenHeight()
    bg_buf = { texture, (height / texture:getHeight()) * texture:getWidth(), height};
end

VFX.SpriteRenderer.setForeground = function (texture)
    local width, height = getCore():getScreenWidth(), getCore():getScreenHeight()
    fg_buf = { texture, (height / texture:getHeight()) * texture:getWidth(), height};
end

VFX.SpriteRenderer.clearBackground = function()
    bg_tex = nil; bg_buf = nil;
end

VFX.SpriteRenderer.clearForeground = function()
    fg_tex = nil; fg_buf = nil;
end

function VFX.prerender()
    if QSystem.preview then SRN.render(screen) end
    if bg_buf and bg_buf ~= bg_tex then
        if bg_buf[1]:isReady() then
            bg_tex = bg_buf; bg_buf = nil;
        end
    end
    if bg_tex then
        if bg_tex[2] ~= screen_width then
            screen:drawRect(0, 0, screen_width, screen_height, 1, 0, 0, 0);
        end
        screen:drawTextureScaled(bg_tex[1], (screen_width / 2) - (bg_tex[2] / 2), 0, bg_tex[2], bg_tex[3], 1, 1, 1, 1);
    end
    if fg_buf and fg_buf ~= fg_tex then
        if fg_buf[1]:isReady() then
            fg_tex = fg_buf; fg_buf = nil;
        end
    end
    if fg_tex then
        screen:drawTextureScaled(fg_tex[1], (screen_width / 2) - (fg_tex[2] / 2), 0, fg_tex[2], fg_tex[3], 1, 1, 1, 1);
    end
    if VFX.Dissolve.fade == 1 then
        if VFX.Dissolve.a < 1 then
            local a = VFX.Dissolve.a + 0.04 * (UIManager.getMillisSinceLastRender() / 33.3) * VFX.Dissolve.speed;
            VFX.Dissolve.a = a > 1 and 1 or a;
        end
    elseif VFX.Dissolve.fade == 2 then
        if VFX.Dissolve.a > 0 then
            local a = VFX.Dissolve.a - 0.04 * (UIManager.getMillisSinceLastRender() / 33.3) * VFX.Dissolve.speed;
            VFX.Dissolve.a = a < 0 and 0 or a;
        end
    end
    if VFX.Dissolve.a > 0 then
        screen:drawRect(0, 0, screen_width, screen_height, VFX.Dissolve.a, VFX.Dissolve.r, VFX.Dissolve.g, VFX.Dissolve.b);
    end
end

function VFX.update()
    screen:setVisible(Blocker.enabled or VFX.Dissolve.a ~= 0 or bg_tex ~= nil or fg_tex ~= nil or SRN.active)
end

function VFX.onResolutionChange(oldw, oldh, neww, newh)
    screen_width = neww;
    screen_height = newh;

    if bg_tex then
        bg_tex[2] = (newh / bg_tex[1]:getHeight()) * bg_tex[1]:getWidth();
        bg_tex[3] = newh;
    end
    if fg_tex then
        fg_tex[2] = (newh / fg_tex[1]:getHeight()) * fg_tex[1]:getWidth();
        fg_tex[3] = newh;
    end
end

function VFX.init()
    if not screen then
        screen_width, screen_height =  getCore():getScreenWidth(), getCore():getScreenHeight();
        screen = ISUIElement:new (0, 0, screen_width, screen_height);
        screen.Type = "VFX";
        screen.onFocus = function() end
        screen.bringToTop = function() end
        screen.onMouseUp = function() return false end
        screen.onMouseDown = function() return false end
        screen:initialise();
        screen:backMost();
        screen:addToUIManager();
        Events.OnTick.Add(VFX.update)
        Events.OnTick.Add(VFX.Dissolve.update)
        Events.OnPreUIDraw.Add(VFX.prerender)
        Events.OnResolutionChange.Add(VFX.onResolutionChange);
    end
end

Events.OnQSystemInit.Add(VFX.init);


function VFX.onScriptExit()
    VFX.SpriteRenderer.clearBackground();
	VFX.SpriteRenderer.clearForeground();
	VFX.Dissolve.cancel();
end

Events.OnScriptExit.Add(VFX.onScriptExit);