-- From "More Damaged Objects [B41]" mod -- Author = carlesturo

local MDO_SpriteData = require("MDO_SpriteData")
local MDO_Utils = require("MDO_Utils")
local IsoObjectUtilsFromStarlitLibrary = require("IsoObjectUtilsFromStarlitLibrary")

local MDO_StreetLightsTall_Utils = {}

-- ------------------------------------------------------------------------------------------------

-- **************** MORE DAMAGED OBJECTS - UTILS - STREET LIGHTS TALL ****************

function MDO_StreetLightsTall_Utils.replaceObjectsInSquareAbove(squareAbove, spriteData) -- Replace Objects in Square Above
    local objectsAbove = squareAbove:getObjects()
    local objectsToReplace = {}
    local replacedSprites = {}

    for i = 0, objectsAbove:size() - 1 do
        local objAbove = objectsAbove:get(i)
        if objAbove and objAbove:getSprite() then
            local spriteName = objAbove:getSprite():getName()
            local replacementSprite = spriteData.aboveSpriteReplacements[spriteName]
            if replacementSprite then
                table.insert(objectsToReplace, {oldSprite = spriteName, newSprite = replacementSprite})
            end
        end
    end

    for _, data in ipairs(objectsToReplace) do
        if MDO_Utils.replaceAllObjectsBySprite(squareAbove, data.oldSprite, data.newSprite) then
            table.insert(replacedSprites, data.newSprite)
        end
    end

    return replacedSprites
end


function MDO_StreetLightsTall_Utils.replaceBaseSprite(spriteData, spriteType, obj) -- Replace Base Sprite
    if spriteData and spriteData.baseSpriteReplacement then
        for newBase, sprites in pairs(spriteData.baseSpriteReplacement) do
            for _, sprite in ipairs(sprites) do
                if spriteType == sprite then
                    obj:setSpriteFromName(newBase)
                    break
                end
            end
        end
    end
end

function MDO_StreetLightsTall_Utils.addExtraSprites(spriteData, spriteType, square) -- Add Extra Sprites
    if spriteData and spriteData.additionalSprites then
        local extraSprites = spriteData.additionalSprites[spriteType]
        if extraSprites then
            for _, coord in ipairs(extraSprites) do
                local newSquare = IsoObjectUtilsFromStarlitLibrary.getOrCreateSquare(square:getX() + coord.dx, square:getY() + coord.dy, 1)
                if newSquare then
                    MDO_Utils.addObjectToSquare(newSquare, coord.sprite)
                end
            end
        end
    end
end

function MDO_StreetLightsTall_Utils.addBrokenGlass(spriteData, spriteType, square) -- Add Broken Glass
	--local player = getPlayer()

	local emitter = getWorld():getFreeEmitter(square:getX(), square:getY(), square:getZ())
	local volume = 1.0
	emitter:playSound("SmashWindow")
	emitter:setVolumeAll(volume)

    --local args = { x = square:getX(), y = square:getY(), z = square:getZ(), sound = "SmashWindow", volume = volume }
    --sendClientCommand(player, "MDO", "RequestPlaySound", args)

    if spriteData and spriteData.brokenGlassSprites then
        local brokenGlassPos = spriteData.brokenglassPositions[spriteType]
        if brokenGlassPos then
            for _, pos in ipairs(brokenGlassPos) do
                local newSquare = IsoObjectUtilsFromStarlitLibrary.getOrCreateSquare(square:getX() + pos.dx, square:getY() + pos.dy, 0)
                if newSquare then
                    local randomGlassSprite = spriteData.brokenGlassSprites[ZombRand(#spriteData.brokenGlassSprites) + 1]
                    MDO_Utils.addObjectToSquare(newSquare, randomGlassSprite)
                end
            end
        end
    end
end

-- ------------------------------------------------------------------------------------------------

return MDO_StreetLightsTall_Utils