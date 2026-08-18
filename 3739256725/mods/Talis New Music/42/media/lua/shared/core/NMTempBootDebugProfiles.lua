NMTempBootDebugProfiles = NMTempBootDebugProfiles or {}

local profiles = NMTempBootDebugProfiles

local ENABLE_TEMP_SP_LOOT_RELOAD_PROBE = false
local TEMP_SP_LOOT_RELOAD_PROBE_NAME = "sp_loot_reload_probe"

local function isEnabled()
    return ENABLE_TEMP_SP_LOOT_RELOAD_PROBE == true
end

local function shouldAutoApply()
    if not isEnabled() then
        return false
    end
    return not (NMCore and NMCore.isMultiplayerMode and NMCore.isMultiplayerMode() == true)
end

local function collectRawSandboxValues()
    local root = type(SandboxVars) == "table" and SandboxVars or nil
    local page = root and type(root.NewMusic) == "table" and root.NewMusic or nil
    local function getPageValue(key)
        if page == nil then
            return nil
        end
        return page[key]
    end
    return {
        pagePresent = page ~= nil,
        mediaSpawnsWithCases = getPageValue("MediaSpawnsWithCases"),
        zomboidOST = getPageValue("ZomboidOST"),
        convertVanilla = getPageValue("ConvertVanillaCDsAndCDPlayers"),
        cassettes = page and page.CassettesSpawnRate or nil,
        vinyl = page and page.VinylRecordsSpawnRate or nil,
        cds = page and page.CDsSpawnRate or nil,
        walkman = page and page.WalkmanSpawnRate or nil,
        boombox = page and page.BoomboxSpawnRate or nil,
        cdplayer = page and page.CDPlayerSpawnRate or nil,
        recordplayer = page and page.RecordPlayerSpawnRate or nil
    }
end

local function resolveGetterValue(getter)
    if type(getter) ~= "function" then
        return nil
    end
    return getter()
end

local function collectResolvedSandboxValues()
    return {
        mediaSpawnsWithCases = resolveGetterValue(NMRuntimeConfig and NMRuntimeConfig.getMediaSpawnsWithCasesEnabled or nil),
        zomboidOST = resolveGetterValue(NMRuntimeConfig and NMRuntimeConfig.getZomboidOSTEnabled or nil),
        convertVanilla = resolveGetterValue(NMRuntimeConfig and NMRuntimeConfig.getConvertVanillaCDsAndCDPlayersEnabled or nil),
        cassettes = NMRuntimeConfig and NMRuntimeConfig.getCassettesSpawnRate and NMRuntimeConfig.getCassettesSpawnRate() or nil,
        vinyl = NMRuntimeConfig and NMRuntimeConfig.getVinylRecordsSpawnRate and NMRuntimeConfig.getVinylRecordsSpawnRate() or nil,
        cds = NMRuntimeConfig and NMRuntimeConfig.getCDsSpawnRate and NMRuntimeConfig.getCDsSpawnRate() or nil,
        walkman = NMRuntimeConfig and NMRuntimeConfig.getWalkmanSpawnRate and NMRuntimeConfig.getWalkmanSpawnRate() or nil,
        boombox = NMRuntimeConfig and NMRuntimeConfig.getBoomboxSpawnRate and NMRuntimeConfig.getBoomboxSpawnRate() or nil,
        cdplayer = NMRuntimeConfig and NMRuntimeConfig.getCDPlayerSpawnRate and NMRuntimeConfig.getCDPlayerSpawnRate() or nil,
        recordplayer = NMRuntimeConfig and NMRuntimeConfig.getRecordPlayerSpawnRate and NMRuntimeConfig.getRecordPlayerSpawnRate() or nil
    }
end

local function formatRateMap(values)
    return string.format(
        "cassettes=%s vinyl=%s cds=%s walkman=%s boombox=%s cdplayer=%s recordplayer=%s",
        tostring(values and values.cassettes),
        tostring(values and values.vinyl),
        tostring(values and values.cds),
        tostring(values and values.walkman),
        tostring(values and values.boombox),
        tostring(values and values.cdplayer),
        tostring(values and values.recordplayer)
    )
end

function profiles.getActivePresetName()
    if shouldAutoApply() then
        return TEMP_SP_LOOT_RELOAD_PROBE_NAME
    end
    return nil
end

function profiles.applyIfEnabled(side, stage)
    if not shouldAutoApply() then
        return false
    end
    local applied = false
    if NMRuntimeConfig and NMRuntimeConfig.applySubsystemDebugPreset then
        applied = NMRuntimeConfig.applySubsystemDebugPreset(TEMP_SP_LOOT_RELOAD_PROBE_NAME, true) == true
    else
        applied = (NMRuntimeConfig and NMRuntimeConfig.setSubsystemDebugEnabled and NMRuntimeConfig.setSubsystemDebugEnabled("core", true) == true) or false
        if NMRuntimeConfig and NMRuntimeConfig.setSubsystemDebugEnabled then
            NMRuntimeConfig.setSubsystemDebugEnabled("loot", true)
            NMRuntimeConfig.setSubsystemDebugEnabled("loot_probe", true)
        end
    end
    if NMCore and NMCore.dumpDebugState then
        NMCore.dumpDebugState()
    end
    if NMCore and NMCore.logChannel then
        NMCore.logChannel(
            "core",
            "temp_boot_debug_profile_applied",
            string.format(
                "preset=%s side=%s stage=%s applied=%s authority=%s",
                tostring(TEMP_SP_LOOT_RELOAD_PROBE_NAME),
                tostring(side or "unknown"),
                tostring(stage or "unknown"),
                tostring(applied),
                tostring(NMCore.getRuntimeAuthorityMode and NMCore.getRuntimeAuthorityMode() or "unknown")
            )
        )
    end
    return applied
end

function profiles.logSandboxSnapshot(channel, tag, stage, extra)
    local raw = collectRawSandboxValues()
    local resolved = collectResolvedSandboxValues()
    local extraText = tostring(extra or "")
    local line = string.format(
        "stage=%s raw={pagePresent=%s mediaSpawnsWithCases=%s zomboidOST=%s convertVanilla=%s rates={%s}} resolved={mediaSpawnsWithCases=%s zomboidOST=%s convertVanilla=%s rates={%s}} extra=%s",
        tostring(stage or "unknown"),
        tostring(raw.pagePresent),
        tostring(raw.mediaSpawnsWithCases),
        tostring(raw.zomboidOST),
        tostring(raw.convertVanilla),
        formatRateMap(raw),
        tostring(resolved.mediaSpawnsWithCases),
        tostring(resolved.zomboidOST),
        tostring(resolved.convertVanilla),
        formatRateMap(resolved),
        extraText
    )
    if NMCore and NMCore.logChannel then
        NMCore.logChannel(tostring(channel or "core"), tostring(tag or "temp_boot_sandbox"), line)
    else
        print("[NewMusic] [TempBootProbe] " .. line)
    end
end

return profiles
