--***********************************************************
--**                    ROBERT JOHNSON                     **
--***********************************************************

require "ISUI/ISPanel"

ItemsListTable = ISPanel:derive("ItemsListTable");

local FONT_HGT_SMALL = getTextManager():getFontHeight(UIFont.Small)
local FONT_HGT_MEDIUM = getTextManager():getFontHeight(UIFont.Medium)
local FONT_HGT_LARGE = getTextManager():getFontHeight(UIFont.Large)

local HEADER_HGT = FONT_HGT_MEDIUM + 2 * 2

function ItemsListTable:initialise()
    ISPanel.initialise(self);
end

function ItemsListTable:render()
    ISPanel.render(self);
    
    local y = self.datas.y + self.datas.height + 5
    self:drawText(getText("IGUI_DbViewer_TotalResult") .. self.totalResult, 0, y, 1,1,1,1,UIFont.Small)
    self:drawText(getText("UI_Listtip"), 0, y + FONT_HGT_SMALL, 1,1,1,1,UIFont.Small)
    self:drawText(self.editstr, 0, self.editSELL:getBottom()+3, 1,1,1,1,UIFont.Small)

    y = self.filters:getBottom()
    
    self:drawRectBorder(self.datas.x, y, self.datas:getWidth(), HEADER_HGT, 1, self.borderColor.r, self.borderColor.g, self.borderColor.b);
    self:drawRect(self.datas.x, y+1, self.datas:getWidth(), HEADER_HGT, self.listHeaderColor.a, self.listHeaderColor.r, self.listHeaderColor.g, self.listHeaderColor.b);

    local x = 0;
    for i,v in ipairs(self.datas.columns) do
        local size;
        if i == #self.datas.columns then
            size = self.datas.width - x
        else
            size = self.datas.columns[i+1].size - self.datas.columns[i].size
        end
--        print(v.name, x, v.size)
        self:drawText(v.name, x+10+3, y+2, 1,1,1,1,UIFont.Small);
        self:drawRectBorder(self.datas.x + x, y, 1, self.datas.itemheight + 1, 1, self.borderColor.r, self.borderColor.g, self.borderColor.b);
        x = x + size;
    end
end

function ItemsListTable:new (x, y, width, height, viewer)
    local o = ISPanel:new(x, y, width, height);
    setmetatable(o, self);
    o.listHeaderColor = {r=0.4, g=0.4, b=0.4, a=0.3};
    o.borderColor = {r=0.4, g=0.4, b=0.4, a=0};
    o.backgroundColor = {r=0, g=0, b=0, a=1};
    o.buttonBorderColor = {r=0.7, g=0.7, b=0.7, a=0.5};
    o.totalResult = 0;
    o.filterWidgets = {};
    o.filterWidgetMap = {}
    o.viewer = viewer
    o.selectTable = {};
    ItemsListTable.instance = o;
    return o;
end

function ItemsListTable:createChildren()
    ISPanel.createChildren(self);
    
    local btnWid = 100
    local btnHgt = math.max(25, FONT_HGT_SMALL + 3 * 2)
    local entryHgt = FONT_HGT_MEDIUM + 2 * 2
    local bottomHgt = 5 + FONT_HGT_SMALL * 2 + 5 + btnHgt + 20 + FONT_HGT_LARGE + HEADER_HGT + entryHgt

    self.datas = ISScrollingListBox:new(0, HEADER_HGT, self.width, self.height - bottomHgt - HEADER_HGT);
    self.datas:initialise();
    self.datas:instantiate();
    self.datas:setOnMouseDownFunction(self, ItemsListTable.onMouseDown2)
    self.datas.itemheight = FONT_HGT_SMALL + 4 * 2
    self.datas.selected = 0;
    self.datas.joypadParent = self;
    self.datas.font = UIFont.NewSmall;
    self.datas.doDrawItem = self.drawDatas;
    self.datas.drawBorder = true;
