-- Guaranteed starter car at the quest car location.

if isClient() and not isServer() then
    return
end

ModpackFestivalVehicleServer = ModpackFestivalVehicleServer or {}

local MOD_ID = "ModpackFestivalSpawn"
local LOG_PREFIX = "[" .. MOD_ID .. "][StarterVehicle] "

local VEHICLE_TYPE = "Base.CarNormal"
local VEHICLE_DIRECTION = IsoDirections and IsoDirections.E or nil
local DEFAULT_X = 13855
local DEFAULT_Y = 1907
local DEFAULT_Z = 0
local START_DELAY_TICKS = 90
local RETRY_INTERVAL = 30
local WAITING_LOG_INTERVAL = 600
local FEATURE_VERSION = 3
local STARTING_FUEL_LITRES = 40

local tickCount = 0
local done = false

local function getTarget()
    if ModpackFestivalQuests then
        return ModpackFestivalQuests.VEHICLE_SPAWN_X or DEFAULT_X,
            ModpackFestivalQuests.VEHICLE_SPAWN_Y or DEFAULT_Y,
            ModpackFestivalQuests.VEHICLE_SPAWN_Z or DEFAULT_Z
    end
    return DEFAULT_X, DEFAULT_Y, DEFAULT_Z
end

local function getState()
    local md = ModData.getOrCreate(MOD_ID)
    md.starterVehicle = md.starterVehicle or {}
    local st = md.starterVehicle
    if st.version ~= FEATURE_VERSION then
        st.done = nil
        st.disabled = nil
        st.vehicleId = nil
        st.finishedReason = nil
        st.lastWaitingLogTick = nil
        st.version = FEATURE_VERSION
    end
    return st
end

local function tagStarterVehicle(vehicle)
    if not vehicle or not vehicle.getModData then
        return
    end
    local md = vehicle:getModData()
    md.ModpackFestivalSpawn = md.ModpackFestivalSpawn or {}
    md.ModpackFestivalSpawn.starterCar = true
    md.ModpackFestivalSpawn.featureVersion = FEATURE_VERSION
    if vehicle.getId then
        md.ModpackFestivalSpawn.vehicleId = vehicle:getId()
    end
    if vehicle.transmitModData then
        vehicle:transmitModData()
    end
end

local function addItemToVehiclePart(vehicle, partId, itemType)
    pcall(function()
        local part = vehicle.getPartById and vehicle:getPartById(partId)
        local container = part and part.getItemContainer and part:getItemContainer()
        if not container then return end
        local item = container:AddItem(itemType)
        if item and sendAddItemToContainer then
            pcall(sendAddItemToContainer, container, item)
        end
    end)
end

local function stockStarterVehicle(vehicle)
    -- Glove box: pistol + 2 magazines + ammo box
    addItemToVehiclePart(vehicle, "GloveBox", "Base.Pistol")
    addItemToVehiclePart(vehicle, "GloveBox", "Base.9mmClip")
    addItemToVehiclePart(vehicle, "GloveBox", "Base.9mmClip")
    addItemToVehiclePart(vehicle, "GloveBox", "Base.Bullets9mmBox")
    -- Trunk: baseball bat + crowbar (crowbar lets player remove barricades if needed)
    addItemToVehiclePart(vehicle, "TruckBed", "Base.BaseballBat")
    addItemToVehiclePart(vehicle, "TruckBed", "Base.Crowbar")
    print(LOG_PREFIX .. "stocked starter car glove box and trunk")
end

local function tuneStarterVehicle(vehicle)
    if not vehicle then
        return
    end
    pcall(function()
        if vehicle.repair then
            vehicle:repair()
        end
    end)
    pcall(function()
        if vehicle.setHotwired then
            vehicle:setHotwired(false)
        end
    end)
    pcall(function()
        if vehicle.setAlarmed then
            vehicle:setAlarmed(false)
        end
    end)
    pcall(function()
        if vehicle.setLocked then
            vehicle:setLocked(false)
        end
    end)
    pcall(function()
        local tank = vehicle.getPartById and vehicle:getPartById("GasTank")
        if tank and tank.setContainerContentAmount then
            local litres = STARTING_FUEL_LITRES
            if tank.getContainerCapacity then
                litres = math.min(litres, tank:getContainerCapacity())
            end
            tank:setContainerContentAmount(litres)
        end
    end)
    stockStarterVehicle(vehicle)
    if ModpackFestivalVehicleRadio and ModpackFestivalVehicleRadio.queueVehicleRadioPreset then
        ModpackFestivalVehicleRadio.queueVehicleRadioPreset(vehicle)
    end
