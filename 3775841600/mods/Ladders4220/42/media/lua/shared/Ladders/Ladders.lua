--[[
	Make ladder sprites use Project Zomboid's sheet-rope climbing system.

	A ladder is climbed in the square containing the ladder sprite. At the top,
	the player needs a climbSheetTop* flag in that same square and a hoppable
	boundary between the ladder and the landing. West/north boundaries belong to
	the ladder square; east/south boundaries belong to the adjacent landing
	square, so those directions use a second invisible helper object.
--]]

local Ladders = {}

-- Custom animation variables are not part of the normal player packet. Build
-- 42 only replicates them after they have been added to this sync list.
Ladders.climbAnimationVariable = "ClimbLadder"
addVariableToSyncList(Ladders.climbAnimationVariable)

-- Keep the original names and IDs for save compatibility. The new IDs extend
-- the same private range that this mod has historically used.
Ladders.idW = 26476542
Ladders.idN = 26476543
Ladders.idE = 26476541
Ladders.idS = 26476540
Ladders.idBarrierE = 26476539
Ladders.idBarrierS = 26476538

Ladders.climbSheetTopW = "TopOfLadderW"
Ladders.climbSheetTopN = "TopOfLadderN"
Ladders.climbSheetTopE = "TopOfLadderE"
Ladders.climbSheetTopS = "TopOfLadderS"
Ladders.barrierE = "TopOfLadderBarrierE"
Ladders.barrierS = "TopOfLadderBarrierS"

Ladders.directionOrder = { "W", "N", "E", "S" }
Ladders.directions = {
	W = {
		climbFlag = IsoFlagType.climbSheetW,
		topFlag = IsoFlagType.climbSheetTopW,
		topSprite = Ladders.climbSheetTopW,
		spriteID = Ladders.idW,
		wallFlag = IsoFlagType.WallW,
		barrierEdge = "W",
		barrierDX = 0,
		barrierDY = 0,
	},
	N = {
		climbFlag = IsoFlagType.climbSheetN,
		topFlag = IsoFlagType.climbSheetTopN,
		topSprite = Ladders.climbSheetTopN,
		spriteID = Ladders.idN,
		wallFlag = IsoFlagType.WallN,
		barrierEdge = "N",
		barrierDX = 0,
		barrierDY = 0,
	},
	E = {
		climbFlag = IsoFlagType.climbSheetE,
		topFlag = IsoFlagType.climbSheetTopE,
		topSprite = Ladders.climbSheetTopE,
		spriteID = Ladders.idE,
		wallFlag = IsoFlagType.WallW,
		barrierEdge = "W",
		barrierDX = 1,
		barrierDY = 0,
		barrierSprite = Ladders.barrierE,
		barrierSpriteID = Ladders.idBarrierE,
	},
	S = {
		climbFlag = IsoFlagType.climbSheetS,
		topFlag = IsoFlagType.climbSheetTopS,
		topSprite = Ladders.climbSheetTopS,
		spriteID = Ladders.idS,
		wallFlag = IsoFlagType.WallN,
		barrierEdge = "N",
		barrierDX = 0,
		barrierDY = 1,
		barrierSprite = Ladders.barrierS,
		barrierSpriteID = Ladders.idBarrierS,
	},
}

local function normalizeDirection(direction)
	-- Preserve the old public API: true meant north and false meant west.
	if direction == true then return "N" end
	if direction == false then return "W" end
	if Ladders.directions[direction] then return direction end
	return nil
end

local function getObjectBySpriteName(square, spriteName)
	if not square then return nil end
	local objects = square:getObjects()
	for i = 0, objects:size() - 1 do
		local object = objects:get(i)
		if object:getTextureName() == spriteName then
			return object
		end
	end
	return nil
end

local function removeObjectsBySpriteName(square, spriteName)
	if not square then return false end
	local removed = false
	local objects = square:getObjects()
	for i = objects:size() - 1, 0, -1 do
		local object = objects:get(i)
		if object:getTextureName() == spriteName then
			square:transmitRemoveItemFromSquare(object)
			removed = true
		end
	end
	return removed
end

local function getBarrierSquare(square, direction)
	if not square or not direction then return nil end
	if direction.barrierDX == 0 and direction.barrierDY == 0 then
		return square
	end
	return getSquare(
		square:getX() + direction.barrierDX,
		square:getY() + direction.barrierDY,
		square:getZ()
	)