--    self.datas.parent = self;
    self.datas:addColumn("Type", 0);
    self.datas:addColumn("Name", 200);
    self.datas:addColumn("Category", 450);
    self.datas:addColumn("DisplayCategory", 650)
    --self.datas:setOnMouseDoubleClick(self, ItemsListTable.addItem);
    self:addChild(self.datas);

    self.toolTip = ISToolTip:new();
    self.toolTip:initialise();
    self.toolTip:setOwner(self);
    self.toolTip:setX(850);
    self.toolTip:setY(200);
    self.toolTip.followMouse = false;
    self.toolTip:doLayout();
    self.toolTip:setVisible(false);
    self:addChild(self.toolTip);

    local btnY = self.datas.y + self.datas.height + 5 + FONT_HGT_SMALL * 2 + 5

    self.editSELL = ISTextEntryBox:new("", 0, btnY, btnWid, btnHgt)
    self.editSELL.font = UIFont.Medium
    self.editSELL:initialise()
    self.editSELL:instantiate()
    self.editSELL:setOnlyNumbers(true);
    self.editSELL.target = self.editSELL
    self:addChild(self.editSELL)
        
        
    self.buttonAdd5 = ISButton:new(self.editSELL:getRight() + 20, btnY, btnWid, btnHgt, "EDIT", self, ItemsListTable.onOptionMouseDown);
    self.buttonAdd5.internal = "EDIT";
    self.buttonAdd5.enable = false;
    self.buttonAdd5.borderColor = self.buttonBorderColor;
    self:addChild(self.buttonAdd5);

    self.buttonAdd6 = ISButton:new(self.buttonAdd5:getRight() + 20, btnY, btnWid, btnHgt, "INCLUDE", self, ItemsListTable.onOptionMouseDown);
    self.buttonAdd6.internal = "INCLUDE";
    self.buttonAdd6.enable = false;
    self.buttonAdd6.borderColor = self.buttonBorderColor;
    self:addChild(self.buttonAdd6);

    self.buttonAdd7 = ISButton:new(self.buttonAdd6:getRight() + 20, btnY, btnWid, btnHgt, "EXCLUDE", self, ItemsListTable.onOptionMouseDown);
    self.buttonAdd7.internal = "EXCLUDE";
    self.buttonAdd7.enable = false;
    self.buttonAdd7.borderColor = self.buttonBorderColor;
    self:addChild(self.buttonAdd7);

    self.buttonAdd8 = ISButton:new(self.buttonAdd7:getRight() + 20, btnY, btnWid, btnHgt, "ALL EXCLUDE : TRUE", self, ItemsListTable.onOptionMouseDown);
    self.buttonAdd8.internal = "ALL EXCLUDE : TRUE";
    self.buttonAdd8.enable = false;
    self.buttonAdd8.borderColor = self.buttonBorderColor;
    self:addChild(self.buttonAdd8);

    self.buttonAdd9 = ISButton:new(self.buttonAdd8:getRight() + 20, btnY, btnWid, btnHgt, "ALL EXCLUDE : FALSE", self, ItemsListTable.onOptionMouseDown);
    self.buttonAdd9.internal = "ALL EXCLUDE : FALSE";
    self.buttonAdd9.enable = false;
    self.buttonAdd9.borderColor = self.buttonBorderColor;
    self:addChild(self.buttonAdd9);


    self.filters = ISLabel:new(0, self.buttonAdd5:getBottom() + 20, FONT_HGT_LARGE, getText("IGUI_DbViewer_Filters"), 1, 1, 1, 1, UIFont.Large, true)
    self.filters:initialise()
    self.filters:instantiate()
    self:addChild(self.filters)
    
    local x = 0;
    local entryY = self.filters:getBottom() + self.datas.itemheight
    for i,column in ipairs(self.datas.columns) do
        local size;
        if i == #self.datas.columns then -- last column take all the remaining width
            size = self.datas:getWidth() - x;
        else
            size = self.datas.columns[i+1].size - self.datas.columns[i].size
        end
        if column.name == "Category" then
            local combo = ISComboBox:new(x, entryY, size, entryHgt)
            combo.font = UIFont.Medium
            combo:initialise()
            combo:instantiate()
            combo.columnName = column.name
            combo.target = combo
            combo.onChange = self.onFilterChange
            combo.itemsListFilter = self.filterCategory
            self:addChild(combo)
            table.insert(self.filterWidgets, combo)
            self.filterWidgetMap[column.name] = combo
        elseif column.name == "DisplayCategory" then
            local combo = ISComboBox:new(x, entryY, size, entryHgt)
            combo.font = UIFont.Medium
            combo:initialise()
            combo:instantiate()
            combo.columnName = column.name
            combo.target = combo
            combo.onChange = self.onFilterChange
            combo.itemsListFilter = self.filterDisplayCategory
            self:addChild(combo)
            table.insert(self.filterWidgets, combo)
            self.filterWidgetMap[column.name] = combo
        else
            local entry = ISTextEntryBox:new("", x, entryY, size, entryHgt);
            entry.font = UIFont.Medium
            entry:initialise();
            entry:instantiate();
            entry.columnName = column.name;
            entry.itemsListFilter = self['filter'..column.name]
            entry.onTextChange = ItemsListTable.onFilterChange;
            entry.onOtherKey = function(entry, key) ItemsListTable.onOtherKey(entry, key) end
            entry.target = self;
            entry:setClearButton(true)
            self:addChild(entry);
            table.insert(self.filterWidgets, entry);
            self.filterWidgetMap[column.name] = entry
        end
        x = x + size;
    end
