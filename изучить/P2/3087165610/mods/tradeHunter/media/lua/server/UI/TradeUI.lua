require "ISUI/ISPanelJoypad"

TradeUI = ISPanelJoypad:derive("TradeUI");

local FONT_HGT_SMALL = getTextManager():getFontHeight(UIFont.Small)
--local getItem = getSandboxOptions():getOptionByName("TradeHunter.tradeitems"):getValue();
local getItem = {};
local getItemSell = {};
local sellindex = 0;
local detailview = nil;
local toolTip = nil;
local tipview = nil;
local Tradeview = nil;
local getAutoR = nil;

function TradeUI:initialise()
	ISPanel.initialise(self);
  Tradeview = self;  
  getAutoR = getSandboxOptions():getOptionByName("TradeHunter.EnableReload"):getValue();
  --getItem = {};
  local player = getPlayer();
  local x = self:getWidth() / 2 - 225;
  local y = self:getHeight() - 450;
  local allItems = getScriptManager():getAllItems();

  self.Tip = ISToolTip:new();
  tipview = self.Tip;
  self.Tip:initialise();
  self.Tip:setOwner(self);
  self.Tip:setX(x);
  self.Tip:setY(self:getHeight() / 2 + 215);
  --self.Tip.maxLineWidth = 10;
  self.Tip.followMouse = false;
  --toolTip:setName(getItem[self.selected]:getDisplayName())
  self.Tip.description = player:getZombieKills() .. "C";
  local titem = InventoryItemFactory.CreateItem("Base.Money");
  local tex = titem:getTex():getName();
  self.Tip:setTexture(tex);
  self.Tip:doLayout();
  self:addChild(self.Tip);

  self.list = ISScrollingListBox:new(x, self:getHeight() / 2 - 150, 450, 400);
  self.list:initialise();
  self.list:instantiate();
  --self.list:setFont(UIFont.Medium,10);
  --self.list.itemPadY = 20;
  self.list.itemheight = 50;
  self.list.backgroundColor.a = 0;
  self.list.drawBorder = false;
  self.list.onMouseDown = self.onMouseDown2;
  self.list.doDrawItem = self.doDrawItemIcon;
  self:addChild(self.list);
  self:getItemList("normal", self.max);

  self.exit = ISButton:new(x+400, y-10, 50, 30, getText("UI_exit"), self, TradeUI.onClick);
  self.exit.internal = "EXIT";
  self.exit:initialise();
  self:addChild(self.exit);

  self.refresh = ISButton:new(x+300, y-10, 60, 30, getText("UI_refresh"), self, TradeUI.onClick);
  self.refresh.internal = "refresh";
  --self.refresh:setImage(getTexture("media/textures/refresh.png"));
  self.refresh:initialise();
  if getAutoR then self.refresh:setVisible(false); end
  self:addChild(self.refresh);
end

function TradeUI:getItemList(type, count)
    local player = getPlayer()
    local aitems = ArrayList.new(getAllItems());
    local getIncludeitem = getSandboxOptions():getOptionByName("TradeHunter.tradeitems"):getValue();
    local intem = nil;
    if getIncludeitem ~= nil then intem = string.split(getIncludeitem,","); end
    local getExcludeitem = getSandboxOptions():getOptionByName("TradeHunter.tradeExcludeitems"):getValue();
    if getExcludeitem ~= nil then
     local ex = string.split(getExcludeitem,",");
     for i=1,#ex do
        --local fitem = InventoryItemFactory.CreateItem(tostring(ex[i]))
        local fitem = getScriptManager():FindItem(ex[i]);
        if fitem ~= nil then
          aitems:remove(fitem)
        end
     end
    end
    local readfile = getFileReader("items/[" .. getWorld():getWorld() .. "]items.ini", false)
    if readfile ~= nil then
        while true do
          local line = readfile:readLine()
          if line == nil then
            readfile:close()
            break
          end
          if string.contains(line, "/") then
           local readitems = string.split(line,"/");
           self.itemslist[readitems[1]] = line;
          end
        end
    end

    for i=1,count*10 do
        local randomitem = ZombRand(0,aitems:size());
        local randsell = ZombRand(1,30);
        local item = aitems:get(randomitem);

        if self.list.count > 6 then
            break;
        end
       
        if getItem[i] ~= nil then
          item = getItem[i];  
        end
        
        if item ~= nil then
         if intem[i] ~= nil and intem[i] ~= "" and getItem[i] == nil then 
            if getScriptManager():FindItem(intem[i]) ~= nil then
             item = getScriptManager():FindItem(intem[i]); 
            end
         end

         local base = item:getModuleName() .. "." .. item:getName()
         if self.itemslist[base] ~= nil then
            local readiniSell = string.split(self.itemslist[base],"/")
            if readiniSell[4] ~= nil and readiniSell[4] ~= "RANDOM" then
                randsell = readiniSell[4];
            end
        end

        if i == 1 and ZombRand(1,100) == 1 then
            item = getScriptManager():FindItem("tradeHunter.SpawnHunter");
            randsell = 500;
        end

         local icon = item:getIcon();
           if item:getIconsForTexture() and not item:getIconsForTexture():isEmpty() then
               icon = item:getIconsForTexture():get(0)
           end
           if icon and not string.contains(item:getDisplayName(),"base.") and not string.contains(item:getDisplayName(),"Base.") then
             --local texture = getTexture("Item_" .. icon);
               --player:Say(tostring(icon))
               table.insert(getItem, item);
               table.insert(getItemSell, randsell)
               if type == "normal" then self.list:addItem(item:getDisplayName(),item); end
               if type == "refresh" then 
                 --self.list:removeItemByIndex(i);
                 self.list:insertItem(i, item:getDisplayName(), item); 
               end
               
               --self.list.itemheight = FONT_HGT_SMALL + 4 * 2;
               --25
           end
       end
   end
