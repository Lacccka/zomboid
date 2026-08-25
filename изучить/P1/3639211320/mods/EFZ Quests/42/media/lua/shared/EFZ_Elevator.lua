-- Shared elevator teleport (B42.19 SP/MP)
-- Client: requests teleport from the elevator tile.
-- Server: validates the tile, then authorizes client-local teleport in MP.

if not EFZ then
    EFZ = {}
end

EFZ.Elevator = EFZ.Elevator or {}
local Elevator = EFZ.Elevator

local LOG_PREFIX = "[EFZ][Elevator] "

local function logInfo(message)
    DebugLog.log(LOG_PREFIX .. message)
end

local runTeleportCallback
local beginFadeIn

-- NOTE: Keep this table identical on client/server (shared file) so validation & UI match.
local teleportDestinations = {
    ["20107:16:0"] = { x = 20390, y = 6, z = 1 },
    ["20107:15:0"] = { x = 20390, y = 6, z = 1 },
    ["20390:6:1"] = { x = 20107, y = 16, z = 0 },
    ["20390:5:1"] = { x = 20107, y = 16, z = 0 },
}

local function buildKey(x, y, z)
    return string.format("%d:%d:%d", math.floor(x), math.floor(y), math.floor(z or 0))
end

local function normalizeDestination(dest)
    if not dest then
        return nil
    end
    local x = tonumber(dest.x)
    local y = tonumber(dest.y)
    local z = math.floor(tonumber(dest.z) or 0)
    if x == nil or y == nil then
        return nil
    end
    return { x = x, y = y, z = z }
end

local function teleportPlayerCommon(playerObj, destination)
    if EFZ.Teleport and EFZ.Teleport.beginLocalTeleport then
        return EFZ.Teleport.beginLocalTeleport(playerObj, destination)
    end

    local dest = normalizeDestination(destination)
    if (not playerObj) or (not dest) then
        return false
    end

    local x = dest.x
    local y = dest.y
    local z = math.floor(dest.z)

    playerObj:StopAllActionQueue()
    if playerObj:getVehicle() then
        playerObj:ensureNotInVehicle()
    end
    -- Use the engine teleport path so distant destinations can stream in after arrival.
    playerObj:teleportTo(x, y, z)
    playerObj:setForceX(x)
    playerObj:setForceY(y)

    local world = getWorld and getWorld() or nil
    if world and world.update then
        world:update()
    end

    return true
end

-- === Networking ===

local function sendTeleportCompleteToClient(playerObj, destination, options)
    if not sendServerCommand then
        return
    end
    options = options or {}
    local payload = {}
    if playerObj and playerObj.getOnlineID then
        payload.onlineID = playerObj:getOnlineID()
    end
    if playerObj and playerObj.getPlayerNum then
        payload.playerNum = playerObj:getPlayerNum()
    end
    if destination then
        payload.destination = {
            x = destination.x,
            y = destination.y,
            z = destination.z,
        }
    end
    payload.performLocalTeleport = options.performLocalTeleport == true

    if playerObj then
        sendServerCommand(playerObj, "EFZ", "ElevatorTeleportComplete", payload)
    else
        sendServerCommand("EFZ", "ElevatorTeleportComplete", payload)
    end
end

local function sendTeleportRequestToServer(fadeState)
    if not (isClient and isClient() and sendClientCommand) then
        return false
    end

    local payload = {
        requestKind = (fadeState and fadeState.requestKind) or "elevator",
    }
    if fadeState and fadeState.destination then
        payload.destination = {
            x = fadeState.destination.x,
            y = fadeState.destination.y,
            z = fadeState.destination.z,
        }
    end
    sendClientCommand("EFZ", "ElevatorRequestTeleport", payload)
    return true
end

local function parseTileKey(keyString)
    local x, y, z = string.match(keyString, "^(%d+):(%d+):(%d+)$")
    if not x then
        return nil, nil, nil
    end
    return tonumber(x), tonumber(y), tonumber(z)
end

