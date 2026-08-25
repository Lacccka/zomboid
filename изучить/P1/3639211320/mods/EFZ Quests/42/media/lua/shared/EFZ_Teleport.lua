-- Shared teleport helper (B42.17 SP/MP)
-- Reuses one teleport flow from both client and server code.

if not EFZ then
    EFZ = {}
end

EFZ.Teleport = EFZ.Teleport or {}
local Teleport = EFZ.Teleport

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

local function applyArkStyleTeleport(playerObj, destination)
    local dest = normalizeDestination(destination)
    if (not playerObj) or (not dest) then
        return false
    end

    local x = tonumber(dest.x)
    local y = tonumber(dest.y)
    local z = math.floor(tonumber(dest.z) or 0)
    if x == nil or y == nil then
        return false
    end

    playerObj:StopAllActionQueue()
    if playerObj:getVehicle() then
        playerObj:ensureNotInVehicle()
    end
    if playerObj.setSitOnFurnitureObject then
        playerObj:setSitOnFurnitureObject(nil)
    end

    -- Use the engine teleport path so distant deploy targets can stream in after arrival.
    playerObj:teleportTo(x, y, z)
    playerObj:setForceX(x)
    playerObj:setForceY(y)

    local world = getWorld and getWorld() or nil
    if world and world.update then
        world:update()
    end

    return true
end

function Teleport.beginLocalTeleport(playerObj, destination)
    return applyArkStyleTeleport(playerObj, destination)
end

function Teleport.isPlayerAtDestination(playerObj, destination)
    local dest = normalizeDestination(destination)
    if (not playerObj) or (not dest) then
        return false
    end
    return math.floor(playerObj:getX()) == math.floor(dest.x)
        and math.floor(playerObj:getY()) == math.floor(dest.y)
        and math.floor(playerObj:getZ()) == dest.z
end

function Teleport.finishLocalTeleport(playerObj)
    return playerObj ~= nil
end

Teleport._pendingLocalTeleports = Teleport._pendingLocalTeleports or {}
Teleport._localTeleportTickerAdded = Teleport._localTeleportTickerAdded or false

local function removePendingLocalTeleport(playerNum)
    Teleport._pendingLocalTeleports[playerNum] = nil
end

local function ensureLocalTeleportTicker()
    if Teleport._localTeleportTickerAdded then
        return true
    end
    if not Events or not Events.OnTick or type(Events.OnTick.Add) ~= "function" or type(Events.OnTick.Remove) ~= "function" then
        return false
    end
    Events.OnTick.Add(Teleport._processPendingLocalTeleports)
    Teleport._localTeleportTickerAdded = true
    return true
end

local function disableLocalTeleportTickerIfIdle()
    if not Teleport._localTeleportTickerAdded then
        return
    end

    local hasPending = false
    for _, _ in pairs(Teleport._pendingLocalTeleports) do
        hasPending = true
        break
    end

    if not hasPending and Events and Events.OnTick and type(Events.OnTick.Remove) == "function" then
        Events.OnTick.Remove(Teleport._processPendingLocalTeleports)
        Teleport._localTeleportTickerAdded = false
    end
end

function Teleport._processPendingLocalTeleports()
    local completed = {}
    local callbacks = {}

    for playerNum, pending in pairs(Teleport._pendingLocalTeleports) do
        local playerObj = pending.playerObj or (getSpecificPlayer and getSpecificPlayer(playerNum)) or (getPlayer and getPlayer()) or nil
        if not playerObj or (playerObj.isDead and playerObj:isDead()) then
            completed[#completed + 1] = playerNum
            callbacks[#callbacks + 1] = { callback = pending.callback, success = false }
        else
            pending.ticks = (pending.ticks or 0) + 1

            if pending.phase == "applyLocalTeleport" then
                if Teleport.beginLocalTeleport(playerObj, pending.destination) then
                    pending.phase = "waitForArrival"
                else
                    completed[#completed + 1] = playerNum
                    callbacks[#callbacks + 1] = { callback = pending.callback, success = false }
                end
            elseif pending.phase == "waitForArrival" then
                if Teleport.isPlayerAtDestination(playerObj, pending.destination) then
                    pending.phase = "waitForSquare"
                elseif pending.ticks > 300 then
                    print(string.format("[EFZ Teleport] Local teleport arrival timed out for %d at %.2f,%.2f,%.2f.",
                        playerNum,
                        playerObj:getX(),
                        playerObj:getY(),
                        playerObj:getZ()))
                    completed[#completed + 1] = playerNum
                    callbacks[#callbacks + 1] = { callback = pending.callback, success = false }
                end
            elseif pending.phase == "waitForSquare" then
                if Teleport.finishLocalTeleport(playerObj) then
                    completed[#completed + 1] = playerNum
                    callbacks[#callbacks + 1] = { callback = pending.callback, success = true }
                elseif pending.ticks > 300 then
                    print(string.format("[EFZ Teleport] Local teleport stabilization timed out for %d.", playerNum))
                    completed[#completed + 1] = playerNum
                    callbacks[#callbacks + 1] = { callback = pending.callback, success = false }
                end
            end
        end
    end

    for i = 1, #completed do
        removePendingLocalTeleport(completed[i])
    end

    disableLocalTeleportTickerIfIdle()

    for i = 1, #callbacks do
        local entry = callbacks[i]
        if entry.callback then
            entry.callback(entry.success)
        end
    end
end

function Teleport.requestLocalTeleport(playerObj, destination, callback)
    local dest = normalizeDestination(destination)
    if (not playerObj) or (not dest) then
        if callback then
            callback(false)
        end
        return false
    end

    local playerNum = playerObj.getPlayerNum and playerObj:getPlayerNum() or 0
    if not ensureLocalTeleportTicker() then
        local success = Teleport.beginLocalTeleport(playerObj, dest) and Teleport.finishLocalTeleport(playerObj)
        if callback then
            callback(success)
        end
        return success
    end

    Teleport._pendingLocalTeleports[playerNum] = {
        playerObj = playerObj,
        destination = dest,
        callback = callback,
        phase = "applyLocalTeleport",
        ticks = 0,
    }
    return true
end

-- Shared teleport helper for B42.17.
-- Returns: success (boolean)
function Teleport.teleportPlayer(playerObj, destination)
    if isServer and isServer() and GameServer and GameServer.sendTeleport and playerObj and playerObj.getOnlineID and playerObj:getOnlineID() ~= nil then
        local dest = normalizeDestination(destination)
        if not dest then
            return false
        end
        GameServer.sendTeleport(playerObj, dest.x, dest.y, dest.z)
        return true
    end

    return applyArkStyleTeleport(playerObj, destination)
end


