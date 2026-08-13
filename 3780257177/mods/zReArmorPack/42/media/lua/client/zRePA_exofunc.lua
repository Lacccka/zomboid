local function zRe_checkWeightLimit()
	local player = getPlayer();
	local maxWeightBase = 8

	local zReExo = player:getWornItem(zReBodyLocation.ZOMBIEREENGINE_EXOSKELET)
	local zReKGChanger = 0;
	if zReExo then
		zReKGChanger = zReKGChanger + 2
	end
	
	if getActivatedMods():contains("\\SimpleOverhaulTraitsAndOccupations") or getActivatedMods():contains("\\zReModServerB42") then
		if player:hasTrait(SOTO.CharacterTrait.STRONG_BACK) then
			zReKGChanger = zReKGChanger + 1
		elseif player:hasTrait(SOTO.CharacterTrait.WEAK_BACK) then
			zReKGChanger = zReKGChanger - 1
		end
	end
	
	maxWeightBase = maxWeightBase + zReKGChanger
	if player:getMaxWeightBase() ~= maxWeightBase then
        if isClient() then
            sendClientCommand(player, 'zRePA', 'zRe_updateWeight', { weight = maxWeightBase })
        end
        player:setMaxWeightBase(maxWeightBase)
    end
end

Events.OnGameStart.Add(zRe_checkWeightLimit)
Events.OnCreatePlayer.Add(zRe_checkWeightLimit)
Events.zReOnClothingEquipped.Add(zRe_checkWeightLimit)	-- zRePA_Event.lua
Events.OnClothingUpdated.Add(zRe_checkWeightLimit)

if getActivatedMods():contains("\\SimpleOverhaulTraitsAndOccupations") then
	Events.EveryHours.Remove(SOcheckWeight);
	Events.EveryHours.Add(zRe_checkWeightLimit);
	Events.OnGameStart.Remove(SOcheckWeight);
	Events.OnCreatePlayer.Remove(SOcheckWeight);
end

--[[
if getActivatedMods():contains("\\DracoExpandedTraits") then	-- Локальная функция
end
if getActivatedMods():contains("\\ToadTraits") then
	Events.EveryTenMinutes.Remove(checkWeight);
end
]]