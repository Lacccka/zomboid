require "ISUI/ISPanel"
require "ui/shared/slots/NMSlotHostLifecycle"

_G.NMCDPlayerWindow = _G.NMCDPlayerWindow or {}
_G.NMCDPlayerWindowEnv = _G.NMCDPlayerWindowEnv or {}

local env = _G.NMCDPlayerWindowEnv
if getmetatable(env) == nil then
    setmetatable(env, { __index = _G })
end

env.NMCDPlayerWindow = _G.NMCDPlayerWindow
env.CDPlayerWindow = env.CDPlayerWindow or ISPanel:derive("NMCDPlayerWindow")
env.windowsByPlayer = env.windowsByPlayer or {}
env.pendingRestoreByPlayer = env.pendingRestoreByPlayer or {}
env.UI_TEXTURES = env.UI_TEXTURES or {}

return env
