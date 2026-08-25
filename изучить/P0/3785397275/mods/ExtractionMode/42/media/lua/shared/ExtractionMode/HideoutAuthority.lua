ExtractionMode = ExtractionMode or {}
if isClient and isClient() then
    ExtractionMode.HideoutAuthority = ExtractionMode.HideoutAuthority or {}
    return ExtractionMode.HideoutAuthority
end

require "ExtractionMode/Config"
require "ExtractionMode/Util"
require "ExtractionMode/Upgrades"
require "ExtractionMode/Generator"
require "ExtractionMode/HideoutUtilities"
require "ExtractionMode/ModCompatibility"

local Config = ExtractionMode.Config
local Util = ExtractionMode.Util
local Upgrades = ExtractionMode.Upgrades
local Generator = ExtractionMode.Generator
local HideoutUtilities = ExtractionMode.HideoutUtilities
local Compatibility = ExtractionMode.ModCompatibility
local Runtime = ExtractionMode.RaidRuntime
local Hideout = {}
local lastGeneratorBroadcast = -1

local function maxLoadedZ(cell, minimum)
    minimum = tonumber(minimum) or 0
    if cell == nil then return math.min(31, minimum) end
    local maximum = minimum
    pcall(function() maximum = math.max(minimum, tonumber(cell:getMaxZ()) or minimum) end)
    return math.min(31, maximum)
end

local function isIndividualLamp(object)
    if object == nil or not instanceof(object, "IsoLightSwitch") then return false end
    local lamp = false
    pcall(function() if object:getCanBeModified() or object:getUseBattery() then lamp = true end end)
    pcall(function()
        local lightRoom = object.lightRoom
        if lightRoom == false then lamp = true end
    end)
    pcall(function()
        local sprite = object:getSprite()
        local properties = sprite and sprite:getProperties()
        if properties and (properties:has(IsoPropertyType.RED_LIGHT)
            or properties:has("lightR") or properties:has("LightR")) then
            lamp = true
        end
    end)
    pcall(function()
        local lights = object:getLights()
        if lights and lights:size() > 0 then lamp = true end
    end)
    return lamp
end

local function setSquareLightsActivated(square, activated, synchronize, fixedOnly)
    local objects = square and square:getObjects()
    if objects == nil then return end
    for index = 0, objects:size() - 1 do
        local object = objects:get(index)
        if object and instanceof(object, "IsoLightSwitch")
            and (fixedOnly ~= true or not isIndividualLamp(object)) then
            local current = nil
            pcall(function() current = object:isActivated() end)
            local correctionNeeded = current ~= nil and current ~= activated
            pcall(function()
                local room = square:getRoom()
                local definition = room and room:getRoomDef()
                if definition and definition.lightsActive ~= activated then correctionNeeded = true end
                local lights = object:getLights()
                if lights then
                    for lightIndex = 0, lights:size() - 1 do
                        if lights:get(lightIndex):isActive() ~= activated then
                            correctionNeeded = true
                            break
                        end
                    end
                end
            end)
            if current ~= nil and correctionNeeded then
                pcall(function()
                    if current ~= activated then object:setActive(activated, false, true) end
                    local actual = object:isActivated()
                    object:switchLight(actual)
                    if synchronize and object.syncIsoObject then
                        object:syncIsoObject(false, actual and 1 or 0, nil, nil)
                    end
                end)
            end
        end
    end
end

local function visitLoadedHideoutSquares(visitor)
    local cell = getCell and getCell()
    if cell == nil then return end
    local hideout = Config.hideout()
    local bounds = Config.hideoutCellBounds()
    local radius = math.max(1, math.min(50, math.floor(hideout.radius)))
    local minimumX = math.floor(hideout.x) - radius
    local maximumX = math.floor(hideout.x) + radius
    local minimumY = math.floor(hideout.y) - radius
    local maximumY = math.floor(hideout.y) + radius
    local anchorZ = math.floor(tonumber(hideout.z) or 0)
    local minimumZ = anchorZ
    local maximumZ = maxLoadedZ(cell, anchorZ)
    local anchor = cell:getGridSquare(math.floor(hideout.x), math.floor(hideout.y), anchorZ)
    local hideoutBuilding = anchor and anchor:getBuilding()
    pcall(function()
        minimumZ = math.max(-32, math.min(anchorZ, tonumber(cell:getMinZ()) or anchorZ))
        local definition = hideoutBuilding and hideoutBuilding:getDef()
        if definition then
            minimumX = math.max(bounds.minX, definition:getX())
            maximumX = math.min(bounds.maxXExclusive - 1, definition:getX2())
            minimumY = math.max(bounds.minY, definition:getY())
            maximumY = math.min(bounds.maxYExclusive - 1, definition:getY2())
        end
    end)
    for x = minimumX, maximumX do
        for y = minimumY, maximumY do
            for z = minimumZ, maximumZ do
                local square = cell:getGridSquare(x, y, z)
                if square and (hideoutBuilding == nil or square:getBuilding() == hideoutBuilding) then
                    visitor(square)
                end
            end
        end
    end
