require "ISUI/ISPanelJoypad"

DetailsUI = ISPanelJoypad:derive("DetailsUI");

local FONT_HGT_SMALL = getTextManager():getFontHeight(UIFont.Small)


function DetailsUI:initialise()
	ISPanel.initialise(self);
  local player = getPlayer();
  local x = self:getWidth() / 2 - 150;
  local y = self:getHeight() - 450;
  local xx = 0;
  local yy = 0;
  local size = 0;
  if self.typeitems ~= 2 then
	xx = x - 85;
	yy = y / 2 + 20
	size = 200;
  else
	xx = x;
	yy = y / 2;
	size = 300;
  end

  self.trade = ISButton:new(xx, yy, size, size, "", self, DetailsUI.onClick);
  self.trade.internal = "ItemAndActive";
  self.trade:initialise();
  self.trade.backgroundColor.a = 0;
  self.trade.backgroundColorMouseOver.a = 100;
  self.trade:setImage(self:getTex());
  self.trade:forceImageSize(size-100, size-100);
  self.trade.textColor.a = 0;
  self:addChild(self.trade);

  
  self.toolTip = ISToolTip:new();
  self.toolTip:initialise();
  self.toolTip:setOwner(self);
  self.toolTip:setX(x + 145);
  self.toolTip:setY(y / 2 + 60);
  self.toolTip.followMouse = false;
  if #self.items:getDisplayName() < 15 then
	self.toolTip:setName(self.items:getDisplayName())
	self.toolTip.description = self.showtip;
  else self.toolTip.description = self.items:getDisplayName() .. "\n" .. self.showtip;
  end
  self.toolTip:doLayout();
  if self.typeitems == 1 then
	self.toolTip:setVisible(true);
  else self.toolTip:setVisible(false);
  end
  self:addChild(self.toolTip);

  if self.typeitems ~=2 then
  local inv = InventoryItemFactory.CreateItem(self.items:getModuleName() .. "." .. self.items:getName());
  self.toolRender = DoItemToolTip:new(inv, 20, self.height / 2 + 20, self.width - 45, self.height / 2 - 40)
  self.toolRender:initialise();
  self.toolRender:setVisible(true);
  self.toolRender:setCharacter(getPlayer());
  --self.toolRender.borderColor = {r=0, g=0, b=0, a=0};
  --self.toolRender.backgroundColor = {r=0, g=0, b=0, a=0};
  --self.toolRender.followMouse = false;
  self.toolRender:setOwner(self);
  --self.toolRender:setX(xx);
  --self.toolRender:setY(y / 2 + 200);
  --self.toolRender:setX(self.width + (getPlayerScreenWidth(getPlayer():getPlayerNum()) * 0.36));
  --self.toolRender:setY((y / 2) + (getPlayerScreenHeight(getPlayer():getPlayerNum()) * 0.51));
  self:addChild(self.toolRender);
  end
end

function DetailsUI:getTipHeight()
	return self.toolRender:getHeight();
end

function DetailsUI:getTex()
	local texture = nil;
	if self.typeitems == 1 then
		local icon = self.items:getIcon();
		if self.items:getIconsForTexture() and not self.items:getIconsForTexture():isEmpty() then
			icon = self.items:getIconsForTexture():get(0)
		end
		   texture = getTexture("Item_" .. icon);
		if texture == nil then
			texture = getTexture("media/textures/Item_" .. icon .. ".png")
			 if texture == nil then
			   local scriptItem = getScriptManager():FindItem("Base.Spiffo");
			   icon = scriptItem:getIcon();
			   texture = getTexture("Item_" .. icon);
			 end
		 end
	elseif self.typeitems == 2 then
	  texture = self.items:getTex();
	end
	return texture;
end

function DetailsUI:destroy()
	local inGame = MainScreen.instance and MainScreen.instance.inGame and not MainScreen.instance:getIsVisible()
	UIManager.setShowPausedMessage(inGame);
	self:setVisible(false);
	self:removeFromUIManager();
	if UIManager.getSpeedControls() and inGame then
		UIManager.getSpeedControls():SetCurrentGameSpeed(1);
	end
	if self.player ~= nil then
		setJoypadFocus(self.player, self.prevFocus);
	elseif self.joyfocus and self.joyfocus.focus == self then
		self.joyfocus.focus = self.prevFocus
		updateJoypadFocus(self.joyfocus)
	end
end

function DetailsUI:onClick(button)
	if self.onclick ~= nil then
		button.player = self.player;
		self.onclick(self.target, button, self.items);
	end
end