end

function ItemsListTable:onMouseDown2(x, y)

	getSoundManager():playUISound("UISelectListItem")

    local selectitem = self.datas.items[self.datas.selected].item;

	if selectitem ~= nil then
        local findName = selectitem:getModuleName() .. "." .. selectitem:getName()
        local showdetail = "INCLUDE : FALSE\n" .. "EXCLUDE : FALSE\n" .. "PRICE : RANDOM"
        if self.selectTable[findName] ~= nil then
            local str = string.split(self.selectTable[findName] , "/")
            showdetail = "INCLUDE : " .. tostring(str[2]) .. "\nEXCLUDE : " .. tostring(str[3]) .. "\nPRICE : " .. tostring(str[4])
        end
        if self.toolTip ~= nil then 
            self.toolTip:setVisible(false); 
            self.toolTip:removeFromUIManager();
        end
        self.toolTip:setVisible(true); 
        if #selectitem:getDisplayName() < 26 then
            self.toolTip:setName(selectitem:getDisplayName())
            self.toolTip.description = showdetail;
        else 
            self.toolTip:setName("");
            self.toolTip.description = selectitem:getDisplayName() .. "\n" .. showdetail;
        end
        --detailview:addChild(toolTip);
		--self.onmousedblclick(self.target, getItem[self.selected]);
	end
  --  if self.onmousedown then
	--	self.onmousedown(self.target, self.items[self.selected].item);
  --	end
end

function ItemsListTable:addItem(item)
    --local playerNum = self.viewer.playerSelect.selected - 1
    local playerObj = getSpecificPlayer(getPlayer():getPlayerNum())
    if not playerObj or playerObj:isDead() then return end
    playerObj:getInventory():AddItem(item:getFullName())
end

function ItemsListTable:onChangeText(s1, s2, s3, item)
    if #item:getDisplayName() < 26 then
        self.toolTip:setName(item:getDisplayName())
        self.toolTip.description = "INCLUDE : " .. s1 .. "\nEXCLUDE : " .. s2 .. "\nPRICE : " .. s3;
    else self.toolTip.description = item:getDisplayName() .. "\n" .. "INCLUDE : " .. s1 .. "\nEXCLUDE : " .. s2 .. "\nPRICE : " .. s3;
    end
end

