-- Gamepad support via the context-sensitive climb-through binding.

local Ladders = require "Ladders/Ladders"

local GamePad = {}
GamePad.testedSq = {}

local function getPromptPlayer(buttonPromptData)
	if not buttonPromptData then return nil end
	local player = getSpecificPlayer(buttonPromptData.player or 0)
	if not player or not player:isAlive() then return nil end
	return player
end

-- This callback is invoked as a method of ISButtonPrompt, so the prompt object
-- is the first argument and our values begin with square.
function GamePad.triggerGamepadClimbing(buttonPromptData, square, down, direction)
	local player = getPromptPlayer(buttonPromptData)
	if not player then return end
	Ladders.triggerClimbing(
		player,
		buttonPromptData.player or 0,
		square,
		down,
		direction
	)
end

local function hasClimbFlag(square, direction)
	if not square then return false end
	local props = square:getProperties()
	return (direction == IsoDirections.W and props:has(IsoFlagType.climbSheetW))
		or (direction == IsoDirections.N and props:has(IsoFlagType.climbSheetN))
		or (direction == IsoDirections.E and props:has(IsoFlagType.climbSheetE))
		or (direction == IsoDirections.S and props:has(IsoFlagType.climbSheetS))
end

local function prepareLadder(buttonPromptData, player)
	local square = player:getSquare()
	if not square then return nil end
	if GamePad.testedSq[buttonPromptData] == square then return square end

	GamePad.testedSq[buttonPromptData] = square
	Ladders.player = player
	Ladders.makeLadderClimbableFromTop(square)
	Ladders.makeLadderClimbableFromBottom(square)
	return square
end

local function setLadderPrompt(buttonPromptData, direction, useBinding)
	local player = getPromptPlayer(buttonPromptData)
	if not player then return false end
	local square = prepareLadder(buttonPromptData, player)
	if not square then return false end

	if hasClimbFlag(square, direction) and player:canClimbSheetRope(square) then
		if useBinding then
			buttonPromptData:setButtonBindingPrompt(
				CharacterJoypadButtonBinding.ClimbThrough,
				getText("UI_Ladders_Climb"),
				GamePad.triggerGamepadClimbing,
				square,
				false,
				direction
			)
		else
			buttonPromptData:setBPrompt(
				getText("UI_Ladders_Climb"),
				GamePad.triggerGamepadClimbing,
				square,
				false,
				direction
			)
		end
		return true
	end

	local adjacentSquare = square:getAdjacentSquare(direction)
	if adjacentSquare and IsoWindow.isTopOfSheetRopeHere(adjacentSquare)
			and player:canClimbDownSheetRope(adjacentSquare) then
		if useBinding then
			buttonPromptData:setButtonBindingPrompt(
				CharacterJoypadButtonBinding.ClimbThrough,
				getText("UI_Ladders_Climb"),
				GamePad.triggerGamepadClimbing,
				adjacentSquare,
				true,
				direction
			)
		else
			buttonPromptData:setBPrompt(
				getText("UI_Ladders_Climb"),
				GamePad.triggerGamepadClimbing,
				adjacentSquare,
				true,
				direction
			)
		end
		return true
	end

	return false
end

local function getPromptForButton(buttonPromptData, button)
	if button == JoypadButton.A then return buttonPromptData.aPrompt end
	if button == JoypadButton.B then return buttonPromptData.bPrompt end
	if button == JoypadButton.X then return buttonPromptData.xPrompt end
	if button == JoypadButton.Y then return buttonPromptData.yPrompt end
	if button == JoypadButton.LeftBump then return buttonPromptData.lbPrompt end
	if button == JoypadButton.RightBump then return buttonPromptData.rbPrompt end
	return nil
end

local function hasClimbBindingPrompt(buttonPromptData)
	local binding = CharacterJoypadButtonBinding and CharacterJoypadButtonBinding.ClimbThrough
	if not binding then return buttonPromptData.bPrompt ~= nil end
	return getPromptForButton(buttonPromptData, binding:getJoypadButton()) ~= nil
end

function GamePad.patchBestClimbAction()
	if GamePad.patched or not ISButtonPrompt then return end

	-- Build 42.19+ routes controller actions through remappable bindings.
	if ISButtonPrompt.testClimbThroughButtonAction then
		GamePad.originalTestClimbThroughButtonAction = ISButtonPrompt.testClimbThroughButtonAction
		ISButtonPrompt.testClimbThroughButtonAction = function(self, direction)
			if hasClimbBindingPrompt(self) then return end
			if setLadderPrompt(self, direction, true) then return end
			return GamePad.originalTestClimbThroughButtonAction(self, direction)
		end
		GamePad.patched = true
		return
	end

	-- Compatibility with the pre-remapping Build 42 controller prompt.
	if ISButtonPrompt.testBButtonAction then
		GamePad.originalTestBButtonAction = ISButtonPrompt.testBButtonAction
		ISButtonPrompt.testBButtonAction = function(self, direction)
			if not self.bPrompt and setLadderPrompt(self, direction, false) then return end
			return GamePad.originalTestBButtonAction(self, direction)
		end
		GamePad.patched = true
	end
end

function GamePad.OnObjectAdded()
	table.wipe(GamePad.testedSq)
end

Events.OnGameStart.Add(GamePad.patchBestClimbAction)
Events.OnObjectAdded.Add(GamePad.OnObjectAdded)

-- Hide the progress bar while walking to a ladder.
require "TimedActions/ISBaseTimedAction"
GamePad.ISBaseTimedAction = {
	create = ISBaseTimedAction.create,
}

function ISBaseTimedAction:create()
	local result = GamePad.ISBaseTimedAction.create(self)
	if Ladders.enRoute then
		self.action:setUseProgressBar(false)
		Ladders.enRoute = nil
	end
	return result
end

return GamePad