end

function TradeUI:clear()
    getItem = {};
    getItemSell = {};
    self.list:clear();
    self:getItemList("refresh", self.max);
end

function TradeUI:doDrawItemIcon(y, itemlist, alt)
	if not itemlist.height then itemlist.height = self.itemheight end -- compatibililty
    if self.selected == itemlist.index then
        self:drawRect(0, (y), self:getWidth(), itemlist.height-1, 0.3, 0.7, 0.35, 0.15);

    end
	self:drawRectBorder(0, (y), self:getWidth(), itemlist.height, 0.5, self.borderColor.r, self.borderColor.g, self.borderColor.b);
	local itemPadY = self.itemPadY or (itemlist.height - self.fontHgt) / 2
	self:drawText(itemlist.text, 65, (y)+itemPadY, 0.9, 0.9, 0.9, 0.9, self.font);
    --local scriptItem = getScriptManager():FindItem("Base.Axe");
    local scriptItem = getItem[itemlist.index];
    --getPlayer():Say(tostring(scriptItem))
    if scriptItem ~= nil then
        local icon = scriptItem:getIcon()
        if scriptItem:getIconsForTexture() and not scriptItem:getIconsForTexture():isEmpty() then
            icon = scriptItem:getIconsForTexture():get(0)
        end
        if icon then
			local texture = getTexture("Item_" .. icon)
            if texture == nil then
                texture = getTexture("media/textures/Item_" .. icon .. ".png")
                if texture == nil then
                local scriptItem = getScriptManager():FindItem("Base.Spiffo");
                icon = scriptItem:getIcon();
                texture = getTexture("Item_" .. icon);
                end
            end
			if texture then
                self:drawTextureScaledAspect2(texture, -200, (y+5), self:getWidth(), itemlist.height-10,  1, 1, 1, 1);
            end
        end
    end
	y = y + itemlist.height;
	return y;

end

function TradeUI:destroy()
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

function TradeUI:onClick(button)
	if button.internal == "EXIT" then 
        getPlayer():playSound("come-back");
        self:destroy(); 
        if detailview ~= nil then detailview:destroy(); end
    end
    if button.internal == "refresh" then 
        local allItems = getScriptManager():getAllItems();
        local credit = getPlayer():getZombieKills();
        local itemcount = self.max;
        if credit > 4 and not getAutoR then
         getPlayer():setZombieKills(credit-5);
         self.Tip.description = credit-5 .. "C";
         getItem = {};
         getItemSell = {};
         self.list:clear();
         self:getItemList("refresh", self.max);
         else self.text = getText("UI_checkrefresh"); 
        end
    end
	if self.onclick ~= nil then
		button.player = self.player;
		self.onclick(self.target, button, self.max);
	end
end


function DetailsUI_onClick(dum, button, item)
 getPlayer():Say("buy item!");
 local player = getPlayer();
 local haveCredit = player:getZombieKills();
 local sell = getItemSell[sellindex]
 player:Say(item:getName());
 if haveCredit >= sell then
 getPlayer():playSound("buying");
 player:getInventory():AddItem(item:getName());
 haveCredit = haveCredit - sell;
 player:setZombieKills(haveCredit);
 --player:Say("Trade finish!");
 Tradeview.text = getText("UI_purchaseitem") .. item:getDisplayName();
 tipview.description = haveCredit;
 if getAutoR then
    getItem = {};
    getItemSell = {};
    Tradeview.list:clear();
    Tradeview:getItemList("refresh", Tradeview.max);
 end
 else 
    Tradeview.text = getText("UI_notpurchaseitem");
    getPlayer():playSound("no-cash")
 end
end

