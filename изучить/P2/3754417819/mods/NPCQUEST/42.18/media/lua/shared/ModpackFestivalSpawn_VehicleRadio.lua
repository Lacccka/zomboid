-- Restless Dreams FM: channel registration + default vehicle radio preset (on, 88.880, half volume).

ModpackFestivalVehicleRadio = ModpackFestivalVehicleRadio or {}

local VR = ModpackFestivalVehicleRadio
local MOD_ID = "ModpackFestivalSpawn"

VR.CHANNEL = 88880
VR.CHANNEL_UUID = "d1f5a8c3-2e94-4b7d-a6f0-9c3e7b2d4a81"
VR.CHANNEL_NAME = "Restless Dreams"
-- Same scale as festival radios (full = 30). Boost car radio output by +50%.
VR.VOLUME_MULT = 1.5
VR.DEFAULT_VOLUME = 19
VR.PRESET_FLAG = "restlessRadioPreset"
VR.spawnQueue = VR.spawnQueue or {}
VR.SPAWN_QUEUE_MAX_PER_TICK = 8

-- Stable BWORadio cache key (never use x-y-z; moving cars restarted music every tick).
function VR.getRestlessCacheId(vehicle)
    if not vehicle then
        return nil
    end
    if vehicle.getId then
        local ok, vid = pcall(function()
            return vehicle:getId()
        end)
        if ok and vid ~= nil then
            return "veh-" .. tostring(vid)
        end
    end
    return "vehpos-" .. tostring(vehicle:getX()) .. "-" .. tostring(vehicle:getY())
        .. "-" .. tostring(vehicle:getZ() or 0)
end

function VR.getVehicleModData(vehicle)
    if not vehicle or not vehicle.getModData then
        return nil
    end
    local md = vehicle:getModData()
    md.ModpackFestivalSpawn = md.ModpackFestivalSpawn or {}
    return md.ModpackFestivalSpawn
end

function VR.hasPreset(vehicle)
    local md = VR.getVehicleModData(vehicle)
    return md and md[VR.PRESET_FLAG] == true
end

function VR.findRadioDeviceData(vehicle)
    if not vehicle or not vehicle.getPartById then
        return nil, nil
    end
    local partIds = { "Radio", "HamRadio" }
    for i = 1, #partIds do
        local part = vehicle:getPartById(partIds[i])
        if part and part.getDeviceData then
            local deviceData = part:getDeviceData()
            if deviceData then
                return part, deviceData
            end
        end
    end
    local parts = vehicle:getParts()
    if not parts then
        return nil, nil
    end
    for pi = 0, parts:size() - 1 do
        local part = parts:get(pi)
        if part and part.getDeviceData then
            local deviceData = part:getDeviceData()
            if deviceData and deviceData.isVehicleDevice and deviceData:isVehicleDevice() then
                return part, deviceData
            end
        end
    end
    return nil, nil
end

function VR.isRadioPoweredOn(deviceData)
    if not deviceData then
        return false
    end
    if deviceData.getIsTurnedOn and not deviceData:getIsTurnedOn() then
        return false
    end
    if deviceData.getPower and (deviceData:getPower() or 0) <= 0 then
        return false
    end
    return true
end

function VR.tuneChannelOnly(deviceData)
    if not VR.isRadioPoweredOn(deviceData) then
        return false
    end
    if deviceData.setChannel then
        pcall(function()
            deviceData:setChannel(VR.CHANNEL, true)
        end)
        if deviceData.getChannel and deviceData:getChannel() ~= VR.CHANNEL then
            deviceData:setChannel(VR.CHANNEL)
        end
    end
    if deviceData.setHasBattery then
        deviceData:setHasBattery(true)
    end
    if deviceData.setPower then
        deviceData:setPower(1)
    end
    -- Block vanilla program/static bed on the part emitter; music is BWORadio world emitter only.
    if deviceData.setNoTransmit then
        deviceData:setNoTransmit(true)
    end
    return true
end

function VR.tuneDeviceData(deviceData)
    if not deviceData then
        return false
    end
    if deviceData.setIsTurnedOn then
        deviceData:setIsTurnedOn(true)
    end
    VR.tuneChannelOnly(deviceData)
    if deviceData.setDeviceVolume then
        deviceData:setDeviceVolume(VR.DEFAULT_VOLUME)
    end
    return true
end

function VR.needsRestlessPreset(vehicle)
    local _, deviceData = VR.findRadioDeviceData(vehicle)
    if not deviceData then
        return false
    end
    if not VR.isRadioPoweredOn(deviceData) then
        return true
    end
    if deviceData.getChannel then
        local ch = deviceData:getChannel()
        if ch ~= VR.CHANNEL then
            return true
        end
    end
    return false
end

function VR.commitRadioPart(vehicle, part, deviceData)
    if part and part.setDeviceData and deviceData then
        pcall(function()
            part:setDeviceData(deviceData)
        end)
    end
    if vehicle and vehicle.transmitModData then
        vehicle:transmitModData()
    end
end

