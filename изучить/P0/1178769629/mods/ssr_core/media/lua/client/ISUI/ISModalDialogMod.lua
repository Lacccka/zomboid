--***********************************************************
--**              	  ROBERT JOHNSON                       **
--**            UI display with a question or text         **
--**          can display a yes/no button or ok btn        **
--***********************************************************

require "ISUI/ISPanelJoypad"

ISModalDialogMod = ISPanelJoypad:derive("ISModalDialogMod");

ISModalDialogMod.drawMargins = false
local FONT_HGT_SMALL = getTextManager():getFontHeight(UIFont.NewSmall)

--************************************************************************--
--** ISModalDialogMod:initialise
--**
--************************************************************************--

function ISModalDialogMod:initialise()
	ISPanel.initialise(self);
	local btnWid = 100
	local btnHgt = math.max(25, FONT_HGT_SMALL + 3 * 2)
	local padBottom = 10
	if self.yesno then
		self.yes = ISButton:new((self:getWidth() / 2) - btnWid - 5, self:getHeight() - padBottom - btnHgt, btnWid, btnHgt, getText("UI_Yes"), self, ISModalDialogMod.onClick);
		self.yes.internal = "YES";
		self.yes:initialise();
		self.yes:instantiate();
		self.yes.borderColor = {r=1, g=1, b=1, a=0.1};
		self:addChild(self.yes);

		self.no = ISButton:new((self:getWidth() / 2) + 5, self:getHeight() - padBottom - btnHgt, btnWid, btnHgt, getText("UI_No"), self, ISModalDialogMod.onClick);
		self.no.internal = "NO";
		self.no:initialise();
		self.no:instantiate();
		self.no.borderColor = {r=1, g=1, b=1, a=0.1};
		self:addChild(self.no);
	else
		self.ok = ISButton:new((self:getWidth() / 2) - btnWid / 2, self:getHeight() - padBottom - btnHgt, btnWid, btnHgt, getText("UI_Ok"), self, ISModalDialogMod.onClick);
		self.ok.internal = "OK";
		self.ok:initialise();
		self.ok:instantiate();
		self.ok.borderColor = {r=1, g=1, b=1, a=0.1};
		self:addChild(self.ok);
	end
--	if UIManager.getSpeedControls() then
--		UIManager.getSpeedControls():SetCurrentGameSpeed(0);
--		UIManager.setShowPausedMessage(false);
--	end
end

function ISModalDialogMod:processCommand(command, x, y, lineImageHeight, lineHeight)
    if command == "LINE" then
        x = 0;
        lineImageHeight = 0;
        y = y + lineHeight;

    end
    if command == "BR" then
        x = 0;
        lineImageHeight = 0;
        y = y + lineHeight + lineHeight;

    end
    if command == "H1" then
        self.orient[self.currentLine] = "centre";
        self.rgb[self.currentLine] = {};
        self.rgb[self.currentLine].r = 1;
        self.rgb[self.currentLine].g = 1;
        self.rgb[self.currentLine].b = 1;
        self.font = UIFont.NewLarge;
        self.fonts[self.currentLine] = self.font;
    end
    if command == "H2" then
        self.orient[self.currentLine] = "left";
        self.rgb[self.currentLine] = {};
        self.rgb[self.currentLine].r = 0.8;
        self.rgb[self.currentLine].g = 0.8;
        self.rgb[self.currentLine].b = 0.8;
        self.font = UIFont.NewMedium;
        self.fonts[self.currentLine] = self.font;
    end
    if command == "TEXT" then
        self.orient[self.currentLine] = "left";
        self.rgb[self.currentLine] = {};
        self.rgb[self.currentLine].r = 0.7;
        self.rgb[self.currentLine].g = 0.7;
        self.rgb[self.currentLine].b = 0.7;
        self.font = UIFont.NewSmall;
        self.fonts[self.currentLine] = self.font;
    end
    if command == "CENTRE" then
        self.orient[self.currentLine] = "centre";
    end

    if command == "LEFT" then
        self.orient[self.currentLine] = "left";
    end

    if command == "RIGHT" then
        self.orient[self.currentLine] = "right";
    end
    if string.find(command, "RGB:") then
		local rgb = string.split(string.sub(command, 5, string.len(command)), ",");
		self.rgb[self.currentLine] = {};
		self.rgb[self.currentLine].r = tonumber(rgb[1]);
		self.rgb[self.currentLine].g = tonumber(rgb[2]);
		self.rgb[self.currentLine].b = tonumber(rgb[3]);
    end
    if string.find(command, "RED") then
        self.rgb[self.currentLine] = {};
        self.rgb[self.currentLine].r = 1;
        self.rgb[self.currentLine].g = 0;
        self.rgb[self.currentLine].b = 0;
    end
    if string.find(command, "ORANGE") then
        self.rgb[self.currentLine] = {};
        self.rgb[self.currentLine].r = 0.9;
        self.rgb[self.currentLine].g = 0.3;
        self.rgb[self.currentLine].b = 0;
    end
    if string.find(command, "GREEN") then
        self.rgb[self.currentLine] = {};
        self.rgb[self.currentLine].r = 0;
        self.rgb[self.currentLine].g = 1;
        self.rgb[self.currentLine].b = 0;
    end
    if string.find(command, "SIZE:") then

		local size = string.sub(command, 6);
