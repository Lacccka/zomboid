require "ExtractionMode/Config"

ExtractionMode = ExtractionMode or {}

local Config = ExtractionMode.Config
local Compatibility = {}

Compatibility.MOD_COMMON_SENSE = "VB_CommonSense"
Compatibility.MOD_EXTENDED_PLUMBING = "LGExtendedPlumbing"
Compatibility.MOD_EXTENDED_ELECTRICITY = "LGExtendedElectricity"
Compatibility.MOD_PROJECT_RV_INTERIOR = "PROJECTRVInterior42"
Compatibility.MOD_TRUE_COMPANIONS = "TrueCompanions"

local confirmedActive = {}

function Compatibility.isActive(modId)
    modId = tostring(modId or "")
    if modId == "" then return false end
    if confirmedActive[modId] == true then return true end
    local mods = getActivatedMods and getActivatedMods() or nil
    if mods == nil then return false end
    for index = 0, mods:size() - 1 do
        local activeId = tostring(mods:get(index) or ""):gsub("^\\", "")
        if activeId == modId then
            confirmedActive[modId] = true
            return true
        end
    end
    -- Do not cache a negative result. Some Build 42 environments populate the
    -- activated-mod list after shared Lua has already loaded.
    return false
end

function Compatibility.isCommonSenseActive()
    return Compatibility.isActive(Compatibility.MOD_COMMON_SENSE)
end

function Compatibility.isExtendedPlumbingActive()
    return Compatibility.isActive(Compatibility.MOD_EXTENDED_PLUMBING)
end

function Compatibility.isExtendedElectricityActive()
    return Compatibility.isActive(Compatibility.MOD_EXTENDED_ELECTRICITY)
end

function Compatibility.isProjectRVInteriorActive()
    return Compatibility.isActive(Compatibility.MOD_PROJECT_RV_INTERIOR)
end

function Compatibility.isTrueCompanionsActive()
    return Compatibility.isActive(Compatibility.MOD_TRUE_COMPANIONS)
end

function Compatibility.isProtectedHideoutObject(object)
    if object == nil then return false end
    local square = nil
    pcall(function() square = object:getSquare() end)
    if Config.isHideoutGarageDoorSquare(square)
        or Config.isHideoutGarageProtectedSquare(square) then return true end
    local protected = false
    pcall(function()
        local modData = object:getModData()
        protected = modData ~= nil and modData.ExtractionModeIndestructible == true
    end)
    return protected
end

function Compatibility.extendedPlumbingOwnsFixture(object)
    if object == nil or not Compatibility.isExtendedPlumbingActive() then return false end
    local owned = false
    pcall(function()
        local modData = object:getModData()
        owned = modData ~= nil and modData.LGEPPiped == true
    end)
    return owned
end

local function rvData()
    if not Compatibility.isProjectRVInteriorActive()
        or ModData == nil or ModData.getOrCreate == nil then return nil end
    local data = nil
    pcall(function() data = ModData.getOrCreate("modPROJECTRVInterior") end)
    return data
end

local function vehicleRVId(vehicle)
    if vehicle == nil or not Compatibility.isProjectRVInteriorActive() then return nil end
    local value = nil
    pcall(function() value = vehicle:getModData().projectRV_uniqueId end)
    if value == nil or tostring(value) == "" then return nil end
    return tostring(value)
end

local function playerRVRecord(player)
    local data = rvData()
    if data == nil or player == nil then return nil, data end
    local playerId = nil
    pcall(function() playerId = player:getModData().projectRV_playerId end)
    if playerId == nil or type(data.Players) ~= "table" then return nil, data end
    return data.Players[playerId] or data.Players[tostring(playerId)], data
end

local function playerStillInsideAssignedRoom(player, record)
    if player == nil or type(record) ~= "table" or type(record.ActualRoom) ~= "table" then return false end
    local roomX = tonumber(record.ActualRoom.x)
    local roomY = tonumber(record.ActualRoom.y)
    if roomX == nil or roomY == nil then return false end
    local x, y = nil, nil
    pcall(function() x, y = tonumber(player:getX()), tonumber(player:getY()) end)
    -- The largest current RV layouts fit comfortably inside this allowance. It
    -- also prevents a stale RV assignment from masking an Extraction teleport.
    return x ~= nil and y ~= nil and math.abs(x - roomX) <= 32 and math.abs(y - roomY) <= 32
