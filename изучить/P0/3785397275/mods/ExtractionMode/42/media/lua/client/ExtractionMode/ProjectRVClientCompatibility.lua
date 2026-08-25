require "ExtractionMode/ModCompatibility"

ExtractionMode = ExtractionMode or {}
local Compatibility = ExtractionMode.ModCompatibility

local function install()
    if not Compatibility.isProjectRVInteriorActive() then return end
    if type(CheckIfInRV) == "function"
        and ExtractionMode.ProjectRVOriginalCheckIfInRV == nil then
        ExtractionMode.ProjectRVOriginalCheckIfInRV = CheckIfInRV
        CheckIfInRV = function(player)
            return Compatibility.isPlayerInsideRVInterior(player)
        end
    end
    -- The single-player RV script keeps its coordinate check in a private local
    -- table, so it cannot be replaced directly. Guard the one distinctive menu
    -- option instead; this also remains correct regardless of mod load order.
    if ISContextMenu and ISContextMenu.addOption
        and not ISContextMenu.ExtractionModeOriginalAddOptionForRV then
        ISContextMenu.ExtractionModeOriginalAddOptionForRV = ISContextMenu.addOption
        function ISContextMenu:addOption(name, target, callback, ...)
            local exitLabel = getText and getText("ContextMenu_GetOutFromRV") or nil
            if exitLabel ~= nil and tostring(name) == tostring(exitLabel)
                and not Compatibility.isPlayerInsideRVInterior(target) then return nil end
            return self:ExtractionModeOriginalAddOptionForRV(name, target, callback, ...)
        end
    end
end

install()
Events.OnGameStart.Add(install)
Events.OnFillWorldObjectContextMenu.Add(install)

ExtractionMode.ProjectRVClientCompatibility = { install = install }
return ExtractionMode.ProjectRVClientCompatibility
