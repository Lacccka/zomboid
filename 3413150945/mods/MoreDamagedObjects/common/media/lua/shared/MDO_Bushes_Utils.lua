-- From "More Damaged Objects [B42]" mod -- Author = carlesturo

local MDO_SpriteData = require("MDO_SpriteData")
local MDO_Utils = require("MDO_Utils")
local IsoObjectUtilsFromStarlitLibrary = require("IsoObjectUtilsFromStarlitLibrary")

local MDO_Bushes_Utils = {}

-- ------------------------------------------------------------------------------------------------

-- **************** MORE DAMAGED OBJECTS - UTILS - BUSHES ****************

function MDO_Bushes_Utils.removeBushSpriteAndAddItems(square, spriteData) -- Remove Bush Sprite and Add Items
    if not square or not spriteData then return end

    MDO_Utils.removeAllObjectsBySprite(square, spriteData.baseSpriteBush)

	--local player = getPlayer()

	local emitter = getWorld():getFreeEmitter(square:getX(), square:getY(), square:getZ())
	local volume = 3.0
	emitter:playSound("VehicleHitTree")
	emitter:setVolumeAll(volume)

    --local args = { x = square:getX(), y = square:getY(), z = square:getZ(), sound = "VehicleHitTree", volume = volume }
    --sendClientCommand(player, "MDO", "RequestPlaySound", args)

    if ZombRand(2) == 0 then
        square:AddWorldInventoryItem("Base.TreeBranch2", 0, 0, 0)
    end
    if ZombRand(1) == 0 then
        square:AddWorldInventoryItem("Base.Twigs", 0, 0, 0)
    end
end

-- ------------------------------------------------------------------------------------------------

return MDO_Bushes_Utils