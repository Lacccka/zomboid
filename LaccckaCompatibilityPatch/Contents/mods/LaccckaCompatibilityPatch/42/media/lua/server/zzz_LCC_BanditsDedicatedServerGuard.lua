-- Bandits 42.20 uses BanditZombie.GetInstanceById() from BanditServerCommands.lua,
-- but BanditZombie.lua belongs to media/lua/client and is not available on a
-- dedicated server. Provide only the missing lookup contract so the server-side
-- brain sync can complete instead of throwing "GetInstanceById of non-table: null".
--
-- Returning nil preserves the behavior that is already intended by the caller:
-- updating ItemsToSpawnAtDeath is conditional on a live local IsoZombie lookup.
if not isServer() then return end

BanditZombie = BanditZombie or {}

if not BanditZombie.GetInstanceById then
    BanditZombie.GetInstanceById = function(id)
        return nil
    end
end
