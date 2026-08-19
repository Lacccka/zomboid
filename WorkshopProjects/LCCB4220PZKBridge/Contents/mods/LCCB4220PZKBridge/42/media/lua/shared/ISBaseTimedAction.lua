-- PZK Carzone still requires the pre-B42.20 root-level path.
local Guard = require "LCC/Guard"
local FEATURE = "shim.pzk.base-timed-action"

Guard.safeRequire(FEATURE, "TimedActions/ISBaseTimedAction")
return ISBaseTimedAction or {}
