-- Publication-oriented empty-server guard for Bandits wanderer scheduling.
--
-- BanditServerWanderers keeps its original local orchestrator and event
-- registration. The upstream orchestrator obtains clan data through the public
-- BanditCustom.ClanGetAll() API. On an empty multiplayer server it has no
-- player-derived `day`, so iterating any wanderer clan can compare nil to the
-- configured day range. Returning an empty view for this one runtime state
-- makes that scheduler tick a no-op without replacing upstream source.
if not isServer() then return end

local Guard = require "LCC/Guard"
local FEATURE = "bandits.wanderers-empty-server"

-- Resolve BanditCustom from the separately installed Bandits2 dependency before
-- wrapping its public API. This removes reliance on incidental filename order.
Guard.safeRequire(FEATURE, "BanditCustom")
if not Guard.isEnabled(FEATURE) then return end

local function isEmptyMultiplayerServer()
    local world = getWorld()
    if not world or world:getGameMode() ~= "Multiplayer" then
        return false
    end

    local players = getOnlinePlayers()
    return players ~= nil and players:size() == 0
end

Guard.install {
    id = FEATURE,
    validate = function()
        if type(BanditCustom) ~= "table" then
            return false, "BanditCustom table is unavailable"
        end
        if type(BanditCustom.ClanGetAll) ~= "function" then
            return false, "BanditCustom.ClanGetAll is unavailable"
        end
        if type(getOnlinePlayers) ~= "function" then
            return false, "getOnlinePlayers is unavailable"
        end
        return true
    end,
    install = function()
        if BanditCustom.__LCCEmptyServerClanView then return end

        local originalClanGetAll = BanditCustom.ClanGetAll

        BanditCustom.ClanGetAll = function(...)
            if Guard.isEnabled(FEATURE) then
                local ok, emptyServer = Guard.protect(
                    FEATURE,
                    "empty-server check",
                    isEmptyMultiplayerServer
                )
                if ok and emptyServer then
                    -- Return a fresh table so no unrelated empty-server caller can
                    -- mutate shared guard state. The Bandits orchestrator only reads it.
                    return {}
                end
            end

            -- The original Bandits API remains authoritative in single-player
            -- and whenever at least one multiplayer player is online.
            return originalClanGetAll(...)
        end

        BanditCustom.__LCCEmptyServerClanView = true
    end,
}