function ItemsListTable:onOptionMouseDown(button, x, y)
    local readfile = getFileReader("items/[" .. getWorld():getWorld() .. "]items.ini", false)
    local include = "FALSE"
    local exclude = "FALSE"
    local price = "RANDOM"

    if readfile ~= nil then
      while true do
        local line = readfile:readLine()
        if line == nil then
          readfile:close()
          break
        end
        if string.contains(line, "/") then
         local readitems = string.split(line,"/");
         self.selectTable[readitems[1]] = line;
        end
      end
    end

    local item = button.parent.datas.items[button.parent.datas.selected].item;
    local findName = item:getModuleName() .. "." .. item:getName();
    local writer = getFileWriter("items/[" .. getWorld():getWorld() .. "]items.ini", true, false);
    local exc = "FALSE";
    local ixc = "FALSE";


    if self.selectTable[findName] ~= nil then
        local readitems = string.split(self.selectTable[findName],"/"); 
        include = readitems[2]
        exclude = readitems[3]
        price = readitems[4]
        self.selectTable[findName] = nil;
    end
   -- for i,v in pairs(self.selectTable) do
   --     writer:write(v .. "\r\n")
   -- end
    if button.internal == "EDIT" then
        local getEdit = self.editSELL:getText();
        if getEdit == "" then
            self.selectTable[findName] = item:getModuleName() .. "." .. item:getName() .. "/" .. include .. "/" .. exclude .. "/RANDOM"
            self.editstr = item:getDisplayName() .. " : PRICE : RANDOM"
            self:onChangeText(include, exclude, "RANDOM", item);
        end
        if getEdit ~= "" and tonumber(getEdit) >= 0 then
            --writer:write("SELL ITEMS LIST\r\n")
            self.selectTable[findName] = item:getModuleName() .. "." .. item:getName() .. "/" .. include .. "/" .. exclude .. "/" .. getEdit;
            self.editstr = item:getDisplayName() .. " : PRICE : " .. getEdit
            self:onChangeText(include, exclude, getEdit, item);
        end
        if getEdit ~= "" and tonumber(getEdit) < 0 then
            self.editstr = "Please enter the correct value."
        end
        self.editSELL:setText("");
        --self:addItem(item)
    end

    if button.internal == "INCLUDE" then
            local getValue = getSandboxOptions():getOptionByName("TradeHunter.tradeitems"):getValue();
            if include == "TRUE" then
                local str = ""
                local readstr = item:getModuleName() .. "." .. item:getName()
                local substr = string.split(getValue, readstr .. ",")
                if substr[2] ~= nil then
                    str = substr[1] .. substr[2];
                else str = substr[1];
                end
                ixc = "FALSE"
                if exclude ~= "FALSE" or price ~= "RANDOM" then
                self.selectTable[findName] = item:getModuleName() .. "." .. item:getName() .. "/" .. ixc .. "/" .. exclude .. "/" .. price;
                else 
                    getSandboxOptions():getOptionByName("TradeHunter.tradeitems"):setValue(str);
                    getSandboxOptions():sendToServer();
                end
            else 
                ixc = "TRUE"
                self.selectTable[findName] = item:getModuleName() .. "." .. item:getName() .. "/" .. ixc .. "/" .. exclude .. "/" .. price;
                getSandboxOptions():getOptionByName("TradeHunter.tradeitems"):setValue(getValue .. item:getModuleName() .. "." .. item:getName() .. ",");
                getSandboxOptions():sendToServer();
            end
            self.editstr = item:getDisplayName() .. " : INCLUDE : " .. ixc
            self:onChangeText(ixc, exclude, price, item);  
    end

    if button.internal == "EXCLUDE" then
          local getValue = getSandboxOptions():getOptionByName("TradeHunter.tradeExcludeitems"):getValue();
          if exclude == "TRUE" then
            local str = ""
            local readstr = item:getModuleName() .. "." .. item:getName()
            local substr = string.split(getValue, readstr .. ",")
            if substr[2] ~= nil then
                str = substr[1] .. substr[2];
            else str = substr[1];
            end
            exc = "FALSE"
            if include ~= "FALSE" or price ~= "RANDOM" then
            self.selectTable[findName] = item:getModuleName() .. "." .. item:getName() .. "/" .. include .. "/" .. exc .. "/" .. price;
            else 
                getSandboxOptions():getOptionByName("TradeHunter.tradeExcludeitems"):setValue(str);
                getSandboxOptions():sendToServer();
            end
        else 
            exc = "TRUE"
            self.selectTable[findName] = item:getModuleName() .. "." .. item:getName() .. "/" .. include .. "/" .. exc .. "/" .. price;
            getSandboxOptions():getOptionByName("TradeHunter.tradeExcludeitems"):setValue(getValue .. item:getModuleName() .. "." .. item:getName() .. ",");
            getSandboxOptions():sendToServer();
        end
        self.editstr = item:getDisplayName() .. " : INCLUDE : " .. exc
        self:onChangeText(include, exc, price, item); 
    end

    if button.internal == "ALL EXCLUDE : TRUE" then
      if #self.datas.items < 1500 then
        for i=1,#self.datas.items do
            local item = self.datas.items[i].item;
            local find = item:getModuleName() .. "." .. item:getName();
            local getValue = getSandboxOptions():getOptionByName("TradeHunter.tradeExcludeitems"):getValue();
            getSandboxOptions():getOptionByName("TradeHunter.tradeExcludeitems"):setValue(getValue .. item:getModuleName() .. "." .. item:getName() .. ",");
            getSandboxOptions():sendToServer();
            --writer:writeln(item:getModuleName() .. "." .. item:getName() .. "/" .. include .. "/TRUE/" .. price .. "\r\n");
            self.selectTable[find] = item:getModuleName() .. "." .. item:getName() .. "/" .. include .. "/TRUE/" .. price;
        end
        self.editstr =  "Remove all visible lists."
      else self.editstr =  "Too many items you want to remove."
      end
    end

    if button.internal == "ALL EXCLUDE : FALSE" then
        local getValue = getSandboxOptions():getOptionByName("TradeHunter.tradeExcludeitems"):getValue();
        local str = ""
        if #self.datas.items < 1500 then
          for i=1,#self.datas.items do
              local item = self.datas.items[i].item;
              local find = item:getModuleName() .. "." .. item:getName();
              str = str .. find .. ",";
              self.selectTable[find] = nil;
          end
          local substr = string.split(getValue, str)
          if substr[2] ~= nil then
            str = substr[1] .. substr[2];
          else str = substr[1]
          end
          getSandboxOptions():getOptionByName("TradeHunter.tradeExcludeitems"):setValue(str);
          getSandboxOptions():sendToServer();
          for i,v in pairs(self.selectTable) do
              writer:write(v .. "\r\n")
          end
          writer:close();
          self.editstr =  "Recover all visible lists."
        else self.editstr =  "Too many items you want to recover."
        end
      end
      if self.selectTable[findName] ~= nil then
        local readitems = string.split(self.selectTable[findName],"/"); 
        include = readitems[2]
        exclude = readitems[3]
        price = readitems[4]
        if include == "FALSE" and exclude == "FALSE" and price == "RANDOM" then
         self.selectTable[findName] = nil;
        end
      end
      for i,v in pairs(self.selectTable) do
        writer:write(v .. "\r\n")
      end
      writer:close();
