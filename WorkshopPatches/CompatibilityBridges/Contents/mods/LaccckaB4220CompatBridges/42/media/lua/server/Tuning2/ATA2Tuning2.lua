-- SVU3 still requires the pre-B42 spelling. TsarLib B42 ships ATATuning2.
local Guard = require "LCC/Guard"
local FEATURE = "shim.svu3.ata2-tuning2"

local module = Guard.safeRequire(FEATURE, "Tuning2/ATATuning2", {})
return module or ATATuning2 or {}