local function resolveDestinationForPlayer(playerObj)
    if not playerObj or not playerObj.getX or not playerObj.getY or not playerObj.getZ then
        return nil, nil
    end
    local keyString = buildKey(playerObj:getX(), playerObj:getY(), playerObj:getZ())
    local destination = teleportDestinations[keyString]
    return destination, keyString
end

local function findNearElevatorTileKey(keyString)
    local x, y, z = parseTileKey(keyString)
    if x == nil or y == nil or z == nil then
        return nil
    end

    for srcKey, _ in pairs(teleportDestinations) do
        local sx, sy, sz = parseTileKey(srcKey)
        if sx == x and sz == z and math.abs(sy - y) <= 1 then
            return srcKey
        end
    end

    return nil
end

function Elevator.isTeleportInProgress()
    return Elevator._fadeState ~= nil or Elevator._pendingTeleport ~= nil
end

-- Server: validate & execute.
local function onClientCommand(module, command, playerObj, args)
    if module ~= "EFZ" then
        return
    end
    if command ~= "ElevatorRequestTeleport" then
        return
    end

    -- Only meaningful on the server Lua state.
    if isClient and isClient() then
        return
    end

    if not playerObj or (playerObj.isDead and playerObj:isDead()) then
        return
    end

    local requestKind = args and args.requestKind or "elevator"
    local destination = nil
    if requestKind == "direct" then
        destination = normalizeDestination(args and args.destination)
    else
        destination = resolveDestinationForPlayer(playerObj)
    end
    if not destination then
        logInfo(string.format("Rejected %s request for onlineID=%s: no destination.",
            tostring(requestKind),
            tostring(playerObj.getOnlineID and playerObj:getOnlineID() or nil)))
        return
    end

    -- B42.18 MP: deploy teleport is reliable because the client performs the local teleport.
    -- Reuse the same pattern here after the server validates the request, instead of relying
    -- on GameServer.sendTeleport(), which can fail silently for remote clients.
    sendTeleportCompleteToClient(playerObj, destination, {
        performLocalTeleport = true,
    })
end

if Events and Events.OnClientCommand then
    Events.OnClientCommand.Add(onClientCommand)
end

-- Client: finish the fade after the server authorizes the teleport.
local function onServerCommand(module, command, args)
    if module ~= "EFZ" then
        return
    end
    if command ~= "ElevatorTeleportComplete" then
        return
    end

    local playerObj = getPlayer and getPlayer() or nil
    if not playerObj or (playerObj.isDead and playerObj:isDead()) then
        return
    end

    -- Match target (onlineID preferred; fallback to playerNum; SP/unknown -> accept).
    local matchesTarget = false
    if args.onlineID ~= nil and playerObj.getOnlineID and playerObj:getOnlineID() == args.onlineID then
        matchesTarget = true
    elseif args.playerNum ~= nil and playerObj.getPlayerNum and playerObj:getPlayerNum() == args.playerNum then
        matchesTarget = true
    elseif (not isClient) or (not isClient()) then
        matchesTarget = true
    end
    if not matchesTarget then
        return
    end

    local destination = nil
    if args and args.destination then
        destination = normalizeDestination(args.destination)
    elseif Elevator._fadeState and Elevator._fadeState.destination then
        destination = normalizeDestination(Elevator._fadeState.destination)
    end
    if not destination then
        if Elevator._fadeState then
            Elevator._fadeState.waitTicks = 0
            Elevator._fadeState.phase = "fadeIn"
        end
        runTeleportCallback(false)
        return
    end

    if not (Events and Events.OnTick) then
        if Elevator._fadeState then
            Elevator._fadeState.waitTicks = 0
            Elevator._fadeState.phase = "fadeIn"
        end
        runTeleportCallback(false)
        return
    end

    if Elevator._processPendingTeleport then
        Events.OnTick.Remove(Elevator._processPendingTeleport)
    end
    local phase = "waitForArrival"
    if args and args.performLocalTeleport == true then
        phase = "applyLocalTeleport"
    end

    Elevator._pendingTeleport = {
        playerObj = playerObj,
        playerNum = playerObj.getPlayerNum and playerObj:getPlayerNum() or 0,
        destination = destination,
        phase = phase,
        ticks = 0,
    }
    Events.OnTick.Add(Elevator._processPendingTeleport)
