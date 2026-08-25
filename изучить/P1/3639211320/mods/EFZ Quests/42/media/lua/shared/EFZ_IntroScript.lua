if not EFZ then
    EFZ = {}
end

local INTRO_MODULE = "efzIntroGodMode"
local INTRO_COMMAND_APPLY = "apply"
local INTRO_COMMAND_CLEAR = "clear"
local TICK_INTERVAL_MS = 250
local MINIMUM_PROTECTED_HEALTH = 1

local serverProtectedPlayers = setmetatable({}, { __mode = "k" })
local clientProtectedPlayers = {}
local nextProtectionTickMs = 0

local function debugLog(message)
    print("[EFZ IntroScript] " .. tostring(message))
end

local function nowMs()
    return getTimestampMs and getTimestampMs() or 0
end

local function shouldProcessTick()
    local now = nowMs()
    if now < nextProtectionTickMs then
        return false
    end

    nextProtectionTickMs = now + TICK_INTERVAL_MS
    return true
end

local function clampProtectionHealth(value)
    value = tonumber(value) or MINIMUM_PROTECTED_HEALTH
    if value < MINIMUM_PROTECTED_HEALTH then
        return MINIMUM_PROTECTED_HEALTH
    end
    if value > 100 then
        return 100
    end
    return value
end

local function getLocalPlayer(playerObj)
    if playerObj then
        return playerObj
    end

    if getPlayer then
        local resolved = getPlayer()
        if resolved then
            return resolved
        end
    end

    if getSpecificPlayer then
        return getSpecificPlayer(0)
    end

    return nil
end

local function captureProtectionState(playerObj)
    local body = playerObj:getBodyDamage()
    local playerHealth = clampProtectionHealth(playerObj:getHealth())
    local overallHealth = playerHealth
    local partHealths = {}

    if body and body.getHealth then
        overallHealth = clampProtectionHealth(body:getHealth())
    end

    local floor = overallHealth
    if playerHealth > floor then
        floor = playerHealth
    end

    if body and body.getBodyParts then
        local parts = body:getBodyParts()
        if parts and parts.size and parts.get then
            for i = 0, parts:size() - 1 do
                local part = parts:get(i)
                if part and part.getHealth then
                    local partFloor = clampProtectionHealth(part:getHealth())
                    if partFloor < floor then
                        partFloor = floor
                    end
                    partHealths[i] = partFloor
                end
            end
        end
    end

    return {
        floor = floor,
        partHealths = partHealths,
    }
end

local function rememberServerProtection(playerObj)
    local state = serverProtectedPlayers[playerObj]
    if not state then
        state = captureProtectionState(playerObj)
        serverProtectedPlayers[playerObj] = state
    end
    return state
end

local function rememberClientProtection(playerObj)
    local resolvedPlayer = getLocalPlayer(playerObj)
    if not resolvedPlayer then
        return nil, nil, nil
    end

    local playerNum = resolvedPlayer:getPlayerNum() or 0
    local state = clientProtectedPlayers[playerNum]
    if not state then
        state = captureProtectionState(resolvedPlayer)
        clientProtectedPlayers[playerNum] = state
    end

    return resolvedPlayer, playerNum, state
end

local function getTargetPlayer(playerObj)
    if playerObj then
        return playerObj
    end

    local localPlayer = getLocalPlayer()
    if localPlayer then
        return localPlayer
    end

    if isServer() and getOnlinePlayers then
        local players = getOnlinePlayers()
        if players and players:size() == 1 then
            return players:get(0)
        end
    end

    return nil
end

local function syncServerExtraInfo(playerObj)
    if isServer() and GameServer and GameServer.sendPlayerExtraInfo and playerObj then
        GameServer.sendPlayerExtraInfo(playerObj, nil, true)
    end
end

local function syncClientApply(playerObj)
    if playerObj and sendServerCommand then
        sendServerCommand(playerObj, INTRO_MODULE, INTRO_COMMAND_APPLY, {})
    end
end

local function syncClientClear(playerObj)
    if playerObj and sendServerCommand then
        sendServerCommand(playerObj, INTRO_MODULE, INTRO_COMMAND_CLEAR, {})
    end
end

local function setServerProtected(playerObj, active)
    if not playerObj then
        return false
    end

    if active then
        if not serverProtectedPlayers[playerObj] then
            serverProtectedPlayers[playerObj] = captureProtectionState(playerObj)
        end
    else
        serverProtectedPlayers[playerObj] = nil
    end

    return true
end

local function setClientProtected(playerObj, active)
    local resolvedPlayer = getLocalPlayer(playerObj)
    if not resolvedPlayer then
        return false
    end

    local playerNum = resolvedPlayer:getPlayerNum() or 0
    if active then
        if not clientProtectedPlayers[playerNum] then
            clientProtectedPlayers[playerNum] = captureProtectionState(resolvedPlayer)
        end
    else
        clientProtectedPlayers[playerNum] = nil
    end

    return true
