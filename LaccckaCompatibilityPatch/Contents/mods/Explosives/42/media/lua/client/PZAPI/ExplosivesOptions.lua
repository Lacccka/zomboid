ExplosivesOptions = {}

local _options = PZAPI.ModOptions:create("ExplosivesMenuOptionsID", "US Military Grenades Options")

-- Default is Shift + Numpad "+" (the keybind itself is just Numpad "+";
-- Shift is checked separately in ExplosivesDebugSpawn.lua) -- an unlikely
-- combo to already be bound to something else.
ExplosivesOptions.debugSpawnKey = _options:addKeyBind("ExplosivesDebugSpawnKey", getText("UI_Options_Explosives_DEBUG_SPAWN_KEY"), Keyboard.KEY_ADD, getText("Tooltip_Explosives_DEBUG_SPAWN_KEY"))

Events.OnMainMenuEnter.Add(function()
    PZAPI.ModOptions:getOptions("ExplosivesMenuOptionsID"):apply()
end)
