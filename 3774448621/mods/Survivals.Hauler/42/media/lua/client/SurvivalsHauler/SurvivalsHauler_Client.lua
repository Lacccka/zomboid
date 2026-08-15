require "Vehicles/ISUI/ISCarMechanicsOverlay"
require "Vehicles/ISUI/ISVehicleSeatUI"
require "Vehicles/ISUI/ISVehicleMenu"
require "Vehicles/TimedActions/ISAttachTrailerToVehicle"
require "Vehicles/ISVehicleTrailerUtils"

print("[SurvivalsHauler] client lua loaded")

SurvivalsHauler = SurvivalsHauler or {}

local HAULER_LOAD_ICON = "media/textures/icons/HaulerLoadVehicle.png"
local HAULER_UNLOAD_ICON = "media/textures/icons/HaulerUnloadVehicle.png"
local HAULER_LOAD_ICON_FALLBACK = "media/ui/vehicles/vehicle_changeseats.png"
local HAULER_UNLOAD_ICON_FALLBACK = "media/ui/ZoomOut.png"

local function getHaulerMenuIcon(path, fallback)
    return getTexture(path) or getTexture(fallback)
end

local function isHauler(vehicle)
    if not vehicle then
        return false
    end

    local scriptName = vehicle:getScriptName()
    return scriptName == "Base.TrailerSurvivalsHauler"
        or scriptName == "TrailerSurvivalsHauler"
        or scriptName == "Base.SurvivalsHauler"
        or scriptName == "SurvivalsHauler"
end

local function isHaulerDisplayVehicle(vehicle)
    return vehicle
        and vehicle.getModData
        and vehicle:getModData().SurvivalsHaulerDisplayFor ~= nil
end

local DISPLAY_SLOT_OFFSETS = {
    [1] = { x = 0.00, y = 3.60 },
    [2] = { x = 0.00, y = 0.40 },
    [3] = { x = 0.00, y = -2.80 },
    [4] = { x = -0.55, y = 2.00 },
    [5] = { x = -0.55, y = -1.20 },
    [6] = { x = -0.55, y = -4.40 },
}

local function getVehicleByIdSafe(vehicleId)
    if vehicleId and getVehicleById then
        return getVehicleById(tonumber(vehicleId))
    end

    return nil
end

local function getVehicleYawAngle(vehicle)
    local angle = 0

    if vehicle and vehicle.getAngleY then
        angle = tonumber(vehicle:getAngleY()) or 0
    elseif vehicle and vehicle.getAngleZ then
        angle = tonumber(vehicle:getAngleZ()) or 0
    end

    if math.abs(angle) > 6.28319 then
        angle = math.rad(angle)
    end

    return angle
end

local function copyVehicleAngles(targetVehicle, sourceVehicle)
    if not targetVehicle or not sourceVehicle or not targetVehicle.setAngles then
        return
    end

    if sourceVehicle.getAngleX and sourceVehicle.getAngleY and sourceVehicle.getAngleZ then
        pcall(function()
            targetVehicle:setAngles(sourceVehicle:getAngleX(), sourceVehicle:getAngleY(), sourceVehicle:getAngleZ())
        end)
    else
        local yaw = getVehicleYawAngle(sourceVehicle)
        pcall(function()
            targetVehicle:setAngles(0, yaw, 0)
        end)
    end
end

local function getDisplayVehiclePosition(hauler, slot)
    local offset = DISPLAY_SLOT_OFFSETS[tonumber(slot)] or DISPLAY_SLOT_OFFSETS[1]
    local angle = getVehicleYawAngle(hauler)

    local sin = math.sin(angle)
    local cos = math.cos(angle)
    local x = hauler:getX() + offset.x * cos - offset.y * sin
    local y = hauler:getY() + offset.x * sin + offset.y * cos

    return x, y, hauler:getZ()
end