end

if Events and Events.OnServerCommand then
    Events.OnServerCommand.Add(onServerCommand)
end

-- === Client UX (fade + Interact key) ===

local function stopPendingTeleport()
    Elevator._pendingTeleport = nil
    if Events and Events.OnTick and Elevator._processPendingTeleport then
        Events.OnTick.Remove(Elevator._processPendingTeleport)
    end
end

runTeleportCallback = function(success)
    local fadeState = Elevator._fadeState
    if not fadeState then
        return
    end
    local callback = fadeState.completionCallback
    fadeState.completionCallback = nil
    if callback then
        callback(success == true)
    end
end

beginFadeIn = function()
    if Elevator._fadeState then
        Elevator._fadeState.waitTicks = 0
        Elevator._fadeState.phase = "fadeIn"
    end
end

local function finishFadeAfterTeleport()
    beginFadeIn()
end

local function isPlayerAtDestination(playerObj, destination)
    if EFZ.Teleport and EFZ.Teleport.isPlayerAtDestination then
        return EFZ.Teleport.isPlayerAtDestination(playerObj, destination)
    end

    if not playerObj or not destination then
        return false
    end
    return math.floor(playerObj:getX()) == math.floor(destination.x)
        and math.floor(playerObj:getY()) == math.floor(destination.y)
        and math.floor(playerObj:getZ()) == math.floor(destination.z)
end

local function removePlayerFromSquareLists(playerObj, square)
    if not playerObj or not square then
        return
    end
    square:getMovingObjects():remove(playerObj)
    square:getStaticMovingObjects():remove(playerObj)
end

local function finalizePlayerSquare(playerObj)
    if EFZ.Teleport and EFZ.Teleport.finishLocalTeleport then
        return EFZ.Teleport.finishLocalTeleport(playerObj)
    end

    local oldCurrent = playerObj:getCurrentSquare()
    local oldLast = playerObj.getLastSquare and playerObj:getLastSquare() or nil
    local oldSquare = playerObj.getSquare and playerObj:getSquare() or nil

    playerObj:setCurrentSquareFromPosition(playerObj:getX(), playerObj:getY(), playerObj:getZ())
    local square = playerObj:getCurrentSquare()
    if not square then
        return false
    end

    removePlayerFromSquareLists(playerObj, oldCurrent)
    removePlayerFromSquareLists(playerObj, oldLast)
    removePlayerFromSquareLists(playerObj, oldSquare)

    playerObj:setSquare(square)
    playerObj:setCurrent(square)
    if playerObj.setLast then
        playerObj:setLast(square)
    end
    playerObj:setMovingSquare(square)
    if not square:getStaticMovingObjects():contains(playerObj) then
        square:getStaticMovingObjects():add(playerObj)
    end
    playerObj:ensureOnTile()
    if playerObj.setSitOnFurnitureObject then
        playerObj:setSitOnFurnitureObject(nil)
    end
    if playerObj.setDefaultState then
        playerObj:setDefaultState()
    end
    if playerObj.resetModel then
        playerObj:resetModel()
    else
        playerObj:resetModelNextFrame()
    end
    return true
end

local function stopFade()
    Elevator._fadeState = nil
    stopPendingTeleport()
    if Events and Events.OnPostUIDraw and Elevator._updateFadeOverlay then
        Events.OnPostUIDraw.Remove(Elevator._updateFadeOverlay)
    end
end

