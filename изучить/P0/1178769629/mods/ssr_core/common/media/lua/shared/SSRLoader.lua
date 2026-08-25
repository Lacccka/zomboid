-- Copyright (c) 2022-2024 Oneline/D.Borovsky
-- All rights reserved
require "SSRCore"
require "SSRTimer"

SSRLoader = {}
SSRLoader.initialized = false;
SSRLoader.timezone = 0;

SSRLoader.getScale = function ()
	if not isServer() then
		local height = getTextManager():getFontHeight(UIFont.Small);
		if height == 14 then return 1
		elseif height == 16 then return 1.066667
		elseif height == 19 then return 1.384615
		elseif height == 26 then return 1.547619
		elseif height == 33 then return 1.904762
		elseif height == 38 then return 2.142857
		end
	end
	return 1;
end

SSRLoader.scale = SSRLoader.getScale();

SSRLoader.NFO = require("NFOCompatibility");
SSRLoader.QSystem = require("QuestsCompatibility");
SSRLoader.Market = require("MarketCompatibility");
SSRLoader.Private = require("PrivateCompatibility");
SSRLoader.Expansion = require("ExpansionCompatibility");


function DrawSSRCoreErrorMsg()
	getTextManager():DrawString(UIFont.NewSmall, 10, getCore():getScreenHeight() - 35*SSRLoader.scale, "Unauthorized mod reupload detected. Some mods were disabled.", 1.0, 0.0, 0.0, 1.0);
	getTextManager():DrawString(UIFont.NewSmall, 10, getCore():getScreenHeight() - 20*SSRLoader.scale, "Repacking/reuploading ssr mods is strictly forbidden, as stated by the copyright message in mod description on steam workshop.", 1.0, 0.0, 0.0, 1.0);
end

-- Dedicated to people who either not read or ignore the copyright message, that says NOT to reupload or repack my mods.
-- Stop being ignorant shitheads, seriously. This will only get you banned from steam community. From now on I'll be submitting DMCA complaints without warning.
local function load()
	if not SSRLoader.initialized then
		SSRLoader.initialized = true;
		if isServer() then
			if SSRLoader.NFO then
				NFOServer.init();
			end

			if SSRLoader.Private then
				FW.init();
			end
		elseif isClient() then
			if getSteamModeActive() then
				local function deobfuscate(a, b, c) return (c and "ssr-" or "")..a..b; end
				local function gtfo() print("[Core] RatPoison: Unauthorized mod reupload detected. Some mods were disabled."); Events.OnPostUIDraw.Add(DrawSSRCoreErrorMsg); end
				local mods = { { deobfuscate("co","re", true), deobfuscate("11787","69629") } };
				if SSRLoader.NFO then mods[#mods+1] = { deobfuscate("ne", "ws", true), deobfuscate("11787","73471") }; end
				if SSRLoader.Private then mods[#mods+1] = { PrivateSync and deobfuscate("pri","vate", true) or deobfuscate("priva","te-lite", true), deobfuscate("11787","72929") }; end
				if SSRLoader.QSystem then mods[#mods+1] = { deobfuscate("que","sts", true), deobfuscate("27933","85743") }; end
				local modString = getServerOptions():getOptionByName("Mods"):getValue();
				local activeMods = modString:ssplit(";");
				for i=1, #activeMods do
					local modID = activeMods[i];
					local modInfo = getModInfoByID(modID);
					local workshopID = modInfo and modInfo:getWorkshopID();
					for j=#mods, 1, -1 do
						if modID == mods[j][1] or modID == "\\"..mods[j][1] then
							if workshopID and workshopID ~= "" and workshopID ~= mods[j][2] then
								return gtfo();
							else
								table.remove(mods, j); break;
							end
						end
					end
				end
				if #mods > 0 then return gtfo(); end
			end

			if SSRLoader.NFO then
				NFOClient.init();
			end

			if SSRLoader.Private then
				Private.init();
			end
		end

		if SSRLoader.QSystem then
			QSystem.init();
		end

		if SSRLoader.Market then
			Marketplace.init();
		end
	end
end

SSRLoader.onGameStartZombie = function(zombie)
	Events.OnZombieUpdate.Remove(SSRLoader.onGameStartZombie); -- remove event after zombies appeared on map and got their first update
	load();
end

local function setTimeZone()
	local sdf = SimpleDateFormat.new("X");
	local timestamp = Calendar.getInstance():getTime();
	SSRLoader.timezone = tonumber(sdf:format(timestamp)) or 0;
end

SSRLoader.init = function ()
	setTimeZone();
	if isServer() then
		load();
	else
		SSRTimer.add_ms(SSRLoader.onGameStartZombie, SSRLoader.NFO and 3000 or 500, false); -- a) load after 3 seconds or 500 ms
		Events.OnZombieUpdate.Add(SSRLoader.onGameStartZombie); -- b) load after the first zombie update
	end
end

Events.OnGameStart.Add(SSRLoader.init)
Events.OnServerStarted.Add(SSRLoader.init);

SSRLoader.preinit = function ()
	if SSRLoader.QSystem then
		QImport.preinit();
	end
end

if not isServer() then
	Events.OnPostMapLoad.Add(SSRLoader.preinit);
end