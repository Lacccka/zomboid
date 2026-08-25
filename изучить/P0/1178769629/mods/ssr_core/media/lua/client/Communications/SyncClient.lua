-- Copyright (c) 2022-2024 Oneline/D.Borovsky
-- All rights reserved
SyncClient = {};
SyncClient.queue = {}
SyncClient.last_id = 0;

SyncClient.callback = function (id)
    for i=#SyncClient.queue,1,-1 do
        if SyncClient.queue[i].id == id then
            SyncClient.queue[i].callback();
            table.remove(SyncClient.queue, i);
        end
    end
end

SyncClient.extract = function (id)
    for i=#SyncClient.queue,1,-1 do
        if SyncClient.queue[i].id == id then
            return table.remove(SyncClient.queue, i).callback;
        end
    end
end

SyncClient.request = function (module, command, args, sender, callback)
	if callback then
		local entry = {};
		entry.id = tostring(SyncClient.last_id);
		entry.callback = callback;
		SyncClient.last_id = SyncClient.last_id + 1;
		SyncClient.queue[#SyncClient.queue+1] = entry;
		sendClientCommand(getPlayer(), module, command, { sender, args, entry.id });
	else
		sendClientCommand(getPlayer(), module, command, { sender, args });
	end
end

SyncClient.sendCommand = function(module, command)
	-- Private UI operations
	if module == 'PrivateUI' then
		if command == 'sendUpdate' then
			local list = Private.getUnknownPlayers(getPlayer():getSquare())
			for i=1, #list do
				print(list[i]);
			end
			sendClientCommand(getPlayer(), module, command, {list});
		elseif command == 'sendAlert_1' or command == 'sendAlert_2' then
			-- Inform the enemy
			local safehouse = SafeHouse.getSafeHouse(getPlayer():getSquare());
			if not safehouse then
				print("SyncClient: Safehouse is NULL.");
				return;
			end
			local faction = Private.getFaction(safehouse:getOwner(), true)
			if not faction then
				print("SyncClient: Faction is NULL.");
				return;
			end

			local list_enemies = {}

			table.insert(list_enemies, faction:getOwner())
			for j=0,faction:getPlayers():size()-1 do
				table.insert(list_enemies, faction:getPlayers():get(j));
			end

			-- Inform allies
			local leader = getPlayer():getUsername()
			faction = Private.getFaction(leader, true)
			local list_allies = {}

			for j=0,faction:getPlayers():size()-1 do
				local p = faction:getPlayers():get(j)
				if p ~= leader then
					table.insert(list_allies, faction:getPlayers():get(j));
				end
			end

			if command == 'sendAlert_1' then
				addLineInChat(getText("UI_Text_SafehouseIsBeingCaptured", safehouse:getTitle()), 0);
				sendClientCommand(getPlayer(), module, command, {list_enemies, list_allies, safehouse:getTitle(), 1});
			elseif command == 'sendAlert_2' then
				addLineInChat(getText("UI_Text_SafehouseWasCaptured", safehouse:getTitle()), 0);
				sendClientCommand(getPlayer(), module, command, {list_enemies, list_allies, safehouse:getTitle(), 2});
			end
		end
	end
end

SyncClient.onServerCommand = function(module, command, args)
	if module == 'NFO' then
		NFOClient.onServerCommand(module, command, args)
	elseif module == "PrivateUI" then
		PrivateSync.OnServerCommand(module, command, args)
	elseif module == "QSystem" then
		QSystem.OnServerCommand(module, command, args)
	elseif module == "Market" then
		Marketplace.OnServerCommand(module, command, args)
	end
end

Events.OnServerCommand.Add(SyncClient.onServerCommand);