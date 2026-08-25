local MenuDock = require("ElyonLib/UI/MenuDock/MenuDock")
local Client = require("InteractiveNPCs/Client")
require("InteractiveNPCs/ContextMenu")

local function openManager()
	local AdminUI = require("InteractiveNPCs/UI/AdminUI")
	AdminUI.open()
	Client.requestAdmin()
end

MenuDock.registerButton({
	id = "interactive_npcs_manager",
	title = getText("IGUI_INPC_ManageTitle"),
	icon = "media/ui/ui_icon_interactive_npcs.png",
	minimumAccessLevel = "Admin",
	allowSinglePlayer = true,
	onClick = function(playerNum, entry)
		openManager()
	end,
})
