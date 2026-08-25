-- Copyright (c) 2022-2024 Oneline/D.Borovsky
-- All rights reserved
require "ISUI/ISLayoutManager"
require "ISUI/ISSafetyUI"
require "SSRLoader"

-- Don't show again (explicit modal)
ISLayoutManager.SaveWindowVisibleSSR = function(bool, layout)
    if getCore():getGameMode() == "Tutorial" then return; end
	layout.visible = tostring(bool)
end