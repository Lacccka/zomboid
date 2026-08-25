local MuggyCoreUtilities = require("MuggyMod/Muggy_CoreUtils")

local MuggyInteractionHandler = {}


--- @param contextMenu ISContextMenu
--- @param targetPlayer IsoPlayer
--- @param targetAnimal IsoAnimal
function MuggyInteractionHandler.buildMuggyContextMenu(contextMenu, targetPlayer, targetAnimal)
    if not MuggyCoreUtilities.currentAnimalMuggy(targetAnimal) then
        return
    end
end

--- Event callback for animal context menu creation
--- @type Callback_OnClickedAnimalForContext
local function onAnimalContextMenuRequested(playerIndex, contextMenu, animalList, testMode)
    if not animalList or animalList[1] == nil then
        return
    end

    local targetPlayer = getSpecificPlayer(playerIndex)
    local primaryAnimal = animalList[1]

    MuggyInteractionHandler.buildMuggyContextMenu(contextMenu, targetPlayer, primaryAnimal)
end

Events.OnClickedAnimalForContext.Add(onAnimalContextMenuRequested)

return MuggyInteractionHandler