end

local function setForcedGodMode(playerObj, enabled)
    if not playerObj then
        return false
    end
    if playerObj:isDead() then
        return false
    end

    playerObj:setGodMod(enabled, true)
    return true
end

local function applyProtectionState(playerObj, state)
    if not playerObj or not state or playerObj:isDead() then
        return false
    end

    local body = playerObj:getBodyDamage()
    local changed = false
    local floor = state.floor

    if playerObj:getHealth() < floor then
        playerObj:setHealth(floor)
        changed = true
    end

    if body and body.getHealth and body.setOverallBodyHealth and body:getHealth() < floor then
        body:setOverallBodyHealth(floor)
        changed = true
    end

    if body and body.getBodyParts then
        local parts = body:getBodyParts()
        if parts and parts.size and parts.get then
            for i = 0, parts:size() - 1 do
                local part = parts:get(i)
                local partFloor = state.partHealths[i]
                if part and partFloor and part.getHealth and part:getHealth() < partFloor then
                    local setPartHealth = part.SetHealth or part.setHealth
                    if setPartHealth then
                        setPartHealth(part, partFloor)
                        changed = true
                    end
                end
            end
        end
    end

    if playerObj.isOnFire and playerObj:isOnFire() and playerObj.StopBurning then
        playerObj:StopBurning()
        changed = true
    end

    if changed and type(sendPlayerDamage) == "function" then
        sendPlayerDamage(playerObj)
    end

    return changed
end

local function activateServerProtection(playerObj)
    if not playerObj then
        return false
    end
    if playerObj:isDead() then
        return false
    end

    local state = rememberServerProtection(playerObj)
    setForcedGodMode(playerObj, true)
    syncServerExtraInfo(playerObj)
    applyProtectionState(playerObj, state)
    debugLog("Server protection activated.")
    return true
end

local function clearServerProtection(playerObj)
    if not playerObj then
        return false
    end

    serverProtectedPlayers[playerObj] = nil
    if not playerObj:isDead() then
        setForcedGodMode(playerObj, false)
        syncServerExtraInfo(playerObj)
    end
    debugLog("Server protection cleared.")
    return true
end

local function activateClientProtection(playerObj)
    local resolvedPlayer, _, state = rememberClientProtection(playerObj)
    if not resolvedPlayer then
        print("[EFZ IntroScript] Godmode apply failed: local player is not ready.")
        return false
    end
    if resolvedPlayer:isDead() then
        print("[EFZ IntroScript] Godmode apply failed: player is dead.")
        return false
    end

    setForcedGodMode(resolvedPlayer, true)
    applyProtectionState(resolvedPlayer, state)
    debugLog("Local protection activated.")
    return true
end

local function clearClientProtection(playerObj)
    local resolvedPlayer = getLocalPlayer(playerObj)
    if not resolvedPlayer then
        print("[EFZ IntroScript] Godmode clear failed: local player is not ready.")
        return false
    end

    clientProtectedPlayers[resolvedPlayer:getPlayerNum() or 0] = nil
    if not resolvedPlayer:isDead() then
        setForcedGodMode(resolvedPlayer, false)
    end
    debugLog("Local protection cleared.")
    return true
end

local function enforceServerProtection(playerObj)
    local state = serverProtectedPlayers[playerObj]
    if not state or not playerObj or playerObj:isDead() then
        return false
    end

    if not playerObj:isGodMod() then
        setForcedGodMode(playerObj, true)
        syncServerExtraInfo(playerObj)
    end
    applyProtectionState(playerObj, state)
    return true
end

local function enforceClientProtection(playerObj)
    local resolvedPlayer = getLocalPlayer(playerObj)
    if not resolvedPlayer then
        return false
    end

    local state = clientProtectedPlayers[resolvedPlayer:getPlayerNum() or 0]
    if not state or resolvedPlayer:isDead() then
        return false
    end

    if not resolvedPlayer:isGodMod() then
        setForcedGodMode(resolvedPlayer, true)
    end
    applyProtectionState(resolvedPlayer, state)
    return true
end

local function onClientCommand(module, command, playerObj, args)
    if not isServer() then
        return
    end
    if module ~= INTRO_MODULE then
        return
    end

    if command == INTRO_COMMAND_APPLY then
        setServerProtected(playerObj, true)
        activateServerProtection(playerObj)
        syncClientApply(playerObj)
        return
    end

    if command == INTRO_COMMAND_CLEAR then
        setServerProtected(playerObj, false)
        clearServerProtection(playerObj)
        syncClientClear(playerObj)
        return
    end

    debugLog("Unknown client command: " .. tostring(command))
