-- From "More Damaged Objects [B41]" mod -- Author = carlesturo

local MDO_SpriteData = require("MDO_SpriteData")
local MDO_Utils = require("MDO_Utils")
local IsoObjectUtilsFromStarlitLibrary = require("IsoObjectUtilsFromStarlitLibrary")

local MDO_StreetLightsShort_Utils = {}

-- ------------------------------------------------------------------------------------------------

-- **************** MORE DAMAGED OBJECTS - UTILS - STREET LIGHTS SHORT ****************

function MDO_StreetLightsShort_Utils.replaceSpriteAndAddBrokenGlass(square, spriteData) -- Replace Sprite and Add Broken Glass
    if not square or not spriteData then return end
    local player = getPlayer()
    local playerX = player:getX()
    local spriteX = square:getX()
	local cell = getCell()

    local newSprite, dx

    if playerX < spriteX then
        newSprite = spriteData.spriteLeft
        dx = -1
		MDO_Utils.replaceAllObjectsBySprite(square, spriteData.baseSprite, newSprite)
    elseif playerX > spriteX then
        dx = 1
		local lightSource = cell:getLightSourceAt(square:getX(), square:getY(), square:getZ())
        if lightSource then
            --cell:removeLamppost(lightSource) -- No need, because I use sendClientCommand

			local args = { x = square:getX(), y = square:getY(), z = square:getZ() }
			sendClientCommand(player, "MDO", "RemoveLampPost", args)
        end
    end

	local emitter = getWorld():getFreeEmitter(square:getX(), square:getY(), square:getZ())
	local volume = 1.0
	emitter:playSound("SmashWindow")
	emitter:setVolumeAll(volume)

    --local args = { x = square:getX(), y = square:getY(), z = square:getZ(), sound = "SmashWindow", volume = volume }
    --sendClientCommand(player, "MDO", "RequestPlaySound", args)

    if spriteData.brokenGlassSprites then
        local newSquare = IsoObjectUtilsFromStarlitLibrary.getOrCreateSquare(square:getX() + dx, square:getY(), 0)
        if newSquare then
            local randomGlassSprite = spriteData.brokenGlassSprites[ZombRand(#spriteData.brokenGlassSprites) + 1]
            MDO_Utils.addObjectToSquare(newSquare, randomGlassSprite)
        end
    end
end

-- ------------------------------------------------------------------------------------------------

return MDO_StreetLightsShort_Utils