local function pinDisplayVehiclePosition(displayVehicle, x, y, z)
    if not displayVehicle then
        return
    end

    pcall(function() displayVehicle:setX(x) end)
    pcall(function() displayVehicle:setY(y) end)
    pcall(function() displayVehicle:setZ(z) end)
    pcall(function() displayVehicle:setForceX(x) end)
    pcall(function() displayVehicle:setForceY(y) end)
    pcall(function() displayVehicle:setLastX(x) end)
    pcall(function() displayVehicle:setLastY(y) end)
    pcall(function() displayVehicle:setLastZ(z) end)
    pcall(function() displayVehicle:setNextX(x) end)
    pcall(function() displayVehicle:setNextY(y) end)
    pcall(function() displayVehicle:setScriptnx(x) end)
    pcall(function() displayVehicle:setScriptny(y) end)

    local square = getCell():getGridSquare(math.floor(x), math.floor(y), math.floor(z))
    if square then
        pcall(function() displayVehicle:setSquare(square) end)
        pcall(function() displayVehicle:setCurrent(square) end)
        pcall(function() displayVehicle:setCurrentSquare(square) end)
        pcall(function() displayVehicle:setMovingSquare(square) end)
        pcall(function() displayVehicle:setMovingSquareNow() end)
    end
end

local function freezeDisplayVehicle(displayVehicle)
    if not displayVehicle then
        return
    end

    pcall(function() displayVehicle:setPhysicsActive(false) end)
    pcall(function() displayVehicle:setActiveInBullet(false) end)
    pcall(function() displayVehicle:setCollidable(false) end)
    pcall(function() displayVehicle:setSolid(false) end)
    pcall(function() displayVehicle:setShootable(false) end)
    pcall(function() displayVehicle:setNoPicking(true) end)
    pcall(function() displayVehicle:setMass(0.001) end)
    pcall(function() displayVehicle:setInitialMass(0.001) end)
    pcall(function() displayVehicle:setDebugZ(0) end)
end

local function freezeHaulerDisplayVehicles()
    if not getCell then
        return
    end

    local cell = getCell()
    if not cell or not cell.getVehicles then
        return
    end

    local okVehicles, vehicles = pcall(function()
        return cell:getVehicles()
    end)
    if not okVehicles or not vehicles or not vehicles.size or not vehicles.get then
        return
    end

    for index = 0, vehicles:size() - 1 do
        local okVehicle, vehicle = pcall(function()
            return vehicles:get(index)
        end)
        if okVehicle and isHaulerDisplayVehicle(vehicle) then
            local modData = vehicle:getModData()
            local hauler = getVehicleByIdSafe(modData.SurvivalsHaulerDisplayFor)
            if hauler then
                local x, y, z = getDisplayVehiclePosition(hauler, modData.SurvivalsHaulerDisplaySlot)
                pinDisplayVehiclePosition(vehicle, x, y, z)
                copyVehicleAngles(vehicle, hauler)
            end
            freezeDisplayVehicle(vehicle)
        end
    end
end

local function logTowChecks(vehicle)
    if not vehicle or not vehicle.getSquare then
        return
    end

    local square = vehicle:getSquare()
    if not square then
        return
    end

    local checks = {
        { "trailer", "trailer" },
        { "trailer", "trailerfront" },
        { "trailerfront", "trailer" },
        { "trailerfront", "trailerfront" },
    }

    for y = square:getY() - 6, square:getY() + 6 do
        for x = square:getX() - 6, square:getX() + 6 do
            local square2 = getCell():getGridSquare(x, y, square:getZ())
            if square2 then
                for i = 1, square2:getMovingObjects():size() do
                    local other = square2:getMovingObjects():get(i - 1)
                    if instanceof(other, "BaseVehicle") and other ~= vehicle and not isHaulerDisplayVehicle(other) and (isHauler(vehicle) or isHauler(other)) then
                        print(string.format("[SurvivalsHauler] tow probe: %s -> %s", vehicle:getScriptName(), other:getScriptName()))
                        for _, pair in ipairs(checks) do
                            local ok = vehicle:canAttachTrailer(other, pair[1], pair[2])
                            print(string.format("[SurvivalsHauler]   %s -> %s = %s", pair[1], pair[2], tostring(ok)))
                        end
                    end
                end
            end
        end
    end
end