end

local function hasBlockingWall(square, direction)
	local barrierSquare = getBarrierSquare(square, direction)
	if not barrierSquare then return true end
	local props = barrierSquare:getProperties()
	return props:has(direction.wallFlag) or props:has(IsoFlagType.WallNW)
end

---@return IsoObject topOfLadder
function Ladders.getTopOfLadder(square, direction)
	local directionKey = normalizeDirection(direction)
	if directionKey then
		return getObjectBySpriteName(square, Ladders.directions[directionKey].topSprite)
	end
	for _, key in ipairs(Ladders.directionOrder) do
		local object = getObjectBySpriteName(square, Ladders.directions[key].topSprite)
		if object then return object end
	end
	return nil
end

local function ensureBarrier(square, direction)
	if not direction.barrierSprite then return true end
	local barrierSquare = getBarrierSquare(square, direction)
	if not barrierSquare then return false end
	if getObjectBySpriteName(barrierSquare, direction.barrierSprite) then return true end

	local object = IsoObject.new(getCell(), barrierSquare, direction.barrierSprite)
	barrierSquare:transmitAddObjectToSquare(object, -1)
	return true
end

---@return IsoObject topOfLadder
function Ladders.addTopOfLadder(square, direction)
	local directionKey = normalizeDirection(direction)
	local directionData = directionKey and Ladders.directions[directionKey]
	if not square or not directionData then return nil end

	if hasBlockingWall(square, directionData) then
		Ladders.removeTopOfLadder(square, directionKey)
		return nil
	end

	local object = Ladders.getTopOfLadder(square, directionKey)
	if object then
		ensureBarrier(square, directionData)
		return object
	end

	-- Do not replace a real sheet rope or another mod's climbing object.
	if square:getProperties():has(directionData.topFlag) then return nil end

	object = IsoObject.new(getCell(), square, directionData.topSprite)
	square:transmitAddObjectToSquare(object, -1)
	ensureBarrier(square, directionData)
	return object
end

function Ladders.removeTopOfLadder(square, direction)
	if not square then return false end
	local directionKey = normalizeDirection(direction)
	local removed = false

	for _, key in ipairs(Ladders.directionOrder) do
		if not directionKey or key == directionKey then
			local directionData = Ladders.directions[key]
			if removeObjectsBySpriteName(square, directionData.topSprite) then
				removed = true
			end
			if directionData.barrierSprite then
				local barrierSquare = getBarrierSquare(square, directionData)
				if removeObjectsBySpriteName(barrierSquare, directionData.barrierSprite) then
					removed = true
				end
			end
		end
	end

	return removed
end

-- E/S top flags live west/north of their paired barrier. This helper is used
-- when a supporting object is destroyed from the landing side.
function Ladders.removeTopOfLadderAround(square)
	if not square then return false end
	local removed = Ladders.removeTopOfLadder(square)

	local westSquare = getSquare(square:getX() - 1, square:getY(), square:getZ())
	if westSquare and Ladders.getTopOfLadder(westSquare, "E") then
		removed = Ladders.removeTopOfLadder(westSquare, "E") or removed
	end

	local northSquare = getSquare(square:getX(), square:getY() - 1, square:getZ())
	if northSquare and Ladders.getTopOfLadder(northSquare, "S") then
		removed = Ladders.removeTopOfLadder(northSquare, "S") or removed
	end

	return removed
end

function Ladders.makeLadderClimbable(square, direction)
	local directionKey = normalizeDirection(direction)
	local flags = directionKey and Ladders.directions[directionKey]
	if not square or not flags then return end

	local x, y, z = square:getX(), square:getY(), square:getZ()
	local topSquare = square
	local topObject

	while true do
		topObject = topSquare:has(flags.topFlag) and Ladders.getTopOfLadder(topSquare, directionKey)
		z = z + 1
		local aboveSquare = getSquare(x, y, z)
		if not aboveSquare or aboveSquare:TreatAsSolidFloor() or aboveSquare:has("RoofGroup") then break end

		if aboveSquare:has(flags.climbFlag) then
			if topObject then Ladders.removeTopOfLadder(topSquare, directionKey) end
			topSquare = aboveSquare
		elseif not hasBlockingWall(aboveSquare, flags) then
			if topObject then Ladders.removeTopOfLadder(topSquare, directionKey) end
			topSquare = aboveSquare
			break
		else
			Ladders.removeTopOfLadder(aboveSquare, directionKey)
			break
		end
	end

	topObject = Ladders.addTopOfLadder(topSquare, directionKey)
	Ladders.chooseAnimVar(topSquare, topObject)