end

local function findExteriorVehicle(rvId)
    if rvId == nil then return nil end
    local cell = getCell and getCell() or nil
    local vehicles = cell and cell:getVehicles() or nil
    if vehicles == nil then return nil end
    for index = 0, vehicles:size() - 1 do
        local vehicle = vehicles:get(index)
        if vehicleRVId(vehicle) == tostring(rvId) then return vehicle end
    end
    return nil
end

function Compatibility.projectRVVehicleId(vehicle)
    return vehicleRVId(vehicle)
end

function Compatibility.isPlayerInsideRVInterior(player)
    if not Compatibility.isProjectRVInteriorActive() then return false end
    local record = playerRVRecord(player)
    return playerStillInsideAssignedRoom(player, record)
end

function Compatibility.effectivePlayerPosition(player)
    if not Compatibility.isProjectRVInteriorActive() then return nil end
    local record, data = playerRVRecord(player)
    if not playerStillInsideAssignedRoom(player, record) or record.VehicleId == nil then return nil end
    local rvId = tostring(record.VehicleId)
    local vehicle = findExteriorVehicle(rvId)
    if vehicle ~= nil then
        local point = { x = vehicle:getX(), y = vehicle:getY(), z = vehicle:getZ() }
        data.Vehicles = type(data.Vehicles) == "table" and data.Vehicles or {}
        data.Vehicles[rvId] = { x = point.x, y = point.y, z = point.z }
        return point, vehicle
    end
    local stored = type(data.Vehicles) == "table" and data.Vehicles[rvId] or nil
    if type(stored) ~= "table" then return nil end
    local x, y, z = tonumber(stored.x), tonumber(stored.y), tonumber(stored.z) or 0
    if x == nil or y == nil then return nil end
    return { x = x, y = y, z = z }, nil
end

function Compatibility.playerPosition(player)
    local effective = Compatibility.effectivePlayerPosition(player)
    if effective ~= nil then return effective end
    if player == nil then return nil end
    return { x = player:getX(), y = player:getY(), z = player:getZ() }
end

function Compatibility.hasRemoteRVOccupants(vehicle, players)
    local rvId = vehicleRVId(vehicle)
    if rvId == nil then return false, {} end
    local occupants = {}
    for _, player in ipairs(players or {}) do
        local record = playerRVRecord(player)
        if playerStillInsideAssignedRoom(player, record)
            and tostring(record.VehicleId or "") == rvId then
            occupants[#occupants + 1] = player
        end
    end
    return #occupants > 0, occupants
end

function Compatibility.captureVehicleMetadata(vehicle)
    local rvId = vehicleRVId(vehicle)
    if rvId == nil then return nil end
    local rvType = nil
    pcall(function() rvType = vehicle:getModData().projectRV_type end)
    return {
        projectRVInterior = {
            uniqueId = rvId,
            roomType = rvType,
        },
    }
end

function Compatibility.updateProjectRVVehiclePosition(vehicle)
    local rvId = vehicleRVId(vehicle)
    local data = rvData()
    if rvId == nil or data == nil then return false end
    data.Vehicles = type(data.Vehicles) == "table" and data.Vehicles or {}
    data.Vehicles[rvId] = {
        x = vehicle:getX(), y = vehicle:getY(), z = vehicle:getZ(),
    }
    return true
end

function Compatibility.restoreVehicleMetadata(vehicle, metadata)
    if vehicle == nil or not Compatibility.isProjectRVInteriorActive()
        or type(metadata) ~= "table" then return false end
    local rv = metadata.projectRVInterior
    if type(rv) ~= "table" or rv.uniqueId == nil or tostring(rv.uniqueId) == "" then return false end
    local restored = pcall(function()
        local modData = vehicle:getModData()
        modData.projectRV_uniqueId = tostring(rv.uniqueId)
        if rv.roomType ~= nil then modData.projectRV_type = rv.roomType end
        if vehicle.transmitModData then vehicle:transmitModData() end
    end)
    if restored then Compatibility.updateProjectRVVehiclePosition(vehicle) end
    return restored
end

ExtractionMode.ModCompatibility = Compatibility
return Compatibility