end

function ItemsListTable:onAddItem(button, item)
    if button.internal == "OK" then
        for i=0,tonumber(button.parent.entry:getText()) - 1 do
            self:addItem(item);
        end
    end
end

function ItemsListTable:initList(module)
    self.totalResult = 0;
    self.editstr = "";
    local getValue1 = getSandboxOptions():getOptionByName("TradeHunter.tradeitems"):getValue();
    local getValue2 = getSandboxOptions():getOptionByName("TradeHunter.tradeExcludeitems"):getValue();
    local categoryNames = {}
    local displayCategoryNames = {}
    local categoryMap = {}
    local displayCategoryMap = {}
    for x,v in ipairs(module) do
        self.datas:addItem(v:getDisplayName(), v);
        if not categoryMap[v:getTypeString()] then
            categoryMap[v:getTypeString()] = true
            table.insert(categoryNames, v:getTypeString())
        end
        if not displayCategoryMap[v:getDisplayCategory()] then
            displayCategoryMap[v:getDisplayCategory()] = true
            table.insert(displayCategoryNames, v:getDisplayCategory())
        end
        self.totalResult = self.totalResult + 1;
    end
    table.sort(self.datas.items, function(a,b) return not string.sort(a.item:getDisplayName(), b.item:getDisplayName()); end);

    local combo = self.filterWidgetMap.Category
    table.sort(categoryNames, function(a,b) return not string.sort(a, b) end)
    table.insert(categoryNames, "EDIT ITEMS")
    combo:addOption("<Any>")
    for _,categoryName in ipairs(categoryNames) do
        combo:addOption(categoryName)
    end

    local combo = self.filterWidgetMap.DisplayCategory
    table.sort(displayCategoryNames, function(a,b) return not string.sort(a, b) end)
    combo:addOption("<Any>")
    combo:addOption("<No category set>")
    for _,displayCategoryName in ipairs(displayCategoryNames) do
        combo:addOption(displayCategoryName)
    end
    local readfile = getFileReader("items/[" .. getWorld():getWorld() .. "]items.ini", false)
    local delCheck = false;
    local readDel = ""
    if readfile ~= nil then
      while true do
        local line = readfile:readLine()
        if line == nil then
          readfile:close()
          break
        end
        if string.contains(line, "/") then
         local readitems = string.split(line,"/");
         self.selectTable[readitems[1]] = line;
         if (getValue1 == nil or getValue1 == "") and readitems[2] == "TRUE" then 
            self.selectTable[readitems[1]] = nil; 
            delCheck = true;
         end
         if (getValue2 == nil or getValue2 == "")  and readitems[3] == "TRUE" then 
            self.selectTable[readitems[1]] = nil; 
            delCheck = true;
         end
         if self.selectTable[readitems[1]] ~= nil then
         readDel = readDel ..  self.selectTable[readitems[1]] .. "\r\n";
         end
        end
      end
      if delCheck then
        local writer = getFileWriter("items/[" .. getWorld():getWorld() .. "]items.ini", true, false);
        writer:write(readDel);
        writer:close();
      end
      else
        getSandboxOptions():getOptionByName("TradeHunter.tradeitems"):setValue("");
        getSandboxOptions():getOptionByName("TradeHunter.tradeExcludeitems"):setValue("");
        getSandboxOptions():sendToServer();
    end