local function getNearbyHaulerTowPair(vehicle)
    if not vehicle or not vehicle.getSquare then
        return nil
    end

    local square = vehicle:getSquare()
    if not square then
        return nil
    end

    local checks = {
        { "trailer", "trailer" },
        { "trailer", "trailerfront" },
        { "trailerfront", "trailer" },
        { "trailerfront", "trailerfront" },
    }
    for y = square:getY() - 20, square:getY() + 20 do
        for x = square:getX() - 20, square:getX() + 20 do
            local square2 = getCell():getGridSquare(x, y, square:getZ())
            if square2 then
                for i = 1, square2:getMovingObjects():size() do
                    local other = square2:getMovingObjects():get(i - 1)
                    if instanceof(other, "BaseVehicle") and other ~= vehicle and not isHaulerDisplayVehicle(other) and (isHauler(vehicle) or isHauler(other)) then
                        print(string.format("[SurvivalsHauler] wide tow candidate: %s at %d,%d -> %s at %d,%d", vehicle:getScriptName(), square:getX(), square:getY(), other:getScriptName(), square2:getX(), square2:getY()))
                        for _, pair in ipairs(checks) do
                            local posA = vehicle:getTowingWorldPos(pair[1], Vector3f.new())
                            local posB = other:getTowingWorldPos(pair[2], Vector3f.new())
                            if posA and posB then
                                print(string.format("[SurvivalsHauler]   pos %s:%s %.3f,%.3f,%.3f -> %s:%s %.3f,%.3f,%.3f dist=%.3f",
                                    vehicle:getScriptName(), pair[1], posA:x(), posA:y(), posA:z(),
                                    other:getScriptName(), pair[2], posB:x(), posB:y(), posB:z(),
                                    math.sqrt((posA:x() - posB:x()) ^ 2 + (posA:y() - posB:y()) ^ 2 + (posA:z() - posB:z()) ^ 2)))
                            else
                                print(string.format("[SurvivalsHauler]   pos %s:%s or %s:%s is nil", vehicle:getScriptName(), pair[1], other:getScriptName(), pair[2]))
                            end
                            local ok = vehicle:canAttachTrailer(other, pair[1], pair[2])
                            print(string.format("[SurvivalsHauler]   wide %s -> %s = %s", pair[1], pair[2], tostring(ok)))
                            if ok then
                                local towVehicle = vehicle
                                local trailerVehicle = other
                                local attachmentA = pair[1]
                                local attachmentB = pair[2]
                                if isHauler(vehicle) and not isHauler(other) then
                                    towVehicle = other
                                    trailerVehicle = vehicle
                                    attachmentA = pair[2]
                                    attachmentB = pair[1]
                                end
                                return towVehicle, trailerVehicle, attachmentA, attachmentB
                            end
                        end
                    end
                end
            end
        end
    end

    return nil
end

local function forceAttachHauler(playerObj, towVehicle, trailerVehicle, attachmentA, attachmentB)
    if not towVehicle or not trailerVehicle then
        return
    end

    print(string.format("[SurvivalsHauler] force attach %s:%s -> %s:%s", towVehicle:getScriptName(), attachmentA, trailerVehicle:getScriptName(), attachmentB))
    sendClientCommand(playerObj, "vehicle", "attachTrailer", {
        vehicleA = towVehicle:getId(),
        vehicleB = trailerVehicle:getId(),
        attachmentA = attachmentA,
        attachmentB = attachmentB,
    })
end

local function addForceAttachSlice(playerObj, vehicle, menu)
    if not vehicle or vehicle:getVehicleTowing() or vehicle:getVehicleTowedBy() then
        return
    end

    local towVehicle, trailerVehicle, attachmentA, attachmentB = getNearbyHaulerTowPair(vehicle)
    if towVehicle and trailerVehicle then
        menu:addSlice("Force Attach Hauler", getTexture("media/ui/ZoomIn.png"), forceAttachHauler, playerObj, towVehicle, trailerVehicle, attachmentA, attachmentB)
    end
end

local MAX_LOAD_SLOTS = 6
local LOAD_SEARCH_RADIUS = 20

