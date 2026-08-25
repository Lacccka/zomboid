local MuggyCoreUtilities = require("MuggyMod/Muggy_CoreUtils")
local UIHooks = {}

--- Configures the animal UI panel with securitron-specific settings
--- @param uiInstance ISAnimalUI
function UIHooks.configureAnimalUIForMuggy(uiInstance)
    if not uiInstance or not uiInstance.animal then
        return
    end

    local currentAnimal = uiInstance.animal
    if MuggyCoreUtilities.currentAnimalMuggy(currentAnimal) then
        if uiInstance.avatarPanel and uiInstance.avatarPanel.setVariable then
            pcall(function()
                uiInstance.avatarPanel:setVariable("currentAnimalMuggy", true)
            end)
        end
    end
end

local originalCreateMethod = ISAnimalUI.create

---@diagnostic disable-next-line: duplicate-set-field
ISAnimalUI.create = function(selfInstance)
    local success = pcall(function()
        originalCreateMethod(selfInstance)
    end)

    if not success then
        print("[MuggyCoreHooks] ERROR: Failed to execute original ISAnimalUI.create")
        return
    end

    pcall(function()
        UIHooks.configureAnimalUIForMuggy(selfInstance)
    end)
end

print("[MuggyCoreHooks] Muggy UI hooks initialized with safe error handling")

return UIHooks