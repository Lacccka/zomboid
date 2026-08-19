-- Bandits 42.20 uses BanditZombie.GetInstanceById() from BanditServerCommands.lua,
-- but BanditZombie.lua belongs to media/lua/client and is not available on a
-- dedicated server. Provide only the missing lookup contract so the server-side
-- brain sync can complete instead of throwing "GetInstanceById of non-table: null".
--
-- Returning nil preserves the behavior that is already intended by the caller:
-- updating ItemsToSpawnAtDeath is conditional on a live local IsoZombie lookup.
if not isServer() then return end

local Guard = require "LCC/Guard"
local FEATURE = "bandits.dedicated-zombie-lookup"

Guard.install {
    id = FEATURE,
    validate = function()
        if BanditZombie ~= nil and type(BanditZombie) ~= "table" then
            return false, "BanditZombie exists but is not a table"
        end
        return true
    end,
    install = function()
        BanditZombie = BanditZombie or {}

        -- If Bandits restores the server-side API itself, leave it untouched.
        if not BanditZombie.GetInstanceById then
            BanditZombie.GetInstanceById = function(id)
                return nil
            end
        end
    end,
}