local function getLoadedVehicles(hauler)
    local modData = hauler:getModData()
    modData.SurvivalsHaulerLoadedVehicles = modData.SurvivalsHaulerLoadedVehicles or {}
    return modData.SurvivalsHaulerLoadedVehicles
end

local function getFirstFreeSlot(hauler)
    local loaded = getLoadedVehicles(hauler)

    for slot = 1, MAX_LOAD_SLOTS do
        local occupied = false
        for _, entry in pairs(loaded) do
            if entry and tonumber(entry.slot) == slot then
                occupied = true
                break
            end
        end

        if not occupied then
            return slot
        end
    end

    return nil
end

local function vehicleDisplayName(vehicle)
    if not vehicle then
        return "Vehicle"
    end

    local script = vehicle:getScript()
    if script and script.getName then
        return script:getName()
    end

    return tostring(vehicle:getScriptName() or "Vehicle")
end

local function getVehicleLocalOffset(baseVehicle, targetVehicle)
    local dx = targetVehicle:getX() - baseVehicle:getX()
    local dy = targetVehicle:getY() - baseVehicle:getY()
    local angle = getVehicleYawAngle(baseVehicle)
    local sin = math.sin(angle)
    local cos = math.cos(angle)

    return dx * cos + dy * sin, -dx * sin + dy * cos
end

local function isLikelyTowVehicleAtHitch(hauler, vehicle)
    local localX, localY = getVehicleLocalOffset(hauler, vehicle)

    return math.abs(localX) <= 3.5 and localY >= 7.0 and localY <= 13.0
end

local function isLoadCandidate(hauler, vehicle)
    if not vehicle or vehicle == hauler or not instanceof(vehicle, "BaseVehicle") or isHauler(vehicle) or isHaulerDisplayVehicle(vehicle) then
        return false
    end

    if vehicle:getVehicleTowedBy() or vehicle:getVehicleTowing() then
        return false
    end

    if hauler:getVehicleTowedBy() == vehicle or hauler:getVehicleTowing() == vehicle then
        return false
    end

    if isLikelyTowVehicleAtHitch(hauler, vehicle) then
        return false
    end

    local modData = vehicle:getModData()
    return not modData.SurvivalsHaulerLoadedOn
end

local function getNearestLoadCandidate(hauler)
    local square = hauler:getSquare() or hauler:getCurrentSquare()
    if not square then
        return nil
    end

    local bestVehicle = nil
    local bestDistanceSq = nil

    for y = square:getY() - LOAD_SEARCH_RADIUS, square:getY() + LOAD_SEARCH_RADIUS do
        for x = square:getX() - LOAD_SEARCH_RADIUS, square:getX() + LOAD_SEARCH_RADIUS do
            local square2 = getCell():getGridSquare(x, y, square:getZ())
            if square2 then
                for i = 1, square2:getMovingObjects():size() do
                    local other = square2:getMovingObjects():get(i - 1)
                    if isLoadCandidate(hauler, other) then
                        local dx = other:getX() - hauler:getX()
                        local dy = other:getY() - hauler:getY()
                        local distanceSq = dx * dx + dy * dy
                        if not bestDistanceSq or distanceSq < bestDistanceSq then
                            bestVehicle = other
                            bestDistanceSq = distanceSq
                        end
                    end
                end
            end
        end
    end

    return bestVehicle
end

local function loadNearestVehicle(playerObj, hauler, cargoVehicle, slot)
    if not hauler or not cargoVehicle or not slot then
        return
    end

    print(string.format("[SurvivalsHauler] request load %s into slot %s on %s",
        tostring(cargoVehicle:getScriptName()), tostring(slot), tostring(hauler:getId())))

    sendClientCommand("SurvivalsHauler", "loadVehicle", {
        haulerId = hauler:getId(),
        vehicleId = cargoVehicle:getId(),
        slot = slot,
    })
end

local function unloadVehicleSlot(playerObj, hauler, slot)
    if not hauler or not slot then
        return
    end

    print(string.format("[SurvivalsHauler] request unload slot %s from %s", tostring(slot), tostring(hauler:getId())))

    sendClientCommand("SurvivalsHauler", "unloadSlot", {
        haulerId = hauler:getId(),
        slot = slot,
    })
end

