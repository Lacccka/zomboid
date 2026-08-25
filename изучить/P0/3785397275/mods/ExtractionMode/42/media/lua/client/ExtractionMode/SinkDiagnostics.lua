require "ExtractionMode/Config"
require "ISUI/ISWorldObjectContextMenu"

ExtractionMode = ExtractionMode or {}
local Config = ExtractionMode.Config
local Diagnostics = {}
local probeNumber = 0
local reportedContextHook = false

local function safeValue(callback, fallback)
    local ok, value = pcall(callback)
    if ok then return value end
    return fallback or ("<error: " .. tostring(value) .. ">")
end

local function enabled()
    if Config.value("DebugLogging") ~= true then return false end
    return safeValue(function()
        local mods = getActivatedMods()
        return mods ~= nil and mods:contains("ExtractionModeWIP")
    end, false) == true
end

local function isPiped(object)
    return object ~= nil and safeValue(function()
        local properties = object:getProperties()
        return properties ~= nil and properties:has(IsoFlagType.waterPiped)
    end, false) == true
end

local function firstPipedObject(worldObjects)
    for _, object in ipairs(worldObjects or {}) do
        if isPiped(object) then return object end
    end
    return nil
end

local function addObjects(result, seen, objects)
    if objects == nil then return end
    for index = 0, objects:size() - 1 do
        local object = objects:get(index)
        local key = tostring(object)
        if object ~= nil and not seen[key] then
            seen[key] = true
            result[#result + 1] = object
        end
    end
end

local function nearbyObjects(worldObjects)
    local result = {}
    local seen = {}
    local visitedSquares = {}

    for _, clickedObject in ipairs(worldObjects or {}) do
        local clickedSquare = clickedObject and clickedObject:getSquare() or nil
        if clickedSquare ~= nil then
            local z = clickedSquare:getZ()
            for offsetY = -1, 1 do
                for offsetX = -1, 1 do
                    local x = clickedSquare:getX() + offsetX
                    local y = clickedSquare:getY() + offsetY
                    local squareKey = tostring(x) .. ":" .. tostring(y) .. ":" .. tostring(z)
                    if not visitedSquares[squareKey] then
                        visitedSquares[squareKey] = true
                        local square = getCell():getGridSquare(x, y, z)
                        if square ~= nil then
                            addObjects(result, seen, square:getObjects())
                            addObjects(result, seen, square:getSpecialObjects())
                        end
                    end
                end
            end
        end
    end

    return result
end

local function printable(value)
    if value == nil then return "<nil>" end
    return tostring(value)
end

local function logLine(probe, label, value)
    print("[ExtractionMode][SinkDebug][" .. tostring(probe) .. "] "
        .. tostring(label) .. "=" .. printable(value))
end

local function listProperties(properties)
    if properties == nil then return "<nil>" end
    return safeValue(function()
        local entries = {}
        local names = properties:getPropertyNames()
        for index = 0, math.min(names:size() - 1, 127) do
            local name = tostring(names:get(index))
            entries[#entries + 1] = name .. ":" .. tostring(properties:get(name))
        end
        table.sort(entries)
        return table.concat(entries, " | ")
    end)
end

local function listFlags(properties)
    if properties == nil then return "<nil>" end
    return safeValue(function()
        local result = {}
        local flags = properties:getFlagsList()
        for index = 0, math.min(flags:size() - 1, 127) do
            result[#result + 1] = tostring(flags:get(index))
        end
        table.sort(result)
        return table.concat(result, ",")
    end)
end

local function listModData(modData)
    if modData == nil then return "<nil>" end
    local entries = {}
    for key, value in pairs(modData) do
        local valueType = type(value)
        if valueType == "string" or valueType == "number" or valueType == "boolean" then
            entries[#entries + 1] = tostring(key) .. ":" .. tostring(value)
        end
    end
    table.sort(entries)
    return table.concat(entries, " | ")
end

local function listZones(square)
    if square == nil then return "<no square>" end
    return safeValue(function()
        local world = getWorld()
        local metaGrid = world and world:getMetaGrid()
        local zones = metaGrid and metaGrid:getZonesAt(square:getX(), square:getY(), square:getZ())
        if zones == nil or zones:size() == 0 then return "<none>" end
        local result = {}
        for index = 0, zones:size() - 1 do
            local zone = zones:get(index)
            result[#result + 1] = tostring(zone:getName()) .. "/" .. tostring(zone:getType())
                .. "@" .. tostring(zone:getX()) .. "," .. tostring(zone:getY())
                .. "," .. tostring(zone:getZ()) .. "+" .. tostring(zone:getWidth())
                .. "x" .. tostring(zone:getHeight())
        end
        return table.concat(result, " | ")
    end)
end

function Diagnostics.logSink(object, player)
    probeNumber = probeNumber + 1
    local probe = probeNumber
    local square = safeValue(function() return object:getSquare() end, nil)
    local sprite = safeValue(function() return object:getSprite() end, nil)
    local properties = safeValue(function() return object:getProperties() end, nil)
    local fluidContainer = safeValue(function() return object:getFluidContainer() end, nil)
    local modData = safeValue(function() return object:getModData() end, nil)
    local state = ExtractionMode.ClientState or {}
    local generator = state.generator or {}
    local hideout = state.hideout or Config.hideout()
    local anchor = safeValue(function()
        return getCell():getGridSquare(math.floor(hideout.x), math.floor(hideout.y),
            math.floor(tonumber(hideout.z) or 0))
    end, nil)
    local anchorBuilding = anchor and safeValue(function() return anchor:getBuilding() end, nil) or nil
    local objectBuilding = square and safeValue(function() return square:getBuilding() end, nil) or nil
    local room = square and safeValue(function() return square:getRoom() end, nil) or nil

    print("[ExtractionMode][SinkDebug][" .. tostring(probe) .. "] ===== BEGIN READ-ONLY SINK PROBE =====")
    logLine(probe, "modVersion", "0.8.45")
    logLine(probe, "clientState", state.state)
    logLine(probe, "generator.running", generator.running)
    logLine(probe, "generator.fuel", generator.fuel)
    logLine(probe, "generator.capacity", generator.capacity)
    logLine(probe, "player", safeValue(function() return player:getUsername() end))
    logLine(probe, "player.position", safeValue(function()
        return tostring(player:getX()) .. "," .. tostring(player:getY()) .. "," .. tostring(player:getZ())
    end))
    logLine(probe, "object", object)
    logLine(probe, "object.class", safeValue(function() return object:getClass():getName() end))
    logLine(probe, "object.index", safeValue(function() return object:getObjectIndex() end))
    logLine(probe, "object.name", safeValue(function() return object:getName() end))
    logLine(probe, "object.objectName", safeValue(function() return object:getObjectName() end))
    logLine(probe, "sprite.name", sprite and safeValue(function() return sprite:getName() end) or nil)
    logLine(probe, "square.position", square and (tostring(square:getX()) .. ","
        .. tostring(square:getY()) .. "," .. tostring(square:getZ())) or nil)
    logLine(probe, "square.outside", square and safeValue(function() return square:isOutside() end) or nil)
    logLine(probe, "square.haveElectricity", square and safeValue(function() return square:haveElectricity() end) or nil)
    logLine(probe, "square.hasGridPower", square and safeValue(function() return square:hasGridPower() end) or nil)
    logLine(probe, "room.name", room and safeValue(function() return room:getRoomDef():getName() end) or nil)
    logLine(probe, "building.matchesHideoutAnchor", objectBuilding ~= nil and objectBuilding == anchorBuilding)
    logLine(probe, "zones", listZones(square))
    logLine(probe, "properties.waterPiped", isPiped(object))
    logLine(probe, "properties.all", listProperties(properties))
    logLine(probe, "properties.flags", listFlags(properties))
    logLine(probe, "fluidContainer.present", fluidContainer ~= nil)
    logLine(probe, "fluidContainer.capacity", fluidContainer and safeValue(function() return fluidContainer:getCapacity() end) or nil)
    logLine(probe, "fluidContainer.amount", fluidContainer and safeValue(function() return fluidContainer:getAmount() end) or nil)
    logLine(probe, "object.getFluidCapacity", safeValue(function() return object:getFluidCapacity() end))
    logLine(probe, "object.getFluidAmount", safeValue(function() return object:getFluidAmount() end))
    logLine(probe, "object.hasWater", safeValue(function() return object:hasWater() end))
    logLine(probe, "external.getUses", safeValue(function() return object:getUsesExternalWaterSource() end))
    logLine(probe, "external.has", safeValue(function() return object:hasExternalWaterSource() end))
    logLine(probe, "modData.allScalars", listModData(modData))
    logLine(probe, "sandbox.waterShutModifier", safeValue(function()
        return getSandboxOptions():getWaterShutModifier()
    end))
    logLine(probe, "worldAgeHours", safeValue(function() return getGameTime():getWorldAgeHours() end))
    print("[ExtractionMode][SinkDebug][" .. tostring(probe) .. "] ===== END READ-ONLY SINK PROBE =====")
end

function Diagnostics.logContext(worldObjects, player)
    probeNumber = probeNumber + 1
    local probe = probeNumber
    local objects = nearbyObjects(worldObjects)
    local sink = firstPipedObject(objects)

    print("[ExtractionMode][SinkDebug][" .. tostring(probe) .. "] ===== BEGIN NEARBY OBJECT LIST =====")
    logLine(probe, "clicked.count", #(worldObjects or {}))
    logLine(probe, "nearby.count", #objects)
    for index, object in ipairs(objects) do
        if index > 128 then
            logLine(probe, "clicked.truncated", true)
            break
        end
        local square = safeValue(function() return object:getSquare() end, nil)
        local sprite = safeValue(function() return object:getSprite() end, nil)
        local prefix = "clicked[" .. tostring(index) .. "]"
        logLine(probe, prefix .. ".object", object)
        logLine(probe, prefix .. ".class", safeValue(function() return object:getClass():getName() end))
        logLine(probe, prefix .. ".sprite", sprite and safeValue(function() return sprite:getName() end) or nil)
        logLine(probe, prefix .. ".square", square and (tostring(square:getX()) .. ","
            .. tostring(square:getY()) .. "," .. tostring(square:getZ())) or nil)
        logLine(probe, prefix .. ".waterPiped", isPiped(object))
        logLine(probe, prefix .. ".fluidCapacity", safeValue(function() return object:getFluidCapacity() end))
        logLine(probe, prefix .. ".fluidAmount", safeValue(function() return object:getFluidAmount() end))
    end
    logLine(probe, "clicked.pipedObjectFound", sink ~= nil)
    print("[ExtractionMode][SinkDebug][" .. tostring(probe) .. "] ===== END NEARBY OBJECT LIST =====")

    if sink ~= nil then
        Diagnostics.logSink(sink, player)
    end
end

local function onWorldContext(playerNum, context, worldObjects, test)
    if not enabled() then return end
    if not reportedContextHook then
        reportedContextHook = true
        print("[ExtractionMode][SinkDebug] Build 42 pre-context hook observed; diagnostic menu is active")
    end
    if test then
        ISWorldObjectContextMenu.setTest()
        return
    end
    local player = getSpecificPlayer(playerNum)
    context:addOption("DEBUG: Log Clicked Water Context", worldObjects, Diagnostics.logContext, player)
end

Events.OnPreFillWorldObjectContextMenu.Add(onWorldContext)

ExtractionMode.SinkDiagnostics = Diagnostics
return Diagnostics
