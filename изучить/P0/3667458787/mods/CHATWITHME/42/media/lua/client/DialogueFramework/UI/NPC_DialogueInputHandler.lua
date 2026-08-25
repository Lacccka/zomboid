local NPC_DialogueInputHandler = {}

local activeDialogueUI = nil

function NPC_DialogueInputHandler.setActiveUI(ui)
    activeDialogueUI = ui
end

function NPC_DialogueInputHandler.clearActiveUI()
    activeDialogueUI = nil
end

function NPC_DialogueInputHandler.getActiveUI()
    return activeDialogueUI
end

function NPC_DialogueInputHandler.hasActiveUI()
    return activeDialogueUI ~= nil
end

local function onKeyPressed(key)
    if not activeDialogueUI then
        return
    end

    local uiState = activeDialogueUI:getUIState()

    if uiState == "npc_response" then
        if key == Keyboard.KEY_SPACE or key == Keyboard.KEY_RETURN or key == Keyboard.KEY_E then
            activeDialogueUI:skipNPCResponse()
        elseif key == Keyboard.KEY_ESCAPE then
            activeDialogueUI:cancelDialogue()
        end
    elseif uiState == "standard" then
        if key == Keyboard.KEY_UP then
            activeDialogueUI:selectPreviousOption()
        elseif key == Keyboard.KEY_DOWN then
            activeDialogueUI:selectNextOption()
        elseif key == Keyboard.KEY_RETURN or key == Keyboard.KEY_E then
            activeDialogueUI:confirmSelection()
        elseif key == Keyboard.KEY_ESCAPE or key == Keyboard.KEY_SPACE or key == Keyboard.KEY_LSHIFT or key == Keyboard.KEY_RSHIFT then
            activeDialogueUI:cancelDialogue()
        end
    elseif uiState == "fading_out" or uiState == "fading_in" then
        if key == Keyboard.KEY_ESCAPE then
            activeDialogueUI:cancelDialogue()
        end
    end
end

Events.OnKeyPressed.Add(onKeyPressed)

local function onResolutionChange(oldw, oldh, neww, newh)
    if not activeDialogueUI then
        return
    end

    if activeDialogueUI.onResolutionChange then
        activeDialogueUI:onResolutionChange(oldw, oldh, neww, newh)
    end
end

Events.OnResolutionChange.Add(onResolutionChange)

return NPC_DialogueInputHandler
