-- Compatibility wrapper for the pre-0.2.8 module name.
--
-- Dedicated Server 42.20 adds the client Lua path before the server Lua path.
-- Client and server files must therefore not implement different modules under
-- the same require name. The real server adapter now has a unique module path.
return require "LCCQF/Runtime/LCCQFBanditsServerRuntime"
