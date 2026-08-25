local PATCH_TAG = "[EFZ_SSR_Teleport_Compat]"
local TARGET_SSR_MOD_VERSION = "2025_12_15"

if _G.__EFZ_SSR_TELEPORT_COMPAT_FILE_LOADED then
    return
end
_G.__EFZ_SSR_TELEPORT_COMPAT_FILE_LOADED = true
print(PATCH_TAG .. " Loaded EFZ_SSR_Teleport_Compat.lua")

local loggedKeys = {}
local pendingTeleport = nil

local function debugPrint(message)
    print(PATCH_TAG .. " " .. message)
end

local function logOnce(key, message)
    if loggedKeys[key] then
        return
    end
    loggedKeys[key] = true
    debugPrint(message)
end

local function isSsrQuestsActive()
    local mods = getActivatedMods()
    return mods and (mods:contains("ssr-quests") or mods:contains("ssr_quests") or mods:contains("SSR_QUESTS"))
end

local function tryRequire(path)
    local ok, result = pcall(require, path)
    if ok then
        return result
    end
    return nil
end

local function getSsrModDetails()
    local mod = nil
    if type(getModInfoByID) == "function" then
        mod = getModInfoByID("ssr-quests")
    end
    if not mod and type(getModInfo) == "function" then
        mod = getModInfo("ssr-quests")
    end
    if not mod and ChooseGameInfo and type(ChooseGameInfo.getModDetails) == "function" then
        mod = ChooseGameInfo.getModDetails("ssr-quests")
    end
    if not mod and ChooseGameInfo and type(ChooseGameInfo.getAvailableModDetails) == "function" then
        mod = ChooseGameInfo.getAvailableModDetails("ssr-quests")
    end
    return mod
end

local function readModVersionFromInfoFile(infoPath)
    if type(getFileReader) ~= "function" or not infoPath then
        return nil
    end

    local ok, reader = pcall(getFileReader, infoPath, false)
    if not ok or not reader then
        return nil
    end

    while true do
        local line = reader:readLine()
        if not line then
            break
        end
        local version = string.match(tostring(line), "^modversion=(.+)$")
        if version and version ~= "" then
            reader:close()
            return version
        end
    end

    reader:close()
    return nil
end

local function getVersionFromModDetails(mod)
    if not mod then
        return nil
    end

    if type(mod.getModVersion) == "function" then
        local version = mod:getModVersion()
        if version and version ~= "" then
            return tostring(version)
        end
    end

    local dirGetters = { "getCommonDir", "getVersionDir", "getDir" }
    for i = 1, #dirGetters do
        local getterName = dirGetters[i]
        local getter = mod[getterName]
        if type(getter) == "function" then
            local dir = getter(mod)
            if dir and dir ~= "" then
                if type(getModInfo) == "function" then
                    local dirInfo = getModInfo(dir)
                    if dirInfo and dirInfo ~= mod and type(dirInfo.getModVersion) == "function" then
                        local version = dirInfo:getModVersion()
                        if version and version ~= "" then
                            return tostring(version)
                        end
                    end
                end

                local normalizedDir = tostring(dir):gsub("\\", "/")
                local version = readModVersionFromInfoFile(normalizedDir .. "/mod.info")
                if version then
                    return version
                end
            end
        end
    end

    return nil
end

local function getSsrModVersion()
    local mod = getSsrModDetails()
    return getVersionFromModDetails(mod)
end

local function isLegacyKamisamaRequestTeleport()
    if not debug or type(debug.getinfo) ~= "function" then
        return false
    end
    if type(Kamisama) ~= "table" or type(Kamisama.requestTeleport) ~= "function" then
        return false
    end

    local info = debug.getinfo(Kamisama.requestTeleport, "S")
    if not info then
        return false
    end

    local source = tostring(info.source or "")
    if not string.find(source, "Kamisama.lua", 1, true) then
        return false
    end

    return info.linedefined == 28 and info.lastlinedefined == 40
end

local function shouldPatchSsrTeleport()
    local version = getSsrModVersion()
    if version == TARGET_SSR_MOD_VERSION then
        return true, version
    end
    if version and version ~= TARGET_SSR_MOD_VERSION then
        return false, version
    end
    if isLegacyKamisamaRequestTeleport() then
        return true, "legacy-structure"
    end
    return false, version
end

local function scheduleCallback(callback)
    if not callback then
        return
    end
    if SSRTimer and SSRTimer.add_ms then
        SSRTimer.add_ms(callback, 100, false)
    else
        callback()
    end
end

local function clearPendingTeleport()
    pendingTeleport = nil
    if Events and Events.OnTick and Events.OnTick.Remove and EFZ_SSR_Teleport_Compat_ProcessPendingTeleport then
        Events.OnTick.Remove(EFZ_SSR_Teleport_Compat_ProcessPendingTeleport)
    end
end

