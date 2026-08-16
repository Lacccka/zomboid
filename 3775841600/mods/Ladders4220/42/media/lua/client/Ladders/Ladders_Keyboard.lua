-- Keyboard bindings and a context-sensitive prompt for ladder climbing.

require "ISUI/ISUIElement"
require "ISUI/ISWorldObjectContextMenu"

local Ladders = require "Ladders/Ladders"

local PC = {}
PC.climbUpBinding = "LaddersClimbUp"
PC.climbDownBinding = "LaddersClimbDown"
PC.testedSquare = nil

function PC.initKeyBindings()
	local hasHeader = false
	local hasClimbUp = false
	local hasClimbDown = false

	for _, binding in ipairs(keyBinding) do
		hasHeader = hasHeader or binding.value == "[Ladders]"
		hasClimbUp = hasClimbUp or binding.value == PC.climbUpBinding
		hasClimbDown = hasClimbDown or binding.value == PC.climbDownBinding
	end

	if not hasHeader then
		table.insert(keyBinding, { value = "[Ladders]" })
	end
	if not hasClimbUp then
		table.insert(keyBinding, {
			value = PC.climbUpBinding,
			key = Keyboard.KEY_PRIOR,
		})
	end
	if not hasClimbDown then
		table.insert(keyBinding, {
			value = PC.climbDownBinding,
			key = Keyboard.KEY_NEXT,
		})
	end
end

local function canUseLadderControls(player)
	if not player or not player:isAlive() then return false end
	if isGamePaused() or player:getVehicle() or player:isAsleep() then return false end
	if player:getIgnoreMovement() or player:isClimbingRope() then return false end
	if player:hasTimedActions() then return false end
	if getCell() and getCell():getDrag(0) then return false end
	if MainScreen and MainScreen.instance and MainScreen.instance:isVisible() then return false end
	return true
end

local function isKeyboardAndMouseActive()
	local joypadData = getJoypadData and getJoypadData(0)
	return not joypadData or wasMouseActiveMoreRecentlyThanJoypad()
end

local function prepareLadder(player)
	local square = player:getSquare()
	if not square then return nil end
	if PC.testedSquare == square then return square end

	PC.testedSquare = square
	Ladders.player = player
	Ladders.makeLadderClimbableFromTop(square)
	Ladders.makeLadderClimbableFromBottom(square)
	return square
end

local function hasClimbFlag(square, direction)
	if not square then return false end
	local props = square:getProperties()
	return (direction == IsoDirections.W and props:has(IsoFlagType.climbSheetW))
		or (direction == IsoDirections.N and props:has(IsoFlagType.climbSheetN))
		or (direction == IsoDirections.E and props:has(IsoFlagType.climbSheetE))
		or (direction == IsoDirections.S and props:has(IsoFlagType.climbSheetS))
end

local function getFacingDirections(direction)
	if direction == IsoDirections.NE then
		return { IsoDirections.N, IsoDirections.E }
	elseif direction == IsoDirections.SE then
		return { IsoDirections.S, IsoDirections.E }
	elseif direction == IsoDirections.SW then
		return { IsoDirections.S, IsoDirections.W }
	elseif direction == IsoDirections.NW then
		return { IsoDirections.N, IsoDirections.W }
	elseif direction == IsoDirections.N or direction == IsoDirections.E
			or direction == IsoDirections.S or direction == IsoDirections.W then
		return { direction }
	end
	return {}
end

-- Returns the ladder square and the cardinal direction selected from the
-- player's facing direction. Up and down are deliberately resolved separately
-- so each configured binding can only start its named action.
function PC.getClimbAction(player, down)
	local square = prepareLadder(player)
	if not square then return nil end

	for _, direction in ipairs(getFacingDirections(player:getDir())) do
		if down then
			local adjacentSquare = square:getAdjacentSquare(direction)
			if adjacentSquare and IsoWindow.isTopOfSheetRopeHere(adjacentSquare)
					and player:canClimbDownSheetRope(adjacentSquare) then
				return adjacentSquare, direction
			end
		elseif hasClimbFlag(square, direction) and player:canClimbSheetRope(square) then
			return square, direction
		end
	end

	return nil
end

function PC.onKeyPressed(key)
	local climbUp = getCore():getKey(PC.climbUpBinding) ~= Keyboard.KEY_NONE
		and getCore():isKey(PC.climbUpBinding, key)
	local climbDown = getCore():getKey(PC.climbDownBinding) ~= Keyboard.KEY_NONE
		and getCore():isKey(PC.climbDownBinding, key)
	if not climbUp and not climbDown then return end

	local player = getSpecificPlayer(0)
	-- The vanilla movement keys can reverse direction on a rope. Give the
	-- dedicated ladder bindings the same behavior once a climb has started.
	if player and player:isClimbingRope() then
		local isClimbingUp = player:getCurrentState() == ClimbSheetRopeState.instance()
		if climbUp and not isClimbingUp then
			player:clear(ClimbSheetRopeState:instance())
			player:reportEvent("EventClimbRope")
		elseif climbDown and isClimbingUp then
			player:clear(ClimbDownSheetRopeState:instance())
			player:reportEvent("EventClimbDownRope")
		end
		return
	end
	if not canUseLadderControls(player) then return end

	local down = climbDown and not climbUp
	local square, direction = PC.getClimbAction(player, down)
	if not square then return end

	Ladders.player = player
	Ladders.triggerClimbing(player, player:getPlayerNum(), square, down, direction)
