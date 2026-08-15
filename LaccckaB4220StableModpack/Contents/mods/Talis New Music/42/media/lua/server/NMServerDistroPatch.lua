NMServerDistroPatch = NMServerDistroPatch or {}

local patch = NMServerDistroPatch

local state = {
    removedVanillaCDPlayers = 0,
    removedVanillaCDs = 0,
    removedVanillaCDPlayersByFamily = {
        procedural = 0,
        suburbs = 0
    },
    removedVanillaCDsByFamily = {
        procedural = 0,
        suburbs = 0
    }
}

local function shouldSuppressVanillaCDItems()
    if NMRuntimeConfig and NMRuntimeConfig.getConvertVanillaCDsAndCDPlayersEnabled then
        return NMRuntimeConfig.getConvertVanillaCDsAndCDPlayersEnabled() == true
    end
    return false
end

local function removeItemFromLootList(list, itemName)
    if type(list) ~= "table" then
        return 0
    end
    local removed = 0
    for i = #list - 1, 1, -2 do
        if tostring(list[i]) == itemName then
            table.remove(list, i + 1)
            table.remove(list, i)
            removed = removed + 1
        end
    end
    return removed
end

local function walkAndStrip(node, itemName)
    local removed = 0
    if type(node) ~= "table" then
        return removed
    end
    if type(node.items) == "table" then
        removed = removed + removeItemFromLootList(node.items, itemName)
    end
    for _, value in pairs(node) do
        if type(value) == "table" then
            removed = removed + walkAndStrip(value, itemName)
        end
    end
    return removed
end

function patch.getStats()
    return {
        removedVanillaCDPlayers = tonumber(state.removedVanillaCDPlayers) or 0,
        removedVanillaCDs = tonumber(state.removedVanillaCDs) or 0,
        removedVanillaCDPlayersByFamily = {
            procedural = tonumber(state.removedVanillaCDPlayersByFamily and state.removedVanillaCDPlayersByFamily.procedural) or 0,
            suburbs = tonumber(state.removedVanillaCDPlayersByFamily and state.removedVanillaCDPlayersByFamily.suburbs) or 0
        },
        removedVanillaCDsByFamily = {
            procedural = tonumber(state.removedVanillaCDsByFamily and state.removedVanillaCDsByFamily.procedural) or 0,
            suburbs = tonumber(state.removedVanillaCDsByFamily and state.removedVanillaCDsByFamily.suburbs) or 0
        }
    }
end

local function applyVanillaCDItemSuppression()
    state.removedVanillaCDPlayers = 0
    state.removedVanillaCDs = 0
    state.removedVanillaCDPlayersByFamily = { procedural = 0, suburbs = 0 }
    state.removedVanillaCDsByFamily = { procedural = 0, suburbs = 0 }
    if not shouldSuppressVanillaCDItems() then
        return
    end
    if ProceduralDistributions and ProceduralDistributions.list then
        state.removedVanillaCDPlayersByFamily.procedural = state.removedVanillaCDPlayersByFamily.procedural
            + walkAndStrip(ProceduralDistributions.list, "CDplayer")
            + walkAndStrip(ProceduralDistributions.list, "Base.CDplayer")
        state.removedVanillaCDsByFamily.procedural = state.removedVanillaCDsByFamily.procedural
            + walkAndStrip(ProceduralDistributions.list, "Disc_Retail")
            + walkAndStrip(ProceduralDistributions.list, "Base.Disc_Retail")
    end
    if SuburbsDistributions then
        state.removedVanillaCDPlayersByFamily.suburbs = state.removedVanillaCDPlayersByFamily.suburbs
            + walkAndStrip(SuburbsDistributions, "CDplayer")
            + walkAndStrip(SuburbsDistributions, "Base.CDplayer")
        state.removedVanillaCDsByFamily.suburbs = state.removedVanillaCDsByFamily.suburbs
            + walkAndStrip(SuburbsDistributions, "Disc_Retail")
            + walkAndStrip(SuburbsDistributions, "Base.Disc_Retail")
    end
    state.removedVanillaCDPlayers = state.removedVanillaCDPlayersByFamily.procedural + state.removedVanillaCDPlayersByFamily.suburbs
    state.removedVanillaCDs = state.removedVanillaCDsByFamily.procedural + state.removedVanillaCDsByFamily.suburbs
    if NMCore and NMCore.logChannel then
        NMCore.logChannel(
            "registry",
            "distro.patch removed vanilla CD items",
            string.format(
                "cdplayers={procedural=%s suburbs=%s total=%s} cds={procedural=%s suburbs=%s total=%s}",
                tostring(state.removedVanillaCDPlayersByFamily.procedural),
                tostring(state.removedVanillaCDPlayersByFamily.suburbs),
                tostring(state.removedVanillaCDPlayers),
                tostring(state.removedVanillaCDsByFamily.procedural),
                tostring(state.removedVanillaCDsByFamily.suburbs),
                tostring(state.removedVanillaCDs)
            )
        )
    end
end

if Events and Events.OnPreDistributionMerge and Events.OnPreDistributionMerge.Add then
    Events.OnPreDistributionMerge.Add(applyVanillaCDItemSuppression)
end

