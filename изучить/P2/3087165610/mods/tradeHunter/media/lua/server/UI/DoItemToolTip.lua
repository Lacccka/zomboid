require "ISUI/ISPanel"

DoItemToolTip = ISPanel:derive("DoItemToolTip");


function DoItemToolTip:initialise()
	ISPanel.initialise(self);
end

function DoItemToolTip:setItem(item)
	self.item = item;
end

function DoItemToolTip:onMouseDown(x, y)
	return false
end

function DoItemToolTip:onMouseUp(x, y)
	return false
end

function DoItemToolTip:onRightMouseDown(x, y)
	return false
end

function DoItemToolTip:onRightMouseUp(x, y)
	return false
end

--************************************************************************--
--** DoItemToolTip:render
--**
--************************************************************************--

function DoItemToolTip:prerender()
	if self.owner and not self.owner:isReallyVisible() then
		self:removeFromUIManager()
		self:setVisible(false)
		return
	end
end

function DoItemToolTip:render()
	
   
        local mx = self:getX()
        local my = self:getY()


        self.tooltip:setX(mx+11);
        self.tooltip:setY(my);

        self.tooltip:setWidth(self:getWidth())
        self.tooltip:setMeasureOnly(true)
        self.item:DoTooltip(self.tooltip);
        self.tooltip:setMeasureOnly(false)

     -- clampy x, y

     local myCore = getCore();
     local maxX = myCore:getScreenWidth();
     local maxY = myCore:getScreenHeight();

     local tw = self.tooltip:getWidth();
     local th = self.tooltip:getHeight();

	 --self.tooltip:setX(mx+11);
	 --self.tooltip:setY(my);

	 --self.tooltip:setX(maxX / 2);
	 --self.tooltip:setY(maxY / 2);
     
     self.tooltip:setX(mx + (maxX * 0.635));
     self.tooltip:setY(my + (maxY * 0.275));
     if maxX == 2560 and maxY == 1440 then
      self.tooltip:setX(mx + (maxX * 0.6));
      self.tooltip:setY(my + (maxY * 0.33));
     end
     if maxX > 2560 and maxY > 1440 then
      local subX =  (maxX - 2560) * 0.000546875;
      local subY =  (maxY - 1440) * 0.000152777;
      self.tooltip:setX(mx + (maxX * (0.6 - subX)));
      self.tooltip:setY(my + (maxY * (0.33 + subY)));
     end
	  self:setHeight(th + 10);

     --self:setX(self.tooltip:getX() - 11);
     --self:setY(self.tooltip:getY());

     self:drawRect(0, 0, self.width, self.height, self.backgroundColor.a, self.backgroundColor.r, self.backgroundColor.g, self.backgroundColor.b);
     self:drawRectBorder(0, 0, self.width, self.height, self.borderColor.a, self.borderColor.r, self.borderColor.g, self.borderColor.b);
     self.item:DoTooltip(self.tooltip);
end

function DoItemToolTip:setOwner(ui)
	self.owner = ui
end

function DoItemToolTip:setCharacter(chr)
	self.tooltip:setCharacter(chr)
end

--************************************************************************--
--** DoItemToolTip:new
--**
--************************************************************************--
function DoItemToolTip:new(item, x, y, w, h)
   local o = {}
   o = ISPanel:new(x, y, w, h);
   setmetatable(o, self)
   self.__index = self
   o.tooltip = ObjectTooltip.new();
   o.item = item;

   o.tooltip:setX(0);
   o.tooltip:setY(0);

   o.x = x;
   o.y = y;

   o.toolTipDone = false;

   o.backgroundColor = {r=0, g=0, b=0, a=0.3};
   o.borderColor = {r=255, g=255, b=255, a=1};
   o.width = w;
   o.height = h;
   o.anchorLeft = false;
   o.anchorRight = false;
   o.anchorTop = false;
   o.anchorBottom = false;

   o.owner = nil
   return o;
end