end

function Ladders.makeLadderClimbableFromTop(square)
	if not square then return end
	local x, y, z = square:getX(), square:getY(), square:getZ() - 1

	Ladders.makeLadderClimbableFromBottom(getSquare(x - 1, y, z))
	Ladders.makeLadderClimbableFromBottom(getSquare(x + 1, y, z))
	Ladders.makeLadderClimbableFromBottom(getSquare(x, y - 1, z))
	Ladders.makeLadderClimbableFromBottom(getSquare(x, y + 1, z))
end

function Ladders.makeLadderClimbableFromBottom(square)
	if not square then return end
	local props = square:getProperties()

	for _, key in ipairs(Ladders.directionOrder) do
		if props:has(Ladders.directions[key].climbFlag) then
			Ladders.makeLadderClimbable(square, key)
			return
		end
	end
end

local function directionBetween(fromSquare, toSquare)
	if not fromSquare or not toSquare then return nil end
	if toSquare:getX() < fromSquare:getX() then return IsoDirections.W end
	if toSquare:getX() > fromSquare:getX() then return IsoDirections.E end
	if toSquare:getY() < fromSquare:getY() then return IsoDirections.N end
	if toSquare:getY() > fromSquare:getY() then return IsoDirections.S end
	return nil
end

-- Start the same climb used by the controller prompt. Descending needs to
-- cross the landing boundary before the sheet-rope state can take over.
function Ladders.triggerClimbing(player, playerNum, square, down, direction)
	local location = player and player:getSquare()
	if not player or not location or not square then return false end
	if MainScreen and MainScreen.instance and MainScreen.instance:isVisible() then return false end

	if down then
		local window = location:getWindowTo(square)
		local thumpable = location:getWindowThumpableTo(square)

		if window or thumpable then
			if window and not window:IsOpen() then
				window:ToggleWindow(player)
			end

			-- The integer argument is part of the vanilla climb-through API.
			if window and window:canClimbThrough(player) then
				player:climbThroughWindow(window, 4)
			elseif thumpable and thumpable:canClimbThrough(player) then
				player:climbThroughWindow(thumpable, 4)
			else
				player:climbDownSheetRope()
			end
			return true
		end

		local hoppable = location:getHoppableThumpableTo(square)
		local wall = location:getWallHoppableTo(square)
		if hoppable or wall then
			direction = direction or directionBetween(location, square) or player:getDir()
			local north = direction == IsoDirections.N or direction == IsoDirections.S
			if IsoWindow.canClimbThroughHelper(player, location, square, north) then
				player:climbOverFence(direction)
			else
				player:climbDownSheetRope()
			end
			return true
		end

		local frame = location:getWindowFrameTo(square)
		if frame then
			if IsoWindowFrame.canClimbThrough(frame, player) then
				player:climbThroughWindowFrame(frame)
			else
				player:climbDownSheetRope()
			end
			return true
		end

		-- Preserve the original fallback for unusual map geometry.
		player:setX(square:getX())
		player:setY(square:getY())
		player:setZ(square:getZ())
		player:climbDownSheetRope()
		return true
	end

	Ladders.enRoute = true
	ISWorldObjectContextMenu.onClimbSheetRope(nil, square, false, playerNum or 0)
	return true
end

-- Use the configured interaction key so vanilla key rebinding is respected.
function Ladders.OnKeyPressed(key)
	if not getCore():isKey("Interact", key) then return end
	local player = getPlayer()
	if not player or player:isDead() then return end
	if MainScreen and MainScreen.instance and MainScreen.instance:isVisible() then return end

	-- Used when selecting the ladder animation.
	Ladders.player = player
	local square = player:getSquare()
	Ladders.makeLadderClimbableFromTop(square)
	Ladders.makeLadderClimbableFromBottom(square)
end

Events.OnKeyPressed.Add(Ladders.OnKeyPressed)

