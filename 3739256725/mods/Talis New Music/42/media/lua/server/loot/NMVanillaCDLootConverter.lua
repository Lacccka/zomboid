NMVanillaCDLootConverter = NMVanillaCDLootConverter or {}

local converter = NMVanillaCDLootConverter

local state = {
    stats = {
        mode = "nm_owned_backfill",
        convertedVanillaCDs = 0,
        convertedVanillaCDPlayers = 0,
        failedVanillaCDs = 0,
        failedVanillaCDPlayers = 0
    }
}

local function resetStats()
    state.stats = {
        mode = "nm_owned_backfill",
        convertedVanillaCDs = 0,
        convertedVanillaCDPlayers = 0,
        failedVanillaCDs = 0,
        failedVanillaCDPlayers = 0
    }
end

local function isEnabled()
    return NMRuntimeConfig
        and NMRuntimeConfig.getConvertVanillaCDsAndCDPlayersEnabled
        and NMRuntimeConfig.getConvertVanillaCDsAndCDPlayersEnabled() == true
end

function converter.configure()
    resetStats()
end

function converter.isEnabled()
    return isEnabled()
end

function converter.getStats()
    return {
        mode = tostring(state.stats.mode or "disabled_by_strip"),
        convertedVanillaCDs = tonumber(state.stats.convertedVanillaCDs) or 0,
        convertedVanillaCDPlayers = tonumber(state.stats.convertedVanillaCDPlayers) or 0,
        failedVanillaCDs = tonumber(state.stats.failedVanillaCDs) or 0,
        failedVanillaCDPlayers = tonumber(state.stats.failedVanillaCDPlayers) or 0
    }
end

function converter.getConfigurationSnapshot()
    return {
        mode = "nm_owned_backfill"
    }
end

function converter.convertContainer(_container)
    if not isEnabled() then
        return 0, 0
    end
    return 0, 0
end

return converter
