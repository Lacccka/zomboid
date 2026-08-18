ExplosivesOptions = {}

local _options = PZAPI.ModOptions:create("ExplosivesMenuOptionsID", "US Military Grenades Options")

-- Default is Numpad "+" (Shift is checked separately in ExplosivesDebugSpawn.lua) -- unlikely to already be bound.
ExplosivesOptions.debugSpawnKey = _options:addKeyBind("ExplosivesDebugSpawnKey", getText("UI_Options_Explosives_DEBUG_SPAWN_KEY"), Keyboard.KEY_ADD, getText("Tooltip_Explosives_DEBUG_SPAWN_KEY"))

Events.OnMainMenuEnter.Add(function()
    PZAPI.ModOptions:getOptions("ExplosivesMenuOptionsID"):apply()
end)
