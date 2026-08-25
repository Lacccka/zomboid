pcall(require, "ISUI/ISContextMenu")
pcall(require, "ISUI/ISRadialMenu")
pcall(require, "Vehicles/ISUI/ISVehicleMenu")

LCCRussianTextRuntime = LCCRussianTextRuntime or {}
local state = LCCRussianTextRuntime

local exactTextKeys = {
    ["Ignite Flare"] = "ContextMenu_LCC_Explosives_IgniteFlare",
    ["Remove Rust"] = "ContextMenu_LCC_PZK_RemoveRust",
    ["Force Attach Hauler"] = "UI_LCC_SurvivalsHauler_ForceAttach",
    ["No Vehicle To Load"] = "UI_LCC_SurvivalsHauler_NoVehicle",
    ["Hauler Full"] = "UI_LCC_SurvivalsHauler_Full",
}

local function translatedText(key, ...)
    if not getText then
        return key
    end
    local value = getText(key, ...)
    if not value or value == "" then
        return key
    end
    return value
end

local function translateMenuText(text)
    if type(text) ~= "string" then
        return text
    end

    local exactKey = exactTextKeys[text]
    if exactKey then
        return translatedText(exactKey)
    end

    local vehicleName, slot = string.match(text, "^Load (.+) %[(%d+)%]$")
    if vehicleName and slot then
        if vehicleName == "Vehicle" then
            vehicleName = translatedText("UI_LCC_SurvivalsHauler_Vehicle")
        end
        return translatedText("UI_LCC_SurvivalsHauler_Load", vehicleName, slot)
    end

    local unloadSlot, unloadName = string.match(text, "^Unload (%d+): (.+)$")
    if unloadSlot and unloadName then
        if unloadName == "Vehicle" then
            unloadName = translatedText("UI_LCC_SurvivalsHauler_Vehicle")
        end
        return translatedText("UI_LCC_SurvivalsHauler_Unload", unloadSlot, unloadName)
    end

    return text
end

local function installContextMenuBridge()
    if not ISContextMenu or not ISContextMenu.addOption then
        return
    end
    if ISContextMenu.addOption == state.contextMenuWrapper then
        return
    end

    local previous = ISContextMenu.addOption
    local wrapper = function(self, name, ...)
        return previous(self, translateMenuText(name), ...)
    end
    state.contextMenuWrapper = wrapper
    ISContextMenu.addOption = wrapper
end

local function installRadialMenuBridge()
    if not ISRadialMenu or not ISRadialMenu.addSlice then
        return
    end
    if ISRadialMenu.addSlice == state.radialMenuWrapper then
        return
    end

    local previous = ISRadialMenu.addSlice
    local wrapper = function(self, name, ...)
        return previous(self, translateMenuText(name), ...)
    end
    state.radialMenuWrapper = wrapper
    ISRadialMenu.addSlice = wrapper
end

local function translateRustTooltip(context)
    local options = context and context.options
    if type(options) ~= "table" then
        return
    end

    local oldDescription = "Use Rust Solvent and Ripped Sheets to remove rust from this vehicle."
    for _, option in pairs(options) do
        local toolTip = option and option.toolTip
        if toolTip and toolTip.description == oldDescription then
            toolTip.description = translatedText("Tooltip_LCC_PZK_RemoveRust")
        end
    end
end

local function installVehicleMenuBridge()
    if not ISVehicleMenu or not ISVehicleMenu.FillPartMenu then
        return
    end
    if ISVehicleMenu.FillPartMenu == state.vehicleMenuWrapper then
        return
    end

    local previous = ISVehicleMenu.FillPartMenu
    local wrapper = function(playerIndex, context, slice, vehicle)
        local result = previous(playerIndex, context, slice, vehicle)
        translateRustTooltip(context)
        return result
    end
    state.vehicleMenuWrapper = wrapper
    ISVehicleMenu.FillPartMenu = wrapper
end

local function installAll()
    installContextMenuBridge()
    installRadialMenuBridge()
    installVehicleMenuBridge()
end

installAll()
Events.OnGameStart.Add(installAll)
Events.OnCreatePlayer.Add(installAll)