Elevator._processPendingTeleport = function()
    local pending = Elevator._pendingTeleport
    if not pending then
        stopPendingTeleport()
        return
    end

    local playerObj = pending.playerObj or (getSpecificPlayer and getSpecificPlayer(pending.playerNum)) or (getPlayer and getPlayer()) or nil
    if not playerObj or (playerObj.isDead and playerObj:isDead()) then
        stopPendingTeleport()
        runTeleportCallback(false)
        finishFadeAfterTeleport()
        return
    end

    pending.ticks = (pending.ticks or 0) + 1

    if pending.phase == "applyLocalTeleport" then
        teleportPlayerCommon(playerObj, pending.destination)
        pending.phase = "waitForArrival"
        return
    end

    if pending.phase == "waitForArrival" and isPlayerAtDestination(playerObj, pending.destination) then
        pending.phase = "waitForSquare"
    end

    if pending.phase == "waitForSquare" and finalizePlayerSquare(playerObj) then
        stopPendingTeleport()
        runTeleportCallback(true)
        finishFadeAfterTeleport()
        return
    end

    if pending.ticks > 300 then
        logInfo(string.format("Teleport stabilization timed out at %.2f,%.2f,%.2f for %d,%d,%d",
            playerObj:getX(), playerObj:getY(), playerObj:getZ(),
            pending.destination.x, pending.destination.y, pending.destination.z))
        stopPendingTeleport()
        runTeleportCallback(false)
        beginFadeIn()
    end
end

local function queueLocalTeleport(playerObj, destination)
    local dest = normalizeDestination(destination)
    if not playerObj or not dest then
        return false
    end

    if not (Events and Events.OnTick) then
        local ok = teleportPlayerCommon(playerObj, dest)
        if ok and finalizePlayerSquare(playerObj) then
            runTeleportCallback(true)
            finishFadeAfterTeleport()
        else
            runTeleportCallback(false)
            beginFadeIn()
        end
        return ok
    end

    Elevator._pendingTeleport = {
        playerObj = playerObj,
        playerNum = playerObj.getPlayerNum and playerObj:getPlayerNum() or 0,
        destination = dest,
        phase = "applyLocalTeleport",
        ticks = 0,
    }
    if Elevator._processPendingTeleport then
        Events.OnTick.Remove(Elevator._processPendingTeleport)
    end
    Events.OnTick.Add(Elevator._processPendingTeleport)
    return true
end

Elevator._updateFadeOverlay = function()
    local fadeState = Elevator._fadeState
    if not fadeState then
        if Events and Events.OnPostUIDraw and Elevator._updateFadeOverlay then
            Events.OnPostUIDraw.Remove(Elevator._updateFadeOverlay)
        end
        return
    end

    local playerObj = fadeState.playerObj or (getSpecificPlayer and getSpecificPlayer(fadeState.playerNum)) or (getPlayer and getPlayer()) or nil
    if not playerObj then
        stopFade()
        return
    end

    local delta = fadeState.fadeSpeed or 0.05
    if fadeState.phase == "fadeOut" then
        fadeState.alpha = math.min(1, fadeState.alpha + delta)
        if fadeState.alpha >= 1 and (not fadeState.didRequest) then
            fadeState.didRequest = true
            -- MP: request server, then wait for ElevatorTeleport response.
            if isClient and isClient() and sendClientCommand then
                fadeState.phase = "waitServer"
                fadeState.waitTicks = 0
                sendTeleportRequestToServer(fadeState)
            else
                -- SP: execute on the next game tick, not during UI rendering.
                fadeState.phase = "waitLocal"
                fadeState.waitTicks = 0
                queueLocalTeleport(playerObj, fadeState.destination)
            end
        end
    elseif fadeState.phase == "waitServer" or fadeState.phase == "waitLocal" then
        fadeState.alpha = 1
        fadeState.waitTicks = (fadeState.waitTicks or 0) + 1
        if fadeState.waitTicks > 300 then
            -- timeout fallback: unblack the screen even if teleport failed
            fadeState.phase = "fadeIn"
        end
    elseif fadeState.phase == "fadeIn" then
        fadeState.alpha = math.max(0, fadeState.alpha - delta)
        if fadeState.alpha <= 0 then
            stopFade()
            return
        end
    else
        stopFade()
        return
    end

    if ISUIElement and ISUIElement.drawRectStatic and getCore then
        ISUIElement.drawRectStatic(0, 0, getCore():getScreenWidth(), getCore():getScreenHeight(), fadeState.alpha, 0, 0, 0)
    end