function TradeUI:onMouseDown2(x, y)

	if #self.items == 0 then return end
	local row = self:rowAt(x, y)

	if row > #self.items then
		row = #self.items;
	end
	if row < 1 then
		row = 1;
	end

	getSoundManager():playUISound("UISelectListItem")

    self.selected = row;


	if getItem[self.selected] ~= nil then
        --getPlayer():Say(getItem[self.selected]:getDisplayName())
        local inv = InventoryItemFactory.CreateItem(getItem[self.selected]:getModuleName() .. "." .. getItem[self.selected]:getName());
        --ISInventoryPaneContextMenu.information(inv);
        sellindex = self.selected;
        local sell = getItemSell[self.selected] .. "C"
        local showdetail = "";
        local getWeight = tostring(round(inv:getWeight(), 3));
        if getItem[self.selected]:getDisplayCategory() ~= nil then
        showdetail = "\n" .. getText("UI_type") .. getItem[self.selected]:getDisplayCategory() .. "\n" .. getText("UI_weight") .. getWeight .. "\n" .. getText("UI_price") .. sell
        else showdetail = "\n" .. getText("UI_type") .. getItem[self.selected]:getTypeString() .. "\n" .. getText("UI_weight") .. getWeight .. "\n" .. getText("UI_price") .. sell
        end
        local x = getPlayerScreenLeft(getPlayer():getPlayerNum()) + (getPlayerScreenWidth(getPlayer():getPlayerNum()) - 500) / 2
        local y = getPlayerScreenTop(getPlayer():getPlayerNum()) + (getPlayerScreenHeight(getPlayer():getPlayerNum()) - 500) / 2
        if detailview ~= nil then
            detailview:destroy(); 
        end
        detailview = DetailsUI:new(x + 500, y, 530, 530, "", self.target, DetailsUI_onClick, getPlayer():getPlayerNum(), getItem[self.selected], 1, showdetail)
        detailview:initialise();
        detailview:addToUIManager();
        self.detailview = detailview;
	end
end


function TradeUI:prerender()
	self:drawRect(0, 0, self.width, self.height, self.backgroundColor.a, self.backgroundColor.r, self.backgroundColor.g, self.backgroundColor.b);
	self:drawRectBorder(0, 0, self.width, self.height, self.borderColor.a, self.borderColor.r, self.borderColor.g, self.borderColor.b);
	self:drawTextCentre(self.text, (self:getWidth() / 2), 20, 204, 0, 0, 1, UIFont.Large);
    local logoitem = InventoryItemFactory.CreateItem("tradeHunter.SpawnHunter");
    local logoTexture = logoitem:getTex();
    local getX = getTextManager():MeasureStringX(UIFont.Large, self.text);
    self:drawTextureScaledAspect2(logoTexture, (self:getWidth() / 2) - (getX / 2) - 32, 15, 30, 30,  1, 1, 1, 1);
end

function TradeUI:onMouseUp(x, y)
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

function TradeUI:onMouseUpOutside(x, y)
    if not self.moveWithMouse then return; end
    if not self:getIsVisible() then
        return;
    end

    self.moving = false;
    ISMouseDrag.dragView = nil;
end

function TradeUI:onMouseDown(x, y)
    if not self.moveWithMouse then return; end
    if not self:getIsVisible() then
        return;
    end

    self.downX = x;
    self.downY = y;
    self.moving = true;
    self:bringToTop();
end

function TradeUI:onMouseMoveOutside(dx, dy)
    if not self.moveWithMouse then return; end
    self.mouseOver = false;

    if self.moving then
        self:setX(self.x + dx);
        self:setY(self.y + dy);
        self:bringToTop();
    end
end

function TradeUI:onMouseMove(dx, dy)
    if not self.moveWithMouse then return; end
    self.mouseOver = true;

    if self.moving then
        self:setX(self.x + dx);
        self:setY(self.y + dy);
        self:bringToTop();
    end
end

function TradeUI:onGainJoypadFocus(joypadData)
    ISPanelJoypad.onGainJoypadFocus(self, joypadData);
		self:setISButtonForB(self.exit)
	self.joypadButtons = {}
end

function TradeUI:onLoseJoypadFocus(joypadData)
	ISPanelJoypad.onLoseJoypadFocus(self, joypadData)
		self.exit:clearJoypadButton()
end

function TradeUI:onJoypadBeforeDeactivate(joypadData)
	if self.removeIfJoypadDeactivated then 
		self:destroy()
	end
end

function TradeUI:onJoypadDown(button)
	ISPanelJoypad.onJoypadDown(self, button)
end

function TradeUI:toView()
	if detailview~=nil then return detailview end
end

function TradeUI:toView2()
	if toolTip~=nil then return toolTip end
end

function TradeUI.CalcSize(width, height, text)
	local fontHgt = getTextManager():getFontHeight(UIFont.Large)
	local textWid = 0
	local textHgt = 0
	local lines = text:split("\\n")
	for _,line in ipairs(lines) do
		textWid = math.max(textWid, getTextManager():MeasureStringX(UIFont.Large, line))
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


function TradeUI:new(x, y, width, height, text, target, onclick, player, max)
	text = text:gsub("\\n", "\n")
	width,height = TradeUI.CalcSize(width, height, text)
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
  o.exit = nil;
  o.refresh = nil;
  o.list = nil;
  o.Tip = nil;
  o.trade = nil;
  o.detailview = self.detailview;
  o.backgroundColor = {r=0, g=0, b=0, a=0.8};
  o.borderColor = {r=0.4, g=0.4, b=0.4, a=1};
	o.anchorLeft = true;
	o.anchorRight = true;
	o.anchorTop = true;
	o.anchorBottom = true;
	o.text = text;
	o.target = target;
	o.onclick = onclick;
  --o.items = items;
  o.max = max;
  o.player = player;
  o.itemslist = {};
  o.moveWithMouse = false;
    return o;
end

