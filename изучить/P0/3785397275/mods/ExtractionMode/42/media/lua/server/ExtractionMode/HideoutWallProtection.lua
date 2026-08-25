require "ExtractionMode/Config"
require "ExtractionMode/Util"

if isClient and isClient() then return end

ExtractionMode = ExtractionMode or {}
local Config = ExtractionMode.Config
local Util = ExtractionMode.Util
local Protection = {}

local SCAN_INTERVAL_SECONDS = 1
local DESTRUCTIBLE_HIDEOUT_TILESET_PREFIX = "location_shop_mall_01_"
local lastScanSecond = -SCAN_INTERVAL_SECONDS
local logged = false

local function hideoutBuilding(cell, hideout)
    local anchor = cell and cell:getGridSquare(math.floor(hideout.x), math.floor(hideout.y),
        math.floor(tonumber(hideout.z) or 0))
    return anchor and anchor:getBuilding()
end

local function authoredWall(object)
    if object == nil or not object.isWall or not object:isWall() then return false end
    -- IsoDoor and IsoWindow sprites also carry WallN/WallW flags, so isWall()
    -- alone does not identify a static wall. Transmitting mod-data for those
    -- mapped special objects can make Build 42 remove them while synchronizing
    -- a newly loaded multiplayer chunk.
    if instanceof and (instanceof(object, "IsoDoor")
        or instanceof(object, "IsoWindow")
        or instanceof(object, "IsoThumpable")) then return false end
    local spriteName = object:getSpriteName()
    if spriteName ~= nil and tostring(spriteName):sub(
        1, #DESTRUCTIBLE_HIDEOUT_TILESET_PREFIX) == DESTRUCTIBLE_HIDEOUT_TILESET_PREFIX then return false end
    return true
end

local function protectSquare(square, building)
    if square == nil or square:getBuilding() ~= building then return 0, 0 end
    local objects = square:getObjects()
    if objects == nil then return 0, 0 end

    local protectedCount = 0
    local releasedCount = 0
    local protectedGarage = Config.isHideoutGarageProtectedSquare(square)
    for index = 0, objects:size() - 1 do
        local object = objects:get(index)
        local modData = object:getModData()
        local spriteName = object:getSpriteName()
        local destructibleTile = spriteName ~= nil and tostring(spriteName):sub(
            1, #DESTRUCTIBLE_HIDEOUT_TILESET_PREFIX) == DESTRUCTIBLE_HIDEOUT_TILESET_PREFIX
        if protectedGarage then
            -- The garage rectangle overrides the mall-tileset wall exception:
            -- every authored object here is part of the reserved vehicle bay.
            modData.ExtractionModeIndestructible = true
            modData.ExtractionModeProtectedGarage = true
            protectedCount = protectedCount + 1
        elseif destructibleTile and modData.ExtractionModeHideoutWall == true then
            -- Migrate walls marked by the first implementation in existing saves.
            modData.ExtractionModeHideoutWall = nil
            modData.ExtractionModeIndestructible = nil
            releasedCount = releasedCount + 1
        elseif authoredWall(object) then
            modData.ExtractionModeIndestructible = true
            modData.ExtractionModeHideoutWall = true
            -- These are static mapped walls, not zombie-thumpable constructions.
            -- The client guard identifies them independently from their mapped
            -- wall flags. Do not transmit per-object mod-data here: Build 42 can
            -- remove mapped special objects while reconciling those packets.
            protectedCount = protectedCount + 1
        end
    end
    return protectedCount, releasedCount
end

function Protection.refresh()
    local cell = getCell and getCell()
    if cell == nil then return 0 end
    local hideout = Config.hideout()
    local building = hideoutBuilding(cell, hideout)
    if building == nil then return 0 end

    local radius = math.max(4, math.min(50, math.floor(tonumber(hideout.radius) or 14)))
    local minimumX = math.floor(hideout.x) - radius
    local maximumX = math.floor(hideout.x) + radius
    local minimumY = math.floor(hideout.y) - radius
    local maximumY = math.floor(hideout.y) + radius
    local minimumZ = math.floor(tonumber(hideout.z) or 0)
    local maximumZ = minimumZ
    local bounds = Config.hideoutCellBounds()
    pcall(function()
        local definition = building:getDef()
        if definition then
            minimumX = math.max(bounds.minX, definition:getX())
            maximumX = math.min(bounds.maxXExclusive - 1, definition:getX2())
            minimumY = math.max(bounds.minY, definition:getY())
            maximumY = math.min(bounds.maxYExclusive - 1, definition:getY2())
            minimumZ = math.max(-32, tonumber(definition:getMinLevel()) or minimumZ)
            maximumZ = math.min(31, tonumber(definition:getMaxLevel()) or minimumZ)
        end
    end)

    local protectedCount = 0
    local releasedCount = 0
    for x = minimumX, maximumX do
        for y = minimumY, maximumY do
            for z = minimumZ, maximumZ do
                local protectedOnSquare, releasedOnSquare = protectSquare(
                    cell:getGridSquare(x, y, z), building)
                protectedCount = protectedCount + protectedOnSquare
                releasedCount = releasedCount + releasedOnSquare
            end
        end
    end
    if protectedCount > 0 and not logged then
        logged = true
        Util.log("Protected " .. tostring(protectedCount) .. " authored hideout wall tile(s)")
    end
    if releasedCount > 0 then
        Util.log("Restored destructibility for " .. tostring(releasedCount)
            .. " location_shop_mall_01 hideout wall tile(s)")
    end
    return protectedCount
end

local function onGridSquareLoaded(square)
    local cell = getCell and getCell()
    if cell == nil or square == nil then return end
    local building = hideoutBuilding(cell, Config.hideout())
    if building then protectSquare(square, building) end
end

local function onTick()
    local nowSecond = math.floor(Util.nowMs() / 1000)
    if nowSecond - lastScanSecond < SCAN_INTERVAL_SECONDS then return end
    lastScanSecond = nowSecond
    Protection.refresh()
end

if Events.LoadGridsquare then Events.LoadGridsquare.Add(onGridSquareLoaded) end
Events.OnTick.Add(onTick)

ExtractionMode.HideoutWallProtection = Protection
return Protection
