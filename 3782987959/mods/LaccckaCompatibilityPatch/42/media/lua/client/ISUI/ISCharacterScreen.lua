-- Bandits still requires the pre-B42.20 character-screen path.
local Guard = require "LCC/Guard"
local FEATURE = "shim.bandits.character-screen"

Guard.safeRequire(FEATURE, "XpSystem/ISUI/ISCharacterScreen")
return ISCharacterScreen or {}
