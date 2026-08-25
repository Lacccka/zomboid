-- Copyright (c) 2022-2024 Oneline/D.Borovsky
-- All rights reserved
SyncServer = {};
SyncServer.data = {};

SyncServer.OnClientCommand = function(module, command, player, args)
	--print("SyncServer: Received '"..module.."."..command.."' command from server!");
	if module == 'NFO' then
		NFOServer.OnClientCommand(module, command, player, args);
	elseif module == 'PrivateUI' then
		PrivateSync.OnClientCommand(module, command, player, args);
	elseif module == 'QSystem' then
		QSystem.OnClientCommand(module, command, player, args);
	elseif module == 'Market' then
		Marketplace.OnClientCommand(module, command, player, args);
	end
end

Events.OnClientCommand.Add(SyncServer.OnClientCommand);