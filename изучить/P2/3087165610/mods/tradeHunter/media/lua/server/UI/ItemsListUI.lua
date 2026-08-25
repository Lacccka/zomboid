--***********************************************************
--**              	  ROBERT JOHNSON                       **
--***********************************************************

ItemsListUI = ISPanel:derive("ItemsListUI");
ItemsListUI.messages = {};

local FONT_HGT_SMALL = getTextManager():getFontHeight(UIFont.Small)

--************************************************************************--
--** ItemsListUI:initialise
--**
--************************************************************************--

function ItemsListUI:initialise()
    ISPanel.initialise(self);
    local btnWid = 100
    local btnHgt = math.max(25, FONT_HGT_SMALL + 3 * 2)
    local padBottom = 10

    local top = 50
    self.panel = ISTabPanel:new(10, top, self.width - 10 * 2, self.height - padBottom - btnHgt - padBottom - top);
    self.panel:initialise();
    self.panel.borderColor = { r = 0, g = 0, b = 0, a = 0};
    self.panel.target = self;
    self.panel.equalTabWidth = false
    self:addChild(self.panel);

    self.ok = ISButton:new(10, self:getHeight() - padBottom - btnHgt, btnWid, btnHgt, getText("IGUI_CraftUI_Close"), self, ItemsListUI.onClick);
    self.ok.internal = "CLOSE";
    self.ok.anchorTop = false
    self.ok.anchorBottom = true
    self.ok:initialise();
    self.ok:instantiate();
    self.ok.borderColor = {r=1, g=1, b=1, a=0.1};
    self:addChild(self.ok);
    
    self:initList();
end

function ItemsListUI:initList()
    self.items = getAllItems();

    -- we gonna separate items by module
    self.module = {};
    local moduleNames = {}
    local allItems = {}
    for i=0,self.items:size()-1 do
        local item = self.items:get(i);
        --The following code is used to generate a list of all items in the game
        --in a format that allows for easier conversion into an excel / google sheets
        --compatible layout. IN THE OUTPUT, replace <<<>>> with a tab.

        --if (item:getDisplayCategory() ~= nil) then
        --    print("<<<>>>" .. item:getName() .. "<<<>>>" .. item:getDisplayCategory())
        --else
        --    print("<<<>>>" .. item:getName() .. "<<<>>>")
        --end

        --The above code activates as soon as the item list viewer is activated.
        if not item:getObsolete() and not item:isHidden() then
            if not self.module[item:getModuleName()] then
                self.module[item:getModuleName()] = {}
                table.insert(moduleNames, item:getModuleName())
            end
            table.insert(self.module[item:getModuleName()], item);
            table.insert(allItems, item)
        end
    end

    table.sort(moduleNames, function(a,b) return not string.sort(a, b) end)

    local listBox = ItemsListTable:new(0, 0, self.panel.width, self.panel.height - self.panel.tabHeight, self);
    listBox:initialise();
    self.panel:addView("All", listBox);
--    listBox.parent = self;
    listBox:initList(allItems)

    for _,moduleName in ipairs(moduleNames) do
        -- we ignore the "Moveables" module
        if moduleName ~= "Moveables" then
            local cat1 = ItemsListTable:new(0, 0, self.panel.width, self.panel.height - self.panel.tabHeight, self);
            cat1:initialise();
            self.panel:addView(moduleName, cat1);
--            cat1.parent = self;
            cat1:initList(self.module[moduleName])
        end
    end
    self.panel:activateView("All");
end

function ItemsListUI:prerender()
    local z = 20;
    local splitPoint = 100;
    local x = 10;
    self:drawRect(0, 0, self.width, self.height, self.backgroundColor.a, self.backgroundColor.r, self.backgroundColor.g, self.backgroundColor.b);
    self:drawRectBorder(0, 0, self.width, self.height, self.borderColor.a, self.borderColor.r, self.borderColor.g, self.borderColor.b);
    self:drawText(getText("IGUI_AdminPanel_ItemList"), self.width/2 - (getTextManager():MeasureStringX(UIFont.Medium, getText("IGUI_AdminPanel_ItemList")) / 2), z, 1,1,1,1, UIFont.Medium);
end

function ItemsListUI:onClick(button)
    if button.internal == "CLOSE" then
        self:close();
    end
end

function ItemsListUI:onSelectPlayer()
end

function ItemsListUI:setKeyboardFocus()
    local view = self.panel:getActiveView()
    if not view then return end
    Core.UnfocusActiveTextEntryBox()
    view.filterWidgetMap.Type:focus()
end

function ItemsListUI:close()
    self:setVisible(false);
    self:removeFromUIManager();
end

function ItemsListUI.OnOpenPanel()
    if ItemsListUI.instance then
        ItemsListUI.instance:setVisible(true)
        ItemsListUI.instance:addToUIManager()
        ItemsListUI.instance:setKeyboardFocus()
        return
    end
    local modal = ItemsListUI:new(50, 200, 850, 650)
    modal:initialise();
    modal:addToUIManager();
    modal.instance:setKeyboardFocus()
end

--************************************************************************--
--** ItemsListUI:new
--**
--************************************************************************--
function ItemsListUI:new(x, y, width, height)
    local o = {}
    x = getCore():getScreenWidth() / 2 - (width / 2);
    y = getCore():getScreenHeight() / 2 - (height / 2);
    o = ISPanel:new(x, y, width, height);
    setmetatable(o, self)
    self.__index = self
    o.borderColor = {r=0.4, g=0.4, b=0.4, a=1};
    o.backgroundColor = {r=0, g=0, b=0, a=0.8};
    o.width = width;
    o.height = height;
    o.moveWithMouse = true;
    ItemsListUI.instance = o;
    ISDebugMenu.RegisterClass(self);
    return o;
end
