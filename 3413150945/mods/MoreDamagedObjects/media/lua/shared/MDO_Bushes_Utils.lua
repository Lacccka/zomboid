-- From "More Damaged Objects [B41]" mod -- Author = carlesturo

local MDO_SpriteData = require("MDO_SpriteData")
local MDO_Utils = require("MDO_Utils")
local IsoObjectUtilsFromStarlitLibrary = require("IsoObjectUtilsFromStarlitLibrary")

local MDO_Bushes_Utils = {}

-- ------------------------------------------------------------------------------------------------

-- **************** MORE DAMAGED OBJECTS - UTILS - BUSHES ****************

function MDO_Bushes_Utils.removeBushSpriteAndAddItems(square, spriteData) -- Remove Bush Sprite and Add Items
    if not square or not spriteData then return end

    MDO_Utils.removeAllObjectsBySprite(square, spriteData.baseSpriteBush)

	local player = getPlayer()

	local emitter = getWorld():getFreeEmitter(square:getX(), square:getY(), square:getZ())
	local volume = 3.0
	emitter:playSound("VehicleHitTree")
	emitter:setVolumeAll(volume)

    --local args = { x = square:getX(), y = square:getY(), z = square:getZ(), sound = "VehicleHitTree", volume = volume }
    --sendClientCommand(player, "MDO", "RequestPlaySound", args)

    local itemsToAdd = {}

    if ZombRand(2) == 0 then
        table.insert(itemsToAdd, "Base.TreeBranch")
    end
    if ZombRand(1) == 0 then
        table.insert(itemsToAdd, "Base.Twigs")
    end

    if #itemsToAdd > 0 then
        local args = { x = square:getX(), y = square:getY(), z = square:getZ(), items = itemsToAdd }
        sendClientCommand(player, "MDO", "AddWorldItems", args)
    end
end

-- ------------------------------------------------------------------------------------------------

return MDO_Bushes_Utils