end

local function onServerCommand(module, command, args)
    if module ~= INTRO_MODULE then
        return
    end

    if command == INTRO_COMMAND_APPLY then
        activateClientProtection()
        return
    end

    if command == INTRO_COMMAND_CLEAR then
        clearClientProtection()
        return
    end

    debugLog("Unknown server command: " .. tostring(command))
end

local function onProtectionTick()
    if not shouldProcessTick() then
        return
    end

    if isServer() then
        for playerObj, state in pairs(serverProtectedPlayers) do
            if playerObj:isDead() then
                serverProtectedPlayers[playerObj] = nil
            else
                enforceServerProtection(playerObj)
            end
        end
        return
    end

    if getNumActivePlayers and getSpecificPlayer then
        local count = getNumActivePlayers()
        for i = 0, count - 1 do
            local state = clientProtectedPlayers[i]
            if state then
                local playerObj = getSpecificPlayer(i)
                if not playerObj or playerObj:isDead() then
                    clientProtectedPlayers[i] = nil
                else
                    enforceClientProtection(playerObj)
                end
            end
        end
        return
    end

    local state = clientProtectedPlayers[0]
    if state then
        local playerObj = getLocalPlayer()
        if not playerObj or playerObj:isDead() then
            clientProtectedPlayers[0] = nil
        else
            enforceClientProtection(playerObj)
        end
    end
end

local function tryProtectPlayerNow(playerObj)
    if not playerObj then
        return
    end

    if isServer() then
        enforceServerProtection(playerObj)
        return
    end

    enforceClientProtection(playerObj)
end

local function hookProtectionDamageEvent(eventObj)
    if not eventObj or not eventObj.Add then
        return
    end

    eventObj.Add(function(a, b)
        if instanceof and instanceof(a, "IsoPlayer") then
            tryProtectPlayerNow(a)
            return
        end
        if instanceof and instanceof(b, "IsoPlayer") then
            tryProtectPlayerNow(b)
            return
        end

        tryProtectPlayerNow(getLocalPlayer())
    end)
end

if Events and Events.OnClientCommand and Events.OnClientCommand.Add then
    Events.OnClientCommand.Add(onClientCommand)
end

if Events and Events.OnServerCommand and Events.OnServerCommand.Add then
    Events.OnServerCommand.Add(onServerCommand)
end

if Events and Events.OnTick and Events.OnTick.Add then
    Events.OnTick.Add(onProtectionTick)
end

hookProtectionDamageEvent(Events and Events.OnPlayerGetDamage)
hookProtectionDamageEvent(Events and Events.OnPlayerDamage)
hookProtectionDamageEvent(Events and Events.OnCharacterDamage)
hookProtectionDamageEvent(Events and Events.OnHitCharacter)
hookProtectionDamageEvent(Events and Events.OnWeaponHitCharacter)

function EFZ.GodmodePlayer(playerObj)
    local targetPlayer = getTargetPlayer(playerObj)
    if not targetPlayer then
        print("[EFZ IntroScript] Godmode apply failed: target player was not found.")
        return false
    end

    if isClient() then
        setClientProtected(targetPlayer, true)
        activateClientProtection(targetPlayer)
        if not sendClientCommand then
            print("[EFZ IntroScript] Godmode apply failed: sendClientCommand is unavailable.")
            return false
        end
        sendClientCommand(targetPlayer, INTRO_MODULE, INTRO_COMMAND_APPLY, {})
        debugLog("Godmode apply requested from client.")
        return true
    end

    if isServer() then
        setServerProtected(targetPlayer, true)
        activateServerProtection(targetPlayer)
        syncClientApply(targetPlayer)
        return true
    end

    setClientProtected(targetPlayer, true)
    return activateClientProtection(targetPlayer)
end

function EFZ.UnGodmodePlayer(playerObj)
    local targetPlayer = getTargetPlayer(playerObj)
    if not targetPlayer then
        print("[EFZ IntroScript] Godmode clear failed: target player was not found.")
        return false
    end

    if isClient() then
        setClientProtected(targetPlayer, false)
        clearClientProtection(targetPlayer)
        if not sendClientCommand then
            print("[EFZ IntroScript] Godmode clear failed: sendClientCommand is unavailable.")
            return false
        end
        sendClientCommand(targetPlayer, INTRO_MODULE, INTRO_COMMAND_CLEAR, {})
        debugLog("Godmode clear requested from client.")
        return true
    end

    if isServer() then
        setServerProtected(targetPlayer, false)
        clearServerProtection(targetPlayer)
        syncClientClear(targetPlayer)
        return true
    end

    setClientProtected(targetPlayer, false)
    return clearClientProtection(targetPlayer)
end