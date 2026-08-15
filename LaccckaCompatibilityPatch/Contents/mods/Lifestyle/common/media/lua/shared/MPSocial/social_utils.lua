--------------------------------------------------------------------------------------------------
--		----	  |			  |			|		 |				|    --    |      ----			--
--		----	  |			  |			|		 |				|    --	   |      ----			--
--		----	  |		-------	   -----|	 ---------		-----          -      ----	   -------
--		----	  |			---			|		 -----		------        --      ----			--
--		----	  |			---			|		 -----		-------	 	 ---      ----			--
--		----	  |		-------	   ----------	 -----		-------		 ---      ----	   -------
--			|	  |		-------			|		 -----		-------		 ---		  |			--
--			|	  |		-------			|	 	 -----		-------		 ---		  |			--
--------------------------------------------------------------------------------------------------

LSMPS = LSMPS or {}

LSMPS.isPlayerNearby = function(src, target, range)
	local srcX, srcY, srcZ = src:getX(), src:getY(), src:getZ()
	local tX, tY, tZ = target:getX(), target:getY(), target:getZ()
	return srcZ == tZ and srcX >= tX-range and srcX <= tX+range and srcY >= tY-range and srcY <= tY+range
end

LSMPS.getOrCreatePersonalInfo = function(character)
	local charData = character:getModData()
	if charData.LSSocial then return charData.LSSocial; end
	charData.LSSocial = {}
	-- id
	local id = character:getSteamID() or character:getUsername()
	charData.LSSocial.id = id
	-- hidden skills
	if not charData.LSHiddenSkills then -- initialise hs table
		HiddenSkills.getSkill(character, "Yoga")
	end
	charData.LSSocial.hs = charData.LSHiddenSkills
	LSSync.updateClientData(character, charData)
	return charData.LSSocial
end

LSMPS.getPlayersNearby = function(area, x, y, exclude)
	local players = getOnlinePlayers()
	if players then
		local nearbyPlayers = {}
		for i=1,players:size() do
			local player = players:get(i-1)
			if player and (not player.isAnimal or not player:isAnimal()) then
				local id = player.getOnlineID and player:getOnlineID()
				if id and id ~= exclude then
					local pX, pY = player:getX(), player:getY()
					if pX >= x-area and pX < x+area and pY >= y-area and pY < y+area then
						table.insert(nearbyPlayers, player)
					end
				end
			end
		end
		return nearbyPlayers
	end
	return false
end

--[[
function ScanForPlayers(command, args)
	if not command or not args then return; end
	if type(args) ~= "table" then return; end
	local playerObj = getPlayer()
	local players = getOnlinePlayers();
	if players then
		for i=1,players:size() do
			local player = players:get(i-1)
			if player and player ~= playerObj and (not player.isAnimal or not player:isAnimal()) then
				if player:getX() >= playerObj:getX() - 30 and player:getX() < playerObj:getX() + 30 and
                   player:getY() >= playerObj:getY() - 30 and player:getY() < playerObj:getY() + 30 then
					sendClientCommand(playerObj, "LS", command, {player:getOnlineID(), playerObj:getDisplayName(), args})
				end
			end
		end
	end
end
]]--

LSMPS.getRandomVoxSound = function(parent, group, oldSound, isFemale)
	local audioKeys = LSMPS.audioLib[parent][group]
	local gender = (isFemale and "Woman") or "Man"
	local possibleSounds = {}
	for k, v in pairs(audioKeys) do
		local strPrefix = (not v.sex and "") or ((v.sex == "All" or v.sex == gender) and gender)
		if strPrefix then
			for n=1,v.total do
				local strSuffix = (n < 10 and "0"..tostring(n)) or tostring(n)
				local soundName = strPrefix..v.name..strSuffix
				if not oldSound or soundName ~= oldSound then table.insert(possibleSounds, soundName); end
			end
		end
	end
	local rdmIdx = LSUtil.rdm_inst:random(#possibleSounds)
	return possibleSounds[rdmIdx]
end