end

local function giveKey(player, vehicle)
    if not player or not vehicle or not vehicle.createVehicleKey then
        return false
    end
    local item = vehicle:createVehicleKey()
    if not item then
        return false
    end
    player:getInventory():AddItem(item)
    if sendAddItemToContainer then
        pcall(sendAddItemToContainer, player:getInventory(), item)
    end
    return true
end

local function isSpawnSquareValid(sq)
    if not sq then
        return false
    end
    if sq.getVehicleContainer and sq:getVehicleContainer() then
        return false
    end
    if sq.isVehicleIntersecting and sq:isVehicleIntersecting() then
        return false
    end
    return true
end

local function findSpawnSquare(cell)
    local x, y, z = getTarget()
    local baseX = math.floor(x + 0.5)
    local baseY = math.floor(y + 0.5)
    local baseZ = math.floor(z + 0.5)
    local sq = cell and cell:getGridSquare(baseX, baseY, baseZ)
    if isSpawnSquareValid(sq) then
        return sq
    end
    for radius = 1, 2 do
        for dx = -radius, radius do
            for dy = -radius, radius do
                sq = cell:getGridSquare(baseX + dx, baseY + dy, baseZ)
                if isSpawnSquareValid(sq) then
                    return sq
                end
            end
        end
    end
    return nil
end

local function spawnVehicleAt(square)
    if addVehicleDebug and VEHICLE_DIRECTION then
        return addVehicleDebug(VEHICLE_TYPE, VEHICLE_DIRECTION, nil, square)
    end
    if addVehicle then
        return addVehicle(VEHICLE_TYPE, square:getX(), square:getY(), square:getZ())
    end
    return nil
end

local function trySpawnStarterVehicle(player, st)
    local cell = player and player.getCell and player:getCell()
    if not cell then
        return false
    end

    local sq = findSpawnSquare(cell)
    if not sq then
        if not st.lastWaitingLogTick or tickCount - st.lastWaitingLogTick >= WAITING_LOG_INTERVAL then
            st.lastWaitingLogTick = tickCount
            print(LOG_PREFIX .. "waiting for festival car square to load")
        end
        return false
    end

    local vehicle = spawnVehicleAt(sq)
    if not vehicle then
        print(LOG_PREFIX .. "vehicle spawn failed")
        return false
    end

    tagStarterVehicle(vehicle)
    tuneStarterVehicle(vehicle)
    giveKey(player, vehicle)

    st.done = true
    st.vehicleId = vehicle.getId and vehicle:getId() or nil
    st.finishedReason = "spawned"
    print(LOG_PREFIX .. "spawned starter car at "
        .. tostring(sq:getX()) .. "," .. tostring(sq:getY()) .. "," .. tostring(sq:getZ()))
    return true
end

local function onTick()
    tickCount = tickCount + 1
    if tickCount < START_DELAY_TICKS then
        return
    end
    if not ModpackFestivalTick.every(tickCount, RETRY_INTERVAL) then
        return
    end

    local st = getState()
    local player = getSpecificPlayer and getSpecificPlayer(0)
    if not player or not player.getSquare or not player:getSquare() then
        return
    end

    if ModpackFestivalFeatures and ModpackFestivalFeatures.isStarterVehicleEnabled
        and not ModpackFestivalFeatures.isStarterVehicleEnabled() then
        st.disabled = true
        done = true
        return
    end

    if st.done then
        done = true
        return
    end

    trySpawnStarterVehicle(player, st)
end

local function tickWrapper()
    if done then
        Events.OnTick.Remove(tickWrapper)
        return
    end
    onTick()
end

Events.OnTick.Add(tickWrapper)
