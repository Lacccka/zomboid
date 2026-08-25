-- Copyright (c) 2022-2025 Oneline/D.Borovsky
-- All rights reserved

QItemFactory = {}

-- Client side

QItemFactory.createEntry = function (name, amount)
    local item = {}
    item.name = name;
    item.amount = amount;
    return item;
end

QItemFactory.request = function (sender, items, callback)
    if type(items) == "table" then
        if #items > 0 then
            if isClient() then
                SyncClient.request('QSystem', 'requestItem', items, sender, callback);
            elseif not isServer() then
                local inventory = getPlayer():getInventory();
                for i=1, #items do
                    local added = 0;
                    while added < items[i].amount do
                        inventory:AddItem(items[i].name);
                        added = added + 1;
                    end
                end
                if callback then
                    SSRTimer.add_ms(callback, 100, false);
                end
            end
        end
    end
end

local function loadTexture(id, icons)
    if id > -1 and id < icons:size() then
        return getTexture("Item_"..tostring(icons:get(id)));
    end
end

QItemFactory.getTextureFromItem = function(item)
	local texture = item:getNormalTexture();
	if not texture or texture:getName() == "Question_On" then
		local obj = item:InstanceItem(nil);
		if obj then
			local icons = item:getIconsForTexture();
			if icons and icons:size() > 0 then
				texture = loadTexture(obj:getVisual():getBaseTexture(), icons) or loadTexture(obj:getVisual():getTextureChoice(), icons);
			else
				texture = obj:getTexture();
			end
		end
	end
	return texture;
end

QItemFactory.getWorldSpriteFromItem = function(item)
    if instanceof(item, "Item") and item:getItemType() and item:getItemType():toString() == "base:moveable" then
        local obj = item:InstanceItem(nil);
        if obj then
            local sprite = obj:getWorldSprite();
            if sprite and sprite ~= "" then
                return getTexture(sprite);
            end
        end
    end
end

-- Server side

QItemFactory.grant = function (username, items)
    local done = false;
    if type(items) == "table" then
        for i=1, #items do
            items[i].amount = tonumber(items[i].amount)
            if items[i].name and items[i].amount then
                local result = jm_addItem(username, items[i].name, items[i].amount);
                print(tostring(result));
                done = true;
            end
        end
    end

    if done then
        return true;
    end
end

local function removeItemsWithIDs(player, item, ids)
    for j=1, #ids do
        if item:getID() == ids[j] then
            if item:getContainer() then
                sendRemoveItemFromContainer(item:getContainer(), item);
                item:getContainer():Remove(item);
            else
                player:getInventory():Remove(item)
                sendRemoveItemFromContainer(player:getInventory(), item);
            end
            table.remove(ids, j);
            return true;
        end
    end
end

QItemFactory.remove = function (player, ids)
    if type(ids) == "table" then
        local size = #ids;
        local inventory = player:getInventory():getItems();
        for i=inventory:size()-1, 0, -1 do
            if size == 0 then break end
            local o = inventory:get(i);
            if instanceof(o, "InventoryContainer") then
                local container = o:getInventory():getItems();
                for j=container:size()-1, 0, -1 do
                    if removeItemsWithIDs(player, container:get(j), ids) then
                        size = size - 1;
                        if size == 0 then break end
                    end
                end
            elseif instanceof(o, "InventoryItem") then
                if removeItemsWithIDs(player, o, ids) then
                    size = size - 1;
                end
            else
                print("[QSystem] (Error) QItemFactory: Unexpected argument "..type(o))
            end
        end
        if ids[1] then
            print("[QSystem] (Error) QItemFactory: Failed to remove the following items: "..table.concat(ids, ', '))
        else
            return true;
        end
    end
end