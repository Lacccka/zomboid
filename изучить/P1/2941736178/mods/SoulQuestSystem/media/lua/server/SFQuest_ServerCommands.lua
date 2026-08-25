local Commands = {}

function Commands.saveData(player, args)
	local id = player:getUsername();

	--print("Parsing data table for ID " .. id);
	--Write the text file
	local filepath = "/Backup/SFQuest_" .. id .. ".txt";
	--print("File path is: " .. filepath);
	local filewriter = getFileWriter(filepath, true, false);
	SFQuest_Server.parseTable(args, filewriter, "temp");
	print("SOUL QUEST SYSTEM - Saved quest data for ID: " .. id);
end

function Commands.sendData(player, args)
	local id = args.id;
	print("SOUL QUEST SYSTEM - Server received a request for quest data. Player ID: " .. id);
	local filepath = "/Backup/SFQuest_" .. id .. ".txt";
	local filereader = getFileReader(path, false);
	if filereader then
		print("SOUL QUEST SYSTEM - Located backup file player " .. id);
		local temp = {};
		local line = filereader:readLine();
		while line ~= nil do
			table.insert(temp, line);
			line = filereader:readLine();
		end
		filereader:close();
		local newargs = { id = id , data = temp };
		print("SOUL QUEST SYSTEM - Requested quest data for player " .. id " sent.");
		sendServerCommand('SFQuest', "setProgress", newargs);
	end;
end

Events.OnClientCommand.Add(function(module, command, player, args)
	if module == 'SFQuest' and Commands[command] then
		args = args or {}
		Commands[command](player, args)
	end
end)