end

local function startFadeTeleport(playerObj, destination, options)
    if not playerObj or not destination then
        return
    end

    if Elevator._fadeState then
        return
    end

    logInfo(string.format("Starting fade teleport to %d,%d,%d (requestKind=%s).",
        math.floor(destination.x),
        math.floor(destination.y),
        math.floor(destination.z),
        tostring(options and options.requestKind or "elevator")))

    Elevator._fadeState = {
        playerObj = playerObj,
        playerNum = playerObj.getPlayerNum and playerObj:getPlayerNum() or 0,
        destination = destination,
        alpha = 0,
        phase = "fadeOut",
        fadeSpeed = 0.05,
        didRequest = false,
        waitTicks = 0,
        completionCallback = options and options.callback or nil,
        requestKind = options and options.requestKind or "elevator",
    }

    if not (Events and Events.OnPostUIDraw) then
        -- No UI draw hook: do the action immediately.
        if isClient and isClient() and sendClientCommand then
            sendTeleportRequestToServer(Elevator._fadeState)
        else
            local ok = teleportPlayerCommon(playerObj, destination)
            if ok and finalizePlayerSquare(playerObj) then
                runTeleportCallback(true)
            else
                runTeleportCallback(false)
            end
        end
        stopFade()
        return
    end

    Events.OnPostUIDraw.Add(Elevator._updateFadeOverlay)
end

-- Public API (optional): scripts can call EFZ.Elevator.requestTeleport() without dealing with networking.
function Elevator.requestTeleport(playerObj)
    local resolved = playerObj or (getPlayer and getPlayer()) or nil
    if not resolved or (resolved.isDead and resolved:isDead()) then
        return false
    end

    local destination, keyString = resolveDestinationForPlayer(resolved)
    if not destination then
        return false
    end

    logInfo(string.format("Elevator teleport requested from tile %s.", tostring(keyString)))
    startFadeTeleport(resolved, destination, { requestKind = "elevator" })
    return true
end

function Elevator.requestTeleportToDestination(playerObj, destination, options)
    local resolved = playerObj or (getPlayer and getPlayer()) or nil
    local dest = normalizeDestination(destination)
    if not resolved or (resolved.isDead and resolved:isDead()) or not dest then
        return false
    end

    local teleportOptions = options or {}
    teleportOptions.requestKind = teleportOptions.requestKind or "direct"
    startFadeTeleport(resolved, dest, teleportOptions)
    return true
end

-- Backwards compatible alias (if anything called EFZ.OpenElevator()).
function EFZ.OpenElevator(destination)
    local playerObj = getPlayer and getPlayer() or nil
    if not playerObj or not destination then
        return
    end
    startFadeTeleport(playerObj, destination)
end

local function shouldHandleElevatorInput()
    if isServer and isServer() and not (isClient and isClient()) then
        return false
    end
    return Events and Events.OnKeyPressed and Keyboard and Keyboard.KEY_E ~= nil
end

local function onElevatorKeyPressed(key)
    if key ~= Keyboard.KEY_E then
        return
    end

    local playerObj = getPlayer and getPlayer() or nil
    if not playerObj or (playerObj.isDead and playerObj:isDead()) then
        return
    end

    if Elevator.isTeleportInProgress() then
        return
    end

    local destination, keyString = resolveDestinationForPlayer(playerObj)
    if destination then
        Elevator.requestTeleport(playerObj)
        return
    end

    local nearElevatorKey = findNearElevatorTileKey(keyString)
    if nearElevatorKey then
        logInfo(string.format("Interact key pressed near elevator tile %s but player is at %s (%.2f,%.2f,%.2f).",
            tostring(nearElevatorKey),
            tostring(keyString),
            playerObj:getX(),
            playerObj:getY(),
            playerObj:getZ()))
    end
end

if shouldHandleElevatorInput() then
    Events.OnKeyPressed.Add(onElevatorKeyPressed)
    logInfo("Registered elevator Interact key handler (Keyboard.KEY_E).")
end