function VR.applyDefaultRadioToVehicle(vehicle, force)
    if not vehicle then
        return false
    end
    if not force and VR.hasPreset(vehicle) and not VR.needsRestlessPreset(vehicle) then
        return false
    end
    local part, deviceData = VR.findRadioDeviceData(vehicle)
    if not deviceData then
        return false
    end
    VR.tuneDeviceData(deviceData)
    local md = VR.getVehicleModData(vehicle)
    if md then
        md[VR.PRESET_FLAG] = true
    end
    VR.commitRadioPart(vehicle, part, deviceData)
    return true
end

-- Player entered a vehicle: radio on, 88.880 FM, default volume (always re-apply).
function VR.ensureRestlessRadioOnEnter(vehicle)
    return VR.applyDefaultRadioToVehicle(vehicle, true) == true
end

function VR.queueVehicleRadioPreset(vehicle)
    if not vehicle then
        return
    end
    VR.spawnQueue[#VR.spawnQueue + 1] = vehicle
end

local function isVehicleGone(vehicle)
    if not vehicle then
        return true
    end
    if vehicle.isRemovedFromWorld and vehicle:isRemovedFromWorld() then
        return true
    end
    if vehicle.getSquare and not vehicle:getSquare() then
        return true
    end
    return false
end

function VR.processSpawnQueue()
    if not VR.spawnQueue[1] then
        return
    end
    local processed = 0
    local i = 1
    while i <= #VR.spawnQueue and processed < VR.SPAWN_QUEUE_MAX_PER_TICK do
        local vehicle = VR.spawnQueue[i]
        if isVehicleGone(vehicle) then
            table.remove(VR.spawnQueue, i)
        elseif VR.applyDefaultRadioToVehicle(vehicle, true) then
            table.remove(VR.spawnQueue, i)
            processed = processed + 1
        else
            i = i + 1
        end
    end
    if #VR.spawnQueue > 200 then
        local tail = {}
        for j = #VR.spawnQueue - 99, #VR.spawnQueue do
            tail[#tail + 1] = VR.spawnQueue[j]
        end
        VR.spawnQueue = tail
    end
end

function VR.autoTuneOnEnter(vehicle)
    return VR.ensureRestlessRadioOnEnter(vehicle)
end

function VR.isChannelRegistered()
    local zr = getZomboidRadio and getZomboidRadio()
    if not zr or not zr.getChannelName then
        return false
    end
    local name = zr:getChannelName(VR.CHANNEL)
    return name ~= nil and name ~= ""
end

local function logRegistration()
    if VR.isChannelRegistered() then
        local zr = getZomboidRadio()
        local name = zr:getChannelName(VR.CHANNEL)
        print("[" .. MOD_ID .. "] " .. tostring(name) .. " registered at " .. (VR.CHANNEL / 1000) .. " MHz")
    else
        print("[" .. MOD_ID .. "] WARN: Restless Dreams missing from radio data (check media/radio/RestlessDreams.xml)")
    end
end

if Events and Events.OnGameStart then
    Events.OnGameStart.Add(logRegistration)
elseif Events and Events.OnInitWorld then
    Events.OnInitWorld.Add(logRegistration)
end

local function forEachOnlinePlayer(fn)
    if getOnlinePlayers then
        local op = getOnlinePlayers()
        if op and op.size then
            for i = 0, op:size() - 1 do
                fn(op:get(i))
            end
            return
        end
    end
    local p = getSpecificPlayer and getSpecificPlayer(0)
    if p then
        fn(p)
    end
end

local function applyPresetsInCell(cell)
    if not cell or not cell.getVehicles then
        return
    end
    local list = cell:getVehicles()
    if not list or not list.size then
        return
    end
    for vi = 0, list:size() - 1 do
        VR.applyDefaultRadioToVehicle(list:get(vi), false)
    end
end

local function applyPresetsNearPlayer(player)
    if not player or player:isDead() then
        return
    end
    local cell = player:getCell()
    if not cell then
        return
    end
    applyPresetsInCell(cell)
end

local function tickVehicleRadioPresets()
    VR.processSpawnQueue()
    local scanTick = (tickVehicleRadioPresets._n or 0) + 1
    tickVehicleRadioPresets._n = scanTick
    if ModpackFestivalTick and ModpackFestivalTick.every(scanTick, ModpackFestivalTick.GAME or 60) then
        forEachOnlinePlayer(applyPresetsNearPlayer)
    end
end

local function onSpawnVehicleStart(vehicle)
    VR.queueVehicleRadioPreset(vehicle)
end

if isServer() then
    if Events and Events.OnTick then
        Events.OnTick.Add(tickVehicleRadioPresets)
    end
    if Events and Events.OnSpawnVehicleStart then
        Events.OnSpawnVehicleStart.Add(onSpawnVehicleStart)
    end
end

-- Enter hooks: ModpackFestivalSpawn_VehicleRadioHooksClient.lua (client).

print("[" .. MOD_ID .. "] vehicle radio: every car spawns/loads with Restless Dreams "
    .. (VR.CHANNEL / 1000) .. " MHz on, vol " .. (VR.DEFAULT_VOLUME or 19))