end

function ItemsListTable:update()
    --local playerNum = self.viewer.playerSelect.selected - 1
    local playerObj = getSpecificPlayer(getPlayer():getPlayerNum())
    local hasPlayer = playerObj ~= nil


    self.buttonAdd5.enable = self.datas.selected > 0 and hasPlayer;
    self.buttonAdd6.enable = self.datas.selected > 0 and hasPlayer;
    self.buttonAdd7.enable = self.datas.selected > 0 and hasPlayer;
    self.buttonAdd8.enable = self.datas.selected > 0 and hasPlayer;
    self.buttonAdd9.enable = self.datas.selected > 0 and hasPlayer;
    self.datas.doDrawItem = self.drawDatas;
end

function ItemsListTable:filterDisplayCategory(widget, scriptItem)
    if widget.selected == 1 then return true end -- Any category
    if widget.selected == 2 then return scriptItem:getDisplayCategory() == nil end
    return scriptItem:getDisplayCategory() == widget:getOptionText(widget.selected)
end

function ItemsListTable:filterCategory(widget, scriptItem)
    if widget.selected == 1 then return true end -- Any category
    return scriptItem:getTypeString() == widget:getOptionText(widget.selected)
end

function ItemsListTable:filterName(widget, scriptItem)
    local txtToCheck = string.lower(scriptItem:getDisplayName())
    local filterTxt = string.lower(widget:getInternalText())
    return checkStringPattern(filterTxt) and string.match(txtToCheck, filterTxt)
end

function ItemsListTable:filterType(widget, scriptItem)
    local txtToCheck = string.lower(scriptItem:getName())
    local filterTxt = string.lower(widget:getInternalText())
    return checkStringPattern(filterTxt) and string.match(txtToCheck, filterTxt)
end

function ItemsListTable.onFilterChange(widget)
    local datas = widget.parent.datas;
    if not datas.fullList then datas.fullList = datas.items; end
    widget.parent.totalResult = 0;
    datas:clear();
    --getPlayer():Say("find : " .. tostring(widget:getOptionText(widget.selected)))
