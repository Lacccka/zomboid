-- Compatibility wrapper for the pre-0.2.8 module name.
--
-- Dedicated Server 42.20 adds the client Lua path before the server Lua path.
-- Client and server files must therefore not implement different modules under
-- the same require name. The real server adapter now has a unique module path.
local adapter = require "LCCQF/Runtime/LCCQFBanditsServerRuntime"

-- Essential quest-giver physical policy is provider-specific and must be
-- loaded together with the Bandits server adapter. Do not rely on recursive
-- server-directory auto-loading for this role policy.
require "LCCQF/Runtime/zz_LCCQFBanditsQuestGiverProtection"

return adapter
