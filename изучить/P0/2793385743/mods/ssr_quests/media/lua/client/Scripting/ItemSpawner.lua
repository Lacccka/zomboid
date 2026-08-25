-- Copyright (c) 2022-2025 Oneline/D.Borovsky
-- All rights reserved
require "ISUI/PlayerData/ISPlayerData"
require "ISUI/ISInventoryPaneContextMenu"
require "ISUI/ISInventoryPane"
require "luautils"

require "Scripting/SaveManager"

local function clone(obj)
	local new_data = {};
	for i=1, #obj do
		local entry = {}
		entry.type = obj[i].type;
		entry.single = obj[i].single;
		entry.name = obj[i].name;
		table.insert(new_data, entry);
	end
	return new_data;
end

QItemSpawner = {}
QItemSpawner.containers = {}

local counter = 0;
local function createRealItem(item, container, list)
    if item:getModData().virtual then
        local name = tostring(item:getModule())..'.'..tostring(item:getType());
        item:getModData().virtual = false;
        container:Remove(item);
        local exists = false;
        for i=#list,1,-1  do
            if list[i].name == name then
                list[i].amount = list[i].amount + 1;
                exists = true;
            end
        end
        if not exists then
            local entry = QItemFactory.createEntry(name, 1);
            table.insert(list, entry);
        end
        counter = counter + 1;

        local m = getPlayer():getModData();
        local qid = item:getModData().qid;
        local single = item:getModData().single;
        for i=#QItemSpawner.containers[qid].items, 1, -1 do
            if single or item == QItemSpawner.containers[qid].items[i] then
                m.ispwn[qid][#m.ispwn[qid]+1] = item:getModData().idx;
                QItemSpawner.containers[qid].task.extdata = m.ispwn[qid];
                QItemSpawner.containers[qid].items[i]:getModData().virtual = false;
                QuestLogger.print("[QSystem*] ItemSpawner: Removed single virtual item - "..tostring(QItemSpawner.containers[qid].items[i]:getType()));
                table.remove(QItemSpawner.containers[qid].items, i);
                SaveManager.onQuestDataChange();
                if not single then break end
            end
        end
        SaveManager.save();

        if item:getModData().single then
            return true;
        end
    end
end

local function createVirtualItem(item_type, single, item_name, idx, qid)
    local item = InventoryItemFactory.CreateItem(item_type)
    if not item then
        return;
    end
    --[[if item_type then
        item:setType(item_type)
    end--]]
    if item_name then
        item:setName(item_name);
    end
    --[[if item_tex then
        item:getScriptItem():setIcon(item_tex)
        local status, tex = pcall(getTexture, "media/textures/Item_"..tostring(item_tex)..".png")
        if status and tex then
            item:setTexture(tex)
        end
    end--]]
    item:getModData().idx = idx;
    item:getModData().virtual = true;
    item:getModData().single = single or false;
    item:getModData().qid = qid;
    return item;
end

local function onGrabVirtualItems(items, player, mode)
    local loot = getPlayerLoot(player);
    local inventory = loot.inventoryPane.inventory:getParent();
    local list = {};
    if inventory then
        counter = 0;
        local container = inventory:getContainer();
        for i=1, #items do
            if items[i].items then
                local amount = 1;
                if mode == 3 then
                    amount = #items[i].items;
                elseif mode == 2 then
                    amount = math.floor((#items[i].items - 1) / 2);
                    if amount < 1 then
                        amount = 1;
                    end
                end
                for j=#items[i].items, 1, -1 do
                    if createRealItem(items[i].items[j], container, list) then
                        break;
                    elseif counter >= amount then
                        break;
                    end
                end
            else
                if createRealItem(items[i], container, list) then
                    break;
                end
            end
            loot:refreshBackpacks();
        end
    end
    if #list > 0 then
        if isClient() then
            for i=1, #list do
                local _item = getScriptManager():FindItem(list[i].name);
                if _item then
                    QuestLogger.report(getText("UI_QSystem_Logger_ItemGet", tostring(_item:getDisplayName()), list[i].amount))
                end
            end
        end
        QItemFactory.request("LootContainer", list);
    end
end

function QItemSpawner.init(task, data, loot)
    local items = {};
    local container = { type = task.container, x = task.x, y = task.y, z = task.z, items = {}, task = task }
    for i=1, #data do
        local item = createVirtualItem(data[i].type, data[i].single, data[i].name, data[i].idx, task.key);-- create virtual item
        container.items[#container.items+1] = item; -- add it to virtual container
        table.insert(items, item);
    end
    QItemSpawner.containers[task.key] = container;
    loot:refreshBackpacks();
    return items;
end

local function refresh()
    local loot = getPlayerLoot(getPlayer():getPlayerNum());
    if loot then loot:refreshBackpacks() end
end

function QItemSpawner.remove(qid)
    local m = getPlayer():getModData();
    if type(m.ispwn) == "table" then
        m.ispwn[qid] = nil;
    end
    QItemSpawner.containers[qid] = nil;
    refresh();
end

function QItemSpawner.reset()
    QItemSpawner.containers = {}
    getPlayer():getModData().ispwn = {}
    refresh();
end

Events.OnQSystemReset.Add(QItemSpawner.reset);

function QItemSpawner.restore()
    local m = getPlayer():getModData();
    if not m.ispwn then m.ispwn = {}; end
    for key, container in pairs(QItemSpawner.containers) do
        if container.task.unlocked and not container.task.completed and not container.task.pending then
            m.ispwn[key] = container.task.extdata;
        else
            m.ispwn[key] = nil;
        end
    end
    refresh();
end

Events.OnCreatePlayer.Add(QItemSpawner.restore);
Events.OnQSystemUpdate.Add(function (code) if code == 4 then QItemSpawner.restore() end end)


-- safely override original functions
local ISInventoryPane_refreshContainer = ISInventoryPane.refreshContainer;
function ISInventoryPane:refreshContainer()
    ISInventoryPane_refreshContainer(self);
    local playerObj = getSpecificPlayer(self.player)
    local parent = self.inventory:getParent();
    if parent and self.qid and parent:getContainer():getType() == QItemSpawner.containers[self.qid[1]].type then
        self.itemindex = {}

        if self.collapsed == nil then
            self.collapsed = {}
        end
        if self.selected == nil then
            self.selected = {}
        end

        local selected = self:saveSelection({})
        table.wipe(self.selected)

        if not self.hotbar then
            self.hotbar = getPlayerHotbar(self.player);
        end

        local isEquipped = {}
        local isInHotbar = {}
        if self.parent.onCharacter then
            local wornItems = playerObj:getWornItems()
            for i=1,wornItems:size() do
                local wornItem = wornItems:get(i-1)
                isEquipped[wornItem:getItem()] = true
            end
            local item = playerObj:getPrimaryHandItem()
            if item then
                isEquipped[item] = true
            end
            item = playerObj:getSecondaryHandItem()
            if item then
                isEquipped[item] = true
            end
            if self.hotbar and self.hotbar.attachedItems then
                for _,item in pairs(self.hotbar.attachedItems) do
                    isInHotbar[item] = true
                end
            end
        end

        for x=1, #self.qid do
            local container = QItemSpawner.containers[self.qid[x]];

            local it = container.items;
            for i = 1, #container.items do
                local item = it[i];
                local add = true;
                -- don't add the ZedDmg category, they are just equipped models
                if item:isHidden() then
                    add = false;
                end
                if add then
                    local itemName = item:getName();
                    if item:IsFood() and item:getHerbalistType() and item:getHerbalistType() ~= "" then
                        if playerObj:isRecipeKnown("Herbalist") then
                            if item:getHerbalistType() == "Berry" then
                                itemName = (item:getPoisonPower() > 0) and getText("IGUI_PoisonousBerry") or getText("IGUI_Berry")
                            end
                            if item:getHerbalistType() == "Mushroom" then
                                itemName = (item:getPoisonPower() > 0) and getText("IGUI_PoisonousMushroom") or getText("IGUI_Mushroom")
                            end
                        else
                            if item:getHerbalistType() == "Berry"  then
                                itemName = getText("IGUI_UnknownBerry")
                            end
                            if item:getHerbalistType() == "Mushroom" then
                                itemName = getText("IGUI_UnknownMushroom")
                            end
                        end
                        if itemName ~= item:getDisplayName() then
                            item:setName(itemName);
                        end
                        itemName = item:getName()
                    end
                    local equipped = false
                    local inHotbar = false
                    if self.parent.onCharacter then
                        if isEquipped[item] then
                            itemName = "equipped:"..itemName
                            equipped = true
                        elseif item:getType() == "KeyRing" and playerObj:getInventory():contains(item) then
                            itemName = "keyring:"..itemName
                            equipped = true
                        end
                        if self.hotbar then
                            inHotbar = isInHotbar[item];
                            if inHotbar and not equipped then
                                itemName = "hotbar:"..itemName
                            end
                        end
                    end
                    if self.itemindex[itemName] == nil then
                        self.itemindex[itemName] = {};
                        self.itemindex[itemName].items = {}
                        self.itemindex[itemName].count = 0
                        self.itemindex[itemName].qid = self.qid[x];
                    end
                    local ind = self.itemindex[itemName];
                    ind.equipped = equipped
                    ind.inHotbar = inHotbar;

                    ind.count = ind.count + 1
                    ind.items[ind.count] = item;
                end
            end

            for k, v in pairs(self.itemindex) do
                if v ~= nil and v.qid == self.qid[x] then
                    table.insert(self.itemslist, v);
                    local count = 1;
                    local weight = 0;
                    for k2, v2 in ipairs(v.items) do
                        if v2 == nil then
                            table.remove(v.items, k2);
                        else
                            count = count + 1;
                            weight = weight + v2:getUnequippedWeight();
                        end
                    end
                    v.count = count;
                    v.invPanel = self;
                    v.name = k -- v.items[1]:getName();
                    v.cat = v.items[1]:getDisplayCategory() or v.items[1]:getCategory();
                    v.weight = weight;
                    if self.collapsed[v.name] == nil then
                        self.collapsed[v.name] = true;
                    end
                end
            end

            --print("Preparing to sort inv items");
            table.sort(self.itemslist, self.itemSortFunc );

            -- Adding the first item in list additionally at front as a dummy at the start, to be used in the details view as a header.
            for k, v in ipairs(self.itemslist) do
                if v.qid == self.qid[x] then
                    local item = v.items[1];
                    table.insert(v.items, 1, item);
                end
            end
        end

        self:restoreSelection(selected);
        table.wipe(selected);

        self:updateScrollbars();
        self.inventory:setDrawDirty(false);

        -- Update the buttons
        if self:isMouseOver() then
            self:onMouseMove(0, 0)
        end
    end
end

local ISInventoryPage_refreshBackpacks = ISInventoryPage.refreshBackpacks;
function ISInventoryPage:refreshBackpacks() -- FIXME: не всегда обновляется список, когда подходишь к контейнеру
    local search = true;
    self.inventoryPane.qid = nil;
    for _, containerButton in ipairs(self.backpacks) do
        if search and containerButton.inventory:getParent() ~= getSpecificPlayer(self.player) then
            local parent = containerButton.inventory:getParent();
            local container = parent and parent:getContainer();
            if container then
                for key, value in pairs(QItemSpawner.containers) do
                    if container:getType() == value.type then
                        local square = container:getParent():getSquare()
                        if square:getX() == value.x and square:getY() == value.y and square:getZ() == value.z then
                            if containerButton.inventory == self.inventoryPane.inventory then
                                if not self.inventoryPane.qid then self.inventoryPane.qid = {} end
                                self.inventoryPane.qid[#self.inventoryPane.qid+1] = key;
                            end
                            search = false;
                        end
                    end
                end
            end
        end
    end
    ISInventoryPage_refreshBackpacks(self);
end

local onGrabItems = ISInventoryPaneContextMenu.onGrabItems;
ISInventoryPaneContextMenu.onGrabItems = function(items, player)
    onGrabVirtualItems(items, player, 3);
    onGrabItems(items, player);
end

local onGrabHalfItems = ISInventoryPaneContextMenu.onGrabHalfItems;
ISInventoryPaneContextMenu.onGrabHalfItems = function(items, player)
    onGrabVirtualItems(items, player, 2);
    onGrabHalfItems(items, player)
end

local onGrabOneItems = ISInventoryPaneContextMenu.onGrabOneItems;
ISInventoryPaneContextMenu.onGrabOneItems = function(items, player)
    onGrabVirtualItems(items, player, 1);
    onGrabOneItems(items, player)
end

local luautils_walkToContainer = luautils.walkToContainer;
function luautils.walkToContainer(container, playerNum)
    if container then
        return luautils_walkToContainer(container, playerNum);
    else
        return false;
    end
end