-- From "More Damaged Objects [B42]" mod -- Author = carlesturo

local MDO_SpriteData = require("MDO_SpriteData")
local MDO_Utils = require("MDO_Utils")
local MDO_StreetLightsTall_Utils = require("MDO_StreetLightsTall_Utils")
local MDO_StreetLightsShort_Utils = require("MDO_StreetLightsShort_Utils")
local MDO_WallsAndFences_Utils = require("MDO_WallsAndFences_Utils")
local MDO_Bushes_Utils = require("MDO_Bushes_Utils")
local MDO_Standpipe_Utils = require("MDO_Standpipe_Utils")

-- ------------------------------------------------------------------------------------------------

-- **************** MORE DAMAGED OBJECTS - MAIN ****************

local MoreDamagedObjects = {}

isInVehicle = false

MoreDamagedObjects.OnCreatePlayer = function(index, player)
	MoreDamagedObjects.isInVehicle = player:getVehicle() and true or false
	if MoreDamagedObjects.isInVehicle then
		Events.OnWorldSound.Add(MoreDamagedObjects.OnWorldSound)
	end
end
Events.OnCreatePlayer.Add(MoreDamagedObjects.OnCreatePlayer)

local function OnEnterVehicle(character)
	MoreDamagedObjects.isInVehicle = true
	Events.OnWorldSound.Add(MoreDamagedObjects.OnWorldSound)
end
Events.OnEnterVehicle.Add(OnEnterVehicle)

local function OnExitVehicle(character)
	MoreDamagedObjects.isInVehicle = false
	Events.OnWorldSound.Remove(MoreDamagedObjects.OnWorldSound)
end
Events.OnExitVehicle.Add(OnExitVehicle)

MoreDamagedObjects.OnWorldSound = function(x, y, z, radius, volume, source)
    if MoreDamagedObjects.isInVehicle then
        if radius == 20 and volume == 20 and not source then
            MDO_Utils.DelayFunction(function()
                local foundSprites = false
                for dx = -2, 2 do
                    for dy = -2, 2 do
                        local square = getSquare(x + dx, y + dy, z)
                        if square then
                            local objects = square:getObjects()
                            for i = 0, objects:size() - 1 do
                                local obj = objects:get(i)
                                if obj and obj:getSprite() then
                                    local spriteName = obj:getSprite():getName()
                                    local spriteData = MDO_SpriteData.getSpriteDataByBaseSprite(spriteName)
                                    if spriteData then

                                        if spriteName == spriteData.baseSprite then -- Street Lights
                                            local modData = obj:getModData()
                                            local squareAbove = getSquare(x, y, 1)
                                            if squareAbove then
                                                local replacedSprites = MDO_StreetLightsTall_Utils.replaceObjectsInSquareAbove(squareAbove, spriteData)
                                                for _, aboveReplacementSprite in ipairs(replacedSprites) do
                                                    MDO_StreetLightsTall_Utils.replaceBaseSprite(spriteData, aboveReplacementSprite, obj)
                                                    MDO_StreetLightsTall_Utils.addExtraSprites(spriteData, aboveReplacementSprite, square)
                                                    MDO_StreetLightsTall_Utils.addBrokenGlass(spriteData, aboveReplacementSprite, square)
                                                end
                                            elseif not modData.spriteUpdatedAndBrokenGlassAdded then
                                                MDO_StreetLightsShort_Utils.replaceSpriteAndAddBrokenGlass(square, spriteData)
                                                modData.spriteUpdatedAndBrokenGlassAdded = true
                                            end

                                        elseif spriteName == spriteData.baseSpriteWestWall or spriteName == spriteData.baseSpriteNorthWall or 
                                               spriteName == spriteData.baseSpriteNorthWestCorner or spriteName == spriteData.baseSpriteSouthEastCorner then -- Walls and Fences
                                            local modData = obj:getModData()
                                            if not modData.spriteWallOrFenceUpdatedAndFloorSpriteAdded then
                                                MDO_WallsAndFences_Utils.removeAndAddWallOrFenceSpriteAndAddFloorSprite(square, spriteData, spriteName)
                                                modData.spriteWallOrFenceUpdatedAndFloorSpriteAdded = true
                                            end

										elseif spriteName == spriteData.baseSpriteBush then -- Bushes
											MDO_Bushes_Utils.removeBushSpriteAndAddItems(square, spriteData)

										elseif spriteName == spriteData.baseSpriteStandpipe then -- Standpipe (Fire Hydrant)
											MDO_Standpipe_Utils.animateWaterJet(square, spriteData, true)
										end
                                    end
                                end
                            end
                        end
                    end
                end
            end, 1) -- Delay 1 tick
        end
    end
end

-- ------------------------------------------------------------------------------------------------

local function ReactivateWaterJetAnimation(square) -- Reactivate Water Jet Animation
	if not square then return end

    local modData = square:getModData()
    if not modData.waterJetActive then return end

	local objects = square:getObjects()
	if not objects then return end

	for i = 0, objects:size() - 1 do
		local obj = objects:get(i)

		local sprite = obj and obj:getSprite()
		if sprite then
			local spriteName = sprite:getName()
			local spriteData = MDO_SpriteData.getSpriteDataFromNewStandpipeSprite(spriteName)
			if spriteData then
				MDO_Standpipe_Utils.animateWaterJet(square, spriteData, false)
				break
			end
		end
	end
end
Events.LoadGridsquare.Add(ReactivateWaterJetAnimation)
