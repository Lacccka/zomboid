local Commands = {}

function Commands.setProgress(args)
	local player = getPlayer();
	local id = player:getUsername();
	if not id == args.id then
		return
	end

	print("SOUL QUEST SYSTEM - Backup data received from server, recovering quest progress.");
	local temp = {};
	for a, b in ipairs(args.data) do
		local line = b;
		assert(loadstring(b));
	end
	if not temp.Delivery then
		print("SOUL QUEST SYSTEM - Data transformation likely to be corrupted, aborting backup.");
		return
	end
	player:getModData().missionProgress = temp;
	SF_MissionPanel.instance:triggerUpdate();
end

Events.OnServerCommand.Add(function(module, command, args)
	if not isClient() then return end
	if module == "SFQuest" and Commands[command] then
		args = args or {}
		Commands[command](args)
	end
end)