function DetailsUI:prerender()
	self:drawRect(0, 0, self.width, self.height, self.backgroundColor.a, self.backgroundColor.r, self.backgroundColor.g, self.backgroundColor.b);
	self:drawRectBorder(0, 0, self.width, self.height, self.borderColor.a, self.borderColor.r, self.borderColor.g, self.borderColor.b);

	if self.typeitems ~= 2 then
	 self:setHeight(self.toolRender:getBottom() + 25)
	--self:drawRect(20, self.height / 2 + 20, self.width - 45, self.height / 2 - 40, 0.3, 0, 0, 0);
	--self:drawRectBorder(20, self.height / 2 + 20, self.width - 45, self.height / 2 - 40, 1, 255, 255, 255);
	end
	if #self.items:getDisplayName() < 15 then
		self:drawText(self.text, (self:getWidth() / 2) - 150, 360, 1, 1, 1, 1, UIFont.Medium);
	else self:drawTextCentre(self.text, (self:getWidth() / 2), 360, 1, 1, 1, 1, UIFont.Small);
	end
end

function DetailsUI:onMouseUp(x, y)
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

function DetailsUI:onMouseUpOutside(x, y)
    if not self.moveWithMouse then return; end
    if not self:getIsVisible() then
        return;
    end

    self.moving = false;
    ISMouseDrag.dragView = nil;
end

function DetailsUI:onMouseDown(x, y)
    if not self.moveWithMouse then return; end
    if not self:getIsVisible() then
        return;
    end

    self.downX = x;
    self.downY = y;
    self.moving = true;
    self:bringToTop();
end

function DetailsUI:onMouseMoveOutside(dx, dy)
    if not self.moveWithMouse then return; end
    self.mouseOver = false;

    if self.moving then
        self:setX(self.x + dx);
        self:setY(self.y + dy);
        self:bringToTop();
    end
end

function DetailsUI:onMouseMove(dx, dy)
    if not self.moveWithMouse then return; end
    self.mouseOver = true;

    if self.moving then
        self:setX(self.x + dx);
        self:setY(self.y + dy);
        self:bringToTop();
    end
end

function DetailsUI:onGainJoypadFocus(joypadData)
    ISPanelJoypad.onGainJoypadFocus(self, joypadData);
		self:setISButtonForB(self.exit)
	self.joypadButtons = {}
end

function DetailsUI:onLoseJoypadFocus(joypadData)
	ISPanelJoypad.onLoseJoypadFocus(self, joypadData)
		self.exit:clearJoypadButton()
        self.trade:clearJoypadButton()
end

function DetailsUI:onJoypadBeforeDeactivate(joypadData)
	if self.removeIfJoypadDeactivated then 
		self:destroy()
	end
end

function DetailsUI:onJoypadDown(button)
	ISPanelJoypad.onJoypadDown(self, button)
end

function DetailsUI:additem(item)
	
end


function DetailsUI.CalcSize(width, height, text)
	local fontHgt = getTextManager():getFontHeight(UIFont.Medium)
	local textWid = 0
	local textHgt = 0
	local lines = text:split("\\n")
	for _,line in ipairs(lines) do
		textWid = math.max(textWid, getTextManager():MeasureStringX(UIFont.Medium, line))
		textHgt = textHgt + fontHgt
	end
	local buttonWid = 100
	if width < math.max(textWid + 20, buttonWid * 2 + 10) then
		width = math.max(textWid + 20, buttonWid * 2 + 10)
	end
	local buttonHgt = 25
	local padBottom = 10
	if height < 20 + textHgt + 20 + buttonHgt + padBottom then
		height = 20 + textHgt + 20 + buttonHgt + padBottom
	end
	return width,height
end


function DetailsUI:new(x, y, width, height, text, target, onclick, player, items, typeitems, showtip)
	text = text:gsub("\\n", "\n")
	width,height = DetailsUI.CalcSize(width, height, text)
	local o = ISPanelJoypad.new(self, x, y, width, height);
	local playerObj = player and getSpecificPlayer(player) or nil
	if y == 0 then
		if playerObj and playerObj:getJoypadBind() ~= -1 then
			o.y = getPlayerScreenTop(player) + (getPlayerScreenHeight(player) - height) / 2
		else
			o.y = o:getMouseY() - (height / 2)
		end
		o:setY(o.y)
	end
	if x == 0 then
		if playerObj and playerObj:getJoypadBind() ~= -1 then
			o.x = getPlayerScreenLeft(player) + (getPlayerScreenWidth(player) - width) / 2
		else
			o.x = o:getMouseX() - (width / 2)
		end
		o:setX(o.x)
	end
  o.backgroundColor = {r=0, g=0, b=0, a=0.8};
  o.borderColor = {r=0.4, g=0.4, b=0.4, a=1};
  o.trade = nil;
  o.toolTip = nil;
  o.toolRender = nil;
	o.anchorLeft = true;
	o.anchorRight = true;
	o.anchorTop = true;
	o.anchorBottom = true;
	o.typeitems = typeitems;
	o.showtip = showtip;
	o.text = text;
	o.target = target;
	o.onclick = onclick;
  o.items = items;
  o.player = player;
  o.moveWithMouse = false;
    return o;
end