local function syncCargoProxies(playerObj, hauler)
    if not playerObj or not hauler then
        return
    end

    sendClientCommand("SurvivalsHauler", "syncCargoProxies", {
        haulerId = hauler:getId(),
    })
end

local function addHaulerLoadSlices(playerObj, vehicle, menu)
    if not isHauler(vehicle) then
        return
    end

    syncCargoProxies(playerObj, vehicle)

    local slot = getFirstFreeSlot(vehicle)
    if slot then
        local cargoVehicle = getNearestLoadCandidate(vehicle)
        if cargoVehicle then
            local text = "Load " .. vehicleDisplayName(cargoVehicle) .. " [" .. tostring(slot) .. "]"
            menu:addSlice(text, getHaulerMenuIcon(HAULER_LOAD_ICON, HAULER_LOAD_ICON_FALLBACK), loadNearestVehicle, playerObj, vehicle, cargoVehicle, slot)
        else
            menu:addSlice("No Vehicle To Load", getHaulerMenuIcon(HAULER_LOAD_ICON, HAULER_LOAD_ICON_FALLBACK), nil)
        end
    else
        menu:addSlice("Hauler Full", getHaulerMenuIcon(HAULER_LOAD_ICON, HAULER_LOAD_ICON_FALLBACK), nil)
    end

    local loaded = getLoadedVehicles(vehicle)
    for _, entry in pairs(loaded) do
        if entry and entry.slot then
            local name = entry.name or entry.script or "Vehicle"
            local text = "Unload " .. tostring(entry.slot) .. ": " .. tostring(name)
            menu:addSlice(text, getHaulerMenuIcon(HAULER_UNLOAD_ICON, HAULER_UNLOAD_ICON_FALLBACK), unloadVehicleSlot, playerObj, vehicle, tonumber(entry.slot))
        end
    end
end

local function applySurvivalsHaulerClientPatch()
    ISCarMechanicsOverlay.CarList["Base.TrailerSurvivalsHauler"] = ISCarMechanicsOverlay.CarList["Base.TrailerAdvert"] or ISCarMechanicsOverlay.CarList["Base.Trailer"]
    ISCarMechanicsOverlay.CarList["Base.SurvivalsHauler"] = ISCarMechanicsOverlay.CarList["Base.TrailerAdvert"] or ISCarMechanicsOverlay.CarList["Base.Trailer"]

    SeatOffsetX["Base.TrailerSurvivalsHauler"] = SeatOffsetX["Base.TrailerAdvert"] or SeatOffsetX["Base.Trailer"] or 0
    SeatOffsetY["Base.TrailerSurvivalsHauler"] = SeatOffsetY["Base.TrailerAdvert"] or SeatOffsetY["Base.Trailer"] or 0
    SeatOffsetX["Base.SurvivalsHauler"] = SeatOffsetX["Base.TrailerAdvert"] or SeatOffsetX["Base.Trailer"] or 0
    SeatOffsetY["Base.SurvivalsHauler"] = SeatOffsetY["Base.TrailerAdvert"] or SeatOffsetY["Base.Trailer"] or 0

    if SurvivalsHauler.towingMenuPatched or not ISVehicleMenu or not ISVehicleMenu.doTowingMenu then
        return
    end

    local originalDoTowingMenu = ISVehicleMenu.doTowingMenu
    ISVehicleMenu.doTowingMenu = function(playerObj, vehicle, menu)
        if vehicle then
            print("[SurvivalsHauler] doTowingMenu for " .. tostring(vehicle:getScriptName()))
        end
        pcall(logTowChecks, vehicle)
        pcall(addForceAttachSlice, playerObj, vehicle, menu)
        pcall(addHaulerLoadSlices, playerObj, vehicle, menu)
        return originalDoTowingMenu(playerObj, vehicle, menu)
    end

    SurvivalsHauler.towingMenuPatched = true
    print("[SurvivalsHauler] client patch applied")
end

Events.OnGameStart.Add(applySurvivalsHaulerClientPatch)
Events.OnCreatePlayer.Add(applySurvivalsHaulerClientPatch)
applySurvivalsHaulerClientPatch()
