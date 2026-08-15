-- From "More Damaged Objects [B41]" mod -- Author = carlesturo

local MDO_SpriteData = require("MDO_SpriteData")
local MDO_Utils = require("MDO_Utils")
local IsoObjectUtilsFromStarlitLibrary = require("IsoObjectUtilsFromStarlitLibrary")

local MDO_Standpipe_Utils = {}

-- ------------------------------------------------------------------------------------------------

-- **************** MORE DAMAGED OBJECTS - UTILS - STANDPIPE ****************

local WaterJetAnimation = {}

function MDO_Standpipe_Utils.animateWaterJet(square, spriteData, isFirstActivation)
    if not square or not spriteData then return end

	local aboveSquare = IsoObjectUtilsFromStarlitLibrary.getOrCreateSquare(square:getX(), square:getY(), square:getZ() + 1)
    local above2Square = IsoObjectUtilsFromStarlitLibrary.getOrCreateSquare(square:getX(), square:getY(), square:getZ() + 2)

	if isFirstActivation then
		square:getModData().waterJetActive = true

		MDO_Utils.removeAllAndAddObjectInSquare(square, spriteData.baseSpriteStandpipe, spriteData.newSpriteStandpipe)

		local newSquare = IsoObjectUtilsFromStarlitLibrary.getOrCreateSquare(square:getX() + 1, square:getY(), square:getZ())
		MDO_Utils.addObjectToSquare(newSquare, spriteData.brokenSpriteStandpipe)

		local emitter = getWorld():getFreeEmitter(square:getX(), square:getY(), square:getZ())
		emitter:playSound("CrowbarBreak")
		emitter:setVolumeAll(1.0)
	end

	if not MDO_Utils.isWaterShutOff() then
		if spriteData.animationSprites then
			MDO_Utils.removeAllObjectsBySpriteList(square, spriteData.animationSprites)
			MDO_Utils.removeAllObjectsBySpriteList(aboveSquare, spriteData.aboveAnimationSprites)
			MDO_Utils.removeAllObjectsBySpriteList(above2Square, spriteData.above2AnimationSprites)
		end

		MDO_Utils.addObjectToSquare(square, spriteData.baseSpriteWaterJet)
		MDO_Utils.addObjectToSquare(aboveSquare, spriteData.baseSpriteAboveWaterJet)
		MDO_Utils.addObjectToSquare(above2Square, spriteData.baseSpriteAbove2WaterJet)

		local emitter = getWorld():getFreeEmitter(square:getX(), square:getY(), square:getZ())
		emitter:playSound("WaterJet")
		emitter:setVolumeAll(1.0)

		WaterJetAnimation[square] = {
			square = square,
			spriteData = spriteData,
			index = 1,
			tickCounter = 0,
			currentSprite = spriteData.baseSpriteWaterJet,
			animationSprites = spriteData.animationSprites,
			emitter = emitter,
		}

		WaterJetAnimation[aboveSquare] = {
			square = aboveSquare,
			spriteData = spriteData,
			index = 1,
			tickCounter = 0,
			currentSprite = spriteData.baseSpriteAboveWaterJet,
			animationSprites = spriteData.aboveAnimationSprites,
		}

		WaterJetAnimation[above2Square] = {
			square = above2Square,
			spriteData = spriteData,
			index = 1,
			tickCounter = 0,
			currentSprite = spriteData.baseSpriteAbove2WaterJet,
			animationSprites = spriteData.above2AnimationSprites,
		}
	end

	if MDO_Utils.isWaterShutOff() then
		MDO_Utils.removeAllObjectsBySpriteList(square, spriteData.animationSprites)
		MDO_Utils.removeAllObjectsBySpriteList(aboveSquare, spriteData.aboveAnimationSprites)
		MDO_Utils.removeAllObjectsBySpriteList(above2Square, spriteData.above2AnimationSprites)
	end
end

local waterWasOn = true

local function updateWaterJetAnimations()
	if not MDO_Utils.isWaterShutOff() then
		waterWasOn = true

		for square, data in pairs(WaterJetAnimation) do
			data.tickCounter = data.tickCounter + 1
			if data.tickCounter >= 30 then
				data.tickCounter = 0

				local spriteList = data.animationSprites or data.spriteData.animationSprites
				local newSprite = spriteList[data.index]

				MDO_Utils.replaceAllObjectsBySprite(data.square, data.currentSprite, newSprite)

				data.currentSprite = newSprite
				data.index = data.index + 1
				if data.index > #spriteList then
					data.index = 1
				end
			end
		end

	elseif waterWasOn and MDO_Utils.isWaterShutOff() then
		waterWasOn = false

		local toRemove = {}
		for square, data in pairs(WaterJetAnimation) do
			table.insert(toRemove, {
				square = square,
				aboveSquare = IsoObjectUtilsFromStarlitLibrary.getOrCreateSquare(square:getX(), square:getY(), square:getZ() + 1),
				above2Square = IsoObjectUtilsFromStarlitLibrary.getOrCreateSquare(square:getX(), square:getY(), square:getZ() + 2),
				spriteData = data.spriteData,
				emitter = data.emitter
			})
		end

		for _, entry in ipairs(toRemove) do
			if entry.spriteData then
				MDO_Utils.removeAllObjectsBySpriteList(entry.square, entry.spriteData.animationSprites or {})
				MDO_Utils.removeAllObjectsBySpriteList(entry.aboveSquare, entry.spriteData.aboveAnimationSprites or {})
				MDO_Utils.removeAllObjectsBySpriteList(entry.above2Square, entry.spriteData.above2AnimationSprites or {})
			end

			entry.square:getModData().waterJetActive = false

			if entry.emitter then
				entry.emitter:stopAll()
				entry.emitter = nil
			end

			WaterJetAnimation[entry.square] = nil
			WaterJetAnimation[entry.aboveSquare] = nil
			WaterJetAnimation[entry.above2Square] = nil
		end
	end
end

Events.OnTick.Add(updateWaterJetAnimations)

-- ------------------------------------------------------------------------------------------------

return MDO_Standpipe_Utils