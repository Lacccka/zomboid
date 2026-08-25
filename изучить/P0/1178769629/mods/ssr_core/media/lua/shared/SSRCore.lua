-- Copyright (c) 2022-2024 Oneline/D.Borovsky
-- All rights reserved
JM = {}
JM.installed = false;

local version = 0;

JM.init = function ()
	local status;
	status, version = pcall(jm_version, nil);
	if status and version then
		JM.installed = true;
		print(string.format("[Core] SSROverride is installed. Version: %.2f", version))
	else
		print("[Core] SSROverride is not installed or outdated version is used.");
	end
end

JM.require = function(v)
	if JM.installed then
		return version >= v;
	else
		return false;
	end
end

Events.OnServerStarted.Add(JM.init);