-- Remove synthetic top objects only after the authoritative timed action has
-- completed. In 42.20, world mutation occurs in complete(), not perform().
local function getObjectLadderDirections(object)
	local result = {}
	if not object or not object:getProperties() then return result end
	local props = object:getProperties()
	for _, key in ipairs(Ladders.directionOrder) do
		local directionData = Ladders.directions[key]
		if props:has(directionData.climbFlag) or props:has("ladder" .. key) then
			result[#result + 1] = key
		end
	end
	return result
end

local function removeLadderTopsAbove(square, directions)
	if not square or #directions == 0 then return end
	-- A ladder top is normally one or more levels above the removed segment,
	-- but ladders ending at a floor opening can carry it on the same square.
	for z = square:getZ(), square:getZ() + 31 do
		local aboveSquare = getSquare(square:getX(), square:getY(), z)
		if not aboveSquare then return end

		local found = false
		for _, key in ipairs(directions) do
			if Ladders.getTopOfLadder(aboveSquare, key) then
				Ladders.removeTopOfLadder(aboveSquare, key)
				found = true
			end
		end
		if found then return end
	end
end

require "Moveables/ISMoveablesAction"
Ladders.ISMoveablesAction = {
	complete = ISMoveablesAction.complete,
}

function ISMoveablesAction:complete()
	local square = self.square
	local directions = self.mode == "pickup" and getObjectLadderDirections(self.object) or {}
	local completed = Ladders.ISMoveablesAction.complete(self)
	if completed and square then
		removeLadderTopsAbove(square, directions)
	end
	return completed
end

require "TimedActions/ISDestroyStuffAction"
Ladders.ISDestroyStuffAction = {
	complete = ISDestroyStuffAction.complete,
}

function ISDestroyStuffAction:complete()
	local square = self.item and self.item:getSquare()
	local directions = getObjectLadderDirections(self.item)
	local hadSheetRope = self.item and self.item:haveSheetRope()
	local completed = Ladders.ISDestroyStuffAction.complete(self)

	if completed and square then
		removeLadderTopsAbove(square, directions)
		if hadSheetRope then
			Ladders.removeTopOfLadderAround(square)
		end
	end
	return completed
end

-- Animation selection -------------------------------------------------------

function Ladders.chooseAnimVar(square, topObject)
	if not Ladders.player then return end
	local doLadderAnim = topObject ~= nil

	if doLadderAnim then
		local objects = square:getObjects()
		for i = 0, objects:size() - 1 do
			local sprite = objects:get(i):getTextureName()
			if Ladders.excludeAnimTiles[sprite] then
				doLadderAnim = false
				break
			end
		end
	end

	if doLadderAnim then
		Ladders.player:setVariable(Ladders.climbAnimationVariable, true)
	else
		-- clearVariable() is local-only; setting false also informs other clients.
		Ladders.player:setVariable(Ladders.climbAnimationVariable, false)
	end
end

-- Vanilla and popular mod ladder sprites. The four new 42.20-facing variants
-- are tagged ladderE/ladderS by the game but are not made climbable by vanilla.
Ladders.westLadderTiles = {
	"advertising_01_6", "carpentry_02_84", "industry_02_86", "location_sewer_01_32",
	"industry_railroad_05_20", "industry_railroad_05_36", "walls_commercial_03_0",
	"edit_ddd_RUS_decor_house_01_16", "edit_ddd_RUS_decor_house_01_19",
	"edit_ddd_RUS_industry_crane_01_72", "edit_ddd_RUS_industry_crane_01_73",
	"rus_industry_crane_ddd_01_24", "rus_industry_crane_ddd_01_25",
	"A1 Wall_48", "A1 Wall_80", "A1_CULT_36", "aaa_RC_6", "trelai_tiles_01_30",
	"trelai_tiles_01_38", "industry_crane_rus_72", "industry_crane_rus_73",
}

Ladders.northLadderTiles = {
	"advertising_01_14", "carpentry_02_85", "location_sewer_01_33",
	"industry_railroad_05_21", "industry_railroad_05_37",
	"edit_ddd_RUS_decor_house_01_17", "edit_ddd_RUS_decor_house_01_18",
	"edit_ddd_RUS_industry_crane_01_76", "edit_ddd_RUS_industry_crane_01_77",
	"A1 Wall_49", "A1 Wall_81", "A1_CULT_37", "aaa_RC_14", "trelai_tiles_01_31",
	"trelai_tiles_01_39", "industry_crane_rus_76", "industry_crane_rus_77",
}

Ladders.eastLadderTiles = {
	"carpentry_02_86", "industry_railroad_05_56", "industry_railroad_05_58",
	"location_sewer_01_48",
}

Ladders.southLadderTiles = {
	"carpentry_02_87", "industry_railroad_05_57", "industry_railroad_05_59",
	"location_sewer_01_49",
}

for index = 1, 62 do
	local name = "basement_objects_02_" .. index
	if index % 2 == 0 then
		Ladders.westLadderTiles[#Ladders.westLadderTiles + 1] = name
	else
		Ladders.northLadderTiles[#Ladders.northLadderTiles + 1] = name
	end
end

Ladders.holeTiles = {
	"floors_interior_carpet_01_24",
}

Ladders.poleTiles = {
	"recreational_sports_01_32", "recreational_sports_01_33",
}

Ladders.excludeAnimTiles = {}
for _, name in ipairs(Ladders.poleTiles) do
	Ladders.excludeAnimTiles[name] = true
end

local function setSpriteFlag(manager, name, flag)
	local sprite = manager:getSprite(name)
	if sprite then
		sprite:getProperties():set(flag)
	end
end

local function configureBarrierProperties(props, edge)
	if edge == "W" then
		props:set(IsoFlagType.collideW)
		props:set(IsoFlagType.transparentW)
		props:set(IsoFlagType.cutW)
		props:set(IsoFlagType.HoppableW)
		props:set(IsoFlagType.canPathW)
		props:set(IsoFlagType.WallWTrans)
	else
		props:set(IsoFlagType.collideN)
		props:set(IsoFlagType.transparentN)
		props:set(IsoFlagType.cutN)
		props:set(IsoFlagType.HoppableN)
		props:set(IsoFlagType.canPathN)
		props:set(IsoFlagType.WallNTrans)
	end
end

local function addHelperSprite(manager, name, id, topFlag, barrierEdge)
	local sprite = manager:AddSprite(name, id)
	sprite:setName(name)
	local props = sprite:getProperties()
	if topFlag then props:set(topFlag) end
	if barrierEdge then configureBarrierProperties(props, barrierEdge) end
	props:set(IsoFlagType.EntityScript)
	props:CreateKeySet()
end

function Ladders.setLadderClimbingFlags(manager)
	for _, name in ipairs(Ladders.westLadderTiles) do
		setSpriteFlag(manager, name, IsoFlagType.climbSheetW)
	end
	for _, name in ipairs(Ladders.northLadderTiles) do
		setSpriteFlag(manager, name, IsoFlagType.climbSheetN)
	end
	for _, name in ipairs(Ladders.eastLadderTiles) do
		setSpriteFlag(manager, name, IsoFlagType.climbSheetE)
	end
	for _, name in ipairs(Ladders.southLadderTiles) do
		setSpriteFlag(manager, name, IsoFlagType.climbSheetS)
	end

	for _, name in ipairs(Ladders.holeTiles) do
		local sprite = manager:getSprite(name)
		if sprite then
			local properties = sprite:getProperties()
			properties:set(IsoFlagType.climbSheetTopW)
			properties:set(IsoFlagType.HoppableW)
			properties:unset(IsoFlagType.solidfloor)
		end
	end

	for _, name in ipairs(Ladders.poleTiles) do
		setSpriteFlag(manager, name, IsoFlagType.climbSheetW)
	end

	-- W/N helpers carry both the top flag and the boundary. E/S helpers need
	-- separate logical-top and landing-boundary sprites.
	addHelperSprite(manager, Ladders.climbSheetTopW, Ladders.idW, IsoFlagType.climbSheetTopW, "W")
	addHelperSprite(manager, Ladders.climbSheetTopN, Ladders.idN, IsoFlagType.climbSheetTopN, "N")
	addHelperSprite(manager, Ladders.climbSheetTopE, Ladders.idE, IsoFlagType.climbSheetTopE, nil)
	addHelperSprite(manager, Ladders.climbSheetTopS, Ladders.idS, IsoFlagType.climbSheetTopS, nil)
	addHelperSprite(manager, Ladders.barrierE, Ladders.idBarrierE, nil, "W")
	addHelperSprite(manager, Ladders.barrierS, Ladders.idBarrierS, nil, "N")
end

Events.OnLoadedTileDefinitions.Add(Ladders.setLadderClimbingFlags)

return Ladders
