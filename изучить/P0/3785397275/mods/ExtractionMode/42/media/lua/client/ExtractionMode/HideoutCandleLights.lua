require "ExtractionMode/Config"
require "ExtractionMode/Util"

ExtractionMode = ExtractionMode or {}
local Config = ExtractionMode.Config
local Util = ExtractionMode.Util

local lights = {}
local lastRefreshSecond = -1

local function removeLight(entry)
    if entry and entry.light and getCell then
        pcall(function() getCell():removeLamppost(entry.light) end)
    end
end

local function clearLights()
    for _, entry in pairs(lights) do removeLight(entry) end
    lights = {}
end

local function candleItem(worldObject)
    if worldObject == nil or not instanceof(worldObject, "IsoWorldInventoryObject") then return nil end
    local item = worldObject:getItem()
    if item == nil or item:getFullType() ~= "Base.CandleLit" then return nil end
    local modData = item:getModData()
    if modData == nil or modData.ExtractionModeHideoutCandle ~= true then return nil end
    return item
end

local function refreshLights()
    local player = getPlayer and getPlayer()
    local hideout = Config.hideout()
    if player == nil or not Util.playerNear(player, hideout, hideout.radius + 12) then
        clearLights()
        return
    end

    local cell = getCell and getCell()
    if cell == nil or IsoLightSource == nil then return end
    local seen = {}
    local radius = math.max(4, math.floor(tonumber(hideout.radius) or 14))
    local minimumZ = math.floor(tonumber(hideout.z) or 0)
    local maximumZ = minimumZ
    pcall(function() maximumZ = math.min(31, math.max(minimumZ, tonumber(cell:getMaxZ()) or minimumZ)) end)

    for x = math.floor(hideout.x) - radius, math.floor(hideout.x) + radius do
        for y = math.floor(hideout.y) - radius, math.floor(hideout.y) + radius do
            for z = minimumZ, maximumZ do
                local square = cell:getGridSquare(x, y, z)
                local objects = square and square:getWorldObjects()
                if objects then
                    for index = 0, objects:size() - 1 do
                        local item = candleItem(objects:get(index))
                        if item then
                            local key = tostring(item:getID())
                            seen[key] = true
                            if lights[key] == nil then
                                local light = IsoLightSource.new(x, y, z, 1.0, 0.55, 0.22, 5)
                                if light then
                                    light:setActive(true)
                                    cell:addLamppost(light)
                                    lights[key] = { light = light }
                                end
                            end
                        end
                    end
                end
            end
        end
    end

    for key, entry in pairs(lights) do
        if seen[key] ~= true then
            removeLight(entry)
            lights[key] = nil
        end
    end
end

local function onTick()
    local nowSecond = math.floor(Util.nowMs() / 1000)
    if nowSecond == lastRefreshSecond then return end
    lastRefreshSecond = nowSecond
    refreshLights()
end

Events.OnTick.Add(onTick)
Events.OnDisconnect.Add(clearLights)
Events.OnMainMenuEnter.Add(clearLights)