--~         print(size);
		if(size == "small") then
			self.font = UIFont.NewSmall;
		end
		if(size == "medium") then
			self.font = UIFont.NewMedium;
		end
		if(size == "large") then
			self.font = UIFont.NewLarge;
		end
		self.fonts[self.currentLine] = self.font;
	end

    if string.find(command, "IMAGE:") ~= nil then
        local w = 0;
        local h = 0;
        if string.find(command, ",") ~= nil then
            local vs = string.split(command, ",");

            command = string.trim(vs[1]);
            w = tonumber(string.trim(vs[2]));
            h = tonumber(string.trim(vs[3]));

        end
		
		useTextureFiltering(true);
        self.images[self.imageCount] = getTexture(string.sub(command, 7));
		useTextureFiltering(false);
		
        if(w==0) then
            w = self.images[self.imageCount]:getWidth();
            h = self.images[self.imageCount]:getHeight();
        end
        if(x + w >= self.width - (self.marginLeft + self.marginRight)) then
            x = 0;
            y = y +  lineHeight;
        end

        if(lineImageHeight < (h / 2) + 8) then
            lineImageHeight = (h / 2) + 8;
        end

        if self.images[self.imageCount] == nil then
            --print("Could not find texture");
        end
        self.imageX[self.imageCount] = x+2;
        self.imageY[self.imageCount] = y;
        self.imageW[self.imageCount] = w;
        self.imageH[self.imageCount] = h;
        self.imageCount = self.imageCount + 1;
        x = x + w + 7;

        local c = 1;
        for i,v in ipairs(self.lines) do
            if self.lineY[c] == y then
                self.lineY[c] = self.lineY[c] + (h / 2) - 7;
            end
            c = c + 1;
        end

        y = y + (h / 2) - 7;
    end


    if string.find(command, "IMAGECENTRE:") ~= nil then
        local w = 0;
        local h = 0;
        if string.find(command, ",") ~= nil then
            local vs = string.split(command, ",");

            command = string.trim(vs[1]);
            w = tonumber(string.trim(vs[2]));
            h = tonumber(string.trim(vs[3]));

        end
		
		useTextureFiltering(true);
        self.images[self.imageCount] = getTexture(string.sub(command, 13));
		useTextureFiltering(false);
		
        if(w==0) then
            w = self.images[self.imageCount]:getWidth();
            h = self.images[self.imageCount]:getHeight();
        end
        if(x + w >= self.width - (self.marginLeft + self.marginRight)) then
            x = 0;
            y = y +  lineHeight;
        end

        if(lineImageHeight < (h / 2) + 8) then
            lineImageHeight = (h / 2) + 16;
        end

        if self.images[self.imageCount] == nil then
            --print("Could not find texture");
        end
        local mx = (self.width / 2) - self.marginLeft;
        self.imageX[self.imageCount] = mx - (w/2);
        self.imageY[self.imageCount] = y;
        self.imageW[self.imageCount] = w;
        self.imageH[self.imageCount] = h;
        self.imageCount = self.imageCount + 1;
        x = x + w + 7;

        local c = 1;
        for i,v in ipairs(self.lines) do
            if self.lineY[c] == y then
                self.lineY[c] = self.lineY[c] + (h / 2);
            end
            c = c + 1;
        end

        y = y + (h / 2);
    end


    return x, y, lineImageHeight;

end

function ISModalDialogMod:onMouseWheel(del)
	self:setYScroll(self:getYScroll() - (del*18));
    return true;
end