function EFZ_SSR_Teleport_Compat_ProcessPendingTeleport()
    if not pendingTeleport then
        clearPendingTeleport()
        return
    end

    local playerObj = getSpecificPlayer(pendingTeleport.playerNum) or getPlayer()
    if not playerObj or playerObj:isDead() then
        local callback = pendingTeleport.callback
        clearPendingTeleport()
        scheduleCallback(callback)
        return
    end

    pendingTeleport.ticks = pendingTeleport.ticks + 1
    if pendingTeleport.phase == "applyLocalTeleport" then
        if EFZ and EFZ.Teleport and EFZ.Teleport.beginLocalTeleport then
            if EFZ.Teleport.beginLocalTeleport(playerObj, pendingTeleport.destination) then
                pendingTeleport.phase = "waitForSquare"
                return
            end
        end
        local callback = pendingTeleport.callback
        clearPendingTeleport()
        scheduleCallback(callback)
        return
    end

    if pendingTeleport.phase == "waitForSquare" then
        if EFZ and EFZ.Teleport and EFZ.Teleport.finishLocalTeleport and EFZ.Teleport.finishLocalTeleport(playerObj) then
            local callback = pendingTeleport.callback
            clearPendingTeleport()
            scheduleCallback(callback)
            return
        end
    end

    if pendingTeleport.ticks > 300 then
        logOnce("teleport-timeout", string.format(
            "SSR teleport stabilization timed out at %.2f, %.2f, %.2f.",
            playerObj:getX(),
            playerObj:getY(),
            playerObj:getZ()
        ))
        local callback = pendingTeleport.callback
        clearPendingTeleport()
        scheduleCallback(callback)
    end
end

local function startSingleplayerTeleport(x, y, z, callback)
    local playerObj = getPlayer()
    if not playerObj then
        scheduleCallback(callback)
        return false
    end

    local tileX = math.floor(tonumber(x) or 0)
    local tileY = math.floor(tonumber(y) or 0)
    local tileZ = math.floor(tonumber(z) or 0)
    if not getWorld():isValidSquare(tileX, tileY, tileZ) then
        logOnce("invalid-destination", string.format(
            "Skipped SSR teleport: invalid destination %d,%d,%d.",
            tileX,
            tileY,
            tileZ
        ))
        scheduleCallback(callback)
        return false
    end

    local destination = { x = tileX, y = tileY, z = tileZ }
    if EFZ and EFZ.Teleport and EFZ.Teleport.beginLocalTeleport and EFZ.Teleport.finishLocalTeleport then
        pendingTeleport = {
            playerNum = playerObj:getPlayerNum(),
            callback = callback,
            destination = destination,
            phase = "applyLocalTeleport",
            ticks = 0,
        }

        if Events and Events.OnTick and Events.OnTick.Add and Events.OnTick.Remove then
            Events.OnTick.Remove(EFZ_SSR_Teleport_Compat_ProcessPendingTeleport)
            Events.OnTick.Add(EFZ_SSR_Teleport_Compat_ProcessPendingTeleport)
            return true
        end

        if EFZ.Teleport.beginLocalTeleport(playerObj, destination) and EFZ.Teleport.finishLocalTeleport(playerObj) then
            scheduleCallback(callback)
            return true
        end

        clearPendingTeleport()
    end

    playerObj:StopAllActionQueue()
    if playerObj:getVehicle() then
        playerObj:ensureNotInVehicle()
    end
    playerObj:teleportTo(tileX, tileY, tileZ)
    scheduleCallback(callback)
    return true
end

local function hasExpectedKamisamaStructure()
    if type(Kamisama) ~= "table" then
        return false, "Kamisama is not a table"
    end
    if type(Kamisama.requestTeleport) ~= "function" then
        return false, "requestTeleport is " .. type(Kamisama.requestTeleport)
    end
    if type(Kamisama.teleport) ~= "function" then
        return false, "teleport is " .. type(Kamisama.teleport)
    end
    return true, nil
end

local function applyPatch()
    if not isSsrQuestsActive() then
        return
    end

    if _G.__EFZ_SSR_TELEPORT_COMPAT_PATCHED then
        return
    end

    if not Kamisama then
        tryRequire("Communications/Kamisama")
    end

    local shouldPatch, version = shouldPatchSsrTeleport()
    if not shouldPatch then
        logOnce("skip-version", "Skipped SSR teleport patch: modversion=" .. tostring(version))
        return
    end

    local structureOk, structureReason = hasExpectedKamisamaStructure()
    if not structureOk then
        logOnce("structure-mismatch", "Skipped SSR teleport patch: " .. structureReason)
        return
    end

    local originalRequestTeleport = Kamisama.requestTeleport
    Kamisama._efzOriginalRequestTeleport = originalRequestTeleport

    function Kamisama.requestTeleport(sender, x, y, z, callback)
        if isClient() or isServer() then
            return originalRequestTeleport(sender, x, y, z, callback)
        end

        triggerEvent("OnTeleport", nil)
        return startSingleplayerTeleport(x, y, z, callback)
    end

    _G.__EFZ_SSR_TELEPORT_COMPAT_PATCHED = true
    debugPrint("Patched SSR singleplayer teleport compatibility. Signature=" .. tostring(version))
end

Events.OnGameBoot.Add(applyPatch)
Events.OnCreatePlayer.Add(applyPatch)
Events.OnGameStart.Add(applyPatch)
