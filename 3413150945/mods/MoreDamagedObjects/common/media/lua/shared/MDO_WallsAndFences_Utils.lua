-- From "More Damaged Objects [B42]" mod -- Author = carlesturo

local MDO_SpriteData = require("MDO_SpriteData")
local MDO_Utils = require("MDO_Utils")
local IsoObjectUtilsFromStarlitLibrary = require("IsoObjectUtilsFromStarlitLibrary")

local MDO_WallsAndFences_Utils = {}

-- ------------------------------------------------------------------------------------------------

-- **************** MORE DAMAGED OBJECTS - UTILS - WALLS AND FENCES ****************

function MDO_WallsAndFences_Utils.removeAndAddWallOrFenceSpriteAndAddFloorSprite(square, spriteData, spriteName) -- Remove and Add Wall or Fence Sprite and Add Floor Sprite
    if not square or not spriteData then return end
    local player = getPlayer()
    local playerX = player:getX()
    local playerY = player:getY()
    local spriteX = square:getX()
    local spriteY = square:getY()

    local newSprite
	local dx, dy = 0, 0

    if spriteName == spriteData.baseSpriteWestWall then
		newSprite = spriteData.newSpriteWestWall
		if playerX > spriteX then dx = -1 end
    elseif spriteName == spriteData.baseSpriteNorthWall then
		newSprite = spriteData.newSpriteNorthWall
        if playerY > spriteY then dy = -1 end
	elseif spriteName == spriteData.baseSpriteNorthWestCorner then
		newSprite = spriteData.newSpriteNorthWestCorner
		dx, dy = -1, -1
	elseif spriteName == spriteData.baseSpriteSouthEastCorner then
		newSprite = spriteData.newSpriteSouthEastCorner
	end

    MDO_Utils.removeAllAndAddObjectInSquare(square, spriteName, newSprite)

	local soundName
	local volume = 1.0
	if spriteData.brokenGlassSprites then
		soundName = "SmashWindow"
		volume = 1.0
	elseif spriteData.brokenWhiteWoodSprites or spriteData.brokenBrownWoodSprites or spriteData.brokenCarpentrySprites or 
		   spriteData.brokenNewB42WoodSprites or spriteData.brokenBurnedSprites or spriteData.brokenWhiteWoodSprites2 then
		soundName = "BreakObject"
		volume = 1.0
	elseif spriteData.brokenWireSprites or spriteData.brokenPoleSprites then
		soundName = "ZombieThumpChainlinkFence"
		volume = 1.0
	end

    if soundName then
        local emitter = getWorld():getFreeEmitter(square:getX(), square:getY(), square:getZ())
        emitter:playSound(soundName)
        emitter:setVolumeAll(volume)

		--local args = { x = square:getX(), y = square:getY(), z = square:getZ(), sound = soundName, volume = volume }
		--sendClientCommand(player, "MDO", "RequestPlaySound", args)
    end

	if spriteData.brokenGlassSprites or spriteData.brokenWhiteWoodSprites or spriteData.brokenWireSprites or 
	   spriteData.brokenBrownWoodSprites or spriteData.brokenCarpentrySprites or spriteData.brokenNewB42WoodSprites or 
	   spriteData.brokenBurnedSprites or spriteData.brokenWhiteWoodSprites2 or spriteData.brokenPoleSprites then
		local allFloorSpriteTables = {}

		if spriteData.brokenGlassSprites then
			table.insert(allFloorSpriteTables, spriteData.brokenGlassSprites)
		end
		if spriteData.brokenWhiteWoodSprites then
			table.insert(allFloorSpriteTables, spriteData.brokenWhiteWoodSprites)
		end
		if spriteData.brokenWireSprites then
			table.insert(allFloorSpriteTables, spriteData.brokenWireSprites)
		end
		if spriteData.brokenBrownWoodSprites then
			table.insert(allFloorSpriteTables, spriteData.brokenBrownWoodSprites)
		end
		if spriteData.brokenCarpentrySprites then
			table.insert(allFloorSpriteTables, spriteData.brokenCarpentrySprites)
		end
		if spriteData.brokenNewB42WoodSprites then
			table.insert(allFloorSpriteTables, spriteData.brokenNewB42WoodSprites)
		end
		if spriteData.brokenBurnedSprites then
			table.insert(allFloorSpriteTables, spriteData.brokenBurnedSprites)
		end
		if spriteData.brokenWhiteWoodSprites2 then
			table.insert(allFloorSpriteTables, spriteData.brokenWhiteWoodSprites2)
		end
		if spriteData.brokenPoleSprites then
			table.insert(allFloorSpriteTables, spriteData.brokenPoleSprites)
		end

		for _, selectedFloorSprites in ipairs(allFloorSpriteTables) do
			local randomFloorSprite = selectedFloorSprites[ZombRand(#selectedFloorSprites) + 1]
			if spriteName == spriteData.baseSpriteWestWall or spriteName == spriteData.baseSpriteNorthWall then
				local newSquare = IsoObjectUtilsFromStarlitLibrary.getOrCreateSquare(square:getX() + (dx or 0), square:getY() + (dy or 0), 0)
				if playerX < spriteX or playerY < spriteY then
					MDO_Utils.addObjectToSquare(square, randomFloorSprite)
				elseif newSquare then
					MDO_Utils.addObjectToSquare(newSquare, randomFloorSprite)
				end
			elseif spriteName == spriteData.baseSpriteNorthWestCorner then
				if playerX < spriteX or playerY < spriteY then
					MDO_Utils.addObjectToSquare(square, randomFloorSprite)
					MDO_Utils.addObjectToSquare(square, randomFloorSprite)
				else
					local newSquare1 = IsoObjectUtilsFromStarlitLibrary.getOrCreateSquare(square:getX() + dx, square:getY(), 0)
					local newSquare2 = IsoObjectUtilsFromStarlitLibrary.getOrCreateSquare(square:getX(), square:getY() + dy, 0)
					if newSquare1 then MDO_Utils.addObjectToSquare(newSquare1, randomFloorSprite) end
					if newSquare2 then MDO_Utils.addObjectToSquare(newSquare2, randomFloorSprite) end
				end
			end
		end
	end
end

-- ------------------------------------------------------------------------------------------------

return MDO_WallsAndFences_Utils