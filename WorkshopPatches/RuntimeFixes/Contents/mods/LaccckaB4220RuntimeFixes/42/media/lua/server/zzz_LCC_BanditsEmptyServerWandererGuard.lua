-- Source-clean empty-server guard for the Bandits wanderer scheduler.
--
-- BanditServerWanderers keeps its own local orchestrator and event registration.
-- In dedicated multiplayer with zero online players, upstream leaves `day` nil
-- and later compares it with the clan day range. The orchestrator obtains its
-- clan view through BanditCustom.ClanGetAll(), so returning an empty temporary
-- view only for that runtime state turns the scheduler tick into a no-op without
-- replacing or redistributing BanditServerWanderers.lua.
if not isServer() then return end

local Guard = require "LCC/Guard"
local FEATURE = "bandits.wanderers-empty-server"

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
                    return {}
                end
            end

            return originalClanGetAll(...)
        end

        BanditCustom.__LCCEmptyServerClanView = true
    end,
}