end

function Hideout.activateInstalledLighting()
    local data = Runtime.currentStore()
    local powered = data.generatorRunning == true and (tonumber(data.generatorFuel) or 0) > 0.0001
    HideoutUtilities.ensureGridIsolation(powered)
    local visitedChunks = {}
    local cell = getCell and getCell()
    local hideout = Config.hideout()
    local anchorZ = math.floor(tonumber(hideout.z) or 0)
    HideoutUtilities.setVirtualPowerArea(cell, hideout, powered, visitedChunks,
        anchorZ, maxLoadedZ(cell, anchorZ))
    visitLoadedHideoutSquares(function(square)
        HideoutUtilities.setVirtualPower(square, powered, visitedChunks)
        square:setHaveElectricity(powered)
        setSquareLightsActivated(square, powered, true, true)
    end)
end

function Hideout.refreshUtilities()
    local data = Runtime.currentStore()
    local lightingInstalled = Upgrades.isInstalled(data.upgrades, "lighting")
    local powered = data.generatorRunning == true and (tonumber(data.generatorFuel) or 0) > 0.0001
    HideoutUtilities.ensureGridIsolation(powered)
    local visitedChunks = {}
    local cell = getCell and getCell()
    local hideout = Config.hideout()
    local anchorZ = math.floor(tonumber(hideout.z) or 0)
    HideoutUtilities.setVirtualPowerArea(cell, hideout, powered, visitedChunks,
        anchorZ, maxLoadedZ(cell, anchorZ))
    visitLoadedHideoutSquares(function(square)
        HideoutUtilities.setVirtualPower(square, powered, visitedChunks)
        square:setHaveElectricity(powered)
        HideoutUtilities.setPipedWaterAvailable(square, powered)
        if not powered then
            setSquareLightsActivated(square, false, true, false)
        elseif not lightingInstalled then
            setSquareLightsActivated(square, false, true, true)
        else
            setSquareLightsActivated(square, true, true, true)
        end
    end)
end

-- LG Extended Electricity and vanilla both rebuild generator-position caches as
-- chunks stream. Reassert Extraction's virtual source immediately for newly
-- loaded hideout squares so LGEE cannot leave a room dark until a later refresh.
function Hideout.onLoadGridSquare(square)
    if not Compatibility.isExtendedElectricityActive() or square == nil
        or Runtime == nil or Runtime.initialized ~= true
        or not HideoutUtilities.squareInsideVirtualPowerArea(square) then return end
    local data = Runtime.currentStore()
    if data == nil then return end
    local powered = data.generatorRunning == true
        and (tonumber(data.generatorFuel) or 0) > 0.0001
    HideoutUtilities.setVirtualPower(square, powered, {})
    pcall(function() square:setHaveElectricity(powered) end)
end

function Hideout.processGenerator(nowSecond)
    local data = Runtime.currentStore()
    local nowHour = Util.worldHours()
    local previousHour = tonumber(data.generatorLastWorldHour) or nowHour
    data.generatorLastWorldHour = nowHour
    local elapsedHours = math.max(0, nowHour - previousHour)
    if data.generatorRunning ~= true then return end
    local previousFuel = math.max(0, tonumber(data.generatorFuel) or 0)
    data.generatorFuel = math.max(0, previousFuel - elapsedHours
        * Generator.fuelPerHour(data.upgrades, Runtime.generatorUsageMultiplier()))
    if data.generatorFuel <= 0.0001 then
        data.generatorFuel = 0
        data.generatorRunning = false
        Hideout.refreshUtilities()
        Runtime.broadcastState()
        Runtime.announceLocalized("IGUI_ExtractionMode_Message_GeneratorEmpty",
            "The hideout generator has run out of gasoline. Electricity and water are offline.")
        return
    end
    if nowSecond - lastGeneratorBroadcast >= 5 then
        lastGeneratorBroadcast = nowSecond
        Runtime.broadcastState()
    end
end

ExtractionMode.HideoutAuthority = Hideout
return Hideout