end

local LadderKeyPrompt = ISUIElement:derive("LadderKeyPrompt")
local PROMPT_FONT = UIFont.Medium

local function getLuaKeyBinding(name)
	if not MainOptions then return nil end

	-- Core:getKeyBinding() returns a Java record in B42.20, but that record's
	-- accessors are not exposed to Lua. MainOptions.keyText is the game's own
	-- Lua-side source for the current key and modifier values.
	for _, binding in ipairs(MainOptions.keyText or {}) do
		if not binding.value and binding.txt and binding.txt:getName() == name then
			return binding
		end
	end

	-- keyText is populated when the options UI is created. Keep the initially
	-- loaded values available as a fallback during startup.
	for _, binding in ipairs(MainOptions.keys or {}) do
		if binding.value == name then return binding end
	end
	return nil
end

local function getBindingDisplayName(name)
	local binding = getLuaKeyBinding(name)
	local prefix = ""
	if binding then
		if binding.shift then prefix = prefix .. "SHIFT + " end
		if binding.ctrl then prefix = prefix .. "CTRL + " end
		if binding.alt then prefix = prefix .. "ALT + " end
	end
	return prefix .. getKeyName(getCore():getKey(name))
end

function LadderKeyPrompt:update()
	self.rows = {}
	local player = getSpecificPlayer(0)
	if not isKeyboardAndMouseActive() or not canUseLadderControls(player) then return end

	local upSquare = PC.getClimbAction(player, false)
	if upSquare then
		self.rows[#self.rows + 1] = {
			binding = PC.climbUpBinding,
			label = getText("UI_Ladders_ClimbUp"),
		}
	end

	local downSquare = PC.getClimbAction(player, true)
	if downSquare then
		self.rows[#self.rows + 1] = {
			binding = PC.climbDownBinding,
			label = getText("UI_Ladders_ClimbDown"),
		}
	end

	if #self.rows == 0 then return end

	local textManager = getTextManager()
	local padding = 8
	local gap = 8
	local rowHeight = textManager:getFontHeight(PROMPT_FONT) + 10
	local maxRowWidth = 0

	for _, row in ipairs(self.rows) do
		row.keyName = getBindingDisplayName(row.binding)
		row.keyWidth = math.max(50, textManager:MeasureStringX(PROMPT_FONT, row.keyName) + 16)
		row.width = row.keyWidth + gap + textManager:MeasureStringX(PROMPT_FONT, row.label)
		maxRowWidth = math.max(maxRowWidth, row.width)
	end

	self:setWidth(maxRowWidth + padding * 2)
	self:setHeight(rowHeight * #self.rows + padding * 2)
	self:setX(getPlayerScreenLeft(0) + (getPlayerScreenWidth(0) - self.width) / 2)
	self:setY(getPlayerScreenTop(0) + getPlayerScreenHeight(0) - self.height - 92)
	self.rowHeight = rowHeight
	self.padding = padding
	self.gap = gap
end

function LadderKeyPrompt:prerender()
	if not self.rows or #self.rows == 0 then return end

	self:drawRect(0, 0, self.width, self.height, 0.72, 0.04, 0.04, 0.04)
	self:drawRectBorder(0, 0, self.width, self.height, 0.75, 0.45, 0.45, 0.45)

	local fontHeight = getTextManager():getFontHeight(PROMPT_FONT)
	for index, row in ipairs(self.rows) do
		local x = (self.width - row.width) / 2
		local y = self.padding + (index - 1) * self.rowHeight
		local keyY = y + (self.rowHeight - fontHeight - 6) / 2

		self:drawRect(x, keyY, row.keyWidth, fontHeight + 6, 0.92, 0.16, 0.16, 0.16)
		self:drawRectBorder(x, keyY, row.keyWidth, fontHeight + 6, 0.9, 0.72, 0.62, 0.48)
		self:drawTextCentre(row.keyName, x + row.keyWidth / 2, keyY + 3, 1, 1, 1, 1, PROMPT_FONT)
		self:drawText(row.label, x + row.keyWidth + self.gap, y + 5, 1, 1, 1, 1, PROMPT_FONT)
	end
end

function LadderKeyPrompt:new()
	local o = ISUIElement.new(self, 0, 0, 1, 1)
	o.rows = {}
	o:setWantMouseEvents(false)
	return o
end

function PC.onGameStart()
	if PC.prompt and not PC.prompt:isRemoved() then
		PC.prompt:removeFromUIManager()
	end

	PC.prompt = LadderKeyPrompt:new()
	PC.prompt:initialise()
	PC.prompt:addToUIManager()
	PC.prompt:setAlwaysOnTop(true)
end

function PC.onObjectAdded()
	PC.testedSquare = nil
end

Events.OnGameBoot.Add(PC.initKeyBindings)
Events.OnGameStart.Add(PC.onGameStart)
Events.OnKeyPressed.Add(PC.onKeyPressed)
Events.OnObjectAdded.Add(PC.onObjectAdded)

return PC