--print(entry.parent, combo)
--    local filterTxt = entry:getInternalText();
--    if filterTxt == "" then datas.items = datas.fullList; return; end
    if widget:getOptionText(widget.selected) ~= "EDIT ITEMS" then
       for i,v in ipairs(datas.fullList) do -- check every items
          local add = true;
          for j,widget in ipairs(widget.parent.filterWidgets) do -- check every filters
              if not widget.itemsListFilter(self, widget, v.item) then
                  add = false
                  break
              end
          end
          if add then
              datas:addItem(i, v.item);
              widget.parent.totalResult = widget.parent.totalResult + 1;
          end
       end
      else
        local readfile = getFileReader("items/[" .. getWorld():getWorld() .. "]items.ini", false)
        local i = 1;
        if readfile ~= nil then
          while true do
            local line = readfile:readLine()
            if line == nil then
              readfile:close()
              break
            end
            if string.contains(line, "/") then
             local readitems = string.split(line,"/");
             local item = getScriptManager():FindItem(readitems[1]);
              if item ~= nil then
                datas:addItem(i, item);
                widget.parent.totalResult = widget.parent.totalResult + 1;
                i = i + 1;
              end
            end
          end
        end
    end
end

function ItemsListTable:onOtherKey(key)
    if key == Keyboard.KEY_TAB then
        Core.UnfocusActiveTextEntryBox()
        if self.columnName == "Type" then
            self.parent.filterWidgetMap.Name:focus()
        else
            self.parent.filterWidgetMap.Type:focus()
        end
    end
end

function ItemsListTable:drawDatas(y, item, alt)
    if y + self:getYScroll() + self.itemheight < 0 or y + self:getYScroll() >= self.height then
        return y + self.itemheight
    end
    
    local a = 0.9;

    if self.selected == item.index then
        self:drawRect(0, (y), self:getWidth(), self.itemheight, 0.3, 0.7, 0.35, 0.15);
    end

    if alt then
        self:drawRect(0, (y), self:getWidth(), self.itemheight, 0.3, 0.6, 0.5, 0.5);
    end

    self:drawRectBorder(0, (y), self:getWidth(), self.itemheight, a, self.borderColor.r, self.borderColor.g, self.borderColor.b);

    local iconX = 4
    local iconSize = FONT_HGT_SMALL;
    local xoffset = 10;

    local clipX = self.columns[1].size
    local clipX2 = self.columns[2].size
    local clipY = math.max(0, y + self:getYScroll())
    local clipY2 = math.min(self.height, y + self:getYScroll() + self.itemheight)
    
    self:setStencilRect(clipX, clipY, clipX2 - clipX, clipY2 - clipY)
    self:drawText(item.item:getName(), xoffset, y + 4, 1, 1, 1, a, self.font);
    self:clearStencilRect()

    clipX = self.columns[2].size
    clipX2 = self.columns[3].size
    self:setStencilRect(clipX, clipY, clipX2 - clipX, clipY2 - clipY)
    self:drawText(item.item:getDisplayName(), self.columns[2].size + iconX + iconSize + 4, y + 4, 1, 1, 1, a, self.font);
    self:clearStencilRect()

    clipX = self.columns[3].size
    clipX2 = self.columns[4].size
    self:setStencilRect(clipX, clipY, clipX2 - clipX, clipY2 - clipY)
    self:drawText(item.item:getTypeString(), self.columns[3].size + xoffset, y + 4, 1, 1, 1, a, self.font);
    self:clearStencilRect()

    if item.item:getDisplayCategory() ~= nil then
        self:drawText(getText("IGUI_ItemCat_" .. item.item:getDisplayCategory()), self.columns[4].size + xoffset, y + 4, 1, 1, 1, a, self.font);
        else
        self:drawText("Error: No category set", self.columns[4].size + xoffset, y + 4, 1, 1, 1, a, self.font);
    end


    self:repaintStencilRect(0, clipY, self.width, clipY2 - clipY)

    local icon = item.item:getIcon()
    if item.item:getIconsForTexture() and not item.item:getIconsForTexture():isEmpty() then
        icon = item.item:getIconsForTexture():get(0)
    end
    if icon then
        local texture = getTexture("Item_" .. icon)
        if texture  == nil then
        texture = getTexture("media/textures/Item_" .. icon .. ".png")
	       if texture == nil then
	         local findItem = getScriptManager():FindItem("Base.Spiffo");
	         icon = findItem:getIcon();
	         texture = getTexture("Item_" .. icon);
	       end
        end   
            self:drawTextureScaledAspect2(texture, self.columns[2].size + iconX, y + (self.itemheight - iconSize) / 2, iconSize, iconSize,  1, 1, 1, 1);
    end

    return y + self.itemheight;
end