function ISModalDialogMod:paginate()
	local lines = 1;
	self.textDirty = false;
	self.imageCount = 1;
	self.font = UIFont.NewSmall;
	self.fonts = {};
	self.images = {}
	self.imageX = {}
	self.imageY = {}
	self.rgb = {};
    self.orient = {}

	self.imageW = {}
	self.imageH = {}

	self.lineY = {}
	self.lineX = {}
	self.lines = {}
	local bDone = false;
	local leftText = self.text..' ';
	local cur = 0;
	local y = 0;
	local x = 0;
	local lineImageHeight = 0;
	leftText = leftText:gsub("\n", " <LINE> ")
	if self.maxLines > 0 then
		local lines = leftText:split("<LINE>")
		for i=1,(#lines - self.maxLines) do
			table.remove(lines,1)
		end
		leftText = ' '
		for k,v in ipairs(lines) do
			leftText = leftText..v.." <LINE> "
		end
	end
	local maxLineWidth = self.maxLineWidth or (self.width - self.marginRight - self.marginLeft)
	-- Always go through at least once.
	while not bDone do
		cur = string.find(leftText, " ", cur+1);
		if cur ~= nil then
			local token = string.sub(leftText, 0, cur);
			if string.find(token, "<") and string.find(token, ">") then -- handle missing ' ' after '>'
				cur = string.find(token, ">") + 1;
				token = string.sub(leftText, 0, cur - 1);
			end
			leftText = string.sub(leftText, cur);
			cur = 1
			if string.find(token, "<") and string.find(token, ">") then
				if not self.lines[lines] then
					self.lines[lines] = ''
					self.lineX[lines] = x
					self.lineY[lines] = y
				end
				lines = lines + 1
				local st = string.find(token, "<");
				local en = string.find(token, ">");
				local escSeq = string.sub(token, st+1, en-1);
				local lineHeight = getTextManager():getFontFromEnum(self.font):getLineHeight();
				if lineHeight < 10 then
					lineHeight = 10;
				end
				if lineHeight < lineImageHeight then
					lineHeight = lineImageHeight;
				end
				self.currentLine = lines;
				x, y, lineImageHeight = self:processCommand(escSeq, x, y, lineImageHeight, lineHeight);
			else
				token = string.gsub(token, "&lt;", "<")
				token = string.gsub(token, "&gt;", ">")
				local chunkText = self.lines[lines] or ''
				local chunkX = self.lineX[lines] or x
				if chunkText == '' then
					chunkText = string.trim(token)
				else
					chunkText = chunkText..' '..string.trim(token)
				end
				local pixLen = getTextManager():MeasureStringX(self.font, chunkText);
				if chunkX + pixLen > maxLineWidth then
					if self.lines[lines] and self.lines[lines] ~= '' then
						lines = lines + 1;
					end
					local lineHeight = getTextManager():getFontFromEnum(self.font):getLineHeight();
					if lineHeight < lineImageHeight then
						lineHeight = lineImageHeight;
					end
					lineImageHeight = 0;
					y = y + lineHeight;
					x = 0;
					self.lines[lines] = string.trim(token)
					self.lineX[lines] = x
					self.lineY[lines] = y
					x = x + getTextManager():MeasureStringX(self.font, self.lines[lines])
				else
					if not self.lines[lines] then
						self.lines[lines] = ''
						self.lineX[lines] = x
						self.lineY[lines] = y
					end
					self.lines[lines] = chunkText
					x = self.lineX[lines] + pixLen
				end
			end
        else
			if string.trim(leftText) ~= '' then
				self.lines[lines] = string.trim(leftText);
				self.lineX[lines] = x;
				self.lineY[lines] = y;
				local lineHeight = getTextManager():getFontFromEnum(self.font):getLineHeight();
				y = y + lineHeight
			elseif self.lines[lines] and self.lines[lines] ~= '' then
				local lineHeight = getTextManager():getFontFromEnum(self.font):getLineHeight();
				if lineHeight < lineImageHeight then
					lineHeight = lineImageHeight;
				end
				y = y + lineHeight
			end
			bDone = true;
		end
	end
	if(self.autosetheight) then
		self:setHeight(self.marginTop + y + self.marginBottom);
    end

    self:setScrollHeight(self.marginTop + y + self.marginBottom);
end

function ISModalDialogMod:destroy()
	local inGame = MainScreen.instance and MainScreen.instance.inGame and not MainScreen.instance:getIsVisible()
	UIManager.setShowPausedMessage(inGame);
	self:setVisible(false);
	--self:removeFromUIManager();
	if UIManager.getSpeedControls() and inGame then
		UIManager.getSpeedControls():SetCurrentGameSpeed(1);
	end
end

function ISModalDialogMod:onClick(button)
	self:destroy();
	if self.onclick ~= nil then
		self.onclick(self.target, button, self.param1, self.param2);
	end
end

function ISModalDialogMod:prerender()
	self:drawRect(0, 0, self.width, self.height, self.backgroundColor.a, self.backgroundColor.r, self.backgroundColor.g, self.backgroundColor.b);
	self:drawRect(0, 0, self.width, 1, self.borderColor.a, self.borderColor.r, self.borderColor.g, self.borderColor.b);
	self:drawRect(0, self.height-1, self.width, 1, self.borderColor.a, self.borderColor.r, self.borderColor.g, self.borderColor.b);
	self:drawRect(0, 0, 1, self.height, self.borderColor.a, self.borderColor.r, self.borderColor.g, self.borderColor.b);
	self:drawRect(0+self.width-1, 0, 1, self.height, self.borderColor.a, self.borderColor.r, self.borderColor.g, self.borderColor.b);
	--self:drawRectBorder(0, 0, self.width, self.height, self.borderColor.a, self.borderColor.r, self.borderColor.g, self.borderColor.b);
	--self:drawTextCentre(self.text, self:getWidth() / 2, 20, 1, 1, 1, 1, UIFont.Small);
end

function ISModalDialogMod:onResize()
  --  ISUIElement.onResize(self);
	self.width = self:getWidth();
	self.height = self:getHeight();
    self.textDirty = true;
end

function ISModalDialogMod:setMargins(left, top, right, bottom)
	self.marginLeft = left
	self.marginTop = top
	self.marginRight = right
	self.marginBottom = bottom
end


function ISModalDialogMod:onMouseUp(x, y)
    if not self.moveWithMouse then return; end
    if not self:getIsVisible() then
        return;
    end

    self.moving = false;
    if ISMouseDrag.tabPanel then
        ISMouseDrag.tabPanel:onMouseUp(x,y);
    end

    ISMouseDrag.dragView = nil;
end

function ISModalDialogMod:onMouseUpOutside(x, y)
    if not self.moveWithMouse then return; end
    if not self:getIsVisible() then
        return;
    end

    self.moving = false;
    ISMouseDrag.dragView = nil;
end

function ISModalDialogMod:onMouseDown(x, y)
    if not self.moveWithMouse then return; end
    if not self:getIsVisible() then
        return;
    end

    self.downX = x;
    self.downY = y;
    self.moving = true;
    self:bringToTop();
end

function ISModalDialogMod:onMouseMoveOutside(dx, dy)
    if not self.moveWithMouse then return; end
    self.mouseOver = false;

    if self.moving then
        self:setX(self.x + dx);
        self:setY(self.y + dy);
        self:bringToTop();
    end
end

function ISModalDialogMod:onMouseMove(dx, dy)
    if not self.moveWithMouse then return; end
    self.mouseOver = true;

    if self.moving then
        self:setX(self.x + dx);
        self:setY(self.y + dy);
        self:bringToTop();
        --ISMouseDrag.dragView = self;
    end
end

function ISModalDialogMod:onGainJoypadFocus(joypadData)
--    print("gained modal focus");
    ISPanelJoypad.onGainJoypadFocus(self, joypadData);
	if self.yesno then
		self:setISButtonForA(self.yes)
		self:setISButtonForB(self.no)
	else
		self:setISButtonForA(self.ok)
	end
	self.joypadButtons = {}
end

function ISModalDialogMod:onJoypadDown(button)
	if button == Joypad.AButton then
       if self.yesno then
			self.yes.player = self.player;
			self.yes.onclick(self.yes.target, self.yes);
       else
			self.ok.onclick(self.ok.target, self.ok);
       end

       if(self.player ~= nil) then
            setJoypadFocus(self.player, nil);
       end
       self:destroy();
    end
    if button == Joypad.BButton then
        if self.yesno then
            self.no.player = self.player;
            self.no.onclick(self.no.target, self.no);
        else
            self.ok.onclick(self.ok.target, self.ok);
        end
       if(self.player ~= nil) then
            setJoypadFocus(self.player, nil);
       end
        self:destroy();
    end
end

--************************************************************************--
--** ISModalDialogMod:render
--**
--************************************************************************--
function ISModalDialogMod:render()

    self.r = 1;
    self.g = 1;
    self.b = 1;

	if self.lines == nil then
		return;
	end
	if self.clip then self:setStencilRect(0, 0, self.width, self.height) end
--	print "render";
	if self.textDirty then
		self:paginate();
	end
    local y = 0;
	--ISPanel.render(self);
    local c = 1;
    for i,v in ipairs(self.images) do
        if v == nil then

            --print("Tried to draw non-existant texture");
        end

        local h = self.imageY[c] + self.marginTop + self.imageH[c];
        if(h > y) then
            y = h;
        end
        self:drawTextureScaled(v, self.imageX[c] + self.marginLeft, self.imageY[c] + self.marginTop, self.imageW[c], self.imageH[c], 1, 1, 1, 1);
        c = c + 1;
    end
	c = 1;
    local orient = "left";
	y = 0;
	--self.font = UIFont.NewSmall
	for i,v in ipairs(self.lines) do

		if self.lineY[c] + self.marginTop + self:getYScroll() >= self:getHeight() then
			break
		end

		if self.rgb[c] then
			self.r = self.rgb[c].r;
			self.g = self.rgb[c].g;
			self.b = self.rgb[c].b;
		end

		if self.orient[c] then
			orient = self.orient[c];
		end

		if self.fonts[c] then
			self.font = self.fonts[c];
		end

		if i == #self.lines or (self.lineY[c+1] + self.marginTop + self:getYScroll() > 0) then
		
			local r = self.r;
			local b = self.b;
			local g = self.g;

			v = v:gsub("&lt;", "<")
			v = v:gsub("&gt;", ">")

			if string.trim(v) ~= "" then
				if orient == "centre" then
					self:drawTextCentre( string.trim(v), self.width / 2 , self.lineY[c] + self.marginTop, r, g, b,1, self.font);
				elseif orient == "right" then
					self:drawTextLeft( string.trim(v), self.lineX[c] + self.marginLeft, self.lineY[c] + self.marginTop, r, g, b,1, self.font);
				else
					self:drawText( string.trim(v), self.lineX[c] + self.marginLeft, self.lineY[c] + self.marginTop, r, g, b,1, self.font);
				end
			end

		end

		local h = self.lineY[c] + self.marginTop + 32;
		if(h > y) then
			y = h;
		end
		c = c + 1;
	end

	if ISModalDialogMod.drawMargins then
		self:drawRectBorder(0, 0, self.width, self:getScrollHeight(), 0.5,1,1,1)
		self:drawRect(self.marginLeft, 0, 1, self:getScrollHeight(), 1,1,1,1)
		local maxLineWidth = self.maxLineWidth or (self.width - self.marginRight - self.marginLeft)
		self:drawRect(self.marginLeft + maxLineWidth, 0, 1, self:getScrollHeight(), 1,1,1,1)
		self:drawRect(0, self.marginTop, self.width, 1, 1,1,1,1)
		self:drawRect(0, self:getScrollHeight() - self.marginBottom, self.width, 1, 1,1,1,1)
	end

	if self.clip then self:clearStencilRect() end
	--self:setScrollHeight(y);
end
--************************************************************************--
--** ISModalDialogMod:new
--**
--************************************************************************--
function ISModalDialogMod:new(x, y, width, height, yesno, target, onclick, player, param1, param2)
	local o = {}
	o = ISPanelJoypad:new(x, y, width, height);
	setmetatable(o, self);
    self.__index = self;
	o.x = x;
	o.y = y;
	o.name = nil;
	--local playerObj = player and getSpecificPlayer(player) or nil;
    o.backgroundColor = {r=0, g=0, b=0, a=0.6};
    o.borderColor = {r=0.4, g=0.4, b=0.4, a=1};
	o.width = width;
	o.height = height;
	local buttonWid = 100
	local buttonHgt = 25
	local padBottom = 10
	o.anchorLeft = true;
	o.anchorRight = false;
	o.anchorTop = true;
	o.anchorBottom = false;
	o.marginLeft = 20;
	o.marginTop = 10;
	o.marginRight = 10;
	o.marginBottom = 10;
	o.autosetheight = false;
	o.text = "";
	o.textDirty = false;
	o.textR = 1;
	o.textG = 1;
	o.textB = 1;
	o.clip = false
	o.maxLines = 0
	
	o.yesno = yesno;
	o.target = target;
	o.onclick = onclick;
	o.yes = nil;
    o.player = player;
	o.no = nil;
	o.ok = nil;
	o.param1 = param1;
	o.param2 = param2;
    return o;
end

